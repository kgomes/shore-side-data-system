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
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.Keyword;
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

public class KeywordDAO extends MetadataDAO {

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
	public KeywordDAO(Session session) throws MetadataAccessException {
		super(Keyword.class, session);
	}

	/**
	 * @see IMetadataDAO#findEquivalentPersistentObject(IMetadataObject)
	 */
	public IMetadataObject findEquivalentPersistentObject(
			IMetadataObject metadataObject, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		logger.debug("findEquivalentPersistentObject called");
		// The keyword to return
		Keyword keyword = this.checkIncomingMetadataObject(metadataObject);

		// Loop through any persistent keyword with the same name and check
		// for equality
		Keyword keywordToReturn = null;
		if (keyword.getId() != null) {
			keywordToReturn = (Keyword) this.findById(keyword.getId(), false);
		}
		if (keywordToReturn == null) {
			Collection persistentKeywords = this.findByName(keyword.getName(),
					true);
			if (persistentKeywords != null) {
				Iterator dps = persistentKeywords.iterator();
				// Just return the first one
				while (dps.hasNext()) {
					return (Keyword) dps.next();
				}
			}
		}
		if ((returnFullObjectGraph) && (keywordToReturn != null))
			keywordToReturn = (Keyword) this
					.getMetadataObjectGraph(keywordToReturn);

		// If we are here, return nothing
		return keywordToReturn;
	}

