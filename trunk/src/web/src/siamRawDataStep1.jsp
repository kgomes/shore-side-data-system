<%@page
	import="java.io.*,java.util.*,java.text.*,java.net.*,moos.ssds.metadata.*,moos.ssds.services.metadata.*,moos.ssds.services.data.*,moos.ssds.util.*,javax.naming.*,java.util.regex.*"%>
<!-- Include JSP Setup code common to all pages -->
<%@ include file="WEB-INF/fragments/setup.jspf"%>
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
<title>Access SIAM Raw Data, Step 1</title>
<!-- Include the mbari stylesheets -->
<%@ include file="WEB-INF/fragments/mbari_styles.jspf"%>
<!-- Include calendar java script -->
<script language="JavaScript" src="calendar2.js"></script>
<script type="text/javascript">
            function selectEndDateBack() {
                document.forms['querySetup'].elements['startTime'].disabled=true;
                document.forms['querySetup'].elements['endTime'].disabled=false;
                document.forms['querySetup'].elements['hoursOffset'].disabled=false;
                document.startCalAnchor.href="javascript:void(0)";
                document.endCalAnchor.href="javascript:endCal.popup()";
            }
            function selectStartDateForward() {
                document.forms['querySetup'].elements['startTime'].disabled=false;
                document.forms['querySetup'].elements['endTime'].disabled=true;
                document.forms['querySetup'].elements['hoursOffset'].disabled=false;
                document.startCalAnchor.href="javascript:startCal.popup()";
                document.endCalAnchor.href="javascript:void(0)";
            }
            function selectStartToEndDate() {
                document.forms['querySetup'].elements['startTime'].disabled=false;
                document.forms['querySetup'].elements['endTime'].disabled=false;
                document.forms['querySetup'].elements['hoursOffset'].disabled=true;
                document.startCalAnchor.href="javascript:startCal.popup()";
                document.endCalAnchor.href="javascript:endCal.popup()";
            }
        </script>
