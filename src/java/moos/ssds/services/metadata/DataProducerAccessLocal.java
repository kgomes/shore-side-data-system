package moos.ssds.services.metadata;

import java.util.Collection;
import java.util.Date;

import javax.ejb.Local;

import moos.ssds.dao.DataProducerDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.DataProducerGroup;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.Event;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.Software;
import moos.ssds.metadata.util.MetadataException;

@Local
public interface DataProducerAccessLocal extends AccessLocal {

	/**
	 * @see DataProducerDAO#findByProperties(String, boolean, String, Date,
	 *      boolean, Date, boolean, Double, Double, Double, Double, Float,
	 *      Float, Float, Float, String, boolean, String, boolean)
	 */
	public abstract Collection<DataProducer> findByProperties(String name,
			boolean exactMatch, String dataProducerType, Date startDate,
			boolean boundedByStartDate, Date endDate, boolean boundedByEndDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialDepthMin, Float geospatialDepthMax,
			Float geospatialBenthicAltitudeMin,
			Float geospatialBenthicAltitudeMax, String hostName,
			boolean exactHostNameMatch, String orderByProperty,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#countFindByProperties(String, boolean, String, Date,
	 *      boolean, Date, boolean, Double, Double, Double, Double, Float,
	 *      Float, Float, Float, String, boolean)
	 */
	public abstract int countFindByProperties(String name, boolean exactMatch,
			String dataProducerType, Date startDate,
			boolean boundedByStartDate, Date endDate, boolean boundedByEndDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialDepthMin, Float geospatialDepthMax,
			Float geospatialBenthicAltitudeMin,
			Float geospatialBenthicAltitudeMax, String hostName,
			boolean exactHostNameMatch) throws MetadataAccessException;

