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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.NamingException;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.services.metadata.DataContainerAccessLocal;
import moos.ssds.services.metadata.DataContainerAccessLocalHome;
import moos.ssds.services.metadata.DataContainerAccessUtil;
import moos.ssds.services.metadata.DeviceAccessLocal;
import moos.ssds.services.metadata.DeviceAccessLocalHome;
import moos.ssds.services.metadata.DeviceAccessUtil;
import moos.ssds.services.metadata.RecordVariableAccessLocal;
import moos.ssds.services.metadata.RecordVariableAccessLocalHome;
import moos.ssds.services.metadata.RecordVariableAccessUtil;
import moos.ssds.util.XmlDateFormat;

import org.apache.log4j.Logger;

/**
 * This class provides access to the raw data in a DataStream that was collected
 * by SSDS.
 * 
 * @author kgomes
 * @ejb.bean name="RecordVariableDataAccess" type="Stateless"
 *           jndi-name="moos/ssds/services/data/RecordVariableDataAccess"
 *           local-jndi-name="moos/ssds/services/data/RecordVariableDataAccessLocal"
 *           view-type="both"
 * @ejb.home create="true"
 *           local-class="moos.ssds.services.data.RecordVariableDataAccessLocalHome"
 *           remote-class="moos.ssds.services.data.RecordVariableDataAccessHome"
 * @ejb.interface create="true"
 *                local-class="moos.ssds.services.data.RecordVariableDataAccessLocal"
 *                remote-class="moos.ssds.services.data.RecordVariableDataAccess"
 * @soap.service urn="RecordVariableDataAccess" scope="Request"
 * @axis.service urn="RecordVariableDataAccess" scope="Request"
 */
public class RecordVariableDataAccessEJB implements SessionBean {

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

    // The EJB Create callback
    public void ejbCreate() throws CreateException {}

    /**
     * @throws CreateException
     */
    public void ejbPostCreate() throws CreateException {}

