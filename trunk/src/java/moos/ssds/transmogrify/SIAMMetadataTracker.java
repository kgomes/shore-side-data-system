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
package moos.ssds.transmogrify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

/**
 * This class is designed to manage a pool of PacketOutput objects that can be
 * used by the ingest MessageDrivenBeans. The reason this is done is to prevent
 * unsynchronized writes to the file where the DevicePackets are serialized to.
 * 
 * @author kgomes
 */
public class SIAMMetadataTracker {

	/**
	 * This method checks to see if the metadata submitted has changed from the
	 * previous time it was sent. If it has not changed, nothing is done. If it
	 * has changed, the timestamp of the packet is added to the sorted set for
	 * the given device/parent combination.
	 */
	public static void checkMetadataForChange(long deviceID, long parentID,
			long timestamp, byte[] buffer) {

		logger.debug("checkMetadataForChange called with deviceID " + deviceID
				+ ", parentID " + parentID + ", timestamp " + timestamp);

		// First check to see if the HashMap has been loaded yet
		if (metadataRevisionMap == null)
			loadSIAMMetadataTracker();

		// Now check to see if there is any metadata in the buffer
		String originalBufferString = new String(buffer);
		String bufferString = (new String(buffer)).toLowerCase();
		int indexOfMetadataStartTag = bufferString.indexOf("<?xml version=");
		if (indexOfMetadataStartTag < 0) {
			indexOfMetadataStartTag = bufferString.indexOf("<metadata>");
		}
		if (indexOfMetadataStartTag >= 0) {
			int indexOfMetadataEndTag = bufferString.indexOf("</metadata>");
			if ((indexOfMetadataEndTag > 0)
					&& (indexOfMetadataStartTag < indexOfMetadataEndTag)) {

				logger.debug("Found metadata");

				// Grab the metadata string from the buffer
				String metadataString = originalBufferString.substring(
						indexOfMetadataStartTag, indexOfMetadataEndTag + 11);

				// If the key or device_parent exists, grab the sorted set
				// associated with it.
				TreeMap treeMap = null;
				if (!SIAMMetadataTracker.metadataRevisionMap
						.containsKey(new String(deviceID + "_" + parentID))) {

					logger
							.debug("No existing entry was found for key "
									+ deviceID + "_" + parentID
									+ ".  Will create one.");

					// Since there is no device/parent combination create one
					treeMap = new TreeMap();

					// Add it to the hashmap and serialize the hashmap
					SIAMMetadataTracker.metadataRevisionMap.put(new String(
							deviceID + "_" + parentID), treeMap);

				}

				// Grab the sorted set from the map
				treeMap = (TreeMap) SIAMMetadataTracker.metadataRevisionMap
						.get(new String(deviceID + "_" + parentID));

				// Grab the size
				int treeMapSize = treeMap.size();

				logger.debug("There are " + treeMapSize + " entries for key "
						+ deviceID + "_" + parentID);

				// First check to see if the sorted set is empty
				if (treeMap.size() <= 0) {

					// Go ahead and save the file with revision one
					saveXMLToFile(deviceID, parentID, 1L, metadataString);

					// Now add that to the sorted set and serialize it
					treeMap.put(new Long(timestamp), new Long("1"));
					SIAMMetadataTracker.serializeSIAMMetadataTracker();

					logger.debug("Inserted new TreeMap and serialized to disk");
				} else {

					// Now this means the set is not empty, so search for
					// the timestamp.

					// Grab the key set from the treemap
					Set treeKeys = treeMap.keySet();
					Object[] keyObjects = treeKeys.toArray();
					int indexOfSearch = Arrays.binarySearch(keyObjects,
							new Long(timestamp));

					// Now if the index was found (positive return), the
					// timestamp is already there, so do nothing. If
					// it is less than zero, that means that the
					// timestamp was not found and we should compare it
					// to the closest previous timestamps' XML.
					if (indexOfSearch < 0) {
						// First let's create a "revision" number and make
						// it one larger than the size of the tree. That
						// should fill up revision numbers from the bottom,
						// but I should check to see if the number is there
						// before writing it in.
						long currentRevisionNumber = treeMapSize + 1;
						while (treeMap.containsValue(new Long(
								currentRevisionNumber))) {
							currentRevisionNumber++;
						}

						logger.debug("Will use a revision number of "
								+ currentRevisionNumber);

						// We can do a simple check first (simple in that we
						// don't have to compare XML documents. There were cases
						// when a parent node was switched to a node that the
						// child was deployed on previously. If that is the
						// case, sometimes new deployments are not triggered if
						// the XML is the same as the last time it was deployed
						// on that parent. So we need to check to see if any of
						// the other child_parent combinations have a timestamp
						// greater than the larges time stamp in this
						// child_parent relationship. If so, that means a new
						// deployment should be generated automatically. Now the
						// bad news. The only way I really know how to do this
						// is brute force loop through all the keys and look for
						// the child.
						Set childParentKeys = SIAMMetadataTracker.metadataRevisionMap
								.keySet();
						Long currentLargestTimestamp = (Long) treeMap.lastKey();
						boolean newDeployment = false;
						if (childParentKeys != null) {
							Iterator childParentKeysIterator = childParentKeys
									.iterator();
							while (childParentKeysIterator.hasNext()) {
								String childParentToCheck = (String) childParentKeysIterator
										.next();
								if (childParentToCheck.startsWith(deviceID
										+ "_")
										&& (!childParentToCheck.equals(deviceID
												+ "_" + parentID))) {
									TreeMap tempTreeMap = (TreeMap) SIAMMetadataTracker.metadataRevisionMap
											.get(childParentToCheck);
									// Grab the largest timestamp
									Long largestTimeStamp = null;
									if (tempTreeMap != null) {
										largestTimeStamp = (Long) tempTreeMap
												.lastKey();
										if (largestTimeStamp != null) {
											if (largestTimeStamp.longValue() > currentLargestTimestamp
													.longValue()) {
												// This means that this device
												// was deployed on another
												// parent after the last
												// deployment on this device, so
												// it is a new deployment
												newDeployment = true;
												break;
											}
										}
									}

								}
							}
						}
						// If it was already deemed a new deployment, save it
						// and finish up
						if (newDeployment) {
							logger
									.debug("A new deployment was detected, will save");

							// Go ahead and save the file with revision
							saveXMLToFile(deviceID, parentID,
									currentRevisionNumber, metadataString);
							// Now add that to the sorted set and serialize it
							treeMap.put(new Long(timestamp), new Long(
									currentRevisionNumber));
							SIAMMetadataTracker.serializeSIAMMetadataTracker();
							logger
									.debug("Saved the XML, added entry to TreeMap and serialized");
						} else if (indexOfSearch == -1) {
							// Now check to see if the index is -1, that would
							// mean that the timestamp is before any other and
							// by nature you would have to save and version that
							// one. This should be extremely rare.

							logger
									.debug("Index returned on search shows "
											+ "that it should be added at the beginning");

							// Go ahead and save the file with revision
							saveXMLToFile(deviceID, parentID,
									currentRevisionNumber, metadataString);
							// Now add that to the sorted set and serialize it
							treeMap.put(new Long(timestamp), new Long(
									currentRevisionNumber));
							SIAMMetadataTracker.serializeSIAMMetadataTracker();

							logger
									.debug("Saved the XML, added entry to TreeMap and serialized");
						} else {
							// Grab the revision number to compare to
							Long revToCompare = (Long) treeMap
									.get(keyObjects[(-1 * indexOfSearch) - 2]);
							logger
									.debug("Previous revision number to compare is "
											+ revToCompare);
							// See if it is equal to the previous sequence
							// number XML
							if (!metadataString.equals(readXMLFromFile(
									deviceID, parentID, revToCompare
											.longValue()))) {

								logger
										.debug("They are different, so it will be added");

								// Go ahead and save the file with revision
								saveXMLToFile(deviceID, parentID,
										currentRevisionNumber, metadataString);

								// Now add that to the sorted set and serialize
								// it
								treeMap.put(new Long(timestamp), new Long(
										currentRevisionNumber));
								SIAMMetadataTracker
										.serializeSIAMMetadataTracker();
								logger.debug("New metadata revision "
										+ currentRevisionNumber
										+ " added and serialized to disk");
							} else {
								logger
										.debug("They are the same so nothing will be done");
							}
						}
					} else {
						logger
								.debug("There was already an entry for timestamp "
										+ timestamp);
					}
				}
			}
		} else {
			// No metadata found
			return;
		}
	}

