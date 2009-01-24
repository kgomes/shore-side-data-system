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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import junit.framework.TestCase;

import moos.ssds.jms.PublisherComponent;

import org.apache.log4j.Logger;
import org.mbari.isi.interfaces.MetadataPacket;
import org.mbari.isi.interfaces.SensorDataPacket;

import ucar.nc2.NCdump;

/**
 * This test is responsible for testing the watchdog class that is
 * responsible for keeping the NetCDF files in synch with the 
 * data stream files.  It is basically responsible for making
 * sure there are NetCDF agents active for data streams that have
 * enough information to be converted to NetCDF files.
 * 
 * In order to really test this class, the following cases need
 * to be tested:
 * 
 * Case 1
 * --------------------------------------------------------------------------
 * Initial conditions:
 * - There are no data stream files and no
 *   XML files describing states of NetCDFAgents.
 * - There is no metadata in the database to
 *   describe any data that will appear in 
 *   the test.
 * 
 * Test steps:
 * - Metadata is sent that describes data that will
 *   be following the metadata.
 * - No data is sent yet.
 * 
 * Final conditions:
 * - There should be a metadata data stream storage
 *   that has appeared, but no data stream for the
 *   records described in the metadata.
 * - Metadata should be stored in the SSDS metadata
 *   database.
 * - No NetCDFAgent is created because there is
 *   no data available yet.
 * 
 * Case 2
 * --------------------------------------------------------------------------
 * Initial conditions:
 * - A metadata packet has been sent describing the data to follow.
 * - The metadata is in the SSDS metadata database.
 * - No data has been sent
 * - No NetCDFAgent is active yet because no data exists
 * 
 * Test steps:
 * - Send a burst of data packets for the records described by the
 *   metadata in the database.
 * 
 * Final conditions:
 * - A data stream file should be created containing the data that
 *   was sent
 * - A netcdf agent should be active that points to the data stream file
 * 
 * NOTE: The condition of the netcdf agents resultant NetCDF file is
 * not tested here as that is a part of the NetCDFAgent test
 *
 * Case 3
 * --------------------------------------------------------------------------
 * Initial conditions:
 * - A metadata packet has been sent describing the data to follow.
 * - The metadata is in the SSDS metadata database.
 * - A burst of data has been sent, but no further data.
 * - NetCDFAgent is active for data stream
 * 
 * Test steps:
 * - Let timeout period expire for NetCDFAgent (this means that no more data
 *   was sent for a specified period)
 * 
 * Final conditions:
 * - NetCDFAgent pointing to the data stream should be inactive
 *   and removed from the list of NetCDFAgents
 * 
 * Case 4
 * --------------------------------------------------------------------------
 * Initial conditions:
 * - A metadata packet has been sent describing the data to follow.
 * - The metadata is in the SSDS metadata database.
 * - Data has been sent.
 * - NetCDFAgent is not active due to data not being sent recently.
 * 
 * Test steps:
 * - Send another burst of data
 * 
 * Final conditions:
 * - NetCDFAgent pointing to the data is active.
 * 
 * Case 5
 * --------------------------------------------------------------------------
 * Intial conditions:
 * - A metadata packet has been sent describing the data to follow.
 * - The metadata is in the SSDS metadata database.
 * - A burst of data has been sent, followed by a timeout, then more
 *   data sent
 * - NetCDFAgent is active for data stream
 * 
 * Test steps:
 * - Send another burst of data
 * 
 * Final conditions:
 * - NetCDFAgent pointing to the data is active.
 * 
 * Case 6
 * --------------------------------------------------------------------------
 * Initial conditions:
 * - There are no data stream files and no
 *   XML files describing states of NetCDFAgents.
 * - There is no metadata in the database to
 *   describe any data that will appear in 
 *   the test.
 * 
 * Test steps:
 * - Burst of data is sent.
 * 
 * Final conditions:
 * - Data stream file created.
 * - No NetCDFAgent is created because there is
 *   no metadata available yet.
 * 
 * Case 7
 * --------------------------------------------------------------------------
 * Initial conditions:
 * - Data stream file exists
 * - No metadata describing that file exists
 * - No NetCDFAgent active
 * 
 * Test steps:
 * - Metadata packet is sent describing data in data stream file
 * 
 * Final condition:
 * - Metadata in the SSDS metadata database
 * - Metadata data stream created
 * - NetCDFAgent active pointing to the data stream file that the
 *   metadata desribes
 * 
 * JUnit TestCase.
 * @testfamily JUnit
 * @testkind testcase
 * @testsetup Default TestCase
 */
