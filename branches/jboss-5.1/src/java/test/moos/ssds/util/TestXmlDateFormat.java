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
	 * This test checks the equals method which depends on the outputs of the
	 * processRun
	 */
	public void testOne() {
		// OK, first is the target date that we are going to try to
		// represent and parse in different ways
		Calendar goldCalendar = Calendar.getInstance();
		// Set the time zone
		goldCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		// Set the time/date
		goldCalendar.set(Calendar.YEAR, 1941);
		goldCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
		goldCalendar.set(Calendar.DAY_OF_MONTH, 7);
		goldCalendar.set(Calendar.HOUR_OF_DAY, 17);
		goldCalendar.set(Calendar.MINUTE, 53);
		goldCalendar.set(Calendar.SECOND, 0);
		goldCalendar.set(Calendar.MILLISECOND, 0);

		// Create some XML date strings that it should be able to parse
		String xmlDate1 = "1941/12/07 17:53:00Z";
		String xmlDate2 = "1941-12-07 17:53:00Z";
		String xmlDate3 = "1941-12-07 07:53:00Z-10";
		String xmlDate4 = "1941/12/07 07:53:00Z-09-60";
		String xmlDate5 = "1941-12-07 07:53:00-10";
		String xmlDate6 = "1941-12-07 07:53:00-09:60";
		String xmlDate7 = "1941/12/07T17:53:00Z";
		String xmlDate8 = "1941-12-07T17:53:00Z";
		String xmlDate9 = "1941-12-07T07:53:00Z-10";
		String xmlDate10 = "1941/12/07T07:53:00Z-09-60";
		String xmlDate11 = "1941-12-07T07:53:00-10";
		String xmlDate12 = "1941-12-07T07:53:00-09:60";
		String xmlDate13 = "1941-12-07 19:53:00Z+02";
		String xmlDate14 = "1941/12/07 19:53:00Z+01+60";

		Calendar xmlCal1 = Calendar.getInstance();
		Date xmlDate1Date = xmlDateFormat.parse(xmlDate1);
		assertNotNull("parsedXMLDate1 should not be null", xmlDate1Date);
		xmlCal1.setTimeInMillis(xmlDate1Date.getTime());
		xmlCal1.set(Calendar.MILLISECOND, 0);
		Calendar xmlCal2 = Calendar.getInstance();
		Date xmlDate2Date = xmlDateFormat.parse(xmlDate2);
		assertNotNull("parsedXMLDate2 should not be null", xmlDate2Date);
		xmlCal2.setTimeInMillis(xmlDate2Date.getTime());
		xmlCal2.set(Calendar.MILLISECOND, 0);
		Calendar xmlCal3 = Calendar.getInstance();
		Date xmlDate3Date = xmlDateFormat.parse(xmlDate3);
		assertNotNull("parsedXMLDate3 should not be null", xmlDate3Date);
		xmlCal3.setTimeInMillis(xmlDate3Date.getTime());
		xmlCal3.set(Calendar.MILLISECOND, 0);
		Calendar xmlCal4 = Calendar.getInstance();
		Date xmlDate4Date = xmlDateFormat.parse(xmlDate4);
		assertNotNull("parsedXMLDate4 should not be null", xmlDate4Date);
		xmlCal4.setTimeInMillis(xmlDate4Date.getTime());
		xmlCal4.set(Calendar.MILLISECOND, 0);
		Calendar xmlCal5 = Calendar.getInstance();
		Date xmlDate5Date = xmlDateFormat.parse(xmlDate5);
		assertNotNull("parsedXMLDate5 should not be null", xmlDate5Date);
		xmlCal5.setTimeInMillis(xmlDate5Date.getTime());
		xmlCal5.set(Calendar.MILLISECOND, 0);
		Calendar xmlCal6 = Calendar.getInstance();
		Date xmlDate6Date = xmlDateFormat.parse(xmlDate6);
		assertNotNull("parsedXMLDate6 should not be null", xmlDate6Date);
		xmlCal6.setTimeInMillis(xmlDate6Date.getTime());
		xmlCal6.set(Calendar.MILLISECOND, 0);
		Calendar xmlCal7 = Calendar.getInstance();
		Date xmlDate7Date = xmlDateFormat.parse(xmlDate7);
		assertNotNull("parsedXMLDate7 should not be null", xmlDate7Date);
		xmlCal7.setTimeInMillis(xmlDate7Date.getTime());
		xmlCal7.set(Calendar.MILLISECOND, 0);
		Calendar xmlCal8 = Calendar.getInstance();
		Date xmlDate8Date = xmlDateFormat.parse(xmlDate8);
		assertNotNull("parsedXMLDate8 should not be null", xmlDate8Date);
		xmlCal8.setTimeInMillis(xmlDate8Date.getTime());
		xmlCal8.set(Calendar.MILLISECOND, 0);
		Calendar xmlCal9 = Calendar.getInstance();
		Date xmlDate9Date = xmlDateFormat.parse(xmlDate9);
		assertNotNull("parsedXMLDate9 should not be null", xmlDate9Date);
		xmlCal9.setTimeInMillis(xmlDate9Date.getTime());
		xmlCal9.set(Calendar.MILLISECOND, 0);
		Calendar xmlCal10 = Calendar.getInstance();
		Date xmlDate10Date = xmlDateFormat.parse(xmlDate10);
		assertNotNull("parsedXMLDate10 should not be null", xmlDate10Date);
		xmlCal10.setTimeInMillis(xmlDate10Date.getTime());
		xmlCal10.set(Calendar.MILLISECOND, 0);
		Calendar xmlCal11 = Calendar.getInstance();
		Date xmlDate11Date = xmlDateFormat.parse(xmlDate11);
		assertNotNull("parsedXMLDate11 should not be null", xmlDate11Date);
		xmlCal11.setTimeInMillis(xmlDate11Date.getTime());
		xmlCal11.set(Calendar.MILLISECOND, 0);
		Calendar xmlCal12 = Calendar.getInstance();
		Date xmlDate12Date = xmlDateFormat.parse(xmlDate12);
		assertNotNull("parsedXMLDate12 should not be null", xmlDate12Date);
		xmlCal12.setTimeInMillis(xmlDate12Date.getTime());
		xmlCal12.set(Calendar.MILLISECOND, 0);
		Calendar xmlCal13 = Calendar.getInstance();
		Date xmlDate13Date = xmlDateFormat.parse(xmlDate13);
		assertNotNull("parsedXMLDate13 should not be null", xmlDate13Date);
		xmlCal13.setTimeInMillis(xmlDate13Date.getTime());
		xmlCal13.set(Calendar.MILLISECOND, 0);
		Calendar xmlCal14 = Calendar.getInstance();
		Date xmlDate14Date = xmlDateFormat.parse(xmlDate14);
		assertNotNull("parsedXMLDate14 should not be null", xmlDate14Date);
		xmlCal14.setTimeInMillis(xmlDate14Date.getTime());
		xmlCal14.set(Calendar.MILLISECOND, 0);
		logger.debug("xmlCal1 => " + xmlCal1.getTime());
		logger.debug("xmlCal2 => " + xmlCal2.getTime());
		logger.debug("xmlCal3 => " + xmlCal3.getTime());
		logger.debug("xmlCal4 => " + xmlCal4.getTime());
		logger.debug("xmlCal5 => " + xmlCal5.getTime());
		logger.debug("xmlCal6 => " + xmlCal6.getTime());
		logger.debug("xmlCal7 => " + xmlCal7.getTime());
		logger.debug("xmlCal8 => " + xmlCal8.getTime());
		logger.debug("xmlCal9 => " + xmlCal9.getTime());
		logger.debug("xmlCal10 => " + xmlCal10.getTime());
		logger.debug("xmlCal11 => " + xmlCal11.getTime());
		logger.debug("xmlCal12 => " + xmlCal12.getTime());
		logger.debug("xmlCal13 => " + xmlCal13.getTime());
		logger.debug("xmlCal14 => " + xmlCal14.getTime());

		assertEquals("Date 1 should be equal:", goldCalendar.getTimeInMillis(),
				xmlCal1.getTimeInMillis());
		assertEquals("Date 2 should be equal:", goldCalendar.getTimeInMillis(),
				xmlCal2.getTimeInMillis());
		assertEquals("Date 3 should be equal:", goldCalendar.getTimeInMillis(),
				xmlCal3.getTimeInMillis());
		assertEquals("Date 4 should be equal:", goldCalendar.getTimeInMillis(),
				xmlCal4.getTimeInMillis());
		assertEquals("Date 5 should be equal:", goldCalendar.getTimeInMillis(),
				xmlCal5.getTimeInMillis());
		assertEquals("Date 6 should be equal:", goldCalendar.getTimeInMillis(),
				xmlCal6.getTimeInMillis());
		assertEquals("Date 7 should be equal:", goldCalendar.getTimeInMillis(),
				xmlCal7.getTimeInMillis());
		assertEquals("Date 8 should be equal:", goldCalendar.getTimeInMillis(),
				xmlCal8.getTimeInMillis());
		assertEquals("Date 9 should be equal:", goldCalendar.getTimeInMillis(),
				xmlCal9.getTimeInMillis());
		assertEquals("Date 10 should be equal:",
				goldCalendar.getTimeInMillis(), xmlCal10.getTimeInMillis());
		assertEquals("Date 11 should be equal:",
				goldCalendar.getTimeInMillis(), xmlCal11.getTimeInMillis());
		assertEquals("Date 12 should be equal:",
				goldCalendar.getTimeInMillis(), xmlCal12.getTimeInMillis());
		assertEquals("Date 13 should be equal:",
				goldCalendar.getTimeInMillis(), xmlCal13.getTimeInMillis());
		assertEquals("Date 14 should be equal:",
				goldCalendar.getTimeInMillis(), xmlCal14.getTimeInMillis());
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
		assertEquals("The year should be correct", "2010",
				formattedDate.substring(0, 4));
		assertEquals("The month should match", "08",
				formattedDate.substring(5, 7));
		assertEquals("The month should match", "04",
				formattedDate.substring(8, 10));
		assertEquals("The hour should match", "22",
				formattedDate.substring(11, 13));
		assertEquals("The minute should match", "18",
				formattedDate.substring(14, 16));
		assertEquals("The second should match", "20",
				formattedDate.substring(17, 19));
		assertEquals("The zone should match", "Z", formattedDate.substring(19));
		// Grab the compact formatted date
		String compactFormattedDate = xmlDateFormat.formatCompact(date);
		// Now make sure it is what we expect
		assertEquals("The year should be correct", "2010",
				compactFormattedDate.substring(0, 4));
		assertEquals("The month should match", "08",
				compactFormattedDate.substring(4, 6));
		assertEquals("The month should match", "04",
				compactFormattedDate.substring(6, 8));
		assertEquals("The hour should match", "22",
				compactFormattedDate.substring(9, 11));
		assertEquals("The minute should match", "18",
				compactFormattedDate.substring(11, 13));
		assertEquals("The second should match", "20",
				compactFormattedDate.substring(13, 15));
	}

	/**
	 * This method tests whether or not the XmlDateFormat is parsing things
	 * correctly
	 */
	public void testParse() {
		// Create the string for the date
		String dateString = "2010-08-04T12:34:56Z";

		// Now parse it
		Date parsedDate = xmlDateFormat.parse(dateString);

		assertNotNull("The parser should have returned something", parsedDate);

		// A Calendar to help with testing
		Calendar parsedCalendar = Calendar.getInstance();
		parsedCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		parsedCalendar.setTime(parsedDate);

		// Check the fields
		assertEquals("The year should match", 2010,
				parsedCalendar.get(Calendar.YEAR));
		assertEquals("The month should match", 8,
				parsedCalendar.get(Calendar.MONTH) + 1);
		assertEquals("The day should match", 4,
				parsedCalendar.get(Calendar.DAY_OF_MONTH));
		assertEquals("The hour should match", 12,
				parsedCalendar.get(Calendar.HOUR_OF_DAY));
		assertEquals("The minute should match", 34,
				parsedCalendar.get(Calendar.MINUTE));
		assertEquals("The second should match", 56,
				parsedCalendar.get(Calendar.SECOND));

		// Try one with short digits and no timezone
		dateString = "2010-8-4T9:3:5";

		// Now parse it
		parsedDate = xmlDateFormat.parse(dateString);

		assertNotNull("The parser should have returned something", parsedDate);

		// A Calendar to help with testing
		parsedCalendar = Calendar.getInstance();
		parsedCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		parsedCalendar.setTime(parsedDate);

		// Check the fields
		assertEquals("The year should match", 2010,
				parsedCalendar.get(Calendar.YEAR));
		assertEquals("The month should match", 8,
				parsedCalendar.get(Calendar.MONTH) + 1);
		assertEquals("The day should match", 4,
				parsedCalendar.get(Calendar.DAY_OF_MONTH));
		assertEquals("The hour should match", 9,
				parsedCalendar.get(Calendar.HOUR_OF_DAY));
		assertEquals("The minute should match", 3,
				parsedCalendar.get(Calendar.MINUTE));
		assertEquals("The second should match", 5,
				parsedCalendar.get(Calendar.SECOND));

		// And one more normal, but with no timezone
		dateString = "2010-08-04T09:03:25";

		// Now parse it
		parsedDate = xmlDateFormat.parse(dateString);

		assertNotNull("The parser should have returned something", parsedDate);

		// A Calendar to help with testing
		parsedCalendar = Calendar.getInstance();
		parsedCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		parsedCalendar.setTime(parsedDate);

		// Check the fields
		assertEquals("The year should match", 2010,
				parsedCalendar.get(Calendar.YEAR));
		assertEquals("The month should match", 8,
				parsedCalendar.get(Calendar.MONTH) + 1);
		assertEquals("The day should match", 4,
				parsedCalendar.get(Calendar.DAY_OF_MONTH));
		assertEquals("The hour should match", 9,
				parsedCalendar.get(Calendar.HOUR_OF_DAY));
		assertEquals("The minute should match", 3,
				parsedCalendar.get(Calendar.MINUTE));
		assertEquals("The second should match", 25,
				parsedCalendar.get(Calendar.SECOND));
	}
}
