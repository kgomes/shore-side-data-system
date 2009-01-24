// $Header: /home/cvs/ssds/src/java/moos/ssds/io/PacketInput.java,v 1.20
// 2005/04/25 15:32:02 kgomes Exp $

package moos.ssds.io;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import moos.ssds.transmogrify.SSDSGeoLocatedDevicePacket;

import org.apache.log4j.Logger;

/**
 * <p>
 * This class provides access to the raw data in a DataStream that was collected
 * by SSDS and stored in a relational database. It supports different query
 * arguments that, in general, if left <code>null</code> will not be used in
 * the query. Also, note that some parameters have starts and ends and if the
 * end is left null, only the start value will be queried for. Otherwise an
 * inclusive query (includes starts and ends) will be performed. The parameters
 * are:
 * <ul>
 * <li>deviceID</li>
 * <li>startParentID</li>
 * <li>endParentID</li>
 * <li>startPacketType</li>
 * <li>endPacketType</li>
 * <li>startPacketSubType</li>
 * <li>endPacketSubType</li>
 * <li>startDataDescriptionID</li>
 * <li>endDataDescriptionID</li>
 * <li>startDataDescriptionVersion</li>
 * <li>endDataDescriptionVersion</li>
 * <li>startTimestampSeconds</li>
 * <li>endTimestampSeconds</li>
 * <li>startTimestampNanoseconds</li>
 * <li>endTimestampNanoseconds</li>
 * <li>startSequenceNumber</li>
 * <li>endSequenceNumber</li>
 * <li>startLatitude</li>
 * <li>endLatitude</li>
 * <li>startLongitude</li>
 * <li>endLongitude</li>
 * <li>startDepth</li>
 * <li>endDepth</li>
 * </ul>
 * There are also some options that can be used to affect the query and the
 * returns. Those options are:
 * <ul>
 * <li>orderBy</li>
 * SequenceNumber|Timestamp(s)
 * <li>returnAsDevicePackets</li>
 * will return TreeMap of SSDSDevicePackets (versus a TreeMap of just the byte
 * array (first byte array)) with timestamp/sequenceNumber as key.
 * <li></li>
 * <li></li>
 * </ul>
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.6 $
 */
public class PacketSQLInput implements Enumeration {

	/**
	 * No argument constructor. If created with this you will need to call the
	 * <code>setDataSource(DataSource dataSource)</code> method before calling
	 * <code>readObject</code>
	 */
	public PacketSQLInput() {
		this(null, PacketSQLInput.MISSING_VALUE, "`");
	}

	/**
	 * This constructor takes in the DataSource that will be used to query data
	 * from and the ID of the device that will be queried for
	 * 
	 * @param dataSource
	 * @param deviceID
	 */
	public PacketSQLInput(DataSource dataSource, long deviceID,
			String sqlTableDelimiter) {
		this.setDataSource(dataSource);
		this.setDeviceID(deviceID);
		this.setSqlTableDelimiter(sqlTableDelimiter);
	}

	/**
	 * This is the constructor that takes in several strings to setup a database
	 * connection. This is for times when you want to use the
	 * <code>PacketSQLInput</code> outside of a J2EE container.
	 * 
	 * @param databaseDriverClassName
	 * @param databaseJDBCUrl
	 * @param username
	 * @param password
	 * @param deviceID
	 * @throws ClassNotFoundException
	 */
	public PacketSQLInput(String databaseDriverClassName,
			String databaseJDBCUrl, String username, String password,
			long deviceID, String sqlTableDelimiter) throws SQLException,
			ClassNotFoundException {
		// First check that incoming parameters are OK
		if ((databaseDriverClassName == null) || (databaseJDBCUrl == null)
				|| (username == null) || (password == null))
			throw new SQLException(
					"One of the constructor parameters was not specified, all four must be.");

		// Set the flag for a direct connection
		this.directConnection = true;

		// Load the DB driver
		Class.forName(databaseDriverClassName);

		this.setSqlTableDelimiter(sqlTableDelimiter);
	}

