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

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import moos.ssds.dao.SoftwareDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.Software;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

/**
 * Provides a facade that provides client services for Software objects.
 * 
 * @ejb.bean name="SoftwareAccess" type="Stateless"
 *           jndi-name="moos/ssds/services/metadata/SoftwareAccess"
 *           local-jndi-name="moos/ssds/services/metadata/SoftwareAccessLocal"
 *           view-type="both" transaction-type="Container"
 * @ejb.home create="true"
 *           local-class="moos.ssds.services.metadata.SoftwareAccessLocalHome"
 *           remote-class="moos.ssds.services.metadata.SoftwareAccessHome"
 *           extends="javax.ejb.EJBHome"
 * @ejb.interface create="true"
 *                local-class="moos.ssds.services.metadata.SoftwareAccessLocal"
 *                local-extends="javax.ejb.EJBLocalObject,moos.ssds.services.metadata.IMetadataAccess"
 *                remote-class="moos.ssds.services.metadata.SoftwareAccess"
 *                extends="javax.ejb.EJBObject,moos.ssds.services.metadata.IMetadataAccessRemote"
 * @ejb.util generate="physical"
 * @soap.service urn="SoftwareAccess" scope="Request"
 * @axis.service urn="SoftwareAccess" scope="Request"
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.6 $
 */
public class SoftwareAccessEJB extends AccessBean implements IMetadataAccess {

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
        super.setPersistentClass(Software.class);
        // And the DAO
        super.setDaoClass(SoftwareDAO.class);
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
        SoftwareDAO softwareDAO = (SoftwareDAO) this.getMetadataDAO();

        // Now call the method
        return softwareDAO.findByName(name);
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
        SoftwareDAO softwareDAO = (SoftwareDAO) this.getMetadataDAO();

        // Now call the method
        return softwareDAO.findByLikeName(likeName);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @return
     * @throws MetadataAccessException
     */
    public Collection findAllNames() throws MetadataAccessException {
        // Grab the DAO
        SoftwareDAO softwareDAO = (SoftwareDAO) this.getMetadataDAO();

        // Now call the method
        return softwareDAO.findAllNames();
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param name
     * @param version
     * @return
     * @throws MetadataAccessException
     */
    public Software findByNameAndSoftwareVersion(String name, String version,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        SoftwareDAO softwareDAO = (SoftwareDAO) this.getMetadataDAO();

        // Now call the method
        return softwareDAO.findByNameAndSoftwareVersion(name, version,
            returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param uriString
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByURIString(String uriString)
        throws MetadataAccessException {
        // Grab the DAO
        SoftwareDAO softwareDAO = (SoftwareDAO) this.getMetadataDAO();

        // Now call the method
        return softwareDAO.findByURIString(uriString);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param uri
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByURI(URI uri) throws MetadataAccessException {
        // Grab the DAO
        SoftwareDAO softwareDAO = (SoftwareDAO) this.getMetadataDAO();

        // Now call the method
        return softwareDAO.findByURI(uri);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param url
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByURL(URL url) throws MetadataAccessException {
        // Grab the DAO
        SoftwareDAO softwareDAO = (SoftwareDAO) this.getMetadataDAO();

        // Now call the method
        return softwareDAO.findByURL(url);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param person
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByPerson(Person person)
        throws MetadataAccessException {
        // Grab the DAO
        SoftwareDAO softwareDAO = (SoftwareDAO) this.getMetadataDAO();

        // Now call the method
        return softwareDAO.findByPerson(person);
    }

    /**
     * This is the version that we can control for serialization purposes
     */
    private static final long serialVersionUID = 1L;

    /**
     * A log4j logger
     */
    static Logger logger = Logger.getLogger(SoftwareAccessEJB.class);
}
