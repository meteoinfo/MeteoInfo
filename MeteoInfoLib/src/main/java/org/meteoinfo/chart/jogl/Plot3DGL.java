/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.jogl;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.glu.GLUtessellator;
import com.jogamp.opengl.glu.GLUtessellatorCallback;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import com.jogamp.opengl.math.VectorUtil;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import org.meteoinfo.chart.ChartColorBar;
import org.meteoinfo.chart.ChartLegend;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.chart.ChartText3D;
import org.meteoinfo.chart.Margin;
import org.meteoinfo.chart.axis.Axis;
import org.meteoinfo.chart.plot.Plot;
import org.meteoinfo.chart.plot.PlotType;
import org.meteoinfo.chart.plot.XAlign;
import org.meteoinfo.chart.plot.YAlign;
import org.meteoinfo.chart.plot3d.GraphicCollection3D;
import org.meteoinfo.data.Dataset;
import org.meteoinfo.geoprocess.GeometryUtil;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.Extent3D;
import org.meteoinfo.global.util.FontUtil;
import org.meteoinfo.legend.*;
import org.meteoinfo.math.meteo.MeteoMath;
import org.meteoinfo.shape.*;

import static org.meteoinfo.shape.ShapeTypes.PointZ;

/**
 *
 * @author wyq
 */
public class Plot3DGL extends Plot implements GLEventListener {

    // <editor-fold desc="Variables">
    private boolean doScreenShot;
    private BufferedImage screenImage;
    private final GLU glu = new GLU();
    private final GraphicCollection3D graphics;
    private Extent3D extent;
    private ChartText title;
    private List<ChartLegend> legends;
    private final Axis xAxis;
    private final Axis yAxis;
    private final Axis zAxis;
    private float xmin, xmax = 1.0f, ymin;
    private float ymax = 1.0f, zmin, zmax = 1.0f;

    private Color boxColor = Color.getHSBColor(0f, 0f, 0.95f);
    private Color lineboxColor = Color.getHSBColor(0f, 0f, 0.8f);

    private boolean boxed, mesh, scaleBox, displayXY, displayZ,
            displayGrids, drawBoundingBox, hideOnDrag;

    int viewport[] = new int[4];
    float mvmatrix[] = new float[16];
    float projmatrix[] = new float[16];

