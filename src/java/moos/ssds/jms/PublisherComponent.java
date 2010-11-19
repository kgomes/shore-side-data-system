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
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.mbari.siam.distributed.DevicePacket;

/**
 * <p>
 * <code>PublisherComponent</code> provides an easy to use class that handles
 * JMS publish-subscribe messaging. A <code>PublisherComponent</code> can
 * publish to multiple topics. The goal of this is to provide messaging without
 * requiring the developer to use JMS api's
 * </p>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.10.2.1 $
 */

public class PublisherComponent {

	/**
	 * This is a Log4JLogger that is used to log information to
	 */
	static Logger logger = Logger.getLogger(PublisherComponent.class);

	/**
	 * A properties file used for reading in jms information for the publisher
	 * to utilize when working
	 */
	private Properties jmsProps = new Properties();

	/**
	 * This is the name of the topic that will used to publish packets to
	 */
	private String topicname;

	/**
	 * This is the default topic name that is read from the properties file
	 */
	private String defaultTopicname;

	/**
	 * This is the hostname where the naming service is located
	 */
	private String jndiHostName = null;

	/**
	 * This is the connection that the messages will be published to
	 */
	private Connection connection;

	/**
	 * This is a session that the publishing of messages will be run in.
	 */
	private Session session;

	/**
	 * This is the JMS MessageProducer for publishing
	 */
	private MessageProducer messageProducer;

