package moos.ssds.metadata.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

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
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import org.apache.log4j.Logger;

/**
 * <p>
 * Generate XML from the model
 * </p>
 * Use As:
 * 
 * <pre>
 *     // Get an instance 
 *     XmlBuilder xb = new XmlBuilder(); 
 *     // Add the elements that are to appear just below the root of the XML doc. 
 *     // Here we just add an instance of DataProducer 
 *     xb.add(aDataProducer); 
 *     // Set whether or not you want legaacy XML format or not (ususally not)
 *     xb.setLegacyFormat(false);  // true means it will print out the old style XML
 *     // Transform the object graph to xml 
 *     xb.marshal(); 
 *     // view it in a console. 
 *     xb.print();
 *     // write it to an OutputStream 
 *     xb.print(anOutputStream);
 *     // Write out to a file.
 *     xb.toFile(aFile)
 * </pre>
 * 
 * <p>
 * <code>XmlBuilder</code> is used for marshalling XML from objects.
 * </p>
 * <hr>
 * 
 * @author : $Author: mccann $
 * @version : $Revision: 1.1.2.7 $
 * @testcase test.moos.ssds.model.TestXmlBuilder
 */
public class XmlBuilder {

	/**
	 * A Log4JLogger
	 */
	private static Logger logger = Logger.getLogger(XmlBuilder.class);

	/**
	 * This is the XOM Document that will be used during serialization
	 */
	private Document document;

	/**
	 * Collection of Objects that could be elements just below the root of the
	 * XML document (i.e Device, Deployment, DataProducers (and subclasses).
	 */
	private Collection<Object> rootElements = new ArrayList<Object>();

	/**
	 * An XMLDateFormat to use in writing out the XML
	 */
	private XmlDateFormat dateFormat = new XmlDateFormat();

	/**
	 * This is a flag that indicates if the marshalling should output the XML in
	 * the legacy format which using Deployment/ProcessRun instead of
	 * DataProducer and DataFile/DataStream instead of DataContainer
	 */
	private boolean legacyFormat = false;

	/**
	 * The default constructor
	 */
	public XmlBuilder() {

		// Create the <Metadata> root element
		Element root = new Element("Metadata");

		root.addNamespaceDeclaration("xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		root.addAttribute(new Attribute(
				"xsi:noNamespaceSchemaLocation",
				"http://www.w3.org/2001/XMLSchema-instance",
				"http://shore-side-data-system.googlecode.com/svn/trunk/src/xml/SSDS_Metadata.xsd"));

