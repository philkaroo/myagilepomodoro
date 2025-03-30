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

import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.mypomodoro.gui.SaveListener;
import org.mypomodoro.gui.create.CreatePanel;
import org.mypomodoro.util.Labels;

public class SaveButton extends TabPanelButton {

    public SaveButton(CreatePanel panel) {
        super(Labels.getString("Common.Save"));
        setToolTipText(Labels.getString("Common.Save"));
        // Save action
        SaveListener save = new SaveListener(panel);
        // Listener for mouse action
        addActionListener(save);
        // Keystrokes for keyboard action
        // These two lines are required to enable Enter anywhere in the form
        registerKeyboardAction(save,
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(save,
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
