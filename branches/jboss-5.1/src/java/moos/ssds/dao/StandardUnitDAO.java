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
import java.util.Iterator;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.StandardUnit;
import moos.ssds.metadata.StandardVariable;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * This Data Access Object (DAO) provides methods for interacting with the
 * persitence mechanism the handle the persistence of <code>StandardUnit</code>
 * objects. It also provides query methods.
 * <hr>
 * 
 * @stereotype service
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.15 $
 */
public class StandardUnitDAO extends MetadataDAO {

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
	public StandardUnitDAO(Session session) throws MetadataAccessException {
		// Construct the parent DAO with the correct class and session
		super(StandardUnit.class, session);
	}

	/**
	 * @see IMetadataDAO#findEquivalentPersistentObject(IMetadataObject)
	 */
	public IMetadataObject findEquivalentPersistentObject(
			IMetadataObject metadataObject, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// First try to cast to a StandardUnit
		StandardUnit standardUnit = this
				.checkIncomingMetadataObject(metadataObject);

		// The StandardUnit to return
		StandardUnit standardUnitToReturn = null;
		if (standardUnit.getId() != null)
			standardUnitToReturn = (StandardUnit) this.findById(standardUnit
					.getId(), false);
		if (standardUnitToReturn == null)
			standardUnitToReturn = this.findByName(standardUnit.getName());

		// Double check that if the incoming standardUnit has an ID, it matches
		// the one that was found with the matching username
		if (standardUnit.getId() != null) {
			if (standardUnit.getId().longValue() != standardUnitToReturn
					.getId().longValue()) {
				logger
						.error("The ID and the name of the incoming StandardUnit "
								+ "did not match a ID/username combination of "
								+ "anything in the persistent store, this should "
								+ "not happen");
				throw new MetadataAccessException(
						"The ID and the name of the incoming StandardUnit "
								+ "did not match a ID/username combination of "
								+ "anything in the persistent store, this should "
								+ "not happen");
			}
		}

		if ((returnFullObjectGraph) && (standardUnitToReturn != null))
			standardUnitToReturn = (StandardUnit) this
					.getMetadataObjectGraph(standardUnitToReturn);
		// Now return it
		return standardUnitToReturn;
	}

