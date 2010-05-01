package test.moos.ssds.transmogrify;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

public class TransmogrifyMDBTestMessageListener implements MessageListener {

	private BytesMessage currentBytesMessage = null;

	public BytesMessage getCurrentBytesMessage() {
		return currentBytesMessage;
	}

	public void clearCurrentBytesMessage() {
		currentBytesMessage = null;
	}

	public void onMessage(Message message) {
		if (message instanceof BytesMessage) {
			currentBytesMessage = (BytesMessage) message;
		}
	}

}
