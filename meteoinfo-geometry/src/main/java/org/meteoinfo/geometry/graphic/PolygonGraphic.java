package org.meteoinfo.geometry.graphic;

import org.meteoinfo.common.PointD;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.geometry.shape.PolygonShape;
import org.meteoinfo.geometry.shape.PolylineShape;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.IndexIterator;

import java.util.ArrayList;
import java.util.List;

public class PolygonGraphic extends Graphic {

    private Array xData;
    private Array yData;
    private Array cData;
    private LegendScheme legendScheme;

    /**
     * Constructor
     * @param shape Polygon shape
     * @param legend Polygon legend break
     */
    public PolygonGraphic(PolygonShape shape, PolygonBreak legend) {
        this.shape = shape;
        this.legend = legend;
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     * @param polygonBreak Polygon break
     */
    public PolygonGraphic(Array xData, Array yData, PolygonBreak polygonBreak) {
        this.xData = xData;
        this.yData = yData;

        updateShape();
        if (polygonBreak == null) {
            polygonBreak = new PolygonBreak();
        }
        this.legend = polygonBreak;
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     */
    public PolygonGraphic(Array xData, Array yData) {
        this(xData, yData, new PolygonBreak());
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     * @param cData Color data
     * @param legendScheme Legend scheme
     */
    public PolygonGraphic(Array xData, Array yData, Array cData, LegendScheme legendScheme) {
        this.xData = xData;
        this.yData = yData;
        this.cData = cData;

        updateShapeLegend(legendScheme);
    }

    protected void updateShape() {
        List<PointD> points = new ArrayList<>();
        IndexIterator xIter = this.xData.getIndexIterator();
        IndexIterator yIter = this.yData.getIndexIterator();
        double x, y;
        while (xIter.hasNext() && yIter.hasNext()) {
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
            if (Double.isNaN(x) || Double.isNaN(y)) {
                continue;
            }
            points.add(new PointD(x, y));
        }
        if (this.shape == null) {
            this.shape = new PolygonShape();
        }
        this.shape.setPoints(points);
    }

    protected void updateShapeLegend(LegendScheme legendScheme) {
        this.legendScheme = legendScheme;
        List<PointD> points = new ArrayList<>();
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
            points.add(new PointD(x, y));
            cb = legendScheme.findLegendBreakAlways(c);
            cbc.add(cb);
        }
        if (this.shape == null) {
            this.shape = new PolygonShape();
        }
        if (points.size() >= 2)
            this.shape.setPoints(points);

        this.legend = cbc;
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
