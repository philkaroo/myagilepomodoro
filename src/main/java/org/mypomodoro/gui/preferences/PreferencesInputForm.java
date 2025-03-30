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
package org.mypomodoro.gui.preferences;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.mypomodoro.Main;
import org.mypomodoro.gui.ItemLocale;
import org.mypomodoro.gui.activities.AbstractComboBoxRenderer;
import org.mypomodoro.gui.create.FormLabel;
import org.mypomodoro.gui.preferences.plaf.MAPLookAndFeel;
import org.mypomodoro.util.Labels;

/**
 * Preferences input form
 *
 */
public class PreferencesInputForm extends JPanel {

    private static final Dimension PANEL_DIMENSION = new Dimension(400, 200);
    protected final TimerValueSlider pomodoroSlider;
    protected final TimerValueSlider shortBreakSlider;
    protected final TimerValueSlider longBreakSlider;
    protected final TimerValueSlider maxNbPomPerDaySlider;
    protected final TimerValueSlider nbPomPerSetSlider;
    protected final TimerValueSlider maxNbPomPerActivitySlider;
    protected final JCheckBox tickingBox;
    protected final JCheckBox ringingBox;
    protected final JComboBox localesComboBox;
    protected final JCheckBox systemTrayBox;
    protected final JCheckBox systemTrayMessageBox;
    protected final JCheckBox alwaysOnTopBox;
    protected final JCheckBox bringToFrontBox;
    protected final JCheckBox agileModeBox;
    protected final JCheckBox pomodoroModeBox;
    protected final JCheckBox plainHoursBox;
    protected final JCheckBox effectiveHoursBox;
    protected final JComboBox themesComboBox;
    public static final String NIMROD_LAF = "com.nilo.plaf.nimrod.NimRODLookAndFeel"; // NimROD
    public static final String PLASTIC3D_LAF = "com.jgoodies.looks.plastic.Plastic3DLookAndFeel"; // JGoodies  
    public static final String PGS_LAF = "com.pagosoft.plaf.PgsLookAndFeel"; // Pgs
    // Removing SeaGlass theme 0.2.1 as it fixes an issue on java8_u60 but it not backward compatible with java 6 -->
    //public static final String SEAGLASS_LAF = "com.seaglasslookandfeel.SeaGlassLookAndFeel"; // Seaglass
    private static final String NOIRE_LAF = "com.jtattoo.plaf.noire.NoireLookAndFeel"; // JTattoo
    public static final String ACRYL_LAF = "com.jtattoo.plaf.acryl.AcrylLookAndFeel"; // JTattoo
    public static final String MCWIN_LAF = "com.jtattoo.plaf.mcwin.McWinLookAndFeel"; // JTattoo
    public static final String INFONODE_LAF = "net.infonode.gui.laf.InfoNodeLookAndFeel"; // InfoNode
    //private static final String WEB_LAF = "com.alee.laf.WebLookAndFeel"; // WebLaF    

