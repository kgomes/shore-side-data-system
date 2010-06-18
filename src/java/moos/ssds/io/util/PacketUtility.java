package moos.ssds.io.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.jms.BytesMessage;
import javax.jms.JMSException;

import moos.ssds.io.SSDSDevicePacket;
import moos.ssds.io.SSDSGeoLocatedDevicePacket;
import moos.ssds.util.DateUtils;

import org.apache.log4j.Logger;
import org.mbari.siam.distributed.DeviceMessagePacket;
import org.mbari.siam.distributed.DevicePacket;
import org.mbari.siam.distributed.Exportable;
import org.mbari.siam.distributed.MetadataPacket;
import org.mbari.siam.distributed.SensorDataPacket;
import org.mbari.siam.distributed.SummaryPacket;

/**
 * This class contains some utility methods for dealing with the various aspects
 * of Packet structures in SSDS.
 * 
 * @author kgomes
 * 
 */
public class PacketUtility {

	/**
	 * A log4j logger
	 */
	private static final Logger logger = Logger.getLogger(PacketUtility.class);

	/**
	 * This method takes in a JMS BytesMessage and tries to extract a byte array
	 * 
	 * @param bytesMessage
	 *            the JMS message that contains the bytes desired
	 * @return the array of bytes that were extracted from the BytesMessage.
	 *         This will be null if the BytesMessage was null or if something
	 *         went wrong in the extraction.
	 */
	public static byte[] extractByteArrayFromBytesMessage(
			BytesMessage bytesMessage) {

		// Create the byte array to return
		byte[] bytesToReturn = null;

		// Make sure the bytes message exists
		if (bytesMessage != null) {
			// Make sure the message pointer is reset
			try {
				bytesMessage.reset();
			} catch (JMSException e1) {
				logger.error("JMSException caught trying to "
						+ "reset the bytes message: " + e1.getMessage());
			}

			// Grab the size of the payload
			long payloadSize = 0;
			try {
				// Create a new array with the size of the payload
				payloadSize = bytesMessage.getBodyLength();
				if (payloadSize > Integer.MAX_VALUE) {
					logger
							.error("The payload size of the incoming BytesMessage "
									+ "is bigger than the maximum size of integer array I "
									+ "can allocate, this is VERY unsual as the message is "
									+ "larger than 2GB. I am ignoring the message");
					return null;
				}
			} catch (JMSException e) {
				logger.error("JMSException was caught trying to read "
						+ "the payload length from the BytesMessage: "
						+ e.getMessage());
				return null;
			}
			// Now cast the size down to an int and allocate the byte array
			bytesToReturn = new byte[(int) payloadSize];

			// Now try to read in the byte array
			if (bytesToReturn != null && bytesToReturn.length > 0) {
				try {
					bytesMessage.readBytes(bytesToReturn);
				} catch (JMSException e) {
					logger.error("JMSException was caught trying to "
							+ "extract the byte array from the BytesMessage: "
							+ e.getMessage());
					return null;
				}
			} else {
				logger.error("For some reason a byte buffer was not allocated");
			}
		}

		// Now return it
		return bytesToReturn;
	}

