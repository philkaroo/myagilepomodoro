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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboBoxUI;
import static org.mypomodoro.util.TimeConverter.getLength;

/**
 *
 *
 */
public class ComboBoxPanel extends JPanel {

    protected JLabel labelBefore = new JLabel();
    protected JComboBox comboBox;
    protected JLabel labelAfter = new JLabel();

    // Generic constructor
    public <E> ComboBoxPanel(E[] data, boolean editable) {
        setLayout(new GridBagLayout());
        add(labelBefore);
        comboBox = new JComboBox();
        for (E d : data) { // jdk 7 : simply use comboBox = new JComboBox<E>(data);
            comboBox.addItem(d);
        }
        setOpaque(true);
        comboBox.setEditable(editable);
        if (data instanceof String[]) { // combo of strings
            add(comboBox, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 3, 2, 3), 0, 0));
        } else { // combo of numbers            
            // Hide combo arrow
            comboBox.setUI(new BasicComboBoxUI() {

                @Override
                protected JButton createArrowButton() {
                    return new JButton() {

                        @Override
                        public int getWidth() {
                            return 0;
                        }
                    };
                }
            });
            add(comboBox, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 5, 2, 5), 0, 0));
        }
        add(labelAfter);
    }

    public class ComboBoxFloatRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String text;
            if (value.toString().equals("0.5")) {
                text = "1/2";
            } else {
                text = Math.round((Float) value) + ""; // used Math.round to display SP as integer (eg: 1.0 --> 1 but 1.6 --> 2)
            }
            setText(text);
            return this;
        }
    }

    public class ComboBoxEstimatedLengthRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String length = getLength(Integer.parseInt(value.toString()));
            setToolTipText(length);
            return this;
        }
    }

    public class ComboBoxStringRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (!((String) value).trim().isEmpty()) {
                setToolTipText((String) value);
            } else {
                setToolTipText(null);
            }
            return this;
        }
    }

    public class ComboBoxIterationRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String text = value.toString();
            if (value.toString().equals("-1")) {
                text = " ";
            }
            setText(text);
            return this;
        }
    }
}
