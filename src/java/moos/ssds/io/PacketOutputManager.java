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
package moos.ssds.io;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 * This class is designed to manage a pool of PacketOutput objects that can be
 * used by the ingest MessageDrivenBeans. The reason this is done is to prevent
 * unsynchronized writes to the file where the DevicePackets are serialized to.
 * 
 * @author kgomes
 */
public class PacketOutputManager {

	/**
	 * This is the collection of active <code>PacketOutput</code> s that can be
	 * returned to calling clients.
	 * 
	 * @associates PacketOutput
	 */
	private static Map<String, PacketOutput> packetOutputs = Collections
			.synchronizedMap(new HashMap<String, PacketOutput>());

	/**
	 * This is the string that is the base of the directory where the raw
	 * serialized packets are stored
	 */
	private static String packetStorageBase = null;

	/**
	 * This is the Map that contains a map of device ID to the PacketSQLOutput
	 * objects
	 * 
	 * @associates PacketSQLOutput
	 */
	private static Map<Long, PacketSQLOutput> packetSQLOutputs = Collections
			.synchronizedMap(new HashMap<Long, PacketSQLOutput>());

	/**
	 * This is the <code>DataSource</code> that the PacketSQLOutputs will use.
	 * 
	 * @associates DataSource
	 */
	private static DataSource dataSource = null;

	/**
	 * This is the host where the DataSource will be looked up
	 */
	private String jndiHostName = null;

	/**
	 * This is the JNDI name of the data source that will be used
	 */
	private String dataSourceJndiName = null;

	/**
	 * These are strings that will be used as templates to form SQL statements
	 * for the sql table creation
	 */
	private static String createTableSQLTemplate = null;
	private static String createPrimaryKeySQLTemplate = null;
	private static String createTimestampIndexSQLTemplate = null;
	private static String sqlTableDelimiter = null;

	/**
	 * This is the static <code>PacketOutputManager</code> that enforces the
	 * singelton pattern.
	 */
	private static PacketOutputManager instance = null;

	/**
	 * The IO Properties for the PacketInput/Output
	 */
	private static Properties ioProperties = null;

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(PacketOutputManager.class);

	/**
	 * This constructor is private because no client should be able to construct
	 * one of these. This class is a singleton.
	 */
	private PacketOutputManager() {
		logger.debug("Creating a new PacketOutputManager");
		// Create and load the io properties
		if (ioProperties == null) {
			ioProperties = new Properties();
			try {
				ioProperties.load(this.getClass().getResourceAsStream(
						"/moos/ssds/io/io.properties"));
			} catch (Exception e) {
				logger.error("Exception trying to read in properties file: "
						+ e.getMessage());
			}
		}
		// Grab the directory for the storage name
		packetStorageBase = ioProperties.getProperty("io.storage.directory");
		logger.debug("packetStorageBase = " + packetStorageBase);

		// Grab JNDI stuff
		this.jndiHostName = ioProperties
				.getProperty("io.storage.sql.jndi.server.name");
		this.dataSourceJndiName = "java:/"
				+ ioProperties.getProperty("io.storage.sql.jndi.name");

		// Now grab the DataSource from the JNDI
		Context jndiContext = null;
		try {
			jndiContext = new InitialContext();
			if ((this.jndiHostName != null) && (!this.jndiHostName.equals(""))) {
				jndiContext.removeFromEnvironment(Context.PROVIDER_URL);
				jndiContext.addToEnvironment(Context.PROVIDER_URL,
						this.jndiHostName + ":1099");
			}
			logger.debug("JNDI environment = " + jndiContext.getEnvironment());
		} catch (NamingException ne) {
			logger.error("!!--> A naming exception was caught while trying "
					+ "to get an initial context: " + ne.getMessage());
			return;
		} catch (Exception e) {
			logger.error("!!--> An unknown exception was caught while trying "
					+ "to get an initial context: " + e.getMessage());
			return;
		}
		try {
			dataSource = (DataSource) jndiContext
					.lookup(this.dataSourceJndiName);
		} catch (NamingException e1) {
			logger.error("Could not get DataSource: " + e1.getMessage());
		}

		// Now grab the SQL templates to use for creating tables, primary keys,
		// and indicies
		createTableSQLTemplate = ioProperties
				.getProperty("io.storage.sql.create.device.table");
		createPrimaryKeySQLTemplate = ioProperties
				.getProperty("io.storage.sql.create.device.table.primary.key");
		createTimestampIndexSQLTemplate = ioProperties
				.getProperty("io.storage.sql.create.device.table.index");
		sqlTableDelimiter = ioProperties
				.getProperty("io.storage.sql.table.delimiter");
		logger.debug("SQL Templates are:");
		logger.debug("Create table ->" + createTableSQLTemplate);
		logger.debug("Create primary key -> " + createPrimaryKeySQLTemplate);
		logger.debug("Create timestamp index -> "
				+ createTimestampIndexSQLTemplate);
		logger.debug("SQL table delimiter -> " + sqlTableDelimiter);
	}

