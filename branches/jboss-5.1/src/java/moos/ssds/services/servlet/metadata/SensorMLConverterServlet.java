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
package moos.ssds.services.servlet.metadata;

import java.io.IOException;
import java.io.PrintWriter;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.apache.log4j.Logger;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataFactory;
import moos.ssds.services.metadata.DataProducerAccessLocal;
import moos.ssds.services.metadata.DataProducerAccessLocalHome;
import moos.ssds.services.metadata.DataProducerAccessUtil;
import moos.ssds.wrapper.ogc.sensorml.SensorMLFactory;

/**
 * @author kgomes
 * @version 1.0
 * @web.servlet name="SensorMLConverterServlet" display-name="Servlet to Convert
 *              a given DataProducer to SensorML"
 * @web.servlet-mapping url-pattern="/SensorMLConverterServlet/*"
 * @web.servlet-mapping url-pattern="*.SensorMLConverterServlet"
 * @web.servlet-mapping url-pattern="/SensorMLConverterServlet"
 */
public class SensorMLConverterServlet extends HttpServlet {

	/**
	 * Override the init method to do the one time setup things
	 */
	public void init(ServletConfig servletConfig) throws ServletException {
		// Call the init on HttpServlet
		super.init(servletConfig);
	}

	/**
	 * This is the doPost method defined in the HTTPServlet. In this case, it
	 * simply calls the doGet method passing the request response pair
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * This is the doGet method where the real stuff happens
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Set the response to text XML
		response.setContentType("text/xml");
		PrintWriter out = response.getWriter();

		// Grab the DataProducer from the parameters
		String dataProducerStringRepresentation = request
				.getParameter("DataProducer");
		logger.debug("dataProducerStringRepresentation = "
				+ dataProducerStringRepresentation);

		// Grab the delimiter
		String delimiter = request.getParameter("delimiter");
		logger.debug("delimiter before check = " + delimiter);
		if (delimiter == null || delimiter.equals("")) {
			delimiter = "|";
		}
		logger.debug("delimiter after check = " + delimiter);

		// Grab a data record if there is one
		String dataRecord = request.getParameter("dataRecord");

		// Check it to make sure it exists
		if ((dataProducerStringRepresentation != null)
				&& (!dataProducerStringRepresentation.equals(""))) {
			// Since we have a string, convert it to a DataProducer
			DataProducer dp = null;
			logger
					.debug("Going to try and create DataProducer from string representation");
			try {
				dp = (DataProducer) MetadataFactory
						.createMetadataObjectFromStringRepresentation(
								dataProducerStringRepresentation, delimiter);
			} catch (MetadataException e) {
			}
			logger.debug("dp after create is " + dp);

			// If the DataProducer is not null, find a matching one in SSDS
			if (dp != null) {
				DataProducerAccessLocalHome dpalh = null;
				try {
					dpalh = DataProducerAccessUtil.getLocalHome();
				} catch (NamingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (dpalh != null) {
					DataProducerAccessLocal dpal = null;
					try {
						dpal = dpalh.create();
					} catch (CreateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (dpal != null) {
						DataProducer persistentDataProducer = null;
						try {
							persistentDataProducer = (DataProducer) dpal
									.findEquivalentPersistentObject(dp, true);
						} catch (MetadataAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (persistentDataProducer != null) {
							// If there was a data record
							byte[] dataRecordBytes = null;
							if (dataRecord != null && !dataRecord.equals(""))
								dataRecordBytes = dataRecord.getBytes();
							// Convert it
							Object sensorMLObject = SensorMLFactory
									.createSensorMLFromMetadataObject(
											persistentDataProducer,
											dataRecordBytes);

							try {
								JAXBContext jaxbContext = JAXBContext
										.newInstance("net.opengis.sensorml.v_1_0_1");
								Marshaller omMarshaller = jaxbContext
										.createMarshaller();
								omMarshaller.setProperty(
										Marshaller.JAXB_FORMATTED_OUTPUT,
										Boolean.TRUE);
								omMarshaller.marshal(sensorMLObject, out);
							} catch (PropertyException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (JAXBException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}
				}
			}
		} else {
			out.println("<null/>");
			out.flush();
		}
		out.close();
	}

	static Logger logger = Logger.getLogger(SensorMLConverterServlet.class);
}
