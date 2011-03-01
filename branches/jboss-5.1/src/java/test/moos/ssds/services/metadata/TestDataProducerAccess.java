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
package test.moos.ssds.services.metadata;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.Event;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.Software;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataFactory;
import moos.ssds.services.metadata.DataContainerAccess;
import moos.ssds.services.metadata.DataProducerAccess;
import moos.ssds.services.metadata.DeviceAccess;
import moos.ssds.services.metadata.EventAccess;
import moos.ssds.services.metadata.PersonAccess;
import moos.ssds.services.metadata.ResourceAccess;
import moos.ssds.services.metadata.SoftwareAccess;
import moos.ssds.util.XmlDateFormat;

import org.apache.log4j.Logger;

/**
 * This class tests the DataProducerAccess service EJB to make sure all is well.
 * There has to be an SSDS server running somewhere for this to hit against and
 * a jndi.properties in the classpath so the tests can get to the server
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.21 $
 */
public class TestDataProducerAccess extends TestAccessCase {

	// naming context
	Context context = null;

	// The connection to the service classes
	DataProducerAccess dataProducerAccess = null;

	// Other services
	DataContainerAccess dataContainerAccess = null;
	PersonAccess personAccess = null;
	DeviceAccess deviceAccess = null;
	SoftwareAccess softwareAccess = null;
	EventAccess eventAccess = null;
	ResourceAccess resourceAccess = null;

	// The test DataProducers
	DataProducer dataProducerOne = null;
	DataProducer dataProducerTwo = null;
	String dataProducerOneStringRep = "DataProducer|" + "name=DataProducerOne|"
			+ "description=DataProducerOne Description|" + "dataProducerType="
			+ DataProducer.TYPE_DEPLOYMENT + "|"
			+ "startDate=2003-05-05T16:11:44Z|"
			+ "endDate=2004-02-01T08:38:19Z|" + "role="
			+ DataProducer.ROLE_INSTRUMENT + "|" + "nominalLatitude=36.2311|"
			+ "nominalLongitude=-122.2344|" + "nominalDepth=10.5|"
			+ "xOffset=2.3|" + "yOffset=1.2|" + "zOffset=-2.3|"
			+ "orientationDescription=Really weird orientation|"
			+ "x3DOrientationText=<x3d/>|" + "hostName=myhost.mbari.org";
	String dataProducerTwoStringRep = "DataProducer|" + "name=DataProducerTwo|"
			+ "description=DataProducerTwo Description|" + "dataProducerType="
			+ DataProducer.TYPE_PROCESS_RUN + "|"
			+ "startDate=2004-01-05T16:11:44Z|"
			+ "endDate=2005-02-01T08:38:19Z|" + "role="
			+ DataProducer.ROLE_PLATFORM + "|" + "nominalLatitude=63.2311|"
			+ "nominalLongitude=-155.2344|" + "nominalDepth=2444.5|"
			+ "xOffset=0.0|" + "yOffset=0.1|" + "zOffset=-0.2|"
			+ "orientationDescription=Really weird orientation two|"
			+ "x3DOrientationText=<x3d/>|" + "hostName=myhost.two.mbari.org";

	// Some DataContainers
	DataContainer dataContainerOne = null;
	DataContainer dataContainerTwo = null;
	DataContainer dataContainerThree = null;
	String dataContainerOneStringRep = "DataContainer|"
			+ "name=DataContainerOne|"
			+ "description=DataContainerOne Description|"
			+ "dataContainerType=File|" + "startDate=2003-05-05T16:11:44Z|"
			+ "endDate=2004-02-01T08:38:19Z|" + "original=true|"
			+ "uriString=http://kasatka.shore.mbari.org/DataContainerOne.txt|"
			+ "contentLength=50000|" + "mimeType=CSV|" + "numberOfRecords=800|"
			+ "dodsAccessible=false|" + "minLatitude=36.6|"
			+ "maxLatitude=36.805|" + "minLongitude=-121.56|"
			+ "maxLongitude=-121.034|" + "minDepth=0.0|" + "maxDepth=10.546";
	String dataContainerTwoStringRep = "DataContainer|"
			+ "name=DataContainerTwo|"
			+ "description=DataContainerTwo Description|"
			+ "dataContainerType=Stream|" + "startDate=2005-01-01T00:00:00Z|"
			+ "endDate=2005-02-01T08:38:19Z|" + "original=true|"
			+ "uri=http://kasatka.shore.mbari.org/DataContainerTwo.xls|"
			+ "contentLength=1295|" + "mimeType=app/excel|"
			+ "numberOfRecords=10|" + "dodsAccessible=false|"
			+ "minLatitude=6.0|" + "maxLatitude=45.0|"
			+ "minLongitude=-255.34|" + "maxLongitude=-245.567|"
			+ "minDepth=10.5|" + "maxDepth=10000.5";
	String dataContainerThreeStringRep = "DataContainer|"
			+ "name=DataContainerThree|"
			+ "description=DataContainerThree Description|"
			+ "dataContainerType=File|" + "startDate=2004-07-02T00:00:00Z|"
			+ "endDate=2004-07-04T08:38:19Z|" + "original=true|"
			+ "uri=http://kasatka.shore.mbari.org/DataContainerThree.csv|"
			+ "contentLength=36|" + "mimeType=CSV|" + "numberOfRecords=1|"
			+ "dodsAccessible=false|" + "minLatitude=1.0|" + "maxLatitude=5.0|"
			+ "minLongitude=-55.34|" + "maxLongitude=-45.567|"
			+ "minDepth=1.5|" + "maxDepth=5.5";

	// Persons
	Person personOne = null;
	String personOneStringRep = "Person|" + "firstname=John|" + "surname=Doe|"
			+ "organization=MBARI|" + "email=jdoe@mbari.org|"
			+ "username=jdoe|" + "password=dumbPassword|" + "status=active";

