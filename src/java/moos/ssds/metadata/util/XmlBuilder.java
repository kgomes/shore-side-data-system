package moos.ssds.metadata.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import moos.ssds.metadata.DataProducer;
import moos.ssds.util.XmlDateFormat;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Serializer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

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
 * <code>XmlBuilder</code> uses reflection for marshalling XML from objects.
 * Changes to the model can be handled without requring changes to this class.
 * However, sometimes the result is undesirable, so some modifications may be
 * required.
 * </p>
 * <hr>
 * 
 * @author : $Author: mccann $
 * @version : $Revision: 1.1.2.7 $
 * @testcase test.moos.ssds.model.TestXmlBuilder
 */
public class XmlBuilder {

	public XmlBuilder() {
		// Set the logger level
		logger.setLevel(level);
		// Create the <Metadata> root element
		Element root = new Element("Metadata");

		// Create a new XOM Document
		document = new Document(root);

		// Grab the package from a metadata class so we can run checks later
		p = DataProducer.class.getPackage();

		// Clear the counter
		objectToElementCount = 0;

		// Hash list
		objList = new ArrayList();
	}

	public void add(Object obj) {
		rootElements.add(obj);
	}

	public void remove(Object obj) {
		rootElements.remove(obj);
	}

	public void addAll(Collection c) {
		rootElements.addAll(c);
	}

	public void removeAll(Collection c) {
		rootElements.removeAll(c);
	}

	public Collection getRootElements() {
		return rootElements;
	}

	/**
	 * @return The XML document as text
	 */
	public String toXML() {
		return document.toXML();
	}

