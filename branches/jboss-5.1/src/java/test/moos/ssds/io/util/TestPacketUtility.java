package test.moos.ssds.io.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;
import moos.ssds.io.SSDSDevicePacket;
import moos.ssds.io.SSDSDevicePacketProto;
import moos.ssds.io.util.PacketUtility;
import moos.ssds.util.DateUtils;
import net.java.jddac.common.type.ArgArray;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.mbari.siam.distributed.DeviceMessagePacket;
import org.mbari.siam.distributed.MeasurementPacket;
import org.mbari.siam.distributed.MetadataPacket;
import org.mbari.siam.distributed.SensorDataPacket;
import org.mbari.siam.distributed.SummaryPacket;
import org.mbari.siam.operations.utils.ExportablePacket;

import com.google.protobuf.ByteString;

public class TestPacketUtility extends TestCase {

	/**
	 * The Apache Log4J Logger
	 */
	private static final Logger logger = Logger
			.getLogger(TestPacketUtility.class);

	/**
	 * This is a session that the publishing of messages will be run in.
	 */
	private TopicSession topicSession = null;

	/**
	 * The device ID to be used in testing
	 */
	private long deviceID = 101;

	/**
	 * The ID of the mythical parent the device is plugged into
	 */
	private long parentID = 100;

	/**
	 * The current time that will be used to tag packets
	 */
	private Date metadataPacketDate = new Date();
	private Date deviceMessagePacketDate = new Date(metadataPacketDate
			.getTime() + 10000);
	private Date measurementPacketDate = new Date(deviceMessagePacketDate
			.getTime() + 10000);
	private Date sensorDataPacketDate = new Date(measurementPacketDate
			.getTime() + 10000);
	private Date summaryPacketDate = new Date(
			sensorDataPacketDate.getTime() + 10000);

	/**
	 * There are several classes that SIAM uses to send information and they are
	 * all subclasses of DevicePacket:
	 * <ol>
	 * <li>MetadataPacket
	 * <li>DeviceMessagePacket
	 * <li>MeasurementPacket (extends DeviceMessagePacket)
	 * <li>SensorDataPacket
	 * <li>SummaryPacket
	 * </ol>
	 */
	// Metadata
	private MetadataPacket metadataPacket = null;
	private long metadataPacketSequenceNumber = 1;
	private long metadataPacketRecordType = 0;
	private String metadataPacketCauseMessage = "Test MetadataPacket cause.";
	private String metadataPacketBytesMessage = "Test MetadataPacket bytes.";
	private byte[] metadataPacketExportedBytes = null;
	private BytesMessage metadataBytesMessage = null;
	// DeviceMessage
	private DeviceMessagePacket deviceMessagePacket = null;
	private long deviceMessagePacketSequenceNumber = 2;
	private long deviceMessagePacketRecordType = 2;
	private String deviceMessageMessage = "Test DeviceMessagePacket message.";
	private byte[] deviceMessagePacketExportedBytes = null;
	private BytesMessage deviceMessageBytesMessage = null;
	// Measurement (subclass of DeviceMessage)
	private MeasurementPacket measurementPacket = null;
	private long measurementPacketSequenceNumber = 3;
	private long measurementPacketRecordType = deviceMessagePacketRecordType;
	private ArgArray measurementPacketArgArray = new ArgArray();
	private byte[] measurementPacketExportedBytes = null;
	private BytesMessage measurementBytesMessage = null;
	// SensorData
	private SensorDataPacket sensorDataPacket = null;
	private String sensorDataPacketBuffer = "Test SensorDataPacket Buffer";
	private long sensorDataPacketSequenceNumber = 4;
	private long sensorDataPacketRecordType = 1;
	private byte[] sensorDataPacketExportedBytes = null;
	private BytesMessage sensorDataBytesMessage = null;
	// Summary
	private SummaryPacket summaryPacket = null;
	private String summaryPacketData = "Test SummaryPacket Data";
	private long summaryPacketSequenceNumber = 5;
	private long summaryPacketRecordType = 1000;
	private byte[] summaryPacketExportedBytes = null;
	private BytesMessage summaryPacketBytesMessage = null;

