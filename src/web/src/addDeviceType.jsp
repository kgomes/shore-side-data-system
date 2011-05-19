<%@ page
	import="java.io.*,java.util.*,moos.ssds.metadata.*,javax.naming.*,moos.ssds.services.metadata.*,javax.ejb.*"%>
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
<title>Device Creation Results</title>
<!-- Include the mbari stylesheets -->
<%@ include file="WEB-INF/fragments/mbari_styles.jspf"%>
</head>
<body id="body">
	<!-- Include the MBARI specific headers -->
	<%@ include file="WEB-INF/fragments/mbari_head.jspf"%>
	<div align="center" id="pagetitle">Create New Device Type Results</div>
	<%
		// Grab the URL parameters
		String name = request.getParameter("name");
		String description = request.getParameter("description");

		// A boolean to indicate that the parameters are all OK
		boolean paramsOK = true;
		StringBuffer paramsErrorMessage = new StringBuffer();
		paramsErrorMessage.append("<ol>");
		// Check email address
		if ((name == null) || (name.compareTo("") == 0)) {
			paramsErrorMessage
					.append("<li>No device type name was entered</li>");
			paramsOK = false;
		}
		paramsErrorMessage.append("</ol>");
		// If the parameters are OK, continue on with the processing
		if (paramsOK) {
			// The DeviceTypeAccess service
			DeviceTypeAccessLocal deviceTypeAccessLocal = null;

			// Get the SSDS Services
			try {
				InitialContext ctx = new InitialContext();
				deviceTypeAccessLocal = (DeviceTypeAccessLocal) ctx
						.lookup("moos/ssds/services/metadata/DeviceTypeAccessLocal");
			} catch (NamingException e) {
				out.println("NamingException: Could not find the SSDS Services: "
						+ e.getMessage());
			}
			// Check to see if the home interfaces were found
			if (deviceTypeAccessLocal != null) {

				// First try to find a device type with the same name
				DeviceType deviceType = null;
				try {
					deviceType = deviceTypeAccessLocal.findByName(name,
							false);
				} catch (Exception dae) {
				}
				// If the collection was null or empty, go ahead and insert a new device
				if (deviceType == null) {
					// Now create the new device type and add it to the database
					deviceType = new DeviceType();
					boolean problemWithFormData = false;
					String errorMessage = null;
					try {
						deviceType.setName(name);
						deviceType.setDescription(description);
					} catch (Exception e) {
						errorMessage = e.getMessage();
						problemWithFormData = true;
					}

					if (problemWithFormData) {
						out.println("<h2>There was a problem with the data that you entered, please go back and try again: "
								+ errorMessage + "</h2>");
					} else {
						// Now persist it
						Long deviceTypeIDReturned = null;
						try {
							deviceTypeIDReturned = deviceTypeAccessLocal
									.makePersistent(deviceType);
						} catch (Exception dae) {
							out.println("<h2>Exception caught trying to insert new device type: "
									+ dae.getMessage() + "</h2>");
							dae.printStackTrace();
						}
						// If we got here, it should have worked!
						out.println("<center><h5>Success!! The following information was entered in the database:</h5></center>");
						out.println("<ul>");
						out.println("<li>Device Type Name = "
								+ deviceType.getName() + "</li>");
						out.println("<li>Device Type Description = "
								+ deviceType.getDescription() + "</li>");
						out.println("</ul>");
					}
				} else {
					out.println("<b>A device type with name "
							+ name
							+ " already exists, so a new Device type was <i>NOT</i> added to SSDS</b>");
				}
			}
		}
	%>
	<!-- Include the MBARI specific footer -->
	<%@ include file="WEB-INF/fragments/mbari_footer.jspf"%>

	<div class="clear" />
</body>
</html>