	/**
	 * This method simply takes in a byte array that should be in the SIAM
	 * format and logs the parameters of the message
	 * 
	 * @param siamByteArray
	 */
	public static void logSIAMMessageByteArray(byte[] siamByteArray,
			boolean convertBuffersToASCII) {
		// This is a string buffer to keep track of the progress of the
		// extraction
		StringBuffer loggerMessage = new StringBuffer();
		logger.debug("Logging of SIAM byte array proceeding:");

		// Makes sure the incoming BytesMessage is not null
		if (siamByteArray != null) {

			// Create convenience input streams to read from the byte array
			ByteArrayInputStream bis = new ByteArrayInputStream(siamByteArray);
			DataInputStream dis = new DataInputStream(bis);

			// Now read in all the information from the SIAM byte array and
			// append to the log message
			try {
				// Read in the StreamID
				short streamID = dis.readShort();
				loggerMessage.append("StreamID=" + streamID + "|");

				// Read in the devicePacketVersion
				long devicePacketVersion = dis.readLong();
				loggerMessage.append("DevicePacketVersion="
						+ devicePacketVersion + "|");

				// Read in the sourceID
				long sourceID = dis.readLong();
				loggerMessage.append("SourceID=" + sourceID + "|");

				// Read in the in timestamp
				long timestamp = dis.readLong();
				loggerMessage.append("Timestamp=" + timestamp + "|");
				loggerMessage.append("Date=" + new Date(timestamp) + "|");

				// Read in the sequence number
				long sequenceNumber = dis.readLong();
				loggerMessage.append("SequenceNumber=" + sequenceNumber + "|");

				// Read in the metadataref number
				long metadataRef = dis.readLong();
				loggerMessage.append("MetadataRef=" + metadataRef + "|");

				// Read in the parentID
				long parentID = dis.readLong();
				loggerMessage.append("ParentID=" + parentID + "|");

				// Read in the recordType
				long recordType = dis.readLong();
				loggerMessage.append("RecordType=" + recordType + "|");

				// Read in the streamID
				short secondStreamID = dis.readShort();
				loggerMessage.append("SecondStreamID=" + secondStreamID + "|");

				// Read packet Version
				long secondPacketVersion = dis.readLong();
				loggerMessage.append("SecondPacketVersion="
						+ secondPacketVersion + "|");

				// Read in the first data buffer length
				int firstBufferLength = 0;
				try {
					firstBufferLength = dis.readInt();
				} catch (Exception e) {
					logger.error("An Exception was caught trying to "
							+ "extract the first buffer length by reading "
							+ "in int from the SIAM byte array: "
							+ e.getMessage());
				}
				loggerMessage.append("FirstBufferLength=" + firstBufferLength
						+ "|");
				// If it looks like there is some buffer to read in, go ahead
				// and do so
				if (firstBufferLength > 0) {
					// Create the array to hold the first buffer
					byte[] firstBufferBytes = new byte[firstBufferLength];
					// Now read in the bytes
					dis.read(firstBufferBytes);
					// Check to see if the user wants the buffers converted to
					// ASCII for logging
					if (convertBuffersToASCII) {
						loggerMessage.append("FirstBuffer(in ASCII)="
								+ new String(firstBufferBytes) + "|");
					} else {
						// Convert it to hex so it can be logged
						StringBuffer hexData = new StringBuffer();
						ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(
								firstBufferBytes);
						while (byteArrayIS.available() > 0) {
							hexData.append(Integer.toHexString(
									(0xFF & byteArrayIS.read()) | 0x100)
									.substring(1));
						}
						loggerMessage.append("FirstBuffer(in hex)="
								+ hexData.toString() + "|");
					}
				}

				// Now try to read in a length of second buffer
				int secondBufferLength = 0;
				try {
					secondBufferLength = dis.readInt();
				} catch (Exception e) {
					if (secondStreamID == Exportable.EX_METADATAPACKET) {
						logger.debug("An Exception was caught trying to "
								+ "extract the second buffer length by "
								+ "reading in int from the SIAM byte array:\n"
								+ e.getMessage()
								+ ".\n  This is a metadata packet which "
								+ "usually has two buffers, this might be "
								+ "unusual.");
					}
				}
				loggerMessage.append("SecondBufferLength=" + secondBufferLength
						+ "|");
				// If it looks like there is a second buffer, try to read it
				if (secondBufferLength > 0) {
					// Create the byte array for the second buffer
					byte[] secondBufferBytes = new byte[secondBufferLength];
					// Read them in
					dis.read(secondBufferBytes);
					// Check to see if the user wants the buffers converted to
					// ASCII for logging
					if (convertBuffersToASCII) {
						loggerMessage.append("SecondBuffer(in ASCII)="
								+ new String(secondBufferBytes) + "|");
					} else {
						// Now convert them to hex, so they can be logged
						StringBuffer hexTwoData = new StringBuffer();
						ByteArrayInputStream byteTwoArrayIS = new ByteArrayInputStream(
								secondBufferBytes);
						while (byteTwoArrayIS.available() > 0) {
							hexTwoData.append(Integer.toHexString(
									(0xFF & byteTwoArrayIS.read()) | 0x100)
									.substring(1));
						}
						loggerMessage.append("SecondBuffer(in hex)="
								+ hexTwoData.toString() + "\n");
					}
				}
			} catch (IOException e) {
				logger.error("An IOException was trapped during "
						+ "the extraction: " + e.getMessage());
			} catch (Exception e) {
				logger.error("An Exception was trapped during "
						+ "the extraction: " + e.getMessage());
			}
		} else {
			logger.info("The incoming byte array was null!");
		}
		logger.debug(loggerMessage.toString());
	}

