package test.moos.ssds.transmogrify;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.jms.BytesMessage;

import junit.framework.TestCase;
import moos.ssds.io.util.PacketUtility;
import moos.ssds.jms.PublisherComponent;
import moos.ssds.jms.SubscriberComponent;

import org.apache.log4j.Logger;
import org.mbari.siam.distributed.MetadataPacket;
import org.mbari.siam.operations.utils.ExportablePacket;

import test.moos.ssds.io.util.TestPacketUtility;

public class TransmogrifyMDBTest extends TestCase {

	/**
	 * A log4j logger
	 */
	private static Logger logger = Logger.getLogger(TransmogrifyMDBTest.class);

	/**
	 * This is the class to help with publishing
	 */
	private static PublisherComponent publisherComponent;

	/**
	 * This is the class to help with subscribing
	 */
	private static SubscriberComponent subscriberComponent;

	/**
	 * The listener to be used for handling the message
	 */
	private TransmogrifyMDBTestMessageListener transmogrifyMDBTestMessageListener = new TransmogrifyMDBTestMessageListener();

	public TransmogrifyMDBTest(String name) {
		super(name);

		// Grab the transmogrifier properties for the file
		Properties transmogProps = new Properties();
		logger.debug("Constructor called ... going to "
				+ "try and read the transmogrifier properties file in ...");
		try {
			transmogProps.load(this.getClass().getResourceAsStream(
					"/moos/ssds/transmogrify/transmogrify.properties"));
		} catch (Exception e) {
			logger.error("Exception trying to read in properties file: "
					+ e.getMessage());
		}

		// Grab the topic name to republish to
		String republishTopicName = transmogProps
				.getProperty("transmogrify.republish.topic");
		logger.debug("Will listen for messages on topic " + republishTopicName);

		// Create the subscriber
		if (subscriberComponent == null)
			subscriberComponent = new SubscriberComponent(republishTopicName,
					transmogrifyMDBTestMessageListener);

		// Create the publisher component to connect up to the topic that the
		// transmogrify MDB is listening to
		if (publisherComponent == null)
			publisherComponent = new PublisherComponent();

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
	@SuppressWarnings("deprecation")
	public void testSendMessage() {
		// Create the output stream to help export the SIAM packet
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		// Create the ExportablePacket to use to wrap and export a SIAM packet
		ExportablePacket exportablePacket = new ExportablePacket();

		// MetadataPacket comes first
		MetadataPacket metadataPacket = new MetadataPacket(101, ("Cause")
				.getBytes(), ("Buffer bytes").getBytes());
		metadataPacket.setMetadataRef(0);
		metadataPacket.setParentId(100);
		metadataPacket.setRecordType(0);
		metadataPacket.setSequenceNo(1);
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

		// Now publish the SIAM formatted byte array to the TransmogrifyMDB
		// input topic
		publisherComponent.publishBytes(bos.toByteArray());

		// Wait for a bit or until the listener gets a message
		Date dateToStopWaiting = new Date(new Date().getTime() + 5000);
		while (new Date().before(dateToStopWaiting)
				&& transmogrifyMDBTestMessageListener.getCurrentBytesMessage() == null) {

		}
		// Check for the message
		if (transmogrifyMDBTestMessageListener.getCurrentBytesMessage() != null) {
			// Grab the bytes message which should be the Metadata packet but in
			// SSDS form
			BytesMessage receivedBytesMessage = transmogrifyMDBTestMessageListener
					.getCurrentBytesMessage();

			// Test that it is what we expect
			assertTrue(
					"The received bytes message should be in SSDS format",
					TestPacketUtility
							.testSSDSByteArray(
									PacketUtility
											.extractByteArrayFromBytesMessage(receivedBytesMessage),
									101,
									100,
									1,
									0,
									0,
									0,
									metadataPacketDate.getTime() / 1000,
									(metadataPacketDate.getTime() % 1000) * 1000,
									1, ("Buffer bytes").getBytes(), "Cause"
											.getBytes()));
			// Now clear it
			transmogrifyMDBTestMessageListener.clearCurrentBytesMessage();
		} else {
			assertTrue("The message was never received by the subscriber",
					false);
		}

		// Now let's try sending the object itself (yes, I know this is
		// deprecated)
		publisherComponent.publish(metadataPacket);

		// Wait for a bit or until the listener gets a message
		dateToStopWaiting = new Date(new Date().getTime() + 5000);
		while (new Date().before(dateToStopWaiting)
				&& transmogrifyMDBTestMessageListener.getCurrentBytesMessage() == null) {
		}
		// Check for the message
		if (transmogrifyMDBTestMessageListener.getCurrentBytesMessage() != null) {
			// Grab the bytes message which should be the Metadata packet but in
			// SSDS form
			BytesMessage receivedBytesMessage = transmogrifyMDBTestMessageListener
					.getCurrentBytesMessage();

			// Test that it is what we expect
			assertTrue(
					"The received bytes message should be in SSDS format",
					TestPacketUtility
							.testSSDSByteArray(
									PacketUtility
											.extractByteArrayFromBytesMessage(receivedBytesMessage),
									101,
									100,
									1,
									0,
									0,
									0,
									metadataPacketDate.getTime() / 1000,
									(metadataPacketDate.getTime() % 1000) * 1000,
									1, ("Buffer bytes").getBytes(), "Cause"
											.getBytes()));
			// Now clear it
			transmogrifyMDBTestMessageListener.clearCurrentBytesMessage();
		} else {
			assertTrue("After object send, the byte array was not received",
					false);
		}

	}
}
