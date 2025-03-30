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

import org.mypomodoro.gui.create.ActivityInputForm;
import org.mypomodoro.model.Activity;

public class EditInputForm extends ActivityInputForm {

    public EditInputForm() {
        setBorder(null); // reset border set in ActivityInputForm
    }

    @Override
    protected void addForm(int gridy) {
        addDate(gridy);
        addAuthor(++gridy);
        addPlace(++gridy);
        addDescription(++gridy);
    }

    /**
     * Returns an updated activity from the class fields
     *
     * @return activity
     */
    @Override
    public Activity getActivityFromFields() {
        Activity activity = Activity.getActivity(activityId);
        activity.setDate(datePicker.getDate());
        String author = (String) authors.getSelectedItem();
        author = author != null ? author.trim() : "";
        activity.setAuthor(author);
        String place = (String) places.getSelectedItem();
        place = place != null ? place.trim() : "";
        activity.setPlace(place);
        activity.setDescription(descriptionField.getText().trim());
        return activity;
    }
}
