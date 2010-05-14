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

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * <p>
 * SubscriberComponent provides an easy to use class that handles JMS
 * publish-subscribe messaging. The goal of this is to povide messsaging without
 * requiring the developer to use JMS api's
 * </p>
 * <p>
 * Here's an example of code using a <code>SubscriberComponent</code>
 * </p>
 * 
 * <pre>
 * import ssds.portal.*;
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
 * method stopeed this problem.
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.4.2.1 $
 */
public class SubscriberComponent {

	/**
	 * The topic name that this SubscriberComponent will subscribe to
	 */
	protected String topicname;

	/**
	 * This the the naming Context that will be used to look up the JMS related
	 * servcies. It is going to use the first jndi.properties file it finds in
	 * its classpath to determine which JNDI service to utilize.
	 */
	protected Context jndiContext;

	/**
	 * This is the name of the host to subscribe to
	 */
	protected String hostName = null;

	/**
	 * This is the JMS TopicConnectionFactory that is used to get the topic
	 * connection for this subscriber
	 */
	protected TopicConnectionFactory topicConnectionFactory;

	/**
	 * This is the TopicSession that will be used to listen and process messages
	 * coming from JMS
	 */
	protected TopicSession topicSession;

	/**
	 * This is the TopicConnection to the JMS server
	 */
	protected TopicConnection topicConnection;

	/**
	 * A dummy message
	 */
	protected Message message;

	/**
	 * This is the topic that this component subscribes to
	 */
	private Topic topic;

	/**
	 * This is the TopicSubscriber obtained from the JMS server
	 */
	protected TopicSubscriber topicSubscriber;

	/**
	 * This is a local copy of the message listener used to process messages
	 */
	private MessageListener messageListener;

	/**
	 * This is the log4j logger used to log activity in a SubscriberComponent
	 */
	private Logger subscriberLogger = Logger
			.getLogger(SubscriberComponent.class);

	/**
	 * A properties file used for reading in jms information for the subscriber
	 * to utilize when working
	 */
	Properties jmsProps = new Properties();

	/**
	 * The constructor to help specify the hostname
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
	 * Constructor
	 * 
	 * @param topicName
	 *            The name of the topic to subscribe to. For JBoss this needs to
	 *            be "topic/someTopic" whereas most other J2EE servers would use
	 *            "someTopic"
	 * @param messageListener
	 *            The <code>MessageListener</code> to be used to handle the
	 *            message.
	 */
	public SubscriberComponent(String topicName, MessageListener messageListener) {
		// Now try to read the jms properties to find out what the JNDI name of
		// the TopicConnectionFactory is
		try {
			jmsProps.load(this.getClass().getResourceAsStream(
					"/moos/ssds/jms/jms.properties"));
		} catch (Exception e) {
			subscriberLogger.debug("Could not get jms properties file");
		}
		// Now call the subscribe method
		subscribe(topicName, messageListener);
	}

	/**
	 * This method assigns the hostname that you will subscribe to. This gives
	 * the client a way to override any jndi.properties file that was found.
	 * 
	 * @param hostName
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
		this.subscribe(this.topicname, this.messageListener);
	}

	/**
	 * This method will attempt to connect to the JMS topic using the currently
	 * defined topic names and message listeners. If either are null (or an
	 * empty topic name), it will throw an IllegalArgumentException
	 */
	private void subscribe() throws IllegalArgumentException {
		if ((this.topicname == null) || (this.topicname.equals(""))) {
			throw new IllegalArgumentException("The topic name is empty so no "
					+ "connection could be established");
		}
		if (this.messageListener == null) {
			throw new IllegalArgumentException(
					"The message listener has not be created "
							+ "so no connection was established.");
		}
		this.subscribe(this.topicname, this.messageListener);
	}

