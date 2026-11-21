package org.meteoinfo.chart.render.jogl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.meteoinfo.chart.graphic.GraphicCollection;
import org.meteoinfo.chart.graphic.LinealGraphic;
import org.meteoinfo.chart.graphic.PointGraphic;
import org.meteoinfo.chart.graphic.PointGraphicCollection;
import org.meteoinfo.chart.graphic.cylinder.Cylinder;
import org.meteoinfo.chart.jogl.Program;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.jogl.Utils;
import org.meteoinfo.chart.graphic.Graphic;
import org.meteoinfo.geometry.geoprocess.GeoComputation;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.Extent;
import org.meteoinfo.geometry.Geometry;
import org.meteoinfo.geometry.LineString;
import org.meteoinfo.geometry.MultiLineString;
import org.meteoinfo.geometry.Point;
import org.meteoinfo.math.Matrix4f;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LineRender extends JOGLGraphicRender {

    private LinealGraphic linealGraphic;
    private IntBuffer vbo;
    private Program program;
    private int vertexNum;
    private int sizePosition;
    private int sizeColor;
    private int sizeNormal;
    private float[] vertexPosition;
    private float[] vertexColor;
    private float lineWidth = 1.0f;
    private LineStyles lineStyle = LineStyles.SOLID;
    private List<PolylineBreak> lineBreaks = new ArrayList<>();
    private List<Integer> linePointNumbers;
    private PointGraphicCollection symbolGraphics;
    private PointRender symbolRender;
    private boolean symbolsDirty = true;
    private Extent clipExtent;
    private boolean streamline = false;
    private IntBuffer vboCone;
    private float[] coneVertexPosition;
    private float[] coneVertexNormal;
    private float[] coneVertexColor;
    private int[] coneVertexIndices;
    private int sizeConePosition;
    private int sizeConeNormal;
    private int sizeConeColor;

    /**
     * Constructor
     *
     * @param gl The JOGL GL2 object
     */
    public LineRender(GL2 gl) {
        super(gl);

        if (useShader) {
            try {
                this.compileShaders();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        initVertexBuffer();
    }

    /**
     * Constructor
     *
     * @param gl The JOGL GL2 object
     * @param graphics Linestring 3D graphics
     */
    public LineRender(GL2 gl, LinealGraphic linealGraphic) {
        this(gl);

        this.linealGraphic = linealGraphic;
        ColorBreak cb = linealGraphic.getLegendBreak();
        PolylineBreak lineBreak;
        if (cb.getBreakType() == BreakTypes.COLOR_BREAK_COLLECTION) {
            lineBreak = (PolylineBreak) ((ColorBreakCollection) cb).get(0);
        } else {
            lineBreak = (PolylineBreak) cb;
        }
        this.streamline = lineBreak instanceof StreamlineBreak;
        this.lineWidth = lineBreak.getWidth();
        this.lineStyle = lineBreak.getStyle();

        this.vertexNum = ((Graphic) this.linealGraphic).getNumPoints();
        this.vertexPosition = this.linealGraphic.getVertexPosition();
        this.vertexColor = this.linealGraphic.getVertexColor();
        this.linePointNumbers = this.linealGraphic.getLinePointNumbers();
        this.lineBreaks = getLineBreaks();
    }

    /**
     * Get the polyline break of a color break.
     *
     * @param cb Color break
     * @return Polyline break, or null if the break has no line style
     */
    private static PolylineBreak getPolylineBreak(ColorBreak cb) {
        if (cb instanceof PolylineBreak) {
            return (PolylineBreak) cb;
        }
        if (cb instanceof ColorBreakCollection) {
            ColorBreakCollection cbc = (ColorBreakCollection) cb;
            if (cbc.size() > 0) {
                return getPolylineBreak(cbc.get(0));
            }
        }
        return null;
    }

    /**
     * Build the polyline break list with one entry for each line strip. The
     * order has to match the line point number list.
     *
     * @return Polyline break list
     */
    private List<PolylineBreak> getLineBreaks() {
        List<PolylineBreak> breaks = new ArrayList<>();
        Graphic graphic = (Graphic) this.linealGraphic;
        PolylineBreak defaultBreak = getPolylineBreak(this.linealGraphic.getLegendBreak());
        for (int i = 0; i < graphic.getNumGraphics(); i++) {
            Graphic child = graphic.getGraphicN(i);
            PolylineBreak pb = getPolylineBreak(child.getLegendBreak());
            if (pb == null) {
                pb = defaultBreak;
            }
            Geometry geometry = child.getGeometry();
            int lineNum = 1;
            if (geometry instanceof MultiLineString) {
                lineNum = ((MultiLineString) geometry).getPolylines().size();
            }
            for (int j = 0; j < lineNum; j++) {
                breaks.add(pb);
            }
        }
        return breaks;
    }

    /**
     * Get the polyline break for a line strip.
     *
     * @param index Line strip index
     * @return Polyline break
     */
    private PolylineBreak getLineBreak(int index) {
        if (index >= 0 && index < this.lineBreaks.size()) {
            return this.lineBreaks.get(index);
        }
        return null;
    }

    @Override
    public void setDpiScale(float value) {
        if (Math.abs(this.dpiScale - value) > 1.0e-4f) {
            this.symbolsDirty = true;
        }
        super.setDpiScale(value);
    }

    /**
     * Set the extent used to clip the line symbols.
     *
     * @param value Clip extent, usually the axes extent of the plot
     */
    public void setClipExtent(Extent value) {
        if (value == null) {
            this.clipExtent = null;
        } else if (this.clipExtent == null
                || this.clipExtent.minX != value.minX || this.clipExtent.maxX != value.maxX
                || this.clipExtent.minY != value.minY || this.clipExtent.maxY != value.maxY
                || this.clipExtent.minZ != value.minZ || this.clipExtent.maxZ != value.maxZ) {
            this.clipExtent = (Extent) value.clone();
        }
    }

    /**
     * Build the point symbols drawn on the line vertices. The symbols use the
     * same {@link PointStyle} rendering as {@link PointRender}, so line symbols
     * and point graphics have a consistent appearance.
     */
    private void buildSymbolGraphics() {
        PointGraphicCollection symbols = new PointGraphicCollection();
        if (this.vertexPosition == null || this.linePointNumbers == null) {
            this.symbolGraphics = symbols;
            this.symbolRender = null;
            return;
        }

        int vertexCount = this.vertexPosition.length / 3;
        int vertexIndex = 0;
        for (int i = 0; i < this.linePointNumbers.size(); i++) {
            int pointNum = this.linePointNumbers.get(i);
            PolylineBreak pb = getLineBreak(i);
            if (pb != null && pb.isDrawSymbol()) {
                int interval = Math.max(1, pb.getSymbolInterval());
                Color fillColor = pb.getSymbolFillColor();
                if (fillColor == null) {
                    fillColor = pb.getColor();
                }
                Color outlineColor = pb.getSymbolColor();
                if (outlineColor == null) {
                    outlineColor = fillColor;
                }
                PointBreak spb = new PointBreak();
                spb.setMarkerType(MarkerType.SIMPLE);
                spb.setStyle(pb.getSymbolStyle());
                spb.setSize(pb.getSymbolSize());
                spb.setColor(fillColor);
                spb.setOutlineColor(outlineColor);
                spb.setDrawFill(pb.isFillSymbol());
                spb.setDrawOutline(pb.isDrawSymbolOutline());
                for (int j = 0; j < pointNum; j += interval) {
                    int idx = vertexIndex + j;
                    if (idx >= vertexCount) {
                        break;
                    }
                    Point point = new Point(new Coordinate(
                            this.vertexPosition[idx * 3],
                            this.vertexPosition[idx * 3 + 1],
                            this.vertexPosition[idx * 3 + 2]));
                    symbols.add(new PointGraphic(point, spb));
                }
            }
            vertexIndex += pointNum;
        }

        this.symbolGraphics = symbols;
        this.symbolRender = null;
    }

    /**
     * Draw the point symbols of the lines.
     */
    private void drawSymbols() {
        if (this.symbolGraphics == null || this.symbolGraphics.getNumGraphics() == 0) {
            return;
        }
        if (this.symbolRender == null) {
            this.symbolRender = new PointRender(gl, this.symbolGraphics);
        }
        this.symbolRender.setDpiScale(this.dpiScale);
        if (this.transform != null) {
            this.symbolRender.setTransform(this.transform, false);
        }
        this.symbolRender.setOrthographic(this.orthographic);
        this.symbolRender.setLighting(this.lighting);
        this.symbolRender.setClipExtent(this.clipExtent);
        this.symbolRender.updateMatrix();
        this.symbolRender.setRotateModelView(this.modelViewMatrixR);
        this.symbolRender.draw();
    }

    void compileShaders() throws Exception {
        String vertexShaderCode = Utils.loadResource("/shaders/surface/vertex.vert");
        String fragmentShaderCode = Utils.loadResource("/shaders/surface/surface.frag");
        program = new Program("surface", vertexShaderCode, fragmentShaderCode);
    }

    private void initVertexBuffer() {
        vbo = GLBuffers.newDirectIntBuffer(1);
    }

    private static List<LineString> getLines(Geometry geometry) {
        if (geometry instanceof MultiLineString) {
            return ((MultiLineString) geometry).getPolylines();
        } else if (geometry instanceof LineString) {
            return Collections.singletonList((LineString) geometry);
        }
        return Collections.emptyList();
    }

    private void updateConeVertex() {
        List<Vector3f> vertexPositionList = new ArrayList<>();
        List<Vector3f> vertexNormalList = new ArrayList<>();
        List<Vector4f> vertexColorList = new ArrayList<>();
        List<Integer> vertexIndices = new ArrayList<>();
        Cylinder cylinder = null;
        Graphic lineGraphic = (Graphic) this.linealGraphic;
        for (int k = 0; k < lineGraphic.getNumGraphics(); k++) {
            Graphic graphic = lineGraphic.getGraphicN(k);
            Geometry geometry = graphic.getGeometry();
            int pointNum = geometry.getNumPoints();
            List<Coordinate> ps = geometry.getCoordinates();
            ColorBreak cb = graphic.getLegendBreak();
            if (cb.getBreakType() == BreakTypes.COLOR_BREAK_COLLECTION) {
                ColorBreakCollection cbc = (ColorBreakCollection) cb;
                StreamlineBreak slb = (StreamlineBreak) cbc.get(0);
                int interval = slb.getInterval();
                Vector3f v1, v2;
                for (int i = 1; i < pointNum; i++) {
                    if (i % interval == 0) {
                        Coordinate p2 = ps.get(i);
                        Coordinate p1 = ps.get(i - 1);
                        v1 = transform.transform((float) p1.x, (float) p1.y, (float) p1.z);
                        v2 = transform.transform((float) p2.x, (float) p2.y, (float) p2.z);
                        //v1 = new Vector3f((float) p1.X, (float) p1.Y, (float) p1.Z);
                        //v2 = new Vector3f((float) p2.X, (float) p2.Y, (float) p2.Z);
                        slb = (StreamlineBreak) cbc.get(i);
                        if (cylinder == null) {
                            cylinder = new Cylinder(slb.getArrowHeadWidth() * 0.02f,
                                    0, slb.getArrowHeadLength() * 0.02f, 8, 1, true);
                        }
                        Matrix4f matrix = new Matrix4f();
                        matrix.lookAt(v2.sub(v1, new Vector3f()));
                        matrix.translate(v2);
                        List<Vector3f> vertices = cylinder.getVertices();
                        int n = vertices.size();
                        int nAdded = vertexPositionList.size();
                        for (Vector3f v : vertices) {
                            vertexPositionList.add(matrix.mul(v));
                        }
                        List<Vector3f> normals = cylinder.getNormals();
                        vertexNormalList.addAll(normals);
                        float[] color = slb.getColor().getRGBComponents(null);
                        for (int j = 0; j < n; j++) {
                            vertexColorList.add(new Vector4f(color));
                        }
                        if (nAdded == 0) {
                            vertexIndices.addAll(cylinder.getIndices());
                        } else {
                            for (int idx : cylinder.getIndices()) {
                                vertexIndices.add(idx + nAdded);
                            }
                        }
                    }
                }
            } else {
                StreamlineBreak slb = (StreamlineBreak) cb;
                int interval = slb.getInterval();
                Vector3f v1, v2;
                for (int i = 1; i < pointNum; i++) {
                    if (i % interval == 0) {
                        Coordinate p2 = ps.get(i);
                        Coordinate p1 = ps.get(i - 1);
                        v1 = transform.transform((float) p1.x, (float) p1.y, (float) p1.z);
                        v2 = transform.transform((float) p2.x, (float) p2.y, (float) p2.z);
                        //v1 = new Vector3f((float) p1.X, (float) p1.Y, (float) p1.Z);
                        //v2 = new Vector3f((float) p2.X, (float) p2.Y, (float) p2.Z);
                        if (cylinder == null) {
                            cylinder = new Cylinder(slb.getArrowHeadWidth() * 0.02f,
                                    0, slb.getArrowHeadLength() * 0.02f, 8, 1, true);
                        }
                        Matrix4f matrix = new Matrix4f();
                        matrix.lookAt(v2.sub(v1, new Vector3f()));
                        matrix.translate(v2);
                        List<Vector3f> vertices = cylinder.getVertices();
                        int n = vertices.size();
                        int nAdded = vertexPositionList.size();
                        for (Vector3f v : vertices) {
                            vertexPositionList.add(matrix.mul(v));
                        }
                        List<Vector3f> normals = cylinder.getNormals();
                        vertexNormalList.addAll(normals);
                        /*for (Vector3f v : normals) {
                            vertexNormalList.add(matrix.mul(v));
                        }*/
                        float[] color = slb.getColor().getRGBComponents(null);
                        for (int j = 0; j < n; j++) {
                            vertexColorList.add(new Vector4f(color));
                        }
                        if (nAdded == 0) {
                            vertexIndices.addAll(cylinder.getIndices());
                        } else {
                            for (int idx : cylinder.getIndices()) {
                                vertexIndices.add(idx + nAdded);
                            }
                        }
                    }
                }
            }
        }

        int n = vertexPositionList.size();
        this.coneVertexPosition = new float[n * 3];
        this.coneVertexNormal = new float[n * 3];
        this.coneVertexColor = new float[n * 4];
        Vector3f v;
        Vector4f v4;
        for (int i = 0, j = 0, k = 0; i < n; i++, j+=3, k+=4) {
            v = vertexPositionList.get(i);
            coneVertexPosition[j] = v.x;
            coneVertexPosition[j + 1] = v.y;
            coneVertexPosition[j + 2] = v.z;
            v = vertexNormalList.get(i);
            coneVertexNormal[j] = v.x;
            coneVertexNormal[j + 1] = v.y;
            coneVertexNormal[j + 2] = v.z;
            v4 = vertexColorList.get(i);
            coneVertexColor[k] = v4.x;
            coneVertexColor[k + 1] = v4.y;
            coneVertexColor[k + 2] = v4.z;
            coneVertexColor[k + 3] = v4.w;
        }
        coneVertexIndices = vertexIndices.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public void setTransform(Transform transform, boolean alwaysUpdateBuffers) {
        boolean updateBuffer = true;
        if (!alwaysUpdateBuffers && this.transform != null && this.transform.equals(transform))
            updateBuffer = false;

        super.setTransform((Transform) transform.clone());

        if (updateBuffer) {
            if (this.vertexPosition == null || this.linePointNumbers == null) {
                this.linealGraphic.updateVertex();
                this.vertexPosition = this.linealGraphic.getVertexPosition();
                this.vertexColor = this.linealGraphic.getVertexColor();
                this.linePointNumbers = this.linealGraphic.getLinePointNumbers();
            }
            this.lineBreaks = getLineBreaks();
            this.symbolsDirty = true;
            FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexPosition);
            sizePosition = vertexBuffer.capacity() * Float.BYTES;

            FloatBuffer colorBuffer = GLBuffers.newDirectFloatBuffer(vertexColor);
            sizeColor = colorBuffer.capacity() * Float.BYTES;
            int totalSize = sizePosition + sizeColor;

            gl.glGenBuffers(1, vbo);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));
            gl.glBufferData(GL.GL_ARRAY_BUFFER, totalSize, null, GL.GL_STATIC_DRAW);
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, sizePosition, vertexBuffer);
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER, sizePosition, sizeColor, colorBuffer);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

            if (this.streamline) {
                this.updateConeVertex();

                FloatBuffer coneVertexBuffer = GLBuffers.newDirectFloatBuffer(coneVertexPosition);
                sizeConePosition = coneVertexBuffer.capacity() * Float.BYTES;
                FloatBuffer coneNormalBuffer = GLBuffers.newDirectFloatBuffer(coneVertexNormal);
                sizeConeNormal = coneNormalBuffer.capacity() * Float.BYTES;
                FloatBuffer coneColorBuffer = GLBuffers.newDirectFloatBuffer(coneVertexColor);
                sizeConeColor = coneColorBuffer.capacity() * Float.BYTES;

                vboCone = GLBuffers.newDirectIntBuffer(2);
                gl.glGenBuffers(2, vboCone);
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboCone.get(0));
                gl.glBufferData(GL.GL_ARRAY_BUFFER, sizeConePosition + sizeConeNormal + sizeConeColor,
                        null, GL.GL_STATIC_DRAW);
                gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, sizeConePosition, coneVertexBuffer);
                gl.glBufferSubData(GL.GL_ARRAY_BUFFER, sizeConePosition, sizeConeNormal, coneNormalBuffer);
                gl.glBufferSubData(GL.GL_ARRAY_BUFFER, sizeConePosition + sizeConeNormal, sizeConeColor,
                        coneColorBuffer);
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

                IntBuffer coneIndexBuffer = GLBuffers.newDirectIntBuffer(coneVertexIndices);
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vboCone.get(1));
                gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, (long) coneIndexBuffer.capacity() * Integer.BYTES,
                        coneIndexBuffer, GL.GL_STATIC_DRAW);
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
            }
        }
    }

    void setUniforms() {

    }

    /**
     * Apply the OpenGL line stipple pattern of a line style. The patterns use
     * window pixels, so dashes keep a constant screen size at any camera
     * distance and orientation.
     *
     * @param style Line style
     */
    private void applyLineStipple(LineStyles style) {
        if (style == null) {
            gl.glDisable(GL2.GL_LINE_STIPPLE);
            return;
        }
        int factor = Math.max(1, Math.round(this.dpiScale));
        switch (style) {
            case DASH:
                // 4 pixels on, 4 pixels off
                gl.glLineStipple(factor, (short) 0x0F0F);
                gl.glEnable(GL2.GL_LINE_STIPPLE);
                break;
            case DOT:
                // 2 pixels on, 2 pixels off
                gl.glLineStipple(factor, (short) 0x3333);
                gl.glEnable(GL2.GL_LINE_STIPPLE);
                break;
            case DASH_DOT:
                // 7 pixels on, 4 off, 1 on, 4 off
                gl.glLineStipple(factor, (short) 0x087F);
                gl.glEnable(GL2.GL_LINE_STIPPLE);
                break;
            case DASH_DOT_DOT:
                // 10 pixels on, 6 off, 2 on, 6 off, 2 on, 6 off
                gl.glLineStipple(2 * factor, (short) 0x111F);
                gl.glEnable(GL2.GL_LINE_STIPPLE);
                break;
            default:
                gl.glDisable(GL2.GL_LINE_STIPPLE);
                break;
        }
    }

    @Override
    public void draw() {
        if (useShader) {    //  not working now
            program.use(gl);
            setUniforms();

            gl.glUseProgram(0);
        } else {
            if (this.linePointNumbers == null) {
                return;
            }
            if (this.symbolsDirty || this.symbolGraphics == null) {
                buildSymbolGraphics();
                this.symbolsDirty = false;
            }

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));

            // enable vertex arrays
            gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
            gl.glColorPointer(4, GL.GL_FLOAT, 0, sizePosition);

            boolean lightEnabled = this.lighting.isEnable();
            if (lightEnabled) {
                this.lighting.stop(gl);
            }
            int sIdx = 0;
            for (int i = 0; i < linePointNumbers.size(); i++) {
                int np = linePointNumbers.get(i);
                PolylineBreak pb = getLineBreak(i);
                if (pb == null || pb.isDrawPolyline()) {
                    float width = pb == null ? this.lineWidth : pb.getWidth();
                    LineStyles style = pb == null ? this.lineStyle : pb.getStyle();
                    gl.glLineWidth(Math.max(1.0f, width * this.dpiScale));
                    applyLineStipple(style);
                    gl.glDrawArrays(GL.GL_LINE_STRIP, sIdx, np);
                }
                sIdx += np;
            }
            gl.glDisable(GL2.GL_LINE_STIPPLE);
            if (lightEnabled) {
                this.lighting.start(gl);
            }

            gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

            drawSymbols();

            if (this.streamline) {
                gl.glPushMatrix();
                FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
                gl.glLoadMatrixf(this.modelViewMatrixR.get(fb));

                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboCone.get(0));
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vboCone.get(1));

                // enable vertex arrays
                gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
                gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
                gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
                gl.glNormalPointer(GL.GL_FLOAT, 0, sizeConePosition);
                gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
                gl.glColorPointer(4, GL.GL_FLOAT, 0, sizeConePosition + sizeConeNormal);

                gl.glDrawElements(GL2.GL_TRIANGLES, coneVertexIndices.length, GL.GL_UNSIGNED_INT, 0);

                gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
                gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
                gl.glDisableClientState(GL2.GL_COLOR_ARRAY);

                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);

                gl.glPopMatrix();
            }
        }
    }
}
