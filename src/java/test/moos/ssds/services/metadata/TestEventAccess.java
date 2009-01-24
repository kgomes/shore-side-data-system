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

import javax.ejb.CreateException;
import javax.naming.NamingException;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.Event;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataFactory;
import moos.ssds.services.metadata.EventAccess;
import moos.ssds.services.metadata.EventAccessHome;
import moos.ssds.services.metadata.EventAccessUtil;

import org.apache.log4j.Logger;

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

        // Grab a event facade
        try {
            eventAccessHome = EventAccessUtil.getHome();
        } catch (NamingException ex) {
            logger
                .error("NamingException caught while getting eventAccessHome from app server: "
                    + ex.getMessage());
        }
        try {
            eventAccess = eventAccessHome.create();
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught while creating eventAccess interface: "
                    + e.getMessage());
        } catch (CreateException e) {
            logger
                .error("CreateException caught while creating eventAccess interface: "
                    + e.getMessage());
        }
    }

    /**
     * Run suite of tests on event one
     */
    public void testOne() {
        this.setupObjects();
        logger.debug("Event one is " + eventOne.toStringRepresentation("|"));
        this.eventTest(eventOne);
        logger.debug("Done with test one");
    }

    /**
     * Run suite of tests on event two
     */
    public void testTwo() {
        this.setupObjects();
        logger.debug("Event two is " + eventTwo.toStringRepresentation("|"));
        eventTest(eventTwo);
        logger.debug("Done with test two");
    }

    /**
     * This method checks all the "findBy*" methods
     */
    public void testFindBys() {
        this.setupObjects();
        logger.debug("testFindBys starting");
        // We will insert both events and then see if we can find them using
        // the find by methods
        Long eventOneId = null;
        Long eventTwoId = null;
        try {
            eventOneId = eventAccess.insert(eventOne);
            eventTwoId = eventAccess.insert(eventTwo);
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

        // OK both should be inserted. Excercise some of the findBys
        Event foundOne = null;
        try {
            foundOne = (Event) eventAccess.findById(eventOneId, false);
        } catch (RemoteException e) {
            assertTrue("RemoteException was thrown: " + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue("MetadataException was thrown: " + e.getMessage(), false);
        }
        assertNotNull("The findById(Long) should have returned something",
            foundOne);
        assertEquals(
            "The foundOne (by Long) should be equal to the local eventOne",
            foundOne, eventOne);

        // Clear and try with different find by
        foundOne = null;
        try {
            foundOne = (Event) eventAccess.findById(eventOneId.longValue(),
                false);
        } catch (RemoteException e) {
            assertTrue("RemoteException was thrown: " + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue("MetadataException was thrown: " + e.getMessage(), false);
        }
        assertNotNull("The findById(long) should have returned something",
            foundOne);
        assertEquals(
            "The foundOne (by long) should be equal to the local eventOne",
            foundOne, eventOne);

        // Clear and try again
        foundOne = null;
        try {
            foundOne = (Event) eventAccess.findById(eventOneId.toString(),
                false);
        } catch (RemoteException e) {
            assertTrue("RemoteException was thrown: " + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue("MetadataException was thrown: " + e.getMessage(), false);
        }
        assertNotNull("The findById(String) should have returned something",
            foundOne);
        assertEquals(
            "The foundOne (by String) should be equal to the local eventOne",
            foundOne, eventOne);

        // OK, find by local event and check against ID
        Long foundOneId = null;
        try {
            foundOneId = eventAccess.findId(eventOne);
        } catch (RemoteException e) {
            assertTrue("RemoteException was thrown: " + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue("MetadataException was thrown: " + e.getMessage(), false);
        }
        assertNotNull("foundOneId should not be null", foundOneId);
        assertEquals("ID from insert and returned ID should be the same.",
            foundOneId.longValue(), eventOneId.longValue());

        // Now check find all
        Collection allEvents = null;
        try {
            allEvents = eventAccess.findAll(null, null, false);
        } catch (RemoteException e) {
            assertTrue("RemoteException was thrown: " + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue("MetadataException was thrown: " + e.getMessage(), false);
        }
        assertNotNull("Collection returned by findAll should not be null",
            allEvents);
        assertTrue("Collection should contain local event one", allEvents
            .contains(eventOne));
        assertTrue("Collection should contain local event two", allEvents
            .contains(eventTwo));
        logger.debug("allEvents = " + allEvents);

        // Now test the findEquivalent persistent object
        Event persistentOne = null;
        try {
            Long equivalentId = eventAccess.findId(eventOne);
            persistentOne = (Event) eventAccess.findById(equivalentId, false);
        } catch (RemoteException e) {
            assertTrue("RemoteException was thrown: " + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue("MetadataException was thrown: " + e.getMessage(), false);
        }
        assertNotNull(
            "persistent object that matches eventOne should not be null",
            persistentOne);
        assertEquals(
            "Persistent and local objects for eventOne should be equal",
            persistentOne, eventOne);
        assertEquals("The inserted ID and the persistent ID should be equal",
            eventOneId.longValue(), persistentOne.getId().longValue());

    }

    /**
     * This is the suite of tests to run on a event
     * 
     * @param event
     */
    private void eventTest(Event event) {

        // The ID of the event
        Long eventId = null;
        eventId = testInsert(event, eventAccess);

        // Now query back by ID and make sure all attributes are equal
        Event persistedEvent = null;

        try {
            persistedEvent = (Event) eventAccess.findById(eventId, false);
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
        HashMap variablesToUpdate = new HashMap();

        // Change the description
        Object[] variable1 = new Object[1];
        variable1[0] = new String("Updated Description");
        variablesToUpdate.put("Description", variable1);

        testUpdate(persistedEvent, variablesToUpdate, eventAccess);

        testDelete(persistedEvent, eventAccess);
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
        this.cleanObjectFromDB();
    }

    /**
     * This method instantiates clean data producers and data containers from
     * the string representations
     */
    private void setupObjects() {
        try {
            eventOne = (Event) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    eventOneStringRep, delimiter);
            eventTwo = (Event) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    eventTwoStringRep, delimiter);
        } catch (MetadataException e) {
            logger.error("MetadataException caught trying to create objects: "
                + e.getMessage());
        } catch (ClassCastException cce) {
            logger.error("ClassCastException caught trying to create objects: "
                + cce.getMessage());
        }
    }

    private void cleanObjectFromDB() {
        this.setupObjects();
        // Delete any dataProducer objects as we don't want any leftover's if a
        // test fails
        try {
            eventAccess.delete(eventOne);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
        try {
            eventAccess.delete(eventTwo);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
    }

    // The connection to the service classes
    EventAccessHome eventAccessHome = null;
    EventAccess eventAccess = null;

    // The test Events
    Event eventOne = null;
    Event eventTwo = null;
    String eventOneStringRep = "Event|" + "name=EventOne|"
        + "description=EventOne Description|"
        + "startDate=2004-07-02T01:00:00Z|" + "endDate=2004-07-02T01:00:10Z";
    String eventTwoStringRep = "Event|" + "name=EventTwo|"
        + "description=EventTwo Description|"
        + "startDate=2004-07-04T09:00:00Z|" + "endDate=2004-07-04T10:00:10Z";

    String delimiter = "|";

    /**
     * A log4J logger
     */
    static Logger logger = Logger.getLogger(TestEventAccess.class);
}