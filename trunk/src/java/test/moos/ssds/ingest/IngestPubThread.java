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
package test.moos.ssds.ingest;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

import moos.ssds.io.SSDSDevicePacket;
import moos.ssds.jms.PublisherComponent;
import moos.ssds.services.data.SQLDataStreamRawDataAccess;
import moos.ssds.services.data.SQLDataStreamRawDataAccessEJB;
import moos.ssds.services.data.SQLDataStreamRawDataAccessHome;
import moos.ssds.services.data.SQLDataStreamRawDataAccessUtil;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mbari.siam.distributed.Exportable;

/**
 * @author kgomes
 */
public class IngestPubThread implements Runnable {

	/**
	 * The constructor
	 * 
	 * @param numberOfPackets
	 */
	public IngestPubThread(int numberOfPackets, int bufferSizeLimit,
			long deviceID, long parentID) {

		logger.setLevel(Level.DEBUG);
		// Set the number of packets
		this.numberOfPackets = numberOfPackets;

		// Set buffer size
		this.bufferSizeLimit = bufferSizeLimit;

		// Set device/parent stuff
		this.deviceID = deviceID;
		this.parentID = parentID;

		// Setup flags
		this.testComplete = false;
		this.testsPassed = true;

		// Create the PublisherComponent
		if (this.jmsTopicName != null) {
			if (this.jmsHostNameLong != null) {
				pc = new PublisherComponent(this.jmsTopicName,
						this.jmsHostNameLong);
			} else {
				pc = new PublisherComponent(this.jmsTopicName);
			}
		} else {
			pc = new PublisherComponent();
		}

		if (secondsToSleepBeforeReadBackString != null) {
			try {
				secondsToSleepBeforeReadBack = Integer
						.parseInt(secondsToSleepBeforeReadBackString);
			} catch (NumberFormatException e) {
			}
		}
		logger.debug("IngestPubThread created with:\nnumberOfPackets = "
				+ this.numberOfPackets + "\n");
		logger.debug("bufferSizeLimit = " + this.bufferSizeLimit + "\n");
		logger.debug("deviceID = " + this.deviceID + "\n");
		logger.debug("parentID = " + this.parentID + "\n");
		logger.debug("jmsTopicName = " + this.jmsTopicName + "\n");
		logger.debug("jmsHostNameLong = " + this.jmsHostNameLong + "\n");
		logger.debug("PublisherComponent = " + this.pc + "\n");
		logger.debug("secondsToSleepBeforeReadBackString = "
				+ this.secondsToSleepBeforeReadBack + "\n");

	}

	public void run() {

		logger.debug("Here we go, publishing from device " + this.deviceID
				+ " that is child of " + this.parentID);
		// So let's loop through and publish numberOfPackets packets
		Date[] dates = new Date[numberOfPackets];
		String[] data = new String[numberOfPackets];
		TreeMap returnedData = new TreeMap();
		Random random = new Random(new Date().getTime());
		long lasttime = 0L;
		long startTime = 0L;
		long endTime = 0L;
		for (int i = 0; i < numberOfPackets; i++) {
			// First let's create the data by grabbing a random
			// buffer size (with limits), creating a new byte
			// array and populating with random bytes.
			int bufferSize = random.nextInt(this.bufferSizeLimit);
			// Make is something if it is really small
			if (bufferSize < 10)
				bufferSize = 10;
			byte[] dataBuffer = new byte[bufferSize];
			random.nextBytes(dataBuffer);

			// Convert to string for testing
			String dataInString = new String(dataBuffer);
			// Now store it for comparison
			data[i] = dataInString;

			// Create a timestamp
			Date currentDate = new Date();
			// Make sure it is distinct and add to the array
			dates[i] = currentDate;
			if (i > 0) {
				if (dates[i].getTime() <= lasttime) {
					dates[i].setTime(lasttime + 1);
				}
			}
			lasttime = dates[i].getTime();
			// Grab the first date for query later
			if (startTime == 0L)
				startTime = lasttime;

			// Now publish the bytes
			pc.publishBytes(this.createByteArray(Exportable.EX_DEVICEPACKET, 0,
					this.deviceID, currentDate.getTime(), i + 1, 0,
					this.parentID, 1, Exportable.EX_SENSORDATAPACKET, 0,
					dataBuffer, new byte[0]));
		}
		endTime = new Date().getTime();

		logger.debug("OK, device " + this.deviceID + " (child of "
				+ this.parentID
				+ ") has finished publishing, will sleep for a bit");

		// Now all the data should be published, we should be able to open the
		// service to read back data and find the data we just sent
		SQLDataStreamRawDataAccessHome sqlServiceHome = null;
		SQLDataStreamRawDataAccess sqlService = null;

		try {
			sqlServiceHome = SQLDataStreamRawDataAccessUtil.getHome();
			sqlService = sqlServiceHome.create();
		} catch (Throwable e) {
			logger
					.error("Caught throwable trying to get SQL service interface: "
							+ e.getMessage());
			testsPassed = false;
			testComplete = true;
			return;
		}

		// Sleep for thirty seconds or so
		try {
			Thread.sleep(secondsToSleepBeforeReadBack * 1000);
		} catch (InterruptedException e1) {
		}

		// Now try to read back all the packets that you just published
		logger.debug("Gathering results for device " + this.deviceID);
		try {
			returnedData = sqlService.getSortedRawData(new Long(this.deviceID),
					new Long(this.parentID), null, null, null, null, null,
					null, null, null, null, new Long(startTime / 1000),
					new Long(endTime / 1000), null, null, null, null, null,
					null, null, null, null, null, null,
					SQLDataStreamRawDataAccessEJB.BY_TIMESTAMP, true);
		} catch (Throwable e) {
			logger
					.error("Caught throwable trying to read the data back from the service: "
							+ e.getMessage());
			testsPassed = false;
			testComplete = true;
			return;
		}

		// Now loop through the time stamps and make sure the data matches
		if (returnedData == null) {
			logger.error("The returned data was null, should not be");
			testsPassed = false;
			testComplete = true;
			return;
		}
		if (returnedData.size() != numberOfPackets) {
			logger.error("The returned data for device " + deviceID + " had "
					+ returnedData.size()
					+ " packets, but there should have been "
					+ this.numberOfPackets + " of them");
			testsPassed = false;
			testComplete = true;
			return;
		}
		for (int i = 0; i < numberOfPackets; i++) {
			// Make sure the timestamp was found
			boolean keyFound = returnedData.containsKey(new Long(dates[i]
					.getTime()));
			if (!keyFound) {
				logger.error("Date " + dates[i]
						+ " was not found as key for a packet from device "
						+ this.deviceID);
				testsPassed = false;
				testComplete = true;
				return;
			}

			// Check data as well, by grabbing the collection of packets
			Collection packets = (Collection) returnedData.get(new Long(
					dates[i].getTime()));
			// The size should only be one, so grab it
			Iterator packetIter = packets.iterator();
			SSDSDevicePacket ssdsdp = (SSDSDevicePacket) packetIter.next();

			// String returnedDataString = (String) returnedData.get(new Long(
			// dates[i].getTime()));
			if (!(data[i].equals(new String(ssdsdp.getDataBuffer())))) {
				logger.error("Data string for device " + this.deviceID
						+ " did not match (" + data[i] + " != "
						+ new String(ssdsdp.getDataBuffer()));
				testsPassed = false;
				testComplete = true;
				return;
			}
		}

		logger.debug("Test for device " + this.deviceID + " is complete");
		testComplete = true;
		return;
	}

