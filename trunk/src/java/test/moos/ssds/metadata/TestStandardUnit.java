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
import moos.ssds.metadata.StandardUnit;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * This is the test class to test the StandardUnit class
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.7 $
 */
public class TestStandardUnit extends TestCase {

    /**
     * @param arg0
     */
    public TestStandardUnit(String arg0) {
        super(arg0);
    }

    protected void setUp() {
        BasicConfigurator.configure();
        logger.setLevel(Level.DEBUG);
        logger.addAppender(new ConsoleAppender(new PatternLayout(
            "%d %-5p [%c %M %L] %m%n")));
    }

    /**
     * This method checks the creation of a <code>StandardUnit</code> object
     */
    public void testCreateStandardUnit() {
        // Create the new standardUnit
        StandardUnit standardUnit = new StandardUnit();

        // Set all the values
        standardUnit.setId(new Long(1));
        try {
            standardUnit.setName("StandardUnitOne");
            standardUnit.setDescription("StandardUnit one description");
            standardUnit.setLongName("Standard Unit one long name");
            standardUnit.setSymbol("SUONE");
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }

        // Now read all of them back
        assertEquals(standardUnit.getId(), new Long(1));
        assertEquals(standardUnit.getName(), "StandardUnitOne");
        assertEquals(standardUnit.getDescription(),
            "StandardUnit one description");
        assertEquals(standardUnit.getLongName(), "Standard Unit one long name");
        assertEquals(standardUnit.getSymbol(), "SUONE");
    }

    /**
     * This method checks to see if the toStringRepresentation method works
     * properly
     */
    public void testToStringRepresentation() {
        // Create the new standardUnit
        StandardUnit standardUnit = new StandardUnit();

        // Set all the values
        // Set all the values
        standardUnit.setId(new Long(1));
        try {
            standardUnit.setName("StandardUnitOne");
            standardUnit.setDescription("StandardUnit one description");
            standardUnit.setLongName("Standard Unit one long name");
            standardUnit.setSymbol("SUONE");
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }

        // Check that the string representations are equal
        String stringStandardUnit = standardUnit.toStringRepresentation(",");
        String stringRep = "StandardUnit," + "id=1," + "name=StandardUnitOne,"
            + "description=StandardUnit one description,"
            + "longName=Standard Unit one long name," + "symbol=SUONE";
        assertEquals(
            "The string represntation should match the set attributes",
            stringStandardUnit, stringRep);

    }

    /**
     * This tests the method that sets the values from a string representation
     */
    public void testSetValuesFromStringRepresentation() {

        // Create the standardUnit
        StandardUnit standardUnit = new StandardUnit();

        // Create the string representation
        String stringRep = "StandardUnit," + "id=1," + "name=StandardUnitOne,"
            + "description=StandardUnit one description,"
            + "longName=Standard Unit one long name," + "symbol=SUONE";

        try {
            standardUnit.setValuesFromStringRepresentation(stringRep, ",");
        } catch (MetadataException e) {
            logger.error("MetadataException caught trying to set "
                + "values from string representation: " + e.getMessage());
        }

        // Now check that everything was set OK
        assertEquals(standardUnit.getId(), new Long(1));
        assertEquals(standardUnit.getName(), "StandardUnitOne");
        assertEquals(standardUnit.getDescription(),
            "StandardUnit one description");
        assertEquals(standardUnit.getLongName(), "Standard Unit one long name");
        assertEquals(standardUnit.getSymbol(), "SUONE");
    }

