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
package moos.ssds.data.converters;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import moos.ssds.data.ITimeIndexedDataAccess;
import moos.ssds.data.TimeIndexedDataAccessFactory;
import moos.ssds.data.parsers.IParser;
import moos.ssds.data.parsers.Parser;
import moos.ssds.data.parsers.ParsingException;
import moos.ssds.data.parsers.VariableFormatMap;
import moos.ssds.data.util.DataException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.DateRange;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.services.metadata.DataProducerAccess;
import moos.ssds.services.metadata.DataProducerAccessHome;
import moos.ssds.services.metadata.DataProducerAccessLocal;
import moos.ssds.services.metadata.DataProducerAccessLocalHome;
import moos.ssds.services.metadata.DataProducerAccessUtil;
import moos.ssds.util.XmlDateFormat;

import org.apache.log4j.Logger;

import ucar.ma2.ArrayAbstract;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

/**
 * This class takes in a <code>DataContainer</code> at construction time and
 * then provides the methods to create a NetCDF file that contains the same data
 * that is in the <code>DataContainer</code>. This only works if the
 * <code>RecordDescription</code> and all <code>RecordVariable</code>s are
 * described and the source <code>DataContainer</code> is parseable.
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @created November 15, 2002
 * @version : $Revision: 1.1.2.5 $
 */
public class NetcdfConverter {

    /**
     * This is the constructor for the NetCDFConverter.
     * 
     * @param dataContainer
     *            is the <code>DataContainer</code> that contains the metadata
     *            and pointer to the source of data to create a NetCDF file from
     * @param netcdfFile
     *            is the <code>java.io.File</code> that will be used to house
     *            the NetCDF data.
     * @throws ParsingException
     *             if the file referenced by the <code>DataContainer</code>
     *             cannot be parsed
     * @throws IOException
     *             if something goes wrong in the IO
     */
    public NetcdfConverter(DataContainer dataContainer, File netcdfFile)
        throws ParsingException, IOException {

        // Assign the incoming variables to local ones
        this.dataContainer = dataContainer;
        this.netcdfFile = netcdfFile;

        // Check to make sure incoming variables are valid
        if ((dataContainer == null)
            || (dataContainer.getRecordDescription() == null)) {
            throw new ParsingException("Incoming flatFile was null or "
                + "its RecordDescription was null. The file cannot be parsed.");
        }
        if (!dataContainer.getRecordDescription().isParseable().booleanValue()) {
            throw new ParsingException(dataContainer.getUrl()
                + " is defined as unparseable in it's RecordDescription");
        }

        // Make sure the file specified by the DataContainer exists
        if (!doesDataContainerExist(dataContainer)) {
            throw new IOException(
                "DataContainer specified does not exist at the given URL");
        }
        logger.debug("NetCDFCreate instantiated with DataContainer: "
            + dataContainer.toStringRepresentation("|") + " and NetCDF file: "
            + netcdfFile.toString());
    }

    /**
     * This is the method that is called to actually execute the creation of the
     * NetCDF file
     * 
     * @throws IOException
     */
    public void create() throws IOException {

        // Try to create an ITimeIndexedDataAccess from the
        // <code>DataContainer</code>
        logger.debug("Create called.");
        try {
            logger
                .debug("Going to build TimeIndexedDataAccess for the DataContainer");
            this.timeIndexedDataAccess = TimeIndexedDataAccessFactory
                .getTimeIndexedDataAccess(this.dataContainer, null, null, null);
        } catch (DataException e) {
            logger
                .debug("Could not create a time indexed data access for the NetCDF creator");
            this.timeIndexedDataAccess = null;
        } catch (Exception e) {
            logger
                .error("Exception caught trying to build time indexed data access: "
                    + e.getMessage());
        }

        // If not able, create the parser
        if (this.timeIndexedDataAccess == null) {
            this.parser = new Parser(this.dataContainer);
        }

        // Create or get the specified NetCDFWriteable
        this.makeNetcdfFileWriteable();

        this.initLog();
        this.addData();
        netcdfWriteable.close();

        // Set the flag to indicate that the file was created
        this.fileCreated = true;
    }

