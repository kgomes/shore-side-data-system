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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import moos.ssds.dao.util.MetadataAccessException;
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
 * This class is a MessageDrivenBean (MDB) and is responsible for taking in a
 * packet and parsing out any metadata that is applicable. If processes the
 * metadata into SSDS and then republishes the updated model in XML format
 * </p>
 * <p>
 * When ruminate processes an incoming message, the following steps occur:
 * <ol>
 * <li>Extracts the XML metadata from the payload</li>
 * <li>Checks to see if this metadata has arrived before for the
 * instrument-parent combination (just compares the text blobs)</li>
 * <li>If it looks to be new, it keeps going, otherwise it stops processing</li>
 * <li>Marshalls the XML into SSDS Metadata Objects</li>
 * <li>Creates a new <code>Resource</code> that represents the XML file that was
 * just saved.
 * <ul>
 * <li>Sets the start and end date on the new Resource to match the timestamp of
 * the packet that the metadata arrived in
 * <li>
 * <li>Creates a new <code>ResourceType</code> object named
 * &quot;application/xhtml+xml&quot; and sets it to the new
 * <code>Resource</code>'s resource type</li>
 * <li>Sets the new <code>Resource</code>'s mime type to
 * &quot;application/xhtml+xml&quot;</li>
 * <li>Reads the length of the new XML file and sets the contentLength to that
 * size</li>
 * </ul>
 * </li>
 * <li>Creates a new <code>Keyword</code> object named &quot;XML&quot; and adds
 * it to the new <code>Resource</code></li>
 * <li>Creates a new <code>Keyword</code> object named &quot;Metadata&quot; and
 * adds it to the new <code>Resource</code></li>
 * <li>Rescursively adds the new <code>Resource</code> to the
 * <code>DataProducer</code> and any child <code>DataProducer</code>s as well.</li>
 * <li>The <code>DataProducer</code> is then examined to see if there are any
 * outputs (from children also) that are <code>DataContainer</code>s of type
 * DATA_STREAM. If that is the case then some special processing happens as
 * follows:
 * <ul>
 * <li>Ruminate searches SSDS for all <code>DataProducer</code>s that are
 * associated with the <code>Device</code> that is associated with the data
 * streams found here.</li>
 * <li>If the top level <code>DataProducer</code> does not have a name, ruminate
 * will do it's best to create some name that makes sense</li>
 * <li>If the top level <code>DataProducer</code> does not have a description, a
 * &quot;No description&quot; is added</li>
 * <li>If the top level <code>DataProducer</code> does not have a role assigned
 * to it, since it is a <code>DataProducer</code> with a stream, the role of
 * ROLE_INSTRUMENT is set</li>
 * <li>If the top level <code>DataProducer</code> does not have a start date,
 * the date of the packet the metadata arrived in is used as the start date</li>
 * <li>If the top level <code>DataProducer</code> does not have a
 * &quot;Type&quot; set, one of TYPE_DEPLOYMENT will be set since this is a
 * <code>DataProducer</code> with a stream of data</li>
 * <li>The output of the <code>DataProducer</code> is examined and if it has no
 * name, one is created from the date and <code>Device</code> UUID.</li>
 * <li>The output of the <code>DataProducer</code> is examined and if the URL is
 * empty, one is constructed using the <code>GetOriginalDataServlet</code> so
 * that the URL will return the last 10 packets</li>
 * <li>The output of the <code>DataProducer</code> is examined and if there is
 * no start date, the date of the packet in which the metadata arrived is used
 * to set the start date of the output</li>
 * <li>The output of the <code>DataProducer</code> is examine and if there is no
 * description, one is generated that talks about which instrument this stream
 * comes from</li>
 * </ul>
 * <li>The above set of steps is then repeated recursively on all the child
 * <code>DataProducer</code>s.</li>
 * </li>
 * <li>If the incoming metadata packet had a parent ID specified, ruminate will
 * attempt to add the newly created <code>DataProducer</code> to the most recent
 * parent <code>DataProducer</code> associated with the parent device using the
 * following steps:
 * <ul>
 * <li>Query for all <code>DataProducer</code>s that are associated with the
 * parent <code>Device</code> sorting by start dates</li>
 * <li>Grab the <code>DataProducer</code> with the most recent start date</li>
 * <li>If there are no parent <code>DataProducer</code>s or the most recent one
 * has an end date already, create new parent <code>DataProducer</code></li>
 * <li>Add the newly created <code>DataProducer</code> to the most recent parent
 * <code>DataProducer</code> or the newly created <code>DataProducer</code></li>
 * </ul>
 * <li>Lastly, if the newly created <code>DataProducer</code> does not have a
 * start date, use the date from the metadata packet that was received as the
 * start date of the <code>DataProducer</code>.</li>
 * </ol>
 * </p>
 * <hr/>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.5.2.17 $
 * 
 *          From here down is XDoclet Stuff for generating deployment
 *          configuration files
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "topic/SSDSRuminateTopic"),
		@ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "NonDurable") })
