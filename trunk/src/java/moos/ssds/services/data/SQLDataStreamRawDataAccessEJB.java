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
package moos.ssds.services.data;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import moos.ssds.io.PacketSQLInput;
import moos.ssds.transmogrify.SSDSGeoLocatedDevicePacket;
import moos.ssds.util.XmlDateFormat;

import org.apache.log4j.Logger;

/**
 * @author kgomes
 * @ejb.bean name="SQLDataStreamRawDataAccess" type="Stateless"
 *           jndi-name="moos/ssds/services/data/SQLDataStreamRawDataAccess"
 *           local-jndi-name="moos/ssds/services/data/SQLDataStreamRawDataAccessLocal"
 *           view-type="both"
 * @ejb.home create="true"
 *           local-class="moos.ssds.services.data.SQLDataStreamRawDataAccessLocalHome"
 *           remote-class="moos.ssds.services.data.SQLDataStreamRawDataAccessHome"
 * @ejb.interface create="true"
 *                local-class="moos.ssds.services.data.SQLDataStreamRawDataAccessLocal"
 *                remote-class="moos.ssds.services.data.SQLDataStreamRawDataAccess"
 */
public class SQLDataStreamRawDataAccessEJB implements SessionBean {

	/**
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException {
	}

	/**
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException {
	}

	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException, RemoteException {
	}

	/**
	 * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext arg0) throws EJBException,
			RemoteException {
	}

	/**
	 * The EJB callback that is used when the bean is created
	 */
	public void ejbCreate() throws CreateException {

		// Create and load the io properties
		ioProperties = new Properties();
		try {
			ioProperties.load(this.getClass().getResourceAsStream(
					"/moos/ssds/io/io.properties"));
		} catch (Exception e) {
			logger.error("Exception trying to read in properties file: "
					+ e.getMessage());
		}

		// Grab the template string for checking if a table exists
		this.sqlCheckTableExistsTemplate = ioProperties
				.getProperty("io.storage.sql.template.find.table");

		// Grab the template string for counting the number of rows
		this.sqlCountNumberOfRowsTemplate = ioProperties
				.getProperty("io.storage.sql.template.count.number.of.rows");

		// Grab the template string for counting the number of rows with record
		// type
		this.sqlCountNumberOfRowsWithRecordTypeTemplate = ioProperties
				.getProperty("io.storage.sql.template.count.number.of.rows.with.record.type");

		// Grab the template for finding the latest timestamp in seconds
		this.sqlLatestTimestampSecondsTemplate = ioProperties
				.getProperty("io.storage.sql.template.latest.timestamp.seconds");

		// Grab it again, but with record type clause
		this.sqlLatestTimestampSecondsWithRecordTypeTemplate = ioProperties
				.getProperty("io.storage.sql.template.latest.timestamp.seconds.with.record.type");

		// Grab the template for finding the nanoseconds portion of the latest
		// timestamp
		this.sqlLatestTimestampNanosecondsTemplate = ioProperties
				.getProperty("io.storage.sql.template.latest.timestamp.nanoseconds");

		// Grab it again, but with clause for record type
		this.sqlLatestTimestampNanosecondsWithRecordTypeTemplate = ioProperties
				.getProperty("io.storage.sql.template.latest.timestamp.nanoseconds.with.record.type");

		// Grab the template for finding the packets over a time window
		this.sqlSelectPacketsByTimeTemplate = ioProperties
				.getProperty("io.storage.sql.template.select.packets.by.time");

		// The same template, but with RecordType clause
		this.sqlSelectPacketsByTimeWithRecordTypeTemplate = ioProperties
				.getProperty("io.storage.sql.template.select.packets.by.time.with.record.type");

		// Grab the SQL table delimiter
		this.sqlTableDelimiter = ioProperties
				.getProperty("io.storage.sql.table.delimiter");

		// Grab the last number of packets pre and post ambles
		this.sqlLastNumberOfPacketsPreamble = ioProperties
				.getProperty("io.storage.sql.lastnumber.preamble");
		this.sqlLastNumberOfPacketsPostamble = ioProperties
				.getProperty("io.storage.sql.lastnumber.postamble");

		// Grab the host name for the JNDI service
		this.jndiHostName = ioProperties
				.getProperty("io.storage.sql.jndi.server.name");
		// logger.debug("jndiHostName set to: " + this.jndiHostName);
		this.dataSourceJndiName = "java:/"
				+ ioProperties.getProperty("io.storage.sql.jndi.name");
		// logger.debug("dataSourceJndiName set to " + this.dataSourceJndiName);

		// Now grab the DataSource from the JNDI
		Context jndiContext = null;
		try {
			jndiContext = new InitialContext();
			if ((this.jndiHostName != null) && (!this.jndiHostName.equals(""))) {
				jndiContext.removeFromEnvironment(Context.PROVIDER_URL);
				jndiContext.addToEnvironment(Context.PROVIDER_URL,
						this.jndiHostName + ":1099");
			}
			// logger.debug("JNDI environment = " +
			// jndiContext.getEnvironment());
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

	}

	/**
	 * @throws CreateException
	 */
	public void ejbPostCreate() throws CreateException {
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @return
	 * @throws SQLException
	 */
	public Collection getDataProducingDeviceIDs() throws SQLException {
		// The Collection to return
		Collection deviceIDs = new ArrayList();

		// Get a connection to the data source
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
		} catch (SQLException e) {
			logger.error("Could not get connection to data source:"
					+ e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getClass().getName() + ": "
					+ e.getMessage());
		}

		// Try to query for all tables in the database
		DatabaseMetaData dbm = null;
		try {
			dbm = connection.getMetaData();
		} catch (SQLException e1) {
			logger
					.error("SQLException caught trying to get DataSource metadata: "
							+ e1.getMessage());
			throw e1;
		}

		// Now try to tease out the tables from the metadata
		String[] tableTypes = { "TABLE" };
		ResultSet allTables = null;
		if (dbm != null) {
			try {
				allTables = dbm.getTables(null, null, null, tableTypes);
			} catch (SQLException e2) {
				logger.error("SQLException trying to get all tables: "
						+ e2.getMessage());
			}
		}
		if (allTables != null) {
			try {
				while (allTables.next()) {
					String tableName = allTables.getString("TABLE_NAME");
					logger.debug("Table information: " + tableName);
					// If I can convert it to a Long, add it
					Long deviceIDLong = null;
					try {
						deviceIDLong = new Long(tableName);
					} catch (NumberFormatException e) {
					}
					if ((deviceIDLong != null)
							&& (deviceIDLong.longValue() > 0)) {
						deviceIDs.add(deviceIDLong);
					}
				}
			} catch (SQLException e3) {
			}
		}
		// Close the connection
		try {
			connection.close();
		} catch (SQLException e) {
			logger.error("SQLException caught trying to close connection: "
					+ e.getMessage());
		}
		// Now return the results
		return deviceIDs;
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @return
	 * @throws SQLException
	 */
	public TreeMap getParentChildDataProducerTrees() throws SQLException {

		logger.debug("getParentChildDataProducerTrees called");
		// Get a connection to the data source
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
		} catch (SQLException e) {
			logger.error("Could not get connection to data source:"
					+ e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getClass().getName() + ": "
					+ e.getMessage());
		}

		// The collection to return
		TreeMap parentChildMap = new TreeMap();
		// Grab all the device IDs
		Collection dataProducingIDs = this.getDataProducingDeviceIDs();
		if (dataProducingIDs != null) {
			logger.debug("Got " + dataProducingIDs.size()
					+ " devices that produce data");
			Iterator deviceIDIter = dataProducingIDs.iterator();
			while (deviceIDIter.hasNext()) {
				Long deviceID = (Long) deviceIDIter.next();
				logger.debug("Working with device " + deviceID);
				// Now query for all parents
				String sqlString = "SELECT ParentID_FK FROM DeviceParent WHERE DeviceID_FK = "
						+ deviceID + " ORDER BY ParentID_FK";
				Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sqlString);
				while (rs.next()) {
					String parentID = rs.getString("ParentID_FK");
					logger.debug("parentID found and is " + parentID);
					Long parentIDLong = null;
					try {
						parentIDLong = new Long(parentID);
					} catch (NumberFormatException e) {
					}
					if (parentIDLong != null) {
						TreeSet childTreeSet = null;
						if (parentChildMap.containsKey(parentIDLong)) {
							childTreeSet = (TreeSet) parentChildMap
									.get(parentIDLong);
						} else {
							childTreeSet = new TreeSet();
							parentChildMap.put(parentIDLong, childTreeSet);
							logger.debug("New child treeset added");
						}
						childTreeSet.add(deviceID);
					}
				}
			}
		}

		// Close the connection
		try {
			connection.close();
		} catch (SQLException e) {
			logger.error("SQLException caught trying to close connection: "
					+ e.getMessage());
		}
		// Return the list
		return parentChildMap;
	}

