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
import moos.ssds.metadata.StandardDomain;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.ObjectBuilder;
import moos.ssds.metadata.util.XmlBuilder;

import org.apache.log4j.Logger;

/**
 * This is the test class to test the StandardDomain class
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.7 $
 */
public class TestStandardDomain extends TestCase {

	/**
	 * The logger for dumping information to
	 */
	static Logger logger = Logger.getLogger(TestStandardDomain.class);

	/**
	 * @param arg0
	 */
	public TestStandardDomain(String arg0) {
		super(arg0);
	}

	protected void setUp() {
	}

	/**
	 * This method checks the creation of a <code>StandardDomain</code> object
	 */
	public void testCreateStandardDomain() {
		// Create the new StandardDomain
		StandardDomain standardDomain = new StandardDomain();

		// Set all the values
		standardDomain.setId(new Long(1));
		try {
			standardDomain.setName("StandardDomainOne");
			standardDomain.setDescription("StandardDomain one description");
		} catch (MetadataException e) {
			assertTrue(
					"MetadataException caught trying to set values: "
							+ e.getMessage(), false);
		}

		// Now read all of them back
		assertEquals(standardDomain.getId(), new Long(1));
		assertEquals(standardDomain.getName(), "StandardDomainOne");
		assertEquals(standardDomain.getDescription(),
				"StandardDomain one description");
	}

	/**
	 * This method checks to see if the toStringRepresentation method works
	 * properly
	 */
	public void testToStringRepresentation() {
		// Create the new StandardDomain
		StandardDomain standardDomain = new StandardDomain();

		// Set all the values
		standardDomain.setId(new Long(1));
		try {
			standardDomain.setName("StandardDomainOne");
			standardDomain.setDescription("StandardDomain one description");
		} catch (MetadataException e) {
			assertTrue(
					"MetadataException caught trying to set values: "
							+ e.getMessage(), false);
		}

		// Check that the string representations are equal
		String stringStandardDomain = standardDomain
				.toStringRepresentation(",");
		String stringRep = "StandardDomain," + "id=1,"
				+ "name=StandardDomainOne,"
				+ "description=StandardDomain one description";
		assertEquals(
				"The string represntation should match the set attributes",
				stringStandardDomain, stringRep);

	}

	/**
	 * This tests the method that sets the values from a string representation
	 */
	public void testSetValuesFromStringRepresentation() {

		// Create the StandardDomain
		StandardDomain standardDomain = new StandardDomain();

		// Create the string representation
		String stringRep = "StandardDomain," + "id=1,"
				+ "name=StandardDomainOne,"
				+ "description=StandardDomain one description";

		try {
			standardDomain.setValuesFromStringRepresentation(stringRep, ",");
		} catch (MetadataException e) {
			logger.error("MetadataException caught trying to set "
					+ "values from string representation: " + e.getMessage());
		}

		// Now check that everything was set OK
		assertEquals(standardDomain.getId(), new Long(1));
		assertEquals(standardDomain.getName(), "StandardDomainOne");
		assertEquals(standardDomain.getDescription(),
				"StandardDomain one description");
	}

	/**
	 * This method tests the equals method
	 */
	public void testEquals() {
		// Create the string representation
		String stringRep = "StandardDomain," + "id=1,"
				+ "name=StandardDomainOne,"
				+ "description=StandardDomain one description";
		String stringRepTwo = "StandardDomain," + "id=1,"
				+ "name=StandardDomainOne,"
				+ "description=StandardDomain one description";

		StandardDomain standardDomainOne = new StandardDomain();
		StandardDomain standardDomainTwo = new StandardDomain();

		try {
			standardDomainOne.setValuesFromStringRepresentation(stringRep, ",");
			standardDomainTwo.setValuesFromStringRepresentation(stringRepTwo,
					",");
		} catch (MetadataException e) {
			logger.error("MetadataException caught trying to create two StandardDomain objects");
		}

		assertTrue("The two StandardDomains should be equal (part one).",
				standardDomainOne.equals(standardDomainTwo));
		assertEquals("The two StandardDomains should be equal (part two).",
				standardDomainOne, standardDomainTwo);

		// Now change the ID of the second one and they should still be equal
		standardDomainTwo.setId(new Long(2));
		assertTrue("The two StandardDomain should be equal",
				standardDomainOne.equals(standardDomainTwo));

		// Now set the ID back, check equals again
		standardDomainTwo.setId(new Long(1));
		assertEquals(
				"The two StandardDomains should be equal after ID set back.",
				standardDomainOne, standardDomainTwo);

		// Now set the name and they should be different
		try {
			standardDomainTwo.setName("StandardDomainTwo");
		} catch (MetadataException e) {
			assertTrue(
					"MetadataException caught trying to set values: "
							+ e.getMessage(), false);
		}
		assertTrue("The two StandardDomain should not be equal",
				!standardDomainOne.equals(standardDomainTwo));

		// Now set it back and change all the non-business key values. The
		// results should be equals
		try {
			standardDomainTwo.setName("StandardDomainOne");
			standardDomainTwo.setDescription("blah blah");
		} catch (MetadataException e) {
			assertTrue(
					"MetadataException caught trying to set values: "
							+ e.getMessage(), false);
		}
		assertEquals(
				"The two StandardDomains should be equal after ID set back.",
				standardDomainOne, standardDomainTwo);
	}

