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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.Event;

import org.apache.log4j.Logger;
import org.hibernate.loader.CollectionAliases;

/**
 * This class tests the EventAccess service EJB to make sure all is well. There
 * has to be an SSDS server running somewhere for this to hit against and a
 * jndi.properties in the classpath so the tests can get to the server
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.4 $
 */
public class TestEventAccess extends TestAccessCase {

	/**
	 * A log4J logger
	 */
	static Logger logger = Logger.getLogger(TestEventAccess.class);

	/**
	 * A constructor
	 * 
	 * @param name
	 */
	public TestEventAccess(String name) {
		super(name);
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {

		// Setup the super class
		super.setUp();
	}

	/**
	 * Run suite of tests on event one
	 */
	public void testOne() {
		logger.debug("Event one is " + EVENT_ONE.toStringRepresentation("|"));
		this.eventTest(EVENT_ONE);
		logger.debug("Done with test one");
	}

	/**
	 * Run suite of tests on event two
	 */
	public void testTwo() {
		logger.debug("Event two is " + EVENT_TWO.toStringRepresentation("|"));
		eventTest(EVENT_TWO);
		logger.debug("Done with test two");
	}

	/**
	 * This method checks all the "findBy*" methods
	 */
	@SuppressWarnings("unchecked")
	public void testFindBys() {

		// Some initial statistics before testing
		int initialEventCount = -9999;
		try {
			initialEventCount = EVENT_ACCESS.countFindAllIDs();
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Make sure count is zero or greater
		assertTrue("Initial count should be zero or more",
				initialEventCount >= 0);

		logger.debug("testFindBys starting");
		// We will insert both events and then see if we can find them using
		// the find by methods
		Long eventOneId = null;
		Long eventTwoId = null;
		try {
			eventOneId = EVENT_ACCESS.insert(EVENT_ONE);
			eventTwoId = EVENT_ACCESS.insert(EVENT_TWO);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Make sure the returned ids are real
		assertNotNull(eventOneId);
		assertNotNull(eventTwoId);
		logger.debug("EventOneId = " + eventOneId);
		logger.debug("EventTwoId = " + eventTwoId);

		// OK both should be inserted. Exercise some of the findBys
		Event foundOne = null;
		try {
			foundOne = (Event) EVENT_ACCESS.findById(eventOneId, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The findById(Long) should have returned something",
				foundOne);
		assertEquals(
				"The foundOne (by Long) should be equal to the local EVENT_ONE",
				foundOne, EVENT_ONE);

		// Clear and try with different find by
		foundOne = null;
		try {
			foundOne = (Event) EVENT_ACCESS.findById(eventOneId.longValue(),
					false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The findById(long) should have returned something",
				foundOne);
		assertEquals(
				"The foundOne (by long) should be equal to the local EVENT_ONE",
				foundOne, EVENT_ONE);

		// Clear and try again
		foundOne = null;
		try {
			foundOne = (Event) EVENT_ACCESS.findById(eventOneId.toString(),
					false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The findById(String) should have returned something",
				foundOne);
		assertEquals(
				"The foundOne (by String) should be equal to the local EVENT_ONE",
				foundOne, EVENT_ONE);

		// OK, find by local event and check against ID
		Long foundOneId = null;
		try {
			foundOneId = EVENT_ACCESS.findId(EVENT_ONE);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("foundOneId should not be null", foundOneId);
		assertEquals("ID from insert and returned ID should be the same.",
				foundOneId.longValue(), eventOneId.longValue());

		// Now check find all
		Collection<Event> allEvents = null;
		try {
			allEvents = EVENT_ACCESS.findAll(null, null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("Collection returned by findAll should not be null",
				allEvents);
		assertTrue("Collection should contain local event one", allEvents
				.contains(EVENT_ONE));
		assertTrue("Collection should contain local event two", allEvents
				.contains(EVENT_TWO));
		logger.debug("allEvents = " + allEvents);

		// Now test the findEquivalent persistent object
		Event persistentOne = null;
		try {
			Long equivalentId = EVENT_ACCESS.findId(EVENT_ONE);
			persistentOne = (Event) EVENT_ACCESS.findById(equivalentId, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull(
				"persistent object that matches EVENT_ONE should not be null",
				persistentOne);
		assertEquals(
				"Persistent and local objects for EVENT_ONE should be equal",
				persistentOne, EVENT_ONE);
		assertEquals("The inserted ID and the persistent ID should be equal",
				eventOneId.longValue(), persistentOne.getId().longValue());

		// Test the find by name
		Collection<Event> eventsByName = null;
		try {
			eventsByName = EVENT_ACCESS.findByName(EVENT_ONE.getName(), true,
					null, null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertTrue("Collection should contain local event one", eventsByName
				.contains(EVENT_ONE));
		assertTrue("Collection should NOT contain local event two",
				!eventsByName.contains(EVENT_TWO));

		try {
			eventsByName = EVENT_ACCESS.findByName(EVENT_TWO.getName(), true,
					null, null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertTrue("Collection should NOT contain local event one",
				!eventsByName.contains(EVENT_ONE));
		assertTrue("Collection should contain local event two", eventsByName
				.contains(EVENT_TWO));

		// Make sure all IDs are found
		Collection<Long> allIds = null;
		try {
			allIds = EVENT_ACCESS.findAllIDs();
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertTrue("All IDs should contain ID one", allIds.contains(eventOneId));
		assertTrue("All IDs should contain ID two", allIds.contains(eventTwoId));

		// Count all the IDs and it should be 2 more than the original numbers
		int newCountIds = -99;
		try {
			newCountIds = EVENT_ACCESS.countFindAllIDs();
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertTrue("newCountIds is equal to or greater than zero",
				newCountIds >= 0);
		assertTrue("newCountIds is two more than the original count",
				newCountIds == (initialEventCount + 2));

		// Find by like name
		Collection<Event> likeNamedEvents = null;
		try {
			likeNamedEvents = EVENT_ACCESS.findByLikeName("est Event O", null,
					null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertTrue("LikeNamedEvents contains event one", likeNamedEvents
				.contains(EVENT_ONE));
		assertTrue("LikeNamedEvents does not containe event two",
				!likeNamedEvents.contains(EVENT_TWO));

		// Find by like name but happens to match exactly
		likeNamedEvents = null;
		try {
			likeNamedEvents = EVENT_ACCESS.findByLikeName(EVENT_ONE.getName(),
					null, null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertTrue("LikeNamedEvents contains event one", likeNamedEvents
				.contains(EVENT_ONE));
		assertTrue("LikeNamedEvents does not containe event two",
				!likeNamedEvents.contains(EVENT_TWO));

		// Find all names
		Collection<String> allNames = null;
		try {
			allNames = EVENT_ACCESS.findAllNames();
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertTrue("All names contains event one name", allNames
				.contains(EVENT_ONE.getName()));
		assertTrue("All names contains event two name", allNames
				.contains(EVENT_TWO.getName()));

		// By name and dates
		Event byNameAndDates = null;
		try {
			byNameAndDates = EVENT_ACCESS.findByNameAndDates(EVENT_ONE
					.getName(), EVENT_ONE.getStartDate(), EVENT_ONE
					.getEndDate(), false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("byNameAndDates should not be null", byNameAndDates);
		assertEquals("EventOne and byNameAndDates should be equal", EVENT_ONE,
				byNameAndDates);
		assertTrue("EventTow and byNameAndDates should not be equals",
				EVENT_TWO != byNameAndDates);

		// Change the end date and should get null
		byNameAndDates = null;
		try {
			byNameAndDates = EVENT_ACCESS.findByNameAndDates(EVENT_ONE
					.getName(), EVENT_ONE.getStartDate(), new Date(), false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNull("byNameAndDates should be null", byNameAndDates);

		// Find within date range
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		Calendar cal3 = Calendar.getInstance();
		Calendar cal4 = Calendar.getInstance();
		Calendar cal5 = Calendar.getInstance();

		// Set cal1 to before start of event one
		cal1.setTimeInMillis(EVENT_ONE.getStartDate().getTime());
		cal1.add(Calendar.HOUR, -96);
		// Set cal2 to in between dates for startdate
		cal2.setTimeInMillis(EVENT_ONE.getStartDate().getTime());
		cal2.add(Calendar.HOUR, 96);
		// Set cal3 to just before event one end
		cal3.setTimeInMillis(EVENT_ONE.getEndDate().getTime());
		cal3.add(Calendar.HOUR, -96);
		// Set cal4 to just after event one end
		cal4.setTimeInMillis(EVENT_ONE.getEndDate().getTime());
		cal4.add(Calendar.HOUR, 96);
		// Set cal 5 to after event two end date
		cal5.setTimeInMillis(EVENT_TWO.getEndDate().getTime());
		cal5.add(Calendar.HOUR, 96);

		// Find event one
		Collection<Event> foundEvents = null;
		try {
			foundEvents = EVENT_ACCESS.findWithinDateRange(cal1.getTime(), cal2
					.getTime(), null, null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertTrue("Should contain event one", foundEvents.contains(EVENT_ONE));
		assertTrue("Should not contain event two", !foundEvents
				.contains(EVENT_TWO));

		foundEvents = null;
		try {
			foundEvents = EVENT_ACCESS.findWithinDateRange(cal1.getTime(), cal3
					.getTime(), null, null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertTrue("Should contain event one", foundEvents.contains(EVENT_ONE));
		assertTrue("Should contain event two", foundEvents.contains(EVENT_TWO));

		foundEvents = null;
		try {
			foundEvents = EVENT_ACCESS.findWithinDateRange(cal2.getTime(), cal3
					.getTime(), null, null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertTrue("Should contain event one", foundEvents.contains(EVENT_ONE));
		assertTrue("Should contain event two", foundEvents.contains(EVENT_TWO));

		foundEvents = null;
		try {
			foundEvents = EVENT_ACCESS.findWithinDateRange(cal2.getTime(), cal5
					.getTime(), null, null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertTrue("Should contain event one", foundEvents.contains(EVENT_ONE));
		assertTrue("Should contain event two", foundEvents.contains(EVENT_TWO));

		foundEvents = null;
		try {
			foundEvents = EVENT_ACCESS.findWithinDateRange(cal1.getTime(), cal5
					.getTime(), null, null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertTrue("Should contain event one", foundEvents.contains(EVENT_ONE));
		assertTrue("Should contain event two", foundEvents.contains(EVENT_TWO));

		foundEvents = null;
		try {
			foundEvents = EVENT_ACCESS.findWithinDateRange(cal4.getTime(), cal5
					.getTime(), null, null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertTrue("Should not contain event one", !foundEvents
				.contains(EVENT_ONE));
		assertTrue("Should contain event two", foundEvents.contains(EVENT_TWO));
	}

	/**
	 * This is the suite of tests to run on a event
	 * 
	 * @param event
	 */
	private void eventTest(Event event) {

		// The ID of the event
		Long eventId = null;
		eventId = testInsert(event, EVENT_ACCESS);

		// Now query back by ID and make sure all attributes are equal
		Event persistedEvent = null;

		try {
			persistedEvent = (Event) EVENT_ACCESS.findById(eventId, false);
		} catch (RemoteException e1) {
			logger.error("RemoteException caught during findById: "
					+ e1.getMessage());
		} catch (MetadataAccessException e1) {
			logger.error("MetadataAccessException caught during findById: "
					+ e1.getMessage());
		}

		// Now check that they are equal
		assertEquals("The two events should be considered equal", event,
				persistedEvent);

		// Check all the getter methods are equal
		testEqualityOfAllGetters(event, persistedEvent);

		// Create a map with the values to update
		HashMap<String, Object[]> variablesToUpdate = new HashMap<String, Object[]>();

		// Change the description
		Object[] variable1 = new Object[1];
		variable1[0] = new String("Updated Description");
		variablesToUpdate.put("Description", variable1);

		testUpdate(persistedEvent, variablesToUpdate, EVENT_ACCESS);

		testDelete(persistedEvent, EVENT_ACCESS);
	}

}