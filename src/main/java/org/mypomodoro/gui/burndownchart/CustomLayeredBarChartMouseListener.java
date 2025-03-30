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
package org.mypomodoro.gui.burndownchart;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.data.category.CategoryDataset;

/**
 * Custom listener for layered bar charts
 *
 */
public class CustomLayeredBarChartMouseListener implements ChartMouseListener {

    private final JFreeChart chart;

    public CustomLayeredBarChartMouseListener(JFreeChart chart) {
        this.chart = chart;
    }

    @Override
    public void chartMouseClicked(ChartMouseEvent event) {
        /*ChartEntity entity = event.getEntity();
         if (entity == null) {
         return;
         }

         // Get entity details
         String tooltip = ((XYItemEntity) entity).getToolTipText();
         XYDataset dataset = ((XYItemEntity) entity).getDataset();
         int seriesIndex = ((XYItemEntity) entity).getSeriesIndex();
         int item = ((XYItemEntity) entity).getItem();

         // You have the dataset the data point belongs to, the index of the series in that dataset of the data point, and the specific item index in the series of the data point.
         XYSeries series = ((XYSeriesCollection) dataset).getSeries(seriesIndex);
         XYDataItem xyItem = series.getDataItem(item);*/
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent event) {
        ChartEntity entity = event.getEntity();
        // Burndown
        CustomLayeredBarRenderer burndownRenderer = (CustomLayeredBarRenderer) chart.getCategoryPlot().getRenderer(4); // burndown plot index = 4
        if (burndownRenderer != null) {
            if (!(entity instanceof CategoryItemEntity)) {
                burndownRenderer.setHighlightedItem(-1, -1);
                return;
            }
            CategoryItemEntity cie = (CategoryItemEntity) entity;
            CategoryDataset dataset = cie.getDataset();
            burndownRenderer.setHighlightedItem(dataset.getRowIndex(cie.getRowKey()), dataset.getColumnIndex(cie.getColumnKey()));
        }
        // Burn-up
        CustomLayeredBarRenderer burnupRenderer = (CustomLayeredBarRenderer) chart.getCategoryPlot().getRenderer(3);  // burnup plot index = 3
        if (burnupRenderer != null) {
            if (!(entity instanceof CategoryItemEntity)) {
                burnupRenderer.setHighlightedItem(-1, -1);
                return;
            }
            CategoryItemEntity cie = (CategoryItemEntity) entity;
            CategoryDataset dataset = cie.getDataset();
            burnupRenderer.setHighlightedItem(dataset.getRowIndex(cie.getRowKey()), dataset.getColumnIndex(cie.getColumnKey()));
        }
    }
}
