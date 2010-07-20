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

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

/**
 * @author achase
 */
public class XYPlotWithColorBar extends XYPlot {

    private ColorBarBugFix colorBar;
    private RectangleEdge colorBarLocation = RectangleEdge.LEFT;

    /**
     * @param gpsData
     * @param longitudeAxis
     * @param latitudeAxis
     * @param renderer
     */
    public XYPlotWithColorBar(XYDataset gpsData, NumberAxis longitudeAxis,
        NumberAxis latitudeAxis, AbstractXYItemRenderer renderer) {
        super(gpsData, longitudeAxis, latitudeAxis, renderer);
    }

    public void draw(Graphics2D g2, Rectangle2D plotArea, Point2D anchor,
        PlotState parentState, PlotRenderingInfo info) {
        AxisSpace space = new AxisSpace();

        space = this.getDomainAxis().reserveSpace(g2, this, plotArea,
            RectangleEdge.BOTTOM, space);
        space = this.getRangeAxis().reserveSpace(g2, this, plotArea,
            RectangleEdge.LEFT, space);

        Rectangle2D estimatedDataArea = space.shrink(plotArea, null);

        AxisSpace space2 = new AxisSpace();
        space2 = this.colorBar.reserveSpace(g2, this, plotArea,
            estimatedDataArea, this.colorBarLocation, space2);
        Rectangle2D adjustedPlotArea = space2.shrink(plotArea, null);

        Rectangle2D dataArea = space.shrink(adjustedPlotArea, null);

        Rectangle2D colorBarArea = space2.reserved(plotArea,
            this.colorBarLocation);
        if (this.colorBar != null) {
            this.colorBar.draw(g2, 0, adjustedPlotArea, dataArea, colorBarArea,
                this.colorBarLocation);
        }
        super.draw(g2, adjustedPlotArea, anchor, parentState, info);
    }

    /**
     * @return Returns the colorBar.
     */
    public ColorBarBugFix getColorBar() {
        return colorBar;
    }

    /**
     * @param colorBar
     *            The colorBar to set.
     */
    public void setColorBar(ColorBarBugFix colorBar) {
        this.colorBar = colorBar;
    }
}