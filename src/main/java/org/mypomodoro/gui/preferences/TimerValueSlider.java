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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.mypomodoro.Main;
import org.mypomodoro.util.TimeConverter;

public class TimerValueSlider extends JPanel {

    private final JSlider slider;
    private final JLabel label = new JLabel();
    private final String textLabel;
    private final boolean displaylength;
    private final boolean lengthInHours;

    public TimerValueSlider(final PreferencesPanel controlPanel, int min, int max,
            int val, final int recommendedMin,
            final int recommendedMax,
            String textLabel) {
        this(controlPanel, min, max,
                val, recommendedMin,
                recommendedMax,
                textLabel, false, false);
    }

    public TimerValueSlider(final PreferencesPanel controlPanel, int min, int max,
            int val, final int recommendedMin,
            final int recommendedMax,
            String textLabel,
            boolean displaylength, boolean lengthInHours) {
        this.textLabel = textLabel;
        this.displaylength = displaylength;
        this.lengthInHours = lengthInHours;
        setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
        slider = new JSlider(min, max, val);
        setSliderColor(recommendedMin, recommendedMax);
        slider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                setSliderColor(recommendedMin, recommendedMax);
                controlPanel.enableSaveButton();
                controlPanel.clearValidation();
            }
        });
        add(slider);
        setText();
        add(label);
    }

    public void setText() {
        int sliderValue = slider.getValue();
        String text = " " + sliderValue + " ";
        text += textLabel;
        if (displaylength) {
            if (lengthInHours) {
                text += " (" + TimeConverter.getLengthInHours(sliderValue) + ")";
            } else {
                text += " (" + TimeConverter.getLength(sliderValue) + ")";
            }
        }
        label.setText(text);
    }

    public void setText(int pomodoroLength, int shortBreakLength, int longBreakLength, int nbPomPerSet, boolean isPlainHours, int nbMaxNbPomPerDay) {
        int sliderValue = slider.getValue();
        String text = " " + sliderValue + " ";
        text += textLabel;
        if (displaylength) {
            if (lengthInHours) {
                text += " (" + TimeConverter.getLengthInHours(sliderValue, pomodoroLength, shortBreakLength, longBreakLength, nbPomPerSet, isPlainHours) + ")";
            } else {
                text += " (" + TimeConverter.getLength(sliderValue, pomodoroLength, shortBreakLength, longBreakLength, nbPomPerSet, isPlainHours, nbMaxNbPomPerDay) + ")";
            }
        }
        label.setText(text);
    }

    public JSlider getSlider() {
        return slider;
    }

    public int getSliderValue() {
        return slider.getValue();
    }

    public void setSliderValue(int value) {
        slider.setValue(value);
    }

    public void setSliderColor(int recommendedMin, int recommendedMax) {
        if (getSliderValue() < recommendedMin
                || getSliderValue() > recommendedMax) {
            slider.setBackground(Color.orange);
        } else {
            slider.setBackground(Main.taskFinishedColor);
        }
    }

    public void changeSlider(int max) {
        slider.setMaximum(max);
        slider.repaint();
    }
}
