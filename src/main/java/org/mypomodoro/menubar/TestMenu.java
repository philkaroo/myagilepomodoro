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
package org.mypomodoro.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import static java.lang.Thread.sleep;
import java.util.Date;
import java.util.Random;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import org.joda.time.DateTime;
import org.mypomodoro.Main;
import org.mypomodoro.db.ActivitiesDAO;
import org.mypomodoro.gui.ImageIcons;
import org.mypomodoro.gui.MainPanel;
import org.mypomodoro.gui.create.list.TaskTypeList;
import org.mypomodoro.model.AbstractActivities;
import org.mypomodoro.model.Activity;
import org.mypomodoro.model.ActivityList;
import org.mypomodoro.model.ReportList;
import org.mypomodoro.model.ToDoList;
import org.mypomodoro.util.Labels;
import org.mypomodoro.util.WaitCursor;

public class TestMenu extends JMenu {

    public TestMenu() {
        super(Labels.getString("MenuBar.Data"));
        add(new TestDataMenu(100));
        add(new TestDataMenu(500));
        add(new TestDataMenu(1000));
        //add(new TemplateDataMenu());
        add(new JSeparator());
        add(new ResetDataItem());
        addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent ex) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                MenuSelectionManager.defaultManager().clearSelectedPath();
            }
        });
    }

    class TestDataMenu extends JMenu {

        public TestDataMenu(final int nbTask) {
            super(Labels.getString("DataMenu.Test Data") + " (" + nbTask + ")");
            add(new TestDataItem(nbTask, true));
            add(new TestDataItem(nbTask, false));
        }
    }

    /*class TemplateDataMenu extends JMenu {

        public TemplateDataMenu() {
            super(Labels.getString("DataMenu.Template Data"));
            add(new YCombinatorDataItem());
        }
    }*/
    // create test data
    class TestDataItem extends JMenuItem {

        public TestDataItem(final int nbTask, final boolean withSubTask) {
            super(Labels.getString(withSubTask ? "DataMenu.with subtask" : "DataMenu.without subtask"));
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    createTestData(nbTask, withSubTask);
                }
            });
        }

        private void createTestData(final int nbTask, final boolean withSubtask) {
            new Thread() { // This new thread is necessary for updating the progress bar
                @Override
                public void run() {
                    if (!WaitCursor.isStarted()) {
                        // Start wait cursor
                        WaitCursor.startWaitCursor();
                        // Disable item menu
                        setEnabled(false);
                        // Set progress bar
                        MainPanel.progressBar.setVisible(true);
                        MainPanel.progressBar.getBar().setValue(0);
                        MainPanel.progressBar.getBar().setMaximum(nbTask);
                        String[] tasks = new String[]{"Task", "Tâche", "任务", "задача", "कार्य"}; // English, French, Hindi, Russian, Chinese simplified
                        Float[] storypoint = new Float[]{0f, 0.5f, 0.5f, 0.5f, 1f, 1f, 1f, 2f, 2f, 2f, 3f, 3f, 5f, 5f, 8f};
                        Integer[] iterations = new Integer[]{-1, 0, 1, 2, 3, 4};
                        Integer[] iterationsForActivities = new Integer[]{-1, -1, -1, iterations.length - 1, iterations.length};
                        Random rand = new Random();
                        int activityListValue = 0;
                        int subactivityListValue = 0;
                        int todoListValue = 0;
                        int subtodoListValue = 0;
                        int reportListValue = 0;
                        int subreportListValue = 0;
                        final StringBuilder progressText = new StringBuilder();
                        for (int i = 0; i < nbTask; i++) {
                            if (!MainPanel.progressBar.isStopped()) {
                                int iteration = Main.preferences.getAgileMode() ? iterations[rand.nextInt(iterations.length)] : -1;
                                if (iteration == -1 && Main.preferences.getAgileMode()) {
                                    iteration = iterations[rand.nextInt(iterations.length)]; // reduce the occurence of iteration = -1                               
                                }
                                final Activity a = new Activity(
                                        "Place" + " " + (rand.nextInt(10) + 1),
                                        "Author" + " " + (rand.nextInt(10) + 1),
                                        tasks[rand.nextInt(tasks.length)] + " " + (i + 1),
                                        "",
                                        (Main.preferences.getAgileMode() ? (iteration == -1 ? "Other" : TaskTypeList.getTypes().get(rand.nextInt(TaskTypeList.getTypes().size()))) : "Type" + " " + (rand.nextInt(10) + 1)),
                                        rand.nextInt(Main.preferences.getMaxNbPomPerActivity() + 1), // estimation
                                        Main.preferences.getAgileMode() ? storypoint[rand.nextInt(storypoint.length)] : 0,
                                        iteration,
                                        (new DateTime(new Date()).minusDays(rand.nextInt(iterations[iterations.length - 1] + 1 * 5))).toDate());
                                a.setType(a.getType().equals("Subtask") ? "Task" : a.getType()); // Subtask is part of the list of types but this is a task
                                a.setIsCompleted(rand.nextBoolean() && rand.nextBoolean()); // less than Activity List but more than ToDo list
                                int real = rand.nextInt(a.getEstimatedPoms() + 1); // 0 to getEstimatedPoms()
                                if (rand.nextBoolean() && (a.isCompleted() || rand.nextBoolean())) { // once in a while all poms done
                                    real = a.getEstimatedPoms();
                                }
                                if (real > 0 && real == a.getEstimatedPoms()) { // overestimation only if all poms done
                                    a.setOverestimatedPoms(rand.nextInt(6));
                                    if (rand.nextBoolean() && (a.isCompleted() || rand.nextBoolean())) { // once in a while set finished
                                        real += a.getOverestimatedPoms();
                                    } else if (a.getOverestimatedPoms() > 0 && rand.nextBoolean()) {
                                        real += rand.nextInt(a.getOverestimatedPoms() + 1); // 1 to getOverestimatedPoms()                                       
                                    }
                                }
                                a.setActualPoms(real);
                                if (a.getIteration() == -1) {
                                    a.setStoryPoints(0);
                                }
                                if (a.isCompleted()) { // Tasks for the Report list
                                    // Dates
                                    // Date Added must be older than Date Completed
                                    // Date Completed of iteration N must be older than Date completed of iteration N+1
                                    Date dateCompleted;
                                    if (iteration == -1) {
                                        dateCompleted = (new DateTime(new Date()).minusDays(rand.nextInt(iterations[iterations.length - 1] + 1 * 5))).toDate(); // up to 35 days older than today
                                    } else {
                                        dateCompleted = (new DateTime(new Date()).minusDays(rand.nextInt(5) + ((iterations[iterations.length - 1] - iteration) * 5))).toDate(); // int = 0 --> older by 24 to 20 days; int = 1 --> minus 19 to 15 days, etc.
                                    }
                                    Date dateAdded = (new DateTime(dateCompleted).minusDays(rand.nextInt(iterations.length * 5))).toDate(); // up to 30 days older than date completed
                                    if (Main.preferences.getAgileMode()) { // set 1 out of 2 reports as done-done
                                        a.setIsDoneDone(rand.nextBoolean());
                                        if (a.isDoneDone()) {
                                            a.setDateDoneDone(dateCompleted); // say we set the task done-done the same day it is completed
                                        }
                                    }
                                    ReportList.getList().add(a, dateAdded, dateCompleted);
                                    if (rand.nextBoolean() && rand.nextBoolean() && rand.nextBoolean()) { // once in a while reopen a task
                                        ReportList.getList().reopenToActivtyList(a);
                                        if (withSubtask) {
                                            // Adding subtasks
                                            subactivityListValue += addSubTasks(a, ActivityList.getList());
                                        }
                                        Main.gui.getActivityListPanel().getMainTable().insertRow(a); // main table !
                                        activityListValue++;
                                    } else {
                                        if (withSubtask) {
                                            // Adding subtasks
                                            subreportListValue += addSubTasks(a, ReportList.getList());
                                        }
                                        Main.gui.getReportListPanel().getMainTable().insertRow(a); // main table !
                                        reportListValue++;
                                    }
                                } else // Tasks for the Activity and ToDo list
                                {
                                    if (rand.nextBoolean() && rand.nextBoolean() && rand.nextBoolean()) { // less than Activity List and Report List                                                                                                                                                        
                                        if (a.getIteration() >= 0) {
                                            a.setIteration(iterations[iterations.length - 1]); // use highest iteration number for tasks in the Iteration backlog
                                        }
                                        ToDoList.getList().add(a);
                                        if (withSubtask) { // Adding subtasks                                            
                                            subtodoListValue += addSubTasks(a, ToDoList.getList());
                                        }
                                        Main.gui.getToDoPanel().getMainTable().insertRow(a); // main table !
                                        todoListValue++;
                                        if (rand.nextBoolean() && rand.nextBoolean() && rand.nextBoolean()) { // once in a while duplicate a task
                                            try {
                                                // once in a while duplicate a task
                                                Activity duplicatedActivity = ToDoList.getList().duplicate(a);
                                                Main.gui.getToDoPanel().getMainTable().insertRow(duplicatedActivity); // main table !
                                                todoListValue++;
                                                i++; // an extra activity was added
                                            } catch (CloneNotSupportedException ignored) {
                                            }
                                        }
                                    } else { // Tasks for the Activity list
                                        iteration = Main.preferences.getAgileMode() ? iterationsForActivities[rand.nextInt(iterationsForActivities.length)] : -1;
                                        a.setIteration(iteration);
                                        a.setOverestimatedPoms(0);
                                        a.setActualPoms(0);
                                        ActivityList.getList().add(a, a.getDate());
                                        if (withSubtask) { // Adding subtasks                                            
                                            subactivityListValue += addSubTasks(a, ActivityList.getList());
                                        }
                                        Main.gui.getActivityListPanel().getMainTable().insertRow(a);
                                        activityListValue++;
                                        if (rand.nextBoolean() && rand.nextBoolean() && rand.nextBoolean()) { // once in a while duplicate a task
                                            try {
                                                // once in a while duplicate a task
                                                Activity duplicatedActivity = ActivityList.getList().duplicate(a);
                                                Main.gui.getActivityListPanel().getMainTable().insertRow(duplicatedActivity);
                                                activityListValue++;
                                                i++; // an extra activity was added
                                            } catch (CloneNotSupportedException ignored) {
                                            }
                                        }
                                    }
                                }
                                final int progressValue = i + 1;
                                progressText.setLength(0); // reset string builder
                                progressText.append(Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "ActivityListPanel.Activity List") + " : ");
                                progressText.append(Integer.toString(activityListValue));
                                if (withSubtask) {
                                    progressText.append(" (" + Integer.toString(subactivityListValue) + ")");
                                }
                                progressText.append(" | ");
                                progressText.append(Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "ToDoListPanel.ToDo List") + " : ");
                                progressText.append(Integer.toString(todoListValue));
                                if (withSubtask) {
                                    progressText.append(" (" + Integer.toString(subtodoListValue) + ")");
                                }
                                progressText.append(" | ");
                                progressText.append(Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "ReportListPanel.Report List") + " : ");
                                progressText.append(Integer.toString(reportListValue));
                                if (withSubtask) {
                                    progressText.append(" (" + Integer.toString(subreportListValue) + ")");
                                }
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainPanel.progressBar.getBar().setValue(progressValue); // % - required to see the progress                                
                                        MainPanel.progressBar.getBar().setString(progressText.toString()); // task
                                    }
                                });
                            }
                        }
                        // Close progress bar
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                MainPanel.progressBar.getBar().setString(Labels.getString("ProgressBar.Done") + " (" + progressText + ")");
                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            sleep(1000); // wait one second before hiding the progress bar
                                        } catch (InterruptedException ex) {
                                            Main.logger.error("", ex);
                                        }
                                        // hide progress bar
                                        MainPanel.progressBar.getBar().setString(null);
                                        MainPanel.progressBar.setVisible(false);
                                        MainPanel.progressBar.setStopped(false);
                                    }
                                }.start();
                            }
                        });
                        // Enable item menu
                        setEnabled(true);
                        // Stop wait cursor
                        WaitCursor.stopWaitCursor();
                    }
                }
            }.start();
        }

        private int addSubTasks(Activity a, AbstractActivities list) {
            Random rand = new Random();
            int totalEstimated = 0;
            int totalReal = 0;
            int totalOverestimated = 0;
            int nbSubTask = rand.nextInt(6); // 0 to 5 subtasks
            try {
                for (int j = 0; j < nbSubTask; j++) {
                    Activity aClone = a.clone();
                    aClone.setParentId(a.getId());
                    aClone.setName(a.getName() + "." + j);
                    int maxNbPomPerActivity = Main.preferences.getAgileMode() ? 5 : Main.preferences.getMaxNbPomPerActivity(); // 5 poms max for subtasks in Agile mode
                    int estimated = rand.nextInt(maxNbPomPerActivity + 1);
                    if (totalEstimated == maxNbPomPerActivity) { // not enough estimate left
                        estimated = 0;
                    } else {
                        while (estimated + totalEstimated > maxNbPomPerActivity) {
                            estimated = rand.nextInt(maxNbPomPerActivity + 1);
                        }
                    }
                    int real = 0;
                    if (estimated > 0
                            && (list instanceof ToDoList
                            || list instanceof ReportList)) {
                        rand.nextInt(estimated + 1); // 0 to estimated
                        if (rand.nextBoolean() && (list instanceof ReportList || rand.nextBoolean())) { // once in a while all poms done
                            real = estimated;
                        }
                    }
                    int overestimated = 0;
                    if (rand.nextBoolean()
                            && real > 0
                            && real == estimated
                            && (list instanceof ToDoList
                            || list instanceof ReportList)) { // overestimation only if all poms done                        
                        overestimated = rand.nextInt(6);
                        if (rand.nextBoolean() && (list instanceof ReportList || rand.nextBoolean())) { // once in a while set finished
                            real += overestimated;
                        } else if (overestimated > 0 && rand.nextBoolean()) {
                            real += rand.nextInt(overestimated + 1); // 1 to overestimated                                       
                        }
                    }
                    aClone.setEstimatedPoms(estimated);
                    aClone.setOverestimatedPoms(overestimated);
                    aClone.setActualPoms(real);
                    aClone.setStoryPoints(0);
                    aClone.setIteration(-1);
                    aClone.setType(Labels.getString("Common.Subtask"));
                    if (list instanceof ToDoList || list instanceof ReportList || a.isReopen()) {
                        aClone.setIsCompleted(rand.nextBoolean()); // set 1 out of 2 subtasks as done                        
                        if (list instanceof ReportList) {
                            // activity done-done: set subtasks to be completed
                            aClone.setIsCompleted(a.isDoneDone() ? a.isDoneDone() : rand.nextBoolean());
                        }
                        if (aClone.isCompleted()) {
                            aClone.setDateCompleted(aClone.getDate()); // subtask done the same day it is created
                        }
                    }
                    if (list instanceof ToDoList) {
                        ToDoList.getList().add(aClone, a.getDate(), aClone.getDateCompleted());
                    } else if (list instanceof ActivityList) {
                        ActivityList.getList().add(aClone, a.getDate(), aClone.getDateCompleted());
                    } else if (list instanceof ReportList) {
                        ReportList.getList().add(aClone, a.getDate(), aClone.getDateCompleted());
                    }
                    totalEstimated += estimated;
                    totalReal += real;
                    totalOverestimated += overestimated;
                }
            } catch (CloneNotSupportedException ignored) {
            }
            // update parent task 
            if (nbSubTask > 0) {
                a.setEstimatedPoms(totalEstimated);
                a.setActualPoms(totalReal);
                a.setOverestimatedPoms(totalOverestimated);
                if (list instanceof ToDoList) {
                    ToDoList.getList().update(a);
                } else if (list instanceof ActivityList) {
                    ActivityList.getList().update(a);
                } else if (list instanceof ReportList) {
                    ReportList.getList().update(a);
                }
                a.databaseUpdate();
            }
            return nbSubTask;
        }
    }

    // create Y Combinator model data: 3 major tasks, 30 minor tasks a day
    /*class YCombinatorDataItem extends JMenuItem {

        public YCombinatorDataItem() {
            super("Y Combinator");
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    createYCombinatorData();
                }
            });
        }

        private void createYCombinatorData() {
            new Thread() { // This new thread is necessary for updating the progress bar
                @Override
                public void run() {
                    if (!WaitCursor.isStarted()) {
                        // Start wait cursor
                        WaitCursor.startWaitCursor();
                        // Disable item menu
                        setEnabled(false);
                        // Set progress bar
                        MainPanel.progressBar.setVisible(true);
                        MainPanel.progressBar.getBar().setValue(0);
                        MainPanel.progressBar.getBar().setMaximum(4);
                        int todoListValue = 0;
                        boolean withSubtask = false;
                        final StringBuilder progressText = new StringBuilder();
                        for (int i = 1; i <= 4; i++) {
                            if (!MainPanel.progressBar.isStopped()) {
                                final Activity a = new Activity(
                                        "", // place
                                        "", // author
                                        (i == 4 ? "Minor Tasks" : "Major Task " + i), // name
                                        "", // description
                                        "", // type
                                        0, // up to the user to set the estimation
                                        new Date()); // today
                                if (i == 4) {
                                    withSubtask = true;
                                }
                                // Tasks for the ToDo list
                                ToDoList.getList().add(a);
                                if (withSubtask) { // Adding subtasks                                            
                                    addYCombinatorSubTasks(a);
                                }
                                Main.gui.getToDoPanel().getMainTable().insertRow(a); // main table !
                                todoListValue++;
                                final int progressValue = i;
                                progressText.setLength(0); // reset string builder
                                progressText.append(Labels.getString((Main.preferences.getAgileMode() ? "Agile." : "") + "ToDoListPanel.ToDo List") + " : ");
                                progressText.append(Integer.toString(todoListValue));
                                if (withSubtask) {
                                    progressText.append(" (" + Integer.toString(30) + ")");
                                }
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainPanel.progressBar.getBar().setValue(progressValue); // % - required to see the progress                                
                                        MainPanel.progressBar.getBar().setString(progressText.toString()); // task
                                    }
                                });
                            }
                        }
                        // Close progress bar
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                MainPanel.progressBar.getBar().setString(Labels.getString("ProgressBar.Done") + " (" + progressText + ")");
                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            sleep(1000); // wait one second before hiding the progress bar
                                        } catch (InterruptedException ex) {
                                            Main.logger.error("", ex);
                                        }
                                        // hide progress bar
                                        MainPanel.progressBar.getBar().setString(null);
                                        MainPanel.progressBar.setVisible(false);
                                        MainPanel.progressBar.setStopped(false);
                                    }
                                }.start();
                            }
                        });
                        // Enable item menu
                        setEnabled(true);
                        // Stop wait cursor
                        WaitCursor.stopWaitCursor();
                    }
                }
            }.start();
        }

        // 30 sutasks representing 30 minor tasks a day
        private void addYCombinatorSubTasks(Activity a) {
            for (int j = 1; j <= 30; j++) {
                Activity sub = new Activity(
                        "", // place
                        "", // author
                        "Minor Task " + j, // name
                        "", // description
                        "", // type
                        0, // estimation
                        new Date()); // today
                sub.setParentId(a.getId());
                sub.setType(Labels.getString("Common.Subtask"));
                ToDoList.getList().add(sub);
            }
        }
    }*/
    // delete all data
    class ResetDataItem extends JMenuItem {

        public ResetDataItem() {
            super(Labels.getString("DataMenu.Clear All Data"));
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String title = Labels.getString("DataMenu.Clear All Data");
                    String message = Labels.getString("DataMenu.Are you sure to delete all data?");
                    int reply = JOptionPane.showConfirmDialog(Main.gui, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, ImageIcons.DIALOG_ICON);
                    if (reply == JOptionPane.YES_OPTION) {
                        ActivitiesDAO.getInstance().deleteAll();
                        Main.updateLists();
                        MainPanel.updateViews();
                        Main.updateComboBoxLists();
                    }
                }
            });
        }
    }
}
