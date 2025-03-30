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
package org.mypomodoro;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import org.mypomodoro.db.Database;
import org.mypomodoro.db.mysql.MySQLConfigLoader;
import org.mypomodoro.gui.MainPanel;
import org.mypomodoro.gui.create.list.AuthorList;
import org.mypomodoro.gui.create.list.PlaceList;
import org.mypomodoro.gui.create.list.TaskTypeList;
import org.mypomodoro.gui.export.google.GoogleConfigLoader;
import org.mypomodoro.model.ActivityList;
import org.mypomodoro.model.ChartList;
import org.mypomodoro.model.Preferences;
import org.mypomodoro.model.ReportList;
import org.mypomodoro.model.ToDoList;
import org.mypomodoro.util.ColorUtil;
import org.mypomodoro.util.ExecutablePath;

/**
 * Main Application Starter
 *
 */
public class Main {

    public static org.slf4j.Logger logger;

    // Database
    public static MySQLConfigLoader mySQLconfig;
    public static Database database;
    // Google drive
    public static GoogleConfigLoader googleConfig;
    // Preferences
    public static Preferences preferences;
    // GUI
    public static MainPanel gui;
    private static Font font;
    // Table colors
    public static Color tableBackgroundColor = ColorUtil.WHITE;
    public static Color selectedRowColor = ColorUtil.BLUE_ROW;
    public static Color oddRowColor = ColorUtil.WHITE;
    public static Color evenRowColor = ColorUtil.BLUE_ROW_LIGHT;
    public static Color hoverRowColor = ColorUtil.YELLOW_ROW;
    public static Color rowBorderColor = ColorUtil.BLUE_ROW_DARKER;
    public static Color taskFinishedColor = ColorUtil.GREEN;
    public static Color taskRunningColor = ColorUtil.RED;
    public static Color myIconBackgroundColor; // eg mAP laf uses this setting
    public static String iconsSetPath = "/images/icons_dark_set/";
    public static String mAPIconTimer = "mAPIconTimer.png";
    public static String mAPIconTinyTimer = "mAPIconTinyTimer.png";
    public static String configPath = "";