		// Create a new XOM Document using the Metadata tag as the root
		document = new Document(root);
	}

	/**
	 * This method sets the flag that tells the marshalling to build legacy
	 * style XML
	 * 
	 * @param legacyFormat
	 */
	public void setLegacyFormat(boolean legacyFormat) {
		this.legacyFormat = legacyFormat;
	}

	/**
	 * This method returns the flag that indicates if the builder will marshall
	 * in legacy format
	 * 
	 * @return
	 */
	public boolean isLegacyFormat() {
		return this.legacyFormat;
	}

	/**
	 * The method to return the XOM document that was built after unmarshalling
	 * 
	 * @return The XOM Document
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * This method adds a <code>IMetadataObject</code> to the list of elements
	 * to be serialized out under the Metadata tag
	 * 
	 * @param obj
	 */
	public void add(Object obj) {
		rootElements.add(obj);
	}

	/**
	 * Method to remove a <code>IMetadataObject</code> from the list of elements
	 * to serialize
	 * 
	 * @param obj
	 */
	public void remove(Object obj) {
		rootElements.remove(obj);
	}

	/**
	 * A method to add a collection of objects to the the list of elements to be
	 * serialized out under Metadata tag
	 * 
	 * @param c
	 */
	public void addAll(Collection<Object> c) {
		rootElements.addAll(c);
	}

	/**
	 * A method to remove a collection of objects from the collection to be
	 * serialized
	 * 
	 * @param c
	 */
	public void removeAll(Collection<Object> c) {
		rootElements.removeAll(c);
	}

	/**
	 * This is the method to return the collection of objects to be serialized
	 * out under the Metadata tag
	 * 
	 * @return
	 */
	public Collection<Object> getRootElements() {
		return rootElements;
	}

	/**
	 * The method to simply return the XOM document as test
	 * 
	 * @return The XML document as text
	 */
	public String toString() {
		return document.toString();
	}

	/**
	 * This method takes the XOM document converts it to XML and returns the
	 * String
	 * 
	 * @return The XML document as text
	 */
	public String toXML() {
		return document.toXML();
	}

	/**
	 * This method returns the XOM document as a formatted (indents and line
	 * breaks) string of XML
	 * 
	 * @return The XOM document as a formatted (indents & line breaks) string
	 */
	public String toFormattedXML() throws IOException,
			UnsupportedEncodingException {

		// Create a output stream to buffer the results of the serialization
		ByteArrayOutputStream buf = new ByteArrayOutputStream();

		// Create the serializer to serialize the document
		Serializer serializer = new Serializer(buf, "ISO-8859-1");

		// Set some configuration parameters
		serializer.setIndent(4);
		serializer.setMaxLength(65536);

		// Now serialize to the buffered output stream
		serializer.write(document);

		// Return the results as a String
		return buf.toString();
	}

	/**
	 * Write formated xml to a file using "ISO-8859-1" encoding
	 * 
	 * @param file
	 *            The file to write into.
	 */
	public void toFile(java.io.File file) throws IOException,
			UnsupportedEncodingException {
		// Call the method to write to the file
		toFile(file, "ISO-8859-1");
	}

	/**
	 * Write formatted XML to a file using the specified encoding
	 * 
	 * @param file
	 *            The file to write into.
	 * @param encoding
	 *            the encoding to use
	 */
	public void toFile(java.io.File file, String encoding) throws IOException,
			UnsupportedEncodingException {
		// Create the output stream that points to the file
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

		// Print the XML using the endcoding to the file
		print(out, encoding);

		// Close the output stream
		out.close();
	}

	/**
	 * The method to print the XML to the supplied output stream using the
	 * ISO-8859-1 encoding
	 * 
	 * @param out
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public void print(OutputStream out) throws IOException,
			UnsupportedEncodingException {
		print(out, "ISO-8859-1");
	}

	/**
	 * Write the formated XML out to a stream
	 * 
	 * @param out
	 *            An output stream to write to
	 * @param encoding
	 *            the encoding to use
	 */
	public void print(OutputStream out, String encoding) throws IOException,
			UnsupportedEncodingException {

		// Create the XOM Serializer to use
		Serializer serializer = new Serializer(out, encoding);

		// Set some configuration settings
		serializer.setIndent(4);
		serializer.setMaxLength(65536);

		// Write the document to the output stream
		serializer.write(document);
	}

	/**
	 * Print the XMl document to System.out
	 */
	public void print() throws IOException, UnsupportedEncodingException {
		print(System.out);
	}

	/**
	 * Generates XML tree. Use this after adding all the objects to be
	 * marshalled.
	 * 
	 * <pre>
	 * XmlBuilder xb = new XmlBuilder();
	 * xb.addDeployment(aDeployment);
	 * xb.setLegacyFormat(false);
	 * 
	 * // Generate the xml tree
	 * xb.marshal();
	 * 
	 * // do something with the xml tree. Here we dump it to stdout
	 * xb.print();
	 * </pre>
	 */
	public void marshal() {
		// Clear the root element (Metadata)
		document.getRootElement().removeChildren();

		// Now iterate over the objects that were added
		Iterator<Object> i = rootElements.iterator();
		while (i.hasNext()) {
			// Grab the object
			Object obj = i.next();
			if (obj != null) {
				try {
					// Convert the object to an XML element
					Element element = objectToElement(obj);

					// If it looks OK, add it the the <Metadata> element
					if (element != null && !isEmptyElement(element)) {
						document.getRootElement().appendChild(element);
					}
				} catch (Exception e) {
					logger.error("Exception caught trying to serialize "
							+ "Metadata objects to XML:" + e.getMessage());
				}
			}
		}
	}

	/**
	 * Checks to see if an element has child nodes or attributes. If it doesn't
	 * the element corresponds to an empty object and so is generally not
	 * written out to XML.
	 */
	private boolean isEmptyElement(Element element) {
		boolean ok = true;
		if ((element != null)
				&& ((element.getChildCount() > 0) || (element
						.getAttributeCount() > 0))) {
			ok = false;
		}
		return ok;
	}

	/**
	 * Convert an object to its corresponding XML element
	 * 
	 * @return An element representing a given object.
	 */
	private Element objectToElement(Object parentObject) {

		// Check the class type and call the appropriate method
		if (parentObject instanceof CommentTag) {
			return commentTagToElement((CommentTag) parentObject);
		} else if (parentObject instanceof DataContainer) {
			return dataContainerToElement((DataContainer) parentObject);
		} else if (parentObject instanceof DataContainerGroup) {
			return dataContainerGroupToElement((DataContainerGroup) parentObject);
		} else if (parentObject instanceof DataProducer) {
			return dataProducerToElement((DataProducer) parentObject);
		} else if (parentObject instanceof DataProducerGroup) {
			return dataProducerGroupToElement((DataProducerGroup) parentObject);
		} else if (parentObject instanceof Device) {
			return deviceToElement((Device) parentObject);
		} else if (parentObject instanceof DeviceType) {
			return deviceTypeToElement((DeviceType) parentObject);
		} else if (parentObject instanceof Event) {
			return eventToElement((Event) parentObject);
		} else if (parentObject instanceof HeaderDescription) {
			return headerDescriptionToElement((HeaderDescription) parentObject);
		} else if (parentObject instanceof Keyword) {
			return keywordToElement((Keyword) parentObject);
		} else if (parentObject instanceof Person) {
			return personToElement((Person) parentObject);
		} else if (parentObject instanceof RecordDescription) {
			return recordDescriptionToElement((RecordDescription) parentObject);
		} else if (parentObject instanceof RecordVariable) {
			return recordVariableToElement((RecordVariable) parentObject);
		} else if (parentObject instanceof Resource) {
			return resourceToElement((Resource) parentObject);
		} else if (parentObject instanceof ResourceBLOB) {
			return resourceBLOBToElement((ResourceBLOB) parentObject);
		} else if (parentObject instanceof ResourceType) {
			return resourceTypeToElement((ResourceType) parentObject);
		} else if (parentObject instanceof Software) {
			return softwareToElement((Software) parentObject);
		} else if (parentObject instanceof StandardDomain) {
			return standardDomainToElement((StandardDomain) parentObject);
		} else if (parentObject instanceof StandardKeyword) {
			return standardKeywordToElement((StandardKeyword) parentObject);
		} else if (parentObject instanceof StandardReferenceScale) {
			return standardReferenceScaleToElement((StandardReferenceScale) parentObject);
		} else if (parentObject instanceof StandardUnit) {
			return standardUnitToElement((StandardUnit) parentObject);
		} else if (parentObject instanceof StandardVariable) {
			return standardVariableToElement((StandardVariable) parentObject);
		} else if (parentObject instanceof UserGroup) {
			return userGroupToElement((UserGroup) parentObject);
		} else {
			logger.error("Incoming object was not recognized and was "
					+ "not converted to an XML Element");
			logger.error("Object is " + parentObject);
			return null;
		}
	}

	/**
	 * This method takes in a <code>CommentTag</code> object and returns an
	 * <code>Element</code> that represents that object
	 * 
	 * @param commentTag
	 *            the CommentTag that will be converted to XML
	 * @return
	 */
	private Element commentTagToElement(CommentTag commentTag) {
		// Create the new Element
		Element element = new Element("CommentTag");

		// Make sure incoming object is there
		if (commentTag != null) {
			// If the objects has attributes, add them to the element
			if (commentTag.getId() != null)
				element.addAttribute(new Attribute("id", commentTag.getId()
						.toString()));
			if (commentTag.getTagString() != null)
				element.addAttribute(new Attribute("tagString", commentTag
						.getTagString()));
		}

		// Return the element
		return element;
	}

	/**
	 * This method takes in <code>DataContainer</code> and converts it to a XOM
	 * Element
	 * 
	 * @param dataContainer
	 * @return
	 */
	private Element dataContainerToElement(DataContainer dataContainer) {
		// Create the data container element
		Element element = null;

		// Make sure incoming object is there
		if (dataContainer != null) {

			// Check to see if the marshalling is set to legacy
			if (legacyFormat) {
				// Check the type
				if (dataContainer.getDataContainerType().equalsIgnoreCase(
						DataContainer.TYPE_FILE)) {
					element = new Element("DataFile");
				} else if (dataContainer.getDataContainerType()
						.equalsIgnoreCase(DataContainer.TYPE_STREAM)) {
					element = new Element("DataStream");
				} else {
					// The most common is probably File
					element = new Element("DataFile");
				}
			} else {
				element = new Element("DataContainer");
				if (dataContainer.getDataContainerType() != null) {
					element.addAttribute(new Attribute("dataContainerType",
							dataContainer.getDataContainerType()));
				} else {
					// Choose the most common type
					element.addAttribute(new Attribute("dataContainerType",
							"File"));
				}
			}

			// Add all attributes
			if (dataContainer.getId() != null)
				element.addAttribute(new Attribute("id", dataContainer.getId()
						.toString()));
			if (dataContainer.getName() != null)
				element.addAttribute(new Attribute("name", dataContainer
						.getName()));
			if (dataContainer.getStartDate() != null)
				element.addAttribute(new Attribute("startDate", dateFormat
						.format(dataContainer.getStartDate())));
			if (dataContainer.getEndDate() != null)
				element.addAttribute(new Attribute("endDate", dateFormat
						.format(dataContainer.getEndDate())));
			if (dataContainer.isOriginal() != null)
				element.addAttribute(new Attribute("original", dataContainer
						.isOriginal().toString()));
			if (dataContainer.getUriString() != null) {
				if (legacyFormat) {
					element.addAttribute(new Attribute("url", dataContainer
							.getUriString()));
				} else {
					element.addAttribute(new Attribute("uri", dataContainer
							.getUriString()));
				}
			}
			if (dataContainer.getContentLength() != null)
				element.addAttribute(new Attribute("contentLength",
						dataContainer.getContentLength().toString()));
			if (dataContainer.getMimeType() != null)
				element.addAttribute(new Attribute("mimeType", dataContainer
						.getMimeType()));
			if (dataContainer.getNumberOfRecords() != null)
				element.addAttribute(new Attribute("numberOfRecords",
						dataContainer.getNumberOfRecords().toString()));
			if (dataContainer.isDodsAccessible() != null)
				element.addAttribute(new Attribute("dodsAccessible",
						dataContainer.isDodsAccessible().toString()));
			if (dataContainer.getDodsUrlString() != null)
				element.addAttribute(new Attribute("dodsUrl", dataContainer
						.getDodsUrlString()));
			if (dataContainer.isNoNetCDF() != null && !legacyFormat)
				element.addAttribute(new Attribute("noNetCDF", dataContainer
						.isNoNetCDF().toString()));
			if (dataContainer.getMinLatitude() != null)
				element.addAttribute(new Attribute("minLatitude", dataContainer
						.getMinLatitude().toString()));
			if (dataContainer.getMaxLatitude() != null)
				element.addAttribute(new Attribute("maxLatitude", dataContainer
						.getMaxLatitude().toString()));
			if (dataContainer.getMinLongitude() != null)
				element.addAttribute(new Attribute("minLongitude",
						dataContainer.getMinLongitude().toString()));
			if (dataContainer.getMaxLongitude() != null)
				element.addAttribute(new Attribute("maxLongitude",
						dataContainer.getMaxLongitude().toString()));
			if (dataContainer.getMinDepth() != null)
				element.addAttribute(new Attribute("minDepth", dataContainer
						.getMinDepth().toString()));
			if (dataContainer.getMaxDepth() != null)
				element.addAttribute(new Attribute("maxDepth", dataContainer
						.getMaxDepth().toString()));

			// Now for the relationships. Unfortunately, with schemas, the order
			// does matter here. There are three types of DataContainers:
			// DataContainer, DataFile (legacy) and DataStream (legacy). The
			// order of relationships for the three are:
			//
			// DataContainer:
			// -consumer
			// -description
			// -DataContainerGroup
			// -HeaderDescription
			// -Keyword
			// -Person
			// -RecordDescription
			// -Resource
			//
			// DataFile:
			// -destiny
			// -Person
			// -Resource
			// -HeaderDescription
			// -description
			// -RecordDescription
			//
			// DataStream:
			// -destiny
			// -Person
			// -Resource
			// -description
			// -RecordDescription
			//
			// The code below enforces the correct order so all generated XML
			// will validate

			// Simply create the elements first and we can order them later
			Element descriptionElement = null;
			ArrayList<Element> consumersDestiny = new ArrayList<Element>();
			ArrayList<Element> dataContainerGroups = new ArrayList<Element>();
			Element headerDescriptionElement = null;
			ArrayList<Element> keywords = new ArrayList<Element>();
			Element personElement = null;
			Element recordDescriptionElement = null;
			ArrayList<Element> resources = new ArrayList<Element>();

			// Description
			if (dataContainer.getDescription() != null) {
				descriptionElement = new Element("description");
				descriptionElement.appendChild(dataContainer.getDescription());
			}

			// Consumers. Note that we do this here instead of doing it as
			// inputs to DataProducers as the direction makes more sense.
			if (dataContainer.getConsumers() != null
					&& dataContainer.getConsumers().size() > 0) {
				// Add the consumer element
				Iterator<DataProducer> iterator = dataContainer.getConsumers()
						.iterator();
				while (iterator.hasNext()) {
					Element consumerElement = null;
					if (legacyFormat) {
						consumerElement = new Element("destiny");
					} else {
						consumerElement = new Element("consumer");
					}
					Element dataProducerElement = dataProducerToElement(iterator
							.next());
					if (dataProducerElement != null
							&& !isEmptyElement(dataProducerElement)) {
						consumerElement.appendChild(dataProducerElement);
						consumersDestiny.add(consumerElement);
					}
				}
			}

			// DataContainerGroups
			if (dataContainer.getDataContainerGroups() != null
					&& dataContainer.getDataContainerGroups().size() > 0) {
				Iterator<DataContainerGroup> iterator = dataContainer
						.getDataContainerGroups().iterator();
				while (iterator.hasNext()) {
					// Add an element that represents the DataContainer group
					Element dataContainerGroupElement = dataContainerGroupToElement(iterator
							.next());
					if (dataContainerGroupElement != null
							&& !isEmptyElement(dataContainerGroupElement))
						dataContainerGroups.add(dataContainerGroupElement);
				}
			}

			// Header Descriptions
			if (dataContainer.getHeaderDescription() != null) {
				Element tempHeaderDescriptionElement = headerDescriptionToElement(dataContainer
						.getHeaderDescription());
				if (tempHeaderDescriptionElement != null
						&& !isEmptyElement(tempHeaderDescriptionElement))
					headerDescriptionElement = tempHeaderDescriptionElement;
			}

			// Keywords
			if (dataContainer.getKeywords() != null
					&& dataContainer.getKeywords().size() > 0) {
				Iterator<Keyword> iterator = dataContainer.getKeywords()
						.iterator();
				while (iterator.hasNext()) {
					Element keywordElement = keywordToElement(iterator.next());
					if (keywordElement != null
							&& !isEmptyElement(keywordElement))
						keywords.add(keywordElement);
				}
			}

			// Person
			if (dataContainer.getPerson() != null) {
				Element tempPersonElement = personToElement(dataContainer
						.getPerson());
				if (tempPersonElement != null
						&& !isEmptyElement(tempPersonElement)) {
					personElement = tempPersonElement;
				}
			}

			// RecordDescription
			if (dataContainer.getRecordDescription() != null) {
				Element tempRecordDescriptionElement = recordDescriptionToElement(dataContainer
						.getRecordDescription());
				if (tempRecordDescriptionElement != null
						&& !isEmptyElement(tempRecordDescriptionElement))
					recordDescriptionElement = tempRecordDescriptionElement;
			}

			// Resources
			if (dataContainer.getResources() != null
					&& dataContainer.getResources().size() > 0) {
				Iterator<Resource> iterator = dataContainer.getResources()
						.iterator();
				while (iterator.hasNext()) {
					Element resourceElement = resourceToElement(iterator.next());
					if (resourceElement != null
							&& !isEmptyElement(resourceElement))
						resources.add(resourceElement);
				}
			}

			// Now let's put them in the right order
			if (legacyFormat) {
				// Destiny first
				if (consumersDestiny.size() > 0) {
					Iterator<Element> iterator = consumersDestiny.iterator();
					while (iterator.hasNext())
						element.appendChild(iterator.next());
				}
				// Person
				if (personElement != null)
					element.appendChild(personElement);
				// Resources
				if (resources.size() > 0) {
					Iterator<Element> iterator = resources.iterator();
					while (iterator.hasNext())
						element.appendChild(iterator.next());
				}
				// Which type of DC
				if (dataContainer.getDataContainerType().equalsIgnoreCase(
						DataContainer.TYPE_FILE)
						&& headerDescriptionElement != null) {
					element.appendChild(headerDescriptionElement);
				}
				// Then description
				if (descriptionElement != null)
					element.appendChild(descriptionElement);
				// RecordDescription
				if (recordDescriptionElement != null)
					element.appendChild(recordDescriptionElement);
			} else {
				// Consumers come first
				if (consumersDestiny.size() > 0) {
					Iterator<Element> iterator = consumersDestiny.iterator();
					while (iterator.hasNext())
						element.appendChild(iterator.next());
				}
				// Then description
				if (descriptionElement != null)
					element.appendChild(descriptionElement);
				// DataContainerGroups
				if (dataContainerGroups.size() > 0) {
					Iterator<Element> iterator = dataContainerGroups.iterator();
					while (iterator.hasNext())
						element.appendChild(iterator.next());
				}
				// HeaderDescription
				if (headerDescriptionElement != null)
					element.appendChild(headerDescriptionElement);
				// Keywords
				if (keywords.size() > 0) {
					Iterator<Element> iterator = keywords.iterator();
					while (iterator.hasNext())
						element.appendChild(iterator.next());
				}
				// Person
				if (personElement != null)
					element.appendChild(personElement);
				// RecordDescription
				if (recordDescriptionElement != null)
					element.appendChild(recordDescriptionElement);
				// Resources
				if (resources.size() > 0) {
					Iterator<Element> iterator = resources.iterator();
					while (iterator.hasNext())
						element.appendChild(iterator.next());
				}
			}
		}

		// Return the result
		return element;
	}

	/**
	 * This method takes in a <code>DataContainerGroup</code> object and returns
	 * an <code>Element</code> that represents that object
	 * 
	 * @param dataContainerGroup
	 * @return
	 */
	private Element dataContainerGroupToElement(
			DataContainerGroup dataContainerGroup) {
		Element element = null;
		// Do a sanity check first
		if (dataContainerGroup != null && dataContainerGroup.getName() != null) {
			// Create the element
			element = new Element("DataContainerGroup");
			// Set the id if it exists
			if (dataContainerGroup.getId() != null)
				element.addAttribute(new Attribute("id", dataContainerGroup
						.getId().toString()));
			// Set the name
			element.addAttribute(new Attribute("name", dataContainerGroup
					.getName()));
			// Add description element if there is one
			if (dataContainerGroup.getDescription() != null) {
				Element descriptionElement = new Element("description");
				descriptionElement.appendChild(dataContainerGroup
						.getDescription());
				element.appendChild(descriptionElement);
			}
		}
		return element;
	}

	/**
	 * This method takes in a <code>DataProducer</code> and creates an Element
	 * to represent it in XML
	 * 
	 * @param dataProducer
	 * @return
	 */
	private Element dataProducerToElement(DataProducer dataProducer) {
		// Element to return
		Element element = null;

		// Do a sanity check
		if (dataProducer != null) {
			// Create the new element depending on the legacy flag
			if (legacyFormat) {
				// Set to most common
				element = new Element("Deployment");
				if (dataProducer.getDataProducerType() != null) {
					if (dataProducer.getDataProducerType().equalsIgnoreCase(
							DataProducer.TYPE_PROCESS_RUN)) {
						// Override since it is process run
						element = new Element("ProcessRun");
					}
				}
			} else {
				element = new Element("DataProducer");
				// Create a default attribute and change if necessary
				Attribute typeAttribute = new Attribute("dataProducerType",
						DataProducer.TYPE_DEPLOYMENT);
				if (dataProducer.getDataProducerType() != null) {
					if (dataProducer.getDataProducerType().equalsIgnoreCase(
							DataProducer.TYPE_PROCESS_RUN)) {
						element.addAttribute(new Attribute("dataProducerType",
								DataProducer.TYPE_PROCESS_RUN));
					}
				}
				// Add the attribute
				element.addAttribute(typeAttribute);
			}

			// We should now have the element
			if (element != null) {
				// Add the attributes
				if (dataProducer.getId() != null) {
					element.addAttribute(new Attribute("id", dataProducer
							.getId().toString()));
				}
				if (dataProducer.getName() != null) {
					element.addAttribute(new Attribute("name", dataProducer
							.getName()));
				}
				if (dataProducer.getStartDate() != null)
					element.addAttribute(new Attribute("startDate", dateFormat
							.format(dataProducer.getStartDate())));
				if (dataProducer.getEndDate() != null)
					element.addAttribute(new Attribute("endDate", dateFormat
							.format(dataProducer.getEndDate())));
				if (dataProducer.getRole() != null)
					element.addAttribute(new Attribute("role", dataProducer
							.getRole()));
				if (dataProducer.getNominalLatitude() != null)
					element.addAttribute(new Attribute("nominalLatitude",
							dataProducer.getNominalLatitude().toString()));
				if (dataProducer.getNominalLatitudeAccuracy() != null)
					element.addAttribute(new Attribute(
							"nominalLatitudeAccuracy", dataProducer
									.getNominalLatitudeAccuracy().toString()));
				if (dataProducer.getNominalLongitude() != null)
					element.addAttribute(new Attribute("nominalLongitude",
							dataProducer.getNominalLongitude().toString()));
				if (dataProducer.getNominalLongitudeAccuracy() != null)
					element.addAttribute(new Attribute(
							"nominalLongitudeAccuracy", dataProducer
									.getNominalLongitudeAccuracy().toString()));
				if (dataProducer.getNominalDepth() != null)
					element.addAttribute(new Attribute("nominalDepth",
							dataProducer.getNominalDepth().toString()));
				if (dataProducer.getNominalDepthAccuracy() != null)
					element.addAttribute(new Attribute("nominalDepthAccuracy",
							dataProducer.getNominalDepthAccuracy().toString()));
				if (dataProducer.getNominalBenthicAltitude() != null)
					element.addAttribute(new Attribute(
							"nominalBenthicAltitude", dataProducer
									.getNominalBenthicAltitude().toString()));
				if (dataProducer.getNominalBenthicAltitudeAccuracy() != null)
					element.addAttribute(new Attribute(
							"nominalBenthicAltitudeAccuracy", dataProducer
									.getNominalBenthicAltitudeAccuracy()
									.toString()));
				if (dataProducer.getXoffset() != null)
					element.addAttribute(new Attribute("xOffset", dataProducer
							.getXoffset().toString()));
				if (dataProducer.getYoffset() != null)
					element.addAttribute(new Attribute("yOffset", dataProducer
							.getYoffset().toString()));
				if (dataProducer.getZoffset() != null)
					element.addAttribute(new Attribute("zOffset", dataProducer
							.getZoffset().toString()));
				if (dataProducer.getOrientationDescription() != null)
					element.addAttribute(new Attribute(
							"orientationDescription", dataProducer
									.getOrientationDescription()));
				if (dataProducer.getX3DOrientationText() != null)
					element.addAttribute(new Attribute("x3DOrientationText",
							dataProducer.getX3DOrientationText()));
				if (dataProducer.getHostName() != null) {
					element.addAttribute(new Attribute("hostName", dataProducer
							.getHostName()));
				}

				// Add relationships
				// DP:
				// -DataProducer
				// -DataProducerGroup
				// -description
				// -Device
				// -Event
				// -Keyword
				// -output
				// -Person
				// -Resource
				// -Software
				//
				// D:
				// -Device
				// -Deployment
				// -Person
				// -description
				// -Resource
				// -Event
				// -output
				//
				// PR:
				// -description
				// -Person
				// -Software
				// -Resource
				// -Event
				// -output

				// Just create all the elements first before adding them
				Element deviceElement = null;
				ArrayList<Element> childDataProducerElements = new ArrayList<Element>();
				ArrayList<Element> dataProducerGroups = new ArrayList<Element>();
				Element descriptionElement = null;
				ArrayList<Element> events = new ArrayList<Element>();
				ArrayList<Element> keywords = new ArrayList<Element>();
				ArrayList<Element> outputs = new ArrayList<Element>();
				Element personElement = null;
				ArrayList<Element> resources = new ArrayList<Element>();
				Element softwareElement = null;

				// Device
				if (dataProducer.getDevice() != null) {
					Element tempDeviceElement = deviceToElement(dataProducer
							.getDevice());
					if (tempDeviceElement != null
							&& !isEmptyElement(tempDeviceElement))
						deviceElement = tempDeviceElement;
				}

				// Child data producers
				if (dataProducer.getChildDataProducers() != null
						&& dataProducer.getChildDataProducers().size() > 0) {
					Iterator<DataProducer> iterator = dataProducer
							.getChildDataProducers().iterator();
					while (iterator.hasNext()) {
						Element dataProducerElement = dataProducerToElement(iterator
								.next());
						if (dataProducerElement != null
								&& !isEmptyElement(dataProducerElement))
							childDataProducerElements.add(dataProducerElement);
					}
				}

				// DataProducerGroups
				if (dataProducer.getDataProducerGroups() != null
						&& dataProducer.getDataProducerGroups().size() > 0) {
					Iterator<DataProducerGroup> iterator = dataProducer
							.getDataProducerGroups().iterator();
					while (iterator.hasNext()) {
						Element dataProducerGroupElement = dataProducerGroupToElement(iterator
								.next());
						if (dataProducerGroupElement != null
								&& !isEmptyElement(dataProducerGroupElement))
							dataProducerGroups.add(dataProducerGroupElement);
					}
				}

				// Add description element if there is one
				if (dataProducer.getDescription() != null) {
					descriptionElement = new Element("description");
					descriptionElement.appendChild(dataProducer
							.getDescription());
				}

				// Event
				if (dataProducer.getEvents() != null
						&& dataProducer.getEvents().size() > 0) {
					Iterator<Event> iterator = dataProducer.getEvents()
							.iterator();
					while (iterator.hasNext()) {
						Element eventElement = eventToElement(iterator.next());
						if (eventElement != null
								&& !isEmptyElement(eventElement))
							events.add(eventElement);
					}
				}

				// NOTE: We ignore inputs here as they would cause recursion
				// from DataContainers.consumers. Since the graph direction
				// makes more sense to go through the DataContainers consumers,
				// we do it there.

				// Keywords
				if (dataProducer.getKeywords() != null
						&& dataProducer.getKeywords().size() > 0) {
					Iterator<Keyword> iterator = dataProducer.getKeywords()
							.iterator();
					while (iterator.hasNext()) {
						Element keywordElement = keywordToElement(iterator
								.next());
						if (keywordElement != null
								&& !isEmptyElement(keywordElement))
							keywords.add(keywordElement);
					}
				}

				// Outputs
				if (dataProducer.getOutputs() != null
						&& dataProducer.getOutputs().size() > 0) {
					Element outputElement = new Element("output");
					Iterator<DataContainer> iterator = dataProducer
							.getOutputs().iterator();
					while (iterator.hasNext()) {
						Element dataContainerElement = dataContainerToElement(iterator
								.next());
						if (dataContainerElement != null
								&& !isEmptyElement(dataContainerElement)) {
							outputElement.appendChild(dataContainerElement);
							outputs.add(outputElement);
						}
					}
				}

				// Person
				if (dataProducer.getPerson() != null) {
					Element tempPersonElement = personToElement(dataProducer
							.getPerson());
					if (tempPersonElement != null
							&& !isEmptyElement(tempPersonElement)) {
						personElement = tempPersonElement;
					}
				}

				// Resources
				if (dataProducer.getResources() != null
						&& dataProducer.getResources().size() > 0) {
					Iterator<Resource> iterator = dataProducer.getResources()
							.iterator();
					while (iterator.hasNext()) {
						Element resourceElement = resourceToElement(iterator
								.next());
						if (resourceElement != null
								&& !isEmptyElement(resourceElement))
							resources.add(resourceElement);
					}
				}

				// Software
				if (dataProducer.getSoftware() != null) {
					Element tempSoftwareElement = softwareToElement(dataProducer
							.getSoftware());
					if (tempSoftwareElement != null
							&& !isEmptyElement(tempSoftwareElement)) {
						softwareElement = tempSoftwareElement;
					}
				}

				// Now let's put them all in the right order:
				if (legacyFormat) {
					if (dataProducer.getDataProducerType().equalsIgnoreCase(
							DataProducer.TYPE_DEPLOYMENT)) {
						if (deviceElement != null)
							element.appendChild(deviceElement);
						if (childDataProducerElements.size() > 0) {
							Iterator<Element> iterator = childDataProducerElements
									.iterator();
							while (iterator.hasNext())
								element.appendChild(iterator.next());
						}
						if (personElement != null)
							element.appendChild(personElement);
						if (descriptionElement != null)
							element.appendChild(descriptionElement);
					} else {
						if (descriptionElement != null)
							element.appendChild(descriptionElement);
						if (personElement != null)
							element.appendChild(personElement);
						if (softwareElement != null)
							element.appendChild(softwareElement);
					}
					if (resources.size() > 0) {
						Iterator<Element> iterator = resources.iterator();
						while (iterator.hasNext())
							element.appendChild(iterator.next());
					}
					if (events.size() > 0) {
						Iterator<Element> iterator = events.iterator();
						while (iterator.hasNext())
							element.appendChild(iterator.next());
					}
					if (outputs.size() > 0) {
						Iterator<Element> iterator = outputs.iterator();
						while (iterator.hasNext())
							element.appendChild(iterator.next());
					}
				} else {
					// DP:
					if (childDataProducerElements.size() > 0) {
						Iterator<Element> iterator = childDataProducerElements
								.iterator();
						while (iterator.hasNext())
							element.appendChild(iterator.next());
					}
					if (dataProducerGroups.size() > 0) {
						Iterator<Element> iterator = dataProducerGroups
								.iterator();
						while (iterator.hasNext())
							element.appendChild(iterator.next());
					}
					if (descriptionElement != null)
						element.appendChild(descriptionElement);
					if (deviceElement != null)
						element.appendChild(deviceElement);
					if (events.size() > 0) {
						Iterator<Element> iterator = events.iterator();
						while (iterator.hasNext())
							element.appendChild(iterator.next());
					}
					if (keywords.size() > 0) {
						Iterator<Element> iterator = keywords.iterator();
						while (iterator.hasNext())
							element.appendChild(iterator.next());
					}
					if (outputs.size() > 0) {
						Iterator<Element> iterator = outputs.iterator();
						while (iterator.hasNext())
							element.appendChild(iterator.next());
					}
					if (personElement != null)
						element.appendChild(personElement);
					if (resources.size() > 0) {
						Iterator<Element> iterator = resources.iterator();
						while (iterator.hasNext())
							element.appendChild(iterator.next());
					}
					if (softwareElement != null)
						element.appendChild(softwareElement);
				}
			}
		}

		// Return it
		return element;
	}

	/**
	 * This method takes in a <code>DataProducerGroup</code> object and returns
	 * an <code>Element</code> that represents that object
	 * 
	 * @param dataProducerGroup
	 * @return
	 */
	private Element dataProducerGroupToElement(
			DataProducerGroup dataProducerGroup) {
		// The Element to return
		Element element = null;

		// Do a sanity check
		if (dataProducerGroup != null && dataProducerGroup.getName() != null) {
			element = new Element("DataProducerGroup");
			// Add attributes
			if (dataProducerGroup.getId() != null)
				element.addAttribute(new Attribute("id", dataProducerGroup
						.getId().toString()));
			element.addAttribute(new Attribute("name", dataProducerGroup
					.getName()));

			// Add description element if there is one
			if (dataProducerGroup.getDescription() != null) {
				Element descriptionElement = new Element("description");
				descriptionElement.appendChild(dataProducerGroup
						.getDescription());
				element.appendChild(descriptionElement);
			}
		}

		// Return it
		return element;
	}

	/**
	 * This method takes in a <code>Device</code> object and returns an
	 * <code>Element</code> that represents that object
	 * 
	 * @param device
	 * @return
	 */
	private Element deviceToElement(Device device) {
		Element element = null;

		// Do a sanity check
		if (device != null) {
			// Create the element
			element = new Element("Device");

			// Add the attributes
			if (device.getId() != null)
				element.addAttribute(new Attribute("id", device.getId()
						.toString()));
			if (device.getName() != null)
				element.addAttribute(new Attribute("name", device.getName()));
			if (device.getUuid() != null)
				element.addAttribute(new Attribute("uuid", device.getUuid()));
			if (device.getMfgName() != null)
				element.addAttribute(new Attribute("mfgName", device
						.getMfgName()));
			if (device.getMfgModel() != null)
				element.addAttribute(new Attribute("mfgModel", device
						.getMfgModel()));
			if (device.getMfgSerialNumber() != null)
				element.addAttribute(new Attribute("mfgSerialNumber", device
						.getMfgSerialNumber()));
			if (device.getInfoUrlList() != null)
				element.addAttribute(new Attribute("infoUrlList", device
						.getInfoUrlList()));

			// Relationships

			// Add description element if there is one
			if (device.getDescription() != null) {
				Element descriptionElement = new Element("description");
				descriptionElement.appendChild(device.getDescription());
				element.appendChild(descriptionElement);
			}

			// DeviceType
			if (device.getDeviceType() != null) {
				// Check for legacy
				if (legacyFormat) {
					element.addAttribute(new Attribute("type", device
							.getDeviceType().getName()));
				} else {
					element.appendChild(deviceTypeToElement(device
							.getDeviceType()));
				}
			}

			// Person
			if (device.getPerson() != null) {
				Element personElement = personToElement(device.getPerson());
				if (personElement != null && !isEmptyElement(personElement)) {
					element.appendChild(personElement);
				}
			}

			// Resources
			if (device.getResources() != null
					&& device.getResources().size() > 0) {
				Iterator<Resource> iterator = device.getResources().iterator();
				while (iterator.hasNext()) {
					Element resourceElement = resourceToElement(iterator.next());
					if (resourceElement != null
							&& !isEmptyElement(resourceElement))
						element.appendChild(resourceElement);
				}
			}
		}

		return element;
	}

	/**
	 * This method takes in a <code>DeviceType</code> object and returns an
	 * <code>Element</code> that represents that object
	 * 
	 * @param deviceType
	 * @return
	 */
	private Element deviceTypeToElement(DeviceType deviceType) {
		// The element to return
		Element element = null;

		// Do a sanity check
		if (deviceType != null && deviceType.getName() != null) {
			// Create the element
			element = new Element("DeviceType");

			// Add the attributes
			if (deviceType.getId() != null)
				element.addAttribute(new Attribute("id", deviceType.getId()
						.toString()));
			element.addAttribute(new Attribute("name", deviceType.getName()));

			// Add description element if there is one
			if (deviceType.getDescription() != null) {
				Element descriptionElement = new Element("description");
				descriptionElement.appendChild(deviceType.getDescription());
				element.appendChild(descriptionElement);
			}
		}

		// Return the result
		return element;
	}

	/**
	 * This method takes in a <code>Event</code> object and returns an
	 * <code>Element</code> that represents that object
	 * 
	 * @param event
	 * @return
	 */
	private Element eventToElement(Event event) {
		// The element to return
		Element element = null;

		// Do a sanity check
		if (event != null) {
			// Create the element
			element = new Element("Event");

			// Add the attributes
			if (event.getId() != null) {
				element.addAttribute(new Attribute("id", event.getId()
						.toString()));
			}
			if (event.getName() != null) {
				element.addAttribute(new Attribute("name", event.getName()));
			}
			if (event.getStartDate() != null)
				element.addAttribute(new Attribute("startDate", dateFormat
						.format(event.getStartDate())));
			if (event.getEndDate() != null)
				element.addAttribute(new Attribute("endDate", dateFormat
						.format(event.getEndDate())));

			// Add description element if there is one
			if (event.getDescription() != null) {
				Element descriptionElement = new Element("description");
				descriptionElement.appendChild(event.getDescription());
				element.appendChild(descriptionElement);
			}
		}
		// Return the result
		return element;
	}

	/**
	 * This method takes in a <code>HeaderDescription</code> object and returns
	 * an <code>Element</code> that represents that object
	 * 
	 * @param headerDescription
	 * @return
	 */
	private Element headerDescriptionToElement(
			HeaderDescription headerDescription) {
		// The element to return
		Element element = null;

		// Do a sanity check
		if (headerDescription != null) {
			// Create the element
			element = new Element("HeaderDescription");

			// Add the attributes
			if (headerDescription.getId() != null) {
				element.addAttribute(new Attribute("id", headerDescription
						.getId().toString()));
			}
			element.addAttribute(new Attribute("numHeaderLines",
					headerDescription.getNumHeaderLines() + ""));
			element.addAttribute(new Attribute("byteOffset", headerDescription
					.getByteOffset() + ""));

			// Add description element if there is one
			if (headerDescription.getDescription() != null) {
				Element descriptionElement = new Element("description");
				descriptionElement.appendChild(headerDescription
						.getDescription());
				element.appendChild(descriptionElement);
			}

			// CommentTags
			if (headerDescription.getCommentTags() != null
					&& headerDescription.getCommentTags().size() > 0) {
				Iterator<CommentTag> iterator = headerDescription
						.getCommentTags().iterator();
				while (iterator.hasNext()) {
					Element commentTagElement = commentTagToElement(iterator
							.next());
					if (commentTagElement != null
							&& !isEmptyElement(commentTagElement))
						element.appendChild(commentTagElement);
				}
			}
		}

		// Return the result
		return element;
	}

	/**
	 * This method takes in a <code>Keyword</code> object and returns an
	 * <code>Element</code> that represents that object
	 * 
	 * @param keyword
	 * @return
	 */
	private Element keywordToElement(Keyword keyword) {
		// The Element to return
		Element element = null;

		// Do a sanity check
		if (keyword != null && keyword.getName() != null) {
			// Create the element
			element = new Element("Keyword");

			// Add attributes
			if (keyword.getId() != null) {
				element.addAttribute(new Attribute("id", keyword.getId()
						.toString()));
			}
			element.addAttribute(new Attribute("name", keyword.getName()));

			// Add description element if there is one
			if (keyword.getDescription() != null) {
				Element descriptionElement = new Element("description");
				descriptionElement.appendChild(keyword.getDescription());
				element.appendChild(descriptionElement);
			}
		}

		// Now return it
		return element;
	}

	/**
	 * This method takes in a <code>Person</code> object and returns an
	 * <code>Element</code> that represents that object
	 * 
	 * @param person
	 * @return
	 */
	private Element personToElement(Person person) {
		// The element to return
		Element element = null;

		// Do a sanity check
		if (person != null) {

			// Create the element
			element = new Element("Person");

			// Add the attributes
			if (person.getId() != null)
				element.addAttribute(new Attribute("id", person.getId()
						.toString()));
			if (person.getFirstname() != null)
				element.addAttribute(new Attribute("firstname", person
						.getFirstname()));
			if (person.getSurname() != null)
				element.addAttribute(new Attribute("surname", person
						.getSurname()));
			if (person.getOrganization() != null)
				element.addAttribute(new Attribute("organization", person
						.getOrganization()));
			if (person.getUsername() != null)
				element.addAttribute(new Attribute("username", person
						.getUsername()));
			// TODO kgomes Password is not serialized out, but might be able to
			// do it with some kind of encryption
			if (person.getEmail() != null)
				element.addAttribute(new Attribute("email", person.getEmail()));
			if (person.getPhone() != null)
				element.addAttribute(new Attribute("phone", person.getPhone()));
			if (person.getAddress1() != null)
				element.addAttribute(new Attribute("address1", person
						.getAddress1()));
			if (person.getAddress2() != null)
				element.addAttribute(new Attribute("address2", person
						.getAddress2()));
			if (person.getCity() != null)
				element.addAttribute(new Attribute("city", person.getCity()));
			if (person.getState() != null)
				element.addAttribute(new Attribute("state", person.getState()));
			if (person.getZipcode() != null)
				element.addAttribute(new Attribute("zipcode", person
						.getZipcode()));
			if (person.getStatus() != null)
				element.addAttribute(new Attribute("status", person.getStatus()));

			// Add the UserGroups
			if (person.getUserGroups() != null
					&& person.getUserGroups().size() > 0) {
				Iterator<UserGroup> iterator = person.getUserGroups()
						.iterator();
				while (iterator.hasNext()) {
					Element userGroupElement = userGroupToElement(iterator
							.next());
					if (userGroupElement != null
							&& !isEmptyElement(userGroupElement))
						element.appendChild(userGroupElement);
				}
			}
		}
		// Return the result
		return element;
	}

	/**
	 * This method takes in a <code>RecordDescription</code> object and returns
	 * an <code>Element</code> that represents that object
	 * 
	 * @param recordDescription
	 * @return
	 */
	private Element recordDescriptionToElement(
			RecordDescription recordDescription) {
		// The element to return
		Element element = null;

		// Do a sanity check
		if (recordDescription != null) {
			// Create the element
			element = new Element("RecordDescription");

			// Add the attributes
			if (recordDescription.getId() != null)
				element.addAttribute(new Attribute("id", recordDescription
						.getId().toString()));
			if (recordDescription.getRecordType() != null)
				element.addAttribute(new Attribute("recordType",
						recordDescription.getRecordType().toString()));
			if (recordDescription.getBufferStyle() != null)
				element.addAttribute(new Attribute("bufferStyle",
						recordDescription.getBufferStyle()));
			if (recordDescription.getBufferParseType() != null)
				element.addAttribute(new Attribute("bufferParseType",
						recordDescription.getBufferParseType()));
			if (recordDescription.getBufferItemSeparator() != null)
				element.addAttribute(new Attribute("bufferItemSeparator",
						recordDescription.getBufferItemSeparator()));
			if (recordDescription.getBufferLengthType() != null)
				element.addAttribute(new Attribute("bufferLengthType",
						recordDescription.getBufferLengthType()));
			if (recordDescription.getRecordTerminator() != null)
				element.addAttribute(new Attribute("recordTerminator",
						recordDescription.getRecordTerminator()));
			if (recordDescription.isParseable() != null)
				element.addAttribute(new Attribute("parseable",
						recordDescription.isParseable().toString()));
			if (recordDescription.getEndian() != null)
				element.addAttribute(new Attribute("endian", recordDescription
						.getEndian()));
			if (recordDescription.getRecordParseRegExp() != null)
				element.addAttribute(new Attribute("recordParseRegExp",
						recordDescription.getRecordParseRegExp()));

			// Add the RecordVariables

			// Create a TreeMap to order the RVs by columnIndex if they are
			// available
			TreeMap<Long, Element> recordVariableElementSorted = new TreeMap<Long, Element>();
			ArrayList<Element> recordVariableElements = new ArrayList<Element>();
			if (recordDescription.getRecordVariables() != null
					&& recordDescription.getRecordVariables().size() > 0) {
				Iterator<RecordVariable> iterator = recordDescription
						.getRecordVariables().iterator();
				while (iterator.hasNext()) {
					RecordVariable recordVariable = iterator.next();
					Element recordVariableElement = recordVariableToElement(recordVariable);
					if (recordVariableElement != null
							&& !isEmptyElement(recordVariableElement)) {
						// If the record variable has a column index put it in
						// the treemap
						if (recordVariable.getColumnIndex() >= 0
								&& !recordVariableElementSorted
										.containsKey(recordVariable
												.getColumnIndex())) {
							recordVariableElementSorted.put(
									recordVariable.getColumnIndex(),
									recordVariableElement);
						} else {
							// Put it in the ArrayList
							recordVariableElements.add(recordVariableElement);
						}

					}
				}
				// Attach sorted elements first, then the rest in random order
				Iterator<Long> columnIndexIterator = recordVariableElementSorted
						.keySet().iterator();
				while (columnIndexIterator.hasNext()) {
					element.appendChild(recordVariableElementSorted
							.get(columnIndexIterator.next()));
				}
				Iterator<Element> elementIterator = recordVariableElements
						.iterator();
				while (elementIterator.hasNext())
					element.appendChild(elementIterator.next());
			}
		}
		// Now return the result
		return element;
	}

	/**
	 * This method takes in a <code>RecordVariable</code> object and returns an
	 * <code>Element</code> that represents that object
	 * 
	 * @param recordVariable
	 * @return
	 */
	private Element recordVariableToElement(RecordVariable recordVariable) {
		// The element to return
		Element element = null;

		// Do a sanity check
		if (recordVariable != null) {
			// Create the element
			element = new Element("RecordVariable");

			// Add the attributes
			if (recordVariable.getId() != null)
				element.addAttribute(new Attribute("id", recordVariable.getId()
						.toString()));
			if (recordVariable.getName() != null)
				element.addAttribute(new Attribute("name", recordVariable
						.getName()));
			if (recordVariable.getColumnIndex() >= 0)
				element.addAttribute(new Attribute("columnIndex",
						recordVariable.getColumnIndex() + ""));
			if (recordVariable.getFormat() != null)
				element.addAttribute(new Attribute("format", recordVariable
						.getFormat()));
			if (recordVariable.getLongName() != null)
				element.addAttribute(new Attribute("longName", recordVariable
						.getLongName()));
			if (recordVariable.getUnits() != null)
				element.addAttribute(new Attribute("units", recordVariable
						.getUnits()));
			if (recordVariable.getMissingValue() != null)
				element.addAttribute(new Attribute("missingValue",
						recordVariable.getMissingValue()));
			if (recordVariable.getAccuracy() != null)
				element.addAttribute(new Attribute("accuracy", recordVariable
						.getAccuracy()));
			if (recordVariable.getValidMin() != null)
				element.addAttribute(new Attribute("validMin", recordVariable
						.getValidMin()));
			if (recordVariable.getValidMax() != null)
				element.addAttribute(new Attribute("validMax", recordVariable
						.getValidMax()));
			if (recordVariable.getDisplayMin() != null)
				element.addAttribute(new Attribute("displayMin", recordVariable
						.getDisplayMin().toString()));
			if (recordVariable.getDisplayMax() != null)
				element.addAttribute(new Attribute("displayMax", recordVariable
						.getDisplayMax().toString()));
			if (recordVariable.getParseRegExp() != null)
				element.addAttribute(new Attribute("parseRegExp",
						recordVariable.getParseRegExp()));
			if (recordVariable.getReferenceScale() != null)
				element.addAttribute(new Attribute("referenceScale",
						recordVariable.getReferenceScale()));
			if (recordVariable.getConversionScale() != null)
				element.addAttribute(new Attribute("conversionScale",
						recordVariable.getConversionScale().toString()));
			if (recordVariable.getConversionOffset() != null)
				element.addAttribute(new Attribute("conversionOffset",
						recordVariable.getConversionOffset().toString()));
			if (recordVariable.getConvertedUnits() != null)
				element.addAttribute(new Attribute("convertedUnits",
						recordVariable.getConvertedUnits()));
			if (recordVariable.getSourceSensorID() != null)
				element.addAttribute(new Attribute("sourceSensorID",
						recordVariable.getSourceSensorID().toString()));

			// Add the relationships

			// Add description element if there is one
			if (recordVariable.getDescription() != null) {
				Element descriptionElement = new Element("description");
				descriptionElement.appendChild(recordVariable.getDescription());
				element.appendChild(descriptionElement);
			}

			// StandardDomain
			if (recordVariable.getStandardDomain() != null) {
				Element standardDomainElement = standardDomainToElement(recordVariable
						.getStandardDomain());
				if (standardDomainElement != null
						&& !isEmptyElement(standardDomainElement))
					element.appendChild(standardDomainToElement(recordVariable
							.getStandardDomain()));
			}

			// StandardKeyword
			if (recordVariable.getStandardKeyword() != null) {
				Element standardKeywordElement = standardKeywordToElement(recordVariable
						.getStandardKeyword());
				if (standardKeywordElement != null
						&& !isEmptyElement(standardKeywordElement))
					element.appendChild(standardKeywordToElement(recordVariable
							.getStandardKeyword()));
			}

			// StandardReferenceScale
			if (recordVariable.getStandardReferenceScale() != null) {
				Element standardReferenceScaleElement = standardReferenceScaleToElement(recordVariable
						.getStandardReferenceScale());
				if (standardReferenceScaleElement != null
						&& !isEmptyElement(standardReferenceScaleElement))
					element.appendChild(standardReferenceScaleToElement(recordVariable
							.getStandardReferenceScale()));
			}

			// StandardUnit
			if (recordVariable.getStandardUnit() != null) {
				Element standardUnitElement = standardUnitToElement(recordVariable
						.getStandardUnit());
				if (standardUnitElement != null
						&& !isEmptyElement(standardUnitElement))
					element.appendChild(standardUnitToElement(recordVariable
							.getStandardUnit()));
			}

			// StandardVariable
			if (recordVariable.getStandardVariable() != null) {
				Element standardVariableElement = standardVariableToElement(recordVariable
						.getStandardVariable());
				if (standardVariableElement != null
						&& !isEmptyElement(standardVariableElement))
					element.appendChild(standardVariableToElement(recordVariable
							.getStandardVariable()));
			}
		}

		// Now return the result
		return element;
	}

	/**
	 * This method takes in a <code>Resource</code> object and returns an
	 * <code>Element</code> that represents that object
	 * 
	 * @param resource
	 * @return
	 */
	private Element resourceToElement(Resource resource) {
		// The Element to return
		Element element = null;

		// Do a sanity check
		if (resource != null) {
			// Create the element
			element = new Element("Resource");

			// Add the attributes
			if (resource.getId() != null)
				element.addAttribute(new Attribute("id", resource.getId()
						.toString()));
			if (resource.getName() != null)
				element.addAttribute(new Attribute("name", resource.getName()));
			if (resource.getStartDate() != null)
				element.addAttribute(new Attribute("startDate", dateFormat
						.format(resource.getStartDate())));
			if (resource.getEndDate() != null)
				element.addAttribute(new Attribute("endDate", dateFormat
						.format(resource.getEndDate())));
			if (resource.getUriString() != null)
				element.addAttribute(new Attribute("uri", resource
						.getUriString()));
			if (resource.getContentLength() != null)
				element.addAttribute(new Attribute("contentLength", resource
						.getContentLength().toString()));
			if (resource.getMimeType() != null)
				element.addAttribute(new Attribute("mimeType", resource
						.getMimeType()));

			// Add the relationships

			// Add description element if there is one
			if (resource.getDescription() != null) {
				Element descriptionElement = new Element("description");
				descriptionElement.appendChild(resource.getDescription());
				element.appendChild(descriptionElement);
			}

			// Keywords
			if (resource.getKeywords() != null
					&& resource.getKeywords().size() > 0) {
				Iterator<Keyword> iterator = resource.getKeywords().iterator();
				while (iterator.hasNext()) {
					Element keywordElement = keywordToElement(iterator.next());
					if (keywordElement != null
							&& !isEmptyElement(keywordElement))
						element.appendChild(keywordElement);
				}
			}

			// Person
			if (resource.getPerson() != null) {
				Element personElement = personToElement(resource.getPerson());
				if (personElement != null && !isEmptyElement(personElement)) {
					element.appendChild(personElement);
				}
			}

			// ResourceBLOB
			if (resource.getResourceBLOB() != null) {
				Element resourceBLOBElement = resourceBLOBToElement(resource
						.getResourceBLOB());
				if (resourceBLOBElement != null
						&& !isEmptyElement(resourceBLOBElement))
					element.appendChild(resourceBLOBElement);
			}

			// ResourceType
			if (resource.getResourceType() != null
					&& resource.getResourceType().getName() != null) {
				// Check for legacy
				if (legacyFormat) {
					element.addAttribute(new Attribute("resourceType", resource
							.getResourceType().getName()));
				} else {
					Element resourceTypeElement = resourceTypeToElement(resource
							.getResourceType());
					if (resourceTypeElement != null
							&& !isEmptyElement(resourceTypeElement))
						element.appendChild(resourceTypeElement);
				}
			}
		}
		// Return the result
		return element;
	}

	/**
	 * This method takes in a <code></code> object and returns an
	 * <code>Element</code> that represents that object
	 * 
	 * @param
	 * @return
	 */
	private Element resourceBLOBToElement(ResourceBLOB resourceBLOB) {
		// The element to return
		Element element = null;

		// Do a sanity check
		if (resourceBLOB != null && resourceBLOB.getByteArray() != null) {

			// Create the element
			element = new Element("ResourceBLOB");

			// Assign the attributes
			if (resourceBLOB.getId() != null)
				element.addAttribute(new Attribute("id", resourceBLOB.getId()
						.toString()));
			if (resourceBLOB.getName() != null)
				element.addAttribute(new Attribute("name", resourceBLOB
						.getName()));

			// It is necessary to encode the payload before we construct the
			// element
			Base64 base64 = new Base64();
			String payload = null;
			try {
				payload = base64.encode(resourceBLOB.getByteArray());
			} catch (Exception e) {
				logger.error("Something went wrong trying to encode ResourceBLOB parameter: "
						+ e.getMessage());
				logger.error("ResourceBLOB = "
						+ resourceBLOB.toStringRepresentation("|"));
				return null;
			}
			if (payload != null) {
				element.addAttribute(new Attribute("byteArray", payload));
			}

			// Add description element if there is one
			if (resourceBLOB.getDescription() != null) {
				Element descriptionElement = new Element("description");
				descriptionElement.appendChild(resourceBLOB.getDescription());
				element.appendChild(descriptionElement);
			}
		}

		// Return the result
		return element;
	}

	/**
	 * This method takes in a <code>ResourceType</code> object and returns an
	 * <code>Element</code> that represents that object
	 * 
	 * @param resourceType
	 * @return
	 */
	private Element resourceTypeToElement(ResourceType resourceType) {
		// The element to return
		Element element = null;

		// Do a sanity check
		if (resourceType != null && resourceType.getName() != null) {
			// Create the element
			element = new Element("ResourceType");

			// Add the attributes
			if (resourceType.getId() != null)
				element.addAttribute(new Attribute("id", resourceType.getId()
						.toString()));
			if (resourceType.getName() != null)
				element.addAttribute(new Attribute("name", resourceType
						.getName()));

			// Add description element if there is one
			if (resourceType.getDescription() != null) {
				Element descriptionElement = new Element("description");
				descriptionElement.appendChild(resourceType.getDescription());
				element.appendChild(descriptionElement);
			}
		}

		// Return the result
		return element;
	}

	/**
	 * This method takes in a <code>Software</code> object and returns an
	 * <code>Element</code> that represents that object
	 * 
	 * @param software
	 * @return
	 */
	private Element softwareToElement(Software software) {
		// The element to return
		Element element = null;

		// Do a sanity check
		if (software != null) {
			// Create the element
			element = new Element("Software");

			// Add the attributes
			if (software.getId() != null)
				element.addAttribute(new Attribute("id", software.getId()
						.toString()));
			if (software.getName() != null)
				element.addAttribute(new Attribute("name", software.getName()));
			if (software.getUriString() != null)
				element.addAttribute(new Attribute("uri", software
						.getUriString()));
			if (software.getSoftwareVersion() != null)
				element.addAttribute(new Attribute("softwareVersion", software
						.getSoftwareVersion()));

			// Add the relationships
			// Add description element if there is one
			if (software.getDescription() != null) {
				Element descriptionElement = new Element("description");
				descriptionElement.appendChild(software.getDescription());
				element.appendChild(descriptionElement);
			}

			// Person
			if (software.getPerson() != null) {
				Element personElement = personToElement(software.getPerson());
				if (personElement != null && !isEmptyElement(personElement)) {
					element.appendChild(personElement);
				}
			}

			// Resources
			if (software.getResources() != null
					&& software.getResources().size() > 0) {
				Iterator<Resource> iterator = software.getResources()
						.iterator();
				while (iterator.hasNext()) {
					Element resourceElement = resourceToElement(iterator.next());
					if (resourceElement != null
							&& !isEmptyElement(resourceElement))
						element.appendChild(resourceElement);
				}
			}
		}

		// Return the result
		return element;
	}

	/**
	 * This method takes in a <code>StandardDomain</code> object and returns an
	 * <code>Element</code> that represents that object
	 * 
	 * @param standardDomain
	 * @return
	 */
	private Element standardDomainToElement(StandardDomain standardDomain) {
		// The element to return
		Element element = null;

		// Do a sanity check
		if (standardDomain != null) {
			// Create the element
			element = new Element("StandardDomain");

			// Add the attributes
			if (standardDomain.getId() != null)
				element.addAttribute(new Attribute("id", standardDomain.getId()
						.toString()));
			if (standardDomain.getName() != null)
				element.addAttribute(new Attribute("name", standardDomain
						.getName()));

			// Add the relationships
			// Add description element if there is one
			if (standardDomain.getDescription() != null) {
				Element descriptionElement = new Element("description");
				descriptionElement.appendChild(standardDomain.getDescription());
				element.appendChild(descriptionElement);
			}

		}

		// Return the result
		return element;
	}

	/**
	 * This method takes in a <code>StandardKeyword</code> object and returns an
	 * <code>Element</code> that represents that object
	 * 
	 * @param standardKeyword
	 * @return
	 */
	private Element standardKeywordToElement(StandardKeyword standardKeyword) {
		// The element to return
		Element element = null;

		// Do a sanity check
		if (standardKeyword != null) {
			// Create the element
			element = new Element("StandardKeyword");

			// Add the attributes
			if (standardKeyword.getId() != null)
				element.addAttribute(new Attribute("id", standardKeyword
						.getId().toString()));
			if (standardKeyword.getName() != null)
				element.addAttribute(new Attribute("name", standardKeyword
						.getName()));

			// Add the relationships
			// Add description element if there is one
			if (standardKeyword.getDescription() != null) {
				Element descriptionElement = new Element("description");
				descriptionElement
						.appendChild(standardKeyword.getDescription());
				element.appendChild(descriptionElement);
			}

		}

		// Return the result
		return element;
	}

	/**
	 * This method takes in a <code></code> object and returns an
	 * <code>Element</code> that represents that object
	 * 
	 * @param
	 * @return
	 */
	private Element standardReferenceScaleToElement(
			StandardReferenceScale standardReferenceScale) {
		// The element to return
		Element element = null;

		// Do a sanity check
		if (standardReferenceScale != null) {
			// Create the element
			element = new Element("StandardReferenceScale");

			// Add the attributes
			if (standardReferenceScale.getId() != null)
				element.addAttribute(new Attribute("id", standardReferenceScale
						.getId().toString()));
			if (standardReferenceScale.getName() != null)
				element.addAttribute(new Attribute("name",
						standardReferenceScale.getName()));

			// Add the relationships
			// Add description element if there is one
			if (standardReferenceScale.getDescription() != null) {
				Element descriptionElement = new Element("description");
				descriptionElement.appendChild(standardReferenceScale
						.getDescription());
				element.appendChild(descriptionElement);
			}

		}
		// Return the result
		return element;
	}

	/**
	 * This method takes in a <code></code> object and returns an
	 * <code>Element</code> that represents that object
	 * 
	 * @param
	 * @return
	 */
	private Element standardUnitToElement(StandardUnit standardUnit) {
		// The element to return
		Element element = null;

		// Do a sanity check
		if (standardUnit != null) {
			// Create the element
			element = new Element("StandardUnit");

			// Add the attributes
			if (standardUnit.getId() != null)
				element.addAttribute(new Attribute("id", standardUnit.getId()
						.toString()));
			if (standardUnit.getName() != null)
				element.addAttribute(new Attribute("name", standardUnit
						.getName()));
			if (standardUnit.getLongName() != null)
				element.addAttribute(new Attribute("longName", standardUnit
						.getLongName()));
			if (standardUnit.getSymbol() != null)
				element.addAttribute(new Attribute("symbol", standardUnit
						.getSymbol()));

			// Add the relationships
			// Add description element if there is one
			if (standardUnit.getDescription() != null) {
				Element descriptionElement = new Element("description");
				descriptionElement.appendChild(standardUnit.getDescription());
				element.appendChild(descriptionElement);
			}

		}

		// Return the result
		return element;
	}

	/**
	 * This method takes in a <code></code> object and returns an
	 * <code>Element</code> that represents that object
	 * 
	 * @param
	 * @return
	 */
	private Element standardVariableToElement(StandardVariable standardVariable) {
		// The element to return
		Element element = null;

		// Do a sanity check
		if (standardVariable != null) {
			// Create the element
			element = new Element("StandardVariable");

			// Add the attributes
			if (standardVariable.getId() != null)
				element.addAttribute(new Attribute("id", standardVariable
						.getId().toString()));
			if (standardVariable.getName() != null)
				element.addAttribute(new Attribute("name", standardVariable
						.getName()));
			if (standardVariable.getNamespaceUri() != null)
				element.addAttribute(new Attribute("namespaceUriString",
						standardVariable.getNamespaceUriString()));
			if (standardVariable.getReferenceScale() != null)
				element.addAttribute(new Attribute("referenceScale",
						standardVariable.getReferenceScale()));

			// Add the relationships
			// Add description element if there is one
			if (standardVariable.getDescription() != null) {
				Element descriptionElement = new Element("description");
				descriptionElement.appendChild(standardVariable
						.getDescription());
				element.appendChild(descriptionElement);
			}

			// StandardUnits
			if (standardVariable.getStandardUnits() != null
					&& standardVariable.getStandardUnits().size() > 0) {
				Iterator<StandardUnit> iterator = standardVariable
						.getStandardUnits().iterator();
				while (iterator.hasNext()) {
					Element standardUnitElement = standardUnitToElement(iterator
							.next());
					if (standardUnitElement != null
							&& !isEmptyElement(standardUnitElement))
						element.appendChild(standardUnitElement);
				}
			}
		}

		// Return the result
		return element;
	}

	/**
	 * This method takes in a <code></code> object and returns an
	 * <code>Element</code> that represents that object
	 * 
	 * @param
	 * @return
	 */
	private Element userGroupToElement(UserGroup userGroup) {
		// The element to return
		Element element = null;

		// Do a sanity check
		if (userGroup != null) {
			// Create the element
			element = new Element("UserGroup");

			// Add the attributes
			if (userGroup.getId() != null)
				element.addAttribute(new Attribute("id", userGroup.getId()
						.toString()));
			if (userGroup.getGroupName() != null)
				element.addAttribute(new Attribute("groupName", userGroup
						.getGroupName()));
		}

		// Return the result
		return element;
	}
}