	/**
	 * This is a boolean to track if this PublisherComponent is currently
	 * connected to a topic session
	 */
	private boolean connected = false;

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
			logger.debug("Topic name was null or empty so "
					+ "using default topicname of " + defaultTopicname);
			this.topicname = defaultTopicname;
		} else {
			this.topicname = topicName;
		}

		// Restart the connection
		restart();

		// If we are not connected, print this out to the logger
		if (!connected) {
			logger.error("Could not setTopicName because "
					+ "connection was not established");
		}

	} // End setTopicname

	/**
	 * This method cleans up the connection
	 */
	private void stop() {
		logger.debug("Stop called");

		// Close the connection
		if (connection != null) {
			try {
				logger.debug("Connection stats before closing:\nClientID = "
						+ connection.getClientID() + "\nJMSProviderName = "
						+ connection.getMetaData().getJMSProviderName());
				connection.stop();
			} catch (JMSException e) {
				logger.error("JMS Exception caught while stopping "
						+ "the connection:" + e.getMessage());
			} catch (Exception e) {
				logger.error("Unknown Exception caught while stopping "
						+ "the connection:" + e.getMessage());
			}
		}

		// Set flag to say that the publisher is not connected
		logger.debug("Everything should be stopped, closed and cleaned now");
		connected = false;

	} // End stop

	/**
	 * This method starts the connection to the JMS server.
	 */
	private void start() {
		// Set the connected value to true
		logger.debug("Start called");
		connected = true;

		// Create an InitialContext (using the jndi.properties file that
		// needs to be in the classpath somewhere). If there is a local
		// messaging hostname defined, override the jndi.properties one
		// with that one.
		Context jndiContext = null;
		try {
			jndiContext = new InitialContext();
			if ((this.jndiHostName != null) && (!this.jndiHostName.equals(""))) {
				jndiContext.removeFromEnvironment(Context.PROVIDER_URL);
				jndiContext.addToEnvironment(Context.PROVIDER_URL,
						this.jndiHostName + ":1099");
			} else {
				this.jndiHostName = ((String) jndiContext.getEnvironment().get(
						Context.PROVIDER_URL)).split(":")[0];
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

		// Now try to grab the connection factory from the initial context
		ConnectionFactory connectionFactory = null;
		if (jndiContext != null) {
			try {
				connectionFactory = (ConnectionFactory) jndiContext
						.lookup(jmsProps
								.getProperty("ssds.jms.topic.connection.factory.jndi.name"));
			} catch (NamingException ne) {
				logger.error("!!--> "
						+ "A naming exception was caught while trying "
						+ "to get the ConnectionFactory: " + ne.getMessage());
				connected = false;
				return;
			} catch (Exception e) {
				logger.error("!!--> "
						+ "An unknown exception was caught while trying "
						+ "to get the ConnectionFactory: " + e.getMessage());
				connected = false;
				return;
			}
		}

		// Look up the destination
		Destination publishingDestination = null;
		if (connectionFactory != null) {
			// Get the destination
			try {
				publishingDestination = (Destination) jndiContext
						.lookup(this.topicname);
			} catch (NamingException ne) {
				logger.error("!!--> Could not get destination "
						+ "and a NamingException was caught: "
						+ ne.getMessage());
				connected = false;
				return;
			} catch (Exception e) {
				logger.error("!!--> Could not get destination "
						+ "and an unknown exception was caught: "
						+ e.getMessage());
				connected = false;
				return;
			}
		} else {
			connected = false;
			return;
		}

		// Try to get a connection
		if (publishingDestination != null) {
			try {
				connection = connectionFactory.createConnection();
			} catch (JMSException jmse) {
				logger.error("!!--> A JMS exception was caught while trying "
						+ "to get the connection: " + jmse.getMessage());
				connected = false;
				return;
			} catch (Exception e) {
				logger.error("!!--> An unknown exception was caught while "
						+ "trying to get the connection: " + e.getMessage());
				connected = false;
				return;
			}
		} else {
			connected = false;
			return;
		}

		// Get the session
		if (connection != null) {
			try {
				session = connection.createSession(false,
						Session.AUTO_ACKNOWLEDGE);
			} catch (JMSException jmse) {
				logger.error("!!--> A JMS exception was caught while trying "
						+ "to get the session: " + jmse.getMessage());
				connected = false;
				return;
			} catch (Exception e) {
				logger.error("!!--> An unknown exception was caught while "
						+ "trying to get the session: " + e.getMessage());
				connected = false;
				return;
			}
		} else {
			connected = false;
			return;
		}

		// Try to setup the producer
		if (session != null) {
			try {
				messageProducer = session.createProducer(publishingDestination);
			} catch (JMSException jmse) {
				logger.error("!!--> Could not create producer and a "
						+ "JMSException was caught: " + jmse.getMessage());
				connected = false;
				return;
			} catch (Exception e) {
				logger.error("!!--> Could not create producer and an "
						+ "unknown exception was caught: " + e.getMessage());
				connected = false;
				return;
			}
		} else {
			connected = false;
			return;
		}

		// Now start the connection
		try {
			connection.start();
		} catch (JMSException jmse) {
			logger.error("!!--> A JMS exception was caught while trying "
					+ "to start the connection: " + jmse.getMessage());
			connected = false;
			return;
		} catch (Exception e) {
			logger.error("!!--> "
					+ "An unknown exception was caught while trying "
					+ "to start the connection: " + e.getMessage());
			connected = false;
			return;
		}

		// Close the initial context
		try {
			if (jndiContext != null)
				jndiContext.close();
		} catch (NamingException e) {
			logger.error("NamingException caught while trying "
					+ "to close the naming context: " + e.getMessage());
		}

		// If here return as everything should be OK and up and running
		return;

	} // End start

	/**
	 * This method simply calls stop and then start
	 */
	public void restart() {
		// Stop things
		stop();

		// Start it up again
		start();
	}

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
				ObjectMessage message = session.createObjectMessage();
				message.setObject(obj);
				messageProducer.send(message);
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
				BytesMessage bytesMessage = session.createBytesMessage();
				bytesMessage.writeBytes(bytes);
				messageProducer.send(bytesMessage);
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
	 * Getter method to retrieve the name of the topic that this publisher is
	 * sending its messages to
	 * 
	 * @return String that is the topic name of the topic this
	 *         PublisherComponent is sending message to
	 */
	public String getTopicname() {
		return topicname;
	}

	public String getJndiHostName() {
		return jndiHostName;
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
	 * The method that closes everything up
	 */
	public void close() {
		// Call the stop method
		this.stop();
	}

} // End PublisherComponent