    private float angleX = -45.0f;
    private float angleY = 45.0f;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private float scaleZ = 1.0f;
    private float distanceX = 0.0f;
    private float distanceY = 0.0f;
    tessellCallBack tessCallback;
    private int width;
    private int height;
    float tickSpace = 5.0f;
    final private float lenScale = 0.01f;
    private Lighting lighting = new Lighting();
    private boolean antialias;
    private float scale;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public Plot3DGL() {
        this.doScreenShot = false;
        this.legends = new ArrayList<>();
        //this.legends.add(new ChartColorBar(new LegendScheme(ShapeTypes.Polygon, 5)));
        this.xAxis = new Axis();
        this.xAxis.setLabel("X");
        //this.xAxis.setLabel("Longitude");
        this.xAxis.setTickLength(8);
        this.yAxis = new Axis();
        this.yAxis.setLabel("Y");
        //this.yAxis.setLabel("Latitude");
        this.yAxis.setTickLength(8);
        this.zAxis = new Axis();
        this.zAxis.setLabel("Z");
        //this.zAxis.setLabel("Altitude");
        this.zAxis.setTickLength(8);
        this.graphics = new GraphicCollection3D();
        this.hideOnDrag = false;
        this.boxed = true;
        this.displayGrids = false;
        this.displayXY = true;
        this.displayZ = true;
        this.drawBoundingBox = false;
        this.antialias = false;
        this.scale = 1;
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
     * Get if do screenshot
     * @return Boolean
     */
    public boolean isDoScreenShot() {
        return this.doScreenShot;
    }

    /**
     * Set if do screenshot
     * @param value Boolean
     */
    public void setDoScreenShot(boolean value) {
        this.doScreenShot = value;
    }

    /**
     * Get screen image
     *
     * @return Screen image
     */
    public BufferedImage getScreenImage() {
        return this.screenImage;
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
    }

    /**
     * Get box color
     *
     * @return Box color
     */
    public Color getBoxColor() {
        return this.boxColor;
    }

    /**
     * Set box color
     *
     * @param value Box color
     */
    public void setBoxColor(Color value) {
        this.boxColor = value;
    }

    /**
     * Get box line color
     *
     * @return Box line color
     */
    public Color getLineBoxColor() {
        return this.lineboxColor;
    }

    /**
     * Set box line color
     *
     * @param value Box line color
     */
    public void setLineBoxColor(Color value) {
        this.lineboxColor = value;
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
     * Set display X/Y axis or not
     *
     * @param value Boolean
     */
    public void setDisplayXY(boolean value) {
        this.displayXY = value;
    }

    /**
     * Set display Z axis or not
     *
     * @param value Boolean
     */
    public void setDisplayZ(boolean value) {
        this.displayZ = value;
    }

    /**
     * Set display grids or not
     *
     * @param value Boolean
     */
    public void setDisplayGrids(boolean value) {
        this.displayGrids = value;
    }

    /**
     * Set display box or not
     *
     * @param value Boolean
     */
    public void setBoxed(boolean value) {
        this.boxed = value;
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
     * Get scale x
     * @return Scale x
     */
    public float getScaleX() { return this.scaleX; }

    /**
     * Set scale x
     * @param value Scale x
     */
    public void setScaleX(float value) { this.scaleX = value; }

    /**
     * Get scale y
     * @return Scale y
     */
    public float getScaleY() { return this.scaleY; }

    /**
     * Set scale y
     * @param value Scale y
     */
    public void setScaleY(float value) { this.scaleY = value; }

    /**
     * Get scale z
     * @return Scale z
     */
    public float getScaleZ() { return this.scaleZ; }

    /**
     * Set scale z
     * @param value Scale z
     */
    public void setScaleZ(float value) { this.scaleZ = value; }

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

    /**
     * Get lighting set
     *
     * @return Lighting set
     */
    public Lighting getLighting() {
        return this.lighting;
    }

    /**
     * Set lighting set
     *
     * @param value Lighting set
     */
    public void setLighting(Lighting value) {
        this.lighting = value;
    }

    /**
     * Get is antialias or not
     * @return Antialias or not
     */
    public boolean isAntialias() { return this.antialias; }

    /**
     * Set is antialias or not
     * @param value Antialias or not
     */
    public void setAntialias(boolean value) { this.antialias = value; }

    /**
     * Get scale
     * @return Scale
     */
    public float getScale() {
        return this.scale;
    }

    /**
     * Set scale
     * @param value Scale
     */
    public void setScale(float value) {
        this.scale = value;
    }

    // </editor-fold>
    // <editor-fold desc="methods">
    /**
     * Add a legend
     *
     * @param legend The legend
     */
    public void addLegend(ChartLegend legend) {
        this.legends.clear();
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
        if (!ex.is3D()) {
            ex = ex.to3D();
        }
        this.setExtent((Extent3D) ex);
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

    @Override
    public void display(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glPushMatrix();

        gl.glShadeModel(GL2.GL_SMOOTH);

        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        //gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);

        if (this.antialias) {
            gl.glEnable(GL2.GL_MULTISAMPLE);
            /*gl.glEnable(GL2.GL_LINE_SMOOTH);
            gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
            gl.glEnable(GL2.GL_POINT_SMOOTH);
            gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
            gl.glEnable(GL2.GL_POLYGON_SMOOTH);
            gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);*/
        } else {
            gl.glDisable(GL2.GL_MULTISAMPLE);
            /*gl.glDisable(GL2.GL_LINE_SMOOTH);
            gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_FASTEST);
            gl.glDisable(GL2.GL_POINT_SMOOTH);
            gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_FASTEST);
            gl.glDisable(GL2.GL_POLYGON_SMOOTH);
            gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_FASTEST);*/
        }

        gl.glScalef(scaleX, scaleY, scaleZ);

        gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(angleY, 0.0f, 0.0f, 1.0f);

        this.updateMatrix(gl);

        //Set lighting
        if (this.lighting.isEnable()) {
            this.lighting.start(gl);
            //keep material colors
            gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_DIFFUSE);
            //gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
            gl.glEnable(GL2.GL_COLOR_MATERIAL);
        }

        gl.glColor3f(0.0f, 0.0f, 0.0f);

        //Draw box
        drawBoxGridsTicksLabels(gl);

        //Draw title
        this.drawTitle();

        //Draw graphics
        float s = 1.01f;
        //gl.glClipPlanef(GL2.GL_CLIP_PLANE0, new float[]{1, 0, 0, s}, 0);
        gl.glClipPlane(GL2.GL_CLIP_PLANE0, new double[]{1, 0, 0, s}, 0);
        gl.glEnable(GL2.GL_CLIP_PLANE0);
        //gl.glClipPlanef(GL2.GL_CLIP_PLANE1, new float[]{-1, 0, 0, s}, 0);
        gl.glClipPlane(GL2.GL_CLIP_PLANE1, new double[]{-1, 0, 0, s}, 0);
        gl.glEnable(GL2.GL_CLIP_PLANE1);
        //gl.glClipPlanef(GL2.GL_CLIP_PLANE2, new float[]{0, -1, 0, s}, 0);
        gl.glClipPlane(GL2.GL_CLIP_PLANE2, new double[]{0, -1, 0, s}, 0);
        gl.glEnable(GL2.GL_CLIP_PLANE2);
        //gl.glClipPlanef(GL2.GL_CLIP_PLANE3, new float[]{0, 1, 0, s}, 0);
        gl.glClipPlane(GL2.GL_CLIP_PLANE3, new double[]{0, 1, 0, s}, 0);
        gl.glEnable(GL2.GL_CLIP_PLANE3);
        //gl.glClipPlanef(GL2.GL_CLIP_PLANE4, new float[]{0, 0, 1, s}, 0);
        gl.glClipPlane(GL2.GL_CLIP_PLANE4, new double[]{0, 0, 1, s}, 0);
        gl.glEnable(GL2.GL_CLIP_PLANE4);
        //gl.glClipPlanef(GL2.GL_CLIP_PLANE5, new float[]{0, 0, -1, s}, 0);
        gl.glClipPlane(GL2.GL_CLIP_PLANE5, new double[]{0, 0, -1, s}, 0);
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

        //Draw text
        for (int m = 0; m < this.graphics.getNumGraphics(); m++) {
            Graphic graphic = this.graphics.get(m);
            if (graphic.getNumGraphics() == 1) {
                Shape shape = graphic.getGraphicN(0).getShape();
                if (shape.getShapeType() == ShapeTypes.TEXT) {
                    this.drawText(gl, (ChartText3D) shape);
                }
            } else {
                for (int i = 0; i < graphic.getNumGraphics(); i++) {
                    Shape shape = graphic.getGraphicN(i).getShape();
                    if (shape.getShapeType() == ShapeTypes.TEXT) {
                        this.drawText(gl, (ChartText3D) shape);
                    }
                }
            }
        }

        //Stop lighting
        if (this.lighting.isEnable()) {
            this.lighting.stop(gl);
        }

        //Draw legend
        gl.glPopMatrix();
        this.updateMatrix(gl);
        this.drawLegend(gl);

        gl.glFlush();

        //Do screen shot
        if (this.doScreenShot) {
            AWTGLReadBufferUtil glReadBufferUtil = new AWTGLReadBufferUtil(drawable.getGLProfile(), false);
            this.screenImage = glReadBufferUtil.readPixelsToBufferedImage(drawable.getGL(), true);
            this.doScreenShot = false;
        }
    }

    /**
     * Draws the base plane. The base plane is the x-y plane.
     *
     * @param g the graphics context to draw.
     * @param x used to retrieve x coordinates of drawn plane from this method.
     * @param y used to retrieve y coordinates of drawn plane from this method.
     */
    private void drawBase(GL2 gl) {
        float[] rgba = this.lineboxColor.getRGBComponents(null);
        gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
        gl.glLineWidth(1.0f);
        gl.glBegin(GL2.GL_LINE_STRIP);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glEnd();
    }

    private void updateMatrix(GL2 gl) {
        gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
        gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, mvmatrix, 0);
        gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projmatrix, 0);
    }

    private float[] toScreen(float vx, float vy, float vz) {
        //Get screen coordinates
        float coord[] = new float[4];// x, y;// returned xy 2d coords        
        glu.gluProject(vx, vy, vz, mvmatrix, 0, projmatrix, 0, viewport, 0, coord, 0);

        return coord;
    }

    private float toScreenLength(float x1, float y1, float z1, float x2, float y2, float z2) {
        float[] coord = toScreen(x1, y1, z1);
        float sx1 = coord[0];
        float sy1 = coord[1];
        coord = toScreen(x2, y2, z2);
        float sx2 = coord[0];
        float sy2 = coord[1];

        return (float) Math.sqrt(Math.pow(sx2 - sx1, 2) + Math.pow(sy2 - sy1, 2));
    }

    private float toScreenAngle(float x1, float y1, float z1, float x2, float y2, float z2) {
        float[] coord = toScreen(x1, y1, z1);
        float sx1 = coord[0];
        float sy1 = coord[1];
        coord = toScreen(x2, y2, z2);
        float sx2 = coord[0];
        float sy2 = coord[1];

        return (float) MeteoMath.uv2ds(sx2 - sx1, sy2 - sy1)[0];
    }

    private int getLabelGap(Font font, List<ChartText> labels, double len) {
        TextRenderer textRenderer = new TextRenderer(font);
        int n = labels.size();
        int nn;
        Rectangle2D rect = textRenderer.getBounds("Text".subSequence(0, 4));
        nn = (int) (len / rect.getHeight());
        if (nn == 0) {
            nn = 1;
        }
        return n / nn + 1;
    }

    private int getLegendTickGap(ChartColorBar legend, double len) {
        if (legend.getTickLabelAngle() != 0) {
            return 1;
        }

        Font font = legend.getTickLabelFont();
        if (this.scale != 1) {
            font = new Font(font.getFontName(), font.getStyle(), (int)(font.getSize() * this.scale));
        }
        TextRenderer textRenderer = new TextRenderer(font);
        int n = legend.getLegendScheme().getBreakNum();
        int nn;
        Rectangle2D rect = textRenderer.getBounds("Text".subSequence(0, 4));
        nn = (int) (len / rect.getHeight());
        if (nn == 0) {
            nn = 1;
        }
        return n / nn + 1;
    }

    private void drawBoxGridsTicksLabels(GL2 gl) {
        this.drawBase(gl);

        float[] rgba;
        //Draw box
        if (boxed) {
            if (this.angleY >= 180 && this.angleY < 360) {
                rgba = this.lineboxColor.getRGBComponents(null);
                gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                gl.glLineWidth(1.0f);
                gl.glBegin(GL2.GL_LINE_STRIP);
                gl.glVertex3f(-1.0f, 1.0f, -1.0f);
                gl.glVertex3f(-1.0f, -1.0f, -1.0f);
                gl.glVertex3f(-1.0f, -1.0f, 1.0f);
                gl.glVertex3f(-1.0f, 1.0f, 1.0f);
                gl.glVertex3f(-1.0f, 1.0f, -1.0f);
                gl.glEnd();
            } else {
                rgba = this.lineboxColor.getRGBComponents(null);
                gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                gl.glLineWidth(1.0f);
                gl.glBegin(GL2.GL_LINE_STRIP);
                gl.glVertex3f(1.0f, 1.0f, -1.0f);
                gl.glVertex3f(1.0f, -1.0f, -1.0f);
                gl.glVertex3f(1.0f, -1.0f, 1.0f);
                gl.glVertex3f(1.0f, 1.0f, 1.0f);
                gl.glVertex3f(1.0f, 1.0f, -1.0f);
                gl.glEnd();
            }
            if (this.angleY >= 90 && this.angleY < 270) {
                rgba = this.lineboxColor.getRGBComponents(null);
                gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                gl.glLineWidth(1.0f);
                gl.glBegin(GL2.GL_LINE_STRIP);
                gl.glVertex3f(-1.0f, -1.0f, -1.0f);
                gl.glVertex3f(1.0f, -1.0f, -1.0f);
                gl.glVertex3f(1.0f, -1.0f, 1.0f);
                gl.glVertex3f(-1.0f, -1.0f, 1.0f);
                gl.glVertex3f(-1.0f, -1.0f, -1.0f);
                gl.glEnd();
            } else {
                rgba = this.lineboxColor.getRGBComponents(null);
                gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                gl.glLineWidth(1.0f);
                gl.glBegin(GL2.GL_LINE_STRIP);
                gl.glVertex3f(-1.0f, 1.0f, -1.0f);
                gl.glVertex3f(1.0f, 1.0f, -1.0f);
                gl.glVertex3f(1.0f, 1.0f, 1.0f);
                gl.glVertex3f(-1.0f, 1.0f, 1.0f);
                gl.glVertex3f(-1.0f, 1.0f, -1.0f);
                gl.glEnd();
            }
        }

        //Draw axis
        float x, y, v;
        int skip;
        XAlign xAlign;
        YAlign yAlign;
        Rectangle2D rect;
        float strWidth, strHeight;
        if (this.displayXY) {
            //Draw x/y axis lines
            //x axis line            
            if (this.angleY >= 90 && this.angleY < 270) {
                y = 1.0f;
            } else {
                y = -1.0f;
            }
            rgba = this.xAxis.getLineColor().getRGBComponents(null);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glLineWidth(this.xAxis.getLineWidth());
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3f(-1.0f, y, -1.0f);
            gl.glVertex3f(1.0f, y, -1.0f);
            gl.glEnd();

            //x axis ticks
            float tickLen = this.xAxis.getTickLength() * this.lenScale;
            this.xAxis.updateTickLabels();
            List<ChartText> tlabs = this.xAxis.getTickLabels();
            float axisLen = this.toScreenLength(-1.0f, y, -1.0f, 1.0f, y, -1.0f);
            skip = getLabelGap(this.xAxis.getTickLabelFont(), tlabs, axisLen);
            float y1 = y > 0 ? y + tickLen : y - tickLen;
            if (this.angleY < 90 || (this.angleY >= 180 && this.angleY < 270)) {
                xAlign = XAlign.LEFT;
            } else {
                xAlign = XAlign.RIGHT;
            }
            if (this.angleX > -120) {
                yAlign = YAlign.TOP;
            } else {
                yAlign = YAlign.BOTTOM;
            }
            strWidth = 0.0f;
            strHeight = 0.0f;
            for (int i = 0; i < this.xAxis.getTickValues().length; i += skip) {
                v = (float) this.xAxis.getTickValues()[i];
                if (v < xmin || v > xmax) {
                    continue;
                }
                v = this.transform_xf(v);
                if (i == tlabs.size()) {
                    break;
                }

                //Draw grid line
                if (this.displayGrids && (v != -1.0f && v != 1.0f)) {
                    rgba = this.getLineBoxColor().getRGBComponents(null);
                    gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                    gl.glLineWidth(1.0f);
                    gl.glBegin(GL2.GL_LINES);
                    gl.glVertex3f(v, y, -1.0f);
                    gl.glVertex3f(v, -y, -1.0f);
                    gl.glEnd();
                    if (this.displayZ && this.boxed) {
                        gl.glBegin(GL2.GL_LINES);
                        gl.glVertex3f(v, -y, -1.0f);
                        gl.glVertex3f(v, -y, 1.0f);
                        gl.glEnd();
                    }
                }

                //Draw tick line
                rgba = this.xAxis.getLineColor().getRGBComponents(null);
                gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                gl.glLineWidth(this.xAxis.getLineWidth());
                gl.glBegin(GL2.GL_LINES);
                gl.glVertex3f(v, y, -1.0f);
                gl.glVertex3f(v, y1, -1.0f);
                gl.glEnd();

                //Draw tick label                
                rect = drawString(gl, tlabs.get(i), v, y1, -1.0f, xAlign, yAlign);
                if (strWidth < rect.getWidth()) {
                    strWidth = (float) rect.getWidth();
                }
                if (strHeight < rect.getHeight()) {
                    strHeight = (float) rect.getHeight();
                }
            }

            //Draw x axis label
            ChartText label = this.xAxis.getLabel();
            if (label != null) {
                strWidth += this.tickSpace;
                float angle = this.toScreenAngle(-1.0f, y, -1.0f, 1.0f, y, -1.0f);
                angle = y < 0 ? 270 - angle : 90 - angle;
                float yShift = Math.min(-strWidth, -strWidth);
                if (this.angleX <= -120) {
                    yShift = -yShift;
                }
                drawString(gl, label, 0.0f, y1, -1.0f, XAlign.CENTER, yAlign, angle, 0, yShift);
            }

            ////////////////////////////////////////////
            //y axis line
            if (this.angleY >= 180 && this.angleY < 360) {
                x = 1.0f;
            } else {
                x = -1.0f;
            }
            rgba = this.yAxis.getLineColor().getRGBComponents(null);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glLineWidth(this.yAxis.getLineWidth());
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3f(x, -1.0f, -1.0f);
            gl.glVertex3f(x, 1.0f, -1.0f);
            gl.glEnd();

            //y axis ticks
            this.yAxis.updateTickLabels();
            tlabs = this.yAxis.getTickLabels();
            axisLen = this.toScreenLength(x, -1.0f, -1.0f, x, 1.0f, -1.0f);
            skip = getLabelGap(this.yAxis.getTickLabelFont(), tlabs, axisLen);
            tickLen = this.yAxis.getTickLength() * this.lenScale;
            float x1 = x > 0 ? x + tickLen : x - tickLen;
            if (this.angleY < 90 || (this.angleY >= 180 && this.angleY < 270)) {
                xAlign = XAlign.RIGHT;
            } else {
                xAlign = XAlign.LEFT;
            }
            if (this.angleX > -120) {
                yAlign = YAlign.TOP;
            } else {
                yAlign = YAlign.BOTTOM;
            }
            strWidth = 0.0f;
            strHeight = 0.0f;
            for (int i = 0; i < this.yAxis.getTickValues().length; i += skip) {
                v = (float) this.yAxis.getTickValues()[i];
                if (v < ymin || v > ymax) {
                    continue;
                }
                v = this.transform_yf(v);
                if (i == tlabs.size()) {
                    break;
                }

                //Draw grid line
                if (this.displayGrids && (v != -1.0f && v != 1.0f)) {
                    rgba = this.getLineBoxColor().getRGBComponents(null);
                    gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                    gl.glLineWidth(1.0f);
                    gl.glBegin(GL2.GL_LINES);
                    gl.glVertex3f(x, v, -1.0f);
                    gl.glVertex3f(-x, v, -1.0f);
                    gl.glEnd();
                    if (this.displayZ && this.boxed) {
                        gl.glBegin(GL2.GL_LINES);
                        gl.glVertex3f(-x, v, -1.0f);
                        gl.glVertex3f(-x, v, 1.0f);
                        gl.glEnd();
                    }
                }

                //Draw tick line
                rgba = this.yAxis.getLineColor().getRGBComponents(null);
                gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                gl.glLineWidth(this.yAxis.getLineWidth());
                gl.glBegin(GL2.GL_LINES);
                gl.glVertex3f(x, v, -1.0f);
                gl.glVertex3f(x1, v, -1.0f);
                gl.glEnd();

                //Draw tick label                
                rect = drawString(gl, tlabs.get(i), x1, v, -1.0f, xAlign, yAlign);
                if (strWidth < rect.getWidth()) {
                    strWidth = (float) rect.getWidth();
                }
                if (strHeight < rect.getHeight()) {
                    strHeight = (float) rect.getHeight();
                }
            }

            //Draw y axis label
            label = this.yAxis.getLabel();
            if (label != null) {
                strWidth += this.tickSpace;
                float angle = this.toScreenAngle(x, -1.0f, -1.0f, x, 1.0f, -1.0f);
                angle = x > 0 ? 270 - angle : 90 - angle;
                float yShift = Math.min(-strWidth, -strWidth);
                if (this.angleX <= -120) {
                    yShift = -yShift;
                }
                drawString(gl, label, x1, 0.0f, -1.0f, XAlign.CENTER, yAlign, angle, 0, yShift);
            }
        }

        //Draw z axis
        if (this.displayZ) {
            //z axis line
            if (this.angleY < 90) {
                x = -1.0f;
                y = 1.0f;
            } else if (this.angleY < 180) {
                x = 1.0f;
                y = 1.0f;
            } else if (this.angleY < 270) {
                x = 1.0f;
                y = -1.0f;
            } else {
                x = -1.0f;
                y = -1.0f;
            }
            rgba = this.zAxis.getLineColor().getRGBComponents(null);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glLineWidth(this.zAxis.getLineWidth());
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3f(x, y, -1.0f);
            gl.glVertex3f(x, y, 1.0f);
            gl.glEnd();

            //z axis ticks
            this.zAxis.updateTickLabels();
            List<ChartText> tlabs = this.zAxis.getTickLabels();
            float axisLen = this.toScreenLength(x, y, -1.0f, x, y, 1.0f);
            skip = getLabelGap(this.zAxis.getTickLabelFont(), tlabs, axisLen);
            float x1 = x;
            float y1 = y;
            float tickLen = this.zAxis.getTickLength() * this.lenScale;
            if (x < 0) {
                if (y > 0) {
                    y1 += tickLen;
                } else {
                    x1 -= tickLen;
                }
            } else {
                if (y > 0) {
                    x1 += tickLen;
                } else {
                    y1 -= tickLen;
                }
            }
            xAlign = XAlign.RIGHT;
            yAlign = YAlign.CENTER;
            strWidth = 0.0f;
            for (int i = 0; i < this.zAxis.getTickValues().length; i += skip) {
                v = (float) this.zAxis.getTickValues()[i];
                if (v < zmin || v > zmax) {
                    continue;
                }
                v = this.transform_zf(v);
                if (i == tlabs.size()) {
                    break;
                }

                //Draw grid line
                if (this.displayGrids && this.boxed && (v != -1.0f && v != 1.0f)) {
                    rgba = this.getLineBoxColor().getRGBComponents(null);
                    gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                    gl.glLineWidth(1.0f);
                    gl.glBegin(GL2.GL_LINE_STRIP);
                    gl.glVertex3f(x, y, v);
                    if (x < 0) {
                        if (y > 0) {
                            gl.glVertex3f(-x, y, v);
                            gl.glVertex3f(-x, -y, v);
                        } else {
                            gl.glVertex3f(x, -y, v);
                            gl.glVertex3f(-x, -y, v);
                        }
                    } else {
                        if (y > 0) {
                            gl.glVertex3f(x, -y, v);
                            gl.glVertex3f(-x, -y, v);
                        } else {
                            gl.glVertex3f(-x, y, v);
                            gl.glVertex3f(-x, -y, v);
                        }
                    }
                    gl.glEnd();
                }

                //Draw tick line
                rgba = this.zAxis.getLineColor().getRGBComponents(null);
                gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                gl.glLineWidth(this.zAxis.getLineWidth());
                gl.glBegin(GL2.GL_LINES);
                gl.glVertex3f(x, y, v);
                gl.glVertex3f(x1, y1, v);
                gl.glEnd();

                //Draw tick label                
                rect = drawString(gl, tlabs.get(i), x1, y1, v, xAlign, yAlign, -this.tickSpace, 0);
                if (strWidth < rect.getWidth()) {
                    strWidth = (float) rect.getWidth();
                }
            }

            //Draw z axis label
            ChartText label = this.zAxis.getLabel();
            if (label != null) {
                float yShift = strWidth + this.tickSpace * 3;
                drawString(gl, label, x1, y1, 0.0f, XAlign.CENTER, YAlign.BOTTOM, 90.f, 0, yShift);
            }
        }
    }

    Rectangle2D drawString(GL2 gl, ChartText text, float vx, float vy, float vz, XAlign xAlign, YAlign yAlign) {
        return drawString(gl, text, vx, vy, vz, xAlign, yAlign, 0, 0);
    }

    Rectangle2D drawString(GL2 gl, ChartText text, float vx, float vy, float vz,
            XAlign xAlign, YAlign yAlign, float xShift, float yShift) {
        return drawString(gl, text.getText(), text.getFont(), text.getColor(), vx,
                vy, vz, xAlign, yAlign, xShift, yShift);
    }

    Rectangle2D drawString(GL2 gl, String str, Font font, Color color, float vx, float vy, float vz,
            XAlign xAlign, YAlign yAlign) {
        return drawString(gl, str, font, color, vx, vy, vz, xAlign, yAlign, 0, 0);
    }

    Rectangle2D drawString(GL2 gl, String str, Font font, Color color, float vx, float vy, float vz,
            XAlign xAlign, YAlign yAlign, float xShift, float yShift) {
        //Get screen coordinates
        float coord[] = this.toScreen(vx, vy, vz);
        float x = coord[0];
        float y = coord[1];

        //Rendering text string
        TextRenderer textRenderer;
        if (this.scale == 1) {
            textRenderer = new TextRenderer(font, true, true);
        } else {
            textRenderer = new TextRenderer(new Font(font.getFontName(), font.getStyle(),
                    (int)(font.getSize() * (1 + (this.scale - 1) * 0.8))), true, true);
        }
        textRenderer.beginRendering(this.width, this.height);
        textRenderer.setColor(color);
        textRenderer.setSmoothing(true);
        Rectangle2D rect = textRenderer.getBounds(str.subSequence(0, str.length()));
        switch (xAlign) {
            case CENTER:
                x -= rect.getWidth() * 0.5;
                break;
            case RIGHT:
                x -= rect.getWidth();
                break;
        }
        switch (yAlign) {
            case CENTER:
                y -= rect.getHeight() * 0.3;
                break;
            case TOP:
                y -= rect.getHeight();
                break;
        }
        textRenderer.draw(str, (int) (x + xShift), (int) (y + yShift));
        textRenderer.endRendering();

        return rect;
    }

    Rectangle2D drawString(GL2 gl, ChartText text, float vx, float vy, float vz,
            XAlign xAlign, YAlign yAlign, float angle) {
        return drawString(gl, text.getText(), text.getFont(), text.getColor(), vx, vy, vz, xAlign, yAlign, angle, 0, 0);
    }

    Rectangle2D drawString(GL2 gl, ChartText text, float vx, float vy, float vz,
            XAlign xAlign, YAlign yAlign, float angle, float xShift, float yShift) {
        return drawString(gl, text.getText(), text.getFont(), text.getColor(), vx, vy,
                vz, xAlign, yAlign, angle, xShift, yShift);
    }

    Rectangle2D drawString(GL2 gl, String str, Font font, Color color, float vx, float vy, float vz,
            XAlign xAlign, YAlign yAlign, float angle) {
        return drawString(gl, str, font, color, vx, vy, vz, xAlign, yAlign, angle, 0, 0);
    }

    Rectangle2D drawString(GL2 gl, String str, Font font, Color color, float vx, float vy, float vz,
            XAlign xAlign, YAlign yAlign, float angle, float xShift, float yShift) {
        //Get screen coordinates
        float coord[] = this.toScreen(vx, vy, vz);
        float x = coord[0];
        float y = coord[1];

        //Rendering text string
        TextRenderer textRenderer;
        if (this.scale == 1) {
            textRenderer = new TextRenderer(font, true, true);
        } else {
            textRenderer = new TextRenderer(new Font(font.getFontName(), font.getStyle(),
                    (int)(font.getSize() * (1 + (this.scale - 1) * 0.8))), true, true);
        }
        textRenderer.beginRendering(this.width, this.height);
        textRenderer.setColor(color);
        textRenderer.setSmoothing(true);
        Rectangle2D rect = textRenderer.getBounds(str.subSequence(0, str.length()));
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glTranslatef(x, y, 0.0f);
        if (angle != 0) {
            gl.glRotatef(angle, 0.0f, 0.0f, 1.0f);
        }
        x = 0;
        y = 0;
        switch (xAlign) {
            case CENTER:
                x -= rect.getWidth() * 0.5;
                break;
            case RIGHT:
                x -= rect.getWidth();
                break;
        }
        switch (yAlign) {
            case CENTER:
                y -= rect.getHeight() * 0.5;
                break;
            case TOP:
                y -= rect.getHeight();
                break;
        }
        x += xShift;
        y += yShift;
        textRenderer.draw(str, (int) x, (int) y);
        textRenderer.endRendering();
        gl.glPopMatrix();

        return rect;
    }

    void drawTitle() {
        if (title != null) {
            //Rendering text string
            Font font = title.getFont();
            TextRenderer textRenderer;
            if (this.scale == 1) {
                textRenderer = new TextRenderer(font, true, true);
            } else {
                textRenderer = new TextRenderer(new Font(font.getFontName(), font.getStyle(),
                        (int)(font.getSize() * this.scale)), true, true);
            }
            textRenderer.beginRendering(this.width, this.height);
            textRenderer.setColor(title.getColor());
            textRenderer.setSmoothing(true);
            Rectangle2D rect = textRenderer.getBounds(title.getText().subSequence(0, title.getText().length()));
            float x = (float) (this.width / 2.0f) - (float) rect.getWidth() / 2.0f;
            float y = this.height - (float) rect.getHeight();
            textRenderer.draw(title.getText(), (int) x, (int) y);
            textRenderer.endRendering();
        }
    }

    private void drawGraphics(GL2 gl, Graphic graphic) {
        if (graphic.getNumGraphics() == 1) {
            Graphic gg = graphic.getGraphicN(0);
            this.drawGraphic(gl, gg);
        } else {
            if (graphic instanceof SurfaceGraphics) {
                this.drawSurface(gl, (SurfaceGraphics) graphic);
            } else if (graphic instanceof IsosurfaceGraphics) {
                this.drawIsosurface(gl, (IsosurfaceGraphics) graphic);
            } else if (graphic instanceof ParticleGraphics) {
                this.drawParticles(gl, (ParticleGraphics) graphic);
            } else {
                boolean isDraw = true;
                if (graphic instanceof GraphicCollection3D) {
                    GraphicCollection3D gg = (GraphicCollection3D) graphic;
                    if (gg.isAllQuads()) {
                        this.drawQuadsPolygons(gl, gg);
                        isDraw = false;
                    } else if (gg.isAllTriangle()) {
                        this.drawTrianglePolygons(gl, gg);
                        isDraw = false;
                    }
                }
                if (isDraw) {
                    switch (graphic.getGraphicN(0).getShape().getShapeType()) {
                        case PointZ:
                            this.drawPoints(gl, graphic);
                            break;
                        default:
                            for (int i = 0; i < graphic.getNumGraphics(); i++) {
                                Graphic gg = graphic.getGraphicN(i);
                                this.drawGraphic(gl, gg);
                            }
                            break;
                    }
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
                //this.drawText(gl, (ChartText3D) shape);
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
                this.drawWindArrow(gl, graphic);
                break;
            case CUBIC:
                this.drawCubic(gl, graphic);
                break;
            case CYLINDER:
                this.drawCylinder(gl, graphic);
                break;
            case Image:
                this.drawImage(gl, graphic);
                break;
            case TEXTURE:
                this.drawTexture(gl, graphic);
                break;
        }
    }

    private void drawText(GL2 gl, ChartText3D text) {
        float x = this.transform_xf((float) text.getX());
        float y = this.transform_yf((float) text.getY());
        float z = this.transform_zf((float) text.getZ());
        this.drawString(gl, text, x, y, z, text.getXAlign(), text.getYAlign());
    }

    private void drawPoint(GL2 gl, Graphic graphic) {
        if (extent.intersects(graphic.getExtent())) {
            PointZShape shape = (PointZShape) graphic.getShape();
            PointBreak pb = (PointBreak) graphic.getLegend();
            float[] rgba = pb.getColor().getRGBComponents(null);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glPointSize(pb.getSize());
            gl.glBegin(GL2.GL_POINTS);
            PointZ p = (PointZ) shape.getPoint();
            gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
            gl.glEnd();
        }
    }

    private void drawPoints(GL2 gl, Graphic graphic) {
        PointBreak pb = (PointBreak) graphic.getGraphicN(0).getLegend();
        gl.glPointSize(pb.getSize());
        gl.glBegin(GL2.GL_POINTS);
        for (Graphic gg : graphic.getGraphics()) {
            PointZShape shape = (PointZShape) gg.getShape();
            pb = (PointBreak) gg.getLegend();
            float[] rgba = pb.getColor().getRGBComponents(null);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            PointZ p = (PointZ) shape.getPoint();
            gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
        }
        gl.glEnd();
    }

    private void drawParticles(GL2 gl, ParticleGraphics particles) {
        for (Map.Entry<Integer, List> map : particles.getParticleList()) {
            gl.glPointSize(particles.getPointSize());
            gl.glBegin(GL2.GL_POINTS);
            for (ParticleGraphics.Particle p : (List<ParticleGraphics.Particle>)map.getValue()) {
                float[] rgba = p.rgba;
                gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                gl.glVertex3f(transform_xf((float) p.x), transform_yf((float) p.y), transform_zf((float) p.z));
            }
            gl.glEnd();
        }
    }

    private void drawLineString(GL2 gl, Graphic graphic) {
        if (extent.intersects(graphic.getExtent())) {
            PolylineZShape shape = (PolylineZShape) graphic.getShape();
            ColorBreak cb = graphic.getLegend();
            if (cb.getBreakType() == BreakTypes.ColorBreakCollection) {
                ColorBreakCollection cbc = (ColorBreakCollection) cb;
                Polyline line = shape.getPolylines().get(0);
                List<PointZ> ps = (List<PointZ>) line.getPointList();
                float[] rgba;
                PointZ p;
                gl.glBegin(GL2.GL_LINE_STRIP);
                for (int i = 0; i < ps.size(); i++) {
                    PolylineBreak plb = (PolylineBreak) cbc.get(i);
                    rgba = plb.getColor().getRGBComponents(null);
                    gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                    gl.glLineWidth(plb.getWidth());
                    p = ps.get(i);
                    gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
                }
                gl.glEnd();
            } else {
                PolylineBreak pb = (PolylineBreak) cb;
                float[] rgba = pb.getColor().getRGBComponents(null);
                gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                gl.glLineWidth(pb.getWidth());
                gl.glBegin(GL2.GL_LINE_STRIP);
                for (Polyline line : shape.getPolylines()) {
                    List<PointZ> ps = (List<PointZ>) line.getPointList();
                    for (PointZ p : ps) {
                        gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
                    }
                }
                gl.glEnd();
            }
        }
    }

    private void drawPolygonShape(GL2 gl, Graphic graphic) {
        if (extent.intersects(graphic.getExtent())) {
            PolygonZShape shape = (PolygonZShape) graphic.getShape();
            PolygonBreak pb = (PolygonBreak) graphic.getLegend();
            for (PolygonZ poly : (List<PolygonZ>) shape.getPolygons()) {
                if (GeometryUtil.isConvex(poly)) {
                    drawConvexPolygon(gl, poly, pb);
                } else {
                    drawPolygon(gl, poly, pb);
                }
            }
        }
    }

    private void drawPolygon(GL2 gl, PolygonZ aPG, PolygonBreak aPGB) {
        PointZ p;
        if (aPGB.isDrawFill() && aPGB.getColor().getAlpha() > 0) {
            gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
            gl.glPolygonOffset(1.0f, 1.0f);

            float[] rgba = aPGB.getColor().getRGBComponents(null);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);

            try {
                //int startList = gl.glGenLists(1);
                GLUtessellator tobj = GLU.gluNewTess();
                GLU.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, tessCallback);// glVertex3dv);
                GLU.gluTessCallback(tobj, GLU.GLU_TESS_COMBINE, tessCallback);
                GLU.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, tessCallback);// beginCallback);
                GLU.gluTessCallback(tobj, GLU.GLU_TESS_END, tessCallback);// endCallback);
                GLU.gluTessCallback(tobj, GLU.GLU_TESS_ERROR, tessCallback);// errorCallback);

                //gl.glNewList(startList, GL2.GL_COMPILE);
                //gl.glShadeModel(GL2.GL_FLAT);
                GLU.gluTessBeginPolygon(tobj, null);
                GLU.gluTessBeginContour(tobj);
                double[] v;
                for (int i = 0; i < aPG.getOutLine().size() - 1; i++) {
                    p = ((List<PointZ>) aPG.getOutLine()).get(i);
                    v = transform(p);
                    GLU.gluTessVertex(tobj, v, 0, v);
                }
                GLU.gluTessEndContour(tobj);
                if (aPG.hasHole()) {
                    for (int i = 0; i < aPG.getHoleLineNumber(); i++) {
                        GLU.gluTessBeginContour(tobj);
                        for (int j = 0; j < aPG.getHoleLine(i).size() - 1; j++) {
                            p = ((List<PointZ>) aPG.getHoleLine(i)).get(j);
                            v = transform(p);
                            GLU.gluTessVertex(tobj, v, 0, v);
                        }
                        GLU.gluTessEndContour(tobj);
                    }
                }
                GLU.gluTessEndPolygon(tobj);
                //gl.glEndList();
                GLU.gluDeleteTess(tobj);

                //gl.glCallList(startList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (aPGB.isDrawOutline()) {
            float[] rgba = aPGB.getOutlineColor().getRGBComponents(null);
            gl.glLineWidth(aPGB.getOutlineSize());
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glBegin(GL2.GL_LINE_STRIP);
            for (int i = 0; i < aPG.getOutLine().size(); i++) {
                p = ((List<PointZ>) aPG.getOutLine()).get(i);
                gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
            }
            gl.glEnd();

            if (aPG.hasHole()) {
                List<PointZ> newPList;
                gl.glBegin(GL2.GL_LINE_STRIP);
                for (int h = 0; h < aPG.getHoleLines().size(); h++) {
                    newPList = (List<PointZ>) aPG.getHoleLines().get(h);
                    for (int j = 0; j < newPList.size(); j++) {
                        p = newPList.get(j);
                        gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
                    }
                }
                gl.glEnd();
            }
            gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
        }
    }

    private void drawConvexPolygon(GL2 gl, PolygonZ aPG, PolygonBreak aPGB) {
        PointZ p;
        if (aPGB.isDrawFill()) {
            float[] rgba = aPGB.getColor().getRGBComponents(null);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glBegin(GL2.GL_POLYGON);
            for (int i = 0; i < aPG.getOutLine().size(); i++) {
                p = ((List<PointZ>) aPG.getOutLine()).get(i);
                gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
            }
            gl.glEnd();
        }

        if (aPGB.isDrawOutline()) {
            float[] rgba = aPGB.getOutlineColor().getRGBComponents(null);
            gl.glLineWidth(aPGB.getOutlineSize());
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glBegin(GL2.GL_LINE_STRIP);
            for (int i = 0; i < aPG.getOutLine().size(); i++) {
                p = ((List<PointZ>) aPG.getOutLine()).get(i);
                gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
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
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glBegin(GL2.GL_QUADS);
            for (int i = 0; i < aPG.getOutLine().size(); i++) {
                p = ((List<PointZ>) aPG.getOutLine()).get(i);
                gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
            }
            gl.glEnd();
        }

        if (aPGB.isDrawOutline()) {
            rgba = aPGB.getOutlineColor().getRGBComponents(null);
            gl.glLineWidth(aPGB.getOutlineSize());
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glBegin(GL2.GL_LINE_STRIP);
            for (int i = 0; i < aPG.getOutLine().size(); i++) {
                p = ((List<PointZ>) aPG.getOutLine()).get(i);
                gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
            }
            gl.glEnd();
        }
    }

    private void drawTrianglePolygons(GL2 gl, GraphicCollection3D graphic) {
        PointZ p;
        for (int i = 0; i < graphic.getNumGraphics(); i++) {
            Graphic gg = graphic.getGraphicN(i);
            if (extent.intersects(gg.getExtent())) {
                PolygonZShape shape = (PolygonZShape) gg.getShape();
                PolygonBreak pb = (PolygonBreak) gg.getLegend();
                for (PolygonZ poly : (List<PolygonZ>) shape.getPolygons()) {
                    drawTriangle(gl, poly, pb);
                }
            }
        }
    }

    private void drawTriangle(GL2 gl, PolygonZ aPG, PolygonBreak aPGB) {
        PointZ p;
        float[] rgba = aPGB.getColor().getRGBComponents(null);
        if (aPGB.isDrawFill()) {
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glBegin(GL2.GL_TRIANGLES);
            for (int i = 0; i < aPG.getOutLine().size(); i++) {
                p = ((List<PointZ>) aPG.getOutLine()).get(i);
                gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
            }
            gl.glEnd();
        }

        if (aPGB.isDrawOutline()) {
            rgba = aPGB.getOutlineColor().getRGBComponents(null);
            gl.glLineWidth(aPGB.getOutlineSize());
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glBegin(GL2.GL_LINE_STRIP);
            for (int i = 0; i < aPG.getOutLine().size(); i++) {
                p = ((List<PointZ>) aPG.getOutLine()).get(i);
                gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
            }
            gl.glEnd();
        }
    }

    private void drawTriangle(GL2 gl, PointZ[] points, PolygonBreak aPGB) {
        PointZ p;
        float[] rgba = aPGB.getColor().getRGBComponents(null);
        if (aPGB.isDrawFill()) {
            //gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glBegin(GL2.GL_TRIANGLES);
            for (int i = 0; i < 3; i++) {
                p = points[i];
                gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
            }
            gl.glEnd();
        }

        if (aPGB.isDrawOutline()) {
            rgba = aPGB.getOutlineColor().getRGBComponents(null);
            gl.glLineWidth(aPGB.getOutlineSize());
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glBegin(GL2.GL_LINE_STRIP);
            for (int i = 0; i < 3; i++) {
                p = points[i];
                gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
            }
            gl.glEnd();
        }
    }

    private void drawSurface(GL2 gl, SurfaceGraphics surface) {
        PolygonBreak pgb = (PolygonBreak) surface.getLegendBreak(0, 0);
        int dim1 = surface.getDim1();
        int dim2 = surface.getDim2();
        float[] rgba;
        PointZ p;

        if (pgb.isDrawFill()) {
            gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
            gl.glPolygonOffset(1.0f, 1.0f);
            for (int i = 0; i < dim1 - 1; i++) {
                for (int j = 0; j < dim2 - 1; j++) {
                    gl.glBegin(GL2.GL_QUADS);
                    p = surface.getVertex(i, j);
                    rgba = surface.getRGBA(i, j);
                    gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                    gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
                    p = surface.getVertex(i + 1, j);
                    rgba = surface.getRGBA(i + 1, j);
                    gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                    gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
                    p = surface.getVertex(i + 1, j + 1);
                    rgba = surface.getRGBA(i + 1, j + 1);
                    gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                    gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
                    p = surface.getVertex(i, j + 1);
                    rgba = surface.getRGBA(i, j + 1);
                    gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                    gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
                    gl.glEnd();
                }
            }
            gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
        }

        if (pgb.isDrawOutline()) {
            rgba = pgb.getOutlineColor().getRGBComponents(null);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glLineWidth(pgb.getOutlineSize());
            for (int i = 0; i < dim1; i++) {
                gl.glBegin(GL2.GL_LINE_STRIP);
                for (int j = 0; j < dim2; j++) {
                    p = surface.getVertex(i, j);
                    gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
                }
                gl.glEnd();
            }
            for (int j = 0; j < dim2; j++) {
                gl.glBegin(GL2.GL_LINE_STRIP);
                for (int i = 0; i < dim1; i++) {
                    p = surface.getVertex(i, j);
                    gl.glVertex3f(transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z));
                }
                gl.glEnd();
            }
        }
    }

    private void drawIsosurface(GL2 gl, IsosurfaceGraphics isosurface) {
        List<PointZ[]> triangles = isosurface.getTriangles();
        PolygonBreak pgb = (PolygonBreak) isosurface.getLegendBreak();
        for (PointZ[] triangle : triangles) {
            this.drawTriangle(gl, triangle, pgb);
        }
    }

    private void drawImage(GL2 gl, Graphic graphic) {
        ImageShape ishape = (ImageShape) graphic.getShape();
        BufferedImage image = ishape.getImage();
        Texture texture = AWTTextureIO.newTexture(gl.getGLProfile(), image, true);
        //Texture texture = this.imageCache.get(image);
        int idTexture = texture.getTextureObject();
        List<PointZ> coords = ishape.getCoords();

        gl.glColor3f(1f, 1f, 1f);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, idTexture);

        // Texture parameterization
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);

