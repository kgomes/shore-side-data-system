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
package moos.ssds.io.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import moos.ssds.io.PacketInput;
import moos.ssds.io.PacketOutput;
import moos.ssds.io.SSDSDevicePacket;

/**
 * <p>
 * This class is used to merge serialized packet streams into one stream (file).
 * It is more of a utility class.
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.1 $
 */
public class PacketMerger {

    /**
     * This is the default constructor
     */
    public PacketMerger() {}

    /**
     * @return
     */
    public boolean mergeOnMetadataID() {

        logger.debug("mergeOnMetadataID called");
        // A boolean to track the success of the merge
        boolean mergeSuccess = true;

        // Check the merge conditions
        if (this.checkIfMergeConditionsOK()) {
            mergeSuccess = this.merge(this.packetDirectory
                .listFiles(new PacketNameFileFilter(this.deviceID, null,
                    this.recordType, this.parentID)));
        } else {
            logger.debug("Merge conditions failed, no merge done");
            mergeSuccess = false;
        }
        return mergeSuccess;
    }

    public boolean mergeOnRecordType() {

        logger.debug("mergeOnRecordType called");
        // A boolean to track the success of the merge
        boolean mergeSuccess = true;

        // Check the merge conditions
        if (this.checkIfMergeConditionsOK()) {
            mergeSuccess = this.merge(this.packetDirectory
                .listFiles(new PacketNameFileFilter(this.deviceID,
                    this.metadataID, null, this.parentID)));
        } else {
            logger.debug("Merge conditions failed, no merge done");
            mergeSuccess = false;
        }
        return mergeSuccess;
    }

    public boolean mergeOnParentID() {

        logger.debug("mergeOnParentID called");
        // A boolean to track the success of the merge
        boolean mergeSuccess = true;

        // Check the merge conditions
        if (this.checkIfMergeConditionsOK()) {
            mergeSuccess = this.merge(this.packetDirectory
                .listFiles(new PacketNameFileFilter(this.deviceID,
                    this.metadataID, this.recordType, null)));
        } else {
            logger.debug("Merge conditions failed, no merge done");
            mergeSuccess = false;
        }
        return mergeSuccess;
    }

