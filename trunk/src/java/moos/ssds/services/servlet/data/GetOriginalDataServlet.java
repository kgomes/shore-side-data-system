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
package moos.ssds.services.servlet.data;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import moos.ssds.io.PacketInput;
import moos.ssds.io.PacketSQLInput;
import moos.ssds.services.data.SQLDataStreamRawDataAccessLocal;
import moos.ssds.services.data.SQLDataStreamRawDataAccessLocalHome;
import moos.ssds.services.data.SQLDataStreamRawDataAccessUtil;
import moos.ssds.transmogrify.SSDSDevicePacket;
import moos.ssds.transmogrify.SSDSGeoLocatedDevicePacket;
import moos.ssds.util.XmlDateFormat;

import org.apache.log4j.Logger;

/**
 * To provide access to original DataStream data from SSDS.
 * 
 * @author kgomes
 * @version 1.0
 * @web.servlet name="GetOriginalDataServlet" display-name="Get Original Data
 *              Servlet" load-on-startup="1"
 * @web.servlet-mapping url-pattern="/GetOriginalData/*"
 * @web.servlet-mapping url-pattern="*.GetOriginalData"
 * @web.servlet-mapping url-pattern="/GetOriginalDataServlet"
 */
public class GetOriginalDataServlet extends HttpServlet {

    /**
     * This is the implementation of the method to return some information about
     * what this particular servlet does
     */
    public String getServletInfo() {
        return "This servlet retrieves data from a database of "
            + "serialized packets and returns it to the user";
    }

