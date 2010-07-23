package test.moos.ssds.transmogrify;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.log4j.Logger;

/**
 * This class is used to test the transmogrify component. It is simply a message
 * handler that takes in a message and assigns it to a local variable which a
 * client can check.
 * 
 * @author kgomes
 * 
 */
public class TransmogrifyMDBTestMessageListener implements MessageListener {

	// A logger
	private static Logger logger = Logger
			.getLogger(TransmogrifyMDBTestMessageListener.class);

	// The local BytesMessage that will be assigned to an incoming message
	private BytesMessage currentBytesMessage = null;

	/**
	 * A method to return the most recent BytesMessage received by this handler
	 * 
	 * @return
	 */
	public BytesMessage getCurrentBytesMessage() {
		return currentBytesMessage;
	}

	/**
	 * A method to clear out the most recent message
	 */
	public void clearCurrentBytesMessage() {
		currentBytesMessage = null;
	}

	/**
	 * The method that will be called when a message arrives
	 */
	public void onMessage(Message message) {
		logger.debug("Message received!");
		// Makes sure it is a bytes message before assigning it
		if (message instanceof BytesMessage) {
			logger.debug("It was a BytesMessage so will store locally");
			currentBytesMessage = (BytesMessage) message;
		}
	}

}