	/**
	 * 
	 * @ejb.interface-method view-type="both"
	 * 
	 * @param deviceID
	 * @param recordType
	 * @param checkForGaps
	 * @param typeOfGap
	 * @param marginMillis
	 * @param gapSpec
	 * @param numberOfRecords
	 * @param intervalCalcStartWindow
	 * @param intervalCalcEndWindow
	 * @param gapInMillis
	 * @return
	 * @throws SQLException
	 */
	public Properties getDataStreamProperties(Long deviceID, Long recordType,
			Boolean checkForGaps, Date startGapCheckWindow,
			Date endGapCheckWindow, String typeOfGap, Long marginInMillis,
			String gapSpec, Long numberOfRecords, Date intervalCalcStartWindow,
			Date intervalCalcEndWindow, Long gapInMillis) throws SQLException {
		logger.debug("getDataStreamProperties called with:\ndeviceID="
				+ deviceID + "\nrecordType=" + recordType + "\ncheckForGaps="
				+ checkForGaps + "\nstartGapCheckWindow=" + startGapCheckWindow
				+ "\nendCheckGapWindow=" + endGapCheckWindow + "\ntypeOfGap="
				+ typeOfGap + "\nmarginInMillis=" + marginInMillis
				+ "\ngapSpec=" + gapSpec + "\nnumberOfRecords="
				+ numberOfRecords + "\nintervalCalcStartWindow="
				+ intervalCalcStartWindow + "\nintervalCalcEndWindow="
				+ intervalCalcEndWindow + "\ngapInMillis=" + gapInMillis);

		// The Properties item to return
		Properties propertiesToReturn = new Properties();

		// Verify the required parameters are real and if not, just return an
		// empty properies object
		if (deviceID == null || deviceID.longValue() == 0)
			return propertiesToReturn;

		// The first thing to do would be to check for the existence of the
		// table by querying for the number of rows
		boolean tableExists = doesDataTableExist(deviceID);

		// If the table exists, figure out the rest of the statistics
		if (tableExists) {
			// The number of rows for the device and record type
			long numberOfRows = getNumberOfRows(deviceID, recordType);
			propertiesToReturn.put(NUMBER_OF_RECORDS, "" + numberOfRows);

			// Grab the latest date for the device and record Type
			Date latestDate = this.findDatetimeOfLatestPacketReceived(deviceID,
					recordType);
			if (latestDate != null)
				propertiesToReturn.put(DATE_OF_LAST_RECORD, latestDate);

			// If the call includes gaps, find them
			if (checkForGaps != null && checkForGaps.booleanValue() == true) {
				// Grab the map of possible gaps
				TreeMap gapMap = this.findDataGaps(deviceID, recordType,
						startGapCheckWindow, endGapCheckWindow, typeOfGap,
						marginInMillis, gapSpec, numberOfRecords,
						intervalCalcStartWindow, intervalCalcEndWindow,
						gapInMillis);

				logger.debug("There are " + gapMap.keySet().size()
						+ " gaps in treemap returned");

				// XML Format
				XmlDateFormat xmlDateFormat = new XmlDateFormat();

				// Create a counter
				int gapCounter = 1;
				Iterator gapMapIter = gapMap.keySet().iterator();
				while (gapMapIter.hasNext()) {
					Date startDate = (Date) gapMapIter.next();
					Date endDate = (Date) gapMap.get(startDate);
					if (startDate != null && endDate != null) {
						propertiesToReturn.put(
								"dataGap" + gapCounter + "Start", xmlDateFormat
										.format(startDate));
						propertiesToReturn.put("dataGap" + gapCounter + "End",
								xmlDateFormat.format(endDate));
					}
				}
			}
		}
		return propertiesToReturn;
	}

