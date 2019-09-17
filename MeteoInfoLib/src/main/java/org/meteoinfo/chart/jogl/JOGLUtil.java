/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.jogl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.chart.plot3d.GraphicCollection3D;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.Extent3D;
import org.meteoinfo.layer.ImageLayer;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.GraphicCollection;
import org.meteoinfo.shape.ImageShape;
import org.meteoinfo.shape.PointZ;

/**
 *
 * @author yaqiang
 */
public class JOGLUtil {
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
}
