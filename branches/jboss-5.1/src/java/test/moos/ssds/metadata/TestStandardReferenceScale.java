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
import moos.ssds.metadata.StandardReferenceScale;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.ObjectBuilder;
import moos.ssds.metadata.util.XmlBuilder;

import org.apache.log4j.Logger;

/**
 * This is the test class to test the StandardKeyword class
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.7 $
 */
public class TestStandardReferenceScale extends TestCase {

	/**
	 * The logger for dumping information to
	 */
	static Logger logger = Logger.getLogger(TestStandardReferenceScale.class);

	/**
	 * @param arg0
	 */
	public TestStandardReferenceScale(String arg0) {
		super(arg0);
	}

	protected void setUp() {
	}

	/**
	 * This method checks the creation of a <code>StandardReferenceScale</code>
	 * object
	 */
	public void testCreateStandardReferenceScale() {
		// Create the new StandardReferenceScale
		StandardReferenceScale standardReferenceScale = new StandardReferenceScale();

		// Set all the values
		standardReferenceScale.setId(new Long(1));
		try {
			standardReferenceScale.setName("StandardReferenceScaleOne");
			standardReferenceScale
					.setDescription("StandardReferenceScale one description");
		} catch (MetadataException e) {
			assertTrue(
					"MetadataException caught trying to set values: "
							+ e.getMessage(), false);
		}

		// Now read all of them back
		assertEquals(standardReferenceScale.getId(), new Long(1));
		assertEquals(standardReferenceScale.getName(),
				"StandardReferenceScaleOne");
		assertEquals(standardReferenceScale.getDescription(),
				"StandardReferenceScale one description");
	}

	/**
	 * This method checks to see if the toStringRepresentation method works
	 * properly
	 */
	public void testToStringRepresentation() {
		// Create the new StandardReferenceScale
		StandardReferenceScale standardReferenceScale = new StandardReferenceScale();

		// Set all the values
		standardReferenceScale.setId(new Long(1));
		try {
			standardReferenceScale.setName("StandardReferenceScaleOne");
			standardReferenceScale
					.setDescription("StandardReferenceScale one description");
		} catch (MetadataException e) {
			assertTrue(
					"MetadataException caught trying to set values: "
							+ e.getMessage(), false);
		}

		// Check that the string representations are equal
		String stringStandardReferenceScale = standardReferenceScale
				.toStringRepresentation(",");
		String stringRep = "StandardReferenceScale," + "id=1,"
				+ "name=StandardReferenceScaleOne,"
				+ "description=StandardReferenceScale one description";
		assertEquals(
				"The string represntation should match the set attributes",
				stringStandardReferenceScale, stringRep);

	}

	/**
	 * This tests the method that sets the values from a string representation
	 */
	public void testSetValuesFromStringRepresentation() {

		// Create the StandardReferenceScale
		StandardReferenceScale standardReferenceScale = new StandardReferenceScale();

		// Create the string representation
		String stringRep = "StandardReferenceScale," + "id=1,"
				+ "name=StandardReferenceScaleOne,"
				+ "description=StandardReferenceScale one description";

		try {
			standardReferenceScale.setValuesFromStringRepresentation(stringRep,
					",");
		} catch (MetadataException e) {
			logger.error("MetadataException caught trying to set "
					+ "values from string representation: " + e.getMessage());
		}

		// Now check that everything was set OK
		assertEquals(standardReferenceScale.getId(), new Long(1));
		assertEquals(standardReferenceScale.getName(),
				"StandardReferenceScaleOne");
		assertEquals(standardReferenceScale.getDescription(),
				"StandardReferenceScale one description");
	}

	/**
	 * This method tests the equals method
	 */
	public void testEquals() {
		// Create the string representation
		String stringRep = "StandardReferenceScale," + "id=1,"
				+ "name=StandardReferenceScaleOne,"
				+ "description=StandardReferenceScale one description";
		String stringRepTwo = "StandardReferenceScale," + "id=1,"
				+ "name=StandardReferenceScaleOne,"
				+ "description=StandardReferenceScale one description";

		StandardReferenceScale standardReferenceScaleOne = new StandardReferenceScale();
		StandardReferenceScale standardReferenceScaleTwo = new StandardReferenceScale();

		try {
			standardReferenceScaleOne.setValuesFromStringRepresentation(
					stringRep, ",");
			standardReferenceScaleTwo.setValuesFromStringRepresentation(
					stringRepTwo, ",");
		} catch (MetadataException e) {
			logger.error("MetadataException caught trying to create two StandardReferenceScale objects");
		}

		assertTrue(
				"The two StandardReferenceScales should be equal (part one).",
				standardReferenceScaleOne.equals(standardReferenceScaleTwo));
		assertEquals(
				"The two StandardReferenceScales should be equal (part two).",
				standardReferenceScaleOne, standardReferenceScaleTwo);

		// Now change the ID of the second one and they should still be equal
		standardReferenceScaleTwo.setId(new Long(2));
		assertTrue("The two StandardReferenceScale should be equal",
				standardReferenceScaleOne.equals(standardReferenceScaleTwo));

		// Now set the ID back, check equals again
		standardReferenceScaleTwo.setId(new Long(1));
		assertEquals(
				"The two StandardReferenceScales should be equal after ID set back.",
				standardReferenceScaleOne, standardReferenceScaleTwo);

		// Now set the name and they should be different
		try {
			standardReferenceScaleTwo.setName("StandardReferenceScaleTwo");
		} catch (MetadataException e) {
			assertTrue(
					"MetadataException caught trying to set values: "
							+ e.getMessage(), false);
		}
		assertTrue("The two StandardReferenceScale should not be equal",
				!standardReferenceScaleOne.equals(standardReferenceScaleTwo));

		// Now set it back and change all the non-business key values. The
		// results should be equals
		try {
			standardReferenceScaleTwo.setName("StandardReferenceScaleOne");
			standardReferenceScaleTwo.setDescription("blah blah");
		} catch (MetadataException e) {
			assertTrue(
					"MetadataException caught trying to set values: "
							+ e.getMessage(), false);
		}
		assertEquals(
				"The two StandardReferenceScales should be equal after ID set back.",
				standardReferenceScaleOne, standardReferenceScaleTwo);
	}

