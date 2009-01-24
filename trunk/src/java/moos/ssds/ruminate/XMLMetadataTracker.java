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
package moos.ssds.ruminate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * This class gives ruminate an easy way to check to see if the XML that was
 * recieved with a certain set of keys (parent, device, dataDescriptionID) has
 * changed or not.
 * 
 * @author kgomes
 */
public class XMLMetadataTracker {

	/**
	 * This method checks to see if the combination of keys and metadata pass
	 * the criteria to make it "new" metadata.
	 * 
	 * @param deviceID
	 * @param parentID
	 * @param dataDescriptionID
	 * @param buffer
	 * @return
	 */
	public static synchronized boolean checkIfNewMetadata(long deviceID,
			long parentID, long dataDescriptionID, File xmlFile) {

		// First read in the properties file
		try {
			ruminateProps
					.load(XMLMetadataTracker.class
							.getResourceAsStream("/moos/ssds/ruminate/ruminate.properties"));
		} catch (Exception e) {
			logger.error("Exception trying to read in properties file: "
					+ e.getMessage());
		}

		// The boolean to return
		boolean newMetadata = true;
		logger.debug("checkIfNewMetadata called with deviceID " + deviceID
				+ ", parentID " + parentID + ", dataDescriptionID "
				+ dataDescriptionID);
		if (xmlFile != null) {
			logger.debug("And incoming xmlFile is " + xmlFile.getName());
		} else {
			logger.debug("Incoming xmlFile is null");
		}

		// First check to see if the properties have been loaded/instantiated
		if (xmlFileProps == null) {
			logger.debug("xmlFileProps not loaded, will do so now");
			loadXMLFileProps();
		}

		// First check to see if the property exists
		String xmlFileList = xmlFileProps.getProperty(deviceID + "." + parentID
				+ "." + dataDescriptionID);
		if ((xmlFileList != null) && (!xmlFileList.equals(""))) {
			logger.debug("xmlFileList from property is " + xmlFileList);
			// First read the XML from the incoming file
			String incomingXML = readXMLFromFile(xmlFile);
			logger.debug("Read in incoming XML");

			// OK, there are some previous XML files available, so we need to
			// compare the incoming to each file. First grab all the filenames
			String[] xmlFileNames = xmlFileList.split(",");
			// Now loop over those names
			for (int i = 0; i < xmlFileNames.length; i++) {
				File xmlFileToCompare = new File(ruminateProps
						.getProperty("ruminate.storage.xml")
						+ File.separator + xmlFileNames[i]);
				logger.debug("Comparing to file " + xmlFileToCompare.getName());
				String xmlToCompare = readXMLFromFile(xmlFileToCompare);
				if (xmlToCompare != null) {
					if (xmlToCompare.equals(incomingXML)) {
						logger.debug("It matched so it is not new metadata");
						newMetadata = false;
					} else {
						logger.debug("No match");
					}
				}
			}
		} else {
			logger
					.debug("no xmlFileList property was found, it must be new metadata");
			newMetadata = true;
		}
		// Store the filename as a property
		if (newMetadata) {
			logger
					.debug("Appears to be new metadata, will save the file reference.");
			String oldXMLFileList = xmlFileProps.getProperty(deviceID + "."
					+ parentID + "." + dataDescriptionID);
			// Store the XML file name in the properties
			if (oldXMLFileList != null) {
				logger.debug("oldXMLFileList = " + oldXMLFileList);
				xmlFileProps.setProperty(deviceID + "." + parentID + "."
						+ dataDescriptionID, oldXMLFileList + ","
						+ xmlFile.getName());
			} else {
				logger.debug("There was no old file list");
				xmlFileProps.setProperty(deviceID + "." + parentID + "."
						+ dataDescriptionID, xmlFile.getName());
			}
			storeXMLFileProps();
		}
		// Return the result
		return newMetadata;
	}

	/**
	 * This method sets up the directories and loads in the serialized format of
	 * the sorted set for tracking version numbers.
	 */
	private static void loadXMLFileProps() {

		logger.debug("loadXMLFileProps called");

		// If properties object is null, load it
		if (xmlFileProps == null) {
			logger
					.debug("Properties object is null, will create a new one so it can be loaded");
			xmlFileProps = new Properties();
		}

		// Get the directory where the XML docs are stored
		XMLMetadataTracker.ruminateXMLMetadataTrackDirectory = new File(
				ruminateProps.getProperty("ruminate.storage.xml"));

		// If it does not exist, create it
		if (!ruminateXMLMetadataTrackDirectory.exists()) {
			logger
					.debug("ruminateXMLMetadataTrackDirectory does not exist, will create it");
			ruminateXMLMetadataTrackDirectory.mkdir();
		}
		logger.debug("ruminateXMLMetadataTrackDirectory is "
				+ ruminateXMLMetadataTrackDirectory.getName());

		// This is the serialized file for the sorted set
		XMLMetadataTracker.ruminateXMLMetadataTrackSerializedFile = new File(
				ruminateProps.getProperty("ruminate.storage.xml")
						+ File.separator + "xmlFileProps.properties");

		logger.debug("ruminateXMLMetadataTrackSerializedFile is "
				+ ruminateXMLMetadataTrackSerializedFile.getName());

		// Now try to load the serialized object into the sorted set
		if (XMLMetadataTracker.ruminateXMLMetadataTrackSerializedFile.exists()) {
			try {
				xmlFileProps.load(new FileInputStream(
						ruminateXMLMetadataTrackSerializedFile));
			} catch (FileNotFoundException e) {
				logger.error("Could not load properties from file: "
						+ e.getMessage());
			} catch (IOException e) {
				logger
						.error("IOExcepiton when trying to load properties from file: "
								+ e.getMessage());
			}
		} else {
			logger.error("No serialized form of properties exist! "
					+ "I hope this is the first time this has been setup!!"
					+ " I will go ahead and create it");
			try {
				XMLMetadataTracker.ruminateXMLMetadataTrackSerializedFile
						.createNewFile();
			} catch (IOException e1) {
				logger
						.error("Could not created the file for serializing the sorted set"
								+ e1.getMessage());
			}
		}
	}

