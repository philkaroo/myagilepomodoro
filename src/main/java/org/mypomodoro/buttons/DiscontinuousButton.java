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
 * Worflow interruption
 *
 * Continue/discontine the pomodoro workflow
 *
 * Continuous : pomodoro and breaks run continiously
 *
 * Discontinous : workflow stops after each break
 *
 */
public class DiscontinuousButton extends JButton {

    private final ImageIcon discontinuousIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "discontinuous.png"));
    private final ImageIcon continuousIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "continuous.png"));
    private boolean isContinuous = true;

    public DiscontinuousButton(final Pomodoro pomodoro) {
        setDiscontinuous();
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (isContinuous) {
                    setContinuous();
                    pomodoro.discontinueWorkflow();
                } else {
                    setDiscontinuous();
                    pomodoro.continueWorkflow();
                }
                pomodoro.setTooltipOnImage();
            }
        });
    }

    private void setDiscontinuous() {
        isContinuous = true;
        setIcon(continuousIcon);
        setBackground(null);
        setOpaque(true);
        setContentAreaFilled(true);
        setBorderPainted(true);
        setToolTipText(Labels.getString("ToDoListPanel.Stop the workflow"));
    }

    private void setContinuous() {
        isContinuous = false;
        setIcon(discontinuousIcon);
        setBackground(ColorUtil.GRAY);
        setToolTipText(Labels.getString("ToDoListPanel.Restore the workflow"));
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
