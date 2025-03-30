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
package org.mypomodoro.gui.activities;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JTable;
import org.mypomodoro.Main;
import org.mypomodoro.model.Activity;
import org.mypomodoro.model.ActivityList;
import org.mypomodoro.util.ColorUtil;

/**
 * Combo Box Cell Renderer
 *
 */
public class ActivitiesComboBoxCellRenderer extends ComboBoxCellRenderer {

    public <E> ActivitiesComboBoxCellRenderer(E[] data, boolean editable) {
        super(data, editable);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        int id = (Integer) table.getModel().getValueAt(table.convertRowIndexToModel(row), table.getModel().getColumnCount() - 1);
        Activity activity = ActivityList.getList().getById(id);
        if (activity != null && activity.isFinished()) {
            labelBefore.setForeground(Main.taskFinishedColor);
            comboBox.getEditor().getEditorComponent().setForeground(Main.taskFinishedColor); // editable combo box
            comboBox.setForeground(Main.taskFinishedColor);
            labelAfter.setForeground(Main.taskFinishedColor);
        } else { // reset foreground (depends on the theme)
            labelBefore.setForeground(ColorUtil.BLACK);
            comboBox.getEditor().getEditorComponent().setForeground(new JComboBox().getForeground()); // editable combo box
            comboBox.setForeground(new JComboBox().getForeground());
            labelAfter.setForeground(ColorUtil.BLACK); // we force color to be black (especially for JTatto Noire theme)
        }
        // Hide combobox and label when cell not editable 
        /*if (!table.getModel().isCellEditable(row, column)) {
         comboBox.setVisible(false);
         label.setVisible(false);
         }*/
        return this;
    }
}
