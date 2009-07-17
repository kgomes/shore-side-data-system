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
package moos.ssds.transmogrify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import moos.ssds.io.PacketInput;

import org.apache.log4j.Logger;
import org.mbari.siam.distributed.DeviceMessagePacket;
import org.mbari.siam.distributed.DevicePacket;
import org.mbari.siam.distributed.MetadataPacket;
import org.mbari.siam.distributed.SensorDataPacket;

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
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(SSDSDevicePacket.class);

	/**
	 * This is a integer to define what type of packet this is. Here are the
	 * assumptions: packetType = 0 for MetadataPacket packetType = 1 for
	 * SensorDataPacket packetType = 2 for DeviceMessagePacket
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
	 * This is a constuctor that takes in an existing data packet and constructs
	 * the appropriate SSDSDevicePacket for for our application
	 * 
	 * @param packet
	 *            A sensor data packet
	 * @see org.mbari.isi.interfaces.SensorDataPacket
	 */
	public SSDSDevicePacket(DevicePacket devicePacket) {

		// Call the super constructor
		super(devicePacket.sourceID());

		// Copy over the sequence number
		this.setSequenceNo(devicePacket.sequenceNo());

		// Copy over the system time
		this.setSystemTime(devicePacket.systemTime());

		// Copy over parent id and record type
		this.setParentId(devicePacket.getParentId());
		this.setPlatformID(devicePacket.getParentId());
		this.setRecordType(devicePacket.getRecordType());

		// Check to see if it is a MetadataPacket
		if (devicePacket instanceof MetadataPacket) {
			logger.debug("DevicePacket is a MetadataPacket:");
			// Dumpt the data buffer into this one
			this.setDataBuffer(((MetadataPacket) devicePacket).getBytes());
			// Dump the cause buffer into the other buffer
			this.setOtherBuffer(((MetadataPacket) devicePacket).cause());
			// Set the packetType to 0
			this.setPacketType(0);
			// Set the recordtype to zero also
			this.setRecordType(0L);
			// Set the metadata sequence number to this sequence number
			this.setMetadataSequenceNumber(((MetadataPacket) devicePacket)
					.metadataRef());
			this.setDataDescriptionVersion(((MetadataPacket) devicePacket)
					.metadataRef());
		}

		// Now check to see if this is a SensorDataPacket
		if (devicePacket instanceof SensorDataPacket) {
			StringBuffer hexData = new StringBuffer();
			ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(
					((SensorDataPacket) devicePacket).dataBuffer());
			while (byteArrayIS.available() > 0) {
				hexData.append(Integer.toHexString(
						(0xFF & byteArrayIS.read()) | 0x100).substring(1));
			}
			logger.debug("DevicePacket is a SensorDataPacket:" + "deviceID="
					+ devicePacket.sourceID() + "," + "timestamp="
					+ devicePacket.systemTime() + "("
					+ new Date(devicePacket.systemTime()) + "),"
					+ "sequenceNumber=" + devicePacket.sequenceNo() + ","
					+ "dataBufferLength="
					+ ((SensorDataPacket) devicePacket).dataBuffer().length
					+ "," + "recordType="
					+ ((SensorDataPacket) devicePacket).getRecordType() + ","
					+ "metadataRef=" + devicePacket.metadataRef() + ","
					+ "dataBuffer(in hex)=" + hexData.toString());

			// Copy the data buffer over
			this.setDataBuffer(((SensorDataPacket) devicePacket).dataBuffer());
			// Set the packetType to 1
			this.setPacketType(1);
			// Set the recordType
			this.setRecordType(((SensorDataPacket) devicePacket)
					.getRecordType());
			// Set the metadata sequence number to the one recieved
			// from the class
			this.setMetadataSequenceNumber(((SensorDataPacket) devicePacket)
					.metadataRef());
			this.setDataDescriptionVersion(((SensorDataPacket) devicePacket)
					.metadataRef());
		}

		// Check to see if DeviceMessagePacket
		if (devicePacket instanceof DeviceMessagePacket) {
			logger.debug("DevicePacket is a DeviceMessagePacket:");
			// Dump the message buffer
			this.setDataBuffer(((DeviceMessagePacket) devicePacket)
					.getMessage());
			// Set the packet type
			this.setPacketType(2);
			// Set the metadataSequenceNumber
			this.setMetadataSequenceNumber(((DeviceMessagePacket) devicePacket)
					.metadataRef());
			this.setDataDescriptionVersion(((DeviceMessagePacket) devicePacket)
					.metadataRef());
		}

	} // End constructor

	/**
	 * A constructor that takes in the sourceID and the size of the buffer
	 */
	public SSDSDevicePacket(long sourceID, int buffersize) {
		// Construct the super class object
		super(sourceID);

		// Now setup the buffer to the right size
		this.setDataBuffer(new byte[buffersize]);
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
	 * This method takes in a <code>SSDSDevicePacket</code> and converts it to
	 * an array of bytes that is the equivalent packet representation in bytes.
	 * <b>NOTE: Do not use this to serialize a packet for publishing, it is
	 * meant for other purposes, use <code>convertToPublishableByteArray</code>
	 * for serializing for publishing </b>
	 * 
	 * @param ssdsDevicePacket
	 *            is the <code>SSDSDevicePacket</code> to convert
	 * @return the byte array that contains the equivalent contents of the
	 *         <code>SSDSDevicePacket</code>
	 */
	public static byte[] convertToByteArray(SSDSDevicePacket ssdsDevicePacket)
			throws IOException {
		// Return the byte array that matches the byte array that matches the
		// version 3 of the device packet (this is the only one currently, but I
		// structured it for future changes)
		return SSDSDevicePacket.convertToVersion3ByteArray(ssdsDevicePacket);
	}

	/**
	 * This method takes in a <code>SSDSDevicePacket</code> and converts it to
	 * an array of bytes that matches what the TransmogrifyMDB component is
	 * expecting. Transmogrify is expecting a format that is based on the SIAM
	 * interface and so is a little different from the internal SSDS
	 * representation
	 * 
	 * @param ssdsDevicePacket
	 * @return
	 * @throws IOException
	 */
	public static byte[] convertToPublishableByteArray(
			SSDSDevicePacket ssdsDevicePacket) throws IOException {
		return SSDSDevicePacket
				.convertToPublishableVersion3ByteArray(ssdsDevicePacket);
	}

	/**
	 * This method converts a <code>SSDSDevicePacket</code> to a byte array
	 * that matches the byte array specification that was used in version 3 of
	 * <code>SSDSDevicePacket</code>s
	 * 
	 * @param ssdsDevicePacket
	 * @return
	 */
	private static byte[] convertToVersion3ByteArray(
			SSDSDevicePacket ssdsDevicePacket) throws IOException {

		// The byte array to return after construction
		byte[] byteArrayToReturn = null;

		// Create the object to help in the construction of the byte array
		ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
		DataOutputStream dataOS = new DataOutputStream(byteOS);

		// Grab all the data from the packet
		long sourceID = ssdsDevicePacket.sourceID();
		long platformID = ssdsDevicePacket.getPlatformID();
		int packetType = ssdsDevicePacket.getPacketType();
		long recordType = ssdsDevicePacket.getRecordType();
		long metadataSequenceNumber = ssdsDevicePacket
				.getMetadataSequenceNumber();
		long dataDescriptionVersion = ssdsDevicePacket
				.getDataDescriptionVersion();
		long systemTime = ssdsDevicePacket.systemTime();
		long sequenceNumber = ssdsDevicePacket.sequenceNo();
		byte[] dataBuffer = ssdsDevicePacket.getDataBuffer();
		byte[] otherBuffer = ssdsDevicePacket.getOtherBuffer();

		// Now we need to write them out now in the correct format.

		// Write the device ID
		dataOS.writeLong(sourceID);
		// Write the parent ID
		dataOS.writeLong(platformID);
		// Convert the packet type to the ones we are expecting
		if (packetType == 0) {
			dataOS.writeInt(1);
		} else if (packetType == 1) {
			dataOS.writeInt(0);
		} else if (packetType == 2) {
			dataOS.writeInt(4);
		}
		// Write the packet sub type (which is their record type)
		if (packetType == 0) {
			dataOS.writeLong(0);
		} else {
			dataOS.writeLong(recordType);
		}

		// Write the metadata sequence number
		dataOS.writeLong(metadataSequenceNumber);

		// Write the DataDescription version
		dataOS.writeLong(dataDescriptionVersion);

		// Now for time stamp, I have to convert it from
		// milliseconds since 1/1/70 to seconds/nanoseconds
		// since 1/1/70
		long timestampSeconds = systemTime / 1000;
		long timestampNanoseconds = (systemTime % 1000 * 1000);
		dataOS.writeLong(timestampSeconds);
		dataOS.writeLong(timestampNanoseconds);
		// Write the sequence number
		dataOS.writeLong(sequenceNumber);
		if (dataBuffer != null) {
			dataOS.writeInt(dataBuffer.length);
			dataOS.write(dataBuffer);
		} else {
			dataOS.writeInt(1);
			dataOS.write(new byte[1]);
		}
		if (otherBuffer != null) {
			dataOS.writeInt(otherBuffer.length);
			dataOS.write(otherBuffer);
		} else {
			dataOS.writeInt(1);
			dataOS.write(new byte[1]);
		}

		// Now convert whole thing to byte array
		byteArrayToReturn = byteOS.toByteArray();

		// Now return it
		return byteArrayToReturn;
	}

	/**
	 * This method converts a <code>SSDSDevicePacket</code> to a byte array
	 * that is suitable for publishing to the TransmogrifyMDB
	 * 
	 * @param ssdsDevicePacket
	 * @return
	 */
	private static byte[] convertToPublishableVersion3ByteArray(
			SSDSDevicePacket ssdsDevicePacket) throws IOException {

		// Define the stuff to write to
		ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
		DataOutputStream dataOS = new DataOutputStream(byteOS);

		// Write out the data in our format
		try {
			// First write the device packet stream ID
			dataOS
					.writeShort(TransmogrifyMDB.DEVICE_PACKET_STREAM_ID_VERSION_ONE);
			// Now write the device packet version number
			dataOS.writeLong(TransmogrifyMDB.DEVICE_PACKET_VERSION_VERSION_ONE);
			// Write the device ID
			dataOS.writeLong(ssdsDevicePacket.sourceID());
			// Write the timestamp
			dataOS.writeLong(ssdsDevicePacket.systemTime());
			// Write the sequence number
			dataOS.writeLong(ssdsDevicePacket.sequenceNo());
			// Write the correct metadata sequence number
			dataOS.writeLong(ssdsDevicePacket.getMetadataSequenceNumber());
			// Write the parent ID
			dataOS.writeLong(ssdsDevicePacket.getParentId());
			// Now do the writing for specific packet types
			if (ssdsDevicePacket.getPacketType() == 0) {
				// Write the record type
				dataOS.writeLong(0);
				// Write the streamID
				dataOS
						.writeShort(TransmogrifyMDB.METADATA_PACKET_STREAM_ID_VERSION_ONE);
				// Write the version
				dataOS
						.writeLong(TransmogrifyMDB.METADATA_PACKET_VERSION_VERSION_ONE);
				// Grab the cause buffer and write that
				byte[] otherBuffer = ssdsDevicePacket.getOtherBuffer();
				if (otherBuffer != null) {
					dataOS.writeInt(otherBuffer.length);
					dataOS.write(otherBuffer);
				} else {
					dataOS.writeInt(1);
					dataOS.write(new byte[1]);
				}
				// Do the same with the data buffer
				byte[] dataBuffer = ssdsDevicePacket.getDataBuffer();
				if (dataBuffer != null) {
					dataOS.writeInt(dataBuffer.length);
					dataOS.write(dataBuffer);
				} else {
					dataOS.writeInt(1);
					dataOS.write(new byte[1]);
				}
			} else if (ssdsDevicePacket.getPacketType() == 2) {
				// Write the record type
				dataOS.writeLong(ssdsDevicePacket.getRecordType());
				// Write the streamID
				dataOS
						.writeShort(TransmogrifyMDB.DEVICE_MESSAGE_PACKET_STREAM_ID_VERSION_ONE);
				// Write the version
				dataOS
						.writeLong(TransmogrifyMDB.DEVICE_MESSAGE_PACKET_VERSION_VERSION_ONE);
				// Write the message buffer
				byte[] dataBuffer = ssdsDevicePacket.getDataBuffer();
				if (dataBuffer != null) {
					dataOS.writeInt(dataBuffer.length);
					dataOS.write(dataBuffer);
				} else {
					dataOS.writeInt(1);
					dataOS.write(new byte[1]);
				}
			} else if (ssdsDevicePacket.getPacketType() == 1) {
				// Write the record type
				dataOS.writeLong(ssdsDevicePacket.getRecordType());
				// Write the streamID
				dataOS
						.writeShort(TransmogrifyMDB.SENSOR_DATA_PACKET_STREAM_ID_VERSION_ONE);
				// Write the version
				dataOS
						.writeLong(TransmogrifyMDB.SENSOR_DATA_PACKET_VERSION_VERSION_ONE);
				// Write the data buffer
				byte[] dataBuffer = ssdsDevicePacket.getDataBuffer();
				if (dataBuffer != null) {
					dataOS.writeInt(dataBuffer.length);
					dataOS.write(dataBuffer);
				} else {
					dataOS.writeInt(1);
					dataOS.write(new byte[1]);
				}
			} else {
				// If it not understood, just assume it is a data packet
				// Write the record type
				dataOS.writeLong(ssdsDevicePacket.getRecordType());
				// Write the streamID
				dataOS
						.writeShort(TransmogrifyMDB.DEVICE_MESSAGE_PACKET_STREAM_ID_VERSION_ONE);
				// Write the version
				dataOS
						.writeLong(TransmogrifyMDB.DEVICE_MESSAGE_PACKET_VERSION_VERSION_ONE);
				// Write the message buffer
				byte[] dataBuffer = ssdsDevicePacket.getDataBuffer();
				if (dataBuffer != null) {
					dataOS.writeInt(dataBuffer.length);
					dataOS.write(dataBuffer);
				} else {
					dataOS.writeInt(1);
					dataOS.write(new byte[1]);
				}
			}
		} catch (IOException e1) {
			logger.error("IOException trying to write packet in our format"
					+ e1.getMessage());
		}
		return byteOS.toByteArray();
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
