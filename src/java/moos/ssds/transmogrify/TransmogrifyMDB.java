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

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import moos.ssds.io.SSDSDevicePacket;
import moos.ssds.io.util.PacketUtility;

import org.apache.log4j.Logger;
import org.mbari.siam.distributed.DevicePacket;

/**
 * <p>
 * This class is a MessageDrivenBean (MDB) and is responsible for taking in the
 * SIAM DevicePackets and turning them into something that SSDS is expecting.
 * The reason is that the interface between the two systems was not completely
 * designed when coding began so this was created to serve as a bridge to take
 * incomplete packets in and convert them (by adding data from a properties
 * file) to a packet that SSDS can do something with.
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.13.2.1 $
 */
@MessageDriven(name = "TransmogrifyMDB", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "topic/SSDSTransmogTopic"),
		@ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable") })
public class TransmogrifyMDB implements MessageListener {

	/**
	 * This is just a default serial version ID for this class (java best
	 * practice)
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(TransmogrifyMDB.class);

	/**
	 * The ConnectionFactory that will be injected by the container
	 */
	@Resource(mappedName = "ConnectionFactory")
	private ConnectionFactory connectionFactory;

	/**
	 * This is the JMS destination that will be published to and will be
	 * injected by the container
	 */
	@Resource(mappedName = "topic/SSDSIngestTopic")
	private Destination destination;

	/**
	 * This is the connection to the topic that the messages will be published
	 * to
	 */
	private Connection connection = null;

	/**
	 * This is a session that the publishing of messages will be run in.
	 */
	private Session session = null;

	/**
	 * This is the message producer that is actually used to send messages to
	 * the
	 */
	private MessageProducer messageProducer = null;

	/**
	 * This method sets up the publishing so the the message driven bean can
	 * republish the message after doing its thing.
	 */
	@PostConstruct
	public void setupPublishing() {
		// Make sure the connection factory is there
		if (connectionFactory != null) {
			// Create a connection
			try {
				connection = connectionFactory.createConnection();
			} catch (JMSException e) {
				logger.error("JMSException caught trying to create "
						+ "the connection from the "
						+ "injected connection factory: " + e.getMessage());
			}
			if (connection != null) {
				// Create a session
				try {
					session = connection.createSession(false,
							Session.AUTO_ACKNOWLEDGE);
				} catch (JMSException e) {
					logger.error("JMSException caught trying to create "
							+ "the session from the connection:"
							+ e.getMessage());
				}
				if (session != null) {
					if (destination != null) {
						// Create a message producer
						try {
							messageProducer = session
									.createProducer(destination);
						} catch (JMSException e) {
							logger.error("JMSException caught trying "
									+ "to create the producer from the "
									+ "session: " + e.getMessage());
						}
						if (messageProducer == null)
							logger.error("Was not able to "
									+ "create a MessageProducer");
					} else {
						logger.error("The destination that was to be "
								+ "injected by the container "
								+ "appears to be null");
					}
				} else {
					logger.error("Could not seem to create a "
							+ "session from the connection");
				}
			} else {
				logger.error("Could not seem to create a connection "
						+ "using the injected connection factory");
			}
		} else {
			logger.error("ConnectionFactory is NULL and should "
					+ "have been injected by the container!");
		}
	}

	/**
	 * This method cleans up the connection to the next topic
	 */
	@PreDestroy
	public void tearDownPublishing() {
		// Now close the connection
		if (connection != null) {
			try {
				connection.close();
			} catch (JMSException e) {
				logger.error("JMSException caught while trying to "
						+ "close the topic connection: " + e.getMessage());
			}
		}
	}

