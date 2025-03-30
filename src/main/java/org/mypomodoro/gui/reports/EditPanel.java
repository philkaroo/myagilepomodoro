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
package org.mypomodoro.gui.reports;

import java.awt.GridBagConstraints;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.mypomodoro.gui.IActivityInformation;
import org.mypomodoro.gui.create.ActivityInputForm;
import org.mypomodoro.gui.create.CreatePanel;
import org.mypomodoro.model.Activity;
import org.mypomodoro.model.ReportList;

/**
 * GUI for editing an existing report and store to data layer.
 *
 */
public class EditPanel extends CreatePanel {

    private ReportInputForm reportInputForm;
    private final IActivityInformation information;

    public EditPanel(IActivityInformation information) {
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
        reportInputForm = new ReportInputForm();
        reportInputForm.getNameField().getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
                enableSaveButton();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (reportInputForm.getNameField().getText().length() == 0) {
                    disableSaveButton();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                enableSaveButton();
            }
        });
        add(new JScrollPane(reportInputForm), gbc);
    }

    @Override
    protected void addSaveButton() {
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.gridheight = 2;
        // gbc.fill = GridBagConstraints.NONE;
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
        ReportList.getList().update(activity);
        activity.databaseUpdate();
        // update details panel
        information.selectInfo(activity);
        information.showInfo();
        /*String title = Labels.getString("ReportListPanel.Edit report");
         String message = Labels.getString("ReportListPanel.Report updated");
         JOptionPane.showConfirmDialog(Main.gui, message, title, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, ImageIcons.DIALOG_ICON);*/
    }

    @Override
    public void saveActivity(Activity report) {
        if (ReportList.getList().size() > 0) {
            // no check for existing reports with same name and date
            if (report.isValid()) {
                validActivityAction(report);
            }
        }
    }

    @Override
    public ActivityInputForm getFormPanel() {
        return reportInputForm;
    }

    @Override
    public void showInfo(Activity report) {
        clearForm();
        reportInputForm.setAuthorField(report.getAuthor());
        reportInputForm.setPlaceField(report.getPlace());
        reportInputForm.setDescriptionField(report.getDescription());
        reportInputForm.setActivityId(report.getId());
    }
}
