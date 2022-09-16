package org.meteoinfo.chart.plot;

import org.meteoinfo.chart.Margin;
import org.meteoinfo.common.Extent;
import org.meteoinfo.data.Dataset;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.ArrayList;

public class SubPlots extends Plot {

    private List<Plot> plots = new ArrayList<>();
    private int rowNum = 1;
    private int colNum = 1;
    private boolean shareX = false;
    private boolean shareY = false;

    /**
     * Constructor
     * @param position Position
     * @param rowNum Row number
     * @param colNum Column number
     * @param shareX If share x axis
     * @param shareY If share y axis
     */
    public SubPlots(Rectangle2D position, int rowNum, int colNum, boolean shareX,
                    boolean shareY) {
        this.outerPosition = outerPosition;
        this.rowNum = rowNum;
        this.colNum = colNum;
        this.shareX = shareX;
        this.shareY = shareY;
    }

    /**
     * Get plots
     * @return Plots
     */
    public List<Plot> getPlots() {
        return this.plots;
    }

    /**
     * Set plots
     * @param value The plots
     */
    public void setPlots(List<Plot> value) {
        this.plots = value;
    }

    /**
     * Get row number
     * @return Row number
     */
    public int getRowNum() {
        return this.rowNum;
    }

    /**
     * Set row number
     * @param value Row number
     */
    public void setRowNum(int value) {
        this.rowNum = value;
    }

    /**
     * Get column number
     * @return Column number
     */
    public int getColNum() {
        return this.colNum;
    }

    /**
     * Set column number
     * @param value Column number
     */
    public void setColNum(int value) {
        this.colNum = value;
    }

    /**
     * If share x axis
     * @return Share x axis or not
     */
    public boolean isShareX() {
        return this.shareX;
    }

    /**
     * Set if share x axis
     * @param value Share x axis or not
     */
    public void setShareX(boolean value) {
        this.shareX = value;
    }

    /**
     * If share y axis
     * @return Share y axis or not
     */
    public boolean isShareY() {
        return this.shareY;
    }

    /**
     * Set if share y axis
     * @param value Share y axis or not
     */
    public void setShareY(boolean value) {
        this.shareY = value;
    }

    /**
     * Add a plot
     * @param plot The plot
     */
    public void addPlot(Plot plot) {
        this.plots.add(plot);
    }

    /**
     * Get a plot
     * @param row Row index
     * @param col Column index
     * @return
     */
    public Plot getPlot(int row, int col) {
        return this.plots.get(row * this.colNum + col);
    }

    @Override
    public Rectangle2D getOuterPositionArea(Rectangle2D area) {
        Rectangle2D rect = this.getOuterPosition();
        double x = area.getWidth() * rect.getX() + area.getX();
        double y = area.getHeight() * (1 - rect.getHeight() - rect.getY()) + area.getY();
        double w = area.getWidth() * rect.getWidth();
        double h = area.getHeight() * rect.getHeight();
        return new Rectangle2D.Double(x, y, w, h);
    }

    @Override
    public Dataset getDataset() {
        return null;
    }

    @Override
    public void setDataset(Dataset dataset) {

    }

    @Override
    public PlotType getPlotType() {
        return null;
    }

    @Override
    public void draw(Graphics2D g2, Rectangle2D area) {

    }

    @Override
    public Margin getTightInset(Graphics2D g, Rectangle2D positionArea) {
        return null;
    }

    @Override
    public Extent getExtent() {
        return null;
    }

    @Override
    public void setDrawExtent(Extent extent) {

    }
}
