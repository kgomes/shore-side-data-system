package test.moos.ssds.io;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import junit.framework.TestCase;
import moos.ssds.io.PacketInput;
import moos.ssds.io.PacketOutput;
import moos.ssds.io.PacketOutputManager;
import moos.ssds.io.SSDSDevicePacket;
import moos.ssds.io.util.PacketUtility;
import moos.ssds.util.DateUtils;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class TestPacketOutput extends TestCase {

	static Logger logger = Logger.getLogger(TestPacketOutput.class);

	public TestPacketOutput(String name) {
		super(name);

		Properties log4jProperties = new Properties();
		try {
			log4jProperties.load(this.getClass().getResourceAsStream(
					"/log4j.properties"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		PropertyConfigurator.configure(log4jProperties);

	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testWriteSSDSByteArrayToFileReadBackSSDSDevicePacket() {
		// Construct a SSDS byte array
		long sourceID = 101;
		long parentID = 100;
		// The type of packet to be sent Data = 0, Metadata = 1, Device Message
		// = 4
		int packetType = 0;
		// For legacy reasons, there is a mapping that happens between packet
		// types on SSDSDevicePackets and the raw byte array
		// Byte Array | SSDSDevicePacket
		// 0 | 1
		// 1 | 0
		// 4 | 2
		int equivalentSSDSDevicePacketType = 1;

		// This is the record type
		long packetSubType = 1;
		long metadataSequenceNumber = 0;
		long dataDescriptionVersion = 0;
		Date now = new Date();
		long timestampSeconds = DateUtils.getEpochTimestampSeconds(now);
		long timestampNanoseconds = DateUtils.getNanoseconds(now);
		long sequenceNumber = 1;
		byte[] firstBuffer = "Sensor Data First Buffer".getBytes();
		byte[] secondBuffer = "SensorData Second Buffer (ignored)".getBytes();

		// Create and publish the byte array in SSDS format
		PacketOutput packetOutput = PacketOutputManager.getPacketOutput(
				sourceID, dataDescriptionVersion, packetSubType, parentID);

		SSDSDevicePacket lastPacket = null;
		try {
			// Now write the packet
			packetOutput.writeBytes(PacketUtility.createVersion3SSDSByteArray(
					sourceID, parentID, packetType, packetSubType,
					metadataSequenceNumber, dataDescriptionVersion,
					timestampSeconds, timestampNanoseconds, sequenceNumber,
					firstBuffer, secondBuffer));

			// Now read it back
			PacketInput packetInput = new PacketInput(packetOutput.getFile());
			// Now read elements until the end
			while (packetInput.hasMoreElements()) {
				Object nextElement = packetInput.nextElement();
				// Make sure it is a SSDSDevicePacket
				if (nextElement instanceof SSDSDevicePacket) {
					// Cast it
					lastPacket = (SSDSDevicePacket) nextElement;
				}
			}
		} catch (IOException e) {
			logger.error("IOException caught: " + e.getMessage());
		}
		// Now test
		assertEquals("SourceIDs should be the same", sourceID, lastPacket
				.sourceID());
		assertEquals("ParentIDs should be the same", parentID, lastPacket
				.getParentId());
		assertEquals("PacketType should be the same",
				equivalentSSDSDevicePacketType, lastPacket.getPacketType());
		assertEquals("PacketSubType/RecordType should be the same",
				packetSubType, lastPacket.getRecordType());
		assertEquals("MetadataSequenceNumber should be the same",
				metadataSequenceNumber, lastPacket.getMetadataSequenceNumber());
		assertEquals("DataDescriptionVerion should be the same",
				dataDescriptionVersion, lastPacket.getDataDescriptionVersion());
		assertEquals("TimestampSeconds should be the same", timestampSeconds,
				lastPacket.getTimestampSeconds());
		assertEquals("TimestampNanoseconds should be the same",
				timestampNanoseconds, lastPacket.getTimestampNanoseconds());
		assertEquals("SequenceNumber should be the same", sequenceNumber,
				lastPacket.sequenceNo());
		assertTrue("FirstBuffer should be the same", Arrays.equals(firstBuffer,
				lastPacket.getDataBuffer()));
		assertTrue("SecondBuffer should be the same", Arrays.equals(
				secondBuffer, lastPacket.getOtherBuffer()));
	}

	public void testWriteSSDSDevicePacketReadSSDSDevicePacket() {
		// Create a SSDSDevicePacket
		SSDSDevicePacket ssdsDevicePacket = new SSDSDevicePacket(101);
		Date now = new Date();
		ssdsDevicePacket.setSystemTime(now.getTime());
		ssdsDevicePacket.setSequenceNo(1);
		ssdsDevicePacket.setMetadataSequenceNumber(0);
		ssdsDevicePacket.setParentId(100);
		ssdsDevicePacket.setRecordType(999);
		ssdsDevicePacket.setPacketType(1);
		ssdsDevicePacket.setDataDescriptionVersion(3);
		ssdsDevicePacket.setDataBuffer("First Buffer Bytes".getBytes());
		ssdsDevicePacket.setOtherBuffer("Other Buffer Bytes".getBytes());

		// Create and publish the byte array in SSDS format
		PacketOutput packetOutput = PacketOutputManager.getPacketOutput(
				ssdsDevicePacket.sourceID(), ssdsDevicePacket
						.getDataDescriptionVersion(), ssdsDevicePacket
						.getRecordType(), ssdsDevicePacket.getParentId());

		SSDSDevicePacket lastPacket = null;
		try {
			// Now write the packet
			packetOutput.writeObject(ssdsDevicePacket);

			// Now read it back
			PacketInput packetInput = new PacketInput(packetOutput.getFile());
			// Now read elements until the end
			while (packetInput.hasMoreElements()) {
				Object nextElement = packetInput.nextElement();
				// Make sure it is a SSDSDevicePacket
				if (nextElement instanceof SSDSDevicePacket) {
					// Cast it
					lastPacket = (SSDSDevicePacket) nextElement;
				}
			}
		} catch (IOException e) {
			logger.error("IOException caught: " + e.getMessage());
		}
		// Now test
		assertEquals("SourceIDs should be the same", ssdsDevicePacket
				.sourceID(), lastPacket.sourceID());
		assertEquals("ParentIDs should be the same", ssdsDevicePacket
				.getParentId(), lastPacket.getParentId());
		assertEquals("PacketType should be the same", ssdsDevicePacket
				.getPacketType(), lastPacket.getPacketType());
		assertEquals("PacketSubType/RecordType should be the same",
				ssdsDevicePacket.getRecordType(), lastPacket.getRecordType());
		assertEquals("MetadataSequenceNumber should be the same",
				ssdsDevicePacket.getMetadataSequenceNumber(), lastPacket
						.getMetadataSequenceNumber());
		assertEquals("DataDescriptionVerion should be the same",
				ssdsDevicePacket.getDataDescriptionVersion(), lastPacket
						.getDataDescriptionVersion());
		assertEquals("TimestampSeconds should be the same", ssdsDevicePacket
				.getTimestampSeconds(), lastPacket.getTimestampSeconds());
		assertEquals("TimestampNanoseconds should be the same",
				ssdsDevicePacket.getTimestampNanoseconds(), lastPacket
						.getTimestampNanoseconds());
		assertEquals("SequenceNumber should be the same", ssdsDevicePacket
				.sequenceNo(), lastPacket.sequenceNo());
		assertTrue("FirstBuffer should be the same", Arrays.equals(
				ssdsDevicePacket.getDataBuffer(), lastPacket.getDataBuffer()));
		assertTrue("SecondBuffer should be the same", Arrays.equals(
				ssdsDevicePacket.getOtherBuffer(), lastPacket.getOtherBuffer()));

		// Create a new packet, but make it a metadata packet
		ssdsDevicePacket = new SSDSDevicePacket(101);
		now = new Date();
		ssdsDevicePacket.setSystemTime(now.getTime());
		ssdsDevicePacket.setSequenceNo(1);
		ssdsDevicePacket.setMetadataSequenceNumber(0);
		ssdsDevicePacket.setParentId(100);
		ssdsDevicePacket.setRecordType(999);
		ssdsDevicePacket.setPacketType(0);
		ssdsDevicePacket.setDataDescriptionVersion(3);
		ssdsDevicePacket.setDataBuffer("First Buffer Bytes".getBytes());
		ssdsDevicePacket.setOtherBuffer("Other Buffer Bytes".getBytes());

		lastPacket = null;
		try {
			// Now write the packet
			packetOutput.writeObject(ssdsDevicePacket);

			// Now read it back
			PacketInput packetInput = new PacketInput(packetOutput.getFile());
			// Now read elements until the end
			while (packetInput.hasMoreElements()) {
				Object nextElement = packetInput.nextElement();
				// Make sure it is a SSDSDevicePacket
				if (nextElement instanceof SSDSDevicePacket) {
					// Cast it
					lastPacket = (SSDSDevicePacket) nextElement;
				}
			}
		} catch (IOException e) {
			logger.error("IOException caught: " + e.getMessage());
		}
		// Now test
		assertEquals("SourceIDs should be the same", ssdsDevicePacket
				.sourceID(), lastPacket.sourceID());
		assertEquals("ParentIDs should be the same", ssdsDevicePacket
				.getParentId(), lastPacket.getParentId());
		assertEquals("PacketType should be the same", ssdsDevicePacket
				.getPacketType(), lastPacket.getPacketType());
		// This tests to make sure the record type is set to zero when it is a
		// metadatapacket despite the fact that it had a record type of 999 on
		// the incoming packet
		assertEquals("PacketSubType/RecordType should be the same", 0,
				lastPacket.getRecordType());
		assertEquals("MetadataSequenceNumber should be the same",
				ssdsDevicePacket.getMetadataSequenceNumber(), lastPacket
						.getMetadataSequenceNumber());
		assertEquals("DataDescriptionVerion should be the same",
				ssdsDevicePacket.getDataDescriptionVersion(), lastPacket
						.getDataDescriptionVersion());
		assertEquals("TimestampSeconds should be the same", ssdsDevicePacket
				.getTimestampSeconds(), lastPacket.getTimestampSeconds());
		assertEquals("TimestampNanoseconds should be the same",
				ssdsDevicePacket.getTimestampNanoseconds(), lastPacket
						.getTimestampNanoseconds());
		assertEquals("SequenceNumber should be the same", ssdsDevicePacket
				.sequenceNo(), lastPacket.sequenceNo());
		assertTrue("FirstBuffer should be the same", Arrays.equals(
				ssdsDevicePacket.getDataBuffer(), lastPacket.getDataBuffer()));
		assertTrue("SecondBuffer should be the same", Arrays.equals(
				ssdsDevicePacket.getOtherBuffer(), lastPacket.getOtherBuffer()));
	}
}