public class TestNetCDFWatchdog extends TestCase {
	/**
	 * Constructs a test case with the given name.
	 */
	public TestNetCDFWatchdog(String name) {
		super(name);
	}

	/**
	 * Set up for the tests
	 */
	protected void setUp() {
//		// First grab the properties file
//		try {
//			ruminateProperties.load(
//				this.getClass().getResourceAsStream(
//					"/moos/ssds/ruminate/ruminate.properties"));
//			ingestProperties.load(
//				this.getClass().getResourceAsStream(
//					"/moos/ssds/ingest/ingest.properties"));
//		} catch (IOException e) {
//			logger.error(
//				"Could not load the ruminate.properties and/or ingest.properties file");
//		}
//		// Setup file pointers
//		metadataStreamOne =
//			new File(
//				ingestProperties.getProperty("ingest.storage.directory"),
//				"1_1_0_-1");
//		dataStreamOneRTOne =
//			new File(
//				ingestProperties.getProperty("ingest.storage.directory"),
//				"1_1_1_-1");
//		dataStreamOneRTTwo =
//			new File(
//				ingestProperties.getProperty("ingest.storage.directory"),
//				"1_1_2_-1");
//		metadataStreamTwo =
//			new File(
//				ingestProperties.getProperty("ingest.storage.directory"),
//				"2_1_0_-1");
//		dataStreamTwoRTOne =
//			new File(
//				ingestProperties.getProperty("ingest.storage.directory"),
//				"2_1_1_-1");
//		// The netcdf files
//		dataStreamOneRTOneNetCDF =
//			new File(
//				ruminateProperties.getProperty("ruminate.storage.netcdf")
//					+ File.separator
//					+ "packetNetCDFs"
//					+ File.separator
//					+ "1_1_1_-1.nc");
//		dataStreamOneRTTwoNetCDF =
//			new File(
//				ruminateProperties.getProperty("ruminate.storage.netcdf")
//					+ File.separator
//					+ "packetNetCDFs"
//					+ File.separator
//					+ "1_1_2_-1.nc");
//		dataStreamTwoRTOneNetCDF =
//			new File(
//				ruminateProperties.getProperty("ruminate.storage.netcdf")
//					+ File.separator
//					+ "packetNetCDFs"
//					+ File.separator
//					+ "2_1_1_-1.nc");
//		// The xml agent files
//		dataStreamOneRTOneAgentXML =
//			new File(
//				ruminateProperties.getProperty("ruminate.storage.netcdf")
//					+ File.separator
//					+ "packetNetCDFs"
//					+ File.separator
//					+ "agentXML"
//					+ File.separator
//					+ "1_1_1_-1_agent_state.xml");
//		dataStreamOneRTTwoAgentXML =
//			new File(
//				ruminateProperties.getProperty("ruminate.storage.netcdf")
//					+ File.separator
//					+ "packetNetCDFs"
//					+ File.separator
//					+ "agentXML"
//					+ File.separator
//					+ "1_1_2_-1_agent_state.xml");
//		dataStreamTwoRTOneAgentXML =
//			new File(
//				ruminateProperties.getProperty("ruminate.storage.netcdf")
//					+ File.separator
//					+ "packetNetCDFs"
//					+ File.separator
//					+ "agentXML"
//					+ File.separator
//					+ "2_1_1_-1_agent_state.xml");
//
//		// TODO KJG - Remove the metadata from the database
//
//		// Create the publisher component
//		pc = new PublisherComponent();
	}