        // Draw image
        gl.glBegin(GL2.GL_QUADS);
        // Front Face
        //gl.glTexCoord2f(0.0f, 0.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(transform_xf((float) coords.get(0).X), transform_yf((float) coords.get(0).Y), transform_zf((float) coords.get(0).Z));
        //gl.glTexCoord2f(1.0f, 0.0f);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(transform_xf((float) coords.get(1).X), transform_yf((float) coords.get(1).Y), transform_zf((float) coords.get(1).Z));
        //gl.glTexCoord2f(1.0f, 1.0f);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(transform_xf((float) coords.get(2).X), transform_yf((float) coords.get(2).Y), transform_zf((float) coords.get(2).Z));
        //gl.glTexCoord2f(0.0f, 1.0f);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(transform_xf((float) coords.get(3).X), transform_yf((float) coords.get(3).Y), transform_zf((float) coords.get(3).Z));
        gl.glEnd();

        // Unbinding the texture
        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
    }

    private void drawImage(GL2 gl) throws IOException {
        File im = new File("D:\\Temp\\image\\lenna.jpg ");
        BufferedImage image = ImageIO.read(im);
        Texture t = AWTTextureIO.newTexture(gl.getGLProfile(), image, true);
        //Texture t = TextureIO.newTexture(im, true);
        //Texture t = this.imageCache.get(image);
        int idTexture = t.getTextureObject(gl);

        gl.glColor3f(1f, 1f, 1f);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, idTexture);

