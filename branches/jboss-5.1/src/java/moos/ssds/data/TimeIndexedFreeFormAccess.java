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
package moos.ssds.data;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import moos.ssds.data.parsers.Parser;
import moos.ssds.data.parsers.IParser;
import moos.ssds.data.util.MathUtil;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.RecordVariable;

import org.apache.log4j.Logger;

/**
 * This class provides data from a <code>DataContainer</code> that has an
 * unstructured form.
 */
public class TimeIndexedFreeFormAccess implements ITimeIndexedDataAccess {

    /**
     * The protected constructor that takes in a DataContainer and a time window
     * to construct an object to provide access to data
     * 
     * @param dataContainer
     * @param startDate
     * @param endDate
     */
    public TimeIndexedFreeFormAccess(DataContainer dataContainer,
        Date startDate, Date endDate, Collection recordVariableNames) {

        // Assign the local variables
        this.dataContainer = dataContainer;
        if (startDate == null) {
            this.startDate = new Date();
            this.startDate.setTime(0);
        } else {
            this.startDate = startDate;
        }
        // If there is not start date assign it the begging of time
        if (this.endDate == null) {
            this.endDate = new Date();
        } else {
            this.endDate = endDate;
        }
        this.recordVariableNames = recordVariableNames;

        // Create the appropriate parser
        this.parser = new Parser(dataContainer);

        // Now initialize the data
        this.initializeData();
    }

    /**
     * @see IDataAccess#getData(String)
     */
    public Object[] getData(String recordVariableName) {
        // Loop through the list of record variables and find the one
        // that matches the given name and return its data.
        Set rvs = mapRecordVariablesToData.keySet();
        Iterator iterator = rvs.iterator();
        while (iterator.hasNext()) {
            RecordVariable tempRV = (RecordVariable) iterator.next();
            if (tempRV.getName().equalsIgnoreCase(recordVariableName)) {
                return this.getData(tempRV);
            }
        }
        // If we are here, just return null, because no recordVariable with
        // that name was found
        return null;
    }

    /**
     * @see IDataAccess#getData(RecordVariable)
     */
    public Object[] getData(RecordVariable recordVariable) {
        // Make sure argument is not null
        if (recordVariable == null) {
            return null;
        }

        // Setup times if not setup already
        if (times == null) {
            logger.debug("No times found, going to initialize the data");
            initializeData();
        }

        // Try to find by ID
        Set rvs = mapRecordVariablesToData.keySet();
        Iterator iterator = rvs.iterator();
        RecordVariable foundRV = recordVariable;
        while (iterator.hasNext()) {
            RecordVariable currentRV = (RecordVariable) iterator.next();
            if ((currentRV.getId() != null)
                && (recordVariable.getId() != null)
                && (currentRV.getId().longValue() == recordVariable.getId()
                    .longValue())) {
                foundRV = currentRV;
                break;
            } else if ((currentRV.getName() != null)
                && (recordVariable.getName() != null)
                && (currentRV.getName().equalsIgnoreCase(recordVariable
                    .getName()))
                && (currentRV.getColumnIndex() == recordVariable
                    .getColumnIndex())) {
                foundRV = currentRV;
            }
        }
        if (mapRecordVariablesToData.containsKey(foundRV)) {
            // Grab the data list
            List dataList = (List) mapRecordVariablesToData.get(foundRV);

            // Sort based on time. Reject duplicates (use first occurence)
            Object[] out = (Object[]) dataList.toArray(new Object[dataList
                .size()]);
            return MathUtil.orderVector(out, sortOrder);
        } else {
            logger.debug("getData called with RecordVariable "
                + recordVariable.getName()
                + ", but it appears that data needs to be initialized");
            initializeData();
        }

        // try to call this method again, now that the buffer is filled in.
        return getData(foundRV);
    }

    /**
     * @see ITimeIndexedDataAccess#getTime()
     */
    public Object[] getTime() {
        // logger.debug("getTime called");
        Object[] timesToReturn = null;
        if (times == null) {
            logger.debug("No times found, going to initialize the data");
            initializeData();
        }
        if (sortOrder != null) {
            // logger.debug("Sort order was not null");
            timesToReturn = MathUtil.orderVector((Long[]) times
                .toArray(new Long[times.size()]), sortOrder);
            // logger.debug("going to return an array of " +
            // timesToReturn.length );
        } else {
            timesToReturn = times.toArray(new Long[times.size()]);
        }
        return timesToReturn;
    }

