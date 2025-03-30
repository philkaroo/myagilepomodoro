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
package org.mypomodoro.gui.create.list;

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JList;
import org.mypomodoro.gui.activities.AbstractComboBoxRenderer;

/**
 * Template list combo box
 *
 */
public class AbstractComboBox extends JComboBox {

    final ComboBoxToolTipRenderer tooltipRenderer = new ComboBoxToolTipRenderer();

    public AbstractComboBox() {
        setRenderer(tooltipRenderer);
    }

    protected class ComboBoxToolTipRenderer extends AbstractComboBoxRenderer {

        ArrayList tooltips;

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (-1 < index && null != value && null != tooltips) {
                list.setToolTipText((String) tooltips.get(index));
            }
            return this;
        }

        public void setTooltips(ArrayList tooltips) {
            this.tooltips = tooltips;
        }
    }
}
