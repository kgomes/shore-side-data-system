package test.moos.ssds.ingest;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.TestCase;
import moos.ssds.io.SSDSDevicePacket;
import moos.ssds.io.util.PacketUtility;
import moos.ssds.jms.PublisherComponent;
import moos.ssds.services.data.SSDSByteArrayAccess;
import moos.ssds.util.DateUtils;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.mbari.siam.distributed.MetadataPacket;
import org.mbari.siam.distributed.SensorDataPacket;
import org.mbari.siam.operations.utils.ExportablePacket;

public class IngestMDBTest extends TestCase {

	/**
	 * A log4j logger
	 */
	private static Logger logger = Logger.getLogger(IngestMDBTest.class);

	private int numberOfMessagesToSend = 1234;

	public IngestMDBTest(String name) {
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

		// Grab the transmogrifier properties for the file
		Properties ingestProperties = new Properties();
		logger.debug("Constructor called ... going to "
				+ "try and read the transmogrifier properties file in ...");
		try {
			ingestProperties.load(this.getClass().getResourceAsStream(
					"/moos/ssds/ingest/ingest.properties"));
		} catch (Exception e) {
			logger.error("Exception trying to read in properties file: "
					+ e.getMessage()
					+ "\nThis could be due to the fact that you have "
					+ "not run 'ant -Dtarget=build' from the root of "
					+ "the project directory.  This needs to be done "
					+ "before these tests are run.");
		}
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * This method tests whether or not a message can be sent to the
	 * transmogrifyMDB input topic and then the transmogrify message was
	 * received on the IngestMDB topic. It also checks if the transmogrification
	 * went OK.
	 */
	public void testSendMessage() {

		// Create the output stream to help export the SIAM packet
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		// Create the ExportablePacket to use to wrap and export a SIAM packet
		ExportablePacket exportablePacket = new ExportablePacket();

		// MetadataPacket comes first
		MetadataPacket metadataPacket = new MetadataPacket(101, ("Cause")
				.getBytes(), ("Buffer bytes").getBytes());
		metadataPacket.setMetadataRef(1);
		metadataPacket.setParentId(100);
		metadataPacket.setRecordType(0);
		metadataPacket.setSequenceNo(9);
		Date metadataPacketDate = new Date();
		metadataPacket.setSystemTime(metadataPacketDate.getTime());

		// Wrap and export
		exportablePacket.wrapPacket(metadataPacket);
		try {
			exportablePacket.export(dos);
		} catch (IOException e) {
			assertTrue("IOException caught trying to export "
					+ "MetadataPacket to SIAM formatted byte array.", false);
		}

		// Convert those bytes to SSDS formatted bytes
		byte[] ssdsFormattedBytes = PacketUtility
				.convertSIAMByteArrayToVersion3SSDSByteArray(bos.toByteArray(),
						false, false, false, false);

		// Now publish the SSDS formatted byte array to the IngestMDB topic
		PublisherComponent publisherComponent = new PublisherComponent();
		publisherComponent.publishBytes(ssdsFormattedBytes);
		publisherComponent.close();

		// Wait for a bit or until the listener gets a message
		Date dateToStopWaiting = new Date(new Date().getTime() + 500);
		while (new Date().before(dateToStopWaiting)) {
		}

		// Now let's construct the SSDSByteArrayEJB interface
		try {
			// Grab a naming context
			Context context = new InitialContext();

			// Look up the remote bean
			SSDSByteArrayAccess ssdsByteArrayAccess = (SSDSByteArrayAccess) context
					.lookup("moos/ssds/services/data/SSDSByteArrayAccess");

			// Set the device ID
			ssdsByteArrayAccess.setDeviceID(101);

			// Now try to search for the latest byte array
			ssdsByteArrayAccess.setLastNumberOfPackets(1);

			// Run the query
			try {
				ssdsByteArrayAccess.queryForData();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Now grab the byte array
			if (ssdsByteArrayAccess.hasMoreElements()) {
				byte[] returnedByteArray = ssdsByteArrayAccess.nextElement();

				// Convert it to SSDS format
				byte[] ssdsFormat = null;
				if (returnedByteArray != null)
					ssdsFormat = PacketUtility
							.stripOffVersionAndAddDeviceIDInFront(
									returnedByteArray, 101);

				// Now convert it to SSDSDevice Packet
				SSDSDevicePacket ssdsDevicePacket = PacketUtility
						.convertVersion3SSDSByteArrayToSSDSDevicePacket(
								ssdsFormat, true);

				// Now check some things
				// SourceID (device ID)
				assertEquals("SourceID should be 101", 101, ssdsDevicePacket
						.sourceID());
				// SystemTime
				assertEquals("System time should be equal", metadataPacketDate
						.getTime(), ssdsDevicePacket.systemTime());
				// Sequence Number
				assertEquals("The sequence Number should be 9", 9,
						ssdsDevicePacket.sequenceNo());
				// MetadataRef
				assertEquals("MetadataRef should be 1", 1, ssdsDevicePacket
						.metadataRef());
				// ParentID
				assertEquals("ParentID should be 100", 100, ssdsDevicePacket
						.getParentId());
				// RecordType
				assertEquals("RecordType should be 0", 0, ssdsDevicePacket
						.getRecordType());
				// MetadataSequenceNumber
				assertEquals("MetadataSequenceNumber should be 1", 1,
						ssdsDevicePacket.getMetadataSequenceNumber());
				// DeviceDescriptionVersion
				assertEquals("DataDescriptionVersion should be 1", 1,
						ssdsDevicePacket.getDataDescriptionVersion());
				// Timestamp Seconds
				assertEquals("Timestamp seconds should be equal", DateUtils
						.getEpochTimestampSeconds(metadataPacketDate),
						ssdsDevicePacket.getTimestampSeconds());
				// Timestamp Nanoseconds
				assertEquals("Timestamp nanoseconds should be equal", DateUtils
						.getNanoseconds(metadataPacketDate), ssdsDevicePacket
						.getTimestampNanoseconds());
				// PlatformID
				assertEquals("PlatformID should be 100", 100, ssdsDevicePacket
						.getPlatformID());
				// PacketType
				assertEquals("PacketType should be 0", 0, ssdsDevicePacket
						.getPacketType());

				// Data Buffer
				assertTrue("Data Buffers should be equal", Arrays.equals(
						"Buffer bytes".getBytes(), ssdsDevicePacket
								.getDataBuffer()));
				// Other Buffer
				assertTrue("Other buffers should be equal", Arrays.equals(
						"Cause".getBytes(), ssdsDevicePacket.getOtherBuffer()));
			}

			ssdsByteArrayAccess.remove();
		} catch (NamingException e) {
			assertTrue("NamingException caught during test:" + e.getMessage(),
					false);
		}

	}

	public void testSendMultipleMessagesAndCorrectReadback() {
		// Create the output stream to help export the SIAM packet
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		// Create the ExportablePacket to use to wrap and export a SIAM packet
		ExportablePacket exportablePacket = new ExportablePacket();

		// SensorDataPacket that will be used to send many messages
		SensorDataPacket sensorDataPacket = new SensorDataPacket(101, 1024);
		sensorDataPacket.setMetadataRef(1);
		sensorDataPacket.setParentId(100);
		sensorDataPacket.setRecordType(1);

		// Create the starting date to be 500 milliseconds ago
		Calendar startDateOfMessages = Calendar.getInstance();
		startDateOfMessages.add(Calendar.MILLISECOND, -1
				* numberOfMessagesToSend);
		Calendar baseTime = (Calendar) startDateOfMessages.clone();

		// Now loop over and create messages and send them as fast as I can
		PublisherComponent publisherComponent = new PublisherComponent();
		for (int i = 0; i < numberOfMessagesToSend; i++) {
			// Reset the ByteArrayOutputStream
			bos.reset();
			sensorDataPacket.setSystemTime(startDateOfMessages
					.getTimeInMillis());
			sensorDataPacket.setSequenceNo(i + 1);
			sensorDataPacket.setDataBuffer(("SDP " + i).getBytes());
			// Wrap and export
			exportablePacket.wrapPacket(sensorDataPacket);
			try {
				exportablePacket.export(dos);
			} catch (IOException e) {
				assertTrue("IOException caught trying to export "
						+ "MetadataPacket to SIAM formatted byte array.", false);
			}

			// Convert those bytes to SSDS formatted bytes
			byte[] ssdsFormattedBytes = PacketUtility
					.convertSIAMByteArrayToVersion3SSDSByteArray(bos
							.toByteArray(), false, false, false, false);

			// Now publish the SSDS formatted byte array to the IngestMDB topic
			publisherComponent.publishBytes(ssdsFormattedBytes);

			// Add a second
			startDateOfMessages.add(Calendar.MILLISECOND, 1);
		}
		publisherComponent.close();

		// Now sleep for a bit
		// Wait for a bit or until the listener gets a message
		Date dateToStopWaiting = new Date(new Date().getTime() + 5000);
		while (new Date().before(dateToStopWaiting)) {
		}

		// Now let's construct the SSDSByteArrayEJB interface
		try {
			// Grab a naming context
			Context context = new InitialContext();

			// Look up the remote bean
			SSDSByteArrayAccess ssdsByteArrayAccess = (SSDSByteArrayAccess) context
					.lookup("moos/ssds/services/data/SSDSByteArrayAccess");

			// Set the device ID
			ssdsByteArrayAccess.setDeviceID(101);

			// Now try to search for the latest number of packets that I sent
			ssdsByteArrayAccess.setLastNumberOfPackets(numberOfMessagesToSend);

			// Run the query
			try {
				ssdsByteArrayAccess.queryForData();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Now grab the byte array
			int loopCounter = 0;
			assertTrue("Result should have elements.", ssdsByteArrayAccess
					.hasMoreElements());
			while (ssdsByteArrayAccess.hasMoreElements()) {
				byte[] returnedByteArray = ssdsByteArrayAccess.nextElement();

				// Make sure it is not null
				assertNotNull("The byte array in the element "
						+ (loopCounter + 1) + " should not be null",
						returnedByteArray);

				// Convert it to SSDS format
				byte[] ssdsFormat = null;
				if (returnedByteArray != null)
					ssdsFormat = PacketUtility
							.stripOffVersionAndAddDeviceIDInFront(
									returnedByteArray, 101);

				// Now convert it to SSDSDevice Packet
				SSDSDevicePacket ssdsDevicePacket = PacketUtility
						.convertVersion3SSDSByteArrayToSSDSDevicePacket(
								ssdsFormat, true);
				logger.debug("Testing read back of sequence number "
						+ ssdsDevicePacket.sequenceNo());

				// Now check some things
				// SourceID (device ID)
				assertEquals("SourceID should be 101", 101, ssdsDevicePacket
						.sourceID());
				// SystemTime
				assertEquals("System time should be equal", baseTime
						.getTimeInMillis(), ssdsDevicePacket.systemTime());
				// Sequence Number
				assertEquals("The sequence Number should equal",
						loopCounter + 1, ssdsDevicePacket.sequenceNo());
				// MetadataRef
				assertEquals("MetadataRef should be 1", 1, ssdsDevicePacket
						.metadataRef());
				// ParentID
				assertEquals("ParentID should be 100", 100, ssdsDevicePacket
						.getParentId());
				// RecordType
				assertEquals("RecordType should be 1", 1, ssdsDevicePacket
						.getRecordType());
				// MetadataSequenceNumber
				assertEquals("MetadataSequenceNumber should be 1", 1,
						ssdsDevicePacket.getMetadataSequenceNumber());
				// DeviceDescriptionVersion
				assertEquals("DataDescriptionVersion should be 1", 1,
						ssdsDevicePacket.getDataDescriptionVersion());
				// Timestamp Seconds
				assertEquals("Timestamp seconds should be equal", DateUtils
						.getEpochTimestampSeconds(baseTime.getTime()),
						ssdsDevicePacket.getTimestampSeconds());
				// Timestamp Nanoseconds
				assertEquals("Timestamp nanoseconds should be equal", DateUtils
						.getNanoseconds(baseTime.getTime()), ssdsDevicePacket
						.getTimestampNanoseconds());
				// PlatformID
				assertEquals("PlatformID should be 100", 100, ssdsDevicePacket
						.getPlatformID());
				// PacketType
				assertEquals("PacketType should be 1", 1, ssdsDevicePacket
						.getPacketType());

				// Data Buffer
				String buffer = "SDP " + loopCounter;
				String deviceBuffer = new String(ssdsDevicePacket
						.getDataBuffer());
				assertEquals("Data Buffers should be equal", buffer,
						deviceBuffer);

				// Bump System time and loop counter
				baseTime.add(Calendar.MILLISECOND, 1);
				loopCounter++;
			}

			ssdsByteArrayAccess.remove();
			assertEquals("Loop counter should match the "
					+ "number of packets published", loopCounter,
					numberOfMessagesToSend);
		} catch (NamingException e) {
			assertTrue("NamingException caught during test:" + e.getMessage(),
					false);
		}
	}
}
