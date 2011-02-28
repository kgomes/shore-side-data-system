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
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.CreateException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

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

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * Provides a facade that provides client services for DataProducer objects.
 * 
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.25 $
 */
@Stateless
@RemoteBinding(jndiBinding = "moos/ssds/services/metadata/DataProducerAccess")
@LocalBinding(jndiBinding = "moos/ssds/services/metadata/DataProducerAccessLocal")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class DataProducerAccessEJB extends AccessBean implements
		DataProducerAccess, DataProducerAccessLocal {

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(DataProducerAccessEJB.class);

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

		// Now set the super persistent class to DataProducer
		super.setPersistentClass(DataProducer.class);
		logger.debug("OK, set Persistent class to DataProducer");

		// And the DAO
		super.setDaoClass(DataProducerDAO.class);
		logger.debug("OK, set DAO Class to DataProducerDAO");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByProperties(java.
	 * lang.String, boolean, java.lang.String, java.util.Date, boolean,
	 * java.util.Date, boolean, java.lang.Double, java.lang.Double,
	 * java.lang.Double, java.lang.Double, java.lang.Float, java.lang.Float,
	 * java.lang.Float, java.lang.Float, java.lang.String, boolean,
	 * java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findByProperties(String name,
			boolean exactMatch, String dataProducerType, Date startDate,
			boolean boundedByStartDate, Date endDate, boolean boundedByEndDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialDepthMin, Float geospatialDepthMax,
			Float geospatialBenthicAltitudeMin,
			Float geospatialBenthicAltitudeMax, String hostName,
			boolean exactHostNameMatch, String orderByProperty,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByProperties(name, exactMatch,
				dataProducerType, startDate, boundedByStartDate, endDate,
				boundedByEndDate, geospatialLatMin, geospatialLatMax,
				geospatialLonMin, geospatialLonMax, geospatialDepthMin,
				geospatialDepthMax, geospatialBenthicAltitudeMin,
				geospatialBenthicAltitudeMax, hostName, exactHostNameMatch,
				orderByProperty, ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#countFindByProperties(
	 * java.lang.String, boolean, java.lang.String, java.util.Date, boolean,
	 * java.util.Date, boolean, java.lang.Double, java.lang.Double,
	 * java.lang.Double, java.lang.Double, java.lang.Float, java.lang.Float,
	 * java.lang.Float, java.lang.Float, java.lang.String, boolean)
	 */
	@Override
	public int countFindByProperties(String name, boolean exactMatch,
			String dataProducerType, Date startDate,
			boolean boundedByStartDate, Date endDate, boolean boundedByEndDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialDepthMin, Float geospatialDepthMax,
			Float geospatialBenthicAltitudeMin,
			Float geospatialBenthicAltitudeMax, String hostName,
			boolean exactHostNameMatch) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindByProperties(name, exactMatch,
				dataProducerType, startDate, boundedByStartDate, endDate,
				boundedByEndDate, geospatialLatMin, geospatialLatMax,
				geospatialLonMin, geospatialLonMax, geospatialDepthMin,
				geospatialDepthMax, geospatialBenthicAltitudeMin,
				geospatialBenthicAltitudeMax, hostName, exactHostNameMatch);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByName(java.lang.String
	 * , boolean, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findByName(String name, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByName(name, exactMatch,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#countFindByName(java.lang
	 * .String, boolean)
	 */
	@Override
	public int countFindByName(String name, boolean exactMatch)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindByName(name, exactMatch);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByDataProducerTypeAndName
	 * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * boolean, boolean)
	 */
	@Override
	public Collection<DataProducer> findByDataProducerTypeAndName(
			String dataProducerType, String name, String orderByPropertyName,
			String ascendingOrDescending, boolean exactMatch,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByDataProducerTypeAndName(dataProducerType,
				name, exactMatch, orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataProducerAccess#
	 * countFindByDataProducerTypeAndName(java.lang.String, java.lang.String,
	 * boolean)
	 */
	@Override
	public int countFindByDataProducerTypeAndName(String dataProducerType,
			String name, boolean exactMatch) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindByDataProducerTypeAndName(
				dataProducerType, name, exactMatch);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findParentlessDeployments
	 * (java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findParentlessDeployments(
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findParentlessDeployments(orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#countFindParentlessDeployments
	 * ()
	 */
	@Override
	public int countFindParentlessDeployments() throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindParentlessDeployments();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findParentlessDataProducers
	 * (java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findParentlessDataProducers(
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findParentlessDataProducers(orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataProducerAccess#
	 * countFindParentlessDataProducers()
	 */
	@Override
	public int countFindParentlessDataProducers()
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindParentlessDataProducers();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByDateRangeAndName
	 * (java.util.Date, boolean, java.util.Date, boolean, java.lang.String,
	 * boolean, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findByDateRangeAndName(Date startDate,
			boolean boundedByStartDate, Date endDate, boolean boundedByEndDate,
			String name, boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByDateRangeAndName(startDate,
				boundedByStartDate, endDate, boundedByEndDate, name,
				exactMatch, orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#countFindByDateRangeAndName
	 * (java.util.Date, boolean, java.util.Date, boolean, java.lang.String,
	 * boolean)
	 */
	@Override
	public int countFindByDateRangeAndName(Date startDate,
			boolean boundedByStartDate, Date endDate, boolean boundedByEndDate,
			String name, boolean exactMatch) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO
				.countFindByDateRangeAndName(startDate, boundedByStartDate,
						endDate, boundedByEndDate, name, exactMatch);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByGeospatialCube(java
	 * .lang.Double, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Float, java.lang.Float, java.lang.String, java.lang.String,
	 * boolean)
	 */
	@Override
	public Collection<DataProducer> findByGeospatialCube(
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByGeospatialCube(geospatialLatMin,
				geospatialLatMax, geospatialLonMin, geospatialLonMax,
				geospatialVerticalMin, geospatialVerticalMax,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#countFindByGeospatialCube
	 * (java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Float, java.lang.Float)
	 */
	@Override
	public int countFindByGeospatialCube(Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindByGeospatialCube(geospatialLatMin,
				geospatialLatMax, geospatialLonMin, geospatialLonMax,
				geospatialVerticalMin, geospatialVerticalMax);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByTimeAndGeospatialCube
	 * (java.util.Date, boolean, java.util.Date, boolean, java.lang.Double,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Float,
	 * java.lang.Float, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findByTimeAndGeospatialCube(Date startDate,
			boolean boundedByStartDate, Date endDate, boolean boundedByEndDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByTimeAndGeospatialCube(startDate,
				boundedByStartDate, endDate, boundedByEndDate,
				geospatialLatMin, geospatialLatMax, geospatialLonMin,
				geospatialLonMax, geospatialVerticalMin, geospatialVerticalMax,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataProducerAccess#
	 * countFindByTimeAndGeospatialCube(java.util.Date, boolean, java.util.Date,
	 * boolean, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Double, java.lang.Float, java.lang.Float)
	 */
	@Override
	public int countFindByTimeAndGeospatialCube(Date startDate,
			boolean boundedByStartDate, Date endDate, boolean boundedByEndDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax)
			throws MetadataAccessException { // Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindByTimeAndGeospatialCube(startDate,
				boundedByStartDate, endDate, boundedByEndDate,
				geospatialLatMin, geospatialLatMax, geospatialLonMin,
				geospatialLonMax, geospatialVerticalMin, geospatialVerticalMax);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByNameAndGeospatialCube
	 * (java.lang.String, boolean, java.lang.Double, java.lang.Double,
	 * java.lang.Double, java.lang.Double, java.lang.Float, java.lang.Float,
	 * java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findByNameAndGeospatialCube(String name,
			boolean exactMatch, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByNameAndGeospatialCube(name, exactMatch,
				geospatialLatMin, geospatialLatMax, geospatialLonMin,
				geospatialLonMax, geospatialVerticalMin, geospatialVerticalMax,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataProducerAccess#
	 * countFindByNameAndGeospatialCube(java.lang.String, boolean,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Float, java.lang.Float)
	 */
	@Override
	public int countFindByNameAndGeospatialCube(String name,
			boolean exactMatch, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindByNameAndGeospatialCube(name,
				exactMatch, geospatialLatMin, geospatialLatMax,
				geospatialLonMin, geospatialLonMax, geospatialVerticalMin,
				geospatialVerticalMax);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataProducerAccess#
	 * findByNameAndTimeAndGeospatialCube(java.lang.String, boolean,
	 * java.util.Date, boolean, java.util.Date, boolean, java.lang.Double,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Float,
	 * java.lang.Float, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findByNameAndTimeAndGeospatialCube(
			String name, boolean exactMatch, Date startDate,
			boolean boundedByStartDate, Date endDate, boolean boundedByEndDate,
			Double geospatialLatMin, Double geospatialLatMax,
			Double geospatialLonMin, Double geospatialLonMax,
			Float geospatialVerticalMin, Float geospatialVerticalMax,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByNameAndTimeAndGeospatialCube(name,
				exactMatch, startDate, boundedByStartDate, endDate,
				boundedByEndDate, geospatialLatMin, geospatialLatMax,
				geospatialLonMin, geospatialLonMax, geospatialVerticalMin,
				geospatialVerticalMax, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataProducerAccess#
	 * countFindByNameAndTimeAndGeospatialCube(java.lang.String, boolean,
	 * java.util.Date, boolean, java.util.Date, boolean, java.lang.Double,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Float,
	 * java.lang.Float)
	 */
	@Override
	public int countFindByNameAndTimeAndGeospatialCube(String name,
			boolean exactMatch, Date startDate, boolean boundedByStartDate,
			Date endDate, boolean boundedByEndDate, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindByNameAndTimeAndGeospatialCube(name,
				exactMatch, startDate, boundedByStartDate, endDate,
				boundedByEndDate, geospatialLatMin, geospatialLatMax,
				geospatialLonMin, geospatialLonMax, geospatialVerticalMin,
				geospatialVerticalMax);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByHostName(java.lang
	 * .String, boolean, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findByHostName(String hostName,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByHostName(hostName, exactMatch,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#countFindByHostName(java
	 * .lang.String, boolean)
	 */
	@Override
	public int countFindByHostName(String hostName, boolean exactMatch)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindByHostName(hostName, exactMatch);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByPerson(moos.ssds
	 * .metadata.Person, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findByPerson(Person person,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByPerson(person, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#countFindByPerson(moos
	 * .ssds.metadata.Person)
	 */
	@Override
	public int countFindByPerson(Person person) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindByPerson(person);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByDevice(moos.ssds
	 * .metadata.Device, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findByDevice(Device device,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByDevice(device, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByDeviceId(java.lang
	 * .Long, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findByDeviceId(Long deviceId,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByDeviceId(deviceId, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByDeviceAndTimeWindow
	 * (moos.ssds.metadata.Device, java.util.Date, java.util.Date,
	 * java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findByDeviceAndTimeWindow(Device device,
			Date startDate, Date endDate, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByDeviceAndTimeWindow(device, startDate,
				endDate, orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByDeviceTypeName(java
	 * .lang.String, boolean, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findByDeviceTypeName(String deviceTypeName,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByDeviceTypeName(deviceTypeName, exactMatch,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataProducerAccess#
	 * findCurrentParentlessDeployments(java.lang.String, java.lang.String,
	 * boolean)
	 */
	@Override
	public Collection<DataProducer> findCurrentParentlessDeployments(
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now return the call
		return dataProducerDAO.findCurrentParentlessDeployments(
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataProducerAccess#
	 * findParentlessDeploymentsByName(java.lang.String, boolean,
	 * java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findParentlessDeploymentsByName(
			String name, boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now return the call
		return dataProducerDAO.findParentlessDeploymentsByName(name,
				exactMatch, orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findCurrentDeployments
	 * (java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findCurrentDeployments(
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findCurrentDeployments(orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findCurrentDeploymentsOfDevice
	 * (moos.ssds.metadata.Device, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findCurrentDeploymentsOfDevice(
			Device device, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findCurrentDeploymentsOfDevice(device,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataProducerAccess#
	 * findCurrentDeploymentsOfDeviceId(java.lang.Long, java.lang.String,
	 * java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findCurrentDeploymentsOfDeviceId(
			Long deviceId, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findCurrentDeploymentsOfDeviceId(deviceId,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findCurrentDeploymentsByRole
	 * (java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findCurrentDeploymentsByRole(String role,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException,
			MetadataException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findCurrentDeploymentsByRole(role,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataProducerAccess#
	 * findCurrentDeploymentsByRoleAndName(java.lang.String, java.lang.String,
	 * boolean, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findCurrentDeploymentsByRoleAndName(
			String role, String name, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException,
			MetadataException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findCurrentDeploymentsByRoleAndName(role, name,
				exactMatch, orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findBySoftware(moos.ssds
	 * .metadata.Software, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findBySoftware(Software software,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findBySoftware(software, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findParentDataProducer
	 * (moos.ssds.metadata.DataProducer, boolean)
	 */
	@Override
	public DataProducer findParentDataProducer(DataProducer dataProducer,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findParentDataProducer(dataProducer,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataProducerAccess#
	 * findClosestParentDataProducerLatitude(moos.ssds.metadata.DataProducer)
	 */
	@Override
	public Double findClosestParentDataProducerLatitude(
			DataProducer dataProducer) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO
				.findClosestParentDataProducerLatitude(dataProducer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findChildDataProducers
	 * (moos.ssds.metadata.DataProducer, boolean)
	 */
	@Override
	public Collection<DataProducer> findChildDataProducers(
			DataProducer dataProducer, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findChildDataProducers(dataProducer,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#countFindChildDataProducers
	 * (moos.ssds.metadata.DataProducer)
	 */
	@Override
	public int countFindChildDataProducers(DataProducer dataProducer)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindChildDataProducers(dataProducer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByDataProducerGroup
	 * (moos.ssds.metadata.DataProducerGroup, java.lang.String,
	 * java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findByDataProducerGroup(
			DataProducerGroup dataProducerGroup, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByDataProducerGroup(dataProducerGroup,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByDataProducerGroupName
	 * (java.lang.String, boolean, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findByDataProducerGroupName(
			String dataProducerGroupName, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByDataProducerGroupName(
				dataProducerGroupName, exactMatch, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataProducerAccess#
	 * countFindByDataProducerGroupName(java.lang.String, boolean)
	 */
	@Override
	public int countFindByDataProducerGroupName(String dataProducerGroupName,
			boolean exactMatch) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindByDataProducerGroupName(
				dataProducerGroupName, exactMatch);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByInput(moos.ssds.
	 * metadata.DataContainer, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findByInput(DataContainer dataContainer,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByInput(dataContainer, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByOutput(moos.ssds
	 * .metadata.DataContainer, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public DataProducer findByOutput(DataContainer dataContainer,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByOutput(dataContainer, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByResource(moos.ssds
	 * .metadata.Resource, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findByResource(Resource resource,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByResource(resource, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByKeywordName(java
	 * .lang.String, boolean, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findByKeywordName(String keywordName,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByKeywordName(keywordName, exactMatch,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#findByEvent(moos.ssds.
	 * metadata.Event, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findByEvent(Event event,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByEvent(event, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataProducerAccess#
	 * findAllDeploymentsOfDeviceTypeFromParent(moos.ssds.metadata.DataProducer,
	 * java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findAllDeploymentsOfDeviceTypeFromParent(
			DataProducer parentDeployment, String deviceTypeName,
			String orderByProperty, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findAllDeploymentsOfDeviceTypeFromParent(
				parentDeployment, deviceTypeName, orderByProperty,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataProducerAccess#
	 * findAllDeploymentsOfDeviceTypeFromParent(java.lang.Long,
	 * java.lang.String, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Double, java.lang.Float, java.lang.Double, java.lang.String,
	 * java.lang.String, boolean)
	 */
	@Override
	public Collection<DataProducer> findAllDeploymentsOfDeviceTypeFromParent(
			Long parentID, String deviceTypeName, Double nominalLongitude,
			Double longitudeTolerance, Double nominalLatitude,
			Double latitudeTolerance, Float nominalDepth,
			Double depthTolerance, String orderByProperty,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findAllDeploymentsOfDeviceTypeFromParent(
				parentID, deviceTypeName, nominalLongitude, longitudeTolerance,
				nominalLatitude, latitudeTolerance, nominalDepth,
				depthTolerance, orderByProperty, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataProducerAccess#
	 * findDevicesByParentByTypeAndByLocation(java.lang.Long, java.lang.String,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Float, java.lang.Double, java.lang.String, java.lang.String,
	 * boolean)
	 */
	@Override
	public Collection<Device> findDevicesByParentByTypeAndByLocation(
			Long parentID, String deviceTypeName, Double nominalLongitude,
			Double longitudeTolerance, Double nominalLatitude,
			Double latitudeTolerance, Float nominalDepth,
			Double depthTolerance, String orderByProperty,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findDevicesByParentByTypeAndByLocation(parentID,
				deviceTypeName, nominalLongitude, longitudeTolerance,
				nominalLatitude, latitudeTolerance, nominalDepth,
				depthTolerance, orderByProperty, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataProducerAccess#
	 * findDevicesByParentByTypeAndByLocation(java.lang.String,
	 * java.lang.String, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Double, java.lang.Float, java.lang.Double, java.lang.String,
	 * java.lang.String, boolean)
	 */
	@Override
	public Collection<Device> findDevicesByParentByTypeAndByLocation(
			String parentName, String deviceTypeName, Double nominalLongitude,
			Double longitudeTolerance, Double nominalLatitude,
			Double latitudeTolerance, Float nominalDepth,
			Double depthTolerance, String orderByProperty,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findDevicesByParentByTypeAndByLocation(
				parentName, deviceTypeName, nominalLongitude,
				longitudeTolerance, nominalLatitude, latitudeTolerance,
				nominalDepth, depthTolerance, orderByProperty,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataProducerAccess#
	 * findAllDevicesByParentDeploymentByTypeAndByLocation
	 * (moos.ssds.metadata.DataProducer, java.lang.String, java.lang.Double,
	 * java.lang.Double, java.lang.Double, java.lang.Double, java.lang.Float,
	 * java.lang.Double, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<Device> findAllDevicesByParentDeploymentByTypeAndByLocation(
			DataProducer parentDeployment, String deviceTypeName,
			Double nominalLongitude, Double longitudeTolerance,
			Double nominalLatitude, Double latitudeTolerance,
			Float nominalDepth, Double depthTolerance, String orderByProperty,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO
				.findAllDevicesByParentDeploymentByTypeAndByLocation(
						parentDeployment, deviceTypeName, nominalLongitude,
						longitudeTolerance, nominalLatitude, latitudeTolerance,
						nominalDepth, depthTolerance, orderByProperty,
						ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataProducerAccess#
	 * findAllDevicesByParentByTypeAndByLocation(java.lang.Long,
	 * java.lang.String, java.lang.Double, java.lang.Double, java.lang.Double,
	 * java.lang.Double, java.lang.Float, java.lang.Double, java.lang.String,
	 * java.lang.String, boolean)
	 */
	@Override
	public Collection<Device> findAllDevicesByParentByTypeAndByLocation(
			Long parentID, String deviceTypeName, Double nominalLongitude,
			Double longitudeTolerance, Double nominalLatitude,
			Double latitudeTolerance, Float nominalDepth,
			Double depthTolerance, String orderByProperty,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findAllDevicesByParentByTypeAndByLocation(
				parentID, deviceTypeName, nominalLongitude, longitudeTolerance,
				nominalLatitude, latitudeTolerance, nominalDepth,
				depthTolerance, orderByProperty, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#addChildDataProducer(moos
	 * .ssds.metadata.DataProducer, moos.ssds.metadata.DataProducer)
	 */
	@Override
	public void addChildDataProducer(DataProducer parentDataProducer,
			DataProducer childDataProducer) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		dataProducerDAO.addChildDataProducer(parentDataProducer,
				childDataProducer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#addResource(moos.ssds.
	 * metadata.DataProducer, moos.ssds.metadata.Resource)
	 */
	@Override
	public void addResource(DataProducer dataProducer, Resource resourceToAdd)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		dataProducerDAO.addResource(dataProducer, resourceToAdd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#removeResource(moos.ssds
	 * .metadata.DataProducer, moos.ssds.metadata.Resource)
	 */
	@Override
	public void removeResource(DataProducer dataProducer, Resource resource)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		dataProducerDAO.removeResource(dataProducer, resource);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#createDuplicateDeepDeployment
	 * (moos.ssds.metadata.DataProducer, java.util.Date, boolean,
	 * java.util.Date, java.lang.String, java.lang.String)
	 */
	@Override
	public Long createDuplicateDeepDeployment(DataProducer deploymentToCopy,
			Date newStartDate, boolean closeOld, Date oldEndDate,
			String newHeadDeploymentName, String baseDataStreamUri)
			throws MetadataAccessException {

		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.createDuplicateDeepDeployment(deploymentToCopy,
				newStartDate, closeOld, oldEndDate, newHeadDeploymentName,
				baseDataStreamUri);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#deepDelete(moos.ssds.metadata
	 * .DataProducer)
	 */
	@Override
	public void deepDelete(DataProducer dataProducer)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		dataProducerDAO.makeDeepTransient(dataProducer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataProducerAccess#makeDeepTransient(moos
	 * .ssds.metadata.DataProducer)
	 */
	@Override
	public void makeDeepTransient(DataProducer dataProducer)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		dataProducerDAO.makeDeepTransient(dataProducer);
	}
}