	/**
	 * The test constructor
	 */
	public TestPacketUtility(String arg0) {
		super(arg0);
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

	/**
	 * The setup method that is called before each test is run
	 */
	protected void setUp() throws Exception {

		// Call the parent setup
		super.setUp();
		logger.debug("setUp called.");

		// Load the properties for testing
		Properties jmsProps = new Properties();
		jmsProps.load(this.getClass().getResourceAsStream(
				"/moos/ssds/jms/jms.properties"));

		// Set the local default topic name
		String defaultTopicname = jmsProps.getProperty("ssds.jms.topic");
		logger.debug("defaultTopicname = " + defaultTopicname);

		// Grab the JNDI context (this will be constructed from the
		// jndi.properites that is found on the classpath
		Context jndiContext = new InitialContext();
		logger.debug("JNDI environment = " + jndiContext.getEnvironment());

		// Now try to grab the connection factory for topics from the initial
		// context
		TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) jndiContext
				.lookup(jmsProps
						.getProperty("ssds.jms.topic.connection.factory.jndi.name"));
		logger.debug("TopicConnectionFactory = " + topicConnectionFactory);

		// Create a connection
		TopicConnection topicConnection = topicConnectionFactory
				.createTopicConnection();
		logger.debug("TopicConnection = " + topicConnection);

		// Get the topic session
		topicSession = topicConnection.createTopicSession(false,
				Session.AUTO_ACKNOWLEDGE);
		logger.debug("TopicSession = " + topicSession);

		// Let's create the objects to convert the objects (packets) to byte
		// arrays that are in the SIAM export format
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ExportablePacket exportablePacket = new ExportablePacket();

		// MetadataPacket comes first
		metadataPacket = new MetadataPacket(deviceID,
				metadataPacketCauseMessage.getBytes(),
				metadataPacketBytesMessage.getBytes());
		metadataPacket.setMetadataRef(0);
		metadataPacket.setParentId(parentID);
		metadataPacket.setRecordType(metadataPacketRecordType);
		metadataPacket.setSequenceNo(metadataPacketSequenceNumber);
		metadataPacket.setSystemTime(metadataPacketDate.getTime());
		exportablePacket.wrapPacket(metadataPacket);
		exportablePacket.export(dos);
		metadataPacketExportedBytes = bos.toByteArray();
		metadataBytesMessage = topicSession.createBytesMessage();
		metadataBytesMessage.writeBytes(metadataPacketExportedBytes);
		metadataBytesMessage.reset();

		// Now DeviceMessagePacket
		deviceMessagePacket = new DeviceMessagePacket(deviceID);
		deviceMessagePacket.setMessage(deviceMessagePacketDate.getTime(),
				deviceMessageMessage.getBytes());
		deviceMessagePacket.setMetadataRef(metadataPacketSequenceNumber);
		deviceMessagePacket.setParentId(parentID);
		deviceMessagePacket.setRecordType(deviceMessagePacketRecordType);
		deviceMessagePacket.setSequenceNo(deviceMessagePacketSequenceNumber);
		deviceMessagePacket.setSystemTime(deviceMessagePacketDate.getTime());
		bos = new ByteArrayOutputStream();
		dos = new DataOutputStream(bos);
		exportablePacket.wrapPacket(deviceMessagePacket);
		exportablePacket.export(dos);
		deviceMessagePacketExportedBytes = bos.toByteArray();
		deviceMessageBytesMessage = topicSession.createBytesMessage();
		deviceMessageBytesMessage.writeBytes(deviceMessagePacketExportedBytes);
		deviceMessageBytesMessage.reset();

		// Followed by MeasurementPacket
		measurementPacketArgArray.put("TestKey1", "TestKeyValue1");
		measurementPacketArgArray.put("TestKey2", "TestValue2");
		measurementPacket = new MeasurementPacket(deviceID,
				measurementPacketArgArray);
		measurementPacket.setMetadataRef(metadataPacketSequenceNumber);
		measurementPacket.setParentId(parentID);
		measurementPacket.setRecordType(measurementPacketRecordType);
		measurementPacket.setSequenceNo(measurementPacketSequenceNumber);
		measurementPacket.setSystemTime(measurementPacketDate.getTime());
		bos = new ByteArrayOutputStream();
		dos = new DataOutputStream(bos);
		exportablePacket.wrapPacket(measurementPacket);
		exportablePacket.export(dos);
		measurementPacketExportedBytes = bos.toByteArray();
		measurementBytesMessage = topicSession.createBytesMessage();
		measurementBytesMessage.writeBytes(measurementPacketExportedBytes);
		measurementBytesMessage.reset();

		// SensorDataPacket
		sensorDataPacket = new SensorDataPacket(deviceID, 100000);
		sensorDataPacket.setDataBuffer(sensorDataPacketBuffer.getBytes());
		sensorDataPacket.setMetadataRef(metadataPacketSequenceNumber);
		sensorDataPacket.setParentId(parentID);
		sensorDataPacket.setRecordType(sensorDataPacketRecordType);
		sensorDataPacket.setSequenceNo(sensorDataPacketSequenceNumber);
		sensorDataPacket.setSystemTime(sensorDataPacketDate.getTime());
		bos = new ByteArrayOutputStream();
		dos = new DataOutputStream(bos);
		exportablePacket.wrapPacket(sensorDataPacket);
		exportablePacket.export(dos);
		sensorDataPacketExportedBytes = bos.toByteArray();
		sensorDataBytesMessage = topicSession.createBytesMessage();
		sensorDataBytesMessage.writeBytes(sensorDataPacketExportedBytes);
		sensorDataBytesMessage.reset();

		// SummaryPacket
		summaryPacket = new SummaryPacket(deviceID);
		summaryPacket.setData(summaryPacketDate.getTime(), summaryPacketData
				.getBytes());
		summaryPacket.setMetadataRef(metadataPacketSequenceNumber);
		summaryPacket.setParentId(parentID);
		summaryPacket.setRecordType(summaryPacketRecordType);
		summaryPacket.setSequenceNo(summaryPacketSequenceNumber);
		summaryPacket.setSystemTime(summaryPacketDate.getTime());
		bos = new ByteArrayOutputStream();
		dos = new DataOutputStream(bos);
		exportablePacket.wrapPacket(summaryPacket);
		exportablePacket.export(dos);
		summaryPacketExportedBytes = bos.toByteArray();
		summaryPacketBytesMessage = topicSession.createBytesMessage();
		summaryPacketBytesMessage.writeBytes(summaryPacketExportedBytes);
		summaryPacketBytesMessage.reset();

		// OK all BytesMessages should be created
	}

