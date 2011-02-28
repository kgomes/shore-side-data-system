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
import moos.ssds.metadata.Person;
import moos.ssds.metadata.UserGroup;
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
 * persitence mechanism that handles the persistence of <code>UserGroup</code>
 * objects. It also provides query methods.
 * <hr>
 * 
 * @stereotype service
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.6 $
 */
public class UserGroupDAO extends MetadataDAO {

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
	public UserGroupDAO(Session session) throws MetadataAccessException {
		super(UserGroup.class, session);
	}

	/**
	 * @see IMetadataDAO#findEquivalentPersistentObject(IMetadataObject)
	 */
	public IMetadataObject findEquivalentPersistentObject(
			IMetadataObject metadataObject, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		logger.debug("findEquivalentPersistentObject called");

		// First make sure the incoming object is legit
		UserGroup userGroup = this.checkIncomingMetadataObject(metadataObject);

		// The userGroup to return
		UserGroup userGroupToReturn = null;

		// First check for the ID and if there is one, search by id
		Long idToSearchFor = userGroup.getId();
		if ((idToSearchFor != null) && (idToSearchFor.longValue() > 0)) {
			Criteria criteria = this.formulatePropertyCriteria(false,
					idToSearchFor, null, false, null, null);
			userGroupToReturn = (UserGroup) criteria.uniqueResult();
		}

		// If the UserGroup was not found, search by groupName
		if (userGroupToReturn == null) {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					userGroup.getGroupName(), true, null, null);
			userGroupToReturn = (UserGroup) criteria.uniqueResult();
		}

		// Is a full object graph was requested, fill it out
		if (returnFullObjectGraph)
			userGroupToReturn = (UserGroup) getRealObjectAndRelationships(userGroupToReturn);