	/**
	 * This method stores the xml file properties to a file
	 */
	private static void storeXMLFileProps() {

		logger.debug("storeXMLFileProps called");

		// If properties object is null, do nothing
		if (xmlFileProps == null) {
			logger.debug("xmlFileProps was null, will return");
			return;
		}

		// First read in the properties file
		try {
			ruminateProps
					.load(XMLMetadataTracker.class
							.getResourceAsStream("/moos/ssds/ruminate/ruminate.properties"));
			logger.debug("loaded ruminate properties");
		} catch (Exception e) {
			logger.error("Exception trying to read in properties file: "
					+ e.getMessage());
		}

		// Get the directory where the XML docs are stored
		XMLMetadataTracker.ruminateXMLMetadataTrackDirectory = new File(
				ruminateProps.getProperty("ruminate.storage.xml"));

		// If it does not exist, create it
		if (!ruminateXMLMetadataTrackDirectory.exists()) {
			logger
					.debug("ruminateXMLMetadataTrackDirectory does not exist, will create it.");
			ruminateXMLMetadataTrackDirectory.mkdir();
		}

		// This is the serialized file for the sorted set
		XMLMetadataTracker.ruminateXMLMetadataTrackSerializedFile = new File(
				ruminateProps.getProperty("ruminate.storage.xml")
						+ File.separator + "xmlFileProps.properties");
		logger.debug("Loaded up the properties file");

		// Check to see if the properties files exists
		if (XMLMetadataTracker.ruminateXMLMetadataTrackSerializedFile.exists()) {
			// Create the backup file
			File backupSerializedFile = new File(ruminateProps
					.getProperty("ruminate.storage.xml")
					+ File.separator
					+ "xmlFileProps.properties."
					+ (new Date()).getTime());
			// Rename the old one
			ruminateXMLMetadataTrackSerializedFile
					.renameTo(backupSerializedFile);
			logger.debug("Renaming the xmlProperties file to "
					+ backupSerializedFile.getName());

			// This is the serialized file for the sorted set
			ruminateXMLMetadataTrackSerializedFile = new File(ruminateProps
					.getProperty("ruminate.storage.xml")
					+ File.separator + "xmlFileProps.properties");
			try {
				ruminateXMLMetadataTrackSerializedFile.createNewFile();
				logger
						.debug("Created new ruminateXMLMetadataTrackSerializedFile with name "
								+ ruminateXMLMetadataTrackSerializedFile
										.getName());
			} catch (IOException e3) {
				logger
						.error("IOException: Could not create new serialized object file: "
								+ e3.getMessage());
			}

		} else {
			logger.error("No serialized form of the sorted set exists! "
					+ "I hope this is the first time this has been setup!!"
					+ " I will go ahead and create it");
			try {
				ruminateXMLMetadataTrackSerializedFile.createNewFile();
			} catch (IOException e1) {
				logger
						.error("Could not created the file for serializing the sorted set"
								+ e1.getMessage());
			}
		}
		// Now store the properties
		try {
			xmlFileProps.store(new FileOutputStream(
					ruminateXMLMetadataTrackSerializedFile),
					"XML File Names for Ruminate");
			logger.debug("Stored properties in new file");
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException : " + e.getMessage());
		} catch (IOException e) {
			logger.error("IOException : " + e.getMessage());
		}
	}

	/**
	 * This method reads the XML back from a file and puts it in string form
	 * 
	 * @param deviceID
	 * @param parentID
	 * @param revisionNumber
	 * @return
	 */
	private static String readXMLFromFile(File xmlFile) {
		logger.debug("readXMLFromFile called");
		// Create the string to return
		String xmlFromFile = null;
		// Check to see if the file exists
		if (xmlFile.exists()) {
			logger.debug("Going to read XML from file " + xmlFile.getName());
			// Create the file input stream
			FileInputStream fileInputStream = null;
			try {
				fileInputStream = new FileInputStream(xmlFile);
			} catch (FileNotFoundException e) {
				logger
						.error("FileNotFoundException caught while trying to read bytes from XML file: "
								+ e.getMessage());
				return null;
			}
			// Read the bytes and convert to a String
			try {
				byte[] xmlBytes = new byte[fileInputStream.available()];
				fileInputStream.read(xmlBytes);
				xmlFromFile = new String(xmlBytes);
			} catch (IOException e1) {
				logger
						.error("IOException caught while trying to read bytes from XML file: "
								+ e1.getMessage());
				return null;
			}
			// Close the stream
			try {
				fileInputStream.close();
			} catch (IOException e2) {
				logger
						.error("IOException caught while trying to read bytes from XML file: "
								+ e2.getMessage());
			}
		}
		logger.debug("OK, should be done reading, now will return it.");
		return xmlFromFile;
	}

	/**
	 * General transmogrify properties
	 */
	private static Properties ruminateProps = new Properties();

	/**
	 * This is the directory where the XML and serialized sorted set will be
	 * stored
	 */
	private static File ruminateXMLMetadataTrackDirectory = null;

	/**
	 * This is the file where the serialized sorted set will be kept
	 */
	private static File ruminateXMLMetadataTrackSerializedFile = null;

	/**
	 * This is a properties file that contains the keys mapped to all the XML
	 * files that have that arrived with those same keys.
	 */
	private static Properties xmlFileProps = null;

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(XMLMetadataTracker.class);
}