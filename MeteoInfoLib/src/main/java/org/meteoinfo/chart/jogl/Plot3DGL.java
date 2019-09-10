/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.jogl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.glu.GLU;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import org.meteoinfo.chart.ChartLegend;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.chart.Margin;
import org.meteoinfo.chart.axis.Axis;
import org.meteoinfo.chart.plot.Plot;
import org.meteoinfo.chart.plot.PlotType;
import org.meteoinfo.chart.plot3d.GraphicCollection3D;
import org.meteoinfo.data.Dataset;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.Extent3D;
import org.meteoinfo.global.PointF;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.PointZ;
import org.meteoinfo.shape.PointZShape;
import org.meteoinfo.shape.PolygonZ;
import org.meteoinfo.shape.PolygonZShape;
import org.meteoinfo.shape.Polyline;
import org.meteoinfo.shape.PolylineZShape;
import org.meteoinfo.shape.Shape;
import static org.meteoinfo.shape.ShapeTypes.PointZ;

/**
 *
 * @author wyq
 */
public class Plot3DGL extends Plot implements GLEventListener {

    // <editor-fold desc="Variables">
    private GLU glu = new GLU();
    private final GraphicCollection3D graphics;
    private Extent3D extent;
    private ChartText title;
    private List<ChartLegend> legends;
    private final Axis xAxis;
    private final Axis yAxis;
    private final Axis zAxis;
    private float xmin, xmax, ymin;
    private float ymax, zmin, zmax = 1.0f;
    private float angleX = 0.0f;
    private float angleY = 0.0f;
    private float distanceX = 0.0f;
    private float distanceY = 0.0f;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public Plot3DGL() {
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
        this.graphics = new GraphicCollection3D();
    }