    /**
     * This method tests the equals method
     */
    public void testEquals() {
        // Create the string representation
        String stringRep = "StandardUnit," + "id=1," + "name=StandardUnitOne,"
            + "description=StandardUnit one description,"
            + "longName=Standard Unit one long name," + "symbol=SUONE";
        String stringRepTwo = "StandardUnit," + "id=1,"
            + "name=StandardUnitOne,"
            + "description=StandardUnit one description,"
            + "longName=Standard Unit one long name," + "symbol=SUONE";

        StandardUnit standardUnitOne = new StandardUnit();
        StandardUnit standardUnitTwo = new StandardUnit();

        try {
            standardUnitOne.setValuesFromStringRepresentation(stringRep, ",");
            standardUnitTwo
                .setValuesFromStringRepresentation(stringRepTwo, ",");
        } catch (MetadataException e) {
            logger
                .error("MetadataException caught trying to create two standardUnit objects");
        }

        assertTrue("The two standardUnits should be equal (part one).",
            standardUnitOne.equals(standardUnitTwo));
        assertEquals("The two standardUnits should be equal (part two).",
            standardUnitOne, standardUnitTwo);

        // Now change the ID of the second one and they should still be equal
        standardUnitTwo.setId(new Long(2));
        assertTrue("The two standardUnit should be equal", standardUnitOne
            .equals(standardUnitTwo));

        // Now set the ID back, check equals again
        standardUnitTwo.setId(new Long(1));
        assertEquals(
            "The two standardUnits should be equal after ID set back.",
            standardUnitOne, standardUnitTwo);

        // Now set the name and they should be different
        try {
            standardUnitTwo.setName("StandardUnitTwo");
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }
        assertTrue("The two standardUnit should not be equal", !standardUnitOne
            .equals(standardUnitTwo));

        // Now set it back and change all the non-business key values. The
        // results should be equals
        try {
            standardUnitTwo.setName("StandardUnitOne");
            standardUnitTwo.setDescription("blah blah");
            standardUnitTwo.setLongName("blah blah long name");
            standardUnitTwo.setSymbol("blah blah symbol");
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }
        assertEquals(
            "The two standardUnits should be equal after ID set back.",
            standardUnitOne, standardUnitTwo);
    }

    /**
     * This method tests the hashCode method
     */
    public void testHashCode() {
        // Create the string representation
        String stringRep = "StandardUnit," + "id=1," + "name=StandardUnitOne,"
            + "description=StandardUnit one description,"
            + "longName=Standard Unit one long name," + "symbol=SUONE";
        String stringRepTwo = "StandardUnit," + "id=1,"
            + "name=StandardUnitOne,"
            + "description=StandardUnit one description,"
            + "longName=Standard Unit one long name," + "symbol=SUONE";

        StandardUnit standardUnitOne = new StandardUnit();
        StandardUnit standardUnitTwo = new StandardUnit();

        try {
            standardUnitOne.setValuesFromStringRepresentation(stringRep, ",");
            standardUnitTwo
                .setValuesFromStringRepresentation(stringRepTwo, ",");
        } catch (MetadataException e) {
            logger
                .error("MetadataException caught trying to create two standardUnit objects: "
                    + e.getMessage());
        }

        assertTrue("The two hashCodes should be equal (part one).",
            standardUnitOne.hashCode() == standardUnitTwo.hashCode());
        assertEquals("The two hashCodes should be equal (part two).",
            standardUnitOne.hashCode(), standardUnitTwo.hashCode());

        // Now change the ID of the second one and they should not be equal
        standardUnitTwo.setId(new Long(2));
        assertTrue("The two hashCodes should be equal", standardUnitOne
            .hashCode() == standardUnitTwo.hashCode());

        // Now set the ID back, check equals again
        standardUnitTwo.setId(new Long(1));
        assertEquals("The two hashCodes should be equal after ID set back.",
            standardUnitOne.hashCode(), standardUnitTwo.hashCode());

        // Now set the name and they should be different
        try {
            standardUnitTwo.setName("StandardUnitTwo");
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }
        assertTrue("The two hashCodes should not be equal after name change",
            standardUnitOne.hashCode() != standardUnitTwo.hashCode());

        // Now set it back and change all the non-business key values. The
        // results should be equals
        try {
            standardUnitTwo.setName("StandardUnitOne");
            standardUnitTwo.setDescription("blah blah");
            standardUnitTwo.setLongName("blah blah long name");
            standardUnitTwo.setSymbol("blah blah symbol");
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }
        assertEquals("The two hashCodes should be equal after ID and name same"
            + ", but different business keys.", standardUnitOne.hashCode(),
            standardUnitTwo.hashCode());
    }

    /**
     * The logger for dumping information to
     */
    static Logger logger = Logger.getLogger(TestStandardUnit.class);
}