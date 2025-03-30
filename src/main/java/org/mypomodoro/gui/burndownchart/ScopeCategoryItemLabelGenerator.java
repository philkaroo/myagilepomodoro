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

import java.util.ArrayList;
import java.util.Date;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;
import org.mypomodoro.util.DateUtil;

/**
 * Custom renderer for Scope line
 *
 */
public class ScopeCategoryItemLabelGenerator extends StandardCategoryItemLabelGenerator {

    private final ArrayList<Date> XAxisDateValues;

    public ScopeCategoryItemLabelGenerator(ArrayList<Date> list) {
        this.XAxisDateValues = list;
    }

    // Match the occurence of the scope values with the dates of the XAxis (see getXAxisDateValue in CreateChart)
    @Override
    public String generateLabel(CategoryDataset dataset, int row, int column) {
        String label = super.generateLabel(dataset, row, column);
        boolean displayDate = true;
        if (XAxisDateValues.size() > 0) {
            Date date = XAxisDateValues.get(column);
            if (column != 0
                    && column + 1 != XAxisDateValues.size()) { // first date always displayed            
                if (XAxisDateValues.size() > 100
                        && !DateUtil.isFirstDayOfMonth(date)) { // first condition
                    displayDate = false;
                } else if (XAxisDateValues.size() <= 100
                        && XAxisDateValues.size() > 10
                        && !DateUtil.isMonday(date)) { // second condition (cannot be mixed up with the first condition)
                    displayDate = false;
                }
            }
        }
        return displayDate ? label : "";
    }
}
