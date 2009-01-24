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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import moos.ssds.dao.DataContainerDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataContainerGroup;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.Resource;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

/**
 * Provides a facade that provides client services for DataContainer objects.
 * 
 * @ejb.bean name="DataContainerAccess" type="Stateless"
 *           jndi-name="moos/ssds/services/metadata/DataContainerAccess"
 *           local-jndi-name="moos/ssds/services/metadata/DataContainerAccessLocal"
 *           view-type="both" transaction-type="Container"
 * @ejb.home create="true"
 *           local-class="moos.ssds.services.metadata.DataContainerAccessLocalHome"
 *           remote-class="moos.ssds.services.metadata.DataContainerAccessHome"
 *           extends="javax.ejb.EJBHome"
 * @ejb.interface create="true"
 *                local-class="moos.ssds.services.metadata.DataContainerAccessLocal"
 *                local-extends="javax.ejb.EJBLocalObject,moos.ssds.services.metadata.IMetadataAccess"
 *                remote-class="moos.ssds.services.metadata.DataContainerAccess"
 *                extends="javax.ejb.EJBObject,moos.ssds.services.metadata.IMetadataAccessRemote"
 * @ejb.util generate="physical"
 * @soap.service urn="DataContainerAccess" scope="Request"
 * @axis.service urn="DataContainerAccess" scope="Request"
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.19 $
 */