    /**
     * The method reads all the data and times in from the given data container
     * to so that it can be utilized later
     */
    private void initializeData() {

        // logger.debug("initializeData called");
        // Create a new array for times
        times = new ArrayList();

        // This loop is creating a new array list for each variable in the
        // datacontainer
        // where the data will be stored
        for (Iterator iter = getDataContainer().getRecordDescription()
            .getRecordVariables().iterator(); iter.hasNext();) {
            Object obj = iter.next();
            // Check to see if the collection of record variable names was
            // specified
            if ((this.recordVariableNames != null)
                && (this.recordVariableNames.size() > 0)) {
                // Cast to a record variable to check against name
                RecordVariable rv = (RecordVariable) obj;
                if (this.recordVariableNames.contains(rv.getName())) {
                    mapRecordVariablesToData.put(obj, new ArrayList());
                }
            } else {
                mapRecordVariablesToData.put(obj, new ArrayList());
            }
        }

        // This is the map that contains a map of IRecordVariable to
        // data for a single record that was parsed from the DataContainer
        Map freeFormMapRecordVariablesToData = null;

        // Grab the ASCI file context from the parser
        // AsciiFileContext context = (AsciiFileContext)
        // getParser().getContext();

        // An index counter
        int index = 0;

        // Load all data in a single pass for all record variables (this is
        // relying
        // somewhat on smaller time frames so that the in-memory footprint
        // doesn't
        // run wild.
        while (getParser().hasNext()) {
            // Increment record index that we have read
            index++;

            // Grab the data for the record variables from the DataContainer
            try {
                freeFormMapRecordVariablesToData = (Map) getParser().next();
            } catch (Exception e) {
                logger.debug("Failed to parse packet contents: "
                    + e.getMessage());
                continue;
            }

            // Check to see if the data is even valid, if not skip it
            if (freeFormMapRecordVariablesToData == null) {
                logger
                    .debug("Returned freeFormMapRecordVariablesToData was null");
                continue;
            }

            // Try to get the date out of the record
            Date recordDate = this.findDate(freeFormMapRecordVariablesToData);
//            logger.debug("recordDate at index " + index + " found to be " + recordDate);

            // If one found, add the data if in the time range specified
            if (((recordDate == null) && (freeFormMapRecordVariablesToData != null))
                || ((freeFormMapRecordVariablesToData != null) && ((recordDate
                    .after(getStartDate()) && recordDate.before(getEndDate()))
                    || recordDate.equals(getStartDate()) || recordDate
                    .equals(getEndDate())))) {
                if (recordDate != null) {
                    times.add(new Long(recordDate.getTime()));
                    // logger.debug("Adding record date that was converted to a
                    // long " + recordDate.getTime());
                    datesResolved = true;
                } else {
                    times.add(new Long(index));
                }
                for (Iterator iter = freeFormMapRecordVariablesToData.keySet()
                    .iterator(); iter.hasNext();) {

                    RecordVariable rv = (RecordVariable) iter.next();
                    List vList = (List) mapRecordVariablesToData.get(rv);
                    if (vList != null) {
                        Object o = freeFormMapRecordVariablesToData.get(rv);
                        if (o != null) {
                            vList.add(o);
                        } else {
                            vList.add(null);
                        }
                    }
                }
            }
        }

        // Create the sort order for the times
        // logger.debug("There are " + times.size() + " time entries");
        sortOrder = MathUtil.uniqueSort((Long[]) times.toArray(new Long[times
            .size()]));
        // logger.debug("Soft order has " + sortOrder.length + " elements");
    }

    /**
     * This returns a <code>boolean</code> that indicates if the object was
     * able to determine a time fields from the <code>DataContainer</code>
     * 
     * @return a <code>boolean</code> that indicates if the object thinks it
     *         was able to find enough information to resolve the date/time on
     *         the data (<code>true</code>) or not (<code>false</code>)
     */
    public boolean getDatesResolved() {
        return datesResolved;
    }

