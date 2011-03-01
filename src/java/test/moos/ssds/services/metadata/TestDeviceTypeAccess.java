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

import javax.naming.NamingException;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DeviceType;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataFactory;
import moos.ssds.services.metadata.DeviceTypeAccess;

import org.apache.log4j.Logger;

/**
 * This class tests the DeviceTypeAccess service EJB to make sure all is well.
 * There has to be an SSDS server running somewhere for this to hit against and
 * a jndi.properties in the classpath so the tests can get to the server
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.9 $
 */
public class TestDeviceTypeAccess extends TestAccessCase {

	// The connection to the service classes
	DeviceTypeAccess deviceTypeAccess = null;

	// The test DeviceTypes
	String deviceTypeOneStringRep = "DeviceType|" + "name=DeviceTypeOne|"
			+ "description=DeviceType One Description";
	String deviceTypeTwoStringRep = "DeviceType|" + "name=DeviceTypeTwo|"
			+ "description=DeviceType Two Description";

	// The delimiter
	String delimiter = "|";

	// The objects
	DeviceType deviceTypeOne = null;
	DeviceType deviceTypeTwo = null;

	/**
	 * A log4J logger
	 */
	static Logger logger = Logger.getLogger(TestDeviceTypeAccess.class);

	/**
	 * A constructor
	 * 
	 * @param name
	 */
	public TestDeviceTypeAccess(String name) {
		super(name);
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {

		// Setup the super class
		super.setUp();

		try {
			deviceTypeAccess = (DeviceTypeAccess) context
					.lookup("moos/ssds/sevices/metadata/DeviceTypeAccess");
		} catch (NamingException e) {
			logger.error("CreateException caught while creating deviceTypeAccess interface: "
					+ e.getMessage());
		}

		try {
			deviceTypeOne = (DeviceType) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							deviceTypeOneStringRep, delimiter);
			deviceTypeTwo = (DeviceType) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							deviceTypeTwoStringRep, delimiter);
		} catch (MetadataException e) {
			logger.error("MetadataException caught trying to create two DeviceType objects: "
					+ e.getMessage());
		} catch (ClassCastException cce) {
			logger.error("ClassCastException caught trying to create two DeviceType objects: "
					+ cce.getMessage());
		}
	}

	/**
	 * Run suite of tests on deviceType one
	 */
	public void testOne() {
		logger.debug("DeviceType one is "
				+ deviceTypeOne.toStringRepresentation("|"));
		this.deviceTypeTest(deviceTypeOne);
		logger.debug("Done with test one");
	}

	/**
	 * Run suite of tests on deviceType two
	 */
	public void testTypeTwo() {
		logger.debug("DeviceType two is "
				+ deviceTypeTwo.toStringRepresentation("|"));
		deviceTypeTest(deviceTypeTwo);
		logger.debug("Done with test two");
	}

	/**
	 * This is the suite of tests to run on a deviceType
	 * 
	 * @param device
	 */
	private void deviceTypeTest(DeviceType deviceType) {

		// The ID of the deviceType
		Long deviceTypeId = null;
		deviceTypeId = testInsert(deviceType, deviceTypeAccess);

		// Now query back by ID and make sure all attributes are equal
		DeviceType persistedDeviceType = null;

		try {
			persistedDeviceType = (DeviceType) deviceTypeAccess.findById(
					deviceTypeId, false);
		} catch (MetadataAccessException e1) {
			logger.error("MetadataAccessException caught during findById: "
					+ e1.getMessage());
		}

		// Now check that they are equal
		assertEquals("The two deviceTypes should be considered equal",
				deviceType, persistedDeviceType);

		// Check all the getter methods are equal
		testEqualityOfAllGetters(deviceType, persistedDeviceType);

		// Create a map with the values to update
		HashMap variablesToUpdate = new HashMap();

		// Change the description
		Object[] variable2 = new Object[1];
		variable2[0] = new String("Updated Description");
		variablesToUpdate.put("Description", variable2);

		testUpdate(persistedDeviceType, variablesToUpdate, deviceTypeAccess);

		testDelete(persistedDeviceType, deviceTypeAccess);
	}

	/**
	 * This test checks that all the find by methods work correctly
	 */
	public void testFindBys() {
		// OK, let's fist let's null out all ID's
		deviceTypeOne.setId(null);
		deviceTypeTwo.setId(null);

		// OK now insert all six deviceTypes
		Long deviceTypeOneId = null;
		Long deviceTypeTwoId = null;
		try {
			deviceTypeOneId = deviceTypeAccess.insert(deviceTypeOne);
			deviceTypeTwoId = deviceTypeAccess.insert(deviceTypeTwo);
		} catch (MetadataAccessException e) {
			logger.error("MetadataAccessException caught inserting deviceTypes in find by test: "
					+ e.getMessage());
			assertTrue(
					"MetadataAccessException caught inserting deviceTypes in find by test: "
							+ e.getMessage(), false);
		}
		logger.debug("DeviceType one's ID is " + deviceTypeOneId);
		logger.debug("DeviceType two's ID is " + deviceTypeTwoId);

		// OK, now let's do the find by id's
		DeviceType persistedDeviceTypeOne = null;
		try {
			persistedDeviceTypeOne = (DeviceType) deviceTypeAccess.findById(
					deviceTypeOneId, false);
		} catch (MetadataAccessException e) {
			assertTrue(
					"MetadataAccessException caught trying to findById(Long):"
							+ e.getMessage(), false);
		}

		// Make sure they are equal
		assertEquals("The two deviceType one's should be equal", deviceTypeOne,
				persistedDeviceTypeOne);

		// Now by little long
		persistedDeviceTypeOne = null;
		try {
			persistedDeviceTypeOne = (DeviceType) deviceTypeAccess.findById(
					deviceTypeOneId.longValue(), false);
		} catch (MetadataAccessException e) {
			assertTrue(
					"MetadataAccessException caught trying to findById(long):"
							+ e.getMessage(), false);
		}
		// Make sure they are equal
		assertEquals("The two deviceType one's should be equal", deviceTypeOne,
				persistedDeviceTypeOne);

		// Now find by string
		persistedDeviceTypeOne = null;
		try {
			persistedDeviceTypeOne = (DeviceType) deviceTypeAccess.findById(
					deviceTypeOneId.toString(), false);
		} catch (MetadataAccessException e) {
			assertTrue(
					"MetadataAccessException caught trying to findById(String):"
							+ e.getMessage(), false);
		}
		// Make sure they are equal
		assertEquals("The two deviceType one's should be equal", deviceTypeOne,
				persistedDeviceTypeOne);

		// Now try the find ID method
		Long idByDeviceTypeFind = null;
		try {
			idByDeviceTypeFind = deviceTypeAccess.findId(deviceTypeOne);
		} catch (MetadataAccessException e) {
			assertTrue(
					"MetadataAccessException caught trying to findId(DeviceType):"
							+ e.getMessage(), false);
		}
		assertEquals("DeviceType ids should be equal after findId(DeviceType)",
				deviceTypeOneId, idByDeviceTypeFind);

		// Check the find equivalent persistent object
		DeviceType equivalentDeviceTypeOne = null;
		try {
			Long equivalentId = deviceTypeAccess.findId(deviceTypeOne);
			equivalentDeviceTypeOne = (DeviceType) deviceTypeAccess.findById(
					equivalentId, false);
		} catch (MetadataAccessException e) {
			assertTrue(
					"MetadataAccessException caught trying to findEquivalentPersistentObject(DeviceType):"
							+ e.getMessage(), false);
		}
		assertEquals("Id of the equivalent persistent object"
				+ " should match that of insert", deviceTypeOneId,
				equivalentDeviceTypeOne.getId());

		// Now make sure all the deviceTypes are returned in the findAll method
		Collection allDeviceTypes = null;
		try {
			allDeviceTypes = deviceTypeAccess.findAll(null, null, false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataAccessException caught trying to findAll():"
					+ e.getMessage(), false);
		}
		assertTrue("findAll should have deviceTypeOne",
				allDeviceTypes.contains(deviceTypeOne));
		assertTrue("findAll should have deviceTypeTwo",
				allDeviceTypes.contains(deviceTypeTwo));

		// Now test the find by exact name
		DeviceType findByNameDeviceType = null;
		try {
			findByNameDeviceType = (DeviceType) deviceTypeAccess.findByName(
					"DeviceType", false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataAccessException caught trying to findByName():"
					+ e.getMessage(), false);
		}
		assertNull("findByName(DeviceType) should be an empty return",
				findByNameDeviceType);
		findByNameDeviceType = null;
		try {
			findByNameDeviceType = (DeviceType) deviceTypeAccess.findByName(
					"DeviceTypeOne", false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataAccessException caught trying to findByName():"
					+ e.getMessage(), false);
		}
		assertNotNull("findByName(DeviceTypeOne) should found something",
				findByNameDeviceType);
		assertEquals("findByNameDeviceTypes should be DeviceTypeOne",
				findByNameDeviceType, deviceTypeOne);

		// Now try to find by like name. If I search for "DeviceTypeO", I should
		// get DeviceTypeOne, but if I search for "DeviceType", I should get
		// both
		Collection likeNameDeviceTypes = null;
		try {
			likeNameDeviceTypes = deviceTypeAccess.findByLikeName(
					"DeviceTypeO", null, null, false);
		} catch (MetadataAccessException e) {
			assertTrue(
					"MetadataAccessException caught trying to findByLikeName():"
							+ e.getMessage(), false);
		}
		assertTrue("findAll should have deviceTypeOne",
				likeNameDeviceTypes.contains(deviceTypeOne));
		assertTrue("findAll should NOT have deviceTypeTwo",
				!likeNameDeviceTypes.contains(deviceTypeTwo));

		likeNameDeviceTypes = null;
		try {
			likeNameDeviceTypes = deviceTypeAccess.findByLikeName("DeviceType",
					null, null, false);
		} catch (MetadataAccessException e) {
			assertTrue(
					"MetadataAccessException caught trying to findByLikeName():"
							+ e.getMessage(), false);
		}
		assertTrue("findAll should have deviceTypeOne",
				likeNameDeviceTypes.contains(deviceTypeOne));
		assertTrue("findAll should have deviceTypeTwo",
				likeNameDeviceTypes.contains(deviceTypeTwo));

		// Now check the find all names and find all ID's
		Collection allIds = null;
		Collection allNames = null;
		try {
			allIds = deviceTypeAccess.findAllIDs();
			allNames = deviceTypeAccess.findAllNames();
		} catch (MetadataAccessException e2) {
			assertTrue(
					"MetadataAccessException caught trying to "
							+ "findAllDeviceTypeIDs/findAllDeviceTypeNames():"
							+ e2.getMessage(), false);
		}

		// OK allIDs and names should have 2 members
		assertNotNull("allIds should not be null", allIds);
		assertNotNull("allNames should not be null", allNames);
		assertTrue("allIds should have two (or more) entries.",
				allIds.size() >= 2);
		assertTrue("allNames should have two (or more) entries",
				allNames.size() >= 2);
		// Now make sure the ids and names are all in the returned collection
		assertTrue("DeviceType ID one should be there: ",
				allIds.contains(deviceTypeOneId));
		assertTrue("DeviceType ID two should be there: ",
				allIds.contains(deviceTypeTwoId));

		assertTrue("DeviceType name one should be there: ",
				allNames.contains("DeviceTypeOne"));
		assertTrue("DeviceType name two should be there: ",
				allNames.contains("DeviceTypeTwo"));

		// Now clean up
		try {
			deviceTypeAccess.delete(deviceTypeOne);
		} catch (MetadataAccessException e) {
		}
		try {
			deviceTypeAccess.delete(deviceTypeTwo);
		} catch (MetadataAccessException e) {
		}
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
		// Delete any deviceType objects as we don't want any leftover's if a
		// test
		// fails
		try {
			deviceTypeAccess.delete(deviceTypeOne);
		} catch (MetadataAccessException e) {
		}
		try {
			deviceTypeAccess.delete(deviceTypeTwo);
		} catch (MetadataAccessException e) {
		}
	}
}