package test.moos.ssds.metadata;

import java.io.File;
import java.util.Collection;

import junit.framework.TestCase;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.DataProducerGroup;
import moos.ssds.metadata.util.ObjectBuilder;

import org.apache.log4j.Logger;

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
	 * object, checks the attributes.
	 */
	public void testXMLBinding() {
		// The name of the XML file to test
		String xmlFileName = "DataProducerGroup";

		// Grab the file that has the XML in it
		File xmlFile = new File("src" + File.separator + "resources"
				+ File.separator + "test" + File.separator + "xml"
				+ File.separator + xmlFileName + ".xml");
		if (!xmlFile.exists())
			assertTrue("Could not find xml file file for testing.", false);
		logger.debug("Will read XML from " + xmlFile.getAbsolutePath());

		// Create the ObjectBuilder and unmarshall to objects
		ObjectBuilder objectBuilder = null;
		try {
			objectBuilder = new ObjectBuilder(xmlFile.toURI().toURL());
			objectBuilder.unmarshal();
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
			assertTrue("Could not unmarshal XML file: " + e.getMessage(), false);
		}

		// Make sure object builder is there
		assertNotNull("ObjectBuilder should not be null", objectBuilder);

		// Grab all the top level objects
		Collection<Object> allObjects = objectBuilder.listAll();

		// Makes sure it is not null
		assertNotNull("allObject should not be null", allObjects);

		// Make sure there is one object at the top
		assertEquals("There should be only one object", 1, allObjects.size());

		// Should be of class DataProducerGroup
		Object dataProducerGroupObject = allObjects.iterator().next();

		// Make sure it is not null
		assertNotNull("Object should not be null", dataProducerGroupObject);

		// Make sure it is a data producer group
		assertTrue("Should be DataProducerGroup",
				dataProducerGroupObject instanceof DataProducerGroup);

		// Cast it
		DataProducerGroup testDataProducerGroup = (DataProducerGroup) dataProducerGroupObject;

		// Check the attributes
		assertEquals("ID should be 1", 1L, testDataProducerGroup.getId()
				.longValue());
		assertEquals("Name should be Test DataProducerGroup",
				"Test DataProducerGroup", testDataProducerGroup.getName());
		assertEquals("Descriptions should be equal",
				"Test DataProducerGroup Description as element",
				testDataProducerGroup.getDescription());
	}

	/**
	 * This test takes a MetadataObject defined in XML, converts it to an
	 * object, checks the attributes
	 */
	public void testXMLBindingDescriptionAsAttribute() {
		// The name of the XML file to test
		String xmlFileName = "DataProducerGroupDescriptionAsAttribute";

		// Grab the file that has the XML in it
		File xmlFile = new File("src" + File.separator + "resources"
				+ File.separator + "test" + File.separator + "xml"
				+ File.separator + xmlFileName + ".xml");
		if (!xmlFile.exists())
			assertTrue("Could not find xml file file for testing.", false);
		logger.debug("Will read XML from " + xmlFile.getAbsolutePath());

		// Create the ObjectBuilder and unmarshall to objects
		ObjectBuilder objectBuilder = null;
		try {
			objectBuilder = new ObjectBuilder(xmlFile.toURI().toURL());
			objectBuilder.unmarshal();
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
			assertTrue("Could not unmarshal XML file: " + e.getMessage(), false);
		}

		// Make sure object builder is there
		assertNotNull("ObjectBuilder should not be null", objectBuilder);

		// Grab all the top level objects
		Collection<Object> allObjects = objectBuilder.listAll();

		// Makes sure it is not null
		assertNotNull("allObject should not be null", allObjects);

		// Make sure there is one object at the top
		assertEquals("There should be only one object", 1, allObjects.size());

		// Should be of class DataProducerGroup
		Object dataProducerGroupObject = allObjects.iterator().next();

		// Make sure it is not null
		assertNotNull("Object should not be null", dataProducerGroupObject);

		// Make sure it is a data producer group
		assertTrue("Should be DataProducerGroup",
				dataProducerGroupObject instanceof DataProducerGroup);

		// Cast it
		DataProducerGroup testDataProducerGroup = (DataProducerGroup) dataProducerGroupObject;

		// Check the attributes
		assertEquals("ID should be 1", 1L, testDataProducerGroup.getId()
				.longValue());
		assertEquals("Name should be Test DataProducerGroup",
				"Test DataProducerGroup", testDataProducerGroup.getName());
		assertEquals("Descriptions should be equal",
				"TestDataProducerGroup Description as attribute",
				testDataProducerGroup.getDescription());
	}

	/**
	 * This test loads XML that has a DataProducerGroup with a child Deployment
	 * and checks the flip
	 */
	public void testDataProducerGroupDataProducerFlip() {
		// The name of the XML file to test
		String xmlFileName = "DataProducerGroupWithDataProducer";

		// Grab the file that has the XML in it
		File xmlFile = new File("src" + File.separator + "resources"
				+ File.separator + "test" + File.separator + "xml"
				+ File.separator + xmlFileName + ".xml");
		if (!xmlFile.exists())
			assertTrue("Could not find xml file file for testing.", false);
		logger.debug("Will read XML from " + xmlFile.getAbsolutePath());

		// Create the ObjectBuilder and unmarshall to objects
		ObjectBuilder objectBuilder = null;
		try {
			objectBuilder = new ObjectBuilder(xmlFile.toURI().toURL());
			objectBuilder.unmarshal();
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
			assertTrue("Could not unmarshal XML file: " + e.getMessage(), false);
		}

		// Make sure object builder is there
		assertNotNull("ObjectBuilder should not be null", objectBuilder);

		// Grab all the top level objects
		Collection<Object> allObjects = objectBuilder.listAll();

		// Makes sure it is not null
		assertNotNull("allObject should not be null", allObjects);

		// Make sure there is one object at the top
		assertEquals("There should be only one object", 1, allObjects.size());

		// Should be of class DataProducer
		Object dataProducerObject = allObjects.iterator().next();

		// Make sure it is not null
		assertNotNull("Object should not be null", dataProducerObject);

		// Make sure it is a data producer
		assertTrue("Should be DataProducer",
				dataProducerObject instanceof DataProducer);

		// Cast it
		DataProducer testDataProducer = (DataProducer) dataProducerObject;

		// Check the attributes
		assertEquals("ID should be 2", 2L, testDataProducer.getId().longValue());
		assertEquals("Name should be Test Deployment under DataProducerGroup",
				"Test Deployment under DataProducerGroup",
				testDataProducer.getName());
		assertEquals("DataProducer should be deployment",
				DataProducer.TYPE_DEPLOYMENT,
				testDataProducer.getDataProducerType());
		assertEquals("role should be platform", "platform",
				testDataProducer.getRole());

		// Now should have data producer group
		assertNotNull("DataProducer should have a DataProducerGroup",
				testDataProducer.getDataProducerGroups());
		assertEquals("Should have one DataProducerGroup", 1, testDataProducer
				.getDataProducerGroups().size());
	}
}