public class RuminateMDB implements MessageListener {

	/**
	 * The ruminate properties for this instance
	 */
	private static Properties properties = new Properties();

	/**
	 * A boolean to indicate if ruminate is connected to the services of SSDS.
	 */
	private boolean connected = false;

	/**
	 * The SSDS Services
	 */
	// @javax.annotation.Resource(mappedName =
	// "moos/ssds/services/metadata/DataProducerGroupAccessLocal")
	// private DataProducerGroupAccessLocal dataProducerGroupAccessLocal;
	@javax.annotation.Resource(mappedName = "moos/ssds/services/metadata/DataProducerAccessLocal")
	private DataProducerAccessLocal dataProducerAccessLocal;
	@javax.annotation.Resource(mappedName = "moos/ssds/services/metadata/DeviceAccessLocal")
	private DeviceAccessLocal deviceAccessLocal;
	// @javax.annotation.Resource(mappedName =
	// "moos/ssds/services/metadata/DataContainerAccessLocal")
	// private DataContainerAccessLocal dataContainerAccessLocal;

	/**
	 * This is the base URL of the data stream access
	 */
	private String dataStreamBaseURL = null;

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

	/**
	 * This is the MessageDrivenContext that is from the container
	 */
	// private javax.ejb.MessageDrivenContext ctx;

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(RuminateMDB.class);

	// Some helpers
	private Date packetDate = null;

	// A data formatter
	private static XmlDateFormat xmlDateFormat = new XmlDateFormat();

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
	 * A static counter that can be used to make sure that incoming deployment
	 * names are unique
	 */
	private static int counter = 0;

	/**
	 * This is the name of the topic that the packets will be republished to
	 */
	private String republishTopicName = null;

	/**
	 * This is a boolean to indicated if the publishing is setup and working
	 * correctly
	 */
	private boolean publishingSetup = false;

	/**
	 * The ConnectionFactory that will be injected by the container
	 */
	@javax.annotation.Resource(mappedName = "ConnectionFactory")
	private ConnectionFactory connectionFactory;

	/**
	 * This is the JMS destination that will be published to and will be
	 * injected by the container
	 */
	@javax.annotation.Resource(mappedName = "topic/SSDSRuminateRepublishTopic")
	private Destination destination;

	/**
	 * This is the connection to the topic that the messages will be published
	 * to
	 */
	private Connection connection = null;

	/**
	 * This is a session that the publishing of messages will be run in.
	 */
	private Session session = null;

	/**
	 * This is the message producer that is actually used to send messages to
	 * the
	 */
	private MessageProducer messageProducer = null;

	/**
	 * This method sets up the internal properties and message producer
	 * utilizing resources from the container
	 */
	@PostConstruct
	public void setup() {
		logger.debug("setup called.");
		// Read in the ruminate properties file
		try {
			properties.load(this.getClass().getResourceAsStream(
					"/moos/ssds/ruminate/ruminate.properties"));
			logger.debug("ruminate.properties should have read in.");
		} catch (IOException e) {
			logger.error("IOException caught trying to read "
					+ "ruminate.properties in setup of RuminateMDB: "
					+ e.getMessage());
			e.printStackTrace();
		}

		// Set the base URL for data stream
		this.dataStreamBaseURL = properties
				.getProperty("ruminate.ssds.datastream.servlet.base.url");
		logger.debug("Base data stream URL will be " + this.dataStreamBaseURL);

		// Make sure the connection factory is there
		if (connectionFactory != null) {
			// Create a connection
			try {
				connection = connectionFactory.createConnection();
			} catch (JMSException e) {
				logger.error("JMSException caught trying to create "
						+ "the connection from the "
						+ "injected connection factory: " + e.getMessage());
			}
			if (connection != null) {
				// Create a session
				try {
					session = connection.createSession(false,
							Session.AUTO_ACKNOWLEDGE);
				} catch (JMSException e) {
					logger.error("JMSException caught trying to create "
							+ "the session from the connection:"
							+ e.getMessage());
				}
				if (session != null) {
					if (destination != null) {
						// Create a message producer
						try {
							messageProducer = session
									.createProducer(destination);
						} catch (JMSException e) {
							logger.error("JMSException caught trying "
									+ "to create the producer from the "
									+ "session: " + e.getMessage());
						}
						if (messageProducer == null)
							logger.error("Was not able to "
									+ "create a MessageProducer");
					} else {
						logger.error("The destination that was to be "
								+ "injected by the container "
								+ "appears to be null");
					}
				} else {
					logger.error("Could not seem to create a "
							+ "session from the connection");
				}
			} else {
				logger.error("Could not seem to create a connection "
						+ "using the injected connection factory");
			}
		} else {
			logger.error("ConnectionFactory is NULL and should "
					+ "have been injected by the container!");
		}

	}

