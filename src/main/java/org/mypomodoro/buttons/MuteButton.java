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
package org.mypomodoro.buttons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.mypomodoro.Main;
import org.mypomodoro.gui.preferences.PreferencesInputForm;
import org.mypomodoro.gui.todo.Pomodoro;
import org.mypomodoro.util.CheckWindowsClassicTheme;
import org.mypomodoro.util.ColorUtil;
import org.mypomodoro.util.Labels;

/**
 * Mute sound button
 *
 */
public class MuteButton extends JButton {

    private final ImageIcon muteIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "mute.png"));
    private final ImageIcon soundIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "sound.png"));
    private boolean isSound = true;

    public MuteButton(final Pomodoro pomodoro) {
        setMute();
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (isSound) {
                    setSound();
                    pomodoro.mute();
                } else {
                    setMute();
                    pomodoro.unmute();
                }
            }
        });
    }

    private void setMute() {
        isSound = true;
        setIcon(soundIcon);
        setBackground(null);
        setOpaque(true);
        setContentAreaFilled(true);
        setBorderPainted(true);
        setToolTipText(Labels.getString("ToDoListPanel.Mute"));
    }

    private void setSound() {
        isSound = false;
        setIcon(muteIcon);
        setBackground(ColorUtil.GRAY);
        setToolTipText(Labels.getString("ToDoListPanel.Unmute"));
        // Win LAF classic, InfoNode and Plastic3D get flattened and grayed out only if not opaque
        if (!CheckWindowsClassicTheme.isWindowsClassicLAF()
                && !Main.preferences.getTheme().equalsIgnoreCase(PreferencesInputForm.INFONODE_LAF)
                && !Main.preferences.getTheme().equalsIgnoreCase(PreferencesInputForm.PLASTIC3D_LAF)) {
            setOpaque(false);
        }
        // Nimrod, PGS, System(Metal) get flattened only                 
        // Seaglass and win LAF under win7 get the change of icon only       
        // To make the button disappear for Seaglass and win LAF under win7
        /*if (Main.preferences.getTheme().equalsIgnoreCase(PreferencesInputForm.SEAGLASS_LAF)
         || (!CheckWindowsClassicTheme.isWindowsClassicLAF() && CheckWindowsClassicTheme.isWindowsLAF())) {
         setContentAreaFilled(false);
         setBorderPainted(false);
         }*/
    }
}
