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
	<div align="center" id="pagetitle">Listing of Device Types
		Registered in SSDS</div>
	<center>
		<table width="800" cellpadding="2" border="2" cellspacing="1">
			<tr nowrap>
				<th>Name</th>
				<th>Description</th>
			</tr>
			<%
				// The DeviceTypeAccessLocal service
				DeviceTypeAccessLocal deviceTypeAccessLocal = null;

				// Get the SSDS services from the container
				try {
					InitialContext ctx = new InitialContext();
					deviceTypeAccessLocal = (DeviceTypeAccessLocal) ctx
							.lookup("moos/ssds/services/metadata/DeviceTypeAccessLocal");
				} catch (NamingException e) {
					out.println("NamingException: Could not get the SSDS services: "
							+ e.getMessage());
				}
				if (deviceTypeAccessLocal != null) {
					// Grab all the devices from the SSDS database
					Collection results = null;
					try {
						results = deviceTypeAccessLocal
								.findAll("name", "asc", true);
					} catch (Exception e) {
					}
					// Now build the pick list
					if (results != null) {
						Iterator i = results.iterator();
						while (i.hasNext()) {
							DeviceType deviceType = (DeviceType) i.next();
							out.println("<tr nowrap>");
							out.println("<td>" + deviceType.getName() + "</td>");
							out.println("<td>" + deviceType.getDescription()
									+ "</td>");
							out.println("</tr>");
						}
					}
				} else {
					out.println("Could not get SSDS services");
				}
			%>
		</table>
		Click <a href="newDeviceType.jsp">here</a> to enter a new device type
	</center>
	<!-- Include the MBARI specific footer -->
	<%@ include file="WEB-INF/fragments/mbari_footer.jspf"%>

	<div class="clear" />
</body>
</html>