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
package moos.ssds.dao;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.CommentTag;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataContainerGroup;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.HeaderDescription;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.Keyword;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.RecordDescription;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.StandardDomain;
import moos.ssds.metadata.StandardKeyword;
import moos.ssds.metadata.StandardReferenceScale;
import moos.ssds.metadata.StandardUnit;
import moos.ssds.metadata.StandardVariable;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * This Data Access Object (DAO) provides methods for interacting with the
 * persitence mechanism that handles the persistence of
 * <code>DataContainer</code> objects. It also provides query methods.
 * <hr>
 * 
 * @stereotype service
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.38 $
 */
public class DataContainerDAO extends MetadataDAO {

	/**
	 * This is the constructor that calls the super constructor and sets the
	 * proper class and Hibernate Session
	 * 
	 * @param session
	 *            is the <code>Session</code> that will be used in the
	 *            persistent operations
	 * @throws MetadataAccessException
	 *             if something goes awry
	 */
	public DataContainerDAO(Session session) throws MetadataAccessException {
		super(DataContainer.class, session);
	}

	/**
	 * @see IMetadataDAO#findEquivalentPersistentObject(IMetadataObject)
	 */
	public IMetadataObject findEquivalentPersistentObject(
			IMetadataObject metadataObject, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		logger.debug("findEquivalentPersistentObject called");
		// First try to cast to a DataContainer
		DataContainer dataContainer = this
				.checkIncomingMetadataObject(metadataObject);

		// The DataContainer to be returned
		DataContainer dataContainerToReturn = null;

		// Check if the incoming object has an ID. If so, query by the ID
		if (dataContainer.getId() != null) {
			Criteria criteria = this.formulatePropertyCriteria(false,
					dataContainer.getId(), null, false, null, null, false,
					null, false, null, false, null, false, null, false, null,
					false, null, false, null, false, null, false, null, false,
					null, false, null, null);
			dataContainerToReturn = (DataContainer) criteria.uniqueResult();
		}

		// If one was not found, look up by URI string
		if (dataContainerToReturn == null)
			dataContainerToReturn = this.findByURIString(dataContainer
					.getUriString(), false);

		// If not found and there is a DODS URL, try by that
		if ((dataContainerToReturn == null)
				&& (dataContainer.getDodsUrlString() != null)
				&& (!dataContainer.getDodsUrlString().equals(""))) {
			dataContainerToReturn = this.findByDODSURLString(dataContainer
					.getDodsUrlString(), false);
		}

		// Check for return full object graph
		if (returnFullObjectGraph)
			dataContainerToReturn = (DataContainer) getRealObjectAndRelationships(dataContainerToReturn);

		return dataContainerToReturn;
	}

	/**
	 * This method returns a <code>Collection</code> of <code>Long</code>s that
	 * are the IDs of all the dataContainers that are in SSDS.
	 * 
	 * @return a <code>Collection</code> of <code>Long</code>s that are the IDs
	 *         of all dataContainers in SSDS.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	@SuppressWarnings("unchecked")
	public Collection<Long> findAllIDs() throws MetadataAccessException {
		Collection<Long> dataContainerIDs = null;

		// Create the query and run it
		try {
			Query query = getSession()
					.createQuery(
							"select distinct dataContainer.id from "
									+ "DataContainer dataContainer order by dataContainer.id");
			dataContainerIDs = (Collection<Long>) query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return dataContainerIDs;
	}

	/**
	 * @see IMetadataDAO#countFindAllIDs()
	 */
	public int countFindAllIDs() throws MetadataAccessException {
		// The count
		int count = 0;
		try {
			Long longCount = (Long) getSession().createQuery(
					"select count(distinct dataContainer.id) from "
							+ "DataContainer dataContainer").uniqueResult();
			if (longCount != null)
				count = longCount.intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the result
		return count;
	}

	/**
	 * This method looks for all <code>DataContainer</code>s by their name. If
	 * the <code>exactMatch</code> boolean is true, it will look for the name
	 * exactly, otherwise it will perform a like search
	 * 
	 * @param name
	 *            is the name to search for
	 * @param exactMatch
	 *            indicates if the name has to match exactly (<code>true</code>)
	 *            or not (<code>false</code>). If not, it is like a wildcard
	 *            search on the <code>DataContainer</code>'s name (*name*).
	 * @param returnFullObjectGraph
	 *            is a boolean that specifies if the caller wants the fully
	 *            instantianted object graph (relationships) returned, or just
	 *            the query object itself. If you want the full graph returned
	 *            specify <code>true</code>, otherwise leave it false. <b>NOTE:
	 *            By specifying true, you could be requesting a large object
	 *            tree which will slow things down, so use sparingly</b>
	 * @return a <code>Collection</code> of <code>DataContainer</code>s that
	 *         have matching names
	 * @throws MetadataAccessException
	 *             if something goes wrong.
	 */
	public Collection findByName(String name, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Make sure argument is not null
		logger.debug("name = " + name);
		if ((name == null) && (name.equals(""))) {
			logger.debug("Name in query was null, I used to throw an "
					+ "exception, now I just return nulll");
			return null;
		}

		// The collection to be returned
		Collection results = new ArrayList();

		// Construct and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					name, exactMatch, null, null, false, null, false, null,
					false, null, false, null, false, null, false, null, false,
					null, false, null, false, null, false, null, false,
					orderByPropertyName, ascendingOrDescending);
			results = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Check to see if full object graphs were requested
		if (returnFullObjectGraph)
			results = getRealObjectsAndRelationships(results);

		// Return the results
		return results;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param name
	 * @param exactMatch
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindByName(String name, boolean exactMatch)
			throws MetadataAccessException {
		// The count to return
		int count = 0;

		// Make sure argument is not null
		logger.debug("name = " + name);
		if ((name == null) && (name.equals(""))) {
			logger.debug("Name in query was null, I used to throw an "
					+ "exception, now I just return nulll");
			return 0;
		}
		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					name, exactMatch, null, null, false, null, false, null,
					false, null, false, null, false, null, false, null, false,
					null, false, null, false, null, false, null, false, null,
					null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the count
		return count;
	}

	/**
	 * This method returns a <code>Collection</code> of <code>String</code>s
	 * that are the names of all the dataContainers that are registered in SSDS.
	 * 
	 * @return a <code>Collection</code> of <code>String</code>s that are the
	 *         names of all dataContainers in SSDS.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public Collection findAllNames() throws MetadataAccessException {
		Collection dataContainerNames = null;

		// Create the query and run it
		try {
			Query query = getSession()
					.createQuery(
							"select distinct dataContainer.name from "
									+ "DataContainer dataContainer order by dataContainer.name");
			dataContainerNames = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return dataContainerNames;
	}

	/**
	 * This method looks for all dataContainers of the given 'type' and name.
	 * 
	 * @param dataContainerType
	 *            is the type that will be used to search for.
	 * @param name
	 *            is the name of the <code>DataContainer</code> to search for.
	 *            If this parameter is <code>null</code> or empty the search
	 *            will only be done by the data container type.
	 * @param exactMatch
	 *            is a boolean to indicate if the name search is to be an exact
	 *            match of the name
	 * @param returnFullObjectGraph
	 *            is a boolean that specifies if the caller wants the fully
	 *            instantianted object graph (relationships) returned, or just
	 *            the query object itself. If you want the full graph returned
	 *            specify <code>true</code>, otherwise leave it false. <b>NOTE:
	 *            By specifying true, you could be requesting a large object
	 *            tree which will slow things down, so use sparingly</b>
	 * @return a <code>Collection</code> of <code>DataContainer</code>s that
	 *         match the type specified as the parameter.
	 */
	public Collection findByDataContainerTypeAndName(String dataContainerType,
			String name, boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// The collection to be returned
		Collection results = new ArrayList();

		// Make sure argument is not null
		logger.debug("dataContainerType = " + dataContainerType);
		if (!DataContainer.isValidDataContainerType(dataContainerType))
			return results;

		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					name, exactMatch, dataContainerType, null, false, null,
					false, null, false, null, false, null, false, null, false,
					null, false, null, false, null, false, null, false, null,
					false, orderByPropertyName, ascendingOrDescending);
			results = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Check if full object graphs were requested
		if (returnFullObjectGraph)
			results = getRealObjectsAndRelationships(results);

		// Return the results
		return results;
	}

	public int countFindByDataContainerTypeAndName(String dataContainerType,
			String name, boolean exactMatch) throws MetadataAccessException {
		// The count to return
		int count = 0;
		// Make sure argument is not null
		logger.debug("dataContainerType = " + dataContainerType);
		if (!DataContainer.isValidDataContainerType(dataContainerType))
			return 0;

		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					name, exactMatch, dataContainerType, null, false, null,
					false, null, false, null, false, null, false, null, false,
					null, false, null, false, null, false, null, false, null,
					false, null, null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the results
		return count;
	}

