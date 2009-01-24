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
import java.util.HashSet;
import java.util.Iterator;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.Keyword;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.ResourceBLOB;
import moos.ssds.metadata.ResourceType;
import moos.ssds.metadata.Software;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

public class ResourceDAO extends MetadataDAO {

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
	public ResourceDAO(Session session) throws MetadataAccessException {
		super(Resource.class, session);
	}

	public IMetadataObject findEquivalentPersistentObject(
			IMetadataObject metadataObject, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		logger.debug("findEquivalentPersistentObject called");
		// First try to cast to a DataContainer
		Resource resource = this.checkIncomingMetadataObject(metadataObject);

		// The id that will be returned
		Resource resourceToReturn = null;
		if (resource.getId() != null)
			resourceToReturn = (Resource) this
					.findById(resource.getId(), false);
		if (resourceToReturn == null)
			resourceToReturn = this.findByURIString(resource.getUriString());

		// Double check that if the incoming resource has an ID, it matches
		// the one that was found with the matching URI string
		if ((resource.getId() != null) && (resourceToReturn != null)) {
			if (resource.getId().longValue() != resourceToReturn.getId()
					.longValue()) {
				logger.error("The ID and the URI of the incoming Resource "
						+ "did not match a ID/URI combination of "
						+ "anything in the persistent store, this should "
						+ "not happen");
				throw new MetadataAccessException(
						"The ID and the URI of the incoming Resource "
								+ "did not match a ID/URI combination of "
								+ "anything in the persistent store, this should "
								+ "not happen");
			}
		}

		if ((returnFullObjectGraph) && (resourceToReturn != null))
			resourceToReturn = (Resource) this
					.getMetadataObjectGraph(resourceToReturn);

		logger.debug("OK, returning the persistent resource: "
				+ resourceToReturn);
		return resourceToReturn;
	}

	public Collection findAllIDs() throws MetadataAccessException {
		return null;
	}

