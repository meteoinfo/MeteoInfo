package org.meteoinfo.chart.jogl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import org.apache.commons.imaging.ImageReadException;
import org.joml.Vector3f;
import org.meteoinfo.chart.ChartText3D;
import org.meteoinfo.chart.axis.Axis;
import org.meteoinfo.chart.graphic.GraphicCollection3D;
import org.meteoinfo.chart.graphic.SurfaceGraphics;
import org.meteoinfo.chart.plot.GridLine;
import org.meteoinfo.common.Extent;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.geo.legend.LegendManage;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.legend.PolygonBreak;
import org.meteoinfo.geometry.shape.Shape;
import org.meteoinfo.geometry.shape.ShapeTypes;
import org.meteoinfo.image.ImageUtil;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.InvalidRangeException;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class EarthPlot3D extends Plot3DGL {
    // <editor-fold desc="Variables">
    private float radius = 6371.f;
    private SurfaceGraphics surface;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public EarthPlot3D() {
        super();

        //earthSurface(50);
        //addGraphic(this.surface);
    }

    /**
     * Initialize angles
     */
    public void initAngles() {
        this.angleX = -45.f;
        this.angleY = 160.f;
        this.headAngle = 0.f;
        this.pitchAngle = 0.f;
    }

    // </editor-fold>
    // <editor-fold desc="GetSet">

    /**
     * Get earth radius
     * @return Earth radius
     */
    public float getRadius() {
        return this.radius;
    }

    /**
     * Set earth radius
     * @param value Earth radius
     */
    public void setRadius(float value) {
        this.radius = value;
    }

    // </editor-fold>
    // <editor-fold desc="Method">

    /**
     * Add a graphic
     * @param graphic The graphic
     */
    @Override
    public void addGraphic(Graphic graphic) {
        this.graphics.add(SphericalTransform.transform(graphic));
        Extent ex = this.graphics.getExtent();
        if (!ex.is3D()) {
            ex = ex.to3D();
        }
        this.extent = (Extent3D) ex;
        this.setDrawExtent((Extent3D) this.extent.clone());
    }

    /**
     * Set earth surface
     * @param n The sphere has n*n faces
     */
    public SurfaceGraphics earthSurface(int n) {
        Array lon = ArrayUtil.lineSpace(-180.f, 180.f, n + 1, true);
        Array lat = ArrayUtil.lineSpace(-90.f, 90.f, n + 1, true);
        lat = lat.flip(0).copy();
        Array[] lonlat = ArrayUtil.meshgrid(lon, lat);
        lon = lonlat[0];
        lat = lonlat[1];
        Array alt = ArrayUtil.zeros(lon.getShape(), DataType.FLOAT);
        LegendScheme ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POLYGON, Color.cyan, 1);
        ((PolygonBreak) ls.getLegendBreak(0)).setDrawOutline(false);
        ((PolygonBreak) ls.getLegendBreak(0)).setOutlineColor(Color.white);
        surface = JOGLUtil.surface(lon, lat, alt, ls);

        return surface;
    }

    /**
     * Set earth image
     * @param imageFile
     * @throws IOException
     * @throws ImageReadException
     */
    public void earthImage(String imageFile) throws IOException, ImageReadException {
        BufferedImage image = ImageUtil.imageLoad(imageFile);
        if (this.surface == null) {
            this.earthSurface(50);
            this.addGraphic(this.surface);
        }
        this.surface.setImage(image);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();
        float[] rgba = this.background.getRGBComponents(null);
        gl.glClearColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        this.lighting.setPosition(gl);

        gl.glPushMatrix();

        gl.glShadeModel(GL2.GL_SMOOTH);

        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        //gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);

        if (this.antialias) {
            if (this.sampleBuffers)
                gl.glEnable(GL2.GL_MULTISAMPLE);
            else {
                gl.glEnable(GL2.GL_LINE_SMOOTH);
                gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
                gl.glEnable(GL2.GL_POINT_SMOOTH);
                gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
                //gl.glEnable(GL2.GL_POLYGON_SMOOTH);
                //gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
            }
        } else {
            if (this.sampleBuffers)
                gl.glDisable(GL2.GL_MULTISAMPLE);
            else {
                gl.glDisable(GL2.GL_LINE_SMOOTH);
                gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_FASTEST);
                gl.glDisable(GL2.GL_POINT_SMOOTH);
                gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_FASTEST);
                //gl.glDisable(GL2.GL_POLYGON_SMOOTH);
                //gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_FASTEST);
            }
        }

        //gl.glScalef(scaleX, scaleY, scaleZ);

        if (pitchAngle != 0) {
            float scale = getScale();
            gl.glTranslatef(0, 0, scale);
            gl.glRotatef(70.f * (pitchAngle / 90.f), 1.0f, 0.0f, 0.0f);
            gl.glTranslatef(0, 0, -scale);
        }
        gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(angleY, 0.0f, 0.0f, 1.0f);
        if (headAngle != 0) {
            gl.glRotatef(headAngle, 0.0f, 1.0f, 0.0f);
        }

        this.updateMatrix(gl);

        //Draw title
        this.drawTitle();

        this.setLight(gl);

        //Draw graphics
        for (int m = 0; m < this.graphics.getNumGraphics(); m++) {
            Graphic graphic = this.graphics.get(m);
            drawGraphics(gl, graphic);
        }

        //Draw text
        for (int m = 0; m < this.graphics.getNumGraphics(); m++) {
            Graphic graphic = this.graphics.get(m);
            if (graphic.getNumGraphics() == 1) {
                org.meteoinfo.geometry.shape.Shape shape = graphic.getGraphicN(0).getShape();
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

        //Do screen-shot
        if (this.doScreenShot) {
            AWTGLReadBufferUtil glReadBufferUtil = new AWTGLReadBufferUtil(drawable.getGLProfile(), false);
            this.screenImage = glReadBufferUtil.readPixelsToBufferedImage(drawable.getGL(), true);
            this.doScreenShot = false;
        }
    }

    private float getScale() {
        return (float) (this.extent.getWidth() / this.drawExtent.getWidth());
    }
    // </editor-fold>
}
