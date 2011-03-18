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
package moos.ssds.metadata.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import moos.ssds.io.util.Base64;
import moos.ssds.metadata.CommentTag;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataContainerGroup;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.DataProducerGroup;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.DeviceType;
import moos.ssds.metadata.Event;
import moos.ssds.metadata.HeaderDescription;
import moos.ssds.metadata.Keyword;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.RecordDescription;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.ResourceBLOB;
import moos.ssds.metadata.ResourceType;
import moos.ssds.metadata.Software;
import moos.ssds.metadata.StandardDomain;
import moos.ssds.metadata.StandardKeyword;
import moos.ssds.metadata.StandardReferenceScale;
import moos.ssds.metadata.StandardUnit;
import moos.ssds.metadata.StandardVariable;
import moos.ssds.metadata.UserGroup;
import moos.ssds.util.XmlDateFormat;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.log4j.Logger;

/**
 * <p>
 * Class for unmarshalling XML into the SSDS object model
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.8 $
 * @stereotype factory
 * @testcase test.moos.ssds.model.TestObjectBuilder
 */
public class ObjectBuilder {

	/**
	 * This is a Log4JLogger that is used to log information to
	 */
	private static Logger logger = Logger.getLogger(ObjectBuilder.class);

	/**
	 * The url of the file used to build the objects from (XML file)
	 */
	private URL url;

	/**
	 * This is the string that is the XML document if is specified
	 */
	private String xmlDocument = null;

	/**
	 * This is a boolean to track whether the XML was specified as a string or
	 * not (URL).
	 */
	private boolean xmlInStringFormat = false;

	/**
	 * This is the parser that comes from the NU XOM package that will be used
	 * to read the XML document into an object that we can work with
	 */
	private Builder parser;

	/**
	 * This is a date formatting utility
	 */
	private XmlDateFormat xmlDateFormat = new XmlDateFormat();

	/**
	 * This boolean indicates if the conversion went OK or not
	 */
	private boolean buildFailed = false;

	/**
	 * A StringBuilder that is used to build up a cumulative message of the
	 * object building process
	 */
	private StringBuilder buildReport = new StringBuilder();

	/**
	 * keys = class value = ArrayList of objects of type class, specified by the
	 * key.
	 */
	@SuppressWarnings("rawtypes")
	private Map<Class, Collection<Object>> metadataObjects = new HashMap<Class, Collection<Object>>();

