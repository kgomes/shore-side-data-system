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

import java.util.Collection;
import java.util.Iterator;

import moos.ssds.io.SSDSDevicePacket;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.RecordDescription;

import org.apache.log4j.Logger;

/**
 * <p>
 * Context for parsing <code>SSDSDevicePacket</code>s
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.2 $
 * @stereotype role
 */
public class PacketParserContext extends ParserContext {

    /**
     * This is the constructor that takes in a
     * <code>DataContainer<code> and sets up the context and 
     * packet parsers.  It also takes in the <code>Collection</code> 
     * of <code>SSDSDevicePacket</code>s that will be iterated over to parse data.
     * 
     * @param source
     */
    public PacketParserContext(DataContainer source,
        Collection ssdsDevicePackets) {
        setSource(source);
        this.ssdsDevicePackets = ssdsDevicePackets;
    }

    /**
     * This method sets the associated <code>DataContainer</code> and
     * constructs the correct record parser.
     */
    public void setSource(DataContainer source) {
        this.recordDescription = source.getRecordDescription();
        // Set up the packet parser
        if (recordDescription.getBufferStyle().equalsIgnoreCase(
            RecordDescription.BUFFER_STYLE_ASCII)
            && recordDescription.isParseable().booleanValue()) {
            packetParser = new AsciiPacketParser(recordDescription);
        } else if (recordDescription.getBufferStyle().equalsIgnoreCase(
            RecordDescription.BUFFER_STYLE_BINARY)
            && recordDescription.isParseable().booleanValue()) {
            packetParser = new BinaryPacketParser(recordDescription);
        }
    }

    /**
     * @see moos.ssds.data.parsers.ParserContext#next()
     * @return A map where key = RecordVariable value = Object
     */
    public Object next() {
        if ((this.ssdsDevicePacketIterator != null)
            && (this.ssdsDevicePacketIterator.hasNext())) {
            Object object = this.ssdsDevicePacketIterator.next();
            currentSsdsDevicePacket = (SSDSDevicePacket) object;
            // Now parse that packet!
            try {
                return packetParser.parse(currentSsdsDevicePacket);
            } catch (ParsingException e) {
                this.appendLogtext("ParsingException: " + e.getMessage());
            }
        }
        // If here, just return null
        return null;
    }

    /**
     * This method checks to see if the iterator has more packets it can deliver
     */
    public boolean hasNext() {
        if ((this.ssdsDevicePacketIterator != null)
            && (this.ssdsDevicePacketIterator.hasNext())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method skips over the next packet that is queued up in the iterator
     */
    public void skip() {
        if ((this.ssdsDevicePacketIterator != null)
            && (this.ssdsDevicePacketIterator.hasNext())) {
            this.ssdsDevicePacketIterator.next();
        }
    }

    /**
     * This is the getters and setters for the <code>Collection</code> of
     * SSDSDevicePackets</code> to be iterated over.
     */
    public Collection getSsdsDevicePacket() {
        return this.ssdsDevicePackets;
    }

    public void setSsdsDevicePackets(Collection ssdsDevicePackets) {
        this.ssdsDevicePackets = ssdsDevicePackets;
        if (this.ssdsDevicePackets != null) {
            this.ssdsDevicePacketIterator = this.ssdsDevicePackets.iterator();
        } else {
            this.ssdsDevicePacketIterator = null;
        }
    }

    /**
     * This is the getter for the last packet that was pulled from the iterator
     */
    public SSDSDevicePacket getCurrentSsdsDevicePacket() {
        return this.currentSsdsDevicePacket;
    }

    /**
     * This is the Collection of <code>SSDSDevicePackets</code> that will be
     * parsed
     */
    private Collection ssdsDevicePackets;
    private Iterator ssdsDevicePacketIterator;

    /**
     * This is the device packet that was last pulled from the iterator
     */
    private SSDSDevicePacket currentSsdsDevicePacket;

    /**
     * This is the internal parser that can be used to parse the packets
     */
    protected PacketParser packetParser;

    /**
     * A logger
     */
    Logger logger = Logger.getLogger(PacketParserContext.class);

}
