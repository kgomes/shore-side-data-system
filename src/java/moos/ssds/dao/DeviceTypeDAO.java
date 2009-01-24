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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.DeviceType;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * This Data Access Object (DAO) provides methods for interacting with the
 * persitence mechanism that handles the persistence of <code>DeviceType</code>
 * objects. It also provides query methods.
 * <hr>
 * 
 * @stereotype service
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.13 $
 */
public class DeviceTypeDAO extends MetadataDAO {

    /**
     * This is the constructor that calls the super constructor and sets the
     * proper class and Hibernate Session
     * 
     * @param session
     *            is the <code>Session</code> that will be used in the
     *            persistent operations
     * @throws MetadataAccessException
     *             if something goes awry
     */
    public DeviceTypeDAO(Session session) throws MetadataAccessException {
        super(DeviceType.class, session);
    }

    /**
     * @see IMetadataDAO#findEquivalentPersistentObject(IMetadataObject)
     */
    public IMetadataObject findEquivalentPersistentObject(
        IMetadataObject metadataObject, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // Check the incoming object
        DeviceType deviceType = this
            .checkIncomingMetadataObject(metadataObject);

        // The DeviceType to be returned
        DeviceType deviceTypeToReturn = null;

        // Now check the ID and if there is one, search by it
        if (deviceType.getId() != null) {
            Criteria criteria = this.formulatePropertyCriteria(false,
                deviceType.getId(), null, false, null, null);
            deviceTypeToReturn = (DeviceType) criteria.uniqueResult();
        }

        // If it was not found, try by name
        if ((deviceTypeToReturn == null) && (deviceType.getName() != null)) {
            Criteria criteria = this.formulatePropertyCriteria(false, null,
                deviceType.getName(), true, null, null);
            deviceTypeToReturn = (DeviceType) criteria.uniqueResult();
        }

        // Check for relationship initialization
        if (returnFullObjectGraph)
            initializeRelationships(deviceTypeToReturn);

        return deviceTypeToReturn;
    }

    /**
     * This method returns a <code>Collection</code> of <code>Long</code>s
     * that are the IDs of all the deviceTypes that are in SSDS.
     * 
     * @return a <code>Collection</code> of <code>Long</code>s that are the
     *         IDs of all deviceTypes in SSDS.
     * @throws MetadataAccessException
     *             if something goes wrong in the method call.
     */
    public Collection findAllIDs() throws MetadataAccessException {
        Collection deviceTypeIDs = null;

        // Create the query and run it
        try {
            Query query = getSession().createQuery(
                "select distinct deviceType.id from "
                    + "DeviceType deviceType order by deviceType.id");
            deviceTypeIDs = query.list();
        } catch (HibernateException e) {
            throw new MetadataAccessException(e);
        }
        return deviceTypeIDs;
    }

    /**
     * @see IMetadataDAO#countFindAllIDs()
     */
    public int countFindAllIDs() throws MetadataAccessException {
        // The count
        int count = 0;
        try {
            Long longCount = (Long) getSession().createQuery(
                "select count(distinct deviceType.id) from "
                    + "DeviceType deviceType").uniqueResult();
            if (longCount != null)
                count = longCount.intValue();
        } catch (HibernateException e) {
            throw new MetadataAccessException(e);
        }

        // Return the result
        return count;
    }

    /**
     * This method tries to look up and instantiate a <code>DeviceType</code>
     * by its name. This is looking for an exact match.
     * 
     * @param username
     *            is a <code>java.lang.String</code> that will be used to
     *            search for matches of a DeviceType's name
     * @return a <code>MetadataObject</code> of class <code>DeviceType</code>
     *         that has a name that matches the one specified. If no matches
     *         were found, null will be returned
     * @throws MetadataAccessException
     *             if something goes wrong with the search
     */
    public DeviceType findByName(String name, boolean returnFullObjectGraph)
        throws MetadataAccessException {

        // First check to see if the name is null
        logger.debug("findByName called with name = " + name);
        if ((name == null) || (name.equals(""))) {
            return null;
        }

        // The DeviceType to return
        DeviceType deviceTypeToReturn = null;

        Criteria criteria = this.formulatePropertyCriteria(false, null, name,
            true, null, null);
        deviceTypeToReturn = (DeviceType) criteria.uniqueResult();

        // Check for relationship initiation
        if (returnFullObjectGraph)
            initializeRelationships(deviceTypeToReturn);

        // Return the first deviceType
        return deviceTypeToReturn;
    }