	/**
	 * This method is called after each test is run
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * This method tests the extraction of SIAM exported byte arrays inside
	 * BytesMessages to byte arrays which are in the expected SIAM format
	 */
	public void testExtractSIAMByteArrayFromMessage() {

		// Run the test on the MetadataPacket
		assertTrue(
				"MetadataPacket byte structures should be identical",
				testSIAMByteArray(
						PacketUtility
								.extractByteArrayFromBytesMessage(metadataBytesMessage),
						(short) 0x0100, 0L, deviceID, metadataPacketDate
								.getTime(), metadataPacketSequenceNumber, 0L,
						parentID, metadataPacketRecordType, (short) 0x0101, 0L,
						metadataPacketCauseMessage.getBytes().length,
						metadataPacketCauseMessage.getBytes(),
						metadataPacketBytesMessage.getBytes().length,
						metadataPacketBytesMessage.getBytes()));

		// Now for the DeviceMessage
		assertTrue(
				"DeviceMessage byte structures should be identical",
				testSIAMByteArray(
						PacketUtility
								.extractByteArrayFromBytesMessage(deviceMessageBytesMessage),
						(short) 0x0100, 0L, deviceID, deviceMessagePacketDate
								.getTime(), deviceMessagePacketSequenceNumber,
						metadataPacketSequenceNumber, parentID,
						deviceMessagePacketRecordType, (short) 0x0103, 0L,
						deviceMessageMessage.getBytes().length,
						deviceMessageMessage.getBytes(), 0, null));

		// Now for the Measurement packet
		ArgArray targetMeasurementPacketArgArray = new ArgArray();
		targetMeasurementPacketArgArray.put("TestKey1", "TestKeyValue1");
		targetMeasurementPacketArgArray.put("TestKey2", "TestValue2");
		assertTrue(
				"Measurement byte structures should be identical",
				testSIAMByteArray(
						PacketUtility
								.extractByteArrayFromBytesMessage(measurementBytesMessage),
						(short) 0x0100,
						0L,
						deviceID,
						measurementPacketDate.getTime(),
						measurementPacketSequenceNumber,
						metadataPacketSequenceNumber,
						parentID,
						measurementPacketRecordType,
						(short) 0x0103,
						0L,
						targetMeasurementPacketArgArray.toString().getBytes().length,
						targetMeasurementPacketArgArray.toString().getBytes(),
						0, null));

		// Now for the SensorDataPacket
		assertTrue(
				"SensorDataPacket byte structures should be identical",
				testSIAMByteArray(
						PacketUtility
								.extractByteArrayFromBytesMessage(sensorDataBytesMessage),
						(short) 0x0100, 0L, deviceID, sensorDataPacketDate
								.getTime(), sensorDataPacketSequenceNumber,
						metadataPacketSequenceNumber, parentID,
						sensorDataPacketRecordType, (short) 0x0102, 0L,
						sensorDataPacketBuffer.getBytes().length,
						sensorDataPacketBuffer.getBytes(), 0, null));

		// Now for the SummaryPacket
		assertTrue(
				"SummaryPacket byte structures should be identical",
				testSIAMByteArray(
						PacketUtility
								.extractByteArrayFromBytesMessage(summaryPacketBytesMessage),
						(short) 0x0100, 0L, deviceID, summaryPacketDate
								.getTime(), summaryPacketSequenceNumber,
						metadataPacketSequenceNumber, parentID,
						summaryPacketRecordType, (short) 0x0102, 0L,
						summaryPacketData.getBytes().length, summaryPacketData
								.getBytes(), 0, null));
	}