	/**
	 * @return Returns the ds.
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * @param ds
	 *            The ds to set.
	 */
	public void setDataSource(DataSource dataSource) {
		// Set the flag to turn off the direct connection
		this.directConnection = false;
		// If the data source is null, look up one using the local properties
		if (dataSource == null) {
			Properties ioProperties = null;
			// Create and load the io properties
			ioProperties = new Properties();
			try {
				ioProperties.load(this.getClass().getResourceAsStream(
						"/moos/ssds/io/io.properties"));
			} catch (Exception e) {
				logger.error("Exception trying to read in properties file: "
						+ e.getMessage());
			}
			// Grab JNDI stuff
			String jndiHostName = ioProperties
					.getProperty("io.storage.sql.jndi.server");
			String dataSourceJndiName = "java:/"
					+ ioProperties.getProperty("io.storage.sql.jndi.name");

			// Now grab the DataSource from the JNDI
			Context jndiContext = null;
			try {
				jndiContext = new InitialContext();
				if ((jndiHostName != null) && (!jndiHostName.equals(""))) {
					jndiContext.removeFromEnvironment(Context.PROVIDER_URL);
					jndiContext.addToEnvironment(Context.PROVIDER_URL,
							jndiHostName + ":1099");
				}
				// logger.debug("JNDI environment = "
				// + jndiContext.getEnvironment());
			} catch (NamingException ne) {
				logger
						.error("!!--> A naming exception was caught while trying "
								+ "to get an initial context: "
								+ ne.getMessage());
				return;
			} catch (Exception e) {
				logger
						.error("!!--> An unknown exception was caught while trying "
								+ "to get an initial context: "
								+ e.getMessage());
				return;
			}
			try {
				this.dataSource = (DataSource) jndiContext
						.lookup(dataSourceJndiName);
			} catch (NamingException e1) {
				logger.error("Could not get DataSource: " + e1.getMessage());
			}

		} else {
			this.dataSource = dataSource;
		}
		this.paramsChanged = true;
	}

	public String getSqlTableDelimiter() {
		return sqlTableDelimiter;
	}

	public void setSqlTableDelimiter(String sqlTableDelimiter) {
		this.sqlTableDelimiter = sqlTableDelimiter;
	}

	public String getSqlLastNumberOfPacketsPreamble() {
		return sqlLastNumberOfPacketsPreamble;
	}

	public void setSqlLastNumberOfPacketsPreamble(
			String sqlLastNumberOfPacketsPreamble) {
		this.sqlLastNumberOfPacketsPreamble = sqlLastNumberOfPacketsPreamble;
	}

	public String getSqlLastNumberOfPacketsPostamble() {
		return sqlLastNumberOfPacketsPostamble;
	}

	public void setSqlLastNumberOfPacketsPostamble(
			String sqlLastNumberOfPacketsPostamble) {
		this.sqlLastNumberOfPacketsPostamble = sqlLastNumberOfPacketsPostamble;
	}

