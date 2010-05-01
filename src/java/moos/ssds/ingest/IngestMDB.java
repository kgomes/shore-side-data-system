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
package moos.ssds.ingest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import moos.ssds.io.PacketOutput;
import moos.ssds.io.PacketOutputManager;
import moos.ssds.io.PacketSQLOutput;
import moos.ssds.io.util.PacketUtility;

import org.apache.log4j.Logger;

/**
 * <p>
 * This class is a Message Driven Bean (MDB) that listen to a topic for incoming
 * messages. It then serializes those message to disk in file that are organized
 * by certain &quot;keys&quot;. It then passes these message on to more topics
 * for further processing.
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.11.2.1 $ <br>
 *          XDoclet Stuff for deployment
 * @ejb.bean name="Ingest" display-name="Ingest Message-Driven Bean"
 *           description="This is the front-line ingest for the Shore-Side Data
 *           System-SSDS" transaction-type="Container"
 *           acknowledge-mode="Auto-acknowledge"
 *           destination-type="javax.jms.Topic"
 * @ejb.resource-ref res-ref-name="TopicConnectionFactory"
 *                   res-type="javax.jms.TopicConnectionFactory"
 *                   res-auth="Application"
 * @jboss.destination-jndi-name name="topic/${ingest.topic.name}"
 */
