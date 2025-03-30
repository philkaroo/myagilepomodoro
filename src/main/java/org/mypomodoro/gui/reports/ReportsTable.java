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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import org.mypomodoro.Main;
import org.mypomodoro.buttons.DeleteButton;
import org.mypomodoro.buttons.MoveButton;
import org.mypomodoro.db.mysql.MySQLConfigLoader;
import org.mypomodoro.gui.AbstractTable;
import org.mypomodoro.gui.AbstractTableModel;
import org.mypomodoro.model.Activity;
import org.mypomodoro.util.ColorUtil;
import org.mypomodoro.util.ColumnResizer;
import org.mypomodoro.model.AbstractActivities;
import org.mypomodoro.model.ReportList;
import org.mypomodoro.util.DateUtil;
import org.mypomodoro.util.Labels;
import org.mypomodoro.util.TimeConverter;

/**
 * Table for activities
 *
 */
public class ReportsTable extends AbstractTable {

    protected final ReportsPanel panel;

    public ReportsTable(final ReportsTableModel model, final ReportsPanel panel) {
        super(model, panel);

        this.panel = panel;

        setTableHeader();

        setColumnModel();

        initTabs();

        getSelectionModel().addListSelectionListener(new AbstractListSelectionListener() {

            @Override
            public void customValueChanged(ListSelectionEvent e) {
                //System.err.println("method name = " + Thread.currentThread().getStackTrace()[1].getMethodName());
                int selectedRowCount = getSelectedRowCount();
                if (selectedRowCount > 0) {
                    // See AbstractActivitiesTable for reason to set WHEN_FOCUSED here
                    setInputMap(JTable.WHEN_FOCUSED, im);
                    if (selectedRowCount > 1) { // multiple selection
                        // diactivate/gray out unused tabs
                        panel.getTabbedPane().disableCommentTab();
                        panel.getTabbedPane().disableEditTab();
                        if (panel.getTabbedPane().getSelectedIndex() == panel.getTabbedPane().getCommentTabIndex()
                                || panel.getTabbedPane().getSelectedIndex() == panel.getTabbedPane().getEditTabIndex()) {
                            panel.getTabbedPane().setSelectedIndex(0); // switch to details panel
                        }
                        // Display info (list of selected tasks)                            
                        showDetailsForSelectedRows();
                        // empty subtable
                        emptySubTable();
                    } else if (selectedRowCount == 1) {
                        // activate all panels
                        for (int index = 0; index < panel.getTabbedPane().getTabCount(); index++) {
                            panel.getTabbedPane().setEnabledAt(index, true);
                        }
                        if (panel.getTabbedPane().getTabCount() > 0) { // at start-up time not yet initialised (see constructor)
                            panel.getTabbedPane().setSelectedIndex(panel.getTabbedPane().getSelectedIndex()); // switch to selected panel
                        }
                        scrollToSelectedRows(); // when sorting columns, focus on selected row
                        // Display details                           
                        showInfoForSelectedRow();
                        // populate subtable
                        populateSubTable();
                    }
                }
            }
        });

        // Listener on editable cells
        // Table model has a flaw: the update table event is fired whenever once click on an editable cell
        // To avoid update overhead, we compare old value with new value
        // (we could also have used solution found at https://tips4java.wordpress.com/2009/06/07/table-cell-listener)        
        getModel().addTableModelListener(new AbstractTableModelListener() {

            @Override
            public void customTableChanged(TableModelEvent e) {
                //System.err.println("method name = " + Thread.currentThread().getStackTrace()[1].getMethodName());                                                    
                int row = e.getFirstRow();
                int column = e.getColumn();
                ReportsTableModel sourceModel = (ReportsTableModel) e.getSource();
                Object data = sourceModel.getValueAt(row, column);
                if (data != null) {
                    Activity act = getActivityFromRowIndex(convertRowIndexToView(row)); // get index of the row in the view as getActivityFromRowIndex gets it in the model already
                    if (column == AbstractTableModel.TITLE_COLUMN_INDEX) { // Title (can't be empty)
                        String name = data.toString().trim();
                        if (!name.equals(act.getName())) {
                            if (name.length() == 0) {
                                // reset the original value. Title can't be empty.
                                sourceModel.setValueAt(act.getName(), row, AbstractTableModel.TITLE_COLUMN_INDEX);
                            } else {
                                act.setName(name);
                                act.databaseUpdate();
                                // The customer resizer may resize the title column to fit the length of the new text
                                ColumnResizer.adjustColumnPreferredWidths(ReportsTable.this);
                                revalidate();
                            }
                        }
                    } else if (column == AbstractTableModel.ESTIMATED_COLUMN_INDEX) { // This may happen when importing subtasks
                        // Update Diff 1 and 2 cells
                        Integer diffIPoms = act.getActualPoms() - act.getEstimatedPoms();
                        sourceModel.setValueAt(diffIPoms, row, AbstractTableModel.DIFFI_COLUMN_INDEX);
                        Integer diffIIPoms = act.getActualPoms()
                                - act.getEstimatedPoms()
                                - act.getOverestimatedPoms();
                        sourceModel.setValueAt(diffIIPoms, row, AbstractTableModel.DIFFII_COLUMN_INDEX);
                    }
                    getList().update(act);
                    // Updating details only
                    panel.getDetailsPanel().selectInfo(act);
                    panel.getDetailsPanel().showInfo();
                }
            }
        });
    }

