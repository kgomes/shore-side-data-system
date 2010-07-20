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
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import moos.ssds.data.util.LocationAndTime;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYDrawableAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.servlet.ServletUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Drawable;

/**
 * Created on May 14, 2004
 * 
 * @author achase
 */
public class GpsCharter {

    /**
     * This is a constructor
     * 
     * @param anchorLocation
     * @param watchCircleSizeKm
     */
    public GpsCharter(final LocationAndTime[] locationsAndTimes,
        boolean drawWatchCircle, double watchCircleDiameterInKm,
        boolean scaleChartToFitData, boolean plotAnchorLocation,
        LocationAndTime anchorLocation) {

        // Assign the local variables to those that are incoming
        this.anchorLocation = anchorLocation;
        this.watchCircleSize = watchCircleDiameterInKm;
        this.locationsAndTimes = locationsAndTimes;
        this.scaleOnlyToWatchCircle = !scaleChartToFitData;

        // Change the number format on the lat axis and the lon axis
        longitudeAxis.setNumberFormatOverride(new DegreesMinutesNumberFormat());
        latitudeAxis.setNumberFormatOverride(new DegreesMinutesNumberFormat());
    }

    /**
     * This is the method that return the JFreeChart for the data that was
     * supplied in construction of the object
     * 
     * @param title
     * @return
     */
    public JFreeChart getGpsPlot(String title) {

        // Create an XYDataset using an LocationAndTimeDataSet
        XYDataset gpsData = new LocationAndTimeDataset(locationsAndTimes);

        // Create a WatchCircleDrawer
        final WatchCircleDrawer circleDrawer = new WatchCircleDrawer(
            anchorLocation, watchCircleSize);

        // Do some setup
        establishMaxAndMins(circleDrawer);
        setLatLonAxisRanges();

        AbstractXYItemRenderer renderer = new XYCircleRenderer(
            locationsAndTimes.length);

        colorBar = ((XYCircleRenderer) renderer).getTimeBar(locationsAndTimes);

        XYPlotWithColorBar plot = new XYPlotWithColorBar(gpsData,
            longitudeAxis, latitudeAxis, renderer);
        plot.setColorBar(colorBar);
        colorBar.getAxis().setPlot(plot);

        AnchorDrawer anchorDrawer = new AnchorDrawer();
        XYAnnotation anchor = new XYDrawableAnnotation(anchorLocation
            .getLongitude().doubleValue(), anchorLocation.getLatitude()
            .doubleValue(), ANCHOR_SIZE, ANCHOR_SIZE, anchorDrawer);
        plot.addAnnotation(anchor);

        final LocationAndTime locationForScale = new LocationAndTime(
            new Double(latitudeAxis.getLowerBound()), new Double(longitudeAxis
                .getUpperBound()), null, new Long(0));
        final double kilometersOfScale = LatLonConverter
            .convertDegreesToKilometersAtLatitude(maxLongitude - minLongitude,
                minLatitude) / 4.0;
        final DistanceScaleDrawer scaleDrawer = new DistanceScaleDrawer(
            kilometersOfScale, locationForScale);
        plot.addAnnotation(scaleDrawer);

        XYAnnotation watchCircle = new WatchCircleDrawer(anchorLocation,
            watchCircleSize);
        plot.addAnnotation(watchCircle);

        if ((locationsAndTimes != null) && (locationsAndTimes.length > 0)) {
            plot.addAnnotation(getStartPointAnnotation());
            plot.addAnnotation(getEndPointAnnotation());
            plot.addAnnotation(getLastKnownCoordinatesAnnotation(latitudeAxis
                .getUpperBound(), longitudeAxis.getLowerBound()));
        }

        // remove the legend
        try {
            plot.setFixedLegendItems(new LegendItemCollection());
        } catch (Throwable e) {
            logger.error("Throwable caught trying to set fixed legend items: "
                + e.getMessage());
            e.printStackTrace();
        }

        JFreeChart chart = new JFreeChart(title, plot);
        return chart;
    }

    private void setLatLonAxisRanges() {
        latitudeAxis.setRange(minLatitude - .0001, maxLatitude + .0001);
        longitudeAxis.setRange(minLongitude - .0001, maxLongitude + .0001);
        latitudeAxis.setAutoRange(false);
        longitudeAxis.setAutoRange(false);
    }

