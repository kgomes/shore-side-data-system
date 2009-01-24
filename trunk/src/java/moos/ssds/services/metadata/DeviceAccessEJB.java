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
package moos.ssds.services.metadata;

import java.util.Collection;
import java.util.Properties;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import moos.ssds.dao.DeviceDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.DeviceType;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.RecordVariable;

import org.apache.log4j.Logger;
import org.doomdark.uuid.UUID;
import org.hibernate.SessionFactory;

/**
 * Provides a facade that provides client services for Device objects.
 * 
 * @ejb.bean name="DeviceAccess" type="Stateless"
 *           jndi-name="moos/ssds/services/metadata/DeviceAccess"
 *           local-jndi-name="moos/ssds/services/metadata/DeviceAccessLocal"
 *           view-type="both" transaction-type="Container"
 * @ejb.home create="true"
 *           local-class="moos.ssds.services.metadata.DeviceAccessLocalHome"
 *           remote-class="moos.ssds.services.metadata.DeviceAccessHome"
 *           extends="javax.ejb.EJBHome"
 * @ejb.interface create="true"
 *                local-class="moos.ssds.services.metadata.DeviceAccessLocal"
 *                local-extends="javax.ejb.EJBLocalObject,moos.ssds.services.metadata.IMetadataAccess"
 *                remote-class="moos.ssds.services.metadata.DeviceAccess"
 *                extends="javax.ejb.EJBObject,moos.ssds.services.metadata.IMetadataAccessRemote"
 * @ejb.util generate="physical"
 * @soap.service urn="DeviceAccess" scope="Request"
 * @axis.service urn="DeviceAccess" scope="Request"
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.21 $
 */
public class DeviceAccessEJB extends AccessBean implements IMetadataAccess {

	/**
	 * This is the ejb callback that the container calls when the EJB is first
	 * created. In this case it sets up the Hibernate session factory and sets
	 * the class that is associate with the bean
	 * 
	 * @throws CreateException
	 */
	public void ejbCreate() throws CreateException {
		logger.debug("ejbCreate called");
		logger.debug("Going to read in the properties");
		servicesMetadataProperties = new Properties();
		try {
			servicesMetadataProperties
					.load(this
							.getClass()
							.getResourceAsStream(
									"/moos/ssds/services/metadata/servicesMetadata.properties"));
		} catch (Exception e) {
			logger.error("Exception trying to read in properties file: "
					+ e.getMessage());
		}

		// Make sure the properties were read from the JAR OK
		if (servicesMetadataProperties != null) {
			logger.debug("Loaded props OK");
		} else {
			logger.warn("Could not load the servicesMetadata.properties.");
		}

		// Now create the intial context for looking up the hibernate session
		// factory and look up the session factory
		try {
			InitialContext initialContext = new InitialContext();
			sessionFactory = (SessionFactory) initialContext
					.lookup(servicesMetadataProperties
							.getProperty("metadata.hibernate.jndi.name"));
		} catch (NamingException e) {
			logger
					.error("NamingException caught when trying to get hibernate's "
							+ "SessionFactory from JNDI: " + e.getMessage());
		}

		// Now set the super persistent class to Device
		super.setPersistentClass(Device.class);
		// And the DAO
		super.setDaoClass(DeviceDAO.class);
	}

	/**
	 * This method looks up and returns the persistent instance of a
	 * <code>Device</code> with the specified UUID.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @param uuid
	 *            is the UUID of the <code>Device</code> to search for
	 * @return is the <code>Device</code> that was found in the persistent
	 *         store with the given UUID. If no <code>Device</code> was found,
	 *         null is returned.
	 */
	public IMetadataObject findByUuid(String uuid, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// Grab the DeviceDAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		return deviceDAO.findByUuid(uuid, returnFullObjectGraph);
	}

