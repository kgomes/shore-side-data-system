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
package moos.ssds.metadata;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Date;

/**
 * This classRepresents an interval of time. This class provides the
 * functionality to easily query date ranges for objects implementing the
 * <code>IDateRange</code> interface. It can operate on native start and end
 * dates, or if it is acting as a proxy (by using setDateRangeObject or
 * constructor that takes in an <code>IDateRange</code>) to another
 * <code>IDateRange</code> object, it will use the other object's dates. Typical
 * use is: </p>
 * 
 * <pre>
 * IDateRange idr; // This can be a DataContainer, DataProducer, Event, Resource, etc.
 * DateRange dr = idr.getDateRange();
 * // Setting the start and end dates of a DateRange WILL set the dates of it's
 * // IDateRange proxy
 * dr.setStartDate(someStartDate);
 * dr.setEndDate(someEndDate);
 * Date someDate = new Date();
 * 
 * // Does dr occur before someDate?
 * boolean before = dr.before(someDate);
 * 
 * // Does dr occur after someDate?
 * boolean after = dr.after(someDate);
 * 
 * // Does someDate occur during dr?
 * boolean during = dr.during(someDate);
 * 
 * // How long is dr?
 * long durationMs = dr.duration();
 * </pre>
 * 
 * Remember that if it is to act as a proxy for another object, you must set the
 * object using setDateRangeObject
 * 
 * @stereotype mi-detail
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.1 $
 */
public class DateRange implements Serializable, IDateRange, Externalizable {

	/**
	 * This is the version that we can control for serialization purposes
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the native start <code>Date</code> that will be used if this
	 * object is not acting as a proxy for another <code>IDateRange</code>
	 * object
	 */
	private Date startDate = null;

	/**
	 * This is the native start <code>Date</code> that will be used if this
	 * object is not acting as a proxy for another <code>IDateRange</code>
	 * object
	 */
	private Date endDate;

	/**
	 * This is a flag to indicate if the object is acting on behalf (as a proxy)
	 * for another <code>IDateRange</code> object
	 */
	private boolean proxy = false;

	/**
	 * This is the object that will be used if the proxy flag is set to true.
	 */
	private IDateRange dateRange = null;

	/**
	 * Some constants that apply to the time queries
	 */
	public final static int BEFORE = -1;
	public final static int AFTER = 1;
	public final static int DURING = 0;

	/**
	 * This is the default constructor that simply sets the flag to indicate it
	 * is a proxy acting on behalf of another <code>IDateRange</code> object
	 */
	public DateRange() {
		this.proxy = false;
	}

	/**
	 * This constructor takes in an object that support the
	 * <code>IDateRange</code> and sets this object to be acting as a proxy for
	 * that object
	 * 
	 * @param iDateRange
	 *            The object that this will be a proxy for.
	 */
	public DateRange(IDateRange dateRange) {
		this.setDateRange(dateRange);
	}

	/**
	 * @see IDateRange#getStartDate()
	 */
	public Date getStartDate() {
		if (proxy) {
			return dateRange.getStartDate();
		} else {
			return this.startDate;
		}
	}

	/**
	 * @see IDateRange#setStartDate(Date)
	 */
	public void setStartDate(Date startDate) {
		if (proxy) {
			dateRange.setStartDate(startDate);
		} else {
			this.startDate = startDate;
		}
	}

	/**
	 * @see IDateRange#getEndDate()
	 */
	public Date getEndDate() {
		if (proxy) {
			return this.dateRange.getEndDate();
		} else {
			return this.endDate;
		}
	}

	/**
	 * @see IDateRange#setEndDate(Date)
	 */
	public void setEndDate(Date endDate) {
		if (proxy) {
			this.dateRange.setEndDate(endDate);
		} else {
			this.endDate = endDate;
		}
	}

	/**
	 * @see IDateRange#getDateRange()
	 */
	public IDateRange getDateRange() {
		if (proxy) {
			return this.dateRange;
		} else {
			return this;
		}
	}

	/**
	 * This method sets the <code>IDateRange</code> that the object will act on
	 * behalf of. If it is null, it will simply use it's own internal dates.
	 * 
	 * @param dateRange
	 *            the <code>IDateRange</code> to act as a proxy for. If it is
	 *            null, it will act on its own internal dates.
	 */
	public void setDateRange(IDateRange dateRange) {
		// Assign the object
		this.dateRange = dateRange;

		// Set the proxy flag correctly
		if (this.dateRange != null) {
			proxy = true;
		} else {
			proxy = false;
		}
	}