    /**
     * Description of the Method
     * 
     * @exception IOException
     *                Description of the Exception
     */
    public void close() throws IOException {
        netcdfWriteable.close();
    }

    /**
     * Return start and end time parsed from the flatFile
     * 
     * @return a DateRange object with start and end dates set
     */
    public DateRange getDateRange() {
        DateRange dr = new DateRange();

        if (startEMilliSecs != null) {
            dr.setStartDate(new Date(startEMilliSecs.longValue()));
        }
        if (endEMilliSecs != null) {
            dr.setEndDate(new Date(endEMilliSecs.longValue()));
        }
        return dr;
    }

    /**
     * This method returns
     * 
     * @return
     */
    public File getNetcdfFile() {
        return netcdfFile;
    }

    /**
     * Gets the netcdfFile attribute of the NetcdfBucket object
     * 
     * @return The netcdfFile value
     */
    public NetcdfFileWriteable getNetcdfWriteable() {
        return netcdfWriteable;
    }

    /**
     * @return Returns the logText.
     */
    public String getLogText() {
        return logText.toString();
    }

    /**
     * @param logText
     */
    public void setLogText(String logText) {
        // Clear out the old
        this.logText = new StringBuffer();
        // Bring in the new
        this.logText.append(logText);
    }

    /**
     * Add a test string to the logText
     * 
     * @param text
     */
    public void appendLogText(String text) {
        if (this.logText == null) {
            this.logText = new StringBuffer();
        }
        this.logText.append(text + "\n");
        logger.debug("AppendToLog: " + text);
    }

    /**
     * This method returns the number of records in the NetCDF file
     * 
     * @return
     */
    public Long getNumberOfRecords() {
        return numberOfRecords;
    }

    /**
     * Getter method for the geospatial extents of the NetCDF file created
     * 
     * @return
     */
    public Number getMinLatitude() {
        return minLatitude;
    }

    public Number getMaxLatitude() {
        return maxLatitude;
    }

    public Number getMeanLatitude() {
        return meanLatitude;
    }

    public Number getMinLongitude() {
        return minLongitude;
    }

    public Number getMaxLongitude() {
        return maxLongitude;
    }

    public Number getMeanLongitude() {
        return meanLongitude;
    }

    public Number getMinDepth() {
        return minDepth;
    }

    public Number getMaxDepth() {
        return maxDepth;
    }

    public Number getMeanDepth() {
        return meanDepth;
    }

    /**
     * The method returns the boolean that indicates if the NetCDF file was
     * created yet
     * 
     * @return
     */
    public boolean isFileCreated() {
        return fileCreated;
    }

    /**
     * This is the method that takes the data that is backing the
     * <code>DataContainer</code> reads it all in and writes it out the the
     * NetCDF file.
     */
    public void addData() {
        // There are two cases here. Either we will be reading from the
        // ITimeIndexedDataAccess, or from the IParser
        if (timeIndexedDataAccess != null) {
            // Now iterate over the RecordVariables
            Iterator rvIter = this.dataContainer.getRecordDescription()
                .getRecordVariables().iterator();
            while (rvIter.hasNext()) {
                // Grab the record variable
                RecordVariable rv = (RecordVariable) rvIter.next();
                // Grab the array of data values for the RecordVariable
                Object[] dataValues = timeIndexedDataAccess.getData(rv);

                // Write the values
                try {
                    write(rv, dataValues);
                } catch (Exception e) {
                    logger
                        .error("IOException caught trying to write data to variable "
                            + rv.getName() + ": " + e.getMessage());
                }
            }
        } else if (parser != null) {
            // TODO kgomes Implement this
        }
    }

