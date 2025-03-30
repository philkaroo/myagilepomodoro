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
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Combo Box Cell Renderer
 *
 */
public class ComboBoxCellRenderer extends ComboBoxPanel implements TableCellRenderer {

    public <E> ComboBoxCellRenderer(E[] data, boolean editable) {
        super(data, editable);

        // Custom display hovered item value
        comboBox.setRenderer(new AbstractComboBoxRenderer());
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        labelBefore.setFont(isSelected ? labelBefore.getFont().deriveFont(Font.BOLD) : labelBefore.getFont().deriveFont(Font.PLAIN));
        comboBox.setFont(isSelected ? comboBox.getFont().deriveFont(Font.BOLD) : comboBox.getFont().deriveFont(Font.PLAIN));
        labelAfter.setFont(isSelected ? labelAfter.getFont().deriveFont(Font.BOLD) : labelAfter.getFont().deriveFont(Font.PLAIN));
        if (value != null) {
            comboBox.setSelectedItem(value);
        }
        return this;
    }
}
