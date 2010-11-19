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

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * <p>
 * SubscriberComponent provides an easy to use class that handles JMS
 * publish-subscribe messaging. The goal of this is to provide messaging without
 * requiring the developer to use JMS api's
 * </p>
 * <p>
 * Here's an example of code using a <code>SubscriberComponent</code>
 * </p>
 * 
 * <pre>
 * import moos.ssds.jms.*;
 * 
 * public class Subscriber1 {
 * 
 * 	SubscriberComponent sc;
 * 	SensorDataPacketListener listener = new SensorDataPacketListener();
 * 
 * 	public Subscriber1(String topicName) {
 * 		sc = new SubscriberComponent(topicName, listener);
 * 	}
 * 
 * 	public static void main(String[] args) {
 * 		try {
 * 			Subscriber1 sub = new Subscriber1(args[0]);
 * 
 * 		} catch (Throwable e) {
 * 			e.printStackTrace();
 * 		}
 * 	}
 * }
 * </pre>
 * 
 * <p>
 * Note: I tried overriding the finalize() method to close the
 * <code>TopicConnection</code> when the class was no longer used. However, the
 * garbage collector would call the finalize() method at seemingly random times.
 * Causing the program to cease. Moving the close() call out of the finalize()
 * method stopped this problem.
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.4.2.1 $
 */
public class SubscriberComponent {

	/**
	 * This is the log4j logger used to log activity in a SubscriberComponent
	 */
	private Logger logger = Logger.getLogger(SubscriberComponent.class);

	/**
	 * A properties file used for reading in jms information for the subscriber
	 * to utilize when working
	 */
	private Properties jmsProps = new Properties();

	/**
	 * A boolean that indicates the connection status of the subscriber
	 */
	private boolean connected = false;

	/**
	 * The topic name that this SubscriberComponent will subscribe to
	 */
	protected String destinationName = null;

	/**
	 * This is the name of the host to subscribe to
	 */
	protected String hostName = null;

	/**
	 * This is the Connection to the JMS server
	 */
	protected Connection connection = null;

	/**
	 * This is the session that will be used to listen and process messages
	 * coming from JMS
	 */
	protected Session session = null;

	/**
	 * This is the MessageConsumer that will be the link between the JMS
	 * destination and the responsible listener
	 */
	protected MessageConsumer messageConsumer = null;

	/**
	 * This is a local copy of the message listener used to process messages
	 */
	private MessageListener messageListener;

	/**
	 * The constructor to that can be used if you know the hostname, destination
	 * name and the MessageListener that will handle the messages
	 * 
	 * @param hostName
	 * @param topicName
	 * @param messageListener
	 */
	public SubscriberComponent(String hostName, String topicName,
			MessageListener messageListener) {
		this(topicName, messageListener);
		// Assign the hostname and call the other constructor
		this.setHostName(hostName);
	}

	/**
	 * Constructor which takes in a destination name and a MessageListener
	 * 
	 * @param destinationName
	 *            The name of the Destination to subscribe to. For JBoss this
	 *            needs to be "topic/someTopic" whereas most other J2EE servers
	 *            would use "someTopic"
	 * @param messageListener
	 *            The <code>MessageListener</code> to be used to handle the
	 *            message.
	 */
	public SubscriberComponent(String destinationName,
			MessageListener messageListener) {
		// Now read the jms properties to find the JNDI name of the
		// ConnectionFactory
		try {
			jmsProps.load(this.getClass().getResourceAsStream(
					"/moos/ssds/jms/jms.properties"));
		} catch (Exception e) {
			logger.debug("Could not get jms properties file");
		}
		// Now call the subscribe method
		subscribe(destinationName, messageListener);
	}

	/**
	 * This method assigns the hostname that you will subscribe to. This gives
	 * the client a way to override any jndi.properties file that was found.
	 * After assigning the hostname, it restarts the subscription connection.
	 * 
	 * @param hostName
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
		this.subscribe(this.destinationName, this.messageListener);
	}

	/**
	 * This method returns the boolean that indicates the connection status of
	 * the subscriber
	 * 
	 * @return
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Accessor for the MessageListener
	 * 
	 * @return The class used to process the messages received by this
	 *         <code>SubscriberComponent</code>
	 */
	public MessageListener getMessageListener() {
		return messageListener;
	}

	/**
	 * Accessor for the topic name
	 * 
	 * @return The topic name that this <code>SubscriberComponent</code> is
	 *         subscribed to.
	 */
	public String getTopicname() {
		return destinationName;
	}

	/**
	 * Closes the topic connection. Should be called when the class is no longer
	 * used.
	 */
	public void close() {
		logger.debug("close called");
		// Check null and close TopicConnection
		if (connection != null) {
			try {
				logger.debug("Connection stats before closing:\nClientID = "
						+ connection.getClientID() + "\nJMSProviderName = "
						+ connection.getMetaData().getJMSProviderName());
				connection.close();
			} catch (JMSException e) {
				logger.error("JMSException caught trying "
						+ "to close the connection: " + e.getMessage());
			} catch (Exception e) {
				logger.error("Exception caught trying to "
						+ "close the connection: " + e.getMessage());
			}
			connection = null;
		}
		connected = false;
	}

