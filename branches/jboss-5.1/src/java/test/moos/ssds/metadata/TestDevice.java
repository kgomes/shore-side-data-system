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
package test.moos.ssds.metadata;

import junit.framework.TestCase;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.doomdark.uuid.UUID;
import org.doomdark.uuid.UUIDGenerator;

/**
 * This is the test class to test the Device class
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.3 $
 */
public class TestDevice extends TestCase {

    /**
     * @param arg0
     */
    public TestDevice(String arg0) {
        super(arg0);
    }

    protected void setUp() {
        BasicConfigurator.configure();
        logger.setLevel(Level.DEBUG);
        logger.addAppender(new ConsoleAppender(new PatternLayout(
            "%d %-5p [%c %M %L] %m%n")));
    }

    /**
     * This method checks the creation of a <code>Device</code> object
     */
    public void testCreateDevice() {
        // Create the new device
        Device device = new Device();

        // Set all the values
        device.setId(new Long(1));
        try {
            device.setDescription("Device description");
            device
                .setInfoUrlList("http://www.mbari.org;http://ssds.shore.mbari.org");
            device.setMfgModel("Mfg model");
            device.setMfgName("mfg name");
            device.setMfgSerialNumber("mfg serial number");
            device.setName("device name");
        } catch (MetadataException e1) {
            assertTrue("MetadataException caught trying to set values: "
                + e1.getMessage(), false);
        }
        UUIDGenerator uuidGenerator = UUIDGenerator.getInstance();
        UUID tempUUID = uuidGenerator.generateTimeBasedUUID();
        try {
            device.setUuid(tempUUID.toString());
        } catch (MetadataException e) {}
        logger.debug("UUID created (" + tempUUID.toString() + ")"
            + " and set on device (" + device.getUuid() + ")");

        // Now read all of them back
        assertEquals(device.getId(), new Long(1));
        assertEquals(device.getDescription(), "Device description");
        assertEquals(device.getInfoUrlList(),
            "http://www.mbari.org;http://ssds.shore.mbari.org");
        assertEquals(device.getMfgModel(), "Mfg model");
        assertEquals(device.getMfgName(), "mfg name");
        assertEquals(device.getMfgSerialNumber(), "mfg serial number");
        assertEquals(device.getName(), "device name");
        assertEquals(device.getUuid(), tempUUID.toString());
        // Convert to a real UUID and check again
        UUID convertedUUID = UUID.valueOf(device.getUuid());
        assertEquals("UUID's should be equal", tempUUID, convertedUUID);
    }

    /**
     * This method checks to see if the toStringRepresentation method works
     * properly
     */
    public void testToStringRepresentation() {
        // Create the new device
        Device device = new Device();

        // Set all the values
        device.setId(new Long(1));
        try {
            device.setDescription("Device description");
            device
                .setInfoUrlList("http://www.mbari.org;http://ssds.shore.mbari.org");
            device.setMfgModel("Mfg model");
            device.setMfgName("mfg name");
            device.setMfgSerialNumber("mfg serial number");
            device.setName("device name");
        } catch (MetadataException e1) {
            assertTrue("MetadataException caught trying to set values: "
                + e1.getMessage(), false);
        }
        UUIDGenerator uuidGenerator = UUIDGenerator.getInstance();
        UUID tempUUID = uuidGenerator.generateTimeBasedUUID();
        String uuidString = tempUUID.toString();
        try {
            device.setUuid(tempUUID.toString());
        } catch (MetadataException e) {
            assertTrue("MetadataException caught: " + e.getMessage(), false);
        }
        logger.debug("UUID created (" + tempUUID.toString() + ")"
            + " and set on device (" + device.getUuid() + ")");

        // Check that the string representations are equal
        String stringDevice = device.toStringRepresentation(",");
        logger.debug("To string rep = " + stringDevice);
        String stringRep = "Device," + "id=1," + "uuid=" + uuidString + ","
            + "name=device name," + "description=Device description,"
            + "mfgName=mfg name," + "mfgModel=Mfg model,"
            + "mfgSerialNumber=mfg serial number,"
            + "infoUrlList=http://www.mbari.org;http://ssds.shore.mbari.org";
        logger.debug("What it should be = " + stringRep);
        assertEquals(
            "The string represntation should match the set attributes",
            stringDevice, stringRep);
    }

