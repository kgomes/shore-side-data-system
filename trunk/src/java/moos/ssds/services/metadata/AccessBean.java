/*
 * Copyright 2009 MBARI
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1 
 * (the "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at
 *
 * http://www.gnu.org/copyleft/lesser.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package moos.ssds.services.metadata;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import moos.ssds.dao.MetadataDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.IMetadataObject;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * This class is a super class for all the service session beans. It provides
 * common functionality and keeps the subclasses clean from some of the EJB
 * callbacks that are necessary
 * 
 * @stereotype service
 * @ejb.bean generate="false"
 * @ejb.home create="true"
 *           local-class="moos.ssds.services.metadata.AccessLocalHome"
 *           remote-class="moos.ssds.services.metadata.AccessHome"
 *           extends="javax.ejb.EJBHome"
 * @ejb.interface create="true"
 *                local-class="moos.ssds.services.metadata.AccessLocal"
 *                local-extends="javax.ejb.EJBLocalObject"
 *                remote-class="moos.ssds.services.metadata.Access"
 *                extends="javax.ejb.EJBObject"
 * @ejb.resource-ref res-ref-name="${core.mail.jndi.name}"
 *                   res-type="javax.mail.Session" res-auth="Container"
 * @jboss.resource-ref res-ref-name="${core.mail.jndi.name}"
 *                     jndi-name="java:/Mail"
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.20 $
 */
public abstract class AccessBean implements SessionBean {

    /**
     * This method returns the <code>SessionContext</code> for the session
     * bean
     * 
     * @return <code>SessionContext</code>
     */
    public SessionContext getSessionContext() {
        return this.ctx;
    }

    /**
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(SessionContext sessionContext)
        throws EJBException, RemoteException {
        this.ctx = sessionContext;
    }

    /**
     * Returns the the persistent class this session bean is associated with
     * 
     * @return the <code>Class</code> that the bean is associated with
     */
    protected Class getPersistentClass() {
        return persistentClass;
    }

    /**
     * This method sets the proper <code>Class</code> that the bean has to act
     * on behalf of
     * 
     * @param persistentClass
     *            is the <code>Class</code> that will be used in the bean
     *            transactions
     */
    protected void setPersistentClass(Class persistentClass) {
        this.persistentClass = persistentClass;
    }

    /**
     * This method returns the <code>Class</code> that is the Data Access
     * Object (DAO) that the bean will be using for all it transactions
     * 
     * @return the <code>Class</code> that is the DAO class that will be used
     *         for all transactions
     */
    protected Class getDaoClass() {
        return daoClass;
    }

    /**
     * This method sets the Data Access Object (DAO) <code>Class</code> that
     * will be used in all the transactions of this bean
     * 
     * @param daoClass
     *            is the <code>Class</code> that will be used to instantiate
     *            new DAO's for all transactions from this bean
     */
    protected void setDaoClass(Class daoClass) {
        this.daoClass = daoClass;
    }

    /**
     * This method will look up a <code>IMetadataObject</code> using its
     * persistence identifier.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @soap.method
     * @axis.method
     * @see moos.ssds.services.metadata.IMetadataAccess#findById(Long)
     */
    public IMetadataObject findById(Long id, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        MetadataDAO mdao = this.getMetadataDAO();

        // Now look up the object by its ID
        return mdao.findById(id, returnFullObjectGraph);
    }

    /**
     * This method will look up a <code>IMetadataObject</code> using its
     * persistence identifier.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @soap.method
     * @axis.method
     * @see moos.ssds.services.metadata.IMetadataAccess#findById(long)
     */
    public IMetadataObject findById(long id, boolean returnFullObjectGraph)
        throws MetadataAccessException {

        // Try to create a Long object from the incoming long
        Long idLong = null;
        try {
            idLong = new Long(id);
        } catch (Throwable th) {
            String newMessage = new String("Could not convert the long " + id
                + " into a Long object: " + th.getMessage());
            throw new MetadataAccessException(newMessage);
        }

        // Now look up by ID
        return this.findById(idLong, returnFullObjectGraph);
    }

