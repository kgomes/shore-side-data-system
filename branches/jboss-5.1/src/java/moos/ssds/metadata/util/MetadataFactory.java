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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

import moos.ssds.io.util.Base64;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.util.XmlDateFormat;
import nu.xom.ValidityException;

import org.apache.log4j.Logger;

/**
 * This class provides some factory methods for converting metadata objects to,
 * and from, different representations.
 * 
 * @author Kevin Gomes
 */
public class MetadataFactory {

	/**
	 * This method takes in a string representation of the object and gives back
	 * an instance of its correct <code>MetadataObject</code>. It basically
	 * instantiates an object of the given class and then calls its
	 * <code>setValuesFromStringRepresentation</code> method
	 * 
	 * @param stringRepresentation
	 *            the string representation of the object to be instantiated
	 * @param delimiter
	 *            is the delimiter that is used in the string represetation to
	 *            separate out the fields
	 * @return a <code>MetadataObject</code> that has its field populated from
	 *         the string representation
	 * @throws MetadataException
	 *             is something goes wrong in trying to parse the string to an
	 *             object form
	 */
	public static IMetadataObject createMetadataObjectFromStringRepresentation(
			String stringRepresentation, String delimiter)
			throws MetadataException {

		// First create the MetadataObject to return
		IMetadataObject objectToReturn = null;
		logger.debug("createMetadataObjectFromStringRepresentation called:");
		logger.debug("stringRepresentation = " + stringRepresentation);
		logger.debug("delimiter = " + delimiter);

		// If the delimiter is null, use the default delimiter
		String delimiterToUse = delimiter;
		if (delimiterToUse == null)
			delimiterToUse = IMetadataObject.DEFAULT_DELIMITER;

		// First check to see if the string representation is a URL to read XML
		// from
		String headString = null;
		try {
			headString = stringRepresentation.substring(0, 7);
			logger.debug("headString = " + headString);
		} catch (Exception e) {
			logger.error("Exception caught trying to get 0-7 substring from "
					+ stringRepresentation + ": " + e.getMessage());
		}
		if ((headString != null) && (headString.equalsIgnoreCase("xmlurl:"))) {
			logger.debug("The head of " + stringRepresentation
					+ " does start with xmlurl!  Will try to open URL "
					+ stringRepresentation.substring(7));
			URL urlToXML = null;
			try {
				urlToXML = new URL(stringRepresentation.substring(7));
			} catch (MalformedURLException e1) {
				logger.error("Malformed exception trying to build URL from "
						+ stringRepresentation.substring(7) + e1.getMessage());
			} catch (Exception e1) {
				logger.error("Exception trying to build URL from "
						+ stringRepresentation.substring(7) + e1.getMessage());
			}
			if (urlToXML != null) {
				StringBuffer xmlDoc = new StringBuffer();
				InputStream urlInputStream = null;
				try {
					urlInputStream = urlToXML.openStream();
				} catch (Exception e2) {
					logger.error("Exception caught trying to open URL stream: "
							+ e2.getMessage());
				}
				if (urlInputStream != null) {
					byte[] readByte = new byte[1];
					try {
						while (urlInputStream.read(readByte) != -1) {
							xmlDoc.append(new String(readByte));
						}
					} catch (Exception e3) {
						logger
								.error("Exception trying to read bytes from stream: "
										+ e3.getMessage());
					}
				}
				if (xmlDoc.length() > 0) {
					String xmlDocString = xmlDoc.toString();
					// First make sure there is nothing before the <?xml start
					// tag
					if (xmlDocString.indexOf("<?xml") > 0) {
						xmlDocString = xmlDocString.substring(xmlDocString
								.indexOf("<?xml"));
					}
					logger.debug("xmlDocString = " + xmlDocString);
					logger.debug("Will try to construct objects from that.");
					Collection metadataFromBuild = MetadataFactory
							.createMetadataObjectsFromXML(xmlDocString);
					if (metadataFromBuild == null) {
						logger
								.debug("Uh oh, not objects from built from XML!!!!");
					} else {
						logger.debug("OK, objects should be built, there are "
								+ metadataFromBuild.size() + " objects");
					}
					// Now grab the top object
					if ((metadataFromBuild != null)
							&& (metadataFromBuild.size() > 0)) {
						Iterator metadataObjectIter = metadataFromBuild
								.iterator();
						if (metadataObjectIter.hasNext())
							objectToReturn = (IMetadataObject) metadataObjectIter
									.next();
					}
				} else {
					logger.debug("xmlDoc had no length!");
				}
			}
		} else {
			logger.debug("The head of " + stringRepresentation
					+ " does not equals xmlurl:");
		}

		// Now if the object was not built from the XML, try to do it from the
		// string
		if (objectToReturn == null) {
			logger
					.debug("Object was not constructed from XML, will try from string Rep");
			// Create a string tokenizer that uses the delimiter specified (or
			// the
			// default)
			StringTokenizer stok = new StringTokenizer(stringRepresentation,
					delimiterToUse);

			// Grab the first token, which should be the name of the metadata
			// class
			String firstToken = stok.nextToken();

			// Grab the class name and try to create an object with that name
			if ((firstToken == null) || (firstToken.equals("")))
				throw new MetadataException(
						"No class was specified in the string, "
								+ "nothing could be instantiated");

			// Check to see if the string starts with the package name. If not,
			// add
			// it on
			if (!firstToken.startsWith("moos.ssds.metadata."))
				firstToken = "moos.ssds.metadata." + firstToken;

			// Now try to grab the class
			Class classDefinition = null;
			try {
				classDefinition = Class.forName(firstToken);
			} catch (ClassNotFoundException e1) {
				throw new MetadataException("ClassNotFoundException for "
						+ firstToken + ": " + e1.getMessage());
			}

			// Now get an instance of the object
			try {
				objectToReturn = (IMetadataObject) classDefinition
						.newInstance();
			} catch (InstantiationException e1) {
				throw new MetadataException("InstantiationException for "
						+ firstToken + ": " + e1.getMessage());
			} catch (IllegalAccessException e1) {
				throw new MetadataException("IllegalAccessException for "
						+ firstToken + ": " + e1.getMessage());
			}

			// Now call the method to set the values and pass in the string
			// representation. This allows each class to render itself
			objectToReturn.setValuesFromStringRepresentation(
					stringRepresentation, delimiterToUse);
		}

		// Now return the object
		return objectToReturn;
	}