	/**
	 * This method tests the hashCode method
	 */
	public void testHashCode() {
		// Create the string representation
		String stringRep = "StandardDomain," + "id=1,"
				+ "name=StandardDomainOne,"
				+ "description=StandardDomain one description";
		String stringRepTwo = "StandardDomain," + "id=1,"
				+ "name=StandardDomainOne,"
				+ "description=StandardDomain one description";

		StandardDomain standardDomainOne = new StandardDomain();
		StandardDomain standardDomainTwo = new StandardDomain();

		try {
			standardDomainOne.setValuesFromStringRepresentation(stringRep, ",");
			standardDomainTwo.setValuesFromStringRepresentation(stringRepTwo,
					",");
		} catch (MetadataException e) {
			logger.error("MetadataException caught trying to create two StandardDomain objects: "
					+ e.getMessage());
		}

		assertTrue("The two hashCodes should be equal (part one).",
				standardDomainOne.hashCode() == standardDomainTwo.hashCode());
		assertEquals("The two hashCodes should be equal (part two).",
				standardDomainOne.hashCode(), standardDomainTwo.hashCode());

		// Now change the ID of the second one and they should not be equal
		standardDomainTwo.setId(new Long(2));
		assertTrue("The two hashCodes should be equal",
				standardDomainOne.hashCode() == standardDomainTwo.hashCode());

		// Now set the ID back, check equals again
		standardDomainTwo.setId(new Long(1));
		assertEquals("The two hashCodes should be equal after ID set back.",
				standardDomainOne.hashCode(), standardDomainTwo.hashCode());

		// Now set the name and they should be different
		try {
			standardDomainTwo.setName("StandardDomainTwo");
		} catch (MetadataException e) {
			assertTrue(
					"MetadataException caught trying to set values: "
							+ e.getMessage(), false);
		}
		assertTrue("The two hashCodes should not be equal after name change",
				standardDomainOne.hashCode() != standardDomainTwo.hashCode());

		// Now set it back and change all the non-business key values. The
		// results should be equals
		try {
			standardDomainTwo.setName("StandardDomainOne");
			standardDomainTwo.setDescription("blah blah");
		} catch (MetadataException e) {
			assertTrue(
					"MetadataException caught trying to set values: "
							+ e.getMessage(), false);
		}
		assertEquals("The two hashCodes should be equal after ID and name same"
				+ ", but different business keys.",
				standardDomainOne.hashCode(), standardDomainTwo.hashCode());
	}

	/**
	 * This test takes a Keyword defined in XML, converts it to an object,
	 * checks the attributes, converts changes some attributes and converts back
	 * to XML.
	 */
	public void testStandardDomainXMLBinding() {

		// Grab the file that has the XML in it
		File standardDomainXMLFile = new File("src" + File.separator
				+ "resources" + File.separator + "test" + File.separator
				+ "xml" + File.separator + "StandardDomain.xml");
		if (!standardDomainXMLFile.exists())
			assertTrue("Could not find StandardDomain.xml file for testing.",
					false);
		logger.debug("Will read standardDomain XML from "
				+ standardDomainXMLFile.getAbsolutePath());

		// Create the object builder
		ObjectBuilder objectBuilder = null;
		try {
			objectBuilder = new ObjectBuilder(standardDomainXMLFile.toURI()
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
		assertTrue("Unmarshalled object should be a StandardDomain",
				unmarshalledObject instanceof StandardDomain);

		// Cast it
		StandardDomain testStandardDomain = (StandardDomain) unmarshalledObject;
		assertEquals("ID should be 1", testStandardDomain.getId().longValue(),
				Long.parseLong("1"));
		assertEquals("StandardDomain name should match",
				testStandardDomain.getName(), "Test StandardDomain");
		assertEquals("Descripton should match",
				testStandardDomain.getDescription(),
				"Test StandardDomain Description");

		// Now let's change the attributes
		try {
			testStandardDomain.setName("Changed Test StandardDomain");
			testStandardDomain
					.setDescription("Changed Test StandardDomain Description");
			logger.debug("Changed name and description "
					+ "and will marshall to XML");
		} catch (MetadataException e) {
			assertTrue("Error while changing attributes: " + e.getMessage(),
					false);
		}

		// Create an XML builder to marshall back out the XML
		XmlBuilder xmlBuilder = new XmlBuilder();
		xmlBuilder.add(testStandardDomain);
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
					.toString().contains("Changed Test StandardDomain"));
			assertTrue(
					"Marshalled XML contain changed description",
					stringWriter.toString().contains(
							"Changed Test StandardDomain Description"));
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