	/**
	 * This test sends a metadata packet and verifies the metadata stream file is there and
	 * that the metadata was created in the database and there is no netCDF Agent yet.
	 *
	 */
	public void testOne() {
//		// Since these tests cannot be guaranteed to run in order, the files need to be removed
//		// and recreated each time
//		metadataStreamOne.delete();
//		dataStreamOneRTOne.delete();
//		dataStreamOneRTTwo.delete();
//		metadataStreamTwo.delete();
//		dataStreamTwoRTOne.delete();
//		dataStreamOneRTOneNetCDF.delete();
//		dataStreamOneRTTwoNetCDF.delete();
//		dataStreamTwoRTOneNetCDF.delete();
//		dataStreamOneRTOneAgentXML.delete();
//		dataStreamOneRTTwoAgentXML.delete();
//		dataStreamTwoRTOneAgentXML.delete();
//
//		// Now publish a metadata packet
//		MetadataPacket mdp =
//			new MetadataPacket(
//				TestNetCDFWatchdog.deviceOneID.longValue(),
//				new String("No particular reason").getBytes(),
//				TestNetCDFWatchdog.deviceOneMetadata.getBytes());
//		mdp.setMetadataRef(0);
//		mdp.setSequenceNo(1);
//		mdp.setSystemTime(new Date().getTime());
//		mdp.setRecordType(0);
//		mdp.setParentId(-1);
//
//		// Publish it
//		pc.publish(mdp);
//		// Wait for a bit
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		// Assert that the metadata file exists
//		assertTrue(
//			"The metadata data stream file should exist: ",
//			metadataStreamOne.exists());
//		// TODO - how would you remotely test that a NetCDFAgent is active?
//
//		// Now that the metadata exists, send 200 packets
//		Random random = new Random();
//		long previousISITimestamp = 0L;
//		for (int i = 2; i < 202; i++) {
//			SensorDataPacket sdp =
//				new SensorDataPacket(
//					TestNetCDFWatchdog.deviceOneID.longValue(),
//					i);
//			sdp.setSequenceNo(i);
//			String rv1 = new String("RecordVariableOneLoop" + i);
//			String rv2 = new String("RV2" + random.nextDouble());
//			String rv3 = new String("RV3" + random.nextDouble());
//			String rv4 = new String("RecordVariableFourLoop" + i);
//			String rv5 = new String("RV5" + random.nextDouble());
//			String rv6 = new String("RV6" + random.nextDouble());
//			String rv7 = new String("RecordVariableSevenLoop" + i);
//			String dataBuffer =
//				new String(
//					rv1
//						+ ","
//						+ rv2
//						+ ","
//						+ rv3
//						+ ","
//						+ rv4
//						+ ","
//						+ rv5
//						+ ","
//						+ rv6
//						+ ","
//						+ rv7);
//			sdp.setDataBuffer(dataBuffer.getBytes());
//			sdp.setMetadataRef(1L);
//			sdp.setParentId(-1L);
//			sdp.setRecordType(1L);
//			long newTimestamp = new Date().getTime();
//			if (newTimestamp == previousISITimestamp) {
//				newTimestamp++;
//			}
//			sdp.setSystemTime(newTimestamp);
//			previousISITimestamp = newTimestamp;
//			pc.publish(sdp);
//		}
//		// Wait for a bit
//		try {
//			Thread.sleep(60000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		// Assert that the data file exists
//		assertTrue(
//			"The data stream file should exist: ",
//			dataStreamOneRTOne.exists());
//		// Assert that the netcdf file exists
//		assertTrue(
//			"The netcdf file should exist: ",
//			this.dataStreamOneRTOneNetCDF.exists());
//		// Grab the timestamp on the data and netcdf file
//		long dataStreamTimestamp = this.dataStreamOneRTOne.lastModified();
//		long netCDFTimestamp = this.dataStreamOneRTOneNetCDF.lastModified();
//		random = new Random();
//		for (int i = 203; i < 403; i++) {
//			SensorDataPacket sdp =
//				new SensorDataPacket(
//					TestNetCDFWatchdog.deviceOneID.longValue(),
//					i);
//			sdp.setSequenceNo(i);
//			String rv1 = new String("RecordVariableOneLoop" + i);
//			String rv2 = new String("RV2" + random.nextDouble());
//			String rv3 = new String("RV3" + random.nextDouble());
//			String rv4 = new String("RecordVariableFourLoop" + i);
//			String rv5 = new String("RV5" + random.nextDouble());
//			String rv6 = new String("RV6" + random.nextDouble());
//			String rv7 = new String("RecordVariableSevenLoop" + i);
//			String dataBuffer =
//				new String(
//					rv1
//						+ ","
//						+ rv2
//						+ ","
//						+ rv3
//						+ ","
//						+ rv4
//						+ ","
//						+ rv5
//						+ ","
//						+ rv6
//						+ ","
//						+ rv7);
//			sdp.setDataBuffer(dataBuffer.getBytes());
//			sdp.setMetadataRef(1L);
//			sdp.setParentId(-1L);
//			sdp.setRecordType(1L);
//			long newTimestamp = new Date().getTime();
//			if (newTimestamp == previousISITimestamp) {
//				newTimestamp++;
//			}
//			sdp.setSystemTime(newTimestamp);
//			previousISITimestamp = newTimestamp;
//			pc.publish(sdp);
//		}
//		// Wait for a bit
//		try {
//			Thread.sleep(60000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		// Now verify that the timestamp on the netcdf file has changed
//		long newDataStreamTimestamp = this.dataStreamOneRTOne.lastModified();
//		long newNetCDFTimestamp = this.dataStreamOneRTOneNetCDF.lastModified();
//		assertTrue("The timestamp on the data stream should have changed",newDataStreamTimestamp>dataStreamTimestamp);
//		assertTrue("The timestamp on the netcdf file should have changed",newNetCDFTimestamp>netCDFTimestamp);
//		// Do a dump
//		try {
//			NCdump.print(
//				this.dataStreamOneRTOneNetCDF.getAbsolutePath() + " -vall",
//				System.out);
//		} catch (IOException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
	}