	/**
	 * This method returns an object that is specified in the incoming class
	 * name
	 * 
	 * @param className
	 * @return a new instantiated <code>Object</code> of the class specified
	 *         in the className parameter
	 */
	public static Object getObject(String className) {
		logger
				.debug("Going to try to instantiate object of class "
						+ className);
		Object object = null;
		try {
			Class classDefinition = findClassByName(className);
			object = classDefinition.newInstance();
		} catch (Throwable e) {
			logger.error("Throwable caught trying to instantiate class "
					+ className + ": " + e.getMessage());
		}
		return object;
	}

	/**
	 * This method takes in a class and a string to use as a constructor of some
	 * sort. Note that if primitives are specified as the class, an object of
	 * the wrapper class will be returned. It can be used for the following:
	 * <ol>
	 * <li><code>byte</code></li>
	 * <li><code>Byte</code></li>
	 * <li><code>short</code></li>
	 * <li><code>Short</code></li>
	 * <li><code>int</code></li>
	 * <li><code>Integer</code></li>
	 * <li><code>long</code></li>
	 * <li><code>Long</code></li>
	 * <li><code>float</code></li>
	 * <li><code>Float</code></li>
	 * <li><code>double</code></li>
	 * <li><code>Double</code></li>
	 * <li><code>char</code></li>
	 * <li><code>Character</code></li>
	 * <li><code>boolean</code></li>
	 * <li><code>Boolean</code></li>
	 * <li><code>String</code></li>
	 * <li><code>java.util.Date</code> (this uses an XMLDateFormatter so the
	 * incoming string must be in that form</li>
	 * <li><code>java.util.Calendar</code> (this uses an XMLDateFormatter so
	 * the incoming string must be in that form</li>
	 * <li>java.net.URI</li>
	 * <li>java.net.URL</li>
	 * <li><code>IMetadataObject</code> (the string passed in must be the
	 * string representation of that object)</li>
	 * </ol>
	 * 
	 * @param classToInstantiate
	 *            the <code>Class</code> that will be used to instantiate a
	 *            new object
	 * @param constructorValue
	 *            the string value to initialize the object to
	 * @param delimiter
	 *            this is only used if the constructor you pass in is a string
	 *            representation of a <code>IMetadataObject</code>. This
	 *            tells what delimiter is used to parse the string
	 *            representation
	 * @return an <code>Object</code> of the class that is passed in with its
	 *         value set using the incoming string. If the class is not one of
	 *         those listed above, null will be returned
	 * @throws IllegalArgumentException
	 *             if the incoming string could not be converted.
	 */
	public static Object getObject(Class classToInstantiate,
			String constructorValue, String delimiter)
			throws IllegalArgumentException {

		if ((delimiter == null) || (delimiter.equals(""))) {
			delimiter = "|";
		}

		// Grab an XML data formatter to convert string to Date
		XmlDateFormat xmlFormat = new XmlDateFormat();

		// The object to return
		Object objectToReturn = null;

		// If nulls come in
		if (classToInstantiate == null)
			return null;

		// OK, check them all
		try {
			if (classToInstantiate.equals(byte.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = new Byte(constructorValue);
				}
			} else if (classToInstantiate.equals(Byte.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = new Byte(constructorValue);
				}
			} else if (classToInstantiate.equals(byte[].class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					// Since this is a byte array, the HTTP will have recieved
					// an ASCII string that should be a Base64 encoding of the
					// data, so we need to decode to a byte array
					Base64 b64 = new Base64();
					b64.setLineLength(0);
					objectToReturn = b64.decode(constructorValue);
				}
			} else if (classToInstantiate.equals(short.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = new Short(constructorValue);
				}
			} else if (classToInstantiate.equals(Short.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = new Short(constructorValue);
				}
			} else if (classToInstantiate.equals(int.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = new Integer(constructorValue);
				}
			} else if (classToInstantiate.equals(Integer.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = new Integer(constructorValue);
				}
			} else if (classToInstantiate.equals(long.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = new Long(constructorValue);
				}
			} else if (classToInstantiate.equals(Long.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = new Long(constructorValue);
				}
			} else if (classToInstantiate.equals(float.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = new Float(constructorValue);
				}
			} else if (classToInstantiate.equals(Float.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = new Float(constructorValue);
				}
			} else if (classToInstantiate.equals(double.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = new Double(constructorValue);
				}
			} else if (classToInstantiate.equals(Double.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = new Double(constructorValue);
				}
			} else if (classToInstantiate.equals(char.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = new Character(constructorValue
							.toCharArray()[0]);
				}
			} else if (classToInstantiate.equals(Character.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = new Character(constructorValue
							.toCharArray()[0]);
				}
			} else if (classToInstantiate.equals(boolean.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = new Boolean(constructorValue);
				}
			} else if (classToInstantiate.equals(Boolean.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = new Boolean(constructorValue);
				}
			} else if (classToInstantiate.equals(String.class)) {
				return constructorValue;
			} else if (classToInstantiate.equals(java.util.Date.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = xmlFormat.parse(constructorValue);
				}
			} else if (classToInstantiate.equals(java.util.Calendar.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					Date tempDate = xmlFormat.parse(constructorValue);
					Calendar tempCalendar = Calendar.getInstance();
					tempCalendar.setTime(tempDate);
					objectToReturn = tempCalendar;
				}
			} else if (classToInstantiate.equals(java.net.URI.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = new URI(constructorValue);
				}
			} else if (classToInstantiate.equals(java.net.URL.class)) {
				if ((constructorValue == null) || (constructorValue.equals(""))) {
					objectToReturn = null;
				} else {
					objectToReturn = new URL(constructorValue);
				}
			} else if (classToInstantiate.equals(Collection.class)) {
				objectToReturn = new ArrayList();
				// Since this is a collection, tokenize the incoming constructor
				// based on the delimiter
				if ((constructorValue != null)
						&& (!constructorValue.equals(""))) {
					StringTokenizer stok = new StringTokenizer(
							constructorValue, delimiter);
					while (stok.hasMoreTokens()) {
						String entryType = stok.nextToken();
						String entryValue = stok.nextToken();
						// TODO kgomes right now this only supports arrays of
						// strings, but I should open that up
						if (entryType.equalsIgnoreCase("String"))
							((ArrayList) objectToReturn).add(entryValue);
					}
				}
			} else {
				// Try to construct a metadata class
				objectToReturn = MetadataFactory
						.createMetadataObjectFromStringRepresentation(
								constructorValue, delimiter);
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Could not convert value "
					+ constructorValue + " to a "
					+ classToInstantiate.getName() + ": " + e.getMessage());
		} catch (Throwable e) {
			throw new IllegalArgumentException("Could not convert value "
					+ constructorValue + " to a "
					+ classToInstantiate.getName() + ": " + e.getMessage());
		}
		// Now return it if we made it
		return objectToReturn;
	}

