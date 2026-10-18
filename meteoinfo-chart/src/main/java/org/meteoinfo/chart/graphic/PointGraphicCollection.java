package org.meteoinfo.chart.graphic;

import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.legend.ColorBreak;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.legend.LegendType;
import org.meteoinfo.geometry.legend.PointBreak;
import org.meteoinfo.geometry.Point;
import org.meteoinfo.geometry.GeometryType;
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
        this(xData, yData, null, pointBreak);
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     * @param zData Z data
     * @param pointBreak Point break
     */
    public PointGraphicCollection(Array xData, Array yData, Array zData, PointBreak pointBreak) {
        this();
        this.xData = xData;
        this.yData = yData;
        this.zData = zData;
        this.updateGraphics(pointBreak);
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     * @param pointBreak Point break
     */
    public PointGraphicCollection(Array xData, Array yData, List<ColorBreak> cbs) {
        this(xData, yData, null, cbs);
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     * @param zData Z data
     * @param pointBreak Point break
     */
    public PointGraphicCollection(Array xData, Array yData, Array zData, List<ColorBreak> cbs) {
        this();
        this.xData = xData;
        this.yData = yData;
        this.zData = zData;
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
        this(xData, yData, null, cData, ls);
    }

    /**
     * Constructor
     *
     * @param xData X data
     * @param yData Y data
     * @param zData Z data
     * @param cData Color data
     * @param ls Legend scheme
     */
    public PointGraphicCollection(Array xData, Array yData, Array zData, Array cData, LegendScheme ls) {
        this();
        this.xData = xData;
        this.yData = yData;
        this.zData = zData;
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
     * Get z data
     * @return Z data
     */
    public Array getZData() {
        return this.zData;
    }

    /**
     * Set z data
     * @param zData Z data
     */
    public void setZData(Array zData) {
        this.zData = zData;
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
        double x, y, z;
        if (zData == null) {
            while (xIter.hasNext()) {
                x = xIter.getDoubleNext();
                y = yIter.getDoubleNext();
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                Point shape = new Point(new Coordinate(x, y));
                this.add(new PointGraphic(shape, pointBreak));
            }
        } else {
            IndexIterator zIter = this.zData.getIndexIterator();
            while (xIter.hasNext()) {
                x = xIter.getDoubleNext();
                y = yIter.getDoubleNext();
                z = zIter.getDoubleNext();
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                Point shape = new Point(new Coordinate(x, y, z));
                this.add(new PointGraphic(shape, pointBreak));
            }
        }
    }

    protected void updateGraphics(List<ColorBreak> cbs) {
        this.graphics = new ArrayList<>();
        List<Coordinate> points = new ArrayList<>();
        IndexIterator xIter = this.xData.getIndexIterator();
        IndexIterator yIter = this.yData.getIndexIterator();
        double x, y, z;
        if (cbs.size() == this.xData.getSize()) {
            int i = 0;
            if (zData == null) {
                while (xIter.hasNext()) {
                    x = xIter.getDoubleNext();
                    y = yIter.getDoubleNext();
                    if (Double.isNaN(x) || Double.isNaN(y)) {
                        continue;
                    }
                    Point shape = new Point(new Coordinate(x, y));
                    this.add(new PointGraphic(shape, (PointBreak) cbs.get(i)));
                    i += 1;
                }
            } else {
                IndexIterator zIter = this.zData.getIndexIterator();
                while (xIter.hasNext()) {
                    x = xIter.getDoubleNext();
                    y = yIter.getDoubleNext();
                    z = zIter.getDoubleNext();
                    if (Double.isNaN(x) || Double.isNaN(y)) {
                        continue;
                    }
                    Point shape = new Point(new Coordinate(x, y, z));
                    this.add(new PointGraphic(shape, (PointBreak) cbs.get(i)));
                    i += 1;
                }
            }
            LegendScheme ls = new LegendScheme();
            ls.setLegendBreaks(cbs);
            ls.setLegendType(LegendType.UNIQUE_VALUE);
            ls.setShapeType(GeometryType.POINT);
            this.singleLegend = false;
            this.legendScheme = ls;
        } else {
            updateGraphics((PointBreak) cbs.get(0));
        }
    }

    protected void updateGraphics(LegendScheme ls) {
        this.graphics = new ArrayList<Graphic>();
        Point ps;
        double c;
        ColorBreak cb;
        IndexIterator xIter = this.xData.getIndexIterator();
        IndexIterator yIter = this.yData.getIndexIterator();
        IndexIterator cIter = this.cData.getIndexIterator();
        if (ls.getLegendType() == LegendType.UNIQUE_VALUE && this.xData.getSize() == ls.getBreakNum()) {
            int i = 0;
            if (zData == null) {
                while (xIter.hasNext()) {
                    ps = new Point();
                    ps.setCoordinate(new Coordinate(xIter.getDoubleNext(), yIter.getDoubleNext()));
                    c = cIter.getDoubleNext();
                    ps.setValue(c);
                    cb = ls.getLegendBreak(i);
                    this.add(new Graphic(ps, cb));
                    i += 1;
                }
            } else {
                IndexIterator zIter = this.zData.getIndexIterator();
                while (xIter.hasNext()) {
                    ps = new Point();
                    ps.setCoordinate(new Coordinate(xIter.getDoubleNext(), yIter.getDoubleNext(),
                            zIter.getDoubleNext()));
                    c = cIter.getDoubleNext();
                    ps.setValue(c);
                    cb = ls.getLegendBreak(i);
                    this.add(new Graphic(ps, cb));
                    i += 1;
                }
            }
        } else {
            if (zData == null) {
                while (xIter.hasNext()) {
                    ps = new Point();
                    ps.setCoordinate(new Coordinate(xIter.getDoubleNext(), yIter.getDoubleNext()));
                    c = cIter.getDoubleNext();
                    ps.setValue(c);
                    cb = ls.findLegendBreak(c);
                    if (cb != null) {
                        this.add(new Graphic(ps, cb));
                    }
                }
            } else {
                IndexIterator zIter = this.zData.getIndexIterator();
                while (xIter.hasNext()) {
                    ps = new Point();
                    ps.setCoordinate(new Coordinate(xIter.getDoubleNext(), yIter.getDoubleNext(),
                            zIter.getDoubleNext()));
                    c = cIter.getDoubleNext();
                    ps.setValue(c);
                    cb = ls.findLegendBreak(c);
                    if (cb != null) {
                        this.add(new Graphic(ps, cb));
                    }
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

    /**
     * Set x and y data
     * @param xData X data array
     * @param yData Y data array
     * @param zData Z data array
     */
    public void setData(Array xData, Array yData, Array zData) {
        this.xData = xData;
        this.yData = yData;
        this.zData = zData;
        updateShape();
    }
}
