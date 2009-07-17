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
package test.moos.ssds.jms;

import java.io.IOException;
import java.util.Date;

import junit.framework.TestCase;
import moos.ssds.jms.PublisherComponent;
import moos.ssds.transmogrify.SSDSDevicePacket;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * JUnit TestCase.
 * 
 * @testfamily JUnit
 * @testkind testcase
 * @testsetup Default TestCase
 * @testedclass moos.ssds.jms.PublisherComponent
 */
public class TestIngest extends TestCase {

	/**
	 * Constructs a test case with the given name.
	 */
	public TestIngest(String name) {
		super(name);
		// Configure the logger
		BasicConfigurator.configure();
		logger.setLevel(Level.DEBUG);
		logger.addAppender(new ConsoleAppender(new PatternLayout(
				"%d %-5p [%c %M %L] %m%n")));
		// Try to see if we can override the variables
		try {
			numberOfTestThreads = Integer.parseInt(numberOfTestThreadsString);
		} catch (NumberFormatException e) {
		}
		try {
			bufferSizeLimit = Integer.parseInt(bufferSizeLimitString);
		} catch (NumberFormatException e) {
		}
		try {
			numberOfPackets = Integer.parseInt(numberOfPacketsString);
		} catch (NumberFormatException e) {
		}
		try {
			startingDeviceID = Long.parseLong(startingDeviceIDString);
		} catch (NumberFormatException e) {
		}
		try {
			startingParentID = Long.parseLong(startParentDeviceIDString);
		} catch (NumberFormatException e) {
		}
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
		logger.setLevel(Level.DEBUG);
		logger.debug("setupCalled");
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
		// Write your code here
	}

	public void testSSDSDevicePacketSend() {
		// Create a publisher component
		PublisherComponent pc = new PublisherComponent();

		// The device ID that is doin' the sendin'
		long deviceID = 101;

		// The parent that it is plugged in to
		long parentID = 100;

		// Create the payload to send as bytes
		String messagePayload = "This is the payload of the test packet";

		// The date the "data" was collected
		Date packetDate = new Date();

		// Create a new SensorDataPacket
		SSDSDevicePacket packetToSend = new SSDSDevicePacket(101,
				messagePayload.getBytes().length);

		// Set the packet type to data
		// 0 = Metadata
		// 1 = Data
		// 2 = Message
		// 3 = Status
		packetToSend.setPacketType(1);
		
		// Assign the time
		packetToSend.setSystemTime(packetDate.getTime());

		// Set the parent
		packetToSend.setPlatformID(parentID);

		// Set the metadataref number
		packetToSend.setMetadataRef(0);

		// Set the sequence number
		packetToSend.setSequenceNo(1);

		// Set the payload
		packetToSend.setDataBuffer(messagePayload.getBytes());

		// Set the record type
		packetToSend.setRecordType(1);

		try {
			pc.publishBytes(SSDSDevicePacket
					.convertToPublishableByteArray(packetToSend));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This tests, creates some SensorDataPackets, sends them in and then
	 * verifies that they were stored correctly
	 */
	public void testPacketPublishing() {
		logger.debug("Starting testPacketPublishing with parameters:");
		logger.debug("numberOfTestThreads = " + numberOfTestThreads);
		logger.debug("bufferSizeLimit = " + bufferSizeLimit);
		logger.debug("numberOfPackets = " + numberOfPackets);
		logger.debug("startDeviceID = " + startingDeviceID);
		logger.debug("startParentDeviceID = " + startingParentID);

		// Create an array where the threads will be put
		IngestPubThread[] testThread = new IngestPubThread[this.numberOfTestThreads];
		// Now loop through and create the threads and start them
		for (int i = 0; i < this.numberOfTestThreads; i++) {
			IngestPubThread niceThreads = new IngestPubThread(
					this.numberOfPackets, this.bufferSizeLimit,
					(this.startingDeviceID + i), (this.startingParentID + i));
			// Now start it
			new Thread(niceThreads).start();
			// Now add it to the array
			testThread[i] = niceThreads;
			logger.debug("Test thread " + (i + 1) + " created.");
		}
		// Now keep looping through until all threads have finished their tests
		boolean allTestComplete = false;
		while (!allTestComplete) {
			// Sleep for a second
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			logger.debug("Going to check tests");
			// Check all tests status
			allTestComplete = true;
			for (int k = 0; k < this.numberOfTestThreads; k++) {
				if (!testThread[k].isTestComplete()) {
					allTestComplete = false;
				}
			}
			if (allTestComplete) {
				logger.debug("Looks like we are all done!");
			} else {
				logger.debug("Tests are still chunking along");
			}
		}
		// Now check results
		for (int m = 0; m < this.numberOfTestThreads; m++) {
			logger.debug("Checking assertion on test " + m);
			assertTrue("Test should have passed OK", testThread[m]
					.isTestsPassed());
		}
	}

	// Configuration variables.

	// The number of thread to run in this test
	int numberOfTestThreads = 10;
	// The size limit of buffer for each thread to use to generate data
	int bufferSizeLimit = 50;
	// The number of packets each thread will generate
	int numberOfPackets = 100;
	// This is the starting deviceID
	long startingDeviceID = -999L;
	// The "parent" id
	long startingParentID = -1111L;

	// Some variables passed as parameters to override the native variables
	private String numberOfTestThreadsString = System
			.getProperty("test.jms.number.of.test.threads");
	private String bufferSizeLimitString = System
			.getProperty("test.jms.buffer.size.limit");
	private String numberOfPacketsString = System
			.getProperty("test.jms.number.of.packets");
	private String startingDeviceIDString = System
			.getProperty("test.jms.starting.device.id");
	private String startParentDeviceIDString = System
			.getProperty("test.jms.starting.parent.id");

	// A Logger
	static Logger logger = Logger.getLogger(TestIngest.class);
}