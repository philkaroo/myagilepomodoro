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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import org.mypomodoro.Main;
import org.mypomodoro.util.Labels;

/**
 * Icon bar
 *
 */
public class IconBar extends JPanel {

    private final ArrayList<MyIcon> myIcons = new ArrayList<MyIcon>();
    private MyIcon highlightedIcon;

    public IconBar(MainPanel view) {
        myIcons.add(MyIcon.getInstance(view,
                Labels.getString("IconBar.Create"), "createButton",
                view.getCreatePanel()));
        myIcons.add(MyIcon.getInstance(view,
                Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "IconBar.Activity"), "activityButton",
                view.getActivityListPanel()));
        myIcons.add(MyIcon.getInstance(view,
                Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "IconBar.ToDo"),
                "todoButton", view.getToDoPanel()));
        myIcons.add(MyIcon.getInstance(view,
                Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "IconBar.Report"), "reportButton",
                view.getReportListPanel()));
        myIcons.add(MyIcon.getInstance(view,
                Labels.getString("IconBar.Burndown Chart"), "burndownButton",
                view.getChartTabbedPanel()));
        setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        setPreferredSize(new Dimension(getWidth(), 80));
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        for (MyIcon i : myIcons) {
            add(i, c);
            c.gridx++;
        }
    }

    public void highlightIcon(MyIcon icon) {
        unHighlightIcon();
        icon.highlight();
        icon.setFont(icon.getFont().deriveFont(Font.BOLD));
        highlightedIcon = icon;
    }

    public void unHighlightIcon() {
        if (highlightedIcon != null) {
            highlightedIcon.unhighlight();
            highlightedIcon.setFont(highlightedIcon.getFont().deriveFont(Font.PLAIN));
        }
    }

    public MyIcon getIcon(int i) {
        return myIcons.get(i);
    }
}
