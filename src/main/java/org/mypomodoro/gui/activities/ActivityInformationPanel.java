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
package org.mypomodoro.gui.activities;

import java.util.Iterator;
import java.util.LinkedHashMap;
import javax.swing.JPanel;
import org.mypomodoro.Main;
import org.mypomodoro.gui.IActivityInformation;
import org.mypomodoro.model.Activity;
import org.mypomodoro.util.DateUtil;
import org.mypomodoro.util.HtmlEditor;
import org.mypomodoro.util.Labels;
import org.mypomodoro.util.TimeConverter;

/**
 * Activity information panel
 *
 */
public class ActivityInformationPanel extends JPanel implements IActivityInformation {

    protected final HtmlEditor informationArea = new HtmlEditor();
    protected LinkedHashMap<String, String> textMap = new LinkedHashMap<String, String>();

    public ActivityInformationPanel() {
    }

    @Override
    public void selectInfo(Activity activity) {
        textMap = new LinkedHashMap<String, String>();
        textMap.put("date", "<b>" + (Main.preferences.getAgileMode() || activity.isSubTask() ? Labels.getString("Common.Date created") : Labels.getString("Common.Date scheduled")) + ":</b> "
                + (activity.isUnplanned() ? "U [" : "")
                + DateUtil.getLongFormatedDate(activity.getDate()) + (Main.preferences.getAgileMode() ? ", " + DateUtil.getFormatedTime(activity.getDate()) : "")
                + (activity.isUnplanned() ? "]" : "") + "<br>");
        if (activity.isCompleted()) {
            textMap.put("date_completed", "<b>" + (Main.preferences.getAgileMode() ? Labels.getString("Common.Done") : Labels.getString("Common.Date completed")) + ":</b> "
                    + (activity.isUnplanned() ? "U [" : "")
                    + DateUtil.getLongFormatedDate(activity.getDateCompleted()) + ", " + DateUtil.getFormatedTime(activity.getDateCompleted())
                    + (activity.isUnplanned() ? "]" : "") + "<br>");
        }
        if (activity.isTask()) {
            if (activity.isDoneDone() && Main.preferences.getAgileMode()) {
                textMap.put("date_donedone", "<b>" + Labels.getString("Agile.ReportListPanel.Done-Done") + ":</b> "
                        + (activity.isUnplanned() ? "U [" : "")
                        + DateUtil.getLongFormatedDate(activity.getDateDoneDone()) + ", " + DateUtil.getFormatedTime(activity.getDateDoneDone())
                        + (activity.isUnplanned() ? "]" : "") + "<br>");
            }
            // Date reopened
            // Foreground set to black in anycase (important for theme such as Noire which default color is white)        
            textMap.put("date_reopened", "<span style=\"color:black; background-color:#FFFF66\"><b>" + Labels.getString("Common.Date reopened") + ":</b> "
                    + (activity.isUnplanned() ? "U [" : "")
                    + DateUtil.getLongFormatedDate(activity.getDateCompleted()) + ", " + DateUtil.getFormatedTime(activity.getDateCompleted())
                    + (activity.isUnplanned() ? "]" : "") + "</span><br>");
        }
        textMap.put("title", "<b>" + Labels.getString("Common.Title") + ":</b> " + activity.getName() + "<br>");
        textMap.put("type", "<b>" + Labels.getString("Common.Type") + ":</b> " + (activity.getType().isEmpty() ? "-" : activity.getType()) + "<br>");
        textMap.put("estimated", "<b>" + Labels.getString("Common.Estimated") + ":</b> "
                + activity.getActualPoms() + " / "
                + activity.getEstimatedPoms()
                + (activity.getOverestimatedPoms() > 0 ? " + " + activity.getOverestimatedPoms() : "")
                + " (" + TimeConverter.getLength(activity.getActualPoms()) + " / " + TimeConverter.getLength(activity.getEstimatedPoms() + activity.getOverestimatedPoms()) + ")" + "<br>");
        /*if (Main.preferences.getAgileMode()) {
         textMap.put("storypoints", "<b>" + Labels.getString("Agile.Common.Story Points") + ":</b> " + displayStoryPoint(activity.getStoryPoints()) + "<br>");
         textMap.put("iteration", "<b>" + Labels.getString("Agile.Common.Iteration") + ":</b> " + (activity.getIteration() == -1 ? "-" : activity.getIteration()) + "<br>");
         }*/
        textMap.put("author", "<b>" + Labels.getString("Common.Author") + ":</b> " + (activity.getAuthor().isEmpty() ? "-" : activity.getAuthor()) + "<br>");
        textMap.put("place", "<b>" + Labels.getString("Common.Place") + ":</b> " + (activity.getPlace().isEmpty() ? "-" : activity.getPlace()) + "<br>");
        textMap.put("description", "<b>" + Labels.getString("Common.Description") + ":</b> " + (activity.getDescription().isEmpty() ? "-" : activity.getDescription()) + "<br>");
    }

    // informationArea may be null when moving the cursor around (mouseExited) while deleting/moving tasks
    @Override
    public void showInfo() {
        if (informationArea != null) {
            try {
                Iterator<String> keySetIterator = textMap.keySet().iterator();
                String text = "";
                while (keySetIterator.hasNext()) {
                    String key = keySetIterator.next();
                    text += textMap.get(key);
                }
                informationArea.setText(text);
                // disable auto scrolling
                informationArea.setCaretPosition(0);
            } catch (IndexOutOfBoundsException ignored) {
                // this may happen and must be ignored
            } catch (java.lang.RuntimeException ignored) {
                // this may happen and must be ignored (it happens when a multi, discontinued, selection of rows are moved back to the Activity List or completed)
            }
        }
    }

    @Override
    public void showInfo(String newInfo) {
        if (informationArea != null) {
            informationArea.setText(newInfo);
            // disable auto scrolling
            informationArea.setCaretPosition(0);
        }
    }

    /*private String displayStoryPoint(float points) {
        String text;
        if (points / 0.5 == 1) {
            text = "1/2";
        } else {
            text = Math.round(points) + ""; // used Math.round to display SP as integer (eg: 1.0 --> 1)
        }
        return text;
    }*/
}