</head>
<body id="body">
<!-- Include the MBARI specific headers -->
<%@ include file="WEB-INF/fragments/mbari_head.jspf"%>
<!-- *** BEGIN PAGE CONTENT HERE *** -->
<div align="center" id="pagetitle">Raw Data Access</div>
<%
            // This is the StringBuffer that will be used to build the pick list
            StringBuffer devicePickList = new StringBuffer();
            devicePickList.append("<option value=\"\" selected=\"true\"></option>");

            // First thing is to get the list of devices that are producing data
            // (and the parents they are connected to)
            TreeMap parentDeviceTree = null;
            DeviceAccessLocalHome devalh = null;
            DeviceAccessLocal deval = null;
            SQLDataStreamRawDataAccessLocalHome sdalh = null;
            SQLDataStreamRawDataAccessLocal sdal = null;
            try {
                sdalh =
                        SQLDataStreamRawDataAccessUtil.getLocalHome();
                sdal = sdalh.create();
                parentDeviceTree = sdal.getParentChildDataProducerTrees();
                devalh = DeviceAccessUtil.getLocalHome();
                deval = devalh.create();
            } catch (Exception e) {
            }
            if (parentDeviceTree != null) {

                // Now I can loop through and grab the child tree set for each parent
                Set parentKeys = parentDeviceTree.keySet();
                Iterator parentIter = parentKeys.iterator();
                int counter = 0;
                while (parentIter.hasNext()) {

                    // Grab the parent ID and the associated device
                    Long parentID = (Long) parentIter.next();

                    // Grab the associated device
                    Device parentDevice = null;
                    try {
                        parentDevice = (Device) deval.findById(parentID, false);
                    } catch (Exception e) {
                    }

                    // Create placeholders for various strings
                    String parentIDString = parentID.toString();
                    String parentDeviceName = null;

                    // Now try to fill those strings in
                    if (parentDevice != null) {
                        if (parentDevice.getName() != null) {
                            parentDeviceName = parentDevice.getName();
                        }
                    }

                    // Now build the grouping label
                    String parentGroupLabel = null;
                    if (parentDeviceName != null) {
                        parentGroupLabel = parentDeviceName + " (" +
                                parentID.longValue() + ")";
                    } else {
                        parentGroupLabel = "(" + parentID.longValue() + ")";
                    }

                    // Now build the label for the parent device entry
                    String parentDeviceLabel = null;
                    if (parentDeviceName != null) {
                        parentDeviceLabel = parentID.longValue() + " " +
                                parentDeviceName;
                    } else {
                        parentDeviceLabel = parentID.longValue() + "";
                    }

                    // Create the parent select item
                    devicePickList.append("<optgroup label=\"" + parentGroupLabel + "\">\n");
                    // Now the parent option
                    devicePickList.append("<option value=\"" + parentID + "\">" + parentDeviceLabel + "</option>\n");

                    // Now loop over the children and create those
                    TreeSet childTreeSet = (TreeSet) parentDeviceTree.get(parentID);
                    if ((childTreeSet != null) && (childTreeSet.size() > 0)) {

                        // Now iterate over the children and add the select items
                        Iterator childIter = childTreeSet.iterator();
                        int childCounter = 1;
                        while (childIter.hasNext()) {
                            // Grab the ID
                            Long childID = (Long) childIter.next();
                            // Grab the associated device
                            Device childDevice = null;
                            try {
                                childDevice = (Device) deval.findById(childID, false);
                            } catch (Exception e) {
                            }

                            // Create some place holders for label strings
                            String childIDString = childID.toString();
                            String childDeviceName = null;

                            // Now try to fill those strings in
                            if (childDevice != null) {
                                if (childDevice.getName() != null) {
                                    childDeviceName = childDevice.getName();
                                }
                            }

                            // Create the label string
                            String childDeviceLabel = null;
                            if (childDeviceName != null) {
                                childDeviceLabel = childID.longValue() + " " +
                                        childDeviceName;
                            } else {
                                childDeviceLabel = childID.longValue() + "";
                            }

                            // Now add the child as an option
                            devicePickList.append("<option value=\"" + childID + "\">" + childDeviceLabel + "</option>\n");
                        }
                    }
                    devicePickList.append("</optgroup>\n");
                }
            } else {
            }

            // Convert it to a options listing HTML string
            String devicePickListString = devicePickList.toString();

            // Grab a current calendar to set some defaults
            Calendar currentCalendar = Calendar.getInstance();
            int currentMonth = currentCalendar.get(Calendar.MONTH);
            currentMonth++;
            // Now create a default end time
            String defaultEndTime =
                    currentMonth + "/" +
                    currentCalendar.get(Calendar.DAY_OF_MONTH) + "/" +
                    currentCalendar.get(Calendar.YEAR) + " " +
                    currentCalendar.get(Calendar.HOUR_OF_DAY) + ":" +
                    currentCalendar.get(Calendar.MINUTE) + ":" +
                    currentCalendar.get(Calendar.SECOND);
            // Now create a default start time, by subtracting a month
            currentCalendar.add(Calendar.MONTH, -1);
            currentMonth = currentCalendar.get(Calendar.MONTH);
            currentMonth++;
            String defaultStartTime =
                    currentMonth + "/" +
                    currentCalendar.get(Calendar.DAY_OF_MONTH) + "/" +
                    currentCalendar.get(Calendar.YEAR) + " " +
                    currentCalendar.get(Calendar.HOUR_OF_DAY) + ":" +
                    currentCalendar.get(Calendar.MINUTE) + ":" +
                    currentCalendar.get(Calendar.SECOND);
            // Put up a form to ask for device
