package test.moos.ssds.metadata;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import junit.framework.TestCase;
import moos.ssds.metadata.Keyword;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.ObjectBuilder;
import moos.ssds.metadata.util.XmlBuilder;

import org.apache.log4j.Logger;

public class TestKeyword extends TestCase {

	static Logger logger = Logger.getLogger(TestKeyword.class);

	public TestKeyword(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * This test takes a Keyword defined in XML, converts it to an object,
	 * checks the attributes, converts changes some attributes and converts back
	 * to XML.
	 */
	public void testKeywordXMLBinding() {

		// Grab the file that has the XML in it
		File keywordXMLFile = new File("src" + File.separator + "resources"
				+ File.separator + "test" + File.separator + "xml"
				+ File.separator + "Keyword.xml");
		if (!keywordXMLFile.exists())
			assertTrue("Could not find Keyword.xml file for testing.", false);
		logger.debug("Will read keyword XML from "
				+ keywordXMLFile.getAbsolutePath());

		// Create the object builder
		ObjectBuilder objectBuilder = null;
		try {
			objectBuilder = new ObjectBuilder(keywordXMLFile.toURI().toURL());
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
		assertTrue("Unmarshalled object should be a Keyword",
				unmarshalledObject instanceof Keyword);

		// Cast it
		Keyword testKeyword = (Keyword) unmarshalledObject;

		assertEquals("ID should be 1", testKeyword.getId().longValue(),
				Long.parseLong("1"));
		assertEquals("Keyword name should match", testKeyword.getName(),
				"Test Keyword");
		assertEquals("Descripton should match", testKeyword.getDescription(),
				"Test Keyword Description");

		// Now let's change the attributes
		try {
			testKeyword.setName("Changed Test Keyword");
			testKeyword.setDescription("Changed Test Keyword Description");
			logger.debug("Changed name and description "
					+ "and will marshall to XML");
		} catch (MetadataException e) {
			assertTrue(
					"Error while changing attributes on keyword: "
							+ e.getMessage(), false);
		}

		// Create an XML builder to marshall back out the XML
		XmlBuilder xmlBuilder = new XmlBuilder();
		xmlBuilder.add(testKeyword);
		xmlBuilder.marshal();

		// Now test the xml
		try {
			// Now test the string
			assertTrue("Marshalled XML contain changed name", xmlBuilder
					.toFormattedXML().contains("Changed Test Keyword"));
			assertTrue(
					"Marshalled XML contain changed description",
					xmlBuilder.toFormattedXML().toString()
							.contains("Changed Test Keyword Description"));
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
