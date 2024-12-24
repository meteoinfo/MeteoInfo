/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.axis;

import org.meteoinfo.chart.ChartText;
import org.meteoinfo.chart.Location;
import org.meteoinfo.chart.plot.AbstractPlot2D;
import org.meteoinfo.chart.geo.MapGridLine;
import org.meteoinfo.common.*;
import org.meteoinfo.render.java2d.Draw;
import org.meteoinfo.projection.ProjectionInfo;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 *
 * @author Yaqiang Wang
 */
public class ProjLonLatAxis extends LonLatAxis{
    // <editor-fold desc="Variables">
    protected MapGridLine mapGridLine;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     * @param label Label
     * @param isX Is x/longitude axis or not
     * @param mapGridLine MapGridLine object
     */
    public ProjLonLatAxis(String label, boolean isX, MapGridLine mapGridLine){
        super(label, isX);

        this.mapGridLine = mapGridLine;
    }

    /**
     * Constructor
     * @param axis The LonLatAxis object
     * @param mapGridLine MapGridLine object
     */
    public ProjLonLatAxis(LonLatAxis axis,  MapGridLine mapGridLine) {
        super(axis);

        this.drawDegreeSymbol = axis.drawDegreeSymbol;
        this.degreeSpace = axis.degreeSpace;

        this.mapGridLine = mapGridLine;
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get projection
     * @return Projection
     */
    public ProjectionInfo getProjInfo(){
        return this.mapGridLine.getProjInfo();
    }

    /**
     * Get map grid line
     * @return Map grid line
     */
    public MapGridLine getMapGridLine() {
        return mapGridLine;
    }

    /**
     * Set map grid line
     * @param mapGridLine Map grid line
     */
    public void setMapGridLine(MapGridLine mapGridLine) {
        this.mapGridLine = mapGridLine;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Update label gap
     *
     * @param g Graphics2D
     * @param len Length
     * @param labels Labels
     * @return Label gap
     */
    public int getLabelGap(Graphics2D g, double len, List<GridLabel> labels) {
        int n = labels.size();
        int labLen = this.getMaxLabelLength(g, labels);
        int nn = (int) ((len * 0.8) / labLen);
        if (nn == 0) {
            nn = 1;
        }
        return n / nn + 1;
    }

    /**
     * Get maximum label string length
     *
     * @param g Graphics2D
     * @param labels Labels
     * @return Maximum label string length
     */
    public int getMaxLabelLength(Graphics2D g, List<GridLabel> labels) {
        int max = 0;
        Dimension dim;
        int width, height;
        g.setFont(this.tickLabelFont);
        for (int i = 0; i < labels.size(); i++) {
            GridLabel lab = labels.get(i);
            dim = Draw.getStringDimension(lab.getLabString(), g);
            width = dim.width;
            if (this.tickLabelAngle != 0) {
                width = (int) (dim.getWidth() * Math.cos(this.tickLabelAngle * Math.PI / 180));
                height = dim.height;
                width = Math.max(width, height);
            }
            if (max < width) {
                max = width;
            }
        }

        return max;
    }

    @Override
    void drawXAxis(Graphics2D g, Rectangle2D area, AbstractPlot2D plot) {
        double xMin = area.getX();
        double xMax = area.getX() + area.getWidth();
        double yMin = area.getY();
        double yMax = area.getY() + area.getHeight();

        //Draw X axis
        //Axis line
        g.setColor(this.getLineColor());
        g.setStroke(new BasicStroke(this.getLineWidth()));
        g.draw(new Line2D.Double(xMin, yMax, xMax, yMax));

        //Longitude axis ticks
        double x, y;
        double[] xy;
        if (this.isDrawTickLine()) {
            List<GridLabel> lonLabels = mapGridLine.getLongitudeLabels();
            int labelGap = this.getLabelGap(g, xMax - xMin, lonLabels);
            g.setColor(this.getTickColor());
            g.setStroke(new BasicStroke(this.getTickWidth()));
            int i = 0, idx = 0;
            for (GridLabel gridLabel : lonLabels) {
                PointD point = gridLabel.getCoord();
                x = point.X;
                if (x < plot.getDrawExtent().minX || x > plot.getDrawExtent().maxX) {
                    continue;
                }

                //Draw tick line
                x = plot.projToScreenX(x, area) + xMin;
                if (this.getLocation() == Location.BOTTOM) {
                    if (this.isInsideTick()) {
                        g.draw(new Line2D.Double(x, yMax, x, yMax - this.getTickLength()));
                    } else {
                        g.draw(new Line2D.Double(x, yMax, x, yMax + this.getTickLength()));
                    }
                } else {
                    if (this.isInsideTick()) {
                        g.draw(new Line2D.Double(x, yMin, x, yMin + this.getTickLength()));
                    } else {
                        g.draw(new Line2D.Double(x, yMin, x, yMin - this.getTickLength()));
                    }
                }

                //Draw tick label
                if (this.isDrawTickLabel()) {
                    if (i == idx) {
                        idx += labelGap;
                        if (this.getLocation() == Location.BOTTOM) {
                            if (this.isInsideTick()) {
                                y = yMax;
                            } else {
                                y = yMax + this.getTickLength();
                            }
                            y += this.getTickSpace();
                        } else {
                            if (this.isInsideTick()) {
                                y = yMin;
                            } else {
                                y = yMin - this.getTickLength();
                            }
                            y -= this.getTickSpace();
                        }
                        g.setColor(this.getTickLabelColor());
                        g.setFont(this.getTickLabelFont());
                        Draw.drawString(g, x, y, gridLabel.getLabString(), XAlign.CENTER, YAlign.TOP, true);
                    }
                }
                i += 1;
            }
        }
    }

    @Override
    void drawYAxis(Graphics2D g, Rectangle2D area, AbstractPlot2D plot) {
        double xMin = area.getX();
        double xMax = area.getX() + area.getWidth();
        double yMin = area.getY();
        double yMax = area.getY() + area.getHeight();

        //Y axis
        //Axis line
        g.setColor(this.getLineColor());
        g.setStroke(new BasicStroke(this.getLineWidth()));
        g.draw(new Line2D.Double(xMin, yMin, xMin, yMax));

        //Latitude axis ticks
        double x, y;
        double[] xy;
        if (this.isDrawTickLine()) {
            List<GridLabel> latLabels = mapGridLine.getLatitudeLabels();
            g.setColor(this.getTickColor());
            g.setStroke(new BasicStroke(this.getTickWidth()));
            for (GridLabel gridLabel : latLabels) {
                PointD point = gridLabel.getCoord();
                y = point.Y;
                if (y < plot.getDrawExtent().minY || y > plot.getDrawExtent().maxY) {
                    continue;
                }

                //Draw tick line
                y = plot.projToScreenY(y, area) + yMin;
                if (this.getLocation() == Location.LEFT) {
                    if (this.isInsideTick()) {
                        g.draw(new Line2D.Double(xMin, y, xMin + this.getTickLength(), y));
                    } else {
                        g.draw(new Line2D.Double(xMin, y, xMin - this.getTickLength(), y));
                    }
                } else {
                    if (this.isInsideTick()) {
                        g.draw(new Line2D.Double(xMax, y, xMax - this.getTickLength(), y));
                    } else {
                        g.draw(new Line2D.Double(xMax, y, xMax + this.getTickLength(), y));
                    }
                }

                //Draw tick label
                if (this.isDrawTickLabel()) {
                    if (this.getLocation() == Location.LEFT) {
                        if (this.isInsideTick()){
                            x = xMin;
                        } else {
                            x = xMin - this.getTickLength();
                        }
                        x -= this.getTickSpace();
                    } else {
                        if (this.isInsideTick()){
                            x = xMax;
                        } else {
                            x = xMax + this.getTickLength();
                        }
                        x += this.getTickSpace();
                    }
                    g.setColor(this.getTickLabelColor());
                    g.setFont(this.getTickLabelFont());
                    Draw.drawString(g, x, y, gridLabel.getLabString(), XAlign.RIGHT, YAlign.CENTER, true);
                }
            }
        }
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        ProjLonLatAxis axis = (ProjLonLatAxis) super.clone();
        axis.mapGridLine = this.mapGridLine;
        return axis;
    }
    // </editor-fold>
}
