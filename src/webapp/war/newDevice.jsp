<%@ page
	import="java.io.*,java.util.*,moos.ssds.metadata.*,moos.ssds.services.metadata.*,javax.naming.*,javax.ejb.*"%>
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
<html>
<head>
<title>Create New Device in the SSDS</title>
<!-- Include the mbari stylesheets -->
<%@ include file="WEB-INF/fragments/mbari_styles.jspf"%>
</head>
<body id="body">
	<!-- Include the MBARI specific headers -->
	<%@ include file="WEB-INF/fragments/mbari_head.jspf"%>
	<div align="center" id="pagetitle">Create New Device</div>
	<%
		// Create a string buffer that will be used to populate a picklist of people
		StringBuffer personsInputBuffer = new StringBuffer();
		// Create a string buffer that will be used to populate a picklist of device types
		StringBuffer deviceTypeInputBuffer = new StringBuffer();
		// Give a blank entry in case the user's choice is not in the pick list
		deviceTypeInputBuffer
				.append("<option value=\"\" selected></option>");

		// The properties object that contains all the properties to access ssds
		Properties properties = new Properties();
		// The DeviceAccessLocal service
		DeviceAccessLocal deviceAccessLocal = null;
		// The DeviceTypesAccessLocal service
		DeviceTypeAccessLocal deviceTypeAccessLocal = null;
		// The PersonAccessLocal service
		PersonAccessLocal personAccessLocal = null;
		// Get the SSDS services from the container
		// Get the service objects
		try {
			Context context = new InitialContext();
			personAccessLocal = (PersonAccessLocal) context
					.lookup("moos/ssds/services/metadata/PersonAccessLocal");
			deviceAccessLocal = (DeviceAccessLocal) context
					.lookup("moos/ssds/services/metadata/DeviceAccessLocal");
			deviceTypeAccessLocal = (DeviceTypeAccessLocal) context
					.lookup("moos/ssds/services/metadata/DeviceTypeAccessLocal");
		} catch (Exception ex) {
		}

		// Grab all the persons from the SSDS database
		Collection results = null;
		try {
			results = personAccessLocal.findAll("surname", "asc", false);
		} catch (Exception dae) {
		}
		// Now build the pick list
		if (results != null) {
			Iterator i = results.iterator();
			while (i.hasNext()) {
				Person person = (Person) i.next();
				personsInputBuffer.append("<option value=\""
						+ person.getId() + "\">" + person.getSurname()
						+ ", " + person.getFirstname() + " ("
						+ person.getEmail() + ")</option>");
			}
		}
		// Now do the same for device types
		try {
			results = deviceTypeAccessLocal.findAll("name", "asc", false);
		} catch (Exception dae) {
		}

		// Build the pick list again
		if (results != null) {
			Iterator i = results.iterator();
			while (i.hasNext()) {
				DeviceType deviceType = (DeviceType) i.next();
				deviceTypeInputBuffer
						.append("<option value=\"" + deviceType.getId()
								+ "\">" + deviceType.getName() + " ("
								+ deviceType.getDescription()
								+ ")</option>");
			}
		}
	%>
	<center>
		<form name="newDeviceForm" method="post" action="addDevice.jsp">
			<table width="75%" border="3" cellspacing="1" cellpadding="1">
				<tr>
					<td colspan="2"><div align="center">
							<strong><em>Enter the New Device Information</em> </strong>
						</div></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>Device Name</strong></td>
					<td><input type="text" name="name" /></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>Device Description</strong></td>
					<td><input type="text" name="description" /></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>Manufacturer's Name</strong></td>
					<td><input type="text" name="mfgName" /></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>Manufacturer's Model</strong></td>
					<td><input type="text" name="mfgModel" /></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>Manufacturer's Serial Number</strong></td>
					<td><input type="text" name="mfgSerialNumber" /></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>UUID</strong> (SSDS will generate if empty)</td>
					<td><input type="text" name="uuid" /></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>Device Type</strong></td>
					<td><select name="type">
							<%
								out.print(deviceTypeInputBuffer.toString());
							%>
					</select></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>Person Responsible for Device</strong>
						(NOTE: If the person is not listed here, click <a
						href="newPerson.jsp" target="_blank">here</a> to add the person to
						the database and then refresh this page)</td>
					<td><select name="person" size="1">
							<%
								out.print(personsInputBuffer.toString());
							%>
					</select></td>
				</tr>
			</table>

			<input type="submit" name="Submit" value="Create Device">
		</form>
	</center>
	</div>
	</tr>
	<!-- Include the MBARI specific footer -->
	<%@ include file="WEB-INF/fragments/mbari_footer.jspf"%>

	<div class="clear" />
</body>
</html>