	/**
	 * This method simply takes in a byte array that should be in the SSDS
	 * expected format and creates a log entry to detail out the contents
	 * 
	 * @param ssdsByteArray
	 */
	public static void logVersion3SSDSByteArray(byte[] ssdsByteArray,
			boolean convertBuffersToASCII) {
		// This is a string buffer to keep track of the progress of the
		// extraction
		StringBuffer loggerMessage = new StringBuffer();
		loggerMessage.append("Logging of SSDS byte array proceeding:\n");

		// Makes sure the incoming BytesMessage is not null
		if (ssdsByteArray != null) {

			// Create convenience input streams to read from the byte array
			ByteArrayInputStream bis = new ByteArrayInputStream(ssdsByteArray);
			DataInputStream dis = new DataInputStream(bis);

			// Now read in all the information from the SSDS byte array and
			// append to the log message
			try {
				// Read in the sourceID
				long sourceID = dis.readLong();
				loggerMessage.append("SourceID=" + sourceID + "|");

				// Read in the parentID
				long parentID = dis.readLong();
				loggerMessage.append("ParentID=" + parentID + "|");

				// Read in the packet type
				int packetType = dis.readInt();
				loggerMessage.append("PacketType=" + packetType + "|");

				// Read in the packet sub type
				long packetSubType = dis.readLong();
				loggerMessage.append("PacketSubType=" + packetSubType + "|");

				// Read in the metadata sequence number
				long metadataSequenceNumber = dis.readLong();
				loggerMessage.append("MetadataSequenceNumber="
						+ metadataSequenceNumber + "|");

				// Read in the data description version
				long dataDescriptionVersion = dis.readLong();
				loggerMessage.append("DataDescriptionVersion="
						+ dataDescriptionVersion + "|");

				// Read in the in timestamp in Seconds
				long timestampSeconds = dis.readLong();
				loggerMessage.append("TimestampSeconds=" + timestampSeconds
						+ "|");

				// Nano seconds
				long timestampNanoseconds = dis.readLong();
				loggerMessage.append("TimestampNanoseconds="
						+ timestampNanoseconds + "|");

				// Print out the human readable date
				loggerMessage.append("Date="
						+ DateUtils.constructDateFromEpochSecondsAndNanseconds(
								timestampSeconds, timestampNanoseconds) + "|");

				// Read in the sequence number
				long sequenceNumber = dis.readLong();
				loggerMessage.append("SequenceNumber=" + sequenceNumber + "|");

				// Read in the first data buffer length
				int firstBufferLength = 0;
				try {
					firstBufferLength = dis.readInt();
				} catch (Exception e) {
					logger.error("An Exception was caught trying to "
							+ "extract the first buffer length by "
							+ "reading in int from the SSDS byte array: "
							+ e.getMessage());
				}
				loggerMessage.append("FirstBufferLength=" + firstBufferLength
						+ "|");
				// If it looks like there is some buffer to read in, go ahead
				// and do sos
				if (firstBufferLength > 0) {
					// Create the array to hold the first buffer
					byte[] firstBufferBytes = new byte[firstBufferLength];
					// Now read in the bytes
					dis.read(firstBufferBytes);
					// Check to see if the user wants the buffers converted to
					// ASCII for logging
					if (convertBuffersToASCII) {
						loggerMessage.append("FirstBuffer(in ASCII)="
								+ new String(firstBufferBytes) + "|");
					} else {
						// Convert it to hex so it can be logged
						StringBuffer hexData = new StringBuffer();
						ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(
								firstBufferBytes);
						while (byteArrayIS.available() > 0) {
							hexData.append(Integer.toHexString(
									(0xFF & byteArrayIS.read()) | 0x100)
									.substring(1));
						}
						loggerMessage.append("FirstBuffer(in hex)="
								+ hexData.toString() + "|");
					}
				}
				// Now try to read in a length of second buffer
				int secondBufferLength = 0;
				try {
					secondBufferLength = dis.readInt();
				} catch (Exception e) {
					logger
							.error("An Exception was caught trying to "
									+ "extract the "
									+ "second buffer length by reading in "
									+ "int from the ssds byte array: "
									+ e.getMessage());
				}
				loggerMessage.append("SecondBufferLength=" + secondBufferLength
						+ "|");
				// If it looks like there is a second buffer, try to read it
				if (secondBufferLength > 0) {
					// Create the byte array for the second buffer
					byte[] secondBufferBytes = new byte[secondBufferLength];
					// Read them in
					dis.read(secondBufferBytes);
					// Check to see if the user wants the buffers converted
					// to ASCII for logging
					if (convertBuffersToASCII) {
						loggerMessage.append("SecondBuffer(in ASCII)="
								+ new String(secondBufferBytes) + "|");
					} else {
						// Now convert them to hex, so they can be logged
						StringBuffer hexTwoData = new StringBuffer();
						ByteArrayInputStream byteTwoArrayIS = new ByteArrayInputStream(
								secondBufferBytes);
						while (byteTwoArrayIS.available() > 0) {
							hexTwoData.append(Integer.toHexString(
									(0xFF & byteTwoArrayIS.read()) | 0x100)
									.substring(1));
						}
						loggerMessage.append("SecondBuffer(in hex)="
								+ hexTwoData.toString() + "\n");
					}
				}
			} catch (IOException e) {
				logger
						.error("An IOException was trapped during the extraction: "
								+ e.getMessage());
			} catch (Exception e) {
				logger.error("An Exception was trapped during the extraction: "
						+ e.getMessage());
			}
		} else {
			logger.info("The incoming byte array was null!");
		}
		logger.debug(loggerMessage.toString());
	}

