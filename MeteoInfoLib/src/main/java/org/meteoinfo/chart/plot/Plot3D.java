/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.chart.ChartLegend;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.chart.ChartText3D;
import org.meteoinfo.chart.LegendPosition;
import org.meteoinfo.chart.Margin;
import org.meteoinfo.chart.axis.Axis;
import org.meteoinfo.chart.axis.LogAxis;
import org.meteoinfo.chart.plot3d.GraphicCollection3D;
import org.meteoinfo.chart.plot3d.Projector;
import org.meteoinfo.data.DataMath;
import org.meteoinfo.data.Dataset;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.Extent3D;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointF;
import org.meteoinfo.legend.BreakTypes;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.ColorBreakCollection;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.math.sort.QuickSort;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.ImageShape;
import org.meteoinfo.shape.PointZ;
import org.meteoinfo.shape.PointZShape;
import org.meteoinfo.shape.PolygonZ;
import org.meteoinfo.shape.PolygonZShape;
import org.meteoinfo.shape.Polyline;
import org.meteoinfo.shape.PolylineZShape;
import org.meteoinfo.shape.Shape;
import org.meteoinfo.shape.ShapeTypes;
import org.meteoinfo.shape.WindArrow3D;

/**
 *
 * @author Yaqiang Wang
 */
public class Plot3D extends Plot {

    // <editor-fold desc="Variables">
    private final GraphicCollection3D graphics;
    private Extent3D extent;
    private ChartText title;
    private List<ChartLegend> legends;
    private Axis xAxis;
    private Axis yAxis;
    private Axis zAxis;

    private final Projector projector; // the projector, controls the point of view
    private int prevwidth, prevheight; // canvas size
    private Rectangle graphBounds;    //Graphic area bounds

    private boolean isBoxed, isMesh, isScaleBox, isDisplayXY, isDisplayZ,
            isDisplayGrids, drawBoundingBox, drawBase;
    private boolean hideOnDrag;
    private float xmin, xmax, ymin;
    private float ymax, zmin, zmax;

    private Color boxColor = Color.getHSBColor(0f, 0f, 0.95f);
    private Color lineboxColor = Color.getHSBColor(0f, 0f, 0.8f);

    // Projection parameters
    private int factor_x, factor_y; // conversion factors
    private int t_x, t_y, t_z; // determines ticks density
    //private final int poly_x[] = new int[9];
    //private final int poly_y[] = new int[9];
    private Point projection;
    float xfactor;
    float yfactor;
    float zfactor;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public Plot3D() {
        this.legends = new ArrayList<>();
        this.xAxis = new Axis();
        this.xAxis.setLabel("X");
        this.xAxis.setTickLength(8);
        this.yAxis = new Axis();
        this.yAxis.setLabel("Y");
        this.yAxis.setTickLength(8);
        this.zAxis = new Axis();
        this.zAxis.setLabel("Z");
        this.zAxis.setTickLength(8);
        projector = new Projector();
        projector.setDistance(10000);
        projector.set2DScaling(15);
        projector.setRotationAngle(225);
        projector.setElevationAngle(30);
        this.graphics = new GraphicCollection3D();
        this.hideOnDrag = false;
        this.isBoxed = true;
        this.isDisplayGrids = true;
        this.isDisplayXY = true;
        this.isDisplayZ = true;
        this.drawBoundingBox = false;
        this.drawBase = true;
    }

    // </editor-fold>
    // <editor-fold desc="GetSet">
    /**
     * Get graphics
     * @return The graphics
     */
    public GraphicCollection3D getGraphics() {
        return this.graphics;
    }

    /**
     * Get the number of graphics
     * @return The number of graphics
     */
    public int getGraphicNumber() {
        return this.graphics.size();
    }

    /**
     * Get projector
     *
     * @return The Projector
     */
    public Projector getProjector() {
        return this.projector;
    }

    /**
     * Get title
     *
     * @return Title
     */
    public ChartText getTitle() {
        return this.title;
    }

    /**
     * Set title
     *
     * @param value Title
     */
    public void setTitle(ChartText value) {
        this.title = value;
    }

    /**
     * Set title
     *
     * @param text Title text
     */
    public void setTitle(String text) {
        if (this.title == null) {
            this.title = new ChartText(text);
        } else {
            this.title.setText(text);
        }
    }

    /**
     * Get legends
     *
     * @return Legends
     */
    public List<ChartLegend> getLegends() {
        return this.legends;
    }

    /**
     * Get chart legend
     *
     * @param idx Index
     * @return Chart legend
     */
    public ChartLegend getLegend(int idx) {
        if (this.legends.isEmpty()) {
            return null;
        } else {
            return this.legends.get(idx);
        }
    }

    /**
     * Get chart legend
     *
     * @return Chart legend
     */
    public ChartLegend getLegend() {
        if (this.legends.isEmpty()) {
            return null;
        } else {
            return this.legends.get(this.legends.size() - 1);
        }
    }

    /**
     * Set chart legend
     *
     * @param value Legend
     */
    public void setLegend(ChartLegend value) {
        this.legends.clear();
        this.legends.add(value);
    }

    /**
     * Set legends
     *
     * @param value Legends
     */
    public void setLegends(List<ChartLegend> value) {
        this.legends = value;
    }

    /**
     * Get x axis
     *
     * @return X axis
     */
    public Axis getXAxis() {
        return this.xAxis;
    }

    /**
     * Set x axis
     * @param value X axis
     */
    public void setXAxis(Axis value) {
        this.xAxis = value;
    }

    /**
     * Get y axis
     *
     * @return Y axis
     */
    public Axis getYAxis() {
        return this.yAxis;
    }

    /**
     * Set y axis
     * @param value Y axis
     */
    public void setYAxis(Axis value) { this.yAxis = value; }

    /**
     * Get z axis
     * @return Z axis
     */
    public Axis getZAxis() {
        return this.zAxis;
    }

    /**
     * Set z axis
     * @param value Z axis
     */
    public void setZAxis(Axis value) {
        this.zAxis = value;
    }
    
    /**
     * Get x minimum
     * @return X minimum
     */
    public float getXMin(){
        return this.xmin;
    }

    /**
     * Set minimum x
     *
     * @param value Minimum x
     */
    public void setXMin(float value) {
        this.xmin = value;
        updateExtent();
        this.xAxis.setMinMaxValue(xmin, xmax);
    }
    
    /**
     * Get x maximum
     * @return X maximum
     */
    public float getXMax() {
        return this.xmax;
    }

    /**
     * Set maximum x
     *
     * @param value Maximum x
     */
    public void setXMax(float value) {
        this.xmax = value;
        updateExtent();
        this.xAxis.setMinMaxValue(xmin, xmax);
    }

    /**
     * Set x minimum and maximum values
     *
     * @param min Minimum value
     * @param max Maximum value
     */
    public void setXMinMax(float min, float max) {
        this.xmin = min;
        this.xmax = max;
        updateExtent();
        this.xAxis.setMinMaxValue(min, max);
    }
    
    /**
     * Get y minimum
     * @return Y minimum
     */
    public float getYMin(){
        return this.ymin;
    }

    /**
     * Set minimum y
     *
     * @param value Minimum y
     */
    public void setYMin(float value) {
        this.ymin = value;
        updateExtent();
        this.yAxis.setMinMaxValue(ymin, ymax);
    }
    
    /**
     * Get y maximum
     * @return Y maximum
     */
    public float getYMax() {
        return this.ymax;
    }

    /**
     * Set Maximum y
     *
     * @param value Maximum y
     */
    public void setYMax(float value) {
        this.ymax = value;
        updateExtent();
        this.yAxis.setMinMaxValue(ymin, ymax);
    }

    /**
     * Set y minimum and maximum values
     *
     * @param min Minimum value
     * @param max Maximum value
     */
    public void setYMinMax(float min, float max) {
        this.ymin = min;
        this.ymax = max;
        updateExtent();
        this.yAxis.setMinMaxValue(min, max);
    }
    
    /**
     * Get z minimum
     * @return Z minimum
     */
    public float getZMin(){
        return this.zmin;
    }

    /**
     * Set minimum z
     *
     * @param value Minimum z
     */
    public void setZMin(float value) {
        this.zmin = value;
        updateExtent();
        this.zAxis.setMinMaxValue(zmin, zmax);
    }
    
    /**
     * Get z maximum
     * @return Z maximum
     */
    public float getZMax() {
        return this.zmax;
    }

    /**
     * Set maximum z
     *
     * @param value Maximum z
     */
    public void setZMax(float value) {
        this.zmax = value;
        updateExtent();
        this.zAxis.setMinMaxValue(zmin, zmax);
    }

    /**
     * Set z minimum and maximum values
     *
     * @param min Minimum value
     * @param max Maximum value
     */
    public void setZMinMax(float min, float max) {
        this.zmin = min;
        this.zmax = max;
        updateExtent();
        this.zAxis.setMinMaxValue(min, max);
    }

    /**
     * Set display X/Y axis or not
     *
     * @param value Boolean
     */
    public void setDisplayXY(boolean value) {
        this.isDisplayXY = value;
    }

    /**
     * Set display Z axis or not
     *
     * @param value Boolean
     */
    public void setDisplayZ(boolean value) {
        this.isDisplayZ = value;
    }

    /**
     * Set display grids or not
     *
     * @param value Boolean
     */
    public void setDisplayGrids(boolean value) {
        this.isDisplayGrids = value;
    }

    /**
     * Set display box or not
     *
     * @param value Boolean
     */
    public void setBoxed(boolean value) {
        this.isBoxed = value;
    }

    /**
     * Set display mesh line or not
     *
     * @param value Boolean
     */
    public void setMesh(boolean value) {
        this.isMesh = value;
    }

    /**
     * Get if draw bounding box or not
     *
     * @return Boolean
     */
    public boolean isDrawBoundingBox() {
        return this.drawBoundingBox;
    }

