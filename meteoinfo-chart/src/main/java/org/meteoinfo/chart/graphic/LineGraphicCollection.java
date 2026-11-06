package org.meteoinfo.chart.graphic;

import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.LineString;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.IndexIterator;

import java.util.ArrayList;
import java.util.List;

public class LineGraphicCollection extends GraphicCollection implements LinealGraphic {
    private Array cData;
    private List<Array> data;
    private boolean curve = false;
    private float[] vertexPosition;
    private float[] vertexColor;
    private List<Integer> linePointNumbers;

    /**
     * Constructor
     */
    public LineGraphicCollection() {
        super();
        this.graphics = new ArrayList<LineGraphic>();
    }

    /**
     * Constructor
     * @param graphics Graphics
     */
    public LineGraphicCollection(List<LineGraphic> graphics) {
        super();
        this.graphics = graphics;
        this.legendBreak = new PolylineBreak();
    }

    /**
     * Constructor
     * @param data Data list
     * @param lineBreak Polyline break
     */
    public LineGraphicCollection(List<Array> data, PolylineBreak lineBreak) {
        super();

        updateGraphics(data, lineBreak);
    }

    /**
     * Constructor
     * @param data Data list
     * @param lineBreaks Polyline break list
     */
    public LineGraphicCollection(List<Array> data, List<PolylineBreak> lineBreaks) {
        super();

        updateGraphics(data, lineBreaks);
    }

    /**
     * Constructor
     * @param data Data list
     * @param cdata Color data
     * @param ls Legend scheme
     */
    public LineGraphicCollection(List<Array> data, Array cData, LegendScheme ls) {
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
    public LineGraphicCollection(List<Array> data, List<Array> cData, LegendScheme ls) {
        this.legendScheme = ls;
        this.setSingleLegend(false);
        updateGraphics(data, cData, ls);
    }

    protected void updateGraphics(List<Array> data, PolylineBreak lineBreak) {
        this.data = data;
        this.graphics = new ArrayList<LineGraphic>();
        int[] origin = new int[2];
        int[] shape = new int[2];
        try {
            for (Array array : data) {
                int[] arrShape = array.getShape();
                int nPoint = arrShape[0];
                int nDim = arrShape[1];
                origin = new int[]{0, 0};
                shape = new int[]{nPoint, 1};
                Array x = array.section(origin, shape);
                origin = new int[]{0, 1};
                Array y = array.section(origin, shape);
                Array z = null;
                if (nDim == 3) {
                    origin = new int[]{0, 2};
                    z = array.section(origin, shape);
                }
                this.add(new LineGraphic(x, y, z, lineBreak));
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
        try {
            int i = 0;
            for (Array array : data) {
                int[] arrShape = array.getShape();
                int nPoint = arrShape[0];
                int nDim = arrShape[1];
                origin = new int[]{0, 0};
                shape = new int[]{nPoint, 1};
                Array x = array.section(origin, shape);
                origin = new int[]{0, 1};
                Array y = array.section(origin, shape);
                Array z = null;
                if (nDim == 3) {
                    origin = new int[]{0, 2};
                    z = array.section(origin, shape);
                }
                if (i >= lineBreaks.size()) {
                    i = 0;
                }
                this.add(new LineGraphic(x, y, z, lineBreaks.get(i)));
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
        try {
            int i = 0;
            for (Array array : data) {
                int[] arrShape = array.getShape();
                int nPoint = arrShape[0];
                int nDim = arrShape[1];
                origin = new int[]{0, 0};
                shape = new int[]{nPoint, 1};
                Array x = array.section(origin, shape);
                origin = new int[]{0, 1};
                Array y = array.section(origin, shape);
                Array z = null;
                if (nDim == 3) {
                    origin = new int[]{0, 2};
                    z = array.section(origin, shape);
                }
                this.add(new LineGraphic(x, y, z, cData.get(i), ls));
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
        try {
            int i = 0;
            for (Array array : data) {
                int[] arrShape = array.getShape();
                int nPoint = arrShape[0];
                int nDim = arrShape[1];
                origin = new int[]{0, 0};
                shape = new int[]{nPoint, 1};
                Array x = array.section(origin, shape);
                origin = new int[]{0, 1};
                Array y = array.section(origin, shape);
                Array z = null;
                if (nDim == 3) {
                    origin = new int[]{0, 2};
                    z = array.section(origin, shape);
                }
                Array c = cData;
                if (c.getRank() == 2) {
                    origin = new int[]{0, i};
                    c = cData.section(origin, shape);
                }
                this.add(new LineGraphic(x, y, z, c, ls));
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
    public void updateVertex() {
        int vertexNum = this.getNumPoints();

        vertexPosition = new float[vertexNum * 3];
        vertexColor = new float[vertexNum * 4];
        int i = 0, ii = 0;
        float[] color;
        linePointNumbers = new ArrayList<>();
        for (LineGraphic graphic : (List<LineGraphic>) this.getGraphics()) {
            List<Coordinate> ps = graphic.getGeometry().getCoordinates();
            linePointNumbers.add(ps.size());
            for (Coordinate p : ps) {
                vertexPosition[i] = (float) p.x;
                vertexPosition[i + 1] = (float) p.y;
                vertexPosition[i + 2] = (float) p.z;
                i += 3;
            }

            int n = graphic.getGeometry().getNumPoints();
            ColorBreak cb = graphic.getLegendBreak();
            if (cb.getBreakType() == BreakTypes.COLOR_BREAK_COLLECTION) {
                PolylineBreak lineBreak;
                for (int j = 0; j < n; j++) {
                    lineBreak = (PolylineBreak) ((ColorBreakCollection) cb).get(j);
                    color = lineBreak.getColor().getRGBComponents(null);
                    System.arraycopy(color, 0, vertexColor, ii * 4, 4);
                    ii++;
                }
            } else {
                color = cb.getColor().getRGBComponents(null);
                for (int j = 0; j < n; j++) {
                    System.arraycopy(color, 0, vertexColor, ii * 4, 4);
                    ii++;
                }
            }
        }
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
}
