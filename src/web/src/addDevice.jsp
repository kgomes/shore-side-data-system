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
<head>
<title>Device Creation Results</title>
<!-- Include the mbari stylesheets -->
<%@ include file="WEB-INF/fragments/mbari_styles.jspf"%>
</head>
<body id="body">
	<!-- Include the MBARI specific headers -->
	<%@ include file="WEB-INF/fragments/mbari_head.jspf"%>
	<div align="center" id="pagetitle">Create New Device</div>
	<%
		// Grab the URL parameters
		String name = request.getParameter("name");
		String description = request.getParameter("description");
		String type = request.getParameter("type");
		String mfgName = request.getParameter("mfgName");
		String mfgModel = request.getParameter("mfgModel");
		String mfgSerialNumber = request.getParameter("mfgSerialNumber");
		String uuid = request.getParameter("uuid");
		String person = request.getParameter("person");
		String deviceType = request.getParameter("type");
		// A boolean to indicate that the parameters are all OK
		boolean paramsOK = true;
		StringBuffer paramsErrorMessage = new StringBuffer();
		paramsErrorMessage.append("<ol>");
		// Check email address
		if ((name == null) || (name.compareTo("") == 0)) {
			paramsErrorMessage
					.append("<li>No device name was entered</li>");
			paramsOK = false;
		}
		if ((person == null) || (person.compareTo("") == 0)) {
			paramsErrorMessage.append("<li>No person was specified</li>");
			paramsOK = false;
		}
		paramsErrorMessage.append("</ol>");
		// If the parameters are OK, continue on with the processing
		if (paramsOK) {
			// A Properties object that will contain the properties to access SSDS	
			Properties properties = new Properties();

			// The DeviceAccessLocal service
			DeviceAccessLocal deviceAccessLocal = null;

			// The DeviceTypeAccess service
			DeviceTypeAccessLocal deviceTypeAccessLocal = null;

			// The PersonAccessLocal service
			PersonAccessLocal personAccessLocal = null;
			// The naming service for the container
			Context jndiContext = null;

			// Get the SSDS Services
			try {
				InitialContext ctx = new InitialContext();
				deviceAccessLocal = (DeviceAccessLocal) ctx
						.lookup("moos/ssds/services/metadata/DeviceAccessLocal");
				deviceTypeAccessLocal = (DeviceTypeAccessLocal) ctx
						.lookup("moos/ssds/services/metadata/DeviceTypeAccessLocal");
				personAccessLocal = (PersonAccessLocal) ctx
						.lookup("moos/ssds/services/metadata/PersonAccessLocal");
			} catch (NamingException e) {
				out.println("NamingException: Could not find the SSDS Services: "
						+ e.getMessage());
			}
			// Check to see if the home interfaces were found
			if ((personAccessLocal != null) && (deviceAccessLocal != null)) {

				// First try to find a device with the input name and manufacturing info
				Collection devices = null;
				try {
					devices = deviceAccessLocal.findByNameAndMfgInfo(name,
							true, mfgName, true, mfgModel, true,
							mfgSerialNumber, true, null, null, false);
				} catch (Exception dae) {
				}
				// If the collection was null or empty, go ahead and insert a new device
				if ((devices == null) || (devices.size() <= 0)) {
					// Find the person object that was specified in the list
					Person p = null;
					try {
						p = (Person) personAccessLocal.findById(person,
								false);
					} catch (Exception dae) {
						out.println("<h2>Exception " + dae.getMessage()
								+ "</h2>");
					}
					// Find the device type as well
					DeviceType dt = null;
					if ((deviceType != null) && (!deviceType.equals(""))) {
						try {
							dt = (DeviceType) deviceTypeAccessLocal
									.findById(deviceType, false);
						} catch (Exception dae) {
						}
					}
					// Now create the new device and add it to the database
					Device d = new Device();
					boolean problemWithFormData = false;
					String errorMessage = null;
					try {
						d.setName(name);
						d.setDescription(description);
						d.setDeviceType(dt);
						d.setMfgName(mfgName);
						d.setMfgModel(mfgModel);
						d.setMfgSerialNumber(mfgSerialNumber);
						if ((uuid != null) && (!uuid.equals("")))
							d.setUuid(uuid);
						d.setPerson(p);
					} catch (Exception e) {
						errorMessage = e.getMessage();
						problemWithFormData = true;
					}

					if (problemWithFormData) {
						out.println("<h2>There was a problem with the data that you entered, please go back and try again: "
								+ errorMessage + "</h2>");
					} else {
						// Now persist it
						Long deviceIDReturned = null;
						try {
							deviceIDReturned = deviceAccessLocal
									.makePersistent(d);
						} catch (Exception dae) {
							out.println("<h2>Exception caught trying to insert new device: "
									+ dae.getMessage() + "</h2>");
							dae.printStackTrace();
						}
						// If we got here, it should have worked!
						out.println("<b>Success!! Please record the device ID listed below as that is the key to having your device recognized in the system!!</b>");
						out.println("<br>The following information was entered in the database:<br>");
						out.println("<ul>");
						out.println("<li><b>Device ID = " + d.getId()
								+ "</b></li>");
						out.println("<li><b>Device UUID = " + d.getUuid()
								+ "</b></li>");
						out.println("<li>Device Name = " + d.getName()
								+ "</li>");
						out.println("<li>Device Description = "
								+ d.getDescription() + "</li>");
						if (d.getDeviceType() != null)
							out.println("<li>Device Type = "
									+ d.getDeviceType().getName() + "</li>");
						out.println("<li>Manufacturer Name = "
								+ d.getMfgName() + "</li>");
						out.println("<li>Manufacturer Model = "
								+ d.getMfgModel() + "</li>");
						out.println("<li>Manufacturer Serial Number = "
								+ d.getMfgSerialNumber() + "</li>");
					}
				} else {
					out.println("<b>A device with name "
							+ name
							+ ", manufacturer name of "
							+ mfgName
							+ ", manufacturer model of "
							+ mfgModel
							+ ", and manufacturer serial number of "
							+ mfgSerialNumber
							+ " already exists, so a new Device was not added to SSDS</b>");
				}
			}
		}
	%>
	<center></center>
	</div>
	</tr>
	<!-- Include the MBARI specific footer -->
	<%@ include file="WEB-INF/fragments/mbari_footer.jspf"%>

	<div class="clear" />
</body>
</html>