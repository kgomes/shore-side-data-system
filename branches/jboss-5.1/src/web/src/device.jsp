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
	<div align="center" id="pagetitle">Listing of Devices Registered
		in SSDS</div>
	<center>
		<table width="800" cellpadding="2" border="2" cellspacing="1">
			<tr nowrap>
				<th>DeviceID</th>
				<th>Name</th>
				<th>Model</th>
				<th>Serial #</th>
				<th>Description</th>
				<th>Type</th>
				<th>Contact</th>
			</tr>
			<%
				// The DeviceAccessLocal service
				DeviceAccessLocal deviceAccessLocal = null;

				// Get the SSDS services from the container
				try {
					InitialContext ctx = new InitialContext();
					deviceAccessLocal = (DeviceAccessLocal) ctx
							.lookup("moos/ssds/services/metadata/DeviceAccessLocal");
				} catch (NamingException e) {
					out.println("NamingException: Could not get the SSDS services: "
							+ e.getMessage());
				}
				if (deviceAccessLocal != null) {
					// Grab all the devices from the SSDS database
					Collection results = null;
					try {
						results = deviceAccessLocal.findAll("id", "asc", true);
					} catch (Exception e) {
					}

					// Now build the pick list
					if (results != null) {
						// Create a treemap for sorting by ID in case they did not come back that way
						TreeMap<Long, Device> sortedDevices = new TreeMap<Long, Device>();
						Iterator<Device> deviceIterator = (Iterator<Device>) results
								.iterator();
						while (deviceIterator.hasNext()) {
							Device deviceToAdd = deviceIterator.next();
							sortedDevices.put(deviceToAdd.getId(), deviceToAdd);
						}

						// Now print them out
						Iterator<Long> idIterator = sortedDevices.keySet()
								.iterator();
						while (idIterator.hasNext()) {
							Device device = sortedDevices.get(idIterator.next());
							DeviceType deviceType = device.getDeviceType();
							Person person = device.getPerson();
							out.println("<tr nowrap>");
							out.println("<td>" + device.getId() + "</td>");
							out.println("<td>" + device.getName() + "</td>");
							out.println("<td>" + device.getMfgModel() + "</td>");
							out.println("<td>" + device.getMfgSerialNumber()
									+ "</td>");
							out.println("<td>" + device.getDescription() + "</td>");
							if (deviceType != null) {
								out.println("<td>" + deviceType.getName() + "</td>");
							} else {
								out.println("<td></td>");
							}
							if (person != null) {
								if (person.getEmail() != null) {
									out.println("<td>" + person.getFirstname()
											+ " " + person.getSurname() + "</td>");
								} else {
									out.println("<td></td>");
								}
							} else {
								out.println("<td></td>");
							}
							out.println("</tr>");
						}
					}
				} else {
					out.println("Some of the SSDS services homes were null");
				}
			%>
		</table>
		Click <a href="newDevice.jsp">here</a> to enter a new device and
		retrieve an ID for that device
	</center>
	<!-- Include the MBARI specific footer -->
	<%@ include file="WEB-INF/fragments/mbari_footer.jspf"%>

	<div class="clear" />
</body>
</html>