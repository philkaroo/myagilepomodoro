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
import org.mypomodoro.gui.MainPanel;
import org.mypomodoro.util.Labels;

/**
 * Up / Downsize the app
 *
 *
 */
public class ResizeButton extends JButton {

    private final ImageIcon upSizeIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "upsize.png"));
    private final ImageIcon downSizeIcon = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "downsize.png"));

    public ResizeButton() {
        setUpSizeIcon();
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                MainPanel.RESIZE.resize();
            }
        });
    }

    public void setUpSizeIcon() {
        setIcon(upSizeIcon);
        setToolTipText(Labels.getString("ToDoListPanel.Enlarge") + " (ALT + M)");
    }

    public void setDownSizeIcon() {
        setIcon(downSizeIcon);
        setToolTipText(Labels.getString("ToDoListPanel.Reduce") + " (ALT + M)");
    }
}