    // </editor-fold>
    // <editor-fold desc="GetSet">
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
    }

    /**
     * Get x rotate angle
     *
     * @return X rotate angle
     */
    public float getAngleX() {
        return this.angleX;
    }

    /**
     * Set x rotate angle
     *
     * @param value X rotate angle
     */
    public void setAngleX(float value) {
        this.angleX = value;
    }

    /**
     * Get y rotate angle
     *
     * @return Y rotate angle
     */
    public float getAngleY() {
        return this.angleY;
    }

    /**
     * Set y rotate angle
     *
     * @param value Y rotate angle
     */
    public void setAngleY(float value) {
        this.angleY = value;
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
     * Get y axis
     *
     * @return Y axis
     */
    public Axis getYAxis() {
        return this.yAxis;
    }

    /**
     * Get z axis
     *
     * @return Z axis
     */
    public Axis getZAxis() {
        return this.zAxis;
    }

    /**
     * Get x minimum
     *
     * @return X minimum
     */
    public float getXMin() {
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
     *
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
     *
     * @return Y minimum
     */
    public float getYMin() {
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
     *
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
     *
     * @return Z minimum
     */
    public float getZMin() {
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
     *
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

    // </editor-fold>
    // <editor-fold desc="methods">
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

    }

    private void updateExtent() {
        this.extent = new Extent3D(xmin, xmax, ymin, ymax, zmin, zmax);
    }

    /**
     * Add a graphic
     *
     * @param g Grahic
     */
    public void addGraphic(Graphic g) {
        this.graphics.add(g);
        Extent ex = this.graphics.getExtent();
        if (!ex.is3D()) {
            ex = ex.to3D();
        }
        this.setExtent((Extent3D) ex);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glRotatef(-45.f + angleX, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(45.f, 0.0f, 0.0f, 1.0f);
        //gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
        //gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
        gl.glRotatef(angleY, 0.0f, 0.0f, 1.0f);
        gl.glColor3f(0.0f, 0.0f, 0.0f);

        //Draw box
        drawBoxGridsTicksLabels(gl);

        //Draw graphics
        gl.glClipPlanef(GL2.GL_CLIP_PLANE0, new float[]{1, 0, 0, 1}, 0);
        gl.glEnable(GL2.GL_CLIP_PLANE0);
        gl.glClipPlanef(GL2.GL_CLIP_PLANE1, new float[]{-1, 0, 0, 1}, 0);
        gl.glEnable(GL2.GL_CLIP_PLANE1);
        gl.glClipPlanef(GL2.GL_CLIP_PLANE2, new float[]{0, -1, 0, 1}, 0);
        gl.glEnable(GL2.GL_CLIP_PLANE2);
        gl.glClipPlanef(GL2.GL_CLIP_PLANE3, new float[]{0, 1, 0, 1}, 0);
        gl.glEnable(GL2.GL_CLIP_PLANE3);
        gl.glClipPlanef(GL2.GL_CLIP_PLANE4, new float[]{0, 0, 1, 1}, 0);
        gl.glEnable(GL2.GL_CLIP_PLANE4);
        gl.glClipPlanef(GL2.GL_CLIP_PLANE5, new float[]{0, 0, -1, 1}, 0);
        gl.glEnable(GL2.GL_CLIP_PLANE5);
        for (int m = 0; m < this.graphics.getNumGraphics(); m++) {
            Graphic graphic = this.graphics.get(m);
            drawGraphics(gl, graphic);
        }
        gl.glDisable(GL2.GL_CLIP_PLANE0);
        gl.glDisable(GL2.GL_CLIP_PLANE1);
        gl.glDisable(GL2.GL_CLIP_PLANE2);
        gl.glDisable(GL2.GL_CLIP_PLANE3);
        gl.glDisable(GL2.GL_CLIP_PLANE4);
        gl.glDisable(GL2.GL_CLIP_PLANE5);

        gl.glFlush();
    }

    private void drawBoxGridsTicksLabels(GL2 gl) {
        gl.glColor3f(0.0f, 0.0f, 0.0f);
        gl.glLineWidth(1.0f);
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(1f, 1f, -1f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glEnd();
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);
        gl.glEnd();
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glEnd();
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glVertex3f(1f, 1f, -1f);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(1f, 1f, -1f);
        gl.glVertex3f(1f, -1f, -1f);
        gl.glEnd();
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(1f, -1f, -1f);
        gl.glVertex3f(1f, -1f, 1f);
        gl.glEnd();
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(1f, -1f, 1f);
        gl.glVertex3f(1f, 1f, 1f);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(-1f, 1f, -1f);
        gl.glVertex3f(-1f, -1f, -1f);
        gl.glEnd();
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(-1f, -1f, -1f);
        gl.glVertex3f(1f, -1f, -1f);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(-1f, 1f, 1f);
        gl.glVertex3f(-1f, -1f, 1f);
        gl.glEnd();
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(-1f, -1f, 1f);
        gl.glVertex3f(-1f, -1f, -1f);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(-1f, -1f, 1f);
        gl.glVertex3f(1f, -1f, 1f);
        gl.glEnd();

        gl.glColor3f(1.0f, 0.0f, 0.0f);
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(0f, 0f, -1f);
        gl.glVertex3f(0f, 0f, 0f);
        gl.glEnd();
    }

    private void drawGraphics(GL2 gl, Graphic graphic) {
        if (graphic.getNumGraphics() == 1) {
            Graphic gg = graphic.getGraphicN(0);
            this.drawGraphic(gl, gg);
        } else {
            boolean isDraw = true;
            if (graphic instanceof GraphicCollection3D) {
                GraphicCollection3D gg = (GraphicCollection3D) graphic;
                if (gg.isAllQuads()) {
                    this.drawQuadsPolygons(gl, gg);
                    isDraw = false;
                }
            }
            if (isDraw) {
                for (int i = 0; i < graphic.getNumGraphics(); i++) {
                    Graphic gg = graphic.getGraphicN(i);
                    this.drawGraphic(gl, gg);
                }
            }
        }
    }

    private void drawGraphic(GL2 gl, Graphic graphic) {
        Shape shape = graphic.getGraphicN(0).getShape();
        switch (shape.getShapeType()) {
            case Point:
            case PointZ:
                this.drawPoint(gl, graphic);
                break;
            case TEXT:
                //this.drawText((ChartText3D) shape, g);
                break;
            case Polyline:
            case PolylineZ:
                this.drawLineString(gl, graphic);
                break;
            case Polygon:
            case PolygonZ:
                this.drawPolygonShape(gl, graphic);
                break;
            case WindArraw:
                //this.drawWindArrow(g, graphic);
                break;
            case Image:

                break;
        }
    }

    private void drawPoint(GL2 gl, Graphic graphic) {
        if (extent.intersects(graphic.getExtent())) {
            PointZShape shape = (PointZShape) graphic.getShape();
            PointBreak pb = (PointBreak) graphic.getLegend();
            float[] rgba = pb.getColor().getRGBComponents(null);
            gl.glColor3f(rgba[0], rgba[1], rgba[2]);
            gl.glPointSize(pb.getSize());
            gl.glBegin(GL2.GL_POINTS);
            PointZ p = (PointZ) shape.getPoint();
            gl.glVertex3f(transform_x((float) p.X), transform_y((float) p.Y), transform_z((float) p.Z));
            gl.glEnd();
        }
    }

    private void drawLineString(GL2 gl, Graphic graphic) {
        if (extent.intersects(graphic.getExtent())) {
            PolylineZShape shape = (PolylineZShape) graphic.getShape();
            PolylineBreak pb = (PolylineBreak) graphic.getLegend();
            float[] rgba = pb.getColor().getRGBComponents(null);
            gl.glColor3f(rgba[0], rgba[1], rgba[2]);
            gl.glLineWidth(pb.getWidth());
            gl.glBegin(GL2.GL_LINE_STRIP);
            for (Polyline line : shape.getPolylines()) {
                List<PointZ> ps = (List<PointZ>) line.getPointList();
                for (PointZ p : ps) {
                    gl.glVertex3f(transform_x((float) p.X), transform_y((float) p.Y), transform_z((float) p.Z));
                }
            }
            gl.glEnd();
        }
    }

    private void drawPolygonShape(GL2 gl, Graphic graphic) {
        if (extent.intersects(graphic.getExtent())) {
            PolygonZShape shape = (PolygonZShape) graphic.getShape();
            PolygonBreak pb = (PolygonBreak) graphic.getLegend();
            for (PolygonZ poly : (List<PolygonZ>) shape.getPolygons()) {
                //drawPolygon(gl, poly, pb);
                drawConvexPolygon(gl, poly, pb);
            }
        }
    }

    private void drawPolygon(GL2 gl, PolygonZ aPG, PolygonBreak aPGB) {
        PointZ p;
        float[] rgba = aPGB.getOutlineColor().getRGBComponents(null);
        gl.glLineWidth(aPGB.getOutlineSize());
        gl.glColor3f(rgba[0], rgba[1], rgba[2]);
        gl.glBegin(GL2.GL_LINE_STRIP);
        for (int i = 0; i < aPG.getOutLine().size(); i++) {
            p = ((List<PointZ>) aPG.getOutLine()).get(i);
            gl.glVertex3f(transform_x((float) p.X), transform_y((float) p.Y), transform_z((float) p.Z));
        }
        gl.glEnd();

        if (aPG.hasHole()) {
            List<PointZ> newPList;
            gl.glBegin(GL2.GL_LINE_STRIP);
            for (int h = 0; h < aPG.getHoleLines().size(); h++) {
                newPList = (List<PointZ>) aPG.getHoleLines().get(h);
                for (int j = 0; j < newPList.size(); j++) {
                    p = newPList.get(j);
                    gl.glVertex3f(transform_x((float) p.X), transform_y((float) p.Y), transform_z((float) p.Z));
                }
            }
            gl.glEnd();
        }
    }

    private void drawConvexPolygon(GL2 gl, PolygonZ aPG, PolygonBreak aPGB) {
        PointZ p;
        float[] rgba = aPGB.getColor().getRGBComponents(null);
        if (aPGB.isDrawFill()) {
            gl.glColor3f(rgba[0], rgba[1], rgba[2]);
            gl.glBegin(GL2.GL_POLYGON);
            for (int i = 0; i < aPG.getOutLine().size(); i++) {
                p = ((List<PointZ>) aPG.getOutLine()).get(i);
                gl.glVertex3f(transform_x((float) p.X), transform_y((float) p.Y), transform_z((float) p.Z));
            }
            gl.glEnd();
        }

        if (aPGB.isDrawOutline()) {
            rgba = aPGB.getOutlineColor().getRGBComponents(null);
            gl.glLineWidth(aPGB.getOutlineSize());
            gl.glColor3f(rgba[0], rgba[1], rgba[2]);
            gl.glBegin(GL2.GL_LINE_STRIP);
            for (int i = 0; i < aPG.getOutLine().size(); i++) {
                p = ((List<PointZ>) aPG.getOutLine()).get(i);
                gl.glVertex3f(transform_x((float) p.X), transform_y((float) p.Y), transform_z((float) p.Z));
            }
            gl.glEnd();
        }
    }

    private void drawQuadsPolygons(GL2 gl, GraphicCollection3D graphic) {
        PointZ p;
        for (int i = 0; i < graphic.getNumGraphics(); i++) {
            Graphic gg = graphic.getGraphicN(i);
            if (extent.intersects(gg.getExtent())) {
                PolygonZShape shape = (PolygonZShape) gg.getShape();
                PolygonBreak pb = (PolygonBreak) gg.getLegend();
                for (PolygonZ poly : (List<PolygonZ>) shape.getPolygons()) {
                    drawQuads(gl, poly, pb);
                }
            }
        }
    }

    private void drawQuads(GL2 gl, PolygonZ aPG, PolygonBreak aPGB) {
        PointZ p;
        float[] rgba = aPGB.getColor().getRGBComponents(null);
        if (aPGB.isDrawFill()) {
            gl.glColor3f(rgba[0], rgba[1], rgba[2]);
            gl.glBegin(GL2.GL_QUADS);
            for (int i = 0; i < aPG.getOutLine().size(); i++) {
                p = ((List<PointZ>) aPG.getOutLine()).get(i);
                gl.glVertex3f(transform_x((float) p.X), transform_y((float) p.Y), transform_z((float) p.Z));
            }
            gl.glEnd();
        }

        if (aPGB.isDrawOutline()) {
            rgba = aPGB.getOutlineColor().getRGBComponents(null);
            gl.glLineWidth(aPGB.getOutlineSize());
            gl.glColor3f(rgba[0], rgba[1], rgba[2]);
            gl.glBegin(GL2.GL_LINE_STRIP);
            for (int i = 0; i < aPG.getOutLine().size(); i++) {
                p = ((List<PointZ>) aPG.getOutLine()).get(i);
                gl.glVertex3f(transform_x((float) p.X), transform_y((float) p.Y), transform_z((float) p.Z));
            }
            gl.glEnd();
        }
    }

    private float transform_x(float v) {
        return (v - xmin) / (xmax - xmin) * 2.f - 1.0f;
    }

    private float transform_y(float v) {
        return (v - ymin) / (ymax - ymin) * 2.f - 1.0f;
    }

    private float transform_z(float v) {
        return (v - zmin) / (zmax - zmin) * 2.f - 1.0f;
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        // method body
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        //White background
        gl.glClearColor(1f, 1f, 1f, 1.0f);
        gl.glEnable(GL2.GL_POINT_SMOOTH);
        gl.glEnable(GL2.GL_DEPTH_TEST);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        final GL2 gl = drawable.getGL().getGL2();
        gl.glTranslatef(0f, 0f, 5f);
        if (height <= 0) {
            height = 1;
        }

        final float h = (float) width / (float) height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        //glu.gluPerspective(45.0f, h, 1.0, 20.0);
        gl.glOrthof(-2f, 2f, -2f, 2f, -2.0f, 2.0f);
        //glu.gluLookAt(0.0f, 0.0f, 5.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);        
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
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
        return null;
    }

    public static void main(String[] args) {

        final GLProfile gp = GLProfile.get(GLProfile.GL2);
        GLCapabilities cap = new GLCapabilities(gp);

        final GLChartPanel gc = new GLChartPanel(cap, new Plot3DGL());
        gc.setSize(400, 400);

        final JFrame frame = new JFrame("JOGL Line");
        frame.add(gc);
        frame.setSize(500, 400);
        frame.setVisible(true);

        gc.animator_start();
    }
    // </editor-fold>
}