    /**
     * This method looks for all deviceTypes whose name contain the name
     * supplied. It could be an exact match of just contain the name. For you
     * wildcard folks, it is basically looking for all devices whose names match
     * *likeName*.
     * 
     * @param likeName
     *            is the name that will be used to search for. In SQL terms, it
     *            will do a LIKE '%likeName%'
     * @return a <code>Collection</code> of <code>DeviceType</code>s that
     *         have names like the one specified as the parameter.
     */
    public Collection findByLikeName(String likeName,
        String orderByPropertyName, String ascendingOrDescending,
        boolean returnFullObjectGraph) throws MetadataAccessException {

        // Make sure argument is not null
        logger.debug("likeName = " + likeName);
        if ((likeName == null) && (likeName.equals(""))) {
            return new ArrayList();
        }

        // The collection to be returned
        Collection results = new ArrayList();

        Criteria criteria = this.formulatePropertyCriteria(false, null,
            likeName, false, orderByPropertyName, ascendingOrDescending);
        results = criteria.list();

        // Check for relationship initialization
        if (returnFullObjectGraph)
            initializeRelationships(results);

        // Return the results
        return results;
    }

    /**
     * This method returns a <code>Collection</code> of <code>String</code>s
     * that are the names of all the deviceTypes that are in SSDS.
     * 
     * @return a <code>Collection</code> of <code>String</code>s that are
     *         the names of all deviceTypes in SSDS.
     * @throws MetadataAccessException
     *             if something goes wrong in the method call.
     */
    public Collection findAllNames() throws MetadataAccessException {
        Collection deviceTypeNames = null;

        // Create the query and run it
        try {
            Query query = getSession().createQuery(
                "select distinct deviceType.name from "
                    + "DeviceType deviceType order by deviceType.name");
            deviceTypeNames = query.list();
        } catch (HibernateException e) {
            throw new MetadataAccessException(e);
        }
        return deviceTypeNames;
    }

