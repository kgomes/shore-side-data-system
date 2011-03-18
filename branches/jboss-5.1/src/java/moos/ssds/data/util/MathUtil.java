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

import java.util.Arrays;
import java.util.TreeMap;

/**
 * <p>
 * Static methods for doing useful math
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.3 $
 */
public class MathUtil {

    public static final double[] interpLinear(double[] x, double[] y,
        double[] xi) throws IllegalArgumentException {

        if (x.length != y.length)
            throw new IllegalArgumentException(
                "X and Y must be the same length");
        if (x.length == 1)
            throw new IllegalArgumentException(
                "X must contain more than one value");

        double[] dx = new double[x.length - 1], dy = new double[x.length - 1], slope = new double[x.length - 1], intercept = new double[x.length - 1];

        // Calculate the line equation (i.e. slope and intercept) between each
        // point
        for (int i = 0; i < x.length - 1; i++) {
            dx[i] = x[i + 1] - x[i];
            if (dx[i] == 0)
                throw new IllegalArgumentException(
                    "X must be montotonic. A duplicate " + "x-value was found");
            if (dx[i] < 0)
                throw new IllegalArgumentException("X must be sorted");
            dy[i] = y[i + 1] - y[i];
            slope[i] = dy[i] / dx[i];
            intercept[i] = y[i] - x[i] * slope[i];
        }

        // Perform the interpolation here
        double[] yi = new double[xi.length];
        for (int i = 0; i < xi.length; i++) {
            if ((xi[i] > x[x.length - 1]) || (xi[i] < x[0])) {
                yi[i] = Double.NaN;
            } else {
                int loc = Arrays.binarySearch(x, xi[i]);
                if (loc < -1) {
                    loc = -loc - 2;
                    yi[i] = slope[loc] * xi[i] + intercept[loc];
                } else {
                    yi[i] = y[loc];
                }
            }
        }

        return yi;
    }

    public static final double[] interpLinear(long[] x, double[] y, long[] xi)
        throws IllegalArgumentException {

        double[] xd = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            xd[i] = (double) x[i];
        }

        double[] xid = new double[xi.length];
        for (int i = 0; i < xi.length; i++) {
            xid[i] = (double) xi[i];
        }

