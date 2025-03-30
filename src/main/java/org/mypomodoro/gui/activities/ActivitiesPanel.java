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

import org.mypomodoro.gui.TitlePanel;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import org.mypomodoro.Main;
import org.mypomodoro.gui.AbstractPanel;
import org.mypomodoro.gui.AbstractTable;
import org.mypomodoro.gui.SubTableTitlePanel;
import org.mypomodoro.gui.TabbedPane;
import org.mypomodoro.gui.export.ExportPanel;
import org.mypomodoro.gui.export.ImportPanel;
import org.mypomodoro.model.ActivityList;
import org.mypomodoro.util.Labels;

/**
 * GUI for viewing what is in the ActivityList. This can be changed later. Right
 * now it uses a TableModel to build the JTable. Table Listeners can be added to
 * save cell edits to the ActivityCollection which can then be saved to the data
 * layer.
 *
 */
public class ActivitiesPanel extends AbstractPanel {

    private static final Dimension PANE_DIMENSION = new Dimension(800, 200);
    private static final Dimension TABPANE_DIMENSION = new Dimension(800, 50);
    // Tab panes: details,...
    private final DetailsPanel detailsPanel = new DetailsPanel(this);
    private final CommentPanel commentPanel = new CommentPanel(this);
    private final EditPanel editPanel = new EditPanel(this, detailsPanel);
    private final MergingPanel mergingPanel = new MergingPanel(this);

    public ActivitiesPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Init List pane
        listPane.setMinimumSize(PANE_DIMENSION);
        listPane.setPreferredSize(PANE_DIMENSION);
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));

        // Init Tabbed pane        
        tabbedPane = new TabbedPane(this);
        tabbedPane.setMinimumSize(TABPANE_DIMENSION);
        tabbedPane.setPreferredSize(TABPANE_DIMENSION);
        initTabbedPane();

        // Init Split pane
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listPane, tabbedPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        splitPane.setDividerSize(0); // remove divider by hiding it

        // Init table and sub table (data model and rendering)
        subTableModel = new ActivitiesSubTableModel();
        tableModel = new ActivitiesTableModel();
        subTable = new ActivitiesSubTable((ActivitiesSubTableModel) subTableModel, this); // instanciate this before table
        table = new ActivitiesTable((ActivitiesTableModel) tableModel, this);
        currentTable = table;

        // Init scroll panes
        subTableScrollPane = new JScrollPane(subTable);
        tableScrollPane = new JScrollPane(table);

        // Init title and sub title
        tableTitlePanel = new TitlePanel(this, table);
        subTableTitlePanel = new SubTableTitlePanel(this, subTable);

        // select first activity of the table so the selection listener gets fired only now that both tables have been instanciated
        if (tableModel.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }

        // Add panes of List pane
        addTableTitlePanel();
        addTable();
        addSubTableTitlePanel();

        // Add Split pane
        add(splitPane);
    }

    ////////////////////////////////////////////////
    // TABBED PANE
    ////////////////////////////////////////////////
    @Override
    public void initTabbedPane() {
        tabbedPane.setDetailsTabIndex(0);
        tabbedPane.setCommentTabIndex(1);
        tabbedPane.setEditTabIndex(2);
        tabbedPane.setMergeTabIndex(3);
        tabbedPane.setImportTabIndex(4);
        tabbedPane.setExportTabIndex(5);
        tabbedPane.add(Labels.getString("Common.Details"), detailsPanel);
        tabbedPane.add(Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "Common.Comment"), commentPanel);
        tabbedPane.add(Labels.getString("Common.Edit"), editPanel);
        tabbedPane.add(Labels.getString("ToDoListPanel.Merge"), mergingPanel);
        ImportPanel importPanel = new ImportPanel(this);
        tabbedPane.add(Labels.getString("ReportListPanel.Import"), importPanel);
        ExportPanel exportPanel = new ExportPanel(this);
        tabbedPane.add(Labels.getString("ReportListPanel.Export"), exportPanel);
        addTabbedPaneKeyStrokes();
    }

    @Override
    public ActivitiesTableModel getNewTableModel() {
        return new ActivitiesTableModel();
    }

    @Override
    public ActivityList getList() {
        return ActivityList.getList();
    }

    @Override
    public ActivitiesTable getMainTable() {
        return (ActivitiesTable) table;
    }

    @Override
    public ActivitiesTable getCurrentTable() {
        return (ActivitiesTable) currentTable;
    }

    @Override
    public void setCurrentTable(AbstractTable table) {
        currentTable = (ActivitiesTable) table;
    }

    @Override
    public ActivitiesSubTable getSubTable() {
        return (ActivitiesSubTable) subTable;
    }

    public DetailsPanel getDetailsPanel() {
        return detailsPanel;
    }

    public CommentPanel getCommentPanel() {
        return commentPanel;
    }

    public EditPanel getEditPanel() {
        return editPanel;
    }

    @Override
    public void populateSubTable(int parentId) {
        ((ActivitiesSubTableModel) subTableModel).update(parentId);
        subTable.setColumnModel();
        subTable.setTitle();
    }
}
