<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- $Header: /home/cvs/ssds/src/xml/xslformats/NiceDeviceInfo.xsl,v 1.1 2005/02/28 05:59:34 graybeal Exp $	-->
<!-- Last edited by $Author: graybeal $	$Date: 2005/02/28 05:59:34 $   -->

    <xsl:template match="/">
        <html>
            <h1>Instrument Description</h1>
            <xsl:apply-templates/>
        </html>
    </xsl:template>
    
    <xsl:template match="text()">
    </xsl:template>
    
    <xsl:template match="/Metadata">
        <font size="-1" align="right">
            <blockquote><p>
            Metadata last updated by <xsl:value-of select="./@lastAuthor"/> on <xsl:value-of select="./@lastUpdate"/><br/>
            Version Info: majorVersion=<xsl:value-of select="./@majorVersion"/>, minorVersion=<xsl:value-of select="./@minorVersion"/><br/>
            <!--XSL Template: http://ssds.shore.mbari.org/src/xml/xslformats/NiceDeviceInfo.xsl-->
           </p></blockquote>
        </font>
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="/Metadata/Deployment/Device">
        <h2>General description of device ID <strong><xsl:value-of select="./@id"></xsl:value-of></strong></h2>
        
        <p>This is instrument ID <xsl:value-of select="./@id"></xsl:value-of>, 
        named <em><xsl:value-of select="./@name"></xsl:value-of></em>. 
        It is deployed in the role of <xsl:value-of select="../@role"></xsl:value-of>, 
        on the <xsl:value-of select="../@name"></xsl:value-of> deployment.</p>
        
        <xsl:if test="../@nominalLongitude">
            <p>The instrument is deployed (nominally) at
            <xsl:if test="../@nominalLongitude"> 
                <xsl:value-of select="../@nominalLatitude"/> degrees latitude, 
                <xsl:value-of select="../@nominalLongitude"/> degrees longitude<xsl:if test="not(../@nominalDepth)">. (No nominal depth is recorded.)</xsl:if>
            </xsl:if>
            <xsl:if test="../@nominalDepth">, and 
                <xsl:value-of select="../@nominalDepth"/> meters depth 
                (negative values are above sea level).
            </xsl:if>
            </p>
        </xsl:if>
        
        <p>This instrument is of type <xsl:value-of select="./@type"></xsl:value-of>,
        and was manufactured by <xsl:value-of select="./@mfgName"></xsl:value-of>.
        (Its model name is <xsl:value-of select="./@mfgModel"></xsl:value-of> and 
        its serial number is 
        <xsl:if test="boolean(./@mfgSerialNumber)">
            <xsl:value-of select="./@mfgSerialNumber"/>
        </xsl:if>
        <xsl:if test="not(boolean(./@mfgSerialNumber))">
            <font color="red"> unknown</font>
        </xsl:if>.)</p>
        
        <p>The responsible person for this instrument is <xsl:value-of select="./Person/@firstname"></xsl:value-of>
        <xsl:text> </xsl:text>
        <xsl:value-of select="./Person/@surname"></xsl:value-of>, 
        who belongs to the organization <xsl:value-of select="./Person/@organization"></xsl:value-of>.
        You can reach the responsible person via phone at <xsl:value-of select="./Person/@phone"></xsl:value-of>, 
        and via email at <xsl:value-of select="./Person/@email"></xsl:value-of>.</p>
        
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="/Metadata/Deployment/Deployment[position()=1]">
        <h2>Deployed Sensors</h2>
 
            <p>The following sensors are deployed on this instrument.</p>
            <xsl:element name="table">
            <xsl:attribute name="border">1</xsl:attribute>
            <tr>
                <xsl:attribute name="color">#FFFFFF</xsl:attribute>
                <xsl:attribute name="bgcolor">#3399FF</xsl:attribute>
                <xsl:attribute name="align">center</xsl:attribute>
                <td>
                    <font name="verdana" size="2"> 
                        <b>Device ID</b>
                    </font>
                </td>
                <td>
                    <font name="verdana" size="2">
                        <b>Device Name </b>
                    </font>
                </td>
                <td>
                    <font name="verdana" size="2">
                        <b>Device Type</b>
                    </font>
                </td>
                <td>
                    <font name="verdana" size="2">
                        <b>Manufacture Name</b>
                    </font>
                </td>
                <td>
                    <font name="verdana" size="2">
                        <b>Manufacturer Model</b>
                    </font>
                </td>
                <td>
                    <font name="verdana" size="2">
                        <b>Serial Number</b>
                    </font>
                </td>
            </tr> 
            <xsl:apply-templates select='/Metadata/Deployment/Deployment/Device'/>
            </xsl:element>
            <xsl:apply-templates select='/Metadata/Deployment/output'/>
    </xsl:template>
    
     <xsl:template match="/Metadata/Deployment/Deployment/Device">
          <font name="verdana" size="2">
                <xsl:element name="tr">
                    <xsl:attribute name="align">center</xsl:attribute>
                    <xsl:element name="td">
                            <xsl:value-of select="./@id"/>
                    </xsl:element>
                    <xsl:element name="td">
                            <xsl:value-of select="./@name"/>
                    </xsl:element>
                    <xsl:element name="td">
                            <xsl:value-of select="./@type"/>
                     </xsl:element>
                    <xsl:element name="td">
                            <xsl:value-of select="./@mfgName"/>
                    </xsl:element>
                    <xsl:element name="td">
                            <xsl:value-of select="./@mfgModel"/>
                    </xsl:element>
                    <xsl:element name="td">
                            <xsl:value-of select="./@mfgSerialNumber"/>
                    </xsl:element>
                </xsl:element>
            </font>
    </xsl:template>
    
    <xsl:template match="/Metadata/Deployment/output/DataStream/RecordDescription|/Metadata/Deployment/output/DataFile/RecordDescription">
        <h2>Data record description for device ID <xsl:value-of select="/Metadata/Deployment/Device/@id"/></h2>

        <h3>Record description for record type <xsl:value-of select="./@recordType"/></h3>
        
        <p>This instrument has an <xsl:value-of select="./@bufferStyle"/> buffer, 
        which SSDS will <xsl:if test="not(./@parseable)">not </xsl:if> attempt to parse
        <xsl:if test="./@parseable">using the item descriptions below.</xsl:if>
        The buffer's values are <xsl:value-of select="./@bufferParseType"/>, and
        the length of the buffers is <xsl:value-of select="./@bufferLengthType"/>.</p>
        
        <p>The buffer values described in the following table are separated by the
        '<xsl:value-of select="./@bufferItemSeparator"/>' character.</p>
        
        <xsl:if test="RecordVariable">
            <xsl:element name="table">
            <xsl:attribute name="border">1</xsl:attribute>
            <tr>
                <xsl:attribute name="color">#FFFFFF</xsl:attribute>
                <xsl:attribute name="bgcolor">#3399FF</xsl:attribute>
                <xsl:attribute name="align">center</xsl:attribute>
                <td>
                    <font name="verdana" size="2"> 
                        <b>Column / <br/>Item #</b>
                    </font>
                </td>
                <td>
                    <font name="verdana" size="2">
                        <b>Variable Name </b>
                    </font>
                </td>
                <td>
                    <font name="verdana" size="2">
                        <b>Displayed Name</b>
                    </font>
                </td>
                <td>
                    <font name="verdana" size="2">
                        <b>Units</b>
                    </font>
                </td>
                <td>
                    <font name="verdana" size="2">
                        <b>Data Format</b>
                    </font>
                </td>
                <td>
                    <font name="verdana" size="2">
                        <b>Custom Formatting</b>
                    </font>
                </td>
                <td>
                    <font name="verdana" size="2" align="left">
                         <b>Description</b>
                    </font>
                </td>
            </tr>       
            <xsl:apply-templates select='//RecordVariable'/>
            </xsl:element>
        </xsl:if>
    </xsl:template>
    <xsl:template match="//RecordVariable">
        <xsl:element name="tr">
            <xsl:attribute name="align">center</xsl:attribute>
            <xsl:element name="td">
                <font name="verdana" size="2">
                    <xsl:value-of select="./@columnIndex"/>
                </font>              
            </xsl:element>
            <xsl:element name="td">
                <font name="verdana" size="2">
                    <xsl:value-of select="./@name"/>
                </font>              
            </xsl:element>
            <xsl:element name="td">
                <font color="black" name="verdana" size="2">
                     <xsl:value-of select="./@longName"/>
                </font>
            </xsl:element>
            <xsl:element name="td">
                <font color="black" name="verdana" size="2">
                     <xsl:value-of select="./@units"/>
                </font>
            </xsl:element>
            <xsl:element name="td">
                <font color="black" name="verdana" size="2">
                     <xsl:value-of select="./@format"/>
                </font>
            </xsl:element>
            <xsl:element name="td">
                <font color="black" name="verdana" size="2">
                     <xsl:value-of select="./@parseRegExp"/>
                </font>
            </xsl:element>
            <xsl:element name="td">
                <font color="black" name="verdana" size="2" align="left">
                    <xsl:value-of select="./description"/>
                </font>
            </xsl:element>
        </xsl:element>        
    </xsl:template>    
</xsl:stylesheet>