	/**
	 * This method takes in all the various parameters and creates a byte array
	 * that is in the SIAM exportable format
	 * 
	 * @param streamID
	 * @param devicePacketVersion
	 * @param sourceID
	 * @param timestamp
	 * @param sequenceNumber
	 * @param metadataRef
	 * @param parentID
	 * @param recordType
	 * @param secondStreamID
	 * @param secondPacketVersion
	 * @param firstBuffer
	 * @param secondBuffer
	 * @return
	 */
	public static byte[] createSIAMFormatByteArray(short streamID,
			long devicePacketVersion, long sourceID, long timestamp,
			long sequenceNumber, long metadataRef, long parentID,
			long recordType, short secondStreamID, long secondPacketVersion,
			byte[] firstBuffer, byte[] secondBuffer) {
		// First create a DataOutputStream
		ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
		DataOutputStream dataOS = new DataOutputStream(byteOS);

		// Now write the StreamID
		try {
			dataOS.writeShort(streamID);
			dataOS.writeLong(devicePacketVersion);
			dataOS.writeLong(sourceID);
			dataOS.writeLong(timestamp);
			dataOS.writeLong(sequenceNumber);
			dataOS.writeLong(metadataRef);
			dataOS.writeLong(parentID);
			dataOS.writeLong(recordType);
			dataOS.writeShort(secondStreamID);
			dataOS.writeLong(secondPacketVersion);
			if (firstBuffer != null && firstBuffer.length > 0) {
				dataOS.writeInt(firstBuffer.length);
				dataOS.write(firstBuffer);
			} else {
				dataOS.writeInt(0);
			}
			if (secondBuffer != null && secondBuffer.length > 0) {
				dataOS.writeInt(secondBuffer.length);
				dataOS.write(secondBuffer);
			} else {
				dataOS.writeInt(0);
			}
		} catch (IOException e) {
			logger.error("IOException caught trying to convert "
					+ "all attributes to byte array: " + e.getMessage());
		}

		// Now convert to byte array and return
		return byteOS.toByteArray();
	}

	/**
	 * This method takes int the various pieces of information and builds a byte
	 * array that is in the prescribed SSDS format
	 */
	public static byte[] createVersion3SSDSByteArray(long sourceID,
			long parentID, int packetType, long packetSubType,
			long metadataSequenceNumber, long dataDescriptionVersion,
			long timestampSeconds, long timestampNanoseconds,
			long sequenceNumber, byte[] firstBuffer, byte[] secondBuffer) {

		// Create the output byte array (streams)
		ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
		DataOutputStream dataOS = new DataOutputStream(byteOS);

		try {
			// Write the device ID
			dataOS.writeLong(sourceID);
			// Write the parent ID
			dataOS.writeLong(parentID);
			// The packet Type
			dataOS.writeInt(packetType);
			// The subtype
			dataOS.writeLong(packetSubType);
			// The metadata sequence number
			dataOS.writeLong(metadataSequenceNumber);
			// Write the DataDescription version
			dataOS.writeLong(dataDescriptionVersion);
			// Timestamps
			dataOS.writeLong(timestampSeconds);
			dataOS.writeLong(timestampNanoseconds);
			// Write the sequence number
			dataOS.writeLong(sequenceNumber);
			// If the first buffer exists, write the length and the buffer or
			// record the length as zero
			if (firstBuffer != null && firstBuffer.length > 0) {
				dataOS.writeInt(firstBuffer.length);
				dataOS.write(firstBuffer);
			} else {
				dataOS.writeInt(0);
			}
			// Same for the second buffer
			if (secondBuffer != null && secondBuffer.length > 0) {
				dataOS.writeInt(secondBuffer.length);
				dataOS.write(secondBuffer);
			} else {
				dataOS.writeInt(0);
			}
		} catch (IOException e) {
			logger.error("IOException trying to build SSDS format byte array"
					+ e.getMessage());
		}
		// Now return the array
		if (byteOS != null && byteOS.size() > 0) {
			return byteOS.toByteArray();
		} else {
			return null;
		}
	}

	/**
	 * This method takes in a byte array and return an Object array that will be
	 * in the order of items in the SSDS byte arrary format
	 * 
	 * @return an array of <code>Object</code>s that will be the various pieces
	 *         of the SSDS Byte Array
	 *         <ol>
	 *         <li>sourceID (long)</li>
	 *         <li>parentID (long)</li>
	 *         <li>packetType (int)</li>
	 *         <li>packetSubType (long)</li>
	 *         <li>metadataSequenceNumber (long)</li>
	 *         <li>dataDescriptionVersion (long)</li>
	 *         <li>timestampSeconds (long)</li>
	 *         <li>timestampNanoseconds (long)</li>
	 *         <li>sequenceNumber (long)</li>
	 *         <li>firstBufferLength (int)</li>
	 *         <li>firstBuffer (byte [])</li>
	 *         <li>secondBufferLength (int)</li>
	 *         <li>secondBuffer (byte [])</li>
	 *         </ol>
	 * @param ssdsFormatByteArray
	 */
	public static Object[] readVariablesFromVersion3SSDSByteArray(
			byte[] ssdsByteArray) {
		// The collection to return is an array list
		ArrayList<Object> returnCollection = new ArrayList<Object>();

		// OK now parse out the keys from the byte array
		DataInputStream dataInputStream = new DataInputStream(
				new ByteArrayInputStream(ssdsByteArray));
		try {
			returnCollection.add(dataInputStream.readLong());
			returnCollection.add(dataInputStream.readLong());
			returnCollection.add(dataInputStream.readInt());
			returnCollection.add(dataInputStream.readLong());
			returnCollection.add(dataInputStream.readLong());
			returnCollection.add(dataInputStream.readLong());
			returnCollection.add(dataInputStream.readLong());
			returnCollection.add(dataInputStream.readLong());
			returnCollection.add(dataInputStream.readLong());
			int firstBufferSize = dataInputStream.readInt();
			byte[] firstBuffer = new byte[firstBufferSize];
			// Read in the data buffer
			dataInputStream.read(firstBuffer);
			returnCollection.add(firstBufferSize);
			returnCollection.add(firstBuffer);
			// Read in the size of the secondary buffer
			int secondBufferSize = dataInputStream.readInt();
			byte[] secondBuffer = new byte[secondBufferSize];
			// Read in the buffer
			dataInputStream.read(secondBuffer);
			returnCollection.add(secondBufferSize);
			returnCollection.add(secondBuffer);
		} catch (IOException e) {
			logger.error("IException caught reading from byte array: "
					+ e.getMessage());
		}

		// Now return the collection
		return returnCollection.toArray();
	}

