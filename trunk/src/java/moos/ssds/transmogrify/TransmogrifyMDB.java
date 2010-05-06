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
import java.util.Enumeration;
import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

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
 * 
 *          From here down is XDoclet Stuff for generating deployment
 *          configuration files
 * @ejb.bean name="Transmogrifier" display-name="Transmogrifier Message-Driven
 *           Bean" description="Transmogrifies stuff into stuff"
 *           transaction-type="Container" acknowledge-mode="Auto-acknowledge"
 *           destination-type="javax.jms.Topic"
 * @ejb.resource-ref res-ref-name="TopicConnectionFactory"
 *                   res-type="javax.jms.TopicConnectionFactory"
 *                   res-auth="Application"
 * @jboss.destination-jndi-name name="topic/${transmogrify.topic.name}"
 */
public class TransmogrifyMDB implements javax.ejb.MessageDrivenBean,
		javax.jms.MessageListener {

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
	 * This is the properties that contains the information needed to
	 * transmogrify incoming packets
	 */
	private Properties transmogProps = null;

	/**
	 * This is the name of the topic that the packets will be republished to
	 */
	private String republishTopicName = null;

	/**
	 * This is the JNDI Context that will be used (Naming Service) to locate the
	 * appropriate remote classes to use for publishing messages.
	 */
	private Context jndiContext = null;

	/**
	 * The TopicConnectionFactory that will be used to republish messages
	 */
	private TopicConnectionFactory topicConnectionFactory = null;

	/**
	 * This is the connection to the topic that the messages will be published
	 * to
	 */
	private TopicConnection topicConnection = null;

	/**
	 * This is the JMS topic that will be used for publishing
	 */
	private Topic topic = null;

	/**
	 * This is a session that the publishing of messages will be run in.
	 */
	private TopicSession topicSession = null;

	/**
	 * This is the topic publisher that is actually used to send messages to the
	 * topic
	 */
	private TopicPublisher topicPublisher = null;

	/**
	 * This is the default constructor
	 */
	public TransmogrifyMDB() {
	} // End Constructor

	/**
	 * This is the callback that the container uses to set the
	 * MessageDrivenContext
	 */
	public void setMessageDrivenContext(javax.ejb.MessageDrivenContext context) {
	} // End setMessageDrivenContext

	/**
	 * This is the callback that the container uses to create this bean
	 */
	@SuppressWarnings("unchecked")
	public void ejbCreate() {
		// Grab the transmogrifier properties for the file
		transmogProps = new Properties();
		logger.debug("Constructor called ... going to "
				+ "try and read the properties file in ...");
		try {
			transmogProps.load(this.getClass().getResourceAsStream(
					"/moos/ssds/transmogrify/transmogrify.properties"));
		} catch (Exception e) {
			logger.error("Exception trying to read in properties file: "
					+ e.getMessage());
		}
		// If they are not null, write them to a debug log
		if (transmogProps != null) {
			logger.debug("Loaded props OK and they are:");
			Enumeration keys = transmogProps.keys();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				String value = (String) transmogProps.get(key);
				logger.debug(key + ": " + value);
			}
		} else {
			logger.error("Could not seem to load the "
					+ "properties for transmogrifier.");
		}

		// Grab the topic name to republish to
		this.republishTopicName = transmogProps
				.getProperty("transmogrify.republish.topic");

		// Instead of using the publisher component, let's manage our own so
		// that we can use a different InvocationLayer
		this.setupPublishing();

	} // End ejbCreate

	/**
	 * This method sets up the publishing so the the message driven bean can
	 * republish the message after doing its thing.
	 * 
	 * @return a <code>boolean</code> that indicates if the setup went OK or not
	 */
	private boolean setupPublishing() {
		// First tear down any existing connections
		this.tearDownPublishing();

		// First get the naming context from the container
		try {
			this.jndiContext = new InitialContext();
		} catch (NamingException e) {
			logger.error("NamingException caught from the container "
					+ "while trying to setup the " + "publishing: "
					+ e.getMessage());
			return false;
		} catch (Exception e) {
			logger.error("Exception caught from the container "
					+ "while trying to setup the " + "publishing: "
					+ e.getMessage());
			return false;
		}

		// Now lets get the topic connection factory. In this case, we want
		// to use the the JVM Invocation layer since we are in the same
		// jvm as jboss.
		try {
			topicConnectionFactory = (TopicConnectionFactory) jndiContext
					.lookup("java:/ConnectionFactory");
		} catch (NamingException e) {
			logger.error("NamingException caught from the container "
					+ "while trying to get the JVM Invocation "
					+ "layer the publishing: " + e.getMessage());
			return false;
		} catch (Exception e) {
			logger.error("Exception caught from the container "
					+ "while trying to get the JVM Invocation "
					+ "layer the publishing: " + e.getMessage());
			return false;
		}

		// Now create a topic connection from the factory
		try {
			this.topicConnection = topicConnectionFactory
					.createTopicConnection();
		} catch (JMSException e1) {
			logger.error("JMSException caught from the container "
					+ "while trying to create the TopicConnection: "
					+ e1.getMessage());
			return false;
		} catch (Exception e1) {
			logger.error("Exception caught from the container "
					+ "while trying to create the TopicConnection: "
					+ e1.getMessage());
			return false;
		}

		// Grab the topic from the container
		try {
			this.topic = (Topic) jndiContext.lookup(this.republishTopicName);
		} catch (NamingException e) {
			logger.error("NamingException caught from the container "
					+ "while trying to get the topic "
					+ this.republishTopicName + " for publishing: "
					+ e.getMessage());
			return false;
		} catch (Exception e) {
			logger.error("Exception caught from the container "
					+ "while trying to get the topic "
					+ this.republishTopicName + " for publishing: "
					+ e.getMessage());
			return false;
		}

		// Now create the topic session
		try {
			this.topicSession = this.topicConnection.createTopicSession(false,
					Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e2) {
			logger.error("JMSException caught while trying to create the"
					+ " topicSession from the connection: " + e2.getMessage());
			return false;
		} catch (Exception e2) {
			logger.error("Exception caught while trying to create the"
					+ " topicSession from the connection: " + e2.getMessage());
			return false;
		}

		// Now start the connection
		try {
			this.topicConnection.start();
		} catch (JMSException e2) {
			logger.error("JMSException caught while trying to start the"
					+ " topicConnection: " + e2.getMessage());
			return false;
		} catch (Exception e2) {
			logger.error("Exception caught while trying to start the"
					+ " topicConnection: " + e2.getMessage());
			return false;
		}

		// Now create the publisher
		try {
			this.topicPublisher = topicSession.createPublisher(this.topic);
		} catch (JMSException e2) {
			logger.error("JMSException caught while trying to create the"
					+ " topicPublisher from the topicSession: "
					+ e2.getMessage());
			return false;
		} catch (Exception e2) {
			logger.error("Exception caught while trying to create the"
					+ " topicPublisher from the topicSession: "
					+ e2.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * This method stops all the JMS components
	 * 
	 * @return
	 */
	private boolean tearDownPublishing() {
		// First close the topic publisher
		if (this.topicPublisher != null) {
			try {
				this.topicPublisher.close();
			} catch (JMSException e) {
				logger.error("JMSException caught while trying to "
						+ "close the topic publisher: " + e.getMessage());
				return false;
			} catch (Exception e) {
				logger.error("Exception caught while trying to "
						+ "close the topic publisher: " + e.getMessage());
				return false;
			}
			this.topicPublisher = null;
		}
		// Now stop the connection
		if (this.topicConnection != null) {
			try {
				this.topicConnection.stop();
			} catch (JMSException e) {
				logger.error("JMSException caught while trying to "
						+ "stop the topic connection: " + e.getMessage());
				return false;
			} catch (Exception e) {
				logger.error("Exception caught while trying to "
						+ "stop the topic connection: " + e.getMessage());
				return false;
			}
		}
		// Now close the session
		if (this.topicSession != null) {
			try {
				this.topicSession.close();
			} catch (JMSException e) {
				logger.error("JMSException caught while trying to "
						+ "close the topic session: " + e.getMessage());
				return false;
			} catch (Exception e) {
				logger.error("Exception caught while trying to "
						+ "close the topic session: " + e.getMessage());
				return false;
			}
			this.topicSession = null;
		}
		// Now close the connection
		if (this.topicConnection != null) {
			try {
				this.topicConnection.close();
			} catch (JMSException e) {
				logger.error("JMSException caught while trying to "
						+ "close the topic connection: " + e.getMessage());
				return false;
			} catch (Exception e) {
				logger.error("Exception caught while trying to "
						+ "close the topic connection: " + e.getMessage());
				return false;
			}
			this.topicConnection = null;
		}
		// Now close the jndi context
		if (this.jndiContext != null) {
			try {
				this.jndiContext.close();
			} catch (NamingException e) {
				logger.error("Naming Exception caught while trying to "
						+ "close the JNDI context: " + e.getMessage());
				return false;
			} catch (Exception e) {
				logger.error("Exception caught while trying to "
						+ "close the JNDI context: " + e.getMessage());
				return false;
			}
			this.jndiContext = null;
		}
		// Null out the topic connection factory
		this.topicConnectionFactory = null;

		return true;
	}

	/**
	 * This is the callback that the container uses when removing this bean
	 */
	public void ejbRemove() {
		this.tearDownPublishing();
	} // End ejbRemove

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
				this
						.publishSSDSFormattedBytes(PacketUtility
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
		byte[] ssdsBytes = PacketUtility.convertSIAMByteArrayToVersion3SSDSByteArray(
				siamBytes, false, false, false, false);
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
			newBytesMessage = topicSession.createBytesMessage();
		} catch (JMSException e1) {
			logger.error("JMSException caught while trying to create a "
					+ "new bytes message: " + e1.getMessage());
			// TODO kgomes while setupPublishing helps for the next message,
			// this message will be lost
			this.setupPublishing();
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
			topicPublisher.publish(newBytesMessage);
		} catch (JMSException e2) {
			logger.error("JMSException caught while trying to publish "
					+ "the bytes message" + e2.getMessage());
		}

	}
}