public class IngestMDB implements javax.ejb.MessageDrivenBean,
		javax.jms.MessageListener {

	/**
	 * A serial version ID to make eclipse happy
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(IngestMDB.class);

	/**
	 * This is the properties that contains the information needed to ingest
	 * incoming packets
	 */
	private Properties ingestProps = null;

	/**
	 * These are the names of the topics that the packets will be republished to
	 */
	private String republishTopicName = null;

	/**
	 * This is a flag to indicate if Ingest should serialize packets to disk or
	 * simply rely on the database storage
	 */
	private boolean fileSerializationEnabled = false;

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
	 * These are the JMS topics that will be used for publishing
	 */
	private Topic topic = null;

	/**
	 * This is a session that the publishing of messages will be run in.
	 */
	private TopicSession topicSession = null;

	/**
	 * These are the topic publishers that are actually used to send messages to
	 * the topic
	 */
	private TopicPublisher topicPublisher = null;

	/**
	 * This is a boolean to indicated if the publishing is setup and working
	 * correctly
	 */
	private boolean publishingSetup = false;

	/**
	 * This is the default constructor
	 */
	public IngestMDB() {
	}

	/**
	 * This is the callback that the container uses to set the
	 * MessageDrivenContext
	 */
	public void setMessageDrivenContext(javax.ejb.MessageDrivenContext context) {
	}

	/**
	 * This is the callback that the container uses to create this bean
	 */
	public void ejbCreate() {
		// Grab the transmogrifier properties for the file
		ingestProps = new Properties();
		try {
			ingestProps.load(this.getClass().getResourceAsStream(
					"/moos/ssds/ingest/ingest.properties"));
		} catch (Exception e) {
			logger.error("Exception trying to read in properties file: "
					+ e.getMessage());
		}

		// Grab the topic name to republish to
		this.republishTopicName = ingestProps
				.getProperty("ingest.republish.topic");

		// Grab the property that indicates if file serialization is requested
		if (ingestProps.getProperty("ingest.file.serialization")
				.equalsIgnoreCase("on"))
			fileSerializationEnabled = true;

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
		this.publishingSetup = false;

		// Set a flag to track success of setup
		boolean setupOK = true;
		// First get the naming context from the container
		try {
			this.jndiContext = new InitialContext();
			topicConnectionFactory = (TopicConnectionFactory) jndiContext
					.lookup("java:/ConnectionFactory");
			this.topicConnection = topicConnectionFactory
					.createTopicConnection();
			this.topic = (Topic) jndiContext.lookup(this.republishTopicName);
			this.topicSession = this.topicConnection.createTopicSession(false,
					Session.AUTO_ACKNOWLEDGE);
			this.topicConnection.start();
			this.topicPublisher = topicSession.createPublisher(this.topic);
		} catch (NamingException e) {
			logger.error("NamingException caught in setupPublishing: "
					+ e.getMessage());
			this.publishingSetup = false;
			setupOK = false;
		} catch (JMSException e) {
			logger.error("JMSException caught in setupPublishing: "
					+ e.getMessage());
			this.publishingSetup = false;
			setupOK = false;
		} catch (Exception e) {
			logger.error("Exception caught in setupPublishing: "
					+ e.getMessage());
			this.publishingSetup = false;
			setupOK = false;
		}
		this.publishingSetup = true;
		return setupOK;
	}

	/**
	 * This method stops all the JMS components
	 * 
	 * @return
	 */
	private boolean tearDownPublishing() {
		this.publishingSetup = false;
		boolean tearDownOK = true;
		try {
			// Close up everything
			if (this.topicPublisher != null) {
				this.topicPublisher.close();
				this.topicPublisher = null;
			}
			// Now stop the connection
			if (this.topicConnection != null) {
				this.topicConnection.stop();
			}
			// Now close the session
			if (this.topicSession != null) {
				this.topicSession.close();
				this.topicSession = null;
			}
			// Now close the connection
			if (this.topicConnection != null) {
				this.topicConnection.close();
				this.topicConnection = null;
			}
			// Now close the jndi context
			if (this.jndiContext != null) {
				this.jndiContext.close();
				this.jndiContext = null;
			}
			// Null out the topic connection factory
			this.topicConnectionFactory = null;
		} catch (JMSException e) {
			logger.error("Tear down caught a JMSException " + e.getMessage());
			tearDownOK = false;
		} catch (NamingException e) {
			logger.error("Tear down caught a NameException " + e.getMessage());
			tearDownOK = false;
		} catch (Exception e) {
			logger.error("Tear down caught a Exception " + e.getMessage());
			tearDownOK = false;
		}

		return tearDownOK;
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
		// Make sure it is a bytes message
		if (msg instanceof BytesMessage) {
			logger.debug("Received BytesMessage");
			// Cast it
			BytesMessage bytesMessage = (BytesMessage) msg;

			// Read in the keys for the metadata packet
			long deviceID = -999999;
			long parentID = -999999;
			int packetType = -999999;
			long packetSubType = -999999;
			long dataDescriptionID = -999999;
			try {
				deviceID = bytesMessage.readLong();
				parentID = bytesMessage.readLong();
				packetType = bytesMessage.readInt();
				packetSubType = bytesMessage.readLong();
				dataDescriptionID = bytesMessage.readLong();
			} catch (JMSException e) {
				logger.error("JMSException caught trying to read the "
						+ "deviceID, parentID, packetType, packetSubType "
						+ "and dataDescriptionID from the bytes message: "
						+ e.getMessage());
			}

			// Check to see if the message should be serialized to disk
			if (fileSerializationEnabled)
				persistBytesMessageToFile(deviceID, parentID, packetSubType,
						dataDescriptionID, bytesMessage);

			// Now "Serialize" the packet to the database
			persistBytesMessageToDatabase(deviceID, bytesMessage);

			// If the packet is a metadata packet, send on to Ruminate
			if (packetType == 1) {
				logger.debug("bytesMessage was a metadata packet "
						+ "so ingest will republish");
				// Should be it, now republish the packet to the next step
				// Create a new message
				if (!this.publishingSetup) {
					logger.error("Publishing was not setup");
					this.setupPublishing();
				}
				try {
					topicPublisher.publish(bytesMessage);
				} catch (JMSException e2) {
					logger.error("JMSException caught while trying "
							+ "to publish the bytes message" + e2.getMessage());
					this.publishingSetup = false;
				}
			}
		}

	}

	/**
	 * This method takes in a BytesMessage (JMS Message) and records it's
	 * contents to storage on disk
	 * 
	 * @param bytesMessage
	 */
	private void persistBytesMessageToFile(long deviceID, long parentID,
			long packetSubType, long dataDescriptionID,
			BytesMessage bytesMessage) {

		// This assumes that this byte array is in the form of the SSDS
		// specification
		PacketOutput po = PacketOutputManager.getPacketOutput(deviceID,
				dataDescriptionID, packetSubType, parentID);
		try {
			po.writeBytesMessage(bytesMessage);
		} catch (IOException e1) {
			logger.error("IOException caught trying to write to disk: "
					+ e1.getMessage());
		}
	} // End onMessage

	/**
	 * This method takes in the device ID and the bytes message and persists it
	 * to the database
	 * 
	 * @param deviceID
	 * @param bytesMessage
	 */
	private void persistBytesMessageToDatabase(long deviceID,
			BytesMessage bytesMessage) {
		// Grab the SQL packet output
		PacketSQLOutput po = PacketOutputManager.getPacketSQLOutput(deviceID);
		try {
			po.writeBytesMessage(bytesMessage);
		} catch (SQLException e1) {
			logger.error("SQLException caught trying to persist "
					+ "packet from device " + deviceID + " to the database: "
					+ e1.getMessage());
		}
		// TODO kgomes there needs to be some mechanism here to keep trying and
		// ensure that incoming messages get persisted if it is just the
		// connection that has gone down
	}
} // End IngestMDB
