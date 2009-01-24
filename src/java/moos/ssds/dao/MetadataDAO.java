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
package moos.ssds.dao;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.TreeMap;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.util.XmlDateFormat;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import com.sun.org.apache.bcel.internal.generic.NEWARRAY;

/**
 * This is the super class that implements the functions that are common to all
 * SSDS Data Access Objects (DAO)s.
 * <hr>
 * 
 * @stereotype service
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.22 $
 */
public abstract class MetadataDAO implements IMetadataDAO {

	/**
	 * This is the constructor that takes in the <code>Class</code> that the
	 * DAO will be dealing with and the <code>Session</code> that it will use
	 * to perform the persistence operations
	 * 
	 * @param persistentClass
	 *            is the <code>Class</code> that the DAO will be working with
	 * @param session
	 *            is the <code>Session</code> that it will use to perform
	 *            persistence operations
	 */
	public MetadataDAO(Class persistentClass, Session session)
			throws MetadataAccessException {

		// Check construction dependencies
		if (persistentClass == null) {
			throw new MetadataAccessException(
					"The persistentClass was not specified, please fix");
		}
		if (session == null) {
			throw new MetadataAccessException(
					"The session was not specified, please fix");
		}

		// Set the local variables
		superLogger.debug("MetadataDAO created with persistentClass "
				+ persistentClass.getName());
		this.persistentClass = persistentClass;
		this.session = session;

		// If the ThreadLocal is not instantiated yet, create a new TreeMap
		if (messageTreeMap == null) {

			// Load the properties
			this.loadProperties();

			superLogger
					.debug("The ThreadLocal to hold the message TreeMap "
							+ "was not instantiated yet, will do so now (Thread name is "
							+ Thread.currentThread().getName() + ")");
			messageTreeMap = new ThreadLocal();
			messageTreeMap.set(new TreeMap());
		} else {
			// Check to see if the ThreadLocal has a TreeMap
			TreeMap treeMapToCheck = (TreeMap) messageTreeMap.get();
			// If not, create a new one
			if (treeMapToCheck == null) {
				superLogger.debug("The ThreadLocal was not null, but did "
						+ " not have a TreeMap associated to it "
						+ "so I created a new one");
				messageTreeMap.set(new TreeMap());
			} else {
				superLogger
						.debug("The ThreadLocal to hold the message TreeMap "
								+ "was already instantiated and has a TreeMap, "
								+ "so no need to "
								+ "create a new one (Thread name is "
								+ Thread.currentThread().getName() + ")");
			}
		}
	}

	/**
	 * This method simply returns the <code>Class</code> that the DAO is
	 * operating on behalf of
	 * 
	 * @return the <code>Class</code> that the DAO is operating in behalf of
	 */
	public Class getPersistentClass() {
		return this.persistentClass;
	}

	/**
	 * This method sets the <code>Class</code> that the DAO will be acting on
	 * behalf of
	 * 
	 * @param persistentClass
	 *            is the <code>Class</code> that the DAO will be acting on
	 *            behalf of
	 */
	public void setPersistentClass(Class persistentClass) {
		this.persistentClass = persistentClass;
	}

	/**
	 * @see IMetadataDAO#getSession()
	 */
	public Session getSession() {
		return this.session;
	}

	/**
	 * @see IMetadataDAO#setSession(Session)
	 */
	public void setSession(Session session) {
		this.session = session;
	}

	/**
	 * The method to get the SSDS Administrator email address where any messages
	 * in the DAO should be sent from.
	 * 
	 * @return
	 */
	public String getSsdsAdminEmailToAddress() {
		return ssdsAdminEmailToAddress;
	}

	/**
	 * @see IMetadataDAO#findById(Long, boolean)
	 */
	public IMetadataObject findById(Long id, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// Check to see if id is null or negative
		superLogger.debug("findById with ID " + id + " called");
		if (id == null)
			return null;
		if (id.longValue() <= 0)
			return null;

		// Create the MetadataObject to return
		superLogger.debug("ID looks OK, will look up instance by ID");
		IMetadataObject metadataObject = null;

		// Grab the MetadataObject by its ID
		try {
			metadataObject = (IMetadataObject) session.get(
					getPersistentClass(), id);
		} catch (HibernateException e) {
			superLogger
					.error("HibernateException caught on get call (will be re-thrown):"
							+ e.getMessage());
			throw new MetadataAccessException("HibernateException ("
					+ e.getClass().getName()
					+ ") caught in MetadataDAO.findById (Message =  "
					+ e.getMessage() + ")");
		}

		// Return the result
		superLogger.debug("OK, will return the metadataObject found by ID");
		if (returnFullObjectGraph)
			initializeRelationships(metadataObject);

		return metadataObject;
	}

