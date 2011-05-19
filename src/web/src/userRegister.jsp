<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:ui="http://www.sun.com/web/ui">
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <ui:page binding="#{userRegister.page1}" id="page1">
            <ui:html binding="#{userRegister.html1}" id="html1">
                <ui:head binding="#{userRegister.head1}" id="head1">
                    <ui:link binding="#{userRegister.link1}" id="link1" url="/resources/stylesheet.css"/>
                </ui:head>
                <ui:body binding="#{userRegister.body1}" id="body1" style="-rave-layout: grid">
                    <ui:form binding="#{userRegister.form1}" id="form1">
                        <div style="position: absolute; left: 168px; top: 144px">
                            <jsp:directive.include file="footer.jspf"/>
                        </div>
                        <ui:tree binding="#{userRegister.tree1}" id="tree1" style="position: absolute; left: 192px; top: 312px" text="Tree">
                            <ui:treeNode action="#{SessionBean1.jumpToIndex}" binding="#{userRegister.treeNode1}" expanded="true" id="treeNode1"
                                style="font-size: 8px" text="Tree Node 1">
                                <f:facet name="image">
                                    <ui:image binding="#{userRegister.image1}" icon="TREE_DOCUMENT" id="image1"/>
                                </f:facet>
                            </ui:treeNode>
                        </ui:tree>
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
