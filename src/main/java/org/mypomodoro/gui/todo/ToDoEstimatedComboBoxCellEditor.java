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

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JTable;
import org.mypomodoro.Main;
import org.mypomodoro.model.Activity;
import org.mypomodoro.model.ToDoList;

/**
 *
 *
 */
class ToDoEstimatedComboBoxCellEditor extends ToDoComboBoxCellEditor {

    public <E> ToDoEstimatedComboBoxCellEditor(E[] data, boolean editable) {
        super(data, editable);

        // Custom display hovered item value
        comboBox.setRenderer(new ComboBoxEstimatedLengthRenderer());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        super.getTableCellEditorComponent(table, value, isSelected, row, column);
        Activity activity = ((ToDoTable) table).getActivityFromRowIndex(row);
        if (activity != null) {
            int realpoms = activity.getActualPoms();
            int estimatedpoms = activity.getEstimatedPoms();
            // no change to the label set by the cell renderer
            if (realpoms > 0
                    || (Main.gui != null
                    && Main.gui.getToDoPanel().getPomodoro().inPomodoro()
                    && Main.gui.getToDoPanel().getPomodoro().getCurrentToDo() != null
                    && activity.getId() == Main.gui.getToDoPanel().getPomodoro().getCurrentToDo().getId())
                    || (activity.isCompleted() && activity.isSubTask())
                    || (activity.isDoneDone() && activity.isTask() && Main.preferences.getAgileMode())) {
                comboBox.setVisible(false);
            } else if (realpoms == 0) { // can't edit when task already started (real > 0)
                int minimum = 0; // no matter overestimation
                int maximum = estimatedpoms + Main.preferences.getMaxNbPomPerActivity();
                comboBox.setVisible(true);
                comboBox.removeAllItems();
                if (activity.isTask()) {
                    ArrayList<Activity> subList = ToDoList.getList().getSubTasks(activity.getId());
                    for (Activity act : subList) {
                        minimum += act.getEstimatedPoms();
                    }
                }
                for (int i = minimum; i <= maximum; i++) {
                    comboBox.addItem(i);
                }
                comboBox.setSelectedItem(activity.getEstimatedPoms());
            }
        }
        return this;
    }
}