    /**
     * @see IMetadataDAO#makePersistent(IMetadataObject)
     */
    public Long makePersistent(IMetadataObject metadataObject)
        throws MetadataAccessException {

        logger.debug("makePersistent called");
        // A boolean flag to indicate if the object has been persisted before
        boolean persistedBefore = false;

        // Check incoming object
        DeviceType deviceType = this
            .checkIncomingMetadataObject(metadataObject);

        // Look for the persistent equivalent
        DeviceType persistentDeviceType = (DeviceType) this
            .findEquivalentPersistentObject(metadataObject, false);

        // A DeviceType that will be the one all changes are applied to
        DeviceType deviceTypeToPersist = null;

        // If there is an existing deviceType, copy over the incoming non-null
        // fields if they are different so real changes will be updated
        if (persistentDeviceType != null) {
            logger.debug("The search for an existing object returned "
                + persistentDeviceType.toStringRepresentation("|"));
            String deviceTypeStringBefore = persistentDeviceType
                .toStringRepresentation("<li>");
            if (this.updateDestinationObject(deviceType, persistentDeviceType)) {
                addMessage(ssdsAdminEmailToAddress,
                    "A DeviceType was changed in the "
                        + "Shore-Side Data System:<br><b>Before:</b><ul><li>"
                        + deviceTypeStringBefore
                        + "</ul><br><b>After</b><ul><li>"
                        + persistentDeviceType.toStringRepresentation("<li>")
                        + "</ul><br>");
            }

            // Set the flag
            persistedBefore = true;

            // Set the one to change
            deviceTypeToPersist = persistentDeviceType;

        } else {
            logger.debug("No matching device type found in the database");
            // Now if the name was not specified, let's create one so the
            // persistence can still happen
            if ((deviceType.getName() == null)
                || (deviceType.getName().equals(""))) {
                try {
                    deviceType.setName("DeviceType_" + getUniqueNameSuffix());
                } catch (MetadataException e) {
                    logger.error("Error trying to set the DeviceType "
                        + "name to an autogenerated name: " + e.getMessage());
                }
                addMessage(ssdsAdminEmailToAddress,
                    "An incoming device type had no name, so one was assigned dynamically:<br><ul>"
                        + deviceType.toStringRepresentation("<li>")
                        + "</ul><br>");
            }

            // Clear the persisted flag
            persistedBefore = false;

            // Set the new one to be persisted
            deviceTypeToPersist = deviceType;
        }

        // Now if not persisted before, save it
        if (!persistedBefore) {
            getSession().save(deviceTypeToPersist);
            addMessage(ssdsAdminEmailToAddress,
                "A new DeviceType was entered into SSDS:<br><ul>"
                    + deviceTypeToPersist.toStringRepresentation("<li>")
                    + "</ul><br>");
        }

        // Now return the ID
        if (deviceTypeToPersist != null) {
            return deviceTypeToPersist.getId();
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
        DeviceType deviceType = this
            .checkIncomingMetadataObject(metadataObject);

        // Check the persistent store for the matching object
        DeviceType persistentDeviceType = (DeviceType) this
            .findEquivalentPersistentObject(deviceType, false);
        if (persistentDeviceType != null)
            logger.debug("The search for existing object returned "
                + persistentDeviceType.toStringRepresentation("|"));

        // If no matching deviceType was found, throw an exception
        if (persistentDeviceType == null) {
            logger
                .debug("No matching deviceType could be found in the persistent store, "
                    + "no delete performed");
        } else {
            // Now before removing, let's break any ties with Devices
            Collection devicesByType = null;
            DeviceDAO deviceDAO = new DeviceDAO(this.getSession());
            devicesByType = deviceDAO.findByDeviceType(persistentDeviceType,
                null, null, true);
            if (devicesByType != null) {
                Iterator iter = devicesByType.iterator();
                while (iter.hasNext()) {
                    Device device = (Device) iter.next();
                    device.setDeviceType(null);
                }
            }

            logger.debug("Now let's try to delete it");
            try {
                getSession().delete(persistentDeviceType);
                addMessage(ssdsAdminEmailToAddress,
                    "A DeviceType was removed from SSDS<br><ul><li>"
                        + persistentDeviceType.toStringRepresentation("<li>")
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
     *            <code>DeviceType</code>
     * @return a <code>DeviceType</code> that is same object that came in
     * @throws MetadataAccessException
     *             if something is wrong
     */
    private DeviceType checkIncomingMetadataObject(
        IMetadataObject metadataObject) throws MetadataAccessException {

        // Check for null argument
        if (metadataObject == null) {
            throw new MetadataAccessException(
                "Failed: incoming DeviceType was null");
        }

        // Try to cast the incoming object into the correct class
        DeviceType deviceType = null;
        try {
            deviceType = (DeviceType) metadataObject;
        } catch (ClassCastException cce) {
            throw new MetadataAccessException(
                "Could not cast the incoming object into a DeviceType");
        }
        return deviceType;
    }

    private Criteria formulatePropertyCriteria(boolean countQuery, Long id,
        String name, boolean exactNameMatch, String orderByProperty,
        String ascendingOrDescending) throws MetadataAccessException {
        // The Criteria to return
        Criteria criteria = getSession().createCriteria(DeviceType.class);
        // Make the return distinct
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        if (id != null) {
            criteria.add(Restrictions.eq("id", id));
        } else {
            // Name
            if ((name != null) && (!name.equals(""))) {
                if (exactNameMatch) {
                    criteria.add(Restrictions.eq("name", name));
                } else {
                    criteria.add(Restrictions.like("name", "%" + name + "%"));
                }
            }
        }
        // Setup if a count query, if not add fetching and ordering
        if (countQuery) {
            criteria.setProjection(Projections.rowCount());
        } else {
            addOrderByCriteria(criteria, orderByProperty, ascendingOrDescending);
        }
        return criteria;
    }

    protected void initializeRelationships(IMetadataObject metadataObject)
        throws MetadataAccessException {
    // For DeviceType, nothing is done
    }

    /**
     * The Log4J Logger
     */
    static Logger logger = Logger.getLogger(DeviceTypeDAO.class);

}