	/**
	 * This method checks to see if the data table exists in the database for a
	 * specified device
	 * 
	 * @param deviceID
	 *            The device to check for a data table
	 * @return <code>false</code> if there is no table <code>true</code> if
	 *         the table exists
	 */
	private boolean doesDataTableExist(Long deviceID) throws SQLException {

		// Result to return
		boolean tableExists = false;

		// The connection to the data source that will be used
		Connection connection = null;

		// The string to use to build query
		String sqlString = null;

		// Check to see if the table exists
		if (this.sqlCheckTableExistsTemplate != null
				&& this.sqlCheckTableExistsTemplate.length() > 0) {

			// Substitute the device ID in the template text
			sqlString = this.sqlCheckTableExistsTemplate.replaceAll(
					"@DEVICE_ID@", deviceID.toString());
			try {
				// Grab a connection to the data source
				connection = dataSource.getConnection();

				// Create and run the statement
				Statement stmt = connection.createStatement();
				logger.debug("Table exists SQL = " + sqlString);
				ResultSet rs = stmt.executeQuery(sqlString);
				if (rs.next())
					tableExists = true;

				connection.close();
				logger.debug("Does device table " + deviceID + " exist? "
						+ tableExists);
			} catch (SQLException e) {
				logger
						.error("SQLException caught try to see if a table for device "
								+ deviceID + " exists: " + e.getMessage());
				connection.close();
			} catch (Exception e) {
				logger
						.error("SQLException caught try to see if a table for device "
								+ deviceID + " exists: " + e.getMessage());
				connection.close();
			}
		}
		// Return the flag
		return tableExists;
	}

	/**
	 * This method checks to see how many rows of data there are for a given
	 * device and recordType. If the recordType is not specified (null), the
	 * number of rows in the complete table is returned.
	 * 
	 * @param deviceID
	 *            The ID of the device whose data table is to be searched.
	 * @param recordType
	 *            The recordType for the device.
	 * @return
	 * @throws SQLException
	 */
	private long getNumberOfRows(Long deviceID, Long recordType)
			throws SQLException {

		// The number of rows to return
		long numberOfRows = 0;

		// If the device ID is not specified, just return 0
		if (deviceID == null)
			return numberOfRows;

		// The connection to the data source that will be used in these
		// queries
		Connection connection = null;

		// The string to use to build the various queries
		String sqlString = null;

		// Check to see if the count number of rows template was found
		if (this.sqlCountNumberOfRowsTemplate != null
				&& this.sqlCountNumberOfRowsTemplate.length() > 0
				&& this.sqlCountNumberOfRowsWithRecordTypeTemplate != null
				&& this.sqlCountNumberOfRowsWithRecordTypeTemplate.length() > 0) {

			// If the record type is defined, use that template
			if (recordType != null) {
				sqlString = this.sqlCountNumberOfRowsWithRecordTypeTemplate
						.replaceAll("@RECORD_TYPE@", ""
								+ recordType.longValue());
			} else {
				sqlString = this.sqlCountNumberOfRowsTemplate;
			}

			// Substitute the device ID in the template text
			sqlString = sqlString
					.replaceAll("@DEVICE_ID@", deviceID.toString());
			try {
				// Grab a connection to the data source
				connection = dataSource.getConnection();

				// Create and run the statement
				Statement stmt = connection.createStatement();
				logger.debug("Row count SQL = " + sqlString);
				ResultSet rs = stmt.executeQuery(sqlString);
				if (rs.next()) {
					numberOfRows = rs.getLong(1);
				}
				connection.close();
				logger.debug("There are " + numberOfRows + " for device "
						+ deviceID + " with recordType " + recordType);
			} catch (SQLException e) {
				logger.error("SQLException caught try to count "
						+ "the number of rows for device " + deviceID
						+ " and recordType " + recordType + ": "
						+ e.getMessage());
				connection.close();
			} catch (Exception e) {
				logger.error("Exception caught try to count "
						+ "the number of rows for device " + deviceID
						+ " and recordType " + recordType + ": "
						+ e.getMessage());
				connection.close();
			}
		}
		return numberOfRows;
	}

