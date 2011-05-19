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
<body>
	<div id="content">

		<!-- Include the MBARI specific headers -->
		<%@ include file="WEB-INF/fragments/mbari_head.jspf"%>
		<div align="center" id="pagetitle">People in SSDS</div>

		<center>
			<table cellpadding="2" border="2" cellspacing="1">
				<tr nowrap>
					<!--	  <th>id</th> -->
					<th>Last Name</th>
					<th>First Name</th>
					<th>Organization</th>
				</tr>
				<%
					// The PersonAccess service
					PersonAccessLocal personAccessLocal = null;
					// Get the PersistenceBroker from the container
					try {
						InitialContext ctx = new InitialContext();
						personAccessLocal = (PersonAccessLocal) ctx
								.lookup("moos/ssds/services/metadata/PersonAccessLocal");
					} catch (NamingException e) {
						out.println("NamingException: Could not find the personAccessHome interface: "
								+ e.getMessage());
					}
					if (personAccessLocal != null) {
						Collection persons = null;
						try {
							persons = personAccessLocal
									.findAll("surname", "asc", false);
						} catch (Exception e) {
						}
						if (persons != null) {
							Iterator i = persons.iterator();
							while (i.hasNext()) {
								Person person = (Person) i.next();
								out.println("<tr nowrap>");
								out.println("<td>" + person.getSurname() + "</td>");
								out.println("<td>" + person.getFirstname() + "</td>");
								out.println("<td>" + person.getOrganization() + "</td>");
								out.println("</tr>");
							}
						}
					} else {
						out.println("personAccessLocal service was null");
					}
				%>
			</table>

			<p>
				Please click <a href="newPerson.jsp">here</a> if you need to enter a
				new person in the SSDS Database
			</p>
		</center>
	</div>
	<!-- Include the MBARI specific footer -->
	<%@ include file="WEB-INF/fragments/mbari_footer.jspf"%>

	<div class="clear" />
</body>
</html>