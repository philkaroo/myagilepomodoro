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
package org.mypomodoro.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mypomodoro.Main;

public class PreferencesDAO {

    private final Database database = Main.database;
    private static final PreferencesDAO instance = new PreferencesDAO();

    public static PreferencesDAO getInstance() {
        return instance;
    }

    PreferencesDAO() {
        database.createPreferencesTable();
    }

    public void load() {
        database.lock();
        try {
            ResultSet rs = database.query("SELECT * FROM preferences;");
            try {
                if (rs.next()) {
                    Main.preferences.setPomodoroLength(rs.getInt("pom_length"));
                    Main.preferences.setShortBreakLength(rs.getInt("short_break_length"));
                    Main.preferences.setLongBreakLength(rs.getInt("long_break_length"));
                    Main.preferences.setMaxNbPomPerActivity(rs.getInt("max_nb_pom_per_activity"));
                    Main.preferences.setMaxNbPomPerDay(rs.getInt("max_nb_pom_per_day"));
                    Main.preferences.setNbPomPerSet(rs.getInt("nb_pom_per_set"));
                    Main.preferences.setTicking(rs.getInt("ticking") == 1);
                    Main.preferences.setRinging(rs.getInt("ringing") == 1);
                    String locale = rs.getString("locale");
                    String regularExpression = "[a-z]{2}_[A-Z]{2}_[a-zA-Z]+"; // locale with variant        
                    Pattern pat = Pattern.compile(regularExpression);
                    Matcher mat = pat.matcher(locale);
                    if (mat.find()) {
                        Main.preferences.setLocale(new Locale(locale.substring(0, 2), locale.substring(3, 5), locale.substring(6)));
                    } else {
                        regularExpression = "[a-z]{2}_[A-Z]{2}"; // locale without variant
                        pat = Pattern.compile(regularExpression);
                        mat = pat.matcher(locale);
                        if (mat.find()) {
                            Main.preferences.setLocale(new Locale(locale.substring(0, 2), locale.substring(3, 5)));
                        }
                    }
                    Main.preferences.setSystemTray(rs.getInt("system_tray") == 1);
                    Main.preferences.setSystemTrayMessage(rs.getInt("system_tray_msg") == 1);
                    Main.preferences.setAlwaysOnTop(rs.getInt("always_on_top") == 1);
                    Main.preferences.setAgileMode(rs.getInt("agile_mode") == 1);
                    Main.preferences.setPlainHours(rs.getInt("plain_hours") == 1);
                    Main.preferences.setBringToFront(rs.getInt("bring_to_front") == 1);
                    Main.preferences.setTheme(rs.getString("theme"));
                }
            } catch (SQLException ex) {
                // Upgrade from 3.0, 3.1 or 3.2 TO 3.3, 3.4, 4.0, 4.1, 4.1.1
                /*Main.logger.error("Fixing following issue... Done", ex);
                 database.update("ALTER TABLE preferences ADD bring_to_front BOOLEAN DEFAULT 0;");
                 String mAPLookAndFeel = MAPLookAndFeel.class.getPackage().getName() + ".MAPLookAndFeel";
                 if (MySQLConfigLoader.isValid()) {
                 database.update("ALTER TABLE preferences ADD theme VARCHAR(255) DEFAULT '" + mAPLookAndFeel + "';");
                 } else {
                 database.update("ALTER TABLE preferences ADD theme TEXT DEFAULT '" + mAPLookAndFeel + "';");
                 }
                 Main.preferences.setBringToFront(false);
                 Main.preferences.setTheme(mAPLookAndFeel);*/
            } finally {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Main.logger.error("", ex);
                }
            }
        } finally {
            database.unlock();
        }
    }

    public void update() {
        String updateSQL = "UPDATE preferences SET "
                + "pom_length = " + Main.preferences.getPomodoroLength() + ", "
                + "short_break_length = " + Main.preferences.getShortBreakLength() + ", "
                + "long_break_length = " + Main.preferences.getLongBreakLength() + ", "
                + "max_nb_pom_per_activity = " + Main.preferences.getMaxNbPomPerActivity() + ", "
                + "max_nb_pom_per_day = " + Main.preferences.getMaxNbPomPerDay() + ", "
                + "nb_pom_per_set = " + Main.preferences.getNbPomPerSet() + ", "
                + "ticking = " + (Main.preferences.getTicking() ? 1 : 0) + ", "
                + "ringing = " + (Main.preferences.getRinging() ? 1 : 0) + ", "
                + "locale = '" + Main.preferences.getLocale().toString() + "'" + ", "
                + "system_tray = " + (Main.preferences.getSystemTray() ? 1 : 0) + ", "
                + "system_tray_msg = " + (Main.preferences.getSystemTrayMessage() ? 1 : 0) + ", "
                + "always_on_top = " + (Main.preferences.getAlwaysOnTop() ? 1 : 0) + ", "
                + "agile_mode = " + (Main.preferences.getAgileMode() ? 1 : 0) + ", "
                + "plain_hours = " + (Main.preferences.getPlainHours() ? 1 : 0) + ", "
                + "bring_to_front = " + (Main.preferences.getBringToFront() ? 1 : 0) + ", "
                + "theme = '" + Main.preferences.getTheme().toString() + "'" + ";";
        database.lock();
        try {
            database.update("begin;");
            database.update(updateSQL);
            database.update("commit;");
        } finally {
            database.unlock();
        }
    }
}
