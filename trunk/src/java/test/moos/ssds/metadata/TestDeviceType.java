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
import moos.ssds.metadata.DeviceType;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * This is the test class to test the DeviceType class
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.4 $
 */
public class TestDeviceType extends TestCase {

    /**
     * @param arg0
     */
    public TestDeviceType(String arg0) {
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
    public void testCreateDeviceType() {
        // Create the new device
        DeviceType deviceType = new DeviceType();

        // Set all the values
        deviceType.setId(new Long(1));
        try {
            deviceType.setDescription("DeviceType description");
            deviceType.setName("deviceType name");
        } catch (MetadataException e) {
            assertTrue(
                "MetadataException caught trying to set values: "
                    + e.getMessage(), false);
        }

        // Now read all of them back
        assertEquals(deviceType.getId(), new Long(1));
        assertEquals(deviceType.getDescription(), "DeviceType description");
        assertEquals(deviceType.getName(), "deviceType name");
    }

    /**
     * This method checks to see if the toStringRepresentation method works
     * properly
     */
    public void testToStringRepresentation() {
        // Create the new device
        DeviceType deviceType = new DeviceType();

        // Set all the values
        deviceType.setId(new Long(1));
        try {
            deviceType.setDescription("DeviceType description");
            deviceType.setName("deviceType name");
        } catch (MetadataException e) {
            assertTrue(
                "MetadataException caught trying to set values: "
                    + e.getMessage(), false);
        }

        // Check that the string representations are equal
        String stringDevice = deviceType.toStringRepresentation(",");
        logger.debug("To string rep = " + stringDevice);
        String stringRep = "DeviceType," + "id=1," + "name=deviceType name,"
            + "description=DeviceType description";
        logger.debug("What it should be = " + stringRep);
        assertEquals(
            "The string represntation should match the set attributes",
            stringDevice, stringRep);
    }

    /**
     * This tests the method that sets the values from a string representation
     */
    public void testSetValuesFromStringRepresentation() {

        // Create the deviceType
        DeviceType deviceType = new DeviceType();

        // Create the string representation
        String stringRep = "DeviceType," + "id=1," + "name=deviceType name,"
            + "description=DeviceType description";

        try {
            deviceType.setValuesFromStringRepresentation(stringRep, ",");
        } catch (MetadataException e) {
            logger.error("MetadataException caught trying to set "
                + "values from string representation: " + e.getMessage());
            assertTrue("Metadata Exception caught: " + e.getMessage(), false);
        }
        // Now check that everything was set OK
        assertEquals(deviceType.getId(), new Long(1));
        assertEquals(deviceType.getDescription(), "DeviceType description");
        assertEquals(deviceType.getName(), "deviceType name");
    }

    /**
     * This method tests the equals method
     */
    public void testEqualsAndHashcode() {
        // Create the string representations
        String stringRep = "DeviceType," + "id=1," + "name=deviceType name,"
            + "description=Device description";
        String stringRepTwo = "DeviceType," + "id=1," + "name=deviceType name,"
            + "description=DeviceType description";

        DeviceType deviceTypeOne = new DeviceType();
        DeviceType deviceTypeTwo = new DeviceType();

        try {
            deviceTypeOne.setValuesFromStringRepresentation(stringRep, ",");
            deviceTypeTwo.setValuesFromStringRepresentation(stringRepTwo, ",");
        } catch (MetadataException e) {
            logger
                .error("MetadataException caught trying to create two deviceType objects");
            assertTrue(
                "MetadataException caught trying to screate two deviceType objects: "
                    + e.getMessage(), false);
        }

        assertTrue("The two deviceTypes should be equal (part one).",
            deviceTypeOne.equals(deviceTypeTwo));
        assertEquals("The two deviceTypes should be equal (part two).",
            deviceTypeOne, deviceTypeTwo);
        assertEquals("The two hashcodes should be equal (part two).",
            deviceTypeOne.hashCode(), deviceTypeTwo.hashCode());

        // Now change the ID of the second one and they should be equal
        deviceTypeTwo.setId(new Long(2));
        assertTrue("The two deviceTypes should be equal", deviceTypeOne
            .equals(deviceTypeTwo));
        assertTrue("The two deviceType hashcodes should be equal",
            deviceTypeOne.hashCode() == deviceTypeTwo.hashCode());

        // Now set the ID back, check equals again
        deviceTypeTwo.setId(new Long(1));
        assertEquals("The two deviceTypes should be equal after ID set back.",
            deviceTypeOne, deviceTypeTwo);
        assertEquals("The two hashcodes should be equal after ID set back.",
            deviceTypeOne.hashCode(), deviceTypeTwo.hashCode());

        // Set the name and the two should be different
        try {
            deviceTypeTwo.setName("new name");
        } catch (MetadataException e) {
            assertTrue(
                "MetadataException caught trying to set values: "
                    + e.getMessage(), false);
        }
        assertTrue("The two should not be equal after name change",
            !deviceTypeOne.equals(deviceTypeTwo));
        assertTrue(
            "The two deviceType hashcodes should not be equal after name change",
            deviceTypeOne.hashCode() != deviceTypeTwo.hashCode());

        // Set the name back and all should be well
        try {
            deviceTypeTwo.setName(deviceTypeOne.getName());
        } catch (MetadataException e) {
            assertTrue(
                "MetadataException caught trying to set values: "
                    + e.getMessage(), false);
        }
        assertTrue("The two should be equal again after name change back",
            deviceTypeOne.equals(deviceTypeTwo));
        assertEquals(
            "The two hashcodes should be equal again after name change back",
            deviceTypeOne.hashCode(), deviceTypeTwo.hashCode());

        // Change descriptions and the two should be equal
        try {
            deviceTypeTwo.setDescription("new description");
        } catch (MetadataException e) {
            assertTrue(
                "MetadataException caught trying to set values: "
                    + e.getMessage(), false);
        }
        assertTrue("The two should be equal after description change",
            deviceTypeOne.equals(deviceTypeTwo));
        assertEquals(
            "The two hashcodes should be equal even though description was changed",
            deviceTypeOne.hashCode(), deviceTypeTwo.hashCode());
    }

    /**
     * The logger for dumping information to
     */
    static Logger logger = Logger.getLogger(TestDeviceType.class);
}