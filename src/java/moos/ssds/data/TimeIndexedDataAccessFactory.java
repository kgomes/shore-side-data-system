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
package moos.ssds.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.data.util.DataException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.services.metadata.DataContainerAccess;
import moos.ssds.services.metadata.DataContainerAccessLocal;

import org.apache.log4j.Logger;

import dods.dap.DODSException;

/**
 * This class is designed to provide factory methods to give clients objects
 * that implement the <code>ITimeIndexedDataAccess</code> interface. There are
 * several factory methods that allow the user to find the right data access
 * object.
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.3 $
 * @stereotype factory
 */
public class TimeIndexedDataAccessFactory {

	/**
	 * This method sets up up a <code>ITimeIndexedDataAccess</code> that
	 * contains the data from the given <code>DataContainer</code> over the
	 * given time window. If a collection of <code>String</code>s are specified,
	 * it will limit the <code>RecordVariable</code>s available to those
	 * specified in the collection (otherwise it will use all).
	 * 
	 * @param dataContainer
	 * @param startDate
	 * @param endDate
	 * @param recordVariableNames
	 * @return an object that implements the <code>ITimeIndexedDataAccess</code>
	 *         interface. It will return null if no object could be constructed
	 *         to provide the necessary methods.
	 */
	public static ITimeIndexedDataAccess getTimeIndexedDataAccess(
			DataContainer dataContainer, Date startDate, Date endDate,
			Collection recordVariableNames) throws DataException {

		// The one to return
		ITimeIndexedDataAccess dataAccessToReturn = null;

		// Check what type of DataContainer
		if (dataContainer.getDataContainerType()
				.equals(DataContainer.TYPE_FILE)) {

			// Check to see if it is dods accessible
			if ((dataContainer.isDodsAccessible().booleanValue())
					|| (dataContainer.getUriString().endsWith(".nc"))) {
				try {
					dataAccessToReturn = new TimeIndexedNetcdfAccess(
							dataContainer, startDate, endDate);
				} catch (MalformedURLException e3) {
					logger.error("MalformedURLException caught : "
							+ e3.getMessage());
				} catch (IOException e3) {
					logger.error("IOException caught : " + e3.getMessage());
				} catch (DODSException e3) {
					logger.error("DODSException caught : " + e3.getMessage());
				}
			} else {
				// Not netcdf or DODS, so try to do it with Free form access
				dataAccessToReturn = new TimeIndexedFreeFormAccess(
						dataContainer, startDate, endDate, recordVariableNames);
			}
		} else if (dataContainer.getDataContainerType().equals(
				DataContainer.TYPE_STREAM)) {
			dataAccessToReturn = new TimeIndexedPacketAccess(dataContainer,
					startDate, endDate, recordVariableNames);
		}

		// Now return it
		return dataAccessToReturn;
	}

	/**
	 * This method returns a class that implements the
	 * <code>ITimeIndexedDataAccess</code> interface from a
	 * <code>DataContainer</code> that is found at the given URL.
	 * 
	 * @see #getTimeIndexedDataAccess(DataContainer, Date, Date, Collection)
	 * @param URL
	 *            is a <code>String</code> that is the URL that points to the
	 *            <code>DataContainer</code> that the data access is to be setup
	 *            for
	 * @param startDate
	 *            is the inclusive <code>Date</code> that specifies the start of
	 *            the data that will be available in the data access object
	 * @param endDate
	 *            is the inclusive <code>Date</code> that specifies the end of
	 *            the data that will be available in the data access object
	 * @return an object that implements the <code>ITimeIndexedDataAccess</code>
	 *         interface. It will return null if no object could be constructed
	 *         to provide the necessary methods.
	 */
	public static ITimeIndexedDataAccess getTimeIndexedDataAccess(String URL,
			Date startDate, Date endDate, Collection recordVariableNames) {

		// The one to return
		ITimeIndexedDataAccess dataAccessToReturn = null;

		// The DataContainer that matches the URL
		DataContainer dataContainer = null;

		// First try to find the DataContainer with that URL (try files first,
		// then streams)
		Context context = null;
		DataContainerAccessLocal dcAccessLocal = null;
		try {
			context = new InitialContext();
			dcAccessLocal = (DataContainerAccessLocal) context
					.lookup("moos/ssds/services/metadata/DeviceAccessLocal");
		} catch (NamingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// If the local interface was found, query for the DataContainer
		if (dcAccessLocal != null) {
			try {
				dataContainer = dcAccessLocal.findByURIString(URL, true);
			} catch (MetadataAccessException e) {
				logger.error("MetadataAccessException trying to find a "
						+ "DataContainer by URL: " + e.getMessage());
			}
		} else {
			logger.debug("No local interfaces were found, will try remote");
			// Something went wrong with looking up local services, must try
			// remote
			DataContainerAccess dataContainerAccess = null;
			try {
				dataContainerAccess = (DataContainerAccess) context
						.lookup("moos/ssds/services/metadata/DataContainerAccess");
			} catch (NamingException e) {
				logger.error("NamingException caught trying to get remote "
						+ "interfaces to DataContainerAccess:" + e.getMessage());
			}
			// If the interface was found, try to look up the matching
			// dataContainer
			if (dataContainerAccess != null) {
				// Find by URL
				try {
					dataContainer = dataContainerAccess.findByURIString(URL,
							true);
				} catch (MetadataAccessException e) {
					logger.error("MetadataAccessException caught trying to find DataContainer "
							+ "that matches the URL "
							+ URL
							+ ": "
							+ e.getMessage());
				}

			}
		}
		// If a matching data container was found
		if (dataContainer != null) {
			try {
				dataAccessToReturn = getTimeIndexedDataAccess(dataContainer,
						startDate, endDate, recordVariableNames);
			} catch (DataException e) {
				logger.error("DataException caught: " + e.getMessage());
			}
		}

		return dataAccessToReturn;
	}

	/** A Logger */
	static Logger logger = Logger.getLogger(TimeIndexedDataAccessFactory.class);
}