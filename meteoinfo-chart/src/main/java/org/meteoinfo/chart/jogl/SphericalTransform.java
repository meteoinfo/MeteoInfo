package org.meteoinfo.chart.jogl;

import org.joml.Vector3f;
import org.meteoinfo.chart.ChartText3D;
import org.meteoinfo.chart.graphic.*;
import org.meteoinfo.chart.jogl.tessellator.Primitive;
import org.meteoinfo.chart.jogl.tessellator.TessPolygon;
import org.meteoinfo.chart.graphic.Graphic;
import org.meteoinfo.chart.graphic.GraphicCollection;
import org.meteoinfo.geometry.Extent;
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
        Vector3f xyz = transform((float)p.x, (float)p.y, (float)p.z);

        p.x = xyz.x;
        p.y = xyz.y;
        p.z = xyz.z;

        return p;
    }

    /**
     * Transform a geographic extent (longitude / latitude / altitude) into an ECEF
     * bounding box that fully encloses the spherical shell sector.
     *
     * @param geo Geographic extent - X: longitude, Y: latitude, Z: altitude
     * @return ECEF bounding box
     */
    public static Extent transform(Extent geo) {
        double lonMin = geo.minX;
        double lonMax = geo.maxX;
        if (lonMax < lonMin) {
            // Sector crossing the date line
            lonMax += 360;
        }
        double latMin = Math.max(-90.0, geo.minY);
        double latMax = Math.min(90.0, geo.maxY);
        double altMin = geo.minZ;
        double altMax = geo.maxZ;

        double xMin = Double.MAX_VALUE;
        double xMax = -Double.MAX_VALUE;
        double yMin = Double.MAX_VALUE;
        double yMax = -Double.MAX_VALUE;
        double zMin = Double.MAX_VALUE;
        double zMax = -Double.MAX_VALUE;

        // Sample the shell surface so the extreme directions (every 90 degrees of
        // longitude, plus the poles) are always contained in the returned box. The
        // sample count is derived from the extent so that the first and the last
        // longitude / latitude are always included.
        int lonN = Math.max(1, (int) Math.ceil((lonMax - lonMin) / 1.0));
        int latN = Math.max(1, (int) Math.ceil((latMax - latMin) / 1.0));
        for (int i = 0; i <= lonN; i++) {
            double lon = lonMin + (lonMax - lonMin) * i / lonN;
            for (int j = 0; j <= latN; j++) {
                double lat = latMin + (latMax - latMin) * j / latN;
                for (int k = 0; k < 2; k++) {
                    float alt = (float) (k == 0 ? altMin : altMax);
                    Vector3f p = transform((float) lon, (float) lat, alt);
                    xMin = Math.min(xMin, p.x);
                    xMax = Math.max(xMax, p.x);
                    yMin = Math.min(yMin, p.y);
                    yMax = Math.max(yMax, p.y);
                    zMin = Math.min(zMin, p.z);
                    zMax = Math.max(zMax, p.z);
                }
            }
        }

        Extent extent = new Extent();
        extent.minX = xMin;
        extent.maxX = xMax;
        extent.minY = yMin;
        extent.maxY = yMax;
        extent.minZ = zMin;
        extent.maxZ = zMax;
        return extent;
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
            VolumeGraphic volumeGraphic = (VolumeGraphic) graphic;
            Extent geo = volumeGraphic.getGeoExtent();
            if (geo == null) {
                // Keep the geographic (lon/lat/alt) extent for the spherical ray
                // casting - it must be a copy because the graphic extent itself is
                // replaced by the ECEF bounding box below.
                geo = (Extent) volumeGraphic.getExtent().clone();
                volumeGraphic.setGeoExtent(geo);
            }
            // Convert the lon/lat/alt range into an ECEF bounding box so the scene
            // transform (and the drawing extent) stays in cartesian space, and let
            // the renderer know the volume must be ray-cast against an earth shell.
            volumeGraphic.setExtent(transform(geo));
            volumeGraphic.setSpherical(true);
            return volumeGraphic;
        } else if (graphic instanceof PolygonGraphicCollection) {
            PolygonGraphicCollection polygonGraphic = (PolygonGraphicCollection) graphic;
            polygonGraphic.updateLineVertex();
            if (polygonGraphic.isDrawOutline()) {
                float[] lineVertex = polygonGraphic.getLineVertex();
                Vector3f vector3f;
                for (int i = 0; i < lineVertex.length; i+=3) {
                    vector3f = transform(lineVertex[i], lineVertex[i+1], lineVertex[i+2]);
                    lineVertex[i] = vector3f.x;
                    lineVertex[i+1] = vector3f.y;
                    lineVertex[i+2] = vector3f.z;
                }
                polygonGraphic.setLineVertex(lineVertex);
            }
            if (polygonGraphic.isFill()) {
                polygonGraphic.tessellate(true);
                float[] vertexPosition = polygonGraphic.getVertexPosition();
                Vector3f vector3f;
                for (int i = 0; i < vertexPosition.length; i+=3) {
                    vector3f = transform(vertexPosition[i], vertexPosition[i+1], vertexPosition[i+2]);
                    vertexPosition[i] = vector3f.x;
                    vertexPosition[i+1] = vector3f.y;
                    vertexPosition[i+2] = vector3f.z;
                }
                polygonGraphic.setVertexPosition(vertexPosition);
            }
            polygonGraphic.updateExtent();
            return polygonGraphic;
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
