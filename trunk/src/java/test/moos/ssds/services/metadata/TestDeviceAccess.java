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
import java.util.HashMap;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.DeviceType;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataFactory;
import moos.ssds.services.metadata.DeviceAccess;
import moos.ssds.services.metadata.DeviceAccessHome;
import moos.ssds.services.metadata.DeviceAccessUtil;
import moos.ssds.services.metadata.DeviceTypeAccess;
import moos.ssds.services.metadata.DeviceTypeAccessHome;
import moos.ssds.services.metadata.DeviceTypeAccessUtil;
import moos.ssds.services.metadata.PersonAccess;
import moos.ssds.services.metadata.PersonAccessHome;
import moos.ssds.services.metadata.PersonAccessUtil;

import org.apache.log4j.Logger;
import org.doomdark.uuid.UUID;

/**
 * This class tests the DeviceAccess service EJB to make sure all is well. There
 * has to be an SSDS server running somewhere for this to hit against and a
 * jndi.properties in the classpath so the tests can get to the server
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.20 $
 */
public class TestDeviceAccess extends TestAccessCase {

    /**
     * A constructor
     * 
     * @param name
     */
    public TestDeviceAccess(String name) {
        super(name);
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {

        // Setup the super class
        super.setUp();

        // Grab a person facade
        try {
            deviceAccessHome = DeviceAccessUtil.getHome();
            personAccessHome = PersonAccessUtil.getHome();
            deviceTypeAccessHome = DeviceTypeAccessUtil.getHome();
        } catch (NamingException ex) {
            logger
                .error("NamingException caught while getting AccessHome's from app server: "
                    + ex.getMessage());
        }
        try {
            deviceAccess = deviceAccessHome.create();
            personAccess = personAccessHome.create();
            deviceTypeAccess = deviceTypeAccessHome.create();
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught while creating Access interfaces: "
                    + e.getMessage());
        } catch (CreateException e) {
            logger
                .error("CreateException caught while creating Access interfaces: "
                    + e.getMessage());
        }

        this.setupObjects();
    }

    /**
     * Run suite of tests on device one
     */
    public void testOne() {
        logger.debug("Device one is " + deviceOne.toStringRepresentation("|"));
        this.deviceTest(deviceOne);
        logger.debug("Done with test one");
    }

    /**
     * Run suite of tests on device two
     */
    public void testTwo() {
        logger.debug("Device two is " + deviceTwo.toStringRepresentation("|"));
        deviceTest(deviceTwo);
        logger.debug("Done with test two");
    }

    /**
     * This is the suite of tests to run on a device
     * 
     * @param device
     */
    private void deviceTest(Device device) {

        logger.debug("Entering deviceTest");
        // The ID of the device
        Long deviceId = null;
        logger.debug("Going to call testInsert on TestAccessCase");
        deviceId = testInsert(device, deviceAccess);
        logger.debug("OK, the insert test should be complete");

        // Now query back by ID and make sure all attributes are equal
        Device persistedDevice = null;

        logger.debug("Going to look up that inserted device now by id "
            + deviceId);
        try {
            persistedDevice = (Device) deviceAccess.findById(deviceId, false);
        } catch (RemoteException e1) {
            logger.error("RemoteException caught during findById: "
                + e1.getMessage());
        } catch (MetadataAccessException e1) {
            logger.error("MetadataAccessException caught during findById: "
                + e1.getMessage());
        }

        // Now check that they are equal
        assertEquals("The two devices should be considered equal", device,
            persistedDevice);

        // Check all the getter methods are equal
        logger.debug("Going to check equality of all getters");
        testEqualityOfAllGetters(device, persistedDevice);
        logger.debug("OK, all getters checked");

        // Create a map with the values to update
        HashMap variablesToUpdate = new HashMap();

        // Change the name
        Object[] variable1 = new Object[1];
        variable1[0] = new String("UpdatedName");
        variablesToUpdate.put("Name", variable1);

        // Change the description
        Object[] variable2 = new Object[1];
        variable2[0] = new String("Updated Description");
        variablesToUpdate.put("Description", variable2);

        logger.debug("Going to call testUpdate on TestAccessCase");
        testUpdate(persistedDevice, variablesToUpdate, deviceAccess);
        logger.debug("OK, testUpdate complete");

        testDelete(persistedDevice, deviceAccess);
    }

    /**
     * This method checks to see that the ID generation is working correctly
     */
    public void testIDGeneration() {
        this.setupObjects();

        Long deviceID = null;
        try {
            deviceID = deviceAccess.insert(deviceOne);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught: " + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue("MetadataAccessException caught: " + e.getMessage(),
                false);
        }
        assertNotNull("The ID should not be null", deviceID);

