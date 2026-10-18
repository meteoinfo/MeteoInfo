package org.meteoinfo.chart.jogl;

import org.joml.Vector3f;
import org.meteoinfo.chart.ChartText3D;
import org.meteoinfo.chart.graphic.*;
import org.meteoinfo.chart.jogl.tessellator.Primitive;
import org.meteoinfo.chart.jogl.tessellator.TessPolygon;
import org.meteoinfo.chart.graphic.Graphic;
import org.meteoinfo.chart.graphic.GraphicCollection;
import org.meteoinfo.geometry.legend.PolygonBreak;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.Polygon;
import org.meteoinfo.geometry.MultiPolygon;
import org.meteoinfo.geometry.Geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SphericalTransform {
    public static float radius = 6371.f;

    /**
     * Transform spherical coordinates to normal 3D coordinates
     * @param lon Longitude
     * @param lat Latitude
     * @param alt Altitude
     * @return Normal 3D coordinates - x,y,z
     */
    public static Vector3f transform(float lon, float lat, float alt) {
        double u = Math.toRadians(lon);
        double v = Math.toRadians(lat);
        float x = (float) (Math.cos(u) * Math.cos(v)) * (radius + alt);
        float y = (float) (Math.sin(u) * Math.cos(v)) * (radius + alt);
        float z = (float) Math.sin(v) * (radius + alt);

        return new Vector3f(x, y, z);
    }

    /**
     * Transform spherical coordinates to normal 3D coordinates
     * @param vertex Vertex
     * @return Transformed vertex
     */
    public static float[] transform(float[] vertex) {
        double u = Math.toRadians(vertex[0]);
        double v = Math.toRadians(vertex[1]);
        float x = (float) (Math.cos(u) * Math.cos(v)) * (radius + vertex[2]);
        float y = (float) (Math.sin(u) * Math.cos(v)) * (radius + vertex[2]);
        float z = (float) Math.sin(v) * (radius + vertex[2]);

        return new float[]{x, y, z};
    }

    /**
     * Transform spherical coordinates to normal 3D coordinates
     * @param p Input PointZ
     * @return Transformed PointZ
     */
    public static Coordinate transform(Coordinate p) {
        Vector3f xyz = transform((float)p.x, (float)p.y, (float)p.z);

        return new Coordinate(xyz.x, xyz.y, xyz.z);
    }

    /**
     * Transform spherical coordinates to normal 3D coordinates
     * @param p Input Vector3f
     * @return Transformed Vector3f
     */
    public static Vector3f transform(Vector3f p) {
        return transform(p.x, p.y, p.z);
    }

    /**
     * Transform spherical coordinates to normal 3D coordinates
     * @param p Input particle
     * @return Transformed particle
     */
    public static ParticleGraphics.Particle transform(ParticleGraphics.Particle p) {
        Vector3f xyz = transform((float)p.x, (float)p.y, (float)p.x);

        p.x = xyz.x;
        p.y = xyz.y;
        p.z = xyz.z;

        return p;
    }

    /**
     * Transform a graphic
     * @param graphic The graphic
     * @return Transformed graphic
     */
    public static Graphic transform(Graphic graphic) {
        if (graphic instanceof QuadMeshGraphic) {
            QuadMeshGraphic surfaceGraphic = (QuadMeshGraphic) graphic;
            float[] vertexPosition = surfaceGraphic.getVertexPosition();
            Vector3f vector3f;
            for (int i = 0; i < vertexPosition.length; i+=3) {
                vector3f = transform(vertexPosition[i], vertexPosition[i+1], vertexPosition[i+2]);
                vertexPosition[i] = vector3f.x;
                vertexPosition[i+1] = vector3f.y;
                vertexPosition[i+2] = vector3f.z;
            }
            surfaceGraphic.setVertexPosition(vertexPosition);
            surfaceGraphic.calculateNormalVectors(vertexPosition);
            surfaceGraphic.updateVertexTexture();
            return surfaceGraphic;
        } else if (graphic instanceof IsosurfaceGraphics) {
            IsosurfaceGraphics isosurfaceGraphics = (IsosurfaceGraphics) graphic;
            List<Coordinate[]> triangles = new ArrayList<>();
            for (Coordinate[] triangle : isosurfaceGraphics.getTriangles()) {
                Coordinate[] t = new Coordinate[3];
                for (int i = 0; i < 3; i++) {
                    t[i] = transform(triangle[i]);
                }
                triangles.add(t);
            }
            isosurfaceGraphics.setTriangles(triangles);
            return isosurfaceGraphics;
        } else if (graphic instanceof TriMeshGraphic) {
            TriMeshGraphic meshGraphic = (TriMeshGraphic) graphic;
            float[] vertexData = meshGraphic.getVertexPosition();
            Vector3f vector3f;
            for (int i = 0; i < vertexData.length; i+=3) {
                vector3f = transform(vertexData[i], vertexData[i+1], vertexData[i+2]);
                vertexData[i] = vector3f.x;
                vertexData[i+1] = vector3f.y;
                vertexData[i+2] = vector3f.z;
            }
            meshGraphic.setVertexPosition(vertexData);
            meshGraphic.calculateNormalVectors(vertexData);
            return meshGraphic;
        } else if (graphic instanceof ParticleGraphics) {
            ParticleGraphics particleGraphics = (ParticleGraphics) graphic;
            for (Map.Entry<Integer, List> map : particleGraphics.getParticleList()) {
                for (ParticleGraphics.Particle p : (List<ParticleGraphics.Particle>)map.getValue()) {
                    transform(p);
                }
            }
            return particleGraphics;
        } else if (graphic instanceof VolumeGraphic) {
            return graphic;
        } else {
            if (graphic instanceof GraphicCollection) {
                GraphicCollection graphics = (GraphicCollection) graphic;
                for (int i = 0; i < graphics.getNumGraphics(); i++) {
                    Graphic gg = graphics.getGraphicN(i);
                    Geometry shape = gg.getGraphicN(0).getGeometry();
                    boolean isTess = false;
                    if (shape instanceof MultiPolygon) {
                        PolygonBreak pb = (PolygonBreak) gg.getGraphicN(0).getLegendBreak();
                        isTess = pb.isDrawFill();
                    }
                    if (isTess) {
                        MultiPolygon polygonZShape = (MultiPolygon) shape;
                        List<Polygon> polygonZS = polygonZShape.getPolygons();
                        for (int j = 0; j < polygonZS.size(); j++) {
                            Polygon polygonZ = polygonZS.get(j);
                            TessPolygon tessPolygon = new TessPolygon(polygonZ);
                            tessPolygon.subdivideEquilateral(3.0f);
                            for (Triangle triangle : tessPolygon.getTriangles()) {
                                List<Vector3f> points = triangle.getPoints();
                                points.replaceAll(SphericalTransform::transform);
                                triangle.setPoints(points);
                            }
                            List<Coordinate> outLine = tessPolygon.getOutLine();
                            outLine.replaceAll(SphericalTransform::transform);
                            for (int k = 0; k < tessPolygon.getNumInteriorRing(); k++) {
                                List<Coordinate> holeLine = tessPolygon.getHoleLine(k);
                                holeLine.replaceAll(SphericalTransform::transform);
                            }
                            polygonZS.set(j, tessPolygon);
                        }
                        polygonZShape.setPolygons(polygonZS);
                    } else {
                        List<Coordinate> points = shape.getCoordinates();
                        points.replaceAll(SphericalTransform::transform);
                        if (shape instanceof MultiPolygon)
                            ((MultiPolygon) shape).setPoints_keep(points);
                        else
                            shape.setCoordinates(points);
                    }
                    gg.setGeometry(shape);
                    graphics.setGraphicN(i, gg);
                }
                graphics.updateExtent();
                return graphics;
            } else {
                Graphic gg = graphic.getGraphicN(0);
                Geometry shape = gg.getGeometry();
                if (shape instanceof ChartText3D) {
                    Coordinate p = ((ChartText3D) shape).getPoint();
                    ((ChartText3D) shape).setPoint(SphericalTransform.transform(p));
                } else {
                    List<Coordinate> points = (List<Coordinate>) shape.getCoordinates();
                    points.replaceAll(SphericalTransform::transform);
                    if (shape instanceof MultiPolygon)
                        ((MultiPolygon) shape).setPoints_keep(points);
                    else
                        shape.setCoordinates(points);
                }

                gg.setGeometry(shape);
                return gg;
            }
        }
    }
}
