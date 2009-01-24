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
package moos.ssds.services.data.graphing;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.NamingException;

import moos.ssds.data.graphing.GpsCharter;
import moos.ssds.data.util.DataException;
import moos.ssds.data.util.LocationAndTime;
import moos.ssds.metadata.Device;
import moos.ssds.services.data.DeviceDataAccessLocal;
import moos.ssds.services.data.DeviceDataAccessLocalHome;
import moos.ssds.services.data.DeviceDataAccessUtil;
import moos.ssds.util.XmlDateFormat;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

/**
 * This class provides access to graphing functions for different SSDS data.
 * 
 * @author kgomes
 * @ejb.bean name="GeospatialGraphingAccess" type="Stateless"
 *           jndi-name="moos/ssds/services/data/graphing/GeospatialGraphingAccess"
 *           local-jndi-name="moos/ssds/services/data/graphing/GeospatialGraphingAccessLocal"
 *           view-type="both"
 * @ejb.home create="true"
 *           local-class="moos.ssds.services.data.graphing.GeospatialGraphingAccessLocalHome"
 *           remote-class="moos.ssds.services.data.graphing.GeospatialGraphingAccessHome"
 * @ejb.interface create="true"
 *                local-class="moos.ssds.services.data.graphing.GeospatialGraphingAccessLocal"
 *                remote-class="moos.ssds.services.data.graphing.GeospatialGraphingAccess"
 */
public class GeospatialGraphingAccessEJB implements SessionBean {

    /**
     * @see javax.ejb.SessionBean#ejbActivate()
     */
    public void ejbActivate() throws EJBException, RemoteException {}

    /**
     * @see javax.ejb.SessionBean#ejbPassivate()
     */
    public void ejbPassivate() throws EJBException, RemoteException {}

    /**
     * @see javax.ejb.SessionBean#ejbRemove()
     */
    public void ejbRemove() throws EJBException, RemoteException {}

    /**
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(SessionContext arg0) throws EJBException,
        RemoteException {}

    // The EJB Create callback
    public void ejbCreate() throws CreateException {
        logger.debug("ejbCreate called");
        logger.debug("Going to read in the properties");
        dataServiceProperties = new Properties();
        try {
            dataServiceProperties.load(this.getClass().getResourceAsStream(
                "/moos/ssds/services/data/servicesData.properties"));
        } catch (Exception e) {
            logger.error("Exception trying to read in properties file: "
                + e.getMessage());
        }

        // Make sure the properties were read from the JAR OK
        if (dataServiceProperties != null) {
            logger.debug("Loaded props OK");
            this.dataServicesGpsPlotLocation = dataServiceProperties
                .getProperty("data.services.gps.graph.location");
            logger.debug("GPS plot location is "
                + this.dataServicesGpsPlotLocation);
            this.dataServicesGpsPlotUrlBase = dataServiceProperties
                .getProperty("data.services.gps.graph.url.base");
            logger.debug("GPS plot base URL is "
                + this.dataServicesGpsPlotUrlBase);
        } else {
            logger.warn("Could not load the servicesData.properties.");
        }
    }

    /**
     * @throws CreateException
     */
    public void ejbPostCreate() throws CreateException {}