    /**
     * This tests the method that sets the values from a string representation
     */
    public void testSetValuesFromStringRepresentation() {

        // Create the device
        Device device = new Device();

        // UUID stuff
        UUIDGenerator uuidGenerator = UUIDGenerator.getInstance();
        UUID tempUUID = uuidGenerator.generateTimeBasedUUID();
        String uuidString = tempUUID.toString();

        // Create the string representation
        String stringRep = "Device," + "id=1," + "uuid=" + uuidString + ","
            + "name=device name," + "description=Device description,"
            + "mfgName=mfg name," + "mfgModel=Mfg model,"
            + "mfgSerialNumber=mfg serial number,"
            + "infoUrlList=http://www.mbari.org;http://ssds.shore.mbari.org";

        try {
            device.setValuesFromStringRepresentation(stringRep, ",");
        } catch (MetadataException e) {
            logger.error("MetadataException caught trying to set "
                + "values from string representation: " + e.getMessage());
            assertTrue("Metadata Exception caught: " + e.getMessage(), false);
        }
        // Now check that everything was set OK
        assertEquals(device.getId(), new Long(1));
        assertEquals(device.getDescription(), "Device description");
        assertEquals(device.getInfoUrlList(),
            "http://www.mbari.org;http://ssds.shore.mbari.org");
        assertEquals(device.getMfgModel(), "Mfg model");
        assertEquals(device.getMfgName(), "mfg name");
        assertEquals(device.getMfgSerialNumber(), "mfg serial number");
        assertEquals(device.getName(), "device name");
        assertEquals(device.getUuid(), tempUUID.toString());
        // Convert to a real UUID and check again
        UUID convertedUUID = UUID.valueOf(device.getUuid());
        assertEquals("UUID's should be equal", tempUUID, convertedUUID);
    }

    /**
     * This method tests the equals method
     */
    public void testEquals() {
        // UUID stuff
        UUIDGenerator uuidGenerator = UUIDGenerator.getInstance();
        UUID tempUUID = uuidGenerator.generateTimeBasedUUID();
        UUID tempUUID2 = uuidGenerator.generateTimeBasedUUID();
        String uuidString = tempUUID.toString();

        // Create the string representation
        String stringRep = "Device," + "id=1," + "uuid=" + uuidString + ","
            + "name=device name," + "description=Device description,"
            + "mfgName=mfg name," + "mfgModel=Mfg model,"
            + "mfgSerialNumber=mfg serial number,"
            + "infoUrlList=http://www.mbari.org;http://ssds.shore.mbari.org";
        String stringRepTwo = "Device," + "id=1," + "uuid=" + uuidString + ","
            + "name=device name," + "description=Device description,"
            + "mfgName=mfg name," + "mfgModel=Mfg model,"
            + "mfgSerialNumber=mfg serial number,"
            + "infoUrlList=http://www.mbari.org;http://ssds.shore.mbari.org";

        Device deviceOne = new Device();
        Device deviceTwo = new Device();

        try {
            deviceOne.setValuesFromStringRepresentation(stringRep, ",");
            deviceTwo.setValuesFromStringRepresentation(stringRepTwo, ",");
        } catch (MetadataException e) {
            logger
                .error("MetadataException caught trying to create two device objects");
            assertTrue(
                "MetadataException caught trying to screate two device objects: "
                    + e.getMessage(), false);
        }

        assertTrue("The two devices should be equal (part one).", deviceOne
            .equals(deviceTwo));
        assertEquals("The two devices should be equal (part two).", deviceOne,
            deviceTwo);

        // Now change the ID of the second one and they should be equal
        deviceTwo.setId(new Long(2));
        assertTrue("The two device should be equal", deviceOne
            .equals(deviceTwo));

        // Now set the ID back, check equals again
        deviceTwo.setId(new Long(1));
        assertEquals("The two devices should be equal after ID set back.",
            deviceOne, deviceTwo);

        // Now set the UUID and they should be different
        try {
            deviceTwo.setUuid(tempUUID2.toString());
        } catch (MetadataException e) {
            logger.error("MetadataException caught trying to set the UUID: "
                + e.getMessage());
            assertTrue("MetadataException caught trying to set the UUID: "
                + e.getMessage(), false);
        }
        assertTrue("The two device should not be equal", !deviceOne
            .equals(deviceTwo));

        // Now set it back and change all the non-business key values. The
        // results should be equals
        try {
            deviceTwo.setUuid(tempUUID.toString());
        } catch (MetadataException e) {
            logger
                .error("MetadataException caught trying to set the username: "
                    + e.getMessage());
            assertTrue("MetadataException caught trying to set the UUID: "
                + e.getMessage(), false);
        }
        try {
            deviceTwo.setName("new name");
            deviceTwo.setDescription("new description");
            deviceTwo.setInfoUrlList("new infourl list");
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }
        deviceTwo.setMfgModel("new model");
        deviceTwo.setMfgName("new mfg name");
        deviceTwo.setMfgSerialNumber("new serial number");
        assertEquals("The two devices should be equal after UUID is set back.",
            deviceOne, deviceTwo);
    }