    /**
     * Set if draw bounding box or not
     *
     * @param value Boolean
     */
    public void setDrawBoundingBox(boolean value) {
        this.drawBoundingBox = value;
    }

    /**
     * Get if draw base area
     * @return Draw base area or not
     */
    public boolean isDrawBase() {
        return  this.drawBase;
    }

    /**
     * Set if draw base area
     * @param value Draw base area or not
     */
    public void setDrawBase(boolean value) {
        this.drawBase = value;
    }

    /**
     * Get extent
     *
     * @return Extent
     */
    public Extent3D getExtent() {
        return this.extent;
    }

    /**
     * Set extent
     *
     * @param value Extent
     */
    public void setExtent(Extent3D value) {
        this.extent = value;
        xmin = (float) extent.minX;
        xmax = (float) extent.maxX;
        ymin = (float) extent.minY;
        ymax = (float) extent.maxY;
        zmin = (float) extent.minZ;
        zmax = (float) extent.maxZ;
        xAxis.setMinMaxValue(xmin, xmax);
        yAxis.setMinMaxValue(ymin, ymax);
        zAxis.setMinMaxValue(zmin, zmax);
        if (zAxis instanceof LogAxis)
            this.projector.setZRange((float)Math.log10(zmin), (float)Math.log10(zmax));
        else
            this.projector.setZRange(zmin, zmax);
    }

    // </editor-fold>
    // <editor-fold desc="Methods">    
    private void updateExtent() {
        this.extent = new Extent3D(xmin, xmax, ymin, ymax, zmin, zmax);
    }
    
    /**
     * Set axis tick font
     *
     * @param font Font
     */
    public void setAxisTickFont(Font font) {
        this.xAxis.setTickLabelFont(font);
        this.yAxis.setTickLabelFont(font);
        this.zAxis.setTickLabelFont(font);
    }

    /**
     * Add a graphic
     *
     * @param g Grahic
     */
    public void addGraphic(Graphic g) {
        this.graphics.add(g);
        Extent ex = this.graphics.getExtent();
        if (!ex.is3D()){
            ex = ex.to3D();
        }
        this.setExtent((Extent3D)ex);
    }
    
    /**
     * Remove a graphic by index
     *
     * @param idx Index
     */
    public void removeGraphic(int idx) {
        this.graphics.remove(idx);
    }

    /**
     * Remove last graphic
     */
    public void removeLastGraphic() {
        this.graphics.remove(this.graphics.size() - 1);
    }
    
    /**
     * Set auto extent
     */
    public void setAutoExtent() {
    }

    /**
     * Destroys the internal image. It will force <code>SurfaceCanvas</code> to
     * regenerate all images when the <code>paint</code> method is called.
     */
    public void destroyImage() {
        repaint();
    }

    private void repaint() {

    }

    /**
     * Add a legend
     *
     * @param legend The legend
     */
    public void addLegend(ChartLegend legend) {
        this.legends.add(legend);
    }

    /**
     * Remove a legend
     *
     * @param legend The legend
     */
    public void removeLegend(ChartLegend legend) {
        this.legends.remove(legend);
    }

    /**
     * Remove a legend by index
     *
     * @param idx The legend index
     */
    public void removeLegend(int idx) {
        this.legends.remove(idx);
    }

    /**
     * Get outer position area
     *
     * @param area Whole area
     * @return Position area
     */
    @Override
    public Rectangle2D getOuterPositionArea(Rectangle2D area) {
        Rectangle2D rect = this.getOuterPosition();
        double x = area.getWidth() * rect.getX() + area.getX();
        double y = area.getHeight() * (1 - rect.getHeight() - rect.getY()) + area.getY();
        double w = area.getWidth() * rect.getWidth();
        double h = area.getHeight() * rect.getHeight();
        return new Rectangle2D.Double(x, y, w, h);
    }

    @Override
    public Dataset getDataset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDataset(Dataset dataset) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PlotType getPlotType() {
        return PlotType.XYZ;
    }

    @Override
    public void draw(Graphics2D g2, Rectangle2D area) {
        this.setGraphArea(this.getPositionArea());

        //Draw title
        float y = this.drawTitle(g2, this.getGraphArea());

        //Set projection area
        Rectangle parea = this.getPositionArea(area).getBounds();
        if ((parea.width != prevwidth) || (parea.height != prevheight)) {
            prevwidth = parea.width;
            prevheight = parea.height;
        }
        projector.setProjectionArea(parea);

        if (this.xAxis instanceof LogAxis)
            xfactor = 20f / ((float)Math.log10(xmax) - (float)Math.log10(this.xmin));
        else
            xfactor = 20f / (this.xmax - this.xmin); // 20 aint magic: surface vertex requires a value in [-10 ; 10]
        if (this.yAxis instanceof LogAxis)
            yfactor = 20f / ((float)Math.log10(this.ymax) - (float)Math.log10(this.ymin));
        else
            yfactor = 20f / (this.ymax - this.ymin);
        if (this.zAxis instanceof LogAxis)
            zfactor = 20f / ((float)Math.log10(this.zmax) - (float)Math.log10(this.zmin));
        else
            zfactor = 20f / (this.zmax - this.zmin);

        //Get graph bounds
        this.graphBounds = this.projector.getBounds();

        //Draw box
        drawBoxGridsTicksLabels(g2);

        //Draw graph border polygon
        java.awt.Polygon border = getBorder();
        //g2.setColor(Color.red);
        //g2.draw(border);

        //Set graph border polygon clip
        Rectangle oldClip = g2.getClipBounds();
        g2.setClip(border);

        //Draw 3D graphics        
        drawAllGraphics(g2);

        //Cancel the graph border polygon clip
        g2.setClip(oldClip);

        //Draw bounding box
        if (this.drawBoundingBox) {
            drawBoundingBox(g2);
        }

        //Draw legend
        this.drawLegend(g2, area, this.graphBounds, y);
    }

    float drawTitle(Graphics2D g, Rectangle2D graphArea) {
        float y = (float) graphArea.getY();
        if (title != null) {
            float x = (float) (graphArea.getX() + graphArea.getWidth() / 2);
            y -= 8;
            title.draw(g, x, y);
            g.setFont(new Font("Arial", Font.PLAIN, 14));
        }
        return y;
    }

    private void drawAllGraphics(Graphics2D g2) {
        for (int m = 0; m < this.graphics.getNumGraphics(); m++) {
            Graphic graphic = this.graphics.get(m);
            if (graphic instanceof GraphicCollection3D && ((GraphicCollection3D) graphic).isFixZ()) {
                this.drawGraphics_FixZ(g2, graphic);
            } else {
                this.drawGrahpics(g2, graphic);
            }
        }
    }

    private void drawGrahic(Graphics2D g, Graphic graphic) {
        Shape shape = graphic.getGraphicN(0).getShape();
        switch (shape.getShapeType()) {
            case Point:
            case PointZ:
                this.drawPoint(g, graphic);
                break;
            case TEXT:
                this.drawText((ChartText3D) shape, g);
                break;
            case Polyline:
            case PolylineZ:
                this.drawLineString(g, graphic);
                break;
            case Polygon:
            case PolygonZ:
                this.drawPolygonShape(g, graphic);
                break;
            case WindArraw:
                this.drawWindArrow(g, graphic);
                break;
            case Image:

                break;
        }
    }

    private void drawGraphics_FixZ(Graphics2D g, Graphic graphic) {
        //Set clip polygon
        float zValue = (float) ((GraphicCollection3D) graphic).getZValue();
        String zdir = ((GraphicCollection3D) graphic).getZDir();
        java.awt.Polygon polygon = new java.awt.Polygon();
        Point p;
        List<Point> points = new ArrayList<>();
        switch (zdir) {
            case "x":
                /*zValue = (zValue - this.xmin) * xfactor - 10;
                p = projector.project(zValue, -10, -10);*/
                p = this.project(zValue, this.ymin, this.zmin);
                points.add(p);
                //p = projector.project(zValue, -10, 10);
                p = this.project(zValue, this.ymin, this.zmax);
                points.add(p);
                //p = projector.project(zValue, 10, 10);
                p = this.project(zValue, this.ymax, this.zmax);
                points.add(p);
                //p = projector.project(zValue, 10, -10);
                p = this.project(zValue, this.ymax, this.zmin);
                points.add(p);
                break;
            case "y":
                //zValue = (zValue - this.ymin) * yfactor - 10;
                //p = projector.project(-10, zValue, -10);
                p = this.project(this.xmin, zValue, this.zmin);
                points.add(p);
                //p = projector.project(-10, zValue, 10);
                p = this.project(this.xmin, zValue, this.zmax);
                points.add(p);
                //p = projector.project(10, zValue, 10);
                p = this.project(this.xmax, zValue, this.zmax);
                points.add(p);
                //p = projector.project(10, zValue, -10);
                p = this.project(this.xmax, zValue, this.zmin);
                points.add(p);
                break;
            case "xy":
                List<Number> sePoint = ((GraphicCollection3D) graphic).getSEPoint();
                if (sePoint != null && sePoint.size() > 3){
                    float sx = sePoint.get(0).floatValue();
                    float sy = sePoint.get(1).floatValue();
                    float ex = sePoint.get(2).floatValue();
                    float ey = sePoint.get(3).floatValue();
                    /*sx = (sx - this.xmin) * xfactor - 10;
                    ex = (ex - this.xmin) * xfactor - 10;
                    sy = (sy - this.ymin) * yfactor - 10;
                    ey = (ey - this.ymin) * yfactor - 10;
                    p = projector.project(sx, sy, -10);*/
                    p = this.project(sx, sy, this.zmin);
                    points.add(p);
                    //p = projector.project(sx, sy, 10);
                    p = this.project(sx, sy, this.zmax);
                    points.add(p);
                    //p = projector.project(ex, ey, 10);
                    p = this.project(ex, ey, this.zmax);
                    points.add(p);
                    //p = projector.project(ex, ey, -10);
                    p = this.project(ex, ey, this.zmin);
                    points.add(p);
                }
                break;
            case "z":
                //zValue = (zValue - this.zmin) * zfactor - 10;
                //p = projector.project(-10, -10, zValue);
                p = this.project(this.xmin, this.ymin, zValue);
                points.add(p);
                //p = projector.project(-10, 10, zValue);
                p = this.project(this.xmin, this.ymax, zValue);
                points.add(p);
                //p = projector.project(10, 10, zValue);
                p = this.project(this.xmax, this.ymax, zValue);
                points.add(p);
                //p = projector.project(10, -10, zValue);
                p = this.project(this.xmax, this.ymin, zValue);
                points.add(p);
                break;
        }
        java.awt.Shape oldRegion = g.getClip();
        if (points.size() > 3){
            for (Point pp : points) {
                polygon.addPoint(pp.x, pp.y);
            }            
            g.setClip(polygon);
        }

        //Draw graphics
        for (int i = 0; i < graphic.getNumGraphics(); i++) {
            Graphic gg = graphic.getGraphicN(i);
            if (gg.getShape().getShapeType() == ShapeTypes.Image) {
                //g.setClip(oldRegion);
                this.drawImage(g, gg, zdir, (float) ((GraphicCollection3D) graphic).getZValue());
            } else {
                this.drawGrahic(g, gg);
            }
        }

        //Set clip to orgin
        if (points.size() > 3){
            g.setClip(oldRegion);
        }
    }

