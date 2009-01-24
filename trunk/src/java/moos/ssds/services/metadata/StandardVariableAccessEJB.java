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

import moos.ssds.dao.StandardVariableDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.StandardVariable;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

/**
 * Provides a facade that provides client services for StandardVariable objects.
 * 
 * @ejb.bean name="StandardVariableAccess" type="Stateless"
 *           jndi-name="moos/ssds/services/metadata/StandardVariableAccess"
 *           local-jndi-name="moos/ssds/services/metadata/StandardVariableAccessLocal"
 *           view-type="both" transaction-type="Container"
 * @ejb.home create="true"
 *           local-class="moos.ssds.services.metadata.StandardVariableAccessLocalHome"
 *           remote-class="moos.ssds.services.metadata.StandardVariableAccessHome"
 *           extends="javax.ejb.EJBHome"
 * @ejb.interface create="true"
 *                local-class="moos.ssds.services.metadata.StandardVariableAccessLocal"
 *                local-extends="javax.ejb.EJBLocalObject,moos.ssds.services.metadata.IMetadataAccess"
 *                remote-class="moos.ssds.services.metadata.StandardVariableAccess"
 *                extends="javax.ejb.EJBObject,moos.ssds.services.metadata.IMetadataAccessRemote"
 * @ejb.util generate="physical"
 * @soap.service urn="StandardVariableAccess" scope="Request"
 * @axis.service urn="StandardVariableAccess" scope="Request"
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.10 $
 */
