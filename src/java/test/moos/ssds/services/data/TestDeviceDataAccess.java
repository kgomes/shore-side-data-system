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
package test.moos.ssds.services.data;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import moos.ssds.data.util.DataException;
import moos.ssds.metadata.Device;
import moos.ssds.services.data.DeviceDataAccess;
import moos.ssds.services.data.DeviceDataAccessHome;
import moos.ssds.services.data.DeviceDataAccessLocalHome;
import moos.ssds.services.data.DeviceDataAccessUtil;

import org.apache.log4j.Logger;

import test.moos.ssds.services.metadata.TestAccessCase;

/**
 * This class tests the DeviceDataAccess service EJB to make sure all is well.
 * There has to be an SSDS server running somewhere for this to hit against and
 * a jndi.properties in the classpath so the tests can get to the server
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.4 $
 */
public class TestDeviceDataAccess extends TestAccessCase {

    /**
     * A constructor
     * 
     * @param name
     */
    public TestDeviceDataAccess(String name) {
        super(name);
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {

    }

    /**
     * Run suite of tests on device one
     */
    public void testOne() {
        // Create a device
        Device device = new Device();
        device.setId(new Long("1313"));

        // Create a start time
        Date endDate = new Date();
        // Create an end time
        Date startDate = new Date();
        startDate.setTime(endDate.getTime() - 10000000);

        // Create collection of recordVariableNames
        Collection rvNames = new ArrayList();
        rvNames.add("Latitude String");
        rvNames.add("Longitude String");

        // Now try to read in a data object array
        Object[][] data = null;
        DeviceDataAccessHome ddah = null;
        try {
            ddah = DeviceDataAccessUtil.getHome();
        } catch (NamingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DeviceDataAccess dda = null;
        try {
            if (ddah != null)
                dda = ddah.create();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CreateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            if (dda != null)
                data = dda.getDeviceData(device, new Long(1), rvNames,
                    startDate, endDate);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testGpsDataAccess() {
        Device device = new Device();
        device.setId(new Long("1313"));

        // Create a start time
        Date endDate = new Date();
        // Create an end time
        Date startDate = new Date();
        startDate.setTime(endDate.getTime() - 10000000);

        // Now try to read in a data object array
        Collection data = null;
        DeviceDataAccessHome ddah = null;
        try {
            ddah = DeviceDataAccessUtil.getHome();
        } catch (NamingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DeviceDataAccess dda = null;
        try {
            if (ddah != null)
                dda = ddah.create();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CreateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            if (dda != null)
                data = dda.getGpsDeviceLocationAndTimes(device, startDate,
                    endDate);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        logger.debug("Found " + data.size() + " pakcets");
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {}

    /**
     * A log4J logger
     */
    static Logger logger = Logger.getLogger(TestDeviceDataAccess.class);
}