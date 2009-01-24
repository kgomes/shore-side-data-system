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

import java.util.Date;

/**
 * This interface defines the methods that a class must support for access to
 * time indexed data.
 */
public interface ITimeIndexedDataAccess extends IDataAccess {

    /**
     * This method returns all the time <code>Object</code>s that are
     * associated with the data from the getData methods
     * 
     * @return The time index for the data that can be returned from the object
     */
    public abstract Object[] getTime();

    /**
     * This returns the <code>Date</code> of the earliest data object.
     * 
     * @return a <code>Data</code> that signifies the earliest data object.
     */
    public abstract Date getStartDate();

    /**
     * This returns the <code>Date</code> of the latest data object.
     * 
     * @return a <code>Data</code> that signifies the latest data object.
     */
    public abstract Date getEndDate();
}