	/**
	 * This method tests the hashCode method
	 */
	public void testHashCode() {
		// Create the string representation
		String stringRep = "StandardReferenceScale," + "id=1,"
				+ "name=StandardReferenceScaleOne,"
				+ "description=StandardReferenceScale one description";
		String stringRepTwo = "StandardReferenceScale," + "id=1,"
				+ "name=StandardReferenceScaleOne,"
				+ "description=StandardReferenceScale one description";

		StandardReferenceScale standardReferenceScaleOne = new StandardReferenceScale();
		StandardReferenceScale standardReferenceScaleTwo = new StandardReferenceScale();

		try {
			standardReferenceScaleOne.setValuesFromStringRepresentation(
					stringRep, ",");
			standardReferenceScaleTwo.setValuesFromStringRepresentation(
					stringRepTwo, ",");
		} catch (MetadataException e) {
			logger.error("MetadataException caught trying to create two StandardReferenceScale objects: "
					+ e.getMessage());
		}

		assertTrue(
				"The two hashCodes should be equal (part one).",
				standardReferenceScaleOne.hashCode() == standardReferenceScaleTwo
						.hashCode());
		assertEquals("The two hashCodes should be equal (part two).",
				standardReferenceScaleOne.hashCode(),
				standardReferenceScaleTwo.hashCode());

		// Now change the ID of the second one and they should not be equal
		standardReferenceScaleTwo.setId(new Long(2));
		assertTrue(
				"The two hashCodes should be equal",
				standardReferenceScaleOne.hashCode() == standardReferenceScaleTwo
						.hashCode());

		// Now set the ID back, check equals again
		standardReferenceScaleTwo.setId(new Long(1));
		assertEquals("The two hashCodes should be equal after ID set back.",
				standardReferenceScaleOne.hashCode(),
				standardReferenceScaleTwo.hashCode());

		// Now set the name and they should be different
		try {
			standardReferenceScaleTwo.setName("StandardReferenceScaleTwo");
		} catch (MetadataException e) {
			assertTrue(
					"MetadataException caught trying to set values: "
							+ e.getMessage(), false);
		}
		assertTrue(
				"The two hashCodes should not be equal after name change",
				standardReferenceScaleOne.hashCode() != standardReferenceScaleTwo
						.hashCode());

		// Now set it back and change all the non-business key values. The
		// results should be equals
		try {
			standardReferenceScaleTwo.setName("StandardReferenceScaleOne");
			standardReferenceScaleTwo.setDescription("blah blah");
		} catch (MetadataException e) {
			assertTrue(
					"MetadataException caught trying to set values: "
							+ e.getMessage(), false);
		}
		assertEquals("The two hashCodes should be equal after ID and name same"
				+ ", but different business keys.",
				standardReferenceScaleOne.hashCode(),
				standardReferenceScaleTwo.hashCode());
	}

	/**
	 * This test takes a StandardReferenceScale defined in XML, converts it to
	 * an object, checks the attributes, converts changes some attributes and
	 * converts back to XML.
	 */
	public void testStandardReferenceScaleXMLBinding() {

		// Grab the file that has the XML in it
		File standardReferenceScaleXMLFile = new File("src" + File.separator
				+ "resources" + File.separator + "test" + File.separator
				+ "xml" + File.separator + "StandardReferenceScale.xml");
		if (!standardReferenceScaleXMLFile.exists())
			assertTrue(
					"Could not find StandardReferenceScale.xml file for testing.",
					false);
		logger.debug("Will read StandardReferenceScale XML from "
				+ standardReferenceScaleXMLFile.getAbsolutePath());

		// Create the object builder
		ObjectBuilder objectBuilder = null;
		try {
			objectBuilder = new ObjectBuilder(standardReferenceScaleXMLFile
					.toURI().toURL());
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
		assertTrue("Unmarshalled object should be a StandardReferenceScale",
				unmarshalledObject instanceof StandardReferenceScale);

		// Cast it
		StandardReferenceScale testStandardReferenceScale = (StandardReferenceScale) unmarshalledObject;
		assertEquals("ID should be 1", testStandardReferenceScale.getId()
				.longValue(), Long.parseLong("1"));
		assertEquals("StandardReferenceScale name should match",
				testStandardReferenceScale.getName(),
				"Test StandardReferenceScale");
		assertEquals("Descripton should match",
				testStandardReferenceScale.getDescription(),
				"Test StandardReferenceScale Description");

		// Now let's change the attributes
		try {
			testStandardReferenceScale
					.setName("Changed Test StandardReferenceScale");
			testStandardReferenceScale
					.setDescription("Changed Test StandardReferenceScale Description");
			logger.debug("Changed name and description "
					+ "and will marshall to XML");
		} catch (MetadataException e) {
			assertTrue("Error while changing attributes: " + e.getMessage(),
					false);
		}

		// Create an XML builder to marshall back out the XML
		XmlBuilder xmlBuilder = new XmlBuilder();
		xmlBuilder.add(testStandardReferenceScale);
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
					.toString().contains("Changed Test StandardReferenceScale"));
			assertTrue(
					"Marshalled XML contain changed description",
					stringWriter.toString().contains(
							"Changed Test StandardReferenceScale Description"));
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