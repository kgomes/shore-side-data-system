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
package moos.ssds.clients.updateBot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.activation.DataHandler;
import javax.ejb.CreateException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import javax.naming.NamingException;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.data.converters.NetcdfConverter;
import moos.ssds.data.parsers.ParsingException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataContainerGroup;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.DataProducerGroup;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.Keyword;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.RecordDescription;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.ResourceType;
import moos.ssds.metadata.Software;
import moos.ssds.metadata.StandardVariable;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.services.metadata.DataContainerAccess;
import moos.ssds.services.metadata.DataContainerAccessHome;
import moos.ssds.services.metadata.DataContainerAccessUtil;
import moos.ssds.services.metadata.DataProducerAccess;
import moos.ssds.services.metadata.DataProducerAccessHome;
import moos.ssds.services.metadata.DataProducerAccessUtil;
import moos.ssds.services.metadata.DeviceAccess;
import moos.ssds.services.metadata.DeviceAccessHome;
import moos.ssds.services.metadata.DeviceAccessUtil;
import moos.ssds.services.metadata.PersonAccess;
import moos.ssds.services.metadata.PersonAccessHome;
import moos.ssds.services.metadata.PersonAccessUtil;
import moos.ssds.util.XmlDateFormat;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import dods.dap.Attribute;
import dods.dap.AttributeTable;
import dods.dap.BaseType;
import dods.dap.BooleanPrimitiveVector;
import dods.dap.BytePrimitiveVector;
import dods.dap.DAS;
import dods.dap.DArray;
import dods.dap.DBoolean;
import dods.dap.DByte;
import dods.dap.DConnect;
import dods.dap.DConstructor;
import dods.dap.DDS;
import dods.dap.DDSException;
import dods.dap.DFloat32;
import dods.dap.DFloat64;
import dods.dap.DGrid;
import dods.dap.DInt16;
import dods.dap.DInt32;
import dods.dap.DODSException;
import dods.dap.DString;
import dods.dap.DVector;
import dods.dap.Float32PrimitiveVector;
import dods.dap.Float64PrimitiveVector;
import dods.dap.Int16PrimitiveVector;
import dods.dap.Int32PrimitiveVector;
import dods.dap.NoSuchVariableException;
import dods.dap.PrimitiveVector;
import dods.dap.parser.ParseException;

/**
 * <p>
 * This class crawls the SSDS database, looks for top level DataProducers (i.e.
 * parentless) and then crawls those to create NetCDF files and update metadata
 * that can be updated.
 * </p>
 * @author : $Author: kgomes $
 * @version : $Revision: 1.20.2.10 $
 */
public class UpdateBot {

