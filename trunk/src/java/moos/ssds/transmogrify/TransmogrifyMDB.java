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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
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

import org.apache.log4j.Logger;
import org.mbari.isi.interfaces.DevicePacket;

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
	 * This is the MessageDrivenContext that is from the container
	 */
	private javax.ejb.MessageDrivenContext ctx;

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
	 * This is a boolean to indicated if the publishing is setup and working
	 * correctly
	 */
	private boolean publishingSetup = false;

	/**
	 * These are the versions of the incoming byte streams from SIAM
	 */
	public final static short DEVICE_PACKET_STREAM_ID_VERSION_ONE = 0x0100;
	public final static long DEVICE_PACKET_VERSION_VERSION_ONE = 0;
	public final static short SENSOR_DATA_PACKET_STREAM_ID_VERSION_ONE = 0x0102;
	public final static long SENSOR_DATA_PACKET_VERSION_VERSION_ONE = 0;
	public final static short DEVICE_MESSAGE_PACKET_STREAM_ID_VERSION_ONE = 0x0103;
	public final static long DEVICE_MESSAGE_PACKET_VERSION_VERSION_ONE = 0;
	public final static short METADATA_PACKET_STREAM_ID_VERSION_ONE = 0x0101;
	public final static long METADATA_PACKET_VERSION_VERSION_ONE = 0;
	// From the SIAM guys
	public static final short EX_BASE = 0x0000;
	public static final short EX_STATE = 0x0001;
	public static final short EX_STATEATTRIBUTE = 0x0002;
	public static final short EX_BOOLEANOBJATT = 0x0003;
	public static final short EX_INTEGEROBJATT = 0x0004;
	public static final short EX_LONGOBJATT = 0x0005;
	public static final short EX_MNEMONICINTEGEROBJATT = 0x0006;
	public static final short EX_SCHEDULESPECIFIEROBJATT = 0x0007;
	public static final short EX_BYTEARRAYOBJATT = 0x0008;
	public static final short EX_FLOATOBJATT = 0x0009;
	public static final short EX_DOUBLEOBJATT = 0x000a;
	public static final short EX_DEVICEPACKET = 0x0100;
	public static final short EX_METADATAPACKET = 0x0101;
	public static final short EX_SENSORDATAPACKET = 0x0102;
	public static final short EX_DEVICEMESSAGEPACKET = 0x0103;
	public static final short EX_MAX = 0x0103;

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
		// Set the context
		ctx = context;
	} // End setMessageDrivenContext

	/**
	 * This is the callback that the container uses to create this bean
	 */
	public void ejbCreate() {
		// Grab the transmogrifier properties for the file
		transmogProps = new Properties();
		// logger.debug(
		// "Constructor called ... going to try and read the properties file in
		// ...");
		try {
			transmogProps.load(this.getClass().getResourceAsStream(
					"/moos/ssds/transmogrify/transmogrify.properties"));
		} catch (Exception e) {
			logger.error("Exception trying to read in properties file: "
					+ e.getMessage());
		}
		if (transmogProps != null) {
			// logger.debug("Loaded props OK");
		} else {
			logger
					.error("Could not seem to load the properties for transmogrifier.");
		}

		// Grab the topic name to republish to
		this.republishTopicName = transmogProps
				.getProperty("transmogrify.republish.topic");

		// Instead of using the publisher component, let's manage our own so
		// that
		// we can use a different InvocationLayer
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
		boolean tearDownOK = this.tearDownPublishing();
		this.publishingSetup = false;

		boolean setupOK = true;
		// First get the naming context from the container
		try {
			this.jndiContext = new InitialContext();
		} catch (NamingException e) {
			logger
					.error("NamingException caught from the container "
							+ "while trying to setup the publishing: "
							+ e.getMessage());
			return false;
		} catch (Exception e) {
			logger
					.error("Exception caught from the container "
							+ "while trying to setup the publishing: "
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
			logger
					.error("NamingException caught from the container "
							+ "while trying to get the JVM Invocation layer the publishing: "
							+ e.getMessage());
			return false;
		} catch (Exception e) {
			logger
					.error("Exception caught from the container "
							+ "while trying to get the JVM Invocation layer the publishing: "
							+ e.getMessage());
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
		// First close the topic publisher
		if (this.topicPublisher != null) {
			try {
				this.topicPublisher.close();
			} catch (JMSException e) {
				logger.error("JMSException caught while trying to "
						+ "close the topic publisher: " + e.getMessage());
			} catch (Exception e) {
				logger.error("Exception caught while trying to "
						+ "close the topic publisher: " + e.getMessage());
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
			} catch (Exception e) {
				logger.error("Exception caught while trying to "
						+ "stop the topic connection: " + e.getMessage());
			}
		}
		// Now close the session
		if (this.topicSession != null) {
			try {
				this.topicSession.close();
			} catch (JMSException e) {
				logger.error("JMSException caught while trying to "
						+ "close the topic session: " + e.getMessage());
			} catch (Exception e) {
				logger.error("Exception caught while trying to "
						+ "close the topic session: " + e.getMessage());
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
			} catch (Exception e) {
				logger.error("Exception caught while trying to "
						+ "close the topic connection: " + e.getMessage());
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
			} catch (Exception e) {
				logger.error("Exception caught while trying to "
						+ "close the JNDI context: " + e.getMessage());
			}
			this.jndiContext = null;
		}
		// Null out the topic connection factory
		this.topicConnectionFactory = null;

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
	 *            Is the message object that the topic recieved.
	 */
	public void onMessage(javax.jms.Message msg) {
		// Check to see if it a JMS message object
		if (msg instanceof ObjectMessage) {
			// Grab the ObjectMessage
			ObjectMessage om = (ObjectMessage) msg;
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
			logger.debug("ObjectMessage recieved");
			// Now check to see if the object it is carrying is a DevicePacket
			if (dp != null) {
				logger
						.debug("ObjectMessage was a DevicePacket with the following info: "
								+ "deviceID="
								+ dp.sourceID()
								+ ","
								+ "timestamp="
								+ dp.systemTime()
								+ "("
								+ new Date(dp.systemTime())
								+ "),"
								+ "sequenceNo=" + dp.sequenceNo());
				// Since a DevicePacket was found, convert it to a
				// SSDSDevicePacket
				SSDSDevicePacket ssdsDevicePacket = new SSDSDevicePacket(dp);

				// Now we need to add things that are not there. These are
				// device specific things that are gathered from the properties
				// file. This is so that we can add items that are not currently
				// in the SIAM/ISI architecture like parent, metadataSeqence,
				// etc. First, check the packetType, if it is -1, then the
				// packet type was not identified. This should not happen and if
				// it does, there is not much we can do about it, but print out
				// a log statement to that effect
				if (ssdsDevicePacket.getPacketType() == -1) {
					logger
							.error("The transmogrified packet has a packet type of -1.  "
									+ "This should not happen and needs to be investigated");
				}

				// Now convert it to a BytesMessage and publish it
				try {
					this.checkAndPublishBytes(SSDSDevicePacket
							.convertToPublishableByteArray(ssdsDevicePacket));
				} catch (IOException e) {
					logger
							.error("Could not convert SSDSDevicePacket to a publishable byte array and send: "
									+ e.getMessage());
				}
			} else {
				logger
						.error("The incoming ObjectMessage did not contain a DevicePacket.");
			}
		} else if (msg instanceof BytesMessage) {
			BytesMessage bytesMessage = (BytesMessage) msg;
			// Create a output stream to write to
			ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
			DataOutputStream dataOS = new DataOutputStream(byteOS);
			// Now read in all the information from the SIAM byte array
			try {
				StringBuffer loggerMessage = new StringBuffer();
				loggerMessage.append("BytesMessage contains:");
				// Read in the StreamID
				short streamID = bytesMessage.readShort();
				loggerMessage.append("StreamID=" + streamID + ",");
				dataOS.writeShort(streamID);

				// Read in the devicePacketVersion
				long devicePacketVersion = bytesMessage.readLong();
				loggerMessage.append("DevicePacketVersion="
						+ devicePacketVersion + ",");
				dataOS.writeLong(devicePacketVersion);

				// Read in the sourceID
				long sourceID = bytesMessage.readLong();
				loggerMessage.append("SourceID=" + sourceID + ",");
				dataOS.writeLong(sourceID);

				// Read in the in timestamp
				long timestamp = bytesMessage.readLong();
				loggerMessage.append("timestamp=" + timestamp + "("
						+ new Date(timestamp) + "),");
				dataOS.writeLong(timestamp);

				// Read in the sequence number
				long sequenceNumber = bytesMessage.readLong();
				loggerMessage.append("sequenceNumber=" + sequenceNumber + ",");
				dataOS.writeLong(sequenceNumber);

				// Read in the metadataref number
				long metadataRef = bytesMessage.readLong();
				loggerMessage.append("metadataRef=" + metadataRef + ",");
				dataOS.writeLong(metadataRef);

				// Read in the parentID
				long parentID = bytesMessage.readLong();
				loggerMessage.append("parentID=" + parentID + ",");
				dataOS.writeLong(parentID);

				// Read in the recordType
				long recordType = bytesMessage.readLong();
				loggerMessage.append("recordType=" + recordType + ",");
				dataOS.writeLong(recordType);

				// Read in the streamID
				short secondStreamID = bytesMessage.readShort();
				loggerMessage.append("secondStreamID=" + secondStreamID + ",");
				dataOS.writeShort(secondStreamID);

				// Read packet Version
				long secondPacketVersion = bytesMessage.readLong();
				loggerMessage.append("secondPacketVersion="
						+ secondPacketVersion + ",");
				dataOS.writeLong(secondPacketVersion);

				// Read in the first data buffer length
				int firstBufferLength = 0;
				try {
					firstBufferLength = bytesMessage.readInt();
				} catch (Exception e) {
				}
				if (firstBufferLength > 0) {
					loggerMessage.append("firstBufferLength="
							+ firstBufferLength + ",");
					dataOS.writeInt(firstBufferLength);
					byte[] firstBufferBytes = new byte[firstBufferLength];
					bytesMessage.readBytes(firstBufferBytes);
					StringBuffer hexData = new StringBuffer();
					ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(
							firstBufferBytes);
					while (byteArrayIS.available() > 0) {
						hexData.append(Integer.toHexString(
								(0xFF & byteArrayIS.read()) | 0x100).substring(
								1));
					}
					loggerMessage.append("firstBuffer(in hex)="
							+ hexData.toString() + ",");
					dataOS.write(firstBufferBytes);
					int secondBufferLength = 0;
					try {
						secondBufferLength = bytesMessage.readInt();
					} catch (Exception e) {
					}
					if (secondBufferLength > 0) {
						loggerMessage.append("secondBufferLength="
								+ secondBufferLength + ",");
						dataOS.writeInt(secondBufferLength);
						byte[] secondBufferBytes = new byte[secondBufferLength];
						bytesMessage.readBytes(secondBufferBytes);
						StringBuffer hexTwoData = new StringBuffer();
						ByteArrayInputStream byteTwoArrayIS = new ByteArrayInputStream(
								secondBufferBytes);
						while (byteTwoArrayIS.available() > 0) {
							hexTwoData.append(Integer.toHexString(
									(0xFF & byteTwoArrayIS.read()) | 0x100)
									.substring(1));
						}
						loggerMessage.append("secondBuffer(in hex)="
								+ hexTwoData.toString() + ",");
						dataOS.write(secondBufferBytes);
					}
				}
				logger.debug(loggerMessage.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
			}
			// Grab the byte array out and call the check and publish
			checkAndPublishBytes(byteOS.toByteArray());
		}

	}

	private void checkAndPublishBytes(byte[] bytes) {
		// So the incoming byte array should be in the SIAM format so we need
		// to extract the information we need from it
		ByteArrayInputStream byteIS = new ByteArrayInputStream(bytes);
		DataInputStream dataIS = new DataInputStream(byteIS);

		// Create the output byte array (streams)
		ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
		DataOutputStream dataOS = new DataOutputStream(byteOS);

		// Now read in all the information from the SIAM byte array
		short devicePacketStreamID = -99;
		long devicePacketVersion = -999999;
		long sourceID = -999999;
		long timestamp = -999999;
		long sequenceNumber = -999999;
		long metadataRef = -999999;
		long parentID = -999999;
		int packetType = -999999;
		long recordType = -999999;
		short subclassStreamID = -99;
		long subclassPacketVersion = -999999;
		int firstBufferLength = 1;
		byte[] firstBufferBytes = new byte[firstBufferLength];
		int secondBufferLength = 1;
		byte[] secondBufferBytes = new byte[secondBufferLength];
		try {
			devicePacketStreamID = dataIS.readShort();
			devicePacketVersion = dataIS.readLong();
			sourceID = dataIS.readLong();
			timestamp = dataIS.readLong();
			sequenceNumber = dataIS.readLong();
			metadataRef = dataIS.readLong();
			parentID = dataIS.readLong();
			recordType = dataIS.readLong();
			subclassStreamID = dataIS.readShort();
			subclassPacketVersion = dataIS.readLong();
			// OK, so we now know the core components, we need to
			// do some specific processing depending on these values
			if (devicePacketVersion == TransmogrifyMDB.DEVICE_PACKET_VERSION_VERSION_ONE) {
				if (subclassStreamID == TransmogrifyMDB.METADATA_PACKET_STREAM_ID_VERSION_ONE) {
					if (subclassPacketVersion == TransmogrifyMDB.METADATA_PACKET_VERSION_VERSION_ONE) {
						packetType = 0;
						secondBufferLength = dataIS.readInt();
						secondBufferBytes = new byte[secondBufferLength];
						dataIS.read(secondBufferBytes);
						firstBufferLength = dataIS.readInt();
						firstBufferBytes = new byte[firstBufferLength];
						dataIS.read(firstBufferBytes);
					}
				} else if (subclassStreamID == TransmogrifyMDB.SENSOR_DATA_PACKET_STREAM_ID_VERSION_ONE) {
					if (subclassPacketVersion == TransmogrifyMDB.SENSOR_DATA_PACKET_VERSION_VERSION_ONE) {
						packetType = 1;
						firstBufferLength = dataIS.readInt();
						firstBufferBytes = new byte[firstBufferLength];
						dataIS.read(firstBufferBytes);
					}
				} else if (subclassStreamID == TransmogrifyMDB.DEVICE_MESSAGE_PACKET_STREAM_ID_VERSION_ONE) {
					if (subclassPacketVersion == TransmogrifyMDB.DEVICE_MESSAGE_PACKET_VERSION_VERSION_ONE) {
						packetType = 2;
						firstBufferLength = dataIS.readInt();
						firstBufferBytes = new byte[firstBufferLength];
						dataIS.read(firstBufferBytes);
					}
				}
			}
			try {
				StringBuffer tempMessage = new StringBuffer();
				tempMessage.append("StreamID=" + devicePacketStreamID + ",");
				tempMessage.append("DevicePacketVersion=" + devicePacketVersion
						+ ",");
				tempMessage.append("SourceID=" + sourceID + ",");
				tempMessage.append("timestamp=" + timestamp + "("
						+ new Date(timestamp) + "),");
				tempMessage.append("sequenceNumber=" + sequenceNumber + ",");
				tempMessage.append("metadataRef=" + metadataRef + ",");
				tempMessage.append("parentID=" + parentID + ",");
				tempMessage.append("recordType=" + recordType + ",");
				tempMessage.append("packetType=" + packetType + ",");
				tempMessage.append("secondStreamID=" + subclassStreamID + ",");
				tempMessage.append("secondPacketVersion="
						+ subclassPacketVersion + ",");
				tempMessage.append("firstBufferLength=" + firstBufferLength
						+ ",");
				StringBuffer hexData = new StringBuffer();
				ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(
						firstBufferBytes);
				while (byteArrayIS.available() > 0) {
					hexData.append(Integer.toHexString(
							(0xFF & byteArrayIS.read()) | 0x100).substring(1));
				}
				tempMessage.append("firstBuffer(in hex)=" + hexData.toString()
						+ ",");
				tempMessage.append("secondBufferLength=" + secondBufferLength);
				hexData = new StringBuffer();
				byteArrayIS = new ByteArrayInputStream(secondBufferBytes);
				while (byteArrayIS.available() > 0) {
					hexData.append(Integer.toHexString(
							(0xFF & byteArrayIS.read()) | 0x100).substring(1));
				}
				tempMessage
						.append("secondBuffer(in hex)=" + hexData.toString());
				logger.debug("checkAndPublishBytes:" + tempMessage.toString());
			} catch (Exception e) {
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Write out the data in our format
		try {
			// Write the device ID
			dataOS.writeLong(sourceID);
			// Write the parent ID
			dataOS.writeLong(parentID);
			// Convert the packet type to the ones we are expecting
			if (packetType == 0) {
				dataOS.writeInt(1);
			} else if (packetType == 1) {
				dataOS.writeInt(0);
			} else if (packetType == 2) {
				dataOS.writeInt(4);
			}
			if (packetType == 0) {
				// Update the siam metadata tracker with the metadata
				SIAMMetadataTracker.checkMetadataForChange(sourceID, parentID,
						timestamp, firstBufferBytes);
			}
			// Write the packet sub type (which is their record type)
			if (packetType == 0) {
				dataOS.writeLong(0);
			} else {
				dataOS.writeLong(recordType);
			}
			// Write the correct metadata sequence number
			if (packetType == 0) {
				// TODO KJG: I think this will need to match the revision number
				// that the SIAMMetadataTracker utilizes? So I commented out the
				// zero and added a lookup. If that works, I should just
				// collapse
				// all these to one statement
				// dataOS.writeLong(0);
				dataOS.writeLong(SIAMMetadataTracker.findDataDescriptionID(
						sourceID, parentID, timestamp));
			} else if (packetType == 1) {
				dataOS.writeLong(SIAMMetadataTracker.findDataDescriptionID(
						sourceID, parentID, timestamp));
			} else if (packetType == 2) {
				dataOS.writeLong(SIAMMetadataTracker.findDataDescriptionID(
						sourceID, parentID, timestamp));
			}
			// Write the DataDescription version
			dataOS.writeLong(metadataRef);
			// Now for time stamp, I have to convert it from
			// milliseconds since 1/1/70 to seconds/nanoseconds
			// since 1/1/70
			long timestampSeconds = timestamp / 1000;
			long timestampNanoseconds = (timestamp % 1000 * 1000);
			dataOS.writeLong(timestampSeconds);
			dataOS.writeLong(timestampNanoseconds);
			// Write the sequence number
			dataOS.writeLong(sequenceNumber);
			dataOS.writeInt(firstBufferLength);
			dataOS.write(firstBufferBytes);
			dataOS.writeInt(secondBufferLength);
			dataOS.write(secondBufferBytes);
		} catch (IOException e1) {
			logger.error("IOException trying to write packet in our format"
					+ e1.getMessage());
		}
		// Now rebuplish that to SSDS ingest
		BytesMessage newBytesMessage = null;
		try {
			newBytesMessage = topicSession.createBytesMessage();
		} catch (JMSException e1) {
			logger.error("JMSException caught while trying to create a "
					+ "new bytes message: " + e1.getMessage());
			this.publishingSetup = false;
			this.setupPublishing();
		}
		if (newBytesMessage != null) {
			try {
				newBytesMessage.writeBytes(byteOS.toByteArray());
			} catch (JMSException e2) {
				logger
						.error("JMSException caught while trying to set the byte array in the message"
								+ e2.getMessage());
				this.publishingSetup = false;
			}
		}
		try {
			topicPublisher.publish(newBytesMessage);
		} catch (JMSException e2) {
			logger
					.error("JMSException caught while trying to publish the bytes message"
							+ e2.getMessage());
			this.publishingSetup = false;
		}
	}
} // End IngestMDB
