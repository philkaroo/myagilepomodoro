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

import java.util.Date;
import org.mypomodoro.util.DateUtil;

/**
 * Comparable object for rendering dates as days of month on X Axis
 *
 */
public class ComparableCustomDateForXAxis implements Comparable<ComparableCustomDateForXAxis> {

    private final Date date;
    private final boolean display;

    ComparableCustomDateForXAxis(Date date) {
        this.date = date;
        this.display = true;
    }

    ComparableCustomDateForXAxis(Date date, boolean display) {
        this.date = date;
        this.display = display;
    }

    @Override
    public int compareTo(ComparableCustomDateForXAxis key) {
        int compare;
        if (getDate() == null && key.getDate() == null) {
            compare = 0;
        } else {
            compare = getDate().compareTo(key.getDate());
        }
        return compare;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return compareTo((ComparableCustomDateForXAxis) obj) == 0;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.date != null ? this.date.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        //return DateUtil.getFormatedDate(getDate(), "dd MMM."); // before 4.0.0 format eg '15 Jun.'
        return display ? DateUtil.getShortFormatedDateNoYear(getDate()) : "";
    }

    private Date getDate() {
        return date;
    }
}
