package org.meteoinfo.chart.graphic;

import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.LineString;
import org.meteoinfo.geometry.MultiLineString;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.IndexIterator;

import java.util.ArrayList;
import java.util.List;

public class MultiLineGraphicCollection extends GraphicCollection implements LinealGraphic {
    private Array cData;
    private List<Array> data;
    private boolean curve = false;
    private float[] vertexPosition;
    private float[] vertexColor;
    private List<Integer> linePointNumbers;

    /**
     * Constructor
     */
    public MultiLineGraphicCollection() {
        super();
        this.graphics = new ArrayList<LineGraphic>();
    }

    /**
     * Constructor
     * @param graphics Graphics
     */
    public MultiLineGraphicCollection(List<LineGraphic> graphics) {
        super();
        this.graphics = graphics;
        this.legendBreak = new PolylineBreak();
    }

    /**
     * Constructor
     * @param data Data list
     * @param lineBreak Polyline break
     */
    public MultiLineGraphicCollection(List<Array> data, PolylineBreak lineBreak) {
        super();

        updateGraphics(data, lineBreak);
    }

    /**
     * Constructor
     * @param data Data list
     * @param lineBreaks Polyline break list
     */
    public MultiLineGraphicCollection(List<Array> data, List<PolylineBreak> lineBreaks) {
        super();

        updateGraphics(data, lineBreaks);
    }

    /**
     * Constructor
     * @param data Data list
     * @param cdata Color data
     * @param ls Legend scheme
     */
    public MultiLineGraphicCollection(List<Array> data, Array cData, LegendScheme ls) {
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
    public MultiLineGraphicCollection(List<Array> data, List<Array> cData, LegendScheme ls) {
        this.legendScheme = ls;
        this.setSingleLegend(false);
        updateGraphics(data, cData, ls);
    }

    protected void updateGraphics(List<Array> data, PolylineBreak lineBreak) {
        this.data = data;
        this.graphics = new ArrayList<>();
        int[] origin = new int[2];
        int[] shape = new int[2];
        Array x, y, z;
        try {
            for (Array array : data) {
                if (array.getRank() == 2) {
                    origin = new int[]{0, 0};
                    shape = new int[]{array.getShape()[0], 1};
                    x = array.section(origin, shape);
                    origin = new int[]{0, 1};
                    y = array.section(origin, shape);
                    this.add(new LineGraphic(x, y, lineBreak));
                } else {
                    origin = new int[]{0, 0, 0};
                    shape = new int[]{array.getShape()[0], 1, 1};
                    x = array.section(origin, shape);
                    origin = new int[]{0, 1, 0};
                    y = array.section(origin, shape);
                    origin = new int[]{0, 0, 1};
                    z = array.section(origin, shape);
                    this.add(new LineGraphic(x, y, z, lineBreak));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updateGraphics(List<Array> data, List<PolylineBreak> lineBreaks) {
        this.singleLegend = false;
        this.data = data;
        this.graphics = new ArrayList<>();
        int[] origin = new int[2];
        int[] shape = new int[2];
        Array x, y, z;
        try {
            int i = 0;
            for (Array array : data) {
                if (array.getRank() == 2) {
                    origin = new int[]{0, 0};
                    shape = new int[]{array.getShape()[0], 1};
                    x = array.section(origin, shape);
                    origin = new int[]{0, 1};
                    y = array.section(origin, shape);
                    if (i >= lineBreaks.size()) {
                        i = 0;
                    }
                    this.add(new LineGraphic(x, y, lineBreaks.get(i)));
                } else {
                    origin = new int[]{0, 0, 0};
                    shape = new int[]{array.getShape()[0], 1, 1};
                    x = array.section(origin, shape);
                    origin = new int[]{0, 1, 0};
                    y = array.section(origin, shape);
                    origin = new int[]{0, 0, 1};
                    z = array.section(origin, shape);
                    if (i >= lineBreaks.size()) {
                        i = 0;
                    }
                    this.add(new LineGraphic(x, y, z, lineBreaks.get(i)));
                }

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
        Array x, y, z;
        try {
            int i = 0;
            for (Array array : data) {
                if (array.getRank() == 2) {
                    origin = new int[]{0, 0};
                    shape = new int[]{array.getShape()[0], 1};
                    x = array.section(origin, shape);
                    origin = new int[]{0, 1};
                    y = array.section(origin, shape);
                    this.add(new LineGraphic(x, y, cData.get(i), ls));
                } else {
                    origin = new int[]{0, 0, 0};
                    shape = new int[]{array.getShape()[0], 1, 1};
                    x = array.section(origin, shape);
                    origin = new int[]{0, 1, 0};
                    y = array.section(origin, shape);
                    origin = new int[]{0, 0, 1};
                    z = array.section(origin, shape);
                    this.add(new LineGraphic(x, y, z, cData.get(i), ls));
                }

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
        Array x, y, z, c;
        try {
            int i = 0;
            for (Array array : data) {
                if (array.getRank() == 2) {
                    origin = new int[]{0, 0};
                    shape = new int[]{array.getShape()[0], 1};
                    x = array.section(origin, shape);
                    origin = new int[]{0, 1};
                    y = array.section(origin, shape);
                    origin = new int[]{0, i};
                    c = cData.section(origin, shape);
                    this.add(new LineGraphic(x, y, c, ls));
                } else {
                    origin = new int[]{0, 0, 0};
                    shape = new int[]{array.getShape()[0], 1, 1};
                    x = array.section(origin, shape);
                    origin = new int[]{0, 1, 0};
                    y = array.section(origin, shape);
                    origin = new int[]{0, 0, 1};
                    z = array.section(origin, shape);
                    origin = new int[]{0, i};
                    shape = new int[]{array.getShape()[0], 1};
                    c = cData.section(origin, shape);
                    this.add(new LineGraphic(x, y, z, c, ls));
                }

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
            List<PolylineBreak> lineBreaks = (List<PolylineBreak>) getLegendBreaks();
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

    @Override
    public float[] getVertexPosition() {
        return this.vertexPosition;
    }

    @Override
    public float[] getVertexColor() {
        return this.vertexColor;
    }

    @Override
    public List<Integer> getLinePointNumbers() {
        return this.linePointNumbers;
    }

    @Override
    public void updateVertex() {
        int vertexNum = this.getNumPoints();

        vertexPosition = new float[vertexNum * 3];
        vertexColor = new float[vertexNum * 4];
        int i = 0, ii = 0;
        float[] colors;
        linePointNumbers = new ArrayList<>();
        for (MultiLineGraphic graphic : (List<MultiLineGraphic>) this.getGraphics()) {
            for (LineString lineString : (LineString[]) ((MultiLineString) graphic.getGeometry()).getGeometries()) {
                List<Coordinate> ps = lineString.getCoordinates();
                linePointNumbers.add(ps.size());
                for (Coordinate p : ps) {
                    vertexPosition[i] = (float) p.x;
                    vertexPosition[i + 1] = (float) p.y;
                    vertexPosition[i + 2] = (float) p.z;
                    i += 3;
                }
            }

            int n = graphic.getGeometry().getNumPoints();
            ColorBreak cb = graphic.getLegendBreak();
            colors = cb.getColor().getRGBComponents(null);
            for (int j = 0; j < n; j++) {
                System.arraycopy(colors, 0, vertexColor, ii * 4, 4);
                ii++;
            }
        }
    }
}
