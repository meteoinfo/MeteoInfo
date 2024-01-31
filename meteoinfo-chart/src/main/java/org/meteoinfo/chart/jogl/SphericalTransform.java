package org.meteoinfo.chart.jogl;

import org.joml.Vector3f;
import org.meteoinfo.chart.ChartText3D;
import org.meteoinfo.chart.graphic.*;
import org.meteoinfo.chart.jogl.tessellator.Primitive;
import org.meteoinfo.chart.jogl.tessellator.TessPolygon;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.graphic.GraphicCollection3D;
import org.meteoinfo.geometry.legend.PolygonBreak;
import org.meteoinfo.geometry.shape.PointZ;
import org.meteoinfo.geometry.shape.PolygonZ;
import org.meteoinfo.geometry.shape.PolygonZShape;
import org.meteoinfo.geometry.shape.Shape;

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
    public static PointZ transform(PointZ p) {
        Vector3f xyz = transform((float)p.X, (float)p.Y, (float)p.Z);

        return new PointZ(xyz.x, xyz.y, xyz.z);
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
        if (graphic instanceof MeshGraphic) {
            MeshGraphic surfaceGraphic = (MeshGraphic) graphic;
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
            List<PointZ[]> triangles = new ArrayList<>();
            for (PointZ[] triangle : isosurfaceGraphics.getTriangles()) {
                PointZ[] t = new PointZ[3];
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
            if (graphic instanceof GraphicCollection3D) {
                GraphicCollection3D graphics = (GraphicCollection3D) graphic;
                for (int i = 0; i < graphics.getNumGraphics(); i++) {
                    Graphic gg = graphics.getGraphicN(i);
                    Shape shape = gg.getGraphicN(0).getShape();
                    boolean isTess = false;
                    if (shape instanceof PolygonZShape) {
                        PolygonBreak pb = (PolygonBreak) gg.getGraphicN(0).getLegend();
                        isTess = pb.isDrawFill();
                    }
                    if (isTess) {
                        PolygonZShape polygonZShape = (PolygonZShape) shape;
                        List<PolygonZ> polygonZS = (List<PolygonZ>) polygonZShape.getPolygons();
                        for (int j = 0; j < polygonZS.size(); j++) {
                            PolygonZ polygonZ = polygonZS.get(j);
                            TessPolygon tessPolygon = new TessPolygon(polygonZ);
                            for (Primitive primitive : tessPolygon.getPrimitives()) {
                                primitive.vertices.replaceAll(SphericalTransform::transform);
                            }
                            List<PointZ> outLine = (List<PointZ>) tessPolygon.getOutLine();
                            outLine.replaceAll(SphericalTransform::transform);
                            for (int k = 0; k < tessPolygon.getHoleLineNumber(); k++) {
                                List<PointZ> holeLine = (List<PointZ>) tessPolygon.getHoleLine(k);
                                holeLine.replaceAll(SphericalTransform::transform);
                            }
                            polygonZS.set(j, tessPolygon);
                        }
                    } else {
                        List<PointZ> points = (List<PointZ>) shape.getPoints();
                        points.replaceAll(SphericalTransform::transform);
                        if (shape instanceof PolygonZShape)
                            ((PolygonZShape) shape).setPoints_keep(points);
                        else
                            shape.setPoints(points);
                    }
                    gg.setShape(shape);
                    graphics.setGraphicN(i, gg);
                }
                graphics.updateExtent();
                return graphics;
            } else {
                Graphic gg = graphic.getGraphicN(0);
                Shape shape = gg.getShape();
                if (shape instanceof ChartText3D) {
                    PointZ p = ((ChartText3D) shape).getPoint();
                    ((ChartText3D) shape).setPoint(SphericalTransform.transform(p));
                } else {
                    List<PointZ> points = (List<PointZ>) shape.getPoints();
                    points.replaceAll(SphericalTransform::transform);
                    if (shape instanceof PolygonZShape)
                        ((PolygonZShape) shape).setPoints_keep(points);
                    else
                        shape.setPoints(points);
                }

                gg.setShape(shape);
                return gg;
            }
        }
    }
}
