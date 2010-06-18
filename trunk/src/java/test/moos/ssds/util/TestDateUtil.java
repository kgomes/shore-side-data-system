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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * This is the test class to test the DateUtil class
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.1 $
 */
public class TestDateUtil extends TestCase {

	/**
	 * @param arg0
	 */
	public TestDateUtil(String arg0) {
		super(arg0);
	}

	protected void setUp() {
	}

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

	public void testRoundDateDownToSeconds() {

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

	/**
	 * The logger for dumping information to
	 */
	static Logger logger = Logger.getLogger(TestDateUtil.class);
}