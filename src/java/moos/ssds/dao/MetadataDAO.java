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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.TreeMap;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.CommentTag;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataContainerGroup;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.DataProducerGroup;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.DeviceType;
import moos.ssds.metadata.Event;
import moos.ssds.metadata.HeaderDescription;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.Keyword;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.RecordDescription;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.Software;
import moos.ssds.metadata.StandardDomain;
import moos.ssds.metadata.StandardKeyword;
import moos.ssds.metadata.StandardReferenceScale;
import moos.ssds.metadata.StandardUnit;
import moos.ssds.metadata.StandardVariable;
import moos.ssds.metadata.UserGroup;
import moos.ssds.util.XmlDateFormat;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.proxy.HibernateProxy;

/**
 * This is the super class that implements the functions that are common to all
 * SSDS Data Access Objects (DAO)s.
 * 
 * @stereotype service
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.22 $
 */
public abstract class MetadataDAO implements IMetadataDAO {

	/**
	 * A log4j logger
	 */
	static Logger superLogger = Logger.getLogger(MetadataDAO.class);

	/**
	 * Some constants for ordering search results
	 */
	public static final String ASCENDING_ORDER = "asc";
	public static final String DESCENDING_ORDER = "desc";

	/**
	 * This is the class that the particular DAO is responsible for
	 */
	private Class<? extends IMetadataObject> persistentClass = null;

	/**
	 * This is the Hibernate session that is used to perform actions in the
	 * persistence layer.
	 */
	private Session session = null;

	/**
	 * A properties object that contains the properties for the DAO classes
	 */
	protected Properties daoProperties;

	/**
	 * A static ThreadLocal that will contain a TreeMap of email addresses to
	 * messages which is built up during the thread's execution.
	 */
	static ThreadLocal<TreeMap<String, String>> messageTreeMap = null;

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
	 * A random number generator
	 */
	private static final Random generator = new Random(new Date().getTime());

	/**
	 * A formatting utility
	 */
	protected static final XmlDateFormat xmlDateFormat = new XmlDateFormat();

