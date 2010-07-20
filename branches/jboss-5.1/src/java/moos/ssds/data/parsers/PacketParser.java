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
import java.util.Map;

import moos.ssds.io.SSDSDevicePacket;
import moos.ssds.metadata.RecordDescription;

/**
 * <p>
 * This is an abstract class that is implented by different kinds of parsers
 * that understand how to break up <code>SSDSDevicePackets</code> into their
 * corresponding <code>RecordVariable</code>s.
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.2 $
 * @stereotype process
 */
abstract public class PacketParser {

    /**
     * This is the constructor that takes in the <code>RecordDescription</code>
     * that contains the metadata to describe the records so they can be paresd.
     * 
     * @param recordDescription
     */
    public PacketParser(RecordDescription recordDescription) {
        setRecordDescription(recordDescription);
    }

    /**
     * The getters and setters for the RecordDescription.
     * 
     * @return
     */
    public RecordDescription getRecordDescription() {
        return this.recordDescription;
    }

    /**
     * This method sets the <code>RecordDescription</code> and extracts the
     * list of <code>RecordVariables</code> for later use.
     * 
     * @param recordDescription
     *            is the <code>RecordDescription</code> that will be used to
     *            parse records
     */
    public void setRecordDescription(RecordDescription recordDescription) {
        // Set the record description
        this.recordDescription = recordDescription;

        // Now extract the list of variables
        if (recordDescription != null)
            recordVariables = recordDescription.getRecordVariables();
    }

    /**
     * This method returns a <code>Map</code> that contains the
     * <code>RecordVariables</code> as keys and their data as values
     */
    public abstract Map parse(SSDSDevicePacket ssdsDevicePacket)
        throws ParsingException;

    /**
     * This method returns a String that is a compiled set of messages from the
     * previous parse method call. It should be helpful in debugging.
     * 
     * @return
     */
    public String getParseMessages() {
        return this.parseMessages.toString();
    }

    /**
     * This is the <code>RecordDescription</code> that contains the metdata
     * that can be used to parse the records
     */
    protected RecordDescription recordDescription = null;

    /**
     * This is the collection of <code>RecordVariables</code> that is
     * associated with the <code>RecordDescription</code> for the parser
     */
    protected Collection recordVariables;

    /**
     * This is a <code>Map</code> that maps <code>class</code>s to strings
     * to make <code>class</code> lookups easy.
     */
    protected Map typeMap = VariableFormatMap.getInstance();

    /**
     * This is a string buffer that can be used to store message about the
     * parsing
     */
    protected StringBuffer parseMessages = new StringBuffer();
}