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
import org.mypomodoro.Main;
import org.mypomodoro.buttons.MoveSubtaskButton;
import org.mypomodoro.buttons.DeleteButton;
import org.mypomodoro.gui.AbstractTableModel;
import org.mypomodoro.model.AbstractActivities;
import org.mypomodoro.model.Activity;
import org.mypomodoro.model.ActivityList;
import org.mypomodoro.util.ColorUtil;
import org.mypomodoro.util.Labels;
import org.mypomodoro.util.TimeConverter;

/**
 * Table for sub-activities
 *
 */
public class ActivitiesSubTable extends ActivitiesTable {

    public ActivitiesSubTable(ActivitiesSubTableModel model, final ActivitiesPanel panel) {
        super(model, panel);
    }

    // no story points and no refresh button for subtasks
    @Override
    public void setTitle() {
        String title = Labels.getString("Common.Subtasks");
        if (canCreateNewTask()) {
            getTitlePanel().showCreateButton();
        } else {
            getTitlePanel().hideCreateButton(); // this happens when main table is empty
        }
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
                // Tool tip
                String toolTipText = Labels.getString("Common.Estimated") + ": ";
                toolTipText += TimeConverter.getLength(real) + " / ";
                toolTipText += TimeConverter.getLength(estimated + overestimated);
                /*if (overestimated > 0) {
                 toolTipText += " + " + TimeConverter.getLength(overestimated);
                 }*/
                getTitlePanel().setToolTipText(toolTipText);
                // Hide buttons of the quick bar
                getTitlePanel().hideDuplicateButton();
                getTitlePanel().hideDoneButton();
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
                // Tool tip
                String toolTipText = Labels.getString("Common.Estimated") + ": ";
                toolTipText += TimeConverter.getLength(tableList.getNbRealPom()) + " / ";
                toolTipText += TimeConverter.getLength(tableList.getNbEstimatedPom() + tableList.getNbOverestimatedPom());
                /*if (tableList.getNbOverestimatedPom() > 0) {
                 toolTipText += " + " + TimeConverter.getLength(tableList.getNbOverestimatedPom());
                 }*/
                getTitlePanel().setToolTipText(toolTipText);
                if (getSelectedRowCount() == 1) {
                    getTitlePanel().showDuplicateButton();
                } else {
                    getTitlePanel().hideDuplicateButton();
                }
                if (getSelectedRowCount() == 1) {
                    getTitlePanel().showDoneButton();
                } else {
                    getTitlePanel().hideDoneButton();
                }
            }
        } else {
            //title += " (0)";
            getTitlePanel().hideDuplicateButton();
            getTitlePanel().hideDoneButton();
        }
        /*if (canMoveSubtasks()) {
         getTitlePanel().showMoveSubtasksButton();
         } else {
         getTitlePanel().hideMoveSubtasksButton();
         }*/
        // Update title
        getTitlePanel().setText("<html>" + title + "</html>");
        getTitlePanel().repaint(); // this is necessary to force stretching of panel
    }

    @Override
    public void setColumnModel() {
        super.setColumnModel();
        getColumnModel().getColumn(AbstractTableModel.DATE_COLUMN_INDEX).setCellRenderer(new ActivityDateRenderer());
        // sub types
        /*String[] types = (String[]) SubTaskTypeList.getTypes().toArray(new String[0]);
         getColumnModel().getColumn(AbstractTableModel.TYPE_COLUMN_INDEX).setCellRenderer(new ActivitiesTypeComboBoxCellRenderer(types, true));
         getColumnModel().getColumn(AbstractTableModel.TYPE_COLUMN_INDEX).setCellEditor(new ActivitiesTypeComboBoxCellEditor(types, true));*/
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
    protected ActivityList getTableList() {
        return ActivityList.getSubTaskList(panel.getMainTable().getActivityIdFromSelectedRow());
    }

    @Override
    public TitlePanel getTitlePanel() {
        return panel.getSubTableTitlePanel();
    }

    // Create specific to subtasks
    // default name (N) + New subtask
    @Override
    public void createNewTask() {
        Activity newActivity = new Activity();
        newActivity.setName("(N) " + Labels.getString("Common.New subtask"));
        // Set parent id
        Activity parentActivity = panel.getMainTable().getActivityFromSelectedRow();
        newActivity.setParentId(parentActivity.getId());
        getList().add(newActivity); // save activity in database       
        int row = insertRow(newActivity);
        editTitleCellAtRowIndex(row);
        panel.getTabbedPane().selectEditTab(); // open edit tab
    }

    @Override
    public void deleteTasks() {
        if (canDeleteTasks()) {
            DeleteButton b = new DeleteButton(panel);
            b.doClick();
        }
    }

    @Override
    public void deleteTask(int rowIndex) {
        super.deleteTask(rowIndex);
        // set main table as current table when no subtasks anymore
        if (getRowCount() == 0) {
            panel.setCurrentTable(panel.getMainTable());
        }
    }

    @Override
    public void moveSubtasksToMainTable() {
        MoveSubtaskButton m = new MoveSubtaskButton(Labels.getString("Common.Move subtask"), Labels.getString("Common.Are you sure to move those subtasks?"), panel);
        m.doClick();
    }

    @Override
    public void moveSubtaskToMainTable(int rowIndex) {
        Activity subtask = getActivityFromRowIndex(rowIndex);
        // remove pomodoros from parent task
        panel.getMainTable().removePomsFromSelectedRow(subtask);
        subtask.setParentId(-1); // make subtask a task
        getList().update(subtask); // update ex-subtask
        removeRow(rowIndex);
        panel.getMainTable().insertRow(subtask);
        // set main table as current table when no subtasks anymore
        if (getRowCount() == 0) {
            panel.setCurrentTable(panel.getMainTable());
        }
    }

    // Can't move subtasks
    @Override
    public void moveTask(int rowIndex) {
    }

    private boolean canCreateNewTask() {
        return panel.getMainTable().getSelectedRowCount() == 1; // no multiple selection in main table to create subtask
    }

    // can't send subtasks to ToDo list
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
