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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.EtchedBorder;
import org.mypomodoro.Main;
import org.mypomodoro.buttons.DefaultButton;
import org.mypomodoro.buttons.DiscontinuousButton;
import org.mypomodoro.buttons.MuteButton;
import org.mypomodoro.buttons.PinButton;
import org.mypomodoro.buttons.ResizeButton;
import org.mypomodoro.gui.AbstractPanel;
import org.mypomodoro.gui.AbstractTable;
import org.mypomodoro.gui.TabbedPane;
import org.mypomodoro.gui.activities.CommentPanel;
import org.mypomodoro.gui.export.ExportPanel;
import org.mypomodoro.gui.export.ImportPanel;
import org.mypomodoro.model.ToDoList;
import org.mypomodoro.util.Labels;

/**
 * GUI for viewing what is in the ToDoList. This can be changed later. Right now
 * it uses a DefaultTableModel to build the JTable. Table Listeners can be added
 * to save cell edits to the ActivityCollection which can then be saved to the
 * data layer.
 *
 */
public class ToDoPanel extends AbstractPanel {

    private static final Dimension LIST_TIMER_PANE_DIMENSION = new Dimension(800, 200);
    private static final Dimension PANE_DIMENSION = new Dimension(500, 200);
    private static final Dimension TABPANE_DIMENSION = new Dimension(800, 50);
    // List and Timer Pane : listPane + Timer
    private final JPanel listPaneAndTimer = new JPanel();
    private final GridBagConstraints gbcListPaneAndTimer = new GridBagConstraints();
    // Tab panes: details,...
    private final DetailsPanel detailsPanel = new DetailsPanel(this);
    private final CommentPanel commentPanel = new CommentPanel(this, true);
    private final EditPanel editPanel = new EditPanel(detailsPanel);
    private final MergingPanel mergingPanel = new MergingPanel(this);
    // Pomodoro
    private final JLabel pomodoroTime = new JLabel();
    private final Pomodoro pomodoro = new Pomodoro(this, detailsPanel, pomodoroTime);
    private final TimerPanel timerPanel = new TimerPanel(pomodoro, pomodoroTime, this);
    final ImageIcon timerIcon = new ImageIcon(Main.class.getResource("/images/" + Main.mAPIconTimer));
    final ImageIcon tinyTimerIcon = new ImageIcon(Main.class.getResource("/images/" + Main.mAPIconTinyTimer));
    private JPanel wrap = new JPanel();
    // Discontinuous and Resize buttons
    private final DiscontinuousButton discontinuousButton = new DiscontinuousButton(pomodoro);
    private final MuteButton muteButton = new MuteButton(pomodoro);
    public static final ResizeButton RESIZEBUTTON = new ResizeButton();

    public ToDoPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Init List and Timer Pane
        listPaneAndTimer.setMinimumSize(LIST_TIMER_PANE_DIMENSION);
        listPaneAndTimer.setPreferredSize(LIST_TIMER_PANE_DIMENSION);
        listPaneAndTimer.setLayout(new GridBagLayout());
        gbcListPaneAndTimer.fill = GridBagConstraints.BOTH;