	/**
	 * This is the constructor that takes in the <code>Class</code> that the DAO
	 * will be dealing with and the Hibernate <code>Session</code> that it will
	 * use to perform the persistence operations.
	 * 
	 * @param persistentClass
	 *            is the <code>Class</code> that the DAO will be working with.
	 *            For example, for the <code>PersonDAO</code> this would be
	 *            <code>Person.class</code>.
	 * @param session
	 *            is the Hibernate <code>Session</code> that it will use to
	 *            perform persistence operations
	 */
	public MetadataDAO(Class<? extends IMetadataObject> persistentClass,
			Session session) throws MetadataAccessException {

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

			superLogger.debug("The ThreadLocal to hold the message TreeMap "
					+ "was not instantiated yet, will do "
					+ "so now (Thread name is "
					+ Thread.currentThread().getName() + ")");
			messageTreeMap = new ThreadLocal<TreeMap<String, String>>();
			messageTreeMap.set(new TreeMap<String, String>());
		} else {
			// Check to see if the ThreadLocal has a TreeMap
			TreeMap<?, ?> treeMapToCheck = (TreeMap<?, ?>) messageTreeMap.get();
			// If not, create a new one
			if (treeMapToCheck == null) {
				superLogger.debug("The ThreadLocal was not null, but did "
						+ " not have a TreeMap associated to it "
						+ "so I created a new one");
				messageTreeMap.set(new TreeMap<String, String>());
			} else {
				superLogger.debug("The ThreadLocal to hold the message "
						+ "TreeMap was already instantiated and "
						+ "has a TreeMap, so no need to "
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
	public Class<? extends IMetadataObject> getPersistentClass() {
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
	public void setPersistentClass(
			Class<? extends IMetadataObject> persistentClass) {
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
			superLogger.error("HibernateException caught on get "
					+ "call (will be re-thrown):" + e.getMessage());
			throw new MetadataAccessException("HibernateException ("
					+ e.getClass().getName()
					+ ") caught in MetadataDAO.findById (Message =  "
					+ e.getMessage() + ")");
		}

		// Return the result
		superLogger.debug("OK, will return the metadataObject found by ID");
		if (returnFullObjectGraph)
			metadataObject = getRealObjectAndRelationships(metadataObject);

		return metadataObject;
	}

	/**
	 * @see IMetadataDAO#findId(IMetadataObject)
	 */
	public Long findId(IMetadataObject metadataObject)
			throws MetadataAccessException {

		// The ID to return
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
	@SuppressWarnings("unchecked")
	public Collection<? extends IMetadataObject> findAll(
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// The collection to return
		superLogger.debug("findAll called");
		Collection<? extends IMetadataObject> results = new ArrayList<IMetadataObject>();

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
		superLogger.debug("SQL string for findAll is: "
				+ sqlStringBuffer.toString());

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
			results = getRealObjectsAndRelationships(results);

		// Return what was found
		return results;
	}

	/**
	 * @see IMetadataDAO#findBySQL(String, String, Class, boolean)
	 */
	@SuppressWarnings("unchecked")
	public Collection<? extends IMetadataObject> findBySQL(String sqlString,
			String aliasName, Class<?> classOfReturn,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// If the query contains terms that are not allowed, throw an exception
		superLogger.debug("findBySQL called with SQL string: " + sqlString);
		if (!sqlString.startsWith("SELECT")
				&& (!sqlString.startsWith("select"))) {
			throw new MetadataAccessException(
					"The action performed in the SQL String specified is not allowed");
		}

		// The collection to return
		Collection<? extends IMetadataObject> results = new ArrayList<IMetadataObject>();

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
			results = getRealObjectsAndRelationships(results);

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
	 * This method returns a copy of the <code>IMetadataObject</code> supplied
	 * with its associated relationships also copied. This copy depends on the
	 * underlying <code>IMetadataObject</code> so consult the documentation for
	 * the <code>deepCopy</code> method on the metadata classes.
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
			superLogger.error("CloneNotSupportedException caught "
					+ "trying to deep copy "
					+ persistentMetadataObject.toStringRepresentation("|"));
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
	 * notify users of important DAO related messages.
	 * 
	 * @return
	 */
	public static ThreadLocal<TreeMap<String, String>> getMessageTreeMap() {
		return messageTreeMap;
	}

	public static void setMessageTreeMap(
			ThreadLocal<TreeMap<String, String>> threadLocal) {
		messageTreeMap = threadLocal;
	}

	/**
	 * This method takes in an object that supports the
	 * <code>IMetadataObject</code> interface and returns the real object with
	 * it's instantiated relationships filled out inside a Hibernate session.
	 * This was necessary to make sure we were transferring real objects instead
	 * of the Hibernate proxies that are used to manage lazy loading.
	 * 
	 * @param metadataObject
	 *            The <code>IMetadataObject</code> who needs it and it's
	 *            relationships filled out with real objects instead of
	 *            Hibernate Proxies
	 * @return is an object that supports the <code>IMetadataObject</code> and
	 *         if it was a Hibernate proxy, it is replaced by it's real object
	 *         and the objects relationships are filled out as well. <b>Please
	 *         note that the extent of the graphs that are filled out are
	 *         dependent on the objects so the server can manage large object
	 *         graphs with better performance</b>
	 * @throws MetadataAccessException
	 *             if something goes awry
	 */
	@SuppressWarnings("unchecked")
	protected static IMetadataObject getRealObjectAndRelationships(
			IMetadataObject metadataObject) throws MetadataAccessException {

		// The object to return
		IMetadataObject objectToReturn = null;

		// Make sure object is there
		if (metadataObject != null) {

			// Make the call to Hibernate to initialize the object
			Hibernate.initialize(metadataObject);

			// Link it to the return object for now (in case it is not a
			// Hibernate proxy)
			objectToReturn = metadataObject;

			// If object is HibernateProxy, replace it with the real
			// implementation
			if (metadataObject instanceof HibernateProxy) {
				objectToReturn = ((IMetadataObject) ((HibernateProxy) metadataObject)
						.getHibernateLazyInitializer().getImplementation());
			}

			// Now depending on the object, drill down into relationships

			// DataContainer
			if (objectToReturn instanceof DataContainer) {
				// Cast it
				DataContainer dataContainer = (DataContainer) objectToReturn;

				// Check for person
				if (dataContainer.getPerson() != null)
					dataContainer
							.setPerson((Person) getRealObjectAndRelationships(dataContainer
									.getPerson()));

				// Check for HeaderDescription
				if (dataContainer.getHeaderDescription() != null)
					dataContainer
							.setHeaderDescription((HeaderDescription) getRealObjectAndRelationships(dataContainer
									.getHeaderDescription()));

				// Check for RecordDescription
				if (dataContainer.getRecordDescription() != null)
					dataContainer
							.setRecordDescription((RecordDescription) getRealObjectAndRelationships(dataContainer
									.getRecordDescription()));

				// DataContainer Groups
				if (dataContainer.getDataContainerGroups() != null
						&& dataContainer.getDataContainerGroups().size() > 0)
					dataContainer
							.setDataContainerGroups((Collection<DataContainerGroup>) getRealObjectsAndRelationships(dataContainer
									.getDataContainerGroups()));

				// Keywords
				if (dataContainer.getKeywords() != null
						&& dataContainer.getKeywords().size() > 0)
					dataContainer
							.setKeywords((Collection<Keyword>) getRealObjectsAndRelationships(dataContainer
									.getKeywords()));

				// Resources
				if (dataContainer.getResources() != null
						&& dataContainer.getResources().size() > 0)
					dataContainer
							.setResources((Collection<Resource>) getRealObjectsAndRelationships(dataContainer
									.getResources()));

				// Note: consumers and creator are left off here to constrain
				// the graph and will be left to the client to pursue

			}

			// DataProducer
			if (objectToReturn instanceof DataProducer) {
				// Cast it
				DataProducer dataProducer = (DataProducer) objectToReturn;

				// Check for person
				if (dataProducer.getPerson() != null)
					dataProducer
							.setPerson((Person) getRealObjectAndRelationships(dataProducer
									.getPerson()));

				// Check for device
				if (dataProducer.getDevice() != null)
					dataProducer
							.setDevice((Device) getRealObjectAndRelationships(dataProducer
									.getDevice()));

				// Check for software
				if (dataProducer.getSoftware() != null)
					dataProducer
							.setSoftware((Software) getRealObjectAndRelationships(dataProducer
									.getSoftware()));

				// parentDataProducer is ignored to keep graph boundaries

				// childDataProducers are ignored to keep graph boundaries

				// Check for DataProducerGroups
				if (dataProducer.getDataProducerGroups() != null
						&& dataProducer.getDataProducerGroups().size() > 0)
					dataProducer
							.setDataProducerGroups((Collection<DataProducerGroup>) getRealObjectsAndRelationships(dataProducer
									.getDataProducerGroups()));

				// Check for inputs
				if (dataProducer.getInputs() != null
						&& dataProducer.getInputs().size() > 0)
					dataProducer
							.setInputs((Collection<DataContainer>) getRealObjectsAndRelationships(dataProducer
									.getInputs()));

				// Check for outputs
				if (dataProducer.getOutputs() != null
						&& dataProducer.getOutputs().size() > 0)
					dataProducer
							.setOutputs((Collection<DataContainer>) getRealObjectsAndRelationships(dataProducer
									.getOutputs()));

				// Check for Resources
				if (dataProducer.getResources() != null
						&& dataProducer.getResources().size() > 0)
					dataProducer
							.setResources((Collection<Resource>) getRealObjectsAndRelationships(dataProducer
									.getResources()));

				// Check for Keywords
				if (dataProducer.getKeywords() != null
						&& dataProducer.getKeywords().size() > 0)
					dataProducer
							.setKeywords((Collection<Keyword>) getRealObjectsAndRelationships(dataProducer
									.getKeywords()));

				// Check for Events
				if (dataProducer.getEvents() != null
						&& dataProducer.getEvents().size() > 0)
					dataProducer
							.setEvents((Collection<Event>) getRealObjectsAndRelationships(dataProducer
									.getEvents()));
			}

			// Device
			if (objectToReturn instanceof Device) {
				// Cast it
				Device device = (Device) objectToReturn;

				// Swap out person if available
				if (device.getPerson() != null)
					device.setPerson((Person) getRealObjectAndRelationships(device
							.getPerson()));

				// Same with DeviceType
				if (device.getDeviceType() != null)
					device.setDeviceType((DeviceType) getRealObjectAndRelationships(device
							.getDeviceType()));

				// Also with resources
				if (device.getResources() != null
						&& device.getResources().size() > 0)
					device.setResources((Collection<Resource>) getRealObjectsAndRelationships(device
							.getResources()));
			}

			// HeaderDescription
			if (objectToReturn instanceof HeaderDescription) {
				// Cast it
				HeaderDescription headerDescription = (HeaderDescription) objectToReturn;

				// Replace any comment tags
				if (headerDescription.getCommentTags() != null
						&& headerDescription.getCommentTags().size() > 0) {
					Collection<CommentTag> commentTags = (Collection<CommentTag>) getRealObjectsAndRelationships(headerDescription
							.getCommentTags());

					// Clear the current collection and add the real objects
					headerDescription.clearCommentTags();

					for (Iterator<CommentTag> iterator = commentTags.iterator(); iterator
							.hasNext();) {
						CommentTag commentTag = (CommentTag) iterator.next();
						headerDescription.addCommentTag(commentTag);
					}
				}
			}

			// Person
			if (objectToReturn instanceof Person) {
				// Cast it to a person
				Person person = (Person) objectToReturn;

				// Replace the user groups with the real objects
				if (person.getUserGroups() != null
						&& person.getUserGroups().size() > 0)
					person.setUserGroups((Collection<UserGroup>) getRealObjectsAndRelationships(person
							.getUserGroups()));
			}

			// RecordDescription
			if (objectToReturn instanceof RecordDescription) {
				// Cast it
				RecordDescription recordDescription = (RecordDescription) objectToReturn;

				// Check for recordVariables
				if (recordDescription.getRecordVariables() != null
						&& recordDescription.getRecordVariables().size() > 0) {
					Collection<RecordVariable> recordVariables = (Collection<RecordVariable>) getRealObjectsAndRelationships(recordDescription
							.getRecordVariables());

					// Clear the existing ones
					recordDescription.clearRecordVariables();

					// Now add the replacements
					for (Iterator<RecordVariable> iterator = recordVariables
							.iterator(); iterator.hasNext();) {
						RecordVariable recordVariable = (RecordVariable) iterator
								.next();
						recordDescription.addRecordVariable(recordVariable);
					}
				}
			}

			// RecordVariable
			if (objectToReturn instanceof RecordVariable) {
				// Cast it
				RecordVariable recordVariable = (RecordVariable) objectToReturn;

				// StandardDomain
				if (recordVariable.getStandardDomain() != null)
					recordVariable
							.setStandardDomain((StandardDomain) getRealObjectAndRelationships(recordVariable
									.getStandardDomain()));

				// StandardKeyword
				if (recordVariable.getStandardKeyword() != null)
					recordVariable
							.setStandardKeyword((StandardKeyword) getRealObjectAndRelationships(recordVariable
									.getStandardKeyword()));

				// StandardReferenceScale
				if (recordVariable.getStandardReferenceScale() != null)
					recordVariable
							.setStandardReferenceScale((StandardReferenceScale) getRealObjectAndRelationships(recordVariable
									.getStandardReferenceScale()));

				// StandardUnit
				if (recordVariable.getStandardUnit() != null)
					recordVariable
							.setStandardUnit((StandardUnit) getRealObjectAndRelationships(recordVariable
									.getStandardUnit()));

				// StandardVariable
				if (recordVariable.getStandardVariable() != null)
					recordVariable
							.setStandardVariable((StandardVariable) getRealObjectAndRelationships(recordVariable
									.getStandardVariable()));
			}

			// StandardVariable
			if (objectToReturn instanceof StandardVariable) {
				// Cast it
				StandardVariable standardVariable = (StandardVariable) objectToReturn;

				// Check for standard units
				if (standardVariable.getStandardUnits() != null
						&& standardVariable.getStandardUnits().size() > 0)
					standardVariable
							.setStandardUnits((Collection<StandardUnit>) getRealObjectsAndRelationships(standardVariable
									.getStandardUnits()));
			}
		}

		// Return it
		return objectToReturn;
	}

	/**
	 * This method takes in a collection of <code>IMetadataObject</code>s and
	 * iterate over them to create a new collection of
	 * <code>IMetadataObject</code>s which are real metadata objects instead of
	 * Hibernate proxies.
	 * 
	 * @param metadataObjects
	 *            the collection of <code>IMetadataObject</code>s to replace
	 *            with the real objects
	 * @return a collection of <code>IMetadataObject</code>s which are no longer
	 *         Hibernate proxies
	 * @throws MetadataAccessException
	 */
	@SuppressWarnings("unchecked")
	protected static Collection<? extends IMetadataObject> getRealObjectsAndRelationships(
			Collection<? extends IMetadataObject> metadataObjects)
			throws MetadataAccessException {

		// Create a new HashSet
		Collection<IMetadataObject> metadataObjectsToReturn = new HashSet<IMetadataObject>();

		// Iterate
		for (Iterator<IMetadataObject> iterator = (Iterator<IMetadataObject>) metadataObjects
				.iterator(); iterator.hasNext();) {
			IMetadataObject metadataObject = iterator.next();
			// Add the real object
			metadataObjectsToReturn
					.add(getRealObjectAndRelationships(metadataObject));
		}

		// Now return the result
		return metadataObjectsToReturn;
	}

	/**
	 * This method adds an ordering clause to the criteria supplied. If checks
	 * to see if the property is valid, then adds the order clause to the
	 * Criteria
	 * 
	 * @param criteria
	 *            The <code>Criteria</code> to add the clause to.
	 * @param orderByProperty
	 *            is the <code>String</code> that is the name of the property to
	 *            order by.
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
	 * @return a <code>boolean</code> to indicate if the destination object was
	 *         changed (<code>true</code>) or not (<code>false</code>)
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
		Class<?> destinationClass = destinationMetadataObject.getClass();
		Class<?> sourceClass = sourceMetadataObject.getClass();

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
					Class<?> fieldClass = currentField.getType();

					// Skip the collection classes as they are most likely
					// relationships with other IMetadataObjects. Also skip any
					// fields that are IMetadataObject
					if ((fieldClass.getName().equals("java.util.Collection"))
							|| (fieldClass.getName()
									.startsWith("moos.ssds.metadata"))) {
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

						// All getters should have empty parameter lists
						Method getterMethod = null;
						Method isMethod = null;
						try {
							getterMethod = destinationClass.getMethod(
									getterMethodName, new Class[0]);
						} catch (SecurityException e) {
							superLogger.error("SecurityException caught "
									+ "trying to find method by name of "
									+ getterMethodName + ": " + e.getMessage());
						} catch (NoSuchMethodException e) {
						}

						try {
							isMethod = destinationClass.getMethod(isMethodName,
									new Class[0]);
						} catch (SecurityException e) {
							superLogger.error("SecurityException caught "
									+ "trying to find method by name of "
									+ isMethodName + ": " + e.getMessage());
						} catch (NoSuchMethodException e) {
						}

						// If method was not found, go to next field
						if ((getterMethod == null) && (isMethod == null)) {
							continue;
						}

						// Call the method on the source and destination objects
						// to get their field values
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
								superLogger.error("IllegalArgumentException "
										+ "caught trying to invoke "
										+ getterMethodName + ": "
										+ e.getMessage());
							} catch (IllegalAccessException e) {
								superLogger.error("IllegalArgumentException "
										+ "caught trying to invoke "
										+ getterMethodName + ": "
										+ e.getMessage());
							} catch (InvocationTargetException e) {
								superLogger.error("IllegalArgumentException "
										+ "caught trying to invoke "
										+ getterMethodName + ": "
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
								superLogger.error("IllegalArgumentException "
										+ "caught trying to invoke "
										+ isMethodName + ": " + e.getMessage());
							} catch (IllegalAccessException e) {
								superLogger.error("IllegalArgumentException "
										+ "caught trying to invoke "
										+ isMethodName + ": " + e.getMessage());
							} catch (InvocationTargetException e) {
								superLogger.error("IllegalArgumentException "
										+ "caught trying to invoke "
										+ isMethodName + ": " + e.getMessage());
							}
						}

						// If the source result is not null, we need to check
						// the destination object to see if they are different
						if (sourceResult != null) {
							// First create a boolean to track whether the
							// setterMethod needs to be called on the
							// destination object
							boolean updateNeeded = false;

							// The first and easiest check is to see if the
							// destination result is null, it will need setting
							if (destinationResult == null) {
								updateNeeded = true;
							} else {
								// First check to see if it is the version of
								// object
								if ((getterMethodName != null)
										&& (getterMethodName
												.equals("getVersion"))) {
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
							// needed an update, do it
							if (updateNeeded) {
								// Mark the object as updated (for the return)
								destinationUpdated = true;

								// Grab the setter method
								@SuppressWarnings("rawtypes")
								Class[] parameterClass = new Class[1];
								parameterClass[0] = fieldClass;
								Method setterMethod = null;
								try {
									setterMethod = destinationClass.getMethod(
											setterMethodName, parameterClass);
								} catch (SecurityException e) {
									superLogger.error("SecurityException "
											+ "caught trying to get "
											+ "setter method from "
											+ "desination object: "
											+ e.getMessage());
								} catch (NoSuchMethodException e) {
									superLogger.error("NoSuchMethodException "
											+ "caught trying to get "
											+ "setter method from "
											+ "desination object: "
											+ e.getMessage());
								}
								// Now if the setter method was found, call it
								// with the result from the source getterMethod
								if (setterMethod != null) {
									Object[] parameterObjects = new Object[1];
									parameterObjects[0] = sourceResult;
									try {
										setterMethod.invoke(
												destinationMetadataObject,
												parameterObjects);
									} catch (IllegalArgumentException e) {
										superLogger
												.error("IllegalArgumentException "
														+ "caught trying "
														+ "to call setter on "
														+ "destination object: "
														+ e.getMessage());
									} catch (IllegalAccessException e) {
										superLogger
												.error("IllegalAccessException "
														+ "caught trying "
														+ "to call setter on "
														+ "destination object: "
														+ e.getMessage());
									} catch (InvocationTargetException e) {
										superLogger
												.error("InvocationTargetException "
														+ "caught trying "
														+ "to call setter on "
														+ "destination object: "
														+ e.getMessage());
									}
								}
							}
						}
					}
				}
			}
		} else {
			superLogger.error("The two objects were not of "
					+ "the same class (source = " + sourceClass.getName()
					+ ", destination = " + destinationClass.getName() + ")");
			throw new MetadataAccessException("The two objects "
					+ "were not of the same class (source = "
					+ sourceClass.getName() + ", destination = "
					+ destinationClass.getName() + ")");
		}

		// Now return the result
		return destinationUpdated;
	}

	/**
	 * This method adds a message to the ThreadLocal TreeMap so that it can be
	 * sent to the email address given upon completion of the session.
	 * 
	 * @param emailAddress
	 * @param message
	 */
	protected void addMessage(String emailAddress, String message) {
		superLogger.debug("addMessage called with emailAddress = "
				+ emailAddress + " and message = " + message);

		TreeMap<String, String> localTreeMap = (TreeMap<String, String>) messageTreeMap
				.get();
		superLogger.debug("Before adding message, localTreeMap size is "
				+ localTreeMap.size());

		if (localTreeMap.containsKey(emailAddress)) {
			superLogger.debug("Email address was already there, "
					+ "so will append");
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
			// Grab the email address of the SSDS administrator
			ssdsAdminEmailToAddress = daoProperties
					.getProperty("metadata.dao.ssds.admin.to.email.address");
			superLogger.debug("Will send messages to "
					+ ssdsAdminEmailToAddress);
			// Check for setting of whether or not to send associated users
			// an email
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
	 * <code>IMetadataObject</code>s and then tries to sort them by the property
	 * name supplied. It tries to find the property by looking for a method that
	 * starts with "get" and ends with the property name supplied. If the
	 * collection can't be sorted, <code>null</code> will be returned.
	 * 
	 * @param toSort
	 * @param propertyName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Collection<? extends IMetadataObject> sortCollectionByPropertyName(
			Collection<IMetadataObject> toSort, String propertyName,
			String ascendingOrDescending) {
		// The Collection to return
		Collection<IMetadataObject> toReturn = null;

		// Make sure the incoming collection is not null and the property name
		// is supplied
		if ((toSort != null) && (propertyName != null)
				&& (!propertyName.equals(""))) {
			// Create a TreeMap to help with the sorting
			TreeMap<Object, IMetadataObject> treeMap = new TreeMap<Object, IMetadataObject>();

			// Now iterate over the incoming collection
			Iterator<IMetadataObject> iterator = toSort.iterator();
			while (iterator.hasNext()) {
				// Grab the object
				IMetadataObject objectToSort = iterator.next();
				// Grab the class of the object
				@SuppressWarnings("rawtypes")
				Class objectClass = objectToSort.getClass();
				// Make sure the property name starts with a capital letter
				String propertyNameMethod = "get"
						+ propertyName.substring(0, 1).toUpperCase()
						+ propertyName.substring(1);
				// Now try to find a method that looks like "get" + propertyName
				Method methodToGetProperty = null;
				try {
					methodToGetProperty = objectClass.getMethod(
							propertyNameMethod, new Class[0]);
				} catch (SecurityException e) {
					superLogger.error("SecurityException "
							+ "trying to find the get property method: "
							+ e.getMessage());
				} catch (NoSuchMethodException e) {
					superLogger.error("NoSuchMethodException "
							+ "trying to find the get property method: "
							+ e.getMessage());
				}
				// If the method was found call it to get the object to sort by
				if (methodToGetProperty != null) {
					Object fieldToSortBy = null;
					try {
						fieldToSortBy = methodToGetProperty.invoke(
								objectToSort, new Object[0]);
					} catch (IllegalArgumentException e) {
						superLogger.error("IllegalArgumentException "
								+ "trying to find the call the get "
								+ "property method for sorting: "
								+ e.getMessage());
					} catch (IllegalAccessException e) {
						superLogger.error("IllegalAccessException "
								+ "trying to find the call the get "
								+ "property method for sorting: "
								+ e.getMessage());
					} catch (InvocationTargetException e) {
						superLogger.error("InvocationTargetException "
								+ "trying to find the call the get "
								+ "property method for sorting: "
								+ e.getMessage());
					}
					// If something was found, add it to the TreeMap
					if (fieldToSortBy != null) {
						treeMap.put(fieldToSortBy, objectToSort);
					}
				}
			}

			// Now if the TreeMap has stuff in it, convert it to a regular old
			// collection
			if ((treeMap != null) && (treeMap.size() > 0)) {
				toReturn = new ArrayList<IMetadataObject>(
						(Collection<IMetadataObject>) treeMap.values());
			}

			// Now if the a descending order was specified, reverse the order of
			// the ArrayList
			if ((toReturn != null)
					&& (toReturn.size() > 1)
					&& (ascendingOrDescending != null)
					&& (ascendingOrDescending
							.equalsIgnoreCase(DESCENDING_ORDER))) {
				IMetadataObject[] reversedArray = new IMetadataObject[toReturn
						.size()];
				// Iterate over the collection and fill the array backwards
				Iterator<IMetadataObject> reverseIterator = toReturn.iterator();
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
}
