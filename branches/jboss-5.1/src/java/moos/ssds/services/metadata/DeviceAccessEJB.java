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

import javax.annotation.PostConstruct;
import javax.ejb.CreateException;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import moos.ssds.dao.DeviceDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.DeviceType;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.RecordVariable;

import org.apache.log4j.Logger;
import org.doomdark.uuid.UUID;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * Provides a facade that provides client services for Device objects.
 * 
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.21 $
 */
@Stateless
@Local(DeviceAccessLocal.class)
@LocalBinding(jndiBinding = "moos/ssds/services/metadata/DeviceAccessLocal")
@Remote(DeviceAccess.class)
@RemoteBinding(jndiBinding = "moos/ssds/services/metadata/DeviceAccess")
public class DeviceAccessEJB extends AccessBean implements DeviceAccess,
		DeviceAccessLocal {

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(DeviceAccessEJB.class);

	/**
	 * This is the version that we can control for serialization purposes
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This method is called after the EJB is constructed and it sets the
	 * <code>Class</code> on the super class that defines the type of EJB access
	 * class it will work with.
	 * 
	 * @throws CreateException
	 */
	@PostConstruct
	public void setUpEJBType() {

		// Now set the super persistent class to Device
		super.setPersistentClass(Device.class);
		logger.debug("OK, set Persistent class to Device");

		// And the DAO
		super.setDaoClass(DeviceDAO.class);
		logger.debug("OK, set DAO Class to DeviceDAO");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DeviceAccess#findByUuid(java.lang.String,
	 * boolean)
	 */

	public Device findByUuid(String uuid, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// Grab the DeviceDAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		return deviceDAO.findByUuid(uuid, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DeviceAccess#findByUuid(byte[], boolean)
	 */

	public Device findByUuid(byte[] uuidAsBytes, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// Convert the byte array to a UUID
		UUID uuidToQueryFor = UUID.valueOf(uuidAsBytes);

		// If the UUID is valid, call the other search method
		if (uuidToQueryFor == null)
			return null;

		return this
				.findByUuid(uuidToQueryFor.toString(), returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DeviceAccess#findByName(java.lang.String,
	 * boolean, java.lang.String, java.lang.String, boolean)
	 */

	public Collection<Device> findByName(String name, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DeviceDAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		return deviceDAO.findByName(name, exactMatch, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DeviceAccess#findByNameAndMfgInfo(java.lang
	 * .String, boolean, java.lang.String, boolean, java.lang.String, boolean,
	 * java.lang.String, boolean, java.lang.String, java.lang.String, boolean)
	 */

	public Collection<Device> findByNameAndMfgInfo(String name,
			boolean nameExactMatch, String mfgName, boolean mfgNameExactMatch,
			String mfgModel, boolean mfgModelExactMatch,
			String mfgSerialNumber, boolean mfgSerialNumberExactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now call the method
		return deviceDAO.findByNameAndMfgInfo(name, nameExactMatch, mfgName,
				mfgNameExactMatch, mfgModel, mfgModelExactMatch,
				mfgSerialNumber, mfgSerialNumberExactMatch,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DeviceAccess#findByPerson(moos.ssds.metadata
	 * .Person, java.lang.String, java.lang.String, boolean)
	 */

	public Collection<Device> findByPerson(Person person,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now call the method
		return deviceDAO.findByPerson(person, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DeviceAccess#findByDeviceType(moos.ssds.metadata
	 * .DeviceType, java.lang.String, java.lang.String, boolean)
	 */

	public Collection<Device> findByDeviceType(DeviceType deviceType,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now call the method
		return deviceDAO.findByDeviceType(deviceType, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DeviceAccess#findAllIDs()
	 */

	public Collection<Long> findAllIDs() throws MetadataAccessException {

		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now call the method
		return deviceDAO.findAllIDs();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DeviceAccess#findAllNames()
	 */

	public Collection<String> findAllNames() throws MetadataAccessException {
		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now call the method
		return deviceDAO.findAllNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DeviceAccess#findAllDeviceDeployedUnderParent
	 * (moos.ssds.metadata.DataProducer, boolean)
	 */

	public Collection<Device> findAllDeviceDeployedUnderParent(
			DataProducer parentDataProducer, boolean currentOnly)
			throws MetadataAccessException {
		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now call the method
		return deviceDAO.findAllDeviceDeployedUnderParent(parentDataProducer,
				currentOnly);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DeviceAccess#findAllManufacturerNames()
	 */

	public Collection<String> findAllManufacturerNames()
			throws MetadataAccessException {
		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now call the method
		return deviceDAO.findAllManufacturerNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DeviceAccess#findMfgModels(java.lang.String)
	 */

	public Collection<String> findMfgModels(String mfgName)
			throws MetadataAccessException {
		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now make the call
		return deviceDAO.findMfgModels(mfgName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DeviceAccess#findMfgSerialNumbers(java.lang
	 * .String, java.lang.String)
	 */

	public Collection<String> findMfgSerialNumbers(String mfgName,
			String mfgModel) throws MetadataAccessException {
		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now make the call
		return deviceDAO.findMfgSerialNumbers(mfgName, mfgModel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DeviceAccess#findByRecordVariable(moos.ssds
	 * .metadata.RecordVariable, boolean)
	 */

	public Device findByRecordVariable(RecordVariable recordVariable,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now make the call
		return deviceDAO.findByRecordVariable(recordVariable,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DeviceAccess#findBySearchingFields(java.lang
	 * .String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, boolean)
	 */

	public Collection<Device> findBySearchingFields(String id, String uuid,
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DeviceAccess#findBySearchingAllFieldsAndType
	 * (java.lang.String, java.lang.String, java.lang.String, boolean)
	 */

	public Collection<Device> findBySearchingAllFieldsAndType(
			String searchTerm, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DeviceDAO deviceDAO = (DeviceDAO) this.getMetadataDAO();

		// Now make the call
		return deviceDAO.findBySearchingAllFieldsAndType(searchTerm,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}
}