public class DataContainerAccessEJB extends AccessBean
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
        super.setPersistentClass(DataContainer.class);
        // And the DAO
        super.setDaoClass(DataContainerDAO.class);
    }

    /**
     * @see DataContainerDAO#findByName(String, boolean)
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     */
    public Collection findByName(String name, boolean exactMatch,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findByName(name, exactMatch,
            orderByPropertyName, ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @see DataContainerDAO#countFindByName(String, boolean)
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     */
    public int countFindByName(String name, boolean exactMatch)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.countFindByName(name, exactMatch);
    }

    /**
     * This method returns a <code>Collection</code> of <code>String</code>s
     * that are the names of all the dataContainers that are registered in SSDS.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @return a <code>Collection</code> of <code>String</code>s that are
     *         the names of all dataContainers in SSDS.
     * @throws MetadataAccessException
     *             if something goes wrong in the method call.
     */
    public Collection findAllNames() throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findAllNames();

    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param dataContainerType
     * @param name
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByDataContainerTypeAndName(String dataContainerType,
        String name, boolean exactMatch, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findByDataContainerTypeAndName(
            dataContainerType, name, exactMatch, orderByPropertyName,
            ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param dataContainerType
     * @param name
     * @param exactMatch
     * @return
     * @throws MetadataAccessException
     */
    public int countFindByDataContainerTypeAndName(String dataContainerType,
        String name, boolean exactMatch) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.countFindByDataContainerTypeAndName(
            dataContainerType, name, exactMatch);
    }

    /**
     * @see DataContainerDAO#findWithDataWithinTimeWindow(Date, boolean, Date,
     *      boolean)
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     */
    public Collection findWithDataWithinTimeWindow(Date startDate,
        boolean allDataAfterStartDate, Date endDate,
        boolean allDataBeforeEndDate, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findWithDataWithinTimeWindow(startDate,
            allDataAfterStartDate, endDate, allDataBeforeEndDate,
            orderByPropertyName, ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * This method tries to look up and instantiate a DataContainer by its URI
     * string
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param uriString
     *            is a <code>java.lang.String</code> that will be used to
     *            search for matches of a dataContainer's URI string
     * @return a <code>DataContainer</code> object that has that URI string.
     *         If no matches were found, an empty collection is returned
     * @throws MetadataAccessException
     *             if something goes wrong with the search
     */
    public DataContainer findByURIString(String uriString,
        boolean returnFullObjectGraph) throws MetadataAccessException {

        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now find the dataContainers ID
        return dataContainerDAO.findByURIString(uriString,
            returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param uri
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByURI(URI uri, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findByURI(uri, orderByPropertyName,
            ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param url
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByURL(URL url, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findByURL(url, orderByPropertyName,
            ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * This method returns a <code>Collection</code> of <code>String</code>s
     * that are the URI strings of all the dataContainers that are registered in
     * SSDS.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @return a <code>Collection</code> of <code>String</code>s that are
     *         the URI strings of all dataContainers in SSDS.
     * @throws MetadataAccessException
     *             if something goes wrong in the method call.
     */
    public Collection findAllURIStrings() throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findAllURIStrings();

    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @return a <code>DataContainer</code> that has exactly the same DODS
     *         URL.
     * @throws MetadataAccessException
     *             if something goes wrong in the method call.
     */
    public DataContainer findByDODSURLString(String dodsUrlString,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findByDODSURLString(dodsUrlString,
            returnFullObjectGraph);
    }

    /**
     * This method returns a <code>Collection</code> of
     * <code>DataContainer</code>s that have a matching DODS URL String. If
     * <code>exactMatch</code> is true, only those the have exactly the same
     * DODS URLs will be returned, otherwise all <code>DataContainer</code>s
     * that have part of their DODS URLs that match will be returned
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @return a <code>Collection</code> of <code>DataContainer</code>s
     *         that have matching DODS URLs
     * @throws MetadataAccessException
     *             if something goes wrong in the method call.
     */
    public Collection findByDODSURLString(String dodsUrlString,
        boolean exactMatch, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        return dataContainerDAO.findByDODSURLString(dodsUrlString, exactMatch,
            orderByPropertyName, ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * This method returns the number of DataContainers that match the
     * DODSUrlString provided. If <code>exactMatch</code> is true then only
     * the <code>DataContainer</code>s that match that DODS URL exactly will
     * be returned. Otherwise, a LIKE search will be done for
     * <code>DataContainer</code>s that have part of their DODS URLs that
     * match
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @return an int that indicates how many DataContainers match that
     *         DODSUrlString
     * @throws MetadataAccessException
     *             if something goes wrong in the method call.
     */
    public int countFindByDODSURLString(String dodsUrlString, boolean exactMatch)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call and return result
        return dataContainerDAO.countFindByDODSURLString(dodsUrlString,
            exactMatch);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param mimeType
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByMimeType(String mimeType,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findByMimeType(mimeType, orderByPropertyName,
            ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * This method will return all <code>DataContainer</code>s that have data
     * that is inside the specified geospatial box. In general if any sides of
     * the box are not specified, the search dimension will be considered
     * infinite in that direction.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param geospatialLatMin
     *            This is the minimum lattitude of the search cube.
     * @param geospatialLatMax
     *            This is the maximum lattitude of the search cube.
     * @param geospatialLonMin
     *            This is the minimum longitude of the search cube.
     * @param geospatialLonMax
     *            This is the maximum longitude of the search cube.
     * @param geospatialVerticalMin
     *            This is the minimum vertical value of the cube (the bottom of
     *            the cube).
     * @param geospatialVerticalMax
     *            This is the maximum vertical value of the cube (the top of the
     *            cube).
     * @return A <code>Collection</code> of <code>DataContainer</code>s
     *         that have data within that cube.
     */
    public Collection findWithinGeospatialCube(Double geospatialLatMin,
        Double geospatialLatMax, Double geospatialLonMin,
        Double geospatialLonMax, Float geospatialVerticalMin,
        Float geospatialVerticalMax, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findWithinGeospatialCube(geospatialLatMin,
            geospatialLatMax, geospatialLonMin, geospatialLonMax,
            geospatialVerticalMin, geospatialVerticalMax, orderByPropertyName,
            ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * This method will return all <code>DataContainer</code>s that have data
     * that is inside the specified geospatial box and within the time
     * specified. In general if any sides of the box are not specified, the
     * search dimension will be considered infinite in that direction.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param startDate
     *            specifies the time after which the <code>DataContainer</code>
     *            must have data to be found in this query.
     * @param endDate
     *            specifies the time before which the <code>DataContainer</code>
     *            must have data to be found in this query.
     * @param geospatialLatMin
     *            This is the minimum lattitude of the search cube.
     * @param geospatialLatMax
     *            This is the maximum lattitude of the search cube.
     * @param geospatialLonMin
     *            This is the minimum longitude of the search cube.
     * @param geospatialLonMax
     *            This is the maximum longitude of the search cube.
     * @param geospatialVerticalMin
     *            This is the minimum vertical value of the cube (the bottom of
     *            the cube).
     * @param geospatialVerticalMax
     *            This is the maximum vertical value of the cube (the top of the
     *            cube).
     * @return A <code>Collection</code> of <code>DataContainer</code>s
     *         that have data within that cube.
     */
    public Collection findWithinTimeAndGeospatialCube(Date startDate,
        Date endDate, Double geospatialLatMin, Double geospatialLatMax,
        Double geospatialLonMin, Double geospatialLonMax,
        Float geospatialVerticalMin, Float geospatialVerticalMax,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findWithinTimeAndGeospatialCube(startDate,
            endDate, geospatialLatMin, geospatialLatMax, geospatialLonMin,
            geospatialLonMax, geospatialVerticalMin, geospatialVerticalMax,
            orderByPropertyName, ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * This method returns all <code>DataContainer</code>s that are linked
     * (normally means owned) by a <code>Person</code>.
     * 
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param person
     *            is the <code>Person</code> that will be used to search for
     *            devices.
     * @return a <code>Collection</code> of devices that are linked to that
     *         person.
     */
    public Collection findByPerson(Person person, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findByPerson(person, orderByPropertyName,
            ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @see DataContainerDAO#findByRecordVariable(RecordVariable, boolean)
     */
    public DataContainer findByRecordVariable(RecordVariable recordVariable,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findByRecordVariable(recordVariable,
            returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param recordVariableName
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByRecordVariableName(String recordVariableName,
        boolean exactMatch, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findByRecordVariableName(recordVariableName,
            exactMatch, orderByPropertyName, ascendingOrDescending,
            returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param likeRecordVariableName
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByLikeRecordVariableName(
        String likeRecordVariableName, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findByLikeRecordVariableName(
            likeRecordVariableName, orderByPropertyName, ascendingOrDescending,
            returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param standardVariableName
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByStandardVariableName(String standardVariableName,
        boolean exactMatch, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findByStandardVariableName(
            standardVariableName, exactMatch, orderByPropertyName,
            ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param likeStandardVariableName
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByLikeStandardVariableName(
        String likeStandardVariableName, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findByLikeStandardVariableName(
            likeStandardVariableName, orderByPropertyName,
            ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param dataContainerGroup
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByDataContainerGroup(
        DataContainerGroup dataContainerGroup, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findByDataContainerGroup(dataContainerGroup,
            orderByPropertyName, ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param dataContainerGroupName
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByDataContainerGroupName(
        String dataContainerGroupName, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findByDataContainerGroupName(
            dataContainerGroupName, orderByPropertyName, ascendingOrDescending,
            returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param likeDataContainerGroupName
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByLikeDataContainerGroupName(
        String likeDataContainerGroupName, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findByLikeDataContainerGroupName(
            likeDataContainerGroupName, orderByPropertyName,
            ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @see DataContainerDAO#findByKeywordName(String, boolean, boolean)
     */
    public Collection findByKeywordName(String keywordName, boolean exactMatch,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findByKeywordName(keywordName, exactMatch,
            orderByPropertyName, ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @see DataContainerDAO#countFindByKeywordName(String, boolean)
     */
    public int countFindByKeywordName(String keywordName, boolean exactMatch,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.countFindByKeywordName(keywordName, exactMatch);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param resource
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByResource(Resource resource,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findByResource(resource, orderByPropertyName,
            ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param dataContainer
     * @param fetchDepth
     * @return
     * @throws MetadataAccessException
     */
    public Collection findAllIndirectCreators(DataContainer dataContainer,
        int fetchDepth, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findAllIndirectCreators(dataContainer,
            fetchDepth, orderByPropertyName, ascendingOrDescending,
            returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param dataContainer
     * @param fetchDepth
     * @return
     * @throws MetadataAccessException
     */
    public Collection findCreatorChain(DataContainer dataContainer,
        int fetchDepth, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findCreatorChain(dataContainer, fetchDepth,
            orderByPropertyName, ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param dataContainer
     * @param fetchDepth
     * @return
     * @throws MetadataAccessException
     */
    public Collection findAllIndirectConsumers(DataContainer dataContainer,
        int fetchDepth, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findAllIndirectConsumers(dataContainer,
            fetchDepth, orderByPropertyName, ascendingOrDescending,
            returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param dataContainer
     * @param fetchDepth
     * @return
     * @throws MetadataAccessException
     */
    public Collection findAllConsumers(DataContainer dataContainer,
        int fetchDepth, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findAllConsumers(dataContainer, fetchDepth,
            orderByPropertyName, ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param dataContainer
     * @return
     * @throws MetadataAccessException
     */
    public Collection findDirectInputs(DataContainer dataContainer,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findDirectInputs(dataContainer,
            orderByPropertyName, ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param dataContainer
     * @param fetchDepth
     * @return
     * @throws MetadataAccessException
     */
    public Collection findAllInputs(DataContainer dataContainer,
        int fetchDepth, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findAllInputs(dataContainer, fetchDepth,
            orderByPropertyName, ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param dataContainer
     * @param fetchDepth
     * @return
     * @throws MetadataAccessException
     */
    public Collection findAllDerivedOutputs(DataContainer dataContainer,
        int fetchDepth, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findAllDerivedOutputs(dataContainer,
            fetchDepth, orderByPropertyName, ascendingOrDescending,
            returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param dataProducer
     * @param fetchDepth
     * @return
     * @throws MetadataAccessException
     */
    public Collection findAllDerivedOutputs(DataProducer dataProducer,
        int fetchDepth, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findAllDerivedOutputs(dataProducer, fetchDepth,
            orderByPropertyName, ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @see DataContainerDAO#findInputsByDataProducer(DataProducer dataProducer,
     *      String orderByPropertyName, boolean returnFullObjectGraphs)
     */
    public Collection findInputsByDataProducer(DataProducer dataProducer,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findInputsByDataProducer(dataProducer,
            orderByPropertyName, ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @see DataContainerDAO#findOutputsByDataProducer(DataProducer
     *      dataProducer, String orderByPropertyName, boolean
     *      returnFullObjectGraphs)
     */
    public Collection findOutputsByDataProducer(DataProducer dataProducer,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findOutputsByDataProducer(dataProducer,
            orderByPropertyName, ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param dataProducer
     * @return
     * @throws MetadataAccessException
     */
    public DataContainer findBestDirectOutput(DataProducer dataProducer,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO.findBestDirectOutput(dataProducer,
            orderByPropertyName, ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param recordVariableName
     * @param startDate
     * @param endDate
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByRecordVariableNameAndDataWithinTimeWindow(
        String recordVariableName, Date startDate, Date endDate,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO
            .findByRecordVariableNameAndDataWithinTimeWindow(
                recordVariableName, startDate, endDate, orderByPropertyName,
                ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param likeRecordVariableName
     * @param startDate
     * @param endDate
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByLikeRecordVariableNameAndDataWithinTimeWindow(
        String likeRecordVariableName, Date startDate, Date endDate,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO
            .findByLikeRecordVariableNameAndDataWithinTimeWindow(
                likeRecordVariableName, startDate, endDate,
                orderByPropertyName, ascendingOrDescending,
                returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param standardVariableName
     * @param startDate
     * @param endDate
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByStandardVariableNameAndDataWithinTimeWindow(
        String standardVariableName, Date startDate, Date endDate,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO
            .findByStandardVariableNameAndDataWithinTimeWindow(
                standardVariableName, startDate, endDate, orderByPropertyName,
                ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param likeStandardVariableName
     * @param startDate
     * @param endDate
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByLikeStandardVariableNameAndDataWithinTimeWindow(
        String likeStandardVariableName, Date startDate, Date endDate,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO
            .findByLikeStandardVariableNameAndDataWithinTimeWindow(
                likeStandardVariableName, startDate, endDate,
                orderByPropertyName, ascendingOrDescending,
                returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param recordVariableName
     * @param geospatialLatMin
     * @param geospatialLatMax
     * @param geospatialLonMin
     * @param geospatialLonMax
     * @param geospatialVerticalMin
     * @param geospatialVerticalMax
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByRecordVariableNameAndWithinGeospatialCube(
        String recordVariableName, Double geospatialLatMin,
        Double geospatialLatMax, Double geospatialLonMin,
        Double geospatialLonMax, Float geospatialVerticalMin,
        Float geospatialVerticalMax, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO
            .findByRecordVariableNameAndWithinGeospatialCube(
                recordVariableName, geospatialLatMin, geospatialLatMax,
                geospatialLonMin, geospatialLonMax, geospatialVerticalMin,
                geospatialVerticalMax, orderByPropertyName,
                ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param likeRecordVariableName
     * @param geospatialLatMin
     * @param geospatialLatMax
     * @param geospatialLonMin
     * @param geospatialLonMax
     * @param geospatialVerticalMin
     * @param geospatialVerticalMax
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByLikeRecordVariableNameAndWithinGeospatialCube(
        String likeRecordVariableName, Double geospatialLatMin,
        Double geospatialLatMax, Double geospatialLonMin,
        Double geospatialLonMax, Float geospatialVerticalMin,
        Float geospatialVerticalMax, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO
            .findByLikeRecordVariableNameAndWithinGeospatialCube(
                likeRecordVariableName, geospatialLatMin, geospatialLatMax,
                geospatialLonMin, geospatialLonMax, geospatialVerticalMin,
                geospatialVerticalMax, orderByPropertyName,
                ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param standardVariableName
     * @param geospatialLatMin
     * @param geospatialLatMax
     * @param geospatialLonMin
     * @param geospatialLonMax
     * @param geospatialVerticalMin
     * @param geospatialVerticalMax
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByStandardVariableNameAndWithinGeospatialCube(
        String standardVariableName, Double geospatialLatMin,
        Double geospatialLatMax, Double geospatialLonMin,
        Double geospatialLonMax, Float geospatialVerticalMin,
        Float geospatialVerticalMax, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO
            .findByStandardVariableNameAndWithinGeospatialCube(
                standardVariableName, geospatialLatMin, geospatialLatMax,
                geospatialLonMin, geospatialLonMax, geospatialVerticalMin,
                geospatialVerticalMax, orderByPropertyName,
                ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param likeStandardVariableName
     * @param geospatialLatMin
     * @param geospatialLatMax
     * @param geospatialLonMin
     * @param geospatialLonMax
     * @param geospatialVerticalMin
     * @param geospatialVerticalMax
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByLikeStandardVariableNameAndWithinGeospatialCube(
        String likeStandardVariableName, Double geospatialLatMin,
        Double geospatialLatMax, Double geospatialLonMin,
        Double geospatialLonMax, Float geospatialVerticalMin,
        Float geospatialVerticalMax, String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO
            .findByLikeStandardVariableNameAndWithinGeospatialCube(
                likeStandardVariableName, geospatialLatMin, geospatialLatMax,
                geospatialLonMin, geospatialLonMax, geospatialVerticalMin,
                geospatialVerticalMax, orderByPropertyName,
                ascendingOrDescending, returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param recordVariableName
     * @param startDate
     * @param endDate
     * @param geospatialLatMin
     * @param geospatialLatMax
     * @param geospatialLonMin
     * @param geospatialLonMax
     * @param geospatialVerticalMin
     * @param geospatialVerticalMax
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByRecordVariableNameWithinTimeAndWithinGeospatialCube(
        String recordVariableName, Date startDate, Date endDate,
        Double geospatialLatMin, Double geospatialLatMax,
        Double geospatialLonMin, Double geospatialLonMax,
        Float geospatialVerticalMin, Float geospatialVerticalMax,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO
            .findByRecordVariableNameWithinTimeAndWithinGeospatialCube(
                recordVariableName, startDate, endDate, geospatialLatMin,
                geospatialLatMax, geospatialLonMin, geospatialLonMax,
                geospatialVerticalMin, geospatialVerticalMax,
                orderByPropertyName, ascendingOrDescending,
                returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param likeRecordVariableName
     * @param startDate
     * @param endDate
     * @param geospatialLatMin
     * @param geospatialLatMax
     * @param geospatialLonMin
     * @param geospatialLonMax
     * @param geospatialVerticalMin
     * @param geospatialVerticalMax
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByLikeRecordVariableNameWithinTimeAndWithinGeospatialCube(
        String likeRecordVariableName, Date startDate, Date endDate,
        Double geospatialLatMin, Double geospatialLatMax,
        Double geospatialLonMin, Double geospatialLonMax,
        Float geospatialVerticalMin, Float geospatialVerticalMax,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO
            .findByLikeRecordVariableNameWithinTimeAndWithinGeospatialCube(
                likeRecordVariableName, startDate, endDate, geospatialLatMin,
                geospatialLatMax, geospatialLonMin, geospatialLonMax,
                geospatialVerticalMin, geospatialVerticalMax,
                orderByPropertyName, ascendingOrDescending,
                returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param standardVariableName
     * @param startDate
     * @param endDate
     * @param geospatialLatMin
     * @param geospatialLatMax
     * @param geospatialLonMin
     * @param geospatialLonMax
     * @param geospatialVerticalMin
     * @param geospatialVerticalMax
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByStandardVariableNameWithinTimeAndWithinGeospatialCube(
        String standardVariableName, Date startDate, Date endDate,
        Double geospatialLatMin, Double geospatialLatMax,
        Double geospatialLonMin, Double geospatialLonMax,
        Float geospatialVerticalMin, Float geospatialVerticalMax,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO
            .findByStandardVariableNameWithinTimeAndWithinGeospatialCube(
                standardVariableName, startDate, endDate, geospatialLatMin,
                geospatialLatMax, geospatialLonMin, geospatialLonMax,
                geospatialVerticalMin, geospatialVerticalMax,
                orderByPropertyName, ascendingOrDescending,
                returnFullObjectGraph);
    }

    /**
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     * @param likeStandardVariableName
     * @param startDate
     * @param endDate
     * @param geospatialLatMin
     * @param geospatialLatMax
     * @param geospatialLonMin
     * @param geospatialLonMax
     * @param geospatialVerticalMin
     * @param geospatialVerticalMax
     * @return
     * @throws MetadataAccessException
     */
    public Collection findByLikeStandardVariableNameWithinTimeAndWithinGeospatialCube(
        String likeStandardVariableName, Date startDate, Date endDate,
        Double geospatialLatMin, Double geospatialLatMax,
        Double geospatialLonMin, Double geospatialLonMax,
        Float geospatialVerticalMin, Float geospatialVerticalMax,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {
        // Grab the DAO
        DataContainerDAO dataContainerDAO = (DataContainerDAO) this
            .getMetadataDAO();

        // Now call the method
        return dataContainerDAO
            .findByLikeStandardVariableNameWithinTimeAndWithinGeospatialCube(
                likeStandardVariableName, startDate, endDate, geospatialLatMin,
                geospatialLatMax, geospatialLonMin, geospatialLonMax,
                geospatialVerticalMin, geospatialVerticalMax,
                orderByPropertyName, ascendingOrDescending,
                returnFullObjectGraph);
    }

    /**
     * This is the version that we can control for serialization purposes
     */
    private static final long serialVersionUID = 1L;

    /**
     * A log4j logger
     */
    static Logger logger = Logger.getLogger(DataContainerAccessEJB.class);

}