        return MathUtil.interpLinear(xd, y, xid);

    }

    public static final int find(double[] array, double valueToFind) {
        return Arrays.binarySearch(array, valueToFind);
    }

    public static double mod(double x, double y) {
        double m = x;
        if (y != 0) {
            m = x - y * Math.floor(x / y);
        }
        return m;
    }

    public static double rem(double x, double y) {
        double m = x;
        if (y != 0) {
            m = x - y * MathUtil.fix(x / y);
        }
        return m;
    }

    public static double fix(double x) {
        int sign = MathUtil.sign(x);
        double y = 0;
        if (sign == -1) {
            y = Math.ceil(x);
        } else if (sign == 1) {
            y = Math.floor(x);
        }
        return y;
    }

    public static int sign(double x) {
        int s = 0;
        if (x > 0) {
            s = 1;
        } else if (x < 0) {
            s = -1;
        }
        return s;
    }

    /**
     * Cumulatively sum a vector Example: cumSum([1 1 1 1 2]) = [1 2 3 4 6]
     */
    public static double[] cumSum(double[] n) {
        double[] buf = new double[n.length];

        for (int i = 0; i < n.length; i++) {
            if (i == 0) {
                buf[i] = n[0];
            } else {
                buf[i] = buf[i - 1] + n[i];
            }
        }

        return buf;
    }

    public static boolean isEven(double x) {
        double i = MathUtil.rem(x, 2.0);
        boolean even = true;
        if (i != 0.0)
            even = false;
        return even;
    }

    /**
     * Returns an array of indices indicating the order the data should be
     * sorted in. Duplicate values are discarded with the first one being kept.
     * This method is useful when a number of data arrays have to be sorted
     * based on the values in some coordinate array, such as time. To convert a
     * array of values to a sorted monooic array try: <br>
     * double[] x; // some 1-D array of data <br>
     * int[] i = MathUtil.uniqueSort(x); <br>
     * double[] xSorted = MathUtil.orderVector(x, i);<br>
     * <br>
     * 
     * @param x
     *            An array of data that is to be sorted.
     * @return order An array of indexes such that y = Array.sort(x) and y =
     *         x(order) are the same.
     */
    public static final synchronized int[] uniqueSort(double[] x) {
        TreeMap tm = new TreeMap();
        for (int i = 0; i < x.length; i++) {
            Double key = new Double(x[i]);
            boolean exists = tm.containsKey(key);
            if (exists) {
                // Do nothing. Ignore duplicate keys
            } else {
                tm.put(key, new Integer(i));
            }
        }
        Object[] values = tm.values().toArray();
        int[] order = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            Integer tmp = (Integer) values[i];
            order[i] = tmp.intValue();
        }
        return order;
    }

    /**
     * Returns an array of indices indicating the order the data should be
     * sorted in. Duplicate values are discarded with the first one being kept.
     * This method is useful when a number of data arrays have to be sorted
     * based on the values in some coordinate array, such as time. To convert a
     * array of values to a sorted monooic array try: <br>
     * double[] x; // some 1-D array of data <br>
     * int[] i = MathUtil.uniqueSort(x); <br>
     * double[] xSorted = MathUtil.orderVector(x, i);<br>
     * <br>
     * 
     * @param x
     *            An array of data that is to be sorted.
     * @return order An array of indexes such that y = Array.sort(x) and y =
     *         x(order) are the same.
     */
    public static final synchronized int[] uniqueSort(long[] x) {
        TreeMap tm = new TreeMap();
        for (int i = 0; i < x.length; i++) {
            Long key = new Long(x[i]);
            boolean exists = tm.containsKey(key);
            if (exists) {
                // Do nothing. Ignore duplicate keys
            } else {
                tm.put(key, new Integer(i));
            }
        }
        Object[] values = tm.values().toArray();
        int[] order = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            Integer tmp = (Integer) values[i];
            order[i] = tmp.intValue();
        }
        return order;
    }

    public static final synchronized int[] uniqueSort(Long[] x) {
        TreeMap tm = new TreeMap();
        for (int i = 0; i < x.length; i++) {
            Long key = x[i];
            boolean exists = tm.containsKey(key);
            if (exists) {
                // Do nothing. Ignore duplicate keys
            } else {
                tm.put(key, new Integer(i));
            }
        }
        Object[] values = tm.values().toArray();
        int[] order = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            Integer tmp = (Integer) values[i];
            order[i] = tmp.intValue();
        }
        return order;
    }

    /**
     * Returns an array of indices indicating the order the data should be
     * sorted in. Duplicate values are NOT discarded and are in the order of the
     * original array. This method is useful when a number of data arrays have
     * to be sorted based on the values in some coordinate array, such as time.
     * To convert a array of values to a sorted monooic array try: <br>
     * double[] x; // some 1-D array of data <br>
     * int[] i = MathUtil.sort(x); <br>
     * double[] xSorted = MathUtil.orderVector(x, i);<br>
     * <br>
     * 
     * @param x
     *            An array of data that is to be sorted.
     * @return order An array of indexes such that y = Array.sort(x) and y =
     *         x(order) are the same.
     */
    public static final synchronized int[] getSortOrder(double[] x) {
        // Create an array the same size as the incoming array
        int[] orderToReturn = new int[x.length];
        // Create a counter
        for (int i = 0; i < x.length; i++) {
            if (i > 0) {
                boolean inserted = false;
                for (int j = 0; j < i; j++) {
                    if (x[orderToReturn[j]] > x[i]) {
                        // Move all greater values down
                        for (int k = i; k > j; k--) {
                            orderToReturn[k] = orderToReturn[k - 1];
                        }
                        // Add at the location
                        orderToReturn[j] = i;
                        inserted = true;
                    }
                    if (inserted)
                        break;
                }
                if (!inserted)
                    orderToReturn[i] = i;
            } else {
                orderToReturn[0] = i;
            }
        }
        return orderToReturn;
    }

    /**
     * Returns an array of indices indicating the order the data should be
     * sorted in. Duplicate values are NOT discarded and are in the order of the
     * original array. This method is useful when a number of data arrays have
     * to be sorted based on the values in some coordinate array, such as time.
     * To convert a array of values to a sorted monooic array try: <br>
     * long[] x; // some 1-D array of data <br>
     * int[] i = MathUtil.uniqueSort(x); <br>
     * long[] xSorted = MathUtil.orderVector(x, i);<br>
     * <br>
     * 
     * @param x
     *            An array of data that is to be sorted.
     * @return order An array of indexes such that y = Array.sort(x) and y =
     *         x(order) are the same.
     */
    public static final synchronized int[] getSortOrder(long[] x) {
        // Create an array the same size as the incoming array
        int[] orderToReturn = new int[x.length];
        // Create a counter
        for (int i = 0; i < x.length; i++) {
            if (i > 0) {
                boolean inserted = false;
                for (int j = 0; j < i; j++) {
                    if (x[orderToReturn[j]] > x[i]) {
                        // Move all greater values down
                        for (int k = i; k > j; k--) {
                            orderToReturn[k] = orderToReturn[k - 1];
                        }
                        // Add at the location
                        orderToReturn[j] = i;
                        inserted = true;
                    }
                    if (inserted)
                        break;
                }
                if (!inserted)
                    orderToReturn[i] = i;
            } else {
                orderToReturn[0] = i;
            }
        }
        return orderToReturn;
    }

    /**
     * Returns an array of indices indicating the order the data should be
     * sorted in. Duplicate values are NOT discarded and are in the order of the
     * original array. This method is useful when a number of data arrays have
     * to be sorted based on the values in some coordinate array, such as time.
     * To convert a array of values to a sorted monooic array try: <br>
     * Long[] x; // some 1-D array of data <br>
     * int[] i = MathUtil.uniqueSort(x); <br>
     * Long[] xSorted = MathUtil.orderVector(x, i);<br>
     * <br>
     * 
     * @param x
     *            An array of data that is to be sorted.
     * @return order An array of indexes such that y = Array.sort(x) and y =
     *         x(order) are the same.
     */
    public static final synchronized int[] getSortOrder(Long[] x) {
        // Create an array the same size as the incoming array
        int[] orderToReturn = new int[x.length];
        // Create a counter
        for (int i = 0; i < x.length; i++) {
            if (i > 0) {
                boolean inserted = false;
                for (int j = 0; j < i; j++) {
                    if (x[orderToReturn[j]].longValue() > x[i].longValue()) {
                        // Move all greater values down
                        for (int k = i; k > j; k--) {
                            orderToReturn[k] = orderToReturn[k - 1];
                        }
                        // Add at the location
                        orderToReturn[j] = i;
                        inserted = true;
                    }
                    if (inserted)
                        break;
                }
                if (!inserted)
                    orderToReturn[i] = i;
            } else {
                orderToReturn[0] = i;
            }
        }
        return orderToReturn;
    }

    /**
     * Useful method for ordering a 1-D array based on an array of indices
     * 
     * @see uniqueSort
     * @param values
     *            A 1-D array of data to be sorted based on an array of indices
     * @param order
     *            A 1-D array of indices specifying the ordering of the data.
     */
    public static final double[] orderVector(double[] values, int[] order) {
        double[] out = new double[order.length];
        // for (int i = 1; i < order.length; i++) {
        for (int i = 0; i < order.length; i++) {
            out[i] = values[order[i]];
        }
        return out;
    }

    public static final float[] orderVector(float[] values, int[] order) {
        float[] out = new float[order.length];
        // for (int i = 1; i < order.length; i++) {
        for (int i = 0; i < order.length; i++) {
            out[i] = values[order[i]];
        }
        return out;
    }

    public static final long[] orderVector(long[] values, int[] order) {
        long[] out = new long[order.length];
        // for (int i = 1; i < order.length; i++) {
        for (int i = 0; i < order.length; i++) {
            out[i] = values[order[i]];
        }
        return out;
    }

    public static final Object[] orderVector(Object[] values, int[] order) {
        Object[] out = new Object[order.length];
        // for (int i = 1; i < order.length; i++) {
        for (int i = 0; i < order.length; i++) {
            out[i] = values[order[i]];
        }
        return out;
    }

    /**
     * Find the index of the value nearest to the key. The values array can
     * contain only unique values. If it doesn't the first occurence of a value
     * in the values array is the one used, subsequent duplicate are ignored.
     * 
     * @param values
     *            Values to search through for the nearest point.
     * @param key
     *            The key to search for the nearest neighbor in values.
     * @return the index of the value nearest to the key.
     * @since 20011207
     */
    public static final int near(double[] values, double key) {

        int n = binarySearch(values, key);
        if (n < 0) { // when n is an insertion point
            n = (-n) - 1; // Convert n to an index

            // If n == 0 we don't need to find nearest neighbor. If n is
            // larger than the array, use the last point in the array.
            if (n > values.length - 1) {
                n = values.length - 1;
                // If key is larger than any value in values
            } else if (n > 0) { // find nearest neighbor
                double d1 = values[n - 1] - key;
                double d2 = values[n] - key;
                if (d1 <= d2) {
                    n = n - 1;
                }
            }
        }
        return n;
    }

    /**
     * Searches the specified array of doubles for the specified value using the
     * binary search algorithm. The array <strong>must</strong> be sorted (as
     * by the <tt>sort</tt> method, above) prior to making this call. If it is
     * not sorted, the results are undefined. If the array contains multiple
     * elements with the specified value, there is no guarantee which one will
     * be found. The array can be sorted from low values to high or from high
     * values to low.
     * 
     * @param a
     *            the array to be searched.
     * @param key
     *            the value to be searched for.
     * @return index of the search key, if it is contained in the list;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>. The
     *         <i>insertion point</i> is defined as the point at which the key
     *         would be inserted into the list: the index of the first element
     *         greater than the key, or <tt>list.size()</tt>, if all elements
     *         in the list are less than the specified key. Note that this
     *         guarantees that the return value will be &gt;= 0 if and only if
     *         the key is found.
     * @see #sort(double[])
     */
    public static int binarySearch(double[] a, double key) {
        int index = -1;
        if (a[0] < a[1]) {
            index = Arrays.binarySearch(a, key);
        } else {
            index = binarySearch(a, key, 0, a.length - 1);
        }
        return index;
    }

    private static int binarySearch(double[] a, double key, int low, int high) {
        while (low <= high) {
            int mid = (low + high) / 2;
            double midVal = a[mid];

            int cmp;
            if (midVal > key) {
                cmp = -1; // Neither val is NaN, thisVal is smaller
            } else if (midVal < key) {
                cmp = 1; // Neither val is NaN, thisVal is larger
            } else {
                long midBits = Double.doubleToLongBits(midVal);
                long keyBits = Double.doubleToLongBits(key);
                cmp = (midBits == keyBits ? 0 : // Values are equal
                    (midBits < keyBits ? -1 : // (-0.0, 0.0) or (!NaN, NaN)
                        1)); // (0.0, -0.0) or (NaN, !NaN)
            }

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -(low + 1); // key not found.
    }

}