<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
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
<title>MBARI's Shore Side Data System</title>
    <!-- Meta tags -->
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

    <!-- Stylesheet information -->
    <link rel="stylesheet" type="text/css" href="history/history.css" />
    <script type="text/javascript" src="history/history.js"></script>
    <script type="text/javascript" src="swfobject.js"></script>

<!-- Include the mbari stylesheets -->
<%@ include file="WEB-INF/fragments/mbari_styles.jspf"%>

	<script type="text/javascript">
    	<!-- For version detection, set to min. required Flash Player version, or 0 (or 0.0.0), for no version detection. --> 
        var swfVersionStr = "10.0.0";
        <!-- To use express install, set to playerProductInstall.swf, otherwise the empty string. -->
        var xiSwfUrlStr = "";
        var flashvars = {};
        var params = {};
        params.quality = "high";
        params.bgcolor = "white";
        params.allowscriptaccess = "sameDomain";
        params.allowfullscreen = "true";
        var attributes = {};
        attributes.id = "explorer";
        attributes.name = "explorer";
        attributes.align = "middle";
        swfobject.embedSWF(
            "explorer.swf", "flashContent", 
            "1024", "768", 
            swfVersionStr, xiSwfUrlStr, 
            flashvars, params, attributes);
		<!-- JavaScript enabled so display the flashContent div in case it is not replaced with a swf object. -->
		swfobject.createCSS("#flashContent", "display:block;text-align:left;");
	</script>
</head>

<body>
<%@ include file="WEB-INF/fragments/mbari_head.jspf"%>
	<!-- SWFObject's dynamic embed method replaces this alternative HTML content with Flash content when enough 
		 JavaScript and Flash plug-in support is available. The div is initially hidden so that it doesn't show
		 when JavaScript is disabled.
	-->
    <div id="flashContent">
     	<p>
	       	To view this page ensure that Adobe Flash Player version 
			10.0.0 or greater is installed. 
		</p>
		<script type="text/javascript"> 
			var pageHost = ((document.location.protocol == "https:") ? "https://" :	"http://"); 
			document.write("<a href='http://www.adobe.com/go/getflashplayer'><img src='" 
							+ pageHost + "www.adobe.com/images/shared/download_buttons/get_flash_player.gif' alt='Get Adobe Flash player' /></a>" ); 
		</script> 
    </div>
	   	
    <noscript>
        <object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" width="100%" height="100%" id="explorer" align="middle">
            <param name="movie" value="explorer.swf" />
            <param name="quality" value="high" />
            <param name="bgcolor" value="white" />
            <param name="allowScriptAccess" value="sameDomain" />
            <param name="allowFullScreen" value="true" />
            <!--[if !IE]>-->
            <object type="application/x-shockwave-flash" data="explorer.swf" width="100%" height="100%" align="middle">
                <param name="quality" value="high" />
                <param name="bgcolor" value="white" />
                <param name="allowScriptAccess" value="sameDomain" />
                <param name="allowFullScreen" value="true" />
            <!--<![endif]-->
            <!--[if gte IE 6]>-->
            	<p> 
               		Either scripts and active content are not permitted to run or Adobe Flash Player version
               		10.0.0 or greater is not installed.
               	</p>
            <!--<![endif]-->
                <a href="http://www.adobe.com/go/getflashplayer">
                    <img src="http://www.adobe.com/images/shared/download_buttons/get_flash_player.gif" alt="Get Adobe Flash Player" />
                </a>
            <!--[if !IE]>-->
            </object>
            <!--<![endif]-->
        </object>
	</noscript>		
<!-- Include the MBARI footer -->
<%@ include file="WEB-INF/fragments/mbari_footer.jspf"%>
</body>
</html>