        // Now remove it
        try {
            deviceAccess.delete(deviceOne);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught: " + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue("MetadataAccessException caught: " + e.getMessage(),
                false);
        }

        // Now clear the device id
        deviceOne.setId(null);

        // Now reinsert it
        Long deviceIDSecondInsert = null;
        try {
            deviceIDSecondInsert = deviceAccess.insert(deviceOne);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught: " + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue("MetadataAccessException caught: " + e.getMessage(),
                false);
        }
        assertNotNull("Id should not be null after second insert",
            deviceIDSecondInsert);
        assertTrue("The first and second IDs should not be the same", deviceID
            .longValue() != deviceIDSecondInsert.longValue());
        assertTrue("The second ID should be larger than the first",
            deviceIDSecondInsert.longValue() > deviceID.longValue());

        this.cleanObjectsFromDB();
    }

    /**
     * This method makes sure that the insert, update and delete methods work on
     * the relationships the way we expect them to.
     */
    public void testRelationships() {
        this.setupObjects();
        // OK, let's fist let's null out all ID's
        deviceOne.setId(null);
        deviceTwo.setId(null);
        personOne.setId(null);
        personTwo.setId(null);

        // Now, let's set up the relationships
        deviceOne.setPerson(personOne);
        deviceTwo.setPerson(personTwo);
        logger.debug("Set a person to each device");

        // Now let's insert both of them
        Long deviceOneId = null;
        Long deviceTwoId = null;
        try {
            deviceOneId = deviceAccess.insert(deviceOne);
            deviceTwoId = deviceAccess.insert(deviceTwo);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught inserting devices in relationship test: "
                    + e.getMessage());
            assertTrue(
                "RemoteException caught inserting devices in relationship test: "
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught inserting devices in relationship test: "
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught inserting devices in relationship test: "
                    + e.getMessage(), false);
        }
        logger.debug("Device one's ID is " + deviceOneId);
        logger.debug("Device two's ID is " + deviceTwoId);

        // Now query back for them and they
        Device persistentOne = null;
        Device persistentTwo = null;
        try {
            persistentOne = (Device) deviceAccess.findById(deviceOneId, false);
            persistentTwo = (Device) deviceAccess.findById(deviceTwoId, false);
        } catch (RemoteException e) {
            logger.error("RemoteException caught trying to read back devices: "
                + e.getMessage());
            assertTrue("RemoteException caught trying to read back devices: "
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught trying to read back devices: "
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught trying to read back devices: "
                    + e.getMessage(), false);
        }

        // Check that they are not null
        assertNotNull(
            "persistentOne should not be null after find by id using id of "
                + deviceOneId, persistentOne);
        assertNotNull(
            "persistentTwo should not be null after find by id using id of "
                + deviceOneId, persistentOne);

        // Now try to update one of the attributes on persistentOne and update
        // it
        try {
            persistentOne.setName("BlahName");
        } catch (MetadataException e2) {
            assertTrue("MetadataException caught trying to read back devices: "
                + e2.getMessage(), false);
        }
        try {
            deviceAccess.update(persistentOne);
        } catch (RemoteException e1) {
            logger.error("RemoteException caught trying to read back devices: "
                + e1.getMessage());
            assertTrue("RemoteException caught trying to read back devices: "
                + e1.getMessage(), false);
        } catch (MetadataAccessException e1) {
            logger
                .error("MetadataAccessException caught trying to read back devices: "
                    + e1.getMessage());
            assertTrue(
                "MetadataAccessException caught trying to read back devices: "
                    + e1.getMessage(), false);
        }

        // Now grab the same object but with its relationships filled out
        Device persistentOneFilled = null;
        Device persistentTwoFilled = null;

        try {
            persistentOneFilled = (Device) deviceAccess
                .getMetadataObjectGraph(persistentOne);
            persistentTwoFilled = (Device) deviceAccess
                .getMetadataObjectGraph(persistentTwo);
        } catch (RemoteException e) {
            logger.error("RemoteException caught trying to read back devices: "
                + e.getMessage());
            assertTrue("RemoteException caught trying to read back devices: "
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught trying to read back devices: "
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught trying to read back devices: "
                    + e.getMessage(), false);
        }
        assertNotNull("PersonOne attached to deviceOne should not be null",
            persistentOneFilled.getPerson());
        assertNotNull("PersonTwo attached to deviceTwo should not be null",
            persistentTwoFilled.getPerson());

        // Grab the person Ids
        Long personOneId = persistentOneFilled.getPerson().getId();
        Long personTwoId = persistentTwoFilled.getPerson().getId();
        logger.debug("Person one's id = " + personOneId);
        logger.debug("Person two's id = " + personTwoId);

        // Now check object equality
        assertEquals(
            "The persistent and local object for device one should be equal",
            deviceOne, persistentOne);
        assertEquals(
            "The persistent and local object for device two should be equal",
            deviceTwo, persistentTwo);
        assertEquals(
            "The persistent and local object for person one should be equal",
            personOne, persistentOneFilled.getPerson());
        assertEquals(
            "The persistent and local object for person two should be equal",
            personTwo, persistentTwoFilled.getPerson());
        // Now I should be able to set the first person to the second device and
        // do an update
        deviceTwo.setPerson(personOne);
        logger.debug("Set device two's person to person one "
            + "(device one and two person should be same)");

        Long deviceTwoUpdatedId = null;
        try {
            deviceTwoUpdatedId = deviceAccess.update(deviceTwo);
        } catch (RemoteException e) {
            logger.error("RemoteException caught trying to update device two: "
                + e.getMessage());
            assertTrue("RemoteException caught trying to update device two: "
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught trying to update device two: "
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught trying to read back devices: "
                    + e.getMessage(), false);
        }
        logger.debug("Device two should have been updated"
            + " in the persistent store with new person");

        try {
            persistentTwoFilled = (Device) deviceAccess
                .getMetadataObjectGraph(persistentTwo);
        } catch (RemoteException e) {
            logger.error("RemoteException caught trying to read back devices: "
                + e.getMessage());
            assertTrue("RemoteException caught trying to read back devices: "
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught trying to read back devices: "
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught trying to read back devices: "
                    + e.getMessage(), false);
        }
        logger.debug("OK, looked up the updated device");
        assertEquals("Device one and device two's person should be equal",
            persistentOneFilled.getPerson(), persistentTwoFilled.getPerson());

        // Now delete both devices and the people should still be there.
        logger.debug("Going to delete both devices");
        try {
            deviceAccess.delete(persistentOne);
            deviceAccess.delete(persistentTwo);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught trying to delete the devices: "
                    + e.getMessage());
            assertTrue("RemoteException caught trying to delete the devices: "
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught trying to delete the devices:"
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught trying to delete the devices: "
                    + e.getMessage(), false);
        }

        // Check find bys
        logger.debug("OK, devices deleted, let's see about finding the people");
        Person persistentPersonOne = null;
        Person persistentPersonTwo = null;
        try {
            persistentPersonOne = (Person) personAccess.findById(personOneId,
                false);
            persistentPersonTwo = (Person) personAccess.findById(personTwoId,
                false);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught trying to find the people after deleting the devices: "
                    + e.getMessage());
            assertTrue(
                "RemoteException caught trying to find the people after deleting the devices: "
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger.error("MetadataAccessException caught trying to find"
                + " the people after deleting the devices: " + e.getMessage());
            assertTrue("MetadataAccessException caught trying to find"
                + " the people after deleting the devices: " + e.getMessage(),
                false);
        }

        assertNotNull(
            "Person one should still be around after device one deleted",
            persistentPersonOne);
        assertNotNull(
            "Person two should still be around after device two deleted",
            persistentPersonTwo);

        logger.debug("Let's try to find the devices (should not)");
        Device deletedOne = null;
        Device deletedTwo = null;
        try {
            deletedOne = (Device) deviceAccess.findById(deviceOneId, false);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught trying to find the devices after deleting them: "
                    + e.getMessage());
            assertTrue(
                "RemoteException caught trying to find the devices after deleting them: "
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger.error("MetadataAccessException caught trying to find"
                + " the devices after deleting them: " + e.getMessage());
            assertTrue("MetadataAccessException caught trying to find"
                + " the devices after deleting them: " + e.getMessage(), false);
        }
        try {
            deletedTwo = (Device) deviceAccess.findById(deviceTwoId, false);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught trying to find the devices after deleting them: "
                    + e.getMessage());
            assertTrue(
                "RemoteException caught trying to find the devices after deleting them: "
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger.error("MetadataAccessException caught trying to find"
                + " the devices after deleting them: " + e.getMessage());
            assertTrue("MetadataAccessException caught trying to find"
                + " the devices after deleting them: " + e.getMessage(), false);
        }

        assertNull("Device one should not have been found after delete",
            deletedOne);
        assertNull("Device two should nto have been found after delete",
            deletedTwo);

        // Now clean up by deleting the persons
        try {
            personAccess.delete(personOne);
        } catch (RemoteException e) {
            logger.error("RemoteException caught trying to delete the people: "
                + e.getMessage());
            assertTrue("RemoteException caught trying to delete the people: "
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught trying to delete the people: "
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught trying to delete the people: "
                    + e.getMessage(), false);
        }
        try {
            personAccess.delete(personTwo);
        } catch (RemoteException e) {
            logger.error("RemoteException caught trying to delete the people: "
                + e.getMessage());
            assertTrue("RemoteException caught trying to delete the people: "
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught trying to delete the people: "
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught trying to delete the people: "
                    + e.getMessage(), false);
        }

        // Make sure they were removed
        Person deletedPersonOne = null;
        Person deletedPersonTwo = null;
        try {
            deletedPersonOne = (Person) personAccess.findById(personOneId,
                false);
            deletedPersonTwo = (Person) personAccess.findById(personTwoId,
                false);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught trying to find the people after deleting them: "
                    + e.getMessage());
            assertTrue(
                "RemoteException caught trying to find the people after deleting them: "
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger.error("MetadataAccessException caught trying to find"
                + " the people after deleting them: " + e.getMessage());
            assertTrue("MetadataAccessException caught trying to find"
                + " the people after deleting them: " + e.getMessage(), false);
        }

        assertNull("Person one should be gone", deletedPersonOne);
        assertNull("Person two should be gone", deletedPersonTwo);

        // **************************************************
        // * DeviceType relationship
        // **************************************************
        // OK, let's fist let's null out all ID's
        deviceOne.setId(null);
        deviceTwo.setId(null);
        deviceTypeOne.setId(null);
        deviceTypeTwo.setId(null);

        // Now, let's set up the relationships
        deviceOne.setDeviceType(deviceTypeOne);
        deviceTwo.setDeviceType(deviceTypeTwo);
        logger.debug("Set a deviceType to each device");

        // Now let's insert both of them
        deviceOneId = null;
        deviceTwoId = null;
        try {
            deviceOneId = deviceAccess.insert(deviceOne);
            deviceTwoId = deviceAccess.insert(deviceTwo);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught inserting devices in relationship test: "
                    + e.getMessage());
            assertTrue(
                "RemoteException caught inserting devices in relationship test: "
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught inserting devices in relationship test: "
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught inserting devices in relationship test: "
                    + e.getMessage(), false);
        }
        logger.debug("Device one's ID is " + deviceOneId);
        logger.debug("Device two's ID is " + deviceTwoId);

        // Now query back for them
        persistentOne = null;
        persistentTwo = null;
        try {
            persistentOne = (Device) deviceAccess.findById(deviceOneId, false);
            persistentTwo = (Device) deviceAccess.findById(deviceTwoId, false);
        } catch (RemoteException e) {
            logger.error("RemoteException caught trying to read back devices: "
                + e.getMessage());
            assertTrue("RemoteException caught trying to read back devices: "
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught trying to read back devices: "
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught trying to read back devices: "
                    + e.getMessage(), false);
        }

        // Check that they are not null and deviceTypes are still attached
        assertNotNull(
            "persistentOne should not be null after find by id using id of "
                + deviceOneId, persistentOne);
        assertNotNull(
            "persistentTwo should not be null after find by id using id of "
                + deviceOneId, persistentOne);

        // Now grab the full object graph of the persistent devices
        Device persistOneFull = null;
        Device persistTwoFull = null;
        try {
            persistOneFull = (Device) deviceAccess
                .getMetadataObjectGraph(persistentOne);
            persistTwoFull = (Device) deviceAccess
                .getMetadataObjectGraph(persistentTwo);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught trying to read back device object graphs: "
                    + e.getMessage());
            assertTrue(
                "RemoteException caught trying to read back device object graphs: "
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught trying to read back device object graph: "
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught trying to read back device object graph: "
                    + e.getMessage(), false);
        }

        assertNotNull("DeviceTypeOne attached to deviceOne should not be null",
            persistOneFull.getDeviceType());
        assertNotNull("DeviceTypeTwo attached to deviceTwo should not be null",
            persistTwoFull.getDeviceType());

        // Grab the deviceType Ids
        Long deviceTypeOneId = persistOneFull.getDeviceType().getId();
        Long deviceTypeTwoId = persistTwoFull.getDeviceType().getId();
        logger.debug("DeviceType one's id = " + deviceTypeOneId);
        logger.debug("DeviceType two's id = " + deviceTypeTwoId);

        // Now check object equality
        assertEquals(
            "The persistent and local object for device one should be equal",
            deviceOne, persistOneFull);
        assertEquals(
            "The persistent and local object for device two should be equal",
            deviceTwo, persistTwoFull);
        assertEquals(
            "The persistent and local object for deviceType one should be equal",
            deviceTypeOne, persistOneFull.getDeviceType());
        assertEquals(
            "The persistent and local object for deviceType two should be equal",
            deviceTypeTwo, persistTwoFull.getDeviceType());

        // Now I should be able to set the first deviceType to the second device
        // and do an update
        deviceTwo.setDeviceType(deviceTypeOne);
        logger.debug("Set device two's deviceType to deviceType one "
            + "(device one and two's deviceType should be same)");

        deviceTwoUpdatedId = null;
        try {
            deviceTwoUpdatedId = deviceAccess.update(deviceTwo);
        } catch (RemoteException e) {
            logger.error("RemoteException caught trying to update device two: "
                + e.getMessage());
            assertTrue("RemoteException caught trying to update device two: "
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught trying to update device two: "
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught trying to update deviceTwo: "
                    + e.getMessage(), false);
        }
        logger.debug("Device two should have been updated"
            + " in the persistent store with new deviceType");

        Device persistentUpdatedDeviceTwo = null;
        try {
            persistentUpdatedDeviceTwo = (Device) deviceAccess.findById(
                deviceTwoUpdatedId, false);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught trying to find the updated device: "
                    + e.getMessage());
            assertTrue(
                "RemoteException caught trying to find the updated device: "
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught trying to find the updated device:"
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught trying to find the updated device"
                    + e.getMessage(), false);
        }

        // Grab the full object graph
        persistTwoFull = null;
        try {
            persistTwoFull = (Device) deviceAccess
                .getMetadataObjectGraph(persistentUpdatedDeviceTwo);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught trying to read back device object graphs: "
                    + e.getMessage());
            assertTrue(
                "RemoteException caught trying to read back device object graphs: "
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught trying to read back device object graph: "
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught trying to read back device object graph: "
                    + e.getMessage(), false);
        }

        logger.debug("OK, looked up the updated device");
        assertEquals("Device one and device two's DeviceType should be equal",
            persistOneFull.getDeviceType(), persistTwoFull.getDeviceType());

        // Now delete both devices and the deviceTypes should still be
        // there.
        logger.debug("Going to delete both devices");
        try {
            deviceAccess.delete(persistentOne);
            deviceAccess.delete(persistentTwo);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught trying to delete the devices: "
                    + e.getMessage());
            assertTrue("RemoteException caught trying to delete the devices: "
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught trying to delete the devices: "
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught trying to delete the devices: "
                    + e.getMessage(), false);
        }

        // Check find bys
        logger
            .debug("OK, devices deleted, let's see about finding the deviceTypes");
        DeviceType persistentDeviceTypeOne = null;
        DeviceType persistentDeviceTypeTwo = null;
        try {
            persistentDeviceTypeOne = (DeviceType) deviceTypeAccess.findById(
                deviceTypeOneId, false);
            persistentDeviceTypeTwo = (DeviceType) deviceTypeAccess.findById(
                deviceTypeTwoId, false);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught trying to find the deviceTypes "
                    + "after deleting the devices: " + e.getMessage());
            assertTrue(
                "RemoteException caught trying to find the deviceTypes after "
                    + "deleting the devices: " + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger.error("MetadataAccessException caught trying to find"
                + " the deviceTypes after deleting the devices: "
                + e.getMessage());
            assertTrue("MetadataAccessException caught trying to find"
                + " the deviceTypes after deleting the devices: "
                + e.getMessage(), false);
        }

        assertNotNull(
            "DeviceType one should still be around after device one deleted",
            persistentDeviceTypeOne);
        assertNotNull(
            "DeviceType two should still be around after device two deleted",
            persistentDeviceTypeTwo);

        logger.debug("Let's try to find the devices (should not)");
        deletedOne = null;
        deletedTwo = null;
        try {
            deletedOne = (Device) deviceAccess.findById(deviceOneId, false);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught trying to find the devices after deleting them: "
                    + e.getMessage());
            assertTrue(
                "RemoteException caught trying to find the devices after deleting them: "
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger.error("MetadataAccessException caught trying to find"
                + " the devices after deleting them: " + e.getMessage());
            assertTrue("MetadataAccessException caught trying to find"
                + " the devices after deleting them: " + e.getMessage(), false);
        }
        try {
            deletedTwo = (Device) deviceAccess.findById(deviceTwoId, false);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught trying to find the devices after deleting them: "
                    + e.getMessage());
            assertTrue(
                "RemoteException caught trying to find the devices after deleting them: "
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger.error("MetadataAccessException caught trying to find"
                + " the devices after deleting them: " + e.getMessage());
            assertTrue("MetadataAccessException caught trying to find"
                + " the devices after deleting them: " + e.getMessage(), false);
        }

        assertNull("Device one should not have been found after delete",
            deletedOne);
        assertNull("Device two should nto have been found after delete",
            deletedTwo);

        // Now clean up by deleting the deviceTypes
        try {
            deviceTypeAccess.delete(deviceTypeOne);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught trying to delete the deviceType: "
                    + e.getMessage());
            assertTrue(
                "RemoteException caught trying to delete the deviceType: "
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught trying to delete the deviceType: "
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught trying to delete the deviceType: "
                    + e.getMessage(), false);
        }
        try {
            deviceTypeAccess.delete(deviceTypeTwo);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught trying to delete the deviceType: "
                    + e.getMessage());
            assertTrue(
                "RemoteException caught trying to delete the deviceType: "
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught trying to delete the deviceType: "
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught trying to delete the deviceType: "
                    + e.getMessage(), false);
        }

        // Make sure they were removed
        DeviceType deletedDeviceTypeOne = null;
        DeviceType deletedDeviceTypeTwo = null;
        try {
            deletedDeviceTypeOne = (DeviceType) deviceTypeAccess.findById(
                deviceTypeOneId, false);
            deletedDeviceTypeTwo = (DeviceType) deviceTypeAccess.findById(
                deviceTypeTwoId, false);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught trying to find the deviceType after deleting them: "
                    + e.getMessage());
            assertTrue(
                "RemoteException caught trying to find the deviceType after deleting them: "
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger.error("MetadataAccessException caught trying to find"
                + " the deviceType after deleting them: " + e.getMessage());
            assertTrue("MetadataAccessException caught trying to find"
                + " the peodeviceTypeple after deleting them: "
                + e.getMessage(), false);
        }

        assertNull("DeviceType one should be gone", deletedDeviceTypeOne);
        assertNull("DeviceType two should be gone", deletedDeviceTypeTwo);
        this.cleanObjectsFromDB();
    }

    /**
     * This test checks that all the find by methods work correctly
     */
    public void testFindBys() {
        this.setupObjects();
        // OK, let's fist let's null out all ID's
        deviceOne.setId(null);
        deviceTwo.setId(null);
        deviceThree.setId(null);
        deviceFour.setId(null);
        deviceFive.setId(null);
        deviceSix.setId(null);
        // OK now insert all six devices
        Long deviceOneId = null;
        Long deviceTwoId = null;
        Long deviceThreeId = null;
        Long deviceFourId = null;
        Long deviceFiveId = null;
        Long deviceSixId = null;
        try {
            deviceOneId = deviceAccess.insert(deviceOne);
            deviceTwoId = deviceAccess.insert(deviceTwo);
            deviceThreeId = deviceAccess.insert(deviceThree);
            deviceFourId = deviceAccess.insert(deviceFour);
            deviceFiveId = deviceAccess.insert(deviceFive);
            deviceSixId = deviceAccess.insert(deviceSix);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught inserting devices in relationship test: "
                    + e.getMessage());
            assertTrue(
                "RemoteException caught inserting devices in relationship test: "
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught inserting devices in relationship test: "
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught inserting devices in relationship test: "
                    + e.getMessage(), false);
        }
        logger.debug("Device one's ID is " + deviceOneId);
        logger.debug("Device two's ID is " + deviceTwoId);
        logger.debug("Device three's ID is " + deviceThreeId);
        logger.debug("Device four's ID is " + deviceFourId);
        logger.debug("Device five's ID is " + deviceFiveId);
        logger.debug("Device six's ID is " + deviceSixId);
        // OK, now let's do the find by id's
        Device persistedDeviceOne = null;
        try {
            persistedDeviceOne = (Device) deviceAccess.findById(deviceOneId,
                false);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findById(Long):"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findById(Long):"
                    + e.getMessage(), false);
        }
        // Make sure they are equal
        assertEquals("The two device one's should be equal", deviceOne,
            persistedDeviceOne);
        // Now by little long
        persistedDeviceOne = null;
        try {
            persistedDeviceOne = (Device) deviceAccess.findById(deviceOneId
                .longValue(), false);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findById(long):"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findById(long):"
                    + e.getMessage(), false);
        }
        // Make sure they are equal
        assertEquals("The two device one's should be equal", deviceOne,
            persistedDeviceOne);
        // Now find by string
        persistedDeviceOne = null;
        try {
            persistedDeviceOne = (Device) deviceAccess.findById(deviceOneId
                .toString(), false);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findById(String):"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findById(String):"
                    + e.getMessage(), false);
        }
        // Make sure they are equal
        assertEquals("The two device one's should be equal", deviceOne,
            persistedDeviceOne);
        // Now find by UUID
        persistedDeviceOne = null;
        try {
            persistedDeviceOne = (Device) deviceAccess.findByUuid(deviceOne
                .getUuid(), false);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findByUuid(String):"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findByUuid(String):"
                    + e.getMessage(), false);
        }
        // Make sure they are equal
        assertEquals("The two device one's should be equal", deviceOne,
            persistedDeviceOne);
        // Try by bytes
        persistedDeviceOne = null;
        byte[] uuidAsBytes = null;
        try {
            uuidAsBytes = UUID.valueOf(deviceOne.getUuid()).toByteArray();
        } catch (NumberFormatException e) {
            uuidAsBytes = null;
        }
        try {
            persistedDeviceOne = (Device) deviceAccess.findByUuid(uuidAsBytes,
                false);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findByUuid(byte []):"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findByUuid(byte []):"
                    + e.getMessage(), false);
        }
        // Make sure they are equal
        assertEquals("The two device one's should be equal", deviceOne,
            persistedDeviceOne);
        // Now try the find ID method
        Long idByDeviceFind = null;
        try {
            idByDeviceFind = deviceAccess.findId(deviceOne);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findId(Device):"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findId(Device):"
                    + e.getMessage(), false);
        }
        assertEquals("Device ids should be equal after findId(Device)",
            deviceOneId, idByDeviceFind);
        // Check the find equivalent persistent object
        Device equivalentDeviceOne = null;
        try {
            Long equivalentId = deviceAccess.findId(deviceOne);
            equivalentDeviceOne = (Device) deviceAccess.findById(equivalentId,
                false);
        } catch (RemoteException e1) {
            assertTrue(
                "RemoteException caught trying to findEquivalentPersistentObject(Device):"
                    + e1.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findEquivalentPersistentObject(Device):"
                    + e.getMessage(), false);
        }
        assertEquals("Id of the equivalent persistent object"
            + " should match that of insert", deviceOneId, equivalentDeviceOne
            .getId());
        // Now make sure all the devices are returned in the findAll method
        Collection allDevices = null;
        try {
            allDevices = deviceAccess.findAll(null, null, false);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findAll():"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue("MetadataAccessException caught trying to findAll():"
                + e.getMessage(), false);
        }
        assertTrue("findAll should have deviceOne", allDevices
            .contains(deviceOne));
        assertTrue("findAll should have deviceTwo", allDevices
            .contains(deviceTwo));
        assertTrue("findAll should have deviceThree", allDevices
            .contains(deviceThree));
        assertTrue("findAll should have deviceFour", allDevices
            .contains(deviceFour));
        assertTrue("findAll should have deviceFive", allDevices
            .contains(deviceFive));
        assertTrue("findAll should have deviceSix", allDevices
            .contains(deviceSix));
        // Now try to find by like name. If I search for "Like Name", I should
        // get all but the first two devices
        Collection likeNameDevices = null;
        try {
            likeNameDevices = deviceAccess.findByName("Like Name", false, "id",
                null, false);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findByLikeName():"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findByLikeName():"
                    + e.getMessage(), false);
        }
        assertTrue("findAll should NOT have deviceOne", !likeNameDevices
            .contains(deviceOne));
        assertTrue("findAll should NOT have deviceTwo", !likeNameDevices
            .contains(deviceTwo));
        assertTrue("findAll should have deviceThree", likeNameDevices
            .contains(deviceThree));
        assertTrue("findAll should have deviceFour", likeNameDevices
            .contains(deviceFour));
        assertTrue("findAll should have deviceFive", likeNameDevices
            .contains(deviceFive));
        assertTrue("findAll should have deviceSix", likeNameDevices
            .contains(deviceSix));
        // Now test the find by name and manufacturer info
        // This one should find devices three and four
        Collection byNameAndMFGStuff = null;
        try {
            byNameAndMFGStuff = deviceAccess.findByNameAndMfgInfo(null, false,
                "LikeNameMFG", false, null, false, null, false, "id", null,
                false);
        } catch (RemoteException e1) {
            assertTrue(
                "RemoteException caught trying to findByNameAndMfgInfo():"
                    + e1.getMessage(), false);
        } catch (MetadataAccessException e1) {
            assertTrue(
                "MetadataAccessException caught trying to findByNameAndMfgInfo():"
                    + e1.getMessage(), false);
        }
        assertTrue("There should be two in the return", byNameAndMFGStuff
            .size() == 2);
        assertTrue("Device three should be there", byNameAndMFGStuff
            .contains(deviceThree));
        assertTrue("Device four should be there", byNameAndMFGStuff
            .contains(deviceFour));
        // Now query with name that should narrow that return
        byNameAndMFGStuff = null;
        try {
            byNameAndMFGStuff = deviceAccess.findByNameAndMfgInfo(
                "Like Name One", false, "LikeNameMFG", false, null, false,
                null, false, "id", null, false);
        } catch (RemoteException e1) {
            assertTrue(
                "RemoteException caught trying to findByNameAndMfgInfo():"
                    + e1.getMessage(), false);
        } catch (MetadataAccessException e1) {
            assertTrue(
                "MetadataAccessException caught trying to findByNameAndMfgInfo():"
                    + e1.getMessage(), false);
        }
        assertTrue("There should be one in the return", byNameAndMFGStuff
            .size() == 1);
        assertTrue("Device three should be there", byNameAndMFGStuff
            .contains(deviceThree));
        // Now try to find one by serial number
        byNameAndMFGStuff = null;
        try {
            byNameAndMFGStuff = deviceAccess.findByNameAndMfgInfo(null, false,
                null, false, null, false, "LikeNameMFGSN", false, "id", null,
                false);
        } catch (RemoteException e1) {
            assertTrue(
                "RemoteException caught trying to findByNameAndMfgInfo():"
                    + e1.getMessage(), false);
        } catch (MetadataAccessException e1) {
            assertTrue(
                "MetadataAccessException caught trying to findByNameAndMfgInfo():"
                    + e1.getMessage(), false);
        }
        assertTrue("There should be two in the return", byNameAndMFGStuff
            .size() == 2);
        assertTrue("Device five should be there", byNameAndMFGStuff
            .contains(deviceFive));
        assertTrue("Device six should be there", byNameAndMFGStuff
            .contains(deviceSix));
        // Now check the find all names and find all ID's
        Collection allIds = null;
        Collection allNames = null;
        try {
            allIds = deviceAccess.findAllIDs();
            allNames = deviceAccess.findAllNames();
        } catch (RemoteException e2) {
            assertTrue(
                "RemoteException caught trying to findByNameAndMfgInfo():"
                    + e2.getMessage(), false);
        } catch (MetadataAccessException e2) {
            assertTrue(
                "MetadataAccessException caught trying to findByNameAndMfgInfo():"
                    + e2.getMessage(), false);
        }
        // OK allIDs and names should have 6 members
        assertNotNull("allIds should not be null", allIds);
        assertNotNull("allNames should not be null", allNames);
        assertTrue("allIds should have six (or more) entries.",
            allIds.size() >= 6);
        assertTrue("allNames should have six (or more) entries", allNames
            .size() >= 6);
        // Now make sure the ids and names are all in the returned collection
        assertTrue("Device ID one should be there: ", allIds
            .contains(deviceOneId));
        assertTrue("Device ID two should be there: ", allIds
            .contains(deviceTwoId));
        assertTrue("Device ID three should be there: ", allIds
            .contains(deviceThreeId));
        assertTrue("Device ID four should be there: ", allIds
            .contains(deviceFourId));
        assertTrue("Device ID five should be there: ", allIds
            .contains(deviceFiveId));
        assertTrue("Device ID six should be there: ", allIds
            .contains(deviceSixId));
        assertTrue("Device name one should be there: ", allNames
            .contains("DeviceOne"));
        assertTrue("Device name two should be there: ", allNames
            .contains("DeviceTwo"));
        assertTrue("Device name two should be there: ", allNames
            .contains("Like Name One"));
        assertTrue("Device name two should be there: ", allNames
            .contains("Like Name Two"));
        assertTrue("Device name two should be there: ", allNames
            .contains("Like Name Three"));
        assertTrue("Device name two should be there: ", allNames
            .contains("Like Name Four"));
        // Test the find by person method
        // At first there should be no people attached anywhere, so the result
        // should be empty
        personOne.setId(null);
        Collection devicesByPerson = null;
        boolean metadataExceptionCaught = false;
        try {
            devicesByPerson = deviceAccess.findByPerson(personOne, "id", null,
                false);
        } catch (RemoteException e1) {
            assertTrue("RemoteException caught trying to findByPerson:"
                + e1.getMessage(), false);
        } catch (MetadataAccessException e1) {
            metadataExceptionCaught = true;
        }
        // Check to make sure an exception was thrown (person one not yet in
        // database)
        assertTrue("MetadataException should have been thrown, "
            + "person not yet in database", metadataExceptionCaught);
        // Now insert the person
        try {
            personAccess.insert(personOne);
        } catch (RemoteException e1) {
            assertTrue("RemoteException caught trying to insert personOne:"
                + e1.getMessage(), false);
        } catch (MetadataAccessException e1) {
            assertTrue(
                "MetadataAccessException caught trying to insert personOne:"
                    + e1.getMessage(), false);
        }
        // Now try to find devices by person one
        devicesByPerson = null;
        try {
            devicesByPerson = deviceAccess.findByPerson(personOne, "id", null,
                false);
        } catch (RemoteException e1) {
            assertTrue("RemoteException caught trying to findByPerson:"
                + e1.getMessage(), false);
        } catch (MetadataAccessException e1) {
            assertTrue("MetadataAccessException caught trying to findByPerson:"
                + e1.getMessage(), false);
        }
        // The return should be empty, but not null
        assertNotNull(
            "The devices associated with person one should not be null",
            devicesByPerson);
        assertEquals(
            "The number of devices associated with the person should be 0",
            devicesByPerson.size(), 0);
        // Now link up the person to all but one of the devices
        deviceOne.setPerson(personOne);
        deviceTwo.setPerson(personOne);
        deviceFour.setPerson(personOne);
        deviceFive.setPerson(personOne);
        deviceSix.setPerson(personOne);
        // Now update all the devices
        try {
            deviceAccess.update(deviceOne);
            deviceAccess.update(deviceTwo);
            deviceAccess.update(deviceFour);
            deviceAccess.update(deviceFive);
            deviceAccess.update(deviceSix);
        } catch (RemoteException e1) {
            assertTrue(
                "RemoteException caught trying to update device after person added:"
                    + e1.getMessage(), false);
        } catch (MetadataAccessException e1) {
            assertTrue(
                "MetadataAccessException caught trying to udpate device after person added:"
                    + e1.getMessage(), false);
        }
        // Now re-run query by person
        // Now try to find devices by person one
        devicesByPerson = null;
        try {
            devicesByPerson = deviceAccess.findByPerson(personOne, "id", null,
                false);
        } catch (RemoteException e1) {
            assertTrue("RemoteException caught trying to findByPerson:"
                + e1.getMessage(), false);
        } catch (MetadataAccessException e1) {
            assertTrue("MetadataAccessException caught trying to findByPerson:"
                + e1.getMessage(), false);
        } // The return should not be null
        assertNotNull(
            "The devices associated with person one should not be null",
            devicesByPerson);
        assertEquals(
            "The number of devices associated with the person should be 5",
            devicesByPerson.size(), 5);
        assertTrue("DeviceOne should be there", devicesByPerson
            .contains(deviceOne));
        assertTrue("DeviceTwo should be there", devicesByPerson
            .contains(deviceTwo));
        assertTrue("DeviceThree should NOT  be there", !devicesByPerson
            .contains(deviceThree));
        assertTrue("DeviceFour should be there", devicesByPerson
            .contains(deviceFour));
        assertTrue("DeviceFive should be there", devicesByPerson
            .contains(deviceFive));
        assertTrue("DeviceSix should be there", devicesByPerson
            .contains(deviceSix));
        // Test the find by deviceType method
        // At first there should be no deviceTypes attached anywhere, so the
        // result
        // should be empty
        deviceTypeOne.setId(null);
        Collection devicesByDeviceType = null;
        metadataExceptionCaught = false;
        try {
            devicesByDeviceType = deviceAccess.findByDeviceType(deviceTypeOne,
                "id", null, false);
        } catch (RemoteException e1) {
            assertTrue("RemoteException caught trying to findByDeviceType:"
                + e1.getMessage(), false);
        } catch (MetadataAccessException e1) {
            metadataExceptionCaught = true;
        }
        // Check to make sure an exception was thrown (deviceType one not yet in
        // database)
        assertTrue("MetadataException should have been thrown, "
            + "deviceType not yet in database", metadataExceptionCaught);

        // Now insert the deviceType
        try {
            deviceTypeAccess.insert(deviceTypeOne);
        } catch (RemoteException e1) {
            assertTrue("RemoteException caught trying to insert deviceTypeOne:"
                + e1.getMessage(), false);
        } catch (MetadataAccessException e1) {
            assertTrue(
                "MetadataAccessException caught trying to insert persdeviceTypeOneonOne:"
                    + e1.getMessage(), false);
        }
        // Now try to find devices by deviceType one
        devicesByDeviceType = null;
        try {
            devicesByDeviceType = deviceAccess.findByDeviceType(deviceTypeOne,
                "id", null, false);
        } catch (RemoteException e1) {
            assertTrue("RemoteException caught trying to findByDeviceType:"
                + e1.getMessage(), false);
        } catch (MetadataAccessException e1) {
            assertTrue(
                "MetadataAccessException caught trying to findByDeviceType:"
                    + e1.getMessage(), false);
        }
        // The return should be empty, but not null
        assertNotNull(
            "The devices associated with deviceType one should not be null",
            devicesByDeviceType);
        assertEquals(
            "The number of devices associated with the deviceType should be 0",
            devicesByDeviceType.size(), 0);
        // Now link up the deviceType to all but one of the devices
        deviceOne.setDeviceType(deviceTypeOne);
        deviceTwo.setDeviceType(deviceTypeOne);
        deviceFour.setDeviceType(deviceTypeOne);
        deviceFive.setDeviceType(deviceTypeOne);
        deviceSix.setDeviceType(deviceTypeOne);
        // Now update all the devices
        try {
            deviceAccess.update(deviceOne);
            deviceAccess.update(deviceTwo);
            deviceAccess.update(deviceFour);
            deviceAccess.update(deviceFive);
            deviceAccess.update(deviceSix);
        } catch (RemoteException e1) {
            assertTrue(
                "RemoteException caught trying to update device after deviceType added:"
                    + e1.getMessage(), false);
        } catch (MetadataAccessException e1) {
            assertTrue(
                "MetadataAccessException caught trying to udpate device after deviceType added:"
                    + e1.getMessage(), false);
        }
        // Now re-run query by deviceType
        // Now try to find devices by deviceType one
        devicesByDeviceType = null;
        try {
            devicesByDeviceType = deviceAccess.findByDeviceType(deviceTypeOne,
                "id", null, false);
        } catch (RemoteException e1) {
            assertTrue("RemoteException caught trying to devicesByDeviceType:"
                + e1.getMessage(), false);
        } catch (MetadataAccessException e1) {
            assertTrue(
                "MetadataAccessException caught trying to devicesByDeviceType:"
                    + e1.getMessage(), false);
        }
        // The return should not be null
        assertNotNull(
            "The devices associated with deviceType one should not be null",
            devicesByDeviceType);
        assertEquals(
            "The number of devices associated with the deviceType should be 5",
            devicesByDeviceType.size(), 5);
        assertTrue("DeviceOne should be there", devicesByDeviceType
            .contains(deviceOne));
        assertTrue("DeviceTwo should be there", devicesByDeviceType
            .contains(deviceTwo));
        assertTrue("DeviceThree should NOT be there", !devicesByDeviceType
            .contains(deviceThree));
        assertTrue("DeviceFour should be there", devicesByDeviceType
            .contains(deviceFour));
        assertTrue("DeviceFive should be there", devicesByDeviceType
            .contains(deviceFive));
        assertTrue("DeviceSix should be there", devicesByDeviceType
            .contains(deviceSix));
        // Now clean up
        this.cleanObjectsFromDB();
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
        this.cleanObjectsFromDB();
    }

    private void setupObjects() {
        try {
            deviceOne = (Device) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    deviceOneStringRep, delimiter);
            deviceTwo = (Device) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    deviceTwoStringRep, delimiter);
            deviceThree = (Device) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    deviceThreeStringRep, delimiter);
            deviceFour = (Device) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    deviceFourStringRep, delimiter);
            deviceFive = (Device) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    deviceFiveStringRep, delimiter);
            deviceSix = (Device) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    deviceSixStringRep, delimiter);
            personOne = (Person) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    personOneStringRep, delimiter);
            personTwo = (Person) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    personTwoStringRep, delimiter);
            deviceTypeOne = (DeviceType) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    deviceTypeOneStringRep, delimiter);
            deviceTypeTwo = (DeviceType) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    deviceTypeTwoStringRep, delimiter);
        } catch (MetadataException e) {
            logger
                .error("MetadataException caught trying to create two Person objects: "
                    + e.getMessage());
        } catch (ClassCastException cce) {
            logger
                .error("ClassCastException caught trying to create two Person objects: "
                    + cce.getMessage());
        }
    }

    private void cleanObjectsFromDB() {
        // Delete any device objects as we don't want any leftover's if a test
        // fails
        try {
            deviceAccess.delete(deviceOne);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
        try {
            deviceAccess.delete(deviceTwo);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
        try {
            deviceAccess.delete(deviceThree);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
        try {
            deviceAccess.delete(deviceFour);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
        try {
            deviceAccess.delete(deviceFive);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
        try {
            deviceAccess.delete(deviceSix);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
        try {
            deviceTypeAccess.delete(deviceTypeOne);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
        try {
            deviceTypeAccess.delete(deviceTypeTwo);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
        try {
            personAccess.delete(personOne);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
        try {
            personAccess.delete(personTwo);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
    }

    // The connection to the service classes
    DeviceAccessHome deviceAccessHome = null;
    DeviceAccess deviceAccess = null;
    // The access services for the relationship classes
    PersonAccessHome personAccessHome = null;
    PersonAccess personAccess = null;
    DeviceTypeAccessHome deviceTypeAccessHome = null;
    DeviceTypeAccess deviceTypeAccess = null;

    // The test Devices
    String deviceOneStringRep = "Device|" + "name=DeviceOne|"
        + "description=Device One Description|" + "mfgName=DeviceOneMFG|"
        + "mfgModel=DeviceOneMFGModel|" + "mfgSerialNumber=DeviceOneMFGSN|";
    String deviceTwoStringRep = "Device|" + "name=DeviceTwo|"
        + "description=Device Two Description|" + "mfgName=DeviceTwoMFG|"
        + "mfgModel=DeviceTwoMFGModel|" + "mfgSerialNumber=DeviceTwoMFGSN|";
    String deviceThreeStringRep = "Device|" + "name=Like Name One|"
        + "description=Like Name One Description|" + "mfgName=LikeNameMFG|"
        + "mfgModel=LikeNameOneMFGModel|" + "mfgSerialNumber=LikeNameOneMFGSN|";
    String deviceFourStringRep = "Device|" + "name=Like Name Two|"
        + "description=Like Name Two Description|" + "mfgName=LikeNameMFG|"
        + "mfgModel=LikeNameTwoMFGModel|" + "mfgSerialNumber=LikeNameTwoMFGSN|";
    String deviceFiveStringRep = "Device|" + "name=Like Name Three|"
        + "description=Like Name Three Description|"
        + "mfgName=LikeNameThreeMFG|" + "mfgModel=LikeNameThreeMFGModel|"
        + "mfgSerialNumber=LikeNameMFGSN|";
    String deviceSixStringRep = "Device|" + "name=Like Name Four|"
        + "description=Like Name Four Description|"
        + "mfgName=LikeNameFourMFG|" + "mfgModel=LikeNameFourMFGModel|"
        + "mfgSerialNumber=LikeNameMFGSN|";

    // The test Persons
    String personOneStringRep = "Person|" + "firstname=John|" + "surname=Doe|"
        + "organization=MBARI|" + "email=jdoe@mbari.org|" + "username=jdoe|"
        + "password=dumbPassword|" + "status=active";

    String personTwoStringRep = "Person|" + "firstname=Jane|" + "surname=Doe|"
        + "organization=MBARI|" + "email=janedoe@mbari.org|"
        + "username=janedoe|" + "password=dumbPassword|" + "status=active";

    // The test DeviceTypes
    String deviceTypeOneStringRep = "DeviceType|" + "name=DeviceTypeOne|"
        + "description=DeviceType One Description";
    String deviceTypeTwoStringRep = "DeviceType|" + "name=DeviceTypeTwo|"
        + "description=DeviceType Two Description";

    // The delimiter
    String delimiter = "|";

    // The objects
    Device deviceOne = null;
    Device deviceTwo = null;
    Device deviceThree = null;
    Device deviceFour = null;
    Device deviceFive = null;
    Device deviceSix = null;
    Person personOne = null;
    Person personTwo = null;
    DeviceType deviceTypeOne = null;
    DeviceType deviceTypeTwo = null;

    /**
     * A log4J logger
     */
    static Logger logger = Logger.getLogger(TestDeviceAccess.class);
}