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
package moos.ssds.data.util;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class represents a geospatial location and time.
 * Created on May 14, 2004
 * 
 * @author achase
 */
public class LocationAndTime implements Serializable {

    /**
     * The constructor for the geospatial point in time
     * 
     * @param latitude
     * @param longitude
     * @param depth
     * @param epochSeconds
     */
    public LocationAndTime(Double latitude, Double longitude, Double depth,
        Long epochSeconds) {
        // Assign the private variables
        this.latitude = latitude;
        this.longitude = longitude;
        this.depth = depth;
        this.epochSeconds = epochSeconds;
    }

    /**
     * @return Returns the latitude. (Null if not set).
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * @return Returns the longitude. (Null if not set).
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * @return Returns the depth. (Null if not set).
     */
    public Double getDepth() {
        return depth;
    }

    /**
     * @return Returns the epochSeconds. (Null if not set).
     */
    public Long getEpochSeconds() {
        return epochSeconds;
    }

    /**
     * @return Returns a Date object in GMT timezone
     */
    public Date getGmtDate() {
        // Check for null value first
        if (this.epochSeconds == null)
            return null;

        // Create a GMT Date
        Calendar tempCalendar = Calendar.getInstance();
        tempCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        tempCalendar.setTimeInMillis(this.epochSeconds.longValue());
        return tempCalendar.getTime();
    }

    /**
     * Override the toString method
     */
    public String toString() {
        StringBuffer stringRepBuffer = new StringBuffer();
        // Attach latitude
        stringRepBuffer.append("latitude=");
        if (this.latitude != null)
            stringRepBuffer.append(latitude.doubleValue());

        // Attach longitude
        stringRepBuffer.append(", longitude=");
        if (this.longitude != null)
            stringRepBuffer.append(longitude.doubleValue());

        // Attach depth
        stringRepBuffer.append(", depth=");
        if (this.depth != null)
            stringRepBuffer.append(depth.doubleValue());

        // Attach the time
        stringRepBuffer.append(", epochseconds=");
        if (this.epochSeconds != null)
            stringRepBuffer.append(this.epochSeconds.longValue() + "("
                + new Date(this.epochSeconds.longValue()) + ")");

        // Now return the string
        return stringRepBuffer.toString();
    }

    // These are the geospatial coorindates
    private final Double latitude;
    private final Double longitude;
    private final Double depth;

    // This is the time in epochseconds
    private final Long epochSeconds;
}
