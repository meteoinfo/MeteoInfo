package org.meteoinfo.chart.graphic;

import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.LineString;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.IndexIterator;

import java.util.ArrayList;
import java.util.List;

public class LineGraphic extends Graphic implements LinealGraphic {
    private Array xData;
    private Array yData;
    private Array zData;
    private Array cData;
    private boolean curve = false;
    private LegendScheme legendScheme;
    private float[] vertexPosition;
    private float[] vertexColor;
    private List<Integer> linePointNumbers;

    /**
     * Constructor
     *
     * @param lineString LineString geometry
     * @param polylineBreak Polyline break
     */
    public LineGraphic(LineString lineString, PolylineBreak polylineBreak) {
        this.geometry = lineString;
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
     * @param zData Z data
     * @param polylineBreak Polyline break
     */
    public LineGraphic(Array xData, Array yData, Array zData, PolylineBreak polylineBreak) {
        this.xData = xData;
        this.yData = yData;
        this.zData = zData;

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
     * @param zData Z data
     */
    public LineGraphic(Array xData, Array yData, Array zData) {
        this(xData, yData, zData, new PolylineBreak());
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     * @param cData Color data
     * @param legendScheme Legend scheme
     */
    public LineGraphic(Array xData, Array yData, Array cData, LegendScheme legendScheme) {
        this(xData, yData, null, cData, legendScheme);
    }

    /**
     * Constructor
     * @param xData X data
     * @param yData Y data
     * @param zData Z data
     * @param cData Color data
     * @param legendScheme Legend scheme
     */
    public LineGraphic(Array xData, Array yData, Array zData, Array cData, LegendScheme legendScheme) {
        this.xData = xData;
        this.yData = yData;
        this.zData = zData;
        this.cData = cData;

        updateShapeLegend(legendScheme);
    }

    protected void updateShape() {
        List<Coordinate> points = new ArrayList<>();
        IndexIterator xIter = this.xData.getIndexIterator();
        IndexIterator yIter = this.yData.getIndexIterator();
        if (this.zData == null) {
            double x, y;
            while (xIter.hasNext() && yIter.hasNext()) {
                x = xIter.getDoubleNext();
                y = yIter.getDoubleNext();
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                points.add(new Coordinate(x, y));
            }
        } else {
            IndexIterator zIter = this.zData.getIndexIterator();
            double x, y, z;
            while (xIter.hasNext() && yIter.hasNext()) {
                x = xIter.getDoubleNext();
                y = yIter.getDoubleNext();
                z = zIter.getDoubleNext();
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                points.add(new Coordinate(x, y, z));
            }
        }

        if (this.geometry == null) {
            this.geometry = new LineString();
        }
        this.geometry.setCoordinates(points);
    }

    protected void updateShapeLegend(LegendScheme legendScheme) {
        this.legendScheme = legendScheme;
        List<Coordinate> points = new ArrayList<>();
        IndexIterator xIter = this.xData.getIndexIterator();
        IndexIterator yIter = this.yData.getIndexIterator();
        IndexIterator cIter = this.cData.getIndexIterator();
        ColorBreakCollection cbc = new ColorBreakCollection();
        ColorBreak cb;
        double x, y, z, c;
        if (zData == null) {
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
        } else {
            IndexIterator zIter = this.zData.getIndexIterator();
            while (xIter.hasNext()) {
                x = xIter.getDoubleNext();
                y = yIter.getDoubleNext();
                z = zIter.getDoubleNext();
                c = cIter.getDoubleNext();
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                points.add(new Coordinate(x, y, z));
                cb = legendScheme.findLegendBreakAlways(c);
                cbc.add(cb);
            }
        }

        if (this.geometry == null) {
            this.geometry = new LineString();
        }
        if (points.size() >= 2)
            this.geometry.setCoordinates(points);

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
        if (zData == null) {
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
        } else {
            double z;
            while (xIter.hasNext()) {
                x = xIter.getDoubleNext();
                y = yIter.getDoubleNext();
                z = yIter.getDoubleNext();
                cb = cbs.get(i);
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                points.add(new Coordinate(x, y, z));
                cbc.add(cb);
                i += 1;
            }
        }

        if (this.geometry == null) {
            this.geometry = new LineString();
        }
        this.geometry.setCoordinates(points);

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
     * Get z data
     * @return Z data
     */
    public Array getZData() {
        return this.yData;
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
     * Set data
     * @param xData X data
     * @param yData Y data
     */
    public void setData(Array xData, Array yData, Array zData) {
        this.xData = xData;
        this.yData = yData;
        this.zData = zData;
        updateShape();
    }

    /**
     * Get legend scheme
     * @return Legend scheme
     */
    public LegendScheme getLegendScheme() {
        return this.legendScheme;
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
        int i = 0;
        float[] color;
        linePointNumbers = new ArrayList<>();
        List<Coordinate> ps = this.getGeometry().getCoordinates();
        linePointNumbers.add(ps.size());
        for (Coordinate p : ps) {
            vertexPosition[i] = (float) p.x;
            vertexPosition[i + 1] = (float) p.y;
            vertexPosition[i + 2] = (float) p.z;
            i += 3;
        }

        ColorBreak cb = this.getLegendBreak();
        i = 0;
        if (cb.getBreakType() == BreakTypes.COLOR_BREAK_COLLECTION) {
            PolylineBreak lineBreak;
            for (int j = 0; j < vertexNum; j++) {
                lineBreak = (PolylineBreak) ((ColorBreakCollection) cb).get(j);
                color = lineBreak.getColor().getRGBComponents(null);
                System.arraycopy(color, 0, vertexColor, i * 4, 4);
                i++;
            }
        } else {
            color = cb.getColor().getRGBComponents(null);
            for (int j = 0; j < vertexNum; j++) {
                System.arraycopy(color, 0, vertexColor, i * 4, 4);
                i++;
            }
        }
    }
}
