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
package org.mypomodoro.gui.export;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.mypomodoro.buttons.DefaultButton;
import org.mypomodoro.gui.create.FormLabel;
import org.mypomodoro.util.Labels;

/**
 * Import form
 *
 */
public class ImportInputForm extends ExportInputForm {

    private FileDialog fileDialog;
    private static final Dimension TEXT_FIELD_DIMENSION = new Dimension(215, 25);

    public ImportInputForm() {
        defaultFileName = "";
        taskBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                taskBox.setSelected(true);
                subtaskBox.setSelected(false);
            }
        });
        subtaskBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                subtaskBox.setSelected(true);
                taskBox.setSelected(false);
            }
        });
    }

    @Override
    protected void addTaskSubTaskCheckbox(GridBagConstraints c) {
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        JPanel taskSubtaskMode = new JPanel();
        taskSubtaskMode.setLayout(new GridBagLayout());
        GridBagConstraints gbctaskSubtaskMode = new GridBagConstraints();
        gbctaskSubtaskMode.fill = GridBagConstraints.HORIZONTAL;
        gbctaskSubtaskMode.anchor = GridBagConstraints.NORTH;
        gbctaskSubtaskMode.gridx = 0;
        gbctaskSubtaskMode.gridy = 0;
        taskSubtaskMode.add(taskBox, gbctaskSubtaskMode);
        gbctaskSubtaskMode.gridx = 1;
        gbctaskSubtaskMode.gridy = 0;
        taskSubtaskMode.add(subtaskBox, gbctaskSubtaskMode);
        exportFormPanel.add(taskSubtaskMode, c);
    }

    @Override
    protected void addFileField(GridBagConstraints c) {
        c.gridx = 0;
        c.gridy = 2;
        c.weighty = 0.5;
        FormLabel fileNamelabel = new FormLabel(
                Labels.getString("ReportListPanel.File") + "*: ");
        exportFormPanel.add(fileNamelabel, c);
        c.gridx = 1;
        c.gridy = 2;
        c.weighty = 0.5;
        JPanel fileChooserPanel = new JPanel();
        fileChooserPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fileName.setEditable(false);
        fileName.setMinimumSize(TEXT_FIELD_DIMENSION);
        fileName.setPreferredSize(TEXT_FIELD_DIMENSION);
        fileName.setBackground(new JTextField().getBackground()); // set the background of the current theme
        fileName.setForeground(new JTextField().getForeground()); // set the background of the current theme
        fileChooserPanel.add(fileName);
        fileChooserPanel.add(new JLabel(" ")); // space
        JDialog d = new JDialog();
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // garbage collection
        fileDialog = new FileDialog(d, Labels.getString("ReportListPanel.Choose a file"), FileDialog.LOAD);
        DefaultButton browseButton = new DefaultButton(Labels.getString("ReportListPanel.Browse"));
        browseButton.setFont(getFont().deriveFont(Font.BOLD));
        browseButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fileDialog.setVisible(true);
                String directory = fileDialog.getDirectory();
                String file = fileDialog.getFile();
                if (directory != null && file != null) {
                    fileName.setText(directory + file);
                }
            }
        });
        fileChooserPanel.add(browseButton);
        exportFormPanel.add(fileChooserPanel, c);
    }

    @Override
    public Object[] getFileFormats() {
        return new Object[]{CSVFormat, ExcelFormat, ExcelOpenXMLFormat, XMLFormat};
    }

    public JCheckBox getTaskBox() {
        return taskBox;
    }

    public JCheckBox getSubtaskBox() {
        return subtaskBox;
    }
}
