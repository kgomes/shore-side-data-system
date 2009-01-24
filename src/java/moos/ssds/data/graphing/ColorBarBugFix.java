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
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.ColorPalette;
import org.jfree.chart.plot.ContourPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.RainbowPalette;
import org.jfree.ui.RectangleEdge;

/**
 * TODO achase 20040618 please note: This class is for a color bar with
 * adjustable thickness. Unfortunately the original author of this class made
 * DEFAULT_COLORBAR_THICKNESS final, and made caclulateBarThickness a private
 * method. This means there's no way (that I can figure out) to adjust the
 * thickness of the color bar through inheritance. So, this is a complete cut
 * and paste of the original ColorBar class from the org.jfree.chart.axis class,
 * and all the standard warnings should apply to this class for this type of
 * situation. To vary the width of the color bar change the return value of
 * CalculateBarThickness. The bar thickness still adjusts a bit on resizing, but
 * I think it's good enough for what we're doing.
 * 
 * @author Andrew C. Chase
 * @originalAuthor David M. O'Donnell
 */
public class ColorBarBugFix implements Cloneable, Serializable {

    /** The default color bar thickness. */
    public static final int DEFAULT_COLORBAR_THICKNESS = 0;

    /** The default color bar thickness percentage. */
    public static final double DEFAULT_COLORBAR_THICKNESS_PERCENT = 0.10;

    /** The default outer gap. */
    public static final int DEFAULT_OUTERGAP = 2;

    /** The axis. */
    private ValueAxis axis;

    /** The color bar thickness. */
    private int colorBarThickness = DEFAULT_COLORBAR_THICKNESS;

    /** The color bar thickness as a percentage of the height of the data area. */
    private double colorBarThicknessPercent = DEFAULT_COLORBAR_THICKNESS_PERCENT;

    /** The color palette. */
    private ColorPalette colorPalette = null;

    /** The color bar length. */
    private int colorBarLength = 0; // default make height of plotArea

    /** The amount of blank space around the colorbar. */
    private int outerGap;

    /**
     * Constructs a horizontal colorbar axis, using default values where
     * necessary.
     * 
     * @param label
     *            the axis label.
     */
    public ColorBarBugFix(String label) {

        NumberAxis a = new NumberAxis(label);
        a.setAutoRangeIncludesZero(false);
        this.axis = a;
        this.axis.setLowerMargin(0.0);
        this.axis.setUpperMargin(0.0);
        this.axis.setLabelAngle(90);
        this.axis.setVerticalTickLabels(true);

        this.colorPalette = new RainbowPalette();
        this.colorBarThickness = DEFAULT_COLORBAR_THICKNESS;
        this.colorBarThicknessPercent = DEFAULT_COLORBAR_THICKNESS_PERCENT;
        this.outerGap = DEFAULT_OUTERGAP;
        this.colorPalette.setMinZ(this.axis.getRange().getLowerBound());
        this.colorPalette.setMaxZ(this.axis.getRange().getUpperBound());

    }

    /**
     * Configures the color bar.
     * 
     * @param plot
     *            the plot.
     */
    public void configure(ContourPlot plot) {
        double minZ = plot.getDataset().getMinZValue();
        double maxZ = plot.getDataset().getMaxZValue();
        setMinimumValue(minZ);
        setMaximumValue(maxZ);
    }

    /**
     * Returns the axis.
     * 
     * @return The axis.
     */
    public ValueAxis getAxis() {
        return this.axis;
    }

    /**
     * Sets the axis.
     * 
     * @param axis
     *            the axis.
     */
    public void setAxis(ValueAxis axis) {
        this.axis = axis;
    }

    /**
     * Rescales the axis to ensure that all data are visible.
     */
    public void autoAdjustRange() {
        // TODO I had to comment out this method because it is package protected
        // this.axis.autoAdjustRange();
        this.colorPalette.setMinZ(this.axis.getLowerBound());
        this.colorPalette.setMaxZ(this.axis.getUpperBound());
    }