	/**
	 * This method takes in a DevicePacket and then converts it to a new
	 * instance of a SSDSDevicePacket
	 * 
	 * @param devicePacket
	 * @return
	 */
	public static SSDSDevicePacket convertSIAMDevicePacketToSSDSDevicePacket(
			DevicePacket devicePacket) {
		// Make sure the device packet is not null
		if (devicePacket == null)
			return null;
		// The SSDSDevicePacket to return
		SSDSDevicePacket ssdsDevicePacket = new SSDSDevicePacket(devicePacket
				.sourceID());

		// Copy over the sequence number
		ssdsDevicePacket.setSequenceNo(devicePacket.sequenceNo());

		// Copy over the system time
		ssdsDevicePacket.setSystemTime(devicePacket.systemTime());

		// Copy over parent id and record type
		ssdsDevicePacket.setParentId(devicePacket.getParentId());
		ssdsDevicePacket.setPlatformID(devicePacket.getParentId());
		ssdsDevicePacket.setRecordType(devicePacket.getRecordType());

		// Check to see if it is a MetadataPacket
		if (devicePacket instanceof MetadataPacket) {
			logger.debug("DevicePacket is a MetadataPacket:");
			// Dump the data buffer into this one
			ssdsDevicePacket.setDataBuffer(((MetadataPacket) devicePacket)
					.getBytes());
			// Dump the cause buffer into the other buffer
			ssdsDevicePacket.setOtherBuffer(((MetadataPacket) devicePacket)
					.cause());
			// Set the packetType to 1
			ssdsDevicePacket.setPacketType(0);
			// Set the recordtype to zero also
			ssdsDevicePacket.setRecordType(0L);
			// Set the metadata sequence number to this sequence number
			ssdsDevicePacket
					.setMetadataSequenceNumber(((MetadataPacket) devicePacket)
							.metadataRef());
			ssdsDevicePacket
					.setDataDescriptionVersion(((MetadataPacket) devicePacket)
							.metadataRef());
		}

		// Now check to see if this is a SensorDataPacket
		if (devicePacket instanceof SensorDataPacket) {
			logger.debug("DevicePacket is a SensorDataPacket:");
			StringBuffer hexData = new StringBuffer();
			ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(
					((SensorDataPacket) devicePacket).dataBuffer());
			while (byteArrayIS.available() > 0) {
				hexData.append(Integer.toHexString(
						(0xFF & byteArrayIS.read()) | 0x100).substring(1));
			}

			// Copy the data buffer over
			ssdsDevicePacket.setDataBuffer(((SensorDataPacket) devicePacket)
					.dataBuffer());
			// Set the packetType to 0
			ssdsDevicePacket.setPacketType(1);
			// Set the recordType
			ssdsDevicePacket.setRecordType(((SensorDataPacket) devicePacket)
					.getRecordType());
			// Set the metadata sequence number to the one recieved
			// from the class
			ssdsDevicePacket
					.setMetadataSequenceNumber(((SensorDataPacket) devicePacket)
							.metadataRef());
			ssdsDevicePacket
					.setDataDescriptionVersion(((SensorDataPacket) devicePacket)
							.metadataRef());
		}

		// Check to see if DeviceMessagePacket (works for Measurement packets
		// too
		if (devicePacket instanceof DeviceMessagePacket) {
			logger.debug("DevicePacket is a DeviceMessagePacket:");
			// Dump the message buffer
			ssdsDevicePacket.setDataBuffer(((DeviceMessagePacket) devicePacket)
					.getMessage());
			// Set the packet type
			ssdsDevicePacket.setPacketType(2);
			// Set the metadataSequenceNumber
			ssdsDevicePacket
					.setMetadataSequenceNumber(((DeviceMessagePacket) devicePacket)
							.metadataRef());
			ssdsDevicePacket
					.setDataDescriptionVersion(((DeviceMessagePacket) devicePacket)
							.metadataRef());
		}

		// Summary Packets
		if (devicePacket instanceof SummaryPacket) {
			logger.debug("DevicePacket is a SummaryPacket:");
			// Dump the message buffer
			ssdsDevicePacket.setDataBuffer(((SummaryPacket) devicePacket)
					.getData());
			// Set the packet type
			ssdsDevicePacket.setPacketType(1);
			// Set the metadataSequenceNumber
			ssdsDevicePacket
					.setMetadataSequenceNumber(((SummaryPacket) devicePacket)
							.metadataRef());
			ssdsDevicePacket
					.setDataDescriptionVersion(((SummaryPacket) devicePacket)
							.metadataRef());
		}

		// Now return it
		return ssdsDevicePacket;
	}