    private void establishMaxAndMins(WatchCircleDrawer circleDrawer) {
        LocationAndTime watchCircleUpperLeft = circleDrawer
            .getUpperLeftCornerOfCircle();
        LocationAndTime watchCircleLowerRight = circleDrawer
            .getLowerRightCornerOfCircle();

        maxLongitude = watchCircleLowerRight.getLongitude().doubleValue();
        minLongitude = watchCircleUpperLeft.getLongitude().doubleValue();
        minLatitude = watchCircleLowerRight.getLatitude().doubleValue();
        maxLatitude = watchCircleUpperLeft.getLatitude().doubleValue();
        minTime = Long.MAX_VALUE;
        maxTime = Long.MIN_VALUE;
        if (!isScaleOnlyToWatchCircle()) {
            for (int i = 0; i < locationsAndTimes.length; i++) {
                if (locationsAndTimes[i].getLatitude().doubleValue() > maxLatitude)
                    maxLatitude = locationsAndTimes[i].getLatitude()
                        .doubleValue();
                if (locationsAndTimes[i].getLatitude().doubleValue() < minLatitude)
                    minLatitude = locationsAndTimes[i].getLatitude()
                        .doubleValue();
                if (locationsAndTimes[i].getLongitude().doubleValue() > maxLongitude)
                    maxLongitude = locationsAndTimes[i].getLongitude()
                        .doubleValue();
                if (locationsAndTimes[i].getLongitude().doubleValue() < minLongitude)
                    minLongitude = locationsAndTimes[i].getLongitude()
                        .doubleValue();
                if (locationsAndTimes[i].getEpochSeconds().longValue() > maxTime)
                    maxTime = locationsAndTimes[i].getEpochSeconds()
                        .longValue();
                if (locationsAndTimes[i].getEpochSeconds().longValue() < minTime)
                    minTime = locationsAndTimes[i].getEpochSeconds()
                        .longValue();
            }
            // now, adjust so that the scale is the same on the vertical and
            // horizontal axis.
            double latitudeDelta = Math.abs(maxLatitude - minLatitude);
            double longitudeDelta = Math.abs(maxLongitude - minLongitude);
            if (longitudeDelta > latitudeDelta) {
                double difference = longitudeDelta - latitudeDelta;
                maxLatitude += difference / 2;
                minLatitude -= difference / 2;
            } else if (latitudeDelta > longitudeDelta) {
                double difference = latitudeDelta - longitudeDelta;
                maxLongitude += difference / 2;
                minLongitude -= difference / 2;
            }
        }
    }

    private XYAnnotation getEndPointAnnotation() {
        final int circleRadius = 8;
        XYAnnotation endPoint = new XYDrawableAnnotation(
            locationsAndTimes[locationsAndTimes.length - 1].getLongitude()
                .doubleValue(), locationsAndTimes[locationsAndTimes.length - 1]
                .getLatitude().doubleValue(), 0, 0, new Drawable() {

                public void draw(Graphics2D g2, Rectangle2D drawSpace) {
                    drawBullsEye(g2, drawSpace, circleRadius, Color.red);
                    // g2.setColor(Color.black);
                    // Font oldFont = g2.getFont();
                    // g2.setFont(g2.getFont().deriveFont(Font.BOLD));
                    // g2.drawString("End", (int) drawSpace.getX()
                    // + circleRadius, (int) drawSpace.getY());
                    // g2.setFont(oldFont);
                }
            });
        return endPoint;
    }

