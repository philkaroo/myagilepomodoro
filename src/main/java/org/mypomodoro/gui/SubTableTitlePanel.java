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

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.border.EtchedBorder;
import org.mypomodoro.Main;
import org.mypomodoro.buttons.DefaultButton;
import org.mypomodoro.gui.preferences.PreferencesInputForm;

/**
 *
 *
 */
public class SubTableTitlePanel extends TitlePanel {

    private final IListPanel panel;
    private int viewCount = 0;

    public SubTableTitlePanel(IListPanel panel, AbstractTable table) {
        super(panel, table);

        this.panel = panel;

        // Add listeners
        setListeners();

        // Add listeners to components 
        setListeners(expandButton);
        setListeners(titleLabel);
        setListeners(buttonPanel);
        // Add listeners to buttons
        // One problem remains: when the sutable is empty,  when pressing the create button, the panel is repaint, new buttons appear but the sub table doesn't open
        setListeners(unplannedButton);
        setListeners(internalButton);
        setListeners(externalButton);
        setListeners(overestimationButton);
        setListeners(createButton);
        setListeners(duplicateButton);
        setListeners(selectedButton);
        //setListeners(moveSubtasksButton);
    }

    private void setListeners() {
        setListeners(this);
    }

    private void setListeners(Component comp) {
        // Manage mouse hovering
        comp.addMouseMotionListener(new HoverMouseMotionAdapter());
        // This is to address the case/event when the mouse exit the title
        comp.addMouseListener(new ExitMouseAdapter());
        // On click action (expand / fold)
        comp.addMouseListener(new OneClickMouseAdapter(comp));
    }

    // Hover
    class HoverMouseMotionAdapter extends MouseMotionAdapter {

        @Override
        public void mouseMoved(MouseEvent e) {
            setBorder(new EtchedBorder(EtchedBorder.LOWERED, Main.selectedRowColor, Main.rowBorderColor));
            //setBackground(Main.hoverRowColor);
            //titleLabel.setForeground(ColorUtil.BLACK); // this is necessary for themes such as JTatoo Noire
            //buttonPanel.setBackground(Main.hoverRowColor);
        }
    }

    // Exit
    class ExitMouseAdapter extends MouseAdapter {

        @Override
        public void mouseExited(MouseEvent e) {
            setBorder(new EtchedBorder(EtchedBorder.LOWERED));
            //JPanel p = new JPanel();
            //setBackground(p.getBackground()); // reset default/theme background color
            //titleLabel.setForeground(p.getForeground()); // this is necessary for themes such as JTatoo Noire
            //buttonPanel.setBackground(p.getBackground()); // reset default/theme background color
        }
    }

    // One click: expand / fold
    class OneClickMouseAdapter extends MouseAdapter {

        private final Component comp;
        private Robot robot = null; // used to move the cursor

        public OneClickMouseAdapter(Component comp) {
            this.comp = comp;
            try {
                robot = new Robot();
            } catch (AWTException ignored) {
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1) { // single click
                Point pOriginal = e.getLocationOnScreen(); // original location on screen
                if (viewCount == 2 && !(comp instanceof DefaultButton)) { // fold: excluding click on buttons
                    panel.getListPane().remove(panel.getSubTableScrollPane());
                    panel.addTableTitlePanel();
                    panel.addTable();
                    panel.addSubTableTitlePanel(); // put the sub title back at the bottom
                    viewCount = 0;
                } else if (viewCount == 0
                        && panel.getMainTable().getSelectedRowCount() == 1) { // expand half way: including click on buttons                        
                    panel.getListPane().add(panel.getSubTableScrollPane());
                    viewCount = 1;
                } else if (viewCount == 1 && !(comp instanceof DefaultButton)) { // maximize: excluding click on buttons                        
                    panel.getListPane().remove(panel.getTableScrollPane());
                    panel.getListPane().remove(panel.getTableTitlePanel());
                    viewCount = 2;
                }
                // The next two lines adress an issue found on NimRod theme with the resizing of titles                    
                if (Main.preferences.getTheme().equalsIgnoreCase(PreferencesInputForm.NIMROD_LAF)) {
                    setMaximumSize(new Dimension(Main.gui.getSize().width, 30));
                    panel.getTableTitlePanel().setMaximumSize(new Dimension(Main.gui.getSize().width, 30));
                }
                // The two following lines are required to
                // repaint after resizing and move the cursor correctly
                panel.getListPane().validate();
                panel.getListPane().repaint();
                // Center cursor on panel
                if (robot != null && !(comp instanceof DefaultButton)) {
                    Point pFinal = getLocationOnScreen(); // final location on screen
                    // Set cursor at the same original X position
                    robot.mouseMove((int) pOriginal.getX(), (int) pFinal.getY() + getHeight() / 2);
                }
            }
        }
    }
}
