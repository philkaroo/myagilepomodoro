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
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.mypomodoro.buttons.DefaultButton;
import org.mypomodoro.model.ChartList;
import org.mypomodoro.util.DateUtil;
import org.mypomodoro.util.Labels;
import org.mypomodoro.util.WaitCursor;

/**
 * Panel to generate burndown charts
 *
 */
public class ConfigurePanel extends JPanel {

    private static final Dimension PANE_DIMENSION = new Dimension(700, 200);
    private static final Dimension CREATEBUTTON_DIMENSION = new Dimension(100, 250);

    private final ChartTabbedPanel chartTabbedPanel;
    private final GridBagConstraints gbc = new GridBagConstraints();
    private final ChooseInputForm chooseInputForm;
    private final ConfigureInputForm configureInputForm = new ConfigureInputForm();

    public ConfigurePanel(ChartTabbedPanel chartTabbedPanel, ChoosePanel choosePanel) {
        this.chartTabbedPanel = chartTabbedPanel;
        this.chooseInputForm = choosePanel.getForm();

        setLayout(new GridBagLayout());
        //setBorder(new EtchedBorder(EtchedBorder.LOWERED));

        addConfigureInputForm();
        addCheckButton();
    }

    public void refresh() {
        configureInputForm.refresh(chooseInputForm);
    }

    public ConfigureInputForm getForm() {
        return configureInputForm;
    }

    private void addConfigureInputForm() {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        JScrollPane configureScrollPane = new JScrollPane(configureInputForm);
        configureScrollPane.setMinimumSize(PANE_DIMENSION);
        configureScrollPane.setPreferredSize(PANE_DIMENSION);
        add(configureScrollPane, gbc);
    }

    private void addCheckButton() {
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.weightx = 0.1;
        JButton checkButton = new DefaultButton(
                Labels.getString("BurndownChartPanel.Check"));
        checkButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!WaitCursor.isStarted()) {
                    //tabbedPane.setEnabledAt(2, true);
                    //tabbedPane.setSelectedIndex(2);
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            if (configureInputForm.getDatesCheckBox().isSelected()) {
                                ArrayList<Date> datesToBeIncluded = DateUtil.getDatesWithExclusions(configureInputForm.getStartDate(),
                                        configureInputForm.getEndDate(),
                                        configureInputForm.getExcludeSaturdays().isSelected(),
                                        configureInputForm.getExcludeSundays().isSelected(),
                                        configureInputForm.getExcludedDates());
                                if (configureInputForm.getReleaseOnly().isSelected()) { // Tasks and subtasks                                    
                                    ChartList.getList().refreshDateRange(configureInputForm.getStartDate(), configureInputForm.getEndDate(), datesToBeIncluded, true, chooseInputForm.getDataSubtasksCheckBox().isSelected());
                                } else if (configureInputForm.getReleaseAndIteration().isSelected()) { // Tasks and subtasks
                                    ChartList.getList().refreshDateRange(configureInputForm.getStartDate(), configureInputForm.getEndDate(), datesToBeIncluded, false, chooseInputForm.getDataSubtasksCheckBox().isSelected());
                                } else if (configureInputForm.getIterationOnly().isSelected()) { // Tasks only
                                    ChartList.getList().refreshDateRangeAndIteration(configureInputForm.getStartDate(), configureInputForm.getEndDate(), datesToBeIncluded, configureInputForm.getIteration());
                                }
                            } else if (configureInputForm.getIterationsCheckBox().isSelected()) { // Tasks only
                                ChartList.getList().refreshIterationRange(configureInputForm.getStartIteration(), configureInputForm.getEndIteration());
                            }
                        }
                    });
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            //checkPanel.refresh();
                            chartTabbedPanel.goToStep3();
                        }
                    });
                }
            }
        });
        checkButton.setMinimumSize(CREATEBUTTON_DIMENSION);
        checkButton.setMaximumSize(CREATEBUTTON_DIMENSION);
        checkButton.setPreferredSize(CREATEBUTTON_DIMENSION);
        add(checkButton, gbc);
    }
}
