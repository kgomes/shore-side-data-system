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
import java.util.Iterator;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.Person;
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

public class SoftwareDAO extends MetadataDAO {

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
	public SoftwareDAO(Session session) throws MetadataAccessException {
		super(Software.class, session);
	}

	public IMetadataObject findEquivalentPersistentObject(
			IMetadataObject metadataObject, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		logger.debug("findEquivalentPersistentObject called");
		// First try to cast to a Software
		Software software = this.checkIncomingMetadataObject(metadataObject);

		// The id that will be returned
		Software softwareToReturn = null;
		if (software.getId() != null)
			softwareToReturn = (Software) this.findById(software.getId(),
					returnFullObjectGraph);
		if (softwareToReturn == null)
			softwareToReturn = this.findByNameAndSoftwareVersion(software
					.getName(), software.getSoftwareVersion(),
					returnFullObjectGraph);

		// Double check that if the incoming software has an ID, it matches
		// the one that was found with the matching URI string
		if ((software.getId() != null) && (softwareToReturn != null)) {
			if (software.getId().longValue() != softwareToReturn.getId()
					.longValue()) {
				logger.error("The ID and the dates of the incoming Software "
						+ "did not match a ID/dates combination of "
						+ "anything in the persistent store, this should "
						+ "not happen");
				throw new MetadataAccessException(
						"The ID and the dates of the incoming Software "
								+ "did not match a ID/dates combination of "
								+ "anything in the persistent store, this should "
								+ "not happen");
			}
		}

		logger.debug("OK, returning the persistent software: "
				+ softwareToReturn);
		return softwareToReturn;
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
					"select count(distinct software.id) from "
							+ "Software software").uniqueResult();
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

	public Software findByNameAndSoftwareVersion(String name,
			String softwareVersion, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// First check to see if the parameters are OK
		if ((name == null) || (name.equals("")) || (softwareVersion == null)
				|| (softwareVersion.equals("")))
			return null;

		// Now build and perform the query
		Software softwareToReturn = (Software) this.getSession()
				.createCriteria(Software.class).add(
						Restrictions.eq("name", name)).add(
						Restrictions.eq("softwareVersion", softwareVersion))
				.uniqueResult();

		// Check for full object graph
		if ((softwareToReturn != null) && (returnFullObjectGraph))
			this.initializeRelationships(softwareToReturn);

		// Now return it
		return softwareToReturn;
	}

	public Collection findByURIString(String uriString)
			throws MetadataAccessException {
		return null;
	}

	public Collection findByURI(URI uri) throws MetadataAccessException {
		return null;
	}

	public Collection findByURL(URL url) throws MetadataAccessException {
		return null;
	}

