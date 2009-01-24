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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
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
 * @version : $Revision: 1.11.2.1 $
 *          <br>
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
public class IngestMDB
    implements
        javax.ejb.MessageDrivenBean,
        javax.jms.MessageListener {

    /**
     * This is the MessageDrivenContext that is set by the container
     */
    private javax.ejb.MessageDrivenContext ctx;

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
    private String sqlTopicName = null;
    private String arrivalTopicName = null;

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
    private Topic sqlTopic = null;
    private Topic arrivalTopic = null;

    /**
     * This is a session that the publishing of messages will be run in.
     */
    private TopicSession topicSession = null;

    /**
     * These are the topic publishers that are actually used to send messages to
     * the topic
     */
    private TopicPublisher topicPublisher = null;
    private TopicPublisher sqlTopicPublisher = null;
    private TopicPublisher arrivalTopicPublisher = null;

    /**
     * This is a boolean to indicated if the publishing is setup and working
     * correctly
     */
    private boolean publishingSetup = false;

    /**
     * This is the <code>PacketOutputManager</code> that will manage the
     * <code>PacketOutputs</code> that will be used by the ingest bean to
     * serialize packets
     */
    PacketOutputManager pom = PacketOutputManager.getInstance();

    /**
     * This is the default constructor
     */
    public IngestMDB() {}

    /**
     * This is the callback that the container uses to set the
     * MessageDrivenContext
     */
    public void setMessageDrivenContext(javax.ejb.MessageDrivenContext context) {
        // Set the context
        ctx = context;
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
        this.sqlTopicName = ingestProps.getProperty("ingest.sql.topic");
        this.arrivalTopicName = ingestProps.getProperty("ingest.arrival.topic");

        // Instead of using the publisher component, let's manage our own so
        // that we can use a different InvocationLayer
        this.setupPublishing();

    } // End ejbCreate

    /**
     * This method sets up the publishing so the the message driven bean can
     * republish the message after doing its thing.
     * 
     * @return a <code>boolean</code> that indicates if the setup went OK or
     *         not
     */
    private boolean setupPublishing() {
        // First tear down any existing connections
        boolean tearDownOK = this.tearDownPublishing();
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
            this.sqlTopic = (Topic) jndiContext.lookup(this.sqlTopicName);
            this.arrivalTopic = (Topic) jndiContext
                .lookup(this.arrivalTopicName);
            this.topicSession = this.topicConnection.createTopicSession(false,
                Session.AUTO_ACKNOWLEDGE);
            this.topicConnection.start();
            this.topicPublisher = topicSession.createPublisher(this.topic);
            this.sqlTopicPublisher = topicSession
                .createPublisher(this.sqlTopic);
            this.arrivalTopicPublisher = topicSession
                .createPublisher(this.arrivalTopic);
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
            if (this.sqlTopicPublisher != null) {
                this.sqlTopicPublisher.close();
                this.sqlTopicPublisher = null;
            }
            if (this.arrivalTopicPublisher != null) {
                this.arrivalTopicPublisher.close();
                this.arrivalTopicPublisher = null;
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
     *            Is the message object that the topic recieved.
     */
    public void onMessage(javax.jms.Message msg) {
        if (msg instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) msg;
            writeAndPublishBytesMessage(bytesMessage);
        }

    }

    /**
     * This method takes in a BytesMessage (JMS Message) and records it's
     * contents to storage and then republishes it to ruminate if it is a
     * metadata packet
     * 
     * @param bytesMessage
     */
    private void writeAndPublishBytesMessage(BytesMessage bytesMessage) {
        StringBuffer keyMessage = new StringBuffer();
        // This assumes that this byte array is in the form of the SSDS
        // specification
        long deviceID = -999999;
        long parentID = -999999;
        int packetType = -999999;
        long packetSubType = -999999;
        long dataDescriptionID = -999999;
        long dataDescriptionVersion = -999999;
        long timestampSeconds = -999999;
        long timestampNanoseconds = -999999;
        long sequenceNumber = -999999;
        int bufferLen = 1;
        byte[] bufferBytes = new byte[bufferLen];
        int bufferTwoLen = 1;
        byte[] bufferTwoBytes = new byte[bufferTwoLen];
        try {
            deviceID = bytesMessage.readLong();
            parentID = bytesMessage.readLong();
            packetType = bytesMessage.readInt();
            packetSubType = bytesMessage.readLong();
            dataDescriptionID = bytesMessage.readLong();
            dataDescriptionVersion = bytesMessage.readLong();
            timestampSeconds = bytesMessage.readLong();
            timestampNanoseconds = bytesMessage.readLong();
            sequenceNumber = bytesMessage.readLong();
            bufferLen = bytesMessage.readInt();
            bufferBytes = new byte[bufferLen];
            bytesMessage.readBytes(bufferBytes);
            bufferTwoLen = bytesMessage.readInt();
            bufferTwoBytes = new byte[bufferTwoLen];
            bytesMessage.readBytes(bufferTwoBytes);

            // Debugging stuff
            StringBuffer hexData = new StringBuffer();
            ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(
                bufferBytes);
            while (byteArrayIS.available() > 0) {
                hexData.append(Integer.toHexString(
                    (0xFF & byteArrayIS.read()) | 0x100).substring(1));
            }
            StringBuffer hexTwoData = new StringBuffer();
            ByteArrayInputStream byteTwoArrayIS = new ByteArrayInputStream(
                bufferTwoBytes);
            while (byteTwoArrayIS.available() > 0) {
                hexData.append(Integer.toHexString(
                    (0xFF & byteTwoArrayIS.read()) | 0x100).substring(1));
            }
            keyMessage.append("deviceID=" + deviceID + ",");
            keyMessage.append("parentID=" + parentID + ",");
            keyMessage.append("packetType=" + packetType + ",");
            keyMessage.append("packetSubType=" + packetSubType + ",");
            keyMessage.append("dataDescriptionID=" + dataDescriptionID + ",");
            keyMessage.append("dataDescriptionVersion="
                + dataDescriptionVersion + ",");
            keyMessage.append("timestampSeconds=" + timestampSeconds + ",");
            keyMessage.append("timestampNanoseconds=" + timestampNanoseconds
                + ",");
            keyMessage.append("sequenceNumber=" + sequenceNumber + ",");
            keyMessage.append("bufferLen=" + bufferLen + ",");
            keyMessage.append("bufferTwoLen=" + bufferTwoLen + ",");
            logger.debug("Got bytesMessage and will write to disk:"
                + "deviceID=" + deviceID + "," + "parentID=" + parentID + ","
                + "packetType=" + packetType + "," + "packetSubType="
                + packetSubType + "," + "dataDescriptionID="
                + dataDescriptionID + "," + "timestampSeconds="
                + timestampSeconds + "," + "timestampNanoseconds="
                + timestampNanoseconds + "," + "sequenceNumber="
                + sequenceNumber + "," + "bufferLen=" + bufferLen + ","
                + "bufferBytes(in hex)=" + hexData.toString() + "bufferTwoLen="
                + bufferTwoLen + "," + "bufferTwoBytes(in hex)="
                + hexTwoData.toString());
        } catch (JMSException e) {
            logger.error("JMSException caught: " + e.getMessage());
        }
        PacketOutput po = PacketOutputManager.getPacketOutput(deviceID,
            dataDescriptionID, packetSubType, parentID);
        try {
            po.writeBytesMessage(bytesMessage);
        } catch (IOException e1) {
            logger.error("IOException caught trying to write to disk: "
                + e1.getMessage());
        }
        // Republish all messages to the SQL ingest topic
        if (!this.publishingSetup) {
            logger.error("Publishing was not setup");
            this.setupPublishing();
        }
        try {
            sqlTopicPublisher.publish(bytesMessage);
        } catch (JMSException e2) {
            logger
                .error("JMSException caught while trying to publish the bytes message"
                    + e2.getMessage());
            this.publishingSetup = false;
        } catch (Exception e2) {
            logger.error("Exception caught trying to publish to SQL topic: "
                + e2.getMessage());
            this.publishingSetup = false;
        }

        // Now, republish a key message to the arrival topic
        try {
            TextMessage textMessage = topicSession.createTextMessage(keyMessage
                .toString());
            arrivalTopicPublisher.publish(textMessage);
        } catch (JMSException e3) {
            logger
                .error("JMSException caught while trying to publish the text message"
                    + e3.getMessage());
            this.publishingSetup = false;
        } catch (Exception e3) {
            logger
                .error("Exception caught trying to publish to Arrival topic: "
                    + e3.getMessage());
            this.publishingSetup = false;
        }

        // Now, republish metadata messages to the Ruminate topic
        if (packetType == 1) {
            logger
                .debug("bytesMessage was a metadata packet so ingest will republish");
            // Should be it, now republish the packet to the next step
            // Create a new message
            if (!this.publishingSetup) {
                logger.error("Publishing was not setup");
                this.setupPublishing();
            }
            try {
                topicPublisher.publish(bytesMessage);
            } catch (JMSException e2) {
                logger
                    .error("JMSException caught while trying to publish the bytes message"
                        + e2.getMessage());
                this.publishingSetup = false;
            }
        }
    } // End onMessage

} // End IngestMDB
