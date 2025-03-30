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

import org.mypomodoro.gui.AbstractTableModel;
import org.mypomodoro.model.Activity;
import org.mypomodoro.model.ChartList;

/**
 * Table model for chart tasks
 *
 */
public class CheckTableModel extends AbstractTableModel {

    public CheckTableModel() {
        setDataVector(ChartList.getList().getTasks());
    }

    @Override
    protected Object[] getRow(Activity activity) {
        int colIndex = COLUMN_NAMES.length;
        Object[] rowData = new Object[colIndex];
        rowData[UNPLANNED_COLUMN_INDEX] = activity.isUnplanned();
        rowData[DATE_COLUMN_INDEX] = activity.getDateCompleted();
        rowData[TITLE_COLUMN_INDEX] = activity.getName();
        rowData[TYPE_COLUMN_INDEX] = activity.getType();
        Integer poms = activity.getActualPoms(); // Real poms !
        rowData[ESTIMATED_COLUMN_INDEX] = poms;
        Float points = activity.getStoryPoints();
        rowData[STORYPOINTS_COLUMN_INDEX] = points;
        Integer iteration = activity.getIteration();
        rowData[ITERATION_COLUMN_INDEX] = iteration;
        rowData[ACTIVITYID_COLUMN_INDEX] = activity.getId();
        return rowData;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
