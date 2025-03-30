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
import java.util.ArrayList;
import javax.swing.JTable;
import org.mypomodoro.Main;
import org.mypomodoro.model.Activity;
import org.mypomodoro.model.ActivityList;

/**
 *
 *
 */
class ActivitiesEstimatedComboBoxCellEditor extends ActivitiesComboBoxCellEditor {

    public <E> ActivitiesEstimatedComboBoxCellEditor(E[] data, boolean editable) {
        super(data, editable);

        // Custom display hovered item value
        comboBox.setRenderer(new ComboBoxEstimatedLengthRenderer());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        super.getTableCellEditorComponent(table, value, isSelected, row, column);
        int id = (Integer) table.getModel().getValueAt(table.convertRowIndexToModel(row), table.getModel().getColumnCount() - 1);
        Activity activity = ActivityList.getList().getById(id);
        if (activity != null) {
            int realPoms = activity.getActualPoms();
            if (realPoms > 0
                    || (activity.isCompleted() && activity.isSubTask())) {
                comboBox.setVisible(false);
            } else {
                int overestimatedPoms = activity.getOverestimatedPoms();
                int mimumEstimated = overestimatedPoms > 0 && realPoms >= overestimatedPoms ? realPoms - overestimatedPoms : realPoms;
                int estimated = 0;
                comboBox.setVisible(true);
                comboBox.removeAllItems();
                if (activity.isTask()) {
                    ArrayList<Activity> subList = ActivityList.getList().getSubTasks(activity.getId());
                    for (Activity act : subList) {
                        estimated += act.getEstimatedPoms();
                    }
                }
                int minimum = mimumEstimated > estimated ? mimumEstimated : estimated;
                int maximum = minimum + Main.preferences.getMaxNbPomPerActivity();
                for (int i = minimum; i <= maximum; i++) {
                    comboBox.addItem(i);
                }
                comboBox.setSelectedItem(activity.getEstimatedPoms());
            }
        }
        return this;
    }
}