	/**
	 * @return The XML document as a formatted (indents & line breaks) string
	 */
	public String toFormattedXML() throws IOException,
			UnsupportedEncodingException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		Serializer serializer = new Serializer(buf, "ISO-8859-1");
		serializer.setIndent(4);
		serializer.setMaxLength(65536);
		serializer.write(document);
		return buf.toString();
	}

	/**
	 * @return The XML document as text
	 */
	public String toString() {
		return document.toString();
	}

	/**
	 * @return The XML document as an Object
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Write formated xml to a file using "ISO-8859-1" encoding
	 * 
	 * @param file
	 *            The file to write into.
	 */
	public void toFile(java.io.File file) throws IOException,
			UnsupportedEncodingException {
		toFile(file, "ISO-8859-1");
	}

	/**
	 * Write formated xml to a file using the specified encoding
	 * 
	 * @param file
	 *            The file to write into.
	 * @param encoding
	 *            the encodinb to use
	 */
	public void toFile(java.io.File file, String encoding) throws IOException,
			UnsupportedEncodingException {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		print(out, encoding);
		out.close();
	}

	/**
	 * Write the formated xml out to a stream
	 * 
	 * @param out
	 *            An outstream to write to
	 * @param encoding
	 *            the encodinb to use
	 */
	public void print(OutputStream out, String encoding) throws IOException,
			UnsupportedEncodingException {
		Serializer serializer = new Serializer(out, encoding);
		serializer.setIndent(4);
		serializer.setMaxLength(65536);
		serializer.write(document);
	}

	public void print(OutputStream out) throws IOException,
			UnsupportedEncodingException {
		print(out, "ISO-8859-1");
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

		// Placeholders
		Element element = null;
		Object obj = null;

		// Now iterate over the objects that were added
		Iterator i = rootElements.iterator();
		while (i.hasNext()) {
			// Grab the object
			obj = i.next();
			if (obj != null) {
				try {
					// Convert the object to an XML element
					element = objectToElement(obj);
					// If it looks OK, add it the the <Metadata> element
					if (element != null && !isEmptyElement(element)) {
						document.getRootElement().appendChild(element);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// Now sort all the attributes alphabetically
		this.deepSortChildrenElements(document.getRootElement());
	}

	/**
	 * Convert an object to its corresponding XML element
	 * 
	 * @return An element representing a given object.
	 */
	private Element objectToElement(Object parentObject) {

		// Count the number of times we are here to trap recursions
		objectToElementCount++;
		if (objectToElementCount > maxObjectToElementCount) {
			logger.warn("*** Called objectToElement " + objectToElementCount
					+ " times.  Possible recursion in model with object "
					+ parentObject.getClass().getName());
		}

		// Create the element placeholder
		Element parentElement = null;

		// If the object is not in the Hibernate session, return nothing
		if (!Hibernate.isInitialized(parentObject))
			return parentElement;

		logger.debug("Converting " + parentObject.getClass().getName() + "("
				+ parentObject + ") to XML");

		// Grab the class from the object
		Class parentClass = this.getRealClassOfObject(parentObject);

		// Ignore Objects that are not in the metadata package
		if (isClassInMetadataPackage(parentClass)) {

			// Create an empty element that matches the class
			parentElement = makeEmptyElement(parentObject);

			// Now grab all the methods off the incoming object
			Method[] methods = parentClass.getMethods();
			Method method = null;
			// Loop over the methods to populate the element with attributes and
			// child elements
			for (int i = 0; i < methods.length; i++) {
				// Grab the method
				method = methods[i];

				// Check to see if the method returns a collection
				if (!isMethodToSkip(method)) {
					if (method.getReturnType() == Collection.class) {

						// Save hash of objects and compare for preventing
						// recursion
						// as can happen with a PR that is a consumer of its
						// input.
						objList.add(parentObject.hashCode());

						appendCollection(parentObject, method, parentElement);
					} else {
						appendElement(parentObject, method, parentElement);
					}
				}
				// Check for methods that return Collections
				// if (isListMethod(parentObject, method)) {
				// appendCollection(parentObject, method, parentElement);
				// }
				// // Check all other get methods.
				// else if (isGetMethod(parentObject, method)) {
				// appendElement(parentObject, method, parentElement);
				// }
			}
		}
		return parentElement;
	}

	/**
	 * @param c
	 *            A class
	 * @return true if the supplied class is package local to XmlBuilder
	 */
	private boolean isClassInMetadataPackage(Class c) {
		boolean OK = false;
		try {
			OK = c.getPackage().equals(p);
		} catch (Exception e) {
			logger
					.error("isPackageLocal(): Exception trying to getPackage() from class "
							+ c.getClass().getName());
			OK = false;
		}
		return OK;
	}

	/**
	 * Generates an Empty XMLelement with same name as the Object supplied as an
	 * argument.
	 * 
	 * @return An element with the same local name as the object's class
	 */
	private Element makeEmptyElement(Object object) {

		// Grab the class name
		String name = this.getRealClassNameOfObject(object);

		// Shorten it to just the object name
		int idx = name.lastIndexOf(".");
		name = name.substring(idx + 1);

		// Now create and return an new Element with that name
		return new Element(name);
	}

	/**
	 * Privides special handiling for accessors that return Collections.
	 * 
	 * @param parentObject
	 *            An Object
	 * @param method
	 *            An accessor (list or get) method of parentObject
	 * @param parentElement
	 *            An XML element coresponding to parentObject
	 */
	private void appendCollection(Object parentObject, Method method,
			Element parentElement) {

		logger.debug("appendCollection called on class "
				+ parentObject.getClass().getName() + " using method "
				+ method.getName());
		// Call the method and get the collection
		Object childObject = getChildObject(parentObject, method);
		Collection c = (Collection) childObject;

		// Now we need to make sure the child object is instantiated in
		// Hibernate. If it is not, do nothing
		if (!Hibernate.isInitialized(c))
			return;

		/*
		 * Handle special caseof input and output. This is done by creating an
		 * new element <input> or <output> and then appending the contents of
		 * the colleciton to this element rather than the parentElement provided
		 * as an argument.
		 */
		String methodName = method.getName();
		if ((methodName.equals("getInputs"))
				|| (methodName.equals("getOutputs"))) {
			if (!c.isEmpty()) {
				StringBuffer sb = new StringBuffer(getParameterName(method));
				sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
				String paramName = sb.substring(0, sb.length() - 1);
				// remove trailing 's'
				Element ioElement = new Element(paramName);
				parentElement.appendChild(ioElement);
				parentElement = ioElement;
			}
		} else if (methodName.equals("getConsumers")) {
			if (!c.isEmpty()) {
				if (!objList.contains(childObject.hashCode())) {
					Element ioElement = new Element("consumer");
					parentElement.appendChild(ioElement);
					parentElement = ioElement;
				} else {
					return; // recursion detected
				}
			}

		}

		// Each object in the collection becomes a childElement to parentElement
		Element childElement = null;
		if (c != null && !c.isEmpty()) {
			Iterator it = c.iterator();
			Object lo = null;
			while (it.hasNext()) {
				lo = it.next();
				if (lo == null) {
					continue;
				}
				childElement = objectToElement(lo);
				if ((childElement != null) && (!isEmptyElement(childElement))) {
					parentElement.appendChild(childElement);
				}
			}
		}
	}

	/**
	 * * Retrieve an object from an accessor method
	 * 
	 * @return An object returned from the given method call
	 * @param parentObject
	 *            The object whos method is called
	 * @param method
	 *            The method to call on the parentObject
	 */
	private Object getChildObject(Object parentObject, Method method) {
		Object childObject = null;
		try {
			childObject = method.invoke(parentObject, new Object[] {});
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		} catch (IllegalArgumentException e) {
		}
		return childObject;
	}

	/**
	 * This method returns the name of the method without the get or is on it
	 * 
	 * @param method
	 *            The method of an object
	 * @return The parameter name from 'get' or 'is' methods (i.e Device for
	 *         getDevice)
	 */
	private String getParameterName(Method method) {
		// Grab the name
		String methodName = method.getName();
		String parameterName = "";
		// If it starts with get/is, grab the last part
		if (methodName.startsWith("get")) {
			parameterName = methodName.substring(3);
		} else if (methodName.startsWith("is")) {
			parameterName = methodName.substring(2);
		}
		return parameterName;
	}

	/**
	 * Append any Objects retrieved from a parent object using the method
	 * specified in the arguments to the corresponding parent element. This
	 * method actually acts a switchyard that calls the appropriate appending
	 * method. Differt methods exist for appending Collections, Objects, and
	 * primitive types.
	 * 
	 * @param parentObject
	 *            An Object
	 * @param method
	 *            An accessor (list or get) method of parentObject
	 * @param parentElement
	 *            An XML element coresponding to parentObject
	 */
	private void appendElement(Object parentObject, Method method,
			Element parentElement) {

		logger.debug("appendElement called on class "
				+ parentObject.getClass().getName() + " using method "
				+ method.getName());
		// primitives and their wrapped types are added as XML attributes
		if (isAttributeType(method)) {
			appendPrimitiveType(parentObject, method, parentElement);
		}
		// Non-package local objects require special handling
		else if (isSpecialMethod(method)) {
			appendSpecial(parentObject, method, parentElement);
		}
		// True SSDS Objects are added as XML Elements
		else {
			appendObject(parentObject, method, parentElement);
		}
	}

	/**
	 * Does the method return a type that will need to be treated as XML
	 * Attribute. Primitive and wrapped types such as Strings,Doubles etc are to
	 * be treated as XML attributes.
	 * 
	 * @param method
	 *            An accessor method
	 */
	private boolean isAttributeType(Method method) {
		boolean ok = false;

		Class methodReturnType = method.getReturnType();
		if ((methodReturnType.isPrimitive())
				|| (methodReturnType.equals(String.class))
				|| (methodReturnType.equals(Boolean.class))
				|| (methodReturnType.equals(Character.class))
				|| (methodReturnType.equals(Byte.class))
				|| (methodReturnType.equals(Short.class))
				|| (methodReturnType.equals(Integer.class))
				|| (methodReturnType.equals(Long.class))
				|| (methodReturnType.equals(Float.class))
				|| (methodReturnType.equals(Double.class))) {
			ok = true;
		}
		return ok;
	}

	/**
	 * Handles prmitive types
	 * 
	 * @param parentObject
	 *            An Object
	 * @param method
	 *            An accessor (list or get) method of parentObject
	 * @param parentElement
	 *            An XML element coresponding to parentObject
	 */
	private void appendPrimitiveType(Object parentObject, Method method,
			Element parentElement) {
		logger.debug("appendPrimitiveType called with method name "
				+ method.getName() + " on object of class "
				+ parentObject.getClass().getName());
		Object childObject = getChildObject(parentObject, method);
		if (childObject != null) {
			// Change the first character of the parameter name to lowercase
			StringBuffer sb = new StringBuffer(getParameterName(method));
			sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
			String paramName = sb.toString();
			String paramValue = String.valueOf(childObject);
			// Don't add atributes that are empty strings or NaN
			if ((!paramValue.equals("")) && (!paramValue.equals("NaN"))) {
				// Description is added as an element because it is easier to
				// read
				if (paramName.equals("description")) {
					Element childElement = new Element("description");
					childElement.appendChild(paramValue);
					parentElement.appendChild(childElement);
				} else {
					Attribute childAttribute = new Attribute(paramName,
							paramValue);
					parentElement.addAttribute(childAttribute);
				}
			}
		}
	}

	/**
	 * Does the parameter require special handling (i.e ids, dates and URLs)
	 * 
	 * @param method
	 *            An accessor method
	 */
	private boolean isSpecialMethod(Method method) {
		boolean ok = false;
		String methodName = method.getName();
		if (methodName.equals("getStartDate")
				|| methodName.equals("getEndDate")
				|| methodName.equals("getUrl") || methodName.equals("getUri")
				|| methodName.equals("getDODSUrl")) {
			ok = true;
		}
		return ok;
	}

	/**
	 * Handles non-package local objects
	 * 
	 * @param parentObject
	 *            An Object
	 * @param method
	 *            An accessor (list or get) method of parentObject
	 * @param parentElement
	 *            An XML element coresponding to parentObject
	 */
	private void appendSpecial(Object parentObject, Method method,
			Element parentElement) {

		String methodName = method.getName();
		Object childObject = getChildObject(parentObject, method);

		// Get the paramValue
		if (childObject != null) {

			// Change the first character of the parameter name to lowercase
			StringBuffer sb = new StringBuffer(getParameterName(method));
			sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
			String paramName = sb.toString();

			String paramValue = null;
			Attribute attribute = null;
			if (methodName.equals("getStartDate")
					|| methodName.equals("getEndDate")) {
				paramValue = dateFormat.format((Date) childObject);
			} else if ((methodName.equals("getUrl"))
					|| (methodName.equals("getDODSUrl"))) {
				paramValue = ((URL) childObject).toString();
			} else if (methodName.equals("getUri")) {
				paramValue = ((URI) childObject).toString();
			}
			attribute = new Attribute(paramName, paramValue);
			parentElement.addAttribute(attribute);
		}

	}

	/**
	 * Handles package local objects
	 * 
	 * @param parentObject
	 *            An Object
	 * @param method
	 *            An accessor (list or get) method of parentObject
	 * @param parentElement
	 *            An XML element coresponding to parentObject
	 */
	private void appendObject(Object parentObject, Method method,
			Element parentElement) {
		Object childObject = getChildObject(parentObject, method);
		if (childObject != null) {
			Element childElement = objectToElement(childObject);
			if ((childElement != null) && (!isEmptyElement(childElement))) {
				parentElement.appendChild(childElement);
			}
		}
	}

	/**
	 * Checks to see if an element has child nodes or attributes. If it doesn't
	 * the element corresponds to an emtpy object and so is generally not
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
	 * This method checks to see if this is a method that will walk back up a
	 * object graph and cause infinite recursion. If it will the method will
	 * return <code>true</code> indicating the method should be skipped
	 * 
	 * @param method
	 * @return
	 */
	private boolean isMethodToSkip(Method method) {
		boolean skip = false;

		Class parentClass = method.getDeclaringClass();
		String parentClassName = parentClass.getSimpleName();
		String methodName = method.getName();
		if ((parentClassName.equals("DataContainer"))
				&& (methodName.equals("getRecordVariables")))
			return true;
		if ((methodName.equals("getCreator"))
				|| (methodName.equals("getDateRange"))
				|| (methodName.equals("getUrl"))
				|| (methodName.equals("getUriString"))
				|| (methodName.equals("getParentDataProducer"))
				|| (methodName.equals("getVersion"))
				|| (!methodName.startsWith("get") && !methodName
						.startsWith("is")) || (methodName.equals("equals"))
				|| (methodName.equals("hashCode"))) {
			skip = true;
		}
		return skip;
	}

	private void deepSortChildrenElements(Element element) {
		// Grab the children
		Elements childElements = element.getChildElements();

		// Loop over and sort in the current tree then call sort on its children
		for (int i = 0; i < childElements.size(); i++) {
			childElements = element.getChildElements();
			Element currentElement = childElements.get(i);
			for (int j = 0; j < i; j++) {
				String currentElementName = currentElement.getLocalName();
				currentElementName = currentElementName.toLowerCase();
				String childElementName = childElements.get(j).getLocalName();
				childElementName = childElementName.toLowerCase();
				if (currentElementName.compareTo(childElementName) < 0) {
					Node nodeToMoveUp = element.removeChild(i);
					nodeToMoveUp.detach();
					Node nodeToMoveDown = element.removeChild(j);
					nodeToMoveDown.detach();
					// Now swap them
					element.insertChild(nodeToMoveUp, j);
					element.insertChild(nodeToMoveDown, i);
					break;
				}
			}
			deepSortChildrenElements(childElements.get(i));
		}
	}

	/**
	 * I had to create this method to handle Hibernate proxies. If you call
	 * getClass() on a Hibernate proxy you get some very strange stuff and you
	 * need to drill down to the real object to get the correct class
	 * 
	 * @param object
	 * @return
	 */
	private Class getRealClassOfObject(Object object) {
		Class realClass = null;

		// Grab the name from the object class
		realClass = object.getClass();

		// Check for Hibernate, then replace with real class name if proxy
		if (realClass.getName().contains("EnhancerByCGLIB")) {
			realClass = Hibernate.getClass(object);
		}
		return realClass;
	}

	/**
	 * I had to create this method to handle Hibernate proxies. If you call
	 * getClass().getName() on a Hibernate proxy you get some very strange stuff
	 * and you need to drill down to the real object to get the correct class
	 * name
	 * 
	 * @param object
	 * @return
	 */
	private String getRealClassNameOfObject(Object object) {
		String realClassName = null;

		// Grab the name from the object class
		realClassName = object.getClass().getName();

		// Check for Hibernate, then replace with real class name if proxy
		if (realClassName.contains("EnhancerByCGLIB")) {
			realClassName = Hibernate.getClass(object).getName();
		}
		return realClassName;
	}

	private Document document;

	/**
	 * Collection of Objects that could be elements just below the root of the
	 * XML document (i.e Device, Deployment, DataProducers (and subclasses)
	 */
	private Collection rootElements = new ArrayList();

	/**
	 * The local package. XmlBuilder should be put in the same package as the
	 * model classes. Itonly marshalls objects in its package.
	 */
	private Package p;

	private static int objectToElementCount = 0;
	private static final int maxObjectToElementCount = 10000;

	private XmlDateFormat dateFormat = new XmlDateFormat();

	private ArrayList objList;

	/**
	 * Log4J Stuff
	 */
	static Logger logger = Logger.getLogger(XmlBuilder.class);
	static Level level = Level.DEBUG;
}
