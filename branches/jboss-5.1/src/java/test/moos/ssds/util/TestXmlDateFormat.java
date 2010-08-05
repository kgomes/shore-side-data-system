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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import moos.ssds.util.XmlDateFormat;

import org.apache.log4j.Logger;

import junit.framework.TestCase;

/**
 * This class tests the class XmlDateFormat
 * 
 * @author kgomes
 * 
 */
public class TestXmlDateFormat extends TestCase {

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(TestXmlDateFormat.class);

	/**
	 * The XmlDateFormat to use during the test
	 */
	private XmlDateFormat xmlDateFormat = new XmlDateFormat();

	public TestXmlDateFormat(String name) {
		super(name);
	}

	/**
	 * This method tests whether or not the formatter correctly formats a date
	 * to the ISO 8601 format
	 */
	public void testFormats() {
		Date date = new Date(1280960300288L);
		// Grab the formatted date
		String formattedDate = xmlDateFormat.format(date);
		// Now make sure it is what we expect
		assertEquals("The year should be correct", "2010", formattedDate
				.substring(0, 4));
		assertEquals("The month should match", "08", formattedDate.substring(5,
				7));
		assertEquals("The month should match", "04", formattedDate.substring(8,
				10));
		assertEquals("The hour should match", "22", formattedDate.substring(11,
				13));
		assertEquals("The minute should match", "18", formattedDate.substring(
				14, 16));
		assertEquals("The second should match", "20", formattedDate.substring(
				17, 19));
		assertEquals("The zone should match", "Z", formattedDate.substring(19));
		// Grab the compact formatted date
		String compactFormattedDate = xmlDateFormat.formatCompact(date);
		// Now make sure it is what we expect
		assertEquals("The year should be correct", "2010", compactFormattedDate
				.substring(0, 4));
		assertEquals("The month should match", "08", compactFormattedDate
				.substring(4, 6));
		assertEquals("The month should match", "04", compactFormattedDate
				.substring(6, 8));
		assertEquals("The hour should match", "22", compactFormattedDate
				.substring(9, 11));
		assertEquals("The minute should match", "18", compactFormattedDate
				.substring(11, 13));
		assertEquals("The second should match", "20", compactFormattedDate
				.substring(13, 15));
	}

	/**
	 * This method tests whether or not the XmlDateFormat is parsing things
	 * correctly
	 */
	public void testParse() {
		// Create the string for the date
		String dateString = "2010-08-0T12:34:56Z";

		// Now parse it
		Date parsedDate = xmlDateFormat.parse(dateString);

		assertNotNull("The parser should have returned something", parsedDate);

		// A Calendar to help with testing
		Calendar parsedCalendar = Calendar.getInstance();
		parsedCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		parsedCalendar.setTime(parsedDate);

		// Check the fields
		assertEquals("The year should match", 2010, parsedCalendar
				.get(Calendar.YEAR));
		assertEquals("The month should match", 8, parsedCalendar
				.get(Calendar.MONTH) + 1);
		assertEquals("The day should match", 4, parsedCalendar
				.get(Calendar.DAY_OF_MONTH));
		assertEquals("The hour should match", 12, parsedCalendar
				.get(Calendar.HOUR_OF_DAY));
		assertEquals("The minute should match", 34, parsedCalendar
				.get(Calendar.MINUTE));
		assertEquals("The second should match", 56, parsedCalendar
				.get(Calendar.SECOND));
	}
}
