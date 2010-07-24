package test.moos.ssds.io;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import junit.framework.TestCase;
import moos.ssds.io.PacketSQLQueryFactory;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class PacketSQLQueryFactoryTest extends TestCase {

	Logger logger = Logger.getLogger(PacketSQLQueryFactory.class);

	/**
	 * This is the delimiter that is used in queries to delimit the table name
	 * since they are device IDs. The default is set to the MySQL one of `, but
	 * it can be changed in the constructor
	 */
	private String sqlTableDelimiter = null;

	/**
	 * These are the SQL fragments that will be inserted to select by last
	 * number of packets. These are DB specific they need to be set before
	 * using.
	 */
	private String sqlLastNumberOfPacketsPreamble = null;
	private String sqlLastNumberOfPacketsPostamble = null;

	public PacketSQLQueryFactoryTest(String name) {
		super(name);

		Properties log4jProperties = new Properties();
		try {
			log4jProperties.load(this.getClass().getResourceAsStream(
					"/log4j.properties"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		PropertyConfigurator.configure(log4jProperties);

		// Create a properties object and read in the io.properties file
		Properties ioProperties = new Properties();
		try {
			ioProperties.load(this.getClass().getResourceAsStream(
					"/moos/ssds/io/io.properties"));
		} catch (Exception e) {
			logger.error("Exception trying to read in properties file: "
					+ e.getMessage());
		}

		// Grab the SQL table delimiter
		this.sqlTableDelimiter = ioProperties
				.getProperty("io.storage.sql.table.delimiter");
		// Grab the last number of packets pre and post ambles for the
		// underlying database
		this.sqlLastNumberOfPacketsPreamble = ioProperties
				.getProperty("io.storage.sql.lastnumber.preamble");
		this.sqlLastNumberOfPacketsPostamble = ioProperties
				.getProperty("io.storage.sql.lastnumber.postamble");

	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testDefaultSelectConstruction() {
		// Create the factory class
		PacketSQLQueryFactory packetSQLQueryFactory = new PacketSQLQueryFactory(
				new Long(100));

		String defaultQuery = packetSQLQueryFactory.getQueryStatement();

		// Now pull the default query
		assertEquals(
				"The default SELECT statement should be correct",
				"SELECT * FROM "
						+ sqlTableDelimiter
						+ "100"
						+ sqlTableDelimiter
						+ " ORDER BY timestampSeconds ASC, timestampNanoseconds ASC",
				defaultQuery);
	}

	public void testSelectWithParametersConstruction() {
		// Create the factory class
		PacketSQLQueryFactory packetSQLQueryFactory = new PacketSQLQueryFactory(
				new Long(100));

		// Add some parameters
		packetSQLQueryFactory
				.addSelectParameter(PacketSQLQueryFactory.SSDS_PACKET_VERSION);
		packetSQLQueryFactory
				.addSelectParameter(PacketSQLQueryFactory.DATA_DESCRIPTION_ID);
		packetSQLQueryFactory
				.addSelectParameter(PacketSQLQueryFactory.SEQUENCE_NUMBER);

		String selectQuery = packetSQLQueryFactory.getQueryStatement();

		// Now pull the default query
		assertEquals(
				"The default SELECT statement should be correct",
				"SELECT "
						+ PacketSQLQueryFactory.SSDS_PACKET_VERSION
						+ ", "
						+ PacketSQLQueryFactory.DATA_DESCRIPTION_ID
						+ ", "
						+ PacketSQLQueryFactory.SEQUENCE_NUMBER
						+ " FROM "
						+ sqlTableDelimiter
						+ "100"
						+ sqlTableDelimiter
						+ " ORDER BY timestampSeconds ASC, timestampNanoseconds ASC",
				selectQuery);
	}

	public void testSelectWithWhereClause() {
		// Create the factory class
		PacketSQLQueryFactory packetSQLQueryFactory = new PacketSQLQueryFactory(
				new Long(100));

		// Add some parameters
		packetSQLQueryFactory
				.addSelectParameter(PacketSQLQueryFactory.SSDS_PACKET_VERSION);
		packetSQLQueryFactory
				.addSelectParameter(PacketSQLQueryFactory.DATA_DESCRIPTION_ID);
		packetSQLQueryFactory
				.addSelectParameter(PacketSQLQueryFactory.SEQUENCE_NUMBER);

		// Set some variables for the WHERE clause
		packetSQLQueryFactory.setSSDSPacketVersion(3);
		Date startDate = new Date();
		startDate.setTime(startDate.getTime() - (1000 * 60 * 60 * 168));
		Date endDate = new Date();
		packetSQLQueryFactory.setEndDate(endDate);
		packetSQLQueryFactory.setStartDate(startDate);
		packetSQLQueryFactory.setStartPacketSubType(1);

		String selectQuery = packetSQLQueryFactory.getQueryStatement();

		// Now pull the default query
		assertEquals(
				"The default SELECT statement should be correct",
				"SELECT "
						+ PacketSQLQueryFactory.SSDS_PACKET_VERSION
						+ ", "
						+ PacketSQLQueryFactory.DATA_DESCRIPTION_ID
						+ ", "
						+ PacketSQLQueryFactory.SEQUENCE_NUMBER
						+ " FROM "
						+ sqlTableDelimiter
						+ "100"
						+ sqlTableDelimiter
						+ " WHERE "
						+ PacketSQLQueryFactory.SSDS_PACKET_VERSION
						+ " = 3 AND "
						+ PacketSQLQueryFactory.PACKET_SUB_TYPE
						+ " = 1 AND "
						+ PacketSQLQueryFactory.TIMESTAMP_SECONDS
						+ " >= "
						+ startDate.getTime() / 1000
						+ " AND "
						+ PacketSQLQueryFactory.TIMESTAMP_SECONDS
						+ " <= "
						+ endDate.getTime() / 1000
						+ " ORDER BY timestampSeconds ASC, timestampNanoseconds ASC",
				selectQuery);
	}

	public void testOrderByClause() {
		// Create the factory class
		PacketSQLQueryFactory packetSQLQueryFactory = new PacketSQLQueryFactory(
				new Long(100));

		// Add some parameters
		packetSQLQueryFactory
				.addSelectParameter(PacketSQLQueryFactory.SSDS_PACKET_VERSION);
		packetSQLQueryFactory
				.addSelectParameter(PacketSQLQueryFactory.DATA_DESCRIPTION_ID);
		packetSQLQueryFactory
				.addSelectParameter(PacketSQLQueryFactory.SEQUENCE_NUMBER);

		// Set some variables for the WHERE clause
		packetSQLQueryFactory.setSSDSPacketVersion(3);
		Date startDate = new Date();
		startDate.setTime(startDate.getTime() - (1000 * 60 * 60 * 168));
		Date endDate = new Date();
		packetSQLQueryFactory.setEndDate(endDate);
		packetSQLQueryFactory.setStartDate(startDate);
		packetSQLQueryFactory.setStartPacketSubType(1);

		// Add some order by variables
		packetSQLQueryFactory.addOrderByParameter(
				PacketSQLQueryFactory.TIMESTAMP_SECONDS, true);
		packetSQLQueryFactory.addOrderByParameter(
				PacketSQLQueryFactory.TIMESTAMP_NANOSECONDS, true);
		packetSQLQueryFactory.addOrderByParameter(
				PacketSQLQueryFactory.SEQUENCE_NUMBER, false);

		String selectQuery = packetSQLQueryFactory.getQueryStatement();

		// Now pull the default query
		assertEquals("The default SELECT statement should be correct",
				"SELECT " + PacketSQLQueryFactory.SSDS_PACKET_VERSION + ", "
						+ PacketSQLQueryFactory.DATA_DESCRIPTION_ID + ", "
						+ PacketSQLQueryFactory.SEQUENCE_NUMBER + " FROM "
						+ sqlTableDelimiter + "100" + sqlTableDelimiter
						+ " WHERE " + PacketSQLQueryFactory.SSDS_PACKET_VERSION
						+ " = 3 AND " + PacketSQLQueryFactory.PACKET_SUB_TYPE
						+ " = 1 AND " + PacketSQLQueryFactory.TIMESTAMP_SECONDS
						+ " >= " + startDate.getTime() / 1000 + " AND "
						+ PacketSQLQueryFactory.TIMESTAMP_SECONDS + " <= "
						+ endDate.getTime() / 1000 + " ORDER BY "
						+ PacketSQLQueryFactory.TIMESTAMP_SECONDS + " DESC, "
						+ PacketSQLQueryFactory.TIMESTAMP_NANOSECONDS
						+ " DESC, " + PacketSQLQueryFactory.SEQUENCE_NUMBER
						+ " ASC", selectQuery);
	}

	public void testLastNumberOfPackets() {
		// Create the factory class
		PacketSQLQueryFactory packetSQLQueryFactory = new PacketSQLQueryFactory(
				new Long(100));

		// Add some parameters
		packetSQLQueryFactory
				.addSelectParameter(PacketSQLQueryFactory.SSDS_PACKET_VERSION);
		packetSQLQueryFactory
				.addSelectParameter(PacketSQLQueryFactory.DATA_DESCRIPTION_ID);
		packetSQLQueryFactory
				.addSelectParameter(PacketSQLQueryFactory.SEQUENCE_NUMBER);

		// Set some variables for the WHERE clause
		packetSQLQueryFactory.setSSDSPacketVersion(3);
		Date startDate = new Date();
		startDate.setTime(startDate.getTime() - (1000 * 60 * 60 * 168));
		Date endDate = new Date();
		packetSQLQueryFactory.setEndDate(endDate);
		packetSQLQueryFactory.setStartDate(startDate);
		packetSQLQueryFactory.setStartPacketSubType(1);
		packetSQLQueryFactory.setLastNumberOfPackets(199);

		// Add some order by variables
		packetSQLQueryFactory.addOrderByParameter(
				PacketSQLQueryFactory.TIMESTAMP_SECONDS, true);
		packetSQLQueryFactory.addOrderByParameter(
				PacketSQLQueryFactory.TIMESTAMP_NANOSECONDS, true);
		packetSQLQueryFactory.addOrderByParameter(
				PacketSQLQueryFactory.SEQUENCE_NUMBER, false);

		String selectQuery = packetSQLQueryFactory.getQueryStatement();

		// Now pull the default query
		String supposedToBe = "SELECT "
				+ PacketSQLQueryFactory.SSDS_PACKET_VERSION
				+ ", "
				+ PacketSQLQueryFactory.DATA_DESCRIPTION_ID
				+ ", "
				+ PacketSQLQueryFactory.SEQUENCE_NUMBER
				+ " FROM "
				+ this.sqlLastNumberOfPacketsPreamble.replaceAll(
						"@LAST_NUMBER_OF_PACKETS@", 199 + "")
				+ " "
				+ sqlTableDelimiter
				+ "100"
				+ sqlTableDelimiter
				+ " WHERE "
				+ PacketSQLQueryFactory.SSDS_PACKET_VERSION
				+ " = 3 AND "
				+ PacketSQLQueryFactory.PACKET_SUB_TYPE
				+ " = 1 AND "
				+ PacketSQLQueryFactory.TIMESTAMP_SECONDS
				+ " >= "
				+ startDate.getTime() / 1000
				+ " AND "
				+ PacketSQLQueryFactory.TIMESTAMP_SECONDS
				+ " <= "
				+ endDate.getTime()
				/ 1000
				+ " "
				+ this.sqlLastNumberOfPacketsPostamble.replaceAll(
						"@LAST_NUMBER_OF_PACKETS@", 199 + "") + " ORDER BY "
				+ PacketSQLQueryFactory.TIMESTAMP_SECONDS + " DESC, "
				+ PacketSQLQueryFactory.TIMESTAMP_NANOSECONDS + " DESC, "
				+ PacketSQLQueryFactory.SEQUENCE_NUMBER + " ASC";
		assertEquals("The default SELECT statement should be correct",
				supposedToBe, selectQuery);

	}
}
