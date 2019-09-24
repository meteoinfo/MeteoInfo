/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.jogl;

import com.jogamp.opengl.GL2;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.chart.jogl.mc.MarchingCubes;
import org.meteoinfo.chart.plot3d.GraphicCollection3D;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.Extent3D;
import org.meteoinfo.layer.ImageLayer;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.GraphicCollection;
import org.meteoinfo.shape.ImageShape;
import org.meteoinfo.shape.PointZ;
import org.meteoinfo.shape.PolygonZShape;

/**
 *
 * @author yaqiang
 */
public class JOGLUtil {
    /**
     * Get RGBA components from a legend break
     * @param cb Legend break
     * @return RGBA float array
     */
    public static float[] getRGBA(ColorBreak cb) {
        return cb.getColor().getRGBComponents(null);
    }
    
    /**
     * Create Texture
     *
     * @param gl GL2
     * @param layer Image layer
     * @param offset Offset of z axis
     * @param xshift X shift - to shift the grahpics in x direction, normally
     * for map in 180 - 360 degree east
     * @param interpolation Interpolation
     * @return Graphics
     * @throws java.io.IOException
     */
    public static GraphicCollection createTexture(GL2 gl, ImageLayer layer, double offset, double xshift,
            String interpolation) throws IOException {
        GraphicCollection3D graphics = new GraphicCollection3D();
        graphics.setFixZ(true);
        graphics.setZDir("z");
        graphics.setZValue(offset);
        TextureShape ishape = new TextureShape();
        ishape.setFileName(layer.getFileName());
        Extent extent = layer.getExtent();
        Extent3D ex3 = new Extent3D(extent.minX + xshift, extent.maxX + xshift, extent.minY, extent.maxY, offset, offset);
        List<PointZ> coords = new ArrayList<>();
        coords.add(new PointZ(extent.minX + xshift, extent.minY, offset));
        coords.add(new PointZ(extent.maxX + xshift, extent.minY, offset));
        coords.add(new PointZ(extent.maxX + xshift, extent.maxY, offset));
        coords.add(new PointZ(extent.minX + xshift, extent.maxY, offset));
        ishape.setExtent(ex3);
        ishape.setCoords(coords);
        Graphic gg = new Graphic(ishape, new ColorBreak());
        if (interpolation != null) {
            ((ImageShape) gg.getShape()).setInterpolation(interpolation);
        }
        graphics.add(gg);

        return graphics;
    }
    
    /**
     * Create surface graphics
     *
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param za Z coordinate array
     * @param ls Legend scheme
     * @return Surface graphics
     */
    public static SurfaceGraphics surface(Array xa, Array ya, Array za, LegendScheme ls) {
        SurfaceGraphics graphics = new SurfaceGraphics();
        int[] shape = xa.getShape();
        int colNum = shape[1];
        int rowNum = shape[0];
        int idx;
        PointZ[][] vertices = new PointZ[rowNum][colNum];
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                idx = i * colNum + j;
                vertices[i][j] = new PointZ(xa.getDouble(idx), ya.getDouble(idx), za.getDouble(idx), za.getDouble(idx));
            }
        }
        graphics.setVertices(vertices);
        graphics.setLegendScheme(ls);
        return graphics;
    }
    
    /**
     * Create isosurface graphics
     * @param data 3d data array
     * @param x X coordinates
     * @param y Y coordinates
     * @param z Z coordinates
     * @param isoLevel iso level
     * @param pb Polygon break
     * @return Graphics
     */
    public static GraphicCollection isosurface(Array data, Array x, Array y, Array z,
            float isoLevel, PolygonBreak pb) {
        List<float[]> vertices = MarchingCubes.marchingCubes(data, x, y, z, isoLevel);
        IsosurfaceGraphics graphics = new IsosurfaceGraphics();
        graphics.setLegendBreak(pb);
        float[] v1, v2, v3;
        for (int i = 0; i < vertices.size(); i += 3) {            
            PointZ[] points = new PointZ[3];
            v1 = vertices.get(i);
            v2 = vertices.get(i + 1);
            v3 = vertices.get(i + 2);
            points[0] = new PointZ(v1[0], v1[1], v1[2]);
            points[1] = new PointZ(v2[0], v2[1], v2[2]);
            points[2] = new PointZ(v3[0], v3[1], v3[2]);
            graphics.addTriangle(points);
        }
        
        return graphics;
    }
    
    /**
     * Create isosurface graphics
     * @param data 3d data array
     * @param x X coordinates
     * @param y Y coordinates
     * @param z Z coordinates
     * @param isoLevel iso level
     * @param pb Polygon break
     * @return Graphics
     */
    public static GraphicCollection isosurface_bak(Array data, Array x, Array y, Array z,
            float isoLevel, PolygonBreak pb) {
        List<float[]> vertices = MarchingCubes.marchingCubes(data, x, y, z, isoLevel);
        GraphicCollection3D graphics = new GraphicCollection3D();
        pb.setColor(Color.cyan);
        float[] v1, v2, v3;
        for (int i = 0; i < vertices.size(); i += 3) {
            PolygonZShape ps = new PolygonZShape();
            List<PointZ> points = new ArrayList<>();
            v1 = vertices.get(i);
            v2 = vertices.get(i + 1);
            v3 = vertices.get(i + 2);
            points.add(new PointZ(v1[0], v1[1], v1[2]));
            points.add(new PointZ(v2[0], v2[1], v2[2]));
            points.add(new PointZ(v3[0], v3[1], v3[2]));
            points.add(new PointZ(v1[0], v1[1], v1[2]));
            ps.setPoints(points);
            Graphic graphic = new Graphic(ps, pb);
            graphics.add(graphic);
        }
        graphics.setAllTriangle(true);
        
        return graphics;
    }
}