	/**
	 * This method takes in a Device ID and a Record Type and then find the most
	 * recent packet that matches that criteria. It then returns the Date of
	 * that packet
	 * 
	 * @param deviceID
	 * @param recordType
	 * @return
	 */
	private Date findDatetimeOfLatestPacketReceived(Long deviceID,
			Long recordType) throws SQLException {
		// The Date to return
		Date dateToReturn = null;

		// If the device ID is not specified, return null
		if (deviceID == null)
			return dateToReturn;

		// The number of seconds on the latest timestamp
		long latestTimestampSeconds = 0;
		long latestTimestampNanoseconds = 0;

		// The connection to the data source that will be used in these
		// queries
		Connection connection = null;

		// The string to use to build the various queries
		String sqlString = null;

		// Make sure the template text exists for timestampSeconds search
		// first
		if (this.sqlLatestTimestampSecondsTemplate != null
				&& this.sqlLatestTimestampSecondsTemplate.length() > 0
				&& this.sqlLatestTimestampSecondsWithRecordTypeTemplate != null
				&& this.sqlLatestTimestampSecondsWithRecordTypeTemplate
						.length() > 0) {

			// The current date to use for future data checks
			Date currentDate = new Date();
			logger.debug("Current Date = " + currentDate);

			// Check for the use of recordType first
			if (recordType != null) {
				sqlString = this.sqlLatestTimestampSecondsWithRecordTypeTemplate
						.replaceAll("@RECORD_TYPE@", ""
								+ recordType.longValue());
			} else {
				sqlString = this.sqlLatestTimestampSecondsTemplate;
			}

			// Lets search for the date and time in seconds of the latest
			// packet. Create the query string by substituting in the device
			// ID and the current time to only search before now
			sqlString = sqlString.replaceAll("@DEVICE_ID@", "" + deviceID);
			sqlString = sqlString.replaceAll("@CURRENT_TIMESTAMP_SECONDS@", ""
					+ (currentDate.getTime() / 1000));
			try {
				// Grab a connection to the data source
				connection = dataSource.getConnection();

				// Create and run the statement
				Statement stmt = connection.createStatement();
				logger.debug("Find max timestampSeconds SQL = " + sqlString
						+ ": stmt->" + stmt.toString());
				ResultSet rs = stmt.executeQuery(sqlString);
				if (rs.next()) {
					latestTimestampSeconds = rs.getLong("maxseconds");
				}
				connection.close();
				logger.debug("Max timestamp seconds is "
						+ latestTimestampSeconds);
			} catch (SQLException e) {
				logger.error("SQLException caught try to execute search "
						+ "for max timestamp seconds for device " + deviceID
						+ ": " + e.getMessage());
				connection.close();
			} catch (Exception e) {
				logger.error("Exception caught try to execute search "
						+ "for max timestamp seconds for device " + deviceID
						+ ": " + e.getMessage());
				connection.close();
			}
		}

		// Now let's look for nanoseconds given that the seconds was found
		if (latestTimestampSeconds > 0
				&& this.sqlLatestTimestampNanosecondsTemplate != null
				&& this.sqlLatestTimestampNanosecondsTemplate.length() > 0
				&& this.sqlLatestTimestampNanosecondsWithRecordTypeTemplate != null
				&& this.sqlLatestTimestampNanosecondsWithRecordTypeTemplate
						.length() > 0) {

			// Check for use of recordType first
			if (recordType != null) {
				sqlString = this.sqlLatestTimestampNanosecondsWithRecordTypeTemplate
						.replaceAll("@RECORD_TYPE@", ""
								+ recordType.longValue());
			} else {
				sqlString = this.sqlLatestTimestampNanosecondsTemplate;
			}

			// Subsitute time and device
			sqlString = sqlString.replaceAll("@DEVICE_ID@", "" + deviceID);
			sqlString = sqlString.replaceAll("@TIMESTAMP_SECONDS@", ""
					+ latestTimestampSeconds);
			try {
				// Grab a connection
				connection = dataSource.getConnection();

				// Now grab the latest time
				Statement stmt = connection.createStatement();
				logger.debug("Find max timestampNanoseconds SQL = " + sqlString
						+ ": stmt->" + stmt.toString());
				ResultSet rs = stmt.executeQuery(sqlString);
				if (rs.next()) {
					latestTimestampNanoseconds = rs.getLong("maxnanoseconds");
				}
				connection.close();
				logger.debug("Max timestamp nanoseconds is "
						+ latestTimestampNanoseconds);
			} catch (SQLException e) {
				logger
						.error("SQLException caught trying to query for max nanoseconds: "
								+ e.getMessage());
				connection.close();
			} catch (Exception e) {
				logger
						.error("Exception caught trying to query for max nanoseconds: "
								+ e.getMessage());
				connection.close();
			}
		}

		if (latestTimestampSeconds > 0) {
			// Now calculate a human readable date and insert that
			dateToReturn = new Date();
			dateToReturn.setTime(latestTimestampSeconds * 1000
					+ (latestTimestampNanoseconds / 1000));
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
			calendar.setTime(dateToReturn);
			XmlDateFormat xmlDateFormat = new XmlDateFormat();
			String dateString = xmlDateFormat.format(calendar.getTime());
			logger.debug("Seconds and nanos converted to date: " + dateString);
		}
		return dateToReturn;
	}

