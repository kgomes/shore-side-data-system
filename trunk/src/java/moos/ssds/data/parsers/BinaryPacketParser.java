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

/**
 * <p>
 * This is a class that can be used to parse packets that have an associated
 * <code>RecordDescription</code>
 * </p>
 * @stereotype thing
 */
public class BinaryPacketParser extends PacketParser {

    public BinaryPacketParser(RecordDescription recordDescription) {
        // Call the super class constructor
        super(recordDescription);

        // Set the RecordDescription on the parser
        parser.setRecordDescription(recordDescription);
    }

    /**
     * This takes in a <code>SSDSDevicePacket</code> and returns a map of its
     * buffer parsed into <code>RecordVariable</code>s
     * 
     * @param ssdsDevicePacket
     * @return
     * @throws ParsingException
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
     * This is the binary record parser that will be used to parse the buffer
     */
    private final BinaryRecordParser parser = new BinaryRecordParser();
}
