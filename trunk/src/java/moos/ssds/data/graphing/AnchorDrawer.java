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
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;

import org.jfree.ui.Drawable;

/**
 * @author achase
 * 
 *  
 */
public class AnchorDrawer implements Drawable {

    /*
     * (non-Javadoc)
     * 
     * @see org.jfree.ui.Drawable#draw(java.awt.Graphics2D,
     *      java.awt.geom.Rectangle2D)
     */
    public void draw(Graphics2D g2d, Rectangle2D rectangle) {
        Image anchor = Toolkit
                .getDefaultToolkit()
                .getImage(
                        AnchorDrawer.class
                                .getResource("/moos/ssds/data/graphing/anchor.gif"));

        //TODO achase 20040528 I'm not sure what the implications of this while loop
        //are, it seems safe enough to me.
        //while g2d.drawImage returns false, the image has not finished rendering
        while(!g2d.drawImage(anchor, (int) rectangle.getMinX(), (int) rectangle
                .getMinY(), (int) rectangle.getWidth(), (int) rectangle
                .getHeight(), null));
      
    }

    public static void main(String[] args) {
    }
}