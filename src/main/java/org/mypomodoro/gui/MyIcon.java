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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import org.mypomodoro.Main;
import org.mypomodoro.gui.todo.ToDoPanel;

/**
 * MyIcon singleton for icon bar
 *
 */
public class MyIcon extends JLabel {

    private final Dimension d = new Dimension(1000, 80);
    private final JPanel panel;
    private final ImageIcon on;
    private final ImageIcon off;
    private final ImageIcon onBusy;
    private final ImageIcon offBusy;

    private static ImageIcon getIcon(String path) {
        return new ImageIcon(Main.class.getResource(path));
    }

    public static MyIcon getInstance(final MainPanel view, String text,
            String fileName, JPanel p) {
        String onPath = "/images/" + fileName + "2.png";
        String offPath = "/images/" + fileName + ".png";
        String onBusyPath = onPath;
        String offBusyPath = offPath;
        if (p instanceof ToDoPanel) {
            onBusyPath = "/images/" + fileName + "red2.png";
            offBusyPath = "/images/" + fileName + "red.png";
        }
        ImageIcon onIcon = getIcon(onPath);
        ImageIcon offIcon = getIcon(offPath);
        ImageIcon onBusyIcon = getIcon(onBusyPath);
        ImageIcon offBusyIcon = getIcon(offBusyPath);
        MyIcon myIcon = new MyIcon(view, text, onIcon, onBusyIcon, offIcon, offBusyIcon, p);
        if (Main.myIconBackgroundColor != null) { // mAP laf set this setting
            myIcon.setBackground(Main.myIconBackgroundColor);
        }
        return myIcon;
    }

    public void highlight() {
        if (panel instanceof ToDoPanel && ((ToDoPanel) panel).getPomodoro().inPomodoro()) {
            setIcon(onBusy);
        } else {
            setIcon(on);
        }
    }

    public void unhighlight() {
        if (panel instanceof ToDoPanel && ((ToDoPanel) panel).getPomodoro().inPomodoro()) {
            setIcon(offBusy);
        } else {
            setIcon(off);
        }
    }

    public MyIcon(final MainPanel view, String Text, ImageIcon on, ImageIcon onBusy,
            ImageIcon off, ImageIcon offBusy, JPanel p) {
        super(off, CENTER);
        setToolTipText(Text);
        this.off = off;
        this.on = on;
        this.offBusy = offBusy;
        this.onBusy = onBusy;
        panel = p;

        setPreferredSize(d);
        setMinimumSize(d);
        setHorizontalTextPosition(JLabel.CENTER);
        setVerticalTextPosition(JLabel.BOTTOM);
        addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                view.getIconBar().highlightIcon(MyIcon.this);
                view.setWindow(panel);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setBorder(new EtchedBorder(EtchedBorder.LOWERED));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBorder(null);
            }
        });
    }

    public JPanel getPanel() {
        return panel;
    }
}
