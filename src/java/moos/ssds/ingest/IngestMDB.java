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
import javax.jms.Session;
import javax.sql.DataSource;

import moos.ssds.io.PacketOutput;
import moos.ssds.io.PacketOutputManager;
import moos.ssds.io.PacketSQLOutput;

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
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "topic/SSDSIngestTopic"),
		@ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable") })
public class IngestMDB implements MessageListener {

	/**
	 * A serial version ID to make eclipse happy
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(IngestMDB.class);

	/**
	 * The ConnectionFactory that will be injected by the container
	 */
	@Resource(mappedName = "ConnectionFactory")
	private ConnectionFactory connectionFactory;

	/**
	 * This is the JMS destination that will be published to and will be
	 * injected by the container
	 */
	@Resource(mappedName = "topic/SSDSRuminateTopic")
	private Destination destination;

	/**
	 * This is the data source where data will be written to
	 */
	@Resource(mappedName = "java:/SSDS_Data")
	private static DataSource dataSource;

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
	 * This is the properties that contains the information needed to ingest
	 * incoming packets
	 */
	private Properties ingestProps = null;

	/**
	 * This is a flag to indicate if Ingest should serialize packets to disk or
	 * simply rely on the database storage
	 */
	private boolean fileSerializationEnabled = false;

	/**
	 * This method sets up the internal properties and message producer
	 * utilizing resources from the container
	 */
	@PostConstruct
	public void setup() {
		// Grab the ingest properties from the properties file
		ingestProps = new Properties();
		try {
			ingestProps.load(this.getClass().getResourceAsStream(
					"/moos/ssds/ingest/ingest.properties"));
		} catch (Exception e) {
			logger.error("Exception trying to read in properties file: "
					+ e.getMessage());
		}

		// Grab the property that indicates if file serialization is requested
		if (ingestProps.getProperty("ingest.file.serialization")
				.equalsIgnoreCase("on"))
			fileSerializationEnabled = true;

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
	 * This method stops all the JMS components
	 */
	@PreDestroy
	public void tearDownPublishing() {
		try {
			// Now close the connection
			if (connection != null) {
				connection.close();
			}
		} catch (JMSException e) {
			logger.error("JMSException caught trying to close the connection: "
					+ e.getMessage());
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
				try {
					messageProducer.send(bytesMessage);
				} catch (JMSException e2) {
					logger.error("JMSException caught while trying "
							+ "to publish the bytes message" + e2.getMessage());
				}
			}
		}

	}

	/**
	 * This method takes packet information and serializes to disk
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
		PacketSQLOutput po = PacketOutputManager.getPacketSQLOutput(dataSource,
				deviceID);
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
