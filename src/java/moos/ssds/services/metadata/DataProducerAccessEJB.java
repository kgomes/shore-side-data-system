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
import java.util.Properties;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

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
import org.hibernate.SessionFactory;

/**
 * Provides a facade that provides client services for DataProducer objects.
 * 
 * @ejb.bean name="DataProducerAccess" type="Stateless"
 *           jndi-name="moos/ssds/services/metadata/DataProducerAccess"
 *           local-jndi-name="moos/ssds/services/metadata/DataProducerAccessLocal"
 *           view-type="both" transaction-type="Container"
 * @ejb.home create="true"
 *           local-class="moos.ssds.services.metadata.DataProducerAccessLocalHome"
 *           remote-class="moos.ssds.services.metadata.DataProducerAccessHome"
 *           extends="javax.ejb.EJBHome"
 * @ejb.interface create="true"
 *                local-class="moos.ssds.services.metadata.DataProducerAccessLocal"
 *                local-extends="javax.ejb.EJBLocalObject,moos.ssds.services.metadata.IMetadataAccess"
 *                remote-class="moos.ssds.services.metadata.DataProducerAccess"
 *                extends="javax.ejb.EJBObject,moos.ssds.services.metadata.IMetadataAccessRemote"
 * @ejb.util generate="physical"
 * @soap.service urn="DataProducerAccess" scope="Request"
 * @axis.service urn="DataProducerAccess" scope="Request"
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.25 $
 */
