<%@ page import="java.io.*"%>
<!-- Include JSP Setup code common to all pages -->
<%@ include file="WEB-INF/fragments/setup.jspf"%>
<?xml version="1.0"?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title>Create New Person in the SSDS</title>
<!-- Include the mbari stylesheets -->
<%@ include file="WEB-INF/fragments/mbari_styles.jspf"%>
</head>
<body id="body">
	<%@ include file="WEB-INF/fragments/mbari_head.jspf"%>
	<div align="center" id="pagetitle">Create New Person</div>
	<!--begin whole page table-->
	<table id="pagetable" border="0" cellspacing="0" cellpadding="0"
		align="center">
		<tr>
			<td align="right" valign="top" height="53">
				<!--provide section title here -->
				<div id="sectiontitle">Person Creation Page</div></td>
		</tr>
		<!--end header-->
		<tr>
			<!--begin content cell-->
			<td id="content_cell" align="left" valign="top" colspan="3">
				<div id="content">
					<div align="center" id="pagetitle">Enter the new person's
						information</div>

					<center>
						<form name="newPersonForm" method="post" action="addPerson.jsp">
							<table width="50%" border="3" cellspacing="1" cellpadding="1">
								<tr>
									<td colspan="2"><div align="center">
											<strong><em>Enter the New Person's Information</em>
											</strong>
										</div>
									</td>
								</tr>
								<tr>
									<td>&nbsp;<strong>First Name</strong>
									</td>
									<td><input type="text" name="firstName">
									</td>
								</tr>
								<tr>
									<td>&nbsp;<strong>Last Name</strong>
									</td>
									<td><input type="text" name="lastName">
									</td>
								</tr>
								<tr>
									<td>&nbsp;<strong>Organization</strong>
									</td>
									<td><input type="text" name="organization">
									</td>
								</tr>
								<tr>
									<td>&nbsp;<strong>Username</strong> (required)</td>
									<td><input type="text" name="username">
									</td>
								</tr>
								<tr>
									<td>&nbsp;<strong>Email</strong> (required)</td>
									<td><input type="text" name="email">
									</td>
								</tr>
								<tr>
									<td>&nbsp;<strong>Phone</strong>
									</td>
									<td><input type="text" name="phone">
									</td>
								</tr>
								<tr>
									<td>&nbsp;<strong>Address 1</strong>
									</td>
									<td><input type="text" name="address1">
									</td>
								</tr>
								<tr>
									<td>&nbsp;<strong>Address 2</strong>
									</td>
									<td><input type="text" name="address2">
									</td>
								</tr>
								<tr>
									<td>&nbsp;<strong>City</strong>
									</td>
									<td><input type="text" name="city">
									</td>
								</tr>
								<tr>
									<td>&nbsp;<strong>State</strong>
									</td>
									<td><input type="text" name="state">
									</td>
								</tr>
								<tr>
									<td>&nbsp;<strong>Zip</strong>
									</td>
									<td><input type="text" name="zipcode">
									</td>
								</tr>
								<tr>
									<td>&nbsp;<strong>Status</strong>
									</td>
									<td><select name="status"><option selected
												value="active">Active</option>
											<option value="inactive">Inactive</option>
									</select>
									</td>
								</tr>
							</table>

							<input type="submit" name="Submit" value="Add Person">
						</form>
					</center>
				</div>
		</tr>
	</table>
</body>
</html>