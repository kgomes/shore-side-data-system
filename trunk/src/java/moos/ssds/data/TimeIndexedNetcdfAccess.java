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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.RecordVariable;

import org.apache.log4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.DataType;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dods.DODSNetcdfFile;
import dods.dap.DODSException;

/**
 * This class provide data access to NetCDF files. The thing that is different
 * is that is can be accessed by providing the query metadata in SSDS
 * terminology.
 */
public class TimeIndexedNetcdfAccess implements ITimeIndexedDataAccess {

    /**
     * This is the constructor to build the time indexed data access
     * 
     * @param dataContainer
     *            is the <code>DataContainer</code> that points to the data
     *            source to use to provide the data from this service class
     * @param startDate
     *            is the inclusive start date that the data in the object will
     *            cover
     * @param endDate
     *            is the inclusive end date that the data in the object will
     *            cover
     */
    public TimeIndexedNetcdfAccess(DataContainer dataContainer, Date startDate,
        Date endDate) throws MalformedURLException, IOException, DODSException {
        if ((dataContainer == null) || (startDate == null) || (endDate == null)) {
            throw new NullPointerException(
                "Arguments to TimeIndexNetcdfAccess may not be null");
        }

        // Set the local variables
        this.dataContainer = dataContainer;
        this.startDate = startDate;
        this.endDate = endDate;

        // Now construct the netcdf file
        netcdfFile = getNetcdfFile(dataContainer);
    }

    /**
     * This method constructs the netcdf file from the given
     * <code>IDataContainer</code>
     * 
     * @param dataContainer
     *            is the <code>DataContainer</code> that points to the file to
     *            create the NetcdfFile object from
     * @return a <code>NetcdfFile</code> object that points to the NetCDF file
     *         that the input <code>DataContainer</code> points to.
     * @throws MalformedURLException
     * @throws IOException
     * @throws DODSException
     */
    private NetcdfFile getNetcdfFile(DataContainer dataContainer)
        throws MalformedURLException, IOException, DODSException {
        if (dataContainer.isDodsAccessible().booleanValue()) {
            return new DODSNetcdfFile(dataContainer.getUrl().toExternalForm());
        } else {
            NetcdfFile netcdfFileToReturn = new NetcdfFile(dataContainer
                .getUrl());
            if (netcdfFileToReturn == null) {
                logger.error("netcdfFile from the URL "
                    + dataContainer.getUrl() + " returned null!!!!");
            } else {
                logger.debug("netcdfFile found: "
                    + netcdfFileToReturn.toString());
            }
            return netcdfFileToReturn;
        }
    }

    /**
     * This method return a <code>Map</code> that has
     * <code>RecordVariable</code>s as keys and
     * <code>ucar.nc2.Variable</code> as values
     * 
     * @return <code>Map</code>
     */
    private Map getVariableMap() {

        // Map the SSDS variable to the netcdf variable
        if (variableMap == null) {
            // Map the recordVariables ot their short names
            Map nameMap = new HashMap();
            Collection ssdsVar = dataContainer.getRecordDescription()
                .getRecordVariables();
            for (Iterator i = ssdsVar.iterator(); i.hasNext();) {
                RecordVariable rv = (RecordVariable) i.next();
                nameMap.put(rv.getName(), rv);
            }

            // If the name of the recordVariable matches the netcdf variables
            // name add them to the map
            variableMap = new HashMap();
            List ncVar = netcdfFile.getVariables();
            for (Iterator i = ncVar.iterator(); i.hasNext();) {
                Variable v = (Variable) i.next();
                String shortName = v.getName();
                if (nameMap.containsKey(shortName)) {
                    variableMap.put(nameMap.get(shortName), v);
                }
            }
        }
        return variableMap;
    }

