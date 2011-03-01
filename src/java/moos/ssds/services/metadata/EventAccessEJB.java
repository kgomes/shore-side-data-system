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
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import moos.ssds.dao.EventDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.Event;

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * Provides a facade that provides client services for Event objects.
 * 
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.5 $
 */
@Stateless
@Local(EventAccessLocal.class)
@LocalBinding(jndiBinding = "moos/ssds/services/metadata/EventAccessLocal")
@Remote(EventAccess.class)
@RemoteBinding(jndiBinding = "moos/ssds/services/metadata/EventAccess")
public class EventAccessEJB extends AccessBean implements EventAccess,
		EventAccessLocal {

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(EventAccessEJB.class);

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

		// Now set the super persistent class to Event
		super.setPersistentClass(Event.class);
		logger.debug("OK, set Persistent class to Event");

		// And the DAO
		super.setDaoClass(EventDAO.class);
		logger.debug("OK, set DAO Class to EventDAO");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.EventAccess#findByName(java.lang.String,
	 * boolean, java.lang.String, java.lang.String, boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public Collection<Event> findByName(String name, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		EventDAO eventDAO = (EventDAO) this.getMetadataDAO();

		// Now call the method
		return eventDAO.findByName(name, exactMatch, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.EventAccess#findByLikeName(java.lang.String,
	 * java.lang.String, java.lang.String, boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public Collection<Event> findByLikeName(String likeName,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		EventDAO eventDAO = (EventDAO) this.getMetadataDAO();

		// Now call the method
		return eventDAO.findByLikeName(likeName, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.EventAccess#findByNameAndDates(java.lang.
	 * String, java.util.Date, java.util.Date, boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public Event findByNameAndDates(String name, Date startDate, Date endDate,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		EventDAO eventDAO = (EventDAO) this.getMetadataDAO();

		// Now call the method
		return eventDAO.findByNameAndDates(name, startDate, endDate,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.EventAccess#findAllNames()
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public Collection<String> findAllNames() throws MetadataAccessException {
		// Grab the DAO
		EventDAO eventDAO = (EventDAO) this.getMetadataDAO();

		// Now call the method
		return eventDAO.findAllNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.EventAccess#findWithinDateRange(java.util
	 * .Date, java.util.Date, java.lang.String, java.lang.String, boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public Collection<Event> findWithinDateRange(Date startDate, Date endDate,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		EventDAO eventDAO = (EventDAO) this.getMetadataDAO();

		// Now call the method
		return eventDAO.findWithinDateRange(startDate, endDate,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

}
