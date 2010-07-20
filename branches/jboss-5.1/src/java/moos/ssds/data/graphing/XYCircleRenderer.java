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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;

import moos.ssds.data.util.LocationAndTime;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.ColorPalette;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

/**
 * @author achase
 */
public class XYCircleRenderer extends AbstractXYItemRenderer {

    private final int numberOfPoints;
    private float hue = .75f; // starting point for the color of the circle
    private float hueDecrement = .001f;
    private ColorPalette colorPalette;

    /**
     * Constructs a new renderer.
     */
    public XYCircleRenderer(int numberOfPoints) {
        super();
        this.numberOfPoints = numberOfPoints;
        // decrement the hue so that the last point will have a value of 0
        hueDecrement = (float) (hue / numberOfPoints);
        initializeColorPalette();
    }

    private void initializeColorPalette() {
        final int[] red = new int[numberOfPoints];
        final int[] green = new int[numberOfPoints];
        final int[] blue = new int[numberOfPoints];
        Color tempColor;
        for (int i = 0; i < numberOfPoints; i++) {
            hue -= hueDecrement;
            tempColor = Color.getHSBColor(hue, 1, 1);
            red[i] = tempColor.getRed();
            green[i] = tempColor.getGreen();
            blue[i] = tempColor.getBlue();
        }
        colorPalette = new ColorPalette() {

            public void initialize() {
                this.r = red;
                this.g = green;
                this.b = blue;
            }

            public Color getColorLinear(double value) {
                double difference = value - this.minZ;
                double divided = difference
                    / ((this.maxZ - this.minZ) / numberOfPoints);
                int newValue = (int) (divided);
                if (newValue != 0) {
                    newValue = newValue - 1;
                }
                return new Color(this.r[newValue], this.g[newValue],
                    this.b[newValue]);
            }
        };
        colorPalette.initialize();
    }

    public Paint getItemPaint(int row, int column) {
        return colorPalette.getColor(column);
    }

    /**
     * Draws the visual representation of a single data item.
     * 
     * @param g2
     *            the graphics device.
     * @param state
     *            the renderer state.
     * @param dataArea
     *            the area within which the data is being drawn.
     * @param info
     *            collects information about the drawing.
     * @param plot
     *            the plot (can be used to obtain standard color information
     *            etc).
     * @param domainAxis
     *            the domain (horizontal) axis.
     * @param rangeAxis
     *            the range (vertical) axis.
     * @param dataset
     *            the dataset.
     * @param series
     *            the series index (zero-based).
     * @param item
     *            the item index (zero-based).
     * @param crosshairState
     *            crosshair information for the plot (<code>null</code>
     *            permitted).
     * @param pass
     *            the pass index.
     */
    public void drawItem(Graphics2D g2, XYItemRendererState state,
        Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
        ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
        int series, int item, CrosshairState crosshairState, int pass) {

        // get the data point...
        Number xn = dataset.getX(series, item);
        Number yn = dataset.getY(series, item);
        if (yn != null) {
            double x = xn.doubleValue();
            double y = yn.doubleValue();
            RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
            RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
            double transX = domainAxis
                .valueToJava2D(x, dataArea, xAxisLocation);
            double transY = rangeAxis.valueToJava2D(y, dataArea, yAxisLocation);

            g2.setPaint(this.getItemPaint(series, item));
            PlotOrientation orientation = plot.getOrientation();
            if (orientation == PlotOrientation.HORIZONTAL) {
                g2.fillOval((int) transY, (int) transX, 6, 6);
            } else if (orientation == PlotOrientation.VERTICAL) {
                g2.fillOval((int) transX, (int) transY, 6, 6);
            }

            updateCrosshairValues(crosshairState, x, y, transX, transY,
                orientation);
        }

    }

    protected ColorBarBugFix getTimeBar(LocationAndTime[] locationsAndTimes) {

        // Create the time bar
        ColorBarBugFix timeBar = new ColorBarBugFix("Time");

        // If not data, just return it
        if ((locationsAndTimes == null) || (locationsAndTimes.length <= 0))
            return timeBar;

        DateAxis dateAxis = new DateAxis();
        dateAxis.setTickUnit(new DateTickUnit(DateTickUnit.DAY, 1, DateFormat
            .getDateInstance(DateFormat.FULL)));
        dateAxis.setRange(locationsAndTimes[0].getEpochSeconds().doubleValue(),
            locationsAndTimes[locationsAndTimes.length - 1].getEpochSeconds()
                .doubleValue());
        dateAxis.setDateFormatOverride(DateFormat
            .getDateInstance(DateFormat.MEDIUM));

        timeBar.setColorPalette(colorPalette);
        colorPalette.setMinZ(locationsAndTimes[0].getEpochSeconds()
            .doubleValue());
        colorPalette.setMaxZ(locationsAndTimes[locationsAndTimes.length - 1]
            .getEpochSeconds().doubleValue());
        timeBar.setAxis(dateAxis);

        return timeBar;
    }

    public static void main(String[] args) {}
}