	/**
	 * This creates a SIAM formatted byte array
	 * 
	 * @param deviceStreamID
	 * @param devicePacketVersion
	 * @param sourceID
	 * @param timestamp
	 * @param sequenceNumber
	 * @param metadataRefNumber
	 * @param parentID
	 * @param recordType
	 * @param subclassStreamID
	 * @param subclassVersion
	 * @param payloadOne
	 * @param payloadTwo
	 * @return
	 */
	private byte[] createByteArray(short deviceStreamID,
			long devicePacketVersion, long sourceID, long timestamp,
			long sequenceNumber, long metadataRefNumber, long parentID,
			long recordType, short subclassStreamID, long subclassVersion,
			byte[] payloadOne, byte[] payloadTwo) {
		ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
		DataOutputStream dataOS = new DataOutputStream(byteOS);
		try {
			// Write the device stream id
			dataOS.writeShort(deviceStreamID);
			// Write the serial version ID
			dataOS.writeLong(devicePacketVersion);
			// Now write the source ID (device ID)
			dataOS.writeLong(sourceID);
			// Now write the timestamp
			dataOS.writeLong(timestamp);
			// Now write the sequence number
			dataOS.writeLong(sequenceNumber);
			// Now write the metadataRef Number
			dataOS.writeLong(metadataRefNumber);
			// Now write the parent ID
			dataOS.writeLong(parentID);
			// Now write the record type
			dataOS.writeLong(recordType);
			// Write the subclass stream id
			dataOS.writeShort(subclassStreamID);
			// Now write the subclass version id
			dataOS.writeLong(subclassVersion);
			// Now write the DataLen
			dataOS.writeInt(payloadOne.length);
			// Now write the bytes
			dataOS.write(payloadOne);
			if (subclassStreamID == Exportable.EX_METADATAPACKET) {
				// Now write the second length
				dataOS.writeInt(payloadTwo.length);
				// Now write the second payload
				dataOS.write(payloadTwo);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return byteOS.toByteArray();
	}

	/**
	 * @return Returns the testComplete.
	 */
	public boolean isTestComplete() {
		return testComplete;
	}

	/**
	 * @return Returns the testsPassed.
	 */
	public boolean isTestsPassed() {
		return testsPassed;
	}

	// The boolean to check if test passed
	boolean testsPassed = false;

	// A boolean to check progress of test
	boolean testComplete = false;

	// The number of packet to publish
	int numberOfPackets = 0;

	// The "device" these are coming from
	long deviceID = -999L;

	// The "parent" id
	long parentID = -1111L;

	// The limit to the size of the buffer
	int bufferSizeLimit = 0;

	int secondsToSleepBeforeReadBack = 30;

	// The PublisherComponent used in the test
	private PublisherComponent pc = null;

	// These are configuration information for the tests
	private String jmsTopicName = System.getProperty("test.jms.topic.name");
	private String jmsHostNameLong = System
			.getProperty("test.jms.host.name.long");
	private String secondsToSleepBeforeReadBackString = System
			.getProperty("test.jms.seconds.to.sleep.before.readback");

	// A Logger
	static Logger logger = Logger.getLogger(IngestPubThread.class);
}