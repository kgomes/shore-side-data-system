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

public class TestEvent extends TestCase {

	static Logger logger = Logger.getLogger(TestEvent.class);

	public TestEvent(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * This test takes a Event defined in XML, converts it to an object, checks
	 * the attributes, converts changes some attributes and converts back to
	 * XML.
	 */
	public void testEventXMLBinding() {

		// Grab the file that has the XML in it
		File eventXMLFile = new File("src" + File.separator + "resources"
				+ File.separator + "test" + File.separator + "xml"
				+ File.separator + "Event.xml");
		if (!eventXMLFile.exists())
			assertTrue("Could not find Event.xml file for testing.", false);
		logger.debug("Will read event XML from "
				+ eventXMLFile.getAbsolutePath());

		// Create a file reader
		FileReader eventXMLFileReader = null;
		try {
			eventXMLFileReader = new FileReader(eventXMLFile);
		} catch (FileNotFoundException e2) {
			assertTrue("Error in creating file reader for event XML file: "
					+ e2.getMessage(), false);
		}

		// Grab the binding factory for Events
		IBindingFactory bfact = null;
		try {
			bfact = BindingDirectory.getFactory(Metadata.class);
		} catch (JiBXException e1) {
			assertTrue("Error in getting Binding Factory for Event: "
					+ e1.getMessage(), false);
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
			Event testEvent = null;
			try {
				topMetadata = (Metadata) uctx.unmarshalDocument(
						eventXMLFileReader, null);
				testEvent = topMetadata.getEvents().iterator().next();

				logger.debug("TestEvent after unmarshalling: "
						+ testEvent.toStringRepresentation("|"));
			} catch (JiBXException e1) {
				assertTrue("Error in unmarshalling Event: " + e1.getMessage(),
						false);
			} catch (Throwable t) {
				t.printStackTrace();
				logger.error("Throwable caught: " + t.getMessage());
			}

			if (testEvent != null) {
				assertEquals("ID should be 1", testEvent.getId().longValue(),
						Long.parseLong("1"));
				assertEquals("Event name should match", testEvent.getName(),
						"Test Event");
				assertEquals("Descripton should match", testEvent
						.getDescription(), "Test Event Description");
				Calendar startDate = Calendar.getInstance();
				startDate.setTimeZone(TimeZone.getTimeZone("GMT"));
				startDate.set(Calendar.YEAR, 2000);
				startDate.set(Calendar.MONTH, Calendar.JANUARY);
				startDate.set(Calendar.DAY_OF_MONTH, 2);
				startDate.set(Calendar.HOUR_OF_DAY, 3);
				startDate.set(Calendar.MINUTE, 4);
				startDate.set(Calendar.SECOND, 5);
				Date startDateForComparison = startDate.getTime();
				assertEquals("StartDate should match to the second", testEvent
						.getStartDate().getTime() / 1000,
						startDateForComparison.getTime() / 1000);
				Calendar endDate = Calendar.getInstance();
				endDate.setTimeZone(TimeZone.getTimeZone("GMT"));
				endDate.set(Calendar.YEAR, 2006);
				endDate.set(Calendar.MONTH, Calendar.JULY);
				endDate.set(Calendar.DAY_OF_MONTH, 8);
				endDate.set(Calendar.HOUR_OF_DAY, 9);
				endDate.set(Calendar.MINUTE, 10);
				endDate.set(Calendar.SECOND, 11);
				Date endDateForComparison = endDate.getTime();
				assertEquals("End should match to the second", testEvent
						.getEndDate().getTime() / 1000, endDateForComparison
						.getTime() / 1000);

				// Now let's change the attributes
				try {
					testEvent.setName("Changed Test Event");
					testEvent.setDescription("Changed Test Event Description");
					logger.debug("Changed name and description "
							+ "and will marshall to XML");
				} catch (MetadataException e) {
					assertTrue("Error while changing attributes on event: "
							+ e.getMessage(), false);
				}

				// Create a string writer
				StringWriter eventStringWriter = new StringWriter();

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
						mctx.marshalDocument(testEvent, "UTF-8", null,
								eventStringWriter);
					} catch (JiBXException e) {
						assertTrue("Error while marshalling testEvent "
								+ "after attribute changes: " + e.getMessage(),
								false);
					}

					logger.debug("Marshalled XML after change: "
							+ eventStringWriter.toString());

					// Now test the string
					assertTrue("Marshalled XML contain changed name",
							eventStringWriter.toString().contains(
									"Changed Test Event"));
					assertTrue("Marshalled XML contain changed description",
							eventStringWriter.toString().contains(
									"Changed Test Event Description"));
				}

			} else {
				assertTrue("testEvent came back null!", false);
			}
		}

	}
}