	/**
	 * @see IMetadataDAO#findId(IMetadataObject)
	 */
	public Long findId(IMetadataObject metadataObject)
			throws MetadataAccessException {

		Long idToReturn = null;

		// Get the persistent object
		IMetadataObject equivalentMetadataObject = this
				.findEquivalentPersistentObject(metadataObject, false);

		if (equivalentMetadataObject != null) {
			idToReturn = equivalentMetadataObject.getId();
		}
		return idToReturn;
	}

	/**
	 * @see IMetadataDAO#findEquivalentPersistentObject(IMetadataObject,
	 *      boolean)
	 */
	public abstract IMetadataObject findEquivalentPersistentObject(
			IMetadataObject metadataObject, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see IMetadataDAO#findAll(String, boolean)
	 */
	public Collection findAll(String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// The collection to return
		superLogger.debug("findAll called");
		Collection results = new ArrayList();

		// The StringBuffer
		StringBuffer sqlStringBuffer = new StringBuffer();
		sqlStringBuffer.append("from " + getPersistentClass().getName() + " ");
		String shortClassName = persistentClass.getSimpleName();
		String firstClassLetter = shortClassName.substring(0, 1);
		shortClassName = firstClassLetter.toLowerCase()
				+ shortClassName.substring(1);
		sqlStringBuffer.append(shortClassName + " ");
		sqlStringBuffer.append(this.getOrderByPropertyNameSQLClause(
				orderByPropertyName, ascendingOrDescending));

		// Query for all instances
		try {
			Query query = session.createQuery(sqlStringBuffer.toString());
			results = query.list();
		} catch (HibernateException e) {
			superLogger.error("HibernateException caught while trying to "
					+ "findAll (will be re-thrown):" + e.getMessage());
			throw new MetadataAccessException(e.getMessage());
		}

		// Return full object graph is requested
		if (returnFullObjectGraph)
			initializeRelationships(results);

		// Return what was found
		return results;
	}

	/**
	 * @see IMetadataDAO#findBySQL(String, String, Class, boolean)
	 */
	public Collection findBySQL(String sqlString, String aliasName,
			Class classOfReturn, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// If the query contains terms that are not allowed, throw an exception
		superLogger.debug("findBySQL called with SQL string: " + sqlString);
		if (!sqlString.startsWith("SELECT")
				&& (!sqlString.startsWith("select"))) {
			throw new MetadataAccessException(
					"The action performed in the SQL String specified is not allowed");
		}
		// The collection to return
		Collection results = new ArrayList();

		// Now the query
		try {
			Query query = session.createSQLQuery(sqlString).addEntity(
					aliasName, classOfReturn);
			results = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e.getMessage());
		}
		// Check if full graphs are to be returned
		if (returnFullObjectGraph)
			initializeRelationships(results);

		// Return the results
		return results;
	}

	/**
	 * @see IMetadataDAO#getMetadataObjectWithGraph(IMetadataObject)
	 */
	public IMetadataObject getMetadataObjectGraph(IMetadataObject metadataObject)
			throws MetadataAccessException {
		return this.findEquivalentPersistentObject(metadataObject, true);
	}

	/**
	 * @see IMetadataDAO#getDeepCopy(IMetadataObject metadataObject)
	 */
	public IMetadataObject getDeepCopy(IMetadataObject metadataObject)
			throws MetadataAccessException {
		// Find the equivalent persistent object
		IMetadataObject persistentMetadataObject = this
				.findEquivalentPersistentObject(metadataObject, false);
		if (persistentMetadataObject == null)
			return null;
		// Now return the deep copy
		IMetadataObject deepCopy = null;
		try {
			deepCopy = persistentMetadataObject.deepCopy();
		} catch (CloneNotSupportedException e) {
		}
		return deepCopy;
	}