//        // Texture parameterization
//        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);
//        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        // Draw image
        gl.glBegin(GL2.GL_QUADS);
        // Front Face
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);
        gl.glEnd();

        // Unbinding the texture
        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
    }

    private void drawTexture(GL2 gl, Graphic graphic) {
        TextureShape ishape = (TextureShape) graphic.getShape();
        Texture texture = ishape.getTexture();
        if (texture == null) {
            try {
                ishape.loadTexture();
                texture = ishape.getTexture();
            } catch (IOException ex) {
                Logger.getLogger(Plot3DGL.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (texture == null) {
            return;
        }

        int idTexture = texture.getTextureObject();
        List<PointZ> coords = ishape.getCoords();

        gl.glColor3f(1f, 1f, 1f);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, idTexture);

        // Texture parameterization
        //gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);

        // Draw image
        gl.glBegin(GL2.GL_QUADS);
        // Front Face
        gl.glTexCoord2f(0.0f, 0.0f);
        //gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(transform_xf((float) coords.get(0).X), transform_yf((float) coords.get(0).Y), transform_zf((float) coords.get(0).Z));
        gl.glTexCoord2f(1.0f, 0.0f);
        //gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(transform_xf((float) coords.get(1).X), transform_yf((float) coords.get(1).Y), transform_zf((float) coords.get(1).Z));
        gl.glTexCoord2f(1.0f, 1.0f);
        //gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(transform_xf((float) coords.get(2).X), transform_yf((float) coords.get(2).Y), transform_zf((float) coords.get(2).Z));
        gl.glTexCoord2f(0.0f, 1.0f);
        //gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(transform_xf((float) coords.get(3).X), transform_yf((float) coords.get(3).Y), transform_zf((float) coords.get(3).Z));
        gl.glEnd();

        // Unbinding the texture
        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
    }

    void drawWindArrow(GL2 gl, Graphic graphic) {
        if (extent.intersects(graphic.getExtent())) {
            WindArrow3D shape = (WindArrow3D) graphic.getShape();
            PointBreak pb = (PointBreak) graphic.getLegend();
            PointZ sp = (PointZ) shape.getPoint();
            PointZ ep = (PointZ) shape.getEndPoint();
            float x1 = transform_xf((float) sp.X);
            float y1 = transform_yf((float) sp.Y);
            float z1 = transform_zf((float) sp.Z);
            float x2 = transform_xf((float) ep.X);
            float y2 = transform_yf((float) ep.Y);
            float z2 = transform_zf((float) ep.Z);

            gl.glPushMatrix();
            gl.glPushAttrib(GL2.GL_POLYGON_BIT); // includes GL_CULL_FACE
            gl.glDisable(GL2.GL_CULL_FACE); // draw from all sides

            float[] rgba = pb.getColor().getRGBComponents(null);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glLineWidth(pb.getOutlineSize());
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3f(x1, y1, z1);
            gl.glVertex3f(x2, y2, z2);
            gl.glEnd();

            // Calculate vector along direction of line
            float[] v = {x2 - x1, y2 - y1, z2 - z1};
            float norm_of_v = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);

            // Size of cone in arrow:
            //float coneFractionAxially = 0.025f; // radius at thickest part
            //float coneFractionRadially = 0.12f; // length of arrow

            //float coneHgt = coneFractionAxially * norm_of_v;
            //float coneRadius = coneFractionRadially * norm_of_v;
            float coneRadius = 0.05f;
            float coneHgt = coneRadius * 0.4f;

            // Set location of arrowhead to be at the startpoint of the line
            float[] vConeLocation = {x1, y1, z1};

            // Initialize transformation matrix
            float[] mat44
                    = {1, 0, 0, 0,
                        0, 1, 0, 0,
                        0, 0, 1, 0,
                        0, 0, 0, 1};

            // The direction of the arrowhead is the line vector
            float[] dVec = {v[0], v[1], v[2]};

            // Normalize dVec to get Unit Vector norm_startVec
            float[] norm_startVec = VectorUtil.normalizeVec3(dVec);

            // Normalize zaxis to get Unit Vector norm_endVec
            float[] zaxis = {0.0f, 0.0f, 1.0f};
            float[] norm_endVec = VectorUtil.normalizeVec3(zaxis);

            if (Float.isNaN(norm_endVec[0]) || Float.isNaN(norm_endVec[1]) || Float.isNaN(norm_endVec[2])) {
                norm_endVec[0] = 0.0f;
                norm_endVec[1] = 0.0f;
                norm_endVec[2] = 0.0f;
            }

            // If vectors are identical, set transformation matrix to identity
            if (((norm_startVec[0] - norm_endVec[0]) > 1e-14) && ((norm_startVec[1] - norm_endVec[1]) > 1e-14) && ((norm_startVec[2] - norm_endVec[2]) > 1e-14)) {
                mat44[0] = 1.0f;
                mat44[5] = 1.0f;
                mat44[10] = 1.0f;
                mat44[15] = 1.0f;
            } // otherwise create the matrix
            else {
                // Vector cross-product, result = axb
                float[] axb = new float[3];
                VectorUtil.crossVec3(axb, norm_startVec, norm_endVec);

                // Normalize axb to get Unit Vector norm_axb
                float[] norm_axb = VectorUtil.normalizeVec3(axb);

                if (Float.isNaN(norm_axb[0]) || Float.isNaN(norm_axb[1]) || Float.isNaN(norm_axb[2])) {
                    norm_axb[0] = 0.0f;
                    norm_axb[1] = 0.0f;
                    norm_axb[2] = 0.0f;
                }

                // Build the rotation matrix
                float ac = (float) Math.acos(VectorUtil.dotVec3(norm_startVec, norm_endVec));

                float s = (float) Math.sin(ac);
                float c = (float) Math.cos(ac);
                float t = 1 - c;

                float x = norm_axb[0];
                float y = norm_axb[1];
                float z = norm_axb[2];

                // Fill top-left 3x3
                mat44[0] = t * x * x + c;
                mat44[1] = t * x * y - s * z;
                mat44[2] = t * x * z + s * y;

                mat44[4] = t * x * y + s * z;
                mat44[5] = t * y * y + c;
                mat44[6] = t * y * z - s * x;

                mat44[8] = t * x * z - s * y;
                mat44[9] = t * y * z + s * x;
                mat44[10] = t * z * z + c;

                mat44[15] = 1.0f;
            }

            // Translate and rotate arrowhead to correct position
            gl.glTranslatef(vConeLocation[0], vConeLocation[1], vConeLocation[2]);
            gl.glMultMatrixf(mat44, 0);

            GLUquadric cone_obj = glu.gluNewQuadric();
            glu.gluCylinder(cone_obj, 0, coneHgt, coneRadius, 8, 1);

            gl.glPopAttrib(); // GL_CULL_FACE
            gl.glPopMatrix();
        }
    }

    void drawCircle(GL2 gl, float z, float radius, PolygonBreak bb) {
        int points = 100;

        if (bb.isDrawFill()) {
            float[] rgba = bb.getColor().getRGBComponents(null);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glBegin(GL2.GL_TRIANGLE_FAN);
            double angle = 0.0;
            for(int i =0; i < points;i++){
                angle = 2 * Math.PI * i / points;
                gl.glVertex3f((float)Math.cos(angle) * radius, (float)Math.sin(angle) * radius, z);
            }
            gl.glEnd();
        }

        if (bb.isDrawOutline()) {
            float[] rgba = bb.getOutlineColor().getRGBComponents(null);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glLineWidth(bb.getOutlineSize());
            gl.glBegin(GL2.GL_LINE_LOOP);
            double angle = 0.0;
            for (int i = 0; i < points; i++) {
                angle = 2 * Math.PI * i / points;
                gl.glVertex3f((float) Math.cos(angle) * radius, (float) Math.sin(angle) * radius, z);
            }
            gl.glEnd();
        }
    }

    void drawCubic(GL2 gl, Graphic graphic) {
        if (extent.intersects(graphic.getExtent())) {
            CubicShape cubic = (CubicShape) graphic.getShape();
            BarBreak bb = (BarBreak) graphic.getLegend();
            List<PointZ> ps = cubic.getPoints();
            List<float[]> vertex = new ArrayList<>();
            for (PointZ p : ps) {
                vertex.add(new float[]{transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z)});
            }

            gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
            gl.glPolygonOffset(1.0f, 1.0f);
            int[][] index = cubic.getIndex();
            float[] rgba = bb.getColor().getRGBComponents(null);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glBegin(GL2.GL_QUADS);
            for (int[] ii : index) {
                for (int i : ii) {
                    gl.glVertex3f(vertex.get(i)[0], vertex.get(i)[1], vertex.get(i)[2]);
                }
            }
            gl.glEnd();
            gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);

            rgba = bb.getOutlineColor().getRGBComponents(null);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glLineWidth(bb.getOutlineSize());
            gl.glBegin(GL2.GL_LINES);
            for (int[] ii : cubic.getLineIndex()) {
                for (int i : ii) {
                    gl.glVertex3f(vertex.get(i)[0], vertex.get(i)[1], vertex.get(i)[2]);
                }
            }
            gl.glEnd();
        }
    }

    void drawCylinder(GL2 gl, Graphic graphic) {
        if (extent.intersects(graphic.getExtent())) {
            CylinderShape cylinder = (CylinderShape) graphic.getShape();
            BarBreak bb = (BarBreak) graphic.getLegend();
            List<PointZ> ps = cylinder.getPoints();
            List<float[]> vertex = new ArrayList<>();
            for (PointZ p : ps) {
                vertex.add(new float[]{transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z)});
            }
            double height = vertex.get(1)[2] - vertex.get(0)[2];

            gl.glPushMatrix();
            gl.glPushAttrib(GL2.GL_POLYGON_BIT); // includes GL_CULL_FACE
            gl.glDisable(GL2.GL_CULL_FACE); // draw from all sides

            float[] rgba = bb.getColor().getRGBComponents(null);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glTranslatef(vertex.get(0)[0], vertex.get(0)[1], vertex.get(0)[2]);
            GLUquadric cone_obj = glu.gluNewQuadric();
            glu.gluCylinder(cone_obj, cylinder.getRadius(), cylinder.getRadius(), height, 100, 1);
            bb.setDrawOutline(false);
            this.drawCircle(gl, (float) height, (float) cylinder.getRadius(), bb);

            gl.glPopAttrib(); // GL_CULL_FACE
            gl.glPopMatrix();
        }
    }

    void drawLegend(GL2 gl) {
        if (!this.legends.isEmpty()) {
            ChartColorBar legend = (ChartColorBar) this.legends.get(0);
            LegendScheme ls = legend.getLegendScheme();
            int bNum = ls.getBreakNum();
            if (ls.getLegendBreaks().get(bNum - 1).isNoData()) {
                bNum -= 1;
            }

            float x = 1.6f;
            x += legend.getXShift() * this.lenScale;
            float y = -1.0f;
            float lHeight = 2.0f;
            float lWidth = lHeight / legend.getAspect();
            List<Integer> labelIdxs = new ArrayList<>();
            List<String> tLabels = new ArrayList<>();
            if (legend.isAutoTick()) {
                float legendLen = this.toScreenLength(x, y, 0.0f, x, y + lHeight, 0.0f);
                int tickGap = this.getLegendTickGap(legend, legendLen);
                int sIdx = (bNum % tickGap) / 2;
                int labNum = bNum - 1;
                if (ls.getLegendType() == LegendType.UniqueValue) {
                    labNum += 1;
                } else if (legend.isDrawMinLabel()) {
                    sIdx = 0;
                    labNum = bNum;
                }
                while (sIdx < labNum) {
                    labelIdxs.add(sIdx);
                    sIdx += tickGap;
                }
            } else {
                int tickIdx;
                for (int i = 0; i < bNum; i++) {
                    ColorBreak cb = ls.getLegendBreaks().get(i);
                    double v = Double.parseDouble(cb.getEndValue().toString());
                    if (legend.getTickLocations().contains(v)) {
                        labelIdxs.add(i);
                        tickIdx = legend.getTickLocations().indexOf(v);
                        tLabels.add(legend.getTickLabels().get(tickIdx).getText());
                    }
                }
            }

            float barHeight = lHeight / bNum;

            //Draw color bar
            float yy = y;
            float[] rgba;
            for (int i = 0; i < bNum; i++) {
                //Fill color
                rgba = ls.getLegendBreak(i).getColor().getRGBComponents(null);
                gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                gl.glBegin(GL2.GL_QUADS);
                gl.glVertex2f(x, yy);
                gl.glVertex2f(x + lWidth, yy);
                gl.glVertex2f(x + lWidth, yy + barHeight);
                gl.glVertex2f(x, yy + barHeight);
                gl.glEnd();
                yy += barHeight;
            }

            //Draw neatline
            rgba = Color.black.getRGBComponents(null);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glLineWidth(1.0f);
            gl.glBegin(GL2.GL_LINE_STRIP);
            gl.glVertex2f(x, y);
            gl.glVertex2f(x, y + lHeight);
            gl.glVertex2f(x + lWidth, y + lHeight);
            gl.glVertex2f(x + lWidth, y);
            gl.glVertex2f(x, y);
            gl.glEnd();

            //Draw ticks
            int idx = 0;
            yy = y;
            String caption;
            float tickLen = legend.getTickLength() * this.lenScale;
            float labelX = x + lWidth;
            if (legend.isInsideTick()) {
                if (tickLen > lWidth)
                    tickLen = lWidth;
            } else {
                labelX += tickLen;
            }
            float strWidth = 0;
            Rectangle2D rect;
            for (int i = 0; i < bNum; i++) {
                if (labelIdxs.contains(i)) {
                    ColorBreak cb = ls.getLegendBreaks().get(i);
                    if (legend.isAutoTick()) {
                        if (ls.getLegendType() == LegendType.UniqueValue) {
                            caption = cb.getCaption();
                        } else {
                            caption = DataConvert.removeTailingZeros(cb.getEndValue().toString());
                        }
                    } else {
                        caption = tLabels.get(idx);
                    }
                    if (ls.getLegendType() == LegendType.UniqueValue) {
                        rect = this.drawString(gl, caption, legend.getTickLabelFont(), legend.getTickLabelColor(),
                                x + lWidth, yy + barHeight * 0.5f, 0, XAlign.LEFT, YAlign.CENTER, 5, 0);
                    } else {
                        rgba = Color.black.getRGBComponents(null);
                        gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
                        gl.glLineWidth(legend.getTickWidth());
                        gl.glBegin(GL2.GL_LINES);
                        if (legend.isInsideTick())
                            gl.glVertex2f(x + lWidth - tickLen, yy + barHeight);
                        else
                            gl.glVertex2f(x + lWidth + tickLen, yy + barHeight);
                        gl.glVertex2f(x + lWidth, yy + barHeight);
                        gl.glEnd();
                        rect = this.drawString(gl, caption, legend.getTickLabelFont(), legend.getTickLabelColor(),
                                labelX, yy + barHeight, 0, XAlign.LEFT, YAlign.CENTER, 5, 0);
                    }
                    if (strWidth < rect.getWidth())
                        strWidth = (float) rect.getWidth();

                    idx += 1;
                }
                yy += barHeight;
            }

            //Draw label
            ChartText label = legend.getLabel();
            if (label != null) {
                float sx, sy, yShift;
                switch (legend.getLabelLocation()) {
                    case "top":
                        sx = x + lWidth * 0.5f;
                        sy = y + lHeight;
                        yShift = this.tickSpace;
                        drawString(gl, label, sx, sy, 0.0f, XAlign.CENTER, YAlign.BOTTOM, 0, 0, yShift);
                        break;
                    case "bottom":
                        sx = x + lWidth * 0.5f;
                        sy = y;
                        yShift = -this.tickSpace;
                        drawString(gl, label, sx, sy, 0.0f, XAlign.CENTER, YAlign.TOP, 0, 0, yShift);
                        break;
                    case "left":
                    case "in":
                        sx = x;
                        sy = y + lHeight * 0.5f;
                        yShift = this.tickSpace;
                        drawString(gl, label, sx, sy, 0.0f, XAlign.CENTER, YAlign.BOTTOM, 90.f, 0, yShift);
                        break;
                    default:
                        sx = labelX;
                        sy = y + lHeight * 0.5f;
                        yShift = -strWidth - this.tickSpace;
                        drawString(gl, label, sx, sy, 0.0f, XAlign.CENTER, YAlign.TOP, 90.f, 0, yShift);
                        break;
                }
            }
        }
    }

    private float transform_xf(float v) {
        return (v - xmin) / (xmax - xmin) * 2.f - 1.0f;
    }

    private double transform_x(double v) {
        return (v - xmin) / (xmax - xmin) * 2. - 1.0;
    }

    private float transform_yf(float v) {
        return (v - ymin) / (ymax - ymin) * 2.f - 1.0f;
    }

    private double transform_y(double v) {
        return (v - ymin) / (ymax - ymin) * 2. - 1.0;
    }

    private float transform_zf(float v) {
        return (v - zmin) / (zmax - zmin) * 2.f - 1.0f;
    }

    private double transform_z(double v) {
        return (v - zmin) / (zmax - zmin) * 2. - 1.0;
    }

    private float[] transformf(PointZ p) {
        return new float[]{transform_xf((float) p.X), transform_yf((float) p.Y), transform_zf((float) p.Z)};
    }

    private double[] transform(PointZ p) {
        return new double[]{transform_x(p.X), transform_y(p.Y), transform_z(p.Z)};
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
        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glDepthFunc(GL2.GL_LEQUAL);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
        gl.glEnable(GL2.GL_TEXTURE_2D);

        //jogl specific addition for tessellation
        tessCallback = new tessellCallBack(gl, glu);

        this.positionArea = new Rectangle2D.Double(0, 0, 1, 1);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        this.width = width;
        this.height = height;
        this.positionArea = this.getPositionArea(new Rectangle2D.Double(0, 0, width, height));

        final GL2 gl = drawable.getGL().getGL2();
        //gl.glTranslatef(0f, 0f, 5f);
        if (height <= 0) {
            height = 1;
        }

        final float h = (float) width / (float) height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        //glu.gluPerspective(45.0f, h, 1.0, 20.0);
        float v = 2.0f;
        gl.glOrthof(-v, v, -v, v, -v, v);
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

        //gc.animator_start();
    }
    // </editor-fold>

    /*
     * Tessellator callback implemenation with all the callback routines. YOu
     * could use GLUtesselatorCallBackAdapter instead. But
     */
    class tessellCallBack
            implements GLUtessellatorCallback {

        private final GL2 gl;
        private final GLU glu;

        public tessellCallBack(GL2 gl, GLU glu) {
            this.gl = gl;
            this.glu = glu;
        }

        @Override
        public void begin(int type) {
            gl.glBegin(type);
        }

        @Override
        public void end() {
            gl.glEnd();
        }

        @Override
        public void vertex(Object vertexData) {
            double[] pointer;
            if (vertexData instanceof double[]) {
                pointer = (double[]) vertexData;
                if (pointer.length == 6) {
                    gl.glColor3dv(pointer, 3);
                }
                gl.glVertex3dv(pointer, 0);
            }
        }

        @Override
        public void vertexData(Object vertexData, Object polygonData) {
        }

        /*
         * combineCallback is used to create a new vertex when edges intersect.
         * coordinate location is trivial to calculate, but weight[4] may be used to
         * average color, normal, or texture coordinate data. In this program, color
         * is weighted.
         */
        @Override
        public void combine(double[] coords, Object[] data, //
                float[] weight, Object[] outData) {
            double[] vertex = new double[3];
            //int i;

            vertex[0] = coords[0];
            vertex[1] = coords[1];
            vertex[2] = coords[2];
//            for (i = 3; i < 6/* 7OutOfBounds from C! */; i++) {
//                vertex[i] = weight[0] //
//                        * ((double[]) data[0])[i] + weight[1]
//                        * ((double[]) data[1])[i] + weight[2]
//                        * ((double[]) data[2])[i] + weight[3]
//                        * ((double[]) data[3])[i];
//            }
            outData[0] = vertex;
        }

        @Override
        public void combineData(double[] coords, Object[] data, //
                float[] weight, Object[] outData, Object polygonData) {
        }

        @Override
        public void error(int errnum) {
            String estring;

            estring = glu.gluErrorString(errnum);
            System.err.println("Tessellation Error: " + estring);
            System.exit(0);
        }

        @Override
        public void beginData(int type, Object polygonData) {
        }

        @Override
        public void endData(Object polygonData) {
        }

        @Override
        public void edgeFlag(boolean boundaryEdge) {
        }

        @Override
        public void edgeFlagData(boolean boundaryEdge, Object polygonData) {
        }

        @Override
        public void errorData(int errnum, Object polygonData) {
        }
    }// tessellCallBack
}
