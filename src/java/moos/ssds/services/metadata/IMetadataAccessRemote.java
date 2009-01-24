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

import java.rmi.RemoteException;
import java.util.Collection;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.IMetadataObject;

/**
 * <p>
 * This interface defines the functions that are provided by the services to
 * access the persistent model classes. This is the remote interface to metadata
 * access services
 * </p>
 * <hr>
 * 
 * @stereotype service
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.12 $
 */
public interface IMetadataAccessRemote {

    /**
     * @see IMetadataAccess#findById(Long)
     * @throws RemoteException
     *             if something goes wrong in the remote call
     */
    public IMetadataObject findById(Long id, boolean returnFullObjectGraph)
        throws MetadataAccessException, RemoteException;

    /**
     * @see IMetadataAccess#findById(long)
     * @throws RemoteException
     *             if something goes wrong with the remote call
     */
    public IMetadataObject findById(long id, boolean returnFullObjectGraph)
        throws MetadataAccessException, RemoteException;

    /**
     * @see IMetadataAccess#findById(String)
     * @throws RemoteException
     *             if something goes wrong with the remote call
     */
    public IMetadataObject findById(String id, boolean returnFullObjectGraph)
        throws MetadataAccessException, RemoteException;

    /**
     * @see IMetadataAccess#findId(IMetadataObject)
     * @throws RemoteException
     *             if something goes wrong with the remote call
     */
    public Long findId(IMetadataObject metadataObject)
        throws MetadataAccessException, RemoteException;

    /**
     * @see IMetadataAccess#findAll()
     * @throws RemoteException
     *             if something goes wrong with the remote call
     */
    public Collection findAllIDs() throws MetadataAccessException,
        RemoteException;

    /**
     * @see IMetadataAccess#findEquivalentPersistentObject(IMetadataObject)
     * @throws RemoteException
     *             if something goes wrong with the remote call
     */
    public IMetadataObject findEquivalentPersistentObject(
        IMetadataObject metadataObject, boolean returnFullObjectGraph)
        throws MetadataAccessException, RemoteException;

    /**
     * @see IMetadataAccess#findAll()
     * @throws RemoteException
     *             if something goes wrong with the remote call
     */
    public Collection findAll(String orderByPropertyName,
        String ascendingOrDescending, boolean returnFullObjectGraph)
        throws MetadataAccessException, RemoteException;

    /**
     * @see IMetadataAccess#findBySQL(String)
     * @throws RemoteException
     *             if something goes wrong with the remote call
     */
    public Collection findBySQL(String sqlString, String aliasName,
        Class classOfReturn, boolean returnFullObjectGraph)
        throws MetadataAccessException, RemoteException;

    /**
     * @see IMetadataAccess#getMetadataObjectGraph(IMetadataObject)
     * @throws RemoteException
     *             if something goes wrong with the remote call
     */
    public IMetadataObject getMetadataObjectGraph(IMetadataObject metadataObject)
        throws MetadataAccessException, RemoteException;

    /**
     * @see IMetadataAccess#getDeepCopy(IMetadataObject)
     * @throws MetadataAccessException
     * @throws RemoteException
     */
    public IMetadataObject getDeepCopy(IMetadataObject metadataObject)
        throws MetadataAccessException, RemoteException;

    /**
     * @see IMetadataAccess#insert(IMetadataObject)
     * @throws RemoteException
     *             if something goes wrong with the remote call
     */
    public Long insert(IMetadataObject insertRecord)
        throws MetadataAccessException, RemoteException;

    /**
     * @see IMetadataAccess#update(IMetadataObject)
     * @throws RemoteException
     *             if something goes wrong with the remote call
     */
    public Long update(IMetadataObject updateRecord)
        throws MetadataAccessException, RemoteException;

    /**
     * @see IMetadataAccess#delete(IMetadataObject)
     * @throws RemoteException
     *             if something goes wrong with the remote call
     */
    public void delete(IMetadataObject deleteRecord)
        throws MetadataAccessException, RemoteException;

    /**
     * @see IMetadataAccess#makePersistent(IMetadataObject)
     * @throws RemoteException
     *             if something goes wrong with the remote call
     */
    public Long makePersistent(IMetadataObject metadataObject)
        throws MetadataAccessException, RemoteException;

    /**
     * @see IMetadataAccess#makeTransient(IMetadataObject)
     * @throws RemoteException
     *             if something goes wrong with the remote call
     */
    public void makeTransient(IMetadataObject metadataObject)
        throws MetadataAccessException, RemoteException;

}