	/**
	 * @see IMetadataDAO#makePersistent(IMetadataObject)
	 */
	public abstract Long makePersistent(IMetadataObject metadataObject)
			throws MetadataAccessException;

	/**
	 * @see IMetadataDAO#makeTransient(IMetadataObject)
	 */
	public abstract void makeTransient(IMetadataObject metadataObject)
			throws MetadataAccessException;

	/**
	 * These methods get and set the the ThreadLocal that contains the TreeMap
	 * of email addresses to messages in case the handling application wants to
	 * notify users of important DAO related messages
	 * 
	 * @return
	 */
	public static ThreadLocal getMessageTreeMap() {
		return messageTreeMap;
	}

	public static void setMessageTreeMap(ThreadLocal threadLocal) {
		messageTreeMap = threadLocal;
	}

	/**
	 * This method is implemented by the individual DAOs, but basically takes in
	 * the <code>IMetadataDAO</code> object and then initializes the
	 * relationships that are need to instantiate a full graph of the object and
	 * its relationships.
	 * 
	 * @param metadataObject
	 *            the <code>IMetadataObject</code> that will have its
	 *            relationships initialized
	 */
	protected abstract void initializeRelationships(
			IMetadataObject metadataObject) throws MetadataAccessException;

	/**
	 * This initializes the relationships on a collection of
	 * <code>IMetadataObject</code>s.
	 * 
	 * @see MetadataDAO#initializeRelationships(IMetadataObject)
	 * @param metadataObjects
	 */
	protected void initializeRelationships(Collection metadataObjects)
			throws MetadataAccessException {
		if (metadataObjects == null)
			return;
		Iterator metadataObjectIter = metadataObjects.iterator();
		while (metadataObjectIter.hasNext()) {
			IMetadataObject metadataObject = null;
			try {
				metadataObject = (IMetadataObject) metadataObjectIter.next();
			} catch (Throwable e) {
				superLogger.error("Throwable caught trying to cast object to "
						+ "IMetadataObject in initializeRelationships: "
						+ e.getMessage());
				continue;
			}
			this.initializeRelationships(metadataObject);
		}
	}

	/**
	 * This method adds an ordering clause to the criteria supplied. If checks
	 * to see if the property is valid, then adds the order clause to the
	 * Criteria
	 * 
	 * @param criteria
	 *            The <code>Criteria</code> to add the clause to.
	 * @param orderByProperty
	 *            is the <code>String</code> that is the name of the property
	 *            to order by.
	 * @param ascendOrDescend
	 *            is the <code>String</code> that needs to match the ascending
	 *            or descending constants defined in this class.
	 */
	protected void addOrderByCriteria(Criteria criteria,
			String orderByProperty, String ascendOrDescend) {
		// Setup ordering if real property
		if (checkIfPropertyOK(orderByProperty)) {
			if ((ascendOrDescend == null)
					|| ((!ascendOrDescend.equals(MetadataDAO.ASCENDING_ORDER)) && (!ascendOrDescend
							.equals(MetadataDAO.DESCENDING_ORDER)))) {
				ascendOrDescend = MetadataDAO.ASCENDING_ORDER;
			}
			if (ascendOrDescend.equals(MetadataDAO.ASCENDING_ORDER)) {
				criteria.addOrder(Order.asc(orderByProperty));
			} else if (ascendOrDescend.equals(MetadataDAO.DESCENDING_ORDER)) {
				criteria.addOrder(Order.desc(orderByProperty));
			}
		}
	}

	/**
	 * This method checks to see if the property name fed in will match a
	 * property on the persistent class for this DAO
	 * 
	 * @param propertyName
	 *            the name of the property to check
	 * @return a boolean that is <code>true</code> if the property name exists
	 *         on the persistent class for this DAO
	 */
	protected boolean checkIfPropertyOK(String propertyName) {
		// Set the boolean to return
		boolean propertyOK = false;

		// Make sure the property is valid before continuing
		if ((propertyName == null) || (propertyName.equals("")))
			return false;

		// Just to make sure, drop the first letter to a lower case
		String firstLetter = propertyName.substring(0, 1);
		propertyName = firstLetter.toLowerCase() + propertyName.substring(1);
		Field[] classFields = persistentClass.getDeclaredFields();
		for (int i = 0; i < classFields.length; i++) {
			if (classFields[i].getName().equals(propertyName)) {
				propertyOK = true;
			}
		}
		return propertyOK;
	}

