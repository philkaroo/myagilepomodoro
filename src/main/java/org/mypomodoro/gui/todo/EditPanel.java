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

import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.mypomodoro.gui.IActivityInformation;
import org.mypomodoro.gui.create.ActivityInputForm;
import org.mypomodoro.gui.create.CreatePanel;
import org.mypomodoro.model.Activity;
import org.mypomodoro.model.ToDoList;

/**
 * GUI for editing an existing activity and store to data layer.
 *
 */
public class EditPanel extends CreatePanel {

    private EditInputForm editInputForm;
    private final IActivityInformation information;

    public EditPanel(IActivityInformation information) {
        this.information = information;
        setBorder(null); // remove create panel border
    }

    @Override
    protected void addToDoIconPanel() {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.gridheight = 1;
        iconPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        add(iconPanel, gbc);
    }

    @Override
    protected void addInputFormPanel() {
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        editInputForm = new EditInputForm();
        editInputForm.getNameField().getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
                enableSaveButton();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (editInputForm.getNameField().getText().length() == 0) {
                    disableSaveButton();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                enableSaveButton();
            }
        });
        add(new JScrollPane(editInputForm), gbc);
    }

    @Override
    protected void addSaveButton() {
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.1;
        gbc.gridheight = 2;
        add(saveButton, gbc);
    }

    @Override
    protected void addClearButton() {
    }

    @Override
    protected void addValidation() {
    }

    @Override
    protected void validActivityAction(Activity activity) {
        ToDoList.getList().update(activity);
        activity.databaseUpdate();
        // update details panel
        information.selectInfo(activity);
        information.showInfo();
        /*String title = Labels.getString("ToDoListPanel.Edit ToDo");
         String message = Labels.getString("ToDoListPanel.ToDo updated");
         JOptionPane.showConfirmDialog(Main.gui, message, title, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, ImageIcons.DIALOG_ICON);*/
    }

    @Override
    public void saveActivity(Activity activity) {
        if (ToDoList.getList().size() > 0) {
            // no check for existing todos with same name and date
            if (activity.isValid()) {
                validActivityAction(activity);
            }
        }
    }

    @Override
    public ActivityInputForm getFormPanel() {
        return editInputForm;
    }

    @Override
    public void showInfo(Activity activity) {
        clearForm();
        editInputForm.setTypeField(activity.getType());
        editInputForm.setAuthorField(activity.getAuthor());
        editInputForm.setPlaceField(activity.getPlace());
        editInputForm.setDescriptionField(activity.getDescription());
        editInputForm.setActivityId(activity.getId());
    }

    public JPanel getIconPanel() {
        return iconPanel;
    }
}