    /**
     * This method will look up a <code>IMetadataObject</code> using its
     * persistence identifier that is passed in as a
     * <code>java.lang.String</code>.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @soap.method
     * @axis.method
     * @see moos.ssds.services.metadata.IMetadataAccess#findById(String)
     */
    public IMetadataObject findById(String id, boolean returnFullObjectGraph)
        throws MetadataAccessException {

        // Check for null argument
        superLogger.debug("findById(String) called with id = " + id);
        if (id == null) {
            throw new MetadataAccessException("Failed: incoming id was null");
        }

        // Try to create a Long object from the incoming string
        Long idLong = null;
        try {
            idLong = new Long(id);
        } catch (Throwable th) {
            String newMessage = new String("Could not convert the string " + id
                + " into a Long object: " + th.getMessage());
            throw new MetadataAccessException(newMessage);
        }

        // Now look up by ID
        return this.findById(idLong, returnFullObjectGraph);
    }

    /**
     * This method returns a <code>Collection</code> of <code>Long</code>s
     * that are the IDs of all the dataContainers that are in SSDS.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @soap.method
     * @axis.method
     * @return a <code>Collection</code> of <code>Long</code>s that are the
     *         IDs of all dataContainers in SSDS.
     * @throws MetadataAccessException
     *             if something goes wrong in the method call.
     */
    public Collection findAllIDs() throws MetadataAccessException {
        // Grab the DAO
        MetadataDAO mdao = this.getMetadataDAO();

        // Now call the method
        return mdao.findAllIDs();
    }

    /**
     * This method returns a count of all the IDs of the
     * <code>MetadataObject</code> associated with the DAO
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @soap.method
     * @axis.method
     * @return a <code>Collection</code> of <code>Long</code>s that are the
     *         IDs of all dataContainers in SSDS.
     * @throws MetadataAccessException
     *             if something goes wrong in the method call.
     */
    public int countFindAllIDs() throws MetadataAccessException {
        // Grab the DAO
        MetadataDAO mdao = this.getMetadataDAO();

        // Now call the method
        return mdao.countFindAllIDs();
    }

