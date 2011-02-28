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

import java.util.Enumeration;

import javax.ejb.Remote;

/**
 * This is the remote interface to the <code>SSDSByteArrayAccessEJB</code>.
 * 
 * @author kgomes
 * @see SSDSByteArrayAccessEJB
 */
@Remote
public interface SSDSByteArrayAccess extends Enumeration<byte[]> {
	/**
	 * This method clears out all the order by parameters that will be used in
	 * the query. It sets up the default to be timestampSeconds and
	 * timestampNanoseconds ascending as the default
	 */
	public void clearOrderByParameters();

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
	public void addOrderByParameter(java.lang.String orderByParameter,
			boolean isDescending);

	/**
	 * This method returns the device ID of the data that will be queried for
	 * 
	 * @return Returns the deviceID.
	 */
	public long getDeviceID();

	/**
	 * This method sets the device ID that will be used in the query
	 * 
	 * @param deviceID
	 */
	public void setDeviceID(long deviceID);

	/**
	 * This method returns the starting parent ID for the query that will be
	 * used.
	 * 
	 * @return Returns the startParentID.
	 */
	public long getStartParentID();

	/**
	 * This sets the starting ID of the parent device during the data
	 * collection. If no end parentID is specified, the query will set the
	 * criteria to equals to the starting parent ID
	 * 
	 * @param startParentID
	 *            The parentID to set.
	 */
	public void setStartParentID(long startParentID);

	/**
	 * This method returns the ID of the parent ID that will be the upper bound
	 * for the query.
	 * 
	 * @return Returns the endParentID.
	 */
	public long getEndParentID();

	/**
	 * This method sets the upper bound for the parent ID for the query.
	 * 
	 * @param endParentID
	 *            The parentID to set.
	 */
	public void setEndParentID(long endParentID);

	/**
	 * This method returns the lower bound of the packetType for the query to
	 * include. If no endPacketType is specified, the query will search for
	 * packetType equal to startPacketType
	 * 
	 * @return Returns the startPacketType.
	 */
	public int getStartPacketType();

	/**
	 * This method sets the lower bound of the packetType for the query to
	 * include. If no endPacketType is specified, the query will searhc for
	 * packetType equal to startPacketType
	 * 
	 * @param startPacketType
	 *            The packetType to set.
	 */
	public void setStartPacketType(int startPacketType);

	/**
	 * This method returns the upper bound of the packetType for the query to
	 * include.
	 * 
	 * @return Returns the endPacketType.
	 */
	public int getEndPacketType();

	/**
	 * This method sets the upper bound of the packetType for the query to
	 * include
	 * 
	 * @param endPacketType
	 *            The packetType to set.
	 */
	public void setEndPacketType(int endPacketType);

	/**
	 * This method returns the lower bound of the packetSubType that will be
	 * included in the query. If the endPacketSubType is not specified, the
	 * query will set the criteria to search for packetSubTypes that are equal
	 * to startPacketSubType.
	 * 
	 * @return Returns the startPacketSubType.
	 */
	public long getStartPacketSubType();

	/**
	 * This method sets the lower bound of the packetSubType that will be
	 * included in the query. If the endPacketSubType is not specified, the
	 * query will set the criteria to search for packetSubTypes that are equal
	 * to startPacketSubType.
	 * 
	 * @param startPacketSubType
	 *            The packetSubType to set.
	 */
	public void setStartPacketSubType(long startPacketSubType);

	/**
	 * This method returns the upper bound of the packetSubType that will be
	 * included in the query.
	 * 
	 * @return Returns the endPacketSubType.
	 */
	public long getEndPacketSubType();

	/**
	 * This method sets the upper bound of the packetSubType that will be
	 * included in the query.
	 * 
	 * @param endPacketSubType
	 *            The packetSubType to set.
	 */
	public void setEndPacketSubType(long endPacketSubType);

	/**
	 * This method returns the lower bound of the DataDescriptionID for the
	 * query to include. If the endDataDescriptionID is not specified, the query
	 * will set the criteria for dataDescriptionID to be equal to the
	 * startDataDescriptionID.
	 * 
	 * @return Returns the startDataDescriptionID.
	 */
	public long getStartDataDescriptionID();

	/**
	 * This method sets the lower bound of the DataDescriptionID for the query
	 * to include. If the endDataDescriptionID is not specified, the query will
	 * set the criteria for dataDescriptionID to be equal to the
	 * startDataDescriptionID.
	 * 
	 * @param startDataDescriptionID
	 *            The dataDescriptionID to set.
	 */
	public void setStartDataDescriptionID(long startDataDescriptionID);

