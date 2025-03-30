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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import org.mypomodoro.Main;
import org.mypomodoro.buttons.CompleteToDoButton;
import org.mypomodoro.buttons.DeleteButton;
import org.mypomodoro.buttons.MoveToDoButton;
import org.mypomodoro.gui.IActivityInformation;
import org.mypomodoro.gui.activities.ActivityInformationPanel;
import org.mypomodoro.model.Activity;
import org.mypomodoro.util.Labels;

/**
 * Panel that displays information on the selected Pomodoro
 *
 */
public class DetailsPanel extends ActivityInformationPanel implements IActivityInformation {

    private final ToDoPanel panel;
    private final JPanel iconPanel = new JPanel();
    private final GridBagConstraints gbc = new GridBagConstraints();
    private MoveToDoButton moveButton;
    private CompleteToDoButton completeButton;
    private DeleteButton deleteSubtaskButton;

    public DetailsPanel(ToDoPanel todoPanel) {
        this.panel = todoPanel;

        setLayout(new GridBagLayout());
        setBorder(null);

        addMoveButton(todoPanel);
        addDeleteSubtaskButton(todoPanel);
        addInformationPanel();
        addCompleteButton(todoPanel);
    }

    private void addDeleteSubtaskButton(ToDoPanel todoPanel) {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.1;
        deleteSubtaskButton = new DeleteButton(todoPanel);
        deleteSubtaskButton.setVisible(false); // invisible by default
        add(deleteSubtaskButton, gbc);
    }

    private void addMoveButton(ToDoPanel todoPanel) {
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.1;
        gbc.gridheight = 2;
        String leftArrow = getFont().canDisplay('\u226a') ? "\u226a" : "<<<";
        moveButton = new MoveToDoButton(leftArrow, todoPanel);
        moveButton.setFont(getFont().deriveFont(Font.PLAIN, getFont().getSize() + (getFont().canDisplay('\u226a') ? 30 : 6)));
        add(moveButton, gbc);
    }

    private void addInformationPanel() {
        JPanel infoPanel = new JPanel();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 2;
        GridBagConstraints igbc = new GridBagConstraints();
        infoPanel.setLayout(new GridBagLayout());
        addToDoIconPanel(infoPanel, igbc);
        addInformationArea(infoPanel, igbc);
        add(infoPanel, gbc);
    }

    private void addToDoIconPanel(JPanel infoPanel, GridBagConstraints igbc) {
        igbc.gridx = 0;
        igbc.gridy = 0;
        igbc.fill = GridBagConstraints.BOTH;
        igbc.weightx = 1.0;
        igbc.gridheight = 1;
        iconPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        infoPanel.add(iconPanel, igbc);
    }

    private void addInformationArea(JPanel infoPanel, GridBagConstraints igbc) {
        // add the information area
        igbc.gridx = 0;
        igbc.gridy = 1;
        igbc.fill = GridBagConstraints.BOTH;
        igbc.weightx = 1.0;
        igbc.weighty = 1.0;
        informationArea.setEditable(false);
        infoPanel.add(new JScrollPane(informationArea), igbc);
    }

    private void addCompleteButton(ToDoPanel todoPanel) {
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.1;
        gbc.gridheight = 2;
        String rightArrow = Labels.getString(Main.preferences.getAgileMode() ? "Common.Done" : "ToDoListPanel.Complete");
        completeButton = new CompleteToDoButton(rightArrow, todoPanel);
        add(completeButton, gbc);
    }

    @Override
    public void selectInfo(Activity activity) {
        super.selectInfo(activity);
        Activity currentToDo = panel.getPomodoro().getCurrentToDo();
        if (activity.isSubTask()) {
            deleteSubtaskButton.setVisible(true);
            moveButton.setVisible(false);
            completeButton.setVisible(false);
        } else {
            deleteSubtaskButton.setVisible(false);
            moveButton.setVisible(true);
            completeButton.setVisible(true);
        }
        if (currentToDo != null
                && (activity.getId() == currentToDo.getId() || activity.getId() == currentToDo.getParentId())
                && panel.getPomodoro().inPomodoro()) {
            disableAllButtons();
        } else {
            enableAllButtons();
        }
        textMap.remove("date_reopened");
        if (activity.isTask()) {
            textMap.remove("date_completed"); // subtasks may be done
        }
        textMap.remove("date_donedone");
        /*if (!Main.preferences.getAgileMode()) {
         textMap.remove("storypoints");
         textMap.remove("iteration");
         }*/
    }

    public JPanel getIconPanel() {
        return iconPanel;
    }

    public void disableAllButtons() {
        deleteSubtaskButton.setEnabled(false);
        moveButton.setEnabled(false);
        completeButton.setEnabled(false);
    }

    public void enableAllButtons() {
        deleteSubtaskButton.setEnabled(true);
        moveButton.setEnabled(true);
        completeButton.setEnabled(true);
    }
}
