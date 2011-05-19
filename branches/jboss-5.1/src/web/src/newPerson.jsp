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
<title>Create New Person in the SSDS</title>
<!-- Include the mbari stylesheets -->
<%@ include file="WEB-INF/fragments/mbari_styles.jspf"%>
</head>
<body id="body">
	<!-- Include the MBARI specific headers -->
	<%@ include file="WEB-INF/fragments/mbari_head.jspf"%>
	<div align="center" id="pagetitle">Create New Person</div>
	<center>
		<form name="newPersonForm" method="post" action="addPerson.jsp">
			<table width="50%" border="3" cellspacing="1" cellpadding="1">
				<tr>
					<td colspan="2"><div align="center">
							<strong><em>Enter the New Person's Information</em> </strong>
						</div></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>First Name</strong></td>
					<td><input type="text" name="firstName"></input></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>Last Name</strong></td>
					<td><input type="text" name="lastName"></input></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>Organization</strong></td>
					<td><input type="text" name="organization"></input></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>Username</strong> (required)</td>
					<td><input type="text" name="username"></input></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>Email</strong> (required)</td>
					<td><input type="text" name="email"></input></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>Phone</strong></td>
					<td><input type="text" name="phone"></input></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>Address 1</strong></td>
					<td><input type="text" name="address1"></input></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>Address 2</strong></td>
					<td><input type="text" name="address2"></input></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>City</strong></td>
					<td><input type="text" name="city"></input></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>State</strong></td>
					<td><input type="text" name="state"></input></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>Zip</strong></td>
					<td><input type="text" name="zipcode"></input></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>Status</strong></td>
					<td><select name="status"><option selected
								value="active">Active</option>
							<option value="inactive">Inactive</option>
					</select></td>
				</tr>
			</table>

			<input type="submit" name="Submit" value="Add Person"></input>
		</form>
	</center>
	<!-- Include the MBARI specific footer -->
	<%@ include file="WEB-INF/fragments/mbari_footer.jspf"%>

	<div class="clear" />
</body>
</html>