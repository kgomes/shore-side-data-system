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
package test.moos.ssds.services.metadata;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import junit.framework.TestCase;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.Event;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.Keyword;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.RecordDescription;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.StandardVariable;
import moos.ssds.metadata.UserGroup;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataFactory;
import moos.ssds.services.metadata.IDataContainerAccess;
import moos.ssds.services.metadata.IDataContainerAccess;
import moos.ssds.services.metadata.IDataContainerAccess;
import moos.ssds.services.metadata.DataProducerAccess;
import moos.ssds.services.metadata.DataProducerAccessHome;
import moos.ssds.services.metadata.DataProducerAccessUtil;
import moos.ssds.services.metadata.EventAccess;
import moos.ssds.services.metadata.EventAccessHome;
import moos.ssds.services.metadata.EventAccessUtil;
import moos.ssds.services.metadata.IMetadataAccessRemote;
import moos.ssds.services.metadata.KeywordAccess;
import moos.ssds.services.metadata.KeywordAccessHome;
import moos.ssds.services.metadata.KeywordAccessUtil;
import moos.ssds.services.metadata.PersonAccess;
import moos.ssds.services.metadata.PersonAccessHome;
import moos.ssds.services.metadata.PersonAccessUtil;
import moos.ssds.services.metadata.RecordVariableAccess;
import moos.ssds.services.metadata.RecordVariableAccessHome;
import moos.ssds.services.metadata.RecordVariableAccessUtil;
import moos.ssds.services.metadata.StandardVariableAccess;
import moos.ssds.services.metadata.StandardVariableAccessHome;
import moos.ssds.services.metadata.StandardVariableAccessUtil;
import moos.ssds.services.metadata.UserGroupAccess;
import moos.ssds.services.metadata.UserGroupAccessHome;
import moos.ssds.services.metadata.UserGroupAccessUtil;

import org.apache.log4j.Logger;

/**
 * 
 * @author kgomes
 * 
 */
public abstract class TestAccessCase extends TestCase {

	/**
	 * Constructs a test case with the given name.
	 */
	public TestAccessCase(String name) {
		super(name);
	}

	protected void setUp() {

		// Grab all the Access EJB Home interfaces
		try {
			EVENT_ACCESS_HOME = EventAccessUtil.getHome();
			KEYWORD_ACCESS_HOME = KeywordAccessUtil.getHome();
			PERSON_ACCESS_HOME = PersonAccessUtil.getHome();
			USER_GROUP_ACCESS_HOME = UserGroupAccessUtil.getHome();
			DATA_CONTAINER_ACCESS_HOME = DataContainerAccessUtil.getHome();
			DATA_PRODUCER_ACCESS_HOME = DataProducerAccessUtil.getHome();
			RECORD_VARIABLE_ACCESS_HOME = RecordVariableAccessUtil.getHome();
			STANDARD_VARIABLE_ACCESS_HOME = StandardVariableAccessUtil
					.getHome();
		} catch (NamingException ex) {
			superLogger
					.error("NamingException caught while getting home interfaces "
							+ "from app server: " + ex.getMessage());
		}
		try {
			EVENT_ACCESS = EVENT_ACCESS_HOME.create();
			KEYWORD_ACCESS = KEYWORD_ACCESS_HOME.create();
			PERSON_ACCESS = PERSON_ACCESS_HOME.create();
			USER_GROUP_ACCESS = USER_GROUP_ACCESS_HOME.create();
			DATA_CONTAINER_ACCESS = DATA_CONTAINER_ACCESS_HOME.create();
			DATA_PRODUCER_ACCESS = DATA_PRODUCER_ACCESS_HOME.create();
			RECORD_VARIABLE_ACCESS = RECORD_VARIABLE_ACCESS_HOME.create();
			STANDARD_VARIABLE_ACCESS = STANDARD_VARIABLE_ACCESS_HOME.create();
		} catch (RemoteException e) {
			superLogger
					.error("RemoteException caught while creating access interfaces: "
							+ e.getMessage());
		} catch (CreateException e) {
			superLogger
					.error("CreateException caught while creating access interfaces: "
							+ e.getMessage());
		}

		try {

			// Create Event objects
			EVENT_ONE = (Event) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							EVENT_ONE_STRING_REP, DELIMITER);
			EVENT_TWO = (Event) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							EVENT_TWO_STRING_REP, DELIMITER);

