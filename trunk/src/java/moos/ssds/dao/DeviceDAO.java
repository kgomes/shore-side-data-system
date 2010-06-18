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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.DeviceType;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.services.metadata.DataProducerAccessLocal;
import moos.ssds.services.metadata.DataProducerAccessLocalHome;
import moos.ssds.services.metadata.DataProducerAccessUtil;

import org.apache.log4j.Logger;
import org.doomdark.uuid.EthernetAddress;
import org.doomdark.uuid.UUID;
import org.doomdark.uuid.UUIDGenerator;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.proxy.HibernateProxy;

/**
 * This Data Access Object (DAO) provides methods for interacting with the
 * persitence mechanism that handles the persistence of <code>Device</code>
 * objects. It also provides query methods.
 * <hr>
 * 
 * @stereotype service
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.26 $
 */
public class DeviceDAO extends MetadataDAO {

	/**
	 * This is the constructor that calls the super constructor and sets the
	 * proper class and Hibernate Session
	 * 
	 * @param session
	 *            is the <code>Session</code> that will be used in the
	 *            persistent operations
	 * @throws MetadataAccessException
	 *             if something goes weird
	 */
	public DeviceDAO(Session session) throws MetadataAccessException {
		super(Device.class, session);
	}

	/**
	 * @see IMetadataDAO#findEquivalentPersistentObject(IMetadataObject)
	 */
	public IMetadataObject findEquivalentPersistentObject(
			IMetadataObject metadataObject, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		logger.debug("findEquivalentPersistentObject called");
		// Check the incoming device
		Device device = this.checkIncomingMetadataObject(metadataObject);

		// The id that will be returned
		Device deviceToReturn = null;
		if (device.getId() != null) {
			try {
				Criteria criteria = this.formulatePropertyCriteria(false,
						device.getId(), null, false, null, false, null, false,
						null, false, null, false, null, false, null, null);
				deviceToReturn = (Device) criteria.uniqueResult();
			} catch (HibernateException e) {
				throw new MetadataAccessException(e);
			}
		}
		// If not found, try by UUID
		if ((deviceToReturn == null) && (device.getUuid() != null)
				&& (!device.getUuid().equals(""))) {
			try {
				Criteria criteria = this.formulatePropertyCriteria(false, null,
						device.getUuid(), true, null, false, null, false, null,
						false, null, false, null, false, null, null);
				deviceToReturn = (Device) criteria.uniqueResult();
			} catch (HibernateException e) {
				throw new MetadataAccessException(e);
			}
		}

		// Check for relationship initialization
		if (returnFullObjectGraph)
			deviceToReturn = (Device) getRealObjectAndRelationships(deviceToReturn);

		// OK, return the result
		if (deviceToReturn != null)
			logger.debug("OK, returning the persistent device: "
					+ deviceToReturn.toStringRepresentation("|"));
		return deviceToReturn;
	}

	/**
	 * This method returns a <code>Collection</code> of <code>Long</code>s that
	 * are the IDs of all the devices that are registered in SSDS.
	 * 
	 * @return a <code>Collection</code> of <code>Long</code>s that are the IDs
	 *         of all devices in SSDS.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public Collection findAllIDs() throws MetadataAccessException {
		Collection deviceIDs = null;

		// Create the query and run it
		try {
			Query query = getSession().createQuery(
					"select distinct device.id from "
							+ "Device device order by device.id");
			deviceIDs = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return deviceIDs;
	}

	/**
	 * @see IMetadataDAO#countFindAllIDs()
	 */
	public int countFindAllIDs() throws MetadataAccessException {
		// The count
		int count = 0;
		try {
			Long longCount = (Long) getSession().createQuery(
					"select count(distinct device.id) from Device device")
					.uniqueResult();
			if (longCount != null)
				count = longCount.intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the result
		return count;
	}

	/**
	 * This method looks up and returns the persistent instance of a
	 * <code>Device</code> with the specified UUID.
	 * 
	 * @param uuid
	 *            is the UUID of the <code>Device</code> to search for
	 * @return is the <code>Device</code> that was found in the persistent store
	 *         with the given UUID. If no <code>Device</code> was found, null is
	 *         returned.
	 */
	public Device findByUuid(String uuid, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// First make sure the incoming value is not null
		if ((uuid == null) || (uuid.equals(""))) {
			logger.debug("Failed: incoming uuid was null or empty");
			return null;
		}

		// Create the Device to return
		Device deviceToReturn = null;

		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					uuid, true, null, false, null, false, null, false, null,
					false, null, false, null, null);
			deviceToReturn = (Device) criteria.uniqueResult();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Check for relationship init
		if (returnFullObjectGraph)
			deviceToReturn = (Device) getRealObjectAndRelationships(deviceToReturn);

		// Return the result
		return deviceToReturn;
	}

