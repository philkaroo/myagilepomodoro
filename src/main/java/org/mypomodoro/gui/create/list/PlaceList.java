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
package org.mypomodoro.gui.create.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.mypomodoro.db.ActivitiesDAO;

/**
 * List of places of activities and reports
 *
 */
public class PlaceList extends AbstractList {

    private static List<String> places = new ArrayList<String>();

    public static void refresh() {
        places = ActivitiesDAO.getInstance().getPlaces();
    }

    public static List<String> getPlaces() {
        if (places.size() > 1) {
            Collections.sort(places, new SortIgnoreCase());
        }
        return places;
    }

    public static void addPlace(String place) {
        if (place.trim().length() > 0 && !places.contains(place)) {
            places.add(place);
        }
    }
}
