package test.moos.ssds.ingest;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

import javax.ejb.CreateException;
import javax.ejb.RemoveException;
import javax.naming.NamingException;

import junit.framework.TestCase;
import moos.ssds.io.SSDSDevicePacket;
import moos.ssds.io.util.PacketUtility;
import moos.ssds.jms.PublisherComponent;
import moos.ssds.services.data.SSDSByteArrayAccess;
import moos.ssds.services.data.SSDSByteArrayAccessHome;
import moos.ssds.services.data.SSDSByteArrayAccessUtil;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.mbari.siam.distributed.MetadataPacket;
import org.mbari.siam.operations.utils.ExportablePacket;

import test.moos.ssds.ClassPathHacker;

public class IngestMDBTest extends TestCase {

	/**
	 * A log4j logger
	 */
	private static Logger logger = Logger.getLogger(IngestMDBTest.class);

	/**
	 * This is the class to help with publishing
	 */
	private static PublisherComponent publisherComponent;

	public IngestMDBTest(String name) {
		super(name);
		// Add the base of the transmogrifier build files to the classpath
		try {
			ClassPathHacker.addFile(new File("build/ingest"));
			ClassPathHacker.addFile(new File("build/ingest-pub"));
		} catch (IOException e1) {
			logger.error("IOException caught trying to add the "
					+ "build/transmogrify directory to the class path"
					+ e1.getMessage());
		}

		Properties log4jProperties = new Properties();
		try {
			log4jProperties.load(this.getClass().getResourceAsStream(
					"/log4j.properties"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		PropertyConfigurator.configure(log4jProperties);

		// Grab the transmogrifier properties for the file
		Properties ingestProperties = new Properties();
		logger.debug("Constructor called ... going to "
				+ "try and read the transmogrifier properties file in ...");
		try {
			ingestProperties.load(this.getClass().getResourceAsStream(
					"/moos/ssds/ingest/ingest.properties"));
		} catch (Exception e) {
			logger.error("Exception trying to read in properties file: "
					+ e.getMessage()
					+ "\nThis could be due to the fact that you have "
					+ "not run 'ant -Dtarget=build' from the root of "
					+ "the project directory.  This needs to be done "
					+ "before these tests are run.");
		}

		// Create the publisher component to connect up to the topic that the
		// transmogrify MDB is listening to
		if (publisherComponent == null)
			publisherComponent = new PublisherComponent();
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * This method tests whether or not a message can be sent to the
	 * transmogrifyMDB input topic and then the transmogrify message was
	 * received on the IngestMDB topic. It also checks if the transmogrification
	 * went OK.
	 */
	public void testSendMessage() {

		// Create the output stream to help export the SIAM packet
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		// Create the ExportablePacket to use to wrap and export a SIAM packet
		ExportablePacket exportablePacket = new ExportablePacket();

		// MetadataPacket comes first
		MetadataPacket metadataPacket = new MetadataPacket(101, ("Cause")
				.getBytes(), ("Buffer bytes").getBytes());
		metadataPacket.setMetadataRef(0);
		metadataPacket.setParentId(100);
		metadataPacket.setRecordType(0);
		metadataPacket.setSequenceNo(1);
		Date metadataPacketDate = new Date();
		metadataPacket.setSystemTime(metadataPacketDate.getTime());

		// Wrap and export
		exportablePacket.wrapPacket(metadataPacket);
		try {
			exportablePacket.export(dos);
		} catch (IOException e) {
			assertTrue("IOException caught trying to export "
					+ "MetadataPacket to SIAM formatted byte array.", false);
		}

		// Convert those bytes to SSDS formatted bytes
		byte[] ssdsFormattedBytes = PacketUtility
				.convertSIAMByteArrayToVersion3SSDSByteArray(bos.toByteArray(),
						false, false, false, false);

		// Now publish the SSDS formatted byte array to the IngestMDB topic
		publisherComponent.publishBytes(ssdsFormattedBytes);

		// Wait for a bit or until the listener gets a message
		Date dateToStopWaiting = new Date(new Date().getTime() + 5000);
		while (new Date().before(dateToStopWaiting)) {
		}

		// Now let's construct the SSDSByteArrayEJB interface
		try {
			SSDSByteArrayAccessHome ssdsByteArrayAccessHome = SSDSByteArrayAccessUtil
					.getHome();
			SSDSByteArrayAccess ssdsByteArrayAccess = ssdsByteArrayAccessHome
					.create();

			// Set the device ID
			ssdsByteArrayAccess.setDeviceID(101);

			// Now try to search for the latest byte array
			ssdsByteArrayAccess.setLastNumberOfPackets(1);

			// Run the query
			try {
				ssdsByteArrayAccess.queryForData();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Now grab the byte array
			if (ssdsByteArrayAccess.hasMoreElements()) {
				byte[] ssdsVersion3ByteArrayWithVersion = ssdsByteArrayAccess
						.nextElement();

				// Convert to SSDS DevicePacket
				SSDSDevicePacket ssdsDevicePacket = PacketUtility
						.convertSSDSByteArrayToSSDSDevicePacket(
								ssdsVersion3ByteArrayWithVersion, true);
			}

			ssdsByteArrayAccess.remove();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CreateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