	/**
	 * This method returns a <code>Collection</code> of <code>Long</code>s
	 * that are the IDs of all the standardUnits that are in SSDS.
	 * 
	 * @return a <code>Collection</code> of <code>Long</code>s that are the
	 *         IDs of all standardUnits in SSDS.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public Collection findAllIDs() throws MetadataAccessException {
		Collection standardUnitIDs = null;

		// Create the query and run it
		try {
			Query query = getSession()
					.createQuery(
							"select distinct standardUnit.id from "
									+ "StandardUnit standardUnit order by standardUnit.id");
			standardUnitIDs = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return standardUnitIDs;
	}

	/**
	 * @see IMetadataDAO#countFindAllIDs()
	 */
	public int countFindAllIDs() throws MetadataAccessException {
		// The count
		int count = 0;
		try {
			Long longCount = (Long) getSession().createQuery(
					"select count(distinct standardUnit.id) from "
							+ "StandardUnit standardUnit").uniqueResult();
			if (longCount != null)
				count = longCount.intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the result
		return count;
	}

	/**
	 * This method tries to look up and instantiate a <code>StandardUnit</code>
	 * by its name
	 * 
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
	public StandardUnit findByName(String name) throws MetadataAccessException {

		// First check to see if the name is null
		logger.debug("findByName called with name = " + name);
		if ((name == null) || (name.equals(""))) {
			return null;
		}

		// The StandardUnit to return
		StandardUnit standardUnitToReturn = null;

		// Grab a session and run the query
		Collection standardUnitsWithName = null;
		try {
			Query query = getSession().createQuery(
					"from StandardUnit su where su.name = :name");
			query.setString("name", name);
			standardUnitsWithName = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// If a collection was found
		if ((standardUnitsWithName != null)
				&& (standardUnitsWithName.size() > 0)) {
			Iterator it = standardUnitsWithName.iterator();
			standardUnitToReturn = (StandardUnit) it.next();
		}
		// Return the first standardUnit
		return standardUnitToReturn;
	}

	/**
	 * This method looks for all <code>StandardUnit</code>s whose name
	 * contain the name supplied. It could be an exact match of just contain the
	 * name. For you wildcard folks, it is basically looking for all
	 * <code>StandardUnit</code>s whose names match *likeName*.
	 * 
	 * @param likeName
	 *            is the name that will be used to search for. In SQL terms, it
	 *            will do a LIKE '%likeName%'
	 * @return a <code>Collection</code> of <code>StandardUnit</code>s that
	 *         have names like the one specified as the parameter.
	 */
	public Collection findByLikeName(String likeName)
			throws MetadataAccessException {

		// Make sure argument is not null
		logger.debug("likeName = " + likeName);
		if ((likeName == null) && (likeName.equals(""))) {
			return new ArrayList();
		}

		// The collection to be returned
		Collection results = new ArrayList();

		// Grab a session and run the query
		try {
			Query query = getSession().createQuery(
					"from StandardUnit su where su.name "
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
	 * This method returns a collection of <code>java.lang.String</code>s
	 * that are all the names of the <code>StandardUnit</code>s in the
	 * database
	 * 
	 * @return a <code>Collection</code> of <code>java.lang.String</code>s
	 *         that are all the <code>StandardUnit</code> names that are
	 *         currently in the system. If there are no names, an empty
	 *         collection is returned
	 */
	public Collection findAllNames() throws MetadataAccessException {

		// Create the collection to return
		Collection names = new ArrayList();

		// Create the query and run it
		try {
			Query query = getSession()
					.createQuery(
							"select distinct standardUnit.name from "
									+ "StandardUnit standardUnit order by standardUnit.name");
			names = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Now return them
		return names;
	}

	/**
	 * This method tries to look up all <code>StandardUnit</code>s by their
	 * symbol
	 * 
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

		// First check to see if the symbol is null
		logger.debug("findBySymbol called with symbol = " + symbol);
		if ((symbol == null) || (symbol.equals(""))) {
			return new ArrayList();
		}

		// The StandardUnits to return
		Collection standardUnitsToReturn = null;

		// Grab a session and run the query
		try {
			Query query = getSession().createQuery(
					"from StandardUnit su where su.symbol = :symbol");
			query.setString("symbol", symbol);
			standardUnitsToReturn = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the first standardUnit
		return standardUnitsToReturn;
	}

	/**
	 * This method looks for all <code>StandardUnit</code>s whose symbol
	 * contain the symbol supplied. It could be an exact match of just contain
	 * the symbol. For you wildcard folks, it is basically looking for all
	 * <code>StandardUnit</code>s whose symbols match *likeSymbol*.
	 * 
	 * @param likeSymbol
	 *            is the symbol that will be used to search for. In SQL terms,
	 *            it will do a LIKE '%likeSymbol%'
	 * @return a <code>Collection</code> of <code>StandardUnit</code>s that
	 *         have symbols like the one specified as the parameter.
	 */
	public Collection findByLikeSymbol(String likeSymbol)
			throws MetadataAccessException {

		// Make sure argument is not null
		logger.debug("likeSymbol = " + likeSymbol);
		if ((likeSymbol == null) && (likeSymbol.equals(""))) {
			return new ArrayList();
		}

		// The collection to be returned
		Collection results = new ArrayList();

		// Grab a session and run the query
		try {
			Query query = getSession().createQuery(
					"from StandardUnit su where su.symbol "
							+ "like :likeSymbol order by su.symbol");
			query.setString("likeSymbol", "%" + likeSymbol + "%");
			results = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the results
		return results;
	}

	public Collection findStandardVariables(StandardUnit standardUnit)
			throws MetadataAccessException {
		return null;
	}

	/**
	 * @see IMetadataDAO#makePersistent(IMetadataObject)
	 */
	public Long makePersistent(IMetadataObject metadataObject)
			throws MetadataAccessException {

		logger.debug("makePersistent called");

		// A flag to indicate if it was previously persisted
		boolean persistedBefore = false;

		// Check incoming object
		StandardUnit standardUnit = this
				.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		StandardUnit persistentStandardUnit = (StandardUnit) this
				.findEquivalentPersistentObject(standardUnit, false);

		// Create a handle to the StandardUnit that will really be persisted
		StandardUnit standardUnitToPersist = null;

		// If there is a persistent one, copy over any non-null, changed fields
		// and assign to the persistent handle
		if (persistentStandardUnit != null) {
			String standardUnitBefore = persistentStandardUnit
					.toStringRepresentation("<li>");
			if (this.updateDestinationObject(standardUnit,
					persistentStandardUnit)) {
				addMessage(ssdsAdminEmailToAddress,
						"A StandardUnit was changed in SSDS:<br><b>Before</b><ul><li>"
								+ standardUnitBefore
								+ "</ul><br><b>After</b><br><ul><li>"
								+ persistentStandardUnit
										.toStringRepresentation("<li>")
								+ "</ul><br>");
			}

			// Set the flag
			persistedBefore = true;

			// Attach to the handle
			standardUnitToPersist = persistentStandardUnit;
		} else {
			// Since this is a new StandardUnit, make sure the alternate key is
			// there
			if ((standardUnit.getName() == null)
					|| (standardUnit.getName().equals(""))) {
				try {
					standardUnit.setName("StandardUnit_"
							+ getUniqueNameSuffix());
				} catch (MetadataException e) {
					logger.error("MetadataException caught trying to "
							+ "auto-generate a name for a StandardUnit: "
							+ e.getMessage());
				}
				addMessage(ssdsAdminEmailToAddress,
						"An incoming StandardUnit did not have a name, "
								+ "so SSDS auto-generated one:<br><ul><li>"
								+ standardUnit.toStringRepresentation("<li>")
								+ "</ul><br>");
			}

			// Clear the flag
			persistedBefore = false;

			// Attach to persisting handle
			standardUnitToPersist = standardUnit;
		}

		// If it was not persisted before, save it
		if (!persistedBefore) {
			getSession().save(standardUnitToPersist);
			addMessage(ssdsAdminEmailToAddress,
					"A new StandardUnit was inserted into SSDS: <br><ul><li>"
							+ standardUnitToPersist
									.toStringRepresentation("<li>")
							+ "</ul><br>");
		}

		// Now return the ID
		if (standardUnitToPersist != null) {
			return standardUnitToPersist.getId();
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
		StandardUnit standardUnit = this
				.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		StandardUnit persistentStandardUnit = (StandardUnit) this
				.findEquivalentPersistentObject(standardUnit, false);

		// If no matching standardUnit was found, do nothing
		if (persistentStandardUnit == null) {
			logger
					.debug("No matching standardUnit could be found in the persistent store, "
							+ "no delete performed");
		} else {
			// First clear any relationships with StandardVariable
			StandardVariableDAO standardVariableDAO = new StandardVariableDAO(
					getSession());
			Collection standardVariablesByStandardUnit = standardVariableDAO
					.findByStandardUnit(persistentStandardUnit, null, null,
							false);
			if (standardVariablesByStandardUnit != null) {
				Iterator iterator = standardVariablesByStandardUnit.iterator();
				while (iterator.hasNext()) {
					StandardVariable standardVariable = (StandardVariable) iterator
							.next();
					standardVariable.removeStandardUnit(persistentStandardUnit);
				}
			}

			// Now clear any with RecordVariable
			Collection recordVariablesByStandardUnit = null;
			RecordVariableDAO recordVariableDAO = new RecordVariableDAO(
					getSession());
			recordVariablesByStandardUnit = recordVariableDAO
					.findByStandardUnit(persistentStandardUnit, null, null,
							false);
			if (recordVariablesByStandardUnit != null) {
				Iterator recordVariablesIterator = recordVariablesByStandardUnit
						.iterator();
				while (recordVariablesIterator.hasNext()) {
					RecordVariable recordVariable = (RecordVariable) recordVariablesIterator
							.next();
					recordVariable.setStandardUnit(null);
				}
			}

			logger
					.debug("Existing object was found, so we will try to delete it");
			try {
				getSession().delete(persistentStandardUnit);
				addMessage(ssdsAdminEmailToAddress,
						"A StandardUnit was removed from SSDS:<br><ul><li>"
								+ persistentStandardUnit
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
	 *            <code>StandardUnit</code>
	 * @return a <code>StandardUnit</code> that is same object that came in
	 * @throws MetadataAccessException
	 *             if something is wrong
	 */
	private StandardUnit checkIncomingMetadataObject(
			IMetadataObject metadataObject) throws MetadataAccessException {

		// Check for null argument
		if (metadataObject == null) {
			throw new MetadataAccessException(
					"Failed: incoming StandardUnit was null");
		}

		// Try to cast the incoming object into the correct class
		StandardUnit standardUnit = null;
		try {
			standardUnit = (StandardUnit) metadataObject;
		} catch (ClassCastException cce) {
			throw new MetadataAccessException(
					"Could not cast the incoming object into a StandardUnit");
		}
		return standardUnit;
	}

//	protected void initializeRelationships(IMetadataObject metadataObject)
//			throws MetadataAccessException {
//
//	}

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(StandardUnitDAO.class);

}
