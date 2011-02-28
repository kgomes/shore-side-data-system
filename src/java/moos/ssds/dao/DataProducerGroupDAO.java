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
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.DataProducerGroup;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class DataProducerGroupDAO extends MetadataDAO {

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
	public DataProducerGroupDAO(Session session) throws MetadataAccessException {
		super(DataProducerGroup.class, session);
	}

	public IMetadataObject findEquivalentPersistentObject(
			IMetadataObject metadataObject, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		logger.debug("findEquivalentPersistentObject called");
		// The dataProducerGroup to return
		DataProducerGroup dataProducerGroup = this
				.checkIncomingMetadataObject(metadataObject);

		// Loop through any persistent dataProducerGroup with the same name and
		// check for equality
		DataProducerGroup dataProducerGroupToReturn = null;
		if (dataProducerGroup.getId() != null) {
			Criteria criteria = this.formulatePropertyCriteria(false,
					dataProducerGroup.getId(), null, false, null, null);
			dataProducerGroupToReturn = (DataProducerGroup) criteria
					.uniqueResult();
		}
		// If it's still null, look up by name
		if ((dataProducerGroupToReturn == null)
				&& (dataProducerGroup.getName() != null)
				&& (!dataProducerGroup.getName().equals(""))) {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					dataProducerGroup.getName(), true, null, null);
			dataProducerGroupToReturn = (DataProducerGroup) criteria
					.uniqueResult();
		}
		if (returnFullObjectGraph)
			dataProducerGroupToReturn = (DataProducerGroup) getRealObjectAndRelationships(dataProducerGroupToReturn);

		// Return the result
		return dataProducerGroupToReturn;
	}

	/**
	 * @see IMetadataDAO#findAllIDs()
	 */
	public Collection<Long> findAllIDs() throws MetadataAccessException {
		Collection dataProducerGroupIDs = null;

		// Create the query and run it
		try {
			Query query = getSession()
					.createQuery(
							"select distinct dataProducerGroup.id from "
									+ "DataProducerGroup dataProducerGroup order by dataProducerGroup.id");
			dataProducerGroupIDs = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return dataProducerGroupIDs;
	}

	/**
	 * @see IMetadataDAO#countFindAllIDs()
	 */
	public int countFindAllIDs() throws MetadataAccessException {
		// The count
		int count = 0;
		try {
			Long longCount = (Long) getSession().createQuery(
					"select count(distinct dataProducerGroup.id) from "
							+ "DataProducerGroup dataProducerGroup")
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
	 * This method will return all the <code>DataProducerGroup</code>s that
	 * match the supplied name. If you specify <code>true</code> for the
	 * exactMatch parameter, only those <code>DataProducerGroup</code>s that
	 * exactly match the given word will be returned. Otherwise, it will do a
	 * &quot;like&quot; lookup.
	 * 
	 * @param name
	 *            is the name to search for
	 * @param exactMatch
	 *            is whether to look for an exact match (<code>true</code>) or
	 *            not
	 * @return a <code>Collection</code> of <code>DataProducerGroup</code>s that
	 *         match the given name. It returns an empty collection if non were
	 *         found
	 * @throws MetadataAccessException
	 */
	public Collection<DataProducerGroup> findByName(String name,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Make sure argument is not null
		logger.debug("findByName where name = " + name + " called.");
		if ((name == null) && (name.equals(""))) {
			return new ArrayList();
		}

		// The collection to be returned
		Collection results = new ArrayList();

		Criteria criteria = this.formulatePropertyCriteria(false, null, name,
				exactMatch, orderByPropertyName, ascendingOrDescending);
		results = criteria.list();
		if (returnFullObjectGraph)
			results = getRealObjectsAndRelationships(results);
		// Return the results
		return results;
	}

	public int countFindByName(String name, boolean exactMatch)
			throws MetadataAccessException {
		// Make sure argument is not null
		logger.debug("findByName where name = " + name + " called.");
		if ((name == null) && (name.equals(""))) {
			throw new MetadataAccessException("Name was not specified");
		}

		// The count to be returned
		int count = 0;

		// If one not found, look up by exact name
		Criteria criteria = this.formulatePropertyCriteria(true, null, name,
				exactMatch, null, null);
		count = ((Long) criteria.uniqueResult()).intValue();

		// Return the results
		return count;
	}

	/**
	 * This method returns a <code>Collection</code> of <code>String</code>s
	 * that are the names of all the dataProducerGroups that are registered in
	 * SSDS.
	 * 
	 * @return a <code>Collection</code> of <code>String</code>s that are the
	 *         names of all dataProducerGroups in SSDS.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public Collection<String> findAllNames() throws MetadataAccessException {
		Collection dataProducerGroupNames = null;

		// Create the query and run it
		try {
			Query query = getSession()
					.createQuery(
							"select distinct dataProducerGroup.name from "
									+ "DataProducerGroup dataProducerGroup order by dataProducerGroup.name");
			dataProducerGroupNames = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return dataProducerGroupNames;
	}

	public int countFindAllNames() throws MetadataAccessException {
		int count = 0;
		try {
			Long integerCount = (Long) getSession().createQuery(
					"select count(distinct dataProducerGroup.name) from "
							+ "DataProducerGroup dataProducerGroup")
					.uniqueResult();
			if (integerCount != null)
				count = integerCount.intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return count;
	}

	/**
	 * This method returns all the <code>DataProducerGroup</code>s that are
	 * associated with the given <code>DataProducer</code>. TODO add
	 * orderByPropertyName and ascending or descending
	 * 
	 * @param dataProducer
	 *            the <code>DataProducer</codFe> that is to use to look up
	 *            dataProducerGroups that are associated with it
	 * @return a <code>Collection</code> of <code>DataProducerGroup</code>s that
	 *         are associated to the <code>DataProducer</code>. It will return
	 *         an empty collection if none are found
	 * @throws MetadataAccessException
	 */
	public Collection<DataProducerGroup> findByDataProducer(
			DataProducer dataProducer, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		Collection dataProducerGroups = new ArrayList();

		// Build the query and run
		if (dataProducer != null) {
			DataProducerDAO dataProducerDAO = new DataProducerDAO(
					this.getSession());
			Long dataProducerId = dataProducerDAO.findId(dataProducer);
			if (dataProducerId != null) {
				Query query = getSession().createQuery(
						"select dataProducer.dataProducerGroups from DataProducer dataProducer  "
								+ "where dataProducer.id = :id");
				query.setString("id", dataProducerId.toString());
				dataProducerGroups = query.list();
			}
		}

		if (returnFullObjectGraph)
			dataProducerGroups = getRealObjectsAndRelationships(dataProducerGroups);

		// Return the result
		return dataProducerGroups;
	}

	public int countFindByDataProducer(DataProducer dataProducer)
			throws MetadataAccessException {

		int count = 0;

		// Build the query and run
		if (dataProducer != null) {
			DataProducerDAO dataProducerDAO = new DataProducerDAO(
					this.getSession());
			Long dataProducerId = dataProducerDAO.findId(dataProducer);
			if (dataProducerId != null) {
				Collection dataProducerGroups = null;
				Query query = getSession().createQuery(
						"select dataProducer.dataProducerGroups from DataProducer dataProducer  "
								+ "where dataProducer.id = :id");
				query.setString("id", dataProducerId.toString());
				dataProducerGroups = query.list();
				if (dataProducerGroups != null)
					count = dataProducerGroups.size();
			}
		}

		// Return the result
		return count;
	}

	/**
	 * @see IMetadataDAO#makePersistent(IMetadataObject)
	 */
	public Long makePersistent(IMetadataObject metadataObject)
			throws MetadataAccessException {

		logger.debug("makePersistent called");

		// A flag to track if it was persisted previously
		boolean persistedBefore = false;

		// Check incoming object
		DataProducerGroup dataProducerGroup = this
				.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		DataProducerGroup persistentDataProducerGroup = (DataProducerGroup) this
				.findEquivalentPersistentObject(metadataObject, false);

		// Create a handle to the object that will actually be persisted
		DataProducerGroup dataProducerGroupToPersist = null;

		// If it was persisted before, copy over any non-null, changed fields
		// and assign to persisting handle
		if (persistentDataProducerGroup != null) {
			this.updateDestinationObject(dataProducerGroup,
					persistentDataProducerGroup);

			// Set the flag
			persistedBefore = true;

			// Assign to the handle
			dataProducerGroupToPersist = persistentDataProducerGroup;
		} else {
			// Since this is a new DataProducerGroup, make sure the alternate
			// business key is there.
			if ((dataProducerGroup.getName() == null)
					|| (dataProducerGroup.getName().equals(""))) {
				try {
					dataProducerGroup.setName("DataProducerGroup_"
							+ getUniqueNameSuffix());
				} catch (MetadataException e) {
					logger.error("MetadataException caught trying to set the "
							+ "auto-generated name for a DataProducerGroup: "
							+ e.getMessage());
				}
				addMessage(
						ssdsAdminEmailToAddress,
						"An incoming DataProducerGroup had no name, so SSDS auto-generated one:<br><ul><li>"
								+ dataProducerGroup
										.toStringRepresentation("<li>")
								+ "</ul><br>");
			}

			// Clear the flag
			persistedBefore = false;

			// Assign to the handle
			dataProducerGroupToPersist = dataProducerGroup;
		}

		// If it was not persisted before, save it
		if (!persistedBefore)
			getSession().save(dataProducerGroupToPersist);

		// Now return the ID
		if (dataProducerGroupToPersist != null) {
			return dataProducerGroupToPersist.getId();
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
		DataProducerGroup dataProducerGroup = this
				.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		DataProducerGroup persistentDataProducerGroup = (DataProducerGroup) this
				.findEquivalentPersistentObject(dataProducerGroup, false);

		// If no matching dataProducerGroup was found, do nothing
		if (persistentDataProducerGroup == null) {
			logger.debug("No matching dataProducerGroup could be found in the persistent store, "
					+ "no delete performed");
		} else {
			// If we are here, we need to make sure all the connections to any
			// DataProducers are removed before removing it
			DataProducerDAO dpDAO = new DataProducerDAO(getSession());
			Collection associatedDPs = dpDAO.findByDataProducerGroup(
					persistentDataProducerGroup, null, null, true);
			if (associatedDPs != null) {
				Iterator associatedDPsIter = associatedDPs.iterator();
				while (associatedDPsIter.hasNext()) {
					DataProducer tempDP = (DataProducer) associatedDPsIter
							.next();
					tempDP.removeDataProducerGroup(persistentDataProducerGroup);
				}
			}

			logger.debug("Existing object was found, so we will try to delete it");
			try {
				getSession().delete(persistentDataProducerGroup);
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
	 *            <code>DataProducerGroup</code>
	 * @return a <code>DataProducerGroup</code> that is same object that came in
	 * @throws MetadataAccessException
	 *             if something is wrong
	 */
	private DataProducerGroup checkIncomingMetadataObject(
			IMetadataObject metadataObject) throws MetadataAccessException {

		// Check for null argument
		if (metadataObject == null) {
			throw new MetadataAccessException(
					"Failed: incoming DataProducerGroup was null");
		}

		// Try to cast the incoming object into the correct class
		DataProducerGroup dataProducerGroup = null;
		try {
			dataProducerGroup = (DataProducerGroup) metadataObject;
		} catch (ClassCastException cce) {
			throw new MetadataAccessException(
					"Could not cast the incoming object into a DataProducerGroup");
		}
		return dataProducerGroup;
	}

	private Criteria formulatePropertyCriteria(boolean countQuery, Long id,
			String name, boolean exactNameMatch, String orderByProperty,
			String ascendingOrDescending) throws MetadataAccessException {
		// The Criteria to return
		Criteria criteria = getSession()
				.createCriteria(DataProducerGroup.class);
		// Make the return distinct
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

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
			addOrderByCriteria(criteria, orderByProperty, ascendingOrDescending);
		}
		return criteria;
	}

	// protected void initializeRelationships(IMetadataObject metadataObject)
	// throws MetadataAccessException {
	// // For DataProducerGroup, there is nothing to do
	// }

	/**
	 * The Log4J Logger
	 */
	static Logger logger = Logger.getLogger(DataProducerGroupDAO.class);
}
