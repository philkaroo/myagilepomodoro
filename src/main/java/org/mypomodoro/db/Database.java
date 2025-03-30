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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.locks.ReentrantLock;
import org.mypomodoro.Main;
import org.mypomodoro.db.mysql.MySQLConfigLoader;
import org.mypomodoro.gui.preferences.plaf.MAPLookAndFeel;

/**
 * Database
 *
 */
public class Database {

    private final ReentrantLock lock = new ReentrantLock();
    private Connection connection = null;
    private Statement statement = null;
    private String driverClassName = "org.sqlite.JDBC";
    private final String databaseFileName = "myagilepomodoro.db";
    private String connectionStatement = "jdbc:sqlite:" + Main.configPath + databaseFileName;
    final public static String SQLLITE = "SQLLITE";
    final public static String MYSQL = "MYSQL";
    // SQLLite database specific
    private String autoIncrementKeyword = "AUTOINCREMENT";
    private String longInteger = "INTEGER";
    public String selectStatementSeqId = "SELECT seq FROM sqlite_sequence WHERE name = 'activities'";
    public String sequenceIdName = "seq";
    public static boolean firstTime = false;

    /*
     // Postgresql database specific
     autoIncrementKeyword = "???";
     longInteger = "???";
     selectStatementSeqId = "SELECT CURRVAL(pg_get_serial_sequence('activities','id'))";
     sequenceIdName = "pg_get_serial_sequence";
     */
    public Database() {
        if (MySQLConfigLoader.isValid()) { // Remote mode (using MySQL database)
            driverClassName = "com.mysql.jdbc.Driver";
            connectionStatement = "jdbc:mysql://" + MySQLConfigLoader.getHost() + "/" + MySQLConfigLoader.getDatabase() + "?"
                    + "user=" + MySQLConfigLoader.getUser() + "&password=" + MySQLConfigLoader.getPassword();
            // MySQL database specific
            autoIncrementKeyword = "AUTO_INCREMENT";
            longInteger = "BIGINT";
            selectStatementSeqId = "SELECT LAST_INSERT_ID()";
            sequenceIdName = "last_insert_id()";
        } else { // SQLite
            File file = new File(Main.configPath + databaseFileName);
            if (!file.exists()) { // SQLlite database not created yet            
                firstTime = true;
            }
        }
        // Connect to database
        try {
            Class.forName(driverClassName);
            connection = DriverManager.getConnection(connectionStatement);
            statement = connection.createStatement();
        } catch (ClassNotFoundException ex) {
            Main.logger.error("", ex);
        } catch (SQLException ex) {
            Main.logger.error("", ex);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public void close() {
        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ex) {
            Main.logger.error("", ex);
        }
    }

    public void update(String sql) {
        try {
            statement.executeUpdate(sql);
        } catch (SQLException ex) {
            Main.logger.error("", ex);
        }
    }

    public ResultSet query(String sql) {
        ResultSet rs = null;
        try {
            rs = statement.executeQuery(sql);
        } catch (SQLException ex) {
            Main.logger.error("", ex);
        }
        return rs;
    }

    public void init() {
        createActivitiesTable();
        createPreferencesTable();
    }

    // note: is_complete, is_unplanned, is_donedone should, one day, be made boolean
    public void createActivitiesTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS activities ( "
                + "id INTEGER PRIMARY KEY " + autoIncrementKeyword + ", "
                + "name TEXT, "
                + "type TEXT, "
                + "description TEXT, "
                + "notes TEXT, "
                + "author TEXT, "
                + "place TEXT, "
                + "date_added " + longInteger + ", "
                + "date_completed " + longInteger + ", "
                + "estimated_poms INTEGER, "
                + "actual_poms INTEGER, "
                + "overestimated_poms INTEGER, "
                + "is_complete TEXT, "
                + "is_unplanned TEXT, "
                + "num_interruptions INTEGER, "
                + "priority INTEGER, "
                + "num_internal_interruptions INTEGER, "
                + "story_points FLOAT, "
                + "iteration INTEGER, "
                + "parent_id INTEGER, "
                + "is_donedone TEXT, "
                + "date_donedone " + longInteger + ");";
        update(createTableSQL);
    }

    public void createPreferencesTable() {
        String createPreferencesTableSQL = "CREATE TABLE IF NOT EXISTS preferences ( "
                + "pom_length INTEGER DEFAULT 25, "
                + "short_break_length INTEGER DEFAULT 5, "
                + "long_break_length INTEGER DEFAULT 20, "
                + "max_nb_pom_per_activity INTEGER DEFAULT 20, "
                + "max_nb_pom_per_day INTEGER DEFAULT 10, "
                + "nb_pom_per_set INTEGER DEFAULT 4, "
                + "ticking BOOLEAN DEFAULT 1, "
                + "ringing BOOLEAN DEFAULT 1, "
                + "locale TEXT, "
                + "system_tray BOOLEAN DEFAULT 1, "
                + "system_tray_msg BOOLEAN DEFAULT 1, "
                + "always_on_top BOOLEAN DEFAULT 0, "
                + "agile_mode BOOLEAN DEFAULT 1, "
                + "plain_hours BOOLEAN DEFAULT 1, "
                + "bring_to_front BOOLEAN DEFAULT 0, "
                + "theme TEXT" + ");";
        update(createPreferencesTableSQL);
        initPreferencesTable();
    }

    private void initPreferencesTable() {
        String selectPreferencesSQL = "SELECT * FROM preferences;";
        ResultSet rs = query(selectPreferencesSQL);
        try {
            if (!rs.next()) { // make sure there is no row in the result set
                String insertPreferencesSQL = "INSERT INTO preferences ("
                        + "pom_length,short_break_length,long_break_length,"
                        + "max_nb_pom_per_activity,max_nb_pom_per_day,nb_pom_per_set,"
                        + "ticking,ringing,locale,system_tray,system_tray_msg,always_on_top,agile_mode,plain_hours,bring_to_front,theme) "
                        + "VALUES ("
                        + "25,5,20,20,10,4,1,1,'en_US',1,1,0,1,1,0,'" + MAPLookAndFeel.class.getPackage().getName() + ".MAPLookAndFeel" + "');";
                update(insertPreferencesSQL);
            }
        } catch (SQLException ex) {
            Main.logger.error("", ex);
        } finally {
            try {
                rs.close();
            } catch (SQLException ex) {
                Main.logger.error("", ex);
            }
        }
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public String getLongIntegerVarName() {
        return longInteger;
    }
}
