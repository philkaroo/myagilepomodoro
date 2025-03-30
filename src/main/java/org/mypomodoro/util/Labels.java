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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.mypomodoro.Main;

/**
 * Labels bundle
 *
 */
public class Labels {

    private final static String BUNDLE_NAME = "org.mypomodoro.labels.mypomodoro";
    private static ResourceBundle resource_bundle;

    public Labels(Locale locale) {
        resource_bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
    }

    public static String getString(String key) {
        try {
            return resource_bundle.getString(key);
        } catch (MissingResourceException ex) {
            Main.logger.error("", ex);
            return '!' + key + '!';
        }
    }

    public static String getString(String key, Object... params) {
        try {
            return MessageFormat.format(resource_bundle.getString(key), params);
        } catch (MissingResourceException ex) {
            Main.logger.error("", ex);
            return '!' + key + '!';
        }
    }

    public static Locale getLocale() {
        return resource_bundle.getLocale();
    }
}
