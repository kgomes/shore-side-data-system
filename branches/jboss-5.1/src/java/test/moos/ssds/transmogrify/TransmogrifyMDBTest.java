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
import org.apache.log4j.PropertyConfigurator;
import org.mbari.siam.distributed.MetadataPacket;
import org.mbari.siam.operations.utils.ExportablePacket;

import test.moos.ssds.io.util.TestPacketUtility;

public class TransmogrifyMDBTest extends TestCase {

	/**
	 * A log4j logger
	 */
	private static Logger logger = Logger.getLogger(TransmogrifyMDBTest.class);

	public TransmogrifyMDBTest(String name) {
		super(name);

		// Load the log4j properties
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

	/**
	 * This method tests whether or not a message can be sent to the
	 * transmogrifyMDB input topic and then the transmogrify message was
	 * received on the IngestMDB topic. It also checks if the transmogrification
	 * went OK.
	 */
	@SuppressWarnings("deprecation")
	public void testSendMessage() {
		// Grab the topic name to republish to
		String republishTopicName = "topic/SSDSIngestTopic";
		logger.debug("Will listen for messages on topic " + republishTopicName);

		SubscriberComponent subscriberComponent = null;
		PublisherComponent publisherComponent = null;
		TransmogrifyMDBTestMessageListener transmogrifyMDBTestMessageListener = new TransmogrifyMDBTestMessageListener();

		// Create the subscriber
		if (subscriberComponent == null)
			subscriberComponent = new SubscriberComponent(republishTopicName,
					transmogrifyMDBTestMessageListener);
		logger.debug("SubscriberComponent created and listening.");

		// Create the publisher component to connect up to the topic that the
		// transmogrify MDB is listening to
		if (publisherComponent == null)
			publisherComponent = new PublisherComponent();
		logger.debug("PublisherComponent created");

		// Create the output stream to help export the SIAM packet
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		// Create the ExportablePacket to use to wrap and export a SIAM packet
		ExportablePacket exportablePacket = new ExportablePacket();

		// MetadataPacket comes first
		MetadataPacket metadataPacket = new MetadataPacket(101,
				("Cause").getBytes(), ("Buffer bytes").getBytes());
		metadataPacket.setMetadataRef(0);
		metadataPacket.setParentId(100);
		metadataPacket.setRecordType(0);
		metadataPacket.setSequenceNo(1);
		Date metadataPacketDate = new Date();
		metadataPacket.setSystemTime(metadataPacketDate.getTime());
		logger.debug("MetadataPacket constructed");

		// Wrap and export
		exportablePacket.wrapPacket(metadataPacket);
		try {
			exportablePacket.export(dos);
		} catch (IOException e) {
			assertTrue("IOException caught trying to export "
					+ "MetadataPacket to SIAM formatted byte array.", false);
		}
		logger.debug("Wrapped in an exportable packet (SIAM)");

		// Now publish the SIAM formatted byte array to the TransmogrifyMDB
		// input topic
		publisherComponent.publishBytes(bos.toByteArray());
		logger.debug("Published packet and will check to "
				+ "see if subscriber received it.");

		// Wait for a bit or until the listener gets a message
		Date dateToStopWaiting = new Date(new Date().getTime() + 5000);
		while (new Date().before(dateToStopWaiting)
				&& transmogrifyMDBTestMessageListener.getCurrentBytesMessage() == null) {

		}
		// Check for the message
		if (transmogrifyMDBTestMessageListener.getCurrentBytesMessage() != null) {
			logger.debug("Look like the receiver did "
					+ "get something, let's compare");
			// Grab the bytes message which should be the Metadata packet but in
			// SSDS form
			BytesMessage receivedBytesMessage = transmogrifyMDBTestMessageListener
					.getCurrentBytesMessage();

			// Test that it is what we expect
			assertTrue(
					"The received bytes message should be in SSDS format",
					TestPacketUtility.testSSDSByteArray(
							PacketUtility
									.extractByteArrayFromBytesMessage(receivedBytesMessage),
							101, 100, 1, 0, 0, 0,
							metadataPacketDate.getTime() / 1000,
							(metadataPacketDate.getTime() % 1000) * 1000, 1,
							("Buffer bytes").getBytes(), "Cause".getBytes()));
			// Now clear it
			transmogrifyMDBTestMessageListener.clearCurrentBytesMessage();
		} else {
			assertTrue("The message was never received by the subscriber",
					false);
		}

		// Now let's try sending the object itself (yes, I know this is
		// deprecated)
		publisherComponent.publish(metadataPacket);
		logger.debug("OK, going to just send the object message itself");

		// Wait for a bit or until the listener gets a message
		dateToStopWaiting = new Date(new Date().getTime() + 5000);
		while (new Date().before(dateToStopWaiting)
				&& transmogrifyMDBTestMessageListener.getCurrentBytesMessage() == null) {
		}
		// Check for the message
		if (transmogrifyMDBTestMessageListener.getCurrentBytesMessage() != null) {
			logger.debug("OK, look's like the receiver "
					+ "got the new message too");
			// Grab the bytes message which should be the Metadata packet but in
			// SSDS form
			BytesMessage receivedBytesMessage = transmogrifyMDBTestMessageListener
					.getCurrentBytesMessage();

			// Test that it is what we expect
			assertTrue(
					"The received bytes message should be in SSDS format",
					TestPacketUtility.testSSDSByteArray(
							PacketUtility
									.extractByteArrayFromBytesMessage(receivedBytesMessage),
							101, 100, 1, 0, 0, 0,
							metadataPacketDate.getTime() / 1000,
							(metadataPacketDate.getTime() % 1000) * 1000, 1,
							("Buffer bytes").getBytes(), "Cause".getBytes()));
			// Now clear it
			transmogrifyMDBTestMessageListener.clearCurrentBytesMessage();
		} else {
			assertTrue("After object send, the byte array was not received",
					false);
		}

		// Tear down the publisher too
		logger.debug("Closing publisher");
		if (publisherComponent != null)
			publisherComponent.close();
		publisherComponent = null;
		logger.debug("Publisher closed");

		// Tear down the subscriber and null it out
		logger.debug("Closing subscriber");
		if (subscriberComponent != null)
			subscriberComponent.close();
		subscriberComponent = null;
		logger.debug("Subscriber closed");

	}

}
