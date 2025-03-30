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
 * ToDo list
 *
 */
public class ToDoList extends AbstractActivities {

    private static final ToDoList list = new ToDoList();

    private ToDoList() {
        refresh();
    }

    @Override
    final public void refresh() {
        removeAll();
        for (Activity act : ActivitiesDAO.getInstance().getTODOs()) {
            super.add(act);
        }
    }

    public static ToDoList getList() {
        return list;
    }

    // List of main tasks
    public static ToDoList getTaskList() {
        ToDoList tableList = new ToDoList();
        for (Activity a : list) {
            if (a.isSubTask()) {
                tableList.removeById(a.getId());
            }
        }
        return tableList;
    }

    // List of sub tasks
    // The bigger the list the heavier this will be
    // May we use use Guava https://github.com/google/guava
    // OR have a specific list for subtasks ?...
    public static ToDoList getSubTaskList(int parentId) {
        ToDoList subTableList = new ToDoList();
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
        add(act, act.getDate());
    }

    public void add(Activity act, Date date) {
        add(act, date, new Date(0)); // reset date reopen to avoid any confusion with date completed
    }

    public void add(Activity act, Date date, Date dateCompleted) {
        if (act.isSubTask()) {
            act.setPriority(getSubTasks(act.getParentId()).size() + 1);
        } else {
            act.setPriority(getTasks().size() + 1);
        }
        act.setDate(date);
        act.setDateCompleted(dateCompleted);
        if (act.isTask()) {
            act.setIsCompleted(false);
            act.setIsDoneDone(false); // tasks cannot be done-done in todo list --> this makes sure we remove done-done status of imported tasks
        }
        if (act.getId() == -1) { // add to the database (new todo)
            act.setId(act.databaseInsert());
        } else { // update in database (modified todo or moved from activity list)
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
        remove(activity);
        activity.databaseDelete();
    }

    // Move a task and its subtasks to ActivityList
    public void moveToActivtyList(Activity activity) {
        if (activity.isTask()) {
            ArrayList<Activity> subList = getSubTasks(activity.getId());
            for (Activity subTask : subList) {
                ActivityList.getList().add(subTask);
                remove(subTask);
            }
        }
        ActivityList.getList().add(activity); // set the priority and update the database
        remove(activity);
    }

    /*public void moveAll() {
     for (Activity activity : activities) {
     ActivityList.getList().add(activity);
     }
     ActivitiesDAO.getInstance().moveAllTODOs();
     removeAll();
     }*/
    // Complete a task and its subtasks to ReportList
    public void completeToReportList(Activity activity) {
        if (activity.isTask()) {
            ArrayList<Activity> subList = getSubTasks(activity.getId());
            for (Activity subTask : subList) {
                ReportList.getList().add(subTask);
                remove(subTask);
            }
        }
        ReportList.getList().add(activity);
        remove(activity);
    }

    /*public void completeAll() {
     for (Activity activity : activities) {
     ReportList.getList().add(activity);
     }
     ActivitiesDAO.getInstance().completeAllTODOs();
     removeAll();
     }*/
    // set new priorities
    public void reorderByPriority() {
        sortByPriority(); // sort what is left of the ToDos (after delete, complete...)        
        // Very slow because of the per-row database update
        // Very QUICK with drag and drop
        int increment = 1;
        for (Activity activity : activities) {
            if (activity.getPriority() != increment) { // optimization
                activity.setPriority(increment);
                list.update(activity); // the update must be done on the list !!!! (because this is a sub list that is calling this method)
                activity.databaseUpdate();
            }
            increment++;
        }
        /*
         // As slow as previous algo
         // Very slow with drag and drop
         ArrayList<Activity> alist;
         alist = clone();        
         removeAll();
         for (Activity activity : alist) {            
         add(activity);
         }*/
    }
}
