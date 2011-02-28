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

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.IMetadataObject;

/**
 * <p>
 * This interface defines the functions that are provided by the services to
 * access the persistent model classes.
 * </p>
 * <hr>
 * 
 * @stereotype service
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.12 $
 */
public interface IMetadataAccess {

	/**
	 * This method returns the <code>MetadataObject</code> that was found by
	 * searching for it's identifier (id). If nothing is found, null is
	 * returned.
	 * 
	 * @param id
	 *            is a <code>java.lang.Long</code> the identifier (at the
	 *            persistence) layer of the <code>MetadataObject</code> to
	 *            search for.
	 * @return the <code>MetadataObject</code> that has the matching identifier
	 *         (id). If no match was found, null is returned.
	 * @throws MetadataAccessException
	 *             if there was a problem with the parameter or the search
	 */
	public IMetadataObject findById(Long id, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * This method returns the <code>MetadataObject</code> that was found by
	 * searching for it's identifier (id). If nothing is found, null is
	 * returned.
	 * 
	 * @param id
	 *            is a <code>long</code> the identifier (at the persistence)
	 *            layer of the <code>MetadataObject</code> to search for.
	 * @return the <code>MetadataObject</code> that has the matching identifier
	 *         (id). If no match was found, null is returned.
	 * @throws MetadataAccessException
	 *             if there was a problem with the parameter or the search
	 */
	public IMetadataObject findById(long id, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * This method returns the <code>MetadataObject</code> that was found by
	 * searching for it's identifier (id). If nothing is found, null is
	 * returned.
	 * 
	 * @param id
	 *            is a <code>java.lang.String</code> that will be converted to a
	 *            <code>java.lang.Long</code> that is the identifier (at the
	 *            persistence) layer of the <code>MetadataObject</code> to
	 *            search for.
	 * @return the <code>MetadataObject</code> that has the matching identifier
	 *         (id). If no match was found, null is returned.
	 * @throws MetadataAccessException
	 *             if there was a problem with the parameter or the search
	 */
	public IMetadataObject findById(String id, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * This method takes in a <code>MetadataObject</code> of the type whose
	 * service is being called (for example, PersonAccess takes in a Person
	 * metadata object) and then returns the ID of the matching object in the
	 * persistenct store. The uses the equivalent key (called 'business key') to
	 * find the associated ID. If no match was found, null is returned.
	 * 
	 * @param metadataObject
	 *            is the <code>MetadataObject</code> to search for in the
	 *            persistent store.
	 * @return a <code>Long</code> that is the ID of the equivalent object in
	 *         the persistenct store. If no equivalent was found, null is
	 *         returned.
	 * @throws MetadataAccessException
	 */
	public Long findId(IMetadataObject metadataObject)
			throws MetadataAccessException;

	/**
	 * This method returns all available IDs for the object on which the DAO is
	 * called. For example, if this is called on the PersonDAO, all IDs for
	 * Person object will be returned
	 * 
	 * @return a <code>Collection</code> of <code>Long</code> that are the IDs
	 *         of the <code>Person</code> object in the persistent store
	 * @throws MetadataAccessException
	 *             if something goes wrong
	 */
	public Collection<Long> findAllIDs() throws MetadataAccessException;

	/**
	 * This method takes in a <code>IMetadataObject</code> and finds the
	 * corresponding persistent object if there is one. It uses the identifier
	 * and/or the business key to locate the persistent equivalent
	 * 
	 * @param metadataObject
	 *            the <code>IMetadataObject</code> to search the persistent
	 *            store for (and bring into the Hibernate session)
	 * @return an persistent <code>IMetadataObject</code> that matches
	 *         either/both the ID and the business key of the object supplied.
	 *         Null is returned if the object does not have an equivalent
	 * @throws MetadataAccessException
	 *             if something strange happens
	 */
	public IMetadataObject findEquivalentPersistentObject(
			IMetadataObject metadataObject, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * This method will return all instances of <code>MetadataObject</code>s for
	 * the specific services it was called again.
	 * 
	 * @param orderByPropertyName
	 *            TODO kgomes document this
	 * @param returnFullObjectGraph
	 *            TODO kgomes document this
	 * @return a <code>Collection</code> that contains all instances of the
	 *         <code>MetadataObject</code>s that match the service they are
	 *         calling against. If nothing is found, null is returned.
	 * @throws MetadataAccessException
	 *             if something goes wrong with the search
	 */
	public Collection<? extends IMetadataObject> findAll(
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * This method will take in a <code>String</code> that is a SQL statement
	 * and run that against the database that backs the persistent storage, then
	 * return the results. Please note that this method is provided as a
	 * convenience method and clients are warned that it is to be used at the
	 * clients own risk. SQL Statements can breaks if the underlying database
	 * server is changed or the schema is changed. If you stick to the other
	 * methods defined in the DAO's, they can be better isolated from those
	 * changes, but this method effectively bypasses all business logic and goes
	 * straight to a SQL call on the persistent store. Also note that only
	 * SELECT statements will be allowed, all UPDATE and DELETE statements will
	 * be rejected.
	 * 
	 * @param sqlString
	 *            the SQL statement to be used in the query
	 * @return a <code>Collection</code> of <code>Object</code> that are
	 *         returned from the resulting SQL string
	 * @throws MetadataAccessException
	 *             if the query is not a select query or if something goes wrong
	 *             with the call
	 */
	@SuppressWarnings("rawtypes")
	public Collection<? extends IMetadataObject> findBySQL(String sqlString,
			String aliasName, Class classOfReturn, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * This method will return the same MetadataObject that is sent in, but with
	 * its relevant relationships loaded. This is to prevent
	 * LazyInitializationExceptions while trying to navigate the object graph
	 * associated with the MetadataObject. <b>Please consult the documentation
	 * of this method on each DAO to find out where the graph boundaries are
	 * extended to. This is very important because to keep things efficient, the
	 * server needs to keep the graph boundaries limited</b>.
	 * 
	 * @param metadataObject
	 *            the <code>IMetadataObject</code> that will be returned with
	 *            its pertinent relationships filled out (non-lazy loaded).
	 * @return the same <code>IMetadataObject</code> as that which was passed
	 *         in, but with it relevant relationships filled out with real
	 *         object instead of lazy-loaded proxies.
	 * @throws MetadataAccessException
	 *             is something goes wrong.
	 */
	public IMetadataObject getMetadataObjectGraph(IMetadataObject metadataObject)
			throws MetadataAccessException;

	/**
	 * This method will return a deep copy of the object that is specified
	 * (assuming it is in the persistent store). The deep copy will have fields
	 * copied, the IDs will be nulled and the relationships will be filled to a
	 * certain extent. If you want details on how deep the copies go, look at
	 * the underlying DAO documentation
	 * 
	 * @param metadataObject
	 *            the <code>IMetadataObject</code> to return a copy of
	 * @return a &quot;deep&quot; copy of the IMetadataObject supplied. Null
	 *         will be returned if a matching object is not found in the
	 *         peristence mechanism
	 * @throws MetadataAccessException
	 */
	public IMetadataObject getDeepCopy(IMetadataObject metadataObject)
			throws MetadataAccessException;

	/**
	 * This method inserts the <code>MetadataObject</code> into the persistence
	 * of the system.
	 * 
	 * @param insertRecord
	 *            is the <code>MetadataObject</code> that will be inserted into
	 *            the persistence system
	 * @return a <code>java.lang.Long</code> that is the identifier that the
	 *         persistence mechanism assigned to the newly inserted object
	 * @throws MetadataAccessException
	 *             is something goes wrong during the insert
	 */
	public Long insert(IMetadataObject insertRecord)
			throws MetadataAccessException;

	/**
	 * This method will update the given <code>MetadatdObject</code> in the
	 * persistenct store.
	 * 
	 * @param updateRecord
	 *            is the <code>MetadataObject</code> that will be updated
	 * @return a <code>java.lang.Long</code> that is the identifier of the
	 *         updated <code>MetadataObject</code>
	 * @throws MetadataAccessException
	 *             if something goes wrong during the update
	 */
	public Long update(IMetadataObject updateRecord)
			throws MetadataAccessException;

	/**
	 * This method will remove the given <code>MetadataObject</code> from the
	 * persistent store in the system
	 * 
	 * @param deleteRecord
	 *            is the <code>MetadataObject</code> to be deleted.
	 * @throws MetadataAccessException
	 *             is something goes wrong in the delete
	 */
	public void delete(IMetadataObject deleteRecord)
			throws MetadataAccessException;

	/**
	 * This method takes a <code>MetadataObject</code> and makes it persistent
	 * which could mean that it inserts a new one, or updates an existing one.
	 * Whichever happens, the ID is returned.
	 * 
	 * @param metadataObject
	 *            is the <code>MetadataObject</code> that will be persisted
	 * @return a <code>java.lang.Long</code> that is the identifier that the
	 *         persistence mechanism assigned to the persisted object
	 * @throws MetadataAccessException
	 *             is something goes wrong during the insert
	 */
	public Long makePersistent(IMetadataObject metadataObject)
			throws MetadataAccessException;

	/**
	 * This method will remove the given <code>MetadataObject</code> from the
	 * persistent store in the system
	 * 
	 * @param metadataObject
	 *            is the <code>MetadataObject</code> to be deleted from the
	 *            persistent store.
	 * @throws MetadataAccessException
	 *             is something goes wrong in the delete
	 */
	public void makeTransient(IMetadataObject metadataObject)
			throws MetadataAccessException;
}
