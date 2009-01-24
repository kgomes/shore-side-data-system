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
package moos.ssds.services.servlet.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.util.MetadataFactory;

import org.apache.log4j.Logger;

/**
 * This class is to be used to facilitate calling methods on objects and can
 * take in URL fragments to parse out the parameters. An example of the
 * parameter:
 * 
 * <pre>
 *     &amp;p1Type=java.util.Date&amp;p1Value=2005-10-10T00:00:00Z
 * </pre>
 * 
 * This object can be constructed by either handing in an HTTP request that
 * contains that information in the parameters of by handing it a string that is
 * of the previous form. A delimiter may be needed if one of the object is a
 * <code>IMetadataObject</code> in string representation
 * 
 * @author kgomes
 */
public class MethodProxy {

    /**
     * This is the constructor that takes in an HTTPRequest and then parses it
     * to find all the appropriate information
     * 
     * @param objectToInvokeMethodOn
     * @param request
     */
    public MethodProxy(Object objectToInvokeMethodOn, HttpServletRequest request) {
        this.resetMethodProxy(objectToInvokeMethodOn, request);
    }

    /**
     * @param objectToInvokeMethodOn
     * @param fullQueryString
     *            the parameters must be separated by &amp; signs
     */
    // public MethodProxy(Object objectToInvokeMethodOn, String fullQueryString)
    // {}
    /**
     * @param objectToInvokeMethodOn
     * @param methodName
     * @param methodParamString
     *            these parameter pairs must be separated by the &amp; sign
     * @param delimiter
     */
    // public MethodProxy(Object objectToInvokeMethodOn, String methodName,
    // String methodParamString, String delimiter) {}
    /**
     * These are all getters for the status and messages for the method proxy.
     */
    public boolean isOkToCallMethod() {
        return okToCallMethod;
    }

    public String getFaultCode() {
        return faultCode;
    }

    public String getFaultDetail() {
        return faultDetail;
    }

    public String getFaultReason() {
        return faultReason;
    }

    public String getFaultSubcode() {
        return faultSubcode;
    }

    public void resetMethodProxy(Object objectToInvokeMethodOn,
        HttpServletRequest request) {
        this.objectToInvokeOn = objectToInvokeMethodOn;
        if ((objectToInvokeMethodOn != null) && (request != null)) {
            // Parse out the parameters from the request
            this.findParametersFromHTTPRequest(request);
            if (okToCallMethod) {
                // Now build the objects that will be passed into the method
                this.buildParameterObjects();
                if (okToCallMethod) {
                    // Now extract the method that will be called
                    this.findMethod();
                }
            }
            // Now the proxy should be ready to call
        } else {
            okToCallMethod = false;
            this.faultCode = ServletFaultHandler.FAULT_CODE_SENDER;
            this.faultSubcode = "Missing Parameter";
            this.faultReason = "You need to specify an object to be able to invoke methods on it";
            this.faultDetail = "This message is from the MessageProxy class that is trying to"
                + " invoke a method you specified on some object you specified";
        }
    }

    /**
     * This is the method that will actually call the method with the parameters
     * supplied at instantiation and then return the result
     * 
     * @return
     */
    public Object invokeMethod() {
        Object objectToReturn = null;

        // First check status
        if (okToCallMethod) {
            if (objectToInvokeOn != null) {
                try {
                    objectToReturn = methodToInvoke.invoke(objectToInvokeOn,
                        parameters);
                } catch (IllegalArgumentException e) {
                    okToCallMethod = false;
                    this.faultCode = ServletFaultHandler.FAULT_CODE_RECEIVER;
                    this.faultSubcode = "Exception Caught";
                    this.faultReason = "IllegalArgumentException Caught";
                    this.faultDetail = "An IllegalArgumentException was caught trying "
                        + "to invoke the method, the details are: "
                        + e.getMessage();
                } catch (IllegalAccessException e) {
                    okToCallMethod = false;
                    this.faultCode = ServletFaultHandler.FAULT_CODE_RECEIVER;
                    this.faultSubcode = "Exception Caught";
                    this.faultReason = "IllegalAccessException Caught";
                    this.faultDetail = "An IllegalAccessException was caught trying "
                        + "to invoke the method, the details are: "
                        + e.getMessage();
                } catch (InvocationTargetException e) {
                    okToCallMethod = false;
                    this.faultCode = ServletFaultHandler.FAULT_CODE_RECEIVER;
                    this.faultSubcode = "Exception Caught";
                    this.faultReason = "InvocationTargetException Caught";
                    this.faultDetail = "An InvocationTargetException was caught trying "
                        + "to invoke the method, the details are: "
                        + e.getTargetException().getMessage();
                }
            }
        }

        return objectToReturn;
    }

