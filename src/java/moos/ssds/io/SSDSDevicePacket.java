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
package moos.ssds.io;

import moos.ssds.util.DateUtils;

import org.apache.log4j.Logger;
import org.mbari.siam.distributed.DevicePacket;

/**
 * <p>
 * <!--Insert summary here-->
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.7.2.1 $
 */
public class SSDSDevicePacket extends DevicePacket implements
		java.io.Serializable {

	/**
	 * This is the version (currently 3)
	 */
	private static final long serialVersionUID = 3L;

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(SSDSDevicePacket.class);

	/**
	 * This is a integer to define what type of packet this is. Here are the
	 * assumptions: packetType = 1 for MetadataPacket; packetType = 0 for
	 * SensorDataPacket and SummaryPacket; packetType = 4 for
	 * DeviceMessagePacket and MeasurementPacket
	 */
	private int packetType = -1;

	/**
	 * This is to simulate the metadataSequenceNumber that ISI will be
	 * providing. The metadataSequence number is used to link a SensorDataPacket
	 * back to the correct metadata that was in force when it was generated.
	 * Here are the assumptions: metadataSequenceNumber = -1 means it is unknown
	 * metadataSequenceNumber = 0 for metadatapackets metadataSequenceNumber >=
	 * 1 for anything else
	 */
	private long metadataSequenceNumber = -1L;

	/**
	 * This is used for minor metadata changes that do not cause new buckets to
	 * be created
	 */
	private long dataDescriptionVersion = -1L;

	/**
	 * This it the parentID that the packet came from. Here are the assumptions:
	 * platformID = -1 for unknown parent platformID = 0 for no parent
	 * platformID >=1 for known parent
	 */
	private long platformID = -1L;

	/**
	 * This is the type of record that the packet contains Here are the
	 * assumptions: recordType = -1 means the recordType is unknown recordType =
	 * 0 means it is a metadata packet recordType >= 1 means data packet of some
	 * type
	 */
	private long recordType = -1L;

	/**
	 * This is the data buffer for the packet that contains the bytes to come
	 * across
	 */
	private byte[] dataBuffer = null;

	/**
	 * This is a buffer that is used to carry other things that a packet might
	 * contain. It was created due to some packets having 'cause' byte arrays.
	 */
	private byte[] otherBuffer = null;

	/**
	 * This is the version number of the packet that we are implmenting. This
	 * was so that we could implement our own serialization scheme.
	 */
	public static int VERSION_ID = 3;

	/**
	 * This constructor simply uses the constructor from the DevicePacket
	 * 
	 * @param sourceID
	 */
	public SSDSDevicePacket(long sourceID) {
		// Just use the DevicePacket constructor
		super(sourceID);
	}

	/**
	 * Getter method for retrieving the metadata sequence number
	 * 
	 * @return the metadataSequenceNumber for this packet
	 */
	public long getMetadataSequenceNumber() {
		return metadataSequenceNumber;
	}

	/**
	 * Setter method for setting the metadata sequence number
	 * 
	 * @param metadataSequenceNumber
	 *            the metadataSequenceNumber for this packet
	 */
	public void setMetadataSequenceNumber(long metadataSequenceNumber) {
		this.metadataSequenceNumber = metadataSequenceNumber;
		super.setMetadataRef(metadataSequenceNumber);
	}

	/**
	 * Getter method for retrieving the metadata minor revision number
	 * 
	 * @return the dataDescriptionVersion for this packet
	 */
	public long getDataDescriptionVersion() {
		return dataDescriptionVersion;
	}

	/**
	 * Setter method for setting the metadata sequence number
	 * 
	 * @param metadataSequenceNumber
	 *            the metadataSequenceNumber for this packet
	 */
	public void setDataDescriptionVersion(long dataDescriptionVersion) {
		this.dataDescriptionVersion = dataDescriptionVersion;
	}

	/**
	 * Returns the time in epoch seconds
	 * 
	 * @return
	 */
	public long getTimestampSeconds() {
		return DateUtils.getEpochTimestampSecondsFromEpochMillis(systemTime());
	}

	/**
	 * Returns the nanaoseconds portion of the timestamp
	 * 
	 * @return
	 */
	public long getTimestampNanoseconds() {
		return DateUtils.getNanosecondsFromEpochMillis(systemTime());
	}

	/**
	 * Getter method for getting the platformID
	 * 
	 * @return the ID of the platform that is the parent of the device that sent
	 *         this packet
	 */
	public long getPlatformID() {
		return platformID;
	}

	/**
	 * The setter method for setting the platformID
	 * 
	 * @param platformID
	 *            is the ID of the platform that is the parent of the device
	 *            that sent this packet
	 */
	public void setPlatformID(long platformID) {
		this.platformID = platformID;
		super.setParentId(platformID);
	}

	/**
	 * Getter method for getting the type of record this contains
	 * 
	 * @return the number representing the type of record this packet contains
	 */
	public long getRecordType() {
		return recordType;
	}

	/**
	 * The setter method for setting the record type for this packet
	 * 
	 * @param recordType
	 *            is the number representing the type of record in this packet
	 */
	public void setRecordType(long recordType) {
		this.recordType = recordType;
		super.setRecordType(recordType);
	}

	/**
	 * The getter to get the data buffer of this packet
	 * 
	 * @return an array of bytes that is the databuffer
	 */
	public byte[] getDataBuffer() {
		return dataBuffer;
	}

	/**
	 * The setter method to set the databuffer
	 * 
	 * @param dataBuffer
	 *            is the array of bytes to set the databuffer to
	 */
	public void setDataBuffer(byte[] dataBuffer) {
		this.dataBuffer = dataBuffer;
	}

	/**
	 * The getter method to retrieve the packetType
	 * 
	 * @return the type of packet that this was converted from
	 */
	public int getPacketType() {
		return packetType;
	}

	/**
	 * The setterMethod to set the packetType
	 * 
	 * @param packetType
	 *            is the type of packet this was created from 0 = MetaDataPacket
	 *            1 = SensorDataPacket 2 = DeviceMessagePacket 3 =
	 *            SensorStatusPacket
	 */
	public void setPacketType(int packetType) {
		this.packetType = packetType;
	}

	/**
	 * This is the getter for the other buffer
	 * 
	 * @return an array of bytes that are just for extra stuff
	 */
	public byte[] getOtherBuffer() {
		return otherBuffer;
	}

	/**
	 * This is the setter for the other buffe that contains extra stuff
	 * 
	 * @param bs
	 *            is the byte array to assign to the other buffer
	 */
	public void setOtherBuffer(byte[] otherBuffer) {
		this.otherBuffer = otherBuffer;
	}

	/**
	 * This overrides the toString method to print something meaningful
	 */
	public String toString() {
		String dataBufferString = null;
		String otherBufferString = null;
		if (this.dataBuffer != null) {
			dataBufferString = new String(this.getDataBuffer());
		} else {
			dataBufferString = new String("");
		}
		if (this.getOtherBuffer() != null) {
			otherBufferString = new String(this.getOtherBuffer());
		} else {
			otherBufferString = new String("");
		}
		String stringToReturn = new String("SSDSDevicePacket:deviceID="
				+ this.sourceID() + ";sequenceNumber=" + this.sequenceNo()
				+ ";packetType=" + this.getPacketType()
				+ ";metadataSequenceNumber=" + this.getMetadataSequenceNumber()
				+ ";dataDescriptionVersion=" + this.getDataDescriptionVersion()
				+ ";platformID=" + this.getPlatformID() + ";recordType="
				+ this.getRecordType() + ";dataBuffer=" + dataBufferString
				+ ";otherBuffer=" + otherBufferString);
		return stringToReturn;
	}

} // End SSDSDevicePacket
