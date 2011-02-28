package moos.ssds.services.metadata;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Date;

import javax.ejb.Local;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataContainerGroup;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.Resource;

@Local
public interface DataContainerAccessLocal extends AccessLocal {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByName(java.lang.
	 * String, boolean, java.lang.String, java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByName(String name,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#countFindByName(java.
	 * lang.String, boolean)
	 */
	public abstract int countFindByName(String name, boolean exactMatch)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#findAllNames()
	 */
	public abstract Collection<String> findAllNames()
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByDataContainerTypeAndName(java.lang.String, java.lang.String,
	 * boolean, java.lang.String, java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByDataContainerTypeAndName(
			String dataContainerType, String name, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * countFindByDataContainerTypeAndName(java.lang.String, java.lang.String,
	 * boolean)
	 */
	public abstract int countFindByDataContainerTypeAndName(
			String dataContainerType, String name, boolean exactMatch)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findWithDataWithinTimeWindow
	 * (java.util.Date, boolean, java.util.Date, boolean, java.lang.String,
	 * java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findWithDataWithinTimeWindow(
			Date startDate, boolean allDataAfterStartDate, Date endDate,
			boolean allDataBeforeEndDate, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByURIString(java.
	 * lang.String, boolean)
	 */
	public abstract DataContainer findByURIString(String uriString,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByURI(java.net.URI,
	 * java.lang.String, java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByURI(URI uri,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByURL(java.net.URL,
	 * java.lang.String, java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByURL(URL url,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#findAllURIStrings()
	 */
	public abstract Collection<String> findAllURIStrings()
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByDODSURLString(java
	 * .lang.String, boolean)
	 */
	public abstract DataContainer findByDODSURLString(String dodsUrlString,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByDODSURLString(java
	 * .lang.String, boolean, java.lang.String, java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByDODSURLString(
			String dodsUrlString, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#countFindByDODSURLString
	 * (java.lang.String, boolean)
	 */
	public abstract int countFindByDODSURLString(String dodsUrlString,
			boolean exactMatch) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByMimeType(java.lang
	 * .String, java.lang.String, java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByMimeType(String mimeType,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findWithinGeospatialCube
	 * (java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Float, java.lang.Float, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public abstract Collection<DataContainer> findWithinGeospatialCube(
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findWithinTimeAndGeospatialCube(java.util.Date, java.util.Date,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Float, java.lang.Float, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public abstract Collection<DataContainer> findWithinTimeAndGeospatialCube(
			Date startDate, Date endDate, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByPerson(moos.ssds
	 * .metadata.Person, java.lang.String, java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByPerson(Person person,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByRecordVariable(
	 * moos.ssds.metadata.RecordVariable, boolean)
	 */
	public abstract DataContainer findByRecordVariable(
			RecordVariable recordVariable, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByRecordVariableName
	 * (java.lang.String, boolean, java.lang.String, java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByRecordVariableName(
			String recordVariableName, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByLikeRecordVariableName
	 * (java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByLikeRecordVariableName(
			String likeRecordVariableName, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByStandardVariableName
	 * (java.lang.String, boolean, java.lang.String, java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByStandardVariableName(
			String standardVariableName, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByLikeStandardVariableName(java.lang.String, java.lang.String,
	 * java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByLikeStandardVariableName(
			String likeStandardVariableName, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByDataContainerGroup
	 * (moos.ssds.metadata.DataContainerGroup, java.lang.String,
	 * java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByDataContainerGroup(
			DataContainerGroup dataContainerGroup, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByDataContainerGroupName
	 * (java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByDataContainerGroupName(
			String dataContainerGroupName, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByLikeDataContainerGroupName(java.lang.String, java.lang.String,
	 * java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByLikeDataContainerGroupName(
			String likeDataContainerGroupName, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByKeywordName(java
	 * .lang.String, boolean, java.lang.String, java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByKeywordName(
			String keywordName, boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#countFindByKeywordName
	 * (java.lang.String, boolean, java.lang.String, java.lang.String, boolean)
	 */
	public abstract int countFindByKeywordName(String keywordName,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByResource(moos.ssds
	 * .metadata.Resource, java.lang.String, java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByResource(Resource resource,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findAllIndirectCreators
	 * (moos.ssds.metadata.DataContainer, int, java.lang.String,
	 * java.lang.String, boolean)
	 */
	public abstract Collection<DataProducer> findAllIndirectCreators(
			DataContainer dataContainer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findCreatorChain(moos
	 * .ssds.metadata.DataContainer, int, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public abstract Collection<DataProducer> findCreatorChain(
			DataContainer dataContainer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findAllIndirectConsumers
	 * (moos.ssds.metadata.DataContainer, int, java.lang.String,
	 * java.lang.String, boolean)
	 */
	public abstract Collection<DataProducer> findAllIndirectConsumers(
			DataContainer dataContainer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findAllConsumers(moos
	 * .ssds.metadata.DataContainer, int, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public abstract Collection<DataProducer> findAllConsumers(
			DataContainer dataContainer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findDirectInputs(moos
	 * .ssds.metadata.DataContainer, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public abstract Collection<DataContainer> findDirectInputs(
			DataContainer dataContainer, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findAllInputs(moos.ssds
	 * .metadata.DataContainer, int, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public abstract Collection<DataContainer> findAllInputs(
			DataContainer dataContainer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findAllDerivedOutputs
	 * (moos.ssds.metadata.DataContainer, int, java.lang.String,
	 * java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findAllDerivedOutputs(
			DataContainer dataContainer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findAllDerivedOutputs
	 * (moos.ssds.metadata.DataProducer, int, java.lang.String,
	 * java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findAllDerivedOutputs(
			DataProducer dataProducer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findInputsByDataProducer
	 * (moos.ssds.metadata.DataProducer, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public abstract Collection<DataContainer> findInputsByDataProducer(
			DataProducer dataProducer, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findOutputsByDataProducer
	 * (moos.ssds.metadata.DataProducer, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public abstract Collection<DataContainer> findOutputsByDataProducer(
			DataProducer dataProducer, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findBestDirectOutput(
	 * moos.ssds.metadata.DataProducer, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public abstract DataContainer findBestDirectOutput(
			DataProducer dataProducer, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByRecordVariableNameAndDataWithinTimeWindow(java.lang.String,
	 * java.util.Date, java.util.Date, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public abstract Collection<DataContainer> findByRecordVariableNameAndDataWithinTimeWindow(
			String recordVariableName, Date startDate, Date endDate,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByLikeRecordVariableNameAndDataWithinTimeWindow(java.lang.String,
	 * java.util.Date, java.util.Date, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public abstract Collection<DataContainer> findByLikeRecordVariableNameAndDataWithinTimeWindow(
			String likeRecordVariableName, Date startDate, Date endDate,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByStandardVariableNameAndDataWithinTimeWindow(java.lang.String,
	 * java.util.Date, java.util.Date, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public abstract Collection<DataContainer> findByStandardVariableNameAndDataWithinTimeWindow(
			String standardVariableName, Date startDate, Date endDate,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByLikeStandardVariableNameAndDataWithinTimeWindow(java.lang.String,
	 * java.util.Date, java.util.Date, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public abstract Collection<DataContainer> findByLikeStandardVariableNameAndDataWithinTimeWindow(
			String likeStandardVariableName, Date startDate, Date endDate,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByRecordVariableNameAndWithinGeospatialCube(java.lang.String,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Float, java.lang.Float, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public abstract Collection<DataContainer> findByRecordVariableNameAndWithinGeospatialCube(
			String recordVariableName, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByLikeRecordVariableNameAndWithinGeospatialCube(java.lang.String,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Float, java.lang.Float, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public abstract Collection<DataContainer> findByLikeRecordVariableNameAndWithinGeospatialCube(
			String likeRecordVariableName, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByStandardVariableNameAndWithinGeospatialCube(java.lang.String,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Float, java.lang.Float, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public abstract Collection<DataContainer> findByStandardVariableNameAndWithinGeospatialCube(
			String standardVariableName, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByLikeStandardVariableNameAndWithinGeospatialCube(java.lang.String,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Float, java.lang.Float, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public abstract Collection<DataContainer> findByLikeStandardVariableNameAndWithinGeospatialCube(
			String likeStandardVariableName, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByRecordVariableNameWithinTimeAndWithinGeospatialCube
	 * (java.lang.String, java.util.Date, java.util.Date, java.lang.Double,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Float,
	 * java.lang.Float, java.lang.String, java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByRecordVariableNameWithinTimeAndWithinGeospatialCube(
			String recordVariableName, Date startDate, Date endDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByLikeRecordVariableNameWithinTimeAndWithinGeospatialCube
	 * (java.lang.String, java.util.Date, java.util.Date, java.lang.Double,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Float,
	 * java.lang.Float, java.lang.String, java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByLikeRecordVariableNameWithinTimeAndWithinGeospatialCube(
			String likeRecordVariableName, Date startDate, Date endDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByStandardVariableNameWithinTimeAndWithinGeospatialCube
	 * (java.lang.String, java.util.Date, java.util.Date, java.lang.Double,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Float,
	 * java.lang.Float, java.lang.String, java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByStandardVariableNameWithinTimeAndWithinGeospatialCube(
			String standardVariableName, Date startDate, Date endDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByLikeStandardVariableNameWithinTimeAndWithinGeospatialCube
	 * (java.lang.String, java.util.Date, java.util.Date, java.lang.Double,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Float,
	 * java.lang.Float, java.lang.String, java.lang.String, boolean)
	 */
	public abstract Collection<DataContainer> findByLikeStandardVariableNameWithinTimeAndWithinGeospatialCube(
			String likeStandardVariableName, Date startDate, Date endDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

}