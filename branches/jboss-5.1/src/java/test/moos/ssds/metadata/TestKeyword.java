package test.moos.ssds.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;

import junit.framework.TestCase;
import moos.ssds.metadata.Keyword;
import moos.ssds.metadata.Metadata;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

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

		// Create a file reader
		FileReader keywordXMLFileReader = null;
		try {
			keywordXMLFileReader = new FileReader(keywordXMLFile);
		} catch (FileNotFoundException e2) {
			assertTrue("Error in creating file reader for keyword XML file: "
					+ e2.getMessage(), false);
		}

		// Grab the binding factory for Keywords
		IBindingFactory bfact = null;
		try {
			bfact = BindingDirectory.getFactory(Metadata.class);
		} catch (JiBXException e1) {
			assertTrue("Error in getting Binding Factory for Keyword: "
					+ e1.getMessage(), false);
		}

		// Grab a JiBX unmarshalling context
		IUnmarshallingContext uctx = null;
		if (bfact != null) {
			try {
				uctx = bfact.createUnmarshallingContext();
			} catch (JiBXException e) {
				assertTrue(
						"Error in getting UnmarshallingContext for Keyword: "
								+ e.getMessage(), false);
			}
		}

		// Now unmarshall it
		if (uctx != null) {
			Metadata topMetadata = null;
			Keyword testKeyword = null;
			try {
				topMetadata = (Metadata) uctx.unmarshalDocument(
						keywordXMLFileReader, null);
				testKeyword = topMetadata.getKeywords().iterator().next();

				logger.debug("TestKeyword after unmarshalling: "
						+ testKeyword.toStringRepresentation("|"));
			} catch (JiBXException e1) {
				assertTrue(
						"Error in unmarshalling Keyword: " + e1.getMessage(),
						false);
			} catch (Throwable t) {
				t.printStackTrace();
				logger.error("Throwable caught: " + t.getMessage());
			}

			if (testKeyword != null) {
				assertEquals("ID should be 1", testKeyword.getId().longValue(),
						Long.parseLong("1"));
				assertEquals("Keyword name should match",
						testKeyword.getName(), "Test Keyword");
				assertEquals("Descripton should match", testKeyword
						.getDescription(), "Test Keyword Description");

				// Now let's change the attributes
				try {
					testKeyword.setName("Changed Test Keyword");
					testKeyword
							.setDescription("Changed Test Keyword Description");
					logger.debug("Changed name and description "
							+ "and will marshall to XML");
				} catch (MetadataException e) {
					assertTrue("Error while changing attributes on keyword: "
							+ e.getMessage(), false);
				}

				// Create a string writer
				StringWriter keywordStringWriter = new StringWriter();

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
						mctx.marshalDocument(testKeyword, "UTF-8", null,
								keywordStringWriter);
					} catch (JiBXException e) {
						assertTrue("Error while marshalling testKeyword "
								+ "after attribute changes: " + e.getMessage(),
								false);
					}

					logger.debug("Marshalled XML after change: "
							+ keywordStringWriter.toString());

					// Now test the string
					assertTrue("Marshalled XML contain changed name",
							keywordStringWriter.toString().contains(
									"Changed Test Keyword"));
					assertTrue("Marshalled XML contain changed description",
							keywordStringWriter.toString().contains(
									"Changed Test Keyword Description"));
				}

			} else {
				assertTrue("testKeyword came back null!", false);
			}
		}

	}
}