    @Override
    public ReportsTableModel getModel() {
        return (ReportsTableModel) super.getModel();
    }

    @Override
    public void setColumnModel() {
        // set custom render for dates
        getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setCellRenderer(new UnplannedRenderer()); // unplanned (custom renderer)
        getColumnModel().getColumn(AbstractTableModel.DATE_COLUMN_INDEX).setCellRenderer(new ReportDateRenderer()); // date (custom report renderer)
        getColumnModel().getColumn(AbstractTableModel.TITLE_COLUMN_INDEX).setCellRenderer(new TitleRenderer()); // title
        getColumnModel().getColumn(AbstractTableModel.TYPE_COLUMN_INDEX).setCellRenderer(new ToolTipRenderer()); // type
        getColumnModel().getColumn(AbstractTableModel.ESTIMATED_COLUMN_INDEX).setCellRenderer(new EstimatedCellRenderer()); // estimated        
        getColumnModel().getColumn(AbstractTableModel.DIFFI_COLUMN_INDEX).setCellRenderer(new CustomRenderer()); // Diff I
        getColumnModel().getColumn(AbstractTableModel.DIFFII_COLUMN_INDEX).setCellRenderer(new Diff2CellRenderer()); // Diff II
        getColumnModel().getColumn(AbstractTableModel.STORYPOINTS_COLUMN_INDEX).setCellRenderer(new StoryPointsCellRenderer()); // story points
        getColumnModel().getColumn(AbstractTableModel.ITERATION_COLUMN_INDEX).setCellRenderer(new IterationCellRenderer()); // iteration        

        // hide story points and iteration in 'classic' mode
        if (!Main.preferences.getAgileMode()) {
            getColumnModel().getColumn(AbstractTableModel.STORYPOINTS_COLUMN_INDEX).setMaxWidth(0);
            getColumnModel().getColumn(AbstractTableModel.STORYPOINTS_COLUMN_INDEX).setMinWidth(0);
            getColumnModel().getColumn(AbstractTableModel.STORYPOINTS_COLUMN_INDEX).setPreferredWidth(0);
            getColumnModel().getColumn(AbstractTableModel.ITERATION_COLUMN_INDEX).setMaxWidth(0);
            getColumnModel().getColumn(AbstractTableModel.ITERATION_COLUMN_INDEX).setMinWidth(0);
            getColumnModel().getColumn(AbstractTableModel.ITERATION_COLUMN_INDEX).setPreferredWidth(0);
        } else {
            // Set width of columns story points, iteration
            getColumnModel().getColumn(AbstractTableModel.STORYPOINTS_COLUMN_INDEX).setMaxWidth(40);
            getColumnModel().getColumn(AbstractTableModel.STORYPOINTS_COLUMN_INDEX).setMinWidth(40);
            getColumnModel().getColumn(AbstractTableModel.STORYPOINTS_COLUMN_INDEX).setPreferredWidth(40);
            getColumnModel().getColumn(AbstractTableModel.ITERATION_COLUMN_INDEX).setMaxWidth(40);
            getColumnModel().getColumn(AbstractTableModel.ITERATION_COLUMN_INDEX).setMinWidth(40);
            getColumnModel().getColumn(AbstractTableModel.ITERATION_COLUMN_INDEX).setPreferredWidth(40);
        }
        // hide unplanned in Agile mode
        if (Main.preferences.getAgileMode()) {
            getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setMaxWidth(0);
            getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setMinWidth(0);
            getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setPreferredWidth(0);
        } else {
            // Set width of columns Unplanned and date
            getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setMaxWidth(30);
            getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setMinWidth(30);
            getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setPreferredWidth(30);
        }
        // Set width of column Date
        getColumnModel().getColumn(AbstractTableModel.DATE_COLUMN_INDEX).setMaxWidth(90);
        getColumnModel().getColumn(AbstractTableModel.DATE_COLUMN_INDEX).setMinWidth(90);
        getColumnModel().getColumn(AbstractTableModel.DATE_COLUMN_INDEX).setPreferredWidth(90);
        // Set width of column estimated
        getColumnModel().getColumn(AbstractTableModel.ESTIMATED_COLUMN_INDEX).setMaxWidth(90);
        getColumnModel().getColumn(AbstractTableModel.ESTIMATED_COLUMN_INDEX).setMinWidth(90);
        getColumnModel().getColumn(AbstractTableModel.ESTIMATED_COLUMN_INDEX).setPreferredWidth(90);
        getColumnModel().getColumn(AbstractTableModel.DIFFI_COLUMN_INDEX).setMaxWidth(40);
        getColumnModel().getColumn(AbstractTableModel.DIFFI_COLUMN_INDEX).setMinWidth(40);
        getColumnModel().getColumn(AbstractTableModel.DIFFI_COLUMN_INDEX).setPreferredWidth(40);
        getColumnModel().getColumn(AbstractTableModel.DIFFII_COLUMN_INDEX).setMaxWidth(40);
        getColumnModel().getColumn(AbstractTableModel.DIFFII_COLUMN_INDEX).setMinWidth(40);
        getColumnModel().getColumn(AbstractTableModel.DIFFII_COLUMN_INDEX).setPreferredWidth(40);
        // Set width of column type
        getColumnModel().getColumn(AbstractTableModel.TYPE_COLUMN_INDEX).setMaxWidth(150);
        getColumnModel().getColumn(AbstractTableModel.TYPE_COLUMN_INDEX).setMinWidth(150);
        getColumnModel().getColumn(AbstractTableModel.TYPE_COLUMN_INDEX).setPreferredWidth(150);
        // hide priority
        getColumnModel().getColumn(AbstractTableModel.PRIORITY_COLUMN_INDEX).setMaxWidth(0);
        getColumnModel().getColumn(AbstractTableModel.PRIORITY_COLUMN_INDEX).setMinWidth(0);
        getColumnModel().getColumn(AbstractTableModel.PRIORITY_COLUMN_INDEX).setPreferredWidth(0);
        // hide ID column
        getColumnModel().getColumn(AbstractTableModel.ACTIVITYID_COLUMN_INDEX).setMaxWidth(0);
        getColumnModel().getColumn(AbstractTableModel.ACTIVITYID_COLUMN_INDEX).setMinWidth(0);
        getColumnModel().getColumn(AbstractTableModel.ACTIVITYID_COLUMN_INDEX).setPreferredWidth(0);
        // enable sorting
        if (getModel().getRowCount() > 0) {
            setAutoCreateRowSorter(true);
        }

        // Make sure column title will fit long titles
        ColumnResizer.adjustColumnPreferredWidths(this);
        revalidate();
    }

