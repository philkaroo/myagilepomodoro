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

import java.text.DecimalFormat;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import org.mypomodoro.Main;
import org.mypomodoro.buttons.DeleteButton;
import org.mypomodoro.buttons.MoveButton;
import org.mypomodoro.db.mysql.MySQLConfigLoader;
import org.mypomodoro.gui.AbstractTable;
import org.mypomodoro.gui.AbstractTableModel;
import org.mypomodoro.gui.create.list.TaskTypeList;
import org.mypomodoro.model.Activity;
import org.mypomodoro.model.ActivityList;
import org.mypomodoro.util.ColorUtil;
import org.mypomodoro.util.ColumnResizer;
import org.mypomodoro.gui.TableHeader;
import org.mypomodoro.model.AbstractActivities;
import org.mypomodoro.util.Labels;
import org.mypomodoro.util.TimeConverter;

/**
 * Table for activities
 *
 */
public class ActivitiesTable extends AbstractTable {

    protected final ActivitiesPanel panel;

    public ActivitiesTable(final ActivitiesTableModel model, final ActivitiesPanel panel) {
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
                        panel.getTabbedPane().enableMergeTab();
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
                            if (index == panel.getTabbedPane().getMergeTabIndex()) {
                                panel.getTabbedPane().disableMergeTab();
                                if (panel.getTabbedPane().getSelectedIndex() == panel.getTabbedPane().getMergeTabIndex()) {
                                    panel.getTabbedPane().setSelectedIndex(0); // switch to details panel
                                }
                            } else {
                                panel.getTabbedPane().setEnabledAt(index, true);
                            }
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
        // Table model has a flaw: the update table event is fired whenever once clicks on an editable cell
        // To avoid update overhead, we compare old value with new value
        // (we could also have used solution found at https://tips4java.wordpress.com/2009/06/07/table-cell-listener)        
        getModel().addTableModelListener(new AbstractTableModelListener() {

            @Override
            public void customTableChanged(TableModelEvent e) {
                //System.err.println("method name = " + Thread.currentThread().getStackTrace()[1].getMethodName());                                                    
                int row = e.getFirstRow();
                int column = e.getColumn();
                ActivitiesTableModel sourceModel = (ActivitiesTableModel) e.getSource();
                Object data = sourceModel.getValueAt(row, column); // in the view !!!!!!
                if (data != null) {
                    Activity act = getActivityFromRowIndex(convertRowIndexToView(row)); // get index of the row in the view as getActivityFromRowIndex gets it in the model already
                    if (column == AbstractTableModel.TITLE_COLUMN_INDEX) {
                        String name = data.toString().trim();
                        if (!name.equals(act.getName())) {
                            if (name.isEmpty()) { // Title (can't be empty)
                                // reset the original value. Title can't be empty.
                                sourceModel.setValueAt(act.getName(), row, AbstractTableModel.TITLE_COLUMN_INDEX);
                            } else {
                                act.setName(name);
                                act.databaseUpdate();
                                // The customer resizer may resize the title column to fit the length of the new text
                                ColumnResizer.adjustColumnPreferredWidths(ActivitiesTable.this);
                                revalidate();
                            }
                        }
                    } else if (column == AbstractTableModel.TYPE_COLUMN_INDEX) { // Type
                        String type = data.toString().trim();
                        if (!type.equals(act.getType())) {
                            act.setType(type);
                            act.databaseUpdate();
                            // load template for user stories
                            if (Main.preferences.getAgileMode()) {
                                panel.getCommentPanel().showInfo(act);
                            }
                            // refresh the combo boxes of all rows to display the new type (if any)
                            String[] types = (String[]) TaskTypeList.getTypes().toArray(new String[0]);
                            /*if (act.isSubTask()) {
                             types = (String[]) SubTaskTypeList.getTypes().toArray(new String[0]);
                             }*/
                            // if the columns have been moved around wemust covert the column index of the model to the column index of the view
                            getColumnModel().getColumn(convertColumnIndexToView(AbstractTableModel.TYPE_COLUMN_INDEX)).setCellRenderer(new ActivitiesTypeComboBoxCellRenderer(types, true));
                            getColumnModel().getColumn(convertColumnIndexToView(AbstractTableModel.TYPE_COLUMN_INDEX)).setCellEditor(new ActivitiesTypeComboBoxCellEditor(types, true));
                        }
                    } else if (column == AbstractTableModel.ESTIMATED_COLUMN_INDEX) { // Estimated
                        int estimated = (Integer) data;
                        if (estimated != act.getEstimatedPoms()
                                && estimated + act.getOverestimatedPoms() >= act.getActualPoms()) {
                            int diffEstimated = estimated - act.getEstimatedPoms();
                            act.setEstimatedPoms(estimated);
                            act.databaseUpdate();
                            if (act.isSubTask()) { // update parent activity
                                panel.getMainTable().addPomsToSelectedRow(0, diffEstimated, 0);
                            }
                        }
                    } else if (column == AbstractTableModel.STORYPOINTS_COLUMN_INDEX) { // Story Points
                        Float storypoints = (Float) data;
                        if (storypoints != act.getStoryPoints()) {
                            act.setStoryPoints(storypoints);
                            act.databaseUpdate();
                        }
                    } else if (column == AbstractTableModel.ITERATION_COLUMN_INDEX) { // Iteration 
                        int iteration = Integer.parseInt(data.toString());
                        if (iteration != act.getIteration()) {
                            act.setIteration(iteration);
                            act.databaseUpdate();
                        }
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
    public ActivitiesTableModel getModel() {
        return (ActivitiesTableModel) super.getModel();
    }

    @Override
    public void setColumnModel() {
        // set custom render for dates
        getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setCellRenderer(new UnplannedRenderer()); // unplanned (custom renderer)
        getColumnModel().getColumn(AbstractTableModel.DATE_COLUMN_INDEX).setCellRenderer(new DateRenderer()); // date (custom renderer)
        getColumnModel().getColumn(AbstractTableModel.TITLE_COLUMN_INDEX).setCellRenderer(new TitleRenderer()); // title
        // type combo box
        String[] types = (String[]) TaskTypeList.getTypes().toArray(new String[0]);
        getColumnModel().getColumn(AbstractTableModel.TYPE_COLUMN_INDEX).setCellRenderer(new ActivitiesTypeComboBoxCellRenderer(types, true));
        getColumnModel().getColumn(AbstractTableModel.TYPE_COLUMN_INDEX).setCellEditor(new ActivitiesTypeComboBoxCellEditor(types, true));
        // Estimated combo box
        // The values of the combo depends on the activity : see EstimatedComboBoxCellRenderer and EstimatedComboBoxCellEditor
        getColumnModel().getColumn(AbstractTableModel.ESTIMATED_COLUMN_INDEX).setCellRenderer(new ActivitiesEstimatedComboBoxCellRenderer(new Integer[0], false));
        getColumnModel().getColumn(AbstractTableModel.ESTIMATED_COLUMN_INDEX).setCellEditor(new ActivitiesEstimatedComboBoxCellEditor(new Integer[0], false));
        // Story Point combo box
        Float[] points = new Float[]{0f, 0.5f, 1f, 2f, 3f, 5f, 8f, 13f, 20f, 40f, 100f};
        getColumnModel().getColumn(AbstractTableModel.STORYPOINTS_COLUMN_INDEX).setCellRenderer(new ActivitiesStoryPointsComboBoxCellRenderer(points, false));
        getColumnModel().getColumn(AbstractTableModel.STORYPOINTS_COLUMN_INDEX).setCellEditor(new ActivitiesStoryPointsComboBoxCellEditor(points, false));
        // Iteration combo box
        Integer[] iterations = new Integer[102];
        for (int i = 0; i <= 101; i++) {
            iterations[i] = i - 1;
        }
        getColumnModel().getColumn(AbstractTableModel.ITERATION_COLUMN_INDEX).setCellRenderer(new ActivitiesIterationComboBoxCellRenderer(iterations, false));
        getColumnModel().getColumn(AbstractTableModel.ITERATION_COLUMN_INDEX).setCellEditor(new ActivitiesIterationComboBoxCellEditor(iterations, false));
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
            getColumnModel().getColumn(AbstractTableModel.STORYPOINTS_COLUMN_INDEX).setMaxWidth(60);
            getColumnModel().getColumn(AbstractTableModel.STORYPOINTS_COLUMN_INDEX).setMinWidth(60);
            getColumnModel().getColumn(AbstractTableModel.STORYPOINTS_COLUMN_INDEX).setPreferredWidth(60);
            getColumnModel().getColumn(AbstractTableModel.ITERATION_COLUMN_INDEX).setMaxWidth(60);
            getColumnModel().getColumn(AbstractTableModel.ITERATION_COLUMN_INDEX).setMinWidth(60);
            getColumnModel().getColumn(AbstractTableModel.ITERATION_COLUMN_INDEX).setPreferredWidth(60);
        }
        // hide unplanned and date in Agile mode
        if (Main.preferences.getAgileMode()) {
            getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setMaxWidth(0);
            getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setMinWidth(0);
            getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setPreferredWidth(0);
            getColumnModel().getColumn(AbstractTableModel.DATE_COLUMN_INDEX).setMaxWidth(0);
            getColumnModel().getColumn(AbstractTableModel.DATE_COLUMN_INDEX).setMinWidth(0);
            getColumnModel().getColumn(AbstractTableModel.DATE_COLUMN_INDEX).setPreferredWidth(0);
        } else {
            // Set width of columns Unplanned and date
            getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setMaxWidth(30);
            getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setMinWidth(30);
            getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setPreferredWidth(30);
            getColumnModel().getColumn(AbstractTableModel.DATE_COLUMN_INDEX).setMaxWidth(90);
            getColumnModel().getColumn(AbstractTableModel.DATE_COLUMN_INDEX).setMinWidth(90);
            getColumnModel().getColumn(AbstractTableModel.DATE_COLUMN_INDEX).setPreferredWidth(90);
        }
        // Set width of column estimated
        getColumnModel().getColumn(AbstractTableModel.ESTIMATED_COLUMN_INDEX).setMaxWidth(90);
        getColumnModel().getColumn(AbstractTableModel.ESTIMATED_COLUMN_INDEX).setMinWidth(90);
        getColumnModel().getColumn(AbstractTableModel.ESTIMATED_COLUMN_INDEX).setPreferredWidth(90);
        // Set width of column type
        getColumnModel().getColumn(AbstractTableModel.TYPE_COLUMN_INDEX).setMaxWidth(200);
        getColumnModel().getColumn(AbstractTableModel.TYPE_COLUMN_INDEX).setMinWidth(200);
        getColumnModel().getColumn(AbstractTableModel.TYPE_COLUMN_INDEX).setPreferredWidth(200);
        // hide priority, DiffI and DiffII
        getColumnModel().getColumn(AbstractTableModel.PRIORITY_COLUMN_INDEX).setMaxWidth(0);
        getColumnModel().getColumn(AbstractTableModel.PRIORITY_COLUMN_INDEX).setMinWidth(0);
        getColumnModel().getColumn(AbstractTableModel.PRIORITY_COLUMN_INDEX).setPreferredWidth(0);
        getColumnModel().getColumn(AbstractTableModel.DIFFI_COLUMN_INDEX).setMaxWidth(0);
        getColumnModel().getColumn(AbstractTableModel.DIFFI_COLUMN_INDEX).setMinWidth(0);
        getColumnModel().getColumn(AbstractTableModel.DIFFI_COLUMN_INDEX).setPreferredWidth(0);
        getColumnModel().getColumn(AbstractTableModel.DIFFII_COLUMN_INDEX).setMaxWidth(0);
        getColumnModel().getColumn(AbstractTableModel.DIFFII_COLUMN_INDEX).setMinWidth(0);
        getColumnModel().getColumn(AbstractTableModel.DIFFII_COLUMN_INDEX).setPreferredWidth(0);
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
    protected ActivityList getList() {
        return ActivityList.getList();
    }

    @Override
    protected ActivityList getTableList() {
        return ActivityList.getTaskList();
    }

    @Override
    public void setTableHeader() {
        String[] columnToolTips = AbstractTableModel.COLUMN_NAMES.clone();
        columnToolTips[AbstractTableModel.UNPLANNED_COLUMN_INDEX] = Labels.getString("Common.Unplanned");
        columnToolTips[AbstractTableModel.DATE_COLUMN_INDEX] = Labels.getString("Common.Date scheduled");
        columnToolTips[AbstractTableModel.ESTIMATED_COLUMN_INDEX] = "(" + Labels.getString("Common.Real") + " / ) " + Labels.getString("Common.Estimated") + " (+ " + Labels.getString("Common.Overestimated") + ")";
        TableHeader customTableHeader = new TableHeader(this, columnToolTips);
        setTableHeader(customTableHeader);
    }

    @Override
    public void setTitle() {
        String title = Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "ActivityListPanel.Activity List");
        getTitlePanel().showCreateButton();
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
                for (int row : rows) {
                    Activity selectedActivity = getActivityFromRowIndex(row);
                    estimated += selectedActivity.getEstimatedPoms();
                    overestimated += selectedActivity.getOverestimatedPoms();
                    storypoints += selectedActivity.getStoryPoints();
                    real += selectedActivity.getActualPoms();
                }
                title += " (" + "<span style=\"color:black; background-color:" + ColorUtil.toHex(Main.selectedRowColor) + "\">&nbsp;" + selectedRowCount + "&nbsp;</span>" + "/" + rowCount + ")";
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
                if (Main.preferences.getAgileMode()) {
                    DecimalFormat df = new DecimalFormat("0.#");
                    title += " > SP: " + "<span style=\"color:black; background-color:" + ColorUtil.toHex(Main.selectedRowColor) + "\">&nbsp;" + df.format(storypoints) + "&nbsp;</span>";
                    toolTipText += " > " + Labels.getString("Agile.Common.Story Points") + ": " + df.format(storypoints);
                }
                getTitlePanel().setToolTipText(toolTipText);
                // Hide buttons of the quick bar
                getTitlePanel().hideDuplicateButton();
            } else {
                title += " (" + rowCount + ")";
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
                if (Main.preferences.getAgileMode()) {
                    float storypoints = tableList.getStoryPoints();
                    DecimalFormat df = new DecimalFormat("0.#");
                    title += " > SP: " + df.format(storypoints);
                    toolTipText += " > " + Labels.getString("Agile.Common.Story Points") + ": " + df.format(storypoints);
                }
                getTitlePanel().setToolTipText(toolTipText);
                // Show buttons of the quick bar                
                getTitlePanel().showDuplicateButton();
            }
        } else {
            getTitlePanel().hideSelectedButton();
            getTitlePanel().hideDuplicateButton();
        }
        if (MySQLConfigLoader.isValid()) { // Remote mode (using MySQL database)
            getTitlePanel().showRefreshButton(); // end of the line
        }
        // Update title
        getTitlePanel().setText("<html>" + title + "</html>");
        getTitlePanel().repaint();
    }

    // default name (N) + New task
    @Override
    public void createNewTask() {
        Activity newActivity = new Activity();
        newActivity.setName("(N) " + Labels.getString("Common.New task"));
        getList().add(newActivity); // save activity in database
        int row = insertRow(newActivity);
        editTitleCellAtRowIndex(row);
        panel.getTabbedPane().selectEditTab(); // open edit tab
    }

    // default name: (D) + name ('(D)' is added by ActivityList)
    // duplicate subtasks too
    @Override
    public void duplicateTask() {
        if (getSelectedRowCount() == 1) {
            Activity activity = getActivityFromSelectedRow();
            try {
                Activity duplicatedActivity = getList().duplicate(activity);
                int row = insertRow(duplicatedActivity); // no selection after insertion so the editing works        
                editTitleCellAtRowIndex(row);
                if (duplicatedActivity.isSubTask()) {
                    panel.getMainTable().addPomsToSelectedRow(duplicatedActivity);
                }
                panel.getTabbedPane().selectEditTab(); // open edit tab
            } catch (CloneNotSupportedException ignored) {
            }
        }
    }

    @Override
    public void deleteTasks() {
        if (canDeleteTasks()) {
            DeleteButton b = new DeleteButton(panel);
            b.doClick();
        }
    }

    // tasks and subtasks can be deleted  
    protected boolean canDeleteTasks() {
        return getSelectedRowCount() > 0;
    }

    @Override
    public void deleteTask(int rowIndex) {
        Activity activity = getActivityFromRowIndex(rowIndex);
        if (activity.isSubTask()) {
            panel.getMainTable().removePomsFromSelectedRow(activity);
        }
        getList().delete(activity); // delete tasks and subtasks
        removeRow(rowIndex);
    }

    @Override
    public void moveRightTasks() {
        if (canMoveTasks()) {
            MoveButton moveButton = new MoveButton("", panel);
            moveButton.doClick();
        }
    }

    // only tasks can be send to ToDo list  
    protected boolean canMoveTasks() {
        return getSelectedRowCount() > 0;
    }

    // only tasks can be moved
    @Override
    public void moveTask(int rowIndex) {
        Activity activity = getActivityFromRowIndex(rowIndex);
        if (activity.isTask()) {
            getList().moveToTODOList(activity); // move to ToDoList
            removeRow(rowIndex);
        }
    }
}
