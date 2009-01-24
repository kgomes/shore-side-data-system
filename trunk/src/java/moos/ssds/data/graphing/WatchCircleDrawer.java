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
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import moos.ssds.data.util.LocationAndTime;

import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.Drawable;
import org.jfree.ui.RectangleEdge;

/**
 * An implementation of the {@link Drawable}interface, to illustrate the use of
 * the {@link org.jfree.chart.annotations.XYDrawableAnnotation}class.
 * 
 * @author David Gilbert
 */
public class WatchCircleDrawer implements XYAnnotation {

    /**
     * Creates a new instance.
     */
    public WatchCircleDrawer(LocationAndTime location,
        double watchCircleDiameter) {
        this.anchorLocation = location;
        this.watchCircleDiameter = watchCircleDiameter;
    }

    public double getDifferenceBetweenCoordinates(double coordinate1,
        double coordinate2, ValueAxis rangeAxis, Rectangle2D dataArea,
        RectangleEdge rangeEdge) {
        double latitudeOnScreen1 = rangeAxis.valueToJava2D(coordinate1,
            dataArea, rangeEdge);
        double latitudeOnScreen2 = rangeAxis.valueToJava2D(coordinate2,
            dataArea, rangeEdge);
        return latitudeOnScreen1 > latitudeOnScreen2 ? latitudeOnScreen1
            - latitudeOnScreen2 : latitudeOnScreen2 - latitudeOnScreen1;
    }

    public LocationAndTime getUpperLeftCornerOfCircle() {
        double longitude = anchorLocation.getLongitude().doubleValue()
            - LatLonConverter.convertKilometersToDegreesAtLatitude(
                watchCircleDiameter / 2, anchorLocation.getLatitude()
                    .doubleValue());
        double latitude = anchorLocation.getLatitude().doubleValue()
            + LatLonConverter
                .convertKilometersToDegrees(watchCircleDiameter / 2);
        return new LocationAndTime(new Double(latitude), new Double(longitude),
            null, new Long(0));
    }

    public LocationAndTime getLowerRightCornerOfCircle() {
        LocationAndTime upperRight = getUpperLeftCornerOfCircle();
        double latitude = upperRight.getLatitude().doubleValue()
            - LatLonConverter.convertKilometersToDegrees(watchCircleDiameter);
        double longitude = upperRight.getLongitude().doubleValue()
            + LatLonConverter
                .convertKilometersToDegreesAtLatitude(watchCircleDiameter,
                    anchorLocation.getLatitude().doubleValue());
        return new LocationAndTime(new Double(latitude), new Double(longitude),
            null, new Long(0));
    }

    /**
     * Draws the circle.
     * 
     * @param g2
     *            the graphics device.
     * @param area
     *            the area in which to draw.
     */
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
        ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex,
        PlotRenderingInfo info) {
        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot
            .getDomainAxisLocation(), orientation);
        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot
            .getRangeAxisLocation(), orientation);
        LocationAndTime location = getUpperLeftCornerOfCircle();
        float xOnScreen = (float) domainAxis.valueToJava2D(location
            .getLongitude().doubleValue(), dataArea, domainEdge);
        float yOnScreen = (float) rangeAxis.valueToJava2D(location
            .getLatitude().doubleValue(), dataArea, rangeEdge);
        // get the diameter of the circle given the current coordinate system
        // mapping

        double lon = anchorLocation.getLatitude().doubleValue();
        double lon2 = lon
            + LatLonConverter
                .convertKilometersToDegreesAtLatitude(watchCircleDiameter,
                    anchorLocation.getLatitude().doubleValue());
        double diameterX = getDifferenceBetweenCoordinates(lon, lon2,
            domainAxis, dataArea, domainEdge);
        double lat = anchorLocation.getLatitude().doubleValue();
        double lat2 = lat
            + LatLonConverter.convertKilometersToDegrees(watchCircleDiameter);
        double diameterY = getDifferenceBetweenCoordinates(lat, lat2,
            rangeAxis, dataArea, rangeEdge);

        ellipse = new Ellipse2D.Double(xOnScreen, yOnScreen, diameterX,
            diameterY);

        if (this.outlinePaint != null && this.outlineStroke != null) {
            g2.setPaint(this.outlinePaint);
            g2.setStroke(this.outlineStroke);
            g2.draw(ellipse);
        }
    }

    /**
     * @return Returns the fillPaint.
     */
    public Paint getFillPaint() {
        return fillPaint;
    }

    /**
     * @param fillPaint
     *            The fillPaint to set.
     */
    public void setFillPaint(Paint fillPaint) {
        this.fillPaint = fillPaint;
    }

    /**
     * @return Returns the outlinePaint.
     */
    public Paint getOutlinePaint() {
        return outlinePaint;
    }

    /**
     * @param outlinePaint
     *            The outlinePaint to set.
     */
    public void setOutlinePaint(Paint outlinePaint) {
        this.outlinePaint = outlinePaint;
    }

    /**
     * @return Returns the outlineStroke.
     */
    public Stroke getOutlineStroke() {
        return outlineStroke;
    }

    /**
     * @param outlineStroke
     *            The outlineStroke to set.
     */
    public void setOutlineStroke(Stroke outlineStroke) {
        this.outlineStroke = outlineStroke;
    }

    /** The outline paint. */
    private Paint outlinePaint = Color.black;

    /** The outline stroke. */
    private Stroke outlineStroke = new BasicStroke();

    /** The fill paint. */
    private Paint fillPaint;

    private Ellipse2D ellipse;

    /** The location of the anchor */
    private final LocationAndTime anchorLocation;

    /** The watch circle diameter in km */
    private final double watchCircleDiameter;

}