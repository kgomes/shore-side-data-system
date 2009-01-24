/*
 * Copyright 2009 MBARI
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1 
 * (the "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at
 *
 * http://www.gnu.org/copyleft/lesser.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.moos.ssds.metadata.util;

import moos.ssds.metadata.util.MetadataFactory;

import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.NodeDetail;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This DifferenceListener will check for superficial differences between xml
 * files and mark them as "similar" if the differences are only superficial.
 * 
 * @author achase
 */
public class SansSuperficialDifferenceListener implements DifferenceListener {

    /**
     * This is a Log4JLogger that is used to log information to
     */
    static Logger logger = Logger.getLogger(MetadataFactory.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.custommonkey.xmlunit.DifferenceListener#differenceFound(org.custommonkey.xmlunit.Difference)
     */
    public int differenceFound(Difference difference) {

        // Begin check for supperficial differences
        if (difference.getId() == DifferenceConstants.ATTR_VALUE_ID) {
            String testValue = difference.getTestNodeDetail().getValue();
            String controlValue = difference.getControlNodeDetail().getValue();
            // if these are both numbers, make sure they aren't being seen as
            // different
            // because of an integer vs floating point problem.
            try {
                double testValueNumber = Double.parseDouble(testValue);
                double controlValueNumber = Double.parseDouble(controlValue);
                if (testValueNumber == controlValueNumber) {
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                } else {
                    // do nothing, let the rest of the if checks have a chance
                    // to catch this difference
                    // return Diff.RETURN_ACCEPT_DIFFERENCE;
                }
            } catch (NumberFormatException nfe) {
                // return Diff.RETURN_ACCEPT_DIFFERENCE;
            }
        }
        if (difference.getId() == DifferenceConstants.ATTR_SEQUENCE_ID) {
            return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
        }
        if (difference.getId() == DifferenceConstants.CHILD_NODELIST_SEQUENCE_ID) {
            return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
        }
        // NOTE achase 20040923: For backwards compatibility we accept
        // commentTag
        // and CommentTag elements in the xml submittted to ObjectBuilder.
        // However,
        // we only output CommentTag's so, we've got to check and make sure the
        // difference found is not a difference of CommentTag and commentTag.
        // Additionally, for backwards compatibility, we allow the syntax:
        // <commentTag>comment</commentTag> to substitute for the proper
        // tagString
        // attribute in CommentTag.
        if (difference.getControlNodeDetail().getNode().getNodeName()
            .equalsIgnoreCase("metadata")) {
            return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
        }
        if (difference.getControlNodeDetail().getNode().getNodeName()
            .equalsIgnoreCase("Deployment")) {
            // If it is the name that is different, ignore it
            if (difference.equals(DifferenceConstants.ELEMENT_TAG_NAME)) {
                if (difference.getTestNodeDetail().getValue().equalsIgnoreCase(
                    "DataProducer"))
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
            }
            // If it it the number of attributes, and they are just off by one,
            // that is OK
            if (difference.equals(DifferenceConstants.ELEMENT_NUM_ATTRIBUTES)) {
                if ((new Integer(difference.getTestNodeDetail().getValue())
                    .intValue() - new Integer(difference.getControlNodeDetail()
                    .getValue()).intValue()) == 1) {
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                }
            }
        }
        if (difference.getControlNodeDetail().getNode().getNodeName()
            .equalsIgnoreCase("ProcessRun")) {
            // If it is the name that is different, ignore it
            if (difference.equals(DifferenceConstants.ELEMENT_TAG_NAME)) {
                if (difference.getTestNodeDetail().getValue().equalsIgnoreCase(
                    "DataProducer"))
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
            }
            // If it it the number of attributes, and they are just off by one,
            // that is OK
            if (difference.equals(DifferenceConstants.ELEMENT_NUM_ATTRIBUTES)) {
                if ((new Integer(difference.getTestNodeDetail().getValue())
                    .intValue() - new Integer(difference.getControlNodeDetail()
                    .getValue()).intValue()) == 1) {
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                }
            }
        }
        if (difference.getControlNodeDetail().getNode().getNodeName()
            .equalsIgnoreCase("DataFile")) {
            // If it is the name that is different, ignore it
            if (difference.equals(DifferenceConstants.ELEMENT_TAG_NAME)) {
                if (difference.getTestNodeDetail().getValue().equalsIgnoreCase(
                    "DataContainer"))
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
            }
            // If it it the number of attributes, and they are just off by one,
            // that is OK
            if (difference.equals(DifferenceConstants.ELEMENT_NUM_ATTRIBUTES)) {
                if ((new Integer(difference.getTestNodeDetail().getValue())
                    .intValue() - new Integer(difference.getControlNodeDetail()
                    .getValue()).intValue()) == 1) {
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                }
            }
        }
        if (difference.getControlNodeDetail().getNode().getNodeName()
            .equalsIgnoreCase("DataStream")) {
            // If it is the name that is different, ignore it
            if (difference.equals(DifferenceConstants.ELEMENT_TAG_NAME)) {
                if (difference.getTestNodeDetail().getValue().equalsIgnoreCase(
                    "DataContainer"))
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
            }
            // If it it the number of attributes, and they are just off by one,
            // that is OK
            if (difference.equals(DifferenceConstants.ELEMENT_NUM_ATTRIBUTES)) {
                if ((new Integer(difference.getTestNodeDetail().getValue())
                    .intValue() - new Integer(difference.getControlNodeDetail()
                    .getValue()).intValue()) == 1) {
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                }
            }
        }
        if (difference.getControlNodeDetail().getNode().getNodeName()
            .equalsIgnoreCase("commentTag")) {
            if (difference.getId() == DifferenceConstants.ELEMENT_TAG_NAME_ID) {
                String commentTagOriginal = difference.getControlNodeDetail()
                    .getNode().getNodeName();
                String commentTagTest = difference.getTestNodeDetail()
                    .getNode().getNodeName();
                if (commentTagOriginal.equalsIgnoreCase(commentTagTest)) {
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                }
            }
            if (difference.getId() == DifferenceConstants.ELEMENT_NUM_ATTRIBUTES_ID) {
                Node tagStringAttribute = difference.getTestNodeDetail()
                    .getNode().getAttributes().getNamedItem("tagString");
                String originalCommentValue = difference.getControlNodeDetail()
                    .getNode().getFirstChild().getNodeValue();
                if (tagStringAttribute.getNodeValue().equals(
                    originalCommentValue)) {
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                }
            }
            if (difference.getId() == DifferenceConstants.HAS_CHILD_NODES_ID) {
                Node tagStringAttribute = difference.getTestNodeDetail()
                    .getNode().getAttributes().getNamedItem("tagString");
                String originalCommentValue = difference.getControlNodeDetail()
                    .getNode().getFirstChild().getNodeValue();
                if (tagStringAttribute.getNodeValue().equals(
                    originalCommentValue)) {
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                }
            }
        }
        // the attribute "type" within Device is now being transformed into a
        // child element "DeviceType", check for this
        if (difference.getControlNodeDetail().getNode().getNodeName().equals(
            "Device")) {
            if (difference.getControlNodeDetail().getValue().equalsIgnoreCase(
                "preferredDeploymentRole")) {
                return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
            }
            if (difference.getId() == DifferenceConstants.ELEMENT_NUM_ATTRIBUTES_ID) {
                String originalTypeAttributeValue = null;

                // First check to see if the control does not have an UUID, but
                // the test one does, that is OK
                if ((difference.getControlNodeDetail() != null)
                    && (difference.getControlNodeDetail().getNode() != null)
                    && (difference.getControlNodeDetail().getNode()
                        .getAttributes() != null)
                    && (difference.getControlNodeDetail().getNode()
                        .getAttributes().getNamedItem("uuid") == null)) {
                    if ((difference.getTestNodeDetail() != null)
                        && (difference.getTestNodeDetail().getNode() != null)
                        && (difference.getTestNodeDetail().getNode()
                            .getAttributes() != null)
                        && (difference.getTestNodeDetail().getNode()
                            .getAttributes().getNamedItem("uuid") != null)) {
                        return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                    }

                }
                if ((difference.getControlNodeDetail() != null)
                    && (difference.getControlNodeDetail().getNode() != null)
                    && (difference.getControlNodeDetail().getNode()
                        .getAttributes() != null)
                    && (difference.getControlNodeDetail().getNode()
                        .getAttributes().getNamedItem("type") != null)) {
                    originalTypeAttributeValue = difference
                        .getControlNodeDetail().getNode().getAttributes()
                        .getNamedItem("type").getNodeValue();

                }
                int i = 0;
                NodeList nodeList = difference.getTestNodeDetail().getNode()
                    .getChildNodes();
                while (i < nodeList.getLength()
                    && !nodeList.item(i).getNodeName().equals("DeviceType")) {
                    i++;
                }
                if (i >= nodeList.getLength()) {
                    i = nodeList.getLength() - 1;
                }

                // ElementNode2 deviceTypeElement = null;
                Element deviceTypeElement = null;
                Node mayBeDeviceTypeElement = nodeList.item(i);
                if (mayBeDeviceTypeElement != null
                    && mayBeDeviceTypeElement.getNodeName()
                        .equals("DeviceType")) {
                    // deviceTypeElement = (ElementNode2)
                    // mayBeDeviceTypeElement;
                    deviceTypeElement = (Element) mayBeDeviceTypeElement;
                }
                if (deviceTypeElement != null) {
                    String nameAtt = deviceTypeElement.getAttribute("name");
                    if (nameAtt.equals(originalTypeAttributeValue)) {
                        return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                    }
                }
            }
            if (difference.getId() == DifferenceConstants.ATTR_NAME_NOT_FOUND_ID) {
                if (difference.getControlNodeDetail().getValue().equals("type")) {
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                }
            }
            if (difference.getId() == DifferenceConstants.CHILD_NODELIST_LENGTH_ID) {
                // KJG - 1/25/2005, I changed this because it used to check to
                // see
                // if under Device, there was 1 node at first then two nodes
                // after
                // it would say that was OK. This was due to the fact that the
                // device attribute "type" is converted to "DeviceType" element
                // so you would have two if the first only had one. But if you
                // have person node under it, then it could have 3 instead of
                // two, so I changed it to look for just the difference and see
                // if the difference was one
                if ((difference.getTestNodeDetail().getNode().getChildNodes()
                    .getLength() - difference.getControlNodeDetail().getNode()
                    .getChildNodes().getLength()) == 1) {
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                }
                // if
                // (difference.getControlNodeDetail().getNode().getChildNodes()
                // .getLength() == 1
                // && difference.getTestNodeDetail().getNode().getChildNodes()
                // .getLength() == 2) {
                // }
            }
            // there will now be child nodes when before there weren't
            if (difference.getId() == DifferenceConstants.HAS_CHILD_NODES_ID) {
                // the only child should be the DeviceType element
                Node deviceTypeNode = difference.getTestNodeDetail().getNode()
                    .getChildNodes().item(0);
                if (deviceTypeNode.getNodeName().equals("DeviceType")) {
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                }

            }
        }
        // the attribute "type" within Resource is now being transformed into a
        // child element "ResourceType", check for this
        if (difference.getControlNodeDetail().getNode().getNodeName().equals(
            "Resource")) {
            if (difference.getId() == DifferenceConstants.ELEMENT_NUM_ATTRIBUTES_ID) {
                String originalTypeAttributeValue = null;

                if ((difference.getControlNodeDetail() != null)
                    && (difference.getControlNodeDetail().getNode() != null)
                    && (difference.getControlNodeDetail().getNode()
                        .getAttributes() != null)
                    && (difference.getControlNodeDetail().getNode()
                        .getAttributes().getNamedItem("resourceType") != null)) {
                    originalTypeAttributeValue = difference
                        .getControlNodeDetail().getNode().getAttributes()
                        .getNamedItem("resourceType").getNodeValue();

                }
                int i = 0;
                NodeList nodeList = difference.getTestNodeDetail().getNode()
                    .getChildNodes();
                while (i < nodeList.getLength()
                    && !nodeList.item(i).getNodeName().equals("ResourceType")) {
                    i++;
                }
                if (i >= nodeList.getLength()) {
                    i = nodeList.getLength() - 1;
                }

                Element resourceTypeElement = null;
                Node mayBeDeviceTypeElement = nodeList.item(i);
                if (mayBeDeviceTypeElement != null
                    && mayBeDeviceTypeElement.getNodeName()
                        .equals("ResourceType")) {
                    resourceTypeElement = (Element) mayBeDeviceTypeElement;
                }
                if (resourceTypeElement != null) {
                    String nameAtt = resourceTypeElement.getAttribute("name");
                    if (nameAtt.equals(originalTypeAttributeValue)) {
                        return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                    }
                }
            }
            if (difference.getId() == DifferenceConstants.ATTR_NAME_NOT_FOUND_ID) {
                if (difference.getControlNodeDetail().getValue().equals("resourceType")) {
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                }
            }
            if (difference.getId() == DifferenceConstants.CHILD_NODELIST_LENGTH_ID) {
                // KJG - 1/25/2005, I changed this because it used to check to
                // see
                // if under Device, there was 1 node at first then two nodes
                // after
                // it would say that was OK. This was due to the fact that the
                // device attribute "type" is converted to "DeviceType" element
                // so you would have two if the first only had one. But if you
                // have person node under it, then it could have 3 instead of
                // two, so I changed it to look for just the difference and see
                // if the difference was one
                if ((difference.getTestNodeDetail().getNode().getChildNodes()
                    .getLength() - difference.getControlNodeDetail().getNode()
                    .getChildNodes().getLength()) == 1) {
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                }
                // if
                // (difference.getControlNodeDetail().getNode().getChildNodes()
                // .getLength() == 1
                // && difference.getTestNodeDetail().getNode().getChildNodes()
                // .getLength() == 2) {
                // }
            }
            // there will now be child nodes when before there weren't
            if (difference.getId() == DifferenceConstants.HAS_CHILD_NODES_ID) {
                // the only child should be the DeviceType element
                Node deviceTypeNode = difference.getTestNodeDetail().getNode()
                    .getChildNodes().item(0);
                if (deviceTypeNode.getNodeName().equals("DeviceType")) {
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                }

            }
        }
        // we also need to check for changes from the contentType attribute to
        // the mimeType attribute
        if (difference.getId() == DifferenceConstants.ATTR_NAME_NOT_FOUND_ID) {
            NodeDetail controlNodeDetail = difference.getControlNodeDetail();
            if (controlNodeDetail.getValue().equals("contentType")) {
                // the mimeType transformation should only occur in datafile and
                // datastream
                String elementName = controlNodeDetail.getNode().getNodeName();
                if (elementName.equals("DataFile")
                    || elementName.equals("DataStream")) {
                    // make sure the contentType attribute was correctly moved
                    // over to mimeType
                    String originalValue = controlNodeDetail.getNode()
                        .getAttributes().getNamedItem("contentType")
                        .getNodeValue();
                    Node mimeTypeNode = difference.getTestNodeDetail()
                        .getNode().getAttributes().getNamedItem("mimeType");
                    if (mimeTypeNode != null
                        && mimeTypeNode.getNodeValue().equals(originalValue)) {
                        return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                    }
                }
            }
        }
        return RETURN_ACCEPT_DIFFERENCE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.custommonkey.xmlunit.DifferenceListener#skippedComparison(org.w3c.dom.Node,
     *      org.w3c.dom.Node)
     */
    public void skippedComparison(Node control, Node test) {
        System.err.println("Skipping nodes that aren't comparable.");
        System.err.println("Control Node Skipped: " + control.toString());
        System.err.println("Test Node Skipped: " + test.toString());
    }
}