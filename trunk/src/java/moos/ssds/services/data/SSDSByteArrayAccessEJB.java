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
import java.sql.SQLException;
import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import moos.ssds.io.PacketSQLQuery;
import moos.ssds.io.PacketSQLQueryFactory;

import org.apache.log4j.Logger;

/**
 * This EJB is a <code>SessionBean</code> that gives clients the capability of
 * submitting packets that match the SSDS byte array format. These packets will
 * end up in the SQL database storage for the SSDS system.
 * 
 * @author kgomes
 * @ejb.bean name="SSDSByteArrayAccess" type="Stateful"
 *           jndi-name="moos/ssds/services/data/SSDSByteArrayAccess"
 *           local-jndi-name="moos/ssds/services/data/SSDSByteArrayLocal"
 *           view-type="both"
 * @ejb.home create="true"
 *           local-class="moos.ssds.services.data.SSDSByteArrayAccessLocalHome"
 *           remote-class="moos.ssds.services.data.SSDSByteArrayAccessHome"
 * @ejb.interface create="true"
 *                local-class="moos.ssds.services.data.SSDSByteArrayAccessLocal"
 *                remote-class="moos.ssds.services.data.SSDSByteArrayAccess"
 */
public class SSDSByteArrayAccessEJB implements SessionBean {

	/**
	 * The default serial version ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the session contexct
	 */
	private SessionContext sessionContext = null;

	/**
	 * This is the packet SQL factory that will be used to construct the query
	 */
	private PacketSQLQueryFactory packetSQLQueryFactory = null;

	/**
	 * This is the PacketSQLQuery used to actually handle the SQL calls
	 */
	private PacketSQLQuery packetSQLQuery = null;

	/** A log4j logger */
	static Logger logger = Logger.getLogger(SSDSByteArrayAccessEJB.class);

	/**
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException {
		logger.debug("ejbActivate called");
		// Create a new packetSQLQuery
		try {
			if (packetSQLQuery == null)
				packetSQLQuery = new PacketSQLQuery(null, packetSQLQueryFactory);
		} catch (SQLException e) {
			throw new RemoteException(e.getMessage());
		}
	}

	/**
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException {
		logger.debug("ejbPassivate called");
		// Close the sql connection
		packetSQLQuery.close();
		packetSQLQuery = null;
	}

	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException, RemoteException {
		logger.debug("ejbRemove called");
		// Close the PacketSQLQuery
		packetSQLQuery.close();
	}

	/**
	 * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException {
		logger.debug("setSessionContext called with context " + sessionContext);
		sessionContext = sessionContext;
	}

	/**
	 * The EJB callback that is used when the bean is created
	 */
	public void ejbCreate() throws CreateException {
		logger.debug("ejbCreate called");
		// Create the PacketSQLQueryFactory
		packetSQLQueryFactory = new PacketSQLQueryFactory();

		// Now the PacketSQLQuery
		try {
			if (packetSQLQuery == null)
				packetSQLQuery = new PacketSQLQuery(null, packetSQLQueryFactory);
		} catch (SQLException e) {
			throw new CreateException(e.getMessage());
		}
	}

	/**
	 * @throws CreateException
	 */
	public void ejbPostCreate() throws CreateException {
		logger.debug("ejbPostCreate called");
	}

	/**
	 * This method clears out all the order by parameters that will be used in
	 * the query. It sets up the default to be timestampSeconds and
	 * timestampNanoseconds ascending as the default
	 * 
	 * @ejb.interface-method view-type="both"
	 */
	public void clearOrderByParameters() {
		packetSQLQueryFactory.clearOrderByParameters();
	}

	/**
	 * This method adds a parameter to order the result by. The boolean
	 * indicates if it should be descending (true) or ascending (false)
	 * 
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
	 * @return Returns the deviceID.
	 */
	public long getDeviceID() {
		return packetSQLQueryFactory.getDeviceID();
	}

	/**
	 * This method sets the device ID that will be used in the query
	 * 
	 * @ejb.interface-method view-type="both"
	 * @param deviceID
	 */
	public void setDeviceID(long deviceID) {
		packetSQLQueryFactory.setDeviceID(deviceID);
	}

