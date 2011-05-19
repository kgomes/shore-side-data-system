<%@ page
	import="java.io.*,java.util.*,moos.ssds.metadata.*,moos.ssds.services.metadata.*,javax.naming.*,javax.ejb.*"%>
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
<title>Create New Device Type in the SSDS</title>
<!-- Include the mbari stylesheets -->
<%@ include file="WEB-INF/fragments/mbari_styles.jspf"%>
</head>
<body id="body">
	<!-- Include the MBARI specific headers -->
	<%@ include file="WEB-INF/fragments/mbari_head.jspf"%>
	<div align="center" id="pagetitle">Create New Device Type</div>
	<center>
		<form name="newDeviceTypeForm" method="post"
			action="addDeviceType.jsp">
			<table width="75%" border="3" cellspacing="1" cellpadding="1">
				<tr>
					<td colspan="2"><div align="center">
							<strong><em>Enter the New Device Type Information</em> </strong>
						</div></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>Device Type Name</strong></td>
					<td><input type="text" name="name" /></td>
				</tr>
				<tr>
					<td>&nbsp;<strong>Device Type Description</strong></td>
					<td><input type="text" name="description" /></td>
				</tr>
			</table>

			<input type="submit" name="Submit" value="Create Device Type"></input>
		</form>
	</center>
	<!-- Include the MBARI specific footer -->
	<%@ include file="WEB-INF/fragments/mbari_footer.jspf"%>

	<div class="clear" />
</body>
</html>