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
package moos.ssds.services.servlet.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import moos.ssds.data.util.LocationAndTime;
import moos.ssds.metadata.Device;
import moos.ssds.services.data.DeviceDataAccessLocal;

/**
 */
public class GoogleMapsGPSDataServlet extends HttpServlet {

	@Resource(mappedName = "moos/ssds/services/data/DeviceDataAccessLocal")
	private DeviceDataAccessLocal ddal;

	/**
	 * This is the implementation of the method to return some information about
	 * what this particular servelt does
	 */
	public String getServletInfo() {
		return "This servlet returns data in XML format that "
				+ "can be used to plot polylines in Google Maps API";
	}

	/**
	 * This is the doPost method defined in the HTTPServlet. In this case, it
	 * simply calls the doGet method passing the request response pair
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Setup the response
		response.setContentType("text/xml");
		PrintWriter out = new PrintWriter(response.getOutputStream());

		// Grab the device ID and number of hours back from the request
		// parameters
		String deviceIDParameter = request.getParameter("deviceID");
		String numberOfHoursBackParameter = request
				.getParameter("numberOfHoursBack");

		// The locations and times to populate
		Collection locationAndTimes = null;

		// Create a deviceID
		Long deviceID = null;
		if ((deviceIDParameter != null) && (!deviceIDParameter.equals(""))) {
			try {
				deviceID = new Long(deviceIDParameter);
			} catch (NumberFormatException e) {
			}
		}
		if (deviceID != null) {

			// Now create a new device with that ID
			Device gpsDevice = new Device();
			gpsDevice.setId(deviceID);

			// Now grab the number of hours back
			Long numHoursBack = null;
			try {
				numHoursBack = new Long(numberOfHoursBackParameter);
			} catch (NumberFormatException e1) {
			}
			if (numHoursBack == null)
				numHoursBack = new Long(168);

			// Create a calendar to specify the start date
			Calendar startCalendar = GregorianCalendar.getInstance();
			startCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));

			// Set the time to now
			startCalendar.setTime(new Date());
			// Now take off the number of hours
			startCalendar.add(GregorianCalendar.HOUR,
					-1 * numHoursBack.intValue());

			// Now grab the Colleciton of LocationAndTimes
			if (ddal != null) {
				try {
					locationAndTimes = ddal.getGpsDeviceLocationAndTimes(
							gpsDevice, startCalendar.getTime(), new Date());
				} catch (Exception e) {
				}
			}
		}
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<gpspoints>");
		if (locationAndTimes != null) {
			Iterator locTimeIter = locationAndTimes.iterator();
			while (locTimeIter.hasNext()) {
				LocationAndTime locationAndTime = (LocationAndTime) locTimeIter
						.next();
				if ((locationAndTime != null)
						&& (locationAndTime.getLatitude() != null)
						&& (locationAndTime.getLongitude() != null)
						&& (locationAndTime.getEpochSeconds() != null)) {
					StringBuffer xmlReply = new StringBuffer();
					xmlReply.append("  <gpspoint ");
					xmlReply.append("lat=\"");
					xmlReply.append(locationAndTime.getLatitude().toString());
					xmlReply.append("\" lon=\"");
					xmlReply.append(locationAndTime.getLongitude());
					xmlReply.append("\" epochs=\"");
					xmlReply.append(locationAndTime.getEpochSeconds()
							.doubleValue());
					xmlReply.append("\"/>");
					out.println(xmlReply.toString());
				}
			}
		}
		out.println("</gpspoints>");
		out.flush();
	}
}
