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
package moos.ssds.dao;

import java.util.Collection;
import java.util.Iterator;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.StandardReferenceScale;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class StandardReferenceScaleDAO extends MetadataDAO {

	/**
	 * This is the constructor that calls the super constructor and sets the
	 * proper class and Hibernate Session
	 * 
	 * @param session
	 *            is the <code>Session</code> that will be used in the
	 *            persistent operations
	 * @throws MetadataAccessException
	 *             if something goes weird
	 */
	public StandardReferenceScaleDAO(Session session)
			throws MetadataAccessException {
		super(StandardReferenceScale.class, session);
	}

	public IMetadataObject findEquivalentPersistentObject(
			IMetadataObject metadataObject, boolean returnFullObjectGraph)
			throws MetadataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection findAllIDs() throws MetadataAccessException {
		return null;
	}

	/**
	 * @see IMetadataDAO#countFindAllIDs()
	 */
	public int countFindAllIDs() throws MetadataAccessException {
		// The count
		int count = 0;
		try {
			Long longCount = (Long) getSession().createQuery(
					"select count(distinct standardReferenceScale.id) from "
							+ "StandardReferenceScale standardReferenceScale")
					.uniqueResult();
			if (longCount != null)
				count = longCount.intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the result
		return count;
	}

	public Collection findByName(String name) throws MetadataAccessException {
		return null;
	}

	public Collection findByLikeName(String likeNames)
			throws MetadataAccessException {
		return null;
	}

	public Collection findAllNames() throws MetadataAccessException {
		return null;
	}

	/**
	 * @see IMetadataDAO#makePersistent(IMetadataObject)
	 */
	public Long makePersistent(IMetadataObject metadataObject)
			throws MetadataAccessException {

		logger.debug("makePersistent called");

		// A flag to indicate if it was previously persisted
		boolean persistedBefore = false;

		// Check incoming object
		StandardReferenceScale standardReferenceScale = this
				.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		StandardReferenceScale persistentStandardReferenceScale = (StandardReferenceScale) this
				.findEquivalentPersistentObject(standardReferenceScale, false);

		// Create a handle to the StandardReferenceScale that will really be
		// persisted
		StandardReferenceScale standardReferenceScaleToPersist = null;

		// If there is a persistent one, copy over any non-null, changed fields
		// and assign to the persistent handle
		if (persistentStandardReferenceScale != null) {
			String standardReferenceScaleBefore = persistentStandardReferenceScale
					.toStringRepresentation("<li>");
			if (this.updateDestinationObject(standardReferenceScale,
					persistentStandardReferenceScale)) {
				addMessage(ssdsAdminEmailToAddress,
						"A StandardReferenceScale was changed in SSDS:<br><b>Before</b><ul><li>"
								+ standardReferenceScaleBefore
								+ "</ul><br><b>After</b><br><ul><li>"
								+ persistentStandardReferenceScale
										.toStringRepresentation("<li>")
								+ "</ul><br>");
			}

			// Set the flag
			persistedBefore = true;

			// Attach to the handle
			standardReferenceScaleToPersist = persistentStandardReferenceScale;
		} else {
			// Since this is a new StandardReferenceScale, make sure the
			// alternate key
			// is there
			if ((standardReferenceScale.getName() == null)
					|| (standardReferenceScale.getName().equals(""))) {
				try {
					standardReferenceScale.setName("StandardReferenceScale_"
							+ getUniqueNameSuffix());
				} catch (MetadataException e) {
					logger
							.error("MetadataException caught trying to "
									+ "auto-generate a name for a StandardReferenceScale: "
									+ e.getMessage());
				}
				addMessage(ssdsAdminEmailToAddress,
						"An incoming StandardReferenceScale did not have a name, "
								+ "so SSDS auto-generated one:<br><ul><li>"
								+ standardReferenceScale
										.toStringRepresentation("<li>")
								+ "</ul><br>");
			}

			// Clear the flag
			persistedBefore = false;

			// Attach to persisting handle
			standardReferenceScaleToPersist = standardReferenceScale;
		}

		// If it was not persisted before, save it
		if (!persistedBefore) {
			getSession().save(standardReferenceScaleToPersist);
			addMessage(ssdsAdminEmailToAddress,
					"A new StandardReferenceScale was inserted into SSDS: <br><ul><li>"
							+ standardReferenceScaleToPersist
									.toStringRepresentation("<li>")
							+ "</ul><br>");
		}

		// Now return the ID
		if (standardReferenceScaleToPersist != null) {
			return standardReferenceScaleToPersist.getId();
		} else {
			return null;
		}
	}

	/**
	 * @see IMetadataDAO#makeTransient(IMetadataObject)
	 */
	public void makeTransient(IMetadataObject metadataObject)
			throws MetadataAccessException {

		logger.debug("makeTransient called");

		// Check incoming object
		StandardReferenceScale standardReferenceScale = this
				.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		StandardReferenceScale persistentStandardReferenceScale = (StandardReferenceScale) this
				.findEquivalentPersistentObject(standardReferenceScale, false);

		// If no matching standardReferenceScale was found, do nothing
		if (persistentStandardReferenceScale == null) {
			logger
					.debug("No matching standardReferenceScale could be found in the persistent store, "
							+ "no delete performed");
		} else {
			// Clear any associations with RecordVariable
			Collection recordVariablesByStandardReferenceScale = null;
			RecordVariableDAO recordVariableDAO = new RecordVariableDAO(
					getSession());
			recordVariablesByStandardReferenceScale = recordVariableDAO
					.findByStandardReferenceScale(
							persistentStandardReferenceScale, null, null, false);
			if (recordVariablesByStandardReferenceScale != null) {
				Iterator recordVariablesIterator = recordVariablesByStandardReferenceScale
						.iterator();
				while (recordVariablesIterator.hasNext()) {
					RecordVariable recordVariable = (RecordVariable) recordVariablesIterator
							.next();
					recordVariable.setStandardReferenceScale(null);
				}
			}

			logger
					.debug("Existing object was found, so we will try to delete it");
			try {
				getSession().delete(persistentStandardReferenceScale);
				addMessage(ssdsAdminEmailToAddress,
						"A StandardReferenceScale was removed from SSDS:<br><ul><li>"
								+ persistentStandardReferenceScale
										.toStringRepresentation("<li>")
								+ "</ul><br>");
			} catch (HibernateException e) {
				logger.error("HibernateException caught (will be re-thrown):"
						+ e.getMessage());
				throw new MetadataAccessException(e);
			}
		}
	}

	/**
	 * This method checks to make sure an incoming <code>MetadataObject</code>
	 * is not null and is in fact of the correct class. It then converts it to
	 * the correct class and returns it
	 * 
	 * @param metadataObject
	 *            the <code>MetadataObject</code> to check and return as a
	 *            <code>StandardReferenceScale</code>
	 * @return a <code>StandardReferenceScale</code> that is same object that
	 *         came in
	 * @throws MetadataAccessException
	 *             if something is wrong
	 */
	private StandardReferenceScale checkIncomingMetadataObject(
			IMetadataObject metadataObject) throws MetadataAccessException {

		// Check for null argument
		if (metadataObject == null) {
			throw new MetadataAccessException(
					"Failed: incoming StandardReferenceScale was null");
		}

		// Try to cast the incoming object into the correct class
		StandardReferenceScale standardReferenceScale = null;
		try {
			standardReferenceScale = (StandardReferenceScale) metadataObject;
		} catch (ClassCastException cce) {
			throw new MetadataAccessException(
					"Could not cast the incoming object into a StandardReferenceScale");
		}
		return standardReferenceScale;
	}

//	protected void initializeRelationships(IMetadataObject metadataObject)
//			throws MetadataAccessException {
//
//	}

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(StandardReferenceScaleDAO.class);

}
