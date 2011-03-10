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

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.CreateException;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import moos.ssds.dao.DataContainerDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataContainerGroup;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.Resource;

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * Provides a facade that provides client services for DataContainer objects.
 * 
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.19 $
 */
@Stateless
@Local(DataContainerAccessLocal.class)
@LocalBinding(jndiBinding = "moos/ssds/services/metadata/DataContainerAccessLocal")
@Remote(DataContainerAccess.class)
@RemoteBinding(jndiBinding = "moos/ssds/services/metadata/DataContainerAccess")
public class DataContainerAccessEJB extends AccessBean implements
		DataContainerAccess, DataContainerAccessLocal {

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(DataContainerAccessEJB.class);

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

		// Now set the super persistent class to DataContainer
		super.setPersistentClass(DataContainer.class);
		logger.debug("OK, set Persistent class to DataContainer");

		// And the DAO
		super.setDaoClass(DataContainerDAO.class);
		logger.debug("OK, set DAO Class to DataContainerDAO");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByName(java.lang.
	 * String, boolean, java.lang.String, java.lang.String, boolean)
	 */
	public Collection<DataContainer> findByName(String name,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findByName(name, exactMatch,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#countFindByName(java.
	 * lang.String, boolean)
	 */
	public int countFindByName(String name, boolean exactMatch)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.countFindByName(name, exactMatch);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#findAllNames()
	 */
	public Collection<String> findAllNames() throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findAllNames();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByDataContainerTypeAndName(java.lang.String, java.lang.String,
	 * boolean, java.lang.String, java.lang.String, boolean)
	 */
	public Collection<DataContainer> findByDataContainerTypeAndName(
			String dataContainerType, String name, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findByDataContainerTypeAndName(
				dataContainerType, name, exactMatch, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * countFindByDataContainerTypeAndName(java.lang.String, java.lang.String,
	 * boolean)
	 */
	public int countFindByDataContainerTypeAndName(String dataContainerType,
			String name, boolean exactMatch) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.countFindByDataContainerTypeAndName(
				dataContainerType, name, exactMatch);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findWithDataWithinTimeWindow
	 * (java.util.Date, boolean, java.util.Date, boolean, java.lang.String,
	 * java.lang.String, boolean)
	 */
	public Collection<DataContainer> findWithDataWithinTimeWindow(
			Date startDate, boolean allDataAfterStartDate, Date endDate,
			boolean allDataBeforeEndDate, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findWithDataWithinTimeWindow(startDate,
				allDataAfterStartDate, endDate, allDataBeforeEndDate,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByURIString(java.
	 * lang.String, boolean)
	 */
	public DataContainer findByURIString(String uriString,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now find the dataContainers ID
		return dataContainerDAO.findByURIString(uriString,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByURI(java.net.URI,
	 * java.lang.String, java.lang.String, boolean)
	 */
	public Collection<DataContainer> findByURI(URI uri,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findByURI(uri, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByURL(java.net.URL,
	 * java.lang.String, java.lang.String, boolean)
	 */
	public Collection<DataContainer> findByURL(URL url,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findByURL(url, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#findAllURIStrings()
	 */
	public Collection<String> findAllURIStrings()
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findAllURIStrings();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByDODSURLString(java
	 * .lang.String, boolean)
	 */
	public DataContainer findByDODSURLString(String dodsUrlString,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findByDODSURLString(dodsUrlString,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByDODSURLString(java
	 * .lang.String, boolean, java.lang.String, java.lang.String, boolean)
	 */
	public Collection<DataContainer> findByDODSURLString(String dodsUrlString,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		return dataContainerDAO.findByDODSURLString(dodsUrlString, exactMatch,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#countFindByDODSURLString
	 * (java.lang.String, boolean)
	 */
	public int countFindByDODSURLString(String dodsUrlString, boolean exactMatch)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call and return result
		return dataContainerDAO.countFindByDODSURLString(dodsUrlString,
				exactMatch);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByMimeType(java.lang
	 * .String, java.lang.String, java.lang.String, boolean)
	 */
	public Collection<DataContainer> findByMimeType(String mimeType,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findByMimeType(mimeType, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findWithinGeospatialCube
	 * (java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Float, java.lang.Float, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public Collection<DataContainer> findWithinGeospatialCube(
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findWithinGeospatialCube(geospatialLatMin,
				geospatialLatMax, geospatialLonMin, geospatialLonMax,
				geospatialVerticalMin, geospatialVerticalMax,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findWithinTimeAndGeospatialCube(java.util.Date, java.util.Date,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Float, java.lang.Float, java.lang.String, java.lang.String,
	 * boolean)
	 */
	public Collection<DataContainer> findWithinTimeAndGeospatialCube(
			Date startDate, Date endDate, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findWithinTimeAndGeospatialCube(startDate,
				endDate, geospatialLatMin, geospatialLatMax, geospatialLonMin,
				geospatialLonMax, geospatialVerticalMin, geospatialVerticalMax,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByPerson(moos.ssds
	 * .metadata.Person, java.lang.String, java.lang.String, boolean)
	 */

	public Collection<DataContainer> findByPerson(Person person,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findByPerson(person, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByRecordVariable(
	 * moos.ssds.metadata.RecordVariable, boolean)
	 */

	public DataContainer findByRecordVariable(RecordVariable recordVariable,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findByRecordVariable(recordVariable,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByRecordVariableName
	 * (java.lang.String, boolean, java.lang.String, java.lang.String, boolean)
	 */

	public Collection<DataContainer> findByRecordVariableName(
			String recordVariableName, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findByRecordVariableName(recordVariableName,
				exactMatch, orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByLikeRecordVariableName
	 * (java.lang.String, java.lang.String, java.lang.String, boolean)
	 */

	public Collection<DataContainer> findByLikeRecordVariableName(
			String likeRecordVariableName, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findByLikeRecordVariableName(
				likeRecordVariableName, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByStandardVariableName
	 * (java.lang.String, boolean, java.lang.String, java.lang.String, boolean)
	 */

	public Collection<DataContainer> findByStandardVariableName(
			String standardVariableName, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findByStandardVariableName(
				standardVariableName, exactMatch, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByLikeStandardVariableName(java.lang.String, java.lang.String,
	 * java.lang.String, boolean)
	 */

	public Collection<DataContainer> findByLikeStandardVariableName(
			String likeStandardVariableName, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findByLikeStandardVariableName(
				likeStandardVariableName, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByDataContainerGroup
	 * (moos.ssds.metadata.DataContainerGroup, java.lang.String,
	 * java.lang.String, boolean)
	 */

	public Collection<DataContainer> findByDataContainerGroup(
			DataContainerGroup dataContainerGroup, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findByDataContainerGroup(dataContainerGroup,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByDataContainerGroupName
	 * (java.lang.String, java.lang.String, java.lang.String, boolean)
	 */

	public Collection<DataContainer> findByDataContainerGroupName(
			String dataContainerGroupName, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findByDataContainerGroupName(
				dataContainerGroupName, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByLikeDataContainerGroupName(java.lang.String, java.lang.String,
	 * java.lang.String, boolean)
	 */

	public Collection<DataContainer> findByLikeDataContainerGroupName(
			String likeDataContainerGroupName, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findByLikeDataContainerGroupName(
				likeDataContainerGroupName, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByKeywordName(java
	 * .lang.String, boolean, java.lang.String, java.lang.String, boolean)
	 */

	public Collection<DataContainer> findByKeywordName(String keywordName,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findByKeywordName(keywordName, exactMatch,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#countFindByKeywordName
	 * (java.lang.String, boolean, java.lang.String, java.lang.String, boolean)
	 */

	public int countFindByKeywordName(String keywordName, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.countFindByKeywordName(keywordName, exactMatch);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findByResource(moos.ssds
	 * .metadata.Resource, java.lang.String, java.lang.String, boolean)
	 */

	public Collection<DataContainer> findByResource(Resource resource,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findByResource(resource, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findAllIndirectCreators
	 * (moos.ssds.metadata.DataContainer, int, java.lang.String,
	 * java.lang.String, boolean)
	 */

	public Collection<DataProducer> findAllIndirectCreators(
			DataContainer dataContainer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findAllIndirectCreators(dataContainer,
				fetchDepth, orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findCreatorChain(moos
	 * .ssds.metadata.DataContainer, int, java.lang.String, java.lang.String,
	 * boolean)
	 */

	public Collection<DataProducer> findCreatorChain(
			DataContainer dataContainer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findCreatorChain(dataContainer, fetchDepth,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findAllIndirectConsumers
	 * (moos.ssds.metadata.DataContainer, int, java.lang.String,
	 * java.lang.String, boolean)
	 */

	public Collection<DataProducer> findAllIndirectConsumers(
			DataContainer dataContainer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findAllIndirectConsumers(dataContainer,
				fetchDepth, orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findAllConsumers(moos
	 * .ssds.metadata.DataContainer, int, java.lang.String, java.lang.String,
	 * boolean)
	 */

	public Collection<DataProducer> findAllConsumers(
			DataContainer dataContainer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findAllConsumers(dataContainer, fetchDepth,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findDirectInputs(moos
	 * .ssds.metadata.DataContainer, java.lang.String, java.lang.String,
	 * boolean)
	 */

	public Collection<DataContainer> findDirectInputs(
			DataContainer dataContainer, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findDirectInputs(dataContainer,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findAllInputs(moos.ssds
	 * .metadata.DataContainer, int, java.lang.String, java.lang.String,
	 * boolean)
	 */

	public Collection<DataContainer> findAllInputs(DataContainer dataContainer,
			int fetchDepth, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findAllInputs(dataContainer, fetchDepth,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findAllDerivedOutputs
	 * (moos.ssds.metadata.DataContainer, int, java.lang.String,
	 * java.lang.String, boolean)
	 */

	public Collection<DataContainer> findAllDerivedOutputs(
			DataContainer dataContainer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findAllDerivedOutputs(dataContainer,
				fetchDepth, orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findAllDerivedOutputs
	 * (moos.ssds.metadata.DataProducer, int, java.lang.String,
	 * java.lang.String, boolean)
	 */

	public Collection<DataContainer> findAllDerivedOutputs(
			DataProducer dataProducer, int fetchDepth,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findAllDerivedOutputs(dataProducer, fetchDepth,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findInputsByDataProducer
	 * (moos.ssds.metadata.DataProducer, java.lang.String, java.lang.String,
	 * boolean)
	 */

	public Collection<DataContainer> findInputsByDataProducer(
			DataProducer dataProducer, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findInputsByDataProducer(dataProducer,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findOutputsByDataProducer
	 * (moos.ssds.metadata.DataProducer, java.lang.String, java.lang.String,
	 * boolean)
	 */

	public Collection<DataContainer> findOutputsByDataProducer(
			DataProducer dataProducer, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findOutputsByDataProducer(dataProducer,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerAccess#findBestDirectOutput(
	 * moos.ssds.metadata.DataProducer, java.lang.String, java.lang.String,
	 * boolean)
	 */

	public DataContainer findBestDirectOutput(DataProducer dataProducer,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO.findBestDirectOutput(dataProducer,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByRecordVariableNameAndDataWithinTimeWindow(java.lang.String,
	 * java.util.Date, java.util.Date, java.lang.String, java.lang.String,
	 * boolean)
	 */

	public Collection<DataContainer> findByRecordVariableNameAndDataWithinTimeWindow(
			String recordVariableName, Date startDate, Date endDate,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO
				.findByRecordVariableNameAndDataWithinTimeWindow(
						recordVariableName, startDate, endDate,
						orderByPropertyName, ascendingOrDescending,
						returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByLikeRecordVariableNameAndDataWithinTimeWindow(java.lang.String,
	 * java.util.Date, java.util.Date, java.lang.String, java.lang.String,
	 * boolean)
	 */

	public Collection<DataContainer> findByLikeRecordVariableNameAndDataWithinTimeWindow(
			String likeRecordVariableName, Date startDate, Date endDate,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO
				.findByLikeRecordVariableNameAndDataWithinTimeWindow(
						likeRecordVariableName, startDate, endDate,
						orderByPropertyName, ascendingOrDescending,
						returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByStandardVariableNameAndDataWithinTimeWindow(java.lang.String,
	 * java.util.Date, java.util.Date, java.lang.String, java.lang.String,
	 * boolean)
	 */

	public Collection<DataContainer> findByStandardVariableNameAndDataWithinTimeWindow(
			String standardVariableName, Date startDate, Date endDate,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO
				.findByStandardVariableNameAndDataWithinTimeWindow(
						standardVariableName, startDate, endDate,
						orderByPropertyName, ascendingOrDescending,
						returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByLikeStandardVariableNameAndDataWithinTimeWindow(java.lang.String,
	 * java.util.Date, java.util.Date, java.lang.String, java.lang.String,
	 * boolean)
	 */

	public Collection<DataContainer> findByLikeStandardVariableNameAndDataWithinTimeWindow(
			String likeStandardVariableName, Date startDate, Date endDate,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO
				.findByLikeStandardVariableNameAndDataWithinTimeWindow(
						likeStandardVariableName, startDate, endDate,
						orderByPropertyName, ascendingOrDescending,
						returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByRecordVariableNameAndWithinGeospatialCube(java.lang.String,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Float, java.lang.Float, java.lang.String, java.lang.String,
	 * boolean)
	 */

	public Collection<DataContainer> findByRecordVariableNameAndWithinGeospatialCube(
			String recordVariableName, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO
				.findByRecordVariableNameAndWithinGeospatialCube(
						recordVariableName, geospatialLatMin, geospatialLatMax,
						geospatialLonMin, geospatialLonMax,
						geospatialVerticalMin, geospatialVerticalMax,
						orderByPropertyName, ascendingOrDescending,
						returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByLikeRecordVariableNameAndWithinGeospatialCube(java.lang.String,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Float, java.lang.Float, java.lang.String, java.lang.String,
	 * boolean)
	 */

	public Collection<DataContainer> findByLikeRecordVariableNameAndWithinGeospatialCube(
			String likeRecordVariableName, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO
				.findByLikeRecordVariableNameAndWithinGeospatialCube(
						likeRecordVariableName, geospatialLatMin,
						geospatialLatMax, geospatialLonMin, geospatialLonMax,
						geospatialVerticalMin, geospatialVerticalMax,
						orderByPropertyName, ascendingOrDescending,
						returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByStandardVariableNameAndWithinGeospatialCube(java.lang.String,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Float, java.lang.Float, java.lang.String, java.lang.String,
	 * boolean)
	 */

	public Collection<DataContainer> findByStandardVariableNameAndWithinGeospatialCube(
			String standardVariableName, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO
				.findByStandardVariableNameAndWithinGeospatialCube(
						standardVariableName, geospatialLatMin,
						geospatialLatMax, geospatialLonMin, geospatialLonMax,
						geospatialVerticalMin, geospatialVerticalMax,
						orderByPropertyName, ascendingOrDescending,
						returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByLikeStandardVariableNameAndWithinGeospatialCube(java.lang.String,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Float, java.lang.Float, java.lang.String, java.lang.String,
	 * boolean)
	 */

	public Collection<DataContainer> findByLikeStandardVariableNameAndWithinGeospatialCube(
			String likeStandardVariableName, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO
				.findByLikeStandardVariableNameAndWithinGeospatialCube(
						likeStandardVariableName, geospatialLatMin,
						geospatialLatMax, geospatialLonMin, geospatialLonMax,
						geospatialVerticalMin, geospatialVerticalMax,
						orderByPropertyName, ascendingOrDescending,
						returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByRecordVariableNameWithinTimeAndWithinGeospatialCube
	 * (java.lang.String, java.util.Date, java.util.Date, java.lang.Double,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Float,
	 * java.lang.Float, java.lang.String, java.lang.String, boolean)
	 */

	public Collection<DataContainer> findByRecordVariableNameWithinTimeAndWithinGeospatialCube(
			String recordVariableName, Date startDate, Date endDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO
				.findByRecordVariableNameWithinTimeAndWithinGeospatialCube(
						recordVariableName, startDate, endDate,
						geospatialLatMin, geospatialLatMax, geospatialLonMin,
						geospatialLonMax, geospatialVerticalMin,
						geospatialVerticalMax, orderByPropertyName,
						ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByLikeRecordVariableNameWithinTimeAndWithinGeospatialCube
	 * (java.lang.String, java.util.Date, java.util.Date, java.lang.Double,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Float,
	 * java.lang.Float, java.lang.String, java.lang.String, boolean)
	 */

	public Collection<DataContainer> findByLikeRecordVariableNameWithinTimeAndWithinGeospatialCube(
			String likeRecordVariableName, Date startDate, Date endDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO
				.findByLikeRecordVariableNameWithinTimeAndWithinGeospatialCube(
						likeRecordVariableName, startDate, endDate,
						geospatialLatMin, geospatialLatMax, geospatialLonMin,
						geospatialLonMax, geospatialVerticalMin,
						geospatialVerticalMax, orderByPropertyName,
						ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByStandardVariableNameWithinTimeAndWithinGeospatialCube
	 * (java.lang.String, java.util.Date, java.util.Date, java.lang.Double,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Float,
	 * java.lang.Float, java.lang.String, java.lang.String, boolean)
	 */
	public Collection<DataContainer> findByStandardVariableNameWithinTimeAndWithinGeospatialCube(
			String standardVariableName, Date startDate, Date endDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO
				.findByStandardVariableNameWithinTimeAndWithinGeospatialCube(
						standardVariableName, startDate, endDate,
						geospatialLatMin, geospatialLatMax, geospatialLonMin,
						geospatialLonMax, geospatialVerticalMin,
						geospatialVerticalMax, orderByPropertyName,
						ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerAccess#
	 * findByLikeStandardVariableNameWithinTimeAndWithinGeospatialCube
	 * (java.lang.String, java.util.Date, java.util.Date, java.lang.Double,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Float,
	 * java.lang.Float, java.lang.String, java.lang.String, boolean)
	 */
	public Collection<DataContainer> findByLikeStandardVariableNameWithinTimeAndWithinGeospatialCube(
			String likeStandardVariableName, Date startDate, Date endDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataContainerDAO dataContainerDAO = (DataContainerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerDAO
				.findByLikeStandardVariableNameWithinTimeAndWithinGeospatialCube(
						likeStandardVariableName, startDate, endDate,
						geospatialLatMin, geospatialLatMax, geospatialLonMin,
						geospatialLonMax, geospatialVerticalMin,
						geospatialVerticalMax, orderByPropertyName,
						ascendingOrDescending, returnFullObjectGraph);
	}
}