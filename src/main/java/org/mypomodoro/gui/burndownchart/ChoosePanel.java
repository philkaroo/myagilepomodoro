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
package org.mypomodoro.gui.burndownchart;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.mypomodoro.buttons.DefaultButton;
import org.mypomodoro.util.Labels;

/**
 * Panel to generate burndown charts
 *
 */
public class ChoosePanel extends JPanel {

    private static final Dimension PANE_DIMENSION = new Dimension(700, 200);
    private static final Dimension CREATEBUTTON_DIMENSION = new Dimension(100, 250);

    private final ChartTabbedPanel chartTabbedPanel;
    private final GridBagConstraints gbc = new GridBagConstraints();
    private final ChooseInputForm chooseInputForm = new ChooseInputForm();

    public ChoosePanel(ChartTabbedPanel chartTabbedPanel) {
        this.chartTabbedPanel = chartTabbedPanel;

        setLayout(new GridBagLayout());
        //setBorder(new EtchedBorder(EtchedBorder.LOWERED));

        addChooseInputForm();
        addConfigureButton();
    }

    public ChooseInputForm getForm() {
        return chooseInputForm;
    }

    private void addChooseInputForm() {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        JScrollPane chooseScrollPane = new JScrollPane(chooseInputForm);
        chooseScrollPane.setMinimumSize(PANE_DIMENSION);
        chooseScrollPane.setPreferredSize(PANE_DIMENSION);
        add(chooseScrollPane, gbc);
    }

    private void addConfigureButton() {
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.weightx = 0.1;
        JButton configureButton = new DefaultButton(
                Labels.getString("BurndownChartPanel.Configure"));
        configureButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                chartTabbedPanel.goToStep2();
            }
        });
        configureButton.setMinimumSize(CREATEBUTTON_DIMENSION);
        configureButton.setMaximumSize(CREATEBUTTON_DIMENSION);
        configureButton.setPreferredSize(CREATEBUTTON_DIMENSION);
        add(configureButton, gbc);
    }
}
