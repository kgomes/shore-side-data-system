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

import moos.ssds.dao.RecordVariableDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.RecordVariable;

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * Provides a facade that provides client services for RecordVariable objects.
 * 
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: mccann $
 * @version : $Revision: 1.1.2.1 $
 */
@Stateless
@RemoteBinding(jndiBinding = "moos/ssds/services/metadata/RecordVariableAccess")
@LocalBinding(jndiBinding = "moos/ssds/services/metadata/RecordVariableAccessLocal")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class RecordVariableAccessEJB extends AccessBean implements
		RecordVariableAccess, RecordVariableAccessLocal {

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(RecordVariableAccessEJB.class);

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

		// Now set the super persistent class to RecordVariable
		super.setPersistentClass(RecordVariable.class);
		logger.debug("OK, set Persistent class to RecordVariable");

		// And the DAO
		super.setDaoClass(RecordVariableDAO.class);
		logger.debug("OK, set DAO Class to RecordVariableDAO");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.RecordVariableAccess#findByName(java.lang
	 * .String, boolean, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Collection<RecordVariable> findByName(String name,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException {

		// Grab the DAO
		RecordVariableDAO recordVariableDAO = (RecordVariableDAO) this
				.getMetadataDAO();

		// Now call the associated method
		return recordVariableDAO.findByName(name, exactMatch,
				orderByPropertyName, ascendingOrDescending,
				returnFullObjectGraph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.RecordVariableAccess#findAllNames()
	 */
	@Override
	public Collection<String> findAllNames() throws MetadataAccessException {

		// Grab the DAO
		RecordVariableDAO recordVariableDAO = (RecordVariableDAO) this
				.getMetadataDAO();

		// Now call the associated method
		return recordVariableDAO.findAllNames();
	}
}
