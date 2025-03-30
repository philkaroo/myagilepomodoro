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
import org.mypomodoro.util.Labels;
import org.mypomodoro.util.Restart;

/**
 * Restart button
 *
 */
public class RestartButton extends DefaultButton {

    public RestartButton() {
        super(Labels.getString("Common.Restart") + " myAgilePomodoro");
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Restart.restartApplication(null);
            }
        });
    }
}
