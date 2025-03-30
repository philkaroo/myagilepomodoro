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
import java.util.HashSet;
import java.util.List;
import org.mypomodoro.Main;
import org.mypomodoro.db.ActivitiesDAO;

/**
 * List of types
 *
 */
public class TaskTypeList extends AbstractList {

    private static ArrayList<String> types = new ArrayList<String>();

    public static void refresh() {
        types = ActivitiesDAO.getInstance().getTypes();
        if (Main.preferences.getAgileMode()) {
            types.add("User Story");
            types.add("Epic");
            types.add("Defect");
            types.add("Impediment");
            types.add("Bug");
            types.add("Duplicate");
            types.add("Spike");
            types.add("Change request");
            types = new ArrayList<String>(new HashSet<String>(types)); // remove duplicates
        }
    }

    public static List<String> getTypes() {
        if (types.size() > 1) {
            Collections.sort(types, new SortIgnoreCase());
        }
        return types;
    }

    public static void addType(String type) {
        if (type.trim().length() > 0 && !types.contains(type.trim())) {
            types.add(type.trim());
        }
    }
}
