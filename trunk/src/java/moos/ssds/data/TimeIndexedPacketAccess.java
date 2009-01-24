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
package moos.ssds.data;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import moos.ssds.data.parsers.IParser;
import moos.ssds.data.parsers.PacketParserContext;
import moos.ssds.data.parsers.Parser;
import moos.ssds.data.util.DataException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.services.data.SQLDataStreamRawDataAccess;
import moos.ssds.services.data.SQLDataStreamRawDataAccessEJB;
import moos.ssds.services.data.SQLDataStreamRawDataAccessHome;
import moos.ssds.services.data.SQLDataStreamRawDataAccessLocal;
import moos.ssds.services.data.SQLDataStreamRawDataAccessLocalHome;
import moos.ssds.services.data.SQLDataStreamRawDataAccessUtil;

import org.apache.log4j.Logger;
import org.mbari.util.MathUtil;

/**
 * This class provides time indexed data access to <code>DataContainer</code>s
 * that are backed by a stream of packets.
 */
public class TimeIndexedPacketAccess implements ITimeIndexedDataAccess {

    /**
     * This is the constructor that takes in the <code>DataContainer</code>
     * and a time window to load the data for access. It also accepts a
     * <code>Collection</code> of <code>String</code>s that are the
     * variable names of the variables to pull the data for. If the the
     * collection is not specified, all record variables will be read in.
     * 
     * @param dataContainer
     * @param startDate
     * @param endDate
     * @param recordVariableNames
     * @throws DataException
     */
    public TimeIndexedPacketAccess(DataContainer dataContainer, Date startDate,
        Date endDate, Collection recordVariableNames) throws DataException {
        // Setup the local variables
        this.dataContainer = dataContainer;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recordVariableNames = recordVariableNames;

        // Create the appropriate parser
        this.parser = new Parser(dataContainer);

        // Load the data
        this.initializeData();
    }

    /**
     * @see IDataAccess#getData(String)
     */
    public Object[] getData(String recordVariableName) {
        // Loop through the list of record variables and find the one
        // that matches the given name and return its data.
        Set rvs = mapRecordVariablesToData.keySet();
        Iterator iterator = rvs.iterator();
        while (iterator.hasNext()) {
            RecordVariable tempRV = (RecordVariable) iterator.next();
            if (tempRV.getName().equalsIgnoreCase(recordVariableName)) {
                return this.getData(tempRV);
            }
        }
        // If we are here, just return null, because no recordVariable with
        // that name was found
        return null;
    }

    /**
     * @see moos.ssds.access.data.IDataAccess#getData(moos.ssds.model.IRecordVariable)
     * @return The data array. It should be the same size as the array retrieved
     *         from getTime(). Missing values are represented by <strong>null
     *         </strong>. The data returned is sorted by time. Duplicate time
     *         values will result in the first occurence being used.
     */
    public Object[] getData(RecordVariable v) {
        // Look for the matching record variable
        RecordVariable matchingID = null;
        Set rvs = mapRecordVariablesToData.keySet();
        Iterator iterator = rvs.iterator();
        while (iterator.hasNext()) {
            RecordVariable tempRV = (RecordVariable) iterator.next();
            if (tempRV.getId().longValue() == v.getId().longValue()) {
                matchingID = tempRV;
                break;
            }
        }
        // If matching ID was not found, try by name
        if (matchingID == null) {
            iterator = rvs.iterator();
            while (iterator.hasNext()) {
                RecordVariable tempRV = (RecordVariable) iterator.next();
                if (tempRV.getName().equalsIgnoreCase(v.getName())) {
                    matchingID = tempRV;
                    break;
                }
            }

        }
        // Return the data
        if (mapRecordVariablesToData.containsKey(matchingID)) {
            List dataList = (List) mapRecordVariablesToData.get(matchingID);
            if (dataList == null) {
                logger.error("dataList was null, this should never happen");
            }
            // Sort based on time. Reject duplicates (use first occurence)
            Object[] out = (Object[]) dataList.toArray(new Object[dataList
                .size()]);
            return MathUtil.orderVector(out, sortOrder);
        }
        return null;
    }