	/**
	 * 
	 * @param deviceID
	 * @param recordType
	 * @param typeOfGap
	 * @param marginMillis
	 * @param gapSpec
	 * @param numberOfRecords
	 * @param intervalCalcStartWindow
	 * @param intervalCalcEndWindow
	 * @param gapInMillis
	 * @return
	 */
	private TreeMap findDataGaps(Long deviceID, Long recordType,
			Date startCheckGapWindow, Date endCheckGapWindow, String typeOfGap,
			Long marginMillis, String gapSpec, Long numberOfRecords,
			Date intervalCalcStartWindow, Date intervalCalcEndWindow,
			Long gapInMillis) throws SQLException {
		logger.debug("findDataGaps called with:\ndeviceID=" + deviceID
				+ "\nrecordType=" + recordType + "\nstartCheckGapWindow="
				+ startCheckGapWindow + "\nendCheckGapWindow="
				+ endCheckGapWindow + "\ntypeOfGap=" + typeOfGap
				+ "\nmarginInMillis=" + marginMillis + "\ngapSpec=" + gapSpec
				+ "\nnumberOfRecords=" + numberOfRecords
				+ "\nintervalCalcStartWindow=" + intervalCalcStartWindow
				+ "\nintervalCalcEndWindow=" + intervalCalcEndWindow
				+ "\ngapInMillis=" + gapInMillis);
		// The connection to the data source that will be used in these
		// queries
		Connection connection = null;

		// The string to use to build the various queries
		String sqlString = null;

		// The TreeMap to return
		TreeMap gaps = new TreeMap();

		// These are the numbers that will be used to test for gaps
		long marginInMillisToUse = 0;
		long gapInMillisToUse = 0;

		// These are the dates that will be used in the query and we set the
		// defaults to 0 epoch seconds at the start and 5 seconds into the
		// future for the end (to allow for a bit of latency)
		Date queryStartDate = new Date();
		queryStartDate.setTime(0);
		Date queryEndDate = new Date();
		queryEndDate.setTime(queryEndDate.getTime() + 5000);

		// First, if the user specified any dates for query, set those
		if (startCheckGapWindow != null)
			queryStartDate = startCheckGapWindow;
		if (endCheckGapWindow != null)
			queryEndDate = endCheckGapWindow;

		// If the device ID is not specified, return the empty treemap
		if (deviceID == null)
			return gaps;

		// Next, does the gap criteria need to be calculated, or do we use the
		// one supplied?
		if (gapSpec.equalsIgnoreCase(USER_SPECIFIED)) {
			// If the user said they want to specify the gap criteria and didn't
			// send in a margin, simply return the empty treemap
			if (marginMillis == null)
				return gaps;
			// Same for gap specification (zero is also not a realistic spec)
			if (gapInMillis == null || gapInMillis.longValue() == 0)
				return gaps;
			// Just use the user specified criteria
			marginInMillisToUse = gapInMillis.longValue();
			gapInMillisToUse = gapInMillis.longValue();
		} else {
			// The user wants the service to calculate the values for gap and/or
			// margin
		}
		logger.debug("marginInMillisToUse=" + marginInMillisToUse);
		logger.debug("gapInMillisToUse=" + gapInMillisToUse);

		logger.debug("sqlSelectPacketByTimeTemplate="
				+ sqlSelectPacketsByTimeTemplate);
		logger.debug("sqlSelectPacketByTimeWithRecordTypeTemplate="
				+ sqlSelectPacketsByTimeWithRecordTypeTemplate);
		// Now the we have dates and gap criteria, execute the query and look
		// for gaps
		if (this.sqlSelectPacketsByTimeTemplate != null
				&& this.sqlSelectPacketsByTimeTemplate.length() > 0
				&& this.sqlSelectPacketsByTimeWithRecordTypeTemplate != null
				&& this.sqlSelectPacketsByTimeWithRecordTypeTemplate.length() > 0) {

			// If the record type is specified, use that template
			if (recordType != null) {
				sqlString = this.sqlSelectPacketsByTimeWithRecordTypeTemplate
						.replaceAll("@RECORD_TYPE@", ""
								+ recordType.longValue());
			} else {
				sqlString = this.sqlSelectPacketsByTimeTemplate;
			}

			// Replace the device ID
			sqlString = sqlString.replaceAll("@DEVICE_ID@", ""
					+ deviceID.longValue());

			// Replace date and times
			sqlString = sqlString.replaceAll(
					"@START_TIMESTAMP_WINDOW_SECONDS@", ""
							+ queryStartDate.getTime() / 1000);
			sqlString = sqlString.replaceAll("@END_TIMESTAMP_WINDOW_SECONDS@",
					"" + queryEndDate.getTime() / 1000);

			// Now run the query
			try {
				// Grab a connection
				connection = dataSource.getConnection();

				// Create the statement
				Statement stmt = connection.createStatement();
				logger.debug("Find last number of packets SQL = " + sqlString
						+ ": stmt->" + stmt.toString());
				ResultSet rs = stmt.executeQuery(sqlString);

				// Loop over the results
				int sampleCounter = 0;
				int gapCounter = 0;
				long previousMillis = 0;
				while (rs.next()) {
					// Bump the sample counter
					sampleCounter++;

					// Grab the timestamp seconds
					long tempTimestampSeconds = rs.getLong("timestampSeconds");
					long tempTimestampNanoseconds = rs
							.getLong("timestampNanoseconds");
					// Figure out the millis from record
					long rsTimestampInMillis = ((tempTimestampSeconds * 1000) + (tempTimestampNanoseconds / 1000));

					// If this is not the first sample, add the difference
					// from the previous sample
					if (sampleCounter > 1) {
						// Calculate the interval from the last previous
						// sample
						long currentGapInMillis = Math.abs(rsTimestampInMillis
								- previousMillis);
						logger.debug("Calcuated gap in millis is "
								+ currentGapInMillis);

						// If the gap is more than the specified gap + margin,
						// mark it as a gap
						if (currentGapInMillis > (gapInMillisToUse + marginInMillisToUse)) {
							logger
									.debug("It appears that "
											+ currentGapInMillis
											+ " is greater than the gapInMillisToUse + marginInMillisToUse ("
											+ gapInMillisToUse
											+ " + "
											+ marginInMillisToUse
											+ " = "
											+ (gapInMillisToUse + marginInMillisToUse)
											+ ")");
							// Increment gap counter
							gapCounter++;
							Date startGapDate = new Date();
							startGapDate.setTime(previousMillis);
							Date endGapDate = new Date();
							endGapDate.setTime(rsTimestampInMillis);
							logger.debug("Putting gap " + sampleCounter);
							gaps.put(startGapDate, endGapDate);
						}
					}

					// Assign it as the previous timestamp
					previousMillis = rsTimestampInMillis;
				}

				// Close the connection
				connection.close();

			} catch (SQLException e) {
				logger
						.error("SQLException caught trying to calculate "
								+ "the average time between samples: "
								+ e.getMessage());
				// Close the connection
				connection.close();
			} catch (Exception e) {
				logger.error("Exception caught: " + e.getClass().getName()
						+ ": " + e.getMessage());
				// Close the connection
				connection.close();
			}
		}
		// Return the TreeMap
		return gaps;
	}