    /**
     * @see IDataAccess#getData(String)
     */
    public Object[] getData(String recordVariableName) {
        // Loop through the list of record variables and find the one
        // that matches the given name and return its data.
        Set rvs = getVariableMap().keySet();
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
     * @see IDataAccess#getData(RecordVariable)
     */
    public Object[] getData(RecordVariable rv) {

        // The data objects to return
        logger.debug("Going to try to find data for variable with name "
            + rv.getName());
        Object[] out = null;

        // Initialize times if needed
        if (times == null) {
            initializeTime();
        }

        // First find the record variable in the keys to the map of
        // netcdf variables and grab it
        RecordVariable matchedRV = rv;

        Set rvs = getVariableMap().keySet();
        Iterator iterator = rvs.iterator();
        while (iterator.hasNext()) {
            RecordVariable tempRV = (RecordVariable) iterator.next();
            if ((tempRV.getId() != null) && (rv.getId() != null)
                && (tempRV.getId().longValue() == rv.getId().longValue())) {
                matchedRV = tempRV;
                break;
            } else if ((tempRV.getName() != null) && (rv.getName() != null)
                && (tempRV.getName().equalsIgnoreCase(rv.getName()))
                && (tempRV.getColumnIndex() == rv.getColumnIndex())) {
                matchedRV = tempRV;
            }
        }

        // Look for it in the map of Netcdf variables
        if (getVariableMap().containsKey(matchedRV)) {
            Variable v = (Variable) variableMap.get(matchedRV);
            if (v != null) {
                logger.debug("Found netcdf variable that matched and has name "
                    + v.getName() + " and DataType " + v.getDataType());
                if ((endIndex - startIndex) > 0) {
                    // Get the shape of the netcdf file
                    int[] shape = {endIndex - startIndex + 1};
                    // Get the origin of the netcdf file
                    int[] origin = {startIndex};
                    // The array to read into
                    Array a = null;
                    // Now read it
                    try {
                        a = v.read(origin, shape);
                    } catch (IOException e) {
                        logger.error("IOException caught: " + e.getMessage());
                    } catch (InvalidRangeException e) {
                        logger.error("InvalidRangeException caught: "
                            + e.getMessage());
                    }
                    // Check to see if something was returned
                    if (a != null) {
                        if (v.getDataType() == DataType.INT) {
                            int[] data = (int[]) a.copyTo1DJavaArray();
                            Integer[] integerData = new Integer[data.length];
                            for (int j = 0; j < data.length; j++) {
                                integerData[j] = new Integer(data[j] + "");
                            }
                            logger.debug("Array of " + data.length
                                + " integers will be returned");
                            out = integerData;
                        } else if (v.getDataType() == DataType.DOUBLE) {
                            double[] data = (double[]) a.copyTo1DJavaArray();
                            Double[] doubleData = new Double[data.length];
                            for (int j = 0; j < data.length; j++) {
                                doubleData[j] = new Double(data[j] + "");
                            }
                            logger.debug("Array of " + data.length
                                + " doubles will be returned");
                            out = doubleData;
                        } else if (v.getDataType() == DataType.FLOAT) {
                            float[] data = (float[]) a.copyTo1DJavaArray();
                            Float[] floatData = new Float[data.length];
                            for (int j = 0; j < data.length; j++) {
                                floatData[j] = new Float(data[j] + "");
                            }
                            logger.debug("Array of " + data.length
                                + " floats will be returned");
                            out = floatData;
                        } else if (v.getDataType() == DataType.LONG) {
                            long[] data = (long[]) a.copyTo1DJavaArray();
                            Long[] longData = new Long[data.length];
                            for (int j = 0; j < data.length; j++) {
                                longData[j] = new Long(data[j] + "");
                            }
                            logger.debug("Array of " + data.length
                                + " longs will be returned");
                            out = longData;
                        } else if (v.getDataType() == DataType.SHORT) {
                            short[] data = (short[]) a.copyTo1DJavaArray();
                            Short[] shortData = new Short[data.length];
                            for (int j = 0; j < data.length; j++) {
                                shortData[j] = new Short(data[j] + "");
                            }
                            logger.debug("Array of " + data.length
                                + " shorts will be returned");
                            out = shortData;
                        } else if (v.getDataType() == DataType.STRING) {
                            String[] data = (String[]) a.copyTo1DJavaArray();
                            logger.debug("Array of " + data.length
                                + " strings will be returned");
                            out = data;
                        }
                    } else {
                        logger.error("Data array was null");
                    }
                }
            }
        }
        return out;
    }

    /**
     * @see ITimeIndexedDataAccess#getTime()
     */
    public Object[] getTime() {

        logger.debug("getTime called");
        // Check to see if time needs to be initialized
        if (times == null) {
            initializeTime();
        }

        // Get the array and return it (try to cast into array of Longs)
        Object[] timesToReturn = times.toArray(new Long[times.size()]);

        return timesToReturn;
    }

    /**
     * @see ITimeIndexedDataAccess#getStartDate()
     */
    public Date getStartDate() {
        return this.startDate;
    }

    /**
     * @see ITimeIndexedDataAccess#getEndDate()
     */
    public Date getEndDate() {
        return this.getEndDate();
    }

    /**
     * A method to initialize the array of time objects from the netcdf file
     */
    private void initializeTime() {

        logger.debug("initializeTime called");
        if (times == null) {
            times = new ArrayList();
            // Check for netcdf file
            if (netcdfFile != null) {
                // Lookup the netcdf variable of time
                Variable timeVariable = netcdfFile.findVariable("time");
                if (timeVariable != null) {
                    Array timeArray = null;
                    try {
                        timeArray = timeVariable.read();
                    } catch (IOException e) {
                        logger
                            .error("IOException caught while trying to read time:"
                                + e.getMessage());
                    }
                    // Now copy over times to double array
                    double[] doubleTimes = (double[]) timeArray
                        .copyTo1DJavaArray();
                    // Check if OK
                    if (doubleTimes != null) {
                        boolean startIndexFound = false;
                        // Loop through double times to find section that is
                        // valid
                        // over range of start/end times that were specified
                        for (int i = 0; i < doubleTimes.length; i++) {
                            Calendar tempCal = Calendar.getInstance();
                            tempCal.setTimeZone(TimeZone.getTimeZone("GMT"));
                            tempCal
                                .setTimeInMillis((new Double(doubleTimes[i]))
                                    .longValue() * 1000);
                            if ((tempCal.getTime().after(startDate) || tempCal
                                .getTime().equals(startDate))
                                && (tempCal.getTime().before(endDate) || tempCal
                                    .getTime().equals(endDate))) {
                                // Set the index flag to true
                                if (!startIndexFound) {
                                    startIndexFound = true;
                                    startIndex = i;
                                }
                                endIndex = i;
                                times
                                    .add(new Long(tempCal.getTime().getTime()));
                            }
                        }
                    }
                } else {
                    logger.error("Could not find time variable in netcdf file");
                }
            } else {
                logger.error("netcdfFile is null!!!");
            }
        }
    }

    /**
     * @see IDataAccess#getDataContainer()
     */
    public DataContainer getDataContainer() {
        return dataContainer;
    }

    /**
     * @see IDataAccess#getRecordVariables()
     */
    public Collection getRecordVariables() {
        return new ArrayList(this.dataMap.keySet());
    }

    /**
     * This is the <code>DataContainer</code> that points to the location of
     * the file that has the data we are interested in
     */
    private final DataContainer dataContainer;

    /**
     * This is the <code>NetcdfFile</code> that points the the same file that
     * is specified in the <code>IDataFile</code>
     */
    private final NetcdfFile netcdfFile;

    /**
     * This is the inclusive start time of the data included in this
     * <code>ITimeIndexedDataAccess</code>
     */
    private final Date startDate;

    /**
     * This is the inclusive end time of the data included in this
     * <code>ITimeIndexedDataAccess</code>
     */
    private final Date endDate;

    /**
     * This is a <code>List</code> of the time objects that are associated
     * with the corresponding data objects
     */
    private List times = null;

    /**
     * TODO KJG - Document this
     */
    private int startIndex = 0;

    /**
     * TODO KJG - Document this
     */
    private int endIndex = 0;

    /**
     * This is a <code>Map</code> that holds the
     * <code>moos.ssds.model.IRecordVariable</code> as the key and the
     * corresponding netcdf <code>ucar.nc2.Variable</code> as the value
     */
    private Map variableMap;

    /**
     * This is a <code>Map</code> that holds the <code>RecordVariable</code>
     * as the key and an array of <code>Number</code>s that are the data for
     * that <code>RecordVariable</code>
     */
    private final Map dataMap = new HashMap();

    /**
     * A log4j logger
     */
    static Logger logger = Logger.getLogger(TimeIndexedNetcdfAccess.class);
}