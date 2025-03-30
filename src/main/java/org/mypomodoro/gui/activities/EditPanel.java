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
package org.mypomodoro.gui.activities;

import java.awt.GridBagConstraints;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.mypomodoro.gui.AbstractTableModel;
import org.mypomodoro.gui.IActivityInformation;
import org.mypomodoro.gui.create.ActivityInputForm;
import org.mypomodoro.gui.create.CreatePanel;
import org.mypomodoro.model.Activity;
import org.mypomodoro.model.ActivityList;

/**
 * GUI for editing an existing activity and store to data layer.
 *
 */
public class EditPanel extends CreatePanel {

    private EditInputForm editInputForm;
    private final ActivitiesPanel panel;
    private final IActivityInformation information;

    public EditPanel(ActivitiesPanel panel, IActivityInformation information) {
        this.panel = panel;
        this.information = information;

        setBorder(null);
    }

    @Override
    protected void addInputFormPanel() {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = GridBagConstraints.REMAINDER;
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
        gbc.weightx = 0.1;
        //gbc.fill = GridBagConstraints.NONE;
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
        ActivityList.getList().update(activity);
        activity.databaseUpdate();
        int row = panel.getCurrentTable().getSelectedRow();
        panel.getCurrentTable().getModel().setValueAt(activity.getDate(), panel.getCurrentTable().convertRowIndexToModel(row), AbstractTableModel.DATE_COLUMN_INDEX);
        // update details panel
        information.selectInfo(activity);
        information.showInfo();
        /*String title = Labels.getString("ActivityListPanel.Edit activity");
         String message = Labels.getString("ActivityListPanel.Activity updated");
         JOptionPane.showConfirmDialog(Main.gui, message, title, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, ImageIcons.DIALOG_ICON);*/
    }

    @Override
    public void saveActivity(Activity activity) {
        if (ActivityList.getList().size() > 0) {
            // no check for existing reports with same name and date
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
        editInputForm.setDate(activity.getDate());
        editInputForm.setAuthorField(activity.getAuthor());
        editInputForm.setPlaceField(activity.getPlace());
        editInputForm.setDescriptionField(activity.getDescription());
        editInputForm.setActivityId(activity.getId());
    }
}
