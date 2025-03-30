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
package org.mypomodoro.gui.reports;

import org.mypomodoro.gui.activities.*;
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
import org.mypomodoro.model.ReportList;
import org.mypomodoro.util.Labels;

/**
 * GUI for viewing what is in the ActivityList. This can be changed later. Right
 * now it uses a TableModel to build the JTable. Table Listeners can be added to
 * save cell edits to the ActivityCollection which can then be saved to the data
 * layer.
 *
 */
public class ReportsPanel extends AbstractPanel {

    private static final Dimension PANE_DIMENSION = new Dimension(800, 200);
    private static final Dimension TABPANE_DIMENSION = new Dimension(800, 50);
    // Tab panes: details,...
    private final DetailsPanel detailsPanel = new DetailsPanel(this);
    private final CommentPanel commentPanel = new CommentPanel(this);
    private final EditPanel editPanel = new EditPanel(detailsPanel);

    public ReportsPanel() {
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
        subTableModel = new ReportsSubTableModel();
        tableModel = new ReportsTableModel();
        subTable = new ReportsSubTable((ReportsSubTableModel) subTableModel, this); // instance this before table
        table = new ReportsTable((ReportsTableModel) tableModel, this);
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
        tabbedPane.setImportTabIndex(3);
        tabbedPane.setExportTabIndex(4);
        tabbedPane.add(Labels.getString("Common.Details"), detailsPanel);
        tabbedPane.add(Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "Common.Comment"), commentPanel);
        tabbedPane.add(Labels.getString("Common.Edit"), editPanel);
        ImportPanel importPanel = new ImportPanel(this);
        tabbedPane.add(Labels.getString("ReportListPanel.Import"), importPanel);
        ExportPanel exportPanel = new ExportPanel(this);
        tabbedPane.add(Labels.getString("ReportListPanel.Export"), exportPanel);
        addTabbedPaneKeyStrokes();
    }

    @Override
    public ReportsTableModel getNewTableModel() {
        return new ReportsTableModel();
    }

    @Override
    public ReportList getList() {
        return ReportList.getList();
    }

    @Override
    public ReportsTable getMainTable() {
        return (ReportsTable) table;
    }

    @Override
    public ReportsTable getCurrentTable() {
        return (ReportsTable) currentTable;
    }

    @Override
    public void setCurrentTable(AbstractTable table) {
        currentTable = (ReportsTable) table;
    }

    @Override
    public ReportsSubTable getSubTable() {
        return (ReportsSubTable) subTable;
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
        ((ReportsSubTableModel) subTableModel).update(parentId);
        subTable.setColumnModel();
        subTable.setTitle();
        setCurrentTable(table);
    }
}
