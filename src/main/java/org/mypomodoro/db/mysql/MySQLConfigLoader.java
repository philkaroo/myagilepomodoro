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
package org.mypomodoro.db.mysql;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.mypomodoro.Main;

/**
 * Loader for MySQL config properties file
 *
 * File mysql.properties is located where mypomodoro.jar is
 *
 */
public class MySQLConfigLoader {

    private static final Properties properties = new Properties();

    public MySQLConfigLoader() {
        try {
            loadProperties();
        } catch (IOException ignored) {
            // no logger here, there is no properties file to be found if MySQL isn't used.
        }
    }

    private void loadProperties() throws IOException {
        String path = Main.configPath + "mysql.properties";
        FileInputStream file = new FileInputStream(path);
        properties.load(file);
        file.close();
    }

    public static boolean isValid() {
        return getPassword() != null && getUser() != null && getHost() != null && getDatabase() != null;
    }

    public static String getPassword() { // may be empty
        return properties.getProperty("password");
    }

    public static String getUser() { // eg root
        return properties.getProperty("user");
    }

    public static String getHost() { // eg 127.0.0.1:3306
        return properties.getProperty("host");
    }

    public static String getDatabase() { // eg pomodoro
        return properties.getProperty("database");
    }
}
