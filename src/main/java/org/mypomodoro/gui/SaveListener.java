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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import org.mypomodoro.gui.create.ActivityInputForm;
import org.mypomodoro.gui.create.CreatePanel;
import org.mypomodoro.model.Activity;
import org.mypomodoro.util.DatePicker;

public class SaveListener implements ActionListener {

    final private CreatePanel panel;

    public SaveListener(CreatePanel panel) {
        this.panel = panel;
    }

    /**
     * Action performer that reacts on button click or on Enter keystroke (see
     * SaveButton) Condition added to prevent the action to be performed when
     * the Enter key is used while editing in a text area, a combo or date
     * picker box
     *
     * @param event
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        boolean doSave = true;
        ActivityInputForm inputForm = (ActivityInputForm) panel.getFormPanel();
        // Check for focus ownership on editable jComboBox
        // Note: isFocusOwner not working on either non-editable combo boxes or textarea embedded in scrollpane 
        Component[] components = ((ActivityInputForm) panel.getFormPanel()).getComponents();
        for (Component component : components) {
            if ((component instanceof JComboBox && (((JComboBox) component).getEditor().getEditorComponent().isFocusOwner()))
                    || (component instanceof DatePicker && (((DatePicker) component).getEditor().isFocusOwner()))
                    || inputForm.getDescriptionField().isFocusOwner()) {
                doSave = false;
                break;
            }
        }
        if (doSave) {
            Activity newActivity = panel.getFormPanel().getActivityFromFields();
            if (newActivity != null) {
                panel.saveActivity(newActivity);
            }
        }
    }
}
