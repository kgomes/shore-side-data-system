<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="moos.ssds.metadata.*" %>
<%@ page import="moos.ssds.services.metadata.*" %>
<%@ page import="javax.naming.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <head>
        <script type="text/javascript" src="assets/javascripts/interior.js"></script>
        <link rel="stylesheet" type="text/css" href="assets/styles/no_nav_interior.css">
    </head>
    <body>
        <!--begin whole page table-->
        <table id="pagetable" border="0" cellspacing="0" cellpadding="0" align="center">
            <!--begin header-->
<%@ include file="assets/includes/no_nav_interior_header.html" %>
            <tr>
                <td align="right" valign="top" height="53">
                    <!--provide section title here -->
                    <div id="sectiontitle">Device Edit Page</div>
                </td>
            </tr>
            <!--end header-->
            <tr>
                <!--begin content cell-->
                <td id="content_cell" align="left" valign="top" colspan="3">
                    <div id="content">
                        <div align="center" id="pagetitle">Edit the device's information</div>
<%
// Create a string buffer that will be used to populate a picklist of people
StringBuffer personsInputBuffer = new StringBuffer();
// Create a string buffer that will be used to populate a picklist of device types
StringBuffer deviceTypeInputBuffer = new StringBuffer();
// Give a blank entry in case the user's choice is not in the pick list
deviceTypeInputBuffer.append("<option value=\"\" selected></option>");
// Grab the deviceID from the query string
String deviceID = request.getParameter("deviceID");
// Now make sure it is there
if ((deviceID == null) || (deviceID.compareTo("") == 0)) {
%>
<form name="editDevice" method="post" action="editDevice.jsp">
	<center>
	No Device ID was specified, please input the device ID to edit
	<table width="50%" border="3" cellspacing="1" cellpadding="1">
		<tr><td>&nbsp;<strong>Device ID</strong></td>
		<td><input type="text" name="deviceID"></td>
		</tr>
  	</table>
	<input type="submit" name="Submit" value="Get Device To Edit">
	</center>
</form>
<%
} else {
    // The LocalHome interface to the deviceAccess service
    DeviceAccessLocalHome deviceAccessLocalHome = null;
    // The DeviceAccessLocal service
    DeviceAccessLocal deviceAccessLocal = null;
    // The LocalHome interface to the deviceTypesAccess service
    DeviceTypeAccessLocalHome deviceTypeAccessLocalHome = null;
    // The DeviceTypesAccessLocal service
    DeviceTypeAccessLocal deviceTypeAccessLocal = null;
    // The LocalHome interface to the personAccess service
    PersonAccessLocalHome personAccessLocalHome = null;
    // The PersonAccessLocal service
    PersonAccessLocal personAccessLocal = null;

    // Get the SSDS services from the container
    try {
        deviceAccessLocalHome = DeviceAccessUtil.getLocalHome();
        deviceTypeAccessLocalHome = DeviceTypeAccessUtil.getLocalHome();
        personAccessLocalHome = PersonAccessUtil.getLocalHome();
    } catch (NamingException e) {
        out.println("NamingException: Could not get the SSDS services: " + e.getMessage());
    }
    if (deviceAccessLocalHome != null) {
        // Get the service objects
        try {
            deviceAccessLocal = deviceAccessLocalHome.create();
        } catch (Exception e) {
        }
    }
    if (deviceTypeAccessLocalHome != null) {
        // Get the service objects
        try {
            deviceTypeAccessLocal = deviceTypeAccessLocalHome.create();
        } catch (Exception e) {
        }
    }
    if (personAccessLocalHome != null) {
        // Get the service objects
        try {
            personAccessLocal = personAccessLocalHome.create();
        } catch (Exception e) {
        }
    }
    // OK, so now get the device requested and populate the form
    if ((deviceAccessLocal != null) && (deviceTypeAccessLocal != null) && (personAccessLocal != null)) {
        Device device = null;
        try {
            device = (Device) deviceAccessLocal.findById(deviceID,true);
        } catch (Exception e) {
        }
        if (device != null) {
            Person selectedPerson = device.getPerson();
            DeviceType selectedDeviceType = device.getDeviceType();
            // Grab all the persons from the SSDS database
            Collection results = null;
            try {
                results = personAccessLocal.findAll("surname","asc",false);
            } catch (Exception dae) {
            }
            // Now build the pick list
            if (results != null) {
                Iterator i = results.iterator();
                while (i.hasNext()) {
                    Person person = (Person)i.next();
                    personsInputBuffer.append("<option ");
                    if ((selectedPerson != null) && (selectedPerson.getId().longValue() == person.getId().longValue())) {
                        personsInputBuffer.append("selected ");
                    }
                    personsInputBuffer.append("value=\"" + person.getId() + "\">" + person.getSurname() + ", " + person.getFirstname() + " (" + person.getEmail() + ")</option>");
                }
            }
            // Now do the same for device types
            try {
                results = deviceTypeAccessLocal.findAll("name","asc",false);
            } catch (Exception dae) {
            }
            // Build the pick list again
            if (results != null) {
                Iterator i = results.iterator();
                while (i.hasNext()) {
                    DeviceType deviceType = (DeviceType)i.next();
                    deviceTypeInputBuffer.append("<option ");
                    if ((selectedDeviceType != null) && (selectedDeviceType.getId().longValue() == deviceType.getId().longValue())) {
                        deviceTypeInputBuffer.append("selected ");
                    }
                    deviceTypeInputBuffer.append("value=\"" + deviceType.getId() + "\">" + deviceType.getName()  + " (" + deviceType.getDescription() + ")</option>");
                }
            }
%>
<center>
<form name="editDevice" method="post" action="updateDevice.jsp">
	<input type="hidden" name="deviceID" value="<% out.print(device.getId()); %>"></input>
  <table width="50%" border="3" cellspacing="1" cellpadding="1">
  <tr>
        <td colspan="2"><div align="center"><strong><em>Edit the device's information</em></strong></div></td>
  </tr>
  <tr>
        <td>&nbsp;<strong>Device ID</strong></td>
    <td><%out.print(device.getId());%></td>
  </tr>
  <tr>
  <td>&nbsp;<strong>Name</strong></td>
    <td><input type="text" name="name" value="<% out.print(device.getName()); %>"></td>
  </tr>
  <tr>
        <td>&nbsp;<strong>Description</strong></td>
    <td><input type="text" size="75" name="description" value="<% out.print(device.getDescription()); %>"></td>
  </tr>
  <tr>
        <td>&nbsp;<strong>Manufacturer's Name</strong></td>
    <td><input type="text" name="mfgName" value="<% out.print(device.getMfgName()); %>"></td>
  </tr>
  <tr>
        <td>&nbsp;<strong>Manufacturer's Model</strong></td>
    <td><input type="text" name="mfgModel" value="<% out.print(device.getMfgModel()); %>"></td>
  </tr>
  <tr>
        <td>&nbsp;<strong>Manufacturer's Serial Number</strong></td>
    <td><input type="text" name="mfgSerialNumber" value="<% out.print(device.getMfgSerialNumber()); %>"></td>
  </tr>
  <tr>
        <td>&nbsp;<strong>UUID</strong></td>
    <td><input type="text" name="uuid" value="<% out.print(device.getUuid()); %>"></td>
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
        <td>&nbsp;<strong>Person Responsible for Device</strong> (NOTE: If the 
          person is not listed here, click 
          <a href="newPerson.jsp" target="_blank">here</a> to add the person 
          to the database and
          then refresh this page)</td>
    <td><select name="person" size="1">
<%
	out.print(personsInputBuffer.toString());
%>
	</select></td>
  </tr>
</table>

  <input type="submit" name="Submit" value="Update Device Information">
</form>
</center>

<%
        } else {
            out.println("<center><b>No device with ID " + deviceID + " was found</b></center>");
        }
    }
}
%>
</div>
</tr>
<!--begin footer-->
<%@ include file="assets/includes/no_nav_interior_footer.html" %>
<!--end footer-->
</table>
</html>