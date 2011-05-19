<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    	<%@ page import="java.io.*,java.util.*,moos.ssds.metadata.*,moos.ssds.services.metadata.*,javax.naming.*" %>
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
<div id="sectiontitle">Person Updated</div>

</td></tr>
<!--end header-->
<tr>
<!--begin content cell-->
<td id="content_cell" align="left" valign="top" colspan="3">
<div id="content">
<div align="center" id="pagetitle">Person Updated</div>

<%
	// Grab the person's information
	String personID = request.getParameter("personID");
	String firstname = request.getParameter("firstname");
	String surname = request.getParameter("surname");
	String organization = request.getParameter("organization");
	String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String address1 = request.getParameter("address1");
        String address2 = request.getParameter("address2");
        String city = request.getParameter("city");
        String state = request.getParameter("state");
        String zipcode = request.getParameter("zipcode");
	
	// Now make sure it is there
	if ((personID == null) || (personID.compareTo("") == 0)) {
%>
	<Center><B>The person ID was not in the query string, please go back and try again</B></center>
<%
	} else {
		boolean updateOK = true;
		// The LocalHome interface to the personAccess service
		PersonAccessLocalHome personAccessLocalHome = null;
		// The PersonAccessLocal service
		PersonAccessLocal personAccessLocal = null;

		// Get the SSDS services from the container
		try {
			personAccessLocalHome = PersonAccessUtil.getLocalHome();
		} catch (NamingException e) {
			out.println("NamingException: Could not get the SSDS services: " + e.getMessage());
		}
		if (personAccessLocalHome != null) {
			// Get the service objects
			try {
				personAccessLocal = personAccessLocalHome.create();
			} catch (Exception e) {
			}
		}
		// OK, so now get the person requested from the service
		Person person = null;
		if (personAccessLocal != null) {
			try {
				person = (Person) personAccessLocal.findById(personID,false);
			} catch (Exception e) {
				updateOK = false;
			}
			if (person != null) {
				person.setFirstname(firstname);
				person.setSurname(surname);
				person.setOrganization(organization);
				person.setEmail(email);
                                person.setPhone(phone);
                                person.setAddress1(address1);
                                person.setAddress2(address2);
                                person.setCity(city);
                                person.setState(state);
                                person.setZipcode(zipcode);
				try {
					personAccessLocal.update(person);
				} catch (Exception e) {
					updateOK = false;
				}
			} else {
				updateOK = false;
			}
		}
		if (updateOK) {
			out.println("<center><b>Update successful</b></center>");
		} else {
			out.println("<center><b><font color=\"red\">Update NOT successful</font></b></center>");
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