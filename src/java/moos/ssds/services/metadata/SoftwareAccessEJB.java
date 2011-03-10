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
import moos.ssds.dao.SoftwareDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.Software;

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * Provides a facade that provides client services for Software objects.
 * 
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.6 $
 */
@Stateless
@Local(SoftwareAccessLocal.class)
@LocalBinding(jndiBinding = "moos/ssds/services/metadata/SoftwareAccessLocal")
@Remote(SoftwareAccess.class)
@RemoteBinding(jndiBinding = "moos/ssds/services/metadata/SoftwareAccess")
public class SoftwareAccessEJB extends AccessBean implements SoftwareAccess,
		SoftwareAccessLocal {

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(SoftwareAccessEJB.class);

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
	 * moos.ssds.services.metadata.SoftwareAccess#findByName(java.lang.String)
	 */

	public Collection<Software> findByName(String name)
			throws MetadataAccessException {
		// Grab the DAO
		SoftwareDAO softwareDAO = (SoftwareDAO) this.getMetadataDAO();

		// Now call the method
		return softwareDAO.findByName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.SoftwareAccess#findByLikeName(java.lang.String
	 * )
	 */

	public Collection<Software> findByLikeName(String likeName)
			throws MetadataAccessException {
		// Grab the DAO
		SoftwareDAO softwareDAO = (SoftwareDAO) this.getMetadataDAO();

		// Now call the method
		return softwareDAO.findByLikeName(likeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.SoftwareAccess#findAllNames()
	 */

	public Collection<String> findAllNames() throws MetadataAccessException {
		// Grab the DAO
		SoftwareDAO softwareDAO = (SoftwareDAO) this.getMetadataDAO();

		// Now call the method
		return softwareDAO.findAllNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.SoftwareAccess#findByNameAndSoftwareVersion
	 * (java.lang.String, java.lang.String, boolean)
	 */

	public Software findByNameAndSoftwareVersion(String name, String version,
			boolean returnFullObjectGraph) throws MetadataAccessException {
		// Grab the DAO
		SoftwareDAO softwareDAO = (SoftwareDAO) this.getMetadataDAO();

		// Now call the method
		return softwareDAO.findByNameAndSoftwareVersion(name, version,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.SoftwareAccess#findByURIString(java.lang.
	 * String)
	 */

	public Collection<Software> findByURIString(String uriString)
			throws MetadataAccessException {
		// Grab the DAO
		SoftwareDAO softwareDAO = (SoftwareDAO) this.getMetadataDAO();

		// Now call the method
		return softwareDAO.findByURIString(uriString);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.SoftwareAccess#findByURI(java.net.URI)
	 */

	public Collection<Software> findByURI(URI uri)
			throws MetadataAccessException {
		// Grab the DAO
		SoftwareDAO softwareDAO = (SoftwareDAO) this.getMetadataDAO();

		// Now call the method
		return softwareDAO.findByURI(uri);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.SoftwareAccess#findByURL(java.net.URL)
	 */

	public Collection<Software> findByURL(URL url)
			throws MetadataAccessException {
		// Grab the DAO
		SoftwareDAO softwareDAO = (SoftwareDAO) this.getMetadataDAO();

		// Now call the method
		return softwareDAO.findByURL(url);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.SoftwareAccess#findByPerson(moos.ssds.metadata
	 * .Person)
	 */

	public Collection<Software> findByPerson(Person person)
			throws MetadataAccessException {
		// Grab the DAO
		SoftwareDAO softwareDAO = (SoftwareDAO) this.getMetadataDAO();

		// Now call the method
		return softwareDAO.findByPerson(person);
	}

}