	public Collection findByPerson(Person person)
			throws MetadataAccessException {
		return null;
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

		sqlStringBuffer.append("select distinct software from "
				+ "Software software, Resource resource where");
		sqlStringBuffer.append(" resource.id = :resourceID and ");
		sqlStringBuffer.append(" resource in elements(software.resources)");

		if ((orderByPropertyName != null)
				&& (checkIfPropertyOK(orderByPropertyName))) {
			sqlStringBuffer.append(" order by software." + orderByPropertyName);
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
	 * @see IMetadataDAO#makePersistent(IMetadataObject)
	 */
	public Long makePersistent(IMetadataObject metadataObject)
			throws MetadataAccessException {

		logger.debug("makePersistent called");

		// A flag to indicate if it was persisted before
		boolean persistedBefore = false;

		// Check incoming object
		Software software = this.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		Software persistentSoftware = (Software) this
				.findEquivalentPersistentObject(software, false);

		// A handle for the actual Software object to be persisted
		Software softwareToPersist = null;

		// If it was persisted before, copy over any non-null, changed fields
		// and attach to persisting handle
		if (persistentSoftware != null) {
			this.updateDestinationObject(software, persistentSoftware);

			// Set the flag
			persistedBefore = true;

			// Attach to handle
			softwareToPersist = persistentSoftware;
		} else {
			// Since it is a new Software, make sure the alternate keys exist
			if ((software.getName() == null) || (software.getName().equals(""))) {
				try {
					software.setName("Software_" + getUniqueNameSuffix());
				} catch (MetadataException e) {
					logger.error("MetadataException caught trying to "
							+ "auto-generate a name for a Software: "
							+ e.getMessage());
				}
				addMessage(ssdsAdminEmailToAddress,
						"An incoming software had no name so "
								+ "SSDS auto-generated one:<br><ul><li>"
								+ software.toStringRepresentation("<li>")
								+ "</ul><br>");
			}
			if ((software.getSoftwareVersion() == null)
					|| (software.getSoftwareVersion().equals(""))) {
				try {
					software.setSoftwareVersion("Software_Version_"
							+ getUniqueNameSuffix());
				} catch (MetadataException e) {
					logger
							.error("MetadataException caught trying to "
									+ "auto-generate a softwareVersion for a Software: "
									+ e.getMessage());
				}
				addMessage(ssdsAdminEmailToAddress,
						"An incoming software had no softwareVersion so "
								+ "SSDS auto-generated one:<br><ul><li>"
								+ software.toStringRepresentation("<li>")
								+ "</ul><br>");
			}

			// Clear the flag
			persistedBefore = false;

			// Attach to handle
			softwareToPersist = software;
		}

		// -------------------
		// Person Relationship
		// -------------------
		// First see if there is a person associated with the incoming software
		if (software.getPerson() != null) {
			// Now, since there is, check to see if it has been initialized
			if (Hibernate.isInitialized(software.getPerson())) {
				// Grab the Person DAO to handle that relationship
				PersonDAO personDAO = new PersonDAO(this.getSession());

				// Now persist the person
				Person tempPerson = software.getPerson();
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

				// Now check to see if the software was persisted in the past,
				// if
				// so, just check to see if softwares person is different and
				// update it if so
				if (persistedBefore) {
					if ((softwareToPersist.getPerson() == null)
							|| (!softwareToPersist.getPerson().equals(
									tempPersonInSession))) {
						softwareToPersist.setPerson(tempPersonInSession);
					}
				} else {
					// Make sure the person associated with the software is the
					// session, if not replace it with the one that is
					if (!getSession().contains(softwareToPersist.getPerson())) {
						softwareToPersist.setPerson(tempPersonInSession);
					}
				}
			}
		}

		// ---------------------
		// Resource Relationship
		// ----------------------
		// First make sure the resources relationship exists
		if (software.getResources() != null) {
			// Now check to make sure it is initialized. If it is not, no
			// changes have taken place and we don't need to do anything
			if (Hibernate.isInitialized(software.getResources())) {

				// Grab the DAO for Resource
				ResourceDAO resourceDAO = new ResourceDAO(this.getSession());

				// Make sure the are resources to iterate over
				if (software.getResources().size() > 0) {

					// Now iterate over the Resources and persist them
					Iterator userGroupIter = software.getResources().iterator();
					while (userGroupIter.hasNext()) {
						Resource tempResource = (Resource) userGroupIter.next();
						resourceDAO.makePersistent(tempResource);
					}
				}

				// Create a copy of the collection associated with the software
				// to
				// prevent concurrent modifications
				Collection softwareResourceCopy = new ArrayList(software
						.getResources());

				// Now we need to make the correct associations. Currently, you
				// have a collection of Resource objects that have their values
				// marked for persistence. Now the object will either be in the
				// session or not depending on if they were previously
				// persisted.
				Iterator softwareResourceCopyIterator = softwareResourceCopy
						.iterator();
				while (softwareResourceCopyIterator.hasNext()) {
					Resource currentResource = (Resource) softwareResourceCopyIterator
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
					// Now if the parent software was persisted before, just
					// check to make sure the sessioned Resources is in the
					// collection are associated with the software that will be
					// persisted
					if (persistedBefore) {
						if (!softwareToPersist.getResources().contains(
								currentResourceInSession))
							softwareToPersist.getResources().add(
									currentResourceInSession);
					} else {
						// This means that the software has not been persisted
						// before. If the Resource is already in the session,
						// there is nothing to do, but if not, we need to
						// replace it with the sessioned one
						if (!getSession().contains(currentResource)) {
							softwareToPersist.getResources().remove(
									currentResource);
							softwareToPersist.getResources().add(
									currentResourceInSession);
						}
					}
				}
			}
		}

		// If it was not persisted in the past, save it
		if (!persistedBefore)
			getSession().save(softwareToPersist);

		// Return the ID
		if (softwareToPersist != null) {
			return softwareToPersist.getId();
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
		Software software = this.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		Software persistentSoftware = (Software) this
				.findEquivalentPersistentObject(software, false);

		// If no matching software was found, do nothing
		if (persistentSoftware == null) {
			logger
					.debug("No matching software could be found in the persistent store, "
							+ "no delete performed");
		} else {
			// Handle the relationships
			persistentSoftware.setPerson(null);
			persistentSoftware.clearResources();

			// Find all the DataProducers who use the software and clear the
			// relationship
			DataProducerDAO dpDAO = new DataProducerDAO(getSession());
			Collection associatedDPs = dpDAO.findBySoftware(persistentSoftware,
					null, null, true);
			if (associatedDPs != null) {
				Iterator associatedDPsIter = associatedDPs.iterator();
				while (associatedDPsIter.hasNext()) {
					DataProducer tempDP = (DataProducer) associatedDPsIter
							.next();
					tempDP.setSoftware(null);
				}
			}

			logger
					.debug("Existing object was found, so we will try to delete it");
			try {
				getSession().delete(persistentSoftware);
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
	 *            <code>Software</code>
	 * @return a <code>Software</code> that is same object that came in
	 * @throws MetadataAccessException
	 *             if something is wrong
	 */
	private Software checkIncomingMetadataObject(IMetadataObject metadataObject)
			throws MetadataAccessException {

		// Check for null argument
		if (metadataObject == null) {
			throw new MetadataAccessException(
					"Failed: incoming Software was null");
		}

		// Try to cast the incoming object into the correct class
		Software software = null;
		try {
			software = (Software) metadataObject;
		} catch (ClassCastException cce) {
			throw new MetadataAccessException(
					"Could not cast the incoming object into a Software");
		}
		return software;
	}

	private Criteria formulatePropertyCriteria(boolean countQuery, Long id,
			String name, boolean exactNameMatch, String uriString,
			boolean exactUriStringMatch, String softwareVersion,
			boolean exactsoftwareVersionMatch, String orderByProperty,
			String ascendOrDescend) throws MetadataAccessException {
		// The Criteria to return
		Criteria criteria = getSession().createCriteria(Software.class);
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
			if ((uriString != null) && (!uriString.equals(""))) {
				if (exactUriStringMatch) {
					criteria.add(Restrictions.eq("uriString", uriString));
				} else {
					criteria.add(Restrictions.like("uriString", "%" + uriString
							+ "%"));
				}
			}
			if ((softwareVersion != null) && (!softwareVersion.equals(""))) {
				if (exactsoftwareVersionMatch) {
					criteria.add(Restrictions.eq("softwareVersion",
							softwareVersion));
				} else {
					criteria.add(Restrictions.like("softwareVersion", "%"
							+ softwareVersion + "%"));
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
		if (metadataObject == null)
			return;
		Software software = (Software) this
				.checkIncomingMetadataObject(metadataObject);
		if (software.getPerson() != null)
			Hibernate.initialize(software.getPerson());
		if (software.getResources() != null)
			software.getResources().size();
	}

	/**
	 * The Log4J Logger
	 */
	static Logger logger = Logger.getLogger(SoftwareDAO.class);
}
