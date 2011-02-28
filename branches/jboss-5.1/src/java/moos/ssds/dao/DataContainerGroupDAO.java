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
import moos.ssds.metadata.DataContainerGroup;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * This Data Access Object (DAO) provides methods for interacting with the
 * persitence mechanism that handles the persistence of
 * <code>DataContainerGroup</code> objects. It also provides query methods.
 * <hr>
 * 
 * @stereotype service
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.13 $
 */
public class DataContainerGroupDAO extends MetadataDAO {

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
	public DataContainerGroupDAO(Session session)
			throws MetadataAccessException {
		super(DataContainerGroup.class, session);
	}

	/**
	 * @see IMetadataDAO#findEquivalentPersistentObject(IMetadataObject)
	 */
	public IMetadataObject findEquivalentPersistentObject(
			IMetadataObject metadataObject, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		logger.debug("findEquivalentPersistentObject called");

		// First cast the incoming object into proper class
		DataContainerGroup dataContainerGroup = this
				.checkIncomingMetadataObject(metadataObject);

		// Construct the DataContainerGroup to return
		DataContainerGroup dataContainerGroupToReturn = null;

		// Now check the ID, if there is one, search by the ID
		if (dataContainerGroup.getId() != null) {
			// Formulate the query by ID
			Criteria criteria = this.formulatePropertyCriteria(false,
					dataContainerGroup.getId(), null, false, null, null);
			// Run it and get the result
			dataContainerGroupToReturn = (DataContainerGroup) criteria
					.uniqueResult();
		}

		// If one is still not found, try to look up by the name of the incoming
		// object
		if ((dataContainerGroupToReturn == null)
				&& (dataContainerGroup.getName() != null)
				&& (!dataContainerGroup.getName().equals(""))) {
			// Formulate the query
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					dataContainerGroup.getName(), true, null, null);
			// Run it and get the result
			dataContainerGroupToReturn = (DataContainerGroup) criteria
					.uniqueResult();
		}

		// If the full return graph is requested, fill it out
		if (returnFullObjectGraph)
			dataContainerGroupToReturn = (DataContainerGroup) getRealObjectAndRelationships(dataContainerGroupToReturn);

