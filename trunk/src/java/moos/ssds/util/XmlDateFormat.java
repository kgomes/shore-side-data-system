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
package moos.ssds.util;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * <p>
 * Class for formatting and parsing dates in the format specifed by the W3C XML
 * Schema 2 standard
 * </p>
 * 
 * @stereotype factory
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.3 $
 */
public final class XmlDateFormat implements Serializable {

	/**
	 * This is the version that we can control for serialization purposes
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The Calendar object to do the transformations
	 */
	private Calendar calendar = new GregorianCalendar(TimeZone
			.getTimeZone("GMT"));

	/**
	 * A Number formatter
	 */
	private NumberFormat numberFormat = new DecimalFormat("0000");

	/**
	 * A logger for debugging purposes
	 */
	static Logger logger = Logger.getLogger(XmlDateFormat.class);

	/**
	 * The default constructor
	 */
	public XmlDateFormat() {
		numberFormat.setMaximumFractionDigits(0);
	}

	/**
	 * Format a date as the standard used in XML
	 * 
	 * @param date
	 *            The date to convert to a string representation
	 * @return A GMT representation. Example 2003-05-05T16:11:44Z
	 */
	public String format(Date date) {
		if (date == null) {
			return null;
		}
		StringBuffer toAppendTo = new StringBuffer();
		calendar.setTime(date);

		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);

		numberFormat.setMinimumIntegerDigits(4);
		toAppendTo.append(numberFormat.format((long) year));

		numberFormat.setMinimumIntegerDigits(2);
		toAppendTo.append("-" + numberFormat.format((long) month));
		toAppendTo.append("-" + numberFormat.format((long) day));
		toAppendTo.append("T" + numberFormat.format((long) hour));
		toAppendTo.append(":" + numberFormat.format((long) minute));
		toAppendTo.append(":" + numberFormat.format((long) second) + "Z");
		return toAppendTo.toString();

	}

	/**
	 * Format a date as a string. This is a compact format used for generating
	 * filenames
	 * 
	 * @param date
	 *            The data to convert to a string representation
	 * @return A compact GMT representation. Example 20030505T161144
	 */
	public String formatCompact(Date date) {
		StringBuffer toAppendTo = new StringBuffer();
		calendar.setTime(date);

		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);

		numberFormat.setMinimumIntegerDigits(4);
		toAppendTo.append(numberFormat.format((long) year));

		numberFormat.setMinimumIntegerDigits(2);
		toAppendTo.append(numberFormat.format((long) month));
		toAppendTo.append(numberFormat.format((long) day));
		toAppendTo.append("T" + numberFormat.format((long) hour));
		toAppendTo.append(numberFormat.format((long) minute));
		toAppendTo.append(numberFormat.format((long) second));
		return toAppendTo.toString();
	}

	/**
	 * This method takes in a string of format YYYY-MM-DD HH:MM:SSZ and converts
	 * that to a Date object
	 * 
	 * @param source
	 *            is the <code>String</code> that will be used to parse and
	 *            create a <code>Date</code> object.
	 * @return a <code>Date</code> if parsing was successful, otherwise it will
	 *         return null if not parsed. (This is consistent with how Java date
	 *         parsers return their parsing results).
	 */
	public Date parse(String source) {

		// Create the date to return
		Date dateToReturn = null;

		// Reset the calendar
		calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

		// Some local variables for tracking
		// int index = 0;
		int year = 0;
		int month = 0;
		int day = 0;
		int hour = 0;
		int minute = 0;
		int second = 0;
		String zone = null;

		// Setup the pattern to grab the values
		Pattern datePattern = Pattern
				.compile("^\\s*(\\d{4})[/|-]+(\\d+)[/|-]+(\\d+)\\D*(\\d+):(\\d+):(\\d+)\\s*(\\S+)\\s*$");
		Matcher matcher = datePattern.matcher(source);
		if (matcher.matches()) {
			year = new Integer(matcher.group(1)).intValue();
			month = new Integer(matcher.group(2)).intValue();
			month--;
			day = new Integer(matcher.group(3)).intValue();
			hour = new Integer(matcher.group(4)).intValue();
			minute = new Integer(matcher.group(5)).intValue();
			second = new Integer(matcher.group(6)).intValue();
			zone = matcher.group(7);
		} else {
			datePattern = Pattern
					.compile("^\\s*(\\d{4})[/|-]+(\\d+)[/|-]+(\\d+)\\D*(\\d+):(\\d+):(\\d+)\\s*$");
			matcher = datePattern.matcher(source);
			if (matcher.matches()) {
				year = new Integer(matcher.group(1)).intValue();
				month = new Integer(matcher.group(2)).intValue();
				month--;
				day = new Integer(matcher.group(3)).intValue();
				hour = new Integer(matcher.group(4)).intValue();
				minute = new Integer(matcher.group(5)).intValue();
				second = new Integer(matcher.group(6)).intValue();
				zone = "Z";
			} else {
				logger.error("Could not pattern match to string");
				return null;
			}
		}

		// Set the calendar
		calendar.set(year, month, day, hour, minute, second);
		// Depending on the zone, you may have to add or subtract hours
		// Pattern zonePattern =
		// Pattern.compile("^([+-]*)(\\d+)([+-]*)(\\d*)$");
		Pattern zonePattern = Pattern
				.compile("^\\D*([+|-])(\\d{2})(\\D*)(\\d{2})*$");
		Matcher zoneMatcher = zonePattern.matcher(zone);
		if (zoneMatcher.matches()) {
			String offsetHourDirection = zoneMatcher.group(1);
			String offsetHour = zoneMatcher.group(2);
			String offsetMinuteIndicator = zoneMatcher.group(3);
			String offsetMinute = zoneMatcher.group(4);
			if ((offsetHourDirection.equals("+"))
					|| (offsetHourDirection.equals(""))) {
				calendar.add(Calendar.HOUR, new Integer("-" + offsetHour)
						.intValue());
			} else if (offsetHourDirection.equals("-")) {
				calendar.add(Calendar.HOUR, new Integer(offsetHour).intValue());
			}
			if ((offsetMinuteIndicator != null) && (offsetMinute != null)) {
				if (offsetMinuteIndicator.equals(":")) {
					if (offsetHourDirection.equals("+")) {
						calendar.add(Calendar.MINUTE, new Integer("-"
								+ offsetMinute).intValue());
					} else if (offsetHourDirection.equals("-")) {
						calendar.add(Calendar.MINUTE, new Integer(offsetMinute)
								.intValue());
					}
				} else if (offsetMinuteIndicator.equals("+")) {
					calendar.add(Calendar.MINUTE, new Integer("-"
							+ offsetMinute).intValue());
				} else if (offsetMinuteIndicator.equals("-")) {
					calendar.add(Calendar.MINUTE, new Integer(offsetMinute)
							.intValue());
				}
			}
		}
		dateToReturn = calendar.getTime();
		return dateToReturn;
	}

}