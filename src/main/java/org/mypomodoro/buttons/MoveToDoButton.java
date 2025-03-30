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
import static java.lang.Thread.sleep;
import javax.swing.SwingUtilities;
import org.mypomodoro.Main;
import org.mypomodoro.gui.MainPanel;
import org.mypomodoro.gui.todo.ToDoPanel;
import org.mypomodoro.model.Activity;
import org.mypomodoro.util.Labels;
import org.mypomodoro.util.WaitCursor;

/**
 * Move button to move tasks back to Activity list
 *
 */
public class MoveToDoButton extends TabPanelButton {

    public MoveToDoButton(String label, final ToDoPanel panel) {
        super(label);
        setToolTipText(Labels.getString("Common.Move") + " (SHIFT + <)");
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                move(panel);
            }
        });
    }

    public void move(final ToDoPanel panel) {
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
                        /*if (!panel.getPomodoro().inPomodoro()
                         && panel.getCurrentTable().getSelectedRowCount() == panel.getCurrentTable().getRowCount()) { // complete all at once                       
                         panel.moveAll();
                         panel.refresh();
                         } else {*/
                        int increment = 0;
                        int[] rows = panel.getCurrentTable().getSelectedRows();
                        // clear current selection before adding selection row to main table
                        Main.gui.getActivityListPanel().getMainTable().clearSelection();
                        for (int row : rows) {
                            if (!MainPanel.progressBar.isStopped()) {
                                // removing a row requires decreasing the row index number
                                row = row - increment;
                                Activity selectedToDo = panel.getCurrentTable().getActivityFromRowIndex(row);
                                // excluding current running task
                                if (panel.getPomodoro().inPomodoro()
                                        && (selectedToDo.getId() == panel.getPomodoro().getCurrentToDo().getId()
                                        || selectedToDo.getId() == panel.getPomodoro().getCurrentToDo().getParentId())) {
                                    if (rows.length > 1) {
                                        continue;
                                    } else {
                                        break;
                                    }
                                }
                                panel.getCurrentTable().moveTask(row);
                                Main.gui.getActivityListPanel().getMainTable().addRow(selectedToDo); // add selection row to main table
                                increment++;
                                final int progressValue = increment;
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainPanel.progressBar.getBar().setValue(progressValue); // % - required to see the progress
                                        MainPanel.progressBar.getBar().setString(Integer.toString(progressValue) + " / " + (panel.getPomodoro().inPomodoro() ? Integer.toString(selectedRowCount - 1) : Integer.toString(selectedRowCount))); // task
                                    }
                                });
                            }
                        }
                        //}
                        // Indicate reordering by priority in progress bar
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                MainPanel.progressBar.getBar().setValue(MainPanel.progressBar.getBar().getMaximum());
                                MainPanel.progressBar.getBar().setString(Labels.getString("ProgressBar.Updating priorities"));
                            }
                        });
                        // When the list has a lot of tasks, the reorderByPriority method is very slow (probably) because there are now gaps in the index of the ToDo list due to previous deletion (removal) of tasks                            
                        panel.getCurrentTable().reorderByPriority();
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
}
