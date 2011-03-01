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

import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.sql.DataSource;

import moos.ssds.io.PacketSQLQuery;
import moos.ssds.io.PacketSQLQueryFactory;

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * This is the stateful session EJB for clients to get access to SSDS raw data
 * packets that have been streamed to the SSDS. It has lots of parameters that
 * can be set for the query to allow for flexible data queries. It also handles
 * paging of the data on the server so the client does not have to worry about
 * bogging down the memory if the request is large.
 * 
 */
@Stateful
@RemoteBinding(jndiBinding = "moos/ssds/services/data/SSDSByteArrayAccess")
@LocalBinding(jndiBinding = "moos/ssds/services/data/SSDSByteArrayAccessLocal")
public class SSDSByteArrayAccessEJB implements SSDSByteArrayAccess,
		SSDSByteArrayAccessLocal, Enumeration<byte[]> {

	/**
	 * A log4j logger
	 */
	private static Logger logger = Logger
			.getLogger(SSDSByteArrayAccessEJB.class);
	/**
	 * The default serial version ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the data source that the container will provide and will be used
	 * to query for data
	 */
	@Resource(mappedName = "java:/SSDS_Data")
	private static DataSource dataSource;

	/**
	 * This is the packet SQL factory that will be used to construct the query
	 */
	private PacketSQLQueryFactory packetSQLQueryFactory = null;

	/**
	 * This is the PacketSQLQuery used to actually handle the SQL calls
	 */
	private PacketSQLQuery packetSQLQuery = null;

	/**
	 * This is the default constructor that is called when the bean is
	 * instantiated
	 */
	public SSDSByteArrayAccessEJB() {
	}

	@PostConstruct
	public void setupPacketSQLQuery() throws SQLException {
		logger.debug("DataSource is " + dataSource.toString());
		// Create the PacketSQLQueryFactory
		packetSQLQueryFactory = new PacketSQLQueryFactory();

		// Create a new packetSQLQuery
		if (packetSQLQuery == null)
			packetSQLQuery = new PacketSQLQuery(dataSource,
					packetSQLQueryFactory);
	}

	/**
	 * This method is called by the container just before the EJB is passivated
	 */
	@PrePassivate
	public void prePassivate() {
		logger.debug("prePassivate called");
		// Close the sql connection
		if (packetSQLQuery != null) {
			packetSQLQuery.close();
			packetSQLQuery = null;
		}
	}

	/**
	 * This method is called by the container just after the EJB is activated
	 * back into a session
	 * 
	 * @throws SQLException
	 */
	@PostActivate
	public void postActivate() throws SQLException {
		setupPacketSQLQuery();
	}

	/**
	 * This method is called by the container just before the EJB is removed
	 * (destroyed)
	 */
	@Remove
	public void remove() {
		logger.debug("remove called");
		// Close the PacketSQLQuery
		if (packetSQLQuery != null)
			packetSQLQuery.close();
	}

	/**
	 * This method clears out all the order by parameters that will be used in
	 * the query. It sets up the default to be timestampSeconds and
	 * timestampNanoseconds ascending as the default
	 */
	public void clearOrderByParameters() {
		packetSQLQueryFactory.clearOrderByParameters();
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
		packetSQLQueryFactory.addOrderByParameter(orderByParameter,
				isDescending);
	}

	/**
	 * This method returns the device ID of the data that will be queried for
	 * 
	 * @return Returns the deviceID.
	 */
	public long getDeviceID() {
		return packetSQLQueryFactory.getDeviceID();
	}

	/**
	 * This method sets the device ID that will be used in the query
	 * 
	 * @param deviceID
	 */
	public void setDeviceID(long deviceID) {
		packetSQLQueryFactory.setDeviceID(deviceID);
	}

	/**
	 * This method returns the starting parent ID for the query that will be
	 * used.
	 * 
	 * 
	 * @return Returns the startParentID.
	 */
	public long getStartParentID() {
		return packetSQLQueryFactory.getStartParentID();
	}

	/**
	 * This sets the starting ID of the parent device during the data
	 * collection. If no end parentID is specified, the query will set the
	 * criteria to equals to the starting parent ID
	 * 
	 * 
	 * @param startParentID
	 *            The parentID to set.
	 */
	public void setStartParentID(long startParentID) {
		packetSQLQueryFactory.setStartParentID(startParentID);
	}

	/**
	 * This method returns the ID of the parent ID that will be the upper bound
	 * for the query.
	 * 
	 * 
	 * @return Returns the endParentID.
	 */
	public long getEndParentID() {
		return packetSQLQueryFactory.getEndParentID();
	}

	/**
	 * This method sets the upper bound for the parent ID for the query.
	 * 
	 * 
	 * @param endParentID
	 *            The parentID to set.
	 */
	public void setEndParentID(long endParentID) {
		packetSQLQueryFactory.setEndParentID(endParentID);
	}

	/**
	 * This method returns the lower bound of the packetType for the query to
	 * include. If no endPacketType is specified, the query will search for
	 * packetType equal to startPacketType
	 * 
	 * 
	 * @return Returns the startPacketType.
	 */
	public int getStartPacketType() {
		return packetSQLQueryFactory.getStartPacketType();
	}

	/**
	 * This method sets the lower bound of the packetType for the query to
	 * include. If no endPacketType is specified, the query will searhc for
	 * packetType equal to startPacketType
	 * 
	 * 
	 * @param startPacketType
	 *            The packetType to set.
	 */
	public void setStartPacketType(int startPacketType) {
		packetSQLQueryFactory.setStartPacketType(startPacketType);
	}

	/**
	 * This method returns the upper bound of the packetType for the query to
	 * include.
	 * 
	 * 
	 * @return Returns the endPacketType.
	 */
	public int getEndPacketType() {
		return packetSQLQueryFactory.getEndPacketType();
	}

	/**
	 * This method sets the upper bound of the packetType for the query to
	 * include
	 * 
	 * 
	 * @param endPacketType
	 *            The packetType to set.
	 */
	public void setEndPacketType(int endPacketType) {
		packetSQLQueryFactory.setEndPacketType(endPacketType);
	}

	/**
	 * This method returns the lower bound of the packetSubType that will be
	 * included in the query. If the endPacketSubType is not specified, the
	 * query will set the criteria to search for packetSubTypes that are equal
	 * to startPacketSubType.
	 * 
	 * 
	 * @return Returns the startPacketSubType.
	 */
	public long getStartPacketSubType() {
		return packetSQLQueryFactory.getStartPacketSubType();
	}

	/**
	 * This method sets the lower bound of the packetSubType that will be
	 * included in the query. If the endPacketSubType is not specified, the
	 * query will set the criteria to search for packetSubTypes that are equal
	 * to startPacketSubType.
	 * 
	 * 
	 * @param startPacketSubType
	 *            The packetSubType to set.
	 */
	public void setStartPacketSubType(long startPacketSubType) {
		packetSQLQueryFactory.setStartPacketSubType(startPacketSubType);
	}

	/**
	 * This method returns the upper bound of the packetSubType that will be
	 * included in the query.
	 * 
	 * 
	 * @return Returns the endPacketSubType.
	 */
	public long getEndPacketSubType() {
		return packetSQLQueryFactory.getEndPacketSubType();
	}

	/**
	 * This method sets the upper bound of the packetSubType that will be
	 * included in the query.
	 * 
	 * 
	 * @param endPacketSubType
	 *            The packetSubType to set.
	 */
	public void setEndPacketSubType(long endPacketSubType) {
		packetSQLQueryFactory.setEndPacketSubType(endPacketSubType);
	}

	/**
	 * This method returns the lower bound of the DataDescriptionID for the
	 * query to include. If the endDataDescriptionID is not specified, the query
	 * will set the criteria for dataDescriptionID to be equal to the
	 * startDataDescriptionID.
	 * 
	 * 
	 * @return Returns the startDataDescriptionID.
	 */
	public long getStartDataDescriptionID() {
		return packetSQLQueryFactory.getStartDataDescriptionID();
	}

	/**
	 * This method sets the lower bound of the DataDescriptionID for the query
	 * to include. If the endDataDescriptionID is not specified, the query will
	 * set the criteria for dataDescriptionID to be equal to the
	 * startDataDescriptionID.
	 * 
	 * 
	 * @param startDataDescriptionID
	 *            The dataDescriptionID to set.
	 */
	public void setStartDataDescriptionID(long startDataDescriptionID) {
		packetSQLQueryFactory.setStartDataDescriptionID(startDataDescriptionID);
	}

	/**
	 * This method returns the upper bound of the dataDescriptionID for the
	 * query to be executed.
	 * 
	 * 
	 * @return Returns the endDataDescriptionID.
	 */
	public long getEndDataDescriptionID() {
		return packetSQLQueryFactory.getEndDataDescriptionID();
	}

	/**
	 * This method sets the upper bound for the dataDescriptionID for the query
	 * to be executed
	 * 
	 * 
	 * @param endDataDescriptionID
	 *            The dataDescriptionID to set.
	 */
	public void setEndDataDescriptionID(long endDataDescriptionID) {
		packetSQLQueryFactory.setEndDataDescriptionID(endDataDescriptionID);
	}

	/**
	 * This method returns the lower bound of the dataDescriptionVersion for the
	 * query to be executed. If the endDataDescriptionVersion is not specified,
	 * the query will set the criteria to be equal to the
	 * startDataDescriptionVersion.
	 * 
	 * 
	 * @return Returns the startDataDescriptionVersion.
	 */
	public long getStartDataDescriptionVersion() {
		return packetSQLQueryFactory.getStartDataDescriptionVersion();
	}

	/**
	 * This method sets the lower bound of the dataDescriptionVersion for the
	 * query to be executed. If the endDataDescriptionVersion is not specified,
	 * the query will set the criteria to be equal to the
	 * startDataDescriptionVersion.
	 * 
	 * 
	 * @param startDataDescriptionVersion
	 *            The dataDescriptionVersion to set.
	 */
	public void setStartDataDescriptionVersion(long startDataDescriptionVersion) {
		packetSQLQueryFactory
				.setStartDataDescriptionVersion(startDataDescriptionVersion);
	}

	/**
	 * This method returns the upper bound for the dataDescriptionVersion that
	 * will be used in the query.
	 * 
	 * 
	 * @return Returns the endDataDescriptionVersion.
	 */
	public long getEndDataDescriptionVersion() {
		return packetSQLQueryFactory.getEndDataDescriptionVersion();
	}

	/**
	 * This method sets the upper bound for the dataDescriptionVersion that will
	 * be used in the query.
	 * 
	 * 
	 * @param endDataDescriptionVersion
	 *            The dataDescriptionVersion to set.
	 */
	public void setEndDataDescriptionVersion(long endDataDescriptionVersion) {
		packetSQLQueryFactory
				.setEndDataDescriptionVersion(endDataDescriptionVersion);
	}

	/**
	 * This method sets the lower bound for the start date for the query. If the
	 * endDate is not specified, the query will set the criteria to be equal to
	 * the start date specified here. A method to set the start date for the
	 * query
	 * 
	 * 
	 * @param startDate
	 */
	public void setStartDate(Date startDate) {
		packetSQLQueryFactory.setStartDate(startDate);
	}

	/**
	 * This method returns the start time in epoch seconds for the query that
	 * will be executed. If the endTimestampSeconds is not specified, the query
	 * will set the criteria equal to the startTimeSeconds.
	 * 
	 * 
	 * @return Returns the startTimestampSeconds.
	 */
	public long getStartTimestampSeconds() {
		return packetSQLQueryFactory.getStartTimestampSeconds();
	}

	/**
	 * This method sets the start time in epoch seconds for the query that will
	 * be executed. If the endTimestampSeconds is not specified, the query will
	 * set the criteria equal to the startTimeSeconds.
	 * 
	 * 
	 * @param startTimestampSeconds
	 *            The timestampSeconds to set.
	 */
	public void setStartTimestampSeconds(long startTimestampSeconds) {
		packetSQLQueryFactory.setStartTimestampSeconds(startTimestampSeconds);
	}

	/**
	 * This method sets the upper limit of the date and time that the query will
	 * be searching over.
	 * 
	 * 
	 * @param startDate
	 */
	public void setEndDate(Date endDate) {
		packetSQLQueryFactory.setEndDate(endDate);
	}

	/**
	 * This method returns the upper bound for the timestamp in epoch seconds
	 * for which the query will be searching over.
	 * 
	 * 
	 * @return Returns the endTimestampSeconds.
	 */
	public long getEndTimestampSeconds() {
		return packetSQLQueryFactory.getEndTimestampSeconds();
	}

	/**
	 * This method sets the upper bound for the timestamp in epoch seconds for
	 * which the query will search osver.
	 * 
	 * 
	 * @param endTimestampSeconds
	 *            The timestampSeconds to set.
	 */
	public void setEndTimestampSeconds(long endTimestampSeconds) {
		packetSQLQueryFactory.setEndTimestampSeconds(endTimestampSeconds);
	}

	/**
	 * This method returns the lower bound for the sequence number that will be
	 * queried for. If the endSequenceNumber is not specified, the query will
	 * search only for entries that have a sequenceNumber equal to the
	 * startSequenceNumber specified here.
	 * 
	 * 
	 * @return Returns the startSequenceNumber.
	 */
	public long getStartSequenceNumber() {
		return packetSQLQueryFactory.getStartSequenceNumber();
	}

	/**
	 * This method sets the lower bound for the sequence number that will be
	 * queried for. If the endSequenceNumber is not specified, the query will
	 * search only for entries that have a sequenceNumber equal to the
	 * startSequenceNumber specified here.
	 * 
	 * 
	 * @param startSequenceNumber
	 *            The sequenceNumber to set.
	 */
	public void setStartSequenceNumber(long startSequenceNumber) {
		packetSQLQueryFactory.setStartSequenceNumber(startSequenceNumber);
	}

	/**
	 * This method returns the upper bound for the sequenceNumber that will be
	 * used in the query.
	 * 
	 * 
	 * @return Returns the endSequenceNumber.
	 */
	public long getEndSequenceNumber() {
		return packetSQLQueryFactory.getEndSequenceNumber();
	}

	/**
	 * This method sets the upper bound for the sequenceNumber to search for.
	 * 
	 * 
	 * @param startSequenceNumber
	 *            The sequenceNumber to set.
	 */
	public void setEndSequenceNumber(long endSequenceNumber) {
		packetSQLQueryFactory.setEndSequenceNumber(endSequenceNumber);
	}

	/**
	 * This method returns the lower bound of the window of latitude that will
	 * be searched for. If this is set and the endLatitude is not set, the
	 * criteria will be set to search for records that match the startLatitude.
	 * 
	 * 
	 * @return Returns the startLatitude.
	 */
	public double getStartLatitude() {
		return packetSQLQueryFactory.getStartLatitude();
	}

	/**
	 * This method sets the lower bound of the window of latitude that will be
	 * searched for. If this is set and the endLatitude is not set, the criteria
	 * will be set to search for records that match the startLatitude.
	 * 
	 * 
	 * @param startLatitude
	 *            The startLatitude to set.
	 */
	public void setStartLatitude(double startLatitude) {
		packetSQLQueryFactory.setStartLatitude(startLatitude);
	}

	/**
	 * This method returns the upper bound of the latitude that will be searched
	 * for in this query.
	 * 
	 * 
	 * @return Returns the endLatitude.
	 */
	public double getEndLatitude() {
		return packetSQLQueryFactory.getEndLatitude();
	}

	/**
	 * This method sets the upper bound of the latitude that will be searched
	 * for in this query.
	 * 
	 * 
	 * @param endLatitude
	 *            The endLatitude to set.
	 */
	public void setEndLatitude(double endLatitude) {
		packetSQLQueryFactory.setEndLatitude(endLatitude);
	}

	/**
	 * This method returns the lower bound of the window of longitude that will
	 * be searched for. If this is set and the endLongitude is not set, the
	 * criteria will be set to search for records that match the startLongitude.
	 * 
	 * 
	 * @return Returns the startLongitude.
	 */
	public double getStartLongitude() {
		return packetSQLQueryFactory.getStartLongitude();
	}

	/**
	 * This method sets the lower bound of the window of longitude that will be
	 * searched for. If this is set and the endLongitude is not set, the
	 * criteria will be set to search for records that match the startLongitude.
	 * 
	 * 
	 * @param startLongitude
	 *            The startLongitude to set.
	 */
	public void setStartLongitude(double startLongitude) {
		packetSQLQueryFactory.setStartLongitude(startLongitude);
	}

	/**
	 * This method returns the upper bound of longitude that is set for the
	 * query.
	 * 
	 * 
	 * @return Returns the endLongitude.
	 */
	public double getEndLongitude() {
		return packetSQLQueryFactory.getEndLongitude();
	}

	/**
	 * This method sets the upper bound of longitude that is set for the query.
	 * 
	 * 
	 * @param endLongitude
	 *            The endLongitude to set.
	 */
	public void setEndLongitude(double endLongitude) {
		packetSQLQueryFactory.setEndLongitude(endLongitude);
	}

	/**
	 * This method returns the lower bound of the window of depth that will be
	 * searched for. If this is set and the endDepth is not set, the criteria
	 * will be set to search for records that match the startDepth.
	 * 
	 * 
	 * @return Returns the startDepth.
	 */
	public float getStartDepth() {
		return packetSQLQueryFactory.getStartDepth();
	}

	/**
	 * This method sets the lower bound of the window of depth that will be
	 * searched for. If this is set and the endDepth is not set, the criteria
	 * will be set to search for records that match the startDepth.
	 * 
	 * 
	 * @param startDepth
	 *            The startDepth to set.
	 */
	public void setStartDepth(float startDepth) {
		packetSQLQueryFactory.setStartDepth(startDepth);
	}

	/**
	 * This method returns the upper bound (poor choice of words for depth, I
	 * know) for this query.
	 * 
	 * 
	 * @return Returns the endDepth.
	 */
	public float getEndDepth() {
		return packetSQLQueryFactory.getEndDepth();
	}

	/**
	 * This method sets the upper bound (poor choice of words for depth, I know)
	 * for this query.
	 * 
	 * 
	 * @param endDepth
	 *            The endDepth to set.
	 */
	public void setEndDepth(float endDepth) {
		packetSQLQueryFactory.setEndDepth(endDepth);
	}

	/**
	 * This is the method that returns the number of packets to be retrieved
	 * from the end of the query that is returned
	 * 
	 * 
	 * @return
	 */
	public long getLastNumberOfPackets() {
		return packetSQLQueryFactory.getLastNumberOfPackets();
	}

	/**
	 * This method sets the number of packets to be retrieved from the end of
	 * the data query.
	 * 
	 * 
	 * @param lastNumberOfPackets
	 */
	public void setLastNumberOfPackets(long lastNumberOfPackets) {
		packetSQLQueryFactory.setLastNumberOfPackets(lastNumberOfPackets);
	}

	/**
	 * This method is called when the actually query is executed. It must be
	 * called before iterating over any elements
	 * 
	 * 
	 * 
	 * @throws SQLException
	 */
	public void queryForData() throws SQLException {
		if (packetSQLQueryFactory.getDeviceID() > 0) {
			packetSQLQuery.queryForData();
		} else {
			throw new IllegalArgumentException(
					"No Device ID was specified on the "
							+ "PacketSQLQueryFactory, now query possible");
		}
	}

	/**
	 * This method returns the names of the fields in the byte array in the
	 * order in which they are returned
	 * 
	 * 
	 * @return
	 */
	public String[] listFieldNames() {
		return packetSQLQuery.listFieldNames();
	}

	/**
	 * This method returns the classes that are associated with the byte array
	 * that is returned
	 * 
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Class[] listFieldClasses() {
		return packetSQLQuery.listFieldClasses();
	}

	/**
	 * @see java.util.Enumeration#hasMoreElements()
	 * 
	 */
	public boolean hasMoreElements() {
		return packetSQLQuery.hasMoreElements();
	}

	/**
	 * @see java.util.Enumeration#nextElement()
	 * 
	 */
	public byte[] nextElement() {
		return packetSQLQuery.nextElement();
	}
}