	/**
	 * This method returns the upper bound of the dataDescriptionID for the
	 * query to be executed.
	 * 
	 * @return Returns the endDataDescriptionID.
	 */
	public long getEndDataDescriptionID();

	/**
	 * This method sets the upper bound for the dataDescriptionID for the query
	 * to be executed
	 * 
	 * @param endDataDescriptionID
	 *            The dataDescriptionID to set.
	 */
	public void setEndDataDescriptionID(long endDataDescriptionID);

	/**
	 * This method returns the lower bound of the dataDescriptionVersion for the
	 * query to be executed. If the endDataDescriptionVersion is not specified,
	 * the query will set the criteria to be equal to the
	 * startDataDescriptionVersion.
	 * 
	 * @return Returns the startDataDescriptionVersion.
	 */
	public long getStartDataDescriptionVersion();

	/**
	 * This method sets the lower bound of the dataDescriptionVersion for the
	 * query to be executed. If the endDataDescriptionVersion is not specified,
	 * the query will set the criteria to be equal to the
	 * startDataDescriptionVersion.
	 * 
	 * @param startDataDescriptionVersion
	 *            The dataDescriptionVersion to set.
	 */
	public void setStartDataDescriptionVersion(long startDataDescriptionVersion);

	/**
	 * This method returns the upper bound for the dataDescriptionVersion that
	 * will be used in the query.
	 * 
	 * @return Returns the endDataDescriptionVersion.
	 */
	public long getEndDataDescriptionVersion();

	/**
	 * This method sets the upper bound for the dataDescriptionVersion that will
	 * be used in the query.
	 * 
	 * @param endDataDescriptionVersion
	 *            The dataDescriptionVersion to set.
	 */
	public void setEndDataDescriptionVersion(long endDataDescriptionVersion);

	/**
	 * This method sets the lower bound for the start date for the query. If the
	 * endDate is not specified, the query will set the criteria to be equal to
	 * the start date specified here. A method to set the start date for the
	 * query
	 * 
	 * @param startDate
	 */
	public void setStartDate(java.util.Date startDate);

	/**
	 * This method returns the start time in epoch seconds for the query that
	 * will be executed. If the endTimestampSeconds is not specified, the query
	 * will set the criteria equal to the startTimeSeconds.
	 * 
	 * @return Returns the startTimestampSeconds.
	 */
	public long getStartTimestampSeconds();

	/**
	 * This method sets the start time in epoch seconds for the query that will
	 * be executed. If the endTimestampSeconds is not specified, the query will
	 * set the criteria equal to the startTimeSeconds.
	 * 
	 * @param startTimestampSeconds
	 *            The timestampSeconds to set.
	 */
	public void setStartTimestampSeconds(long startTimestampSeconds);

	/**
	 * This method sets the upper limit of the date and time that the query will
	 * be searching over.
	 * 
	 * @param startDate
	 */
	public void setEndDate(java.util.Date endDate);

	/**
	 * This method returns the upper bound for the timestamp in epoch seconds
	 * for which the query will be searching over.
	 * 
	 * @return Returns the endTimestampSeconds.
	 */
	public long getEndTimestampSeconds();

	/**
	 * This method sets the upper bound for the timestamp in epoch seconds for
	 * which the query will search osver.
	 * 
	 * @param endTimestampSeconds
	 *            The timestampSeconds to set.
	 */
	public void setEndTimestampSeconds(long endTimestampSeconds);

	/**
	 * This method returns the lower bound for the sequence number that will be
	 * queried for. If the endSequenceNumber is not specified, the query will
	 * search only for entries that have a sequenceNumber equal to the
	 * startSequenceNumber specified here.
	 * 
	 * @return Returns the startSequenceNumber.
	 */
	public long getStartSequenceNumber();

	/**
	 * This method sets the lower bound for the sequence number that will be
	 * queried for. If the endSequenceNumber is not specified, the query will
	 * search only for entries that have a sequenceNumber equal to the
	 * startSequenceNumber specified here.
	 * 
	 * @param startSequenceNumber
	 *            The sequenceNumber to set.
	 */
	public void setStartSequenceNumber(long startSequenceNumber);

	/**
	 * This method returns the upper bound for the sequenceNumber that will be
	 * used in the query.
	 * 
	 * @return Returns the endSequenceNumber.
	 */
	public long getEndSequenceNumber();

	/**
	 * This method sets the upper bound for the sequenceNumber to search for.
	 * 
	 * @param startSequenceNumber
	 *            The sequenceNumber to set.
	 */
	public void setEndSequenceNumber(long endSequenceNumber);

