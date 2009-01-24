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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import moos.ssds.data.util.LocationAndTime;

import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;

/**
 * @author achase
 */
public class DistanceScaleDrawer implements XYAnnotation {

    private final double scaleRangeInKilometers;
    private final double scaleRangeInDegrees;
    private final LocationAndTime locationToDrawAt;
    private int decimalPlacesShown = 1;
    double height = 5;

    public DistanceScaleDrawer(double scaleRangeInKilometers,
        LocationAndTime locationToDrawAt) {
        this.scaleRangeInKilometers = scaleRangeInKilometers;
        scaleRangeInDegrees = LatLonConverter
            .convertKilometersToDegreesAtLatitude(scaleRangeInKilometers,
                locationToDrawAt.getLatitude().doubleValue());
        this.locationToDrawAt = locationToDrawAt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jfree.chart.annotations.XYAnnotation#draw(java.awt.Graphics2D,
     *      org.jfree.chart.plot.XYPlot, java.awt.geom.Rectangle2D,
     *      org.jfree.chart.axis.ValueAxis, org.jfree.chart.axis.ValueAxis)
     */
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
        ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex,
        PlotRenderingInfo info) {
        // convert from lat/lon coordinates to the coordinates used for drawing
        // by the swing graphics toolkit.
        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot
            .getDomainAxisLocation(), orientation);
        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot
            .getRangeAxisLocation(), orientation);

        float xOnScreen = (float) domainAxis.valueToJava2D(locationToDrawAt
            .getLongitude().doubleValue(), dataArea, domainEdge);
        float yOnScreen = (float) rangeAxis.valueToJava2D(locationToDrawAt
            .getLatitude().doubleValue(), dataArea, rangeEdge);
        // substract three times the height, assume one height for the box
        // one height for the text, and one more for a border
        yOnScreen -= height * 3;

        double longitudeStart = locationToDrawAt.getLongitude().doubleValue()
            - scaleRangeInDegrees;
        float xOnScreenStart = (float) domainAxis.valueToJava2D(longitudeStart,
            dataArea, domainEdge);

        g2.setColor(Color.black);

        double width = xOnScreen - xOnScreenStart;
        // add a little buffer to the right of the scale by shifting the
        // xOnScreen value
        xOnScreenStart -= 10;

        // create two rectangles each representing half the scale
        Rectangle2D scaleRect = new Rectangle2D.Double(xOnScreenStart,
            yOnScreen, width, height);
        double halfWidth = width / 2.0;
        Rectangle2D scaleRect1 = new Rectangle2D.Double(scaleRect.getX(),
            scaleRect.getY(), halfWidth, scaleRect.getHeight());
        Rectangle2D scaleRect2 = new Rectangle2D.Double(scaleRect.getX()
            + halfWidth, scaleRect.getY(), halfWidth, scaleRect.getHeight());

        g2.setStroke(new BasicStroke(1.0f));
        g2.fill(scaleRect1);
        g2.draw(scaleRect1);

        double range = scaleRangeInKilometers;
        String units;
        if (range < 1) {
            units = "m";
            range *= 1000;
        } else {
            units = "km";
        }
        // truncate the kilometers
        String kilometers = range + "";
        int indexOfDecimal = kilometers.indexOf('.');
        if (indexOfDecimal != -1
            && !((indexOfDecimal + decimalPlacesShown) > kilometers.length())) {
            kilometers = kilometers.substring(0, indexOfDecimal
                + decimalPlacesShown + 1);
        }
        g2.drawString(kilometers + " " + units,
            (float) (scaleRect.getX() + halfWidth / 2), (float) (scaleRect
                .getY()
                - scaleRect.getHeight() - 1));
        g2.draw(scaleRect2);
    }

    public static void main(String[] args) {}

    /**
     * @return Returns the decimalPlacesShown.
     */
    public int getDecimalPlacesShown() {
        return decimalPlacesShown;
    }

    /**
     * @param decimalPlacesShown
     *            The decimalPlacesShown to set.
     */
    public void setDecimalPlacesShown(int decimalPlacesShown) {
        this.decimalPlacesShown = decimalPlacesShown;
    }
}