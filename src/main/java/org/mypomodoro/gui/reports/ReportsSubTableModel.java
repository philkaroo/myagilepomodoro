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
package org.mypomodoro.gui.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.mypomodoro.model.Activity;
import org.mypomodoro.model.ReportList;

/**
 * Table model for sub-reports
 *
 */
public class ReportsSubTableModel extends ReportsTableModel {

    public ReportsSubTableModel() {
        emptyModel();
    }

    public void update(int parentId) {
        ArrayList<Activity> subList = ReportList.getList().getSubTasks(parentId);
        sortByPriority(subList); // sort by priority
        setDataVector(subList);
    }

    private void sortByPriority(ArrayList<Activity> activities) {
        Collections.sort(activities, new Comparator<Activity>() {

            @Override
            public int compare(Activity a1, Activity a2) {
                Integer p1 = (Integer) a1.getPriority();
                Integer p2 = (Integer) a2.getPriority();
                return p1.compareTo(p2);
            }
        });
    }
}