	/**
	 * This method returns the correct data description id based on timestamp
	 * 
	 * @param buffer
	 * @return
	 */
	public static long findDataDescriptionID(long deviceID, long parentID,
			long timestamp) {

		logger.debug("findDataDescriptionID called");

		// First check to see if the HashMap has been loaded yet
		if (metadataRevisionMap == null)
			loadSIAMMetadataTracker();

		// Set it to zero in case no metadata is found
		long dataDescriptionID = 0L;

		// If the key or device_parent exists, grab the sorted set
		// associated with it.
		TreeMap treeMap = null;

		// Check to see if something with the key is found
		if (SIAMMetadataTracker.metadataRevisionMap.containsKey(new String(
				deviceID + "_" + parentID))) {

			// Grab the sorted set from the map
			treeMap = (TreeMap) SIAMMetadataTracker.metadataRevisionMap
					.get(new String(deviceID + "_" + parentID));

			// If a TreeMap was found, try to find the closest previous
			// revision number
			if (treeMap != null) {
				// Grab the keys (timestamps)
				Set treeKeys = treeMap.keySet();
				Object[] keyObjects = treeKeys.toArray();
				// Search for the timestamp
				int indexOfSearch = Arrays.binarySearch(keyObjects, new Long(
						timestamp));
				// If it is positive, return the index
				if (indexOfSearch >= 0) {
					Long revToReturn = (Long) treeMap
							.get(keyObjects[indexOfSearch]);
					dataDescriptionID = revToReturn.longValue();
					logger.debug("Found the exact timestamp, revision is "
							+ dataDescriptionID);
					return dataDescriptionID;
				} else {
					Long revToReturn = (Long) treeMap
							.get(keyObjects[(-1 * indexOfSearch) - 2]);
					dataDescriptionID = revToReturn.longValue();
					logger.debug("Found the previous timestamp, revision is "
							+ dataDescriptionID);
					return dataDescriptionID;
				}
			}
		} else {
			dataDescriptionID = 0L;
		}
		// Now return it
		return dataDescriptionID;
	}

