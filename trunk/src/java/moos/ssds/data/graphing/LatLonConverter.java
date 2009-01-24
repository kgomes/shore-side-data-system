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

/**
 * This is a utitlity class for performing transformations on lat/lon values
 * Created on May 13, 2004
 * 
 * @author achase
 */
public class LatLonConverter {

    /**
     * Convert from kilometers to degrees at a given latitude. This is needed
     * because of the shrinking x-axis as latitudes move towards the earths
     * poles.
     * 
     * @param kilometers
     *            The number of kilometers to convert to degrees
     * @param latitude
     *            The latitude at which to perform the conversion
     * @return The number of degrees, in longitude, needed to travel the given
     *         number of kilometers at the given latitude
     */
    public static double convertKilometersToDegreesAtLatitude(
        double kilometers, double latitude) {
        if (kilometers <= 0) {
            throw new IllegalArgumentException(
                "Distance must be a positive value");
        }
        double kilometersPerDegree = getKilometersPerDegreeAtLatitude(latitude);
        return kilometers / kilometersPerDegree;
    }

    private static double getKilometersPerDegreeAtLatitude(double latitude) {
        // It doesn't matter whether the latitude is north or south, so go ahead
        // and normalize it
        latitude = Math.abs(latitude);
        if (latitude > 90) {
            throw new IllegalArgumentException(
                "Latitude can not be greater than 90");
        }
        // find the radius of the earth at the given latitude, numbers must be
        // converted to radians
        // to work with the java.lang.Math library
        double radiusAtLatitude = RADIUS_OF_EARTH
            * Math.sin(convertFromDegreesToRadians(90 - latitude));
        // now find the circumference of the earth
        double circumferenceAtLatitude = radiusAtLatitude * 2 * Math.PI;
        // and finally, the multiplier for kilometers per degree
        double kilometersPerDegree = circumferenceAtLatitude / 360;
        return kilometersPerDegree;
    }

    public static double convertDegreesToKilometersAtLatitude(double degrees,
        double latitude) {
        double kilometersPerDegree = getKilometersPerDegreeAtLatitude(latitude);
        return degrees * kilometersPerDegree;
    }

    /**
     * Convert kilometers to degrees at the equator, works great for latitude,
     * not valid away from the equator with longitude.
     * 
     * @param kilometers
     * @return
     */
    public static double convertKilometersToDegrees(double kilometers) {
        return convertKilometersToDegreesAtLatitude(kilometers, 0);
    }

    private static double convertFromDegreesToRadians(double degrees) {
        return (degrees * Math.PI) / 180;
    }

    /**
     * Takes a representing latitude in the form DDMM.MMMM where D is degrees
     * and can be any value from 0 to 90, M is minutes as a decimal value. If
     * there is a degree value, there must be a minute value as well.
     * 
     * @param latitude
     * @param direction
     * @return
     */
    public static double convertDegreeMinuteLatitudeToDecimalDegree(
        String latitude, char direction) {
        Number[] degreesAndMinutes = parseCoordinateString(latitude);
        int degrees = degreesAndMinutes[0].intValue();
        double minutes = degreesAndMinutes[1].doubleValue();
        return convertDegreeMinuteLatitudeToDecimalDegree(degrees, minutes,
            direction);
    }

    public static double convertDegreeMinuteLongitudeToDecimalDegree(
        String longitude, char direction) {
        Number[] degreesAndMinutes = parseCoordinateString(longitude);
        int degrees = degreesAndMinutes[0].intValue();
        double minutes = degreesAndMinutes[1].doubleValue();
        return convertDegreeMinuteLongitudeToDecimalDegree(degrees, minutes,
            direction);
    }

    private static Number[] parseCoordinateString(String coordinate) {
        int degrees;
        double minutes;

        int decimalPosition = coordinate.indexOf('.');
        int minutePosition = decimalPosition - 2;
        if (minutePosition < 0) {
            minutePosition = 0;
        }
        if (decimalPosition == -1) {
            decimalPosition = coordinate.length();
        }

        if (minutePosition == 0) {
            degrees = 0;
        } else {
            String degreesString = coordinate.substring(0, minutePosition);
            degrees = Integer.parseInt(degreesString);
        }

        String minutesString = coordinate.substring(minutePosition);
        minutes = Double.parseDouble(minutesString);

        return new Number[]{new Integer(degrees), new Double(minutes)};
    }

    /**
     * Yikes, couldn't think of a better name for this method. It does what it
     * says, converts from a latitude in the format DDMM.MMMM where D is whole
     * degrees, and MM.MM is a float minute value.
     * 
     * @param latitude
     *            The latitude value to convert.
     * @param direction
     *            The indicator whether this value is north or south of the
     *            equator
     * @return Latitude in decimal degrees
     */
    // TODO achase 20040513 check for illegal values
    public static double convertDegreeMinuteLatitudeToDecimalDegree(
        int degrees, double minutes, char direction) {
        double degreesLatitude = degrees + (minutes / 60.0);

        if (NORTH != Character.toLowerCase(direction)) {
            degreesLatitude *= -1.0;
        }

        return degreesLatitude;
    }

    /**
     * @param longitude
     * @param direction
     * @return
     */
    // TODO achase 20040513 check for illegal values
    public static double convertDegreeMinuteLongitudeToDecimalDegree(
        int degrees, double minutes, char direction) {
        double degreesLongitude = degrees + (minutes / 60.0);

        if (EAST != Character.toLowerCase(direction)) {
            degreesLongitude *= -1.0;
        }

        return degreesLongitude;
    }

    public static String getDegreeDecimalMinute(double degreeDecimal,
        int precision) {
        if (precision < 0) {
            throw new IllegalArgumentException("precision can not be negative");
        }
        // everything before the decimal is a whole degree, after the decimal
        // comes the minutes
        int degrees = (int) degreeDecimal;
        double minutes = (degreeDecimal - degrees) * 60;
        // degrees contains the sign, so remove it from minutes
        minutes = Math.abs(minutes);
        // move minutes to the proper precision
        minutes = minutes * Math.pow(10, precision);
        minutes = Math.round(minutes);
        minutes = minutes / Math.pow(10, precision);
        String minutesString = Double.toString(minutes);
        // make sure there is the proper number of zeros following the final
        // real number
        int currentPrecision = minutesString.length()
            - (minutesString.indexOf('.') + 1);
        if (currentPrecision < precision) {
            int zerosNeeded = precision - currentPrecision;
            for (int i = 0; i < zerosNeeded; i++) {
                minutesString += "0";
            }
        }
        if (precision == 0) {
            minutesString = minutesString.substring(0, minutesString
                .indexOf('.'));
        }
        return degrees + "\u00B0" + minutesString + "\'";
    }

    // Some static variables for simplifying some tasks
    final static char NORTH = 'n';
    final static char EAST = 'e';
    final static double RADIUS_OF_EARTH = 6378.1;
}
