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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.TimeZone;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.naming.Context;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.jms.PublisherComponent;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.DeviceType;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.Keyword;
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.ResourceType;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.ObjectBuilder;
import moos.ssds.metadata.util.XmlBuilder;
import moos.ssds.services.metadata.DataContainerAccessLocal;
import moos.ssds.services.metadata.DataProducerAccessLocal;
import moos.ssds.services.metadata.DataProducerGroupAccessLocal;
import moos.ssds.services.metadata.DeviceAccessLocal;
import moos.ssds.util.XmlDateFormat;

import org.apache.log4j.Logger;

/**
 * <p>
 * Handles <code>MetadataPackets</code> that contain XML metadata in their
 * buffers.
 * </p>
 * <hr>
 * 
 * @author :
 * @version :
 */
public class MetadataHandler implements Handler {

	/**
	 * The ruminate properties for this instance
	 */
	private static Properties properties = new Properties();

	/**
	 * A boolean to indicate if ruminate is connected to the services of SSDS.
	 */
	private boolean connected = false;

	/**
	 * The remote services interfaces
	 */
	@javax.annotation.Resource(mappedName = "moos/ssds/services/metadata/DataProducerGroupAccessLocal")
	private DataProducerGroupAccessLocal dataProducerGroupAccessLocal = null;
	@javax.annotation.Resource(mappedName = "moos/ssds/services/metadata/DataProducerAccessLocal")
	private DataProducerAccessLocal dataProducerAccessLocal = null;
	@javax.annotation.Resource(mappedName = "moos/ssds/services/metadata/DeviceAccessLocal")
	private DeviceAccessLocal deviceAccessLocal = null;
	@javax.annotation.Resource(mappedName = "moos/ssds/services/metadata/DataContainerAccessLocal")
	private DataContainerAccessLocal dataContainerAccessLocal = null;

	/**
	 * This is the base URL of the data stream access
	 */
	private String dataStreamBaseURL = null;

	/**
	 * A PublisherComponent that will re-publish updated metadata
	 */
	private PublisherComponent publisherComponent = null;

	/**
	 * The incoming packets information
	 */
	private long deviceID = -999999;
	private long parentID = -999999;
	private int packetType = -999999;
	private long packetSubType = -999999;
	private long dataDescriptionID = -999999;
	private long dataDescriptionVersion = -999999;
	private long timestampSeconds = -999999;
	private long timestampNanoseconds = -999999;
	private long sequenceNumber = -999999;
	private int bufferLen = 1;
	private byte[] bufferBytes = new byte[bufferLen];
	private int bufferTwoLen = 1;
	private byte[] bufferTwoBytes = new byte[bufferTwoLen];

	// Some helpers
	private Date packetDate = null;

	// A data formatter
	private XmlDateFormat xmlDateFormat = new XmlDateFormat();

	/**
	 * This is a String that holds the actual metadata that was extracted from
	 * the incoming packet
	 */
	private String metadataString = null;

	/**
	 * This is the File where the incoming XML will be written to
	 */
	private java.io.File xmlFile;

	/**
	 * An object builder for building the model classes from the XML
	 */
	private ObjectBuilder objectBuilder;

	/**
	 * A boolean to indicate if the incoming XML is considered "new" metadata
	 */
	boolean newMetadata = true;

	/**
	 * This is a Log4JLogger that is used to log information to
	 */
	static Logger logger = Logger.getLogger(MetadataHandler.class);

	/**
	 * This collection is built in persistToDatabase and is used by to
	 * triggerTasks.
	 */
	// private Collection outputsToBuildNetCDSfrom = null;
	private static Properties ioProperties = new Properties();

	// Not sure
	private static final int revision = 1;

	/**
	 * This is the naming context for incoming packets
	 */
	private Context jndiContext = null;

	/**
	 * The naming context for the republishing of metadata
	 */
	private Context jmsJndiContext = null;

