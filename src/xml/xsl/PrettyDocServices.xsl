<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <!-- $Header: /home/cvs/ssds/src/xml/xslformats/PrettyDocServices.xsl,v 1.2 2005/04/07 06:27:24 graybeal Exp $	-->
    <!-- Last edited by $Author: graybeal $	$Date: 2005/04/07 06:27:24 $   -->
    <xsl:output indent="yes"></xsl:output>
    <xsl:template match="/">
        <xsl:comment>Beginning a new file.</xsl:comment>
        <xsl:apply-templates></xsl:apply-templates>
    </xsl:template>
    <xsl:template match="text()"></xsl:template>
    <xsl:template match="/ServletMetadata">
        <font align="right" size="-1">
            <blockquote>
                <p> Documentation file updated <xsl:value-of select="./@lastUpdate"></xsl:value-of>,
                    version <xsl:value-of select="./@majorVersion"></xsl:value-of>.<xsl:value-of
                        select="./@minorVersion"></xsl:value-of>
                    <br></br>
                </p>
            </blockquote>
        </font>
        <xsl:apply-templates></xsl:apply-templates>
    </xsl:template>
    <xsl:template match="//ServiceHost">
        <h1 align="center">Service Host <font color="000099">
                <xsl:value-of select="./ServiceHostName"></xsl:value-of>
            </font>
        </h1>
        <p>This file describes the services provided by the aggregate Service Host called <font
                color="000099">
                <xsl:value-of select="./ServiceHostName"></xsl:value-of>
            </font>.</p>
        <p>For this service host, the base access URL begins <xsl:value-of select="./BaseUrl"
            ></xsl:value-of>
        </p>
        <xsl:apply-templates></xsl:apply-templates>
    </xsl:template>
    <xsl:template match="//ServiceHost/Service">
        <hr size="6" width="75%"></hr>
        <h2>Service ID <font color="000099">
                <xsl:value-of select="./ServiceId"></xsl:value-of>
        </font>
            (at service named <font color="000099">
                <xsl:value-of select="./ServiceName"></xsl:value-of>
            </font>)
        </h2>
        <h3>Description</h3>
        <p>
            <xsl:value-of select="./Description"></xsl:value-of>
        </p>
        <h3>Parameters</h3>
        <p>The following parameters are defined for this service.</p>
        <xsl:apply-templates></xsl:apply-templates>
        <hr></hr>
        <h2>Service Examples</h2>
        <xsl:for-each select="./Example">
            <p>
                <strong>Example: </strong>
                <code>
                    <xsl:value-of select="./exCode"></xsl:value-of>
                </code>
                <br></br>
                <xsl:value-of select="./effect"></xsl:value-of>
            </p>
            <xsl:apply-templates></xsl:apply-templates>
        </xsl:for-each>
    </xsl:template>
    <xsl:template match="//ServiceHost/Service/Parameter">
        <hr size="2" width="50%"></hr>
        <h4>Parameter: <font color="000099">
                <xsl:value-of select="./ParamName"></xsl:value-of>
            </font>
        </h4>
        <xsl:if test="boolean(./RequiredValue)">
            <p><strong><font color="red">NOTE: </font></strong>
                <font size="-1">This parameter is required and must have a value of 
            <xsl:value-of select="./RequiredValue"/> for this service to work correctly.</font></p>
        </xsl:if>
        <p>
            <strong>Parameter Description: </strong>
            <xsl:value-of select="./Description"></xsl:value-of>
        </p>
        <p>
            <strong>Required/Optional: </strong>
            <xsl:value-of select="./Status"></xsl:value-of>
            <br></br>
            <strong>Parameter Type: </strong>
            <xsl:value-of select="./Type"></xsl:value-of>
            <br></br>
            <strong>Default: </strong>
            <xsl:if test="boolean(./Default)">
                <xsl:value-of select="./Default"/>
            </xsl:if>
            <xsl:if test="not(boolean(./Default))">
                <em> None</em>
            </xsl:if>
            <xsl:if test="boolean(./RequiredValue)">
               <br/> <strong>Required Value: </strong><xsl:value-of select="./RequiredValue"/>
            </xsl:if>
        </p>
        <xsl:apply-templates></xsl:apply-templates>
    </xsl:template>
    <xsl:template match="//ServiceHost/Service/Parameter/Example">
        <p>
            <strong>Example: </strong>
            <code>
                <xsl:value-of select="./exCode"></xsl:value-of>
            </code>
            <br></br>
            <xsl:value-of select="./effect"></xsl:value-of>
        </p>
        <xsl:apply-templates></xsl:apply-templates>
    </xsl:template>
</xsl:stylesheet>
