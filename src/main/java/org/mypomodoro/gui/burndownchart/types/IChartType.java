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
import org.mypomodoro.model.Activity;

/**
 * Interface for types of Chart data
 *
 */
public interface IChartType {

    String getYLegend();

    String getXLegend();

    float getValue(Activity activity, Date date);

    float getTotalForBurndown();

    float getTotalForBurnup();

    ArrayList<Float> getSumDateRangeForScope(ArrayList<Date> dates, boolean subtasks);

    ArrayList<Float> getSumIterationRangeForScope(int startIteration, int endIteration);

    @Override
    String toString();
}
