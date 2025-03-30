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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.swing.JPanel;
import org.mypomodoro.db.Database;

public class WindowPanel extends JPanel {

    CardLayout cardLayout = new CardLayout();
    final JPanel windowPanel = new JPanel(cardLayout);

    public WindowPanel(JPanel iconBar, MainPanel view) {
        setLayout(new BorderLayout());
        setOpaque(true);
        add(iconBar, BorderLayout.NORTH);
        if (Database.firstTime) { // display preferences as splash screen when MSQlite database is being created for the first time (does not work with MySQL)
            windowPanel.add(view.getPreferencesPanel(), view.getPreferencesPanel().getClass().getName());
            windowPanel.add(view.getSplashScreen(), view.getSplashScreen().getClass().getName());
        } else {
            windowPanel.add(view.getSplashScreen(), view.getSplashScreen().getClass().getName());
            windowPanel.add(view.getPreferencesPanel(), view.getPreferencesPanel().getClass().getName());
        }
        windowPanel.add(view.getCreatePanel(), view.getCreatePanel().getClass().getName());
        windowPanel.add(view.getActivityListPanel(), view.getActivityListPanel().getClass().getName());
        windowPanel.add(view.getToDoPanel(), view.getToDoPanel().getClass().getName());
        windowPanel.add(view.getReportListPanel(), view.getReportListPanel().getClass().getName());
        windowPanel.add(view.getChartTabbedPanel(), view.getChartTabbedPanel().getClass().getName());
        add(windowPanel, BorderLayout.CENTER);
        add(view.getProgressBar(), BorderLayout.SOUTH);
    }

    public void showPanel(String name) {
        cardLayout.show(windowPanel, name);
    }
}
