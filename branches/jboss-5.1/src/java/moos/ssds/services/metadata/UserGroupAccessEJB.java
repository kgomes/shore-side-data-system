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
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import moos.ssds.dao.UserGroupDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.UserGroup;

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * Provides a facade that provides client services for UserGroup objects.
 * 
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.1 $
 */
@Stateless
@RemoteBinding(jndiBinding = "moos/ssds/services/metadata/UserGroupAccess")
@LocalBinding(jndiBinding = "moos/ssds/services/metadata/UserGroupAccessLocal")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class UserGroupAccessEJB extends AccessBean implements UserGroupAccess,
		UserGroupAccessLocal {

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(UserGroupAccessEJB.class);

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

		// Now set the super persistent class to UserGroup
		super.setPersistentClass(UserGroup.class);
		logger.debug("OK, set Persistent class to UserGroup");

		// And the DAO
		super.setDaoClass(UserGroupDAO.class);
		logger.debug("OK, set DAO Class to UserGroupDAO");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.UserGroupAccess#findByGroupName(java.lang
	 * .String, boolean, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<UserGroup> findByGroupName(String groupName,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// Grab the DAO
		UserGroupDAO userGroupDAO = (UserGroupDAO) this.getMetadataDAO();

		// Now call the associated method
		return userGroupDAO.findByGroupName(groupName, exactMatch,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.UserGroupAccess#countFindByGroupName(java
	 * .lang.String, boolean)
	 */
	@Override
	public int countFindByGroupName(String groupName, boolean exactMatch)
			throws MetadataAccessException {

		// Grab the DAO
		UserGroupDAO userGroupDAO = (UserGroupDAO) this.getMetadataDAO();

		// Now call the associated method
		return userGroupDAO.countFindByGroupName(groupName, exactMatch);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.UserGroupAccess#findAllGroupNames()
	 */
	@Override
	public Collection<String> findAllGroupNames()
			throws MetadataAccessException {

		// Grab the DAO
		UserGroupDAO userGroupDAO = (UserGroupDAO) this.getMetadataDAO();

		// Now call the associated method
		return userGroupDAO.findAllGroupNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.UserGroupAccess#countFindAllGroupNames()
	 */
	@Override
	public int countFindAllGroupNames() throws MetadataAccessException {

		// Grab the DAO
		UserGroupDAO userGroupDAO = (UserGroupDAO) this.getMetadataDAO();

		// Now call the associated method
		return userGroupDAO.countFindAllGroupNames();
	}

}