    @Override
    protected void showInfo(Activity activity) {
        panel.getDetailsPanel().selectInfo(activity);
        panel.getDetailsPanel().showInfo();
        panel.getCommentPanel().showInfo(activity);
        panel.getEditPanel().showInfo(activity);
    }

    @Override
    protected void showDetailsForSelectedRows() {
        panel.getDetailsPanel().showInfo(getDetailsForSelectedRows());
    }

    @Override
    protected ReportList getList() {
        return ReportList.getList();
    }

    @Override
    protected ReportList getTableList() {
        return ReportList.getTaskList();
    }

    @Override
    public void setTableHeader() {
        String[] columnToolTips = AbstractTableModel.COLUMN_NAMES.clone();
        columnToolTips[AbstractTableModel.UNPLANNED_COLUMN_INDEX] = Labels.getString("Common.Unplanned");
        columnToolTips[AbstractTableModel.DATE_COLUMN_INDEX] = Main.preferences.getAgileMode() ? Labels.getString("Common.Done") : Labels.getString("Common.Date completed"); //  + (Main.preferences.getAgileMode() ? " / " + Labels.getString("Agile.ReportListPanel.Done-Done") : "")
        columnToolTips[AbstractTableModel.ESTIMATED_COLUMN_INDEX] = Labels.getString("Common.Real") + " / " + Labels.getString("Common.Estimated") + " (+ " + Labels.getString("Common.Overestimated") + ")";
        columnToolTips[AbstractTableModel.DIFFI_COLUMN_INDEX] = Labels.getString("ReportListPanel.Diff I") + " = " + Labels.getString("Common.Real") + " - " + Labels.getString("Common.Estimated");
        columnToolTips[AbstractTableModel.DIFFII_COLUMN_INDEX] = Labels.getString("ReportListPanel.Diff II") + " = " + Labels.getString("Common.Real") + " - " + Labels.getString("Common.Estimated") + " - " + Labels.getString("Common.Overestimated");
        ReportsTableHeader customTableHeader = new ReportsTableHeader(this, columnToolTips);
        setTableHeader(customTableHeader);
    }

