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

import javax.annotation.PostConstruct;
import javax.ejb.CreateException;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import moos.ssds.dao.PersonDAO;
import moos.ssds.dao.ResourceDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.ResourceType;

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * Provides a facade that provides client services for Resource objects.
 * 
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.6 $
 */
@Stateless
@Local(ResourceAccessLocal.class)
@LocalBinding(jndiBinding = "moos/ssds/services/metadata/ResourceAccessLocal")
@Remote(ResourceAccess.class)
@RemoteBinding(jndiBinding = "moos/ssds/services/metadata/ResourceAccess")
public class ResourceAccessEJB extends AccessBean implements ResourceAccess,
		ResourceAccessLocal {

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(ResourceAccessEJB.class);

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

		// Now set the super persistent class to Person
		super.setPersistentClass(Person.class);
		logger.debug("OK, set Persistent class to Person");

		// And the DAO
		super.setDaoClass(PersonDAO.class);
		logger.debug("OK, set DAO Class to PersonDAO");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.ResourceAccess#findByName(java.lang.String)
	 */

	public Collection<Resource> findByName(String name)
			throws MetadataAccessException {
		// Grab the DAO
		ResourceDAO resourceDAO = (ResourceDAO) this.getMetadataDAO();

		// Now call the method
		return resourceDAO.findByName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.ResourceAccess#findByLikeName(java.lang.String
	 * )
	 */

	public Collection<Resource> findByLikeName(String likeName)
			throws MetadataAccessException {
		// Grab the DAO
		ResourceDAO resourceDAO = (ResourceDAO) this.getMetadataDAO();

		// Now call the method
		return resourceDAO.findByLikeName(likeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.ResourceAccess#findAllNames()
	 */

	public Collection<String> findAllNames() throws MetadataAccessException {
		// Grab the DAO
		ResourceDAO resourceDAO = (ResourceDAO) this.getMetadataDAO();

		// Now call the method
		return resourceDAO.findAllNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.ResourceAccess#findByURIString(java.lang.
	 * String)
	 */

	public Resource findByURIString(String uriString)
			throws MetadataAccessException {
		// Grab the DAO
		ResourceDAO resourceDAO = (ResourceDAO) this.getMetadataDAO();

		// Now call the method
		return resourceDAO.findByURIString(uriString);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.ResourceAccess#findByURI(java.net.URI)
	 */

	public Collection<Resource> findByURI(URI uri)
			throws MetadataAccessException {
		// Grab the DAO
		ResourceDAO resourceDAO = (ResourceDAO) this.getMetadataDAO();

		// Now call the method
		return resourceDAO.findByURI(uri);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.ResourceAccess#findByURL(java.net.URL)
	 */

	public Collection<Resource> findByURL(URL url)
			throws MetadataAccessException {
		// Grab the DAO
		ResourceDAO resourceDAO = (ResourceDAO) this.getMetadataDAO();

		// Now call the method
		return resourceDAO.findByURL(url);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.ResourceAccess#findByMimeType(java.lang.String
	 * )
	 */

	public Collection<Resource> findByMimeType(String mimeType)
			throws MetadataAccessException {
		// Grab the DAO
		ResourceDAO resourceDAO = (ResourceDAO) this.getMetadataDAO();

		// Now call the method
		return resourceDAO.findByMimeType(mimeType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.ResourceAccess#findByPerson(moos.ssds.metadata
	 * .Person)
	 */

	public Collection<Resource> findByPerson(Person person)
			throws MetadataAccessException {
		// Grab the DAO
		ResourceDAO resourceDAO = (ResourceDAO) this.getMetadataDAO();

		// Now call the method
		return resourceDAO.findByPerson(person);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.ResourceAccess#findByResourceType(moos.ssds
	 * .metadata.ResourceType, java.lang.String, java.lang.String, boolean)
	 */

	public Collection<Resource> findByResourceType(ResourceType resourceType,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		ResourceDAO resourceDAO = (ResourceDAO) this.getMetadataDAO();

		// Now call the method
		return resourceDAO.findByResourceType(resourceType,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}
}