	/**
	 * This is the method that returns the active instance of the
	 * <code>PacketOutputManager</code> for clients to use. This is what should
	 * be called by any client that is interested in getting
	 * <code>PacketOutput</code> s to write <code>SSDSDevicePacket</code> s to.
	 * 
	 * @return the one instance of <code>PacketOutputManager</code> that
	 *         everybody uses
	 */
	public static PacketOutputManager getInstance() {
		// Check to see if the instance is null, and if so
		// create a new PacketOutputManager.
		if (instance == null) {
			instance = new PacketOutputManager();
		}
		// Now return the active instance
		return instance;
	}

	/**
	 * This method takes in an <code>SSDSDevicePacket</code> and returns the
	 * appropriate <code>PacketOutput</code> for that
	 * <code>SSDSDevicePacket</code>. The client can then serialize the packet
	 * to that PacketOutput. This is to prevent unsynchronized writes to
	 * serialization files
	 */
	public static synchronized PacketOutput getPacketOutput(
			SSDSDevicePacket ssdsDevicePacket) {
		// Call the other method using the keys from the packet
		return PacketOutputManager.getPacketOutput(ssdsDevicePacket.sourceID(),
				ssdsDevicePacket.getMetadataSequenceNumber(), ssdsDevicePacket
						.getRecordType(), ssdsDevicePacket.getPlatformID());
	}

	/**
	 * This method returns a <code>PacketOutput</code> based on the keys
	 * provided.
	 * 
	 * @param sourceID
	 * @param metadataRevisionNumber
	 * @param recordType
	 * @param platformID
	 * @return
	 */
	public static synchronized PacketOutput getPacketOutput(long sourceID,
			long metadataRevisionNumber, long recordType, long platformID) {
		logger.debug("getPacketOutput called with keys: sourceID=" + sourceID
				+ ", metadataRevisionNumber=" + metadataRevisionNumber
				+ ", recordType=" + recordType + ", platformID=" + platformID);

		// Make sure the Singleton exists
		getInstance();

		// Create the key that is the name of the file
		String packetKey = new String(sourceID + "_" + metadataRevisionNumber
				+ "_" + recordType + "_" + platformID);

		// OK, now find the packet output with that key
		PacketOutput packetOutput = null;
		packetOutput = (PacketOutput) packetOutputs.get(packetKey);

		// If it was not found, a new one should be created and added
		if (packetOutput == null) {
			logger.debug("Did not find key " + packetKey
					+ " so will create new PacketOutput and add");
			// First make sure the base directory exists
			File baseDirectory = new File(packetStorageBase);
			if (!baseDirectory.exists()) {
				logger.debug("The base directory " + packetStorageBase
						+ " does not appear to exist, will create it.");
				baseDirectory.mkdirs();
			}

			// Now create the full path name
			String packetStorageName = null;
			try {
				packetStorageName = packetStorageBase + File.separator
						+ sourceID + "_" + metadataRevisionNumber + "_"
						+ recordType + "_" + platformID;
			} catch (Exception ex) {
				packetStorageName = packetStorageBase + File.separator
						+ "unknownDevicePackets";
			}
			try {
				packetOutput = new PacketOutput(new File(packetStorageName));
			} catch (IOException e) {
				logger
						.error("Could not create a new PacketOutput for the requested packet");
			}
			if ((packetKey != null) && (packetOutput != null)) {
				packetOutputs.put(packetKey, packetOutput);
			}
		}
		logger.debug("There are now " + packetOutputs.size()
				+ " PacketOutputs in the HashMap");
		return packetOutput;
	}

