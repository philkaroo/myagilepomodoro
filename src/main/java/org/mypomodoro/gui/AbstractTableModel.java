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
package org.mypomodoro.gui;

import java.util.ArrayList;
import java.util.Date;
import javax.swing.table.DefaultTableModel;
import org.mypomodoro.model.Activity;
import org.mypomodoro.util.Labels;

/**
 * Table model for tasks and sub-tasks
 *
 */
public abstract class AbstractTableModel extends DefaultTableModel {

    public static final String[] COLUMN_NAMES = {
        Labels.getString("Common.Priority"),
        "U",
        Labels.getString("Common.Date"),
        Labels.getString("Common.Title"),
        Labels.getString("Common.Type"),
        Labels.getString("Common.Estimated"),
        Labels.getString("ReportListPanel.Diff I"),
        Labels.getString("ReportListPanel.Diff II"),
        Labels.getString("Agile.Common.Story Points"),
        Labels.getString("Agile.Common.Iteration"),
        "ID"};

    public static final int PRIORITY_COLUMN_INDEX = 0;
    public static final int UNPLANNED_COLUMN_INDEX = 1;
    public static final int DATE_COLUMN_INDEX = 2;
    public static final int TITLE_COLUMN_INDEX = 3;
    public static final int TYPE_COLUMN_INDEX = 4;
    public static final int ESTIMATED_COLUMN_INDEX = 5;
    public static final int DIFFI_COLUMN_INDEX = 6;
    public static final int DIFFII_COLUMN_INDEX = 7;
    public static final int STORYPOINTS_COLUMN_INDEX = 8;
    public static final int ITERATION_COLUMN_INDEX = 9;
    public static final int ACTIVITYID_COLUMN_INDEX = 10;

    // this is mandatory to get columns with integers properly sorted
    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case PRIORITY_COLUMN_INDEX:
                return Integer.class;
            case UNPLANNED_COLUMN_INDEX:
                return Boolean.class;
            case DATE_COLUMN_INDEX:
                return Date.class;
            case ESTIMATED_COLUMN_INDEX:
                return Integer.class;
            case DIFFI_COLUMN_INDEX:
                return Integer.class;
            case DIFFII_COLUMN_INDEX:
                return Integer.class;
            case STORYPOINTS_COLUMN_INDEX:
                return Float.class;
            case ITERATION_COLUMN_INDEX:
                return Integer.class;
            case ACTIVITYID_COLUMN_INDEX:
                return Integer.class;
            default:
                return String.class;
        }
    }

    public void setDataVector(final ArrayList<Activity> list) {
        int rowIndex = list.size();
        int colIndex = COLUMN_NAMES.length;
        Object[][] tableData = new Object[rowIndex][colIndex];
        for (int i = 0; i < list.size(); i++) {
            tableData[i] = getRow(list.get(i));
        }
        setDataVector(tableData, COLUMN_NAMES);
    }

    protected void emptyModel() {
        int rowIndex = 0;
        int colIndex = COLUMN_NAMES.length;
        Object[][] tableData = new Object[rowIndex][colIndex];
        setDataVector(tableData, COLUMN_NAMES);
    }

    public void addRow(Activity activity) {
        addRow(getRow(activity));
    }

    protected abstract Object[] getRow(Activity activity);
}
