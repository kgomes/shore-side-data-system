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

import java.io.Serializable;
import java.util.Date;

/**
 * This interface means that those objects that implement it have the concept of
 * a range of time. If some object has a temporal start and end, it can
 * implement this interface.
 * <hr>
 * 
 * @stereotype interface
 * @author : $Author: kgomes $
 * @see DateRange
 * @version : $Revision: 1.1.2.1 $
 */

public interface IDateRange extends Serializable {

    /**
     * This method returns the <code>Date</code> that is associated with the
     * start of the time interval that this object applies to
     * 
     * @return a <code>Date</code> that is the start date of the date range
     */
    Date getStartDate();

    /**
     * This method sets the <code>Date</code> that is associated with the
     * start of the time interval that this object applies to
     * 
     * @param startDate
     *            is the <code>Date</code> that will be set as the start date
     *            of the date range
     */
    void setStartDate(Date startDate);

    /**
     * This method returns the <code>Date</code> that is associated with the
     * end of the time interval that this object applies to
     * 
     * @return a <code>Date</code> that is the end date of the date range
     */
    Date getEndDate();

    /**
     * This method sets the <code>Date</code> that is associated with the end
     * of the time interval that this object applies to
     * 
     * @param startDate
     *            is the <code>Date</code> that will be set as the end date of
     *            the date range
     */
    void setEndDate(Date endDate);

    /**
     * This method returns an object that supports the <code>IDateRange</code>
     * interface
     * 
     * @return an <code>IDateRange</code> object
     */
    IDateRange getDateRange();
}