	/**
	 * This method returns a class that is specified by a string name, or the
	 * closest thing to it. If the className is not a primitive and a class with
	 * the className is not found, it will also try to find a metadata class
	 * with that name. It is sort of a convenience method. If you don't specify
	 * a fully qualified classname, it will search common libraries like
	 * java.lang.* and java.util.* so be careful. For example if you want a
	 * java.sql.Date and you just say Date, you will get a java.util.Date. The
	 * moral of the story is to fully qualify the name :)
	 * 
	 * @param className
	 *            the name of the class to get a <code>Class</code> object for
	 * @return the <code>Class</code> that has a name that matches the
	 *         incoming string
	 */
	public static Class findClassByName(String className)
			throws ClassNotFoundException {

		// We first need to look for a primitive
		if (className == null)
			return null;
		if (className.equals("boolean"))
			return boolean.class;
		if (className.equals("int"))
			return int.class;
		if (className.equals("byte"))
			return byte.class;
		if (className.equals("char"))
			return char.class;
		if (className.equals("short"))
			return short.class;
		if (className.equals("long"))
			return long.class;
		if (className.equals("float"))
			return float.class;
		if (className.equals("double"))
			return double.class;
		if (className.equals("byte[]"))
			return byte[].class;

		// The class to return
		Class classToReturn = null;
		// Exceptional conditions
		if ((className == null) || (className.equals(""))) {
			return null;
		}

		try {
			// First just try to look for it straight up
			classToReturn = Class.forName(className);
		} catch (ClassNotFoundException e) {
		}

		// If not periods in the class name, change the first letter to capital
		// and try again
		if ((classToReturn == null) && (className.indexOf(".") < 0)) {
			// Grab the first character
			String firstChar = className.substring(0, 1);
			firstChar = firstChar.toUpperCase();
			try {
				classToReturn = Class.forName(firstChar
						+ className.substring(1));
			} catch (ClassNotFoundException e1) {
			}
			// Now if class still not found, try adding java.lang
			if (classToReturn == null) {
				try {
					classToReturn = Class.forName("java.lang." + className);
				} catch (ClassNotFoundException e2) {
				}
			}
			if (classToReturn == null) {
				try {
					classToReturn = Class.forName("java.lang." + firstChar
							+ className.substring(1));
				} catch (ClassNotFoundException e2) {
				}
			}
			if (classToReturn == null) {
				try {
					classToReturn = Class.forName("java.util." + className);
				} catch (ClassNotFoundException e2) {
				}
			}
			if (classToReturn == null) {
				try {
					classToReturn = Class.forName("java.util." + firstChar
							+ className.substring(1));
				} catch (ClassNotFoundException e2) {
				}
			}
			if (classToReturn == null) {
				try {
					classToReturn = Class.forName("moos.ssds.metadata."
							+ className);
				} catch (ClassNotFoundException e2) {
				}
			}
			if (classToReturn == null) {
				try {
					classToReturn = Class.forName("moos.ssds.metadata."
							+ firstChar + className.substring(1));
				} catch (ClassNotFoundException e2) {
				}
			}
		}
		if (classToReturn == null) {
			throw new ClassNotFoundException("Could not find class with name "
					+ className);
		}

		return classToReturn;

	}