	/**
	 * This method returns all devices that match the given name exactly
	 * 
	 * @param name
	 * @param orderByPropertyName
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByName(String name, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		logger.debug("DeviceDAO.findByName called with:");
		logger.debug("->name = " + name);
		logger.debug("->exactMatch = " + exactMatch);
		logger.debug("->orderByPropertyName = " + orderByPropertyName);
		logger.debug("->ascendingOrDescending = " + ascendingOrDescending);
		logger.debug("->returnFullObjectGraph = " + returnFullObjectGraph);
		// The devices to return
		Collection devicesToReturn = new ArrayList();

		// If no name was specified just return an empty collection
		if ((name == null) || (name.equals("")))
			return devicesToReturn;

		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					null, false, name, exactMatch, null, false, null, false,
					null, false, null, false, orderByPropertyName,
					ascendingOrDescending);
			devicesToReturn = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Check for relationship init
		if (returnFullObjectGraph)
			devicesToReturn = getRealObjectsAndRelationships(devicesToReturn);

		// Now return the results
		if (devicesToReturn != null) {
			logger.debug(devicesToReturn.size() + " matchers found!");
		} else {
			logger.debug("No matches found :(.");
		}
		return devicesToReturn;
	}

	public int countFindByName(String name, boolean exactMatch)
			throws MetadataAccessException {
		int count = 0;
		// If no name was specified just return an empty collection
		if ((name == null) || (name.equals("")))
			return count;

		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					null, false, name, exactMatch, null, false, null, false,
					null, false, null, false, null, null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the count
		return count;
	}

	/**
	 * This method returns a <code>Collection</code> of <code>String</code>s
	 * that are the names of all the devices that are registered in SSDS.
	 * 
	 * @return a <code>Collection</code> of <code>String</code>s that are the
	 *         names of all devices in SSDS.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public Collection findAllNames() throws MetadataAccessException {
		Collection deviceNames = null;

		// Create the query and run it
		try {
			Query query = getSession().createQuery(
					"select distinct device.name from "
							+ "Device device order by device.name");
			deviceNames = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return deviceNames;
	}

	/**
	 * This method returns all the devices that are deployed under a given
	 * parent deployment (DataProducer). This method walks the child deployments
	 * and grabs their devices as well. It will include the device of the given
	 * deployment as well.
	 * 
	 * @param parentDataProducer
	 *            The <code>DataProducer</code> that the method will search for
	 *            devices under.
	 * @param currentOnly
	 *            is a <code>boolean</code> to indicate if you only want the
	 *            devices that are currently deployed (i.e. no end date).
	 *            <code>true</code> means you only want current, false means
	 *            return all
	 * @return a <code>Collection</code> of <code>Device</code>s that are
	 *         deployed under the given parent deployment
	 * @throws MetadataAccessException
	 */
	public Collection findAllDeviceDeployedUnderParent(
			DataProducer parentDataProducer, boolean currentOnly)
			throws MetadataAccessException {
		// This is the collection of devices to return
		Collection deployedDevices = new ArrayList();

		// Grab the DataProducer interface
		DataProducerAccessLocalHome dpalh = null;
		DataProducerAccessLocal dpal = null;
		try {
			dpalh = DataProducerAccessUtil.getLocalHome();
		} catch (NamingException e) {
			throw new MetadataAccessException(e);
		}
		if (dpalh != null) {
			try {
				dpal = dpalh.create();
			} catch (CreateException e) {
				throw new MetadataAccessException(e);
			}
		}
		// Find the equivalent DataProducer
		DataProducer realParentDataProducer = null;
		if (dpal != null) {
			realParentDataProducer = (DataProducer) dpal
					.findEquivalentPersistentObject(parentDataProducer, true);
		}
		if (realParentDataProducer != null) {
			// Grab the current device (if one)
			if (realParentDataProducer.getDevice() != null) {
				if ((!currentOnly)
						|| ((currentOnly) && (realParentDataProducer
								.getEndDate() == null)))
					deployedDevices.add(realParentDataProducer.getDevice());
			}
			// Now grab any child devices
			if ((realParentDataProducer.getChildDataProducers() != null)
					&& (realParentDataProducer.getChildDataProducers().size() > 0)) {
				Iterator childDPIter = realParentDataProducer
						.getChildDataProducers().iterator();
				while (childDPIter.hasNext()) {
					DataProducer childDP = (DataProducer) childDPIter.next();
					Collection devices = this.findAllDeviceDeployedUnderParent(
							childDP, currentOnly);
					if ((devices != null) && (devices.size() > 0)) {
						Iterator deviceIter = devices.iterator();
						while (deviceIter.hasNext()) {
							Device tempDevice = (Device) deviceIter.next();
							if (!deployedDevices.contains(tempDevice))
								deployedDevices.add(tempDevice);
						}
					}
				}
			}
		}

		// Now return the devices
		return deployedDevices;
	}