	/**
	 * Looks up the topic via JNDI and subscribes to it.
	 * 
	 * @param topicName
	 *            The name of the topic to subscribe to.
	 * @param messageListener
	 *            The <code>MessageListener</code> to be used to handle the
	 *            message.
	 */
	private void subscribe(String topicName, MessageListener messageListener) {

		this.close();
		// Set the connected flag to false
		boolean connected = false;
		subscriberLogger.debug("SubscriberComponent is subscribing to "
				+ topicName);

		// Now try to connect up everything
		try {
			// Set the local variables to those incoming
			this.topicname = topicName;
			this.messageListener = messageListener;
			subscriberLogger
					.debug("Starting JNDI and JMS in SubscriberComponent");

			// Create a JNDI API Context Object if none exists
			jndiContext = new InitialContext();

			subscriberLogger
					.debug("Should have initial context and it looks like:"
							+ jndiContext.getEnvironment());
			// Check to see if a hostname is specified
			if ((this.hostName != null) && (!this.hostName.equals(""))) {
				jndiContext.removeFromEnvironment(Context.PROVIDER_URL);
				jndiContext.addToEnvironment(Context.PROVIDER_URL, hostName
						+ ":1099");
				subscriberLogger
						.debug("Changed subscriber host, now JNDI looks like:"
								+ jndiContext.getEnvironment());
			}

			// Loop through and try to get the TopicConnectionFactory from the
			// JMS server. Keep looping until the TopicConnectionFactory is
			// found
			while (!connected) {
				try {
					topicConnectionFactory = (TopicConnectionFactory) jndiContext
							.lookup(jmsProps
									.getProperty("ssds.jms.topic.connection.factory.jndi.name"));
				} catch (NamingException ne) {
					subscriberLogger
							.error("Could not get to initial context due to naming exception, will wait and try again. Message = "
									+ ne.getMessage());
					try {
						Thread.sleep(10000);
					} catch (Exception te) {
						subscriberLogger.warn("Could not put thread to sleep");
					}
					continue;
				} catch (Exception ex) {
					subscriberLogger
							.error("Could not get to initial context due to unknown exception, will wait and try again. Message = "
									+ ex.getMessage());
					try {
						Thread.sleep(10000);
					} catch (Exception te) {
						subscriberLogger.warn("Could not put thread to sleep");
					}
					continue;
				}
				// Now set connected flag to true
				connected = true;
			}
			subscriberLogger.debug("should have topicConnectionFactory");

			// Now create the TopicConnection
			topicConnection = topicConnectionFactory.createTopicConnection();
			subscriberLogger.debug("Should have topicConnection");

			// Set the exception callback listener
			topicConnection.setExceptionListener(new JmsExceptionListener());

			// Now create a TopicSession
			topicSession = topicConnection.createTopicSession(false,
					TopicSession.AUTO_ACKNOWLEDGE);
			subscriberLogger.debug("should have topicSession");

			// Now create a dummy message
			message = topicSession.createObjectMessage();
			subscriberLogger.debug("should have create message");

			// Lookup the topic and get the appropriate publisher
			topic = (Topic) jndiContext.lookup(topicName);
			subscriberLogger.debug("should have topic");

			topicSubscriber = topicSession.createSubscriber(topic);
			subscriberLogger.debug("should have topicSubscriber");

			// Now set the message listener to that that was handed off in
			// the constructor
			topicSubscriber.setMessageListener(messageListener);
			subscriberLogger.debug("should have set message listener");

			// Now start up the connection and listen for packets
			topicConnection.start();
			subscriberLogger.debug("should have started");
		} catch (NamingException e) {
			subscriberLogger.debug("Error publishing to JMS. JNDI API lookup"
					+ " failed: " + e.toString());
			e.printStackTrace();
		} catch (JMSException e) {
			subscriberLogger
					.debug("Error connecting to JMS: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			subscriberLogger.debug("Unknown exception caught: "
					+ e.getMessage());
			e.printStackTrace();
		}

		// Now close the InitialContext to make sure it goes away
		try {
			if (jndiContext != null)
				jndiContext.close();
		} catch (NamingException e) {
			subscriberLogger
					.debug("NamingException caught trying to close the JNDI Context: "
							+ e.getMessage());
		}

	}

	/**
	 * Accessor for the MessageListener
	 * 
	 * @return The class used to process the messages recieved by this <code>
     * SubscriberComponent</code>
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
		return topicname;
	}

	/**
	 * Closes the topic connection. Should be called when the class is no longer
	 * used.
	 */
	public void close() {
		subscriberLogger.debug("close called");
		try {
			// Null out the local message
			message = null;
			// Null out the Topic
			topic = null;
			// Check null and close TopicSubscriber
			if (topicSubscriber != null) {
				topicSubscriber.close();
				topicSubscriber = null;
			}
			// Check null and close TopicSession
			if (topicSession != null) {
				topicSession.close();
				topicSession = null;
			}
			// Check null and close TopicConnection
			if (topicConnection != null) {
				topicConnection.close();
				topicConnection = null;
			}
			// Null out TopicConnection Factory
			topicConnectionFactory = null;
			// Check null and close JNDI Context
			if (jndiContext != null) {
				jndiContext.close();
				jndiContext = null;
			}
		} catch (JMSException e) {
			subscriberLogger.debug("Error with JMS connection on cleanup: "
					+ e.getMessage());
		} catch (NamingException e) {
			subscriberLogger.debug("Error with JNDI API on cleanup: "
					+ e.getMessage());
		} catch (Exception e) {
			subscriberLogger.debug("Unknown exception on close: "
					+ e.getMessage());
		}

	}

	/**
	 * This is a listener that will be used to trap JMSExceptions from the
	 * server and then try to re-establish the connection
	 * 
	 * @author kgomes
	 */
	private class JmsExceptionListener implements ExceptionListener {

		public void onException(JMSException e) {
			subscriberLogger.warn("Connection caught an exception so "
					+ "I will try to restart the connection (Message = "
					+ e.getMessage() + ")");
			// First close everything
			close();
			// Now try to restart
			try {
				subscribe();
			} catch (IllegalArgumentException e1) {
				subscriberLogger
						.error("IllegalArgumentException caught trying to re-subscribe: "
								+ e1.getMessage());
			}
		}
	}

}