	public void testSIAMToSSDSConversion() {
		// So I should now be able to test the various conversions of byte
		// arrays

		// TODO kgomes the time calculations are not correct here and will have
		// to be adjusted when bug SSDS-77 if fixed

		// MetadataPacket
		assertTrue(
				"MetadataPacket byte array should be converted properly to SSDS byte array",
				testSSDSByteArray(
						PacketUtility
								.convertSIAMByteArrayToVersion3SSDSByteArray(
										PacketUtility
												.extractByteArrayFromBytesMessage(metadataBytesMessage),
										true, true, true, true), deviceID,
						parentID, 1, 0L, 0L, 0L, DateUtils
								.getEpochTimestampSeconds(metadataPacketDate),
						DateUtils.getNanoseconds(metadataPacketDate),
						metadataPacketSequenceNumber,
						metadataPacketBytesMessage.getBytes(),
						metadataPacketCauseMessage.getBytes()));

		// DeviceMessagePacket
		assertTrue(
				"DeviceMessagePacket byte array should be converted properly to SSDS byte array",
				testSSDSByteArray(
						PacketUtility
								.convertSIAMByteArrayToVersion3SSDSByteArray(
										PacketUtility
												.extractByteArrayFromBytesMessage(deviceMessageBytesMessage),
										true, true, true, true),
						deviceID,
						parentID,
						4,
						deviceMessagePacketRecordType,
						metadataPacketSequenceNumber,
						metadataPacketSequenceNumber,
						DateUtils
								.getEpochTimestampSeconds(deviceMessagePacketDate),
						DateUtils.getNanoseconds(deviceMessagePacketDate),
						deviceMessagePacketSequenceNumber, deviceMessageMessage
								.getBytes(), null));

		// MeasurementPacket
		ArgArray targetMeasurementPacketArgArray = new ArgArray();
		targetMeasurementPacketArgArray.put("TestKey1", "TestKeyValue1");
		targetMeasurementPacketArgArray.put("TestKey2", "TestValue2");
		assertTrue(
				"MeasurmentPacket byte array should be converted properly to SSDS byte array",
				testSSDSByteArray(
						PacketUtility
								.convertSIAMByteArrayToVersion3SSDSByteArray(
										PacketUtility
												.extractByteArrayFromBytesMessage(measurementBytesMessage),
										true, true, true, true),
						deviceID,
						parentID,
						4,
						measurementPacketRecordType,
						metadataPacketSequenceNumber,
						metadataPacketSequenceNumber,
						DateUtils
								.getEpochTimestampSeconds(measurementPacketDate),
						DateUtils.getNanoseconds(measurementPacketDate),
						measurementPacketSequenceNumber,
						targetMeasurementPacketArgArray.toString().getBytes(),
						null));

		// SensorDataPacket
		assertTrue(
				"SensorDataPacket byte array should be converted properly to SSDS byte array",
				testSSDSByteArray(
						PacketUtility
								.convertSIAMByteArrayToVersion3SSDSByteArray(
										PacketUtility
												.extractByteArrayFromBytesMessage(sensorDataBytesMessage),
										true, true, true, true),
						deviceID,
						parentID,
						0,
						sensorDataPacketRecordType,
						metadataPacketSequenceNumber,
						metadataPacketSequenceNumber,
						DateUtils
								.getEpochTimestampSeconds(sensorDataPacketDate),
						DateUtils.getNanoseconds(sensorDataPacketDate),
						sensorDataPacketSequenceNumber, sensorDataPacketBuffer
								.getBytes(), null));

		// SummaryPacket
		assertTrue(
				"SummaryPacket byte array should be converted properly to SSDS byte array",
				testSSDSByteArray(
						PacketUtility
								.convertSIAMByteArrayToVersion3SSDSByteArray(
										PacketUtility
												.extractByteArrayFromBytesMessage(summaryPacketBytesMessage),
										true, true, true, true), deviceID,
						parentID, 0, summaryPacketRecordType,
						metadataPacketSequenceNumber,
						metadataPacketSequenceNumber, DateUtils
								.getEpochTimestampSeconds(summaryPacketDate),
						DateUtils.getNanoseconds(summaryPacketDate),
						summaryPacketSequenceNumber, summaryPacketData
								.getBytes(), null));
	}