    /**
     * Draws the plot on a Java 2D graphics device (such as the screen or a
     * printer).
     * 
     * @param g2
     *            the graphics device.
     * @param cursor
     *            the cursor.
     * @param plotArea
     *            the area within which the chart should be drawn.
     * @param dataArea
     *            the area within which the plot should be drawn (a subset of
     *            the drawArea).
     * @param reservedArea
     *            the reserved area.
     * @param edge
     *            the color bar location.
     * @return The new cursor location.
     */
    public double draw(Graphics2D g2, double cursor, Rectangle2D plotArea,
        Rectangle2D dataArea, Rectangle2D reservedArea, RectangleEdge edge) {

        Rectangle2D colorBarArea = null;

        double thickness = calculateBarThickness(dataArea, edge);
        if (this.colorBarThickness > 0) {
            thickness = this.colorBarThickness; // allow fixed thickness
        }

        double length = 0.0;
        if (RectangleEdge.isLeftOrRight(edge)) {
            length = dataArea.getHeight();
        } else {
            length = dataArea.getWidth();
        }

        if (this.colorBarLength > 0) {
            length = this.colorBarLength;
        }

        if (edge == RectangleEdge.BOTTOM) {
            colorBarArea = new Rectangle2D.Double(dataArea.getX(), plotArea
                .getMaxY()
                + this.outerGap, length, thickness);
        } else if (edge == RectangleEdge.TOP) {
            colorBarArea = new Rectangle2D.Double(dataArea.getX(), reservedArea
                .getMinY()
                + this.outerGap, length, thickness);
        } else if (edge == RectangleEdge.LEFT) {
            colorBarArea = new Rectangle2D.Double(plotArea.getX() - thickness
                - this.outerGap, dataArea.getMinY(), thickness, length);
        } else if (edge == RectangleEdge.RIGHT) {
            colorBarArea = new Rectangle2D.Double(plotArea.getMaxX()
                + this.outerGap, dataArea.getMinY(), thickness, length);
        }

        // update, but dont draw tick marks (needed for stepped colors)
        // this.axis.refreshTicks(g2, new AxisState(), plotArea, colorBarArea,
        // edge);
        this.axis.refreshTicks(g2, new AxisState(), plotArea, 
            edge);

        drawColorBar(g2, colorBarArea, edge);

        AxisState state = null;
        if (edge == RectangleEdge.TOP) {
            cursor = colorBarArea.getMinY();
            state = this.axis.draw(g2, cursor, reservedArea, colorBarArea,
                RectangleEdge.TOP, null);
        } else if (edge == RectangleEdge.BOTTOM) {
            cursor = colorBarArea.getMaxY();
            state = this.axis.draw(g2, cursor, reservedArea, colorBarArea,
                RectangleEdge.BOTTOM, null);
        } else if (edge == RectangleEdge.LEFT) {
            cursor = colorBarArea.getMinX();
            state = this.axis.draw(g2, cursor, reservedArea, colorBarArea,
                RectangleEdge.LEFT, null);
        } else if (edge == RectangleEdge.RIGHT) {
            cursor = colorBarArea.getMaxX();
            state = this.axis.draw(g2, cursor, reservedArea, colorBarArea,
                RectangleEdge.RIGHT, null);
        }
        return state.getCursor();

    }

    /**
     * Draws the plot on a Java 2D graphics device (such as the screen or a
     * printer).
     * 
     * @param g2
     *            the graphics device.
     * @param colorBarArea
     *            the area within which the axis should be drawn.
     * @param edge
     *            the location.
     */
    public void drawColorBar(Graphics2D g2, Rectangle2D colorBarArea,
        RectangleEdge edge) {

        Object antiAlias = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_OFF);

        // setTickValues was missing from ColorPalette v. 0.96
        // colorPalette.setTickValues(this.axis.getTicks());

        Stroke strokeSaved = g2.getStroke();
        g2.setStroke(new BasicStroke(1.0f));

