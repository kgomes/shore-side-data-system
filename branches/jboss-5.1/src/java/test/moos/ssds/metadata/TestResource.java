package test.moos.ssds.metadata;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import junit.framework.TestCase;
import moos.ssds.metadata.Keyword;
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.ObjectBuilder;
import moos.ssds.metadata.util.XmlBuilder;

import org.apache.log4j.Logger;

public class TestResource extends TestCase {

	static Logger logger = Logger.getLogger(TestResource.class);

	public TestResource(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * This test takes a Resource defined in XML, converts it to an object,
	 * checks the attributes, converts changes some attributes and converts back
	 * to XML.
	 */
	public void testResourceXMLBinding() {

		// Grab the file that has the XML in it
		File resourceXMLFile = new File("src" + File.separator + "resources"
				+ File.separator + "test" + File.separator + "xml"
				+ File.separator + "Resource.xml");
		if (!resourceXMLFile.exists())
			assertTrue("Could not find Resource.xml file for testing.", false);
		logger.debug("Will read resource XML from "
				+ resourceXMLFile.getAbsolutePath());

		// Create the object builder
		ObjectBuilder objectBuilder = null;
		try {
			objectBuilder = new ObjectBuilder(resourceXMLFile.toURI().toURL());
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
		assertTrue("Unmarshalled object should be a Resource",
				unmarshalledObject instanceof Resource);

		// Cast it
		Resource testResource = (Resource) unmarshalledObject;
		assertEquals("ID should be 1", testResource.getId().longValue(),
				Long.parseLong("1"));
		assertEquals("Resource name should match", testResource.getName(),
				"Test Resource Name");
		assertEquals("Descripton should match", testResource.getDescription(),
				"Test Resource Description");
		assertEquals("Content length should match", testResource
				.getContentLength().longValue(), Long.parseLong("100"));
		Calendar startDate = Calendar.getInstance();
		startDate.setTimeZone(TimeZone.getTimeZone("GMT"));
		startDate.set(Calendar.YEAR, 2000);
		startDate.set(Calendar.MONTH, Calendar.JANUARY);
		startDate.set(Calendar.DAY_OF_MONTH, 2);
		startDate.set(Calendar.HOUR_OF_DAY, 3);
		startDate.set(Calendar.MINUTE, 4);
		startDate.set(Calendar.SECOND, 5);
		Date startDateForComparison = startDate.getTime();
		assertEquals("StartDate should match to the second", testResource
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
		assertEquals("End should match to the second", testResource
				.getEndDate().getTime() / 1000,
				endDateForComparison.getTime() / 1000);
		assertEquals("MimeTypes should match", testResource.getMimeType(),
				"Test Resource MimeType");
		assertEquals("UriString should match", testResource.getUriString(),
				"http://test.resource.uri");

		// Let's look over the keywords
		for (Iterator<Keyword> iterator = testResource.getKeywords().iterator(); iterator
				.hasNext();) {
			Keyword keyword = iterator.next();
			assertTrue(
					"Keyword matches one given",
					(keyword.getId().equals(new Long("1"))
							|| keyword.getId().equals(new Long("2"))
							|| keyword.getId().equals(new Long("3")) || keyword
							.getId().equals(new Long("4")))
							&& (keyword.getName().equals("Test Keyword 1")
									|| keyword.getName().equals(
											"Test Keyword 2")
									|| keyword.getName().equals(
											"Test Keyword 3") || keyword
									.getName().equals("Test Keyword 4"))
							&& (keyword.getDescription().equals(
									"Test Keyword 1 Description")
									|| keyword.getDescription().equals(
											"Test Keyword 2 Description")
									|| keyword.getDescription().equals(
											"Test Keyword 3 Description") || keyword
									.getDescription().equals(
											"Test Keyword 4 Description")));
		}
		// How about resourceBLOB
		assertEquals("ResourceBLOB ids are equal", testResource
				.getResourceBLOB().getId().longValue(), Long.parseLong("1"));
		assertEquals("ResourceBLOB names are equal", testResource
				.getResourceBLOB().getName(), "Test ResourceBLOB");
		assertEquals("ResourceBLOB descriptions are equal", testResource
				.getResourceBLOB().getDescription(),
				"Test ResourceBLOB Description");
		logger.debug("ResourceBLOB byte array: "
				+ new String(testResource.getResourceBLOB().getByteArray()));
		assertTrue("Byte Arrays should be the same", Arrays.equals(testResource
				.getResourceBLOB().getByteArray(), "Hello world".getBytes()));

		// And finally ResourceType
		assertEquals("ResourceType id should be the same", testResource
				.getResourceType().getId().longValue(), Long.parseLong("1"));
		assertEquals("ResourceType name should be equal", testResource
				.getResourceType().getName(), "Test ResourceType");
		assertEquals("ResourceType description should be equal", testResource
				.getResourceType().getDescription(),
				"Test ResourceType Description");

		// Now let's change the attributes
		try {
			testResource.setName("Changed Test Resource Name");
			testResource.setDescription("Changed Test Resource Description");
			logger.debug("Changed name and description "
					+ "and will marshall to XML");
		} catch (MetadataException e) {
			assertTrue(
					"Error while changing attributes on resource: "
							+ e.getMessage(), false);
		}

		// Create an XML builder to marshall back out the XML
		XmlBuilder xmlBuilder = new XmlBuilder();
		xmlBuilder.add(testResource);
		xmlBuilder.marshal();

		// Now test the xml
		try {
			StringWriter resourceStringWriter = new StringWriter();
			resourceStringWriter.append(xmlBuilder.toFormattedXML());

			// Now make sure the resulting string contains all the
			// updates I did
			logger.debug("Marshalled XML after change: "
					+ resourceStringWriter.toString());
			// Now test the string
			assertTrue(
					"Marshalled XML contain changed name",
					resourceStringWriter.toString().contains(
							"Changed Test Resource Name"));
			assertTrue(
					"Marshalled XML contain changed description",
					resourceStringWriter.toString().contains(
							"Changed Test Resource Description"));
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
