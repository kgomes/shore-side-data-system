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

import moos.ssds.dao.DeviceTypeDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DeviceType;

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * Provides a facade that provides client services for DeviceType objects.
 * 
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.13 $
 */
@Stateless
@Local(DeviceTypeAccessLocal.class)
@LocalBinding(jndiBinding = "moos/ssds/services/metadata/DeviceTypeAccessLocal")
@Remote(DeviceTypeAccess.class)
@RemoteBinding(jndiBinding = "moos/ssds/services/metadata/DeviceTypeAccess")
public class DeviceTypeAccessEJB extends AccessBean implements
		DeviceTypeAccess, DeviceTypeAccessLocal {

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(DeviceTypeAccessEJB.class);

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

		// Now set the super persistent class to DeviceType
		super.setPersistentClass(DeviceType.class);
		logger.debug("OK, set Persistent class to DeviceType");

		// And the DAO
		super.setDaoClass(DeviceTypeDAO.class);
		logger.debug("OK, set DAO Class to DeviceTypeDAO");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DeviceTypeAccess#findByName(java.lang.String,
	 * boolean)
	 */

	public DeviceType findByName(String name, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// Grab the DAO
		DeviceTypeDAO deviceTypeDAO = (DeviceTypeDAO) this.getMetadataDAO();

		// Now call the method and return the results
		return deviceTypeDAO.findByName(name, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.DeviceTypeAccess#findByLikeName(java.lang
	 * .String, java.lang.String, java.lang.String, boolean)
	 */

	public Collection<DeviceType> findByLikeName(String likeName,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		DeviceTypeDAO deviceTypeDAO = (DeviceTypeDAO) this.getMetadataDAO();

		// Now call the method and return the results
		return deviceTypeDAO.findByLikeName(likeName, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.DeviceTypeAccess#findAllNames()
	 */

	public Collection<String> findAllNames() throws MetadataAccessException {
		// Grab the DAO
		DeviceTypeDAO deviceTypeDAO = (DeviceTypeDAO) this.getMetadataDAO();

		// Now call the method and return the results
		return deviceTypeDAO.findAllNames();
	}

}