	/**
	 * This method returns the order by HQL clause that can be appended to HQL
	 * queries for the persistent class for this DAO
	 * 
	 * @param propertyName
	 *            is the name of the property to order by
	 * @param ascendingOrDescending
	 *            is the <code>String</code> that should match one of the
	 *            constants defined in this DAO and will define the ascending or
	 *            descending nature of the order of the return
	 * @return
	 */
	protected String getOrderByPropertyNameSQLClause(String propertyName,
			String ascendingOrDescending) {
		if (propertyName == null)
			return "";
		if (!checkIfPropertyOK(propertyName))
			return "";
		String ascendOrDescend = ascendingOrDescending;
		// Change the property name to lower case at the start
		String firstLetter = propertyName.substring(0, 1);
		propertyName = firstLetter.toLowerCase() + propertyName.substring(1);

		// Change the persistent class name to lower case at the start
		String shortClassName = persistentClass.getSimpleName();
		String firstClassLetter = shortClassName.substring(0, 1);
		shortClassName = firstClassLetter.toLowerCase()
				+ shortClassName.substring(1);

		String orderByClause = " order by " + shortClassName + "."
				+ propertyName + " ";
		if ((ascendOrDescend == null)
				|| ((!ascendOrDescend.equals(MetadataDAO.ASCENDING_ORDER)) && (!ascendOrDescend
						.equals(MetadataDAO.DESCENDING_ORDER)))) {
			ascendOrDescend = MetadataDAO.ASCENDING_ORDER;
		}
		if (ascendOrDescend.equals(MetadataDAO.ASCENDING_ORDER)) {
			orderByClause = orderByClause + MetadataDAO.ASCENDING_ORDER;
		} else if (ascendOrDescend.equals(MetadataDAO.DESCENDING_ORDER)) {
			orderByClause = orderByClause + MetadataDAO.DESCENDING_ORDER;
		}
		return orderByClause;
	}

