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

import java.util.Date;

public class DateUtils {

	/**
	 * This method extract the time in epoch seconds from a <code>Date</code>.
	 * 
	 * @param date
	 * @return
	 */
	public static long getEpochTimestampSeconds(Date date) {
		return getEpochTimestampSecondsFromEpochMillis(date.getTime());
	}

	/**
	 * This method extract the time in epoch seconds from epoch millis
	 * 
	 * @param date
	 * @return
	 */
	public static long getEpochTimestampSecondsFromEpochMillis(long epochMillis) {
		return epochMillis / 1000;
	}

	/**
	 * This method extracts the nanoseconds from the date supplied.
	 * 
	 * @param date
	 * @return
	 */
	public static long getNanoseconds(Date date) {
		return getNanosecondsFromEpochMillis(date.getTime());
	}

	/**
	 * This method extracts the nanoseconds from the date supplied.
	 * 
	 * @param date
	 * @return
	 */
	public static long getNanosecondsFromEpochMillis(long epochMillis) {
		return (epochMillis % 1000) * 1000000;
	}

	/**
	 * This method takes in epoch seconds and a nanoseconds portion and
	 * constructs the appropriate Java <code>Date</code> to match that time
	 * 
	 * @param epochSeconds
	 * @param nanoseconds
	 * @return
	 */
	public static Date constructDateFromEpochSecondsAndNanseconds(
			long epochSeconds, long nanoseconds) {
		Date date = new Date();
		date.setTime(constructEpochMillisFromEpochSecondsAndNanoseconds(
				epochSeconds, nanoseconds));
		return date;
	}

	/**
	 * This method takes in epoch seconds and a nanoseconds portion and
	 * constructs the appropriate timestamp in epoch millis to match that time
	 * 
	 * @param epochSeconds
	 * @param nanoseconds
	 * @return
	 */
	public static long constructEpochMillisFromEpochSecondsAndNanoseconds(
			long epochSeconds, long nanoseconds) {
		return (epochSeconds * 1000) + (nanoseconds / 1000000);
	}

	/**
	 * This method takes in two <code>Date</code> objects and compares their
	 * times to see if they are equal. The last parameter defines a window in
	 * seconds that they must be within. For example if you specify 1, the two
	 * dates must be within 1 second of each other to be considered equal.
	 * 
	 * @param date1
	 *            The first date
	 * @param date2
	 *            The second date
	 * @param timeWindowInMilliseconds
	 *            is the maximum difference allowed (in seconds) between the two
	 *            date to meet the criteria of being equal
	 * @return a <code>boolean</code> to indicate if the dates are considered
	 *         equal (<code>true</code>) or not (<code>false</code>)
	 */
	public static boolean equalsWithinSeconds(Date date1, Date date2,
			long timeWindowInSeconds) {

		if ((date1 == null) || (date2 == null)) {
			return false;
		}

		// Grab each time in millis
		long millis1 = date1.getTime();
		long millis2 = date2.getTime();

		// Now calculate the difference in seconds and see if it is less than
		// separation
		if (Math.abs((millis1 - millis2) / 1000) < timeWindowInSeconds) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method will take in a <code>Date</code> object and change the
	 * underlying time in milliseconds by truncating the time to the next lowest
	 * second. For example if you times in milliseconds are 1300, it will set it
	 * to 1. If the time is 1900 milliseconds, it will set it to 1.
	 * 
	 * @param date
	 * @return
	 */
	public static Date roundDateDownToSeconds(Date date) {
		Date dateToReturn = new Date();

		// Get time in milliseconds
		long dateInMillis = date.getTime();

		// Convert that by truncating the millis part of the time
		long dateInSeconds = (dateInMillis / 1000);

		// Now set the date on the new date
		dateToReturn.setTime(dateInSeconds * 1000);

		// Now return it
		return dateToReturn;
	}
}
