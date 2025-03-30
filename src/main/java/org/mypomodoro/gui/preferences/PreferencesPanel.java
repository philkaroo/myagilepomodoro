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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.apache.commons.lang3.SystemUtils;
import org.mypomodoro.Main;
import static org.mypomodoro.Main.preferences;
import org.mypomodoro.buttons.DefaultButton;
import org.mypomodoro.buttons.RestartButton;
import org.mypomodoro.gui.ItemLocale;
import org.mypomodoro.model.Preferences;
import org.mypomodoro.util.DateUtil;
import org.mypomodoro.util.Labels;

public class PreferencesPanel extends JPanel {

    public static Labels labels;
    public static DateUtil dateUtil;
    public DefaultButton saveButton;
    public DefaultButton resetButton;
    protected JLabel validation = new JLabel();
    protected JPanel validPanel = new JPanel();
    public DefaultButton restartButton;
    protected GridBagConstraints gbc = new GridBagConstraints();
    protected final PreferencesInputForm preferencesInputFormPanel;

    public PreferencesPanel() {
        Locale locale = new Locale(Main.preferences.getLocale().getLanguage(),
                Main.preferences.getLocale().getCountry(), Main.preferences.getLocale().getVariant());
        labels = new Labels(locale);
        dateUtil = new DateUtil(locale);
        saveButton = new DefaultButton(Labels.getString("Common.Save"));
        preferencesInputFormPanel = new PreferencesInputForm(this);
        resetButton = new DefaultButton(
                Labels.getString("Common.Reset"));
        restartButton = new RestartButton();
        restartButton.setVisible(false);

        setLayout(new GridBagLayout());

        addPreferencesInputFormPanel();
        addSaveButton();
        addResetButton();
        addValidation();
    }

    protected void addPreferencesInputFormPanel() {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.80;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        add(preferencesInputFormPanel, gbc);
    }

