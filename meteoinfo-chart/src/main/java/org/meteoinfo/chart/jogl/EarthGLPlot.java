package org.meteoinfo.chart.jogl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import org.apache.commons.imaging.ImageReadException;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.meteoinfo.chart.ChartColorBar;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.chart.ChartText3D;
import org.meteoinfo.chart.graphic.GraphicFactory;
import org.meteoinfo.chart.graphic.MeshGraphic;
import org.meteoinfo.common.*;
import org.meteoinfo.geometry.legend.LegendManage;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.legend.PolygonBreak;
import org.meteoinfo.geometry.shape.Shape;
import org.meteoinfo.geometry.shape.ShapeTypes;
import org.meteoinfo.image.ImageUtil;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.math.ArrayUtil;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.projection.ProjectionUtil;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.List;

public class EarthGLPlot extends GLPlot {
    // <editor-fold desc="Variables">
    private float radius = 6371.f;
    private MeshGraphic surface;
    private Extent3D dataExtent;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public EarthGLPlot() {
        super();

        this.projInfo = KnownCoordinateSystems.geographic.world.WGS1984;
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

    @Override
    public void setDrawExtent(Extent value) {
        this.drawExtent = (Extent3D) value;
        this.transform.setExtent(this.drawExtent);
    }

    // </editor-fold>
    // <editor-fold desc="Method">

    void updateDataExtent() {
        xAxis.setMinMaxValue(dataExtent.minX, dataExtent.maxX);
        yAxis.setMinMaxValue(dataExtent.minY, dataExtent.maxY);
        zAxis.setMinMaxValue(dataExtent.minZ, dataExtent.maxZ);
    }

    /**
     * Add a graphic
     * @param graphic The graphic
     */
    @Override
    public void addGraphic(Graphic graphic) {
        if (this.dataExtent == null) {
            this.dataExtent = (Extent3D) graphic.getExtent();
        } else {
            this.dataExtent = this.dataExtent.union((Extent3D) graphic.getExtent());
        }
        updateDataExtent();

        this.graphics.add(SphericalTransform.transform(graphic));
        Extent ex = this.graphics.getExtent();
        if (!ex.is3D()) {
            ex = ex.to3D();
        }
        this.graphicExtent = (Extent3D) ex;
        this.setDrawExtent((Extent3D) this.graphicExtent.clone());
    }

    /**
     * Add a graphic
     *
     * @param index The index
     * @param graphic The graphic
     */
    @Override
    public void addGraphic(int index, Graphic graphic) {
        if (this.dataExtent == null) {
            this.dataExtent = (Extent3D) graphic.getExtent();
        } else {
            this.dataExtent = this.dataExtent.union((Extent3D) graphic.getExtent());
        }
        updateDataExtent();

        this.graphics.add(index, SphericalTransform.transform(graphic));
        Extent ex = this.graphics.getExtent();
        if (!ex.is3D()) {
            ex = ex.to3D();
        }
        this.graphicExtent = (Extent3D) ex;
        this.setDrawExtent((Extent3D) this.graphicExtent.clone());
    }

    /**
     * Add a graphic
     *
     * @param graphic The graphic
     * @param proj The graphic projection
     */
    @Override
    public void addGraphic(Graphic graphic, ProjectionInfo proj) {
        if (! proj.equals(this.projInfo)) {
            Graphic nGraphic = ProjectionUtil.projectGraphic(graphic, proj, this.projInfo);
            addGraphic(nGraphic);
        } else {
            addGraphic(graphic);
        }
    }

    /**
     * Add a graphic
     *
     * @param index The index
     * @param graphic The graphic
     * @param proj The graphic projection
     */
    @Override
    public void addGraphic(int index, Graphic graphic, ProjectionInfo proj) {
        if (! proj.equals(this.projInfo)) {
            Graphic nGraphic = ProjectionUtil.projectGraphic(graphic, proj, this.projInfo);
            addGraphic(index, nGraphic);
        } else {
            addGraphic(index, graphic);
        }
    }

    /**
     * Set earth surface
     * @param n The sphere has n*n faces
     */
    public MeshGraphic earthSurface(int n) {
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
        surface = GraphicFactory.surface(lon, lat, alt, ls);
        surface.setFaceInterp(true);

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
        gl.glLoadIdentity();

        this.updateTextRender(this.xAxis.getTickLabelFont());

        //Set light position - follow glLoadIdentity
        this.lighting.setPosition(gl);

        gl.glPushMatrix();

        this.modelViewMatrix = new Matrix4f();
        if (pitchAngle != 0) {
            float ss = getScale();
            modelViewMatrix.translate(0, 0, ss);
            modelViewMatrix.rotate((float) Math.toRadians(70.f * (pitchAngle / 90.f)), 1.0f, 0.0f, 0.0f);
            modelViewMatrix.translate(0, 0, -ss);
        }
        modelViewMatrix.rotate((float) Math.toRadians(angleX), 1.0f, 0.0f, 0.0f);
        modelViewMatrix.rotate((float) Math.toRadians(angleY), 0.0f, 0.0f, 1.0f);
        if (headAngle != 0) {
            modelViewMatrix.rotate((float) Math.toRadians(headAngle), 0.0f, 1.0f, 0.0f);
        }
        modelViewMatrixR = new Matrix4f(modelViewMatrix);

        Vector3f center = transform.getCenter();
        Vector3f scale = transform.getScale();
        modelViewMatrix.scale(scale);
        modelViewMatrix.translate(center.negate());

        FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
        gl.glLoadMatrixf(modelViewMatrix.get(fb));

        /*if (pitchAngle != 0) {
            float scale = getScale();
            gl.glTranslatef(0, 0, scale);
            gl.glRotatef(70.f * (pitchAngle / 90.f), 1.0f, 0.0f, 0.0f);
            gl.glTranslatef(0, 0, -scale);
        }
        gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(angleY, 0.0f, 0.0f, 1.0f);
        if (headAngle != 0) {
            gl.glRotatef(headAngle, 0.0f, 1.0f, 0.0f);
        }*/

        this.updateMatrix(gl);

        //Lighting
        this.setLight(gl);

        for (int m = 0; m < this.graphics.getNumGraphics(); m++) {
            Graphic graphic = this.graphics.get(m);
            drawGraphics(gl, graphic);
        }

        //Stop lighting
        if (this.lighting.isEnable()) {
            this.lighting.stop(gl);
        }

        //Draw axis
        this.drawAllZAxis(gl);

        //Draw legend
        gl.glPopMatrix();
        this.updateMatrix(gl);
        if (!this.legends.isEmpty()) {
            ChartColorBar legend = (ChartColorBar) this.legends.get(0);
            if (legend.getLegendScheme().getColorMap() == null)
                this.drawLegend(gl, legend);
            else
                this.drawColorbar(gl, legend);
        }

        //Draw title
        this.drawTitle();

        this.textRenderer.dispose();
        this.textRenderer = null;

        gl.glFlush();

        /*//Do screenshot
        if (this.doScreenShot) {
            AWTGLReadBufferUtil glReadBufferUtil = new AWTGLReadBufferUtil(drawable.getGLProfile(), false);
            this.screenImage = glReadBufferUtil.readPixelsToBufferedImage(drawable.getGL(), true);
            this.doScreenShot = false;
        }*/

        //Disable always update buffers
        if (this.alwaysUpdateBuffers)
            this.alwaysUpdateBuffers = false;
    }

    @Override
    protected void drawZAxis(GL2 gl, PointF loc) {
        float[] rgba;
        float x, y, z, v;
        int skip = 1;
        XAlign xAlign;
        YAlign yAlign;
        Rectangle2D rect;
        float strWidth, strHeight;

        //z axis line
        rgba = this.zAxis.getLineColor().getRGBComponents(null);
        gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
        gl.glLineWidth(this.zAxis.getLineWidth() * this.dpiScale);
        gl.glBegin(GL2.GL_LINES);
        Vector3f xyz = SphericalTransform.transform(loc.X, loc.Y, (float) this.dataExtent.minZ);
        x = xyz.x;
        y = xyz.y;
        z = xyz.z;
        gl.glVertex3f(x, y, z);
        xyz = SphericalTransform.transform(loc.X, loc.Y, (float) this.dataExtent.maxZ);
        x = xyz.x;
        y = xyz.y;
        z = xyz.z;
        gl.glVertex3f(x, y, z);
        gl.glEnd();

        //z axis ticks
        this.zAxis.updateTickLabels();
        List<ChartText> tlabs = this.zAxis.getTickLabels();
        //float axisLen = this.toScreenLength(x, y, zMin, x, y, zMax);
        //skip = getLabelGap(this.zAxis.getTickLabelFont(), tlabs, axisLen);
        float x1 = x;
        float y1 = y;
        float tickLen = this.zAxis.getTickLength() * this.lenScale;
        xAlign = XAlign.RIGHT;
        yAlign = YAlign.CENTER;
        Vector3f center = this.transform.getCenter();
        strWidth = 0.0f;
        for (int i = 0; i < this.zAxis.getTickValues().length; i += skip) {
            v = (float) this.zAxis.getTickValues()[i];
            if (v < dataExtent.minZ || v > dataExtent.maxZ) {
                continue;
            }

            xyz = SphericalTransform.transform(loc.X, loc.Y, v);
            x = xyz.x;
            y = xyz.y;
            z = xyz.z;
            x1 = x;
            y1 = y;
            if (x < center.x) {
                if (y > center.y) {
                    y1 += tickLen;
                } else {
                    x1 -= tickLen;
                }
            } else {
                if (y > center.y) {
                    x1 += tickLen;
                } else {
                    y1 -= tickLen;
                }
            }
            if (i == tlabs.size()) {
                break;
            }

            //Draw tick line
            rgba = this.zAxis.getLineColor().getRGBComponents(null);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glLineWidth(this.zAxis.getLineWidth() * this.dpiScale);
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3f(x, y, z);
            gl.glVertex3f(x1, y1, z);
            gl.glEnd();

            //Draw tick label
            rect = drawString(gl, tlabs.get(i), x1, y1, z, xAlign, yAlign, -this.tickSpace, 0);
            if (strWidth < rect.getWidth()) {
                strWidth = (float) rect.getWidth();
            }
        }

        //Draw z axis label
        ChartText label = this.zAxis.getLabel();
        if (label != null) {
            v = (float) (dataExtent.minZ + dataExtent.maxZ) / 2;
            xyz = SphericalTransform.transform(loc.X, loc.Y, v);
            x = xyz.x;
            y = xyz.y;
            z = xyz.z;
            x1 = x;
            y1 = y;
            if (x < center.x) {
                if (y > center.y) {
                    y1 += tickLen;
                } else {
                    x1 -= tickLen;
                }
            } else {
                if (y > center.y) {
                    x1 += tickLen;
                } else {
                    y1 -= tickLen;
                }
            }
            float yShift = strWidth + this.tickSpace * 3;
            drawString(gl, label, x1, y1, z, XAlign.CENTER, YAlign.BOTTOM, 90.f, 0, yShift);
        }
    }

    protected void drawZAxis(GL2 gl, ZAxisOption zAxisOption) {
        Matrix4f mvMatrix = toMatrix(mvmatrix);
        gl.glPushMatrix();

        float[] rgba;
        float v;
        int skip = 1;
        XAlign xAlign;
        YAlign yAlign;
        Rectangle2D rect;
        float strWidth, strHeight;

        PointF loc = zAxisOption.getLocation();
        boolean left = zAxisOption.isLeft();

        //z axis line
        rgba = this.zAxis.getLineColor().getRGBComponents(null);
        gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
        gl.glLineWidth(this.zAxis.getLineWidth() * this.dpiScale);
        gl.glBegin(GL2.GL_LINES);
        Vector3f xyz = SphericalTransform.transform(loc.X, loc.Y, (float) this.dataExtent.minZ);
        //xyz = this.transform.transform(xyz);
        gl.glVertex3f(xyz.x, xyz.y, xyz.z);
        Vector3f xyz1 = SphericalTransform.transform(loc.X, loc.Y, (float) this.dataExtent.maxZ);
        //xyz1 = this.transform.transform(xyz1);
        gl.glVertex3f(xyz1.x, xyz1.y, xyz1.z);
        gl.glEnd();

        //Load identity
        gl.glLoadIdentity();
        this.updateMatrix(gl);

        //z axis ticks positions
        this.zAxis.updateTickLabels();
        List<ChartText> tlabs = this.zAxis.getTickLabels();
        float tickLen = this.zAxis.getTickLength() * this.lenScale;
        xAlign = XAlign.RIGHT;
        yAlign = YAlign.CENTER;
        strWidth = 0.0f;
        for (int i = 0; i < this.zAxis.getTickValues().length; i += skip) {
            v = (float) this.zAxis.getTickValues()[i];
            if (v < dataExtent.minZ || v > dataExtent.maxZ) {
                continue;
            }

            xyz = SphericalTransform.transform(loc.X, loc.Y, v);
            //xyz = this.transform.transform(xyz);
            mvMatrix.transformPosition(xyz);
            xyz1 = new Vector3f(xyz.x, xyz.y, xyz.z);
            float xShift;
            if (left) {
                xyz1.x -= tickLen;
                xAlign = XAlign.RIGHT;
                xShift = -this.tickSpace;
            } else {
                xyz1.x += tickLen;
                xAlign = XAlign.LEFT;
                xShift = this.tickSpace;
            }
            if (i == tlabs.size()) {
                break;
            }

            //Draw tick line
            rgba = this.zAxis.getLineColor().getRGBComponents(null);
            gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
            gl.glLineWidth(this.zAxis.getLineWidth() * this.dpiScale);
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3f(xyz.x, xyz.y, xyz.z);
            gl.glVertex3f(xyz1.x, xyz1.y, xyz1.z);
            gl.glEnd();

            //Draw tick label
            rect = drawString(gl, tlabs.get(i), xyz1.x, xyz1.y, xyz1.z, xAlign, yAlign, xShift, 0);
            if (strWidth < rect.getWidth()) {
                strWidth = (float) rect.getWidth();
            }
        }

        //Draw z axis label
        ChartText label = this.zAxis.getLabel();
        if (label != null) {
            v = (float) (dataExtent.minZ + dataExtent.maxZ) / 2;
            xyz = SphericalTransform.transform(loc.X, loc.Y, v);
            //xyz = this.transform.transform(xyz);
            mvMatrix.transformPosition(xyz);
            if (left) {
                xyz.x -= tickLen;
                float yShift = strWidth + this.tickSpace * 3;
                drawString(gl, label, xyz.x, xyz.y, xyz.z, XAlign.CENTER, YAlign.BOTTOM, 90.f, 0, yShift);
            } else {
                xyz.x += tickLen;
                float yShift = -(strWidth + this.tickSpace * 3);
                drawString(gl, label, xyz.x, xyz.y, xyz.z, XAlign.CENTER, YAlign.TOP, 90.f, 0, yShift);
            }
        }

        gl.glPopMatrix();
        this.updateMatrix(gl);
    }

    @Override
    protected void drawPolygonShape(GL2 gl, Graphic graphic) {
        super.drawPolygonShape(gl, graphic);
    }

    // </editor-fold>
}
