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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import org.mypomodoro.Main;
import org.mypomodoro.model.AbstractActivities;
import org.mypomodoro.util.WaitCursor;

/**
 *
 *
 */
public abstract class AbstractPanel extends JPanel implements IListPanel {

    // List pane: title + table + sub-title + sub-table
    protected final JPanel listPane = new JPanel();
    // Split pane: list pane + tabbed pane
    protected JSplitPane splitPane;
    // Title panes: title and sub-title    
    protected TitlePanel tableTitlePanel;
    protected SubTableTitlePanel subTableTitlePanel;
    // Table panes: table and sub-table
    protected JScrollPane tableScrollPane;
    protected JScrollPane subTableScrollPane;
    // Tabbed pane: details + ...
    protected TabbedPane tabbedPane;
    // Tables
    protected AbstractTable currentTable;
    protected AbstractTableModel tableModel;
    protected AbstractTable table;
    protected AbstractTableModel subTableModel;
    protected AbstractTable subTable;

    ////////////////////////////////////////////////
    // TITLE
    ////////////////////////////////////////////////
    @Override
    public void addTableTitlePanel() {
        table.setTitle(); // init title
        listPane.add(tableTitlePanel);
    }

    ////////////////////////////////////////////////
    // TABLE
    ////////////////////////////////////////////////
    @Override
    public void addTable() {
        listPane.add(tableScrollPane);
    }

    ////////////////////////////////////////////////
    // SUB TITLE
    ////////////////////////////////////////////////
    @Override
    public void addSubTableTitlePanel() {
        subTable.setTitle(); // init title
        listPane.add(subTableTitlePanel);
    }

    ////////////////////////////////////////////////
    // REFRESH
    ////////////////////////////////////////////////
    @Override
    public void refresh() {
        refresh(false);
    }

    @Override
    public void refresh(boolean fromDatabase) {
        if (!WaitCursor.isStarted()) {
            // Start wait cursor
            WaitCursor.startWaitCursor();
            try {
                if (fromDatabase) {
                    getList().refresh();
                }
                table.getModel().setDataVector(getList().getTasks());
                table.setTableHeader();
                table.setColumnModel();
                table.setTitle();
                table.initTabs();
                if (tableModel.getRowCount() > 0) {
                    table.setRowSelectionInterval(0, 0);
                } else {
                    emptySubTable();
                }
                table.setTitle();
                if (subTable != null) { // no subtable in CheckPanel
                    subTable.setTitle();
                }
            } catch (Exception ex) {
                Main.logger.error("", ex);
            } finally {
                // Stop wait cursor
                WaitCursor.stopWaitCursor();
            }
        }
    }

    @Override
    public abstract AbstractTableModel getNewTableModel();

    @Override
    public abstract AbstractActivities getList();

    @Override
    public abstract void initTabbedPane();

    @Override
    public abstract AbstractTable getMainTable();

    @Override
    public abstract AbstractTable getCurrentTable();

    @Override
    public abstract AbstractTable getSubTable();

    @Override
    public abstract void setCurrentTable(AbstractTable table);

    @Override
    public TabbedPane getTabbedPane() {
        return tabbedPane;
    }

    @Override
    public JPanel getListPane() {
        return listPane;
    }

    @Override
    public JSplitPane getSplitPane() {
        return splitPane;
    }

    @Override
    public TitlePanel getTableTitlePanel() {
        return tableTitlePanel;
    }

    @Override
    public SubTableTitlePanel getSubTableTitlePanel() {
        return subTableTitlePanel;
    }

    @Override
    public JScrollPane getTableScrollPane() {
        return tableScrollPane;
    }

    @Override
    public JScrollPane getSubTableScrollPane() {
        return subTableScrollPane;
    }

    @Override
    public abstract void populateSubTable(int activityId);

    @Override
    public void emptySubTable() {
        subTableModel.setRowCount(0);
        subTable.setColumnModel();
        subTable.setTitle();
    }

    protected void addTabbedPaneKeyStrokes() {
        // Activate Control Tab 1, 2 ...
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        for (int i = 1; i <= tabbedPane.getTabCount(); i++) {
            im.put(KeyStroke.getKeyStroke(getKeyEvent(i), KeyEvent.CTRL_DOWN_MASK), "Tab" + i);
            am.put("Tab" + i, new tabAction(i - 1));
        }
    }

    // Keystroke for tab
    class tabAction extends AbstractAction {

        final int index;

        public tabAction(int index) {
            this.index = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.isEnabledAt(index)) {
                tabbedPane.setSelectedIndex(index);
                tabbedPane.setSelectedIndexOnCustomMouseAdapter(index); // make sure the selected index for the adapter is up to date
            }
        }
    }

    private int getKeyEvent(int index) {
        int key = 0;
        try {
            Field f = KeyEvent.class.getField("VK_" + index);
            f.setAccessible(true);
            key = (Integer) f.get(null);
        } catch (IllegalAccessException ignored) {
        } catch (IllegalArgumentException ignored) {
        } catch (NoSuchFieldException ignored) {
        } catch (SecurityException ignored) {
        }
        return key;
    }
}
