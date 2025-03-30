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
package org.mypomodoro.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import org.apache.commons.lang3.SystemUtils;
import org.jdesktop.swingx.JXTable;
import org.mypomodoro.Main;
import org.mypomodoro.db.ActivitiesDAO;
import org.mypomodoro.gui.activities.ActivitiesSubTable;
import org.mypomodoro.gui.reports.ReportsSubTable;
import org.mypomodoro.gui.reports.ReportsTable;
import org.mypomodoro.gui.todo.ToDoSubTable;
import org.mypomodoro.gui.todo.ToDoTable;
import org.mypomodoro.model.AbstractActivities;
import org.mypomodoro.model.Activity;
import org.mypomodoro.util.ColorUtil;
import org.mypomodoro.util.DateUtil;
import static org.mypomodoro.util.TimeConverter.getLength;

/**
 *
 *
 */
public abstract class AbstractTable extends JXTable {

    private final IListPanel panel;
    protected int mouseHoverRow = 0;
    protected InputMap im;

    public AbstractTable(AbstractTableModel model, final IListPanel panel) {
        super(model);

        this.panel = panel;

        setBackground(Main.tableBackgroundColor);
        /*setSelectionBackground(Main.selectedRowColor);
         setForeground(ColorUtil.BLACK);
         setSelectionForeground(ColorUtil.BLACK);*/

        // Row height
        setRowHeight(30);

        // Prevent key events from editing the cell (this meanly to avoid conflicts with shortcuts)
        // The selecting editor is used to automatically select the whole text when editing
        JTextField titleTextField = new JTextField();
        titleTextField.setCaretColor(new JTextField().getForeground()); // Set colors according to input settings and themes
        SelectingEditor editor = new SelectingEditor(titleTextField) {

            @Override
            public boolean isCellEditable(EventObject e) {
                boolean cellEditable = super.isCellEditable(e);
                if (e instanceof KeyEvent) {
                    cellEditable = false;
                }
                return cellEditable;
            }
        };
        setDefaultEditor(Object.class, editor);

        // Manage mouse hovering
        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                if (rowIndex >= 0) {
                    if (getSelectedRowCount() <= 1
                            && mouseHoverRow != rowIndex) { // no multiple selection
                        showInfoForRowIndex(rowIndex);
                        mouseHoverRow = rowIndex;
                    } else if (getSelectedRowCount() > 1) { // multiple selection
                        // Display info (list of selected tasks)                            
                        showDetailsForSelectedRows();
                    }
                } else {
                    setToolTipText(null); // this way tooltip won't stick
                    mouseHoverRow = -1;
                }
            }
        });
        // This is to address the case/event when the mouse exit the table
        addMouseListener(new MouseAdapter() {

            // Way to select a row of the main table that is already selected in order to trigger AbstractListSelectionListener            
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                if (rowIndex >= 0
                        && panel.getMainTable().equals(AbstractTable.this)
                        && getSelectedRowCount() == 1
                        && rowIndex == getSelectedRow()) {
                    clearSelection(); // clear row selected in main table...
                    setRowSelectionInterval(rowIndex, rowIndex); // ... then reselect row to trigger the listener
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (panel.getCurrentTable().getSelectedRowCount() == 1) { // one selected row either on the main or the sub table
                    Activity activity = panel.getCurrentTable().getActivityFromSelectedRow();
                    // Activity may be null when hovering the cursor over the tasks while deleting/moving it
                    if (activity != null) {
                        showInfo(activity);
                    }
                } else if (panel.getCurrentTable().getSelectedRowCount() > 1) { // multiple selection
                    // Display info (list of selected tasks)                        
                    panel.getCurrentTable().showDetailsForSelectedRows();
                }
                mouseHoverRow = -1;
            }
        });

        // Activate Delete key stroke
        im = getInputMap(JTable.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "Delete"); // for MAC
        } else {
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Delete");
        }
        class deleteAction extends AbstractAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                deleteTasks();
            }
        }
        am.put("Delete", new deleteAction());

        // Activate Shift + '>'                
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, KeyEvent.SHIFT_MASK), "Move right"); // move to ToDoList and complete
        class moveRightAction extends AbstractAction {

            final IListPanel panel;

            public moveRightAction(IListPanel panel) {
                this.panel = panel;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                moveRightTasks(); // send to ToDo or complete
            }
        }
        am.put("Move right", new moveRightAction(panel));

        // Activate Shift + '<'
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, KeyEvent.SHIFT_MASK), "Move left"); // send back to ActivityList and reopen
        class moveLeftAction extends AbstractAction {

            final IListPanel panel;

            public moveLeftAction(IListPanel panel) {
                this.panel = panel;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                moveLeftTasks(); // send back to Activity or reopen
            }
        }
        am.put("Move left", new moveLeftAction(panel));

        // Activate Control A
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK), "Control A");
        class selectAllAction extends AbstractAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectAll();
            }
        }
        am.put("Control A", new selectAllAction());

        // Activate Control T (create new task)        
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK), "Control T");
        class create extends AbstractAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                createNewTask();
            }
        }
        am.put("Control T", new create());

        // Activate Control D (duplicate task)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK), "Control D");
        class duplicate extends AbstractAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                duplicateTask();
            }
        }
        am.put("Control D", new duplicate());

        // Activate Control R (scroll back to the selected task)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK), "Control G");
        class scrollBackToTask extends AbstractAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                scrollToSelectedRows();
            }
        }
        am.put("Control G", new scrollBackToTask());

        // Activate Control U (quick unplanned task)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK), "Control U");
        class createUnplanned extends AbstractAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                createUnplannedTask();
            }
        }
        am.put("Control U", new createUnplanned());

        // Activate Control I (quick internal interruption)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK), "Control I");
        class createInternal extends AbstractAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                createInternalInterruption();
            }
        }
        am.put("Control I", new createInternal());

        // Activate Control E (quick internal interruption)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK), "Control E");
        class createExternal extends AbstractAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                createExternalInterruption();
            }
        }
        am.put("Control E", new createExternal());
    }

    // Editor that select the whole text when editing
    public class SelectingEditor extends DefaultCellEditor {

        public SelectingEditor(JTextField textField) {
            super(textField);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
            if (c instanceof JTextComponent) {
                final JTextComponent jtc = (JTextComponent) c;
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        jtc.requestFocus();
                        jtc.selectAll();
                    }
                });
            }
            return c;
        }
    }

    // List selection listener
    // when a row is selected, the table becomes the 'current' table
    protected abstract class AbstractListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getSource() == getSelectionModel() && e.getFirstIndex() >= 0) { // See if this is a valid table selection                
                if (!e.getValueIsAdjusting()) { // ignoring the deselection event 
                    if (!panel.getCurrentTable().equals(AbstractTable.this)) { // switch main table / sub table
                        panel.setCurrentTable(AbstractTable.this); // set new current table                        
                    }
                    customValueChanged(e);
                    setTitle();
                } else if (getModel().getRowCount() == 0) {
                    if (panel.getMainTable().getModel().getRowCount() > 0
                            && !panel.getMainTable().equals(AbstractTable.this)) { // the sub table is empty so we reselect the row currently selected in the main table
                        int rowIndex = panel.getMainTable().getSelectedRow();
                        panel.getMainTable().clearSelection(); // clear first...
                        panel.getMainTable().setRowSelectionInterval(rowIndex, rowIndex); // ...then reselect
                    }
                    setTitle();
                }
            }
        }

        public abstract void customValueChanged(ListSelectionEvent e);
    }

    protected abstract class AbstractTableModelListener implements TableModelListener {

        @Override
        public void tableChanged(TableModelEvent e) {
            if (e.getFirstRow() >= 0
                    && e.getColumn() >= 0
                    && e.getType() == TableModelEvent.UPDATE) {
                customTableChanged(e);
                // reset title
                setTitle();
            }
        }

        public abstract void customTableChanged(TableModelEvent e);
    }

    public int getActivityIdFromSelectedRow() {
        int activityId = -1;
        try {
            activityId = (Integer) getModel().getValueAt(convertRowIndexToModel(getSelectedRow()), AbstractTableModel.ACTIVITYID_COLUMN_INDEX);
        } catch (IndexOutOfBoundsException ignored) {
            // do nothing
            // this may happen when hovering the cursor over the tasks while deleting/moving it 
        }
        return activityId;
    }

    public Activity getActivityFromSelectedRow() {
        return getList().getById(getActivityIdFromSelectedRow()); // return null if not found
    }

    public int getActivityIdFromRowIndex(int rowIndex) {
        int activityId = -1;
        try {
            activityId = (Integer) getModel().getValueAt(convertRowIndexToModel(rowIndex), AbstractTableModel.ACTIVITYID_COLUMN_INDEX);
        } catch (IndexOutOfBoundsException ignored) {
            // do nothing
            // this may happen when hovering the cursor over the tasks while deleting/moving it 
        }
        return activityId;
    }

    public Activity getActivityFromRowIndex(int rowIndex) {
        return getList().getById(getActivityIdFromRowIndex(rowIndex)); // return null if not found
    }

    public Activity getActivityById(int id) {
        return getList().getById(id);
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = null;
        try {
            c = super.prepareRenderer(renderer, row, column);
            if (isRowSelected(row)) {
                ((JComponent) c).setBackground(Main.selectedRowColor);
                // using ((JComponent) c).getFont() to preserve current font (eg strike through)
                ((JComponent) c).setFont(((JComponent) c).getFont().deriveFont(Font.BOLD));
                ((JComponent) c).setBorder(new MatteBorder(1, 0, 1, 0, Main.rowBorderColor));
            } else if (row == mouseHoverRow) {
                ((JComponent) c).setBackground(Main.hoverRowColor);
                ((JComponent) c).setFont(((JComponent) c).getFont().deriveFont(Font.BOLD));
                Component[] comps = ((JComponent) c).getComponents();
                for (Component comp : comps) { // sub-components (combo boxes)
                    comp.setFont(comp.getFont().deriveFont(Font.BOLD));
                }
                ((JComponent) c).setBorder(new MatteBorder(1, 0, 1, 0, Main.rowBorderColor));
            } else {
                if (row % 2 == 0) { // odd
                    ((JComponent) c).setBackground(Main.oddRowColor); // This stays White despite the background or the current theme
                } else { // even
                    ((JComponent) c).setBackground(Main.evenRowColor);
                }
                ((JComponent) c).setBorder(null);
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {
            // do nothing
            // this may happen when hovering the cursor over the tasks while deleting/moving it        
        } catch (IndexOutOfBoundsException ignored) {
            // do nothing
            // this may happen when hovering the cursor over the tasks while deleting/moving it 
        }
        return c;
    }

    // Scroll to selected row(s) in the current table
    // scroll to the bottom of the selection or, at least, the last item on the list of selected rows
    public void scrollToSelectedRow() {
        int[] selectedRows = getSelectedRows();
        if (selectedRows.length > 0) {
            scrollToRowIndex(selectedRows[selectedRows.length - 1]);
        }
    }

    public void scrollToRowIndex(int row) {
        scrollRectToVisible(getCellRect(row, 0, true));
    }

    // Scroll to selected row(s) in the main table and the sub-table
    // scroll to the bottom of the selection or, at least, the last item on the list of selected rows
    public void scrollToSelectedRows() {
        int[] selectedRows = panel.getMainTable().getSelectedRows();
        if (selectedRows.length > 0) {
            panel.getMainTable().scrollToRowIndex(selectedRows[selectedRows.length - 1]);
        }
        if (panel.getSubTable() != null) { // check panel doesn't have a sub table
            int[] selectedRowsSubTable = panel.getSubTable().getSelectedRows();
            if (selectedRowsSubTable.length > 0) {
                panel.getSubTable().scrollToRowIndex(selectedRowsSubTable[selectedRowsSubTable.length - 1]);
            }
        }
    }

    // selected row BOLD
    public class CustomRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();
            value = " " + value + " ";
            JLabel renderer = (JLabel) defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            renderer.setForeground(ColorUtil.BLACK);
            renderer.setFont(isSelected ? getFont().deriveFont(Font.BOLD) : getFont());
            renderer.setHorizontalAlignment(SwingConstants.CENTER);
            Activity activity = getActivityFromRowIndex(row);
            if (activity != null) {
                if (Main.gui != null && table instanceof ToDoTable) {
                    if (Main.gui.getToDoPanel().getPomodoro().getCurrentToDo() != null
                            && (activity.getId() == Main.gui.getToDoPanel().getPomodoro().getCurrentToDo().getId()
                            || (activity.isTask() && activity.getId() == Main.gui.getToDoPanel().getPomodoro().getCurrentToDo().getParentId()))
                            && Main.gui.getToDoPanel().getPomodoro().inPomodoro()) {
                        renderer.setForeground(Main.taskRunningColor);
                    }
                }
                if (activity.isFinished()) {
                    renderer.setForeground(Main.taskFinishedColor);
                }
                // This way we prevent strike through in check panel table
                if (Main.gui != null && (
                        table instanceof ActivitiesSubTable
                        || table instanceof ToDoSubTable
                        || table instanceof ReportsSubTable
                        || table instanceof ReportsTable)) {
                    if ((activity.isCompleted() && activity.isSubTask()) || (activity.isDoneDone() && activity.isTask() && Main.preferences.getAgileMode())) {
                        Map<TextAttribute, Object> map = new HashMap<TextAttribute, Object>();
                        map.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                        renderer.setFont(getFont().deriveFont(map));
                        //renderer.setFont(getFont().deriveFont(map).deriveFont(Font.BOLD));
                        //renderer.setBorder(new MatteBorder(1, 1, 1, 1, ColorUtil.RED));
                        //renderer.setBackground(Color.LIGHT_GRAY);
                        //renderer.setOpaque(true);
                    }
                }
            }
            return renderer;
        }
    }

    public class UnplannedRenderer extends CustomRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if ((Boolean) value) {
                if (!getFont().canDisplay('\u2714')) { // unicode tick
                    renderer.setText("U");
                } else {
                    renderer.setText("\u2714");
                }
            } else {
                renderer.setText("");
            }
            return renderer;
        }
    }

    public class DateRenderer extends CustomRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!DateUtil.isSameDay((Date) value, new Date(0))) {
                renderer.setText(DateUtil.getShortFormatedDate((Date) value));
                String tooltipValue = DateUtil.getLongFormatedDate((Date) value);
                renderer.setToolTipText(tooltipValue);
            } else {
                renderer.setText(""); // empty text
                renderer.setToolTipText(null); // remove tooltip
            }
            return renderer;
        }
    }

    public class ActivityDateRenderer extends DateRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            int id = (Integer) table.getModel().getValueAt(table.convertRowIndexToModel(row), AbstractTableModel.ACTIVITYID_COLUMN_INDEX);
            Activity activity = getList().getById(id);
            if (activity != null) {
                if (activity.isSubTask()) {
                    if (activity.isCompleted()) {
                        value = activity.getDateCompleted();
                    } else {
                        value = new Date(0); // subtasks have no schedule date (only create date)
                    }
                }
            }
            JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (activity != null) {
                if (!Main.preferences.getAgileMode()) { // Pomodoro mode only
                    if (activity.isTask() && activity.isOverdue()) {
                        Map<TextAttribute, Object> map = new HashMap<TextAttribute, Object>();
                        map.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                        renderer.setFont(getFont().deriveFont(map));
                    }
                }
                String tooltipValue = DateUtil.getLongFormatedDate((Date) value);
                if (activity.isCompleted() && activity.isSubTask()) {
                    renderer.setToolTipText("<html><strike> " + tooltipValue + " </strike></html>");
                }
            }
            return renderer;
        }
    }

    public class ReportDateRenderer extends DateRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            int id = (Integer) table.getModel().getValueAt(table.convertRowIndexToModel(row), AbstractTableModel.ACTIVITYID_COLUMN_INDEX);
            Activity activity = getList().getById(id);
            if (activity != null) {
                if (activity.isDoneDone() && activity.isTask() && Main.preferences.getAgileMode()) {
                    value = activity.getDateDoneDone();
                } else if (activity.isCompleted()) {
                    value = activity.getDateCompleted();
                } else {
                    value = new Date(0);
                }
            }
            JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String tooltipValue = DateUtil.getLongFormatedDate((Date) value);
            if (activity != null && ((activity.isCompleted() && activity.isSubTask()) || (activity.isDoneDone() && activity.isTask() && Main.preferences.getAgileMode()))) {
                renderer.setToolTipText("<html><strike> " + tooltipValue + " </strike></html>");
            }
            return renderer;
        }
    }

    public class ToolTipRenderer extends CustomRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String tooltipValue = (String) value;
            if (!tooltipValue.isEmpty()) {
                int id = (Integer) table.getModel().getValueAt(table.convertRowIndexToModel(row), AbstractTableModel.ACTIVITYID_COLUMN_INDEX);
                Activity activity = getList().getById(id);
                if (activity != null && ((activity.isCompleted() && activity.isSubTask()) || (activity.isDoneDone() && activity.isTask() && Main.preferences.getAgileMode()))) {
                    tooltipValue = "<html><strike> " + tooltipValue + " </strike></html>";
                }
                renderer.setToolTipText(tooltipValue);
            }
            return renderer;
        }
    }

    public class TitleRenderer extends CustomRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            int id = (Integer) table.getModel().getValueAt(table.convertRowIndexToModel(row), AbstractTableModel.ACTIVITYID_COLUMN_INDEX);
            JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Activity activity = getList().getById(id);
            if (activity != null) {
                String textValue = "<html>";
                if (activity.getRecordedTime() > 0) {
                    textValue += "<span style=\"color:" + ColorUtil.toHex(Main.taskRunningColor) + "\">*</span>";
                    // Font size increased : "<span style=\"font-size:" + (renderer.getFont().getSize() + 10) + "pt;color:" + ColorUtil.toHex(Main.taskRunningColor) + "\">*</span>";                
                }
                if ((activity.isCompleted() && activity.isSubTask()) || (activity.isDoneDone() && activity.isTask() && Main.preferences.getAgileMode())) {
                    textValue += "<strike> " + (String) value + " </strike>";
                } else {
                    textValue += (String) value;
                }
                String tooltipValue = textValue;
                renderer.setText(textValue + "</html>");
                renderer.setToolTipText(tooltipValue + "</html>");
            }
            return renderer;
        }
    }

    public class EstimatedCellRenderer extends CustomRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int id = (Integer) table.getModel().getValueAt(table.convertRowIndexToModel(row), AbstractTableModel.ACTIVITYID_COLUMN_INDEX);
            Activity activity = getList().getById(id);
            if (activity != null) {
                int realpoms = activity.getActualPoms();
                int estimatedpoms = activity.getEstimatedPoms();
                int overestimatedpoms = activity.getOverestimatedPoms();
                String text = activity.getActualPoms() + " / " + activity.getEstimatedPoms() + (overestimatedpoms > 0 ? " + " + overestimatedpoms : "");
                renderer.setText(text);
                String tooltipValue = getLength(realpoms) + " / " + getLength(estimatedpoms + overestimatedpoms);
                if ((activity.isCompleted() && activity.isSubTask()) || (activity.isDoneDone() && activity.isTask() && Main.preferences.getAgileMode())) {
                    renderer.setToolTipText("<html><strike> " + tooltipValue + " </strike></html>");
                } else {
                    renderer.setToolTipText(tooltipValue);
                }
            }
            return renderer;
        }
    }

    public class StoryPointsCellRenderer extends CustomRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text;
            if (value.toString().equals("0.5")) {
                text = "1/2";
            } else {
                text = Math.round((Float) value) + ""; // used Math.round to display SP as integer (eg: 1.0 --> 1)
            }
            renderer.setText(text);
            return renderer;
        }
    }

    public class IterationCellRenderer extends CustomRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text = value.toString();
            if (value.toString().equals("-1")) {
                text = "";
            }
            renderer.setText(text);
            return renderer;
        }
    }

    public class Diff2CellRenderer extends CustomRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int id = (Integer) table.getModel().getValueAt(table.convertRowIndexToModel(row), AbstractTableModel.ACTIVITYID_COLUMN_INDEX);
            Activity activity = getList().getById(id);
            String text = value.toString();
            if (activity != null && activity.getOverestimatedPoms() == 0) {
                text = "";
            }
            renderer.setText(text);
            return renderer;
        }
    }

    @Override
    public AbstractTableModel getModel() {
        return (AbstractTableModel) super.getModel();
    }

    public abstract void setColumnModel();

    // This method is empty in sub table classes
    public void initTabs() {
        panel.getTabbedPane().initTabs(getModel().getRowCount());
    }

    // This method is empty in sub table classs
    protected void populateSubTable() {
        panel.populateSubTable(getActivityIdFromSelectedRow());
    }

    // This method is empty in sub table classs
    protected void emptySubTable() {
        panel.emptySubTable();
    }

    public TitlePanel getTitlePanel() {
        return panel.getTableTitlePanel();
    }

    protected abstract void showInfo(Activity activity);

    protected abstract void showDetailsForSelectedRows();

    protected String getDetailsForSelectedRows() {
        String info = "";
        int[] rows = getSelectedRows();
        for (int row : rows) {
            Activity activity = getActivityFromRowIndex(row);
            // Activity may be null when hovering the cursor over the tasks while deleting/moving it
            if (activity != null) {
                info += activity.getName() + "<br>";
            }
        }
        return info;
    }

    protected void showInfoForSelectedRow() {
        Activity activity = getActivityFromSelectedRow();
        // Activity may be null when hovering the cursor over the tasks while deleting/moving it
        if (activity != null) {
            showInfo(activity);
        }
    }

    protected void showInfoForRowIndex(int rowIndex) {
        Activity activity = getActivityFromRowIndex(rowIndex);
        // Activity may be null when hovering the cursor over the tasks while deleting/moving it
        if (activity != null) {
            showInfo(activity);
        }
    }

    public abstract void setTitle();

    public abstract void setTableHeader();

    protected abstract AbstractActivities getList();

    protected abstract AbstractActivities getTableList();

    public void createNewTask() {
        // do nothing by default
    }

    public void duplicateTask() {
        // do nothing by default
    }

    public void deleteTask(int rowIndex) {
        // do nothing by default        
    }

    public void moveTask(int rowIndex) {
        // do nothing by default
    }

    public void completeTask(int rowIndex) {
        // do nothing by default
    }

    public void createUnplannedTask() {
        // do nothing by default
    }

    public void createInternalInterruption() {
        // do nothing by default
    }

    public void createExternalInterruption() {
        // do nothing by default
    }

    public void overestimateTask(int poms) {
        // do nothing by default
    }

    private ArrayList<Activity> getActivitiesFromSelectedRows() {
        ArrayList<Activity> selectedActivities = new ArrayList<Activity>();
        int[] selectedRows = getSelectedRows();
        for (int id : selectedRows) {
            selectedActivities.add(getActivityById(id));
        }
        return selectedActivities;
    }

    public void setSubtaskComplete() {
        Activity act = getActivityFromSelectedRow();
        act.setDateCompleted(!act.isCompleted() ? new Date() : new Date(0));
        act.setIsCompleted(!act.isCompleted());
        getList().update(act);
        ActivitiesDAO.getInstance().updateComplete(act);
        repaint();
        setTitle();
    }

    public void setTaskDoneDone() {
        Activity act = getActivityFromSelectedRow();
        act.setDateDoneDone(!act.isDoneDone() ? new Date() : new Date(0));
        act.setIsDoneDone(!act.isDoneDone());
        getList().update(act);
        ActivitiesDAO.getInstance().updateDoneDone(act);
        repaint();
        setTitle();
    }

    public void deleteTasks() {
        // do nothing by default
    }

    public void moveLeftTasks() {
        // do nothing by default
    }

    public void moveRightTasks() {
        // do nothing by default
    }

    public void moveSubtasksToMainTable() {
        // do nothing by default
    }

    public void moveSubtaskToMainTable(int rowIndex) {
        // do nothing by default
    }

    public void removeRow(int rowIndex) {
        getModel().removeRow(convertRowIndexToModel(rowIndex)); // we remove in the Model...
        if (getModel().getRowCount() > 0) {
            int currentRow = rowIndex == 0 ? 0 : rowIndex - 1;
            if (currentRow >= 0) {
                setRowSelectionInterval(currentRow, currentRow); // ...while selecting in the View
                scrollRectToVisible(getCellRect(currentRow, 0, true));
            }
        } else if (panel.getCurrentTable().equals(panel.getMainTable())) {
            // refresh tabs and emtpy sub-table when main table is empty
            emptySubTable();
            initTabs();
        }
    }

    // insert but no selection of the inserted row (used when editing)
    public int insertRowNoSelection(Activity activity) {
        return insertRow(activity, false, false);
    }

    // insert but current selection not cleared (used when multiple row inserted)
    public int addRow(Activity activity) {
        return insertRow(activity, false, true);
    }

    // clear selection, insert then select
    public int insertRow(Activity activity) {
        return insertRow(activity, true, true);
    }

    // Insert new row
    public int insertRow(Activity activity, boolean clearSelection, boolean selectRow) {
        if (clearSelection) {
            clearSelection();
        }
        // By default, the row is added at the bottom of the list
        // However, if one of the columns has been previously sorted the position of the row might not be the bottom position...
        getModel().addRow(activity); // we add in the Model...
        int rowCount = getRowCount(); // get row count on the view not the model !
        if (rowCount == 1) { // refresh tabs as the very first row has just been added to the table
            initTabs();
        }
        int currentRow = convertRowIndexToView(rowCount - 1); // ...while selecting in the View
        if (selectRow) {
            addRowSelectionInterval(currentRow, currentRow); // we use addRowSelectionInterval instead of setRowSelectionInterval so we get the inserted row(s) selected at once
        }
        scrollRectToVisible(getCellRect(currentRow, 0, true));
        return currentRow;
    }

    // The clear and selection, made in the insertRow method, will prevent the editCellAt method to edit the cell unless the call is done in a runnable
    protected void editTitleCellAtRowIndex(final int rowIndex) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (editCellAt(rowIndex, convertColumnIndexToView(AbstractTableModel.TITLE_COLUMN_INDEX))) { // programatic editing                        
                    setSurrendersFocusOnKeystroke(true);
                    if (getEditorComponent() != null) { // set blinking cursor
                        getEditorComponent().requestFocus();
                    }
                }
            }
        });
    }

    // This method does not need to be abstract as it's implemented by the Todo table and sub-tables only
    public void reorderByPriority() {
    }

    public void saveComment(String comment) {
        if (getSelectedRowCount() == 1) {
            Activity selectedActivity = getActivityFromSelectedRow();
            if (selectedActivity != null) {
                selectedActivity.setNotes(comment);
                selectedActivity.databaseUpdateComment();
            }
        }
    }

    public void removePomsFromSelectedRow(Activity activity) {
        addPomsToSelectedRow(-activity.getActualPoms(),
                -activity.getEstimatedPoms(),
                -activity.getOverestimatedPoms());
    }

    public void addPomsToSelectedRow(Activity activity) {
        addPomsToSelectedRow(activity.getActualPoms(),
                activity.getEstimatedPoms(),
                activity.getOverestimatedPoms());
    }

    public void addPomsToSelectedRow(int realPoms, int estimatedPoms, int overestimatedPoms) {
        Activity parentActivity = getActivityFromSelectedRow();
        parentActivity.setActualPoms(parentActivity.getActualPoms() + realPoms);
        parentActivity.setEstimatedPoms(parentActivity.getEstimatedPoms() + estimatedPoms);
        parentActivity.setOverestimatedPoms(parentActivity.getOverestimatedPoms() + overestimatedPoms);
        parentActivity.databaseUpdate();
        getList().update(parentActivity);
        panel.getMainTable().getModel().setValueAt(parentActivity.getEstimatedPoms(), convertRowIndexToModel(panel.getMainTable().getSelectedRow()), AbstractTableModel.ESTIMATED_COLUMN_INDEX);
        // Do no use repaint(): it doesn't trigger TableModelListener (example in ReportsTable: listener is needed when updating diff I and II cells when changing estimate while importing subtasks)
        //repaint(); // trigger row renderers        
    }

    public void addActivity(Activity activity) {
        getList().add(activity);
    }

    public void importActivity(Activity activity) { // this is overwritten in Report table
        getList().add(activity);
    }

    public void delete(Activity activity) {
        getList().delete(activity);
    }
}
