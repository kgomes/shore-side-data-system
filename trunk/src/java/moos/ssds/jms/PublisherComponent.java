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
package moos.ssds.jms;

import java.io.Serializable;
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
 * <code>PublisherComponent</code> provides an easy to use class that handles
 * JMS publish-subscribe messaging. A <code>PublisherComponent</code> can
 * publish to multiple topics. The goal of this is to provide messsaging without
 * requiring the developer to use JMS api's
 * </p>
 * @author : $Author: kgomes $
 * @version : $Revision: 1.10.2.1 $
 */

public class PublisherComponent {

    /**
     * This is a constructor that starts a connection to a JMS server
     * 
     * @param topicName
     *            is a String that represents the JNDI name of the topic that
     *            this PublisherComponent will be sending messages to
     */
    public PublisherComponent(String topicName) {
        // Call default constructor
        this();
        // Set the topic name to the incoming string
        setTopicname(topicName);
    } // End constructor

    /**
     * This is the constructor that takes in a topic name to publish to and a
     * hostname to publish to.
     * 
     * @param topicName
     *            This is the name of the topic that the JMS messages will be
     *            published to. If this is null, it will use the default topic
     *            name specified in a jms.properties file.
     * @param hostName
     *            This is the fully qualified name of the host where the
     *            messages will be published to. If this is null, it will use
     *            the default hostname found in a jndi.properties file.
     */
    public PublisherComponent(String topicName, String hostName) {
        // Call the default constructor
        this();
        // Set the local hostname
        this.jndiHostName = hostName;
        // Call setTopicName
        setTopicname(topicName);
    }

    /**
     * This is the default constructor
     */
    public PublisherComponent() {
        // Create a publisher component with a null name and use
        // the properties from the jms.properties file since no
        // topic was defined.
        boolean jmsPropsLoaded = false;
        try {
            jmsProps.load(this.getClass().getResourceAsStream(
                "/moos/ssds/jms/jms.properties"));
            jmsPropsLoaded = true;
        } catch (Exception e) {
            logger.error("Could not get jms properties file");
            jmsPropsLoaded = false;
        }
        if (jmsPropsLoaded) {
            // Set the local default topic name
            defaultTopicname = jmsProps.getProperty("ssds.jms.topic");
            logger.debug("Default Constructor called: defaultTopicName =  "
                + defaultTopicname);
            setTopicname(defaultTopicname);
        } else {
            logger.error("The jmsProperties could not be loaded");
        }
    } // End default constructor