    /**
     * This method performs a merge on all the files supplied in the parameters
     * and will remove duplicates by what is currently set as the duplicate
     * criterion
     */
    private boolean merge(File[] filesToMerge) {

        logger.debug("Merge called");
        // The boolean to return
        boolean mergeSuccess = true;
        // Now the same for the merged file
        if (this.mergedPacketFile.exists()) {
            logger
                .debug("Merged file already exists, will delete and create a new one");
            this.mergedPacketFile.delete();
            try {
                this.mergedPacketFile.createNewFile();
            } catch (IOException e) {
                logger.error("IOException caught: " + e.getMessage());
                mergeSuccess = false;
            }
        }

        // If a baseFilename was specified, do that one first.
        if (this.baseFileName != null) {
            // Put that one at files index 0 if not there already
            logger.debug("Going to search for and make sure the baseFileName ("
                + this.getBaseFileName() + ") is first in the list");
            for (int i = 0; i < filesToMerge.length; i++) {
                // Check for index of base file
                if (filesToMerge[i].getName().indexOf(this.baseFileName) >= 0) {
                    logger.debug("Found the file at index " + i);
                    if (i > 0) {
                        logger.debug("Going to swap entries " + i + " and 0");
                        logger.debug("Before they were: 0="
                            + filesToMerge[0].getAbsolutePath() + ": " + i
                            + "=" + filesToMerge[i].getAbsolutePath());
                        File baseFile = filesToMerge[i];
                        filesToMerge[i] = filesToMerge[0];
                        filesToMerge[0] = baseFile;
                        logger.debug("After they are: 0="
                            + filesToMerge[0].getAbsolutePath() + ": " + i
                            + "=" + filesToMerge[i].getAbsolutePath());
                    } else {
                        logger
                            .debug("Already at index 0, no shuffling will occur");
                    }
                }
            }
        }

        // For debugging purposes
        int totalNumberOfSubPacketsRead = 0;

        // Create an array list of all the keys that we are removing duplicates
        // on
        ArrayList keysAlreadyInMergedFile = new ArrayList();
        // The treemap to store the sorted packets
        TreeMap packetTreeMap = new TreeMap();

        // Loop through all the files
        for (int i = 0; i < filesToMerge.length; i++) {
            // Create an array list to hold all the packets from this file that
            // will be added to the merged packet file
            logger.debug("Going to read packets from file "
                + filesToMerge[i].getAbsolutePath());
            // Create a new PacketInput
            PacketInput pi = null;
            try {
                pi = new PacketInput(filesToMerge[i]);
            } catch (IOException e) {
                logger
                    .error("IOException caught trying to open PacketInput on file "
                        + filesToMerge[i].getAbsolutePath());
            }
            // Start reading packets
            int packetCounter = 0;
            while (pi.hasMoreElements()) {
                // A boolean to track whether the key for the read packet was
                // found
                boolean alreadyThere = false;
                // Grab the packet
                SSDSDevicePacket ssdsDP = null;
                try {
                    ssdsDP = (SSDSDevicePacket) pi.nextElement();
                } catch (Exception e4) {
                    logger.error("Trouble reading packet" + e4.getMessage());
                    logger.error("Will try to keep going");
                    e4.printStackTrace();
                }
                // Increment the number of subpackets read
                packetCounter++;
                totalNumberOfSubPacketsRead++;
                // Grab the key that will be used to remove duplicates
                Long key = null;
                if (removeDuplicatesBy
                    .equals(PacketMerger.DUPLICATES_BY_SEQUENCE_NUMBER)) {
                    if (ssdsDP != null) {
                        key = new Long(ssdsDP.sequenceNo());
                        if (keysAlreadyInMergedFile.contains(key)) {
                            alreadyThere = true;
                        }
                    }
                } else {
                    if (removeDuplicatesBy
                        .equals(PacketMerger.DUPLICATES_BY_TIMESTAMP)) {
                        if (ssdsDP != null) {
                            key = new Long(ssdsDP.systemTime());
                            if (keysAlreadyInMergedFile.contains(key)) {
                                alreadyThere = true;
                            }
                        }
                    }
                }
                // Check to see if the packet should be added for merging
                if (!alreadyThere && (ssdsDP != null)) {
                    // Check the timestamp on the packet to filter out whacked
                    // ones
                    Date checkDate = new Date();
                    checkDate.setTime(ssdsDP.systemTime());
                    if (checkDate.getYear() < 3000) {
                        packetTreeMap.put(key, ssdsDP);
                    }
                    keysAlreadyInMergedFile.add(key);
                }
            }
            logger.debug(packetCounter + " packets were read from file");

            // Now close the packet input stream
            try {
                pi.close();
            } catch (IOException e2) {}
        }

        // Now create the packet output to write all this to
        logger
            .debug("Going to write out all the sorted/filtered packets to the merged file "
                + this.mergedPacketFile.getAbsolutePath());
        PacketOutput sortedOutput = null;
        try {
            sortedOutput = new PacketOutput(this.mergedPacketFile);
        } catch (IOException e3) {
            logger
                .error("There was a problem with opening up the file to write sorted packets to: "
                    + e3.getMessage());
        }

        // Now write the sorted packets out to the file
        Set keySet = packetTreeMap.keySet();
        Iterator keyIterator = keySet.iterator();

        // Loop over the packets to add and write them out
        while (keyIterator.hasNext()) {
            SSDSDevicePacket packetToMerge = (SSDSDevicePacket) packetTreeMap
                .get(keyIterator.next());
            try {
                sortedOutput.writeObject(packetToMerge);
            } catch (IOException e4) {
                logger.error("Problem with writing merged packet:"
                    + e4.getMessage());
                mergeSuccess = false;
            }
        }
        // Close up the PacketOutput for the merged file
        try {
            sortedOutput.close();
        } catch (IOException e1) {
            logger.error("Problem with closing merged PacketOutput: "
                + e1.getMessage());
        }

        logger.debug("Total number of packets read from all files    = "
            + totalNumberOfSubPacketsRead);
        logger.debug("Final number of packets written to merged file = "
            + packetTreeMap.size());

        return mergeSuccess;
    }