	/**
	 * This method stops all the JMS components
	 */
	@PreDestroy
	public void tearDownPublishing() {
		try {
			// Now close the connection
			if (connection != null) {
				connection.close();
			}
		} catch (JMSException e) {
			logger.error("JMSException caught trying to close the connection: "
					+ e.getMessage());
		}
	}

	/**
	 * This is the callback method that the container calls when a message is
	 * received on the topic that this bean is subscribed to.
	 * 
	 * @param msg
	 *            Is the message object that the topic recieved.
	 */
	public void onMessage(Message message) {
		logger.debug("Got a message at " + (new Date()).toString());
		if (message instanceof BytesMessage) {
			BytesMessage bytesMessage = (BytesMessage) message;

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
				logger.debug("->dataDescriptionVersion="
						+ dataDescriptionVersion);
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

			if (connected) {
				// Processing steps
				logger.debug("Saving the XML to a file: ");
				boolean ok = saveToFile();
				if (ok) {
					// Check to see if the incoming constitutes new metdata
					newMetadata = XMLMetadataTracker.checkIfNewMetadata(
							deviceID, parentID, dataDescriptionID, xmlFile);
					logger.debug("XML file new metadata? " + newMetadata);

					boolean ptdbOK = true;
					if (newMetadata) {
						logger.debug("OK, now marshall XML to objects");
						marshallObjects();

						logger.debug("Marshalled. Now update model with XML files");
						updateModel();
						logger.debug("Was able to run the object builder and update the model");

						logger.debug("Updated. Now will persist.");
						ptdbOK = persistToDatabase();
						logger.debug("Reply from persist to DB as to success = "
								+ ptdbOK);
					}

					if (ptdbOK) {
						// First re-publish data to next topic for other clients
						// to use
						logger.debug("Going to republish the updated model for downstream processing");
						republishModel();
					} else {
						logger.error("Failed to persist to database.");
					}
				}
				logger.debug("Done with process method");
			} else {
				logger.debug("Could not connect to SSDS, ruminate will not ruminate");
			}
		}
	}