    /**
     * This method starts the connection to the JMS server. Can also be used to
     * restart a lost connection.
     */
    private void start() {
        // First clean up everything from the past by calling stop
        this.stop();

        // Set the connected value to true
        logger.debug("Start called");
        connected = true;

        // Create an InitialContext (using the jndi.properties file that
        // needs to be in the classpath somewhere). If there is a local
        // messaging hostname defined, override the jndi.properties one
        // with that one.
        try {
            jndiContext = new InitialContext();
            if ((this.jndiHostName != null) && (!this.jndiHostName.equals(""))) {
                jndiContext.removeFromEnvironment(Context.PROVIDER_URL);
                jndiContext.addToEnvironment(Context.PROVIDER_URL,
                    this.jndiHostName + ":1099");
            }
            logger.debug("JNDI environment = " + jndiContext.getEnvironment());
        } catch (NamingException ne) {
            logger.error("!!--> A naming exception was caught while trying "
                + "to get an initial context: " + ne.getMessage());
            // Set connected to false and bail
            connected = false;
            return;
        } catch (Exception e) {
            logger.error("!!--> An unknown exception was caught while trying "
                + "to get an initial context: " + e.getMessage());
            // Set connected to false and bail
            connected = false;
            return;
        }

        // Now try to grab the connection factory for topics from the initial
        // context
        try {
            topicConnectionFactory = (TopicConnectionFactory) jndiContext
                .lookup(jmsProps
                    .getProperty("ssds.jms.topic.connection.factory.jndi.name"));
        } catch (NamingException ne) {
            logger.error("!!--> "
                + "A naming exception was caught while trying "
                + "to get the topicConnectionFactory: " + ne.getMessage());
            connected = false;
            return;
        } catch (Exception e) {
            logger.error("!!--> "
                + "An unknown exception was caught while trying "
                + "to get the topicConnectionFactory: " + e.getMessage());
            connected = false;
            return;
        }

        // Try to get the topic connection
        try {
            topicConnection = topicConnectionFactory.createTopicConnection();
        } catch (JMSException jmse) {
            logger.error("!!--> " + "A JMS exception was caught while trying "
                + "to get the topicConnection: " + jmse.getMessage());
            connected = false;
            return;
        } catch (Exception e) {
            logger.error("!!--> "
                + "An unknown exception was caught while trying "
                + "to get the topicConnection: " + e.getMessage());
            connected = false;
            return;
        }

        // Get the Topic
        try {
            topic = (Topic) jndiContext.lookup(this.topicname);
        } catch (NamingException ne) {
            logger.error("!!--> Could not get topic in setTopicName and a "
                + "NamingException was caught: " + ne.getMessage());
            connected = false;
            return;
        } catch (Exception e) {
            logger.error("!!--> Could not get topic in setTopicName and an "
                + "unknown exception was caught: " + e.getMessage());
            connected = false;
            return;
        }

        // Get the topic session
        try {
            topicSession = topicConnection.createTopicSession(false,
                Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException jmse) {
            logger.error("!!--> " + "A JMS exception was caught while trying "
                + "to get the topicSession: " + jmse.getMessage());
            connected = false;
            return;
        } catch (Exception e) {
            logger.error("!!--> "
                + "An unknown exception was caught while trying "
                + "to get the topicSession: " + e.getMessage());
            connected = false;
            return;
        }

        // Now start the connection
        try {
            topicConnection.start();
        } catch (JMSException jmse) {
            logger.error("!!--> " + "A JMS exception was caught while trying "
                + "to start the topic connection: " + jmse.getMessage());
            connected = false;
            return;
        } catch (Exception e) {
            logger.error("!!--> "
                + "An unknown exception was caught while trying "
                + "to start the topic connection: " + e.getMessage());
            connected = false;
            return;
        }

        // Try to get a publisher
        try {
            topicPublisher = topicSession.createPublisher(topic);
        } catch (JMSException jmse) {
            logger
                .error("!!--> Could not get topicPublisher in setTopicName and a "
                    + "JMSException was caught: " + jmse.getMessage());
            connected = false;
            return;
        } catch (Exception e) {
            logger
                .error("!!--> Could not get topicPublisher in setTopicName and an "
                    + "unknown exception was caught: " + e.getMessage());
            connected = false;
            return;
        }

        // Now try to create a message
        try {
            message = topicSession.createObjectMessage();
        } catch (JMSException jmse) {
            logger.error("!!--> " + "A JMS exception was caught while trying "
                + "to create a message: " + jmse.getMessage());
            connected = false;
            return;
        } catch (Exception e) {
            logger.error("!!--> "
                + "An unknown exception was caught while trying "
                + "to create a message: " + e.getMessage());
            try {
                if (topicPublisher != null) {
                    topicPublisher.close();
                }
                if (topicConnection != null) {
                    topicConnection.stop();
                }
                if (topicSession != null) {
                    topicSession.close();
                }
                if (topicConnection != null) {
                    topicConnection.close();
                }
            } catch (JMSException jmse) {}
            connected = false;
            return;
        }
        // If here return as everything should be OK and up and running
        return;
    } // End start

    /**
     * This method gets rid of all the topic related objects and the initial
     * context.
     */
    private void stop() {
        logger.debug("Stop called");

        // NULL out all the objects
        message = null;
        topic = null;

        // First close the TopicPublisher
        if (topicPublisher != null) {
            try {
                topicPublisher.close();
            } catch (JMSException e) {
                logger
                    .error("JMS Exception caught while closing topic publisher in stop():"
                        + e.getMessage());
            } catch (Exception e) {
                logger
                    .error("Unknown Exception caught while closing topic publisher in stop():"
                        + e.getMessage());
            }
            topicPublisher = null;
        }

        // Now stop the TopicConnection
        if (topicConnection != null) {
            try {
                topicConnection.stop();
            } catch (JMSException e) {
                logger
                    .error("JMS Exception caught while stopping the topicConnection:"
                        + e.getMessage());
            } catch (Exception e) {
                logger
                    .error("Unknown Exception caught while stopping the topicConnection:"
                        + e.getMessage());
            }
        }

        // Now close the TopicSession
        if (topicSession != null) {
            try {
                topicSession.close();
            } catch (JMSException e) {
                logger
                    .error("JMS Exception caught while closing topic session in stop():"
                        + e.getMessage());
            } catch (Exception e) {
                logger
                    .error("Unknown Exception caught while closing topic session in stop():"
                        + e.getMessage());
            }
            topicSession = null;
        }

        // Now close the TopicConnection
        if (topicConnection != null) {
            try {
                topicConnection.close();
            } catch (JMSException e) {
                logger
                    .error("JMS Exception caught while closing topic connection in stop():"
                        + e.getMessage());
            } catch (Exception e) {
                logger
                    .error("Unknown Exception caught while closing topic connection in stop():"
                        + e.getMessage());
            }
            topicConnection = null;
        }

        // Null out the connection factory
        topicConnectionFactory = null;

        // If the naming context is not null, close and null it
        if (jndiContext != null) {
            try {
                jndiContext.close();
            } catch (NamingException e) {
                logger
                    .error("JMS Exception caught while closing jndi context in stop():"
                        + e.getMessage());
            } catch (Exception e) {
                logger
                    .error("Unknown Exception caught while closing jndi context in stop():"
                        + e.getMessage());
            }
            // Null it out
            jndiContext = null;
        }

        // Set flag to say that the publisher is not connected
        logger.debug("Everything should be stopped, closed and cleaned now");
        connected = false;

    } // End stop

    /**
     * This is the method that the client will call when it is ready to publish
     * 
     * @deprecated This was deprecated in favor of publishBytes which takes in
     *             an array of bytes that meet the SSDS incoming structure
     *             specification. This method was used to publish Java objects,
     *             but was version fragile, so byte array were ued instead.
     * @param obj
     *            This is the serialized object that will be published to the
     *            JMS topic
     * @return A boolean to indicate if the publish was successful (true) or not
     *         (false)
     */
    public boolean publish(Serializable obj) {
        // Print out debug message
        if (obj instanceof DevicePacket) {
            DevicePacket dp = (DevicePacket) obj;
            logger.debug("Published: deviceID=" + dp.sourceID() + ":seqNumber="
                + dp.sequenceNo() + ":systemTime=" + dp.systemTime());
        } else {
            logger.debug("Publish called but not with DevicePacket");
        }

        // Initialize success boolean to false
        boolean success = false;

        // Check to see if this PublisherComponent is connected and if it is
        // not, try to reconnect
        if (!connected) {
            logger.debug("Not connected, so going to reconnect");
            this.setTopicname(this.topicname);
        }
        // Check to see if connected again and if so, try to publish
        if (connected) {
            try {
                // Send the serializable object
                message = topicSession.createObjectMessage();
                message.setObject(obj);
                topicPublisher.publish(message);
                success = true;
            } catch (JMSException e) {
                logger.error("JMS Exception caught while publishing: "
                    + e.getMessage());
                this.connected = false;
            } catch (Exception e) {
                logger.error("Unknown Exception caught while publishing: "
                    + e.getMessage());
                this.connected = false;
            }
        }
        return success;
    } // End publish

    /**
     * This method publishes the given byte array as a ByteMessage to the SSDS
     * topic
     * 
     * @param bytes
     *            the bytes to publish to SSDS as a BytesMessage
     * @return
     */
    public boolean publishBytes(byte[] bytes) {
        // Set the flag to indicate it did not publish successfully to start
        boolean success = false;
        // Check for connection first
        if (!connected) {
            logger.debug("Not connected, so going to reconnect");
            this.setTopicname(this.topicname);
        }
        // Check to see if connected again and if so, try to publish
        if (connected) {
            try {
                // Send the serializable object
                bytesMessage = topicSession.createBytesMessage();
                bytesMessage.writeBytes(bytes);
                topicPublisher.publish(bytesMessage);
                success = true;
            } catch (JMSException e) {
                logger.error("JMS Exception caught while publishing: "
                    + e.getMessage());
                this.connected = false;
            } catch (Exception e) {
                logger.error("Unknown Exception caught while publishing: "
                    + e.getMessage());
                this.connected = false;
            }
        }
        return success;
    }

    /**
     * This function specifies the topic name to publish to. Calling this method
     * will stop and restart the publisher with a new topic name.
     * 
     * @param topicName
     *            The name of the JMS topic name to publish to
     */
    public void setTopicname(String topicName) {
        // Set the topic name
        logger.debug("New topic name will be assigned to " + topicName);
        if ((topicName == null) || (topicName.compareTo("") == 0)) {
            logger
                .debug("Topic name was null or empty so using default topicname of "
                    + defaultTopicname);
            this.topicname = defaultTopicname;
        } else {
            this.topicname = topicName;
        }

        // Close the old connection
        if (topicPublisher != null) {
            try {
                topicPublisher.close();
            } catch (JMSException jmse) {
                logger.error("Could not close the topicPublisher: "
                    + jmse.getMessage());
            }
        }

        // Now that the topic name has changed, restart the connections
        start();

        // If we are not connected, print this out to the logger
        if (!connected) {
            logger
                .error("Could not setTopicName because connection was not established");
        }

    } // End setTopicname

    /**
     * Getter method to retrieve the name of the topic that this publisher is
     * sending its messagest to
     * 
     * @return String that is the topic name of the topic this
     *         PublisherComponent is sending message to
     */
    public String getTopicname() {
        return topicname;
    }

    /**
     * Return connection status
     * 
     * @return
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * This is the finalize method that is run by the garbage collector just
     * before destroying the object. It is used to clean up connections, etc.
     */
    public void finalize() {
        // Finalize the parent then call stop on this object
        try {
            logger.debug("finalize method called ...");
            super.finalize();
            stop();
            logger.debug("finalize finished.");
        } catch (Throwable t) {
            logger.debug("Could not complete finalize correctly ...");
        }
    } // End finalize

    /**
     * This is the name of the topic that will used to publish packets to
     */
    private String topicname;

    /**
     * This is the default topic name that is read from the properties file
     */
    private String defaultTopicname;

    /**
     * This is the JNDI Context that will be used (Naming Service) to locate the
     * appropriate remote classes to use for publishing messages.
     */
    private Context jndiContext;

    /**
     * This is the hostname where the naming service is located
     */
    private String jndiHostName = null;

    /**
     * This is the JMS TopicConnectionFactory that is used to get a topic that
     * can then be used to publish messages to
     */
    private TopicConnectionFactory topicConnectionFactory;

    /**
     * This is the connection to the topic that the messages will be published
     * to
     */
    private TopicConnection topicConnection;

    /**
     * This is the JMS topic that will be used for publishing
     */
    private Topic topic;

    /**
     * This is a session that the publishing of messages will be run in.
     */
    private TopicSession topicSession;

    /**
     * This is the topic publisher that is actually used to send messages to the
     * topic
     */
    private TopicPublisher topicPublisher;

    /**
     * This is a JMS message that is used to send to the topic
     */
    private ObjectMessage message;

    /**
     * This is a BytesMessage that can be used to publish an array of bytes.
     */
    private BytesMessage bytesMessage;

    /**
     * This is a boolean to track if this PublisherComponent is currently
     * connected to a topic session
     */
    private boolean connected = false;

    /**
     * A properties file used for reading in jms information for the publisher
     * to utilize when working
     */
    private Properties jmsProps = new Properties();

    /**
     * This is a Log4JLogger that is used to log information to
     */
    static Logger logger = Logger.getLogger(PublisherComponent.class);
} // End PublisherComponent
