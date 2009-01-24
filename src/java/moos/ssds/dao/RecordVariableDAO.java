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
import java.util.HashSet;

import moos.ssds.dao.util.MetadataAccessException;

import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.RecordVariable;
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

public class RecordVariableDAO extends MetadataDAO {

	/**
	 * This is the constructor that calls the super constructor and sets the
	 * proper class and Hibernate Session
	 * 
	 * @param session
	 *            is the <code>Session</code> that will be used in the
	 *            persistent operations
	 * @throws MetadataAccessException
	 *             if something goes weird
	 */
	public RecordVariableDAO(Session session) throws MetadataAccessException {
		super(RecordVariable.class, session);
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
		RecordVariable recordVariable = this
				.checkIncomingMetadataObject(metadataObject);

		// The DataProducer to return (if found)
		RecordVariable recordVariableToReturn = null;

		// The id that will be used in the search
		Long idToSearchFor = recordVariable.getId();

		// If the ID is specified, do a look up for the object
		if ((idToSearchFor != null) && (idToSearchFor.longValue() > 0)) {
			// Now grab the Criteria query
			Criteria criteria = this.formulatePropertyCriteria(false,
					idToSearchFor, null, false, null, null);
			recordVariableToReturn = (RecordVariable) criteria.uniqueResult();
		}

		// If the recordVariable was not found then return null
		// As RecordVariables are typically cascaded from DataContainers there
		// is little need
		// for business logic to find a RecordVariable that does no
		if (recordVariableToReturn == null) {
			return null;
		}

		// Check to see if the graph is requested
		if (returnFullObjectGraph)
			this.initializeRelationships(recordVariableToReturn);

		// Return the result
		return recordVariableToReturn;
	}

	/**
	 * @see IMetadataDAO#findAllIDs()
	 */
	public Collection findAllIDs() throws MetadataAccessException {
		return null;
	}

	/**
	 * @see IMetadataDAO#countFindAllIDs()
	 */
	public int countFindAllIDs() throws MetadataAccessException {
		// The count
		int count = 0;
		// Return the result
		return count;
	}

