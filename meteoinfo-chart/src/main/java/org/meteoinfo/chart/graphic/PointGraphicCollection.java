package org.meteoinfo.chart.graphic;

import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.legend.ColorBreak;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.legend.LegendType;
import org.meteoinfo.geometry.legend.PointBreak;
import org.meteoinfo.geometry.shape.PointShape;
import org.meteoinfo.geometry.shape.ShapeTypes;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.IndexIterator;

import java.util.ArrayList;
import java.util.List;

public class PointGraphicCollection extends GraphicCollection {
    private Array xData;
    private Array yData;
    private Array zData;
    private Array cData;

    /**
     * Constructor
     */
    public PointGraphicCollection() {
        super();
        this.graphics = new ArrayList<PointGraphic>();
    }

    /**
     * Constructor
     * @param graphics Graphics
     */
    public PointGraphicCollection(List<PointGraphic> graphics) {
        this();
        this.graphics = graphics;
        this.legendBreak = new PointBreak();
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     * @param pointBreak Point break
     */
    public PointGraphicCollection(Array xData, Array yData, PointBreak pointBreak) {
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
    public PointGraphicCollection(Array xData, Array yData, List<ColorBreak> cbs) {
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
    public PointGraphicCollection(Array xData, Array yData, Array cData, LegendScheme ls) {
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
        if (this.legendScheme != null) {
            if (this.hasColorData()) {
                updateGraphics(this.legendScheme);
            } else {
                updateGraphics(this.legendScheme.getLegendBreaks());
            }
        } else {
            updateGraphics((PointBreak) this.legendBreak);
        }
    }

    protected void updateGraphics() {
        updateGraphics((PointBreak) this.legendBreak);
    }

    protected void updateGraphics(PointBreak pointBreak) {
        this.legendBreak = pointBreak;
        this.graphics = new ArrayList<>();
        List<Coordinate> points = new ArrayList<>();
        IndexIterator xIter = this.xData.getIndexIterator();
        IndexIterator yIter = this.yData.getIndexIterator();
        double x, y;
        while (xIter.hasNext()) {
            x = xIter.getDoubleNext();
            y = yIter.getDoubleNext();
            if (Double.isNaN(x) || Double.isNaN(y)) {
                continue;
            }
            PointShape shape = new PointShape(new Coordinate(x, y));
            this.add(new PointGraphic(shape, pointBreak));
        }
    }

    protected void updateGraphics(List<ColorBreak> cbs) {
        this.graphics = new ArrayList<>();
        List<Coordinate> points = new ArrayList<>();
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
                PointShape shape = new PointShape(new Coordinate(x, y));
                this.add(new PointGraphic(shape, (PointBreak) cbs.get(i)));
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
                ps.setPoint(new Coordinate(xIter.getDoubleNext(), yIter.getDoubleNext()));
                z = zIter.getDoubleNext();
                ps.setValue(z);
                cb = ls.getLegendBreak(i);
                this.add(new Graphic(ps, cb));
                i += 1;
            }
        } else {
            while (xIter.hasNext()) {
                ps = new PointShape();
                ps.setPoint(new Coordinate(xIter.getDoubleNext(), yIter.getDoubleNext()));
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

    /**
     * Set x and y data
     * @param xData X data array
     * @param yData Y data array
     */
    public void setData(Array xData, Array yData) {
        this.xData = xData;
        this.yData = yData;
        updateShape();
    }
}