	/**
	 * This method will return all <code>DataContainer</code>s that have any
	 * data within the specified window of time. If either start or end dates
	 * are null, the query window will essentially be infinite in that
	 * direction. Also note that <code>DataContainer</code>s that do not have
	 * start and/or end times specified will be assumed to match all times for
	 * the missing times. Hmmm, that doesn't make much sense, basically if the
	 * <code>DataContainer</code> does not have an end date, for example, the
	 * end date will be assumed as infinite and so it will match all possible
	 * queiries that supply a start date. Better?. Also the two booleans
	 * indicate if the query is to only return <code>DataContainer</code>s that
	 * have data inside the edges of the time window specified
	 * 
	 * @param startDate
	 *            the start <code>Date</code> of the time window to search in
	 * @param allDataAfterStartDate
	 *            indicates if (<code>true</code>) the query is to only return
	 *            <code>DataContainer</code>s that have all their data after the
	 *            start date specified
	 * @param endDate
	 *            the end <code>Date</code> of the time window to search in
	 * @param allDataBeforeEndDate
	 *            indicates if (<code>true</code>) the query is to only return
	 *            <code>DataContainer</code>s that have all their data before
	 *            the end date specified.
	 * @return all <code>DataContainer</code>s that contains any data within
	 *         that time window
	 */
	public Collection findWithDataWithinTimeWindow(Date startDate,
			boolean allDataAfterStartDate, Date endDate,
			boolean allDataBeforeEndDate, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// The collection to be returned
		Collection results = new ArrayList();

		// Now build and perform the query
		if ((startDate == null) && (endDate == null)) {
			return results;
		}

		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					null, false, null, startDate, allDataAfterStartDate,
					endDate, allDataBeforeEndDate, null, false, null, false,
					null, false, null, false, null, false, null, false, null,
					false, null, false, null, false, orderByPropertyName,
					ascendingOrDescending);
			results = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Check if full object graphs were requested
		if (returnFullObjectGraph)
			results = getRealObjectsAndRelationships(results);

		// Return the results
		return results;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param startDate
	 * @param allDataAfterStartDate
	 * @param endDate
	 * @param allDataBeforeEndDate
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindWithDataWithinTimeWindow(Date startDate,
			boolean allDataAfterStartDate, Date endDate,
			boolean allDataBeforeEndDate) throws MetadataAccessException {
		// The count to be returned
		int count = 0;

		// Now build and perform the query
		if ((startDate == null) && (endDate == null)) {
			return 0;
		}

		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					null, false, null, startDate, allDataAfterStartDate,
					endDate, allDataBeforeEndDate, null, false, null, false,
					null, false, null, false, null, false, null, false, null,
					false, null, false, null, false, null, null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the results
		return count;
	}

	/**
	 * This method looks up and returns the persistent instance of a
	 * <code>DataContainer</code> with the specified URI string.
	 * 
	 * @param uriString
	 *            is the URI (string form) of the <code>DataContainer</code> to
	 *            search for
	 * @return is the <code>DataContainer</code> that was found in the
	 *         persistent store with the given URI string. If no
	 *         <code>DataContainer</code> was found, null is returned.
	 */
	public DataContainer findByURIString(String uriString,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// First make sure the incoming value is not null
		if ((uriString == null) || (uriString.equals(""))) {
			logger.debug("Failed: incoming uriString was null or empty");
			return null;
		}

		// Create the DataContainer to return
		DataContainer dataContainerToReturn = null;

		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					null, false, null, null, false, null, false, uriString,
					true, null, false, null, false, null, false, null, false,
					null, false, null, false, null, false, null, false, null,
					null);
			dataContainerToReturn = (DataContainer) criteria.uniqueResult();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Check if full object graphs were requested
		if (returnFullObjectGraph)
			dataContainerToReturn = (DataContainer) getRealObjectAndRelationships(dataContainerToReturn);

