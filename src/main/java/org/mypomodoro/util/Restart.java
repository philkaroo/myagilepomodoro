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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.SystemUtils;
import org.mypomodoro.Main;

/**
 * Restart application Code found at
 * http://java.dzone.com/articles/programmatically-restart-java
 *
 * Code modified to support non Hotspot VM implementation, EXE wrapper file and
 * changes since java 6u45
 * http://www.oracle.com/technetwork/java/javase/6u45-relnotes-1932876.html
 * (arguments required to be passed to Runtime.getRuntime().exec as String
 * array)
 *
 * Also support for spaces and special characters in the path
 *
 */
public class Restart {

    /**
     * Sun property pointing the main class and its arguments. Might not be
     * defined on non Hotspot VM implementations.
     */
    public static final String SUN_JAVA_COMMAND = "sun.java.command";

    /**
     * Restart the current Java application
     *
     * @param runBeforeRestart some custom code to be run before restarting
     */
    public static void restartApplication(Runnable runBeforeRestart) {
        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) { //for Mac OS X .app, need to additionally seperate .jar from .app b/c .jar still won't restart on mac
            new RestartMac(0);
        } else {
            try {
                // init the command to execute, add the vm args
                final ArrayList<String> cmd = new ArrayList<String>();
                // java binary
                String java = System.getProperty("java.home") + "/bin/java";
                cmd.add(java);
                // vm arguments
                List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
                for (String arg : vmArguments) {
                    // if it's the agent argument : we ignore it otherwise the
                    // address of the old application and the new one will be in conflict
                    if (!arg.contains("-agentlib")) {
                        cmd.add(arg);
                    }
                }
                // program main and program arguments
                String[] mainCommand = System.getProperty(SUN_JAVA_COMMAND).split(" ");
                String pathFile = ExecutablePath.getExecutablePath();
                if (pathFile.endsWith(".exe")) { // EXE wrapper
                    cmd.add("-jar");
                    cmd.add(new File(pathFile).toString());
                } else if (mainCommand != null && !mainCommand[0].isEmpty()) { // Hotspot VM implementation
                    if (pathFile.endsWith(".jar")) { // Jar file  
                        cmd.add("-jar");
                        cmd.add(new File(pathFile).toString());
                    } else { // Class file (running in IDE like Netbeans)
                        cmd.add("-cp");
                        cmd.add(System.getProperty("java.class.path"));
                        cmd.add(mainCommand[0]);
                        // Program arguments
                        for (int i = 1; i < mainCommand.length; i++) {
                            cmd.add(mainCommand[i]);
                        }
                    }
                } else { // Non Hotspot VM implementation
                    cmd.add("-jar");
                    cmd.add(new File(pathFile).toString());
                }

                // execute the command in a shutdown hook, to be sure that all the
                // resources have been disposed before restarting the application
                Runtime.getRuntime().addShutdownHook(new Thread() {

                    @Override
                    public void run() {
                        try {
                            Runtime.getRuntime().exec(cmd.toArray(new String[cmd.size()]));
                        } catch (IOException ex) {
                            Main.logger.error("", ex);
                        }
                    }
                });
                // execute some custom code before restarting
                if (runBeforeRestart != null) {
                    runBeforeRestart.run();
                }
                // exit
                System.exit(0);
            } catch (UnsupportedEncodingException ex) {
                Main.logger.error("Error while trying to decode some special characters", ex);
            }
        }
    }
}
