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

import moos.ssds.dao.StandardUnitDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.StandardUnit;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

/**
 * Provides a facade that provides client services for StandardUnit objects.
 * 
 * @ejb.bean name="StandardUnitAccess" type="Stateless"
 *           jndi-name="moos/ssds/services/metadata/StandardUnitAccess"
 *           local-jndi-name="moos/ssds/services/metadata/StandardUnitAccessLocal"
 *           view-type="both" transaction-type="Container"
 * @ejb.home create="true"
 *           local-class="moos.ssds.services.metadata.StandardUnitAccessLocalHome"
 *           remote-class="moos.ssds.services.metadata.StandardUnitAccessHome"
 *           extends="javax.ejb.EJBHome"
 * @ejb.interface create="true"
 *                local-class="moos.ssds.services.metadata.StandardUnitAccessLocal"
 *                local-extends="javax.ejb.EJBLocalObject,moos.ssds.services.metadata.IMetadataAccess"
 *                remote-class="moos.ssds.services.metadata.StandardUnitAccess"
 *                extends="javax.ejb.EJBObject,moos.ssds.services.metadata.IMetadataAccessRemote"
 * @ejb.util generate="physical"
 * @soap.service urn="StandardUnitAccess" scope="Request"
 * @axis.service urn="StandardUnitAccess" scope="Request"
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.10 $
 */
public class StandardUnitAccessEJB extends AccessBean
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

        // Now set the super persistent class to StandardUnit
        super.setPersistentClass(StandardUnit.class);
        // And the DAO
        super.setDaoClass(StandardUnitDAO.class);
    }

    /**
     * This method tries to look up and instantiate a <code>StandardUnit</code>
     * by its name
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param name
     *            is a <code>java.lang.String</code> that will be used to
     *            search for matches of a <code>StandardUnit</code>'s name
     * @return a <code>MetadataObject</code> of class
     *         <code>StandardUnit</code> that has a name that matches the one
     *         specified. If no matches were found, and empty collection is
     *         returned
     * @throws MetadataAccessException
     *             if something goes wrong with the search
     */
    public IMetadataObject findByName(String name)
        throws MetadataAccessException {

        // Grab the DAO
        StandardUnitDAO standardUnitDAO = (StandardUnitDAO) this
            .getMetadataDAO();

        // Now make the correct call and return the result
        return standardUnitDAO.findByName(name);
    }

    /**
     * This method looks for all <code>StandardUnit</code>s whose name
     * contain the name supplied. It could be an exact match of just contain the
     * name. For you wildcard folks, it is basically looking for all
     * <code>StandardUnit</code>s whose names match *likeName*.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param likeName
     *            is the name that will be used to search for. In SQL terms, it
     *            will do a LIKE '%likeName%'
     * @return a <code>Collection</code> of <code>StandardUnit</code>s that
     *         have names like the one specified as the parameter.
     */
    public Collection findByLikeName(String likeName)
        throws MetadataAccessException {

        // Grab the DAO
        StandardUnitDAO standardUnitDAO = (StandardUnitDAO) this
            .getMetadataDAO();

        // Now make the correct call and return the result
        return standardUnitDAO.findByLikeName(likeName);
    }

    /**
     * This method tries to look up all <code>StandardUnit</code>s by their
     * symbol
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param name
     *            is a <code>java.lang.String</code> that will be used to
     *            search for exact matches of a <code>StandardUnit</code>'s
     *            symbol (this is case in-sensitive)
     * @return a <code>Collection</code> of <code>StandardUnit</code>s that
     *         have a symbol that exactly matches (case-insensitive) the one
     *         specified. If no matches were found, an empty collection is
     *         returned.
     * @throws MetadataAccessException
     *             if something goes wrong with the search
     */
    public Collection findBySymbol(String symbol)
        throws MetadataAccessException {

        // Grab the DAO
        StandardUnitDAO standardUnitDAO = (StandardUnitDAO) this
            .getMetadataDAO();

        // Now make the correct call and return the result
        return standardUnitDAO.findBySymbol(symbol);
    }

    /**
     * This method looks for all <code>StandardUnit</code>s whose symbol
     * contain the symbol supplied. It could be an exact match of just contain
     * the symbol. For you wildcard folks, it is basically looking for all
     * <code>StandardUnit</code>s whose symbols match *likeSymbol*.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param likeSymbol
     *            is the symbol that will be used to search for. In SQL terms,
     *            it will do a LIKE '%likeSymbol%'
     * @return a <code>Collection</code> of <code>StandardUnit</code>s that
     *         have symbols like the one specified as the parameter.
     */
    public Collection findByLikeSymbol(String likeSymbol)
        throws MetadataAccessException {

        // Grab the DAO
        StandardUnitDAO standardUnitDAO = (StandardUnitDAO) this
            .getMetadataDAO();

        // Now make the correct call and return the result
        return standardUnitDAO.findByLikeSymbol(likeSymbol);
    }

    /**
     * This method returns a collection of <code>java.lang.String</code>s
     * that are all the names of the <code>StandardUnit</code>s in the
     * database
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @return a <code>Collection</code> of <code>java.lang.String</code>s
     *         that are all the <code>StandardUnit</code> names that are
     *         currently in the system. If there are no names, an empty
     *         collection is returned
     */
    public Collection findAllNames() throws MetadataAccessException {

        // Grab the DAO
        StandardUnitDAO standardUnitDAO = (StandardUnitDAO) this
            .getMetadataDAO();

        // Now make the correct call and return the result
        return standardUnitDAO.findAllNames();
    }

    /**
     * This is the version that we can control for serialization purposes
     */
    private static final long serialVersionUID = 1L;

    /**
     * A log4j logger
     */
    static Logger logger = Logger.getLogger(StandardUnitAccessEJB.class);

}