    /**
     * This method reads in the data
     * 
     * @throws DataException
     */
    private void initializeData() throws DataException {
        // First let's make sure we can grab the device ID from the URL
        Long deviceIDLong = null;
        String urlString = this.dataContainer.getUriString();
        if (urlString != null) {
            Pattern deviceIDPattern = Pattern.compile(".*deviceID=(\\d+).*");
            Matcher matcher = deviceIDPattern.matcher(urlString);
            String deviceIDString = null;
            if (matcher.find()) {
                deviceIDString = matcher.group(1);
            }
            if (deviceIDString != null) {
                try {
                    deviceIDLong = new Long(deviceIDString);
                } catch (NumberFormatException e) {}
            }

        }
        if (deviceIDLong == null)
            throw new DataException(
                "No device ID could be extracted from the DataContainer specified");

        // The array list of times
        times = new ArrayList();

        // This loop is creating a new list for each variable in the
        // datacontainer
        for (Iterator iter = getDataContainer().getRecordDescription()
            .getRecordVariables().iterator(); iter.hasNext();) {
            Object obj = iter.next();
            // Check to see if the collection of record variable names was
            // specified
            if ((this.recordVariableNames != null)
                && (this.recordVariableNames.size() > 0)) {
                // Cast to a record variable to check against name
                RecordVariable rv = (RecordVariable) obj;
                if (this.recordVariableNames.contains(rv.getName())) {
                    mapRecordVariablesToData.put(obj, new ArrayList());
                }
            } else {
                mapRecordVariablesToData.put(obj, new ArrayList());
            }
        }
        // This is the map of data to record variables for a single record
        Map packetMapRecordVariablesToData;

        // Next we need to pull the packets from the time range given
        TreeMap dataMap = null;
        // Try to grab the local/remote interface to the
        // SQLDataStreamRawDataAccessEJB
        SQLDataStreamRawDataAccessLocalHome sqlDSLocalHome = null;
        SQLDataStreamRawDataAccessLocal sqlDSLocal = null;
        SQLDataStreamRawDataAccessHome sqlDSHome = null;
        SQLDataStreamRawDataAccess sqlDS = null;
        try {
            sqlDSLocalHome = SQLDataStreamRawDataAccessUtil.getLocalHome();
        } catch (NamingException e) {
            logger.debug("Naming Exception caught trying to find the local "
                + "home interface to SQLDataStreamRawDataAccess.  "
                + "This can be OK if we are operating remotely: "
                + e.getMessage());
        }
        if (sqlDSLocalHome == null) {
            // Try for the remote home
            try {
                sqlDSHome = SQLDataStreamRawDataAccessUtil.getHome();
            } catch (NamingException e) {
                logger
                    .error("OK, I could not get the remote home SQLDataStreamRawDataAccess. "
                        + "This is a problem as I could not get the local home either!: "
                        + e.getMessage());
            }
        }
        if (sqlDSLocalHome != null) {
            try {
                sqlDSLocal = sqlDSLocalHome.create();
            } catch (CreateException e) {
                logger
                    .error("Could not create the local interface to the SQL Data Service: "
                        + e.getMessage());
            }
        } else if (sqlDSHome != null) {
            try {
                sqlDS = sqlDSHome.create();
            } catch (RemoteException e) {
                logger
                    .error("Could not create the remote interface to the SQL Data Service:"
                        + e.getMessage());
            } catch (CreateException e) {
                logger
                    .error("Could not create the remote interface to the SQL Data Service:"
                        + e.getMessage());
            }
        }
        // Try to grab the SSDSDevicePackets
        Long startTimestampSeconds = null;
        Long startTimestampNanoseconds = null;
        Long endTimestampSeconds = null;
        Long endTimestampNanoseconds = null;
        if (this.startDate != null) {
            long startTimeInMillis = startDate.getTime();
            startTimestampSeconds = new Long(startTimeInMillis / 1000);
            startTimestampNanoseconds = new Long(
                startTimeInMillis % 1000 * 1000);
        } else {
            startTimestampSeconds = new Long(0);
            startTimestampNanoseconds = new Long(0);
        }
        if (this.endDate != null) {
            long endTimeInMillis = endDate.getTime();
            endTimestampSeconds = new Long(endTimeInMillis / 1000);
            endTimestampNanoseconds = new Long(endTimeInMillis % 1000 * 1000);
        } else {
            Date currentDate = new Date();
            long endTimeInMillis = currentDate.getTime();
            endTimestampSeconds = new Long(endTimeInMillis / 1000);
            endTimestampNanoseconds = new Long(endTimeInMillis % 1000 * 1000);
        }
        if (sqlDSLocal != null) {
            try {
                dataMap = sqlDSLocal.getSortedRawData(deviceIDLong, null, null,
                    null, null, dataContainer.getRecordDescription()
                        .getRecordType(), null, null, null, null, null,
                    startTimestampSeconds, endTimestampSeconds,
                    startTimestampNanoseconds, endTimestampNanoseconds, null,
                    null, null, null, null, null, null, null, null,
                    SQLDataStreamRawDataAccessEJB.BY_TIMESTAMP, true);
            } catch (SQLException e) {
                throw new DataException(e.getMessage());
            }
        } else {
            try {
                dataMap = sqlDS.getSortedRawData(deviceIDLong, null, null,
                    null, null, dataContainer.getRecordDescription()
                        .getRecordType(), null, null, null, null, null,
                    startTimestampSeconds, endTimestampSeconds,
                    startTimestampNanoseconds, endTimestampNanoseconds, null,
                    null, null, null, null, null, null, null, null,
                    SQLDataStreamRawDataAccessEJB.BY_TIMESTAMP, true);
            } catch (RemoteException e) {
                throw new DataException(e.getMessage());
            } catch (SQLException e) {
                throw new DataException(e.getMessage());
            }
        }

        // Now the TreeMap contains a mapping between timestamp and a Collection
        // of records. This is to support multiple recordTypes from one stream.
        // In this case it should be a one-to-one associated, but we still need
        // to extract out all the individual records into one Collection
        ArrayList packets = new ArrayList();
        if (dataMap != null) {
            Iterator dataMapValueIterator = dataMap.values().iterator();
            while (dataMapValueIterator.hasNext()) {
                packets.addAll((Collection) dataMapValueIterator.next());
            }
        }
        // Get the packet context so we can get some information from the
        // packets
        PacketParserContext context = (PacketParserContext) getParser()
            .getParserContext();

        // Set the collection of packets
        context.setSsdsDevicePackets(packets);

        // Load all data in a single pass
        while (getParser().hasNext()) {
            try {
                // map contains data as key = RecordVariable, value = data (as
                // Object)
                packetMapRecordVariablesToData = (Map) getParser().next();
            } catch (Exception e) {
                logger
                    .debug("Failed to parse packet contents for DataContainer : "
                        + getDataContainer().toStringRepresentation(",")
                        + ": "
                        + e.getMessage());
                continue;
            }
            // Grab the packet information
            Date packetDate = new Date(context.getCurrentSsdsDevicePacket()
                .systemTime());
            // Check to see if data is already there
            int alreadyThere = times.indexOf(new Long(context
                .getCurrentSsdsDevicePacket().systemTime()));

            // I had to add this in to watch for null start and end dates (which
            // is possible)
            Date tempStartDate = new Date();
            tempStartDate.setTime(0);
            Date tempEndDate = new Date();
            if (getStartDate() != null)
                tempStartDate = getStartDate();
            if (getEndDate() != null)
                tempEndDate = getEndDate();

            // If not, and it meets time criterion, add it
            if ((alreadyThere == -1)
                && (packetMapRecordVariablesToData != null)
                && ((packetDate.after(tempStartDate) && packetDate
                    .before(tempEndDate))
                    || packetDate.equals(tempStartDate) || packetDate
                    .equals(tempEndDate))) {
                // Add the time from the packet
                times.add(new Long(context.getCurrentSsdsDevicePacket()
                    .systemTime()));
                // Now add the data for each variable
                for (Iterator iter = packetMapRecordVariablesToData.keySet()
                    .iterator(); iter.hasNext();) {
                    RecordVariable rv = (RecordVariable) iter.next();
                    List vList = (List) mapRecordVariablesToData.get(rv);
                    if (vList != null) {
                        Object o = packetMapRecordVariablesToData.get(rv);
                        if (o != null) {
                            vList.add(o);
                        } else {
                            logger
                                .error("object is null, trouble with record variable "
                                    + rv.toStringRepresentation("|"));
                            vList.add(null);
                        }
                    }
                }
            }
        }
        // Set the sort order
        sortOrder = MathUtil.uniqueSort((Long[]) times.toArray(new Long[times
            .size()]));
    }