    /**
     * This method takes in a map that holds the RecordVariables and their
     * associated values in the column. What will be returned is a java Date
     * (GMT) that was parsed out of the data record in question. This is simply
     * a method that tries to do its best and find a column (or columns) of data
     * that make up a valid timestamp. If nothing can be found, null is
     * returned. There are some pretty serious assumptions made in this part. It
     * looks for column that is of the name 'datetime' and then uses the units
     * to determine which part of the timestamp the column represents. For
     * example, a variable named datetime with units of (MM) would then contain
     * the two digit month. I am using the designations used in the Java class
     * SimpleDateFormat: <br>
     * <a
     * href="http://java.sun.com/j2se/1.4.2/docs/api/index.html">SimpleDateFormat</a>
     * <br>
     * TODO KJG 06-15-2005 The better thing to do here would be to figure out
     * the parsing scheme once and then just execute that same parsing scheme
     * for each record. Currently, I figure out the scheme each time. This is
     * too slow.
     */
    private Date findDate(Map freeFormMapRecordVariablesToData) {

        // The date to return
        Date resolvedDate = null;

        if (freeFormMapRecordVariablesToData != null) {
            // First create a GMT calendar
            Calendar recordCalendar = Calendar.getInstance();
            recordCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
            boolean foundCalendarInformation = false;

            // Now create placeholders for all date/time fields
            long epochMilliseconds = -1;
            int year = -1;
            int month = -1;
            int weekInYear = -1;
            int weekInMonth = -1;
            int dayInYear = -1;
            int dayInMonth = -1;
            int dayOfWeekInMonth = -1;
            int dayInWeek = -1;
            int hourOfDay0to23 = -1;
            int hourOfDay1to24 = -1;
            int hourInAmPm0to11 = -1;
            int hourInAmPm1to12 = -1;
            int minute = -1;
            int second = -1;
            int millisecond = -1;

            // Loop through all record variables and check it against what we
            // are looking for
            Set keySet = freeFormMapRecordVariablesToData.keySet();
            Iterator i = keySet.iterator();
            while (i.hasNext()) {
                RecordVariable currentRecordVariable = (RecordVariable) i
                    .next();

                // Check to see if the variable name is datetime
                if ((currentRecordVariable.getFormat()
                    .equalsIgnoreCase("datetime"))
                    || (currentRecordVariable.getName().indexOf("Time") >= 0)
                    || (currentRecordVariable.getName().indexOf("time") >= 0)) {

                    // Grab the data value
                    Object data = freeFormMapRecordVariablesToData
                        .get(currentRecordVariable);
                    boolean dataIsNumber = false;
                    Number dataNumber = null;
                    try {
                        dataNumber = (Number) data;
                    } catch (Throwable e1) {
                        dataIsNumber = false;
                        dataNumber = null;
                    }
                    if (dataNumber != null)
                        dataIsNumber = true;

                    // Grab the units
                    String units = currentRecordVariable.getUnits();

                    // Look for the more common parsable patterns defined
                    // above
                    Matcher mDate1 = date1.matcher(units);
                    Matcher mDate2 = date2.matcher(units);
                    Matcher mTime1 = time1.matcher(units);
                    Matcher mTime2 = time2.matcher(units);
                    Matcher mTime3 = time3.matcher(units);

                    // Before applying the generic processing that looks
                    // for exact index matches, look for the special
                    // cases first. There are:
                    // 1. "Epoch seconds" can be used to directly determine
                    // the date
                    // 2. "Epoch milliseconds" can be used to directly
                    // determine the date
                    // 3. The third one will look for any combinations
                    // that are likely to be date time (separated by
                    // slashes/dashes for days and separated by colons
                    // for seconds. The reason this is done is that
                    // often times, when the user specifies units of
                    // MM/dd/yyyy for example, the day might actually
                    // come up as 2/1/2005. If you try to parse be
                    // the indexes of the MM and DD, it will fail
                    // because they do not have two positions. For
                    // these scenarios, pattern matching is used to
                    // get those values.
                    if ((units.equalsIgnoreCase("epoch seconds"))
                        || (units
                            .equalsIgnoreCase("seconds since 1970-01-01 00:00:00"))) {
                        epochMilliseconds = -1;
                        try {
                            long epochSeconds = -1;
                            if (dataIsNumber) {
                                double epochMillisecondsDouble = dataNumber
                                    .doubleValue() * 1000;
                                epochMilliseconds = (long) epochMillisecondsDouble;
                            } else {
                                epochSeconds = Long.parseLong((String) data);
                                epochMilliseconds = epochSeconds * 1000;
                            }
                            // Convert to milliseconds
                        } catch (NumberFormatException e) {
                            logger.error("Could not parse " + data
                                + " into epoch seconds");
                            epochMilliseconds = -1;
                        }
                        if (epochMilliseconds >= 0) {
                            recordCalendar.setTimeInMillis(epochMilliseconds);
                            // Now just return that date
                            return recordCalendar.getTime();
                        }
                    } else if (units.equalsIgnoreCase("epoch milliseconds")) {
                        epochMilliseconds = -1;
                        try {
                            if (dataIsNumber) {
                                double epochMillisecondsDouble = dataNumber
                                    .doubleValue();
                                epochMilliseconds = (long) epochMillisecondsDouble;
                            } else {
                                epochMilliseconds = Long
                                    .parseLong((String) data);
                            }
                        } catch (NumberFormatException e) {
                            logger.error("Could not parse " + data
                                + " into epoch seconds");
                            epochMilliseconds = -1;
                        }
                        if (epochMilliseconds >= 0) {
                            recordCalendar.setTimeInMillis(epochMilliseconds);
                            // Now just return that date
                            return recordCalendar.getTime();
                        }
                    } else if ((mDate1.matches() || mDate2.matches()
                        || mTime1.matches() || mTime2.matches() || mTime3
                        .matches())
                        && (!dataIsNumber)) {
                        // Use the SimpleDateFormat
                        DateFormat df = new SimpleDateFormat(units);
                        // Now parse the date
                        Date tempDate = null;
                        try {
                            tempDate = df.parse((String) data);
                        } catch (ParseException e) {}
                        if (tempDate != null) {
                            recordCalendar.setTimeInMillis(tempDate.getTime());
                            epochMilliseconds = tempDate.getTime();
                            foundCalendarInformation = true;
                        }
                    } else if (units.equalsIgnoreCase("minuteOfDay")) {
                        int minuteOfDay = -1;
                        try {
                            if (dataIsNumber) {
                                minuteOfDay = dataNumber.intValue();
                            } else {
                                minuteOfDay = Integer.parseInt((String) data);
                            }
                        } catch (NumberFormatException e) {}
                        if (minuteOfDay >= 0) {
                            // Now convert to hours and minutes

                            // If it is less than 60, assign the hour to
                            // zero and the minutes to the number
                            if (minuteOfDay < 60) {
                                hourOfDay0to23 = 0;
                                minute = minuteOfDay;
                            } else {
                                // Divide by 60 to get the hour
                                hourOfDay0to23 = minuteOfDay / 60;
                                // Now take the remainder to be minutes
                                minute = minuteOfDay - (hourOfDay0to23 * 60);
                            }
                            recordCalendar.set(Calendar.HOUR_OF_DAY,
                                hourOfDay0to23);
                            recordCalendar.set(Calendar.MINUTE, minute);
                            foundCalendarInformation = true;
                        }

                    } else if (!dataIsNumber) {

                        // Era
                        int eraIndex = units.indexOf("G");
                        if (eraIndex >= 0) {
                            String era = ((String) data).substring(eraIndex,
                                units.lastIndexOf("G") + 1);
                            logger.debug("Era found at " + eraIndex
                                + " and is " + era);
                            if (era.equalsIgnoreCase("bc")) {
                                recordCalendar.set(Calendar.ERA,
                                    GregorianCalendar.BC);
                                foundCalendarInformation = true;
                            } else if (era.equalsIgnoreCase("ad")) {
                                recordCalendar.set(Calendar.ERA,
                                    GregorianCalendar.AD);
                                foundCalendarInformation = true;
                            }
                        }

                        // Year
                        int yearIndex = units.indexOf("y");
                        if (yearIndex >= 0) {
                            String yearString = ((String) data).substring(
                                yearIndex, units.lastIndexOf("y") + 1);
                            logger.debug("Year found at " + yearIndex
                                + " and is " + yearString);
                            try {
                                year = Integer.parseInt(yearString);
                                if (year >= 0) {
                                    if (year < 70) {
                                        year += 2000;
                                    } else if (year < 1970) {
                                        year += 1900;
                                    }
                                }
                            } catch (NumberFormatException e) {
                                logger.error("Could not parse " + yearString
                                    + " into a year");
                            }
                            logger.debug("Parsed year is " + year);
                            if (year >= 0) {
                                recordCalendar.set(Calendar.YEAR, year);
                                foundCalendarInformation = true;
                            }
                        }

                        // Month in year
                        int monthInYearIndex = units.indexOf("M");
                        if (monthInYearIndex >= 0) {
                            String monthInYearString = ((String) data)
                                .substring(monthInYearIndex, units
                                    .lastIndexOf("M") + 1);
                            if (monthInYearString.length() > 2) {
                                DateFormatSymbols dfs = new DateFormatSymbols();
                                String[] longMonths = dfs.getMonths();
                                String[] shortMonths = dfs.getShortMonths();
                                // Search for string
                                int index = Arrays.binarySearch(longMonths,
                                    monthInYearString);
                                if (index < 0) {
                                    index = Arrays.binarySearch(shortMonths,
                                        monthInYearString);
                                }
                                if (index >= 0) {
                                    month = index;
                                } else {
                                    logger
                                        .error("Could not find a month that matched string "
                                            + monthInYearString);
                                }
                            } else {
                                try {
                                    month = Integer.parseInt(monthInYearString);
                                    // Adjust for zero base
                                    month--;
                                } catch (NumberFormatException e) {
                                    logger.error("Could not parse "
                                        + monthInYearString + " to a month");
                                    month = -1;
                                }
                            }
                            if (month >= 0) {
                                recordCalendar.set(Calendar.MONTH, month);
                                foundCalendarInformation = true;
                            }
                        }

                        // Week in year
                        int weekInYearIndex = units.indexOf("w");
                        if (weekInYearIndex >= 0) {
                            String weekInYearString = ((String) data)
                                .substring(weekInYearIndex, units
                                    .lastIndexOf("w") + 1);
                            try {
                                weekInYear = Integer.parseInt(weekInYearString);
                            } catch (NumberFormatException e) {
                                logger.error("Could not parse "
                                    + weekInYearString + " into a weekInYear");
                                weekInYear = -1;
                            }
                            if ((weekInYear > 0) && (weekInYear <= 53)) {
                                recordCalendar.set(Calendar.WEEK_OF_YEAR,
                                    weekInYear);
                                foundCalendarInformation = true;
                            }
                        }

                        // Week in month
                        int weekInMonthIndex = units.indexOf("W");
                        if (weekInMonthIndex >= 0) {
                            String weekInMonthString = ((String) data)
                                .substring(weekInMonthIndex, units
                                    .lastIndexOf("W") + 1);
                            try {
                                weekInMonth = Integer
                                    .parseInt(weekInMonthString);
                            } catch (NumberFormatException e) {
                                logger.error("Could not parse " + weekInMonth
                                    + " to weekInMonth");
                                weekInMonth = -1;
                            }
                            if ((weekInMonth > 0) && (weekInMonth <= 5)) {
                                recordCalendar.set(Calendar.WEEK_OF_MONTH,
                                    weekInMonth);
                                foundCalendarInformation = true;
                            }
                        }

                        // Day in year
                        int dayInYearIndex = units.indexOf("D");
                        if (dayInYearIndex >= 0) {
                            String dayInYearString = ((String) data).substring(
                                dayInYearIndex, units.lastIndexOf("D") + 1);
                            try {
                                dayInYear = Integer.parseInt(dayInYearString);
                            } catch (NumberFormatException e) {
                                logger.error("Could not parse "
                                    + dayInYearString + " into dayInYear");
                                dayInYear = -1;
                            }
                            if ((dayInYear > 0) && (dayInYear <= 366)) {
                                recordCalendar.set(Calendar.DAY_OF_YEAR,
                                    dayInYear);
                                foundCalendarInformation = true;
                            }
                        }

                        // Day in month
                        int dayInMonthIndex = units.indexOf("d");
                        if (dayInMonthIndex >= 0) {
                            String dayInMonthString = ((String) data)
                                .substring(dayInMonthIndex, units
                                    .lastIndexOf("d") + 1);
                            try {
                                dayInMonth = Integer.parseInt(dayInMonthString);
                            } catch (NumberFormatException e) {
                                logger.error("Could not parse "
                                    + dayInMonthString + " into dayInMonth");
                                dayInMonth = -1;
                            }
                            if ((dayInMonth > 0) && (dayInMonth < 35)) {
                                recordCalendar.set(Calendar.DAY_OF_MONTH,
                                    dayInMonth);
                                foundCalendarInformation = true;
                            }
                        }

                        // Day of week in month
                        int dayOfWeekInMonthIndex = units.indexOf("F");
                        if (dayOfWeekInMonthIndex >= 0) {
                            String dayOfWeekInMonthString = ((String) data)
                                .substring(dayOfWeekInMonthIndex, units
                                    .lastIndexOf("F") + 1);
                            try {
                                dayOfWeekInMonth = Integer
                                    .parseInt(dayOfWeekInMonthString);
                            } catch (NumberFormatException e) {
                                logger.error("Could not parse "
                                    + dayOfWeekInMonth
                                    + " into dayOfWeekInMonth");
                                dayOfWeekInMonth = -1;
                            }
                            if (dayOfWeekInMonth > 0) {
                                recordCalendar.set(
                                    Calendar.DAY_OF_WEEK_IN_MONTH,
                                    dayOfWeekInMonth);
                                foundCalendarInformation = true;
                            }
                        }

                        // Day in week
                        int dayInWeekIndex = units.indexOf("E");
                        if (dayInWeekIndex >= 0) {
                            String dayInWeekString = ((String) data).substring(
                                dayInWeekIndex, units.lastIndexOf("E") + 1);
                            DateFormatSymbols dfs = new DateFormatSymbols();
                            String[] longWeekdays = dfs.getWeekdays();
                            String[] shortWeekdays = dfs.getShortWeekdays();
                            // Search for string
                            int index = Arrays.binarySearch(longWeekdays,
                                dayInWeekString);
                            if (index < 0) {
                                index = Arrays.binarySearch(shortWeekdays,
                                    dayInWeekString);
                            }
                            if (index >= 0) {
                                dayInWeek = index;
                            } else {
                                logger
                                    .error("Could not find a day of the week that matched string "
                                        + dayInWeekString);
                            }
                            if (dayInWeek >= 0) {
                                recordCalendar.set(Calendar.DAY_OF_WEEK,
                                    dayInWeek);
                                foundCalendarInformation = true;
                            }
                        }

                        // Am/Pm marker
                        int amPmMarkerIndex = units.indexOf("a");
                        if (amPmMarkerIndex >= 0) {
                            String amPmMarkerString = ((String) data)
                                .substring(amPmMarkerIndex, units
                                    .lastIndexOf("a") + 1);
                            if (amPmMarkerString.equalsIgnoreCase("am")) {
                                recordCalendar.set(Calendar.AM_PM, Calendar.AM);
                                foundCalendarInformation = true;
                            } else if (amPmMarkerString.equalsIgnoreCase("pm")) {
                                recordCalendar.set(Calendar.AM_PM, Calendar.PM);
                                foundCalendarInformation = true;
                            }
                        }

                        // Hour in day(0-23)
                        int hourInDay0to23Index = units.indexOf("H");
                        if (hourInDay0to23Index >= 0) {
                            String hourInDay0to23String = ((String) data)
                                .substring(hourInDay0to23Index, units
                                    .lastIndexOf("H") + 1);
                            try {
                                hourOfDay0to23 = Integer
                                    .parseInt(hourInDay0to23String);
                            } catch (NumberFormatException e) {
                                logger.error("Could not parse "
                                    + hourInDay0to23String
                                    + " into hourOfDay0to23");
                                hourOfDay0to23 = -1;
                            }
                            if ((hourOfDay0to23 >= 0) && (hourOfDay0to23 <= 23)) {
                                recordCalendar.set(Calendar.HOUR_OF_DAY,
                                    hourOfDay0to23);
                                foundCalendarInformation = true;
                            }
                        }

                        // Hour in day(1-24)
                        int hourInDay1to24Index = units.indexOf("k");
                        if (hourInDay1to24Index >= 0) {
                            String hourInDay1to24String = ((String) data)
                                .substring(hourInDay1to24Index, units
                                    .lastIndexOf("k") + 1);
                            try {
                                hourOfDay1to24 = Integer
                                    .parseInt(hourInDay1to24String);
                            } catch (NumberFormatException e) {
                                logger.error("Could not parse "
                                    + hourInDay1to24String
                                    + " into hourInDay1to24");
                                hourInDay1to24Index = -1;
                            }
                            if ((hourOfDay1to24 > 0) && (hourOfDay1to24 <= 24)) {
                                // Convert to 0-23 base
                                hourOfDay1to24--;
                                recordCalendar.set(Calendar.HOUR_OF_DAY,
                                    hourOfDay1to24);
                                foundCalendarInformation = true;
                            }
                        }

                        // Hour in am/pm(0-11)
                        int hourInAmPm0to11Index = units.indexOf("K");
                        if (hourInAmPm0to11Index >= 0) {
                            String hourInAmPm0to11String = ((String) data)
                                .substring(hourInAmPm0to11Index, units
                                    .lastIndexOf("K") + 1);
                            try {
                                hourInAmPm0to11 = Integer
                                    .parseInt(hourInAmPm0to11String);
                            } catch (NumberFormatException e) {
                                logger.error("Could not parse "
                                    + hourInAmPm0to11String
                                    + " into hourInAmPm0to11");
                                hourInAmPm0to11 = -1;
                            }
                            if ((hourInAmPm0to11 >= 0)
                                && (hourInAmPm0to11 <= 11)) {
                                // Increment to make 1-12
                                hourInAmPm0to11++;
                                recordCalendar.set(Calendar.HOUR,
                                    hourInAmPm0to11);
                                foundCalendarInformation = true;
                            }
                        }

                        // Hour in am/pm(1-12)
                        int hourInAmPm1to12Index = units.indexOf("h");
                        if (hourInAmPm1to12Index >= 0) {
                            String hourInAmPm1to12String = ((String) data)
                                .substring(hourInAmPm1to12Index, units
                                    .lastIndexOf("h") + 1);
                            try {
                                hourInAmPm1to12 = Integer
                                    .parseInt(hourInAmPm1to12String);
                            } catch (NumberFormatException e) {
                                logger.error("Could not parse "
                                    + hourInAmPm1to12String
                                    + " into hourInAmPm1to12");
                                hourInAmPm1to12 = -1;
                            }
                            if ((hourInAmPm1to12 >= 1)
                                && (hourInAmPm1to12 <= 12)) {
                                recordCalendar.set(Calendar.HOUR,
                                    hourInAmPm1to12);
                                foundCalendarInformation = true;
                            }
                        }

                        // Minute in hour
                        int minuteInHourIndex = units.indexOf("m");
                        if (minuteInHourIndex >= 0) {
                            String minuteInHourString = ((String) data)
                                .substring(minuteInHourIndex, units
                                    .lastIndexOf("m") + 1);
                            try {
                                minute = Integer.parseInt(minuteInHourString);
                            } catch (NumberFormatException e) {
                                logger.error("Could not parse "
                                    + minuteInHourString + " to minute");
                                minute = -1;
                            }
                            if ((minute >= 0) && (minute <= 59)) {
                                recordCalendar.set(Calendar.MINUTE, minute);
                                foundCalendarInformation = true;
                            }
                        }

                        // Second in minute
                        int secondInMinuteIndex = units.indexOf("s");
                        if (secondInMinuteIndex >= 0) {
                            String secondString = ((String) data)
                                .substring(secondInMinuteIndex, units
                                    .lastIndexOf("s") + 1);
                            try {
                                second = Integer.parseInt(secondString);
                            } catch (NumberFormatException e) {
                                logger.error("Could not parse " + secondString
                                    + " to second");
                                second = -1;
                            }
                            if ((second >= 0) && (second <= 59)) {
                                recordCalendar.set(Calendar.SECOND, second);
                                foundCalendarInformation = true;
                            }
                        }

                        // Millisecond
                        int millisecondIndex = units.indexOf("S");
                        if (millisecondIndex >= 0) {
                            String millisString = ((String) data).substring(
                                millisecondIndex, units.lastIndexOf("S") + 1);
                            try {
                                millisecond = Integer.parseInt(millisString);
                            } catch (NumberFormatException e) {
                                logger.error("Could not parse " + millisString
                                    + " to milliseconds");
                            }
                            if ((millisecond >= 0) && (millisecond <= 999)) {
                                recordCalendar.set(Calendar.MILLISECOND,
                                    millisecond);
                                foundCalendarInformation = true;
                            }
                        }

                        // TimeZone (z)
                        int timeZoneLittleZIndex = units.indexOf("z");
                        if (timeZoneLittleZIndex >= 0) {
                            String timeZoneString = ((String) data).substring(
                                timeZoneLittleZIndex,
                                units.lastIndexOf("z") + 1);
                            TimeZone tz = null;
                            try {
                                tz = TimeZone.getTimeZone(timeZoneString);
                            } catch (RuntimeException e) {}
                            if (tz != null) {
                                recordCalendar.setTimeZone(tz);
                            }
                        }

                        // TimeZone (Z)
                        int timeZoneBigZIndex = units.indexOf("Z");
                        if (timeZoneBigZIndex >= 0) {
                            String timeZoneString = ((String) data).substring(
                                timeZoneLittleZIndex,
                                units.lastIndexOf("Z") + 1);
                            TimeZone tz = null;
                            try {
                                tz = TimeZone.getTimeZone(timeZoneString);
                            } catch (RuntimeException e) {}
                            if (tz != null) {
                                recordCalendar.setTimeZone(tz);
                            }
                        }

                    }
                }
            }
            // Now check to see if it some information was found
            if (foundCalendarInformation) {
                // Now we have to make sure we feel like we got enough data
                // to make the calendar valid. For now, I will just assume
                // that if no hour was specified, that can't be good and so
                // invalid. Otherwise, I will use the current values
                if ((epochMilliseconds >= 0) || (dayInMonth >= 0)
                    || (dayInWeek > 0) || (dayInYear > 0)
                    || (dayOfWeekInMonth > 0)) {
                    resolvedDate = recordCalendar.getTime();
                }
            }
        }
        return resolvedDate;
    }