    public PreferencesInputForm(final PreferencesPanel controlPanel) {
        TitledBorder titledborder = new TitledBorder(new EtchedBorder(), " " + Labels.getString("PreferencesPanel.Preferences") + " ");
        titledborder.setTitleFont(new JTextField().getFont().deriveFont(Font.BOLD)); // Ticket #70 : java 7: use of JTextField to retrieve the default font (titledborder.getTitleFont() fails)
        titledborder.setTitleColor(getForeground()); // normally black; depends on the theme
        setBorder(titledborder);

        setMinimumSize(PANEL_DIMENSION);
        setPreferredSize(PANEL_DIMENSION);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        pomodoroSlider = new TimerValueSlider(controlPanel, 10, 45,
                Main.preferences.getPomodoroLength(),
                25, 30,
                Labels.getString("PreferencesPanel.minutes"));
        pomodoroSlider.getSlider().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                pomodoroSlider.setText();
                updatePomodoroSlidersText();
            }
        });
        shortBreakSlider = new TimerValueSlider(controlPanel, 1, 15,
                Main.preferences.getShortBreakLength(),
                3, 5,
                Labels.getString("PreferencesPanel.minutes"));
        shortBreakSlider.getSlider().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                shortBreakSlider.setText();
                updatePomodoroSlidersText();
            }
        });
        longBreakSlider = new TimerValueSlider(controlPanel, 5, 45,
                Main.preferences.getLongBreakLength(),
                15, 30,
                Labels.getString("PreferencesPanel.minutes"));
        longBreakSlider.getSlider().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                longBreakSlider.setText();
                updatePomodoroSlidersText();
            }
        });
        final int maxNbPomPerActivity = 7;
        final int initMaxNbPomPerActivity = 5;
        final int maxNbPomPerActivityAgileMode = 24; // In the Agile world, a task may last up to 2 days (2 times the max nb of pom per day)
        final int initMaxNbPomPerActivityAgileMode = 20;
        final int maxNbPomPerDay = 12;
        final int initMaxNbPomPerDay = 10;
        maxNbPomPerDaySlider = new TimerValueSlider(controlPanel, 1, maxNbPomPerDay,
                Main.preferences.getMaxNbPomPerDay(),
                1, initMaxNbPomPerDay,
                Labels.getString("PreferencesPanel.pomodoros"),
                true, true);
        maxNbPomPerDaySlider.getSlider().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                updatePomodoroSlidersText();
            }
        });
        nbPomPerSetSlider = new TimerValueSlider(controlPanel, 2, 5,
                Main.preferences.getNbPomPerSet(),
                3, 4,
                Labels.getString("PreferencesPanel.pomodoros"),
                true, false);
        nbPomPerSetSlider.getSlider().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                updateNbPomPerSetSliderText();
            }
        });
        maxNbPomPerActivitySlider = new TimerValueSlider(controlPanel, 1, maxNbPomPerActivityAgileMode,
                Main.preferences.getMaxNbPomPerActivity(),
                1, initMaxNbPomPerActivityAgileMode,
                Labels.getString("PreferencesPanel.pomodoros"),
                true, false);
        maxNbPomPerActivitySlider.getSlider().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                updateMaxNbPomPerActivitySliderText();
            }
        });
        tickingBox = new JCheckBox(
                Labels.getString("PreferencesPanel.ticking"),
                Main.preferences.getTicking());
        ringingBox = new JCheckBox(
                Labels.getString("PreferencesPanel.ringing"),
                Main.preferences.getRinging());
        tickingBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                controlPanel.enableSaveButton();
                controlPanel.clearValidation();
            }
        });
        ringingBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                controlPanel.enableSaveButton();
                controlPanel.clearValidation();
            }
        });
        List<ItemLocale> locales = ItemLocale.getLocalesFromPropertiesTitlefiles();
        localesComboBox = new JComboBox(locales.toArray());
        for (int i = 0; i < locales.size(); i++) {
            if (locales.get(i).getLocale().equals(
                    Main.preferences.getLocale())) {
                localesComboBox.setSelectedIndex(i);
            }
        }
        // Setting the background color is required here for the Cross Platform Look And Feel (see Main)
        localesComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                controlPanel.enableSaveButton();
                controlPanel.clearValidation();
            }
        });
        systemTrayBox = new JCheckBox(
                Labels.getString("PreferencesPanel.System Tray"),
                Main.preferences.getSystemTray());
        systemTrayMessageBox = new JCheckBox(
                Labels.getString("PreferencesPanel.Popup Message"),
                Main.preferences.getSystemTrayMessage());
        systemTrayBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                controlPanel.enableSaveButton();
                controlPanel.clearValidation();
                if (!systemTrayBox.isSelected()) {
                    systemTrayMessageBox.setSelected(false);
                }
            }
        });
        systemTrayMessageBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                controlPanel.enableSaveButton();
                controlPanel.clearValidation();
                if (systemTrayMessageBox.isSelected()) {
                    systemTrayBox.setSelected(true);
                }
            }
        });
        alwaysOnTopBox = new JCheckBox(
                Labels.getString("PreferencesPanel.Always On Top"),
                Main.preferences.getAlwaysOnTop());
        alwaysOnTopBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                controlPanel.enableSaveButton();
                controlPanel.clearValidation();
            }
        });
        bringToFrontBox = new JCheckBox(
                Labels.getString("PreferencesPanel.Bring To Front"),
                Main.preferences.getBringToFront());
        bringToFrontBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                controlPanel.enableSaveButton();
                controlPanel.clearValidation();
            }
        });
        agileModeBox = new JCheckBox(
                Labels.getString("PreferencesPanel.Agile.Agile Mode"),
                Main.preferences.getAgileMode());
        agileModeBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                controlPanel.enableSaveButton();
                controlPanel.clearValidation();
                // In the Agile world, a task may last up to 2 days (2 times the max nb of pom per day)
                maxNbPomPerActivitySlider.changeSlider(maxNbPomPerActivityAgileMode);
                maxNbPomPerActivitySlider.setSliderValue(maxNbPomPerDaySlider.getSliderValue() * 2);
                agileModeBox.setSelected(true);
                pomodoroModeBox.setSelected(false);
            }
        });
        pomodoroModeBox = new JCheckBox(
                Labels.getString("PreferencesPanel.Agile.Pomodoro Mode"),
                !Main.preferences.getAgileMode());
        pomodoroModeBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                controlPanel.enableSaveButton();
                controlPanel.clearValidation();
                maxNbPomPerActivitySlider.changeSlider(maxNbPomPerActivity);
                maxNbPomPerActivitySlider.setSliderValue(maxNbPomPerDaySlider.getSliderValue() < initMaxNbPomPerActivity ? maxNbPomPerDaySlider.getSliderValue() : initMaxNbPomPerActivity);
                maxNbPomPerDaySlider.setVisible(true);
                pomodoroModeBox.setSelected(true);
                agileModeBox.setSelected(false);
            }
        });
        plainHoursBox = new JCheckBox(
                Labels.getString("PreferencesPanel.Plain hours"),
                Main.preferences.getPlainHours());
        plainHoursBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                controlPanel.enableSaveButton();
                controlPanel.clearValidation();
                plainHoursBox.setSelected(true);
                effectiveHoursBox.setSelected(false);
                updatePomodoroSlidersText();

            }
        });
        effectiveHoursBox = new JCheckBox(
                Labels.getString("PreferencesPanel.Effective hours"),
                !Main.preferences.getPlainHours());
        effectiveHoursBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                controlPanel.enableSaveButton();
                controlPanel.clearValidation();
                effectiveHoursBox.setSelected(true);
                plainHoursBox.setSelected(false);
                updatePomodoroSlidersText();
            }
        });
        // Themes
        // http://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
        // About custom laf: http://wiki.netbeans.org/FaqCustomLaf                
        // Napkin: "net.sourceforge.napkinlaf.NapkinLookAndFeel" (enable dependency in pom.xml)        
        // NimRod: "com.nilo.plaf.nimrod.NimRODLookAndFeel" (enable dependency in pom.xml)
        // Kunststoff: Unew KunststoffLookAndFeel() (enable dependency in pom.xml)
        // JGoodies: "com.jgoodies.looks.plastic.Plastic3DLookAndFeel" (enable dependency in pom.xml)
        // TinyLaf: "net.sf.tinylaf.TinyLookAndFeel" (enable dependency in pom.xml)
        // Metal: "javax.swing.plaf.metal.MetalLookAndFeel" (same as cross platform)
        // GT: "com.sun.java.swing.plaf.gtk.GTKLookAndFeel" (same as System - Linux only)
        // Motif: "com.sun.java.swing.plaf.motif.MotifLookAndFeel" (same as System - Solaris only)
        // Nimbus: "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"
        // InfoNode: net.infonode.gui.laf.InfoNodeLookAndFeel (enable dependency in pom.xml)                        
        // Substance: new SubstanceCremeLookAndFeel() (enable dependency in pom.xml)                        
        // Tonic: "com.digitprop.tonic.TonicLookAndFeel" (enable dependency in pom.xml)
        // WebLaf: "com.alee.laf.WebLookAndFeel" (enable dependency in pom.xml)
        // Pgs: "com.pagosoft.plaf.PgsLookAndFeel" (enable dependency in pom.xml)
        // Seaglass: "com.seaglasslookandfeel.SeaGlassLookAndFeel" (enable dependency in pom.xml)
        // JTattoo: "com.jtattoo.plaf.smart.SmartLookAndFeel" (enable dependency in pom.xml)
        // JTattoo: "com.jtattoo.plaf.noire.NoireLookAndFeel" (http://www.jtattoo.net/PredefinedThemes.html)
        // JTattoo: "com.jtattoo.plaf.mcwin.McWinLookAndFeel"
        // JTattoo: com.jtattoo.plaf.acryl.AcrylLookAndFeel
        // JTattoo: "com.jtattoo.plaf.aero.AeroLookAndFeel"
        // JTattoo: "com.jtattoo.plaf.aluminium.AluminiumLookAndFeel"
        // JTattoo: "com.jtattoo.plaf.bernstein.BernsteinLookAndFeel"
        // JTattoo: "com.jtattoo.plaf.fast.FastLookAndFeel"
        // JTattoo: "com.jtattoo.plaf.graphite.GraphiteLookAndFeel"
        // JTattoo: "com.jtattoo.plaf.hifi.HiFiLookAndFeel"
        // JTattoo: "com.jtattoo.plaf.luna.LunaLookAndFeel"
        // JTattoo: "com.jtattoo.plaf.mint.MintLookAndFeel"
        ArrayList<String> themes = new ArrayList<String>();
        themes.add(MAPLookAndFeel.class.getPackage().getName() + ".MAPLookAndFeel"); // mAP laf red      
        if (!UIManager.getSystemLookAndFeelClassName().equals(UIManager.getCrossPlatformLookAndFeelClassName())) {
            themes.add(UIManager.getSystemLookAndFeelClassName()); // Windows / GTK / Motif
        }
        themes.add(UIManager.getCrossPlatformLookAndFeelClassName()); // Metal
        /*for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
         System.err.println("theme = " + info.getClassName());
         }*/
        String nimbusTheme = getNimbusTheme();
        if (nimbusTheme != null) {
            themes.add(nimbusTheme); // Nimbus
        }
        themes.add(NIMROD_LAF); // NimROD
        themes.add(PLASTIC3D_LAF); // JGoodies  
        themes.add(PGS_LAF); // Pgs
        //themes.add(SEAGLASS_LAF); // Seaglass
        themes.add(NOIRE_LAF); // JTattoo
        themes.add(ACRYL_LAF); // JTattoo
        themes.add(MCWIN_LAF); // JTattoo
        themes.add(INFONODE_LAF); // InfoNode
        //themes.add(WEB_LAF); // WebLaF        
        // Quaqua
        // Due to copyright restrictions and technical constraints, Quaqua can be run on non-Mac OS X systems for development purposes only.
        //if (SystemUtils.IS_OS_MAC_OSX) {
        // themes.add("ch.randelshofer.quaqua.QuaquaLookAndFeel");
        //}           
        themes.add(MAPLookAndFeel.class.getPackage().getName() + ".MAPLookAndFeelAssurance"); // mAP laf green
        themes.add(MAPLookAndFeel.class.getPackage().getName() + ".MAPLookAndFeelCreativity"); // mAP laf purple
        themes.add(MAPLookAndFeel.class.getPackage().getName() + ".MAPLookAndFeelConfidence"); // mAP laf yellow 
        themes.add(MAPLookAndFeel.class.getPackage().getName() + ".MAPLookAndFeelIntelligence"); // mAP laf blue 
        themes.add(MAPLookAndFeel.class.getPackage().getName() + ".MAPLookAndFeelOptimism"); // mAP laf orange 
        themesComboBox = new JComboBox(themes.toArray());
        for (int i = 0; i < themes.size(); i++) {
            if (themes.get(i).equalsIgnoreCase(Main.preferences.getTheme())) {
                themesComboBox.setSelectedIndex(i);
            }
        }
        // Setting the background color is required here for the Cross Platform Look And Feel (see Main)
        themesComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // The following block has been commented out because it doesn't work well, and doesn't work at all with Metal (cross platform) laf
                // The problem here is that some Look and Feel classes are subclasses of MetalLookAndFeel. 
                // The color theme is a static field in the MetalLookAndFeel, which means that all instances of KunststoffLookAndFeel and MetalLookAndFeel are using one and the same color theme. 
                // JDK 7: To get the default metal color theme when switching to the Metal Look&Feel you will have to insert a line like this:
                // myMetalLookAndFeel.setCurrentTheme(new javax.swing.plaf.metal.DefaultMetalTheme())
                /*try {
                 // Show look and feel at run time
                 if (((String) themesComboBox.getSelectedItem()).contains("MAPLookAndFeel")) {
                 UIManager.setLookAndFeel((String) themesComboBox.getSelectedItem());
                 SwingUtilities.updateComponentTreeUI(Main.gui);
                 Main.gui.pack();
                 }*/
                controlPanel.enableSaveButton();
                controlPanel.clearValidation();
                /*} catch (ClassNotFoundException ignored) {
                 } catch (InstantiationException ignored) {
                 } catch (IllegalAccessException ignored) {
                 } catch (UnsupportedLookAndFeelException ignored) {
                 }*/
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 0;
        addAgileMode(gbc);
        gbc.gridy = 1;
        addSliders(gbc);
        gbc.gridy = 2;
        addPlainHours(gbc);
        gbc.gridy = 3;
        addLocales(gbc);
        gbc.gridy = 4;
        addSounds(gbc);
        gbc.gridy = 5;
        if (SystemTray.isSupported()) {
            addSystemTray(gbc);
        }
        gbc.gridy = 6;
        addAlwaysOnTop(gbc);
        gbc.gridy = 7;
        addThemes(gbc);
    }

    private void addAgileMode(GridBagConstraints gbc) {
        JPanel agileMode = new JPanel();
        agileMode.setLayout(new GridBagLayout());
        GridBagConstraints gbcAgileMode = new GridBagConstraints();
        gbcAgileMode.fill = GridBagConstraints.HORIZONTAL;
        gbcAgileMode.anchor = GridBagConstraints.NORTH;
        gbcAgileMode.gridx = 0;
        gbcAgileMode.gridy = 0;
        agileMode.add(agileModeBox, gbcAgileMode);
        gbcAgileMode.gridx = 1;
        gbcAgileMode.gridy = 0;
        agileMode.add(pomodoroModeBox, gbcAgileMode);
        add(agileMode, gbc);
    }

    private void addSliders(GridBagConstraints gbc) {
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;
        final FormLabel pomodoroSliderLabel = new FormLabel(
                Labels.getString("PreferencesPanel.Pomodoro Length") + ": ");
        c.gridx = 0;
        c.gridy = 0;
        sliderPanel.add(pomodoroSliderLabel, c);
        c.gridx = 1;
        c.gridy = 0;
        sliderPanel.add(pomodoroSlider, c);

        final FormLabel shortBreakSliderLabel = new FormLabel(
                Labels.getString("PreferencesPanel.Short Break Length") + ": ");
        c.gridx = 0;
        c.gridy = 1;
        sliderPanel.add(shortBreakSliderLabel, c);
        c.gridx = 1;
        c.gridy = 1;
        sliderPanel.add(shortBreakSlider, c);

        final FormLabel longBreakSliderLabel = new FormLabel(
                Labels.getString("PreferencesPanel.Long Break Length") + ": ");
        c.gridx = 0;
        c.gridy = 2;
        sliderPanel.add(longBreakSliderLabel, c);
        c.gridx = 1;
        c.gridy = 2;
        sliderPanel.add(longBreakSlider, c);

        final FormLabel maxNbPomPerDaySliderLabel = new FormLabel(
                Labels.getString("PreferencesPanel.Max nb pom/day") + ": ");
        c.gridx = 0;
        c.gridy = 3;
        sliderPanel.add(maxNbPomPerDaySliderLabel, c);
        c.gridx = 1;
        c.gridy = 3;
        sliderPanel.add(maxNbPomPerDaySlider, c);

        final FormLabel nbPomPerSetSliderLabel = new FormLabel(
                Labels.getString("PreferencesPanel.Nb pom/set") + ": ");
        c.gridx = 0;
        c.gridy = 4;
        sliderPanel.add(nbPomPerSetSliderLabel, c);
        c.gridx = 1;
        c.gridy = 4;
        sliderPanel.add(nbPomPerSetSlider, c);

        final FormLabel maxNbPomPerActivitySliderLabel = new FormLabel(
                Labels.getString("PreferencesPanel.Max nb pom/activity") + ": ");
        c.gridx = 0;
        c.gridy = 5;
        sliderPanel.add(maxNbPomPerActivitySliderLabel, c);
        c.gridx = 1;
        c.gridy = 5;
        sliderPanel.add(maxNbPomPerActivitySlider, c);

        add(sliderPanel, gbc);
    }

    private void addSounds(GridBagConstraints gbc) {
        JPanel sounds = new JPanel();
        sounds.setLayout(new GridBagLayout());
        GridBagConstraints gbcSounds = new GridBagConstraints();
        gbcSounds.fill = GridBagConstraints.HORIZONTAL;
        gbcSounds.anchor = GridBagConstraints.NORTH;
        gbcSounds.gridx = 0;
        gbcSounds.gridy = 0;
        sounds.add(tickingBox, gbcSounds);
        gbcSounds.gridx = 1;
        gbcSounds.gridy = 0;
        sounds.add(ringingBox, gbcSounds);
        add(sounds, gbc);
    }

    private void addLocales(GridBagConstraints gbc) {
        JPanel locales = new JPanel();
        locales.setLayout(new GridBagLayout());
        GridBagConstraints gbcLocales = new GridBagConstraints();
        gbcLocales.fill = GridBagConstraints.HORIZONTAL;
        gbcLocales.anchor = GridBagConstraints.NORTH;
        gbcLocales.gridx = 0;
        gbcLocales.gridy = 0;
        localesComboBox.setRenderer(new AbstractComboBoxRenderer());
        locales.add(localesComboBox, gbcLocales);
        add(locales, gbc);
    }

    private void addSystemTray(GridBagConstraints gbc) {
        JPanel systemTray = new JPanel();
        systemTray.setLayout(new GridBagLayout());
        GridBagConstraints gbcSystemTray = new GridBagConstraints();
        gbcSystemTray.fill = GridBagConstraints.HORIZONTAL;
        gbcSystemTray.anchor = GridBagConstraints.NORTH;
        gbcSystemTray.gridx = 0;
        gbcSystemTray.gridy = 0;
        systemTray.add(systemTrayBox, gbcSystemTray);
        gbcSystemTray.gridx = 1;
        gbcSystemTray.gridy = 0;
        systemTray.add(systemTrayMessageBox, gbcSystemTray);
        add(systemTray, gbc);
    }

    private void addPlainHours(GridBagConstraints gbc) {
        JPanel plainHours = new JPanel();
        plainHours.setLayout(new GridBagLayout());
        GridBagConstraints gbcSystemTray = new GridBagConstraints();
        gbcSystemTray.fill = GridBagConstraints.HORIZONTAL;
        gbcSystemTray.anchor = GridBagConstraints.NORTH;
        gbcSystemTray.gridx = 0;
        gbcSystemTray.gridy = 0;
        plainHours.add(plainHoursBox, gbcSystemTray);
        gbcSystemTray.gridx = 1;
        gbcSystemTray.gridy = 0;
        plainHours.add(effectiveHoursBox, gbcSystemTray);
        add(plainHours, gbc);
    }

    private void addAlwaysOnTop(GridBagConstraints gbc) {
        JPanel alwaysOnTop = new JPanel();
        alwaysOnTop.setLayout(new GridBagLayout());
        GridBagConstraints gbcAlwaysOnTop = new GridBagConstraints();
        gbcAlwaysOnTop.fill = GridBagConstraints.HORIZONTAL;
        gbcAlwaysOnTop.anchor = GridBagConstraints.NORTH;
        gbcAlwaysOnTop.gridx = 0;
        gbcAlwaysOnTop.gridy = 0;
        alwaysOnTop.add(alwaysOnTopBox, gbcAlwaysOnTop);
        gbcAlwaysOnTop.gridx = 1;
        gbcAlwaysOnTop.gridy = 0;
        alwaysOnTop.add(bringToFrontBox, gbcAlwaysOnTop);
        add(alwaysOnTop, gbc);
    }

    private void addThemes(GridBagConstraints gbc) {
        JPanel themes = new JPanel();
        themes.setLayout(new GridBagLayout());
        GridBagConstraints gbcThemes = new GridBagConstraints();
        gbcThemes.fill = GridBagConstraints.HORIZONTAL;
        gbcThemes.anchor = GridBagConstraints.NORTH;
        gbcThemes.gridx = 0;
        gbcThemes.gridy = 0;
        themesComboBox.setRenderer(new ThemeComboBoxRenderer());
        themes.add(themesComboBox, gbcThemes);
        add(themes, gbc);
    }

    // Display the class name of the look and feel instead of the whole class path
    class ThemeComboBoxRenderer extends AbstractComboBoxRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String text = value.toString();
            setText(text.substring(text.lastIndexOf(".") + 1, text.length()));
            return this;
        }
    }

    private void updatePomodoroSlidersText() {
        maxNbPomPerDaySlider.setText(pomodoroSlider.getSliderValue(), shortBreakSlider.getSliderValue(), longBreakSlider.getSliderValue(), nbPomPerSetSlider.getSliderValue(), plainHoursBox.isSelected(), maxNbPomPerDaySlider.getSliderValue());
        updateNbPomPerSetSliderText();
        updateMaxNbPomPerActivitySliderText();
    }

    private void updateNbPomPerSetSliderText() {
        nbPomPerSetSlider.setText(pomodoroSlider.getSliderValue(), shortBreakSlider.getSliderValue(), longBreakSlider.getSliderValue(), nbPomPerSetSlider.getSliderValue(), plainHoursBox.isSelected(), maxNbPomPerDaySlider.getSliderValue());
    }

    private void updateMaxNbPomPerActivitySliderText() {
        maxNbPomPerActivitySlider.setText(pomodoroSlider.getSliderValue(), shortBreakSlider.getSliderValue(), longBreakSlider.getSliderValue(), nbPomPerSetSlider.getSliderValue(), plainHoursBox.isSelected(), maxNbPomPerDaySlider.getSliderValue());
    }

    static public String getNimbusTheme() {
        String nimbusTheme = null;
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {

                if ("Nimbus".equals(info.getName())) {
                    nimbusTheme = info.getClassName();
                    break;
                }
            }
        } catch (Exception ignored) {
            // Nimbus not available
        }
        return nimbusTheme;
    }
}