	/**
	 * This method sets up the directories and loads in the serialized format of
	 * the sorted set for tracking version numbers.
	 */
	private static void loadSIAMMetadataTracker() {

		logger.debug("loadSIAMMetadataTracker called");

		// If the map is null, create a new one
		if (metadataRevisionMap == null) {
			logger
					.debug("Map is null, will create a new one so it can be loaded");
			metadataRevisionMap = Collections.synchronizedMap(new HashMap());
		}

		// First read in the properties file
		try {
			transmogProps
					.load(SIAMMetadataTracker.class
							.getResourceAsStream("/moos/ssds/transmogrify/transmogrify.properties"));
		} catch (Exception e) {
			logger.error("Exception trying to read in properties file: "
					+ e.getMessage());
		}

		// Now in order to store the XML for comparison and find the
		// location (directory) where the sorted set will be serialized,
		// get the file from the properties.
		SIAMMetadataTracker.transmogMetaTrackDirectory = new File(transmogProps
				.getProperty("transmogrify.metadata.tracking.directory"));
		logger
				.debug("TransmogMetaTrackDirectory is "
						+ transmogProps
								.getProperty("transmogrify.metadata.tracking.directory"));

		// If it does not exist, create it
		if (!transmogMetaTrackDirectory.exists()) {
			logger
					.info("The transmogMetaTrackDirectory "
							+ transmogProps
									.getProperty("transmogrify.metadata.tracking.directory")
							+ " does not exist, will create it ...");
			transmogMetaTrackDirectory.mkdirs();
		}

		// This is the serialized file for the sorted set
		SIAMMetadataTracker.transmogMetaTrackSerializedFile = new File(
				transmogProps
						.getProperty("transmogrify.metadata.tracking.directory")
						+ File.separator + "metadataTrackerHashMap.properties");

		// Now try to load the serialized object into the sorted set
		if (SIAMMetadataTracker.transmogMetaTrackSerializedFile.exists()) {
			SIAMMetadataTracker
					.deserializeHashMap(SIAMMetadataTracker.transmogMetaTrackSerializedFile);
		} else {
			logger.error("No serialized form of the sorted set exists! "
					+ "I hope this is the first time this has been setup!!"
					+ " I will go ahead and create it");
			try {
				SIAMMetadataTracker.transmogMetaTrackSerializedFile
						.createNewFile();
			} catch (IOException e1) {
				logger
						.error("Could not created the file for serializing the sorted set"
								+ e1.getMessage());
			}
		}
	}

