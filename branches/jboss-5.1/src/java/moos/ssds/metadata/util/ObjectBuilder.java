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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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
	 * A date formatter used to process dates from XML
	 */
	private XmlDateFormat dateFormat = new XmlDateFormat();

	/**
	 * This is the static string that hold the package name of the ObjectBuilder
	 * classes.
	 */
	private static String modelPackage = "moos.ssds.metadata";

	/**
	 * keys = class value = ArrayList of objects of type class, specified by the
	 * key.
	 */
	@SuppressWarnings("rawtypes")
	private Map<Class, Collection<Object>> metadataObjects = new HashMap<Class, Collection<Object>>();

	/**
	 * A counter that can be used to give child data producers unique names if
	 * they all have the same name
	 */
	private int childDataProducerCounter = 2;

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
				|| !element.getLocalName().equalsIgnoreCase("DataContainer")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is of the correct type, construct a new object
		dataContainer = new DataContainer();

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
						"uriString")) {
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
						"dodsUrlString")) {
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

				// Convert the element to an object
				Object object = elementToObject(childElement);

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
					if (childElement.getLocalName()
							.equalsIgnoreCase("consumer")
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
				|| !element.getLocalName().equalsIgnoreCase("DataProducer")) {
			throw new MetadataException("Exception trying to convert element ("
					+ element + ") to an object");
		}

		// OK, the element is of the correct type, construct a new object
		dataProducer = new DataProducer();

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

				// Convert the element to an object
				Object object = elementToObject(childElement);

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

				// Convert the element to an object
				Object object = elementToObject(childElement);

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

				// Convert the element to an object
				Object object = elementToObject(childElement);

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
						"uriString")) {
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
						"uriString")) {
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

				// Convert the element to an object
				Object object = elementToObject(childElement);

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

				// Convert the element to an object
				Object object = elementToObject(childElement);

				// If the object is a StandardUnit, add it
				if (object instanceof StandardUnit) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Convert the file specified in the constructor to an Object graph. If flag
	 * is true then attempt to validate the XML.
	 */
	public void unmarshal(boolean flag) throws ValidityException, Exception {
		// Rest the childdata producer count
		childDataProducerCounter = 2;

		// Try to unmarshal the XML into objects using the ObjectBuilder
		logger.debug("Unmarshal called");
		try {
			parser = new Builder(flag);
		} catch (Exception e) {
			logger.error("Exception caught while trying to create an XOM Builder:"
					+ e.getMessage());
		}
		if (this.xmlInStringFormat) {
			try {
				xmlToObjects(null);
			} catch (Exception e) {
				logger.error("Exception caught while trying to convert XML to objects: "
						+ e.getMessage());
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
			}
		}
	}

	public boolean didUnmarshalFail() {
		return buildFailed;
	}

	public String getErrorMessage() {
		return buildReport.toString();
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

		} catch (ParsingException e) {
			logger.error(getClass().getName() + ": Unable to parse" + xml + ".");
		} catch (IOException e) {
			logger.error(getClass().getName() + ": Unable to open" + xml
					+ ". Exception cause is" + e.getMessage());
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
			// if (size == 1) {
			// Element topElement = children.get(0);
			// if (topElement.getLocalName().equalsIgnoreCase(
			// "DataProducerGroup")) {
			// // Grab the children
			// Elements dpgChildren = topElement.getChildElements();
			// // Grab the first child
			// if (dpgChildren.size() > 0) {
			// root.removeChild(topElement);
			// topElement.removeChildren();
			// for (int i = 0; i < dpgChildren.size(); i++) {
			// Element tempDPGChild = dpgChildren.get(i);
			// // If it is a DataProducer (or Deployment), flip the
			// // relationship
			// if (tempDPGChild.getLocalName().equalsIgnoreCase(
			// "DataProducer")
			// || tempDPGChild.getLocalName()
			// .equalsIgnoreCase("Deployment")) {
			// tempDPGChild.appendChild(topElement);
			// root.appendChild(tempDPGChild);
			// } else if (tempDPGChild.getLocalName()
			// .equalsIgnoreCase("description")) {
			// topElement
			// .addAttribute(new Attribute(
			// "description", tempDPGChild
			// .getValue()));
			// }
			// }
			// children = root.getChildElements();
			// size = children.size();
			// }
			// }
			// }
			//
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

		if (name.equalsIgnoreCase("DataContainer")
				|| name.equalsIgnoreCase("DataStream")
				|| name.equalsIgnoreCase("DataFile"))
			return buildDataContainer(element);

		if (name.equalsIgnoreCase("DataContainerGroup"))
			return buildDataContainerGroup(element);

		// If the tag is a consumer, that means there is a DataProducer tag that
		// need to be parsed out
		if (name.equalsIgnoreCase("consumer")) {
			// Grab the children
			Elements childElements = element.getChildElements();
			// Loop over them and look for a DataProducer (or ProcessRun)
			for (int i = 0; i < childElements.size(); i++) {
				Element childElement = childElements.get(i);
				if (childElement.getLocalName()
						.equalsIgnoreCase("DataProducer")
						|| childElement.getLocalName().equalsIgnoreCase(
								"ProcessRun")) {
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

		// -------------------------------------------------------------------
		// SPECIAL CASES
		// This is the processing that has to be done to support schema
		// evolution and deprecated tages
		// -------------------------------------------------------------------

		// We depreacted the "type" field on device and changed it an object
		// association to DeviceType
		if (name.equalsIgnoreCase("device")
				&& element.getAttribute("type") != null) {
			Attribute typeAtt = element.getAttribute("type");
			element.removeAttribute(typeAtt);
			Element deviceTypeElement = new Element("DeviceType");
			Attribute nameAtt = new Attribute("name", typeAtt.getValue());
			deviceTypeElement.addAttribute(nameAtt);
			element.appendChild(deviceTypeElement);
		}

		if ((name.equalsIgnoreCase("resource") && ((element
				.getAttribute("type") != null) || (element
				.getAttribute("resourceType") != null)))) {
			Attribute typeAtt = element.getAttribute("type");
			if (typeAtt == null)
				typeAtt = element.getAttribute("resourceType");
			element.removeAttribute(typeAtt);
			Element resourceTypeElement = new Element("ResourceType");
			Attribute nameAtt = new Attribute("name", typeAtt.getValue());
			resourceTypeElement.addAttribute(nameAtt);
			element.appendChild(resourceTypeElement);
		}

		// We deprecated elements with lowercase commentTag, so these need to be
		// converted to the uppercase to match the class
		if (name.equals("commentTag")) {
			element.setLocalName("CommentTag");
			element.addAttribute(new Attribute("tagString", element.getValue()));
		}

		// If the tag is a DataFile or DataStream and there is an attribute
		// called "contentType" we changed that to "mimeType" so make that
		// change as well.
		if ((name.equals("DataFile") || name.equals("DataStream"))
				&& element.getAttribute("contentType") != null) {
			Attribute contentType = element.getAttribute("contentType");
			Attribute mimeType = new Attribute("mimeType",
					contentType.getValue());
			element.removeAttribute(contentType);
			element.addAttribute(mimeType);
		}

		// We collapsed the inheritance tree on DataFile and DataStream, so if
		// they need to be converted to DataContainers
		if (name.equals("DataFile")) {
			name = "DataContainer";
			element.setLocalName("DataContainer");
			// Set the type correctly
			Attribute dataContainerTypeAttribute = new Attribute(
					"dataContainerType", DataContainer.TYPE_FILE);
			element.addAttribute(dataContainerTypeAttribute);
		}
		if (name.equals("DataStream")) {
			name = "DataContainer";
			element.setLocalName("DataContainer");
			// Set the type correctly
			Attribute dataContainerTypeAttribute = new Attribute(
					"dataContainerType", DataContainer.TYPE_STREAM);
			element.addAttribute(dataContainerTypeAttribute);
		}

		// We did the same and collapsed Deployment and ProcessRun into
		// DataProducer
		if (name.equals("Deployment")) {
			name = "DataProducer";
			element.setLocalName("DataProducer");
			// Set the type correctly
			Attribute dataProducerTypeAttribute = new Attribute(
					"dataProducerType", DataProducer.TYPE_DEPLOYMENT);
			element.addAttribute(dataProducerTypeAttribute);
		}
		if (name.equals("ProcessRun")) {
			name = "DataProducer";
			element.setLocalName("DataProducer");
			// Set the type correctly
			Attribute dataProducerTypeAttribute = new Attribute(
					"dataProducerType", DataProducer.TYPE_PROCESS_RUN);
			element.addAttribute(dataProducerTypeAttribute);
		}

		// -------------------------------------------------------------------
		// END SPECIAL CASES
		// -------------------------------------------------------------------

		// Create a couple of place holders for Class and Object
		Class c = null;
		Object obj = null;

		// Check the name of the element, if it is uppercase, it is an
		// object, if not, it is an attribute
		boolean isObject = Character.isUpperCase(name.charAt(0));

		// If it is an object, generate one
		// if (isObject) {
		// logger.debug(name + " appears to be an Object");
		// // Create the class
		// c = Class.forName(modelPackage + "." + name);
		// // Now create a new instance of that class
		// obj = c.newInstance();
		// // Now call on the builder to fill out that object
		// buildObject(obj, element);
		// } else {
		// // If it's not an object ignore it. All elements just below
		// // root should be objects.
		//
		// }
		// Return the new object
		return obj;
	}

	/**
	 * The method takes in an object (which is an instance of a class) and sets
	 * the attributes on that object from the attributes of the XML element
	 * 
	 * @param parentObject
	 *            is the object to set the attributes on
	 * @param parentElement
	 *            is the XML element that contains the information to set the
	 *            object attributes with
	 */
	private void buildObject(Object parentObject, Element parentElement)
			throws Exception {

		logger.debug("buildObject called");
		// Call the method to build the object from elements
		buildFromElements(parentObject, parentElement);
		// Call the method to build the object from attributes
		buildFromAttributes(parentObject, parentElement);
	}

	/**
	 * This method takes in and object and an XML element and builds the object
	 * using the elements in the incoming element
	 * 
	 * @param parentObject
	 *            This is the object to build up
	 * @param parentElement
	 *            This is the element used to populate the parentObject
	 * @throws Exception
	 */
	private void buildFromElements(Object parentObject, Element parentElement)
			throws Exception {

		logger.debug("buildFromElements called");
		// Create placeholders for some variables
		String name = null;
		Element childElement = null;
		Object childObject = null;
		boolean isObject = false;
		boolean isProperty = false;

		// Get the class from the incoming object
		Class c = parentObject.getClass();
		logger.debug("parent object class is " + c.getName());

		// Grab all the child elements of the incoming element
		Elements childElements = parentElement.getChildElements();
		logger.debug("childElements is " + childElements.size()
				+ " elements large");

		// Now loop through all the child elements
		for (int i = 0; i < childElements.size(); i++) {
			// Set some variables initially
			isProperty = false;
			childObject = null;

			// Grab the ith child element
			childElement = childElements.get(i);

			// Grab the name of the current child element
			name = childElement.getLocalName();
			// Convert the name if need be
			name = this.convertNameIfNeeded(name);

			// Check for lower case commentTag
			if (name.equals("commentTag")) {
				name = "CommentTag";
				childElement.setLocalName("CommentTag");
				childElement.addAttribute(new Attribute("tagString",
						childElement.getValue()));
			}

			logger.debug("childElement local name is " + name);

			// If it starts with a Capital its an object
			isObject = Character.isUpperCase(name.charAt(0));
			// If it is an object, build the object
			if (isObject) {
				logger.debug(name + " appears to be an object");
				childObject = elementToObject(childElement);
				// Handle some special cases, these are done
				// because in order to attach some elements,
				// different methods must be called instead of
				// the standard get/set on beans. This will need
				// to be kept in synch with any changes to the
				// model
				if (name.equalsIgnoreCase("CommentTag")) {
					add(parentObject, "addCommentTag", childElement);
				} else if (name.equalsIgnoreCase("DataContainerGroup")) {
					add(parentObject, "addDataContainerGroup", childElement);
				} else if (name.equalsIgnoreCase("DataProducer")) {
					addChildDataProducer(parentObject, childElement);
				} else if (name.equalsIgnoreCase("DataProducerGroup")) {
					add(parentObject, "addDataProducerGroup", childElement);
				} else if (name.equalsIgnoreCase("Event")) {
					add(parentObject, "addEvent", childElement);
				} else if (name.equalsIgnoreCase("Keyword")) {
					add(parentObject, "addKeyword", childElement);
				} else if (name.equalsIgnoreCase("RecordVariable")) {
					add(parentObject, "addRecordVariable", childElement);
				} else if (name.equalsIgnoreCase("Resource")) {
					add(parentObject, "addResource", childElement);
				} else if (name.equalsIgnoreCase("StandardUnit")) {
					add(parentObject, "addStandardUnit", childElement);
				} else {
					logger.debug("It appears that " + name + " is a property");
					isProperty = true;
				}
			} else {
				logger.debug("It appears that " + name + " is not an object.");
				// Handle the special case elements
				if (name.equalsIgnoreCase("input")) {
					addInput(parentObject, childElement);
				} else if (name.equalsIgnoreCase("output")) {
					addOutput(parentObject, childElement);
				} else if (name.equalsIgnoreCase("consumer")) {
					addConsumer(parentObject, childElement);
				} else if (name.equalsIgnoreCase("endDate")) {
					// Parse the XML to a date and assign as child object
					childObject = dateFormat.parse(childElement.getValue());
					isProperty = true;
				} else if (name.equalsIgnoreCase("startDate")) {
					// Parse the XML to a date and assign as child object
					childObject = dateFormat.parse(childElement.getValue());
					isProperty = true;
				} else {
					logger.debug(name + " is not a object or a special case");
					childObject = childElement.getValue();
					isProperty = true;
				}
			}
			// If the element is a property and there is a child object, add it
			// to the parent
			if ((isProperty) && (childObject != null)) {
				logger.debug("It is a property and the childObject is not null");
				String methodName = toSetMethodName(childElement);
				logger.debug("methodName is: " + methodName);
				invokeSetMethod(parentObject, methodName, childObject);
			}
		}
	}

	/**
	 * Sets the properties of an object base on the information in an XML
	 * element
	 * 
	 * @param obj
	 *            The object to build
	 * @param element
	 *            The XML element contining the info need to build obj
	 */
	private void buildFromAttributes(Object obj, Element element)
			throws Exception {

		// Create an empty attribute
		Attribute attribute = null;

		// Loop through the attributes of element
		int numAttributes = element.getAttributeCount();
		for (int i = 0; i < numAttributes; i++) {

			// Get the name of the attribute. Need this to call correct setter
			// method.
			attribute = element.getAttribute(i);

			// Attributes almost never represent objects, only basetypes.
			// Handle the special case attributes which represent objects
			if (isSpecialAttribute(attribute)) {
				processSpecialAttribute(obj, attribute);
			} else {
				// Default case: The childObject will be a primitive type
				processAttribute(obj, attribute);
			}
		}
	}

	/**
	 * Special Attributes are attributes that map to Objects rather than
	 * primitive types
	 * 
	 * @return true if the attribute requries special handling, false if it can
	 *         be handled generically as a primitive type
	 */
	private boolean isSpecialAttribute(Attribute attribute) {
		boolean isSpecial = false;
		String attributeName = attribute.getLocalName();
		if ((attributeName.equalsIgnoreCase("endDate"))
				|| (attributeName.equalsIgnoreCase("startDate"))
				|| (attributeName.equalsIgnoreCase("url"))
				|| (attributeName.equalsIgnoreCase("uri"))
				|| (attributeName.equalsIgnoreCase("DODSUrl"))) {
			isSpecial = true;
		}
		return isSpecial;
	}

	/**
	 * Processes attributes that can not be handled gerically as primitive
	 * types. For example, dates are converted to Date Objects.
	 * 
	 * @throws NoSuchMethodException
	 */
	private void processSpecialAttribute(Object parentObj, Attribute attribute)
			throws NoSuchMethodException {
		// Create an empty object
		Object childObject = null;
		// Grab the name of the attribute
		String attributeName = attribute.getLocalName();
		// Check name and do the appropriate thing
		if (attributeName.equalsIgnoreCase("endDate")) {
			// Convert it to a data object
			childObject = dateFormat.parse(attribute.getValue());
		} else if (attributeName.equalsIgnoreCase("startDate")) {
			// Convert it to a date object
			childObject = dateFormat.parse(attribute.getValue());
		} else if ((attributeName.equalsIgnoreCase("url"))
				|| (attributeName.equalsIgnoreCase("DODSUrl"))) {
			try {
				// Convert it to a url
				childObject = new URL(attribute.getValue());
			} catch (MalformedURLException e) {
				return;
			}
		} else if (attributeName.equalsIgnoreCase("uri")) {
			try {
				childObject = new URI(attribute.getValue());
			} catch (URISyntaxException e) {
				return;
			}
		}
		// Now call the set method to set the child to the parent
		try {
			invokeSetMethod(parentObj, toSetMethodName(attribute), childObject);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			logger.error("Throwable caught: " + e.getMessage());
			buildReport.append("Throwable caught: " + e.getMessage());
			buildFailed = true;
		}
	}

	/**
	 * Handle an Attribute containing a primitive data type value
	 * 
	 * @param parentObj
	 *            The object with a property specified by attribute
	 * @param attribute
	 *            Basically a name value pair
	 * @throws InvocationTargetException
	 */
	private void processAttribute(Object parentObj, Attribute attribute)
			throws InvocationTargetException {
		// logger.debug("processAttribute called...");
		String methodName = toSetMethodName(attribute);
		Method[] methods = parentObj.getClass().getMethods();
		Method method = null;
		Object value = null;
		Class[] parameterTypes = null;
		/*
		 * Find the method specifed by name. Once the correct method is found
		 * retrieve the parameter type, then cast the attribute value to the
		 * correct type.
		 */
		for (int i = 0; i < methods.length; i++) {
			method = methods[i];
			if (methodName.equals(method.getName())) {
				parameterTypes = method.getParameterTypes();
				if (parameterTypes[0].equals(byte.class)
						|| parameterTypes[0].equals(Byte.class)) {
					value = Byte.valueOf(attribute.getValue());
				} else if (parameterTypes[0].equals(int.class)
						|| parameterTypes[0].equals(Integer.class)) {
					value = Integer.valueOf(attribute.getValue());
				} else if (parameterTypes[0].equals(short.class)
						|| parameterTypes[0].equals(Short.class)) {
					value = Short.valueOf(attribute.getValue());
				} else if (parameterTypes[0].equals(long.class)
						|| parameterTypes[0].equals(Long.class)) {
					value = Long.valueOf(attribute.getValue());
				} else if (parameterTypes[0].equals(float.class)
						|| parameterTypes[0].equals(Float.class)) {
					value = Float.valueOf(attribute.getValue());
				} else if (parameterTypes[0].equals(double.class)
						|| parameterTypes[0].equals(Double.class)) {
					value = Double.valueOf(attribute.getValue());
				} else if (parameterTypes[0].equals(char.class)
						|| parameterTypes[0].equals(Character.class)) {
					value = new Character(attribute.getValue().charAt(0));
				} else if (parameterTypes[0].equals(boolean.class)
						|| parameterTypes[0].equals(Boolean.class)) {
					value = Boolean.valueOf(attribute.getValue());
				} else if (parameterTypes[0].equals(String.class)) {
					value = attribute.getValue();
				}

				if (value != null) {
					try {
						method.invoke(parentObj, new Object[] { value });
					} catch (InvocationTargetException e) {
						// Pass that one up the stack
						buildFailed = true;
						buildReport.append(e.getTargetException().getMessage());
						logger.error("Method invoke failed: "
								+ e.getTargetException().getMessage());
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				break;
			}

		}
	}

	/**
	 * Invokes a method to set a property on some object
	 * 
	 * @param parentObj
	 *            The object whose property will be set
	 * @param methodName
	 *            The name of the methdo to invoke on parentObj
	 * @param childObj
	 *            The object representing the property
	 * @throws NoSuchMethodException
	 */
	private void invokeSetMethod(Object parentObj, String methodName,
			Object childObj) throws NoSuchMethodException {
		Class[] parameterTypes = new Class[] { childObj.getClass() };
		Object[] arguments = new Object[] { childObj };
		try {
			Method method = parentObj.getClass().getMethod(
					methodName.toString(), parameterTypes);
			method.invoke(parentObj, arguments);
			logger.debug("...completed");
		} catch (InvocationTargetException e) {
			logger.error("...FAILED: problem with " + parentObj);
		} catch (IllegalAccessException e) {
			logger.error("...FAILED: Access Denied!!");
		} catch (NoSuchMethodException e) {
			buildFailed = true;
			logger.error("NoSuchMethodException: problem with " + parentObj
					+ " trying to find method " + methodName + "("
					+ parameterTypes[0] + ")");
			buildReport.append("NoSuchMethodException: problem with "
					+ parentObj + " trying to find method " + methodName + "("
					+ parameterTypes[0] + ")");
			throw e;
		}
	}

	/**
	 * @return The correct method name to use to set the attribute value on an
	 *         object
	 */
	private String toSetMethodName(Attribute attribute) {
		return toSetMethodName(attribute.getLocalName());
	}

	/**
	 * @return The correct method name to use to set the element value on an
	 *         object
	 */
	private String toSetMethodName(Element element) {
		return toSetMethodName(element.getLocalName());

	}

	private String toSetMethodName(String paramName) {
		StringBuffer methodName = new StringBuffer();
		String firstLetter = paramName.substring(0, 1);
		methodName.append("set" + firstLetter.toUpperCase()
				+ paramName.substring(1, paramName.length()));
		return methodName.toString();
	}

	/**
	 * Add an Object, represented by an <code>Element</code> to a parent Object
	 * by calling a method other than Java Bean get and set methods.
	 * 
	 * @param parentObject
	 *            The object whos method will be invoked
	 * @param methodName
	 *            The method to invoke on the parent Object
	 * @param element
	 *            The Element representing the object to be added to the
	 *            parentObject
	 */
	private void add(Object parentObject, String methodName, Element element) {
		try {
			logger.debug("add with object, " + methodName + ", element called");
			// First create an object from the incoming element
			Object childObject = elementToObject(element);
			// Now add it to the parent object using the input method call
			add(parentObject, methodName, childObject);
		} catch (Exception e) {
			logger.info(getClass().getName()
					+ ": Unable to add the contents of " + element + " to "
					+ parentObject + " using " + methodName);
		}
	}

	/**
	 * Add one Object to a parent Object by calling a method other than Java
	 * Bean get and set methods.
	 * 
	 * @param parentObject
	 *            The object whos method will be invoked
	 * @param methodName
	 *            The method to invoke on the parent Object
	 * @param Object
	 *            The object to be added to the parentObject
	 */
	private void add(Object parentObject, String methodName, Object childObject) {
		try {
			logger.debug("add with object, " + methodName + ", object called");
			// Call the method to add and tack on the class name
			add(parentObject, methodName, childObject, childObject.getClass());
		} catch (Exception e) {
			logger.info(getClass().getName() + ": Unable to add " + childObject
					+ " to " + parentObject + " using " + methodName
					+ ". The reason is " + e.getMessage());
		}
	}

	/**
	 * Add an Object to a parent Object by calling a method other than Java Bean
	 * get and set methods.
	 * 
	 * @param parentObject
	 *            The object whos method will be invoked
	 * @param methodName
	 *            The method to invoke on the parent Object
	 * @param Object
	 *            The object to be added to the parentObject
	 * @param childClass
	 *            the Class for the child object
	 */
	private void add(Object parentObject, String methodName,
			Object childObject, Class childClass) {
		try {
			// Create an array of classes with one class which is the
			// class of the object to be added
			Class[] c = new Class[] { childClass };
			// Grab the method from the parent object which matches
			// the method name that was an input parameter
			Method m = parentObject.getClass().getMethod(methodName, c);
			// Now invoke the method on the parent and hand it the array
			// of objects
			m.invoke(parentObject, new Object[] { childObject });
			// } catch (InvocationTargetException e) {
			// logger.error("Unable to add the child object to the parent
			// object: " + e.getMessage());
			// Throwable t = e.getTargetException();
			// e.printStackTrace();
		} catch (Exception e) {
		}
	}

	/**
	 * This method does some special handling
	 * 
	 * @param parentObject
	 * @param element
	 * @throws Exception
	 */
	private void addChildDataProducer(Object parentObject, Element element)
			throws Exception {
		logger.debug("addChildDataProducer called with parentObject = "
				+ parentObject + " and element = " + element);
		// Create empty object
		Object childObject = null;
		// Convert it to an object
		childObject = elementToObject(element);
		// Now the parent and the child objects should be a DataProducers,
		// so cast them to use ther methods to check for something
		DataProducer parentDataProducer = null;
		DataProducer tempChildDataProducer = null;
		if (parentObject instanceof DataProducer) {
			parentDataProducer = (DataProducer) parentObject;
		}
		if (childObject instanceof DataProducer) {
			tempChildDataProducer = (DataProducer) childObject;
		}
		// Now check if it exists
		if ((parentDataProducer != null)
				&& (tempChildDataProducer != null)
				&& (parentDataProducer.getChildDataProducers()
						.contains(tempChildDataProducer))) {
			logger.debug("Looks like the parent data producer already "
					+ "contains a child data producer with the same "
					+ "name, so will change the name");
			tempChildDataProducer.setName(tempChildDataProducer.getName() + "("
					+ childDataProducerCounter + ")");
			childDataProducerCounter++;
		}
		// Add it to the parent using the addInput method
		add(parentObject, "addChildDataProducer", childObject,
				DataProducer.class);
	}

	/**
	 * This method adds the element parameter to the parentObject using a method
	 * called addInput
	 * 
	 * @param parentObject
	 *            The Object that has <code>addInput()</code> methods
	 * @param element
	 *            The "input" element that contains various subclasses of
	 *            <code>DataContainer</code>s. All Object elements below
	 *            <input>will be added to the parent object.
	 */
	private void addInput(Object parentObject, Element element)
			throws Exception {
		logger.debug("addInput called with parentObject = " + parentObject
				+ " and element = " + element);
		// Create empty object
		Object childObject = null;
		// Grab all the child elements
		Elements children = element.getChildElements();
		// Create an empty child element
		Element child = null;
		// Loop through array of child elements
		for (int i = 0; i < children.size(); i++) {
			// Grab the ith child
			child = children.get(i);
			// Convert it to an object
			childObject = elementToObject(child);
			// Add it to the parent using the addInput method
			add(parentObject, "addInput", childObject, DataContainer.class);
		}
	}

	/**
	 * This method adds the element parameter to the parentObject using a method
	 * called addOutput
	 * 
	 * @param parentObject
	 *            The Object that has <code>addOutput()</code> methods
	 * @param element
	 *            The "output" element that contains various subclasses of
	 *            <code>DataContainer</code>s. All Object elements below
	 *            <output>will be added to the parent object.
	 */
	private void addOutput(Object parentObject, Element element)
			throws Exception {
		logger.debug("addOutput called with parentObject = " + parentObject
				+ " and element = " + element);

		// Create an empty object
		Object childObject = null;
		// Grab all the child element
		Elements children = element.getChildElements();
		// Create an emtpy child element
		Element child = null;
		// Now loop through the child elements
		for (int i = 0; i < children.size(); i++) {
			// Grab the ith element
			child = children.get(i);
			// Convert that element to an object
			childObject = elementToObject(child);
			// Now add it to the parent by calling add with addOutput method
			add(parentObject, "addOutput", childObject, DataContainer.class);
		}
	}

	/**
	 * This method adds the element parameter to the parentObject using a method
	 * called addConsumer
	 * 
	 * @param parentObject
	 *            The Object that has <code>addConsumer()</code> methods
	 * @param element
	 *            The "consumer" element that contains various subclasses of
	 *            <code>DataContainer</code>s.
	 */
	private void addConsumer(Object parentObject, Element element)
			throws Exception {
		logger.debug("addConsumer called with parentObject = " + parentObject
				+ " and element = " + element);
		// Create an emtpy object
		Object childObject = null;
		// Get all the child elements
		Elements children = element.getChildElements();
		// Create an empty child element
		Element child = null;
		// Loop through child elements
		for (int i = 0; i < children.size(); i++) {
			// Grab the ith child
			child = children.get(i);
			// Convert it to an object
			childObject = elementToObject(child);
			// Add it to the parent using the addConsumer method
			add(parentObject, "addConsumer", childObject, DataProducer.class);
		}

	}

	private Collection retrieveObjects(String className) {
		Collection out = null;
		try {
			out = (Collection) metadataObjects.get(Class.forName(className));
		} catch (ClassNotFoundException e) {
			System.err.println(getClass().getName() + ": Unable to load class "
					+ className);
			// e.printStackTrace();
		}
		if (out == null) {
			out = new ArrayList();
		}
		return out;
	}

	private String convertNameIfNeeded(String name) {
		if (name.equalsIgnoreCase("Deployment"))
			return "DataProducer";
		if (name.equalsIgnoreCase("ProcessRun"))
			return "DataProducer";
		if (name.equalsIgnoreCase("DataFile"))
			return "DataContainer";
		if (name.equalsIgnoreCase("DataStream"))
			return "DataContainer";
		return name;
	}

	/**
	 * @return All objects generated from the XML
	 */
	public Collection listAll() {
		Collection out = new ArrayList();
		Collection cs = metadataObjects.values();
		Iterator i = cs.iterator();
		while (i.hasNext()) {
			out.addAll((Collection) i.next());
		}
		return out;
	}

	public Collection listDataProducers() {
		return retrieveObjects(modelPackage + ".DataProducer");
	}

	public Collection listDevices() {
		return retrieveObjects(modelPackage + ".Device");
	}

}