	/**
	 * Looks up the topic via JNDI and subscribes to it.
	 * 
	 * @param destinationName
	 *            The name of the destination to subscribe to.
	 * @param messageListener
	 *            The <code>MessageListener</code> to be used to handle the
	 *            message.
	 */
	private void subscribe(String destinationName,
			MessageListener messageListener) {

		// Set the local variables to those incoming
		this.destinationName = destinationName;
		this.messageListener = messageListener;

		// Close the existing connection
		this.close();

		// Set the connected flag to false
		connected = true;
		logger.debug("SubscriberComponent is " + "subscribing to "
				+ destinationName);

		// Get initialContexct
		logger.debug("Looking up InitialContext");
		Context jndiContext = null;
		try {
			jndiContext = new InitialContext();

			logger.debug("Should have initial context and it looks like:"
					+ jndiContext.getEnvironment());
			// Check to see if a hostname is specified
			if ((this.hostName != null) && (!this.hostName.equals(""))) {
				jndiContext.removeFromEnvironment(Context.PROVIDER_URL);
				jndiContext.addToEnvironment(Context.PROVIDER_URL, hostName
						+ ":1099");
				logger.debug("Changed subscriber host, now JNDI looks like:"
						+ jndiContext.getEnvironment());
			}
		} catch (NamingException e) {
			logger.error("NamingException caught trying "
					+ "to get InitialContext: " + e.getMessage());
			connected = false;
			return;
		}

		// If the naming context was found, look up the connection factory
		logger.debug("Looking up the ConnectionFactory");
		ConnectionFactory connectionFactory = null;
		if (jndiContext != null) {
			try {
				connectionFactory = (ConnectionFactory) jndiContext
						.lookup(jmsProps
								.getProperty("ssds.jms.topic.connection.factory.jndi.name"));
			} catch (NamingException e) {
				logger.error("NamingException caught trying "
						+ "to get ConnectionFactory: " + e.getMessage());
				connected = false;
				return;
			}
		}

		// Grab the Destination to subscribe to
		logger.debug("Looking up the destination with name " + destinationName);
		Destination consumerDestination = null;
		if (connectionFactory != null) {
			try {
				consumerDestination = (Destination) jndiContext
						.lookup(destinationName);
			} catch (NamingException e) {
				logger.error("NamingException caught trying "
						+ "to get the destination: " + e.getMessage());
				connected = false;
				return;
			}
		} else {
			connected = false;
			return;
		}

		// Now create the connection
		logger.debug("Going to create the connection.");
		if (consumerDestination != null) {
			try {
				connection = connectionFactory.createConnection();
			} catch (JMSException e) {
				logger.error("JMSException caught trying "
						+ "to create the connection: " + e.getMessage());
				connected = false;
				return;
			}
		} else {
			connected = false;
			return;
		}

		// Create the session
		logger.debug("Going to create the session");
		if (consumerDestination != null) {
			try {
				session = connection.createSession(false,
						Session.AUTO_ACKNOWLEDGE);
			} catch (JMSException e) {
				logger.error("JMSException caught trying "
						+ "to create the connection: " + e.getMessage());
				connected = false;
				return;
			}
		}

		// Now create the consumer
		logger.debug("Going the create the consumer");
		if (session != null) {
			try {
				messageConsumer = session.createConsumer(consumerDestination);
			} catch (JMSException e) {
				logger.error("JMSException caught trying "
						+ "to create the message consumer: " + e.getMessage());
				connected = false;
				return;
			}
		} else {
			connected = false;
			return;
		}

		// If the message consumer was all good, connect up the listeners
		logger.debug("Going to set the listener on the consumer");
		if (messageConsumer != null) {
			try {
				messageConsumer.setMessageListener(messageListener);
			} catch (JMSException e) {
				logger.error("JMSException caught trying "
						+ "to set the message listener: " + e.getMessage());
				connected = false;
				return;
			}
		} else {
			connected = false;
			return;
		}

		// Now start up the connection and listen for packets
		logger.debug("Going to start the connection.");
		try {
			if (connected)
				connection.start();
		} catch (JMSException e) {
			logger.error("JMSException caught trying "
					+ "to set start the connection: " + e.getMessage());
			connected = false;
			return;
		}
		logger.debug("should have started");

		// Now close the InitialContext to make sure it goes away
		try {
			if (jndiContext != null)
				jndiContext.close();
		} catch (NamingException e) {
			logger.debug("NamingException caught trying to "
					+ "close the JNDI Context: " + e.getMessage());
		}

		// Now return
		return;

	}

}