	/**
	 * This method returns all <code>RecordVariable</code>s that are
	 * associated with the input <code>StandardUnit</code>
	 * 
	 * @param standardUnit
	 * @param orderByProperty
	 * @param ascendingOrDescending
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByStandardUnit(StandardUnit standardUnit,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// The Collection to return
		Collection recordVariablesByStandardUnit = new HashSet();

		// First check to make sure the standard unit is not null
		if (standardUnit == null)
			return recordVariablesByStandardUnit;

		// See if the incoming StandardUnit has an ID, if not, look it up
		Long standardUnitId = standardUnit.getId();
		if ((standardUnit.getId() == null)
				|| (standardUnit.getId().longValue() <= 0)) {
			// Grab the DAO
			StandardUnitDAO standardUnitDAO = new StandardUnitDAO(getSession());

			// Grab the ID
			standardUnitId = standardUnitDAO.findId(standardUnit);
		}

		// If no persistent standardUnit found, return an empty set
		if ((standardUnitId == null) || (standardUnitId.longValue() <= 0)) {
			return recordVariablesByStandardUnit;
		}

		// The query string
		StringBuffer sqlStringBuffer = new StringBuffer();

		// Now create the query
		Query query = null;

		sqlStringBuffer
				.append("select distinct recordVariable from "
						+ "RecordVariable recordVariable where recordVariable.standardUnit.id = '"
						+ standardUnitId.toString() + "'");

		// Add any extra query information
		if (checkIfPropertyOK(orderByPropertyName)) {
			sqlStringBuffer.append(this.getOrderByPropertyNameSQLClause(
					orderByPropertyName, ascendingOrDescending));
		}

		// Run the query
		try {
			query = this.getSession().createQuery(sqlStringBuffer.toString());
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Grab the results
		recordVariablesByStandardUnit = query.list();

		// Check for full graph request
		if (returnFullObjectGraph)
			initializeRelationships(recordVariablesByStandardUnit);

		// Now return the results
		return recordVariablesByStandardUnit;
	}

	/**
	 * This method returns all <code>RecordVariable</code>s that are
	 * associated with the input <code>StandardVariable</code>
	 * 
	 * @param standardVariable
	 * @param orderByProperty
	 * @param ascendingOrDescending
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByStandardVariable(StandardVariable standardVariable,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// The Collection to return
		Collection recordVariablesByStandardVariable = new HashSet();

		// First check to make sure the standard variable is not null
		if (standardVariable == null)
			return recordVariablesByStandardVariable;

		// See if the incoming StandardVariable has an ID, if not, look it up
		Long standardVariableId = standardVariable.getId();
		if ((standardVariable.getId() == null)
				|| (standardVariable.getId().longValue() <= 0)) {
			// Grab the DAO
			StandardVariableDAO standardVariableDAO = new StandardVariableDAO(
					getSession());

			// Grab the ID
			standardVariableId = standardVariableDAO.findId(standardVariable);
		}

		// If no persistent standardVariable found, return an empty set
		if ((standardVariableId == null)
				|| (standardVariableId.longValue() <= 0)) {
			return recordVariablesByStandardVariable;
		}

		// The query string
		StringBuffer sqlStringBuffer = new StringBuffer();

		// Now create the query
		Query query = null;

		sqlStringBuffer
				.append("select distinct recordVariable from "
						+ "RecordVariable recordVariable where recordVariable.standardVariable.id = '"
						+ standardVariableId.toString() + "'");

		// Add any extra query information
		if (checkIfPropertyOK(orderByPropertyName)) {
			sqlStringBuffer.append(this.getOrderByPropertyNameSQLClause(
					orderByPropertyName, ascendingOrDescending));
		}

		// Run the query
		try {
			query = this.getSession().createQuery(sqlStringBuffer.toString());
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Grab the results
		recordVariablesByStandardVariable = query.list();

		// Check for full graph request
		if (returnFullObjectGraph)
			initializeRelationships(recordVariablesByStandardVariable);

		// Now return the results
		return recordVariablesByStandardVariable;
	}

	/**
	 * This method returns all <code>RecordVariable</code>s that are
	 * associated with the input <code>StandardDomain</code>
	 * 
	 * @param standardDomain
	 * @param orderByProperty
	 * @param ascendingOrDescending
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByStandardDomain(StandardDomain standardDomain,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// The Collection to return
		Collection recordVariablesByStandardDomain = new HashSet();

		// First check to make sure the standard domain is not null
		if (standardDomain == null)
			return recordVariablesByStandardDomain;

		// See if the incoming StandardDomain has an ID, if not, look it up
		Long standardDomainId = standardDomain.getId();
		if ((standardDomain.getId() == null)
				|| (standardDomain.getId().longValue() <= 0)) {
			// Grab the DAO
			StandardDomainDAO standardDomainDAO = new StandardDomainDAO(
					getSession());

			// Grab the ID
			standardDomainId = standardDomainDAO.findId(standardDomain);
		}

		// If no persistent standardDomain found, return an empty set
		if ((standardDomainId == null) || (standardDomainId.longValue() <= 0)) {
			return recordVariablesByStandardDomain;
		}

		// The query string
		StringBuffer sqlStringBuffer = new StringBuffer();

		// Now create the query
		Query query = null;

		sqlStringBuffer
				.append("select distinct recordVariable from "
						+ "RecordVariable recordVariable where recordVariable.standardDomain.id = '"
						+ standardDomainId.toString() + "'");

		// Add any extra query information
		if (checkIfPropertyOK(orderByPropertyName)) {
			sqlStringBuffer.append(this.getOrderByPropertyNameSQLClause(
					orderByPropertyName, ascendingOrDescending));
		}

		// Run the query
		try {
			query = this.getSession().createQuery(sqlStringBuffer.toString());
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Grab the results
		recordVariablesByStandardDomain = query.list();

		// Check for full graph request
		if (returnFullObjectGraph)
			initializeRelationships(recordVariablesByStandardDomain);

		// Now return the results
		return recordVariablesByStandardDomain;
	}

	/**
	 * This method returns all <code>RecordVariable</code>s that are
	 * associated with the input <code>StandardReferenceScale</code>
	 * 
	 * @param standardReferenceScale
	 * @param orderByProperty
	 * @param ascendingOrDescending
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByStandardReferenceScale(
			StandardReferenceScale standardReferenceScale,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// The Collection to return
		Collection recordVariablesByStandardReferenceScale = new HashSet();

		// First check to make sure the standard referenceScale is not null
		if (standardReferenceScale == null)
			return recordVariablesByStandardReferenceScale;

		// See if the incoming StandardReferenceScale has an ID, if not, look it
		// up
		Long standardReferenceScaleId = standardReferenceScale.getId();
		if ((standardReferenceScale.getId() == null)
				|| (standardReferenceScale.getId().longValue() <= 0)) {
			// Grab the DAO
			StandardReferenceScaleDAO standardReferenceScaleDAO = new StandardReferenceScaleDAO(
					getSession());

			// Grab the ID
			standardReferenceScaleId = standardReferenceScaleDAO
					.findId(standardReferenceScale);
		}

		// If no persistent standardReferenceScale found, return an empty set
		if ((standardReferenceScaleId == null)
				|| (standardReferenceScaleId.longValue() <= 0)) {
			return recordVariablesByStandardReferenceScale;
		}

		// The query string
		StringBuffer sqlStringBuffer = new StringBuffer();

		// Now create the query
		Query query = null;

		sqlStringBuffer
				.append("select distinct recordVariable from "
						+ "RecordVariable recordVariable where recordVariable.standardReferenceScale.id = '"
						+ standardReferenceScaleId.toString() + "'");

		// Add any extra query information
		if (checkIfPropertyOK(orderByPropertyName)) {
			sqlStringBuffer.append(this.getOrderByPropertyNameSQLClause(
					orderByPropertyName, ascendingOrDescending));
		}

		// Run the query
		try {
			query = this.getSession().createQuery(sqlStringBuffer.toString());
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Grab the results
		recordVariablesByStandardReferenceScale = query.list();

		// Check for full graph request
		if (returnFullObjectGraph)
			initializeRelationships(recordVariablesByStandardReferenceScale);

		// Now return the results
		return recordVariablesByStandardReferenceScale;
	}

	/**
	 * This method returns all <code>RecordVariable</code>s that are
	 * associated with the input <code>StandardKeyword</code>
	 * 
	 * @param standardKeyword
	 * @param orderByProperty
	 * @param ascendingOrDescending
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByStandardKeyword(StandardKeyword standardKeyword,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// The Collection to return
		Collection recordVariablesByStandardKeyword = new HashSet();

		// First check to make sure the standard keyword is not null
		if (standardKeyword == null)
			return recordVariablesByStandardKeyword;

		// See if the incoming StandardKeyword has an ID, if not, look it up
		Long standardKeywordId = standardKeyword.getId();
		if ((standardKeyword.getId() == null)
				|| (standardKeyword.getId().longValue() <= 0)) {
			// Grab the DAO
			StandardKeywordDAO standardKeywordDAO = new StandardKeywordDAO(
					getSession());

			// Grab the ID
			standardKeywordId = standardKeywordDAO.findId(standardKeyword);
		}

		// If no persistent standardKeyword found, return an empty set
		if ((standardKeywordId == null) || (standardKeywordId.longValue() <= 0)) {
			return recordVariablesByStandardKeyword;
		}

		// The query string
		StringBuffer sqlStringBuffer = new StringBuffer();

		// Now create the query
		Query query = null;

		sqlStringBuffer
				.append("select distinct recordVariable from "
						+ "RecordVariable recordVariable where recordVariable.standardKeyword.id = '"
						+ standardKeywordId.toString() + "'");

		// Add any extra query information
		if (checkIfPropertyOK(orderByPropertyName)) {
			sqlStringBuffer.append(this.getOrderByPropertyNameSQLClause(
					orderByPropertyName, ascendingOrDescending));
		}

		// Run the query
		try {
			query = this.getSession().createQuery(sqlStringBuffer.toString());
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Grab the results
		recordVariablesByStandardKeyword = query.list();

		// Check for full graph request
		if (returnFullObjectGraph)
			initializeRelationships(recordVariablesByStandardKeyword);

		// Now return the results
		return recordVariablesByStandardKeyword;
	}

	/**
	 * @see IMetadataDAO#makePersistent(IMetadataObject)
	 */
	public Long makePersistent(IMetadataObject metadataObject)
			throws MetadataAccessException {

		logger.debug("makePersistent called");

		// A flag to indicate if the object was persisted in the past
		boolean persistedBefore = false;

		// Check incoming variable
		RecordVariable recordVariable = this
				.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		RecordVariable persistentRecordVariable = (RecordVariable) this
				.findEquivalentPersistentObject(metadataObject, false);

		// The handle to the actual object that will be persisted
		RecordVariable recordVariableToPersist = null;

		// Copy over any non-null, changed values to the persistent object (if
		// it exists
		if (persistentRecordVariable != null) {
			String recordVariableBeforeUpdate = persistentRecordVariable
					.toStringRepresentation("<li>");
			if (this.updateDestinationObject(recordVariable,
					persistentRecordVariable)) {
				addMessage(ssdsAdminEmailToAddress,
						"A RecordVariable was changed in SSDS<br><b>Before</b><ul><li>"
								+ recordVariableBeforeUpdate
								+ "</ul><br><b>After</b><ul><li>"
								+ persistentRecordVariable
										.toStringRepresentation("<li>")
								+ "</ul><br>");
			}

			// Set the flag
			persistedBefore = true;

			// Assign to the handle
			recordVariableToPersist = persistentRecordVariable;
		} else {

		}

		// -------------------
		// StandardVariable Relationship
		// -------------------
		// First see if there is a standardVariable associated with the incoming
		// recordVariable
		if (recordVariable.getStandardVariable() != null) {
			// Now, since there is, check to see if it has been initialized
			if (Hibernate.isInitialized(recordVariable.getStandardVariable())) {
				// Grab the StandardVariable DAO to handle that relationship
				StandardVariableDAO standardVariableDAO = new StandardVariableDAO(
						this.getSession());

				// Now persist the standardVariable
				StandardVariable tempStandardVariable = recordVariable
						.getStandardVariable();
				standardVariableDAO.makePersistent(tempStandardVariable);

				// The matching standardVariable that is in the session
				StandardVariable tempStandardVariableInSession = null;

				// Check to see if the persisted standardVariable is in the
				// session
				if (!getSession().contains(tempStandardVariable)) {
					tempStandardVariableInSession = (StandardVariable) standardVariableDAO
							.findEquivalentPersistentObject(
									tempStandardVariable, false);
				} else {
					tempStandardVariableInSession = tempStandardVariable;
				}

				// Now check to see if the recordVariable was persisted in the
				// past, if
				// so, just check to see if recordVariables standardVariable is
				// different and
				// update it if so
				if (persistedBefore) {
					if ((recordVariableToPersist.getStandardVariable() == null)
							|| (!recordVariableToPersist.getStandardVariable()
									.equals(tempStandardVariableInSession))) {
						recordVariableToPersist
								.setStandardVariable(tempStandardVariableInSession);
					}
				} else {
					// Make sure the standardVariable associated with the
					// recordVariable is the
					// session, if not replace it with the one that is
					if (!getSession().contains(
							recordVariableToPersist.getStandardVariable())) {
						recordVariableToPersist
								.setStandardVariable(tempStandardVariableInSession);
					}
				}
			}
		}

		// -------------------------
		// StandardUnit Relationship
		// -------------------------
		// First make sure the standardUnit relationship exists

		// If the RecordVariable was not persisted before, save it
		if (!persistedBefore) {
			getSession().save(recordVariableToPersist);
			addMessage(ssdsAdminEmailToAddress,
					"A new RecordVariable was added to SSDS<br><ul><li>"
							+ recordVariableToPersist
									.toStringRepresentation("<li>")
							+ "</ul><br>");
		}

		// Now return the ID
		if (recordVariableToPersist != null) {
			return recordVariableToPersist.getId();
		} else {
			return null;
		}
	}