    /**
     * This method tests the hashCode method
     */
    public void testHashCode() {
        // UUID stuff
        UUIDGenerator uuidGenerator = UUIDGenerator.getInstance();
        UUID tempUUID = uuidGenerator.generateTimeBasedUUID();
        UUID tempUUID2 = uuidGenerator.generateTimeBasedUUID();
        String uuidString = tempUUID.toString();

        // Create the string representation
        String stringRep = "Device," + "id=1," + "uuid=" + uuidString + ","
            + "name=device name," + "description=Device description,"
            + "mfgName=mfg name," + "mfgModel=Mfg model,"
            + "mfgSerialNumber=mfg serial number,"
            + "infoUrlList=http://www.mbari.org;http://ssds.shore.mbari.org";
        String stringRepTwo = "Device," + "id=1," + "uuid=" + uuidString + ","
            + "name=device name," + "description=Device description,"
            + "mfgName=mfg name," + "mfgModel=Mfg model,"
            + "mfgSerialNumber=mfg serial number,"
            + "infoUrlList=http://www.mbari.org;http://ssds.shore.mbari.org";

        Device deviceOne = new Device();
        Device deviceTwo = new Device();

        try {
            deviceOne.setValuesFromStringRepresentation(stringRep, ",");
            deviceTwo.setValuesFromStringRepresentation(stringRepTwo, ",");
        } catch (MetadataException e) {
            logger
                .error("MetadataException caught trying to create two device objects");
            assertTrue(
                "MetadataException caught trying to screate two device objects: "
                    + e.getMessage(), false);
        }

        assertEquals("The two device hashcodes should be equal.", deviceOne
            .hashCode(), deviceTwo.hashCode());

        // Now change the ID of the second one and they should not be equal
        deviceTwo.setId(new Long(2));
        assertTrue("The two device hashcodes should be equal", deviceOne
            .hashCode() == deviceTwo.hashCode());

        // Now set the ID back, check equals again
        deviceTwo.setId(new Long(1));
        assertEquals(
            "The two device hashcodes should be equal after ID set back.",
            deviceOne.hashCode(), deviceTwo.hashCode());

        // Now set the UUID and they should be different
        try {
            deviceTwo.setUuid(tempUUID2.toString());
        } catch (MetadataException e) {
            logger.error("MetadataException caught trying to set the UUID: "
                + e.getMessage());
            assertTrue("MetadataException caught trying to set the UUID: "
                + e.getMessage(), false);
        }
        assertTrue("The two device hashcode should not be equal", deviceOne
            .hashCode() != deviceTwo.hashCode());

        // Now set it back and change all the non-business key values. The
        // results should be equals
        try {
            deviceTwo.setUuid(tempUUID.toString());
        } catch (MetadataException e) {
            logger
                .error("MetadataException caught trying to set the username: "
                    + e.getMessage());
            assertTrue("MetadataException caught trying to set the UUID: "
                + e.getMessage(), false);
        }
        try {
            deviceTwo.setName("new name");
            deviceTwo.setDescription("new description");
            deviceTwo.setInfoUrlList("new infourl list");
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }
        deviceTwo.setMfgModel("new model");
        deviceTwo.setMfgName("new mfg name");
        deviceTwo.setMfgSerialNumber("new serial number");
        assertEquals(
            "The two device hashcodes should be equal after UUID is set back.",
            deviceOne.hashCode(), deviceTwo.hashCode());
    }

    /**
     * The logger for dumping information to
     */
    static Logger logger = Logger.getLogger(TestDevice.class);
}