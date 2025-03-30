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

import com.jtattoo.plaf.noire.NoireLookAndFeel;
import java.awt.Color;
import java.util.Properties;
import org.mypomodoro.Main;
import org.mypomodoro.util.ColorUtil;

/**
 * MAP look and Feel RGB codes
 *
 * Based on JTattoo's Acryl Look And Feel http://www.jtattoo.net/ThemeProps.html
 *
 */
public class MAPLookAndFeel extends NoireLookAndFeel {

    protected Color DARK_COLOR = new Color(200, 42, 42); // dark red
    protected Color COLOR = new Color(216, 54, 54); // red
    protected Color FOREGROUND_COLOR = Color.WHITE; // white
    protected Properties props = new Properties();

    public MAPLookAndFeel() {
        // icon set path
        Main.iconsSetPath = "/images/icons_light_set/";
        setProperties();
        setCurrentTheme(props);
    }

    protected void setProperties() {
        // Table colors        
        Main.tableBackgroundColor = ColorUtil.WHITE;
        Main.myIconBackgroundColor = COLOR;

        // JTatoo theme settings
        props.put("logoString", "");

        // Main window background and foreground colors
        props.put("windowTitleForegroundColor", ColorUtil.toProperty(FOREGROUND_COLOR));
        props.put("windowTitleBackgroundColor", ColorUtil.toProperty(COLOR));
        props.put("windowTitleColorLight", ColorUtil.toProperty(COLOR));
        props.put("windowTitleColorDark", ColorUtil.toProperty(COLOR));
        props.put("windowBorderColor", ColorUtil.toProperty(COLOR));

        // (Inactive) Main window background and foreground colors (the window is inactive when opening a dialog message)
        props.put("windowInactiveTitleForegroundColor", ColorUtil.toProperty(FOREGROUND_COLOR));
        props.put("windowInactiveTitleBackgroundColor", ColorUtil.toProperty(COLOR));
        props.put("windowInactiveTitleColorLight", ColorUtil.toProperty(COLOR));
        props.put("windowInactiveTitleColorDark", ColorUtil.toProperty(COLOR));
        props.put("windowInactiveBorderColor", ColorUtil.toProperty(COLOR));

        // Background and foreground colors
        props.put("backgroundColor", ColorUtil.toProperty(COLOR));
        props.put("foregroundColor", ColorUtil.toProperty(FOREGROUND_COLOR));
        props.put("backgroundColorLight", ColorUtil.toProperty(COLOR)); // these 2 lines are very important to override the background color of the Noire theme
        props.put("backgroundColorDark", ColorUtil.toProperty(COLOR));

        // Menu background colors
        props.put("menuColorLight", ColorUtil.toProperty(COLOR));
        props.put("menuColorDark", ColorUtil.toProperty(DARK_COLOR));

        // Menu foreground colors
        props.put("menuForegroundColor", ColorUtil.toProperty(FOREGROUND_COLOR));

        // Menu items background colors
        props.put("menuBackgroundColor", ColorUtil.toProperty(DARK_COLOR));
        props.put("menuSelectionBackgroundColor", ColorUtil.toProperty(COLOR));
        props.put("menuSelectionBackgroundColorLight", ColorUtil.toProperty(COLOR));
        props.put("menuSelectionBackgroundColorDark", ColorUtil.toProperty(COLOR));

        // Menu items foreground colors
        props.put("menuSelectionForegroundColor", ColorUtil.toProperty(FOREGROUND_COLOR));

        // Input/fields background and foreground colors
        props.put("inputBackgroundColor", ColorUtil.toProperty(ColorUtil.WHITE)); // this must be white
        props.put("inputForegroundColor", ColorUtil.toProperty(ColorUtil.BLACK));

        // Selection background and foreground colors
        props.put("selectionBackgroundColor", ColorUtil.toProperty(COLOR));
        props.put("selectionBackgroundColorLight", ColorUtil.toProperty(COLOR));
        props.put("selectionBackgroundColorDark", ColorUtil.toProperty(DARK_COLOR));
        props.put("selectionForegroundColor", ColorUtil.toProperty(FOREGROUND_COLOR));

        // Buttons
        props.put("buttonBackgroundColor", ColorUtil.toProperty(DARK_COLOR));
        props.put("buttonForegroundColor", ColorUtil.toProperty(FOREGROUND_COLOR));
        props.put("buttonColorLight", ColorUtil.toProperty(COLOR));
        props.put("buttonColorDark", ColorUtil.toProperty(DARK_COLOR));

        // Tooltip
        props.put("tooltipBackgroundColor", ColorUtil.toProperty(DARK_COLOR));
        props.put("tooltipForegroundColor", ColorUtil.toProperty(FOREGROUND_COLOR));
        props.put("tooltipBorderSize", "0");

        // Tabbed panel
        props.put("controlBackgroundColor", ColorUtil.toProperty(COLOR)); // tabbed pane background
        props.put("controlForegroundColor", ColorUtil.toProperty(FOREGROUND_COLOR)); // tab and dialog foreground
        props.put("controlColorLight", ColorUtil.toProperty(COLOR)); // this must be set for rollover prop to work
        props.put("controlColorDark", ColorUtil.toProperty(COLOR)); // this must be set for rollover prop to work

        // Roll over buttons, table headers, tabs, checkboxes
        props.put("rolloverColor", ColorUtil.toProperty(COLOR));
        props.put("rolloverColorLight", ColorUtil.toProperty(COLOR));
        props.put("rolloverColorDark", ColorUtil.toProperty(COLOR));

        // Missing property : rollover foreground color for buttons
    }
}