    private void drawGrahpics(Graphics2D g, Graphic graphic) {
        if (graphic.getNumGraphics() == 1) {
            Graphic gg = graphic.getGraphicN(0);
            this.drawGrahic(g, gg);
        } else {
            int n = graphic.getNumGraphics();
            double[] dds = new double[n];
            int[] order = new int[n];
            PointZ p;
            double d;
            float angle = projector.getRotationAngle();
            boolean xdir = true;
            if (angle < 45 || angle > 135 && angle < 225 || angle > 315) {
                xdir = false;
            }
            
            if (xdir) {
                for (int i = 0; i < n; i++) {
                    Graphic gg = graphic.getGraphicN(i);
                    Shape shape = gg.getShape();
                    p = (PointZ) shape.getPoints().get(0);
                    d = p.X * projector.getSinRotationAngle();
                    dds[i] = d;
                    order[i] = i;
                }
            } else {
                for (int i = 0; i < graphic.getNumGraphics(); i++) {
                    Graphic gg = graphic.getGraphicN(i);
                    Shape shape = gg.getShape();
                    p = (PointZ) shape.getPoints().get(0);
                    d = p.Y * projector.getCosRotationAngle();
                    dds[i] = d;
                    order[i] = i;
                }
            }
            
            QuickSort.sort(dds, order);

            for (int i : order) {
                Graphic gg = graphic.getGraphicN(i);
                this.drawGrahic(g, gg);
            }
        }
    }
    
    private void drawGrahpics_bak(Graphics2D g, Graphic graphic) {
        if (graphic.getNumGraphics() == 1) {
            Graphic gg = graphic.getGraphicN(0);
            this.drawGrahic(g, gg);
        } else {
            List<Double> dds = new ArrayList<>();
            List<Integer> order = new ArrayList<>();
            PointZ p;
            double d;
            boolean isIn;
            float angle = projector.getRotationAngle();
            boolean xdir = true;
            if (angle < 45 || angle > 135 && angle < 225 || angle > 315) {
                xdir = false;
            }
            if (xdir) {
                for (int i = 0; i < graphic.getNumGraphics(); i++) {
                    Graphic gg = graphic.getGraphicN(i);
                    Shape shape = gg.getShape();
                    p = (PointZ) shape.getPoints().get(0);
                    d = p.X * projector.getSinRotationAngle();
                    isIn = false;
                    for (int j = 0; j < dds.size(); j++) {
                        if (d < dds.get(j)) {
                            dds.add(j, d);
                            order.add(j, i);
                            isIn = true;
                            break;
                        }
                    }
                    if (!isIn) {
                        dds.add(d);
                        order.add(i);
                    }
                }
            } else {
                for (int i = 0; i < graphic.getNumGraphics(); i++) {
                    Graphic gg = graphic.getGraphicN(i);
                    Shape shape = gg.getShape();
                    p = (PointZ) shape.getPoints().get(0);
                    d = p.Y * projector.getCosRotationAngle();
                    isIn = false;
                    for (int j = 0; j < dds.size(); j++) {
                        if (d < dds.get(j)) {
                            dds.add(j, d);
                            order.add(j, i);
                            isIn = true;
                            break;
                        }
                    }
                    if (!isIn) {
                        dds.add(d);
                        order.add(i);
                    }
                }
            }

            for (int i : order) {
                Graphic gg = graphic.getGraphicN(i);
                this.drawGrahic(g, gg);
            }
        }
    }

    private void drawPoints(Graphics2D g, Graphic graphic) {
        if (graphic.getNumGraphics() == 1) {
            Graphic gg = graphic.getGraphicN(0);
            drawPoint(g, gg);
        } else {
            List<Double> dds = new ArrayList<>();
            List<Integer> order = new ArrayList<>();
            PointZ p;
            double d;
            boolean isIn;
            float angle = projector.getRotationAngle();
            boolean xdir = true;
            if (angle < 45 || angle > 135 && angle < 225 || angle > 315) {
                xdir = false;
            }
            if (xdir) {
                for (int i = 0; i < graphic.getNumGraphics(); i++) {
                    Graphic gg = graphic.getGraphicN(i);
                    PointZShape shape = (PointZShape) gg.getShape();
                    p = (PointZ) shape.getPoint();
                    d = p.X * projector.getSinRotationAngle();
                    isIn = false;
                    for (int j = 0; j < dds.size(); j++) {
                        if (d < dds.get(j)) {
                            dds.add(j, d);
                            order.add(j, i);
                            isIn = true;
                            break;
                        }
                    }
                    if (!isIn) {
                        dds.add(d);
                        order.add(i);
                    }
                }
            } else {
                for (int i = 0; i < graphic.getNumGraphics(); i++) {
                    Graphic gg = graphic.getGraphicN(i);
                    PointZShape shape = (PointZShape) gg.getShape();
                    p = (PointZ) shape.getPoint();
                    d = p.Y * projector.getCosRotationAngle();
                    isIn = false;
                    for (int j = 0; j < dds.size(); j++) {
                        if (d < dds.get(j)) {
                            dds.add(j, d);
                            order.add(j, i);
                            isIn = true;
                            break;
                        }
                    }
                    if (!isIn) {
                        dds.add(d);
                        order.add(i);
                    }
                }
            }

            for (int i : order) {
                Graphic gg = graphic.getGraphicN(i);
                drawPoint(g, gg);
            }
        }
    }

    /**
     * Project 3D point
     *
     * @param x X
     * @param y Y
     * @param z Z
     * @return Projected 2D point
     */
    public Point project(float x, float y, float z) {
        float px, py, pz;
        if (this.xAxis instanceof LogAxis)
            px = (float)(Math.log10(x) - Math.log10(xmin)) * xfactor - 10;
        else
            px = (x - xmin) * xfactor - 10;
        if (this.yAxis instanceof LogAxis)
            py = (float)(Math.log10(y) - Math.log10(ymin)) * yfactor - 10;
        else
            py = (y - ymin) * yfactor - 10;
        if (this.zAxis instanceof LogAxis)
            pz = (float)(Math.log10(z) - Math.log10(zmin)) * zfactor - 10;
        else
            pz = (z - zmin) * zfactor - 10;
        return this.projector.project(px, py, pz);
    }

    /**
     * Project 3D point
     *
     * @param x X
     * @param y Y
     * @param z Z
     * @return Projected 2D point
     */
    public Point project_bak(float x, float y, float z) {
        return this.projector.project((x - xmin) * xfactor - 10,
                (y - ymin) * yfactor - 10, (z - zmin) * zfactor - 10);
    }

    void drawText(ChartText3D text, Graphics2D g) {
        float x, y;
        Point p = this.project((float) text.getX(), (float) text.getY(), (float) text.getZ());
        x = p.x;
        y = p.y;
        this.drawText(g, text, x, y);
    }

    private void drawText(Graphics2D g, ChartText3D text, float x, float y) {
        AffineTransform tempTrans = g.getTransform();
        //AffineTransform myTrans = new AffineTransform();
        AffineTransform myTrans = (AffineTransform)tempTrans.clone();
        myTrans.translate(x, y);
        if (text.getZDir() != null) {
            text.updateAngle(projector);
            float angle = text.getAngle() + 90;
            myTrans.rotate(-angle * Math.PI / 180);
        }
        g.setTransform(myTrans);
        g.setFont(text.getFont());
        g.setColor(text.getColor());
        x = 0;
        y = 0;
        switch (text.getYAlign()) {
            case TOP:
                y += g.getFontMetrics(g.getFont()).getAscent();
                break;
            case CENTER:
                y += g.getFontMetrics(g.getFont()).getAscent() / 2;
                break;
        }
        String s = text.getText();
        Dimension labSize = Draw.getStringDimension(s, g);
        switch (text.getXAlign()) {
            case RIGHT:
                x = x - labSize.width;
                break;
            case CENTER:
                x = x - labSize.width / 2;
                break;
        }
        Draw.drawString(g, s, x, y);
        g.setTransform(tempTrans);
    }

    private void drawPoint(Graphics2D g, Graphic graphic) {
        if (extent.intersects(graphic.getExtent())) {
            PointZShape shape = (PointZShape) graphic.getShape();
            PointBreak pb = (PointBreak) graphic.getLegend();
            PointZ p = (PointZ) shape.getPoint();
            /*PointZ pp = new PointZ((p.X - xmin) * xfactor - 10, (p.Y - ymin) * yfactor - 10,
                    (p.Z - this.zmin) * zfactor - 10);
            projection = projector.project((float) pp.X, (float) pp.Y, (float) pp.Z);*/
            projection = this.project((float)p.X, (float)p.Y, (float)p.Z);
            PointF pf = new PointF(projection.x, projection.y);
            Draw.drawPoint(pf, pb, g);
        }
    }

