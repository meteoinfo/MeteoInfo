package org.meteoinfo.geometry.graphic;

import org.meteoinfo.common.PointD;
import org.meteoinfo.geometry.legend.ColorBreak;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.legend.LegendType;
import org.meteoinfo.geometry.legend.PointBreak;
import org.meteoinfo.geometry.shape.PointShape;
import org.meteoinfo.geometry.shape.PolylineShape;
import org.meteoinfo.geometry.shape.Shape;
import org.meteoinfo.geometry.shape.ShapeTypes;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.IndexIterator;

import java.util.ArrayList;
import java.util.List;

public class Point2DGraphicCollection extends GraphicCollection {
    private Array xData;
    private Array yData;
    private Array cData;

    /**
     * Constructor
     */
    public Point2DGraphicCollection() {
        super();
        this.graphics = new ArrayList<Point2DGraphic>();
        this.legend = new PointBreak();
    }

    /**
     * Constructor
     * @param graphics Graphics
     */
    public Point2DGraphicCollection(List<Point2DGraphic> graphics) {
        this();
        this.graphics = graphics;
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     * @param pointBreak Point break
     */
    public Point2DGraphicCollection(Array xData, Array yData, PointBreak pointBreak) {
        this();
        this.xData = xData;
        this.yData = yData;
        this.updateGraphics(pointBreak);
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     * @param pointBreak Point break
     */
    public Point2DGraphicCollection(Array xData, Array yData, List<ColorBreak> cbs) {
        this();
        this.xData = xData;
        this.yData = yData;
        this.updateGraphics(cbs);
    }

    /**
     * Constructor
     *
     * @param xData X data
     * @param yData Y data
     * @param cData Color data
     * @param ls Legend scheme
     */
    public Point2DGraphicCollection(Array xData, Array yData, Array cData, LegendScheme ls) {
        this();
        this.xData = xData;
        this.yData = yData;
        this.cData = cData;
        this.updateGraphics(ls);
    }

    /**
     * Return has color data array or not
     *
     * @return Has color data array of not
     */
    public boolean hasColorData() {
        return this.cData != null;
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
     * Get color data
     * @return Color data
     */
    public Array getColorData() {
        return this.cData;
    }

    protected void updateShape() {
        if (this.hasColorData()) {
            updateGraphics(this.legendScheme);
        } else {
            updateGraphics((PointBreak) this.legend);
        }
    }

    protected void updateGraphics() {
        updateGraphics((PointBreak) this.legend);
    }

    protected void updateGraphics(PointBreak pointBreak) {
        this.legend = pointBreak;
        this.graphics = new ArrayList<>();
        List<PointD> points = new ArrayList<>();
        IndexIterator xIter = this.xData.getIndexIterator();
        IndexIterator yIter = this.yData.getIndexIterator();
        double x, y;
        while (xIter.hasNext()) {
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
            if (Double.isNaN(x) || Double.isNaN(y)) {
                continue;
            }
            PointShape shape = new PointShape(new PointD(x, y));
            this.add(new Point2DGraphic(shape, pointBreak));
        }
    }

    protected void updateGraphics(List<ColorBreak> cbs) {
        this.graphics = new ArrayList<>();
        List<PointD> points = new ArrayList<>();
        IndexIterator xIter = this.xData.getIndexIterator();
        IndexIterator yIter = this.yData.getIndexIterator();
        double x, y;
        if (cbs.size() == this.xData.getSize()) {
            int i = 0;
            while (xIter.hasNext()) {
                x = xIter.getDoubleNext();
                y = yIter.getDoubleNext();
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                PointShape shape = new PointShape(new PointD(x, y));
                this.add(new Point2DGraphic(shape, (PointBreak) cbs.get(i)));
                i += 1;
            }
            LegendScheme ls = new LegendScheme();
            ls.setLegendBreaks(cbs);
            ls.setLegendType(LegendType.UNIQUE_VALUE);
            ls.setShapeType(ShapeTypes.POINT);
            this.singleLegend = false;
            this.legendScheme = ls;
        } else {
            updateGraphics((PointBreak) cbs.get(0));
        }
    }

    protected void updateGraphics(LegendScheme ls) {
        this.graphics = new ArrayList<Graphic>();
        PointShape ps;
        double z;
        ColorBreak cb;
        IndexIterator xIter = this.xData.getIndexIterator();
        IndexIterator yIter = this.yData.getIndexIterator();
        IndexIterator zIter = this.cData.getIndexIterator();
        if (ls.getLegendType() == LegendType.UNIQUE_VALUE && this.xData.getSize() == ls.getBreakNum()) {
            int i = 0;
            while (xIter.hasNext()) {
                ps = new PointShape();
                ps.setPoint(new PointD(xIter.getDoubleNext(), yIter.getDoubleNext()));
                z = zIter.getDoubleNext();
                ps.setValue(z);
                cb = ls.getLegendBreak(i);
                this.add(new Graphic(ps, cb));
                i += 1;
            }
        } else {
            while (xIter.hasNext()) {
                ps = new PointShape();
                ps.setPoint(new PointD(xIter.getDoubleNext(), yIter.getDoubleNext()));
                z = zIter.getDoubleNext();
                ps.setValue(z);
                cb = ls.findLegendBreak(z);
                if (cb != null) {
                    this.add(new Graphic(ps, cb));
                }
            }
        }
        this.singleLegend = false;
        this.legendScheme = ls;
    }
}
