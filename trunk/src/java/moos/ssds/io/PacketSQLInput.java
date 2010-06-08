// $Header: /home/cvs/ssds/src/java/moos/ssds/io/PacketInput.java,v 1.20
// 2005/04/25 15:32:02 kgomes Exp $

package moos.ssds.io;

import java.sql.SQLException;
import java.util.Enumeration;

import javax.sql.DataSource;

import moos.ssds.io.util.PacketUtility;

import org.apache.log4j.Logger;

/**
 * <p>
 * This class provides access to the raw data in a DataStream that was collected
 * by SSDS and stored in a relational database. It supports different query
 * arguments that, in general, if left <code>null</code> will not be used in the
 * query. Also, note that some parameters have starts and ends and if the end is
 * left null, only the start value will be queried for. Otherwise an inclusive
 * query (includes starts and ends) will be performed. The parameters are:
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
public class PacketSQLInput implements Enumeration<Object> {

	/** A log4j logger */
	static Logger logger = Logger.getLogger(PacketSQLInput.class);

	/**
	 * This is the packet SQL factory that will be used to construct the query
	 */
	private PacketSQLQueryFactory packetSQLQueryFactory = null;

	/**
	 * This is the PacketSQLQuery used to actually handle the SQL calls
	 */
	private PacketSQLQuery packetSQLQuery = null;

	/**
	 * This is the value that will be returned if any of the parameter objects
	 * are null
	 */
	public static final int MISSING_VALUE = -999999;

	/**
	 * This constructor takes in the DataSource that will be used to query data
	 * from and the ID of the device that will be queried for
	 * 
	 * @param dataSource
	 * @param deviceID
	 */
	public PacketSQLInput(DataSource dataSource, long deviceID,
			String sqlTableDelimiter) {
		// Create the new PacketSQLQueryFactory
		this.packetSQLQueryFactory = new PacketSQLQueryFactory(deviceID);

		// If the sqlTableDelimeter is specified, it must be overriding
		if (sqlTableDelimiter != null) {
			this.packetSQLQueryFactory.setSqlTableDelimiter(sqlTableDelimiter);
		}

		// Construct a new PacketSQLQuery
		try {
			this.packetSQLQuery = new PacketSQLQuery(dataSource,
					packetSQLQueryFactory);
		} catch (SQLException e) {
			logger
					.error("SQLException caught trying to setup a new PacketSQLQuery:"
							+ e.getMessage());
		}
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

		// Create the factory
		this.packetSQLQueryFactory = new PacketSQLQueryFactory(deviceID);

		// If the sqlTableDelimeter is specified, it must be overriding
		if (sqlTableDelimiter != null) {
			this.packetSQLQueryFactory.setSqlTableDelimiter(sqlTableDelimiter);
		}

		// Now the packetSQLQuery
		this.packetSQLQuery = new PacketSQLQuery(databaseDriverClassName,
				databaseJDBCUrl, username, password, this.packetSQLQueryFactory);
	}

	/**
	 * @return Returns the deviceID.
	 */
	public long getDeviceID() {
		return packetSQLQueryFactory.getDeviceID();
	}

	/**
	 * @param deviceID
	 *            The deviceID to set.
	 */
	public void setDeviceID(long deviceID) {
		packetSQLQueryFactory.setDeviceID(deviceID);
	}

	/**
	 * @return Returns the startParentID.
	 */
	public long getStartParentID() {
		return packetSQLQueryFactory.getStartParentID();
	}

	/**
	 * @param startParentID
	 *            The parentID to set.
	 */
	public void setStartParentID(long startParentID) {
		packetSQLQueryFactory.setStartParentID(startParentID);
	}

	/**
	 * @return Returns the endParentID.
	 */
	public long getEndParentID() {
		return packetSQLQueryFactory.getEndParentID();
	}

	/**
	 * @param endParentID
	 *            The parentID to set.
	 */
	public void setEndParentID(long endParentID) {
		packetSQLQueryFactory.setEndParentID(endParentID);
	}

	/**
	 * @return Returns the startPacketType.
	 */
	public int getStartPacketType() {
		return packetSQLQueryFactory.getStartPacketType();
	}

	/**
	 * @param startPacketType
	 *            The packetType to set.
	 */
	public void setStartPacketType(int startPacketType) {
		packetSQLQueryFactory.setStartPacketType(startPacketType);
	}

	/**
	 * @return Returns the endPacketType.
	 */
	public int getEndPacketType() {
		return packetSQLQueryFactory.getEndPacketType();
	}

	/**
	 * @param endPacketType
	 *            The packetType to set.
	 */
	public void setEndPacketType(int endPacketType) {
		packetSQLQueryFactory.setEndPacketType(endPacketType);
	}

	/**
	 * @return Returns the startPacketSubType.
	 */
	public long getStartPacketSubType() {
		return packetSQLQueryFactory.getStartPacketSubType();
	}

	/**
	 * @param startPacketSubType
	 *            The packetSubType to set.
	 */
	public void setStartPacketSubType(long startPacketSubType) {
		packetSQLQueryFactory.setStartPacketSubType(startPacketSubType);
	}

	/**
	 * @return Returns the endPacketSubType.
	 */
	public long getEndPacketSubType() {
		return packetSQLQueryFactory.getEndPacketSubType();
	}

	/**
	 * @param endPacketSubType
	 *            The packetSubType to set.
	 */
	public void setEndPacketSubType(long endPacketSubType) {
		packetSQLQueryFactory.setEndPacketSubType(endPacketSubType);
	}

	/**
	 * @return Returns the startDataDescriptionID.
	 */
	public long getStartDataDescriptionID() {
		return packetSQLQueryFactory.getStartDataDescriptionID();
	}

	/**
	 * @param startDataDescriptionID
	 *            The dataDescriptionID to set.
	 */
	public void setStartDataDescriptionID(long startDataDescriptionID) {
		packetSQLQueryFactory.setStartDataDescriptionID(startDataDescriptionID);
	}

	/**
	 * @return Returns the endDataDescriptionID.
	 */
	public long getEndDataDescriptionID() {
		return packetSQLQueryFactory.getEndDataDescriptionID();
	}

	/**
	 * @param endDataDescriptionID
	 *            The dataDescriptionID to set.
	 */
	public void setEndDataDescriptionID(long endDataDescriptionID) {
		packetSQLQueryFactory.setEndDataDescriptionID(endDataDescriptionID);
	}

	/**
	 * @return Returns the startDataDescriptionVersion.
	 */
	public long getStartDataDescriptionVersion() {
		return packetSQLQueryFactory.getStartDataDescriptionVersion();
	}

	/**
	 * @param startDataDescriptionVersion
	 *            The dataDescriptionVersion to set.
	 */
	public void setStartDataDescriptionVersion(long startDataDescriptionVersion) {
		packetSQLQueryFactory
				.setStartDataDescriptionVersion(startDataDescriptionVersion);
	}

	/**
	 * @return Returns the endDataDescriptionVersion.
	 */
	public long getEndDataDescriptionVersion() {
		return packetSQLQueryFactory.getEndDataDescriptionVersion();
	}

	/**
	 * @param endDataDescriptionVersion
	 *            The dataDescriptionVersion to set.
	 */
	public void setEndDataDescriptionVersion(long endDataDescriptionVersion) {
		packetSQLQueryFactory
				.setEndDataDescriptionVersion(endDataDescriptionVersion);
	}

	/**
	 * @return Returns the startTimestampSeconds.
	 */
	public long getStartTimestampSeconds() {
		return packetSQLQueryFactory.getStartTimestampSeconds();
	}

	/**
	 * @param startTimestampSeconds
	 *            The timestampSeconds to set.
	 */
	public void setStartTimestampSeconds(long startTimestampSeconds) {
		packetSQLQueryFactory.setStartTimestampSeconds(startTimestampSeconds);
	}

	/**
	 * @return Returns the endTimestampSeconds.
	 */
	public long getEndTimestampSeconds() {
		return packetSQLQueryFactory.getEndTimestampSeconds();
	}

	/**
	 * @param endTimestampSeconds
	 *            The timestampSeconds to set.
	 */
	public void setEndTimestampSeconds(long endTimestampSeconds) {
		packetSQLQueryFactory.setEndTimestampSeconds(endTimestampSeconds);
	}

	/**
	 * @return Returns the startTimestampNanoseconds.
	 */
	public long getStartTimestampNanoseconds() {
		return packetSQLQueryFactory.getStartTimestampNanoseconds();
	}

	/**
	 * @param startTimestampNanoseconds
	 *            The timestampNanoseconds to set.
	 */
	public void setStartTimestampNanoseconds(long startTimestampNanoseconds) {
		packetSQLQueryFactory
				.setStartTimestampNanoseconds(startTimestampNanoseconds);
	}

	/**
	 * @return Returns the endTimestampNanoseconds.
	 */
	public long getEndTimestampNanoseconds() {
		return packetSQLQueryFactory.getEndTimestampNanoseconds();
	}

	/**
	 * @param endTimestampNanoseconds
	 *            The timestampNanoseconds to set.
	 */
	public void setEndTimestampNanoseconds(long endTimestampNanoseconds) {
		packetSQLQueryFactory
				.setEndTimestampNanoseconds(endTimestampNanoseconds);
	}

	/**
	 * @return Returns the startSequenceNumber.
	 */
	public long getStartSequenceNumber() {
		return packetSQLQueryFactory.getStartSequenceNumber();
	}

	/**
	 * @param startSequenceNumber
	 *            The sequenceNumber to set.
	 */
	public void setStartSequenceNumber(long startSequenceNumber) {
		packetSQLQueryFactory.setStartSequenceNumber(startSequenceNumber);
	}

	/**
	 * @return Returns the endSequenceNumber.
	 */
	public long getEndSequenceNumber() {
		return packetSQLQueryFactory.getEndSequenceNumber();
	}

	/**
	 * @param startSequenceNumber
	 *            The sequenceNumber to set.
	 */
	public void setEndSequenceNumber(long endSequenceNumber) {
		packetSQLQueryFactory.setEndSequenceNumber(endSequenceNumber);
	}

	/**
	 * This is the method that returns the number of packets to be retrieved
	 * from the end of the data stream
	 * 
	 * @return
	 */
	public long getLastNumberOfPackets() {
		return packetSQLQueryFactory.getLastNumberOfPackets();
	}

	/**
	 * This method sets the number of packets to be retrieved from the end of
	 * the data stream.
	 * 
	 * @param lastNumberOfPackets
	 */
	public void setLastNumberOfPackets(long lastNumberOfPackets) {
		packetSQLQueryFactory.setLastNumberOfPackets(lastNumberOfPackets);
	}

	/**
	 * @return Returns the startLatitude.
	 */
	public double getStartLatitude() {
		return packetSQLQueryFactory.getStartLatitude();
	}

	/**
	 * @param startLatitude
	 *            The startLatitude to set.
	 */
	public void setStartLatitude(double startLatitude) {
		packetSQLQueryFactory.setStartLatitude(startLatitude);
	}

	/**
	 * @return Returns the endLatitude.
	 */
	public double getEndLatitude() {
		return getEndLatitude();
	}

	/**
	 * @param endLatitude
	 *            The endLatitude to set.
	 */
	public void setEndLatitude(double endLatitude) {
		packetSQLQueryFactory.setEndLatitude(endLatitude);
	}

	/**
	 * @return Returns the startLongitude.
	 */
	public double getStartLongitude() {
		return packetSQLQueryFactory.getStartLongitude();
	}

	/**
	 * @param startLongitude
	 *            The startLongitude to set.
	 */
	public void setStartLongitude(double startLongitude) {
		packetSQLQueryFactory.setStartLongitude(startLongitude);
	}

	/**
	 * @return Returns the endLongitude.
	 */
	public double getEndLongitude() {
		return packetSQLQueryFactory.getEndLongitude();
	}

	/**
	 * @param endLongitude
	 *            The endLongitude to set.
	 */
	public void setEndLongitude(double endLongitude) {
		packetSQLQueryFactory.setEndLongitude(endLongitude);
	}

	/**
	 * @return Returns the startDepth.
	 */
	public float getStartDepth() {
		return packetSQLQueryFactory.getStartDepth();
	}

	/**
	 * @param startDepth
	 *            The startDepth to set.
	 */
	public void setStartDepth(float startDepth) {
		packetSQLQueryFactory.setStartDepth(startDepth);
	}

	/**
	 * @return Returns the endDepth.
	 */
	public float getEndDepth() {
		return packetSQLQueryFactory.getEndDepth();
	}

	/**
	 * @param endDepth
	 *            The endDepth to set.
	 */
	public void setEndDepth(float endDepth) {
		packetSQLQueryFactory.setEndDepth(endDepth);
	}

	/**
	 * This method runs the query using the parameters stored and then holds the
	 * result set
	 */
	public void queryForData() throws SQLException {
		// Query on the packetSQLQuery
		if (packetSQLQuery != null)
			packetSQLQuery.queryForData();
	}

	/**
	 * This method returns a boolean that indicates if there are more packets
	 * that can be read from the source. If <code>true</code>, the caller should
	 * be able to call <code>nextElement</code> to retrieve another packet.
	 * 
	 * @return a <code>boolean</code> that indicates if more packets can be read
	 *         from the source. More can be read if <code>true</code>, none if
	 *         <code>false</code>.
	 */
	public boolean hasMoreElements() {
		return packetSQLQuery.hasMoreElements();
	}

	/**
	 * This method closes the results and connections.
	 */
	public void close() {
		packetSQLQuery.close();
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
		// This is the object to return
		Object objectToReturn = null;

		// Convert it to and SSDSDevicePacket
		objectToReturn = PacketUtility
				.convertVersion3SSDSByteArrayToSSDSDevicePacket(
						nextSSDSByteArray(), true);

		// Now return it
		return objectToReturn;
	}

	/**
	 * This method returns the next record read from the database but in the
	 * SSDS byte array format
	 * 
	 * @return
	 */
	public byte[] nextSSDSByteArray() {
		// The byte array to return
		byte[] ssdsFormat = null;

		// First thing to do is the grab the next element from the sql query
		// underneath
		byte[] byteArrayWithVersionAndNoDevice = packetSQLQuery.nextElement();

		// Convert it to SSDS format
		if (byteArrayWithVersionAndNoDevice != null)
			ssdsFormat = PacketUtility.stripOffVersionAndAddDeviceIDInFront(
					byteArrayWithVersionAndNoDevice, packetSQLQueryFactory
							.getDeviceID());

		// Now return it
		return ssdsFormat;
	}

	/**
	 * This method reads in the next record as a byte array, but then returns
	 * those records in the form of an array of Object in the SSDS byte array
	 * form and order
	 * 
	 * @return
	 */
	public Object[] nextSSDSByteArrayAsObjectArray() {
		return PacketUtility.readVariablesFromVersion3SSDSByteArray(this
				.nextSSDSByteArray());
	}
}