	/**
	 * This method returns the lower bound of the window of latitude that will
	 * be searched for. If this is set and the endLatitude is not set, the
	 * criteria will be set to search for records that match the startLatitude.
	 * 
	 * @return Returns the startLatitude.
	 */
	public double getStartLatitude();

	/**
	 * This method sets the lower bound of the window of latitude that will be
	 * searched for. If this is set and the endLatitude is not set, the criteria
	 * will be set to search for records that match the startLatitude.
	 * 
	 * @param startLatitude
	 *            The startLatitude to set.
	 */
	public void setStartLatitude(double startLatitude);

	/**
	 * This method returns the upper bound of the latitude that will be searched
	 * for in this query.
	 * 
	 * @return Returns the endLatitude.
	 */
	public double getEndLatitude();

	/**
	 * This method sets the upper bound of the latitude that will be searched
	 * for in this query.
	 * 
	 * @param endLatitude
	 *            The endLatitude to set.
	 */
	public void setEndLatitude(double endLatitude);

	/**
	 * This method returns the lower bound of the window of longitude that will
	 * be searched for. If this is set and the endLongitude is not set, the
	 * criteria will be set to search for records that match the startLongitude.
	 * 
	 * @return Returns the startLongitude.
	 */
	public double getStartLongitude();

	/**
	 * This method sets the lower bound of the window of longitude that will be
	 * searched for. If this is set and the endLongitude is not set, the
	 * criteria will be set to search for records that match the startLongitude.
	 * 
	 * @param startLongitude
	 *            The startLongitude to set.
	 */
	public void setStartLongitude(double startLongitude);

	/**
	 * This method returns the upper bound of longitude that is set for the
	 * query.
	 * 
	 * @return Returns the endLongitude.
	 */
	public double getEndLongitude();

	/**
	 * This method sets the upper bound of longitude that is set for the query.
	 * 
	 * @param endLongitude
	 *            The endLongitude to set.
	 */
	public void setEndLongitude(double endLongitude);

	/**
	 * This method returns the lower bound of the window of depth that will be
	 * searched for. If this is set and the endDepth is not set, the criteria
	 * will be set to search for records that match the startDepth.
	 * 
	 * @return Returns the startDepth.
	 */
	public float getStartDepth();

	/**
	 * This method sets the lower bound of the window of depth that will be
	 * searched for. If this is set and the endDepth is not set, the criteria
	 * will be set to search for records that match the startDepth.
	 * 
	 * @param startDepth
	 *            The startDepth to set.
	 */
	public void setStartDepth(float startDepth);

	/**
	 * This method returns the upper bound (poor choice of words for depth, I
	 * know) for this query.
	 * 
	 * @return Returns the endDepth.
	 */
	public float getEndDepth();

	/**
	 * This method sets the upper bound (poor choice of words for depth, I know)
	 * for this query.
	 * 
	 * @param endDepth
	 *            The endDepth to set.
	 */
	public void setEndDepth(float endDepth);

	/**
	 * This is the method that returns the number of packets to be retrieved
	 * from the end of the query that is returned
	 * 
	 * @return
	 */
	public long getLastNumberOfPackets();

	/**
	 * This method sets the number of packets to be retrieved from the end of
	 * the data query.
	 * 
	 * @param lastNumberOfPackets
	 */
	public void setLastNumberOfPackets(long lastNumberOfPackets);

	/**
	 * This method is called when the actually query is executed. It must be
	 * called before iterating over any elements
	 * 
	 * @throws SQLException
	 */
	public void queryForData() throws java.sql.SQLException;

	/**
	 * This method closes the session with the server and cleans things up.
	 * <b>PLEASE CALL THIS WHEN YOU ARE DONE WITH YOUR DATA ACCESS</b>.
	 */
	public void remove();

	/**
	 * This method returns the names of the fields in the byte array in the
	 * order in which they are returned
	 * 
	 * @return
	 */
	public java.lang.String[] listFieldNames();

	/**
	 * This method returns the classes that are associated with the byte array
	 * that is returned
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Class[] listFieldClasses();

	/**
	 * Tests if this enumeration contains more elements.
	 * 
	 * @see Enumeration#hasMoreElements()
	 */
	public boolean hasMoreElements();

	/**
	 * Returns the next element of this enumeration if this enumeration object
	 * has at least one more element to provide.
	 * 
	 * @see Enumeration#nextElement()
	 */
	public byte[] nextElement();

}
