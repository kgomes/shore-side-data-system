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
package org.mbari.util;

import javax.swing.JScrollPane;

/**
 * This class is intended to hold a series of static decorator
 * methods which add helpful behaviors to Swing Components.
 * 
 * @author achase
 */
public class SwingDecorators {
    
    /** The distance scrolled when the scroll bar arrows are used. **/
    public static int SCROLLBAR_INCREMENT = 10;

    /**
     * Fix the unit increment in the <tt>JScrollBar</tt> so that it does
     * not creep along at such a slow pace when the user  uses the scrollbar
     * arrows to move the bar.
     * 
     * @param pane The JScrollPane to fix
     * @return The fixed JScrollPane
     */
    public static JScrollPane decJScrollPane(JScrollPane pane){
        pane.getVerticalScrollBar().setUnitIncrement(SCROLLBAR_INCREMENT);
        return pane;
    }
}