	/**
	 * @return Returns the deviceID.
	 */
	public long getDeviceID() {
		if (deviceID != null) {
			return deviceID.longValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * @param deviceID
	 *            The deviceID to set.
	 */
	public void setDeviceID(long deviceID) {
		if (deviceID == PacketSQLInput.MISSING_VALUE) {
			this.deviceID = null;
		} else {
			this.deviceID = new Long(deviceID);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the startParentID.
	 */
	public long getStartParentID() {
		if (startParentID != null) {
			return startParentID.longValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * @param startParentID
	 *            The parentID to set.
	 */
	public void setStartParentID(long startParentID) {
		if (startParentID == PacketSQLInput.MISSING_VALUE) {
			this.startParentID = null;
		} else {
			this.startParentID = new Long(startParentID);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the endParentID.
	 */
	public long getEndParentID() {
		if (endParentID != null) {
			return endParentID.longValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * @param endParentID
	 *            The parentID to set.
	 */
	public void setEndParentID(long endParentID) {
		if (endParentID == PacketSQLInput.MISSING_VALUE) {
			this.endParentID = null;
		} else {
			this.endParentID = new Long(endParentID);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the startPacketType.
	 */
	public int getStartPacketType() {
		if (startPacketType != null) {
			return startPacketType.intValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * @param startPacketType
	 *            The packetType to set.
	 */
	public void setStartPacketType(int startPacketType) {
		if (startPacketType == PacketSQLInput.MISSING_VALUE) {
			this.startPacketType = null;
		} else {
			this.startPacketType = new Integer(startPacketType);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the endPacketType.
	 */
	public int getEndPacketType() {
		if (endPacketType != null) {
			return endPacketType.intValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * @param endPacketType
	 *            The packetType to set.
	 */
	public void setEndPacketType(int endPacketType) {
		if (endPacketType == PacketSQLInput.MISSING_VALUE) {
			this.endPacketType = null;
		} else {
			this.endPacketType = new Integer(endPacketType);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the startPacketSubType.
	 */
	public long getStartPacketSubType() {
		if (startPacketSubType != null) {
			return startPacketSubType.longValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * @param startPacketSubType
	 *            The packetSubType to set.
	 */
	public void setStartPacketSubType(long startPacketSubType) {
		if (startPacketSubType == PacketSQLInput.MISSING_VALUE) {
			this.startPacketSubType = null;
		} else {
			this.startPacketSubType = new Long(startPacketSubType);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the endPacketSubType.
	 */
	public long getEndPacketSubType() {
		if (endPacketSubType != null) {
			return endPacketSubType.longValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * @param endPacketSubType
	 *            The packetSubType to set.
	 */
	public void setEndPacketSubType(long endPacketSubType) {
		if (endPacketSubType == PacketSQLInput.MISSING_VALUE) {
			this.endPacketSubType = null;
		} else {
			this.endPacketSubType = new Long(endPacketSubType);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the startDataDescriptionID.
	 */
	public long getStartDataDescriptionID() {
		if (startDataDescriptionID != null) {
			return startDataDescriptionID.longValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * @param startDataDescriptionID
	 *            The dataDescriptionID to set.
	 */
	public void setStartDataDescriptionID(long startDataDescriptionID) {
		if (startDataDescriptionID == PacketSQLInput.MISSING_VALUE) {
			this.startDataDescriptionID = null;
		} else {
			this.startDataDescriptionID = new Long(startDataDescriptionID);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the endDataDescriptionID.
	 */
	public long getEndDataDescriptionID() {
		if (endDataDescriptionID != null) {
			return endDataDescriptionID.longValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * @param endDataDescriptionID
	 *            The dataDescriptionID to set.
	 */
	public void setEndDataDescriptionID(long endDataDescriptionID) {
		if (endDataDescriptionID == PacketSQLInput.MISSING_VALUE) {
			this.endDataDescriptionID = null;
		} else {
			this.endDataDescriptionID = new Long(endDataDescriptionID);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the startDataDescriptionVersion.
	 */
	public long getStartDataDescriptionVersion() {
		if (startDataDescriptionVersion != null) {
			return startDataDescriptionVersion.longValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * @param startDataDescriptionVersion
	 *            The dataDescriptionVersion to set.
	 */
	public void setStartDataDescriptionVersion(long startDataDescriptionVersion) {
		if (startDataDescriptionVersion == PacketSQLInput.MISSING_VALUE) {
			this.startDataDescriptionVersion = null;
		} else {
			this.startDataDescriptionVersion = new Long(
					startDataDescriptionVersion);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the endDataDescriptionVersion.
	 */
	public long getEndDataDescriptionVersion() {
		if (endDataDescriptionVersion != null) {
			return endDataDescriptionVersion.longValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * @param endDataDescriptionVersion
	 *            The dataDescriptionVersion to set.
	 */
	public void setEndDataDescriptionVersion(long endDataDescriptionVersion) {
		if (endDataDescriptionVersion == PacketSQLInput.MISSING_VALUE) {
			this.endDataDescriptionVersion = null;
		} else {
			this.endDataDescriptionVersion = new Long(endDataDescriptionVersion);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the startTimestampSeconds.
	 */
	public long getStartTimestampSeconds() {
		if (this.startTimestampSeconds != null) {
			return this.startTimestampSeconds.longValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * @param startTimestampSeconds
	 *            The timestampSeconds to set.
	 */
	public void setStartTimestampSeconds(long startTimestampSeconds) {
		if (startTimestampSeconds == PacketSQLInput.MISSING_VALUE) {
			this.startTimestampSeconds = null;
		} else {
			this.startTimestampSeconds = new Long(startTimestampSeconds);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the endTimestampSeconds.
	 */
	public long getEndTimestampSeconds() {
		if (this.endTimestampSeconds != null) {
			return this.endTimestampSeconds.longValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * @param endTimestampSeconds
	 *            The timestampSeconds to set.
	 */
	public void setEndTimestampSeconds(long endTimestampSeconds) {
		if (endTimestampSeconds == PacketSQLInput.MISSING_VALUE) {
			this.endTimestampSeconds = null;
		} else {
			this.endTimestampSeconds = new Long(endTimestampSeconds);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the startTimestampNanoseconds.
	 */
	public long getStartTimestampNanoseconds() {
		if (this.startTimestampNanoseconds != null) {
			return this.startTimestampNanoseconds.longValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * @param startTimestampNanoseconds
	 *            The timestampNanoseconds to set.
	 */
	public void setStartTimestampNanoseconds(long startTimestampNanoseconds) {
		if (startTimestampNanoseconds == PacketSQLInput.MISSING_VALUE) {
			this.startTimestampNanoseconds = null;
		} else {
			this.startTimestampNanoseconds = new Long(startTimestampNanoseconds);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the endTimestampNanoseconds.
	 */
	public long getEndTimestampNanoseconds() {
		if (this.endTimestampNanoseconds != null) {
			return this.endTimestampNanoseconds.longValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * @param endTimestampNanoseconds
	 *            The timestampNanoseconds to set.
	 */
	public void setEndTimestampNanoseconds(long endTimestampNanoseconds) {
		if (endTimestampNanoseconds == PacketSQLInput.MISSING_VALUE) {
			this.endTimestampNanoseconds = null;
		} else {
			this.endTimestampNanoseconds = new Long(endTimestampNanoseconds);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the startSequenceNumber.
	 */
	public long getStartSequenceNumber() {
		if (this.startSequenceNumber != null) {
			return this.startSequenceNumber.longValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * @param startSequenceNumber
	 *            The sequenceNumber to set.
	 */
	public void setStartSequenceNumber(long startSequenceNumber) {
		if (startSequenceNumber == PacketSQLInput.MISSING_VALUE) {
			this.startSequenceNumber = null;
		} else {
			this.startSequenceNumber = new Long(startSequenceNumber);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the endSequenceNumber.
	 */
	public long getEndSequenceNumber() {
		if (this.endSequenceNumber != null) {
			return this.endSequenceNumber.longValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * @param startSequenceNumber
	 *            The sequenceNumber to set.
	 */
	public void setEndSequenceNumber(long endSequenceNumber) {
		if (endSequenceNumber == PacketSQLInput.MISSING_VALUE) {
			this.endSequenceNumber = null;
		} else {
			this.endSequenceNumber = new Long(endSequenceNumber);
		}
		this.paramsChanged = true;
	}

	/**
	 * This is the method that returns the number of packets to be retrieved
	 * from the end of the data stream
	 * 
	 * @return
	 */
	public long getLastNumberOfPackets() {
		if (this.lastNumberOfPackets != null) {
			return this.lastNumberOfPackets.longValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * This method sets the number of packets to be retrieved from the end of
	 * the data stream.
	 * 
	 * @param lastNumberOfPackets
	 */
	public void setLastNumberOfPackets(long lastNumberOfPackets) {
		if (lastNumberOfPackets == PacketSQLInput.MISSING_VALUE) {
			this.lastNumberOfPackets = null;
		} else {
			this.lastNumberOfPackets = new Long(lastNumberOfPackets);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the startLatitude.
	 */
	public double getStartLatitude() {
		if (this.startLatitude == null) {
			return PacketSQLInput.MISSING_VALUE;
		} else {
			return this.startLatitude.doubleValue();
		}
	}

	/**
	 * @param startLatitude
	 *            The startLatitude to set.
	 */
	public void setStartLatitude(double startLatitude) {
		if (startLatitude == PacketSQLInput.MISSING_VALUE) {
			this.startLatitude = null;
		} else {
			this.startLatitude = new Double(startLatitude);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the endLatitude.
	 */
	public double getEndLatitude() {
		if (this.endLatitude == null) {
			return PacketSQLInput.MISSING_VALUE;
		} else {
			return this.endLatitude.doubleValue();
		}
	}

	/**
	 * @param endLatitude
	 *            The endLatitude to set.
	 */
	public void setEndLatitude(double endLatitude) {
		if (endLatitude == PacketSQLInput.MISSING_VALUE) {
			this.endLatitude = null;
		} else {
			this.endLatitude = new Double(endLatitude);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the startLongitude.
	 */
	public double getStartLongitude() {
		if (this.startLongitude == null) {
			return PacketSQLInput.MISSING_VALUE;
		} else {
			return startLongitude.doubleValue();
		}
	}

	/**
	 * @param startLongitude
	 *            The startLongitude to set.
	 */
	public void setStartLongitude(double startLongitude) {
		if (startLongitude == PacketSQLInput.MISSING_VALUE) {
			this.startLongitude = null;
		} else {
			this.startLongitude = new Double(startLongitude);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the endLongitude.
	 */
	public double getEndLongitude() {
		if (this.endLongitude == null) {
			return PacketSQLInput.MISSING_VALUE;
		} else {
			return endLongitude.doubleValue();
		}
	}

	/**
	 * @param endLongitude
	 *            The endLongitude to set.
	 */
	public void setEndLongitude(double endLongitude) {
		if (endLongitude == PacketSQLInput.MISSING_VALUE) {
			this.endLongitude = null;
		} else {
			this.endLongitude = new Double(endLongitude);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the startDepth.
	 */
	public float getStartDepth() {
		if (this.startDepth == null) {
			return PacketSQLInput.MISSING_VALUE;
		} else {
			return startDepth.floatValue();
		}
	}

	/**
	 * @param startDepth
	 *            The startDepth to set.
	 */
	public void setStartDepth(float startDepth) {
		if (startDepth == PacketSQLInput.MISSING_VALUE) {
			this.startDepth = null;
		} else {
			this.startDepth = new Float(startDepth);
		}
		this.paramsChanged = true;
	}

	/**
	 * @return Returns the endDepth.
	 */
	public float getEndDepth() {
		if (this.endDepth != null) {
			return this.endDepth.floatValue();
		} else {
			return PacketSQLInput.MISSING_VALUE;
		}
	}

	/**
	 * @param endDepth
	 *            The endDepth to set.
	 */
	public void setEndDepth(float endDepth) {
		if (endDepth == PacketSQLInput.MISSING_VALUE) {
			this.endDepth = null;
		} else {
			this.endDepth = new Float(endDepth);
		}
		this.paramsChanged = true;
	}

	/**
	 * This method runs the query using the parameters stored and then holds the
	 * result set
	 */
	private void queryForData() throws SQLException {
		if (this.deviceID == null) {
			throw new SQLException("The DeviceID was not specified.");
		}
		// Close the old connection
		if (this.connection != null)
			this.connection.close();

		// Grab a new connection
		if (!directConnection) {
			this.connection = this.dataSource.getConnection();
		} else {
			this.connection = DriverManager.getConnection(this.databaseJDBCUrl,
					this.username, this.password);
		}

		// Clear the no more data flag
		noMoreData = false;

		// A boolean to track if WHERE was added
		boolean whereAdded = false;

		// Build up the query
		StringBuffer queryString = new StringBuffer();
		queryString.append("SELECT * FROM ");
		if (this.lastNumberOfPackets != null) {
			// First replace any tags in preamble with number of packets and
			// then append
			queryString.append((this.sqlLastNumberOfPacketsPreamble.replaceAll(
					"@LAST_NUMBER_OF_PACKETS@", this.lastNumberOfPackets + ""))
					+ " ");
			// queryString.append("(SELECT TOP "
			// + this.lastNumberOfPackets.longValue() + " * FROM ");
		}
		queryString.append(this.sqlTableDelimiter + this.deviceID.longValue()
				+ this.sqlTableDelimiter);

		// Add all contraints
		if (this.startParentID != null) {
			if (!whereAdded) {
				queryString.append(" WHERE");
				whereAdded = true;
			}
			if (this.endParentID != null) {
				queryString.append(" parentID >= "
						+ this.startParentID.longValue() + " AND parentID <= "
						+ this.endParentID.longValue());
			} else {
				queryString.append(" parentID = "
						+ this.startParentID.longValue());
			}
		}
		// Now check for packetType clause
		if (startPacketType != null) {
			// Add where if not added
			if (!whereAdded) {
				queryString.append(" WHERE");
				whereAdded = true;
			} else {
				queryString.append(" AND");
			}
			if (endPacketType != null) {
				queryString.append(" packetType >= "
						+ startPacketType.intValue() + " AND packetType <= "
						+ endPacketType.intValue());
			} else {
				queryString.append(" packetType = "
						+ startPacketType.intValue());
			}
		}
		// Now for packetSubType
		if (startPacketSubType != null) {
			// Add where if not added
			if (!whereAdded) {
				queryString.append(" WHERE");
				whereAdded = true;
			} else {
				queryString.append(" AND");
			}
			if (endPacketSubType != null) {
				queryString.append(" packetSubType >= "
						+ startPacketSubType.longValue()
						+ " AND packetSubType <= "
						+ endPacketSubType.longValue());
			} else {
				queryString.append(" packetSubType = "
						+ startPacketSubType.longValue());
			}
		}
		// Now for the dataDescriptionID
		if (startDataDescriptionID != null) {
			// Add where if not added
			if (!whereAdded) {
				queryString.append(" WHERE");
				whereAdded = true;
			} else {
				queryString.append(" AND");
			}
			if (endDataDescriptionID != null) {
				queryString.append(" dataDescriptionID >= "
						+ startDataDescriptionID.longValue()
						+ " AND dataDescriptionID <= "
						+ endDataDescriptionID.longValue());
			} else {
				queryString.append(" dataDescriptionID = "
						+ startDataDescriptionID.longValue());
			}
		}
		// Now for the dataDescriptionVersion
		if (startDataDescriptionVersion != null) {
			// Add where if not added
			if (!whereAdded) {
				queryString.append(" WHERE");
				whereAdded = true;
			} else {
				queryString.append(" AND");
			}
			if (endDataDescriptionVersion != null) {
				queryString.append(" dataDescriptionVersion >= "
						+ startDataDescriptionVersion.longValue()
						+ " AND dataDescriptionVersion <= "
						+ endDataDescriptionVersion.longValue());
			} else {
				queryString.append(" dataDescriptionVersion = "
						+ startDataDescriptionVersion.longValue());
			}
		}
		// Now for the timestampSeconds
		if (startTimestampSeconds != null) {
			// Add where if not added
			if (!whereAdded) {
				queryString.append(" WHERE");
				whereAdded = true;
			} else {
				queryString.append(" AND");
			}
			if (endTimestampSeconds != null) {
				queryString.append(" timestampSeconds >= "
						+ startTimestampSeconds.longValue()
						+ " AND timestampSeconds <= "
						+ endTimestampSeconds.longValue());
			} else {
				queryString.append(" timestampSeconds = "
						+ startTimestampSeconds.longValue());
			}
		}

		// Now for the timestampNanoseconds
		// TODO KJG 2006-02-8 I removed the nanoseconds part because it doesn't
		// make any sense in the query side of things. You would only use these
		// if you were querying within the same second.

		// Now for the sequenceNumber
		if (startSequenceNumber != null) {
			// Add where if not added
			if (!whereAdded) {
				queryString.append(" WHERE");
				whereAdded = true;
			} else {
				queryString.append(" AND");
			}
			if (endSequenceNumber != null) {
				queryString.append(" sequenceNumber >= "
						+ startSequenceNumber.longValue()
						+ " AND sequenceNumber <= "
						+ endSequenceNumber.longValue());
			} else {
				queryString.append(" sequenceNumber = "
						+ startSequenceNumber.longValue());
			}
		}
		// Now for the latitude
		if (startLatitude != null) {
			// Add where if not added
			if (!whereAdded) {
				queryString.append(" WHERE");
				whereAdded = true;
			} else {
				queryString.append(" AND");
			}
			if (endLatitude != null) {
				queryString.append(" latitude >= "
						+ startLatitude.doubleValue() + " AND latitude <= "
						+ endLatitude.doubleValue());
			} else {
				queryString
						.append(" latitude = " + startLatitude.doubleValue());
			}
		}
		// Now for the longitude
		if (startLongitude != null) {
			// Add where if not added
			if (!whereAdded) {
				queryString.append(" WHERE");
				whereAdded = true;
			} else {
				queryString.append(" AND");
			}
			if (endLongitude != null) {
				queryString.append(" longitude >= "
						+ startLongitude.doubleValue() + " AND longitude <= "
						+ endLongitude.doubleValue());
			} else {
				queryString.append(" longitude = "
						+ startLongitude.doubleValue());
			}
		}
		// Now for the depth
		if (startDepth != null) {
			// Add where if not added
			if (!whereAdded) {
				queryString.append(" WHERE");
				whereAdded = true;
			} else {
				queryString.append(" AND");
			}
			if (endDepth != null) {
				queryString.append(" depth >= " + startDepth.floatValue()
						+ " AND depth <= " + endDepth.floatValue());
			} else {
				queryString.append(" depth = " + startDepth.floatValue());
			}
		}

		// Now add some ordering stuff
		if (this.lastNumberOfPackets != null) {
			// Replace postamble with last number of packets and append
			queryString.append(" "
					+ (this.sqlLastNumberOfPacketsPostamble.replaceAll(
							"@LAST_NUMBER_OF_PACKETS@",
							this.lastNumberOfPackets + "")));
			// queryString
			// .append(" ORDER BY timestampSeconds DESC, timestampNanoseconds
			// DESC) DERIVEDTBL");
		}
		queryString.append(" ORDER BY timestampSeconds, timestampNanoseconds");

		// Now let's run it
		logger.debug("SQL statement is: " + queryString.toString());

		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(queryString.toString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pstmt != null) {
			resultSet = null;
			try {
				resultSet = pstmt.executeQuery();
			} catch (SQLException e) {
				logger
						.info("SQLException caught trying to read from device table "
								+ this.deviceID.longValue()
								+ ": "
								+ e.getMessage());
			}
		}
		// Changed the flag to indicate that the parameters are done changing
		this.paramsChanged = false;
	}

	/**
	 * This method returns a boolean that indicates if there are more packets
	 * that can be read from the source. If <code>true</code>, the caller
	 * should be able to call <code>nextElement</code> to retrieve another
	 * packet.
	 * 
	 * @return a <code>boolean</code> that indicates if more packets can be
	 *         read from the source. More can be read if <code>true</code>,
	 *         none if <code>false</code>.
	 */
	public boolean hasMoreElements() {
		// Set the return to false as the default
		boolean ok = false;
		// First check to see if the parameters were changed and
		// run the query again if they have
		if (this.paramsChanged) {
			try {
				this.queryForData();
			} catch (SQLException e) {
				logger.error("SQLException caught trying to re-run query: "
						+ e.getMessage());
			}
		}
		if ((!noMoreData) && (resultSet != null)) {
			try {
				if (resultSet.isLast()) {
					ok = false;
				} else {
					ok = true;
				}
			} catch (SQLException e) {
				logger
						.error("SQLException caught trying to go to next, then previous row: "
								+ e.getMessage());
			}
		} else {
			ok = false;
		}
		return ok;
	}

	/**
	 * This method closes the results and connections.
	 */
	public void close() {
		try {
			// Close the result set
			if (resultSet != null)
				resultSet.close();
			// Close the connection
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			logger.error("SQLException caught trying to close: "
					+ e.getMessage());
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
		}
	}

	/**
	 * This method implement the nextElement() method from the
	 * <code>Enumeration</code> interface. When called, it returns the next
	 * available object from the packet stream.
	 * 
	 * @return An object that should be an instance of an SSDSDevicePacket
	 *         Object, it can return null if no object was available
	 */
	public Object nextElement() {
		if (paramsChanged) {
			try {
				this.queryForData();
			} catch (SQLException e) {
				logger
						.error("SQLException caught trying to go to nextElement, then previous row: "
								+ e.getMessage());
			}
		}
		// The object to return
		Object obj = null;
		// If there are results, read the next object
		if (resultSet != null) {
			obj = readObject();
		}
		// Now return it
		return obj;
	}

	/**
	 * This method is called to read an object from the stream. It checks the
	 * version from the stream and then calls the appropriate read of the
	 * correct version.
	 * 
	 * @return An object that is an <code>SSDSDevicePacket</code> that is read
	 *         from the serialized packet stream. A null is returned if no
	 *         packet was available.
	 * @throws IOException
	 *             if something goes wrong with the read
	 */
	public Object readObject() {
		if (paramsChanged) {
			try {
				this.queryForData();
			} catch (SQLException e) {
				logger
						.error("SQLException caught trying to go to nextElement, then previous row: "
								+ e.getMessage());
			}
		}
		// The object to return
		Object obj = null;
		try {
			// Advance cursor and see if there is something to return
			if (resultSet.next()) {
				// Read in the version from the input stream
				int tmpVersionID = resultSet.getInt("ssdsPacketVersion");
				// Now based on the version value, call the appropriate read
				// method
				switch (tmpVersionID) {
				case 3:
					obj = readVersion3();
					break;
				}
			} else {
				noMoreData = true;
			}
		} catch (SQLException e) {
			logger.error("SQLException caught trying to readObject: "
					+ e.getMessage());
		}
		// Return the object
		return obj;
	}

	/**
	 * This is the method that reads packets from the input stream that were
	 * serialized in the version 3 format of packet. This method assumes that
	 * the object will be read from the current location of the result set
	 * cursor
	 * 
	 * @return an Object that is an <code>SSDSDevicePacket</code> that
	 *         conforms to the third version of packet structure
	 */
	private Object readVersion3() throws SQLException {
		// Create a new packet
		int bufferLen = resultSet.getInt("bufferLen");
		int bufferTwoLen = resultSet.getInt("bufferTwoLen");
		SSDSGeoLocatedDevicePacket packet = new SSDSGeoLocatedDevicePacket(
				this.deviceID.longValue(), bufferLen);
		packet.setPlatformID(resultSet.getLong("parentID"));
		int ssdsPacketType = resultSet.getInt("packetType");
		int packetType = -1;
		if (ssdsPacketType == 0) {
			packetType = 1;
		} else if (ssdsPacketType == 1) {
			packetType = 0;
		} else if (ssdsPacketType == 4) {
			packetType = 2;
		}
		packet.setPacketType(packetType);
		packet.setRecordType(resultSet.getLong("packetSubType"));
		packet
				.setMetadataSequenceNumber(resultSet
						.getLong("dataDescriptionID"));
		packet.setDataDescriptionVersion(resultSet
				.getLong("dataDescriptionVersion"));
		packet.setSystemTime((resultSet.getLong("timestampSeconds") * 1000)
				+ (resultSet.getLong("timestampNanoseconds") / 1000));
		packet.setSequenceNo(resultSet.getLong("sequenceNumber"));
		// Now pull the Blob
		Blob bufferOneBlob = resultSet.getBlob("bufferBytes");
		packet.setDataBuffer(bufferOneBlob.getBytes(1, bufferLen));
		Blob bufferTwoBlob = resultSet.getBlob("bufferTwoBytes");
		packet.setOtherBuffer(bufferTwoBlob.getBytes(1, bufferTwoLen));

		// Set the geo coordinates
		packet.setLatitude(resultSet.getDouble("latitude"));
		packet.setLongitude(resultSet.getDouble("longitude"));
		packet.setDepth(resultSet.getFloat("depth"));

		// Return the packet
		return packet;
	}

	/**
	 * These variables are used to control the query
	 */
	private int packetVersion = 3;
	// The ID of the device to find the data for
	private Long deviceID = null;
	// The start and end range of parent IDs
	private Long startParentID = null;
	private Long endParentID = null;
	// The start and end range of packetType
	private Integer startPacketType = null;
	private Integer endPacketType = null;
	// The start and end range of packetSubType
	private Long startPacketSubType = null;
	private Long endPacketSubType = null;
	// The start and end range of the DataDescriptionID
	private Long startDataDescriptionID = null;
	private Long endDataDescriptionID = null;
	// The start and end range of the DataDecriptionVersion
	private Long startDataDescriptionVersion = null;
	private Long endDataDescriptionVersion = null;
	// The start and end range of the TimestampSeconds
	private Long startTimestampSeconds = null;
	private Long endTimestampSeconds = null;
	// The start and end range of the TimestampNanoseconds
	private Long startTimestampNanoseconds = null;
	private Long endTimestampNanoseconds = null;
	// The start and end range of the SequenceNumber
	private Long startSequenceNumber = null;
	private Long endSequenceNumber = null;
	// This is the number of packets back that the selection is to grab
	private Long lastNumberOfPackets = null;
	// The latitude that the packet was aquired at
	private Double startLatitude = null;
	private Double endLatitude = null;
	// The longitude that the packet was aquired at
	private Double startLongitude = null;
	private Double endLongitude = null;
	// The depth the packet was aquired at
	private Float startDepth = null;
	private Float endDepth = null;

	/**
	 * This is the value that will be returned if any of the parameter objects
	 * are null
	 */
	public static final int MISSING_VALUE = -999999;

	/**
	 * This is the DataSource that the packet will be read from
	 */
	private DataSource dataSource = null;

	/* *************************************************** */
	/* * These parameters are for direct DB connections ** */
	/* *************************************************** */
	private String databaseDriverClassName = null;
	private String databaseJDBCUrl = null;
	private String username = null;
	private String password = null;
	// This is a boolean that tells the object if this is supposed to be a
	// direct connection to the database or not
	private boolean directConnection = false;
	/* ************** End direct DB connection *********** */
	/* *************************************************** */

	/**
	 * This is the actual connection to the database used
	 */
	private Connection connection = null;

	/**
	 * This is the result set that contains the database rows
	 */
	private ResultSet resultSet = null;

	/**
	 * Boolean to indicate if any of the query variables have changed since the
	 * last time it was queried
	 */
	private boolean paramsChanged = true;

	/**
	 * A boolean to indicate there is not more data
	 */
	private boolean noMoreData = false;

	/**
	 * This is the delimiter that is used in queries to delimit the table name
	 * since they are device IDs. The default is set to the MySQL one of `, but
	 * it can be changed in the constructor
	 */
	private String sqlTableDelimiter = "`";

	/**
	 * These are the SQL fragments that will be inserted to select by last
	 * number of packets. These are DB specific they need to be set before
	 * using.
	 */
	private String sqlLastNumberOfPacketsPreamble = null;
	private String sqlLastNumberOfPacketsPostamble = null;

	/** A log4j logger */
	static Logger logger = Logger.getLogger(PacketSQLInput.class);
}