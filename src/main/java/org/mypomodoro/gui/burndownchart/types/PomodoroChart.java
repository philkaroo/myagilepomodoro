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
package org.mypomodoro.gui.burndownchart.types;

import java.util.ArrayList;
import java.util.Date;
import org.mypomodoro.db.ActivitiesDAO;
import org.mypomodoro.model.Activity;
import org.mypomodoro.model.ChartList;
import org.mypomodoro.util.DateUtil;
import org.mypomodoro.util.Labels;

/**
 * Pomodoro / Estimate chart type
 *
 * Chart based on real pomodoros
 *
 */
public class PomodoroChart implements IChartType {

    private final String label = Labels.getString("BurndownChartPanel.Pomodoros");

    @Override
    public String getYLegend() {
        return label;
    }

    @Override
    public String getXLegend() {
        return label;
    }

    // Date = null --> iteration
    @Override
    public float getValue(Activity activity, Date date) {
        boolean isComplete = (DateUtil.isEquals(activity.getDateCompleted(), date) || (!DateUtil.isEquals(activity.getDateCompleted(), new Date(0)) && date == null)) && activity.isCompleted();
        return isComplete ? activity.getActualPoms() : 0;
    }

    @Override
    public float getTotalForBurndown() {
        int total = 0;
        for (Activity activity : ChartList.getList().getTasks()) {
            total += activity.getEstimatedPoms() + activity.getOverestimatedPoms();
        }
        return new Float(total);
    }

    @Override
    public float getTotalForBurnup() {
        return getTotalForBurndown();
    }

    @Override
    public ArrayList<Float> getSumDateRangeForScope(ArrayList<Date> dates, boolean subtasks) {
        return ActivitiesDAO.getInstance().getSumOfPomodorosOfActivitiesDateRange(dates, subtasks);
    }

    @Override
    public ArrayList<Float> getSumIterationRangeForScope(int startIteration, int endIteration) {
        return ActivitiesDAO.getInstance().getSumOfPomodorosOfActivitiesIterationRange(startIteration, endIteration);
    }

    @Override
    public String toString() {
        return label;
    }
}
