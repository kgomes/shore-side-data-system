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
package test.moos.ssds.services.metadata;

import java.util.Collection;
import java.util.HashMap;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;

/**
 * This class tests the PersonAccess service EJB to make sure all is well. There
 * has to be an SSDS server running somewhere for this to hit against and a
 * jndi.properties in the classpath so the tests can get to the server
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.10 $
 */
public class TestPersonAccess extends TestAccessCase {

	/**
	 * A constructor
	 * 
	 * @param name
	 */
	public TestPersonAccess(String name) {
		super(name);
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {

		// Setup the super class
		super.setUp();
	}

	/**
	 * Run suite of tests on person one
	 */
	public void testOne() {
		logger.debug("Person one is " + PERSON_ONE.toStringRepresentation("|"));
		this.personTest(PERSON_ONE);
		logger.debug("Done with test one");
	}

	/**
	 * Run suite of tests on person two
	 */
	public void testTwo() {
		logger.debug("Person two is " + PERSON_TWO.toStringRepresentation("|"));
		personTest(PERSON_TWO);
		logger.debug("Done with test two");
	}

	/**
	 * This method checks all the "findBy*" methods
	 */
	@SuppressWarnings("unchecked")
	public void testFindBys() {
		logger.debug("testFindBys starting");
		// We will insert both persons and then see if we can find them using
		// the find by methods
		Long personOneId = null;
		Long personTwoId = null;
		try {
			personOneId = PERSON_ACCESS.insert(PERSON_ONE);
			personTwoId = PERSON_ACCESS.insert(PERSON_TWO);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Make sure the returned ids are real
		assertNotNull(personOneId);
		assertNotNull(personTwoId);
		logger.debug("PersonOneId = " + personOneId);
		logger.debug("PersonTwoId = " + personTwoId);

		// OK both should be inserted. Excercise some of the findBys
		Person foundOne = null;
		try {
			foundOne = (Person) PERSON_ACCESS.findById(personOneId, false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The findById(Long) should have returned something",
				foundOne);
		assertEquals(
				"The foundOne (by Long) should be equal to the local personOne",
				foundOne, PERSON_ONE);

		// Clear and try with different find by
		foundOne = null;
		try {
			foundOne = (Person) PERSON_ACCESS.findById(personOneId.longValue(),
					false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The findById(long) should have returned something",
				foundOne);
		assertEquals(
				"The foundOne (by long) should be equal to the local personOne",
				foundOne, PERSON_ONE);

		// Clear and try again
		foundOne = null;
		try {
			foundOne = (Person) PERSON_ACCESS.findById(personOneId.toString(),
					false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The findById(String) should have returned something",
				foundOne);
		assertEquals(
				"The foundOne (by String) should be equal to the local personOne",
				foundOne, PERSON_ONE);

		// OK, find by local person and check against ID
		Long foundOneId = null;
		try {
			foundOneId = PERSON_ACCESS.findId(PERSON_ONE);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("foundOneId should not be null", foundOneId);
		assertEquals("ID from insert and returned ID should be the same.",
				foundOneId.longValue(), personOneId.longValue());

		// Now check find all
		Collection allPersons = null;
		try {
			allPersons = PERSON_ACCESS.findAll(null, null, false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("Collection returned by findAll should not be null",
				allPersons);
		assertTrue("Collection should contain local person one",
				allPersons.contains(PERSON_ONE));
		assertTrue("Collection should contain local person two",
				allPersons.contains(PERSON_TWO));
		logger.debug("allPersons = " + allPersons);

		// Now test the findEquivalent persistent object
		Person persistentOne = null;
		try {
			Long equivalentId = PERSON_ACCESS.findId(PERSON_ONE);
			persistentOne = (Person) PERSON_ACCESS
					.findById(equivalentId, false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull(
				"persistent object that matches PERSON_ONE should not be null",
				persistentOne);
		assertEquals(
				"Persistent and local objects for personOne should be equal",
				persistentOne, PERSON_ONE);
		assertEquals("The inserted ID and the persistent ID should be equal",
				personOneId.longValue(), persistentOne.getId().longValue());

		// Try the find by email
		Collection byEmail = null;
		try {
			byEmail = PERSON_ACCESS.findByEmail(PERSON_ONE.getEmail(), true,
					null, null, false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("Collection returned by email should not be null",
				byEmail);
		assertTrue("Collection by email should contain person one",
				byEmail.contains(PERSON_ONE));

		// Check out the find by username
		foundOne = null;
		try {
			foundOne = (Person) PERSON_ACCESS.findByUsername(
					PERSON_ONE.getUsername(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The findByUsername should have returned something",
				foundOne);
		assertEquals(
				"The foundOne (by username) should be equal to the local personOne",
				foundOne, PERSON_ONE);

		// Find all username should have both usernames from the two local
		// persons
		Collection usernames = null;
		try {
			usernames = PERSON_ACCESS.findAllUsernames();
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("Collection of usernames should not be null", usernames);
		assertTrue(
				"Collection of usernames should contain personOne username of "
						+ PERSON_ONE.getUsername(),
				usernames.contains(PERSON_ONE.getUsername()));
		assertTrue(
				"Collection of usernames should contain PERSON_TWO username of "
						+ PERSON_TWO.getUsername(),
				usernames.contains(PERSON_TWO.getUsername()));
		logger.debug("Usernames = " + usernames);
		logger.debug("testFindBys done");

	}

	/**
	 * This method checks the relationship to the UserGroup throughout the
	 * persistent actions
	 */
	public void testUserGroupRelationship() {
		// First We will insert both persons
		Long personOneId = null;
		Long personTwoId = null;
		try {
			personOneId = PERSON_ACCESS.insert(PERSON_ONE);
			personTwoId = PERSON_ACCESS.insert(PERSON_TWO);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Make sure the returned ids are real
		assertNotNull(personOneId);
		assertNotNull(personTwoId);
		logger.debug("PersonOneId = " + personOneId);
		logger.debug("PersonTwoId = " + personTwoId);

		// First query back to see that there are no UserGroups
		Person persistentPersonOne = null;
		try {
			persistentPersonOne = (Person) PERSON_ACCESS.findById(personOneId,
					true);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		// Now make sure the collection of user groups is there, but empty
		assertNotNull("persistentPersonOne should not be null",
				persistentPersonOne);
		assertNotNull("The userGroup collection should not be null",
				persistentPersonOne.getUserGroups());
		assertEquals("The number of usergroups should be zero", 0,
				persistentPersonOne.getUserGroups().size());

		// Now add one user group to the persistent person
		persistentPersonOne.addUserGroup(USER_GROUP_ONE);
		// Now persist it
		try {
			PERSON_ACCESS.update(persistentPersonOne);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Now search again by id
		persistentPersonOne = null;
		try {
			persistentPersonOne = (Person) PERSON_ACCESS.findById(personOneId,
					true);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		// Now make sure the collection of user groups is there
		assertNotNull("persistentPersonOne should not be null",
				persistentPersonOne);
		assertNotNull("The userGroup collection should not be null",
				persistentPersonOne.getUserGroups());
		assertEquals("The number of usergroups should be one", 1,
				persistentPersonOne.getUserGroups().size());

		// Now let's add a second user group
		persistentPersonOne.addUserGroup(USER_GROUP_TWO);
		try {
			PERSON_ACCESS.update(persistentPersonOne);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Now search again by id
		persistentPersonOne = null;
		try {
			persistentPersonOne = (Person) PERSON_ACCESS.findById(personOneId,
					true);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		// Now make sure the collection of user groups is there
		assertNotNull("persistentPersonOne should not be null",
				persistentPersonOne);
		assertNotNull("The userGroup collection should not be null",
				persistentPersonOne.getUserGroups());
		assertEquals("The number of usergroups should be two", 2,
				persistentPersonOne.getUserGroups().size());
		assertTrue("The userGroup colleciton should contain user group one",
				persistentPersonOne.getUserGroups().contains(USER_GROUP_ONE));
		assertTrue("The userGroup colleciton should contain user group two",
				persistentPersonOne.getUserGroups().contains(USER_GROUP_TWO));

		// Now add the third userGroup to the PERSON_ONE object which has no ID.
		// This should test if SSDS can keep existing associations in place
		PERSON_ONE.addUserGroup(USER_GROUP_THREE);
		// Make sure there is only one
		assertEquals("There should be only one user group on PERSON_ONE", 1,
				PERSON_ONE.getUserGroups().size());
		assertTrue("It should be USER_GROUP_THREE", PERSON_ONE.getUserGroups()
				.contains(USER_GROUP_THREE));

		// Now persist it
		Long updatedPersonOneID = null;
		try {
			updatedPersonOneID = PERSON_ACCESS.update(PERSON_ONE);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		// The IDs should be the same
		assertNotNull("The updated ID should not be null", updatedPersonOneID);
		assertEquals("The two IDs should be the same",
				updatedPersonOneID.longValue(), personOneId.longValue());

		// Now query back for the person
		// Now search again by id
		persistentPersonOne = null;
		try {
			persistentPersonOne = (Person) PERSON_ACCESS.findById(personOneId,
					true);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		// Now make sure the collection of user groups is there
		assertNotNull("persistentPersonOne should not be null",
				persistentPersonOne);
		assertNotNull("The userGroup collection should not be null",
				persistentPersonOne.getUserGroups());
		assertEquals("The number of usergroups should be three", 3,
				persistentPersonOne.getUserGroups().size());
		assertTrue("The userGroup colleciton should contain user group one",
				persistentPersonOne.getUserGroups().contains(USER_GROUP_ONE));
		assertTrue("The userGroup colleciton should contain user group two",
				persistentPersonOne.getUserGroups().contains(USER_GROUP_TWO));
		assertTrue("The userGroup colleciton should contain user group three",
				persistentPersonOne.getUserGroups().contains(USER_GROUP_THREE));

	}

	/**
	 * This is the suite of tests to run on a person
	 * 
	 * @param person
	 */
	private void personTest(Person person) {

		// The ID of the person
		Long personId = null;
		personId = testInsert(person, PERSON_ACCESS);

		// Now query back by ID and make sure all attributes are equal
		Person persistedPerson = null;

		try {
			persistedPerson = (Person) PERSON_ACCESS.findById(personId, false);
		} catch (MetadataAccessException e1) {
			logger.error("MetadataAccessException caught during findById: "
					+ e1.getMessage());
		}

		// Verify that the returned password is null (should not serialize)
		assertNull("The returned person's password should be null",
				persistedPerson.getPassword());

		// Now set the password so we can check equality
		try {
			persistedPerson.setPassword(person.getPassword());
		} catch (MetadataException e) {
			logger.error("MetadataException caught during findById: "
					+ e.getMessage());
		}

		// Now check that they are equal
		assertEquals("The two persons should be considered equal", person,
				persistedPerson);

		// Check all the getter methods are equal
		testEqualityOfAllGetters(person, persistedPerson);

		// Create a map with the values to update
		HashMap<String, Object[]> variablesToUpdate = new HashMap<String, Object[]>();

		// Change the surname
		Object[] variable1 = new Object[1];
		variable1[0] = new String("UpdatedSurname");
		variablesToUpdate.put("Surname", variable1);

		// Change the status
		Object[] variable2 = new Object[1];
		variable2[0] = new String("UpdatedOrganization");
		variablesToUpdate.put("Organization", variable2);

		testUpdate(persistedPerson, variablesToUpdate, PERSON_ACCESS);

		testDelete(persistedPerson, PERSON_ACCESS);
	}

	/**
	 * A log4J logger
	 */
	static Logger logger = Logger.getLogger(TestPersonAccess.class);
}