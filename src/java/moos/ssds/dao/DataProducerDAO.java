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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.DataProducerGroup;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.Event;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.Keyword;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.Software;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class DataProducerDAO extends MetadataDAO {

	/**
	 * @see MetadataDAO#MetadataDAO(Class, Session)
	 */
	public DataProducerDAO(Session session) throws MetadataAccessException {
		super(DataProducer.class, session);
	}

	/**
	 * @see IMetadataDAO#findEquivalentPersistentObject(IMetadataObject,
	 *      boolean)
	 */
	public IMetadataObject findEquivalentPersistentObject(
			IMetadataObject metadataObject, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		logger.debug("findEquivalentPersistentObject called");
		// First convert the incoming object to a DataProducer
		DataProducer dataProducer = this
				.checkIncomingMetadataObject(metadataObject);

		// The DataProducer to return (if found)
		DataProducer dataProducerToReturn = null;

		// The id that will be used in the search
		Long idToSearchFor = dataProducer.getId();

		// If the ID is specified, do a look up for the object
		if ((idToSearchFor != null) && (idToSearchFor.longValue() > 0)) {
			// Now grab the Criteria query
			Criteria criteria = this.formulatePropertyCriteria(false,
					idToSearchFor, null, false, null, null, false, null, false,
					null, null, null, null, null, null, null, null, null,
					false, null, null);
			dataProducerToReturn = (DataProducer) criteria.uniqueResult();
		}

		// If the dataProducer was not found, search for the creator of one of
		// the incoming DataProducers outputs
		if (dataProducerToReturn == null) {
			// Try an grab an output
			Collection outputs = dataProducer.getOutputs();
			if ((outputs != null) && (outputs.size() > 0)) {
				// Loop over the outputs
				Iterator outputIter = outputs.iterator();
				while (outputIter.hasNext()) {
					DataContainer output = (DataContainer) outputIter.next();
					// Find the creator of the output from the DAO
					DataProducer tempCreator = this.findByOutput(output, null,
							null, false);
					// Check for equivalence
					if ((tempCreator != null)
							&& (tempCreator.equals(dataProducer))) {
						dataProducerToReturn = tempCreator;
						break;
					}
				}
			}
		}

		// Check to see if the graph is requested
		if (returnFullObjectGraph)
			this.initializeRelationships(dataProducerToReturn);

		// Return the result
		return dataProducerToReturn;
	}

	/**
	 * @see IMetadataDAO#findAllIDs()
	 */
	public Collection findAllIDs() throws MetadataAccessException {
		Collection dataProducerIDs = new ArrayList();

		// Create the query and run it
		try {
			Query query = getSession()
					.createQuery(
							"select distinct dataProducer.id from "
									+ "DataProducer dataProducer order by dataProducer.id");
			dataProducerIDs = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}
		return dataProducerIDs;
	}

	/**
	 * @see IMetadataDAO#countFindAllIDs()
	 */
	public int countFindAllIDs() throws MetadataAccessException {
		// The count
		int count = 0;
		try {
			Long longCount = (Long) getSession().createQuery(
					"select count(distinct dataProducer.id) from "
							+ "DataProducer dataProducer").uniqueResult();
			if (longCount != null)
				count = longCount.intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// Return the result
		return count;
	}

	/**
	 * TODO kgomes Document the rest of this. This method allows the caller to
	 * specify the properties that they want to try and find the
	 * <code>DataProducer</code> by. In general if the parameters are left null,
	 * they wil not be used in the query
	 * 
	 * @param name
	 * @param exactMatch
	 * @param dataProducerType
	 * @param startDate
	 * @param boundedByStartDate
	 * @param endDate
	 * @param boundedByEndDate
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialDepthMin
	 * @param geospatialDepthMax
	 * @param geospatialBenthicAltitudeMin
	 * @param geospatialBenthicAltitudeMax
	 * @param hostName
	 * @param exactHostNameMatch
	 * @param orderByProperty
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByProperties(String name, boolean exactMatch,
			String dataProducerType, Date startDate,
			boolean boundedByStartDate, Date endDate, boolean boundedByEndDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialDepthMin, Float geospatialDepthMax,
			Float geospatialBenthicAltitudeMin,
			Float geospatialBenthicAltitudeMax, String hostName,
			boolean exactHostNameMatch, String orderByProperty,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// The Collection to return
		Collection results = new ArrayList();

		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					name, exactMatch, dataProducerType, startDate,
					boundedByStartDate, endDate, boundedByEndDate,
					geospatialLatMin, geospatialLatMax, geospatialLonMin,
					geospatialLonMax, geospatialDepthMin, geospatialDepthMax,
					geospatialBenthicAltitudeMin, geospatialBenthicAltitudeMax,
					hostName, exactHostNameMatch, orderByProperty,
					ascendingOrDescending);
			results = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			this.initializeRelationships(results);
		}

		// Return the results
		return results;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param name
	 * @param exactMatch
	 * @param dataProducerType
	 * @param startDate
	 * @param boundedByStartDate
	 * @param endDate
	 * @param boundedByEndDate
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialDepthMin
	 * @param geospatialDepthMax
	 * @param geospatialBenthicAltitudeMin
	 * @param geospatialBenthicAltitudeMax
	 * @param hostName
	 * @param exactHostNameMatch
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindByProperties(String name, boolean exactMatch,
			String dataProducerType, Date startDate,
			boolean boundedByStartDate, Date endDate, boolean boundedByEndDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialDepthMin, Float geospatialDepthMax,
			Float geospatialBenthicAltitudeMin,
			Float geospatialBenthicAltitudeMax, String hostName,
			boolean exactHostNameMatch) throws MetadataAccessException {
		// The Collection to return
		int count = 0;

		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					name, exactMatch, dataProducerType, startDate,
					boundedByStartDate, endDate, boundedByEndDate,
					geospatialLatMin, geospatialLatMax, geospatialLonMin,
					geospatialLonMax, geospatialDepthMin, geospatialDepthMax,
					geospatialBenthicAltitudeMin, geospatialBenthicAltitudeMax,
					hostName, exactHostNameMatch, null, null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// Return the results
		return count;
	}

	/**
	 * This method returns all the <code>DataProducer</code>s that have a
	 * matching name. The name can be an exact match, or one that uses a LIKE
	 * criteria (depending on the <code>exactMatch</code> boolean)
	 * 
	 * @param name
	 *            is the name to search for
	 * @param exactMatch
	 *            determines whether or not the search should only find exact
	 *            matches (<code>true</code>) or LIKE matches (
	 *            <code>false</code>)
	 * @param orderByPropertyName
	 *            this is a string that can be used to try and specify the
	 *            property that the query should try to order the results by. It
	 *            must match a property name of <code>DataProducer</code>.
	 * @param returnFullObjectGraph
	 *            is a boolean that specifies if the caller wants the fully
	 *            instantianted object graph (relationships) returned, or just
	 *            the query object itself. If you want the full graph returned
	 *            specify <code>true</code>, otherwise leave it false. <b>NOTE:
	 *            By specifying true, you could be requesting a large object
	 *            tree which will slow things down, so use sparingly</b>
	 * @return a <code>Collection</code> of <code>DataProducer</code>s that
	 *         match the given criteria
	 * @throws MetadataAccessException
	 *             if something goes wrong with the query
	 */
	public Collection findByName(String name, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// The collection to be returned
		Collection results = new ArrayList();

		// Make sure argument is not null
		if ("".equals(name)) {
			return results;
		}

		// Construct and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					name, exactMatch, null, null, false, null, false, null,
					null, null, null, null, null, null, null, null, false,
					orderByPropertyName, ascendingOrDescending);
			results = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			this.initializeRelationships(results);
		}
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
		// The int to return
		int count = 0;

		// Make sure argument is not null
		logger.debug("countFindByName where name = " + name + " called.");
		if ((name == null) && (name.equals(""))) {
			return 0;
		}

		// Construct and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					name, exactMatch, null, null, false, null, false, null,
					null, null, null, null, null, null, null, null, false,
					null, null);
			count = ((Number) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// Return the count
		return count;
	}

	/**
	 * This method returns all <code>DataProducer</code>s that match the
	 * incoming dataProducerType and name (if specified).
	 * 
	 * @param dataProducerType
	 *            is the type of data producer to search for and should match
	 *            one of the constants defined in the <code>DataProducer</code>
	 *            class.
	 * @param name
	 *            is the name of the <code>DataProducer</code> to search for. If
	 *            this parameter is <code>null</code> the search will only be
	 *            done by the data producer type.
	 * @param exactMatch
	 *            is a boolean to indicate if the name search is to be an exact
	 *            match of the name
	 * @param orderByPropertyName
	 *            this is a string that can be used to try and specify the
	 *            property that the query should try to order the results by. It
	 *            must match a property name of <code>DataProducer</code>.
	 * @param returnFullObjectGraph
	 *            is a boolean that specifies if the caller wants the fully
	 *            instantianted object graph (relationships) returned, or just
	 *            the query object itself. If you want the full graph returned
	 *            specify <code>true</code>, otherwise leave it false. <b>NOTE:
	 *            By specifying true, you could be requesting a large object
	 *            tree which will slow things down, so use sparingly</b>
	 * @return a <code>Collection</code> of <code>DataProducer</code>s that have
	 *         the same type as the string given.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the search, or if the incoming
	 *             dataProducerType does not match a constant defined in the
	 *             <code>DataProducer</code> class.
	 */
	public Collection findByDataProducerTypeAndName(String dataProducerType,
			String name, boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// The results to return
		Collection results = new ArrayList();

		// Construct and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					name, exactMatch, dataProducerType, null, false, null,
					false, null, null, null, null, null, null, null, null,
					null, false, orderByPropertyName, ascendingOrDescending);
			results = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			this.initializeRelationships(results);
		}

		// Return the result
		return results;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param dataProducerType
	 * @param name
	 * @param exactMatch
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindByDataProducerTypeAndName(String dataProducerType,
			String name, boolean exactMatch) throws MetadataAccessException {
		// The count to return
		int count = 0;

		// Construct and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					name, exactMatch, dataProducerType, null, false, null,
					false, null, null, null, null, null, null, null, null,
					null, false, null, null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// Return the count
		return count;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param orderByPropertyName
	 * @param returnFullObjectGraph
	 *            is a boolean that specifies if the caller wants the fully
	 *            instantianted object graph (relationships) returned, or just
	 *            the query object itself. If you want the full graph returned
	 *            specify <code>true</code>, otherwise leave it false. <b>NOTE:
	 *            By specifying true, you could be requesting a large object
	 *            tree which will slow things down, so use sparingly</b>
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findParentlessDeployments(String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// The results to return
		Collection parentlessDeployments = new ArrayList();
		StringBuffer sqlStringBuffer = new StringBuffer();
		sqlStringBuffer.append("select distinct dataProducer from "
				+ "DataProducer dataProducer "
				+ "where dataProducer.dataProducerType = '"
				+ DataProducer.TYPE_DEPLOYMENT + "'"
				+ " AND dataProducer.parentDataProducer is null ");
		if (this.checkIfPropertyOK(orderByPropertyName))
			sqlStringBuffer.append(getOrderByPropertyNameSQLClause(
					orderByPropertyName, ascendingOrDescending));

		try {
			parentlessDeployments = this.getSession().createQuery(
					sqlStringBuffer.toString()).list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			this.initializeRelationships(parentlessDeployments);
		}
		return parentlessDeployments;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindParentlessDeployments() throws MetadataAccessException {
		int count = 0;
		String sqlString = "select count(distinct dataProducer) from "
				+ "DataProducer dataProducer "
				+ "where dataProducer.dataProducerType = '"
				+ DataProducer.TYPE_DEPLOYMENT + "'"
				+ " AND dataProducer.parentDataProducer is null";

		try {
			count = ((Long) this.getSession().createQuery(sqlString)
					.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}
		return count;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param orderByPropertyName
	 * @param ascendingOrDescending
	 * @param returnFullObjectGraph
	 *            is a boolean that specifies if the caller wants the fully
	 *            instantianted object graph (relationships) returned, or just
	 *            the query object itself. If you want the full graph returned
	 *            specify <code>true</code>, otherwise leave it false. <b>NOTE:
	 *            By specifying true, you could be requesting a large object
	 *            tree which will slow things down, so use sparingly</b>
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findParentlessDataProducers(String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// The results to return
		Collection parentlessDataProducers = new ArrayList();
		StringBuffer sqlStringBuffer = new StringBuffer();
		sqlStringBuffer.append("select distinct dataProducer from "
				+ "DataProducer dataProducer "
				+ "where dataProducer.parentDataProducer is null ");
		if (this.checkIfPropertyOK(orderByPropertyName))
			sqlStringBuffer.append(getOrderByPropertyNameSQLClause(
					orderByPropertyName, ascendingOrDescending));

		try {
			parentlessDataProducers = this.getSession().createQuery(
					sqlStringBuffer.toString()).list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			this.initializeRelationships(parentlessDataProducers);
		}
		return parentlessDataProducers;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindParentlessDataProducers()
			throws MetadataAccessException {
		int count = 0;
		String sqlString = "select count(distinct dataProducer) from "
				+ "DataProducer dataProducer "
				+ "where dataProducer.parentDataProducer is null";

		try {
			count = ((Long) this.getSession().createQuery(sqlString)
					.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}
		return count;
	}

	/**
	 * This methods allows you to query for <code>DataProducer</code>s by a date
	 * range and also by a name (if you choose).
	 * 
	 * @param startDate
	 *            is the <code>Date</code> that is the start of the date range
	 *            to search for. If it is null, the start of the date range will
	 *            be completely open (note that if both start and end dates are
	 *            null, an exception will be thrown).
	 * @param boundedByStartDate
	 *            is a <code>boolean</code> to indicate if the
	 *            <code>DataProducer</code>'s startDate must be after the start
	 *            date specified
	 * @param endDate
	 *            is the <code>Date</code> that is the end of the date range to
	 *            search for. If it is null, the end date will be infinity (note
	 *            that if both start and end dates are null, an exception will
	 *            be thrown)
	 * @param boundedByEndDate
	 *            is a <code>boolean</code> to indicate if the
	 *            <code>DataProducer</code>'s end date must be before the end
	 *            date specified.
	 * @param name
	 *            is the name of the <code>DataProducer</code> that you are
	 *            searching for. If this is null or an empty string, it will not
	 *            be used in the search
	 * @param exactMatch
	 *            this is a <code>boolean</code> that indicates whether or not
	 *            you want the name search to look for exact matches of whether
	 *            or not to search for &quot;like&quot; instances
	 * @param orderByPropertyName
	 *            this is a string that can be used to try and specify the
	 *            property that the query should try to order the results by. It
	 *            must match a property name of <code>DataProducer</code>.
	 * @param returnFullObjectGraph
	 *            is a boolean that specifies if the caller wants the fully
	 *            instantianted object graph (relationships) returned, or just
	 *            the query object itself. If you want the full graph returned
	 *            specify <code>true</code>, otherwise leave it false. <b>NOTE:
	 *            By specifying true, you could be requesting a large object
	 *            tree which will slow things down, so use sparingly</b>
	 * @return a <code>Collection</code> of <code>DataProducer</code>s that
	 *         match the given criteria
	 * @throws MetadataAccessException
	 *             if something is not right with the query (like start date is
	 *             after end date, both start and end dates are null, etc.) or
	 *             if something goes wrong with the query.
	 */
	public Collection findByDateRangeAndName(Date startDate,
			boolean boundedByStartDate, Date endDate, boolean boundedByEndDate,
			String name, boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// The results to return
		Collection results = new ArrayList();

		// Check for exceptional conditions on the query
		if ((startDate == null) && (endDate == null))
			return results;

		// Construct and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					name, exactMatch, null, startDate, boundedByStartDate,
					endDate, boundedByEndDate, null, null, null, null, null,
					null, null, null, null, false, orderByPropertyName,
					ascendingOrDescending);
			results = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// Check for object graph return
		if (returnFullObjectGraph)
			this.initializeRelationships(results);

		// Return the results
		return results;
	}

	/**
	 * This method makes the same call as the findByDateRangeAndName, but
	 * returns the count, not the objects themselves.
	 * 
	 * @see DataProducerDAO#findByDateRangeAndName(Date, boolean, Date, boolean,
	 *      String, boolean, boolean)
	 */
	public int countFindByDateRangeAndName(Date startDate,
			boolean boundedByStartDate, Date endDate, boolean boundedByEndDate,
			String name, boolean exactMatch) throws MetadataAccessException {

		// The count to return
		int count = 0;

		// Check for exceptional conditions on the query
		if ((startDate == null) && (endDate == null))
			return 0;

		// Construct and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					name, exactMatch, null, startDate, boundedByStartDate,
					endDate, boundedByEndDate, null, null, null, null, null,
					null, null, null, null, false, null, null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// Return the results
		return count;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialVerticalMin
	 * @param geospatialVerticalMax
	 * @param orderByPropertyName
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByGeospatialCube(Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// The collection to return
		Collection results = new ArrayList();
		// Check for exceptional conditions on the query
		if ((geospatialLatMin == null) && (geospatialLatMax == null)
				&& (geospatialLonMin == null) && (geospatialLonMax == null)
				&& (geospatialVerticalMin == null)
				&& (geospatialVerticalMax == null))
			return results;

		// Construct and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					null, false, null, null, false, null, false,
					geospatialLatMin, geospatialLatMax, geospatialLonMin,
					geospatialLonMax, geospatialVerticalMin,
					geospatialVerticalMax, null, null, null, false,
					orderByPropertyName, ascendingOrDescending);
			results = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// Check for object graph return
		if (returnFullObjectGraph)
			this.initializeRelationships(results);

		// Return the results
		return results;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialVerticalMin
	 * @param geospatialVerticalMax
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindByGeospatialCube(Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax) throws MetadataAccessException {
		// The count to return
		int count = 0;

		// Check for exceptional conditions on the query
		if ((geospatialLatMin == null) && (geospatialLatMax == null)
				&& (geospatialLonMin == null) && (geospatialLonMax == null)
				&& (geospatialVerticalMin == null)
				&& (geospatialVerticalMax == null))
			return 0;
		// Construct and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					null, false, null, null, false, null, false,
					geospatialLatMin, geospatialLatMax, geospatialLonMin,
					geospatialLonMax, geospatialVerticalMin,
					geospatialVerticalMax, null, null, null, false, null, null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// Return the count
		return count;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param startDate
	 * @param boundedByStartDate
	 * @param endDate
	 * @param boundedByEndDate
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialVerticalMin
	 * @param geospatialVerticalMax
	 * @param orderByPropertyName
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByTimeAndGeospatialCube(Date startDate,
			boolean boundedByStartDate, Date endDate, boolean boundedByEndDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// The results to return
		Collection results = new ArrayList();
		// Check for exceptional conditions on the query
		if ((geospatialLatMin == null) && (geospatialLatMax == null)
				&& (geospatialLonMin == null) && (geospatialLonMax == null)
				&& (geospatialVerticalMin == null)
				&& (geospatialVerticalMax == null))
			return results;

		// Construct and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					null, false, null, startDate, boundedByStartDate, endDate,
					boundedByEndDate, geospatialLatMin, geospatialLatMax,
					geospatialLonMin, geospatialLonMax, geospatialVerticalMin,
					geospatialVerticalMax, null, null, null, false,
					orderByPropertyName, ascendingOrDescending);
			results = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// Check for object graph return
		if (returnFullObjectGraph)
			this.initializeRelationships(results);

		// Return the results
		return results;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param startDate
	 * @param boundedByStartDate
	 * @param endDate
	 * @param boundedByEndDate
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialVerticalMin
	 * @param geospatialVerticalMax
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindByTimeAndGeospatialCube(Date startDate,
			boolean boundedByStartDate, Date endDate, boolean boundedByEndDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax)
			throws MetadataAccessException {
		// The count to return
		int count = 0;
		// Check for exceptional conditions on the query
		if ((geospatialLatMin == null) && (geospatialLatMax == null)
				&& (geospatialLonMin == null) && (geospatialLonMax == null)
				&& (geospatialVerticalMin == null)
				&& (geospatialVerticalMax == null))
			return 0;

		// Construct and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					null, false, null, startDate, boundedByStartDate, endDate,
					boundedByEndDate, geospatialLatMin, geospatialLatMax,
					geospatialLonMin, geospatialLonMax, geospatialVerticalMin,
					geospatialVerticalMax, null, null, null, false, null, null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		// Return the count
		return count;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param name
	 * @param exactMatch
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialVerticalMin
	 * @param geospatialVerticalMax
	 * @param orderByPropertyName
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByNameAndGeospatialCube(String name,
			boolean exactMatch, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// The results to return
		Collection results = new ArrayList();
		// Check for exceptional conditions on the query
		if ((geospatialLatMin == null) && (geospatialLatMax == null)
				&& (geospatialLonMin == null) && (geospatialLonMax == null)
				&& (geospatialVerticalMin == null)
				&& (geospatialVerticalMax == null))
			return results;

		// Construct and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					name, exactMatch, null, null, false, null, false,
					geospatialLatMin, geospatialLatMax, geospatialLonMin,
					geospatialLonMax, geospatialVerticalMin,
					geospatialVerticalMax, null, null, null, false,
					orderByPropertyName, ascendingOrDescending);
			results = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// Check for object graph return
		if (returnFullObjectGraph)
			this.initializeRelationships(results);

		// Return the results
		return results;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param name
	 * @param exactMatch
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialVerticalMin
	 * @param geospatialVerticalMax
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindByNameAndGeospatialCube(String name,
			boolean exactMatch, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax) throws MetadataAccessException {
		// The count to return
		int count = 0;
		// Check for exceptional conditions on the query
		if ((geospatialLatMin == null) && (geospatialLatMax == null)
				&& (geospatialLonMin == null) && (geospatialLonMax == null)
				&& (geospatialVerticalMin == null)
				&& (geospatialVerticalMax == null))
			return 0;

		// Construct and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					name, exactMatch, null, null, false, null, false,
					geospatialLatMin, geospatialLatMax, geospatialLonMin,
					geospatialLonMax, geospatialVerticalMin,
					geospatialVerticalMax, null, null, null, false, null, null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		// Return the count
		return count;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param name
	 * @param exactMatch
	 * @param startDate
	 * @param boundedByStartDate
	 * @param endDate
	 * @param boundedByEndDate
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialVerticalMin
	 * @param geospatialVerticalMax
	 * @param orderByPropertyName
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByNameAndTimeAndGeospatialCube(String name,
			boolean exactMatch, Date startDate, boolean boundedByStartDate,
			Date endDate, boolean boundedByEndDate, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// The results to return
		Collection results = new ArrayList();
		// Check for exceptional conditions on the query
		if ((geospatialLatMin == null) && (geospatialLatMax == null)
				&& (geospatialLonMin == null) && (geospatialLonMax == null)
				&& (geospatialVerticalMin == null)
				&& (geospatialVerticalMax == null))
			return results;

		// Construct and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					name, exactMatch, null, startDate, boundedByStartDate,
					endDate, boundedByEndDate, geospatialLatMin,
					geospatialLatMax, geospatialLonMin, geospatialLonMax,
					geospatialVerticalMin, geospatialVerticalMax, null, null,
					null, false, orderByPropertyName, ascendingOrDescending);
			results = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// Check for object graph return
		if (returnFullObjectGraph)
			this.initializeRelationships(results);

		// Return the results
		return results;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param name
	 * @param exactMatch
	 * @param startDate
	 * @param boundedByStartDate
	 * @param endDate
	 * @param boundedByEndDate
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialVerticalMin
	 * @param geospatialVerticalMax
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindByNameAndTimeAndGeospatialCube(String name,
			boolean exactMatch, Date startDate, boolean boundedByStartDate,
			Date endDate, boolean boundedByEndDate, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax) throws MetadataAccessException {
		// The count to return
		int count = 0;

		// Check for exceptional conditions on the query
		if ((geospatialLatMin == null) && (geospatialLatMax == null)
				&& (geospatialLonMin == null) && (geospatialLonMax == null)
				&& (geospatialVerticalMin == null)
				&& (geospatialVerticalMax == null))
			return 0;

		// Construct and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					name, exactMatch, null, startDate, boundedByStartDate,
					endDate, boundedByEndDate, geospatialLatMin,
					geospatialLatMax, geospatialLonMin, geospatialLonMax,
					geospatialVerticalMin, geospatialVerticalMax, null, null,
					null, false, null, null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// Return the results
		return count;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param hostName
	 * @param exactHostNameMatch
	 * @param orderByPropertyName
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByHostName(String hostName,
			boolean exactHostNameMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// The results to return
		Collection results = new ArrayList();
		// Make sure argument is not null
		if ((hostName == null) && (hostName.equals(""))) {
			return results;
		}

		// Construct and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					null, false, null, null, false, null, false, null, null,
					null, null, null, null, null, null, hostName,
					exactHostNameMatch, orderByPropertyName,
					ascendingOrDescending);
			results = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// Check for object graph return
		if (returnFullObjectGraph)
			this.initializeRelationships(results);

		// Return the results
		return results;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param hostName
	 * @param exactHostNameMatch
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindByHostName(String hostName, boolean exactHostNameMatch)
			throws MetadataAccessException {
		// The count to return
		int count = 0;

		// Make sure argument is not null
		if ((hostName == null) && (hostName.equals(""))) {
			return 0;
		}

		// Construct and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					null, false, null, null, false, null, false, null, null,
					null, null, null, null, null, null, hostName,
					exactHostNameMatch, null, null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// Return the results
		return count;
	}

	/**
	 * This method looks up all <code>DataProducer</code>s who have the given
	 * person associated with them directly.
	 * 
	 * @param person
	 *            is the <code>Person</code> to search for.
	 * @return the <code>Collection</code> of <code>DataProducer</code>s
	 * @throws MetadataAccessException
	 */
	public Collection findByPerson(Person person, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// First make sure the person exists
		PersonDAO personDAO = new PersonDAO(getSession());

		Person persistentPerson = null;
		persistentPerson = (Person) personDAO.findEquivalentPersistentObject(
				person, false);

		if (persistentPerson == null)
			throw new MetadataAccessException(
					"A matching person could not be found in the system");

		// The collection to return
		Collection dataProducersToReturn = new ArrayList();

		// Create the criteria
		try {
			Criteria criteria = getSession().createCriteria(DataProducer.class);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			criteria.add(Restrictions.eq("person", persistentPerson));
			addOrderByCriteria(criteria, orderByPropertyName,
					ascendingOrDescending);
			dataProducersToReturn = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			this.initializeRelationships(dataProducersToReturn);
		}

		return dataProducersToReturn;
	}

	/**
	 * This method returns a count of all <code>DataProducer</code>s who have
	 * the given person associated with them directly.
	 * 
	 * @param person
	 *            is the <code>Person</code> to search for.
	 * @return a count of how many match that criteria
	 * @throws MetadataAccessException
	 *             for many reasons
	 */
	public int countFindByPerson(Person person) throws MetadataAccessException {

		// First make sure the person exists
		PersonDAO personDAO = new PersonDAO(getSession());

		Person persistentPerson = null;
		persistentPerson = (Person) personDAO.findEquivalentPersistentObject(
				person, false);

		if (persistentPerson == null)
			throw new MetadataAccessException(
					"A matching person could not be found in the system");

		// The count to return
		int count = 0;

		// Create the criteria
		try {
			Criteria criteria = getSession().createCriteria(DataProducer.class);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			criteria.add(Restrictions.eq("person", persistentPerson));
			criteria.setProjection(Projections.rowCount());
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		return count;
	}

	/**
	 * This method returns a <code>Collection</code> of all the
	 * <code>DataProducer</code>s associated (directly) with the given device.
	 * 
	 * @param device
	 *            is the <code>Device</code> used to search for
	 * @return the <code>Collection</code> of <code>DataProducer</code>s that
	 *         are directly associated with that device
	 * @throws MetadataAccessException
	 */
	public Collection findByDevice(Device device, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// First make sure the device exists
		DeviceDAO deviceDAO = new DeviceDAO(getSession());

		Device persistentDevice = null;
		persistentDevice = (Device) deviceDAO.findEquivalentPersistentObject(
				device, false);

		if (persistentDevice == null)
			throw new MetadataAccessException(
					"A matching device could not be found in the system");

		// The collection to return
		Collection dataProducersToReturn = new ArrayList();

		// Create the criteria
		try {
			Criteria criteria = getSession().createCriteria(DataProducer.class);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			criteria.add(Restrictions.eq("device", persistentDevice));
			addOrderByCriteria(criteria, orderByPropertyName,
					ascendingOrDescending);
			dataProducersToReturn = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			this.initializeRelationships(dataProducersToReturn);
		}

		return dataProducersToReturn;
	}

	/**
	 * @param deviceID
	 * @param orderByPropertyName
	 * @param ascendingOrDescending
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByDeviceId(Long deviceID, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		if (deviceID == null) {
			return new ArrayList();
		}
		// The device to query for
		Device device = new Device();
		device.setId(deviceID);
		return this.findByDevice(device, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/**
	 * This method returns all deployments of a certain type of device.
	 * 
	 * @param deviceTypeName
	 * @param exactMatch
	 * @param orderByPropertyName
	 * @param ascendingOrDescending
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByDeviceTypeName(String deviceTypeName,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// The collection to return
		Collection collectionToReturn = new ArrayList();

		// Create the query string
		StringBuffer sqlStringBuffer = new StringBuffer();
		sqlStringBuffer
				.append("select distinct dataProducer from "
						+ "DataProducer dataProducer where dataProducer.dataProducerType = '"
						+ DataProducer.TYPE_DEPLOYMENT
						+ "' AND dataProducer.device.deviceType.name ");
		if (exactMatch) {
			sqlStringBuffer.append(" = '" + deviceTypeName + "'");
		} else {
			sqlStringBuffer.append(" LIKE '%" + deviceTypeName + "%'");
		}
		sqlStringBuffer.append(this.getOrderByPropertyNameSQLClause(
				orderByPropertyName, ascendingOrDescending));

		try {
			collectionToReturn = this.getSession().createQuery(
					sqlStringBuffer.toString()).list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			this.initializeRelationships(collectionToReturn);
		}

		// Now return the results (if any)
		return collectionToReturn;
	}

	/**
	 * TODO kgomes implement and document this
	 * 
	 * @param device
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindByDevice(Device device) throws MetadataAccessException {
		return -1;
	}

	/**
	 * TODO kgomes implement and document this
	 * 
	 * @param deviceID
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindByDevice(Long deviceID) throws MetadataAccessException {
		return -1;
	}

	/**
	 * This method finds all the deployments of a <code>Device</code> that fall
	 * within a certain time window. This is usually done when searching for
	 * data from that device.
	 * 
	 * @param device
	 * @param startDate
	 * @param endDate
	 * @param orderByPropertyName
	 * @param ascendingOrDescending
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByDeviceAndTimeWindow(Device device, Date startDate,
			Date endDate, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// The collection to return
		Collection dataProducersToReturn = new ArrayList();

		// First make sure the device exists
		DeviceDAO deviceDAO = new DeviceDAO(getSession());

		Device persistentDevice = null;
		persistentDevice = (Device) deviceDAO.findEquivalentPersistentObject(
				device, false);

		if (persistentDevice == null)
			return dataProducersToReturn;

		// Create the criteria
		try {
			Criteria criteria = getSession().createCriteria(DataProducer.class);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			criteria.add(Restrictions.eq("device", persistentDevice));
			criteria.add(Restrictions.eq("dataProducerType",
					DataProducer.TYPE_DEPLOYMENT));
			// Add the time criteria
			if (startDate != null) {
				criteria.add(Restrictions.or(Restrictions.gt("endDate",
						startDate), Restrictions.isNull("endDate")));
			}
			if (endDate != null) {
				criteria.add(Restrictions.or(Restrictions.lt("startDate",
						endDate), Restrictions.isNull("startDate")));
			}
			addOrderByCriteria(criteria, orderByPropertyName,
					ascendingOrDescending);
			dataProducersToReturn = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			this.initializeRelationships(dataProducersToReturn);
		}

		return dataProducersToReturn;
	}

	/**
	 * This method returns all the <code>DataProducer</code> that have no end
	 * date, no parent, and are of type Deployment.
	 */
	public Collection findCurrentParentlessDeployments(
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// The collection to return
		Collection dataProducersToReturn = new ArrayList();

		// Create the criteria
		try {
			Criteria criteria = getSession().createCriteria(DataProducer.class);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			criteria.add(Restrictions.isNull("endDate"));
			criteria.add(Restrictions.isNull("parentDataProducer"));
			criteria.add(Restrictions.eq("dataProducerType",
					DataProducer.TYPE_DEPLOYMENT));
			addOrderByCriteria(criteria, orderByPropertyName,
					ascendingOrDescending);
			dataProducersToReturn = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			this.initializeRelationships(dataProducersToReturn);
		}

		// Now return it
		return dataProducersToReturn;
	}

	/**
	 * This method returns all the <code>DataProducer</code> that have no
	 * parent, are of type Deployment, and match the name criteria given
	 */
	public Collection findParentlessDeploymentsByName(String name,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// The collection to return
		Collection dataProducersToReturn = new ArrayList();

		// Create the criteria
		try {
			Criteria criteria = getSession().createCriteria(DataProducer.class);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			criteria.add(Restrictions.isNull("parentDataProducer"));
			criteria.add(Restrictions.eq("dataProducerType",
					DataProducer.TYPE_DEPLOYMENT));
			// If exact match
			if (exactMatch) {
				criteria.add(Restrictions.eq("name", name));
			} else {
				criteria.add(Restrictions.like("name", "%" + name + "%"));
			}
			addOrderByCriteria(criteria, orderByPropertyName,
					ascendingOrDescending);
			dataProducersToReturn = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			this.initializeRelationships(dataProducersToReturn);
		}

		// Now return it
		return dataProducersToReturn;
	}

	/**
	 * This method returns all open deployments (anything without an end date)
	 * 
	 * @param orderByPropertyName
	 * @param ascendingOrDescending
	 * @param returnFullObjectGraph
	 * @return
	 */
	public Collection findCurrentDeployments(String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// The collection to return
		Collection dataProducersToReturn = new ArrayList();

		// Create the criteria
		Criteria criteria = getSession().createCriteria(DataProducer.class);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		criteria.add(Restrictions.eq("dataProducerType",
				DataProducer.TYPE_DEPLOYMENT));
		criteria.add(Restrictions.isNull("endDate"));
		addOrderByCriteria(criteria, orderByPropertyName, ascendingOrDescending);
		dataProducersToReturn = criteria.list();

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			this.initializeRelationships(dataProducersToReturn);
		}

		return dataProducersToReturn;

	}

	/**
	 * This method returns all the <code>DataProducer</code> that are of type
	 * Deployment, that are associated directly with the given
	 * <code>Device</code> and have no end time (endDate is null).
	 * 
	 * @param device
	 * @param orderByPropertyName
	 * @param ascendingOrDescending
	 * @param returnFullObjectGraph
	 * @return
	 */
	public Collection findCurrentDeploymentsOfDevice(Device device,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// First make sure the device exists
		DeviceDAO deviceDAO = new DeviceDAO(getSession());

		Device persistentDevice = null;
		persistentDevice = (Device) deviceDAO.findEquivalentPersistentObject(
				device, false);

		if (persistentDevice == null)
			throw new MetadataAccessException(
					"A matching device could not be found in the system");

		// The collection to return
		Collection dataProducersToReturn = new ArrayList();

		// Create the criteria
		try {
			Criteria criteria = getSession().createCriteria(DataProducer.class);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			criteria.add(Restrictions.eq("device", persistentDevice));
			criteria.add(Restrictions.eq("dataProducerType",
					DataProducer.TYPE_DEPLOYMENT));
			criteria.add(Restrictions.isNull("endDate"));
			addOrderByCriteria(criteria, orderByPropertyName,
					ascendingOrDescending);
			dataProducersToReturn = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			this.initializeRelationships(dataProducersToReturn);
		}

		return dataProducersToReturn;
	}

	/**
	 * This method returns all the <code>DataProducer</code> that are of type
	 * Deployment, that are associated directly with the given
	 * <code>Device</code> and have no end time (endDate is null).
	 * 
	 * @param device
	 * @param orderByPropertyName
	 * @param ascendingOrDescending
	 * @param returnFullObjectGraph
	 * @return
	 */
	public Collection findCurrentDeploymentsOfDeviceId(Long deviceID,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		if (deviceID == null) {
			return new ArrayList();
		}
		// Create an empty device and assign the id
		Device device = new Device();
		device.setId(deviceID);

		// Call the other method
		return this.findCurrentDeploymentsOfDevice(device, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/**
	 * This method returns all the <code>DataProducer</code> that are of type
	 * Deployment, that have a specified role (e.g. string "platform" aka
	 * DataProducer.ROLE_PLATFORM) and have no end time (endDate is null).
	 * 
	 * @param role
	 * @param orderByPropertyName
	 * @param ascendingOrDescending
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataException
	 * @throws MetadataAccessException
	 */
	public Collection findCurrentDeploymentsByRole(String role,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException,
			MetadataException {

		if (!DataProducer.isValidRole(role)) {
			throw new MetadataException("The role specified (" + role
					+ ") does not match a constant defined in this class");
		}

		// The collection to return
		Collection dataProducersToReturn = new ArrayList();

		// Create the criteria
		try {
			Criteria criteria = getSession().createCriteria(DataProducer.class);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			criteria.add(Restrictions.eq("role", role));
			criteria.add(Restrictions.eq("dataProducerType",
					DataProducer.TYPE_DEPLOYMENT));
			criteria.add(Restrictions.isNull("endDate"));
			addOrderByCriteria(criteria, orderByPropertyName,
					ascendingOrDescending);
			dataProducersToReturn = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			this.initializeRelationships(dataProducersToReturn);
		}

		return dataProducersToReturn;
	}

	/**
	 * This method returns all the <code>DataProducer</code> that are of type
	 * Deployment, that have a specified role (e.g. string "platform" aka
	 * DataProducer.ROLE_PLATFORM) and have no end time (endDate is null).
	 * 
	 * @param role
	 * @param orderByPropertyName
	 * @param ascendingOrDescending
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataException
	 * @throws MetadataAccessException
	 */
	public Collection findCurrentDeploymentsByRoleAndName(String role,
			String name, boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException, MetadataException {

		if (!DataProducer.isValidRole(role)) {
			throw new MetadataException("The role specified (" + role
					+ ") does not match a constant defined in this class");
		}

		// The collection to return
		Collection dataProducersToReturn = new ArrayList();

		// Create the criteria
		try {
			Criteria criteria = getSession().createCriteria(DataProducer.class);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			criteria.add(Restrictions.eq("role", role));
			criteria.add(Restrictions.eq("dataProducerType",
					DataProducer.TYPE_DEPLOYMENT));
			criteria.add(Restrictions.isNull("endDate"));
			if ((name != null) && (!name.equals(""))) {
				if (exactMatch) {
					criteria.add(Restrictions.eq("name", name));
				} else {
					criteria.add(Restrictions.like("name", "%" + name + "%"));
				}
			}
			addOrderByCriteria(criteria, orderByPropertyName,
					ascendingOrDescending);
			dataProducersToReturn = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			this.initializeRelationships(dataProducersToReturn);
		}

		return dataProducersToReturn;
	}

	/**
	 * TODO kgomes implement and document this
	 * 
	 * @param software
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findBySoftware(Software software,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// First make sure the device exists
		SoftwareDAO softwareDAO = new SoftwareDAO(getSession());

		Software persistentSoftware = null;
		persistentSoftware = (Software) softwareDAO
				.findEquivalentPersistentObject(software, false);

		if (persistentSoftware == null)
			throw new MetadataAccessException(
					"A matching software could not be found in the system");

		// The collection to return
		Collection dataProducersToReturn = null;

		// Create the criteria
		try {
			Criteria criteria = getSession().createCriteria(DataProducer.class);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			criteria.add(Restrictions.eq("software", persistentSoftware));
			addOrderByCriteria(criteria, orderByPropertyName,
					ascendingOrDescending);
			dataProducersToReturn = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			this.initializeRelationships(dataProducersToReturn);
		}

		return dataProducersToReturn;
	}

	/**
	 * TODO kgomes implement and document this
	 * 
	 * @param software
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindBySoftware(Software software)
			throws MetadataAccessException {
		return -1;
	}

	/**
	 * This methods finds the parent <code>DataProducer</code> (if one exists)
	 * for the given <code>DataProducer</code>
	 * 
	 * @param dataProducer
	 *            the <code>DataProducer</code> whose parent we are interested
	 *            in
	 * @param returnFullObjectGraph
	 *            TODO kgomes document this
	 * @return the <code>DataProducer</code> who is the parent of the given
	 *         <code>DataProducer</code>. This will return null if not parent is
	 *         found
	 * @throws MetadataAccessException
	 *             if something goes wrong
	 */
	public DataProducer findParentDataProducer(DataProducer dataProducer,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// First find the id of the parent
		DataProducer childDataProducer = (DataProducer) this
				.findEquivalentPersistentObject(dataProducer, false);

		// If the ID is null return an empty collection
		if (childDataProducer == null) {
			return null;
		} else {
			DataProducer parentDataProducer = childDataProducer
					.getParentDataProducer();
			if (parentDataProducer != null) {
				// This is a bit of a hack, but I need to manually intialize the
				if (parentDataProducer.getOutputs() != null) {
					Iterator outputIter = parentDataProducer.getOutputs()
							.iterator();
					while (outputIter.hasNext()) {
						Hibernate.initialize((DataContainer) outputIter.next());
					}
				}
				if (returnFullObjectGraph) {
					this.initializeRelationships(parentDataProducer);
				}
			}
			return parentDataProducer;
		}
	}

	/**
	 * This method finds the closest latitude up the parent chain for a given
	 * <code>DataProducer</code>.
	 * 
	 * @param dataProducer
	 *            the <code>DataProducer</code> whose ancestor chain we are
	 *            interested in searching
	 * @return the <code>Double</code> that represents the first latitude that
	 *         was found up the ancestor chain. It will return null if nothing
	 *         was found.
	 * @throws MetadataAccessException
	 *             if something goes wrong
	 */
	public Double findClosestParentDataProducerLatitude(
			DataProducer dataProducer) throws MetadataAccessException {

		// The latitude to return
		Double latitudeToReturn = null;

		// Find the peristent equivalent of the incoming DataProducer
		DataProducer childDataProducer = (DataProducer) this
				.findEquivalentPersistentObject(dataProducer, false);

		// Make sure the equivalent was found
		if (childDataProducer != null) {
			// Check to see if the parent exists
			if (childDataProducer.getParentDataProducer() != null) {
				// Check to see if the latitude exists
				if (childDataProducer.getParentDataProducer()
						.getNominalLatitude() != null) {
					latitudeToReturn = childDataProducer
							.getParentDataProducer().getNominalLatitude();
				} else {
					// Try to go up the chain again
					latitudeToReturn = findClosestParentDataProducerLatitude(childDataProducer
							.getParentDataProducer());
				}
			}
		}

		// Now return it
		return latitudeToReturn;
	}

	/**
	 * This method returns a <code>Collection</code> of
	 * <code>DataProducer</code>s that are direct children of the given
	 * <code>DataProducer</code>. TODO kgomes add an orderByProperty and
	 * ascending/descending parameters to the parameter list
	 * 
	 * @param dataProducer
	 *            the parent <code>DataProducer</code>
	 * @param orderByPropertyName
	 *            this is a string that can be used to try and specify the
	 *            property that the query should try to order the results by. It
	 *            must match a property name of <code>DataProducer</code>.
	 * @param returnFullObjectGraphs
	 *            If true then return populated object graphs
	 * @return the <code>Collection</code> of <code>DataProducer</code>s that
	 *         are direct children of the given parent
	 * @throws MetadataAccessException
	 *             if something goes wrong
	 */
	public Collection findChildDataProducers(DataProducer dataProducer,
			boolean returnFullObjectGraphs) throws MetadataAccessException {
		// The collection to return
		Collection results = new ArrayList();

		// Check for null first
		if (dataProducer == null) {
			return results;
		}

		// If the incoming parent does not have an ID find it
		Long parentID = dataProducer.getId();
		if ((parentID == null) || (parentID.longValue() <= 0)) {
			parentID = this.findId(dataProducer);
		}
		if (parentID == null) {
			return results;
		}

		// Now create the query
		StringBuffer sqlStringBuffer = new StringBuffer();
		sqlStringBuffer.append("select distinct dataProducer from "
				+ "DataProducer dataProducer where");
		sqlStringBuffer.append(" dataProducer.parentDataProducer.id = '"
				+ parentID.toString() + "'");

		try {
			results = this.getSession().createQuery(sqlStringBuffer.toString())
					.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Fill out graphs is asked for
		if (returnFullObjectGraphs)
			initializeRelationships(results);

		return results;
	}

	/**
	 * Return a count of <code>DataProducer</code>s that are direct children of
	 * the given <code>DataProducer</code>.
	 * 
	 * @param dataProducer
	 *            the parent <code>DataProducer</code>
	 * @return the size of the collection of child DataProducers
	 * @throws MetadataAccessException
	 */
	public int countFindChildDataProducers(DataProducer dataProducer)
			throws MetadataAccessException {

		// The count to return
		int countToReturn = 0;

		// Check for null first
		if (dataProducer == null) {
			return 0;
		}

		// If the incoming parent does not have an ID find it
		Long parentID = dataProducer.getId();
		if ((parentID == null) || (parentID.longValue() <= 0)) {
			parentID = this.findId(dataProducer);
		}
		if (parentID == null) {
			return 0;
		}

		// Now create the query
		StringBuffer sqlStringBuffer = new StringBuffer();
		sqlStringBuffer.append("select count(distinct dataProducer) from "
				+ "DataProducer dataProducer where");
		sqlStringBuffer.append(" dataProducer.parentDataProducer.id = '"
				+ parentID.toString() + "'");

		try {
			countToReturn = ((Long) this.getSession().createQuery(
					sqlStringBuffer.toString()).uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		return countToReturn;
	}

	/**
	 * TODO kgomes implement and document this
	 * 
	 * @param dataProducerGroup
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByDataProducerGroup(
			DataProducerGroup dataProducerGroup, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// First make sure the dataProducerGroup exists
		DataProducerGroupDAO dataProducerGroupDAO = new DataProducerGroupDAO(
				getSession());

		DataProducerGroup persistentDataProducerGroup = null;
		persistentDataProducerGroup = (DataProducerGroup) dataProducerGroupDAO
				.findEquivalentPersistentObject(dataProducerGroup, false);

		if (persistentDataProducerGroup == null)
			throw new MetadataAccessException(
					"A matching dataProducerGroup could not be found in the system");

		// The collection to return
		Collection dataProducersToReturn = new ArrayList();

		// Create the criteria
		try {
			Criteria criteria = getSession().createCriteria(DataProducer.class);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			criteria.createAlias("dataProducerGroups", "dpgs");
			criteria.add(Restrictions.eq("dpgs.id", persistentDataProducerGroup
					.getId()));
			addOrderByCriteria(criteria, orderByPropertyName,
					ascendingOrDescending);
			dataProducersToReturn = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			this.initializeRelationships(dataProducersToReturn);
		}

		return dataProducersToReturn;
	}

	/**
	 * TODO kgomes implement and document this
	 * 
	 * @param dataProducerGroup
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindByDataProducerGroup(DataProducerGroup dataProducerGroup)
			throws MetadataAccessException {
		return -1;
	}

	/**
	 * This method will look up all the <code>DataProducer</code>s that are
	 * linked to a <code>DataProducerGroup</code> with the name supplied. The
	 * search by name can be done exactly or as a LIKE query.
	 * 
	 * @param dataProducerGroupName
	 *            is the name of the <code>DataProducerGroup</code> to search
	 *            for and then return all <code>DataProducer</code>s that are
	 *            linked to it.
	 * @param exactMatch
	 *            determines whether or not the name search is exact or as a
	 *            LIKE
	 * @param orderByPropertyName
	 *            this is a string that can be used to try and specify the
	 *            property that the query should try to order the results by. It
	 *            must match a property name of <code>DataProducer</code>.
	 * @param returnFullObjectGraph
	 *            is a boolean that specifies if the caller wants the fully
	 *            instantianted object graph (relationships) returned, or just
	 *            the query object itself. If you want the full graph returned
	 *            specify <code>true</code>, otherwise leave it false. <b>NOTE:
	 *            By specifying true, you could be requesting a large object
	 *            tree which will slow things down, so use sparingly</b>
	 * @return a <code>Collection</code> of <code>DataProducer</code>s that are
	 *         linked to the <code>DataProducerGroup</code> with the given name
	 * @throws MetadataAccessException
	 *             if something goes wrong
	 */
	public Collection findByDataProducerGroupName(String dataProducerGroupName,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// The Collection to return
		Collection results = new ArrayList();

		// Construct and run the query
		StringBuffer sqlStringBuffer = new StringBuffer();
		sqlStringBuffer.append("select distinct dataProducer "
				+ "from DataProducer dataProducer "
				+ "join dataProducer.dataProducerGroups dataProducerGroup "
				+ "where dataProducerGroup.name");
		if (exactMatch) {
			sqlStringBuffer.append(" = '" + dataProducerGroupName + "'");
		} else {
			sqlStringBuffer.append(" like '%" + dataProducerGroupName + "%'");
		}
		sqlStringBuffer.append(this.getOrderByPropertyNameSQLClause(
				orderByPropertyName, ascendingOrDescending));
		logger.debug("sqlStringBuffer: " + sqlStringBuffer.toString());
		try {
			Query query = getSession().createQuery(sqlStringBuffer.toString());
			results = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph) {
			initializeRelationships(results);
		}

		// Return the results
		return results;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param dataProducerGroupName
	 * @param exactMatch
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindByDataProducerGroupName(String dataProducerGroupName,
			boolean exactMatch) throws MetadataAccessException {
		// The count to return
		int count = 0;

		// Construct and run the query
		StringBuffer sqlStringBuffer = new StringBuffer();
		sqlStringBuffer
				.append("select count(distinct dataProducer) "
						+ "from DataProducer dataProducer "
						+ "join dataProducer.dataProducerGroups dataProuducerGroup where dataProducerGroup.name");
		if (exactMatch) {
			sqlStringBuffer.append(" = '" + dataProducerGroupName + "'");
		} else {
			sqlStringBuffer.append(" like '%" + dataProducerGroupName + "%'");
		}
		try {
			Query query = getSession().createQuery(sqlStringBuffer.toString());
			count = ((Long) query.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// Return the results
		return count;
	}

	/**
	 * This method takes in a DataContainer and finds all the DataProducers that
	 * use that DataContainer as inputs
	 * 
	 * @param dataContainer
	 * @param orderByPropertyName
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByInput(DataContainer dataContainer,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// First make sure the dataContainer exists
		DataContainerDAO dataContainerDAO = new DataContainerDAO(getSession());

		DataContainer persistentDataContainer = null;
		persistentDataContainer = (DataContainer) dataContainerDAO
				.findEquivalentPersistentObject(dataContainer, false);

		if (persistentDataContainer == null)
			return null;

		// The Collection to return
		Collection dataProducersToReturn = null;
		StringBuffer sqlStringBuffer = new StringBuffer();

		// If the dataContainer is null return null
		if (dataContainer == null)
			return null;

		// Now create the query
		Query query = null;

		sqlStringBuffer
				.append("select distinct dataProducer from "
						+ "DataProducer dataProducer, DataContainer dataContainer where");
		sqlStringBuffer.append(" dataContainer.id = :dataContainerID and ");
		sqlStringBuffer
				.append(" dataContainer in elements(dataProducer.inputs)");

		try {
			query = this.getSession().createQuery(sqlStringBuffer.toString());
			query.setLong("dataContainerID", persistentDataContainer.getId()
					.longValue());
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		dataProducersToReturn = query.list();

		if (returnFullObjectGraph)
			initializeRelationships(dataProducersToReturn);

		return dataProducersToReturn;
	}

	/**
	 * TODO kgomes implement and document this
	 * 
	 * @param dataContainer
	 * @param orderByPropertyName
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public DataProducer findByOutput(DataContainer dataContainer,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// First make sure the dataContainer exists
		DataContainerDAO dataContainerDAO = new DataContainerDAO(getSession());

		DataContainer persistentDataContainer = null;
		persistentDataContainer = (DataContainer) dataContainerDAO
				.findEquivalentPersistentObject(dataContainer, false);

		if (persistentDataContainer == null)
			return null;

		// The DataProducer to return
		DataProducer dataProducerToReturn = null;
		StringBuffer sqlStringBuffer = new StringBuffer();

		// If the dataContainer is null return null
		if (dataContainer == null)
			return null;

		// Now create the query
		Query query = null;

		sqlStringBuffer
				.append("select distinct dataProducer from "
						+ "DataProducer dataProducer, DataContainer dataContainer where");
		sqlStringBuffer.append(" dataContainer.id = :dataContainerID and ");
		sqlStringBuffer
				.append(" dataContainer in elements(dataProducer.outputs)");

		try {
			query = this.getSession().createQuery(sqlStringBuffer.toString());
			query.setLong("dataContainerID", persistentDataContainer.getId()
					.longValue());
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		dataProducerToReturn = (DataProducer) query.uniqueResult();

		if (returnFullObjectGraph)
			initializeRelationships(dataProducerToReturn);

		return dataProducerToReturn;
	}

	/**
	 * TODO kgomes implement and document this
	 * 
	 * @param resource
	 * @param orderByPropertyName
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
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

		sqlStringBuffer.append("select distinct dataProducer from "
				+ "DataProducer dataProducer, Resource resource where");
		sqlStringBuffer.append(" resource.id = :resourceID and ");
		sqlStringBuffer.append(" resource in elements(dataProducer.resources)");

		if ((orderByPropertyName != null)
				&& (checkIfPropertyOK(orderByPropertyName))) {
			sqlStringBuffer.append(" order by dataProducer."
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
			initializeRelationships(results);

		return results;
	}

	/**
	 * TODO kgomes implement and document this
	 * 
	 * @param resource
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindByResource(Resource resource)
			throws MetadataAccessException {
		return -1;
	}

	/**
	 * TODO kgomes document this
	 */
	public Collection findByKeywordName(String keywordName, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// The collection to return
		Collection results = new ArrayList();

		// Check the name
		if ((keywordName == null) || (keywordName.equals("")))
			return results;

		// The query string
		StringBuffer sqlStringBuffer = new StringBuffer();
		sqlStringBuffer.append("select distinct dataProducer "
				+ "from DataProducer dataProducer "
				+ "join dataProducer.keywords keyword where keyword.name ");
		if (exactMatch) {
			sqlStringBuffer.append(" = '" + keywordName + "'");
		} else {
			sqlStringBuffer.append(" like '%" + keywordName + "%'");
		}
		sqlStringBuffer.append(getOrderByPropertyNameSQLClause(
				orderByPropertyName, ascendingOrDescending));

		try {
			results = this.getSession().createQuery(sqlStringBuffer.toString())
					.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		if (returnFullObjectGraph)
			initializeRelationships(results);

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
		sqlStringBuffer.append("select count(distinct dataProducer) "
				+ "from DataProducer dataProducer "
				+ "join dataProducer.keywords keyword where keyword.name ");
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

	/**
	 * TODO kgomes implement and document this
	 * 
	 * @param event
	 * @param orderByPropertyName
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByEvent(Event event, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		return new ArrayList();
	}

	/**
	 * TODO kgomes implement and document this
	 * 
	 * @param event
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindByEvent(Event event) throws MetadataAccessException {
		return -1;
	}

	/**
	 */
	public Collection findAllDeploymentsOfDeviceTypeFromParent(
			DataProducer parentDeployment, String deviceTypeName,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// Create the array list to return
		Collection allDeployments = new ArrayList();

		// Check incoming values
		if (parentDeployment == null)
			return allDeployments;
		if ((deviceTypeName == null) || (deviceTypeName.equals("")))
			return allDeployments;

		// Now grab the peristent equivalent
		DataProducer equivalentPersistentParent = null;
		equivalentPersistentParent = (DataProducer) this
				.findEquivalentPersistentObject(parentDeployment, false);
		if (equivalentPersistentParent == null)
			return allDeployments;

		// Now create the query
		StringBuffer sqlStringBuffer = new StringBuffer();
		sqlStringBuffer.append("select distinct dataProducer from "
				+ "DataProducer dataProducer where");
		sqlStringBuffer.append(" dataProducer.parentDataProducer.id = '"
				+ equivalentPersistentParent.getId().toString() + "' AND "
				+ "dataProducer.device.deviceType.name = '" + deviceTypeName
				+ "'");

		try {
			allDeployments = this.getSession().createQuery(
					sqlStringBuffer.toString()).list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Now we need to loop over the direct child deployments to see if any
		// of those have child deployments with the same devicetype
		Collection directChildren = equivalentPersistentParent
				.getChildDataProducers();
		Iterator childIter = directChildren.iterator();
		while (childIter.hasNext()) {
			Collection moreDataProducers = this
					.findAllDeploymentsOfDeviceTypeFromParent(
							(DataProducer) childIter.next(), deviceTypeName,
							orderByPropertyName, ascendingOrDescending,
							returnFullObjectGraph);
			if ((moreDataProducers != null) && (moreDataProducers.size() > 0)) {
				allDeployments.addAll(moreDataProducers);
			}
		}

		// If no order by property was specified, choose by startDate in
		// descending order
		if ((orderByPropertyName == null) || (orderByPropertyName.equals(""))) {
			orderByPropertyName = "startDate";
			ascendingOrDescending = DESCENDING_ORDER;
		}
		// Check to see if the property to sort by was requested
		if (orderByPropertyName != null) {
			allDeployments = sortCollectionByPropertyName(allDeployments,
					orderByPropertyName, ascendingOrDescending);
		}

		// Check if the full object graph is to be returned
		if (returnFullObjectGraph)
			initializeRelationships(allDeployments);

		// Now return the results
		return allDeployments;
	}

	/**
	 * TODO kgomes implement and document this. This method takes in a deviceID,
	 * a DeviceType name, and some nominal location coordinates and then tries
	 * to return a <code>Collection</code> of <code>IDeployment</code>s the have
	 * a similar device type name, that were deployed on (or under) the given
	 * deployment (parent-child relationship). It will do a &quot;deep&quot;
	 * search for devices. In other words, it will check all deployments of the
	 * parent as well as of any sub-deployments under that parent (i.e. it will
	 * &quot;Walk the chain&quot;).
	 * 
	 * @param parentDeployment
	 *            This is a parent <code>IDeployment</code> to start the search
	 *            from. It is the &quot;Root&quot; of the search tree.
	 * @param deviceTypeName
	 *            This is a <code>String</code> that will be used to search for
	 *            devices that have a similar device type to that named with
	 *            this string.
	 * @param nominalLongitude
	 *            This is the longitude that the device should be deployed at.
	 *            It is used as the point of a search and then you can use the
	 *            <code>longitudeTolerance</code> to declare a +/- search
	 *            window.
	 * @param longitudeTolerance
	 *            This is the +/- that will be added to the
	 *            <code>nominalLongitude</code> parameter for window searches.
	 * @param nominalLatitude
	 *            This is the lattitude that the device should be deployed at.
	 *            It is used as the point of a search and then you can use the
	 *            <code>lattitudeTolerance</code> to declare a +/- search
	 *            window.
	 * @param lattitudeTolerance
	 *            This is the +/- that will be added to the
	 *            <code>nominalLatitude</code> parameter for window searches.
	 * @param nominalDepth
	 *            This is the depth that the device should be deployed at. It is
	 *            used as the point of a search and then you can use the
	 *            <code>depthTolerance</code> to declare a +/- search window.
	 * @param depthTolerance
	 *            This is the +/- that will be added to the
	 *            <code>nominalDepth</code> parameter for window searches.
	 * @return a <code>Collection</code> of <code>IDeployment</code>s that meet
	 *         the search criteria defined by the incoming parameters. No
	 *         duplicates are removed and if no deployments were found, an empty
	 *         collection is returned.
	 */
	public Collection findAllDeploymentsOfDeviceTypeFromParent(Long parentID,
			String deviceTypeName, Double nominalLongitude,
			Double longitudeTolerance, Double nominalLatitude,
			Double latitudeTolerance, Float nominalDepth,
			Double depthTolerance, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph) {

		// // Create the array list to return
		// ArrayList allDeployments = new ArrayList();
		// logger.debug("findAllDeploymentsOfDeviceTypeFromParent called
		// with:");
		// logger.debug("parentID = " + parentID);
		// logger.debug("deviceTypeName = " + deviceTypeName);
		// logger.debug("nominalLongitude = " + nominalLongitude);
		// logger.debug("longitudeTolerance = " + longitudeTolerance);
		// logger.debug("nominalLatitude = " + nominalLatitude);
		// logger.debug("latitudeTolerance = " + latitudeTolerance);
		// logger.debug("nominalDepth = " + nominalDepth);
		// logger.debug("depthTolerance = " + depthTolerance);
		//
		// // Check incoming values
		// if ((parentID == null) || (parentID.longValue() <= 0))
		// return allDeployments;
		// if ((deviceTypeName == null) || (deviceTypeName.equals("")))
		// return allDeployments;
		//
		// // Find the deployments of that parent ID
		// Collection parentDeployments = null;
		// try {
		// parentDeployments = this.findByDevicePK("" + parentID);
		// } catch (DataAccessException e) {
		// logger.error("DataAccessException : " + e.getMessage());
		// }
		//
		// // Now iterate over those deployment and add up the devices
		// if (parentDeployments != null) {
		// logger.debug("Found " + parentDeployments.size()
		// + " deployment of parent " + parentID);
		// Iterator deploymentIter = parentDeployments.iterator();
		// while (deploymentIter.hasNext()) {
		// // Grab the next deployment
		// IDeployment currentDeployment = (IDeployment) deploymentIter
		// .next();
		// logger.debug("Working with deployment: "
		// + currentDeployment.toStringRepresentation("|"));
		//
		// // Call the method to search under this deployment
		// Collection tempDeployments = this
		// .findAllDeploymentsOfDeviceTypeFromParent(
		// currentDeployment, deviceTypeName, nominalLongitude,
		// longitudeTolerance, nominalLatitude, latitudeTolerance,
		// nominalDepth, depthTolerance);
		//
		// // If there were any results, add it to the overall collection
		// if ((tempDeployments != null) && (tempDeployments.size() > 0)) {
		// allDeployments.addAll(tempDeployments);
		// }
		// }
		// }
		//
		// // Now return the results
		// return allDeployments;
		return new ArrayList();
	}

	/**
	 * TODO kgomes implement and document this. This method takes in the ID of a
	 * parent <code>IDevice</code>, the name of a <code>IDeviceType</code>, and
	 * some nominal location coordinates and then tries to return a
	 * <code>Collection</code> of <code>IDevice</code>s with the given type,
	 * that were deployed on the given device (parent-child) relationship. This
	 * will only look for direct child deployments, it won't walk any of the sub
	 * deployments.
	 * 
	 * @param parentID
	 * @param deviceTypeName
	 * @param nominalLongitude
	 * @param longitudeTolerance
	 * @param nominalLatitude
	 * @param latitudeTolerance
	 * @param nominalDepth
	 * @param depthTolerance
	 * @return
	 */
	public Collection findDevicesByParentByTypeAndByLocation(Long parentID,
			String deviceTypeName, Double nominalLongitude,
			Double longitudeTolerance, Double nominalLatitude,
			Double latitudeTolerance, Float nominalDepth,
			Double depthTolerance, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph) {
		// // Check incoming values
		// if ((parentID == null) || (parentID.longValue() <= 0))
		// return null;
		// if ((deviceTypeName == null) || (deviceTypeName.equals("")))
		// return null;
		//
		// // Create the collection that will be returned
		// // Collection devicesToReturn = new ArrayList();
		// ArrayList devicesToReturn = new ArrayList();
		//
		// // Grab the persistence broker
		// PersistenceBroker broker = this.getReadWriteBroker();
		//
		// // ***
		// Criteria crit = new Criteria();
		// crit.addLike("device.deviceType.name", "%" + deviceTypeName + "%");
		// crit.addEqualTo("parentDeployment.device.id", parentID);
		// if (nominalLongitude != null) {
		// if (longitudeTolerance != null) {
		// crit.addLessOrEqualThan("nominalLongitude", new Double(
		// nominalLongitude.doubleValue()
		// + longitudeTolerance.doubleValue()));
		// crit.addGreaterOrEqualThan("nominalLongitude", new Double(
		// nominalLongitude.doubleValue()
		// - longitudeTolerance.doubleValue()));
		// } else {
		// crit.addEqualTo("nominalLongitude", nominalLongitude);
		// }
		// }
		// if (nominalLatitude != null) {
		// if (latitudeTolerance != null) {
		// crit.addLessOrEqualThan("nominalLatitude", new Double(
		// nominalLatitude.doubleValue()
		// + latitudeTolerance.doubleValue()));
		// crit.addGreaterOrEqualThan("nominalLatitude", new Double(
		// nominalLatitude.doubleValue()
		// - latitudeTolerance.doubleValue()));
		// } else {
		// crit.addEqualTo("nominalLatitude", nominalLatitude);
		// }
		// }
		// if (nominalDepth != null) {
		// if (depthTolerance != null) {
		// crit.addLessOrEqualThan("nominalDepth", new Double(nominalDepth
		// .doubleValue()
		// + depthTolerance.doubleValue()));
		// crit.addGreaterOrEqualThan("nominalDepth", new Double(
		// nominalDepth.doubleValue() - depthTolerance.doubleValue()));
		// } else {
		// crit.addEqualTo("nominalDepth", nominalDepth);
		// }
		// }
		// // Query q = QueryFactory.newQuery(Deployment.class, crit);
		// QueryByCriteria q = new QueryByCriteria(Deployment.class, crit);
		// q.addOrderByDescending("startDate");
		//
		// Collection deviceDeployments = null;
		//
		// try {
		// deviceDeployments = broker.getCollectionByQuery(q);
		// } catch (Throwable th) {
		// logger
		// .error("Caught throwable when trying to find IDeployments by name"
		// + th.getMessage() + "\nWill close read only broker.");
		// broker.close();
		// logger.error("Broker closed");
		// }
		// // Now close the broker
		// broker.close();
		//
		// // Now loop through them and construct the list of devices
		// Iterator deploymentIter = deviceDeployments.iterator();
		// while (deploymentIter.hasNext()) {
		// IDeployment tempDeployment = (IDeployment) deploymentIter.next();
		// IDevice deviceToAddMaybe = tempDeployment.getDevice();
		// if (!devicesToReturn.contains(deviceToAddMaybe)) {
		// devicesToReturn.add(deviceToAddMaybe);
		// }
		// }
		//
		// return devicesToReturn;
		//
		return new ArrayList();
	}

	/**
	 * TODO kgomes implement and document this. This method takes in the name of
	 * a <code>IDevice</code>, the name of a <code>IDeviceType</code>, and some
	 * nominal location coordinates and then tries to return a
	 * <code>Collection</code> of <code>IDevices</code> with the given type,
	 * that were deployed on the given device (parent-child) relationship. This
	 * will only look for direct child deployments, it won't walk any of the sub
	 * deployments.
	 * 
	 * @param parentName
	 * @param deviceTypeName
	 * @param nominalLongitude
	 * @param longitudeTolerance
	 * @param nominalLatitude
	 * @param latitudeTolerance
	 * @param nominalDepth
	 * @param depthTolerance
	 * @return
	 */
	public Collection findDevicesByParentByTypeAndByLocation(String parentName,
			String deviceTypeName, Double nominalLongitude,
			Double longitudeTolerance, Double nominalLatitude,
			Double latitudeTolerance, Float nominalDepth,
			Double depthTolerance, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph) {
		//
		// // Check incoming values
		// if ((parentName == null) || (parentName.equals("")))
		// return null;
		//
		// // Now find the parent device
		// DeviceAccessLocalHome daLH = null;
		// try {
		// daLH = DeviceAccessUtil.getLocalHome();
		// } catch (NamingException e1) {
		// logger.error("NamingException caught: " + e1.getMessage());
		// return null;
		// }
		// DeviceAccessLocal daL = null;
		// try {
		// daL = (DeviceAccessLocal) daLH.create();
		// } catch (CreateException e) {
		// logger.error("CreateException caught: " + e.getMessage());
		// return null;
		// }
		// Collection possibleParentDevices = null;
		// try {
		// possibleParentDevices = daL.findByLikeName(parentName);
		// } catch (DataAccessException e2) {
		// logger.error("DataAccessException caught: " + e2.getMessage());
		// return null;
		// }
		// if ((possibleParentDevices != null)
		// && (possibleParentDevices.size() > 0)) {
		// IDevice parentDevice = null;
		// parentDevice = (IDevice) possibleParentDevices.iterator().next();
		// if (parentDevice != null) {
		// return this.findDevicesByParentByTypeAndByLocation(parentDevice
		// .getId(), deviceTypeName, nominalLongitude,
		// longitudeTolerance, nominalLatitude, latitudeTolerance,
		// nominalDepth, depthTolerance);
		// }
		//
		// }
		return new ArrayList();
	}

	/**
	 * TODO kgomes implement and document this. This method takes in a
	 * <code>IDeployment</code> and a <code>DeviceType</code> name, and some
	 * nominal location coordinates and then tries to return a list of
	 * <code>IDevice</code>s the have a similar device type name, that were
	 * deployed on the given device (parent-child) relationship. It will do a
	 * &quot;deep&quot; search for devices. In other words, it will check all
	 * deployments of the parent as well as of any sub-deployments under that
	 * parent (i.e. it will &quot;Walk the chain&quot;).
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Supports"
	 * @param parentDeployment
	 *            This is a parent <code>IDeployment</code> to start the search
	 *            from. It is the &quot;Root&quot; of the search tree.
	 * @param deviceTypeName
	 *            This is a <code>String</code> that will be used to search for
	 *            devices that have a similar device type to that named with
	 *            this string.
	 * @param nominalLongitude
	 *            This is the longitude that the device should be deployed at.
	 *            It is used as the point of a search and then you can use the
	 *            <code>longitudeTolerance</code> to declare a +/- search
	 *            window.
	 * @param longitudeTolerance
	 *            This is the +/- that will be added to the
	 *            <code>nominalLongitude</code> parameter for window searches.
	 * @param nominalLatitude
	 *            This is the lattitude that the device should be deployed at.
	 *            It is used as the point of a search and then you can use the
	 *            <code>lattitudeTolerance</code> to declare a +/- search
	 *            window.
	 * @param lattitudeTolerance
	 *            This is the +/- that will be added to the
	 *            <code>nominalLatitude</code> parameter for window searches.
	 * @param nominalDepth
	 *            This is the depth that the device should be deployed at. It is
	 *            used as the point of a search and then you can use the
	 *            <code>depthTolerance</code> to declare a +/- search window.
	 * @param depthTolerance
	 *            This is the +/- that will be added to the
	 *            <code>nominalDepth</code> parameter for window searches.
	 * @return a <code>Collection</code> of <code>IDevice</code>s that meet the
	 *         search criteria defined by the incoming parameters. The devices
	 *         are listed from the most recent deployment first (index 0) to the
	 *         oldest deployment. Each device is listed only once in the return
	 *         collection
	 */
	public Collection findAllDevicesByParentDeploymentByTypeAndByLocation(
			DataProducer parentDeployment, String deviceTypeName,
			Double nominalLongitude, Double longitudeTolerance,
			Double nominalLatitude, Double latitudeTolerance,
			Float nominalDepth, Double depthTolerance,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) {

		// // Create two array lists to hold devices and dates
		// ArrayList dateArrayList = new ArrayList();
		// ArrayList deviceArrayList = new ArrayList();
		//
		// // Grab the listing of deployment from this service
		// Collection allDeployments = this
		// .findAllDeploymentsOfDeviceTypeFromParent(parentDeployment,
		// deviceTypeName, nominalLongitude, longitudeTolerance,
		// nominalLatitude, latitudeTolerance, nominalDepth,
		// depthTolerance);
		//
		// // If something was returned, process the deployments
		// if ((allDeployments != null) && (allDeployments.size() > 0)) {
		// // Add each one to the tree map using the start date as
		// // the key so it will be in date sorted order
		// Iterator allDeploymentsIterator = allDeployments.iterator();
		// while (allDeploymentsIterator.hasNext()) {
		// // Grab the deployment and device
		// IDeployment currentDeployment = (IDeployment) allDeploymentsIterator
		// .next();
		// IDevice currentDevice = currentDeployment.getDevice();
		//
		// // Check for proxy objects
		// if (ProxyHelper.isProxy(currentDeployment))
		// currentDeployment = (IDeployment) ProxyHelper
		// .getRealObject(currentDeployment);
		// if ((currentDevice != null)
		// && (ProxyHelper.isProxy(currentDevice)))
		// currentDevice = (IDevice) ProxyHelper
		// .getRealObject(currentDevice);
		//
		// // If the device exists, add it to the tree map with
		// // the deployment start date, putting in in the correct
		// // time order
		// if (currentDevice != null) {
		// // Loop through the array list of devices to search
		// // for the current device. If it is found, check the
		// // corresponding date and if the date is less than
		// // the current deployment start date, replace the date.
		// // Otherwise, add the new device/date combination
		// if (deviceArrayList.contains(currentDevice)) {
		// int deviceIndex = deviceArrayList
		// .indexOf(currentDevice);
		// if (((Long) (dateArrayList.get(deviceIndex)))
		// .longValue() < currentDeployment.getStartDate()
		// .getTime()) {
		// dateArrayList.set(deviceIndex, new Long(
		// currentDeployment.getStartDate().getTime()));
		// }
		// } else {
		// dateArrayList.add(new Long(currentDeployment
		// .getStartDate().getTime()));
		// deviceArrayList.add(currentDevice);
		// }
		// }
		// }
		// }
		//
		// // OK, I now have two array lists with dates and devices
		// // that correspond. This means that for each date, there
		// // is a device that is its most recent deployment date. So
		// // I can now grab the sort order of the dates and then use
		// // that to sort the Device arrays.
		//
		// // First convert both ArrayList to Object arrays
		// Long[] timeArray = (Long[]) dateArrayList
		// .toArray(new Long[dateArrayList.size()]);
		// Object[] deviceArray = deviceArrayList
		// .toArray(new Object[deviceArrayList.size()]);
		//
		// // Get the sort order
		// int[] sortOrder = MathUtil.getSortOrder(timeArray);
		// // Now sort the devices
		// Object[] sortedDevices = MathUtil.orderVector(deviceArray,
		// sortOrder);
		//
		// // Now we have an array of device sorted by descending time,
		// // We need to read that backwards and put in a collection
		// ArrayList sortedDeviceCollection = new ArrayList();
		// for (int i = (sortedDevices.length - 1); i >= 0; i--) {
		// sortedDeviceCollection.add(sortedDevices[i]);
		// }
		//
		// // Now return the results
		// return sortedDeviceCollection;
		return new ArrayList();
	}

	/**
	 * TODO kgomes implement and document this.
	 * 
	 * @param parentID
	 * @param deviceTypeName
	 * @param nominalLongitude
	 * @param longitudeTolerance
	 * @param nominalLatitude
	 * @param latitudeTolerance
	 * @param nominalDepth
	 * @param depthTolerance
	 * @return
	 */
	public Collection findAllDevicesByParentByTypeAndByLocation(Long parentID,
			String deviceTypeName, Double nominalLongitude,
			Double longitudeTolerance, Double nominalLatitude,
			Double latitudeTolerance, Float nominalDepth,
			Double depthTolerance, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph) {

		// logger.debug("findAllDevicesByParentByTypeAndByLocation called
		// with:");
		// logger.debug("parentID = " + parentID);
		// logger.debug("deviceTypeName = " + deviceTypeName);
		// logger.debug("nominalLongitude = " + nominalLongitude);
		// logger.debug("longitudeTolerance = " + longitudeTolerance);
		// logger.debug("nominalLatitude = " + nominalLatitude);
		// logger.debug("latitudeTolerance = " + latitudeTolerance);
		// logger.debug("nominalDepth = " + nominalDepth);
		// logger.debug("depthTolerance = " + depthTolerance);
		//
		// // The array list to return
		// ArrayList devicesToReturn = new ArrayList();
		// // An array list to grab all the results
		// ArrayList cumulativeDeployments = new ArrayList();
		//
		// // Check incoming values
		// if ((parentID == null) || (parentID.longValue() <= 0))
		// return devicesToReturn;
		// if ((deviceTypeName == null) || (deviceTypeName.equals("")))
		// return devicesToReturn;
		//
		// // Find the deployments of that parent ID
		// Collection parentDeployments = null;
		// try {
		// parentDeployments = this.findByDevicePK("" + parentID);
		// } catch (DataAccessException e) {
		// logger.error("DataAccessException : " + e.getMessage());
		// }
		// // Now iterate over those deployment and add up the devices
		// if (parentDeployments != null) {
		// logger.debug("Found " + parentDeployments.size()
		// + " deployment of parent " + parentID);
		// Iterator deploymentIter = parentDeployments.iterator();
		// while (deploymentIter.hasNext()) {
		// // Grab the next deployment
		// IDeployment currentDeployment = (IDeployment) deploymentIter
		// .next();
		// logger.debug("Working with deployment: "
		// + currentDeployment.toStringRepresentation("|"));
		//
		// // The collection of subdeployment that meet the search criteria
		// Collection allDeployments = this
		// .findAllDeploymentsOfDeviceTypeFromParent(
		// currentDeployment, deviceTypeName, nominalLongitude,
		// longitudeTolerance, nominalLatitude, latitudeTolerance,
		// nominalDepth, depthTolerance);
		//
		// // Add that to the cumulative list
		// cumulativeDeployments.addAll(allDeployments);
		// }
		// }
		// // Now that I have all the deployments, I must sort like I did in the
		// // method above
		//
		// ArrayList dateArrayList = new ArrayList();
		// ArrayList deviceArrayList = new ArrayList();
		// if ((cumulativeDeployments != null)
		// && (cumulativeDeployments.size() > 0)) {
		// // Add each one to the tree map using the start date as
		// // the key so it will be in date sorted order
		// Iterator allDeploymentsIterator = cumulativeDeployments.iterator();
		// while (allDeploymentsIterator.hasNext()) {
		// // Grab the deployment and device
		// IDeployment currentDeployment = (IDeployment) allDeploymentsIterator
		// .next();
		// IDevice currentDevice = currentDeployment.getDevice();
		//
		// // Check for proxy objects
		// if (ProxyHelper.isProxy(currentDeployment))
		// currentDeployment = (IDeployment) ProxyHelper
		// .getRealObject(currentDeployment);
		// if ((currentDevice != null)
		// && (ProxyHelper.isProxy(currentDevice)))
		// currentDevice = (IDevice) ProxyHelper
		// .getRealObject(currentDevice);
		//
		// // If the device exists, add it to the tree map with
		// // the deployment start date, putting in in the correct
		// // time order
		// if (currentDevice != null) {
		// // Loop through the array list of devices to search
		// // for the current device. If it is found, check the
		// // corresponding date and if the date is less than
		// // the current deployment start date, replace the date.
		// // Otherwise, add the new device/date combination
		// if (deviceArrayList.contains(currentDevice)) {
		// int deviceIndex = deviceArrayList
		// .indexOf(currentDevice);
		// if (((Long) (dateArrayList.get(deviceIndex)))
		// .longValue() < currentDeployment.getStartDate()
		// .getTime()) {
		// dateArrayList.set(deviceIndex, new Long(
		// currentDeployment.getStartDate().getTime()));
		// }
		// } else {
		// dateArrayList.add(new Long(currentDeployment
		// .getStartDate().getTime()));
		// deviceArrayList.add(currentDevice);
		// }
		// }
		// }
		// }
		//
		// // OK, I now have two array lists with dates and devices
		// // that correspond. This means that for each date, there
		// // is a device that is its most recent deployment date. So
		// // I can now grab the sort order of the dates and then use
		// // that to sort the Device arrays.
		//
		// // First convert both ArrayList to Object arrays
		// Long[] timeArray = (Long[]) dateArrayList
		// .toArray(new Long[dateArrayList.size()]);
		// Object[] deviceArray = deviceArrayList
		// .toArray(new Object[deviceArrayList.size()]);
		//
		// // Get the sort order
		// int[] sortOrder = MathUtil.getSortOrder(timeArray);
		// // Now sort the devices
		// Object[] sortedDevices = MathUtil.orderVector(deviceArray,
		// sortOrder);
		//
		// // Now we have an array of device sorted by descending time,
		// // We need to read that backwards and put in a collection
		// for (int i = (sortedDevices.length - 1); i >= 0; i--) {
		// devicesToReturn.add(sortedDevices[i]);
		// }
		//
		// return devicesToReturn;
		return new ArrayList();
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param parentDataProducer
	 * @param childDataProducer
	 * @throws MetadataAccessException
	 */
	public void addChildDataProducer(DataProducer parentDataProducer,
			DataProducer childDataProducer) throws MetadataAccessException {
		// First find both equivalents
		DataProducer persistentParent = null;
		DataProducer persistentChild = null;
		persistentParent = (DataProducer) this.findEquivalentPersistentObject(
				parentDataProducer, false);
		persistentChild = (DataProducer) this.findEquivalentPersistentObject(
				childDataProducer, false);

		if ((persistentParent == null) || (persistentChild == null))
			throw new MetadataAccessException(
					"Either the parent or the child data producers could not found.");

		persistentParent.addChildDataProducer(persistentChild);

		this.makePersistent(persistentChild);
	}

	public void addResource(DataProducer dataProducer, Resource resourceToAdd)
			throws MetadataAccessException {

		// Find the persistent data producer
		DataProducer persistentDataProducer = null;
		persistentDataProducer = (DataProducer) this
				.findEquivalentPersistentObject(dataProducer, false);
		if (persistentDataProducer == null)
			throw new MetadataAccessException(
					"Could not find a matching DataProducer in "
							+ "the persistent store.");

		// Now persist the Resource
		ResourceDAO resourceDAO = new ResourceDAO(this.getSession());
		resourceDAO.makePersistent(resourceToAdd);
		Resource equivalentResource = (Resource) resourceDAO
				.findEquivalentPersistentObject(resourceToAdd, false);

		// Now add it to the DataProducer
		persistentDataProducer.addResource(equivalentResource);
	}

	/**
	 * @param dataProducer
	 * @param resource
	 */
	public void removeResource(DataProducer dataProducer, Resource resource)
			throws MetadataAccessException {

		// Grab the persistent DataProducer
		DataProducer persistentDataProducer = null;
		try {
			persistentDataProducer = (DataProducer) this
					.findEquivalentPersistentObject(dataProducer, false);
		} catch (MetadataAccessException e) {
			logger.error("MetadataAccessException caught trying to find "
					+ "equivalent dataProducer to remove from resource from:"
					+ e.getMessage());
		}

		// Check to make sure a persited DataProducer was found
		if (persistentDataProducer == null)
			throw new MetadataAccessException(
					"Could not find a matching DataProducer");

		// Grab the persistent resource
		Resource persistentResource = null;
		try {
			ResourceDAO resourceDAO = new ResourceDAO(this.getSession());
			persistentResource = (Resource) resourceDAO
					.findEquivalentPersistentObject(resource, false);
		} catch (MetadataAccessException e) {
			logger.error("MetadataAccessException caught trying to find "
					+ "equivalent resource to remove from data producer:"
					+ e.getMessage());
		}

		// Check to make sure the persisted Resource was found
		if (persistentResource == null)
			throw new MetadataAccessException(
					"No equivalent resource was found");

		// If we are here, try to remove the resource from the DataProducer
		persistentDataProducer.removeResource(persistentResource);
	}

	/**
	 * This method takes in a <code>DataProducer</code> that must be of type
	 * "deployment" and then creates "deep" copy of it and persists the new
	 * copy. It pulls in the options specified in the incoming parameters to
	 * create a unique copy of the DataProducers and DataContainer (outputs).
	 * All the rest of the object should be linked to the ones that already
	 * exist in the peristent storage. The new ID of the duplicate deployment is
	 * returned.
	 * 
	 * @param deploymentToCopy
	 * @param newStartDate
	 * @param closeOld
	 * @param oldEndDate
	 * @param newHeadDeploymentName
	 * @return
	 * @throws MetadataAccessException
	 */
	public Long createDuplicateDeepDeployment(DataProducer deploymentToCopy,
			Date newStartDate, boolean closeOld, Date oldEndDate,
			String newHeadDeploymentName, String baseDataStreamUri)
			throws MetadataAccessException {

		// Print some debug information
		logger
				.debug("createDuplicateDeepDeployment called with the following:");
		if (deploymentToCopy != null) {
			logger.debug("Deployment to copy: "
					+ deploymentToCopy.toStringRepresentation("|"));
		} else {
			logger.debug("Deployment to copy was NULL! "
					+ "Will do nothing and return null.");
		}
		logger.debug("New start date: " + newStartDate);
		logger.debug("Close Old? " + closeOld);
		logger.debug("Old end date: " + oldEndDate);
		logger.debug("New head deployment name: " + newHeadDeploymentName);
		logger.debug("Base data stream URI:" + baseDataStreamUri);

		// Grab the current date in case we need it
		Date currentDate = new Date();
		logger.debug("Current date is " + currentDate);

		// The Long to return
		Long duplicateDeploymentID = null;

		// Make sure the deployment is not null
		if (deploymentToCopy == null)
			return null;

		// Make sure the DataProducer is a deployment
		if (!deploymentToCopy.getDataProducerType().equals(
				DataProducer.TYPE_DEPLOYMENT))
			throw new MetadataAccessException(
					"The specified DataProducer must be a "
							+ "deployment and it does not appear to be");

		// Now grab the persistent instance of the specified deployment
		DataProducer persistentOneToCopy = (DataProducer) this
				.findEquivalentPersistentObject(deploymentToCopy, false);
		if (persistentOneToCopy == null)
			throw new MetadataAccessException(
					"No matching deployment could be found to copy");
		logger.debug("The matching persistent deployment was found and is:");
		logger.debug(persistentOneToCopy.toStringRepresentation("|"));

		// OK, so first create the clone
		DataProducer deepClone = null;
		try {
			deepClone = (DataProducer) persistentOneToCopy.deepCopy();
		} catch (CloneNotSupportedException e) {
			logger
					.error("CloneNotSupportedException caught: "
							+ e.getMessage());
		}
		if (deepClone == null)
			throw new MetadataAccessException(
					"Could not create a copy of the incoming deployment");
		logger.debug("OK, a deep copy was finished and "
				+ "the top level of the copy is:");
		logger.debug(deepClone.toStringRepresentation("|"));

		// First update the old deployment if it is to be closed
		if (closeOld) {
			Date endDate = currentDate;
			if (oldEndDate != null)
				endDate = oldEndDate;

			// Now recursively close out deployments and outputs
			deepUpdateDeploymentAndOutputs(persistentOneToCopy, null, endDate,
					false, null, null, null);
			logger.debug("OK, used a end date of " + endDate
					+ " to close the old deployment");
		}

		// Now let's walk the new clone's tree and change what we need to in
		// order to make
		// this deployment copy unique where it needs to be

		// Change the head deployment name if the incoming parameter is
		// different
		if ((newHeadDeploymentName != null)
				&& (!newHeadDeploymentName.equals(""))) {
			try {
				deepClone.setName(newHeadDeploymentName);
			} catch (MetadataException e) {
				throw new MetadataAccessException(
						"The incoming name specified caused "
								+ "an exception to be thrown: "
								+ e.getMessage());
			}
			logger.debug("Set the name of the copied deployment to");
		}

		// Now set the start date if specified
		if (newStartDate != null) {
			deepClone.setStartDate(newStartDate);
		} else {
			deepClone.setStartDate(currentDate);
		}
		logger.debug("New deployment's start date set to "
				+ deepClone.getStartDate());

		// Now call the method to update all the deployment's children and
		// outputs
		this.deepUpdateDeploymentAndOutputs(deepClone,
				deepClone.getStartDate(), null, true, null, null,
				baseDataStreamUri);
		logger.debug("Performed deepUpdateDeploymentAndOutputs "
				+ "with start date of " + deepClone.getStartDate()
				+ " and baseDataStreamUri of " + baseDataStreamUri);

		// Now persist it
		duplicateDeploymentID = makePersistent(deepClone);
		logger.debug("OK, persisted the clone and got ID of "
				+ duplicateDeploymentID);

		// Check to see if old deployment has parent
		DataProducer parentDataProducer = findParentDataProducer(
				persistentOneToCopy, false);
		if (parentDataProducer != null) {
			parentDataProducer.addChildDataProducer(deepClone);
			logger.debug("The old data producer had a parent, so "
					+ "the new one will be connected to parent:");
			logger.debug(parentDataProducer.toStringRepresentation("|"));
		} else {
			logger.debug("There was not parent of the old data "
					+ "sproducer so none will be set on the new one");
		}

		// Return the new ID
		return duplicateDeploymentID;
	}

	private void deepUpdateDeploymentAndOutputs(DataProducer deployment,
			Date startDate, Date endDate, boolean generateNewDataStreamUris,
			Long parentDeviceID, Long packetSubTypeID, String baseDataStreamUri) {

		// Make sure deployment was pased in
		if (deployment == null)
			return;

		// Set the deployment dates (if specified)
		if (startDate != null)
			deployment.setStartDate(startDate);
		if (endDate != null)
			deployment.setEndDate(endDate);
		// Now the same for the outputs
		Iterator outputIter = deployment.getOutputs().iterator();
		while (outputIter.hasNext()) {
			DataContainer output = (DataContainer) outputIter.next();
			output.setEndDate(endDate);
			// If a new URI is to be created for data streams, do so
			if (generateNewDataStreamUris) {
				// Make sure output is data stream
				if (output.getDataContainerType().equals(
						DataContainer.TYPE_STREAM)) {

					// First grab the old uri in case we need it
					String oldUriString = output.getUriString();
					// Try to parse out parameters from the old uri string in
					// case we need them later
					String oldDeviceIDString = null;
					Pattern deviceIDPattern = Pattern
							.compile("deviceID=(\\d+)");
					Matcher deviceIDMatcher = deviceIDPattern
							.matcher(oldUriString);
					if (deviceIDMatcher.find()) {
						oldDeviceIDString = deviceIDMatcher.group(1);
					}
					String oldParentIDString = null;
					Pattern parentIDPattern = Pattern
							.compile("startParentID=(\\d+)");
					Matcher parentIDMatcher = parentIDPattern
							.matcher(oldUriString);
					if (parentIDMatcher.find()) {
						oldParentIDString = parentIDMatcher.group(1);
					}
					String oldPacketSubTypeString = null;
					Pattern packetSubTypeIDPattern = Pattern
							.compile("startPacketSubType=(\\d+)");
					Matcher packetSubTypeIDMatcher = packetSubTypeIDPattern
							.matcher(oldUriString);
					if (packetSubTypeIDMatcher.find()) {
						oldPacketSubTypeString = packetSubTypeIDMatcher
								.group(1);
					}

					// In order to create a unique data stream, I need to
					// specify:
					// 1. DeviceID
					Long deviceID = null;
					// 2. ParentID (if available)
					Long parentID = null;
					// 3. RecordType (packetSubType)
					Long packetSubType = null;
					// 4. StartTimestamp

					// Grab the device ID from the deployment
					if (deployment.getDevice() != null)
						deviceID = deployment.getDevice().getId();
					// If the deviceID is null, try the old
					if ((deviceID == null) && (oldDeviceIDString != null)) {
						try {
							deviceID = new Long(oldDeviceIDString);
						} catch (Exception e) {
							logger
									.error("Could not convert oldDeviceIDString of "
											+ oldDeviceIDString
											+ " to a Long: " + e.getMessage());
						}
					}
					// If the parent ID was passed in use that if not, use the
					// parsed one
					if (parentDeviceID != null) {
						parentID = parentDeviceID;
					} else {
						if (oldParentIDString != null) {
							try {
								parentID = new Long(oldParentIDString);
							} catch (Exception e) {
								logger
										.error("Could not convert oldParentIDString of "
												+ oldParentIDString
												+ " to a Long: "
												+ e.getMessage());
							}
						}
					}
					// Now try the same for packet sub type
					if (packetSubTypeID != null) {
						packetSubType = packetSubTypeID;
					} else {
						if (oldPacketSubTypeString != null) {
							try {
								packetSubType = new Long(oldPacketSubTypeString);
							} catch (Exception e) {
								logger
										.error("Could not convert oldPacketSubTypeString of "
												+ oldPacketSubTypeString
												+ " to a Long: "
												+ e.getMessage());
							}
						}
					}
					// If the start date is not specified, choose now
					Date newStartDate = startDate;
					if (newStartDate == null) {
						newStartDate = new Date();
					}
					// Now create the new URI
					String newUriString = baseDataStreamUri + "?deviceID="
							+ deviceID + "&startParentID=" + parentID
							+ "&startPacketSubType=" + packetSubType
							+ "&startTimestampSeconds="
							+ new Long(newStartDate.getTime() / 1000)
							+ "&lastNumberOfPackets=10&isi=1";
					try {
						output.setUriString(newUriString);
					} catch (MetadataException e) {
						logger.error("Error trying to set the URI string to : "
								+ newUriString + ": " + e.getMessage());
					}
				}
			}
		}
		// Now recursively call on child deployments
		Iterator childDeploymentIter = deployment.getChildDataProducers()
				.iterator();
		while (childDeploymentIter.hasNext()) {
			deepUpdateDeploymentAndOutputs((DataProducer) childDeploymentIter
					.next(), startDate, endDate, generateNewDataStreamUris,
					parentDeviceID, packetSubTypeID, baseDataStreamUri);
		}
	}

	/**
	 * @see IMetadataDAO#makePersistent(IMetadataObject)
	 */
	public Long makePersistent(IMetadataObject metadataObject)
			throws MetadataAccessException {
		logger.debug("makePersistent called");

		// A flag to indicate if the DataProducer was persisted before
		boolean persistedBefore = false;

		// Check incoming object
		DataProducer dataProducer = this
				.checkIncomingMetadataObject(metadataObject);

		// Look for a matching dataProducer that is in the persistent store
		DataProducer persistentDataProducer = (DataProducer) this
				.findEquivalentPersistentObject(dataProducer, false);

		// Create a handle that will be assigned to the DataProducer that will
		// actually be persisted
		DataProducer dataProducerToPersist = null;

		// If there is an existing dataContainer, make sure everything lines up
		if (persistentDataProducer != null) {
			this.updateDestinationObject(dataProducer, persistentDataProducer);

			// Set the flag
			persistedBefore = true;

			// Assign to the handle
			dataProducerToPersist = persistentDataProducer;

			// Now check the parent and if it exists and is already in the
			// session move it to the persistent DataProducer
			if (dataProducer.getParentDataProducer() != null) {
				DataProducer parentDataProducer = dataProducer
						.getParentDataProducer();
				logger
						.debug("The incoming DataProducer has a parent attached ("
								+ parentDataProducer + ")");
				if (!getSession()
						.contains(dataProducer.getParentDataProducer())) {
					logger.debug("The incoming DataProducer's parent"
							+ " is not in the session yet, "
							+ "will try to load the matching one in.");
					DataProducer persistentParentDataProducer = (DataProducer) this
							.findEquivalentPersistentObject(dataProducer
									.getParentDataProducer(), false);
					if (persistentParentDataProducer != null) {
						logger.debug("The matching persistent parent "
								+ "DataProducer was found to be: "
								+ parentDataProducer
										.toStringRepresentation("|"));
						parentDataProducer = persistentDataProducer;
					} else {
						logger.debug("No matching parent DataProducer was "
								+ "found in the persistent store.");
						// TODO kgomes This is a problem and really should not
						// happen, but what do I do if it does?
					}
				} else {
					logger.debug("The incoming DataProducer's parent "
							+ "DataProducer is already in the session");
				}

				// Now if the parent is in the session, add the persistent child
				if (getSession().contains(parentDataProducer)) {
					logger
							.debug("OK, the parent DataProducer is in the session, so"
									+ "so will attach the persistent child");
					// Remove the transient one
					parentDataProducer.removeChildDataProducer(dataProducer);
					// Add the persistent one as a child DP
					parentDataProducer
							.addChildDataProducer(persistentDataProducer);
				}
			}

		} else {
			// Since this is a new one, make sure the alternate business key is
			// there
			if ((dataProducer.getDataProducerType() == null)
					|| (!DataProducer.isValidDataProducerType(dataProducer
							.getDataProducerType()))) {
				try {
					dataProducer
							.setDataProducerType(DataProducer.TYPE_DEPLOYMENT);
				} catch (MetadataException e) {
				}
				addMessage(ssdsAdminEmailToAddress,
						"An incoming DataProducer did not have a "
								+ "correct DataProducer Type assigned, "
								+ "so SSDS auto-generated one:<br><ul><li>"
								+ dataProducer.toStringRepresentation("<li>")
								+ "</ul><br>");
			}
			if ((dataProducer.getName() == null)
					|| (dataProducer.getName().equals(""))) {
				try {
					dataProducer.setName(dataProducer.getDataProducerType()
							+ "_" + getUniqueNameSuffix());
				} catch (MetadataException e) {
					logger.error("MetadataException trying to auto-generate "
							+ "a name for a DataProducer: " + e.getMessage());
				}
				addMessage(ssdsAdminEmailToAddress,
						"An incoming DataProducer did not have a name, "
								+ "so SSDS auto-generated one:<br><ul><li>"
								+ dataProducer.toStringRepresentation("<li>")
								+ "</ul><br>");
			}
			// Clear the flag
			persistedBefore = false;

			// Assign to the handle
			dataProducerToPersist = dataProducer;
		}

		// -------------------------------
		// DataProducerGroup Relationship
		// -------------------------------

		// First make sure the DataProducerGroup relationship exists
		if (dataProducer.getDataProducerGroups() != null) {
			// Now check to make sure it is initialized. If it is not, no
			// changes have taken place and we don't need to do anything
			if (Hibernate.isInitialized(dataProducer.getDataProducerGroups())) {

				// Grab the DAO for DataProducerGroup
				DataProducerGroupDAO dataProducerGroupDAO = new DataProducerGroupDAO(
						this.getSession());

				// Make sure the are DataProducerGroups to iterate over
				if (dataProducer.getDataProducerGroups().size() > 0) {

					// Now iterate over the DataProducerGroups and persist them
					Iterator dataProducerGroupIter = dataProducer
							.getDataProducerGroups().iterator();
					while (dataProducerGroupIter.hasNext()) {
						DataProducerGroup tempDataProducerGroup = (DataProducerGroup) dataProducerGroupIter
								.next();
						dataProducerGroupDAO
								.makePersistent(tempDataProducerGroup);
					}

					// Create a copy of the collection associated with the
					// DataProducer to prevent concurrent modifications
					Collection dataProducerDataProducerGroupCopy = new ArrayList(
							dataProducer.getDataProducerGroups());

					// Now we need to make the correct associations. Currently,
					// you have a collection of DataProducerGroup objects that
					// have their values marked for persistence. Now the object
					// will either be in the session or not depending on if they
					// were previously persisted.
					Iterator dataProducerDataProducerGroupCopyIterator = dataProducerDataProducerGroupCopy
							.iterator();
					while (dataProducerDataProducerGroupCopyIterator.hasNext()) {
						DataProducerGroup currentDataProducerGroup = (DataProducerGroup) dataProducerDataProducerGroupCopyIterator
								.next();
						DataProducerGroup currentDataProducerGroupInSession = null;
						// Is this DataProducerGroup already in the session?
						if (!getSession().contains(currentDataProducerGroup)) {
							// No, so grab the one that is
							currentDataProducerGroupInSession = (DataProducerGroup) dataProducerGroupDAO
									.findEquivalentPersistentObject(
											currentDataProducerGroup, false);
						} else {
							currentDataProducerGroupInSession = currentDataProducerGroup;
						}
						// Now if the parent dataProducer was persisted before,
						// just check to make sure the sessioned
						// DataProducerGroup is in the collection associated
						// with the dataProducer that will be persisted
						if (persistedBefore) {
							if (!dataProducerToPersist
									.getDataProducerGroups()
									.contains(currentDataProducerGroupInSession))
								dataProducerToPersist.getDataProducerGroups()
										.add(currentDataProducerGroupInSession);
						} else {
							// This means that the dataProducer has not been
							// persisted before. If the DataProducerGroup is
							// already in the session, there is nothing to do,
							// but if not, we need to replace it with the
							// sessioned one
							if (!getSession()
									.contains(currentDataProducerGroup)) {
								dataProducerToPersist.getDataProducerGroups()
										.remove(currentDataProducerGroup);
								dataProducerToPersist.getDataProducerGroups()
										.add(currentDataProducerGroupInSession);
							}
						}
					}
				}
			}
		}

		// -------------------------------
		// Event Relationship
		// -------------------------------

		// First make sure the Event relationship exists
		if (dataProducer.getEvents() != null) {
			// Now check to make sure it is initialized. If it is not, no
			// changes have taken place and we don't need to do anything
			if (Hibernate.isInitialized(dataProducer.getEvents())) {

				// Grab the DAO for Event
				EventDAO eventDAO = new EventDAO(this.getSession());

				// Make sure the are Events to iterate over
				if (dataProducer.getEvents().size() > 0) {

					// Now iterate over the Events and persist them
					Iterator eventIter = dataProducer.getEvents().iterator();
					while (eventIter.hasNext()) {
						Event tempEvent = (Event) eventIter.next();
						eventDAO.makePersistent(tempEvent);
					}

					// Create a copy of the collection associated with the
					// DataProducer to prevent concurrent modifications
					Collection dataProducerEventCopy = new ArrayList(
							dataProducer.getEvents());

					// Now we need to make the correct associations. Currently,
					// you have a collection of Event objects that
					// have their values marked for persistence. Now the object
					// will either be in the session or not depending on if they
					// were previously persisted.
					Iterator dataProducerEventCopyIterator = dataProducerEventCopy
							.iterator();
					while (dataProducerEventCopyIterator.hasNext()) {
						Event currentEvent = (Event) dataProducerEventCopyIterator
								.next();
						Event currentEventInSession = null;
						// Is this Event already in the session?
						if (!getSession().contains(currentEvent)) {
							// No, so grab the one that is
							currentEventInSession = (Event) eventDAO
									.findEquivalentPersistentObject(
											currentEvent, false);
						} else {
							currentEventInSession = currentEvent;
						}
						// Now if the parent dataProducer was persisted before,
						// just check to make sure the sessioned
						// Event is in the collection associated
						// with the dataProducer that will be persisted
						if (persistedBefore) {
							if (!dataProducerToPersist.getEvents().contains(
									currentEventInSession))
								dataProducerToPersist.getEvents().add(
										currentEventInSession);
						} else {
							// This means that the dataProducer has not been
							// persisted before. If the Event is
							// already in the session, there is nothing to do,
							// but if not, we need to replace it with the
							// sessioned one
							if (!getSession().contains(currentEvent)) {
								dataProducerToPersist.getEvents().remove(
										currentEvent);
								dataProducerToPersist.getEvents().add(
										currentEventInSession);
							}
						}
					}
				}
			}
		}

		// -------------------
		// Person Relationship
		// -------------------
		// First see if there is a person associated with the incoming
		// dataProducer
		if (dataProducer.getPerson() != null) {
			// Now, since there is, check to see if it has been initialized
			if (Hibernate.isInitialized(dataProducer.getPerson())) {
				// Grab the Person DAO to handle that relationship
				PersonDAO personDAO = new PersonDAO(this.getSession());

				// Now persist the person
				Person tempPerson = dataProducer.getPerson();
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

				// Now check to see if the dataProducer was persisted in the
				// past, if
				// so, just check to see if dataProducers person is different
				// and
				// update it if so
				if (persistedBefore) {
					if ((dataProducerToPersist.getPerson() == null)
							|| (!dataProducerToPersist.getPerson().equals(
									tempPersonInSession))) {
						dataProducerToPersist.setPerson(tempPersonInSession);
					}
				} else {
					// Make sure the person associated with the dataProducer is
					// the
					// session, if not replace it with the one that is
					if (!getSession().contains(
							dataProducerToPersist.getPerson())) {
						dataProducerToPersist.setPerson(tempPersonInSession);
					}
				}
			}
		}

		// -------------------------
		// Device Relationship
		// -------------------------
		// First see if there is a device associated with the incoming
		// dataProducer
		if (dataProducer.getDevice() != null) {
			// Now, since there is, check to see if it has been initialized
			if (Hibernate.isInitialized(dataProducer.getDevice())) {
				// Grab the Device DAO to handle that relationship
				DeviceDAO deviceDAO = new DeviceDAO(this.getSession());

				// Now persist the device
				Device tempDevice = dataProducer.getDevice();
				deviceDAO.makePersistent(tempDevice);

				// The matching device that is in the session
				Device tempDeviceInSession = null;

				// Check to see if the persisted device is in the session
				if (!getSession().contains(tempDevice)) {
					tempDeviceInSession = (Device) deviceDAO
							.findEquivalentPersistentObject(tempDevice, false);
				} else {
					tempDeviceInSession = tempDevice;
				}

				// Now check to see if the dataProducer was persisted in the
				// past, if
				// so, just check to see if dataProducers device is different
				// and
				// update it if so
				if (persistedBefore) {
					if ((dataProducerToPersist.getDevice() == null)
							|| (!dataProducerToPersist.getDevice().equals(
									tempDeviceInSession))) {
						dataProducerToPersist.setDevice(tempDeviceInSession);
					}
				} else {
					// Make sure the device associated with the dataProducer is
					// the
					// session, if not replace it with the one that is
					if (!getSession().contains(
							dataProducerToPersist.getDevice())) {
						dataProducerToPersist.setDevice(tempDeviceInSession);
					}
				}
			}
		}

		// -------------------------
		// Software Relationship
		// -------------------------
		// First see if there is a software associated with the incoming
		// dataProducer
		if (dataProducer.getSoftware() != null) {
			// Now, since there is, check to see if it has been initialized
			if (Hibernate.isInitialized(dataProducer.getSoftware())) {
				// Grab the Software DAO to handle that relationship
				SoftwareDAO softwareDAO = new SoftwareDAO(this.getSession());

				// Now persist the software
				Software tempSoftware = dataProducer.getSoftware();
				softwareDAO.makePersistent(tempSoftware);

				// The matching software that is in the session
				Software tempSoftwareInSession = null;

				// Check to see if the persisted software is in the session
				if (!getSession().contains(tempSoftware)) {
					tempSoftwareInSession = (Software) softwareDAO
							.findEquivalentPersistentObject(tempSoftware, false);
				} else {
					tempSoftwareInSession = tempSoftware;
				}

				// Now check to see if the dataProducer was persisted in the
				// past, if
				// so, just check to see if dataProducers software is different
				// and
				// update it if so
				if (persistedBefore) {
					if ((dataProducerToPersist.getSoftware() == null)
							|| (!dataProducerToPersist.getSoftware().equals(
									tempSoftwareInSession))) {
						dataProducerToPersist
								.setSoftware(tempSoftwareInSession);
					}
				} else {
					// Make sure the software associated with the dataProducer
					// is
					// the
					// session, if not replace it with the one that is
					if (!getSession().contains(
							dataProducerToPersist.getSoftware())) {
						dataProducerToPersist
								.setSoftware(tempSoftwareInSession);
					}
				}
			}
		}

		// ---------------------
		// Keyword Relationship
		// ----------------------
		// First make sure the keyword relationship exists
		if (dataProducer.getKeywords() != null) {
			// Now check to make sure it is initialized. If it is not, no
			// changes have taken place and we don't need to do anything
			if (Hibernate.isInitialized(dataProducer.getKeywords())) {

				// Grab the DAO for Keyword
				KeywordDAO keywordDAO = new KeywordDAO(this.getSession());

				// Make sure the are keywords to iterate over
				if (dataProducer.getKeywords().size() > 0) {

					// Now iterate over the Keywords and persist them
					Iterator keywordIter = dataProducer.getKeywords()
							.iterator();
					while (keywordIter.hasNext()) {
						Keyword tempKeyword = (Keyword) keywordIter.next();
						keywordDAO.makePersistent(tempKeyword);
					}
				}

				// Create a copy of the collection associated with the
				// dataProducer to prevent concurrent modifications
				Collection dataProducerKeywordCopy = new ArrayList(dataProducer
						.getKeywords());

				// Now we need to make the correct associations. Currently, you
				// have a collection of Keyword objects that have their values
				// marked for persistence. Now the object will either be in the
				// session or not depending on if they were previously
				// persisted.
				Iterator dataProducerKeywordCopyIterator = dataProducerKeywordCopy
						.iterator();
				while (dataProducerKeywordCopyIterator.hasNext()) {
					Keyword currentKeyword = (Keyword) dataProducerKeywordCopyIterator
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
					// Now if the parent dataProducer was persisted before,
					// just check to make sure the sessioned Keywords is in the
					// collection are associated with the dataProducer that
					// will be persisted
					if (persistedBefore) {
						if (!dataProducerToPersist.getKeywords().contains(
								currentKeywordInSession))
							dataProducerToPersist.getKeywords().add(
									currentKeywordInSession);
					} else {
						// This means that the dataProducer has not been
						// persisted before. If the Keyword is already in the
						// session, there is nothing to do, but if not, we need
						// to replace it with the sessioned one
						if (!getSession().contains(currentKeyword)) {
							dataProducerToPersist.getKeywords().remove(
									currentKeyword);
							dataProducerToPersist.getKeywords().add(
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
		if (dataProducer.getResources() != null) {
			// Now check to make sure it is initialized. If it is not, no
			// changes have taken place and we don't need to do anything
			if (Hibernate.isInitialized(dataProducer.getResources())) {

				// Grab the DAO for Resource
				ResourceDAO resourceDAO = new ResourceDAO(this.getSession());

				// Make sure the are resources to iterate over
				if (dataProducer.getResources().size() > 0) {

					// Now iterate over the Resources and persist them
					Iterator userGroupIter = dataProducer.getResources()
							.iterator();
					while (userGroupIter.hasNext()) {
						Resource tempResource = (Resource) userGroupIter.next();
						resourceDAO.makePersistent(tempResource);
					}
				}

				// Create a copy of the collection associated with the
				// dataProducer to
				// prevent concurrent modifications
				Collection dataProducerResourceCopy = new ArrayList(
						dataProducer.getResources());

				// Now we need to make the correct associations. Currently, you
				// have a collection of Resource objects that have their values
				// marked for persistence. Now the object will either be in the
				// session or not depending on if they were previously
				// persisted.
				Iterator dataProducerResourceCopyIterator = dataProducerResourceCopy
						.iterator();
				while (dataProducerResourceCopyIterator.hasNext()) {
					Resource currentResource = (Resource) dataProducerResourceCopyIterator
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
					// Now if the parent dataProducer was persisted before,
					// just
					// check to make sure the sessioned Resources is in the
					// collection are associated with the dataProducer that
					// will be
					// persisted
					if (persistedBefore) {
						if (!dataProducerToPersist.getResources().contains(
								currentResourceInSession))
							dataProducerToPersist.getResources().add(
									currentResourceInSession);
					} else {
						// This means that the dataProducer has not been
						// persisted
						// before. If the Resource is already in the session,
						// there is nothing to do, but if not, we need to
						// replace it with the sessioned one
						if (!getSession().contains(currentResource)) {
							dataProducerToPersist.getResources().remove(
									currentResource);
							dataProducerToPersist.getResources().add(
									currentResourceInSession);
						}
					}
				}
			}
		}

		// -----------------------------------
		// Inputs Relationship (DataContainer)
		// -----------------------------------
		// First make sure the input relationship exists
		if (dataProducer.getInputs() != null) {
			// Now check to make sure it is initialized. If it is not, no
			// changes have taken place and we don't need to do anything
			if (Hibernate.isInitialized(dataProducer.getInputs())) {

				// Grab the DAO for DataContainers
				DataContainerDAO dataContainerDAO = new DataContainerDAO(this
						.getSession());

				// Make sure the are inputs to iterate over
				if (dataProducer.getInputs().size() > 0) {

					// Now iterate over the inputs and persist them
					Iterator inputIter = dataProducer.getInputs().iterator();
					while (inputIter.hasNext()) {
						DataContainer tempInput = (DataContainer) inputIter
								.next();
						dataContainerDAO.makePersistent(tempInput);
					}
				}

				// Create a copy of the collection associated with the
				// dataProducer to prevent concurrent modifications
				Collection dataProducerInputCopy = new ArrayList(dataProducer
						.getInputs());

				// Now we need to make the correct associations. Currently, you
				// have a collection of DataContainer objects that have their
				// values
				// marked for persistence. Now the object will either be in the
				// session or not depending on if they were previously
				// persisted.
				Iterator dataProducerInputCopyIterator = dataProducerInputCopy
						.iterator();
				while (dataProducerInputCopyIterator.hasNext()) {
					DataContainer currentInput = (DataContainer) dataProducerInputCopyIterator
							.next();
					DataContainer currentInputInSession = null;
					// Is this input already in the session?
					if (!getSession().contains(currentInput)) {
						// No, so grab the one that is
						currentInputInSession = (DataContainer) dataContainerDAO
								.findEquivalentPersistentObject(currentInput,
										false);
					} else {
						currentInputInSession = currentInput;
					}
					// Now if the parent dataProducer was persisted before,
					// just check to make sure the sessioned inputs in the
					// collection are associated with the dataProducer that
					// will be persisted
					if (persistedBefore) {
						if (!dataProducerToPersist.getInputs().contains(
								currentInputInSession))
							dataProducerToPersist.getInputs().add(
									currentInputInSession);
					} else {
						// This means that the dataProducer has not been
						// persisted before. If the Input is already in the
						// session, there is nothing to do, but if not, we need
						// to replace it with the sessioned one
						if (!getSession().contains(currentInput)) {
							dataProducerToPersist.getInputs().remove(
									currentInput);
							dataProducerToPersist.getInputs().add(
									currentInputInSession);
						}
					}
				}
			}
		}

		// If not persisted before, save it
		if (!persistedBefore)
			getSession().save(dataProducerToPersist);

		// -----------------------------------
		// Outputs Relationship (DataContainer)
		// -----------------------------------
		// First make sure the output relationship exists
		if (dataProducer.getOutputs() != null) {
			// Now check to make sure it is initialized. If it is not, no
			// changes have taken place and we don't need to do anything
			if (Hibernate.isInitialized(dataProducer.getOutputs())) {

				// Grab the DAO for DataContainers
				DataContainerDAO dataContainerDAO = new DataContainerDAO(this
						.getSession());

				// Make sure the are outputs to iterate over
				if (dataProducer.getOutputs().size() > 0) {

					// Now iterate over the outputs and persist them
					Iterator outputIter = dataProducer.getOutputs().iterator();
					while (outputIter.hasNext()) {
						DataContainer tempOutput = (DataContainer) outputIter
								.next();
						dataContainerDAO.makePersistent(tempOutput);
					}
				}

				// Create a copy of the collection associated with the
				// dataProducer to prevent concurrent modifications
				Collection dataProducerOutputCopy = new ArrayList(dataProducer
						.getOutputs());

				// Now we need to make the correct associations. Currently, you
				// have a collection of DataContainer objects that have their
				// values
				// marked for persistence. Now the object will either be in the
				// session or not depending on if they were previously
				// persisted.
				Iterator dataProducerOutputCopyIterator = dataProducerOutputCopy
						.iterator();
				while (dataProducerOutputCopyIterator.hasNext()) {
					DataContainer currentOutput = (DataContainer) dataProducerOutputCopyIterator
							.next();
					DataContainer currentOutputInSession = null;
					// Is this output already in the session?
					if (!getSession().contains(currentOutput)) {
						// No, so grab the one that is
						currentOutputInSession = (DataContainer) dataContainerDAO
								.findEquivalentPersistentObject(currentOutput,
										false);
					} else {
						currentOutputInSession = currentOutput;
					}
					// Now if the parent dataProducer was persisted before,
					// just check to make sure the sessioned outputs in the
					// collection are associated with the dataProducer that
					// will be persisted
					if (persistedBefore) {
						if (!dataProducerToPersist.getOutputs().contains(
								currentOutputInSession))
							dataProducerToPersist
									.addOutput(currentOutputInSession);
					} else {
						// This means that the dataProducer has not been
						// persisted before. If the Output is already in the
						// session, there is nothing to do, but if not, we need
						// to replace it with the sessioned one
						if (!getSession().contains(currentOutput)) {
							dataProducerToPersist.removeOutput(currentOutput);
							dataProducerToPersist
									.addOutput(currentOutputInSession);
						}
					}
				}
			}
		}

		// -------------------------------
		// Child DataProducer Relationship
		// -------------------------------
		// First make sure the childDataProducer relationship exists
		if (dataProducer.getChildDataProducers() != null) {
			// Now check to make sure it is initialized. If it is not, no
			// changes have taken place and we don't need to do anything
			if (Hibernate.isInitialized(dataProducer.getChildDataProducers())) {

				// Make sure there are childDataProducers to iterate over
				if (dataProducer.getChildDataProducers().size() > 0) {

					// Create a copy of the collection associated with the
					// dataProducer to prevent concurrent modifications
					Collection dataProducerChildrenCopy = new ArrayList(
							dataProducer.getChildDataProducers());

					// Now we need to iterate over the child data producers and
					// make sure the relationship to the parent is upheld
					Iterator dataProducerChildrenCopyIterator = dataProducerChildrenCopy
							.iterator();
					while (dataProducerChildrenCopyIterator.hasNext()) {
						// Grab the child data producer
						DataProducer currentChild = (DataProducer) dataProducerChildrenCopyIterator
								.next();

						// If the child's parent is in the session already there
						// is nothing to be done, but if the parent is not in
						// the session and the parent has been persisted before,
						// we need to make sure the child is moved over before
						// it is persisted
						if (!getSession().contains(dataProducer)) {
							if (persistedBefore) {
								// Remove the child from the non-persisted
								// parent
								dataProducer
										.removeChildDataProducer(currentChild);
								// Check to see if the child is already a child
								// of the persisted data producer
								if (!persistentDataProducer
										.getChildDataProducers().contains(
												currentChild)) {
									persistentDataProducer
											.addChildDataProducer(currentChild);
								}
							} else {
								logger
										.error("The child's parent is not in the Hibernate "
												+ "session and the parent was not persisted before, this "
												+ "will cause an Exception when the child is persisted!");
							}
						}

						// Now call make the child persistent
						this.makePersistent(currentChild);

						// DataProducer currentChildInSession = null;
						// // Is this dataProducer already in the session?
						// if (!getSession().contains(currentChild)) {
						// // No, so grab the one that is
						// currentChildInSession = (DataProducer) this
						// .findEquivalentPersistentObject(currentChild,
						// false);
						// } else {
						// currentChildInSession = currentChild;
						// }
						// // Now if the parent dataProducer was persisted
						// before,
						// // just check to make sure the sessioned children in
						// the
						// // collection are associated with the dataProducer
						// that
						// // will be persisted
						// if (persistedBefore) {
						// if (!dataProducerToPersist.getChildDataProducers()
						// .contains(currentChildInSession))
						// // dataProducerToPersist.getChildDataProducers().add(
						// // currentChildInSession);
						// dataProducerToPersist
						// .addChildDataProducer(currentChildInSession);
						// } else {
						// // This means that the dataProducer has not been
						// // persisted before. If the child is already in the
						// // session, there is nothing to do, but if not, we
						// // need
						// // to replace it with the sessioned one
						// if (!getSession().contains(currentChild)) {
						// // dataProducerToPersist.getChildDataProducers()
						// // .remove(currentChild);
						// // dataProducerToPersist.getChildDataProducers().add(
						// // currentChildInSession);
						// dataProducerToPersist
						// .removeChildDataProducer(currentChild);
						// dataProducerToPersist
						// .addChildDataProducer(currentChildInSession);
						// }
						// }
					}
				}
			}
		}

		// Return the ID
		if (dataProducerToPersist != null) {
			return dataProducerToPersist.getId();
		} else {
			return null;
		}
	}

	/**
	 * @see IMetadataDAO#makeTransient(IMetadataObject)
	 */
	public void makeTransient(IMetadataObject metadataObject)
			throws MetadataAccessException {
		logger.debug("makeTransient called");

		// Check incoming object
		DataProducer dataProducer = this
				.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		DataProducer persistentDataProducer = (DataProducer) this
				.findEquivalentPersistentObject(dataProducer, false);

		// If no matching dataProducer was found, do nothing
		if (persistentDataProducer == null) {
			logger
					.debug("No matching dataProducer could be found in the persistent store, "
							+ "no delete performed");
		} else {
			// Handle the relationships
			persistentDataProducer.setPerson(null);
			persistentDataProducer.setDevice(null);
			persistentDataProducer.setSoftware(null);
			persistentDataProducer.clearDataProducerGroups();
			persistentDataProducer.clearEvents();
			persistentDataProducer.clearResources();
			persistentDataProducer.clearKeywords();
			persistentDataProducer.clearInputs();
			persistentDataProducer.clearOutputs();
			persistentDataProducer.clearChildDataProducers();

			logger
					.debug("Existing object was found, so we will try to delete it");
			try {
				getSession().delete(persistentDataProducer);
			} catch (HibernateException e) {
				logger.error("HibernateException caught (will be re-thrown):"
						+ e.getMessage());
				throw new MetadataAccessException(e);
			}
		}
	}

	/**
	 * This method does a &quot;Deep&quot; transient. This effectively removes
	 * all the dependent entitites along with the <code>DataProducer</code> that
	 * is passed in. The dependent entities that will also be made transient
	 * are:
	 * <ol>
	 * <li>Outputs (DataContainers)</li>
	 * <li>Resources</li>
	 * <li>Events</li>
	 * <li>ChildDataProducers</li>
	 * </ol>
	 * The child DataProducers will also be made deep transient
	 * 
	 * @param dataProducer
	 *            is the top <code>DataProducer</code> that will be made
	 *            transient along with the dependend entitites
	 * @throws MetadataAccessException
	 *             if something goes haywire
	 */
	public void makeDeepTransient(DataProducer dataProducer)
			throws MetadataAccessException {

		// Check the persistent store for the matching object
		DataProducer persistentDataProducer = (DataProducer) this
				.findEquivalentPersistentObject(dataProducer, false);

		// If no matching dataProducer was found, do nothing
		if (persistentDataProducer == null) {
			logger
					.debug("No matching dataProducer could be found in the persistent store, "
							+ "no deep delete performed");
		} else {
			// Grab all the object that need to be made transient
			Collection outputs = new ArrayList(persistentDataProducer
					.getOutputs());
			Collection events = new ArrayList(persistentDataProducer
					.getEvents());
			Collection resources = new ArrayList(persistentDataProducer
					.getResources());
			Collection childDataProducers = new ArrayList(
					persistentDataProducer.getChildDataProducers());

			// Now make the current DataProducer transient
			this.makeTransient(persistentDataProducer);

			// Now walk all the relationships and make them transient
			if ((outputs != null) && (outputs.size() > 0)) {
				logger
						.debug("There are initialized outputs, so will make those transient");
				DataContainerDAO dataContainerDAO = new DataContainerDAO(this
						.getSession());
				Iterator outputIter = outputs.iterator();
				while (outputIter.hasNext()) {
					DataContainer tempDC = (DataContainer) outputIter.next();
					logger.debug("tempDC before transient = "
							+ tempDC.toStringRepresentation("|"));
					dataContainerDAO.makeTransient(tempDC);
					logger.debug("tempDC after transient = "
							+ tempDC.toStringRepresentation("|"));
				}
			}
			// Grab any events
			if ((events != null) && (events.size() > 0)) {
				logger
						.debug("There were some initialized events, so will make those transient");
				EventDAO eventDAO = new EventDAO(this.getSession());
				Iterator eventIter = events.iterator();
				while (eventIter.hasNext()) {
					eventDAO.makeTransient((Event) eventIter.next());
				}
			}
			// Grab the resources
			if ((resources != null) && (resources.size() > 0)) {
				logger
						.debug("There were some initialized resources, so will make those transient");
				ResourceDAO resourceDAO = new ResourceDAO(this.getSession());
				Iterator resourceIter = resources.iterator();
				while (resourceIter.hasNext()) {
					resourceDAO.makeTransient((Resource) resourceIter.next());
				}
			}
			// Grab the child data producers
			if ((childDataProducers != null) && (childDataProducers.size() > 0)) {
				logger
						.debug("There were some initialized childDataProducers, so will make those deep transient also");
				Iterator childDataProducerIter = childDataProducers.iterator();
				while (childDataProducerIter.hasNext()) {
					this.makeDeepTransient((DataProducer) childDataProducerIter
							.next());
				}
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
	 *            <code>DataProducer</code>
	 * @return a <code>DataProducer</code> that is same object that came in
	 * @throws MetadataAccessException
	 *             if something is wrong
	 */
	private DataProducer checkIncomingMetadataObject(
			IMetadataObject metadataObject) throws MetadataAccessException {

		// Check for null argument
		if (metadataObject == null) {
			throw new MetadataAccessException(
					"Failed: incoming DataProducer was null");
		}

		// Try to cast the incoming object into the correct class
		DataProducer dataProducer = null;
		try {
			dataProducer = (DataProducer) metadataObject;
		} catch (ClassCastException cce) {
			throw new MetadataAccessException(
					"Could not cast the incoming object into a DataProducer");
		}
		return dataProducer;
	}

	/**
	 * TODO kgomes document this
	 * 
	 * @param countQuery
	 * @param id
	 * @param name
	 * @param exactNameMatch
	 * @param dataProducerType
	 * @param startDate
	 * @param boundedByStartDate
	 * @param endDate
	 * @param boundedByEndDate
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialDepthMin
	 * @param geospatialDepthMax
	 * @param geospatialBenthicAltitudeMin
	 * @param geospatialBenthicAltitudeMax
	 * @param hostName
	 * @param exactHostNameMatch
	 * @param orderByProperty
	 * @param ascendOrDescend
	 * @return
	 * @throws MetadataAccessException
	 */
	private Criteria formulatePropertyCriteria(boolean countQuery, Long id,
			String name, boolean exactNameMatch, String dataProducerType,
			Date startDate, boolean boundedByStartDate, Date endDate,
			boolean boundedByEndDate, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialDepthMin,
			Float geospatialDepthMax, Float geospatialBenthicAltitudeMin,
			Float geospatialBenthicAltitudeMax, String hostName,
			boolean exactHostNameMatch, String orderByProperty,
			String ascendOrDescend) throws MetadataAccessException {
		// The Criteria to return
		Criteria criteria = getSession().createCriteria(DataProducer.class);
		// Make it distinct
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		// Check for exceptional conditions on the query
		if ((dataProducerType != null)
				&& (!DataProducer.isValidDataProducerType(dataProducerType)))
			throw new MetadataAccessException(
					"The dataProducerType ("
							+ dataProducerType
							+ ") does not match a constant defined in the DataProducer class");
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
		if ((geospatialBenthicAltitudeMin != null)
				&& (geospatialBenthicAltitudeMax != null))
			if (geospatialBenthicAltitudeMax.doubleValue() < geospatialBenthicAltitudeMin
					.doubleValue())
				throw new MetadataAccessException(
						"The benthic altitude maximum specified was less than the minimum.");
		if ((startDate != null) && (endDate != null)
				&& (endDate.before(startDate)))
			throw new MetadataAccessException("The end date specified ("
					+ endDate + ") is before the start date specified ("
					+ startDate + ")");

		// Now build the Criteria
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
			if (dataProducerType != null) {
				criteria.add(Restrictions.eq("dataProducerType",
						dataProducerType));
			}
			if (startDate != null) {
				criteria.add(Restrictions.or(Restrictions.gt("endDate",
						startDate), Restrictions.isNull("endDate")));
				if (boundedByStartDate) {
					criteria.add(Restrictions.gt("startDate", startDate));
				}
			}
			if (endDate != null) {
				criteria.add(Restrictions.or(Restrictions.lt("startDate",
						endDate), Restrictions.isNull("startDate")));
				if (boundedByEndDate) {
					criteria.add(Restrictions.lt("endDate", endDate));
				}
			}
			if (geospatialLatMin != null)
				criteria.add(Restrictions.ge("nominalLatitude",
						geospatialLatMin));

			if (geospatialLatMax != null)
				criteria.add(Restrictions.le("nominalLatitude",
						geospatialLatMax));

			if (geospatialLonMin != null)
				criteria.add(Restrictions.ge("nominalLongitude",
						geospatialLonMin));

			if (geospatialLonMax != null)
				criteria.add(Restrictions.le("nominalLongitude",
						geospatialLonMax));

			if (geospatialDepthMin != null)
				criteria.add(Restrictions
						.le("nominalDepth", geospatialDepthMin));

			if (geospatialDepthMax != null)
				criteria.add(Restrictions
						.ge("nominalDepth", geospatialDepthMax));

			if (geospatialBenthicAltitudeMin != null)
				criteria.add(Restrictions.ge("benthicAltitude",
						geospatialBenthicAltitudeMin));

			if (geospatialBenthicAltitudeMax != null)
				criteria.add(Restrictions.lt("benthicAltitude",
						geospatialBenthicAltitudeMax));
			if ((hostName != null) && (!hostName.equals(""))) {
				if (exactHostNameMatch) {
					criteria.add(Restrictions.eq("hostName", hostName));
				} else {
					criteria.add(Restrictions.like("hostName", "%" + hostName
							+ "%"));
				}
			}
		}
		// Setup if a count query, if not add fetching and ordering
		if (countQuery) {
			criteria.setProjection(Projections.rowCount());
		} else {
			addOrderByCriteria(criteria, orderByProperty, ascendOrDescend);
		}
		// Now return the Criteria
		return criteria;
	}

	protected void initializeRelationships(IMetadataObject metadataObject)
			throws MetadataAccessException {

		// If the object is null, just return
		if (metadataObject == null)
			return;

		// First try to cast into DataProducer
		DataProducer dataProducer = this
				.checkIncomingMetadataObject(metadataObject);

		if (dataProducer.getPerson() != null)
			Hibernate.initialize(dataProducer.getPerson());
		if (dataProducer.getDevice() != null)
			Hibernate.initialize(dataProducer.getDevice());
		if (dataProducer.getSoftware() != null)
			Hibernate.initialize(dataProducer.getSoftware());
		if (dataProducer.getParentDataProducer() != null)
			Hibernate.initialize(dataProducer.getParentDataProducer());
		if (dataProducer.getChildDataProducers() != null) {
			logger.debug("There are "
					+ dataProducer.getChildDataProducers().size()
					+ " child data producers, so will initialize them");
			Iterator childDataProducerIterator = dataProducer
					.getChildDataProducers().iterator();
			while (childDataProducerIterator.hasNext()) {
				Hibernate.initialize((DataProducer) childDataProducerIterator
						.next());
				logger.debug("Initialized ...");
			}
		}
		if (dataProducer.getDataProducerGroups() != null) {
			Iterator dataProducerGroupsIterator = dataProducer
					.getDataProducerGroups().iterator();
			while (dataProducerGroupsIterator.hasNext()) {
				Hibernate
						.initialize((DataProducerGroup) dataProducerGroupsIterator
								.next());
			}
		}
		if (dataProducer.getInputs() != null) {
			Iterator inputIterator = dataProducer.getInputs().iterator();
			while (inputIterator.hasNext()) {
				Hibernate.initialize((DataContainer) inputIterator.next());
			}
		}
		if (dataProducer.getOutputs() != null) {
			Iterator outputIterator = dataProducer.getOutputs().iterator();
			while (outputIterator.hasNext()) {
				DataContainer dcToInitialize = (DataContainer) outputIterator
						.next();
				Hibernate.initialize(dcToInitialize);
				if (dcToInitialize.getRecordDescription() != null) {
					Hibernate.initialize(dcToInitialize.getRecordDescription());
				}
				if (dcToInitialize.getRecordVariables() != null) {
					Iterator rvIter = dcToInitialize.getRecordVariables()
							.iterator();
					while (rvIter.hasNext()) {
						Hibernate.initialize((RecordVariable) rvIter.next());
					}
				}
			}
		}
		if (dataProducer.getResources() != null) {
			logger.debug("There are some resources to initialize ("
					+ dataProducer.getResources().size() + " of them)");
			Iterator resourceIterator = dataProducer.getResources().iterator();
			while (resourceIterator.hasNext()) {
				Resource resource = (Resource) resourceIterator.next();
				logger.debug("Resource " + resource.toStringRepresentation("|")
						+ " initialized");
				Hibernate.initialize(resource);
			}
		}
		if (dataProducer.getKeywords() != null) {
			Iterator keywordIterator = dataProducer.getKeywords().iterator();
			while (keywordIterator.hasNext()) {
				Hibernate.initialize((Keyword) keywordIterator.next());
			}
		}
		if (dataProducer.getEvents() != null) {
			Iterator eventIterator = dataProducer.getEvents().iterator();
			while (eventIterator.hasNext()) {
				Hibernate.initialize((Event) eventIterator.next());
			}
		}
	}

	/**
	 * The Log4J Logger
	 */
	static Logger logger = Logger.getLogger(DataProducerDAO.class);
}
