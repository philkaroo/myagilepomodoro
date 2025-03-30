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
 * List of authors of activities and reports
 *
 */
public class AuthorList extends AbstractList {

    private static List<String> authors = new ArrayList<String>();

    public static void refresh() {
        authors = ActivitiesDAO.getInstance().getAuthors();
    }

    public static List<String> getAuthors() {
        if (authors.size() > 1) {
            Collections.sort(authors, new SortIgnoreCase());
        }
        return authors;
    }

    public static void addAuthor(String author) {
        if (author.trim().length() > 0 && !authors.contains(author)) {
            authors.add(author);
        }
    }
}
