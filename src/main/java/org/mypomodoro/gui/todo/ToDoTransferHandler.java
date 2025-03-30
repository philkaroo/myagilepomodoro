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

import java.awt.datatransfer.Transferable;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.MOVE;
import javax.swing.table.DefaultTableModel;
import org.mypomodoro.Main;
import org.mypomodoro.gui.ImageIcons;
import org.mypomodoro.gui.MainPanel;
import org.mypomodoro.model.Activity;
import org.mypomodoro.util.Labels;
import org.mypomodoro.util.WaitCursor;

/**
 * Transfer Handler
 *
 */
public class ToDoTransferHandler extends TransferHandler {

    private final ToDoPanel panel;

    public ToDoTransferHandler(ToDoPanel panel) {
        this.panel = panel;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        return true;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        final JTable.DropLocation dropLocation = (JTable.DropLocation) info.getDropLocation();
        if (dropLocation.isInsertRow()) {
            if (info.getTransferable().isDataFlavorSupported(ToDoRowTransferable.DATA_ROW)) {
                if (!isPriorityColumnSorted()) {
                    String title = Labels.getString("ToDoListPanel.Sort by priority");
                    String message = Labels.getString("ToDoListPanel.ToDos must first be sorted by priority. Sort now?");
                    int reply = JOptionPane.showConfirmDialog(Main.gui, message, title,
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, ImageIcons.DIALOG_ICON);
                    if (reply == JOptionPane.OK_OPTION) {
                        // sort programatically the priority column                        
                        /*panel.getCurrentTable().setAutoCreateRowSorter(true);
                         DefaultRowSorter sorter = ((DefaultRowSorter) panel.getCurrentTable().getRowSorter());
                         ArrayList<SortKey> list = new ArrayList<SortKey>();
                         list.add(new RowSorter.SortKey(AbstractTableModel.PRIORITY_COLUMN_INDEX, SortOrder.ASCENDING));
                         sorter.setSortKeys(list);
                         sorter.sort(); // sort the view*/
                        // get the first row index of the selection (single or multiple)
                        int row = panel.getCurrentTable().convertRowIndexToModel(panel.getCurrentTable().getSelectedRow());
                        if (panel.getCurrentTable().equals(panel.getMainTable())) {
                            ((ToDoTableModel) panel.getMainTable().getModel()).update();
                            panel.getMainTable().setTableHeader();
                        } else {
                            ((ToDoSubTableModel) panel.getSubTable().getModel()).update(panel.getMainTable().getActivityIdFromSelectedRow());
                        }
                        panel.getCurrentTable().setColumnModel();
                        panel.getCurrentTable().setTitle();
                        // reselect row
                        panel.getCurrentTable().addRowSelectionInterval(row, row);
                        panel.getCurrentTable().scrollRectToVisible(panel.getCurrentTable().getCellRect(row, 0, true));
                    }
                } else if (isContinuousSelection()) {
                    final int selectedRowCount = panel.getCurrentTable().getSelectedRowCount();
                    new Thread() { // This new thread is necessary for updating the progress bar
                        @Override
                        public void run() {
                            if (!WaitCursor.isStarted()) {
                                try {
                                    // Start wait cursor
                                    WaitCursor.startWaitCursor();
                                    // Set progress bar
                                    MainPanel.progressBar.setVisible(true);
                                    MainPanel.progressBar.getBar().setValue(0);
                                    MainPanel.progressBar.getBar().setMaximum(selectedRowCount);
                                    // moving row with drag and drop
                                    int[] fromRows = panel.getCurrentTable().getSelectedRows();
                                    int toRow = dropLocation.getRow();
                                    toRow = (toRow < fromRows[0]) ? toRow : toRow - fromRows.length;
                                    // Error case: ArrayIndexOutOfBoundsException occurs here when trying to move rows in the main table when the current table is the subtable
                                    ((DefaultTableModel) panel.getCurrentTable().getModel()).moveRow(fromRows[0], fromRows[fromRows.length - 1], toRow); // fires tableChanged event 
                                    for (int row = 0; row < panel.getCurrentTable().getModel().getRowCount(); row++) {
                                        Activity activity = panel.getCurrentTable().getActivityFromRowIndex(row);
                                        int priority = row + 1;
                                        if (activity.getPriority() != priority) { // set new priorities
                                            activity.setPriority(priority);
                                            activity.databaseUpdate();
                                            panel.getCurrentTable().getList().update(activity);
                                        }
                                    }
                                    // Indicate reordering by priority in progress bar
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainPanel.progressBar.getBar().setValue(MainPanel.progressBar.getBar().getMaximum());
                                            MainPanel.progressBar.getBar().setString(Labels.getString("ProgressBar.Updating priorities"));
                                        }
                                    });
                                    panel.getCurrentTable().updatePriorities();
                                    // Close progress bar
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainPanel.progressBar.getBar().setString(Labels.getString("ProgressBar.Done"));
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
                                                }
                                            }.start();
                                        }
                                    });
                                    // Stop wait cursor
                                    WaitCursor.stopWaitCursor();
                                    // After cursor stops, reset interval of selected row(s)                                
                                    panel.getCurrentTable().getSelectionModel().setSelectionInterval(toRow, toRow + fromRows.length - 1);
                                } catch (ArrayIndexOutOfBoundsException ignored) {
                                    // Stop wait cursor
                                    WaitCursor.stopWaitCursor();
                                    // Close progress bar
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainPanel.progressBar.getBar().setString(null);
                                            MainPanel.progressBar.setVisible(false);
                                        }
                                    });
                                }
                            }
                        }
                    }.start();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected Transferable createTransferable(JComponent cp) {
        int row = panel.getCurrentTable().getSelectedRow();
        int colCount = panel.getCurrentTable().getColumnCount();
        ArrayList<Object> a = new ArrayList<Object>(colCount);
        for (int i = 0; i < colCount; i++) {
            a.add(panel.getCurrentTable().getModel().getValueAt(panel.getCurrentTable().convertRowIndexToModel(row), i));
        }
        return new ToDoRowTransferable(a, row);
    }

    @Override
    public int getSourceActions(JComponent cp) {
        return MOVE;
    }

    private boolean isPriorityColumnSorted() {
        boolean sorted = true;
        for (int i = 0; i < panel.getCurrentTable().getModel().getRowCount() - 1; i++) {
            // Look for the value of the priority in the View while column priority might have been moved around                    
            if ((Integer) panel.getCurrentTable().getValueAt(i, panel.getCurrentTable().convertColumnIndexToView(0)) != (Integer) panel.getCurrentTable().getValueAt(i + 1, panel.getCurrentTable().convertColumnIndexToView(0)) - 1) {
                sorted = false;
                break;
            }
        }
        return sorted;
    }

    /**
     * Checks gaps in the selection
     *
     * @return true if selection is continuous
     */
    private boolean isContinuousSelection() {
        boolean continuous = true;
        int[] rows = panel.getCurrentTable().getSelectedRows();
        int row = rows[0];
        for (int i = 1; i < rows.length; i++) {
            if (row + 1 != rows[i]) {
                continuous = false;
                break;
            }
            row++;
        }
        return continuous;
    }
}
