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
package moos.ssds.services.servlet.util;

/**
 * This is a helper class that provides clients with different ways of
 * formatting fault messages. The user supplies the fault information and the
 * desired format, and the method return that fault in the correct (and
 * consistent) format.
 * 
 * @author kgomes
 */
public class ServletFaultHandler {

    /**
     * This method is used to construct fault messages in different formats.
     * 
     * @param faultCode
     *            this should be one of the constants defined for fault code
     * @param faultSubcode
     * @param faultReason
     * @param faultDetail
     * @param format
     * @return the fault compiled fault message in the specified format
     * @throws IllegalArgumentException
     *             if some of the arguments do not match constants defined here
     *             or format does not specify a recognized format (see
     *             <code>ServletUtils</code> for format constants)
     */
    public static String formatFaultMessage(String faultCode,
        String faultSubcode, String faultReason, String faultDetail,
        String format) throws IllegalArgumentException {
        // Check code first
        if ((faultCode == null)
            || ((!faultCode.equals(FAULT_CODE_RECEIVER)) && (!faultCode
                .equals(FAULT_CODE_SENDER))))
            throw new IllegalArgumentException("The fault code " + faultCode
                + " does not match one of the fault code constants.");

        // Now check the format
        if ((format == null)
            || ((!format.equals(ServletUtils.TEXT_FORMAT))
                && (!format.equals(ServletUtils.STRING_FORMAT))
                && (!format.equals(ServletUtils.HTML_FORMAT)) && (!format
                .equals(ServletUtils.XML_FORMAT)))) {
            throw new IllegalArgumentException("The format specified ("
                + format + ") is not a valid format");
        }
        // Create the message string buffer
        StringBuffer faultMessage = new StringBuffer();
        // Now based on the type of message, construct the message
        if (format.equals(ServletUtils.TEXT_FORMAT)
            || format.equals(ServletUtils.STRING_FORMAT)) {
            faultMessage.append("Fault:\n");
            faultMessage.append("\tCode: " + faultCode + "\n");
            faultMessage.append("\t\tSubcode: " + faultSubcode + "\n");
            faultMessage.append("\tReason: " + faultReason + "\n");
            faultMessage.append("\tDetail: " + faultDetail + "\n");
        } else if (format.equals(ServletUtils.HTML_FORMAT)) {
            faultMessage.append("<b><font color=\"red\">Fault:</font></b><br>");
            faultMessage.append("<ul>");
            faultMessage.append("<li><i><u>Code</u></i>: " + faultCode
                + "<ul><li><i><u>Subcode</u></i>:" + faultSubcode
                + "</li></ul></li>");
            faultMessage.append("<li><i><u>Reason</u></i>: " + faultReason
                + "</li>");
            faultMessage.append("<li><i><u>Detail</u></i>: " + faultDetail
                + "</li>");
            faultMessage.append("</ul>");
        } else if (format.equals(ServletUtils.XML_FORMAT)) {
            faultMessage.append("<ssds:Fault>");
            faultMessage.append("<ssds:Code>");
            faultMessage.append("<ssds:Value>ssds:" + faultCode
                + "</ssds:Value>");
            faultMessage.append("<ssds:Subcode>");
            faultMessage.append("<ssds:Value>ssds:" + faultSubcode
                + "</ssds:Value>");
            faultMessage.append("</ssds:Subcode>");
            faultMessage.append("</ssds:Code>");
            faultMessage.append("<ssds:Reason>");
            faultMessage.append("<ssds:Text>" + faultReason + "</ssds:Text>");
            faultMessage.append("</ssds:Reason>");
            faultMessage.append("<ssds:Detail>");
            faultMessage.append("<ssds:Text>" + faultDetail + "</ssds:Text>");
            faultMessage.append("</ssds:Detail>");
            faultMessage.append("</ssds:Fault>");
        }
        return faultMessage.toString();
    }

    /**
     * Some constants for fault messages
     */
    public static final String FAULT_CODE_SENDER = "Sender";
    public static final String FAULT_CODE_RECEIVER = "Receiver";

}
