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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.StandardUnit;
import moos.ssds.metadata.StandardVariable;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * This is the Data Access Object that provides functionality to support the
 * persitence of <code>StandardVariable</code> objects
 * <hr>
 * 
 * @stereotype service
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.20 $
 */
public class StandardVariableDAO extends MetadataDAO {

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
	public StandardVariableDAO(Session session) throws MetadataAccessException {
		// Construct the parent DAO with the correct class and session
		super(StandardVariable.class, session);
	}

	/**
	 * @see IMetadataDAO#findEquivalentPersistentObject(IMetadataObject)
	 */
	public IMetadataObject findEquivalentPersistentObject(
			IMetadataObject metadataObject, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// First try to cast to a StandardVariable
		StandardVariable standardVariable = this
				.checkIncomingMetadataObject(metadataObject);

		// The StandardVariable to return
		StandardVariable standardVariableToReturn = null;

		// First try to find by id
		if (standardVariable.getId() != null)
			standardVariableToReturn = (StandardVariable) this.findById(
					standardVariable.getId(), false);

		// If not found, try to find by name and namespaceUri
		if (standardVariableToReturn == null)
			standardVariableToReturn = this.findByNameAndNamespaceUri(
					standardVariable.getName(),
					standardVariable.getNamespaceUriString(), false);

		// Check for object graph
		if (returnFullObjectGraph)
			standardVariableToReturn = (StandardVariable) getRealObjectAndRelationships(standardVariableToReturn);

		// Return the answer
		return standardVariableToReturn;
	}

