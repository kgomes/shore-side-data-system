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

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.Keyword;

import org.apache.log4j.Logger;

/**
 * This class tests the KeywordAccess service EJB to make sure all is well.
 * There has to be an SSDS server running somewhere for this to hit against and
 * a jndi.properties in the classpath so the tests can get to the server
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.4 $
 */
public class TestKeywordAccess extends TestAccessCase {

	/**
	 * A log4J logger
	 */
	static Logger logger = Logger.getLogger(TestKeywordAccess.class);

	/**
	 * A constructor
	 * 
	 * @param name
	 */
	public TestKeywordAccess(String name) {
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
	 * Run suite of tests on keyword one
	 */
	public void testOne() {
		logger.debug("Keyword one is " + KEYWORD_ONE.toStringRepresentation("|"));
		this.keywordTest(KEYWORD_ONE);
		logger.debug("Done with test one");
	}

	/**
	 * Run suite of tests on keyword two
	 */
	public void testTwo() {
		logger.debug("Keyword two is " + KEYWORD_TWO.toStringRepresentation("|"));
		keywordTest(KEYWORD_TWO);
		logger.debug("Done with test two");
	}

	/**
	 * This method checks all the "findBy*" methods
	 */
	@SuppressWarnings("unchecked")
	public void testFindBys() {
		logger.debug("testFindBys starting");
		// We will insert both keywords and then see if we can find them using
		// the find by methods
		Long keywordOneId = null;
		Long keywordTwoId = null;
		try {
			keywordOneId = KEYWORD_ACCESS.insert(KEYWORD_ONE);
			keywordTwoId = KEYWORD_ACCESS.insert(KEYWORD_TWO);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}

		// Make sure the returned ids are real
		assertNotNull(keywordOneId);
		assertNotNull(keywordTwoId);
		logger.debug("KeywordOneId = " + keywordOneId);
		logger.debug("KeywordTwoId = " + keywordTwoId);

		// OK both should be inserted. Excercise some of the findBys
		Keyword foundOne = null;
		try {
			foundOne = (Keyword) KEYWORD_ACCESS.findById(keywordOneId, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The findById(Long) should have returned something",
				foundOne);
		assertEquals(
				"The foundOne (by Long) should be equal to the local KEYWORD_ONE",
				foundOne, KEYWORD_ONE);

		// Clear and try with different find by
		foundOne = null;
		try {
			foundOne = (Keyword) KEYWORD_ACCESS.findById(keywordOneId.longValue(),
					false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The findById(long) should have returned something",
				foundOne);
		assertEquals(
				"The foundOne (by long) should be equal to the local KEYWORD_ONE",
				foundOne, KEYWORD_ONE);

		// Clear and try again
		foundOne = null;
		try {
			foundOne = (Keyword) KEYWORD_ACCESS.findById(keywordOneId.toString(),
					false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("The findById(String) should have returned something",
				foundOne);
		assertEquals(
				"The foundOne (by String) should be equal to the local KEYWORD_ONE",
				foundOne, KEYWORD_ONE);

		// OK, find by local keyword and check against ID
		Long foundOneId = null;
		try {
			foundOneId = KEYWORD_ACCESS.findId(KEYWORD_ONE);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("foundOneId should not be null", foundOneId);
		assertEquals("ID from insert and returned ID should be the same.",
				foundOneId.longValue(), keywordOneId.longValue());

		// Now check find all
		Collection<Keyword> allKeywords = null;
		try {
			allKeywords = KEYWORD_ACCESS.findAll(null, null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull("Collection returned by findAll should not be null",
				allKeywords);
		assertTrue("Collection should contain local keyword one", allKeywords
				.contains(KEYWORD_ONE));
		assertTrue("Collection should contain local keyword two", allKeywords
				.contains(KEYWORD_TWO));
		logger.debug("allKeywords = " + allKeywords);

		// Now test the findEquivalent persistent object
		Keyword persistentOne = null;
		try {
			Long equivalentId = KEYWORD_ACCESS.findId(KEYWORD_ONE);
			persistentOne = (Keyword) KEYWORD_ACCESS.findById(equivalentId, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataException was thrown: " + e.getMessage(), false);
		}
		assertNotNull(
				"persistent object that matches KEYWORD_ONE should not be null",
				persistentOne);
		assertEquals(
				"Persistent and local objects for KEYWORD_ONE should be equal",
				persistentOne, KEYWORD_ONE);
		assertEquals("The inserted ID and the persistent ID should be equal",
				keywordOneId.longValue(), persistentOne.getId().longValue());

	}

	/**
	 * This is the suite of tests to run on a keyword
	 * 
	 * @param keyword
	 */
	private void keywordTest(Keyword keyword) {

		// The ID of the keyword
		Long keywordId = null;
		keywordId = testInsert(keyword, KEYWORD_ACCESS);

		// Now query back by ID and make sure all attributes are equal
		Keyword persistedKeyword = null;

		try {
			persistedKeyword = (Keyword) KEYWORD_ACCESS.findById(keywordId, false);
		} catch (RemoteException e1) {
			logger.error("RemoteException caught during findById: "
					+ e1.getMessage());
		} catch (MetadataAccessException e1) {
			logger.error("MetadataAccessException caught during findById: "
					+ e1.getMessage());
		}

		// Now check that they are equal
		assertEquals("The two keywords should be considered equal", keyword,
				persistedKeyword);

		// Check all the getter methods are equal
		testEqualityOfAllGetters(keyword, persistedKeyword);

		// Create a map with the values to update
		HashMap<String, Object[]> variablesToUpdate = new HashMap<String, Object[]>();

		// Change the description
		Object[] variable1 = new Object[1];
		variable1[0] = new String("Updated Description");
		variablesToUpdate.put("Description", variable1);

		testUpdate(persistedKeyword, variablesToUpdate, KEYWORD_ACCESS);

		testDelete(persistedKeyword, KEYWORD_ACCESS);
	}

}