    private void drawLineStrings(Graphics2D g, Graphic graphic) {
        if (graphic.getNumGraphics() == 1) {
            Graphic gg = graphic.getGraphicN(0);
            drawLineString(g, gg);
        } else {
            List<Double> dds = new ArrayList<>();
            List<Integer> order = new ArrayList<>();
            PointZ p;
            double d;
            boolean isIn;
            float angle = projector.getRotationAngle();
            boolean xdir = true;
            if (angle < 45 || angle > 135 && angle < 225 || angle > 315) {
                xdir = false;
            }
            if (xdir) {
                for (int i = 0; i < graphic.getNumGraphics(); i++) {
                    Graphic gg = graphic.getGraphicN(i);
                    Shape shape = gg.getShape();
                    p = (PointZ) shape.getPoints().get(0);
                    d = p.X * projector.getSinRotationAngle();
                    isIn = false;
                    for (int j = 0; j < dds.size(); j++) {
                        if (d < dds.get(j)) {
                            dds.add(j, d);
                            order.add(j, i);
                            isIn = true;
                            break;
                        }
                    }
                    if (!isIn) {
                        dds.add(d);
                        order.add(i);
                    }
                }
            } else {
                for (int i = 0; i < graphic.getNumGraphics(); i++) {
                    Graphic gg = graphic.getGraphicN(i);
                    Shape shape = gg.getShape();
                    p = (PointZ) shape.getPoints().get(0);
                    d = p.Y * projector.getCosRotationAngle();
                    isIn = false;
                    for (int j = 0; j < dds.size(); j++) {
                        if (d < dds.get(j)) {
                            dds.add(j, d);
                            order.add(j, i);
                            isIn = true;
                            break;
                        }
                    }
                    if (!isIn) {
                        dds.add(d);
                        order.add(i);
                    }
                }
            }

            for (int i : order) {
                Graphic gg = graphic.getGraphicN(i);
                drawLineString(g, gg);
            }
        }
    }

    private void drawLineString(Graphics2D g, Graphic graphic) {
        if (extent.intersects(graphic.getExtent())) {
            PolylineZShape shape = (PolylineZShape) graphic.getShape();
            ColorBreak pb = graphic.getLegend();
            for (Polyline line : shape.getPolylines()){
                List<PointZ> ps = (List<PointZ>)line.getPointList();
                PointF[] points = new PointF[ps.size()];
                PointZ p, pp;
                for (int i = 0; i < ps.size(); i++) {
                    p = ps.get(i);
                    projection = this.project((float)p.X, (float)p.Y, (float)p.Z);
                    points[i] = new PointF(projection.x, projection.y);
                }
                if (pb.getBreakType() == BreakTypes.ColorBreakCollection)
                    Draw.drawPolyline(points, (ColorBreakCollection)pb, g);
                else
                    Draw.drawPolyline(points, (PolylineBreak)pb, g);
            }
        }
    }

    private void drawPolygons(Graphics2D g, Graphic graphic) {
        List<Double> dds = new ArrayList<>();
        List<Integer> order = new ArrayList<>();
        PointZ p;
        double d;
        boolean isIn;
        float angle = projector.getRotationAngle();
        boolean xdir = true;
        if (angle < 45 || angle > 135 && angle < 225 || angle > 315) {
            xdir = false;
        }
        if (xdir) {
            for (int i = 0; i < graphic.getNumGraphics(); i++) {
                Graphic gg = graphic.getGraphicN(i);
                Shape shape = gg.getShape();
                p = (PointZ) shape.getPoints().get(0);
                d = p.X * projector.getSinRotationAngle();
                isIn = false;
                for (int j = 0; j < dds.size(); j++) {
                    if (d < dds.get(j)) {
                        dds.add(j, d);
                        order.add(j, i);
                        isIn = true;
                        break;
                    }
                }
                if (!isIn) {
                    dds.add(d);
                    order.add(i);
                }
            }
        } else {
            for (int i = 0; i < graphic.getNumGraphics(); i++) {
                Graphic gg = graphic.getGraphicN(i);
                Shape shape = gg.getShape();
                p = (PointZ) shape.getPoints().get(0);
                d = p.Y * projector.getCosRotationAngle();
                isIn = false;
                for (int j = 0; j < dds.size(); j++) {
                    if (d < dds.get(j)) {
                        dds.add(j, d);
                        order.add(j, i);
                        isIn = true;
                        break;
                    }
                }
                if (!isIn) {
                    dds.add(d);
                    order.add(i);
                }
            }
        }

        for (int i : order) {
            Graphic gg = graphic.getGraphicN(i);
            this.drawPolygonShape(g, gg);
        }
    }

    private void drawPolygonShape(Graphics2D g, Graphic graphic) {
        if (extent.intersects(graphic.getExtent())) {
            PolygonZShape shape = (PolygonZShape) graphic.getShape();
            PolygonBreak pb = (PolygonBreak) graphic.getLegend();
            for (PolygonZ poly : (List<PolygonZ>) shape.getPolygons()) {
                drawPolygon(g, poly, pb);
            }
        }
    }

    private List<PointF> drawPolygon(Graphics2D g, PolygonZ aPG, PolygonBreak aPGB) {
        int len = aPG.getOutLine().size();
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, len);
        PointZ p, pp;
        List<PointF> rPoints = new ArrayList<>();
        for (int i = 0; i < aPG.getOutLine().size(); i++) {
            p = ((List<PointZ>) aPG.getOutLine()).get(i);
//            pp = new PointZ((p.X - xmin) * xfactor - 10, (p.Y - ymin) * yfactor - 10,
//                    (p.Z - this.zmin) * zfactor - 10);
//            projection = projector.project((float) pp.X, (float) pp.Y, (float) pp.Z);
            projection = this.project((float)p.X, (float)p.Y, (float)p.Z);
            if (i == 0) {
                path.moveTo(projection.x, projection.y);
            } else {
                path.lineTo(projection.x, projection.y);
            }
            rPoints.add(new PointF(projection.x, projection.y));
        }

        List<PointZ> newPList;
        if (aPG.hasHole()) {
            for (int h = 0; h < aPG.getHoleLines().size(); h++) {
                newPList = (List<PointZ>) aPG.getHoleLines().get(h);
                for (int j = 0; j < newPList.size(); j++) {
                    p = newPList.get(j);
//                    pp = new PointZ((p.X - xmin) * xfactor - 10, (p.Y - ymin) * yfactor - 10,
//                            (p.Z - this.zmin) * zfactor - 10);
//                    projection = projector.project((float) pp.X, (float) pp.Y, (float) pp.Z);
                    projection = this.project((float)p.X, (float)p.Y, (float)p.Z);
                    if (j == 0) {
                        path.moveTo(projection.x, projection.y);
                    } else {
                        path.lineTo(projection.x, projection.y);
                    }
                }
            }
        }
        path.closePath();

        if (aPGB.isDrawFill()) {
            Color aColor = aPGB.getColor();
            if (aPGB.isUsingHatchStyle()) {
                int size = aPGB.getStyleSize();
                BufferedImage bi = Draw.getHatchImage(aPGB.getStyle(), size, aPGB.getColor(), aPGB.getBackColor());
                Rectangle2D rect = new Rectangle2D.Double(0, 0, size, size);
                g.setPaint(new TexturePaint(bi, rect));
                g.fill(path);
            } else {
                g.setColor(aColor);
                g.fill(path);
            }
        }

        if (aPGB.isDrawOutline()) {
            BasicStroke pen = new BasicStroke(aPGB.getOutlineSize());
            g.setStroke(pen);
            g.setColor(aPGB.getOutlineColor());
            g.draw(path);
        }