    /**
     * This method returns a boolean that indicates whether or not the merge can
     * happen. It checks directories and files to make sure everything is OK
     * 
     * @return a <code>boolean</code> that indicates if the merge can go ahead
     *         <b>true</b> or if not <b>false</b>.
     */
    private boolean checkIfMergeConditionsOK() {
        // Create the boolean to return
        boolean goAheadWithMerge = true;
        // First make sure the directory where the packets are exists
        if ((this.packetDirectory == null) || (!this.packetDirectory.exists())) {
            goAheadWithMerge = false;
            this.statusMessage = this.statusMessage
                + "\n"
                + new Date()
                + ": The directory where the packets are to be read from does not exist or was not set";
        }
        // Check to make sure the location where the merge file will go exists
        if ((this.mergedPacketDirectory == null)
            || (!this.mergedPacketDirectory.exists())) {
            goAheadWithMerge = false;
            this.statusMessage = this.statusMessage
                + "\n"
                + new Date()
                + ": The directory where the merged file will be written does not exist or was not set";
        }
        // If the merged file exists already, don't create the new one
        if ((this.mergedPacketFile == null) || (this.mergedPacketFile.exists())) {
            goAheadWithMerge = false;
            this.statusMessage = this.statusMessage
                + "\n"
                + new Date()
                + ": The file where the merged packets will be written already exists or was not set";
        }
        return goAheadWithMerge;
    }

    /**
     * This is the method that allows this to be used from the command line
     * 
     * @param args
     *            These are the parameters that specify how the merge is to
     *            happen
     */
    public static void main(String[] args) {
        // Check for usage requirements
        if (args.length < 9) {
            System.out.println("Usage: java moos.ssds.io.PacketMerger "
                + "packetLocation " + "resultLocation " + "mergedFilename "
                + "deviceID " + "metadataID " + "recordType " + "parentID "
                + "removeDuplicatesBy"
                + "mergeOn(metadataID|recordType|parentID) [baseFilename]");
            System.exit(0);
        }

        // Create a new PacketMerger object
        PacketMerger pmerger = new PacketMerger();

        // Set all the properties from the command line arguements
        pmerger.setPacketDirectory(args[0]);
        PacketMerger.logger.debug("Packet directory will be "
            + pmerger.getPacketDirectory().getAbsolutePath());
        pmerger.setMergedPacketDirectory(args[1]);
        PacketMerger.logger.debug("Merged packet directory will be "
            + pmerger.getMergedPacketDirectory().getAbsolutePath());
        pmerger.setMergedPacketFile(args[2]);
        PacketMerger.logger.debug("Merged packet file will be "
            + pmerger.getMergedPacketFile().getAbsolutePath());
        pmerger.setDeviceID(args[3]);
        PacketMerger.logger.debug("Device id is " + pmerger.getDeviceID());
        pmerger.setMetadataID(args[4]);
        PacketMerger.logger.debug("Metadata id is " + pmerger.getMetadataID());
        pmerger.setRecordType(args[5]);
        PacketMerger.logger.debug("RecordType is " + pmerger.getRecordType());
        pmerger.setParentID(args[6]);
        PacketMerger.logger.debug("ParentID is " + pmerger.getParentID());
        pmerger.setRemoveDuplicatesBy(args[7]);
        PacketMerger.logger.debug("Will remove duplicates by "
            + pmerger.getRemoveDuplicatesBy());

        // Check to see if a base file name was specified
        if (args.length > 9) {
            if (args[9] != null) {
                pmerger.setBaseFileName(args[9]);
                PacketMerger.logger
                    .debug("Base file name was specified and will be "
                        + pmerger.getBaseFileName());
            }
        }

        // Now depending on what was to be merged by, do the merge
        if (args[8].equals("metadataID"))
            pmerger.mergeOnMetadataID();
        if (args[8].equals("recordType"))
            pmerger.mergeOnRecordType();
        if (args[8].equals("parentID"))
            pmerger.mergeOnParentID();

        // Now print them out
        PacketInput sortedPI = null;
        try {
            sortedPI = new PacketInput(pmerger.getMergedPacketFile());
            PacketMerger.logger
                .debug("Will now read back the packets from the merged file:"
                    + pmerger.getMergedPacketFile().getAbsolutePath());
        } catch (IOException e2) {
            PacketMerger.logger
                .debug("Could not open packet input for reading");
        }
        while (sortedPI.hasMoreElements()) {
            SSDSDevicePacket tempDP = (SSDSDevicePacket) sortedPI.nextElement();
            PacketMerger.logger.debug("SeqNum:" + tempDP.sequenceNo()
                + ",\tTimestamp:" + new Date(tempDP.systemTime()));
        }
        // Close it up
        try {
            sortedPI.close();
        } catch (IOException e3) {}
    }

    /**
     * @return Returns the packetDirectory.
     */
    public File getPacketDirectory() {
        return packetDirectory;
    }

    /**
     * @param packetDirectory
     *            The packetDirectory to set.
     */
    public void setPacketDirectory(File packetDirectory) {
        this.packetDirectory = packetDirectory;
        this.packetDirectoryName = packetDirectory.getAbsolutePath();
    }

