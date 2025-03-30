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
public class MAPLookAndFeelOptimism extends MAPLookAndFeel {

    public MAPLookAndFeelOptimism() {
        DARK_COLOR = new Color(237, 89, 0); // dark orange
        COLOR = new Color(253, 101, 0); // orange
        FOREGROUND_COLOR = Color.WHITE; // white
        // icon set path
        Main.iconsSetPath = "/images/icons_light_set/";
        Main.mAPIconTimer = "mAPIconTimerOptimism.png";
        setProperties();
        setCurrentTheme(props);
    }
}