	/**
	 * This method returns a <code>boolean</code> that indicates if the end of
	 * the <code>IDateRange</code> is before the <code>Date</code> given
	 * 
	 * @param date
	 *            The date to compare to the time range
	 * @return <code>true</code> if the end of the <code>IDateRange</code> is
	 *         before the given <code>Date</code>
	 */
	public boolean before(Date date) {
		return (date.after(this.getEndDate())) ? true : false;
	}

	/**
	 * This method returns a <code>boolean</code> that indicates if the start of
	 * the <code>IDateRange</code> is after the <code>Date</code> given
	 * 
	 * @return <code>true</code> if the startDate of the <code>IDateRange</code>
	 *         occurs after the given <code>Date</code>.
	 * @param date
	 *            The date to compare
	 */
	public boolean after(Date date) {
		return (date.before(this.getStartDate())) ? true : false;
	}

	/**
	 * This method returns a <code>boolean</code> that if the
	 * <code>IDateRange</code> starts before the given <code>Date</code>, but
	 * ends after it.
	 * 
	 * @param date
	 *            The date to compare against the time range
	 * @return <code>true</code> if the supplied date occurs during the
	 *         <code>IDateRange</code>.
	 */
	public boolean during(Date date) {
		boolean ok = false;
		if ((date.after(this.getStartDate()))
				&& (date.before(this.getEndDate()))) {
			ok = true;
		}
		return ok;
	}

	/**
	 * This method checks to see if the <code>IDateRange</code>'s time interval
	 * is BEFORE, AFTER, or over (DURING) the given <code>Date</code>
	 * 
	 * @param date
	 *            is the <code>Date</code> to check against the date range of
	 *            the object
	 * @return -1 if the DateRange is before, 0 during or 1 after the given
	 *         <code>Date</code>
	 */
	public int relation(Date date) {
		int r = DURING;
		if (before(date)) {
			r = BEFORE;
		} else if (after(date)) {
			r = AFTER;
		}
		return r;
	}

	/**
	 * This method returns the number of milliseconds that is covered by the
	 * <code>IDateRange</code>
	 * 
	 * @return a <code>long</code> that is the number of milliseconds covered by
	 *         the <code>IDateRange</code>. If one of the start or end dates (or
	 *         both) are missing, a 0 will be returned.
	 */
	public long duration() {
		if ((this.getEndDate() != null) && (this.getStartDate() != null)) {
			return this.getEndDate().getTime() - this.getStartDate().getTime();
		} else {
			return 0;
		}
	}

	/**
	 * This method returns the number of seconds that is covered by the
	 * <code>IDateRange</code>
	 * 
	 * @return a <code>long</code> that is the number of seconds covered by the
	 *         <code>IDateRange</code>. If one of the start or end dates (or
	 *         both) are missing, a 0 will be returned.
	 */
	public long durationSeconds() {
		if (duration() != 0) {
			return (long) duration() / 1000;
		} else {
			return 0;
		}
	}

	/**
	 * This method returns the number of hours (and decimal fraction) that is
	 * covered by the <code>IDateRange</code>
	 * 
	 * @return a <code>float</code> that is the decimal number of hours covered
	 *         by the <code>IDateRange</code>. If one of the start or end dates
	 *         (or both) are missing, a 0 will be returned.
	 */
	public float durationHours() {
		// show only 3 decimal places
		if (duration() != 0) {
			int hoursTimes1000 = (int) (((float) duration()) / 60 / 60);
			return ((float) hoursTimes1000) / 1000;
		} else {
			return 0;
		}
	}

	/**
	 * This method returns the number of days (and decimal fraction) that is
	 * covered by the <code>IDateRange</code>
	 * 
	 * @return a <code>float</code> that is the decimal number of days covered
	 *         by the <code>IDateRange</code>. If one of the start or end dates
	 *         (or both) are missing, a 0 will be returned.
	 */
	public float durationDays() {
		// show only 3 decimal places
		if (duration() != 0) {
			int daysTimes1000 = (int) (((float) duration()) / 60 / 60 / 24);
			return ((float) daysTimes1000) / 1000;
		} else {
			return 0;
		}
	}

	/**
	 * This is the method to re-constitute an object from a customized format
	 * 
	 * @see Externalizable#readExternal(ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		dateRange = (DateRange) in.readObject();
		endDate = (Date) in.readObject();
		proxy = (Boolean) in.readObject();
		startDate = (Date) in.readObject();
	}

	/**
	 * This is the method to do a custom serialization of a DataProducerGroup
	 * 
	 * @see Externalizable#writeExternal(ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(dateRange);
		out.writeObject(endDate);
		out.writeObject(proxy);
		out.writeObject(startDate);
	}
}
