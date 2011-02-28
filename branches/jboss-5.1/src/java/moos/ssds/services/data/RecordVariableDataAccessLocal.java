package moos.ssds.services.data;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.ejb.Local;

import moos.ssds.metadata.RecordVariable;

@Local
public interface RecordVariableDataAccessLocal {

	/**
	 */
	public abstract Object[][] getRecordVariableData(Long recordVariableId,
			Date startDate, Date endDate);

	/**
	 * @ejb.interface-method view-type="both"
	 * @soap.method
	 * @axis.method
	 */
	public abstract Object[][] getRecordVariableData(Long recordVariableId,
			Long numberOfHoursBack);

	/**
	 * @ejb.interface-method view-type="both"
	 */
	public abstract Object[][] getRecordVariableData(
			Collection recordVariables, Calendar startTime, Calendar endTime);

	/**
	 * @ejb.interface-method view-type="both"
	 */
	public abstract Object[][] getRecordVariableData(
			RecordVariable recordVariable, Date startDate, Date endDate);

}