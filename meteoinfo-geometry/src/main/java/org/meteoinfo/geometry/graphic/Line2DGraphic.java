package org.meteoinfo.geometry.graphic;

import org.meteoinfo.common.PointD;
import org.meteoinfo.geometry.legend.ColorBreak;
import org.meteoinfo.geometry.legend.ColorBreakCollection;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.legend.PolylineBreak;
import org.meteoinfo.geometry.shape.PolylineShape;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.IndexIterator;

import java.util.ArrayList;
import java.util.List;

public class Line2DGraphic extends Graphic {
    private Array xData;
    private Array yData;
    private Array cData;
    private boolean curve = false;

    /**
     * Constructor
     *
     * @param polylineShape Polyline shape
     * @param polylineBreak Polyline break
     */
    public Line2DGraphic(PolylineShape polylineShape, PolylineBreak polylineBreak) {
        this.shape = polylineShape;
        this.legend = polylineBreak;
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     * @param polylineBreak Polyline break
     */
    public Line2DGraphic(Array xData, Array yData, PolylineBreak polylineBreak) {
        this.xData = xData;
        this.yData = yData;

        updateShape();
        if (polylineBreak == null) {
            polylineBreak = new PolylineBreak();
        }
        this.legend = polylineBreak;
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     */
    public Line2DGraphic(Array xData, Array yData) {
        this(xData, yData, new PolylineBreak());
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     * @param cData Color data
     * @param legendScheme Legend scheme
     */
    public Line2DGraphic(Array xData, Array yData, Array cData, LegendScheme legendScheme) {
        this.xData = xData;
        this.yData = yData;
        this.cData = cData;

        updateShapeLegend(legendScheme);
    }

    protected void updateShape() {
        List<PointD> points = new ArrayList<>();
        IndexIterator xIter = this.xData.getIndexIterator();
        IndexIterator yIter = this.yData.getIndexIterator();
        while (xIter.hasNext()) {
            points.add(new PointD(xIter.getDoubleNext(), yIter.getDoubleNext()));
        }
        if (this.shape == null) {
            this.shape = new PolylineShape();
        }
        this.shape.setPoints(points);
    }

    protected void updateShapeLegend(LegendScheme legendScheme) {
        List<PointD> points = new ArrayList<>();
        IndexIterator xIter = this.xData.getIndexIterator();
        IndexIterator yIter = this.yData.getIndexIterator();
        IndexIterator cIter = this.cData.getIndexIterator();
        ColorBreakCollection cbc = new ColorBreakCollection();
        ColorBreak cb;
        double c;
        while (xIter.hasNext()) {
            points.add(new PointD(xIter.getDoubleNext(), yIter.getDoubleNext()));
            c = cIter.getDoubleNext();
            cb = legendScheme.findLegendBreak(c);
            cbc.add(cb);
        }
        if (this.shape == null) {
            this.shape = new PolylineShape();
        }
        this.shape.setPoints(points);

        this.legend = cbc;
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
     * Set x data
     * @param xData X data
     */
    public void setXData(Array xData) {
        this.xData = xData;
        updateShape();
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
     * Set data
     * @param xData X data
     * @param yData Y data
     */
    public void setData(Array xData, Array yData) {
        this.xData = xData;
        this.yData = yData;
        updateShape();
    }
}