	/**
	 * This method takes in a byte array in SIAM exported format and converts to
	 * the SSDS expected format
	 * 
	 * @param siamByteArray
	 *            the incoming byte array in SIAM exported format
	 * @return byte array in SSDS format, returns null if the incoming array is
	 *         empty
	 */
	@SuppressWarnings("unused")
	public static byte[] convertSIAMByteArrayToVersion3SSDSByteArray(
			byte[] siamByteArray, boolean logSIAMByteArray,
			boolean convertBuffersToASCIIInSIAMLogEntry,
			boolean logSSDSByteArray,
			boolean convertBuffersToASCIIInSSDSLogEntry) {

		logger.debug("Conversion of SIAM byte array "
				+ "to SSDS byte array proceeding:");

		// First let's makes sure the array exists
		if (siamByteArray == null || siamByteArray.length <= 0) {
			logger.error("The incoming SIAM byte array was "
					+ "empty returning null");
			return null;
		}

		// First log the siam byte array information
		if (logSIAMByteArray)
			logSIAMMessageByteArray(siamByteArray,
					convertBuffersToASCIIInSIAMLogEntry);

		// The byte array to return
		byte[] ssdsByteArray = null;

		// Create convenience input streams to read from the siam byte array
		ByteArrayInputStream bis = new ByteArrayInputStream(siamByteArray);
		DataInputStream dis = new DataInputStream(bis);

		// Now read in all the information from the SIAM byte array and
		// append to the log message
		try {
			// Read in the StreamID, SSDS basically ignores this
			short streamID = dis.readShort();

			// Read in the devicePacketVersion
			// As of now, this is he class version ID in SIAM and it is always
			// 0, SSDS ignores it
			long devicePacketVersion = dis.readLong();

			// Read in the sourceID
			long sourceID = dis.readLong();

			// Read in the in timestamp
			long timestamp = dis.readLong();

			// Read in the sequence number
			long sequenceNumber = dis.readLong();

			// Read in the metadataref number
			long metadataRef = dis.readLong();

			// Read in the parentID
			long parentID = dis.readLong();

			// Read in the recordType
			long recordType = dis.readLong();

			// Read in the streamID
			short secondStreamID = dis.readShort();

			// Read packet Version
			// As of this writing, this is always the same as the class version
			// ID of the DevicePacket in SIAM which is always 0, SSDS ignores it
			long secondPacketVersion = dis.readLong();

			// Read in the first data buffer length
			int firstBufferLength = 0;
			byte[] firstBufferBytes = null;
			try {
				firstBufferLength = dis.readInt();
			} catch (Exception e) {
				logger.error("An Exception was caught trying to "
						+ "extract the first buffer length by "
						+ "reading in int from siam byte array: "
						+ e.getMessage());
			}
			// If it looks like there is some buffer to read in, go ahead
			// and do sos
			if (firstBufferLength > 0) {
				// Create the array to hold the first buffer
				firstBufferBytes = new byte[firstBufferLength];
				// Now read in the bytes
				dis.read(firstBufferBytes);

			}
			// Now try to read in a length of second buffer
			int secondBufferLength = 0;
			byte[] secondBufferBytes = null;
			try {
				secondBufferLength = dis.readInt();
			} catch (Exception e) {
				if (secondStreamID == Exportable.EX_METADATAPACKET) {
					logger.info("An Exception was caught trying to "
							+ "extract the second buffer length by "
							+ "reading in int from the siam byte array: "
							+ e.getMessage()
							+ ".\n  This is a metadata packet which usually "
							+ "has two buffers, this might be unusual.\n");
				}
			}
			// If it looks like there is a second buffer, try to read it
			if (secondBufferLength > 0) {
				// Create the byte array for the second buffer
				secondBufferBytes = new byte[secondBufferLength];
				// Read them in
				dis.read(secondBufferBytes);
			}

			// OK, we have all the information from the siam byte array, let's
			// construct the SSDS one

			// Some reasoning has to be done to figure out the packetType for
			// SSDS
			int packetType = 0;
			// First we can examine the secondStreamID. First check for metadata
			// packet
			if (secondStreamID == Exportable.EX_METADATAPACKET) {
				// For SSDS, the packet type for Metadata packets is 1
				packetType = 1;
			} else if (secondStreamID == Exportable.EX_SENSORDATAPACKET) {
				// For SSDS, SensorDataPacket (and SummaryPackets) are
				// packetType 0 (data is the most basic packet)
				packetType = 0;
			} else if (secondStreamID == Exportable.EX_DEVICEMESSAGEPACKET) {
				// For SSDS, DeviceMessage packets
				packetType = 4;
			} else {
				// Since it cannot be determined, we will just consider it to be
				// data (the most basic packet), but log a message.
				logger.error("The incoming SIAM secondStreamID of "
						+ secondStreamID
						+ " was not recognized by SSDS so it will be "
						+ "converted to a data packet.");
			}

			// Write the packet sub type (which is their record type)
			long packetSubType = 0;
			if (packetType == 1) {
				// Since this is a metadata packet, the sub type is
				// automatically 0
				packetSubType = 0;
			} else {
				// Write their record type to our sub packet type
				packetSubType = recordType;
			}

			// Now for time stamp, I have to convert it from
			// milliseconds since 1/1/70 to seconds/nanoseconds
			// since 1/1/70
			// TODO kgomes, there is a bug here and is referenced in SSDS-77 in
			// JIRA.
			long timestampSeconds = DateUtils
					.getEpochTimestampSecondsFromEpochMillis(timestamp);
			long timestampNanoseconds = DateUtils
					.getNanosecondsFromEpochMillis(timestamp);

			// If it is a metadata packet, I need to reverse the buffers and I
			// want the byte buffers as the main payload and the "cause" as the
			// secondary
			if (packetType == 1) {
				// Construct the SSDS formatted byte array
				ssdsByteArray = createVersion3SSDSByteArray(sourceID, parentID,
						packetType, packetSubType, metadataRef, metadataRef,
						timestampSeconds, timestampNanoseconds, sequenceNumber,
						secondBufferBytes, firstBufferBytes);
			} else {
				// Construct the SSDS formatted byte array
				ssdsByteArray = createVersion3SSDSByteArray(sourceID, parentID,
						packetType, packetSubType, metadataRef, metadataRef,
						timestampSeconds, timestampNanoseconds, sequenceNumber,
						firstBufferBytes, secondBufferBytes);
			}

		} catch (IOException e) {
			logger.error("\nAn IOException was trapped during the conversion: "
					+ e.getMessage() + "\n");
		} catch (Exception e) {
			logger.error("\nAn Exception was trapped during the conversion: "
					+ e.getMessage() + "\n");
		}

		// Now if there is something in the output byte array, return it,
		// otherwise return null
		if (ssdsByteArray != null && ssdsByteArray.length > 0
				&& logSSDSByteArray) {
			logVersion3SSDSByteArray(ssdsByteArray,
					convertBuffersToASCIIInSSDSLogEntry);
		}

		// Return the byte array
		return ssdsByteArray;
	}

