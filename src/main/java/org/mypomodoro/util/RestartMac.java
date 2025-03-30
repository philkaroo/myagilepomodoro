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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.mypomodoro.Main;

/**
 * This class Restarts Java Bundled Native Mac Applications with the .app
 * extension To use put the following where you want to call a restart:
 * <pre>
 *      RestartMac restart = new RestartMac(0);
 * </pre> Then put the following in your main() method to delete the files
 * created:
 * <pre>
 *      RestartMac restart = new RestartMac(1);
 * </pre>
 *
 */
public class RestartMac {

    /* AppleScript */
    private final String restartScript = "tell application \"myAgilePomodoro\" to quit" + System.getProperty("line.separator")
            + "tell application \"System Events\"" + System.getProperty("line.separator")
            + "repeat until not (exists process \"myAgilePomodoro\")" + System.getProperty("line.separator")
            + "delay 0.2" + System.getProperty("line.separator")
            + "end repeat" + System.getProperty("line.separator")
            + "end tell" + System.getProperty("line.separator")
            + "tell application \"myAgilePomodoro\" to activate";
    /* AppleScript FileName */
    private final File restartFile = new File("myAgilePomodoroRestart.scpt");
    /* Created Application FileName
     * Is created when the AppleScript is Compiled
     */
    private final String restartApp = "myAgilePomodoroRestart.app";
    /* String[] used to Compile AppleScript to Application */
    private final String[] osacompileString = new String[]{"/usr/bin/osacompile", "-o", restartApp, restartFile.toString()};
    /* String[] used to Open created Application */
    private final String[] openString = new String[]{"/usr/bin/open", restartApp};
    /*
     * String used to Delete created Application
     * VERY DANGEROUS IF THIS STRING IS CHANGED
     */
    private final String deleteString = "rm -rf " + restartApp;
    /* Compiles AppleScript to Application */
    private Process osacompile = null;
    /* Opens created Application */
    @SuppressWarnings("unused")
    private Process open = null;
    /* Deletes created Application */
    @SuppressWarnings("unused")
    private Process delete = null;
    /* Arguments for Constructor */
    @SuppressWarnings("unused")
    private final int argv;

    /**
     * Restarts YourApplication.app on Mac OS X
     *
     * @param argv
     */
    public RestartMac(int argv) {
        this.argv = argv;
        if (argv == 0) { //Use 0 when you call a restart, such as in FileMenuItem
            compileAppleScript();
            openApp();
        } else { //Use 1 in main, so on restart it removes the files created
            deleteScript();
            deleteApp();
        }
    }

    /*
     * Write AppleScript to a File
     */
    private void scriptToFile() {
        restartScript.replaceAll("\r", System.getProperty("line.separator")); // Macs OS 9 and earlier
        restartScript.replaceAll("\n", System.getProperty("line.separator")); // Unix and Mac OSX        
        try {
            BufferedWriter restartWriter = new BufferedWriter(new FileWriter(restartFile));
            restartWriter.write(restartScript);
            restartWriter.close();
        } catch (IOException ex) {
            Main.logger.error("", ex);
        }
    }

    /*
     * Compiles AppleScript to Application
     */
    private void compileAppleScript() {
        scriptToFile();
        try {
            osacompile = Runtime.getRuntime().exec(osacompileString);
            osacompile.waitFor(); //everything must wait until this process is completed
        } catch (InterruptedException ex) {
            Main.logger.error("", ex);
        } catch (IOException ex) {
            Main.logger.error("", ex);
        }
    }

    /*
     * Opens created Application
     */
    private void openApp() {
        try {
            open = Runtime.getRuntime().exec(openString);
        } catch (IOException ex) {
            Main.logger.error("", ex);
        }
    }

    /*
     * Deletes AppleScript if found
     */
    private void deleteScript() {
        if (restartFile.exists() && restartFile.isFile()) {
            restartFile.delete();
        }
    }

    /*
     * Deletes Created Application if found
     */
    private void deleteApp() {
        try {
            delete = Runtime.getRuntime().exec(deleteString);
        } catch (IOException ex) {
            Main.logger.error("", ex);
        }
    }
}
