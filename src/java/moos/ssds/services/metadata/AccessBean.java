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
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import moos.ssds.dao.MetadataDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.IMetadataObject;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * This class is a super class for all the service session beans. It provides
 * common functionality
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.20 $
 */
public abstract class AccessBean implements IMetadataAccess {

	/**
	 * A log4j logger
	 */
	static Logger superLogger = Logger.getLogger(AccessBean.class);

	/**
	 * A properties object that contains the properties for the metadata
	 * services classes
	 */
	protected Properties servicesMetadataProperties;

	/**
	 * This is the Hibernate session factory from the container
	 */
	@Resource(mappedName = "java:/hibernate/SessionFactory")
	protected SessionFactory sessionFactory = null;

	/**
	 * This is the <code>Class</code> that the bean service is linked to. This
	 * helps the bean figure out which Data Access Object to use
	 */
	@SuppressWarnings("rawtypes")
	protected Class persistentClass = null;

	/**
	 * This is the <code>Class</code> that will be used to instantiate DAO's for
	 * this bean to utilize
	 */
	@SuppressWarnings("rawtypes")
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
	 * The mail session to use to send mail
	 */
	@Resource(mappedName = "java:/Mail")
	private static javax.mail.Session mailSession;

	@PostConstruct
	protected void setUpBean() {
		superLogger.debug("setupBean called");
		superLogger.debug("Going to read in the properties");
		servicesMetadataProperties = new Properties();
		try {
			servicesMetadataProperties
					.load(this
							.getClass()
							.getResourceAsStream(
									"/moos/ssds/services/metadata/servicesMetadata.properties"));
		} catch (Exception e) {
			superLogger.error("Exception trying to read in properties file: "
					+ e.getMessage());
		}

		// Make sure the properties were read from the JAR OK
		if (servicesMetadataProperties != null) {
			superLogger.debug("Loaded props OK");
		} else {
			superLogger.warn("Could not load the servicesMetadata.properties.");
		}
	}

	/**
	 * Returns the the persistent class this session bean is associated with
	 * 
	 * @return the <code>Class</code> that the bean is associated with
	 */
	@SuppressWarnings("rawtypes")
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
	@SuppressWarnings("rawtypes")
	protected void setPersistentClass(Class persistentClass) {
		this.persistentClass = persistentClass;
	}

	/**
	 * This method returns the <code>Class</code> that is the Data Access Object
	 * (DAO) that the bean will be using for all it transactions
	 * 
	 * @return the <code>Class</code> that is the DAO class that will be used
	 *         for all transactions
	 */
	@SuppressWarnings("rawtypes")
	protected Class getDaoClass() {
		return daoClass;
	}