    /**
     * @see IDataAccess#getDataContainer()
     */
    public DataContainer getDataContainer() {
        return this.dataContainer;
    }

    /**
     * @see moos.ssds.data.ITimeIndexedDataAccess#getStartDate()
     */
    public Date getStartDate() {
        return this.startDate;
    }

    /**
     * @see moos.ssds.data.ITimeIndexedDataAccess#getEndDate()
     */
    public Date getEndDate() {
        return this.endDate;
    }

    /**
     * A private method to return the file parser used to parse the
     * IDataContainer
     * 
     * @return
     */
    private IParser getParser() {
        return this.parser;
    }

    /**
     * @see moos.ssds.data.IDataAccess#getRecordVariables()
     */
    public Collection getRecordVariables() {
        return new ArrayList(this.mapRecordVariablesToData.keySet());
    }

    /**
     * This is the <code>IDataContainer</code> that this data access object
     * uses to read data from
     */
    private final DataContainer dataContainer;

    /**
     * This is the inclusive start date of the data that will be accessible
     * through this object
     */
    private Date startDate;

    /**
     * This is the inclusive end date of the data that will be accessible
     * through this object
     */
    private Date endDate;

    /**
     * This is a collection of strings that list the names of the variables to
     * have data pulled for
     */
    private Collection recordVariableNames;