	/**
	 * This method returns a <code>Collection</code> of <code>Long</code>s that
	 * are the IDs of all the standardVariables that are in SSDS.
	 * 
	 * @return a <code>Collection</code> of <code>Long</code>s that are the IDs
	 *         of all standardVariables in SSDS.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public Collection<Long> findAllIDs() throws MetadataAccessException {
		Collection standardVariableIDs = null;

		// Create the query and run it
		try {
			Query query = getSession()
					.createQuery(
							"select distinct standardVariable.id from "
									+ "StandardVariable standardVariable order by standardVariable.id");
			standardVariableIDs = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return standardVariableIDs;
	}

	/**
	 * @see IMetadataDAO#countFindAllIDs()
	 */
	public int countFindAllIDs() throws MetadataAccessException {
		// The count
		int count = 0;
		try {
			Long longCount = (Long) getSession().createQuery(
					"select count(distinct standardVariable.id) from "
							+ "StandardVariable standardVariable")
					.uniqueResult();
			if (longCount != null)
				count = longCount.intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the result
		return count;
	}

	/**
	 * This method looks for all <code>StandardVariable</code>s whose name is an
	 * exact match of the name supplied.
	 * 
	 * @param name
	 *            is the name that will be used to search for
	 * @return a <code>Collection</code> of <code>StandardVariable</code>s whose
	 *         names exactly match the one specified as the parameter.
	 */
	public Collection<StandardVariable> findByName(String name)
			throws MetadataAccessException {

		// Make sure argument is not null
		logger.debug("name = " + name);
		if ((name == null) && (name.equals(""))) {
			return new ArrayList();
		}

		// The collection to be returned
		Collection results = new ArrayList();

		// Grab a session and run the query
		try {
			Query query = getSession().createQuery(
					"from StandardVariable sv where sv.name = '" + name + "'");
			results = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the results
		return results;
	}

	/**
	 * This method looks for all <code>StandardVariable</code>s whose name
	 * contain the name supplied. It could be an exact match of just contain the
	 * name. For you wildcard folks, it is basically looking for all
	 * <code>StandardVariable</code>s whose names match *likeName*.
	 * 
	 * @param likeName
	 *            is the name that will be used to search for. In SQL terms, it
	 *            will do a LIKE '%likeName%'
	 * @return a <code>Collection</code> of <code>StandardVariable</code>s that
	 *         have names like the one specified as the parameter.
	 */
	public Collection<StandardVariable> findByLikeName(String likeName)
			throws MetadataAccessException {

		// Make sure argument is not null
		logger.debug("likeName = " + likeName);
		if ((likeName == null) && (likeName.equals(""))) {
			return new ArrayList();
		}

		// The collection to be returned
		Collection results = null;

		// Grab a session and run the query
		try {
			Query query = getSession().createQuery(
					"from StandardVariable su where su.name "
							+ "like :likeName order by su.name");
			query.setString("likeName", "%" + likeName + "%");
			results = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the results
		return results;
	}

	/**
	 * This method returns a collection of <code>java.lang.String</code>s that
	 * are all the names of the <code>StandardVariable</code>s in the database
	 * 
	 * @return a <code>Collection</code> of <code>java.lang.String</code>s that
	 *         are all the <code>StandardVariable</code> names that are
	 *         currently in the system. If there are no names, an empty
	 *         collection is returned
	 */
	public Collection<String> findAllNames() throws MetadataAccessException {

		// Create the collection to return
		Collection names = new ArrayList();

		// Create the query and run it
		try {
			Query query = getSession()
					.createQuery(
							"select distinct standardVariable.name from "
									+ "StandardVariable standardVariable order by standardVariable.name");
			names = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Now return them
		return names;
	}

	/**
	 * This method tries to look up all <code>StandardVariable</code>s by their
	 * reference scale
	 * 
	 * @param name
	 *            is a <code>java.lang.String</code> that will be used to search
	 *            for exact matches of a <code>StandardVariable</code>'s
	 *            reference scale (this is case in-sensitive)
	 * @return a <code>Collection</code> of <code>StandardVariable</code>s that
	 *         have a reference scale that exactly matches (case-insensitive)
	 *         the one specified. If no matches were found, an empty collection
	 *         is returned.
	 * @throws MetadataAccessException
	 *             if something goes wrong with the search
	 */
	public Collection<StandardVariable> findByReferenceScale(
			String referenceScale) throws MetadataAccessException {

		// First check to see if the referenceScale is null
		logger.debug("findByReferenceScale called with referenceScale = "
				+ referenceScale);
		if ((referenceScale == null) || (referenceScale.equals(""))) {
			return new ArrayList();
		}

		// The StandardVariables to return
		Collection standardVariablesToReturn = null;

		// Grab a session and run the query
		try {
			Query query = getSession().createQuery(
					"from StandardVariable sv where "
							+ "sv.referenceScale = :referenceScale");
			query.setString("referenceScale", referenceScale);
			standardVariablesToReturn = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the first standardVariable
		return standardVariablesToReturn;
	}

	/**
	 * This method looks for all <code>StandardVariable</code>s whose
	 * referenceScale contain the referenceScale supplied. It could be an exact
	 * match of just contain the referenceScale. For you wildcard folks, it is
	 * basically looking for all <code>StandardVariable</code>s whose
	 * referenceScales match *likeReferenceScale*.
	 * 
	 * @param likeReferenceScale
	 *            is the referenceScale that will be used to search for. In SQL
	 *            terms, it will do a LIKE '%likeReferenceScale%'
	 * @return a <code>Collection</code> of <code>StandardVariable</code>s that
	 *         have referenceScales like the one specified as the parameter.
	 */
	public Collection<StandardVariable> findByLikeReferenceScale(
			String likeReferenceScale) throws MetadataAccessException {

		// Make sure argument is not null
		logger.debug("likeReferenceScale = " + likeReferenceScale);
		if ((likeReferenceScale == null) && (likeReferenceScale.equals(""))) {
			return null;
		}

		// The collection to be returned
		Collection results = null;

		// Grab a session and run the query
		try {
			Query query = getSession()
					.createQuery(
							"from StandardVariable sv where sv.referenceScale "
									+ "like :likeReferenceScale order by sv.referenceScale");
			query.setString("likeReferenceScale", "%" + likeReferenceScale
					+ "%");
			results = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the results
		return results;
	}

	/**
	 * This method returns a collection of <code>java.lang.String</code>s that
	 * are all the referenceScales of the <code>StandardVariable</code>s in the
	 * database
	 * 
	 * @return a <code>Collection</code> of <code>java.lang.String</code>s that
	 *         are all the <code>StandardVariable</code> referenceScales that
	 *         are currently in the system. If there are no referenceScales, an
	 *         empty collection is returned
	 */
	public Collection<String> findAllReferenceScales()
			throws MetadataAccessException {

		// Create the collection to return
		Collection referenceScales = new ArrayList();

		// Create the query and run it
		try {
			Query query = getSession()
					.createQuery(
							"select distinct standardVariable.referenceScale from "
									+ "StandardVariable standardVariable order by standardVariable.referenceScale");
			referenceScales = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Now return them
		return referenceScales;
	}

	/**
	 * @see #findByNameAndNamespaceUri(String, String, boolean)
	 * @param name
	 * @param namespaceUri
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public StandardVariable findByNameAndNamespaceUri(String name,
			URI namespaceUri, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		return this.findByNameAndNamespaceUri(name,
				namespaceUri.toASCIIString(), returnFullObjectGraph);
	}

	/**
	 * This method returns the <code>StandardVariable</code> that matches the
	 * specified name and uri string.
	 * 
	 * @param name
	 * @param namespaceUriString
	 * @return
	 * @throws MetadataAccessException
	 */
	public StandardVariable findByNameAndNamespaceUri(String name,
			String namespaceUriString, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// First check incoming arguments
		if ((name == null) || (name.equals(""))) {
			return null;
		}

		// The StandardVariable to return
		StandardVariable standardVariableToReturn = null;

		// Now construct the query
		StringBuffer queryStringBuffer = new StringBuffer();
		queryStringBuffer.append("from StandardVariable sv where sv.name = '"
				+ name + "'");
		if ((namespaceUriString != null) && (!namespaceUriString.equals(""))) {
			queryStringBuffer.append(" and sv.namespaceUriString = '"
					+ namespaceUriString + "'");
		} else {
			queryStringBuffer.append(" and sv.namespaceUriString IS NULL");
		}

		// Now grab all SV's that match the query
		Collection standardVariablesWithNameAndUriString = null;
		try {
			Query query = getSession()
					.createQuery(queryStringBuffer.toString());
			standardVariablesWithNameAndUriString = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// If a collection was found, grab the first one
		if ((standardVariablesWithNameAndUriString != null)
				&& (standardVariablesWithNameAndUriString.size() > 0)) {
			Iterator it = standardVariablesWithNameAndUriString.iterator();
			standardVariableToReturn = (StandardVariable) it.next();
		}

		// Check for graph
		if (returnFullObjectGraph)
			standardVariableToReturn = (StandardVariable) getRealObjectAndRelationships(standardVariableToReturn);

		// Now return the result
		return standardVariableToReturn;
	}

	/**
	 * This method tries to look up and instantiate a
	 * <code>StandardVariable</code> by its name and reference scale
	 * 
	 * @param name
	 *            is a <code>java.lang.String</code> that will be used to search
	 *            for matches of a <code>StandardVariable</code>'s name
	 * @param referenceScale
	 *            is a <code>java.lang.String</code> that will be used to search
	 *            for matches of a <code>StandardVariable</code>'s reference
	 *            scale
	 * @return a <code>MetadataObject</code> of class
	 *         <code>StandardVariable</code> that has a name and reference scale
	 *         that matches the one specified. If no matches were found, and
	 *         empty collection is returned
	 * @throws MetadataAccessException
	 *             if something goes wrong with the search
	 */
	public StandardVariable findByNameAndReferenceScale(String name,
			String referenceScale) throws MetadataAccessException {

		// First check to see if the name is null
		logger.debug("findByNameAndReferenceScale called with name = " + name
				+ ": referenceScale = " + referenceScale);
		if ((name == null) || (name.equals(""))) {
			return null;
		}

		// The StandardVariable to return
		StandardVariable standardVariableToReturn = null;

		// Grab a session and run the query
		StringBuffer queryStringBuffer = new StringBuffer();
		queryStringBuffer.append("from StandardVariable sv where sv.name = '"
				+ name + "' and sv.referenceScale ");
		if ((referenceScale == null) || (referenceScale.equals(""))) {
			queryStringBuffer.append("is null");
		} else {
			queryStringBuffer.append("= '" + referenceScale + "'");
		}
		Collection standardVariablesWithNameAndReferenceScale = null;
		try {
			Query query = getSession()
					.createQuery(queryStringBuffer.toString());
			standardVariablesWithNameAndReferenceScale = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// If a collection was found
		if ((standardVariablesWithNameAndReferenceScale != null)
				&& (standardVariablesWithNameAndReferenceScale.size() > 0)) {
			Iterator it = standardVariablesWithNameAndReferenceScale.iterator();
			standardVariableToReturn = (StandardVariable) it.next();
		}
		// Return the first standardVariable
		return standardVariableToReturn;
	}

	/**
	 * This method looks up the <code>StandardVariable</code> that is associated
	 * with a <code>RecordVariable</code>.
	 * 
	 * @param recordVariable
	 *            is the <code>RecordVariable</code> to search for
	 * @return is the <code>StandardVariable</code> that is linked to the given
	 *         <code>RecordVariable</code>. Null will be returned if there is no
	 *         association defined
	 */
	public StandardVariable findByRecordVariable(RecordVariable recordVariable)
			throws MetadataAccessException {

		// Create the StandardVariable to return
		StandardVariable standardVariableToReturn = null;

		// Construct query and run it
		if (recordVariable != null) {
			if (recordVariable.getId() == null) {
				throw new MetadataAccessException(
						"StandardVariableDAO.findByRecordVariable: The incoming "
								+ "RecordVariable does not have an ID, one must exist to "
								+ "find the StandardVariable associated with the RecordVariable");
			}
			try {
				Query query = getSession()
						.createQuery(
								"select recordVariable.standardVariable from RecordVariable "
										+ "recordVariable where recordVariable.id = :recordVariableId");
				query.setString("recordVariableId", recordVariable.getId()
						.toString());
				logger.debug("Compiled query = " + query.getQueryString());
				standardVariableToReturn = (StandardVariable) query
						.uniqueResult();
			} catch (HibernateException e) {
				throw new MetadataAccessException(
						"HibernateException ("
								+ e.getClass().getName()
								+ ") caught in StandardVariableDAO.findByRecordVariable (Message =  "
								+ e.getMessage() + ")");
			}
		}
		return standardVariableToReturn;
	}

	public Collection findByStandardUnit(StandardUnit standardUnit,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// The results to return
		Collection results = new HashSet();

		// If the standardUnit is null return null
		if (standardUnit == null)
			return results;

		// First make sure the standardUnit exists
		StandardUnitDAO standardUnitDAO = new StandardUnitDAO(getSession());
		StandardUnit persistentStandardUnit = (StandardUnit) standardUnitDAO
				.findEquivalentPersistentObject(standardUnit, false);
		if (persistentStandardUnit == null)
			return results;

		// The query string
		StringBuffer sqlStringBuffer = new StringBuffer();

		// Now create the query
		Query query = null;

		sqlStringBuffer
				.append("select distinct standardVariable from "
						+ "StandardVariable standardVariable, StandardUnit standardUnit where");
		sqlStringBuffer.append(" standardUnit.id = :standardUnitID and ");
		sqlStringBuffer
				.append(" standardUnit in elements(standardVariable.standardUnits)");
		if ((orderByPropertyName != null)
				&& (checkIfPropertyOK(orderByPropertyName))) {
			sqlStringBuffer.append(" order by standardVariable."
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
			query.setLong("standardUnitID", persistentStandardUnit.getId()
					.longValue());
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		results = query.list();

		if (returnFullObjectGraph)
			results = getRealObjectsAndRelationships(results);

		return results;
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
		StandardVariable standardVariable = this
				.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		StandardVariable persistentStandardVariable = (StandardVariable) this
				.findEquivalentPersistentObject(metadataObject, false);

		// The handle to the actual object that will be persisted
		StandardVariable standardVariableToPersist = null;

		// Copy over any non-null, changed values to the persistent object (if
		// it exists)
		if (persistentStandardVariable != null) {
			String standardVariableBeforeUpdate = persistentStandardVariable
					.toStringRepresentation("<li>");
			if (this.updateDestinationObject(standardVariable,
					persistentStandardVariable)) {
				addMessage(
						ssdsAdminEmailToAddress,
						"A StandardVariable was changed in SSDS<br><b>Before</b><ul><li>"
								+ standardVariableBeforeUpdate
								+ "</ul><br><b>After</b><ul><li>"
								+ persistentStandardVariable
										.toStringRepresentation("<li>")
								+ "</ul><br>");
			}

			// Set the flag
			persistedBefore = true;

			// Assign to the handle
			standardVariableToPersist = persistentStandardVariable;
		} else {
			// This means this will be a new StandardVariable so make sure the
			// alternate primary key is populated
			if ((standardVariable.getName() == null)
					|| (standardVariable.getName().equals(""))) {
				// Generate a random number
				try {
					standardVariable.setName("StandardVariable_"
							+ getUniqueNameSuffix());
				} catch (MetadataException e) {
					logger.error("MetadataException trying to auto-generate "
							+ "the name of a StandardVariable: "
							+ e.getMessage());
				}
				addMessage(
						ssdsAdminEmailToAddress,
						"A name was auto-generated "
								+ "for a StandardVariable:<br><ul><li>"
								+ standardVariable
										.toStringRepresentation("<li>")
								+ "</ul><br>");
			}

			// Clear the flag
			persistedBefore = false;

			// Assign to the handle
			standardVariableToPersist = standardVariable;
		}

		// -------------------------
		// StandardUnit Relationship
		// -------------------------
		// First make sure the standardUnit relationship exists
		if (standardVariable.getStandardUnits() != null) {
			// Now check to make sure it is initialized. If it is not, no
			// changes have taken place and we don't need to do anything
			if (Hibernate.isInitialized(standardVariable.getStandardUnits())) {

				// Grab the DAO for StandardUnit
				StandardUnitDAO standardUnitDAO = new StandardUnitDAO(
						this.getSession());

				// Make sure the are standardUnits to iterate over
				if (standardVariable.getStandardUnits().size() > 0) {

					// Now iterate over the StandardUnits and persist them
					Iterator standardUnitIter = standardVariable
							.getStandardUnits().iterator();
					while (standardUnitIter.hasNext()) {
						StandardUnit tempStandardUnit = (StandardUnit) standardUnitIter
								.next();
						standardUnitDAO.makePersistent(tempStandardUnit);
					}

					// Create a copy of the collection associated with the
					// standardVariable to prevent concurrent modifications
					Collection standardVariableStandardUnitCopy = new ArrayList(
							standardVariable.getStandardUnits());

					// Now we need to make the correct associations. Currently,
					// you have a collection of StandardUnit objects that have
					// their values marked for persistence. Now the object will
					// either be in the session or not depending on if they were
					// previously persisted.
					Iterator standardVariableStandardUnitCopyIterator = standardVariableStandardUnitCopy
							.iterator();
					while (standardVariableStandardUnitCopyIterator.hasNext()) {
						StandardUnit currentStandardUnit = (StandardUnit) standardVariableStandardUnitCopyIterator
								.next();
						StandardUnit currentStandardUnitInSession = null;
						// Is this StandardUnit already in the session?
						if (!getSession().contains(currentStandardUnit)) {
							// No, so grab the one that is
							currentStandardUnitInSession = (StandardUnit) standardUnitDAO
									.findEquivalentPersistentObject(
											currentStandardUnit, false);
						} else {
							currentStandardUnitInSession = currentStandardUnit;
						}
						// Now if the parent standardVariable was persisted
						// before, just check to make sure the sessioned
						// StandardUnit is in the collection associated with the
						// standardVariable that will be persisted
						if (persistedBefore) {
							if (!standardVariableToPersist.getStandardUnits()
									.contains(currentStandardUnitInSession))
								standardVariableToPersist.getStandardUnits()
										.add(currentStandardUnitInSession);
						} else {
							// This means that the standardVariable has not been
							// persisted before. If the StandardUnit is already
							// in the session, there is nothing to do, but if
							// not, we need to replace it with the sessioned one
							if (!getSession().contains(currentStandardUnit)) {
								standardVariableToPersist.getStandardUnits()
										.remove(currentStandardUnit);
								standardVariableToPersist.getStandardUnits()
										.add(currentStandardUnitInSession);
							}
						}
					}
				}
			}
		}

		// If the StandardVariable was not persisted before, save it
		if (!persistedBefore) {
			getSession().save(standardVariableToPersist);
			addMessage(
					ssdsAdminEmailToAddress,
					"A new StandardVariable was added to SSDS<br><ul><li>"
							+ standardVariableToPersist
									.toStringRepresentation("<li>")
							+ "</ul><br>");
		}

		// Now return the ID
		if (standardVariableToPersist != null) {
			return standardVariableToPersist.getId();
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
		StandardVariable standardVariable = this
				.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		StandardVariable persistentStandardVariable = (StandardVariable) this
				.findEquivalentPersistentObject(standardVariable, false);

		// If no matching standardVariable was found, do nothing
		if (persistentStandardVariable == null) {
			logger.debug("No matching standardVariable could be found in the persistent store, "
					+ "no delete performed");
		} else {
			// Clear the StandardUnits
			persistentStandardVariable.clearStandardUnits();

			// Now clear any with RecordVariable
			Collection recordVariablesByStandardVariable = null;
			RecordVariableDAO recordVariableDAO = new RecordVariableDAO(
					getSession());
			recordVariablesByStandardVariable = recordVariableDAO
					.findByStandardVariable(persistentStandardVariable, null,
							null, false);
			if (recordVariablesByStandardVariable != null) {
				Iterator recordVariablesIterator = recordVariablesByStandardVariable
						.iterator();
				while (recordVariablesIterator.hasNext()) {
					RecordVariable recordVariable = (RecordVariable) recordVariablesIterator
							.next();
					recordVariable.setStandardVariable(null);
				}
			}

			logger.debug("Existing object was found, so we will try to delete it");
			try {
				getSession().delete(persistentStandardVariable);
				addMessage(
						ssdsAdminEmailToAddress,
						"A StandardVariable was removed from SSDS<br><ul><li>"
								+ persistentStandardVariable
										.toStringRepresentation("<li>")
								+ "</ul><br>");
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
	 *            <code>StandardVariable</code>
	 * @return a <code>StandardVariable</code> that is same object that came in
	 * @throws MetadataAccessException
	 *             if something is wrong
	 */
	private StandardVariable checkIncomingMetadataObject(
			IMetadataObject metadataObject) throws MetadataAccessException {

		logger.debug("Checking incoming object to see if it is StandardVariable");
		// Check for null argument
		if (metadataObject == null) {
			throw new MetadataAccessException(
					"Failed: incoming StandardVariable was null");
		}

		// Try to cast the incoming object into the correct class
		StandardVariable standardVariable = null;
		try {
			standardVariable = (StandardVariable) metadataObject;
		} catch (ClassCastException cce) {
			throw new MetadataAccessException(
					"Could not cast the incoming object into a StandardVariable");
		}

		logger.debug("Yep, conversion to StandardVariable went OK, will return");
		return standardVariable;
	}

	// protected void initializeRelationships(IMetadataObject metadataObject)
	// throws MetadataAccessException {
	//
	// if (metadataObject == null)
	// return;
	//
	// // For StandardVariables, you want to initialize StandardUnits
	// StandardVariable sv = this.checkIncomingMetadataObject(metadataObject);
	// if (sv.getStandardUnits() != null)
	// Hibernate.initialize(sv.getStandardUnits());
	// }

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(StandardVariableDAO.class);
}
