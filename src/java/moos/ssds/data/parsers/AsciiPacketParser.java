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
package moos.ssds.data.parsers;

import java.util.Date;
import java.util.Map;

import moos.ssds.metadata.RecordDescription;
import moos.ssds.transmogrify.SSDSDevicePacket;

import org.apache.log4j.Logger;

/**
 * <p>
 * Parse packets containing Ascii buffers
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.2 $
 * @stereotype thing
 */
public class AsciiPacketParser extends PacketParser {

    /**
     * The constructor that takes in a <code>RecordDescription</code>
     * 
     * @param recordDescription
     */
    public AsciiPacketParser(RecordDescription recordDescription) {
        // Set the RecordDescription on the super class
        super(recordDescription);

        // Set the record description on the parser
        parser.setRecordDescription(recordDescription);

        this.parseMessages = parser.parseMessages;
    }

    /**
     * This is the method that will take in an SSDSDevicePacket and return the
     * map of parsed variables
     * 
     * @param packet
     *            A packet whose databuffer contents will be parsed
     * @return A Map (from java collections). The map keys are the
     *         <code>RecordVariables</code> that describe each variable in the
     *         packet. The values are The corresponding data object (Float,
     *         Double, etc).
     */
    public Map parse(SSDSDevicePacket ssdsDevicePacket) throws ParsingException {
        Map out = null;
        try {
            out = parser.parse(ssdsDevicePacket.getDataBuffer());
        } catch (ParsingException e) {
            Date timestamp = new Date();
            timestamp.setTime(ssdsDevicePacket.systemTime());
            StringBuffer parseExceptionMessage = new StringBuffer();
            parseExceptionMessage.append("ParsingException caught: "
                + e.getMessage() + "\n");
            parseExceptionMessage.append("PacketInfo: Date=" + timestamp
                + ", DeviceID= " + ssdsDevicePacket.sourceID()
                + ", SequenceNumber=" + ssdsDevicePacket.sequenceNo());
            throw new ParsingException(parseExceptionMessage.toString());
        }
        return out;
    }

    /**
     * This is the RecordParser that will be used to parse the actual record
     * contents.
     */
    private final AsciiRecordParser parser = new AsciiRecordParser();

    /**
     * A log4j logger
     */
    Logger logger = Logger.getLogger(AsciiPacketParser.class);

}