    /**
     * @ejb.interface-method view-type="both"
     */
    public Object[][] getRecordVariableData(Long recordVariableId,
        Date startDate, Date endDate) {

        // Check parameters first
        if (recordVariableId == null) {
            return null;
        }

        // The data to return
        Object[][] data = null;

        // Since I already have an EJB that does this by device and variable
        // name, just use that. So I will need to find the record variable name
        // and device id
        RecordVariable incomingRecordVariable = new RecordVariable();
        incomingRecordVariable.setId(recordVariableId);
        RecordVariable persistentRecordVariable = null;
        Device deviceThatProduced = null;

        // Find the persistent RV first
        RecordVariableAccessLocalHome rvalh = null;
        RecordVariableAccessLocal rval = null;
        try {
            rvalh = RecordVariableAccessUtil.getLocalHome();
            rval = rvalh.create();
        } catch (NamingException e) {
            logger
                .error("NamingException caught trying to get RecordVariable access interfaces: "
                    + e.getMessage());
        } catch (CreateException e) {
            logger
                .error("NamingException caught trying to get RecordVariable access interfaces: "
                    + e.getMessage());
        }

        if (rval != null) {
            try {
                persistentRecordVariable = (RecordVariable) rval
                    .findEquivalentPersistentObject(incomingRecordVariable,
                        false);
            } catch (MetadataAccessException e) {
                logger
                    .error("MetadataAccessException caught trying to get persistent RecordVariable: "
                        + e.getMessage());
            }

            if (persistentRecordVariable != null) {

                // Find the RecordDescription packet type for the data query
                DataContainerAccessLocalHome dcalh = null;
                DataContainerAccessLocal dcal = null;
                try {
                    dcalh = DataContainerAccessUtil.getLocalHome();
                    dcal = dcalh.create();
                } catch (NamingException e1) {
                    logger
                        .error("NamingException caught trying to get DataContainer access interfaces: "
                            + e1.getMessage());
                } catch (CreateException e1) {
                    logger
                        .error("CreateException caught trying to get DataContainer access interfaces: "
                            + e1.getMessage());
                }

                if (dcal != null) {

                    DataContainer dataContainer = null;
                    try {
                        dataContainer = dcal.findByRecordVariable(
                            persistentRecordVariable, true);
                    } catch (MetadataAccessException e1) {
                        logger
                            .error("MetadataAccessException caught trying to get DataContainer by RecordVariable: "
                                + e1.getMessage());
                    }

                    if ((dataContainer != null)
                        && (dataContainer.getRecordDescription() != null)) {

                        // Grab the packet type
                        Long packetType = dataContainer.getRecordDescription()
                            .getRecordType();

                        // Now find the device
                        DeviceAccessLocalHome dalh = null;
                        DeviceAccessLocal dal = null;
                        try {
                            dalh = DeviceAccessUtil.getLocalHome();
                            dal = dalh.create();
                        } catch (NamingException e) {
                            logger
                                .error("NamingException caught trying to get Device access interfaces: "
                                    + e.getMessage());
                        } catch (CreateException e) {
                            logger
                                .error("CreateException caught trying to get Device access interfaces: "
                                    + e.getMessage());
                        }

                        if (dal != null) {
                            try {
                                deviceThatProduced = dal.findByRecordVariable(
                                    persistentRecordVariable, false);
                            } catch (MetadataAccessException e) {
                                logger
                                    .error("MetadataAccessException caught trying to get Device from recordVariable: "
                                        + e.getMessage());
                            }

                            if (deviceThatProduced != null) {
                                // Now query for the data and return
                                Collection recordVariables = new ArrayList();
                                recordVariables.add(persistentRecordVariable
                                    .getName());

                                DeviceDataAccessLocalHome ddalh = null;
                                DeviceDataAccessLocal ddal = null;
                                try {
                                    ddalh = DeviceDataAccessUtil.getLocalHome();
                                    ddal = ddalh.create();
                                } catch (NamingException e) {
                                    logger
                                        .error("NamingException caught trying to get DeviceDataAccess access interfaces: "
                                            + e.getMessage());
                                } catch (CreateException e) {
                                    logger
                                        .error("CreateException caught trying to get DeviceDataAccess access interfaces: "
                                            + e.getMessage());
                                }

                                // Grab the data (or the best approximation to
                                // it)
                                data = ddal.getDeviceData(deviceThatProduced,
                                    packetType, recordVariables, startDate,
                                    endDate);
                            }
                        }
                    }
                }
            }

        }
        return data;
    }

    /**
     * @ejb.interface-method view-type="both"
     * @soap.method
     * @axis.method
     */
    public Object[][] getRecordVariableData(Long recordVariableId,
        Long numberOfHoursBack) {

        // Check parameters
        if ((recordVariableId == null) || (numberOfHoursBack == null))
            return null;

        // Convert the number of hours back to two date (start/end)
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        startCalendar.add(Calendar.HOUR, (new Integer("-"
            + numberOfHoursBack.intValue())).intValue());

        // Now call other method
        return this.getRecordVariableData(recordVariableId, startCalendar
            .getTime(), endCalendar.getTime());
    }

    /**
     * @ejb.interface-method view-type="both"
     */
    public Object[][] getRecordVariableData(Collection recordVariables,
        Calendar startTime, Calendar endTime) {
        return null;
    }

    /**
     * @ejb.interface-method view-type="both"
     */
    public Object[][] getRecordVariableData(RecordVariable recordVariable,
        Date startDate, Date endDate) {
        return null;
    }

    /**
     * A Date formatter
     */
    XmlDateFormat xmlDateFormat = new XmlDateFormat();

    /**
     * This is a Log4JLogger that is used to log information to
     */
    static Logger logger = Logger.getLogger(RecordVariableDataAccessEJB.class);
}