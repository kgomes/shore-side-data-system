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
import moos.ssds.metadata.StandardVariable;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * This is the test class to test the StandardVariable class
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.8 $
 */
public class TestStandardVariable extends TestCase {

    /**
     * @param arg0
     */
    public TestStandardVariable(String arg0) {
        super(arg0);
    }

    protected void setUp() {
        BasicConfigurator.configure();
        logger.setLevel(Level.DEBUG);
        logger.addAppender(new ConsoleAppender(new PatternLayout(
            "%d %-5p [%c %M %L] %m%n")));
    }

    /**
     * This method checks the creation of a <code>StandardVariable</code>
     * object
     */
    public void testCreateStandardVariable() {
        // Create the new standardVariable
        StandardVariable standardVariable = new StandardVariable();

        // Set all the values
        standardVariable.setId(new Long(1));
        try {
            standardVariable.setName("StandardVariableOne");
            standardVariable.setDescription("StandardVariable one description");
            standardVariable
                .setReferenceScale("StandardVariable one reference scale");
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }

        // Now read all of them back
        assertEquals(standardVariable.getId(), new Long(1));
        assertEquals(standardVariable.getName(), "StandardVariableOne");
        assertEquals(standardVariable.getDescription(),
            "StandardVariable one description");
        assertEquals(standardVariable.getReferenceScale(),
            "StandardVariable one reference scale");
    }

    public void testRelationships() {
        // Test the standard unit/variable relationship
        // Create a StandardVariable
        StandardVariable standardVariable = new StandardVariable();

        // Set all the values
        standardVariable.setId(new Long(1));
        try {
            standardVariable.setName("StandardVariableOne");
            standardVariable.setDescription("StandardVariable one description");
            standardVariable
                .setReferenceScale("StandardVariable one reference scale");
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }

        // Create a StandardUnit
        StandardUnit standardUnit = new StandardUnit();
        StandardUnit standardUnitDup = new StandardUnit();
        StandardUnit standardUnitTwo = new StandardUnit();

        // Set all the values
        try {
            standardUnit.setId(new Long(1));
            standardUnit.setName("StandardUnitOne");
            standardUnit.setDescription("StandardUnit one description");
            standardUnit.setLongName("Standard Unit one long name");
            standardUnit.setSymbol("SUONE");

            standardUnitDup.setId(new Long(1));
            standardUnitDup.setName("StandardUnitOne");
            standardUnitDup.setDescription("StandardUnit one description");
            standardUnitDup.setLongName("Standard Unit one long name");
            standardUnitDup.setSymbol("SUONE");

            standardUnitTwo.setId(new Long(2));
            standardUnitTwo.setName("StandardUnitTwo");
            standardUnitTwo.setDescription("StandardUnit two description");
            standardUnitTwo.setLongName("Standard Unit two long name");
            standardUnitTwo.setSymbol("SUTWO");
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }

        // OK, first check to see that the standardVariables units collection is
        // not null, but empty
        assertNotNull(
            "The standardVariable's units collection should not be null",
            standardVariable.getStandardUnits());
        assertTrue("The standardVariable's units collection should be emtpy",
            standardVariable.getStandardUnits().size() == 0);

        // Now add the standardUnit by the add method
        standardVariable.addStandardUnit(standardUnit);
        assertTrue(
            "The standardVariable collection of standard units should have one element",
            standardVariable.getStandardUnits().size() == 1);
        assertTrue("standardUnit should be in the collection of standardUnits",
            standardVariable.getStandardUnits().contains(standardUnit));

        // Now try to add the duplicate standard unit
        standardVariable.addStandardUnit(standardUnitDup);

        // Nothing should have happened
        assertTrue(
            "The standardVariable collection of standard units should have one element",
            standardVariable.getStandardUnits().size() == 1);
        assertTrue("standardUnit should be in the collection of standardUnits",
            standardVariable.getStandardUnits().contains(standardUnit));

        // Now add a really different one
        standardVariable.addStandardUnit(standardUnitTwo);

        // Should have two of them now
        assertTrue(
            "The standardVariable collection of standard units should have two element",
            standardVariable.getStandardUnits().size() == 2);
        assertTrue("standardUnit should be in the collection of standardUnits",
            standardVariable.getStandardUnits().contains(standardUnit));
        assertTrue(
            "standardUnitTwo should be in the collection of standardUnits",
            standardVariable.getStandardUnits().contains(standardUnitTwo));

        // Now remove the first one
        standardVariable.removeStandardUnit(standardUnit);

        // Now should only find the first one
        assertTrue(
            "The standardVariable collection of standard units should have one element",
            standardVariable.getStandardUnits().size() == 1);
        assertTrue(
            "standardUnit should NOT be in the collection of standardUnits",
            !standardVariable.getStandardUnits().contains(standardUnit));
        assertTrue(
            "standardUnitTwo should be in the collection of standardUnits",
            standardVariable.getStandardUnits().contains(standardUnitTwo));

    }

