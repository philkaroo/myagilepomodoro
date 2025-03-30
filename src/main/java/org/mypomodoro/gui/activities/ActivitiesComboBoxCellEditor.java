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
import javax.swing.JTextField;
import org.mypomodoro.Main;
import org.mypomodoro.model.Activity;
import org.mypomodoro.model.ActivityList;

/**
 * Combo Box Cell Editor
 *
 */
class ActivitiesComboBoxCellEditor extends ComboBoxCellEditor {

    public <E> ActivitiesComboBoxCellEditor(E[] data, boolean editable) {
        super(data, editable);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        super.getTableCellEditorComponent(table, value, isSelected, row, column);
        int id = (Integer) table.getModel().getValueAt(table.convertRowIndexToModel(row), table.getModel().getColumnCount() - 1);
        Activity activity = ActivityList.getList().getById(id);
        if (activity != null && activity.isCompleted() && activity.isSubTask()) {
            comboBox.setVisible(false);
        } else {
            comboBox.setVisible(true);
        }
        if (activity != null && activity.isFinished()) {
            comboBox.getEditor().getEditorComponent().setForeground(Main.taskFinishedColor); // editable combo box
            comboBox.setForeground(Main.taskFinishedColor);
            //label.setForeground(Main.taskFinishedColor);
        } else { // reset foreground (depends on the theme)
            comboBox.getEditor().getEditorComponent().setForeground(new JComboBox().getForeground()); // editable combo box
            comboBox.setForeground(new JComboBox().getForeground());
            //label.setForeground(getForeground());
        }
        ((JTextField) comboBox.getEditor().getEditorComponent()).setCaretColor(new JTextField().getForeground()); // Set colors according to input settings and themes
        return this;
    }
}
