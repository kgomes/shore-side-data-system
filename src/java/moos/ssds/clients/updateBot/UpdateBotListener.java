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
package moos.ssds.clients.updateBot;

import java.util.Collection;
import java.util.Iterator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.util.ObjectBuilder;

import org.apache.log4j.Logger;

public class UpdateBotListener implements MessageListener {

    /**
     * This is the call back method that will be called by the Subscriber
     * Component when it gets a message
     */
    public void onMessage(Message message) {

        logger.debug("MessageRecieved.");
        // Should be a TextMessage
        String messagePayloadText = null;
        if (message instanceof TextMessage) {
            logger.debug("The message is a TextMessage so will look for XML.");
            try {
                TextMessage textMessage = (TextMessage) message;
                messagePayloadText = textMessage.getText();
                logger.debug("TextMessage Payload is: \n" + messagePayloadText);
            } catch (JMSException e) {
                logger.error("JMSException trying to read payload: "
                    + e.getMessage());
            }
        }

        // If the payload was found and starts with "<?xml", throw it at
        // ObjectBuilder
        if ((messagePayloadText != null)
            && (messagePayloadText.contains("<?xml"))) {
            logger.debug("Found XML tag, will throw at ObjectBuilder");
            ObjectBuilder ob = new ObjectBuilder(messagePayloadText);

            logger.debug("Will now unmarshall");
            try {
                if (ob != null)
                    ob.unmarshal();
            } catch (Exception e) {
                logger.error("Exception caught trying to unmarshall");
            }

            // Now check for head object to see if a DataProducer was found and
            // if so, fire off UpdateBot
            if (ob != null) {
                logger
                    .debug("OK, ObjectBuild worked, will look at head object");
                Collection allHeadObjects = ob.listAll();
                logger.debug("OK, grabbed all objects");
                if (allHeadObjects != null) {
                    Iterator iter = allHeadObjects.iterator();
                    Object headObject = iter.next();
                    logger.debug("OK, grabbed first head object");
                    if ((headObject != null)
                        && (headObject instanceof DataProducer)) {
                        logger.debug("The headObject is a DataProducer so "
                            + "will build a new UpdateBot");
                        UpdateBot updateBot = new UpdateBot(
                            ((DataProducer) headObject).getName());
                        logger.debug("OK, firing off UpdateBot");
                        if (updateBot != null)
                            updateBot.crawlAllParentlessDeployments();
                        logger.debug("OK, UpdateBot done, will destroy "
                            + "and go back to listening");
                    } else {
                        logger
                            .debug("The head object was not found or is not a "
                                + "DataProducer so no UpdateBot was run");
                    }
                }
            } else {
                logger
                    .error("ObjectBuilder is null ... something is not right!");
            }
        }
    }

    static Logger logger = Logger.getLogger(UpdateBotListener.class);
}