	/**
	 * This method takes in an XML element that should contain information about
	 * a CommentTag and constructs an appropriate CommentTag object.
	 * 
	 * @param element
	 *            the XML element containing information about a CommentTag
	 * @return the CommentTag object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private CommentTag buildCommentTag(Element element)
			throws MetadataException {
		// The object to return
		CommentTag commentTag = null;

		// Make sure the element is not null and matches the class name
		if (element == null
				|| !element.getLocalName().equalsIgnoreCase("CommentTag")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is correct, construct a new object
		commentTag = new CommentTag();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					commentTag.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"tagString")) {
					commentTag.setTagString(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						commentTag.setVersion(version);
					}
				}
			}
		}

		return commentTag;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a DataContainer and constructs an appropriate DataContainer object.
	 * 
	 * @param element
	 *            the XML element containing information about a DataContainer
	 * @return the DataContainer object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private DataContainer buildDataContainer(Element element)
			throws MetadataException {
		// The object to return
		DataContainer dataContainer = null;

		// Make sure the element is not null and matches the object class
		if (element == null
				|| (!element.getLocalName().equalsIgnoreCase("DataContainer")
						&& (!element.getLocalName().equalsIgnoreCase(
								"DataStream")) && (!element.getLocalName()
						.equalsIgnoreCase("DataFile")))) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is of the correct type, construct a new object
		dataContainer = new DataContainer();

		// Check to see if the element is a DatFile or DataStream and set the
		// dataContainerType approriately
		if (element.getLocalName().equalsIgnoreCase("DataFile"))
			dataContainer.setDataContainerType(DataContainer.TYPE_FILE);

		if (element.getLocalName().equalsIgnoreCase("DataStream"))
			dataContainer.setDataContainerType(DataContainer.TYPE_STREAM);

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					dataContainer.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase("name")) {
					dataContainer.setName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					dataContainer.setDescription(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"dataContainerType")) {
					dataContainer.setDataContainerType(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"startDate")) {
					// Try to parse the start date and set it on the resource
					try {
						dataContainer.setStartDate(xmlDateFormat
								.parse(attribute.getValue()));
					} catch (Exception e) {
						throw new MetadataException(
								"Could not parse start date "
										+ attribute.getValue() + ": "
										+ e.getMessage());
					}
				} else if (attribute.getLocalName().equalsIgnoreCase("endDate")) {
					// Try to parse the end date and set it on the resource
					try {
						dataContainer.setEndDate(xmlDateFormat.parse(attribute
								.getValue()));
					} catch (Exception e) {
						throw new MetadataException(
								"Could not parse start date "
										+ attribute.getValue() + ": "
										+ e.getMessage());
					}
				} else if (attribute.getLocalName()
						.equalsIgnoreCase("original")) {
					Boolean original = null;
					try {
						original = Boolean.parseBoolean(attribute.getValue());
					} catch (Exception e) {
						logger.error("Could not convert original "
								+ attribute.getValue() + " to a Boolean");
						throw new MetadataException(
								"Could not convert original "
										+ attribute.getValue()
										+ " to a Boolean");
					}
					dataContainer.setOriginal(original);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"uriString")
						|| attribute.getLocalName().equalsIgnoreCase("url")
						|| attribute.getLocalName().equalsIgnoreCase("uri")) {
					dataContainer.setUriString(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"contentLength")) {
					Long contentLength = null;
					try {
						contentLength = Long.parseLong(attribute.getValue());
					} catch (NumberFormatException e) {
						throw new MetadataException(
								"Could not parse content length "
										+ attribute.getValue() + ": "
										+ e.getMessage());
					}
					dataContainer.setContentLength(contentLength);
				} else if (attribute.getLocalName()
						.equalsIgnoreCase("mimeType")) {
					dataContainer.setMimeType(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"contentType")) {
					dataContainer.setMimeType(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"numberOfRecords")) {
					Long numberOfRecords = null;
					try {
						numberOfRecords = Long.parseLong(attribute.getValue());
					} catch (NumberFormatException e) {
						throw new MetadataException(
								"Could not parse numberOfRecords "
										+ attribute.getValue() + ": "
										+ e.getMessage());
					}
					dataContainer.setNumberOfRecords(numberOfRecords);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"dodsAccessible")) {
					Boolean dodsAccessible = null;
					try {
						dodsAccessible = Boolean.parseBoolean(attribute
								.getValue());
					} catch (Exception e) {
						logger.error("Could not convert dodsAccessible "
								+ attribute.getValue() + " to a Boolean");
						throw new MetadataException(
								"Could not convert dodsAccessible "
										+ attribute.getValue()
										+ " to a Boolean");
					}
					dataContainer.setDodsAccessible(dodsAccessible);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"dodsUrlString")
						|| attribute.getLocalName().equalsIgnoreCase("dodsurl")) {
					dataContainer.setDodsUrlString(attribute.getValue());
				} else if (attribute.getLocalName()
						.equalsIgnoreCase("noNetCDF")) {
					Boolean noNetCDF = null;
					try {
						noNetCDF = Boolean.parseBoolean(attribute.getValue());
					} catch (Exception e) {
						logger.error("Could not convert noNetCDF "
								+ attribute.getValue() + " to a Boolean");
						throw new MetadataException(
								"Could not convert noNetCDF "
										+ attribute.getValue()
										+ " to a Boolean");
					}
					dataContainer.setNoNetCDF(noNetCDF);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"minLatitude")) {
					Double minLatitude = null;
					try {
						minLatitude = Double.parseDouble(attribute.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert minLatitude "
								+ attribute.getValue() + " to a Double");
						throw new MetadataException(
								"Could not convert minLatitude "
										+ attribute.getValue() + " to a Double");
					}
					dataContainer.setMinLatitude(minLatitude);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"maxLatitude")) {
					Double maxLatitude = null;
					try {
						maxLatitude = Double.parseDouble(attribute.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert maxLatitude "
								+ attribute.getValue() + " to a Double");
						throw new MetadataException(
								"Could not convert maxLatitude "
										+ attribute.getValue() + " to a Double");
					}
					dataContainer.setMaxLatitude(maxLatitude);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"minLongitude")) {
					Double minLongitude = null;
					try {
						minLongitude = Double.parseDouble(attribute.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert minLongitude "
								+ attribute.getValue() + " to a Double");
						throw new MetadataException(
								"Could not convert minLongitude "
										+ attribute.getValue() + " to a Double");
					}
					dataContainer.setMinLongitude(minLongitude);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"maxLongitude")) {
					Double maxLongitude = null;
					try {
						maxLongitude = Double.parseDouble(attribute.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert maxLongitude "
								+ attribute.getValue() + " to a Double");
						throw new MetadataException(
								"Could not convert maxLongitude "
										+ attribute.getValue() + " to a Double");
					}
					dataContainer.setMaxLongitude(maxLongitude);
				} else if (attribute.getLocalName()
						.equalsIgnoreCase("minDepth")) {
					Float minDepth = null;
					try {
						minDepth = Float.parseFloat(attribute.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert minDepth "
								+ attribute.getValue() + " to a Float");
						throw new MetadataException(
								"Could not convert minDepth "
										+ attribute.getValue() + " to a Float");
					}
					dataContainer.setMinDepth(minDepth);
				} else if (attribute.getLocalName()
						.equalsIgnoreCase("maxDepth")) {
					Float maxDepth = null;
					try {
						maxDepth = Float.parseFloat(attribute.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert maxDepth "
								+ attribute.getValue() + " to a Float");
						throw new MetadataException(
								"Could not convert maxDepth "
										+ attribute.getValue() + " to a Float");
					}
					dataContainer.setMaxDepth(maxDepth);
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						dataContainer.setVersion(version);
					}
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// Iterate over the children
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					dataContainer.setDescription(childElement.getValue());
					// And then move on
					continue;
				}

				// Convert the element to an object
				Object object = elementToObject(childElement);

				// Check for null and ignore the element if nothing is returned
				if (object == null) {
					logger.info("The element " + childElement.getLocalName()
							+ " returned null from object creation");
					continue;
				}

				// Depending on what comes back, set it on the resource
				if (object instanceof Person) {
					dataContainer.setPerson((Person) object);
				} else if (object instanceof HeaderDescription) {
					dataContainer
							.setHeaderDescription((HeaderDescription) object);
				} else if (object instanceof RecordDescription) {
					dataContainer
							.setRecordDescription((RecordDescription) object);
				} else if (object instanceof DataContainerGroup
						&& !dataContainer.getDataContainerGroups().contains(
								(DataContainerGroup) object)) {
					dataContainer
							.addDataContainerGroup((DataContainerGroup) object);
				} else if (object instanceof Resource
						&& !dataContainer.getResources().contains(
								(Resource) object)) {
					dataContainer.addResource((Resource) object);
				} else if (object instanceof Keyword
						&& !dataContainer.getKeywords().contains(
								(Keyword) object)) {
					dataContainer.addKeyword((Keyword) object);
				} else if (object instanceof DataProducer) {
					// Check to see if the original tag was a consumer and if
					// so, add it to the consumer collection
					if ((childElement.getLocalName().equalsIgnoreCase(
							"consumer") || childElement.getLocalName()
							.equalsIgnoreCase("destiny"))
							&& !dataContainer.getConsumers().contains(
									(DataProducer) object)) {
						dataContainer.addConsumer((DataProducer) object);
					}
				} else {
					logger.error("During the construction of a DataContainer object from an"
							+ " element, there was a child element named "
							+ childElement.getLocalName()
							+ " but that does not match any attributes "
							+ "associated with DataContainer, it will be ignored.");
				}
			}
		}
		return dataContainer;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a DataContainerGroup and constructs an appropriate DataContainerGroup
	 * object.
	 * 
	 * @param element
	 *            the XML element containing information about a
	 *            DataContainerGroup
	 * @return the DataContainerGroup object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private DataContainerGroup buildDataContainerGroup(Element element)
			throws MetadataException {

		// The object to return
		DataContainerGroup dataContainerGroup = null;

		// Make sure the element is not null and matches the class name
		if (element == null
				|| !element.getLocalName().equalsIgnoreCase(
						"DataContainerGroup")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is correct, construct a new object
		dataContainerGroup = new DataContainerGroup();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					dataContainerGroup.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase("name")) {
					dataContainerGroup.setName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					dataContainerGroup.setDescription(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						dataContainerGroup.setVersion(version);
					}
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// Iterate over the children
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					dataContainerGroup.setDescription(childElement.getValue());
					// And then move on
					continue;
				}
			}
		}

		return dataContainerGroup;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a DataProducer and constructs an appropriate DataProducer object.
	 * 
	 * @param element
	 *            the XML element containing information about a DataProducer
	 * @return the DataProducer object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private DataProducer buildDataProducer(Element element)
			throws MetadataException {
		// The object to return
		DataProducer dataProducer = null;

		// Make sure the element is not null and matches the object class
		if (element == null
				|| (!element.getLocalName().equalsIgnoreCase("DataProducer")
						&& (!element.getLocalName().equalsIgnoreCase(
								"Deployment")) && (!element.getLocalName()
						.equalsIgnoreCase("ProcessRun")))) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is of the correct type, construct a new object
		dataProducer = new DataProducer();

		// Check to see if element is Deployment or ProcessRun and set the type
		// appropriately
		if (element.getLocalName().equalsIgnoreCase("ProcessRun"))
			dataProducer.setDataProducerType(DataProducer.TYPE_PROCESS_RUN);

		if (element.getLocalName().equalsIgnoreCase("Deployment"))
			dataProducer.setDataProducerType(DataProducer.TYPE_DEPLOYMENT);

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					dataProducer.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase("name")) {
					dataProducer.setName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					dataProducer.setDescription(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"dataProducerType")) {
					dataProducer.setDataProducerType(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"startDate")) {
					// Try to parse the start date and set it on the resource
					try {
						dataProducer.setStartDate(xmlDateFormat.parse(attribute
								.getValue()));
					} catch (Exception e) {
						throw new MetadataException(
								"Could not parse start date "
										+ attribute.getValue() + ": "
										+ e.getMessage());
					}
				} else if (attribute.getLocalName().equalsIgnoreCase("endDate")) {
					// Try to parse the end date and set it on the resource
					try {
						dataProducer.setEndDate(xmlDateFormat.parse(attribute
								.getValue()));
					} catch (Exception e) {
						throw new MetadataException(
								"Could not parse start date "
										+ attribute.getValue() + ": "
										+ e.getMessage());
					}
				} else if (attribute.getLocalName().equalsIgnoreCase("role")) {
					dataProducer.setRole(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"nominalLatitude")) {
					Double nominalLatitude = null;
					try {
						nominalLatitude = Double.parseDouble(attribute
								.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert nominalLatitude "
								+ attribute.getValue() + " to a Double");
						throw new MetadataException(
								"Could not convert nominalLatitude "
										+ attribute.getValue() + " to a Double");
					}
					dataProducer.setNominalLatitude(nominalLatitude);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"nominalLatitudeAccuracy")) {
					Float nominalLatitudeAccuracy = null;
					try {
						nominalLatitudeAccuracy = Float.parseFloat(attribute
								.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert nominalLatitudeAccuracy "
								+ attribute.getValue() + " to a Float");
						throw new MetadataException(
								"Could not convert nominalLatitudeAccuracy "
										+ attribute.getValue() + " to a Float");
					}
					dataProducer
							.setNominalLatitudeAccuracy(nominalLatitudeAccuracy);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"nominalLongitude")) {
					Double nominalLongitude = null;
					try {
						nominalLongitude = Double.parseDouble(attribute
								.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert nominalLongitude "
								+ attribute.getValue() + " to a Double");
						throw new MetadataException(
								"Could not convert nominalLongitude "
										+ attribute.getValue() + " to a Double");
					}
					dataProducer.setNominalLongitude(nominalLongitude);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"nominalLongitudeAccuracy")) {
					Float nominalLongitudeAccuracy = null;
					try {
						nominalLongitudeAccuracy = Float.parseFloat(attribute
								.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert nominalLongitudeAccuracy "
								+ attribute.getValue() + " to a Float");
						throw new MetadataException(
								"Could not convert nominalLongitudeAccuracy "
										+ attribute.getValue() + " to a Float");
					}
					dataProducer
							.setNominalLongitudeAccuracy(nominalLongitudeAccuracy);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"nominalDepth")) {
					Float nominalDepth = null;
					try {
						nominalDepth = Float.parseFloat(attribute.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert nominalDepth "
								+ attribute.getValue() + " to a Double");
						throw new MetadataException(
								"Could not convert nominalDepth "
										+ attribute.getValue() + " to a Double");
					}
					dataProducer.setNominalDepth(nominalDepth);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"nominalDepthAccuracy")) {
					Float nominalDepthAccuracy = null;
					try {
						nominalDepthAccuracy = Float.parseFloat(attribute
								.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert nominalDepthAccuracy "
								+ attribute.getValue() + " to a Float");
						throw new MetadataException(
								"Could not convert nominalDepthAccuracy "
										+ attribute.getValue() + " to a Float");
					}
					dataProducer.setNominalDepthAccuracy(nominalDepthAccuracy);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"nominalBenthicAltitude")) {
					Float nominalBenthicAltitude = null;
					try {
						nominalBenthicAltitude = Float.parseFloat(attribute
								.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert nominalBenthicAltitude "
								+ attribute.getValue() + " to a Double");
						throw new MetadataException(
								"Could not convert nominalBenthicAltitude "
										+ attribute.getValue() + " to a Double");
					}
					dataProducer
							.setNominalBenthicAltitude(nominalBenthicAltitude);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"nominalBenthicAltitudeAccuracy")) {
					Float nominalBenthicAltitudeAccuracy = null;
					try {
						nominalBenthicAltitudeAccuracy = Float
								.parseFloat(attribute.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert nominalBenthicAltitudeAccuracy "
								+ attribute.getValue() + " to a Float");
						throw new MetadataException(
								"Could not convert nominalBenthicAltitudeAccuracy "
										+ attribute.getValue() + " to a Float");
					}
					dataProducer
							.setNominalBenthicAltitudeAccuracy(nominalBenthicAltitudeAccuracy);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"nominalBenthicAltitudeAccuracy")) {
					Float nominalBenthicAltitudeAccuracy = null;
					try {
						nominalBenthicAltitudeAccuracy = Float
								.parseFloat(attribute.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert nominalBenthicAltitudeAccuracy "
								+ attribute.getValue() + " to a Float");
						throw new MetadataException(
								"Could not convert nominalBenthicAltitudeAccuracy "
										+ attribute.getValue() + " to a Float");
					}
					dataProducer
							.setNominalBenthicAltitudeAccuracy(nominalBenthicAltitudeAccuracy);
				} else if (attribute.getLocalName().equalsIgnoreCase("xoffset")) {
					Float xoffset = null;
					try {
						xoffset = Float.parseFloat(attribute.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert xoffset "
								+ attribute.getValue() + " to a Float");
						throw new MetadataException(
								"Could not convert xoffset "
										+ attribute.getValue() + " to a Float");
					}
					dataProducer.setXoffset(xoffset);
				} else if (attribute.getLocalName().equalsIgnoreCase("yoffset")) {
					Float yoffset = null;
					try {
						yoffset = Float.parseFloat(attribute.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert yoffset "
								+ attribute.getValue() + " to a Float");
						throw new MetadataException(
								"Could not convert yoffset "
										+ attribute.getValue() + " to a Float");
					}
					dataProducer.setYoffset(yoffset);
				} else if (attribute.getLocalName().equalsIgnoreCase("zoffset")) {
					Float zoffset = null;
					try {
						zoffset = Float.parseFloat(attribute.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert zoffset "
								+ attribute.getValue() + " to a Float");
						throw new MetadataException(
								"Could not convert zoffset "
										+ attribute.getValue() + " to a Float");
					}
					dataProducer.setZoffset(zoffset);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"orientationDescription")) {
					dataProducer
							.setOrientationDescription(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"x3DOrientationText")) {
					dataProducer.setX3DOrientationText(attribute.getValue());
				} else if (attribute.getLocalName()
						.equalsIgnoreCase("hostName")) {
					dataProducer.setHostName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						dataProducer.setVersion(version);
					}
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// Iterate over the children
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					dataProducer.setDescription(childElement.getValue());
					// And then move on
					continue;
				}

				// Convert the element to an object
				Object object = elementToObject(childElement);

				// Check for null and ignore the element if nothing is returned
				if (object == null) {
					logger.info("The element " + childElement.getLocalName()
							+ " returned null from object creation");
					continue;
				}

				// Depending on what comes back, set it on the resource
				if (object instanceof Person) {
					dataProducer.setPerson((Person) object);
				} else if (object instanceof Device) {
					dataProducer.setDevice((Device) object);
				} else if (object instanceof Software) {
					dataProducer.setSoftware((Software) object);
				} else if (object instanceof DataProducer
						&& !dataProducer.getChildDataProducers().contains(
								(DataProducer) object)) {
					dataProducer.addChildDataProducer((DataProducer) object);
				} else if (object instanceof DataProducerGroup
						&& !dataProducer.getDataProducerGroups().contains(
								(DataProducerGroup) object)) {
					dataProducer
							.addDataProducerGroup((DataProducerGroup) object);
				} else if (object instanceof Resource
						&& !dataProducer.getResources().contains(
								(Resource) object)) {
					dataProducer.addResource((Resource) object);
				} else if (object instanceof Keyword
						&& !dataProducer.getKeywords().contains(
								(Keyword) object)) {
					dataProducer.addKeyword((Keyword) object);
				} else if (object instanceof Event
						&& !dataProducer.getEvents().contains((Event) object)) {
					dataProducer.addEvent((Event) object);
				} else if (object instanceof DataContainer) {
					if (element.getLocalName().equalsIgnoreCase("output")
							&& !dataProducer.getOutputs().contains(
									(DataContainer) object)) {
						dataProducer.addOutput((DataContainer) object);
					}
				} else {
					logger.error("During the construction of a DataProducer object from an"
							+ " element, there was a child element named "
							+ childElement.getLocalName()
							+ " but that does not match any attributes "
							+ "associated with DataProducer, it will be ignored.");
				}
			}
		}
		return dataProducer;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a DataProducerGroup and constructs an appropriate DataProducerGroup
	 * object.
	 * 
	 * @param element
	 *            the XML element containing information about a
	 *            DataProducerGroup
	 * @return the DataProducerGroup object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private DataProducerGroup buildDataProducerGroup(Element element)
			throws MetadataException {

		// The object to return
		DataProducerGroup dataProducerGroup = null;

		// Make sure the element is not null and matches the class name
		if (element == null
				|| !element.getLocalName()
						.equalsIgnoreCase("DataProducerGroup")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is correct, construct a new object
		dataProducerGroup = new DataProducerGroup();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					dataProducerGroup.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase("name")) {
					dataProducerGroup.setName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					dataProducerGroup.setDescription(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						dataProducerGroup.setVersion(version);
					}
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// Iterate over the children
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					dataProducerGroup.setDescription(childElement.getValue());
					// And then move on
					continue;
				}
			}
		}

		return dataProducerGroup;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a Device and constructs an appropriate Device object.
	 * 
	 * @param element
	 *            the XML element containing information about a Device
	 * @return the Device object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private Device buildDevice(Element element) throws MetadataException {
		// The object to return
		Device device = null;

		// Make sure the element is not null and matches the class name
		if (element == null
				|| !element.getLocalName().equalsIgnoreCase("Device")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is correct, construct a new object
		device = new Device();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					device.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase("uuid")) {
					device.setUuid(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("name")) {
					device.setName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					device.setDescription(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("mfgName")) {
					device.setMfgName(attribute.getValue());
				} else if (attribute.getLocalName()
						.equalsIgnoreCase("mfgModel")) {
					device.setMfgModel(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"mfgSerialNumber")) {
					device.setMfgSerialNumber(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"infoUrlList")) {
					device.setInfoUrlList(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						device.setVersion(version);
					}
				} else if (attribute.getLocalName().equalsIgnoreCase("type")) {
					// Move the type attribute to a new DeviceType object and
					// add it
					DeviceType deviceType = new DeviceType();
					deviceType.setName(attribute.getValue());
					device.setDeviceType(deviceType);
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// Iterate over the children
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					device.setDescription(childElement.getValue());
					// And then move on
					continue;
				}

				// Convert the element to an object
				Object object = elementToObject(childElement);

				// Check for null and ignore the element if nothing is returned
				if (object == null) {
					logger.info("The element " + childElement.getLocalName()
							+ " returned null from object creation");
					continue;
				}

				// Depending on what comes back, set it on the resource
				if (object instanceof Person) {
					device.setPerson((Person) object);
				} else if (object instanceof DeviceType) {
					device.setDeviceType((DeviceType) object);
				} else if (object instanceof Resource
						&& !device.getResources().contains((Resource) object)) {
					device.addResource((Resource) object);
				} else {
					logger.error("During the construction of a DeviceType object from a"
							+ " tag, there was a child element named "
							+ childElement.getLocalName()
							+ " but that is not recognized as an "
							+ "attribute of Device, it will be ignored.");
				}
			}
		}
		return device;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a DeviceType and constructs an appropriate DeviceType object.
	 * 
	 * @param element
	 *            the XML element containing information about a DeviceType
	 * @return the DeviceType object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private DeviceType buildDeviceType(Element element)
			throws MetadataException {

		// The object to return
		DeviceType deviceType = null;

		// Make sure the element is not null and matches the class name
		if (element == null
				|| !element.getLocalName().equalsIgnoreCase("DeviceType")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is correct, construct a new object
		deviceType = new DeviceType();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					deviceType.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase("name")) {
					deviceType.setName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					deviceType.setDescription(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						deviceType.setVersion(version);
					}
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// Iterate over the children
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					deviceType.setDescription(childElement.getValue());
					// And then move on
					continue;
				}
			}
		}

		return deviceType;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * an Event and constructs an appropriate Event object.
	 * 
	 * @param element
	 *            the XML element containing information about an Event
	 * @return the Event object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private Event buildEvent(Element element) throws MetadataException {
		// The object to return
		Event event = null;

		// Make sure the element is not null and matches the object class
		if (element == null
				|| !element.getLocalName().equalsIgnoreCase("Event")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is of the correct type, construct a new object
		event = new Event();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					event.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase("name")) {
					event.setName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					event.setDescription(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"startDate")) {
					// Try to parse the start date and set it on the resource
					try {
						event.setStartDate(xmlDateFormat.parse(attribute
								.getValue()));
					} catch (Exception e) {
						throw new MetadataException(
								"Could not parse start date "
										+ attribute.getValue() + ": "
										+ e.getMessage());
					}
				} else if (attribute.getLocalName().equalsIgnoreCase("endDate")) {
					// Try to parse the end date and set it on the resource
					try {
						event.setEndDate(xmlDateFormat.parse(attribute
								.getValue()));
					} catch (Exception e) {
						throw new MetadataException(
								"Could not parse start date "
										+ attribute.getValue() + ": "
										+ e.getMessage());
					}
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						event.setVersion(version);
					}
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// Iterate over the children
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					event.setDescription(childElement.getValue());
					// And then move on
					continue;
				}
			}
		}

		return event;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a HeaderDescription and constructs an appropriate HeaderDescription
	 * object.
	 * 
	 * @param element
	 *            the XML element containing information about a
	 *            HeaderDescription
	 * @return the HeaderDescription object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private HeaderDescription buildHeaderDescription(Element element)
			throws MetadataException {
		// The object to return
		HeaderDescription headerDescription = null;

		// Make sure the element is not null and matches the class name
		if (element == null
				|| !element.getLocalName()
						.equalsIgnoreCase("HeaderDescription")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is correct, construct a new object
		headerDescription = new HeaderDescription();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					headerDescription.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					headerDescription.setDescription(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"byteOffset")) {
					long byteOffset = -1;
					try {
						byteOffset = Long.parseLong(attribute.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert byteOffset "
								+ attribute.getValue() + " to a long");
						throw new MetadataException(
								"Could not convert byteOffset "
										+ attribute.getValue() + " to a long");
					}
					headerDescription.setByteOffset(byteOffset);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"numHeaderLines")) {
					int numHeaderLines = -1;
					try {
						numHeaderLines = Integer.parseInt(attribute.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert numHeaderLines "
								+ attribute.getValue() + " to a int");
						throw new MetadataException(
								"Could not convert numHeaderLines "
										+ attribute.getValue() + " to a int");
					}
					headerDescription.setNumHeaderLines(numHeaderLines);
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						headerDescription.setVersion(version);
					}
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// The only child elements should be UserGroups
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					headerDescription.setDescription(childElement.getValue());
					// And then move on
					continue;
				}

				// Convert the element to an object
				Object object = elementToObject(childElement);

				// Check for null and ignore the element if nothing is returned
				if (object == null) {
					logger.info("The element " + childElement.getLocalName()
							+ " returned null from object creation");
					continue;
				}

				// If the object is a CommentTag
				if (object instanceof CommentTag
						&& !headerDescription.getCommentTags().contains(
								(CommentTag) object)) {
					headerDescription.addCommentTag((CommentTag) object);
				} else {
					logger.error("During the construction of a HeaderDescription object from a"
							+ " tag, there was a child element named "
							+ childElement.getLocalName()
							+ " but this is not a recognized attribute on HeaderDescription.  It will be ignored.");
				}
			}
		}

		return headerDescription;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a Keyword and constructs an appropriate Keyword object.
	 * 
	 * @param element
	 *            the XML element containing information about a Keyword
	 * @return the Keyword object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private Keyword buildKeyword(Element element) throws MetadataException {
		// The object to return
		Keyword keyword = null;

		// Make sure the element is not null and matches the class name
		if (element == null
				|| !element.getLocalName().equalsIgnoreCase("Keyword")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is correct, construct a new object
		keyword = new Keyword();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					keyword.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase("name")) {
					keyword.setName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					keyword.setDescription(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						keyword.setVersion(version);
					}
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// Iterate over the children
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					keyword.setDescription(childElement.getValue());
					// And then move on
					continue;
				}
			}
		}

		return keyword;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a person and constructs an appropriate Person object.
	 * 
	 * @param element
	 *            the XML element containing information about a person
	 * @return the Person object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private Person buildPersonObject(Element element) throws MetadataException {
		// The object to return
		Person person = null;

		// Make sure the element is not null and matches the object class
		if (element == null
				|| !element.getLocalName().equalsIgnoreCase("person")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is of the correct type, construct a new object
		person = new Person();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					person.setId(id);
				} else if (attribute.getLocalName()
						.equalsIgnoreCase("address1")) {
					person.setAddress1(attribute.getValue());
				} else if (attribute.getLocalName()
						.equalsIgnoreCase("address2")) {
					person.setAddress2(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("city")) {
					person.setCity(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("email")) {
					person.setEmail(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"firstname")) {
					person.setFirstname(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"organization")) {
					person.setOrganization(attribute.getValue());
				} else if (attribute.getLocalName()
						.equalsIgnoreCase("password")) {
					person.setPassword(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("phone")) {
					person.setPhone(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("state")) {
					person.setState(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("status")) {
					person.setStatus(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("surname")) {
					person.setSurname(attribute.getValue());
				} else if (attribute.getLocalName()
						.equalsIgnoreCase("username")) {
					person.setUsername(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						person.setVersion(version);
					}
				} else if (attribute.getLocalName().equalsIgnoreCase("zipcode")) {
					person.setZipcode(attribute.getValue());
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// The only child elements should be UserGroups
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Convert the element to an object
				Object object = elementToObject(childElement);

				// Check for null and ignore the element if nothing is returned
				if (object == null) {
					logger.info("The element " + childElement.getLocalName()
							+ " returned null from object creation");
					continue;
				}

				// If the object is a UserGroup, add it to the PersonObject
				if (object instanceof UserGroup
						&& !person.getUserGroups().contains((UserGroup) object)) {
					person.addUserGroup((UserGroup) object);
				} else {
					logger.error("During the construction of a Person object from a"
							+ " person tag, there was a child element named "
							+ childElement.getLocalName()
							+ " but only UserGroup objects can be children of Person objects");
				}
			}
		}

		return person;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a RecordDescription and constructs an appropriate RecordDescription
	 * object.
	 * 
	 * @param element
	 *            the XML element containing information about a
	 *            RecordDescription
	 * @return the RecordDescription object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private RecordDescription buildRecordDescription(Element element)
			throws MetadataException {
		// The object to return
		RecordDescription recordDescription = null;

		// Make sure the element is not null and matches the class name
		if (element == null
				|| !element.getLocalName()
						.equalsIgnoreCase("RecordDescription")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is correct, construct a new object
		recordDescription = new RecordDescription();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					recordDescription.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"recordType")) {
					Long recordType = null;
					try {
						recordType = Long.parseLong(attribute.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert recordType "
								+ attribute.getValue() + " to a Long");
						throw new MetadataException(
								"Could not convert recordType "
										+ attribute.getValue() + " to a Long");
					}
					recordDescription.setRecordType(recordType);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"bufferStyle")) {
					recordDescription.setBufferStyle(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"bufferParseType")) {
					recordDescription.setBufferParseType(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"bufferItemSeparator")) {
					recordDescription.setBufferItemSeparator(attribute
							.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"bufferLengthType")) {
					recordDescription.setBufferLengthType(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"recordTerminator")) {
					recordDescription.setRecordTerminator(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"parseable")) {
					Boolean parseable = null;
					try {
						parseable = Boolean.parseBoolean(attribute.getValue());
					} catch (Exception e) {
						logger.error("Could not convert parseable "
								+ attribute.getValue() + " to a Boolean");
						throw new MetadataException(
								"Could not convert parseable "
										+ attribute.getValue()
										+ " to a Boolean");
					}
					recordDescription.setParseable(parseable);
				} else if (attribute.getLocalName().equalsIgnoreCase("endian")) {
					recordDescription.setEndian(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"recordParseRegExp")) {
					recordDescription
							.setRecordParseRegExp(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						recordDescription.setVersion(version);
					}
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// Iterate over the children
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Convert the element to an object
				Object object = elementToObject(childElement);

				// Check for null and ignore the element if nothing is returned
				if (object == null) {
					logger.info("The element " + childElement.getLocalName()
							+ " returned null from object creation");
					continue;
				}

				// Depending on what comes back, set it on the resource
				if (object instanceof RecordVariable
						&& !recordDescription.getRecordVariables().contains(
								(RecordVariable) object)) {
					recordDescription
							.addRecordVariable((RecordVariable) object);
				} else {
					logger.error("During the construction of a RecordDescription object from a"
							+ " element, there was a child element named "
							+ childElement.getLocalName()
							+ " but that is not expected for RecordDescription. It will be ignored.");
				}
			}
		}

		return recordDescription;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a RecordVariable and constructs an appropriate RecordVariable object.
	 * 
	 * @param element
	 *            the XML element containing information about a RecordVariable
	 * @return the RecordVariable object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private RecordVariable buildRecordVariable(Element element)
			throws MetadataException {
		// The object to return
		RecordVariable recordVariable = null;

		// Make sure the element is not null and matches the class name
		if (element == null
				|| !element.getLocalName().equalsIgnoreCase("RecordVariable")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is correct, construct a new object
		recordVariable = new RecordVariable();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					recordVariable.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase("name")) {
					recordVariable.setName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					recordVariable.setDescription(attribute.getValue());
				} else if (attribute.getLocalName()
						.equalsIgnoreCase("longName")) {
					recordVariable.setLongName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("format")) {
					recordVariable.setFormat(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("units")) {
					recordVariable.setUnits(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"columnIndex")) {
					long columnIndex = -1;
					try {
						columnIndex = Long.parseLong(attribute.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert column index "
								+ attribute.getValue() + " to a long");
						throw new MetadataException(
								"Could not convert column index "
										+ attribute.getValue() + " to a Long");
					}
					recordVariable.setColumnIndex(columnIndex);
				} else if (attribute.getLocalName()
						.equalsIgnoreCase("validMin")) {
					recordVariable.setValidMin(attribute.getValue());
				} else if (attribute.getLocalName()
						.equalsIgnoreCase("validMax")) {
					recordVariable.setValidMax(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"missingValue")) {
					recordVariable.setMissingValue(attribute.getValue());
				} else if (attribute.getLocalName()
						.equalsIgnoreCase("accuracy")) {
					recordVariable.setAccuracy(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"displayMin")) {
					Double displayMin = null;
					try {
						displayMin = Double.parseDouble(attribute.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert display min "
								+ attribute.getValue() + " to a double");
						throw new MetadataException(
								"Could not convert display min "
										+ attribute.getValue() + " to a double");
					}
					recordVariable.setDisplayMin(displayMin);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"displayMax")) {
					Double displayMax = null;
					try {
						displayMax = Double.parseDouble(attribute.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert display max "
								+ attribute.getValue() + " to a double");
						throw new MetadataException(
								"Could not convert display max "
										+ attribute.getValue() + " to a double");
					}
					recordVariable.setDisplayMax(displayMax);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"referenceScale")) {
					recordVariable.setReferenceScale(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"conversionScale")) {
					Double conversionScale = null;
					try {
						conversionScale = Double.parseDouble(attribute
								.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert conversionScale "
								+ attribute.getValue() + " to a double");
						throw new MetadataException(
								"Could not convert conversionScale "
										+ attribute.getValue() + " to a double");
					}
					recordVariable.setConversionScale(conversionScale);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"conversionOffset")) {
					Double conversionOffset = null;
					try {
						conversionOffset = Double.parseDouble(attribute
								.getValue());
					} catch (NumberFormatException e) {
						logger.error("Could not convert conversionOffset "
								+ attribute.getValue() + " to a double");
						throw new MetadataException(
								"Could not convert conversionOffset "
										+ attribute.getValue() + " to a double");
					}
					recordVariable.setConversionOffset(conversionOffset);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"convertedUnits")) {
					recordVariable.setConvertedUnits(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"sourceSensorID")) {
					Long sourceSensorID = null;
					if (attribute.getValue() != null) {
						try {
							sourceSensorID = Long.parseLong(attribute
									.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert sourceSensorID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException(
									"Could not convert sourceSensorID "
											+ attribute.getValue()
											+ " to a Long");
						}
					}
					// Set it
					recordVariable.setSourceSensorID(sourceSensorID);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"parseRegExp")) {
					recordVariable.setParseRegExp(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						recordVariable.setVersion(version);
					}
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// Iterate over the children
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					recordVariable.setDescription(childElement.getValue());
					// And then move on
					continue;
				}

				// Convert the element to an object
				Object object = elementToObject(childElement);

				// Check for null and ignore the element if nothing is returned
				if (object == null) {
					logger.info("The element " + childElement.getLocalName()
							+ " returned null from object creation");
					continue;
				}

				// Depending on what comes back, set it on the resource
				if (object instanceof StandardDomain) {
					recordVariable.setStandardDomain((StandardDomain) object);
				} else if (object instanceof StandardKeyword) {
					recordVariable.setStandardKeyword((StandardKeyword) object);
				} else if (object instanceof StandardReferenceScale) {
					recordVariable
							.setStandardReferenceScale((StandardReferenceScale) object);
				} else if (object instanceof StandardUnit) {
					recordVariable.setStandardUnit((StandardUnit) object);
				} else if (object instanceof StandardVariable) {
					recordVariable
							.setStandardVariable((StandardVariable) object);
				} else {
					logger.error("During the construction of a RecordVariable object from a"
							+ " element, there was a child element named "
							+ childElement.getLocalName()
							+ " but that is not expected for RecordVariable. It will be ignored.");
				}
			}
		}

		return recordVariable;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a Resource and constructs an appropriate Resource object.
	 * 
	 * @param element
	 *            the XML element containing information about a Resource
	 * @return the Resource object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private Resource buildResource(Element element) throws MetadataException {
		// The object to return
		Resource resource = null;

		// Make sure the element is not null and matches the object class
		if (element == null
				|| !element.getLocalName().equalsIgnoreCase("Resource")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is of the correct type, construct a new object
		resource = new Resource();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					resource.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase("name")) {
					resource.setName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					resource.setDescription(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"startDate")) {
					// Try to parse the start date and set it on the resource
					try {
						resource.setStartDate(xmlDateFormat.parse(attribute
								.getValue()));
					} catch (Exception e) {
						throw new MetadataException(
								"Could not parse start date "
										+ attribute.getValue() + ": "
										+ e.getMessage());
					}
				} else if (attribute.getLocalName().equalsIgnoreCase("endDate")) {
					// Try to parse the end date and set it on the resource
					try {
						resource.setEndDate(xmlDateFormat.parse(attribute
								.getValue()));
					} catch (Exception e) {
						throw new MetadataException(
								"Could not parse start date "
										+ attribute.getValue() + ": "
										+ e.getMessage());
					}
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"uriString")
						|| (attribute.getLocalName().equalsIgnoreCase("uri"))
						|| (attribute.getLocalName().equalsIgnoreCase("url"))) {
					resource.setUriString(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"contentLength")) {
					Long contentLength = null;
					try {
						contentLength = Long.parseLong(attribute.getValue());
					} catch (NumberFormatException e) {
						throw new MetadataException(
								"Could not parse content length "
										+ attribute.getValue() + ": "
										+ e.getMessage());
					}
					resource.setContentLength(contentLength);
				} else if (attribute.getLocalName()
						.equalsIgnoreCase("mimeType")) {
					resource.setMimeType(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						resource.setVersion(version);
					}
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"resourceType")) {
					// Convert to object and add
					ResourceType resourceType = new ResourceType();
					resourceType.setName(attribute.getValue());
					resource.setResourceType(resourceType);
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// Iterate over the children
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					resource.setDescription(childElement.getValue());
					// And then move on
					continue;
				}

				// Convert the element to an object
				Object object = elementToObject(childElement);

				// Check for null and ignore the element if nothing is returned
				if (object == null) {
					logger.info("The element " + childElement.getLocalName()
							+ " returned null from object creation");
					continue;
				}

				// Depending on what comes back, set it on the resource
				if (object instanceof ResourceType) {
					resource.setResourceType((ResourceType) object);
				} else if (object instanceof ResourceBLOB) {
					resource.setResourceBLOB((ResourceBLOB) object);
				} else if (object instanceof Person) {
					resource.setPerson((Person) object);
				} else if (object instanceof Keyword
						&& !resource.getKeywords().contains((Keyword) object)) {
					resource.addKeyword((Keyword) object);
				} else {
					logger.error("During the construction of a Person object from a"
							+ " person tag, there was a child element named "
							+ childElement.getLocalName()
							+ " but only UserGroup objects can be children of Person objects");
				}
			}
		}
		return resource;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a ResourceBLOB and constructs an appropriate ResourceBLOB object.
	 * 
	 * @param element
	 *            the XML element containing information about a ResourceBLOB
	 * @return the ResourceBLOB object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private ResourceBLOB buildResourceBLOB(Element element)
			throws MetadataException {
		// The object to return
		ResourceBLOB resourceBLOB = null;

		// Make sure the element is not null and matches the class name
		if (element == null
				|| !element.getLocalName().equalsIgnoreCase("ResourceBLOB")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is correct, construct a new object
		resourceBLOB = new ResourceBLOB();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					resourceBLOB.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase("name")) {
					resourceBLOB.setName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					resourceBLOB.setDescription(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"byteArray")) {
					// Take the base64 encoded string and decode to byte array
					// and set on the BLOB
					Base64 base64 = new Base64();
					try {
						resourceBLOB.setByteArray(base64.decode(attribute
								.getValue()));
					} catch (Exception e) {
						throw new MetadataException(
								"Something went wrong trying to decode byteArray parameter: "
										+ e.getMessage());
					}
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						resourceBLOB.setVersion(version);
					}
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// Iterate over the children
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					resourceBLOB.setDescription(childElement.getValue());
					// And then move on
					continue;
				}
			}
		}

		return resourceBLOB;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a ResourceType and constructs an appropriate ResourceType object.
	 * 
	 * @param element
	 *            the XML element containing information about a ResourceType
	 * @return the ResourceType object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private ResourceType buildResourceType(Element element)
			throws MetadataException {
		// The object to return
		ResourceType resourceType = null;

		// Make sure the element is not null and matches the class name
		if (element == null
				|| !element.getLocalName().equalsIgnoreCase("ResourceType")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is correct, construct a new object
		resourceType = new ResourceType();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					resourceType.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase("name")) {
					resourceType.setName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					resourceType.setDescription(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						resourceType.setVersion(version);
					}
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// Iterate over the children
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					resourceType.setDescription(childElement.getValue());
					// And then move on
					continue;
				}
			}
		}

		return resourceType;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a Software and constructs an appropriate Software object.
	 * 
	 * @param element
	 *            the XML element containing information about a Software
	 * @return the Software object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private Software buildSoftware(Element element) throws MetadataException {
		// The object to return
		Software software = null;

		// Make sure the element is not null and matches the class name
		if (element == null
				|| !element.getLocalName().equalsIgnoreCase("Software")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is correct, construct a new object
		software = new Software();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					software.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase("name")) {
					software.setName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					software.setDescription(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"uriString")
						|| (attribute.getLocalName().equalsIgnoreCase("uri"))
						|| (attribute.getLocalName().equalsIgnoreCase("url"))) {
					software.setUriString(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"softwareVersion")) {
					software.setSoftwareVersion(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
							// Since I used to have an attribute called version
							// in the XML, if the long parsing fails, it could
							// be one of those and I will just assign the value
							// to softwareVersion as an attribute of version
							// would not likely be the Hibernate version
							software.setSoftwareVersion(attribute.getValue());
						}
						software.setVersion(version);
					}
				}
			}
		}
		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// Iterate over the children
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					software.setDescription(childElement.getValue());
					// And then move on
					continue;
				}

				// Convert the element to an object
				Object object = elementToObject(childElement);

				// Check for null and ignore the element if nothing is returned
				if (object == null) {
					logger.info("The element " + childElement.getLocalName()
							+ " returned null from object creation");
					continue;
				}

				// Depending on what comes back, set it on the resource
				if (object instanceof Person) {
					software.setPerson((Person) object);
				} else if (object instanceof Resource
						&& !software.getResources().contains((Resource) object)) {
					software.addResource((Resource) object);
				} else {
					logger.error("During the construction of a Person object from a"
							+ " person tag, there was a child element named "
							+ childElement.getLocalName()
							+ " but only UserGroup objects can be children of Person objects");
				}
			}
		}
		return software;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a StandardDomain and constructs an appropriate StandardDomain object.
	 * 
	 * @param element
	 *            the XML element containing information about a StandardDomain
	 * @return the StandardDomain object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private StandardDomain buildStandardDomain(Element element)
			throws MetadataException {
		// The object to return
		StandardDomain standardDomain = null;

		// Make sure the element is not null and matches the class name
		if (element == null
				|| !element.getLocalName().equalsIgnoreCase("StandardDomain")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is correct, construct a new object
		standardDomain = new StandardDomain();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					standardDomain.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase("name")) {
					standardDomain.setName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					standardDomain.setDescription(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						standardDomain.setVersion(version);
					}
				}
			}
		}
		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// Iterate over the children
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					standardDomain.setDescription(childElement.getValue());
					// And then move on
					continue;
				}
			}
		}

		return standardDomain;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a StandardKeyword and constructs an appropriate StandardKeyword object.
	 * 
	 * @param element
	 *            the XML element containing information about a StandardKeyword
	 * @return the StandardKeyword object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private StandardKeyword buildStandardKeyword(Element element)
			throws MetadataException {
		// The object to return
		StandardKeyword standardKeyword = null;

		// Make sure the element is not null and matches the class name
		if (element == null
				|| !element.getLocalName().equalsIgnoreCase("StandardKeyword")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is correct, construct a new object
		standardKeyword = new StandardKeyword();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					standardKeyword.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase("name")) {
					standardKeyword.setName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					standardKeyword.setDescription(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						standardKeyword.setVersion(version);
					}
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// Iterate over the children
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					standardKeyword.setDescription(childElement.getValue());
					// And then move on
					continue;
				}
			}
		}

		return standardKeyword;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a StandardReferenceScale and constructs an appropriate
	 * StandardReferenceScale object.
	 * 
	 * @param element
	 *            the XML element containing information about a
	 *            StandardReferenceScale
	 * @return the StandardReferenceScale object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private StandardReferenceScale buildStandardReferenceScaleObject(
			Element element) throws MetadataException {
		// The object to return
		StandardReferenceScale standardReferenceScale = null;

		// Make sure the element is not null and matches the class name
		if (element == null
				|| !element.getLocalName().equalsIgnoreCase(
						"StandardReferenceScale")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is correct, construct a new object
		standardReferenceScale = new StandardReferenceScale();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					standardReferenceScale.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase("name")) {
					standardReferenceScale.setName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					standardReferenceScale.setDescription(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						standardReferenceScale.setVersion(version);
					}
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// Iterate over the children
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					standardReferenceScale.setDescription(childElement
							.getValue());
					// And then move on
					continue;
				}
			}
		}

		return standardReferenceScale;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a StandardUnit and constructs an appropriate StandardUnit object.
	 * 
	 * @param element
	 *            the XML element containing information about a StandardUnit
	 * @return the StandardUnit object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private StandardUnit buildStandardUnitObject(Element element)
			throws MetadataException {
		// The object to return
		StandardUnit standardUnit = null;

		// Make sure the element is not null and matches the class name
		if (element == null
				|| !element.getLocalName().equalsIgnoreCase("StandardUnit")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is of the correct type, construct a new object
		standardUnit = new StandardUnit();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					standardUnit.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase("name")) {
					standardUnit.setName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					standardUnit.setDescription(attribute.getValue());
				} else if (attribute.getLocalName()
						.equalsIgnoreCase("longName")) {
					standardUnit.setLongName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("symbol")) {
					standardUnit.setSymbol(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						standardUnit.setVersion(version);
					}
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// Iterate over the children
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					standardUnit.setDescription(childElement.getValue());
					// And then move on
					continue;
				}
			}
		}

		return standardUnit;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a StandardVariable and constructs an appropriate StandardVariable object.
	 * 
	 * @param element
	 *            the XML element containing information about a
	 *            StandardVariable
	 * @return the StandardVariable object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private StandardVariable buildStandardVariableObject(Element element)
			throws MetadataException {
		// The object to return
		StandardVariable standardVariable = null;

		// Make sure the element is not null and matches the class name
		if (element == null
				|| !element.getLocalName().equalsIgnoreCase("StandardVariable")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is correct, construct a new object
		standardVariable = new StandardVariable();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					standardVariable.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase("name")) {
					standardVariable.setName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"namespaceUriString")) {
					standardVariable
							.setNamespaceUriString(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"description")) {
					standardVariable.setDescription(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"referenceScale")) {
					standardVariable.setReferenceScale(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						standardVariable.setVersion(version);
					}
				}
			}
		}

		// Now check for any contained elements
		if (element.getChildElements() != null
				&& element.getChildElements().size() > 0) {
			// The only child elements should be UserGroups
			for (int i = 0; i < element.getChildElements().size(); i++) {
				// Grab the childElement
				Element childElement = element.getChildElements().get(i);

				// Check to see if the element is description and set
				// description attribute if true
				if (childElement.getLocalName().equalsIgnoreCase("description")) {
					standardVariable.setDescription(childElement.getValue());
					// And then move on
					continue;
				}

				// Convert the element to an object
				Object object = elementToObject(childElement);

				// Check for null and ignore the element if nothing is returned
				if (object == null) {
					logger.info("The element " + childElement.getLocalName()
							+ " returned null from object creation");
					continue;
				}

				// If the object is a StandardUnit, add it
				if (object instanceof StandardUnit
						&& !(standardVariable.getStandardUnits()
								.contains((StandardUnit) object))) {
					standardVariable.addStandardUnit((StandardUnit) object);
				} else {
					logger.error("During the construction of a StandardVariable object"
							+ ", there was a child element named "
							+ childElement.getLocalName()
							+ " but only StandardUnit objects can be "
							+ "children of StandardVariable objects");
				}
			}
		}
		return standardVariable;
	}

	/**
	 * This method takes in an XML element that should contain information about
	 * a UserGroup and constructs an appropriate UserGroup object.
	 * 
	 * @param element
	 *            the XML element containing information about a UserGroup
	 * @return the UserGroup object
	 * @throws MetadataException
	 *             if something goes wrong
	 */
	private UserGroup buildUserGroupObject(Element element)
			throws MetadataException {
		// The object to return
		UserGroup userGroup = null;

		// Make sure the element is not null and matches the class name
		if (element == null
				|| !element.getLocalName().equalsIgnoreCase("UserGroup")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element the correct class, construct a new object
		userGroup = new UserGroup();

		// Now crack any attributes that are listed and set properties if they
		// are found
		if (element.getAttributeCount() > 0) {
			for (int i = 0; i < element.getAttributeCount(); i++) {
				// Grab the element
				Attribute attribute = element.getAttribute(i);
				// Try to match to a attribute on the object and set it.
				if (attribute.getLocalName().equalsIgnoreCase("id")) {
					// Try to convert the value to a long
					Long id = null;
					if (attribute.getValue() != null) {
						try {
							id = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("Could not convert ID "
									+ attribute.getValue() + " to a long");
							throw new MetadataException("Could not convert ID "
									+ attribute.getValue() + " to a Long");
						}
					}
					// Set it
					userGroup.setId(id);
				} else if (attribute.getLocalName().equalsIgnoreCase(
						"groupName")) {
					userGroup.setGroupName(attribute.getValue());
				} else if (attribute.getLocalName().equalsIgnoreCase("version")) {
					// convert version to a long
					if (attribute.getValue() != null) {
						long version = -1;
						try {
							version = Long.parseLong(attribute.getValue());
						} catch (NumberFormatException e) {
							logger.error("NumberFormatException caught trying to parse the version "
									+ attribute.getValue()
									+ " to a long.  Not going to throw an Exception");
						}
						userGroup.setVersion(version);
					}
				}
			}
		}
		return userGroup;
	}

	/**
	 * Converts an XML Element into an Object For processing the Objects just
	 * below root in the XML
	 */
	private Object elementToObject(Element element) throws MetadataException {

		// Grab the name of the incoming element
		String name = element.getLocalName();
		logger.debug("elementToObject called with element with localName "
				+ name);

		// Now build the element by calling the name of the element
		if (name.equalsIgnoreCase("CommentTag"))
			return buildCommentTag(element);

		// Check for an output tag and if it is, assume the child is a
		// DataContainer and send the element to the method to build that object
		if (name.equalsIgnoreCase("output")) {
			// Grab the children
			Elements childElements = element.getChildElements();
			// Loop over them and look for a DataContainer, File or Stream
			for (int i = 0; i < childElements.size(); i++) {
				Element childElement = childElements.get(i);
				if (childElement.getLocalName().equalsIgnoreCase(
						"DataContainer")
						|| childElement.getLocalName().equalsIgnoreCase(
								"DataFile")
						|| childElement.getLocalName().equalsIgnoreCase(
								"DataStream")) {
					return buildDataContainer(childElement);
				}
			}
		}

		if (name.equalsIgnoreCase("DataContainer")
				|| name.equalsIgnoreCase("DataStream")
				|| name.equalsIgnoreCase("DataFile"))
			return buildDataContainer(element);

		if (name.equalsIgnoreCase("DataContainerGroup"))
			return buildDataContainerGroup(element);

		// If the tag is a consumer, that means there is a DataProducer tag that
		// need to be parsed out
		if (name.equalsIgnoreCase("consumer")
				|| name.equalsIgnoreCase("destiny")) {
			// Grab the children
			Elements childElements = element.getChildElements();
			// Loop over them and look for a DataProducer (or ProcessRun)
			for (int i = 0; i < childElements.size(); i++) {
				Element childElement = childElements.get(i);
				if (childElement.getLocalName()
						.equalsIgnoreCase("DataProducer")
						|| childElement.getLocalName().equalsIgnoreCase(
								"ProcessRun")
						|| childElement.getLocalName().equalsIgnoreCase(
								"Deployment")) {
					return buildDataProducer(childElement);
				}
			}
		}

		if (name.equalsIgnoreCase("DataProducer")
				|| name.equalsIgnoreCase("ProcessRun")
				|| name.equalsIgnoreCase("Deployment")) {
			return buildDataProducer(element);
		}

		if (name.equalsIgnoreCase("DataProducerGroup"))
			return buildDataProducerGroup(element);

		if (name.equalsIgnoreCase("Device"))
			return buildDevice(element);

		if (name.equalsIgnoreCase("DeviceType"))
			return buildDeviceType(element);

		if (name.equalsIgnoreCase("Event"))
			return buildEvent(element);

		if (name.equalsIgnoreCase("HeaderDescription"))
			return buildHeaderDescription(element);

		if (name.equalsIgnoreCase("Keyword"))
			return buildKeyword(element);

		if (name.equalsIgnoreCase("Person"))
			return buildPersonObject(element);

		if (name.equalsIgnoreCase("RecordDescription"))
			return buildRecordDescription(element);

		if (name.equalsIgnoreCase("RecordVariable"))
			return buildRecordVariable(element);

		if (name.equalsIgnoreCase("Resource"))
			return buildResource(element);

		if (name.equalsIgnoreCase("ResourceBLOB"))
			return buildResourceBLOB(element);

		if (name.equalsIgnoreCase("ResourceType"))
			return buildResourceType(element);

		if (name.equalsIgnoreCase("Software"))
			return buildSoftware(element);

		if (name.equalsIgnoreCase("StandardDomain"))
			return buildStandardDomain(element);

		if (name.equalsIgnoreCase("StandardKeyword"))
			return buildStandardKeyword(element);

		if (name.equalsIgnoreCase("StandardReferenceScale"))
			return buildStandardReferenceScaleObject(element);

		if (name.equalsIgnoreCase("StandardUnit"))
			return buildStandardUnitObject(element);

		if (name.equalsIgnoreCase("StandardVariable"))
			return buildStandardVariableObject(element);

		if (name.equalsIgnoreCase("UserGroup"))
			return buildUserGroupObject(element);

		return null;
	}

	/**
	 * Convert an XML field to the SSDS object model
	 * 
	 * @param The
	 *            string URL of the xml file to decode
	 */
	private void xmlToObjects(String xml) throws Exception {
		logger.debug("xmlToObjects called with xml string " + xml);
		// In here we have a bit of an issue in that if the XML
		// is specified with a schema definition, it needs to be
		// linked to its corresponding DTD because XOM cannot
		// validate against schema (only DTD).

		// Try to parse the XML into the document
		Document document = null;
		try {
			if (this.xmlInStringFormat) {
				document = parser.build(this.xmlDocument, null);
			} else {
				document = parser.build(xml);
			}
		} catch (ValidityException e) {
			logger.error(getClass().getName() + ": " + xml
					+ " is not a valid XML document.  Validation errors:");
			for (int i = 0; i < e.getErrorCount(); i++) {
				logger.error("line " + e.getLineNumber(i) + ": "
						+ e.getValidityError(i));
			}
			throw e;

		} catch (ParsingException e) {
			logger.error(getClass().getName() + ": Unable to parse" + xml + ".");
			throw e;
		} catch (IOException e) {
			logger.error(getClass().getName() + ": Unable to open" + xml
					+ ". Exception cause is" + e.getMessage());
			throw e;
		}

		// Make sure the document looks like it was built OK
		if (document != null) {

			// Grab the root element which must be a Metadata tag
			Element root = document.getRootElement();
			logger.debug("Root element's localName is " + root.getLocalName());

			// Grab all the children elements which can be any of the Metadata
			// objects
			Elements children = root.getChildElements();

			// Grab the number of children elements
			int numChildElements = children.size();
			logger.debug("There are " + numChildElements
					+ " child elements under the root element "
					+ root.getLocalName());

			// This is a bit of a hack, in that some documents come in with the
			// head being a DataProducerGroup and the child being a
			// DataProducer. The relational model is the other way around so, I
			// will flip parent/child relationship to keep things sane
			if (numChildElements == 1) {
				Element topElement = children.get(0);
				if (topElement.getLocalName().equalsIgnoreCase(
						"DataProducerGroup")) {
					// Grab the children
					Elements dpgChildren = topElement.getChildElements();
					// Grab the first child
					if (dpgChildren.size() > 0) {

						// Remove the DataProducerGroup from the root element
						// (Metadata)
						root.removeChild(topElement);

						// Clear elements under the DataProducerGroup
						topElement.removeChildren();

						// Loop over the children and re-parent them
						for (int i = 0; i < dpgChildren.size(); i++) {
							// Grab the child element
							Element tempDPGChild = dpgChildren.get(i);

							// If it is a DataProducer (or Deployment), flip the
							// relationship
							if (tempDPGChild.getLocalName().equalsIgnoreCase(
									"DataProducer")
									|| tempDPGChild.getLocalName()
											.equalsIgnoreCase("Deployment")
									|| tempDPGChild.getLocalName()
											.equalsIgnoreCase("ProcessRun")) {
								// Append the DataProducerGroup
								tempDPGChild.appendChild(topElement);

								// Then add the DataProducer to the Metadata
								// element
								root.appendChild(tempDPGChild);
							} else if (tempDPGChild.getLocalName()
									.equalsIgnoreCase("description")) {
								topElement
										.addAttribute(new Attribute(
												"description", tempDPGChild
														.getValue()));
							}
						}
						// Get the child elements under the metadata since they
						// have changed
						children = root.getChildElements();

						// And the size for those elements
						numChildElements = children.size();
					}
				}
			}

			// Create a place holder for an object, class, boolean, and
			// collection
			Object obj = null;

			// The class of the object that will be in the collection associated
			// with the class key
			@SuppressWarnings("rawtypes")
			Class c = null;

			// A boolean to indicate if the map has the class already
			boolean hasClass = false;

			// The collection of object parsed that are of a certain class type
			Collection<Object> list = null;

			// Now loop through all the children elements
			for (int i = 0; i < numChildElements; i++) {

				// Grab the ith element and convert it to an object
				obj = elementToObject(children.get(i));

				// Now get the class of that object
				c = obj.getClass();

				// Check to see if the class is in the hash map
				hasClass = metadataObjects.keySet().contains(c);

				// If not, add it with an array list that will
				// be used to store object handles
				if (!hasClass) {
					// list = new ArrayList();
					list = new HashSet<Object>();

					// Add it with the key of the class
					metadataObjects.put(c, list);
				}

				// Grab the array list of the objects with
				// that are of the class of the new object
				list = metadataObjects.get(c);

				// Now add the new object to that list
				list.add(obj);
			}
		}
	}

	/**
	 * The constructor for an object builder
	 * 
	 * @param url
	 *            This is the file URL that points to the XML file used to build
	 *            the objects from
	 */
	public ObjectBuilder(URL url) {

		buildReport.append("ObjectBuilder constructed at " + new Date()
				+ "with URL " + url + "\n");

		// Grab the incoming URL and store it locally
		this.url = url;

		// Clear the flag to read from string
		this.xmlInStringFormat = false;

	}

	/**
	 * This constructor takes in an XML document in a String form and then sets
	 * up for unmarshalling
	 * 
	 * @param xmlDocument
	 *            is the String that contains the XML to parse
	 */
	public ObjectBuilder(String xmlDocument) {
		buildReport.append("ObjectBuilder constructed at " + new Date()
				+ "with String\n" + xmlDocument + "\n");
		// Set some local string to the XML doc
		this.xmlDocument = xmlDocument;

		// Set the flag to indicate this will be parsed from the String and not
		// the URL
		this.xmlInStringFormat = true;
	}

	/**
	 * Convert the file specified in the constructor to an Object graph. Do not
	 * attempt to validate.
	 */
	public void unmarshal() {
		try {
			this.unmarshal(false);
		} catch (ValidityException e) {
			logger.error("ValidityException caught trying to unmarshall: "
					+ e.getMessage());
		} catch (Exception e) {
			logger.error("Exception caught trying to unmarshall: "
					+ e.getMessage());
		}
	}

	/**
	 * Convert the file specified in the constructor to an Object graph. If flag
	 * is true then attempt to validate the XML.
	 */
	public void unmarshal(boolean flag) throws ValidityException, Exception {

		// Try to unmarshal the XML into objects using the ObjectBuilder
		logger.debug("Unmarshal called");
		try {
			parser = new Builder(flag);
		} catch (Exception e) {
			logger.error("Exception caught while trying to create an XOM Builder:"
					+ e.getMessage());
			buildFailed = true;
			throw e;
		}
		if (this.xmlInStringFormat) {
			try {
				xmlToObjects(null);
			} catch (Exception e) {
				logger.error("Exception caught while trying to convert XML to objects: "
						+ e.getMessage());
				buildFailed = true;
				throw e;
			}

		} else {
			try {
				logger.debug("Unmarshal called");
				xmlToObjects(url.toExternalForm());
			} catch (Exception e) {
				logger.error("Exception caught while trying to convert XML to objects:"
						+ e.getMessage());
				buildReport
						.append("Exception caught while trying to convert XML to objects:"
								+ e.getMessage());
				buildFailed = true;
				throw e;
			}
		}
	}

	/**
	 * This mehtod returns a boolean that indicates if the unmarshalling failed
	 * or not
	 * 
	 * @return
	 */
	public boolean didUnmarshalFail() {
		return buildFailed;
	}

	/**
	 * This method returns the text message to indicate what might have gone
	 * wrong in the unmarshalling
	 * 
	 * @return
	 */
	public String getErrorMessage() {
		return buildReport.toString();
	}

	/**
	 * This method returns the object collection for the top level objects of a
	 * specified class
	 * 
	 * @param className
	 *            the name of the class whose top level unmarshalled objects
	 *            will be returned
	 * @return the Collection of Objects
	 */
	private Collection<Object> retrieveObjects(String className) {
		Collection<Object> out = null;
		try {
			out = (Collection<Object>) metadataObjects.get(Class
					.forName(className));
		} catch (ClassNotFoundException e) {
			logger.error(getClass().getName() + ": Unable to load class "
					+ className);
		}
		// If nothing was returned, just returned an empty list
		if (out == null) {
			out = new HashSet<Object>();
		}
		return out;
	}

	/**
	 * This method returns all the top level objects parsed from the XML
	 * 
	 * @return All objects generated from the XML
	 */
	public Collection<Object> listAll() {
		Collection<Object> out = new ArrayList<Object>();
		Collection<Collection<Object>> cs = metadataObjects.values();
		Iterator<Collection<Object>> i = cs.iterator();
		while (i.hasNext()) {
			out.addAll(i.next());
		}
		return out;
	}

	/**
	 * Grab all the top level DataProducers and return
	 * 
	 * @return
	 */
	public Collection<Object> listDataProducers() {
		return retrieveObjects("moos.ssds.metadata.DataProducer");
	}

	/**
	 * Grab all top level devices and return
	 * 
	 * @return
	 */
	public Collection<Object> listDevices() {
		return retrieveObjects("moos.ssds.metadata.Device");
	}

}