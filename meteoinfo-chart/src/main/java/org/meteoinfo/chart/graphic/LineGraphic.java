package org.meteoinfo.chart.graphic;

import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.legend.ColorBreak;
import org.meteoinfo.geometry.legend.ColorBreakCollection;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.legend.PolylineBreak;
import org.meteoinfo.geometry.shape.PolylineShape;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.IndexIterator;

import java.util.ArrayList;
import java.util.List;

public class LineGraphic extends Graphic {
    private Array xData;
    private Array yData;
    private Array cData;
    private boolean curve = false;
    private LegendScheme legendScheme;

    /**
     * Constructor
     *
     * @param polylineShape Polyline shape
     * @param polylineBreak Polyline break
     */
    public LineGraphic(PolylineShape polylineShape, PolylineBreak polylineBreak) {
        this.shape = polylineShape;
        this.legendBreak = polylineBreak;
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     * @param polylineBreak Polyline break
     */
    public LineGraphic(Array xData, Array yData, PolylineBreak polylineBreak) {
        this.xData = xData;
        this.yData = yData;

        updateShape();
        if (polylineBreak == null) {
            polylineBreak = new PolylineBreak();
        }
        this.legendBreak = polylineBreak;
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     */
    public LineGraphic(Array xData, Array yData) {
        this(xData, yData, new PolylineBreak());
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     * @param cData Color data
     * @param legendScheme Legend scheme
     */
    public LineGraphic(Array xData, Array yData, Array cData, LegendScheme legendScheme) {
        this.xData = xData;
        this.yData = yData;
        this.cData = cData;

        updateShapeLegend(legendScheme);
    }

    protected void updateShape() {
        List<Coordinate> points = new ArrayList<>();
        IndexIterator xIter = this.xData.getIndexIterator();
        IndexIterator yIter = this.yData.getIndexIterator();
        double x, y;
        while (xIter.hasNext() && yIter.hasNext()) {
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
            if (Double.isNaN(x) || Double.isNaN(y)) {
                continue;
            }
            points.add(new Coordinate(x, y));
        }
        if (this.shape == null) {
            this.shape = new PolylineShape();
        }
        this.shape.setPoints(points);
    }

    protected void updateShapeLegend(LegendScheme legendScheme) {
        this.legendScheme = legendScheme;
        List<Coordinate> points = new ArrayList<>();
        IndexIterator xIter = this.xData.getIndexIterator();
        IndexIterator yIter = this.yData.getIndexIterator();
        IndexIterator cIter = this.cData.getIndexIterator();
        ColorBreakCollection cbc = new ColorBreakCollection();
        ColorBreak cb;
        double x, y, c;
        while (xIter.hasNext()) {
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
            c = cIter.getDoubleNext();
            if (Double.isNaN(x) || Double.isNaN(y)) {
                continue;
            }
            points.add(new Coordinate(x, y));
            cb = legendScheme.findLegendBreakAlways(c);
            cbc.add(cb);
        }
        if (this.shape == null) {
            this.shape = new PolylineShape();
        }
        if (points.size() >= 2)
            this.shape.setPoints(points);

        this.legendBreak = cbc;
    }

    protected void updateShapeLegend(List<ColorBreak> cbs) {
        this.legendScheme = new LegendScheme(cbs);
        List<Coordinate> points = new ArrayList<>();
        IndexIterator xIter = this.xData.getIndexIterator();
        IndexIterator yIter = this.yData.getIndexIterator();
        ColorBreakCollection cbc = new ColorBreakCollection();
        ColorBreak cb;
        double x, y, c;
        int i = 0;
        while (xIter.hasNext()) {
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
            cb = cbs.get(i);
            if (Double.isNaN(x) || Double.isNaN(y)) {
                continue;
            }
            points.add(new Coordinate(x, y));
            cbc.add(cb);
            i += 1;
        }
        if (this.shape == null) {
            this.shape = new PolylineShape();
        }
        this.shape.setPoints(points);

        this.legendBreak = cbc;
    }

    /**
     * Return plot as curve line or not
     * @return Curve line or not
     */
    public boolean isCurve() {
        return this.curve;
    }

    /**
     * Set plot as curve line or not
     * @param value Curve line or not
     */
    public void setCurve(boolean value) {
        this.curve = value;
    }

    /**
     * Get x data
     * @return X data
     */
    public Array getXData() {
        return this.xData;
    }

    /**
     * Set x data
     * @param xData X data
     */
    public void setXData(Array xData) {
        this.xData = xData;
        updateShape();
    }

    /**
     * Get y data
     * @return Y data
     */
    public Array getYData() {
        return this.yData;
    }

    /**
     * Set y data
     * @param yData Y data
     */
    public void setYData(Array yData) {
        this.yData = yData;
        updateShape();
    }

    /**
     * Get color data array
     * @return Color data array
     */
    public Array getColorData() {
        return this.cData;
    }

    /**
     * Set data
     * @param xData X data
     * @param yData Y data
     */
    public void setData(Array xData, Array yData) {
        this.xData = xData;
        this.yData = yData;
        updateShape();
    }

    /**
     * Get legend scheme
     * @return Legend scheme
     */
    public LegendScheme getLegendScheme() {
        return this.legendScheme;
    }
}