	/**
	 * @see IMetadataDAO#findAllIDs()
	 */
	public Collection<Long> findAllIDs() throws MetadataAccessException {
		Collection keywordIDs = null;

		// Create the query and run it
		try {
			Query query = getSession().createQuery(
					"select distinct keyword.id from "
							+ "Keyword keyword order by keyword.id");
			keywordIDs = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return keywordIDs;
	}

	/**
	 * @see IMetadataDAO#countFindAllIDs()
	 */
	public int countFindAllIDs() throws MetadataAccessException {
		// The count
		int count = 0;
		try {
			Long longCount = (Long) getSession().createQuery(
					"select count(distinct keyword.id) from Keyword keyword")
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
	 * This method will return all the <code>Keyword</code>s that match the
	 * supplied name. If you specify <code>true</code> for the exactMatch
	 * parameter, only those <code>Keyword</code>s that exactly match the given
	 * word will be returned. Otherwise, it will do a &quot;like&quot; lookup.
	 * 
	 * @param name
	 *            is the name to search for
	 * @param exactMatch
	 *            is whether to look for an exact match (<code>true</code>) or
	 *            not
	 * @return a <code>Collection</code> of <code>Keyword</code>s that match the
	 *         given name
	 * @throws MetadataAccessException
	 */
	public Collection findByName(String name, boolean exactMatch)
			throws MetadataAccessException {
		// Make sure argument is not null
		logger.debug("findByName where name = " + name + " called.");
		if ((name == null) && (name.equals(""))) {
			return new ArrayList();
		}

		// The collection to be returned
		Collection results = new ArrayList();

		// Construct and run the query
		try {
			if (!exactMatch) {
				try {
					Query query = getSession()
							.createQuery(
									"select distinct keyword from "
											+ "Keyword keyword where keyword.name like :name");
					query.setString("name", "%" + name + "%");
					results = query.list();
				} catch (HibernateException e) {
					throw new MetadataAccessException(e);
				}
			} else {
				try {
					Query query = getSession()
							.createQuery(
									"select distinct keyword from "
											+ "Keyword keyword where keyword.name = :name");
					query.setString("name", name);
					results = query.list();
				} catch (HibernateException e) {
					throw new MetadataAccessException(e);
				}
			}
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		// Log any results
		if (results != null && results.size() > 0) {
			for (Iterator<Object> iterator = results.iterator(); iterator.hasNext();) {
				Object object = (Object) iterator.next();
				if (object instanceof Keyword) {
					logger.debug("Matching Keyword: "
							+ ((Keyword) object).toStringRepresentation("|"));
				} else {
					logger.debug("Matching object found, but not Keyword: "
							+ object);
				}
			}
		} else {
			logger.debug("No matching keywords found");
		}

		// Return the results
		return results;
	}

	/**
	 * This method returns a <code>Collection</code> of <code>String</code>s
	 * that are the names of all the keywords that are registered in SSDS.
	 * 
	 * @return a <code>Collection</code> of <code>String</code>s that are the
	 *         names of all keywords in SSDS.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public Collection findAllNames() throws MetadataAccessException {
		Collection keywordNames = null;

		// Create the query and run it
		try {
			Query query = getSession().createQuery(
					"select distinct keyword.name from "
							+ "Keyword keyword order by keyword.name");
			keywordNames = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return keywordNames;
	}

	/**
	 * This method returns all the <code>Keyword</code>s that are associated
	 * with the given <code>IMetadataObject</code>.
	 * 
	 * @param metadataObject
	 *            the <code>IMetadataObject</code> that is to use to look up
	 *            keywords that are associated with it
	 * @return a <code>Collection</code> of <code>Keyword</code>s that are
	 *         associated to the <code>IMetadataObject</code>. It will return an
	 *         empty collection if none are found
	 * @throws MetadataAccessException
	 */
	public Collection findByMetadataObject(IMetadataObject metadataObject)
			throws MetadataAccessException {
		Collection keywords = new ArrayList();

		// Build the query and run
		if (metadataObject instanceof DataContainer) {
			DataContainerDAO dataContainerDAO = new DataContainerDAO(this
					.getSession());
			Long dataContainerId = dataContainerDAO.findId(metadataObject);
			if (dataContainerId != null) {
				Query query = getSession().createQuery(
						"select dataContainer.keywords from DataContainer dataContainer  "
								+ "where dataContainer.id = :id");
				query.setString("id", dataContainerId.toString());
				keywords = query.list();
			}
		}

		// Return the result
		return keywords;
	}

	/**
	 * @see IMetadataDAO#makePersistent(IMetadataObject)
	 */
	public Long makePersistent(IMetadataObject metadataObject)
			throws MetadataAccessException {

		logger.debug("makePersistent called");

		// This is a flag to indicate if the keyword has been persisted in the
		// past
		boolean persistedBefore = false;

		// Check incoming object
		Keyword keyword = this.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		Keyword persistentKeyword = (Keyword) this
				.findEquivalentPersistentObject(metadataObject, false);

		// A handle to the Keyword that will actually be persisted
		Keyword keywordToPersist = null;

		// If there is an existing one, copy over any non-null, changed fields
		// for updating
		if (persistentKeyword != null) {
			this.updateDestinationObject(keyword, persistentKeyword);

			// Set the flag for previously persisted
			persistedBefore = true;

			// Set to the handle
			keywordToPersist = persistentKeyword;
		} else {
			// Since this is a new instance, make sure alternate key is valid
			if ((keyword.getName() == null) || (keyword.getName().equals(""))) {
				try {
					keyword.setName("Keyword_" + getUniqueNameSuffix());
				} catch (MetadataException e) {
					logger
							.error("MetadataException caught trying to auto-generate "
									+ "a name for a keyword: " + e.getMessage());
				}
				addMessage(
						ssdsAdminEmailToAddress,
						"An incoming Keyword did not have a name, so SSDS auto-generated one: <br><ul><li>"
								+ keyword.toStringRepresentation("<li>")
								+ "</ul><br>");
			}

			// Clear the persisted flag
			persistedBefore = false;

			// Assign to the handle
			keywordToPersist = keyword;
		}

		// If not previously persisted, save it
		if (!persistedBefore)
			getSession().save(keywordToPersist);

		// Now return the ID
		if (keywordToPersist != null) {
			return keywordToPersist.getId();
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
		Keyword keyword = this.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		Keyword persistentKeyword = (Keyword) this
				.findEquivalentPersistentObject(keyword, false);

		// If no matching keyword was found, throw an exception
		if (persistentKeyword == null) {
			logger
					.debug("No matching keyword could be found in the persistent store, "
							+ "no delete performed");
		} else {
			// Now before actually deleting the keyword, we need to clear the
			// following relationships:
			// 1. DataContainer
			// 2. DataProducer
			// 3. Resource

			// DataContainer
			DataContainerDAO dcDAO = new DataContainerDAO(getSession());
			Collection associatedDCs = dcDAO.findByKeywordName(
					persistentKeyword.getName(), true, null, null, false);
			if (associatedDCs != null) {
				Iterator associatedDCsIter = associatedDCs.iterator();
				while (associatedDCsIter.hasNext()) {
					DataContainer tempDC = (DataContainer) associatedDCsIter
							.next();
					tempDC.removeKeyword(persistentKeyword);
				}
			}

			// DataProducers
			DataProducerDAO dpDAO = new DataProducerDAO(getSession());
			Collection associatedDPs = dpDAO.findByKeywordName(
					persistentKeyword.getName(), true, null, null, false);
			if (associatedDPs != null) {
				Iterator associatedDPsIter = associatedDPs.iterator();
				while (associatedDPsIter.hasNext()) {
					DataProducer tempDP = (DataProducer) associatedDPsIter
							.next();
					tempDP.removeKeyword(persistentKeyword);
				}
			}

			// Resources
			ResourceDAO resourceDAO = new ResourceDAO(getSession());
			Collection associatedResources = resourceDAO.findByKeywordName(
					persistentKeyword.getName(), true, null, null, false);
			if (associatedResources != null) {
				Iterator associatedResourcesIter = associatedResources
						.iterator();
				while (associatedResourcesIter.hasNext()) {
					Resource tempResource = (Resource) associatedResourcesIter
							.next();
					tempResource.removeKeyword(persistentKeyword);
				}
			}

			logger
					.debug("Existing object was found, so we will try to delete it");
			try {
				getSession().delete(persistentKeyword);
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
	 *            <code>Keyword</code>
	 * @return a <code>Keyword</code> that is same object that came in
	 * @throws MetadataAccessException
	 *             if something is wrong
	 */
	private Keyword checkIncomingMetadataObject(IMetadataObject metadataObject)
			throws MetadataAccessException {

		// Check for null argument
		if (metadataObject == null) {
			throw new MetadataAccessException(
					"Failed: incoming Keyword was null");
		}

		// Try to cast the incoming object into the correct class
		Keyword keyword = null;
		try {
			keyword = (Keyword) metadataObject;
		} catch (ClassCastException cce) {
			throw new MetadataAccessException(
					"Could not cast the incoming object into a Keyword");
		}
		return keyword;
	}

//	protected void initializeRelationships(IMetadataObject metadataObject)
//			throws MetadataAccessException {
//
//	}

	/**
	 * The Log4J Logger
	 */
	static Logger logger = Logger.getLogger(KeywordDAO.class);
}