	/**
	 * This method returns the starting parent ID for the query that will be
	 * used.
	 * 
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
	 * @return Returns the endParentID.
	 */
	public long getEndParentID() {
		return packetSQLQueryFactory.getEndParentID();
	}

	/**
	 * This method sets the upper bound for the parent ID for the query.
	 * 
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
	 * @return Returns the endPacketType.
	 */
	public int getEndPacketType() {
		return packetSQLQueryFactory.getEndPacketType();
	}

	/**
	 * This method sets the upper bound of the packetType for the query to
	 * include
	 * 
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
	 * @return Returns the endPacketSubType.
	 */
	public long getEndPacketSubType() {
		return packetSQLQueryFactory.getEndPacketSubType();
	}

	/**
	 * This method sets the upper bound of the packetSubType that will be
	 * included in the query.
	 * 
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
	 * @return Returns the endDataDescriptionID.
	 */
	public long getEndDataDescriptionID() {
		return packetSQLQueryFactory.getEndDataDescriptionID();
	}

	/**
	 * This method sets the upper bound for the dataDescriptionID for the query
	 * to be executed
	 * 
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
	 * @return Returns the endDataDescriptionVersion.
	 */
	public long getEndDataDescriptionVersion() {
		return packetSQLQueryFactory.getEndDataDescriptionVersion();
	}

	/**
	 * This method sets the upper bound for the dataDescriptionVersion that will
	 * be used in the query.
	 * 
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
	 * @param startDate
	 */
	public void setEndDate(Date endDate) {
		packetSQLQueryFactory.setEndDate(endDate);
	}

	/**
	 * This method returns the upper bound for the timestamp in epoch seconds
	 * for which the query will be searching over.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @return Returns the endTimestampSeconds.
	 */
	public long getEndTimestampSeconds() {
		return packetSQLQueryFactory.getEndTimestampSeconds();
	}

	/**
	 * This method sets the upper bound for the timestamp in epoch seconds for
	 * which the query will search osver.
	 * 
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
	 * @return Returns the endSequenceNumber.
	 */
	public long getEndSequenceNumber() {
		return packetSQLQueryFactory.getEndSequenceNumber();
	}

	/**
	 * This method sets the upper bound for the sequenceNumber to search for.
	 * 
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
	 * @return Returns the endLatitude.
	 */
	public double getEndLatitude() {
		return packetSQLQueryFactory.getEndLatitude();
	}

	/**
	 * This method sets the upper bound of the latitude that will be searched
	 * for in this query.
	 * 
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
	 * @return Returns the endLongitude.
	 */
	public double getEndLongitude() {
		return packetSQLQueryFactory.getEndLongitude();
	}

	/**
	 * This method sets the upper bound of longitude that is set for the query.
	 * 
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
	 * @return Returns the endDepth.
	 */
	public float getEndDepth() {
		return packetSQLQueryFactory.getEndDepth();
	}

	/**
	 * This method sets the upper bound (poor choice of words for depth, I know)
	 * for this query.
	 * 
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
	 * @return
	 */
	public long getLastNumberOfPackets() {
		return packetSQLQueryFactory.getLastNumberOfPackets();
	}

	/**
	 * This method sets the number of packets to be retrieved from the end of
	 * the data query.
	 * 
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
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
	 * @ejb.interface-method view-type="both"
	 * @return
	 */
	public String[] listFieldNames() {
		return packetSQLQuery.listFieldNames();
	}

	/**
	 * This method returns the classes that are associated with the byte array
	 * that is returned
	 * 
	 * @ejb.interface-method view-type="both"
	 * @return
	 */
	public Class[] listFieldClasses() {
		return packetSQLQuery.listFieldClasses();
	}

	/**
	 * @see java.util.Enumeration#hasMoreElements()
	 * 
	 * @ejb.interface-method view-type="both"
	 */
	public boolean hasMoreElements() {
		return packetSQLQuery.hasMoreElements();
	}

	/**
	 * @see java.util.Enumeration#nextElement()
	 * 
	 * @ejb.interface-method view-type="both"
	 */
	public byte[] nextElement() {
		return packetSQLQuery.nextElement();
	}
}