    private void drawBullsEye(Graphics2D g2, Rectangle2D drawSpace,
        int circleRadius, Color circleColor) {
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(3));
        Ellipse2D ellipse = new Ellipse2D.Double(drawSpace.getX()
            - circleRadius, drawSpace.getY() - circleRadius, circleRadius * 2,
            circleRadius * 2);
        g2.setColor(Color.white);
        g2.fill(ellipse);
        g2.setColor(circleColor);
        g2.draw(ellipse);
        // draw cross hairs
        // vertical
        g2.drawLine((int) drawSpace.getX(), (int) drawSpace.getY()
            - circleRadius, (int) drawSpace.getX(),
            (int) (drawSpace.getY() + circleRadius));
        // horizontal
        g2.drawLine((int) drawSpace.getX() - circleRadius, (int) drawSpace
            .getY(), (int) (drawSpace.getX() + circleRadius), (int) drawSpace
            .getY());
        g2.setStroke(oldStroke);
    }

    private XYAnnotation getStartPointAnnotation() {
        final int circleRadius = 8;
        XYAnnotation startPoint = new XYDrawableAnnotation(locationsAndTimes[0]
            .getLongitude().doubleValue(), locationsAndTimes[0].getLatitude()
            .doubleValue(), 0, 0, new Drawable() {

            public void draw(Graphics2D g2, Rectangle2D drawSpace) {
                drawBullsEye(g2, drawSpace, circleRadius, Color.blue.darker());
                // g2.setColor(Color.black);
                // Font oldFont = g2.getFont();
                // g2.setFont(g2.getFont().deriveFont(Font.BOLD));
                // g2.drawString("Start",
                // (int) (drawSpace.getX() + circleRadius),
                // (int) drawSpace.getY());
                // g2.setFont(oldFont);
            }
        });
        return startPoint;
    }

    private XYAnnotation getLastKnownCoordinatesAnnotation(
        final double upperLeftLat, final double upperLeftLong) {
        XYAnnotation upperLeftCorner = new XYDrawableAnnotation(upperLeftLong,
            upperLeftLat, 0, 0, new Drawable() {

                static final int BORDER = 10;

                public void draw(Graphics2D g2, Rectangle2D drawSpace) {
                    LocationAndTime lastPoint = locationsAndTimes[locationsAndTimes.length - 1];
                    String latitude = LatLonConverter.getDegreeDecimalMinute(
                        lastPoint.getLatitude().doubleValue(), 3);
                    String longitude = LatLonConverter.getDegreeDecimalMinute(
                        lastPoint.getLongitude().doubleValue(), 3);
                    String position = "Last Known Position " + latitude
                        + " latitude " + longitude + " longitude";
                    String time = "Received on "
                        + lastPoint.getGmtDate().toString();
                    Rectangle2D bounds = g2.getFontMetrics().getStringBounds(
                        position, g2);
                    bounds = new Rectangle2D.Double(bounds.getX(), bounds
                        .getY(), bounds.getWidth(), bounds.getHeight());
                    Rectangle2D boundsTime = g2.getFontMetrics()
                        .getStringBounds(time, g2);
                    double height = bounds.getHeight() + boundsTime.getHeight()
                        + 4;
                    bounds = new Rectangle2D.Double(drawSpace.getX() + BORDER,
                        drawSpace.getY() + BORDER, bounds.getWidth() + 6,
                        height);
                    g2.setColor(Color.white);
                    g2.fill(bounds);
                    g2.setColor(Color.black);
                    g2.draw(bounds);
                    int yPosition = (int) (bounds.getY() + height / 2);
                    int yTime = (int) (bounds.getY() + height - 2);
                    int x = (int) (bounds.getX() + 4);
                    g2.drawString(position, x, (int) yPosition);
                    g2.drawString(time, x, yTime);
                }
            });
        return upperLeftCorner;
    }

    /**
     * This one if for creating GPS plots in servlets
     * 
     * @param locationsAndTimes
     * @param request
     * @param out
     * @param session
     */
    public void plotGpsData(String title, HttpServletRequest request,
        PrintWriter out, HttpSession session) {
        // Create a JFreeChart
        JFreeChart chart = null;
        try {
            chart = this.getGpsPlot(title);
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            out
                .println("<H2><center>No GPS Watch Plot Could be Created</center></H2>");
            out.flush();
        }
        try {
            if (chart != null) {
                chart.setBackgroundPaint(java.awt.Color.white);
                ChartRenderingInfo info = new ChartRenderingInfo(
                    new StandardEntityCollection());
                String filename = null;
                try {
                    filename = ServletUtilities.saveChartAsJPEG(chart, 800,
                        700, info, session);
                } catch (IOException e) {}
                out.println("<center><img src=\"" + request.getContextPath()
                    + "/DisplayChart?filename=" + filename
                    + "\" width=800 height=700 border=0></img></center>");
                out.flush();
            } else {
                out
                    .println("<H2><center>No GPS Watch Plot Could be Created</center></H2>");
                out.flush();
            }
        } catch (Throwable e) {
            logger.error("Throwable caught: " + e.getMessage());
            out
                .println("<H2><center>No GPS Watch Plot Could be Created</center></H2>");
            out.flush();
        }

    }

    /**
     * @return Returns the scaleOnlyToWatchCircle.
     */
    public boolean isScaleOnlyToWatchCircle() {
        return scaleOnlyToWatchCircle;
    }

    /**
     * @param scaleOnlyToWatchCircle
     *            The scaleOnlyToWatchCircle to set.
     */
    public void setScaleOnlyToWatchCircle(boolean scaleOnlyToWatchCircle) {
        this.scaleOnlyToWatchCircle = scaleOnlyToWatchCircle;
    }

    // Local variables

    /**
     * A log4j Logger
     */
    Logger logger = Logger.getLogger(GpsCharter.class);

    // These are the LocationAnTime objects that are the geospatial data
    private final LocationAndTime[] locationsAndTimes;

    // This is the LocationAndTime that shows where the anchor is (time is
    // ignored in this case)
    private final LocationAndTime anchorLocation;

    // This is the size of the watch circle in km
    private final double watchCircleSize;

    // This is a boolean that indicates whether or not the graph should only
    // show the watch circle or scale to include data outside the watch circle
    private boolean scaleOnlyToWatchCircle = false;

    // This is the default size of the anchor on the graph
    private final static int ANCHOR_SIZE = 30;

    // These are the two JFreeChart Axes that will be used in the graph
    private final NumberAxis longitudeAxis = new NumberAxis();
    private final NumberAxis latitudeAxis = new NumberAxis();

    public ColorBarBugFix colorBar = null;

    private double maxLongitude, minLongitude, maxLatitude, minLatitude;
    private long maxTime, minTime;

}