	/**
	 * @see IMetadataDAO#makeTransient(IMetadataObject)
	 */
	public void makeTransient(IMetadataObject metadataObject)
			throws MetadataAccessException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see MetadataDAO#initializeRelationships(IMetadataObject)
	 */
	protected void initializeRelationships(IMetadataObject metadataObject)
			throws MetadataAccessException {
		if (metadataObject == null)
			return;

		// Convert to RecordVariable
		RecordVariable recordVariable = this
				.checkIncomingMetadataObject(metadataObject);

		// Initialize all the down-wind relationships
		if (recordVariable.getStandardUnit() != null)
			Hibernate.initialize(recordVariable.getStandardUnit());
		if (recordVariable.getStandardVariable() != null)
			Hibernate.initialize(recordVariable.getStandardVariable());
		if (recordVariable.getStandardDomain() != null)
			Hibernate.initialize(recordVariable.getStandardDomain());
		if (recordVariable.getStandardReferenceScale() != null)
			Hibernate.initialize(recordVariable.getStandardReferenceScale());
		if (recordVariable.getStandardKeyword() != null)
			Hibernate.initialize(recordVariable.getStandardKeyword());

	}

	/**
	 * This method checks to make sure an incoming <code>MetadataObject</code>
	 * is not null and is in fact of the correct class. It then converts it to
	 * the correct class and returns it
	 * 
	 * @param metadataObject
	 *            the <code>MetadataObject</code> to check and return as a
	 *            <code>RecordVariable</code>
	 * @return a <code>RecordVariable</code> that is same object that came in
	 * @throws MetadataAccessException
	 *             if something is wrong
	 */
	private RecordVariable checkIncomingMetadataObject(
			IMetadataObject metadataObject) throws MetadataAccessException {

		// Check for null argument
		if (metadataObject == null) {
			throw new MetadataAccessException(
					"Failed: incoming RecordVariable was null");
		}

		// Try to cast the incoming object into the correct class
		RecordVariable recordVariable = null;
		try {
			recordVariable = (RecordVariable) metadataObject;
		} catch (ClassCastException cce) {
			throw new MetadataAccessException(
					"Could not cast the incoming object into a RecordVariable");
		}
		return recordVariable;
	}

