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

import java.util.ArrayList;
import org.mypomodoro.model.Activity;
import org.mypomodoro.model.ActivityList;

/**
 * Table model for sub-activtiies
 *
 */
public class ActivitiesSubTableModel extends ActivitiesTableModel {

    public ActivitiesSubTableModel() {
        emptyModel();
    }

    public void update(int parentId) {
        ArrayList<Activity> subList = ActivityList.getList().getSubTasks(parentId);
        // we don't sort the list by priority here (although this would be convenient for reopened tasks)        
        setDataVector(subList);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == TITLE_COLUMN_INDEX || columnIndex == TYPE_COLUMN_INDEX || columnIndex == ESTIMATED_COLUMN_INDEX;
    }
}