        if (RectangleEdge.isTopOrBottom(edge)) {
            double y1 = colorBarArea.getY();
            double y2 = colorBarArea.getMaxY();
            double xx = colorBarArea.getX();
            Line2D line = new Line2D.Double();
            while (xx <= colorBarArea.getMaxX()) {
                double value = this.axis.java2DToValue(xx, colorBarArea, edge);
                line.setLine(xx, y1, xx, y2);
                g2.setPaint(getPaint(value));
                g2.draw(line);
                xx += 1;
            }
        } else {
            double y1 = colorBarArea.getX();
            double y2 = colorBarArea.getMaxX();
            double xx = colorBarArea.getY();
            Line2D line = new Line2D.Double();
            while (xx <= colorBarArea.getMaxY()) {
                double value = this.axis.java2DToValue(xx, colorBarArea, edge);
                line.setLine(y1, xx, y2, xx);
                g2.setPaint(getPaint(value));
                g2.draw(line);
                xx += 1;
            }
        }

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias);
        g2.setStroke(strokeSaved);

    }

    /**
     * Returns the color palette.
     * 
     * @return the color palette.
     */
    public ColorPalette getColorPalette() {
        return this.colorPalette;
    }

    /**
     * Returns the Paint associated with a value.
     * 
     * @param value
     *            the value.
     * @return the paint.
     */
    public Paint getPaint(double value) {
        return this.colorPalette.getPaint(value);
    }

    /**
     * Sets the color palette.
     * 
     * @param palette
     *            the new palette.
     */
    public void setColorPalette(ColorPalette palette) {
        this.colorPalette = palette;
    }

    /**
     * Sets the maximum value.
     * 
     * @param value
     *            the maximum value.
     */
    public void setMaximumValue(double value) {
        this.colorPalette.setMaxZ(value);
        this.axis.setUpperBound(value);
    }

    /**
     * Sets the minimum value.
     * 
     * @param value
     *            the minimum value.
     */
    public void setMinimumValue(double value) {
        this.colorPalette.setMinZ(value);
        this.axis.setLowerBound(value);
    }

    /**
     * Reserves the space required to draw the color bar.
     * 
     * @param g2
     *            the graphics device.
     * @param plot
     *            the plot that the axis belongs to.
     * @param plotArea
     *            the area within which the plot should be drawn.
     * @param dataArea
     *            the data area.
     * @param edge
     *            the axis location.
     * @param space
     *            the space already reserved.
     * @return The space required to draw the axis in the specified plot area.
     */
    public AxisSpace reserveSpace(Graphics2D g2, Plot plot,
        Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge,
        AxisSpace space) {

        AxisSpace result = this.axis.reserveSpace(g2, plot, plotArea, edge,
            space);

        double thickness = calculateBarThickness(dataArea, edge);
        result.add(thickness + 2 * this.outerGap, edge);
        return result;

    }

    /**
     * Calculates the bar thickness.
     * 
     * @param plotArea
     *            the plot area.
     * @param edge
     *            the location.
     * @return The thickness.
     */
    private double calculateBarThickness(Rectangle2D plotArea,
        RectangleEdge edge) {
        /*
         * this is the original jfreechart code double result = 0.0; if
         * (RectangleEdge.isLeftOrRight(edge)) { result = plotArea.getWidth() *
         * this.colorBarThicknessPercent; } else { result = plotArea.getHeight() *
         * this.colorBarThicknessPercent; } return result;
         */
        return 20.0;
    }

    /**
     * Returns a clone of the object.
     * 
     * @return A clone.
     * @throws CloneNotSupportedException
     *             if some component of the color bar does not support cloning.
     */
    public Object clone() throws CloneNotSupportedException {

        ColorBarBugFix clone = (ColorBarBugFix) super.clone();

        clone.axis = (ValueAxis) this.axis.clone();

        return clone;

    }

    /**
     * Tests this object for equality with another.
     * 
     * @param obj
     *            the object to test against.
     * @return A boolean.
     */
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof ColorBarBugFix) {
            ColorBarBugFix cb = (ColorBarBugFix) obj;
            boolean b0 = this.axis.equals(cb.axis);
            boolean b1 = this.colorBarThickness == cb.colorBarThickness;
            boolean b2 = this.colorBarThicknessPercent == cb.colorBarThicknessPercent;
            boolean b3 = this.colorPalette.equals(cb.colorPalette);
            boolean b4 = this.colorBarLength == cb.colorBarLength;
            boolean b5 = this.outerGap == cb.outerGap;
            return b0 && b1 && b2 && b3 && b4 && b5;
        }

        return false;

    }

}