		// Now return the persistent object that is considered equivalent
		return dataContainerGroupToReturn;
	}

	/**
	 * @see IMetadataDAO#findAllIDs()
	 */
	public Collection<Long> findAllIDs() throws MetadataAccessException {
		// The collection to be returned
		Collection dataContainerGroupIDs = new ArrayList();

		// Create the query and run it
		try {
			Query query = getSession().createQuery(
					"select distinct dataContainerGroup.id from "
							+ "DataContainerGroup dataContainerGroup "
							+ "order by dataContainerGroup.id");
			dataContainerGroupIDs = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		// Now return the results
		return dataContainerGroupIDs;
	}

	/**
	 * @see IMetadataDAO#countFindAllIDs()
	 */
	public int countFindAllIDs() throws MetadataAccessException {

		// The count to return
		int count = 0;

		// Formulate and run the query
		try {
			Long longCount = (Long) getSession().createQuery(
					"select count(distinct dataContainerGroup.id) from "
							+ "DataContainerGroup dataContainerGroup")
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
	 * This method will return all the <code>DataContainerGroup</code>s that
	 * match the supplied name. If you specify <code>true</code> for the
	 * exactMatch parameter, only those <code>DataContainerGroup</code>s that
	 * exactly match the given word will be returned. Otherwise, it will do a
	 * &quot;like&quot; lookup.
	 * 
	 * @param name
	 *            is the name to search for
	 * @param exactMatch
	 *            is whether to look for an exact match (<code>true</code>) or
	 *            not
	 * @return a <code>Collection</code> of <code>DataContainerGroup</code>s
	 *         that match the given name. It returns an empty collection if non
	 *         were found
	 * @throws MetadataAccessException
	 *             is something goes awry
	 */
	public Collection<DataContainerGroup> findByName(String name,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// The collection to be returned
		Collection results = new ArrayList();

		// Make sure argument is not null
		logger.debug("findByName where name = " + name + " called.");
		if ((name == null) && (name.equals(""))) {
			return results;
		}

		try {
			// Formulate the query by name
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					name, exactMatch, orderByPropertyName,
					ascendingOrDescending);
			// Execute it and get the result
			results = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Now if the full object graph was requested, fill out the
		// relationships
		if (returnFullObjectGraph)
			results = getRealObjectsAndRelationships(results);

		// Return the results
		return results;
	}

	/**
	 * This method returns the number of results found by searching for
	 * <code>DataContainerGroup</code>s by name.
	 * 
	 * @see #findByName(String, boolean, String, String, boolean)
	 * @param name
	 * @param exactMatch
	 * @return the number of <code>DataContainerGroup</code>s that match the
	 *         name (and conditions) given
	 * @throws MetadataAccessException
	 */
	public int countFindByName(String name, boolean exactMatch)
			throws MetadataAccessException {

		// Make sure argument is not null
		logger.debug("findByName where name = " + name + " called.");
		if ((name == null) && (name.equals(""))) {
			return 0;
		}

		// The count to be returned
		int count = 0;

		// If one not found, look up by exact name
		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					name, exactMatch, null, null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the results
		return count;
	}

	/**
	 * This method returns a <code>Collection</code> of <code>String</code>s
	 * that are the names of all the <code>DataContainerGroup</code>s that are
	 * registered in SSDS.
	 * 
	 * @return a <code>Collection</code> of <code>String</code>s that are the
	 *         names of all <code>DataContainerGroups</code> in SSDS.
	 * @throws MetadataAccessException
	 *             if something went wrong in the method call.
	 */
	public Collection<String> findAllNames() throws MetadataAccessException {
		// The collection of names to return
		Collection dataContainerGroupNames = new ArrayList();

		// Create the query and run it
		try {
			Query query = getSession()
					.createQuery(
							"select distinct dataContainerGroup.name from "
									+ "DataContainerGroup dataContainerGroup order by dataContainerGroup.name");
			dataContainerGroupNames = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Now return the results
		return dataContainerGroupNames;
	}

	/**
	 * This method returns the number of <code>DataContainerGroup</code> names
	 * that would be returned by the call <code>findAllNames</code>.
	 * 
	 * @see #findAllNames()
	 * @return
	 * @throws MetadataAccessException
	 */
	public int countFindAllNames() throws MetadataAccessException {
		int count = 0;
		try {
			Long integerCount = (Long) getSession().createQuery(
					"select count(distinct dataContainerGroup.name) from "
							+ "DataContainerGroup dataContainerGroup")
					.uniqueResult();
			if (integerCount != null)
				count = integerCount.intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return count;
	}

	/**
	 * This method returns all the <code>DataContainerGroup</code>s that are
	 * associated with the given <code>DataContainer</code>.
	 * 
	 * @param dataContainer
	 *            the <code>DataContainer</code> that is to use to look up
	 *            <code>DataContainerGroup</code>s that are associated with it
	 * @return a <code>Collection</code> of <code>DataContainerGroup</code>s
	 *         that are associated with the given <code>DataContainer</code>. It
	 *         will return an empty collection if none are found
	 * @throws MetadataAccessException
	 *             if something goes awry
	 */
	public Collection<DataContainerGroup> findByDataContainer(
			DataContainer dataContainer, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// TODO kgomes 20060327 add orderByPropertyName and ascending or
		// descending.

		// The collection to return
		Collection dataContainerGroups = new ArrayList();

		// Make sure the given DataContainer is not null
		if (dataContainer != null) {

			// First check to see if the given DataContainer has a valid ID. If
			// not use the DataContainer DAO to look it up
			Long dataContainerId = dataContainer.getId();
			if ((dataContainerId == null) || (dataContainerId.longValue() <= 0)) {
				DataContainerDAO dataContainerDAO = new DataContainerDAO(
						this.getSession());
				dataContainerId = dataContainerDAO.findId(dataContainer);
			}
			// Now if something (somewhere) was found, use it to look up the
			// DataContainerGroups
			if (dataContainerId != null) {
				try {
					Query query = getSession().createQuery(
							"select dataContainer.dataContainerGroups from DataContainer dataContainer  "
									+ "where dataContainer.id = :id");
					query.setString("id", dataContainerId.toString());
					dataContainerGroups = query.list();
				} catch (HibernateException e) {
					throw new MetadataAccessException(e);
				}
			}
		}

		// If the full object graph is requested, fill out the relationships
		if (returnFullObjectGraph)
			dataContainerGroups = getRealObjectsAndRelationships(dataContainerGroups);

		// Return the result
		return dataContainerGroups;
	}

	/**
	 * This method returns the number of <code>DataContainerGroup</code>s that
	 * are associated with the given <code>DataContainer</code>.
	 * 
	 * @see #findByDataContainer(DataContainer, boolean)
	 * @param dataContainer
	 * @return the number of <code>DataContainerGroup</code>s associated with
	 *         the given <code>DataContainer</code>
	 * @throws MetadataAccessException
	 */
	public int countFindByDataContainer(DataContainer dataContainer)
			throws MetadataAccessException {

		// The count to return
		int count = 0;

		// Check to make sure the incoming DataContainer is not null
		if (dataContainer != null) {

			// Check to see if the incoming DataContainer has a valid ID
			Long dataContainerId = dataContainer.getId();
			if ((dataContainerId == null) || (dataContainerId.longValue() <= 0)) {
				// If no valid ID found, look up the matching ID
				DataContainerDAO dataContainerDAO = new DataContainerDAO(
						this.getSession());
				dataContainerId = dataContainerDAO.findId(dataContainer);
			}
			// Check if we have ID
			if (dataContainerId != null) {

				// Now create query and run it
				Collection dataContainerGroups = null;
				try {
					Query query = getSession().createQuery(
							"select dataContainer.dataContainerGroups from DataContainer dataContainer  "
									+ "where dataContainer.id = :id");
					query.setString("id", dataContainerId.toString());
					dataContainerGroups = query.list();
				} catch (HibernateException e) {
					throw new MetadataAccessException(e);
				}
				// If something was returned, grab the size
				if (dataContainerGroups != null)
					count = dataContainerGroups.size();
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

		// A flag to indicate if the DataContainerGroup was persisted before
		boolean persistedBefore = false;

		// Check incoming object to make sure it is of the right class
		DataContainerGroup dataContainerGroup = this
				.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		DataContainerGroup persistentDataContainerGroup = (DataContainerGroup) this
				.findEquivalentPersistentObject(metadataObject, false);

		// Create a handle that will be used to identify which object will be
		// persisted
		DataContainerGroup dataContainerGroupToPersist = null;

		// If there is a persistent one, copy over any non-null, changed fields
		if (persistentDataContainerGroup != null) {
			this.updateDestinationObject(dataContainerGroup,
					persistentDataContainerGroup);

			// Now set the flag to indicate it has been persisted in the past
			persistedBefore = true;

			// Now assign to the handle
			dataContainerGroupToPersist = persistentDataContainerGroup;
		} else {
			// Since this is new DataContainerGroup, make sure it's alternate
			// business key is there
			if ((dataContainerGroup.getName() == null)
					|| (dataContainerGroup.getName().equals(""))) {
				try {
					dataContainerGroup.setName("DataContainerGroup_"
							+ getUniqueNameSuffix());
				} catch (MetadataException e) {
					logger.error("MetadataException caught trying to set an "
							+ "auto-generated name on a DataContainerGroup:"
							+ e.getMessage());
				}
				addMessage(
						ssdsAdminEmailToAddress,
						"An incoming DataContainerGroup did not have a name, "
								+ "so SSDS auto-generated one:<br><ul><li>"
								+ dataContainerGroup
										.toStringRepresentation("<li>")
								+ "</ul><br>");
			}

			// Clear the flag
			persistedBefore = false;

			// Assign to the handle
			dataContainerGroupToPersist = dataContainerGroup;
		}

		// If this is not a previously persisted on, save it
		if (!persistedBefore)
			getSession().save(dataContainerGroupToPersist);

		// Now return the ID
		if (dataContainerGroupToPersist != null) {
			return dataContainerGroupToPersist.getId();
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
		DataContainerGroup dataContainerGroup = this
				.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		DataContainerGroup persistentDataContainerGroup = (DataContainerGroup) this
				.findEquivalentPersistentObject(dataContainerGroup, false);
		// If no matching dataContainerGroup was found, throw an exception
		if (persistentDataContainerGroup == null) {
			logger.debug("No matching dataContainerGroup could be found in the persistent store, "
					+ "no delete performed");
		} else {
			// If we are here, we need to make sure all the connections to any
			// DataContainers are removed before removing the DataContainerGroup

			// Grab the DataContainerDAO
			DataContainerDAO dcDAO = new DataContainerDAO(getSession());
			// Grab all the associated DataContainers
			Collection associatedDCs = dcDAO.findByDataContainerGroup(
					persistentDataContainerGroup, null, null, true);

			// If they are there, loop through and break the connections
			if (associatedDCs != null) {
				Iterator associatedDCsIter = associatedDCs.iterator();
				while (associatedDCsIter.hasNext()) {
					DataContainer tempDC = (DataContainer) associatedDCsIter
							.next();
					// Remove the group
					tempDC.removeDataContainerGroup(persistentDataContainerGroup);
				}
			}

			logger.debug("Existing object was found, so we will try to delete it");
			try {
				getSession().delete(persistentDataContainerGroup);
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
	 *            <code>DataContainerGroup</code>
	 * @return a <code>DataContainerGroup</code> that is same object that came
	 *         in
	 * @throws MetadataAccessException
	 *             if something is wrong
	 */
	private DataContainerGroup checkIncomingMetadataObject(
			IMetadataObject metadataObject) throws MetadataAccessException {

		// Check for null argument
		if (metadataObject == null) {
			throw new MetadataAccessException(
					"Failed: incoming DataContainerGroup was null");
		}

		// Try to cast the incoming object into the correct class
		DataContainerGroup dataContainerGroup = null;
		try {
			dataContainerGroup = (DataContainerGroup) metadataObject;
		} catch (ClassCastException cce) {
			throw new MetadataAccessException(
					"Could not cast the incoming object into a DataContainerGroup");
		}
		return dataContainerGroup;
	}

	private Criteria formulatePropertyCriteria(boolean countQuery, Long id,
			String name, boolean exactNameMatch, String orderByProperty,
			String ascendingOrDescending) throws MetadataAccessException {
		// The Criteria to return
		Criteria criteria = getSession().createCriteria(
				DataContainerGroup.class);
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
	// // For DataContainer Group, there is nothing to do
	// }

	/**
	 * The Log4J Logger
	 */
	static Logger logger = Logger.getLogger(DataContainerGroupDAO.class);
}