	/**
	 * Tears down the fixture, for example, close a network connection.
	 * This method is called after a test is executed.
	 */
	protected void tearDown() {
//		// Wait for a bit
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.gc();
//		// Remove the data/metadata stream files
//		if (!metadataStreamOne.delete())
//			logger.error("Could not delete file metadataStreamOne");
//		if (!dataStreamOneRTOne.delete())
//			logger.error("Could not delete file dataStreamOneRTOne");
//		if (!dataStreamOneRTTwo.delete())
//			logger.error("Could not delete file dataStreamOneRTTwo");
//		if (!metadataStreamTwo.delete())
//			logger.error("Could not delete file metadataStreamTwo");
//		if (!dataStreamTwoRTOne.delete())
//			logger.error("Could not delete file dataStreamTwoRTOne");
//		if (!dataStreamOneRTOneNetCDF.delete())
//			logger.error("Could not delete file dataStreamOneRTOneNetCDF");
//		if (!dataStreamOneRTTwoNetCDF.delete())
//			logger.error("Could not delete file dataStreamOneRTTwoNetCDF");
//		if (!dataStreamTwoRTOneNetCDF.delete())
//			logger.error("Could not delete file dataStreamTwoRTOneNetCDF");
//		if (!dataStreamOneRTOneAgentXML.delete())
//			logger.error("Could not delete file dataStreamOneRTOneAgentXML");
//		if (!dataStreamOneRTTwoAgentXML.delete())
//			logger.error("Could not delete file dataStreamOneRTTwoAgentXML");
//		if (!dataStreamTwoRTOneAgentXML.delete())
//			logger.error("Could not delete file dataStreamTwoRTOneAgentXML");
	}

	/**
	 * A log4j logger
	 *
	 */
	static Logger logger = Logger.getLogger(TestNetCDFWatchdog.class);