    /**
     * This method takes in a <code>HttpServletRequest</code> and extracts all
     * the pNTypes and pNValues into the appropriate TreeMaps
     * 
     * @param request
     */
    private void findParametersFromHTTPRequest(HttpServletRequest request) {
        // If delimiter is specified, grab it
        if (request.getParameter("delimiter") != null) {
            this.delimiter = request.getParameter("delimiter");
        }

        // Grab the method name
        if (request.getParameter("method") != null) {
            this.method = request.getParameter("method");
        } else {
            okToCallMethod = false;
            this.faultCode = ServletFaultHandler.FAULT_CODE_SENDER;
            this.faultSubcode = "Missing Parameter";
            this.faultReason = "The name of the method to invoke is missing";
            this.faultDetail = "In order to invoke a method using the MethodProxy "
                + "class, a method name must be specified.";
            return;
        }

        // Clear the number of method parameters
        numberOfMethodParameters = 0;

        // Grab all the parameter keys
        Enumeration parameterKeyEnumeration = request.getParameterNames();

        // Clear the TreeMaps
        parameterTypeTreeMap = new TreeMap();
        parameterValueTreeMap = new TreeMap();

        // Now create Regexp patterns to look for the pNType and pNValues
        Pattern paramTypePattern = null;
        Pattern paramValuePattern = null;
        try {
            paramTypePattern = Pattern.compile("p(\\d+)Type");
            paramValuePattern = Pattern.compile("p(\\d+)Value");
        } catch (Throwable e) {
            logger.error("Could not compile patterns: " + e.getMessage());
            okToCallMethod = false;
            this.faultCode = ServletFaultHandler.FAULT_CODE_RECEIVER;
            this.faultSubcode = "Exception Caught";
            this.faultReason = "Throwable Caught";
            this.faultDetail = "Throwable caught trying to compile patterns for matching"
                + e.getMessage();
            return;
        }

        // Now loop over keys and look for method parameters
        while (parameterKeyEnumeration.hasMoreElements()) {
            String paramName = (String) parameterKeyEnumeration.nextElement();
            // If it matches the pNType, grab the value and put it in the
            // approriate positioned slot
            Matcher typeMatcher = paramTypePattern.matcher(paramName);
            if (typeMatcher.matches()) {
                parameterTypeTreeMap.put(new Integer(typeMatcher.group(1)),
                    request.getParameter(paramName));
                numberOfMethodParameters++;
            }
            // Same for the value
            Matcher valueMatcher = paramValuePattern.matcher(paramName);
            if (valueMatcher.matches()) {
                parameterValueTreeMap.put(new Integer(valueMatcher.group(1)),
                    request.getParameter(paramName));
            }
        }
        okToCallMethod = true;
    }

