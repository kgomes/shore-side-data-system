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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.services.metadata.DataContainerAccessLocal;
import moos.ssds.services.metadata.DeviceAccessLocal;
import moos.ssds.services.metadata.RecordVariableAccessLocal;
import moos.ssds.util.XmlDateFormat;

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * This class provides access to the raw data in a DataStream that was collected
 * by SSDS.
 * 
 * @author kgomes
 */
@Stateless
@Local(RecordVariableDataAccessLocal.class)
@LocalBinding(jndiBinding = "moos/ssds/services/data/RecordVariableDataAccessLocal")
@Remote(RecordVariableDataAccess.class)
@RemoteBinding(jndiBinding = "moos/ssds/services/data/RecordVariableDataAccess")
public class RecordVariableDataAccessEJB implements RecordVariableDataAccess,
		RecordVariableDataAccessLocal {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Resource(mappedName = "moos/ssds/services/metadata/RecordVariableAccessLocal")
	private RecordVariableAccessLocal rval;

	@Resource(mappedName = "moos/ssds/services/metadata/DataContainerAccessLocal")
	private DataContainerAccessLocal dcal;

	@Resource(mappedName = "moos/ssds/services/metadata/DeviceAccessLocal")
	private DeviceAccessLocal dal;

	@Resource(mappedName = "moos/ssds/services/data/DeviceDataAccessLocal")
	private DeviceDataAccessLocal ddal;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.data.RecordVariableDataAccess#getRecordVariableData
	 * (java.lang.Long, java.util.Date, java.util.Date)
	 */
	@Override
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
		if (rval != null) {
			try {
				persistentRecordVariable = (RecordVariable) rval
						.findEquivalentPersistentObject(incomingRecordVariable,
								false);
			} catch (MetadataAccessException e) {
				logger.error("MetadataAccessException caught trying to get persistent RecordVariable: "
						+ e.getMessage());
			}

			if (persistentRecordVariable != null) {

				// Find the RecordDescription packet type for the data query
				if (dcal != null) {

					DataContainer dataContainer = null;
					try {
						dataContainer = dcal.findByRecordVariable(
								persistentRecordVariable, true);
					} catch (MetadataAccessException e1) {
						logger.error("MetadataAccessException caught trying to get DataContainer by RecordVariable: "
								+ e1.getMessage());
					}

					if ((dataContainer != null)
							&& (dataContainer.getRecordDescription() != null)) {

						// Grab the packet type
						Long packetType = dataContainer.getRecordDescription()
								.getRecordType();

						// Now find the device
						if (dal != null) {
							try {
								deviceThatProduced = dal.findByRecordVariable(
										persistentRecordVariable, false);
							} catch (MetadataAccessException e) {
								logger.error("MetadataAccessException caught trying to get Device from recordVariable: "
										+ e.getMessage());
							}

							if (deviceThatProduced != null) {
								// Now query for the data and return
								Collection recordVariables = new ArrayList();
								recordVariables.add(persistentRecordVariable
										.getName());

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.data.RecordVariableDataAccess#getRecordVariableData
	 * (java.lang.Long, java.lang.Long)
	 */
	@Override
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
		startCalendar.add(Calendar.HOUR,
				(new Integer("-" + numberOfHoursBack.intValue())).intValue());

		// Now call other method
		return this.getRecordVariableData(recordVariableId,
				startCalendar.getTime(), endCalendar.getTime());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.data.RecordVariableDataAccess#getRecordVariableData
	 * (java.util.Collection, java.util.Calendar, java.util.Calendar)
	 */
	@Override
	public Object[][] getRecordVariableData(Collection recordVariables,
			Calendar startTime, Calendar endTime) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.data.RecordVariableDataAccess#getRecordVariableData
	 * (moos.ssds.metadata.RecordVariable, java.util.Date, java.util.Date)
	 */
	@Override
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