public class StandardVariableAccessEJB extends AccessBean
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

        // Now set the super persistent class to StandardVariable
        super.setPersistentClass(StandardVariable.class);
        // And the DAO
        super.setDaoClass(StandardVariableDAO.class);
    }

    /**
     * This method tries to look up and instantiate a
     * <code>StandardVariable</code> by its name and reference scale
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param name
     *            is a <code>java.lang.String</code> that will be used to
     *            search for matches of a <code>StandardVariable</code>'s
     *            name
     * @param referenceScale
     *            is a <code>java.lang.String</code> that will be used to
     *            search for matches of a <code>StandardVariable</code>'s
     *            reference scale
     * @return a <code>MetadataObject</code> of class
     *         <code>StandardVariable</code> that has a name and reference
     *         scale that matches the one specified. If no matches were found,
     *         and empty collection is returned
     * @throws MetadataAccessException
     *             if something goes wrong with the search
     */
    public IMetadataObject findByNameAndReferenceScale(String name,
        String referenceScale) throws MetadataAccessException {

        // Grab the DAO
        StandardVariableDAO standardVariableDAO = (StandardVariableDAO) this
            .getMetadataDAO();

        // Now call the associated method
        return standardVariableDAO.findByNameAndReferenceScale(name,
            referenceScale);
    }

    /**
     * This method looks for all <code>StandardVariable</code>s whose name is
     * an exact match of the name supplied.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param name
     *            is the name that will be used to search for
     * @return a <code>Collection</code> of <code>StandardVariable</code>s
     *         whose names exactly match the one specified as the parameter.
     */
    public Collection findByName(String name) throws MetadataAccessException {

        // Grab the DAO
        StandardVariableDAO standardVariableDAO = (StandardVariableDAO) this
            .getMetadataDAO();

        // Now call the associated method
        return standardVariableDAO.findByName(name);
    }

    /**
     * This method looks for all <code>StandardVariable</code>s whose name
     * contain the name supplied. It could be an exact match of just contain the
     * name. For you wildcard folks, it is basically looking for all
     * <code>StandardVariable</code>s whose names match *likeName*.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param likeName
     *            is the name that will be used to search for. In SQL terms, it
     *            will do a LIKE '%likeName%'
     * @return a <code>Collection</code> of <code>StandardVariable</code>s
     *         that have names like the one specified as the parameter.
     */
    public Collection findByLikeName(String likeName)
        throws MetadataAccessException {

        // Grab the DAO
        StandardVariableDAO standardVariableDAO = (StandardVariableDAO) this
            .getMetadataDAO();

        // Now call the associated method
        return standardVariableDAO.findByLikeName(likeName);
    }

    /**
     * This method tries to look up all <code>StandardVariable</code>s by
     * their reference scale
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param name
     *            is a <code>java.lang.String</code> that will be used to
     *            search for exact matches of a <code>StandardVariable</code>'s
     *            reference scale (this is case in-sensitive)
     * @return a <code>Collection</code> of <code>StandardVariable</code>s
     *         that have a reference scale that exactly matches
     *         (case-insensitive) the one specified. If no matches were found,
     *         an empty collection is returned.
     * @throws MetadataAccessException
     *             if something goes wrong with the search
     */
    public Collection findByReferenceScale(String referenceScale)
        throws MetadataAccessException {

        // Grab the DAO
        StandardVariableDAO standardVariableDAO = (StandardVariableDAO) this
            .getMetadataDAO();

        // Now call the associated method
        return standardVariableDAO.findByReferenceScale(referenceScale);
    }

    /**
     * This method looks for all <code>StandardVariable</code>s whose
     * referenceScale contain the referenceScale supplied. It could be an exact
     * match of just contain the referenceScale. For you wildcard folks, it is
     * basically looking for all <code>StandardVariable</code>s whose
     * referenceScales match *likeReferenceScale*.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param likeReferenceScale
     *            is the referenceScale that will be used to search for. In SQL
     *            terms, it will do a LIKE '%likeReferenceScale%'
     * @return a <code>Collection</code> of <code>StandardVariable</code>s
     *         that have referenceScales like the one specified as the
     *         parameter.
     */
    public Collection findByLikeReferenceScale(String likeReferenceScale)
        throws MetadataAccessException {

        // Grab the DAO
        StandardVariableDAO standardVariableDAO = (StandardVariableDAO) this
            .getMetadataDAO();

        // Now call the associated method
        return standardVariableDAO.findByLikeReferenceScale(likeReferenceScale);
    }

    /**
     * This method returns a collection of <code>java.lang.String</code>s
     * that are all the names of the <code>StandardVariable</code>s in the
     * database
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @return a <code>Collection</code> of <code>java.lang.String</code>s
     *         that are all the <code>StandardVariable</code> names that are
     *         currently in the system. If there are no names, an empty
     *         collection is returned
     */
    public Collection findAllNames() throws MetadataAccessException {

        // Grab the DAO
        StandardVariableDAO standardVariableDAO = (StandardVariableDAO) this
            .getMetadataDAO();

        // Now call the associated method
        return standardVariableDAO.findAllNames();
    }

    /**
     * This method returns a collection of <code>java.lang.String</code>s
     * that are all the referenceScales of the <code>StandardVariable</code>s
     * in the database
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @return a <code>Collection</code> of <code>java.lang.String</code>s
     *         that are all the <code>StandardVariable</code> referenceScales
     *         that are currently in the system. If there are no
     *         referenceScales, an empty collection is returned
     */
    public Collection findAllReferenceScales() throws MetadataAccessException {

        // Grab the DAO
        StandardVariableDAO standardVariableDAO = (StandardVariableDAO) this
            .getMetadataDAO();

        // Now call the associated method
        return standardVariableDAO.findAllReferenceScales();
    }

    /**
     * This method looks for the <code>StandardVariable</code> that is linked
     * to the given <code>RecordVariable</code>
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param recordVariable
     *            is the <code>RecordVariable</code> that will be used to look
     *            up the <code>StandardVariable</code>
     * @return a <code>StandardVariable</code> that is linked to the *
     *         <code>RecordVariable</code>. Null is returned if there is no
     *         relationship defined.
     */
    public StandardVariable findByRecordVariable(RecordVariable recordVariable)
        throws MetadataAccessException {

        // Grab the DAO
        StandardVariableDAO standardVariableDAO = (StandardVariableDAO) this
            .getMetadataDAO();

        // Now call the associated method
        return standardVariableDAO.findByRecordVariable(recordVariable);
    }

    /**
     * This is the version that we can control for serialization purposes
     */
    private static final long serialVersionUID = 1L;

    /**
     * A log4j logger
     */
    static Logger logger = Logger.getLogger(StandardVariableAccessEJB.class);
}