    /**
     * This method will take the strings that are in the parameter
     * <code>TreeMap</code>s and convert them to an array of
     * <code>Object</code>s that can be used in the method call
     * 
     * @return
     * @throws IllegalArgumentException
     */
    private void buildParameterObjects() {

        // Build an array of objects that are the same size as the parameter
        // trees
        parameters = new Object[this.parameterTypeTreeMap.size()];
        parameterClasses = new Class[this.parameterTypeTreeMap.size()];

        // NOTE, as a rule, I am going to convert all primatives to their
        // equivalent object as the relection mechanism will take care of
        // the translation if parameters are primatives

        // Loop over parameters
        for (int i = 0; i < this.parameterTypeTreeMap.size(); i++) {
            // Grab the parameterType
            String requestedType = (String) parameterTypeTreeMap
                .get(new Integer(i + 1));
            Class requestedClass = null;
            try {
                // OK, grab the class from the type specified
                requestedClass = MetadataFactory.findClassByName(requestedType);
            } catch (ClassNotFoundException e1) {
                logger.error("ClassNotFoundException: " + e1.getMessage());
                okToCallMethod = false;
                this.faultCode = ServletFaultHandler.FAULT_CODE_RECEIVER;
                this.faultSubcode = "Exception Caught";
                this.faultReason = "ClassNotFoundException Caught";
                this.faultDetail = "An ClassNotFoundException was caught trying "
                    + "to build objects from the parameters specified, the details are: "
                    + e1.getMessage();
                return;
            }

            // Set the class first
            parameterClasses[i] = requestedClass;
            // Now try to construct an object from the value for that class
            try {
                parameters[i] = MetadataFactory.getObject(
                    requestedClass, (String) parameterValueTreeMap
                        .get(new Integer(i + 1)), delimiter);
            } catch (Throwable e1) {
                logger.error("Could not construct parameter object: "
                    + e1.getMessage());
                okToCallMethod = false;
                this.faultCode = ServletFaultHandler.FAULT_CODE_RECEIVER;
                this.faultSubcode = "Exception Caught";
                this.faultReason = "Throwable Caught";
                this.faultDetail = "Throwable caught trying to create an object from the parameter value, details: "
                    + e1.getMessage();
                return;
            }
        }
        okToCallMethod = true;
    }

    /**
     * This will try to find a method on the objectToInvokedOn that matches the
     * method signature supplied
     * 
     * @param object
     *            the object to check for method with the right name
     * @param methodName
     *            the name to check for
     * @return true if the method was found, false otherwise
     */
    private void findMethod() {

        // Since we have the name and the class array, we can try to find one
        // straight out
        try {
            methodToInvoke = objectToInvokeOn.getClass().getDeclaredMethod(
                method, parameterClasses);
        } catch (SecurityException e) {} catch (NoSuchMethodException e) {}

        if (methodToInvoke == null) {
            // Grab all the methods from the object
            Method[] objectMethods = objectToInvokeOn.getClass().getMethods();

            // Now if no methods were found return
            if ((objectMethods == null) || (objectMethods.length == 0)) {
                okToCallMethod = false;
                this.faultCode = ServletFaultHandler.FAULT_CODE_RECEIVER;
                this.faultSubcode = "Incorrect Parameter";
                this.faultReason = "The object to invoke methods on appears invalid";
                this.faultDetail = "No methods could be found on the class of the object specified";
                return;
            }
            // Loop through the methods and see return the first match
            for (int methodCounter = 0; methodCounter < objectMethods.length; methodCounter++) {
                // Grab the method
                Method methodToTest = objectMethods[methodCounter];

                // First check name
                if (methodToTest.getName().equals(method)) {

                    // Grab the parameter types from the method we are examining
                    Class[] parameterTypes = methodToTest.getParameterTypes();

                    // First check parameter sizes
                    if (parameterTypes.length != parameters.length) {
                        continue;
                    } else {
                        // If there are no parameters, return the method
                        if (parameterTypes.length == 0) {
                            methodToInvoke = methodToTest;
                            okToCallMethod = true;
                            return;
                        } else {
                            // First assume the methods are equal
                            methodToInvoke = methodToTest;
                            // Loop through parameters and if any of the classes
                            // do
                            // not match, un-assign it and skip
                            for (int j = 0; j < parameterTypes.length; j++) {
                                // Check to see if the class are mismatched
                                logger.debug("Comparing "
                                    + ((Class) parameterTypes[j]).getName()
                                    + " with "
                                    + parameters[j].getClass().getName());
                                if (!((Class) parameterTypes[j]).getName()
                                    .equals(parameters[j].getClass().getName())) {
                                    // Next, make sure the first class name
                                    // isn't IMetadataObject as that will mess
                                    // up the compare
                                    if (((Class) parameterTypes[j]).getName()
                                        .contains("IMetadataObject")) {
                                        if (!(parameters[j] instanceof IMetadataObject)) {
                                            methodToInvoke = null;
                                            break;
                                        }
                                    } else {
                                        methodToInvoke = null;
                                        break;
                                    }
                                }
                            }
                            // If the method is still associated, return
                            if (methodToInvoke != null) {
                                okToCallMethod = true;
                                return;
                            }
                        }
                    }
                }
            }
        } else {
            return;
        }
        // If we are here, no method was found
        okToCallMethod = false;
        this.faultCode = ServletFaultHandler.FAULT_CODE_RECEIVER;
        this.faultSubcode = "Method Not Found";
        this.faultReason = "The specified method was not found on the object.";
        this.faultDetail = "The specified method could not be found on the incoming object";
    }

