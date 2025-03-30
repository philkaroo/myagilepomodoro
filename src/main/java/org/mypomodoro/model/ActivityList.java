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
package org.mypomodoro.model;

import java.util.ArrayList;
import java.util.Date;
import org.mypomodoro.db.ActivitiesDAO;

/**
 * Activity list
 *
 */
public class ActivityList extends AbstractActivities {

    private static final ActivityList list = new ActivityList();

    private ActivityList() {
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();
        for (Activity act : ActivitiesDAO.getInstance().getActivities()) {
            super.add(act);
        }
    }

    // List of all tasks
    public static ActivityList getList() {
        return list;
    }

    // List of main tasks
    public static ActivityList getTaskList() {
        ActivityList tableList = new ActivityList();
        for (Activity a : list) {
            if (a.isSubTask()) {
                tableList.removeById(a.getId());
            }
        }
        return tableList;
    }

    public static boolean hasSubTasks(int activityId) {
        boolean hasSubTasks = false;
        for (Activity a : list) {
            if (a.getParentId() == activityId) {
                hasSubTasks = true;
                break;
            }
        }
        return hasSubTasks;
    }

    public static ActivityList getSubTaskList(int parentId) {
        ActivityList subTableList = new ActivityList();
        for (Activity a : list) {
            if (a.getParentId() != parentId) {
                subTableList.removeById(a.getId());
            }
        }
        return subTableList;
    }

    public static int getListSize() {
        return getList().size();
    }

    @Override
    public void add(Activity act) {
        add(act, act.getDate()); // date creation/schedule
    }

    public void add(Activity act, Date date) {
        add(act, date, act.getDateCompleted()); // date creation/schedule, date complete
    }

    // Create or update
    public void add(Activity act, Date date, Date dateReopen) {
        act.setPriority(-1);
        act.setDate(date);
        if (act.isTask()) {
            act.setIsCompleted(false);
            act.setDateCompleted(dateReopen);
            act.setIsDoneDone(false); // tasks cannot be done-done in activity list --> this makes sure we remove done-done status of imported and reopened tasks        
        }
        act.recordTime(-1); // no recorded time left
        if (act.getId() == -1) { // add to the database (new activity)
            act.setId(act.databaseInsert());
        } else { // update in database (modified activity or moved from todo list / reopened from report list)
            act.databaseUpdate();
        }
        super.add(act); // add to the list
    }

    public Activity duplicate(Activity activity) throws CloneNotSupportedException {
        return duplicate(activity, activity.isSubTask() ? activity.getParentId() : -1);
    }

    public Activity duplicate(Activity activity, int parentId) throws CloneNotSupportedException {
        Activity clonedActivity = activity.clone(); // a clone is necessary to remove the reference/pointer to the original task        
        clonedActivity.setActualPoms(0);
        clonedActivity.setOverestimatedPoms(0);
        clonedActivity.setName("(D) " + clonedActivity.getName());
        if (activity.isSubTask()) {
            clonedActivity.setParentId(parentId);
            clonedActivity.setIsCompleted(false);
            getList().add(clonedActivity, new Date());
        } else {
            getList().add(clonedActivity, new Date(), new Date(0)); // add task here to get the new Id to be the parentId of the subtasks
            ArrayList<Activity> subList = getSubTasks(activity.getId());
            for (Activity subTask : subList) {
                duplicate(subTask, clonedActivity.getId());
            }
        }
        return clonedActivity;
    }

    @Override
    public void delete(Activity activity) {
        if (activity.isTask()) {
            ArrayList<Activity> subList = getSubTasks(activity.getId());
            for (Activity subTask : subList) {
                remove(subTask);
            }
        }
        remove(activity);
        activity.databaseDelete(); // delete tasks and subtasks
    }

    /*public void deleteAll() {
        ActivitiesDAO.getInstance().deleteAllActivities();
        removeAll();
    }*/
    // Move a task and its subtasks to ToDoList
    public void moveToTODOList(Activity activity) {
        ArrayList<Activity> subList = getSubTasks(activity.getId());
        for (Activity subTask : subList) {
            ToDoList.getList().add(subTask);
            remove(subTask);
        }
        ToDoList.getList().add(activity); // this sets the priority and update the database
        remove(activity);
    }
}
