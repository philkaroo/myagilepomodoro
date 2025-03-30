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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import org.mypomodoro.Main;
import org.mypomodoro.buttons.DefaultButton;
import org.mypomodoro.gui.AbstractPanel;
import org.mypomodoro.gui.AbstractTable;
import org.mypomodoro.gui.TabbedPane;
import org.mypomodoro.gui.TitlePanel;
import org.mypomodoro.gui.activities.CommentPanel;
import org.mypomodoro.gui.export.ExportPanel;
import org.mypomodoro.model.ChartList;
import org.mypomodoro.util.Labels;
import org.mypomodoro.util.WaitCursor;

/**
 * GUI for viewing the Chart List.
 *
 */
public class CheckPanel extends AbstractPanel {

    private static final Dimension PANE_DIMENSION = new Dimension(700, 200);
    private static final Dimension TABPANE_DIMENSION = new Dimension(700, 50);
    private static final Dimension CREATEBUTTON_DIMENSION = new Dimension(100, 250);
    // Tab panes: details,...
    private final DetailsPanel detailsPanel = new DetailsPanel(this);
    private final CommentPanel commentPanel = new CommentPanel(this);
    // head tabbed pane and side button
    private final ChartTabbedPanel chartTabbedPanel;
    private final ChoosePanel choosePanel;

    public CheckPanel(ChartTabbedPanel chartTabbedPanel, ChoosePanel choosePanel) {

        this.chartTabbedPanel = chartTabbedPanel;
        this.choosePanel = choosePanel;

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

        // Init table (data model and rendering)
        tableModel = new CheckTableModel();
        table = new CheckTable((CheckTableModel) tableModel, this);

        // Init scroll panes
        tableScrollPane = new JScrollPane(table);

        // Init title
        tableTitlePanel = new TitlePanel(this, table);

        // select first activity of the table so the selection listener gets fired only now that both tables have been instanciated
        if (tableModel.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }

        // Add panes of List pane
        addTableTitlePanel();
        addTable();

        // Add Split pane
        add(splitPane);

        // Create button
        addCreateButton();
    }

    public ChooseInputForm getChooseInputForm() {
        return choosePanel.getForm();
    }

    ////////////////////////////////////////////////
    // TABBED PANE
    ////////////////////////////////////////////////
    @Override
    public void initTabbedPane() {
        tabbedPane.setDetailsTabIndex(0);
        tabbedPane.setCommentTabIndex(1);
        tabbedPane.setExportTabIndex(2);
        tabbedPane.add(Labels.getString("Common.Details"), detailsPanel);
        tabbedPane.add(Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "Common.Comment"), commentPanel);
        ExportPanel exportPanel = new ExportPanel(this);
        tabbedPane.add(Labels.getString("ReportListPanel.Export"), exportPanel);
        addTabbedPaneKeyStrokes();
    }

    ////////////////////////////////////////////////
    // SUB TITLE
    ////////////////////////////////////////////////
    @Override
    public void addSubTableTitlePanel() {
        // not used - only one table
    }

    @Override
    public CheckTableModel getNewTableModel() {
        return new CheckTableModel();
    }

    private void addCreateButton() {
        GridBagConstraints gbc = new GridBagConstraints();
        setLayout(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(splitPane, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.weightx = 0.1;
        JButton createButton = new DefaultButton(Labels.getString("BurndownChartPanel.Create"));
        createButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!WaitCursor.isStarted()) {
                    if (getList().size() > 0) {
                        chartTabbedPanel.goToStep4();
                    }
                }
            }
        });
        createButton.setMinimumSize(CREATEBUTTON_DIMENSION);
        createButton.setMaximumSize(CREATEBUTTON_DIMENSION);
        createButton.setPreferredSize(CREATEBUTTON_DIMENSION);
        add(createButton, gbc);
    }

    @Override
    public ChartList getList() {
        return ChartList.getList();
    }

    @Override
    public void emptySubTable() {
        // not used - only one table
    }

    @Override
    public void populateSubTable(int parentId) {
        // not used - only one table
    }

    @Override
    public CheckTable getMainTable() {
        return (CheckTable) table; // not used - only one table
    }

    @Override
    public CheckTable getCurrentTable() {
        return (CheckTable) table;
    }

    @Override
    public CheckTable getSubTable() {
        return null; // not used - only one table
    }

    @Override
    public void setCurrentTable(AbstractTable table) {
        // not used - onle one table
    }

    public DetailsPanel getDetailsPanel() {
        return detailsPanel;
    }

    public CommentPanel getCommentPanel() {
        return commentPanel;
    }

    @Override
    public JScrollPane getSubTableScrollPane() {
        return null; // not used - only one table
    }
}
