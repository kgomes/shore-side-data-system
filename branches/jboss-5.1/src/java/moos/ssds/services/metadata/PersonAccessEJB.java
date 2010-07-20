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

import java.util.Collection;
import java.util.Properties;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import moos.ssds.dao.PersonDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.Person;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

/**
 * Provides a facade that provides client services for Person objects.
 * 
 * @ejb.bean name="PersonAccess" type="Stateless"
 *           jndi-name="moos/ssds/services/metadata/PersonAccess"
 *           local-jndi-name="moos/ssds/services/metadata/PersonAccessLocal"
 *           view-type="both" transaction-type="Container"
 * @ejb.home create="true"
 *           local-class="moos.ssds.services.metadata.PersonAccessLocalHome"
 *           remote-class="moos.ssds.services.metadata.PersonAccessHome"
 *           extends="javax.ejb.EJBHome"
 * @ejb.interface create="true"
 *                local-class="moos.ssds.services.metadata.PersonAccessLocal"
 *                local-extends="javax.ejb.EJBLocalObject,moos.ssds.services.metadata.IMetadataAccess"
 *                remote-class="moos.ssds.services.metadata.PersonAccess"
 *                extends="javax.ejb.EJBObject,moos.ssds.services.metadata.IMetadataAccessRemote"
 * @ejb.util generate="physical"
 * @soap.service urn="PersonAccess" scope="Request"
 * @axis.service urn="PersonAccess" scope="Request"
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: mccann $
 * @version : $Revision: 1.1.2.12 $
 */
public class PersonAccessEJB extends AccessBean implements IMetadataAccess {

    /**
     * This is the ejb callback that the container calls when the EJB is first
     * created. In this case it sets up the Hibernate session factory and sets
     * the class that is associate with the bean
     * 
     * @throws CreateException
     */
    public void ejbCreate() throws CreateException {
        logger.debug("ejbCreate called");
        logger.debug("Going to read in the properties");
        servicesMetadataProperties = new Properties();
        try {
            servicesMetadataProperties
                .load(this.getClass().getResourceAsStream(
                    "/moos/ssds/services/metadata/servicesMetadata.properties"));
        } catch (Exception e) {
            logger.error("Exception trying to read in properties file: "
                + e.getMessage());
        }

        // Make sure the properties were read from the JAR OK
        if (servicesMetadataProperties != null) {
            logger.debug("Loaded props OK");
        } else {
            logger.warn("Could not load the servicesMetadata.properties.");
        }

        // Now create the intial context for looking up the hibernate session
        // factory and look up the session factory
        try {
            InitialContext initialContext = new InitialContext();
            sessionFactory = (SessionFactory) initialContext
                .lookup(servicesMetadataProperties
                    .getProperty("metadata.hibernate.jndi.name"));
        } catch (NamingException e) {
            logger
                .error("NamingException caught when trying to get hibernate's "
                    + "SessionFactory from JNDI: " + e.getMessage());
        }
        logger.debug("OK, grabbed the SessionFactory => " + sessionFactory);

        // Now set the super persistent class to Person
        super.setPersistentClass(Person.class);
        logger.debug("OK, set Persistent class to Person");

        // And the DAO
        super.setDaoClass(PersonDAO.class);
        logger.debug("OK, set DAO Class to PersonDAO");
    }

    /**
     * This method tries to look up and instantiate a user by their email
     * address.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @soap.method
     * @axis.method
     * @param email
     *            is a <code>java.lang.String</code> that will be used to
     *            search for matches of a person's email address
     * @return a <code>Collection</code> of person objects that have that
     *         email address. If no matches were found, an empty collection is
     *         returned
     * @throws MetadataAccessException
     *             if something goes wrong with the search
     */
    public Collection findByEmail(String email, boolean exactMatch,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {

    	logger.debug("findByEmail called: email=" + email);
        // Grab the DAO
        PersonDAO personDAO = (PersonDAO) this.getMetadataDAO();

        logger.debug("Grabbed PersonDAO => " + personDAO);
        // Now find the persons ID
        return personDAO.findByEmail(email, exactMatch,
            orderByPropertyName, ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * This method tries to look up and instantiate a user by their username
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param username
     *            is a <code>java.lang.String</code> that will be used to
     *            search for matches of a person's username
     * @return a <code>MetadataObject</code> of class <code>Person</code>
     *         that has a username that matches the one specified. If no matches
     *         were found, an empty collection is returned
     * @throws MetadataAccessException
     *             if something goes wrong with the search
     */
    public IMetadataObject findByUsername(String username,
        boolean returnFullObjectGraph) throws MetadataAccessException {

        logger.debug("findByUsername called with username " + username);

        // Grab the DAO
        PersonDAO personDAO = (PersonDAO) this.getMetadataDAO();

        // Now find the persons ID
        return personDAO.findByUsername(username, returnFullObjectGraph);
    }

    /**
     * This method returns a collection of <code>java.lang.String</code> that
     * are all the usernames of people in the database
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @return a <code>Collection</code> of <code>java.lang.String</code>s
     *         that are all the usernames that are currently in the system. If
     *         there are no usernames, null is returned.
     */
    public Collection findAllUsernames() throws MetadataAccessException {

        logger.debug("findAllUsernames called");

        // Grab the DAO
        PersonDAO personDAO = (PersonDAO) this.getMetadataDAO();

        // Now find the persons ID
        return personDAO.findAllUsernames();
    }

    /**
     * This is the version that we can control for serialization purposes
     */
    private static final long serialVersionUID = 1L;

    /**
     * A log4j logger
     */
    static Logger logger = Logger.getLogger(PersonAccessEJB.class);

}