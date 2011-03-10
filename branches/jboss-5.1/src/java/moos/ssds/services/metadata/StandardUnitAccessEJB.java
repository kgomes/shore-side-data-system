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

import moos.ssds.dao.PersonDAO;
import moos.ssds.dao.StandardUnitDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.StandardUnit;

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * Provides a facade that provides client services for StandardUnit objects.
 * 
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.10 $
 */
@Stateless
@Local(StandardUnitAccessLocal.class)
@LocalBinding(jndiBinding = "moos/ssds/services/metadata/StandardUnitAccessLocal")
@Remote(StandardUnitAccess.class)
@RemoteBinding(jndiBinding = "moos/ssds/services/metadata/StandardUnitAccess")
public class StandardUnitAccessEJB extends AccessBean implements
		StandardUnitAccess, StandardUnitAccessLocal {

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(StandardUnitAccessEJB.class);

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
	 * moos.ssds.services.metadata.StandardUnitAccess#findByName(java.lang.String
	 * )
	 */

	public StandardUnit findByName(String name) throws MetadataAccessException {

		// Grab the DAO
		StandardUnitDAO standardUnitDAO = (StandardUnitDAO) this
				.getMetadataDAO();

		// Now make the correct call and return the result
		return standardUnitDAO.findByName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.StandardUnitAccess#findByLikeName(java.lang
	 * .String)
	 */

	public Collection<StandardUnit> findByLikeName(String likeName)
			throws MetadataAccessException {

		// Grab the DAO
		StandardUnitDAO standardUnitDAO = (StandardUnitDAO) this
				.getMetadataDAO();

		// Now make the correct call and return the result
		return standardUnitDAO.findByLikeName(likeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.StandardUnitAccess#findBySymbol(java.lang
	 * .String)
	 */

	public Collection<StandardUnit> findBySymbol(String symbol)
			throws MetadataAccessException {

		// Grab the DAO
		StandardUnitDAO standardUnitDAO = (StandardUnitDAO) this
				.getMetadataDAO();

		// Now make the correct call and return the result
		return standardUnitDAO.findBySymbol(symbol);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.StandardUnitAccess#findByLikeSymbol(java.
	 * lang.String)
	 */

	public Collection<StandardUnit> findByLikeSymbol(String likeSymbol)
			throws MetadataAccessException {

		// Grab the DAO
		StandardUnitDAO standardUnitDAO = (StandardUnitDAO) this
				.getMetadataDAO();

		// Now make the correct call and return the result
		return standardUnitDAO.findByLikeSymbol(likeSymbol);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.StandardUnitAccess#findAllNames()
	 */

	public Collection<String> findAllNames() throws MetadataAccessException {

		// Grab the DAO
		StandardUnitDAO standardUnitDAO = (StandardUnitDAO) this
				.getMetadataDAO();

		// Now make the correct call and return the result
		return standardUnitDAO.findAllNames();
	}

}