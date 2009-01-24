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
import java.util.Date;
import java.util.Iterator;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.Event;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.util.DateUtils;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class EventDAO extends MetadataDAO {

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
    public EventDAO(Session session) throws MetadataAccessException {
        super(Event.class, session);
    }

    /**
     * @see IMetadataDAO#findEquivalentPersistentObject(IMetadataObject)
     */
    public IMetadataObject findEquivalentPersistentObject(
        IMetadataObject metadataObject, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        logger.debug("findEquivalentPersistentObject called");
        // First try to cast to a Event
        Event event = this.checkIncomingMetadataObject(metadataObject);

        // The id that will be returned
        Event eventToReturn = null;
        if (event.getId() != null)
            eventToReturn = (Event) this.findById(event.getId(), false);
        if (eventToReturn == null)
            eventToReturn = this.findByNameAndDates(event.getName(), event
                .getStartDate(), event.getEndDate());

        // Double check that if the incoming event has an ID, it matches
        // the one that was found with the matching URI string
        if ((event.getId() != null) && (eventToReturn != null)) {
            if (event.getId().longValue() != eventToReturn.getId().longValue()) {
                logger.error("The ID and the dates of the incoming Event "
                    + "did not match a ID/dates combination of "
                    + "anything in the persistent store, this should "
                    + "not happen");
                throw new MetadataAccessException(
                    "The ID and the dates of the incoming Event "
                        + "did not match a ID/dates combination of "
                        + "anything in the persistent store, this should "
                        + "not happen");
            }
        }

        if ((returnFullObjectGraph) && (eventToReturn != null))
            eventToReturn = (Event) this.getMetadataObjectGraph(eventToReturn);

        logger.debug("OK, returning the persistent event: " + eventToReturn);
        return eventToReturn;
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
                "select count(distinct event.id) from " + "Event event")
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

    public Collection findByLikeName(String likeName)
        throws MetadataAccessException {
        return null;
    }

    public Collection findAllNames() throws MetadataAccessException {
        return null;
    }

    public Event findByNameAndDates(String name, Date startDate, Date endDate)
        throws MetadataAccessException {
        // First check to see if the parameters are OK
        if ((name == null) || (name.equals("")) || (startDate == null)
            || (endDate == null))
            return null;

        // Check to make sure they are in correct sequence
        if (endDate.before(startDate))
            throw new MetadataAccessException(
                "End date specified is before the start date. "
                    + "Now that just doesn't make sense now does it?");

        // Create of window around the start and end dates to effectively remove
        // the milliseconds from the query (which are unreliable in storing in
        // the DB)
        Date startDateRoundDown = DateUtils.roundDateDownToSeconds(startDate);
        Date startDateNextSecond = new Date(startDateRoundDown.getTime() + 1000);
        Date endDateRoundDown = DateUtils.roundDateDownToSeconds(endDate);
        Date endDateNextSecond = new Date(endDateRoundDown.getTime() + 1000);

        // Now build and perform the query
        return (Event) this.getSession().createCriteria(Event.class).add(
            Restrictions.eq("name", name)).add(
            Restrictions.lt("startDate", startDateNextSecond)).add(
            Restrictions.ge("startDate", startDateRoundDown)).add(
            Restrictions.lt("endDate", endDateNextSecond)).add(
            Restrictions.ge("endDate", endDateRoundDown)).uniqueResult();
    }

    public Collection findWithinDateRange(Date startDate, Date endDate)
        throws MetadataAccessException {
        return null;
    }

    /**
     * @see IMetadataDAO#makePersistent(IMetadataObject)
     */
    public Long makePersistent(IMetadataObject metadataObject)
        throws MetadataAccessException {

        logger.debug("makePersistent called");

        // The flag to indicate if this was previously persisted
        boolean persistedBefore = false;

        // Check incoming object
        Event event = this.checkIncomingMetadataObject(metadataObject);

        // Check the persistent store for the matching object
        Event persistentEvent = (Event) this.findEquivalentPersistentObject(
            event, false);

        // The handle to the event to really be persisted
        Event eventToPersist = null;

        // Since one already exists, copy over any non-null, changed fields and
        // assign to the handle
        if (persistentEvent != null) {
            this.updateDestinationObject(event, persistentEvent);

            // Set the flag
            persistedBefore = true;

            // Assign to the handle
            eventToPersist = persistentEvent;
        } else {
            // Since this will be a new Event, make sure alternate business key
            // is there. Actually for event the alternate business key is name,
            // start and end date, but name is the critical one and is the only
            // enforced key in the data store
            if ((event.getName() == null) || (event.getName().equals(""))) {
                try {
                    event.setName("Event_" + getUniqueNameSuffix());
                } catch (MetadataException e) {
                    logger.error("MetadataException caught trying to set "
                        + "an auto-generated event name: " + e.getMessage());
                }
                addMessage(
                    ssdsAdminEmailToAddress,
                    "An incoming event did not have a name, so SSDS auto-generated one:<br><ul><li>"
                        + event.toStringRepresentation("<li>") + "</ul><br>");
            }

            // Set the flag
            persistedBefore = false;

            // Attach to handle
            eventToPersist = event;
        }

        // If it was not persisted before, save it
        if (!persistedBefore)
            getSession().save(eventToPersist);

        // Return the ID
        if (eventToPersist != null) {
            return eventToPersist.getId();
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
        Event event = this.checkIncomingMetadataObject(metadataObject);

        // Check the persistent store for the matching object
        Event persistentEvent = (Event) this.findEquivalentPersistentObject(
            event, false);

        // If no matching event was found, do nothing
        if (persistentEvent == null) {
            logger
                .debug("No matching event could be found in the persistent store, "
                    + "no delete performed");
        } else {

            // Make sure the event is removed from any DataProducers
            DataProducerDAO dataProducerDAO = new DataProducerDAO(getSession());
            Collection dataProducersByEvent = dataProducerDAO.findByEvent(
                persistentEvent, null, null, false);
            if (dataProducersByEvent != null) {
                Iterator iterator = dataProducersByEvent.iterator();
                while (iterator.hasNext()) {
                    DataProducer dataProducer = (DataProducer) iterator.next();
                    dataProducer.removeEvent(persistentEvent);
                }
            }

            logger
                .debug("Existing object was found, so we will try to delete it");
            try {
                getSession().delete(persistentEvent);
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
     *            <code>Event</code>
     * @return a <code>Event</code> that is same object that came in
     * @throws MetadataAccessException
     *             if something is wrong
     */
    private Event checkIncomingMetadataObject(IMetadataObject metadataObject)
        throws MetadataAccessException {

        // Check for null argument
        if (metadataObject == null) {
            throw new MetadataAccessException("Failed: incoming Event was null");
        }

        // Try to cast the incoming object into the correct class
        Event event = null;
        try {
            event = (Event) metadataObject;
        } catch (ClassCastException cce) {
            throw new MetadataAccessException(
                "Could not cast the incoming object into a Event");
        }
        return event;
    }

    protected void initializeRelationships(IMetadataObject metadataObject)
        throws MetadataAccessException {

    }

    /**
     * The Log4J Logger
     */
    static Logger logger = Logger.getLogger(EventDAO.class);
}
