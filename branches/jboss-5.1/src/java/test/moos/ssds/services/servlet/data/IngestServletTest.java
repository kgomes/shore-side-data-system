package test.moos.ssds.services.servlet.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import javax.jms.BytesMessage;

import junit.framework.TestCase;
import moos.ssds.io.SSDSDevicePacket;
import moos.ssds.io.util.Base64;
import moos.ssds.io.util.PacketUtility;
import moos.ssds.jms.SubscriberComponent;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class IngestServletTest extends TestCase {

	/**
	 * A log4j logger
	 */
	private static Logger logger = Logger.getLogger(IngestServletTest.class);

	public IngestServletTest(String name) {
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
	 * This method tests whether or not a message can be sent to the ingest
	 * servlet and then the ingest message was received on the RuminateMDB
	 * topic. It also checks to see if all the parameters made it through
	 * unscathed
	 */
	public void testSendMessageToServlet() {
		// Grab the topic name to republish to
		String republishTopicName = "topic/SSDSRuminateTopic";
		logger.debug("Will listen for messages on topic " + republishTopicName);

		SubscriberComponent subscriberComponent = null;
		LastMessageMessageListener ingestMDBTestMessageListener = new LastMessageMessageListener();

		// Create the subscriber
		if (subscriberComponent == null)
			subscriberComponent = new SubscriberComponent(republishTopicName,
					ingestMDBTestMessageListener);
		logger.debug("SubscriberComponent created and listening.");

		// We should be able to grab the host name where the servlet lives from
		// the JNDI properties
		Properties jndiProperties = new Properties();
		try {
			jndiProperties.load(this.getClass().getResourceAsStream(
					"/jndi.properties"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Look for the property java.naming.provider.url
		String javaNamingPropertyUrl = jndiProperties
				.getProperty("java.naming.provider.url");

		// Make sure we found it
		String hostname = null;
		if (javaNamingPropertyUrl != null)
			hostname = javaNamingPropertyUrl.substring(6, javaNamingPropertyUrl
					.indexOf(':', 6));

		// Create the URL to call to send a message for device 101, with the
		// current date and time, a sequence number of 1 and with a payload of
		// "Hello Ingest!" that is base64 encoded for transmission
		// NOTE: This has to be a PacketType of 1 so that it will make it
		// through to ruminate. Other packet types are not forwarded to Ruminate
		Calendar currentTime = Calendar.getInstance();
		currentTime.setTimeZone(TimeZone.getTimeZone("GMT"));
		Base64 base64 = new Base64();
		String payloadToEncode = "Hello Ingest!";
		String payloadEncoded = base64.encode(payloadToEncode.getBytes());
		String ingestServletURLString = "http://" + hostname
				+ ":8080/ingest/Ingest?" + "response=true" + "&SourceID=101"
				+ "&ParentID=100" + "&PacketType=1" + "&PacketSubType=1"
				+ "&MetadataSequenceNumber=0" + "&DataDescriptionVersion=0"
				+ "&Timestamp=" + currentTime.get(Calendar.YEAR) + "-"
				+ (currentTime.get(Calendar.MONTH) + 1) + "-"
				+ currentTime.get(Calendar.DAY_OF_MONTH) + "T"
				+ currentTime.get(Calendar.HOUR_OF_DAY) + ":"
				+ currentTime.get(Calendar.MINUTE) + ":"
				+ currentTime.get(Calendar.SECOND) + "&SequenceNumber=1"
				+ "&FirstBuffer=" + payloadEncoded;
		logger.debug("URL that will be sent:\n" + ingestServletURLString);

		// Now send the current packet by calling the URL
		try {
			URL ingestServletUrl = new URL(ingestServletURLString);
			HttpURLConnection ingestServletUrlConnection = (HttpURLConnection) ingestServletUrl
					.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					ingestServletUrlConnection.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null)
				logger.debug(inputLine);
			in.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("Send URL and received reply");

		// Wait for a bit or until the listener gets a message
		Date dateToStopWaiting = new Date(new Date().getTime() + 5000);
		while (new Date().before(dateToStopWaiting)
				&& ingestMDBTestMessageListener.getCurrentBytesMessage() == null) {

		}
		// Check for the message
		if (ingestMDBTestMessageListener.getCurrentBytesMessage() != null) {
			logger.debug("Look like the receiver did "
					+ "get something, let's compare");
			// Grab the bytes message which should be the Data packet but in
			// SSDS form
			BytesMessage receivedBytesMessage = ingestMDBTestMessageListener
					.getCurrentBytesMessage();

			PacketUtility.logVersion3SSDSByteArray(PacketUtility
					.extractByteArrayFromBytesMessage(receivedBytesMessage),
					true);

			// Convert that form to a SSDSGeoLocatedDevicePacket for testing
			SSDSDevicePacket ssdsDevicePacket = PacketUtility
					.convertVersion3SSDSByteArrayToSSDSDevicePacket(
							PacketUtility
									.extractByteArrayFromBytesMessage(receivedBytesMessage),
							false);

			// Should not be null
			assertNotNull("SSDSDevicePacket should not be null",
					ssdsDevicePacket);

			// SourceID should be 101
			assertTrue("SourceID should be 101",
					ssdsDevicePacket.sourceID() == 101);

			// Check timestamp seconds (don't expect to find nanoseconds same as
			// they are not specified on the input
			assertTrue("Timestamp seconds match",
					ssdsDevicePacket.getTimestampSeconds() == currentTime
							.getTime().getTime() / 1000);

			// Check sequence number
			assertTrue("Sequence number should be 1", ssdsDevicePacket
					.sequenceNo() == 1);

			// PacketType should be 0
			assertTrue("PacketType should be 0", ssdsDevicePacket
					.getPacketType() == 0);

			// RecordType should be 1
			assertTrue("RecordType should be 1", ssdsDevicePacket
					.getRecordType() == 1);

			// Parent should be 100
			assertTrue("Parent should be 100",
					ssdsDevicePacket.getParentId() == 100);

			// Payload should be "Hello World!"
			assertTrue("Payload should be 'Hello Ingest!'", (new String(
					ssdsDevicePacket.getDataBuffer())).equals(payloadToEncode));

			// Now clear it
			ingestMDBTestMessageListener.clearCurrentBytesMessage();
		} else {
			assertTrue("The message was never received by the subscriber",
					false);
		}

		// Tear down the subscriber and null it out
		logger.debug("Closing subscriber");
		if (subscriberComponent != null)
			subscriberComponent.close();
		subscriberComponent = null;
		logger.debug("Subscriber closed");

	}
}
