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
package org.mypomodoro.gui;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import org.mypomodoro.menubar.FileMenu;
import org.mypomodoro.menubar.HelpMenu;
import org.mypomodoro.menubar.TestMenu;
import org.mypomodoro.menubar.ViewMenu;
import org.mypomodoro.util.Clock;

/**
 * Menu Bar
 *
 */
public class MenuBar extends JMenuBar {

    public MenuBar(MainPanel view) {
        add(new FileMenu(view));
        add(new ViewMenu(view));
        add(new TestMenu());
        add(new HelpMenu());
        JPanel clock = new Clock();
        clock.setFont(getFont());
        add(clock);
        setBorder(null);
    }
}
