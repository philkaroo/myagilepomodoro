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
package org.mypomodoro.util;

import java.awt.Color;

/**
 * Color util
 *
 */
public class ColorUtil {

    public static final Color WHITE = Color.WHITE;
    public static final Color BLACK = Color.BLACK;
    public static final Color RED = new Color(216, 54, 54);
    public static final Color GREEN = new Color(0, 153, 26);
    public static final Color YELLOW_CHART = new Color(249, 192, 9);
    public static final Color RED_CHART = new Color(228, 92, 17);
    public static final Color BLUE_ROW_LIGHT = new Color(235, 245, 252);
    public static final Color BLUE_ROW = new Color(200, 221, 242);
    public static final Color BLUE_ROW_DARKER = new Color(200, 221, 242);
    public static final Color YELLOW_ROW = new Color(255, 255, 204);
    public static final Color GRAY = Color.gray;
    public static final Color YELLOW_HIGHLIGHT = new Color(255, 255, 102);
    public static final Color DARK_GRAY_TIMER = Color.DARK_GRAY;
    public static final Color GREEN_TIMER = new Color(156, 234, 156);

    /**
     * Returns a web browser-friendly HEX value representing the colour in the
     * default sRGB ColorModel.
     *
     * Adapted from http://sny.no/2011/11/java-hex
     *
     * @param color
     * @return a browser-friendly HEX value
     */
    public static String toHex(Color color) {
        return "#" + toBrowserHexValue(color.getRed()) + toBrowserHexValue(color.getGreen()) + toBrowserHexValue(color.getBlue());
    }

    public static String toProperty(Color color) {
        return color.getRed() + " " + color.getGreen() + " " + color.getBlue();
    }

    private static String toBrowserHexValue(int number) {
        StringBuilder builder = new StringBuilder(Integer.toHexString(number & 0xff));
        while (builder.length() < 2) {
            builder.append("0");
        }
        return builder.toString().toUpperCase();
    }
}
