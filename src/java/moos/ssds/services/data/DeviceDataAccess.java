package moos.ssds.services.data;

import java.util.Collection;
import java.util.Date;

import javax.ejb.Remote;

import moos.ssds.data.util.DataException;
import moos.ssds.metadata.Device;

@Remote
public interface DeviceDataAccess {

	/**
	 * This method takes in a <code>Device</code>, a collection of
	 * <code>String</code>s that are the variable names, the start and end dates
	 * to span
	 * 
	 * @ejb.interface-method view-type="both"
	 * @param recordVariableID
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public abstract Object[][] getDeviceData(Device device, Long packetSubType,
			Collection recordVariables, Date startDate, Date endDate);

	/**
	 * @ejb.interface-method view-type="both"
	 * @param gpsDevice
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public abstract Collection getGpsDeviceLocationAndTimes(Device gpsDevice,
			Date startDate, Date endDate) throws DataException;

	/**
	 * This method takes in a <code>Device</code> and a time window and returns
	 * a collection of <code>LocationAndTime</code> objects that will be sorted
	 * be ascending time.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @param device
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public abstract Collection getDeviceLocationAndTimes(Device device,
			Date startDate, Date endDate);

}