    /**
     * Main
     *
     * Command line: java -jar <full path of executable> [<full path of database
     * and config files>]
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // full path of the executable with file separator at the end
            configPath = ExecutablePath.getExecutableDirectoryWithSeparator();
            // Compute arguments
            // first argument: full path where to create (first launch) or use the database, the config files and the log file (may be different form the executable path)
            if (args.length > 0) {
                // full path of the database and config files with file separator at the end
                configPath = args[0] + File.separator;
                configPath = ExecutablePath.URLDecode(configPath);
            }
            System.setProperty("full_path_log", configPath); // this is for log back (see logback.xml config file)
            // invoking the logger to set the path
            logger = org.slf4j.LoggerFactory.getLogger(Main.class);
        } catch (UnsupportedEncodingException ex) {
            System.setProperty("full_path_log", configPath); // this is for log back (see logback.xml config file)
            // invoking the logger to set the path
            logger = org.slf4j.LoggerFactory.getLogger(Main.class);
            logger.error("Error while trying to decode some special characters", ex);
        }

        // Load database and config files
        mySQLconfig = new MySQLConfigLoader(); // load properties
        database = new Database(); // create database if necessary
        googleConfig = new GoogleConfigLoader(); // load properties

        // Load preferences
        preferences = new Preferences(); // load preferences
        preferences.loadPreferences();

        // Set font from font file
        GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = g.getAvailableFontFamilyNames();
        for (String fontName : fonts) {
            if (fontName.equals("Arial Unicode MS")) {
                // Arial MS Unicode font
                // Microsoft license: http://www.microsoft.com/typography/fonts/font.aspx?FMID=1081
                // For testing: chinese (政府派高层), japanese (施する外), arabic (راديو مباشر), hebrew(מוטיבציה לר), hindi (भूत-प्रेत की कहानियाँ)
                // russian (Поездка), greek (δημοφιλέστερα), thai (ทำเนียบรัฐบาล), viet (yêu thích nhấ), korean (한국관광공사;)
                // Bundled with Windows and Mac OS
                font = new Font("Arial Unicode MS", Font.PLAIN, 15);
                break;
            }
        }
        // MAC
        // Commented out: won't work on MAC_OSX
        /*if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
         // deletes files created with RestartMac()
         new RestartMac(1);
         return;
         }*/
        // This must be done before setting the look and feel
        // See http://alvinalexander.com/apple/mac/java-mac-native-look/Putting_your_application_na.shtml
        /*if (SystemUtils.IS_OS_MAC_OSX) {
         // take the menu bar off the jframe
         System.setProperty("apple.laf.useScreenMenuBar", "true");
         // set the name of the application menu item
         System.setProperty("com.apple.mrj.application.apple.menu.about.name", "myAgilePomodoro");
         }*/
        // Load combo boxes data (type, author...) before main panel is instanciated in new thread
        updateComboBoxLists();
        // Display GUI
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // Look & Feel (laf)
                // Theme set in Preferences
                try {
                    String theme = preferences.getTheme();
                    UIManager.setLookAndFeel(theme);
                } catch (Exception ex) {
                    // OpenJdk: if the console says "Picked up JAVA_TOOL_OPTIONS...", you want to unset that option.
                    logger.error("Using the System Look and Feel library to fix the following issue...", ex);
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception ex1) {
                        // Note: Some version OpenJDK 7 & 8 have an issue regarding method getSystemLookAndFeelClassName
                        // See https://bugs.openjdk.java.net/browse/JDK-8074303
                        logger.error("This error is a known bug in some versions of OpenJDK 7 and 8...", ex1);
                    }
                }
                // Set global font (before intanstiating the components and the gui)
                // This must be done AFTER the setLookAndFeel for the font to be also set on OptionPane dialog... (don't ask)
                if (font == null) { // In case , Arial Unicode MS isn't installed; let's hope the default OS font is unicode
                    font = new JPanel().getFont().deriveFont(Font.PLAIN, 15f);
                    logger.warn("Arial Unicode MS not supported. Replaced with default System font.");
                }
                setUIFont(new FontUIResource(font.getName(), font.getStyle(), font.getSize()));
                // Set progress bar font (before intanstiating the progress bar)
                UIManager.put("ProgressBar.background", hoverRowColor); // colour of the background // this does not work
                UIManager.put("ProgressBar.foreground", selectedRowColor); // colour of progress bar
                UIManager.put("ProgressBar.selectionBackground", ColorUtil.BLACK); // colour of percentage counter on background
                UIManager.put("ProgressBar.selectionForeground", ColorUtil.BLACK); // colour of precentage counter on progress bar
                // init the gui, and all its components, AFTER setting the UIManager and font
                gui = new MainPanel();
                /*
                 * Old fashion way to center the component onscreen
                 * Dimension screenSize
                 * = gui.getToolkit().getScreenSize(); int w = (int) ( (
                 * screenSize.getWidth() - gui.getSize().width ) / 2 ); int h = (int) (
                 * ( screenSize.getHeight() - gui.getSize().height ) / 2 );
                 * gui.setLocation(w, h);
                 */
                gui.pack();
                gui.setLocationRelativeTo(null); // center the component onscreen
                gui.setVisible(true);
                if (preferences.getAlwaysOnTop()) {
                    gui.setAlwaysOnTop(true);
                }
                Dimension dGUI = new Dimension(Math.max(800, gui.getWidth()), Math.max(600, gui.getHeight()));
                Dimension mindGUI = new Dimension(800, 600);
                gui.setPreferredSize(mindGUI);
                gui.setSize(dGUI);
                /* this prevents the gui from being resizable
                 gui.addComponentListener(new ComponentAdapter() {
                 @Override
                 public void componentResized(ComponentEvent event) {
                 Dimension dGUI = new Dimension(Math.max(800, gui.getWidth()),
                 Math.max(600, gui.getHeight()));
                 Dimension mindGUI = new Dimension(800, 600);
                 //gui.setPreferredSize(mindGUI);
                 gui.setSize(dGUI);
                 }
                 });*/
            }
        });
    }

    // Set default global font for the application
    public static void setUIFont(FontUIResource f) {
        Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value != null && value instanceof FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    public static void updateLists() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                ActivityList.getList().refresh();
                ToDoList.getList().refresh();
                ReportList.getList().refresh();
                ChartList.getList().refresh();
            }
        });
    }

    public static void updateComboBoxLists() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                TaskTypeList.refresh();
                //SubTaskTypeList.refresh();
                AuthorList.refresh();
                PlaceList.refresh();
            }
        });
    }
}
