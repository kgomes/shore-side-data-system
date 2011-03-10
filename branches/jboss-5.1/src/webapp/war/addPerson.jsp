<%@page import="org.apache.log4j.Logger"%>
<%@ page
	import="java.io.*,java.util.*,moos.ssds.metadata.*,moos.ssds.services.metadata.*,javax.naming.*"%>
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
	<!--begin whole page table-->
	<table id="pagetable" border="0" cellspacing="0" cellpadding="0"
		align="center">

		<tr>
			<td align="right" valign="top" height="53">
				<!--provide section title here -->
				<div id="sectiontitle">Person Edit Page</div></td>
		</tr>
		<!--end header-->
		<tr>
			<!--begin content cell-->
			<td id="content_cell" align="left" valign="top" colspan="3">
				<div id="content">
					<div align="center" id="pagetitle">Edit the person's
						information</div>

					<%
						// A log4j logger
						Logger logger = Logger.getLogger("moos.ssds.web.addPerson.jsp");
						// Grab all the parameters from the query string
						String firstName = request.getParameter("firstName");
						String lastName = request.getParameter("lastName");
						String organization = request.getParameter("organization");
						String username = request.getParameter("username");
						String email = request.getParameter("email");
						String phone = request.getParameter("phone");
						String address1 = request.getParameter("address1");
						String address2 = request.getParameter("address2");
						String city = request.getParameter("city");
						String zipcode = request.getParameter("zipcode");
						String state = request.getParameter("state");
						String status = request.getParameter("status");

						// A boolean to indicate that the parameters are all OK
						boolean paramsOK = true;
						// A string buffer to represent the errors in HTML
						StringBuffer paramsErrorMessage = new StringBuffer();
						// Add an opening ordered list tag
						paramsErrorMessage.append("<ol>");
						// Check email address
						if ((email == null) || (email.equals("")) || (username == null)
								|| (username.equals(""))) {
							paramsErrorMessage
									.append("<li>Either email or username were not entered, they are required</li>");
							paramsOK = false;
						}
						// Add closing list tag
						paramsErrorMessage.append("</ol>");
						// If the parameters are OK, continue on with the processing
						if (paramsOK) {
							// Grab the person services from SSDS
							Context context = new InitialContext();
							PersonAccessLocal personAccessLocal = null;
							try {
								personAccessLocal = (PersonAccessLocal) context
										.lookup("moos/ssds/services/metadata/PersonAccessLocal");
								logger.debug("PersonAccessLocal is " + personAccessLocal);
							} catch (Exception e) {
								out.println("<h3>Person access could not be created: <font color=\"red\">"
										+ e.getClass() + ": Message-> " + e.getMessage());
							}
							// Make sure the service object is not null
							Person p = null;
							if (personAccessLocal != null) {
								// Now try to find the person by the email
								logger.debug("Going to try and look up person by username "
										+ username);
								try {
									p = (Person) personAccessLocal.findByUsername(username,
											false);
								} catch (Exception dae) {
									logger.error("Exception (" + dae.getClass()
											+ ") trying to lookup by username: "
											+ dae.getMessage());
								}
								if (p == null) {
									logger.debug("Nobody with that username, will try by email "
											+ email);
									try {
										Collection persons = personAccessLocal.findByEmail(
												email, true, null, null, false);
										if ((persons != null) && (persons.size() > 0)) {
											p = (Person) persons.iterator().next();
										}
									} catch (Exception dae) {
										logger.debug("Exception ()" + dae.getClass()
												+ ") caught trying to lookup by email: "
												+ dae.getMessage());
									}
								}
							}
							// If the person is null, then no person with that email exists and you can create a new one
							if (p == null) {
								logger.debug("Person not found will create a new one");
								// Create a new person
								Person newPerson = new Person();
								// Set all the properties
								newPerson.setFirstname(firstName);
								newPerson.setSurname(lastName);
								newPerson.setOrganization(organization);
								newPerson.setUsername(username);
								newPerson.setEmail(email);
								newPerson.setPhone(phone);
								newPerson.setAddress1(address1);
								newPerson.setAddress2(address2);
								newPerson.setCity(city);
								newPerson.setState(state);
								newPerson.setZipcode(zipcode);
								newPerson.setStatus(status);
								logger.debug("Person (" + newPerson.getClass()
										+ ") created: "
										+ newPerson.toStringRepresentation("|"));
								boolean persistedOK = false;
								// Now write the new person to the database
								try {
									personAccessLocal.makePersistent(newPerson);
									persistedOK = true;
								} catch (Exception ex) {
									logger.error("Exception (" + ex.getClass()
											+ ") caught trying to persist newPerson: "
											+ ex.getMessage());
									out.println("<br><h3>An exception was thrown while trying to persist the new person: <font color=\"red\">"
											+ ex.getClass()
											+ ": Message-> "
											+ ex.getMessage() + "</font></h3><br>");
								}
								// Now print out the message
								if (persistedOK) {
									out.println("<b>Success!!</b><br>The following information was entered in the database:<br>");
									out.println("<ul>");
									out.println("<li>ID = " + newPerson.getId() + "</li>");
									out.println("<li>First Name = " + firstName + "</li>");
									out.println("<li>Last Name = " + lastName + "</li>");
									out.println("<li>Organization = " + organization
											+ "</li>");
									out.println("<li>Status = " + status + "</li>");
									out.println("<li>Email = " + email + "</li>");
								}
							} else {
								out.println("<b>A person with that email or username already exists, so this new person was NOT created</b>");
							} // End else there was somebody in the database with that email address
						} else {
							out.println("<h2>The parameters were not filled out correctly:</h2><br>");
							out.println(paramsErrorMessage.toString());
						} // End else the parameters were not OK
					%>
					<center>
						Click <a href="person.jsp">here</a> to see a list of all people in
						the SSDS Database
					</center>
				</div>
		</tr>
	</table>
</body>
</html>