	public int countFindAllNames() throws MetadataAccessException {
		int count = 0;
		// Create the query and run it
		try {
			Long integerCount = (Long) getSession().createQuery(
					"select count(distinct device.name) from "
							+ "Device device").uniqueResult();
			if (integerCount != null)
				count = integerCount.intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		return count;
	}

	/**
	 * This method returns <code>Device</code>s that are found with the same
	 * (exact) device name, manufacturer name, model, and serial number. Not all
	 * the parameters need to be specified and only the ones specified will be
	 * used in the search.
	 * 
	 * @param name
	 *            is the name of the device to find
	 * @param mfgName
	 *            is the manufacturer's name
	 * @param mfgModel
	 *            is the model assigned by the manufacturer
	 * @param mfgSerialNumber
	 *            is the serialNumber given by the manufacturer to search for
	 * @return a <code>Collection</code> of <code>Device</code>s that have
	 *         attributes that exactly match those that were specified as query
	 *         parameters.
	 */
	public Collection findByNameAndMfgInfo(String name, boolean nameExactMatch,
			String mfgName, boolean mfgNameExactMatch, String mfgModel,
			boolean mfgModelExactMatch, String mfgSerialNumber,
			boolean mfgSerialNumberExactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// Make sure all the arguments are not null
		logger.debug("name = " + name + "\n" + "mfgName = " + mfgName + "\n"
				+ "mfgModel = " + mfgModel + "\n" + "mfgSerialNumber = "
				+ mfgSerialNumber);
		if (((name == null) || (name.equals("")))
				&& ((mfgName == null) || (mfgName.equals("")))
				&& ((mfgModel == null) || (mfgModel.equals("")))
				&& ((mfgSerialNumber == null) && (mfgSerialNumber.equals("")))) {
			return new ArrayList();
		}

		Collection matchingDevices = new ArrayList();

		try {
			Criteria criteria = this.formulatePropertyCriteria(false, null,
					null, false, name, nameExactMatch, mfgName,
					mfgNameExactMatch, mfgModel, mfgModelExactMatch,
					mfgSerialNumber, mfgSerialNumberExactMatch, null, false,
					orderByPropertyName, ascendingOrDescending);
			matchingDevices = criteria.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		if (returnFullObjectGraph)
			matchingDevices = getRealObjectsAndRelationships(matchingDevices);

		// Return the result
		return matchingDevices;
	}

	public int countFindByNameAndMfgInfo(String name, boolean nameExactMatch,
			String mfgName, boolean mfgNameExactMatch, String mfgModel,
			boolean mfgModelExactMatch, String mfgSerialNumber,
			boolean mfgSerialNumberExactMatch) throws MetadataAccessException {

		// Make sure all the arguments are not null
		logger.debug("name = " + name + "\n" + "mfgName = " + mfgName + "\n"
				+ "mfgModel = " + mfgModel + "\n" + "mfgSerialNumber = "
				+ mfgSerialNumber);
		if (((name == null) || (name.equals("")))
				&& ((mfgName == null) || (mfgName.equals("")))
				&& ((mfgModel == null) || (mfgModel.equals("")))
				&& ((mfgSerialNumber == null) && (mfgSerialNumber.equals("")))) {
			return 0;
		}

		int count = 0;

		try {
			Criteria criteria = this.formulatePropertyCriteria(true, null,
					null, false, name, nameExactMatch, mfgName,
					mfgNameExactMatch, mfgModel, mfgModelExactMatch,
					mfgSerialNumber, mfgSerialNumberExactMatch, null, false,
					null, null);
			count = ((Long) criteria.uniqueResult()).intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the result
		return count;
	}

	/**
	 * This method returns all <code>Device</code>s that are linked (normally
	 * means owned) by a <code>Person</code>.
	 * 
	 * @param person
	 *            is the <code>Person</code> that will be used to search for
	 *            devices.
	 * @return a <code>Collection</code> of devices that are linked to that
	 *         person.
	 */
	public Collection findByPerson(Person person, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// The collection to return
		Collection devices = new ArrayList();

		// First validate the incoming person
		if (person == null) {
			return devices;
		}

		// First make sure the person exists
		PersonDAO personDAO = new PersonDAO(getSession());

		// Try to find the equivalent persistent person
		Long personId = personDAO.findId(person);
		if (personId == null) {
			throw new MetadataAccessException(
					"No person was found in the data store"
							+ " that matched the specified person");
		}

		// Construct query and run it
		StringBuffer queryStringBuffer = new StringBuffer();
		queryStringBuffer.append("from Device device ");
		queryStringBuffer.append("where device.person.id = :personId ");
		// Add order by clause
		if (this.checkIfPropertyOK(orderByPropertyName)) {
			queryStringBuffer.append(this.getOrderByPropertyNameSQLClause(
					orderByPropertyName, ascendingOrDescending));
		}
		try {
			Query query = getSession()
					.createQuery(queryStringBuffer.toString());
			query.setString("personId", personId.toString());
			logger.debug("Compiled query = " + query.getQueryString());
			devices = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Check relationship init
		if (returnFullObjectGraph)
			devices = getRealObjectsAndRelationships(devices);

		// Now return the real objects
		return devices;
	}

	public int countFindByPerson(Person person) throws MetadataAccessException {
		int count = 0;
		// TODO implement this
		return count;
	}

	/**
	 * This method returns all <code>Device</code>s that are of a certain
	 * <code>DeviceType</code>
	 * 
	 * @param deviceType
	 *            is the <code>DeviceType</code> that will be used to search for
	 *            devices.
	 * @return a <code>Collection</code> of devices that are categorized by the
	 *         given <code>DeviceType</code>
	 */
	public Collection findByDeviceType(DeviceType deviceType,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// The collection to return
		Collection devices = new ArrayList();

		// First validate the incoming deviceType
		if (deviceType == null) {
			return devices;
		}

		// Get the DeviceTypeDAO and check the existing of the supplied
		// DeviceType
		DeviceTypeDAO deviceTypeDAO = new DeviceTypeDAO(getSession());

		// Try to find the equivalent persistent deviceType
		Long deviceTypeId = deviceTypeDAO.findId(deviceType);
		if (deviceTypeId == null) {
			throw new MetadataAccessException(
					"No DeviceType was found in the data store"
							+ " that matched the specified DeviceType");
		}

		// Construct the query and run it
		StringBuffer queryStringBuffer = new StringBuffer();
		queryStringBuffer.append("from Device device ");
		queryStringBuffer.append("where device.deviceType.id = :deviceTypeId ");
		// Add order by clause
		if (this.checkIfPropertyOK(orderByPropertyName)) {
			queryStringBuffer.append(this.getOrderByPropertyNameSQLClause(
					orderByPropertyName, ascendingOrDescending));
		}
		try {
			Query query = getSession()
					.createQuery(queryStringBuffer.toString());
			query.setString("deviceTypeId", deviceTypeId.toString());
			logger.debug("Compiled query = " + query.getQueryString());
			devices = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		if (returnFullObjectGraph)
			devices = getRealObjectsAndRelationships(devices);

		// Now return the real objects
		return devices;
	}

	public int countFindByDeviceType(DeviceType deviceType)
			throws MetadataAccessException {
		int count = 0;
		// TODO implement this
		return count;
	}

	/**
	 * This method returns a distinct list of Manufacturer names.
	 * 
	 * @return A <code>Collection</code> of <code>Strings</code> that is the
	 *         list of available Manufacturer Names
	 * @throws MetadataAccessException
	 *             if something goes haywire.
	 */
	public Collection findAllManufacturerNames() throws MetadataAccessException {
		// The Collection to return
		Collection mfgNames = null;

		// Construct the query
		try {
			Query query = getSession().createQuery(
					"select distinct device.mfgName from "
							+ "Device device order by device.mfgName");
			mfgNames = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}
		// Return the results
		return mfgNames;
	}

	public int countFindAllManufacturerNames() throws MetadataAccessException {
		int count = 0;
		// TODO implement this
		return count;
	}

	/**
	 * This method will return all the models that a certain manufacturer builds
	 * (or at least the ones that were previously defined in SSDS).
	 * 
	 * @param mfgName
	 *            is the name of the manufacturer to search for. This must be an
	 *            exact match
	 * @return a <code>Collection</code> of <code>String</code>s that are all
	 *         the models available from the given manufacturer name
	 * @throws MetadataAccessException
	 *             if something goes haywire.
	 */
	public Collection findMfgModels(String mfgName)
			throws MetadataAccessException {

		// The collection to return
		Collection mfgModels = new ArrayList();

		if ((mfgName == null) || (mfgName.equals("")))
			return mfgModels;

		// Construct the query
		try {
			Query query = getSession()
					.createQuery(
							"select distinct device.mfgModel from "
									+ "Device device where device.mfgName = :mfgName order by device.mfgModel");
			query.setString("mfgName", mfgName);
			mfgModels = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Now return the results
		return mfgModels;
	}

	public int countFindMfgModels(String mfgName)
			throws MetadataAccessException {
		int count = 0;
		// TODO implement this
		return count;
	}

	/**
	 * This method returns a <code>Collection</code> of serial numbers that are
	 * match the manufacturer and the model number. The manufacturer must be
	 * specified, but the model is optional, but will help narrow the return.
	 * 
	 * @param mfgName
	 *            is the name of the manufacturer to search for. If this is not
	 *            supplied, and empty collection will be returned.
	 * @param mfgModel
	 *            is the model to search for (if this is null, all serial
	 *            numbers for the matching manufacturer will be returned).
	 * @return a <code>Collection</code> of <code>String</code>s that are all
	 *         the serial numbers for the devices that matched the manufacturer
	 *         and the model (if available).
	 */
	public Collection findMfgSerialNumbers(String mfgName, String mfgModel)
			throws MetadataAccessException {

		// Create empty array list for the return in case something goes wrong.
		Collection mfgSerialNumbers = new ArrayList();

		// Create a query buffer
		StringBuffer queryBuffer = new StringBuffer();

		// If no manufacturer name was supplied, just return empty
		if (mfgName == null)
			return mfgSerialNumbers;

		// Build the query string
		queryBuffer
				.append("select distinct device.mfgSerialNumber from Device device where device.mfgName = :mfgName ");
		if ((mfgModel != null) && (!mfgModel.equals(""))) {
			queryBuffer.append("and device.mfgModel = :mfgModel ");
		}
		queryBuffer.append("order by device.mfgSerialNumber");
		// Construct the query
		try {
			Query query = getSession().createQuery(queryBuffer.toString());
			query.setString("mfgName", mfgName);
			if ((mfgModel != null) && (!mfgModel.equals(""))) {
				query.setString("mfgModel", mfgModel);
			}
			mfgSerialNumbers = query.list();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Now return the collectin
		return mfgSerialNumbers;
	}

	public int countFindAllMfgSerialNumbers(String mfgName, String mfgModel)
			throws MetadataAccessException {
		int count = 0;
		// TODO implement this
		return count;
	}

	public Collection findBySearchingFields(String id, String uuid,
			String name, String description, String mfgModel, String mfgName,
			String mfgSerialNumber, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// The Collection to return
		Collection devices = new ArrayList();

		// First check to see if all fields are empty, if they are, just return
		// the empty collection
		if (isStringEmpty(id) && isStringEmpty(uuid) && isStringEmpty(name)
				&& isStringEmpty(description) && isStringEmpty(mfgModel)
				&& isStringEmpty(mfgName) && isStringEmpty(mfgSerialNumber))
			return devices;

		// Since something was specified, formulate the query
		StringBuffer queryBuffer = new StringBuffer();
		queryBuffer.append("from Device device where ");

		// A flag to mark if a clause was specified as the first one
		boolean firstClauseSpecified = false;

		// Check for an ID
		if (!isStringEmpty(id)) {
			queryBuffer.append("device.id = :idSearchTerm");
			firstClauseSpecified = true;
		}
		// Add the UUID clause if specified
		if (!isStringEmpty(uuid)) {
			if (firstClauseSpecified)
				queryBuffer.append(" and ");
			queryBuffer.append("device.uuid like :uuidSearchTerm");
			firstClauseSpecified = true;
		}
		// Add the name clause if specified
		if (!isStringEmpty(name)) {
			if (firstClauseSpecified)
				queryBuffer.append(" and ");
			queryBuffer.append("device.name like :nameSearchTerm");
			firstClauseSpecified = true;
		}
		// Add the description clause if specified
		if (!isStringEmpty(description)) {
			if (firstClauseSpecified)
				queryBuffer.append(" and ");
			queryBuffer
					.append("device.description like :descriptionSearchTerm");
			firstClauseSpecified = true;
		}
		// Add the manufacturer's model if specified
		if (!isStringEmpty(mfgModel)) {
			if (firstClauseSpecified)
				queryBuffer.append(" and ");
			queryBuffer.append("device.mfgModel like :mfgModelSearchTerm");
			firstClauseSpecified = true;

		}
		// Add the manufacturer's name if specified
		if (!isStringEmpty(mfgName)) {
			if (firstClauseSpecified)
				queryBuffer.append(" and ");
			queryBuffer.append("device.mfgName like :mfgNameSearchTerm");
			firstClauseSpecified = true;

		}
		// Add the serial number if specified
		if (!isStringEmpty(mfgSerialNumber)) {
			if (firstClauseSpecified)
				queryBuffer.append(" and ");
			queryBuffer
					.append("device.mfgSerialNumber like :mfgSerialNumberSearchTerm");
			firstClauseSpecified = true;

		}

		// Check for ordering criteria
		if (this.checkIfPropertyOK(orderByPropertyName)) {
			queryBuffer.append(this.getOrderByPropertyNameSQLClause(
					orderByPropertyName, ascendingOrDescending));
		}
		try {
			Query query = getSession().createQuery(queryBuffer.toString());
			if (!isStringEmpty(id))
				query.setString("idSearchTerm", id);
			if (!isStringEmpty(uuid))
				query.setString("uuidSearchTerm", "%" + uuid + "%");
			if (!isStringEmpty(name))
				query.setString("nameSearchTerm", "%" + name + "%");
			if (!isStringEmpty(description))
				query.setString("descriptionSearchTerm", "%" + description
						+ "%");
			if (!isStringEmpty(mfgModel))
				query.setString("mfgModelSearchTerm", "%" + mfgModel + "%");
			if (!isStringEmpty(mfgName))
				query.setString("mfgNameSearchTerm", "%" + mfgName + "%");
			if (!isStringEmpty(mfgSerialNumber))
				query.setString("mfgSerialNumberSearchTerm", "%"
						+ mfgSerialNumber + "%");
			logger.debug("Query string = " + query.getQueryString());
			devices = query.list();
			logger
					.debug("Found " + devices.size()
							+ " devices from that query");
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		if (returnFullObjectGraph)
			devices = getRealObjectsAndRelationships(devices);

		return devices;
	}

	/**
	 * This method will take the search term supplied and return all devices
	 * that have that term in any of the fields or in the device type name
	 * 
	 * @param searchTerm
	 * @param orderByPropertyName
	 * @param ascendingOrDescending
	 * @param returnFullObjectGraph
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findBySearchingAllFieldsAndType(String searchTerm,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// The Collection to return
		Collection devices = new ArrayList();

		// Formulate the query
		StringBuffer queryBuffer = new StringBuffer();
		queryBuffer.append("from Device device where "
				+ "device.uuid like :searchTerm or "
				+ "device.name like :searchTerm or "
				+ "device.description like :searchTerm or "
				+ "device.mfgModel like :searchTerm or "
				+ "device.mfgName like :searchTerm or "
				+ "device.mfgSerialNumber like :searchTerm or "
				+ "device.infoUrlList like :searchTerm or "
				+ "device.deviceType.name like :searchTerm or "
				+ "device.person.surname like :searchTerm or "
				+ "device.person.firstname like :searchTerm or "
				+ "device.person.email like :searchTerm or "
				+ "device.person.username like :searchTerm or "
				+ "device.person.organization like :searchTerm");

		// Check for ordering criteria
		if (this.checkIfPropertyOK(orderByPropertyName)) {
			queryBuffer.append(this.getOrderByPropertyNameSQLClause(
					orderByPropertyName, ascendingOrDescending));
		}
		try {
			Query query = getSession().createQuery(queryBuffer.toString());
			query.setString("searchTerm", "%" + searchTerm + "%");
			logger.debug("Query string = " + query.getQueryString());
			devices = query.list();
			logger
					.debug("Found " + devices.size()
							+ " devices from that query");
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		if (returnFullObjectGraph)
			devices = getRealObjectsAndRelationships(devices);

		return devices;
	}

	public int countFindBySearchingAllFieldsAndType(String searchTerm)
			throws MetadataAccessException {
		int count = 0;
		// TODO implement this
		return count;
	}

	public Collection findByResource(Resource resource,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// The results to return
		Collection results = new ArrayList();

		// If the resource is null return null
		if (resource == null)
			return results;

		// First make sure the resource exists
		ResourceDAO resourceDAO = new ResourceDAO(getSession());

		Resource persistentResource = null;
		persistentResource = (Resource) resourceDAO
				.findEquivalentPersistentObject(resource, false);

		if (persistentResource == null)
			throw new MetadataAccessException(
					"A matching resource could not be found in the system");

		// The query string
		StringBuffer sqlStringBuffer = new StringBuffer();

		// Now create the query
		Query query = null;

		sqlStringBuffer.append("select distinct device from "
				+ "Device device, Resource resource where");
		sqlStringBuffer.append(" resource.id = :resourceID and ");
		sqlStringBuffer.append(" resource in elements(device.resources)");

		if ((orderByPropertyName != null)
				&& (checkIfPropertyOK(orderByPropertyName))) {
			sqlStringBuffer.append(" order by device." + orderByPropertyName);
			if ((ascendingOrDescending != null)
					&& ((ascendingOrDescending
							.equals(MetadataDAO.ASCENDING_ORDER)) || (ascendingOrDescending
							.equals(MetadataDAO.DESCENDING_ORDER)))) {
				sqlStringBuffer.append(" " + ascendingOrDescending);
			}
		}
		try {
			query = this.getSession().createQuery(sqlStringBuffer.toString());
			query.setLong("resourceID", persistentResource.getId().longValue());
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		results = query.list();

		if (returnFullObjectGraph)
			results = getRealObjectsAndRelationships(results);

		return results;
	}

	public int countFindByResource(Resource resource) {
		int count = 0;
		// TODO implement this
		return count;
	}

	public Device findByRecordVariable(RecordVariable recordVariable,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// The device to return
		Device results = null;

		// If the recordVariable is null return null
		if (recordVariable == null)
			return results;

		// First make sure the resource exists
		RecordVariableDAO recordVariableDAO = new RecordVariableDAO(
				getSession());

		RecordVariable persistentRecordVariable = null;
		persistentRecordVariable = (RecordVariable) recordVariableDAO
				.findEquivalentPersistentObject(recordVariable, false);

		if (persistentRecordVariable == null)
			throw new MetadataAccessException(
					"A matching recordVariable could not be found in the system");

		// The query string
		StringBuffer sqlStringBuffer = new StringBuffer();

		// Now create the query
		Query query = null;

		sqlStringBuffer.append("select distinct device from "
				+ "Device device, " + "DataProducer dataProducer, "
				+ "DataContainer dataContainer, "
				+ "RecordDescription recordDescription, "
				+ "RecordVariable recordVariable where "
				+ "device.id = dataProducer.device.id and "
				+ "dataProducer.outputs.id = dataContainer.id and "
				+ "dataContainer.recordDescription.recordVariables.id = "
				+ persistentRecordVariable.getId().toString());

		try {
			query = this.getSession().createQuery(sqlStringBuffer.toString());
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		results = (Device) query.uniqueResult();

		if (returnFullObjectGraph)
			results = (Device) getRealObjectAndRelationships(results);

		return results;
	}

	/**
	 * @see IMetadataDAO#makePersistent(IMetadataObject)
	 */
	public Long makePersistent(IMetadataObject metadataObject)
			throws MetadataAccessException {

		logger.debug("makePersistent called");
		// This is a flag to indicate if the device has been persisted in the
		// past
		boolean persistedBefore = false;

		// Check incoming object
		Device device = this.checkIncomingMetadataObject(metadataObject);
		logger.debug("Incoming device that is source for make persistent is "
				+ device.toStringRepresentation("|"));

		// Look for a matching device that is in the persistent store
		Device persistentDevice = (Device) this.findEquivalentPersistentObject(
				device, false);

		// This is the device that will actually get persisted
		Device deviceToPersist = null;

		// If there is an existing device, copy over any changed non-null fields
		// from the incoming object to persist any real changes
		if (persistentDevice != null) {
			logger.debug("The search for existing device returned: "
					+ persistentDevice.toStringRepresentation("|"));
			// This is a bit touchy, but if the persistentDevice has a UUID,
			// copy that to the incoming one so that it does not get changed
			if (persistentDevice.getUuid() != null) {
				try {
					device.setUuid(persistentDevice.getUuid());
				} catch (MetadataException e) {
				}
			}
			String deviceStringBefore = persistentDevice
					.toStringRepresentation("<li>");
			if (this.updateDestinationObject(device, persistentDevice)) {
				addMessage(ssdsAdminEmailToAddress,
						"A device was updated in SSDS<br><b>Before:</b><br><ul><li>"
								+ deviceStringBefore
								+ "</ul><br><b>After:</b><br><ul><li>"
								+ persistentDevice
										.toStringRepresentation("<li>")
								+ "</ul><br>");
				if ((sendUserMessages)
						&& (persistentDevice.getPerson() != null)
						&& (persistentDevice.getPerson().getEmail() != null)) {
					addMessage(
							persistentDevice.getPerson().getEmail(),
							"A device that is associated with your email was "
									+ "updated in SSDS<br><b>Before:</b><br><ul><li>"
									+ deviceStringBefore
									+ "</ul><br><b>After:</b><br><ul><li>"
									+ persistentDevice
											.toStringRepresentation("<li>")
									+ "</ul><br>");
				}
			}

			// Set the flag to indicate that the device has been persisted in
			// the past
			persistedBefore = true;

			// Set the object to persist to the previously persisted one
			deviceToPersist = persistentDevice;

		} else {
			// Since this will be a new device, let's take care of a couple of
			// things. First clear the ID
			device.setId(null);

			// Now check to make sure the UUID is real
			if ((device.getUuid() == null) || (device.getUuid().equals(""))) {
				logger
						.debug("The incoming device had no UUID, one will be created");
				try {
					device.setUuid(this.generateUUID());
				} catch (MetadataException e) {
					logger
							.error("Could not set the newly create UUID on the incoming device");
				}
				addMessage(
						ssdsAdminEmailToAddress,
						"A Device that was stored in SSDS had no UUID associated "
								+ "with it so one was assigned dynamically:<br><ul><li>"
								+ device.toStringRepresentation("<li>")
								+ "</ul><br>");
				if (sendUserMessages) {
					if ((device.getPerson() != null)
							&& (device.getPerson().getEmail() != null))
						addMessage(
								device.getPerson().getEmail(),
								"A device associated with your email address was "
										+ "entered into the Shore-Side Data System, but it had no "
										+ "associated UUID, so SSDS created one dynamically.  "
										+ "The device information is:<br><ul><li>"
										+ device.toStringRepresentation("<li>")
										+ "</ul><br>");
				}
			}

			// Set the flag to indicate that this has not been persisted in the
			// past
			persistedBefore = false;

			// Assign the device to persist to the incoming (new) one
			deviceToPersist = device;
		}

		// -------------------
		// Person Relationship
		// -------------------
		// First see if there is a person associated with the incoming device
		if (device.getPerson() != null) {
			// Now, since there is, check to see if it has been initialized
			if (Hibernate.isInitialized(device.getPerson())) {
				// Grab the Person DAO to handle that relationship
				PersonDAO personDAO = new PersonDAO(this.getSession());

				// Now persist the person
				Person tempPerson = device.getPerson();
				personDAO.makePersistent(tempPerson);

				// The matching person that is in the session
				Person tempPersonInSession = null;

				// Check to see if the persisted person is in the session
				if (!getSession().contains(tempPerson)) {
					tempPersonInSession = (Person) personDAO
							.findEquivalentPersistentObject(tempPerson, false);
				} else {
					tempPersonInSession = tempPerson;
				}

				// Now check to see if the device was persisted in the past, if
				// so, just check to see if devices person is different and
				// update it if so
				if (persistedBefore) {
					if ((deviceToPersist.getPerson() == null)
							|| (!deviceToPersist.getPerson().equals(
									tempPersonInSession))) {
						deviceToPersist.setPerson(tempPersonInSession);
					}
				} else {
					// Make sure the person associated with the device is the
					// session, if not replace it with the one that is
					if (!getSession().contains(deviceToPersist.getPerson())) {
						deviceToPersist.setPerson(tempPersonInSession);
					}
				}
			}
		}

		// -------------------------
		// DeviceType Relationship
		// -------------------------
		// First see if there is a deviceType associated with the incoming
		// device
		if (device.getDeviceType() != null) {
			// Now, since there is, check to see if it has been initialized
			if (Hibernate.isInitialized(device.getDeviceType())) {
				// Grab the DeviceType DAO to handle that relationship
				DeviceTypeDAO deviceTypeDAO = new DeviceTypeDAO(this
						.getSession());

				// Now persist the deviceType
				DeviceType tempDeviceType = device.getDeviceType();
				deviceTypeDAO.makePersistent(tempDeviceType);

				// The matching deviceType that is in the session
				DeviceType tempDeviceTypeInSession = null;

				// Check to see if the persisted deviceType is in the session
				if (!getSession().contains(tempDeviceType)) {
					tempDeviceTypeInSession = (DeviceType) deviceTypeDAO
							.findEquivalentPersistentObject(tempDeviceType,
									false);
				} else {
					tempDeviceTypeInSession = tempDeviceType;
				}

				// Now check to see if the device was persisted in the past, if
				// so, just check to see if device's deviceType is different and
				// update it if so
				if (persistedBefore) {
					if ((deviceToPersist.getDeviceType() == null)
							|| (!deviceToPersist.getDeviceType().equals(
									tempDeviceTypeInSession))) {
						deviceToPersist.setDeviceType(tempDeviceTypeInSession);
					}
				} else {
					// Make sure the deviceType associated with the device is
					// the
					// session, if not replace it with the one that is
					if (!getSession().contains(deviceToPersist.getDeviceType())) {
						deviceToPersist.setDeviceType(tempDeviceTypeInSession);
					}
				}
			}
		}

		// ---------------------
		// Resource Relationship
		// ----------------------
		// First make sure the resources relationship exists
		if (device.getResources() != null) {
			// Now check to make sure it is initialized. If it is not, no
			// changes have taken place and we don't need to do anything
			if (Hibernate.isInitialized(device.getResources())) {

				// Grab the DAO for Resource
				ResourceDAO resourceDAO = new ResourceDAO(this.getSession());

				// Make sure the are resources to iterate over
				if (device.getResources().size() > 0) {

					// Now iterate over the Resources and persist them
					Iterator userGroupIter = device.getResources().iterator();
					while (userGroupIter.hasNext()) {
						Resource tempResource = (Resource) userGroupIter.next();
						resourceDAO.makePersistent(tempResource);
					}
				}

				// Create a copy of the collection associated with the device to
				// prevent concurrent modifications
				Collection deviceResourceCopy = new ArrayList(device
						.getResources());

				// Now we need to make the correct associations. Currently, you
				// have a collection of Resource objects that have their values
				// marked for persistence. Now the object will either be in the
				// session or not depending on if they were previously
				// persisted.
				Iterator deviceResourceCopyIterator = deviceResourceCopy
						.iterator();
				while (deviceResourceCopyIterator.hasNext()) {
					Resource currentResource = (Resource) deviceResourceCopyIterator
							.next();
					Resource currentResourceInSession = null;
					// Is this Resource already in the session?
					if (!getSession().contains(currentResource)) {
						// No, so grab the one that is
						currentResourceInSession = (Resource) resourceDAO
								.findEquivalentPersistentObject(
										currentResource, false);
					} else {
						currentResourceInSession = currentResource;
					}
					// Now if the parent device was persisted before, just
					// check to make sure the sessioned Resources is in the
					// collection are associated with the device that will be
					// persisted
					if (persistedBefore) {
						if (!deviceToPersist.getResources().contains(
								currentResourceInSession))
							deviceToPersist.getResources().add(
									currentResourceInSession);
					} else {
						// This means that the device has not been persisted
						// before. If the Resource is already in the session,
						// there is nothing to do, but if not, we need to
						// replace it with the sessioned one
						if (!getSession().contains(currentResource)) {
							deviceToPersist.getResources().remove(
									currentResource);
							deviceToPersist.getResources().add(
									currentResourceInSession);
						}
					}
				}
			}
		}

		// If not persisted before, call save on the new object
		if (!persistedBefore) {
			getSession().save(deviceToPersist);
			addMessage(ssdsAdminEmailToAddress,
					"A new device was entered in the system<br><ul><li>"
							+ deviceToPersist.toStringRepresentation("<li>")
							+ "</ul><br>");
			if ((sendUserMessages) && (deviceToPersist.getPerson() != null)
					&& (deviceToPersist.getPerson().getEmail() != null)) {
				addMessage(deviceToPersist.getPerson().getEmail(),
						"A new device that was associated with your "
								+ "email address was entered in the "
								+ "Shore Side Data System<br><ul><li>"
								+ deviceToPersist
										.toStringRepresentation("<li>")
								+ "</ul><br>");
			}
		}

		// Now return the ID
		if (deviceToPersist != null) {
			return deviceToPersist.getId();
		} else {
			return null;
		}
	}

	/**
	 * @see IMetadataDAO#makeTransient(IMetadataObject)
	 */
	public void makeTransient(IMetadataObject metadataObject)
			throws MetadataAccessException {
		logger.debug("makeTransient called");

		// Check incoming object
		Device device = this.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		Device persistentDevice = (Device) this.findEquivalentPersistentObject(
				device, false);

		// If no matching device was found, do nothing
		if (persistentDevice == null) {
			logger
					.debug("No matching device could be found in the persistent store, "
							+ "no delete performed");
		} else {
			// Handle the relationships
			Person persistentPersonForEmail = persistentDevice.getPerson();
			persistentDevice.setPerson(null);
			persistentDevice.setDeviceType(null);
			persistentDevice.clearResources();

			// Find all the DataProducers who use the device and clear the
			// relationship
			DataProducerDAO dpDAO = new DataProducerDAO(getSession());
			Collection associatedDPs = dpDAO.findByDevice(persistentDevice,
					null, null, true);
			if (associatedDPs != null) {
				Iterator associatedDPsIter = associatedDPs.iterator();
				while (associatedDPsIter.hasNext()) {
					DataProducer tempDP = (DataProducer) associatedDPsIter
							.next();
					tempDP.setDevice(null);
				}
			}

			logger
					.debug("Existing object was found, so we will try to delete it");
			try {
				getSession().delete(persistentDevice);
				addMessage(ssdsAdminEmailToAddress,
						"A Device was removed from SSDS:<br><ul><li>"
								+ persistentDevice
										.toStringRepresentation("<li>")
								+ "</ul><br>");
				if ((sendUserMessages) && (persistentPersonForEmail != null)
						&& (persistentPersonForEmail.getEmail() != null)) {
					addMessage(
							persistentPersonForEmail.getEmail(),
							"A device that was associated with your email address "
									+ "was removed from the Shore-Side Data System:<br><ul><li>"
									+ persistentDevice
											.toStringRepresentation("<li>")
									+ "</ul><br>");
				}
			} catch (HibernateException e) {
				logger.error("HibernateException caught (will be re-thrown):"
						+ e.getMessage());
				throw new MetadataAccessException(e);
			}
		}
	}

	/**
	 * This is just a helper method to see if the supplied string is
	 * <code>null</code> or empty
	 * 
	 * @param stringToCheck
	 * @return <code>true</code> if stringToCheck is null or equal &quot;&quot;
	 */
	private boolean isStringEmpty(String stringToCheck) {
		if ((stringToCheck == null) || stringToCheck.equals("")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method checks to make sure an incoming <code>MetadataObject</code>
	 * is not null and is in fact of the correct class. It then converts it to
	 * the correct class and returns it
	 * 
	 * @param metadataObject
	 *            the <code>MetadataObject</code> to check and return as a
	 *            <code>Device</code>
	 * @return a <code>Device</code> that is same object that came in
	 * @throws MetadataAccessException
	 *             if something is wrong
	 */
	private Device checkIncomingMetadataObject(IMetadataObject metadataObject)
			throws MetadataAccessException {

		// Check for null argument
		if (metadataObject == null) {
			throw new MetadataAccessException(
					"Failed: incoming Device was null");
		}

		// Try to cast the incoming object into the correct class
		Device device = null;
		try {
			device = (Device) metadataObject;
		} catch (ClassCastException cce) {
			throw new MetadataAccessException(
					"Could not cast the incoming object into a Device");
		}
		return device;
	}

	/**
	 * This method creates a UUID that can be used as a unique identifier
	 * 
	 * @return the UUID in string form, null if one could not be created
	 */
	private String generateUUID() {
		// Create the string that will be used to return the UUID as a String
		String uuidStringToReturn = null;

		// The UUID that will be used
		UUID uuid = null;

		// The UUID generator
		UUIDGenerator uuidg = UUIDGenerator.getInstance();

		// Try to see if the ethernet address is in the properties file
		String ethernetAddress = this.daoProperties
				.getProperty("metadata.dao.host.ethernet.address");

		// Create the EthernetAddress pointer
		EthernetAddress ethAddr = null;

		// If it was specified as a property, try to use that
		if ((ethernetAddress != null) && (!ethernetAddress.equals(""))) {
			try {
				ethAddr = EthernetAddress.valueOf(ethernetAddress);
				uuid = uuidg.generateTimeBasedUUID(ethAddr);
			} catch (NumberFormatException e) {
				logger.error("Could not generate a UUID with ethernet address "
						+ ethernetAddress + ": " + e.getMessage());
			} catch (Exception e) {
				logger.error("Could not generate a UUID with ethernet address "
						+ ethernetAddress + ": " + e.getMessage());
			}
		}

		// If that did not work, use a time based one (pretty good)
		if (uuid == null) {
			uuid = uuidg.generateTimeBasedUUID();
		}

		// Now return the string representation of that
		if (uuid != null)
			uuidStringToReturn = uuid.toString();

		return uuidStringToReturn;
	}

	private Criteria formulatePropertyCriteria(boolean countQuery, Long id,
			String uuid, boolean exactUUIDMatch, String name,
			boolean exactNameMatch, String mfgName, boolean mfgNameExactMatch,
			String mfgModel, boolean mfgModelExactMatch,
			String mfgSerialNumber, boolean mfgSerialNumberExactMatch,
			String infoUrlList, boolean infoURLListExactMatch,
			String orderByProperty, String ascendOrDescend)
			throws MetadataAccessException {
		// The Criteria to return
		Criteria criteria = getSession().createCriteria(Device.class);
		// Make it distinct
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		// Now build the Criteria
		if (id != null) {
			criteria.add(Restrictions.eq("id", id));
		} else {
			if ((name != null) && (!name.equals(""))) {
				if (exactNameMatch) {
					criteria.add(Restrictions.eq("name", name));
				} else {
					criteria.add(Restrictions.like("name", "%" + name + "%"));
				}
			}
			if ((uuid != null) && (!uuid.equals(""))) {
				if (exactUUIDMatch) {
					criteria.add(Restrictions.eq("uuid", uuid));
				} else {
					criteria.add(Restrictions.like("uuid", "%" + uuid + "%"));
				}
			}
			if ((mfgName != null) && (!mfgName.equals(""))) {
				if (mfgNameExactMatch) {
					criteria.add(Restrictions.eq("mfgName", mfgName));
				} else {
					criteria.add(Restrictions.like("mfgName", "%" + mfgName
							+ "%"));
				}
			}
			if ((mfgModel != null) && (!mfgModel.equals(""))) {
				if (mfgModelExactMatch) {
					criteria.add(Restrictions.eq("mfgModel", mfgModel));
				} else {
					criteria.add(Restrictions.like("mfgModel", "%" + mfgModel
							+ "%"));
				}
			}
			if ((mfgSerialNumber != null) && (!mfgSerialNumber.equals(""))) {
				if (mfgSerialNumberExactMatch) {
					criteria.add(Restrictions.eq("mfgSerialNumber",
							mfgSerialNumber));
				} else {
					criteria.add(Restrictions.like("mfgSerialNumber", "%"
							+ mfgSerialNumber + "%"));
				}
			}
			if ((infoUrlList != null) && (!infoUrlList.equals(""))) {
				if (infoURLListExactMatch) {
					criteria.add(Restrictions.eq("infoURLList", infoUrlList));
				} else {
					criteria.add(Restrictions.like("infoUrlList", "%"
							+ infoUrlList + "%"));
				}
			}
		}
		// Setup if a count query, if not add fetching and ordering
		if (countQuery) {
			criteria.setProjection(Projections.rowCount());
		} else {
			addOrderByCriteria(criteria, orderByProperty, ascendOrDescend);
		}
		// Now return the Criteria
		return criteria;
	}

	/**
	 * This method takes in a <code>Device</code> and initializes (loads) its
	 * relationships
	 * 
	 * @param device
	 */
	// protected void initializeRelationships(IMetadataObject metadataObject)
	// throws MetadataAccessException {
	//
	// Device device = (Device) this
	// .checkIncomingMetadataObject(metadataObject);
	// // See if person exists
	// if (device.getPerson() != null) {
	// logger.debug("Person was found and will be initialized");
	// Person person = device.getPerson();
	// Hibernate.initialize(person);
	// logger.debug("OK, initialized");
	// if (person instanceof HibernateProxy) {
	// logger.debug("An attempt will be made to "
	// + "replace the proxy with the real person");
	// device.setPerson((Person) ((HibernateProxy) person)
	// .getHibernateLazyInitializer().getImplementation());
	// }
	// }
	// if (device.getPerson() != null
	// && device.getPerson().getUserGroups() != null)
	// Hibernate.initialize(device.getPerson().getUserGroups());
	// if (device.getDeviceType() != null)
	// Hibernate.initialize(device.getDeviceType());
	// if ((device.getResources() != null)
	// && (device.getResources().size() > 0)) {
	// Iterator resourceIter = device.getResources().iterator();
	// while (resourceIter.hasNext())
	// Hibernate.initialize((Resource) resourceIter.next());
	// }
	// }

	/**
	 * The Log4J Logger
	 */
	static Logger logger = Logger.getLogger(DeviceDAO.class);
}
