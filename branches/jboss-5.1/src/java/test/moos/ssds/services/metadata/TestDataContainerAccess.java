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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataContainerGroup;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.Keyword;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.RecordDescription;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.ResourceBLOB;
import moos.ssds.metadata.ResourceType;
import moos.ssds.metadata.StandardUnit;
import moos.ssds.metadata.StandardVariable;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataFactory;
import moos.ssds.services.metadata.IDataContainerAccess;
import moos.ssds.services.metadata.IDataContainerAccess;
import moos.ssds.services.metadata.IDataContainerAccess;
import moos.ssds.services.metadata.DataContainerGroupAccess;
import moos.ssds.services.metadata.DataContainerGroupAccessHome;
import moos.ssds.services.metadata.DataContainerGroupAccessUtil;
import moos.ssds.services.metadata.DataProducerAccess;
import moos.ssds.services.metadata.DataProducerAccessHome;
import moos.ssds.services.metadata.DataProducerAccessUtil;
import moos.ssds.services.metadata.KeywordAccess;
import moos.ssds.services.metadata.KeywordAccessHome;
import moos.ssds.services.metadata.KeywordAccessUtil;
import moos.ssds.services.metadata.PersonAccess;
import moos.ssds.services.metadata.PersonAccessHome;
import moos.ssds.services.metadata.PersonAccessUtil;
import moos.ssds.services.metadata.ResourceAccess;
import moos.ssds.services.metadata.ResourceAccessHome;
import moos.ssds.services.metadata.ResourceAccessUtil;
import moos.ssds.services.metadata.ResourceTypeAccess;
import moos.ssds.services.metadata.ResourceTypeAccessHome;
import moos.ssds.services.metadata.ResourceTypeAccessUtil;
import moos.ssds.services.metadata.StandardUnitAccess;
import moos.ssds.services.metadata.StandardUnitAccessHome;
import moos.ssds.services.metadata.StandardUnitAccessUtil;
import moos.ssds.services.metadata.StandardVariableAccess;
import moos.ssds.services.metadata.StandardVariableAccessHome;
import moos.ssds.services.metadata.StandardVariableAccessUtil;
import moos.ssds.util.XmlDateFormat;

import org.apache.log4j.Logger;

/**
 * This class tests the DataContainerAccess service EJB to make sure all is
 * well. There has to be an SSDS server running somewhere for this to hit
 * against and a jndi.properties in the classpath so the tests can get to the
 * server
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.31 $
 */
public class TestDataContainerAccess extends TestAccessCase {

	/**
	 * A constructor
	 * 
	 * @param name
	 */
	public TestDataContainerAccess(String name) {
		super(name);
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {

		// Setup the super class
		super.setUp();

		// Grab a dataContainer facade
		try {
			dataContainerAccessHome = DataContainerAccessUtil.getHome();
			dataProducerAccessHome = DataProducerAccessUtil.getHome();
			standardUnitAccessHome = StandardUnitAccessUtil.getHome();
			personAccessHome = PersonAccessUtil.getHome();
			standardVariableAccessHome = StandardVariableAccessUtil.getHome();
			dataContainerGroupAccessHome = DataContainerGroupAccessUtil
					.getHome();
			keywordAccessHome = KeywordAccessUtil.getHome();
			resourceAccessHome = ResourceAccessUtil.getHome();
			resourceTypeAccessHome = ResourceTypeAccessUtil.getHome();
		} catch (NamingException ex) {
			logger
					.error("NamingException caught while getting access homes from app server: "
							+ ex.getMessage());
		}
		try {
			dataContainerAccess = dataContainerAccessHome.create();
			dataProducerAccess = dataProducerAccessHome.create();
			standardUnitAccess = standardUnitAccessHome.create();
			personAccess = personAccessHome.create();
			standardVariableAccess = standardVariableAccessHome.create();
			dataContainerGroupAccess = dataContainerGroupAccessHome.create();
			keywordAccess = keywordAccessHome.create();
			resourceAccess = resourceAccessHome.create();
			resourceTypeAccess = resourceTypeAccessHome.create();
		} catch (RemoteException e) {
			logger
					.error("RemoteException caught while creating access interface: "
							+ e.getMessage());
		} catch (CreateException e) {
			logger
					.error("CreateException caught while creating access interface: "
							+ e.getMessage());
		}

	}

	/**
	 * Run suite of tests on dataContainer one
	 */
	public void testOne() {
		try {
			dataContainerOne = (DataContainer) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							dataContainerOneStringRep, delimiter);
		} catch (MetadataException e) {
			logger
					.error("MetadataException caught trying to create two DataContainer objects: "
							+ e.getMessage());
		} catch (ClassCastException cce) {
			logger
					.error("ClassCastException caught trying to create two DataContainer objects: "
							+ cce.getMessage());
		}
		logger.debug("DataContainer one is "
				+ dataContainerOne.toStringRepresentation("|"));
		this.dataContainerTest(dataContainerOne);
		logger.debug("Done with test one");
	}

