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
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import moos.ssds.dao.StandardDomainDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.StandardDomain;

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * Provides a facade that provides client services for StandardDomain objects.
 * 
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.4 $
 */
@Stateless
@Local(StandardDomainAccessLocal.class)
@LocalBinding(jndiBinding = "moos/ssds/services/metadata/StandardDomainAccessLocal")
@Remote(StandardDomainAccess.class)
@RemoteBinding(jndiBinding = "moos/ssds/services/metadata/StandardDomainAccess")
public class StandardDomainAccessEJB extends AccessBean implements
		StandardDomainAccess, StandardDomainAccessLocal {

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(StandardDomainAccessEJB.class);

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

		// Now set the super persistent class to StandardDomain
		super.setPersistentClass(StandardDomain.class);
		logger.debug("OK, set Persistent class to StandardDomain");

		// And the DAO
		super.setDaoClass(StandardDomainDAO.class);
		logger.debug("OK, set DAO Class to StandardDomainDAO");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.StandardDomainAccess#findByName(java.lang
	 * .String)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public Collection<StandardDomain> findByName(String name)
			throws MetadataAccessException {
		// Grab the DAO
		StandardDomainDAO standardDomainDAO = (StandardDomainDAO) this
				.getMetadataDAO();

		// Now call the method
		return standardDomainDAO.findByName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.StandardDomainAccess#findByLikeName(java.
	 * lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public Collection<StandardDomain> findByLikeName(String likeName)
			throws MetadataAccessException {
		// Grab the DAO
		StandardDomainDAO standardDomainDAO = (StandardDomainDAO) this
				.getMetadataDAO();

		// Now call the method
		return standardDomainDAO.findByLikeName(likeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.StandardDomainAccess#findAllNames()
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public Collection<String> findAllNames() throws MetadataAccessException {
		// Grab the DAO
		StandardDomainDAO standardDomainDAO = (StandardDomainDAO) this
				.getMetadataDAO();

		// Now call the method
		return standardDomainDAO.findAllNames();
	}
}