	/**
	 * This method returns a PacketSQLOutput that has been created and
	 * associated with a device ID.
	 * 
	 * @param deviceID
	 * @return
	 */
	public static synchronized PacketSQLOutput getPacketSQLOutput(long deviceID) {
		// Make sure instance exits
		getInstance();

		PacketSQLOutput toReturn = null;
		toReturn = (PacketSQLOutput) packetSQLOutputs.get(new Long(deviceID));
		if (toReturn == null) {
			// Make sure that a database table has been built already
			checkForTable(deviceID);
			toReturn = new PacketSQLOutput(PacketOutputManager.dataSource,
					sqlTableDelimiter);
			packetSQLOutputs.put(new Long(deviceID), toReturn);
		}
		return toReturn;
	}

	/**
	 * This method checks to see if there is a table in the database for the
	 * specified device. If not one is created.
	 * 
	 * @param deviceID
	 * @return
	 */
	private static boolean checkForTable(long deviceID) {

		boolean tableOK = true;

		Connection connection = null;
		try {
			connection = dataSource.getConnection();
		} catch (SQLException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		} catch (Exception e4) {
			logger.error("Exception caught: " + e4.getClass().getName() + ": "
					+ e4.getMessage());
		}

		// Try to query for the table
		DatabaseMetaData dbm = null;
		try {
			dbm = connection.getMetaData();
		} catch (SQLException e1) {
			logger.error("Could not get metadata from DataSource:"
					+ e1.getMessage());
		}
		String[] tableTypes = { "TABLE" };
		ResultSet allTables = null;
		if (dbm != null) {
			try {
				allTables = dbm
						.getTables(null, null, "" + deviceID, tableTypes);
			} catch (SQLException e2) {
				logger
						.error("Could not get list of tables from database metadata: "
								+ e2.getMessage());
			}
		}
		boolean tableFound = false;
		if (allTables != null) {
			try {
				while (allTables.next()) {
					tableFound = true;
				}
			} catch (SQLException e3) {
				logger.error("Exception trying to loop over list of tables: "
						+ e3.getMessage());
			}
		}
		if (tableFound) {
			try {
				connection.close();
			} catch (SQLException e) {
				logger
						.error("Could not close the connection after finding table: "
								+ e.getMessage());
			}
			return true;
		} else {
			logger.debug("No table was found for device " + deviceID
					+ ", so will create a new one...");
			// Must create the table
			String createTableSQL = createTableSQLTemplate.replaceAll(
					"@DEVICE_ID@", deviceID + "");
			String createPrimaryKeySQL = createPrimaryKeySQLTemplate
					.replaceAll("@DEVICE_ID@", deviceID + "");
			String createTimestampIndexSQL = createTimestampIndexSQLTemplate
					.replaceAll("@DEVICE_ID@", deviceID + "");
			logger
					.debug("To create the table, the following statements will be run:\n"
							+ createTableSQL
							+ "\n"
							+ createPrimaryKeySQL
							+ "\n" + createTimestampIndexSQL);
			try {
				PreparedStatement pstmt = connection
						.prepareStatement(createTableSQL);
				pstmt.execute();
				pstmt.close();
				pstmt = connection.prepareStatement(createPrimaryKeySQL);
				pstmt.execute();
				pstmt.close();
			} catch (SQLException e) {
				logger
						.error("SQLException caught trying to create table for device "
								+ deviceID + ": " + e.getMessage());
				tableOK = false;
			}
			// Now add the index
			if (tableOK) {
				try {
					PreparedStatement indexStmt = connection
							.prepareStatement(createTimestampIndexSQL);
					indexStmt.execute();
					indexStmt.close();
				} catch (SQLException e) {
					logger
							.error("SQLException caught trying to add index for device "
									+ deviceID + ": " + e.getMessage());
					tableOK = false;
				}
			}
		}
		try {
			// Close the connection
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tableOK;
	}

}