	/**
	 * Note: This method returns a TreeMap with either sequence numbers or
	 * timestamps as the key and a <b><code>Collection</code></b> of
	 * SSDSDevicePackets (or data buffers depending on the input parameters) as
	 * the corresponding value
	 * 
	 * @ejb.interface-method view-type="both"
	 * @param deviceID
	 * @param startParentID
	 * @param endParentID
	 * @param startPacketType
	 * @param endPacketType
	 * @param startPacketSubType
	 * @param endPacketSubType
	 * @param startDataDescriptionID
	 * @param endDataDescriptionID
	 * @param startDataDescriptionVersion
	 * @param endDataDescriptionVersion
	 * @param startTimestampSeconds
	 * @param endTimestampSeconds
	 * @param startTimestampNanoseconds
	 * @param endTimestampNanoseconds
	 * @param startSequenceNumber
	 * @param endSequenceNumber
	 * @param startLatitude
	 * @param endLatitude
	 * @param startLongitude
	 * @param endLongitude
	 * @param startDepth
	 * @param endDepth
	 * @param orderBy
	 * @param returnAsSSDSDevicePackets
	 * @return
	 */
	public TreeMap getSortedRawData(Long deviceID, Long startParentID,
			Long endParentID, Integer startPacketType, Integer endPacketType,
			Long startPacketSubType, Long endPacketSubType,
			Long startDataDescriptionID, Long endDataDescriptionID,
			Long startDataDescriptionVersion, Long endDataDescriptionVersion,
			Long startTimestampSeconds, Long endTimestampSeconds,
			Long startTimestampNanoseconds, Long endTimestampNanoseconds,
			Long startSequenceNumber, Long endSequenceNumber,
			Long lastNumberOfPackets, Double startLatitude, Double endLatitude,
			Double startLongitude, Double endLongitude, Float startDepth,
			Float endDepth, String orderBy, boolean returnAsSSDSDevicePackets)
			throws SQLException {

		// First check to see if the deviceID is specified (it must be)
		if (deviceID == null) {
			throw new SQLException("The deviceID must be specified");
		}

		// Create a PacketSQLInput
		PacketSQLInput packetSQLInput = new PacketSQLInput();
		// Set the right SQL delimiter
		packetSQLInput.setSqlTableDelimiter(this.sqlTableDelimiter);

		// Set the pre and post ambles for last number of packets query
		packetSQLInput
				.setSqlLastNumberOfPacketsPreamble(this.sqlLastNumberOfPacketsPreamble);
		packetSQLInput
				.setSqlLastNumberOfPacketsPostamble(this.sqlLastNumberOfPacketsPostamble);

		// Set all the values
		packetSQLInput.setDeviceID(deviceID.longValue());
		if (startParentID != null) {
			packetSQLInput.setStartParentID(startParentID.longValue());
		} else {
			packetSQLInput.setStartParentID(PacketSQLInput.MISSING_VALUE);
		}
		if (endParentID != null) {
			packetSQLInput.setEndParentID(endParentID.longValue());
		} else {
			packetSQLInput.setEndParentID(PacketSQLInput.MISSING_VALUE);
		}
		if (startPacketType != null) {
			packetSQLInput.setStartPacketType(startPacketType.intValue());
		} else {
			packetSQLInput.setStartPacketType(PacketSQLInput.MISSING_VALUE);
		}
		if (endPacketType != null) {
			packetSQLInput.setEndPacketType(endPacketType.intValue());
		} else {
			packetSQLInput.setEndPacketType(PacketSQLInput.MISSING_VALUE);
		}
		if (startPacketSubType != null) {
			packetSQLInput
					.setStartPacketSubType(startPacketSubType.longValue());
		} else {
			packetSQLInput.setStartPacketSubType(PacketSQLInput.MISSING_VALUE);
		}
		if (endPacketSubType != null) {
			packetSQLInput.setEndPacketSubType(endPacketSubType.longValue());
		} else {
			packetSQLInput.setEndPacketSubType(PacketSQLInput.MISSING_VALUE);
		}
		if (startDataDescriptionID != null) {
			packetSQLInput.setStartDataDescriptionID(startDataDescriptionID
					.longValue());
		} else {
			packetSQLInput
					.setStartDataDescriptionID(PacketSQLInput.MISSING_VALUE);
		}
		if (endDataDescriptionID != null) {
			packetSQLInput.setEndDataDescriptionID(endDataDescriptionID
					.longValue());
		} else {
			packetSQLInput
					.setEndDataDescriptionID(PacketSQLInput.MISSING_VALUE);
		}
		if (startDataDescriptionVersion != null) {
			packetSQLInput
					.setStartDataDescriptionVersion(startDataDescriptionVersion
							.longValue());
		} else {
			packetSQLInput
					.setStartDataDescriptionVersion(PacketSQLInput.MISSING_VALUE);
		}
		if (endDataDescriptionVersion != null) {
			packetSQLInput
					.setEndDataDescriptionVersion(endDataDescriptionVersion
							.longValue());
		} else {
			packetSQLInput
					.setEndDataDescriptionVersion(PacketSQLInput.MISSING_VALUE);
		}
		if (startTimestampSeconds != null) {
			packetSQLInput.setStartTimestampSeconds(startTimestampSeconds
					.longValue());
		} else {
			packetSQLInput
					.setStartTimestampSeconds(PacketSQLInput.MISSING_VALUE);
		}
		if (endTimestampSeconds != null) {
			packetSQLInput.setEndTimestampSeconds(endTimestampSeconds
					.longValue());
		} else {
			packetSQLInput.setEndTimestampSeconds(PacketSQLInput.MISSING_VALUE);
		}
		if (startTimestampNanoseconds != null) {
			packetSQLInput
					.setStartTimestampNanoseconds(startTimestampNanoseconds
							.longValue());
		} else {
			packetSQLInput
					.setStartTimestampNanoseconds(PacketSQLInput.MISSING_VALUE);
		}
		if (endTimestampNanoseconds != null) {
			packetSQLInput.setEndTimestampNanoseconds(endTimestampNanoseconds
					.longValue());
		} else {
			packetSQLInput
					.setEndTimestampNanoseconds(PacketSQLInput.MISSING_VALUE);
		}
		if (startSequenceNumber != null) {
			packetSQLInput.setStartSequenceNumber(startSequenceNumber
					.longValue());
		} else {
			packetSQLInput.setStartSequenceNumber(PacketSQLInput.MISSING_VALUE);
		}
		if (endSequenceNumber != null) {
			packetSQLInput.setEndSequenceNumber(endSequenceNumber.longValue());
		} else {
			packetSQLInput.setEndSequenceNumber(PacketSQLInput.MISSING_VALUE);
		}
		if (lastNumberOfPackets != null) {
			packetSQLInput.setLastNumberOfPackets(lastNumberOfPackets
					.longValue());
		} else {
			packetSQLInput.setLastNumberOfPackets(PacketSQLInput.MISSING_VALUE);
		}
		if (startLatitude != null) {
			packetSQLInput.setStartLatitude(startLatitude.doubleValue());
		} else {
			packetSQLInput.setStartLatitude(PacketSQLInput.MISSING_VALUE);
		}
		if (endLatitude != null) {
			packetSQLInput.setEndLatitude(endLatitude.doubleValue());
		} else {
			packetSQLInput.setEndLatitude(PacketSQLInput.MISSING_VALUE);
		}
		if (startLongitude != null) {
			packetSQLInput.setStartLongitude(startLatitude.doubleValue());
		} else {
			packetSQLInput.setStartLongitude(PacketSQLInput.MISSING_VALUE);
		}
		if (endLongitude != null) {
			packetSQLInput.setEndLongitude(endLatitude.doubleValue());
		} else {
			packetSQLInput.setEndLongitude(PacketSQLInput.MISSING_VALUE);
		}
		if (startDepth != null) {
			packetSQLInput.setStartDepth(startDepth.floatValue());
		} else {
			packetSQLInput.setStartDepth(PacketSQLInput.MISSING_VALUE);
		}
		if (endDepth != null) {
			packetSQLInput.setEndDepth(endDepth.floatValue());
		} else {
			packetSQLInput.setEndDepth(PacketSQLInput.MISSING_VALUE);
		}
		if ((orderBy == null)
				|| (!orderBy
						.equals(SQLDataStreamRawDataAccessEJB.BY_SEQUENCE_NUMBER) && !orderBy
						.equals(SQLDataStreamRawDataAccessEJB.BY_TIMESTAMP))) {
			orderBy = SQLDataStreamRawDataAccessEJB.BY_TIMESTAMP;
		}
		// This is the TreeMap that will be returned
		TreeMap treeMapToReturn = new TreeMap();

		// Now start looking through the results
		while (packetSQLInput.hasMoreElements()) {
			SSDSGeoLocatedDevicePacket ssdsDevicePacket = (SSDSGeoLocatedDevicePacket) packetSQLInput
					.nextElement();

			// If there is a packet, add it correctly
			if (ssdsDevicePacket != null) {
				// First figure out what the key will be (timestamp or sequence
				// number)
				Long key = null;
				if (orderBy
						.equalsIgnoreCase(SQLDataStreamRawDataAccessEJB.BY_SEQUENCE_NUMBER)) {
					key = new Long(ssdsDevicePacket.sequenceNo());
				} else {
					key = new Long(ssdsDevicePacket.systemTime());
				}
				// Now we have the key, check to see if there is a collection
				// associated with the key yet
				Collection valueCollection = null;
				if (treeMapToReturn.containsKey(key)) {
					valueCollection = (Collection) treeMapToReturn.get(key);
				}
				// If the value collection is null, create a new one
				if (valueCollection == null) {
					valueCollection = new ArrayList();
					treeMapToReturn.put(key, valueCollection);
				}

				// Now we have the collection of values and the key, add it
				if (returnAsSSDSDevicePackets) {
					valueCollection.add(ssdsDevicePacket);
				} else {
					valueCollection.add(ssdsDevicePacket.getDataBuffer());
				}
			}
		}
		// Close the PacketSQLInput
		packetSQLInput.close();
		// Now return the results
		return treeMapToReturn;
	}