	/**
	 * This is the callback method that the container calls when a message is
	 * received on the topic that this bean is subscribed to.
	 * 
	 * @param msg
	 *            Is the message object that the topic received.
	 */
	public void onMessage(javax.jms.Message msg) {
		// Check to see if it a JMS message object
		if (msg instanceof ObjectMessage) {
			// Grab the ObjectMessage
			ObjectMessage om = (ObjectMessage) msg;
			logger.debug("ObjectMessage recieved");
			logger.error("An ObjectMessage was received, "
					+ "this was deprecated and clients should be notified.");

			// Try to cast to a DevicePacket
			DevicePacket dp = null;
			try {
				dp = (DevicePacket) om.getObject();
			} catch (JMSException e) {
				logger.error("A JMSException was caught while trying to get a "
						+ "DevicePacket from the object message: "
						+ e.getMessage());
			} catch (Exception e) {
				logger.error("An Exception was caught while trying to get a "
						+ "DevicePacket from the object message: "
						+ e.getMessage());
			}

			// Make sure we have a DevicePacket to work with
			if (dp != null) {
				logger.debug("DevicePacket received with the following info: "
						+ "deviceID=" + dp.sourceID() + "," + "timestamp="
						+ dp.systemTime() + "(" + new Date(dp.systemTime())
						+ ")," + "sequenceNo=" + dp.sequenceNo());

				// Since a DevicePacket was found, convert it to a
				// SSDSDevicePacket
				SSDSDevicePacket ssdsDevicePacket = PacketUtility
						.convertSIAMDevicePacketToSSDSDevicePacket(dp);

				// Now we need to add things that are not there. These are
				// device specific things that are gathered from the properties
				// file. This is so that we can add items that are not currently
				// in the SIAM/ISI architecture like parent, metadataSeqence,
				// etc. First, check the packetType, if it is -1, then the
				// packet type was not identified. This should not happen and if
				// it does, there is not much we can do about it, but print out
				// a log statement to that effect
				if (ssdsDevicePacket.getPacketType() == -1) {
					logger.error("The following transmogrified packet has a "
							+ "packet type of -1.  "
							+ "This should not happen and needs to be "
							+ "investigated: deviceID=" + dp.sourceID()
							+ ", timestamp=" + dp.systemTime() + "("
							+ new Date(dp.systemTime()) + "), sequenceNo="
							+ dp.sequenceNo());
				}

				// Now convert it to a BytesMessage and publish it
				this.publishSSDSFormattedBytes(PacketUtility
						.convertSSDSDevicePacketToVersion3SSDSByteArray(ssdsDevicePacket));
			} else {
				logger.error("The incoming ObjectMessage did not "
						+ "contain a DevicePacket.");
			}
		} else if (msg instanceof BytesMessage) {
			// TODO kgomes, if the payload of the message is larger than 2GB, I
			// should send a rejection message to the sender! We cannot handle
			// messages larger than 2GB.

			logger.debug("BytesMessage received.");

			// Cast it to a BytesMessage that should contain the SIAM formatted
			// byte array
			BytesMessage bytesMessage = (BytesMessage) msg;

			// Let's extract the byte array from the message
			byte[] siamBytes = PacketUtility
					.extractByteArrayFromBytesMessage(bytesMessage);

			// Let's log it (controlled by log4j settings)
			PacketUtility.logSIAMMessageByteArray(siamBytes, false);

			// Pass the SIAM formatted array to the next convert to SSDS format
			// and publish stage
			convertFromSIAMToSSDSAndPublishBytes(siamBytes);
		}

	}

	/**
	 * This method takes in a SIAM formatted byte array, converts it to an SSDS
	 * format and then sends it on to the next JMS topic
	 * 
	 * @param siamBytes
	 */
	private void convertFromSIAMToSSDSAndPublishBytes(byte[] siamBytes) {
		// TODO kgomes might be nice to put some check in here to make sure
		// incoming byte array looks like a SIAM formatted byte array

		// Convert it to SSDS format
		byte[] ssdsBytes = PacketUtility
				.convertSIAMByteArrayToVersion3SSDSByteArray(siamBytes, false,
						false, false, false);
		PacketUtility.logVersion3SSDSByteArray(ssdsBytes, false);

		// Now publish those
		publishSSDSFormattedBytes(ssdsBytes);
	}

	/**
	 * This method takes in SSDS formatted bytes and sends them on to the next
	 * topic
	 * 
	 * @param ssdsBytes
	 */
	private void publishSSDSFormattedBytes(byte[] ssdsBytes) {
		// TODO kgomes might be nice to put some check in here to make sure
		// incoming byte array looks like a SSDS formatted byte array

		// Republish that to SSDS ingest
		BytesMessage newBytesMessage = null;
		try {
			newBytesMessage = session.createBytesMessage();
		} catch (JMSException e1) {
			logger.error("JMSException caught while trying to create a "
					+ "new bytes message: " + e1.getMessage());
		}
		if (newBytesMessage != null) {
			try {
				newBytesMessage.writeBytes(ssdsBytes);
			} catch (JMSException e2) {
				logger.error("JMSException caught while " + "trying to "
						+ "set the byte array in the " + "message"
						+ e2.getMessage());
			}
		}
		try {
			messageProducer.send(newBytesMessage);
		} catch (JMSException e2) {
			logger.error("JMSException caught while trying to publish "
					+ "the bytes message" + e2.getMessage());
		}

	}
}
