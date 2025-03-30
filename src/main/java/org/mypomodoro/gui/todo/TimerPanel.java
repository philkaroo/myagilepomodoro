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

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.mypomodoro.Main;
import org.mypomodoro.buttons.TimeMinusButton;
import org.mypomodoro.buttons.TimePlusButton;
import org.mypomodoro.buttons.TimeSaveButton;
import org.mypomodoro.buttons.TransparentButton;
import org.mypomodoro.gui.ImageIcons;
import org.mypomodoro.model.Activity;
import org.mypomodoro.util.ColorUtil;
import org.mypomodoro.util.Labels;

public class TimerPanel extends JPanel {

    private GridBagConstraints gbc = new GridBagConstraints();
    private final ImageIcon startIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "start.png"));
    private final ImageIcon stopIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "stop.png"));
    private final ImageIcon stopRedIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "stopred.png"));
    private final TimeSaveButton startButton = new TimeSaveButton(startIcon);
    private final ImageIcon pauseIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "pause.png"));
    private final ImageIcon pauseRedIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "pausered.png"));
    private final ImageIcon resumeIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "resume.png"));
    private final ImageIcon resumeRedIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "resumered.png"));
    private final TransparentButton pauseButton = new TransparentButton(pauseRedIcon);
    private final JLabel pomodoroTime;
    private final ToDoPanel panel;
    private final TimePlusButton timePlus;
    private final TimeMinusButton timeMinus;
    public static boolean strictPomodoro = false;

    TimerPanel(final Pomodoro pomodoro, final JLabel pomodoroTime, final ToDoPanel panel) {
        this.pomodoroTime = pomodoroTime;
        this.panel = panel;
        // Timer font
        try {
            pomodoroTime.setFont(Font.createFont(Font.TRUETYPE_FONT,
                    Main.class.getResourceAsStream("/fonts/timer.ttf")));
        } catch (FontFormatException ex) {
            pomodoroTime.setFont(new JLabel().getFont().deriveFont(Font.PLAIN));
            Main.logger.error("TrueType not supported. Replaced with default System font.", ex);
        } catch (IOException ex) {
            pomodoroTime.setFont(new JLabel().getFont().deriveFont(Font.PLAIN));
            Main.logger.error("Timer TTF file not found. Replaced with default System font.", ex);
        }
        pomodoroTime.setForeground(ColorUtil.DARK_GRAY_TIMER);
        // Init time minus button
        timeMinus = new TimeMinusButton(pomodoro);
        timeMinus.setVisible(true); // this is a TransparentButton
        timeMinus.setMargin(new Insets(0, 0, 0, 0)); // inner margin
        timeMinus.setFocusPainted(false); // removes borders around icon
        // Init time plus button
        timePlus = new TimePlusButton(pomodoro);
        timePlus.setVisible(true); // this is a TransparentButton
        timePlus.setMargin(new Insets(0, 0, 0, 0)); // inner margin
        timePlus.setFocusPainted(false); // removes borders around icon
        // Init pomodoro time button
        pomodoroTime.setFont(pomodoroTime.getFont().deriveFont(40f));
        // Init pause button
        pauseButton.setToolTipText(Labels.getString("ToDoListPanel.Pause"));
        pauseButton.setMargin(new Insets(0, 20, 0, 20)); // inner margin
        pauseButton.setFocusPainted(false); // removes borders around icon
        pauseButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (pomodoro.getTimer().isRunning()) { // pause the current ToDo
                    if (pomodoro.inPomodoro()) {
                        setPausedPomodoroEnv();
                    } else {
                        pauseButton.setIcon(resumeIcon);
                    }
                    pomodoro.pause();
                    if (pomodoro.inPomodoro()) {
                        // The current selected ToDo might not be the running on.
                        if (panel.getCurrentTable().getSelectedRowCount() == 1) {
                            pomodoro.setCurrentToDoId(panel.getCurrentTable().getActivityIdFromSelectedRow());
                        }
                        pomodoro.initTimer(pomodoro.getCurrentToDo().getRecordedTime());
                    }
                    pauseButton.setToolTipText(Labels.getString("ToDoListPanel.Resume"));
                } else { // resume 
                    pomodoro.resume();
                    if (pomodoro.inPomodoro()) {
                        setPomodoroEnv();
                    } else {
                        pauseButton.setIcon(pauseIcon);
                    }
                    pauseButton.setToolTipText(Labels.getString("ToDoListPanel.Pause"));
                }
            }
        });
        // Init start button
        startButton.setToolTipText(Labels.getString("ToDoListPanel.Start"));
        startButton.setVisible(true);
        startButton.setMargin(new Insets(0, 20, 0, 20)); // inner margin
        startButton.setFocusPainted(false); // removes borders around icon
        startButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!startButton.isStarted()) {
                    Activity currentToDo = null;
                    if (panel.getCurrentTable().getSelectedRowCount() == 1) {
                        Activity selectedToDo = panel.getCurrentTable().getActivityFromSelectedRow();
                        pomodoro.setCurrentToDoId(selectedToDo.getId());
                        currentToDo = pomodoro.getCurrentToDo();
                    }
                    if (currentToDo != null) {
                        panel.getCurrentTable().scrollToSelectedRows(); // in any case
                        // Retrieve activity from the database in case it's changed (concurrent work : another user may have worked on it)                                       
                        if (currentToDo.hasChanged()) {
                            String title = Labels.getString("ToDoListPanel.ToDo changed");
                            String message = Labels.getString("ToDoListPanel.The ToDo has changed");
                            JOptionPane.showConfirmDialog(Main.gui, message, title, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, ImageIcons.DIALOG_ICON);
                        } else if (currentToDo.isFinished()) {
                            String message = Labels.getString("ToDoListPanel.All pomodoros of this ToDo are already done");
                            message += System.getProperty("line.separator") + "(" + Labels.getString("ToDoListPanel.please complete this ToDo to make a report or make an overestimation to extend it") + ")";
                            JOptionPane.showConfirmDialog(Main.gui, message, null, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, ImageIcons.DIALOG_ICON);
                        } else if (!strictPomodoro || (strictPomodoro && currentToDo.getEstimatedPoms() > 0)) { // strict pomodoro mode doesn't allow starting task with no estimate
                            pomodoro.start();
                            startButton.setStarted(true);
                            startButton.setIcon(stopRedIcon);
                            startButton.setToolTipText(Labels.getString("ToDoListPanel.Void"));
                            if (strictPomodoro) {
                                startButton.setVisible(false);
                            }
                            pomodoroTime.setForeground(Main.taskRunningColor);
                            timePlus.setTimePlusRedIcon(true); // turn time plus button red
                            timeMinus.setTimeMinusRedIcon(true); // turn time minus button red
                            if (!strictPomodoro) {
                                pauseButton.setVisible(true);
                                pauseButton.setToolTipText(Labels.getString("ToDoListPanel.Pause"));
                            }
                        }
                    }
                } else {
                    pomodoro.stopWithWarning();
                }
            }
        });
        // Init timer
        setTimer();
    }

    // Normal size timer
    public void setTimer() {
        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints(); // reset grid
        // Transparent !
        setOpaque(false);
        addTimePlusButton();
        pomodoroTime.setBorder(new EmptyBorder(0, 10, 0, 10)); // margin around jlabel
        addPomodoroTimerLabel();
        addTimeMinusButton();
        startButton.setMargin(new Insets(0, 20, 0, 20));
        addStartButton();
        pauseButton.setMargin(new Insets(0, 20, 0, 20));
        addPauseButton();
    }

    // Tiny size timer
    public void setTinyTimer() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 1, 5));
        // Transparent !
        setOpaque(false);
        startButton.setMargin(new Insets(0, 0, 0, 0));
        add(startButton);
        add(timeMinus);
        pomodoroTime.setBorder(new EmptyBorder(0, 8, 0, 8)); // margin around jlabel
        add(pomodoroTime);
        add(timePlus);
        pauseButton.setMargin(new Insets(0, 0, 0, 0));
        add(pauseButton);
    }

    private void addPauseButton() {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTH;
        add(pauseButton, gbc);
    }

    private void addTimeMinusButton() {
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.1;
        gbc.weighty = 0.1;
        gbc.anchor = GridBagConstraints.EAST;
        add(timeMinus, gbc);
    }

    private void addPomodoroTimerLabel() {
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.1;
        gbc.weighty = 0.1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(pomodoroTime, gbc);
    }

    private void addTimePlusButton() {
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.1;
        gbc.weighty = 0.1;
        gbc.anchor = GridBagConstraints.WEST;
        add(timePlus, gbc);
    }

    private void addStartButton() {
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.NORTH;
        add(startButton, gbc);
    }

    // prepare env to start
    public void setStartEnv() {
        startButton.setStarted(false);
        startButton.setIcon(startIcon);
        startButton.setToolTipText(Labels.getString("ToDoListPanel.Start"));
        pauseButton.setVisible(false);
        pauseButton.setIcon(pauseRedIcon);
        pauseButton.setToolTipText(Labels.getString("ToDoListPanel.Pause"));
        timePlus.setTimePlusRedIcon(false);
        timeMinus.setTimeMinusRedIcon(false);
        pomodoroTime.setForeground(ColorUtil.BLACK);
    }

    // turn icons black
    public void setBreakEnv() {
        startButton.setStarted(true);
        startButton.setIcon(stopIcon);
        startButton.setToolTipText(Labels.getString("ToDoListPanel.Stop"));
        pauseButton.setIcon(pauseIcon);
        pauseButton.setToolTipText(Labels.getString("ToDoListPanel.Pause"));
        timePlus.setTimePlusRedIcon(false);
        timeMinus.setTimeMinusRedIcon(false);
        pomodoroTime.setForeground(ColorUtil.BLACK);
    }

    // turn icons red
    public void setPomodoroEnv() {
        startButton.setStarted(true);
        startButton.setIcon(stopRedIcon);
        startButton.setToolTipText(Labels.getString("ToDoListPanel.Void"));
        pauseButton.setIcon(pauseRedIcon);
        pauseButton.setToolTipText(Labels.getString("ToDoListPanel.Pause"));
        timePlus.setTimePlusRedIcon(true);
        timeMinus.setTimeMinusRedIcon(true);
        pomodoroTime.setForeground(Main.taskRunningColor);
    }

    /*public void setPausedBreakEnv() {
     startButton.setStarted(true);
     startButton.setIcon(stopIcon);
     startButton.setToolTipText(Labels.getString("ToDoListPanel.Stop"));
     pauseButton.setVisible(true);
     pauseButton.setIcon(resumeIcon);
     pauseButton.setToolTipText(Labels.getString("ToDoListPanel.Resume"));
     timePlus.setTimePlusRedIcon(false);
     timeMinus.setTimeMinusRedIcon(false);
     pomodoroTime.setForeground(ColorUtil.BLACK);
     }*/
    // turn icons red
    public void setPausedPomodoroEnv() {
        startButton.setStarted(true);
        startButton.setIcon(stopRedIcon);
        startButton.setToolTipText(Labels.getString("ToDoListPanel.Void"));
        pauseButton.setVisible(true);
        pauseButton.setIcon(resumeRedIcon);
        pauseButton.setToolTipText(Labels.getString("ToDoListPanel.Resume"));
        timePlus.setTimePlusRedIcon(true);
        timeMinus.setTimeMinusRedIcon(true);
        pomodoroTime.setForeground(Main.taskRunningColor);
    }

    public void switchPomodoroCompliance() {
        if (!strictPomodoro) { // make it strict pomodoro
            if (startButton.isStarted()) {
                startButton.setVisible(false);
            }
            pauseButton.setVisible(false);
            panel.hideDiscontinuousButton();
            timePlus.setVisible(false);
            timeMinus.setVisible(false);
            strictPomodoro = true;
        } else { // default
            startButton.setVisible(true);
            if (startButton.isStarted()) {
                pauseButton.setVisible(true);
            }
            panel.showDiscontinuousButton();
            timePlus.setVisible(true);
            timeMinus.setVisible(true);
            strictPomodoro = false;
        }
    }

    public void hideStartButton() {
        startButton.setVisible(false);
    }

    public void showStartButton() {
        startButton.setVisible(true);
    }

    public void hideTimeMinusButton() {
        timeMinus.setVisible(false);
    }

    public void showTimeMinusButton() {
        timeMinus.setVisible(true);
    }

    public void hideTimePlusButton() {
        timePlus.setVisible(false);
    }

    public void showTimePlusButton() {
        timePlus.setVisible(true);
    }

    public void hidePauseButton() {
        pauseButton.setVisible(false);
    }

    public void showPauseButton() {
        pauseButton.setVisible(true);
    }
}