	/**
	 * This method takes the incoming File and reads in the properties to
	 * convert them to the current HashMap that represents the metadata state
	 */
	private static synchronized void deserializeHashMap(File serializationFile) {

		// This is the new HashMap that will be filled and set
		Map hashMapToSet = Collections.synchronizedMap(new HashMap());

		// This is the properties that will be loaded from the file
		Properties readProperties = new Properties();
		try {
			readProperties.load(new FileInputStream(serializationFile));
		} catch (FileNotFoundException e) {
			logger
					.error("FileNotFoundException: Could not save metadata tracker state: "
							+ e.getMessage());
		} catch (IOException e) {
			logger.error("IOException: Could not save metadata tracker state: "
					+ e.getMessage());
		}
		// Grab the keys
		String keysString = readProperties.getProperty("metadataTrackerKeys");

		// Split them into keys
		if (keysString != null) {
			String[] keys = keysString.split(",");
			for (int i = 0; i < keys.length; i++) {
				String key = keys[i];
				// Grab all the timestamp keys
				String tsKeys = readProperties.getProperty(key + ".timestamps");
				String[] timestamps = tsKeys.split(",");
				TreeMap treeMap = new TreeMap();
				for (int j = 0; j < timestamps.length; j++) {
					treeMap.put(new Long(timestamps[j]), new Long(
							readProperties.getProperty(key + "."
									+ timestamps[j])));
				}
				hashMapToSet.put(key, treeMap);
			}
		}
		// Now set the hashmap
		SIAMMetadataTracker.metadataRevisionMap = hashMapToSet;
	}

	/**
	 * This method is responsible for serializing the state of the sorted set to
	 * disk in case it needs to be read in later
	 */
	private static void serializeSIAMMetadataTracker() {
		// If the serialized file exists, backup the old one and create a new
		// one
		if (SIAMMetadataTracker.transmogMetaTrackSerializedFile.exists()) {
			// Create the backup file
			File backupSerializedFile = new File(transmogProps
					.getProperty("transmogrify.metadata.tracking.directory")
					+ File.separator
					+ "metadataTrackerHashMap.properties."
					+ (new Date()).getTime());
			// Rename the old one
			SIAMMetadataTracker.transmogMetaTrackSerializedFile
					.renameTo(backupSerializedFile);

			// This is the serialized file for the sorted set
			SIAMMetadataTracker.transmogMetaTrackSerializedFile = new File(
					transmogProps
							.getProperty("transmogrify.metadata.tracking.directory")
							+ File.separator
							+ "metadataTrackerHashMap.properties");
			try {
				SIAMMetadataTracker.transmogMetaTrackSerializedFile
						.createNewFile();
			} catch (IOException e3) {
				logger
						.error("IOException: Could not create new serialized object file: "
								+ e3.getMessage());
			}

		} else {
			logger.error("No serialized form of the sorted set exists! "
					+ "I hope this is the first time this has been setup!!"
					+ " I will go ahead and create it");
			// First make sure the directory exists
			File trackerDirectory = new File(transmogProps
					.getProperty("transmogrify.metadata.tracking.directory"));
			logger.debug("SIAMMetadataTracker trackerDirectory = "
					+ trackerDirectory);
			if (!trackerDirectory.exists()) {
				logger.debug("It appears that the directory "
						+ trackerDirectory
						+ " does not exist, I will try to create it");
				trackerDirectory.mkdirs();
			}
			try {
				SIAMMetadataTracker.transmogMetaTrackSerializedFile
						.createNewFile();
			} catch (IOException e1) {
				logger
						.error("Could not created the file for serializing the sorted set"
								+ e1.getMessage());
			}
		}
		// Now write out the serialized form
		SIAMMetadataTracker
				.serializeHashMap(SIAMMetadataTracker.transmogMetaTrackSerializedFile);
	}

