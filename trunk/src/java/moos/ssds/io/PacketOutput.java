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

import java.io.DataOutputStream;
import java.io.File;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;

import javax.jms.BytesMessage;

import moos.ssds.io.util.PacketUtility;

import org.apache.log4j.Logger;

/**
 * <p>
 * Class for writing <code>DataPackets</code> to a file. This does not serialize
 * objects. Instead, only the data is written out (as binary).
 * </p>
 * 
 * <pre>
 *  Use as:
 * 		// Open a location for writing. If the file already exists
 * 		// DataPackets will be appended to the file.
 * 		PacketOuput out = new PacketOutput(new File(&quot;somefilename&quot;));
 * 		//or:
 * 	    PacketOutput out = new PacketOutput();
 * 	out.setFile(new File(&quot;somefilename&quot;));
 * 		// Write your dataPackets
 *      out.writeObject(new DataPacket(0, 10);
 *      // Close the output when done
 * 	out.close()
 * 
 * </pre>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.6 $
 */
public class PacketOutput {

	/**
	 * This is the <code>DataOutputStream</code> that is used to write the
	 * packet information out to a file.
	 */
	private DataOutputStream out;

	/**
	 * This is the <code>File</code> that the packet information will be
	 * serialized out to.
	 */
	private File file;

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(PacketOutput.class);

	/**
	 * No argument constructor. If created with this you will need to call the
	 * <code>setFile(File file)</code> method before calling
	 * <code>writeObject</code>
	 */
	public PacketOutput() {
		logger.debug("Default constructor called");
	}

	/**
	 * This is the constructor that takes in the <code>File</code> where the
	 * packets will be serialized to.
	 * 
	 * @param file
	 *            is the <code>File</code> where the
	 *            <code>SSDSDevicePackets</code> will be written to.
	 */
	public PacketOutput(File file) throws IOException {
		logger.debug("Constructor called with input file: " + file);
		setFile(file);
	}

	/**
	 * Specifies the location to write <code>SSDSDevicePackets</code> to. If the
	 * file already exists new data will be appended to the file. <br>
	 * <br>
	 * 
	 * Calling <code>setFile</code> closes the existing location and opens a new
	 * one. So <code>PacketOutput</code> can be redirected while
	 * <code>PacketOutput</code> is running.
	 * 
	 * @param file
	 *            is the <code>File</code> where the
	 *            <code>SSDSDevicePackets</code> will be written to.
	 */
	public void setFile(File file) throws IOException {

		logger.debug("setFile called with file: " + file);
		if (file != null) {
			logger.debug("file.toString() = " + file.toString());
			logger.debug("file.getAbsolutePath() = " + file.getAbsolutePath());
		}

		// If the data output stream exists, close it first
		if (out != null) {
			out.close();
		}
		// Set the file
		this.file = file;

		// Create the file output stream
		FileOutputStream fos = null;

		// Append to the file if it already exists
		if (file.length() > 0) {
			fos = new FileOutputStream(file.getAbsolutePath(), true);
		} else {
			fos = new FileOutputStream(file);
		}
		// Now create the data output stream
		out = new DataOutputStream(new BufferedOutputStream(fos));
	}

	/**
	 * This method returns the <code>File</code> where this
	 * <code>PacketOutput</code> is serializing <code>SSDSDevicePackets</code>
	 * to.
	 * 
	 * @return The <code>File</code> that an instance of PacketOutput is writing
	 *         to.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * This method closes the <code>DataOutputStream<code> that an instance of
	 * <code>PacketOutput</code> is serialzing packets to.
	 */
	public void close() throws IOException {
		out.close();
	}

	/**
	 * This is the method used to serialize <code>SSDSDevicePacket</code>s to a
	 * file. It calls private methods that format the output depending on the
	 * VERSION_ID of the <code>SSDSDevicePacket</code> being written.
	 * 
	 * @param packet
	 *            Is the <code>SSDSDevicePacket</code> to be serialized
	 */
	public synchronized void writeObject(SSDSDevicePacket packet)
			throws IOException {
		this.setFile(file);
		// logger.debug("writeObject() called:");
		// Write out some ascii seperators to help with visual editing of the
		switch (SSDSDevicePacket.VERSION_ID) {
		case 1:
			// logger.debug("Serializing version 1");
			writeVersion1(packet);
			break;
		case 2:
			// logger.debug("Serializing version 2");
			writeVersion2(packet);
			break;
		case 3:
			// logger.debug("Serializing version 3");
			writeVersion3(packet);
			break;
		}
		// Flush the output
		this.flush();
		// Close the output
		this.close();
	}

