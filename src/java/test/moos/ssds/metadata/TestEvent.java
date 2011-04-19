package test.moos.ssds.metadata;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;
import moos.ssds.metadata.Event;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.ObjectBuilder;
import moos.ssds.metadata.util.XmlBuilder;

import org.apache.log4j.Logger;

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

		// Create the object builder
		ObjectBuilder objectBuilder = null;
		try {
			objectBuilder = new ObjectBuilder(eventXMLFile.toURI().toURL());
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
		assertTrue("Unmarshalled object should be an Event",
				unmarshalledObject instanceof Event);

		// Cast it
		Event testEvent = (Event) unmarshalledObject;

		logger.debug("TestEvent after unmarshalling: "
				+ testEvent.toStringRepresentation("|"));

		assertEquals("ID should be 1", testEvent.getId().longValue(),
				Long.parseLong("1"));
		assertEquals("Event name should match", testEvent.getName(),
				"Test Event");
		assertEquals("Descripton should match", testEvent.getDescription(),
				"Test Event Description");
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
		assertEquals("End should match to the second", testEvent.getEndDate()
				.getTime() / 1000, endDateForComparison.getTime() / 1000);

		// Now let's change the attributes
		try {
			testEvent.setName("Changed Test Event");
			testEvent.setDescription("Changed Test Event Description");
			logger.debug("Changed name and description "
					+ "and will marshall to XML");
		} catch (MetadataException e) {
			logger.error("MetadataException caught changing the Event:"
					+ e.getMessage());
			assertTrue(
					"Error while changing attributes on event: "
							+ e.getMessage(), false);
		}

		// Create an XML builder to marshall back out the XML
		XmlBuilder xmlBuilder = new XmlBuilder();
		xmlBuilder.add(testEvent);
		xmlBuilder.marshal();

		// Now test the xml
		try {
			assertTrue("Marshalled XML contain changed name", xmlBuilder
					.toFormattedXML().contains("Changed Test Event"));
			assertTrue("Marshalled XML contain changed description", xmlBuilder
					.toFormattedXML()
					.contains("Changed Test Event Description"));
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
