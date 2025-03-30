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
package org.mypomodoro.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.MenuSelectionManager;
import org.mypomodoro.Main;
import org.mypomodoro.gui.MainPanel;
import org.mypomodoro.gui.MyIcon;
import org.mypomodoro.util.Labels;

/**
 * View Menu
 *
 */
public class ViewMenu extends JMenu {

    private final MainPanel view;

    public ViewMenu(final MainPanel view) {
        super(Labels.getString("MenuBar.View"));
        this.view = view;
        add(new ActivityListItem());
        add(new ToDoListItem());
        add(new ReportListItem());
        add(new BurndownChartItem());
        addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent ex) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                MenuSelectionManager.defaultManager().clearSelectedPath();
            }
        });
    }

    class ActivityListItem extends JMenuItem {

        public ActivityListItem() {
            super(Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "ViewMenu.Activity List"));
            // Adds Keyboard Shortcut Alt-A
            setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                    ActionEvent.ALT_MASK));
            addActionListener(new MenuItemListener());
        }

        class MenuItemListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                MyIcon activityListIcon = view.getIconBar().getIcon(1);
                view.getIconBar().highlightIcon(activityListIcon);
                view.setWindow(view.getActivityListPanel());
            }
        }
    }

    class ToDoListItem extends JMenuItem {

        public ToDoListItem() {
            super(Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "ViewMenu.ToDo List"));
            // Adds Keyboard Shortcut Alt-T
            setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
                    ActionEvent.ALT_MASK));
            addActionListener(new MenuItemListener());
        }

        class MenuItemListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                MyIcon toDoListIcon = view.getIconBar().getIcon(2);
                view.getIconBar().highlightIcon(toDoListIcon);
                view.setWindow(view.getToDoPanel());
            }
        }
    }

    class ReportListItem extends JMenuItem {

        public ReportListItem() {
            super(Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "ViewMenu.Report List"));
            // Adds Keyboard Shortcut Alt-R
            setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                    ActionEvent.ALT_MASK));
            addActionListener(new MenuItemListener());
        }

        class MenuItemListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                MyIcon reportListIcon = view.getIconBar().getIcon(3);
                view.getIconBar().highlightIcon(reportListIcon);
                view.setWindow(view.getReportListPanel());
            }
        }
    }

    class BurndownChartItem extends JMenuItem {

        public BurndownChartItem() {
            super(Labels.getString("ViewMenu.Burndown Chart"));
            // Adds Keyboard Shortcut Alt-B
            setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
                    ActionEvent.ALT_MASK));
            addActionListener(new MenuItemListener());
        }

        class MenuItemListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                MyIcon burndownChartIcon = view.getIconBar().getIcon(4);
                view.getIconBar().highlightIcon(burndownChartIcon);
                view.setWindow(view.getChartTabbedPanel());
            }
        }
    }
}
