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
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.SystemUtils;
import org.mypomodoro.Main;
import org.mypomodoro.gui.IListPanel;
import org.mypomodoro.gui.ImageIcons;
import org.mypomodoro.gui.MainPanel;
import org.mypomodoro.util.Labels;
import org.mypomodoro.util.WaitCursor;

/**
 * Delete button
 *
 */
public class MoveSubtaskButton extends TabPanelButton {

    public MoveSubtaskButton(final String title, final String message, final IListPanel panel) {
        super(Labels.getString("Common.Delete"));
        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            setToolTipText("BACKSPACE");
        } else {
            setToolTipText("DEL");
        }
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final int selectedRowCount = panel.getSubTable().getSelectedRowCount();
                if (selectedRowCount > 0) {
                    new Thread() { // This new thread is necessary for updating the progress bar
                        @Override
                        public void run() {
                            int reply = JOptionPane.showConfirmDialog(Main.gui, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, ImageIcons.DIALOG_ICON);
                            if (reply == JOptionPane.YES_OPTION) {
                                // Disable button
                                setEnabled(false);
                                // Set progress bar
                                MainPanel.progressBar.setVisible(true);
                                MainPanel.progressBar.getBar().setValue(0);
                                MainPanel.progressBar.getBar().setMaximum(selectedRowCount);
                                int increment = 0;
                                int[] rows = panel.getSubTable().getSelectedRows();
                                for (int row : rows) {
                                    if (!MainPanel.progressBar.isStopped()) {
                                        // removing a row requires decreasing  the row index number
                                        row = row - increment;
                                        panel.getSubTable().moveSubtaskToMainTable(row);
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
        });
    }
}
