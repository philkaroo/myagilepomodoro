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
package org.mypomodoro.gui;

import javax.swing.ImageIcon;
import org.mypomodoro.Main;

public class ImageIcons {

    public static final ImageIcon MAIN_ICON_0 = getIcon("/images/pomodoro16-0.png");
    public static final ImageIcon MAIN_ICON_12_5 = getIcon("/images/pomodoro16-12.5.png");
    public static final ImageIcon MAIN_ICON_25 = getIcon("/images/pomodoro16-25.png");
    public static final ImageIcon MAIN_ICON_37_5 = getIcon("/images/pomodoro16-37.5.png");
    public static final ImageIcon MAIN_ICON_50 = getIcon("/images/pomodoro16-50.png");
    public static final ImageIcon MAIN_ICON_62_5 = getIcon("/images/pomodoro16-62.5.png");
    public static final ImageIcon MAIN_ICON_75 = getIcon("/images/pomodoro16-75.png");
    public static final ImageIcon MAIN_ICON_87_5 = getIcon("/images/pomodoro16-87.5.png");
    public static final ImageIcon MAIN_ICON = getIcon("/images/pomodoro16.png");
    public static final ImageIcon DIALOG_ICON = getIcon("/images/pomodoro48.png");
    public static final ImageIcon[] MAIN_ICON_PROGRESSIVE = new ImageIcon[]{
        MAIN_ICON_0,
        MAIN_ICON_12_5,
        MAIN_ICON_25,
        MAIN_ICON_37_5,
        MAIN_ICON_50,
        MAIN_ICON_62_5,
        MAIN_ICON_75,
        MAIN_ICON_87_5,
        MAIN_ICON
    };
    public static final ImageIcon SPLASH_ICON = getIcon("/images/splash_mAP.png");

    private static ImageIcon getIcon(String resourcePath) {
        return new ImageIcon(Main.class.getResource(resourcePath));
    }
}
