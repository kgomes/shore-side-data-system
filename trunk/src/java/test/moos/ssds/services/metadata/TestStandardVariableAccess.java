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
import javax.naming.NamingException;

import moos.ssds.metadata.StandardUnit;
import moos.ssds.metadata.StandardVariable;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataFactory;
import moos.ssds.services.metadata.StandardUnitAccess;
import moos.ssds.services.metadata.StandardUnitAccessHome;
import moos.ssds.services.metadata.StandardUnitAccessUtil;
import moos.ssds.services.metadata.StandardVariableAccess;
import moos.ssds.services.metadata.StandardVariableAccessHome;
import moos.ssds.services.metadata.StandardVariableAccessUtil;
import moos.ssds.dao.util.MetadataAccessException;

import org.apache.log4j.Logger;

/**
 * This class tests the StandardVariableAccess service EJB to make sure all is
 * well. There has to be an SSDS server running somewhere for this to hit
 * against and a jndi.properties in the classpath so the tests can get to the
 * server
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.10 $
 */
public class TestStandardVariableAccess extends TestAccessCase {

    /**
     * A constructor
     * 
     * @param name
     */
    public TestStandardVariableAccess(String name) {
        super(name);
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {

        // Setup the super class
        super.setUp();

        // Grab a standardVariable facade
        try {
            standardVariableAccessHome = StandardVariableAccessUtil.getHome();
            standardUnitAccessHome = StandardUnitAccessUtil.getHome();
        } catch (NamingException ex) {
            logger
                .error("NamingException caught while getting standardVariableAccessHome "
                    + "from app server: " + ex.getMessage());
        }
        try {
            standardVariableAccess = standardVariableAccessHome.create();
            standardUnitAccess = standardUnitAccessHome.create();
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught while creating standardVariableAccess interface: "
                    + e.getMessage());
        } catch (CreateException e) {
            logger
                .error("CreateException caught while creating standardVariableAccess interface: "
                    + e.getMessage());
        }

        try {
            standardVariableOne = (StandardVariable) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    standardVariableOneStringRep, delimiter);
            standardVariableTwo = (StandardVariable) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    standardVariableTwoStringRep, delimiter);
            standardUnitOne = (StandardUnit) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    standardUnitOneStringRep, delimiter);
            standardUnitTwo = (StandardUnit) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    standardUnitTwoStringRep, delimiter);
        } catch (MetadataException e) {
            logger
                .error("MetadataException caught trying to create two StandardVariable objects: "
                    + e.getMessage());
        } catch (ClassCastException cce) {
            logger
                .error("ClassCastException caught trying to create two StandardVariable objects: "
                    + cce.getMessage());
        }
    }

    /**
     * Run suite of tests on standardVariable one
     */
    public void testOne() {
        logger.debug("StandardVariable one is "
            + standardVariableOne.toStringRepresentation("|"));
        this.standardVariableTest(standardVariableOne);
        logger.debug("Done with test one");
    }

    /**
     * Run suite of tests on standardVariable two
     */
    public void testTypeTwo() {
        logger.debug("StandardVariable two is "
            + standardVariableTwo.toStringRepresentation("|"));
        standardVariableTest(standardVariableTwo);
        logger.debug("Done with test two");
    }

    /**
     * This is the suite of tests to run on a standardVariable
     * 
     * @param device
     */
    private void standardVariableTest(StandardVariable standardVariable) {

        // The ID of the standardVariable
        Long standardVariableId = null;
        standardVariableId = testInsert(standardVariable,
            standardVariableAccess);

        // Now query back by ID and make sure all attributes are equal
        StandardVariable persistedStandardVariable = null;

        try {
            persistedStandardVariable = (StandardVariable) standardVariableAccess
                .findById(standardVariableId, false);
        } catch (RemoteException e1) {
            logger.error("RemoteException caught during findById: "
                + e1.getMessage());
        } catch (MetadataAccessException e1) {
            logger.error("MetadataAccessException caught during findById: "
                + e1.getMessage());
        }

        // Now check that they are equal
        assertEquals("The two standardVariables should be considered equal",
            standardVariable, persistedStandardVariable);

        // Check all the getter methods are equal
        testEqualityOfAllGetters(standardVariable, persistedStandardVariable);

        // Create a map with the values to update
        HashMap variablesToUpdate = new HashMap();

        // Change the description
        Object[] variable2 = new Object[1];
        variable2[0] = new String("Updated Description");
        variablesToUpdate.put("Description", variable2);

        testUpdate(persistedStandardVariable, variablesToUpdate,
            standardVariableAccess);

        testDelete(persistedStandardVariable, standardVariableAccess);
    }

    /**
     * This test checks that all the find by methods work correctly
     */
    public void testFindBys() {

        // Grab the count before starting insert
        int countBeforeInserts = 0;
        int countReferenceScalesBeforeInsert = 0;
        int countNamesBeforeInsert = 0;
        try {
            countBeforeInserts = standardVariableAccess.countFindAllIDs();
            countReferenceScalesBeforeInsert = standardVariableAccess
                .findAllReferenceScales().size();
            countNamesBeforeInsert = standardVariableAccess.findAllNames()
                .size();
        } catch (RemoteException e3) {
            assertTrue("RemoteException caught: " + e3.getMessage(), false);
        } catch (MetadataAccessException e3) {
            assertTrue("MetadataAccessException caught: " + e3.getMessage(),
                false);
        }
        // OK, let's fist let's null out all ID's
        standardVariableOne.setId(null);
        standardVariableTwo.setId(null);
        // OK now insert both standardVariables
        Long standardVariableOneId = null;
        Long standardVariableTwoId = null;
        try {
            standardVariableOneId = standardVariableAccess
                .insert(standardVariableOne);
            standardVariableTwoId = standardVariableAccess
                .insert(standardVariableTwo);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught inserting standardVariables in find by test: "
                    + e.getMessage());
            assertTrue(
                "RemoteException caught inserting standardVariables in find by test: "
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught inserting standardVariables in find by test: "
                    + e.getMessage());
            assertTrue(
                "MetadataAccessException caught inserting standardVariables in find by test: "
                    + e.getMessage(), false);
        }
        logger.debug("StandardVariable one's ID is " + standardVariableOneId);
        logger.debug("StandardVariable two's ID is " + standardVariableTwoId);

        // OK, now let's do the find by id's
        StandardVariable persistedStandardVariableOne = null;
        try {
            persistedStandardVariableOne = (StandardVariable) standardVariableAccess
                .findById(standardVariableOneId, false);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findById(Long):"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findById(Long):"
                    + e.getMessage(), false);
        }
        // Make sure they are equal
        assertEquals("The two standardVariable one's should be equal",
            standardVariableOne, persistedStandardVariableOne);
        // Now by little long
        persistedStandardVariableOne = null;
        try {
            persistedStandardVariableOne = (StandardVariable) standardVariableAccess
                .findById(standardVariableOneId.longValue(), false);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findById(long):"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findById(long):"
                    + e.getMessage(), false);
        }

        // Make sure they are equal
        assertEquals("The two standardVariable one's should be equal",
            standardVariableOne, persistedStandardVariableOne);

        // Now find by string
        persistedStandardVariableOne = null;
        try {
            persistedStandardVariableOne = (StandardVariable) standardVariableAccess
                .findById(standardVariableOneId.toString(), false);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findById(String):"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findById(String):"
                    + e.getMessage(), false);
        }

        // Make sure they are equal
        assertEquals("The two standardVariable one's should be equal",
            standardVariableOne, persistedStandardVariableOne);

        // Now try the find ID method
        Long idByStandardVariableFind = null;
        try {
            idByStandardVariableFind = standardVariableAccess
                .findId(standardVariableOne);
        } catch (RemoteException e) {
            assertTrue(
                "RemoteException caught trying to findId(StandardVariable):"
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findId(StandardVariable):"
                    + e.getMessage(), false);
        }
        assertEquals(
            "StandardVariable ids should be equal after findId(StandardVariable)",
            standardVariableOneId, idByStandardVariableFind);

        // Check the find equivalent persistent object
        StandardVariable equivalentStandardVariableOne = null;
        try {
            Long equivalentId = standardVariableAccess
                .findId(standardVariableOne);
            equivalentStandardVariableOne = (StandardVariable) standardVariableAccess
                .findById(equivalentId, false);
        } catch (RemoteException e1) {
            assertTrue(
                "RemoteException caught trying to findEquivalentPersistentObject(StandardVariable):"
                    + e1.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findEquivalentPersistentObject(StandardVariable):"
                    + e.getMessage(), false);
        }
        assertEquals("Id of the equivalent persistent object"
            + " should match that of insert", standardVariableOneId,
            equivalentStandardVariableOne.getId());

        // Now make sure all the standardVariables are returned in the findAll
        // method
        Collection allStandardVariables = null;
        try {
            allStandardVariables = standardVariableAccess.findAll(null, null,
                false);
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findAll():"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue("MetadataAccessException caught trying to findAll():"
                + e.getMessage(), false);
        }
        assertTrue("findAll should have standardVariableOne",
            allStandardVariables.contains(standardVariableOne));
        assertTrue("findAll should have standardVariableTwo",
            allStandardVariables.contains(standardVariableTwo));
        // Now test the find by exact name and reference scale
        StandardVariable findByNameAndReferenceScaleStandardVariable = null;
        try {
            findByNameAndReferenceScaleStandardVariable = (StandardVariable) standardVariableAccess
                .findByNameAndReferenceScale("StandardVariable",
                    "StandardVariable one reference scale");
        } catch (RemoteException e) {
            assertTrue(
                "RemoteException caught trying to findByNameAndReferenceScale():"
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findByNameAndReferenceScale():"
                    + e.getMessage(), false);
        }
        assertNull(
            "findByNameAndReferenceScale(StandardVariable,StandardVariable one reference scale) "
                + "should be an empty return",
            findByNameAndReferenceScaleStandardVariable);
        // Try with some that will hit
        findByNameAndReferenceScaleStandardVariable = null;
        try {
            findByNameAndReferenceScaleStandardVariable = (StandardVariable) standardVariableAccess
                .findByNameAndReferenceScale("StandardVariableOne",
                    "StandardVariable one reference scale");
        } catch (RemoteException e) {
            assertTrue(
                "RemoteException caught trying to findByNameAndReferenceScale():"
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findByNameAndReferenceScale():"
                    + e.getMessage(), false);
        }
        assertNotNull("findByName(StandardVariableOne) should found something",
            findByNameAndReferenceScaleStandardVariable);
        assertEquals(
            "findByNameStandardVariables should be StandardVariableOne",
            findByNameAndReferenceScaleStandardVariable, standardVariableOne);
        // Now try to find by exact name. If I search for "StandardVariableO", I
        // should get nothing. If I search for "StandardVariableOne", I should
        // get StandardVariableOne, but not the second
        Collection nameStandardVariables = null;
        try {
            nameStandardVariables = standardVariableAccess
                .findByName("StandardVariableO");
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findByName():"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue("MetadataAccessException caught trying to findByName():"
                + e.getMessage(), false);
        }
        assertNotNull("findByName should return something.",
            nameStandardVariables);
        assertTrue("findByName should have returned and empty collection",
            nameStandardVariables.size() < 1);

        // Now/ fix the name so it will match one, but not two
        try {
            nameStandardVariables = standardVariableAccess
                .findByName("StandardVariableOne");
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findByName():"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue("MetadataAccessException caught trying to findByName():"
                + e.getMessage(), false);
        }
        assertNotNull("findByName should return something.",
            nameStandardVariables);
        assertTrue("findByName should have returned and empty collection",
            nameStandardVariables.size() == 1);
        assertTrue("findByName should contain the first StandardVariable",
            nameStandardVariables.contains(standardVariableOne));

        // Now try to find by like name. If I search for "StandardVariableO", I
        // should get StandardVariableOne, but if I search for
        // "StandardVariable", I should get both
        Collection likeNameStandardVariables = null;
        try {
            likeNameStandardVariables = standardVariableAccess
                .findByLikeName("StandardVariableO");
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findByLikeName():"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findByLikeName():"
                    + e.getMessage(), false);
        }
        assertTrue("findAll should have standardVariableOne",
            likeNameStandardVariables.contains(standardVariableOne));
        assertTrue("findAll should NOT have standardVariableTwo",
            !likeNameStandardVariables.contains(standardVariableTwo));
        likeNameStandardVariables = null;
        try {
            likeNameStandardVariables = standardVariableAccess
                .findByLikeName("StandardVariable");
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findByLikeName():"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findByLikeName():"
                    + e.getMessage(), false);
        }
        assertTrue("findAll should have standardVariableOne",
            likeNameStandardVariables.contains(standardVariableOne));
        assertTrue("findAll should have standardVariableTwo",
            likeNameStandardVariables.contains(standardVariableTwo));

        // Check for empty return
        likeNameStandardVariables = null;
        try {
            likeNameStandardVariables = standardVariableAccess
                .findByLikeName("Hiccup");
        } catch (RemoteException e) {
            assertTrue("RemoteException caught trying to findByLikeName():"
                + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findByLikeName():"
                    + e.getMessage(), false);
        }
        assertNotNull("findByName should not be null return",
            likeNameStandardVariables);
        assertTrue("but the collection should be empty",
            likeNameStandardVariables.size() < 1);

        // Now test the find by exact referenceScale
        Collection findByReferenceScaleStandardVariables = null;
        try {
            findByReferenceScaleStandardVariables = standardVariableAccess
                .findByReferenceScale("reference scale");
        } catch (RemoteException e) {
            assertTrue(
                "RemoteException caught trying to findByReferenceScale():"
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findByReferenceScale():"
                    + e.getMessage(), false);
        }
        assertTrue("findByReferenceScale() should be an empty return",
            findByReferenceScaleStandardVariables.size() == 0);
        findByReferenceScaleStandardVariables = null;
        try {
            findByReferenceScaleStandardVariables = standardVariableAccess
                .findByReferenceScale("StandardVariable one reference scale");
        } catch (RemoteException e) {
            assertTrue(
                "RemoteException caught trying to findByReferenceScale():"
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findByReferenceScale():"
                    + e.getMessage(), false);
        }
        assertTrue(
            "findByReferenceScale(StandardVariable one reference scale) should return a collection of size one.",
            findByReferenceScaleStandardVariables.size() == 1);
        assertTrue(
            "findByReferenceScaleStandardVariables should contain StandardVariableOne",
            findByReferenceScaleStandardVariables.contains(standardVariableOne));

        // Now try to find by like referenceScale. If I search for "one
        // reference scale", I should get StandardVariableOne, but if I search
        // for "reference scale", I should get both
        Collection likeReferenceScaleStandardVariables = null;
        try {
            likeReferenceScaleStandardVariables = standardVariableAccess
                .findByLikeReferenceScale("one reference scale");
        } catch (RemoteException e) {
            assertTrue(
                "RemoteException caught trying to findByLikeReferenceScale():"
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findByLikeReferenceScale():"
                    + e.getMessage(), false);
        }
        assertTrue("findByLikeReferenceScale should have standardVariableOne",
            likeReferenceScaleStandardVariables.contains(standardVariableOne));
        assertTrue(
            "findByLikeReferenceScale should NOT have standardVariableTwo",
            !likeReferenceScaleStandardVariables.contains(standardVariableTwo));
        likeReferenceScaleStandardVariables = null;
        try {
            likeReferenceScaleStandardVariables = standardVariableAccess
                .findByLikeReferenceScale("reference scale");
        } catch (RemoteException e) {
            assertTrue(
                "RemoteException caught trying to findByLikeReferenceScale():"
                    + e.getMessage(), false);
        } catch (MetadataAccessException e) {
            assertTrue(
                "MetadataAccessException caught trying to findByLikeReferenceScale():"
                    + e.getMessage(), false);
        }
        assertTrue("findByLikeReferenceScale should have standardVariableOne",
            likeReferenceScaleStandardVariables.contains(standardVariableOne));
        assertTrue("findByLikeReferenceScale should have standardVariableTwo",
            likeReferenceScaleStandardVariables.contains(standardVariableTwo));

        // Now check the find all names and find all ID's
        Collection allIds = null;
        Collection allNames = null;
        Collection allReferenceScales = null;
        try {
            allIds = standardVariableAccess.findAllIDs();
            allNames = standardVariableAccess.findAllNames();
            allReferenceScales = standardVariableAccess
                .findAllReferenceScales();
        } catch (RemoteException e2) {
            assertTrue("RemoteException caught trying to "
                + "findAllStandardVariableIDs/findAllStandardVariableNames():"
                + e2.getMessage(), false);
        } catch (MetadataAccessException e2) {
            assertTrue("MetadataAccessException caught trying to "
                + "findAllStandardVariableIDs/findAllStandardVariableNames():"
                + e2.getMessage(), false);
        }

        // OK allIDs and names should have 2 more members
        assertNotNull("allIds should not be null", allIds);
        assertNotNull("allNames should not be null", allNames);
        assertNotNull("allReferenceScale should not be null",
            allReferenceScales);
        assertTrue("allIds should have two more entries.",
            allIds.size() == (countBeforeInserts + 2));
        assertTrue("allNames should have two more entries",
            allNames.size() == (countNamesBeforeInsert + 2));
        assertTrue("allReferenceScales should have two more entries",
            allReferenceScales.size() == (countReferenceScalesBeforeInsert + 2));
        // Now make sure the ids and/ names are all in the returned collection
        assertTrue("StandardVariable ID one should be there: ", allIds
            .contains(standardVariableOneId));
        assertTrue("StandardVariable ID two should be there: ", allIds
            .contains(standardVariableTwoId));
        assertTrue("StandardVariable name one should be there: ", allNames
            .contains("StandardVariableOne"));
        assertTrue("StandardVariable name two should be there: ", allNames
            .contains("StandardVariableTwo"));
        assertTrue("StandardVariable reference scale one should be there: ",
            allReferenceScales.contains("StandardVariable one reference scale"));
        assertTrue("StandardVariable referenece scale two should be there: ",
            allReferenceScales.contains("StandardVariable two reference scale"));

        // Now clean up
        try {
            standardVariableAccess.delete(standardVariableOne);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
        try {
            standardVariableAccess.delete(standardVariableTwo);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
    }

    /**
     * This method tests the persistence services and how they handle the
     * relationship between StandardVariable and StandardUnit
     */
    public void testStandardUnitRelationship() {
        // OK, first setup the objects
        standardVariableOne.setId(null);
        standardVariableTwo.setId(null);
        standardUnitOne.setId(null);
        standardUnitTwo.setId(null);

        /*
         * // OK, let's first insert the two standardVariables Long
         * standardVariableOneId = null; Long standardVariableTwoId = null; try {
         * standardVariableOneId = standardVariableAccess
         * .insert(standardVariableOne); standardVariableTwoId =
         * standardVariableAccess .insert(standardVariableTwo); } catch
         * (RemoteException e) { assertTrue("RemoteException caught trying to " +
         * "insert standardVariables:" + e.getMessage(), false); } catch
         * (MetadataAccessException e) { assertTrue("MetadataAccessException
         * caught trying to " + "insert standardVariables" + e.getMessage(),
         * false); } // OK now get them back and they should not have any
         * StandardUnits StandardVariable persistentSVOne = null;
         * StandardVariable persistentSVTwo = null; try { persistentSVOne =
         * (StandardVariable) standardVariableAccess
         * .findById(standardVariableOneId); persistentSVTwo =
         * (StandardVariable) standardVariableAccess
         * .findById(standardVariableTwoId); } catch (RemoteException e) {
         * assertTrue("RemoteException caught trying to " + "read back the
         * standardvariables:" + e.getMessage(), false); } catch
         * (MetadataAccessException e) { assertTrue("MetadataAccessException
         * caught trying to " + "read back the standardvariables" +
         * e.getMessage(), false); } Collection susOne = null; Collection susTwo =
         * null; try { susOne = standardVariableAccess
         * .findAssociatedStandardUnits(persistentSVOne); susTwo =
         * standardVariableAccess .findAssociatedStandardUnits(persistentSVTwo); }
         * catch (RemoteException e) { assertTrue("RemoteException caught trying
         * to " + "find the associated standardUnits:" + e.getMessage(), false); }
         * catch (MetadataAccessException e) {
         * assertTrue("MetadataAccessException caught trying to " + "find the
         * associated standardUnits:" + e.getMessage(), false); } // OK, they
         * should be empty, but not null assertNotNull("The associated
         * standardUnits(One) should not be null", susOne); assertNotNull("The
         * associated standardUnits(Two) should not be null", susTwo);
         * assertTrue( "The associated StandardUnits(One) collection should be
         * empty", susOne.size() == 0); assertTrue( "The associated
         * StandardUntis(Two) collection should be empty", susTwo.size() == 0); //
         * Now before adding standardUnits, I need to get the object with its //
         * collection filled out try { persistentSVOne = (StandardVariable)
         * standardVariableAccess .getMetadataObjectGraph(persistentSVOne); }
         * catch (RemoteException e1) { assertTrue( "RemoteException caught
         * trying to " + "get the object graph for persistentSVOne:" +
         * e1.getMessage(), false); } catch (MetadataAccessException e1) {
         * assertTrue( "MetadataAccessException caught trying to " + "get the
         * object graph for persistentSVOne:" + e1.getMessage(), false); } //
         * OK, now add both standardUnits to the first standardVariable
         * persistentSVOne.addStandardUnit(standardUnitOne);
         * persistentSVOne.addStandardUnit(standardUnitTwo); try {
         * standardVariableAccess.update(persistentSVOne); } catch
         * (RemoteException e) { assertTrue("RemoteException caught trying to " +
         * "update persistentSVOne:" + e.getMessage(), false); } catch
         * (MetadataAccessException e) { assertTrue("MetadataAccessException
         * caught trying to " + "update persistentSVOne:" + e.getMessage(),
         * false); } // Now I should be able to query back and find susOne =
         * null; susTwo = null; try { susOne = standardVariableAccess
         * .findAssociatedStandardUnits(persistentSVOne); susTwo =
         * standardVariableAccess .findAssociatedStandardUnits(persistentSVTwo); }
         * catch (RemoteException e) { assertTrue("RemoteException caught trying
         * to " + "find the associated standardUnits:" + e.getMessage(), false); }
         * catch (MetadataAccessException e) {
         * assertTrue("MetadataAccessException caught trying to " + "find the
         * associated standardUnits:" + e.getMessage(), false); } // OK, they
         * should be empty, but not null assertNotNull("The associated
         * standardUnits(One) should not be null", susOne); assertNotNull("The
         * associated standardUnits(Two) should not be null", susTwo);
         * assertTrue( "The associated StandardUnits(One) collection should have
         * two StandardUnits", susOne.size() == 2); assertTrue( "The associated
         * StandardUntis(Two) collection should be empty", susTwo.size() == 0);
         */
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
        // Delete any standardVariable objects as we don't want any leftover's
        // if a test fails
        try {
            standardVariableAccess.delete(standardVariableOne);
            standardUnitAccess.delete(standardUnitOne);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
        try {
            standardVariableAccess.delete(standardVariableTwo);
            standardUnitAccess.delete(standardUnitTwo);
        } catch (RemoteException e) {} catch (MetadataAccessException e) {}
    }

    // The connection to the service classes
    StandardVariableAccessHome standardVariableAccessHome = null;
    StandardVariableAccess standardVariableAccess = null;

    // The test StandardVariables
    String standardVariableOneStringRep = "StandardVariable,"
        + "name=StandardVariableOne,"
        + "description=StandardVariable one description,"
        + "referenceScale=StandardVariable one reference scale";
    String standardVariableTwoStringRep = "StandardVariable,"
        + "name=StandardVariableTwo,"
        + "description=StandardVariable two description,"
        + "referenceScale=StandardVariable two reference scale";

    // The delimiter
    String delimiter = ",";

    // The objects
    StandardVariable standardVariableOne = null;
    StandardVariable standardVariableTwo = null;

    // Stuff for the StandardUnits
    StandardUnitAccessHome standardUnitAccessHome = null;
    StandardUnitAccess standardUnitAccess = null;

    StandardUnit standardUnitOne = null;
    StandardUnit standardUnitTwo = null;

    String standardUnitOneStringRep = "StandardUnit," + "name=StandardUnitOne,"
        + "description=StandardUnit one description,"
        + "longName=Standard Unit one long name," + "symbol=SUONE";
    String standardUnitTwoStringRep = "StandardUnit," + "name=StandardUnitTwo,"
        + "description=StandardUnit two description,"
        + "longName=Standard Unit two long name," + "symbol=SUTWO";

    /**
     * A log4J logger
     */
    static Logger logger = Logger.getLogger(TestStandardVariableAccess.class);
}