	/**
	 * @see IMetadataDAO#countFindAllIDs()
	 */
	public int countFindAllIDs() throws MetadataAccessException {
		// The count
		int count = 0;
		try {
			Long longCount = (Long) getSession().createQuery(
					"select count(distinct resource.id) from "
							+ "Resource resource").uniqueResult();
			if (longCount != null)
				count = longCount.intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the result
		return count;
	}

	public Collection findByName(String name) throws MetadataAccessException {
		return null;
	}

	public Collection findByLikeName(String likeName)
			throws MetadataAccessException {
		return null;
	}

	public Collection findAllNames() throws MetadataAccessException {
		return null;
	}

	public Resource findByURIString(String uriString)
			throws MetadataAccessException {
		// First make sure the incoming value is not null
		if ((uriString == null) || (uriString.equals(""))) {
			logger.debug("Failed: incoming uriString was null or empty");
			return null;
		}

		// Create the Resource to return
		Resource resourceToReturn = null;

		// Grab a session and run the query
		try {
			Query query = getSession().createQuery(
					"from Resource r where r.uriString = :uriString");
			query.setString("uriString", uriString);
			resourceToReturn = (Resource) query.uniqueResult();
		} catch (HibernateException e) {
			logger.error("HibernateException caught (will be re-thrown):"
					+ e.getMessage());
			throw new MetadataAccessException(e);
		}

		// Return the result
		return resourceToReturn;
	}

	public Collection findByURI(URI uri) throws MetadataAccessException {
		return null;
	}

	public Collection findByURL(URL url) throws MetadataAccessException {
		return null;
	}

	public Collection findByMimeType(String mimeType)
			throws MetadataAccessException {
		return null;
	}

	public Collection findByPerson(Person person)
			throws MetadataAccessException {
		return null;
	}

	/**
	 * This method returns all <code>Resource</code>s that are of a certain
	 * <code>ResourceType</code>
	 */
	public Collection findByResourceType(ResourceType resourceType,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// The collection to return
		Collection resources = new HashSet();

		// First validate the incoming deviceType
		if (resourceType == null) {
			return resources;
		}

		// If the resourceType already has an ID, use that, if not look up the
		// ID
		Long resourceTypeId = null;
		if ((resourceType.getId() != null)
				&& (resourceType.getId().longValue() > 0)) {
			resourceTypeId = resourceType.getId();
		} else {
			// Get the ResourceTypeDAO and look up the ID
			ResourceTypeDAO resourceTypeDAO = new ResourceTypeDAO(getSession());
			resourceTypeId = resourceTypeDAO.findId(resourceType);
		}

		// If no ID was found, throw an exception
		if (resourceTypeId == null) {
			throw new MetadataAccessException(
					"No ResourceType was found in the data store"
							+ " that matched the specified ResourceType");
		}

		// Construct the query and run it
		StringBuffer queryStringBuffer = new StringBuffer();
		queryStringBuffer.append("select distinct from Resource resource ");
		queryStringBuffer
				.append("where resource.resourceType.id = :resourceTypeId ");
		// Add order by clause
		if (this.checkIfPropertyOK(orderByPropertyName)) {
			queryStringBuffer.append(this.getOrderByPropertyNameSQLClause(
					orderByPropertyName, ascendingOrDescending));
		}
		try {
			Query query = getSession()
					.createQuery(queryStringBuffer.toString());
			query.setString("resourceTypeId", resourceTypeId.toString());
			logger.debug("Compiled query = " + query.getQueryString());
			resources = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Check for full object graphs
		if (returnFullObjectGraph)
			this.initializeRelationships(resources);

		// Now return the results
		return resources;
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
		sqlStringBuffer.append("select distinct resource "
				+ "from Resource resource "
				+ "join resource.keywords keyword where keyword.name ");
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
		sqlStringBuffer.append("select count(distinct resource) "
				+ "from Resource resource "
				+ "join resource.keywords keyword where keyword.name ");
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
	 * @see IMetadataDAO#makePersistent(IMetadataObject)
	 */
	public Long makePersistent(IMetadataObject metadataObject)
			throws MetadataAccessException {

		logger.debug("makePersistent called");

		// This is a flag to indicate if the resource has been persisted in the
		// past
		boolean persistedBefore = false;

		// Check incoming object
		Resource resource = this.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		Resource persistentResource = (Resource) this
				.findEquivalentPersistentObject(metadataObject, false);

		// A handle for the resource that will actually be persisted
		Resource resourceToPersist = null;

		// If there is an existing persistent resource, copy over all changed,
		// non-null fields and assign to the handle
		if (persistentResource != null) {
			logger.debug("A matching resource was found and is "
					+ persistentResource.toStringRepresentation("|"));
			// Copy over
			String resourceBefore = persistentResource
					.toStringRepresentation("<li>");
			if (this.updateDestinationObject(resource, persistentResource)) {
				addMessage(ssdsAdminEmailToAddress,
						"A Resource was updated in SSDS:<br><b>Before</b><ul><li>"
								+ resourceBefore
								+ "</ul><br><b>After</b><ul><li>"
								+ persistentResource
										.toStringRepresentation("<li>"));
			}

			// Set the flag
			persistedBefore = true;

			// Assign to the handle
			resourceToPersist = persistentResource;
		} else {
			logger.debug("No matching resource was found");
			// Now since this will be a new Resource, let's make sure the
			// alternate key is valid. For resource it is the URI
			if ((resource.getUriString() == null)
					|| (resource.getUriString().equals(""))) {
				try {
					resource.setUriString("http://ssds.mbari.org/resource/"
							+ getUniqueNameSuffix());
				} catch (MetadataException e) {
					logger.error("MetadataException caught trying to set "
							+ "the URIString on an incoming resource:"
							+ e.getMessage());
				}
				addMessage(
						ssdsAdminEmailToAddress,
						"An incoming Resource did not have a URI associated "
								+ "with it so SSDS auto-generated one:<br><ul><li>"
								+ resource.toStringRepresentation("<li>")
								+ "</ul><br>");

			}

			// Clear the persisted before flag
			persistedBefore = false;

			// Now attach to the handle
			resourceToPersist = resource;
		}

		// -------------------------
		// ResourceBLOB Relationship
		// -------------------------
		// The resource BLOB relationship is a fully cascaded relationship so it
		// will be managed a bit differently. First let's see if there is a
		// ResourceBLOB attached to Resource. If not there is nothing really to
		// do
		if (resource.getResourceBLOB() != null) {
			// Now, since we have one on the incoming object the only thing we
			// really have to know is if the Resource was persisted before, we
			// will have to remove the old BLOB (if there is one) and attached
			// this one.
			if (persistedBefore) {
				if (resourceToPersist.getResourceBLOB() != null) {
					ResourceBLOB resourceBLOBToDelete = resourceToPersist
							.getResourceBLOB();
					resourceToPersist.setResourceBLOB(null);
					getSession().delete(resourceBLOBToDelete);
				}
				resourceToPersist.setResourceBLOB(resource.getResourceBLOB());
			}
		}

		// -------------------------
		// ResourceType Relationship
		// -------------------------
		// First see if there is a resourceType associated with the incoming
		// resource
		if (resource.getResourceType() != null) {
			// Now, since there is, check to see if it has been initialized
			if (Hibernate.isInitialized(resource.getResourceType())) {
				// Grab the ResourceType DAO to handle that relationship
				ResourceTypeDAO resourceTypeDAO = new ResourceTypeDAO(this
						.getSession());

				// Now persist the resourceType
				ResourceType tempResourceType = resource.getResourceType();
				resourceTypeDAO.makePersistent(tempResourceType);

				// The matching resourceType that is in the session
				ResourceType tempResourceTypeInSession = null;

				// Check to see if the persisted resourceType is in the session
				if (!getSession().contains(tempResourceType)) {
					tempResourceTypeInSession = (ResourceType) resourceTypeDAO
							.findEquivalentPersistentObject(tempResourceType,
									false);
				} else {
					tempResourceTypeInSession = tempResourceType;
				}

				// Now check to see if the resource was persisted in the past,
				// if
				// so, just check to see if resource's resourceType is different
				// and
				// update it if so
				if (persistedBefore) {
					if ((resourceToPersist.getResourceType() == null)
							|| (!resourceToPersist.getResourceType().equals(
									tempResourceTypeInSession))) {
						resourceToPersist
								.setResourceType(tempResourceTypeInSession);
					}
				} else {
					// Make sure the resourceType associated with the resource
					// is the session, if not replace it with the one that is
					if (!getSession().contains(
							resourceToPersist.getResourceType())) {
						resourceToPersist
								.setResourceType(tempResourceTypeInSession);
					}
				}
			}
		}

		// -------------------
		// Person Relationship
		// -------------------
		// First see if there is a person associated with the incoming resource
		if (resource.getPerson() != null) {
			// Now, since there is, check to see if it has been initialized
			if (Hibernate.isInitialized(resource.getPerson())) {
				// Grab the Person DAO to handle that relationship
				PersonDAO personDAO = new PersonDAO(this.getSession());

				// Now persist the person
				Person tempPerson = resource.getPerson();
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

				// Now check to see if the resource was persisted in the past,
				// if
				// so, just check to see if resources person is different and
				// update it if so
				if (persistedBefore) {
					if ((resourceToPersist.getPerson() == null)
							|| (!resourceToPersist.getPerson().equals(
									tempPersonInSession))) {
						resourceToPersist.setPerson(tempPersonInSession);
					}
				} else {
					// Make sure the person associated with the resource is the
					// session, if not replace it with the one that is
					if (!getSession().contains(resourceToPersist.getPerson())) {
						resourceToPersist.setPerson(tempPersonInSession);
					}
				}
			}
		}

		// ---------------------
		// Keyword Relationship
		// ----------------------
		// First make sure the keyword relationship exists
		if (resource.getKeywords() != null) {
			// Now check to make sure it is initialized. If it is not, no
			// changes have taken place and we don't need to do anything
			if (Hibernate.isInitialized(resource.getKeywords())) {

				// Grab the DAO for Keyword
				KeywordDAO keywordDAO = new KeywordDAO(this.getSession());

				// Make sure the are keywords to iterate over
				if (resource.getKeywords().size() > 0) {

					// Now iterate over the Keywords and persist them
					Iterator keywordIter = resource.getKeywords().iterator();
					while (keywordIter.hasNext()) {
						Keyword tempKeyword = (Keyword) keywordIter.next();
						keywordDAO.makePersistent(tempKeyword);
					}
				}

				// Create a copy of the collection associated with the resource
				// to
				// prevent concurrent modifications
				Collection resourceKeywordCopy = new ArrayList(resource
						.getKeywords());

				// Now we need to make the correct associations. Currently, you
				// have a collection of Keyword objects that have their values
				// marked for persistence. Now the object will either be in the
				// session or not depending on if they were previously
				// persisted.
				Iterator resourceKeywordCopyIterator = resourceKeywordCopy
						.iterator();
				while (resourceKeywordCopyIterator.hasNext()) {
					Keyword currentKeyword = (Keyword) resourceKeywordCopyIterator
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
					// Now if the parent resource was persisted before, just
					// check to make sure the sessioned Keywords is in the
					// collection are associated with the resource that will be
					// persisted
					if (persistedBefore) {
						if (!resourceToPersist.getKeywords().contains(
								currentKeywordInSession))
							resourceToPersist.getKeywords().add(
									currentKeywordInSession);
					} else {
						// This means that the resource has not been persisted
						// before. If the Keyword is already in the session,
						// there is nothing to do, but if not, we need to
						// replace it with the sessioned one
						if (!getSession().contains(currentKeyword)) {
							resourceToPersist.getKeywords().remove(
									currentKeyword);
							resourceToPersist.getKeywords().add(
									currentKeywordInSession);
						}
					}
				}
			}
		}

		// If it was not persisted in the past, save the new one
		if (!persistedBefore) {
			getSession().save(resourceToPersist);
		}

		// Now return the ID
		if (resourceToPersist != null) {
			return resourceToPersist.getId();
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
		Resource resource = this.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		Resource persistentResource = (Resource) this
				.findEquivalentPersistentObject(resource, false);

		// If no matching resource was found, throw an exception
		if (persistentResource == null) {
			logger
					.debug("No matching resource could be found in the persistent store, "
							+ "no delete performed");
		} else {
			logger.debug("The search for existing object returned "
					+ persistentResource);

			// Now before we delete it, we must clear up the relationships with
			// 1. Person
			// 2. ResourceType
			// 3. ResourceBLOB (nothing as this is cascade delete)
			// 4. Keywords
			// 5. DataProducers
			// 6. DataContainers
			// 7. Devices
			// 8. Softwares

			// Person
			persistentResource.setPerson(null);

			// ResourceType
			persistentResource.setResourceType(null);

			// Keywords
			persistentResource.clearKeywords();

			// DataProducers
			DataProducerDAO dpDAO = new DataProducerDAO(getSession());
			Collection associatedDPs = dpDAO.findByResource(persistentResource,
					null, null, false);
			if (associatedDPs != null) {
				Iterator associatedDPsIter = associatedDPs.iterator();
				while (associatedDPsIter.hasNext()) {
					DataProducer tempDP = (DataProducer) associatedDPsIter
							.next();
					tempDP.removeResource(persistentResource);
				}
			}

			// DataContainers
			DataContainerDAO dcDAO = new DataContainerDAO(getSession());
			Collection associatedDCs = dcDAO.findByResource(persistentResource,
					null, null, false);
			if (associatedDCs != null) {
				Iterator associatedDCsIter = associatedDCs.iterator();
				while (associatedDCsIter.hasNext()) {
					DataContainer tempDC = (DataContainer) associatedDCsIter
							.next();
					tempDC.removeResource(persistentResource);
				}
			}

			// Devices
			DeviceDAO deviceDAO = new DeviceDAO(getSession());
			Collection associatedDevices = deviceDAO.findByResource(
					persistentResource, null, null, false);
			if (associatedDevices != null) {
				Iterator associatedDevicesIter = associatedDevices.iterator();
				while (associatedDevicesIter.hasNext()) {
					Device tempDevice = (Device) associatedDevicesIter.next();
					tempDevice.removeResource(persistentResource);
				}
			}

			// Softwares
			SoftwareDAO softwareDAO = new SoftwareDAO(getSession());
			Collection associatedSoftwares = softwareDAO.findByResource(
					persistentResource, null, null, false);
			if (associatedSoftwares != null) {
				Iterator associatedSoftwaresIter = associatedSoftwares
						.iterator();
				while (associatedSoftwaresIter.hasNext()) {
					Software tempSoftware = (Software) associatedSoftwaresIter
							.next();
					tempSoftware.removeResource(persistentResource);
				}
			}

			// Now that the relationships are clear, remove the resource
			logger
					.debug("Existing object was found, so we will try to delete it");
			try {
				getSession().delete(persistentResource);
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
	 *            <code>Resource</code>
	 * @return a <code>Resource</code> that is same object that came in
	 * @throws MetadataAccessException
	 *             if something is wrong
	 */
	private Resource checkIncomingMetadataObject(IMetadataObject metadataObject)
			throws MetadataAccessException {

		// Check for null argument
		if (metadataObject == null) {
			throw new MetadataAccessException(
					"Failed: incoming Resource was null");
		}

		// Try to cast the incoming object into the correct class
		Resource resource = null;
		try {
			resource = (Resource) metadataObject;
		} catch (ClassCastException cce) {
			throw new MetadataAccessException(
					"Could not cast the incoming object into a Resource");
		}
		return resource;
	}

	protected void initializeRelationships(IMetadataObject metadataObject)
			throws MetadataAccessException {
		if (metadataObject == null)
			return;
		Resource resource = this.checkIncomingMetadataObject(metadataObject);

		if (resource.getPerson() != null)
			Hibernate.initialize(resource.getPerson());
		if (resource.getResourceBLOB() != null)
			Hibernate.initialize(resource.getResourceBLOB());
		if (resource.getResourceType() != null)
			Hibernate.initialize(resource.getResourceType());
		if (resource.getKeywords() != null) {
			Hibernate.initialize(resource.getKeywords());
			resource.getKeywords().size();
		}
	}

	/**
	 * The Log4J Logger
	 */
	static Logger logger = Logger.getLogger(ResourceDAO.class);

}
