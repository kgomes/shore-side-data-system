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

import moos.ssds.dao.DataContainerGroupDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataContainerGroup;

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * Provides a facade that provides client services for DataContainerGroup
 * objects.
 * 
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.5 $
 */
@Stateless
@Local(DataContainerGroupAccessLocal.class)
@LocalBinding(jndiBinding = "moos/ssds/services/metadata/DataContainerGroupAccessLocal")
@Remote(DataContainerGroupAccess.class)
@RemoteBinding(jndiBinding = "moos/ssds/services/metadata/DataContainerGroupAccess")
public class DataContainerGroupAccessEJB extends AccessBean implements
		DataContainerGroupAccess, DataContainerGroupAccessLocal {

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(DataContainerGroupAccessEJB.class);

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

		// Now set the super persistent class to DataContainerGroup
		super.setPersistentClass(DataContainerGroup.class);
		logger.debug("OK, set Persistent class to DataContainerGroup");

		// And the DAO
		super.setDaoClass(DataContainerGroupDAO.class);
		logger.debug("OK, set DAO Class to DataContainerGroupDAO");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerGroupAccess#findByName(java.
	 * lang.String, boolean, java.lang.String, java.lang.String, boolean)
	 */

	public Collection<DataContainerGroup> findByName(String name,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerGroupDAO dataContainerGroupDAO = (DataContainerGroupDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerGroupDAO.findByName(name, exactMatch,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DataContainerGroupAccess#findAllNames()
	 */

	public Collection<String> findAllNames() throws MetadataAccessException {
		// Grab the DAO
		DataContainerGroupDAO dataContainerGroupDAO = new DataContainerGroupDAO(
				sessionFactory.getCurrentSession());

		// Now call the method
		return dataContainerGroupDAO.findAllNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DataContainerGroupAccess#findByDataContainer
	 * (moos.ssds.metadata.DataContainer, boolean)
	 */

	public Collection<DataContainerGroup> findByDataContainer(
			DataContainer dataContainer, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// Grab the DAO
		DataContainerGroupDAO dataContainerGroupDAO = (DataContainerGroupDAO) this
				.getMetadataDAO();

		// Now call the method
		return dataContainerGroupDAO.findByDataContainer(dataContainer,
				returnFullObjectGraph);
	}

}