    /**
     * This is the integer that keeps track of how many method parameters were
     * specified when this proxy was created
     */
    int numberOfMethodParameters = 0;

    /**
     * This is a <code>TreeMap</code> that contains ordered
     * <code>String</code>s that are the <code>Object</code> types
     * specified in the parameter list. For example, if the parameter string
     * specfied:
     * 
     * <pre>
     *     &amp;p1Type=java.util.Date&amp;p1Value=2005-10-10T00:01:00Z
     * </pre>
     * 
     * this <code>TreeMap</code> would contain the string
     * &quot;java.util.Date&quot; in the number 1 spot. The order is extracted
     * from the number in the parameter name
     */
    private TreeMap parameterTypeTreeMap = new TreeMap();

    /**
     * This is a <code>TreeMap</code> that contains ordered
     * <code>String</code>s that are the values specified in the parameter
     * list. For example, if the parameter string specfied:
     * 
     * <pre>
     *     &amp;p1Type=java.util.Date&amp;p1Value=2005-10-10T00:01:00Z
     * </pre>
     * 
     * this <code>TreeMap</code> would contain the string
     * &quot;2005-10-10T00:01:00Z&quot; in the number 1 spot. The order is
     * extracted from the number in the parameter name
     */
    private TreeMap parameterValueTreeMap = new TreeMap();

    /**
     * This is the object that will have it's method called
     */
    private Object objectToInvokeOn = null;

    /**
     * This is an array of Class that will help in the discovery of the correct
     * Method to call
     */
    private Class[] parameterClasses = null;

    /**
     * This is the array of objects that will be used to pass to the method to
     * be called
     */
    private Object[] parameters = null;

    /**
     * This is the actual method that will be called
     */
    private String method = null;
    private Method methodToInvoke = null;

    /**
     * This is the delimiter that is used in the string representation of a
     * <code>IMetadataObject</code> if one is specified as a parameter. For
     * example, if one of the parameters is a <code>Person</code> object, the
     * incoming query string might look like:
     * 
     * <pre>
     *     &amp;p1Type=Person&amp;p1Value=Person|name=John Doe|username=jdoe&amp;delimiter=|
     * </pre>
     * 
     * and the delimiter should be the | symbol
     */
    private String delimiter = null;

    /**
     * This is a boolean that indicates if the incoming parameters were
     * understandable and the method could be called. If the method cannot be
     * called, it will be <code>false</code>. If it is ready to go, it will
     * be <code>true</code>.
     */
    private boolean okToCallMethod = false;

    /**
     * These are the fault messages that are populated during construction
     */
    private String faultCode = "";
    private String faultSubcode = "";
    private String faultReason = "";
    private String faultDetail = "";

    /**
     * A log4j Logger
     */
    static Logger logger = Logger.getLogger(MethodProxy.class);

}