	/**
	 * This writes the byte array out in the current version. There really is no
	 * "version" control here except with what is hard coded.
	 * 
	 * @param bytes
	 */
	public synchronized void writeBytesMessage(BytesMessage bytesMessage)
			throws IOException {
		this.writeBytesMessageVersion3(bytesMessage);
		// Flush the output
		this.flush();
	}

	/**
	 * This is the method to serialize the first version of
	 * <code>SSDSDevicePacket</code> to a file
	 * 
	 * @deprecated
	 * @param packet
	 *            is the <code>SSDSDevicePacket</code> to serialize
	 * @throws IOException
	 *             is thrown if something goes wrong
	 * 
	 */
	private void writeVersion1(SSDSDevicePacket packet) throws IOException {
		// logger.debug("writeVersion 1 called");
		out.writeInt(SSDSDevicePacket.VERSION_ID);
		out.writeLong(packet.sourceID());
		out.writeLong(packet.getMetadataSequenceNumber());
		out.writeLong(packet.getRecordType());
		out.writeLong(packet.getPlatformID());
		out.writeLong(packet.systemTime());
		out.writeLong(packet.sequenceNo());
		byte[] buffer = packet.getDataBuffer();
		if (buffer != null) {
			out.writeInt(packet.getDataBuffer().length);
			out.write(packet.getDataBuffer());
		} else {
			out.writeInt(1);
			out.write(new byte[1]);
		}
	}

	/**
	 * This is the write method to record the second version of packet to a
	 * stream. This was added so that a second byte array was written to the
	 * serialization mechanism.
	 * 
	 * @deprecated
	 * @param packet
	 * @throws IOException
	 */
	private void writeVersion2(SSDSDevicePacket packet) throws IOException {
		// logger.debug("writeVersion2 called");
		out.writeInt(SSDSDevicePacket.VERSION_ID);
		out.writeLong(packet.sourceID());
		out.writeInt(packet.getPacketType());
		out.writeLong(packet.getMetadataSequenceNumber());
		out.writeLong(packet.getRecordType());
		out.writeLong(packet.getPlatformID());
		out.writeLong(packet.systemTime());
		out.writeLong(packet.sequenceNo());
		byte[] buffer = packet.getDataBuffer();
		if (buffer != null) {
			out.writeInt(packet.getDataBuffer().length);
			out.write(packet.getDataBuffer());
		} else {
			out.writeInt(1);
			out.write(new byte[1]);
		}
		byte[] secondBuffer = packet.getOtherBuffer();
		if (secondBuffer != null) {
			out.writeInt(packet.getOtherBuffer().length);
			out.write(packet.getOtherBuffer());
		} else {
			out.writeInt(1);
			out.write(new byte[1]);
		}
	}

	/**
	 * This is the write method to record the third version of packet to a
	 * stream. This was added so that a second byte array was written to the
	 * serialization mechanism.
	 * 
	 * @param packet
	 * @throws IOException
	 */
	private void writeVersion3(SSDSDevicePacket packet) throws IOException {
		// Convert to a byte array and write
		writeByteArrayVersion3(PacketUtility
				.convertSSDSDevicePacketToSSDSByteArray(packet));
	}

	/**
	 * This method takes in a BytesMessage that should have the SSDS format of
	 * byte array and then writes to disk in format version 3
	 * 
	 * @param bytesMessage
	 * @throws IOException
	 */
	private void writeBytesMessageVersion3(BytesMessage bytesMessage)
			throws IOException {
		// Extract the byte array and write to disk
		writeByteArrayVersion3(PacketUtility
				.extractByteArrayFromBytesMessage(bytesMessage));
	}