	// A Device
	Device deviceOne = null;
	String deviceOneStringRep = "Device|" + "name=DeviceOne|"
			+ "uuid=1ebfc01e-4736-11da-b785-9b0c8eaaa128|"
			+ "description=Device One Description|" + "mfgName=DeviceOneMFG|"
			+ "mfgModel=DeviceOneMFGModel|" + "mfgSerialNumber=DeviceOneMFGSN|";

	// A Software
	Software softwareOne = null;
	String softwareStringRep = "Software|" + "name=SoftwareOne|"
			+ "softwareVersion=Version1.0|"
			+ "uriString=http://mbari.org/software/softwareone.html";

	// Some Events
	Event eventOne = null;
	Event eventTwo = null;
	String eventOneStringRep = "Event|" + "name=EventOne|"
			+ "description=EventOne Description|"
			+ "startDate=2004-07-02T01:00:00Z|"
			+ "endDate=2004-07-02T01:00:10Z";
	String eventTwoStringRep = "Event|" + "name=EventTwo|"
			+ "description=EventTwo Description|"
			+ "startDate=2004-07-04T09:00:00Z|"
			+ "endDate=2004-07-04T10:00:10Z";

	// Some Resources
	Resource resourceOne = null;
	Resource resourceTwo = null;
	String resourceOneStringRep = "Resource|" + "name=ResourceOne|"
			+ "description=ResourceOne Description|"
			+ "startDate=2003-01-02T01:00:00Z|"
			+ "endDate=2003-01-02T01:00:10Z|" + "mimeType=text/html|"
			+ "uriString=http://mbari.org/resources/resourceOne.html";
	String resourceTwoStringRep = "Resource|" + "name=ResourceTwo|"
			+ "description=ResourceTwo Description|"
			+ "startDate=2004-04-02T01:00:00Z|"
			+ "endDate=2004-04-02T01:00:10Z|" + "mimeType=text/html|"
			+ "uriString=http://mbari.org/resources/resourceTwo.html";

	String delimiter = "|";

	XmlDateFormat xmlDateFormat = new XmlDateFormat();

	/**
	 * A log4J logger
	 */
	static Logger logger = Logger.getLogger(TestDataProducerAccess.class);

