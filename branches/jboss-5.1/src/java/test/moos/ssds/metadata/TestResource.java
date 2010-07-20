package test.moos.ssds.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import junit.framework.TestCase;
import moos.ssds.metadata.Keyword;
import moos.ssds.metadata.Metadata;
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

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

		// Create a file reader
		FileReader resourceXMLFileReader = null;
		try {
			resourceXMLFileReader = new FileReader(resourceXMLFile);
		} catch (FileNotFoundException e2) {
			assertTrue("Error in creating file reader for resource XML file: "
					+ e2.getMessage(), false);
		}

		// Grab the binding factory for Resources
		IBindingFactory bfact = null;
		try {
			bfact = BindingDirectory.getFactory(Metadata.class);
		} catch (JiBXException e1) {
			assertTrue("Error in getting Binding Factory for Resource: "
					+ e1.getMessage(), false);
		}

		// Grab a JiBX unmarshalling context
		IUnmarshallingContext uctx = null;
		if (bfact != null) {
			try {
				uctx = bfact.createUnmarshallingContext();
			} catch (JiBXException e) {
				assertTrue(
						"Error in getting UnmarshallingContext for Resource: "
								+ e.getMessage(), false);
			}
		}

		// Now unmarshall it
		if (uctx != null) {
			Metadata topMetadata = null;
			Resource testResource = null;
			try {
				topMetadata = (Metadata) uctx.unmarshalDocument(
						resourceXMLFileReader, null);
				testResource = topMetadata.getResources().iterator().next();

				logger.debug("TestResource after unmarshalling: "
						+ testResource.toStringRepresentation("|"));
			} catch (JiBXException e1) {
				assertTrue("Error in unmarshalling Resource: "
						+ e1.getMessage(), false);
			} catch (Throwable t) {
				t.printStackTrace();
				logger.error("Throwable caught: " + t.getMessage());
			}

			if (testResource != null) {
				assertEquals("ID should be 1",
						testResource.getId().longValue(), Long.parseLong("1"));
				assertEquals("Resource name should match", testResource
						.getName(), "Test Resource Name");
				assertEquals("Descripton should match", testResource
						.getDescription(), "Test Resource Description");
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
				assertEquals("StartDate should match to the second",
						testResource.getStartDate().getTime() / 1000,
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
						.getEndDate().getTime() / 1000, endDateForComparison
						.getTime() / 1000);
				assertEquals("MimeTypes should match", testResource
						.getMimeType(), "Test Resource MimeType");
				assertEquals("UriString should match", testResource
						.getUriString(), "http://test.resource.uri");

				// Let's look over the keywords
				for (Iterator<Keyword> iterator = testResource.getKeywords()
						.iterator(); iterator.hasNext();) {
					Keyword keyword = iterator.next();
					assertTrue(
							"Keyword matches one given",
							(keyword.getId().equals(new Long("1"))
									|| keyword.getId().equals(new Long("2"))
									|| keyword.getId().equals(new Long("3")) || keyword
									.getId().equals(new Long("4")))
									&& (keyword.getName().equals(
											"Test Keyword 1")
											|| keyword.getName().equals(
													"Test Keyword 2")
											|| keyword.getName().equals(
													"Test Keyword 3") || keyword
											.getName().equals("Test Keyword 4"))
									&& (keyword.getDescription().equals(
											"Test Keyword 1 Description")
											|| keyword
													.getDescription()
													.equals(
															"Test Keyword 2 Description")
											|| keyword
													.getDescription()
													.equals(
															"Test Keyword 3 Description") || keyword
											.getDescription()
											.equals(
													"Test Keyword 4 Description")));
				}
				// How about resourceBLOB
				assertEquals("ResourceBLOB ids are equal", testResource
						.getResourceBLOB().getId().longValue(), Long
						.parseLong("1"));
				assertEquals("ResourceBLOB names are equal", testResource
						.getResourceBLOB().getName(), "Test ResourceBLOB");
				assertEquals("ResourceBLOB descriptions are equal",
						testResource.getResourceBLOB().getDescription(),
						"Test ResourceBLOB Description");
				logger.debug("ResourceBLOB byte array: "
						+ new String(testResource.getResourceBLOB()
								.getByteArray()));
				assertTrue("Byte Arrays should be the same", Arrays.equals(
						testResource.getResourceBLOB().getByteArray(),
						"Hello world".getBytes()));

				// And finally ResourceType
				assertEquals("ResourceType id should be the same", testResource
						.getResourceType().getId().longValue(), Long
						.parseLong("1"));
				assertEquals("ResourceType name should be equal", testResource
						.getResourceType().getName(), "Test ResourceType");
				assertEquals("ResourceType description should be equal",
						testResource.getResourceType().getDescription(),
						"Test ResourceType Description");

				// Now let's change the attributes
				try {
					testResource.setName("Changed Test Resource Name");
					testResource
							.setDescription("Changed Test Resource Description");
					logger.debug("Changed name and description "
							+ "and will marshall to XML");
				} catch (MetadataException e) {
					assertTrue("Error while changing attributes on resource: "
							+ e.getMessage(), false);
				}

				// Create a string writer
				StringWriter resourceStringWriter = new StringWriter();

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
						mctx.marshalDocument(testResource, "UTF-8", null,
								resourceStringWriter);
					} catch (JiBXException e) {
						assertTrue("Error while marshalling testResource "
								+ "after attribute changes: " + e.getMessage(),
								false);
					}

					logger.debug("Marshalled XML after change: "
							+ resourceStringWriter.toString());

					// Now test the string
					assertTrue("Marshalled XML contain changed name",
							resourceStringWriter.toString().contains(
									"Changed Test Resource Name"));
					assertTrue("Marshalled XML contain changed description",
							resourceStringWriter.toString().contains(
									"Changed Test Resource Description"));
				}

			} else {
				assertTrue("testResource came back null!", false);
			}
		}

	}
}