	/**
	 * This method takes in a SSDS formatted byte array and writes to disk in
	 * the version 3 format
	 * 
	 * @param ssdsBytes
	 */
	private void writeByteArrayVersion3(byte[] ssdsBytes) {
		// Make sure the array is not null
		if (ssdsBytes != null) {
			// Create convenience input streams to read from the byte array
			ByteArrayInputStream bis = new ByteArrayInputStream(ssdsBytes);
			DataInputStream dis = new DataInputStream(bis);

			try {
				// Write out the serial number
				out.writeInt(3);

				// Write out device ID
				long deviceID = dis.readLong();
				out.writeLong(deviceID);

				// Write out parent ID
				long parentID = dis.readLong();
				out.writeLong(parentID);

				// Write out the packet type
				int packetType = dis.readInt();
				out.writeInt(packetType);

				// Write out the packet subType
				long packetSubType = dis.readLong();
				out.writeLong(packetSubType);

				// Write out the DataDescriptionID
				long dataDescriptionID = dis.readLong();
				out.writeLong(dataDescriptionID);

				// Write out the DataDescriptionVersion
				long dataDescriptionVersion = dis.readLong();
				out.writeLong(dataDescriptionVersion);

				// Write out the timestamp seconds
				long timestampSeconds = dis.readLong();
				out.writeLong(timestampSeconds);

				// Write out the timestamp nanoseconds
				long timestampNanoseconds = dis.readLong();
				out.writeLong(timestampNanoseconds);

				// Write out the sequence number
				long sequenceNumber = dis.readLong();
				out.writeLong(sequenceNumber);

				// Write out first buffer
				int bufferLen = dis.readInt();
				out.writeInt(bufferLen);
				byte[] bufferBytes = new byte[bufferLen];
				dis.read(bufferBytes);
				out.write(bufferBytes);

				// Write out second buffer
				int bufferTwoLen = dis.readInt();
				out.writeInt(bufferTwoLen);
				byte[] bufferTwoBytes = new byte[bufferTwoLen];
				dis.read(bufferTwoBytes);
				out.write(bufferTwoBytes);

				// Write all this to a logger
				// Debugging stuff
				StringBuffer hexData = new StringBuffer();
				ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(
						bufferBytes);
				while (byteArrayIS.available() > 0) {
					hexData.append(Integer.toHexString(
							(0xFF & byteArrayIS.read()) | 0x100).substring(1));
				}
				StringBuffer hexTwoData = new StringBuffer();
				ByteArrayInputStream byteTwoArrayIS = new ByteArrayInputStream(
						bufferTwoBytes);
				while (byteTwoArrayIS.available() > 0) {
					hexData.append(Integer.toHexString(
							(0xFF & byteTwoArrayIS.read()) | 0x100)
							.substring(1));
				}
				logger.debug("Got bytesMessage and wrote to disk file "
						+ file.getAbsolutePath() + ":" + "deviceID=" + deviceID
						+ "," + "parentID=" + parentID + "," + "packetType="
						+ packetType + "," + "packetSubType=" + packetSubType
						+ "," + "dataDescriptionID=" + dataDescriptionID + ","
						+ "timestampSeconds=" + timestampSeconds + ","
						+ "timestampNanoseconds=" + timestampNanoseconds + ","
						+ "sequenceNumber=" + sequenceNumber + ","
						+ "bufferLen=" + bufferLen + ","
						+ "bufferBytes(in hex)=" + hexData.toString()
						+ "bufferTwoLen=" + bufferTwoLen + ","
						+ "bufferTwoBytes(in hex)=" + hexTwoData.toString());
			} catch (IOException e) {
				logger.error("IOException caught writing SSDS "
						+ "byte array to disk: " + e.getMessage());
			}
		}
	}

	/**
	 * This is the method that is called when the object is cleaned up and
	 * closes all the data streams
	 */
	public void finalize() {
		try {
			super.finalize();
			if (out != null) {
				out.close();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * This method flushes the data to the output stream
	 * 
	 * @throws IOException
	 *             if something goes wrong
	 */
	public void flush() throws IOException {
		out.flush();
	}

}
