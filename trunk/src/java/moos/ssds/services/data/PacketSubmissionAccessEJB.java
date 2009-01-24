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
package moos.ssds.services.data;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TreeMap;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import moos.ssds.io.PacketOutputManager;
import moos.ssds.io.PacketSQLInput;
import moos.ssds.io.PacketSQLOutput;
import moos.ssds.services.data.util.DataException;
import moos.ssds.transmogrify.SSDSDevicePacket;

import org.apache.log4j.Logger;

/**
 * This EJB is a <code>SessionBean</code> that gives clients the capability of
 * submitting packets that match the SSDS byte array format. These packets will
 * end up in the SQL database storage for the SSDS system.
 * 
 * @author kgomes
 * @ejb.bean name="PacketSubmissionAccess" type="Stateless"
 *           jndi-name="moos/ssds/services/data/PacketSubmissionAccess"
 *           local-jndi-name="moos/ssds/services/data/PacketSubmissionLocal"
 *           view-type="both"
 * @ejb.home create="true"
 *           local-class="moos.ssds.services.data.PacketSubmissionAccessLocalHome"
 *           remote-class="moos.ssds.services.data.PacketSubmissionAccessHome"
 * @ejb.interface create="true"
 *                local-class="moos.ssds.services.data.PacketSubmissionAccessLocal"
 *                remote-class="moos.ssds.services.data.PacketSubmissionAccess"
 */
public class PacketSubmissionAccessEJB implements SessionBean {

    /**
     * @see javax.ejb.SessionBean#ejbActivate()
     */
    public void ejbActivate() throws EJBException, RemoteException {}

    /**
     * @see javax.ejb.SessionBean#ejbPassivate()
     */
    public void ejbPassivate() throws EJBException, RemoteException {}

    /**
     * @see javax.ejb.SessionBean#ejbRemove()
     */
    public void ejbRemove() throws EJBException, RemoteException {}

    /**
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(SessionContext arg0) throws EJBException,
        RemoteException {}

    /**
     * The EJB callback that is used when the bean is created
     */
    public void ejbCreate() throws CreateException {
        logger.debug("Creating the PacketSubmissionEJB");

        // Get the PacketOutputManager singleton
        if (this.packetOutputManager == null) {
            this.packetOutputManager = PacketOutputManager.getInstance();
        }

        logger.debug("OK, the instance should be created");
    }

    /**
     * @throws CreateException
     */
    public void ejbPostCreate() throws CreateException {}

    /**
     * This is the method to submit a packet to the SQL storage mechanism.
     * 
     * @ejb.interface-method view-type="both"
     * @param deviceID
     *            This is the source of the packet and should be the SSDS ID of
     *            the device
     * @param packetBytes
     *            this is the byte array that contains the data in the format
     *            specified in the SSDS packet documentation
     */
    public void submitPacketAsByteArray(long deviceID, byte[] packetBytes)
        throws DataException {
        // Grab the appropriate PacketSQLOutput
        PacketSQLOutput packetSQLOutput = PacketOutputManager
            .getPacketSQLOutput(deviceID);

        // Now write the packet
        try {
            packetSQLOutput.writeBytes(packetBytes);
        } catch (SQLException e) {
            logger
                .error("SQLException was caught trying to write bytes to SQL DataSource: "
                    + e.getMessage());
            throw new DataException(
                "An SQLException was caught trying to insert the given bytes array: "
                    + e.getMessage());
        } catch (Exception e) {
            logger.error("A " + e.getClass().getName()
                + " was caught trying to write bytes to SQL DataSource: "
                + e.getMessage());
            throw new DataException("A " + e.getClass().getName()
                + " was caught trying to insert the given bytes array: "
                + e.getMessage());
        }
    }

    PacketOutputManager packetOutputManager = null;

    /** A log4j logger */
    static Logger logger = Logger.getLogger(PacketSubmissionAccessEJB.class);

}