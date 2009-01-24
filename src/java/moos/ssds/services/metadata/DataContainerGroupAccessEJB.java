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

import moos.ssds.dao.DataContainerGroupDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataContainerGroup;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

/**
 * Provides a facade that provides client services for DataContainerGroup
 * objects.
 * 
 * @ejb.bean name="DataContainerGroupAccess" type="Stateless"
 *           jndi-name="moos/ssds/services/metadata/DataContainerGroupAccess"
 *           local-jndi-name="moos/ssds/services/metadata/DataContainerGroupAccessLocal"
 *           view-type="both" transaction-type="Container"
 * @ejb.home create="true"
 *           local-class="moos.ssds.services.metadata.DataContainerGroupAccessLocalHome"
 *           remote-class="moos.ssds.services.metadata.DataContainerGroupAccessHome"
 *           extends="javax.ejb.EJBHome"
 * @ejb.interface create="true"
 *                local-class="moos.ssds.services.metadata.DataContainerGroupAccessLocal"
 *                local-extends="javax.ejb.EJBLocalObject,moos.ssds.services.metadata.IMetadataAccess"
 *                remote-class="moos.ssds.services.metadata.DataContainerGroupAccess"
 *                extends="javax.ejb.EJBObject,moos.ssds.services.metadata.IMetadataAccessRemote"
 * @ejb.util generate="physical"
 * @soap.service urn="DataContainerGroupAccess" scope="Request"
 * @axis.service urn="DataContainerGroupAccess" scope="Request"
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.5 $
 */
public class DataContainerGroupAccessEJB extends AccessBean
    implements
        IMetadataAccess {

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
        super.setPersistentClass(DataContainerGroup.class);
        // And the DAO
        super.setDaoClass(DataContainerGroupDAO.class);
    }

    /**
     * @see moos.ssds.dao.DataContainerGroupDAO#findByName(String, boolean)
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     */
    public Collection findByName(String name, boolean exactMatch,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerGroupDAO dataContainerGroupDAO = (DataContainerGroupDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerGroupDAO.findByName(name, exactMatch,
            orderByPropertyName, ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @see DataContainerGroupDAO#findAllNames()
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     */
    public Collection findAllNames() throws MetadataAccessException {
        // Grab the DAO
        DataContainerGroupDAO dataContainerGroupDAO = new DataContainerGroupDAO(
            sessionFactory.getCurrentSession());

        // Now call the method
        return dataContainerGroupDAO.findAllNames();
    }

    /**
     * @see DataContainerGroupDAO#findByDataContainer(DataContainer)
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     */
    public Collection findByDataContainer(DataContainer dataContainer,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerGroupDAO dataContainerGroupDAO = (DataContainerGroupDAO) this
        .getMetadataDAO();

        // Now call the method
        return dataContainerGroupDAO.findByDataContainer(dataContainer,
            returnFullObjectGraph);
    }

    /**
     * This is the version that we can control for serialization purposes
     */
    private static final long serialVersionUID = 1L;

    /**
     * A log4j logger
     */
    static Logger logger = Logger.getLogger(DataContainerGroupAccessEJB.class);
}