	/**
	 * This methods take in two <code>IMetadataObjects</code> and checks all
	 * non-null fields of the source object, to see if they are different from
	 * the destination object. If the are different, the destination object will
	 * be updated with the changed fields of the source object. If a change took
	 * place, true is returned, meaning the destination object was updated.
	 * 
	 * @param sourceMetadataObject
	 * @param destinationMetadataObject
	 * @return a <code>boolean</code> to indicate if the destination object
	 *         was changed (<code>true</code>) or not (<code>false</code>)
	 */
	protected boolean updateDestinationObject(
			IMetadataObject sourceMetadataObject,
			IMetadataObject destinationMetadataObject)
			throws MetadataAccessException {

		// The boolean to indicate if the destination object was updated
		superLogger.debug("updateDestinationObject called.  "
				+ "Will start will false return");
		boolean destinationUpdated = false;

		// Check to make sure both objects exist
		if ((sourceMetadataObject == null)
				|| (destinationMetadataObject == null))
			throw new MetadataAccessException(
					"One (or both) of the incoming objects was null, "
							+ "that cannot be.");

		// Now grab the source and destination classes
		Class destinationClass = destinationMetadataObject.getClass();
		Class sourceClass = sourceMetadataObject.getClass();

		// In order to compare classes, I have to check for Hibernate proxy and
		// if it is one, I have to ask for the underlying class
		if ((destinationClass.getName().indexOf("CGLIB") >= 0)
				|| (destinationClass.getName().indexOf("javassist") >= 0))
			destinationClass = Hibernate.getClass(destinationMetadataObject);
		if ((sourceClass.getName().indexOf("CGLIB") >= 0)
				|| (sourceClass.getName().indexOf("javassist") >= 0))
			sourceClass = Hibernate.getClass(sourceMetadataObject);
		superLogger.debug("SourceClass is " + sourceClass.getName());
		superLogger.debug("DesintationClass is " + destinationClass.getName());

		// Make sure they are of the same class
		if (destinationClass.isInstance(sourceMetadataObject)) {

			// Grab all the declared fields from the source
			Field[] declaredFields = sourceClass.getDeclaredFields();

			// Now we need to loop over the declared fields to find the non-null
			// fields
			if (declaredFields != null) {
				for (int i = 0; i < declaredFields.length; i++) {
					// Grab a field, it's name and class
					Field currentField = declaredFields[i];
					String fieldName = currentField.getName();
					Class fieldClass = currentField.getType();
					// superLogger.debug("fieldName => " + fieldName
					// + ", fieldClass => " + fieldClass.getName());

					// Skip the collection classes as they are most likely
					// relationships with other IMetadataObjects. Also skip any
					// fields that are IMetadataObject
					if ((fieldClass.getName().equals("java.util.Collection"))
							|| (fieldClass.getName()
									.startsWith("moos.ssds.metadata"))) {
						// superLogger.debug("This was a collection or "
						// + "IMetadataObject, so will skip");
						continue;
					}

					// First we can check for some exceptions to the rule here.
					// These exceptions are:
					// 1. If the class is a DataProducer and the field is
					// 'dataProducerType', the field will not be updated. This
					// is due to the fact that the DataProducer class
					// automatically creates a type of 'Deployment'. If a client
					// creates a new 'DataProducer' and sticks an ID of an
					// existing DP that is a 'ProcessRun' and does not change
					// the type to 'ProcessRun' before doing the update, the
					// type will be changed. 99.9% of the time this is not the
					// desired behavior, so we will just disallow. This would be
					// one of those cases where the field would have to be
					// updated in the DB manually (for the 0.01% case).
					// 2. If the class is a DataContainer and the field is
					// 'dataContainerType', the field will not be updated. This
					// is due to the fact that the DataContainer class
					// automatically creates a type of 'File'. If a client
					// creates a new 'DataContainer' and sticks an ID of an
					// existing DP that is a 'Stream' and does not change
					// the type to 'Stream' before doing the update, the
					// type will be changed. 99.9% of the time this is not the
					// desired behavior, so we will just disallow. This would be
					// one of those cases where the field would have to be
					// updated in the DB manually (for the 0.01% case).
					if (!(((sourceMetadataObject instanceof DataProducer) && (fieldName
							.equals("dataProducerType"))) || ((sourceMetadataObject instanceof DataContainer) && (fieldName
							.equals("dataContainerType"))))) {

						// Try to find a getterMethod for the field
						char firstLetter = fieldName.charAt(0);
						firstLetter = Character.toUpperCase(firstLetter);
						String getterMethodName = "get" + firstLetter
								+ fieldName.substring(1);
						String isMethodName = "is" + firstLetter
								+ fieldName.substring(1);
						String setterMethodName = "set" + firstLetter
								+ fieldName.substring(1);
						// superLogger.debug("getterMethodName => " +
						// getterMethodName
						// + ", isMethodName => " + isMethodName
						// + ", setterMethodName => " + setterMethodName);

						// All getters should have empty parameter lists
						Method getterMethod = null;
						Method isMethod = null;
						try {
							getterMethod = destinationClass.getMethod(
									getterMethodName, new Class[0]);
						} catch (SecurityException e) {
							superLogger
									.error("SecurityException caught trying to find method by name of "
											+ getterMethodName
											+ ": "
											+ e.getMessage());
						} catch (NoSuchMethodException e) {
							// superLogger
							// .debug("NoSuchMethodException caught looking for
							// method by name of "
							// + getterMethodName + ": " + e.getMessage());
						}
						try {
							isMethod = destinationClass.getMethod(isMethodName,
									new Class[0]);
						} catch (SecurityException e) {
							superLogger
									.error("SecurityException caught trying to find method by name of "
											+ isMethodName
											+ ": "
											+ e.getMessage());
						} catch (NoSuchMethodException e) {
							// superLogger
							// .debug("NoSuchMethodException caught looking for
							// method by name of "
							// + isMethodName + ": " + e.getMessage());
						}
						// If method was not found, go to next field
						if ((getterMethod == null) && (isMethod == null)) {
							// superLogger
							// .debug("Neither getter not is method found, will
							// skip
							// field "
							// + fieldName);
							continue;
						}

						// Call the method on the source and destination objects
						// to
						// get their field values
						Object sourceResult = null;
						Object destinationResult = null;
						if (getterMethod != null) {
							try {
								sourceResult = getterMethod.invoke(
										sourceMetadataObject, new Object[0]);
								destinationResult = getterMethod.invoke(
										destinationMetadataObject,
										new Object[0]);
							} catch (IllegalArgumentException e) {
								superLogger
										.error("IllegalArgumentException caught trying to invoke "
												+ getterMethodName
												+ ": "
												+ e.getMessage());
							} catch (IllegalAccessException e) {
								superLogger
										.error("IllegalArgumentException caught trying to invoke "
												+ getterMethodName
												+ ": "
												+ e.getMessage());
							} catch (InvocationTargetException e) {
								superLogger
										.error("IllegalArgumentException caught trying to invoke "
												+ getterMethodName
												+ ": "
												+ e.getMessage());
							}
						} else if (isMethod != null) {
							try {
								sourceResult = isMethod.invoke(
										sourceMetadataObject, new Object[0]);
								destinationResult = isMethod.invoke(
										destinationMetadataObject,
										new Object[0]);
							} catch (IllegalArgumentException e) {
								superLogger
										.error("IllegalArgumentException caught trying to invoke "
												+ isMethodName
												+ ": "
												+ e.getMessage());
							} catch (IllegalAccessException e) {
								superLogger
										.error("IllegalArgumentException caught trying to invoke "
												+ isMethodName
												+ ": "
												+ e.getMessage());
							} catch (InvocationTargetException e) {
								superLogger
										.error("IllegalArgumentException caught trying to invoke "
												+ isMethodName
												+ ": "
												+ e.getMessage());
							}
						}
						// If the source result is not null, we need to check
						// the
						// destination object to see if they are different
						if (sourceResult != null) {
							// First create a boolean to track whether the
							// setterMethod needs to be called on the
							// destination
							// object
							// superLogger.debug("Source objects field was not
							// null,
							// "
							// + "will now check on destination.");
							boolean updateNeeded = false;

							// The first and easiest check is to see if the
							// destination result is null, it will need setting
							if (destinationResult == null) {
								// superLogger.debug("destinationResult was
								// null, "
								// + "so update will be performed");
								updateNeeded = true;
							} else {
								// First check to see if it is the version of
								// object
								if ((getterMethodName != null)
										&& (getterMethodName
												.equals("getVersion"))) {
									// superLogger
									// .debug("This method is the Hibernate
									// version,
									// "
									// + "I only update if the source version "
									// + "is a positive number");
									// Convert to a Long
									Long sourceResultLong = (Long) sourceResult;
									Long destinationResultLong = (Long) destinationResult;
									if ((sourceResultLong.longValue() >= 0)
											&& ((sourceResultLong.longValue() != destinationResultLong
													.longValue()))) {
										updateNeeded = true;
									}
								} else {
									// This means it is not the version, so
									// compare
									if (!sourceResult.equals(destinationResult)) {
										updateNeeded = true;
									}
								}
							}

							// If it was deemed that the destination object
							// needed
							// an update, do it
							if (updateNeeded) {
								// Mark the object as updated (for the return)
								destinationUpdated = true;

								// Grab the setter method
								Class[] parameterClass = new Class[1];
								parameterClass[0] = fieldClass;
								Method setterMethod = null;
								try {
									setterMethod = destinationClass.getMethod(
											setterMethodName, parameterClass);
								} catch (SecurityException e) {
									superLogger
											.error("SecurityException caught trying to get "
													+ "setter method from desination object: "
													+ e.getMessage());
								} catch (NoSuchMethodException e) {
									superLogger
											.error("NoSuchMethodException caught trying to get "
													+ "setter method from desination object: "
													+ e.getMessage());
								}
								// Now if the setter method was found, call it
								// with
								// the result from the source getterMethod
								if (setterMethod != null) {
									Object[] parameterObjects = new Object[1];
									parameterObjects[0] = sourceResult;
									try {
										setterMethod.invoke(
												destinationMetadataObject,
												parameterObjects);
									} catch (IllegalArgumentException e) {
										superLogger
												.error("IllegalArgumentException caught trying "
														+ "to call setter on destination object: "
														+ e.getMessage());
									} catch (IllegalAccessException e) {
										superLogger
												.error("IllegalAccessException caught trying "
														+ "to call setter on destination object: "
														+ e.getMessage());
									} catch (InvocationTargetException e) {
										superLogger
												.error("InvocationTargetException caught trying "
														+ "to call setter on destination object: "
														+ e.getMessage());
									}
								}
							}
						}
					}
				}
			}
		} else {
			superLogger
					.error("The two objects were not of the same class (source = "
							+ sourceClass.getName()
							+ ", destination = "
							+ destinationClass.getName() + ")");
			throw new MetadataAccessException(
					"The two objects were not of the same class (source = "
							+ sourceClass.getName() + ", destination = "
							+ destinationClass.getName() + ")");
		}

		// Now return the result
		return destinationUpdated;
	}