    /**
     * @param packetDirectoryName
     */
    public void setPacketDirectory(String packetDirectoryName) {
        this.packetDirectoryName = packetDirectoryName;
        this.packetDirectory = new File(packetDirectoryName);
    }

    /**
     * @return Returns the mergedPacketDirectory.
     */
    public File getMergedPacketDirectory() {
        return mergedPacketDirectory;
    }

    /**
     * @param mergedPacketDirectory
     *            The mergedPacketDirectory to set.
     */
    public void setMergedPacketDirectory(File mergedPacketDirectory) {
        this.mergedPacketDirectory = mergedPacketDirectory;
        this.mergedPacketDirectoryName = mergedPacketDirectory
            .getAbsolutePath();
    }

    public void setMergedPacketDirectory(String mergedPacketDirectoryName) {
        this.mergedPacketDirectoryName = mergedPacketDirectoryName;
        this.mergedPacketDirectory = new File(mergedPacketDirectoryName);
    }

    /**
     * @return Returns the mergedPacketFile.
     */
    public File getMergedPacketFile() {
        return mergedPacketFile;
    }

    /**
     * @param mergedPacketFile
     *            The mergedPacketFile to set.
     */
    public void setMergedPacketFile(File mergedPacketFile) {
        this.mergedPacketFile = mergedPacketFile;
        this.mergedPacketFileName = mergedPacketFile.getAbsolutePath();
    }

    /**
     * @param mergedPacketFileName
     */
    public void setMergedPacketFile(String mergedPacketFileName) {
        this.mergedPacketFile = new File(this.mergedPacketDirectory
            .getAbsolutePath()
            + File.separator + mergedPacketFileName);
        this.mergedPacketFileName = mergedPacketFile.getAbsolutePath();
    }

    /**
     * @return Returns the deviceID.
     */
    public Long getDeviceID() {
        return deviceID;
    }

    /**
     * @param deviceID
     *            The deviceID to set.
     */
    public void setDeviceID(Long deviceID) {
        this.deviceID = deviceID;
    }

    /**
     * @param deviceID
     */
    public void setDeviceID(String deviceID) {
        this.deviceID = null;
        try {
            this.deviceID = new Long(deviceID);
        } catch (NumberFormatException e) {}
    }

    /**
     * @return Returns the metadataID.
     */
    public Long getMetadataID() {
        return metadataID;
    }

    /**
     * @param metadataID
     *            The metadataID to set.
     */
    public void setMetadataID(Long metadataID) {
        this.metadataID = metadataID;
    }

    /**
     * @param metadataID
     */
    public void setMetadataID(String metadataID) {
        this.metadataID = null;
        try {
            this.metadataID = new Long(metadataID);
        } catch (NumberFormatException e) {}
    }

    /**
     * @return Returns the recordType.
     */
    public Long getRecordType() {
        return recordType;
    }

    /**
     * @param recordType
     *            The recordType to set.
     */
    public void setRecordType(Long recordType) {
        this.recordType = recordType;
    }

    /**
     * @param recordType
     */
    public void setRecordType(String recordType) {
        this.recordType = null;
        try {
            this.recordType = new Long(recordType);
        } catch (NumberFormatException e) {}
    }

    /**
     * @return Returns the parentID.
     */
    public Long getParentID() {
        return parentID;
    }

    /**
     * @param parentID
     *            The parentID to set.
     */
    public void setParentID(Long parentID) {
        this.parentID = parentID;
    }

    /**
     * @param parentID
     */
    public void setParentID(String parentID) {
        this.parentID = null;
        this.parentID = new Long(parentID);
    }

    /**
     * @return Returns the removeDuplicatesBy.
     */
    public String getRemoveDuplicatesBy() {
        return removeDuplicatesBy;
    }

    /**
     * @param removeDuplicatesBy
     *            The removeDuplicatesBy to set.
     */
    public void setRemoveDuplicatesBy(String removeDuplicatesBy) {
        if ((removeDuplicatesBy != null)
            && ((removeDuplicatesBy
                .equals(PacketMerger.DUPLICATES_BY_SEQUENCE_NUMBER)) || (removeDuplicatesBy
                .equals(PacketMerger.DUPLICATES_BY_TIMESTAMP)))) {
            this.removeDuplicatesBy = removeDuplicatesBy;
        }
    }