	/**
	 * This method sets the Data Access Object (DAO) <code>Class</code> that
	 * will be used in all the transactions of this bean
	 * 
	 * @param daoClass
	 *            is the <code>Class</code> that will be used to instantiate new
	 *            DAO's for all transactions from this bean
	 */
	@SuppressWarnings("rawtypes")
	protected void setDaoClass(Class daoClass) {
		this.daoClass = daoClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.Access#findById(java.lang.Long, boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public IMetadataObject findById(Long id, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		MetadataDAO mdao = this.getMetadataDAO();

		// Now look up the object by its ID
		return mdao.findById(id, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.Access#findById(long, boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.Access#findById(java.lang.String,
	 * boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.Access#findAllIDs()
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public Collection<Long> findAllIDs() throws MetadataAccessException {
		// Grab the DAO
		MetadataDAO mdao = this.getMetadataDAO();

		// Now call the method
		return mdao.findAllIDs();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.Access#countFindAllIDs()
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public int countFindAllIDs() throws MetadataAccessException {
		// Grab the DAO
		MetadataDAO mdao = this.getMetadataDAO();

		// Now call the method
		return mdao.countFindAllIDs();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.Access#findAll(java.lang.String,
	 * java.lang.String, boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public Collection<? extends IMetadataObject> findAll(
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// Grab the DAO
		MetadataDAO mdao = this.getMetadataDAO();

		// Now find all the objects
		return mdao.findAll(orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.Access#findBySQL(java.lang.String,
	 * java.lang.String, java.lang.Class, boolean)
	 */
	@SuppressWarnings("rawtypes")
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public Collection<? extends IMetadataObject> findBySQL(String sqlString,
			String aliasName, Class classOfReturn, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		MetadataDAO metadataDAO = this.getMetadataDAO();

		// Now find the ID
		return metadataDAO.findBySQL(sqlString, aliasName, classOfReturn,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.Access#findId(moos.ssds.metadata.IMetadataObject
	 * )
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public Long findId(IMetadataObject metadataObject)
			throws MetadataAccessException {

		// Grab the DAO
		MetadataDAO metadataDAO = this.getMetadataDAO();

		// Now find the ID
		return metadataDAO.findId(metadataObject);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.Access#findEquivalentPersistentObject(moos
	 * .ssds.metadata.IMetadataObject, boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public IMetadataObject findEquivalentPersistentObject(
			IMetadataObject metadataObject, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		MetadataDAO metadataDAO = this.getMetadataDAO();

		// Now find the object
		return metadataDAO.findEquivalentPersistentObject(metadataObject,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.Access#getMetadataObjectGraph(moos.ssds.metadata
	 * .IMetadataObject)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public IMetadataObject getMetadataObjectGraph(IMetadataObject metadataObject)
			throws MetadataAccessException {
		// Grab the DAO
		MetadataDAO mdao = this.getMetadataDAO();

		// Now call the method
		return mdao.getMetadataObjectGraph(metadataObject);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.Access#getDeepCopy(moos.ssds.metadata.
	 * IMetadataObject)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public IMetadataObject getDeepCopy(IMetadataObject metadataObject)
			throws MetadataAccessException {
		// Grab the DAO
		MetadataDAO mdao = this.getMetadataDAO();

		// Now call the method
		return mdao.getDeepCopy(metadataObject);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.Access#insert(moos.ssds.metadata.IMetadataObject
	 * )
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.Access#update(moos.ssds.metadata.IMetadataObject
	 * )
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.Access#delete(moos.ssds.metadata.IMetadataObject
	 * )
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void delete(IMetadataObject deleteRecord)
			throws MetadataAccessException {

		// Grab the DAO
		MetadataDAO metadataDAO = this.getMetadataDAO();

		// Now perform makePersistent
		metadataDAO.makeTransient(deleteRecord);

		// Send any notifications
		this.sendOutNotificationMessage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.Access#makePersistent(moos.ssds.metadata.
	 * IMetadataObject)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Long makePersistent(IMetadataObject metadataObject)
			throws MetadataAccessException {
		if (metadataObject != null)
			superLogger.debug("makePersistent called with object "
					+ metadataObject.toStringRepresentation("|"));

		// Grab the DAO
		MetadataDAO metadataDAO = this.getMetadataDAO();

		// Now perform makePersistent
		Long persistentID = metadataDAO.makePersistent(metadataObject);

		// Send any notifications
		this.sendOutNotificationMessage();

		// Return the ID
		return persistentID;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.Access#makeTransient(moos.ssds.metadata.
	 * IMetadataObject)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
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
	 * This method constructs the correct DAO based on the persistent class the
	 * bean is associated with and grabs the current session.
	 * 
	 * @return the proper <code>MetadataDAO</code> for the bean
	 */
	@SuppressWarnings("unchecked")
	protected MetadataDAO getMetadataDAO() throws MetadataAccessException {
		// Construct an instance of the class
		MetadataDAO metadataDAO = null;
		// Find the correct constructor by creating an array of Classes that are
		// of the constructor parameter signature
		@SuppressWarnings("rawtypes")
		Constructor constructor = null;
		// The DAO constructors only have one parameter, which is the Hibernate
		// Session
		@SuppressWarnings("rawtypes")
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
								+ "instantiate the DAO instance: "
								+ e.getMessage());
			} catch (InstantiationException e) {
				superLogger.error("InstantiationException caught trying to "
						+ "instantiate the DAO instance: " + e.getMessage());
				throw new MetadataAccessException(
						"InstantiationException caught trying to "
								+ "instantiate the DAO instance: "
								+ e.getMessage());
			} catch (IllegalAccessException e) {
				superLogger.error("IllegalAccessException caught trying to "
						+ "instantiate the DAO instance: " + e.getMessage());
				throw new MetadataAccessException(
						"IllegalAccessException caught trying to "
								+ "instantiate the DAO instance: "
								+ e.getMessage());
			} catch (InvocationTargetException e) {
				superLogger.error("InvocationTargetException caught trying to "
						+ "instantiate the DAO instance: " + e.getMessage());
				throw new MetadataAccessException(
						"InvocationTargetException caught trying to "
								+ "instantiate the DAO instance: "
								+ e.getMessage());
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
			}
		}

		// Grab the message TreeMap
		TreeMap<String, String> messageTreeMap = (TreeMap<String, String>) MetadataDAO
				.getMessageTreeMap().get();

		// Now see if there are any messages
		if ((sendEmailMessages) && (messageTreeMap != null)
				&& (messageTreeMap.size() > 0)) {
			superLogger.debug("Some messages need sending!");
			if (mailSession != null) {
				superLogger.debug("Got mail session from container!");
				Set<String> keySet = messageTreeMap.keySet();
				Iterator<String> keyIter = keySet.iterator();
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
						msg.setSubject("A message from the Shore-Side Data System");
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
			ThreadLocal<TreeMap<String, String>> threadLocal = MetadataDAO
					.getMessageTreeMap();
			// Clear out the message tree
			if (threadLocal != null)
				threadLocal.set(null);
			superLogger.debug("OK, messages should be cleared");
		} else {
			superLogger.debug("No messages need to be sent");
		}
	}

}