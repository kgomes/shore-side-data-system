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

import java.rmi.RemoteException;
import java.util.Collection;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.UserGroup;

import org.apache.log4j.Logger;

/**
 * This class tests the UserGroupAccess service EJB to make sure all is well.
 * There has to be an SSDS server running somewhere for this to hit against and
 * a jndi.properties in the classpath so the tests can get to the server
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.1 $
 */
public class TestUserGroupAccess extends TestAccessCase {

	/**
	 * A constructor
	 * 
	 * @param name
	 */
	public TestUserGroupAccess(String name) {
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
	 * Run suite of tests on userGroup one
	 */
	public void testOne() {
		logger.debug("UserGroup one is "
				+ USER_GROUP_ONE.toStringRepresentation("|"));
		this.userGroupTest(USER_GROUP_ONE);
		logger.debug("Done with test one");
	}

	/**
	 * Run suite of tests on userGroup two
	 */
	public void testTypeTwo() {
		logger.debug("UserGroup two is "
				+ USER_GROUP_TWO.toStringRepresentation("|"));
		userGroupTest(USER_GROUP_TWO);
		logger.debug("Done with test two");
	}

	/**
	 * This is the suite of tests to run on a userGroup
	 * 
	 * @param device
	 */
	private void userGroupTest(UserGroup userGroup) {

		// The ID of the userGroup
		Long userGroupId = null;
		userGroupId = testInsert(userGroup, USER_GROUP_ACCESS);

		// Now query back by ID and make sure all attributes are equal
		UserGroup persistedUserGroup = null;

		try {
			persistedUserGroup = (UserGroup) USER_GROUP_ACCESS.findById(
					userGroupId, false);
		} catch (MetadataAccessException e1) {
			logger.error("MetadataAccessException caught during findById: "
					+ e1.getMessage());
		}

		// Now check that they are equal
		assertEquals("The two userGroups should be considered equal",
				userGroup, persistedUserGroup);

		// Check all the getter methods are equal
		testEqualityOfAllGetters(userGroup, persistedUserGroup);

		// Now test removal
		testDelete(persistedUserGroup, USER_GROUP_ACCESS);
	}

	/**
	 * This test checks that all the find by methods work correctly
	 */
	public void testFindBys() {
		// OK, let's fist let's null out all ID's
		USER_GROUP_ONE.setId(null);
		USER_GROUP_TWO.setId(null);

		// Grab the count of all before insert
		int countBeforeInserts = 0;
		try {
			countBeforeInserts = USER_GROUP_ACCESS.countFindAllIDs();
		} catch (MetadataAccessException e3) {
			assertTrue("MetadataAccessException caught : " + e3.getMessage(),
					false);
		}
		// OK now insert both userGroups
		Long userGroupOneId = null;
		Long userGroupTwoId = null;
		try {
			userGroupOneId = USER_GROUP_ACCESS.insert(USER_GROUP_ONE);
			userGroupTwoId = USER_GROUP_ACCESS.insert(USER_GROUP_TWO);
		} catch (MetadataAccessException e) {
			logger.error("MetadataAccessException caught inserting userGroups in find by test: "
					+ e.getMessage());
			assertTrue(
					"MetadataAccessException caught inserting userGroups in find by test: "
							+ e.getMessage(), false);
		}
		logger.debug("UserGroup one's ID is " + userGroupOneId);
		logger.debug("UserGroup two's ID is " + userGroupTwoId);

		// OK, now let's do the find by id's
		UserGroup persistedUserGroupOne = null;
		try {
			persistedUserGroupOne = (UserGroup) USER_GROUP_ACCESS.findById(
					userGroupOneId, false);
		} catch (MetadataAccessException e) {
			assertTrue(
					"MetadataAccessException caught trying to findById(Long):"
							+ e.getMessage(), false);
		}

		// Make sure they are equal
		assertEquals("The two userGroup one's should be equal", USER_GROUP_ONE,
				persistedUserGroupOne);

		// Now by little long
		persistedUserGroupOne = null;
		try {
			persistedUserGroupOne = (UserGroup) USER_GROUP_ACCESS.findById(
					userGroupOneId.longValue(), false);
		} catch (MetadataAccessException e) {
			assertTrue(
					"MetadataAccessException caught trying to findById(long):"
							+ e.getMessage(), false);
		}
		// Make sure they are equal
		assertEquals("The two userGroup one's should be equal", USER_GROUP_ONE,
				persistedUserGroupOne);

		// Now find by string
		persistedUserGroupOne = null;
		try {
			persistedUserGroupOne = (UserGroup) USER_GROUP_ACCESS.findById(
					userGroupOneId.toString(), false);
		} catch (MetadataAccessException e) {
			assertTrue(
					"MetadataAccessException caught trying to findById(String):"
							+ e.getMessage(), false);
		}
		// Make sure they are equal
		assertEquals("The two userGroup one's should be equal", USER_GROUP_ONE,
				persistedUserGroupOne);

		// Now try the find ID method
		Long idByUserGroupFind = null;
		try {
			idByUserGroupFind = USER_GROUP_ACCESS.findId(USER_GROUP_ONE);
		} catch (MetadataAccessException e) {
			assertTrue(
					"MetadataAccessException caught trying to findId(UserGroup):"
							+ e.getMessage(), false);
		}
		assertEquals("UserGroup ids should be equal after findId(UserGroup)",
				userGroupOneId, idByUserGroupFind);

		// Check the find equivalent persistent object
		UserGroup equivalentUserGroupOne = null;
		try {
			equivalentUserGroupOne = (UserGroup) USER_GROUP_ACCESS
					.findEquivalentPersistentObject(USER_GROUP_ONE, false);
		} catch (MetadataAccessException e) {
			assertTrue(
					"MetadataAccessException caught trying to findEquivalentPersistentObject(UserGroup):"
							+ e.getMessage(), false);
		}
		assertEquals("Id of the equivalent persistent object"
				+ " should match that of insert", userGroupOneId,
				equivalentUserGroupOne.getId());
		assertEquals("UserGroup after findEquivalentObject should be equal:",
				USER_GROUP_ONE, equivalentUserGroupOne);

		// Now make sure all the userGroups are returned in the findAll
		// method
		Collection allUserGroups = null;
		try {
			allUserGroups = USER_GROUP_ACCESS.findAll(null, null, false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataAccessException caught trying to findAll():"
					+ e.getMessage(), false);
		}
		assertTrue("findAll should have USER_GROUP_ONE",
				allUserGroups.contains(USER_GROUP_ONE));
		assertTrue("findAll should have USER_GROUP_TWO",
				allUserGroups.contains(USER_GROUP_TWO));

		// Now test the find by exact group name
		Collection findByGroupNameUserGroup = null;
		try {
			findByGroupNameUserGroup = USER_GROUP_ACCESS.findByGroupName(
					"JUnit UserGroup", true, null, null, false);
		} catch (MetadataAccessException e) {
			assertTrue(
					"MetadataAccessException caught trying to findByGroupName():"
							+ e.getMessage(), false);
		}
		assertEquals("findByGroupName(UserGroup) should be an empty return",
				findByGroupNameUserGroup.size(), 0);
		findByGroupNameUserGroup = null;
		try {
			findByGroupNameUserGroup = USER_GROUP_ACCESS.findByGroupName(
					"JUnit UserGroupOne", true, null, null, false);
		} catch (MetadataAccessException e) {
			assertTrue(
					"MetadataAccessException caught trying to findByGroupName():"
							+ e.getMessage(), false);
		}
		assertNotNull(
				"findByGroupName(JUnit UserGroupOne) should found something",
				findByGroupNameUserGroup);
		assertEquals("findByGroupNameUserGroups should be UserGroupOne",
				findByGroupNameUserGroup.size(), 1);
		assertTrue("findByGroupNameUserGroups should container UserGroupOne",
				findByGroupNameUserGroup.contains(USER_GROUP_ONE));

		// Now try to find by like name. If I search for "UserGroupO", I should
		// get UserGroupOne, but if I search for "UserGroup", I should get
		// both
		Collection likeNameUserGroups = null;
		try {
			likeNameUserGroups = USER_GROUP_ACCESS.findByGroupName(
					"JUnit UserGroupO", false, null, null, false);
		} catch (MetadataAccessException e) {
			assertTrue(
					"MetadataAccessException caught trying to findByGroupName():"
							+ e.getMessage(), false);
		}
		assertTrue("findByGroupName should have USER_GROUP_ONE",
				likeNameUserGroups.contains(USER_GROUP_ONE));
		assertTrue("findByGroupName should NOT have USER_GROUP_TWO",
				!likeNameUserGroups.contains(USER_GROUP_TWO));

		likeNameUserGroups = null;
		try {
			likeNameUserGroups = USER_GROUP_ACCESS.findByGroupName("UserGroup",
					false, null, null, false);
		} catch (MetadataAccessException e) {
			assertTrue(
					"MetadataAccessException caught trying to findByGroupName():"
							+ e.getMessage(), false);
		}
		assertTrue("findByGroupName should have USER_GROUP_ONE",
				likeNameUserGroups.contains(USER_GROUP_ONE));
		assertTrue("findGroupName should have USER_GROUP_TWO",
				likeNameUserGroups.contains(USER_GROUP_TWO));

		// Now check the find all names and find all ID's
		Collection allIds = null;
		Collection allGroupNames = null;
		try {
			allIds = USER_GROUP_ACCESS.findAllIDs();
			allGroupNames = USER_GROUP_ACCESS.findAllGroupNames();
		} catch (MetadataAccessException e2) {
			assertTrue(
					"MetadataAccessException caught trying to "
							+ "findAllUserGroupIDs/findAllUserGroupNames():"
							+ e2.getMessage(), false);
		}

		// OK allIDs and names should have 2 more than before inserts
		assertNotNull("allIds should not be null", allIds);
		assertNotNull("allGroupNames should not be null", allGroupNames);
		assertTrue("allIds should have two more entries.",
				allIds.size() == (countBeforeInserts + 2));
		assertTrue("allGroupNames should have two more entries",
				allGroupNames.size() == (countBeforeInserts + 2));
		// Now make sure the ids and names are all in the returned collection
		assertTrue("UserGroup ID one should be there: ",
				allIds.contains(userGroupOneId));
		assertTrue("UserGroup ID two should be there: ",
				allIds.contains(userGroupTwoId));

		assertTrue("UserGroup name one should be there: ",
				allGroupNames.contains("JUnit UserGroupOne"));
		assertTrue("UserGroup name two should be there: ",
				allGroupNames.contains("JUnit UserGroupTwo"));

		// Now clean up
		try {
			USER_GROUP_ACCESS.delete(USER_GROUP_ONE);
		} catch (MetadataAccessException e) {
		}
		try {
			USER_GROUP_ACCESS.delete(USER_GROUP_TWO);
		} catch (MetadataAccessException e) {
		}
	}

	/**
	 * A log4J logger
	 */
	static Logger logger = Logger.getLogger(TestUserGroupAccess.class);
}