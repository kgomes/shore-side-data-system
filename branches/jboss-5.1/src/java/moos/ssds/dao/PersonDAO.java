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
import moos.ssds.metadata.Device;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.Software;
import moos.ssds.metadata.UserGroup;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * This is the Data Access Object that provides functionality to support the
 * persistence of <code>Person</code> objects
 * <hr>
 * 
 * @stereotype service
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.13 $
 */
public class PersonDAO extends MetadataDAO {

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
	public PersonDAO(Session session) throws MetadataAccessException {
		// Construct the parent DAO with the correct class and session
		super(Person.class, session);
	}

	/**
	 * @see IMetadataDAO#findEquivalentPersistentObject(IMetadataObject)
	 */
	public IMetadataObject findEquivalentPersistentObject(
			IMetadataObject metadataObject, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		logger.debug("findEquivalentPersistentObject called");
		// First try to cast to a Person
		Person person = this.checkIncomingMetadataObject(metadataObject);

		// The Person that will be returned
		Person personToReturn = null;

		// Now check the ID and if ther is one, search by ID
		if (person.getId() != null) {
			Criteria criteria = this.formulatePropertyCriteria(false, person
					.getId(), null, false, null, false, null, false, null,
					false, null, false, null, false, null, false, null, false,
					null, false, null, false, null, false, null, false, null,
					null);
			personToReturn = (Person) criteria.uniqueResult();
		}
		// If the person is still not found, try by username
		if ((personToReturn == null) && (person.getUsername() != null)) {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					null, false, null, false, null, false,
					person.getUsername(), true, null, false, null, false, null,
					false, null, false, null, false, null, false, null, false,
					null, false, null, null);
			personToReturn = (Person) criteria.uniqueResult();
		}