		// Return the result
		return dataContainerToReturn;
	}

	public Collection findByURIString(String uriString, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// The results
		Collection results = new ArrayList();

		// First make sure the incoming value is not null
		if ((uriString == null) || (uriString.equals(""))) {
			logger.debug("Failed: incoming uriString was null or empty");
			return results;
		}

		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					null, false, null, null, false, null, false, uriString,
					exactMatch, null, false, null, false, null, false, null,
					false, null, false, null, false, null, false, null, false,
					orderByPropertyName, ascendingOrDescending);
			results = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Check if full object graphs were requested
		if (returnFullObjectGraph)
			results = getRealObjectsAndRelationships(results);

		// Return the result
		return results;
	}

	public int countFindByURIString(String uriString, boolean exactMatch)
			throws MetadataAccessException {
		// First make sure the incoming value is not null
		if ((uriString == null) || (uriString.equals(""))) {
			logger.debug("Failed: incoming uriString was null or empty");
			return 0;
		}

		// The count to return
		int count = 0;

		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					null, false, null, null, false, null, false, uriString,
					exactMatch, null, false, null, false, null, false, null,
					false, null, false, null, false, null, false, null, false,
					null, null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the result
		return count;
	}

	public Collection findByURI(URI uri, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findByURL(URL url, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		return new ArrayList();
	}

	/**
	 * This method returns a <code>Collection</code> of <code>String</code>s
	 * that are the URI strings of all the dataContainers that are registered in
	 * SSDS.
	 * 
	 * @return a <code>Collection</code> of <code>String</code>s that are the
	 *         URI strings of all dataContainers in SSDS.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public Collection findAllURIStrings() throws MetadataAccessException {
		Collection dataContainerURIStrings = null;

		// Create the query and run it
		try {
			Query query = getSession()
					.createQuery(
							"select distinct dataContainer.uriString from "
									+ "DataContainer dataContainer order by dataContainer.uriString");
			dataContainerURIStrings = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return dataContainerURIStrings;
	}

	public Collection findByMimeType(String mimeType,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		return new ArrayList();
	}

	/**
	 * This method looks up and returns the persistent instance of a
	 * <code>DataContainer</code> with the specified DODS UrlString.
	 * 
	 * @param dodsUrLString
	 *            is the DODS URL (string form) of the
	 *            <code>DataContainer</code> to search for
	 * @return is the <code>DataContainer</code> that was found in the
	 *         persistent store with the given DODS URL string. If no
	 *         <code>DataContainer</code> was found, null is returned.
	 */
	public DataContainer findByDODSURLString(String dodsUrlString,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// First make sure the incoming value is not null
		if ((dodsUrlString == null) || (dodsUrlString.equals(""))) {
			logger.debug("Failed: incoming dodsUrlString was null or empty");
			return null;
		}

		// Create the DataContainer to return
		DataContainer dataContainerToReturn = null;

		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					null, false, null, null, false, null, false, null, false,
					null, false, dodsUrlString, true, null, false, null, false,
					null, false, null, false, null, false, null, false, null,
					null);
			dataContainerToReturn = (DataContainer) criteria.uniqueResult();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Check if full object graphs were requested
		if (returnFullObjectGraph)
			dataContainerToReturn = (DataContainer) getRealObjectAndRelationships(dataContainerToReturn);

		// Return the result
		return dataContainerToReturn;
	}

	public Collection findByDODSURLString(String dodsUrlString,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// The results
		Collection results = new ArrayList();

		// First make sure the incoming value is not null
		if ((dodsUrlString == null) || (dodsUrlString.equals(""))) {
			logger.debug("Failed: incoming dodsUrlString was null or empty");
			return results;
		}

		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					null, false, null, null, false, null, false, null, false,
					null, false, dodsUrlString, exactMatch, null, false, null,
					false, null, false, null, false, null, false, null, false,
					orderByPropertyName, ascendingOrDescending);
			results = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Check if full object graphs were requested
		if (returnFullObjectGraph)
			results = getRealObjectsAndRelationships(results);

		// Return the result
		return results;
	}

	public int countFindByDODSURLString(String dodsUrlString, boolean exactMatch)
			throws MetadataAccessException {
		// First make sure the incoming value is not null
		if ((dodsUrlString == null) || (dodsUrlString.equals(""))) {
			logger.debug("Failed: incoming uriString was null or empty");
			return 0;
		}

		// The count to return
		int count = 0;

		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					null, false, null, null, false, null, false, null, false,
					null, false, dodsUrlString, exactMatch, null, false, null,
					false, null, false, null, false, null, false, null, false,
					null, null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the result
		return count;
	}

	/**
	 * This method will return all <code>DataContainer</code>s that have data
	 * that is inside the specified geospatial box. In general if any sides of
	 * the box are not specified, the search dimension will be considered
	 * infinite in that direction.
	 * 
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
	 * @return A <code>Collection</code> of <code>DataContainer</code>s that
	 *         have data within that cube.
	 */
	public Collection findWithinGeospatialCube(Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// The results
		Collection results = null;

		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					null, false, null, null, false, null, false, null, false,
					null, false, null, false, geospatialLatMin, false,
					geospatialLatMax, false, geospatialLonMin, false,
					geospatialLonMax, false, geospatialVerticalMin, false,
					geospatialVerticalMax, false, orderByPropertyName,
					ascendingOrDescending);
			results = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Check if full object graphs were requested
		if (returnFullObjectGraph)
			results = getRealObjectsAndRelationships(results);

		// Return the result
		return results;
	}

	public int countFindWithinGeospatialCube(Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax) throws MetadataAccessException {
		// The result
		int count = 0;

		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					null, false, null, null, false, null, false, null, false,
					null, false, null, false, geospatialLatMin, false,
					geospatialLatMax, false, geospatialLonMin, false,
					geospatialLonMax, false, geospatialVerticalMin, false,
					geospatialVerticalMax, false, null, null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the result
		return count;
	}

	/**
	 * This method will return all <code>DataContainer</code>s that have data
	 * that is inside the specified geospatial box and within the time
	 * specified. In general if any sides of the box are not specified, the
	 * search dimension will be considered infinite in that direction.
	 * 
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
	 * @return A <code>Collection</code> of <code>DataContainer</code>s that
	 *         have data within that cube.
	 */
	public Collection findWithinTimeAndGeospatialCube(Date startDate,
			Date endDate, Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// The results
		Collection results = null;

		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					null, false, null, startDate, false, endDate, false, null,
					false, null, false, null, false, geospatialLatMin, false,
					geospatialLatMax, false, geospatialLonMin, false,
					geospatialLonMax, false, geospatialVerticalMin, false,
					geospatialVerticalMax, false, orderByPropertyName,
					ascendingOrDescending);
			results = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Check if full object graphs were requested
		if (returnFullObjectGraph)
			results = getRealObjectsAndRelationships(results);

		// Return the result
		return results;
	}

	public int countFindWithinTimeAndGeospatialCube(Date startDate,
			Date endDate, Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax)
			throws MetadataAccessException {

		// The result
		int count = 0;

		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					null, false, null, startDate, false, endDate, false, null,
					false, null, false, null, false, geospatialLatMin, false,
					geospatialLatMax, false, geospatialLonMin, false,
					geospatialLonMax, false, geospatialVerticalMin, false,
					geospatialVerticalMax, false, null, null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the result
		return count;
	}

	/**
	 * This method returns all <code>DataContainer</code>s that are linked
	 * (normally means owned) by a <code>Person</code>.
	 * 
	 * @param person
	 *            is the <code>Person</code> that will be used to search for
	 *            devices.
	 * @return a <code>Collection</code> of devices that are linked to that
	 *         person.
	 */
	public Collection findByPerson(Person person, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// The collection to return
		Collection dataContainers = new ArrayList();

		// First validate the incoming person
		if (person == null) {
			return dataContainers;
		}

		// First make sure the person exists
		PersonDAO personDAO = new PersonDAO(getSession());
		Person persistentPerson = (Person) personDAO
				.findEquivalentPersistentObject(person, false);

		// First validate the incoming person
		if (persistentPerson == null) {
			throw new MetadataAccessException(
					"No matching person could be found in the data system.");
		}

		// Create the criteria
		try {
			Criteria criteria = getSession()
					.createCriteria(DataContainer.class);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			criteria.add(Restrictions.eq("person", persistentPerson));
			addOrderByCriteria(criteria, orderByPropertyName,
					ascendingOrDescending);
			dataContainers = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			dataContainers = getRealObjectsAndRelationships(dataContainers);
		}

		return dataContainers;
	}

	public DataContainer findByRecordVariable(RecordVariable recordVariable,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// First make sure the incoming value is not null
		if (recordVariable == null) {
			logger.debug("Failed: incoming recordVariable was null");
			return null;
		}

		// Create the DataContainer to return
		DataContainer dataContainerToReturn = null;

		// The query string
		StringBuffer sqlStringBuffer = new StringBuffer();
		sqlStringBuffer
				.append("select distinct dataContainer from DataContainer dataContainer "
						+ "join dataContainer.recordDescription.recordVariables rv where rv.id");

		sqlStringBuffer.append(" = " + recordVariable.getId());

		try {
			dataContainerToReturn = (DataContainer) this.getSession()
					.createQuery(sqlStringBuffer.toString()).uniqueResult();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		if (returnFullObjectGraph)
			dataContainerToReturn = (DataContainer) getRealObjectAndRelationships(dataContainerToReturn);

		return dataContainerToReturn;

	}

	public Collection findByRecordVariableName(String recordVariableName,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// The collection to return
		Collection dataContainers = new ArrayList();

		// First make sure the incoming value is not null
		if ((recordVariableName == null) || (recordVariableName.equals(""))) {
			logger
					.debug("Failed: incoming recordVariableName was null or empty");
			return dataContainers;
		}

		// The query string
		StringBuffer sqlStringBuffer = new StringBuffer();
		sqlStringBuffer
				.append("select distinct dataContainer from DataContainer dataContainer "
						+ "join dataContainer.recordDescription.recordVariables rv where rv.name");

		if (exactMatch) {
			sqlStringBuffer.append(" = '" + recordVariableName + "'");
		} else {
			sqlStringBuffer.append(" like '%" + recordVariableName + "%'");
		}

		// Add order clause
		sqlStringBuffer.append(this.getOrderByPropertyNameSQLClause(
				orderByPropertyName, ascendingOrDescending));

		try {
			dataContainers = this.getSession().createQuery(
					sqlStringBuffer.toString()).list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		if (returnFullObjectGraph)
			dataContainers = getRealObjectsAndRelationships(dataContainers);

		return dataContainers;
	}

	public Collection findByLikeRecordVariableName(
			String likeRecordVariableName, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findByStandardVariableName(String standardVariableName,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// The collection to return
		Collection dataContainers = new ArrayList();

		// First make sure the incoming value is not null
		if ((standardVariableName == null) || (standardVariableName.equals(""))) {
			logger
					.debug("Failed: incoming standardVariableName was null or empty");
			return dataContainers;
		}

		// The query string
		StringBuffer sqlStringBuffer = new StringBuffer();
		sqlStringBuffer
				.append("select distinct dataContainer from DataContainer dataContainer join "
						+ "dataContainer.recordDescription.recordVariables recordVariable "
						+ "where recordVariable.standardVariable.name");

		if (exactMatch) {
			sqlStringBuffer.append(" = '" + standardVariableName + "'");
		} else {
			sqlStringBuffer.append(" like '%" + standardVariableName + "%'");
		}

		// Add order clause
		sqlStringBuffer.append(this.getOrderByPropertyNameSQLClause(
				orderByPropertyName, ascendingOrDescending));

		try {
			dataContainers = this.getSession().createQuery(
					sqlStringBuffer.toString()).list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		if (returnFullObjectGraph)
			dataContainers = getRealObjectsAndRelationships(dataContainers);

		return dataContainers;
	}

	public Collection findByLikeStandardVariableName(
			String likeStandardVariableName, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findByDataContainerGroup(
			DataContainerGroup dataContainerGroup, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// First check to see if the ID of the DataContainerGroup is specified.
		// If not grab it from the persistent store
		Long dataContainerGroupID = dataContainerGroup.getId();
		if (dataContainerGroupID == null) {
			DataContainerGroupDAO dataContainerGroupDAO = new DataContainerGroupDAO(
					getSession());
			dataContainerGroupID = dataContainerGroupDAO
					.findId(dataContainerGroup);
		}

		if (dataContainerGroupID == null)
			throw new MetadataAccessException(
					"A matching dataContainerGroup could not be found in the system");

		// The collection to return
		Collection dataContainersToReturn = new ArrayList();

		// Create the criteria
		try {
			Criteria criteria = getSession()
					.createCriteria(DataContainer.class);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			criteria.createAlias("dataContainerGroups", "dcgs");
			criteria.add(Restrictions.eq("dcgs.id", dataContainerGroupID));
			addOrderByCriteria(criteria, orderByPropertyName,
					ascendingOrDescending);
			dataContainersToReturn = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph)
			dataContainersToReturn = getRealObjectsAndRelationships(dataContainersToReturn);

		// Now return the results
		return dataContainersToReturn;
	}

	public Collection findByDataContainerGroupName(
			String dataContainerGroupName, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findByLikeDataContainerGroupName(
			String likeDataContainerGroupName, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		return new ArrayList();
	}

	/**
	 * This method looks up all <code>DataContainer</code>s that have a matching
	 * keyword with the name supplied. If the exactMatch boolean if false, a
	 * like search will be used
	 * 
	 * @param keywordName
	 *            is the name of the <code>Keyword</code> to search for
	 * @param exactMatch
	 *            is a <code>boolean</code> that indicates if the name is to
	 *            match the <code>Keyword</code> name exactly (<code>true</code>
	 *            ) or not (<code>false</code>)
	 * @param returnFullObjectGraph
	 *            is a boolean that specifies if the caller wants the fully
	 *            instantianted object graph (relationships) returned, or just
	 *            the query object itself. If you want the full graph returned
	 *            specify <code>true</code>, otherwise leave it false. <b>NOTE:
	 *            By specifying true, you could be requesting a large object
	 *            tree which will slow things down, so use sparingly</b>
	 * @return
	 * @throws MetadataAccessException
	 *             if the incoming keywordName is not valid or something goes
	 *             wrong in the search
	 */
	public Collection findByKeywordName(String keywordName, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		logger.debug("findByKeywordName called with:");
		logger.debug("keywordName = " + keywordName);
		logger.debug("exactMatch = " + exactMatch);
		logger.debug("orderByPropertyName = " + orderByPropertyName);
		logger.debug("ascendingOrDescending = " + ascendingOrDescending);
		logger.debug("returnFullObjectGraph = " + returnFullObjectGraph);

		// The collection to return
		Collection results = new ArrayList();

		// Check the name
		if ((keywordName == null) || (keywordName.equals("")))
			return results;

		// The query string
		StringBuffer sqlStringBuffer = new StringBuffer();
		sqlStringBuffer
				.append("select distinct dataContainer from DataContainer dataContainer "
						+ "join dataContainer.keywords keyword where keyword.name ");
		// sqlStringBuffer.append("select distinct dataContainer "
		// + "from DataContainer dataContainer "
		// + "where dataContainer.keywords.name ");
		if (exactMatch) {
			sqlStringBuffer.append(" = '" + keywordName + "'");
		} else {
			sqlStringBuffer.append(" like '%" + keywordName + "%'");
		}
		sqlStringBuffer.append(getOrderByPropertyNameSQLClause(
				orderByPropertyName, ascendingOrDescending));

		logger.debug("Going to use query " + sqlStringBuffer.toString());
		try {
			results = this.getSession().createQuery(sqlStringBuffer.toString())
					.list();
		} catch (HibernateException e) {
			logger.error("HibernateException caught trying to run query: "
					+ e.getMessage());
			throw new MetadataAccessException(e.getMessage());
		}

		if (returnFullObjectGraph)
			results = getRealObjectsAndRelationships(results);

		return results;
	}

	public int countFindByKeywordName(String keywordName, boolean exactMatch)
			throws MetadataAccessException {
		// The count to return
		int count = 0;

		// Check the name
		if ((keywordName == null) || (keywordName.equals("")))
			return 0;

		// The query string
		StringBuffer sqlStringBuffer = new StringBuffer();
		sqlStringBuffer.append("select count(distinct dataContainer) "
				+ "from DataContainer dataContainer "
				+ "join dataContainer.keywords keyword where keyword.name");
		if (exactMatch) {
			sqlStringBuffer.append(" = '" + keywordName + "'");
		} else {
			sqlStringBuffer.append(" like '%" + keywordName + "%'");
		}

		try {
			count = ((Long) this.getSession().createQuery(
					sqlStringBuffer.toString()).uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		return count;
	}

	public Collection findByResource(Resource resource,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// The results to return
		Collection results = new ArrayList();

		// If the resource is null return null
		if (resource == null)
			return results;

		// First make sure the resource exists
		ResourceDAO resourceDAO = new ResourceDAO(getSession());

		Resource persistentResource = null;
		persistentResource = (Resource) resourceDAO
				.findEquivalentPersistentObject(resource, false);

		if (persistentResource == null)
			throw new MetadataAccessException(
					"A matching resource could not be found in the system");

		// The query string
		StringBuffer sqlStringBuffer = new StringBuffer();

		// Now create the query
		Query query = null;

		sqlStringBuffer.append("select distinct dataContainer from "
				+ "DataContainer dataContainer, Resource resource where");
		sqlStringBuffer.append(" resource.id = :resourceID and ");
		sqlStringBuffer
				.append(" resource in elements(dataContainer.resources)");

		if ((orderByPropertyName != null)
				&& (checkIfPropertyOK(orderByPropertyName))) {
			sqlStringBuffer.append(" order by dataContainer."
					+ orderByPropertyName);
			if ((ascendingOrDescending != null)
					&& ((ascendingOrDescending
							.equals(MetadataDAO.ASCENDING_ORDER)) || (ascendingOrDescending
							.equals(MetadataDAO.DESCENDING_ORDER)))) {
				sqlStringBuffer.append(" " + ascendingOrDescending);
			}
		}
		try {
			query = this.getSession().createQuery(sqlStringBuffer.toString());
			query.setLong("resourceID", persistentResource.getId().longValue());
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		results = query.list();

		if (returnFullObjectGraph)
			results = getRealObjectsAndRelationships(results);

		return results;
	}

	public Collection findAllIndirectCreators(DataContainer dataContainer,
			int fetchDepth, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findCreatorChain(DataContainer dataContainer,
			int fetchDepth, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findAllIndirectConsumers(DataContainer dataContainer,
			int fetchDepth, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findAllConsumers(DataContainer dataContainer,
			int fetchDepth, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findDirectInputs(DataContainer dataContainer,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findAllInputs(DataContainer dataContainer,
			int fetchDepth, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findAllDerivedOutputs(DataContainer dataContainer,
			int fetchDepth, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		return new ArrayList();
	}

	public void addAllDerivedOutputsToCollection(Collection derivedOutputs,
			DataContainer dataContainer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// Grab the persistent equivalent
		DataContainer persistentDC = (DataContainer) this
				.findEquivalentPersistentObject(dataContainer, false);

		// Now add all the outputs of the consumers
		Collection consumers = persistentDC.getConsumers();
		if ((fetchDepth > 0) && (consumers != null)) {
			int newFetchDepth = fetchDepth - 1;
			Iterator consumerIter = consumers.iterator();
			while (consumerIter.hasNext()) {
				DataProducer consumer = (DataProducer) consumerIter.next();
				// Grab the outputs
				Collection outputs = consumer.getOutputs();
				// Iterate over them
				Iterator outputsIter = outputs.iterator();
				while (outputsIter.hasNext()) {
					DataContainer output = (DataContainer) outputsIter.next();
					if (!derivedOutputs.contains(output))
						derivedOutputs.add(output);
					this.addAllDerivedOutputsToCollection(derivedOutputs,
							output, newFetchDepth, orderByPropertyName,
							ascendingOrDescending, returnFullObjectGraph);
				}
			}
		}
	}

	public Collection findAllDerivedOutputs(DataProducer dataProducer,
			int fetchDepth, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// The ArrayList to return
		Collection derivedOutputs = new ArrayList();

		// Now grab the persistent equivalent for the DataProducer that is input
		DataProducerDAO dpdao = new DataProducerDAO(this.getSession());
		DataProducer persistentDP = (DataProducer) dpdao
				.findEquivalentPersistentObject(dataProducer, false);

		// Now add its outputs to the collection
		derivedOutputs.addAll(persistentDP.getOutputs());

		// If the fetch depth is greater than zero, keep going
		if (fetchDepth > 0) {
			Iterator outputIter = persistentDP.getOutputs().iterator();
			while (outputIter.hasNext()) {
				this.addAllDerivedOutputsToCollection(derivedOutputs,
						(DataContainer) outputIter.next(), (fetchDepth - 1),
						orderByPropertyName, ascendingOrDescending,
						returnFullObjectGraph);
			}
		}

		// Now drill down through the DataContainers
		return derivedOutputs;
	}

	public void addAllDerivedOutputsToCollection(Collection derivedOutputs,
			DataProducer dataProducer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		return;
	}

	/**
	 * This method returns a <code>Collection</code> of
	 * <code>DataContainer</code>s that are inputs to the given
	 * <code>DataProducer</code>
	 * 
	 * @param dataProducer
	 *            The criteria for this search
	 * @param orderByPropertyName
	 *            This is a string that can be used to try and specify the
	 *            property that the query should try to order the results by. It
	 *            must match a property name of <code>DataProducer</code>.
	 * @param returnFullObjectGraphs
	 *            If true then return populated object graphs
	 * @return the <code>Collection</code> of <code>DataContainer</code>s that
	 *         were consumed by the given dataProducer.
	 * @throws MetadataAccessException
	 *             if something goes wrong
	 */
	public Collection findInputsByDataProducer(DataProducer dataProducer,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// The collection to return
		Collection results = new ArrayList();
		StringBuffer sqlStringBuffer = new StringBuffer();

		// If the dataProducer is null return an empty collection
		if (dataProducer == null)
			return results;

		Query query = null;

		sqlStringBuffer.append("select distinct dataContainer from "
				+ "DataContainer dataContainer join");
		sqlStringBuffer
				.append(" dataContainer.consumers consumer where consumer.id = :dataProducerID");
		sqlStringBuffer.append(this.getOrderByPropertyNameSQLClause(
				orderByPropertyName, ascendingOrDescending));

		try {
			query = this.getSession().createQuery(sqlStringBuffer.toString());
			query.setLong("dataProducerID", dataProducer.getId().longValue());
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		results = query.list();

		if (returnFullObjectGraph)
			results = getRealObjectsAndRelationships(results);

		return results;

	}

	/**
	 * This method returns a <code>Collection</code> of
	 * <code>DataContainer</code>s that are outputs of the given
	 * <code>DataProducer</code>
	 * 
	 * @param dataProducer
	 *            The criteria for this search
	 * @param orderByPropertyName
	 *            This is a string that can be used to try and specify the
	 *            property that the query should try to order the results by. It
	 *            must match a property name of <code>DataProducer</code>.
	 * @param returnFullObjectGraphs
	 *            If true then return populated object graphs
	 * @return the <code>Collection</code> of <code>DataContainer</code>s that
	 *         were created by the given dataProducer.
	 * @throws MetadataAccessException
	 *             if something goes wrong
	 */
	public Collection findOutputsByDataProducer(DataProducer dataProducer,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// The collection to return
		Collection outputs = new HashSet();

		// Grab the persistent DataProducer
		DataProducerDAO dataProducerDAO = new DataProducerDAO(getSession());
		DataProducer persistentDataProducer = (DataProducer) dataProducerDAO
				.findEquivalentPersistentObject(dataProducer, false);

		// Now create a copy of the outputs
		if (persistentDataProducer != null)
			outputs = new HashSet(persistentDataProducer.getOutputs());

		if (returnFullObjectGraph)
			outputs = getRealObjectsAndRelationships(outputs);

		return outputs;

	}

	public DataContainer findBestDirectOutput(DataProducer dataProducer,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		return null;
	}

	public Collection findByRecordVariableNameAndDataWithinTimeWindow(
			String recordVariableName, Date startDate, Date endDate,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findByLikeRecordVariableNameAndDataWithinTimeWindow(
			String likeRecordVariableName, Date startDate, Date endDate,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findByStandardVariableNameAndDataWithinTimeWindow(
			String standardVariableName, Date startDate, Date endDate,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findByLikeStandardVariableNameAndDataWithinTimeWindow(
			String likeStandardVariableName, Date startDate, Date endDate,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findByRecordVariableNameAndWithinGeospatialCube(
			String recordVariableName, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findByLikeRecordVariableNameAndWithinGeospatialCube(
			String likeRecordVariableName, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findByStandardVariableNameAndWithinGeospatialCube(
			String standardVariableName, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findByLikeStandardVariableNameAndWithinGeospatialCube(
			String likeStandardVariableName, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findByRecordVariableNameWithinTimeAndWithinGeospatialCube(
			String recordVariableName, Date startDate, Date endDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findByLikeRecordVariableNameWithinTimeAndWithinGeospatialCube(
			String likeRecordVariableName, Date startDate, Date endDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findByStandardVariableNameWithinTimeAndWithinGeospatialCube(
			String standardVariableName, Date startDate, Date endDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		return new ArrayList();
	}

	public Collection findByLikeStandardVariableNameWithinTimeAndWithinGeospatialCube(
			String likeStandardVariableName, Date startDate, Date endDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		return new ArrayList();
	}

	/**
	 * @see IMetadataDAO#makePersistent(IMetadataObject)
	 */
	public Long makePersistent(IMetadataObject metadataObject)
			throws MetadataAccessException {

		logger.debug("makePersistent called");

		// A flag to indicate if the DataContainer was persisted before
		boolean persistedBefore = false;

		// Check incoming object
		DataContainer dataContainer = this
				.checkIncomingMetadataObject(metadataObject);

		// First look up the equivalent persistent instance (and pull it into
		// the session)
		DataContainer persistentDataContainer = (DataContainer) this
				.findEquivalentPersistentObject(dataContainer, false);

		// Create a handle to the object that will actually be persisted
		DataContainer dataContainerToPersist = null;

		// If DataContainer was persisted in the past, copy over any non-null,
		// changed fields and assign to the persisting handle
		if (persistentDataContainer != null) {
			String persistedDataContainerBeforeString = persistentDataContainer
					.toStringRepresentation("<li>");
			if (this.updateDestinationObject(dataContainer,
					persistentDataContainer)) {
				addMessage(ssdsAdminEmailToAddress,
						"A DataContainer was updated in SSDS:<br><b>Before</b><br><ul><li>"
								+ persistedDataContainerBeforeString
								+ "</ul><br><b>After</b><ul><li>"
								+ persistentDataContainer
										.toStringRepresentation("<li>")
								+ "</ul><br>");
			}

			// Set the flag
			persistedBefore = true;

			// Assign to the handle
			dataContainerToPersist = persistentDataContainer;
		} else {
			// This means it will be a new DataContainer, so let's make sure the
			// alternate business key is not empty
			if ((dataContainer.getDataContainerType() == null)
					|| (!DataContainer.isValidDataContainerType(dataContainer
							.getDataContainerType()))) {
				try {
					dataContainer.setDataContainerType(DataContainer.TYPE_FILE);
				} catch (MetadataException e) {
				}
				addMessage(ssdsAdminEmailToAddress,
						"An incoming DataContainer did not have a "
								+ "correct DataContainer Type assigned, "
								+ "so SSDS auto-generated one:<br><ul><li>"
								+ dataContainer.toStringRepresentation("<li>")
								+ "</ul><br>");
			}
			if ((dataContainer.getUriString() == null)
					|| (dataContainer.getUriString().equals(""))) {
				try {
					dataContainer.setUriString("http://ssds.mbari.org/data"
							+ dataContainer.getDataContainerType() + "/"
							+ getUniqueNameSuffix());
				} catch (MetadataException e) {
					logger
							.error("MetadataExceptin caught trying to auto-generate "
									+ "the URIString for an incoming DataContainer: "
									+ e.getMessage());
				}
				addMessage(
						ssdsAdminEmailToAddress,
						"An incoming DataContainer did not have a "
								+ "URIString, so SSDS auto-generated one<ul><li>"
								+ dataContainer.toStringRepresentation("<li>")
								+ "</ul><br>");
			}

			// Clear the persisted before flag
			persistedBefore = false;

			// Assign to the handle
			dataContainerToPersist = dataContainer;
		}

		// -------------------------------
		// DataContainerGroup Relationship
		// -------------------------------

		// First make sure the DataContainerGroup relationship exists
		if (dataContainer.getDataContainerGroups() != null) {
			// Now check to make sure it is initialized. If it is not, no
			// changes have taken place and we don't need to do anything
			if (Hibernate.isInitialized(dataContainer.getDataContainerGroups())) {

				// Grab the DAO for DataContainerGroup
				DataContainerGroupDAO dataContainerGroupDAO = new DataContainerGroupDAO(
						this.getSession());

				// Make sure the are DataContainerGroups to iterate over
				if (dataContainer.getDataContainerGroups().size() > 0) {

					// Now iterate over the DataContainerGroups and persist them
					Iterator dataContainerGroupIter = dataContainer
							.getDataContainerGroups().iterator();
					while (dataContainerGroupIter.hasNext()) {
						DataContainerGroup tempDataContainerGroup = (DataContainerGroup) dataContainerGroupIter
								.next();
						dataContainerGroupDAO
								.makePersistent(tempDataContainerGroup);
					}

					// Create a copy of the collection associated with the
					// DataContainer to prevent concurrent modifications
					Collection dataContainerDataContainerGroupCopy = new ArrayList(
							dataContainer.getDataContainerGroups());

					// Now we need to make the correct associations. Currently,
					// you have a collection of DataContainerGroup objects that
					// have their values marked for persistence. Now the object
					// will either be in the session or not depending on if they
					// were previously persisted.
					Iterator dataContainerDataContainerGroupCopyIterator = dataContainerDataContainerGroupCopy
							.iterator();
					while (dataContainerDataContainerGroupCopyIterator
							.hasNext()) {
						DataContainerGroup currentDataContainerGroup = (DataContainerGroup) dataContainerDataContainerGroupCopyIterator
								.next();
						DataContainerGroup currentDataContainerGroupInSession = null;
						// Is this DataContainerGroup already in the session?
						if (!getSession().contains(currentDataContainerGroup)) {
							// No, so grab the one that is
							currentDataContainerGroupInSession = (DataContainerGroup) dataContainerGroupDAO
									.findEquivalentPersistentObject(
											currentDataContainerGroup, false);
						} else {
							currentDataContainerGroupInSession = currentDataContainerGroup;
						}
						// Now if the parent dataContainer was persisted before,
						// just check to make sure the sessioned
						// DataContainerGroup is in the collection associated
						// with the dataContainer that will be persisted
						if (persistedBefore) {
							if (!dataContainerToPersist
									.getDataContainerGroups().contains(
											currentDataContainerGroupInSession))
								dataContainerToPersist
										.getDataContainerGroups()
										.add(currentDataContainerGroupInSession);
						} else {
							// This means that the dataContainer has not been
							// persisted before. If the DataContainerGroup is
							// already in the session, there is nothing to do,
							// but if not, we need to replace it with the
							// sessioned one
							if (!getSession().contains(
									currentDataContainerGroup)) {
								dataContainerToPersist.getDataContainerGroups()
										.remove(currentDataContainerGroup);
								dataContainerToPersist
										.getDataContainerGroups()
										.add(currentDataContainerGroupInSession);
							}
						}
					}
				}
			}
		}

		// ------------------------------
		// RecordDescription Relationship
		// ------------------------------

		// Since the DataContainer->RecordDescription->RecordVariable is handled
		// automatically, we need to walk that and persist/find and
		// standardVariables, standardUnits, standardDomain, standardKeyword,
		// standardReferenceScale that are associated with RecordVariables
		if ((dataContainer.getRecordDescription() != null)
				&& (Hibernate.isInitialized(dataContainer
						.getRecordDescription()))) {
			if ((dataContainer.getRecordDescription().getRecordVariables() != null)
					&& (Hibernate.isInitialized(dataContainer
							.getRecordDescription().getRecordVariables()))
					&& (dataContainer.getRecordDescription()
							.getRecordVariables().size() > 0)) {
				Iterator rvIter = dataContainer.getRecordDescription()
						.getRecordVariables().iterator();
				while (rvIter.hasNext()) {
					RecordVariable currentRecordVariable = (RecordVariable) rvIter
							.next();
					// Check the StandardVariable relationship
					if ((currentRecordVariable.getStandardVariable() != null)
							&& (Hibernate.isInitialized(currentRecordVariable
									.getStandardVariable()))) {
						StandardVariableDAO standardVariableDAO = new StandardVariableDAO(
								this.getSession());
						standardVariableDAO
								.makePersistent(currentRecordVariable
										.getStandardVariable());
						// Now if the currently associated StandardVariable is
						// not in the session, it must be replaced by the one
						// that is
						if (!getSession().contains(
								currentRecordVariable.getStandardVariable())) {
							StandardVariable svInSession = (StandardVariable) standardVariableDAO
									.findEquivalentPersistentObject(
											currentRecordVariable
													.getStandardVariable(),
											false);
							currentRecordVariable
									.setStandardVariable(svInSession);
						}
					}
					// Now for the StandardUnit
					if ((currentRecordVariable.getStandardUnit() != null)
							&& (Hibernate.isInitialized(currentRecordVariable
									.getStandardUnit()))) {
						StandardUnitDAO standardUnitDAO = new StandardUnitDAO(
								this.getSession());
						standardUnitDAO.makePersistent(currentRecordVariable
								.getStandardUnit());
						// Now if the currently associated StandardUnit is
						// not in the session, it must be replaced by the one
						// that is
						if (!getSession().contains(
								currentRecordVariable.getStandardUnit())) {
							StandardUnit suInSession = (StandardUnit) standardUnitDAO
									.findEquivalentPersistentObject(
											currentRecordVariable
													.getStandardUnit(), false);
							currentRecordVariable.setStandardUnit(suInSession);
						}
					}
					// Now for the StandardDomain
					if ((currentRecordVariable.getStandardDomain() != null)
							&& (Hibernate.isInitialized(currentRecordVariable
									.getStandardDomain()))) {
						StandardDomainDAO standardDomainDAO = new StandardDomainDAO(
								this.getSession());
						standardDomainDAO.makePersistent(currentRecordVariable
								.getStandardDomain());
						// Now if the currently associated StandardDomain is
						// not in the session, it must be replaced by the one
						// that is
						if (!getSession().contains(
								currentRecordVariable.getStandardDomain())) {
							StandardDomain sdInSession = (StandardDomain) standardDomainDAO
									.findEquivalentPersistentObject(
											currentRecordVariable
													.getStandardDomain(), false);
							currentRecordVariable
									.setStandardDomain(sdInSession);
						}
					}
					// Now for the StandardKeyword
					if ((currentRecordVariable.getStandardKeyword() != null)
							&& (Hibernate.isInitialized(currentRecordVariable
									.getStandardKeyword()))) {
						StandardKeywordDAO standardKeywordDAO = new StandardKeywordDAO(
								this.getSession());
						standardKeywordDAO.makePersistent(currentRecordVariable
								.getStandardKeyword());
						// Now if the currently associated
						// StandardKeyword is not in the session, it must
						// be replaced by the one that is
						if (!getSession().contains(
								currentRecordVariable.getStandardKeyword())) {
							StandardKeyword skInSession = (StandardKeyword) standardKeywordDAO
									.findEquivalentPersistentObject(
											currentRecordVariable
													.getStandardKeyword(),
											false);
							currentRecordVariable
									.setStandardKeyword(skInSession);
						}
					}
					// Now for the StandardReferenceScale
					if ((currentRecordVariable.getStandardReferenceScale() != null)
							&& (Hibernate.isInitialized(currentRecordVariable
									.getStandardReferenceScale()))) {
						StandardReferenceScaleDAO standardReferenceScaleDAO = new StandardReferenceScaleDAO(
								this.getSession());
						standardReferenceScaleDAO
								.makePersistent(currentRecordVariable
										.getStandardReferenceScale());
						// Now if the currently associated
						// StandardReferenceScale is not in the session, it must
						// be replaced by the one that is
						if (!getSession().contains(
								currentRecordVariable
										.getStandardReferenceScale())) {
							StandardReferenceScale srsInSession = (StandardReferenceScale) standardReferenceScaleDAO
									.findEquivalentPersistentObject(
											currentRecordVariable
													.getStandardReferenceScale(),
											false);
							currentRecordVariable
									.setStandardReferenceScale(srsInSession);
						}
					}
				}
			}
			// ------------------------------
			// RecordDescription Relationship
			// ------------------------------
			if (persistentDataContainer != null) {
				// This one is a little touchy as it is is a cascade all
				// relationship so we need to be careful on how we handle the
				// incoming and currently persisted object. First, check to see
				// if
				// the persisted DataContainer has any RecordDescription at all.
				if (persistentDataContainer.getRecordDescription() == null) {
					// If this is the case, check to see if the incoming one has
					// one
					if (dataContainer.getRecordDescription() != null) {
						// Set the incoming one to the persistent DataContainer
						RecordDescription rdToTransfer = dataContainer
								.getRecordDescription();
						dataContainer.setRecordDescription(null);
						persistentDataContainer
								.setRecordDescription(rdToTransfer);
					}
				} else {
					// Now this is the case where a RecordDescription already
					// exists. First check to see if the incoming has one at all
					if (dataContainer.getRecordDescription() != null) {
						// OK, there is an incoming RD and so we will want to
						// update the persistent RD with the information in the
						// incoming RD unless the incoming RD has an ID that is
						// different than the persistent one
						if ((dataContainer.getRecordDescription().getId() != null)
								&& (dataContainer.getRecordDescription()
										.getId().longValue() != persistentDataContainer
										.getRecordDescription().getId()
										.longValue())) {
							RecordDescription rdToTransfer = dataContainer
									.getRecordDescription();
							RecordDescription rdToOrphan = persistentDataContainer
									.getRecordDescription();
							dataContainer.setRecordDescription(null);
							persistentDataContainer
									.setRecordDescription(rdToTransfer);
							// Remove the old one so no orphans exist
							if (rdToOrphan != null) {
								getSession().delete(rdToOrphan);
							}
						} else {
							// Since we are not in that special case, simply
							// update the existing one
							this.updateDestinationObject(dataContainer
									.getRecordDescription(),
									persistentDataContainer
											.getRecordDescription());
							// Now we need to update the RecordVariables from
							// the incoming to the persistent one
							this
									.updateDestinationRecordDescriptionWithRecordVariables(
											dataContainer
													.getRecordDescription(),
											persistentDataContainer
													.getRecordDescription());
						}
					} else {
						// In this case, we will assume the one existing is the
						// one we want to keep and so we will do nothing
					}
				}
			}
		}

		// ------------------------------
		// HeaderDescription Relationship
		// ------------------------------
		// This relationship is identical to the RecordDescription relationship
		if (dataContainer.getHeaderDescription() != null) {
			// Now make sure it is initialized
			if (Hibernate.isInitialized(dataContainer.getHeaderDescription())) {
				// If this is the case, and since the
				// DataContainer->HeaderDescription relationship is cascade all,
				// we just need to check for an existing DataContainer and set
				// it's HeaderDescription to the incoming one
				if (persistentDataContainer != null) {
					HeaderDescription hdToTransfer = dataContainer
							.getHeaderDescription();
					dataContainer.setHeaderDescription(null);
					persistentDataContainer.setHeaderDescription(hdToTransfer);
					// Since we are copying over, clear any IDs so that cascades
					// will work correctly
					persistentDataContainer.getHeaderDescription().setId(null);
					if (persistentDataContainer.getHeaderDescription()
							.getCommentTags() != null) {
						Iterator ctIter = persistentDataContainer
								.getHeaderDescription().getCommentTags()
								.iterator();
						while (ctIter.hasNext())
							((CommentTag) ctIter.next()).setId(null);
					}
				}
			}
		}

		// -------------------
		// Person Relationship
		// -------------------
		// First see if there is a person associated with the incoming
		// dataContainer
		if (dataContainer.getPerson() != null) {
			// Now, since there is, check to see if it has been initialized
			if (Hibernate.isInitialized(dataContainer.getPerson())) {
				// Grab the Person DAO to handle that relationship
				PersonDAO personDAO = new PersonDAO(this.getSession());

				// Now persist the person
				Person tempPerson = dataContainer.getPerson();
				personDAO.makePersistent(tempPerson);

				// The matching person that is in the session
				Person tempPersonInSession = null;

				// Check to see if the persisted person is in the session
				if (!getSession().contains(tempPerson)) {
					tempPersonInSession = (Person) personDAO
							.findEquivalentPersistentObject(tempPerson, false);
				} else {
					tempPersonInSession = tempPerson;
				}

				// Now check to see if the dataContainer was persisted in the
				// past, if
				// so, just check to see if dataContainers person is different
				// and
				// update it if so
				if (persistedBefore) {
					if ((dataContainerToPersist.getPerson() == null)
							|| (!dataContainerToPersist.getPerson().equals(
									tempPersonInSession))) {
						dataContainerToPersist.setPerson(tempPersonInSession);
					}
				} else {
					// Make sure the person associated with the dataContainer is
					// the
					// session, if not replace it with the one that is
					if (!getSession().contains(
							dataContainerToPersist.getPerson())) {
						dataContainerToPersist.setPerson(tempPersonInSession);
					}
				}
			}
		}

		// ---------------------
		// Keyword Relationship
		// ----------------------
		// First make sure the keyword relationship exists
		if (dataContainer.getKeywords() != null) {
			// Now check to make sure it is initialized. If it is not, no
			// changes have taken place and we don't need to do anything
			if (Hibernate.isInitialized(dataContainer.getKeywords())) {

				// Grab the DAO for Keyword
				KeywordDAO keywordDAO = new KeywordDAO(this.getSession());

				// Make sure the are keywords to iterate over
				if (dataContainer.getKeywords().size() > 0) {

					// Now iterate over the Keywords and persist them
					Iterator keywordIter = dataContainer.getKeywords()
							.iterator();
					while (keywordIter.hasNext()) {
						Keyword tempKeyword = (Keyword) keywordIter.next();
						keywordDAO.makePersistent(tempKeyword);
					}
				}

				// Create a copy of the collection associated with the
				// dataContainer to prevent concurrent modifications
				Collection dataContainerKeywordCopy = new ArrayList(
						dataContainer.getKeywords());

				// Now we need to make the correct associations. Currently, you
				// have a collection of Keyword objects that have their values
				// marked for persistence. Now the object will either be in the
				// session or not depending on if they were previously
				// persisted.
				Iterator dataContainerKeywordCopyIterator = dataContainerKeywordCopy
						.iterator();
				while (dataContainerKeywordCopyIterator.hasNext()) {
					Keyword currentKeyword = (Keyword) dataContainerKeywordCopyIterator
							.next();
					Keyword currentKeywordInSession = null;
					// Is this Keyword already in the session?
					if (!getSession().contains(currentKeyword)) {
						// No, so grab the one that is
						currentKeywordInSession = (Keyword) keywordDAO
								.findEquivalentPersistentObject(currentKeyword,
										false);
					} else {
						currentKeywordInSession = currentKeyword;
					}
					// Now if the parent dataContainer was persisted before,
					// just check to make sure the sessioned Keywords is in the
					// collection are associated with the dataContainer that
					// will be persisted
					if (persistedBefore) {
						if (!dataContainerToPersist.getKeywords().contains(
								currentKeywordInSession))
							dataContainerToPersist.getKeywords().add(
									currentKeywordInSession);
					} else {
						// This means that the dataContainer has not been
						// persisted before. If the Keyword is already in the
						// session, there is nothing to do, but if not, we need
						// to replace it with the sessioned one
						if (!getSession().contains(currentKeyword)) {
							dataContainerToPersist.getKeywords().remove(
									currentKeyword);
							dataContainerToPersist.getKeywords().add(
									currentKeywordInSession);
						}
					}
				}
			}
		}

		// ---------------------
		// Resource Relationship
		// ----------------------
		// First make sure the resources relationship exists
		if (dataContainer.getResources() != null) {
			// Now check to make sure it is initialized. If it is not, no
			// changes have taken place and we don't need to do anything
			if (Hibernate.isInitialized(dataContainer.getResources())) {

				// Grab the DAO for Resource
				ResourceDAO resourceDAO = new ResourceDAO(this.getSession());

				// Make sure the are resources to iterate over
				if (dataContainer.getResources().size() > 0) {

					// Now iterate over the Resources and persist them
					Iterator userGroupIter = dataContainer.getResources()
							.iterator();
					while (userGroupIter.hasNext()) {
						Resource tempResource = (Resource) userGroupIter.next();
						resourceDAO.makePersistent(tempResource);
					}
				}

				// Create a copy of the collection associated with the
				// dataContainer to
				// prevent concurrent modifications
				Collection dataContainerResourceCopy = new ArrayList(
						dataContainer.getResources());

				// Now we need to make the correct associations. Currently, you
				// have a collection of Resource objects that have their values
				// marked for persistence. Now the object will either be in the
				// session or not depending on if they were previously
				// persisted.
				Iterator dataContainerResourceCopyIterator = dataContainerResourceCopy
						.iterator();
				while (dataContainerResourceCopyIterator.hasNext()) {
					Resource currentResource = (Resource) dataContainerResourceCopyIterator
							.next();
					Resource currentResourceInSession = null;
					// Is this Resource already in the session?
					if (!getSession().contains(currentResource)) {
						// No, so grab the one that is
						currentResourceInSession = (Resource) resourceDAO
								.findEquivalentPersistentObject(
										currentResource, false);
					} else {
						currentResourceInSession = currentResource;
					}
					// Now if the parent dataContainer was persisted before,
					// just
					// check to make sure the sessioned Resources is in the
					// collection are associated with the dataContainer that
					// will be
					// persisted
					if (persistedBefore) {
						if (!dataContainerToPersist.getResources().contains(
								currentResourceInSession))
							dataContainerToPersist.getResources().add(
									currentResourceInSession);
					} else {
						// This means that the dataContainer has not been
						// persisted
						// before. If the Resource is already in the session,
						// there is nothing to do, but if not, we need to
						// replace it with the sessioned one
						if (!getSession().contains(currentResource)) {
							dataContainerToPersist.getResources().remove(
									currentResource);
							dataContainerToPersist.getResources().add(
									currentResourceInSession);
						}
					}
				}
			}
		}

		// If not persisted in the past, save it
		if (!persistedBefore)
			getSession().save(dataContainerToPersist);

		// Now return the Id
		if (dataContainerToPersist != null) {
			return dataContainerToPersist.getId();
		} else {
			return null;
		}
	}

	/**
	 * This is a method that updates on RecordDescriptions variable from
	 * another. For safety's sake (and the sanity of yours truly), this will be
	 * an additive update. If there are variables missing from the source, they
	 * will NOT be removed from the destination
	 * 
	 * @param sourceRecordDescription
	 * @param destinationRecordDescription
	 */
	private void updateDestinationRecordDescriptionWithRecordVariables(
			RecordDescription sourceRecordDescription,
			RecordDescription destinationRecordDescription) {
		if ((sourceRecordDescription != null)
				&& (destinationRecordDescription != null)) {
			// The collection of RV's that will be copied to the destination
			// when all is said and done
			Collection<RecordVariable> rvsToAdd = new ArrayList<RecordVariable>();
			for (Iterator rvIter = sourceRecordDescription.getRecordVariables()
					.iterator(); rvIter.hasNext();) {
				RecordVariable rvToMigrate = (RecordVariable) rvIter.next();
				boolean matchingRVfound = false;
				// Iterate over the destination RV's
				for (Iterator targetRVIter = destinationRecordDescription
						.getRecordVariables().iterator(); targetRVIter
						.hasNext();) {
					RecordVariable targetRV = (RecordVariable) targetRVIter
							.next();
					// If the ID or the name match
					if (((rvToMigrate.getId() != null)
							&& (targetRV.getId() != null)
							&& (rvToMigrate.getId().longValue() == targetRV
									.getId().longValue()) || ((rvToMigrate
							.getName() != null)
							&& (!rvToMigrate.getName().equals(""))
							&& (targetRV.getName() != null)
							&& (!targetRV.getName().equals("")) && (rvToMigrate
							.getName().equalsIgnoreCase(targetRV.getName()))))) {
						try {
							this.updateDestinationObject(rvToMigrate, targetRV);
						} catch (MetadataAccessException e) {
							logger
									.error("MetadataAccessException caught trying to update one RecordVariable with another:"
											+ e.getMessage());
						}
						// Set the flag that a matching variable was found and
						// bail out of the comparison
						matchingRVfound = true;
						break;
					}
				}
				// If no matching RV was found, add it
				if (!matchingRVfound) {
					rvsToAdd.add(rvToMigrate);
				}
			}
			// Now that we have checked, if there are any to add, add them now
			// to the destination
			if (rvsToAdd.size() > 0) {
				for (Iterator rvsToAddIter = rvsToAdd.iterator(); rvsToAddIter
						.hasNext();) {
					RecordVariable rvToAdd = (RecordVariable) rvsToAddIter
							.next();
					destinationRecordDescription.addRecordVariable(rvToAdd);
				}
			}
		}
	}

	/**
	 * @see IMetadataDAO#makeTransient(IMetadataObject)
	 */
	public void makeTransient(IMetadataObject metadataObject)
			throws MetadataAccessException {
		logger.debug("makeTransient called");

		// Check incoming object
		DataContainer dataContainer = this
				.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		DataContainer persistentDataContainer = (DataContainer) this
				.findEquivalentPersistentObject(dataContainer, false);

		// If no matching dataContainer was found, do nothing
		if (persistentDataContainer == null) {
			logger
					.debug("No matching dataContainer could be found in the persistent store, "
							+ "no delete performed");
		} else {
			// Clear from the inputs of DataProducers
			DataProducerDAO dpDAO = new DataProducerDAO(getSession());
			Collection dpsUsingDC = dpDAO.findByInput(persistentDataContainer,
					null, null, false);
			Iterator dpIter = dpsUsingDC.iterator();
			while (dpIter.hasNext()) {
				DataProducer dp = (DataProducer) dpIter.next();
				if (dp != null) {
					dp.removeInput(persistentDataContainer);
				}
			}

			// Handle the relationships
			persistentDataContainer.setPerson(null);
			persistentDataContainer.clearDataContainerGroups();
			persistentDataContainer.clearKeywords();
			persistentDataContainer.clearResources();

			logger
					.debug("Existing object was found, so we will try to delete it");
			try {
				logger.debug("Going to delete DataContainer "
						+ persistentDataContainer.toStringRepresentation("|"));
				if (persistentDataContainer.getRecordDescription() != null) {
					logger
							.debug("RecordDescription associated with DataContainer is "
									+ persistentDataContainer
											.getRecordDescription()
											.toStringRepresentation("|"));
					if (persistentDataContainer.getRecordDescription()
							.getRecordVariables() != null) {
						logger.debug("There are "
								+ persistentDataContainer
										.getRecordDescription()
										.getRecordVariables().size()
								+ " record variables that should be removed");
					} else {
						logger.debug("No record variables to remove");
					}
				} else {
					logger
							.debug("No RecordDescription on persistentDataContainer to remove");
				}
				// Now the container itself
				getSession().delete(persistentDataContainer);
			} catch (HibernateException e) {
				logger.error("HibernateException caught (will be re-thrown):"
						+ e.getMessage());
				throw new MetadataAccessException(e);
			}
		}
	}

	/**
	 * This method checks to make sure an incoming <code>MetadataObject</code>
	 * is not null and is in fact of the correct class. It then converts it to
	 * the correct class and returns it
	 * 
	 * @param metadataObject
	 *            the <code>MetadataObject</code> to check and return as a
	 *            <code>DataContainer</code>
	 * @return a <code>DataContainer</code> that is same object that came in
	 * @throws MetadataAccessException
	 *             if something is wrong
	 */
	private DataContainer checkIncomingMetadataObject(
			IMetadataObject metadataObject) throws MetadataAccessException {

		// Check for null argument
		if (metadataObject == null) {
			throw new MetadataAccessException(
					"Failed: incoming DataContainer was null");
		}

		// Try to cast the incoming object into the correct class
		DataContainer dataContainer = null;
		try {
			dataContainer = (DataContainer) metadataObject;
		} catch (ClassCastException cce) {
			throw new MetadataAccessException(
					"Could not cast the incoming object into a DataContainer");
		}
		return dataContainer;
	}

	private Criteria formulatePropertyCriteria(boolean countQuery, Long id,
			String name, boolean exactNameMatch, String dataContainerType,
			Date startDate, boolean boundedByStartDate, Date endDate,
			boolean boundedByEndDate, String uriString, boolean exactUriMatch,
			String mimeType, boolean exactMimeTypeMatch, String dodsUrlString,
			boolean exactDodsUrlStringMatch, Double geospatialLatMin,
			boolean boundedByLatMin, Double geospatialLatMax,
			boolean boundedByLatMax, Double geospatialLonMin,
			boolean boundedByLonMin, Double geospatialLonMax,
			boolean boundedByLonMax, Float geospatialDepthMin,
			boolean boundedByDepthMin, Float geospatialDepthMax,
			boolean boundedByDepthMax, String orderByProperty,
			String ascendingOrDescending) throws MetadataAccessException {
		// The Criteria to return
		Criteria criteria = getSession().createCriteria(DataContainer.class);
		// Make the return distinct
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		// Check for exceptional conditions on the query
		if ((dataContainerType != null)
				&& (!DataContainer.isValidDataContainerType(dataContainerType)))
			throw new MetadataAccessException(
					"The dataContainerType ("
							+ dataContainerType
							+ ") does not match a constant defined in the DataContainer class");
		if ((geospatialLatMin != null) && (geospatialLatMax != null))
			if (geospatialLatMax.doubleValue() < geospatialLatMin.doubleValue())
				throw new MetadataAccessException(
						"The maximum latitude specified was less than the minimum.");
		if ((geospatialLonMin != null) && (geospatialLonMax != null))
			if (geospatialLonMax.doubleValue() < geospatialLonMin.doubleValue())
				throw new MetadataAccessException(
						"The maximum longitude specified was less than the minimum.");
		if ((geospatialDepthMin != null) && (geospatialDepthMax != null))
			if (geospatialDepthMax.doubleValue() < geospatialDepthMin
					.doubleValue())
				throw new MetadataAccessException(
						"The depth maximum specified was less than the minimum.");
		if ((startDate != null) && (endDate != null)
				&& (endDate.before(startDate)))
			throw new MetadataAccessException("The end date specified ("
					+ endDate + ") is before the start date specified ("
					+ startDate + ")");

		if (id != null) {
			criteria.add(Restrictions.eq("id", id));
		} else {
			if ((name != null) && (!name.equals(""))) {
				if (exactNameMatch) {
					criteria.add(Restrictions.eq("name", name));
				} else {
					criteria.add(Restrictions.like("name", "%" + name + "%"));
				}
			}
			if (dataContainerType != null) {
				criteria.add(Restrictions.eq("dataContainerType",
						dataContainerType));
			}
			if (startDate != null) {
				criteria.add(Restrictions.gt("endDate", startDate));
				if (boundedByStartDate) {
					criteria.add(Restrictions.gt("startDate", startDate));
				}
			}
			if (endDate != null) {
				criteria.add(Restrictions.lt("startDate", endDate));
				if (boundedByEndDate) {
					criteria.add(Restrictions.lt("endDate", endDate));
				}
			}
			if (uriString != null) {
				if (exactUriMatch) {
					criteria.add(Restrictions.eq("uriString", uriString));
				} else {
					criteria.add(Restrictions.like("uriString", "%" + uriString
							+ "%"));
				}
			}
			if (mimeType != null) {
				if (exactMimeTypeMatch) {
					criteria.add(Restrictions.eq("mimeType", mimeType));
				} else {
					criteria.add(Restrictions.like("mimdType", "%" + mimeType
							+ "%"));
				}
			}
			if (dodsUrlString != null) {
				if (exactDodsUrlStringMatch) {
					criteria.add(Restrictions
							.eq("dodsUrlString", dodsUrlString));
				} else {
					criteria.add(Restrictions.like("dodsUrlString", "%"
							+ dodsUrlString + "%"));
				}
			}
			if (geospatialLatMin != null) {
				criteria.add(Restrictions.gt("maxLatitude", geospatialLatMin));
				if (boundedByLatMin) {
					criteria.add(Restrictions.ge("minLatitude",
							geospatialLatMin));
				}
			}

			if (geospatialLatMax != null) {
				criteria.add(Restrictions.lt("minLatitude", geospatialLatMax));
				if (boundedByLatMax) {
					criteria.add(Restrictions.le("maxLatitude",
							geospatialLatMax));
				}
			}

			if (geospatialLonMin != null) {
				criteria.add(Restrictions.gt("maxLongitude", geospatialLonMin));
				if (boundedByLonMin) {
					criteria.add(Restrictions.ge("minLongitude",
							geospatialLonMin));
				}
			}

			if (geospatialLonMax != null) {
				criteria.add(Restrictions.lt("minLongitude", geospatialLonMax));
				if (boundedByLonMax) {
					criteria.add(Restrictions.le("maxLongitude",
							geospatialLonMax));
				}
			}

			if (geospatialDepthMin != null) {
				criteria.add(Restrictions.gt("maxDepth", geospatialDepthMin));
				if (boundedByDepthMin) {
					criteria.add(Restrictions
							.ge("minDepth", geospatialDepthMin));
				}
			}

			if (geospatialDepthMax != null) {
				criteria.add(Restrictions.lt("minDepth", geospatialDepthMax));
				if (boundedByDepthMax) {
					criteria.add(Restrictions
							.le("maxDepth", geospatialDepthMax));
				}
			}

		}
		// Setup if a count query, if not add fetching and ordering
		if (countQuery) {
			criteria.setProjection(Projections.rowCount());
		} else {
			addOrderByCriteria(criteria, orderByProperty, ascendingOrDescending);
		}
		// Now return the Criteria
		return criteria;
	}

	/**
	 * @see MetadataDAO#initializeRelationships(IMetadataObject)
	 */
	// protected void initializeRelationships(IMetadataObject metadataObject)
	// throws MetadataAccessException {
	//
	// // If the object is null, just return
	// if (metadataObject == null)
	// return;
	//
	// // Convert to DataContainer
	// DataContainer dataContainer = this
	// .checkIncomingMetadataObject(metadataObject);
	//
	// // Now initalize the appropriate relationships
	// if (dataContainer.getHeaderDescription() != null) {
	// Hibernate.initialize(dataContainer.getHeaderDescription());
	// }
	// if (dataContainer.getRecordDescription() != null) {
	// Hibernate.initialize(dataContainer.getRecordDescription());
	// if (dataContainer.getRecordDescription().getRecordVariables() != null) {
	// Iterator rvIter = dataContainer.getRecordDescription()
	// .getRecordVariables().iterator();
	// while (rvIter.hasNext()) {
	// RecordVariable rv = (RecordVariable) rvIter.next();
	// Hibernate.initialize(rv);
	// if (rv.getStandardUnit() != null) {
	// Hibernate.initialize(rv.getStandardUnit());
	// }
	// if (rv.getStandardVariable() != null) {
	// Hibernate.initialize(rv.getStandardVariable());
	// }
	// if (rv.getStandardDomain() != null) {
	// Hibernate.initialize(rv.getStandardDomain());
	// }
	// if (rv.getStandardKeyword() != null) {
	// Hibernate.initialize(rv.getStandardKeyword());
	// }
	// if (rv.getStandardReferenceScale() != null) {
	// Hibernate.initialize(rv.getStandardReferenceScale());
	// }
	// }
	// }
	// }
	// if (dataContainer.getPerson() != null)
	// Hibernate.initialize(dataContainer.getPerson());
	// if (dataContainer.getDataContainerGroups() != null) {
	// Iterator dataContainerGroupsIterator = dataContainer
	// .getDataContainerGroups().iterator();
	// while (dataContainerGroupsIterator.hasNext()) {
	// Hibernate
	// .initialize((DataContainerGroup) dataContainerGroupsIterator
	// .next());
	// }
	// }
	// if (dataContainer.getResources() != null) {
	// Iterator resourceIterator = dataContainer.getResources().iterator();
	// while (resourceIterator.hasNext()) {
	// Hibernate.initialize((Resource) resourceIterator.next());
	// }
	// }
	// if (dataContainer.getKeywords() != null) {
	// Iterator keywordIterator = dataContainer.getKeywords().iterator();
	// while (keywordIterator.hasNext()) {
	// Hibernate.initialize((Keyword) keywordIterator.next());
	// }
	// }
	// if (dataContainer.getCreator() != null) {
	// Hibernate.initialize(dataContainer.getCreator());
	// }
	// if (dataContainer.getConsumers() != null) {
	// Iterator consumerIterator = dataContainer.getConsumers().iterator();
	// while (consumerIterator.hasNext()) {
	// Hibernate.initialize((DataProducer) consumerIterator.next());
	// }
	// }
	// }

	/**
	 * The Log4J Logger
	 */
	static Logger logger = Logger.getLogger(DataContainerDAO.class);
}