	/**
	 * This method will search for a <code>Device</code> with a UUID that
	 * matches the uuid specfied by the incoming byte array
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @param uuidAsBytes
	 *            is the UUID of the <code>Device</code> that is specified in
	 *            a byte array format
	 * @return the <code>Device</code> that has the UUID that was specified.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the search or the format of the
	 *             input UUID
	 */
	public IMetadataObject findByUuid(byte[] uuidAsBytes,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// Convert the byte array to a UUID
		UUID uuidToQueryFor = UUID.valueOf(uuidAsBytes);

		// If the UUID is valid, call the other search method
		if (uuidToQueryFor == null)
			return null;

		return this
				.findByUuid(uuidToQueryFor.toString(), returnFullObjectGraph);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @param name
	 * @return
	 * @throws MetadataAccessException
	 */
	public Collection findByName(String name, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DeviceDAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		return deviceDAO.findByName(name, exactMatch, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/**
	 * This method returns <code>Device</code>s that are found with the same
	 * (exact) device name, manufacturer name, model, and serial number. Not all
	 * the parameters need to be specified and only the ones specified will be
	 * used in the search.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @param name
	 *            is the name of the device to find
	 * @param mfgName
	 *            is the manufacturer's name
	 * @param mfgModel
	 *            is the model assigned by the manufacturer
	 * @param mfgSerialNumber
	 *            is the serialNumber given by the manufacturer to search for
	 * @param returnFullObjectGraph
	 *            is a <code>boolean</code> that indicates if the return
	 *            object should have its downstream objects (relationships)
	 *            instanitated. If it is <code>true</code> the related objects
	 *            will be instantiated. If <code>false</code> they will not be
	 *            instantiated and if the client tries to navigate the graph,
	 *            they will get a <code>LazyInitializationException</code>
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

		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now call the method
		return deviceDAO.findByNameAndMfgInfo(name, nameExactMatch, mfgName,
				mfgNameExactMatch, mfgModel, mfgModelExactMatch,
				mfgSerialNumber, mfgSerialNumberExactMatch,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/**
	 * This method returns all <code>Device</code>s that are linked (normally
	 * means owned) by a <code>Person</code>.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @param person
	 *            is the <code>Person</code> that will be used to search for
	 *            devices.
	 * @return a <code>Collection</code> of devices that are linked to that
	 *         person.
	 */
	public Collection findByPerson(Person person, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now call the method
		return deviceDAO.findByPerson(person, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/**
	 * This method returns all <code>Device</code>s that are of a certain
	 * <code>DeviceType</code>
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @param deviceType
	 *            is the <code>DeviceType</code> that will be used to search
	 *            for devices.
	 * @return a <code>Collection</code> of devices that are categorized by
	 *         the given <code>DeviceType</code>
	 */
	public Collection findByDeviceType(DeviceType deviceType,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now call the method
		return deviceDAO.findByDeviceType(deviceType, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/**
	 * This method returns a <code>Collection</code> of <code>Long</code>s
	 * that are the IDs of all the devices that are registered in SSDS.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @return a <code>Collection</code> of <code>Long</code>s that are the
	 *         IDs of all devices in SSDS.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public Collection findAllIDs() throws MetadataAccessException {

		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now call the method
		return deviceDAO.findAllIDs();
	}

	/**
	 * This method returns a <code>Collection</code> of <code>String</code>s
	 * that are the names of all the devices that are registered in SSDS.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @return a <code>Collection</code> of <code>String</code>s that are
	 *         the names of all devices in SSDS.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public Collection findAllNames() throws MetadataAccessException {
		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now call the method
		return deviceDAO.findAllNames();
	}

	/**
	 * This method returns all the devices that are deployed under a given
	 * parent deployment (DataProducer). This method walks the child deployments
	 * and grabs their devices as well. It will include the device of the given
	 * deployment as well.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @param parentDataProducer
	 *            The <code>DataProducer</code> that the method will search
	 *            for devices under.
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
		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now call the method
		return deviceDAO.findAllDeviceDeployedUnderParent(parentDataProducer,
				currentOnly);
	}

	/**
	 * @see moos.ssds.dao.DeviceDAO#findAllManufacturerNames()
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @throws MetadataAccessException
	 *             if something goes haywire.
	 */
	public Collection findAllManufacturerNames() throws MetadataAccessException {
		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now call the method
		return deviceDAO.findAllManufacturerNames();
	}

	/**
	 * @see moos.ssds.dao.DeviceDAO#findMfgModels(String)
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @throws MetadataAccessException
	 *             if something goes haywire.
	 */
	public Collection findMfgModels(String mfgName)
			throws MetadataAccessException {
		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now make the call
		return deviceDAO.findMfgModels(mfgName);
	}

	/**
	 * @see moos.ssds.dao.DeviceDAO#findMfgSerialNumbers(String, String)
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @throws MetadataAccessException
	 *             if something goes wrong
	 */
	public Collection findMfgSerialNumbers(String mfgName, String mfgModel)
			throws MetadataAccessException {
		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now make the call
		return deviceDAO.findMfgSerialNumbers(mfgName, mfgModel);
	}

	/**
	 * @see moos.ssds.dao.DeviceDAO#findByRecordVariable(RecordVariable,
	 *      boolean)
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @throws MetadataAccessException
	 *             if something goes wrong
	 */
	public Device findByRecordVariable(RecordVariable recordVariable,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now make the call
		return deviceDAO.findByRecordVariable(recordVariable,
				returnFullObjectGraph);
	}

	/**
	 * @see moos.ssds.dao.DeviceDAO#findBySearchingFields(String, String,
	 *      String, String, String, String, String, String, String, boolean)
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @throws MetadataAccessException
	 *             if something goes wrong
	 */
	public Collection findBySearchingFields(String id, String uuid,
			String name, String description, String mfgModel, String mfgName,
			String mfgSerialNumber, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now make the call
		return deviceDAO.findBySearchingFields(id, uuid, name, description,
				mfgModel, mfgName, mfgSerialNumber, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/**
	 * @see moos.ssds.dao.DeviceDAO#findBySearchingAllFieldsAndType(String,
	 *      String, String, boolean)
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @throws MetadataAccessException
	 *             if something goes wrong
	 */
	public Collection findBySearchingAllFieldsAndType(String searchTerm,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now make the call
		return deviceDAO.findBySearchingAllFieldsAndType(searchTerm,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/**
	 * This is the version that we can control for serialization purposes
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(DeviceAccessEJB.class);

}