    /**
     * The default constructor that reads in the properties file for this
     * application
     */
    public UpdateBot() {
        // Read in the properties
        try {
            updateBotProperties.load(this.getClass().getResourceAsStream(
                "/moos/ssds/clients/updateBot/updateBot.properties"));
        } catch (Exception e) {
            logger.error("Exception trying to read in properties file: "
                + e.getMessage());
        }
        // Now assign the correct properties to the local parameters
        this.netCDFBaseWorkingDirectory = updateBotProperties
            .getProperty("client.updateBot.netcdf.base.working.directory");
        this.netCDFBaseDirectory = updateBotProperties
            .getProperty("client.updateBot.netcdf.base.directory");
        this.netCDFBaseUrlString = updateBotProperties
            .getProperty("client.updateBot.netcdf.urlbase");
        this.dodsBaseUrlString = updateBotProperties
            .getProperty("client.updateBot.dods.urlbase");
        // Database properties
        this.databaseDriverClassName = updateBotProperties
            .getProperty("client.updateBot.database.jdbc.class.name");
        this.databaseJDBCUrl = updateBotProperties
            .getProperty("client.updateBot.database.jdbc.url");
        this.username = updateBotProperties
            .getProperty("client.updateBot.database.username");
        this.password = updateBotProperties
            .getProperty("client.updateBot.database.password");
        this.mailHost = updateBotProperties
            .getProperty("client.updateBot.mail.host");
        this.sendUserEmailString = updateBotProperties
            .getProperty("client.updateBot.send.user.email");
        if ((this.sendUserEmailString != null)
            && (this.sendUserEmailString.equalsIgnoreCase("true"))) {
            this.sendUserEmail = true;
        }
        this.sendAdminEmailString = updateBotProperties
            .getProperty("client.updateBot.send.admin.email");
        if ((this.sendAdminEmailString != null)
            && (this.sendAdminEmailString.equalsIgnoreCase("true"))) {
            this.sendAdminEmail = true;
        }
        this.adminEmailAddress = updateBotProperties
            .getProperty("client.updateBot.admin.email.address");

        logger.debug("The following properties will be used:");
        logger.debug("client.updateBot.netcdf.base.directory="
            + this.netCDFBaseDirectory);
        logger.debug("client.updateBot.netcdf.urlbase="
            + this.netCDFBaseUrlString);
        logger.debug("client.updateBot.dods.urlbase=" + this.dodsBaseUrlString);
        logger.debug("client.updateBot.database.jdbc.class.name="
            + this.databaseDriverClassName);
        logger.debug("client.updateBot.database.jdbc.url="
            + this.databaseJDBCUrl);
        logger.debug("client.updateBot.database.username=" + this.username);
        logger.debug("client.updateBot.database.password=" + this.password);
        logger.debug("client.updateBot.send.user.email=" + this.sendUserEmail);
        logger
            .debug("client.updateBot.send.admin.email=" + this.sendAdminEmail);
        logger.debug("client.updateBot.admin.email.address="
            + this.adminEmailAddress);

        // Load the DB driver
        try {
            Class.forName(this.databaseDriverClassName);
        } catch (ClassNotFoundException e) {
            logger.error("Could not find database driver class");
        }

        // Connect up to SSDS
        try {
            DataProducerAccessHome dpah = DataProducerAccessUtil.getHome();
            dpa = dpah.create();
            DataContainerAccessHome dcah = DataContainerAccessUtil.getHome();
            dca = dcah.create();
            DeviceAccessHome dah = DeviceAccessUtil.getHome();
            da = dah.create();
            PersonAccessHome pah = PersonAccessUtil.getHome();
            pa = pah.create();
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught trying to connect up to SSDS: "
                    + e.getMessage());
        } catch (NamingException e) {
            logger
                .error("NamingException caught trying to connect up to SSDS: "
                    + e.getMessage());
        } catch (CreateException e) {
            logger
                .error("CreateException caught trying to connect up to SSDS: "
                    + e.getMessage());
        }

    }

    /**
     * This constructor sets the name of the data producer to be updated
     * 
     * @param dataProducerName
     */
    public UpdateBot(String dataProducerName) {
        this();
        this.specifiedDataProducerName = dataProducerName;
    }

    public UpdateBot(String dataProducerName, boolean sendUserEmail,
        boolean sendAdminEmail) {
        this();
        this.specifiedDataProducerName = dataProducerName;
        this.sendUserEmail = sendAdminEmail;
        this.sendAdminEmail = sendAdminEmail;
    }

    /**
     * This method loads in all parentless deployments and then crawls them to
     * build the products and metadata necessary
     */
    public void crawlAllParentlessDeployments() {

        // First make sure log directory is created
        File deploymentProcessingLogDirectory = new File(
            this.netCDFBaseDirectory + File.separator + "update_bot_logs");
        if (!deploymentProcessingLogDirectory.exists()) {
            deploymentProcessingLogDirectory.mkdir();
        }

        logger.debug("Going to try to find all the parentless deployments");
        try {
            this.parentlessDeployments = dpa.findParentlessDeployments("id",
                "asc", false);
        } catch (RemoteException e) {
            logger.error("RemoteException caught trying to read "
                + "in all the parentless deployments: " + e.getMessage());
        } catch (MetadataAccessException e) {
            logger.error("MetadataAccessException caught trying to read "
                + "in all the parentless deployments: " + e.getMessage());
        } catch (Throwable e) {
            logger.error("Throwable caught trying to read "
                + "in all the parentless deployments: " + e.getMessage());
        }

        // Now iterate over each one and update it
        if ((this.parentlessDeployments != null)
            && (this.parentlessDeployments.size() > 0)) {

            logger.debug("There are " + this.parentlessDeployments.size()
                + " dataProducers to iterate over");
            Iterator parentlessDeploymentIter = this.parentlessDeployments
                .iterator();

            while (parentlessDeploymentIter.hasNext()) {

                // Initialize the buffers to track log and email information
                this.deploymentProcessingLogBuffer = new StringBuffer();

                // Create the start and end dates of the processing
                Date parentlessProcessStartDate = new Date();
                Date parentlessProcessEndDate = null;

                // The string buffer that can be passed to build the report
                boolean reportNeeded = false;

                DataProducer parentlessDeployment = (DataProducer) parentlessDeploymentIter
                    .next();
                logger.debug("Now working with "
                    + parentlessDeployment.toStringRepresentation("|"));

                this.deploymentProcessingLogBuffer
                    .append("SSDS Processing Report for data sets from Deployment "
                        + parentlessDeployment.getName());
                // if (parentlessDeployment.getDevice() != null)
                // this.deploymentProcessingLogBuffer.append(" of platform "
                // + parentlessDeployment.getDevice().getName());
                this.deploymentProcessingLogBuffer.append("\n\n");
                this.deploymentProcessingLogBuffer
                    .append("Processing began at "
                        + xmlDateFormat.format(new Date()) + "\n\n");

                if ((this.specifiedDataProducerName == null)
                    || (parentlessDeployment.getName()
                        .equalsIgnoreCase(this.specifiedDataProducerName))) {

                    // Reset the boolean that states there are not open streams
                    containsOpenStreams = false;

                    // Call the method to build any parallel netCDF outputs
                    reportNeeded = this.buildNetCDFOutputs(
                        parentlessDeployment, 1);

                    // The end of processing date
                    parentlessProcessEndDate = new Date();
                    this.deploymentProcessingLogBuffer
                        .append("Processing for deployment "
                            + parentlessDeployment.getName() + " ended at "
                            + xmlDateFormat.format(parentlessProcessEndDate)
                            + "\n\n");

                    // Now send out any notifications for the parentless data
                    // producer
                    if (reportNeeded) {
                        // Write out the log file
                        File deploymentProcessingLogFile = new File(
                            this.netCDFBaseDirectory + File.separator
                                + "update_bot_logs" + File.separator
                                + parentlessDeployment.getId()
                                + "_processing.log");

                        // If the file exists we want to blow it away and
                        // recreate it
                        if (deploymentProcessingLogFile.exists()) {
                            deploymentProcessingLogFile.delete();
                        }

                        try {
                            FileWriter processingLogWriter = new FileWriter(
                                deploymentProcessingLogFile);
                            if (processingLogWriter != null) {
                                processingLogWriter
                                    .write(this.deploymentProcessingLogBuffer
                                        .toString());
                                processingLogWriter.flush();
                                processingLogWriter.close();
                            }
                        } catch (IOException e) {
                            logger
                                .error("IOException caught trying to write out NetCDF log: "
                                    + e.getMessage());
                        }

                        // Create a resource for the deployment log file and
                        // attach to
                        // the parentless deployment
                        Resource processingLogResource = new Resource();
                        try {
                            processingLogResource
                                .setName("SSDS UpdateBot Processing Log");
                        } catch (MetadataException e1) {}
                        processingLogResource
                            .setStartDate(parentlessProcessStartDate);
                        processingLogResource
                            .setEndDate(parentlessProcessEndDate);
                        try {
                            processingLogResource
                                .setUriString(this.netCDFBaseUrlString
                                    + "/update_bot_logs/"
                                    + parentlessDeployment.getId()
                                    + "_processing.log");
                        } catch (MetadataException e) {}

                        // Now update it
                        try {
                            dpa.addResource(parentlessDeployment,
                                processingLogResource);
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (MetadataAccessException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        // Send any emails
                        if (this.sendAdminEmail) {
                            this.sendOutReports(parentlessDeployment,
                                this.adminEmailAddress);
                        }
                        if (this.sendUserEmail) {
                            String userEmail = null;
                            try {
                                userEmail = this
                                    .findUserEmail(parentlessDeployment);
                            } catch (Throwable e) {
                                logger
                                    .error("Throwable caught trying to get user's email address: "
                                        + e.getMessage());
                            }
                            if (userEmail != null) {
                                this.sendOutReports(parentlessDeployment,
                                    userEmail);
                            } else {
                                String errorMessage = "An email was supposed to be sent to the user, "
                                    + "but no user email could be found to send to!";
                                logger.error(errorMessage);
                                this.sendEmail("No user email found",
                                    errorMessage, adminEmailAddress);
                            }
                        }

                    }
                }
            }
        } else {
            logger.error("No parentless deployments were found");
        }

    }

    /**
     * This method builds NetCDF files that match the data in any direct outputs
     * that have their noNetCDF attribute set to false
     * 
     * @param deployment
     * @param reportBuffer
     */
    private boolean buildNetCDFOutputs(DataProducer deployment, int depth) {

        logger.debug("buildNetCDFOutputs called for deployment "
            + deployment.toStringRepresentation("|"));

        // A boolean to track whether or not the incoming deployment needs
        // updating
        boolean deploymentUpdated = false;

        this.deploymentProcessingLogBuffer.append(this.getReportIndent(depth)
            + "Going to try to convert " + "any outputs of deployment "
            + deployment.getName() + " to NetCDF files:\n");

        // Use these to track the earliest start and latest end date for the
        // outputs so that those can be used to date the data producers
        Date earliestStartDate = null;
        Date latestEndDate = null;

        // Grab the outputs of the deployments
        Collection outputs = deployment.getOutputs();
        if ((outputs != null) && (outputs.size() > 0)) {
            logger.debug("There are " + outputs.size() + " outputs to check");

            // Iterate over them
            Iterator outputIter = outputs.iterator();
            while (outputIter.hasNext()) {
                // Grab the output proxy
                DataContainer output = (DataContainer) outputIter.next();

                // Grab the output
                DataContainer outputFull = null;

                // Make sure we have the full graph
                try {
                    outputFull = (DataContainer) dca
                        .findEquivalentPersistentObject(output, true);
                    this.deploymentProcessingLogBuffer.append(this
                        .getReportIndent(depth)
                        + " - Working on output "
                        + outputFull.getName()
                        + " ("
                        + outputFull.getUriString() + ")\n");
                } catch (RemoteException e2) {
                    logger.error("RemoteException caught trying to read "
                        + "in the full graph for output "
                        + outputFull.toStringRepresentation("|") + ": "
                        + e2.getMessage());
                } catch (MetadataAccessException e2) {
                    logger
                        .error("MetadataAccessException caught trying to read "
                            + "in the full graph for output "
                            + outputFull.toStringRepresentation("|") + ": "
                            + e2.getMessage());
                }

                // Check to see if there is an open stream
                if (outputFull.getDataContainerType().equalsIgnoreCase(
                    DataContainer.TYPE_STREAM)
                    && (outputFull.getEndDate() == null))
                    containsOpenStreams = true;

                // Check the noNetCDF attribute is clear and the
                // recordDescription shows the output to be parseable
                if ((outputFull.isNoNetCDF() != null)
                    && (!outputFull.isNoNetCDF().booleanValue())
                    && (outputFull.getRecordDescription() != null)
                    && (outputFull.getRecordDescription().isParseable() != null)
                    && (outputFull.getRecordDescription().isParseable()
                        .booleanValue())) {

                    logger.debug("OK, I have go for liftoff on converting "
                        + outputFull.toStringRepresentation("|")
                        + " to a NetCDF file");
                    this.deploymentProcessingLogBuffer.append(this
                        .getReportIndent(depth)
                        + "   - The RecordDescription and NoNetCDF "
                        + "flag show the output can be converted.\n");

                    // Grab the path to the parallel NetCDF file
                    String paralleNetCDFWorkingFilePath = this
                        .getParallelNetCDFFilePath(outputFull,
                            PATH_TYPE_WORKING_FILE);
                    String parallelNetCDFFilePath = this
                        .getParallelNetCDFFilePath(outputFull, PATH_TYPE_FILE);

                    if (this.doesDataContainerNeedNetCDF(outputFull,
                        parallelNetCDFFilePath)) {

                        // First set flag that the deployment will need to be
                        // updated
                        deploymentUpdated = true;

                        // OK, so we need a new NetCDF file. In order to use the
                        // NetCDF converter, I must pass in the file.
                        File workingNetCDFFile = new File(
                            paralleNetCDFWorkingFilePath);
                        File netCDFFile = new File(parallelNetCDFFilePath);

                        logger.debug("NetCDF File: " + netCDFFile.toString());
                        this.deploymentProcessingLogBuffer.append(this
                            .getReportIndent(depth)
                            + "   - The NetCDF was flagged that it needed "
                            + "creating so it will be created at "
                            + this.getParallelNetCDFFilePath(outputFull,
                                PATH_TYPE_URL) + "\n");

                        // If the file exists we want to blow it away and
                        // recreate it
                        if (workingNetCDFFile.exists()) {
                            workingNetCDFFile.delete();
                        }
                        // Make sure the final file has its parent directory
                        // structure instantiated
                        File parentFile = netCDFFile.getParentFile();
                        if (!parentFile.exists())
                            parentFile.mkdirs();

                        // If the old file exists delete it
                        if (netCDFFile.exists()) {
                            this.deploymentProcessingLogBuffer.append(this
                                .getReportIndent(depth)
                                + "   - The NetCDF file already exists, "
                                + "so will delete it.\n");
                            netCDFFile.delete();
                        }
                        // Create the new final file
                        try {
                            netCDFFile.createNewFile();
                        } catch (IOException e2) {
                            logger.error("Could not create new file "
                                + netCDFFile.getAbsolutePath() + ": "
                                + e2.getMessage());
                        }

                        // Now build the NetCDFConverter
                        NetcdfConverter netcdfConverter = null;
                        try {
                            netcdfConverter = new NetcdfConverter(outputFull,
                                workingNetCDFFile);
                        } catch (ParsingException e) {
                            logger
                                .error("ParsingException caught creating the NetcdfConverter: "
                                    + e.getMessage());
                        } catch (IOException e) {
                            logger
                                .error("IOException caught creating the NetcdfConverter: "
                                    + e.getMessage());
                        }
                        // Now create the NetCDF file
                        Date netCDFCreationStartDate = new Date();
                        this.deploymentProcessingLogBuffer.append(this
                            .getReportIndent(depth)
                            + "   - Starting the NetCDF creation at "
                            + xmlDateFormat.format(netCDFCreationStartDate)
                            + "\n");
                        try {
                            if (netcdfConverter != null)
                                netcdfConverter.create();

                        } catch (IOException e) {
                            logger
                                .error("IOException caught creating the NetCDF file: "
                                    + e.getMessage());
                            e.printStackTrace();
                        }
                        Date netCDFCreationEndDate = new Date();
                        this.deploymentProcessingLogBuffer.append(this
                            .getReportIndent(depth)
                            + "   - NetCDF creation finished at "
                            + xmlDateFormat.format(netCDFCreationEndDate)
                            + "\n");

                        // Now I have created the working file, copy that over
                        // to the final resting place.
                        logger.debug("Going to name "
                            + workingNetCDFFile.getAbsolutePath() + " to "
                            + netCDFFile.getAbsolutePath());
                        try {
                            // Create channel on the source
                            FileChannel srcChannel = new FileInputStream(
                                workingNetCDFFile.getAbsoluteFile())
                                .getChannel();

                            // Create channel on the destination
                            FileChannel dstChannel = new FileOutputStream(
                                netCDFFile.getAbsoluteFile()).getChannel();

                            // Copy file contents from source to destination
                            dstChannel.transferFrom(srcChannel, 0, srcChannel
                                .size());

                            // Close the channels
                            srcChannel.close();
                            dstChannel.close();
                        } catch (IOException e) {
                            logger
                                .error("IOException caught trying to copy file (Source: "
                                    + workingNetCDFFile.getAbsolutePath()
                                    + ", Dest: "
                                    + netCDFFile.getAbsolutePath()
                                    + ") : " + e.getMessage());
                        }
                        if (!netCDFFile.exists()) {
                            logger
                                .error("After the rename, the file does not exist ... YIKES! (Source: "
                                    + workingNetCDFFile.getAbsolutePath()
                                    + ", Dest: "
                                    + netCDFFile.getAbsolutePath()
                                    + ")");
                        }

                        // Now grab the path to the associated log file and
                        // write out the log
                        String parallelNetCDFLogPath = this
                            .getParallelNetCDFFilePath(outputFull,
                                PATH_TYPE_LOG);

                        this.deploymentProcessingLogBuffer
                            .append(this.getReportIndent(depth)
                                + "   - Now will write out the NetCDF creation log to "
                                + this.getParallelNetCDFFilePath(outputFull,
                                    PATH_TYPE_LOG_URL) + "\n");
                        File netCDFLogFile = new File(parallelNetCDFLogPath);

                        // If the file exists we want to blow it away and
                        // recreate it
                        if (netCDFLogFile.exists()) {
                            netCDFLogFile.delete();
                        }

                        try {
                            FileWriter logWriter = new FileWriter(netCDFLogFile);
                            if (logWriter != null) {
                                logWriter.write(netcdfConverter.getLogText());
                                logWriter.flush();
                                logWriter.close();
                            }
                        } catch (IOException e) {
                            logger
                                .error("IOException caught trying to write out NetCDF log: "
                                    + e.getMessage());
                        }

                        // In order to store all the metadata for the process
                        // and data file, create the object that will be used to
                        // track that information and associate them
                        this.deploymentProcessingLogBuffer.append(this
                            .getReportIndent(depth)
                            + "   - Now will create the SSDS Metadata objects "
                            + "to track the processing and NetCDF file.\n");

                        // The NetCDF DataProducer
                        DataProducer netCDFDataProducer = this
                            .createNetCDFDataProducer(outputFull,
                                netCDFCreationStartDate, netCDFCreationEndDate);
                        this.deploymentProcessingLogBuffer.append(this
                            .getReportIndent(depth)
                            + "   - The UpdateBot DataProducer will be named "
                            + netCDFDataProducer.getName()
                            + "(start date = "
                            + xmlDateFormat.format(netCDFDataProducer
                                .getStartDate())
                            + ", end date = "
                            + xmlDateFormat.format(netCDFDataProducer
                                .getEndDate()) + ").\n");

                        // If the NetCDF Creator has mean geospatial
                        // information, set those on the DataProducer
                        try {
                            if (netcdfConverter.getMeanLatitude() != null) {
                                double meanLat = netcdfConverter
                                    .getMeanLatitude().doubleValue();
                                double meanLatAbs = Math.abs(meanLat);
                                if (meanLatAbs >= 0 && meanLatAbs <= Math.PI) {
                                    meanLat = (180 * meanLat) / Math.PI;
                                }
                                netCDFDataProducer
                                    .setNominalLatitude(new Double(meanLat));
                                // Check to see if the incoming deployment has a
                                // nominal latitude
                                if (deployment.getNominalLatitude() == null)
                                    deployment.setNominalLatitude(new Double(
                                        meanLat));
                                // If there is a parent of the incoming
                                // deployment, check that too
                                if ((deployment.getParentDataProducer() != null)
                                    && (Hibernate.isInitialized(deployment
                                        .getParentDataProducer()))) {
                                    if (deployment.getParentDataProducer()
                                        .getNominalLatitude() == null)
                                        deployment.getParentDataProducer()
                                            .setNominalLatitude(
                                                new Double(meanLat));
                                }
                            }
                            if (netcdfConverter.getMeanLongitude() != null) {
                                double meanLon = netcdfConverter
                                    .getMeanLongitude().doubleValue();
                                double meanLonAbs = Math.abs(meanLon);
                                if (meanLonAbs >= 0 && meanLonAbs <= Math.PI) {
                                    meanLon = (180 * meanLon) / Math.PI;
                                }
                                netCDFDataProducer
                                    .setNominalLongitude(new Double(meanLon));
                                // Check to see if the incoming deployment has a
                                // nominal longitude
                                if (deployment.getNominalLongitude() == null)
                                    deployment.setNominalLongitude(new Double(
                                        meanLon));
                                // If there is a parent of the incoming
                                // deployment, check that too
                                if ((deployment.getParentDataProducer() != null)
                                    && (Hibernate.isInitialized(deployment
                                        .getParentDataProducer()))) {
                                    if (deployment.getParentDataProducer()
                                        .getNominalLongitude() == null)
                                        deployment.getParentDataProducer()
                                            .setNominalLongitude(
                                                new Double(meanLon));
                                }
                            }
                            if (netcdfConverter.getMeanDepth() != null) {
                                netCDFDataProducer
                                    .setNominalDepth(new Float(netcdfConverter
                                        .getMeanDepth().floatValue()));
                                // Check to see if the incoming deployment has a
                                // nominal longitude
                                if (deployment.getNominalDepth() == null)
                                    deployment.setNominalDepth(new Float(
                                        netcdfConverter.getMeanDepth()
                                            .floatValue()));
                                // If there is a parent of the incoming
                                // deployment, check that too
                                if ((deployment.getParentDataProducer() != null)
                                    && (Hibernate.isInitialized(deployment
                                        .getParentDataProducer()))) {
                                    if (deployment.getParentDataProducer()
                                        .getNominalDepth() == null)
                                        deployment.getParentDataProducer()
                                            .setNominalDepth(
                                                new Float(netcdfConverter
                                                    .getMeanDepth()
                                                    .floatValue()));
                                }
                            }
                        } catch (MetadataException e1) {
                            logger
                                .error("MetadataException caught trying to set "
                                    + "nominal geospatial extents on DataProducer: "
                                    + e1.getMessage());
                        }

                        // The NetCDF DataProducerGroup
                        DataProducerGroup netCDFCreationDataProducerGroup = this
                            .createNetCDFDataProducerGroup();
                        this.deploymentProcessingLogBuffer.append(this
                            .getReportIndent(depth)
                            + "   - Created DataProducerGroup with name "
                            + netCDFCreationDataProducerGroup.getName() + "\n");

                        // The NetCDF DataContainer
                        DataContainer parallelNetCDFDataContainer = this
                            .createNetCDFDataContainer(netcdfConverter, this
                                .getParallelNetCDFFilePath(outputFull,
                                    PATH_TYPE_URL), this
                                .getParallelNetCDFFilePath(outputFull,
                                    PATH_TYPE_DODS));

                        // The NetCDF DataContainerGroup
                        DataContainerGroup netCDFDataContainerGroup = this
                            .createNetCDFDataContainerGroup();
                        this.deploymentProcessingLogBuffer.append(this
                            .getReportIndent(depth)
                            + "   - Created DataContainerGroup with name "
                            + netCDFDataContainerGroup.getName() + "\n");

                        // The Resource for the log file
                        Resource logFileResource = this
                            .createNetCDFLogFileResource(this
                                .getParallelNetCDFFilePath(outputFull,
                                    PATH_TYPE_LOG_URL),
                                netCDFCreationStartDate, netCDFCreationEndDate);

                        this.deploymentProcessingLogBuffer.append(this
                            .getReportIndent(depth)
                            + "   - The NetCDF DataContainer will be "
                            + parallelNetCDFDataContainer.getName()
                            + " at "
                            + parallelNetCDFDataContainer.getUriString()
                            + ".\n");

                        this.deploymentProcessingLogBuffer.append(this
                            .getReportIndent(depth)
                            + "   - NetCDF creation log file Resource will be "
                            + logFileResource.getName()
                            + " at "
                            + logFileResource.getUriString() + ".\n");

                        // Now define the Relationships
                        netCDFDataProducer.addInput(outputFull);
                        netCDFDataProducer
                            .addOutput(parallelNetCDFDataContainer);
                        netCDFDataProducer
                            .addDataProducerGroup(netCDFCreationDataProducerGroup);
                        parallelNetCDFDataContainer
                            .addDataContainerGroup(netCDFDataContainerGroup);
                        parallelNetCDFDataContainer
                            .addResource(logFileResource);

                        // If the NetCDF file has start and end dates, use those
                        // to assign start and end dates to the output they were
                        // created from
                        if (parallelNetCDFDataContainer.getStartDate() != null) {
                            this.deploymentProcessingLogBuffer
                                .append(this.getReportIndent(depth)
                                    + "   - Since a start date was found in the NetCDF "
                                    + "file, will assign "
                                    + xmlDateFormat
                                        .format(parallelNetCDFDataContainer
                                            .getStartDate())
                                    + " to the start date of "
                                    + outputFull.getName() + ".\n");
                            output.setStartDate(parallelNetCDFDataContainer
                                .getStartDate());
                            outputFull.setStartDate(parallelNetCDFDataContainer
                                .getStartDate());

                            // Also compare to the earliest start date to see
                            // which is earlier
                            if (earliestStartDate == null) {
                                earliestStartDate = parallelNetCDFDataContainer
                                    .getStartDate();
                            } else {
                                if (parallelNetCDFDataContainer.getStartDate()
                                    .before(earliestStartDate)) {
                                    earliestStartDate = parallelNetCDFDataContainer
                                        .getStartDate();
                                }
                            }
                        }
                        if (parallelNetCDFDataContainer.getEndDate() != null) {
                            output.setEndDate(parallelNetCDFDataContainer
                                .getEndDate());
                            outputFull.setEndDate(parallelNetCDFDataContainer
                                .getEndDate());
                            this.deploymentProcessingLogBuffer
                                .append(this.getReportIndent(depth)
                                    + "   - Since a end date was found in the NetCDF "
                                    + "file, will assign "
                                    + xmlDateFormat
                                        .format(parallelNetCDFDataContainer
                                            .getEndDate())
                                    + " to the end date of "
                                    + outputFull.getName() + ".\n");
                            // Also compare to the latest end date to see which
                            // is later
                            if (latestEndDate == null) {
                                latestEndDate = parallelNetCDFDataContainer
                                    .getEndDate();
                            } else {
                                if (parallelNetCDFDataContainer.getEndDate()
                                    .after(latestEndDate)) {
                                    latestEndDate = parallelNetCDFDataContainer
                                        .getEndDate();
                                }
                            }
                        }

                        // Same for extents
                        if (parallelNetCDFDataContainer.getMinLatitude() != null) {
                            try {
                                output
                                    .setMinLatitude(parallelNetCDFDataContainer
                                        .getMinLatitude());
                                outputFull
                                    .setMinLatitude(parallelNetCDFDataContainer
                                        .getMinLatitude());
                                this.deploymentProcessingLogBuffer
                                    .append(this.getReportIndent(depth)
                                        + "   - Since a min latitude was found in the NetCDF "
                                        + "file, will assign "
                                        + parallelNetCDFDataContainer
                                            .getMinLatitude()
                                        + " to the min latitude of "
                                        + outputFull.getName() + ".\n");
                            } catch (MetadataException e) {}
                        }
                        if (parallelNetCDFDataContainer.getMaxLatitude() != null) {
                            try {
                                output
                                    .setMaxLatitude(parallelNetCDFDataContainer
                                        .getMaxLatitude());
                                outputFull
                                    .setMaxLatitude(parallelNetCDFDataContainer
                                        .getMaxLatitude());
                                this.deploymentProcessingLogBuffer
                                    .append(this.getReportIndent(depth)
                                        + "   - Since a max latitude was found in the NetCDF "
                                        + "file, will assign "
                                        + parallelNetCDFDataContainer
                                            .getMaxLatitude()
                                        + " to the max latitude of "
                                        + outputFull.getName() + ".\n");
                            } catch (MetadataException e) {}
                        }
                        if (parallelNetCDFDataContainer.getMinLongitude() != null) {
                            try {
                                output
                                    .setMinLongitude(parallelNetCDFDataContainer
                                        .getMinLongitude());
                                outputFull
                                    .setMinLongitude(parallelNetCDFDataContainer
                                        .getMinLongitude());
                                this.deploymentProcessingLogBuffer
                                    .append(this.getReportIndent(depth)
                                        + "   - Since a min longitude was found in the NetCDF "
                                        + "file, will assign "
                                        + parallelNetCDFDataContainer
                                            .getMinLongitude()
                                        + " to the min longitude of "
                                        + outputFull.getName() + ".\n");
                            } catch (MetadataException e) {}
                        }
                        if (parallelNetCDFDataContainer.getMaxLongitude() != null) {
                            try {
                                output
                                    .setMaxLongitude(parallelNetCDFDataContainer
                                        .getMaxLongitude());
                                outputFull
                                    .setMaxLongitude(parallelNetCDFDataContainer
                                        .getMaxLongitude());
                                this.deploymentProcessingLogBuffer
                                    .append(this.getReportIndent(depth)
                                        + "   - Since a max longitude was found in the NetCDF "
                                        + "file, will assign "
                                        + parallelNetCDFDataContainer
                                            .getMaxLongitude()
                                        + " to the max longitude of "
                                        + outputFull.getName() + ".\n");
                            } catch (MetadataException e) {}
                        }
                        if (parallelNetCDFDataContainer.getMinDepth() != null) {
                            try {
                                output.setMinDepth(parallelNetCDFDataContainer
                                    .getMinDepth());
                                outputFull
                                    .setMinDepth(parallelNetCDFDataContainer
                                        .getMinDepth());
                                this.deploymentProcessingLogBuffer
                                    .append(this.getReportIndent(depth)
                                        + "   - Since a min depth was found in the NetCDF "
                                        + "file, will assign "
                                        + parallelNetCDFDataContainer
                                            .getMinDepth()
                                        + " to the min depth of "
                                        + outputFull.getName() + ".\n");
                            } catch (MetadataException e) {}
                        }
                        if (parallelNetCDFDataContainer.getMaxDepth() != null) {
                            try {
                                output.setMaxDepth(parallelNetCDFDataContainer
                                    .getMaxDepth());
                                outputFull
                                    .setMaxDepth(parallelNetCDFDataContainer
                                        .getMaxDepth());
                                this.deploymentProcessingLogBuffer
                                    .append(this.getReportIndent(depth)
                                        + "   - Since a max depth was found in the NetCDF "
                                        + "file, will assign "
                                        + parallelNetCDFDataContainer
                                            .getMaxDepth()
                                        + " to the max depth of "
                                        + outputFull.getName() + ".\n");
                            } catch (MetadataException e) {}
                        }

                        // Check for content length on output
                        Long outputLength = this.getContentLengthFromUrl(output
                            .getUriString());
                        if ((outputLength != null)
                            && (outputLength.longValue() > 0)) {
                            output.setContentLength(outputLength);
                            outputFull.setContentLength(outputLength);
                            this.deploymentProcessingLogBuffer.append(this
                                .getReportIndent(depth)
                                + "   - Set the content length of "
                                + outputFull.getName()
                                + " to "
                                + outputLength
                                + ".\n");
                        }

                        // And now persist the data producer to the database
                        Long dataProducerID = null;
                        try {
                            dataProducerID = dpa
                                .makePersistent(netCDFDataProducer);
                        } catch (RemoteException e) {
                            logger
                                .error("RemoteException caught trying to get "
                                    + "dataProducer access interfaces and persist: "
                                    + e.getMessage());
                        } catch (MetadataAccessException e) {
                            logger
                                .error("MetadataAccessException caught trying to get"
                                    + "dataProducer access interfaces and persist: "
                                    + e.getMessage());
                        }
                        this.deploymentProcessingLogBuffer.append(this
                            .getReportIndent(depth)
                            + " - UpdateBot DataProducer "
                            + "persisted and has ID " + dataProducerID + ".\n");
                    } else {
                        this.deploymentProcessingLogBuffer.append(this
                            .getReportIndent(depth)
                            + "   - The NetCDF already exists and is up to "
                            + "date so no NetCDF will be created.\n\n");
                    }
                } else {
                    logger.debug("It looks like the output "
                        + outputFull.toStringRepresentation("|")
                        + " could not be converted to NetCDF");
                    this.deploymentProcessingLogBuffer
                        .append(this.getReportIndent(depth)
                            + "   - This output could not be converted to NetCDF because ");
                    if ((outputFull.isNoNetCDF() != null)
                        && (outputFull.isNoNetCDF().booleanValue())) {
                        this.deploymentProcessingLogBuffer
                            .append("it was flagged to NOT have NetCDF created.");
                    } else if (outputFull.getRecordDescription() == null) {
                        this.deploymentProcessingLogBuffer
                            .append("the RecordDescription was null.");
                    } else if ((outputFull.getRecordDescription().isParseable() == null)
                        || (!outputFull.getRecordDescription().isParseable()
                            .booleanValue())) {
                        this.deploymentProcessingLogBuffer
                            .append("the RecordDescription stated the output was not parseable.");
                    }
                    this.deploymentProcessingLogBuffer.append("\n");
                }
            }

        } else {
            logger.debug("No outputs found");
            this.deploymentProcessingLogBuffer.append(this
                .getReportIndent(depth)
                + "  - There were no direct outputs for that deployment.\n");
        }

        // Now recursively call this method on child deployments
        Collection childDeployments = null;
        try {
            childDeployments = dpa.findChildDataProducers(deployment, false);
        } catch (RemoteException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (MetadataAccessException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        if ((childDeployments != null) && (childDeployments.size() > 0)) {

            this.deploymentProcessingLogBuffer.append(this
                .getReportIndent(depth)
                + "  - Will loop through child deployments now (there are "
                + childDeployments.size() + " of them)\n");
            // Child deployment time spans
            Date earliestChildDeploymentStartDate = null;
            Date latestChildDeploymentEndDate = null;

            Iterator childDeploymentIter = childDeployments.iterator();
            while (childDeploymentIter.hasNext()) {
                DataProducer childDataProducer = (DataProducer) childDeploymentIter
                    .next();

                // try {
                // childDataProducer = (DataProducer) dpa
                // .findEquivalentPersistentObject(childDataProducer, true);
                // } catch (RemoteException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // } catch (MetadataAccessException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // }

                boolean childUpdated = this.buildNetCDFOutputs(
                    childDataProducer, depth + 2);

                if (childUpdated && (!deploymentUpdated))
                    deploymentUpdated = true;

                // Check to make sure that start and end dates were not found in
                // the outputs before bothering to check them on the child
                // deployments
//                if ((earliestStartDate == null) || (latestEndDate == null)) {
//                    // First see if the current child deployment even has a
//                    // start date
//                    if (childDataProducer.getStartDate() != null) {
//                        // If no earlier start date found, just use this one
//                        if (earliestChildDeploymentStartDate == null) {
//                            earliestChildDeploymentStartDate = childDataProducer
//                                .getStartDate();
//                        } else {
//                            if (childDataProducer.getStartDate().before(
//                                earliestChildDeploymentStartDate)) {
//                                earliestChildDeploymentStartDate = childDataProducer
//                                    .getStartDate();
//                            }
//                        }
//                    }
//                    // Make sure we have an end date at all
//                    if (childDataProducer.getEndDate() != null) {
//                        // If no latest end date found, just use this one
//                        if (latestChildDeploymentEndDate == null) {
//                            latestChildDeploymentEndDate = childDataProducer
//                                .getEndDate();
//                        } else {
//                            if (childDataProducer.getEndDate().after(
//                                latestChildDeploymentEndDate)) {
//                                latestChildDeploymentEndDate = childDataProducer
//                                    .getEndDate();
//                            }
//                        }
//                    }
//
//                    // Check to see if I can fill in missing lat,lon, depth
//                    // extents
//                    Device childDevice = null;
//                    try {
//                        childDevice = (Device) da
//                            .findEquivalentPersistentObject(childDataProducer
//                                .getDevice(), true);
//                    } catch (RemoteException e1) {
//                        // TODO Auto-generated catch block
//                        e1.printStackTrace();
//                    } catch (MetadataAccessException e1) {
//                        // TODO Auto-generated catch block
//                        e1.printStackTrace();
//                    }
//                    try {
//                        if ((deployment.getNominalLatitude() == null)
//                            && (childDataProducer.getDevice() != null)
//                            && (childDevice.getDeviceType() != null)
//                            && (childDevice.getDeviceType().getName()
//                                .equalsIgnoreCase("gps"))
//                            && (childDataProducer.getNominalLatitude() != null)) {
//                            deployment.setNominalLatitude(childDataProducer
//                                .getNominalLatitude());
//                            deploymentUpdated = true;
//                        }
//                        if ((deployment.getNominalLongitude() == null)
//                            && (childDevice != null)
//                            && (childDevice.getDeviceType() != null)
//                            && (childDevice.getDeviceType().getName()
//                                .equalsIgnoreCase("gps"))
//                            && (childDataProducer.getNominalLongitude() != null)) {
//                            deployment.setNominalLongitude(childDataProducer
//                                .getNominalLongitude());
//                            deploymentUpdated = true;
//                        }
//                        if ((deployment.getNominalDepth() == null)
//                            && (childDataProducer.getNominalDepth() != null)) {
//                            deployment.setNominalDepth(childDataProducer
//                                .getNominalDepth());
//                            deploymentUpdated = true;
//                        }
//                    } catch (MetadataException e) {
//                        logger
//                            .error("MetadataException caught trying to set the nominal geospatials on deployment: "
//                                + e.getMessage());
//                    }
//                }
            }

            // If no earliest start date was found from the outputs, and one was
            // found in the child deployments, use that one for the
            // tempDeployment startDate
			// if ((earliestStartDate == null)
			// && (earliestChildDeploymentStartDate != null)) {
			// earliestStartDate = earliestChildDeploymentStartDate;
			// }
			// // Same for end dates
			// if ((latestEndDate == null)
			// && (latestChildDeploymentEndDate != null)) {
			// latestEndDate = latestChildDeploymentEndDate;
			//            }
        }

        // If the earliest start time is defined and/or the latestend time,
        // use those for the deployment
        if (earliestStartDate != null) {
            if ((deployment.getStartDate() == null)
                || (earliestStartDate.before(deployment.getStartDate()))) {
                deployment.setStartDate(earliestStartDate);
                deploymentUpdated = true;
            }
        }
        if ((latestEndDate != null) && (!containsOpenStreams)) {
            if ((deployment.getEndDate() == null)
                || (latestEndDate.after(deployment.getEndDate()))) {
                deployment.setEndDate(latestEndDate);
                deploymentUpdated = true;
            }
        }

        // Now update the temp deployment if it needs it
        if (deploymentUpdated) {
            Long tempDPID = null;
            try {
                tempDPID = dpa.makePersistent(deployment);
            } catch (RemoteException e) {
                logger.error("RemoteException caught trying to "
                    + "persist the updated deployment "
                    + deployment.toStringRepresentation("|") + ": "
                    + e.getMessage());
            } catch (MetadataAccessException e) {
                logger.error("MetadataAccessException caught trying to "
                    + "persist the updated deployment "
                    + deployment.toStringRepresentation("|") + ": "
                    + e.getMessage());
            }
            this.deploymentProcessingLogBuffer.append(this
                .getReportIndent(depth)
                + " - Deployment "
                + deployment.getName()
                + " was changed and persisted to the database with ID "
                + tempDPID + ".\n");
        }

        // Return the report flag
        return deploymentUpdated;
    }

    /**
     * This method takes in a DataContainer and the path to its parallel NetCDF
     * file. It then uses information in the database to see if a parallel
     * NetCDF file needs to be created (or re-created). The criteria for this
     * decision are: A. If the parallel NetCDF file does not exist OR B. The
     * modification date of the file at the end of the DataContainer's URL is
     * more recent then the one when it was last processed (read from the
     * database).
     * 
     * @param dataContainer
     * @param netCDFFilePath
     * @return
     */
    private boolean doesDataContainerNeedNetCDF(DataContainer dataContainer,
        String netCDFFilePath) {
        logger.debug("Going to check to see if dataContainer needs NetCDF");

        // The result to return
        boolean newNetCDF = false;

        // First let's grab the modification time from the DataContainer's URL
        // (if there is a URL)
        Calendar modificationTime = null;
        if (dataContainer.getUrl() != null) {
            // Connect to the URL
            URLConnection conn = null;
            try {
                conn = dataContainer.getUrl().openConnection();
            } catch (IOException e) {
                logger.error("IOException caught trying to connect to url "
                    + dataContainer.getUriString());
            }

            // If connection was established, look for modification time
            if (conn != null) {
                // List all the response headers from the server. Note: The
                // first call to getHeaderFieldKey() will implicit send the HTTP
                // request to the server.
                for (int i = 0;; i++) {
                    String headerName = conn.getHeaderFieldKey(i);
                    String headerValue = conn.getHeaderField(i);
                    if (headerName == null && headerValue == null) {
                        // No more headers
                        break;
                    }
                    if (headerName == null) {
                        // The header value contains the server's HTTP version
                    } else if (headerName.equalsIgnoreCase("Last-Modified")) {
                        // Found the date, let's parse it out
                        String[] parsedValues = headerValue.split("\\s+");
                        if (parsedValues.length >= 6) {
                            modificationTime = new GregorianCalendar();
                            modificationTime.setTimeZone(TimeZone
                                .getTimeZone(parsedValues[5]));
                            if (parsedValues[2].equalsIgnoreCase("Jan"))
                                modificationTime.set(Calendar.MONTH,
                                    Calendar.JANUARY);
                            else if (parsedValues[2].equalsIgnoreCase("Feb"))
                                modificationTime.set(Calendar.MONTH,
                                    Calendar.FEBRUARY);
                            else if (parsedValues[2].equalsIgnoreCase("Mar"))
                                modificationTime.set(Calendar.MONTH,
                                    Calendar.MARCH);
                            else if (parsedValues[2].equalsIgnoreCase("Apr"))
                                modificationTime.set(Calendar.MONTH,
                                    Calendar.APRIL);
                            else if (parsedValues[2].equalsIgnoreCase("May"))
                                modificationTime.set(Calendar.MONTH,
                                    Calendar.MAY);
                            else if (parsedValues[2].equalsIgnoreCase("Jun"))
                                modificationTime.set(Calendar.MONTH,
                                    Calendar.JUNE);
                            else if (parsedValues[2].equalsIgnoreCase("Jul"))
                                modificationTime.set(Calendar.MONTH,
                                    Calendar.JULY);
                            else if (parsedValues[2].equalsIgnoreCase("Aug"))
                                modificationTime.set(Calendar.MONTH,
                                    Calendar.AUGUST);
                            else if (parsedValues[2].equalsIgnoreCase("Sep"))
                                modificationTime.set(Calendar.MONTH,
                                    Calendar.SEPTEMBER);
                            else if (parsedValues[2].equalsIgnoreCase("Oct"))
                                modificationTime.set(Calendar.MONTH,
                                    Calendar.OCTOBER);
                            else if (parsedValues[2].equalsIgnoreCase("Nov"))
                                modificationTime.set(Calendar.MONTH,
                                    Calendar.NOVEMBER);
                            else if (parsedValues[2].equalsIgnoreCase("Dec"))
                                modificationTime.set(Calendar.MONTH,
                                    Calendar.DECEMBER);
                            modificationTime.set(Calendar.DAY_OF_MONTH, Integer
                                .parseInt(parsedValues[1]));
                            modificationTime.set(Calendar.YEAR, Integer
                                .parseInt(parsedValues[3]));
                            String[] hms = parsedValues[4].split(":");
                            modificationTime.set(Calendar.HOUR_OF_DAY, Integer
                                .parseInt(hms[0]));
                            modificationTime.set(Calendar.MINUTE, Integer
                                .parseInt(hms[1]));
                            modificationTime.set(Calendar.SECOND, Integer
                                .parseInt(hms[2]));
                            modificationTime.set(Calendar.MILLISECOND, 0);
                            logger.debug("Modification time for file at URL "
                                + dataContainer.getUriString()
                                + " is "
                                + xmlDateFormat.format(modificationTime
                                    .getTime()));
                            break;
                        }
                    }
                }
            }
        }

        // To check the first criteria, look for the file at the given path
        File parallelNetCDFFile = new File(netCDFFilePath);
        if (!parallelNetCDFFile.exists()) {
            logger.debug("The parallel NetCDF File does not exist, so "
                + "yes, it needs to be created");
            newNetCDF = true;
        }

        // If the NetCDF file already exists, I still need to check the
        // modification time against that of the DB
        Calendar modificationTimeDB = null;
        if ((!newNetCDF) && (modificationTime != null)) {

            // First grab a connection to the database
            Connection connection = null;
            try {
                connection = DriverManager.getConnection(this.databaseJDBCUrl,
                    this.username, this.password);
            } catch (SQLException e) {
                logger
                    .error("SQLException caught trying to connect to SSDS_Metadata: "
                        + e.getMessage());
            }

            // Find the last mod time
            String queryStatement = "SELECT DataContainerLastMod "
                + "FROM UpdateBotProps WHERE DataContainerID_FK = '"
                + dataContainer.getId().longValue() + "'";
            ResultSet resultSet = null;
            PreparedStatement pstmt = null;
            try {
                pstmt = connection.prepareStatement(queryStatement);
            } catch (SQLException e) {
                logger.error("SQLException caught: " + e.getMessage());
            }
            if (pstmt != null) {
                resultSet = null;
                try {
                    resultSet = pstmt.executeQuery();
                } catch (SQLException e) {
                    logger
                        .error("SQLException caught trying to read last mod time: "
                            + e.getMessage());
                }
            }
            if (resultSet != null) {
                try {
                    if (resultSet.next()) {
                        modificationTimeDB = Calendar.getInstance();
                        modificationTimeDB.setTimeZone(TimeZone
                            .getTimeZone("GMT"));
                        String modTimeString = resultSet
                            .getString("DataContainerLastMod");
                        logger.debug("Hellow world");
                        if (modTimeString != null) {
                            // First split into date and time
                            String[] dateTime = modTimeString.split("\\s+");
                            String[] date = dateTime[0].split("-");
                            String[] time = dateTime[1].split(":");
                            String[] seconds = time[2].split("\\.");
                            modificationTimeDB.set(Calendar.YEAR, Integer
                                .parseInt(date[0]));
                            modificationTimeDB.set(Calendar.MONTH, Integer
                                .parseInt(date[1]) - 1);
                            modificationTimeDB.set(Calendar.DAY_OF_MONTH,
                                Integer.parseInt(date[2]));
                            modificationTimeDB.set(Calendar.HOUR_OF_DAY,
                                Integer.parseInt(time[0]));
                            modificationTimeDB.set(Calendar.MINUTE, Integer
                                .parseInt(time[1]));
                            modificationTimeDB.set(Calendar.SECOND, Integer
                                .parseInt(seconds[0]));
                            modificationTimeDB.set(Calendar.MILLISECOND,
                                Integer.parseInt(seconds[1]));
                        }
                    }
                } catch (SQLException e) {
                    logger
                        .error("SQLException caught trying to next the result set: "
                            + e.getMessage());
                }
            }

            // Close up the connection
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Could not close the connection effectively: "
                    + e.getMessage());
            }

            // If last modification time in the DB is before the mod time on the
            // file, a new one should be created
            if ((modificationTimeDB == null)
                || (modificationTimeDB.before(modificationTime)))
                newNetCDF = true;
        }

        // If a new NetCDF is to be created, update or insert the mod time
        if (newNetCDF) {
            // First grab a connection to the database
            Connection connection = null;
            try {
                connection = DriverManager.getConnection(this.databaseJDBCUrl,
                    this.username, this.password);
            } catch (SQLException e) {
                logger
                    .error("SQLException caught trying to connect to SSDS_Metadata: "
                        + e.getMessage());
            }

            // Find the last mod time
            String queryStatement = null;
            String modTimeInString = (modificationTime.get(Calendar.MONTH) + 1)
                + "/" + modificationTime.get(Calendar.DAY_OF_MONTH) + "/"
                + modificationTime.get(Calendar.YEAR) + " "
                + modificationTime.get(Calendar.HOUR_OF_DAY) + ":"
                + modificationTime.get(Calendar.MINUTE) + ":"
                + modificationTime.get(Calendar.SECOND) + ".0";
            if (modificationTimeDB == null) {
                queryStatement = "INSERT INTO UpdateBotProps (DataContainerID_FK,DataContainerLastMod) VALUES ('"
                    + dataContainer.getId().toString()
                    + "', '"
                    + modTimeInString + "')";
            } else {
                queryStatement = "UPDATE UpdateBotProps SET DataContainerLastMod = '"
                    + modTimeInString
                    + "' WHERE DataContainerID_FK = '"
                    + dataContainer.getId() + "'";
            }
            PreparedStatement pstmt = null;
            try {
                pstmt = connection.prepareStatement(queryStatement);
            } catch (SQLException e) {
                logger.error("SQLException caught: " + e.getMessage());
            }
            if (pstmt != null) {
                try {
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    logger
                        .error("SQLException caught trying to update the last mod time: "
                            + e.getMessage());
                }
            }

            // Close up the connection
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Could not close the connection effectively: "
                    + e.getMessage());
            }
        }

        // If a new NetCDF is to be created, record the mod time in the database
        // for future runs
        return newNetCDF;
    }

    /**
     * This method returns a new DataProducer that represents the software run
     * to create the NetCDF file from the raw input file
     * 
     * @param output
     * @param startDate
     * @param endDate
     * @return
     */
    private DataProducer createNetCDFDataProducer(DataContainer output,
        Date startDate, Date endDate) {
        // The DataProducer to return
        DataProducer netCDFDataProducer = new DataProducer();

        // Set it to a process run
        try {
            netCDFDataProducer
                .setDataProducerType(DataProducer.TYPE_PROCESS_RUN);
        } catch (MetadataException e3) {
            logger.error("MetadataException caught trying to set the "
                + "NetCDF creation data producer to a type of process run: "
                + e3.getMessage());
        }
        // Set the name to the name of this class
        try {
            netCDFDataProducer.setName("NetCDFConverter on "
                + xmlDateFormat.format(startDate));
        } catch (MetadataException e3) {
            logger.error("MetadataException caught trying to "
                + "set the name of the NetCDF DataProducer to :"
                + UpdateBot.class.getName());
        }
        // Set the description
        try {
            netCDFDataProducer
                .setDescription("This is a DataProducer that is a run of the "
                    + UpdateBot.class.getName()
                    + " to create a parallel NetCDF file for the file at URL "
                    + output.getUriString());
        } catch (MetadataException e3) {
            logger.error("MetadataException caught trying to set the "
                + "description on the DataProducer to create the NetCDF file: "
                + e3.getMessage());
        }

        // Set the data producer start and end dates
        netCDFDataProducer.setStartDate(startDate);
        netCDFDataProducer.setEndDate(endDate);

        // Set the host name
        try {
            netCDFDataProducer.setHostName(InetAddress.getLocalHost()
                .getHostName());
        } catch (UnknownHostException e3) {
            logger.error("UnknownHostException caught trying to set "
                + "hostname of DataProducer: " + e3.getMessage());
        }

        // Set the person
        Person ssdsadmin = new Person();
        try {
            ssdsadmin.setFirstname("SSDS");
            ssdsadmin.setSurname("Administrator");
            ssdsadmin.setUsername("ssdsadmin");
            ssdsadmin.setEmail("ssdsadmin@mbari.org");
        } catch (MetadataException e) {
            logger.error("MetadataException caught trying to set "
                + "the username on ssdsadmin: " + e.getMessage());
        }
        netCDFDataProducer.setPerson(ssdsadmin);

        // Set the Software
        Software netCDFCreationSoftware = new Software();
        try {
            netCDFCreationSoftware.setName(NetcdfConverter.class.getName());
            netCDFCreationSoftware
                .setDescription("This is the software that was written to "
                    + "convert various DataContainers into exact duplicates, "
                    + "but in NetCDF format");
            netCDFCreationSoftware.setSoftwareVersion("1.0");
        } catch (MetadataException e) {
            logger.error("MetadataException caught trying to set "
                + "attributes on the software: " + e.getMessage());
        }
        netCDFDataProducer.setSoftware(netCDFCreationSoftware);

        // Now return it
        return netCDFDataProducer;
    }

    /**
     * This method creates a DataProducerGroup that will be used to group NetCDF
     * DataProducers
     * 
     * @return
     */
    private DataProducerGroup createNetCDFDataProducerGroup() {
        // The DataProducerGroup to return
        DataProducerGroup netCDFDataProducerGroup = new DataProducerGroup();
        try {
            netCDFDataProducerGroup.setName("SSDS NetCDF Creators");
            netCDFDataProducerGroup
                .setDescription("This is a grouping that collects all the "
                    + "process runs where SSDS creates parallel NetCDF files.");
        } catch (MetadataException e1) {
            logger.error("MetadataException caught trying to the the "
                + "name of the DataProducerGroup to SSDS NetCDF Creators: "
                + e1.getMessage());
        }
        // Now return it
        return netCDFDataProducerGroup;
    }

    /**
     * This method creates a DataContainer that matches the NetCDF that was
     * created by the NetCDFCreator
     * 
     * @param netCDFConverter
     * @return
     */
    private DataContainer createNetCDFDataContainer(
        NetcdfConverter netCDFConverter, String uriString, String dodsUriString) {

        // The DataContainer to return
        DataContainer netCDFDataContainer = new DataContainer();

        // Set up the DataContainer
        try {
            netCDFDataContainer.setDataContainerType(DataContainer.TYPE_FILE);
            // Set the name
            netCDFDataContainer.setName(netCDFConverter.getNetcdfFile()
                .getName());
            // Set the description
            netCDFDataContainer
                .setDescription("This is a NetCDF File that was automatically generated by SSDS");
            // Set the URL
            netCDFDataContainer.setUriString(uriString);
            // Set the DODS Url
            netCDFDataContainer.setDodsUrlString(dodsUriString);
        } catch (MetadataException e1) {
            logger.error("MetadataException caught trying to set data "
                + "container type for new netcdf file: " + e1.getMessage());
        }

        // Set the DODS Accessibility
        netCDFDataContainer.setDodsAccessible(new Boolean("true"));

        // Set the mime type
        netCDFDataContainer.setMimeType("application/x-netcdf");

        // Set the flag to no create another NetCDF
        netCDFDataContainer.setNoNetCDF(new Boolean("true"));

        // Set the start and end dates
        netCDFDataContainer.setStartDate(netCDFConverter.getDateRange()
            .getStartDate());
        netCDFDataContainer.setEndDate(netCDFConverter.getDateRange()
            .getEndDate());

        // Set lat, lon, depth extents
        try {
            if (netCDFConverter.getMinLatitude() != null) {
                // First thing to do is check to see if it looks like it was
                // specified in radians, if so convert it to degrees
                double minLat = netCDFConverter.getMinLatitude().doubleValue();
                double minLatAbs = Math.abs(minLat);
                if (minLatAbs >= 0 && minLatAbs <= Math.PI) {
                    minLat = (180 * minLat) / Math.PI;
                }
                netCDFDataContainer.setMinLatitude(new Double(minLat));
            }
            if (netCDFConverter.getMaxLatitude() != null) {
                // First thing to do is check to see if it looks like it was
                // specified in radians, if so convert it to degrees
                double maxLat = netCDFConverter.getMaxLatitude().doubleValue();
                double maxLatAbs = Math.abs(maxLat);
                if (maxLatAbs >= 0 && maxLatAbs <= Math.PI) {
                    maxLat = (180 * maxLat) / Math.PI;
                }
                netCDFDataContainer.setMaxLatitude(new Double(maxLat));
            }
            if (netCDFConverter.getMinLongitude() != null) {
                double minLon = netCDFConverter.getMinLongitude().doubleValue();
                double minLonAbs = Math.abs(minLon);
                if (minLonAbs >= 0 && minLonAbs <= Math.PI) {
                    minLon = (180 * minLon) / Math.PI;
                }
                netCDFDataContainer.setMinLongitude(new Double(minLon));
            }
            if (netCDFConverter.getMaxLongitude() != null) {
                double maxLon = netCDFConverter.getMaxLongitude().doubleValue();
                double maxLonAbs = Math.abs(maxLon);
                if (maxLonAbs >= 0 && maxLonAbs <= Math.PI) {
                    maxLon = (180 * maxLon) / Math.PI;
                }
                netCDFDataContainer.setMaxLongitude(new Double(maxLon));
            }
            if (netCDFConverter.getMinDepth() != null) {
                float minDepth = netCDFConverter.getMinDepth().floatValue();
                if (minDepth < 0)
                    minDepth = 0.0F;
                netCDFDataContainer.setMinDepth(new Float(minDepth));
            }
            if (netCDFConverter.getMaxDepth() != null) {
                float maxDepth = netCDFConverter.getMaxDepth().floatValue();
                if (maxDepth < 0)
                    maxDepth = 0.0F;
                netCDFDataContainer.setMaxDepth(new Float(maxDepth));
            }
        } catch (MetadataException e2) {
            logger
                .error("MetadataException caught trying to set lat, lon, depth extents: "
                    + e2.getMessage());
        }

        // If converter has number of records, set on DataContainer
        if (netCDFConverter.getNumberOfRecords() != null)
            netCDFDataContainer.setNumberOfRecords(netCDFConverter
                .getNumberOfRecords());

        // Define the RecordDescription
        RecordDescription recordDescription = new RecordDescription();
        try {
            recordDescription.setRecordType(new Long(1));
            recordDescription
                .setBufferStyle(RecordDescription.BUFFER_STYLE_BINARY);
            recordDescription.setParseable(new Boolean("false"));
        } catch (MetadataException e1) {
            logger.error("MetadataException caught: " + e1.getMessage());
        }

        // In order to build the record variables, I will use the DODS
        // information so that I know it is accurate. This means I will loop
        // over all the variables defined in the DDS and build the
        // RecordVariables for each. In cases of some specific record variables,
        // I will do some extra querying to find out more information

        // Connect up to the DODS URL
        DConnect dConnect = null;
        DDS dds = null;
        DAS das = null;
        try {
            dConnect = new DConnect(netCDFDataContainer.getDodsUrlString(),
                true);
            dds = dConnect.getDDS();
            das = dConnect.getDAS();
            logger.debug("DDS shows name " + dds.getName());
            logger.debug("DDS shows " + dds.numVariables() + " variables.");
        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException trying to get the "
                + "DConnect and DDS from DODS: " + e.getMessage());
        } catch (MalformedURLException e) {
            logger.error("MalformedURLException trying to get the "
                + "DConnect and DDS from DODS: " + e.getMessage());
        } catch (DDSException e) {
            logger.error("DDSException trying to get the "
                + "DConnect and DDS from DODS: " + e.getMessage());
        } catch (IOException e) {
            logger.error("IOException trying to get the "
                + "DConnect and DDS from DODS: " + e.getMessage());
        } catch (ParseException e) {
            logger.error("ParseException trying to get the "
                + "DConnect and DDS from DODS: " + e.getMessage());
        } catch (DODSException e) {
            logger.error("DODSException trying to get the "
                + "DConnect and DDS from DODS: " + e.getMessage());
        }

        // Grab the variables
        Enumeration ddsEnum = dds.getVariables();
        long columnIndex = 1;
        while (ddsEnum.hasMoreElements()) {

            // The new RecordVariable
            RecordVariable tempRV = new RecordVariable();
            tempRV.setColumnIndex(columnIndex);

            // Grab the next BaseType
            Object obj = ddsEnum.nextElement();

            // Cast it to a BaseType first
            if (obj instanceof BaseType) {
                BaseType baseType = (BaseType) obj;

                // Set the RV name and long name
                try {
                    tempRV.setName(baseType.getName());
                } catch (MetadataException e) {
                    logger.error("MetadataException caught trying to "
                        + "set name or long name: " + e.getMessage());
                }

                // Now we have to watch for DConstructors and if we get one, we
                // need to grab the first variable to make sure we get the real
                // variable
                if (baseType instanceof DConstructor) {
                    // Since it is a DConstructor and we built the NetCDF files,
                    // we know it will be a DGrid with one variable based in
                    // time.
                    if (baseType instanceof DGrid) {
                        DGrid dgrid = (DGrid) baseType;
                        BaseType trueBaseType = null;
                        // Now grab the variable
                        try {
                            trueBaseType = dgrid.getVar(0);
                        } catch (NoSuchVariableException e) {
                            logger
                                .error("NoSuchVariableException caught trying "
                                    + "to grab the first variable from the DGrid: "
                                    + e.getMessage());
                        }

                        // Now we can set the correct from the base type
                        if (trueBaseType instanceof DBoolean) {
                            tempRV.setFormat("boolean");
                        } else if (trueBaseType instanceof DByte) {
                            tempRV.setFormat("byte");
                        } else if (trueBaseType instanceof DFloat32) {
                            tempRV.setFormat("float");
                        } else if (trueBaseType instanceof DFloat64) {
                            tempRV.setFormat("double");
                        } else if (trueBaseType instanceof DInt16) {
                            tempRV.setFormat("short");
                        } else if (trueBaseType instanceof DInt32) {
                            tempRV.setFormat("int");
                        } else if (trueBaseType instanceof DString) {
                            tempRV.setFormat("String");
                        } else if (trueBaseType instanceof DVector) {
                            PrimitiveVector basePrimitiveVector = ((DVector) trueBaseType)
                                .getPrimitiveVector();
                            if (basePrimitiveVector instanceof BooleanPrimitiveVector) {
                                tempRV.setFormat("boolean");
                            } else if (basePrimitiveVector instanceof BytePrimitiveVector) {
                                tempRV.setFormat("byte");
                            } else if (basePrimitiveVector instanceof Float32PrimitiveVector) {
                                tempRV.setFormat("float");
                            } else if (basePrimitiveVector instanceof Float64PrimitiveVector) {
                                tempRV.setFormat("double");
                            } else if (basePrimitiveVector instanceof Int16PrimitiveVector) {
                                tempRV.setFormat("short");
                            } else if (basePrimitiveVector instanceof Int32PrimitiveVector) {
                                tempRV.setFormat("long");
                            }
                        } else {
                            logger
                                .error("Could not interpret the type returned in DDS");
                        }

                        // Now do special processing if variables are
                        // lat,lon,depth and assign standard variables
                        if (baseType.getName().equalsIgnoreCase("latitude")) {
                            StandardVariable latSV = new StandardVariable();
                            try {
                                latSV.setName("latitude");
                                latSV
                                    .setNamespaceUriString("http://marinemetadata.org/cf");
                            } catch (MetadataException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            tempRV.setStandardVariable(latSV);
                        }
                        if (baseType.getName().equalsIgnoreCase("longitude")) {
                            StandardVariable lonSV = new StandardVariable();
                            try {
                                lonSV.setName("longitude");
                                lonSV
                                    .setNamespaceUriString("http://marinemetadata.org/cf");
                            } catch (MetadataException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            tempRV.setStandardVariable(lonSV);
                        }
                        if (baseType.getName().equalsIgnoreCase("depth")) {
                            StandardVariable depthSV = new StandardVariable();
                            try {
                                depthSV.setName("depth");
                                depthSV
                                    .setNamespaceUriString("http://marinemetadata.org/cf");
                            } catch (MetadataException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            tempRV.setStandardVariable(depthSV);
                        }
                    } else {
                        logger
                            .error("baseType was a DConstructor, not a DGrid, "
                                + "this should not happen!");
                    }
                } else if (baseType instanceof DVector) {
                    // If I get a DVector, it is going to probably be the DArray
                    // of time
                    if (baseType instanceof DArray) {
                        DArray darray = (DArray) baseType;
                        // Grab the primitive Vector
                        PrimitiveVector primitiveVector = darray
                            .getPrimitiveVector();
                        // Branch on the type
                        if (primitiveVector instanceof BooleanPrimitiveVector) {
                            tempRV.setFormat("boolean");
                        } else if (primitiveVector instanceof BytePrimitiveVector) {
                            tempRV.setFormat("byte");
                        } else if (primitiveVector instanceof Float32PrimitiveVector) {
                            tempRV.setFormat("float");
                        } else if (primitiveVector instanceof Float64PrimitiveVector) {
                            tempRV.setFormat("double");
                        } else if (primitiveVector instanceof Int16PrimitiveVector) {
                            tempRV.setFormat("short");
                        } else if (primitiveVector instanceof Int32PrimitiveVector) {
                            tempRV.setFormat("long");
                        }
                        // Since this is most likely time and if the name says
                        // that's what it is, grab the start and end dates from
                        // the DODS data
                        if (baseType.getName().equalsIgnoreCase("time")) {
                            // Set the StandardVariable
                            StandardVariable timeSV = new StandardVariable();
                            try {
                                timeSV.setName("time");
                                timeSV
                                    .setNamespaceUriString("http://marinemetadata.org/cf");
                            } catch (MetadataException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                            tempRV.setStandardVariable(timeSV);
                        }
                    } else {
                        logger
                            .error("BaseType was neither a DConstructor or a DVector, "
                                + "this should not happen!");
                    }

                }

                // Now grab the attribute table from the DAS for the variable
                AttributeTable attributeTable = das.getAttributeTable(tempRV
                    .getName());
                // Grab the units
                if (attributeTable != null) {
                    Attribute unitsAttribute = attributeTable
                        .getAttribute("units");
                    String unitsValue = unitsAttribute.getValueAt(0);
                    unitsValue = unitsValue.replaceAll("\"", "");
                    tempRV.setUnits(unitsValue);
                    Attribute longNameAttribute = attributeTable
                        .getAttribute("long_name");
                    String longNameValue = longNameAttribute.getValueAt(0);
                    longNameValue = longNameValue.replaceAll("\"", "");
                    try {
                        tempRV.setLongName(longNameValue);
                    } catch (MetadataException e) {
                        logger.error("MetadataException caught trying to "
                            + "set tempRV long name: " + e.getMessage());
                    }
                }
            } else {
                logger.error("Could not cast DDS Variable to a BaseType: ");
            }

            // Add the variable to the RecordDescription
            recordDescription.addRecordVariable(tempRV);

            // Increment the column index
            columnIndex++;
        } // End while (ddsEnum.hasMoreElements())

        // Add the RecordDescription
        netCDFDataContainer.setRecordDescription(recordDescription);

        // Try to fill out the content length from the URL
        Long contentLength = this.getContentLengthFromUrl(netCDFDataContainer
            .getUriString());
        if (contentLength != null) {
            netCDFDataContainer.setContentLength(contentLength);
        }

        // Now return it
        return netCDFDataContainer;
    }

    /**
     * This method creates a DataContainerGroup that will be used to group
     * NetCDF DataContainers
     * 
     * @return
     */
    private DataContainerGroup createNetCDFDataContainerGroup() {
        // The DataContainerGroup to return
        DataContainerGroup netCDFDataContainerGroup = new DataContainerGroup();
        try {
            netCDFDataContainerGroup.setName("SSDS Generated NetCDFs");
        } catch (MetadataException e1) {
            logger.error("MetadataException caught trying to the the "
                + "name of the DataContainerGroup to SSDS Generated NetCDFs: "
                + e1.getMessage());
        }
        // Now return it
        return netCDFDataContainerGroup;
    }

    /**
     * This method takes in information about the log file and creates a
     * Resource to match it
     * 
     * @param uriString
     * @param startDate
     * @param endDate
     * @return
     */
    private Resource createNetCDFLogFileResource(String uriString,
        Date startDate, Date endDate) {

        // The Resource to return
        Resource netCDFLogFileResource = new Resource();

        // Fill out the resource
        try {
            // Set the name
            netCDFLogFileResource.setName("NetCDF Creation Log File");
            // Set the description
            netCDFLogFileResource
                .setDescription("This is the log file that was created by "
                    + "SSDS when it converted the file at " + uriString
                    + " to a NetCDF File");
            // Set the URIString
            netCDFLogFileResource.setUriString(uriString);
        } catch (MetadataException e) {
            logger.error("MetadataException caught trying to set the name "
                + "on the resource for the NetCDF log file: " + e.getMessage());
        }
        // Set the start and end times of the netcdf creation to
        // the start and end times of the log file
        netCDFLogFileResource.setStartDate(startDate);
        netCDFLogFileResource.setEndDate(endDate);
        netCDFLogFileResource.setContentLength(this
            .getContentLengthFromUrl(netCDFLogFileResource.getUriString()));

        // Set the mime type
        netCDFLogFileResource.setMimeType("text/plain");

        // Create a resource type for the log file
        ResourceType netCDFLogFileResourceType = new ResourceType();
        try {
            netCDFLogFileResourceType.setName("text/plain");
        } catch (MetadataException e2) {
            logger.error("MetadataException caught trying to "
                + "set the resource type for the log " + "file to text/plain: "
                + e2.getMessage());
        }
        netCDFLogFileResource.setResourceType(netCDFLogFileResourceType);

        // Create a keyword for the log file (for searching)
        Keyword netCDFLogFileKeyword = new Keyword();
        try {
            netCDFLogFileKeyword.setName("Log File");
        } catch (MetadataException e1) {
            logger.error("MetadataException caught trying to set "
                + "the log file keyword to Log File: " + e1.getMessage());
        }
        netCDFLogFileResource.addKeyword(netCDFLogFileKeyword);

        // Now return it
        return netCDFLogFileResource;
    }

    /**
     * This method returns the path (File, URI, DODS, Log) for files that are
     * created during the parallel NetCDF file creation from a certain
     * DataContainer. Essentially what happens is that a DataContainer is passed
     * in and a parameter that specifies which path to return, and this method
     * will return the correct path where the NetCDF files should be created
     * 
     * @param dataContainer
     * @param pathType
     *            (FILE|URL|LOG|DODS|LOG_URL)
     * @return
     */
    private String getParallelNetCDFFilePath(DataContainer dataContainer,
        String pathType) {
        // The path to construct and return
        StringBuffer path = new StringBuffer();

        // Decide on the path separator
        String pathSeparator = File.separator;
        if (pathType.equalsIgnoreCase(PATH_TYPE_URL)
            || pathType.equalsIgnoreCase(PATH_TYPE_LOG_URL)
            || pathType.equalsIgnoreCase(PATH_TYPE_DODS))
            pathSeparator = "/";
        // Now depending on the path type, structure the correct base
        if ((pathType.equalsIgnoreCase(PATH_TYPE_FILE))
            || (pathType.equalsIgnoreCase(PATH_TYPE_LOG))) {
            // Add the base directory
            path.append(this.netCDFBaseDirectory + pathSeparator);
        } else if (pathType.equalsIgnoreCase(PATH_TYPE_WORKING_FILE)) {
            path.append(this.netCDFBaseWorkingDirectory + pathSeparator);
        } else if ((pathType.equalsIgnoreCase(PATH_TYPE_URL))
            || (pathType.equalsIgnoreCase(PATH_TYPE_LOG_URL))) {
            path.append(this.netCDFBaseUrlString + pathSeparator);
        } else if (pathType.equalsIgnoreCase(PATH_TYPE_DODS)) {
            path.append(this.dodsBaseUrlString + pathSeparator);
        }

        // First check to see if the datacontainer is a stream or file
        if (dataContainer.getDataContainerType().equalsIgnoreCase(
            DataContainer.TYPE_FILE)) {
            path.append("files" + pathSeparator);
        } else if (dataContainer.getDataContainerType().equalsIgnoreCase(
            DataContainer.TYPE_STREAM)) {
            path.append("streams" + pathSeparator);
        } else {
            path.append("files" + pathSeparator);
        }

        // Now split up the URL by the "/" and create a parallel directory path
        String uriString = dataContainer.getUriString();
        // Strip off any leading stuff up to and including ://
        uriString = uriString.substring(uriString.indexOf("://") + 3);
        // Strip off the actual file name
        String filename = uriString.substring(uriString.lastIndexOf("/") + 1);
        if (filename.indexOf(".") >= 0)
            filename = filename.substring(0, filename.indexOf("."));
        uriString = uriString.substring(0, uriString.lastIndexOf("/"));
        StringTokenizer stok = new StringTokenizer(uriString, "/");
        while (stok.hasMoreTokens()) {
            String token = stok.nextToken();
            path.append(token + pathSeparator);
        }
        // Add the filename back on
        path.append(filename);
        // Now tack on the correct file extension
        if (pathType.equalsIgnoreCase(PATH_TYPE_WORKING_FILE)
            || pathType.equalsIgnoreCase(PATH_TYPE_FILE)
            || pathType.equalsIgnoreCase(PATH_TYPE_URL)
            || pathType.equalsIgnoreCase(PATH_TYPE_DODS)) {
            // Add the base directory
            path.append(".nc");
        } else if ((pathType.equalsIgnoreCase(PATH_TYPE_LOG))
            || (pathType.equalsIgnoreCase(PATH_TYPE_LOG_URL))) {
            path.append(".nc.log");
        }
        return path.toString();
    }

    private Long getContentLengthFromUrl(String url) {
        // The length to return
        Long length = null;

        // Create a URL from the string
        URL urlToConnectTo = null;
        try {
            urlToConnectTo = new URL(url);
        } catch (MalformedURLException e) {
            logger
                .error("MalformedURLException caught trying to get content length from url "
                    + url);
        }

        // If URL could not be found, return null
        if (urlToConnectTo == null)
            return null;

        // Create a URLConnection object for a URL
        URLConnection conn = null;
        try {
            conn = urlToConnectTo.openConnection();
        } catch (IOException e) {
            logger.error("IOException caught trying to connect to url " + url);
        }

        // If no connection return nothing
        if (conn == null)
            return null;

        // List all the response headers from the server. Note: The first call
        // to getHeaderFieldKey() will implicit send the HTTP request to the
        // server.
        for (int i = 0;; i++) {
            String headerName = conn.getHeaderFieldKey(i);
            String headerValue = conn.getHeaderField(i);

            if (headerName == null && headerValue == null) {
                // No more headers
                break;
            }
            if (headerName == null) {
                // The header value contains the server's HTTP version
            } else if (headerName.equalsIgnoreCase("Content-Length")) {
                length = new Long(headerValue);
            }
        }

        // Return the result
        return length;
    }

    /**
     * This method returns a space indent for logging purposes
     * 
     * @param depth
     * @return
     */
    private String getReportIndent(int depth) {
        StringBuffer indentBuffer = new StringBuffer();
        for (int i = 0; i < (depth * 2); i++)
            indentBuffer.append(" ");
        return indentBuffer.toString();
    }

    /**
     * This method finds the appropriate email address from the given parentless
     * deployment to send the notification email to
     * 
     * @param parentlessDeployment
     * @return
     */
    private String findUserEmail(DataProducer parentlessDeployment) {
        // The email to return
        String emailToReturn = null;

        // Basically I just want to walk the deployments till I find a person
        // with an email address
        if (parentlessDeployment.getPerson() != null) {
            // Try to make sure we grab the person from SSDS
            Person personToEmail = null;
            try {
                personToEmail = (Person) pa.findEquivalentPersistentObject(
                    parentlessDeployment.getPerson(), true);
            } catch (RemoteException e) {
                logger
                    .error("RemoteExeption caught trying to get person "
                        + "associated with deployment during search for user email: "
                        + e.getMessage());
            } catch (MetadataAccessException e) {
                logger
                    .error("MetadataAccessException caught trying to get person "
                        + "associated with deployment during search for user email: "
                        + e.getMessage());
            }
            try {
                if ((personToEmail != null)
                    && (personToEmail.getEmail() != null)
                    && (!personToEmail.getEmail().equals(""))) {
                    return personToEmail.getEmail();
                }
            } catch (Throwable e) {
                logger
                    .error("Throwable caught trying to read email from person "
                        + "associated with parentless deployment: "
                        + e.getMessage());
            }
        }

        // If we are here, try with child deployments
        // Grab childDeployments directly from SSDS
        Collection childDataProducers = null;
        try {
            childDataProducers = dpa.findChildDataProducers(
                parentlessDeployment, false);
        } catch (RemoteException e) {
            logger.error("RemoteException caught trying to find child "
                + "DataProducer from parent: " + e.getMessage());
        } catch (MetadataAccessException e) {
            logger.error("MetadataAccessException caught trying to find child "
                + "DataProducer from parent: " + e.getMessage());
        }
        if (childDataProducers != null) {
            Iterator childIterator = childDataProducers.iterator();
            while (childIterator.hasNext() && (emailToReturn == null)) {
                emailToReturn = this.findUserEmail((DataProducer) childIterator
                    .next());
            }
        }

        // Now return it
        return emailToReturn;
    }

    /**
     * This method sends out the processing reports to the appropriate email
     * addresses
     * 
     * @param reportBuffer
     */
    private void sendOutReports(DataProducer parentDataProducer,
        String emailAddress) {
        logger
            .debug("A report about data producer "
                + parentDataProducer.getName() + " will be sent to "
                + emailAddress);

        // Build the subject and message
        StringBuffer subject = new StringBuffer();
        StringBuffer body = new StringBuffer();

        // Build the subject
        subject.append("SSDS Notification - "
            + parentDataProducer.getDataProducerType());
        if (parentDataProducer.getName() != null) {
            subject.append(" " + parentDataProducer.getName());
        }

        // Build the body
        body.append("<html>\n");
        body.append("<body>\n");
        body.append("<h4>SSDS Notification of Post Processing of Deployment "
            + parentDataProducer.getName());
        if ((parentDataProducer.getStartDate() != null)
            && (parentDataProducer.getEndDate() != null)) {
            body.append(" ("
                + xmlDateFormat.format(parentDataProducer.getStartDate())
                + " to "
                + xmlDateFormat.format(parentDataProducer.getEndDate()) + ")");
        }
        body.append("</h4>\n<hr/>\n");
        body.append("<ol>");
        this.addDeploymentToReport(body, parentDataProducer);
        body.append("</ol>");
        body.append("</body>\n</html>\n");

        this.sendEmail(subject.toString(), body.toString(), emailAddress);
    }

    /**
     * This method sends an email out with the specified subject and body
     * 
     * @param reportBuffer
     */
    private void sendEmail(String subject, String body, String emailAddress) {
        logger.debug("The following email will be sent:\nSubject: " + subject
            + "\nBody: " + body + "\nTo: " + emailAddress);

        // Grab the system properties
        Properties systemProps = System.getProperties();

        // Now setup the correct mail server
        systemProps.put("mail.smtp.host", this.mailHost);

        // Get a session
        Session session = Session.getInstance(systemProps, null);

        // Now construct a message and fire it off
        Message msg = new MimeMessage(session);
        try {
            msg.setFrom(new InternetAddress(this.adminEmailAddress));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress
                .parse(emailAddress));
            msg.setSubject(subject.toString());
            msg.setDataHandler(new DataHandler(new ByteArrayDataSource(body
                .toString(), "text/html")));
            msg.setHeader("X-Mailer", "sendhtml");
            msg.setSentDate(new Date());
            Transport.send(msg);
        } catch (AddressException e) {
            logger.error("AddressException caught: " + e.getMessage());
        } catch (MessagingException e) {
            logger.error("MessagingException caught: " + e.getMessage());
        } catch (IOException e) {
            logger.error("IOException caught: " + e.getMessage());
        }
    }

    /**
     * This method takes in an indent, the report StringBuffer, and a
     * DataProducer and will add the data producers information to the report
     * string buffer with the proper in
     * 
     * @param indent
     * @param reportBuffer
     * @param dataProducer
     */
    private void addDeploymentToReport(StringBuffer reportBuffer,
        DataProducer dataProducer) {

        // First grab full deployment information
        DataProducer fullDataProducer = null;
        try {
            fullDataProducer = (DataProducer) dpa
                .findEquivalentPersistentObject(dataProducer, true);
        } catch (RemoteException e) {
            logger
                .error("RemoteException caught trying to read full data producer from SSDS: "
                    + e.getMessage());
        } catch (MetadataAccessException e) {
            logger
                .error("MetadataAccessException caught trying to read full data producer from SSDS: "
                    + e.getMessage());
        }

        // Start with the Device and DataProducer information
        reportBuffer.append("<li>");
        if (fullDataProducer.getRole() != null)
            reportBuffer.append(fullDataProducer.getRole().toUpperCase() + " ");
        reportBuffer.append("Deployment");
        if (!fullDataProducer.getRole().equals(DataProducer.ROLE_SENSOR))
            reportBuffer.append(" " + fullDataProducer.getName());
        if (fullDataProducer.getDevice() != null) {
            Device deployedDevice = fullDataProducer.getDevice();
            reportBuffer.append(" of Device ");
            if (deployedDevice.getName() != null)
                reportBuffer.append(deployedDevice.getName());
            reportBuffer.append("(");
            boolean firstItem = true;
            if ((deployedDevice.getMfgName() != null)
                && (!deployedDevice.equals(""))) {
                firstItem = false;
                reportBuffer.append("Mfg = " + deployedDevice.getMfgName());
            }
            if ((deployedDevice.getMfgModel() != null)
                && (!deployedDevice.getMfgModel().equals(""))) {
                if (!firstItem)
                    reportBuffer.append(", ");
                reportBuffer.append("Model = " + deployedDevice.getMfgModel());
            }
            if ((deployedDevice.getMfgSerialNumber() != null)
                && (!deployedDevice.getMfgSerialNumber().equals(""))) {
                if (!firstItem)
                    reportBuffer.append(", ");
                reportBuffer.append("S/N = "
                    + deployedDevice.getMfgSerialNumber());
            }
            reportBuffer.append(")");
        }
        reportBuffer.append("\n");

        // Add any outputs
        if ((fullDataProducer.getOutputs() != null)
            && (fullDataProducer.getOutputs().size() > 0)) {
            reportBuffer.append("<br/><b>Data:</b><ol>\n");
            Collection outputs = fullDataProducer.getOutputs();
            Iterator outputIter = outputs.iterator();
            while (outputIter.hasNext()) {
                DataContainer fullDC = null;
                try {
                    fullDC = (DataContainer) dca
                        .findEquivalentPersistentObject(
                            (DataContainer) outputIter.next(), true);
                } catch (RemoteException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (MetadataAccessException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                if (fullDC != null) {
                    reportBuffer.append("<li>");
                    reportBuffer.append("Original: <a href=\""
                        + fullDC.getUriString() + "\">" + fullDC.getName()
                        + "</a>\n");
                    if ((fullDC.getConsumers() != null)
                        && (fullDC.getConsumers().size() > 0)) {
                        Collection consumers = fullDC.getConsumers();
                        Iterator consumerIter = consumers.iterator();
                        reportBuffer.append("<ol>\n");
                        while (consumerIter.hasNext()) {
                            DataProducer consumer = (DataProducer) consumerIter
                                .next();
                            DataProducer fullConsumer = null;
                            try {
                                fullConsumer = (DataProducer) dpa
                                    .findEquivalentPersistentObject(consumer,
                                        true);
                            } catch (RemoteException e) {
                                logger
                                    .error("RemoteException trying to get full consumer: "
                                        + e.getMessage());
                            } catch (MetadataAccessException e) {
                                logger
                                    .error("MetadataAccessException trying to get full consumer: "
                                        + e.getMessage());
                            }
                            if (fullConsumer != null) {
                                if ((fullConsumer.getOutputs() != null)
                                    && (fullConsumer.getOutputs().size() > 0)) {
                                    Collection consumerOutputs = fullConsumer
                                        .getOutputs();
                                    Iterator consumerOutputIterator = consumerOutputs
                                        .iterator();
                                    while (consumerOutputIterator.hasNext()) {
                                        DataContainer consumerOutput = (DataContainer) consumerOutputIterator
                                            .next();
                                        reportBuffer
                                            .append("<li>Derived Data Set: <a href=\""
                                                + consumerOutput.getUriString()
                                                + "\">"
                                                + consumerOutput.getName()
                                                + "</a></li>\n");
                                        if (consumerOutput.getDodsUrl() != null)
                                            reportBuffer
                                                .append("<li>DODS Derived Data Set: <a href=\""
                                                    + consumerOutput
                                                        .getDodsUrlString()
                                                    + ".html\">"
                                                    + consumerOutput.getName()
                                                    + " (DODS)</a></li>\n");
                                    }
                                }
                            }
                        }
                        reportBuffer.append("</ol>");
                    }
                    reportBuffer.append("</li>\n");
                }
            }
            reportBuffer.append("</ol>\n");
        }

        // Now add any resources that were associated with the deployment
        if (!fullDataProducer.getRole().equalsIgnoreCase(
            DataProducer.ROLE_SENSOR)) {
            Collection associatedResources = fullDataProducer.getResources();
            if ((associatedResources != null)
                && (associatedResources.size() > 0)) {
                reportBuffer.append("<br/><b>Resources:</b>\n<ol>");
                Iterator resourceIterator = associatedResources.iterator();
                int resourceCounter = 1;
                while (resourceIterator.hasNext()) {
                    Resource tempResource = (Resource) resourceIterator.next();
                    String resourceName = tempResource.getName();
                    if ((resourceName == null) || (resourceName.equals(""))) {
                        resourceName = "Resource " + resourceCounter;
                    }
                    reportBuffer.append("<li><a href=\""
                        + tempResource.getUriString() + "\">" + resourceName
                        + "</a></li>\n");
                    resourceCounter++;
                }
                reportBuffer.append("</ol>\n");
            }
        }

        // Now recusursively call on child data producers
        if ((fullDataProducer.getChildDataProducers() != null)
            && (fullDataProducer.getChildDataProducers().size() > 0)) {
            reportBuffer.append("<br/><b>Attached Devices:</b>\n<ol>");
            Collection childDPs = fullDataProducer.getChildDataProducers();
            Iterator childDPIter = childDPs.iterator();
            while (childDPIter.hasNext()) {
                this.addDeploymentToReport(reportBuffer,
                    (DataProducer) childDPIter.next());
            }
            reportBuffer.append("</ol>");
        }

        // Close out the html
        reportBuffer.append("</li>\n");
    }

    /**
     * TODO kgomes document this
     * 
     * @param args
     */
    public static void main(String[] args) {

        logger.debug("=============== UPDATE BOT RUN STARTED ON " + new Date()
            + " ===============");

        // Create a new UpdateBot object
        UpdateBot updateBot = null;
        if (args[0] != null) {
            updateBot = new UpdateBot(args[0]);
        } else {
            updateBot = new UpdateBot();
        }

        // Run the updates
        updateBot.crawlAllParentlessDeployments();

        // Now exit
        logger.debug("=============== UPDATEBOT FINISHED AT " + new Date()
            + " ===============");
        System.exit(0);

    } // End main()

    /**
     * This is the properties to read in some configuration settings
     */
    private Properties updateBotProperties = new Properties();

    /**
     * The configuration database connection properties
     */
    private String databaseDriverClassName = null;
    private String databaseJDBCUrl = null;
    private String username = null;
    private String password = null;

    /**
     * These are the necessary data access interfaces
     */
    private DataProducerAccess dpa = null;
    private DataContainerAccess dca = null;
    private DeviceAccess da = null;
    private PersonAccess pa = null;

    /**
     * Emailing properties
     */
    private String mailHost = null;
    private String sendUserEmailString = null;
    private boolean sendUserEmail = false;
    private String sendAdminEmailString = null;
    private boolean sendAdminEmail = false;
    private String adminEmailAddress = null;

    /**
     * The location of the working directory for NetCDFCreator
     */
    private String netCDFBaseWorkingDirectory = null;

    /**
     * The directory where netCDF files will be created
     */
    private String netCDFBaseDirectory = null;

    /**
     * The URL base for accessing the base netCDF directory
     */
    private String netCDFBaseUrlString = null;

    /**
     * This is the base part of the URL for DODS access to the NetCDF files
     * created by this process
     */
    private String dodsBaseUrlString = null;

    /**
     * This is the collection of all parentless dataProducers in SSDS.
     */
    private Collection parentlessDeployments = new ArrayList();

    /**
     * This is a DataProducer that can be set in the constructor of the
     * UpdateBot. If it is set, only this DataProducer will be updated
     */
    private String specifiedDataProducerName = null;

    /**
     * A boolean to help track whether or not the deployment has any open
     * streams
     */
    private boolean containsOpenStreams = false;

    /**
     * A StringBuffer used to write log reports
     */
    private StringBuffer deploymentProcessingLogBuffer = null;

    /**
     * Some constants
     */
    private final String PATH_TYPE_WORKING_FILE = "WORKING";
    private final String PATH_TYPE_FILE = "FILE";
    private final String PATH_TYPE_URL = "URL";
    private final String PATH_TYPE_LOG = "LOG";
    private final String PATH_TYPE_DODS = "DODS";
    private final String PATH_TYPE_LOG_URL = "LOG_URL";

    /**
     * A date formatter
     */
    private XmlDateFormat xmlDateFormat = new XmlDateFormat();

    /**
     * A log4j logger
     */
    static Logger logger = Logger.getLogger(UpdateBot.class);
}