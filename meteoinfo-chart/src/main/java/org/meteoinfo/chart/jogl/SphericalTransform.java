package org.meteoinfo.chart.jogl;

import org.meteoinfo.chart.graphic.*;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.shape.PointZ;
import org.meteoinfo.geometry.shape.Shape;

import java.util.ArrayList;
import java.util.List;

public class SphericalTransform {
    public static float radius = 6371.f;

    /**
     * Transform spherical coordinates to normal 3D coordinates
     * @param lon Longitude
     * @param lat Latitude
     * @param alt Altitude
     * @return Normal 3D coordinates - x,y,z
     */
    public static float[] transform(float lon, float lat, float alt) {
        double u = Math.toRadians(lon);
        double v = Math.toRadians(lat);
        float x = (float) (Math.cos(u) * Math.cos(v)) * radius;
        float y = (float) (Math.sin(u) * Math.cos(v)) * radius;
        float z = (float) Math.sin(v) * radius + alt;

        return new float[]{x, y, z};
    }

    /**
     * Transform spherical coordinates to normal 3D coordinates
     * @param p Input PointZ
     * @return Transformed PointZ
     */
    public static PointZ transform(PointZ p) {
        float[] xyz = transform((float)p.X, (float)p.Y, (float)p.Z);

        return new PointZ(xyz[0], xyz[1], xyz[2]);
    }

    /**
     * Transform a graphic
     * @param graphic The graphic
     * @return Transformed graphic
     */
    public static Graphic transform(Graphic graphic) {
        if (graphic instanceof SurfaceGraphics) {
            SurfaceGraphics surfaceGraphics = (SurfaceGraphics) graphic;
            PointZ[][] vertices = surfaceGraphics.getVertices();
            int dim1 = surfaceGraphics.getDim1();
            int dim2 = surfaceGraphics.getDim2();
            for (int i = 0; i < dim1; i++) {
                for (int j = 0; j < dim2; j++) {
                    vertices[i][j] = transform(vertices[i][j]);
                }
            }
            surfaceGraphics.setVertices(vertices);
            return surfaceGraphics;
        } else if (graphic instanceof IsosurfaceGraphics) {
            return graphic;
        } else if (graphic instanceof ParticleGraphics) {
            return graphic;
        } else if (graphic instanceof VolumeGraphics) {
            return graphic;
        } else {
            GraphicCollection3D graphics = (GraphicCollection3D) graphic;
            for (int i = 0; i < graphics.getNumGraphics(); i++) {
                Graphic gg = graphics.getGraphicN(i);
                Shape shape = gg.getGraphicN(0).getShape();
                List<PointZ> points = (List<PointZ>) shape.getPoints();
                for (int j = 0; j < points.size(); j++) {
                    points.set(j, transform(points.get(j)));
                }
                shape.setPoints(points);
                gg.setShape(shape);
                graphics.setGraphicN(i, gg);
            }
            graphics.updateExtent();
            return graphics;
        }
    }
}