	/**
	 * @param emailAddress
	 * @param message
	 */
	protected void addMessage(String emailAddress, String message) {
		superLogger.debug("addMessage called with emailAddress = "
				+ emailAddress + " and message = " + message);
		TreeMap localTreeMap = (TreeMap) messageTreeMap.get();
		superLogger.debug("Before adding message, localTreeMap size is "
				+ localTreeMap.size());
		if (localTreeMap.containsKey(emailAddress)) {
			superLogger
					.debug("Email address was already there, so will append");
			String messageToAppendTo = (String) localTreeMap.get(emailAddress);
			localTreeMap
					.put(emailAddress, messageToAppendTo + "<hr>" + message);
		} else {
			superLogger
					.debug("Email address was not already there, so will put new message");
			localTreeMap.put(emailAddress, message);
		}
		superLogger.debug("After adding message, localTreeMap size is "
				+ localTreeMap.size());
		messageTreeMap.set(localTreeMap);
	}

	/**
	 * This a utility to help with auto-generation of stuff
	 * 
	 * @return
	 */
	protected String getUniqueNameSuffix() {
		return xmlDateFormat.format(new Date()) + "_"
				+ Math.abs(generator.nextInt());
	}

	/**
	 * This method reads in the properties from the resource file (classpath)
	 */
	private void loadProperties() {
		daoProperties = new Properties();
		try {
			daoProperties.load(this.getClass().getResourceAsStream(
					"/moos/ssds/dao/dao.properties"));
		} catch (Exception e) {
			superLogger.error("Exception trying to read in properties file: "
					+ e.getMessage());
		}

		// Make sure the properties were read from the JAR OK
		if (daoProperties != null) {
			superLogger.debug("Loaded props OK");
			// Grab the email address of the ssds administrator
			ssdsAdminEmailToAddress = daoProperties
					.getProperty("metadata.dao.ssds.admin.to.email.address");
			superLogger.debug("Will send messages to "
					+ ssdsAdminEmailToAddress);
			// Check for setting of whether or not to send associated users
			// emails
			String sendUserEmailsProperty = daoProperties
					.getProperty("metadata.dao.ssds.user.email.messages");
			if ((sendUserEmailsProperty != null)
					&& (sendUserEmailsProperty.equalsIgnoreCase("true"))) {
				sendUserMessages = true;
			} else {
				sendUserMessages = false;
			}
		} else {
			superLogger.error("Could not load the dao.properties.");
		}
	}