	/**
	 * This method takes the HashMap that represents the status of the metadata
	 * revisions and serializes to disk
	 */
	private synchronized static void serializeHashMap(File serializationFile) {
		// OK, so we need to take the hashmap and subsequent
		// TreeMaps and write them to a Properties file.
		Properties newProperties = new Properties();
		// Create a CSV value for the keys of the HashMap
		StringBuffer keysString = new StringBuffer();
		Set keys = SIAMMetadataTracker.metadataRevisionMap.keySet();
		Iterator iterator = keys.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			String keyString = (String) iterator.next();
			if (i != 0)
				keysString.append(",");
			keysString.append(keyString);
			// Now grab the TreeMap
			TreeMap keyTreeMap = (TreeMap) SIAMMetadataTracker.metadataRevisionMap
					.get(keyString);
			// Now grab those keys
			Set treeMapKeys = keyTreeMap.keySet();
			Iterator tmKeysIter = treeMapKeys.iterator();
			StringBuffer tmKeysString = new StringBuffer();
			int j = 0;
			while (tmKeysIter.hasNext()) {
				Long timestamp = (Long) tmKeysIter.next();
				if (j > 0) {
					tmKeysString.append(",");
				}
				tmKeysString.append(timestamp + "");
				Long value = (Long) keyTreeMap.get(timestamp);
				newProperties.setProperty(keyString + "." + timestamp, value
						+ "");
				j++;
			}
			newProperties.setProperty(keyString + ".timestamps", tmKeysString
					.toString());
			i++;
		}
		// Now add the keys property
		newProperties.setProperty("metadataTrackerKeys", keysString.toString());
		try {
			// Now save
			newProperties.save(new FileOutputStream(serializationFile), null);
		} catch (FileNotFoundException e) {
			logger.error("Could not save metadata tracker state: "
					+ e.getMessage());
		}
	}

	/**
	 * This method saves the XML provided to a file named after deviceID,
	 * parentID, and revision number
	 * 
	 * @param deviceID
	 * @param parentID
	 * @param revisionNumber
	 * @param metadataString
	 */
	private static void saveXMLToFile(long deviceID, long parentID,
			long revisionNumber, String metadataString) {
		// Create the file where it will be stored
		File newXMLFile = new File(SIAMMetadataTracker.transmogProps
				.getProperty("transmogrify.metadata.tracking.directory")
				+ File.separator
				+ deviceID
				+ "_"
				+ parentID
				+ "_"
				+ revisionNumber + ".xml");
		// Check to see if it exists
		if (!newXMLFile.exists()) {
			try {
				newXMLFile.createNewFile();
			} catch (IOException e1) {
				logger.error("IOException while trying to create new XML file "
						+ newXMLFile.getAbsolutePath());
			}
			// Create the output stream
			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(newXMLFile);
			} catch (FileNotFoundException e1) {
				logger
						.error("FileNotFoundException trying to create new FileOutputStream "
								+ "from " + newXMLFile.getAbsolutePath());
			}
			// Write out to it
			if (fileOutputStream != null) {
				try {
					fileOutputStream.write(metadataString.getBytes());
					fileOutputStream.flush();
					fileOutputStream.close();
				} catch (IOException e2) {
					logger
							.error("IOException trying to write XML bytes to file: "
									+ e2.getMessage());
				}
			}
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
	private static String readXMLFromFile(long deviceID, long parentID,
			long revisionNumber) {
		// Create the string to return
		String xmlFromFile = null;
		// Create the file that will be read from
		File xmlFile = new File(transmogProps
				.getProperty("transmogrify.metadata.tracking.directory")
				+ File.separator
				+ deviceID
				+ "_"
				+ parentID
				+ "_"
				+ revisionNumber + ".xml");
		// Check to see if the file exists
		if (xmlFile.exists()) {
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
		return xmlFromFile;
	}

	/**
	 * General transmogrify properties
	 */
	private static Properties transmogProps = new Properties();

	/**
	 * This is the directory where the XML and serialized sorted set will be
	 * stored
	 */
	private static File transmogMetaTrackDirectory = null;

	/**
	 * This is the file where the serialized sorted set will be kept
	 */
	private static File transmogMetaTrackSerializedFile = null;

	/**
	 * This is the sorted set that is common to all the instances of the
	 * SIAMMetadataTracker. It is synchronized because many threads will be
	 * accessing it.
	 */
	private static Map metadataRevisionMap = null;

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(SIAMMetadataTracker.class);
}