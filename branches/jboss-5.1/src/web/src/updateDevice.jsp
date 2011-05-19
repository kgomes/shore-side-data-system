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
<!--begin whole page table-->
<table id="pagetable" border="0" cellspacing="0" cellpadding="0" align="center">
<!--begin header-->
<%@ include file="assets/includes/no_nav_interior_header.html" %>

<tr>
	<td align="right" valign="top" height="53">

<!--provide section title here -->
<div id="sectiontitle">Device Update Results</div>

</td></tr>
<!--end header-->
<tr>
<!--begin content cell-->
<td id="content_cell" align="left" valign="top" colspan="3">
<div id="content">
<div align="center" id="pagetitle">Device Update Results</div>

<%
	// Grab the device information
	String deviceID = request.getParameter("deviceID");
	String name = request.getParameter("name");
	String description = request.getParameter("description");
	String mfgName = request.getParameter("mfgName");
	String mfgModel = request.getParameter("mfgModel");
	String mfgSerialNumber = request.getParameter("mfgSerialNumber");
        String uuid = request.getParameter("uuid");
	String type = request.getParameter("type");
	String person = request.getParameter("person");
	
	// Now make sure it is there
	if ((deviceID == null) || (deviceID.compareTo("") == 0)) {
%>
	<Center><B>The device ID was not in the query string, please go back and try again</B></center>
<%
	} else {
		boolean updateOK = true;
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
		// OK, so now get the device requested from the service
		Device device = null;
		try {
			device = (Device) deviceAccessLocal.findById(deviceID,true);
		} catch (Exception e) {
			updateOK = false;
		}
		Person foundPerson = null;
		try {
			foundPerson = (Person) personAccessLocal.findById(person,false);
		} catch (Exception e) {
			updateOK = false;
		}
		DeviceType foundDeviceType = null;
		try {
			foundDeviceType = (DeviceType) deviceTypeAccessLocal.findById(type,false);
		} catch (Exception e) {
		}
		device.setName(name);
		device.setDescription(description);
		device.setMfgName(mfgName);
		device.setMfgModel(mfgModel);
		device.setMfgSerialNumber(mfgSerialNumber);
                device.setUuid(uuid);
		device.setPerson(foundPerson);
		device.setDeviceType(foundDeviceType);
		try {
			deviceAccessLocal.update(device);
		} catch(Exception e) {
			out.println("Something went wrong while updating");
			updateOK = false;
		}
		if (updateOK) {
			out.println("<center><B>Device information should be updated</B></center>");
		} else {
			out.println("<center><B><font color=\"red\">Device information was NOT updated successfully</font></B></center>");
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