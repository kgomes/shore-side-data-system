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
import java.util.Date;
import java.util.Properties;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import moos.ssds.dao.EventDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.Event;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

/**
 * Provides a facade that provides client services for Event objects.
 * 
 * @ejb.bean name="EventAccess" type="Stateless"
 *           jndi-name="moos/ssds/services/metadata/EventAccess"
 *           local-jndi-name="moos/ssds/services/metadata/EventAccessLocal"
 *           view-type="both" transaction-type="Container"
 * @ejb.home create="true"
 *           local-class="moos.ssds.services.metadata.EventAccessLocalHome"
 *           remote-class="moos.ssds.services.metadata.EventAccessHome"
 *           extends="javax.ejb.EJBHome"
 * @ejb.interface create="true"
 *                local-class="moos.ssds.services.metadata.EventAccessLocal"
 *                local-extends="javax.ejb.EJBLocalObject,moos.ssds.services.metadata.IMetadataAccess"
 *                remote-class="moos.ssds.services.metadata.EventAccess"
 *                extends="javax.ejb.EJBObject,moos.ssds.services.metadata.IMetadataAccessRemote"
 * @ejb.util generate="physical"
 * @soap.service urn="EventAccess" scope="Request"
 * @axis.service urn="EventAccess" scope="Request"
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.5 $
 */
public class EventAccessEJB extends AccessBean implements IMetadataAccess {

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

        // Now set the super persistent class to DataContainer
        super.setPersistentClass(Event.class);
        // And the DAO
        super.setDaoClass(EventDAO.class);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param name
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByName(String name) throws MetadataAccessException {
        // Grab the DAO
        EventDAO eventDAO = (EventDAO) this.getMetadataDAO();

        // Now call the method
        return eventDAO.findByName(name);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param likeName
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByLikeName(String likeName)
        throws MetadataAccessException {
        // Grab the DAO
        EventDAO eventDAO = (EventDAO) this.getMetadataDAO();

        // Now call the method
        return eventDAO.findByLikeName(likeName);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @return
     * @throws MetadataAccessException
     */
    public Event findByNameAndDates(String name, Date startDate, Date endDate)
        throws MetadataAccessException {
        // Grab the DAO
        EventDAO eventDAO = (EventDAO) this.getMetadataDAO();

        // Now call the method
        return eventDAO.findByNameAndDates(name, startDate, endDate);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @return
     * @throws MetadataAccessException
     */
    public Collection findAllNames() throws MetadataAccessException {
        // Grab the DAO
        EventDAO eventDAO = (EventDAO) this.getMetadataDAO();

        // Now call the method
        return eventDAO.findAllNames();
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param startDate
     * @param endDate
     * @return
     * @throws MetadataAccessException
     */
    public Collection findWithinDateRange(Date startDate, Date endDate)
        throws MetadataAccessException {
        // Grab the DAO
        EventDAO eventDAO = (EventDAO) this.getMetadataDAO();

        // Now call the method
        return eventDAO.findWithinDateRange(startDate, endDate);
    }

    /**
     * This is the version that we can control for serialization purposes
     */
    private static final long serialVersionUID = 1L;

    /**
     * A log4j logger
     */
    static Logger logger = Logger.getLogger(EventAccessEJB.class);
}
