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
package moos.ssds.data.graphing;

import org.jfree.data.xy.AbstractXYZDataset;

import moos.ssds.data.util.LocationAndTime;

/**
 * Created on Jun 2, 2004
 * 
 * @author achase
 */
public class LocationAndTimeDataset extends AbstractXYZDataset {

    /**
     * The constructor that takes in an array of LocationAndTimes
     * 
     * @param locationAndTime
     */
    public LocationAndTimeDataset(LocationAndTime[] locationAndTime) {

        // Call an super construction
        super();

        // Now set the data
        this.locationAndTime = locationAndTime;
    }

    /**
     * This method returns the X value (longitude) at a certain index
     */
    public double getXValue(int series, int item) {
        if (series != 0) {
            throw new IllegalArgumentException("Only one series available");
        }
        return locationAndTime[item].getLongitude().doubleValue();
    }

    /**
     * This method returns the X value (longitude) in <code>Number</code>
     * format
     */
    public Number getX(int series, int item) {
        if (series != 0) {
            throw new IllegalArgumentException("Only one series available");
        }
        return locationAndTime[item].getLongitude();
    }

    /**
     * This method returns the Y value (latitude) at a certain index
     */
    public double getYValue(int series, int item) {
        if (series != 0) {
            throw new IllegalArgumentException("Only one series available");
        }
        return locationAndTime[item].getLatitude().doubleValue();
    }

    /**
     * This method returns the Y values (longitude) in <code>Number</code>
     * format
     */
    public Number getY(int series, int item) {
        if (series != 0) {
            throw new IllegalArgumentException("Only one series available");
        }
        return locationAndTime[item].getLatitude();
    }

    /**
     * This method return the Z value (epoch seconds) at a certain index
     */
    public double getZValue(int series, int item) {
        if (series != 0) {
            throw new IllegalArgumentException("Only one series available");
        }
        return locationAndTime[item].getEpochSeconds().doubleValue();
    }

    /**
     * This method return the Z value (epoch seconds) in <code>Number</code>
     * format
     */
    public Number getZ(int series, int item) {
        if (series != 0) {
            throw new IllegalArgumentException("Only one series available");
        }
        return locationAndTime[item].getEpochSeconds();
    }

    /**
     * This method returns the number of data points available
     */
    public int getItemCount(int series) {
        if (series != 0) {
            throw new IllegalArgumentException("Only one series available");
        }
        return locationAndTime.length;
    }

    /**
     * This method returns the number of series available (for this case, it
     * will always return 1)
     */
    public int getSeriesCount() {
        return 1;
    }

    /**
     * This method returns the name of the data series
     */
    public String getSeriesName(int series) {
        if (series != 0) {
            return "Series Unknown";
        }
        return "GPS Data";
    }

    public Comparable getSeriesKey(int series) {
        return null;
    }

    // The local collection of LocationAndTime objects
    private final LocationAndTime[] locationAndTime;
}