	public void testSIAMPacketToSSDSDevicePacketToSSDSByteArray() {
		// Take the MetadataPacket and convert to SSDSDevicePacket using a
		// constructor
		SSDSDevicePacket ssdsMetadataDevicePacket = PacketUtility
				.convertSIAMDevicePacketToSSDSDevicePacket(metadataPacket);

		// Now convert that packet to an SSDS byte array
		byte[] ssdsMetadataDevicePacketByteArray = PacketUtility
				.convertSSDSDevicePacketToVersion3SSDSByteArray(ssdsMetadataDevicePacket);

		// Now test
		assertTrue(
				"MetadataPacket byte array should be converted properly to SSDS byte array",
				testSSDSByteArray(ssdsMetadataDevicePacketByteArray, deviceID,
						parentID, 1, 0L, 0L, 0L, DateUtils
								.getEpochTimestampSeconds(metadataPacketDate),
						DateUtils.getNanoseconds(metadataPacketDate),
						metadataPacketSequenceNumber,
						metadataPacketBytesMessage.getBytes(),
						metadataPacketCauseMessage.getBytes()));

		// DeviceMessagePacket
		SSDSDevicePacket ssdsDeviceMessageDevicePacket = PacketUtility
				.convertSIAMDevicePacketToSSDSDevicePacket(deviceMessagePacket);

		// Now convert that packet to an SSDS byte array
		byte[] ssdsDeviceMessageDevicePacketByteArray = PacketUtility
				.convertSSDSDevicePacketToVersion3SSDSByteArray(ssdsDeviceMessageDevicePacket);
		assertTrue(
				"DeviceMessagePacket byte array should be converted properly to SSDS byte array",
				testSSDSByteArray(
						ssdsDeviceMessageDevicePacketByteArray,
						deviceID,
						parentID,
						4,
						deviceMessagePacketRecordType,
						metadataPacketSequenceNumber,
						metadataPacketSequenceNumber,
						DateUtils
								.getEpochTimestampSeconds(deviceMessagePacketDate),
						DateUtils.getNanoseconds(deviceMessagePacketDate),
						deviceMessagePacketSequenceNumber, deviceMessageMessage
								.getBytes(), null));

		// MeasurementPacket
		SSDSDevicePacket ssdsMeasurmentDevicePacket = PacketUtility
				.convertSIAMDevicePacketToSSDSDevicePacket(measurementPacket);

		// Now convert that packet to an SSDS byte array
		byte[] ssdsMeasurmentDevicePacketByteArray = PacketUtility
				.convertSSDSDevicePacketToVersion3SSDSByteArray(ssdsMeasurmentDevicePacket);
		ArgArray targetMeasurementPacketArgArray = new ArgArray();
		targetMeasurementPacketArgArray.put("TestKey1", "TestKeyValue1");
		targetMeasurementPacketArgArray.put("TestKey2", "TestValue2");
		assertTrue(
				"MeasurmentPacket byte array should be converted properly to SSDS byte array",
				testSSDSByteArray(
						ssdsMeasurmentDevicePacketByteArray,
						deviceID,
						parentID,
						4,
						measurementPacketRecordType,
						metadataPacketSequenceNumber,
						metadataPacketSequenceNumber,
						DateUtils
								.getEpochTimestampSeconds(measurementPacketDate),
						DateUtils.getNanoseconds(measurementPacketDate),
						measurementPacketSequenceNumber,
						targetMeasurementPacketArgArray.toString().getBytes(),
						null));

		// SensorDataPacket
		SSDSDevicePacket ssdsSensorDataDevicePacket = PacketUtility
				.convertSIAMDevicePacketToSSDSDevicePacket(sensorDataPacket);

		// Now convert that packet to an SSDS byte array
		byte[] ssdsSensorDataDevicePacketByteArray = PacketUtility
				.convertSSDSDevicePacketToVersion3SSDSByteArray(ssdsSensorDataDevicePacket);
		assertTrue(
				"SensorDataPacket byte array should be converted properly to SSDS byte array",
				testSSDSByteArray(
						ssdsSensorDataDevicePacketByteArray,
						deviceID,
						parentID,
						0,
						sensorDataPacketRecordType,
						metadataPacketSequenceNumber,
						metadataPacketSequenceNumber,
						DateUtils
								.getEpochTimestampSeconds(sensorDataPacketDate),
						DateUtils.getNanoseconds(sensorDataPacketDate),
						sensorDataPacketSequenceNumber, sensorDataPacketBuffer
								.getBytes(), null));

		// SummaryPacket
		SSDSDevicePacket ssdsSummaryDevicePacket = PacketUtility
				.convertSIAMDevicePacketToSSDSDevicePacket(summaryPacket);

		// Now convert that packet to an SSDS byte array
		byte[] ssdsSummaryDevicePacketByteArray = PacketUtility
				.convertSSDSDevicePacketToVersion3SSDSByteArray(ssdsSummaryDevicePacket);
		assertTrue(
				"SummaryPacket byte array should be converted properly to SSDS byte array",
				testSSDSByteArray(ssdsSummaryDevicePacketByteArray, deviceID,
						parentID, 0, summaryPacketRecordType,
						metadataPacketSequenceNumber,
						metadataPacketSequenceNumber, DateUtils
								.getEpochTimestampSeconds(summaryPacketDate),
						DateUtils.getNanoseconds(summaryPacketDate),
						summaryPacketSequenceNumber, summaryPacketData
								.getBytes(), null));
	}

	public void testReadVariablesFromVersion3SSDSByteArray() {
		// Construct a SSDS byte array
		long sourceID = 101;
		long parentID = 100;
		// The type of packet Data = 0, Metadata = 1, Device Message = 4
		int packetType = 0;
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

		// Create the byte array
		byte[] ssdsByteArray = PacketUtility.createVersion3SSDSByteArray(
				sourceID, parentID, packetType, packetSubType,
				metadataSequenceNumber, dataDescriptionVersion,
				timestampSeconds, timestampNanoseconds, sequenceNumber,
				firstBuffer, secondBuffer);

		// Call the extraction method
		Object[] extractedArray = PacketUtility
				.readVariablesFromVersion3SSDSByteArray(ssdsByteArray);

		// Now test
		assertEquals("SourceIDs should be the same", sourceID,
				((Long) extractedArray[0]).longValue());
		assertEquals("ParentIDs should be the same", parentID,
				((Long) extractedArray[1]).longValue());
		assertEquals("PacketTypes should be the same", packetType,
				((Integer) extractedArray[2]).intValue());
		assertEquals("PacketSubTypes should be the same", packetSubType,
				((Long) extractedArray[3]).longValue());
		assertEquals("MetadataSequenceNumbers should be the same",
				metadataSequenceNumber, ((Long) extractedArray[4]).longValue());
		assertEquals("DataDescriptionVersion should be the same",
				dataDescriptionVersion, ((Long) extractedArray[5]).longValue());
		assertEquals("TimestampSeconds should be the same", timestampSeconds,
				((Long) extractedArray[6]).longValue());
		assertEquals("TimestampNanoseconds should be the same",
				timestampNanoseconds, ((Long) extractedArray[7]).longValue());
		assertEquals("SequenceNumbers should be the same", sequenceNumber,
				((Long) extractedArray[8]).longValue());
		assertTrue("First buffers should be the same", Arrays.equals(
				firstBuffer, ((byte[]) extractedArray[10])));
		assertTrue("Second buffers should be the same", Arrays.equals(
				secondBuffer, ((byte[]) extractedArray[12])));

		// Let's try nulling out some things
		firstBuffer = null;

		// Create the byte array again
		ssdsByteArray = PacketUtility.createVersion3SSDSByteArray(sourceID,
				parentID, packetType, packetSubType, metadataSequenceNumber,
				dataDescriptionVersion, timestampSeconds, timestampNanoseconds,
				sequenceNumber, firstBuffer, secondBuffer);

		// Call the extraction method
		extractedArray = PacketUtility
				.readVariablesFromVersion3SSDSByteArray(ssdsByteArray);

		// Now test
		assertEquals("SourceIDs should be the same", sourceID,
				((Long) extractedArray[0]).longValue());
		assertEquals("ParentIDs should be the same", parentID,
				((Long) extractedArray[1]).longValue());
		assertEquals("PacketTypes should be the same", packetType,
				((Integer) extractedArray[2]).intValue());
		assertEquals("PacketSubTypes should be the same", packetSubType,
				((Long) extractedArray[3]).longValue());
		assertEquals("MetadataSequenceNumbers should be the same",
				metadataSequenceNumber, ((Long) extractedArray[4]).longValue());
		assertEquals("DataDescriptionVersion should be the same",
				dataDescriptionVersion, ((Long) extractedArray[5]).longValue());
		assertEquals("TimestampSeconds should be the same", timestampSeconds,
				((Long) extractedArray[6]).longValue());
		assertEquals("TimestampNanoseconds should be the same",
				timestampNanoseconds, ((Long) extractedArray[7]).longValue());
		assertEquals("SequenceNumbers should be the same", sequenceNumber,
				((Long) extractedArray[8]).longValue());
		assertEquals("First buffer should be of 0 length", 0,
				((Integer) extractedArray[9]).intValue());
		assertTrue("First buffer should be an empty buffer", Arrays.equals(
				new byte[0], ((byte[]) extractedArray[10])));
		assertTrue("Second buffers should be the same", Arrays.equals(
				secondBuffer, ((byte[]) extractedArray[12])));
	}

