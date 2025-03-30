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
package org.mypomodoro.gui.create;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.mypomodoro.Main;
import org.mypomodoro.buttons.DefaultButton;
import org.mypomodoro.buttons.SaveButton;
import org.mypomodoro.gui.ImageIcons;
import org.mypomodoro.model.Activity;
import org.mypomodoro.util.ColorUtil;
import org.mypomodoro.util.Labels;

/**
 * GUI for creating a new Activity and store to data layer.
 *
 */
public class CreatePanel extends JPanel {

    protected final ActivityInputForm inputFormPanel = new ActivityInputForm();
    protected final JPanel iconPanel = new JPanel();
    protected final JLabel validation = new JLabel("");
    protected final SaveButton saveButton = new SaveButton(this);
    protected GridBagConstraints gbc = new GridBagConstraints();

    public CreatePanel() {
        setLayout(new GridBagLayout());

        addToDoIconPanel();
        addInputFormPanel();
        addSaveButton();
        addClearButton();
        addValidation();
    }

    protected void addToDoIconPanel() {
    }

    protected void addInputFormPanel() {
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.80;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        inputFormPanel.getNameField().getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
                enableSaveButton();
                clearValidation(); // clear validation message (if any)
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (inputFormPanel.getNameField().getText().length() == 0) {
                    disableSaveButton();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                enableSaveButton();
                clearValidation(); // clear validation message (if any)
            }
        });
        add(inputFormPanel, gbc);
    }

    protected void addSaveButton() {
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.5;
        gbc.weighty = 0.1;
        gbc.gridwidth = 1;
        //gbc.fill = GridBagConstraints.NONE;
        disableSaveButton();
        add(saveButton, gbc);
    }

    protected void addClearButton() {
        JButton clearButton = new DefaultButton(Labels.getString("Common.Reset"));
        clearButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                disableSaveButton();
                clearForm();
            }
        });
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0.5;
        gbc.weighty = 0.1;
        gbc.gridwidth = 1;
        //gbc.fill = GridBagConstraints.NONE;
        add(clearButton, gbc);
    }

    protected void addValidation() {
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        validation.setVisible(false);
        add(validation, gbc);
    }

    protected void validActivityAction(Activity newActivity) {
        Main.gui.getActivityListPanel().getMainTable().addActivity(newActivity);
        Main.gui.getActivityListPanel().getMainTable().insertRow(newActivity);
        clearForm();
        validation.setForeground(getForeground());
        validation.setFont(getFont().deriveFont(Font.BOLD));
        validation.setText(Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "CreatePanel.Activity added to Activity List"));
    }

    public void saveActivity(Activity newActivity) {
        if (!newActivity.isValid()) {
            invalidActivityAction();
            validation.setVisible(true);
            disableSaveButton();
        } else if (newActivity.alreadyExists()) {
            String title = Labels.getString("Common.Warning");
            String message = Labels.getString("CreatePanel.An activity with the same title already exists. Proceed anyway?");
            int reply = JOptionPane.showConfirmDialog(Main.gui, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, ImageIcons.DIALOG_ICON);
            if (reply == JOptionPane.YES_OPTION) {
                disableSaveButton();
                validActivityAction(newActivity);
                validation.setVisible(true);
            }
        } else {
            disableSaveButton();
            validActivityAction(newActivity);
            validation.setVisible(true);
        }
    }

    protected void invalidActivityAction() {
        validation.setForeground(ColorUtil.RED);
        validation.setFont(getFont().deriveFont(Font.BOLD));
        validation.setText(Labels.getString("Common.Title is mandatory"));
    }

    public ActivityInputForm getFormPanel() {
        return inputFormPanel;
    }

    public void clearForm() {
        inputFormPanel.setNameField("");
        inputFormPanel.setEstimatedPomodoro(0);
        if (Main.preferences.getAgileMode()) {
            inputFormPanel.setStoryPoints(0);
            inputFormPanel.setIterations(0);
        }
        inputFormPanel.setDescriptionField("");
        inputFormPanel.setTypeField("");
        inputFormPanel.setAuthorField("");
        inputFormPanel.setPlaceField("");
        inputFormPanel.setDate(new Date());
        clearValidation();
    }

    public void showInfo(Activity activity) {
    }

    private void clearValidation() {
        validation.setText("");
        validation.setVisible(false);
    }

    public void enableSaveButton() {
        saveButton.setEnabled(true);
        saveButton.setOpaque(true);
        saveButton.setToolTipText(Labels.getString("Common.Save") + " (ENTER)"); // not set on the saveButton object itself because ENTER shortcut won't work with the edit panels
        saveButton.setForeground(getForeground()); // reset the foreground of the current theme
    }

    public void disableSaveButton() {
        saveButton.setEnabled(false);
        saveButton.setOpaque(false);
        saveButton.setForeground(Color.GRAY);
    }
}