	/**
	 * Saves the xml to an external file. The file location is specified by the
	 * property 'ruminate.storage.xml' in the file 'ruminate.properties'.
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
				// If the directory does not exist, create it
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
		} else {
			xmlFile = null;
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
		logger.debug("Updating the model...");
		Resource resource = new Resource();
		String webPath = properties.getProperty("ruminate.url.xml");
		if (!webPath.endsWith("/")) {
			webPath = webPath + "/";
		}
		webPath = webPath + xmlFile.getName();
		logger.debug("webPath = " + webPath);

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
		logger.debug("The resource to be added = "
				+ resource.toStringRepresentation("|"));
		logger.debug("A resourceType = "
				+ xmlResourceType.toStringRepresentation("|"));
		logger.debug("xmlKeyword = " + xmlKeyword.toStringRepresentation("|"));
		logger.debug("metadataKeyword = "
				+ metadataKeyword.toStringRepresentation("|"));

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

		// Grab the parent device from the ID that came in on the packet
		Device parentDeviceFromPacket = null;
		// Also create a place holder for the current deployment of that parent
		// device
		DataProducer currentParentDataProducer = null;

		// Grab a local interface
		try {
			parentDeviceFromPacket = (Device) deviceAccessLocal.findById(
					parentID, true);
		} catch (MetadataAccessException e2) {
			logger.error("CreateException caught trying to get the parent "
					+ "Device from the parent ID on the packet:"
					+ e2.getMessage());
		}

		// See if the parent was not found
		if (parentDeviceFromPacket != null) {
			logger.debug("ParentDevice = "
					+ parentDeviceFromPacket.toStringRepresentation("|"));

		} else {
			logger.error("parentDeviceFromPacket (from id " + parentID
					+ ") was not found");
		}

		// If the parent was found, grab its device type
		DeviceType parentDeviceTypeFromPacket = null;
		if (parentDeviceFromPacket != null) {
			parentDeviceTypeFromPacket = parentDeviceFromPacket.getDeviceType();
			if (parentDeviceTypeFromPacket != null)
				logger.debug("parentDeviceTypeFromPacket = "
						+ parentDeviceTypeFromPacket
								.toStringRepresentation("|"));
		}

		// Now find the device that was associated with the ID that was
		// on the packet
		Device deviceFromPacketID = null;
		try {
			deviceFromPacketID = (Device) deviceAccessLocal.findById(deviceID,
					true);
		} catch (MetadataAccessException e2) {
			logger.error("MetadataAccessException caught trying to get the device "
					+ "from the device ID on the packet:" + e2.getMessage());
		}

		// Check to see if it was found
		if (deviceFromPacketID != null) {
			logger.debug("deviceFromPacketID = "
					+ deviceFromPacketID.toStringRepresentation("|"));
		} else {
			logger.error("deviceFromPacketID (from ID " + deviceID
					+ ") was not found");
		}

		// If it was found grab its type
		DeviceType deviceTypeFromPacketID = null;
		if (deviceFromPacketID != null) {
			deviceTypeFromPacketID = deviceFromPacketID.getDeviceType();
			if (deviceTypeFromPacketID != null) {
				logger.debug("deviceTypeFromPacketID = "
						+ deviceTypeFromPacketID.toStringRepresentation("|"));
			}
		}

		/*
		 * We also need to check for DataStream objects. If there are
		 * DataStreams defined in a deployment there is some special steps they
		 * must go through
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

			// As a first check it would be nice to know if the dataproducer has
			// the same device as that which came in on the ID
			if ((currentDataProducer != null)
					&& (currentDataProducer.getDevice() != null)
					&& (currentDataProducer.getDevice().getId() != null)
					&& (deviceID != currentDataProducer.getDevice().getId()
							.longValue())) {
				logger.error("The incoming packet had a device ID of "
						+ deviceID
						+ ", but in the XML(Metadata) within the packet, "
						+ "the device shows up as having and ID of "
						+ currentDataProducer.getDevice().getId()
						+ ". This is inconsistent!!!");
			}

			// See if the deployment tree has DataStreams
			if (doesDataProducerContainStreams(currentDataProducer)) {

				logger.debug("DataProducer has streams");

				// Call the method to recursively walk the data producer tree
				// and setup everything appropriately
				deepUpdateDeploymentInformation(currentDataProducer);

				// Now let's link up that to the most recent parent data
				// producer. First grab any deployments of the parent device
				Collection parentDataProducers = null;
				try {
					parentDataProducers = null;
					parentDataProducers = dataProducerAccessLocal.findByDevice(
							parentDeviceFromPacket, "startDate", "desc", false);
				} catch (MetadataAccessException e1) {
					logger.error("MetadataAccessException caught trying to get "
							+ "deployments of the parent device: "
							+ e1.getMessage());
				}

				// Now grab the first one as it should be the most recent
				if (parentDataProducers != null) {
					logger.debug("There were "
							+ parentDataProducers.size()
							+ " parent deployments, so I will grab the first one");
					Iterator parentDataProducerIterator = parentDataProducers
							.iterator();
					if (parentDataProducerIterator.hasNext()) {
						currentParentDataProducer = (DataProducer) parentDataProducerIterator
								.next();
						logger.debug("currentParentDataProducer is "
								+ currentParentDataProducer
										.toStringRepresentation("|"));
					}
					if (currentParentDataProducer != null) {
						// To make sure we work OK, let's get the full object
						// graph.
						logger.debug("To make sure we are working with a full object graph, grab it");
						try {
							currentParentDataProducer = (DataProducer) dataProducerAccessLocal
									.getMetadataObjectGraph(currentParentDataProducer);
							if (currentParentDataProducer == null) {
								logger.debug("No full object graph was found");
							} else {
								logger.debug("OK, full graph is "
										+ currentParentDataProducer
												.toStringRepresentation("|"));
							}
						} catch (MetadataAccessException e1) {
							logger.error("MetadataAccessException trying to get full object graph "
									+ "for the current parent data producer: "
									+ e1.getMessage());
						}
					}
				}

				// OK, I should now have the most recent parent dataproducer. If
				// the current data producer is null, or if the most recent one
				// has an endDate, create a new one
				if ((currentParentDataProducer == null)
						|| (currentParentDataProducer.getEndDate() != null)) {
					logger.debug("OK there is no (or no open) current "
							+ "parent device deployment, create a new one");
					currentParentDataProducer = new DataProducer();
					try {
						currentParentDataProducer
								.setDataProducerType(DataProducer.TYPE_DEPLOYMENT);
					} catch (MetadataException e) {
						logger.error("MetadataException caught trying to set the "
								+ "data producer type on the new parent deployment: "
								+ e.getMessage());
					}
					// If the parent device type is not null, use that in the
					// name
					if (parentDeviceTypeFromPacket != null) {
						try {
							currentParentDataProducer
									.setName(parentDeviceTypeFromPacket
											.getName()
											+ " ("
											+ xmlDateFormat.format(packetDate)
											+ " - "
											+ getNextSuffixCounter()
											+ ") UUID="
											+ parentDeviceFromPacket.getUuid());
						} catch (MetadataException e) {
							logger.error("MetadataException caught trying to set the "
									+ "name on the new parent deployment: "
									+ e.getMessage());
						}
					} else {
						if (parentDeviceFromPacket != null) {
							try {
								currentParentDataProducer.setName("UNKNOWN"
										+ " ("
										+ xmlDateFormat.format(packetDate)
										+ " - " + getNextSuffixCounter()
										+ ") UUID="
										+ parentDeviceFromPacket.getUuid());
							} catch (MetadataException e) {
								logger.error("MetadataException caught trying to set the "
										+ "name on the new parent deployment: "
										+ e.getMessage());
							}
						} else {
							try {
								currentParentDataProducer.setName("UNKNOWN"
										+ " ("
										+ xmlDateFormat.format(packetDate)
										+ " - " + getNextSuffixCounter()
										+ ") UUID=UNKNOWN");
							} catch (MetadataException e) {
								logger.error("MetadataException caught trying to set the "
										+ "name on the new parent deployment: "
										+ e.getMessage());
							}
						}
					}
					// Create a description
					try {
						currentParentDataProducer
								.setDescription("This deployment was created by SSDS because the device was "
										+ "a parent on a incoming deployment, but it was not deployed itself");
					} catch (MetadataException e) {
						logger.error("MetadataException caught trying to set the "
								+ "description on the new parent deployment: "
								+ e.getMessage());
					}
					// Set the start date of the parent deployment
					currentParentDataProducer.setStartDate(packetDate);
					try {
						currentParentDataProducer
								.setRole(DataProducer.ROLE_PLATFORM);
					} catch (MetadataException e) {
						logger.error("MetadataException caught trying to set the "
								+ "role on the new parent deployment: "
								+ e.getMessage());
					}
					// Now assign the parent device
					currentParentDataProducer.setDevice(parentDeviceFromPacket);
				}

				// So we now have the current parent deployment
				logger.debug("right before I add a child deployment, "
						+ "the currentParentDataProducer is "
						+ currentParentDataProducer.toStringRepresentation("|"));
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
			} else {
				// DataProducer does not contain any DataStreams, so don't do
				// anything (for now)
				logger.debug("DataProducer does not contain any streams");
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
		if (dataProducer == null)
			return false;
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
	 * This method walks a <code>DataProducer</code> tree and fills out the
	 * appropriate metadata based on the context of the packet recieved. It also
	 * looks at the current metadata in SSDS and closes up other metadata based
	 * on the new information
	 * 
	 * @param dataProducer
	 */
	private void deepUpdateDeploymentInformation(DataProducer dataProducer) {
		// First thing to do is grab the equivalent device from the system
		Device currentDevice = null;
		DeviceAccessLocal deviceAccessLocal = null;
		if (dataProducer.getDevice() != null) {
			try {
				currentDevice = (Device) deviceAccessLocal
						.findEquivalentPersistentObject(
								dataProducer.getDevice(), true);
			} catch (MetadataAccessException e1) {
				logger.error("MetadataAccessException trying to get the equivalent device from SSDS: "
						+ e1.getMessage());
			}
		} else {
			logger.debug("The incoming dataProducer ("
					+ dataProducer.toStringRepresentation("|")
					+ ") did not have a device associated with it");
		}

		// Now try to all the deployment of the device (if there is a device)
		Collection currentDeviceDeployments = null;
		if (currentDevice != null) {
			try {
				currentDeviceDeployments = dataProducerAccessLocal
						.findByDevice(currentDevice, null, null, false);
			} catch (MetadataAccessException e2) {
				logger.error("CreateException trying to get the "
						+ "dataProducers for the current Device:"
						+ e2.getMessage());
			}
		}
		// If there are some deployments, loop through them and make sure they
		// are closed
		if (currentDeviceDeployments != null) {
			Iterator currentDeviceDeploymentsIterator = currentDeviceDeployments
					.iterator();
			while (currentDeviceDeploymentsIterator.hasNext()) {
				DataProducer tempDataProducer = (DataProducer) currentDeviceDeploymentsIterator
						.next();
				logger.debug("Checking to see if the currentDeployment of "
						+ tempDataProducer.toStringRepresentation("|")
						+ " needs closing");
				if (tempDataProducer.getEndDate() == null) {
					if (dataProducer.getStartDate() != null) {
						logger.debug("Yep, will assign the end date to the start "
								+ "date of the incoming deployment ("
								+ xmlDateFormat.format(dataProducer
										.getStartDate()));
						tempDataProducer
								.setEndDate(dataProducer.getStartDate());
					} else {
						logger.debug("Yep, will update with end date of "
								+ xmlDateFormat.format(packetDate));
						tempDataProducer.setEndDate(packetDate);
					}
					// Now update the changed deployment
					try {
						dataProducerAccessLocal.update(tempDataProducer);
					} catch (MetadataAccessException e) {
						logger.error("MetadataAccessException trying to update a device "
								+ "deployment after setting it's end date"
								+ e.getMessage());
					}
				} else {
					logger.debug("Nope, it already has an end date");
				}
			}
		} else {
			logger.debug("currentDeviceDeployments was null");
		}

		// Now check to see if the deployment has a name, if not create
		// one. If it does, add a unique suffix to make sure it will not clash
		// with others
		if ((dataProducer.getName() == null)
				|| (dataProducer.getName().equals(""))) {
			// If there is a associated device, using information from that
			// device
			if ((currentDevice != null)
					&& (currentDevice.getDeviceType() != null)) {
				try {
					dataProducer.setName(currentDevice.getDeviceType()
							.getName()
							+ " ("
							+ xmlDateFormat.format(packetDate)
							+ " - "
							+ getNextSuffixCounter()
							+ ") UUID="
							+ currentDevice.getUuid());
					logger.debug("Found device and device type so set "
							+ "dataProducer's name to "
							+ dataProducer.getName());
				} catch (MetadataException e) {
					logger.error("MetadataException caught trying to set the name: "
							+ e.getMessage());
				}
			} else {
				// Since no device type was available, see if the device is at
				// least available
				if (currentDevice != null) {
					try {
						dataProducer.setName("UNKNOWN" + " ("
								+ xmlDateFormat.format(packetDate) + " - "
								+ getNextSuffixCounter() + ") UUID="
								+ currentDevice.getUuid());
						logger.debug("Found device, but no device type, so set name to "
								+ dataProducer.getName());
					} catch (MetadataException e) {
						logger.error("MetadataException caught trying to set the "
								+ "name on the new current deployment: "
								+ e.getMessage());
					}
				} else {
					// Since we don't have much information, do the best we can
					try {
						dataProducer.setName("UNKNOWN" + " ("
								+ xmlDateFormat.format(packetDate) + " - "
								+ getNextSuffixCounter() + ") UUID=UNKNOWN");
						logger.debug("Did not know much, so set name to "
								+ dataProducer.getName());
					} catch (MetadataException e) {
						logger.error("MetadataException caught trying to set the "
								+ "name on the new current deployment: "
								+ e.getMessage());
					}
				}
			}
		} else {
			// The dataProducer has a name, but I will add a unique suffix so
			// that it does not clash with others (a problem we have had many
			// times in the past)
			String newName = dataProducer.getName() + " (" + getUniqueSuffix()
					+ ")";
			try {
				dataProducer.setName(newName);
			} catch (MetadataException e) {
				logger.error("MetadataException caught trying to append a "
						+ "unique suffix to the deployment name : "
						+ e.getMessage());
			}
		}

		// If no description, create one
		if ((dataProducer.getDescription() == null)
				|| (dataProducer.getDescription().equals(""))) {
			try {
				dataProducer.setDescription("No Description");
			} catch (MetadataException e) {
				logger.error("MetadataException caught trying to set the "
						+ "description on the new current deployment: "
						+ e.getMessage());
			}
		}

		// If no role, assign to instrument
		if ((dataProducer.getRole() == null)
				|| (dataProducer.getRole().equals(""))) {
			try {
				dataProducer.setRole(DataProducer.ROLE_INSTRUMENT);
				logger.debug("Set the role on the currentDataProducer to "
						+ dataProducer.getRole());
			} catch (MetadataException e) {
				logger.error("MetadataException caught trying to set the "
						+ "role on the new current deployment: "
						+ e.getMessage());
			}
		}

		// If start date not defined
		if (dataProducer.getStartDate() == null) {
			dataProducer.setStartDate(packetDate);
		}

		// If not type defined, set to deployment
		if ((dataProducer.getDataProducerType() == null)
				|| (dataProducer.getDataProducerType().equals(""))) {
			try {
				dataProducer.setDataProducerType(DataProducer.TYPE_DEPLOYMENT);
			} catch (MetadataException e) {
				logger.error("MetadataException caught trying to "
						+ "set the dataProducer type to deployment:"
						+ e.getMessage());
			}
		}

		// Loop through outputs and assign names, URLs, startDates,
		// descriptions
		Collection outputs = dataProducer.getOutputs();
		if (outputs != null) {
			Iterator outputIterator = outputs.iterator();
			while (outputIterator.hasNext()) {
				DataContainer tempDataContainer = (DataContainer) outputIterator
						.next();
				logger.debug("Going to examine DataContainer "
						+ tempDataContainer.toStringRepresentation("|"));
				// Name
				if ((tempDataContainer.getName() == null)
						|| (tempDataContainer.getName().equals(""))) {
					StringBuffer dcNameBuffer = new StringBuffer();
					if ((currentDevice != null)
							&& (currentDevice.getDeviceType() != null)
							&& (currentDevice.getDeviceType().getName() != null)
							&& (!currentDevice.getDeviceType().getName()
									.equals(""))) {
						dcNameBuffer.append(currentDevice.getDeviceType()
								.getName());
					} else {
						dcNameBuffer.append("UNKNOWN");
					}
					dcNameBuffer.append(" (" + xmlDateFormat.format(packetDate)
							+ ") DataStream from device UUID=");
					if (currentDevice != null) {
						dcNameBuffer.append(currentDevice.getUuid());
					}
					// Now assign the name
					try {
						tempDataContainer.setName(dcNameBuffer.toString());
					} catch (MetadataException e) {
						logger.error("MetadataException caught trying to set "
								+ "the name of the DataContainer: "
								+ e.getMessage());
					}
				}
				// URL
				if ((tempDataContainer.getUriString() == null)
						|| (tempDataContainer.getUriString().equals(""))) {
					try {
						StringBuffer uriStringBuffer = new StringBuffer();
						uriStringBuffer.append(dataStreamBaseURL + "?deviceID="
								+ deviceID + "&startTimestampSeconds="
								+ (packetDate.getTime() / 1000));
						if (tempDataContainer.getRecordDescription()
								.getRecordType() != null)
							uriStringBuffer.append("&startPacketSubType="
									+ tempDataContainer.getRecordDescription()
											.getRecordType().toString());
						uriStringBuffer.append("&lastNumberOfPackets=10&isi=1");
						tempDataContainer.setUriString(uriStringBuffer
								.toString());
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
						|| (tempDataContainer.getDescription().equals(""))) {
					try {
						tempDataContainer
								.setDescription("This is the data stream that is being "
										+ "generated from device with UUID "
										+ currentDevice.getUuid()
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
				logger.debug("After examination DataContainer = "
						+ tempDataContainer.toStringRepresentation("|"));
			}
		}

		// Now recursively call this method on child dataProducers
		Collection childDataProducers = dataProducer.getChildDataProducers();
		if ((childDataProducers != null) && (childDataProducers.size() > 0)) {
			Iterator childIter = childDataProducers.iterator();
			while (childIter.hasNext()) {
				deepUpdateDeploymentInformation((DataProducer) childIter.next());
			}
		}
	}

	/**
	 * Persists the object model to a database.
	 */
	private boolean persistToDatabase() {
		boolean success = false;
		logger.debug("persistToDatabase called...");

		// Check for DataProducers
		Collection all = objectBuilder.listAll();
		Iterator it = all.iterator();
		// int count = 0;
		// outputsToBuildNetCDSfrom = new ArrayList();
		while (it.hasNext()) {
			Object obj = it.next();
			try {
				// OK, this is a bit of a hack, but the way I had things,
				// RuminateMDB will create a *new* parent deployment if none
				// exists for the parent ID associated with the device. This is
				// fine, but right here I use to simply just try to persist the
				// device's data producer, not the parent one. If I pulled the
				// parent DataProducer from the database, it was already in the
				// session, so no problem, but if the parent DataProducer truly
				// is new, it was a transient object which then would cause the
				// DataProducerDAO.makePersistent to fail. So what I do here is
				// to check to see if there is a parent DataProducer and if so,
				// simply persist that instead.
				if (obj instanceof DataProducer) {
					// Cast into a DataProducer
					DataProducer deviceDataProducer = (DataProducer) obj;
					// Now check for parent
					if (deviceDataProducer.getParentDataProducer() != null) {
						dataProducerAccessLocal
								.makePersistent(deviceDataProducer
										.getParentDataProducer());
					} else {
						dataProducerAccessLocal
								.makePersistent((DataProducer) obj);
					}
				}
				success = true;
			} catch (MetadataAccessException e) {
				logger.error("MetadataAccessException caught trying to "
						+ "persist the DataProducer to SSDS: " + e.getMessage());
			} catch (Throwable e) {
				logger.error("Throwable caught trying to "
						+ "persist the DataProducer to SSDS: " + e.getMessage());
				e.printStackTrace();
			}
			// If the persist fails, wait a sec, then try to submit it again
			if (!success) {
				logger.error("Something went wrong in the attempt to persist the "
						+ "data producer to SSDS, will wait and try again");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					logger.error("Could not sleep the thread: "
							+ e.getMessage());
				}
				try {
					if (obj instanceof DataProducer)
						dataProducerAccessLocal
								.makePersistent((DataProducer) obj);
					success = true;
				} catch (MetadataAccessException e) {
					logger.error("MetadataAccessException caught trying to "
							+ "persist the DataProducer to SSDS: "
							+ e.getMessage());
				} catch (Throwable e) {
					logger.error("Throwable caught trying to "
							+ "persist the DataProducer to SSDS: "
							+ e.getMessage());
					e.printStackTrace();
				}
				logger.debug("Success after second try is " + success);
			} else {
				logger.debug("Looks like the persist happened OK");
			}
		}
		return success;
	}

	protected static String getUniqueSuffix() {
		return xmlDateFormat.format(new Date()) + " - "
				+ getNextSuffixCounter();
	}

	protected static int getNextSuffixCounter() {
		counter++;
		return counter;
	}

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
		if ((objectBuilder != null) && (objectBuilder.listAll() != null)
				&& (objectBuilder.listAll().size() > 0)) {
			for (Iterator iter = objectBuilder.listAll().iterator(); iter
					.hasNext();) {

				// Grab the next object
				Object nextObject = iter.next();

				// If it a DataProducer, grab the one from SSDS
				if (nextObject instanceof DataProducer) {
					try {
						Object persistentObject = dataProducerAccessLocal
								.findById(((DataProducer) nextObject).getId(),
										true);
						if (persistentObject != null)
							nextObject = persistentObject;
					} catch (MetadataAccessException e) {
						logger.error("MetadataAccessException caught trying to "
								+ "retrieve DataProducer from SSDS: "
								+ e.getMessage());
					} catch (Throwable e) {
						logger.error("Throwable caught trying to "
								+ "retrieve DataProducer from SSDS: "
								+ e.getMessage());
						e.printStackTrace();
					}
				}

				// Now grab the one from SSDS to get full model
				xmlBuilder.add(nextObject);
			}
			// Now marshal out to XML
			xmlBuilder.marshal();

			// Create a TextMessage
			TextMessage textMessage = null;
			try {
				textMessage = session.createTextMessage(xmlBuilder
						.toFormattedXML());
			} catch (UnsupportedEncodingException e) {
				logger.error("UnsupportedEncodingException caught trying to build text message: "
						+ e.getMessage());
			} catch (JMSException e) {
				logger.error("JMSException caught trying to build text message: "
						+ e.getMessage());
			} catch (IOException e) {
				logger.error("IOException caught trying to build text message: "
						+ e.getMessage());
			}

			if (textMessage != null) {
				try {
					messageProducer.send(textMessage);
				} catch (JMSException e) {
					logger.error("JMSException caught trying to publish text message");
				}
			}
		}
	}

} // End RuminateMDB