	/**
	 * A constructor
	 * 
	 * @param name
	 */
	public TestDataProducerAccess(String name) {
		super(name);
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {

		// Setup the super class
		super.setUp();

		// Configure the logger
		// BasicConfigurator.configure();
		// logger.setLevel(Level.DEBUG);

		// Grab a dataProducer facade
		try {
			context = new InitialContext();
			dataProducerAccess = (DataProducerAccess) context
					.lookup("moos/ssds/services/metadata/DataProducerAccess");
			dataContainerAccess = (DataContainerAccess) context
					.lookup("moos/ssds/services/metadata/DataContainerAccess");
			personAccess = (PersonAccess) context
					.lookup("moos/ssds/services/metadata/PersonAccess");
			deviceAccess = (DeviceAccess) context
					.lookup("moos/ssds/services/metadata/DeviceAccess");
			softwareAccess = (SoftwareAccess) context
					.lookup("moos/ssds/services/metadata/SoftwareAccess");
			eventAccess = (EventAccess) context
					.lookup("moos/ssds/services/metadata/EventAccess");
			resourceAccess = (ResourceAccess) context
					.lookup("moos/ssds/services/metadata/ResourceAccess");
		} catch (NamingException e) {
			logger.error("RemoteException caught while creating access interfaces: "
					+ e.getMessage());
		}

	}

	/**
	 * Run suite of tests on dataProducer one
	 */

	public void testOne() {
		this.setupObjects();
		logger.debug("DataProducer one is "
				+ dataProducerOne.toStringRepresentation("|"));
		this.dataProducerTest(dataProducerOne);
		logger.debug("Done with test one");
		this.cleanObjectFromDB();
	}

	/**
	 * Run suite of tests on dataProducer two
	 */

	public void testTwo() {
		this.setupObjects();
		logger.debug("DataProducer two is "
				+ dataProducerTwo.toStringRepresentation("|"));
		dataProducerTest(dataProducerTwo);
		logger.debug("Done with test two");
		this.cleanObjectFromDB();
	}

	public void testUpdateOfTypeFails() {
		this.setupObjects();
		// Insert a Producer that has an original type of 'Deployment'
		Long dataProducerOneId = null;
		try {
			dataProducerOneId = dataProducerAccess.insert(dataProducerOne);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		// Make sure the returned ids are real
		assertNotNull(dataProducerOneId);
		logger.debug("DataProducerOneId = " + dataProducerOneId);

		// OK, now grab the persistent object back
		DataProducer persistedDataProducer = null;

		try {
			persistedDataProducer = (DataProducer) dataProducerAccess.findById(
					dataProducerOneId, false);
		} catch (MetadataAccessException e1) {
			logger.error("MetadataAccessException caught during findById: "
					+ e1.getMessage());
		}

		// Make sure something came back
		assertNotNull(persistedDataProducer);

		// Make sure it has the same ID
		assertEquals(
				"The persistent ID should match the one that was returned on insert",
				dataProducerOneId, persistedDataProducer.getId());
		// Make sure the type is deployment
		assertEquals("The DataProducer type should be 'Deployment'",
				persistedDataProducer.getDataProducerType(),
				DataProducer.TYPE_DEPLOYMENT);

		// Now change the dataProducerType to 'ProcessRun'
		try {
			persistedDataProducer
					.setDataProducerType(DataProducer.TYPE_PROCESS_RUN);
		} catch (MetadataException e) {
			logger.error("MetadataException caught during findById: "
					+ e.getMessage());
		}

		// Verify the set took
		assertEquals("The DataProducerType should now be ProcessRun",
				persistedDataProducer.getDataProducerType(),
				DataProducer.TYPE_PROCESS_RUN);

		// Update it in SSDS.
		try {
			dataProducerAccess.update(persistedDataProducer);

		} catch (MetadataAccessException e) {
			logger.error("MetadataAccessException caught during findById: "
					+ e.getMessage());
		}

		// Query for it again
		persistedDataProducer = null;
		try {
			persistedDataProducer = (DataProducer) dataProducerAccess.findById(
					dataProducerOneId, false);
		} catch (MetadataAccessException e1) {
			logger.error("MetadataAccessException caught during findById: "
					+ e1.getMessage());
		}

		// Now verify it came back
		assertNotNull(persistedDataProducer);

		// Verify that the returned value is back to Deployment
		assertEquals(
				"The udpate of dataProducerType should not have taken as SSDS prevents this.",
				persistedDataProducer.getDataProducerType(),
				DataProducer.TYPE_DEPLOYMENT);

		this.cleanObjectFromDB();
	}

	/**
	 * This test check the findByName method to see if the exact match and like
	 * matches work
	 */

	public void testFindByName() {
		this.setupObjects();
		logger.debug("testFindName starting");
		// We will insert both dataProducers and then see if we can find them
		// using the find by methods
		Long dataProducerOneId = null;
		Long dataProducerTwoId = null;
		try {
			dataProducerOneId = dataProducerAccess.insert(dataProducerOne);
			dataProducerTwoId = dataProducerAccess.insert(dataProducerTwo);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		// Make sure the returned ids are real
		assertNotNull(dataProducerOneId);
		assertNotNull(dataProducerTwoId);
		logger.debug("DataProducerOneId = " + dataProducerOneId);
		logger.debug("DataProducerTwoId = " + dataProducerTwoId);
		// Now let's try the find by name method. Both are named
		// "DataProducer###" First look for exact match of "DataProducer" and it
		// should come back empty
		Collection dataProducers = null;
		try {
			dataProducers = dataProducerAccess.findByName("DataProducer", true,
					null, null, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The returned collection should NOT be null",
				dataProducers);
		assertEquals("The returned collection should have zero items", 0,
				dataProducers.size());
		// Now find by like and I should get both
		dataProducers = null;
		try {
			dataProducers = dataProducerAccess.findByName("DataProducer",
					false, null, null, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The returned collection should not be null",
				dataProducers);
		assertEquals("The returned collection should have two items", 2,
				dataProducers.size());
		// Now query for exact one data container one
		dataProducers = null;
		try {
			dataProducers = dataProducerAccess.findByName("DataProducerOne",
					true, null, null, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The returned collection should not be null",
				dataProducers);
		assertEquals("The returned collection should have one item", 1,
				dataProducers.size());
		assertTrue(
				"The returned collection should contain the first data producer",
				dataProducers.contains(dataProducerOne));
		// Try a like with one match
		dataProducers = null;
		try {
			dataProducers = dataProducerAccess.findByName("DataProducerTw",
					false, null, null, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The returned collection should not be null",
				dataProducers);
		assertEquals("The returned collection should have one item", 1,
				dataProducers.size());
		assertTrue(
				"The returned collection should contain the second data producer",
				dataProducers.contains(dataProducerTwo));
		// Do some cleanup before the next test
		this.cleanObjectFromDB();
	}

	public void testCountFindByName() {
		this.setupObjects();
		logger.debug("testCountFindByName starting");
		// We will insert one dataProducers and then try the count methods
		Long dataProducerOneId = null;
		Long dataProducerTwoId = null;
		try {
			dataProducerOneId = dataProducerAccess.insert(dataProducerOne);
			dataProducerTwoId = dataProducerAccess.insert(dataProducerTwo);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		// Make sure the returned ids are real
		assertNotNull(dataProducerOneId);
		assertNotNull(dataProducerTwoId);
		logger.debug("DataProducerOneId = " + dataProducerOneId);
		logger.debug("DataProducerTwoId = " + dataProducerTwoId);

		int count = 0;

		try {
			count = dataProducerAccess.countFindByName("DataProducer", false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertTrue("There should be two with name like DataProducer",
				count == 2);
		try {
			count = dataProducerAccess.countFindByName("DataProducer", true);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertTrue("There should be zero with name exactly equal DataProducer",
				count == 0);

		try {
			count = dataProducerAccess.countFindByName("DataProducerOne", true);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertTrue(
				"There should be one with name exactly equal DataProducerOne",
				count == 1);
		try {
			count = dataProducerAccess.countFindByName("DataProducerT", false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertTrue("There should be one with name like DataProducerT",
				count == 1);

		// Do some cleanup before the next test
		this.cleanObjectFromDB();
	}

	public void testFindParentlessDeployments() {
		// Count the parentless deployments before
		int parentlessCount = 0;
		try {
			parentlessCount = dataProducerAccess
					.countFindParentlessDeployments();
		} catch (MetadataAccessException e1) {
			assertTrue("MetadataException was thrown: " + e1.getMessage(),
					false);
		}

		this.setupObjects();
		logger.debug("testFindParentlessDeployments starting");
		// We will insert dataProducers
		Long dataProducerOneId = null;
		Long dataProducerTwoId = null;
		try {
			dataProducerOneId = dataProducerAccess.insert(dataProducerOne);
			dataProducerTwoId = dataProducerAccess.insert(dataProducerTwo);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		// Make sure the returned ids are real
		assertNotNull(dataProducerOneId);
		assertNotNull(dataProducerTwoId);
		logger.debug("DataProducerOneId = " + dataProducerOneId);
		logger.debug("DataProducerTwoId = " + dataProducerTwoId);

		// Now get the parentless deployment and we should only get one
		Collection parentlessDeployments = null;
		try {
			parentlessDeployments = dataProducerAccess
					.findParentlessDeployments(null, null, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		// Should have one
		assertTrue(
				"There should be one more parentless deployment than before",
				parentlessDeployments.size() == (parentlessCount + 1));

		this.cleanObjectFromDB();
	}

	public void testCountFindParentlessDeployments() {
		logger.debug("testCountFindParentlessDeployments starting");
		this.setupObjects();

		// Grab the count of parentless deployments
		int count = 0;
		try {
			count = dataProducerAccess.countFindParentlessDeployments();

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		logger.debug("Initial count of parentless deployments is " + count);

		// Set the data producer type of the second one to a deployment
		try {
			dataProducerTwo.setDataProducerType(DataProducer.TYPE_DEPLOYMENT);
		} catch (MetadataException e1) {
			assertTrue("MetadataException was thrown: " + e1.getMessage(),
					false);
		}
		// We will insert dataProducers and then try the count method
		Long dataProducerOneId = null;
		Long dataProducerTwoId = null;
		try {
			dataProducerOneId = dataProducerAccess.insert(dataProducerOne);
			dataProducerTwoId = dataProducerAccess.insert(dataProducerTwo);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		// Make sure the returned ids are real
		assertNotNull(dataProducerOneId);
		assertNotNull(dataProducerTwoId);
		logger.debug("DataProducerOneId = " + dataProducerOneId);
		logger.debug("DataProducerTwoId = " + dataProducerTwoId);

		// Now, count the parentless and I should get two more
		int parentlessCount = 0;
		try {
			parentlessCount = dataProducerAccess
					.countFindParentlessDeployments();

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		logger.debug("After inserting two DataProducers, there are a total of "
				+ parentlessCount + " DataProducers");
		// Should have two more
		assertTrue("There should be two more parentless deployments",
				parentlessCount == (count + 2));

		// Now retrieve both and link one to the other as parent
		DataProducer persistOne = null;
		DataProducer persistTwo = null;

		try {
			persistOne = (DataProducer) dataProducerAccess.findById(
					dataProducerOneId, false);
			persistTwo = (DataProducer) dataProducerAccess.findById(
					dataProducerTwoId, false);
			dataProducerAccess.addChildDataProducer(persistOne, persistTwo);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		logger.debug("I have now added dataProducer two as a child of one.");

		// Now get count of parentless and there should only be one more
		parentlessCount = 0;
		try {
			parentlessCount = dataProducerAccess
					.countFindParentlessDeployments();

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		logger.debug("After changing heirarchy, there are " + parentlessCount
				+ " parentless DataProducers");
		assertEquals("There should only be one more parentless deployments",
				parentlessCount, (count + 1));

		// Since I changed the type of dataproducer two to deployment, I need to
		// delete it explicitly
		try {
			dataProducerAccess.makeTransient(dataProducerTwo);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		this.cleanObjectFromDB();
	}

	// public void testFindByDateRangeAndName() {
	// this.setupObjects();
	// // We will insert dataProducers and then try the count method
	// Long dataProducerOneId = null;
	// Long dataProducerTwoId = null;
	// try {
	// dataProducerOneId = dataProducerAccess.insert(dataProducerOne);
	// dataProducerTwoId = dataProducerAccess.insert(dataProducerTwo);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// // Make sure the returned ids are real
	// assertNotNull(dataProducerOneId);
	// assertNotNull(dataProducerTwoId);
	// logger.debug("DataProducerOneId = " + dataProducerOneId);
	// logger.debug("DataProducerTwoId = " + dataProducerTwoId);
	//
	// // The results collection
	// Collection results = null;
	//
	// // Here are the dates and names used of DPs
	// // DPOne
	// // name=DataContainerOne
	// // startDate=2003-05-05T16:11:44Z
	// // endDate=2004-02-01T08:38:19Z
	// // DPTwo
	// // name=DataContainerTwo
	// // startDate=2005-01-01T00:00:00Z
	// // endDate=2005-02-01T08:38:19Z
	//
	// // So, how to test the query?
	// // First let's test the startDate parameter only. If the startdate is
	// // set to 2005-03..., it should return nothing
	// Date startDate = xmlDateFormat.parse("2005-03-01T00:00:00Z");
	// results = null;
	// try {
	// results = dataProducerAccess.findByDateRangeAndName(startDate,
	// false, null, false, null, false, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	//
	// // The results should be empty
	// assertTrue("Results should not be null", results != null);
	// assertTrue("The results should be empty", results.size() == 0);
	//
	// // Now try a date that will return two, but not one
	// startDate = xmlDateFormat.parse("2005-01-20T00:00:00Z");
	// results = null;
	// try {
	// results = dataProducerAccess.findByDateRangeAndName(startDate,
	// false, null, false, null, false, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	//
	// // The results should have one
	// assertTrue("Results should not be null", results != null);
	// assertTrue("The results should have one", results.size() == 1);
	// assertTrue("It should be the second dataProducer", results
	// .contains(dataProducerTwo));
	//
	// // Now try a date that will return both
	// startDate = xmlDateFormat.parse("2003-06-20T00:00:00Z");
	// results = null;
	// try {
	// results = dataProducerAccess.findByDateRangeAndName(startDate,
	// false, null, false, null, false, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	//
	// // The results should have both
	// assertTrue("Results should not be null", results != null);
	// assertTrue("The results should have two", results.size() == 2);
	// assertTrue("It should have the first dataProducer", results
	// .contains(dataProducerOne));
	// assertTrue("It should have the second dataProducer", results
	// .contains(dataProducerTwo));
	//
	// // Now try to turn on the bounded boolean and it should go back to one
	// results = null;
	// try {
	// results = dataProducerAccess.findByDateRangeAndName(startDate,
	// true, null, false, null, false, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	//
	// // The results should have both
	// assertTrue("Results should not be null", results != null);
	// assertTrue("The results should have one", results.size() == 1);
	// assertTrue("It should NOT have the first dataProducer", !results
	// .contains(dataProducerOne));
	// assertTrue("It should have the second dataProducer", results
	// .contains(dataProducerTwo));
	//
	// // Move the start date some more and we should get both again
	// startDate = xmlDateFormat.parse("2003-02-20T00:00:00Z");
	// results = null;
	// try {
	// results = dataProducerAccess.findByDateRangeAndName(startDate,
	// true, null, false, null, false, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	//
	// // The results should have both
	// assertTrue("Results should not be null", results != null);
	// assertTrue("The results should have two", results.size() == 2);
	// assertTrue("It should have the first dataProducer", results
	// .contains(dataProducerOne));
	// assertTrue("It should have the second dataProducer", results
	// .contains(dataProducerTwo));
	//
	// // Now let's do the same, but with the end date
	//
	// // Move the start date some more and we should get both again
	// startDate = null;
	// Date endDate = xmlDateFormat.parse("2003-02-20T00:00:00Z");
	// results = null;
	// try {
	// results = dataProducerAccess.findByDateRangeAndName(null, false,
	// endDate, false, null, false, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	//
	// // The results should nothing
	// assertTrue("Results should not be null", results != null);
	// assertTrue("The results should be empty", results.size() == 0);
	//
	// // Move it to grab the first one
	// endDate = xmlDateFormat.parse("2003-06-20T00:00:00Z");
	// results = null;
	// try {
	// results = dataProducerAccess.findByDateRangeAndName(null, false,
	// endDate, false, null, false, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	//
	// // The results should nothing
	// assertTrue("Results should not be null", results != null);
	// assertTrue("The results should have one", results.size() == 1);
	// assertTrue("It should be dataproducer one", results
	// .contains(dataProducerOne));
	//
	// // Move it to grab both
	// endDate = xmlDateFormat.parse("2005-01-20T00:00:00Z");
	// results = null;
	// try {
	// results = dataProducerAccess.findByDateRangeAndName(null, false,
	// endDate, false, null, false, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	//
	// // The results contain both
	// assertTrue("Results should not be null", results != null);
	// assertTrue("The results should have two", results.size() == 2);
	// assertTrue("It should contain dataproducer one", results
	// .contains(dataProducerOne));
	// assertTrue("It should contain dataproducer two", results
	// .contains(dataProducerTwo));
	//
	// // Now flip the bounded flag and it should go back to just the first one
	// results = null;
	// try {
	// results = dataProducerAccess.findByDateRangeAndName(null, false,
	// endDate, true, null, false, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	//
	// // The results should contain only the first
	// assertTrue("Results should not be null", results != null);
	// assertTrue("The results should have one", results.size() == 1);
	// assertTrue("It should contain dataproducer one", results
	// .contains(dataProducerOne));
	// assertTrue("It should NOT contain dataproducer two", !results
	// .contains(dataProducerTwo));
	//
	// // Now move the date so even with the bounded flag, it gets both
	// endDate = xmlDateFormat.parse("2005-03-01T00:00:00Z");
	// results = null;
	// try {
	// results = dataProducerAccess.findByDateRangeAndName(null, false,
	// endDate, true, null, false, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	//
	// // The results contain both
	// assertTrue("Results should not be null", results != null);
	// assertTrue("The results should have two", results.size() == 2);
	// assertTrue("It should contain dataproducer one", results
	// .contains(dataProducerOne));
	// assertTrue("It should contain dataproducer two", results
	// .contains(dataProducerTwo));
	//
	// // Using the same idea, let's add in the name constraint and the exact
	// // match to only return the first one
	// String name = "DataProducerOne";
	// results = null;
	// try {
	// results = dataProducerAccess.findByDateRangeAndName(null, false,
	// endDate, true, name, true, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	//
	// // The results contain only the first
	// assertTrue("Results should not be null", results != null);
	// assertTrue("The results should have one", results.size() == 1);
	// assertTrue("It should contain dataproducer one", results
	// .contains(dataProducerOne));
	// assertTrue("It should NOT contain dataproducer two", !results
	// .contains(dataProducerTwo));
	//
	// // Now back the name down a bit and it should return nothing
	// name = "DataProducerO";
	// results = null;
	// try {
	// results = dataProducerAccess.findByDateRangeAndName(null, false,
	// endDate, true, name, true, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	//
	// // The results should be empty
	// assertTrue("Results should not be null", results != null);
	// assertTrue("The results should be empty", results.size() == 0);
	// assertTrue("It should NOT contain dataproducer one", !results
	// .contains(dataProducerOne));
	// assertTrue("It should NOT contain dataproducer two", !results
	// .contains(dataProducerTwo));
	//
	// // Turn on the like flag and I should get one back
	// results = null;
	// try {
	// results = dataProducerAccess.findByDateRangeAndName(null, false,
	// endDate, true, name, false, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	//
	// // The results contain only the first
	// assertTrue("Results should not be null", results != null);
	// assertTrue("The results contain one", results.size() == 1);
	// assertTrue("It should contain dataproducer one", results
	// .contains(dataProducerOne));
	// assertTrue("It should NOT contain dataproducer two", !results
	// .contains(dataProducerTwo));
	//
	// // Now back the name down a little more and you should get both again
	// name = "DataProducer";
	// results = null;
	// try {
	// results = dataProducerAccess.findByDateRangeAndName(null, false,
	// endDate, true, name, false, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	//
	// // The results contain both
	// assertTrue("Results should not be null", results != null);
	// assertTrue("The results contain two", results.size() == 2);
	// assertTrue("It should contain dataproducer one", results
	// .contains(dataProducerOne));
	// assertTrue("It should contain dataproducer two", results
	// .contains(dataProducerTwo));
	//
	// // Now clean up
	// this.cleanObjectFromDB();
	// }

	public void testInsertOfGraph() {
		this.setupObjects();
		// Now create the other entities
		dataProducerOne.addEvent(eventOne);
		dataProducerOne.addEvent(eventTwo);
		dataProducerOne.setDevice(deviceOne);
		dataProducerOne.setPerson(personOne);
		dataProducerOne.addResource(resourceOne);
		dataProducerOne.addResource(resourceTwo);
		dataProducerOne.setSoftware(softwareOne);

		// Now insert the data producer head
		Long dpId = null;
		try {
			dpId = dataProducerAccess.insert(dataProducerOne);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		assertNotNull(
				"The ID of the data producer after the long graph insert should not be null",
				dpId);

		// Now query back for it and let's walk the graph
		DataProducer persistentDataProducer = null;
		try {
			persistentDataProducer = (DataProducer) dataProducerAccess
					.findById(dpId, false);
			persistentDataProducer = (DataProducer) dataProducerAccess
					.getMetadataObjectGraph(persistentDataProducer);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Assure the return was not null
		assertNotNull("The persistent object with graph should not be null",
				persistentDataProducer);

		// Now check values
		assertEquals("The person should be the same", personOne,
				persistentDataProducer.getPerson());
		assertEquals("The device should be the same", deviceOne,
				persistentDataProducer.getDevice());
		assertEquals("The software should be the same", softwareOne,
				persistentDataProducer.getSoftware());
		assertTrue("The events should contain event one",
				persistentDataProducer.getEvents().contains(eventOne));
		assertTrue("The events should contain event two",
				persistentDataProducer.getEvents().contains(eventTwo));
		assertTrue("The resources should contain resource one",
				persistentDataProducer.getResources().contains(resourceOne));
		assertTrue("The resources should contain resource two",
				persistentDataProducer.getResources().contains(resourceTwo));
		assertTrue("The outputs should contain data container one",
				persistentDataProducer.getOutputs().contains(dataContainerOne));
		assertTrue("The outputs should contain data container one",
				persistentDataProducer.getOutputs().contains(dataContainerTwo));

		this.cleanObjectFromDB();
	}

	public void testChildDataProducer() {
		this.setupObjects();
		// Set the second data producer as a child of the first one
		dataProducerOne.addChildDataProducer(dataProducerTwo);

		// Now persist it
		try {
			dataProducerAccess.insert(dataProducerOne);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Now query for its child data producers
		int childCount = 0;
		try {
			childCount = dataProducerAccess
					.countFindChildDataProducers(dataProducerOne);
		} catch (MetadataAccessException e1) {
			assertTrue("MetadataException was thrown: " + e1.getMessage(),
					false);
		}
		assertEquals("There should be one child data producer", childCount, 1);

		Collection childDataProducers = null;
		try {
			childDataProducers = dataProducerAccess.findChildDataProducers(
					dataProducerOne, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		assertTrue("Child dataproducer should be there",
				childDataProducers.contains(dataProducerTwo));

		// Try to get the parent data producer from the first one and that
		// should be null
		DataProducer dpOneParent = null;
		try {
			dpOneParent = dataProducerAccess.findParentDataProducer(
					dataProducerOne, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		assertNull("The parent of the first data producer should be null",
				dpOneParent);

		// Now do it for the second and it should find the first
		DataProducer dpTwoParent = null;
		try {
			dpTwoParent = dataProducerAccess.findParentDataProducer(
					dataProducerTwo, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		assertEquals("The parent of two should be one", dpTwoParent,
				dataProducerOne);

		// Now clean up
		this.cleanObjectFromDB();
	}

	public void testMakeTransient() {
		this.setupObjects();
		// Now add some events, resource, people and devices
		dataProducerOne.setDevice(deviceOne);
		dataProducerOne.setPerson(personOne);
		dataProducerOne.addEvent(eventOne);
		dataProducerOne.addEvent(eventTwo);
		dataProducerOne.addResource(resourceOne);
		dataProducerOne.addResource(resourceTwo);

		// insert them
		try {
			dataProducerAccess.insert(dataProducerOne);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Now look it up again
		DataProducer persistentDPOne = null;
		try {
			persistentDPOne = (DataProducer) dataProducerAccess
					.getMetadataObjectGraph(dataProducerOne);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Now make sure device took in persistence
		assertNotNull("DPOne after query should not be null", persistentDPOne);
		assertNotNull("Returned device should not be null",
				persistentDPOne.getDevice());
		assertEquals("Two devices should be equal",
				persistentDPOne.getDevice(), deviceOne);
		assertNotNull("Returned person should not be null",
				persistentDPOne.getPerson());
		assertEquals("Two people should be equal", persistentDPOne.getPerson(),
				personOne);

		// Make sure it contains the resource
		assertTrue("DPOne should have resource one", persistentDPOne
				.getResources().contains(resourceOne));

		// Make sure DPTwo has events and resource
		assertTrue("DPOne should have two events", persistentDPOne.getEvents()
				.size() == 2);
		assertTrue("DPOne should have event one", persistentDPOne.getEvents()
				.contains(eventOne));
		assertTrue("DPOne should have event two", persistentDPOne.getEvents()
				.contains(eventTwo));
		assertTrue("DPOne should have resource two", persistentDPOne
				.getResources().contains(resourceTwo));

		// Now do deep transient of head object
		try {
			dataProducerAccess.makeTransient(dataProducerOne);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Now look it up again
		persistentDPOne = null;
		try {
			persistentDPOne = (DataProducer) dataProducerAccess
					.getMetadataObjectGraph(dataProducerOne);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNull("persistentDPOne should not have been found",
				persistentDPOne);

		// Check the relationships
		Device persistentDevice = null;
		Person persistentPerson = null;
		DataContainer persistentDataContainerOne = null;
		DataContainer persistentDataContainerTwo = null;
		Event persistentEventOne = null;
		Event persistentEventTwo = null;
		Resource persistentResourceOne = null;
		Resource persistentResourceTwo = null;

		try {
			persistentDevice = (Device) deviceAccess
					.findEquivalentPersistentObject(deviceOne, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		try {
			persistentPerson = (Person) personAccess
					.findEquivalentPersistentObject(personOne, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		try {
			persistentDataContainerOne = (DataContainer) dataContainerAccess
					.findEquivalentPersistentObject(dataContainerOne, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		try {
			persistentDataContainerTwo = (DataContainer) dataContainerAccess
					.findEquivalentPersistentObject(dataContainerTwo, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		try {
			persistentEventOne = (Event) eventAccess
					.findEquivalentPersistentObject(eventOne, false);
			persistentEventTwo = (Event) eventAccess
					.findEquivalentPersistentObject(eventTwo, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		try {
			persistentResourceOne = (Resource) resourceAccess
					.findEquivalentPersistentObject(resourceOne, false);
			persistentResourceTwo = (Resource) resourceAccess
					.findEquivalentPersistentObject(resourceTwo, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("Device should have been found", persistentDevice);
		assertNotNull("Person should have been found", persistentPerson);
		assertNotNull("DataContainerOne should have been found",
				persistentDataContainerOne);
		assertNotNull("DataContainerTwo should have been found",
				persistentDataContainerTwo);
		assertNotNull("EventOne should have been found", persistentEventOne);
		assertNotNull("EventTwo should have been found", persistentEventTwo);
		assertNotNull("ResourceOne should have been found",
				persistentResourceOne);
		assertNotNull("ResourceTwo should have been found",
				persistentResourceTwo);

		this.cleanObjectFromDB();
	}

	public void testMakeDeepTransient() {
		this.setupObjects();
		// Now attach the second DP to the first
		dataProducerOne.addChildDataProducer(dataProducerTwo);

		// Now add some events, resource, people and devices
		dataProducerOne.setDevice(deviceOne);
		dataProducerTwo.setPerson(personOne);
		dataProducerTwo.addEvent(eventOne);
		dataProducerTwo.addEvent(eventTwo);
		dataProducerOne.addResource(resourceOne);
		dataProducerTwo.addResource(resourceTwo);

		// insert them
		try {
			dataProducerAccess.insert(dataProducerOne);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Now look it up again
		DataProducer persistentDPOne = null;
		try {
			persistentDPOne = (DataProducer) dataProducerAccess
					.getMetadataObjectGraph(dataProducerOne);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Now make sure device took in persistence
		assertNotNull("DPOne after query should not be null", persistentDPOne);
		assertNotNull("Returned device should not be null",
				persistentDPOne.getDevice());
		assertEquals("Two devices should be equal",
				persistentDPOne.getDevice(), deviceOne);

		// Make sure it contains the resource
		assertTrue("DPOne should have resource one", persistentDPOne
				.getResources().contains(resourceOne));

		// Now grab the child DP and make sure those relationships were
		// persisted
		DataProducer persistentDPTwo = null;
		try {
			persistentDPTwo = (DataProducer) dataProducerAccess
					.getMetadataObjectGraph(dataProducerTwo);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("DPTwo after query should not be null", persistentDPTwo);
		assertNotNull("Returned person should not be null",
				persistentDPTwo.getPerson());
		assertEquals("Two people should be equal", persistentDPTwo.getPerson(),
				personOne);

		// Make sure DPTwo has events and resource
		assertTrue("DPTwo should have two events", persistentDPTwo.getEvents()
				.size() == 2);
		assertTrue("DPTwo should have event one", persistentDPTwo.getEvents()
				.contains(eventOne));
		assertTrue("DPTwo should have event two", persistentDPTwo.getEvents()
				.contains(eventTwo));
		assertTrue("DPTwo should have resource two", persistentDPTwo
				.getResources().contains(resourceTwo));

		// Now do deep transient of head object
		try {
			dataProducerAccess.makeDeepTransient(dataProducerOne);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Now look it up again
		persistentDPOne = null;
		try {
			persistentDPOne = (DataProducer) dataProducerAccess
					.getMetadataObjectGraph(dataProducerOne);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNull("persistentDPOne should not have been found",
				persistentDPOne);
		persistentDPTwo = null;
		try {
			persistentDPTwo = (DataProducer) dataProducerAccess
					.getMetadataObjectGraph(dataProducerTwo);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNull("persistentDPTwo should not have been found",
				persistentDPTwo);

		// Check the relationships
		Device persistentDevice = null;
		Person persistentPerson = null;
		DataContainer persistentDataContainerOne = null;
		DataContainer persistentDataContainerTwo = null;
		DataContainer persistentDataContainerThree = null;
		Event persistentEventOne = null;
		Event persistentEventTwo = null;
		Resource persistentResourceOne = null;
		Resource persistentResourceTwo = null;

		try {
			persistentDevice = (Device) deviceAccess
					.findEquivalentPersistentObject(deviceOne, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		try {
			persistentPerson = (Person) personAccess
					.findEquivalentPersistentObject(personOne, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		try {
			persistentDataContainerOne = (DataContainer) dataContainerAccess
					.findEquivalentPersistentObject(dataContainerOne, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		try {
			persistentDataContainerTwo = (DataContainer) dataContainerAccess
					.findEquivalentPersistentObject(dataContainerTwo, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		try {
			persistentDataContainerThree = (DataContainer) dataContainerAccess
					.findEquivalentPersistentObject(dataContainerThree, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		try {
			persistentEventOne = (Event) eventAccess
					.findEquivalentPersistentObject(eventOne, false);
			persistentEventTwo = (Event) eventAccess
					.findEquivalentPersistentObject(eventTwo, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		try {
			persistentResourceOne = (Resource) resourceAccess
					.findEquivalentPersistentObject(resourceOne, false);
			persistentResourceTwo = (Resource) resourceAccess
					.findEquivalentPersistentObject(resourceTwo, false);

		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("Device should have been found", persistentDevice);
		assertNotNull("Person should have been found", persistentPerson);
		assertNull("DataContainerOne should not have been found",
				persistentDataContainerOne);
		assertNull("DataContainerTwo should not have been found",
				persistentDataContainerTwo);
		assertNull("DataContainerThree should not have been found",
				persistentDataContainerThree);
		assertNull("EventOne should not have been found", persistentEventOne);
		assertNull("EventTwo should not have been found", persistentEventTwo);
		assertNull("ResourceOne should not have been found",
				persistentResourceOne);
		assertNull("ResourceTwo should not have been found",
				persistentResourceTwo);

		this.cleanObjectFromDB();
	}

	/**
	 * This is the suite of tests to run on a dataProducer
	 * 
	 * @param dataProducer
	 */
	private void dataProducerTest(DataProducer dataProducer) {

		// The ID of the dataProducer
		Long dataProducerId = null;
		dataProducerId = testInsert(dataProducer, dataProducerAccess);

		// Now query back by ID and make sure all attributes are equal
		DataProducer persistedDataProducer = null;

		try {
			persistedDataProducer = (DataProducer) dataProducerAccess.findById(
					dataProducerId, false);
		} catch (MetadataAccessException e1) {
			logger.error("MetadataAccessException caught during findById: "
					+ e1.getMessage());
		}

		// Now check that they are equal
		assertEquals("The two dataProducers should be considered equal",
				dataProducer, persistedDataProducer);

		// Check all the getter methods are equal
		testEqualityOfAllGetters(dataProducer, persistedDataProducer);

		// Create a map with the values to update
		HashMap variablesToUpdate = new HashMap();

		// Change the surname
		Object[] variable1 = new Object[1];
		variable1[0] = new String("Updated Description");
		variablesToUpdate.put("Description", variable1);

		// Change the status
		Object[] variable2 = new Object[1];
		variable2[0] = new String("Updated Orientation Description");
		variablesToUpdate.put("OrientationDescription", variable2);

		testUpdate(persistedDataProducer, variablesToUpdate, dataProducerAccess);

		testDelete(persistedDataProducer, dataProducerAccess);
	}

	/**
	 * This method instantiates clean data producers and data containers from
	 * the string representations
	 */
	private void setupObjects() {
		try {
			dataProducerOne = (DataProducer) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							dataProducerOneStringRep, delimiter);
			dataProducerTwo = (DataProducer) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							dataProducerTwoStringRep, delimiter);
			dataContainerOne = (DataContainer) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							dataContainerOneStringRep, delimiter);
			dataContainerTwo = (DataContainer) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							dataContainerTwoStringRep, delimiter);
			dataContainerThree = (DataContainer) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							dataContainerThreeStringRep, delimiter);
			personOne = (Person) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							personOneStringRep, delimiter);
			deviceOne = (Device) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							deviceOneStringRep, delimiter);
			softwareOne = (Software) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							softwareStringRep, delimiter);
			eventOne = (Event) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							eventOneStringRep, delimiter);
			eventTwo = (Event) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							eventTwoStringRep, delimiter);
			resourceOne = (Resource) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							resourceOneStringRep, delimiter);
			resourceTwo = (Resource) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							resourceTwoStringRep, delimiter);
		} catch (MetadataException e) {
			logger.error("MetadataException caught trying to create objects: "
					+ e.getMessage());
		} catch (ClassCastException cce) {
			logger.error("ClassCastException caught trying to create objects: "
					+ cce.getMessage());
		}
		// Add the outputs
		dataProducerOne.addOutput(dataContainerOne);
		dataProducerOne.addOutput(dataContainerTwo);
		dataProducerTwo.addOutput(dataContainerThree);
	}

	private void cleanObjectFromDB() {
		this.setupObjects();
		// Delete any dataProducer objects as we don't want any leftover's if a
		// test fails
		try {
			dataProducerAccess.delete(dataProducerOne);

		} catch (MetadataAccessException e) {
		}
		try {
			dataProducerAccess.delete(dataProducerTwo);

		} catch (MetadataAccessException e) {
		}
		try {
			dataContainerAccess.delete(dataContainerOne);

		} catch (MetadataAccessException e) {
		}
		try {
			dataContainerAccess.delete(dataContainerTwo);

		} catch (MetadataAccessException e) {
		}
		try {
			dataContainerAccess.delete(dataContainerThree);

		} catch (MetadataAccessException e) {
		}
		try {
			personAccess.delete(personOne);

		} catch (MetadataAccessException e) {
		}
		try {
			deviceAccess.delete(deviceOne);

		} catch (MetadataAccessException e) {
		}
		try {
			softwareAccess.delete(softwareOne);

		} catch (MetadataAccessException e) {
		}
		try {
			eventAccess.delete(eventOne);

		} catch (MetadataAccessException e) {
		}
		try {
			eventAccess.delete(eventTwo);

		} catch (MetadataAccessException e) {
		}
		try {
			resourceAccess.delete(resourceOne);

		} catch (MetadataAccessException e) {
		}
		try {
			resourceAccess.delete(resourceTwo);

		} catch (MetadataAccessException e) {
		}
		// Do a deep delete on any DataProducers with name "DataProducer%"
		Collection dataProducersToDelete = null;
		try {
			dataProducersToDelete = dataProducerAccess.findByName(
					"DataProducer", false, null, null, false);

		} catch (MetadataAccessException e) {
		}
		Iterator deleteIter = dataProducersToDelete.iterator();
		while (deleteIter.hasNext()) {
			DataProducer dpToDeepDelete = (DataProducer) deleteIter.next();
			try {
				dataProducerAccess.deepDelete(dpToDeepDelete);

			} catch (MetadataAccessException e) {
			}
		}

	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
		this.cleanObjectFromDB();
	}

}