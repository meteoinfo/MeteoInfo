package org.meteoinfo.chart.jogl;

import org.apache.commons.imaging.ImageReadException;
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
        this.angleY = 160;
        //earthSurface(50);
        //addGraphic(this.surface);
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
        this.setExtent((Extent3D) ex);
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
    // </editor-fold>
}