    /**
     * This method checks to see if the URL specified in the dataContainer
     * actually has a file at the end of it
     * 
     * @param dataContainer
     * @return
     */
    private boolean doesDataContainerExist(DataContainer dataContainer) {
        logger.debug("Will check if DataContainer at URL "
            + dataContainer.getUrl() + " exists");
        // The exist flag to return
        boolean exists = false;
        // The URL
        URL dcUrl = dataContainer.getUrl();
        if (dcUrl == null)
            return false;
        // Setup the HTTP connection
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) dcUrl.openConnection();
            conn.setRequestMethod("HEAD");
            if (conn.getResponseCode() == 200) {
                exists = true;
            }
        } catch (Exception e) {
            return exists;
        } finally {
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception e) {}
            }
        }
        return exists;
    }

    /**
     * Generates a Netcdf file from a parseable metadata contained in the source
     * <code>DataContainer</code>.
     * 
     * @throws IOException
     */
    private void makeNetcdfFileWriteable() throws IOException {

        // Check to see if the file exists already, if it does open it and set
        // the flag to existing file
        if (!this.netcdfFile.exists()) {

            // The first thing to do is create the directory structure
            File parentFile = netcdfFile.getParentFile();
            if (!parentFile.exists())
                parentFile.mkdirs();

            // Create the new netCDF file and set the flag
            // NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(
            // netcdfFile.getAbsolutePath(), false);
            NetcdfFileWriteable ncfile = new NetcdfFileWriteable();
            ncfile.setName(netcdfFile.getAbsolutePath());

            // Define the coordinate dimension (either index or time)
            Dimension[] dims = null;

            // If the time index data access was successfully created, use a
            // time based one, otherwise, use an index
            String dimName = "index";
            if (timeIndexedDataAccess != null) {
                dimName = "time";
            }

            // Define dimensions
            Dimension dim = ncfile.addDimension(dimName, -1);
            dims = new Dimension[1];
            dims[0] = dim;

            // Define global attributes
            makeGlobalAttributes(ncfile);

            // Create the rest of the NetCDF variables
            makeVariables(ncfile, dims);

            // Now create the NetCDF file
            logger.debug("Will now create file: " + ncfile.getPathName());
            ncfile.create();

            // Assign it to the local NetCDFWriteable
            netcdfWriteable = ncfile;
        }
    }

    /**
     * This method sets up and global attributes of the NetCDF file
     * 
     * @param ncfile
     */
    private void makeGlobalAttributes(NetcdfFileWriteable ncfile) {

        // Create a title
        String title = dataContainer.getDescription();
        if (title != null) {
            ncfile.addGlobalAttribute("title", title);
        }

        // Basic attributes
        Date creationDate = new Date();
        ncfile.addGlobalAttribute("created", DATE_FORMAT.format(creationDate));
        ncfile.addGlobalAttribute("source", dataContainer.getUriString());
        ncfile.addGlobalAttribute("history", DATE_FORMAT.format(creationDate)
            + ": Netcdf file auto-generated by SSDS using "
            + this.getClass().getName());

        // Grab the creator from SSDS (try local, then remote)
        DataProducerAccessLocalHome dpalh = null;
        DataProducerAccessLocal dpal = null;
        DataProducerAccessHome dpah = null;
        DataProducerAccess dpa = null;
        DataProducer producer = null;
        DataProducer parentDataProducer = null;
        try {
            dpalh = DataProducerAccessUtil.getLocalHome();
            dpal = dpalh.create();
            producer = dpal.findByOutput(this.dataContainer, null, null, false);
            parentDataProducer = dpal.findParentDataProducer(producer, false);
        } catch (Exception e) {}
        if (producer == null) {
            try {
                dpah = DataProducerAccessUtil.getHome();
                dpa = dpah.create();
                producer = dpa.findByOutput(this.dataContainer, null, null,
                    false);
                parentDataProducer = dpa
                    .findParentDataProducer(producer, false);
            } catch (Exception e) {}
        }

        // If the producer was found, add some metadata about it
        if ((producer != null)
            && (producer.getDataProducerType()
                .equals(DataProducer.TYPE_DEPLOYMENT))) {
            if (parentDataProducer != null) {
                ncfile.addGlobalAttribute("deploymentName", parentDataProducer
                    .getName());
            } else {
                ncfile.addGlobalAttribute("deploymentName",
                    "No parent deployment in available");
            }
            Device instrument = producer.getDevice();
            if (instrument != null) {
                ncfile.addGlobalAttribute("instrumentId", instrument.getId()
                    + "");
                ncfile.addGlobalAttribute("instrumentUUID", instrument
                    .getUuid()
                    + "");
                ncfile.addGlobalAttribute("instrumentName", instrument
                    .getName()
                    + "");
                if (instrument.getDeviceType() != null) {
                    ncfile.addGlobalAttribute("instrumentType", instrument
                        .getDeviceType().getName()
                        + "");
                }
                ncfile.addGlobalAttribute("instrumentMfg", instrument
                    .getMfgName()
                    + "");
                ncfile.addGlobalAttribute("instrumentSerialNumber", instrument
                    .getMfgSerialNumber()
                    + "");
                ncfile.addGlobalAttribute("instrumentModel", instrument
                    .getMfgModel()
                    + "");
            }
        }
    }

    /**
     * This method sets up the variable that will match the variables in the
     * <code>RecordVariable</code> collection associated with the
     * <code>DataContainer</code>.
     * 
     * @param ncfile
     * @param dims
     */
    private void makeVariables(NetcdfFileWriteable ncfile, Dimension[] dims) {

        // Get all variables from the RecordDescription and iterate over them
        Collection variables = dataContainer.getRecordDescription()
            .getRecordVariables();
        Iterator iterator = variables.iterator();

        // Avoid object thrashing
        RecordVariable recordVariable = null;
        String recordVariableName = null;
        Class format = null;

        while (iterator.hasNext()) {

            // Grab the next variable and its data type
            recordVariable = (RecordVariable) iterator.next();
            recordVariableName = recordVariable.getName();
            if (recordVariableName.equalsIgnoreCase("time")) {
                recordVariableName = "time";
            }
            format = (Class) typeMap.get(recordVariable.getFormat());
            // DataType dataType = DataType.getType(format);

            // If the format is defined, create the variable for it
            // if (dataType != null) {
            if (format != null) {
                // ncfile.addVariable(recordVariableName, dataType, dims);
                ncfile.addVariable(recordVariableName, format, dims);
                ncfile.addVariableAttribute(recordVariableName, "long_name",
                    recordVariable.getLongName());
                ncfile.addVariableAttribute(recordVariableName, "units",
                    recordVariable.getUnits());
                String missingValueString = recordVariable.getMissingValue();

                if ((missingValueString != null)
                    && (!missingValueString.equals(""))) {
                    Number missingValue = null;
                    if (format == byte.class) {
                        missingValue = Byte.valueOf(missingValueString);
                    } else if (format == short.class) {
                        missingValue = Short.valueOf(missingValueString);
                    } else if (format == int.class) {
                        missingValue = Integer.valueOf(missingValueString);
                    } else if (format == long.class) {
                        missingValue = Long.valueOf(missingValueString);
                    } else if (format == float.class) {
                        missingValue = Float.valueOf(missingValueString);
                    }
                    // if no match is found use a double
                    else {
                        missingValue = Double.valueOf(missingValueString);
                    }
                    ncfile.addVariableAttribute(recordVariableName,
                        "missing_value", missingValue);
                    ncfile.addVariableAttribute(recordVariableName,
                        "_FillValue", missingValue);
                }
            }
        }
    }

    /**
     * This method writes the data to the variable in the NetCDF file
     * 
     * @param recordVariable
     * @param dataValues
     * @throws IOException
     * @throws InvalidRangeException
     */
    private void write(RecordVariable recordVariable, Object[] dataValues)
        throws IOException, InvalidRangeException {

        // First check length against number of records
        if ((numberOfRecords == null)
            || (dataValues.length > numberOfRecords.longValue())) {
            numberOfRecords = new Long(dataValues.length);
        }

        // First set a flag to indicate if the RV is a geospatial RV
        boolean geospatialRV = false;
        boolean longitudeRV = false;
        boolean timeRV = false;
        if (recordVariable.getName().equalsIgnoreCase("latitude")
            || recordVariable.getName().equalsIgnoreCase("longitude")
            || recordVariable.getName().equalsIgnoreCase("depth")) {
            geospatialRV = true;
            if (recordVariable.getName().equalsIgnoreCase("longitude"))
                longitudeRV = true;
        }
        String ucRVName = recordVariable.getName().toUpperCase();
        if (ucRVName.indexOf("TIME") >= 0) {
            timeRV = true;
        }

        // Create some variables to track extents
        Number minValue = null;
        Number maxValue = null;
        Number meanValue = null;

        // Grab the first object from the data
        Object firstPoint = dataValues[0];
        // Cast it to a number
        Number number = null;
        try {
            number = (Number) firstPoint;
        } catch (Throwable e) {}
        if (number != null) {
            // Convert the array to the appropriate primitives and then write to
            // the file
            if (number instanceof Byte) {
                byte[] byteArray = new byte[dataValues.length];
                for (int i = 0; i < dataValues.length; i++) {
                    byteArray[i] = ((Byte) dataValues[i]).byteValue();
                }
                netcdfWriteable.write(recordVariable.getName(), ArrayAbstract
                    .factory(byteArray));
            } else if (number instanceof Short) {
                short[] shortArray = new short[dataValues.length];
                for (int i = 0; i < dataValues.length; i++) {
                    shortArray[i] = ((Short) dataValues[i]).shortValue();
                }
                netcdfWriteable.write(recordVariable.getName(), ArrayAbstract
                    .factory(shortArray));
            } else if (number instanceof Integer) {
                int minInt = Integer.MAX_VALUE;
                int maxInt = -1 * Integer.MAX_VALUE;
                int sumInt = 0;
                int[] intArray = new int[dataValues.length];
                for (int i = 0; i < dataValues.length; i++) {
                    intArray[i] = ((Integer) dataValues[i]).intValue();
                    // If it is a geospatial value, track it
                    if (geospatialRV) {
                        if (intArray[i] < minInt)
                            minInt = intArray[i];
                        if (intArray[i] > maxInt)
                            maxInt = intArray[i];
                        sumInt += intArray[i];
                    }
                }
                // Write to NetCDF file
                netcdfWriteable.write(recordVariable.getName(), ArrayAbstract
                    .factory(intArray));

                // Calculate stats if needed
                if (geospatialRV) {
                    double meanDouble = sumInt / dataValues.length;
                    minValue = new Integer(minInt);
                    maxValue = new Integer(maxInt);
                    meanValue = new Double(meanDouble);
                }
            } else if (number instanceof Long) {
                long minLong = Long.MAX_VALUE;
                long maxLong = -1 * Long.MAX_VALUE;
                long sumLong = 0;
                long[] longArray = new long[dataValues.length];
                for (int i = 0; i < dataValues.length; i++) {
                    longArray[i] = ((Long) dataValues[i]).longValue();
                    // If it is a geospatial value, track it
                    if (geospatialRV) {
                        if (longArray[i] < minLong)
                            minLong = longArray[i];
                        if (longArray[i] > maxLong)
                            maxLong = longArray[i];
                        sumLong += longArray[i];
                    }
                    // Check for time
                    if (timeRV) {
                        Long tempTime = null;
                        if (longArray[i] < 1000000000000L) {
                            tempTime = new Long(longArray[i] * 1000);
                        } else {
                            tempTime = new Long(longArray[i]);

                        }
                        if ((startEMilliSecs == null)
                            || (longArray[i] < startEMilliSecs.longValue())) {
                            startEMilliSecs = tempTime;
                        }
                        if ((endEMilliSecs == null)
                            || (longArray[i] > endEMilliSecs.longValue())) {
                            endEMilliSecs = tempTime;
                        }
                    }
                }
                // Write to NetCDF File
                netcdfWriteable.write(recordVariable.getName(), ArrayAbstract
                    .factory(longArray));

                // Calculate stats if needed
                if (geospatialRV) {
                    double meanDouble = sumLong / dataValues.length;
                    minValue = new Long(minLong);
                    maxValue = new Long(maxLong);
                    meanValue = new Double(meanDouble);
                }

            } else if (number instanceof Float) {
                float minFloat = Float.MAX_VALUE;
                float maxFloat = -1 * Float.MAX_VALUE;
                float sumFloat = 0;
                float[] floatArray = new float[dataValues.length];
                for (int i = 0; i < dataValues.length; i++) {
                    floatArray[i] = ((Float) dataValues[i]).floatValue();
                    // If it is a geospatial value, track it
                    if (geospatialRV) {
                        if (floatArray[i] < minFloat)
                            minFloat = floatArray[i];
                        if (floatArray[i] > maxFloat)
                            maxFloat = floatArray[i];
                        sumFloat += floatArray[i];
                    }
                    // Check for time
                    if (timeRV) {
                        Float tempTime = null;
                        if (floatArray[i] < 1000000000000.0) {
                            tempTime = new Float(floatArray[i] * 1000);
                        } else {
                            tempTime = new Float(floatArray[i]);

                        }
                        if ((startEMilliSecs == null)
                            || (tempTime.longValue() < startEMilliSecs
                                .longValue())) {
                            startEMilliSecs = new Long(tempTime.longValue());
                        }
                        if ((endEMilliSecs == null)
                            || (tempTime.longValue() > endEMilliSecs
                                .longValue())) {
                            endEMilliSecs = new Long(tempTime.longValue());
                        }
                    }
                }
                // Write to NetCDF File
                netcdfWriteable.write(recordVariable.getName(), ArrayAbstract
                    .factory(floatArray));

                // Calculate stats if needed
                if (geospatialRV) {
                    double meanDouble = sumFloat / dataValues.length;
                    minValue = new Float(minFloat);
                    maxValue = new Float(maxFloat);
                    meanValue = new Float(meanDouble);
                }
            } else if (number instanceof Double) {
                double minDouble = Double.MAX_VALUE;
                double maxDouble = -1 * Double.MAX_VALUE;
                double sumDouble = 0;
                double[] doubleArray = new double[dataValues.length];
                for (int i = 0; i < dataValues.length; i++) {
                    doubleArray[i] = ((Double) dataValues[i]).doubleValue();
                    // Commented this out after a discussion with Mike McCann
                    // and we both felt this should be fixed in the vehicle
                    // software
                    // if (longitudeRV) {
                    // // TODO kgomes - This is a MAJOR HACK and will cause
                    // // problems when the longitude is in radians and is
                    // // operating in the eastern hemisphere, but I did
                    // // not have any choice. The AUV's at MBARI output
                    // // positive radians no matter what hemisphere they
                    // // are operating in and at the time of this writing,
                    // // there was no way to get the hemisphere from the
                    // // GPS data. So I am going to make the assumption
                    // // that if the longitude data looks like it is in
                    // // radians and is postive, this is incorrect and
                    // // should be negative. I know, I know, major flag
                    // // here, but don't see any other way at this point.
                    // // Essentially what this is saying is that anytime
                    // // longitude is specified in radians and is a
                    // // positive number (which normally means it is
                    // // operating in the eastern hemisphere), we will
                    // // flip its location on the globe to western
                    // // hemisphere.
                    // if ((doubleArray[i] > 0) && (doubleArray[i] < Math.PI))
                    // doubleArray[i] = -1 * doubleArray[i];
                    // }

                    // If it is a geospatial value, track it
                    if (geospatialRV) {
                        if (doubleArray[i] < minDouble)
                            minDouble = doubleArray[i];
                        if (doubleArray[i] > maxDouble)
                            maxDouble = doubleArray[i];
                        sumDouble += doubleArray[i];
                    }
                    // Check for time
                    if (timeRV) {
                        Double tempTime = null;
                        if (doubleArray[i] > 631180800) {
                            if (doubleArray[i] < 1000000000000.0) {
                                tempTime = new Double(doubleArray[i] * 1000);
                            } else {
                                tempTime = new Double(doubleArray[i]);

                            }
                            if ((startEMilliSecs == null)
                                || (tempTime.longValue() < startEMilliSecs
                                    .longValue())) {
                                startEMilliSecs = new Long(tempTime.longValue());
                            }
                            if ((endEMilliSecs == null)
                                || (tempTime.longValue() > endEMilliSecs
                                    .longValue())) {
                                endEMilliSecs = new Long(tempTime.longValue());
                            }
                        }
                    }
                }
                // Write to NetCDF file
                netcdfWriteable.write(recordVariable.getName(), ArrayAbstract
                    .factory(doubleArray));

                // Calculate stats if needed
                if (geospatialRV) {
                    double meanDouble = sumDouble / dataValues.length;
                    minValue = new Double(minDouble);
                    maxValue = new Double(maxDouble);
                    meanValue = new Double(meanDouble);
                }
            }
        }

        // Now if this was a geospatial variable, record the extents
        if (geospatialRV) {
            if (recordVariable.getName().equalsIgnoreCase("latitude")) {
                minLatitude = minValue;
                maxLatitude = maxValue;
                meanLatitude = meanValue;
            } else if (recordVariable.getName().equalsIgnoreCase("longitude")) {
                minLongitude = minValue;
                maxLongitude = maxValue;
                meanLongitude = meanValue;
            } else if (recordVariable.getName().equalsIgnoreCase("depth")) {
                minDepth = minValue;
                maxDepth = maxValue;
                meanDepth = meanValue;
            }
        }
    }

    /**
     * Initialize the logging message
     */
    private void initLog() {

        // Initilize the message buffer
        this.logText = new StringBuffer();

        // Create date that is the current time
        Date d = new Date();
        appendLogText("addData() started " + d.toString() + "\n");
        appendLogText("The software that builds this netCDF is fairly well decomposed ");
        appendLogText("amongst several classes in the SSDS project.  These classes include");
        appendLogText("but are not limited to:");
        appendLogText("  moos.ssds.parsers.BinaryFileContext#skipHeader(): generates some output below");
        appendLogText("  moos.ssds.parsers.NetcdfCreator#addData(): generates some output below");
        appendLogText("  moos.ssds.tasks.MultigenerateNetcdfTask#doProcessRun(): generates some output below");
        appendLogText("\n");
    }

    /**
     * This is the <code>DataContainer</code> that contains the metadata and
     * pointers to the source that will be used to create the NetCDF file
     */
    private DataContainer dataContainer = null;

    /**
     * This is a <code>ITimeIndexedDataAccess</code> object that simplifies
     * the construction of the NetCDF file. I the <code>DataContainer</code>
     * is time based, the NetCDF will use a time dimension, otherwise, it will
     * use an index dimension.
     */
    private ITimeIndexedDataAccess timeIndexedDataAccess = null;

    /**
     * This is the parser that will be used to parse data if a
     * ITimeIndexedDataAccess can't be used
     */
    private IParser parser = null;

    /**
     * This is the file that will be used to store the NetCDF file
     */
    private File netcdfFile = null;

    /**
     * This is the file that is writeable as Netcdf
     */
    private NetcdfFileWriteable netcdfWriteable = null;

    /**
     * A boolean to indicated if the file has been created or not
     */
    private boolean fileCreated = false;

    /**
     * This is a map that can be used to link up names to data types
     */
    private static Map typeMap = VariableFormatMap.getInstance();

    /**
     * A StringBuffer used to store some logging information as a block for
     * later reading
     */
    private StringBuffer logText = new StringBuffer();

    /**
     * A Date formatting utility
     */
    private static final XmlDateFormat DATE_FORMAT = new XmlDateFormat();

    /**
     * This is a log4j logger
     */
    static Logger logger = Logger.getLogger(NetcdfConverter.class);

    /**
     * These are the start and end times in epochs
     */
    private Long startEMilliSecs;
    private Long endEMilliSecs;

    /**
     * This indicates how many records are in the NetCDF file
     */
    private Long numberOfRecords;

    /**
     * These are the geospatial extents of the data in the data container
     */
    private Number minLatitude = null;
    private Number maxLatitude = null;
    private Number meanLatitude = null;
    private Number minLongitude = null;
    private Number maxLongitude = null;
    private Number meanLongitude = null;
    private Number minDepth = null;
    private Number maxDepth = null;
    private Number meanDepth = null;
}