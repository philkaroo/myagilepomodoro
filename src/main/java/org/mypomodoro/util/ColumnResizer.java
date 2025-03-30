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
package org.mypomodoro.util;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Column resizer Get max width for cells in column and make that the preferred
 *
 */
public class ColumnResizer {

    public static void adjustColumnPreferredWidths(JTable table) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int col = 0; col < table.getColumnCount(); col++) {
            TableColumn column = columnModel.getColumn(col);
            if (column.getPreferredWidth() != 0) { // not hidden
                int maxwidth = 0;
                for (int row = 0; row < table.getModel().getRowCount(); row++) {
                    TableCellRenderer rend = table.getCellRenderer(row, col);
                    Object value = table.getValueAt(table.convertRowIndexToModel(row), col);
                    Component comp = rend.getTableCellRendererComponent(table,
                            value, false, false, row, col);
                    maxwidth = Math.max(comp.getPreferredSize().width, maxwidth);
                }
                column.setPreferredWidth(maxwidth);
            }
        }
    }
}
