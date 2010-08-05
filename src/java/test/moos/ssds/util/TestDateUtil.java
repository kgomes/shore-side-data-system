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
package test.moos.ssds.util;

import java.util.Date;

import junit.framework.TestCase;
import moos.ssds.util.DateUtils;

import org.apache.log4j.Logger;

/**
 * This is the test class to test the DateUtil class
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.1 $
 */
public class TestDateUtil extends TestCase {

	/**
	 * The logger for dumping information to
	 */
	static Logger logger = Logger.getLogger(TestDateUtil.class);

	/**
	 * @param arg0
	 */
	public TestDateUtil(String arg0) {
		super(arg0);
	}

	protected void setUp() {
	}

	/**
	 * This test checks to make sure the function that extracts epoch seconds is
	 * working OK
	 */
	public void testGetEpochTimestampSeconds() {
		Date date = new Date(123456789L);
		assertEquals("Extraction of epoch seconds should be OK", 123456,
				DateUtils.getEpochTimestampSeconds(date));
	}

	/**
	 * This test to makes sure the seconds extraction from millis is working OK
	 */
	public void testGetEpochTimestampSecondsFromEpochMillis() {
		Date date = new Date(123456789L);
		assertEquals("Extraction of epoch seconds from millis should be OK",
				123456, DateUtils.getEpochTimestampSecondsFromEpochMillis(date
						.getTime()));
	}

	/**
	 * This makes sure the extraction of nanoseconds from epoch millis is
	 * correct.
	 * 
	 * TODO kgomes - This is actually not correct (see SSDS-77 bug), but it is a
	 * known issue. When SSDS-77 if fixed, this will need to be fixed to test
	 * correctly
	 */
	public void testGetNanosecondsFromEpochMillis() {
		Date date = new Date(123456789L);
		assertEquals("Extraction of nanoseconds from millis should be OK",
				789000, DateUtils.getNanosecondsFromEpochMillis(date.getTime()));
	}

	/**
	 * This tests to make sure the function to take in epoch seconds and
	 * nanoseconds creates the epoch millis correctly
	 * 
	 * TODO kgomes - This is actually not correct (see SSDS-77 bug), but it is a
	 * known issue. When SSDS-77 if fixed, this will need to be fixed to test
	 * correctly
	 */
	public void testConstructEpochMillisFromEpochSecondsAndNanoseconds() {
		assertEquals("The epoch millis should be constructed correctly",
				123456789, DateUtils
						.constructEpochMillisFromEpochSecondsAndNanoseconds(
								123456, 789000));
	}

	/**
	 * This tests checks to see if two different dates that are within 1 second
	 * of each other will be considered equal by the equalsWithinSeconds method
	 */
	public void testEqualsWithinSeconds() {
		Date date1 = new Date(10000L);
		Date date2 = new Date(10050L);

		// First check to see if they are exactly equal
		assertTrue("The two dates should not be equal by their native equals",
				!date1.equals(date2));
		assertTrue(
				"The two dates should not be equal by seconds if seconds = 0",
				!DateUtils.equalsWithinSeconds(date1, date2, 0));
		assertTrue("They should be equal if window is 1 second", DateUtils
				.equalsWithinSeconds(date1, date2, 1));
	}

	/**
	 * This method checks to see if the rounding down to seconds is workign
	 * correctly
	 */
	public void testRoundDateDownToSeconds() {

		// Create some dates
		Date date1 = new Date(10000L);
		Date date2 = new Date(10010L);
		Date date3 = new Date(10999L);
		Date date4 = new Date(11000L);

		// Now round each one down
		Date date1Round = DateUtils.roundDateDownToSeconds(date1);
		Date date2Round = DateUtils.roundDateDownToSeconds(date2);
		Date date3Round = DateUtils.roundDateDownToSeconds(date3);
		Date date4Round = DateUtils.roundDateDownToSeconds(date4);

		// Now test the results
		assertTrue("date 1 and 2 before round should not be equal in millis",
				date1.getTime() != date2.getTime());
		assertTrue("date 1 and 23before round should not be equal in millis",
				date1.getTime() != date3.getTime());
		assertEquals("rounded date 1 and two should be equal in millis",
				date1Round.getTime(), date2Round.getTime());
		assertEquals("rounded date 1 and three should be equal in millis",
				date1Round.getTime(), date3Round.getTime());
		assertTrue("date 1 and four should not be equal before round", date1
				.getTime() != date4.getTime());
		assertTrue("date 1 and four should not be equal after rounding even",
				date1Round.getTime() != date4Round.getTime());
	}

}