<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <!-- $Header: /home/cvs/ssds/src/xml/xslformats/ListAllVariables.xsl,v 1.2 2005/04/07 06:26:51 graybeal Exp $	-->
    <!-- Last edited by $Author: graybeal $	$Date: 2005/04/07 06:26:51 $   -->
    <xsl:output indent="yes"></xsl:output>
    <xsl:template match="/">
        <xsl:comment>Beginning a new file.</xsl:comment>
        <xsl:apply-templates></xsl:apply-templates>
    </xsl:template>
    <xsl:template match="text()"></xsl:template>
    <xsl:template match="/Metadata">
        <font align="right" size="-1">
            <blockquote>
                <p> Metadata updated <xsl:value-of select="./@lastUpdate"></xsl:value-of>, version
                        <xsl:value-of select="./@majorVersion"></xsl:value-of>.<xsl:value-of
                        select="./@minorVersion"></xsl:value-of>
                    <br></br>
                </p>
            </blockquote>
        </font>
        <xsl:apply-templates></xsl:apply-templates>
    </xsl:template>
    <xsl:template match="/Metadata/Deployment/Device">
        <h2>From device ID <xsl:value-of select="./@id"></xsl:value-of>: <xsl:value-of
                select="./@name"></xsl:value-of>
        </h2>
        <p>(This instrument is of type <xsl:value-of select="./@type"></xsl:value-of>,
            manufacturer/model <xsl:value-of select="./@mfgName"></xsl:value-of>. <xsl:value-of
                select="./@mfgModel"></xsl:value-of>).</p>
        <xsl:apply-templates></xsl:apply-templates>
    </xsl:template>
    <xsl:template
        match="/Metadata/Deployment/output/DataStream/RecordDescription|/Metadata/Deployment/output/DataFile/RecordDescription">
        <h3>Available Variables</h3>
        <xsl:if test="not(boolean(RecordVariable))">
            <p>There are no individual variables available from this data set.</p>
        </xsl:if>
        <xsl:if test="RecordVariable">
            <h3>The available variables in record # <xsl:value-of select="./@recordType"
                ></xsl:value-of> are:</h3>
            <xsl:element name="table">
                <xsl:attribute name="border">1</xsl:attribute>
                <tr>
                    <xsl:attribute name="color">#FFFFFF</xsl:attribute>
                    <xsl:attribute name="bgcolor">#3399FF</xsl:attribute>
                    <xsl:attribute name="align">center</xsl:attribute>
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
                        <font align="left" name="verdana" size="2">
                            <b>Description</b>
                        </font>
                    </td>
                </tr>
                <xsl:apply-templates select="//RecordVariable"></xsl:apply-templates>
            </xsl:element>
        </xsl:if>
    </xsl:template>
    <xsl:template match="//RecordVariable">
        <xsl:element name="tr">
            <xsl:attribute name="align">left</xsl:attribute>
            <xsl:element name="td">
                <font name="verdana" size="2">
                    <xsl:value-of select="./@name"></xsl:value-of>
                </font>
            </xsl:element>
            <xsl:element name="td">
                <font color="black" name="verdana" size="2">
                    <xsl:value-of select="./@longName"></xsl:value-of>
                </font>
            </xsl:element>
            <xsl:element name="td">
                <font color="black" name="verdana" size="2">
                    <xsl:value-of select="./@units"></xsl:value-of>
                </font>
            </xsl:element>
            <xsl:element name="td">
                <font align="left" color="black" name="verdana" size="2">
                    <xsl:value-of select="./description"></xsl:value-of>
                </font>
            </xsl:element>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>