	/**
	 * This method takes in a <code>IMetadataObject</code> and returns the
	 * <code>String</code> that is that object graph converted to XML
	 * 
	 * @param metadataObject
	 *            the object who (with it's graph) will be converted to XML
	 * @return the <code>String</code> format of the XML that matches the
	 *         object graph
	 * @throws MetadataException
	 *             if something goes wrong in the construction
	 */
	public static String createXMLFromObject(IMetadataObject metadataObject)
			throws MetadataException {

		// Check for null
		if (metadataObject == null)
			return null;

		// The XML to return
		String xmlDocumentToReturn = null;

		// Grab and XMLBuilder
		XmlBuilder xmlBuilder = new XmlBuilder();

		// Add the object
		xmlBuilder.add(metadataObject);

		// Now convert it
		xmlBuilder.marshal();

		// Now return the result
		try {
			xmlDocumentToReturn = xmlBuilder.toFormattedXML();
		} catch (UnsupportedEncodingException e) {
			throw new MetadataException("UnsupportedEncodingException caught: "
					+ e.getMessage());
		} catch (IOException e) {
			throw new MetadataException("IOException caught: " + e.getMessage());
		}

		// Now return it
		return xmlDocumentToReturn;
	}

	/**
	 * This method takes in a string that is an XML Document and then converts
	 * that over to an graph with <code>IMetadataObjects</code>.
	 * 
	 * @param xmlDocument
	 *            the XML in string format
	 * @return a <code>Collection</code> because you can have many
	 *         <code>IMetadataObject</code>s at the top level
	 * @throws MetadataException
	 *             is something goes wrong with parsing or your XML is funky
	 */
	public static Collection createMetadataObjectsFromXML(String xmlDocument)
			throws MetadataException {

		// Check for null xml
		if (xmlDocument == null) {
			return null;
		}

		// The metadataObject to return
		Collection metadataObjectsToReturn = new ArrayList();

		// Create the object builder
		ObjectBuilder objectBuilder = new ObjectBuilder(xmlDocument);

		// Unmarshal into objects
		try {
			objectBuilder.unmarshal(false);
		} catch (ValidityException e) {
			throw new MetadataException("Validity Exception caught: "
					+ e.getMessage());
		} catch (Exception e) {
			throw new MetadataException("Exception caught: " + e.getMessage());
		}
		// Grab the collection to return
		metadataObjectsToReturn = objectBuilder.listAll();

		return metadataObjectsToReturn;
	}


	/**
	 * The Log 4J Logger
	 */
	static Logger logger = Logger.getLogger(MetadataFactory.class);

}
