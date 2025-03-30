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
package org.mypomodoro.util;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.mypomodoro.buttons.DefaultButton;

/**
 * Progress bar
 *
 */
public class ProgressBar extends JPanel {

    private final JProgressBar bar = new JProgressBar();
    private boolean stopped = false;

    public ProgressBar() {
        setVisible(false);
        setMinimumSize(new Dimension(getWidth(), 30));
        setMaximumSize(new Dimension(getWidth(), 30));
        setPreferredSize(new Dimension(getWidth(), 30));
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 5, 10), 0, 0);
        //bar.setOpaque(true); // required (?) for custom colors to be displayed
        bar.setStringPainted(true); // required for custom colors to be displayed
        bar.setFont(getFont().deriveFont(Font.BOLD));
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.9;
        add(bar, c);
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.1;
        DefaultButton stop = new DefaultButton(Labels.getString("ToDoListPanel.Stop"));
        stop.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stopped = true;
            }
        });
        add(stop, c);
    }

    public JProgressBar getBar() {
        return bar;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean aFlag) {
        stopped = aFlag;
    }
}
