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

import java.util.Properties;

import moos.ssds.jms.SubscriberComponent;

import org.apache.log4j.Logger;

public class UpdateBotRunner {

    public UpdateBotRunner() {
        // Read in the properties
        try {
            updateBotProperties.load(this.getClass().getResourceAsStream(
                "/moos/ssds/clients/updateBot/updateBot.properties"));
        } catch (Exception e) {
            logger.error("Exception trying to read in properties file: "
                + e.getMessage());
        }
        try {
            this.sleepTimeSeconds = Integer.parseInt(updateBotProperties
                .getProperty("client.updateBot.sleep.time.seconds"));
        } catch (NumberFormatException e1) {
            logger
                .error("Could not parse property to set sleep time in seconds ("
                    + updateBotProperties
                        .getProperty("client.updateBot.sleep.time.seconds")
                    + ") : " + e1.getMessage());
        }
        this.jmsHostName = updateBotProperties
            .getProperty("client.updateBot.subscriber.host.name");
        this.jmsTopicName = updateBotProperties
            .getProperty("client.updateBot.subscriber.topic.name");

        logger.debug("The following properties will be used:");
        logger.debug("client.updateBot.sleep.time.seconds="
            + this.sleepTimeSeconds);
    }

    /**
     * Return JMS subsriber information
     * 
     * @return
     */
    public String getJMSHostName() {
        return this.jmsHostName;
    }

    public String getJMSTopicName() {
        return this.jmsTopicName;
    }

    /**
     * Return the time to sleep
     * 
     * @return
     */
    public int getSleepTimeSeconds() {
        return this.sleepTimeSeconds;
    }

    /**
     * The main to fire it off
     * 
     * @param args
     */
    public static void main(String[] args) {

        logger.debug("UpdateBotRunner starting up ...");
        // Construct an UpdateBotRunner
        UpdateBotRunner runner = new UpdateBotRunner();

        // Construct a JMS listener
        SubscriberComponent subscriberComponent = new SubscriberComponent(
            runner.getJMSHostName(), runner.jmsTopicName,
            new UpdateBotListener());
        logger
            .debug("Registered UpdateBotListener as a subscriber to the JSM topic");

        while (true) {
            logger.debug("Creating new UpdateBot as part of normal cycle");
            UpdateBot updateBot = new UpdateBot();
            logger
                .debug("UpdateBot created, now will fire off to crawl all deployments");
            // Run the updates
            updateBot.crawlAllParentlessDeployments();
            logger
                .debug("Done crawling all deployments, will now go to sleep for "
                    + runner.getSleepTimeSeconds() + " seconds");
            try {
                Thread.sleep(runner.getSleepTimeSeconds() * 1000);
            } catch (InterruptedException e) {
                logger.error("InterruptedException thrown trying to sleep: "
                    + e.getMessage());
            }
        }
    } // End main()

    /**
     * This is the properties to read in some configuration settings
     */
    private Properties updateBotProperties = new Properties();

    /**
     * JMS Properties
     */
    private String jmsTopicName = null;
    private String jmsHostName = null;

    /**
     * Sleep time (defaults to 4 hours)
     */
    private int sleepTimeSeconds = 14400;

    /**
     * A log4j logger
     */
    static Logger logger = Logger.getLogger(UpdateBotRunner.class);
}
