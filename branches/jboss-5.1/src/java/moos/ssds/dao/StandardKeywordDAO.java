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
import moos.ssds.metadata.StandardKeyword;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class StandardKeywordDAO extends MetadataDAO {

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
	public StandardKeywordDAO(Session session) throws MetadataAccessException {
		super(StandardKeyword.class, session);
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
					"select count(distinct standardKeyword.id) from "
							+ "StandardKeyword standardKeyword").uniqueResult();
			if (longCount != null)
				count = longCount.intValue();
		} catch (HibernateException e) {
			throw new MetadataAccessException(e);
		}

		// Return the result
		return count;
	}

	public Collection<StandardKeyword> findByName(String name)
			throws MetadataAccessException {
		return null;
	}

	public Collection<StandardKeyword> findByLikeName(String likeName)
			throws MetadataAccessException {
		return null;
	}

	public Collection<String> findAllNames() throws MetadataAccessException {
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
		StandardKeyword standardKeyword = this
				.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		StandardKeyword persistentStandardKeyword = (StandardKeyword) this
				.findEquivalentPersistentObject(standardKeyword, false);

		// Create a handle to the StandardKeyword that will really be persisted
		StandardKeyword standardKeywordToPersist = null;

		// If there is a persistent one, copy over any non-null, changed fields
		// and assign to the persistent handle
		if (persistentStandardKeyword != null) {
			String standardKeywordBefore = persistentStandardKeyword
					.toStringRepresentation("<li>");
			if (this.updateDestinationObject(standardKeyword,
					persistentStandardKeyword)) {
				addMessage(
						ssdsAdminEmailToAddress,
						"A StandardKeyword was changed in SSDS:<br><b>Before</b><ul><li>"
								+ standardKeywordBefore
								+ "</ul><br><b>After</b><br><ul><li>"
								+ persistentStandardKeyword
										.toStringRepresentation("<li>")
								+ "</ul><br>");
			}

			// Set the flag
			persistedBefore = true;

			// Attach to the handle
			standardKeywordToPersist = persistentStandardKeyword;
		} else {
			// Since this is a new StandardKeyword, make sure the alternate key
			// is there
			if ((standardKeyword.getName() == null)
					|| (standardKeyword.getName().equals(""))) {
				try {
					standardKeyword.setName("StandardKeyword_"
							+ getUniqueNameSuffix());
				} catch (MetadataException e) {
					logger.error("MetadataException caught trying to "
							+ "auto-generate a name for a StandardKeyword: "
							+ e.getMessage());
				}
				addMessage(
						ssdsAdminEmailToAddress,
						"An incoming StandardKeyword did not have a name, "
								+ "so SSDS auto-generated one:<br><ul><li>"
								+ standardKeyword
										.toStringRepresentation("<li>")
								+ "</ul><br>");
			}

			// Clear the flag
			persistedBefore = false;

			// Attach to persisting handle
			standardKeywordToPersist = standardKeyword;
		}

		// If it was not persisted before, save it
		if (!persistedBefore) {
			getSession().save(standardKeywordToPersist);
			addMessage(
					ssdsAdminEmailToAddress,
					"A new StandardKeyword was inserted into SSDS: <br><ul><li>"
							+ standardKeywordToPersist
									.toStringRepresentation("<li>")
							+ "</ul><br>");
		}

		// Now return the ID
		if (standardKeywordToPersist != null) {
			return standardKeywordToPersist.getId();
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
		StandardKeyword standardKeyword = this
				.checkIncomingMetadataObject(metadataObject);

		// Check the persistent store for the matching object
		StandardKeyword persistentStandardKeyword = (StandardKeyword) this
				.findEquivalentPersistentObject(standardKeyword, false);

		// If no matching standardKeyword was found, do nothing
		if (persistentStandardKeyword == null) {
			logger.debug("No matching standardKeyword could be found in the persistent store, "
					+ "no delete performed");
		} else {
			// Clear any associations with RecordVariable
			Collection recordVariablesByStandardKeyword = null;
			RecordVariableDAO recordVariableDAO = new RecordVariableDAO(
					getSession());
			recordVariablesByStandardKeyword = recordVariableDAO
					.findByStandardKeyword(persistentStandardKeyword, null,
							null, false);
			if (recordVariablesByStandardKeyword != null) {
				Iterator recordVariablesIterator = recordVariablesByStandardKeyword
						.iterator();
				while (recordVariablesIterator.hasNext()) {
					RecordVariable recordVariable = (RecordVariable) recordVariablesIterator
							.next();
					recordVariable.setStandardKeyword(null);
				}
			}

			logger.debug("Existing object was found, so we will try to delete it");
			try {
				getSession().delete(persistentStandardKeyword);
				addMessage(
						ssdsAdminEmailToAddress,
						"A StandardKeyword was removed from SSDS:<br><ul><li>"
								+ persistentStandardKeyword
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
	 *            <code>StandardKeyword</code>
	 * @return a <code>StandardKeyword</code> that is same object that came in
	 * @throws MetadataAccessException
	 *             if something is wrong
	 */
	private StandardKeyword checkIncomingMetadataObject(
			IMetadataObject metadataObject) throws MetadataAccessException {

		// Check for null argument
		if (metadataObject == null) {
			throw new MetadataAccessException(
					"Failed: incoming StandardKeyword was null");
		}

		// Try to cast the incoming object into the correct class
		StandardKeyword standardKeyword = null;
		try {
			standardKeyword = (StandardKeyword) metadataObject;
		} catch (ClassCastException cce) {
			throw new MetadataAccessException(
					"Could not cast the incoming object into a StandardKeyword");
		}
		return standardKeyword;
	}

	// protected void initializeRelationships(IMetadataObject metadataObject)
	// throws MetadataAccessException {
	//
	// }

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(StandardKeywordDAO.class);

}
