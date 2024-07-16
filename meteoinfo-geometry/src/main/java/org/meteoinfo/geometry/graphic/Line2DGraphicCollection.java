package org.meteoinfo.geometry.graphic;

import org.meteoinfo.common.PointD;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.legend.PolylineBreak;
import org.meteoinfo.geometry.shape.CurveLineShape;
import org.meteoinfo.geometry.shape.PolylineShape;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Index2D;
import org.meteoinfo.ndarray.IndexIterator;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.util.ArrayList;
import java.util.List;

public class Line2DGraphicCollection extends GraphicCollection {
    private Array cData;
    private List<Array> data;
    private boolean curve = false;

    /**
     * Constructor
     */
    public Line2DGraphicCollection() {
        this(new ArrayList<Line2DGraphic>());
    }

    /**
     * Constructor
     * @param graphics Graphics
     */
    public Line2DGraphicCollection(List<Line2DGraphic> graphics) {
        super();
        this.graphics = graphics;
        this.legend = new PolylineBreak();
    }

    /**
     * Constructor
     * @param data Data list
     * @param lineBreak Polyline break
     */
    public Line2DGraphicCollection(List<Array> data, PolylineBreak lineBreak) {
        super();

        updateGraphics(data, lineBreak);
    }

    /**
     * Constructor
     * @param data Data list
     * @param lineBreaks Polyline break list
     */
    public Line2DGraphicCollection(List<Array> data, List<PolylineBreak> lineBreaks) {
        super();

        updateGraphics(data, lineBreaks);
    }

    /**
     * Constructor
     * @param data Data list
     * @param cdata Color data
     * @param ls Legend scheme
     */
    public Line2DGraphicCollection(List<Array> data, Array cData, LegendScheme ls) {
        this.legendScheme = ls;
        this.setSingleLegend(false);
        if (cData.getSize() == data.size()) {
            List<PolylineBreak> lineBreaks = new ArrayList<>();
            IndexIterator iterC = cData.getIndexIterator();
            while (iterC.hasNext()) {
                lineBreaks.add((PolylineBreak) ls.findLegendBreak(iterC.getDoubleNext()));
            }
            updateGraphics(data, lineBreaks);
        } else {
            updateGraphics(data, cData, ls);
        }
    }

    /**
     * Constructor
     * @param data Data list
     * @param cdata Color data
     * @param ls Legend scheme
     */
    public Line2DGraphicCollection(List<Array> data, List<Array> cData, LegendScheme ls) {
        this.legendScheme = ls;
        this.setSingleLegend(false);
        updateGraphics(data, cData, ls);
    }

    protected void updateGraphics(List<Array> data, PolylineBreak lineBreak) {
        this.data = data;
        this.graphics = new ArrayList<>();
        int[] origin = new int[2];
        int[] shape = new int[2];
        Array x, y;
        try {
            for (Array array : data) {
                origin = new int[]{0, 0};
                shape = new int[]{array.getShape()[0], 1};
                x = array.section(origin, shape);
                origin = new int[]{0, 1};
                y = array.section(origin, shape);
                this.add(new Line2DGraphic(x, y, lineBreak));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updateGraphics(List<Array> data, List<PolylineBreak> lineBreaks) {
        this.data = data;
        this.graphics = new ArrayList<>();
        int[] origin = new int[2];
        int[] shape = new int[2];
        Array x, y;
        try {
            int i = 0;
            for (Array array : data) {
                origin = new int[]{0, 0};
                shape = new int[]{array.getShape()[0], 1};
                x = array.section(origin, shape);
                origin = new int[]{0, 1};
                y = array.section(origin, shape);
                if (i >= lineBreaks.size()) {
                    i = 0;
                }
                this.add(new Line2DGraphic(x, y, lineBreaks.get(i)));
                i += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updateGraphics(List<Array> data, List<Array> cData, LegendScheme ls) {
        this.data = data;
        this.graphics = new ArrayList<>();
        int[] origin = new int[2];
        int[] shape = new int[2];
        Array x, y;
        try {
            int i = 0;
            for (Array array : data) {
                origin = new int[]{0, 0};
                shape = new int[]{array.getShape()[0], 1};
                x = array.section(origin, shape);
                origin = new int[]{0, 1};
                y = array.section(origin, shape);
                this.add(new Line2DGraphic(x, y, cData.get(i), ls));
                i += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updateGraphics(List<Array> data, Array cData, LegendScheme ls) {
        this.data = data;
        this.cData = cData;
        this.graphics = new ArrayList<>();
        int[] origin = new int[2];
        int[] shape = new int[2];
        Array x, y, c;
        try {
            int i = 0;
            for (Array array : data) {
                origin = new int[]{0, 0};
                shape = new int[]{array.getShape()[0], 1};
                x = array.section(origin, shape);
                origin = new int[]{0, 1};
                y = array.section(origin, shape);
                origin = new int[]{0, i};
                c = cData.section(origin, shape);
                this.add(new Line2DGraphic(x, y, c, ls));
                i += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
     * Get segments data
     * @return Segments data
     */
    public List<Array> getData() {
        return this.data;
    }

    /**
     * Set segments data
     * @param value Segments data
     */
    public void setData(List<Array> value) {
        if (this.cData != null) {
            updateGraphics(value, this.cData, this.legendScheme);
        } else {
            List<PolylineBreak> lineBreaks = getLegendBreaks();
            updateGraphics(value, lineBreaks);
        }
    }

    /**
     * Set segments data
     * @param value Segments data
     */
    public void setData(List<Array> value, Array cData) {
        updateGraphics(value, cData, this.legendScheme);
    }

    /**
     * Get legend breaks
     * @return Legend breaks
     */
    public List<PolylineBreak> getLegendBreaks() {
        List<PolylineBreak> lineBreaks = new ArrayList<>();
        for (Line2DGraphic graphic : (List<Line2DGraphic>) this.getGraphics()) {
            lineBreaks.add((PolylineBreak) graphic.legend);
        }
        return lineBreaks;
    }
}
