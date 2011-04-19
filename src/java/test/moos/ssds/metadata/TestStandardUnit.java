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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import junit.framework.TestCase;
import moos.ssds.metadata.StandardUnit;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.ObjectBuilder;
import moos.ssds.metadata.util.XmlBuilder;

import org.apache.log4j.Logger;

/**
 * This is the test class to test the StandardUnit class
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.7 $
 */
public class TestStandardUnit extends TestCase {

	/**
	 * The logger for dumping information to
	 */
	static Logger logger = Logger.getLogger(TestStandardUnit.class);

	/**
	 * @param arg0
	 */
	public TestStandardUnit(String arg0) {
		super(arg0);
	}

	protected void setUp() {
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
			assertTrue(
					"MetadataException caught trying to set values: "
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
			assertTrue(
					"MetadataException caught trying to set values: "
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
			logger.error("MetadataException caught trying to create two standardUnit objects");
		}

		assertTrue("The two standardUnits should be equal (part one).",
				standardUnitOne.equals(standardUnitTwo));
		assertEquals("The two standardUnits should be equal (part two).",
				standardUnitOne, standardUnitTwo);

		// Now change the ID of the second one and they should still be equal
		standardUnitTwo.setId(new Long(2));
		assertTrue("The two standardUnit should be equal",
				standardUnitOne.equals(standardUnitTwo));

		// Now set the ID back, check equals again
		standardUnitTwo.setId(new Long(1));
		assertEquals(
				"The two standardUnits should be equal after ID set back.",
				standardUnitOne, standardUnitTwo);

		// Now set the name and they should be different
		try {
			standardUnitTwo.setName("StandardUnitTwo");
		} catch (MetadataException e) {
			assertTrue(
					"MetadataException caught trying to set values: "
							+ e.getMessage(), false);
		}
		assertTrue("The two standardUnit should not be equal",
				!standardUnitOne.equals(standardUnitTwo));

		// Now set it back and change all the non-business key values. The
		// results should be equals
		try {
			standardUnitTwo.setName("StandardUnitOne");
			standardUnitTwo.setDescription("blah blah");
			standardUnitTwo.setLongName("blah blah long name");
			standardUnitTwo.setSymbol("blah blah symbol");
		} catch (MetadataException e) {
			assertTrue(
					"MetadataException caught trying to set values: "
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
			logger.error("MetadataException caught trying to create two standardUnit objects: "
					+ e.getMessage());
		}

		assertTrue("The two hashCodes should be equal (part one).",
				standardUnitOne.hashCode() == standardUnitTwo.hashCode());
		assertEquals("The two hashCodes should be equal (part two).",
				standardUnitOne.hashCode(), standardUnitTwo.hashCode());

		// Now change the ID of the second one and they should not be equal
		standardUnitTwo.setId(new Long(2));
		assertTrue("The two hashCodes should be equal",
				standardUnitOne.hashCode() == standardUnitTwo.hashCode());

		// Now set the ID back, check equals again
		standardUnitTwo.setId(new Long(1));
		assertEquals("The two hashCodes should be equal after ID set back.",
				standardUnitOne.hashCode(), standardUnitTwo.hashCode());

		// Now set the name and they should be different
		try {
			standardUnitTwo.setName("StandardUnitTwo");
		} catch (MetadataException e) {
			assertTrue(
					"MetadataException caught trying to set values: "
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
			assertTrue(
					"MetadataException caught trying to set values: "
							+ e.getMessage(), false);
		}
		assertEquals("The two hashCodes should be equal after ID and name same"
				+ ", but different business keys.", standardUnitOne.hashCode(),
				standardUnitTwo.hashCode());
	}

	/**
	 * This test takes a StandardUnit defined in XML, converts it to an object,
	 * checks the attributes, converts changes some attributes and converts back
	 * to XML.
	 */
	public void testStandardUnitXMLBinding() {

		// Grab the file that has the XML in it
		File standardUnitXMLFile = new File("src" + File.separator
				+ "resources" + File.separator + "test" + File.separator
				+ "xml" + File.separator + "StandardUnit.xml");
		if (!standardUnitXMLFile.exists())
			assertTrue("Could not find StandardUnit.xml file for testing.",
					false);
		logger.debug("Will read standardUnit XML from "
				+ standardUnitXMLFile.getAbsolutePath());

		// Create the object builder
		ObjectBuilder objectBuilder = null;
		try {
			objectBuilder = new ObjectBuilder(standardUnitXMLFile.toURI()
					.toURL());
		} catch (MalformedURLException e) {
			logger.error("MalformedURLException caught trying to create object builder: "
					+ e.getMessage());
			assertTrue(
					"MalformedURLException caught trying to create object builder: "
							+ e.getMessage(), false);
		}

		// Unmarshal XML to objects
		try {
			objectBuilder.unmarshal();
		} catch (Exception e) {
			logger.error("Exception caught trying to unmarshal XML to objects: "
					+ e.getMessage());
			assertTrue("Exception caught trying to unmarshal XML to objects: "
					+ e.getMessage(), false);
		}

		// The top level object should be a Event
		Object unmarshalledObject = objectBuilder.listAll().iterator().next();
		assertNotNull("Unmarshalled object should not be null",
				unmarshalledObject);
		assertTrue("Unmarshalled object should be a StandardUnit",
				unmarshalledObject instanceof StandardUnit);

		// Cast it
		StandardUnit testStandardUnit = (StandardUnit) unmarshalledObject;
		assertEquals("ID should be 1", testStandardUnit.getId().longValue(),
				Long.parseLong("1"));
		assertEquals("StandardUnit name should match",
				testStandardUnit.getName(), "Test StandardUnit");
		assertEquals("Description should match",
				testStandardUnit.getDescription(),
				"Test StandardUnit Description");
		assertEquals("LongName should match", testStandardUnit.getLongName(),
				"Test StandardUnit LongName");
		assertEquals("Symbol should match", testStandardUnit.getSymbol(),
				"Test StandardUnit Symbol");

		// Now let's change the attributes
		try {
			testStandardUnit.setName("Changed Test StandardUnit");
			testStandardUnit
					.setDescription("Changed Test StandardUnit Description");
			testStandardUnit.setLongName("Changed Test StandardUnit LongName");
			testStandardUnit.setSymbol("Changed Test StandardUnit Symbol");
			logger.debug("Changed attributes " + "and will marshall to XML");
		} catch (MetadataException e) {
			assertTrue("Error while changing attributes: " + e.getMessage(),
					false);
		}

		// Create an XML builder to marshall back out the XML
		XmlBuilder xmlBuilder = new XmlBuilder();
		xmlBuilder.add(testStandardUnit);
		xmlBuilder.marshal();

		// Now test the xml
		try {
			StringWriter stringWriter = new StringWriter();
			stringWriter.append(xmlBuilder.toFormattedXML());

			// Now make sure the resulting string contains all the
			// updates I did
			logger.debug("Marshalled XML after change: "
					+ stringWriter.toString());
			// Now test the string
			assertTrue("Marshalled XML contain changed name", stringWriter
					.toString().contains("Changed Test StandardUnit"));
			assertTrue(
					"Marshalled XML contain changed description",
					stringWriter.toString().contains(
							"Changed Test StandardUnit Description"));
			assertTrue("Marshalled XML contain changed longname", stringWriter
					.toString().contains("Changed Test StandardUnit LongName"));
			assertTrue("Marshalled XML contain changed symbol", stringWriter
					.toString().contains("Changed Test StandardUnit Symbol"));
		} catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException caught while converting to XML:"
					+ e.getMessage());
			assertTrue(
					"UnsupportedEncodingException caught while converting to XML: "
							+ e.getMessage(), false);
		} catch (IOException e) {
			logger.error("IOException caught while converting to XML:"
					+ e.getMessage());
			assertTrue(
					"IOException caught while converting to XML: "
							+ e.getMessage(), false);
		}
	}
}