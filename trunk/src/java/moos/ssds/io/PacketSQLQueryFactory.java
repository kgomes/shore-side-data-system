package moos.ssds.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PacketSQLQueryFactory {
	/** A log4j logger */
	static Logger logger = Logger.getLogger(PacketSQLQueryFactory.class);

	/**
	 * These are the variables that are used to construct the SELECT part of the
	 * clause
	 */
	public static final String SSDS_PACKET_VERSION = "ssdsPacketVersion";
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
	 * This is a <code>HashMap</code> that will store the field names as the key
	 * and the <code>Class</code> that that field is as the value
	 */
	@SuppressWarnings("unchecked")
	private HashMap<String, Class> parameterClassMap = null;

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
	 * This is the version of packet that will be queried for
	 */
	private Integer ssdsPacketVersion = null;

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
	 * The default constructor
	 */
	@SuppressWarnings("unchecked")
	public PacketSQLQueryFactory() {

		logger.debug("PacketSQLQueryFactory constructor called");

		// Construct the list of parameters
		availableParameters = new ArrayList<String>();
		parameterClassMap = new HashMap<String, Class>();
		availableParameters.add(SSDS_PACKET_VERSION);
		parameterClassMap.put(SSDS_PACKET_VERSION, int.class);
		availableParameters.add(PARENT_ID);
		parameterClassMap.put(PARENT_ID, long.class);
		availableParameters.add(PACKET_TYPE);
		parameterClassMap.put(PACKET_TYPE, int.class);
		availableParameters.add(PACKET_SUB_TYPE);
		parameterClassMap.put(PACKET_SUB_TYPE, long.class);
		availableParameters.add(DATA_DESCRIPTION_ID);
		parameterClassMap.put(DATA_DESCRIPTION_ID, long.class);
		availableParameters.add(DATA_DESCRIPTION_VERSION);
		parameterClassMap.put(DATA_DESCRIPTION_VERSION, long.class);
		availableParameters.add(TIMESTAMP_SECONDS);
		parameterClassMap.put(TIMESTAMP_SECONDS, long.class);
		availableParameters.add(TIMESTAMP_NANOSECONDS);
		parameterClassMap.put(TIMESTAMP_NANOSECONDS, long.class);
		availableParameters.add(SEQUENCE_NUMBER);
		parameterClassMap.put(SEQUENCE_NUMBER, long.class);
		availableParameters.add(BUFFER_LEN);
		parameterClassMap.put(BUFFER_LEN, int.class);
		availableParameters.add(BUFFER_BYTES);
		parameterClassMap.put(BUFFER_BYTES, byte[].class);
		availableParameters.add(BUFFER_TWO_LEN);
		parameterClassMap.put(BUFFER_TWO_LEN, int.class);
		availableParameters.add(BUFFER_TWO_BYTES);
		parameterClassMap.put(BUFFER_TWO_BYTES, byte[].class);
		availableParameters.add(LATITUDE);
		parameterClassMap.put(LATITUDE, long.class);
		availableParameters.add(LONGITUDE);
		parameterClassMap.put(LONGITUDE, long.class);
		availableParameters.add(DEPTH);
		parameterClassMap.put(DEPTH, long.class);

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
		logger.debug("sqlTableDelimiter = " + sqlTableDelimiter
				+ ", sqlLastNumberOfPacketsPreamble = "
				+ sqlLastNumberOfPacketsPreamble
				+ ", sqlLastNumberOfPacketsPostamble = "
				+ sqlLastNumberOfPacketsPostamble);

		// Clear the order by parameters and set to default
		this.clearOrderByParameters();

	}

	/**
	 * This is the default constructor
	 */
	public PacketSQLQueryFactory(Long deviceID) {
		this();
		// Set the device ID
		this.setDeviceID(deviceID);
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
				temporaryList = new ArrayList<String>(Arrays
						.asList(selectParametersAndOrder));
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
			ArrayList<String> temporaryList = null;
			if (defaultOrderByParameters) {
				temporaryList = new ArrayList<String>();
				defaultOrderByParameters = false;
			} else {
				temporaryList = new ArrayList<String>(Arrays
						.asList(orderByParameters));
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
	 * A method to retrieve the packet version parameter
	 */
	public int geSSDSPacketVersion() {
		if (ssdsPacketVersion != null) {
			return ssdsPacketVersion.intValue();
		} else {
			return MISSING_VALUE;
		}
	}

	/**
	 * This method sets the version of the packet that will be queried for
	 * 
	 * @param packetVersion
	 */
	public void setSSDSPacketVersion(int packetVersion)
			throws IllegalArgumentException {
		if (packetVersion == MISSING_VALUE) {
			this.ssdsPacketVersion = null;
		} else if (packetVersion < 1) {
			throw new IllegalArgumentException(
					"The packetVersion cannot be less than 1");
		} else {
			this.ssdsPacketVersion = packetVersion;
		}
	}

	/**
	 * @return Returns the deviceID.
	 */
	public long getDeviceID() {
		if (deviceID != null) {
			return deviceID.longValue();
		} else {
			return MISSING_VALUE;
		}
	}

	/**
	 * @param deviceID
	 *            The deviceID to set.
	 */
	public void setDeviceID(long deviceID) {
		if (deviceID == MISSING_VALUE) {
			throw new IllegalArgumentException("Device ID must be specified.");
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
			return MISSING_VALUE;
		}
	}

	/**
	 * @param startParentID
	 *            The parentID to set.
	 */
	public void setStartParentID(long startParentID) {
		if (startParentID == MISSING_VALUE) {
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
			return MISSING_VALUE;
		}
	}

	/**
	 * @param endParentID
	 *            The parentID to set.
	 */
	public void setEndParentID(long endParentID) {
		if (endParentID == MISSING_VALUE) {
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
			return MISSING_VALUE;
		}
	}

	/**
	 * @param startPacketType
	 *            The packetType to set.
	 */
	public void setStartPacketType(int startPacketType) {
		if (startPacketType == MISSING_VALUE) {
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
			return MISSING_VALUE;
		}
	}

	/**
	 * @param endPacketType
	 *            The packetType to set.
	 */
	public void setEndPacketType(int endPacketType) {
		if (endPacketType == MISSING_VALUE) {
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
			return MISSING_VALUE;
		}
	}

	/**
	 * @param startPacketSubType
	 *            The packetSubType to set.
	 */
	public void setStartPacketSubType(long startPacketSubType) {
		if (startPacketSubType == MISSING_VALUE) {
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
			return MISSING_VALUE;
		}
	}

	/**
	 * @param endPacketSubType
	 *            The packetSubType to set.
	 */
	public void setEndPacketSubType(long endPacketSubType) {
		if (endPacketSubType == MISSING_VALUE) {
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
			return MISSING_VALUE;
		}
	}

	/**
	 * @param startDataDescriptionID
	 *            The dataDescriptionID to set.
	 */
	public void setStartDataDescriptionID(long startDataDescriptionID) {
		if (startDataDescriptionID == MISSING_VALUE) {
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
			return MISSING_VALUE;
		}
	}

	/**
	 * @param endDataDescriptionID
	 *            The dataDescriptionID to set.
	 */
	public void setEndDataDescriptionID(long endDataDescriptionID) {
		if (endDataDescriptionID == MISSING_VALUE) {
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
			return MISSING_VALUE;
		}
	}

	/**
	 * @param startDataDescriptionVersion
	 *            The dataDescriptionVersion to set.
	 */
	public void setStartDataDescriptionVersion(long startDataDescriptionVersion) {
		if (startDataDescriptionVersion == MISSING_VALUE) {
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
			return MISSING_VALUE;
		}
	}

	/**
	 * @param endDataDescriptionVersion
	 *            The dataDescriptionVersion to set.
	 */
	public void setEndDataDescriptionVersion(long endDataDescriptionVersion) {
		if (endDataDescriptionVersion == MISSING_VALUE) {
			this.endDataDescriptionVersion = null;
		} else {
			this.endDataDescriptionVersion = new Long(endDataDescriptionVersion);
		}
	}

	/**
	 * A method to set the start date for the query
	 * 
	 * TODO kgomes This is affected by SSDS-77 bug
	 * 
	 * @param startDate
	 */
	public void setStartDate(Date startDate) {
		if (startDate == null) {
			this.setStartTimestampSeconds(MISSING_VALUE);
			this.setStartTimestampNanoseconds(MISSING_VALUE);
		} else {
			this.setStartTimestampSeconds(startDate.getTime() / 1000);
			this
					.setStartTimestampNanoseconds((startDate.getTime() % 1000) * 1000);
		}
	}

	/**
	 * @return Returns the startTimestampSeconds.
	 */
	public long getStartTimestampSeconds() {
		if (this.startTimestampSeconds != null) {
			return this.startTimestampSeconds.longValue();
		} else {
			return MISSING_VALUE;
		}
	}

	/**
	 * @param startTimestampSeconds
	 *            The timestampSeconds to set.
	 */
	public void setStartTimestampSeconds(long startTimestampSeconds) {
		if (startTimestampSeconds == MISSING_VALUE) {
			this.startTimestampSeconds = null;
		} else {
			this.startTimestampSeconds = new Long(startTimestampSeconds);
		}
	}

	/**
	 * A method to set the end date for the query
	 * 
	 * TODO kgomes This is affected by SSDS-77 bug
	 * 
	 * @param startDate
	 */
	public void setEndDate(Date endDate) {
		if (endDate == null) {
			this.setEndTimestampSeconds(MISSING_VALUE);
			this.setEndTimestampNanoseconds(MISSING_VALUE);
		} else {
			this.setEndTimestampSeconds(endDate.getTime() / 1000);
			this.setEndTimestampNanoseconds((endDate.getTime() % 1000) * 1000);
		}
	}

	/**
	 * @return Returns the endTimestampSeconds.
	 */
	public long getEndTimestampSeconds() {
		if (this.endTimestampSeconds != null) {
			return this.endTimestampSeconds.longValue();
		} else {
			return MISSING_VALUE;
		}
	}

	/**
	 * @param endTimestampSeconds
	 *            The timestampSeconds to set.
	 */
	public void setEndTimestampSeconds(long endTimestampSeconds) {
		if (endTimestampSeconds == MISSING_VALUE) {
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
			return MISSING_VALUE;
		}
	}

	/**
	 * @param startTimestampNanoseconds
	 *            The timestampNanoseconds to set.
	 */
	public void setStartTimestampNanoseconds(long startTimestampNanoseconds) {
		if (startTimestampNanoseconds == MISSING_VALUE) {
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
			return MISSING_VALUE;
		}
	}

	/**
	 * @param endTimestampNanoseconds
	 *            The timestampNanoseconds to set.
	 */
	public void setEndTimestampNanoseconds(long endTimestampNanoseconds) {
		if (endTimestampNanoseconds == MISSING_VALUE) {
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
			return MISSING_VALUE;
		}
	}

	/**
	 * @param startSequenceNumber
	 *            The sequenceNumber to set.
	 */
	public void setStartSequenceNumber(long startSequenceNumber) {
		if (startSequenceNumber == MISSING_VALUE) {
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
			return MISSING_VALUE;
		}
	}

	/**
	 * @param startSequenceNumber
	 *            The sequenceNumber to set.
	 */
	public void setEndSequenceNumber(long endSequenceNumber) {
		if (endSequenceNumber == MISSING_VALUE) {
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
			return MISSING_VALUE;
		}
	}

	/**
	 * This method sets the number of packets to be retrieved from the end of
	 * the data stream.
	 * 
	 * @param lastNumberOfPackets
	 */
	public void setLastNumberOfPackets(long lastNumberOfPackets) {
		if (lastNumberOfPackets == MISSING_VALUE) {
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
			return MISSING_VALUE;
		} else {
			return this.startLatitude.doubleValue();
		}
	}

	/**
	 * @param startLatitude
	 *            The startLatitude to set.
	 */
	public void setStartLatitude(double startLatitude) {
		if (startLatitude == MISSING_VALUE) {
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
			return MISSING_VALUE;
		} else {
			return this.endLatitude.doubleValue();
		}
	}

	/**
	 * @param endLatitude
	 *            The endLatitude to set.
	 */
	public void setEndLatitude(double endLatitude) {
		if (endLatitude == MISSING_VALUE) {
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
			return MISSING_VALUE;
		} else {
			return startLongitude.doubleValue();
		}
	}

	/**
	 * @param startLongitude
	 *            The startLongitude to set.
	 */
	public void setStartLongitude(double startLongitude) {
		if (startLongitude == MISSING_VALUE) {
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
			return MISSING_VALUE;
		} else {
			return endLongitude.doubleValue();
		}
	}

	/**
	 * @param endLongitude
	 *            The endLongitude to set.
	 */
	public void setEndLongitude(double endLongitude) {
		if (endLongitude == MISSING_VALUE) {
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
			return MISSING_VALUE;
		} else {
			return startDepth.floatValue();
		}
	}

	/**
	 * @param startDepth
	 *            The startDepth to set.
	 */
	public void setStartDepth(float startDepth) {
		if (startDepth == MISSING_VALUE) {
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
			return MISSING_VALUE;
		}
	}

	/**
	 * @param endDepth
	 *            The endDepth to set.
	 */
	public void setEndDepth(float endDepth) {
		if (endDepth == MISSING_VALUE) {
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
		return constructSelectStatement()
				+ constructLastNumberOfPacketsPreamble()
				+ this.sqlTableDelimiter + this.deviceID.longValue()
				+ this.sqlTableDelimiter + constructWhereClause()
				+ constructLastNumberOfPacketPostamble()
				+ constructOrderByClause();
	}

	/**
	 * This method returns the listing of the fields that will be returned and
	 * the order in which they are returned
	 * 
	 * @return
	 */
	public String[] listReturnFields() {
		String[] fields = null;
		// Check to see if no select parameters were specified. If not, include
		// all of them.
		if (selectParametersAndOrder == null
				|| selectParametersAndOrder.length == 0) {
			fields = new String[16];
			fields[0] = SSDS_PACKET_VERSION;
			fields[1] = PARENT_ID;
			fields[2] = PACKET_TYPE;
			fields[3] = PACKET_SUB_TYPE;
			fields[4] = DATA_DESCRIPTION_ID;
			fields[5] = DATA_DESCRIPTION_VERSION;
			fields[6] = TIMESTAMP_SECONDS;
			fields[7] = TIMESTAMP_NANOSECONDS;
			fields[8] = SEQUENCE_NUMBER;
			fields[9] = BUFFER_LEN;
			fields[10] = BUFFER_BYTES;
			fields[11] = BUFFER_TWO_LEN;
			fields[12] = BUFFER_TWO_BYTES;
			fields[13] = LATITUDE;
			fields[14] = LONGITUDE;
			fields[15] = DEPTH;
		} else {
			fields = new String[selectParametersAndOrder.length];
			for (int i = 0; i < selectParametersAndOrder.length; i++) {
				fields[i] = selectParametersAndOrder[i];
			}
		}
		return fields;
	}

	/**
	 * This method returns an array of <code>Class</code>es that specify the
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Class[] listReturnClasses() {
		// Grab the listing of the fields
		String[] fields = listReturnFields();

		// Create an array of classes
		Class[] classes = new Class[fields.length];

		// Loop over and based on the name, add a class for the type
		for (int i = 0; i < classes.length; i++) {
			classes[i] = parameterClassMap.get(fields[i]);
		}

		// Now return it
		return classes;
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
					selectBuilder.append(", ");
			}
		}
		selectBuilder.append(" FROM ");

		return selectBuilder.toString();
	}

	/**
	 * This is a method to construct a preamble that will be needed if the
	 * "last number of packets" is specified
	 * 
	 * @return
	 */
	private String constructLastNumberOfPacketsPreamble() {
		if (this.lastNumberOfPackets != null) {
			// First replace any tags in preamble with number of packets and
			// then return
			return (this.sqlLastNumberOfPacketsPreamble.replaceAll(
					"@LAST_NUMBER_OF_PACKETS@", this.lastNumberOfPackets + "") + " ");
		} else {
			return "";
		}
	}

	/**
	 * This is a method to construct a postamble that will be needed if the
	 * "last number of packets" is specified
	 * 
	 * @return
	 */
	private String constructLastNumberOfPacketPostamble() {
		if (this.lastNumberOfPackets != null) {
			// Replace postamble with last number of packets and append
			return " "
					+ (this.sqlLastNumberOfPacketsPostamble.replaceAll(
							"@LAST_NUMBER_OF_PACKETS@",
							this.lastNumberOfPackets + ""));

		} else {
			return "";
		}
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
		if (this.ssdsPacketVersion != null) {
			if (!whereAdded) {
				whereClauseBuilder.append(" WHERE");
				whereAdded = true;
			}
			whereClauseBuilder.append(" ssdsPacketVersion = "
					+ this.ssdsPacketVersion.longValue());
		}

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