    /**
     * A boolean to indicate if the a variable was able to be found that
     * contains time/date information
     */
    private boolean datesResolved = false;

    /**
     * This is the order of the data in the data array that will give you time
     * sorted data.
     */
    private int[] sortOrder;

    /**
     * This is a List of the time objects that are associated with the
     * corresponding data objects
     */
    private List times = null;

    /**
     * This is a <code>Map</code> that holds the
     * <code>moos.ssds.model.IRecordVariable</code> as the key and an array of
     * <code>Number</code>s that are the data for that
     * <code>RecordVariable</code>
     */
    private final Map mapRecordVariablesToData = new HashMap();

    /**
     * This is the IFileParser that will be used to parse the contents of the
     * IDataContainer
     */
    private final IParser parser;

    // Certain patterns that will be used to search the units for
    // specific cases
    private Pattern date1 = Pattern.compile("M+[/|-]d+[/|-]y+");
    private Pattern date2 = Pattern.compile("d+[/|-]M+[/|-]y+");
    private Pattern time1 = Pattern.compile("hh:mm:ss");
    private Pattern time2 = Pattern.compile("KK:mm:ss");
    private Pattern time3 = Pattern.compile("HH:mm:ss");

    /**
     * A log4j logger
     */
    static Logger logger = Logger.getLogger(TimeIndexedFreeFormAccess.class);
}