    /**
     * This method checks to see if the toStringRepresentation method works
     * properly
     */
    public void testToStringRepresentation() {
        // Create the new standardVariable
        StandardVariable standardVariable = new StandardVariable();

        // Set all the values
        standardVariable.setId(new Long(1));
        try {
            standardVariable.setName("StandardVariableOne");
            standardVariable.setDescription("StandardVariable one description");
            standardVariable
                .setReferenceScale("StandardVariable one reference scale");
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }

        // Check that the string representations are equal
        String stringStandardVariable = standardVariable
            .toStringRepresentation(",");
        String stringRep = "StandardVariable," + "id=1,"
            + "name=StandardVariableOne," + "namespaceUriString=null,"
            + "description=StandardVariable one description,"
            + "referenceScale=StandardVariable one reference scale";
        assertEquals(
            "The string represntation should match the set attributes",
            stringStandardVariable, stringRep);

    }

    /**
     * This tests the method that sets the values from a string representation
     */
    public void testSetValuesFromStringRepresentation() {

        // Create the standardVariable
        StandardVariable standardVariable = new StandardVariable();

        // Create the string representation
        String stringRep = "StandardVariable," + "id=1,"
            + "name=StandardVariableOne,"
            + "description=StandardVariable one description,"
            + "referenceScale=StandardVariable one reference scale";

        try {
            standardVariable.setValuesFromStringRepresentation(stringRep, ",");
        } catch (MetadataException e) {
            logger.error("MetadataException caught trying to set "
                + "values from string representation: " + e.getMessage());
        }

        // Now check that everything was set OK
        assertEquals(standardVariable.getId(), new Long(1));
        assertEquals(standardVariable.getName(), "StandardVariableOne");
        assertEquals(standardVariable.getDescription(),
            "StandardVariable one description");
        assertEquals(standardVariable.getReferenceScale(),
            "StandardVariable one reference scale");
    }

    /**
     * This method tests the equals method
     */
    public void testEqualsAndHashCode() {
        // Create the string representation
        String stringRep = "StandardVariable," + "id=1,"
            + "name=StandardVariableOne,"
            + "description=StandardVariable one description,"
            + "namespaceUriString=StandardVariable one namespaceUriString";
        String stringRepTwo = "StandardVariable," + "id=1,"
            + "name=StandardVariableOne,"
            + "description=StandardVariable one description,"
            + "namespaceUriString=StandardVariable one namespaceUriString";

        StandardVariable standardVariableOne = new StandardVariable();
        StandardVariable standardVariableTwo = new StandardVariable();

        try {
            standardVariableOne.setValuesFromStringRepresentation(stringRep,
                ",");
            standardVariableTwo.setValuesFromStringRepresentation(stringRepTwo,
                ",");
        } catch (MetadataException e) {
            logger
                .error("MetadataException caught trying to create two standardVariable objects");
        }

        assertTrue("The two standardVariables should be equal (part one).",
            standardVariableOne.equals(standardVariableTwo));
        assertEquals("The two standardVariables should be equal (part two).",
            standardVariableOne, standardVariableTwo);
        assertTrue("The two hashcodes should be equal", standardVariableOne
            .hashCode() == standardVariableTwo.hashCode());

        // Now change the ID of the second one and they should be equal
        standardVariableTwo.setId(new Long(2));
        assertTrue("The two standardVariables should be equal",
            standardVariableOne.equals(standardVariableTwo));
        assertTrue("The two hashcodes should be equal", standardVariableOne
            .hashCode() == standardVariableTwo.hashCode());

        // Now set the ID back, check equals again
        standardVariableTwo.setId(new Long(1));
        assertEquals(
            "The two standardVariables should be equal after ID set back.",
            standardVariableOne, standardVariableTwo);
        assertTrue("The two hashcodes should be equal", standardVariableOne
            .hashCode() == standardVariableTwo.hashCode());

        // Now set the name and they should be different
        try {
            standardVariableTwo.setName("StandardVariableTwo");
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }
        assertTrue("The two standardVariable should not be equal",
            !standardVariableOne.equals(standardVariableTwo));
        assertTrue("The two hashcodes should NOT be equal", standardVariableOne
            .hashCode() != standardVariableTwo.hashCode());

        // Now set the name back and they should be the same
        try {
            standardVariableTwo.setName("StandardVariableOne");
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }
        assertTrue("The two standardVariable should be equal",
            standardVariableOne.equals(standardVariableTwo));
        assertTrue("The two hashcodes should be equal", standardVariableOne
            .hashCode() == standardVariableTwo.hashCode());

        // Now set the ref scale and they should be different
        try {
            standardVariableTwo.setNamespaceUriString("nsTwo");
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }
        assertTrue("The two standardVariable should not be equal",
            !standardVariableOne.equals(standardVariableTwo));
        assertTrue("The two hashcodes should NOT be equal", standardVariableOne
            .hashCode() != standardVariableTwo.hashCode());

        // Now set the ref of one to match and should be equal again
        try {
            standardVariableOne.setNamespaceUriString("nsTwo");
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }
        assertTrue("The two standardVariables should be equal after "
            + "setting one refscale to match two's", standardVariableOne
            .equals(standardVariableTwo));
        assertTrue("The two hashcodes should be equal", standardVariableOne
            .hashCode() == standardVariableTwo.hashCode());

        // Change the description and they should still be equal
        try {
            standardVariableTwo.setDescription("blah blah");
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }
        assertEquals(
            "The two standardVariables should be equal after changing only description",
            standardVariableOne, standardVariableTwo);
        assertTrue("The two hashcodes should be equal", standardVariableOne
            .hashCode() == standardVariableTwo.hashCode());
    }

    /**
     * The logger for dumping information to
     */
    static Logger logger = Logger.getLogger(TestStandardVariable.class);
}