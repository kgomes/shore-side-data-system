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
	<div align="center" id="pagetitle">Person Add Results</div>

	<%
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
			PersonAccessLocal personAccessLocal = null;
			try {
				InitialContext ctx = new InitialContext();
				personAccessLocal = (PersonAccessLocal) ctx
						.lookup("moos/ssds/services/metadata/PersonAccessLocal");
			} catch (NamingException e) {
				out.println("NamingException: Could not find the personAccessHome interface: "
						+ e.getMessage());
			}
			// Make sure the service object is not null
			Person p = null;
			if (personAccessLocal != null) {
				// Now try to find the person by the email
				try {
					p = (Person) personAccessLocal.findByUsername(username,
							false);
				} catch (Exception dae) {
				}
				if (p == null) {
					try {
						Collection persons = personAccessLocal.findByEmail(
								email, true, null, null, false);
						if ((persons != null) && (persons.size() > 0)) {
							p = (Person) persons.iterator().next();
						}
					} catch (Exception dae) {
					}
				}
			}
			// If the person is null, then no person with that email exists and you can create a new one
			if (p == null) {
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
				// Now write the new person to the database
				try {
					personAccessLocal.makePersistent(newPerson);
				} catch (Exception ex) {
					out.println("<b>An exception was thrown while trying to persist the new person<font color=\"red\">"
							+ ex.getMessage() + "</font></b><br>");
				}
				// Now print out the message
				out.println("<center><h5>Success!! The following information was entered in the database:</h5></center>");
				out.println("<ul>");
				out.println("<li>First Name = " + firstName + "</li>");
				out.println("<li>Last Name = " + lastName + "</li>");
				out.println("<li>Organization = " + organization + "</li>");
				out.println("<li>Status = " + status + "</li>");
				out.println("<li>Email = " + email + "</li>");
				out.println("</ul>");
			} else {
				out.println("<b>A person with that email or username already exists, so this new person was NOT created</b>");
			} // End else there was somebody in the database with that email address
		} else {
			out.println("<h2>The parameters were not filled out correctly:</h2><br>");
			out.println(paramsErrorMessage.toString());
		} // End else the parameters were not OK
	%>
	<center>
		Click <a href="person.jsp">here</a> to see a list of all people in the
		SSDS Database
	</center>
	<!-- Include the MBARI specific footer -->
	<%@ include file="WEB-INF/fragments/mbari_footer.jspf"%>

	<div class="clear" />
</body>
</html>