<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.util.regex.*"%>
<%@ page import="java.text.*"%>
<%@ page import="java.lang.Math"%>
<%@ page import="moos.ssds.io.*"%>
<%@ page import="moos.ssds.io.util.*"%>
<%@ page import="moos.ssds.metadata.*"%>
<%@ page import="moos.ssds.services.metadata.*"%>
<%@ page import="moos.ssds.services.data.*"%>
<%@ page import="moos.ssds.transmogrify.*"%>
<%@ page import="java.net.*"%>
<%@ page import="org.jfree.data.*"%>
<%@ page import="org.jfree.data.xy.*"%>
<%@ page import="org.jfree.chart.*"%>
<%@ page import="org.jfree.chart.axis.*"%>
<%@ page import="org.jfree.chart.renderer.*"%>
<%@ page import="org.jfree.chart.renderer.xy.*"%>
<%@ page import="org.jfree.chart.plot.*"%>
<%@ page import="org.jfree.chart.entity.*"%>
<%@ page import="org.jfree.chart.servlet.*"%>
<?xml version="1.0"?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
  Copyright 2009 MBARI
	
  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1 
  (the "License"); you may not use this file except in compliance 
  with the License. You may obtain a copy of the License at

  http://www.gnu.org/copyleft/lesser.html

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title>Access SIAM Raw Data, Step 2</title>
        <script type="text/javascript" src="/ssds/theme/com/sun/rave/web/ui/defaulttheme/javascript/formElements.js"></script>
        <link rel="stylesheet" type="text/css" href="/ssds/theme/com/sun/rave/web/ui/defaulttheme/css/css_master.css" />        
        <link rel="stylesheet" type="text/css" href="resources/ssds_stylesheet.css"></link>        
    </head>
    <body id="body" style="-rave-layout: grid">
        <!--begin whole page table-->
        <div class="clear"/>
