<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!-- Include JSP Setup code common to all pages -->
<%@ include file="WEB-INF/fragments/setup.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
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
<title>Login Failure</title>
<!-- Include the mbari stylesheets -->
<%@ include file="WEB-INF/fragments/mbari_styles.jspf"%>
</head>
<body>
<!-- Include the MBARI specific headers -->
<%@ include file="WEB-INF/fragments/mbari_head.jspf"%>
The system could not log you in. Please <a href="login.jsp">return</a> and try again.
<!-- Include the MBARI specific footer -->
<%@ include file="WEB-INF/fragments/mbari_footer.jspf"%>
</body>
</html>