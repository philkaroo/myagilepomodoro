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
package org.mypomodoro.gui;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import org.mypomodoro.model.AbstractActivities;

public interface IListPanel {

    void refresh();

    void refresh(boolean fromDatabase);

    AbstractTableModel getNewTableModel();

    AbstractActivities getList();

    void initTabbedPane();

    AbstractTable getMainTable();

    AbstractTable getCurrentTable();

    AbstractTable getSubTable();

    void setCurrentTable(AbstractTable table);

    JSplitPane getSplitPane();

    JPanel getListPane();

    JScrollPane getSubTableScrollPane();

    void addTableTitlePanel();

    void addTable();

    void addSubTableTitlePanel();

    JScrollPane getTableScrollPane();

    TitlePanel getTableTitlePanel();

    SubTableTitlePanel getSubTableTitlePanel();

    TabbedPane getTabbedPane();

    void populateSubTable(int activityId);

    void emptySubTable();
}