	/**
	 * \ * @see DataProducerDAO#findByName(String, boolean, String, boolean)
	 */
	public abstract Collection<DataProducer> findByName(String name,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#countFindByName(String, boolean)
	 */
	public abstract int countFindByName(String name, boolean exactMatch)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByDataProducerTypeAndName(String, String,
	 *      boolean, String, boolean)
	 */
	public abstract Collection<DataProducer> findByDataProducerTypeAndName(
			String dataProducerType, String name, String orderByPropertyName,
			String ascendingOrDescending, boolean exactMatch,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#countFindByDataProducerTypeAndName(String, String,
	 *      boolean)
	 */
	public abstract int countFindByDataProducerTypeAndName(
			String dataProducerType, String name, boolean exactMatch)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findParentlessDeployments(String, boolean)
	 */
	public abstract Collection<DataProducer> findParentlessDeployments(
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#countFindParentlessDeployments()
	 */
	public abstract int countFindParentlessDeployments()
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findParentlessDataProducer(String, String, boolean)
	 */
	public abstract Collection<DataProducer> findParentlessDataProducers(
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#countFindParentlessDataProducers()
	 */
	public abstract int countFindParentlessDataProducers()
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByDateRangeAndName(Date, boolean, Date, boolean,
	 *      String, boolean, String, boolean)
	 */
	public abstract Collection<DataProducer> findByDateRangeAndName(
			Date startDate, boolean boundedByStartDate, Date endDate,
			boolean boundedByEndDate, String name, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#countFindByDateRangeAndName(Date, boolean, Date,
	 *      boolean, String, boolean)
	 */
	public abstract int countFindByDateRangeAndName(Date startDate,
			boolean boundedByStartDate, Date endDate, boolean boundedByEndDate,
			String name, boolean exactMatch) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByGeospatialCube(Double, Double, Double, Double,
	 *      Float, Float, String, boolean)
	 */
	public abstract Collection<DataProducer> findByGeospatialCube(
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#countFindByGeospatialCube(Double, Double, Double,
	 *      Double, Float, Float)
	 */
	public abstract int countFindByGeospatialCube(Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByTimeAndGeospatialCube(Date, boolean, Date,
	 *      boolean, Double, Double, Double, Double, Float, Float, String,
	 *      boolean)
	 */
	public abstract Collection<DataProducer> findByTimeAndGeospatialCube(
			Date startDate, boolean boundedByStartDate, Date endDate,
			boolean boundedByEndDate, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#countFindByTimeAndGeospatialCube(Date, boolean,
	 *      Date, boolean, Double, Double, Double, Double, Float, Float)
	 */
	public abstract int countFindByTimeAndGeospatialCube(Date startDate,
			boolean boundedByStartDate, Date endDate, boolean boundedByEndDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByNameAndGeospatialCube(String, boolean, Double,
	 *      Double, Double, Double, Float, Float, String, boolean)
	 */
	public abstract Collection<DataProducer> findByNameAndGeospatialCube(
			String name, boolean exactMatch, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#countFindByNameAndGeospatialCube(String, boolean,
	 *      Double, Double, Double, Double, Float, Float)
	 */
	public abstract int countFindByNameAndGeospatialCube(String name,
			boolean exactMatch, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByNameAndTimeAndGeospatialCube(String, boolean,
	 *      Date, boolean, Date, boolean, Double, Double, Double, Double, Float,
	 *      Float, String, boolean)
	 */
	public abstract Collection<DataProducer> findByNameAndTimeAndGeospatialCube(
			String name, boolean exactMatch, Date startDate,
			boolean boundedByStartDate, Date endDate, boolean boundedByEndDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#countFindByNameAndTimeAndGeospatialCube(String,
	 *      boolean, Date, boolean, Date, boolean, Double, Double, Double,
	 *      Double, Float, Float, boolean)
	 */
	public abstract int countFindByNameAndTimeAndGeospatialCube(String name,
			boolean exactMatch, Date startDate, boolean boundedByStartDate,
			Date endDate, boolean boundedByEndDate, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByHostName(String, boolean, String, boolean)
	 */
	public abstract Collection<DataProducer> findByHostName(String hostName,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#countFindByHostName(String, boolean)
	 */
	public abstract int countFindByHostName(String hostName, boolean exactMatch)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByPerson(Person, String, boolean)
	 */
	public abstract Collection<DataProducer> findByPerson(Person person,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#countFindByPerson(Person)
	 */
	public abstract int countFindByPerson(Person person)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByDevice(Device, String, String, boolean)
	 */
	public abstract Collection<DataProducer> findByDevice(Device device,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByDeviceId(Long, String, String, boolean)
	 */
	public abstract Collection<DataProducer> findByDeviceId(Long deviceId,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByDeviceAndTimeWindow(Device, Date, Date,
	 *      String, String, boolean)
	 */
	public abstract Collection<DataProducer> findByDeviceAndTimeWindow(
			Device device, Date startDate, Date endDate,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByDeviceTypeName(String, boolean, String,
	 *      String, boolean)
	 */
	public abstract Collection<DataProducer> findByDeviceTypeName(
			String deviceTypeName, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findCurrentParentlessDeployments(String, String,
	 *      boolean)
	 */
	public abstract Collection<DataProducer> findCurrentParentlessDeployments(
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findParentlessDeploymentsByName(String, boolean,
	 *      String, String, boolean)
	 */
	public abstract Collection<DataProducer> findParentlessDeploymentsByName(
			String name, boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findCurrentDeployments(String, String, boolean)
	 */
	public abstract Collection<DataProducer> findCurrentDeployments(
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findCurrentDeploymentsOfDevice(Device, String,
	 *      String, boolean)
	 */
	public abstract Collection<DataProducer> findCurrentDeploymentsOfDevice(
			Device device, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findCurrentDeploymentsOfDeviceId(Long, String,
	 *      String, boolean)
	 */
	public abstract Collection<DataProducer> findCurrentDeploymentsOfDeviceId(
			Long deviceId, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @throws MetadataAccessException
	 * @throws MetadataException
	 * @see DataProducerDAO#findCurrentDeploymentsByRole(String, String, String,
	 *      boolean)
	 */
	public abstract Collection<DataProducer> findCurrentDeploymentsByRole(
			String role, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException, MetadataException;

	/**
	 * @throws MetadataAccessException
	 * @throws MetadataException
	 * @see DataProducerDAO#findCurrentDeploymentsByRoleAndName(Device, String,
	 *      String, boolean)
	 */
	public abstract Collection<DataProducer> findCurrentDeploymentsByRoleAndName(
			String role, String name, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException,
			MetadataException;

	/**
	 * @see DataProducerDAO#findBySoftware(Software, String, boolean)
	 */
	public abstract Collection<DataProducer> findBySoftware(Software software,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findParentDataProducer(DataProducer)
	 */
	public abstract DataProducer findParentDataProducer(
			DataProducer dataProducer, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findClosestParentDataProducerLatitude
	 */
	public abstract Double findClosestParentDataProducerLatitude(
			DataProducer dataProducer) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findChildDataProducers(DataProducer, boolean)
	 */
	public abstract Collection<DataProducer> findChildDataProducers(
			DataProducer dataProducer, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#countFindChildDataProducers(DataProducer)
	 */
	public abstract int countFindChildDataProducers(DataProducer dataProducer)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByDataProducerGroup(DataProducerGroup, String,
	 *      boolean)
	 */
	public abstract Collection<DataProducer> findByDataProducerGroup(
			DataProducerGroup dataProducerGroup, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByDataProducerGroupName(String, boolean, String,
	 *      boolean)
	 */
	public abstract Collection<DataProducer> findByDataProducerGroupName(
			String dataProducerGroupName, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#countFindByDataProducerGroupName(String, boolean)
	 */
	public abstract int countFindByDataProducerGroupName(
			String dataProducerGroupName, boolean exactMatch)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByInput(DataContainer, String, boolean)
	 */
	public abstract Collection<DataProducer> findByInput(
			DataContainer dataContainer, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByOutput(DataContainer, String, boolean)
	 */
	public abstract DataProducer findByOutput(DataContainer dataContainer,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByResource(Resource, String, boolean)
	 */
	public abstract Collection<DataProducer> findByResource(Resource resource,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByKeywordName(String, boolean, String, String,
	 *      boolean)
	 */
	public abstract Collection<DataProducer> findByKeywordName(
			String keywordName, boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#findByEvent(Event, String, boolean)
	 */
	public abstract Collection<DataProducer> findByEvent(Event event,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * This method takes in a <code>IDeployment</code> and a DeviceType name,
	 * and some nominal location coordinates and then tries to return a
	 * <code>Collection</code> of <code>IDeployment</code>s the have a similar
	 * device type name, that were deployed on (or under) the given deployment
	 * (parent-child relationship). It will do a &quot;deep&quot; search for
	 * devices. In other words, it will check all deployments of the parent as
	 * well as of any sub-deployments under that parent (i.e. it will &quot;Walk
	 * the chain&quot;).
	 * 
	 * @param parentDeployment
	 *            This is a parent <code>IDeployment</code> to start the search
	 *            from. It is the &quot;Root&quot; of the search tree.
	 * @param deviceTypeName
	 *            This is a <code>String</code> that will be used to search for
	 *            devices that have a similar device type to that named with
	 *            this string.
	 * @param nominalLongitude
	 *            This is the longitude that the device should be deployed at.
	 *            It is used as the point of a search and then you can use the
	 *            <code>longitudeTolerance</code> to declare a +/- search
	 *            window.
	 * @param longitudeTolerance
	 *            This is the +/- that will be added to the
	 *            <code>nominalLongitude</code> parameter for window searches.
	 * @param nominalLatitude
	 *            This is the lattitude that the device should be deployed at.
	 *            It is used as the point of a search and then you can use the
	 *            <code>lattitudeTolerance</code> to declare a +/- search
	 *            window.
	 * @param lattitudeTolerance
	 *            This is the +/- that will be added to the
	 *            <code>nominalLatitude</code> parameter for window searches.
	 * @param nominalDepth
	 *            This is the depth that the device should be deployed at. It is
	 *            used as the point of a search and then you can use the
	 *            <code>depthTolerance</code> to declare a +/- search window.
	 * @param depthTolerance
	 *            This is the +/- that will be added to the
	 *            <code>nominalDepth</code> parameter for window searches.
	 * @return a <code>Collection</code> of <code>IDeployment</code>s that meet
	 *         the search criteria defined by the incoming parameters. No
	 *         duplicates are removed and if no deployments were found, an empty
	 *         collection is returned.
	 */
	public abstract Collection<DataProducer> findAllDeploymentsOfDeviceTypeFromParent(
			DataProducer parentDeployment, String deviceTypeName,
			String orderByProperty, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * This method takes in a deviceID, a DeviceType name, and some nominal
	 * location coordinates and then tries to return a <code>Collection</code>
	 * of <code>IDeployment</code>s the have a similar device type name, that
	 * were deployed on (or under) the given deployment (parent-child
	 * relationship). It will do a &quot;deep&quot; search for devices. In other
	 * words, it will check all deployments of the parent as well as of any
	 * sub-deployments under that parent (i.e. it will &quot;Walk the
	 * chain&quot;).
	 * 
	 * @param parentDeployment
	 *            This is a parent <code>IDeployment</code> to start the search
	 *            from. It is the &quot;Root&quot; of the search tree.
	 * @param deviceTypeName
	 *            This is a <code>String</code> that will be used to search for
	 *            devices that have a similar device type to that named with
	 *            this string.
	 * @param nominalLongitude
	 *            This is the longitude that the device should be deployed at.
	 *            It is used as the point of a search and then you can use the
	 *            <code>longitudeTolerance</code> to declare a +/- search
	 *            window.
	 * @param longitudeTolerance
	 *            This is the +/- that will be added to the
	 *            <code>nominalLongitude</code> parameter for window searches.
	 * @param nominalLatitude
	 *            This is the lattitude that the device should be deployed at.
	 *            It is used as the point of a search and then you can use the
	 *            <code>lattitudeTolerance</code> to declare a +/- search
	 *            window.
	 * @param lattitudeTolerance
	 *            This is the +/- that will be added to the
	 *            <code>nominalLatitude</code> parameter for window searches.
	 * @param nominalDepth
	 *            This is the depth that the device should be deployed at. It is
	 *            used as the point of a search and then you can use the
	 *            <code>depthTolerance</code> to declare a +/- search window.
	 * @param depthTolerance
	 *            This is the +/- that will be added to the
	 *            <code>nominalDepth</code> parameter for window searches.
	 * @return a <code>Collection</code> of <code>IDeployment</code>s that meet
	 *         the search criteria defined by the incoming parameters. No
	 *         duplicates are removed and if no deployments were found, an empty
	 *         collection is returned.
	 */
	public abstract Collection<DataProducer> findAllDeploymentsOfDeviceTypeFromParent(
			Long parentID, String deviceTypeName, Double nominalLongitude,
			Double longitudeTolerance, Double nominalLatitude,
			Double latitudeTolerance, Float nominalDepth,
			Double depthTolerance, String orderByProperty,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * This method takes in the ID of a parent <code>IDevice</code>, the name of
	 * a <code>IDeviceType</code>, and some nominal location coordinates and
	 * then tries to return a <code>Collection</code> of <code>IDevice</code>s
	 * with the given type, that were deployed on the given device
	 * (parent-child) relationship. This will only look for direct child
	 * deployments, it won't walk any of the sub deployments.
	 * 
	 * @param parentID
	 * @param deviceTypeName
	 * @param nominalLongitude
	 * @param longitudeTolerance
	 * @param nominalLatitude
	 * @param latitudeTolerance
	 * @param nominalDepth
	 * @param depthTolerance
	 * @return
	 */
	public abstract Collection<Device> findDevicesByParentByTypeAndByLocation(
			Long parentID, String deviceTypeName, Double nominalLongitude,
			Double longitudeTolerance, Double nominalLatitude,
			Double latitudeTolerance, Float nominalDepth,
			Double depthTolerance, String orderByProperty,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * This method takes in the name of a <code>IDevice</code>, the name of a
	 * <code>IDeviceType</code>, and some nominal location coordinates and then
	 * tries to return a <code>Collection</code> of <code>IDevices</code> with
	 * the given type, that were deployed on the given device (parent-child)
	 * relationship. This will only look for direct child deployments, it won't
	 * walk any of the sub deployments.
	 * 
	 * @param parentName
	 * @param deviceTypeName
	 * @param nominalLongitude
	 * @param longitudeTolerance
	 * @param nominalLatitude
	 * @param latitudeTolerance
	 * @param nominalDepth
	 * @param depthTolerance
	 * @return
	 */
	public abstract Collection<Device> findDevicesByParentByTypeAndByLocation(
			String parentName, String deviceTypeName, Double nominalLongitude,
			Double longitudeTolerance, Double nominalLatitude,
			Double latitudeTolerance, Float nominalDepth,
			Double depthTolerance, String orderByProperty,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * This method takes in a <code>IDeployment</code> and a
	 * <code>DeviceType</code> name, and some nominal location coordinates and
	 * then tries to return a list of <code>IDevice</code>s the have a similar
	 * device type name, that were deployed on the given device (parent-child)
	 * relationship. It will do a &quot;deep&quot; search for devices. In other
	 * words, it will check all deployments of the parent as well as of any
	 * sub-deployments under that parent (i.e. it will &quot;Walk the
	 * chain&quot;).
	 * 
	 * @param parentDeployment
	 *            This is a parent <code>IDeployment</code> to start the search
	 *            from. It is the &quot;Root&quot; of the search tree.
	 * @param deviceTypeName
	 *            This is a <code>String</code> that will be used to search for
	 *            devices that have a similar device type to that named with
	 *            this string.
	 * @param nominalLongitude
	 *            This is the longitude that the device should be deployed at.
	 *            It is used as the point of a search and then you can use the
	 *            <code>longitudeTolerance</code> to declare a +/- search
	 *            window.
	 * @param longitudeTolerance
	 *            This is the +/- that will be added to the
	 *            <code>nominalLongitude</code> parameter for window searches.
	 * @param nominalLatitude
	 *            This is the lattitude that the device should be deployed at.
	 *            It is used as the point of a search and then you can use the
	 *            <code>lattitudeTolerance</code> to declare a +/- search
	 *            window.
	 * @param lattitudeTolerance
	 *            This is the +/- that will be added to the
	 *            <code>nominalLatitude</code> parameter for window searches.
	 * @param nominalDepth
	 *            This is the depth that the device should be deployed at. It is
	 *            used as the point of a search and then you can use the
	 *            <code>depthTolerance</code> to declare a +/- search window.
	 * @param depthTolerance
	 *            This is the +/- that will be added to the
	 *            <code>nominalDepth</code> parameter for window searches.
	 * @return a <code>Collection</code> of <code>IDevice</code>s that meet the
	 *         search criteria defined by the incoming parameters. The devices
	 *         are listed from the most recent deployment first (index 0) to the
	 *         oldest deployment. Each device is listed only once in the return
	 *         collection
	 */
	public abstract Collection<Device> findAllDevicesByParentDeploymentByTypeAndByLocation(
			DataProducer parentDeployment, String deviceTypeName,
			Double nominalLongitude, Double longitudeTolerance,
			Double nominalLatitude, Double latitudeTolerance,
			Float nominalDepth, Double depthTolerance, String orderByProperty,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @param parentID
	 * @param deviceTypeName
	 * @param nominalLongitude
	 * @param longitudeTolerance
	 * @param nominalLatitude
	 * @param latitudeTolerance
	 * @param nominalDepth
	 * @param depthTolerance
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<Device> findAllDevicesByParentByTypeAndByLocation(
			Long parentID, String deviceTypeName, Double nominalLongitude,
			Double longitudeTolerance, Double nominalLatitude,
			Double latitudeTolerance, Float nominalDepth,
			Double depthTolerance, String orderByProperty,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#addChildDataProducer(DataProducer, DataProducer)
	 */
	public abstract void addChildDataProducer(DataProducer parentDataProducer,
			DataProducer childDataProducer) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#addResource(DataProducer, Resource)
	 */
	public abstract void addResource(DataProducer dataProducer,
			Resource resourceToAdd) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#removeResource(DataProducer, Resource)
	 */
	public abstract void removeResource(DataProducer dataProducer,
			Resource resource) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#createDuplicateDeepDeployment(DataProducer, Date,
	 *      boolean, Date, String)
	 */
	public abstract Long createDuplicateDeepDeployment(
			DataProducer deploymentToCopy, Date newStartDate, boolean closeOld,
			Date oldEndDate, String newHeadDeploymentName,
			String baseDataStreamUri) throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#makeDeepTransient(DataProducer)
	 */
	public abstract void deepDelete(DataProducer dataProducer)
			throws MetadataAccessException;

	/**
	 * @see DataProducerDAO#makeDeepTransient(DataProducer)
	 */
	public abstract void makeDeepTransient(DataProducer dataProducer)
			throws MetadataAccessException;

}