<%// First grab the query variables
            // First grab the device ID that was entered
            String deviceIDEntered = request.getParameter("deviceIDEntered");
            String deviceID = request.getParameter("deviceID");
            if ((deviceIDEntered != null) && (deviceIDEntered.compareToIgnoreCase("") != 0)) {
                deviceID = deviceIDEntered;
            }

            // Now grab the way the user wants the results sorted by (timestamp of sequence number)
            String orderBy = request.getParameter("tsORseq");
            if ((orderBy == null) || (orderBy.equals(""))) {
                orderBy = "timestamp";
            }
            boolean orderByTimeBoolean = true;
            if (orderBy.compareTo("sequence") == 0) {
                orderByTimeBoolean = false;
            }
            // Grab the user input for whether or not they want the timetamp displayed
            String displayTimestamp = request.getParameter("displayTimestamp");
            boolean displayTimestampBoolean = false;
            if ((displayTimestamp != null) && (displayTimestamp.compareTo("true") == 0)) {
                displayTimestampBoolean = true;
            }
            // Grab the user input for whether or not they want the timetamp displayed as date/time
            String showDate = request.getParameter("showDate");
            boolean showDateBoolean = false;
            if ((showDate != null) && (showDate.compareTo("true") == 0)) {
                showDateBoolean = true;
            }
            // Grab the user input for whether or not they want the sequence number
            String displaySequenceNumber = request.getParameter("displaySequenceNumber");
            boolean displaySequenceNumberBoolean = false;
            if ((displaySequenceNumber != null) && (displaySequenceNumber.compareTo("true") == 0)) {
                displaySequenceNumberBoolean = true;
            }
            // Grab the date time format they chose
            String timezone = request.getParameter("timezone");
            if ((timezone == null) || (timezone.compareTo("") == 0)) {
                timezone = "gmt";
            }
            String dateTimeConversionFormat = request.getParameter("dateTimeConversionFormat");
            // Now grab the user input for whether or not they want the parent ID displayed
            String displayParentIDString = request.getParameter("displayParentID");
            boolean displayParentID = false;
            if ((displayParentIDString != null) && (displayParentIDString.compareTo("true") == 0)) {
                displayParentID = true;
            }
            // Now grab the user input for whether or not they want the RecordType displayed
            String displayRecordTypeString = request.getParameter("displayRecordType");
            boolean displayRecordType = false;
            if ((displayRecordTypeString != null) && (displayRecordTypeString.compareTo("true") == 0)) {
                displayRecordType = true;
            }
            // Now grab the user input for whether or not they want the PacketType displayed
            String displayPacketTypeString = request.getParameter("displayPacketType");
            boolean displayPacketType = false;
            if ((displayPacketTypeString != null) && (displayPacketTypeString.compareTo("true") == 0)) {
                displayPacketType = true;
            }
            // Now grab the user input for whether or not they want primary data buffer displayed
            String displayDataBufferString = request.getParameter("displayDataBuffer");
            boolean displayDataBuffer = true;
            if ((displayDataBufferString != null) && (displayDataBufferString.compareTo("false") == 0)) {
                displayDataBuffer = false;
            }
            // Now grab the user input for whether or not they want the data buffer size displayed
            String showDataBufferSize = request.getParameter("showDataBufferSize");
            boolean showDataBufferSizeBoolean = false;
            if ((showDataBufferSize != null) && (showDataBufferSize.compareTo("true") == 0)) {
                showDataBufferSizeBoolean = true;
            }
            // Now grab the user input for whether or not they want the data buffer converted to hex
            String convertToHex = request.getParameter("convertToHex");
            String convertTo = request.getParameter("convertTo");
            boolean convertToHexBoolean = false;
            boolean convertToImageTag = false;
            if ((convertTo != null) && (convertTo.compareTo("hex") == 0) ||
                    ((convertToHex != null) && (convertToHex.compareTo("yes") == 0))) {
                convertToHexBoolean = true;
            } else if ((convertTo != null) && (convertTo.compareTo("imageTag") == 0)) {
                convertToImageTag = true;
            }
            // Now grab the user input for whether or not they want the second data buffer displayed
            String displaySecondDataBufferString = request.getParameter("displaySecondaryDataBuffer");
            boolean displaySecondDataBuffer = false;
            if ((displaySecondDataBufferString != null) && (displaySecondDataBufferString.compareTo("true") == 0)) {
                displaySecondDataBuffer = true;
            }
            // Now grab the user input for whether or not they want the second data buffer converted to hex
            String convertSecondToHexString = request.getParameter("convertSecondaryToHex");
            boolean convertSecondToHexBoolean = false;
            if ((convertSecondToHexString != null) && (convertSecondToHexString.compareTo("yes") == 0)) {
                convertSecondToHexBoolean = true;
            }
            // Now grab the user input for whether or not they want the size of the second data buffer displayed
            String displaySecondDataBufferSizeString = request.getParameter("displaySizeOfSecondaryDataBuffer");
            boolean displaySecondDataBufferSize = false;
            if ((displaySecondDataBufferSizeString != null) && (displaySecondDataBufferSizeString.compareTo("true") == 0)) {
                displaySecondDataBufferSize = true;
            }
            // Grab the CSV list of record types
            String recordTypeString = request.getParameter("recordType");
            Long recordType = null;
            if (recordTypeString != null) {
                try {
                    recordType = new Long(recordTypeString);
                } catch (Exception e) {
                }
            }
            // Now grab the user input for how the time window will be defined.
            String timeQueryBy = request.getParameter("timeQueryType");
            if ((timeQueryBy == null) || (timeQueryBy.compareTo("") == 0)) {
                timeQueryBy = "lastNumPackets";
            }
            // Grab the number of packets back
            String numberOfPacketsString = request.getParameter("lastNumPackets");
            Long numberOfPackets = new Long(10);
            if (numberOfPacketsString != null) {
                try {
                    numberOfPackets = new Long(numberOfPacketsString);
                } catch (Exception e) {
                }
            }
            // Grab the start time
            String startTime = request.getParameter("startTime");
            // Grab the end time
            String endTime = request.getParameter("endTime");
            // Grab the number of hours offset
            String hoursOffset = request.getParameter("hoursOffset");
            // Get the user input if the user wants to try to graph the data or not
            String tryToGraph = request.getParameter("tryToGraph");
            boolean tryToGraphBoolean = false;
            if ((tryToGraph != null) && (tryToGraph.compareTo("yes") == 0)) {
                tryToGraphBoolean = true;
            }
            // Grab the user's chosen data delimiter
            String graphDelimiter = request.getParameter("graphDelimiter");
            // Grab the user's input if they want the return in an HTML table or CSV
            String htmlFormat = request.getParameter("htmlFormat");
            boolean htmlFormatBoolean = false;
            if ((htmlFormat != null) && (htmlFormat.compareTo("yes") == 0)) {
                htmlFormatBoolean = true;
            }
            // Grab the delimiter for the output if not in HTML
            String outputDelimiter = request.getParameter("outputDelimiter");
            if ((outputDelimiter == null) || (outputDelimiter.compareTo("") == 0)) {
                outputDelimiter = ",";
            }

            // First check to make sure the device ID is valid
            Long deviceIDLong = null;
            Long parentIDLong = null;
            boolean deviceIDOK = false;
            if ((deviceID != null) && (!deviceID.equals(""))) {
                try {
                    deviceIDLong = new Long(deviceID.trim());
                    deviceIDOK = true;
                } catch (Exception ex) {
                    deviceIDLong = null;
                    deviceIDOK = false;
                    out.println("The device ID does not appear to be a number");
                }
            } else {
                out.println("A Valid Device ID was not found");
            }
            // Now check if it is on the embargoed list and if the user
            // if coming in from outside
            if (deviceIDLong != null) {
                try {
                    // Try to read the properties from the localhost ssds-docs
                    // directory
                    URL embargoedDevicePropertiesURL = new URL(
                            "http://localhost/ssds-docs/conf/embargoed-devices.properties");

                    // Create a BufferedReader
                    BufferedReader in =
                            new BufferedReader(new InputStreamReader(
                            embargoedDevicePropertiesURL.openStream()));

                    String inputLine = null;
                    while ((inputLine = in.readLine()) != null) {
                        // Find the line with the list of devices
                        if (inputLine.startsWith("embargoed.devices")) {
                            // Grab what's on the right of the equals sign
                            String afterEquals = inputLine.substring(inputLine.indexOf("=") + 1);

                            // Break into array of devices
                            String[] devices = afterEquals.split(",");
                            if (ArrayUtils.indexOf(devices, deviceID.toString()) >= 0) {
                                // Now make sure requester is inside MBARI
                                if (!request.getRemoteAddr().startsWith("134.89.")) {
                                    deviceIDLong = null;
                                    deviceIDOK = false;
                                    out.println("Device is on the embargo list " + "and is only available inside MBARI.");
                                }
                            }
                        }
                    }
                    in.close();
                } catch (IOException e) {
                } catch (Exception e) {
                }
            }
            if (deviceIDOK) {
                // Setup if it is to be ordered by sequence number
                boolean orderBySequenceNumber = false;
                if (orderBy.compareTo("sequence") == 0) {
                    orderBySequenceNumber = true;
                }
                // Since we now have a OK looking device ID, let's convert the start and end Time to Calendar object
                // First check to see if the user wants to do it by end time and go back a number of hours
                Calendar startCalendar = null;
                Calendar endCalendar = null;
                if (!timeQueryBy.equals("lastNumPackets")) {
                    // Clear out the number of packets back
                    numberOfPackets = null;

                    startCalendar = Calendar.getInstance();
                    endCalendar = Calendar.getInstance();
                    if (timeQueryBy.compareTo("hoursToEnd") == 0) {
                        // Time whitespace
                        endTime = endTime.trim();
                        // Split by space
                        String[] dateTime = null;
                        try {
                            dateTime = endTime.split("\\s");
                        } catch (Exception ex) {
                        }
                        if ((dateTime != null) && (dateTime.length > 0)) {
                            // Now split each into month, day, year, hour, minutes, seconds
                            String[] date = null;
                            String month = null;
                            String dayOfMonth = null;
                            String year = null;
                            try {
                                date = dateTime[0].split("/");
                                month = date[0];
                                dayOfMonth = date[1];
                                year = date[2];
                            } catch (Exception ex) {
                            }
                            String[] time = null;
                            String hour = null;
                            String minute = null;
                            String seconds = null;
                            try {
                                time = dateTime[1].split(":");
                                hour = time[0];
                                minute = time[1];
                                seconds = time[2];
                            } catch (Exception ex) {
                            }
                            // Now set those in the endCalendar
                            if (year != null) {
                                Integer yearInteger = null;
                                try {
                                    yearInteger = new Integer(year);
                                } catch (Exception ex) {
                                }
                                if (yearInteger != null) {
                                    endCalendar.set(Calendar.YEAR, (yearInteger.intValue()));
                                }
                            }
                            if (month != null) {
                                Integer monthInteger = null;
                                try {
                                    monthInteger = new Integer(month);
                                } catch (Exception ex) {
                                }
                                if (monthInteger != null) {
                                    int monthInt = (monthInteger.intValue());
                                    monthInt = monthInt - 1;
                                    endCalendar.set(Calendar.MONTH, monthInt);
                                }
                            }
                            if (dayOfMonth != null) {
                                Integer dayOfMonthInteger = null;
                                try {
                                    dayOfMonthInteger = new Integer(dayOfMonth);
                                } catch (Exception ex) {
                                }
                                if (dayOfMonthInteger != null) {
                                    endCalendar.set(Calendar.DAY_OF_MONTH,
                                        (dayOfMonthInteger.intValue()));
                                }
                            }
                            if (hour != null) {
                                Integer hourInteger = null;
                                try {
                                    hourInteger = new Integer(hour);
                                } catch (Exception ex) {
                                }
                                if (hourInteger != null) {
                                    endCalendar.set(Calendar.HOUR_OF_DAY,
                                        (hourInteger.intValue()));
                                } else {
                                    endCalendar.set(Calendar.HOUR_OF_DAY,
                                        (new Integer("0")).intValue());
                                }
                            } else {
                                endCalendar.set(Calendar.HOUR_OF_DAY,
                                    (new Integer("0")).intValue());
                            }
                            if (minute != null) {
                                Integer minuteInteger = null;
                                try {
                                    minuteInteger = new Integer(minute);
                                } catch (Exception ex) {
                                }
                                if (minuteInteger != null) {
                                    endCalendar.set(Calendar.MINUTE,
                                        (minuteInteger.intValue()));
                                } else {
                                    endCalendar.set(Calendar.MINUTE,
                                        (new Integer("0")).intValue());
                                }
                            } else {
                                endCalendar.set(Calendar.MINUTE, (new Integer(
                                    "0")).intValue());
                            }
                            if (seconds != null) {
                                Integer secondsInteger = null;
                                try {
                                    secondsInteger = new Integer(seconds);
                                } catch (Exception ex) {
                                }
                                if (secondsInteger != null) {
                                    endCalendar.set(Calendar.SECOND,
                                        (secondsInteger.intValue()));
                                } else {
                                    endCalendar.set(Calendar.SECOND,
                                        (new Integer("0")).intValue());
                                }
                            } else {
                                endCalendar.set(Calendar.SECOND, (new Integer(
                                    "0")).intValue());
                            }
                        } // End if dateTime appears a valid string

                        // Now simply substract the number of hours from the end time to give you the start time
                        Long hoursOffsetLong = null;
                        try {
                            hoursOffsetLong = new Long(hoursOffset);
                        } catch (Exception ex) {
                            hoursOffsetLong = new Long("24");
                        }
                        long startTimeInMillis = endCalendar.getTimeInMillis()
                            - (hoursOffsetLong.longValue() * 60 * 60 * 1000);
                        startCalendar.setTimeInMillis(startTimeInMillis);
                    } else {
                        if (timeQueryBy.compareTo("startToHours") == 0) {
                            // Trim whitespace
                            startTime = startTime.trim();
                            // Split by space
                            String[] dateTime = null;
                            try {
                                dateTime = startTime.split("\\s");
                            } catch (Exception ex) {
                            }
                            if ((dateTime != null) && (dateTime.length > 0)) {
                                // Now split each into month, day, year, hour, minutes, seconds
                                String[] date = null;
                                String month = null;
                                String dayOfMonth = null;
                                String year = null;
                                try {
                                    date = dateTime[0].split("/");
                                    month = date[0];
                                    dayOfMonth = date[1];
                                    year = date[2];
                                } catch (Exception ex) {
                                }
                                String[] time = null;
                                String hour = null;
                                String minute = null;
                                String seconds = null;
                                try {
                                    time = dateTime[1].split(":");
                                    hour = time[0];
                                    minute = time[1];
                                    seconds = time[2];
                                } catch (Exception ex) {
                                }
                                // Now set those in the endCalendar
                                if (year != null) {
                                    Integer yearInteger = null;
                                    try {
                                        yearInteger = new Integer(year);
                                    } catch (Exception ex) {
                                    }
                                    if (yearInteger != null) {
                                        startCalendar.set(Calendar.YEAR,
                                            (yearInteger.intValue()));
                                    }
                                }
                                if (month != null) {
                                    Integer monthInteger = null;
                                    try {
                                        monthInteger = new Integer(month);
                                    } catch (Exception ex) {
                                    }
                                    if (monthInteger != null) {
                                        int monthInt = (monthInteger.intValue());
                                        monthInt = monthInt - 1;
                                        startCalendar.set(Calendar.MONTH,
                                            monthInt);
                                    }
                                }
                                if (dayOfMonth != null) {
                                    Integer dayOfMonthInteger = null;
                                    try {
                                        dayOfMonthInteger = new Integer(
                                            dayOfMonth);
                                    } catch (Exception ex) {
                                    }
                                    if (dayOfMonthInteger != null) {
                                        startCalendar.set(
                                            Calendar.DAY_OF_MONTH,
                                            (dayOfMonthInteger.intValue()));
                                    }
                                }
                                if (hour != null) {
                                    Integer hourInteger = null;
                                    try {
                                        hourInteger = new Integer(hour);
                                    } catch (Exception ex) {
                                    }
                                    if (hourInteger != null) {
                                        startCalendar.set(Calendar.HOUR_OF_DAY,
                                            (hourInteger.intValue()));
                                    } else {
                                        startCalendar.set(Calendar.HOUR_OF_DAY,
                                            (new Integer("0")).intValue());
                                    }
                                } else {
                                    startCalendar.set(Calendar.HOUR_OF_DAY,
                                        (new Integer("0")).intValue());
                                }
                                if (minute != null) {
                                    Integer minuteInteger = null;
                                    try {
                                        minuteInteger = new Integer(minute);
                                    } catch (Exception ex) {
                                    }
                                    if (minuteInteger != null) {
                                        startCalendar.set(Calendar.MINUTE,
                                            (minuteInteger.intValue()));
                                    } else {
                                        startCalendar.set(Calendar.MINUTE,
                                            (new Integer("0")).intValue());
                                    }
                                } else {
                                    startCalendar.set(Calendar.MINUTE,
                                        (new Integer("0")).intValue());
                                }
                                if (seconds != null) {
                                    Integer secondsInteger = null;
                                    try {
                                        secondsInteger = new Integer(seconds);
                                    } catch (Exception ex) {
                                    }
                                    if (secondsInteger != null) {
                                        startCalendar.set(Calendar.SECOND,
                                            (secondsInteger.intValue()));
                                    } else {
                                        startCalendar.set(Calendar.SECOND,
                                            (new Integer("0")).intValue());
                                    }
                                } else {
                                    startCalendar.set(Calendar.SECOND,
                                        (new Integer("0")).intValue());
                                }
                            } // End if dateTime appears a valid string

                            // Now simply substract the number of hours from the end time to give you the start time
                            Long hoursOffsetLong = null;
                            try {
                                hoursOffsetLong = new Long(hoursOffset);
                            } catch (Exception ex) {
                                hoursOffsetLong = new Long("24");
                            }
                            long endTimeInMillis = startCalendar.getTimeInMillis() + (hoursOffsetLong.longValue() * 60 * 60 * 1000);
                            endCalendar.setTimeInMillis(endTimeInMillis);
                        } else {
                            // Trim whitespace
                            endTime = endTime.trim();
                            startTime = startTime.trim();
                            // Split by space
                            String[] dateTimeStart = null;
                            String[] dateTimeEnd = null;
                            try {
                                dateTimeStart = startTime.split("\\s");
                            } catch (Exception ex) {
                            }
                            try {
                                dateTimeEnd = endTime.split("\\s");
                            } catch (Exception ex) {
                            }
                            if ((dateTimeStart != null) && (dateTimeStart.length > 0)) {
                                // Now split each into month, day, year, hour, minutes, seconds
                                String[] date = null;
                                String month = null;
                                String dayOfMonth = null;
                                String year = null;
                                try {
                                    date = dateTimeStart[0].split("/");
                                    month = date[0];
                                    dayOfMonth = date[1];
                                    year = date[2];
                                } catch (Exception ex) {
                                }
                                String[] time = null;
                                String hour = null;
                                String minute = null;
                                String seconds = null;
                                try {
                                    time = dateTimeStart[1].split(":");
                                    hour = time[0];
                                    minute = time[1];
                                    seconds = time[2];
                                } catch (Exception ex) {
                                }
                                // Now set those in the endCalendar
                                if (year != null) {
                                    Integer yearInteger = null;
                                    try {
                                        yearInteger = new Integer(year);
                                    } catch (Exception ex) {
                                    }
                                    if (yearInteger != null) {
                                        startCalendar.set(Calendar.YEAR,
                                            (yearInteger.intValue()));
                                    }
                                }
                                if (month != null) {
                                    Integer monthInteger = null;
                                    try {
                                        monthInteger = new Integer(month);
                                    } catch (Exception ex) {
                                    }
                                    if (monthInteger != null) {
                                        int monthInt = (monthInteger.intValue());
                                        monthInt = monthInt - 1;
                                        startCalendar.set(Calendar.MONTH,
                                            monthInt);
                                    }
                                }
                                if (dayOfMonth != null) {
                                    Integer dayOfMonthInteger = null;
                                    try {
                                        dayOfMonthInteger = new Integer(
                                            dayOfMonth);
                                    } catch (Exception ex) {
                                    }
                                    if (dayOfMonthInteger != null) {
                                        startCalendar.set(
                                            Calendar.DAY_OF_MONTH,
                                            (dayOfMonthInteger.intValue()));
                                    }
                                }
                                if (hour != null) {
                                    Integer hourInteger = null;
                                    try {
                                        hourInteger = new Integer(hour);
                                    } catch (Exception ex) {
                                    }
                                    if (hourInteger != null) {
                                        startCalendar.set(Calendar.HOUR_OF_DAY,
                                            (hourInteger.intValue()));
                                    } else {
                                        startCalendar.set(Calendar.HOUR_OF_DAY,
                                            (new Integer("0")).intValue());
                                    }
                                } else {
                                    startCalendar.set(Calendar.HOUR_OF_DAY,
                                        (new Integer("0")).intValue());
                                }
                                if (minute != null) {
                                    Integer minuteInteger = null;
                                    try {
                                        minuteInteger = new Integer(minute);
                                    } catch (Exception ex) {
                                    }
                                    if (minuteInteger != null) {
                                        startCalendar.set(Calendar.MINUTE,
                                            (minuteInteger.intValue()));
                                    } else {
                                        startCalendar.set(Calendar.MINUTE,
                                            (new Integer("0")).intValue());
                                    }
                                } else {
                                    startCalendar.set(Calendar.MINUTE,
                                        (new Integer("0")).intValue());
                                }
                                if (seconds != null) {
                                    Integer secondsInteger = null;
                                    try {
                                        secondsInteger = new Integer(seconds);
                                    } catch (Exception ex) {
                                    }
                                    if (secondsInteger != null) {
                                        startCalendar.set(Calendar.SECOND,
                                            (secondsInteger.intValue()));
                                    } else {
                                        startCalendar.set(Calendar.SECOND,
                                            (new Integer("0")).intValue());
                                    }
                                } else {
                                    startCalendar.set(Calendar.SECOND,
                                        (new Integer("0")).intValue());
                                }
                            } // End if dateTime appears a valid string
                            if ((dateTimeEnd != null) && (dateTimeEnd.length > 0)) {
                                // Now split each into month, day, year, hour, minutes, seconds
                                String[] date = null;
                                String month = null;
                                String dayOfMonth = null;
                                String year = null;
                                try {
                                    date = dateTimeEnd[0].split("/");
                                    month = date[0];
                                    dayOfMonth = date[1];
                                    year = date[2];
                                } catch (Exception ex) {
                                }
                                String[] time = null;
                                String hour = null;
                                String minute = null;
                                String seconds = null;
                                try {
                                    time = dateTimeEnd[1].split(":");
                                    hour = time[0];
                                    minute = time[1];
                                    seconds = time[2];
                                } catch (Exception ex) {
                                }
                                // Now set those in the endCalendar
                                if (year != null) {
                                    Integer yearInteger = null;
                                    try {
                                        yearInteger = new Integer(year);
                                    } catch (Exception ex) {
                                    }
                                    if (yearInteger != null) {
                                        endCalendar.set(Calendar.YEAR,
                                            (yearInteger.intValue()));
                                    }
                                }
                                if (month != null) {
                                    Integer monthInteger = null;
                                    try {
                                        monthInteger = new Integer(month);
                                    } catch (Exception ex) {
                                    }
                                    if (monthInteger != null) {
                                        int monthInt = (monthInteger.intValue());
                                        monthInt = monthInt - 1;
                                        endCalendar.set(Calendar.MONTH,
                                            monthInt);
                                    }
                                }
                                if (dayOfMonth != null) {
                                    Integer dayOfMonthInteger = null;
                                    try {
                                        dayOfMonthInteger = new Integer(
                                            dayOfMonth);
                                    } catch (Exception ex) {
                                    }
                                    if (dayOfMonthInteger != null) {
                                        endCalendar.set(Calendar.DAY_OF_MONTH,
                                            (dayOfMonthInteger.intValue()));
                                    }
                                }
                                if (hour != null) {
                                    Integer hourInteger = null;
                                    try {
                                        hourInteger = new Integer(hour);
                                    } catch (Exception ex) {
                                    }
                                    if (hourInteger != null) {
                                        endCalendar.set(Calendar.HOUR_OF_DAY,
                                            (hourInteger.intValue()));
                                    } else {
                                        endCalendar.set(Calendar.HOUR_OF_DAY,
                                            (new Integer("0")).intValue());
                                    }
                                } else {
                                    endCalendar.set(Calendar.HOUR_OF_DAY,
                                        (new Integer("0")).intValue());
                                }
                                if (minute != null) {
                                    Integer minuteInteger = null;
                                    try {
                                        minuteInteger = new Integer(minute);
                                    } catch (Exception ex) {
                                    }
                                    if (minuteInteger != null) {
                                        endCalendar.set(Calendar.MINUTE,
                                            (minuteInteger.intValue()));
                                    } else {
                                        endCalendar.set(Calendar.MINUTE,
                                            (new Integer("0")).intValue());
                                    }
                                } else {
                                    endCalendar.set(Calendar.MINUTE,
                                        (new Integer("0")).intValue());
                                }
                                if (seconds != null) {
                                    Integer secondsInteger = null;
                                    try {
                                        secondsInteger = new Integer(seconds);
                                    } catch (Exception ex) {
                                    }
                                    if (secondsInteger != null) {
                                        endCalendar.set(Calendar.SECOND,
                                            (secondsInteger.intValue()));
                                    } else {
                                        endCalendar.set(Calendar.SECOND,
                                            (new Integer("0")).intValue());
                                    }
                                } else {
                                    endCalendar.set(Calendar.SECOND,
                                        (new Integer("0")).intValue());
                                }
                            } // End if dateTime appears a valid string
                        }
                    }
                }

                // Grab the local interface to the SQLDataStreamRawDataAccessEJB
                SQLDataStreamRawDataAccessLocalHome sqlDSLocalHome = null;
                try {
                    sqlDSLocalHome = SQLDataStreamRawDataAccessUtil.getLocalHome();
                } catch (Exception ex) {
                }
                SQLDataStreamRawDataAccessLocal dataStreamRawDataAccessLocal = null;
                if (sqlDSLocalHome != null) {
                    try {
                        dataStreamRawDataAccessLocal = sqlDSLocalHome.create();
                    } catch (Exception ex) {
                    }
                }

                // Check if Calendars exist, if so convert them to seconds
                Long startTimestampSeconds = null;
                Long endTimestampSeconds = null;
                if (startCalendar != null) {
                    startTimestampSeconds = new Long(startCalendar.getTimeInMillis() / 1000);
                }
                if (endCalendar != null) {
                    endTimestampSeconds = new Long(endCalendar.getTimeInMillis() / 1000);
                }
                TreeMap dataMap = null;
                try {
                    dataMap = dataStreamRawDataAccessLocal.getSortedRawData(
                        deviceIDLong, null, null, null, null, recordType, null,
                        null, null, null, null, startTimestampSeconds,
                        endTimestampSeconds, null, null, null, null,
                        numberOfPackets, null, null, null, null, null, null,
                        orderBy, true);
                } catch (Exception ex) {
                }

                String keyType = "timestamp";
                if (orderBySequenceNumber) {
                    keyType = "sequenceNumber";
                }
                // Print the HTML header
                out.print("<div id=\"contentTitle\">Data Report for Device " + deviceID + " sorted by ");
                if (orderByTimeBoolean) {
                    out.print("SIAM timestamp</div>");
                } else {
                    out.print("packet sequence number</div>");
                }
                out.println("<div id=\"content\">");
                // If HTML format, print a table line, row line and an opening header tag.  If
                // not, simple print out an opening pre tag
                if (htmlFormatBoolean) {
                    out.println("<table style=\"background-color: white;\" cellpadding=\"1\" border=\"1\" cellspacing=\"1\"><tr>");
                } else {
                    out.println("<pre>");
                }
                // A boolean to track if we have printed out a first column already
                boolean firstColumnYet = false;
                // First print out the SIAM Timestamp column
                if (displayTimestampBoolean) {
                    if (htmlFormatBoolean) {
                        out.print("<th>SIAM Timestamp</th>");
                    } else {
                        if (firstColumnYet) {
                            out.print(outputDelimiter);
                        }
                        out.print("SIAM Timestamp");
                    }
                    firstColumnYet = true;
                }
                // Now print out timestamp converted to date if requested
                if (showDateBoolean) {
                    String chosenTimezone = "GMT";
                    if (timezone.compareTo("pst") == 0) {
                        chosenTimezone = "PST";
                    }
                    if (htmlFormatBoolean) {
                        out.print("<th>SIAM Timestamp<br>converted to time/date (" + chosenTimezone + ")</th>");
                    } else {
                        if (firstColumnYet) {
                            out.print(outputDelimiter);
                        }
                        out.print("SIAM Timestamp coverted to time/date(" + chosenTimezone + ")");
                    }
                    firstColumnYet = true;
                }
                // Now print out the sequence number
                if (displaySequenceNumberBoolean) {
                    if (htmlFormatBoolean) {
                        out.print("<th>Packet Sequence<br>Number</th>");
                    } else {
                        if (firstColumnYet) {
                            out.print(outputDelimiter);
                        }
                        out.print("Packet Sequence Number");
                    }
                    firstColumnYet = true;
                }
                // Now print out the parent ID column if it is chosen
                if (displayParentID) {
                    if (htmlFormatBoolean) {
                        out.print("<th>Parent ID</th>");
                    } else {
                        if (firstColumnYet) {
                            out.print(outputDelimiter);
                        }
                        out.print("Parent ID");
                    }
                    firstColumnYet = true;
                }
                // Same for record type
                if (displayRecordType) {
                    if (htmlFormatBoolean) {
                        out.print("<th>Record Type</th>");
                    } else {
                        if (firstColumnYet) {
                            out.print(outputDelimiter);
                        }
                        out.print("Record Type");
                    }
                    firstColumnYet = true;
                }
                // Same for packet type
                if (displayPacketType) {
                    if (htmlFormatBoolean) {
                        out.print("<th>Packet Type</th>");
                    } else {
                        if (firstColumnYet) {
                            out.print(outputDelimiter);
                        }
                        out.print("Packet Type");
                    }
                    firstColumnYet = true;
                }
                // Is data buffer size chosen
                if (showDataBufferSizeBoolean) {
                    // Print out the buffer size
                    if (htmlFormatBoolean) {
                        out.print("<th>Buffer Size<br>(# bytes)</th>");
                    } else {
                        if (firstColumnYet) {
                            out.print(outputDelimiter);
                        }
                        out.print("Buffer Size (# bytes)");
                    }
                    firstColumnYet = true;
                }
                // Print out the appropriate data column header
                if (displayDataBuffer) {
                    if (convertToHexBoolean) {
                        if (htmlFormatBoolean) {
                            out.print("<th>Hex Representation of Data Buffer</th>");
                        } else {
                            if (firstColumnYet) {
                                out.print(outputDelimiter);
                            }
                            out.print("Hex Representation of Data Buffer");
                        }
                        firstColumnYet = true;
                    } else if (convertToImageTag) {
                        if (htmlFormatBoolean) {
                            out.print("<th>Data Buffer as Image Tag</th>");
                        } else {
                            if (firstColumnYet) {
                                out.print(outputDelimiter);
                            }
                            out.print("Data Buffer as Image Tag");
                        }
                        firstColumnYet = true;
                    } else {
                        if (htmlFormatBoolean) {
                            out.print("<th>String Representation of Data Buffer</th>");
                        } else {
                            if (firstColumnYet) {
                                out.print(outputDelimiter);
                            }
                            out.print("String Representation of Data Buffer");
                        }
                        firstColumnYet = true;
                    }
                }
                // Is data buffer size chosen
                if (displaySecondDataBufferSize) {
                    // Print out the buffer size
                    if (htmlFormatBoolean) {
                        out.print("<th>Secondary Buffer<br>Size (# bytes)</th>");
                    } else {
                        if (firstColumnYet) {
                            out.print(outputDelimiter);
                        }
                        out.print("Secondary Buffer Size (# bytes)");
                    }
                    firstColumnYet = true;
                }
                // Print out the appropriate data column header
                if (displaySecondDataBuffer) {
                    if (convertSecondToHexBoolean) {
                        if (htmlFormatBoolean) {
                            out.print("<th>Hex Representation of Secondary Data Buffer</th>");
                        } else {
                            if (firstColumnYet) {
                                out.print(outputDelimiter);
                            }
                            out.print("Hex Representation of Secondary Data Buffer");
                        }
                        firstColumnYet = true;
                    } else {
                        if (htmlFormatBoolean) {
                            out.print("<th>String Representation of Secondary Data Buffer</th>");
                        } else {
                            if (firstColumnYet) {
                                out.print(outputDelimiter);
                            }
                            out.print("String Representation of Secondary Data Buffer");
                        }
                        firstColumnYet = true;
                    }
                }
                out.println("");
                // Setup a NumberFormat in case we need it for the OASIS stuff
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumIntegerDigits(3);
                nf.setMaximumFractionDigits(5);
                nf.setMinimumIntegerDigits(1);
                nf.setMinimumFractionDigits(5);
                // Set up some timezones and Calendars
                TimeZone timeZone = null;
                if (timezone.compareTo("pst") == 0) {
                    timeZone = TimeZone.getTimeZone("PST");
                } else {
                    timeZone = TimeZone.getTimeZone("GMT");
                }
                Calendar currentCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                // Now grab the key set and loop through it to print out rows
                Set keySet = dataMap.keySet();
                Iterator i = keySet.iterator();
                long prevSeqNum = -1L;
                while (i.hasNext()) {
                    Long key = (Long) i.next();
                    // Grab the collection
                    Collection values = (Collection) dataMap.get(key);
                    if (values != null) {
                        Iterator iterator = values.iterator();
                        while (iterator.hasNext()) {
                            SSDSGeoLocatedDevicePacket ssdsDP = (SSDSGeoLocatedDevicePacket) iterator.next();
                            firstColumnYet = false;
                            // If html format, print out row tag and turn it red if it is sorted by sequence number and one or more are missing
                            if (htmlFormatBoolean) {
                                out.print("<tr");
                                // If this is by sequence number and there is a gap, change the color
                                if ((!orderByTimeBoolean) && (prevSeqNum > -1) && ((key.longValue() - prevSeqNum) > 1)) {
                                    out.print(" bgcolor=\"red\"");
                                }
                                out.print(">");
                            }
                            // Now print out the timestamp
                            if (displayTimestampBoolean) {
                                if (htmlFormatBoolean) {
                                    out.print("<td><xmp>" + ssdsDP.systemTime() + "</xmp></td>");
                                } else {
                                    if (firstColumnYet) {
                                        out.print(outputDelimiter);
                                    }
                                    out.print(ssdsDP.systemTime());
                                }
                                firstColumnYet = true;
                            }
                            // If a date column is specified print that out too
                            if (showDateBoolean) {
                                String dateString = null;
                                if (currentCalendar != null) {
                                    currentCalendar.setTimeInMillis(ssdsDP.systemTime());
                                    if (dateTimeConversionFormat.compareTo("oasis") == 0) {
                                        double dayOfYear = (double) currentCalendar.get(Calendar.DAY_OF_YEAR);
                                        double hourOfDay = (double) currentCalendar.get(Calendar.HOUR_OF_DAY);
                                        double minute = (double) currentCalendar.get(Calendar.MINUTE);
                                        double second = (double) currentCalendar.get(Calendar.SECOND);
                                        double fracDay = hourOfDay + minute / 60.0 + second / 3600.0;
                                        fracDay = fracDay / 24.0;
                                        double oasisTime = (double) (dayOfYear + fracDay);
                                        dateString = nf.format(oasisTime);
                                    } else {
                                        DateFormat df = new SimpleDateFormat(
                                            dateTimeConversionFormat);
                                        df.setTimeZone(timeZone);
                                        dateString = df.format(currentCalendar.getTime());
                                    }
                                    // If html format is on, print out Date with table header tags
                                    if (htmlFormatBoolean) {
                                        out.print("<td><xmp>" + dateString + "</xmp></td>");
                                    } else {
                                        if (firstColumnYet) {
                                            out.print(outputDelimiter);
                                        }
                                        out.print(dateString);
                                    }
                                }
                                firstColumnYet = true;
                            }
                            // Now print out sequence number
                            if (displaySequenceNumberBoolean) {
                                if (htmlFormatBoolean) {
                                    out.print("<td><xmp>" + ssdsDP.sequenceNo() + "</xmp></td>");
                                } else {
                                    if (firstColumnYet) {
                                        out.print(outputDelimiter);
                                    }
                                    out.print(ssdsDP.sequenceNo());
                                }
                                firstColumnYet = true;
                            }
                            // If parent ID is specified, print that too
                            if (displayParentID) {
                                if (htmlFormatBoolean) {
                                    out.print("<td>" + ssdsDP.getPlatformID() + "</td>");
                                } else {
                                    if (firstColumnYet) {
                                        out.print(outputDelimiter);
                                    }
                                    out.print(ssdsDP.getPlatformID());
                                }
                                firstColumnYet = true;
                            }
                            // If record type is chosen, print that too
                            if (displayRecordType) {
                                if (htmlFormatBoolean) {
                                    out.print("<td>" + ssdsDP.getRecordType() + "</td>");
                                } else {
                                    if (firstColumnYet) {
                                        out.print(outputDelimiter);
                                    }
                                    out.print(ssdsDP.getRecordType());
                                }
                                firstColumnYet = true;
                            }
                            // same for packet type
                            if (displayPacketType) {
                                String packetTypeString = null;
                                if (ssdsDP.getPacketType() == 0) {
                                    packetTypeString = "Metadata";
                                }
                                if (ssdsDP.getPacketType() == 1) {
                                    packetTypeString = "Data";
                                }
                                if (ssdsDP.getPacketType() == 2) {
                                    packetTypeString = "Message";
                                }
                                if (htmlFormatBoolean) {
                                    out.print("<td>" + packetTypeString + "</td>");
                                } else {
                                    if (firstColumnYet) {
                                        out.print(outputDelimiter);
                                    }
                                    out.print(packetTypeString);
                                }
                                firstColumnYet = true;
                            }
                            // Display buffer size if appropriate
                            if (showDataBufferSizeBoolean) {
                                // Print out the buffer size
                                if (htmlFormatBoolean) {
                                    out.print("<td><xmp>" + ssdsDP.getDataBuffer().length + "</xmp></td>");
                                } else {
                                    if (firstColumnYet) {
                                        out.print(outputDelimiter);
                                    }
                                    out.print(ssdsDP.getDataBuffer().length);
                                }
                                firstColumnYet = true;
                            }
                            // Do Hex conversion if requested
                            if (displayDataBuffer) {
                                if (convertToHexBoolean) {
                                    StringBuffer hexData = new StringBuffer();
                                    ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(
                                        ssdsDP.getDataBuffer());
                                    while (byteArrayIS.available() > 0) {
                                        hexData.append(Integer.toHexString(
                                            (0xFF & byteArrayIS.read()) | 0x100).substring(1));
                                    }
                                    if (htmlFormatBoolean) {
                                        out.print("<td><xmp>" + hexData.toString().toUpperCase() + "</xmp></td>");
                                    } else {
                                        if (firstColumnYet) {
                                            out.print(outputDelimiter);
                                        }
                                        out.print(hexData.toString().toUpperCase());
                                    }
                                    firstColumnYet = true;
                                } else if (convertToImageTag) {
                                    String godsBase = "../servlet/GetOriginalDataServlet";
                                    String godsDeviceParameter = "?deviceID=" + deviceID;
                                    String godsEndTimestampSeconds = "&endTimestampSeconds=" + ssdsDP.systemTime()/1000;
                                    String godsEndTimestampNanoseconds = "&endTimestampNanoSeconds="
                                            + (ssdsDP.systemTime()-ssdsDP.systemTime()/1000)*1000;
                                    String godsRest = "&lastNumberOfPackets=1"
                                            + "&outputAs=binary" + "&displayPacketHeaderInfo=false" + "&noHTMLHeader=true";
                                    String imageTagString = "<img src=\"" + godsBase
                                            + godsDeviceParameter + godsEndTimestampSeconds
                                            + godsEndTimestampNanoseconds + godsRest + "\"/>";
                                    if (htmlFormatBoolean) {
                                        out.print("<td>" + imageTagString + "</td>");
                                    } else {
                                        if (firstColumnYet) {
                                            out.print(outputDelimiter);
                                        }
                                        out.print(imageTagString);
                                    }
                                    firstColumnYet = true;
                                } else {
                                    // If html, print column tag and xmp tag
                                    if (htmlFormatBoolean) {
                                        out.print("<td><xmp>" + new String(ssdsDP.getDataBuffer()) + "</xmp></td>");
                                    } else {
                                        if (firstColumnYet) {
                                            out.print(outputDelimiter);
                                        }
                                        out.print(new String(ssdsDP.getDataBuffer()));
                                    }
                                    firstColumnYet = true;
                                }
                            }
                            // Display buffer size if appropriate
                            if (displaySecondDataBufferSize) {
                                byte[] dataBuffer = new byte[1];
                                int dataBufferLength = -1;
                                try {
                                    dataBuffer = ssdsDP.getOtherBuffer();
                                } catch (Exception ex) {
                                }
                                try {
                                    dataBufferLength = dataBuffer.length;
                                } catch (Exception ex) {
                                }
                                // Print out the buffer size
                                if (htmlFormatBoolean) {
                                    out.print("<td><xmp>" + dataBufferLength + "</xmp></td>");
                                } else {
                                    if (firstColumnYet) {
                                        out.print(outputDelimiter);
                                    }
                                    out.print(dataBufferLength);
                                }
                                firstColumnYet = true;
                            }
                            // Do Hex conversion if requested
                            if (displaySecondDataBuffer) {
                                byte[] dataBuffer = new byte[1];
                                try {
                                    dataBuffer = ssdsDP.getOtherBuffer();
                                } catch (Exception ex) {
                                }
                                if (convertSecondToHexBoolean) {
                                    StringBuffer hexData = new StringBuffer();
                                    for (int hexCounter = 0; hexCounter < dataBuffer.length; hexCounter++) {
                                        hexData.append(Integer.toHexString(dataBuffer[hexCounter]));
                                    }
                                    if (htmlFormatBoolean) {
                                        out.print("<td><xmp>" + hexData + "</xmp></td>");
                                    } else {
                                        if (firstColumnYet) {
                                            out.print(outputDelimiter);
                                        }
                                        out.print(hexData);
                                    }
                                    firstColumnYet = true;
                                } else {
                                    // If html, print column tag and xmp tag
                                    String bufferString = null;
                                    try {
                                        bufferString = new String(dataBuffer);
                                    } catch (Exception ex) {
                                    }
                                    if (htmlFormatBoolean) {
                                        out.print("<td><xmp>" + bufferString + "</xmp></td>");
                                    } else {
                                        if (firstColumnYet) {
                                            out.print(outputDelimiter);
                                        }
                                        out.print(bufferString);
                                    }
                                    firstColumnYet = true;
                                }
                            }
                        }
                    }
                    out.println("");
                    out.flush();
                    prevSeqNum = key.longValue();
                }
                if (htmlFormatBoolean) {
                    out.println("</table>");
                    out.println("</div>");
                } else {
                    out.println("</pre>");
                }
                // OK, so now the table/data dump has been created, let's check to see
                // if the user wanted a graph created
                if ((dataMap.size() > 0) && (tryToGraphBoolean)) {
                    // The first thing to do is try to find out if we think we can
                    // get anything useful out of the TreeMap we got.  We know we
                    // have longs for keys, so that is OK, but we need to try and
                    // parse apart the first record (or maybe even records in case
                    // the first record is an anomoly) to see if we can get anything
                    // useful from the data

                    // First let's create a StringBuffer that we can use to track
                    // information about the attempt to create a graph and at least
                    // give the user some feedback if the graph could not be created
                    StringBuffer messageToUser = new StringBuffer();

                    // Grab key set and create an iterator
                    Set keySet2 = dataMap.keySet();
                    Iterator i2 = keySet2.iterator();

                    // Loop through and look for first "Data" packet
                    SSDSDevicePacket ssdsDataPacket = null;
                    while (i2.hasNext()) {
                        Long key2 = (Long) i2.next();
                        // Grab the collection
                        Collection values2 = (Collection) dataMap.get(key2);
                        if (values2 != null) {
                            Iterator iterator2 = values2.iterator();
                            while (iterator2.hasNext()) {
                                SSDSGeoLocatedDevicePacket ssdsDP2 = (SSDSGeoLocatedDevicePacket) iterator2.next();
                                if (ssdsDP2.getPacketType() == 1) {
                                    ssdsDataPacket = ssdsDP2;
                                    break;
                                }
                            }
                        }
                        if (ssdsDataPacket != null) {
                            break;
                        }
                    }
                    if (ssdsDataPacket != null) {
                        // Based on the delimiter, let's see if we can split up the new string
                        String delimiterPattern = null;
                        // The arrayList of columns that have data that data can be pulled from
                        ArrayList validColumns = new ArrayList();
                        if (graphDelimiter.compareTo("comma") == 0) {
                            delimiterPattern = new String(",");
                        }
                        if (graphDelimiter.compareTo("tab") == 0) {
                            delimiterPattern = new String("\\s+");
                        }
                        if (graphDelimiter.compareTo("colon") == 0) {
                            delimiterPattern = new String(":");
                        }
                        if (graphDelimiter.compareTo("semicolon") == 0) {
                            delimiterPattern = new String(";");
                        }
                        if (graphDelimiter.compareTo("space") == 0) {
                            delimiterPattern = new String("\\s+");
                        }
                        if (delimiterPattern != null) {
                            // Now let's see which columns we are able to get graphable data from
                            Pattern dataPattern = null;
                            try {
                                dataPattern = Pattern.compile("^[^0-9&&[^-]]*(-*\\d+\\.*\\d*)([[eE][+-]\\d+]*)\\D*");
                            } catch (Exception ex) {
                                out.println("<b>Could not compile data pattern</b>");
                            }
                            String firstByteArray = new String(ssdsDataPacket.getDataBuffer());
                            String[] columns = firstByteArray.split(delimiterPattern);
                            if ((columns != null) && (columns.length != 0)) {
                                // Loop through all the columns and look for pattern matches
                                for (int columnIndex = 0; columnIndex < columns.length; columnIndex++) {
                                    // See if a double can be pulled from the text
                                    try {
                                        Matcher m = dataPattern.matcher(columns[columnIndex]);
                                        boolean b = m.matches();
                                        if (b) {
                                            //								out.println("<b>Column " + columnIndex + " had a match in it, try to create a Double from it...</b><br>");
                                            boolean createdDouble = true;
                                            try {
                                                Double testDouble = new Double(m.group(1));
                                                //									out.println("testDouble is : " + testDouble.doubleValue() + "<br>");
                                            } catch (Exception ex) {
                                                createdDouble = false;
                                            }
                                            // If a Double was created then go ahead and add the column to the list of valid columns
                                            if (createdDouble) {
                                                validColumns.add(new Integer(columnIndex));
                                            }
                                        }
                                    } catch (Exception ex) {
                                        out.println("<b>Could not extract data from text in column " + columnIndex + "</b>");
                                    }
                                }
                                // OK, so now we have an ArrayList that has all the valid columns in it.
                                // Now we can create an array of XYSeries that we will use to graph
                                XYSeries[] dataSeries = new XYSeries[validColumns.size()];
                                double[] minValues = new double[validColumns.size()];
                                double[] maxValues = new double[validColumns.size()];
                                for (int xyIndex = 0; xyIndex < validColumns.size(); xyIndex++) {
                                    dataSeries[xyIndex] = new XYSeries("");
                                    minValues[xyIndex] = Double.MAX_VALUE;
                                    maxValues[xyIndex] = -1 * Double.MAX_VALUE;
                                }
                                // Let's cruise through the dataMap and try to build up the XY Series of data
                                Set graphKeySet = dataMap.keySet();
                                Iterator iGraphKeySet = graphKeySet.iterator();
                                while (iGraphKeySet.hasNext()) {
                                    Long graphKey = (Long) iGraphKeySet.next();
                                    // Grab the collection of SSDSDataPackets
                                    Collection ssdsDataPackets = (Collection) dataMap.get(graphKey);
                                    // Now iterate over those to get SSDSDevicePackets
                                    Iterator ssdsDataPacketsIter = ssdsDataPackets.iterator();
                                    while (ssdsDataPacketsIter.hasNext()) {
                                        // Grab the SSDSDevicePacket
                                        SSDSDevicePacket ssdsDevicePacket = (SSDSDevicePacket) ssdsDataPacketsIter.next();
                                        if (ssdsDevicePacket.getPacketType() == 1) {
                                            String graphData = new String(ssdsDevicePacket.getDataBuffer());
                                            // Now split the data into columns
                                            String[] currentDataColumns = graphData.split(delimiterPattern);
                                            // Now for each valid column, grab the key
                                            for (int validColumnIndex = 0; validColumnIndex < validColumns.size(); validColumnIndex++) {
                                                int column = ((Integer) validColumns.get(validColumnIndex)).intValue();
                                                try {
                                                    Matcher m = dataPattern.matcher(currentDataColumns[column]);
                                                    boolean b = m.matches();
                                                    if (b) {
                                                        boolean createdDouble = true;
                                                        Double putDouble = null;
                                                        String doubleString = null;
                                                        String exponentString = null;
                                                        try {
                                                            doubleString = m.group(1);
                                                        } catch (Exception ex) {
                                                            createdDouble = false;
                                                        }
                                                        try {
                                                            exponentString = m.group(2);
                                                        } catch (Exception ex) {
                                                        }
                                                        try {
                                                            putDouble = new Double(doubleString);
                                                        } catch (Exception ex) {
                                                            createdDouble = false;
                                                        }
                                                        if ((exponentString != null) && (exponentString.compareTo("") != 0)) {
                                                            String exponentValue = exponentString.substring(1);
                                                            if (exponentValue.charAt(0) == '+') {
                                                                exponentValue = exponentValue.substring(1);
                                                            }
                                                            // Multiply by the exponent
                                                            Integer exponentInteger = null;
                                                            try {
                                                                exponentInteger = new Integer(exponentValue);
                                                            } catch (Exception ex) {
                                                            }
                                                            if ((exponentInteger != null) && (exponentInteger.intValue() != 0)) {
                                                                try {
                                                                    putDouble = new Double(
                                                                            putDouble.doubleValue() * (Math.pow(
                                                                            10, exponentInteger.intValue())));
                                                                } catch (Exception ex) {
                                                                    createdDouble = false;
                                                                }
                                                            }
                                                        }
                                                        // If a Double was created then add it the XYSeries
                                                        if (createdDouble) {
                                                            if (putDouble.doubleValue() > maxValues[validColumnIndex]) {
                                                                maxValues[validColumnIndex] = putDouble.doubleValue();
                                                            }
                                                            if (putDouble.doubleValue() < minValues[validColumnIndex]) {
                                                                minValues[validColumnIndex] = putDouble.doubleValue();
                                                            }
                                                            (dataSeries[validColumnIndex]).add(
                                                                    graphKey.longValue(),
                                                                    putDouble.doubleValue());
                                                        }
                                                    }
                                                } catch (Exception ex) {
                                                }
                                            }
                                        }
                                    }
                                } // End loop through dataMap
                                // OK, now I have an array of XYSeries objects that I can use to create a combined
                                // graph for all
                                // Create a simple date format
                                SimpleDateFormat sdf = new SimpleDateFormat(
                                    "MM/dd/yy HH:mm:ss", Locale.US);
                                sdf.setTimeZone(timeZone);
                                // Create the value axis
                                ValueAxis xAxis = null;
                                if (orderByTimeBoolean) {
                                    DateAxis dAxis = new DateAxis("");
                                    dAxis.setDateFormatOverride(sdf);
                                    xAxis = dAxis;
                                } else {
                                    xAxis = new NumberAxis("");
                                }
                                // Loop through each XYSeries and create graph
                                for (int xySeriesIndex = 0; xySeriesIndex < dataSeries.length; xySeriesIndex++) {
                                    // Break up plots into groups of 10 variables for server functioning
                                    // Check to see if the chart would be a flat line and if so, print
                                    // out message and move on
                                    if (minValues[xySeriesIndex] == maxValues[xySeriesIndex]) {
                                        out.println("<h3>Chart was not created because line would be flat with value of " + minValues[xySeriesIndex] + "</h3>");
                                        continue;
                                    }
                                    // Create the XYSeriesCollection
                                    XYSeriesCollection xyColl = new XYSeriesCollection(
                                        dataSeries[xySeriesIndex]);
                                    // The Y Axis
                                    NumberAxis yAxis = new NumberAxis("");
                                    yAxis.setAutoRangeIncludesZero(false);
                                    // Create the item renderer
                                    StandardXYItemRenderer renderer = new StandardXYItemRenderer(
                                        StandardXYItemRenderer.LINES + StandardXYItemRenderer.SHAPES);
                                    // Set the fill shapes
                                    renderer.setShapesFilled(true);
                                    // Now create the XYPlot
                                    XYPlot xyPlot = new XYPlot(xyColl, xAxis,
                                        yAxis, renderer);
                                    // Hmmmm
                                    Range range = yAxis.getRange();
                                    if (range.getLowerBound() == range.getUpperBound()) {
                                        yAxis.setRange(range.getLowerBound() - Math.abs(range.getLowerBound()) * 0.01, range.getUpperBound() + Math.abs(range.getUpperBound()) * 0.01);
                                    }
                                    Double whackedRange = new Double("4.0E-320");
                                    range = yAxis.getRange();
                                    if (range.getUpperBound() - range.getLowerBound() < whackedRange.doubleValue()) {
                                        yAxis.setRange(-0.1, 0.1);
                                    }
                                    JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, xyPlot, false);
                                    chart.setBackgroundPaint(java.awt.Color.white);
                                    ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
                                    String filename = ServletUtilities.saveChartAsPNG(chart, 1024, 400, info, session);
                                    out.flush();
                                    out.println("<br><img src=\"" + request.getContextPath() + "/DisplayChart?filename=" + filename + "\" width=1024 height=400 border=0 usemap=\"#" + filename + "\"></img><br>");
                                }
                            }
                        } else {
                            messageToUser.append("The delimiter was not reckognized, so no chart was created");
                        }
                    }
                    out.println("<br>" + messageToUser.toString());
                }
            } // End if DeviceID OK

%>
    </body>
</html>