    protected void addSaveButton() {
        // Save action
        Action save = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setValidation(Labels.getString("PreferencesPanel.Preferences saved.") + " ");
                updatePreferences();
                disableSaveButton();
                validPanel.setVisible(true);
            }
        };
        // Listener for mouse action
        saveButton.addActionListener(save);
        // Keystroke for keyboard action
        // These two lines are required to enable Enter evrywhere in the form (including text fields and textarea) once the save button is enabled
        saveButton.registerKeyboardAction(save,
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        saveButton.registerKeyboardAction(save,
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        saveButton.setToolTipText(Labels.getString("Common.Save") + " (ENTER)");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.1;
        gbc.gridwidth = 1;
        // gbc.fill = GridBagConstraints.NONE;
        disableSaveButton();
        add(saveButton, gbc);
    }

    protected void addResetButton() { // values recommended by the Pomodoro
        // Technique
        resetButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                preferencesInputFormPanel.pomodoroSlider.setSliderValue(Preferences.PLENGTH);
                preferencesInputFormPanel.shortBreakSlider.setSliderValue(Preferences.SBLENGTH);
                preferencesInputFormPanel.longBreakSlider.setSliderValue(Preferences.LBLENGTH);
                preferencesInputFormPanel.maxNbPomPerActivitySlider.changeSlider(Preferences.MNPPACTIVITY);
                preferencesInputFormPanel.maxNbPomPerActivitySlider.setSliderValue(Preferences.INITMNPPACTIVITY);
                preferencesInputFormPanel.maxNbPomPerDaySlider.setSliderValue(Preferences.MNPPDAY);
                preferencesInputFormPanel.nbPomPerSetSlider.setSliderValue(Preferences.NPPSet);
                preferencesInputFormPanel.tickingBox.setSelected(true);
                preferencesInputFormPanel.ringingBox.setSelected(true);
                // no reset for locale
                preferencesInputFormPanel.systemTrayBox.setSelected(true);
                preferencesInputFormPanel.systemTrayMessageBox.setSelected(true);
                preferencesInputFormPanel.alwaysOnTopBox.setSelected(false);
                preferencesInputFormPanel.bringToFrontBox.setSelected(false);
                preferencesInputFormPanel.agileModeBox.setSelected(true);
                preferencesInputFormPanel.pomodoroModeBox.setSelected(false);
                preferencesInputFormPanel.plainHoursBox.setSelected(true);
                preferencesInputFormPanel.effectiveHoursBox.setSelected(false);
                // no reset for themes
                setValidation(Labels.getString("PreferencesPanel.Preferences reset.") + " ");
                updatePreferences();
                disableSaveButton();
                validPanel.setVisible(true);
            }
        });
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.1;
        gbc.gridwidth = 1;
        // gbc.fill = GridBagConstraints.NONE;
        add(resetButton, gbc);
    }

    protected void addValidation() {
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        validation.setFont(getFont().deriveFont(Font.BOLD));
        //GridBagConstraints vgbc = new GridBagConstraints();
        //vgbc.gridx = 0;
        //vgbc.gridy = 0;
        validPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        validPanel.add(validation);
        //vgbc.gridx = 1;
        //vgbc.gridy = 0;
        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) { // no restart button for Mac OS (does not work - see RestartMac class)
            JLabel restartLabel = new JLabel(Labels.getString("Common.Restart") + " myAgilePomodoro");
            restartLabel.setFont(getFont().deriveFont(Font.BOLD));
            validPanel.add(restartLabel);
        } else {
            validPanel.add(restartButton);
        }
        validPanel.setVisible(false);
        add(validPanel, gbc);
    }

    protected void setValidation(String validationText) {
        validation.setText(validationText);
        restartButton.setVisible(true);
    }

    public void disableSaveButton() {
        saveButton.setEnabled(false);
        saveButton.setForeground(Color.GRAY);
    }

    public void enableSaveButton() {
        saveButton.setEnabled(true);
        saveButton.setForeground(getForeground()); // reset the foreground of the current theme
    }

    public void clearValidation() {
        validation.setText("");
        validPanel.setVisible(false);
    }

    private void updatePreferences() {
        preferences.setPomodoroLength(preferencesInputFormPanel.pomodoroSlider.getSliderValue());
        preferences.setShortBreakLength(preferencesInputFormPanel.shortBreakSlider.getSliderValue());
        preferences.setLongBreakLength(preferencesInputFormPanel.longBreakSlider.getSliderValue());
        preferences.setMaxNbPomPerActivity(preferencesInputFormPanel.maxNbPomPerActivitySlider.getSliderValue());
        preferences.setMaxNbPomPerDay(preferencesInputFormPanel.maxNbPomPerDaySlider.getSliderValue());
        preferences.setNbPomPerSet(preferencesInputFormPanel.nbPomPerSetSlider.getSliderValue());
        preferences.setTicking(preferencesInputFormPanel.tickingBox.isSelected());
        preferences.setRinging(preferencesInputFormPanel.ringingBox.isSelected());
        preferences.setLocale(((ItemLocale) preferencesInputFormPanel.localesComboBox.getSelectedItem()).getLocale());
        preferences.setSystemTray(preferencesInputFormPanel.systemTrayBox.isSelected());
        preferences.setSystemTrayMessage(preferencesInputFormPanel.systemTrayMessageBox.isSelected());
        preferences.setAlwaysOnTop(preferencesInputFormPanel.alwaysOnTopBox.isSelected());
        preferences.setBringToFront(preferencesInputFormPanel.bringToFrontBox.isSelected());
        preferences.setAgileMode(preferencesInputFormPanel.agileModeBox.isSelected());
        preferences.setPlainHours(preferencesInputFormPanel.plainHoursBox.isSelected());
        preferences.setTheme((String) preferencesInputFormPanel.themesComboBox.getSelectedItem());
        preferences.updatePreferences();
    }
}
