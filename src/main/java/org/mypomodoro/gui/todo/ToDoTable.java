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
package org.mypomodoro.gui.todo;

import java.awt.Color;
import java.text.DecimalFormat;
import javax.swing.DropMode;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import org.mypomodoro.Main;
import org.mypomodoro.buttons.CompleteToDoButton;
import org.mypomodoro.buttons.MoveToDoButton;
import org.mypomodoro.db.mysql.MySQLConfigLoader;
import org.mypomodoro.gui.AbstractTable;
import org.mypomodoro.gui.AbstractTableModel;
import org.mypomodoro.model.Activity;
import org.mypomodoro.util.ColorUtil;
import org.mypomodoro.util.ColumnResizer;
import org.mypomodoro.gui.TableHeader;
import org.mypomodoro.model.AbstractActivities;
import org.mypomodoro.model.ToDoList;
import org.mypomodoro.util.Labels;
import org.mypomodoro.util.TimeConverter;

/**
 * Table for activities
 *
 */
public class ToDoTable extends AbstractTable {

    protected final ToDoPanel panel;

    public ToDoTable(final ToDoTableModel model, final ToDoPanel panel) {
        super(model, panel);

        // Drag and drop
        setDragEnabled(true);
        setDropMode(DropMode.INSERT_ROWS);
        setTransferHandler(new ToDoTransferHandler(panel));

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
                        if ((panel.getPomodoro().inPomodoro() && getSelectedRowCount() > 2) || !panel.getPomodoro().inPomodoro()) {
                            panel.getTabbedPane().enableMergeTab();
                        }
                        if (panel.getTabbedPane().getSelectedIndex() == panel.getTabbedPane().getCommentTabIndex()
                                || panel.getTabbedPane().getSelectedIndex() == panel.getTabbedPane().getEditTabIndex()) {
                            panel.getTabbedPane().setSelectedIndex(0); // switch to details panel
                        }
                        // Display info (list of selected tasks)                            
                        showDetailsForSelectedRows();
                        // empty subtable
                        emptySubTable();
                        // hide start button unless timer is running
                        if (!panel.getPomodoro().getTimer().isRunning()) {
                            panel.getTimerPanel().hideStartButton();
                            panel.getTimerPanel().hideTimeMinusButton();
                            panel.getTimerPanel().hideTimePlusButton();
                            if (!panel.getPomodoro().inBreak()
                                    && panel.getPomodoro().getCurrentToDo() != null
                                    && panel.getPomodoro().getCurrentToDo().getRecordedTime() > 0) {
                                panel.getTimerPanel().hidePauseButton();
                            }
                        }
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
                        // here do not use showCurrentSelectedRow()
                        scrollRectToVisible(getCellRect(getSelectedRow(), 0, true)); // when sorting columns, focus on selected row
                        // Display details                           
                        showInfoForSelectedRow();
                        // Set recorded time and proper buttons
                        showTimeForSelectedRow();
                        // populate subtable
                        populateSubTable();
                        // the start button may have been hidden by a multiple selection
                        // hide start button unless timer is running
                        Activity activity = getActivityFromSelectedRow();
                        if (activity.isSubTask() && activity.isCompleted()) { // hide timer buttons if subtask done
                            panel.getTimerPanel().hideStartButton();
                            panel.getTimerPanel().hideTimeMinusButton();
                            panel.getTimerPanel().hideTimePlusButton();
                            panel.getTimerPanel().hidePauseButton();
                        } else {
                            panel.getTimerPanel().showStartButton();
                            panel.getTimerPanel().showTimeMinusButton();
                            panel.getTimerPanel().showTimePlusButton();
                            if (panel.getPomodoro().getTimer().isRunning()) {
                                panel.getTimerPanel().showPauseButton();
                            }
                        }
                    }
                    setIconLabels();
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
                ToDoTableModel sourceModel = (ToDoTableModel) e.getSource();
                Object data = sourceModel.getValueAt(row, column);
                if (data != null) {
                    if (column >= 0) { // This needs to be checked : the moveRow method (see ToDoTransferHandler) fires tableChanged with column = -1 
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
                                    ColumnResizer.adjustColumnPreferredWidths(ToDoTable.this);
                                    revalidate();
                                }
                                // Refresh icon label
                                setIconLabels();
                                // Refresh tooltip (name) on timer
                                Activity currentToDo = panel.getPomodoro().getCurrentToDo();
                                if (currentToDo != null
                                        && act.getId() == currentToDo.getId()) {
                                    panel.getPomodoro().setTooltipOnImage();
                                }
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
            }
        });
    }

    protected void showTimeForSelectedRow() {
        Activity activity = getActivityFromSelectedRow();
        // Activity may be null when hovering the cursor over the tasks while deleting/moving it        
        // Update time if timer not running except when breaks paused (=not yet started or paused during pomodoros)
        if (activity != null
                && !panel.getPomodoro().getTimer().isRunning()
                && !panel.getPomodoro().inBreak()) {
            panel.getPomodoro().initTimer(activity.getRecordedTime());
        }
    }

    @Override
    public ToDoTableModel getModel() {
        return (ToDoTableModel) super.getModel();
    }

    @Override
    public void setColumnModel() {
        getColumnModel().getColumn(AbstractTableModel.PRIORITY_COLUMN_INDEX).setCellRenderer(new CustomRenderer()); // priority
        getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setCellRenderer(new UnplannedRenderer()); // unplanned (custom renderer)
        getColumnModel().getColumn(AbstractTableModel.TITLE_COLUMN_INDEX).setCellRenderer(new TitleRenderer()); // title           
        // The values of the combo depends on the activity : see EstimatedComboBoxCellRenderer and EstimatedComboBoxCellEditor
        getColumnModel().getColumn(AbstractTableModel.ESTIMATED_COLUMN_INDEX).setCellRenderer(new ToDoEstimatedComboBoxCellRenderer(new Integer[0], false));
        getColumnModel().getColumn(AbstractTableModel.ESTIMATED_COLUMN_INDEX).setCellEditor(new ToDoEstimatedComboBoxCellEditor(new Integer[0], false));
        // Story Point combo box
        Float[] points = new Float[]{0f, 0.5f, 1f, 2f, 3f, 5f, 8f, 13f, 20f, 40f, 100f};
        getColumnModel().getColumn(AbstractTableModel.STORYPOINTS_COLUMN_INDEX).setCellRenderer(new ToDoStoryPointsComboBoxCellRenderer(points, false));
        getColumnModel().getColumn(AbstractTableModel.STORYPOINTS_COLUMN_INDEX).setCellEditor(new ToDoStoryPointsComboBoxCellEditor(points, false));
        // Iteration combo box
        Integer[] iterations = new Integer[102];
        for (int i = 0; i <= 101; i++) {
            iterations[i] = i - 1;
        }
        getColumnModel().getColumn(AbstractTableModel.ITERATION_COLUMN_INDEX).setCellRenderer(new ToDoIterationComboBoxCellRenderer(iterations, false));
        getColumnModel().getColumn(AbstractTableModel.ITERATION_COLUMN_INDEX).setCellEditor(new ToDoIterationComboBoxCellEditor(iterations, false));
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
        // hide unplanned in Agile mode
        if (Main.preferences.getAgileMode()) {
            getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setMaxWidth(0);
            getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setMinWidth(0);
            getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setPreferredWidth(0);
        } else {
            // Set width of columns Unplanned
            getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setMaxWidth(30);
            getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setMinWidth(30);
            getColumnModel().getColumn(AbstractTableModel.UNPLANNED_COLUMN_INDEX).setPreferredWidth(30);
        }
        // Set width of column priority
        getColumnModel().getColumn(AbstractTableModel.PRIORITY_COLUMN_INDEX).setMaxWidth(40);
        getColumnModel().getColumn(AbstractTableModel.PRIORITY_COLUMN_INDEX).setMinWidth(40);
        getColumnModel().getColumn(AbstractTableModel.PRIORITY_COLUMN_INDEX).setPreferredWidth(40);
        // Set width of column estimated
        getColumnModel().getColumn(AbstractTableModel.ESTIMATED_COLUMN_INDEX).setMaxWidth(90);
        getColumnModel().getColumn(AbstractTableModel.ESTIMATED_COLUMN_INDEX).setMinWidth(90);
        getColumnModel().getColumn(AbstractTableModel.ESTIMATED_COLUMN_INDEX).setPreferredWidth(90);
        // hide date, type, diffI and diff II columns
        getColumnModel().getColumn(AbstractTableModel.DATE_COLUMN_INDEX).setMaxWidth(0);
        getColumnModel().getColumn(AbstractTableModel.DATE_COLUMN_INDEX).setMinWidth(0);
        getColumnModel().getColumn(AbstractTableModel.DATE_COLUMN_INDEX).setPreferredWidth(0);
        getColumnModel().getColumn(AbstractTableModel.TYPE_COLUMN_INDEX).setMaxWidth(0);
        getColumnModel().getColumn(AbstractTableModel.TYPE_COLUMN_INDEX).setMinWidth(0);
        getColumnModel().getColumn(AbstractTableModel.TYPE_COLUMN_INDEX).setPreferredWidth(0);
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
        setIconLabels(activity);
    }

    @Override
    protected void showDetailsForSelectedRows() {
        setIconLabels(); // This to adress multiple selection in sub table
        panel.getDetailsPanel().showInfo(getDetailsForSelectedRows());
    }

    @Override
    protected ToDoList getList() {
        return ToDoList.getList();
    }

    @Override
    protected ToDoList getTableList() {
        return ToDoList.getTaskList();
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
        //getTitlePanel().setFont(getFont().deriveFont(Font.PLAIN, getFont().getSize() - 3));
        //getTitlePanel().setFont(getTitlePanel().getFont().deriveFont(Font.BOLD, getTitlePanel().getFont().getSize() - 3));
        String title = Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "ToDoListPanel.ToDo List");
        int rowCount = getModel().getRowCount(); // get row count on the model not the view !
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
                getTitlePanel().hideOverestimationButton();
                getTitlePanel().hideExternalButton();
                getTitlePanel().hideInternalButton();
                //getTitlePanel().hideDoneDoneButton();
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
                if (getSelectedRowCount() == 1) {
                    // Show buttons of the quick bar
                    // Hide overestimation options when estimated == 0 or real < estimated
                    Activity selectedActivity = getActivityFromSelectedRow();
                    if (panel.getPomodoro().inPomodoro()) {
                        getTitlePanel().switchRunningButton();
                    } else {
                        getTitlePanel().switchSelectedButton();
                    }
                    if (selectedActivity.getEstimatedPoms() > 0
                            && selectedActivity.getActualPoms() >= selectedActivity.getEstimatedPoms()) {
                        getTitlePanel().showOverestimationButton();
                    } else {
                        getTitlePanel().hideOverestimationButton();
                    }
                }
            }
        } else { // empty table
            getTitlePanel().hideSelectedButton();
            getTitlePanel().hideOverestimationButton();
            getTitlePanel().hideExternalButton();
            getTitlePanel().hideInternalButton();
            //getTitlePanel().hideDoneDoneButton();
        }
        if (canCreateUnplannedTask()) {
            getTitlePanel().showUnplannedButton();
        } else {
            getTitlePanel().hideUnplannedButton();
        }
        if (canCreateInterruptions()) {
            getTitlePanel().showExternalButton();
            getTitlePanel().showInternalButton();
        } else {
            getTitlePanel().hideExternalButton();
            getTitlePanel().hideInternalButton();
        }
        /*if (canBeDone() && getSelectedRowCount() == 1) {
         getTitlePanel().showDoneDoneButton();
         } else {
         getTitlePanel().hideDoneDoneButton();
         }*/
        if (MySQLConfigLoader.isValid()) { // Remote mode (using MySQL database)
            getTitlePanel().showRefreshButton(); // end of the line
        }
        // Update title
        getTitlePanel().setText("<html>" + title + "</html>");
        getTitlePanel().repaint();
    }

    // Can't delete tasks
    @Override
    public void deleteTasks() {
    }

    // only tasks can be moved
    @Override
    public void moveTask(int rowIndex) {
        Activity activity = getActivityFromRowIndex(rowIndex);
        getList().moveToActivtyList(activity); // move to ActivityList
        removeRow(rowIndex);
        if (getTableList().isEmpty()
                && panel.getPomodoro().getTimer().isRunning()) { // break running
            panel.getPomodoro().stop();
            panel.getTimerPanel().setStartEnv();
        }
    }

    // only tasks can be completed
    @Override
    public void completeTask(int rowIndex) {
        Activity activity = getActivityFromRowIndex(rowIndex);
        getList().completeToReportList(activity);
        removeRow(rowIndex);
        if (getTableList().isEmpty()
                && panel.getPomodoro().getTimer().isRunning()) { // break running
            panel.getPomodoro().stop();
            panel.getTimerPanel().setStartEnv();
        }
    }

    // Can't duplicate tasks
    @Override
    public void duplicateTask() {
    }

    @Override
    public void createUnplannedTask() {
        createUnplannedTask(new Activity());
    }

    public void createUnplannedTask(Activity activity) {
        activity.setEstimatedPoms(0);
        activity.setIsUnplanned(true);
        activity.setName("(U) " + Labels.getString("Common.Unplanned"));
        getList().add(activity);
        int row = insertRow(activity);
        editTitleCellAtRowIndex(row);
        panel.getTabbedPane().selectEditTab(); // open edit tab
    }

    @Override
    public void createInternalInterruption() {
        createInternalInterruption(new Activity());
    }

    public void createInternalInterruption(Activity activity) {
        // Interruptions : update current/running pomodoro
        if (canCreateInterruptions()) {
            Activity currentToDo = panel.getPomodoro().getCurrentToDo();
            currentToDo.incrementInternalInter();
            currentToDo.databaseUpdate();
            activity.setEstimatedPoms(0);
            activity.setIsUnplanned(true);
            activity.setName("(I) " + Labels.getString("ToDoListPanel.Internal"));
            getList().add(activity);
            int row = insertRow(activity);
            editTitleCellAtRowIndex(row);
            panel.getTabbedPane().selectEditTab(); // open edit tab
        }
    }

    @Override
    public void createExternalInterruption() {
        createExternalInterruption(new Activity());
    }

    public void createExternalInterruption(Activity activity) {
        // Interruptions : update current/running pomodoro
        if (canCreateInterruptions()) {
            Activity currentToDo = panel.getPomodoro().getCurrentToDo();
            currentToDo.incrementInter();
            currentToDo.databaseUpdate();
            activity.setEstimatedPoms(0);
            activity.setIsUnplanned(true);
            activity.setName("(E) " + Labels.getString("ToDoListPanel.External"));
            getList().add(activity);
            int row = insertRow(activity);
            editTitleCellAtRowIndex(row);
            panel.getTabbedPane().selectEditTab(); // open edit tab
        }
    }

    protected boolean canCreateInterruptions() {
        return panel.getPomodoro().inPomodoro() && panel.getPomodoro().getTimer().isRunning(); // no interruptions during pauses
    }

    protected boolean canBeDone() {
        return !panel.getPomodoro().inPomodoro(); // no done-done during pomodoros
    }

    protected boolean canCreateUnplannedTask() {
        return true; // anytime
    }

    // Overestimation only when estimated >0 and real >= estimated
    @Override
    public void overestimateTask(int poms) {
        Activity selectedToDo = getActivityFromSelectedRow();
        if (selectedToDo.getEstimatedPoms() > 0
                && selectedToDo.getActualPoms() >= selectedToDo.getEstimatedPoms()) {
            // Overestimation
            selectedToDo.setOverestimatedPoms(selectedToDo.getOverestimatedPoms() + poms);
            getList().update(selectedToDo);
            selectedToDo.databaseUpdate();
            if (selectedToDo.isSubTask()) {
                panel.getMainTable().addPomsToSelectedRow(0, 0, poms);
                panel.getMainTable().setTitle();
            }
            repaint();
            setTitle();
            // update details panel
            panel.getDetailsPanel().selectInfo(selectedToDo);
            panel.getDetailsPanel().showInfo();
            setIconLabels();
        }
    }

    @Override
    public void reorderByPriority() {
        getTableList().reorderByPriority(); // reordering the list of tasks or sutasks
        // Priorities have changed: the table must be updated
        updatePriorities();
    }

    public void updatePriorities() {
        for (int row = 0; row < getModel().getRowCount(); row++) {
            Activity activity = getActivityFromRowIndex(row);
            getModel().setValueAt(activity.getPriority(), convertRowIndexToModel(row), AbstractTableModel.PRIORITY_COLUMN_INDEX); // in view
        }
    }

    public void setIconLabels() {
        setIconLabels(getActivityFromSelectedRow());
    }

    public void setIconLabels(Activity selectedToDo) {
        if (panel.getMainTable().getModel().getRowCount() > 0) { // main table not empty
            Activity currentToDo = panel.getPomodoro().getCurrentToDo();
            Color defaultForegroundColor = ColorUtil.BLACK;
            if (selectedToDo != null
                    && selectedToDo.getId() == panel.getCurrentTable().getActivityIdFromSelectedRow()) {
                panel.getDetailsPanel().getIconPanel().setBackground(Main.selectedRowColor);
                panel.getCommentPanel().getIconPanel().setBackground(Main.selectedRowColor);
                panel.getEditPanel().getIconPanel().setBackground(Main.selectedRowColor);
            } else {
                panel.getDetailsPanel().getIconPanel().setBackground(Main.hoverRowColor);
                panel.getCommentPanel().getIconPanel().setBackground(Main.hoverRowColor);
                panel.getEditPanel().getIconPanel().setBackground(Main.hoverRowColor);
            }
            if (panel.getPomodoro().inPomodoro()) {
                ToDoIconPanel.showIconPanel(panel.getDetailsPanel().getIconPanel(), currentToDo, Main.taskRunningColor);
                ToDoIconPanel.showIconPanel(panel.getCommentPanel().getIconPanel(), currentToDo, Main.taskRunningColor);
                ToDoIconPanel.showIconPanel(panel.getEditPanel().getIconPanel(), currentToDo, Main.taskRunningColor);
            }
            if (selectedToDo != null
                    && getSelectedRowCount() <= 1) { // no selection (sub-table) or single selection
                if (panel.getPomodoro().inPomodoro() && selectedToDo.getId() != currentToDo.getId()) {
                    ToDoIconPanel.showIconPanel(panel.getDetailsPanel().getIconPanel(), selectedToDo, selectedToDo.isFinished() ? Main.taskFinishedColor : defaultForegroundColor);
                    ToDoIconPanel.showIconPanel(panel.getCommentPanel().getIconPanel(), selectedToDo, selectedToDo.isFinished() ? Main.taskFinishedColor : defaultForegroundColor);
                    ToDoIconPanel.showIconPanel(panel.getEditPanel().getIconPanel(), selectedToDo, selectedToDo.isFinished() ? Main.taskFinishedColor : defaultForegroundColor);
                } else if (!panel.getPomodoro().inPomodoro()) {
                    ToDoIconPanel.showIconPanel(panel.getDetailsPanel().getIconPanel(), selectedToDo, selectedToDo.isFinished() ? Main.taskFinishedColor : defaultForegroundColor);
                    ToDoIconPanel.showIconPanel(panel.getCommentPanel().getIconPanel(), selectedToDo, selectedToDo.isFinished() ? Main.taskFinishedColor : defaultForegroundColor);
                    ToDoIconPanel.showIconPanel(panel.getEditPanel().getIconPanel(), selectedToDo, selectedToDo.isFinished() ? Main.taskFinishedColor : defaultForegroundColor);
                }
            } else if (getSelectedRowCount() > 1) { // multiple selection
                ToDoIconPanel.clearIconPanel(panel.getDetailsPanel().getIconPanel());
                ToDoIconPanel.clearIconPanel(panel.getCommentPanel().getIconPanel());
                ToDoIconPanel.clearIconPanel(panel.getEditPanel().getIconPanel());
            }
        } else { // empty list
            ToDoIconPanel.clearIconPanel(panel.getDetailsPanel().getIconPanel());
            ToDoIconPanel.clearIconPanel(panel.getCommentPanel().getIconPanel());
            ToDoIconPanel.clearIconPanel(panel.getEditPanel().getIconPanel());
        }
    }

    @Override
    public void scrollToSelectedRows() {
        if (panel.getPomodoro().inPomodoro()) {
            for (int row = 0; row < panel.getMainTable().getModel().getRowCount(); row++) {
                // Scroll to the currentToDo task or, if the currentToDo is a subtask, scroll to its parent task AND select to display the subtasks
                if (panel.getPomodoro().getCurrentToDo().getId() == panel.getMainTable().getActivityIdFromRowIndex(row)
                        || (panel.getPomodoro().getCurrentToDo().isSubTask()
                        && panel.getPomodoro().getCurrentToDo().getParentId() == panel.getMainTable().getActivityIdFromRowIndex(row))) {
                    panel.getMainTable().scrollToRowIndex(row);
                    panel.getMainTable().setRowSelectionInterval(row, row);
                    break;
                }
            }
            if (panel.getPomodoro().getCurrentToDo().isSubTask()) {
                for (int row = 0; row < panel.getSubTable().getModel().getRowCount(); row++) {
                    // Scroll to the currentToDo subtask AND select
                    if (panel.getPomodoro().getCurrentToDo().getId() == panel.getSubTable().getActivityIdFromRowIndex(row)) {
                        panel.getSubTable().scrollToRowIndex(row);
                        panel.getSubTable().setRowSelectionInterval(row, row);
                        break;
                    }
                }
            } else { // subtask not running but selected
                panel.getSubTable().scrollToSelectedRow();
            }
        } else {
            super.scrollToSelectedRows();
        }
    }

    @Override
    public void moveRightTasks() {
        if (canMoveTasks()) {
            CompleteToDoButton completeToDoButton = new CompleteToDoButton("", (ToDoPanel) panel);
            completeToDoButton.doClick();
        }
    }

    @Override
    public void moveLeftTasks() {
        if (canMoveTasks()) {
            MoveToDoButton moveToDoButton = new MoveToDoButton("", (ToDoPanel) panel);
            moveToDoButton.doClick();
        }
    }

    // only tasks can be reopened  
    protected boolean canMoveTasks() {
        return getSelectedRowCount() > 0;
    }
}
