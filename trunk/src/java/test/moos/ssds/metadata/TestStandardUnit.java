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
import moos.ssds.metadata.StandardUnit;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

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

		// Create a file reader
		FileReader standardUnitXMLFileReader = null;
		try {
			standardUnitXMLFileReader = new FileReader(standardUnitXMLFile);
		} catch (FileNotFoundException e2) {
			assertTrue(
					"Error in creating file reader for standardUnit XML file: "
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
			StandardUnit testStandardUnit = null;
			try {
				topMetadata = (Metadata) uctx.unmarshalDocument(
						standardUnitXMLFileReader, null);
				testStandardUnit = topMetadata.getStandardUnits().iterator()
						.next();

				logger.debug("TestStandardUnit after unmarshalling: "
						+ testStandardUnit.toStringRepresentation("|"));
			} catch (JiBXException e1) {
				assertTrue("Error in unmarshalling: " + e1.getMessage(), false);
			} catch (Throwable t) {
				t.printStackTrace();
				logger.error("Throwable caught: " + t.getMessage());
			}

			if (testStandardUnit != null) {
				assertEquals("ID should be 1", testStandardUnit.getId()
						.longValue(), Long.parseLong("1"));
				assertEquals("StandardUnit name should match", testStandardUnit
						.getName(), "Test StandardUnit");
				assertEquals("Description should match", testStandardUnit
						.getDescription(), "Test StandardUnit Description");
				assertEquals("LongName should match", testStandardUnit
						.getLongName(), "Test StandardUnit LongName");
				assertEquals("Symbol should match", testStandardUnit
						.getSymbol(), "Test StandardUnit Symbol");

				// Now let's change the attributes
				try {
					testStandardUnit.setName("Changed Test StandardUnit");
					testStandardUnit
							.setDescription("Changed Test StandardUnit Description");
					testStandardUnit
							.setLongName("Changed Test StandardUnit LongName");
					testStandardUnit
							.setSymbol("Changed Test StandardUnit Symbol");
					logger.debug("Changed attributes "
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
						mctx.marshalDocument(testStandardUnit, "UTF-8", null,
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
									"Changed Test StandardUnit"));
					assertTrue("Marshalled XML contain changed description",
							stringWriter.toString().contains(
									"Changed Test StandardUnit Description"));
					assertTrue("Marshalled XML contain changed longname",
							stringWriter.toString().contains(
									"Changed Test StandardUnit LongName"));
					assertTrue("Marshalled XML contain changed symbol",
							stringWriter.toString().contains(
									"Changed Test StandardUnit Symbol"));
				}

			} else {
				assertTrue("metadata object came back null!", false);
			}
		}

	}

}