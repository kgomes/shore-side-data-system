package moos.ssds.services.metadata;

import java.util.Collection;

import javax.ejb.Remote;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.DeviceType;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.RecordVariable;

@Remote
public interface DeviceAccess extends Access {

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
	public abstract Device findByUuid(String uuid, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * This method will search for a <code>Device</code> with a UUID that
	 * matches the uuid specfied by the incoming byte array
	 * 
	 * @param uuidAsBytes
	 *            is the UUID of the <code>Device</code> that is specified in a
	 *            byte array format
	 * @return the <code>Device</code> that has the UUID that was specified.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the search or the format of the
	 *             input UUID
	 */
	public abstract Device findByUuid(byte[] uuidAsBytes,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param name
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<Device> findByName(String name,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

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
	 * @param returnFullObjectGraph
	 *            is a <code>boolean</code> that indicates if the return object
	 *            should have its downstream objects (relationships)
	 *            instantiated. If it is <code>true</code> the related objects
	 *            will be instantiated. If <code>false</code> they will not be
	 *            instantiated and if the client tries to navigate the graph,
	 *            they will get a <code>LazyInitializationException</code>
	 * @return a <code>Collection</code> of <code>Device</code>s that have
	 *         attributes that exactly match those that were specified as query
	 *         parameters.
	 */
	public abstract Collection<Device> findByNameAndMfgInfo(String name,
			boolean nameExactMatch, String mfgName, boolean mfgNameExactMatch,
			String mfgModel, boolean mfgModelExactMatch,
			String mfgSerialNumber, boolean mfgSerialNumberExactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

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
	public abstract Collection<Device> findByPerson(Person person,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

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
	public abstract Collection<Device> findByDeviceType(DeviceType deviceType,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * This method returns a <code>Collection</code> of <code>Long</code>s that
	 * are the IDs of all the devices that are registered in SSDS.
	 * 
	 * @return a <code>Collection</code> of <code>Long</code>s that are the IDs
	 *         of all devices in SSDS.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public abstract Collection<Long> findAllIDs()
			throws MetadataAccessException;

	/**
	 * This method returns a <code>Collection</code> of <code>String</code>s
	 * that are the names of all the devices that are registered in SSDS.
	 * 
	 * @return a <code>Collection</code> of <code>String</code>s that are the
	 *         names of all devices in SSDS.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public abstract Collection<String> findAllNames()
			throws MetadataAccessException;

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
	public abstract Collection<Device> findAllDeviceDeployedUnderParent(
			DataProducer parentDataProducer, boolean currentOnly)
			throws MetadataAccessException;

	/**
	 * @see moos.ssds.dao.DeviceDAO#findAllManufacturerNames()
	 * @throws MetadataAccessException
	 *             if something goes haywire.
	 */
	public abstract Collection<String> findAllManufacturerNames()
			throws MetadataAccessException;

	/**
	 * @see moos.ssds.dao.DeviceDAO#findMfgModels(String)
	 * @throws MetadataAccessException
	 *             if something goes haywire.
	 */
	public abstract Collection<String> findMfgModels(String mfgName)
			throws MetadataAccessException;

	/**
	 * @see moos.ssds.dao.DeviceDAO#findMfgSerialNumbers(String, String)
	 * @throws MetadataAccessException
	 *             if something goes wrong
	 */
	public abstract Collection<String> findMfgSerialNumbers(String mfgName,
			String mfgModel) throws MetadataAccessException;

	/**
	 * @see moos.ssds.dao.DeviceDAO#findByRecordVariable(RecordVariable,
	 *      boolean)
	 * @throws MetadataAccessException
	 *             if something goes wrong
	 */
	public abstract Device findByRecordVariable(RecordVariable recordVariable,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see moos.ssds.dao.DeviceDAO#findBySearchingFields(String, String,
	 *      String, String, String, String, String, String, String, boolean)
	 * @throws MetadataAccessException
	 *             if something goes wrong
	 */
	public abstract Collection<Device> findBySearchingFields(String id,
			String uuid, String name, String description, String mfgModel,
			String mfgName, String mfgSerialNumber, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see moos.ssds.dao.DeviceDAO#findBySearchingAllFieldsAndType(String,
	 *      String, String, boolean)
	 * @throws MetadataAccessException
	 *             if something goes wrong
	 */
	public abstract Collection<Device> findBySearchingAllFieldsAndType(
			String searchTerm, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

}