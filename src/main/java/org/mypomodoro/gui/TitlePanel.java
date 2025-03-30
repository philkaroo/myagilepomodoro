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

/**
 * Title panel for tables
 *
 */
public class TitlePanel extends AbstractTitlePanel {

    private final IListPanel panel;
    private final AbstractTable table;

    public TitlePanel(IListPanel panel, AbstractTable table) {
        super();
        this.panel = panel;
        this.table = table;
    }

    @Override
    protected void scrollToSelectedRows() {
        table.scrollToSelectedRows();
    }

    @Override
    protected void createNewTask() {
        table.createNewTask();
    }

    @Override
    protected void duplicateTask() {
        table.duplicateTask();
    }

    @Override
    protected void createUnplannedTask() {
        table.createUnplannedTask();
    }

    @Override
    protected void createInternalInterruption() {
        table.createInternalInterruption();
    }

    @Override
    protected void createExternalInterruption() {
        table.createExternalInterruption();
    }

    @Override
    protected void overestimateTask(int poms) {
        table.overestimateTask(poms);
    }

    @Override
    protected void setSubtaskComplete() {
        table.setSubtaskComplete();
    }

    @Override
    protected void setTaskDoneDone() {
        table.setTaskDoneDone();
    }

    /*@Override
     protected void moveSubtasks() {
     table.moveSubtasksToMainTable();
     }*/
    @Override
    protected void refreshTable(boolean fromDatabase) {
        panel.refresh(fromDatabase);
    }
}
