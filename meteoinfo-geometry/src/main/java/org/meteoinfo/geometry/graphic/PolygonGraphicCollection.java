package org.meteoinfo.geometry.graphic;

import org.meteoinfo.common.PointD;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.legend.PolygonBreak;
import org.meteoinfo.geometry.legend.PolylineBreak;
import org.meteoinfo.geometry.shape.PolygonShape;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.IndexIterator;

import java.util.ArrayList;
import java.util.List;

public class PolygonGraphicCollection extends GraphicCollection {

    private Array cData;
    private List<Array> data;

    /**
     * Constructor
     */
    public PolygonGraphicCollection() {
        this(new ArrayList<PolygonGraphic>());
    }

    /**
     * Constructor
     * @param graphics Graphics
     */
    public PolygonGraphicCollection(List<PolygonGraphic> graphics) {
        super();
        this.graphics = graphics;
        this.legend = new PolygonBreak();
    }

    /**
     * Constructor
     * @param data Data list
     * @param lineBreak Polyline break
     */
    public PolygonGraphicCollection(List<Array> data, PolygonBreak polygonBreak) {
        super();

        updateGraphics(data, polygonBreak);
    }

    /**
     * Constructor
     * @param data Data list
     * @param polygonBreaks Polygon break list
     */
    public PolygonGraphicCollection(List<Array> data, List<PolygonBreak> polygonBreaks) {
        super();

        updateGraphics(data, polygonBreaks);
    }

    /**
     * Constructor
     * @param data Data list
     * @param cdata Color data
     * @param ls Legend scheme
     */
    public PolygonGraphicCollection(List<Array> data, Array cData, LegendScheme ls) {
        this.legendScheme = ls;
        this.setSingleLegend(false);
        if (cData.getSize() == data.size()) {
            List<PolygonBreak> polygonBreaks = new ArrayList<>();
            IndexIterator iterC = cData.getIndexIterator();
            while (iterC.hasNext()) {
                polygonBreaks.add((PolygonBreak) ls.findLegendBreak(iterC.getDoubleNext()));
            }
            updateGraphics(data, polygonBreaks);
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
    public PolygonGraphicCollection(List<Array> data, List<Array> cData, LegendScheme ls) {
        this.legendScheme = ls;
        this.setSingleLegend(false);
        updateGraphics(data, cData, ls);
    }

    protected void updateGraphics(List<Array> data, PolygonBreak polygonBreak) {
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
                this.add(new PolygonGraphic(x, y, polygonBreak));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updateGraphics(List<Array> data, List<PolygonBreak> breaks) {
        this.singleLegend = false;
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
                if (i >= breaks.size()) {
                    i = 0;
                }
                this.add(new PolygonGraphic(x, y, breaks.get(i)));
                i += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updateGraphics(List<Array> data, List<Array> cData, LegendScheme ls) {
        this.singleLegend = false;
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
                this.add(new PolygonGraphic(x, y, cData.get(i), ls));
                i += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updateGraphics(List<Array> data, Array cData, LegendScheme ls) {
        this.singleLegend = false;
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
                this.add(new PolygonGraphic(x, y, c, ls));
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
            List<PolygonBreak> breaks = getLegendBreaks();
            updateGraphics(value, breaks);
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
    public List<PolygonBreak> getLegendBreaks() {
        List<PolygonBreak> breaks = new ArrayList<>();
        for (PolygonGraphic graphic : (List<PolygonGraphic>) this.getGraphics()) {
            breaks.add((PolygonBreak) graphic.legend);
        }
        return breaks;
    }

}
