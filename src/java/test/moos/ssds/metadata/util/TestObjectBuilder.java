package test.moos.ssds.metadata.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.util.ObjectBuilder;

import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.XMLTestCase;

public class TestObjectBuilder extends XMLTestCase {

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(TestObjectBuilder.class);

	// The local objectBuilder object
	private ObjectBuilder objectBuilder = null;

	// The URL of the file to use to test
	private URL url = null;

	// A Collection of all objects in the object builder
	private Collection<Object> allObjects = null;

	/**
	 * Constructs a test case with the given name.
	 */
	public TestObjectBuilder(String name) {
		super(name);
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
		// Configure the logger
		logger.debug("Setting up the test");

		// Get the XML file
		try {
			url = ObjectBuilder.class.getResource("TestObjectBuilder.xml");
		} catch (RuntimeException e1) {
			logger.error("Could not get the xml file to read: "
					+ e1.getMessage());
			e1.printStackTrace();
			assertTrue("Could not get XML file to read:", false);
		}
		logger.debug("The url to the XML file is : " + url);

		// Go ahead and unmarshall it to objects
		try {
			objectBuilder = new ObjectBuilder(url);
			objectBuilder.unmarshal(false);
			allObjects = objectBuilder.listAll();

		} catch (Throwable e) {
			logger.error("Failure in object builder somewhere: "
					+ e.getMessage());
			assertTrue(
					"Failure in object builder somewhere: " + e.getMessage(),
					false);
		}
		logger.debug("Setup complete...");
	}

	/**
	 * Test all the XML that should have been bound
	 */
	public void testXMLBinding() {

		// Make sure ObjectBuilder exists
		assertNotNull("ObjectBuilder should not be null", objectBuilder);

		// Check for errors
		assertTrue("Unmarshalling should not have caused a failure",
				!objectBuilder.didUnmarshalFail());

		// Should not be null
		assertNotNull(
				"The head objects after unmarshalling should not be null",
				allObjects);

		// There should be only one thing in the collection
		assertEquals("There should be one object at the head",
				allObjects.size(), 1);

		// Grab the object
		Object headObject = allObjects.iterator().next();

		// The head object should not be null
		assertNotNull("Head object should not be null", headObject);

		// See if it is a DataProducer
		assertTrue("Heads object should be a DataProducer",
				headObject instanceof DataProducer);

		// Cast it for more testing
		DataProducer auvDeployment = (DataProducer) headObject;

		// Make sure it is not null
		assertNotNull("auvDeployment should not be null", auvDeployment);

		// Check it's attributes
		assertEquals("Role should be platform", DataProducer.ROLE_PLATFORM,
				auvDeployment.getRole());
		assertEquals("Name should be DoradoMission", "DoradoMission",
				auvDeployment.getName());
		assertEquals("DataProducer type should be Deployment",
				DataProducer.TYPE_DEPLOYMENT,
				auvDeployment.getDataProducerType());
		// TODO kgomes check rest of the attributes

		// Check the device
		assertNotNull("Deployment should have a device",
				auvDeployment.getDevice());

	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 * 
	 * @throws Exception
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		// Write your code here
	}

}