    /**
     * @return Returns the baseFileName.
     */
    public String getBaseFileName() {
        return baseFileName;
    }

    /**
     * @param baseFileName
     *            The baseFileName to set.
     */
    public void setBaseFileName(String baseFileName) {
        this.baseFileName = baseFileName;
    }

    /**
     * This class implements the <code>FileFilter</code> interface and allows
     * the client to test files to see if their name fits the criterion supplied
     * 
     * @author Kevin Gomes
     */
    private class PacketNameFileFilter implements FileFilter {

        /**
         * This is the constructor of the FileFilter. This allows the client to
         * specify how the names will be filtered
         * 
         * @param deviceID
         *            is the id of the device to be searched for. If it is left
         *            null, a wildcard will be put in its place for the search
         * @param metadataID
         *            is the id of the metadata revision to be searched for. If
         *            it is left null, a wildcard will be inserted in its place
         *            for the search.
         * @param recordType
         *            is the number that represents the record type to search
         *            for. If it is left null, a wildcard will be put in its
         *            place for the search
         * @param parentID
         *            is the id of the parent of the device specified. If it is
         *            left null, a wildcard will be inserted in its place for
         *            the search
         */
        public PacketNameFileFilter(Long deviceID, Long metadataID,
            Long recordType, Long parentID) {

            // These are the string versions of the longs that
            // will be used to construct the filter
            String deviceIDString = null;
            String recordTypeString = null;
            String metadataIDString = null;
            String parentIDString = null;
            // If the incoming arguments are null, insert wildcards,
            // otherwise, change the Longs to Strings
            if (deviceID == null) {
                deviceIDString = ".*";
            } else {
                deviceIDString = "" + deviceID;
            }
            if (recordType == null) {
                recordTypeString = ".*";
            } else {
                recordTypeString = "" + recordType;
            }
            if (metadataID == null) {
                metadataIDString = ".*";
            } else {
                metadataIDString = "" + metadataID;
            }
            if (parentID == null) {
                parentIDString = ".*";
            } else {
                parentIDString = "" + parentID;
            }
            // Now construct the pattern string
            this.packetNamePattern = new String(deviceIDString + "_"
                + metadataIDString + "_" + recordTypeString + "_"
                + parentIDString);
        }

        // The implementation of the accept method
        public boolean accept(File f) {
            // A boolean to return the result
            boolean accept = false;
            // Grab the name of the file
            String fileName = f.getName();
            // Trim the empty space
            fileName.trim();
            // This is the data pattern that will be used to check for matches
            Pattern dataPattern = null;
            // Check for the match and return the result
            if ((packetNamePattern != null) && (!packetNamePattern.equals(""))
                && (fileName != null) && (!fileName.equals(""))) {
                try {
                    dataPattern = Pattern.compile(packetNamePattern);
                } catch (Exception ex) {}
                if (dataPattern != null) {
                    Matcher m = dataPattern.matcher(fileName);
                    accept = m.matches();
                }
            }
            return accept;
        }

        // This is the string to use for pattern matching filenames
        private String packetNamePattern = null;
    }

    /**
     * This is the directory where the serialized packets are stored
     */
    private String packetDirectoryName = null;
    private File packetDirectory = null;

    /**
     * This is the directory where the merged file will be stored
     */
    private String mergedPacketDirectoryName = null;
    private File mergedPacketDirectory = null;

    /**
     * This is the file that will be where the merged packets will be stored
     */
    private String mergedPacketFileName = null;
    private File mergedPacketFile = null;

    /**
     * These are the keys that will control the merging
     */
    private Long deviceID = null;
    private Long metadataID = null;
    private Long recordType = null;
    private Long parentID = null;

    /**
     * This is the string that specifies how duplicates are to be removed and
     * should match one of the constants defined below. It defaults to remove by
     * timestamp.
     */
    private String removeDuplicatesBy = PacketMerger.DUPLICATES_BY_TIMESTAMP;

    /**
     * This is the name of the file to use first. This is optional, but if
     * specified, the merger will go through that file first. This is so that
     * you can prioritize a file to put first in the duplicate removal process
     */
    private String baseFileName = null;

    /**
     * This is a message that a client can check the status of the merger
     */
    private String statusMessage = new String();

    // These are constants that define how the duplicates are removed
    private final static String DUPLICATES_BY_TIMESTAMP = "timestamp";
    private final static String DUPLICATES_BY_SEQUENCE_NUMBER = "sequenceNumber";

    private static Logger logger = Logger.getLogger(PacketMerger.class);

}