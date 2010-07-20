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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataProducer;
import moos.ssds.util.XmlDateFormat;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.log4j.Level;
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
     * The constructor for an object builder
     * 
     * @param url
     *            This is the file URL that points to the XML file used to build
     *            the objects from
     */
    public ObjectBuilder(URL url) {

        // Grab the incoming URL and store it locally
        this.url = url;
        // Clear the flag to read from string
        this.xmlInStringFormat = false;

        logger.setLevel(level);
        logger.debug("ObjectBuilder constructor called with URL = "
            + url.toString());

    }

    /**
     * This constructor takes in an XML document in a String form and then sets
     * up for unmarshalling
     * 
     * @param xmlDocument
     */
    public ObjectBuilder(String xmlDocument) {
        // Set some local variables
        this.xmlDocument = xmlDocument;
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
            logger
                .error("Exception caught while trying to create an XOM Builder:"
                    + e.getMessage());
        }
        if (this.xmlInStringFormat) {
            try {
                xmlToObjects(null);
            } catch (Exception e) {
                logger
                    .error("Exception caught while trying to convert XML to objects: "
                        + e.getMessage());
            }

        } else {
            try {
                logger.debug("Unmarshal called");
                xmlToObjects(url.toExternalForm());
            } catch (Exception e) {
                logger
                    .error("Exception caught while trying to convert XML to objects:"
                        + e.getMessage());
                errorMessageBuffer
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
        return errorMessageBuffer.toString();
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
            logger
                .error(getClass().getName() + ": Unable to parse" + xml + ".");
        } catch (IOException e) {
            logger.error(getClass().getName() + ": Unable to open" + xml
                + ". Exception cause is" + e.getMessage());
        }

        // Grab the root element of the document, if doc is null no objects are
        // built
        if (document != null) {
            Element root = document.getRootElement();
            logger.debug("Root element's localName is " + root.getLocalName());

            // Grab all the children elements
            Elements children = root.getChildElements();
            // Grab the number of children elements
            int size = children.size();
            logger.debug("There are " + size + " child elements.");

            // This is a bit of a hack, in that some documents come in with the
            // head being a DataProducerGroup and the child being a
            // DataProducer. The relational model is the other way around so, I
            // will flip parent/child relationship to keep things sane
            if (size == 1) {
                Element topElement = children.get(0);
                if (topElement.getLocalName().equalsIgnoreCase(
                    "DataProducerGroup")) {
                    // Grab the children
                    Elements dpgChildren = topElement.getChildElements();
                    // Grab the first child
                    if (dpgChildren.size() > 0) {
                        root.removeChild(topElement);
                        topElement.removeChildren();
                        for (int i = 0; i < dpgChildren.size(); i++) {
                            Element tempDPGChild = dpgChildren.get(i);
                            // If it is a DataProducer (or Deployment), flip the
                            // relationship
                            if (tempDPGChild.getLocalName().equalsIgnoreCase(
                                "DataProducer")
                                || tempDPGChild.getLocalName()
                                    .equalsIgnoreCase("Deployment")) {
                                tempDPGChild.appendChild(topElement);
                                root.appendChild(tempDPGChild);
                            } else if (tempDPGChild.getLocalName()
                                .equalsIgnoreCase("description")) {
                                topElement.addAttribute(new Attribute(
                                    "description", tempDPGChild.getValue()));
                            }
                        }
                        children = root.getChildElements();
                        size = children.size();
                    }
                }
            }

            // Create a place holder for an object, class, boolean, and
            // collection
            Object obj = null;
            Class c = null;
            boolean hasClass = false;
            Collection list = null;

            // Now loop through all the children elements
            for (int i = 0; i < size; i++) {

                // Grab the ith element and convert it to an object
                obj = elementToObject(children.get(i));

                // Now get the class of that object
                c = obj.getClass();

                // Check to see if the class is in the hash map
                hasClass = objects.keySet().contains(c);

                // If not, add it with an array list that will
                // be used to store object handles
                if (!hasClass) {
                    // list = new ArrayList();
                    list = new HashSet();
                    objects.put(c, list);
                }

                // Grab the array list of the objects with
                // that are of the class of the new object
                list = (Collection) objects.get(c);

                // Now add the new object to that list
                list.add(obj);
            }
        }
    }

    /**
     * Converts an XML Element into an Object For processing the Objects just
     * below root in the XML
     */
    private Object elementToObject(Element element) throws Exception,
        IllegalAccessException, ClassNotFoundException, InstantiationException {

        // Grab the name of the incoming element
        String name = element.getLocalName();
        logger.debug("elementToObject called with element with localName "
            + name);

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
            element
                .addAttribute(new Attribute("tagString", element.getValue()));
        }

        // If the tag is a DataFile or DataStream and there is an attribute
        // called "contentType" we changed that to "mimeType" so make that
        // change as well.
        if ((name.equals("DataFile") || name.equals("DataStream"))
            && element.getAttribute("contentType") != null) {
            Attribute contentType = element.getAttribute("contentType");
            Attribute mimeType = new Attribute("mimeType", contentType
                .getValue());
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
        if (isObject) {
            logger.debug(name + " appears to be an Object");
            // Create the class
            c = Class.forName(modelPackage + "." + name);
            // Now create a new instance of that class
            obj = c.newInstance();
            // Now call on the builder to fill out that object
            buildObject(obj, element);
        } else {
            // If it's not an object ignore it. All elements just below
            // root should be objects.

        }
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
                logger
                    .debug("It is a property and the childObject is not null");
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
            errorMessageBuffer.append("Throwable caught: " + e.getMessage());
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
                        method.invoke(parentObj, new Object[]{value});
                    } catch (InvocationTargetException e) {
                        // Pass that one up the stack
                        buildFailed = true;
                        errorMessageBuffer.append(e.getTargetException()
                            .getMessage());
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
        Class[] parameterTypes = new Class[]{childObj.getClass()};
        Object[] arguments = new Object[]{childObj};
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
            errorMessageBuffer.append("NoSuchMethodException: problem with "
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
     * Add an Object, represented by an <code>Element</code> to a parent
     * Object by calling a method other than Java Bean get and set methods.
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
            Class[] c = new Class[]{childClass};
            // Grab the method from the parent object which matches
            // the method name that was an input parameter
            Method m = parentObject.getClass().getMethod(methodName, c);
            // Now invoke the method on the parent and hand it the array
            // of objects
            m.invoke(parentObject, new Object[]{childObject});
            // } catch (InvocationTargetException e) {
            // logger.error("Unable to add the child object to the parent
            // object: " + e.getMessage());
            // Throwable t = e.getTargetException();
            // e.printStackTrace();
        } catch (Exception e) {}
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
            tempChildDataProducer.setName(tempChildDataProducer.getName()
                + "(" + childDataProducerCounter + ")");
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
            out = (Collection) objects.get(Class.forName(className));
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
        Collection cs = objects.values();
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

    /**
     * TODO JavaDoc
     */
    Builder parser;

    /**
     * This is the static string that hold the package name of the ObjectBuilder
     * class.
     */
    private static String modelPackage = "moos.ssds.metadata";

    /**
     * keys = class value = ArrayList of objects of type class, specified by the
     * key.
     */
    private Map objects = new HashMap();

    /**
     * The url of the file used to build the objects from (XML file)
     */
    private URL url;

    /**
     * A date formatter used to process dates from XML
     */
    private XmlDateFormat dateFormat = new XmlDateFormat();

    /**
     * This is a boolean to track whether the XML was specified as a string or
     * not.
     */
    private boolean xmlInStringFormat = false;

    /**
     * This is the string that is the XML document if is specified
     */
    private String xmlDocument = null;

    private boolean buildFailed = false;

    /**
     * A counter that can be used to give child data producers unique names if
     * they all have the same name
     */
    private int childDataProducerCounter = 2;

    private StringBuffer errorMessageBuffer = new StringBuffer();

    /**
     * This is a Log4JLogger that is used to log information to
     */
    static Logger logger = Logger.getLogger(ObjectBuilder.class);

    private static Level level = Level.INFO;
}