	public void testConvertProtocolBufferMessageToSSDSByteArray() {
		// Construct the variables to use to create the protocol buffer message
		long sourceID = 101;
		long parentID = 100;
		// The type of packet Data = 0, Metadata = 1, Device Message = 4
		int packetType = 9;
		// This is the record type
		long packetSubType = 1;
		long metadataSequenceNumber = 10;
		long dataDescriptionVersion = 11;
		Date now = new Date();
		long timestampSeconds = DateUtils.getEpochTimestampSeconds(now);
		long timestampNanoseconds = DateUtils.getNanoseconds(now);
		long sequenceNumber = 199;
		byte[] firstBuffer = "Sensor Data First Buffer".getBytes();
		byte[] secondBuffer = "SensorData Second Buffer (ignored)".getBytes();

		// Create the ProtocolBuffersMessage
		SSDSDevicePacketProto.MessagePacket message = SSDSDevicePacketProto.MessagePacket
				.newBuilder().setSourceID(sourceID).setParentID(parentID)
				.setPacketType(packetType).setPacketSubType(packetSubType)
				.setMetadataSequenceNumber(metadataSequenceNumber)
				.setDataDescriptionVersion(dataDescriptionVersion)
				.setTimestampSeconds(timestampSeconds).setTimestampNanoseconds(
						timestampNanoseconds).setSequenceNumber(sequenceNumber)
				.setBufferBytes(ByteString.copyFrom(firstBuffer))
				.setBufferTwoBytes(ByteString.copyFrom(secondBuffer)).build();

		// Serialize it
		byte[] protoSerialArray = message.toByteArray();

		// Now convert that to a SSDSByteArray
		byte[] ssdsByteArray = PacketUtility
				.convertProtocolBuffersByteArrayToSSDSByteArray(protoSerialArray);

		// Run the test
		assertTrue(
				"SummaryPacket byte array should be converted properly to SSDS byte array",
				testSSDSByteArray(ssdsByteArray, sourceID, parentID,
						packetType, packetSubType, metadataSequenceNumber,
						dataDescriptionVersion, timestampSeconds,
						timestampNanoseconds, sequenceNumber, firstBuffer,
						secondBuffer));
	}

