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
package org.mypomodoro.gui.export;

import au.com.bytecode.opencsv.CSVReader;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.lang.Thread.sleep;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderXSDFactory;
import org.mypomodoro.Main;
import org.mypomodoro.buttons.TabPanelButton;
import org.mypomodoro.gui.IListPanel;
import org.mypomodoro.gui.ImageIcons;
import org.mypomodoro.gui.MainPanel;
import org.mypomodoro.gui.reports.ReportsPanel;
import org.mypomodoro.model.Activity;
import org.mypomodoro.util.Labels;
import org.mypomodoro.util.WaitCursor;

/**
 * Panel to import reports
 *
 */
public class ImportPanel extends JPanel {

    protected final ImportInputForm importInputForm = new ImportInputForm();
    private final GridBagConstraints gbc = new GridBagConstraints();
    private final IListPanel panel;

    public ImportPanel(IListPanel panel) {
        this.panel = panel;

        setLayout(new GridBagLayout());
        setBorder(null);

        addImportInputForm();
        addImportButton();
    }

    private void addImportButton() {
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.gridheight = 2;
        // gbc.fill = GridBagConstraints.NONE;
        JButton importButton = new TabPanelButton(
                Labels.getString("ReportListPanel.Import"));
        importButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (importInputForm.getFileName().length() == 0) {
                    importInputForm.initFileName();
                }
                importData();
            }
        });
        add(importButton, gbc);
    }

    private void addImportInputForm() {
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        add(new JScrollPane(importInputForm), gbc);
    }

    private void importData() {
        final String fileName = importInputForm.getFileName(); // path
        if (fileName != null && fileName.length() > 0) {
            new Thread() { // This new thread is necessary for updating the progress bar
                @Override
                public void run() {
                    if (!WaitCursor.isStarted()) {
                        // Start wait cursor
                        WaitCursor.startWaitCursor();
                        try {
                            if (importInputForm.isFileCSVFormat()) {
                                importCSV(fileName);
                            } else if (importInputForm.isFileExcelFormat()) {
                                importExcel(fileName);
                            } else if (importInputForm.isFileExcelOpenXMLFormat()) {
                                importExcelx(fileName);
                            } else if (importInputForm.isFileXMLFormat()) {
                                importXML(fileName);
                            }
                        } catch (Exception ex) {
                            Main.logger.error("Import failed", ex);
                            String title = Labels.getString("Common.Error");
                            String message = Labels.getString("ReportListPanel.Import failed");
                            JOptionPane.showConfirmDialog(Main.gui, message, title,
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, ImageIcons.DIALOG_ICON);
                        } finally {
                            // Stop wait cursor
                            WaitCursor.stopWaitCursor();
                        }
                    }
                }
            }.start();
        }
    }

    private void importCSV(String fileName) throws Exception {
        CSVReader readerCount = new CSVReader(new FileReader(fileName), importInputForm.getSeparator(), '\"', importInputForm.isHeaderSelected() ? 1 : 0);
        final int rowCount = readerCount.readAll().size();
        // Close stream
        readerCount.close();
        if (rowCount > 0) {
            // utf-8 encoding
            FileInputStream fileIn = new FileInputStream(fileName);
            CSVReader reader = new CSVReader(new InputStreamReader(fileIn, "UTF-8"), importInputForm.getSeparator(), '\"', importInputForm.isHeaderSelected() ? 1 : 0);
            // Set progress bar
            MainPanel.progressBar.setVisible(true);
            MainPanel.progressBar.getBar().setValue(0);
            MainPanel.progressBar.getBar().setMaximum(rowCount);
            int increment = 0;
            String[] line;
            while ((line = reader.readNext()) != null) {
                insertData(line);
                increment++;
                final int progressValue = increment;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        MainPanel.progressBar.getBar().setValue(progressValue); // % - required to see the progress
                        MainPanel.progressBar.getBar().setString(Integer.toString(progressValue) + " / " + Integer.toString(rowCount)); // task
                    }
                });
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    MainPanel.progressBar.getBar().setString(Labels.getString("ProgressBar.Done") + " (" + rowCount + ")");
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
                        }
                    }.start();
                }
            });
            // Close stream
            reader.close();
        }
    }

    private void importExcel(String fileName) throws Exception {
        InputStream myxls = new FileInputStream(fileName);
        HSSFWorkbook book = new HSSFWorkbook(myxls);
        FormulaEvaluator eval = book.getCreationHelper().createFormulaEvaluator();
        HSSFSheet sheet = book.getSheetAt(0);
        if (importInputForm.isHeaderSelected()) {
            sheet.removeRow(sheet.getRow(0));
        }
        final int rowCount = sheet.getPhysicalNumberOfRows();
        if (rowCount > 0) {
            // Set progress bar
            MainPanel.progressBar.setVisible(true);
            MainPanel.progressBar.getBar().setValue(0);
            MainPanel.progressBar.getBar().setMaximum(rowCount);
            int increment = 0;
            for (Row row : sheet) {
                String[] line = new String[21];
                int i = 0;
                for (Cell cell : row) {
                    line[i] = getCellContent(cell, eval);
                    i++;
                }
                insertData(line);
                increment++;
                final int progressValue = increment;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        MainPanel.progressBar.getBar().setValue(progressValue); // % - required to see the progress
                        MainPanel.progressBar.getBar().setString(Integer.toString(progressValue) + " / " + Integer.toString(rowCount)); // task
                    }
                });
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    MainPanel.progressBar.getBar().setString(Labels.getString("ProgressBar.Done") + " (" + rowCount + ")");
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
                        }
                    }.start();
                }
            });
        }
        myxls.close();
    }

    private void importExcelx(String fileName) throws Exception {
        InputStream myxlsx = new FileInputStream(fileName);
        XSSFWorkbook book = new XSSFWorkbook(myxlsx);
        FormulaEvaluator eval = book.getCreationHelper().createFormulaEvaluator();
        XSSFSheet sheet = book.getSheetAt(0);
        if (importInputForm.isHeaderSelected()) {
            sheet.removeRow(sheet.getRow(0));
        }
        final int rowCount = sheet.getPhysicalNumberOfRows();
        if (rowCount > 0) {
            // Set progress bar
            MainPanel.progressBar.setVisible(true);
            MainPanel.progressBar.getBar().setValue(0);
            MainPanel.progressBar.getBar().setMaximum(rowCount);
            int increment = 0;
            for (Row row : sheet) {
                String[] line = new String[21];
                int i = 0;
                for (Cell cell : row) {
                    line[i] = getCellContent(cell, eval);
                    i++;
                }
                insertData(line);
                increment++;
                final int progressValue = increment;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        MainPanel.progressBar.getBar().setValue(progressValue); // % - required to see the progress
                        MainPanel.progressBar.getBar().setString(Integer.toString(progressValue) + " / " + Integer.toString(rowCount)); // task
                    }
                });
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    MainPanel.progressBar.getBar().setString(Labels.getString("ProgressBar.Done") + " (" + rowCount + ")");
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
                        }
                    }.start();
                }
            });
        }
        myxlsx.close();
    }

    // http://www.w3.org/TR/xmlschema-0
    private void importXML(String fileName) throws Exception {
        String schemaFile = "/xsd/importSchema.xsd";
        XMLReaderJDOMFactory factory = new XMLReaderXSDFactory(Main.class.getResource(schemaFile));
        SAXBuilder saxBuilder = new SAXBuilder(factory);
        InputStream myxml = new FileInputStream(fileName);
        Document document = saxBuilder.build(new InputStreamReader(myxml, "UTF-8"));
        org.jdom2.Element rootNode = document.getRootElement();
        List<org.jdom2.Element> tasksList = rootNode.getChildren("task");
        final int rowCount = tasksList.size();
        if (rowCount > 0) {
            // Set progress bar
            MainPanel.progressBar.setVisible(true);
            MainPanel.progressBar.getBar().setValue(0);
            MainPanel.progressBar.getBar().setMaximum(rowCount);
            int increment = 0;
            int incrementSubtask = 0;
            for (org.jdom2.Element task : tasksList) {
                Activity newTask = getActivity(getLineFromElement(task));
                panel.getList().add(newTask);
                int parentId = newTask.getId();
                List<org.jdom2.Element> subtasksList = task.getChildren("subtask");
                for (org.jdom2.Element subtask : subtasksList) {
                    Activity newSubtask = getActivity(getLineFromElement(subtask));
                    newSubtask.setParentId(parentId);
                    panel.getList().add(newSubtask);
                    incrementSubtask++;
                }
                // insert task here so it gets selected and this will refresh the title of the sub table
                panel.getMainTable().insertRow(newTask);
                increment++;
                final int progressValue = increment;
                final int progressValueSubTask = incrementSubtask;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String progressText = Integer.toString(progressValue);
                        if (progressValueSubTask > 0) {
                            progressText += " (" + progressValueSubTask + ")";
                        }
                        progressText += " / " + Integer.toString(rowCount);
                        MainPanel.progressBar.getBar().setValue(progressValue); // % - required to see the progress                        
                        MainPanel.progressBar.getBar().setString(progressText); // task
                    }
                });
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    MainPanel.progressBar.getBar().setString(Labels.getString("ProgressBar.Done") + " (" + rowCount + ")");
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
                        }
                    }.start();
                }
            });
        }
    }

    private String[] getLineFromElement(org.jdom2.Element element) throws Exception {
        String[] line = new String[19];
        line[0] = element.getChildText("u");
        line[1] = element.getChildText("date");
        line[2] = element.getChildText("datecompleted");
        line[3] = element.getChildText("title");
        line[4] = element.getChildText("estimate");
        line[5] = element.getChildText("overestimate");
        line[6] = element.getChildText("real");
        line[7] = ""; // diff I - no need
        line[8] = ""; // diff II - no need
        line[9] = element.getChildText("internal");
        line[10] = element.getChildText("internal");
        line[11] = element.getChildText("type");
        line[12] = element.getChildText("author");
        line[13] = element.getChildText("place");
        line[14] = element.getChildText("description");
        line[15] = element.getChildText("comment");
        line[16] = element.getChildText("storypoints");
        line[17] = element.getChildText("iteration");
        line[18] = element.getChildText("priority");
        return line;
    }

    private String getCellContent(Cell cell, FormulaEvaluator eval) {
        String value = "";
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                break;
            case Cell.CELL_TYPE_STRING:
                value = cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    CellValue cellValue = eval.evaluate(cell);
                    value = org.mypomodoro.util.DateUtil.getFormatedDate(DateUtil.getJavaDate(cellValue.getNumberValue(), true), importInputForm.getDatePattern());
                } else if (cell.getCellStyle().getDataFormat() == new XSSFWorkbook().createDataFormat().getFormat(importInputForm.getDatePattern())) {
                    CellValue cellValue = eval.evaluate(cell);
                    value = org.mypomodoro.util.DateUtil.getFormatedDate(DateUtil.getJavaDate(cellValue.getNumberValue(), true), importInputForm.getDatePattern());
                } else if (cell.getCellStyle().getDataFormat() == HSSFDataFormat.getBuiltinFormat("0.0")) {
                    value = (float) cell.getNumericCellValue() + "";
                } else if (cell.getCellStyle().getDataFormat() == new XSSFWorkbook().createDataFormat().getFormat("0.0")) {
                    value = (float) cell.getNumericCellValue() + "";
                } else {
                    value = (int) cell.getNumericCellValue() + "";
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                value = cell.getBooleanCellValue() ? "1" : "0";
                break;
            default:
                value = "";
        }
        return value;
    }

    private Activity getActivity(String[] line) throws Exception {
        Activity newActivity = new Activity(line[13], line[12], line[3], line[14], line[11], Integer.parseInt(line[4]),
                line[1].isEmpty() ? new Date() : org.mypomodoro.util.DateUtil.getDate(line[1], importInputForm.getDatePattern()), Integer.parseInt(line[5]), Integer.parseInt(line[6]),
                Integer.parseInt(line[9]), Integer.parseInt(line[10]), line[15],
                !line[0].equals("0"), panel instanceof ReportsPanel, false);
        try {
            newActivity.setStoryPoints(Float.parseFloat(line[16]));
            newActivity.setIteration(Integer.parseInt(line[17]));
            newActivity.setPriority(Integer.parseInt(line[18]));
        } catch (NumberFormatException ex) {
            Main.logger.error("", ex);
            newActivity.setStoryPoints(0);
            newActivity.setIteration(-1);
            newActivity.setPriority(-1);
        } catch (ArrayIndexOutOfBoundsException ex) {
            Main.logger.error("", ex);
            newActivity.setStoryPoints(0);
            newActivity.setIteration(-1);
            newActivity.setPriority(-1);
        }
        // the dates must be preserved
        if (!line[2].isEmpty()) { // date complete field not empty
            newActivity.setDate(line[1].isEmpty() ? new Date() : org.mypomodoro.util.DateUtil.getDate(line[1], importInputForm.getDatePattern()));
            newActivity.setDateCompleted(org.mypomodoro.util.DateUtil.getDate(line[2], importInputForm.getDatePattern()));
        }
        return newActivity;
    }

    // insert in main table
    private void insertData(String[] line) throws Exception {
        Activity newActivity = getActivity(line);
        if (importInputForm.getTaskBox().isSelected()) {
            panel.getMainTable().importActivity(newActivity);
            panel.getMainTable().insertRow(newActivity);
        } else if (importInputForm.getSubtaskBox().isSelected() && panel.getMainTable().getModel().getRowCount() != 0) {
            newActivity.setParentId(panel.getMainTable().getActivityIdFromSelectedRow());
            panel.getSubTable().importActivity(newActivity);
            panel.getSubTable().insertRow(newActivity);
            // adjust the parent task
            panel.getMainTable().addPomsToSelectedRow(newActivity);
        }
    }
}
