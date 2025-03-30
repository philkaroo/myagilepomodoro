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

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import org.mypomodoro.Main;
import org.mypomodoro.buttons.DefaultButton;
import org.mypomodoro.util.Labels;

/**
 *
 *
 */
public abstract class AbstractTitlePanel extends JPanel {

    protected final JLabel titleLabel = new JLabel();
    protected final JPanel buttonPanel = new JPanel();
    private final ImageIcon refreshIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "refresh.png"));
    private final ImageIcon createIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "create.png"));
    private final ImageIcon duplicateIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "duplicate.png"));
    private final ImageIcon selectedIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "selected.png"));
    private final ImageIcon unplannedIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "unplanned.png"));
    private final ImageIcon internalIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "internal.png"));
    private final ImageIcon externalIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "external.png"));
    private final ImageIcon overestimationIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "plusone.png"));
    protected final ImageIcon runningIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "running.png"));
    protected final ImageIcon moveupIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "moveup.png"));
    protected final ImageIcon doneIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "done.png"));
    protected final DefaultButton unplannedButton = new DefaultButton(unplannedIcon);
    protected final DefaultButton internalButton = new DefaultButton(internalIcon);
    protected final DefaultButton externalButton = new DefaultButton(externalIcon);
    protected final DefaultButton overestimationButton = new DefaultButton(overestimationIcon);
    protected final DefaultButton refreshButton = new DefaultButton(refreshIcon);
    protected final DefaultButton createButton = new DefaultButton(createIcon);
    protected final DefaultButton duplicateButton = new DefaultButton(duplicateIcon);
    protected final DefaultButton selectedButton = new DefaultButton(selectedIcon);
    protected final DefaultButton doneButton = new DefaultButton(doneIcon);
    protected final DefaultButton doneDoneButton = new DefaultButton(doneIcon);
    //protected final DefaultButton moveSubtasksButton = new DefaultButton(moveupIcon);
    protected final Insets buttonInsets = new Insets(0, 10, 0, 10);
    // left and rigth 'small' arrows
    private final String rightArrow = " " + (getFont().canDisplay('\u25b6') ? "\u25b6" : ">") + " ";
    private final String leftArrow = " " + (getFont().canDisplay('\u25c0') ? "\u25c0" : "<") + " ";
    // Expand/Fold button
    protected final DefaultButton expandButton = new DefaultButton(rightArrow);

    public AbstractTitlePanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
        setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        // Fixed size : prevents changes in height when resizing
        setMaximumSize(new Dimension((int) new Frame().getToolkit().getScreenSize().getWidth(), 30));
        setMinimumSize(new Dimension(800, 30));
        setPreferredSize(new Dimension(800, 30));

        // init button panel
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
        buttonPanel.setBorder(null);
        // Init buttons
        // Fold button (the fold button doen't appear by default)
        expandButton.setBorder(null); // this is important to remove the invisible border
        expandButton.addMouseListener(new ExpandMouseAdapter());
        showExpandButton();
        // Add label to panel        
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        titleLabel.setVerticalAlignment(SwingConstants.CENTER);
        showTitleLabel();
        // Scroll to selected task
        selectedButton.setMargin(buttonInsets);
        selectedButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                scrollToSelectedRows();
            }
        });
        selectedButton.setToolTipText(Labels.getString("Common.Scroll to selected") + " (CTRL + G)");
        // Create new task
        createButton.setMargin(buttonInsets);
        createButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                createNewTask();
            }
        });
        createButton.setToolTipText(Labels.getString("Common.Create") + " (CTRL + T)");
        // Duplicate selected task
        duplicateButton.setMargin(buttonInsets);
        duplicateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                duplicateTask();
            }
        });
        duplicateButton.setToolTipText(Labels.getString("Common.Duplicate") + " (CTRL + D)");
        // Create unplanned task
        unplannedButton.setMargin(buttonInsets);
        unplannedButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                createUnplannedTask();
            }
        });
        unplannedButton.setToolTipText(Labels.getString("Common.Unplanned") + " (CTRL + U)");
        // Create internal interruption
        internalButton.setMargin(buttonInsets);
        internalButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                createInternalInterruption();
            }
        });
        internalButton.setToolTipText(Labels.getString("ToDoListPanel.Internal interruption") + " (CTRL + I)");
        // Create external interruption
        externalButton.setMargin(buttonInsets);
        externalButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                createExternalInterruption();
            }
        });
        externalButton.setToolTipText(Labels.getString("ToDoListPanel.External interruption") + " (CTRL + E)");
        // Overestimate by one pomodoro
        overestimationButton.setMargin(buttonInsets);
        overestimationButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                overestimateTask(1);
            }
        });
        overestimationButton.setToolTipText(Labels.getString("ToDoListPanel.Overestimate"));
        // Move up subtask to main table (convert to task)
        /*moveSubtasksButton.setMargin(buttonInsets);
         moveSubtasksButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
         moveSubtasks();
         }
         });
         moveSubtasksButton.setToolTipText(Labels.getString("Common.Move"));*/
        // Refresh table from database
        refreshButton.setMargin(buttonInsets);
        refreshButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                refreshButton.setEnabled(false);
                refreshTable(true);
                refreshButton.setEnabled(true);
            }
        });
        doneButton.setMargin(buttonInsets);
        doneButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setSubtaskComplete();
            }
        });
        doneButton.setToolTipText(Labels.getString(Main.preferences.getAgileMode() ? "Common.Done" : "ToDoListPanel.Complete"));
        doneDoneButton.setMargin(buttonInsets);
        doneDoneButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setTaskDoneDone();
            }
        });
        doneDoneButton.setToolTipText(Labels.getString("Agile.ReportListPanel.Done-Done"));
    }

    class ExpandMouseAdapter extends MouseAdapter {

        private Robot robot = null; // used to move the cursor

        public ExpandMouseAdapter() {
            try {
                robot = new Robot();
            } catch (AWTException ignored) {
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            Point pOriginal = e.getLocationOnScreen(); // original location on screen
            if (buttonPanel.isShowing()) {
                hideButtonPanel();
                expandButton.setText(rightArrow);
                buttonPanel.setToolTipText(null);
            } else {
                add(buttonPanel, 0);
                expandButton.setText(leftArrow);
                buttonPanel.setToolTipText(titleLabel.getText());
            }
            // The following line are required get the cursor to move correctly        
            AbstractTitlePanel.this.validate();
            // Center cursor on button
            if (robot != null) {
                Point pFinal = expandButton.getLocationOnScreen(); // final location on screen
                // Set cursor at the same original Y position
                robot.mouseMove((int) pFinal.getX() + expandButton.getWidth() / 2, (int) pOriginal.getY());
            }
        }
    }

    public void showTitleLabel() {
        add(titleLabel);
    }

    public void hideTitleLabel() {
        remove(titleLabel);
    }

    public void showButtonPanel() {
        add(buttonPanel);
    }

    public void hideButtonPanel() {
        remove(buttonPanel);
    }

    public void showExpandButton() {
        add(expandButton, 0);
    }

    public void hideExpandButton() {
        remove(expandButton);
    }

    public void showSelectedButton() {
        buttonPanel.add(selectedButton);
    }

    public void switchSelectedButton() {
        selectedButton.setIcon(selectedIcon);
    }

    public void switchRunningButton() {
        selectedButton.setIcon(runningIcon);
    }

    public void showCreateButton() {
        buttonPanel.add(createButton);
    }

    public void showDuplicateButton() {
        buttonPanel.add(duplicateButton);
    }

    public void showOverestimationButton() {
        buttonPanel.add(overestimationButton);
    }

    public void showUnplannedButton() {
        buttonPanel.add(unplannedButton);
    }

    public void showInternalButton() {
        buttonPanel.add(internalButton);
    }

    public void showExternalButton() {
        buttonPanel.add(externalButton);
    }

    public void showDoneButton() {
        buttonPanel.add(doneButton);
    }

    public void showDoneDoneButton() {
        buttonPanel.add(doneDoneButton);
    }

    /*public void showMoveSubtasksButton() {
     buttonPanel.add(moveSubtasksButton);
     }*/
    public void showRefreshButton() {
        buttonPanel.add(refreshButton);
    }

    public void hideSelectedButton() {
        buttonPanel.remove(selectedButton);
    }

    public void hideCreateButton() {
        buttonPanel.remove(createButton);
    }

    public void hideDuplicateButton() {
        buttonPanel.remove(duplicateButton);
    }

    public void hideOverestimationButton() {
        buttonPanel.remove(overestimationButton);
    }

    public void hideUnplannedButton() {
        buttonPanel.remove(unplannedButton);
    }

    public void hideInternalButton() {
        buttonPanel.remove(internalButton);
    }

    public void hideExternalButton() {
        buttonPanel.remove(externalButton);
    }

    public void hideDoneButton() {
        buttonPanel.remove(doneButton);
    }

    public void hideDoneDoneButton() {
        buttonPanel.remove(doneDoneButton);
    }

    /*public void hideMoveSubtasksButton() {
     buttonPanel.remove(moveSubtasksButton);
     }*/
    public void hideRefreshButton() {
        buttonPanel.remove(refreshButton);
    }

    @Override
    public void setToolTipText(String text) {
        titleLabel.setToolTipText(text);
    }

    public void setText(String text) {
        titleLabel.setText(text);
    }

    protected abstract void scrollToSelectedRows();

    protected abstract void createNewTask();

    protected abstract void duplicateTask();

    protected abstract void createUnplannedTask();

    protected abstract void createInternalInterruption();

    protected abstract void createExternalInterruption();

    protected abstract void overestimateTask(int poms);

    protected abstract void setSubtaskComplete();

    protected abstract void setTaskDoneDone();

    protected abstract void refreshTable(boolean fromDatabase);

    //protected abstract void moveSubtasks();
    // This is important to make sure the title is refreshed and repainted
    @Override
    public void repaint() {
        validate();
        super.repaint();
    }
}