	/**
	 * This is a convenience method that takes in a byte array and compares it
	 * to the incoming parameters. It returns true if they all match and false
	 * if not.
	 * 
	 * @param exportedBytes
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
	 * @param firstBufferLength
	 * @param firstBufferBytes
	 * @param secondBufferLength
	 * @param secondBufferBytes
	 * @return
	 */
	public static boolean testSIAMByteArray(byte[] exportedBytes,
			short streamID, long devicePacketVersion, long sourceID,
			long timestamp, long sequenceNumber, long metadataRef,
			long parentID, long recordType, short secondStreamID,
			long secondPacketVersion, int firstBufferLength,
			byte[] firstBufferBytes, int secondBufferLength,
			byte[] secondBufferBytes) {
		logger.debug("testSIAMByteArray called on byte array:");
		PacketUtility.logSIAMMessageByteArray(exportedBytes, true);
		// The boolean to return
		boolean allEquals = true;

		// Now, if I extract the various parts of the message expecting to get a
		// SIAM export format, everything should match. First, create a
		// ByteArryInputStream and then a DataInputStream
		ByteArrayInputStream bis = new ByteArrayInputStream(exportedBytes);
		DataInputStream dis = new DataInputStream(bis);

		try {
			// Read in all the bytes from the byte array
			short actualStreamID = dis.readShort();
			long actualDevicePacketVerion = dis.readLong();
			long actualSourceID = dis.readLong();
			long actualTimestamp = dis.readLong();
			long actualSequenceNumber = dis.readLong();
			long actualMetadataRef = dis.readLong();
			long actualParentID = dis.readLong();
			long actualRecordType = dis.readLong();
			short actualSecondStreamID = dis.readShort();
			long actualSecondPacketVersion = dis.readLong();
			int actualFirstBufferLength = 0;
			try {
				actualFirstBufferLength = dis.readInt();
			} catch (Exception e) {
			}
			byte[] actualFirstBufferBytes = null;
			int actualSecondBufferLength = 0;
			byte[] actualSecondBufferBytes = null;
			if (actualFirstBufferLength > 0) {
				actualFirstBufferBytes = new byte[actualFirstBufferLength];
				dis.read(actualFirstBufferBytes);
				try {
					actualSecondBufferLength = dis.readInt();
				} catch (Exception e) {
				}
				if (actualSecondBufferLength > 0) {
					actualSecondBufferBytes = new byte[actualSecondBufferLength];
					dis.read(actualSecondBufferBytes);
				}
			}

			// Test them all
			if (actualStreamID != streamID) {
				allEquals = false;
				logger.debug("StreamIDs did NOT match! Expected " + streamID
						+ " but found " + actualStreamID);
			}
			if (actualDevicePacketVerion != devicePacketVersion) {
				allEquals = false;
				logger.debug("DevicePacketVersions did NOT match! Expected "
						+ devicePacketVersion + " but found "
						+ actualDevicePacketVerion);
			}
			if (actualSourceID != sourceID) {
				allEquals = false;
				logger.debug("SourceIDs did NOT match! Expected " + sourceID
						+ " but found " + actualSourceID);
			}
			if (actualTimestamp != timestamp) {
				allEquals = false;
				logger.debug("Timestamps did NOT match! Expected " + timestamp
						+ " but found " + actualTimestamp);
			}
			if (actualSequenceNumber != sequenceNumber) {
				allEquals = false;
				logger
						.debug("SequenceNumbers did NOT match! Expected "
								+ sequenceNumber + " but found "
								+ actualSequenceNumber);
			}
			if (actualMetadataRef != metadataRef) {
				allEquals = false;
				logger.debug("MetadataRefs did NOT match! Expected "
						+ metadataRef + " but found " + actualMetadataRef);
			}
			if (actualParentID != parentID) {
				allEquals = false;
				logger.debug("ParentIDs did NOT match! Expected " + parentID
						+ " but found " + actualParentID);
			}
			if (actualRecordType != recordType) {
				allEquals = false;
				logger.debug("RecordTypes did NOT match! Expected "
						+ recordType + " but found " + actualRecordType);
			}
			if (actualSecondStreamID != secondStreamID) {
				allEquals = false;
				logger
						.debug("SecondStreamIDs did NOT match! Expected "
								+ secondStreamID + " but found "
								+ actualSecondStreamID);
			}
			if (actualSecondPacketVersion != secondPacketVersion) {
				allEquals = false;
				logger.debug("SecondPacketVersions did NOT match! Expected "
						+ secondPacketVersion + " but found "
						+ actualSecondPacketVersion);
			}
			if (actualFirstBufferLength != firstBufferLength) {
				allEquals = false;
				logger.debug("FirstBufferLengths did NOT match! Expected "
						+ firstBufferLength + " but found "
						+ actualFirstBufferLength);
			}
			if (firstBufferLength > 0) {
				// Compare buffers
				if (actualFirstBufferBytes == null
						|| !Arrays.equals(actualFirstBufferBytes,
								firstBufferBytes)) {
					allEquals = false;
					logger.debug("FirstBufferBytes did NOT match! Expected "
							+ new String(firstBufferBytes) + " but found "
							+ new String(actualFirstBufferBytes));
				}
				// Compare second buffers
				if (actualSecondBufferLength != secondBufferLength) {
					allEquals = false;
					logger.debug("SecondBufferLengths did NOT match! Expected "
							+ secondBufferLength + " but found "
							+ actualSecondBufferLength);
				}
				if (secondBufferLength > 0) {
					// Compare buffers
					if (actualSecondBufferBytes == null
							|| !Arrays.equals(actualSecondBufferBytes,
									secondBufferBytes)) {
						allEquals = false;
						logger
								.debug("SecondBufferBytes did NOT match! Expected "
										+ new String(secondBufferBytes)
										+ " but found "
										+ new String(actualSecondBufferBytes));
					}
				}
			}
		} catch (IOException e) {
			allEquals = false;
		}
		return allEquals;
	}