    /**
     * @see moos.ssds.data.ITimeIndexedDataAccess#getTime()
     * @return A Long[]. The time is the systemTime from the DataPacket. The
     *         array returned has been sorted and had duplicates removed. This
     *         array should be the same size as the arrays returned by
     *         getData();
     */
    public Object[] getTime() {
        if (times == null) {
            try {
                initializeData();
            } catch (DataException e) {
                return null;
            }
        }
        return MathUtil.orderVector((Long[]) times.toArray(new Long[times
            .size()]), sortOrder);
    }

    /**
     * @see moos.ssds.access.data.IDataContainerDataAccess#getDataContainer()
     */
    public DataContainer getDataContainer() {
        return this.dataContainer;
    }

    /**
     * @see moos.ssds.data.ITimeIndexedDataAccess#getStartDate()
     */
    public Date getStartDate() {
        return this.startDate;
    }

    /**
     * @see moos.ssds.data.ITimeIndexedDataAccess#getEndDate()
     */
    public Date getEndDate() {
        return this.endDate;
    }

    /**
     * A private method to return the file parser used to parse the
     * IDataContainer
     * 
     * @return
     */
    private IParser getParser() {
        return this.parser;
    }

    /**
     * @see moos.ssds.data.IDataAccess#getRecordVariables()
     */
    public Collection getRecordVariables() {
        return new ArrayList(this.mapRecordVariablesToData.keySet());
    }

    /**
     * This is the <code>DataContainer</code> that this data access object
     * uses to read data from
     */
    private final DataContainer dataContainer;

    /**
     * This is the inclusive start date of the data that will be accessible
     * through this object
     */
    private final Date startDate;

    /**
     * This is the inclusive end date of the data that will be accessible
     * through this object
     */
    private final Date endDate;

    /**
     * This is the collection of strings that represent the names of the record
     * variables to load data for
     */
    private Collection recordVariableNames;

    /**
     * This is the order of the data in the data array that will give you time
     * sorted data.
     */
    private int[] sortOrder;

    /**
     * This is a List of the time objects that are associated with the
     * corresponding data objects
     */
    private List times = null;

    /**
     * This is a <code>Map</code> that holds the
     * <code>moos.ssds.model.RecordVariable</code> as the key and an array of
     * <code>Number</code> s that are the data for that
     * <code>RecordVariable</code>
     */
    private final Map mapRecordVariablesToData = new HashMap();

    /**
     * This is the IFileParser that will be used to parse the contents of the
     * IDataContainer
     */
    private final IParser parser;

    /**
     * A log4j logger
     */
    static Logger logger = Logger.getLogger(TimeIndexedPacketAccess.class);
}