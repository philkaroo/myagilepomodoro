/*
 * Copyright (C)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mypomodoro.gui.burndownchart;

import java.awt.Color;
import java.awt.Paint;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.renderer.category.LayeredBarRenderer;

/**
 * Renderer with methods to highlight the plot
 *
 * Find at
 * http://juejin-cu.googlecode.com/svn/trunk/shop_total/Dev_lib/02_Resource/jfreechart-1.0.8-demo/source/demo/MouseOverDemo1.java
 * Sorry if the copyright of the demo has been infringed here
 *
 * Note: setDrawBarOutline must be set to 'true' for the highlight to work
 *
 */
public class CustomLayeredBarRenderer extends LayeredBarRenderer {

    /**
     * The row to highlight (-1 for none).
     */
    private int highlightRow = -1;

    /**
     * The column to highlight (-1 for none).
     */
    private int highlightColumn = -1;

    /**
     * Sets the item to be highlighted (use (-1, -1) for no highlight).
     *
     * @param r the row index.
     * @param c the column index.
     */
    public void setHighlightedItem(int r, int c) {
        if (this.highlightRow == r && this.highlightColumn == c) {
            return;  // nothing to do            
        }
        this.highlightRow = r;
        this.highlightColumn = c;
        notifyListeners(new RendererChangeEvent(this));
    }

    /**
     * Return a special colour for the highlighted item.
     *
     * @param row the row index.
     * @param column the column index.
     *
     * @return The outline paint.
     */
    @Override
    public Paint getItemOutlinePaint(int row, int column) {
        if (row == this.highlightRow && column == this.highlightColumn) {
            return Color.black;
        }
        return super.getItemOutlinePaint(row, column);
    }
}
