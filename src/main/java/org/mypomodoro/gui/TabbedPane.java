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
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EtchedBorder;
import org.mypomodoro.Main;

/**
 *
 *
 */
public class TabbedPane extends JTabbedPane {

    private final IListPanel panel;

    // Tab indices
    private int detailsTabIndex = -1;
    private int commentTabIndex = -1;
    private int editTabIndex = -1;
    private int mergeTabIndex = -1;
    private int importTabIndex = -1;
    private int exportTabIndex = -1;
    // mouse adapter
    private final CustomMouseAdapter customMouseAdapter = new CustomMouseAdapter();

    public TabbedPane(IListPanel panel) {
        this.panel = panel;
        setFocusable(false); // removes borders around tab text
        setForeground(new JTabbedPane().getForeground()); // this is necessary for themes such as JTatoo Noire
        // Manage mouse hovering
        addMouseMotionListener(new HoverMouseMotionAdapter());
        // This is to address the case/event when the mouse exit the title
        addMouseListener(new ExitMouseAdapter());
        // One click action (expand / fold)
        addMouseListener(customMouseAdapter);
    }

    // Hover
    class HoverMouseMotionAdapter extends MouseMotionAdapter {

        @Override
        public void mouseMoved(MouseEvent e) {
            setBorder(new EtchedBorder(EtchedBorder.LOWERED, Main.selectedRowColor, Main.rowBorderColor));
            //setBackground(Main.hoverRowColor);            
        }
    }

    // Exit
    class ExitMouseAdapter extends MouseAdapter {

        @Override
        public void mouseExited(MouseEvent e) {
            setBorder(new EtchedBorder(EtchedBorder.LOWERED));
            //JPanel p = new JPanel();
            //setBackground(p.getBackground()); // reset default/theme background color            
        }
    }

    // Implement one-click action on selected tabs
    // Tab already selected = one click to expand
    // Tab not selected = double click to expand
    class CustomMouseAdapter extends MouseAdapter {

        private int originalDividerLocation;
        private int selectedIndex = 0; // first tab is selected by default
        private Robot robot = null; // used to move the cursor
        private int viewCount = 0;

        public CustomMouseAdapter() {
            try {
                robot = new Robot();
            } catch (AWTException ignored) {
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            Double topPosition = 0.0;
            Double bottomPosition = 0.93;
            JSplitPane splitPane = panel.getSplitPane();
            if (e.getClickCount() == 1) {
                Point pOriginal = e.getLocationOnScreen(); // original location on screen
                if (viewCount == 2) { // set back to original position                    
                    splitPane.setDividerLocation(originalDividerLocation);
                    viewCount = 0;
                } else if (selectedIndex == getSelectedIndex()) {
                    if (viewCount == 0) { // set divider on top
                        originalDividerLocation = splitPane.getDividerLocation();
                        splitPane.setDividerLocation(topPosition); // top
                        viewCount = 1;
                    } else if (viewCount == 1) { // set divider on bottom                        
                        splitPane.setDividerLocation(bottomPosition); // bottom
                        viewCount = 2;
                    }
                }
                // The following line is required to get the cursor to move correctly        
                splitPane.validate();
                // Set cursor on splitpane
                if (robot != null) {
                    Point pFinal = getSelectedComponent().getLocationOnScreen(); // final location on screen
                    // Set cursor at the same original X position
                    robot.mouseMove((int) pOriginal.getX(), (int) pFinal.getY() - 15); // center the cursor in the middle of the selected tab
                }
                selectedIndex = getSelectedIndex();
            }
        }

        public void setSelectedIndex(int index) {
            selectedIndex = index;
        }
    }

    public void initTabs(int rowCount) {
        if (rowCount == 0) {
            for (int index = 0; index < getTabCount(); index++) {
                if (index == importTabIndex) { // import tab
                    setSelectedIndex(index);
                    customMouseAdapter.setSelectedIndex(index); // make sure the selected index for the adapter is up to date
                    continue;
                }
                setEnabledAt(index, false);
            }
        } else {
            for (int index = 0; index < getTabCount(); index++) {
                setEnabledAt(index, index != mergeTabIndex); // merge tab                                  
            }
            setSelectedIndex(0);
            customMouseAdapter.setSelectedIndex(0); // make sure the selected index for the adapter is up to date
        }
    }

    public void selectEditTab() {
        setSelectedIndex(editTabIndex);
        customMouseAdapter.setSelectedIndex(editTabIndex); // make sure the selected index for the adapter is up to date
    }

    public void enableMergeTab() {
        setEnabledAt(mergeTabIndex, true);
    }

    public void disableCommentTab() {
        setEnabledAt(commentTabIndex, false);
    }

    public void disableEditTab() {
        setEnabledAt(editTabIndex, false);
    }

    public void disableMergeTab() {
        setEnabledAt(mergeTabIndex, false);
    }

    public int getDetailsTabIndex() {
        return detailsTabIndex;
    }

    public void setDetailsTabIndex(int detailsTabIndex) {
        this.detailsTabIndex = detailsTabIndex;
    }

    public int getCommentTabIndex() {
        return commentTabIndex;
    }

    public void setCommentTabIndex(int commentTabIndex) {
        this.commentTabIndex = commentTabIndex;
    }

    public int getEditTabIndex() {
        return editTabIndex;
    }

    public void setEditTabIndex(int editTabIndex) {
        this.editTabIndex = editTabIndex;
    }

    public int getMergeTabIndex() {
        return mergeTabIndex;
    }

    public void setMergeTabIndex(int mergeTabIndex) {
        this.mergeTabIndex = mergeTabIndex;
    }

    public int getImportTabIndex() {
        return importTabIndex;
    }

    public void setImportTabIndex(int importTabIndex) {
        this.importTabIndex = importTabIndex;
    }

    public int getExportTabIndex() {
        return exportTabIndex;
    }

    public void setExportTabIndex(int exportTabIndex) {
        this.exportTabIndex = exportTabIndex;
    }

    public void setSelectedIndexOnCustomMouseAdapter(int index) {
        customMouseAdapter.setSelectedIndex(index);
    }

    // set tooltip
    @Override
    public void setEnabledAt(int index, boolean enabled) {
        super.setEnabledAt(index, enabled);
        String tooltip = enabled ? getTitleAt(index) + " (CTRL + " + (index + 1) + " )" : null;
        setToolTipTextAt(index, tooltip);
    }
}