    /**
     * @ejb.interface-method view-type="both"
     */
    public String getGpsChart(Device device, Date startDate, Date endDate,
        boolean drawWatchCircle, double watchCircleDiameterInKm,
        boolean scaleChartToFitData, boolean plotAnchorLocation,
        double anchorLatitude, double anchorLongitude, int chartXSize,
        int chartYSize, String chartTitle) {

        // Make sure device is specified
        logger.debug("getGpsChart called");
        if (device == null) {
            logger.debug("Device was null so returning null");
            return null;
        }

        // Construct the base for file location and URL
        StringBuffer graphUrlBuffer = new StringBuffer();
        StringBuffer fileLocationBuffer = new StringBuffer();
        graphUrlBuffer.append(this.dataServicesGpsPlotUrlBase);
        fileLocationBuffer.append(this.dataServicesGpsPlotLocation);
        logger.debug("fileLocationBuffer at start is: "
            + fileLocationBuffer.toString());

        // Check to see if the base file location exists, if not create the
        // directory
        File tempBaseDirectory = new File(fileLocationBuffer.toString());
        if (!tempBaseDirectory.exists()) {
            logger.debug("That directory was not found, will create it");
            tempBaseDirectory.mkdir();
        } else {
            logger.debug("That directory already exists");
        }

        // Now check to make sure string end with URL and file separators
        if (!graphUrlBuffer.toString().endsWith("/"))
            graphUrlBuffer.append("/");
        if (!fileLocationBuffer.toString().endsWith(File.separator)) {
            fileLocationBuffer.append(File.separator);
            logger
                .debug("Appended file separator to file location buffer, now: "
                    + fileLocationBuffer.toString());
        } else {
            logger.debug("File separator already appended.");
        }

        // If the device ID was found, create a directory
        if (device.getId() != null) {
            graphUrlBuffer.append(device.getId().toString() + "/");
            fileLocationBuffer.append(device.getId().toString());
            logger.debug("After appending device ID, file location buffer is: "
                + fileLocationBuffer.toString());
            File tempDeviceDirectory = new File(fileLocationBuffer.toString());
            if (!tempDeviceDirectory.exists()) {
                logger
                    .debug("The device directory does not exist, will create it");
                boolean createdDir = tempDeviceDirectory.mkdir();
                logger.debug("Was directory created? " + createdDir);
            } else {
                logger
                    .debug("The directory with the device name already exists");
            }

            // Now add the device ID to the file name
            graphUrlBuffer.append(device.getId().toString() + "_");
            fileLocationBuffer.append(File.separator
                + device.getId().toString() + "_");
            logger
                .debug("After appending the file separator and device ID, file location buffer is "
                    + fileLocationBuffer.toString());
        }

        // Now if the chart title was specified, use that
        if (chartTitle != null) {
            // Clean up any wierd stuff from the title to make it a file name
            String fileName = chartTitle.replaceAll("\\s+", "_");
            fileName = fileName.replaceAll("\\(", "");
            fileName = fileName.replaceAll("\\)", "");
            fileName = fileName.replaceAll(",", "-");
            graphUrlBuffer.append(fileName);
            fileLocationBuffer.append(fileName);
            logger.debug("After cleaning up title, file name is: "
                + fileLocationBuffer.toString());
        } else {
            // Use the start and end dates if specified
            if (startDate != null) {
                graphUrlBuffer.append(xmlDateFormat.format(startDate));
                fileLocationBuffer.append(xmlDateFormat.format(startDate));
            }
            if (endDate != null) {
                graphUrlBuffer.append("_" + xmlDateFormat.format(endDate));
                fileLocationBuffer.append("_" + xmlDateFormat.format(endDate));
            }
        }
        // Attach the file extension
        graphUrlBuffer.append(".jpg");
        fileLocationBuffer.append(".jpg");
        logger.debug("Added the .jpg extension and finally the name is "
            + fileLocationBuffer.toString());

        // Now create the new file
        File chartFile = new File(fileLocationBuffer.toString());
        try {
            if (!chartFile.exists())
                chartFile.createNewFile();
        } catch (IOException e1) {
            logger.error("IOException caught trying to creat the graph file: "
                + e1.getMessage());
        }
        // Now use the service to grab all the LocationAndTime data for the
        // device
        Collection data = null;
        DeviceDataAccessLocalHome ddalh = null;
        try {
            ddalh = DeviceDataAccessUtil.getLocalHome();
        } catch (NamingException e) {
            logger.error("NamingException caught: " + e.getMessage());
        }
        DeviceDataAccessLocal ddal = null;
        try {
            if (ddalh != null)
                ddal = ddalh.create();
        } catch (CreateException e) {
            logger.error("CreateException caught: " + e.getMessage());
        }
        try {
            if (ddal != null)
                data = ddal.getGpsDeviceLocationAndTimes(device, startDate,
                    endDate);
        } catch (DataException e) {
            logger.error("DataException caught: " + e.getMessage());
        }
        logger.debug("Found " + data.size() + " packets");

        // OK, should have all the location and times now. Convert them to an
        // array
        LocationAndTime[] gpsLocations = (LocationAndTime[]) data
            .toArray(new LocationAndTime[data.size()]);
        logger
            .debug("Converted data collection to an array of LocationAndTime object ("
                + gpsLocations.length + " elements)");

        // Now convert the incoming doubles to a LocationAndTime for the anchor
        LocationAndTime anchorLocation = new LocationAndTime(new Double(
            anchorLatitude), new Double(anchorLongitude), null, null);
        logger.debug("Created an anchor location and time: "
            + anchorLocation.toString());

        // Construct the charting class
        GpsCharter gpsCharter = new GpsCharter(gpsLocations, drawWatchCircle,
            watchCircleDiameterInKm, scaleChartToFitData, plotAnchorLocation,
            anchorLocation);
        logger.debug("GpsCharter created");

        // Create the chart
        JFreeChart gpsChart = gpsCharter.getGpsPlot(chartTitle);
        logger.debug("gpsChart created");

        // Save it to disk
        try {
            ChartUtilities.saveChartAsJPEG(chartFile, gpsChart, chartXSize,
                chartYSize, new ChartRenderingInfo());
        } catch (IOException e) {
            logger.error("IOException trying to save as a JPEG: "
                + e.getMessage());
        }

        // Now return the result
        return graphUrlBuffer.toString();
    }

    /**
     * A Properties object to help configure the services
     */
    private Properties dataServiceProperties = null;

    /**
     * A date formatting utility
     */
    private XmlDateFormat xmlDateFormat = new XmlDateFormat();

    /**
     * This is the local directories where the various products will be stored
     */
    private String dataServicesGpsPlotLocation = null;
    private File dataServicesGpsPlotLocationDirectory = null;
    private String dataServicesGpsPlotUrlBase = null;

    /**
     * This is a Log4JLogger that is used to log information to
     */
    static Logger logger = Logger.getLogger(GeospatialGraphingAccessEJB.class);
}