	/**
	 * These are the constants that define the types of sorting and filtering
	 * can be done by this service
	 */
	public static final String BY_SEQUENCE_NUMBER = "sequenceNumber";
	public static final String BY_TIMESTAMP = "timestamp";

	/**
	 * Some constants to define what properties are available
	 */
	public static final String NUMBER_OF_RECORDS = "numRecords";
	public static final String DATE_OF_LAST_RECORD = "lastRecordDate";
	public static final String AVERAGE_SAMPLE_INTERVAL_IN_MILLIS = "averageSampleIntervalInMillis";
	public static final String TIME_ONLY_GAP = "timeGap";
	public static final String SEQ_ONLY_GAP = "seqGap";
	public static final String TIME_SEQ_GAP = "timeSeqGap";
	public static final String SERVICE_CALCULATED = "serviceCalculated";
	public static final String USER_SPECIFIED = "userSpecified";

	/**
	 * This is the <code>DataSource</code> that the EJB will use to interact
	 * with the SSDS_Data database
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
	 * The IO Properties for the PacketInput/Output
	 */
	private static Properties ioProperties = null;

	/**
	 * This is the string that is used to delimit the table name in the SQL
	 * query
	 */
	private String sqlTableDelimiter = null;

	/**
	 * This is the SQL to execute to see if the table for a certain device
	 * exists in the database. It is a template because the code will have to
	 * substitute the device ID in this string before the query is executed. The
	 * template is read from an external properties file.
	 */
	private String sqlCheckTableExistsTemplate = null;

