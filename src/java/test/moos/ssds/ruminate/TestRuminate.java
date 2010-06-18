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
package test.moos.ssds.ruminate;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import junit.framework.TestCase;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.jms.PublisherComponent;
import moos.ssds.metadata.DataProducer;
import moos.ssds.services.metadata.DataProducerAccess;
import moos.ssds.services.metadata.DataProducerAccessHome;
import moos.ssds.services.metadata.DataProducerAccessUtil;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.mbari.siam.distributed.Exportable;

/**
 * @author kgomes
 */
public class TestRuminate extends TestCase {

	public TestRuminate(String name) {
		super(name);
		// Configure the logger
		BasicConfigurator.configure();
		logger.setLevel(Level.DEBUG);
		logger.addAppender(new ConsoleAppender(new PatternLayout(
				"%d %-5p [%c %M %L] %m%n")));
		// Try to see if we can override the variables
		try {
			deviceID = Long.parseLong(ruminateDeviceIDString);
		} catch (NumberFormatException e) {
		}
		try {
			parentID = Long.parseLong(ruminateParentDeviceIDString);
		} catch (NumberFormatException e) {
		}
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
	}

	public void testMetadataPublish() {

		logger.debug("Here we go, let's create and publish a metadata "
				+ "packet to SSDS and see if we can read back all the "
				+ "same metadata.  The packet will be published to "
				+ this.deviceID + " that is child of " + this.parentID);

		// Create the data buffer (XML)
		StringBuffer sb = new StringBuffer();
		InputStream is = null;
		if (this.xmlFilePathString != null) {
			xmlFile = new File(this.xmlFilePathString);
			try {
				is = new FileInputStream(xmlFile);
			} catch (FileNotFoundException e) {
				logger.error("FileNotFoundException: " + e.getMessage());
			}
		} else {
			// TODO kgomes put some default test file here
		}
		try {
			if (is != null) {
				while (is.available() > 0) {
					byte[] readByte = new byte[1];
					is.read(readByte);
					sb.append(new String(readByte));
				}
			}
		} catch (IOException e) {
			logger.error("IOException caught: " + e.getMessage());
		}
		logger.debug("StringBuffer (xml) is : \n" + sb.toString());
		// Now publish the bytes
		pc.publishBytes(this.createByteArray(Exportable.EX_DEVICEPACKET, 0,
				this.deviceID, new Date().getTime(), 1, new Date().getTime(),
				this.parentID, 0, Exportable.EX_METADATAPACKET, 0, sb
						.toString().getBytes(), sb.toString().getBytes()));

		pc = null;

		System.gc();
		// Now wait for a bit and make sure we can find head data producer
		try {
			logger.debug("Sleeping ...");
			Thread.sleep(10000);
			logger.debug("Awake now!");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Now read back the deployment by name
		DataProducerAccessHome dpaccessHome = null;
		DataProducerAccess dpaccess = null;
		try {
			logger.debug("Let's setup the access interfaces");
			dpaccessHome = DataProducerAccessUtil.getHome();
			dpaccess = dpaccessHome.create();
			logger.debug("OK, they are set");
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (NamingException e) {
			assertTrue("NamingException was thrown: " + e.getMessage(), false);
		} catch (CreateException e) {
			assertTrue("CreateException was thrown: " + e.getMessage(), false);
		}
		Collection dpsByName = null;
		try {
			logger.debug("Let's get all the dataproducers with name "
					+ this.headDataProducerName);
			dpsByName = dpaccess.findByName(this.headDataProducerName, true,
					null, null, false);
		} catch (RemoteException e) {
			assertTrue("RemoteException was thrown: " + e.getMessage(), false);
		} catch (MetadataAccessException e) {
			assertTrue("MetadataAccessException was thrown: " + e.getMessage(),
					false);
		}
		assertNotNull("The dpsByName collection should not be null", dpsByName);
		assertTrue("There should be one or more results in the colletion",
				dpsByName.size() >= 1);
		Iterator dpsByNameIter = dpsByName.iterator();
		DataProducer persistentDataProducer = (DataProducer) dpsByNameIter
				.next();
		// TODO Walk the trees to do some interesting comparisons
		// Remove it
		try {
			dpaccess.deepDelete(persistentDataProducer);
		} catch (RemoteException e) {
			logger.error("RemoteException caught trying to deep delete:"
					+ e.getMessage());
		} catch (MetadataAccessException e) {
			logger
					.error("MetadataAccessException caught trying to deep delete:"
							+ e.getMessage());
		}

	}

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

	// The "device" these are coming from
	long deviceID = -999L;

	// The "parent" id
	long parentID = -1111L;

	// The PublisherComponent used in the test
	private PublisherComponent pc = null;

	// XML file to read in and publish
	private File xmlFile = null;

	// These are configuration information for the tests
	private String ruminateDeviceIDString = System
			.getProperty("test.ruminate.device.id");
	private String ruminateParentDeviceIDString = System
			.getProperty("test.ruminate.parent.id");
	private String jmsTopicName = System
			.getProperty("test.ruminate.topic.name");
	private String jmsHostNameLong = System
			.getProperty("test.ruminate.host.name.long");
	private String xmlFilePathString = System
			.getProperty("test.ruminate.path.to.xml.file");
	private String headDataProducerName = System
			.getProperty("test.ruminate.head.dataproducer.name");

	// A Logger
	static Logger logger = Logger.getLogger(TestRuminate.class);
}