	/**
	 * This method takes in a SSDSDevicePacket and extracts the information into
	 * the proper byte array structure
	 * 
	 * @param ssdsDevicePacket
	 * @return
	 */
	public static byte[] convertSSDSDevicePacketToVersion3SSDSByteArray(
			SSDSDevicePacket ssdsDevicePacket) {
		// Make sure the packet is not null
		if (ssdsDevicePacket == null)
			return null;

		// There are some translation rules that apply to the translation
		// between SSDSDevicePackets and SSDS byte arrays. They mostly come from
		// legacy of development
		int packetType = 0;
		if (ssdsDevicePacket.getPacketType() == 0) {
			packetType = 1;
		} else if (ssdsDevicePacket.getPacketType() == 1) {
			packetType = 0;
		} else if (ssdsDevicePacket.getPacketType() == 2) {
			packetType = 4;
		}

		long recordType = 0;
		if (ssdsDevicePacket.getPacketType() == 0) {
			recordType = 0;
		} else {
			recordType = ssdsDevicePacket.getRecordType();
		}

		return createVersion3SSDSByteArray(ssdsDevicePacket.sourceID(),
				ssdsDevicePacket.getParentId(), packetType, recordType,
				ssdsDevicePacket.getMetadataSequenceNumber(), ssdsDevicePacket
						.getDataDescriptionVersion(), ssdsDevicePacket
						.getTimestampSeconds(), ssdsDevicePacket
						.getTimestampNanoseconds(), ssdsDevicePacket
						.sequenceNo(), ssdsDevicePacket.getDataBuffer(),
				ssdsDevicePacket.getOtherBuffer());
	}

	/**
	 * This method takes in a byte array that is in SSDS and <b>DOES HAVE</b> an
	 * integer at the front of the byte array that indicates what version of
	 * byte array it is.
	 * 
	 * @param ssdsByteArray
	 * @return
	 */
	public static SSDSDevicePacket convertSSDSByteArrayToSSDSDevicePacket(
			byte[] ssdsByteArray, boolean convertToGeoPacket) {
		// The packet to return
		SSDSDevicePacket ssdsDevicePacket = null;

		// OK now parse out the keys from the byte array
		DataInputStream dataInputStream = new DataInputStream(
				new ByteArrayInputStream(ssdsByteArray));
		// Read in the version
		int ssdsPacketVersion = -1;
		try {
			ssdsPacketVersion = dataInputStream.readInt();
		} catch (IOException e) {
			logger.error("IOException caught trying to read "
					+ "the packet version from the byte array");
		}
		// Check the version
		if (ssdsPacketVersion == 3) {
			// A new non-version numbered byte array
			int numBytesAvailable = 0;
			byte[] version3ByteArray = null;
			try {
				numBytesAvailable = dataInputStream.available();
				version3ByteArray = new byte[numBytesAvailable];
				dataInputStream.read(version3ByteArray);
			} catch (IOException e) {
				logger.error("IOException caught trying to read " + "the rest("
						+ numBytesAvailable
						+ ") of the bytes from the byte array");
			}
			ssdsDevicePacket = convertVersion3SSDSByteArrayToSSDSDevicePacket(
					version3ByteArray, convertToGeoPacket);
		}

		// Now return it
		return ssdsDevicePacket;
	}