		// Return the result of the search
		return userGroupToReturn;
	}

	/**
	 * @see IMetadataDAO#findAllIDs()
	 */
	public Collection findAllIDs() throws MetadataAccessException {
		Collection userGroupIDs = null;

		// Create the query and run it
		try {
			Query query = getSession().createQuery(
					"select distinct userGroup.id from "
							+ "UserGroup userGroup order by userGroup.id");
			userGroupIDs = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return userGroupIDs;
	}

	/**
	 * @see IMetadataDAO#countFindAllIDs()
	 */
	public int countFindAllIDs() throws MetadataAccessException {
		// The count
		int count = 0;
		try {
			Long longCount = (Long) getSession().createQuery(
					"select count(distinct userGroup.id) from "
							+ "UserGroup userGroup").uniqueResult();
			if (longCount != null)
				count = longCount.intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the result
		return count;
	}

	/**
	 * This method will return all the <code>UserGroup</code>s that match the
	 * supplied groupName. If you specify <code>true</code> for the exactMatch
	 * parameter, only those <code>UserGroup</code>s that exactly match the
	 * given word will be returned. Otherwise, it will do a &quot;like&quot;
	 * lookup.
	 * 
	 * @param groupName
	 *            is the groupName to search for
	 * @param exactMatch
	 *            is whether to look for an exact match (<code>true</code>) or
	 *            not
	 * @return a <code>Collection</code> of <code>UserGroup</code>s that match
	 *         the given groupName. It returns an empty collection if non were
	 *         found
	 * @throws MetadataAccessException
	 */
	@SuppressWarnings("unchecked")
	public Collection<UserGroup> findByGroupName(String groupName,
			boolean exactMatch, String orderByProperty,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// Make sure argument is not null
		logger.debug("findByName where groupName = " + groupName + " called.");
		if ((groupName == null) || (groupName.equals(""))) {
			return new ArrayList<UserGroup>();
		}

		// The collection to be returned
		Collection<UserGroup> results = new ArrayList<UserGroup>();

		// Formulate the criteria using the groupName
		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					groupName, exactMatch, orderByProperty,
					ascendingOrDescending);
			results = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// If the full object graph is requested, return it
		if (returnFullObjectGraph)
			results = (Collection<UserGroup>) getRealObjectsAndRelationships(results);

		// Return the results
		return results;
	}

	/**
	 * This method will return all the number of <code>UserGroup</code>s that
	 * match the supplied groupName criteria. If you specify <code>true</code>
	 * for the exactMatch parameter, only those <code>UserGroup</code>s that
	 * exactly match the given word will be returned. Otherwise, it will do a
	 * &quot;like&quot; lookup.
	 * 
	 * @param groupName
	 *            is the groupName to search for
	 * @param exactMatch
	 *            is whether to look for an exact match (<code>true</code>) or
	 *            not
	 * @return an integer count of the number of <code>UserGroup</code>s that
	 *         match the query
	 * @throws MetadataAccessException
	 */
	public int countFindByGroupName(String groupName, boolean exactMatch)
			throws MetadataAccessException {

		// The count to return
		int count = 0;

		// Make sure argument is not null
		logger.debug("countFindByGroupName where groupName = " + groupName
				+ " called.");
		if ((groupName == null) && (groupName.equals(""))) {
			return 0;
		}

		// Formulate the criteria using the groupName
		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					groupName, exactMatch, null, null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the result
		return count;
	}

	/**
	 * This method returns a <code>Collection</code> of <code>String</code>s
	 * that are the groupNames of all the <code>UserGroup</code>s that are
	 * registered in SSDS.
	 * 
	 * @return a <code>Collection</code> of <code>String</code>s that are the
	 *         groupNames of all <code>UserGroup</code>s in SSDS.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	@SuppressWarnings("unchecked")
	public Collection<String> findAllGroupNames()
			throws MetadataAccessException {
		// The collection to return
		Collection<String> userGroupNames = null;

		// Create the query and run it
		try {
			Query query = getSession()
					.createQuery(
							"select distinct userGroup.groupName from "
									+ "UserGroup userGroup order by userGroup.groupName");
			userGroupNames = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return userGroupNames;
	}

	/**
	 * This method returns a count of all the group names available
	 * 
	 * @return a integer count of all group names available
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public int countFindAllGroupNames() throws MetadataAccessException {
		// The count to return
		int count = 0;

		// Create the query and run it
		try {
			Long integerCount = (Long) getSession().createQuery(
					"select count(distinct userGroup.groupName) from "
							+ "UserGroup userGroup").uniqueResult();
			if (integerCount != null)
				count = integerCount.intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the count
		return count;
	}

	/**
	 * @see IMetadataDAO#makePersistent(IMetadataObject)
	 */
	public Long makePersistent(IMetadataObject metadataObject)
			throws MetadataAccessException {
		logger.debug("makePersistent called");

		// This is a boolean to indicate if the object has been persisted before
		boolean persistedBefore = false;

		// Check incoming object
		UserGroup userGroup = this.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		UserGroup persistentUserGroup = (UserGroup) this
				.findEquivalentPersistentObject(metadataObject, false);

		// This is the actual usergroup that subsequent changes will be made to
		UserGroup userGroupToPersist = null;

		// If there is an already existing object in the persistent store, copy
		// over any non-null fields that are different from the ones on the
		// persistent object
		if (persistentUserGroup != null) {
			logger.debug("The matching UserGroup is "
					+ persistentUserGroup.toStringRepresentation("|"));
			this.updateDestinationObject(userGroup, persistentUserGroup);
			// Set the flag that this object has been persisted before
			persistedBefore = true;

			// Assign the UserGroup to be persisted to the one that is already
			// persisted
			userGroupToPersist = persistentUserGroup;
		} else {
			logger.debug("The search for existing UserGroup returned nothing");
			// To ensure that the peristence happens, we need to ensure that the
			// alternate primary key is filled out. So, if the grpupName is null
			// (which it should not be), make one up
			if ((userGroup.getGroupName() == null)
					|| (userGroup.getGroupName().equals(""))) {
				try {
					userGroup
							.setGroupName("UserGroup_" + getUniqueNameSuffix());
					logger.error("Had to auto-generate the UserGroup "
							+ "groupName, this should not happen! ("
							+ userGroup.getGroupName() + ")");
					addMessage(
							ssdsAdminEmailToAddress,
							"An incoming UserGroup did not have a group name, "
									+ "so SSDS had to auto-generate one:<br><ul><li>"
									+ userGroup.toStringRepresentation("<li>")
									+ "</ul>");
				} catch (MetadataException e) {
					logger.error("Could not auto-generate the UserGroup name: "
							+ e.getMessage());
				}
			}
			// Set the flag to indicate this has not been persisted before (new)
			persistedBefore = false;

			// Set the UserGroup to persist to the new one
			userGroupToPersist = userGroup;
		}

		// If the object was not persisted in the past, call save to bring it
		// into the session and give it an ID. Otherwise, do nothing as the
		// session will take care of changes upon flushing
		if (!persistedBefore) {
			// It is not persisted, so save the incoming one
			if (userGroupToPersist != null)
				getSession().save(userGroupToPersist);
			addMessage(ssdsAdminEmailToAddress,
					"A new UserGroup has been added to SSDS<br><ul><li>"
							+ userGroupToPersist.toStringRepresentation("<li>")
							+ "</ul>");
		}

		// Grab the ID from the persisted object and return it
		if (userGroupToPersist != null) {
			return userGroupToPersist.getId();
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
		UserGroup userGroup = this.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		UserGroup persistentUserGroup = (UserGroup) this
				.findEquivalentPersistentObject(userGroup, false);
		logger.debug("The search for existing object returned "
				+ persistentUserGroup);

		// If no matching userGroup was found, throw an exception
		if (persistentUserGroup == null) {
			logger.debug("No matching userGroup could be found in the persistent store, "
					+ "no delete performed");
		} else {
			// If we are here, we need to make sure all the connections to any
			// Persons are removed before removing the UserGroup

			// Grab the PersonDAO
			PersonDAO personDAO = new PersonDAO(getSession());

			// Grab all the associated Persons
			Collection associatedPersons = null;
			try {
				associatedPersons = personDAO.findByUserGroup(
						persistentUserGroup, null, null, true);
			} catch (MetadataAccessException e1) {
				logger.error("MetadataAccessException caught trying to break connections "
						+ "between the Persons and the UserGroups: "
						+ e1.getMessage());
			}

			// If they are there, loop through and break the connections
			if (associatedPersons != null) {
				Iterator associatedPersonsIter = associatedPersons.iterator();
				while (associatedPersonsIter.hasNext()) {
					Person tempPerson = (Person) associatedPersonsIter.next();
					// Remove the group
					tempPerson.removeUserGroup(persistentUserGroup);
				}
			}

			logger.debug("Existing object was found, so we will try to delete it");
			try {
				getSession().delete(persistentUserGroup);
				addMessage(
						ssdsAdminEmailToAddress,
						"A UserGroup was deleted from SSDS<br><ul><li>"
								+ persistentUserGroup
										.toStringRepresentation("<li>")
								+ "</ul>");
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
	 *            <code>UserGroup</code>
	 * @return a <code>UserGroup</code> that is same object that came in
	 * @throws MetadataAccessException
	 *             if something is wrong
	 */
	private UserGroup checkIncomingMetadataObject(IMetadataObject metadataObject)
			throws MetadataAccessException {

		// Check for null argument
		if (metadataObject == null) {
			throw new MetadataAccessException(
					"Failed: incoming UserGroup was null");
		}

		// Try to cast the incoming object into the correct class
		UserGroup userGroup = null;
		try {
			userGroup = (UserGroup) metadataObject;
		} catch (ClassCastException cce) {
			throw new MetadataAccessException(
					"Could not cast the incoming object into a UserGroup");
		}
		return userGroup;
	}

	private Criteria formulatePropertyCriteria(boolean countQuery, Long id,
			String groupName, boolean exactGroupNameMatch,
			String orderByProperty, String ascendingOrDescending)
			throws MetadataAccessException {
		// The Criteria to return
		Criteria criteria = getSession().createCriteria(UserGroup.class);
		// Make the return distinct
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		if (id != null) {
			criteria.add(Restrictions.eq("id", id));
		} else {
			if ((groupName != null) && (!groupName.equals(""))) {
				if (exactGroupNameMatch) {
					criteria.add(Restrictions.eq("groupName", groupName));
				} else {
					criteria.add(Restrictions.like("groupName", "%" + groupName
							+ "%"));
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
	// // There is nothing to do for UserGroup relationships
	// }

	/**
	 * The Log4J Logger
	 */
	static Logger logger = Logger.getLogger(UserGroupDAO.class);
}
