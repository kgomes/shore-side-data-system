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
package moos.ssds.metadata.util;

/**
 * This class provides some validators that are used in various places in the
 * setter methods of the Metadata classes. These methods throw
 * MetadataExceptions so they can be used directly in the IMetadataObject
 * classes.
 * 
 * @author kgomes
 */
public class MetadataValidator {

    /**
     * This method checks to see if the supplied string is shorten than the
     * length given
     * 
     * @param stringToCheck
     *            the string to have its length checked
     * @param length
     *            the length that the string must be shorten than
     * @throws MetadataException
     *             if the string is longer than the int specified
     */
    public static void isStringShorterThan(String stringToCheck, int length)
        throws MetadataException {
        if ((stringToCheck != null) && (stringToCheck.length() > length))
            throw new MetadataException("The string supplied was of length "
                + stringToCheck.length() + " which is longer than " + length
                + " characters, please shorten");
    }

    /**
     * This method checks to see if the incoming object is null
     * 
     * @param object
     *            the object to check
     * @throws MetadataException
     *             if the object is null
     */
    public static void isObjectNull(Object object) throws MetadataException {
        if (object == null)
            throw new MetadataException(
                "The incoming object was null, it cannot be");
    }

    /**
     * This method checks to see if the supplied value is between the min and
     * maxs
     * 
     * @param doubleToCheck
     * @param min
     * @param max
     * @throws MetadataException
     */
    public static void isValueBetween(Double doubleToCheck, double min,
        double max) throws MetadataException {
        if ((doubleToCheck != null)
            && ((doubleToCheck.doubleValue() < min) || (doubleToCheck
                .doubleValue() > max)))
            throw new MetadataException("The supplied value of "
                + doubleToCheck.doubleValue() + " was not between " + min
                + " and " + max + ", please correct.");
    }

    /**
     * This method checks to see if the supplied value is between the min and
     * max supplied.
     * 
     * @param floatToCheck
     * @param min
     * @param max
     * @throws MetadataException
     */
    public static void isValueBetween(Float floatToCheck, float min, float max)
        throws MetadataException {
        if ((floatToCheck != null)
            && ((floatToCheck.floatValue() < min) || (floatToCheck.floatValue() > max)))
            throw new MetadataException("The supplied value of "
                + floatToCheck.floatValue() + " was not between " + min
                + " and " + max + ", please correct.");
    }

    /**
     * This method checks to see if the incoming value is greater than the min
     * supplied
     * 
     * @param doubleToCheck
     * @param min
     * @throws MetadataException
     */
    public static void isValueGreaterThan(Double doubleToCheck, double min)
        throws MetadataException {
        if ((doubleToCheck != null) && (doubleToCheck.doubleValue() < min))
            throw new MetadataException("The supplied value of "
                + doubleToCheck.doubleValue() + " was not greater than " + min
                + ", please correct.");
    }

    /**
     * This method checks to see if the incoming value is greater than the min
     * supplied
     * 
     * @param floatToCheck
     * @param min
     * @throws MetadataException
     */
    public static void isValueGreaterThan(Float floatToCheck, float min)
        throws MetadataException {
        if ((floatToCheck != null) && (floatToCheck.floatValue() < min))
            throw new MetadataException("The supplied value of "
                + floatToCheck.floatValue() + " was not greater than " + min
                + ", please correct.");
    }

    /**
     * These are constants for various attributes of metadata objects
     */
    public static final int COMMENT_TAG_LENGTH = 10;
    public static final int NAME_LENGTH = 255;
    public static final int DESCRIPTION_LENGTH = 2048;
    public static final int URI_STRING_LENGTH = 2048;
    public static final int RESOURCE_BLOB_LENGTH = 2048;
    public static final double MIN_LATITUDE = -90.0;
    public static final double MAX_LATITUDE = 90.0;
    public static final double MIN_LONGITUDE = -360.0;
    public static final double MAX_LONGITUDE = 360.0;
    public static final float DEPTH_MIN = 0.0F;
}
