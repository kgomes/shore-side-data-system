package test.moos.ssds.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;
import moos.ssds.metadata.Event;
import moos.ssds.metadata.Metadata;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

public class TestDataProducerGroup extends TestCase {

	static Logger logger = Logger.getLogger(TestDataProducerGroup.class);

	public TestDataProducerGroup(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * This test takes a MetadataObject defined in XML, converts it to an
	 * object, checks the attributes, converts changes some attributes and
	 * converts back to XML.
	 */
	public void testXMLBinding() {

		String xmlFileName = "DataProducerGroup";

		// Grab the file that has the XML in it
		File xmlFile = new File("src" + File.separator + "resources"
				+ File.separator + "test" + File.separator + "xml"
				+ File.separator + xmlFileName + ".xml");
		if (!xmlFile.exists())
			assertTrue("Could not find xml file file for testing.", false);
		logger.debug("Will read XML from " + xmlFile.getAbsolutePath());

		// Create a file reader
		FileReader xmlFileReader = null;
		try {
			xmlFileReader = new FileReader(xmlFile);
		} catch (FileNotFoundException e2) {
			assertTrue(
					"Error in creating file reader for XML file: "
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
				assertTrue("Error in getting UnmarshallingContext for Event: "
						+ e.getMessage(), false);
			}
		}

		// Now unmarshall it
		if (uctx != null) {
			Metadata topMetadata = null;
			try {
				topMetadata = (Metadata) uctx.unmarshalDocument(xmlFileReader,
						null);
			} catch (JiBXException e1) {
				assertTrue("Error in unmarshalling Event: " + e1.getMessage(),
						false);
			} catch (Throwable t) {
				t.printStackTrace();
				logger.error("Throwable caught: " + t.getMessage());
			}
			logger.debug("Metadata object: "
					+ topMetadata.toStringRepresentation("|"));
		}

	}
}
