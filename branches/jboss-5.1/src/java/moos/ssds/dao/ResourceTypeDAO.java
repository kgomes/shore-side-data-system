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
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.ResourceType;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

public class ResourceTypeDAO extends MetadataDAO {

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
	public ResourceTypeDAO(Session session) throws MetadataAccessException {
		super(ResourceType.class, session);
	}

	public IMetadataObject findEquivalentPersistentObject(
			IMetadataObject metadataObject, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		logger.debug("findEquivalentPersistentObject called");
		// The resourceType to return
		ResourceType resourceType = this
				.checkIncomingMetadataObject(metadataObject);

		// Loop through any persistent resourceType with the same name and check
		// for equality
		ResourceType resourceTypeToReturn = null;
		if (resourceType.getId() != null) {
			resourceTypeToReturn = (ResourceType) this.findById(
					resourceType.getId(), false);
		}
		if (resourceTypeToReturn == null) {
			Collection persistentResourceTypes = this.findByName(
					resourceType.getName(), true);
			if (persistentResourceTypes != null) {
				Iterator dps = persistentResourceTypes.iterator();
				// Just return the first one
				while (dps.hasNext()) {
					return (ResourceType) dps.next();
				}
			}
		}

		if ((returnFullObjectGraph) && (resourceTypeToReturn != null))
			resourceTypeToReturn = (ResourceType) this
					.getMetadataObjectGraph(resourceTypeToReturn);

		// If we are here, return nothing
		return resourceTypeToReturn;
	}