        // Init List pane
        listPane.setMinimumSize(PANE_DIMENSION);
        listPane.setPreferredSize(PANE_DIMENSION);
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));

        // Init Tabbed pane        
        tabbedPane = new TabbedPane(this);
        tabbedPane.setMinimumSize(TABPANE_DIMENSION);
        tabbedPane.setPreferredSize(TABPANE_DIMENSION);
        initTabbedPane();

        // Init Split pane
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listPaneAndTimer, tabbedPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        splitPane.setDividerSize(0); // remove divider by hiding it

        // Init table and sub table (data model and rendering)
        subTableModel = new ToDoSubTableModel();
        tableModel = new ToDoTableModel();
        subTable = new ToDoSubTable((ToDoSubTableModel) subTableModel, this); // instanciate this before table
        table = new ToDoTable((ToDoTableModel) tableModel, this);
        currentTable = table;

        // Init scroll panes
        subTableScrollPane = new JScrollPane(subTable);
        tableScrollPane = new JScrollPane(table);

        // Init title and sub title
        tableTitlePanel = new ToDoTableTitlePanel(this, table);
        subTableTitlePanel = new ToDoSubTableTitlePanel(this, subTable);

        // select first activity of the table so the selection listener gets fired only now that both tables have been instanciated
        if (tableModel.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }

        // Add panes of List pane
        addTableTitlePanel();
        addTable();
        addSubTableTitlePanel();

        // Add Split pane
        add(splitPane);

        // Add list pane to ListAndTimerPane
        addListPane();
        // Add timer to ListAndTimerPane
        addTimerPanel();
        pomodoro.setTimerPanel(timerPanel);
    }

    ////////////////////////////////////////////////
    // TABBED PANE
    ////////////////////////////////////////////////
    @Override
    public void initTabbedPane() {
        tabbedPane.setDetailsTabIndex(0);
        tabbedPane.setCommentTabIndex(1);
        tabbedPane.setEditTabIndex(2);
        tabbedPane.setMergeTabIndex(3);
        tabbedPane.setImportTabIndex(4);
        tabbedPane.setExportTabIndex(5);
        tabbedPane.add(Labels.getString("Common.Details"), detailsPanel);
        tabbedPane.add(Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "Common.Comment"), commentPanel);
        tabbedPane.add(Labels.getString("Common.Edit"), editPanel);
        tabbedPane.add(Labels.getString("ToDoListPanel.Merge"), mergingPanel);
        ImportPanel importPanel = new ImportPanel(this);
        tabbedPane.add(Labels.getString("ReportListPanel.Import"), importPanel);
        ExportPanel exportPanel = new ExportPanel(this);
        tabbedPane.add(Labels.getString("ReportListPanel.Export"), exportPanel);
        addTabbedPaneKeyStrokes();
    }

    public void addListPane() {
        gbcListPaneAndTimer.gridx = 0;
        gbcListPaneAndTimer.gridy = 0;
        gbcListPaneAndTimer.weighty = 1.0;
        gbcListPaneAndTimer.weightx = 1.0;
        listPaneAndTimer.add(listPane, gbcListPaneAndTimer);
    }

    public void addTimerPanel() {
        gbcListPaneAndTimer.gridx = 1;
        gbcListPaneAndTimer.gridy = 0;
        gbcListPaneAndTimer.weighty = 1.0;
        gbcListPaneAndTimer.weightx = 1.0;
        wrap = wrapInBackgroundImage(timerPanel, timerIcon);
        // Deactivate/activate non-pomodoro options: pause, minus, plus buttons        
        /*wrap.addMouseListener(new MouseAdapter() {

         // click
         @Override
         public void mouseClicked(MouseEvent e) {
         timerPanel.switchPomodoroCompliance();
         }
         });*/
        gbcListPaneAndTimer.fill = GridBagConstraints.BOTH;
        gbcListPaneAndTimer.anchor = GridBagConstraints.CENTER;
        listPaneAndTimer.add(wrap, gbcListPaneAndTimer);
    }

    public void addTinyTimerPanel() {
        gbcListPaneAndTimer.gridx = 1;
        gbcListPaneAndTimer.gridy = 0;
        gbcListPaneAndTimer.weighty = 1.0;
        gbcListPaneAndTimer.weightx = 1.0;
        wrap = wrapInBackgroundTinyImage(timerPanel, tinyTimerIcon);
        // Deactivate/activate non-pomodoro options: pause, minus, plus buttons        
        /*wrap.addMouseListener(new MouseAdapter() {

         // click
         @Override
         public void mouseClicked(MouseEvent e) {
         timerPanel.switchPomodoroCompliance();
         }
         });*/
        gbcListPaneAndTimer.fill = GridBagConstraints.HORIZONTAL;
        gbcListPaneAndTimer.anchor = GridBagConstraints.NORTH;
        listPaneAndTimer.add(wrap, gbcListPaneAndTimer);
    }

    @Override
    public ToDoTableModel getNewTableModel() {
        return new ToDoTableModel();
    }

    @Override
    public ToDoList getList() {
        return ToDoList.getList();
    }

    @Override
    public ToDoTable getMainTable() {
        return (ToDoTable) table;
    }

    @Override
    public ToDoTable getCurrentTable() {
        return (ToDoTable) currentTable;
    }

    @Override
    public void setCurrentTable(AbstractTable table) {
        currentTable = (ToDoTable) table;
    }

    @Override
    public ToDoSubTable getSubTable() {
        return (ToDoSubTable) subTable;
    }

    public DetailsPanel getDetailsPanel() {
        return detailsPanel;
    }

    public CommentPanel getCommentPanel() {
        return commentPanel;
    }

    public EditPanel getEditPanel() {
        return editPanel;
    }

    @Override
    public void populateSubTable(int parentId) {
        ((ToDoSubTableModel) subTableModel).update(parentId);
        subTable.setColumnModel();
        subTable.setTitle();
        setCurrentTable(table);
    }

    ////////////////////////////
    //  Specific to ToDoPanel
    ///////////////////////////
    private JPanel wrapInBackgroundImage(final TimerPanel timerPanel, ImageIcon pomodoroIcon) {
        // create wrapper JPanel
        JPanel backgroundPanel = new JPanel(new GridBagLayout());
        backgroundPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL; // center the toolbar
        // Toolbar
        JPanel toolBar = new JPanel(new GridBagLayout());
        GridBagConstraints wc = new GridBagConstraints();
        discontinuousButton.setVisible(true); // this is a TransparentButton       
        discontinuousButton.setMargin(new Insets(0, 0, 0, 0));
        discontinuousButton.setFocusPainted(false); // removes borders around text
        toolBar.add(discontinuousButton, wc);
        if (Main.preferences.getTicking()
                && Main.preferences.getRinging()) { // The two options must be enabled
            muteButton.setVisible(true);
            muteButton.setMargin(new Insets(0, 0, 0, 0));
            muteButton.setFocusPainted(false); // removes borders around text
            toolBar.add(muteButton, wc);
        }
        PinButton pinButton = new PinButton();
        if (Main.preferences.getAlwaysOnTop()) {
            pinButton.setPin();
        }
        pinButton.setVisible(true); // this is a TransparentButton       
        pinButton.setMargin(new Insets(0, 0, 0, 0));
        pinButton.setFocusPainted(false); // removes borders around text
        toolBar.add(pinButton, wc);
        RESIZEBUTTON.setVisible(true); // this is a TransparentButton       
        RESIZEBUTTON.setMargin(new Insets(0, 0, 0, 0));
        RESIZEBUTTON.setFocusPainted(false); // removes borders around text
        toolBar.add(RESIZEBUTTON, wc);
        backgroundPanel.add(toolBar, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;// this is very important to center the component (otherwise won't work with some themes such as Metal)        
        gbc.anchor = GridBagConstraints.CENTER; // this is very important to center the component (otherwise won't work with some themes such as Metal)        
        backgroundPanel.add(timerPanel, gbc);
        // Set background image (tomato) in a button to be able to add an action to it
        final DefaultButton pomodoroButton = new DefaultButton(pomodoroIcon, true);
        pomodoroButton.setContentAreaFilled(false); // this is very important to remove borders on Win7 aero
        pomodoroButton.setOpaque(false);
        // Deactivate/activate non-pomodoro options: pause, minus, plus buttons        
        /*pomodoroButton.addMouseListener(new MouseAdapter() {

         // click
         @Override
         public void mouseClicked(MouseEvent e) {
         timerPanel.switchPomodoroCompliance();
         }
         });*/
        backgroundPanel.add(pomodoroButton, gbc);
        return backgroundPanel;
    }

    private JPanel wrapInBackgroundTinyImage(final TimerPanel timerPanel, ImageIcon tinyTimerIcon) {
        // create wrapper JPanel
        JPanel backgroundPanel = new JPanel(new GridBagLayout());
        backgroundPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        backgroundPanel.add(timerPanel, gbc);
        // Set background image (timer) in a button to be able to add an action to it
        final DefaultButton tinyTimerButton = new DefaultButton(tinyTimerIcon, true);
        tinyTimerButton.setContentAreaFilled(false); // this is very important to remove borders on Win7 aero
        tinyTimerButton.setOpaque(false);
        tinyTimerButton.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        // Deactivate/activate non-pomodoro options: pause, minus, plus buttons        
        /*tinyTimerButton.addMouseListener(new MouseAdapter() {

         // click
         @Override
         public void mouseClicked(MouseEvent e) {
         timerPanel.switchPomodoroCompliance();
         }
         });*/
        backgroundPanel.add(tinyTimerButton, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        if (Main.preferences.getTicking()
                && Main.preferences.getRinging()) { // The two options must be enabled
            backgroundPanel.add(muteButton, gbc);
            gbc.gridx = 2;
            gbc.gridy = 0;
        }
        backgroundPanel.add(RESIZEBUTTON, gbc);
        return backgroundPanel;
    }

    public Pomodoro getPomodoro() {
        return pomodoro;
    }

    public void removeTabbedPane() {
        splitPane.remove(tabbedPane);
    }

    public void removeListPane() {
        listPaneAndTimer.remove(listPane);
    }

    public void addTabbedPane() {
        splitPane.setRightComponent(tabbedPane); // bottom
    }

    public void hideDiscontinuousButton() {
        discontinuousButton.setVisible(false);
        pomodoro.continueWorkflow(); // pomodoro strict mode --> force continuous workflow
    }

    public void showDiscontinuousButton() {
        discontinuousButton.setVisible(true);
        if (pomodoro.isDiscontinuous()) {
            pomodoro.discontinueWorkflow();
        }
    }

    public void showSplitPaneDivider() {
        //splitPane.setDividerSize(10);
        splitPane.setDividerSize(0); // remove divider by hiding it
    }

    public void hideSplitPaneDivider() {
        splitPane.setDividerSize(0);
    }

    public TimerPanel getTimerPanel() {
        return timerPanel;
    }

    public void setTimerPanel() {
        listPaneAndTimer.remove(wrap);
        timerPanel.setTimer();
        addTimerPanel();
    }

    public void setTinyTimerPanel() {
        listPaneAndTimer.remove(wrap);
        timerPanel.setTinyTimer();
        addTinyTimerPanel();
    }
}