			// Now keywords
			KEYWORD_ONE = (Keyword) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							KEYWORD_ONE_STRING_REP, DELIMITER);
			KEYWORD_TWO = (Keyword) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							KEYWORD_TWO_STRING_REP, DELIMITER);

			// Create Person objects
			PERSON_ONE = (Person) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							PERSON_ONE_STRING_REP, DELIMITER);
			PERSON_TWO = (Person) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							PERSON_TWO_STRING_REP, DELIMITER);

			// Create UserGroup objects
			USER_GROUP_ONE = (UserGroup) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							USER_GROUP_ONE_STRING_REP, DELIMITER);
			USER_GROUP_TWO = (UserGroup) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							USER_GROUP_TWO_STRING_REP, DELIMITER);
			USER_GROUP_THREE = (UserGroup) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							USER_GROUP_THREE_STRING_REP, DELIMITER);

			// Create DataProducer objects
			DATA_PRODUCER_ONE = (DataProducer) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							DATA_PRODUCER_ONE_STRING_REP, DELIMITER);
			DATA_PRODUCER_TWO = (DataProducer) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							DATA_PRODUCER_TWO_STRING_REP, DELIMITER);

			// Create DataContainer and RDs nd RVs
			DATA_CONTAINER_ONE = (DataContainer) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							DATA_CONTAINER_ONE_STRING_REP, DELIMITER);
			DATA_CONTAINER_TWO = (DataContainer) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							DATA_CONTAINER_TWO_STRING_REP, DELIMITER);

			RECORD_DESCRIPTION_ONE = (RecordDescription) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							RECORD_DESCRIPTION_ONE_STRING_REP, DELIMITER);

			RECORD_VARIABLE_ONE = (RecordVariable) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							RECORD_VARIABLE_ONE_STRING_REP, DELIMITER);
			RECORD_VARIABLE_TWO = (RecordVariable) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							RECORD_VARIABLE_TWO_STRING_REP, DELIMITER);
			RECORD_VARIABLE_THREE = (RecordVariable) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							RECORD_VARIABLE_THREE_STRING_REP, DELIMITER);
			RECORD_VARIABLE_FOUR = (RecordVariable) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							RECORD_VARIABLE_FOUR_STRING_REP, DELIMITER);
			RECORD_VARIABLE_FIVE = (RecordVariable) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							RECORD_VARIABLE_FIVE_STRING_REP, DELIMITER);
			RECORD_VARIABLE_SIX = (RecordVariable) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							RECORD_VARIABLE_SIX_STRING_REP, DELIMITER);

			// Add some RecordVariables to the DataCaontainers
			DATA_CONTAINER_ONE.setRecordDescription(RECORD_DESCRIPTION_ONE);
			DATA_CONTAINER_TWO.setRecordDescription(RECORD_DESCRIPTION_ONE);
			DATA_CONTAINER_ONE.addRecordVariable(RECORD_VARIABLE_ONE);
			DATA_CONTAINER_ONE.addRecordVariable(RECORD_VARIABLE_TWO);
			DATA_CONTAINER_ONE.addRecordVariable(RECORD_VARIABLE_THREE);
			DATA_CONTAINER_TWO.addRecordVariable(RECORD_VARIABLE_FOUR);
			DATA_CONTAINER_TWO.addRecordVariable(RECORD_VARIABLE_FIVE);
			DATA_CONTAINER_TWO.addRecordVariable(RECORD_VARIABLE_SIX);

			// Create some StandardVariables
			STANDARD_VARIABLE_ONE = (StandardVariable) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							STANDARD_VARIABLE_ONE_STRING_REP, DELIMITER);
			STANDARD_VARIABLE_TWO = (StandardVariable) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							STANDARD_VARIABLE_TWO_STRING_REP, DELIMITER);

		} catch (MetadataException e) {
			superLogger
					.error("MetadataException caught trying to create objects: "
							+ e.getMessage());
		} catch (ClassCastException cce) {
			superLogger
					.error("ClassCastException caught trying to create objects: "
							+ cce.getMessage());
		}
	}

	static Logger superLogger = Logger.getLogger(TestAccessCase.class);

	/**
	 * @param unmarshalledCollection
	 */
	// protected Object getFirstObjectFromCollection(
	// Collection unmarshalledCollection) {
	// Object retVal = null;
	// if (unmarshalledCollection != null) {
	// Iterator obIter = unmarshalledCollection.iterator();
	// if (obIter.hasNext()) {
	// try {
	// retVal = obIter.next();
	// } catch (ClassCastException e) {
	// }
	// }
	// }
	// return retVal;
	// }
	/**
	 * @param xmlLocation
	 * @return
	 */
	// protected Collection getUnmarshalledCollection(String xmlLocation) {
	// // ---------------------------------------------
	// // Create deviceTypeOne
	// // ---------------------------------------------
	// // Grab the file from the classpath
	// ObjectBuilder objectBuilder = new ObjectBuilder(this.getClass()
	// .getResource(xmlLocation));
	// // Now unmarshal the XML to objects
	// objectBuilder.unmarshal();
	//
	// // Grab the top level collection from the object builder
	// Collection unmarshalledCollection = objectBuilder.listAll();
	// return unmarshalledCollection;
	// }
	/**
	 * This method creates a map of methods that can be considered "getters"
	 */
	private Map<String, Method> getMethodMapOfTestableGetters(Method[] methods) {
		Map<String, Method> methodsMap = new HashMap<String, Method>();
		for (int i = 0; i < methods.length; i++) {
			if ((methods[i].getName().startsWith("get") || (methods[i]
					.getName().startsWith("is")))
					&& !methods[i].getName().equals("getId")
					&& !methods[i].getName().equals("getDateRange")
					&& !methods[i].getName().equals("getVersion")
					&& !methods[i].getName().equals("getPassword")
					&& !methods[i].getName().startsWith("isValid")) {
				// Check the return type to make sure it is a simple property
				// method
				if ((methods[i].getReturnType().equals(Float.class))
						|| (methods[i].getReturnType().equals(Double.class))
						|| (methods[i].getReturnType().equals(Long.class))
						|| (methods[i].getReturnType().equals(String.class))
						|| (methods[i].getReturnType().equals(Date.class))
						|| (methods[i].getReturnType().equals(URL.class))
						|| (methods[i].getReturnType().isPrimitive())) {
					methodsMap.put(methods[i].getName(), methods[i]);
				}
			}
		}
		return methodsMap;
	}

	// private Map getMethodMapOfTestableListers(Method[] methods) {
	// Map methodsMap = new HashMap();
	// for (int i = 0; i < methods.length; i++) {
	// if (methods[i].getName().startsWith("list")) {
	// // Check the return type to make sure it is a simple property
	// // method
	// if (methods[i].getReturnType().equals(Collection.class)) {
	// if (methods[i].getName().equals("listInputs")
	// || methods[i].getName().equals("listOutputs")
	// || methods[i].getName().equals("listDataProducers")) {
	// methodsMap.put(methods[i].getName(), methods[i]);
	// }
	// }
	// }
	// }
	// return methodsMap;
	// }

	/**
	 * The method to compare two objects "get" methods (Java Beans style)
	 */
	protected void testEqualityOfAllGetters(Object object1, Object object2) {

		// Check to make sure objects are real
		assertNotNull("Object1 should not be null", object1);
		assertNotNull("Object2 should not be null", object2);

		// first make sure the two objects are of the same class
		assertEquals(object1.getClass(), object2.getClass());

		Method[] methods = object1.getClass().getDeclaredMethods();
		Method[] methods2 = object2.getClass().getDeclaredMethods();

		// get all the getter methods and put them in a hash with the key being
		// the method name and value being the method
		Map<String, Method> methods1Map = getMethodMapOfTestableGetters(methods);
		Map<String, Method> methods2Map = getMethodMapOfTestableGetters(methods2);

		Object[] noargs = new Object[] {};
		for (Iterator<String> iter = methods1Map.keySet().iterator(); iter
				.hasNext();) {
			String methodName = (String) iter.next();
			Method method1 = (Method) methods1Map.get(methodName);
			Method method2 = (Method) methods2Map.get(methodName);
			assertNotNull("The method, " + methodName + ", should exist",
					method1);
			assertNotNull("The method, " + methodName + ", should exist",
					method2);

			Object result1 = null, result2 = null;
			try {
				result1 = method1.invoke(object1, noargs);
			} catch (IllegalArgumentException e) {
				fail("Error calling getter method on " + object1 + "\n"
						+ e.getMessage());
			} catch (IllegalAccessException e) {
				fail("Error calling getter method on " + object1 + "\n"
						+ e.getMessage());
			} catch (InvocationTargetException e) {
				fail("Error calling getter method on " + object1 + "\n"
						+ e.getMessage());
			}
			try {
				result2 = method2.invoke(object2, noargs);
			} catch (IllegalArgumentException e) {
				fail("Error calling getter method on " + object2 + "\n"
						+ e.getMessage());
			} catch (IllegalAccessException e) {
				fail("Error calling getter method on " + object2 + "\n"
						+ e.getMessage());
			} catch (InvocationTargetException e) {
				fail("Error calling getter method on " + object2 + "\n"
						+ e.getMessage());
			}
			if ((result1 == null && result2 != null)
					|| (result1 != null && result2 == null)) {
				assertNotNull(
						"Result from method 1 should not be null (method1 = "
								+ method1.getName() + ", method2 = "
								+ method2.getName(), result1);
				assertNotNull(
						"Result from method 2 should not be null (method1 = "
								+ method1.getName() + ", method2 = "
								+ method2.getName(), result2);
			} else {
				if ((result1 instanceof java.util.Date)
						&& (result2 instanceof java.util.Date)) {
					Date date1 = (Date) result1;
					Date date2 = (Date) result2;
					assertTrue(
							"The two dates should be within a couple of seconds of each other",
							((date1.getTime() <= (date2.getTime() + 2000)) && (date1
									.getTime() >= (date2.getTime() - 2000))));
					// assertEquals("The two dates should be equal:", date1
					// .getTime() / 1000, date2.getTime() / 1000);
				} else if ((result1 instanceof java.util.Calendar)
						&& (result2 instanceof java.util.Calendar)) {
					Calendar cal1 = (Calendar) result1;
					Calendar cal2 = (Calendar) result2;
					Date date1 = cal1.getTime();
					Date date2 = cal2.getTime();
					assertTrue(
							"The two dates should be within a couple of seconds of each other",
							((date1.getTime() <= (date2.getTime() + 2000)) && (date1
									.getTime() >= (date2.getTime() - 2000))));
					// assertEquals("The two dates should be equal:", date1
					// .getTime() / 1000, date2.getTime() / 1000);
				} else {
					if (result1 != null && result2 != null) {
						assertTrue("the attribute values:\n" + result1 + "\n"
								+ result2 + "\nshould be the same", result1
								.equals(result2));
					} else {
						superLogger
								.debug("Both values to be compared are null (method1 = "
										+ method1.getName()
										+ ", method2 = "
										+ method2.getName());
					}
				}
			}

		}
	}

	// protected void testEqualityOfAllListers(Object object1, Object object2) {
	// // first make sure the two objects are of the same class
	// // assertEquals(object1.getClass(), object2.getClass());
	//
	// // Get all the methods as the list...() methods are in DataProducer
	// Method[] methods = object1.getClass().getMethods();
	// Method[] methods2 = object2.getClass().getMethods();
	//
	// // get all the getter methods and put them in a hash with the key being
	// // the method name and value being the method
	// Map methods1Map = getMethodMapOfTestableListers(methods);
	// Map methods2Map = getMethodMapOfTestableListers(methods2);
	//
	// Object[] noargs = new Object[]{};
	// for (Iterator iter = methods1Map.keySet().iterator(); iter.hasNext();) {
	// String methodName = (String) iter.next();
	// Method method1 = (Method) methods1Map.get(methodName);
	// Method method2 = (Method) methods2Map.get(methodName);
	// assertNotNull("The method, " + methodName + ", should exist",
	// method1);
	// assertNotNull("The method, " + methodName + ", should exist",
	// method2);
	//
	// Object result1 = null, result2 = null;
	// try {
	// result1 = method1.invoke(object1, noargs);
	// } catch (IllegalArgumentException e) {
	// fail("Error calling getter method on " + object1 + "\n"
	// + e.getMessage());
	// } catch (IllegalAccessException e) {
	// fail("Error calling getter method on " + object1 + "\n"
	// + e.getMessage());
	// } catch (InvocationTargetException e) {
	// fail("Error calling getter method on " + object1 + "\n"
	// + e.getMessage());
	// }
	// try {
	// result2 = method2.invoke(object2, noargs);
	// } catch (IllegalArgumentException e) {
	// fail("Error calling getter method on " + object2 + "\n"
	// + e.getMessage());
	// } catch (IllegalAccessException e) {
	// fail("Error calling getter method on " + object2 + "\n"
	// + e.getMessage());
	// } catch (InvocationTargetException e) {
	// fail("Error calling getter method on " + object2 + "\n"
	// + e.getMessage());
	// }
	// if ((result1 == null && result2 != null)
	// || (result1 != null && result2 == null)) {
	// assertNotNull(
	// "Result from method 1 should not be null (method1 = "
	// + method1.getName() + ", method2 = "
	// + method2.getName(), result1);
	// assertNotNull(
	// "Result from method 2 should not be null (method1 = "
	// + method1.getName() + ", method2 = "
	// + method2.getName(), result2);
	// } else {
	// // For now just test that the collections are the same size
	// if ((result1 instanceof Collection)
	// && (result2 instanceof Collection)) {
	// int size1 = ((Collection) result1).size();
	// int size2 = ((Collection) result2).size();
	// logger.debug("Comparing size of collection returned by "
	// + method1.getName() + " size1 = " + size1
	// + ", size2 = " + size2);
	// if (size1 != size2) {
	// logger.error("result1 = " + result1 + ", result2 = "
	// + result2);
	// }
	// assertEquals(
	// "The two Collections should have the same size:",
	// size1, size2);
	// } else {
	//
	// }
	// }
	// }
	// }
	/**
	 * @param deviceType
	 * @param deviceTypeId
	 * @return
	 */
	protected Long testInsert(IMetadataObject metadataObject,
			IMetadataAccessRemote access) {

		// Insert and grab the ID
		Long id = null;
		try {
			id = access.insert(metadataObject);
		} catch (RemoteException e) {
			superLogger.error("RemoteException: " + e.getMessage());
		} catch (MetadataAccessException e) {
			superLogger.error("MetadataAccessException " + e.getMessage());
		}
		// Check that the ID is not null
		assertNotNull("Returned ID for " + metadataObject.getClass()
				+ " object should not be null", id);

		// Get the interface back and then insert it again, this should throw an
		// exception
		IMetadataObject afterInsert = null;
		try {
			afterInsert = access.findById(id, false);
		} catch (RemoteException e1) {
			superLogger.error("RemoteException: " + e1.getMessage());
		} catch (MetadataAccessException e1) {
			superLogger.error("MetadataAccessException " + e1.getMessage());
		}

		// Assert that it was found
		assertNotNull("Returned object from findById should not be null",
				afterInsert);

		// Assert the ID's are the same
		assertEquals("The ID's should be the same.", afterInsert.getId(), id);

		// Now try to insert the proxy
		boolean exceptionThrown = false;
		Long reinsertID = null;
		try {
			reinsertID = access.insert(afterInsert);
		} catch (RemoteException e2) {
			superLogger.error("RemoteException: " + e2.getMessage());
		} catch (MetadataAccessException e2) {
			superLogger.error("MetadataAccessException " + e2.getMessage());
			exceptionThrown = true;
		}

		// Assert that the id was not null
		assertTrue("An exception should have been thrown", exceptionThrown);

		// Assert that the returned ID is null
		assertNull("Returned ID after second insert should be null.",
				reinsertID);

		// Return the ID for further use
		return id;
	}

	// to test the update, make a new object that is the same type as the
	// original value object, and set a couple of attributes on it. Then set the
	// id of that object to the id of the passed in valueObject id. Then do an
	// access.update using the new object, then retrieve the object by id from
	// the database and make sure that the attributes which were changed show up
	// as changed, and the rest of the attributes stayed the same.
	@SuppressWarnings("unchecked")
	protected void testUpdate(IMetadataObject metadataObject,
			HashMap<String, Object[]> methodsToUpdate,
			IMetadataAccessRemote access) {
		superLogger.debug("testUpdate entered");

		// First make sure everything is legit
		assertNotNull(metadataObject);
		assertNotNull(methodsToUpdate);
		assertNotNull(access);
		superLogger.debug("All values OK (not null)");

		// Grab the incoming value object
		IMetadataObject metadataObjectToWorkWith = metadataObject;
		// Get the id of the object to update
		Long id = metadataObjectToWorkWith.getId();
		superLogger.debug("Id of object to update is " + id);

		// Get all the methods available
		Method[] methods = metadataObjectToWorkWith.getClass()
				.getDeclaredMethods();
		superLogger.debug("There are " + methods.length
				+ " methods on the incoming object");

		// Now grab the map of valid methods to check
		Map<String, Method> methodsMap = getMethodMapOfTestableGetters(methods);
		superLogger.debug("Out of all the methods, there are "
				+ methodsMap.size() + " valid getters");

		// Now make a copy of the results of those getters in a
		// hashmap for later comparison
		HashMap<String, Object> oldValues = new HashMap();
		Object[] noargs = new Object[] {};
		for (Iterator<String> iter = methodsMap.keySet().iterator(); iter
				.hasNext();) {
			String methodName = (String) iter.next();
			superLogger
					.debug("Going to store old value for the return of method "
							+ methodName);
			Method method = (Method) methodsMap.get(methodName);
			try {
				oldValues.put(methodName, method.invoke(
						metadataObjectToWorkWith, noargs));
			} catch (IllegalArgumentException e) {
				superLogger
						.error("IllegalArgumentException: " + e.getMessage());
			} catch (IllegalAccessException e) {
				superLogger.error("IllegalAccessException: " + e.getMessage());
			} catch (InvocationTargetException e) {
				superLogger.error("InvocationTargetException: "
						+ e.getMessage());
			}
		}

		// OK, now go through the map of field name and object and invoke the
		// set method for that to change the value
		for (Iterator<String> iter = methodsToUpdate.keySet().iterator(); iter
				.hasNext();) {
			// Grab the name of the variable
			String variableName = (String) iter.next();
			superLogger.debug("Going to update the variable " + variableName);
			// Create the set method name
			String setMethodName = "set" + variableName;
			// Create the get method name
			String getMethodName = "get" + variableName;

			// Grab the argument objects (actually should just be one in this
			// case)
			Object[] incomingArguments = (Object[]) methodsToUpdate
					.get(variableName);

			// Grab the class type for method calls
			Class[] argumentClasses = new Class[incomingArguments.length];
			for (int i = 0; i < incomingArguments.length; i++) {
				if (incomingArguments[i] != null) {
					argumentClasses[i] = incomingArguments[i].getClass();
				} else {
					if (oldValues.get(getMethodName) != null) {
						argumentClasses[i] = oldValues.get(getMethodName)
								.getClass();
					}
				}
			}

			// First in the hash of old values, change those that are associated
			// with this variable
			superLogger
					.debug("Going to replace the old return value of method "
							+ getMethodName + "->("
							+ oldValues.get(getMethodName)
							+ ") with new value (" + incomingArguments[0] + ")");
			oldValues.remove(getMethodName);
			oldValues.put(getMethodName, incomingArguments[0]);

			Method setMethod = null;
			try {
				setMethod = metadataObjectToWorkWith.getClass().getMethod(
						setMethodName, argumentClasses);
			} catch (SecurityException e) {
				superLogger.error("SecurityException: " + e.getMessage());
			} catch (NoSuchMethodException e) {
				superLogger.error("NoSuchMethodException: " + e.getMessage());
			}
			if (setMethod != null) {
				superLogger.debug("Calling the set method "
						+ setMethod.getName() + " with argument "
						+ incomingArguments[0]);
				try {
					setMethod.invoke(metadataObjectToWorkWith,
							incomingArguments);
					superLogger.debug("OK, value should be updated");
				} catch (IllegalArgumentException e1) {
					superLogger.error("IllegalArgumentException: "
							+ e1.getMessage());
				} catch (IllegalAccessException e1) {
					superLogger.error("IllegalAccessException: "
							+ e1.getMessage());
				} catch (InvocationTargetException e1) {
					superLogger.error("InvocationTargetException: "
							+ e1.getMessage());
				}
			}

		}

		// OK, I should have the updated object, now I can update it in the
		// persistent store
		superLogger.debug("Going to persist updated object");
		Long updatedID = null;
		try {
			updatedID = access.update(metadataObjectToWorkWith);
		} catch (RemoteException e) {
			superLogger.error("RemoteException: " + e.getMessage());
		} catch (MetadataAccessException e) {
			superLogger.error("MetadataAccessException: " + e.getMessage());
		}
		assertNotNull("ID after update should not be null", updatedID);
		assertEquals("ID returned from update should be same as previous ID",
				updatedID, id);

		// Now query back for the value object to get the updated object
		IMetadataObject updatedPersistentObject = null;
		try {
			updatedPersistentObject = access.findById(id, false);
		} catch (RemoteException e1) {
			superLogger.error("RemoteException: " + e1.getMessage());
		} catch (MetadataAccessException e1) {
			superLogger.error("MetadataAccessException: " + e1.getMessage());
		}
		// Make sure something was found
		assertNotNull(updatedPersistentObject);

		// Now makes sure all the value are the same as the updated
		// values that we stored locally
		for (Iterator iter = oldValues.keySet().iterator(); iter.hasNext();) {
			String methodName = (String) iter.next();
			superLogger.debug("Going to check udpated values for method "
					+ methodName);
			// Now call method
			Object localResult = oldValues.get(methodName);
			// Find the method
			Method methodToCall = null;
			try {
				methodToCall = updatedPersistentObject.getClass().getMethod(
						methodName, new Class[0]);
			} catch (SecurityException e2) {
				superLogger.error("SecurityException: " + e2.getMessage());
			} catch (NoSuchMethodException e2) {
				superLogger.error("NoSuchMethodException: " + e2.getMessage());
			}
			assertNotNull("Method " + methodName + " should have been found",
					methodToCall);

			Object persistedResult = null;
			try {
				// Call it and get result
				persistedResult = methodToCall.invoke(updatedPersistentObject,
						new Object[0]);
			} catch (IllegalArgumentException e3) {
				superLogger.error("IllegalArgumentException: "
						+ e3.getMessage());
			} catch (IllegalAccessException e3) {
				superLogger.error("IllegalAccessException: " + e3.getMessage());
			} catch (InvocationTargetException e3) {
				superLogger.error("InvocationTargetException: "
						+ e3.getMessage());
			}
			superLogger.debug("Comparing " + localResult + " to "
					+ persistedResult);
			if (localResult instanceof Date) {
				assertEquals(
						"Before and after update values should be the same",
						((Date) localResult).getTime() / 1000,
						((Date) persistedResult).getTime() / 1000);
			} else {
				assertEquals(
						"Before and after update values should be the same",
						localResult, persistedResult);
			}
		}

	}

	/**
	 * @param resourceOneId
	 * @param oneByQuery
	 */
	protected void testDelete(IMetadataObject metadataObject,
			IMetadataAccessRemote access) {

		// Grab the ID for later
		Long id = metadataObject.getId();
		superLogger.debug("The ID of the object to be deleted is: " + id);

		boolean exceptionsCaught = false;

		// Try to delete the incoming object
		try {
			access.delete(metadataObject);
		} catch (RemoteException e2) {
			superLogger
					.error("RemoteException caught trying to delete object: "
							+ e2.getMessage());
			exceptionsCaught = true;
		} catch (MetadataAccessException e2) {
			superLogger
					.error("MetadataAccessException caught trying to delete object: "
							+ e2.getMessage());
			exceptionsCaught = true;
		}

		// Now try to query for the object
		IMetadataObject afterDelete = null;
		try {
			afterDelete = access.findById(id, false);
		} catch (RemoteException e3) {
			superLogger
					.error("RemoteException caught trying to find after delete: "
							+ e3.getMessage());
			exceptionsCaught = true;
		} catch (MetadataAccessException e3) {
			superLogger
					.error("MetadataAccessException caught trying to find after delete: "
							+ e3.getMessage());
			exceptionsCaught = true;
		}
		// check post condition
		assertNull("After delete the returned MetadataObject should be null",
				afterDelete);

		// Check to make sure no exceptions were caught
		assertTrue(!exceptionsCaught);
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
		// Delete Events
		try {
			EVENT_ACCESS.delete(EVENT_ONE);
		} catch (RemoteException e1) {
		} catch (MetadataAccessException e1) {
		}
		try {
			EVENT_ACCESS.delete(EVENT_TWO);
		} catch (RemoteException e1) {
		} catch (MetadataAccessException e1) {
		}

		try {
			KEYWORD_ACCESS.delete(KEYWORD_ONE);
		} catch (RemoteException e1) {
		} catch (MetadataAccessException e1) {
		}
		try {
			KEYWORD_ACCESS.delete(KEYWORD_TWO);
		} catch (RemoteException e1) {
		} catch (MetadataAccessException e1) {
		}

		// Delete Person objects
		try {
			PERSON_ACCESS.delete(PERSON_ONE);
		} catch (RemoteException e) {
		} catch (MetadataAccessException e) {
		}
		try {
			PERSON_ACCESS.delete(PERSON_TWO);
		} catch (RemoteException e) {
		} catch (MetadataAccessException e) {
		}

		// Delete GroupAccess objects
		try {
			USER_GROUP_ACCESS.delete(USER_GROUP_ONE);
		} catch (RemoteException e) {
		} catch (MetadataAccessException e) {
		}
		try {
			USER_GROUP_ACCESS.delete(USER_GROUP_TWO);
		} catch (RemoteException e) {
		} catch (MetadataAccessException e) {
		}
		try {
			USER_GROUP_ACCESS.delete(USER_GROUP_THREE);
		} catch (RemoteException e) {
		} catch (MetadataAccessException e) {
		}

		// Delete DataContainer objects (along with their RDs and RVs)
		try {
			DATA_CONTAINER_ACCESS.delete(DATA_CONTAINER_ONE);
		} catch (RemoteException e) {
		} catch (MetadataAccessException e) {
		}
		try {
			DATA_CONTAINER_ACCESS.delete(DATA_CONTAINER_TWO);
		} catch (RemoteException e) {
		} catch (MetadataAccessException e) {
		}

		// Delete DataProducer objects
		try {
			DATA_PRODUCER_ACCESS.delete(DATA_PRODUCER_ONE);
		} catch (RemoteException e) {
		} catch (MetadataAccessException e) {
		}
		try {
			DATA_PRODUCER_ACCESS.delete(DATA_PRODUCER_TWO);
		} catch (RemoteException e) {
		} catch (MetadataAccessException e) {
		}

		// Delete StandardVariable objects
		try {
			STANDARD_VARIABLE_ACCESS.delete(STANDARD_VARIABLE_ONE);
		} catch (RemoteException e) {
		} catch (MetadataAccessException e) {
		}
		try {
			STANDARD_VARIABLE_ACCESS.delete(STANDARD_VARIABLE_TWO);
		} catch (RemoteException e) {
		} catch (MetadataAccessException e) {
		}
	}

	/**
	 * Here are some static variables that define different objects to use in
	 * testing
	 */

	// The delimiter
	static String DELIMITER = "|";

	// Test Events
	static Event EVENT_ONE = null;
	static String EVENT_ONE_STRING_REP = "Event|" + "name=Test Event One|"
			+ "description=Test Event One Description|"
			+ "startDate=2000-01-02T03:04:05Z|"
			+ "endDate=2006-07-08T09:10:11Z";
	static Event EVENT_TWO = null;
	static String EVENT_TWO_STRING_REP = "Event|" + "name=Test Event Two|"
			+ "description=Test Event Two Description|"
			+ "startDate=2001-02-03T04:05:06Z|"
			+ "endDate=2007-08-09T10:11:12Z";

	static Keyword KEYWORD_ONE = null;
	static String KEYWORD_ONE_STRING_REP = "Keyword|name=Keyword One|description=Keyword One Description";
	static Keyword KEYWORD_TWO = null;
	static String KEYWORD_TWO_STRING_REP = "Keyword|name=Keyword Two|description=Keyword Two Description";

	// The test Persons
	static Person PERSON_ONE = null;
	static String PERSON_ONE_STRING_REP = "Person|" + "firstname=John|"
			+ "surname=Doe|" + "organization=MBARI|" + "email=jdoe@mbari.org|"
			+ "username=jdoe|" + "password=dumbPassword|" + "status=active";
	static Person PERSON_TWO = null;
	static String PERSON_TWO_STRING_REP = "Person|" + "firstname=Jane|"
			+ "surname=Doe|" + "organization=MBARI|"
			+ "email=janedoe@mbari.org|" + "username=janedoe|"
			+ "password=dumbPassword|" + "status=active";

	// The test UserGroups
	static UserGroup USER_GROUP_ONE = null;
	static String USER_GROUP_ONE_STRING_REP = "UserGroup|groupName=JUnit UserGroupOne";
	static UserGroup USER_GROUP_TWO = null;
	static String USER_GROUP_TWO_STRING_REP = "UserGroup|groupName=JUnit UserGroupTwo";
	static UserGroup USER_GROUP_THREE = null;
	static String USER_GROUP_THREE_STRING_REP = "UserGroup|groupName=JUnit UserGroupThree";

	// For DataContainer & RecordVariable tests
	static DataContainer DATA_CONTAINER_ONE = null;
	static String DATA_CONTAINER_ONE_STRING_REP = "DataContainer|"
			+ "name=DataContainerOne|"
			+ "description=DataContainerOne Description|"
			+ "dataContainerType=File|" + "startDate=2003-05-05T16:11:44Z|"
			+ "endDate=2004-02-01T08:38:19Z|" + "original=true|"
			+ "uriString=http://kasatka.shore.mbari.org/DataContainerOne.txt|"
			+ "contentLength=50000|" + "mimeType=CSV|" + "numberOfRecords=800|"
			+ "dodsAccessible=false|" + "minLatitude=36.6|"
			+ "maxLatitude=36.805|" + "minLongitude=-121.56|"
			+ "maxLongitude=-121.034|" + "minDepth=0.0|" + "maxDepth=10.546";
	static DataContainer DATA_CONTAINER_TWO = null;
	static String DATA_CONTAINER_TWO_STRING_REP = "DataContainer|"
			+ "name=DataContainerTwo|"
			+ "description=DataContainerTwo Description|"
			+ "dataContainerType=Stream|" + "startDate=2005-01-01T00:00:00Z|"
			+ "endDate=2005-02-01T08:38:19Z|" + "original=true|"
			+ "uri=http://kasatka.shore.mbari.org/DataContainerTwo.xls|"
			+ "contentLength=1295|" + "mimeType=app/excel|"
			+ "numberOfRecords=10|" + "dodsAccessible=false|"
			+ "minLatitude=6.0|" + "maxLatitude=45.0|"
			+ "minLongitude=-255.34|" + "maxLongitude=-245.567|"
			+ "minDepth=10.5|" + "maxDepth=10000.5";

	static RecordDescription RECORD_DESCRIPTION_ONE = null;
	static String RECORD_DESCRIPTION_ONE_STRING_REP = "RecordDescription|"
			+ "recordType=1|" + "bufferStyle="
			+ RecordDescription.BUFFER_STYLE_ASCII + "|" + "bufferParseType="
			+ RecordDescription.PARSE_TYPE_FIXED_POSITION + "|"
			+ "bufferItemSeparator=,|" + "bufferLengthType="
			+ RecordDescription.BUFFER_LENGTH_TYPE_FIXED + "|"
			+ "recordTerminator=\\n|" + "parseable=true|" + "endian="
			+ RecordDescription.ENDIAN_BIG;
	static RecordDescription RECORD_DESCRIPTION_TWO = null;
	static String RECORD_DESCRIPTION_TWO_STRING_REP = "RecordDescription|"
			+ "recordType=1|" + "bufferStyle="
			+ RecordDescription.BUFFER_STYLE_BINARY + "|" + "bufferParseType="
			+ RecordDescription.PARSE_TYPE_ORDERED_POSITION + "|"
			+ "bufferItemSeparator=\\t|" + "bufferLengthType="
			+ RecordDescription.BUFFER_LENGTH_TYPE_VARIABLE + "|"
			+ "recordTerminator=\\r\\n|" + "parseable=false|" + "endian="
			+ RecordDescription.ENDIAN_LITTLE;

	static RecordVariable RECORD_VARIABLE_ONE = null;
	static String RECORD_VARIABLE_ONE_STRING_REP = "RecordVariable|"
			+ "name=RecordVarableOne|"
			+ "description=RecordVariableOne Description|"
			+ "longName=RecordVariableOne Long Name|"
			+ "format=RecordVariableOne Format|"
			+ "units=RecordVariableOne Units|" + "columnIndex=1|"
			+ "validMin=-99.99|" + "validMax=99.99|"
			+ "missingValue=999999999.99999|" + "accuracy=-.00001|"
			+ "displayMin=-100|" + "displayMax=100|"
			+ "referenceScale=RecordVariableOne ReferenceScale|"
			+ "conversionScale=10.00|" + "conversionOffset=-1|"
			+ "convertedUnits=RecordVariableOne Converted Units|"
			+ "sourceSensorID=1000|" + "parseRegExp=\\D+(\\d+)\\w+";

	static RecordVariable RECORD_VARIABLE_TWO = null;
	static String RECORD_VARIABLE_TWO_STRING_REP = "RecordVariable|"
			+ "name=RecordVarableTwo|"
			+ "description=RecordVariableTwo Description|"
			+ "longName=RecordVariableTwo Long Name|"
			+ "format=RecordVariableTwo Format|"
			+ "units=RecordVariableTwo Units|" + "columnIndex=2|"
			+ "validMin=-9.9|" + "validMax=9.9|"
			+ "missingValue=999999999.99999|" + "accuracy=-.1|"
			+ "displayMin=-10|" + "displayMax=10|"
			+ "referenceScale=RecordVariableTwo ReferenceScale|"
			+ "conversionScale=1.00|" + "conversionOffset=-5|"
			+ "convertedUnits=RecordVariableTwo Converted Units|"
			+ "sourceSensorID=1002|" + "parseRegExp=2-\\D+(\\d+)\\w+";
	static RecordVariable RECORD_VARIABLE_THREE = null;
	static String RECORD_VARIABLE_THREE_STRING_REP = "RecordVariable|"
			+ "name=RecordVarableThree|"
			+ "description=RecordVariableThree Description|"
			+ "longName=RecordVariableThree Long Name|"
			+ "format=RecordVariableThree Format|"
			+ "units=RecordVariableThree Units|" + "columnIndex=3|"
			+ "validMin=-3.99|" + "validMax=3.99|" + "missingValue=3.99999|"
			+ "accuracy=-3.3|" + "displayMin=-30|" + "displayMax=30|"
			+ "referenceScale=RecordVariableThree ReferenceScale|"
			+ "conversionScale=3.00|" + "conversionOffset=-3|"
			+ "convertedUnits=RecordVariableThree Converted Units|"
			+ "sourceSensorID=1003|" + "parseRegExp=3-\\D+(\\d+)\\w+";
	static RecordVariable RECORD_VARIABLE_FOUR = null;
	static String RECORD_VARIABLE_FOUR_STRING_REP = "RecordVariable|"
			+ "name=RecordVarableFour|"
			+ "description=RecordVariableFour Description|"
			+ "longName=RecordVariableFour Long Name|"
			+ "format=RecordVariableFour Format|"
			+ "units=RecordVariableFour Units|" + "columnIndex=4|"
			+ "validMin=-4.99|" + "validMax=4.99|" + "missingValue=49.99999|"
			+ "accuracy=-4.1|" + "displayMin=-40|" + "displayMax=40|"
			+ "referenceScale=RecordVariableFour ReferenceScale|"
			+ "conversionScale=40.00|" + "conversionOffset=-4|"
			+ "convertedUnits=RecordVariableFour Converted Units|"
			+ "sourceSensorID=1004|" + "parseRegExp=4-\\D+(\\d+)\\w+";

	static RecordVariable RECORD_VARIABLE_FIVE = null;
	static String RECORD_VARIABLE_FIVE_STRING_REP = "RecordVariable|"
			+ "name=RecordVarableFive|"
			+ "description=RecordVariableFive Description|"
			+ "longName=RecordVariableFive Long Name|"
			+ "format=RecordVariableFive Format|"
			+ "units=RecordVariableFive Units|" + "columnIndex=5|"
			+ "validMin=-59.99|" + "validMax=59.99|"
			+ "missingValue=599.99999|" + "accuracy=-5.1|" + "displayMin=-50|"
			+ "displayMax=50|"
			+ "referenceScale=RecordVariableFive ReferenceScale|"
			+ "conversionScale=50.00|" + "conversionOffset=-5|"
			+ "convertedUnits=RecordVariableFive Converted Units|"
			+ "sourceSensorID=1005|" + "parseRegExp=5-\\D+(\\d+)\\w+";
	static RecordVariable RECORD_VARIABLE_SIX = null;
	static String RECORD_VARIABLE_SIX_STRING_REP = "RecordVariable|"
			+ "name=RecordVarableSix|"
			+ "description=RecordVariableSix Description|"
			+ "longName=RecordVariableSix Long Name|"
			+ "format=RecordVariableSix Format|"
			+ "units=RecordVariableSix Units|" + "columnIndex=6|"
			+ "validMin=-6.99|" + "validMax=6.99|" + "missingValue=69.99999|"
			+ "accuracy=-6.6|" + "displayMin=-600|" + "displayMax=600|"
			+ "referenceScale=RecordVariableSix ReferenceScale|"
			+ "conversionScale=60.00|" + "conversionOffset=-6|"
			+ "convertedUnits=RecordVariableSix Converted Units|"
			+ "sourceSensorID=1006|" + "parseRegExp=6-\\D+(\\d+)\\w+";

	// For DataProduce tests
	static DataProducer DATA_PRODUCER_ONE = null;
	static String DATA_PRODUCER_ONE_STRING_REP = "DataProducer|"
			+ "name=DataProducerOne|"
			+ "description=DataProducerOne Description|"
			+ "startDate=2003-05-05T16:11:44Z|"
			+ "endDate=2004-02-01T08:38:19Z|";
	static DataProducer DATA_PRODUCER_TWO = null;
	static String DATA_PRODUCER_TWO_STRING_REP = "DataProducer|"
			+ "name=DataProducerTwo|"
			+ "description=DataProducerTwo Description|"
			+ "startDate=2003-05-05T16:11:44Z|"
			+ "endDate=2004-02-01T08:38:19Z|";

	// StandardVariable used in RecordVariable Tests
	static StandardVariable STANDARD_VARIABLE_ONE = null;
	static String STANDARD_VARIABLE_ONE_STRING_REP = "StandardVariable|"
			+ "name=StandardVariableOne|"
			+ "description=StandardVariable one description|"
			+ "referenceScale=StandardVariable one reference scale";
	static StandardVariable STANDARD_VARIABLE_TWO = null;
	static String STANDARD_VARIABLE_TWO_STRING_REP = "StandardVariable|"
			+ "name=StandardVariableTwo|"
			+ "description=StandardVariable two description|"
			+ "referenceScale=StandardVariable two reference scale";

	// The access services
	static EventAccess EVENT_ACCESS = null;

	static KeywordAccess KEYWORD_ACCESS = null;

	static PersonAccess PERSON_ACCESS = null;

	static UserGroupAccess USER_GROUP_ACCESS = null;

	static DataProducerAccess DATA_PRODUCER_ACCESS = null;

	static IDataContainerAccess DATA_CONTAINER_ACCESS = null;

	static RecordVariableAccess RECORD_VARIABLE_ACCESS = null;

	static StandardVariableAccess STANDARD_VARIABLE_ACCESS = null;

}