	// First device to test with
	static final Long deviceOneID = new Long(1L);
	static final String deviceOneMetadata =
		new String(
			"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
				+ "<Metadata>\n"
				+ "<Deployment>\n"
				+ "<Device id=\"1\"/>\n"
				+ "<output>\n"
				+ "<DataStream>\n"
				+ "<RecordDescription recordType=\"1\" bufferStyle=\"ASCII\" bufferItemSeparator=\",\" parseable=\"true\">\n"
				+ "<RecordVariable name=\"RT1_VAR1\" columnIndex=\"1\" format=\"string\" longName=\"Record Type 1's First Variable\" units=\"RV1 Units\"/>\n"
				+ "<RecordVariable name=\"RT1_VAR2\" columnIndex=\"2\" format=\"double\" longName=\"Record Type 1's Second Variable\" parseRegExp=\"RV2(\\S+)\" units =\"RV2 Units\"/>\n"
				+ "<RecordVariable	name=\"RT1_VAR3\" columnIndex=\"3\" format=\"double\" longName=\"Record Type 1's Third Variable\" parseRegExp=\"RV3(\\S+)\" units =\"RV3 Units\"/>\n"
				+ "<RecordVariable name=\"RT1_VAR4\" columnIndex=\"4\" format=\"string\" longName=\"Record Type 1's Fourth Variable\" units=\"RV4 Units\"/>\n"
				+ "<RecordVariable name=\"RT1_VAR5\" columnIndex=\"5\" format=\"double\" longName=\"Record Type 1's Fifth Variable\" parseRegExp=\"RV5(\\S+)\" units =\"RV5 Units\"/>\n"
				+ "<RecordVariable name=\"RT1_VAR6\" columnIndex=\"6\" format=\"double\" longName=\"Record Type 1's Sixth Variable\" parseRegExp=\"RV6(\\S+)\" units =\"RV6 Units\"/>\n"
				+ "<RecordVariable name=\"RT1_VAR7\" columnIndex=\"7\" format=\"string\" longName=\"Record Type 1's Seventh Variable\" units=\"RV7 Units\"/>\n"
				+ "</RecordDescription>\n"
				+ "</DataStream>\n"
				+ "<DataStream>\n"
				+ "<RecordDescription recordType=\"2\" bufferStyle=\"ASCII\" bufferItemSeparator=\",\" parseable=\"true\">\n"
				+ "<RecordVariable name=\"RT2_RV1\" columnIndex=\"1\" format=\"double\" longName=\"Record Type 2's First Variable\" parseRegExp=\"RV1(\\d+\\.\\d+)\" units =\"RV1 Units\"/>\n"
				+ "<RecordVariable name=\"RT2_RV2\" columnIndex=\"2\" format=\"double\" longName=\"Record Type 2's Second Variable\" parseRegExp=\"RV2(\\d+\\.\\d+)\" units =\"RV2 Units\"/>\n"
				+ "<RecordVariable name=\"RT2_RV3\" columnIndex=\"3\" format=\"double\" longName=\"Record Type 2's Third Variable\" parseRegExp=\"RV3(\\d+\\.\\d+)\" units =\"RV3 Units\"/>\n"
				+ "</RecordDescription>\n"
				+ "</DataStream>\n"
				+ "</output>\n"
				+ "<Person id=\"101\" firstname=\"Kevin\" surname=\"Gomes\" email=\"kgomes@mbari.org\"/>\n"
				+ "</Deployment>\n"
				+ "</Metadata>\n");
	static final String deviceOneSIAMMetadata =
		new String(
			"<ParentMetadata>\nparentId = 1295\nlocation = I'm With Stupid\n</ParentMetadata>"
				+ "<InstrumentState>$PSID, V2.0Q [Feb 14 2004], *1651\n"
				+ "$PALEVEL, PL0.000000, PH0.000000, TL0.000000, TH0.000000, HL0.000000, HH0.000000, GFLL0.000000, GFLH0.000000, GFHL0.000000, GFHH0.000000, CL0.000000, CH0.000000, TCL0.000000, TCH0.000000, *984\n"
				+ "$PAMASK, 00, *729\n"
				+ "$PALARM, 00, *729\n"
				+ "</InstrumentState>"
				+ "<InstrumentServiceState>"
				+ "name=\"MSP430 test\"\n"
				+ "deviceID=\"1296\"\n"
				+ "registryName=\"MSP430_SERVICE\"\n"
				+ "prompt=\"> \"\n"
				+ "sampleTerminator=\"\\n\"\n"
				+ "sampleSchedule=\"R */30 */0 */0 */0 * * * * GMT *\"\n"
				+ "timeout=\"10000\"\n"
				+ "maxSampleTries=\"3\"\n"
				+ "maxSampleBytes=\"256\"\n"
				+ "maxSkipBytes=\"256\"\n"
				+ "instrumentStartDelay=\"500\"\n"
				+ "currentLimit=\"500\"\n"
				+ "instrumentPowerPolicy=\"2\"\n"
				+ "commsPowerPolicy=\"2\"\n"
				+ "</InstrumentServiceState>\n"
				+ "<ServiceProperties>\n"
				+ "getServicePropertiesBytes: File not found: service.properties\n"
				+ "</ServiceProperties>\n"
				+ "<ServiceXML>\n"
				+ "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
				+ "<Metadata>\n"
				+ "<Deployment>\n"
				+ "<Device id=\"1\"/>\n"
				+ "<output>\n"
				+ "<DataStream>\n"
				+ "<RecordDescription recordType=\"1\" bufferStyle=\"ASCII\" bufferItemSeparator=\",\" parseable=\"true\">\n"
				+ "<RecordVariable name=\"RT1_VAR1\" columnIndex=\"1\" format=\"string\" longName=\"Record Type 1's First Variable\" units=\"RV1 Units\"/>\n"
				+ "<RecordVariable name=\"RT1_VAR2\" columnIndex=\"2\" format=\"double\" longName=\"Record Type 1's Second Variable\" parseRegExp=\"RV2(\\S+)\" units =\"RV2 Units\"/>\n"
				+ "<RecordVariable	name=\"RT1_VAR3\" columnIndex=\"3\" format=\"double\" longName=\"Record Type 1's Third Variable\" parseRegExp=\"RV3(\\S+)\" units =\"RV3 Units\"/>\n"
				+ "<RecordVariable name=\"RT1_VAR4\" columnIndex=\"4\" format=\"string\" longName=\"Record Type 1's Fourth Variable\" units=\"RV4 Units\"/>\n"
				+ "<RecordVariable name=\"RT1_VAR5\" columnIndex=\"5\" format=\"double\" longName=\"Record Type 1's Fifth Variable\" parseRegExp=\"RV5(\\S+)\" units =\"RV5 Units\"/>\n"
				+ "<RecordVariable name=\"RT1_VAR6\" columnIndex=\"6\" format=\"double\" longName=\"Record Type 1's Sixth Variable\" parseRegExp=\"RV6(\\S+)\" units =\"RV6 Units\"/>\n"
				+ "<RecordVariable name=\"RT1_VAR7\" columnIndex=\"7\" format=\"string\" longName=\"Record Type 1's Seventh Variable\" units=\"RV7 Units\"/>\n"
				+ "</RecordDescription>\n"
				+ "</DataStream>\n"
				+ "<DataStream>\n"
				+ "<RecordDescription recordType=\"2\" bufferStyle=\"ASCII\" bufferItemSeparator=\",\" parseable=\"true\">\n"
				+ "<RecordVariable name=\"RT2_RV1\" columnIndex=\"1\" format=\"double\" longName=\"Record Type 2's First Variable\" parseRegExp=\"RV1(\\d+\\.\\d+)\" units =\"RV1 Units\"/>\n"
				+ "<RecordVariable name=\"RT2_RV2\" columnIndex=\"2\" format=\"double\" longName=\"Record Type 2's Second Variable\" parseRegExp=\"RV2(\\d+\\.\\d+)\" units =\"RV2 Units\"/>\n"
				+ "<RecordVariable name=\"RT2_RV3\" columnIndex=\"3\" format=\"double\" longName=\"Record Type 2's Third Variable\" parseRegExp=\"RV3(\\d+\\.\\d+)\" units =\"RV3 Units\"/>\n"
				+ "</RecordDescription>\n"
				+ "</DataStream>\n"
				+ "</output>\n"
				+ "<Person id=\"101\" firstname=\"Kevin\" surname=\"Gomes\" email=\"kgomes@mbari.org\"/>\n"
				+ "</Deployment>\n"
				+ "</Metadata>\n"
				+ "</ServiceXML>\n"
				+ "<ServiceCache>\n"
				+ "getServiceCacheBytes: File not found: service.cache"
				+ "</ServiceCache>\n");
	// Second device to test with
	static final Long deviceTwoID = new Long(2L);
	static final String deviceTwoMetadata =
		new String(
			"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
				+ "<Metadata>\n"
				+ "<Deployment>\n"
				+ "<Device id=\"2\"/>\n"
				+ "<output>\n"
				+ "<DataStream>\n"
				+ "<RecordDescription recordType=\"1\" bufferStyle=\"ASCII\" bufferItemSeparator=\",\" parseable=\"true\">\n"
				+ "<RecordVariable name=\"RT1_VAR1\" columnIndex=\"1\" format=\"string\" longName=\"Record Type 1's First Variable\" units=\"RV1 Units\"/>\n"
				+ "<RecordVariable name=\"RT1_VAR2\" columnIndex=\"2\" format=\"double\" longName=\"Record Type 1's Second Variable\" parseRegExp=\"RV2(\\d+\\.\\d+)\" units =\"RV2 Units\"/>\n"
				+ "</DataStream>\n"
				+ "</output>\n"
				+ "<Person id=\"101\" firstname=\"Kevin\" surname=\"Gomes\" email=\"kgomes@mbari.org\"/>\n"
				+ "</Deployment>\n"
				+ "</Metadata>\n");

	// The properties
	private Properties ruminateProperties = new Properties();
	private Properties ingestProperties = new Properties();
	// The files
	private File metadataStreamOne = null;
	private File dataStreamOneRTOne = null;
	private File dataStreamOneRTTwo = null;
	private File metadataStreamTwo = null;
	private File dataStreamTwoRTOne = null;
	private File dataStreamOneRTOneNetCDF = null;
	private File dataStreamOneRTTwoNetCDF = null;
	private File dataStreamTwoRTOneNetCDF = null;
	private File dataStreamOneRTOneAgentXML = null;
	private File dataStreamOneRTTwoAgentXML = null;
	private File dataStreamTwoRTOneAgentXML = null;

	// A publishing component
	private PublisherComponent pc = null;

}