    /**
     * This is the doPost method defined in the HTTPServlet. In this case, it
     * simply calls the doGet method passing the request response pair
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * This is the doGet method where the real stuff happens
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        // Setup some local variables to hold the parsed parameters
        String deviceIDParameter = request.getParameter("deviceID");
        String startParentIDParameter = request.getParameter("startParentID");
        String endParentIDParameter = request.getParameter("endParentID");
        String startPacketTypeParameter = request
            .getParameter("startPacketType");
        String endPacketTypeParameter = request.getParameter("endPacketType");
        String startPacketSubTypeParameter = request
            .getParameter("startPacketSubType");
        String endPacketSubTypeParameter = request
            .getParameter("endPacketSubType");
        String startDataDescriptionIDParameter = request
            .getParameter("startDataDescriptionID");
        String endDataDescriptionIDParameter = request
            .getParameter("endDataDescriptionID");
        String startDataDescriptionVersionParameter = request
            .getParameter("startDataDescriptionVersion");
        String endDataDescriptionVersionParameter = request
            .getParameter("endDataDescriptionVersion");
        String startTimestampSecondsParameter = request
            .getParameter("startTimestampSeconds");
        String endTimestampSecondsParameter = request
            .getParameter("endTimestampSeconds");
        String startTimestampNanosecondsParameter = request
            .getParameter("startTimestampNanoseconds");
        String endTimestampNanosecondsParameter = request
            .getParameter("endTimestampNanoseconds");
        String numHoursOffsetParameter = request.getParameter("numHoursOffset");
        String startSequenceNumberParameter = request
            .getParameter("startSequenceNumber");
        String endSequenceNumberParameter = request
            .getParameter("endSequenceNumber");
        String lastNumberOfPacketsParameter = request
            .getParameter("lastNumberOfPackets");
        String startLatitudeParameter = request.getParameter("startLatitude");
        String endLatitudeParameter = request.getParameter("endLatitude");
        String startLongitudeParameter = request.getParameter("startLongitude");
        String endLongitudeParameter = request.getParameter("endLongitude");
        String startDepthParameter = request.getParameter("startDepth");
        String endDepthParameter = request.getParameter("endDepth");

        // The return format parameters
        String orderByParameter = request.getParameter("orderBy");
        String displayPacketHeaderInfoParameter = request
            .getParameter("displayPacketHeaderInfo");
        boolean displayPacketHeaderInfo = false;
        String noHTMLHeaderParameter = request.getParameter("noHTMLHeader");
        if (noHTMLHeaderParameter == null)
            noHTMLHeaderParameter = request.getParameter("noHTMLHEader");
        boolean noHTMLHeader = false;

        String delimiterParameter = request.getParameter("delimiter");
        // Set a default
        if (delimiterParameter == null) {
            delimiterParameter = ",";
        }

        String recordDelimiterParameter = request
            .getParameter("recordDelimiter");
        // Set a default
        if (recordDelimiterParameter == null) {
            recordDelimiterParameter = "";
        }

        // These are legacy parameters
        String metadataID = request.getParameter("metadataID");
        String recordTypeID = request.getParameter("recordTypeID");
        String platformID = request.getParameter("platformID");
        String isiFlag = request.getParameter("isi");
        String fileName = request.getParameter("fileName");
        String numHoursBack = request.getParameter("numHoursBack");
        String startDateTime = request.getParameter("startDateTime");
        String endDateTime = request.getParameter("endDateTime");
        String outputAs = request.getParameter("outputAs");
        String prependWith = request.getParameter("prependWith");
        String convertTo = request.getParameter("convertTo"); // E.g. "Hex"
        String dsURL = request.getParameter("dsURL");
        String stride = request.getParameter("stride");

        // Now list the data query parameters we need
        Long deviceID = null;
        Long startParentID = null;
        Long endParentID = null;
        Integer startPacketType = null;
        Integer endPacketType = null;
        Long startPacketSubType = null;
        Long endPacketSubType = null;
        Long startDataDescriptionID = null;
        Long endDataDescriptionID = null;
        Long startDataDescriptionVersion = null;
        Long endDataDescriptionVersion = null;
        Long startTimestampSeconds = null;
        Long endTimestampSeconds = null;
        Long startTimestampNanoseconds = null;
        Long endTimestampNanoseconds = null;
        Long startSequenceNumber = null;
        Long endSequenceNumber = null;
        Long lastNumberOfPackets = null;
        Double startLatitude = null;
        Double endLatitude = null;
        Double startLongitude = null;
        Double endLongitude = null;
        Float startDepth = null;
        Float endDepth = null;
        String orderBy = null;
        int strideCount = 1;
        boolean returnAsSSDSDevicePackets = true;

        // Some variables for tracking
        boolean startDateTimeSpecified = false;
        boolean endDateTimeSpecified = false;

        // *******************************
        // Do any legacy mappings up front
        // *******************************

        // Number of hours back
        if ((numHoursOffsetParameter == null) && (numHoursBack != null)) {
            numHoursOffsetParameter = numHoursBack;
        }

        // Convert dsURL to other parameters
        if (dsURL != null) {
            Pattern pattern = Pattern
                .compile(".*/(\\d+)_(\\d+)_(\\d+)_(\\d+)$");
            Matcher matcher = pattern.matcher(dsURL);
            if (matcher.matches()) {
                // Now if the parameters were not defined
                if (deviceIDParameter == null)
                    deviceIDParameter = matcher.group(1);
                if (metadataID == null)
                    metadataID = matcher.group(2);
                if (recordTypeID == null)
                    recordTypeID = matcher.group(3);
                if (platformID == null)
                    platformID = matcher.group(4);
            }
        }

        // Convert the old isi Flag to the new
        if ((isiFlag != null) && (displayPacketHeaderInfoParameter == null)) {
            displayPacketHeaderInfoParameter = isiFlag;
        }
        if ((displayPacketHeaderInfoParameter != null)
            && (displayPacketHeaderInfoParameter.equals("1"))) {
            displayPacketHeaderInfoParameter = "true";
        }

        // Build the device ID
        try {
            deviceID = new Long(deviceIDParameter);
        } catch (NumberFormatException e1) {}

        // Check to make sure the device ID is specified
        if (deviceID == null) {
            response.setContentType("text/html");
            PrintWriter out = new PrintWriter(response.getOutputStream());
            String htmlErrorMessage = "deviceID was not specified, please "
                + "specify it in the query string";
            this.outputUsage(request.getRequestURL().toString(),
                htmlErrorMessage, out);
            out.flush();
            return;
        }

        // Build the correct packetSubTypes (which can be from recordTypes)
        // First just try to convert directly from the parameters
        if (startPacketSubTypeParameter != null) {
            try {
                startPacketSubType = new Long(startPacketSubTypeParameter);
            } catch (NumberFormatException e) {
                startPacketSubType = null;
            }
        }
        if (endPacketSubTypeParameter != null) {
            try {
                endPacketSubType = new Long(endPacketSubTypeParameter);
            } catch (NumberFormatException e) {
                endPacketSubType = null;
            }
        }
        // If the start type is still null, try to convert from the legacy
        // recordTypeID to the startPacketSubType
        if (startPacketSubType == null) {
            try {
                startPacketSubType = new Long(recordTypeID);
            } catch (NumberFormatException e) {
                startPacketSubType = null;
            }
        }

        // Grab the parent ID
        if (startParentIDParameter != null) {
            try {
                startParentID = new Long(startParentIDParameter);
            } catch (NumberFormatException e) {
                startParentID = null;
            }
        }
        if (endParentIDParameter != null) {
            try {
                endParentID = new Long(endParentIDParameter);
            } catch (NumberFormatException e) {
                endParentID = null;
            }
        }
        if (startParentID == null) {
            try {
                startParentID = new Long(platformID);
            } catch (NumberFormatException e) {
                startParentID = null;
            }
        }

        // Now for the data description versions
        if (startDataDescriptionVersionParameter != null) {
            try {
                startDataDescriptionVersion = new Long(
                    startDataDescriptionVersionParameter);
            } catch (NumberFormatException e) {
                startDataDescriptionVersion = null;
            }
        }
        if (endDataDescriptionVersionParameter != null) {
            try {
                endDataDescriptionVersion = new Long(
                    endDataDescriptionVersionParameter);
            } catch (NumberFormatException e) {
                endDataDescriptionVersion = null;
            }
        }
        if (startDataDescriptionVersion == null) {
            try {
                startDataDescriptionVersion = new Long(metadataID);
            } catch (NumberFormatException e) {
                startDataDescriptionVersion = null;
            }
        }

        // Build the dataDescriptionIDs
        if (startDataDescriptionIDParameter != null) {
            try {
                startDataDescriptionID = new Long(
                    startDataDescriptionIDParameter);
            } catch (NumberFormatException e) {
                startDataDescriptionID = null;
            }
        }
        if (endDataDescriptionIDParameter != null) {
            try {
                endDataDescriptionID = new Long(endDataDescriptionIDParameter);
            } catch (NumberFormatException e) {
                endDataDescriptionID = null;
            }
        }

        // Now build the correct start/end times based on the query parameters

        // For the start date, the parameters to check will be in this order
        // 1. startTimestampSecondsParameter (with
        // startTimestampNanosecondsParameter)
        // 2. startDateTime (convert either from YYYYMMDD.hhmmss or
        // XMLDateFormat)
        if (startTimestampSecondsParameter != null) {
            // First try to convert it to a long
            try {
                startTimestampSeconds = new Long(startTimestampSecondsParameter);
                startDateTimeSpecified = true;
            } catch (NumberFormatException e) {
                startTimestampSeconds = null;
                logger
                    .debug("Could not convert startTimestampSecondsParameter of "
                        + startTimestampSecondsParameter + " to a Long");
            }
        }
        if (startTimestampNanosecondsParameter != null) {
            try {
                startTimestampNanoseconds = new Long(
                    startTimestampNanosecondsParameter);
            } catch (NumberFormatException e) {
                startTimestampNanoseconds = null;
                logger
                    .debug("Could not convert startTimestampNanosecondsParameter of "
                        + startTimestampNanosecondsParameter + " to a Long");
            }
        }
        // If the time in seconds/nanoseconds was not parsed out, try to get it
        // from the startDateTime
        if ((startTimestampSeconds == null) && (startDateTime != null)) {

            // Now try to convert the string and assume that it is an
            // XMLDateFormat
            Date startDate = this.getDateFromParameter(startDateTime);

            if (startDate != null) {
                // Now set the times to use in query
                long startTimeInMillis = startDate.getTime();

                startTimestampSeconds = new Long(startTimeInMillis / 1000);
                startTimestampNanoseconds = new Long(
                    startTimeInMillis % 1000 * 1000);
                startDateTimeSpecified = true;
            } else {
                // If it is still null, set it to 0
                if (startTimestampSeconds == null)
                    startTimestampSeconds = new Long(0);
                if (startTimestampNanoseconds == null)
                    startTimestampNanoseconds = new Long(0);
                startDateTimeSpecified = true;
            }
        }

        // Now let's do the same for the end time
        if (endTimestampSecondsParameter != null) {
            // First try to convert it to a long
            try {
                endTimestampSeconds = new Long(endTimestampSecondsParameter);
                endDateTimeSpecified = true;
            } catch (NumberFormatException e) {
                endTimestampSeconds = null;
                logger
                    .debug("Could not convert endTimestampSecondsParameter of "
                        + endTimestampSecondsParameter + " to a Long");
            }
        }

        if (endTimestampNanosecondsParameter != null) {
            try {
                endTimestampNanoseconds = new Long(
                    endTimestampNanosecondsParameter);
            } catch (NumberFormatException e) {
                endTimestampNanoseconds = null;
                logger
                    .debug("Could not convert endTimestampNanosecondsParameter of "
                        + endTimestampNanosecondsParameter + " to a Long");
            }
        }
        // If the time in seconds/nanoseconds was not parsed out, try to get it
        // from the endDateTime
        if ((endTimestampSeconds == null) && (endDateTime != null)) {
            // Now try to convert the string and assume that it is an
            // XMLDateFormat
            Date endDate = this.getDateFromParameter(endDateTime);

            if (endDate == null) {
                // Create some calendar/date objects for use
                Calendar currentCalendar = Calendar.getInstance(); // LocalTime
                // Create a GMT Timezone and set it on the calendars
                TimeZone tz = TimeZone.getTimeZone("GMT");
                currentCalendar.setTimeZone(tz);

                endDate = currentCalendar.getTime();
            }
            // Now set the times to use in query
            long endTimeInMillis = endDate.getTime();
            endTimestampSeconds = new Long(endTimeInMillis / 1000);
            endTimestampNanoseconds = new Long(endTimeInMillis % 1000 * 1000);
            endDateTimeSpecified = true;
        }

        // If neither date time was specified, assume that the user wants and
        // endtime of now
        if (!endDateTimeSpecified && !startDateTimeSpecified) {
            // Create some calendar/date objects for use
            Calendar currentCalendar = Calendar.getInstance(); // LocalTime
            // Create a GMT Timezone and set it on the calendars
            TimeZone tz = TimeZone.getTimeZone("GMT");
            currentCalendar.setTimeZone(tz);
            Date endDate = currentCalendar.getTime();
            long endTimeInMillis = endDate.getTime();
            endTimestampSeconds = new Long(endTimeInMillis / 1000);
            endTimestampNanoseconds = new Long(endTimeInMillis % 1000 * 1000);
            endDateTimeSpecified = true;
        }

        // Now if only one of the start/end times were specified in the
        // parameter list, use the numHoursOffset parameter to figure out the
        // non-specified time
        if ((startDateTimeSpecified && !endDateTimeSpecified)
            || (endDateTimeSpecified && !startDateTimeSpecified)) {

            // First convert parameter to number
            Long numHoursOffset = null;
            try {
                numHoursOffset = new Long(numHoursOffsetParameter);
            } catch (NumberFormatException e) {
                numHoursOffset = null;
            }
            long numberOfSecondsOffset = 0;
            if (numHoursOffset != null) {
                // First convert number of hours to seconds
                numberOfSecondsOffset = numHoursOffset.longValue() * 60 * 60;
            }
            // If the start date was specified
            if (startDateTimeSpecified) {
                // If the number of hours was specified, offset by that
                if (numberOfSecondsOffset > 0) {
                    // Add that to the start time
                    endTimestampSeconds = new Long(startTimestampSeconds
                        .longValue()
                        + numberOfSecondsOffset);
                } else {
                    // Create some calendar/date objects for use
                    Calendar currentCalendar = Calendar.getInstance(); // LocalTime
                    // Create a GMT Timezone and set it on the calendars
                    TimeZone tz = TimeZone.getTimeZone("GMT");
                    currentCalendar.setTimeZone(tz);
                    Date endDate = currentCalendar.getTime();
                    long endTimeInMillis = endDate.getTime();
                    endTimestampSeconds = new Long(endTimeInMillis / 1000);
                    endTimestampNanoseconds = new Long(
                        endTimeInMillis % 1000 * 1000);
                    endDateTimeSpecified = true;
                }
            } else if (endDateTimeSpecified) {
                if (numberOfSecondsOffset > 0) {
                    startTimestampSeconds = new Long(endTimestampSeconds
                        .longValue()
                        - numberOfSecondsOffset);
                } else {
                    // Start at the beginning
                    startTimestampSeconds = new Long(0);
                    startTimestampNanoseconds = new Long(0);
                    startDateTimeSpecified = true;
                }
            }
        }

        if (lastNumberOfPacketsParameter != null) {
            try {
                lastNumberOfPackets = new Long(lastNumberOfPacketsParameter);
            } catch (NumberFormatException e) {
                lastNumberOfPackets = null;
                logger.error("Could not convert "
                    + lastNumberOfPacketsParameter
                    + " to Long and set to lastNumberOfPackets");
            }
        }
        
        // Convert stride to an integer count for number of packets to skip
        if (stride != null) {
            strideCount = Integer.parseInt(stride);
        }
        else {
        	strideCount = 1;
        }
        
        // Convert the displayHeader to boolean
        displayPacketHeaderInfo = Boolean
            .parseBoolean(displayPacketHeaderInfoParameter);
        // Convert the noHTMLHeader to a boolean
        if ((noHTMLHeaderParameter != null)
            && ((noHTMLHeaderParameter.equals("1")) || (noHTMLHeaderParameter
                .equalsIgnoreCase("true"))))
            noHTMLHeader = true;

        // Grab the local interface to the SQLDataStreamRawDataAccessEJB
        SQLDataStreamRawDataAccessLocalHome sqlDSLocalHome = null;
        try {
            sqlDSLocalHome = SQLDataStreamRawDataAccessUtil.getLocalHome();
        } catch (NamingException e) {
            logger.error("Naming Exception caught: " + e.getMessage());
            response.setContentType("text/html");
            PrintWriter out = new PrintWriter(response.getOutputStream());
            out.println("<H1>NamingException caught: " + e.getMessage()
                + "</H1><br>");
            out.flush();
            return;
        }
        SQLDataStreamRawDataAccessLocal sqlDSLocal = null;
        if (sqlDSLocalHome != null) {
            try {
                sqlDSLocal = sqlDSLocalHome.create();
            } catch (CreateException e) {
                logger.error("CreateException caught: " + e.getMessage());
                response.setContentType("text/html");
                PrintWriter out = new PrintWriter(response.getOutputStream());
                out.println("<H1>CreateException caught: " + e.getMessage()
                    + "</H1><br>");
                out.flush();
                return;
            }
        }

        // Now call the method
        TreeMap dataMap = null;
        try {
            dataMap = sqlDSLocal.getSortedRawData(deviceID, startParentID,
                endParentID, startPacketType, endPacketType,
                startPacketSubType, endPacketSubType, startDataDescriptionID,
                endDataDescriptionID, startDataDescriptionVersion,
                endDataDescriptionVersion, startTimestampSeconds,
                endTimestampSeconds, startTimestampNanoseconds,
                endTimestampNanoseconds, startSequenceNumber,
                endSequenceNumber, lastNumberOfPackets, startLatitude,
                endLatitude, startLongitude, endLongitude, startDepth,
                endDepth, orderBy, returnAsSSDSDevicePackets);
        } catch (SQLException e) {
            logger.error("SQLException caught: " + e.getMessage());
            response.setContentType("text/html");
            PrintWriter out = new PrintWriter(response.getOutputStream());
            out.println("<H1>SQLException caught: " + e.getMessage()
                + "</H1><br>");
            out.flush();
            return;
        }

        // Set time in past to prevent browser from caching doc
        long currentTime = System.currentTimeMillis();
        long tenMinutes = 10 * 60 * 1000;
        response.setDateHeader("Expires", currentTime - tenMinutes);

        // Now depending on format specified, return the data in that format
        if ((outputAs != null) && (outputAs.equalsIgnoreCase(OUTPUT_AS_BINARY))) {
            OutputStream outStream = response.getOutputStream();
            respondBinary(dataMap, outStream, response,
                recordDelimiterParameter, strideCount);
        } else {
            PrintWriter out = new PrintWriter(response.getOutputStream());
            respondAscii(dataMap, out, displayPacketHeaderInfo, noHTMLHeader,
                delimiterParameter, recordDelimiterParameter, prependWith,
                convertTo, response, strideCount);
        }
    }

    /**
     * This method takes in a <code>String</code> parameter that can be either
     * XML or legacy (YYYYMMDD.hhmmss) and converts it to a <code>Date</code>
     * object. If the conversion can't take place, null is returned.
     * 
     * @param dateParameter
     *            is the <code>String</code> that represents the
     *            <code>Date</code>
     * @return a <code>Date</code> that is constructed from the
     *         <code>String</code> (if possible). If no conversion was
     *         possible, null is returned.
     */
    private Date getDateFromParameter(String dateParameter) {

        // The Date to return
        Date dateToReturn = null;

        // Try parsing with XML first
        dateToReturn = xmlDateFormat.parse(dateParameter);

        // If not, try to parse the legacy format
        if (dateToReturn == null) {
            // Grab a calendar object
            Calendar calendar = Calendar.getInstance(); // LocalTime
            // Set to GMT
            TimeZone tz = TimeZone.getTimeZone("GMT");
            calendar.setTimeZone(tz);

            try {
                calendar.set(Calendar.YEAR, Integer.parseInt(dateParameter
                    .substring(0, 4)));
                calendar.set(Calendar.MONTH, Integer.parseInt(dateParameter
                    .substring(4, 6)) - 1);
                calendar.set(Calendar.DAY_OF_MONTH, Integer
                    .parseInt(dateParameter.substring(6, 8)));
                calendar.set(Calendar.HOUR_OF_DAY, Integer
                    .parseInt(dateParameter.substring(9, 11)));
                calendar.set(Calendar.MINUTE, Integer.parseInt(dateParameter
                    .substring(11, 13)));
                calendar.set(Calendar.SECOND, Integer.parseInt(dateParameter
                    .substring(13, 15)));
                dateToReturn = calendar.getTime();
            } catch (Exception ex) {
                dateToReturn = null;
            }
        }

        // Now return it
        return dateToReturn;
    }

    /**
     * The method to respond to the request and convert the data buffers to
     * ASCII
     * 
     * @param request
     * @param response
     * @param out
     * @param DEBUG
     */
    private void respondAscii(TreeMap dataMap, PrintWriter out,
        boolean displayPacketHeaderInfo, boolean noHTMLHeader,
        String delimiter, String recordDelimiter, String prependWith,
        String convertTo, HttpServletResponse response, int strideCount) {

        // If HTML is requested print out the header
        if (!noHTMLHeader) {
            response.setContentType("text/html");
            out.println("<HTML>");
            out.println("<HEAD><TITLE>GetOriginalDataServlet Response</TITLE>");
            out.println("</HEAD>");
            out.println("<BODY>");
            out.println("<xmp>");
            if (displayPacketHeaderInfo) {
                out.println("Device/SSDS ID" + delimiter + "Timestamp (GMT)"
                    + delimiter + "SIAM Packet Timestamp" + delimiter
                    + "databuffer");
            } else {
                out.println("databuffer");
            }
        }
        // Now iterate over the data packets
        // Now grab the key set and loop through it to print out rows
        // The data packets are mapped by key which may be a timestamp or sequence number.
        Set keySet = dataMap.keySet();
        Iterator i = keySet.iterator();
        int counter = 0;
        while (i.hasNext()) {
        	
        	// Skip records using a remainder test on the stride specified in the request
        	// Make sure to increment the iterator!
        	counter++;
        	Long key = (Long) i.next();
        	//out.println("counter = " + counter + " strideCount = " + strideCount + " mod div = " + (counter % strideCount));
            if (counter % strideCount != 0) {
            	//out.println(" 1 continuing.");
            	continue;
            }
            
            // Grab the collection
            Collection values = (Collection) dataMap.get(key);
            // Now loop over the values for the key - these values may consist of multiple RecordTypes 
            // that may exist at each timestamp or sequence number.
            if (values != null) {
                Iterator iterator = values.iterator();
                while (iterator.hasNext()) {
                	
                    SSDSGeoLocatedDevicePacket ssdsDP = (SSDSGeoLocatedDevicePacket) iterator
                        .next();
                    Date dat = new Date(ssdsDP.systemTime());
                    // Now depending on the parameters requested, print out any
                    // leading
                    // stuff
                    if (displayPacketHeaderInfo) {
                        out.print(ssdsDP.sourceID() + delimiter
                            + dat.toGMTString() + delimiter
                            + ssdsDP.systemTime() + delimiter);
                    }
                    if ((prependWith != null)
                        && (prependWith.equalsIgnoreCase(PREPEND_WITH_OASIS))) {
                        // Use Calendar object to get proper time format
                        GregorianCalendar cal = new GregorianCalendar();
                        TimeZone tz = TimeZone.getTimeZone("GMT");
                        cal.setTimeZone(tz);
                        cal.setTime(dat);
                        int yd = cal.get(Calendar.DAY_OF_YEAR);
                        int hr = cal.get(Calendar.HOUR_OF_DAY);
                        int mn = cal.get(Calendar.MINUTE);
                        int sc = cal.get(Calendar.SECOND);
                        double fracD = (((float) sc / 60.0 + (float) mn) / 60.0 + (float) hr) / 24.0;
                        float fracYD = (float) ((float) yd + fracD);
                        // Format for trailing zeros
                        NumberFormat nf = NumberFormat.getInstance();
                        nf.setMaximumIntegerDigits(3);
                        nf.setMaximumFractionDigits(5);
                        nf.setMinimumIntegerDigits(1);
                        nf.setMinimumFractionDigits(5);
                        out.print(nf.format(fracYD) + " ");
                    }
                    if ((convertTo != null)
                        && (convertTo.equalsIgnoreCase(CONVERT_TO_HEX))) {
                        StringBuffer hexData = new StringBuffer();
                        ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(
                            ssdsDP.getDataBuffer());
                        while (byteArrayIS.available() > 0) {
                            hexData.append(Integer.toHexString(
                                (0xFF & byteArrayIS.read()) | 0x100).substring(
                                1));
                        }
                        if (recordDelimiter != null) {
                            out.println(hexData.toString().toUpperCase()
                                + recordDelimiter);
                        } else {
                            out.println(hexData.toString().toUpperCase());
                        }
                    } else {
                        if (recordDelimiter != null) {
                            out.println(new String(ssdsDP.getDataBuffer())
                                + recordDelimiter);
                        } else {
                            out.println(new String(ssdsDP.getDataBuffer()));
                        }
                    }
                }
            }
        }
        if (!noHTMLHeader) {
            out.println("</xmp></BODY></HTML>");
        }
        out.flush();
        return;
    }

    private void respondBinary(TreeMap dataMap, OutputStream out,
        HttpServletResponse response, String recordDelimiter, int strideCount) {

        // Set the response header
        response.setContentType("binary/octet-stream");
        // Now iterate over the data packets
        // Now grab the key set and loop through it to print out rows
     // The data packets are mapped by key which may be a timestamp or sequence number.
        Set keySet = dataMap.keySet();
        Iterator i = keySet.iterator();
        int counter = 0;
        while (i.hasNext()) {
        	
        	// Skip records using a remainder test on the stride specified in the request
        	// Make sure to increment the iterator!
        	counter++;
        	Long key = (Long) i.next();
        	if (counter % strideCount != 0) {
            	continue;
            }
        	
        	// These values may consist of multiple RecordTypes 
            // that may exist at each timestamp or sequence number.
            Collection values = (Collection) dataMap.get(key);
            if (values != null) {
                Iterator iterator = values.iterator();
                while (iterator.hasNext()) {
                    SSDSGeoLocatedDevicePacket ssdsDP = (SSDSGeoLocatedDevicePacket) iterator
                        .next();
                    try {
                        if (recordDelimiter != null) {
                            out.write(ssdsDP.getDataBuffer());
                            out.write(recordDelimiter.getBytes());
                        } else {
                            out.write(ssdsDP.getDataBuffer());
                        }
                    } catch (IOException e) {
                        logger
                            .error("IOException caught while writing back binary requested data: "
                                + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * This method writes out a usage type message to the print writer (the HTTP
     * Response) in case something is not specified correctly in the calling
     * URL.
     * 
     * @param out
     *            The PrintWriter to send the message to
     */
    public void outputUsage(String baseURLPath, String htmlErrorMessage,
        PrintWriter out) {

        // Try to grab the base URL from the ServletContext
        String path = baseURLPath;

        // The String Buffer to build the message
        StringBuffer usageMessageBuffer = new StringBuffer();
        // Write the header HTML
        usageMessageBuffer.append("<H2>GetOriginalDataServlet:</H2>\n");
        if (htmlErrorMessage != null) {
            usageMessageBuffer.append("<h3>Message: <font color=\"red\">"
                + htmlErrorMessage + "</font></h3>");
        }
        usageMessageBuffer
            .append("<H3>This servlet returns the raw device data (duplicate packets removed) ");
        usageMessageBuffer.append("that meet your query parameters</H3>\n");
        usageMessageBuffer.append("</H3>\n");
        usageMessageBuffer.append("<PRE>\n");
        usageMessageBuffer.append(path + "\n");
        usageMessageBuffer.append("    ?deviceID=&lt;DVID&gt;\n");
        usageMessageBuffer
            .append("    &startDataDescriptionVersion=&lt;SDDV&gt;\n");
        usageMessageBuffer
            .append("    &endDataDescriptionVersion=&lt;EDDV&gt;\n");
        usageMessageBuffer.append("    &startPacketSubType=&lt;SPST&gt;\n");
        usageMessageBuffer.append("    &endPacketSubType=&lt;EPST&gt;\n");
        usageMessageBuffer.append("    &startParentID=&lt;SPID&gt;\n");
        usageMessageBuffer.append("    &endParentID=&lt;EPID&gt;\n");
        usageMessageBuffer.append("    &startTimestampSeconds=&lt;STS&gt;\n");
        usageMessageBuffer.append("    &endTimestampSeconds=&lt;ETS&gt;\n");
        usageMessageBuffer.append("    &lastNumberOfPackets=&lt;LNP&gt;\n");
        usageMessageBuffer.append("    &numHoursOffset=&lt;NHO&gt;\n");
        usageMessageBuffer.append("    &outputAs=&lt;" + OUTPUT_AS_BINARY
            + "&gt;\n");
        usageMessageBuffer
            .append("    &displayPacketHeaderInfo=&lt;true|false&gt;\n");
        usageMessageBuffer.append("    &noHTMLHeader=&lt;1|0|true|false&gt;\n");
        usageMessageBuffer.append("    &delimiter=&lt;DELIM&gt;\n");
        usageMessageBuffer.append("    &recordDelimiter=&lt;RECDEL&gt;\n");
        usageMessageBuffer.append("    &prependWith=&lt;" + PREPEND_WITH_OASIS
            + "&gt;\n");
        usageMessageBuffer.append("    &convertTo=&lt;" + CONVERT_TO_HEX
            + "&gt;\n");
        usageMessageBuffer.append("    &stride=&lt;COUNT&gt;\n");
        usageMessageBuffer.append("</PRE>\n");
        usageMessageBuffer.append("<h3>Parameters:</h3><br/>");
        usageMessageBuffer
            .append("<table border=\"2\" cellpadding=\"1\" cellspacing=\"1\">");
        usageMessageBuffer.append("<tr><th>Parameter Name</th>"
            + "<th>Opt/Required</th>" + "<th>Query or<br>Return Format</th>"
            + "<th>Available Options</th>" + "<th>Description</th></tr>");
        usageMessageBuffer
            .append("<tr><td>deviceID</td>"
                + "<td><b>required</b></td>"
                + "<td>query</td>"
                + "<td>any number</td>"
                + "<td>This is the SSDS ID of the device that you would like the data "
                + "for</td></tr>");
        usageMessageBuffer
            .append("<tr><td>startDataDescriptionVersion</td>"
                + "<td>optional</td>"
                + "<td>query</td>"
                + "<td>any number</td>"
                + "<td>This is the number that will be used as the starting query number "
                + "for the dataDescriptionVersion number to search for.  This is "
                + "also known as the metadata version (or reference) number.</td></tr>");
        usageMessageBuffer
            .append("<tr><td>endDataDescriptionVersion</td>"
                + "<td>optional</td>"
                + "<td>query</td>"
                + "<td>any number</td>"
                + "<td>This is the number that will be used as the ending query number "
                + "for the dataDescriptionVersion number to search for.  If this is not "
                + "specified, but the startDataDescriptionVersion is, only the "
                + "startDataDescriptionVersion will be searched for.  This is "
                + "also known as the metadata version (or reference) number.</td></tr>");
        usageMessageBuffer
            .append("<tr><td>startPacketSubType</td>"
                + "<td>optional</td>"
                + "<td>query</td>"
                + "<td>any number</td>"
                + "<td>This is the starting number for the &quot;type&quot; of packet to "
                + "retrieve from the device.  If a device generates multiple streams "
                + "of data, these are usually broken up and uniquely identified by"
                + " a packet sub type (also known as record type).  This is the "
                + "starting record type.</td></tr>");
        usageMessageBuffer
            .append("<tr><td>endPacketSubType</td>"
                + "<td>optional</td>"
                + "<td>query</td>"
                + "<td>any number</td>"
                + "<td>This is the ending number for the &quot;type&quot; of packet to "
                + "retrieve from the device.  If a device generates multiple streams "
                + "of data, these are usually broken up and uniquely identified by"
                + " a packet sub type (also known as record type).  This is the "
                + "end record type.  If this is not specified, but the "
                + "startPacketSubType is, only the startPacketSubType will be "
                + "queried for.</td></tr>");
        usageMessageBuffer
            .append("<tr><td>startParentID</td>"
                + "<td>optional</td>"
                + "<td>query</td>"
                + "<td>any number</td>"
                + "<td>This is the SSDS ID of the starting parent to search for.  In other"
                + "words, the query will look for all packets from the device specified "
                + "while it was deployed on parents starting at ID of startingParentID."
                + " This was also known as platform ID.</td></tr>");
        usageMessageBuffer
            .append("<tr><td>endParentID</td>"
                + "<td>optional</td>"
                + "<td>query</td>"
                + "<td>any number</td>"
                + "<td>This is the SSDS ID of the ending parent to search for.  In other"
                + "words, the query will look for all packets from the device specified "
                + "while it was deployed on parents starting at ID of startingParentID "
                + "and ending with parent with endParentID.  If this is not specified and "
                + "the startParentID is, only the startParentID will be searched for. "
                + "ParentID is also known as platform ID.</td></tr>");
        usageMessageBuffer
            .append("<tr><td>startTimestampSeconds</td>"
                + "<td>optional</td>"
                + "<td>query</td>"
                + "<td>Any Number</td>"
                + "<td>These are epoch seconds (number of seconds since January 1, 1970) "
                + "of the start of the time window of data you are looking for</td></tr>");
        usageMessageBuffer
            .append("<tr><td>endTimestampSeconds</td>"
                + "<td>optional</td>"
                + "<td>query</td>"
                + "<td>Any Number</td>"
                + "<td>These are epoch seconds (number of seconds since January 1, 1970) "
                + "of the end of the time window of data you are looking for</td></tr>");
        usageMessageBuffer
            .append("<tr><td>numHoursOffset</td>"
                + "<td>optional</td>"
                + "<td>query</td>"
                + "<td>Any Number</td>"
                + "<td>This is the number of hours wide that the time query window should be.  "
                + "This is only applicable when one of either start or end times have been specified. "
                + "If the start time is specified, this offset is added to the start time to "
                + "get the end time.  If the end time is specified, it is subtracted from the "
                + "end time to get the start time.</td></tr>");
        usageMessageBuffer
            .append("<tr><td>lastNumberOfPackets</td>"
                + "<td>optional</td>"
                + "<td>query</td>"
                + "<td>Any Number</td>"
                + "<td>This is the number of packets that will be returned from the end of the "
                + "query that is defined.  If not parameters but the deviceID are defined, then it "
                + "will return the most recent N number of packets (where N is the number you specify</td></tr>");
        usageMessageBuffer
            .append("<tr><td>outputAs</td>"
                + "<td>optional</td>"
                + "<td>return<br>format</td>"
                + "<td><b>"
                + OUTPUT_AS_BINARY
                + "</b></td>"
                + "<td>This is a flag that indicates if the data should be returned in "
                + "binary format.  In order for this to happen, it must be set"
                + "to <b>" + CONVERT_TO_HEX
                + "</b>.  Otherwise, the return will"
                + " be in ASCII</td></tr>");
        usageMessageBuffer.append("<tr><td>displayPacketHeaderInfo</td>"
            + "<td>optional</td>" + "<td>return<br>format</td>"
            + "<td>true|false</td>"
            + "<td>This is the flag that turns on the printout of the packet "
            + "header information before each packet.</td></tr>");
        usageMessageBuffer
            .append("<tr><td>noHTMLHeader</td>"
                + "<td>optional</td>"
                + "<td>return<br>format</td>"
                + "<td>1|0|true|false</td>"
                + "<td>If this value is set to <b>true</b> or <b>1</b>, the return "
                + "from this URL call will not be wrapped with HTML tags and "
                + "no content-type will be specified.  Otherwise the content-type "
                + "will be text/html and there will be HTML tags around the results.</td></tr>");
        usageMessageBuffer
            .append("<tr><td>delimiter</td>"
                + "<td>optional</td>"
                + "<td>return<br>format</td>"
                + "<td>any text</td>"
                + "<td>This is the text that will be used to separate the packet "
                + "header and data columns of the return.  If this is not specified "
                + "a comma will be used as a default.</td></tr>");
        usageMessageBuffer
            .append("<tr><td>recordDelimiter</td>"
                + "<td>optional</td>"
                + "<td>return<br>format</td>"
                + "<td>any text</td>"
                + "<td>This is the text that will be used to separate the packet "
                + "records of the return.  This is useful if the payload contents "
                + "contain normal line feed characters.  If this is not specified "
                + "then nothing is added to the return to separate the records.</td></tr>");
        usageMessageBuffer
            .append("<tr><td>prependWith</td>"
                + "<td>optional</td>"
                + "<td>return<br>format</td>"
                + "<td><b>"
                + PREPEND_WITH_OASIS
                + "</b></td>"
                + "<td>This is a flag that indicates what is to prepend each returned"
                + " packet.  There is currently only one option, which is <b>"
                + PREPEND_WITH_OASIS
                + "</b> which prepends each record with an OASIS style"
                + " timestamp</td></tr>");
        usageMessageBuffer
            .append("<tr><td>convertTo</td>"
                + "<td>optional</td>"
                + "<td>return<br>format</td>"
                + "<td><b>"
                + CONVERT_TO_HEX
                + "</b></td>"
                + "<td>This is a flag that indicates what the data buffer is to be converted to "
                + "(if any).  Currently only the <b>" + CONVERT_TO_HEX
                + "</b> option does any conversions.</td></tr>");
        usageMessageBuffer
        .append("<tr><td>stride</td>"
            + "<td>optional</td>"
            + "<td>return<br>format</td>"
            + "<td>"
            + "Any number greater &gt; 0"
            + "</td>"
            + "<td>This paramater may be used to subsample the input DataStream by skipping " 
            + "packets in the returned data.  The default value is 1: return all packets.</td></tr>");
        usageMessageBuffer
            .append("<tr><th colspan=\"5\">For legacy reasons, the following are also "
                + "supported</th></tr>");
        usageMessageBuffer
            .append("<tr><td>dsURL</td>"
                + "<td>see description</td>"
                + "<td>query</td>"
                + "<td>String form of URL</td>"
                + "<td>This parameter is the URL that points to a file of serialized "
                + "packets.  This would only be used if the deviceID parameter"
                + "was not used (see above).  So this is required if there is no"
                + "deviceID, but will be ignored if deviceID is specified</td></tr>");
        usageMessageBuffer
            .append("<tr><td>metadataID</td>"
                + "<td colspan=\"4\"><center><i>same as startDataDescriptionVersion "
                + "above</i></center></td></tr>");
        usageMessageBuffer.append("<tr><td>recordType</td>"
            + "<td colspan=\"4\"><center><i>same as startPacketSubType "
            + "above</i></center></td></tr>");
        usageMessageBuffer.append("<tr><td>platformID</td>"
            + "<td colspan=\"4\"><center><i>same as startParentID "
            + "above</i></center></td></tr>");
        usageMessageBuffer
            .append("<tr><td>startDateTime</td>"
                + "<td>optional</td>"
                + "<td>query</td>"
                + "<td>Date/Time</td>"
                + "<td>This is the string representation of the date and time of the "
                + "start of the time window over which you are requesting data.  "
                + "It can be in one of two formats:"
                + "<ol><li>YYYYMMDD.hhmmss</li>"
                + "<li>YYYY-MM-DDTHH:MM:SSZ</li></ol></td></tr>");
        usageMessageBuffer
            .append("<tr><td>endDateTime</td>"
                + "<td>optional</td>"
                + "<td>query</td>"
                + "<td>Date/Time</td>"
                + "<td>This is the string representation of the date and time of the "
                + "end of the time window over which you are requesting data.  "
                + "It can be in one of two formats:"
                + "<ol><li>YYYYMMDD.hhmmss</li>"
                + "<li>YYYY-MM-DDTHH:MM:SSZ</li></ol></td></tr>");
        usageMessageBuffer
            .append("<tr><td>isi</td>"
                + "<td>optional</td>"
                + "<td>return<br>format</td>"
                + "<td>1|0|true|false</td>"
                + "<td>This is the flag that turns on the printout of the SIAM packet "
                + "header information before each packet.</td></tr>");
        usageMessageBuffer.append("<tr><td>numHoursBack</td>"
            + "<td colspan=\"4\"><center><i>same as numHoursOffset "
            + "above</i></center></td></tr>");
        usageMessageBuffer.append("</table>");
        usageMessageBuffer.append("<h3>Examples:</h3><br/>");
        usageMessageBuffer.append("<pre>");
        usageMessageBuffer.append(path
            + "?deviceID=1267&metadataID=0&recordTypeID=1"
            + "&platformID=1299&isi=0&numHoursBack=24&convertTo=hex"
            + "&noHTMLHeader=1&prependWith=oasis\n");
        usageMessageBuffer.append("</pre>");
        usageMessageBuffer.append("To use in a Perl program:\n\n");
        usageMessageBuffer.append("<pre>");
        usageMessageBuffer.append(" use LWP::Simple;\n\n");
        usageMessageBuffer
            .append(" $url = \""
                + path
                + "?deviceID=1294&metadataID=0&recordTypeID=1"
                + "&platformID=1295&isi=0&numHoursBack=24&convertTo=hex&noHTMLHeader=1"
                + "&prependWith=oasis\";\n");
        usageMessageBuffer.append(" $data = get($url);\n");
        usageMessageBuffer.append(" print $data;\n");
        usageMessageBuffer.append("</pre>");
        // Send to the response PrintWriter
        out.print(usageMessageBuffer.toString());
    }

    /**
     * A utility for helping with converting dates back and forth
     */
    XmlDateFormat xmlDateFormat = new XmlDateFormat();

    /** Some private constants */
    private static String OUTPUT_AS_BINARY = "binary";
    private static String CONVERT_TO_HEX = "hex";
    private static String PREPEND_WITH_OASIS = "oasis";

    /** A log4j logger */
    static Logger logger = Logger.getLogger(GetOriginalDataServlet.class);
}