	/**
	 * This method returns a <code>Collection</code> of <code>String</code>s
	 * that are the names of all the RecordVariables that are in SSDS.
	 * 
	 * @return a <code>Collection</code> of <code>String</code>s that are
	 *         the names of all RecordVariables in SSDS.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public Collection findAllNames() throws MetadataAccessException {
		Collection recordVariableNames = null;

		// Create the query and run it
		try {
			Query query = getSession()
					.createQuery(
							"select distinct recordVariable.name from "
									+ "RecordVariable recordVariable order by recordVariable.name");
			recordVariableNames = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return recordVariableNames;
	}

	public int countFindAllNames() throws MetadataAccessException {
		int count = 0;
		// Create the query and run it
		try {
			Long integerCount = (Long) getSession().createQuery(
					"select count(distinct recordVariable.name) from "
							+ "RecordVariable recordVariable").uniqueResult();
			if (integerCount != null)
				count = integerCount.intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return count;
	}

	/**
	 * This method returns all recordVariables that match the given name exactly
	 * 
	 * @param name
	 * @param orderByPropertyName
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByName(String name, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// The recordVariables to return
		Collection recordVariablesToReturn = new ArrayList();

		// If no name was specified just return an empty collection
		if ((name == null) || (name.equals("")))
			return recordVariablesToReturn;

		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					name, exactMatch, orderByPropertyName,
					ascendingOrDescending);
			recordVariablesToReturn = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Check for relationship init
		if (returnFullObjectGraph)
			this.initializeRelationships(recordVariablesToReturn);

		// Now return the results
		return recordVariablesToReturn;
	}

	private Criteria formulatePropertyCriteria(boolean countQuery, Long id,
			String name, boolean exactNameMatch, String orderByProperty,
			String ascendOrDescend) throws MetadataAccessException {
		// The Criteria to return
		Criteria criteria = getSession().createCriteria(RecordVariable.class);
		// Make it distinct
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

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

	/**
	 * The Log4J Logger
	 */
	static Logger logger = Logger.getLogger(DataProducerDAO.class);

}
