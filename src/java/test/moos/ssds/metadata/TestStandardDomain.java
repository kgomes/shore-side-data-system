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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;

import junit.framework.TestCase;
import moos.ssds.metadata.Metadata;
import moos.ssds.metadata.StandardDomain;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

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
			assertTrue("MetadataException caught trying to set values: "
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
			assertTrue("MetadataException caught trying to set values: "
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
			logger
					.error("MetadataException caught trying to create two StandardDomain objects");
		}

		assertTrue("The two StandardDomains should be equal (part one).",
				standardDomainOne.equals(standardDomainTwo));
		assertEquals("The two StandardDomains should be equal (part two).",
				standardDomainOne, standardDomainTwo);

		// Now change the ID of the second one and they should still be equal
		standardDomainTwo.setId(new Long(2));
		assertTrue("The two StandardDomain should be equal", standardDomainOne
				.equals(standardDomainTwo));

		// Now set the ID back, check equals again
		standardDomainTwo.setId(new Long(1));
		assertEquals(
				"The two StandardDomains should be equal after ID set back.",
				standardDomainOne, standardDomainTwo);

		// Now set the name and they should be different
		try {
			standardDomainTwo.setName("StandardDomainTwo");
		} catch (MetadataException e) {
			assertTrue("MetadataException caught trying to set values: "
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
			assertTrue("MetadataException caught trying to set values: "
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
			logger
					.error("MetadataException caught trying to create two StandardDomain objects: "
							+ e.getMessage());
		}

		assertTrue("The two hashCodes should be equal (part one).",
				standardDomainOne.hashCode() == standardDomainTwo.hashCode());
		assertEquals("The two hashCodes should be equal (part two).",
				standardDomainOne.hashCode(), standardDomainTwo.hashCode());

		// Now change the ID of the second one and they should not be equal
		standardDomainTwo.setId(new Long(2));
		assertTrue("The two hashCodes should be equal", standardDomainOne
				.hashCode() == standardDomainTwo.hashCode());

		// Now set the ID back, check equals again
		standardDomainTwo.setId(new Long(1));
		assertEquals("The two hashCodes should be equal after ID set back.",
				standardDomainOne.hashCode(), standardDomainTwo.hashCode());

		// Now set the name and they should be different
		try {
			standardDomainTwo.setName("StandardDomainTwo");
		} catch (MetadataException e) {
			assertTrue("MetadataException caught trying to set values: "
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
			assertTrue("MetadataException caught trying to set values: "
					+ e.getMessage(), false);
		}
		assertEquals("The two hashCodes should be equal after ID and name same"
				+ ", but different business keys.", standardDomainOne
				.hashCode(), standardDomainTwo.hashCode());
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

		// Create a file reader
		FileReader standardDomainXMLFileReader = null;
		try {
			standardDomainXMLFileReader = new FileReader(standardDomainXMLFile);
		} catch (FileNotFoundException e2) {
			assertTrue(
					"Error in creating file reader for standardDomain XML file: "
							+ e2.getMessage(), false);
		}

		// Grab the binding factory
		IBindingFactory bfact = null;
		try {
			bfact = BindingDirectory.getFactory(Metadata.class);
		} catch (JiBXException e1) {
			assertTrue("Error in getting Binding Factory: " + e1.getMessage(),
					false);
		}

		// Grab a JiBX unmarshalling context
		IUnmarshallingContext uctx = null;
		if (bfact != null) {
			try {
				uctx = bfact.createUnmarshallingContext();
			} catch (JiBXException e) {
				assertTrue("Error in getting UnmarshallingContext: "
						+ e.getMessage(), false);
			}
		}

		// Now unmarshall it
		if (uctx != null) {
			Metadata topMetadata = null;
			StandardDomain testStandardDomain = null;
			try {
				topMetadata = (Metadata) uctx.unmarshalDocument(
						standardDomainXMLFileReader, null);
				testStandardDomain = topMetadata.getStandardDomains()
						.iterator().next();

				logger.debug("TestStandardDomain after unmarshalling: "
						+ testStandardDomain.toStringRepresentation("|"));
			} catch (JiBXException e1) {
				assertTrue("Error in unmarshalling: " + e1.getMessage(), false);
			} catch (Throwable t) {
				t.printStackTrace();
				logger.error("Throwable caught: " + t.getMessage());
			}

			if (testStandardDomain != null) {
				assertEquals("ID should be 1", testStandardDomain.getId()
						.longValue(), Long.parseLong("1"));
				assertEquals("StandardDomain name should match",
						testStandardDomain.getName(), "Test StandardDomain");
				assertEquals("Descripton should match", testStandardDomain
						.getDescription(), "Test StandardDomain Description");

				// Now let's change the attributes
				try {
					testStandardDomain.setName("Changed Test StandardDomain");
					testStandardDomain
							.setDescription("Changed Test StandardDomain Description");
					logger.debug("Changed name and description "
							+ "and will marshall to XML");
				} catch (MetadataException e) {
					assertTrue("Error while changing attributes: "
							+ e.getMessage(), false);
				}

				// Create a string writer
				StringWriter stringWriter = new StringWriter();

				// Marshall out to XML
				IMarshallingContext mctx = null;
				try {
					mctx = bfact.createMarshallingContext();
				} catch (JiBXException e) {
					assertTrue("Error while creating marshalling context: "
							+ e.getMessage(), false);
				}

				if (mctx != null) {
					mctx.setIndent(2);
					try {
						mctx.marshalDocument(testStandardDomain, "UTF-8", null,
								stringWriter);
					} catch (JiBXException e) {
						assertTrue("Error while marshalling "
								+ "after attribute changes: " + e.getMessage(),
								false);
					}

					logger.debug("Marshalled XML after change: "
							+ stringWriter.toString());

					// Now test the string
					assertTrue("Marshalled XML contain changed name",
							stringWriter.toString().contains(
									"Changed Test StandardDomain"));
					assertTrue("Marshalled XML contain changed description",
							stringWriter.toString().contains(
									"Changed Test StandardDomain Description"));
				}

			} else {
				assertTrue("metadata object came back null!", false);
			}
		}

	}

}