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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;
import org.mypomodoro.Main;
import org.mypomodoro.gui.activities.AbstractComboBoxRenderer;
import org.mypomodoro.gui.create.FormLabel;
import org.mypomodoro.util.ComponentTitledBorder;
import org.mypomodoro.util.DatePicker;
import org.mypomodoro.util.DateUtil;
import org.mypomodoro.util.Labels;

/**
 * Configure chart form
 *
 */
public class ConfigureInputForm extends JPanel {

    protected static final Dimension LABEL_DIMENSION = new Dimension(400, 100);
    // Tasks form
    private final JPanel dataInputFormPanel = new JPanel();
    private final JPanel scopeInputFormPanel = new JPanel();
    private final JComboBox iterationonlyComboBox = new JComboBox();
    // Dates form
    private final JPanel datesInputFormPanel = new JPanel();
    protected final DatePicker startDatePicker = new DatePicker(Labels.getLocale());
    protected final DatePicker endDatePicker = new DatePicker(Labels.getLocale());
    private final JCheckBox excludeSaturdays = new JCheckBox(Labels.getString("BurndownChartPanel.Saturdays"), true);
    private final JCheckBox excludeSundays = new JCheckBox(Labels.getString("BurndownChartPanel.Sundays"), true);
    private final JCheckBox typeReleaseAndIteration = new JCheckBox(Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "ToDoListPanel.ToDo List") + " + " + Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "ReportListPanel.Report List"), true);
    private final JCheckBox typeReleaseOnly = new JCheckBox(Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "ReportListPanel.Report List"), false);
    private final JCheckBox typeIterationOnly = new JCheckBox(Labels.getString("Agile.Common.Iteration"), false);
    private final DatePicker excludeDatePicker = new DatePicker(Labels.getLocale());
    private final ArrayList<Date> excludedDates = new ArrayList<Date>();
    final JCheckBox datesCheckBox = new JCheckBox(Labels.getString("BurndownChartPanel.Dates"), true);
    final ComponentTitledBorder borderDates = new ComponentTitledBorder(datesCheckBox, datesInputFormPanel, new EtchedBorder(), datesCheckBox.getFont().deriveFont(Font.BOLD));
    // Iterations form
    private final JPanel iterationsInputFormPanel = new JPanel();
    private final JComboBox startIteration = new JComboBox();
    private final JComboBox endIteration = new JComboBox();
    final JCheckBox iterationsCheckBox = new JCheckBox(Labels.getString("BurndownChartPanel.Iterations"), true);
    private final ComponentTitledBorder borderIterations = new ComponentTitledBorder(iterationsCheckBox, iterationsInputFormPanel, new EtchedBorder(), iterationsCheckBox.getFont().deriveFont(Font.BOLD));
    // Dimension
    private final JPanel dimensionInputFormPanel = new JPanel();
    private final JTextField chartWidth = new JTextField("770");
    private final JTextField chartHeight = new JTextField("410");

    private ChooseInputForm chooseInputForm;
    private final JPanel iteration = new JPanel();

    public ConfigureInputForm() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        datesInputFormPanel.setPreferredSize(new Dimension(500, 140));
        iterationsInputFormPanel.setPreferredSize(new Dimension(500, 80));
        addDataInputFormPanel();
        addScopeInputFormPanel();
        addImageInputFormPanel();
    }

    public void refresh(ChooseInputForm chooseInputForm) {
        this.chooseInputForm = chooseInputForm;
        // Refresh form depending on Choose opions (tasks or subtasks)
        if (chooseInputForm.getDataSubtasksCheckBox().isSelected()) {
            iteration.setVisible(false);
            if (typeIterationOnly.isSelected()) {
                typeIterationOnly.setSelected(false);
                typeReleaseAndIteration.setSelected(true);
            }
            iterationsInputFormPanel.setVisible(false);
            if (iterationsCheckBox.isSelected()) {
                datesCheckBox.setSelected(true);
                iterationsCheckBox.setSelected(false);
            }
        } else {
            iteration.setVisible(true);
            if (typeReleaseAndIteration.isSelected()) {
                iterationsInputFormPanel.setVisible(true);
            }
        }
    }

    /////////////////////////////////////
    /////// DATA ////////////////////////
    /////////////////////////////////////
    private void addDataInputFormPanel() {
        JLabel titleBorderData = new JLabel(" " + Labels.getString("BurndownChartPanel.Data") + " ");
        titleBorderData.setOpaque(true);
        ComponentTitledBorder borderData = new ComponentTitledBorder(titleBorderData, dataInputFormPanel, new EtchedBorder(), titleBorderData.getFont().deriveFont(Font.BOLD));
        GridBagConstraints cChart = new GridBagConstraints();
        cChart.weightx = 1;
        cChart.weighty = 1;
        cChart.fill = GridBagConstraints.CENTER;
        cChart.insets = new Insets(0, 5, 2, 5);
        dataInputFormPanel.setBorder(borderData);
        dataInputFormPanel.setLayout(new GridBagLayout());
        addListFields(cChart);
        add(dataInputFormPanel, cChart);
    }

    private void addListFields(final GridBagConstraints cChart) {
        cChart.gridx = 0;
        cChart.gridy = 0;
        JPanel lists = new JPanel();
        lists.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        typeReleaseAndIteration.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                typeReleaseAndIteration.setSelected(true);
                typeReleaseOnly.setSelected(false);
                if (Main.preferences.getAgileMode() && chooseInputForm.getDataTasksCheckBox().isSelected()) {
                    typeIterationOnly.setSelected(false);
                    iterationsInputFormPanel.setVisible(true);
                }
            }
        });
        JPanel releaseanditeration = new JPanel();
        releaseanditeration.setLayout(new FlowLayout());
        releaseanditeration.add(typeReleaseAndIteration);
        gbc.gridx = 0;
        gbc.gridy = 0;
        lists.add(releaseanditeration, gbc); // include ToDos /Iteration Backlog tasks
        // ReportList / Release Backlog only
        typeReleaseOnly.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                typeReleaseAndIteration.setSelected(false);
                typeReleaseOnly.setSelected(true);
                datesCheckBox.setSelected(true); // force use of dates
                if (Main.preferences.getAgileMode()) {
                    typeIterationOnly.setSelected(false);
                    iterationsInputFormPanel.setVisible(false);
                    iterationsCheckBox.setSelected(false);
                }
            }
        });
        JPanel releaseonly = new JPanel();
        releaseonly.setLayout(new FlowLayout());
        releaseonly.add(typeReleaseOnly);
        gbc.gridx = 1;
        gbc.gridy = 0;
        lists.add(releaseonly, gbc); // excludes ToDos/Iteration Backlog tasks
        // Specific iteration                   
        if (Main.preferences.getAgileMode()) {
            iteration.setLayout(new FlowLayout());
            typeIterationOnly.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent event) {
                    typeReleaseAndIteration.setSelected(false);
                    typeReleaseOnly.setSelected(false);
                    datesCheckBox.setSelected(true); // force use of dates
                    typeIterationOnly.setSelected(true);
                    iterationsInputFormPanel.setVisible(false);
                    iterationsCheckBox.setSelected(false);
                }
            });
            iteration.add(typeIterationOnly); // only iteration
            for (int i = 0; i <= 100; i++) {
                iterationonlyComboBox.addItem(i);
            }
            iterationonlyComboBox.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent event) {
                    typeReleaseAndIteration.setSelected(false);
                    typeReleaseOnly.setSelected(false);
                    datesCheckBox.setSelected(true); // force use of dates
                    typeIterationOnly.setSelected(true);
                    iterationsInputFormPanel.setVisible(false);
                    iterationsCheckBox.setSelected(false);
                }
            });
            iteration.add(iterationonlyComboBox);
            gbc.gridx = 2;
            gbc.gridy = 0;
            lists.add(iteration, gbc);
        }
        dataInputFormPanel.add(lists, cChart);
    }

    /////////////////////////////////////
    /////// SCOPE ///////////////////////
    /////////////////////////////////////    
    private void addScopeInputFormPanel() {
        JLabel titleBorderScope = new JLabel(" " + Labels.getString("BurndownChartPanel.Dates") + " ");
        titleBorderScope.setOpaque(true);
        ComponentTitledBorder borderScope = new ComponentTitledBorder(titleBorderScope, scopeInputFormPanel, new EtchedBorder(), titleBorderScope.getFont().deriveFont(Font.BOLD));
        GridBagConstraints cChart = new GridBagConstraints();
        cChart.weightx = 1;
        cChart.weighty = 1;
        cChart.fill = GridBagConstraints.CENTER;
        cChart.insets = new Insets(0, 5, 2, 5);
        scopeInputFormPanel.setBorder(borderScope);
        scopeInputFormPanel.setLayout(new GridBagLayout());
        addDatesInputFormPanel(cChart);
        if (Main.preferences.getAgileMode()) {
            addIterationsInputFormPanel(cChart);
        }
        add(scopeInputFormPanel, cChart);
    }

    private void addDatesInputFormPanel(final GridBagConstraints cChart) {
        cChart.gridx = 0;
        cChart.gridy = 0;
        datesCheckBox.setFocusPainted(false);
        datesCheckBox.setSelected(true);
        datesCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                datesCheckBox.setSelected(true);
                iterationsCheckBox.setSelected(false);
                borderIterations.repaint();
            }
        });
        datesInputFormPanel.setBorder(borderDates);
        datesInputFormPanel.setLayout(new GridBagLayout());

        addDatesFields();
        scopeInputFormPanel.add(datesInputFormPanel, cChart);
    }

    private void addIterationsInputFormPanel(final GridBagConstraints cChart) {
        cChart.gridx = 0;
        cChart.gridy = 1;
        iterationsCheckBox.setFocusPainted(false);
        iterationsCheckBox.setSelected(false);
        iterationsCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                iterationsCheckBox.setSelected(true);
                datesCheckBox.setSelected(false);
                borderDates.repaint();
            }
        });
        iterationsInputFormPanel.setBorder(borderIterations);
        iterationsInputFormPanel.setLayout(new GridBagLayout());
        addIterationsFields();
        scopeInputFormPanel.add(iterationsInputFormPanel, cChart);
    }

    private void addDatesFields() {
        JPanel dates = new JPanel();
        dates.setLayout(new GridBagLayout());
        GridBagConstraints datesgbc = new GridBagConstraints();
        datesgbc.weightx = 1;
        datesgbc.weighty = 1;
        datesgbc.fill = GridBagConstraints.BOTH;
        datesgbc.insets = new Insets(0, 5, 2, 5);
        // Date pickers
        FormLabel dateslabel = new FormLabel(Labels.getString("BurndownChartPanel.Dates") + "*: ");
        //dateslabel.setMinimumSize(LABEL_DIMENSION);
        //dateslabel.setPreferredSize(LABEL_DIMENSION);
        datesgbc.gridx = 0;
        datesgbc.gridy = 0;
        dates.add(dateslabel, datesgbc);
        datesgbc.gridx = 1;
        datesgbc.gridy = 0;
        startDatePicker.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // if end date is sooner than start date set end date to start date
                if (DateUtil.isSooner(endDatePicker.getDate(), startDatePicker.getDate())) {
                    endDatePicker.setDate(startDatePicker.getDate());
                }
                endDatePicker.setDateWithLowerBounds(startDatePicker.getDate());
                // select iterations check box whenever the combo box is used
                datesCheckBox.setSelected(true);
                iterationsCheckBox.setSelected(false);
                borderDates.repaint();
                borderIterations.repaint();
            }
        });
        dates.add(startDatePicker, datesgbc);
        datesgbc.gridx = 2;
        datesgbc.gridy = 0;
        endDatePicker.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // if end date is sooner than start date set start date to end date
                if (DateUtil.isSooner(endDatePicker.getDate(), startDatePicker.getDate())) {
                    startDatePicker.setDate(endDatePicker.getDate());
                }
                startDatePicker.setDateWithUpperBounds(endDatePicker.getDate());
                // select iterations check box whenever the combo box is used
                datesCheckBox.setSelected(true);
                iterationsCheckBox.setSelected(false);
                borderDates.repaint();
                borderIterations.repaint();
            }
        });
        dates.add(endDatePicker, datesgbc);
        // Exclusion
        // first line
        FormLabel exclusionlabel = new FormLabel(Labels.getString("BurndownChartPanel.Exclusion") + "*: ");
        //exclusionlabel.setMinimumSize(LABEL_DIMENSION);
        //exclusionlabel.setPreferredSize(LABEL_DIMENSION);
        datesgbc.gridx = 0;
        datesgbc.gridy = 1;
        dates.add(exclusionlabel, datesgbc);
        datesgbc.gridx = 1;
        datesgbc.gridy = 1;
        dates.add(excludeSaturdays, datesgbc);
        datesgbc.gridx = 2;
        datesgbc.gridy = 1;
        dates.add(excludeSundays, datesgbc);
        // second line
        datesgbc.gridx = 1;
        datesgbc.gridy = 2;
        final JTextArea excludedDatesTextArea = new JTextArea();
        excludedDatesTextArea.setEditable(false);
        // Set colors according to input settings and themes
        // because the area is not editable by default the colors has to be reset
        excludedDatesTextArea.setBackground(new JTextArea().getBackground());
        excludedDatesTextArea.setForeground(new JTextArea().getForeground());
        excludedDatesTextArea.setVisible(false);
        excludedDatesTextArea.setPreferredSize(new Dimension(270, 50));
        excludedDatesTextArea.setLineWrap(true); // enable wrapping
        excludeDatePicker.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!excludedDates.contains(excludeDatePicker.getDate())) {
                    excludedDates.add(excludeDatePicker.getDate());
                }
                String text = "";
                int increment = 1;
                for (Date date : excludedDates) {
                    if (increment > 1) {
                        text += ", ";
                    }
                    text += DateUtil.getFormatedDate(date);
                    increment++;
                }
                excludedDatesTextArea.setText(text);
                excludedDatesTextArea.setVisible(true);
            }
        });
        dates.add(excludeDatePicker, datesgbc);
        datesgbc.gridx = 2;
        datesgbc.gridy = 2;
        JButton reset = new JButton(Labels.getString("Common.Reset"));
        reset.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                excludedDates.clear();
                excludedDatesTextArea.setText("");
                excludedDatesTextArea.setVisible(false);
            }
        });
        dates.add(reset, datesgbc);
        datesgbc.gridx = 1;
        datesgbc.gridy = 3;
        datesgbc.gridwidth = 2;
        dates.add(excludedDatesTextArea, datesgbc);
        datesInputFormPanel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                // no use
            }

            @Override
            public void mousePressed(MouseEvent e) {
                datesCheckBox.setSelected(true);
                iterationsCheckBox.setSelected(false);
                borderDates.repaint();
                borderIterations.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // no use
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // no use
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // no use
            }
        });
        datesInputFormPanel.add(dates);
    }

    private void addIterationsFields() {
        // Iterations
        JPanel iterations = new JPanel();
        iterations.setLayout(new GridBagLayout());
        GridBagConstraints iterationsgbc = new GridBagConstraints();
        iterationsgbc.weightx = 1;
        iterationsgbc.weighty = 1;
        iterationsgbc.fill = GridBagConstraints.BOTH;
        iterationsgbc.insets = new Insets(0, 5, 2, 5);
        FormLabel iterationslabel = new FormLabel(Labels.getString("BurndownChartPanel.Iterations") + "*: ");
        //iterationslabel.setMinimumSize(LABEL_DIMENSION);
        //iterationslabel.setPreferredSize(LABEL_DIMENSION);
        iterationsgbc.gridx = 0;
        iterationsgbc.gridy = 0;
        iterations.add(iterationslabel, iterationsgbc);
        iterationsgbc.gridx = 1;
        iterationsgbc.gridy = 0;
        for (int i = 0; i <= 100; i++) {
            startIteration.addItem(new Integer(i));
        }
        startIteration.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if ((Integer) startIteration.getSelectedItem() > (Integer) endIteration.getSelectedItem()) {
                    endIteration.setSelectedItem(startIteration.getSelectedItem());
                }
                // select iterations check box whenever the combo box is used
                datesCheckBox.setSelected(false);
                iterationsCheckBox.setSelected(true);
                borderDates.repaint();
                borderIterations.repaint();
            }
        });
        startIteration.setRenderer(new AbstractComboBoxRenderer());
        iterations.add(startIteration, iterationsgbc);
        iterationsgbc.gridx = 2;
        iterationsgbc.gridy = 0;
        for (int i = 0; i <= 100; i++) {
            endIteration.addItem(new Integer(i));
        }
        endIteration.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if ((Integer) endIteration.getSelectedItem() < (Integer) startIteration.getSelectedItem()) {
                    startIteration.setSelectedItem(endIteration.getSelectedItem());
                }
                // select iterations check box whenever the combo box is used
                datesCheckBox.setSelected(false);
                iterationsCheckBox.setSelected(true);
                borderDates.repaint();
                borderIterations.repaint();
            }
        });
        endIteration.setRenderer(new AbstractComboBoxRenderer());
        iterations.add(endIteration, iterationsgbc);
        // select iterations check box whenever the panel is selected
        iterationsInputFormPanel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                // no use
            }

            @Override
            public void mousePressed(MouseEvent e) {
                datesCheckBox.setSelected(false);
                iterationsCheckBox.setSelected(true);
                borderDates.repaint();
                borderIterations.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // no use
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // no use
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // no use
            }
        });
        iterationsInputFormPanel.add(iterations);
    }

    /////////////////////////////////////
    /////// IMAGE ///////////////////////
    /////////////////////////////////////    
    private void addImageInputFormPanel() {
        JLabel titleBorderDimension = new JLabel(" " + Labels.getString("BurndownChartPanel.Image") + " ");
        titleBorderDimension.setOpaque(true);
        ComponentTitledBorder borderDimension = new ComponentTitledBorder(titleBorderDimension, dimensionInputFormPanel, new EtchedBorder(), titleBorderDimension.getFont().deriveFont(Font.BOLD));
        dimensionInputFormPanel.setBorder(borderDimension);
        dimensionInputFormPanel.setLayout(new GridBagLayout());
        addDimensionFields();
        add(dimensionInputFormPanel);
    }

    private void addDimensionFields() {
        // Iterations
        JPanel dimension = new JPanel();
        dimension.setLayout(new GridBagLayout());
        GridBagConstraints dimensionsgbc = new GridBagConstraints();
        dimensionsgbc.weightx = 1;
        dimensionsgbc.weighty = 1;
        dimensionsgbc.fill = GridBagConstraints.BOTH;
        dimensionsgbc.insets = new Insets(0, 5, 2, 5);
        FormLabel dimensionlabel = new FormLabel(Labels.getString("BurndownChartPanel.Dimensions") + "*: ");
        //dimensionlabel.setMinimumSize(LABEL_DIMENSION);
        //dimensionlabel.setPreferredSize(LABEL_DIMENSION);
        dimensionsgbc.gridx = 0;
        dimensionsgbc.gridy = 0;
        dimension.add(dimensionlabel, dimensionsgbc);
        dimensionsgbc.gridx = 1;
        dimensionsgbc.gridy = 0;
        chartWidth.setPreferredSize(new Dimension(40, 25));
        chartWidth.setHorizontalAlignment(SwingConstants.RIGHT);
        ((AbstractDocument) chartWidth.getDocument()).setDocumentFilter(new IntegerDocumentFilter());
        chartWidth.setCaretColor(new JTextField().getForeground()); // Set colors according to input settings and themes
        dimension.add(chartWidth, dimensionsgbc);
        dimensionsgbc.gridx = 2;
        dimensionsgbc.gridy = 0;
        dimension.add(new JLabel(" X "), dimensionsgbc);
        dimensionsgbc.gridx = 3;
        dimensionsgbc.gridy = 0;
        chartHeight.setPreferredSize(new Dimension(40, 25));
        chartHeight.setHorizontalAlignment(SwingConstants.RIGHT);
        ((AbstractDocument) chartHeight.getDocument()).setDocumentFilter(new IntegerDocumentFilter());
        chartHeight.setCaretColor(new JTextField().getForeground()); // Set colors according to input settings and themes
        dimension.add(chartHeight, dimensionsgbc);
        dimensionInputFormPanel.add(dimension);
    }

    // Getters
    public Date getStartDate() {
        return startDatePicker.getDate();
    }

    public Date getEndDate() {
        return endDatePicker.getDate();
    }

    public JCheckBox getExcludeSaturdays() {
        return excludeSaturdays;
    }

    public JCheckBox getExcludeSundays() {
        return excludeSundays;
    }

    public JCheckBox getReleaseOnly() {
        return typeReleaseOnly;
    }

    public JCheckBox getReleaseAndIteration() {
        return typeReleaseAndIteration;
    }

    public JCheckBox getIterationOnly() {
        return typeIterationOnly;
    }

    public int getIteration() {
        return (Integer) iterationonlyComboBox.getSelectedItem();
    }

    public ArrayList<Date> getExcludedDates() {
        return excludedDates;
    }

    public JCheckBox getDatesCheckBox() {
        return datesCheckBox;
    }

    public JCheckBox getIterationsCheckBox() {
        return iterationsCheckBox;
    }

    public int getStartIteration() {
        return (Integer) startIteration.getSelectedItem();
    }

    public int getEndIteration() {
        return (Integer) endIteration.getSelectedItem();
    }

    public int getChartWidth() {
        return chartWidth.getText().isEmpty() ? 0 : Integer.parseInt(chartWidth.getText());
    }

    public int getChartHeight() {
        return chartHeight.getText().isEmpty() ? 0 : Integer.parseInt(chartHeight.getText());
    }
}

/**
 * Filter that makes JtextField fields allow integers only
 *
 */
class IntegerDocumentFilter extends DocumentFilter {

    @Override
    public void insertString(FilterBypass fb, int off, String str, AttributeSet attr)
            throws BadLocationException {
        // remove non-digits
        fb.insertString(off, str.replaceAll("\\D++", ""), attr);
    }

    @Override
    public void replace(FilterBypass fb, int off, int len, String str, AttributeSet attr)
            throws BadLocationException {
        // remove non-digits
        fb.replace(off, len, str.replaceAll("\\D++", ""), attr);
    }
}
