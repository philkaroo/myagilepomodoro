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

import org.mypomodoro.gui.TitlePanel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.mypomodoro.Main;
import org.mypomodoro.gui.AbstractTableModel;
import org.mypomodoro.model.AbstractActivities;
import org.mypomodoro.model.Activity;
import org.mypomodoro.model.ReportList;
import org.mypomodoro.util.ColorUtil;
import org.mypomodoro.util.Labels;
import org.mypomodoro.util.TimeConverter;

/**
 * Table for sub-activities
 *
 */
public class ReportsSubTable extends ReportsTable {

    public ReportsSubTable(ReportsSubTableModel model, final ReportsPanel panel) {
        super(model, panel);

        // This is to address the case/event when the mouse exit the table
        // Replacing listener of the ActivtiesTable class constructor
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseExited(MouseEvent e) {
                // Reset to currently selected task
                if (panel.getMainTable().getSelectedRowCount() == 1) {
                    if (getSelectedRowCount() == 1) {
                        showInfoForSelectedRow();
                    } else if (getSelectedRowCount() == 0) { // selected row on the main table
                        Activity activity = panel.getMainTable().getActivityFromSelectedRow();
                        // Activity may be null when hovering the cursor over the tasks while deleting/moving it
                        if (activity != null) {
                            showInfo(activity);
                        }
                    }
                }
                mouseHoverRow = -1;
            }
        });
    }

    // no story points and no refresh button for subtasks
    @Override
    public void setTitle() {
        String title = Labels.getString("Common.Subtasks");
        int rowCount = getModel().getRowCount();
        if (rowCount > 0) {
            int selectedRowCount = getSelectedRowCount();
            AbstractActivities tableList = getTableList();
            if (selectedRowCount > 1) {
                int[] rows = getSelectedRows();
                int estimated = 0;
                int overestimated = 0;
                int real = 0;
                int nbCompleted = 0;
                for (int row : rows) {
                    Activity selectedActivity = getActivityFromRowIndex(row);
                    estimated += selectedActivity.getEstimatedPoms();
                    overestimated += selectedActivity.getOverestimatedPoms();
                    real += selectedActivity.getActualPoms();
                    nbCompleted += selectedActivity.isCompleted() ? 1 : 0;
                }
                title += " (" + "<span style=\"color:black; background-color:" + ColorUtil.toHex(Main.selectedRowColor) + "\">&nbsp;";
                if (nbCompleted > 0) {
                    title += "<span style=\"text-decoration:line-through\">" + nbCompleted + "</span>" + " / ";
                }
                title += selectedRowCount + "&nbsp;</span>" + "/" + rowCount + ")";
                title += " > E: " + "<span style=\"color:black; background-color:" + ColorUtil.toHex(Main.selectedRowColor) + "\">&nbsp;" + real + " / " + estimated;
                if (overestimated > 0) {
                    title += " + " + overestimated;
                }
                title += "&nbsp;</span>";
                int accuracy = real == 0 || estimated + overestimated == 0 ? 0 : Math.round(((float) real / ((float) estimated + overestimated)) * 100); // ok to use Math.round here (eg: 1.0 --> 1 but 1.6 --> 2)
                title += " > " + (Main.preferences.getAgileMode() ? "A" : Labels.getString("ReportListPanel.Accuracy")) + ": " + "<span style=\"color:black; background-color:" + ColorUtil.toHex(Main.selectedRowColor) + "\">&nbsp;" + accuracy + "%" + "&nbsp;</span>";
                // Tool tip
                String toolTipText = Labels.getString("Common.Estimated") + ": ";
                toolTipText += TimeConverter.getLength(real) + " / ";
                toolTipText += TimeConverter.getLength(estimated + overestimated);
                /*if (overestimated > 0) {
                 toolTipText += " + " + TimeConverter.getLength(overestimated);
                 }*/
                toolTipText += " > " + Labels.getString("ReportListPanel.Accuracy") + ": " + accuracy + "%";
                getTitlePanel().setToolTipText(toolTipText);
            } else {
                title += " (";
                int nbCompleted = tableList.getNbSubtasksCompleted();
                if (nbCompleted > 0) {
                    title += "<span style=\"text-decoration:line-through\">" + nbCompleted + "</span>" + "/";
                }
                title += rowCount + ")";
                title += " > E: ";
                title += tableList.getNbRealPom();
                title += " / " + tableList.getNbEstimatedPom();
                if (tableList.getNbOverestimatedPom() > 0) {
                    title += " + " + tableList.getNbOverestimatedPom();
                }
                int accuracy = ReportList.getList().getSubTasksAccuracy(panel.getMainTable().getActivityIdFromSelectedRow());
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
                getTitlePanel().setToolTipText(toolTipText);
            }
        } else {
            getTitlePanel().hideDoneButton();
        }
        if (getSelectedRowCount() == 1) {
            getTitlePanel().showDoneButton();
        } else {
            getTitlePanel().hideDoneButton();
        }
        // Update title
        getTitlePanel().setText("<html>" + title + "</html>");
        getTitlePanel().repaint(); // this is necessary to force stretching of panel
    }

    @Override
    public void setColumnModel() {
        super.setColumnModel();
        // hide Story Points and Iteration columns
        getColumnModel().getColumn(AbstractTableModel.STORYPOINTS_COLUMN_INDEX).setMaxWidth(0);
        getColumnModel().getColumn(AbstractTableModel.STORYPOINTS_COLUMN_INDEX).setMinWidth(0);
        getColumnModel().getColumn(AbstractTableModel.STORYPOINTS_COLUMN_INDEX).setPreferredWidth(0);
        getColumnModel().getColumn(AbstractTableModel.ITERATION_COLUMN_INDEX).setMaxWidth(0);
        getColumnModel().getColumn(AbstractTableModel.ITERATION_COLUMN_INDEX).setMinWidth(0);
        getColumnModel().getColumn(AbstractTableModel.ITERATION_COLUMN_INDEX).setPreferredWidth(0);
    }

    @Override
    public void initTabs() {
        // Do nothing so this doesn't conflict with the main table
    }

    @Override
    public void setTableHeader() {
        // no table header
        setTableHeader(null);
    }

    @Override
    protected void populateSubTable() {
        // no sub table to populate
    }

    @Override
    protected void emptySubTable() {
        // no sub table to empty
    }

    @Override
    protected ReportList getTableList() {
        return ReportList.getSubTaskList(panel.getMainTable().getActivityIdFromSelectedRow());
    }

    @Override
    public TitlePanel getTitlePanel() {
        return panel.getSubTableTitlePanel();
    }

    // Can't delete subtasks  
    @Override
    protected boolean canDeleteTasks() {
        return false;
    }

    // Can't reopen subtasks
    @Override
    protected boolean canMoveTasks() {
        return false;
    }

    @Override
    public void setSubtaskComplete() {
        super.setSubtaskComplete();
        Activity act = getActivityFromSelectedRow();
        panel.getDetailsPanel().selectInfo(act);
        panel.getDetailsPanel().showInfo();
    }
}