	/**
	 * This method was created due to the fact that the queries that come back
	 * from a PackSQLQuery have a version int at the beginning and no deviceID.
	 * This method takes in that byte array and a deviceID and constructs a
	 * propery SSDS formatted byte array.
	 * 
	 * @param byteArrayWithVersionAndNoDevice
	 * @param deviceID
	 * @return
	 */
	public static byte[] stripOffVersionAndAddDeviceIDInFront(
			byte[] byteArrayWithVersionAndNoDevice, long deviceID) {
		// The byte array to return
		byte[] byteArrayToReturn = null;

		// Make sure it is not null
		if (byteArrayWithVersionAndNoDevice != null) {

			// Create a byte array output to enable us to insert the deviceID
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(
					byteArrayOutputStream);

			// The first thing is to strip off the packet version
			// OK now parse out the keys from the byte array
			DataInputStream dataInputStream = new DataInputStream(
					new ByteArrayInputStream(byteArrayWithVersionAndNoDevice));

			// Read in the version
			int ssdsPacketVersion = -1;
			try {
				ssdsPacketVersion = dataInputStream.readInt();
			} catch (IOException e) {
				logger.error("IOException caught trying to read "
						+ "the packet version from the byte array");
			}
			// Check the version
			if (ssdsPacketVersion == 3) {
				try {
					// First slip in the deviceID
					dataOutputStream.writeLong(deviceID);

					// The rest of the payload needs to be written
					int numBytesAvailable = 0;
					byte[] restOfPayload = null;
					numBytesAvailable = dataInputStream.available();
					restOfPayload = new byte[numBytesAvailable];
					dataInputStream.read(restOfPayload);

					// Now write it
					dataOutputStream.write(restOfPayload);

					// Now assign it to the return value
					byteArrayToReturn = byteArrayOutputStream.toByteArray();
				} catch (IOException e) {
					logger.error("IOException caught trying to read bytes "
							+ "from incoming array, slip in device "
							+ "ID and write rest of the array: "
							+ e.getMessage());
				}
			}

		}
		// Return the results
		return byteArrayToReturn;
	}

	/**
	 * This method takes in a SSDS formatted byte array and converts it to a new
	 * SSDSDevicePacket
	 * 
	 * @param ssdsByteArray
	 * @return
	 */
	public static SSDSDevicePacket convertVersion3SSDSByteArrayToSSDSDevicePacket(
			byte[] ssdsByteArray, boolean convertToGeoPacket) {

		// OK now parse out the keys from the byte array
		DataInputStream dataInputStream = new DataInputStream(
				new ByteArrayInputStream(ssdsByteArray));
		long sourceID = -99;
		long platformID = -99;
		int packetType = -99;
		long recordType = -99;
		long metadataSequenceNumber = -99;
		long dataDescriptionVersion = -99;
		long systemTimeSeconds = -99;
		long systemTimeNanoseconds = -99;
		long sequenceNumber = -99;
		int bufferSize = -99;
		byte[] buffer = null;
		int bufferTwoSize = -99;
		byte[] bufferTwo = null;
		try {
			sourceID = dataInputStream.readLong();
			platformID = dataInputStream.readLong();
			int ssdsPacketType = dataInputStream.readInt();
			if (ssdsPacketType == 0) {
				packetType = 1;
			} else if (ssdsPacketType == 1) {
				packetType = 0;
			} else if (ssdsPacketType == 4) {
				packetType = 2;
			}
			recordType = dataInputStream.readLong();
			metadataSequenceNumber = dataInputStream.readLong();
			dataDescriptionVersion = dataInputStream.readLong();
			systemTimeSeconds = dataInputStream.readLong();
			systemTimeNanoseconds = dataInputStream.readLong();
			sequenceNumber = dataInputStream.readLong();
			bufferSize = dataInputStream.readInt();
			buffer = new byte[bufferSize];
			// Read in the data buffer
			dataInputStream.read(buffer);
			// Read in the size of the secondary buffer
			bufferTwoSize = dataInputStream.readInt();
			bufferTwo = new byte[bufferTwoSize];
			// Read in the buffer
			dataInputStream.read(bufferTwo);
		} catch (IOException e) {
			logger.error("IException caught reading from byte array: "
					+ e.getMessage());
		}

		// Create a new packet
		SSDSDevicePacket packet = null;
		if (convertToGeoPacket) {
			packet = new SSDSGeoLocatedDevicePacket(sourceID);
		} else {
			packet = new SSDSDevicePacket(sourceID);
		}
		packet.setPlatformID(platformID);
		packet.setPacketType(packetType);
		packet.setRecordType(recordType);
		packet.setMetadataSequenceNumber(metadataSequenceNumber);
		packet.setDataDescriptionVersion(dataDescriptionVersion);
		packet.setSystemTime(DateUtils
				.constructEpochMillisFromEpochSecondsAndNanoseconds(
						systemTimeSeconds, systemTimeNanoseconds));
		packet.setSequenceNo(sequenceNumber);
		packet.setDataBuffer(buffer);
		packet.setOtherBuffer(bufferTwo);

		// Return the packet
		return packet;

	}
}