%>
<center>
<h3>Please enter the query information</h3>
</center>
<form name="querySetup" action="siamRawDataStep2.jsp" class="form">
<center>
<table cellpadding="2" border="2" cellspacing="1">
	<tr bgcolor="#CCCCCC">
		<td align="center" colspan="2"><b>Choose Device Or Enter
		Device ID:</b></td>
	<tr>
		<td align="center"><select name="deviceID">
			<%out.println(devicePickListString);%>
		</select></td>
		<td align="center"><input type="text" name="deviceIDEntered"
			size="20"></input></td>
	</tr>
	<tr bgcolor="#CCCCCC">
		<td colspan="2" align="center"><b>Choose data to display in
		the results</b></td>
	</tr>
	<tr>
		<td colspan="2" align="center">
		<table cellpadding="1" cellspacing="1" border="1">
			<tr>
				<td align="center">SIAM<br />
				Timestamp</td>
				<td align="center">Convert to<br />
				Time/Date</td>
				<td align="center">Packet<br />
				Sequence<br />
				Number</td>
				<td align="center">Parent ID</td>
				<td align="center">Record Type</td>
				<td align="center">
				<p>Packet Type<br />
				(Metadata,<br />
				Data,<br />
				Message)</p>
				</td>
				<td align="center">Size of Data<br />
				Buffer (# bytes)</td>
				<td align="center">Data<br />
				Buffer</td>
				<td align="center">Size of<br />
				Secondary Data<br />
				Buffer (# bytes)</td>
				<td align="center">Secondary<br />
				Data Buffer</td>
			</tr>
			<tr>
				<td align="center"><input type="checkbox"
					name="displayTimestamp" value="true" checked="true" /></td>
				<td align="center"><input type="checkbox" name="showDate"
					value="true" checked="true" /></td>
				<td align="center"><input type="checkbox"
					name="displaySequenceNumber" value="true" checked="true" /></td>
				<td align="center"><input type="checkbox"
					name="displayParentID" value="true" /></td>
				<td align="center"><input type="checkbox"
					name="displayRecordType" value="true" /></td>
				<td align="center"><input type="checkbox"
					name="displayPacketType" value="true" /></td>
				<td align="center"><input type="checkbox"
					name="showDataBufferSize" value="true" /></td>
				<td align="center"><input type="checkbox"
					name="displayDataBuffer" value="true" checked="true" /></td>
				<td align="center"><input type="checkbox"
					name="displaySizeOfSecondaryDataBuffer" value="true" /></td>
				<td align="center"><input type="checkbox"
					name="displaySecondaryDataBuffer" value="true" /></td>
			</tr>
			<tr>
				<td colspan="10">
				<center><b>Enter the RecordType if you want a specific
				one (leave empty if you want them all)</b> <br />
				<input type="text" size="5" name="recordType" /></center>
				</td>
			</tr>
		</table>
		</td>
	</tr>
	<tr bgcolor="#CCCCCC">
		<td colspan="2" align="center"><b>Choose Display Options</b></td>
	</tr>
	<tr>
		<td width="50%"><b>Sort Results By:</b></td>
		<td><input type="radio" name="tsORseq" value="timestamp"
			checked="true">Timestamp</input> <br />
		<input type="radio" name="tsORseq" value="sequenceNumber">Sequence
		Number</input></td>
	</tr>
	<tr>
		<td width="50%"><b>Choose Date Timezone and Format</b></td>
		<td><input type="radio" name="timezone" value="gmt"
			checked="true">GMT</input> <br />
		<input type="radio" name="timezone" value="pst">PST</input><br />
		<select name="dateTimeConversionFormat">
			<option value="yyyy-MM-dd HH:mm:ss Z" selected="true">yyyy-MM-dd
			HH:mm:ss Z</option>
			<option value="yyyy-MM-dd HH:mm:ss">yyyy-MM-dd HH:mm:ss</option>
			<option value="oasis">Oasis Style</option>
		</select></td>
	</tr>
	<tr>
		<td width="50%"><b>Display Results in HTML Table?</b></td>
		<td><input type="radio" name="htmlFormat" value="yes"
			checked="true">Yes</input> <br />
		<input type="radio" name="htmlFormat" value="no"> No</input> (<i>Output
		delimiter</i>) <select name="outputDelimiter">
			<option value="," selected="true">comma</option>
			<option value=":">colon</option>
			<option value=" ">space</option>
		</select></td>
	</tr>
	<tr>
		<td width="50%"><b>Convert Primary Data Buffer to ASCII Hex?</b></td>
		<td><input type="radio" name="convertToHex" value="yes">Yes</input>
		<br />
		<input type="radio" name="convertToHex" value="no" checked="true">No</input>
		</td>
	</tr>
	<tr>
		<td width="50%"><b>Convert Secondary Data Buffer to ASCII
		Hex?</b></td>
		<td><input type="radio" name="convertSecondaryToHex" value="yes">Yes</input>
		<br />
		<input type="radio" name="convertSecondaryToHex" value="no"
			checked="true">No</input></td>
	</tr>
	<tr>
		<td width="50%"><b>Attempt to Create Plots of Data?</b></td>
		<td><input type="radio" name="tryToGraph" value="yes">
		Yes</input> (<i>Data buffer delimiter</i>) <select name="graphDelimiter">
			<option value="comma" selected="true">comma</option>
			<option value="colon">colon</option>
			<option value="semicolon">semi-colon</option>
			<option value="space">whitespace</option>
		</select> <br />
		<input type="radio" name="tryToGraph" value="no" checked="true">No</input>
		</td>

	</tr>
	<tr bgcolor="#CCCCCC">
		<td colspan="2" align="center"><b>Select Time Window</b></td>
	</tr>
	<tr>
		<td><b>How do you want to select the timeframe?</b><br />
		<input type="radio" name="timeQueryType" value="lastNumPackets"
			checked="true">Last Number of Packets</input><br />
		<input type="radio" name="timeQueryType" value="startToHours"
			onclick="selectStartDateForward()">From <i>Start Time</i>
		forward (Use Hours Offset)</input><br />
		<input type="radio" name="timeQueryType" value="hoursToEnd"
			onclick="selectEndDateBack()">From <i>End Time</i> back (Use
		Hours Offset)</input><br />
		<input type="radio" name="timeQueryType" value="startToEnd"
			onclick="selectStartToEndDate()">Start Time to End Time</input></td>
		<td><b>Query Criteria (<font color="#FF3366">Times in
		GMT</font>)</b><br />
		<input type="text" size="20" name="lastNumPackets" value="10"></input><i>Number
		of Packets Back</i><br />
		<input type="text" size="20" name="startTime"
			value="<% out.println(defaultStartTime);%>"></input><a
			name="startCalAnchor" href="javascript:startCal.popup();"><img
			src="images/cal.gif" width="16" height="16" border="0"
			alt="Click to select start time"></img></a> <i>Start Time</i><br />
		<input type="text" size="20" name="endTime"
			value="<% out.println(defaultEndTime);%>"></input><a
			name="endCalAnchor" href="javascript:endCal.popup();"><img
			src="images/cal.gif" width="16" height="16" border="0"
			alt="Click to select end time"></img></a> <i>End Time</i><br />
		<input type="text" size="5" name="hoursOffset" value="24"> </input> <i>Hours
		(From Start/End Times)</i></td>
	</tr>
	</tr>
</table>
<p></p>
<input type="submit" name="querySetupSubmit" value="Next -->" /></center>
</form>
<script language="JavaScript">
                            <!--
                            var selectedTimeQuery = 0;
                            for (var i = 0; i < document.forms['querySetup'].timeQueryType.length; i++) {
                                if (document.forms['querySetup'].timeQueryType[i].checked) selectedTimeQuery = i;
                            }
                            var startCal = new calendar2(document.forms['querySetup'].elements['startTime']);
                            startCal.year_scroll = true;
                            startCal.time_comp = true;
                            var endCal = new calendar2(document.forms['querySetup'].elements['endTime']);
                            endCal.year_scroll = true;
                            endCal.time_comp = true;
                            if (document.forms['querySetup'].timeQueryType[selectedTimeQuery].value == "startToHours") {
                                document.forms['querySetup'].elements['startTime'].disabled=false;
                                document.forms['querySetup'].elements['endTime'].disabled=true;
                                document.forms['querySetup'].elements['hoursOffset'].disabled=false;
                                document.startCalAnchor.href="javascript:startCal.popup()";
                                document.endCalAnchor.href="javascript:void(0)";
                            }
                            if (document.forms['querySetup'].timeQueryType[selectedTimeQuery].value == "hoursToEnd") {
                                document.forms['querySetup'].elements['startTime'].disabled=true;
                                document.forms['querySetup'].elements['endTime'].disabled=false;
                                document.forms['querySetup'].elements['hoursOffset'].disabled=false;
                                document.startCalAnchor.href="javascript:void(0)";
                                document.endCalAnchor.href="javascript:endCal.popup()";
                            }
                            if (document.forms['querySetup'].timeQueryType[selectedTimeQuery].value == "startToEnd") {
                                document.forms['querySetup'].elements['startTime'].disabled=false;
                                document.forms['querySetup'].elements['endTime'].disabled=false;
                                document.forms['querySetup'].elements['hoursOffset'].disabled=true;
                                document.startCalAnchor.href="javascript:startCal.popup()";
                                document.endCalAnchor.href="javascript:endCal.popup()";
                            }
                            -->
                        </script>
<!-- Include the MBARI specific footer -->
<%@ include file="WEB-INF/fragments/mbari_footer.jspf"%>

<div class="clear" />
</body>
</html>