    /**
     * This method will look up all <code>MetadataObject</code>s.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @soap.method
     * @axis.method
     * @see moos.ssds.services.metadata.IMetadataAccess#findAll()
     */
    public Collection findAll(String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {

        // Grab the DAO
        MetadataDAO mdao = this.getMetadataDAO();

        // Now find all the objects
        return mdao.findAll(orderByPropertyName, ascendingOrDescending,
            returnFullObjectGraph);

    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @soap.method
     * @axis.method
     * @see IMetadataAccess#findBySQL(String)
     */
    public Collection findBySQL(String sqlString, String aliasName,
        Class classOfReturn, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        MetadataDAO metadataDAO = this.getMetadataDAO();

        // Now find the ID
        return metadataDAO.findBySQL(sqlString, aliasName, classOfReturn,
            returnFullObjectGraph);
    }

    /**
     * This method will return the persistence ID of the given
     * <code>IMetadataObject</code> if an equivalent is found in the store.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @soap.method
     * @axis.method
     * @see moos.ssds.services.metadata.IMetadataAccess#findId(IMetadataObject)
     */
    public Long findId(IMetadataObject metadataObject)
        throws MetadataAccessException {

        // Grab the DAO
        MetadataDAO metadataDAO = this.getMetadataDAO();

        // Now find the ID
        return metadataDAO.findId(metadataObject);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @soap.method
     * @axis.method
     * @see IMetadataAccess#findEquivalentPersistentObject(IMetadataObject)
     */
    public IMetadataObject findEquivalentPersistentObject(
        IMetadataObject metadataObject, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        MetadataDAO metadataDAO = this.getMetadataDAO();

        // Now find the object
        return metadataDAO.findEquivalentPersistentObject(metadataObject,
            returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @soap.method
     * @axis.method
     * @see IMetadataAccess#getMetadataObjectGraph(IMetadataObject)
     */
    public IMetadataObject getMetadataObjectGraph(IMetadataObject metadataObject)
        throws MetadataAccessException {
        // Grab the DAO
        MetadataDAO mdao = this.getMetadataDAO();

        // Now call the method
        return mdao.getMetadataObjectGraph(metadataObject);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @soap.method
     * @axis.method
     * @see IMetadataAccess#getMetadataObjectGraph(IMetadataObject)
     */
    public IMetadataObject getDeepCopy(IMetadataObject metadataObject)
        throws MetadataAccessException {
        // Grab the DAO
        MetadataDAO mdao = this.getMetadataDAO();

        // Now call the method
        return mdao.getDeepCopy(metadataObject);
    }

    /**
     * This method inserts a <code>IMetadataObject</code> into the persistent
     * store.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @soap.method
     * @axis.method
     * @see moos.ssds.services.metadata.IMetadataAccess#insert(IMetadataObject)
     */
    public Long insert(IMetadataObject insertRecord)
        throws MetadataAccessException {

        // Grab the DAO
        MetadataDAO metadataDAO = this.getMetadataDAO();

        // First look for the metadata
        Long idOfMetadata = metadataDAO.findId(insertRecord);

        // If the ID was not found, throw exception
        if (idOfMetadata != null) {
            throw new MetadataAccessException("Metadata "
                + insertRecord.getClass() + " with id = "
                + insertRecord.getId()
                + " already exists in storage, no insert performed");
        }
        // Now perform makePersistent
        Long persistentID = metadataDAO.makePersistent(insertRecord);

        // Send any notifications
        this.sendOutNotificationMessage();

        // Return the ID
        return persistentID;
    }

    /**
     * This method updates a <code>IMetadataObject</code> in the persistent
     * store
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @soap.method
     * @axis.method
     * @see moos.ssds.services.metadata.IMetadataAccess#update(IMetadataObject)
     */
    public Long update(IMetadataObject updateRecord)
        throws MetadataAccessException {

        // Grab the DAO
        MetadataDAO metadataDAO = this.getMetadataDAO();

        // First look for the metadata
        Long idOfMetadata = metadataDAO.findId(updateRecord);

        // If the ID was not found, throw exception
        if (idOfMetadata == null) {
            throw new MetadataAccessException(
                "No matching metadata was found to update");
        }

        // Now perform makePersistent
        Long persistentID = metadataDAO.makePersistent(updateRecord);

        // Send any notifications
        this.sendOutNotificationMessage();

        // Return the ID
        return persistentID;
    }

    /**
     * This method deletes a <code>IMetadataObject</code> from the persistent
     * store
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @see moos.ssds.services.metadata.IMetadataAccess#delete(IMetadataObject)
     */
    public void delete(IMetadataObject deleteRecord)
        throws MetadataAccessException {

        // Grab the DAO
        MetadataDAO metadataDAO = this.getMetadataDAO();

        // Now perform makePersistent
        metadataDAO.makeTransient(deleteRecord);

        // Send any notifications
        this.sendOutNotificationMessage();
    }

    /**
     * This method takes a <code>IMetadataObject</code> object and makes it
     * persistent (either by inserting new record or updating existing)
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @soap.method
     * @axis.method
     */
    public Long makePersistent(IMetadataObject metadataObject)
        throws MetadataAccessException {
        // Grab the DAO
        MetadataDAO metadataDAO = this.getMetadataDAO();

        // Now perform makePersistent
        Long persistentID = metadataDAO.makePersistent(metadataObject);

        // Send any notifications
        this.sendOutNotificationMessage();

        // Return the ID
        return persistentID;

    }

    /**
     * This method takes a <code>IMetadataObject</code> and removes it from
     * the persistent store, thus making it transient
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     */
    public void makeTransient(IMetadataObject deleteRecord)
        throws MetadataAccessException {

        // Grab the DAO
        MetadataDAO metadataDAO = this.getMetadataDAO();

        // Now perform makePersistent
        metadataDAO.makeTransient(deleteRecord);

        // Send any notifications
        this.sendOutNotificationMessage();
    }

    /**
     * @see javax.ejb.SessionBean#ejbActivate()
     */
    public void ejbActivate() throws EJBException, RemoteException {}

    /**
     * @see javax.ejb.SessionBean#ejbPassivate()
     */
    public void ejbPassivate() throws EJBException, RemoteException {}

    /**
     * @see javax.ejb.SessionBean#ejbRemove()
     */
    public void ejbRemove() throws EJBException, RemoteException {}

    /**
     * The callback that the container can call after creating the sesssion bean
     */
    public void ejbPostCreate() throws CreateException {}

    /**
     * This method constructs the correct DAO based on the persistent class the
     * bean is associated with and grabs the current session.
     * 
     * @return the proper <code>MetadataDAO</code> for the bean
     */
    protected MetadataDAO getMetadataDAO() throws MetadataAccessException {
        // Construct an instance of the class
        MetadataDAO metadataDAO = null;
        // Find the correct constructor by creating an array of Classes that are
        // of the constructor parameter signature
        Constructor constructor = null;
        // The DAO constructors only have one parameter, which is the Hibernate
        // Session
        Class[] parameters = new Class[1];
        parameters[0] = Session.class;
        // Now find the constructor
        try {
            constructor = daoClass.getConstructor(parameters);
        } catch (SecurityException e1) {
            superLogger.error("SecurityException caught trying to "
                + "find the constructo for the DAO instance: "
                + e1.getMessage());
        } catch (NoSuchMethodException e1) {
            superLogger.error("NoSuchMethodException caught trying to "
                + "find the constructo for the DAO instance: "
                + e1.getMessage());
        }

        // Now create the objects which will be passed
        if (constructor != null) {
            Object[] arguments = new Object[1];
            arguments[0] = this.sessionFactory.getCurrentSession();
            try {
                metadataDAO = (MetadataDAO) constructor.newInstance(arguments);
            } catch (IllegalArgumentException e) {
                superLogger.error("IllegalArgumentException caught trying to "
                    + "instantiate the DAO instance: " + e.getMessage());
                throw new MetadataAccessException(
                    "IllegalArgumentException caught trying to "
                        + "instantiate the DAO instance: " + e.getMessage());
            } catch (InstantiationException e) {
                superLogger.error("InstantiationException caught trying to "
                    + "instantiate the DAO instance: " + e.getMessage());
                throw new MetadataAccessException(
                    "InstantiationException caught trying to "
                        + "instantiate the DAO instance: " + e.getMessage());
            } catch (IllegalAccessException e) {
                superLogger.error("IllegalAccessException caught trying to "
                    + "instantiate the DAO instance: " + e.getMessage());
                throw new MetadataAccessException(
                    "IllegalAccessException caught trying to "
                        + "instantiate the DAO instance: " + e.getMessage());
            } catch (InvocationTargetException e) {
                superLogger.error("InvocationTargetException caught trying to "
                    + "instantiate the DAO instance: " + e.getMessage());
                throw new MetadataAccessException(
                    "InvocationTargetException caught trying to "
                        + "instantiate the DAO instance: " + e.getMessage());
            }
        } else {
            throw new MetadataAccessException(
                "Could not instantiate the MetadataDAO");
        }

        // Now return the DAO
        return metadataDAO;
    }

    /**
     * This method checks the given DAO's messageMap and send out any messages
     * if it needs to
     */
    private void sendOutNotificationMessage() {
        superLogger.debug("sendNotificationMessage called");

        // First check to see if the sendProperties have been set
        if (ssdsAdminEmailAddress == null) {
            superLogger.debug("Looks like the ssdsAdminEmail was not set, "
                + "will read from properties if I can");
            if (servicesMetadataProperties != null) {
                String sendEmailMessagesProperty = servicesMetadataProperties
                    .getProperty("metadata.services.send.email.messages");
                if ((sendEmailMessagesProperty != null)
                    && (sendEmailMessagesProperty.equalsIgnoreCase("true"))) {
                    sendEmailMessages = true;
                } else {
                    sendEmailMessages = false;
                }
                ssdsAdminEmailAddress = servicesMetadataProperties
                    .getProperty("metadata.services.admin.from.email.address");
                if (sendEmailMessages) {
                    superLogger.debug("Email will be sent from "
                        + ssdsAdminEmailAddress);
                } else {
                    superLogger.debug("No email messages will be sent");
                }
                this.javaMailSessionJNDI = servicesMetadataProperties
                    .getProperty("metadata.services.mail.session.jndi.name");
            }
        }

        // Grab the message TreeMap
        TreeMap messageTreeMap = (TreeMap) MetadataDAO.getMessageTreeMap()
            .get();

        // Now see if there are any messages
        if ((sendEmailMessages) && (messageTreeMap != null)
            && (messageTreeMap.size() > 0)) {
            superLogger.debug("Some messages need sending!");
            // Setup the mail session
            javax.mail.Session mailSession = null;
            try {
                Context context = new InitialContext();
                mailSession = (javax.mail.Session) context
                    .lookup(this.javaMailSessionJNDI);
            } catch (NamingException e) {
                superLogger
                    .error("NamingException caught trying to get mail session: "
                        + e.getMessage());
            } catch (Throwable t) {
                superLogger
                    .error("Throwable caught trying to get mail session: "
                        + t.getMessage());
            }
            if (mailSession != null) {
                superLogger.debug("Got mail session from container!");
                Set keySet = messageTreeMap.keySet();
                Iterator keyIter = keySet.iterator();
                while (keyIter.hasNext()) {
                    String toEmailAddress = (String) keyIter.next();
                    superLogger.debug("Going to create a message for "
                        + toEmailAddress);
                    String emailMessage = (String) messageTreeMap
                        .get(toEmailAddress);
                    superLogger.debug("Message will be:\n" + emailMessage);
                    try {
                        MimeMessage msg = new MimeMessage(mailSession);
                        String fromEmail = ssdsAdminEmailAddress;
                        if (fromEmail != null) {
                            InternetAddress fromAddress = null;
                            try {
                                fromAddress = new InternetAddress(fromEmail,
                                    "SSDS Administrator");
                            } catch (UnsupportedEncodingException e) {
                                superLogger
                                    .error("UnsupportedEncodingException caught trying "
                                        + "to construct the from email address: "
                                        + e.getMessage());
                            }
                            if (fromAddress != null) {
                                msg.setSender(fromAddress);
                                Address[] replyToAddress = new Address[1];
                                replyToAddress[0] = fromAddress;
                                msg.setReplyTo(replyToAddress);
                            }
                        }
                        msg.setRecipient(Message.RecipientType.TO,
                            new InternetAddress(toEmailAddress));
                        msg
                            .setSubject("A message from the Shore-Side Data System");
                        msg.setSentDate(new Date());
                        msg.setContent(emailMessage, "text/html");
                        superLogger.debug("To: " + toEmailAddress);
                        superLogger.debug("Subject: " + msg.getSubject());
                        superLogger.debug("Message: ");
                        Transport.send(msg);
                    } catch (AddressException e) {
                        superLogger
                            .error("Could not send a message (AddressException): "
                                + e.getMessage());
                    } catch (MessagingException e) {
                        superLogger
                            .error("Could not send a message (MessagingException): "
                                + e.getMessage());
                    }
                }
            } else {
                superLogger.error("Mail session was not attained");
            }

            // Now I should null out the ThreadLocal for messages to ensure a
            // new one is created
            superLogger
                .debug("Going to check for the thread local to clear any messages");
            ThreadLocal threadLocal = MetadataDAO.getMessageTreeMap();
            // Clear out the message tree
            if (threadLocal != null)
                threadLocal.set(null);
            superLogger.debug("OK, messages should be cleared");
        } else {
            superLogger.debug("No messages need to be sent");
        }
    }

    /**
     * This is the SessionContext of the container
     */
    private SessionContext ctx;

    /**
     * The mail session name for use in emailing notifications
     */
    private String javaMailSessionJNDI = null;

    /**
     * A properties object that contains the properties for the metadata
     * services classes
     */
    protected Properties servicesMetadataProperties;

    /**
     * This is the Hibernate session factory from the container
     */
    protected SessionFactory sessionFactory = null;

    /**
     * This is the <code>Class</code> that the bean service is linked to. This
     * helps the bean figure out which Data Access Object to use
     */
    protected Class persistentClass = null;

    /**
     * This is the <code>Class</code> that will be used to instantiate DAO's
     * for this bean to utilize
     */
    protected Class daoClass = null;

    /**
     * This is a boolean to indicate if the DAO's should send out system
     * messages or not
     */
    static boolean sendEmailMessages = false;

    /**
     * The email address that messages from the DAOs will be sent from
     */
    static String ssdsAdminEmailAddress = null;

    /**
     * A log4j logger
     */
    static Logger superLogger = Logger.getLogger(AccessBean.class);

}