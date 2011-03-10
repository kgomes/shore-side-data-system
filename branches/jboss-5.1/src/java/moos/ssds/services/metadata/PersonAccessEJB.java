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
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.Person;

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * Provides a facade that provides client services for Person objects.
 * 
 * @author : $Author: mccann $
 * @version : $Revision: 1.1.2.12 $
 */
@Stateless
@Local(PersonAccessLocal.class)
@LocalBinding(jndiBinding = "moos/ssds/services/metadata/PersonAccessLocal")
@Remote(PersonAccess.class)
@RemoteBinding(jndiBinding = "moos/ssds/services/metadata/PersonAccess")
public class PersonAccessEJB extends AccessBean implements PersonAccess,
		PersonAccessLocal {

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(PersonAccessEJB.class);

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
	 * moos.ssds.services.metadata.PersonAccess#findByEmail(java.lang.String,
	 * boolean, java.lang.String, java.lang.String, boolean)
	 */
	public Collection<Person> findByEmail(String email, boolean exactMatch,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException {

		logger.debug("findByEmail called: email=" + email);
		// Grab the DAO
		PersonDAO personDAO = (PersonDAO) this.getMetadataDAO();

		logger.debug("Grabbed PersonDAO => " + personDAO);
		// Now find the persons ID
		return personDAO.findByEmail(email, exactMatch, orderByPropertyName,
				ascendingOrDescending, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.PersonAccess#findByUsername(java.lang.String,
	 * boolean)
	 */
	public Person findByUsername(String username, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		logger.debug("findByUsername called with username " + username);

		// Grab the DAO
		PersonDAO personDAO = (PersonDAO) this.getMetadataDAO();

		// Now find the persons ID
		return personDAO.findByUsername(username, returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.PersonAccess#findAllUsernames()
	 */
	public Collection<String> findAllUsernames() throws MetadataAccessException {

		logger.debug("findAllUsernames called");

		// Grab the DAO
		PersonDAO personDAO = (PersonDAO) this.getMetadataDAO();

		// Now find the persons ID
		return personDAO.findAllUsernames();
	}

}