	/**
	 * This is a string that will be read from a properties file that will then
	 * be used to build a query to count the number of rows for a specific
	 * device. It is a template because the code will have to substitute the
	 * device ID in this string before the query is executed
	 */
	private String sqlCountNumberOfRowsTemplate = null;

	/**
	 * This is a string that will be read from a properties file that will then
	 * be used to build a query to count the number of rows for a specific
	 * device and record type. It is a template because the code will have to
	 * substitute the device ID and record type before executing the query.
	 */
	private String sqlCountNumberOfRowsWithRecordTypeTemplate = null;

	/**
	 * This is a string that will be read from a properties file that will
	 * contain some template text for the SQL statement to find the latest
	 * timestamp for a particular device
	 */
	private String sqlLatestTimestampSecondsTemplate = null;

	/**
	 * Same as sqlLatestTimestampSecondsTemplate, but with another clause to
	 * allow specification of Record Type
	 */
	private String sqlLatestTimestampSecondsWithRecordTypeTemplate = null;

	/**
	 * This is a string that will be read from a properites file that will
	 * contain some template text for the SQL statement to find the latest
	 * nanosecond portion of the latest timestamp
	 */
	private String sqlLatestTimestampNanosecondsTemplate = null;

	/**
	 * Same as sqlLatestTimestampNanosecondsTemplate, but with another clause to
	 * allow specification of Record Type
	 */
	private String sqlLatestTimestampNanosecondsWithRecordTypeTemplate = null;

	/**
	 * This is a string that will be read from a properties file that will
	 * contain some template text that will be used to construct a query for
	 * packets within a time window
	 */
	private String sqlSelectPacketsByTimeTemplate = null;

	/**
	 * Same as sqlSelectPacketsByTimeTemplate, but with RecordType clause
	 */
	private String sqlSelectPacketsByTimeWithRecordTypeTemplate = null;

	private String sqlLastNumberOfPacketsPreamble = null;
	private String sqlLastNumberOfPacketsPostamble = null;

	/** A log4j logger */
	static Logger logger = Logger
			.getLogger(SQLDataStreamRawDataAccessEJB.class);

}