		// If the person is still null, try to find by email
		if ((personToReturn == null) && (person.getEmail() != null)) {
			Collection personsWithEmail = new ArrayList();
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					null, false, null, false, null, false, null, false, person
							.getEmail(), true, null, false, null, false, null,
					false, null, false, null, false, null, false, null, false,
					null, null);
			personsWithEmail = criteria.list();
			// Grab the first one
			if (personsWithEmail.size() > 0) {
				Iterator personWithEmailIterator = personsWithEmail.iterator();
				if (personWithEmailIterator.hasNext())
					personToReturn = (Person) personWithEmailIterator.next();
			}
		}

		// If the full object graph was requested, fill it out
		if (returnFullObjectGraph)
			personToReturn = (Person) getRealObjectAndRelationships(personToReturn);

		if (personToReturn != null) {
			logger.debug("OK, returning the person: "
					+ personToReturn.toStringRepresentation("|"));
		} else {
			logger.debug("No equivalent person was found");
		}
		// Return the equivalent person
		return personToReturn;
	}

	/**
	 * @see IMetadataDAO#findAllIDs()
	 */
	public Collection findAllIDs() throws MetadataAccessException {
		// The results to return
		Collection personIDs = null;

		// Create the query and run it
		try {
			Query query = getSession().createQuery(
					"select distinct person.id from "
							+ "Person person order by person.id");
			personIDs = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return personIDs;
	}

	/**
	 * @see IMetadataDAO#countFindAllIDs()
	 */
	public int countFindAllIDs() throws MetadataAccessException {
		// The count
		int count = 0;
		try {
			Long longCount = (Long) getSession().createQuery(
					"select count(distinct person.id) from Person person")
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
	 * This method tries to look up and instantiate a user by their email
	 * address.
	 * 
	 * @param email
	 *            is a <code>java.lang.String</code> that will be used to search
	 *            for matches of a person's email address
	 * @return a <code>Collection</code> of person objects that have that email
	 *         address. If no matches were found, an empty collection is
	 *         returned
	 * @throws MetadataAccessException
	 *             if something goes wrong with the search
	 */
	public Collection findByEmail(String email, boolean exactMatch,
			String orderByProperty, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// First check to see if the email is null
		logger.debug("findByEmail called with email = " + email);
		if ((email == null) || (email.equals(""))) {
			logger.debug("The incoming email address was not valid");
			return new ArrayList();
		}

		// The Collection to return
		Collection personsToReturn = new ArrayList();

		// Grab a session and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					null, false, null, false, null, false, null, false, email,
					exactMatch, null, false, null, false, null, false, null,
					false, null, false, null, false, null, false,
					orderByProperty, ascendingOrDescending);
			personsToReturn = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Check for relationship initialization
		if (returnFullObjectGraph)
			personsToReturn = getRealObjectsAndRelationships(personsToReturn);

		// Return the collection
		return personsToReturn;
	}

	/**
	 * This method tries to look up and instantiate a user by their username
	 * 
	 * @param username
	 *            is a <code>java.lang.String</code> that will be used to search
	 *            for matches of a person's username
	 * @return a <code>MetadataObject</code> of class <code>Person</code> that
	 *         has a username that matches the one specified. If no matches were
	 *         found, an empty collection is returned
	 * @throws MetadataAccessException
	 *             if something goes wrong with the search
	 */
	public Person findByUsername(String username, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// First check to see if the username is null
		logger.debug("findByUsername called with username = " + username);
		if ((username == null) || (username.equals(""))) {
			logger.debug("The supplied username, " + username
					+ ", is not valid");
			return null;
		}

		// The Person to return
		Person personToReturn = null;

		// Grab a session and run the query
		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					null, false, null, false, null, false, username, true,
					null, false, null, false, null, false, null, false, null,
					false, null, false, null, false, null, false, null, null);
			personToReturn = (Person) criteria.uniqueResult();
		} catch (HibernateException e) {
			logger.error("HibernateException caught (will be re-thrown):"
					+ e.getMessage());
			throw new MetadataAccessException(e);
		}

		// Check for relationship initialization
		if (returnFullObjectGraph)
			personToReturn = (Person) getRealObjectAndRelationships(personToReturn);

		// Return the person
		return personToReturn;
	}

	/**
	 * This method returns a collection of <code>java.lang.String</code>s that
	 * are all the usernames of people in the database
	 * 
	 * @return a <code>Collection</code> of <code>java.lang.String</code>s that
	 *         are all the usernames that are currently in the system. If there
	 *         are no usernames and empty collection is returned
	 */
	public Collection findAllUsernames() throws MetadataAccessException {

		logger.debug("findAllUsernames called");
		// Create the collection to return
		Collection usernames = new ArrayList();

		// Create the query and run it
		try {
			Query query = getSession().createQuery(
					"select distinct person.username from "
							+ "Person person order by person.username");
			usernames = query.list();
		} catch (HibernateException e) {
			logger.error("HibernateException caught (will be re-thrown):"
					+ e.getMessage());
			throw new MetadataAccessException(e);
		}

		// Now return them
		return usernames;
	}

	/**
	 * This method finds all <code>Person</code>s that are in a specified
	 * <code>UserGroup</code>
	 * 
	 * @param userGroup
	 * @param orderByPropertyName
	 * @param ascendingOrDescending
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByUserGroup(UserGroup userGroup,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// First check to see if the ID of the UserGroup is specified.
		// If not grab it from the persistent store
		Long userGroupID = userGroup.getId();
		if (userGroupID == null) {
			UserGroupDAO userGroupDAO = new UserGroupDAO(getSession());
			userGroupID = userGroupDAO.findId(userGroup);
		}

		if (userGroupID == null)
			throw new MetadataAccessException(
					"A matching userGroup could not be found in the system");

		// The collection to return
		Collection personsToReturn = new ArrayList();

		// Create the criteria
		try {
			Criteria criteria = getSession().createCriteria(Person.class);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			criteria.createAlias("userGroups", "ugs");
			criteria.add(Restrictions.eq("ugs.id", userGroupID));
			addOrderByCriteria(criteria, orderByPropertyName,
					ascendingOrDescending);
			personsToReturn = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}

		// If the full object graphs are requested
		if (returnFullObjectGraph)
			personsToReturn = getRealObjectsAndRelationships(personsToReturn);

		// Now return the results
		return personsToReturn;
	}

	/**
	 * @see IMetadataDAO#makePersistent(IMetadataObject)
	 */
	public Long makePersistent(IMetadataObject metadataObject)
			throws MetadataAccessException {

		// This is a boolean to indicate if the object has been persisted before
		boolean persistedBefore = false;

		// Check incoming object
		Person person = this.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		Person persistentPerson = (Person) this.findEquivalentPersistentObject(
				metadataObject, false);

		// Define the actual Person object that all further updates will happen
		// to
		Person personToPersist = null;

		// If there is an existing persistent person, copy over any changed
		// non-null fields from the incoming object to persist any real changes
		if (persistentPerson != null) {
			logger.debug("The search for existing object returned "
					+ persistentPerson.toStringRepresentation("|"));
			String personStringBefore = persistentPerson
					.toStringRepresentation("<li>");
			if (this.updateDestinationObject(person, persistentPerson)) {
				addMessage(ssdsAdminEmailToAddress,
						"A user account was updated in SSDS<br><b>Before:</b><br><ul><li>"
								+ personStringBefore
								+ "</ul><br><b>After:</b><br><ul><li>"
								+ persistentPerson
										.toStringRepresentation("<li>")
								+ "</ul><br>");
				if ((sendUserMessages) && (persistentPerson.getEmail() != null)) {
					addMessage(persistentPerson.getEmail(),
							"Your user account was updated in SSDS<br><b>Before:</b><br><ul><li>"
									+ personStringBefore
									+ "</ul><br><b>After:</b><br><ul><li>"
									+ persistentPerson
											.toStringRepresentation("<li>")
									+ "</ul><br>");
				}
			}

			// Set the flag to indicate that this person has been persisted in
			// the past
			persistedBefore = true;

			// Now set the object to persist to the one that exists already
			personToPersist = persistentPerson;
		} else {
			logger.debug("No persistent person was found to match");
			// Since this will be a newly stored person, check to see if the
			// person's username is specified, if not, try to create one from
			// the first part of the email. If that doesn't work, use a
			// combination of their first and surnames and as a last resort, use
			// "guest"
			if ((person.getUsername() == null)
					|| (person.getUsername().equals(""))) {
				String email = person.getEmail();
				if ((email != null) && (!email.equals(""))) {
					try {
						person.setUsername(email.substring(0, email
								.indexOf('@')));
					} catch (MetadataException e) {
					}
				}
				// If it is still empty, try others
				if ((person.getUsername() == null)
						|| (person.getUsername().equals(""))) {
					String firstname = person.getFirstname();
					String surname = person.getSurname();
					if (((firstname == null) || (firstname.equals("")))
							&& ((surname == null) || (surname.equals("")))) {
						try {
							person.setUsername("guest");
						} catch (MetadataException e) {
						}
					} else {
						try {
							person.setUsername(firstname + surname);
						} catch (MetadataException e) {
						}
					}
				}
				addMessage(ssdsAdminEmailToAddress,
						"A Person that was added to SSDS had no username, "
								+ "so one was auto-generated:<br><ul><li>"
								+ person.toStringRepresentation("<li>")
								+ "</ul><br>");
			}
			// Set the flag to indicate that this person has not been persisted
			// in the past
			persistedBefore = false;

			// Set the Person to persist to the new one
			personToPersist = person;
		}

		// ---------------------------------
		// Handle the UserGroup relationship
		// ---------------------------------

		// First make sure the usergroup relationship exists
		if (person.getUserGroups() != null) {

			// Now check to make sure it is initialized. If it is not, no
			// changes have taken place and we don't need to do anything
			if (Hibernate.isInitialized(person.getUserGroups())) {
				logger.debug("UserGroups appear to be initialized");

				// Grab the DAO for UserGroup
				UserGroupDAO userGroupDAO = new UserGroupDAO(this.getSession());

				// Make sure the are user groups to iterate over
				if (person.getUserGroups().size() > 0) {

					// Now iterate over the UserGroups and persist them
					Iterator userGroupIter = person.getUserGroups().iterator();
					while (userGroupIter.hasNext()) {
						UserGroup tempUserGroup = (UserGroup) userGroupIter
								.next();
						if (tempUserGroup != null)
							logger
									.debug("Will try to persist UserGroup: "
											+ tempUserGroup
													.toStringRepresentation("|"));
						userGroupDAO.makePersistent(tempUserGroup);
					}

					// Create a copy of the collection associated with the
					// person to
					// prevent concurrent modifications
					Collection personUserGroupCopy = new ArrayList(person
							.getUserGroups());

					// Now we need to make the correct associations. Currently,
					// you
					// have a collection of UserGroup objects that have their
					// values
					// marked for persistence. Now the object will either be in
					// the
					// session or not depending on if they were previously
					// persisted.
					Iterator personUserGroupCopyIterator = personUserGroupCopy
							.iterator();
					while (personUserGroupCopyIterator.hasNext()) {
						UserGroup currentUserGroup = (UserGroup) personUserGroupCopyIterator
								.next();
						UserGroup currentUserGroupInSession = null;
						// Is this UserGroup already in the session?
						if (!getSession().contains(currentUserGroup)) {
							// No, so grab the one that is
							currentUserGroupInSession = (UserGroup) userGroupDAO
									.findEquivalentPersistentObject(
											currentUserGroup, false);
						} else {
							currentUserGroupInSession = currentUserGroup;
						}
						// Now if the parent person was persisted before, just
						// check to make sure the sessioned UserGroup is in the
						// collection associated with the person that will be
						// persisted
						if (persistedBefore) {
							if (!personToPersist.getUserGroups().contains(
									currentUserGroupInSession))
								personToPersist.getUserGroups().add(
										currentUserGroupInSession);
						} else {
							// This means that the person has not been persisted
							// before. If the UserGroup is already in the
							// session,
							// there is nothing to do, but if not, we need to
							// replace it with the sessioned one
							if (!getSession().contains(currentUserGroup)) {
								personToPersist.getUserGroups().remove(
										currentUserGroup);
								personToPersist.getUserGroups().add(
										currentUserGroupInSession);
							}
						}
					}
				}
			} else {
				logger.debug("UserGroups do not appear to be "
						+ "initialized, so will be ignored");
			}
		}

		// Now, if the person has not been persisted before, we need to call
		// save to pull it into the session
		if (!persistedBefore) {
			// The new person must be saved
			getSession().save(personToPersist);
			if ((sendUserMessages) && (personToPersist.getEmail() != null))
				addMessage(
						personToPersist.getEmail(),
						"Your email address ("
								+ personToPersist.getEmail()
								+ ") has been registered with the Shore Side Data System");
		}

		// Now return the ID
		if (personToPersist != null) {
			return personToPersist.getId();
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
		Person person = this.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		Person persistentPerson = (Person) this.findEquivalentPersistentObject(
				person, false);

		// If no matching person was found, throw an exception
		if (persistentPerson == null) {
			logger
					.debug("No matching person could be found in the persistent store, "
							+ "no delete performed");
		} else {
			logger.debug("The search for existing object returned "
					+ persistentPerson.toStringRepresentation("|"));
			// If we here, we need to make sure all the various relationships
			// are cleared before removing the person. The relationships are:
			// 1. Person has UserGroups
			// 2. DataContainer has Person
			// 3. DataProducer has Person
			// 4. Software has Person
			// 5. Device has Person
			// 6. Resource has Person

			// UserGroups first
			persistentPerson.clearUserGroups();

			// DataContainers
			Collection dataContainersByPerson = null;
			DataContainerDAO dataContainerDAO = new DataContainerDAO(this
					.getSession());
			dataContainersByPerson = dataContainerDAO.findByPerson(
					persistentPerson, null, null, true);
			if (dataContainersByPerson != null) {
				logger.debug("Going to try to clear "
						+ dataContainersByPerson.size()
						+ " dataContainers of a person");
				Iterator iter = dataContainersByPerson.iterator();
				while (iter.hasNext()) {
					DataContainer dataContainer = (DataContainer) iter.next();
					dataContainer.setPerson(null);
				}
			}

			// DataProducers
			Collection dataProducersByPerson = null;
			DataProducerDAO dataProducerDAO = new DataProducerDAO(this
					.getSession());
			dataProducersByPerson = dataProducerDAO.findByPerson(
					persistentPerson, null, null, true);
			if (dataProducersByPerson != null) {
				logger.debug("Going to try to clear "
						+ dataProducersByPerson.size()
						+ " dataProducers of a person");
				Iterator iter = dataProducersByPerson.iterator();
				while (iter.hasNext()) {
					DataProducer dataProducer = (DataProducer) iter.next();
					dataProducer.setPerson(null);
				}
			}

			// Software
			Collection softwareByPerson = null;
			SoftwareDAO softwareDAO = new SoftwareDAO(this.getSession());
			softwareByPerson = softwareDAO.findByPerson(persistentPerson);
			if (softwareByPerson != null) {
				logger.debug("Going to try to clear " + softwareByPerson.size()
						+ " softwares of a person");
				Iterator iter = softwareByPerson.iterator();
				while (iter.hasNext()) {
					Software software = (Software) iter.next();
					software.setPerson(null);
				}
			}

			// Devices
			Collection devicesByPerson = null;
			DeviceDAO deviceDAO = new DeviceDAO(this.getSession());
			devicesByPerson = deviceDAO.findByPerson(persistentPerson, null,
					null, true);
			if (devicesByPerson != null) {
				logger.debug("Going to try to clear " + devicesByPerson.size()
						+ " devices of a person");
				Iterator iter = devicesByPerson.iterator();
				while (iter.hasNext()) {
					Device device = (Device) iter.next();
					device.setPerson(null);
				}
			}

			// Resources
			Collection resourcesByPerson = null;
			ResourceDAO resourceDAO = new ResourceDAO(this.getSession());
			resourcesByPerson = resourceDAO.findByPerson(persistentPerson);
			if (resourcesByPerson != null) {
				logger.debug("Going to try to clear "
						+ resourcesByPerson.size() + " resources of a person");
				Iterator iter = resourcesByPerson.iterator();
				while (iter.hasNext()) {
					Resource resource = (Resource) iter.next();
					resource.setPerson(null);
				}
			}

			logger
					.debug("Existing object was found, so we will try to delete it");
			try {
				getSession().delete(persistentPerson);
				if ((sendUserMessages) && (persistentPerson.getEmail() != null)) {
					addMessage(
							persistentPerson.getEmail(),
							"The user account associated with the email "
									+ persistentPerson.getEmail()
									+ ", was removed from the Shore-Side Data System");
				}
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
	 *            <code>Person</code>
	 * @return a <code>Person</code> that is same object that came in
	 * @throws MetadataAccessException
	 *             if something is wrong
	 */
	private Person checkIncomingMetadataObject(IMetadataObject metadataObject)
			throws MetadataAccessException {

		// Check for null argument
		if (metadataObject == null) {
			throw new MetadataAccessException(
					"Failed: incoming Person was null");
		}

		// Try to cast the incoming object into the correct class
		Person person = null;
		try {
			person = (Person) metadataObject;
		} catch (ClassCastException cce) {
			throw new MetadataAccessException(
					"Could not cast the incoming object into a Person");
		}
		return person;
	}

	private Criteria formulatePropertyCriteria(boolean countQuery, Long id,
			String firstname, boolean exactFirstnameMatch, String surname,
			boolean exactSurnameMatch, String organization,
			boolean exactOrganizationMatch, String username,
			boolean exactUsernameMatch, String email, boolean exactEmailMatch,
			String phone, boolean exactPhoneMatch, String address1,
			boolean exactAddress1Match, String address2,
			boolean exactAddress2Match, String city, boolean exactCityMatch,
			String state, boolean exactStateMatch, String zipcode,
			boolean exactZipcodeMatch, String status, boolean exactStatusMatch,
			String orderByProperty, String ascendingOrDescending)
			throws MetadataAccessException {
		// The Criteria to return
		Criteria criteria = getSession().createCriteria(Person.class);
		// Make the return distinct
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		if (id != null) {
			criteria.add(Restrictions.eq("id", id));
		} else {
			// Firstname
			if ((firstname != null) && (!firstname.equals(""))) {
				if (exactFirstnameMatch) {
					criteria.add(Restrictions.eq("firstname", firstname));
				} else {
					criteria.add(Restrictions.like("firstname", "%" + firstname
							+ "%"));
				}
			}
			// Surname
			if ((surname != null) && (!surname.equals(""))) {
				if (exactSurnameMatch) {
					criteria.add(Restrictions.eq("surname", surname));
				} else {
					criteria.add(Restrictions.like("surname", "%" + surname
							+ "%"));
				}
			}
			// Organization
			if ((organization != null) && (!organization.equals(""))) {
				if (exactOrganizationMatch) {
					criteria.add(Restrictions.eq("organization", organization));
				} else {
					criteria.add(Restrictions.like("organization", "%"
							+ organization + "%"));
				}
			}
			// Username
			if ((username != null) && (!username.equals(""))) {
				if (exactUsernameMatch) {
					criteria.add(Restrictions.eq("username", username));
				} else {
					criteria.add(Restrictions.like("username", "%" + username
							+ "%"));
				}
			}
			// Email
			if ((email != null) && (!email.equals(""))) {
				if (exactEmailMatch) {
					criteria.add(Restrictions.eq("email", email));
				} else {
					criteria.add(Restrictions.like("email", "%" + email + "%"));
				}
			}
			// Phone
			if ((phone != null) && (!phone.equals(""))) {
				if (exactPhoneMatch) {
					criteria.add(Restrictions.eq("phone", phone));
				} else {
					criteria.add(Restrictions.like("phone", "%" + phone + "%"));
				}
			}
			// Address1
			if ((address1 != null) && (!address1.equals(""))) {
				if (exactAddress1Match) {
					criteria.add(Restrictions.eq("address1", address1));
				} else {
					criteria.add(Restrictions.like("address1", "%" + address1
							+ "%"));
				}
			}
			// Address2
			if ((address2 != null) && (!address2.equals(""))) {
				if (exactAddress2Match) {
					criteria.add(Restrictions.eq("address2", address2));
				} else {
					criteria.add(Restrictions.like("address2", "%" + address2
							+ "%"));
				}
			}
			// City
			if ((city != null) && (!city.equals(""))) {
				if (exactCityMatch) {
					criteria.add(Restrictions.eq("city", city));
				} else {
					criteria.add(Restrictions.like("city", "%" + city + "%"));
				}
			}
			// State
			if ((state != null) && (!state.equals(""))) {
				if (exactStateMatch) {
					criteria.add(Restrictions.eq("state", state));
				} else {
					criteria.add(Restrictions.like("state", "%" + state + "%"));
				}
			}
			// Zipcode
			if ((zipcode != null) && (!zipcode.equals(""))) {
				if (exactZipcodeMatch) {
					criteria.add(Restrictions.eq("zipcode", zipcode));
				} else {
					criteria.add(Restrictions.like("zipcode", "%" + zipcode
							+ "%"));
				}
			}
			// Status
			if ((status != null) && (!status.equals(""))) {
				if (exactStatusMatch) {
					criteria.add(Restrictions.eq("status", status));
				} else {
					criteria.add(Restrictions
							.like("status", "%" + status + "%"));
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

	/**
	 * @see MetadataDAO#initializeRelationships(IMetadataObject)
	 */
	// protected void initializeRelationships(IMetadataObject metadataObject)
	// throws MetadataAccessException {
	// // For persons we need to initialize the UserGroup relationship
	// Person person = this.checkIncomingMetadataObject(metadataObject);
	// logger.debug("person is " + person.toStringRepresentation("|"));
	// if (person.getUserGroups() != null) {
	// logger.debug("Person has a user group collection (size = "
	// + person.getUserGroups().size() + ")");
	// if (person.getUserGroups().size() > 0) {
	// Iterator userGroupIter = person.getUserGroups().iterator();
	// while (userGroupIter.hasNext())
	// Hibernate.initialize((UserGroup) userGroupIter.next());
	// } else {
	// logger.debug("No entries in the collection however");
	// }
	// }
	// }

	/**
	 * The Log4J Logger
	 */
	static Logger logger = Logger.getLogger(PersonDAO.class);
}
