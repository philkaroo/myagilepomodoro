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
package org.mypomodoro.gui.export.google;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.mypomodoro.Main;

/**
 * Loader for Google config properties file
 *
 * File google.properties is located where mypomodoro.jar is
 *
 */
public class GoogleConfigLoader {

    private static final Properties properties = new Properties();

    public GoogleConfigLoader() {
        try {
            loadProperties();
        } catch (IOException ignored) {
            // no logger here, the properties file may not be there on purpose.
        }
    }

    private void loadProperties() throws IOException {
        String path = Main.configPath + "google.properties";
        FileInputStream file = new FileInputStream(path);
        properties.load(file);
        file.close();
    }

    public static boolean isValid() {
        return getClientId() != null && getClientSecret() != null && getRedirectURI() != null;
    }

    public static String getClientId() {
        return properties.getProperty("clientid");
    }

    public static String getClientSecret() {
        return properties.getProperty("clientsecret");
    }

    public static String getRedirectURI() {
        return properties.getProperty("redirecturi");
    }
}
