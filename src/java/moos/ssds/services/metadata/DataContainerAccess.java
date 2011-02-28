package moos.ssds.services.metadata;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Date;

import javax.ejb.Remote;

import moos.ssds.dao.DataContainerDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataContainerGroup;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.Resource;

@Remote
public interface DataContainerAccess extends Access {

	/**
	 * @see DataContainerDAO#findByName(String, boolean)
	 */
	public abstract Collection<DataContainer> findByName(String name,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see DataContainerDAO#countFindByName(String, boolean)
	 */
	public abstract int countFindByName(String name, boolean exactMatch)
			throws MetadataAccessException;

	/**
	 * This method returns a <code>Collection</code> of <code>String</code>s
	 * that are the names of all the dataContainers that are registered in SSDS.
	 * 
	 * @return a <code>Collection</code> of <code>String</code>s that are the
	 *         names of all dataContainers in SSDS.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public abstract Collection<String> findAllNames()
			throws MetadataAccessException;

	/**
	 * @param dataContainerType
	 * @param name
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByDataContainerTypeAndName(
			String dataContainerType, String name, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param dataContainerType
	 * @param name
	 * @param exactMatch
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract int countFindByDataContainerTypeAndName(
			String dataContainerType, String name, boolean exactMatch)
			throws MetadataAccessException;

	/**
	 * @see DataContainerDAO#findWithDataWithinTimeWindow(Date, boolean, Date,
	 *      boolean)
	 */
	public abstract Collection<DataContainer> findWithDataWithinTimeWindow(
			Date startDate, boolean allDataAfterStartDate, Date endDate,
			boolean allDataBeforeEndDate, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * This method tries to look up and instantiate a DataContainer by its URI
	 * string
	 * 
	 * @param uriString
	 *            is a <code>java.lang.String</code> that will be used to search
	 *            for matches of a dataContainer's URI string
	 * @return a <code>DataContainer</code> object that has that URI string. If
	 *         no matches were found, an empty collection is returned
	 * @throws MetadataAccessException
	 *             if something goes wrong with the search
	 */
	public abstract DataContainer findByURIString(String uriString,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param uri
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByURI(URI uri,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param url
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByURL(URL url,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * This method returns a <code>Collection</code> of <code>String</code>s
	 * that are the URI strings of all the dataContainers that are registered in
	 * SSDS.
	 * 
	 * @return a <code>Collection</code> of <code>String</code>s that are the
	 *         URI strings of all dataContainers in SSDS.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public abstract Collection<String> findAllURIStrings()
			throws MetadataAccessException;

	/**
	 * @return a <code>DataContainer</code> that has exactly the same DODS URL.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public abstract DataContainer findByDODSURLString(String dodsUrlString,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * This method returns a <code>Collection</code> of
	 * <code>DataContainer</code>s that have a matching DODS URL String. If
	 * <code>exactMatch</code> is true, only those the have exactly the same
	 * DODS URLs will be returned, otherwise all <code>DataContainer</code>s
	 * that have part of their DODS URLs that match will be returned
	 * 
	 * @return a <code>Collection</code> of <code>DataContainer</code>s that
	 *         have matching DODS URLs
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public abstract Collection<DataContainer> findByDODSURLString(
			String dodsUrlString, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * This method returns the number of DataContainers that match the
	 * DODSUrlString provided. If <code>exactMatch</code> is true then only the
	 * <code>DataContainer</code>s that match that DODS URL exactly will be
	 * returned. Otherwise, a LIKE search will be done for
	 * <code>DataContainer</code>s that have part of their DODS URLs that match
	 * 
	 * @return an int that indicates how many DataContainers match that
	 *         DODSUrlString
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public abstract int countFindByDODSURLString(String dodsUrlString,
			boolean exactMatch) throws MetadataAccessException;

	/**
	 * @param mimeType
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByMimeType(String mimeType,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * This method will return all <code>DataContainer</code>s that have data
	 * that is inside the specified geospatial box. In general if any sides of
	 * the box are not specified, the search dimension will be considered
	 * infinite in that direction.
	 * 
	 * @param geospatialLatMin
	 *            This is the minimum lattitude of the search cube.
	 * @param geospatialLatMax
	 *            This is the maximum lattitude of the search cube.
	 * @param geospatialLonMin
	 *            This is the minimum longitude of the search cube.
	 * @param geospatialLonMax
	 *            This is the maximum longitude of the search cube.
	 * @param geospatialVerticalMin
	 *            This is the minimum vertical value of the cube (the bottom of
	 *            the cube).
	 * @param geospatialVerticalMax
	 *            This is the maximum vertical value of the cube (the top of the
	 *            cube).
	 * @return A <code>Collection</code> of <code>DataContainer</code>s that
	 *         have data within that cube.
	 */
	public abstract Collection<DataContainer> findWithinGeospatialCube(
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * This method will return all <code>DataContainer</code>s that have data
	 * that is inside the specified geospatial box and within the time
	 * specified. In general if any sides of the box are not specified, the
	 * search dimension will be considered infinite in that direction.
	 * 
	 * @param startDate
	 *            specifies the time after which the <code>DataContainer</code>
	 *            must have data to be found in this query.
	 * @param endDate
	 *            specifies the time before which the <code>DataContainer</code>
	 *            must have data to be found in this query.
	 * @param geospatialLatMin
	 *            This is the minimum lattitude of the search cube.
	 * @param geospatialLatMax
	 *            This is the maximum lattitude of the search cube.
	 * @param geospatialLonMin
	 *            This is the minimum longitude of the search cube.
	 * @param geospatialLonMax
	 *            This is the maximum longitude of the search cube.
	 * @param geospatialVerticalMin
	 *            This is the minimum vertical value of the cube (the bottom of
	 *            the cube).
	 * @param geospatialVerticalMax
	 *            This is the maximum vertical value of the cube (the top of the
	 *            cube).
	 * @return A <code>Collection</code> of <code>DataContainer</code>s that
	 *         have data within that cube.
	 */
	public abstract Collection<DataContainer> findWithinTimeAndGeospatialCube(
			Date startDate, Date endDate, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * This method returns all <code>DataContainer</code>s that are linked
	 * (normally means owned) by a <code>Person</code>.
	 * 
	 * @param person
	 *            is the <code>Person</code> that will be used to search for
	 *            devices.
	 * @return a <code>Collection</code> of devices that are linked to that
	 *         person.
	 */
	public abstract Collection<DataContainer> findByPerson(Person person,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataContainerDAO#findByRecordVariable(RecordVariable, boolean)
	 */
	public abstract DataContainer findByRecordVariable(
			RecordVariable recordVariable, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @param recordVariableName
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByRecordVariableName(
			String recordVariableName, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param likeRecordVariableName
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByLikeRecordVariableName(
			String likeRecordVariableName, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @param standardVariableName
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByStandardVariableName(
			String standardVariableName, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param likeStandardVariableName
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByLikeStandardVariableName(
			String likeStandardVariableName, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @param dataContainerGroup
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByDataContainerGroup(
			DataContainerGroup dataContainerGroup, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @param dataContainerGroupName
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByDataContainerGroupName(
			String dataContainerGroupName, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @param likeDataContainerGroupName
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByLikeDataContainerGroupName(
			String likeDataContainerGroupName, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see DataContainerDAO#findByKeywordName(String, boolean, boolean)
	 */
	public abstract Collection<DataContainer> findByKeywordName(
			String keywordName, boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see DataContainerDAO#countFindByKeywordName(String, boolean)
	 */
	public abstract int countFindByKeywordName(String keywordName,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @param resource
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByResource(Resource resource,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param dataContainer
	 * @param fetchDepth
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataProducer> findAllIndirectCreators(
			DataContainer dataContainer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param dataContainer
	 * @param fetchDepth
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataProducer> findCreatorChain(
			DataContainer dataContainer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param dataContainer
	 * @param fetchDepth
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataProducer> findAllIndirectConsumers(
			DataContainer dataContainer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param dataContainer
	 * @param fetchDepth
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataProducer> findAllConsumers(
			DataContainer dataContainer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param dataContainer
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findDirectInputs(
			DataContainer dataContainer, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @param dataContainer
	 * @param fetchDepth
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findAllInputs(
			DataContainer dataContainer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param dataContainer
	 * @param fetchDepth
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findAllDerivedOutputs(
			DataContainer dataContainer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param dataProducer
	 * @param fetchDepth
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findAllDerivedOutputs(
			DataProducer dataProducer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @see DataContainerDAO#findInputsByDataProducer(DataProducer dataProducer,
	 *      String orderByPropertyName, boolean returnFullObjectGraphs)
	 */
	public abstract Collection<DataContainer> findInputsByDataProducer(
			DataProducer dataProducer, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see DataContainerDAO#findOutputsByDataProducer(DataProducer
	 *      dataProducer, String orderByPropertyName, boolean
	 *      returnFullObjectGraphs)
	 */
	public abstract Collection<DataContainer> findOutputsByDataProducer(
			DataProducer dataProducer, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @param dataProducer
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract DataContainer findBestDirectOutput(
			DataProducer dataProducer, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @param recordVariableName
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByRecordVariableNameAndDataWithinTimeWindow(
			String recordVariableName, Date startDate, Date endDate,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param likeRecordVariableName
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByLikeRecordVariableNameAndDataWithinTimeWindow(
			String likeRecordVariableName, Date startDate, Date endDate,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param standardVariableName
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByStandardVariableNameAndDataWithinTimeWindow(
			String standardVariableName, Date startDate, Date endDate,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param likeStandardVariableName
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByLikeStandardVariableNameAndDataWithinTimeWindow(
			String likeStandardVariableName, Date startDate, Date endDate,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param recordVariableName
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialVerticalMin
	 * @param geospatialVerticalMax
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByRecordVariableNameAndWithinGeospatialCube(
			String recordVariableName, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @param likeRecordVariableName
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialVerticalMin
	 * @param geospatialVerticalMax
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByLikeRecordVariableNameAndWithinGeospatialCube(
			String likeRecordVariableName, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @param standardVariableName
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialVerticalMin
	 * @param geospatialVerticalMax
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByStandardVariableNameAndWithinGeospatialCube(
			String standardVariableName, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @param likeStandardVariableName
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialVerticalMin
	 * @param geospatialVerticalMax
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByLikeStandardVariableNameAndWithinGeospatialCube(
			String likeStandardVariableName, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @param recordVariableName
	 * @param startDate
	 * @param endDate
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialVerticalMin
	 * @param geospatialVerticalMax
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByRecordVariableNameWithinTimeAndWithinGeospatialCube(
			String recordVariableName, Date startDate, Date endDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param likeRecordVariableName
	 * @param startDate
	 * @param endDate
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialVerticalMin
	 * @param geospatialVerticalMax
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByLikeRecordVariableNameWithinTimeAndWithinGeospatialCube(
			String likeRecordVariableName, Date startDate, Date endDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param standardVariableName
	 * @param startDate
	 * @param endDate
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialVerticalMin
	 * @param geospatialVerticalMax
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByStandardVariableNameWithinTimeAndWithinGeospatialCube(
			String standardVariableName, Date startDate, Date endDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * @param likeStandardVariableName
	 * @param startDate
	 * @param endDate
	 * @param geospatialLatMin
	 * @param geospatialLatMax
	 * @param geospatialLonMin
	 * @param geospatialLonMax
	 * @param geospatialVerticalMin
	 * @param geospatialVerticalMax
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataContainer> findByLikeStandardVariableNameWithinTimeAndWithinGeospatialCube(
			String likeStandardVariableName, Date startDate, Date endDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

}