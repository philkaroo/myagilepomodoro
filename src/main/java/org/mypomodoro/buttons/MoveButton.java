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
package org.mypomodoro.buttons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.mypomodoro.Main;
import org.mypomodoro.gui.IListPanel;
import org.mypomodoro.gui.ImageIcons;
import org.mypomodoro.gui.MainPanel;
import org.mypomodoro.gui.activities.ActivitiesPanel;
import org.mypomodoro.gui.reports.ReportsPanel;
import org.mypomodoro.model.Activity;
import org.mypomodoro.model.ToDoList;
import org.mypomodoro.util.Labels;
import org.mypomodoro.util.WaitCursor;

/**
 * Move button for activities and reports panels. For ToDo panel, see
 * MoveToDoButton.
 *
 */
public class MoveButton extends TabPanelButton {

    public MoveButton(String label, final IListPanel panel) {
        super(label);
        if (panel instanceof ActivitiesPanel) { // move to ToDo list
            setToolTipText(Labels.getString("Common.Move") + " (SHIFT + >)");
        } else if (panel instanceof ReportsPanel) { // reopen tasks
            setToolTipText(Labels.getString("ReportListPanel.Reopen") + " (SHIFT + <)");
        }
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                move(panel);
            }
        });
    }

    public void move(final IListPanel panel) {
        final int selectedRowCount = panel.getCurrentTable().getSelectedRowCount();
        if (selectedRowCount > 0) {
            new Thread() { // This new thread is necessary for updating the progress bar
                @Override
                public void run() {
                    if (!WaitCursor.isStarted()) {
                        // Start wait cursor
                        WaitCursor.startWaitCursor();
                        // Disable button
                        setEnabled(false);
                        // Set progress bar
                        MainPanel.progressBar.setVisible(true);
                        MainPanel.progressBar.getBar().setValue(0);
                        MainPanel.progressBar.getBar().setMaximum(selectedRowCount);
                        // SKIP optimisation -move all tasks at once- to take benefice of the progress bar; slower but better for the user)
                        /*if (selectedRowCount == panel.getCurrentTable().getRowCount()
                         && panel instanceof ReportsPanel) { // reopen all at once                
                         panel.moveAll();
                         panel.refresh();
                         } else {*/
                        int increment = 0;
                        int[] rows = panel.getCurrentTable().getSelectedRows();
                        // clear current selection before adding selection row to main table
                        if (panel instanceof ActivitiesPanel) { // move to ToDo list
                            Main.gui.getToDoPanel().getMainTable().clearSelection();
                        } else if (panel instanceof ReportsPanel) { // reopen tasks
                            Main.gui.getActivityListPanel().getMainTable().clearSelection();
                        }
                        for (int row : rows) {
                            if (!MainPanel.progressBar.isStopped()) {
                                // removing a row requires decreasing the row index number
                                row = row - increment;
                                Activity selectedActivity = panel.getCurrentTable().getActivityFromRowIndex(row);
                                if (panel instanceof ActivitiesPanel && !Main.preferences.getAgileMode()) {
                                    String activityName = selectedActivity.getName().length() > 25 ? selectedActivity.getName().substring(0, 25) + "..." : selectedActivity.getName();
                                    if (selectedActivity.isDateInFuture()) {
                                        String title = Labels.getString("ActivityListPanel.Add activity to ToDo List");
                                        String message = Labels.getString("ActivityListPanel.The date of activity {0} is not today. Proceed anyway?", activityName);
                                        int reply = JOptionPane.showConfirmDialog(Main.gui, message,
                                                title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, ImageIcons.DIALOG_ICON);
                                        if (reply == JOptionPane.NO_OPTION) {
                                            continue; // go to the next one
                                        } else if (reply == JOptionPane.CLOSED_OPTION) {
                                            break;
                                        }
                                    }
                                    if (isMaxNbTotalEstimatedPomReached(selectedActivity)) {
                                        String title = Labels.getString("ActivityListPanel.Add activity to ToDo List");
                                        String message = Labels.getString(
                                                "ActivityListPanel.Max nb of pomodoros per day reached ({0}). Proceed anyway?",
                                                Main.preferences.getMaxNbPomPerDay());
                                        int reply = JOptionPane.showConfirmDialog(Main.gui, message,
                                                title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, ImageIcons.DIALOG_ICON);
                                        if (reply != JOptionPane.YES_OPTION) {
                                            break; // get out of the loop
                                        }
                                    }
                                }
                                panel.getCurrentTable().moveTask(row);
                                if (panel instanceof ActivitiesPanel) { // move to ToDo list
                                    Main.gui.getToDoPanel().getMainTable().addRow(selectedActivity); // add selection row to main table
                                } else if (panel instanceof ReportsPanel) { // reopen tasks
                                    Main.gui.getActivityListPanel().getMainTable().addRow(selectedActivity); // add selection row to main table
                                }
                                increment++;
                                final int progressValue = increment;
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainPanel.progressBar.getBar().setValue(progressValue); // % - required to see the progress                                    
                                        MainPanel.progressBar.getBar().setString(Integer.toString(progressValue) + " / " + Integer.toString(selectedRowCount)); // task
                                    }
                                });
                            }
                        }
                        //}
                        // Close progress bar
                        final int progressCount = increment;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                MainPanel.progressBar.getBar().setString(Labels.getString("ProgressBar.Done") + " (" + progressCount + ")");
                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            sleep(1000); // wait one second before hiding the progress bar
                                        } catch (InterruptedException ex) {
                                            Main.logger.error("", ex);
                                        }
                                        // hide progress bar
                                        MainPanel.progressBar.getBar().setString(null);
                                        MainPanel.progressBar.setVisible(false);
                                        MainPanel.progressBar.setStopped(false);
                                    }
                                }.start();
                            }
                        });
                        // Enable button
                        setEnabled(true);
                        // Stop wait cursor
                        WaitCursor.stopWaitCursor();
                    }
                }
            }.start();
        }
    }

    private boolean isMaxNbTotalEstimatedPomReached(Activity activity) {
        int nbTotalEstimatedPom = ToDoList.getList().getTasksNbTotalEstimatedPom();
        int nbTotalEstimatedPomWithActivity = nbTotalEstimatedPom + activity.getEstimatedPoms() + activity.getOverestimatedPoms();
        return nbTotalEstimatedPom <= Main.preferences.getMaxNbPomPerDay() && nbTotalEstimatedPomWithActivity > Main.preferences.getMaxNbPomPerDay();
    }
}
