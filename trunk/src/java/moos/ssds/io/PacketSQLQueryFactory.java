package moos.ssds.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PacketSQLQueryFactory {
	/** A log4j logger */
	static Logger logger = Logger.getLogger(PacketSQLQuery.class);

	/**
	 * These are the variables that are used to construct the SELECT part of the
	 * clause
	 */
	public static final String DEVICE_ID = "deviceID";
	public static final String PARENT_ID = "parentID";
	public static final String PACKET_TYPE = "packetType";
	public static final String PACKET_SUB_TYPE = "packetSubType";
	public static final String DATA_DESCRIPTION_ID = "dataDescriptionID";
	public static final String DATA_DESCRIPTION_VERSION = "dataDescriptionVersion";
	public static final String TIMESTAMP_SECONDS = "timestampSeconds";
	public static final String TIMESTAMP_NANOSECONDS = "timestampNanoseconds";
	public static final String SEQUENCE_NUMBER = "sequenceNumber";
	public static final String BUFFER_LEN = "bufferLen";
	public static final String BUFFER_BYTES = "bufferBytes";
	public static final String BUFFER_TWO_LEN = "bufferTwoLen";
	public static final String BUFFER_TWO_BYTES = "bufferTwoBytes";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String DEPTH = "depth";

	/**
	 * This is an array of string that defines what will be in the SELECT
	 * statement and the order in which the are selected and returned
	 */
	private String[] selectParametersAndOrder = null;

	/**
	 * This is a list of available parameters to use for select. This is a
	 * convenience so the add method can make sure it is a valid parameter.
	 */
	private List<String> availableParameters = null;

	/**
	 * This is an array of phrases that will be used to build the ORDER BY
	 * clause
	 */
	private String[] orderByParameters = null;

	/**
	 * This is a flag to indicate the default order by parameters are being used
	 */
	private boolean defaultOrderByParameters = true;

	/**
	 * This is the device for which the queries will be created. It translates
	 * into the table name in the FROM clause
	 */
	private Long deviceID = null;

	/**
	 * These variables are used to construct the WHERE clause of the query
	 */
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
	// The latitude that the packet was acquired at
	private Double startLatitude = null;
	private Double endLatitude = null;
	// The longitude that the packet was acquired at
	private Double startLongitude = null;
	private Double endLongitude = null;
	// The depth the packet was acquired at
	private Float startDepth = null;
	private Float endDepth = null;

	/**
	 * This is the value that will be returned if any of the parameter objects
	 * are null
	 */
	public static final int MISSING_VALUE = -999999;

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

	/**
	 * This is the default constructor
	 */
	public PacketSQLQueryFactory(Long deviceID) {
		// Set the device ID
		this.setDeviceID(deviceID);

		// Construct the list of parameters
		availableParameters = new ArrayList<String>();
		availableParameters.add(DEVICE_ID);
		availableParameters.add(PARENT_ID);
		availableParameters.add(PACKET_TYPE);
		availableParameters.add(PACKET_SUB_TYPE);
		availableParameters.add(DATA_DESCRIPTION_ID);
		availableParameters.add(DATA_DESCRIPTION_VERSION);
		availableParameters.add(TIMESTAMP_SECONDS);
		availableParameters.add(TIMESTAMP_NANOSECONDS);
		availableParameters.add(SEQUENCE_NUMBER);
		availableParameters.add(BUFFER_LEN);
		availableParameters.add(BUFFER_BYTES);
		availableParameters.add(BUFFER_TWO_LEN);
		availableParameters.add(BUFFER_TWO_BYTES);
		availableParameters.add(LATITUDE);
		availableParameters.add(LONGITUDE);
		availableParameters.add(DEPTH);

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

		// Clear the order by parameters and set to default
		this.clearOrderByParameters();
	}

	/**
	 * This method clears out all the select parameters that will be used in the
	 * query
	 */
	public void clearSelectParameters() {
		selectParametersAndOrder = null;
	}

	/**
	 * This method adds a parameter to the list of parameters to be returned in
	 * the SELECT query
	 * 
	 * @param selectParameter
	 */
	public void addSelectParameter(String selectParameter) {
		if (selectParameter != null && !selectParameter.equals("")
				&& availableParameters.contains(selectParameter)) {
			// Create an array list so it can be expanded
			List<String> temporaryList = null;
			if (selectParametersAndOrder == null) {
				temporaryList = new ArrayList<String>();
			} else {
				temporaryList = Arrays.asList(selectParametersAndOrder);
			}
			// Add the parameter
			temporaryList.add(selectParameter);
			// Now convert back to array
			selectParametersAndOrder = temporaryList
					.toArray(new String[temporaryList.size()]);
		}
	}

	/**
	 * This method clears out all the order by parameters that will be used in
	 * the query. It sets up the default to be timestampSeconds and
	 * timestampNanoseconds ascending as the default
	 */
	public void clearOrderByParameters() {
		orderByParameters = null;
		this.addOrderByParameter(TIMESTAMP_SECONDS, false);
		this.addOrderByParameter(TIMESTAMP_NANOSECONDS, false);
		defaultOrderByParameters = true;
	}

	/**
	 * This method adds a parameter to order the result by. The boolean
	 * indicates if it should be descending (true) or ascending (false)
	 * 
	 * @param orderByParameter
	 *            the parameter to order the results by
	 * @param isDescending
	 *            whether or not that sorting should be done in descending
	 *            (true) or ascending (false) order
	 */
	public void addOrderByParameter(String orderByParameter,
			boolean isDescending) {
		if (orderByParameter != null && !orderByParameter.equals("")
				&& availableParameters.contains(orderByParameter)) {
			// Create an array list so it can be expanded
			List<String> temporaryList = null;
			if (defaultOrderByParameters) {
				temporaryList = new ArrayList<String>();
				defaultOrderByParameters = false;
			} else {
				temporaryList = Arrays.asList(orderByParameters);
			}
			// Add the parameter
			if (isDescending) {
				temporaryList.add(orderByParameter + " DESC");
			} else {
				temporaryList.add(orderByParameter + " ASC");
			}
			// Now convert back to array
			orderByParameters = temporaryList.toArray(new String[temporaryList
					.size()]);
		}
	}

	/**
	 * @return Returns the deviceID.
	 */
	public long getDeviceID() {
		if (deviceID != null) {
			return deviceID.longValue();
		} else {
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * @param deviceID
	 *            The deviceID to set.
	 */
	private void setDeviceID(long deviceID) {
		if (deviceID == PacketSQLQuery.MISSING_VALUE) {
			this.deviceID = null;
		} else {
			this.deviceID = new Long(deviceID);
		}
	}

	/**
	 * @return Returns the startParentID.
	 */
	public long getStartParentID() {
		if (startParentID != null) {
			return startParentID.longValue();
		} else {
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * @param startParentID
	 *            The parentID to set.
	 */
	public void setStartParentID(long startParentID) {
		if (startParentID == PacketSQLQuery.MISSING_VALUE) {
			this.startParentID = null;
		} else {
			this.startParentID = new Long(startParentID);
		}
	}

	/**
	 * @return Returns the endParentID.
	 */
	public long getEndParentID() {
		if (endParentID != null) {
			return endParentID.longValue();
		} else {
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * @param endParentID
	 *            The parentID to set.
	 */
	public void setEndParentID(long endParentID) {
		if (endParentID == PacketSQLQuery.MISSING_VALUE) {
			this.endParentID = null;
		} else {
			this.endParentID = new Long(endParentID);
		}
	}

	/**
	 * @return Returns the startPacketType.
	 */
	public int getStartPacketType() {
		if (startPacketType != null) {
			return startPacketType.intValue();
		} else {
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * @param startPacketType
	 *            The packetType to set.
	 */
	public void setStartPacketType(int startPacketType) {
		if (startPacketType == PacketSQLQuery.MISSING_VALUE) {
			this.startPacketType = null;
		} else {
			this.startPacketType = new Integer(startPacketType);
		}
	}

	/**
	 * @return Returns the endPacketType.
	 */
	public int getEndPacketType() {
		if (endPacketType != null) {
			return endPacketType.intValue();
		} else {
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * @param endPacketType
	 *            The packetType to set.
	 */
	public void setEndPacketType(int endPacketType) {
		if (endPacketType == PacketSQLQuery.MISSING_VALUE) {
			this.endPacketType = null;
		} else {
			this.endPacketType = new Integer(endPacketType);
		}
	}

	/**
	 * @return Returns the startPacketSubType.
	 */
	public long getStartPacketSubType() {
		if (startPacketSubType != null) {
			return startPacketSubType.longValue();
		} else {
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * @param startPacketSubType
	 *            The packetSubType to set.
	 */
	public void setStartPacketSubType(long startPacketSubType) {
		if (startPacketSubType == PacketSQLQuery.MISSING_VALUE) {
			this.startPacketSubType = null;
		} else {
			this.startPacketSubType = new Long(startPacketSubType);
		}
	}

	/**
	 * @return Returns the endPacketSubType.
	 */
	public long getEndPacketSubType() {
		if (endPacketSubType != null) {
			return endPacketSubType.longValue();
		} else {
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * @param endPacketSubType
	 *            The packetSubType to set.
	 */
	public void setEndPacketSubType(long endPacketSubType) {
		if (endPacketSubType == PacketSQLQuery.MISSING_VALUE) {
			this.endPacketSubType = null;
		} else {
			this.endPacketSubType = new Long(endPacketSubType);
		}
	}

	/**
	 * @return Returns the startDataDescriptionID.
	 */
	public long getStartDataDescriptionID() {
		if (startDataDescriptionID != null) {
			return startDataDescriptionID.longValue();
		} else {
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * @param startDataDescriptionID
	 *            The dataDescriptionID to set.
	 */
	public void setStartDataDescriptionID(long startDataDescriptionID) {
		if (startDataDescriptionID == PacketSQLQuery.MISSING_VALUE) {
			this.startDataDescriptionID = null;
		} else {
			this.startDataDescriptionID = new Long(startDataDescriptionID);
		}
	}

	/**
	 * @return Returns the endDataDescriptionID.
	 */
	public long getEndDataDescriptionID() {
		if (endDataDescriptionID != null) {
			return endDataDescriptionID.longValue();
		} else {
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * @param endDataDescriptionID
	 *            The dataDescriptionID to set.
	 */
	public void setEndDataDescriptionID(long endDataDescriptionID) {
		if (endDataDescriptionID == PacketSQLQuery.MISSING_VALUE) {
			this.endDataDescriptionID = null;
		} else {
			this.endDataDescriptionID = new Long(endDataDescriptionID);
		}
	}

	/**
	 * @return Returns the startDataDescriptionVersion.
	 */
	public long getStartDataDescriptionVersion() {
		if (startDataDescriptionVersion != null) {
			return startDataDescriptionVersion.longValue();
		} else {
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * @param startDataDescriptionVersion
	 *            The dataDescriptionVersion to set.
	 */
	public void setStartDataDescriptionVersion(long startDataDescriptionVersion) {
		if (startDataDescriptionVersion == PacketSQLQuery.MISSING_VALUE) {
			this.startDataDescriptionVersion = null;
		} else {
			this.startDataDescriptionVersion = new Long(
					startDataDescriptionVersion);
		}
	}

	/**
	 * @return Returns the endDataDescriptionVersion.
	 */
	public long getEndDataDescriptionVersion() {
		if (endDataDescriptionVersion != null) {
			return endDataDescriptionVersion.longValue();
		} else {
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * @param endDataDescriptionVersion
	 *            The dataDescriptionVersion to set.
	 */
	public void setEndDataDescriptionVersion(long endDataDescriptionVersion) {
		if (endDataDescriptionVersion == PacketSQLQuery.MISSING_VALUE) {
			this.endDataDescriptionVersion = null;
		} else {
			this.endDataDescriptionVersion = new Long(endDataDescriptionVersion);
		}
	}

	/**
	 * @return Returns the startTimestampSeconds.
	 */
	public long getStartTimestampSeconds() {
		if (this.startTimestampSeconds != null) {
			return this.startTimestampSeconds.longValue();
		} else {
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * @param startTimestampSeconds
	 *            The timestampSeconds to set.
	 */
	public void setStartTimestampSeconds(long startTimestampSeconds) {
		if (startTimestampSeconds == PacketSQLQuery.MISSING_VALUE) {
			this.startTimestampSeconds = null;
		} else {
			this.startTimestampSeconds = new Long(startTimestampSeconds);
		}
	}

	/**
	 * @return Returns the endTimestampSeconds.
	 */
	public long getEndTimestampSeconds() {
		if (this.endTimestampSeconds != null) {
			return this.endTimestampSeconds.longValue();
		} else {
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * @param endTimestampSeconds
	 *            The timestampSeconds to set.
	 */
	public void setEndTimestampSeconds(long endTimestampSeconds) {
		if (endTimestampSeconds == PacketSQLQuery.MISSING_VALUE) {
			this.endTimestampSeconds = null;
		} else {
			this.endTimestampSeconds = new Long(endTimestampSeconds);
		}
	}

	/**
	 * @return Returns the startTimestampNanoseconds.
	 */
	public long getStartTimestampNanoseconds() {
		if (this.startTimestampNanoseconds != null) {
			return this.startTimestampNanoseconds.longValue();
		} else {
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * @param startTimestampNanoseconds
	 *            The timestampNanoseconds to set.
	 */
	public void setStartTimestampNanoseconds(long startTimestampNanoseconds) {
		if (startTimestampNanoseconds == PacketSQLQuery.MISSING_VALUE) {
			this.startTimestampNanoseconds = null;
		} else {
			this.startTimestampNanoseconds = new Long(startTimestampNanoseconds);
		}
	}

	/**
	 * @return Returns the endTimestampNanoseconds.
	 */
	public long getEndTimestampNanoseconds() {
		if (this.endTimestampNanoseconds != null) {
			return this.endTimestampNanoseconds.longValue();
		} else {
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * @param endTimestampNanoseconds
	 *            The timestampNanoseconds to set.
	 */
	public void setEndTimestampNanoseconds(long endTimestampNanoseconds) {
		if (endTimestampNanoseconds == PacketSQLQuery.MISSING_VALUE) {
			this.endTimestampNanoseconds = null;
		} else {
			this.endTimestampNanoseconds = new Long(endTimestampNanoseconds);
		}
	}

	/**
	 * @return Returns the startSequenceNumber.
	 */
	public long getStartSequenceNumber() {
		if (this.startSequenceNumber != null) {
			return this.startSequenceNumber.longValue();
		} else {
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * @param startSequenceNumber
	 *            The sequenceNumber to set.
	 */
	public void setStartSequenceNumber(long startSequenceNumber) {
		if (startSequenceNumber == PacketSQLQuery.MISSING_VALUE) {
			this.startSequenceNumber = null;
		} else {
			this.startSequenceNumber = new Long(startSequenceNumber);
		}
	}

	/**
	 * @return Returns the endSequenceNumber.
	 */
	public long getEndSequenceNumber() {
		if (this.endSequenceNumber != null) {
			return this.endSequenceNumber.longValue();
		} else {
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * @param startSequenceNumber
	 *            The sequenceNumber to set.
	 */
	public void setEndSequenceNumber(long endSequenceNumber) {
		if (endSequenceNumber == PacketSQLQuery.MISSING_VALUE) {
			this.endSequenceNumber = null;
		} else {
			this.endSequenceNumber = new Long(endSequenceNumber);
		}
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
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * This method sets the number of packets to be retrieved from the end of
	 * the data stream.
	 * 
	 * @param lastNumberOfPackets
	 */
	public void setLastNumberOfPackets(long lastNumberOfPackets) {
		if (lastNumberOfPackets == PacketSQLQuery.MISSING_VALUE) {
			this.lastNumberOfPackets = null;
		} else {
			this.lastNumberOfPackets = new Long(lastNumberOfPackets);
		}
	}

	/**
	 * @return Returns the startLatitude.
	 */
	public double getStartLatitude() {
		if (this.startLatitude == null) {
			return PacketSQLQuery.MISSING_VALUE;
		} else {
			return this.startLatitude.doubleValue();
		}
	}

	/**
	 * @param startLatitude
	 *            The startLatitude to set.
	 */
	public void setStartLatitude(double startLatitude) {
		if (startLatitude == PacketSQLQuery.MISSING_VALUE) {
			this.startLatitude = null;
		} else {
			this.startLatitude = new Double(startLatitude);
		}
	}

	/**
	 * @return Returns the endLatitude.
	 */
	public double getEndLatitude() {
		if (this.endLatitude == null) {
			return PacketSQLQuery.MISSING_VALUE;
		} else {
			return this.endLatitude.doubleValue();
		}
	}

	/**
	 * @param endLatitude
	 *            The endLatitude to set.
	 */
	public void setEndLatitude(double endLatitude) {
		if (endLatitude == PacketSQLQuery.MISSING_VALUE) {
			this.endLatitude = null;
		} else {
			this.endLatitude = new Double(endLatitude);
		}
	}

	/**
	 * @return Returns the startLongitude.
	 */
	public double getStartLongitude() {
		if (this.startLongitude == null) {
			return PacketSQLQuery.MISSING_VALUE;
		} else {
			return startLongitude.doubleValue();
		}
	}

	/**
	 * @param startLongitude
	 *            The startLongitude to set.
	 */
	public void setStartLongitude(double startLongitude) {
		if (startLongitude == PacketSQLQuery.MISSING_VALUE) {
			this.startLongitude = null;
		} else {
			this.startLongitude = new Double(startLongitude);
		}
	}

	/**
	 * @return Returns the endLongitude.
	 */
	public double getEndLongitude() {
		if (this.endLongitude == null) {
			return PacketSQLQuery.MISSING_VALUE;
		} else {
			return endLongitude.doubleValue();
		}
	}

	/**
	 * @param endLongitude
	 *            The endLongitude to set.
	 */
	public void setEndLongitude(double endLongitude) {
		if (endLongitude == PacketSQLQuery.MISSING_VALUE) {
			this.endLongitude = null;
		} else {
			this.endLongitude = new Double(endLongitude);
		}
	}

	/**
	 * @return Returns the startDepth.
	 */
	public float getStartDepth() {
		if (this.startDepth == null) {
			return PacketSQLQuery.MISSING_VALUE;
		} else {
			return startDepth.floatValue();
		}
	}

	/**
	 * @param startDepth
	 *            The startDepth to set.
	 */
	public void setStartDepth(float startDepth) {
		if (startDepth == PacketSQLQuery.MISSING_VALUE) {
			this.startDepth = null;
		} else {
			this.startDepth = new Float(startDepth);
		}
	}

	/**
	 * @return Returns the endDepth.
	 */
	public float getEndDepth() {
		if (this.endDepth != null) {
			return this.endDepth.floatValue();
		} else {
			return PacketSQLQuery.MISSING_VALUE;
		}
	}

	/**
	 * @param endDepth
	 *            The endDepth to set.
	 */
	public void setEndDepth(float endDepth) {
		if (endDepth == PacketSQLQuery.MISSING_VALUE) {
			this.endDepth = null;
		} else {
			this.endDepth = new Float(endDepth);
		}
	}

	/**
	 * This method returns the query string that will be used to find the values
	 * of interest that were constructed using this factory
	 * 
	 * @return
	 */
	public String getQueryStatement() {
		return constructSelectStatement() + constructWhereClause()
				+ constructOrderByClause();
	}

	/**
	 * This method takes the parameters listed in the String array and
	 * constructs the appropriate SELECT clause
	 * 
	 * @return
	 */
	private String constructSelectStatement() {
		// First check to see if the select parameter array is empty or null. If
		// that is the case, assume all parameters in the default order are
		// requested
		StringBuilder selectBuilder = new StringBuilder();

		// Add the select
		selectBuilder.append("SELECT ");

		// Add the columns to select
		if (selectParametersAndOrder == null
				|| selectParametersAndOrder.length == 0) {
			selectBuilder.append("*");
		} else {
			for (int i = 0; i < selectParametersAndOrder.length; i++) {
				selectBuilder.append(selectParametersAndOrder[i]);
				if (i != (selectParametersAndOrder.length - 1))
					selectBuilder.append(",");
			}
		}
		selectBuilder.append(" FROM ");
		if (this.lastNumberOfPackets != null) {
			// First replace any tags in preamble with number of packets and
			// then append
			selectBuilder.append((this.sqlLastNumberOfPacketsPreamble
					.replaceAll("@LAST_NUMBER_OF_PACKETS@",
							this.lastNumberOfPackets + ""))
					+ " ");
		}
		selectBuilder.append(this.sqlTableDelimiter + this.deviceID.longValue()
				+ this.sqlTableDelimiter);

		return selectBuilder.toString();
	}

	/**
	 * This method examines all the parameters in the factory and constructs the
	 * appropriate WHERE clause
	 * 
	 * @return
	 */
	private String constructWhereClause() {
		// A StringBuilder to use for the construction
		StringBuilder whereClauseBuilder = new StringBuilder();

		// A boolean to track if WHERE was added
		boolean whereAdded = false;

		// Add all constraints
		if (this.startParentID != null) {
			if (!whereAdded) {
				whereClauseBuilder.append(" WHERE");
				whereAdded = true;
			}
			if (this.endParentID != null) {
				whereClauseBuilder.append(" parentID >= "
						+ this.startParentID.longValue() + " AND parentID <= "
						+ this.endParentID.longValue());
			} else {
				whereClauseBuilder.append(" parentID = "
						+ this.startParentID.longValue());
			}
		}
		// Now check for packetType clause
		if (startPacketType != null) {
			// Add where if not added
			if (!whereAdded) {
				whereClauseBuilder.append(" WHERE");
				whereAdded = true;
			} else {
				whereClauseBuilder.append(" AND");
			}
			if (endPacketType != null) {
				whereClauseBuilder.append(" packetType >= "
						+ startPacketType.intValue() + " AND packetType <= "
						+ endPacketType.intValue());
			} else {
				whereClauseBuilder.append(" packetType = "
						+ startPacketType.intValue());
			}
		}
		// Now for packetSubType
		if (startPacketSubType != null) {
			// Add where if not added
			if (!whereAdded) {
				whereClauseBuilder.append(" WHERE");
				whereAdded = true;
			} else {
				whereClauseBuilder.append(" AND");
			}
			if (endPacketSubType != null) {
				whereClauseBuilder.append(" packetSubType >= "
						+ startPacketSubType.longValue()
						+ " AND packetSubType <= "
						+ endPacketSubType.longValue());
			} else {
				whereClauseBuilder.append(" packetSubType = "
						+ startPacketSubType.longValue());
			}
		}
		// Now for the dataDescriptionID
		if (startDataDescriptionID != null) {
			// Add where if not added
			if (!whereAdded) {
				whereClauseBuilder.append(" WHERE");
				whereAdded = true;
			} else {
				whereClauseBuilder.append(" AND");
			}
			if (endDataDescriptionID != null) {
				whereClauseBuilder.append(" dataDescriptionID >= "
						+ startDataDescriptionID.longValue()
						+ " AND dataDescriptionID <= "
						+ endDataDescriptionID.longValue());
			} else {
				whereClauseBuilder.append(" dataDescriptionID = "
						+ startDataDescriptionID.longValue());
			}
		}
		// Now for the dataDescriptionVersion
		if (startDataDescriptionVersion != null) {
			// Add where if not added
			if (!whereAdded) {
				whereClauseBuilder.append(" WHERE");
				whereAdded = true;
			} else {
				whereClauseBuilder.append(" AND");
			}
			if (endDataDescriptionVersion != null) {
				whereClauseBuilder.append(" dataDescriptionVersion >= "
						+ startDataDescriptionVersion.longValue()
						+ " AND dataDescriptionVersion <= "
						+ endDataDescriptionVersion.longValue());
			} else {
				whereClauseBuilder.append(" dataDescriptionVersion = "
						+ startDataDescriptionVersion.longValue());
			}
		}
		// Now for the timestampSeconds
		if (startTimestampSeconds != null) {
			// Add where if not added
			if (!whereAdded) {
				whereClauseBuilder.append(" WHERE");
				whereAdded = true;
			} else {
				whereClauseBuilder.append(" AND");
			}
			if (endTimestampSeconds != null) {
				whereClauseBuilder.append(" timestampSeconds >= "
						+ startTimestampSeconds.longValue()
						+ " AND timestampSeconds <= "
						+ endTimestampSeconds.longValue());
			} else {
				whereClauseBuilder.append(" timestampSeconds = "
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
				whereClauseBuilder.append(" WHERE");
				whereAdded = true;
			} else {
				whereClauseBuilder.append(" AND");
			}
			if (endSequenceNumber != null) {
				whereClauseBuilder.append(" sequenceNumber >= "
						+ startSequenceNumber.longValue()
						+ " AND sequenceNumber <= "
						+ endSequenceNumber.longValue());
			} else {
				whereClauseBuilder.append(" sequenceNumber = "
						+ startSequenceNumber.longValue());
			}
		}
		// Now for the latitude
		if (startLatitude != null) {
			// Add where if not added
			if (!whereAdded) {
				whereClauseBuilder.append(" WHERE");
				whereAdded = true;
			} else {
				whereClauseBuilder.append(" AND");
			}
			if (endLatitude != null) {
				whereClauseBuilder.append(" latitude >= "
						+ startLatitude.doubleValue() + " AND latitude <= "
						+ endLatitude.doubleValue());
			} else {
				whereClauseBuilder.append(" latitude = "
						+ startLatitude.doubleValue());
			}
		}
		// Now for the longitude
		if (startLongitude != null) {
			// Add where if not added
			if (!whereAdded) {
				whereClauseBuilder.append(" WHERE");
				whereAdded = true;
			} else {
				whereClauseBuilder.append(" AND");
			}
			if (endLongitude != null) {
				whereClauseBuilder.append(" longitude >= "
						+ startLongitude.doubleValue() + " AND longitude <= "
						+ endLongitude.doubleValue());
			} else {
				whereClauseBuilder.append(" longitude = "
						+ startLongitude.doubleValue());
			}
		}
		// Now for the depth
		if (startDepth != null) {
			// Add where if not added
			if (!whereAdded) {
				whereClauseBuilder.append(" WHERE");
				whereAdded = true;
			} else {
				whereClauseBuilder.append(" AND");
			}
			if (endDepth != null) {
				whereClauseBuilder.append(" depth >= "
						+ startDepth.floatValue() + " AND depth <= "
						+ endDepth.floatValue());
			} else {
				whereClauseBuilder
						.append(" depth = " + startDepth.floatValue());
			}
		}

		// Now add some ordering stuff
		if (this.lastNumberOfPackets != null) {
			// Replace postamble with last number of packets and append
			whereClauseBuilder.append(" "
					+ (this.sqlLastNumberOfPacketsPostamble.replaceAll(
							"@LAST_NUMBER_OF_PACKETS@",
							this.lastNumberOfPackets + "")));
		}

		// Return the where clause
		return whereClauseBuilder.toString();
	}

	/**
	 * This method build the order by clause based on the parameters in the
	 * orderByParameters array
	 * 
	 * @return
	 */
	private String constructOrderByClause() {
		// Create a string builder to construct the order by clause
		StringBuilder orderByStringBuilder = new StringBuilder();
		// Make sure there are parameters
		if (orderByParameters != null && orderByParameters.length > 0) {
			orderByStringBuilder.append(" ORDER BY");
			for (int i = 0; i < orderByParameters.length; i++) {
				orderByStringBuilder.append(" " + orderByParameters[i]);
				if (i != (orderByParameters.length - 1))
					orderByStringBuilder.append(",");
			}
		}
		return orderByStringBuilder.toString();
	}
}