	/**
	 * This method takes in a <code>Collection</code> of
	 * <code>IMetadataObject</code>s and then tries to sort them by the
	 * property name supplied. It tries to find the property by looking for a
	 * method that starts with "get" and ends with the property name supplied.
	 * If the collection can't be sorted, <code>null</code> will be returned.
	 * 
	 * @param toSort
	 * @param propertyName
	 * @return
	 */
	protected Collection sortCollectionByPropertyName(Collection toSort,
			String propertyName, String ascendingOrDescending) {
		// The Collection to return
		Collection toReturn = null;

		// Make sure the incoming collection is not null and the property name
		// is supplied
		if ((toSort != null) && (propertyName != null)
				&& (!propertyName.equals(""))) {
			// Create a TreeMap to help with the sorting
			TreeMap treeMap = new TreeMap();

			// Now iterate over the incoming collection
			Iterator iterator = toSort.iterator();
			while (iterator.hasNext()) {
				// Grab the object
				Object objectToSort = iterator.next();
				// Grab the class of the object
				Class objectClass = objectToSort.getClass();
				// Make sure the property name starts with a captial letter
				String propertyNameMethod = "get"
						+ propertyName.substring(0, 1).toUpperCase()
						+ propertyName.substring(1);
				// Now try to find a method that looks like "get" + propertyName
				Method methodToGetProperty = null;
				try {
					methodToGetProperty = objectClass.getMethod(
							propertyNameMethod, new Class[0]);
				} catch (SecurityException e) {
					superLogger
							.error("SecurityException trying to find the get property method: "
									+ e.getMessage());
				} catch (NoSuchMethodException e) {
					superLogger
							.error("NoSuchMethodException trying to find the get property method: "
									+ e.getMessage());
				}
				// If the method was found call it to get the object to sort by
				if (methodToGetProperty != null) {
					Object fieldToSortBy = null;
					try {
						fieldToSortBy = methodToGetProperty.invoke(
								objectToSort, new Object[0]);
					} catch (IllegalArgumentException e) {
						superLogger
								.error("IllegalArgumentException trying to find the call the get "
										+ "property method for sorting: "
										+ e.getMessage());
					} catch (IllegalAccessException e) {
						superLogger
								.error("IllegalAccessException trying to find the call the get "
										+ "property method for sorting: "
										+ e.getMessage());
					} catch (InvocationTargetException e) {
						superLogger
								.error("InvocationTargetException trying to find the call the get "
										+ "property method for sorting: "
										+ e.getMessage());
					}
					// If something was found, add it to the treemap
					if (fieldToSortBy != null) {
						treeMap.put(fieldToSortBy, objectToSort);
					}
				}
			}
			// Now if the treemap has stuff in it, convert it to a regular ol
			// collection
			if ((treeMap != null) && (treeMap.size() > 0)) {
				toReturn = new ArrayList(treeMap.values());
			}

			// Now if the a descending order was specified, reverse the order of
			// the arraylist
			if ((toReturn != null)
					&& (toReturn.size() > 1)
					&& (ascendingOrDescending != null)
					&& (ascendingOrDescending
							.equalsIgnoreCase(DESCENDING_ORDER))) {
				Object[] reversedArray = new Object[toReturn.size()];
				// Iterate over the collection and fill the array backwards
				Iterator reverseIterator = toReturn.iterator();
				int countDownIndex = toReturn.size() - 1;
				while (reverseIterator.hasNext()) {
					reversedArray[countDownIndex] = reverseIterator.next();
					countDownIndex--;
				}
				toReturn = Arrays.asList(reversedArray);
			}
		}

		// Now return it
		return toReturn;
	}

	// Some constants
	public static final String ASCENDING_ORDER = "asc";
	public static final String DESCENDING_ORDER = "desc";

	/**
	 * This is the class that the particular DAO is responsible for
	 */
	private Class persistentClass = null;

	/**
	 * This is the session that is used to perform actions in the persistence
	 * layer
	 */
	private Session session = null;

	/**
	 * A properties object that contains the properties for the dao classes
	 */
	protected Properties daoProperties;

	/**
	 * A static ThreadLocal that will contain a TreeMap of email addresses to
	 * messages which is built up during the thread's execution.
	 */
	static ThreadLocal messageTreeMap = null;

	/**
	 * This is the email address where system email messages will be sent to
	 */
	static String ssdsAdminEmailToAddress = null;

	/**
	 * This is a boolean to indicate if the system should notify the associated
	 * users to events.
	 */
	static boolean sendUserMessages = false;

	/**
	 * A log4j logger
	 */
	static Logger superLogger = Logger.getLogger(MetadataDAO.class);

	/**
	 * A random number generator
	 */
	private static final Random generator = new Random(new Date().getTime());

	/**
	 * A formatting utility
	 */
	protected static final XmlDateFormat xmlDateFormat = new XmlDateFormat();
}
