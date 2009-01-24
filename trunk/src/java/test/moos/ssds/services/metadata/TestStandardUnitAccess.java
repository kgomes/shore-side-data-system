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
import moos.ssds.metadata.StandardUnit;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataFactory;
import moos.ssds.services.metadata.StandardUnitAccess;
import moos.ssds.services.metadata.StandardUnitAccessHome;
import moos.ssds.services.metadata.StandardUnitAccessUtil;

import org.apache.log4j.Logger;

/**
 * This class tests the StandardUnitAccess service EJB to make sure all is well.
 * There has to be an SSDS server running somewhere for this to hit against and
 * a jndi.properties in the classpath so the tests can get to the server
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.10 $
 */
public class TestStandardUnitAccess extends TestAccessCase {

    /**
     * A constructor
     * 
     * @param name
     */
    public TestStandardUnitAccess(String name) {
        super(name);
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {

        // Setup the super class
        super.setUp();

        // Grab a standardUnit facade
        try {
            standardUnitAccessHome = StandardUnitAccessUtil.getHome();
        } catch (NamingException ex) {
            logger
                .error("NamingException caught while getting standardUnitAccessHome "
                    + "from app server: " + ex.getMessage());
        }
        try {
            standardUnitAccess = standardUnitAccessHome.create();
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught while creating standardUnitAccess interface: "
                    + e.getMessage());
        } catch (CreateException e) {
            logger
                .error("CreateException caught while creating standardUnitAccess interface: "
                    + e.getMessage());
        }

        try {
            standardUnitOne = (StandardUnit) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    standardUnitOneStringRep, delimiter);
            standardUnitTwo = (StandardUnit) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    standardUnitTwoStringRep, delimiter);
        } catch (MetadataException e) {
            logger
                .error("MetadataException caught trying to create two StandardUnit objects: "
                    + e.getMessage());
        } catch (ClassCastException cce) {
            logger
                .error("ClassCastException caught trying to create two StandardUnit objects: "
                    + cce.getMessage());
        }
    }

    /**
     * Run suite of tests on standardUnit one
     */
    public void testOne() {
        logger.debug("StandardUnit one is "
            + standardUnitOne.toStringRepresentation("|"));
        this.standardUnitTest(standardUnitOne);
        logger.debug("Done with test one");
    }

    /**
     * Run suite of tests on standardUnit two
     */
    public void testTypeTwo() {
        logger.debug("StandardUnit two is "
            + standardUnitTwo.toStringRepresentation("|"));
        standardUnitTest(standardUnitTwo);
        logger.debug("Done with test two");
    }

    /**
     * This is the suite of tests to run on a standardUnit
     * 
     * @param device
     */
    private void standardUnitTest(StandardUnit standardUnit) {

        // The ID of the standardUnit
        Long standardUnitId = null;
        standardUnitId = testInsert(standardUnit, standardUnitAccess);

        // Now query back by ID and make sure all attributes are equal
        StandardUnit persistedStandardUnit = null;

        try {
            persistedStandardUnit = (StandardUnit) standardUnitAccess.findById(
                standardUnitId, false);
        } catch (RemoteException e1) {
            logger.error("RemoteException caught during findById: "
                + e1.getMessage());
        } catch (MetadataAccessException e1) {
            logger.error("MetadataAccessException caught during findById: "
                + e1.getMessage());
        }

        // Now check that they are equal
        assertEquals("The two standardUnits should be considered equal",
            standardUnit, persistedStandardUnit);

        // Check all the getter methods are equal
        testEqualityOfAllGetters(standardUnit, persistedStandardUnit);

        // Create a map with the values to update
        HashMap variablesToUpdate = new HashMap();

        // Change the description
        Object[] variable2 = new Object[1];
        variable2[0] = new String("Updated Description");
        variablesToUpdate.put("Description", variable2);

        testUpdate(persistedStandardUnit, variablesToUpdate, standardUnitAccess);

        testDelete(persistedStandardUnit, standardUnitAccess);
    }

    /**
     * This test checks that all the find by methods work correctly
     */
    public void testFindBys() {
        // OK, let's fist let's null out all ID's
        standardUnitOne.setId(null);
        standardUnitTwo.setId(null);

        // Grab the count of all before insert
        int countBeforeInserts = 0;
        try {
            countBeforeInserts = standardUnitAccess.countFindAllIDs();
        } catch (RemoteException e3) {
            assertTrue("RemoteException caught : " + e3.getMessage(), false);
        } catch (MetadataAccessException e3) {
            assertTrue("MetadataAccessException caught : " + e3.getMessage(),
                false);
        }
        // OK now insert both standardUnits
        Long standardUnitOneId = null;
        Long standardUnitTwoId = null;
        try {
            standardUnitOneId = standardUnitAccess.insert(standardUnitOne);
            standardUnitTwoId = standardUnitAccess.insert(standardUnitTwo);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught inserting standardUnits in find by test: "
                    + e.getMessage());
            assertTrue(
                "RemoteException caught inserting standardUnits in find by test: "
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught inserting standardUnits in find by test: "
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught inserting standardUnits in find by test: "
                    + e.getMessage(), false);
        }
        logger.debug("StandardUnit one's ID is " + standardUnitOneId);
        logger.debug("StandardUnit two's ID is " + standardUnitTwoId);

        // OK, now let's do the find by id's
        StandardUnit persistedStandardUnitOne = null;
        try {
            persistedStandardUnitOne = (StandardUnit) standardUnitAccess
                .findById(standardUnitOneId, false);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findById(Long):"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findById(Long):"
                    + e.getMessage(), false);
        }

        // Make sure they are equal
        assertEquals("The two standardUnit one's should be equal",
            standardUnitOne, persistedStandardUnitOne);

        // Now by little long
        persistedStandardUnitOne = null;
        try {
            persistedStandardUnitOne = (StandardUnit) standardUnitAccess
                .findById(standardUnitOneId.longValue(), false);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findById(long):"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findById(long):"
                    + e.getMessage(), false);
        }
        // Make sure they are equal
        assertEquals("The two standardUnit one's should be equal",
            standardUnitOne, persistedStandardUnitOne);

        // Now find by string
        persistedStandardUnitOne = null;
        try {
            persistedStandardUnitOne = (StandardUnit) standardUnitAccess
                .findById(standardUnitOneId.toString(), false);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findById(String):"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findById(String):"
                    + e.getMessage(), false);
        }
        // Make sure they are equal
        assertEquals("The two standardUnit one's should be equal",
            standardUnitOne, persistedStandardUnitOne);

        // Now try the find ID method
        Long idByStandardUnitFind = null;
        try {
            idByStandardUnitFind = standardUnitAccess.findId(standardUnitOne);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findId(StandardUnit):"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findId(StandardUnit):"
                    + e.getMessage(), false);
        }
        assertEquals(
            "StandardUnit ids should be equal after findId(StandardUnit)",
            standardUnitOneId, idByStandardUnitFind);

        // Check the find equivalent persistent object
        StandardUnit equivalentStandardUnitOne = null;
        try {
            equivalentStandardUnitOne = (StandardUnit) standardUnitAccess
                .findEquivalentPersistentObject(standardUnitOne, false);
        } catch (RemoteException e1) {
            assertTrue(
                "RemoteException caught trying to findEquivalentPersistentObject(StandardUnit):"
                    + e1.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findEquivalentPersistentObject(StandardUnit):"
                    + e.getMessage(), false);
        }
        assertEquals("Id of the equivalent persistent object"
            + " should match that of insert", standardUnitOneId,
            equivalentStandardUnitOne.getId());
        assertEquals(
            "StandardUnit after findEquivalentObject should be equal:",
            standardUnitOne, equivalentStandardUnitOne);

        // Now make sure all the standardUnits are returned in the findAll
        // method
        Collection allStandardUnits = null;
        try {
            allStandardUnits = standardUnitAccess.findAll(null, null, false);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findAll():"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue("MetadataAccessException caught trying to findAll():"
                + e.getMessage(), false);
        }
        assertTrue("findAll should have standardUnitOne", allStandardUnits
            .contains(standardUnitOne));
        assertTrue("findAll should have standardUnitTwo", allStandardUnits
            .contains(standardUnitTwo));

        // Now test the find by exact name
        StandardUnit findByNameStandardUnit = null;
        try {
            findByNameStandardUnit = (StandardUnit) standardUnitAccess
                .findByName("StandardUnit");
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findByName():"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue("MetadataAccessException caught trying to findByName():"
                + e.getMessage(), false);
        }
        assertNull("findByName(StandardUnit) should be an empty return",
            findByNameStandardUnit);
        findByNameStandardUnit = null;
        try {
            findByNameStandardUnit = (StandardUnit) standardUnitAccess
                .findByName("StandardUnitOne");
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findByName():"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue("MetadataAccessException caught trying to findByName():"
                + e.getMessage(), false);
        }
        assertNotNull("findByName(StandardUnitOne) should found something",
            findByNameStandardUnit);
        assertEquals("findByNameStandardUnits should be StandardUnitOne",
            findByNameStandardUnit, standardUnitOne);

        // Now try to find by like name. If I search for "StandardUnitO", I
        // should
        // get StandardUnitOne, but if I search for "StandardUnit", I should get
        // both
        Collection likeNameStandardUnits = null;
        try {
            likeNameStandardUnits = standardUnitAccess
                .findByLikeName("StandardUnitO");
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findByLikeName():"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findByLikeName():"
                    + e.getMessage(), false);
        }
        assertTrue("findAll should have standardUnitOne", likeNameStandardUnits
            .contains(standardUnitOne));
        assertTrue("findAll should NOT have standardUnitTwo",
            !likeNameStandardUnits.contains(standardUnitTwo));

        likeNameStandardUnits = null;
        try {
            likeNameStandardUnits = standardUnitAccess
                .findByLikeName("StandardUnit");
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findByLikeName():"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findByLikeName():"
                    + e.getMessage(), false);
        }
        assertTrue("findAll should have standardUnitOne", likeNameStandardUnits
            .contains(standardUnitOne));
        assertTrue("findAll should have standardUnitTwo", likeNameStandardUnits
            .contains(standardUnitTwo));

        // Now test the find by exact symbol
        Collection findBySymbolStandardUnits = null;
        try {
            findBySymbolStandardUnits = standardUnitAccess.findBySymbol("SUON");
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findBySymbol():"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findBySymbol():"
                    + e.getMessage(), false);
        }
        assertTrue("findBySymbol() should be an empty return",
            findBySymbolStandardUnits.size() == 0);
        findBySymbolStandardUnits = null;
        try {
            findBySymbolStandardUnits = standardUnitAccess
                .findBySymbol("SUONE");
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findByName():"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue("MetadataAccessException caught trying to findByName():"
                + e.getMessage(), false);
        }
        assertTrue(
            "findBySymbol(SUONE) should return a collection of size one.",
            findBySymbolStandardUnits.size() == 1);
        assertTrue("findBySymbolStandardUnits should contain StandardUnitOne",
            findBySymbolStandardUnits.contains(standardUnitOne));

        // Now try to find by like symbol. If I search for "SUO", I
        // should
        // get StandardUnitOne, but if I search for "SU", I should get
        // both
        Collection likeSymbolStandardUnits = null;
        try {
            likeSymbolStandardUnits = standardUnitAccess
                .findByLikeSymbol("SUO");
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findByLikeSymbol():"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findByLikeSymbol():"
                    + e.getMessage(), false);
        }
        assertTrue("findByLikeSymbol should have standardUnitOne",
            likeSymbolStandardUnits.contains(standardUnitOne));
        assertTrue("findByLikeSymbol should NOT have standardUnitTwo",
            !likeSymbolStandardUnits.contains(standardUnitTwo));

        likeSymbolStandardUnits = null;
        try {
            likeSymbolStandardUnits = standardUnitAccess.findByLikeSymbol("SU");
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findByLikeSymbol():"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findByLikeSymbol():"
                    + e.getMessage(), false);
        }
        assertTrue("findByLikeSymbol should have standardUnitOne",
            likeSymbolStandardUnits.contains(standardUnitOne));
        assertTrue("findByLikeSymbol should have standardUnitTwo",
            likeSymbolStandardUnits.contains(standardUnitTwo));

        // Now check the find all names and find all ID's
        Collection allIds = null;
        Collection allNames = null;
        try {
            allIds = standardUnitAccess.findAllIDs();
            allNames = standardUnitAccess.findAllNames();
        } catch (RemoteException e2) {
            assertTrue("RemoteException caught trying to "
                + "findAllStandardUnitIDs/findAllStandardUnitNames():"
                + e2.getMessage(), false);
        } catch (MetadataAccessException e2) {
            assertTrue("MetadataAccessException caught trying to "
                + "findAllStandardUnitIDs/findAllStandardUnitNames():"
                + e2.getMessage(), false);
        }

        // OK allIDs and names should have 2 more than before inserts
        assertNotNull("allIds should not be null", allIds);
        assertNotNull("allNames should not be null", allNames);
        assertTrue("allIds should have two entries.",
            allIds.size() == (countBeforeInserts + 2));
        assertTrue("allNames should have two entries",
            allNames.size() == (countBeforeInserts + 2));
        // Now make sure the ids and names are all in the returned collection
        assertTrue("StandardUnit ID one should be there: ", allIds
            .contains(standardUnitOneId));
        assertTrue("StandardUnit ID two should be there: ", allIds
            .contains(standardUnitTwoId));

        assertTrue("StandardUnit name one should be there: ", allNames
            .contains("StandardUnitOne"));
        assertTrue("StandardUnit name two should be there: ", allNames
            .contains("StandardUnitTwo"));

        // Now clean up
        try {
            standardUnitAccess.delete(standardUnitOne);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
        try {
            standardUnitAccess.delete(standardUnitTwo);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
        // Delete any standardUnit objects as we don't want any leftover's if a
        // test
        // fails
        try {
            standardUnitAccess.delete(standardUnitOne);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
        try {
            standardUnitAccess.delete(standardUnitTwo);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
    }

    // The connection to the service classes
    StandardUnitAccessHome standardUnitAccessHome = null;
    StandardUnitAccess standardUnitAccess = null;

    // The test StandardUnits
    String standardUnitOneStringRep = "StandardUnit," + "name=StandardUnitOne,"
        + "description=StandardUnit one description,"
        + "longName=Standard Unit one long name," + "symbol=SUONE";
    String standardUnitTwoStringRep = "StandardUnit," + "name=StandardUnitTwo,"
        + "description=StandardUnit two description,"
        + "longName=Standard Unit two long name," + "symbol=SUTWO";

    // The delimiter
    String delimiter = ",";

    // The objects
    StandardUnit standardUnitOne = null;
    StandardUnit standardUnitTwo = null;

    /**
     * A log4J logger
     */
    static Logger logger = Logger.getLogger(TestStandardUnitAccess.class);
}