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

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;

/**
 * Format numbers into Degrees, Minutes, Decimal Minutes for JFreeChart Axis.
 * I've only overridden the format(double, StringBuffer, FieldPosition) method
 * because that is all that JFreeChart needs.
 * Created on Jun 16, 2004
 * 
 * @author achase
 */

public class DegreesMinutesNumberFormat extends NumberFormat {

    /*
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#parse(java.lang.String,
     *      java.text.ParsePosition)
     */
    public Number parse(String source, ParsePosition parsePosition) {
        try {
            return super.parse(source);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#format(double, java.lang.StringBuffer,
     *      java.text.FieldPosition)
     */
    public StringBuffer format(double number, StringBuffer toAppendTo,
        FieldPosition pos) {
        return toAppendTo.append(LatLonConverter.getDegreeDecimalMinute(number,
            DECIMAL_MINUTE_PRECISION));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#format(long, java.lang.StringBuffer,
     *      java.text.FieldPosition)
     */
    public StringBuffer format(long number, StringBuffer toAppendTo,
        FieldPosition pos) {
        throw new IllegalArgumentException("doubles only please");
    }

    // Set the default minute precision
    final static int DECIMAL_MINUTE_PRECISION = 4;
}