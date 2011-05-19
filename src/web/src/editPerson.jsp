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
            <div id="sectiontitle">Person Edit Page</div>

        </td></tr>
        <!--end header-->
        <tr>
            <!--begin content cell-->
            <td id="content_cell" align="left" valign="top" colspan="3">
            <div id="content">
                <div align="center" id="pagetitle">Edit the person's information</div>

                <%
                // Grab the personID from the query string
                String personID = request.getParameter("personID");
                String personEmail = request.getParameter("personEmail");
                // Now make sure it is there
                if (((personID == null) || (personID.compareTo("") == 0)) && ((personEmail == null) || (personEmail.compareTo("") == 0))) {
                %>
                <form name="editPerson" method="post" action="editPerson.jsp">
                    <center>
                        No ID or Email address was specified, please input one of the following
                        <table width="50%" border="3" cellspacing="1" cellpadding="1">
                            <tr><td>&nbsp;<strong>Person ID</strong></td>
                                <td><input type="text" name="personID"></td>
                            </tr>
                            <tr><td>&nbsp;<strong>Person Email</strong></td>
                                <td><input type="text" name="personEmail"></td>
                            </tr>
                        </table>
                        <input type="submit" name="Submit" value="Get Person To Edit">
                    </center>
                </form>
                <%
                } else {
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
                    // OK, so now get the person requested and populate the form.  First
                    // try by ID, then by Email
                    Person person = null;
                    if (personAccessLocal != null) {
                        try {
                            person = (Person) personAccessLocal.findById(personID,false);
                        } catch (Exception e) {
                        }
                        if (person == null) {
                            try {
                                Collection persons = personAccessLocal.findByEmail(personEmail, true, null, null, false);
                                if ((persons != null) && (persons.size() > 0)) {
                                    person = (Person) persons.iterator().next();
                                }
                            } catch (Exception e) {
                            }
                        }
                    }
                    // If person still null, have user try again
                    if (person == null) {
                %>
                <form name="editPerson" method="post" action="editPerson.jsp">
                    <center>
                        Could not find a person matching the query, please try again
                        <table width="50%" border="3" cellspacing="1" cellpadding="1">
                            <tr><td>&nbsp;<strong>Person ID</strong></td>
                                <td><input type="text" name="personID"></td>
                            </tr>
                            <tr><td>&nbsp;<strong>Person Email</strong></td>
                                <td><input type="text" name="personEmail"></td>
                            </tr>
                        </table>
                        <input type="submit" name="Submit" value="Get Person To Edit">
                    </center>
                </form>
                <%
                } else {
                %>
                <center>
                    <form name="editPerson" method="post" action="updatePerson.jsp">
                        <input type="hidden" name="personID" value="<% out.print(person.getId()); %>"></input>
                        <table width="50%" border="3" cellspacing="1" cellpadding="1">
                            <tr>
                                <td colspan="2"><div align="center"><strong><em>Edit the person's information</em></strong></div></td>
                            </tr>
                            <tr>
                                <td>&nbsp;<strong>Person ID</strong></td>
                                <td><%out.print(person.getId());%></td>
                            </tr>
                            <tr>
                                <td>&nbsp;<strong>First Name</strong></td>
                                <td><input type="text" name="firstname" value="<% out.print(person.getFirstname()); %>"></td>
                            </tr>
                            <tr>
                                <td>&nbsp;<strong>Last Name</strong></td>
                                <td><input type="text" name="surname" value="<% out.print(person.getSurname()); %>"></td>
                            </tr>
                            <tr>
                                <td>&nbsp;<strong>Organization</strong></td>
                                <td><input type="text" name="organization" value="<% out.print(person.getOrganization()); %>"></td>
                            </tr>
                            <tr>
                                <td>&nbsp;<strong>Username</strong></td>
                                <td><% out.print(person.getUsername()); %></td>
                            </tr>
                            <tr>
                                <td>&nbsp;<strong>Email</strong></td>
                                <td><input type="text" name="email" value="<% out.print(person.getEmail()); %>"></td>
                            </tr>
                            <tr>
                                <td>&nbsp;<strong>Phone</strong></td>
                                <td><input type="text" name="phone" value="<% out.print(person.getPhone()); %>"></td>
                            </tr>
                            <tr>
                                <td>&nbsp;<strong>Address 1</strong></td>
                                <td><input type="text" name="address1" value="<% out.print(person.getAddress1()); %>"></td>
                            </tr>
                            <tr>
                                <td>&nbsp;<strong>Address 2</strong></td>
                                <td><input type="text" name="address2" value="<% out.print(person.getAddress2()); %>"></td>
                            </tr>
                            <tr>
                                <td>&nbsp;<strong>City</strong></td>
                                <td><input type="text" name="city" value="<% out.print(person.getCity()); %>"></td>
                            </tr>
                            <tr>
                                <td>&nbsp;<strong>State</strong></td>
                                <td><input type="text" name="state" value="<% out.print(person.getState()); %>"></td>
                            </tr>
                            <tr>
                                <td>&nbsp;<strong>Zip</strong></td>
                                <td><input type="text" name="zipcode" value="<% out.print(person.getZipcode()); %>"></td>
                            </tr>
                        </table>

                        <input type="submit" name="Submit" value="Update Person Information">
                    </form>
                </center>

                <%
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