    @Override
    public void setTitle() {
        String title = Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "ReportListPanel.Report List");
        int rowCount = getModel().getRowCount();
        if (rowCount > 0) {
            int selectedRowCount = getSelectedRowCount();
            AbstractActivities tableList = getTableList();
            if (selectedRowCount > 0) {
                getTitlePanel().showSelectedButton();
            }
            if (selectedRowCount > 1) {
                int[] rows = getSelectedRows();
                int estimated = 0;
                int overestimated = 0;
                int real = 0;
                float storypoints = 0;
                int nbDoneDone = 0;
                ArrayList<Date> datesCompleted = new ArrayList<Date>();
                for (int row : rows) {
                    Activity selectedActivity = getActivityFromRowIndex(row);
                    estimated += selectedActivity.getEstimatedPoms();
                    overestimated += selectedActivity.getOverestimatedPoms();
                    real += selectedActivity.getActualPoms();
                    storypoints += selectedActivity.getStoryPoints();
                    nbDoneDone += Main.preferences.getAgileMode() && selectedActivity.isDoneDone() ? 1 : 0;
                    // calculate the number of different completed dates for the selection
                    Date dateCompletedAtMidnight = DateUtil.getDateAtMidnight(selectedActivity.getDateCompleted());
                    if (!datesCompleted.contains(dateCompletedAtMidnight)) {
                        datesCompleted.add(dateCompletedAtMidnight);
                    }
                }
                title += " (" + "<span style=\"color:black; background-color:" + ColorUtil.toHex(Main.selectedRowColor) + "\">&nbsp;";
                if (nbDoneDone > 0 && Main.preferences.getAgileMode()) {
                    title += "<span style=\"text-decoration:line-through\">" + nbDoneDone + "</span>" + " / ";
                }
                title += selectedRowCount + "&nbsp;</span>" + "/" + rowCount + ")";
                title += " > E: " + "<span style=\"color:black; background-color:" + ColorUtil.toHex(Main.selectedRowColor) + "\">&nbsp;" + real + " / " + estimated;
                if (overestimated > 0) {
                    title += " + " + overestimated;
                }
                title += "&nbsp;</span>";
                int accuracy = real == 0 || estimated + overestimated == 0 ? 0 : Math.round(((float) real / ((float) estimated + overestimated)) * 100); // ok to use Math.round here (eg: 1.0 --> 1 but 1.6 --> 2)
                title += " > " + (Main.preferences.getAgileMode() ? "A" : Labels.getString("ReportListPanel.Accuracy")) + ": " + "<span style=\"color:black; background-color:" + ColorUtil.toHex(Main.selectedRowColor) + "\">&nbsp;" + accuracy + "%" + "&nbsp;</span>";
                String toolTipText = Labels.getString("Common.Estimated") + ": ";
                toolTipText += TimeConverter.getLength(real) + " / ";
                toolTipText += TimeConverter.getLength(estimated + overestimated);
                /*if (overestimated > 0) {
                 toolTipText += " + " + TimeConverter.getLength(overestimated);
                 }*/
                toolTipText += " > " + Labels.getString("ReportListPanel.Accuracy") + ": " + accuracy + "%";
                if (Main.preferences.getAgileMode()) {
                    DecimalFormat df = new DecimalFormat("0.#");
                    // Velovity
                    //" + Labels.getString("Agile.Common.Velocity") + "
                    title += " > V: " + "<span style=\"color:black; background-color:" + ColorUtil.toHex(Main.selectedRowColor) + "\">&nbsp;" + df.format(storypoints) + "&nbsp;</span>";
                    toolTipText += " > " + Labels.getString("Agile.Common.Velocity") + ": " + df.format(storypoints);
                    // productivity (SP / day)
                    title += " > P: " + "<span style=\"color:black; background-color:" + ColorUtil.toHex(Main.selectedRowColor) + "\">&nbsp;" + df.format(storypoints / datesCompleted.size()) + "&nbsp;</span>";
                    toolTipText += " > " + Labels.getString("Agile.Common.Productivity") + ": " + df.format(storypoints / datesCompleted.size());
                } else {
                    DecimalFormat df = new DecimalFormat("0.#");
                    // productivity (Poms / day)
                    title += " > P: " + "<span style=\"color:black; background-color:" + ColorUtil.toHex(Main.selectedRowColor) + "\">&nbsp;" + df.format(new Float(real) / datesCompleted.size()) + "&nbsp;</span>";
                    toolTipText += " > " + Labels.getString("Agile.Common.Productivity") + ": " + df.format(new Float(real) / datesCompleted.size());
                }
                getTitlePanel().setToolTipText(toolTipText);
                getTitlePanel().hideDoneDoneButton();
            } else {
                title += " (";
                int nbDoneDone = tableList.getNbTasksDoneDone();
                if (nbDoneDone > 0 && Main.preferences.getAgileMode()) {
                    title += "<span style=\"text-decoration:line-through\">" + nbDoneDone + "</span>" + "/";
                }
                title += rowCount + ")";
                title += " > E: ";
                title += tableList.getNbRealPom();
                title += " / " + tableList.getNbEstimatedPom();
                if (tableList.getNbOverestimatedPom() > 0) {
                    title += " + " + tableList.getNbOverestimatedPom();
                }
                int accuracy = getTableList().getAccuracy();
                title += " > " + (Main.preferences.getAgileMode() ? "A" : Labels.getString("ReportListPanel.Accuracy")) + ": ";
                title += accuracy + "%";
                // Tool tip
                String toolTipText = Labels.getString("Common.Estimated") + ": ";
                toolTipText += TimeConverter.getLength(tableList.getNbRealPom()) + " / ";
                toolTipText += TimeConverter.getLength(tableList.getNbEstimatedPom() + tableList.getNbOverestimatedPom());
                /*if (tableList.getNbOverestimatedPom() > 0) {
                 toolTipText += " + " + TimeConverter.getLength(tableList.getNbOverestimatedPom());
                 }*/
                toolTipText += " > " + Labels.getString("ReportListPanel.Accuracy") + ": " + accuracy + "%";
                if (Main.preferences.getAgileMode()) {
                    float storypoints = tableList.getStoryPoints();
                    DecimalFormat df = new DecimalFormat("0.#");
                    title += " > SP: " + df.format(storypoints);
                    toolTipText += " > " + Labels.getString("Agile.Common.Story Points") + ": " + df.format(storypoints);
                }
                getTitlePanel().setToolTipText(toolTipText);
            }
        } else {
            getTitlePanel().hideSelectedButton();
            getTitlePanel().hideDoneDoneButton();
        }
        if (Main.preferences.getAgileMode() && getSelectedRowCount() == 1) {
            getTitlePanel().showDoneDoneButton();
        } else {
            getTitlePanel().hideDoneDoneButton();
        }
        if (MySQLConfigLoader.isValid()) { // Remote mode (using MySQL database)
            getTitlePanel().showRefreshButton(); // end of the line
        }
        // Update title
        getTitlePanel().setText("<html>" + title + "</html>");
        getTitlePanel().repaint();
    }

    @Override
    public void deleteTasks() {
        if (canDeleteTasks()) {
            DeleteButton b = new DeleteButton(panel);
            b.doClick();
        }
    }

    // only tasks can be deleted  
    protected boolean canDeleteTasks() {
        return getSelectedRowCount() > 0;
    }

    @Override
    public void deleteTask(int rowIndex) {
        Activity activity = getActivityFromRowIndex(rowIndex);
        getList().delete(activity);
        removeRow(rowIndex);
    }

    // reopen
    @Override
    public void moveLeftTasks() {
        if (canMoveTasks()) {
            MoveButton moveButton = new MoveButton("", panel);
            moveButton.doClick();
        }
    }

    // only tasks can be reopened  
    protected boolean canMoveTasks() {
        return getSelectedRowCount() > 0;
    }

    @Override
    public void moveTask(int rowIndex) {
        Activity activity = getActivityFromRowIndex(rowIndex);
        getList().reopenToActivtyList(activity); // reopen to ActivityList; do not reopen/move subtasks only
        removeRow(rowIndex);
    }

    @Override
    public void importActivity(Activity activity) {
        getList().add(activity, activity.getDateCompleted());
    }

    @Override
    public void setTaskDoneDone() {
        super.setTaskDoneDone();
        Activity act = getActivityFromSelectedRow();
        panel.getDetailsPanel().selectInfo(act);
        panel.getDetailsPanel().showInfo();
    }
}
