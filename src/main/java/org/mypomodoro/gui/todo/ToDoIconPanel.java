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
package org.mypomodoro.gui.todo;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.mypomodoro.Main;
import org.mypomodoro.buttons.DefaultButton;
import org.mypomodoro.model.Activity;
import org.mypomodoro.util.ColorUtil;

/**
 * Icon/Button Panel
 *
 */
public class ToDoIconPanel {

    private final static ImageIcon SQUARECROSSICON = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "squareCross.png"));
    private final static ImageIcon SQUAREICON = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "square.png"));
    private final static ImageIcon QUOTEICON = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "quote.png"));
    private final static ImageIcon DASHICON = new ImageIcon(Main.class.getResource(Main.iconsSetPath + "dash.png"));

    static public void showIconPanel(JPanel iconPanel, Activity activity, Color color) {
        showIconPanel(iconPanel, activity, color, true);
    }

    static public void showIconPanel(JPanel iconPanel, Activity activity, Color color, boolean showName) {
        // Remove all components
        iconPanel.removeAll();

        // Set panel
        iconPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
        iconPanel.setFont(new JLabel().getFont().deriveFont(Font.BOLD));

        // Add label component
        JLabel iconLabel = new JLabel();
        if (showName) {
            String activityName = activity.getName().length() > 25 ? activity.getName().substring(0, 25) + "..." : activity.getName();
            String textValue = "<html>";
            if (activity.getRecordedTime() > 0) {
                textValue += "<span style=\"color:" + ColorUtil.toHex(Main.taskRunningColor) + "\">*</span>";
                // Font size increased : "<span style=\"font-size:" + (renderer.getFont().getSize() + 10) + "pt;color:" + ColorUtil.toHex(Main.taskRunningColor) + "\">*</span>";                
            }
            if (activity.isCompleted() && activity.isSubTask()) { // tasks can't be done-done at this point
                textValue += "<strike> " + activityName + " </strike>";
            } else {
                textValue += activityName;
            }
            String tooltipValue = textValue;
            if (activity.getRecordedTime() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                String time = " (" + "<span style=\"color:" + ColorUtil.toHex(Main.taskRunningColor) + ";background-color:" + ColorUtil.toHex(ColorUtil.GREEN_TIMER) + "\"><b> " + sdf.format(activity.getRecordedTime()) + " </b></span>" + ")";
                textValue += time;
            }
            iconLabel.setText(textValue + "</html>");
            iconLabel.setToolTipText(tooltipValue + "</html>");
            iconLabel.setForeground(color);
            iconLabel.setFont(iconPanel.getFont().deriveFont(Font.BOLD));
        }
        iconPanel.add(iconLabel);

        // Add icon/buttons
        int estimatedPoms = activity.getEstimatedPoms();
        int realPoms = activity.getActualPoms();
        int overestimatedPoms = activity.getOverestimatedPoms();
        int numInternalInterruptions = activity.getNumInternalInterruptions();
        int numExternalInterruptions = activity.getNumInterruptions();

        // Estimated pomodoros
        for (int i = 0; i < estimatedPoms; i++) {
            if (realPoms >= i + 1) {
                // We can disable the button but it won't look nice on JTattoo Noire themes
                //squareCrossButton.setEnabled(false);
                //squareCrossButton.setDisabledIcon(squareCrossIcon); // icon used when button is disable                                
                iconPanel.add(new DefaultButton(SQUARECROSSICON, true));
            } else {
                iconPanel.add(new DefaultButton(SQUAREICON, true));
            }
        }
        // Overestimated pomodoros
        if (overestimatedPoms > 0) {
            // Plus sign            
            JLabel plus = new JLabel("+");
            plus.setForeground(ColorUtil.BLACK);
            plus.setFont(iconPanel.getFont().deriveFont(Font.BOLD));
            iconPanel.add(plus);
            // Overestimated pomodoros
            for (int i = 0; i < overestimatedPoms; i++) {
                if (realPoms >= estimatedPoms + i + 1) {
                    iconPanel.add(new DefaultButton(SQUARECROSSICON, true));
                } else {
                    iconPanel.add(new DefaultButton(SQUAREICON, true));
                }
            }
        }
        // Internal interruption
        for (int i = 0; i < numInternalInterruptions; i++) {
            DefaultButton quoteButton = new DefaultButton(QUOTEICON, true);
            iconPanel.add(quoteButton);
        }
        // External interruption        
        for (int i = 0; i < numExternalInterruptions; i++) {
            DefaultButton dashButton = new DefaultButton(DASHICON, true);
            iconPanel.add(dashButton);
        }
    }

    static public void clearIconPanel(JPanel iconPanel) {
        iconPanel.removeAll();
    }
}
