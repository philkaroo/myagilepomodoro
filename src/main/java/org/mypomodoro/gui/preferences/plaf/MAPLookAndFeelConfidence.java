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
package org.mypomodoro.gui.preferences.plaf;

import java.awt.Color;
import org.mypomodoro.Main;

/**
 * Green mAP custom theme
 *
 */
public class MAPLookAndFeelConfidence extends MAPLookAndFeel {

    public MAPLookAndFeelConfidence() {
        //DARK_COLOR = new Color(230, 181, 0); // dark yellow
        //COLOR = new Color(246, 193, 0); // yellow
        DARK_COLOR = new Color(246, 193, 0); // dark yellow
        COLOR = new Color(246, 205, 12); // yellow
        FOREGROUND_COLOR = Color.BLACK; // black
        // icon set path
        Main.iconsSetPath = "/images/icons_dark_set/";
        Main.mAPIconTimer = "mAPIconTimerConfidence.png";
        setProperties();
        setCurrentTheme(props);
    }
}
