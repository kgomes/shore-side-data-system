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

import moos.ssds.dao.StandardVariableDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.StandardVariable;

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * Provides a facade that provides client services for StandardVariable objects.
 * 
 * @see moos.ssds.services.metadata.IMetadataAccess
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.10 $
 */
@Stateless
@RemoteBinding(jndiBinding = "moos/ssds/services/metadata/StandardVariableAccess")
@LocalBinding(jndiBinding = "moos/ssds/services/metadata/StandardVariableAccessLocal")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class StandardVariableAccessEJB extends AccessBean implements
		StandardVariableAccess, StandardVariableAccessLocal {

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(StandardVariableAccessEJB.class);

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

		// Now set the super persistent class to StandardVariable
		super.setPersistentClass(StandardVariable.class);
		logger.debug("OK, set Persistent class to StandardVariable");

		// And the DAO
		super.setDaoClass(StandardVariableDAO.class);
		logger.debug("OK, set DAO Class to StandardVariableDAO");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.StandardVariableAccess#
	 * findByNameAndReferenceScale(java.lang.String, java.lang.String)
	 */
	@Override
	public IMetadataObject findByNameAndReferenceScale(String name,
			String referenceScale) throws MetadataAccessException {

		// Grab the DAO
		StandardVariableDAO standardVariableDAO = (StandardVariableDAO) this
				.getMetadataDAO();

		// Now call the associated method
		return standardVariableDAO.findByNameAndReferenceScale(name,
				referenceScale);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.StandardVariableAccess#findByName(java.lang
	 * .String)
	 */
	@Override
	public Collection<StandardVariable> findByName(String name)
			throws MetadataAccessException {

		// Grab the DAO
		StandardVariableDAO standardVariableDAO = (StandardVariableDAO) this
				.getMetadataDAO();

		// Now call the associated method
		return standardVariableDAO.findByName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.StandardVariableAccess#findByLikeName(java
	 * .lang.String)
	 */
	@Override
	public Collection<StandardVariable> findByLikeName(String likeName)
			throws MetadataAccessException {

		// Grab the DAO
		StandardVariableDAO standardVariableDAO = (StandardVariableDAO) this
				.getMetadataDAO();

		// Now call the associated method
		return standardVariableDAO.findByLikeName(likeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.StandardVariableAccess#findByReferenceScale
	 * (java.lang.String)
	 */
	@Override
	public Collection<StandardVariable> findByReferenceScale(
			String referenceScale) throws MetadataAccessException {

		// Grab the DAO
		StandardVariableDAO standardVariableDAO = (StandardVariableDAO) this
				.getMetadataDAO();

		// Now call the associated method
		return standardVariableDAO.findByReferenceScale(referenceScale);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.StandardVariableAccess#findByLikeReferenceScale
	 * (java.lang.String)
	 */
	@Override
	public Collection<StandardVariable> findByLikeReferenceScale(
			String likeReferenceScale) throws MetadataAccessException {

		// Grab the DAO
		StandardVariableDAO standardVariableDAO = (StandardVariableDAO) this
				.getMetadataDAO();

		// Now call the associated method
		return standardVariableDAO.findByLikeReferenceScale(likeReferenceScale);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see moos.ssds.services.metadata.StandardVariableAccess#findAllNames()
	 */
	@Override
	public Collection<String> findAllNames() throws MetadataAccessException {

		// Grab the DAO
		StandardVariableDAO standardVariableDAO = (StandardVariableDAO) this
				.getMetadataDAO();

		// Now call the associated method
		return standardVariableDAO.findAllNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.StandardVariableAccess#findAllReferenceScales
	 * ()
	 */
	@Override
	public Collection<String> findAllReferenceScales()
			throws MetadataAccessException {

		// Grab the DAO
		StandardVariableDAO standardVariableDAO = (StandardVariableDAO) this
				.getMetadataDAO();

		// Now call the associated method
		return standardVariableDAO.findAllReferenceScales();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.metadata.StandardVariableAccess#findByRecordVariable
	 * (moos.ssds.metadata.RecordVariable)
	 */
	@Override
	public StandardVariable findByRecordVariable(RecordVariable recordVariable)
			throws MetadataAccessException {

		// Grab the DAO
		StandardVariableDAO standardVariableDAO = (StandardVariableDAO) this
				.getMetadataDAO();

		// Now call the associated method
		return standardVariableDAO.findByRecordVariable(recordVariable);
	}
}