        return rPoints;
    }

    private void drawImage(Graphics2D g, Graphic igraphic, String zdir, float zValue) {
        ImageShape ishape = (ImageShape) igraphic.getShape();
        BufferedImage image = ishape.getImage();
        Extent3D ext = (Extent3D) ishape.getExtent();
        Point p1, p2, p3, p4;
        AffineTransform transform = new AffineTransform();
        transform.setToIdentity();
        float minx, miny, maxx, maxy, minz, maxz;
        double angle, xscale, yscale, xtran, ytran;
        switch (zdir) {
            case "x":
                /*zValue = (zValue - this.xmin) * xfactor - 10;
                miny = ((float) ext.minY - ymin) * yfactor - 10;
                maxy = ((float) ext.maxY - ymin) * yfactor - 10;
                minz = ((float) ext.minZ - zmin) * zfactor - 10;
                maxz = ((float) ext.maxZ - zmin) * zfactor - 10;
                p1 = this.projector.project(zValue, miny, maxz);
                p2 = this.projector.project(zValue, maxy, maxz);
                p3 = this.projector.project(zValue, maxy, minz);
                p4 = this.projector.project(zValue, miny, minz);*/
                p1 = this.project(zValue, (float)ext.minY, (float)ext.maxZ);
                p2 = this.project(zValue, (float)ext.maxY, (float)ext.maxZ);
                p3 = this.project(zValue, (float)ext.maxY, (float)ext.minZ);
                p4 = this.project(zValue, (float)ext.minY, (float)ext.minZ);
                xscale = (double) Math.abs(p2.x - p1.x) / image.getWidth();
                yscale = (double) Math.abs(p1.y - p4.y) / image.getHeight();
                xtran = p1.x;
                ytran = p1.y;
                if (p2.x > p1.x) {
                    angle = MIMath.cartesianToPolar(p2.x - p1.x, p1.y - p2.y)[0];
                } else {
                    angle = MIMath.cartesianToPolar(p1.x - p2.x, p2.y - p1.y)[0];
                    xscale = -xscale;
                }
                angle = -angle;
                transform.setTransform(Math.cos(angle), Math.sin(angle), 0, 1, xtran, ytran);
                transform.scale(xscale / Math.cos(angle), yscale);
                break;
            case "y":
                /*zValue = (zValue - this.ymin) * yfactor - 10;
                minx = ((float) ext.minX - xmin) * xfactor - 10;
                maxx = ((float) ext.maxX - xmin) * xfactor - 10;
                minz = ((float) ext.minZ - zmin) * zfactor - 10;
                maxz = ((float) ext.maxZ - zmin) * zfactor - 10;
                p1 = this.projector.project(minx, zValue, maxz);
                p2 = this.projector.project(maxx, zValue, maxz);
                p3 = this.projector.project(maxx, zValue, minz);
                p4 = this.projector.project(minx, zValue, minz);*/
                p1 = this.project((float)ext.minX, zValue, (float)ext.maxZ);
                p2 = this.project((float)ext.maxX, zValue, (float)ext.maxZ);
                p3 = this.project((float)ext.maxX, zValue, (float)ext.minZ);
                p4 = this.project((float)ext.minX, zValue, (float)ext.minZ);
                xscale = (double) Math.abs(p2.x - p1.x) / image.getWidth();
                yscale = (double) Math.abs(p1.y - p4.y) / image.getHeight();
                xtran = p1.x;
                ytran = p1.y;
                if (p2.x > p1.x) {
                    angle = MIMath.cartesianToPolar(p2.x - p1.x, p1.y - p2.y)[0];
                } else {
                    angle = MIMath.cartesianToPolar(p1.x - p2.x, p2.y - p1.y)[0];
                    xscale = -xscale;
                }
                angle = -angle;
                transform.setTransform(Math.cos(angle), Math.sin(angle), 0, 1, xtran, ytran);
                transform.scale(xscale / Math.cos(angle), yscale);
                break;
            case "xy":
                /*miny = ((float) ext.minY - ymin) * yfactor - 10;
                maxy = ((float) ext.maxY - ymin) * yfactor - 10;
                minx = ((float) ext.minX - xmin) * xfactor - 10;
                maxx = ((float) ext.maxX - xmin) * xfactor - 10;
                minz = ((float) ext.minZ - zmin) * zfactor - 10;
                maxz = ((float) ext.maxZ - zmin) * zfactor - 10;
                p1 = this.projector.project(minx, miny, maxz);
                p2 = this.projector.project(maxx, maxy, maxz);
                p3 = this.projector.project(maxx, maxy, minz);
                p4 = this.projector.project(minx, miny, minz);*/
                p1 = this.project((float)ext.minX, (float)ext.minY, (float)ext.maxZ);
                p2 = this.project((float)ext.maxX, (float)ext.maxY, (float)ext.maxZ);
                p3 = this.project((float)ext.maxX, (float)ext.maxY, (float)ext.minZ);
                p4 = this.project((float)ext.minX, (float)ext.minY, (float)ext.minZ);
                xscale = (double) Math.abs(p2.x - p1.x) / image.getWidth();
                yscale = (double) Math.abs(p1.y - p4.y) / image.getHeight();
                xtran = p1.x;
                ytran = p1.y;
                if (p2.x > p1.x) {
                    angle = MIMath.cartesianToPolar(p2.x - p1.x, p1.y - p2.y)[0];
                } else {
                    angle = MIMath.cartesianToPolar(p1.x - p2.x, p2.y - p1.y)[0];
                    xscale = -xscale;
                }
                angle = -angle;
                transform.setTransform(Math.cos(angle), Math.sin(angle), 0, 1, xtran, ytran);
                transform.scale(xscale / Math.cos(angle), yscale);
                break;
            case "z":
                /*zValue = (zValue - this.zmin) * zfactor - 10;
                minx = ((float) ext.minX - xmin) * xfactor - 10;
                maxx = ((float) ext.maxX - xmin) * xfactor - 10;
                miny = ((float) ext.minY - ymin) * yfactor - 10;
                maxy = ((float) ext.maxY - ymin) * yfactor - 10;
                p1 = this.projector.project(minx, maxy, zValue);
                p2 = this.projector.project(maxx, maxy, zValue);
                p3 = this.projector.project(maxx, miny, zValue);
                p4 = this.projector.project(minx, miny, zValue);*/
                p1 = this.project((float)ext.minX, (float)ext.maxY, zValue);
                p2 = this.project((float)ext.maxX, (float)ext.maxY, zValue);
                p3 = this.project((float)ext.maxX, (float)ext.minY, zValue);
                p4 = this.project((float)ext.minX, (float)ext.minY, zValue);
                xscale = (double) Math.abs(p2.x - p1.x) / image.getWidth();
                yscale = (double) Math.abs(p1.y - p4.y) / image.getHeight();
                xtran = p1.x;
                ytran = p1.y;
                angle = MIMath.cartesianToPolar(p2.x - p1.x, p1.y - p2.y)[0];
                double angle_y = MIMath.cartesianToPolar(p4.x - p1.x, p4.y - p1.y)[0];
                if (p2.x < p1.x) {
                    xscale = -xscale;
                    yscale = -yscale;
                }
                angle = -angle;
                angle_y = Math.PI * 0.5 - angle_y;
                angle_y = -angle_y;
                transform.setTransform(Math.cos(angle), Math.sin(angle), -Math.sin(angle_y), Math.cos(angle_y), xtran, ytran);
                transform.scale(xscale / Math.cos(angle), yscale / Math.cos(angle_y));
                break;
        }
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, ishape.getInterpolation());
        g.drawImage(image, transform, null);
    }

    private void drawWindArrow(Graphics2D g, Graphic graphic) {        
        if (extent.intersects(graphic.getExtent())) {
            WindArrow3D shape = (WindArrow3D) graphic.getShape();
            PointBreak pb = (PointBreak) graphic.getLegend();
            PointZ p = (PointZ) shape.getPoint();
            /*PointZ pp = new PointZ((p.X - xmin) * xfactor - 10, (p.Y - ymin) * yfactor - 10,
                    (p.Z - this.zmin) * zfactor - 10);
            projection = projector.project((float) pp.X, (float) pp.Y, (float) pp.Z);*/
            projection = this.project((float)p.X, (float)p.Y, (float)p.Z);
            PointF pf = new PointF(projection.x, projection.y);
            p = (PointZ)shape.getEndPoint();
            /*pp = new PointZ((p.X - xmin) * xfactor - 10, (p.Y - ymin) * yfactor - 10,
                    (p.Z - this.zmin) * zfactor - 10);
            projection = projector.project((float) pp.X, (float) pp.Y, (float) pp.Z);*/
            projection = this.project((float)p.X, (float)p.Y, (float)p.Z);
            PointF epf = new PointF(projection.x, projection.y);
            PointF[] points = new PointF[]{pf, epf};
            Draw.drawArrow(points, pb, 4, g);
        }
    }
    
    /**
     * Draws the base plane. The base plane is the x-y plane.
     *
     * @param g the graphics context to draw.
     * @param x used to retrieve x coordinates of drawn plane from this method.
     * @param y used to retrieve y coordinates of drawn plane from this method.
     */
    private void drawBase(Graphics g, int[] x, int[] y) {
        Point p = projector.project(-10, -10, -10);
        x[0] = p.x;
        y[0] = p.y;
        p = projector.project(-10, 10, -10);
        x[1] = p.x;
        y[1] = p.y;
        p = projector.project(10, 10, -10);
        x[2] = p.x;
        y[2] = p.y;
        p = projector.project(10, -10, -10);
        x[3] = p.x;
        y[3] = p.y;
        x[4] = x[0];
        y[4] = y[0];

        g.setColor(this.boxColor);
        g.fillPolygon(x, y, 4);

        g.setColor(this.lineboxColor);
        g.drawPolygon(x, y, 5);
    }

    /**
     * Draws string at the specified coordinates with the specified alignment.
     *
     * @param g graphics context to draw
     * @param x the x coordinate
     * @param y the y coordinate
     * @param s the string to draw
     * @param x_align the alignment in x direction
     * @param y_align the alignment in y direction
     */
    private void outString(Graphics g, int x, int y, String s, XAlign x_align, YAlign y_align) {
        switch (y_align) {
            case TOP:
                y += g.getFontMetrics(g.getFont()).getAscent();
                break;
            case CENTER:
                y += g.getFontMetrics(g.getFont()).getAscent() / 2;
                break;
        }
        switch (x_align) {
            case LEFT:
                g.drawString(s, x, y);
                break;
            case RIGHT:
                g.drawString(s, x - g.getFontMetrics(g.getFont()).stringWidth(s), y);
                break;
            case CENTER:
                g.drawString(s, x - g.getFontMetrics(g.getFont()).stringWidth(s) / 2, y);
                break;
        }
    }

    private void outString(Graphics2D g, int x, int y, String s, XAlign x_align, YAlign y_align, float angle) {
        AffineTransform tempTrans = g.getTransform();
        //AffineTransform myTrans = new AffineTransform();
        AffineTransform myTrans = (AffineTransform)tempTrans.clone();
        myTrans.translate(x, y);
        myTrans.rotate(-angle * Math.PI / 180);
        g.setTransform(myTrans);
        x = 0; y = 0;
        switch (y_align) {
            case TOP:
                y += g.getFontMetrics(g.getFont()).getAscent();
                break;
            case CENTER:
                y += g.getFontMetrics(g.getFont()).getAscent() / 2;
                break;
        }
        Dimension labSize = Draw.getStringDimension(s, g);
        switch (x_align) {
            case RIGHT:
                x = x - labSize.width;
                break;
            case CENTER:
                x = x - labSize.width / 2;
                break;
        }
        Draw.drawString(g, s, x, y);
        g.setTransform(tempTrans);
    }
    
    private void outString_bak(Graphics2D g, int x, int y, String s, XAlign x_align, YAlign y_align, float angle) {
        switch (y_align) {
            case TOP:
                y += g.getFontMetrics(g.getFont()).getAscent();
                break;
            case CENTER:
                y += g.getFontMetrics(g.getFont()).getAscent() / 2;
                break;
        }
        Dimension labSize = Draw.getStringDimension(s, g);
        switch (x_align) {
            case RIGHT:
                x = x - labSize.width;
                break;
            case CENTER:
                x = x - labSize.width / 2;
                break;
        }

        AffineTransform tempTrans = g.getTransform();
        //AffineTransform myTrans = new AffineTransform();
        AffineTransform myTrans = (AffineTransform)tempTrans.clone();
        myTrans.translate(x, y);
        myTrans.rotate(-angle * Math.PI / 180);
        g.setTransform(myTrans);
        x = 0; y = 0;
//        if (angle == 90) {
//            x = -(int) (labSize.getWidth() - 10);
//            y = (int) (labSize.getHeight() / 3);
//        } else {
//            x = -(int) (labSize.getWidth() - 5);
//            y = 0;
//        }
        Draw.drawString(g, s, x, y);
        g.setTransform(tempTrans);
    }

    /**
     * Sets the axes scaling factor. Computes the proper axis lengths based on
     * the ratio of variable ranges. The axis lengths will also affect the size
     * of bounding box.
     */
    private void setAxesScale() {
        float scale_x, scale_y, scale_z, divisor;
        int longest;

        if (!isScaleBox) {
            projector.setScaling(1);
            t_x = t_y = t_z = 4;
            return;
        }

        scale_x = xmax - xmin;
        scale_y = ymax - ymin;
        scale_z = zmax - zmin;

        if (scale_x < scale_y) {
            if (scale_y < scale_z) {
                longest = 3;
                divisor = scale_z;
            } else {
                longest = 2;
                divisor = scale_y;
            }
        } else if (scale_x < scale_z) {
            longest = 3;
            divisor = scale_z;
        } else {
            longest = 1;
            divisor = scale_x;
        }
        scale_x /= divisor;
        scale_y /= divisor;
        scale_z /= divisor;

        if ((scale_x < 0.2f) || (scale_y < 0.2f) && (scale_z < 0.2f)) {
            switch (longest) {
                case 1:
                    if (scale_y < scale_z) {
                        scale_y /= scale_z;
                        scale_z = 1.0f;
                    } else {
                        scale_z /= scale_y;
                        scale_y = 1.0f;
                    }
                    break;
                case 2:
                    if (scale_x < scale_z) {
                        scale_x /= scale_z;
                        scale_z = 1.0f;
                    } else {
                        scale_z /= scale_x;
                        scale_x = 1.0f;
                    }
                    break;
                case 3:
                    if (scale_y < scale_x) {
                        scale_y /= scale_x;
                        scale_x = 1.0f;
                    } else {
                        scale_x /= scale_y;
                        scale_y = 1.0f;
                    }
                    break;
            }
        }
        if (scale_x < 0.2f) {
            scale_x = 1.0f;
        }
        projector.setXScaling(scale_x);
        if (scale_y < 0.2f) {
            scale_y = 1.0f;
        }
        projector.setYScaling(scale_y);
        if (scale_z < 0.2f) {
            scale_z = 1.0f;
        }
        projector.setZScaling(scale_z);

        if (scale_x < 0.5f) {
            t_x = 8;
        } else {
            t_x = 4;
        }
        if (scale_y < 0.5f) {
            t_y = 8;
        } else {
            t_y = 4;
        }
        if (scale_z < 0.5f) {
            t_z = 8;
        } else {
            t_z = 4;
        }
    }

    /**
     * Draws float at the specified coordinates with the specified alignment.
     *
     * @param g graphics context to draw
     * @param x the x coordinate
     * @param y the y coordinate
     * @param f the float to draw
     * @param x_align the alignment in x direction
     * @param y_align the alignment in y direction
     */
    private void outFloat(Graphics g, int x, int y, float f, XAlign x_align, YAlign y_align) {
        // String s = Float.toString(f);
        String s = format(f);
        outString(g, x, y, s, x_align, y_align);
    }

    private String format(float f) {
        return String.format("%.3G", f);
    }

    private void drawAxes(Graphics2D g) {
        int x[], y[], i;
        x = new int[5];
        y = new int[5];
        drawBase(g, x, y);
        projection = projector.project(0, 0, -10);
        x[0] = projection.x;
        y[0] = projection.y;
        projection = projector.project(10.5f, 0, -10);
        g.drawLine(x[0], y[0], projection.x, projection.y);
        if (projection.x < x[0]) {
            outString(g, (int) (1.05 * (projection.x - x[0])) + x[0], (int) (1.05 * (projection.y - y[0])) + y[0], "x", XAlign.RIGHT, YAlign.TOP);
        } else {
            outString(g, (int) (1.05 * (projection.x - x[0])) + x[0], (int) (1.05 * (projection.y - y[0])) + y[0], "x", XAlign.LEFT, YAlign.TOP);
        }
        projection = projector.project(0, 11.5f, -10);
        g.drawLine(x[0], y[0], projection.x, projection.y);
        if (projection.x < x[0]) {
            outString(g, (int) (1.05 * (projection.x - x[0])) + x[0], (int) (1.05 * (projection.y - y[0])) + y[0], "y", XAlign.RIGHT, YAlign.TOP);
        } else {
            outString(g, (int) (1.05 * (projection.x - x[0])) + x[0], (int) (1.05 * (projection.y - y[0])) + y[0], "y", XAlign.LEFT, YAlign.TOP);
        }
        projection = projector.project(0, 0, 10.5f);
        g.drawLine(x[0], y[0], projection.x, projection.y);
        outString(g, (int) (1.05 * (projection.x - x[0])) + x[0], (int) (1.05 * (projection.y - y[0])) + y[0], "z", XAlign.CENTER, YAlign.CENTER);
    }

    private int getLabelGap(Graphics2D g, List<ChartText> labels, double len) {
        int n = labels.size();
        int nn;
        FontMetrics metrics = g.getFontMetrics();
        nn = (int) (len / metrics.getHeight());
        if (nn == 0) {
            nn = 1;
        }
        return n / nn + 1;
    }

    private java.awt.Polygon getBorder() {
        Point p;
        java.awt.Polygon polygon = new java.awt.Polygon();
        float elevation = this.projector.getElevationAngle();
        float rotation = this.projector.getRotationAngle();
        if (elevation == 0 && ((rotation > 90 && rotation < 180) || (rotation > 270 && rotation < 360))) {
            Rectangle rect = this.projector.getBounds();
            polygon.addPoint(rect.x, rect.y);
            polygon.addPoint(rect.x, rect.y + rect.height);
            polygon.addPoint(rect.x + rect.width, rect.y + rect.height);
            polygon.addPoint(rect.x + rect.width, rect.y);
        } else {
            p = projector.project(factor_x * 10, -factor_y * 10, -10);
            polygon.addPoint(p.x, p.y);
            p = projector.project(factor_x * 10, -factor_y * 10, 10);
            polygon.addPoint(p.x, p.y);
            p = projector.project(-factor_x * 10, -factor_y * 10, 10);
            polygon.addPoint(p.x, p.y);
            p = projector.project(-factor_x * 10, factor_y * 10, 10);
            polygon.addPoint(p.x, p.y);
            p = projector.project(-factor_x * 10, factor_y * 10, -10);
            polygon.addPoint(p.x, p.y);
            p = projector.project(factor_x * 10, factor_y * 10, -10);
            polygon.addPoint(p.x, p.y);
        }

        return polygon;
    }

    /**
     * Draws bounding box, axis grids, axis ticks, axis labels, base plane.
     *
     * @param g the graphics context to draw
     */
    private void drawBoxGridsTicksLabels(Graphics2D g) {
        Point tickpos;
        boolean x_left, y_left;
        int x[], y[], i;

        x = new int[5];
        y = new int[5];
        if (projector == null) {
            return;
        }

        factor_x = factor_y = 1;
        projection = projector.project(0, 0, -10);
        x[0] = projection.x;
        projection = projector.project(10.5f, 0, -10);
        y_left = projection.x > x[0];
        i = projection.y;
        projection = projector.project(-10.5f, 0, -10);
        if (projection.y > i) {
            factor_x = -1;
            y_left = projection.x > x[0];
        }
        projection = projector.project(0, 10.5f, -10);
        x_left = projection.x > x[0];
        i = projection.y;
        projection = projector.project(0, -10.5f, -10);
        if (projection.y > i) {
            factor_y = -1;
            x_left = projection.x > x[0];
        }
        setAxesScale();

        //Draw base area
        if (this.drawBase)
            drawBase(g, x, y);

        //Draw box
        if (isBoxed) {
            projection = projector.project(-factor_x * 10, -factor_y * 10, -10);
            x[0] = projection.x;
            y[0] = projection.y;
            projection = projector.project(-factor_x * 10, -factor_y * 10, 10);
            x[1] = projection.x;
            y[1] = projection.y;
            projection = projector.project(factor_x * 10, -factor_y * 10, 10);
            x[2] = projection.x;
            y[2] = projection.y;
            projection = projector.project(factor_x * 10, -factor_y * 10, -10);
            x[3] = projection.x;
            y[3] = projection.y;
            x[4] = x[0];
            y[4] = y[0];

            g.setColor(this.boxColor);
            g.fillPolygon(x, y, 4);

            g.setColor(this.lineboxColor);
            g.drawPolygon(x, y, 5);

            projection = projector.project(-factor_x * 10, factor_y * 10, 10);
            x[2] = projection.x;
            y[2] = projection.y;
            projection = projector.project(-factor_x * 10, factor_y * 10, -10);
            x[3] = projection.x;
            y[3] = projection.y;
            x[4] = x[0];
            y[4] = y[0];

            g.setColor(this.boxColor);
            g.fillPolygon(x, y, 4);

            g.setColor(this.lineboxColor);
            g.drawPolygon(x, y, 5);
        } /*else if (isDisplayZ) {
            projection = projector.project(factor_x * 10, -factor_y * 10, -10);
            x[0] = projection.x;
            y[0] = projection.y;
            projection = projector.project(factor_x * 10, -factor_y * 10, 10);
            g.drawLine(x[0], y[0], projection.x, projection.y);

            projection = projector.project(-factor_x * 10, factor_y * 10, -10);
            x[0] = projection.x;
            y[0] = projection.y;
            projection = projector.project(-factor_x * 10, factor_y * 10, 10);
            g.drawLine(x[0], y[0], projection.x, projection.y);
        }*/

        //Draw axis
        float v, vi;
        String s;
        double[] value;
        float angle, xangle, yangle, xlen, ylen;
        int skip;
        if (this.isDisplayXY) {
            //Draw x/y axis lines
            //x axis line
            projection = projector.project(-10, factor_y * 10, -10);
            x[0] = projection.x;
            y[0] = projection.y;
            projection = projector.project(10, factor_y * 10, -10);
            g.setColor(this.xAxis.getLineColor());
            g.drawLine(x[0], y[0], projection.x, projection.y);
            if (projection.x > x[0]) {
                value = DataMath.getDSFromUV(projection.x - x[0], projection.y - y[0]);
            } else {
                value = DataMath.getDSFromUV(x[0] - projection.x, y[0] - projection.y);
            }
            xangle = (float) value[0];
            xlen = (float) value[1];

            //yaxis line            
            projection = projector.project(factor_x * 10, -10, -10);
            x[0] = projection.x;
            y[0] = projection.y;
            projection = projector.project(factor_x * 10, 10, -10);
            g.setColor(this.yAxis.getLineColor());
            g.drawLine(x[0], y[0], projection.x, projection.y);
            if (projection.x > x[0]) {
                value = DataMath.getDSFromUV(projection.x - x[0], projection.y - y[0]);
            } else {
                value = DataMath.getDSFromUV(x[0] - projection.x, y[0] - projection.y);
            }
            yangle = (float) value[0];
            ylen = (float) value[1];

            //Draw x ticks
            if (x_left) {
                angle = yangle;
            } else {
                angle = yangle + 180;
                if (angle > 360) {
                    angle -= 360;
                }
            }
            g.setFont(this.xAxis.getTickLabelFont());
            this.xAxis.updateTickLabels();      
            List<ChartText> tlabs = this.xAxis.getTickLabels();
            skip = getLabelGap(g, tlabs, Math.abs(xlen));
            int strWidth = 0, w;
            for (i = 0; i < this.xAxis.getTickValues().length; i += skip) {
                v = (float) this.xAxis.getTickValues()[i];
                if (i == tlabs.size()) {
                    break;
                }
                s = tlabs.get(i).getText();
                if (v < xmin || v > xmax) {
                    continue;
                }
                //vi = (v - xmin) * xfactor - 10;
                //tickpos = projector.project(vi, factor_y * 10, -10);
                tickpos = this.project(v, factor_y > 0 ? this.ymax : this.ymin, this.zmin);
                if (this.isDisplayGrids && (v != xmin && v != xmax)) {
                    //projection = projector.project(vi, -factor_y * 10, -10);
                    projection = this.project(v, factor_y < 0 ? this.ymax : this.ymin, this.zmin);
                    g.setColor(this.lineboxColor);
                    g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
                    if (this.isDisplayZ && this.isBoxed) {
                        x[0] = projection.x;
                        y[0] = projection.y;
                        //projection = projector.project(vi, -factor_y * 10, 10);
                        projection = this.project(v, factor_y < 0 ? this.ymax : this.ymin, this.zmax);
                        g.drawLine(x[0], y[0], projection.x, projection.y);
                    }
                }
                //projection = projector.project(vi, factor_y * 10.5f, -10);                
                value = DataMath.getEndPoint(tickpos.x, tickpos.y, angle, this.xAxis.getTickLength());
                g.setColor(this.xAxis.getLineColor());
                //g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
                g.drawLine(tickpos.x, tickpos.y, (int) value[0], (int) value[1]);
                value = DataMath.getEndPoint(tickpos.x, tickpos.y, angle, this.xAxis.getTickLength() + 5);
                tickpos = new Point((int) value[0], (int) value[1]);                
                if (x_left) {
                    //outString(g, tickpos.x, tickpos.y, s, XAlign.LEFT, YAlign.TOP);
                    Draw.drawString(g, tickpos.x, tickpos.y, s, XAlign.LEFT, YAlign.TOP, true);
                } else {
                    //outString(g, tickpos.x, tickpos.y, s, XAlign.RIGHT, YAlign.TOP);
                    Draw.drawString(g, tickpos.x, tickpos.y, s, XAlign.RIGHT, YAlign.TOP, true);
                }
                //w = g.getFontMetrics().stringWidth(s);
                w = Draw.getStringDimension(s, g).width;
                if (strWidth < w) {
                    strWidth = w;
                }
            }
            String label = this.xAxis.getLabel().getText();
            if (label != null) {
                g.setFont(this.xAxis.getLabelFont());
                g.setColor(this.xAxis.getLabelColor());
                tickpos = projector.project(0, factor_y * 10.f, -10);
                Dimension dim = Draw.getStringDimension(label, g);
                strWidth = (int) Math.abs((strWidth * Math.sin(Math.toRadians(angle))));
                value = DataMath.getEndPoint(tickpos.x, tickpos.y, angle, this.xAxis.getTickLength() + strWidth + dim.height + 5);
                tickpos.x = (int) value[0];
                tickpos.y = (int) value[1];
                if (this.projector.getElevationAngle() < 10) {
                    tickpos.y += g.getFontMetrics().getHeight();
                }
                if (x_left) {
                    //outString(g, tickpos.x, tickpos.y, label, XAlign.CENTER, YAlign.TOP, xangle + 90);
                    Draw.drawString(g, tickpos.x, tickpos.y, label, XAlign.CENTER, YAlign.TOP, xangle + 90, true);
                } else {
                    //outString(g, tickpos.x, tickpos.y, label, XAlign.CENTER, YAlign.TOP, xangle + 90);
                    Draw.drawString(g, tickpos.x, tickpos.y, label, XAlign.CENTER, YAlign.TOP, xangle + 90, true);
                }
            }

            //Draw y ticks   
            if (y_left) {
                angle = xangle;
            } else {
                angle = xangle + 180;
                if (angle > 360) {
                    angle -= 360;
                }
            }
            g.setFont(this.yAxis.getTickLabelFont());
            this.yAxis.updateTickLabels();
            tlabs = this.yAxis.getTickLabels();
            skip = getLabelGap(g, tlabs, Math.abs(ylen));
            strWidth = 0;
            for (i = 0; i < this.yAxis.getTickValues().length; i += skip) {
                if (i >= tlabs.size())
                    break;

                v = (float) this.yAxis.getTickValues()[i];
                s = tlabs.get(i).getText();
                if (v < ymin || v > ymax) {
                    continue;
                }
                //vi = (v - ymin) * yfactor - 10;
                //tickpos = projector.project(factor_x * 10, vi, -10);
                tickpos = this.project(factor_x > 0 ? this.xmax : this.xmin, v, this.zmin);
                if (this.isDisplayGrids && (v != ymin && v != ymax)) {
                    //projection = projector.project(-factor_x * 10, vi, -10);
                    projection = this.project(factor_x < 0 ? this.xmax : this.xmin, v, this.zmin);
                    g.setColor(this.lineboxColor);
                    g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
                    if (this.isDisplayZ && this.isBoxed) {
                        x[0] = projection.x;
                        y[0] = projection.y;
                        //projection = projector.project(-factor_x * 10, vi, 10);
                        projection = this.project(factor_x < 0 ? this.xmax : this.xmin, v, this.zmax);
                        g.drawLine(x[0], y[0], projection.x, projection.y);
                    }
                }
                value = DataMath.getEndPoint(tickpos.x, tickpos.y, angle, this.xAxis.getTickLength());
                g.setColor(this.yAxis.getLineColor());
                g.drawLine(tickpos.x, tickpos.y, (int) value[0], (int) value[1]);
                value = DataMath.getEndPoint(tickpos.x, tickpos.y, angle, this.xAxis.getTickLength() + 5);
                tickpos = new Point((int) value[0], (int) value[1]);
                if (y_left) {
                    //outString(g, tickpos.x, tickpos.y, s, XAlign.LEFT, YAlign.TOP);
                    Draw.drawString(g, tickpos.x, tickpos.y, s, XAlign.LEFT, YAlign.TOP, true);
                } else {
                    //outString(g, tickpos.x, tickpos.y, s, XAlign.RIGHT, YAlign.TOP);
                    Draw.drawString(g, tickpos.x, tickpos.y, s, XAlign.RIGHT, YAlign.TOP, true);
                }
                //w = g.getFontMetrics().stringWidth(s);
                w = Draw.getStringDimension(s, g).width;
                if (strWidth < w) {
                    strWidth = w;
                }
            }
            label = this.yAxis.getLabel().getText();
            if (label != null) {
                g.setFont(this.yAxis.getLabelFont());
                g.setColor(this.yAxis.getLabelColor());
                tickpos = projector.project(factor_x * 10.f, 0, -10);
                Dimension dim = Draw.getStringDimension(label, g);
                strWidth = (int) Math.abs((strWidth * Math.sin(Math.toRadians(angle))));
                value = DataMath.getEndPoint(tickpos.x, tickpos.y, angle, this.yAxis.getTickLength() + strWidth + dim.height + 5);
                tickpos.x = (int) value[0];
                tickpos.y = (int) value[1];
                if (this.projector.getElevationAngle() < 10) {
                    tickpos.y += g.getFontMetrics().getHeight();
                }
                if (y_left) {
                    //outString(g, tickpos.x, tickpos.y, label, XAlign.CENTER, YAlign.TOP, yangle + 90);
                    Draw.drawString(g, tickpos.x, tickpos.y, label, XAlign.CENTER, YAlign.TOP, yangle + 90, true);
                } else {
                    //outString(g, tickpos.x, tickpos.y, label, XAlign.CENTER, YAlign.TOP, yangle + 90);
                    Draw.drawString(g, tickpos.x, tickpos.y, label, XAlign.CENTER, YAlign.TOP, yangle + 90, true);
                }
            }
        }

        //Draw z axis
        if (this.isDisplayZ) {
            float lf = 1;
            if (y_left) {
                lf = -1;
            }
            projection = projector.project(factor_x * 10 * lf, -factor_y * 10 * lf, -10);
            x[0] = projection.x;
            y[0] = projection.y;
            projection = projector.project(factor_x * 10 * lf, -factor_y * 10 * lf, 10);
            g.setFont(this.zAxis.getTickLabelFont());
            g.setColor(this.zAxis.getLineColor());
            g.drawLine(x[0], y[0], projection.x, projection.y);
            this.zAxis.updateTickLabels();
            List<ChartText> tlabs = this.zAxis.getTickLabels();
            int len = Math.abs(y[0] - projection.y);
            skip = getLabelGap(g, tlabs, len);
            int strWidth = 0, w;
            for (i = 0; i < this.zAxis.getTickValues().length; i += skip) {
                v = (float) this.zAxis.getTickValues()[i];
                s = tlabs.get(i).getText();
                if (v < zmin || v > zmax) {
                    continue;
                }
                //vi = (v - zmin) * zfactor - 10;
                //tickpos = projector.project(factor_x * 10 * lf, -factor_y * 10 * lf, vi);
                tickpos = this.project(factor_x * lf > 0 ? this.xmax : this.xmin,
                        factor_y * lf < 0 ? this.ymax : this.ymin, v);
                if (this.isDisplayGrids && this.isBoxed && (v != zmin && v != zmax)) {
                    //projection = projector.project(-factor_x * 10, -factor_y * 10, vi);
                    projection = this.project(factor_x < 0 ? this.xmax : this.xmin,
                            factor_y < 0 ? this.ymax : this.ymin, v);
                    g.setColor(this.lineboxColor);
                    g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
                    x[0] = projection.x;
                    y[0] = projection.y;
                    //projection = projector.project(-factor_x * 10 * lf, factor_y * 10 * lf, vi);
                    projection = this.project(factor_x * lf < 0 ? this.xmax : this.xmin,
                            factor_y * lf > 0 ? this.ymax : this.ymin, v);
                    g.drawLine(x[0], y[0], projection.x, projection.y);
                }
                //projection = projector.project(factor_x * 10.2f * lf, -factor_y * 10.2f * lf, vi);
                g.setColor(this.zAxis.getLineColor());
                //g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
                g.drawLine(tickpos.x, tickpos.y, tickpos.x - (int)this.zAxis.getTickLength(), tickpos.y);
                //tickpos = projector.project(factor_x * 10.5f * lf, -factor_y * 10.5f * lf, vi);
                //outString(g, tickpos.x - this.zAxis.getTickLength() - 5, tickpos.y, s, XAlign.RIGHT, YAlign.CENTER);
                Draw.drawString(g, tickpos.x - this.zAxis.getTickLength() - 5, tickpos.y, s, XAlign.RIGHT, YAlign.CENTER, true);
                w = g.getFontMetrics().stringWidth(s);
                if (strWidth < w) {
                    strWidth = w;
                }
            }
            String label = this.zAxis.getLabel().getText();
            if (label != null) {
                tickpos = projector.project(factor_x * 10 * lf, -factor_y * 10 * lf, 0);
                tickpos.x = tickpos.x - (int)this.xAxis.getTickLength() - 15 - strWidth;
                g.setFont(this.zAxis.getLabelFont());
                g.setColor(this.zAxis.getLabelColor());
                //Draw.drawLabelPoint_270(tickpos.x, tickpos.y, this.zAxis.getLabelFont(), label,
                //        this.zAxis.getLabelColor(), g, null, this.zAxis.getLabel().isUseExternalFont());              
                Draw.drawString(g, tickpos.x, tickpos.y, label, XAlign.CENTER, YAlign.BOTTOM, 90,
                        this.zAxis.getLabel().isUseExternalFont());
            }
        }
    }

    /**
     * Draws the bounding box of surface.
     */
    private void drawBoundingBox(Graphics2D g2) {
        Point startingpoint;

        startingpoint = projector.project(factor_x * 10, factor_y * 10, 10);
        g2.setColor(this.lineboxColor);
        projection = projector.project(-factor_x * 10, factor_y * 10, 10);
        g2.drawLine(startingpoint.x, startingpoint.y, projection.x, projection.y);
        projection = projector.project(factor_x * 10, -factor_y * 10, 10);
        g2.drawLine(startingpoint.x, startingpoint.y, projection.x, projection.y);
        projection = projector.project(factor_x * 10, factor_y * 10, -10);
        g2.drawLine(startingpoint.x, startingpoint.y, projection.x, projection.y);
    }

    /**
     * Get tight inset area
     *
     * @param g Graphics2D
     * @param positionArea Position area
     * @return Tight inset area
     */
    @Override
    public Margin getTightInset(Graphics2D g, Rectangle2D positionArea) {
        int left = 2, bottom = 2, right = 2, top = 5;
        int space = 2;

        if (this.title != null) {
            top += this.title.getTrueDimension(g).height + 10;
        }

        if (!this.legends.isEmpty()) {
            ChartLegend legend = this.getLegend();
            Dimension dim = legend.getLegendDimension(g, new Dimension((int) positionArea.getWidth(),
                    (int) positionArea.getHeight()));
            switch (legend.getPosition()) {
                case UPPER_CENTER_OUTSIDE:
                    top += dim.height + 10;
                    break;
                case LOWER_CENTER_OUTSIDE:
                    bottom += dim.height + legend.getYShift() + 10;
                    break;
                case LEFT_OUTSIDE:
                    left += dim.width + 10;
                    break;
                case RIGHT_OUTSIDE:
                    right += dim.width + legend.getXShift() + 10;
                    break;
            }
        }

        //Get x axis space
        //bottom += this.getXAxisHeight(g, space);
        //Get y axis space
        //left += this.getYAxisWidth(g, space);
        //Set right space
//        if (this.getXAxis().isVisible()) {
//            if (this.getXAxis().isDrawTickLabel()) {
//                right += this.getXAxis().getMaxLabelLength(g) / 2;
//            }
//        }
        return new Margin(left, right, top, bottom);
    }

    void drawLegend(Graphics2D g, Rectangle2D area, Rectangle2D graphArea, float y) {
        if (!this.legends.isEmpty()) {
            Object rendering = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (ChartLegend legend : this.legends) {
                if (legend.isColorbar()) {
                    if (legend.getPlotOrientation() == PlotOrientation.VERTICAL) {
                        legend.setHeight((int) (graphArea.getHeight() * legend.getShrink()));
                    } else {
                        legend.setWidth((int) (graphArea.getWidth() * legend.getShrink()));
                    }
                }
                if (legend.getPosition() == LegendPosition.CUSTOM) {
                    legend.getLegendDimension(g, new Dimension((int) area.getWidth(), (int) area.getHeight()));
                    float x = (float) (area.getWidth() * legend.getX());
                    y = (float) (area.getHeight() * (1 - (this.getLegend().getHeight() / area.getHeight())
                            - this.getLegend().getY()));
                    legend.draw(g, new PointF(x, y));
                } else {
                    this.drawLegendScheme(legend, g, graphArea, y);
                }
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, rendering);
        }
    }

    void drawLegendScheme(ChartLegend legend, Graphics2D g, Rectangle2D area, float y) {
        g.setStroke(new BasicStroke(1));
        g.setFont(legend.getTickLabelFont());
        Dimension dim = legend.getLegendDimension(g, new Dimension((int) area.getWidth(), (int) area.getHeight()));
        float x = 0;
        //Rectangle2D graphArea = this.getPositionArea();
        switch (legend.getPosition()) {
            case UPPER_CENTER_OUTSIDE:
                x = (float) (area.getX() + area.getWidth() / 2 - dim.width / 2);
                y += 5;
                break;
            case LOWER_CENTER_OUTSIDE:
                x = (float) (area.getX() + area.getWidth() / 2 - dim.width / 2);
                y = (float) (area.getY() + area.getHeight() + 10);
                break;
            case LEFT_OUTSIDE:
                x = 10;
                y = (float) area.getHeight() / 2 - dim.height / 2;
                break;
            case RIGHT_OUTSIDE:
                x = (float) area.getX() + (float) area.getWidth() + 10 + 40;
                y = (float) area.getY() + (float) area.getHeight() / 2 - dim.height / 2;
                break;
            case UPPER_CENTER:
                x = (float) (area.getX() + area.getWidth() / 2 - dim.width / 2);
                y = (float) area.getY() + 10;
                break;
            case UPPER_RIGHT:
                x = (float) (area.getX() + area.getWidth()) - dim.width - 10;
                y = (float) area.getY() + 10;
                break;
            case LOWER_CENTER:
                x = (float) (area.getX() + area.getWidth() / 2 - dim.width / 2);
                y = (float) (area.getY() + area.getHeight()) - dim.height - 10;
                break;
            case LOWER_RIGHT:
                x = (float) (area.getX() + area.getWidth()) - dim.width - 10;
                y = (float) (area.getY() + area.getHeight()) - dim.height - 10;
                break;
            case UPPER_LEFT:
                x = (float) area.getX() + 10;
                y = (float) area.getY() + 10;
                break;
            case LOWER_LEFT:
                x = (float) area.getX() + 10;
                y = (float) (area.getY() + area.getHeight()) - dim.height - 10;
                break;
        }
        legend.draw(g, new PointF(x, y));
    }

    // </editor-fold>
}