	public static boolean testSSDSByteArray(byte[] ssdsByteArray,
			long sourceID, long parentID, int packetType, long packetSubType,
			long metadataSequenceNumber, long dataDescriptionVersion,
			long timestampSeconds, long timestampNanoSeconds,
			long sequenceNumber, byte[] firstBuffer, byte[] secondBuffer) {
		// The result to return
		boolean allEquals = true;

		// Create convenience input streams to read from the byte array
		ByteArrayInputStream bis = new ByteArrayInputStream(ssdsByteArray);
		DataInputStream dis = new DataInputStream(bis);

		// Now read in all the information from the SSDS byte array
		try {
			// Read in the sourceID
			long actualSourceID = dis.readLong();

			// Read in the parentID
			long actualParentID = dis.readLong();

			// Read in the packet type
			int actualPacketType = dis.readInt();

			// Read in the packet sub type
			long actualPacketSubType = dis.readLong();

			// Read in the metadata sequence number
			long actualMetadataSequenceNumber = dis.readLong();

			// Read in the data description version
			long actualDataDescriptionVersion = dis.readLong();

			// Read in the in timestamp in Seconds
			long actualTimestampSeconds = dis.readLong();

			// Nano seconds
			long actualTimestampNanoseconds = dis.readLong();

			// Read in the sequence number
			long actualSequenceNumber = dis.readLong();

			// Read in the first data buffer length
			int actualFirstBufferLength = 0;
			byte[] actualFirstBufferBytes = null;
			try {
				actualFirstBufferLength = dis.readInt();
			} catch (Exception e) {
				logger.error("An Exception was caught trying to "
						+ "extract the first buffer length by "
						+ "reading in int from the SSDS byte array: "
						+ e.getMessage());
			}

			// If it looks like there is some buffer to read in, go ahead
			// and do sos
			if (actualFirstBufferLength > 0) {
				// Create the array to hold the first buffer
				actualFirstBufferBytes = new byte[actualFirstBufferLength];
				// Now read in the bytes
				dis.read(actualFirstBufferBytes);
			}
			// Now try to read in a length of second buffer
			int actualSecondBufferLength = 0;
			byte[] actualSecondBufferBytes = null;
			try {
				actualSecondBufferLength = dis.readInt();
			} catch (Exception e) {
				logger.error("An Exception was caught trying to "
						+ "extract the second buffer length by reading in "
						+ "int from the ssds byte array: " + e.getMessage());
			}

			// If it looks like there is a second buffer, try to read it
			if (actualSecondBufferLength > 0) {
				// Create the byte array for the second buffer
				actualSecondBufferBytes = new byte[actualSecondBufferLength];
				// Read them in
				dis.read(actualSecondBufferBytes);
			}

			// Now test everything
			if (actualSourceID != sourceID) {
				allEquals = false;
				logger.error("SourceIDs did NOT match! Expecting " + sourceID
						+ " but found " + actualSourceID);
			}
			if (actualParentID != parentID) {
				allEquals = false;
				logger.error("ParentIDs did NOT match! Expecting " + parentID
						+ " but found " + actualParentID);
			}
			if (actualPacketType != packetType) {
				allEquals = false;
				logger.error("PacketTypes did NOT match! Expecting "
						+ packetType + " but found " + actualPacketType);
			}
			if (actualPacketSubType != packetSubType) {
				allEquals = false;
				logger.error("PacketSubTypes did NOT match! Expecting "
						+ packetSubType + " but found " + actualPacketSubType);
			}
			if (actualMetadataSequenceNumber != metadataSequenceNumber) {
				allEquals = false;
				logger.error("MetadataSequenceNumbers did NOT match! "
						+ "Expecting " + metadataSequenceNumber + " but found "
						+ actualMetadataSequenceNumber);
			}
			if (actualDataDescriptionVersion != dataDescriptionVersion) {
				allEquals = false;
				logger.error("DataDescriptionVersions did NOT match! "
						+ "Expecting " + dataDescriptionVersion + " but found "
						+ actualDataDescriptionVersion);
			}
			if (actualTimestampSeconds != timestampSeconds) {
				allEquals = false;
				logger.error("TimestampSeconds did NOT match! Expecting "
						+ timestampSeconds + " but found "
						+ actualTimestampSeconds);
			}
			if (actualTimestampNanoseconds != timestampNanoSeconds) {
				allEquals = false;
				logger.error("TimestampNanoseconds did NOT match! Expecting "
						+ timestampNanoSeconds + " but found "
						+ actualTimestampNanoseconds);
			}
			if (actualSequenceNumber != sequenceNumber) {
				allEquals = false;
				logger
						.error("SequenceNumbers did NOT match! Expecting "
								+ sequenceNumber + " but found "
								+ actualSequenceNumber);
			}
			if (firstBuffer != null) {
				if (actualFirstBufferLength != firstBuffer.length) {
					allEquals = false;
					logger.error("FirstBufferLengths did NOT match! Expecting "
							+ firstBuffer.length + " but found "
							+ actualFirstBufferLength);
				}
				if (actualFirstBufferLength > 0
						&& !Arrays.equals(actualFirstBufferBytes, firstBuffer)) {
					allEquals = false;
					logger.error("FirstBuffers did NOT match! Expecting "
							+ new String(firstBuffer) + " but found "
							+ new String(actualFirstBufferBytes));
				}
			}
			if (secondBuffer != null) {
				if (actualSecondBufferLength != secondBuffer.length) {
					allEquals = false;
					logger
							.error("SecondBufferLenghts did NOT match! Expecting "
									+ secondBuffer.length
									+ " but found "
									+ actualSecondBufferLength);
				}
				if (actualSecondBufferLength > 0
						&& !Arrays
								.equals(actualSecondBufferBytes, secondBuffer)) {
					allEquals = false;
					logger.error("SecondBuffers did NOT match! Expecting "
							+ new String(secondBuffer) + " but found "
							+ new String(actualSecondBufferBytes));
				}
			}
		} catch (IOException e) {
			logger.error("An IOException was trapped during the extraction: "
					+ e.getMessage());
		} catch (Exception e) {
			logger.error("An Exception was trapped during the extraction: "
					+ e.getMessage());
		}

		// Return the result
		return allEquals;
	}
}
