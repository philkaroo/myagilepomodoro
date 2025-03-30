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
package org.mypomodoro.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.swing.JTextField;
import org.jdesktop.swingx.JXDatePicker;

/**
 * Custom date picker
 *
 */
public class DatePicker extends JXDatePicker {

    private String editorText;

    public DatePicker(Locale locale) {
        super(new Date(), locale);
        getEditor().setEditable(false);
        getEditor().setBackground(new JTextField().getBackground()); // set the background of the current theme
        getEditor().setForeground(new JTextField().getForeground()); // set the background of the current theme
    }

    // Workaround to prevent from having empty text field when selecting a date outside of boundaries 
    @Override
    public void setDate(Date date) {
        super.setDate(date);
        if (!getEditor().getText().isEmpty()) {
            editorText = getEditor().getText(); // record the last selected date
        } else {
            getEditor().setText(editorText); // set the last selected date instead of empty field
        }
    }

    public void setDateWithLowerBounds(Date date) {
        Calendar calendar = getMonthView().getCalendar();
        calendar.setTime(date);
        getMonthView().setLowerBound(calendar.getTime());
    }

    public void setDateWithUpperBounds(Date date) {
        Calendar calendar = getMonthView().getCalendar();
        calendar.setTime(date);
        getMonthView().setUpperBound(calendar.getTime());
    }

    public void setTodayWithLowerBounds() {
        setDateWithLowerBounds(new Date());
    }

    public void setTodayWithUpperBounds() {
        setDateWithUpperBounds(new Date());
    }

    public void setEmptyTodayWithLowerBounds() {
        getMonthView().setLowerBound(new Date());
        getEditor().setText("");
    }
}