public class DataProducerAccessEJB extends AccessBean implements
		IMetadataAccess {

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

		// Now set the super persistent class to DataContainer
		super.setPersistentClass(DataProducer.class);
		// And the DAO
		super.setDaoClass(DataProducerDAO.class);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByProperties(String, boolean, String, Date,
	 *      boolean, Date, boolean, Double, Double, Double, Double, Float,
	 *      Float, Float, Float, String, boolean, String, boolean)
	 */
	public Collection findByProperties(String name, boolean exactMatch,
			String dataProducerType, Date startDate,
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#countFindByProperties(String, boolean, String, Date,
	 *      boolean, Date, boolean, Double, Double, Double, Double, Float,
	 *      Float, Float, Float, String, boolean)
	 */
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByName(String, boolean, String, boolean)
	 */
	public Collection findByName(String name, boolean exactMatch,
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#countFindByName(String, boolean)
	 */
	public int countFindByName(String name, boolean exactMatch)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindByName(name, exactMatch);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByDataProducerTypeAndName(String, String,
	 *      boolean, String, boolean)
	 */
	public Collection findByDataProducerTypeAndName(String dataProducerType,
			String name, String orderByPropertyName,
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#countFindByDataProducerTypeAndName(String, String,
	 *      boolean)
	 */
	public int countFindByDataProducerTypeAndName(String dataProducerType,
			String name, boolean exactMatch) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindByDataProducerTypeAndName(
				dataProducerType, name, exactMatch);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findParentlessDeployments(String, boolean)
	 */
	public Collection findParentlessDeployments(String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findParentlessDeployments(orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#countFindParentlessDeployments()
	 */
	public int countFindParentlessDeployments() throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindParentlessDeployments();
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findParentlessDataProducer(String, String, boolean)
	 */
	public Collection findParentlessDataProducers(String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findParentlessDataProducers(orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#countFindParentlessDataProducers()
	 */
	public int countFindParentlessDataProducers()
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindParentlessDataProducers();
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByDateRangeAndName(Date, boolean, Date, boolean,
	 *      String, boolean, String, boolean)
	 */
	public Collection findByDateRangeAndName(Date startDate,
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#countFindByDateRangeAndName(Date, boolean, Date,
	 *      boolean, String, boolean)
	 */
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByGeospatialCube(Double, Double, Double, Double,
	 *      Float, Float, String, boolean)
	 */
	public Collection findByGeospatialCube(Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#countFindByGeospatialCube(Double, Double, Double,
	 *      Double, Float, Float)
	 */
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByTimeAndGeospatialCube(Date, boolean, Date,
	 *      boolean, Double, Double, Double, Double, Float, Float, String,
	 *      boolean)
	 */
	public Collection findByTimeAndGeospatialCube(Date startDate,
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#countFindByTimeAndGeospatialCube(Date, boolean,
	 *      Date, boolean, Double, Double, Double, Double, Float, Float)
	 */
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByNameAndGeospatialCube(String, boolean, Double,
	 *      Double, Double, Double, Float, Float, String, boolean)
	 */
	public Collection findByNameAndGeospatialCube(String name,
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#countFindByNameAndGeospatialCube(String, boolean,
	 *      Double, Double, Double, Double, Float, Float)
	 */
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByNameAndTimeAndGeospatialCube(String, boolean,
	 *      Date, boolean, Date, boolean, Double, Double, Double, Double, Float,
	 *      Float, String, boolean)
	 */
	public Collection findByNameAndTimeAndGeospatialCube(String name,
			boolean exactMatch, Date startDate, boolean boundedByStartDate,
			Date endDate, boolean boundedByEndDate, Double geospatialLatMin,
			Double geospatialLatMax, Double geospatialLonMin,
			Double geospatialLonMax, Float geospatialVerticalMin,
			Float geospatialVerticalMax, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#countFindByNameAndTimeAndGeospatialCube(String,
	 *      boolean, Date, boolean, Date, boolean, Double, Double, Double,
	 *      Double, Float, Float, boolean)
	 */
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByHostName(String, boolean, String, boolean)
	 */
	public Collection findByHostName(String hostName, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByHostName(hostName, exactMatch,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#countFindByHostName(String, boolean)
	 */
	public int countFindByHostName(String hostName, boolean exactMatch)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindByHostName(hostName, exactMatch);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByPerson(Person, String, boolean)
	 */
	public Collection findByPerson(Person person, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByPerson(person, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#countFindByPerson(Person)
	 */
	public int countFindByPerson(Person person) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindByPerson(person);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByDevice(Device, String, String, boolean)
	 */
	public Collection findByDevice(Device device, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByDevice(device, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByDeviceId(Long, String, String, boolean)
	 */
	public Collection findByDeviceId(Long deviceId, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByDeviceId(deviceId, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByDeviceAndTimeWindow(Device, Date, Date,
	 *      String, String, boolean)
	 */
	public Collection findByDeviceAndTimeWindow(Device device, Date startDate,
			Date endDate, String orderByPropertyName,
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByDeviceTypeName(String, boolean, String,
	 *      String, boolean)
	 */
	public Collection findByDeviceTypeName(String deviceTypeName,
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findCurrentParentlessDeployments(String, String,
	 *      boolean)
	 */
	public Collection findCurrentParentlessDeployments(
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findParentlessDeploymentsByName(String, boolean,
	 *      String, String, boolean)
	 */
	public Collection findParentlessDeploymentsByName(String name,
			boolean exactMatch, String orderByPropertyName,
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findCurrentDeployments(String, String, boolean)
	 */
	public Collection findCurrentDeployments(String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findCurrentDeployments(orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findCurrentDeploymentsOfDevice(Device, String,
	 *      String, boolean)
	 */
	public Collection findCurrentDeploymentsOfDevice(Device device,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findCurrentDeploymentsOfDevice(device,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findCurrentDeploymentsOfDeviceId(Long, String,
	 *      String, boolean)
	 */
	public Collection findCurrentDeploymentsOfDeviceId(Long deviceId,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findCurrentDeploymentsOfDeviceId(deviceId,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/**
	 * @throws MetadataAccessException
	 * @throws MetadataException
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findCurrentDeploymentsByRole(String, String, String,
	 *      boolean)
	 */
	public Collection findCurrentDeploymentsByRole(String role,
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

	/**
	 * @throws MetadataAccessException
	 * @throws MetadataException
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findCurrentDeploymentsByRoleAndName(Device, String,
	 *      String, boolean)
	 */
	public Collection findCurrentDeploymentsByRoleAndName(String role,
			String name, boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException, MetadataException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findCurrentDeploymentsByRoleAndName(role, name,
				exactMatch, orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);

	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findBySoftware(Software, String, boolean)
	 */
	public Collection findBySoftware(Software software,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findBySoftware(software, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findParentDataProducer(DataProducer)
	 */
	public DataProducer findParentDataProducer(DataProducer dataProducer,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findParentDataProducer(dataProducer,
				returnFullObjectGraph);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findChildDataProducers(DataProducer, boolean)
	 */
	public Collection findChildDataProducers(DataProducer dataProducer,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findChildDataProducers(dataProducer,
				returnFullObjectGraph);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#countFindChildDataProducers(DataProducer)
	 */
	public int countFindChildDataProducers(DataProducer dataProducer)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindChildDataProducers(dataProducer);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByDataProducerGroup(DataProducerGroup, String,
	 *      boolean)
	 */
	public Collection findByDataProducerGroup(
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByDataProducerGroupName(String, boolean, String,
	 *      boolean)
	 */
	public Collection findByDataProducerGroupName(String dataProducerGroupName,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByDataProducerGroupName(
				dataProducerGroupName, exactMatch, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#countFindByDataProducerGroupName(String, boolean)
	 */
	public int countFindByDataProducerGroupName(String dataProducerGroupName,
			boolean exactMatch) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.countFindByDataProducerGroupName(
				dataProducerGroupName, exactMatch);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByInput(DataContainer, String, boolean)
	 */
	public Collection findByInput(DataContainer dataContainer,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByInput(dataContainer, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByOutput(DataContainer, String, boolean)
	 */
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByResource(Resource, String, boolean)
	 */
	public Collection findByResource(Resource resource,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByResource(resource, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByKeywordName(String, boolean, String, String,
	 *      boolean)
	 */
	public Collection findByKeywordName(String keywordName, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByKeywordName(keywordName, exactMatch,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#findByEvent(Event, String, boolean)
	 */
	public Collection findByEvent(Event event, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataProducerDAO.findByEvent(event, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/**
	 * This method takes in a <code>IDeployment</code> and a DeviceType name,
	 * and some nominal location coordinates and then tries to return a
	 * <code>Collection</code> of <code>IDeployment</code>s the have a
	 * similar device type name, that were deployed on (or under) the given
	 * deployment (parent-child relationship). It will do a &quot;deep&quot;
	 * search for devices. In other words, it will check all deployments of the
	 * parent as well as of any sub-deployments under that parent (i.e. it will
	 * &quot;Walk the chain&quot;).
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @param parentDeployment
	 *            This is a parent <code>IDeployment</code> to start the
	 *            search from. It is the &quot;Root&quot; of the search tree.
	 * @param deviceTypeName
	 *            This is a <code>String</code> that will be used to search
	 *            for devices that have a similar device type to that named with
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
	 * @return a <code>Collection</code> of <code>IDeployment</code>s that
	 *         meet the search criteria defined by the incoming parameters. No
	 *         duplicates are removed and if no deployments were found, an empty
	 *         collection is returned.
	 */
	public Collection findAllDeploymentsOfDeviceTypeFromParent(
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
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @param parentDeployment
	 *            This is a parent <code>IDeployment</code> to start the
	 *            search from. It is the &quot;Root&quot; of the search tree.
	 * @param deviceTypeName
	 *            This is a <code>String</code> that will be used to search
	 *            for devices that have a similar device type to that named with
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
	 * @return a <code>Collection</code> of <code>IDeployment</code>s that
	 *         meet the search criteria defined by the incoming parameters. No
	 *         duplicates are removed and if no deployments were found, an empty
	 *         collection is returned.
	 */
	public Collection findAllDeploymentsOfDeviceTypeFromParent(Long parentID,
			String deviceTypeName, Double nominalLongitude,
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

	/**
	 * This method takes in the ID of a parent <code>IDevice</code>, the name
	 * of a <code>IDeviceType</code>, and some nominal location coordinates
	 * and then tries to return a <code>Collection</code> of
	 * <code>IDevice</code>s with the given type, that were deployed on the
	 * given device (parent-child) relationship. This will only look for direct
	 * child deployments, it won't walk any of the sub deployments.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
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
	public Collection findDevicesByParentByTypeAndByLocation(Long parentID,
			String deviceTypeName, Double nominalLongitude,
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

	/**
	 * This method takes in the name of a <code>IDevice</code>, the name of a
	 * <code>IDeviceType</code>, and some nominal location coordinates and
	 * then tries to return a <code>Collection</code> of <code>IDevices</code>
	 * with the given type, that were deployed on the given device
	 * (parent-child) relationship. This will only look for direct child
	 * deployments, it won't walk any of the sub deployments.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
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
	public Collection findDevicesByParentByTypeAndByLocation(String parentName,
			String deviceTypeName, Double nominalLongitude,
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

	/**
	 * This method takes in a <code>IDeployment</code> and a
	 * <code>DeviceType</code> name, and some nominal location coordinates and
	 * then tries to return a list of <code>IDevice</code>s the have a
	 * similar device type name, that were deployed on the given device
	 * (parent-child) relationship. It will do a &quot;deep&quot; search for
	 * devices. In other words, it will check all deployments of the parent as
	 * well as of any sub-deployments under that parent (i.e. it will &quot;Walk
	 * the chain&quot;).
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @param parentDeployment
	 *            This is a parent <code>IDeployment</code> to start the
	 *            search from. It is the &quot;Root&quot; of the search tree.
	 * @param deviceTypeName
	 *            This is a <code>String</code> that will be used to search
	 *            for devices that have a similar device type to that named with
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
	 * @return a <code>Collection</code> of <code>IDevice</code>s that meet
	 *         the search criteria defined by the incoming parameters. The
	 *         devices are listed from the most recent deployment first (index
	 *         0) to the oldest deployment. Each device is listed only once in
	 *         the return collection
	 */
	public Collection findAllDevicesByParentDeploymentByTypeAndByLocation(
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
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
	public Collection findAllDevicesByParentByTypeAndByLocation(Long parentID,
			String deviceTypeName, Double nominalLongitude,
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#addChildDataProducer(DataProducer, DataProducer)
	 */
	public void addChildDataProducer(DataProducer parentDataProducer,
			DataProducer childDataProducer) throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		dataProducerDAO.addChildDataProducer(parentDataProducer,
				childDataProducer);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#addResource(DataProducer, Resource)
	 */
	public void addResource(DataProducer dataProducer, Resource resourceToAdd)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		dataProducerDAO.addResource(dataProducer, resourceToAdd);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#removeResource(DataProducer, Resource)
	 */
	public void removeResource(DataProducer dataProducer, Resource resource)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		dataProducerDAO.removeResource(dataProducer, resource);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#createDuplicateDeepDeployment(DataProducer, Date,
	 *      boolean, Date, String)
	 */
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

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#makeDeepTransient(DataProducer)
	 */
	public void deepDelete(DataProducer dataProducer)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		dataProducerDAO.makeDeepTransient(dataProducer);
	}

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @see DataProducerDAO#makeDeepTransient(DataProducer)
	 */
	public void makeDeepTransient(DataProducer dataProducer)
			throws MetadataAccessException {
		// Grab the DAO
		DataProducerDAO dataProducerDAO = (DataProducerDAO) this
				.getMetadataDAO();

		// Now call the method
		dataProducerDAO.makeDeepTransient(dataProducer);
	}

	/**
	 * This is the version that we can control for serialization purposes
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(DataProducerAccessEJB.class);
}