	public Collection findAllIDs() throws MetadataAccessException {
		Collection resourceTypeIDs = null;

		// Create the query and run it
		try {
			Query query = getSession()
					.createQuery(
							"select distinct resourceType.id from "
									+ "ResourceType resourceType order by resourceType.id");
			resourceTypeIDs = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return resourceTypeIDs;
	}

	/**
	 * @see IMetadataDAO#countFindAllIDs()
	 */
	public int countFindAllIDs() throws MetadataAccessException {
		// The count
		int count = 0;
		try {
			Long longCount = (Long) getSession().createQuery(
					"select count(distinct resourceType.id) from "
							+ "ResourceType resourceType").uniqueResult();
			if (longCount != null)
				count = longCount.intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the result
		return count;
	}

	@SuppressWarnings("unchecked")
	public Collection<ResourceType> findByName(String name, boolean exactMatch)
			throws MetadataAccessException {
		// Make sure argument is not null
		logger.debug("findByName where name = " + name + " called.");
		if ((name == null) || (name.equals(""))) {
			return new ArrayList<ResourceType>();
		}

		// The collection to be returned
		Collection<ResourceType> results = new ArrayList<ResourceType>();

		// Construct and run the query
		try {
			if (!exactMatch) {
				try {
					Query query = getSession()
							.createQuery(
									"select distinct resourceType from "
											+ "ResourceType resourceType where resourceType.name like :name");
					query.setString("name", "%" + name + "%");
					results = query.list();
				} catch (HibernateException e) {
					throw new MetadataAccessException(e);
				}
			} else {
				try {
					Query query = getSession()
							.createQuery(
									"select distinct resourceType from "
											+ "ResourceType resourceType where resourceType.name = :name");
					query.setString("name", name);
					results = query.list();
				} catch (HibernateException e) {
					throw new MetadataAccessException(e);
				}
			}
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the results
		return results;
	}

	@SuppressWarnings("unchecked")
	public Collection<String> findAllNames() throws MetadataAccessException {
		Collection<String> resourceTypeNames = null;

		// Create the query and run it
		try {
			Query query = getSession()
					.createQuery(
							"select distinct resourceType.name from "
									+ "ResourceType resourceType order by resourceType.name");
			resourceTypeNames = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return resourceTypeNames;
	}

	/**
	 * @see IMetadataDAO#makePersistent(IMetadataObject)
	 */
	public Long makePersistent(IMetadataObject metadataObject)
			throws MetadataAccessException {

		logger.debug("makePersistent called");
		// A flag to indicate if the ResourceType has been persisted before
		boolean persistedBefore = false;

		// Check incoming object
		ResourceType resourceType = this
				.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		ResourceType persistentResourceType = (ResourceType) this
				.findEquivalentPersistentObject(metadataObject, false);

		// This is a handle to the instance of the ResourceType that will
		// actually be persisted
		ResourceType resourceTypeToPersist = null;

		// If there is an existing persistent instance, copy over an changed,
		// non-null fields to the persistent instance and assign the persistent
		// instance to the handle defined above
		if (persistentResourceType != null) {
			logger.debug("The search for an existing ResourceType returned "
					+ persistentResourceType.toStringRepresentation("|"));

			// Copy over updated, non-null fields
			String resourceTypeStringBefore = persistentResourceType
					.toStringRepresentation("<li>");
			if (this.updateDestinationObject(resourceType,
					persistentResourceType)) {
				addMessage(
						ssdsAdminEmailToAddress,
						"A ResourceType was updated<br><b>Before</b><ul><li>"
								+ resourceTypeStringBefore
								+ "</ul><br><b>After</b><ul><li>"
								+ persistentResourceType
										.toStringRepresentation("<li>")
								+ "</ul><br>");
			}

			// Set the flag to indicate it was persisted before
			persistedBefore = true;

			// Now assign it to the handle
			resourceTypeToPersist = persistentResourceType;
		} else {
			logger.debug("No matching persistent ResourceType was found");
			// Since this is a new ResourceType, make sure the business key
			// (name) is not null
			if ((resourceType.getName() == null)
					|| (resourceType.getName().equals(""))) {
				try {
					resourceType.setName("ResourceType_"
							+ getUniqueNameSuffix());
				} catch (MetadataException e) {
					logger.error("MetadataException trying to set the name on the ResourceType");
				}
				addMessage(ssdsAdminEmailToAddress,
						"A ResourceType came in with no name, so SSDS auto-generated one:<br><ul><li>"
								+ resourceType.toStringRepresentation("<li>")
								+ "</ul><br>");
			}

			// Clear the persisted before flag
			persistedBefore = false;

			// Assign it to the handle
			resourceTypeToPersist = resourceType;
		}

		// Now if not persisted before, save it
		if (!persistedBefore) {
			getSession().save(resourceTypeToPersist);
			addMessage(
					ssdsAdminEmailToAddress,
					"A new ResourceType was created in SSDS:<br><ul><li>"
							+ resourceTypeToPersist
									.toStringRepresentation("<li>")
							+ "</ul><br>");
		}

		// Now return the ID
		if (resourceTypeToPersist != null) {
			return resourceTypeToPersist.getId();
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
		ResourceType resourceType = this
				.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		ResourceType persistentResourceType = (ResourceType) this
				.findEquivalentPersistentObject(resourceType, false);
		logger.debug("The search for existing object returned "
				+ persistentResourceType);

		// If no matching resourceType was found, throw an exception
		if (persistentResourceType == null) {
			logger.debug("No matching resourceType could be found in the persistent store, "
					+ "no delete performed");
		} else {

			// Now before removing, we must make sure we clear out any
			// relationships with Resource
			Collection resourcesByResourceType = null;
			ResourceDAO resourceDAO = new ResourceDAO(getSession());
			resourcesByResourceType = resourceDAO.findByResourceType(
					persistentResourceType, null, null, false);
			if ((resourcesByResourceType != null)
					&& (resourcesByResourceType.size() > 0)) {
				Iterator resourceIterator = resourcesByResourceType.iterator();
				while (resourceIterator.hasNext()) {
					Resource resource = (Resource) resourceIterator.next();
					resource.setResourceType(null);
				}
			}
			// If the incoming resourceType had an ID, make sure it matches with
			// the persistent instance
			logger.debug("Existing object was found, so we will try to delete it");
			try {
				getSession().delete(persistentResourceType);
				addMessage(
						ssdsAdminEmailToAddress,
						"A ResourceType was deleted from SSDS:<br><ul><li>"
								+ persistentResourceType
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
	 *            <code>ResourceType</code>
	 * @return a <code>ResourceType</code> that is same object that came in
	 * @throws MetadataAccessException
	 *             if something is wrong
	 */
	private ResourceType checkIncomingMetadataObject(
			IMetadataObject metadataObject) throws MetadataAccessException {

		// Check for null argument
		if (metadataObject == null) {
			throw new MetadataAccessException(
					"Failed: incoming ResourceType was null");
		}

		// Try to cast the incoming object into the correct class
		ResourceType resourceType = null;
		try {
			resourceType = (ResourceType) metadataObject;
		} catch (ClassCastException cce) {
			throw new MetadataAccessException(
					"Could not cast the incoming object into a ResourceType");
		}
		return resourceType;
	}

	// protected void initializeRelationships(IMetadataObject metadataObject)
	// throws MetadataAccessException {
	//
	// }

	/**
	 * The Log4J Logger
	 */
	static Logger logger = Logger.getLogger(ResourceTypeDAO.class);
}
