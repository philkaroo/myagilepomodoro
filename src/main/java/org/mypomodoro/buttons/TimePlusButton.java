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
import org.mypomodoro.Main;
import org.mypomodoro.gui.todo.Pomodoro;

/**
 * Time plus button
 *
 */
public class TimePlusButton extends TransparentButton {

    private final ImageIcon timePlusIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "timeplus.png"));
    private final ImageIcon timePlusRedIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "timeplusred.png"));

    public TimePlusButton(final Pomodoro pomodoro) {
        setIcon(timePlusIcon);
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                pomodoro.increaseTime();
                // refresh tooltip on timer
                pomodoro.setTooltipOnImage();
            }
        });
    }

    public void setTimePlusRedIcon(boolean aFlag) {
        if (aFlag) {
            setIcon(timePlusRedIcon);
        } else {
            setIcon(timePlusIcon);
        }
    }
}
