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
package org.mypomodoro.buttons;

import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * Template button
 *
 */
public class DefaultButton extends JButton {

    public DefaultButton() {
        setFocusPainted(false); // removes borders around text
        setRolloverEnabled(true);
        setFont(getFont().deriveFont(Font.BOLD));
    }

    public DefaultButton(String text) {
        super(text);
        setFocusPainted(false); // removes borders around text
        setRolloverEnabled(true);
        setFont(getFont().deriveFont(Font.BOLD));
    }

    public DefaultButton(ImageIcon icon) {
        this(icon, false);
    }

    public DefaultButton(ImageIcon icon, Boolean removeBorder) {
        super(icon);
        setFocusPainted(false); // removes borders around text
        setRolloverEnabled(true);
        if (removeBorder) {
            setBorder(null);
        }
    }
}