	/**
	 * Run suite of tests on dataContainer two
	 */
	public void testTwo() {
		try {
			dataContainerTwo = (DataContainer) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							dataContainerOneStringRep, delimiter);
		} catch (MetadataException e) {
			logger
					.error("MetadataException caught trying to create two DataContainer objects: "
							+ e.getMessage());
		} catch (ClassCastException cce) {
			logger
					.error("ClassCastException caught trying to create two DataContainer objects: "
							+ cce.getMessage());
		}
		logger.debug("DataContainer two is "
				+ dataContainerTwo.toStringRepresentation("|"));
		dataContainerTest(dataContainerTwo);
		logger.debug("Done with test two");
	}

	/**
	 * This test check the findByName method to see if the exact match and like
	 * matches work
	 */
	public void testFindByName() {
		this.setupObjects();
		logger.debug("testFindName starting");

		// Now since the setupObjects method attached the data containers to a
		// DataProducer, I want to break that so I can just work with the
		// DataContainers. If I don't do this, I will get an exception while
		// inserting the DC because the creator was not inserted first.
		dataProducerOne.clearOutputs();
		dataProducerTwo.clearOutputs();

		// We will insert both dataContainers and then see if we can find them
		// using the find by methods
		Long dataContainerOneId = null;
		Long dataContainerTwoId = null;
		try {
			dataContainerOneId = dataContainerAccess.insert(dataContainerOne);
			dataContainerTwoId = dataContainerAccess.insert(dataContainerTwo);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Make sure the returned ids are real
		assertNotNull(dataContainerOneId);
		assertNotNull(dataContainerTwoId);
		logger.debug("DataContainerOneId = " + dataContainerOneId);
		logger.debug("DataContainerTwoId = " + dataContainerTwoId);

		// Now let's try the find by name method. Both are named
		// "DataContainer###"
		// First look for exact match of "DataContainer" and it should come back
		// empty
		Collection dataContainers = null;
		try {
			dataContainers = dataContainerAccess.findByName("DataContainer",
					true, "name", null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The returned collection should be null", dataContainers);
		assertEquals("The returned collection should have zero items", 0,
				dataContainers.size());

		// Now find by like and I should get both
		dataContainers = null;
		try {
			dataContainers = dataContainerAccess.findByName("DataContainer",
					false, "name", null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The returned collection should not be null",
				dataContainers);
		assertEquals("The returned collection should have two items", 2,
				dataContainers.size());

		// Now query for exact one data container one
		dataContainers = null;
		try {
			dataContainers = dataContainerAccess.findByName("DataContainerOne",
					true, "name", null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The returned collection should not be null",
				dataContainers);
		assertEquals("The returned collection should have one item", 1,
				dataContainers.size());

		// Try a like with one match
		dataContainers = null;
		try {
			dataContainers = dataContainerAccess.findByName("DataContainerOn",
					false, "name", null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The returned collection should not be null",
				dataContainers);
		assertEquals("The returned collection should have one item", 1,
				dataContainers.size());

		// Do some cleanup before the next test
		this.cleanObjectsFromDB();
	}

	/**
	 * This method checks all the "findBy*" methods
	 */
	public void testFindBys() {
		this.setupObjects();
		logger.debug("testFindBys starting");

		// Now since the setupObjects method attached the data containers to a
		// DataProducer, I want to break that so I can just work with the
		// DataContainers. If I don't do this, I will get an exception while
		// inserting the DC because the creator was not inserted first.
		dataProducerOne.clearOutputs();
		dataProducerTwo.clearOutputs();

		// We will insert both dataContainers and then see if we can find them
		// using
		// the find by methods
		Long dataContainerOneId = null;
		Long dataContainerTwoId = null;
		try {
			dataContainerOneId = dataContainerAccess.insert(dataContainerOne);
			dataContainerTwoId = dataContainerAccess.insert(dataContainerTwo);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Make sure the returned ids are real
		assertNotNull(dataContainerOneId);
		assertNotNull(dataContainerTwoId);
		logger.debug("DataContainerOneId = " + dataContainerOneId);
		logger.debug("DataContainerTwoId = " + dataContainerTwoId);

		// OK both should be inserted. Excercise some of the findBys
		DataContainer foundOne = null;
		try {
			foundOne = (DataContainer) dataContainerAccess.findById(
					dataContainerOneId, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The findById(Long) should have returned something",
				foundOne);
		assertEquals(
				"The foundOne (by Long) should be equal to the local dataContainerOne",
				foundOne, dataContainerOne);

		// Clear and try with different find by
		foundOne = null;
		try {
			foundOne = (DataContainer) dataContainerAccess.findById(
					dataContainerOneId.longValue(), false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The findById(long) should have returned something",
				foundOne);
		assertEquals(
				"The foundOne (by long) should be equal to the local dataContainerOne",
				foundOne, dataContainerOne);

		// Clear and try again
		foundOne = null;
		try {
			foundOne = (DataContainer) dataContainerAccess.findById(
					dataContainerOneId.toString(), false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The findById(String) should have returned something",
				foundOne);
		assertEquals(
				"The foundOne (by String) should be equal to the local dataContainerOne",
				foundOne, dataContainerOne);

		// OK, find by local dataContainer and check against ID
		Long foundOneId = null;
		try {
			foundOneId = dataContainerAccess.findId(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("foundOneId should not be null", foundOneId);
		assertEquals("ID from insert and returned ID should be the same.",
				foundOneId.longValue(), dataContainerOneId.longValue());

		// Now check find all
		// KGomes -- Commented this out as it takes WAY too long, but does work.
		// Collection allDataContainers = null;
		// try {
		// allDataContainers = dataContainerAccess.findAll(null, false);
		// } catch (RemoteException e) {
		// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		// } catch (MetadataAccessException e) {
		// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		// }
		// assertNotNull("Collection returned by findAll should not be null",
		// allDataContainers);
		// assertTrue("Collection should contain local dataContainer one",
		// allDataContainers.contains(dataContainerOne));
		// assertTrue("Collection should contain local dataContainer two",
		// allDataContainers.contains(dataContainerTwo));
		// logger.debug("allDataContainers = " + allDataContainers);
		//
		// Now test the findEquivalent persistent object
		DataContainer persistentOne = null;
		try {
			Long equivalentId = dataContainerAccess.findId(dataContainerOne);
			persistentOne = (DataContainer) dataContainerAccess.findById(
					equivalentId, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull(
				"persistent object that matches dataContainerOne should not be null",
				persistentOne);
		assertEquals(
				"Persistent and local objects for dataContainerOne should be equal",
				persistentOne, dataContainerOne);
		assertEquals("The inserted ID and the persistent ID should be equal",
				dataContainerOneId.longValue(), persistentOne.getId()
						.longValue());

	}

	public void testCountFindAllIDs() {
		this.setupObjects();

		// Now since the setupObjects method attached the data containers to a
		// DataProducer, I want to break that so I can just work with the
		// DataContainers. If I don't do this, I will get an exception while
		// inserting the DC because the creator was not inserted first.
		dataProducerOne.clearOutputs();
		dataProducerTwo.clearOutputs();

		int count = 0;
		try {
			count = dataContainerAccess.countFindAllIDs();
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		logger.debug("testCountFindAllIDs starting");
		// Insert one of the DataContainers
		Long dcId = null;
		try {
			dcId = dataContainerAccess.insert(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		logger.debug("DataContainerOne should be inserted (id = " + dcId + ")");
		// Assert the return was not null
		assertNotNull(
				"The ID after inserting the DataContainer should not be null",
				dcId);

		// Now try to read back the count of all IDs and it should be one more
		// than the last time it was read
		int newcount = 0;
		try {
			newcount = dataContainerAccess.countFindAllIDs();
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		logger.debug("Count read back is " + newcount
				+ " and should at least be one more than " + count);
		// Assert there is one
		assertTrue("There should be one more dataContainer",
				newcount == (count + 1));

		// Now insert the second one
		Long dcTwoId = null;
		try {
			dcTwoId = dataContainerAccess.insert(dataContainerTwo);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		logger.debug("DataContainerTwo inserted (id = " + dcTwoId + ")");
		// Assert the return was not null
		assertNotNull(
				"The ID after inserting the DataContainer should not be null",
				dcTwoId);

		// Now try to read back the count of all IDs
		newcount = 0;
		try {
			newcount = dataContainerAccess.countFindAllIDs();
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		logger.debug("Count read back is " + newcount
				+ " and should be 2 more than " + count);
		// Assert there is one
		assertTrue("There should be two more dataContainers",
				newcount == (count + 2));
		this.cleanObjectsFromDB();
	}

	/**
	 * This test checks to see if a matching DataContainer can be found by its
	 * DODS URL only
	 */
	public void testFindByDodsUrlString() {
		// Setup all the object
		this.setupObjects();

		// Now since the setupObjects method attached the data containers to a
		// DataProducer, I want to break that so I can just work with the
		// DataContainers. If I don't do this, I will get an exception while
		// inserting the DC because the creator was not inserted first.
		dataProducerOne.clearOutputs();

		logger.debug("Test find by DodsUrlString");

		// Insert one of the DataContainers
		Long dcId = null;
		try {
			dcId = dataContainerAccess.insert(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		logger.debug("DataContainerOne should be inserted (id = " + dcId + ")");
		// Assert the return was not null
		assertNotNull(
				"The ID after inserting the DataContainer should not be null",
				dcId);

		// Now search by ID
		DataContainer persistentDC = null;
		try {
			persistentDC = dataContainerAccess.findByDODSURLString(
					dataContainerOneDodsUrlString, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Check that the IDs match
		assertNotNull("persistenDC should not be null", persistentDC);
		assertEquals("ID from DODS Url search should be equal", persistentDC
				.getId(), dcId);

		// Now clear all fields except the DODSUrl and see if the findEquivalent
		// method works with only the DODS UrlString
		dataContainerOne = new DataContainer();
		try {
			dataContainerOne.setDodsUrlString(dataContainerOneDodsUrlString);
		} catch (MetadataException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		persistentDC = null;
		assertNull("persistentDC should be null", persistentDC);
		try {
			persistentDC = (DataContainer) dataContainerAccess
					.findEquivalentPersistentObject(dataContainerOne, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull(
				"persistentDC should not be null after search for equivalent object (DODS)",
				persistentDC);
		assertEquals(
				"ID from equivalent object (DODS Url) search should be equal",
				persistentDC.getId(), dcId);

		// Clean up the DB for the next test
		this.cleanObjectsFromDB();
	}

	public void testFindByKeywordName() {
		this.setupObjects();
		// Now since the setupObjects method attached the data containers to a
		// DataProducer, I want to break that so I can just work with the
		// DataContainers. If I don't do this, I will get an exception while
		// inserting the DC because the creator was not inserted first.
		dataProducerOne.clearOutputs();
		dataProducerTwo.clearOutputs();

		// Attach the keywords to the datacontainer
		dataContainerOne.addKeyword(keywordOne);
		dataContainerOne.addKeyword(keywordTwo);
		dataContainerTwo.addKeyword(keywordOne);

		// Now insert them
		Long dcOneId = null;
		Long dcTwoId = null;
		try {
			dcOneId = dataContainerAccess.insert(dataContainerOne);
			dcTwoId = dataContainerAccess.insert(dataContainerTwo);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Assert the return was not null
		assertNotNull(
				"The ID after inserting the DataContainer should not be null",
				dcOneId);
		assertNotNull(
				"The ID after inserting the DataContainer should not be null",
				dcTwoId);

		// Now search for data container by keyword name
		Collection results = null;
		try {
			results = dataContainerAccess.findByKeywordName("Keyword", true,
					null, null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		// The first one should return nothing
		assertNotNull("The search should not return null", results);
		assertTrue("The search should return nothing", results.size() == 0);

		// Now turn off the flag for exact match and I should get both
		results = null;
		try {
			results = dataContainerAccess.findByKeywordName("Keyword", false,
					null, null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The search should not return null", results);
		assertTrue("The search should two", results.size() == 2);
		assertTrue("DataContainer one should be in there", results
				.contains(dataContainerOne));
		assertTrue("DataContainer two should be in there", results
				.contains(dataContainerTwo));

		results = null;
		try {
			results = dataContainerAccess.findByKeywordName("KeywordTwo", true,
					null, null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		// Should find the first one
		assertNotNull("The search should not return null", results);
		assertTrue("The search should return one", results.size() == 1);
		assertTrue("Should be data container one", results
				.contains(dataContainerOne));
		assertTrue("Should not be dataCotainer two", !results
				.contains(dataContainerTwo));

		results = null;
		try {
			results = dataContainerAccess.findByKeywordName("KeywordT", false,
					null, null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		// Should find the first one
		assertNotNull("The search should not return null", results);
		assertTrue("The search should return one", results.size() == 1);
		assertTrue("Should be data container one", results
				.contains(dataContainerOne));
		assertTrue("Should not be dataCotainer two", !results
				.contains(dataContainerTwo));

		this.cleanObjectsFromDB();
	}

	public void testCascadeInsert() {
		this.setupObjects();
		// Now since the setupObjects method attached the data containers to a
		// DataProducer, I want to break that so I can just work with the
		// DataContainers. If I don't do this, I will get an exception while
		// inserting the DC because the creator was not inserted first.
		dataProducerOne.clearOutputs();
		dataProducerTwo.clearOutputs();

		// Let's insert the DataContainer
		Long dcId = null;
		try {
			dcId = dataContainerAccess.insert(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Assert the return was not null
		assertNotNull(
				"The ID after inserting the DataContainer should not be null",
				dcId);
		// Now query back for the object
		DataContainer persistentDataContainer = null;
		try {
			persistentDataContainer = (DataContainer) dataContainerAccess
					.findById(dcId, true);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Now walk the graph
		assertNotNull("The persistent DataContainer should not be null",
				persistentDataContainer);
		assertNotNull("The associated RecordDescription should not be null",
				persistentDataContainer.getRecordDescription());
		assertEquals(
				"There should be three recordVariables associated with the dataContainer",
				3, persistentDataContainer.getRecordDescription()
						.getRecordVariables().size());

		this.cleanObjectsFromDB();
	}

	public void testUpdateOnCascades() {
		this.setupObjects();

		// Now since the setupObjects method attached the data containers to a
		// DataProducer, I want to break that so I can just work with the
		// DataContainers. If I don't do this, I will get an exception while
		// inserting the DC because the creator was not inserted first.
		dataProducerOne.clearOutputs();
		dataProducerTwo.clearOutputs();

		// Insert the object
		Long dcId = null;
		try {
			dcId = dataContainerAccess.insert(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Assert the return was not null
		assertNotNull(
				"The ID after inserting the DataContainer should not be null",
				dcId);

		// Now retrieve the DataContainer
		DataContainer persistentDataContainer = null;
		try {
			persistentDataContainer = (DataContainer) dataContainerAccess
					.findById(dcId, true);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Now walk the graph
		assertNotNull("The persistent DataContainer should not be null",
				persistentDataContainer);
		assertNotNull("The associated RecordDescription should not be null",
				persistentDataContainer.getRecordDescription());
		assertEquals(
				"There should be three recordVariables associated with the dataContainer",
				3, persistentDataContainer.getRecordDescription()
						.getRecordVariables().size());
		assertEquals("The buffer style should be ascii",
				RecordDescription.BUFFER_STYLE_ASCII, persistentDataContainer
						.getRecordDescription().getBufferStyle());

		// Now take the local DataContainer object, remove the recordDescription
		// and then update some fields, perform an update and the
		// recordDescriptions and recordVariables should remain attached in the
		// persistent store
		dataContainerOne.setRecordDescription(null);

		try {
			dataContainerOne
					.setDescription("Updated description to test updates on cascading relationships");
		} catch (MetadataException e1) {
			assertTrue("MetadataException was thrown: " + e1.getMessage(),
					false);
		}

		// Now do an update
		Long dcAfterUpdateId = null;
		try {
			dcAfterUpdateId = dataContainerAccess.update(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertEquals("The ID after update should equal ID before update", dcId,
				dcAfterUpdateId);

		// Now retrieve the DataContainer
		persistentDataContainer = null;
		try {
			persistentDataContainer = (DataContainer) dataContainerAccess
					.findById(dcId, true);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Make sure recordDescriptions and such are still there
		// Now walk the graph
		assertNotNull("The persistent DataContainer should not be null",
				persistentDataContainer);
		assertNotNull("The associated RecordDescription should not be null",
				persistentDataContainer.getRecordDescription());
		assertEquals(
				"There should be three recordVariables associated with the dataContainer",
				3, persistentDataContainer.getRecordDescription()
						.getRecordVariables().size());
		assertEquals("The buffer style should be ascii",
				RecordDescription.BUFFER_STYLE_ASCII, persistentDataContainer
						.getRecordDescription().getBufferStyle());

		// Now set the second recordDescription
		dataContainerOne.setRecordDescription(recordDescriptionTwo);

		// Now do an update
		dcAfterUpdateId = null;
		try {
			dcAfterUpdateId = dataContainerAccess.update(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertEquals("The ID after update should equal ID before update", dcId,
				dcAfterUpdateId);

		// Now retrieve the DataContainer
		persistentDataContainer = null;
		try {
			persistentDataContainer = (DataContainer) dataContainerAccess
					.findById(dcId, true);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Make sure recordDescriptions and such are still there
		// Now walk the graph
		assertNotNull("The persistent DataContainer should not be null",
				persistentDataContainer);
		assertNotNull("The associated RecordDescription should not be null",
				persistentDataContainer.getRecordDescription());
		assertEquals(
				"There should be six recordVariables associated with the dataContainer",
				6, persistentDataContainer.getRecordDescription()
						.getRecordVariables().size());
		assertEquals("The buffer style should be ascii",
				RecordDescription.BUFFER_STYLE_BINARY, persistentDataContainer
						.getRecordDescription().getBufferStyle());

		// Now due to an earlier bug, if I just did an update of the same thing,
		// the RecordVariables would continue to grow. If I do another update of
		// the same object, nothing should change. Let's test that
		// Now do an update
		dcAfterUpdateId = null;
		try {
			dcAfterUpdateId = dataContainerAccess.update(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertEquals("The ID after update should equal ID before update", dcId,
				dcAfterUpdateId);

		// Make sure recordDescriptions and such are still there
		// Now walk the graph
		assertNotNull("The persistent DataContainer should not be null",
				persistentDataContainer);
		assertNotNull("The associated RecordDescription should not be null",
				persistentDataContainer.getRecordDescription());
		assertEquals(
				"There should be six recordVariables associated with the dataContainer",
				6, persistentDataContainer.getRecordDescription()
						.getRecordVariables().size());
		assertEquals("The buffer style should be ascii",
				RecordDescription.BUFFER_STYLE_BINARY, persistentDataContainer
						.getRecordDescription().getBufferStyle());

		// Now let's read back again, update the DC and see if the cascades hold
		// Now retrieve the DataContainer
		persistentDataContainer = null;
		try {
			persistentDataContainer = (DataContainer) dataContainerAccess
					.findById(dcId, true);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		try {
			persistentDataContainer
					.setDescription("Yet another check of Update");
		} catch (MetadataException e1) {
			assertTrue("MetadataException was thrown: " + e1.getMessage(),
					false);
		}
		// Now do an update
		dcAfterUpdateId = null;
		try {
			dcAfterUpdateId = dataContainerAccess
					.update(persistentDataContainer);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertEquals("The ID after update should equal ID before update", dcId,
				dcAfterUpdateId);

		// Now retrieve the DataContainer
		persistentDataContainer = null;
		try {
			persistentDataContainer = (DataContainer) dataContainerAccess
					.findById(dcId, true);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The persistent DataContainer should not be null",
				persistentDataContainer);
		assertNotNull("The associated RecordDescription should not be null",
				persistentDataContainer.getRecordDescription());
		assertEquals(
				"There should be six recordVariables associated with the dataContainer",
				6, persistentDataContainer.getRecordDescription()
						.getRecordVariables().size());
		assertEquals("The buffer style should be ascii",
				RecordDescription.BUFFER_STYLE_BINARY, persistentDataContainer
						.getRecordDescription().getBufferStyle());
		assertEquals(
				"The new description should be 'Yet another check of Update",
				persistentDataContainer.getDescription(),
				"Yet another check of Update");

		this.cleanObjectsFromDB();
	}

	public void testTheStandardVariableRelationshipWithRecordVariable() {
		this.setupObjects();

		// Now since the setupObjects method attached the data containers to a
		// DataProducer, I want to break that so I can just work with the
		// DataContainers. If I don't do this, I will get an exception while
		// inserting the DC because the creator was not inserted first.
		dataProducerOne.clearOutputs();
		dataProducerTwo.clearOutputs();

		// Add the standardUnits to the data container
		recordVariableOne.setStandardVariable(standardVariableOne);
		recordVariableTwo.setStandardVariable(standardVariableOne);
		recordVariableThree.setStandardVariable(standardVariableOne);
		recordVariableFour.setStandardVariable(standardVariableTwo);
		recordVariableFive.setStandardVariable(standardVariableTwo);
		recordVariableSix.setStandardVariable(standardVariableTwo);

		// Now insert both data containers
		// Insert the object
		Long dcOneId = null;
		Long dcTwoId = null;
		try {
			dcOneId = dataContainerAccess.insert(dataContainerOne);
			dcTwoId = dataContainerAccess.insert(dataContainerTwo);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Assert the return was not null
		assertNotNull(
				"The ID after inserting the DataContainer should not be null",
				dcOneId);
		assertNotNull(
				"The ID after inserting the DataContainer should not be null",
				dcTwoId);

		// Now retrieve the DataContainer
		DataContainer persistentDataContainerOne = null;
		DataContainer persistentDataContainerTwo = null;
		try {
			persistentDataContainerOne = (DataContainer) dataContainerAccess
					.findById(dcOneId, true);
			persistentDataContainerTwo = (DataContainer) dataContainerAccess
					.findById(dcTwoId, true);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Now make sure the standardVariables made the association
		Iterator rvOneIter = persistentDataContainerOne.getRecordDescription()
				.getRecordVariables().iterator();
		while (rvOneIter.hasNext()) {
			assertEquals("RecordVariable should have standardVariableOne",
					standardVariableOne, ((RecordVariable) rvOneIter.next())
							.getStandardVariable());
		}
		Iterator rvTwoIter = persistentDataContainerTwo.getRecordDescription()
				.getRecordVariables().iterator();
		while (rvTwoIter.hasNext()) {
			assertEquals("RecordVariable should have standardVariableTwo",
					standardVariableTwo, ((RecordVariable) rvTwoIter.next())
							.getStandardVariable());
		}

		// Now delete the data containers and both standard variables should
		// still be there
		try {
			dataContainerAccess.delete(dataContainerOne);
			dataContainerAccess.delete(dataContainerTwo);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		persistentDataContainerOne = null;
		persistentDataContainerTwo = null;
		try {
			persistentDataContainerOne = (DataContainer) dataContainerAccess
					.findById(dcOneId, true);
			persistentDataContainerTwo = (DataContainer) dataContainerAccess
					.findById(dcTwoId, true);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// The DCs should not have been found
		assertNull("The data container should not have been found",
				persistentDataContainerOne);
		assertNull("The data container should not have been found",
				persistentDataContainerTwo);

		// Search for StandardVariables
		StandardVariable persistentStandardVariableOne = null;
		StandardVariable persistentStandardVariableTwo = null;
		try {
			persistentStandardVariableOne = (StandardVariable) standardVariableAccess
					.findEquivalentPersistentObject(standardVariableOne, false);
			persistentStandardVariableTwo = (StandardVariable) standardVariableAccess
					.findEquivalentPersistentObject(standardVariableTwo, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataAccessException was thrown: " + e.getMessage(),
					false);
		}
		assertNotNull("The StandardVariable one still should be found",
				persistentStandardVariableOne);
		assertNotNull("The StandardVariable two still should be found",
				persistentStandardVariableTwo);

		this.cleanObjectsFromDB();
	}

	public void testDataContainerGroupRelationship() {
		// Ok this relationship should be that upon insert/update, the
		// DataContainerGroup should be inserted/updated and upon delete, the
		// data
		// containerGroup should be left behind
		this.setupObjects();

		// Now since the setupObjects method attached the data containers to a
		// DataProducer, I want to break that so I can just work with the
		// DataContainers. If I don't do this, I will get an exception while
		// inserting the DC because the creator was not inserted first.
		dataProducerOne.clearOutputs();
		dataProducerTwo.clearOutputs();

		// Now add the data container groups to the data container
		dataContainerOne.addDataContainerGroup(dataContainerGroupOne);
		dataContainerOne.addDataContainerGroup(dataContainerGroupTwo);

		// Now insert the DC
		Long dcOneId = null;
		try {
			dcOneId = dataContainerAccess.insert(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Assert the return was not null
		assertNotNull(
				"The ID after inserting the DataContainer should not be null",
				dcOneId);

		// Now read back and look for the two data container groups
		DataContainer persistentDataContainer = null;
		try {
			persistentDataContainer = (DataContainer) dataContainerAccess
					.getMetadataObjectGraph(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		assertNotNull("Persistent data container should not be null",
				persistentDataContainer);
		assertNotNull("Associated DataContainerGroups should not be null",
				persistentDataContainer.getDataContainerGroups());
		assertEquals("DataContainer should have two DataContainerGroups", 2,
				persistentDataContainer.getDataContainerGroups().size());
		assertTrue("DataContainerGroupOne should be one of them",
				persistentDataContainer.getDataContainerGroups().contains(
						dataContainerGroupOne));
		assertTrue("DataContainerGroupTwo should be the other",
				persistentDataContainer.getDataContainerGroups().contains(
						dataContainerGroupTwo));

		// Look them up by the find method
		Collection dataContainerGroupsByDC = null;
		try {
			dataContainerGroupsByDC = dataContainerGroupAccess
					.findByDataContainer(dataContainerOne, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		assertNotNull("Associated DataContainerGroups should not be null",
				dataContainerGroupsByDC);
		assertEquals("DataContainer should have two DataContainerGroups", 2,
				dataContainerGroupsByDC.size());
		assertTrue("DataContainerGroupOne should be one of them",
				dataContainerGroupsByDC.contains(dataContainerGroupOne));
		assertTrue("DataContainerGroupTwo should be the other",
				dataContainerGroupsByDC.contains(dataContainerGroupTwo));

		// Now if we delete the DataContainer One, we should still find the
		// groups
		try {
			dataContainerAccess.delete(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Make sure the delete took
		persistentDataContainer = null;
		try {
			persistentDataContainer = (DataContainer) dataContainerAccess
					.getMetadataObjectGraph(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNull("DataContainer should not be found", persistentDataContainer);

		// Now look for the groups
		Collection dataContainerGroups = null;
		try {
			dataContainerGroups = dataContainerGroupAccess.findAll(null, null,
					false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("Associated DataContainerGroups should not be null",
				dataContainerGroups);
		assertTrue("DataContainerGroupOne should be one of them",
				dataContainerGroups.contains(dataContainerGroupOne));
		assertTrue("DataContainerGroupTwo should be the other",
				dataContainerGroups.contains(dataContainerGroupTwo));

		// Now clean up
		this.cleanObjectsFromDB();

		// Set up the clean ones again
		this.setupObjects();

		// Now since the setupObjects method attached the data containers to a
		// DataProducer, I want to break that so I can just work with the
		// DataContainers. If I don't do this, I will get an exception while
		// inserting the DC because the creator was not inserted first.
		dataProducerOne.clearOutputs();
		dataProducerTwo.clearOutputs();

		// Now insert the DC
		dcOneId = null;
		try {
			dcOneId = dataContainerAccess.insert(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Assert the return was not null
		assertNotNull(
				"The ID after inserting the DataContainer should not be null",
				dcOneId);

		// Now query back for it and add a DataContainerGroup
		// Now read back and look for the two data container groups
		persistentDataContainer = null;
		try {
			persistentDataContainer = (DataContainer) dataContainerAccess
					.getMetadataObjectGraph(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Let's print out some information
		logger.debug("RD-> "
				+ persistentDataContainer.getRecordDescription()
						.toStringRepresentation("|"));
		logger.debug("RD.RVs.size-> "
				+ persistentDataContainer.getRecordDescription()
						.getRecordVariables().size());

		assertNotNull("Persistent data container should not be null",
				persistentDataContainer);
		assertEquals(
				"The persistent data container should have no data container groups",
				0, persistentDataContainer.getDataContainerGroups().size());
		persistentDataContainer.addDataContainerGroup(dataContainerGroupOne);
		persistentDataContainer.addDataContainerGroup(dataContainerGroupTwo);

		// Now update it
		Long updatedDCId = null;
		try {
			updatedDCId = dataContainerAccess.update(persistentDataContainer);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		assertEquals("The updated ID should be the same as the inserted",
				updatedDCId.longValue(), dcOneId.longValue());
		persistentDataContainer = null;
		try {
			persistentDataContainer = (DataContainer) dataContainerAccess
					.getMetadataObjectGraph(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		assertNotNull("Persistent data container should not be null",
				persistentDataContainer);
		assertEquals("There should be two data container groups", 2,
				persistentDataContainer.getDataContainerGroups().size());
		assertTrue("DataContainerGroupOne should be one of them",
				persistentDataContainer.getDataContainerGroups().contains(
						dataContainerGroupOne));
		assertTrue("DataContainerGroupTwo should be the other",
				persistentDataContainer.getDataContainerGroups().contains(
						dataContainerGroupTwo));

		// Now clean up
		this.cleanObjectsFromDB();

		// Now I would like to start clean, insert one with one data producer
		// group, then read it back out, add another one and make sure they are
		// both still there
		this.setupObjects();
		// Now since the setupObjects method attached the data containers to a
		// DataProducer, I want to break that so I can just work with the
		// DataContainers. If I don't do this, I will get an exception while
		// inserting the DC because the creator was not inserted first.
		dataProducerOne.clearOutputs();
		dataProducerTwo.clearOutputs();

		dataContainerOne.addDataContainerGroup(dataContainerGroupOne);

		// Now insert the DC
		dcOneId = null;
		try {
			dcOneId = dataContainerAccess.insert(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Assert the return was not null
		assertNotNull(
				"The ID after inserting the DataContainer should not be null",
				dcOneId);

		// Now read back and look for the two data container groups
		persistentDataContainer = null;
		try {
			persistentDataContainer = (DataContainer) dataContainerAccess
					.getMetadataObjectGraph(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		assertNotNull("Persistent data container should not be null",
				persistentDataContainer);
		assertNotNull("Associated DataContainerGroups should not be null",
				persistentDataContainer.getDataContainerGroups());
		assertEquals("DataContainer should have one DataContainerGroups", 1,
				persistentDataContainer.getDataContainerGroups().size());
		assertTrue("DataContainerGroupOne should be one of them",
				persistentDataContainer.getDataContainerGroups().contains(
						dataContainerGroupOne));
		assertTrue("DataContainerGroupTwo should not be there",
				!persistentDataContainer.getDataContainerGroups().contains(
						dataContainerGroupTwo));

		// Now add the second one
		persistentDataContainer.addDataContainerGroup(dataContainerGroupTwo);

		// Now update it
		updatedDCId = null;
		try {
			updatedDCId = dataContainerAccess.update(persistentDataContainer);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		assertEquals("The updated ID should be the same as the inserted",
				updatedDCId.longValue(), dcOneId.longValue());
		persistentDataContainer = null;
		try {
			persistentDataContainer = (DataContainer) dataContainerAccess
					.getMetadataObjectGraph(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		assertNotNull("Persistent data container should not be null",
				persistentDataContainer);
		assertEquals("There should be two data container groups", 2,
				persistentDataContainer.getDataContainerGroups().size());
		assertTrue("DataContainerGroupOne should be one of them",
				persistentDataContainer.getDataContainerGroups().contains(
						dataContainerGroupOne));
		assertTrue("DataContainerGroupTwo should be the other",
				persistentDataContainer.getDataContainerGroups().contains(
						dataContainerGroupTwo));

		this.cleanObjectsFromDB();

		// OK let's do the same thing, but construct use a newly constructed
		// data container and have the other DataContainergroup associated with
		// it
		// Now I would like to start clean, insert one with one data producer
		// group, then read it back out, add another one and make sure they are
		// both still there
		this.setupObjects();
		// Now since the setupObjects method attached the data containers to a
		// DataProducer, I want to break that so I can just work with the
		// DataContainers. If I don't do this, I will get an exception while
		// inserting the DC because the creator was not inserted first.
		dataProducerOne.clearOutputs();
		dataProducerTwo.clearOutputs();

		dataContainerOne.addDataContainerGroup(dataContainerGroupOne);

		// Now insert the DC
		dcOneId = null;
		try {
			dcOneId = dataContainerAccess.insert(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Assert the return was not null
		assertNotNull(
				"The ID after inserting the DataContainer should not be null",
				dcOneId);

		// Now read back and look for the two data container groups
		persistentDataContainer = null;
		try {
			persistentDataContainer = (DataContainer) dataContainerAccess
					.getMetadataObjectGraph(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		assertNotNull("Persistent data container should not be null",
				persistentDataContainer);
		assertNotNull("Associated DataContainerGroups should not be null",
				persistentDataContainer.getDataContainerGroups());
		assertEquals("DataContainer should have one DataContainerGroups", 1,
				persistentDataContainer.getDataContainerGroups().size());
		assertTrue("DataContainerGroupOne should be one of them",
				persistentDataContainer.getDataContainerGroups().contains(
						dataContainerGroupOne));
		assertTrue("DataContainerGroupTwo should not be there",
				!persistentDataContainer.getDataContainerGroups().contains(
						dataContainerGroupTwo));

		// Now just change the existing dataContainerOne
		dataContainerOne.clearDataContainerGroups();
		dataContainerOne.addDataContainerGroup(dataContainerGroupTwo);

		// Now update it
		updatedDCId = null;
		try {
			updatedDCId = dataContainerAccess.update(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		assertEquals("The updated ID should be the same as the inserted",
				updatedDCId.longValue(), dcOneId.longValue());
		persistentDataContainer = null;
		try {
			persistentDataContainer = (DataContainer) dataContainerAccess
					.getMetadataObjectGraph(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		assertNotNull("Persistent data container should not be null",
				persistentDataContainer);
		assertEquals("There should be two data container groups", 2,
				persistentDataContainer.getDataContainerGroups().size());
		assertTrue("DataContainerGroupOne should be one of them",
				persistentDataContainer.getDataContainerGroups().contains(
						dataContainerGroupOne));
		assertTrue("DataContainerGroupTwo should be the other",
				persistentDataContainer.getDataContainerGroups().contains(
						dataContainerGroupTwo));

		this.cleanObjectsFromDB();
	}

	public void testKeywordRelationship() {
		this.setupObjects();

		// Now since the setupObjects method attached the data containers to a
		// DataProducer, I want to break that so I can just work with the
		// DataContainers. If I don't do this, I will get an exception while
		// inserting the DC because the creator was not inserted first.
		dataProducerOne.clearOutputs();
		dataProducerTwo.clearOutputs();

		// Attach the keywords to the datacontainer
		dataContainerOne.addKeyword(keywordOne);
		dataContainerOne.addKeyword(keywordTwo);

		// Now insert them
		// Insert the object
		Long dcOneId = null;
		try {
			dcOneId = dataContainerAccess.insert(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Assert the return was not null
		assertNotNull(
				"The ID after inserting the DataContainer should not be null",
				dcOneId);

		// OK now query back and look at the keywords
		Collection keywords = null;
		try {
			keywords = keywordAccess.findByMetadataObject(dataContainerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Make sure the keywords are in there
		assertTrue("Keyword one should be there", keywords.contains(keywordOne));
		assertTrue("Keyword two should be there", keywords.contains(keywordTwo));
		// Clean up
		this.cleanObjectsFromDB();
	}

	/**
	 * This method finds test the findAllByTimeWindowMethod
	 */

	// public void testFindAllByTimeWindow() {
	// this.setupObjects();
	// // Insert them first
	// try {
	// dataContainerAccess.insert(dataContainerOne);
	// dataContainerAccess.insert(dataContainerTwo);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// // Now we know the two DC time windows are:
	// // dcOne -> startDate=2003-05-05T16:11:44Z, endDate=2004-02-01T08:38:19Z
	// // dcTwo -> startDate=2005-01-01T00:00:00Z, endDate=2005-02-01T08:38:19Z
	// // so we can do some searching now.
	// // ************* ONLY SPECIFYING START DATE *************** //
	// // A time window that is before any of the data containers data
	// Date startOfWindow = xmlDateFormat.parse("2001-05-06T00:00:00Z");
	// // If start time is before both containers, I should get both
	// Collection windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(
	// startOfWindow, false, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("Both data containers should be found", 2, windowDCs
	// .size());
	// assertTrue("Collection should contain data container one", windowDCs
	// .contains(dataContainerOne));
	// assertTrue("Collection should contain data container two", windowDCs
	// .contains(dataContainerTwo));
	//
	// // Turn on the all data boolean and I should still get both
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(
	// startOfWindow, true, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("Both data containers should be found", 2, windowDCs
	// .size());
	// assertTrue("Collection should contain data container one", windowDCs
	// .contains(dataContainerOne));
	// assertTrue("Collection should contain data container two", windowDCs
	// .contains(dataContainerTwo));
	//
	// // Now move the start time inside the first data container, and the
	// // first query should return both
	// startOfWindow = xmlDateFormat.parse("2003-06-01T00:00:00Z");
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(
	// startOfWindow, false, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("Both data containers should be found", 2, windowDCs
	// .size());
	// assertTrue("Collection should contain data container one", windowDCs
	// .contains(dataContainerOne));
	// assertTrue("Collection should contain data container two", windowDCs
	// .contains(dataContainerTwo));
	//
	// // Now turn on the boolean and I should only get the second data
	// // container
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(
	// startOfWindow, true, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("Only one data container should be found", 1, windowDCs
	// .size());
	// assertTrue("Collection should NOT contain data container one",
	// !windowDCs.contains(dataContainerOne));
	// assertTrue("Collection should contain data container two", windowDCs
	// .contains(dataContainerTwo));
	//
	// // Now move the start time after the end time of the first data
	// // container
	// startOfWindow = xmlDateFormat.parse("2004-12-01T00:00:00Z");
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(
	// startOfWindow, false, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("Only one data container should be found", 1, windowDCs
	// .size());
	// assertTrue("Collection should NOT contain data container one",
	// !windowDCs.contains(dataContainerOne));
	// assertTrue("Collection should contain data container two", windowDCs
	// .contains(dataContainerTwo));
	//
	// // Turning on the boolean should make no difference here
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(
	// startOfWindow, true, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("Only one data container should be found", 1, windowDCs
	// .size());
	// assertTrue("Collection should NOT contain data container one",
	// !windowDCs.contains(dataContainerOne));
	// assertTrue("Collection should contain data container two", windowDCs
	// .contains(dataContainerTwo));
	//
	// // Now move start time inside second data container and without boolean,
	// // it should still return the second data container
	// startOfWindow = xmlDateFormat.parse("2005-01-25T00:00:00Z");
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(
	// startOfWindow, false, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("Only one data container should be found", 1, windowDCs
	// .size());
	// assertTrue("Collection should NOT contain data container one",
	// !windowDCs.contains(dataContainerOne));
	// assertTrue("Collection should contain data container two", windowDCs
	// .contains(dataContainerTwo));
	//
	// // Turning on the boolean should find nothing
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(
	// startOfWindow, true, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("No data containers should have been found", 0, windowDCs
	// .size());
	//
	// // Now move it after the second data container and both boolean states
	// // should return nothing
	// startOfWindow = xmlDateFormat.parse("2006-01-25T00:00:00Z");
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(
	// startOfWindow, false, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("No data containers should have been found", 0, windowDCs
	// .size());
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(
	// startOfWindow, true, null, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("No data containers should have been found", 0, windowDCs
	// .size());
	//
	// // ****************** ONLY SPECIFYING END DATE ************************
	// // Set the end date after both data containers
	// Date endOfWindow = xmlDateFormat.parse("2006-01-06T00:00:00Z");
	// // I should get both
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(null,
	// false, endOfWindow, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("Both data containers should be found", 2, windowDCs
	// .size());
	// assertTrue("Collection should contain data container one", windowDCs
	// .contains(dataContainerOne));
	// assertTrue("Collection should contain data container two", windowDCs
	// .contains(dataContainerTwo));
	//
	// // Turn on the all data boolean and I should still get both
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(null,
	// false, endOfWindow, true);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("Both data containers should be found", 2, windowDCs
	// .size());
	// assertTrue("Collection should contain data container one", windowDCs
	// .contains(dataContainerOne));
	// assertTrue("Collection should contain data container two", windowDCs
	// .contains(dataContainerTwo));
	//
	// // Now move the end time inside the second data container, and the
	// // first query should return both
	// endOfWindow = xmlDateFormat.parse("2005-01-25T00:00:00Z");
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(null,
	// false, endOfWindow, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("Both data containers should be found", 2, windowDCs
	// .size());
	// assertTrue("Collection should contain data container one", windowDCs
	// .contains(dataContainerOne));
	// assertTrue("Collection should contain data container two", windowDCs
	// .contains(dataContainerTwo));
	//
	// // Now turn on the boolean and I should only get the first data
	// // container
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(null,
	// false, endOfWindow, true);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("Only one data container should be found", 1, windowDCs
	// .size());
	// assertTrue("Collection should contain data container one", windowDCs
	// .contains(dataContainerOne));
	// assertTrue("Collection should NOT contain data container two",
	// !windowDCs.contains(dataContainerTwo));
	//
	// // Now move the end time before the start time of the second data
	// // container
	// endOfWindow = xmlDateFormat.parse("2004-12-01T00:00:00Z");
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(null,
	// false, endOfWindow, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("Only one data container should be found", 1, windowDCs
	// .size());
	// assertTrue("Collection should contain data container one", windowDCs
	// .contains(dataContainerOne));
	// assertTrue("Collection should NOT contain data container two",
	// !windowDCs.contains(dataContainerTwo));
	//
	// // Turning on the boolean should make no difference here
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(null,
	// false, endOfWindow, true);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("Only one data container should be found", 1, windowDCs
	// .size());
	// assertTrue("Collection should contain data container one", windowDCs
	// .contains(dataContainerOne));
	// assertTrue("Collection should NOT contain data container two",
	// !windowDCs.contains(dataContainerTwo));
	//
	// // Now move end time inside first data container and without boolean,
	// // it should still return the first data container
	// endOfWindow = xmlDateFormat.parse("2003-12-25T00:00:00Z");
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(null,
	// false, endOfWindow, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("Only one data container should be found", 1, windowDCs
	// .size());
	// assertTrue("Collection should contain data container one", windowDCs
	// .contains(dataContainerOne));
	// assertTrue("Collection should NOT contain data container two",
	// !windowDCs.contains(dataContainerTwo));
	//
	// // Turning on the boolean should find nothing
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(null,
	// false, endOfWindow, true);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("No data containers should have been found", 0, windowDCs
	// .size());
	//
	// // Now move it before the fist data container and both boolean states
	// // should return nothing
	// endOfWindow = xmlDateFormat.parse("2003-01-25T00:00:00Z");
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(null,
	// false, endOfWindow, true);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("No data containers should have been found", 0, windowDCs
	// .size());
	// windowDCs = null;
	// try {
	// windowDCs = dataContainerAccess.findWithDataWithinTimeWindow(null,
	// false, endOfWindow, true);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("The time window return should not be null", windowDCs);
	// assertEquals("No data containers should have been found", 0, windowDCs
	// .size());
	//
	// // Clean up everything
	// this.cleanObjectsFromDB();
	// }
	// public void testFindBySQL() {
	// this.setupObjects();
	//
	// // Now since the setupObjects method attached the data containers to a
	// // DataProducer, I want to break that so I can just work with the
	// // DataContainers. If I don't do this, I will get an exception while
	// // inserting the DC because the creator was not inserted first.
	// dataProducerOne.clearOutputs();
	// dataProducerTwo.clearOutputs();
	//
	// // Insert the two DataContainers
	// try {
	// dataContainerAccess.insert(dataContainerOne);
	// dataContainerAccess.insert(dataContainerTwo);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	//
	// // Now try to find by SQL
	// String sqlString = "select {dc.*} from ssdsdba.DataContainer dc where
	// dc.name like 'DataContainer%'";
	//
	// Collection dataContainers = null;
	// try {
	// dataContainers = dataContainerAccess.findBySQL(sqlString, "dc",
	// DataContainer.class, false);
	// } catch (RemoteException e) {
	// assertTrue("RemoteException was thrown: " + e.getMessage(), false);
	// } catch (MetadataAccessException e) {
	// assertTrue("MetadataException was thrown: " + e.getMessage(), false);
	// }
	// assertNotNull("Ids should not be null", dataContainers);
	// assertTrue("There should be two with the name like DataContainer",
	// dataContainers.size() == 2);
	// Iterator iterator = dataContainers.iterator();
	// while (iterator.hasNext()) {
	// Object object = iterator.next();
	// logger.debug("Object => " + object);
	// logger.debug("Class => " + object.getClass().getName());
	// }
	// assertTrue("One should be in there", dataContainers
	// .contains(dataContainerOne));
	// assertTrue("Two should be in there", dataContainers
	// .contains(dataContainerTwo));
	//
	// this.cleanObjectsFromDB();
	// }
	public void testFindOutputsByDataProducer() {
		this.setupObjects();

		Long dataProducerOneID = null;
		// Insert the two DataContainer by inserting the DataProducer
		try {
			dataProducerOneID = dataProducerAccess.insert(dataProducerOne);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Now try to find output uy DataProducer
		DataProducer dpToFind = dataProducerOne;

		Collection dataContainers = null;
		try {
			dataContainers = dataContainerAccess.findOutputsByDataProducer(
					dpToFind, "startDate", null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("Ids should not be null", dataContainers);
		assertTrue("There should be two ids", dataContainers.size() == 2);
		assertTrue("One should be in there", dataContainers
				.contains(dataContainerOne));
		assertTrue("Two should be in there", dataContainers
				.contains(dataContainerTwo));

		// Now try the same test but just create a DP from scratch and use the
		// ID
		DataProducer secondTryDP = new DataProducer();
		secondTryDP.setId(dataProducerOneID);

		dataContainers = null;
		try {
			dataContainers = dataContainerAccess.findOutputsByDataProducer(
					secondTryDP, "startDate", null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("Ids should not be null", dataContainers);
		assertTrue("There should be two ids", dataContainers.size() == 2);
		assertTrue("One should be in there", dataContainers
				.contains(dataContainerOne));
		assertTrue("Two should be in there", dataContainers
				.contains(dataContainerTwo));

		this.cleanObjectsFromDB();
	}

	/**
	 * This is the suite of tests to run on a dataContainer
	 * 
	 * @param dataContainer
	 */
	private void dataContainerTest(DataContainer dataContainer) {

		// The ID of the dataContainer
		Long dataContainerId = null;
		dataContainerId = testInsert(dataContainer, dataContainerAccess);

		// Now query back by ID and make sure all attributes are equal
		DataContainer persistedDataContainer = null;

		try {
			persistedDataContainer = (DataContainer) dataContainerAccess
					.findById(dataContainerId, false);
		} catch (RemoteException e1) {
			logger.error("RemoteException caught during findById: "
					+ e1.getMessage());
		} catch (MetadataAccessException e1) {
			logger.error("MetadataAccessException caught during findById: "
					+ e1.getMessage());
		}

		// Now check that they are equal
		assertEquals("The two dataContainers should be considered equal",
				dataContainer, persistedDataContainer);

		// Check all the getter methods are equal
		testEqualityOfAllGetters(dataContainer, persistedDataContainer);

		// Create a map with the values to update
		HashMap variablesToUpdate = new HashMap();

		// Change the surname
		Object[] variable1 = new Object[1];
		variable1[0] = new String("Updated Description");
		variablesToUpdate.put("Description", variable1);

		// Change the status
		Object[] variable2 = new Object[1];
		variable2[0] = new String("Updated Name");
		variablesToUpdate.put("Name", variable2);

		testUpdate(persistedDataContainer, variablesToUpdate,
				dataContainerAccess);

		testDelete(persistedDataContainer, dataContainerAccess);
	}

	private void setupObjects() {
		try {
			dataProducerOne = new DataProducer();
			dataProducerOne.setName("DataProducer One");
			dataProducerTwo = new DataProducer();
			dataProducerTwo.setName("DataProducer Two");

			dataContainerOne = (DataContainer) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							dataContainerOneStringRep, delimiter);
			dataContainerTwo = (DataContainer) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							dataContainerTwoStringRep, delimiter);
			recordDescriptionOne = (RecordDescription) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							recordDescriptionOneStringRep, delimiter);
			recordDescriptionTwo = (RecordDescription) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							recordDescriptionTwoStringRep, delimiter);
			recordVariableOne = (RecordVariable) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							recordVariableStringRepOne, delimiter);
			recordVariableTwo = (RecordVariable) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							recordVariableStringRepTwo, delimiter);
			recordVariableThree = (RecordVariable) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							recordVariableStringRepThree, delimiter);
			recordVariableFour = (RecordVariable) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							recordVariableStringRepFour, delimiter);
			recordVariableFive = (RecordVariable) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							recordVariableStringRepFive, delimiter);
			recordVariableSix = (RecordVariable) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							recordVariableStringRepSix, delimiter);
			standardUnitOne = (StandardUnit) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							standardUnitOneStringRep, delimiter);
			standardUnitTwo = (StandardUnit) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							standardUnitTwoStringRep, delimiter);
			personOne = (Person) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							personOneStringRep, delimiter);
			personTwo = (Person) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							personTwoStringRep, delimiter);
			standardVariableOne = (StandardVariable) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							standardVariableOneStringRep, delimiter);
			standardVariableTwo = (StandardVariable) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							standardVariableTwoStringRep, delimiter);
			keywordOne = (Keyword) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							keywordOneStringRep, delimiter);
			keywordTwo = (Keyword) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							keywordTwoStringRep, delimiter);
			dataContainerGroupOne = (DataContainerGroup) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							dataContainerGroupOneStringRep, delimiter);
			dataContainerGroupTwo = (DataContainerGroup) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							dataContainerGroupTwoStringRep, delimiter);
			resourceOne = (Resource) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							resourceOneStringRep, delimiter);
			resourceTwo = (Resource) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							resourceTwoStringRep, delimiter);
			resourceTypeOne = (ResourceType) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							resourceTypeOneStringRep, delimiter);
			resourceTypeTwo = (ResourceType) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							resourceTypeTwoStringRep, delimiter);
			resourceBLOBOne = (ResourceBLOB) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							resourceBLOBOneStringRep, delimiter);
			resourceBLOBTwo = (ResourceBLOB) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							resourceBLOBTwoStringRep, delimiter);
		} catch (MetadataException e) {
			logger
					.error("MetadataException caught trying to create two DataContainer objects: "
							+ e.getMessage());
		} catch (ClassCastException cce) {
			logger
					.error("ClassCastException caught trying to create two DataContainer objects: "
							+ cce.getMessage());
		}
		// Now create the object trees
		dataProducerOne.addOutput(dataContainerOne);
		dataProducerOne.addOutput(dataContainerTwo);
		dataProducerTwo.addInput(dataContainerTwo);
		dataContainerOne.setRecordDescription(recordDescriptionOne);
		dataContainerTwo.setRecordDescription(recordDescriptionTwo);
		recordDescriptionOne.addRecordVariable(recordVariableOne);
		recordDescriptionOne.addRecordVariable(recordVariableTwo);
		recordDescriptionOne.addRecordVariable(recordVariableThree);
		recordDescriptionTwo.addRecordVariable(recordVariableFour);
		recordDescriptionTwo.addRecordVariable(recordVariableFive);
		recordDescriptionTwo.addRecordVariable(recordVariableSix);
	}

	private void cleanObjectsFromDB() {
		this.setupObjects();
		// Delete any dataContainer objects as we don't want any leftover's if a
		// test fails
		try {
			dataProducerAccess.delete(dataProducerOne);
		} catch (RemoteException e) {
			logger.error("RemoteException caught in delete of dataProducerOne:"
					+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught in delete of dataProducerOne:"
							+ e.getMessage());
		}
		try {
			dataProducerAccess.delete(dataProducerTwo);
		} catch (RemoteException e) {
			logger.error("RemoteException caught in delete of dataProducerTwo:"
					+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught in delete of dataProducerTwo:"
							+ e.getMessage());
		}
		try {
			dataContainerAccess.delete(dataContainerOne);
		} catch (RemoteException e) {
			logger
					.error("RemoteException caught in delete of dataContainerOne:"
							+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught in delete of dataContainerOne:"
							+ e.getMessage());
		}
		try {
			dataContainerAccess.delete(dataContainerTwo);
		} catch (RemoteException e) {
			logger
					.error("RemoteException caught in delete of dataContainerTwo:"
							+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught in delete of dataContainerTwo:"
							+ e.getMessage());
		}
		try {
			standardUnitAccess.delete(standardUnitOne);
		} catch (RemoteException e) {
			logger.error("RemoteException caught in delete of standardUnitOne:"
					+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught in delete of standardUnitOne:"
							+ e.getMessage());
		}
		try {
			standardUnitAccess.delete(standardUnitTwo);
		} catch (RemoteException e) {
			logger.error("RemoteException caught in delete of standardUnitTwo:"
					+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught in delete of standardUnitTwo:"
							+ e.getMessage());
		}
		try {
			standardVariableAccess.delete(standardVariableOne);
		} catch (RemoteException e) {
			logger
					.error("RemoteException caught in delete of standardVariableOne:"
							+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught in delete of standardVariableOne:"
							+ e.getMessage());
		}
		try {
			standardVariableAccess.delete(standardVariableTwo);
		} catch (RemoteException e) {
			logger
					.error("RemoteException caught in delete of standardVariableTwo:"
							+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught in delete of standardVariableTwo:"
							+ e.getMessage());
		}
		try {
			personAccess.delete(personOne);
		} catch (RemoteException e) {
			logger.error("RemoteException caught in delete of personOne:"
					+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught in delete of personOne:"
							+ e.getMessage());
		}
		try {
			personAccess.delete(personTwo);
		} catch (RemoteException e) {
			logger.error("RemoteException caught in delete of personTwo:"
					+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught in delete of personTwo:"
							+ e.getMessage());
		}
		try {
			keywordAccess.delete(keywordOne);
		} catch (RemoteException e) {
			logger.error("RemoteException caught in delete of keywordOne:"
					+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught in delete of keywordOne:"
							+ e.getMessage());
		}
		try {
			keywordAccess.delete(keywordTwo);
		} catch (RemoteException e) {
			logger.error("RemoteException caught in delete of keywordTwo:"
					+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught in delete of keywordTwo:"
							+ e.getMessage());
		}
		try {
			dataContainerGroupAccess.delete(dataContainerGroupOne);
		} catch (RemoteException e) {
			logger
					.error("RemoteException caught in delete of dataContainerGroupOne:"
							+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught in delete of dataContainerGroupOne:"
							+ e.getMessage());
		}
		try {
			dataContainerGroupAccess.delete(dataContainerGroupTwo);
		} catch (RemoteException e) {
			logger
					.error("RemoteException caught in delete of dataContainerGroupTwo:"
							+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught in delete of dataContainerGroupTwo:"
							+ e.getMessage());
		}
		try {
			resourceAccess.delete(resourceOne);
		} catch (RemoteException e) {
			logger.error("RemoteException caught in delete of resourceOne:"
					+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught in delete of resourceOne:"
							+ e.getMessage());
		}
		try {
			resourceAccess.delete(resourceTwo);
		} catch (RemoteException e) {
			logger.error("RemoteException caught in delete of resourceTwo:"
					+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught in delete of resourceTwo:"
							+ e.getMessage());
		}
		try {
			resourceTypeAccess.delete(resourceTypeOne);
		} catch (RemoteException e) {
			logger.error("RemoteException caught in delete of resourceTypeOne:"
					+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught in delete of resourceTypeOne:"
							+ e.getMessage());
		}
		try {
			resourceTypeAccess.delete(resourceTypeTwo);
		} catch (RemoteException e) {
			logger.error("RemoteException caught in delete of resourceTypeTwo:"
					+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught in delete of resourceTypeTwo:"
							+ e.getMessage());
		}
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
		this.cleanObjectsFromDB();
	}

	// The connection to the service classes
	DataContainerAccessHome dataContainerAccessHome = null;
	IDataContainerAccess dataContainerAccess = null;
	DataContainerGroupAccessHome dataContainerGroupAccessHome = null;
	DataContainerGroupAccess dataContainerGroupAccess = null;
	DataProducerAccessHome dataProducerAccessHome = null;
	DataProducerAccess dataProducerAccess = null;
	StandardVariableAccessHome standardVariableAccessHome = null;
	StandardVariableAccess standardVariableAccess = null;
	StandardUnitAccessHome standardUnitAccessHome = null;
	StandardUnitAccess standardUnitAccess = null;
	PersonAccessHome personAccessHome = null;
	PersonAccess personAccess = null;
	KeywordAccessHome keywordAccessHome = null;
	KeywordAccess keywordAccess = null;
	ResourceAccessHome resourceAccessHome = null;
	ResourceAccess resourceAccess = null;
	ResourceTypeAccessHome resourceTypeAccessHome = null;
	ResourceTypeAccess resourceTypeAccess = null;

	// The test DataContainers
	DataContainer dataContainerOne = null;
	DataContainer dataContainerTwo = null;

	// THe test DataProducers
	DataProducer dataProducerOne = null;
	DataProducer dataProducerTwo = null;

	String dataProducerOneStringRep = "DataProducer|" + "name=DataProducerOne|"
			+ "description=DataProducerOne Description|"
			+ "startDate=2003-05-05T16:11:44Z|"
			+ "endDate=2004-02-01T08:38:19Z|";

	String dataProducerTwoStringRep = "DataProducer|" + "name=DataProducerTwo|"
			+ "description=DataProducerTwo Description|"
			+ "startDate=2003-05-05T16:11:44Z|"
			+ "endDate=2004-02-01T08:38:19Z|";

	String dataContainerOneDodsUrlString = "http://dods.mbari.org/DataContainerOne.txt";
	String dataContainerTwoDodsUrlString = "http://dods.mbari.org/DataContainerTwo.txt";

	String dataContainerOneStringRep = "DataContainer|"
			+ "name=DataContainerOne|"
			+ "description=DataContainerOne Description|"
			+ "dataContainerType=File|" + "startDate=2003-05-05T16:11:44Z|"
			+ "endDate=2004-02-01T08:38:19Z|" + "original=true|"
			+ "uriString=http://kasatka.shore.mbari.org/DataContainerOne.txt|"
			+ "contentLength=50000|" + "mimeType=CSV|" + "numberOfRecords=800|"
			+ "dodsAccessible=false|" + "dodsUrlString="
			+ dataContainerOneDodsUrlString + "|" + "minLatitude=36.6|"
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
			+ "dodsUrlString=" + dataContainerTwoDodsUrlString + "|"
			+ "minLatitude=6.0|" + "maxLatitude=45.0|"
			+ "minLongitude=-255.34|" + "maxLongitude=-245.567|"
			+ "minDepth=10.5|" + "maxDepth=10000.5";

	// A Couple of RecordDescriptions
	RecordDescription recordDescriptionOne = null;

	RecordDescription recordDescriptionTwo = null;

	String recordDescriptionOneStringRep = "RecordDescription|"
			+ "recordType=1|" + "bufferStyle="
			+ RecordDescription.BUFFER_STYLE_ASCII + "|" + "bufferParseType="
			+ RecordDescription.PARSE_TYPE_FIXED_POSITION + "|"
			+ "bufferItemSeparator=,|" + "bufferLengthType="
			+ RecordDescription.BUFFER_LENGTH_TYPE_FIXED + "|"
			+ "recordTerminator=\\n|" + "parseable=true|" + "endian="
			+ RecordDescription.ENDIAN_BIG;

	String recordDescriptionTwoStringRep = "RecordDescription|"
			+ "recordType=1|" + "bufferStyle="
			+ RecordDescription.BUFFER_STYLE_BINARY + "|" + "bufferParseType="
			+ RecordDescription.PARSE_TYPE_ORDERED_POSITION + "|"
			+ "bufferItemSeparator=\\t|" + "bufferLengthType="
			+ RecordDescription.BUFFER_LENGTH_TYPE_VARIABLE + "|"
			+ "recordTerminator=\\r\\n|" + "parseable=false|" + "endian="
			+ RecordDescription.ENDIAN_LITTLE;

	// Some recordVariables
	RecordVariable recordVariableOne = null;
	RecordVariable recordVariableTwo = null;
	RecordVariable recordVariableThree = null;
	RecordVariable recordVariableFour = null;
	RecordVariable recordVariableFive = null;
	RecordVariable recordVariableSix = null;

	String recordVariableStringRepOne = "RecordVariable|"
			+ "name=RecordVarableOne|"
			+ "description=RecordVariableOne Description|"
			+ "longName=RecordVariableOne Long Name|"
			+ "format=RecordVariableOne Format|"
			+ "units=RecordVariableOne Units|" + "columnIndex=1|"
			+ "validMin=-99.99|" + "validMax=99.99|"
			+ "missingValue=999999999.99999|" + "accuracy=-.00001|"
			+ "displayMin=-100|" + "displayMax=100|"
			+ "referenceScale=RecordVariableOne ReferenceScale|"
			+ "conversionScale=10.00|" + "conversionOffset=-1|"
			+ "convertedUnits=RecordVariableOne Converted Units|"
			+ "sourceSensorID=1000|" + "parseRegExp=\\D+(\\d+)\\w+";

	String recordVariableStringRepTwo = "RecordVariable|"
			+ "name=RecordVarableTwo|"
			+ "description=RecordVariableTwo Description|"
			+ "longName=RecordVariableTwo Long Name|"
			+ "format=RecordVariableTwo Format|"
			+ "units=RecordVariableTwo Units|" + "columnIndex=2|"
			+ "validMin=-9.9|" + "validMax=9.9|"
			+ "missingValue=999999999.99999|" + "accuracy=-.1|"
			+ "displayMin=-10|" + "displayMax=10|"
			+ "referenceScale=RecordVariableTwo ReferenceScale|"
			+ "conversionScale=1.00|" + "conversionOffset=-5|"
			+ "convertedUnits=RecordVariableTwo Converted Units|"
			+ "sourceSensorID=1002|" + "parseRegExp=2-\\D+(\\d+)\\w+";

	String recordVariableStringRepThree = "RecordVariable|"
			+ "name=RecordVarableThree|"
			+ "description=RecordVariableThree Description|"
			+ "longName=RecordVariableThree Long Name|"
			+ "format=RecordVariableThree Format|"
			+ "units=RecordVariableThree Units|" + "columnIndex=3|"
			+ "validMin=-3.99|" + "validMax=3.99|" + "missingValue=3.99999|"
			+ "accuracy=-3.3|" + "displayMin=-30|" + "displayMax=30|"
			+ "referenceScale=RecordVariableThree ReferenceScale|"
			+ "conversionScale=3.00|" + "conversionOffset=-3|"
			+ "convertedUnits=RecordVariableThree Converted Units|"
			+ "sourceSensorID=1003|" + "parseRegExp=3-\\D+(\\d+)\\w+";

	String recordVariableStringRepFour = "RecordVariable|"
			+ "name=RecordVarableFour|"
			+ "description=RecordVariableFour Description|"
			+ "longName=RecordVariableFour Long Name|"
			+ "format=RecordVariableFour Format|"
			+ "units=RecordVariableFour Units|" + "columnIndex=4|"
			+ "validMin=-4.99|" + "validMax=4.99|" + "missingValue=49.99999|"
			+ "accuracy=-4.1|" + "displayMin=-40|" + "displayMax=40|"
			+ "referenceScale=RecordVariableFour ReferenceScale|"
			+ "conversionScale=40.00|" + "conversionOffset=-4|"
			+ "convertedUnits=RecordVariableFour Converted Units|"
			+ "sourceSensorID=1004|" + "parseRegExp=4-\\D+(\\d+)\\w+";

	String recordVariableStringRepFive = "RecordVariable|"
			+ "name=RecordVarableFive|"
			+ "description=RecordVariableFive Description|"
			+ "longName=RecordVariableFive Long Name|"
			+ "format=RecordVariableFive Format|"
			+ "units=RecordVariableFive Units|" + "columnIndex=5|"
			+ "validMin=-59.99|" + "validMax=59.99|"
			+ "missingValue=599.99999|" + "accuracy=-5.1|" + "displayMin=-50|"
			+ "displayMax=50|"
			+ "referenceScale=RecordVariableFive ReferenceScale|"
			+ "conversionScale=50.00|" + "conversionOffset=-5|"
			+ "convertedUnits=RecordVariableFive Converted Units|"
			+ "sourceSensorID=1005|" + "parseRegExp=5-\\D+(\\d+)\\w+";

	String recordVariableStringRepSix = "RecordVariable|"
			+ "name=RecordVarableSix|"
			+ "description=RecordVariableSix Description|"
			+ "longName=RecordVariableSix Long Name|"
			+ "format=RecordVariableSix Format|"
			+ "units=RecordVariableSix Units|" + "columnIndex=6|"
			+ "validMin=-6.99|" + "validMax=6.99|" + "missingValue=69.99999|"
			+ "accuracy=-6.6|" + "displayMin=-600|" + "displayMax=600|"
			+ "referenceScale=RecordVariableSix ReferenceScale|"
			+ "conversionScale=60.00|" + "conversionOffset=-6|"
			+ "convertedUnits=RecordVariableSix Converted Units|"
			+ "sourceSensorID=1006|" + "parseRegExp=6-\\D+(\\d+)\\w+";

	// The StandardVariable objects
	StandardVariable standardVariableOne = null;
	StandardVariable standardVariableTwo = null;
	String standardVariableOneStringRep = "StandardVariable|"
			+ "name=StandardVariableOne|"
			+ "description=StandardVariable one description|"
			+ "referenceScale=StandardVariableOne RefScale";
	String standardVariableTwoStringRep = "StandardVariable|"
			+ "name=StandardVariableTwo|"
			+ "description=StandardVariable two description|"
			+ "referenceScale=StandardVariableOne RefScale";

	// Some StandardUnits
	StandardUnit standardUnitOne = null;
	StandardUnit standardUnitTwo = null;
	String standardUnitOneStringRep = "StandardUnit|" + "name=StandardUnitOne|"
			+ "description=StandardUnit one description|"
			+ "longName=Standard Unit one long name|" + "symbol=SUONE";
	String standardUnitTwoStringRep = "StandardUnit|" + "name=StandardUnitTwo|"
			+ "description=StandardUnit two description|"
			+ "longName=Standard Unit two long name|" + "symbol=SUTWO";

	// Some person objects
	Person personOne = null;
	Person personTwo = null;
	String personOneStringRep = "Person|" + "firstname=John|" + "surname=Doe|"
			+ "organization=MBARI|" + "email=jdoe@mbari.org|"
			+ "username=jdoe|" + "password=dumbPassword|" + "status=active";

	String personTwoStringRep = "Person|" + "firstname=Jane|" + "surname=Doe|"
			+ "organization=MBARI|" + "email=janedoe@mbari.org|"
			+ "username=janedoe|" + "password=dumbPassword|" + "status=active";

	// Some Keywords
	Keyword keywordOne = null;
	Keyword keywordTwo = null;
	String keywordOneStringRep = "Keyword|" + "name=KeywordOne|"
			+ "description=KeywordOne Description";
	String keywordTwoStringRep = "Keyword|" + "name=KeywordTwo|"
			+ "description=KeywordTwo Description";

	// Some DataContainerGroups
	DataContainerGroup dataContainerGroupOne = null;
	DataContainerGroup dataContainerGroupTwo = null;
	String dataContainerGroupOneStringRep = "DataContainerGroup|"
			+ "name=DataContainerGroupOne|"
			+ "description=DataContainerGroupOne Description";
	String dataContainerGroupTwoStringRep = "DataContainerGroup|"
			+ "name=DataContainerGroupTwo|"
			+ "description=DataContainerGroupTwo Description";

	// Some Resources
	Resource resourceOne = null;
	Resource resourceTwo = null;
	String resourceOneStringRep = "Resource|" + "name=ResourceOne|"
			+ "description=ResourceOne Description|"
			+ "startDate=2004-01-01T00:00:00Z|"
			+ "endDate=2004-01-02T00:00:00Z|"
			+ "uriString=http://kasatka.shore.mbari.org/ResourceOne.txt|"
			+ "contentLength=50000|" + "mimeType=CSV";
	String resourceTwoStringRep = "Resource|" + "name=ResourceTwo|"
			+ "description=ResourceTwo Description|"
			+ "startDate=2004-02-01T00:00:00Z|"
			+ "endDate=2004-02-02T00:00:00Z|"
			+ "uriString=http://kasatka.shore.mbari.org/ResourceTwo.txt|"
			+ "contentLength=2|" + "mimeType=application/excel";

	// Some ResourceBLOBs
	ResourceBLOB resourceBLOBOne = null;
	ResourceBLOB resourceBLOBTwo = null;
	String resourceBLOBOneStringRep = "ResourceBLOB|" + "name=ResourceBLOBOne|"
			+ "description=ResourceBLOBOne Description";
	String resourceBLOBTwoStringRep = "ResourceBLOB|" + "name=ResourceBLOBTwo|"
			+ "description=ResourceBLOBTwo Description";

	// Some ResourceTypes
	ResourceType resourceTypeOne = null;
	ResourceType resourceTypeTwo = null;
	String resourceTypeOneStringRep = "ResourceType|" + "name=ResourceTypeOne|"
			+ "description=ResourceTypeOne Description";
	String resourceTypeTwoStringRep = "ResourceType|" + "name=ResourceTypeTwo|"
			+ "description=ResourceTypeTwo Description";

	// The delimiter for them all
	String delimiter = "|";

	// A date formatter
	XmlDateFormat xmlDateFormat = new XmlDateFormat();

	/**
	 * A log4J logger
	 */
	static Logger logger = Logger.getLogger(TestDataContainerAccess.class);
}