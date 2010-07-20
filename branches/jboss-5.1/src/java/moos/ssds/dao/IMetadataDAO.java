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

import org.hibernate.Session;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.IMetadataObject;

/**
 * This interface defines the methods that the Data Access Objects (DAO)s for
 * the SSDS must support. These DAOs do not create their own Hibernate Session.
 * That session must come from the application that is using the DAOs and is set
 * in the DAO when it is constructed. This means that each instance of a DAO is
 * associated with only one Session at a time. This is critical as some DAOs
 * call other DAOs and pass on the same session to make transactions work
 * correctly.
 * <hr>
 * 
 * @stereotype interface
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.10 $
 */
public interface IMetadataDAO {

	/**
	 * This method returns the Hibernate Session object that is being used for
	 * the persistence transactions. This is useful for other DAOs that want to
	 * be using the same session.
	 * 
	 * @return the Hibernate <code>Session</code> that a DAO is currently using
	 *         for its persistence transactions.
	 */
	public Session getSession();

	/**
	 * This method sets the Hibernate Session that will be used in the
	 * persistence transactions of the DAO.
	 * 
	 * @param session
	 *            the Hibernate <code>Session</code> to use for persistence
	 *            transactions
	 */
	public void setSession(Session session);

	/**
	 * This method returns the <code>MetadataObject</code> that was found by
	 * searching for it's identifier (id). If nothing is found, null is
	 * returned. The <code>Class</code> of the object that will be returned
	 * depends on the DAO being called.
	 * 
	 * @param id
	 *            is a <code>java.lang.Long</code> the identifier (at the
	 *            persistence) layer of the <code>IMetadataObject</code> to
	 *            search for.
	 * @param returnFullObjectGraph
	 *            is a boolean that specifies if the caller wants the fully
	 *            instantiated object graph (relationships) returned as metadata
	 *            objects instead of the Hibernate proxies, or just the query
	 *            object itself. If you want the full graph returned specify
	 *            <code>true</code>, otherwise leave it false. <b>NOTE: By
	 *            specifying true, you could be requesting a large object tree
	 *            which will slow things down, so use sparingly</b>
	 * @return the <code>IMetadataObject</code> that has the matching identifier
	 *         (id). If no match was found, null is returned.
	 * @throws MetadataAccessException
	 *             if there was a problem with the parameter or the search
	 */
	public IMetadataObject findById(Long id, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * This method takes in a <code>MetadataObject</code> of the type whose
	 * service is being called (for example, PersonDAO takes in a Person
	 * metadata object) and then returns the ID of the matching object in the
	 * persistent store. It uses the equivalent key (called 'business key') to
	 * find the associated ID. If no match was found, null is returned.
	 * 
	 * @param metadataObject
	 *            is the <code>MetadataObject</code> to search for in the
	 *            persistent store.
	 * @return a <code>Long</code> that is the ID of the equivalent object in
	 *         the persistent store. If no equivalent was found, null is
	 *         returned.
	 * @throws MetadataAccessException
	 */
	public Long findId(IMetadataObject metadataObject)
			throws MetadataAccessException;

	/**
	 * This method returns all available IDs for the object on which the DAO is
	 * called. For example, if this is called on the PersonDAO, all IDs for
	 * Person objects will be returned.
	 * 
	 * @return a <code>Collection</code> of <code>Long</code> that are the IDs
	 *         of the DAO <code>Class</code> that are in the persistent store.
	 * @throws MetadataAccessException
	 *             if something goes wrong
	 */
	public Collection<Long> findAllIDs() throws MetadataAccessException;

	/**
	 * This method returns a count of all the IDs in the database of the
	 * associated <code>IMetadataObject</code>. This is equal to the number of
	 * instances of that <code>IMetadataObject</code> in the persistent store.
	 * 
	 * @return a count of all the <code>IMetadataObject</code>s that are in the
	 *         persistent store that are associated with the DAO class.
	 * @throws MetadataAccessException
	 */
	public int countFindAllIDs() throws MetadataAccessException;

	/**
	 * This method takes in a <code>IMetadataObject</code> and finds the
	 * corresponding persistent object if there is one. It uses the identifier
	 * and/or the business key to locate the persistent equivalent object in the
	 * persistent store.
	 * 
	 * @param metadataObject
	 *            the <code>IMetadataObject</code> to search the persistent
	 *            store for (and bring into the Hibernate session)
	 * @param returnFullObjectGraph
	 *            is a boolean that specifies if the caller wants the fully
	 *            instantiated object graph (relationships) returned as metadata
	 *            objects instead of the Hibernate proxies, or just the query
	 *            object itself. If you want the full graph returned specify
	 *            <code>true</code>, otherwise leave it false. <b>NOTE: By
	 *            specifying true, you could be requesting a large object tree
	 *            which will slow things down, so use sparingly</b>
	 * @return a persistent <code>IMetadataObject</code> that matches
	 *         either/both the ID and the business key of the object supplied.
	 *         Null is returned if the object does not have an equivalent object
	 *         in the persistent store.
	 * @throws MetadataAccessException
	 *             if something strange happens (for example the ID and business
	 *             keys of the two objects do not match)
	 */
	public IMetadataObject findEquivalentPersistentObject(
			IMetadataObject metadataObject, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * This method will return all instances of <code>IMetadataObject</code>s
	 * for the specific services it was called against. For example, if this
	 * method is called on the PersonDAO, a collection of <code>Person</code>
	 * objects will be returned.
	 * 
	 * @param orderByPropertyName
	 *            is the name of the property that you want the service to try
	 *            and order the results by. For example if you wanted them
	 *            sorted by name, this parameter would be &quot;name&quot;
	 * @param ascendingOrDescending
	 *            specifies whether or not the results should be ordered by
	 *            ascending or descending order based on the
	 *            orderByPropertyName. Use <code>MetadataDAO.ASCENDING</code> or
	 *            <code>MetadataDAO.DESCENDING</code> to specify parameter.
	 * @param returnFullObjectGraph
	 *            is a boolean that specifies if the caller wants the fully
	 *            instantiated object graph (relationships) returned as metadata
	 *            objects instead of the Hibernate proxies, or just the query
	 *            object itself. If you want the full graph returned specify
	 *            <code>true</code>, otherwise leave it false. <b>NOTE: By
	 *            specifying true, you could be requesting a large object tree
	 *            which will slow things down, so use sparingly</b>
	 * @return a <code>Collection</code> that contains all instances of the
	 *         <code>IMetadataObject</code>s that match the service they are
	 *         calling against. If nothing is found, an empty collection is
	 *         returned.
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
	 * clients own risk. SQL Statements can break if the underlying database
	 * server is changed or the schema is changed. If you stick to the other
	 * methods defined in the DAO's, they can be better isolated from those
	 * changes, but this method effectively bypasses all business logic and goes
	 * straight to a SQL call on the persistent store. Also note that only
	 * SELECT statements will be allowed, all other (like UPDATE and DELETE)
	 * statements will be rejected.
	 * 
	 * @param sqlString
	 *            the SQL SELECT statement to be used in the query
	 * @param aliasName
	 *            Is a name that can be used to specify a target path (see
	 *            Hibernate documentation for details).
	 * @param classOfReturn
	 *            specifies the metadata object class that will be returned. For
	 *            example, if you are searching for <code>Person</code> classes,
	 *            this parameter would be <code>Person.class</code>.
	 * @param returnFullObjectGraph
	 *            is a boolean that specifies if the caller wants the fully
	 *            instantianted object graph (relationships) returned, or just
	 *            the query object itself. If you want the full graph returned
	 *            specify <code>true</code>, otherwise leave it false. <b>NOTE:
	 *            By specifying true, you could be requesting a large object
	 *            tree which will slow things down, so use sparingly</b>
	 * @return a <code>Collection</code> of <code>Object</code> that are
	 *         returned from the resulting SQL string
	 * @throws MetadataAccessException
	 *             if the query is not a select query or if something goes wrong
	 *             with the call
	 */
	public Collection<? extends IMetadataObject> findBySQL(String sqlString,
			String aliasName, Class<?> classOfReturn,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * This method will return the same <code>IMetadataObject</code> that is
	 * sent in, but with its relevant relationships loaded. This is to prevent
	 * LazyInitializationExceptions while trying to navigate the object graph
	 * associated with the <code>IMetadataObject</code>. <b>Please consult the
	 * documentation of this method on each DAO to find out where the graph
	 * boundaries are extended to. This is very important because to keep things
	 * efficient, the server needs to keep the graph boundaries limited</b>.
	 * 
	 * @param metadataObject
	 *            the <code>IMetadataObject</code> that will be returned with
	 *            its pertinent relationships filled out (non-lazy loaded).
	 * @return the same <code>IMetadataObject</code> as that which was passed
	 *         in, but with its relevant relationships filled out with real
	 *         objects instead of lazy-loaded proxies.
	 * @throws MetadataAccessException
	 *             is something goes wrong.
	 */
	public IMetadataObject getMetadataObjectGraph(IMetadataObject metadataObject)
			throws MetadataAccessException;

	/**
	 * This method takes a <code>IMetadataObject</code> and makes it persistent
	 * which could mean that it inserts a new one, or updates an existing one.
	 * Whichever happens, the ID is returned.
	 * 
	 * @param metadataObject
	 *            is the <code>IMetadataObject</code> that will be persisted. If
	 *            no ID is specified, the business key will be used to try and
	 *            find an object to update. If no equivalent is found, a new one
	 *            will be saved (inserted).
	 * @return a <code>java.lang.Long</code> that is the identifier that the
	 *         persistence mechanism assigned to the persisted object
	 * @throws MetadataAccessException
	 *             is something goes wrong during the persist operation
	 */
	public Long makePersistent(IMetadataObject metadataObject)
			throws MetadataAccessException;

	/**
	 * This method will remove the given <code>IMetadataObject</code> from the
	 * persistent store in the system.
	 * 
	 * @param metadataObject
	 *            is the <code>IMetadataObject</code> to be deleted from the
	 *            persistent store.
	 * @throws MetadataAccessException
	 *             is something goes wrong in the delete
	 */
	public void makeTransient(IMetadataObject metadataObject)
			throws MetadataAccessException;
}
