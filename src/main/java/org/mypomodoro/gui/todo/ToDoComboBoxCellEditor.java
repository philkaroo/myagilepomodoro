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
package org.mypomodoro.gui.todo;

import org.mypomodoro.gui.activities.*;
import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.mypomodoro.Main;
import org.mypomodoro.model.Activity;

/**
 * Combo Box Cell Editor
 *
 */
class ToDoComboBoxCellEditor extends ComboBoxCellEditor {

    public <E> ToDoComboBoxCellEditor(E[] data, boolean editable) {
        super(data, editable);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        super.getTableCellEditorComponent(table, value, isSelected, row, column);
        Activity activity = ((ToDoTable) table).getActivityFromRowIndex(row);
        if (activity != null && activity.isFinished()) {
            comboBox.getEditor().getEditorComponent().setForeground(Main.taskFinishedColor); // editable combo box
            comboBox.setForeground(Main.taskFinishedColor);
            //label.setForeground(Main.taskFinishedColor);
        } else if (activity != null
                && Main.gui.getToDoPanel().getPomodoro().getCurrentToDo() != null
                && (activity.getId() == Main.gui.getToDoPanel().getPomodoro().getCurrentToDo().getId()
                || (activity.isTask() && activity.getId() == Main.gui.getToDoPanel().getPomodoro().getCurrentToDo().getParentId()))
                && Main.gui.getToDoPanel().getPomodoro().inPomodoro()) {
            comboBox.getEditor().getEditorComponent().setForeground(Main.taskRunningColor); // editable combo box
            comboBox.setForeground(Main.taskRunningColor);
            //label.setForeground(Main.taskRunningColor);
        } else { // reset foreground (depends on the theme)
            comboBox.getEditor().getEditorComponent().setForeground(new JComboBox().getForeground()); // editable combo box
            comboBox.setForeground(new JComboBox().getForeground());
            //label.setForeground(getForeground());
        }
        ((JTextField) comboBox.getEditor().getEditorComponent()).setCaretColor(new JTextField().getForeground()); // Set colors according to input settings and themes
        return this;
    }
}
