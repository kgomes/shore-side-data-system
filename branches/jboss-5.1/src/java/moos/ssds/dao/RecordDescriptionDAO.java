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

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.RecordDescription;
import moos.ssds.metadata.RecordVariable;

import org.hibernate.Hibernate;
import org.hibernate.Session;

public class RecordDescriptionDAO extends MetadataDAO {

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
    public RecordDescriptionDAO(Session session) throws MetadataAccessException {
        super(RecordVariable.class, session);
    }

    /**
     * @see IMetadataDAO#findEquivalentPersistentObject(IMetadataObject,
     *      boolean)
     */
    public IMetadataObject findEquivalentPersistentObject(
        IMetadataObject metadataObject, boolean returnFullObjectGraph)
        throws MetadataAccessException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see IMetadataDAO#findAllIDs()
     */
    public Collection findAllIDs() throws MetadataAccessException {
        return null;
    }

    /**
     * @see IMetadataDAO#countFindAllIDs()
     */
    public int countFindAllIDs() throws MetadataAccessException {
        // The count
        int count = 0;
        // Return the result
        return count;
    }

    /**
     * @see IMetadataDAO#makePersistent(IMetadataObject)
     */
    public Long makePersistent(IMetadataObject metadataObject)
        throws MetadataAccessException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see IMetadataDAO#makeTransient(IMetadataObject)
     */
    public void makeTransient(IMetadataObject metadataObject)
        throws MetadataAccessException {
    // TODO Auto-generated method stub

    }

    /**
     * @see MetadataDAO#initializeRelationships(IMetadataObject)
     */
//    protected void initializeRelationships(IMetadataObject metadataObject)
//        throws MetadataAccessException {
//        if (metadataObject == null)
//            return;
//        RecordDescription rd = this.checkIncomingMetadataObject(metadataObject);
//
//        if (rd.getRecordVariables() != null) {
//            Hibernate.initialize(rd.getRecordVariables());
//            rd.getRecordVariables().size();
//        }
//    }

    /**
     * This method checks to make sure an incoming <code>MetadataObject</code>
     * is not null and is in fact of the correct class. It then converts it to
     * the correct class and returns it
     * 
     * @param metadataObject
     *            the <code>MetadataObject</code> to check and return as a
     *            <code>RecordDescription</code>
     * @return a <code>RecordDescription</code> that is same object that came
     *         in
     * @throws MetadataAccessException
     *             if something is wrong
     */
    private RecordDescription checkIncomingMetadataObject(
        IMetadataObject metadataObject) throws MetadataAccessException {

        // Check for null argument
        if (metadataObject == null) {
            throw new MetadataAccessException(
                "Failed: incoming RecordDescription was null");
        }

        // Try to cast the incoming object into the correct class
        RecordDescription recordDescription = null;
        try {
            recordDescription = (RecordDescription) metadataObject;
        } catch (ClassCastException cce) {
            throw new MetadataAccessException(
                "Could not cast the incoming object into a RecordDescription");
        }
        return recordDescription;
    }

}
