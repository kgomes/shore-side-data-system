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

import moos.ssds.dao.DeviceTypeDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DeviceType;
import moos.ssds.metadata.IMetadataObject;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

/**
 * Provides a facade that provides client services for DeviceType objects.
 * 
 * @ejb.bean name="DeviceTypeAccess" type="Stateless"
 *           jndi-name="moos/ssds/services/metadata/DeviceTypeAccess"
 *           local-jndi-name="moos/ssds/services/metadata/DeviceTypeAccessLocal"
 *           view-type="both" transaction-type="Container"
 * @ejb.home create="true"
 *           local-class="moos.ssds.services.metadata.DeviceTypeAccessLocalHome"
 *           remote-class="moos.ssds.services.metadata.DeviceTypeAccessHome"
 *           extends="javax.ejb.EJBHome"
 * @ejb.interface create="true"
 *                local-class="moos.ssds.services.metadata.DeviceTypeAccessLocal"
 *                local-extends="javax.ejb.EJBLocalObject,moos.ssds.services.metadata.IMetadataAccess"
 *                remote-class="moos.ssds.services.metadata.DeviceTypeAccess"
 *                extends="javax.ejb.EJBObject,moos.ssds.services.metadata.IMetadataAccessRemote"
 * @ejb.util generate="physical"
 * @soap.service urn="DeviceTypeAccess" scope="Request"
 * @axis.service urn="DeviceTypeAccess" scope="Request"
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.13 $
 */
public class DeviceTypeAccessEJB extends AccessBean implements IMetadataAccess {

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

        // Now set the super persistent class to DeviceType
        super.setPersistentClass(DeviceType.class);
        // And the DAO
        super.setDaoClass(DeviceTypeDAO.class);
    }

    /**
     * This method tries to look up and instantiate a <code>DeviceType</code>
     * by its name. This is looking for an exact match.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param username
     *            is a <code>java.lang.String</code> that will be used to
     *            search for matches of a DeviceType's name
     * @return a <code>MetadataObject</code> of class <code>DeviceType</code>
     *         that has a name that matches the one specified. If no matches
     *         were found, null will be returned
     * @throws MetadataAccessException
     *             if something goes wrong with the search
     */
    public IMetadataObject findByName(String name, boolean returnFullObjectGraph)
        throws MetadataAccessException {

        // Grab the DAO
        DeviceTypeDAO deviceTypeDAO = (DeviceTypeDAO) this.getMetadataDAO();

        // Now call the method and return the results
        return deviceTypeDAO.findByName(name, returnFullObjectGraph);
    }

    /**
     * This method looks for all deviceTypes whose name contain the name
     * supplied. It could be an exact match of just contain the name. For you
     * wildcard folks, it is basically looking for all devices whose names match
     * *likeName*.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param likeName
     *            is the name that will be used to search for. In SQL terms, it
     *            will do a LIKE '%likeName%'
     * @return a <code>Collection</code> of <code>DeviceType</code>s that
     *         have names like the one specified as the parameter.
     */
    public Collection findByLikeName(String likeName,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DeviceTypeDAO deviceTypeDAO = (DeviceTypeDAO) this.getMetadataDAO();

        // Now call the method and return the results
        return deviceTypeDAO.findByLikeName(likeName, orderByPropertyName,
            ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * This method returns a <code>Collection</code> of <code>String</code>s
     * that are the names of all the deviceTypes that are in SSDS.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @return a <code>Collection</code> of <code>String</code>s that are
     *         the names of all deviceTypes in SSDS.
     * @throws MetadataAccessException
     *             if something goes wrong in the method call.
     */
    public Collection findAllNames() throws MetadataAccessException {
        // Grab the DAO
        DeviceTypeDAO deviceTypeDAO = (DeviceTypeDAO) this.getMetadataDAO();

        // Now call the method and return the results
        return deviceTypeDAO.findAllNames();
    }

    /**
     * This is the version that we can control for serialization purposes
     */
    private static final long serialVersionUID = 1L;

    /**
     * A log4j logger
     */
    static Logger logger = Logger.getLogger(DeviceTypeAccessEJB.class);

}