	/**
	 * This is the default constructor for the metadata handler
	 */
	public MetadataHandler() {
		logger.debug("MetadataHandler default constructor called.");
		// Read in the ruminate properties file
		try {
			properties.load(this.getClass().getResourceAsStream(
					"/moos/ssds/ruminate/ruminate.properties"));
			logger.debug("ruminate.properties should have read in.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Read in the ingest properties file
		try {
			ioProperties.load(this.getClass().getResourceAsStream(
					"/moos/ssds/io/io.properties"));
			logger.debug("io.properties should have read in.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Set the base URL for data stream
		this.dataStreamBaseURL = properties
				.getProperty("ruminate.ssds.datastream.servlet.base.url");

		// Now try to connect up to SSDS
		try {
			connected = this.connectToSSDS();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean connectToSSDS() {
		boolean connectedToSSDS = true;
		// Now create the publisher component to send on processed messages
		logger.debug("Going to create a Publisher component and point it to topic "
				+ properties.getProperty("ssds.ruminate.republish.topic.name")
				+ " on host "
				+ properties
						.getProperty("ssds.ruminate.republish.host.name.long"));
		this.publisherComponent = new PublisherComponent(
				properties.getProperty("ssds.ruminate.republish.topic.name"),
				properties
						.getProperty("ssds.ruminate.republish.host.name.long"));

		// Now return the result
		return connectedToSSDS;
	}

	/**
	 * TODO KJG - Document this
	 */
	public synchronized void process(BytesMessage bytesMessage) {
		// This assumes that this byte array is in the form of the SSDS
		// specification
		try {
			deviceID = bytesMessage.readLong();
			parentID = bytesMessage.readLong();
			packetType = bytesMessage.readInt();
			packetSubType = bytesMessage.readLong();
			dataDescriptionID = bytesMessage.readLong();
			dataDescriptionVersion = bytesMessage.readLong();
			timestampSeconds = bytesMessage.readLong();
			timestampNanoseconds = bytesMessage.readLong();
			sequenceNumber = bytesMessage.readLong();
			bufferLen = bytesMessage.readInt();
			bufferBytes = new byte[bufferLen];
			bytesMessage.readBytes(bufferBytes);
			bufferTwoLen = bytesMessage.readInt();
			bufferTwoBytes = new byte[bufferTwoLen];
			bytesMessage.readBytes(bufferTwoBytes);
			logger.debug("Incoming Message:");
			logger.debug("->deviceID=" + deviceID);
			logger.debug("->parentID=" + parentID);
			logger.debug("->packetType=" + packetType);
			logger.debug("->packetSubType=" + packetSubType);
			logger.debug("->dataDescriptionID=" + dataDescriptionID);
			logger.debug("->dataDescriptionVersion=" + dataDescriptionVersion);
			logger.debug("->timestampSeconds=" + timestampSeconds);
			logger.debug("->timestampNanoseconds=" + timestampNanoseconds);
			logger.debug("->sequenceNumber=" + sequenceNumber);
		} catch (JMSException e) {
			logger.error("JMSException caught: " + e.getMessage());
		}

		// Create a packet date that is easier to use
		long packetTimestamp = (timestampSeconds * 1000)
				+ (timestampNanoseconds / 1000);
		packetDate = new Date();
		packetDate.setTime(packetTimestamp);

		// Connect if not connected to SSDS
		if (!connected) {
			logger.debug("MetadataHandler was not connected to SSDS, will try to do so now");
			connected = this.connectToSSDS();
		}
		if (connected) {
			// Grab the instance data.

			// Processing steps
			logger.debug("Saving the XML to a file: ");
			boolean ok = saveToFile();
			// // Check to see if the incoming constitutes new metdata
			newMetadata = XMLMetadataTracker.checkIfNewMetadata(deviceID,
					parentID, dataDescriptionID, xmlFile);
			logger.debug("XML file new metadata? " + newMetadata);
			if (ok) {
				logger.debug("OK, now marshall XML to objects");
				marshallObjects();

				logger.debug("Marshalled. Now update model with XML files");
				updateModel();

				logger.debug("Was able to run the object builder");
				boolean ptdbOK = true;
				if (newMetadata) {
					logger.debug("Updated. Now will persist.");
					ptdbOK = persistToDatabase();
					logger.debug("Reply from persist to DB as to success = "
							+ ptdbOK);
				}
				if (ptdbOK) {
					// First re-publish data to next topic for other clients to
					// use
					logger.debug("Going to republish the updated model for downstream processing");
					republishModel();

					// // Now do the ruminate thing
					// // TODO - This will be moved out of ruminate to one of
					// the
					// // clients mentioned above
					// logger.debug("Firing tasks.");
					// if (persistedDeploymentIDs != null) {
					// // Should not have any more of these as of 20 October
					// // 2004 = mpm
					// triggerTasksWithPlatformDeployment();
					// } else if (outputsToBuildNetCDSfrom != null) {
					// triggerTasksWithOutputsCollection();
					// }
					//
					// logger.debug("Done spawning tasks");
				} else {
					logger.error("Failed to persist to database.");
				}
			}
			logger.debug("Done with process method");
		} else {
			logger.debug("Could not connect to SSDS, ruminate will not ruminate");
		}
	}

	/**
	 * Saves the xml to an external file. The file location is specified by the
	 * property 'ruminate.storage.xml' in the file 'ruminate.properties'. TODO
	 * 20030215 brian - The current file naming scheme needs to be changed. Use
	 * deviceId + date?
	 */
	private boolean saveToFile() {
		// Set the success flag to false as the default
		boolean success = false;
		// The first thing to do is look for the XML that starts and ends with
		// metadata tags
		String originalDataBufferString = new String(bufferBytes);
		String dataBufferString = new String(bufferBytes).toLowerCase();
		logger.debug("Going to look for metadata tags");
		int indexOfMetadataStartTag = dataBufferString
				.indexOf("<?xml version=");
		if (indexOfMetadataStartTag < 0) {
			indexOfMetadataStartTag = dataBufferString.indexOf("<metadata>");
		}
		logger.debug("After searching for metadata xml, the index of the starting tag is : "
				+ indexOfMetadataStartTag);
		if (indexOfMetadataStartTag >= 0) {
			int indexOfMetadataEndTag = dataBufferString.indexOf("</metadata>");
			if ((indexOfMetadataEndTag > 0)
					&& (indexOfMetadataStartTag < indexOfMetadataEndTag)) {
				// Grab the metadata string from the buffer
				metadataString = originalDataBufferString.substring(
						indexOfMetadataStartTag, indexOfMetadataEndTag + 11);
				logger.debug("Metadata found and is: " + metadataString);
				File xmlPath = new File(
						properties.getProperty("ruminate.storage.xml"));
				// Check to see if the directory exists, if not, create it
				if (!xmlPath.exists())
					xmlPath.mkdirs();
				String xmlFilename = deviceID + "_" + dataDescriptionID + "_"
						+ parentID;
				xmlFile = new File(xmlPath, xmlFilename + ".xml");
				try {
					// If the file already exists generate a unique file name
					if (xmlFile.exists()) {
						// First try with time date stamp
						Calendar now = Calendar.getInstance();
						now.setTimeZone(TimeZone.getTimeZone("GMT"));
						xmlFile = new File(xmlPath, xmlFilename + "_"
								+ now.get(Calendar.YEAR) + "_"
								+ now.get(Calendar.MONTH) + "_"
								+ now.get(Calendar.DAY_OF_MONTH) + "_"
								+ now.get(Calendar.HOUR_OF_DAY) + "_"
								+ now.get(Calendar.MINUTE) + "_"
								+ now.get(Calendar.SECOND) + ".xml");
						// If that one exists, create some temp one
						if (xmlFile.exists()) {
							xmlFile = File.createTempFile(xmlFilename, ".xml",
									xmlPath);
						}
					}
					BufferedOutputStream out = new BufferedOutputStream(
							new FileOutputStream(xmlFile));
					out.write(metadataString.getBytes());
					out.close();
					success = true;
				} catch (FileNotFoundException e) {
					logger.error("FileNotFoundException: " + e.getMessage());
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("IOException: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		return success;
	}

	/**
	 * Builds the object model from the XML.
	 */
	private void marshallObjects() {
		try {
			objectBuilder = new ObjectBuilder(xmlFile.toURL());
			objectBuilder.unmarshal();
		} catch (MalformedURLException e) {
			logger.error("Caught MalformedURLException trying to unmarshal the XML into objects:"
					+ e.getMessage());
		} catch (Throwable t) {
			logger.error("Caught throwable trying to unmarshal the XML into objects: "
					+ t.getMessage());
		}
	}

	/**
	 * Updates the object model with pertinent information
	 */
	private void updateModel() {

		// Create a fully described Resource referring to the local XML file
		Resource resource = new Resource();
		String webPath = properties.getProperty("ruminate.url.xml");
		if (!webPath.endsWith("/")) {
			webPath = webPath + "/";
		}
		webPath = webPath + xmlFile.getName();
		try {
			resource.setUrl(new URL(webPath));
		} catch (MalformedURLException e) {
			logger.error("MalformedURLException caught trying to set the "
					+ "URL for the XML resource to " + webPath + ": "
					+ e.getMessage());
		} catch (MetadataException e) {
			logger.error("MetadataException caught trying to set URL for the "
					+ "XML resource to " + webPath + ": " + e.getMessage());
		}
		try {
			resource.setName("XML Metadata for " + this.deviceID);
			resource.setDescription("XML Metadata description for "
					+ this.deviceID + ". Revision " + this.dataDescriptionID
					+ " extracted from packet with sequence number "
					+ this.sequenceNumber + " and time "
					+ xmlDateFormat.format(packetDate));
		} catch (MetadataException e) {
			logger.error("MetadataException caught trying to set the name "
					+ "and/or description on the XML resource: "
					+ e.getMessage());
		}
		resource.setStartDate(packetDate);
		resource.setEndDate(packetDate);
		ResourceType xmlResourceType = new ResourceType();
		try {
			xmlResourceType.setName("application/xhtml+xml");
		} catch (MetadataException e) {
			logger.error("MetadataException caught trying to set the "
					+ "name of the ResourceType for the XML: " + e.getMessage());
		}
		resource.setMimeType("application/xhtml+xml");
		resource.setResourceType(xmlResourceType);
		resource.setContentLength(new Long(xmlFile.length()));
		Keyword xmlKeyword = new Keyword();
		Keyword metadataKeyword = new Keyword();
		try {
			xmlKeyword.setName("XML");
			metadataKeyword.setName("Metadata");
		} catch (MetadataException e) {
			logger.error("MetadataException caught trying to "
					+ "set the name of the keywords " + e.getMessage());
		}
		resource.addKeyword(xmlKeyword);
		resource.addKeyword(metadataKeyword);

		// Add the Resource to all DataProducers described in the XML file
		Collection objs = objectBuilder.listAll();
		Iterator iterator = objs.iterator();
		while (iterator.hasNext()) {
			IMetadataObject metadataObject = (IMetadataObject) iterator.next();
			logger.debug("Found a DataProducer at the top of the unmarshalled "
					+ "objects, will add resource to it");
			if (metadataObject instanceof DataProducer) {
				addResourceToDataProducerAndChildren(
						(DataProducer) metadataObject, resource);
			}
		}

		/*
		 * We also need to check for DataStream objects. If there are
		 * DataStreams defined in a deployment they are handled a bit
		 * differently
		 */
		Collection dataProducers = objectBuilder.listAll();
		Iterator dataProducerIterator = dataProducers.iterator();
		while (dataProducerIterator.hasNext()) {
			// First try to cast it to an DataProducer
			DataProducer currentDataProducer = null;
			Object obj = dataProducerIterator.next();
			try {
				currentDataProducer = (DataProducer) obj;
			} catch (Exception ex) {
				logger.warn("During check for DataStream objects, could not cast object "
						+ obj + " to IDeployment");
			}

			// See if the deployment tree has DataStreams
			if (doesDataProducerContainStreams(currentDataProducer)) {
				// First try to look for the most recent deployment of the
				// parent device
				DataProducer currentParentDataProducer = null;
				// Grab the parent device
				Device parentDevice = null;
				try {
					parentDevice = (Device) deviceAccessLocal.findById(
							parentID, true);
				} catch (MetadataAccessException e) {
					logger.error("MetadataAccessException caught trying to get the parent device: "
							+ e.getMessage());
				}
				DeviceType parentDeviceType = null;
				if (parentDevice != null)
					parentDeviceType = parentDevice.getDeviceType();
				Device currentDevice = null;
				try {
					currentDevice = (Device) deviceAccessLocal.findById(
							deviceID, true);
				} catch (MetadataAccessException e) {
					logger.error("MetadataAccessException caught trying to get the current device: "
							+ e.getMessage());
				}
				DeviceType currentDeviceType = null;
				if (currentDevice != null)
					currentDeviceType = currentDevice.getDeviceType();

				// First lets close out any other deployments of this device to
				// clean up things
				Collection currentDeviceDeployments = null;
				try {
					currentDeviceDeployments = dataProducerAccessLocal
							.findByDevice(currentDevice, null, null, false);
				} catch (MetadataAccessException e) {
					logger.error("MetadataAccessException caught trying to get "
							+ "the current device's deployments: "
							+ e.getMessage());
				}
				if (currentDeviceDeployments != null) {
					Iterator currentDeviceDeploymentsIterator = currentDeviceDeployments
							.iterator();
					while (currentDeviceDeploymentsIterator.hasNext()) {
						DataProducer tempDataProducer = (DataProducer) currentDeviceDeploymentsIterator
								.next();
						if (tempDataProducer.getEndDate() == null) {
							tempDataProducer.setEndDate(packetDate);
							try {
								dataProducerAccessLocal
										.update(tempDataProducer);
							} catch (MetadataAccessException e) {
								logger.error("MetadataAccessException caught trying to close out a "
										+ "current deployment by setting it's endDate "
										+ "then doing an update: "
										+ e.getMessage());
							}
						}
					}
				}

				// Grab any deployments of the parent device
				Collection parentDataProducers = null;
				try {
					parentDataProducers = dataProducerAccessLocal.findByDevice(
							parentDevice, "startDate", null, false);
				} catch (MetadataAccessException e) {
					logger.error("MetadataAccessException caught trying to find "
							+ "the parent device's deployments: "
							+ e.getMessage());
				}

				// Now grab the last one (the most recent)
				if (parentDataProducers != null) {
					Iterator parentDataProducerIterator = parentDataProducers
							.iterator();
					while (parentDataProducerIterator.hasNext()) {
						currentParentDataProducer = (DataProducer) parentDataProducerIterator
								.next();
					}
				}
				// OK, I should now have the most recent parent dataproducer. If
				// the current data producer is null, or if the most recent one
				// has an endDate, create a new one
				if ((currentParentDataProducer == null)
						|| (currentParentDataProducer.getEndDate() != null)) {
					currentParentDataProducer = new DataProducer();
					try {
						currentParentDataProducer
								.setDataProducerType(DataProducer.TYPE_DEPLOYMENT);
					} catch (MetadataException e) {
						logger.error("MetadataException caught trying to set the "
								+ "data producer type on the new parent deployment: "
								+ e.getMessage());
					}
					if (parentDeviceType != null) {
						try {
							currentParentDataProducer.setName(parentDeviceType
									.getName()
									+ " ("
									+ xmlDateFormat.format(packetDate)
									+ ") UUID=" + parentDevice.getUuid());
						} catch (MetadataException e) {
							logger.error("MetadataException caught trying to set the "
									+ "name on the new parent deployment: "
									+ e.getMessage());
						}
					} else {
						try {
							currentParentDataProducer.setName("UNKNOWN" + " ("
									+ xmlDateFormat.format(packetDate)
									+ ") UUID=" + parentDevice.getUuid());
						} catch (MetadataException e) {
							logger.error("MetadataException caught trying to set the "
									+ "name on the new parent deployment: "
									+ e.getMessage());
						}
					}
					try {
						currentParentDataProducer
								.setDescription("This deployment was created by SSDS because the device was "
										+ "a parent on a incoming deployment, but it was not deployed itself");
					} catch (MetadataException e) {
						logger.error("MetadataException caught trying to set the "
								+ "description on the new parent deployment: "
								+ e.getMessage());
					}
					currentParentDataProducer.setStartDate(packetDate);
					try {
						currentParentDataProducer
								.setRole(DataProducer.ROLE_PLATFORM);
					} catch (MetadataException e) {
						logger.error("MetadataException caught trying to set the "
								+ "role on the new parent deployment: "
								+ e.getMessage());
					}
					currentParentDataProducer.setDevice(parentDevice);
				}

				// So we now have the current parent deployment
				currentParentDataProducer
						.addChildDataProducer(currentDataProducer);

				// Now, check to see if the current deployment has a start time
				// and if it does not, set the packet time to be the starting
				// time
				if (currentDataProducer.getStartDate() == null) {
					currentDataProducer.setStartDate(packetDate);
					logger.debug("Set the current deployment's start date to the packet timestamp which is "
							+ xmlDateFormat.format(packetDate));
				}

				// Now check to see if the deployment has a name, if not create
				// one
				if ((currentDataProducer.getName() == null)
						|| (currentDataProducer.getName().equals(""))) {
					if ((currentDevice != null) && (currentDeviceType != null)) {
						try {
							currentDataProducer.setName(currentDeviceType
									.getName()
									+ " ("
									+ xmlDateFormat.format(packetDate)
									+ ") UUID=" + currentDevice.getUuid());
						} catch (MetadataException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						if (currentDevice != null) {
							try {
								currentDataProducer.setName("UNKNOWN" + " ("
										+ xmlDateFormat.format(packetDate)
										+ ") UUID=" + currentDevice.getUuid());
							} catch (MetadataException e) {
								logger.error("MetadataException caught trying to set the "
										+ "name on the new current deployment: "
										+ e.getMessage());
							}
						} else {
							try {
								currentDataProducer.setName("UNKNOWN" + " ("
										+ xmlDateFormat.format(packetDate)
										+ ") UUID=UNKNOWN");
							} catch (MetadataException e) {
								logger.error("MetadataException caught trying to set the "
										+ "name on the new current deployment: "
										+ e.getMessage());
							}
						}
					}
				}

				// If no description, create one
				if ((currentDataProducer.getDescription() == null)
						|| (currentDataProducer.getDescription().equals(""))) {
					try {
						currentDataProducer.setDescription("");
					} catch (MetadataException e) {
						logger.error("MetadataException caught trying to set the "
								+ "description on the new current deployment: "
								+ e.getMessage());
					}
				}

				// If no role, assign instrument
				if (currentDataProducer.getRole() == null) {
					try {
						currentDataProducer
								.setRole(DataProducer.ROLE_INSTRUMENT);
					} catch (MetadataException e) {
						logger.error("MetadataException caught trying to set the "
								+ "role on the new current deployment: "
								+ e.getMessage());
					}
				}

				// Loop through outputs and assign names, URLs, startDates,
				// descriptions
				Collection outputs = currentDataProducer.getOutputs();
				if (outputs != null) {
					Iterator outputIterator = outputs.iterator();
					while (outputIterator.hasNext()) {
						DataContainer tempDataContainer = (DataContainer) outputIterator
								.next();
						// Name
						if ((tempDataContainer.getName() == null)
								|| (tempDataContainer.getName().equals(""))) {
							StringBuffer dcNameBuffer = new StringBuffer();
							if ((currentDeviceType != null)
									&& (currentDeviceType.getName() != null)
									&& (!currentDeviceType.getName().equals(""))) {
								dcNameBuffer
										.append(currentDeviceType.getName());
							} else {
								dcNameBuffer.append("UNKNOWN");
							}
							dcNameBuffer.append(" ("
									+ xmlDateFormat.format(packetDate)
									+ ") DataStream from device UUID=");
							if (currentDevice != null) {
								dcNameBuffer.append(currentDevice.getUuid());
							}
						}
						// URL
						if ((tempDataContainer.getUriString() == null)
								|| (tempDataContainer.getUriString().equals(""))) {
							if (!dataStreamBaseURL.endsWith("/")) {
								dataStreamBaseURL = dataStreamBaseURL + "/";
							}
							try {
								tempDataContainer
										.setUriString(dataStreamBaseURL
												+ "?deviceID=" + deviceID
												+ "&startParentID=" + parentID
												+ "&startPacketSubType="
												+ packetSubType
												+ "&numHourOffset=24");
							} catch (MetadataException e) {
								logger.error("Could not set the URIString for the DataContainer: "
										+ e.getMessage());
							}
						}
						// Start date
						if (tempDataContainer.getStartDate() == null)
							tempDataContainer.setStartDate(packetDate);
						// Description
						if ((tempDataContainer.getDescription() == null)
								|| (tempDataContainer.getDescription()
										.equals(""))) {
							try {
								tempDataContainer
										.setDescription("This is the data stream that is being "
												+ "generated from device with UUID "
												+ currentDevice.getUuid()
												+ " that was deployed on parent with UUID "
												+ parentDevice.getUuid()
												+ " starting on date "
												+ xmlDateFormat
														.format(tempDataContainer
																.getStartDate()));
							} catch (MetadataException e) {
								logger.error("MetadataException caught trying to set the "
										+ "description of the data container: "
										+ e.getMessage());
							}
						}
					}
				}
			} else {
				// DataProducer does not contain any DataStreams, so don't do
				// anything (for now)
			}
		}
	}

	/**
	 * This method recursively adds a resource to all the DataProducers down the
	 * tree starting at the DataProducer passed in
	 * 
	 * @param dataProducer
	 * @param resource
	 */
	private void addResourceToDataProducerAndChildren(
			DataProducer dataProducer, Resource resource) {
		logger.debug("Adding " + resource.toStringRepresentation("|") + " to "
				+ dataProducer.toStringRepresentation("|"));
		dataProducer.addResource(resource);
		Collection childDataProducers = dataProducer.getChildDataProducers();
		if (childDataProducers != null) {
			Iterator iterator = childDataProducers.iterator();
			while (iterator.hasNext()) {
				addResourceToDataProducerAndChildren(
						(DataProducer) iterator.next(), resource);
			}
		}
	}

	/**
	 * This method recursively checks to see if the data producer or children
	 * have data streams as output. These are handled a bit differently in SSDS
	 * 
	 * @param dataProducer
	 * @return
	 */
	private boolean doesDataProducerContainStreams(DataProducer dataProducer) {
		logger.debug("Going to look for data streams in "
				+ dataProducer.toStringRepresentation("|"));
		Collection outputs = dataProducer.getOutputs();
		if (outputs != null) {
			Iterator outputIter = outputs.iterator();
			while (outputIter.hasNext()) {
				DataContainer dataContainer = (DataContainer) outputIter.next();
				if (dataContainer.getDataContainerType().equals(
						DataContainer.TYPE_STREAM))
					return true;
			}
		}
		logger.debug("Did not find one, will look in child data producers");
		Collection childDataProducers = dataProducer.getChildDataProducers();
		if (childDataProducers != null) {
			Iterator childIterator = childDataProducers.iterator();
			while (childIterator.hasNext()) {
				DataProducer childDataProducer = (DataProducer) childIterator
						.next();
				boolean childResult = doesDataProducerContainStreams(childDataProducer);
				if (childResult)
					return true;
			}
		}
		return false;
	}

	/**
	 * Persists the object model to a database.
	 */
	private boolean persistToDatabase() {
		boolean success = false;
		logger.debug("persistToDatabase called...");

		// Check for DeploymentGroups
		Collection all = objectBuilder.listAll();
		Iterator it = all.iterator();
		// int count = 0;
		// outputsToBuildNetCDSfrom = new ArrayList();
		while (it.hasNext()) {
			Object obj = it.next();
			try {
				if (obj instanceof DataProducer)
					dataProducerAccessLocal.insert((DataProducer) obj);
			} catch (MetadataAccessException e) {
				logger.error("MetadataAccessException caught trying to insert the whole data producer: "
						+ e.getMessage());
			}
		}

		return success;
	}

	/**
	 * Insert (actually add or update) a DataProducerGroup.
	 * 
	 * @param dpg
	 *            an IDataProducerGroup
	 * @return true is insert is successful
	 */
	// private boolean persistDataProducerGroup(IDataProducerGroup dpg) {
	// boolean success = false;
	//
	// try {
	// logger.debug("Inserting DataProducerGroup " + dpg.getName());
	// Long dpgID = dataProducerGroupAccess.insert(dpg);
	// success = true;
	// } catch (RemoteException e) {
	// logger
	// .error("A RemoteException was caught while trying to insert the new
	// DataProducerGroup object: "
	// + dpg.getName() + "\nmessage = " + e.getMessage());
	// e.printStackTrace();
	// } catch (DataAccessException e) {
	// logger
	// .error("A DataAccessException was caught while trying to insert the new
	// DataProducerGroup objects: "
	// + dpg.getName() + "\nmessage = " + e.getMessage());
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// return success;
	// }
	/**
	 * Insert (actually add or update) a deployment
	 * 
	 * @param d
	 *            an IDeployment
	 * @return true is insert is successful
	 */
	// private boolean persistDeployment(IDeployment d) {
	// boolean success = false;
	// // Now insert the new deployment
	// try {
	// Long persistedID = deploymentAccess.insert(d);
	// logger.debug("Persisted the deployment with id = " + persistedID
	// + " with role " + d.getRole());
	// success = true;
	//
	// } catch (RemoteException e3) {
	// logger
	// .error("A RemoteException was caught while trying to insert the new
	// deployment objects: "
	// + e3.getMessage());
	// } catch (DataAccessException e3) {
	// logger
	// .error("A DataAccessException was caught while trying to insert the new
	// deployment objects: "
	// + e3.getMessage());
	// }
	// return success;
	// }
	/**
	 * This method takes whatever is in the ObjectBuilder model classes,
	 * reconstitutes the XML from it and republishes that as a byte array to the
	 * next topic
	 */
	private void republishModel() {
		// Grab the XML Builder
		XmlBuilder xmlBuilder = new XmlBuilder();
		// Iterate over the object builder to get the model classes and
		// put them into XmlBuilder
		for (Iterator iter = objectBuilder.listAll().iterator(); iter.hasNext();) {
			xmlBuilder.add(iter.next());
		}
		// Now marshal out to XML
		xmlBuilder.marshal();
		try {
			// Now publish the XML as an array of bytes
			publisherComponent.publishBytes(xmlBuilder.toFormattedXML()
					.getBytes());
			logger.debug("OK, updated model published!");
		} catch (UnsupportedEncodingException e) {
			logger.error("Could not republish updated model as XML "
					+ e.getMessage());
		} catch (IOException e) {
			logger.error("Could not republish updated model as XML "
					+ e.getMessage());
		}
	}

	/**
	 * @param persistedID
	 *            The ID of the persisted Deployment
	 * @deprecated add to collection of outputsToBuildNetCDSfrom instead
	 */
	// private void addPeristedDeploymentIDs(Long persistedID) {
	// // Check to see if the deployment is a platform or an instrument, if so,
	// // add it
	// // to the list of platform deployments that were persisted
	// IDeployment d = null;
	// try {
	// d = (IDeployment) deploymentAccess.findByPK(persistedID);
	// } catch (RemoteException e) {
	// e.printStackTrace();
	// } catch (DataAccessException e) {
	// e.printStackTrace();
	// }
	// persistedDeploymentIDs = new ArrayList();
	// if (d.getRole().compareTo(IDeployment.PLATFORM_ROLE) == 0) {
	// persistedDeploymentIDs.add(persistedID);
	// logger
	// .debug("Remembering platform deployment for task firing: id = "
	// + persistedID);
	// } else {
	// logger.debug("Did not add deployment with id " + persistedID
	// + " to the array list");
	// }
	//
	// }
	/**
	 * @return
	 */
	// private boolean doesMetadataXMLExist() {
	// boolean alreadyExists = false;
	// // Open up the directory where all the XML files exist
	// File xmlPath = new File(properties.getProperty("ruminate.storage.xml"));
	// // Read in all the files that have the same device ID and parentID
	// File[] xmlFiles = xmlPath.listFiles(new DeviceParentFileFilter(
	// deviceID, parentID));
	// long[] metadataSequenceNumbers = new long[xmlFiles.length];
	// // Loop through to find the right filename
	// for (int i = 0; i < xmlFiles.length; i++) {
	// String filename = xmlFiles[i].getName();
	// logger.debug("Filename is " + xmlFiles[i].getName());
	// int firstUnderscoreIndex = filename.indexOf('_');
	// logger.debug("firstUnderscore is at " + firstUnderscoreIndex);
	// int secondUnderscoreIndex = filename.indexOf('_',
	// firstUnderscoreIndex + 1);
	// logger.debug("secondUnderscore is at " + secondUnderscoreIndex);
	// logger.debug("MetadataSequence number is "
	// + filename.substring(firstUnderscoreIndex + 1,
	// secondUnderscoreIndex));
	// metadataSequenceNumbers[i] = new Long(filename.substring(
	// firstUnderscoreIndex + 1, secondUnderscoreIndex)).longValue();
	// }
	// // Now sort them
	// Arrays.sort(metadataSequenceNumbers);
	// // Now loop through them to find the previous metadatasequence number
	// long previousNumber = metadataSequenceNumbers[0];
	// for (int i = 0; i < metadataSequenceNumbers.length; i++) {
	// if (metadataSequenceNumbers[i] >= this.dataDescriptionID) {
	// break;
	// } else {
	// previousNumber = metadataSequenceNumbers[i];
	// }
	// }
	// logger.debug("previousNumber = " + previousNumber
	// + " and dataDescriptionID is " + this.dataDescriptionID);
	// // Now, only compare the XML if the previous sequence number is less
	// // than the current on
	// if (previousNumber < this.dataDescriptionID) {
	// File previousXMLFile = new File(properties
	// .getProperty("ruminate.storage.xml")
	// + File.separator
	// + deviceID
	// + "_"
	// + previousNumber
	// + "_"
	// + parentID + ".xml");
	// logger.debug("Looking for file: " + previousXMLFile.getName());
	// if (previousXMLFile.exists()) {
	// logger.debug("Found it!");
	// FileInputStream fileInputStream = null;
	// try {
	// fileInputStream = new FileInputStream(previousXMLFile);
	// } catch (FileNotFoundException e) {}
	// String xmlFromFile = null;
	// if (fileInputStream != null) {
	// try {
	// byte[] xmlBytes = new byte[fileInputStream.available()];
	// fileInputStream.read(xmlBytes);
	// xmlFromFile = new String(xmlBytes);
	// logger.debug("xmlFromFile is " + xmlFromFile);
	// } catch (IOException e1) {
	// e1.printStackTrace();
	// }
	// try {
	// fileInputStream.close();
	// } catch (IOException e2) {
	// e2.printStackTrace();
	// }
	// // Now compare
	// if (xmlFromFile != null) {
	// logger.debug("Comparing current xml \n"
	// + metadataString + "\n\n\nwith xml from file \n\n "
	// + xmlFromFile);
	// if (metadataString.equals(xmlFromFile.toLowerCase())) {
	// alreadyExists = true;
	// logger.debug("They are equal!");
	// }
	// }
	// }
	// }
	// }
	// // Now find the next lowest metadata revision numbered file
	// return alreadyExists;
	// }
	/**
	 * Start the processing tasks given a Collection of DataContainers.
	 * 
	 * @see moos.ssds.tasks.Task
	 */
	// private void triggerTasksWithOutputsCollection() {
	//
	// logger.debug("triggerTasksWithOutputsCollection called");
	// MultiGenerateNetcdfTask task = null;
	//
	// task = new MultiGenerateNetcdfTask();
	// task.setInputs(outputsToBuildNetCDSfrom);
	//
	// logger
	// .debug("Done with triggerTasksWithOutputsCollection for these outputs:");
	//
	// NotificationUpdateTask finishUpTask = new NotificationUpdateTask();
	//
	// task.setSecondTask(finishUpTask);
	// task.start();
	// }
	/**
	 * Send email notification for the deployent
	 */
	// private void sendNotification(IDeployment deployment) {
	// DeploymentNotificationTask email = new DeploymentNotificationTask();
	// logger.debug("sendNotification(): Sending notification for Deployment "
	// + deployment.getName());
	// email.setDeployment(deployment);
	// email.start();
	// }
	// *********** LOCAL VARIABLES ********************//

}