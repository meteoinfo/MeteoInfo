package org.meteoinfo.chart.graphic;

import org.joml.Vector3f;
import org.meteoinfo.geometry.Coordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Triangle {
    private Vector3f pointA;
    private Vector3f pointB;
    private Vector3f pointC;
    private Vector3f faceNormal;

    /**
     * Constructor
     * @param a Point a
     * @param b Point b
     * @param c Point c
     */
    public Triangle(Vector3f a, Vector3f b, Vector3f c) {
        this.pointA = a;
        this.pointB = b;
        this.pointC = c;
    }

    /**
     * Constructor
     * @param a Point a
     * @param b Point b
     * @param c Point c
     */
    public Triangle(Coordinate a, Coordinate b, Coordinate c) {
        this.pointA = new Vector3f((float) a.x, (float) a.y, (float) a.z);
        this.pointB = new Vector3f((float) b.x, (float) b.y, (float) b.z);
        this.pointC = new Vector3f((float) c.x, (float) c.y, (float) c.z);
    }

    /**
     * Get point by index
     * @param index The index
     * @return The point
     */
    public Vector3f getPoint(int index) {
        switch (index) {
            case 0:
                return pointA;
            case 1:
                return pointB;
            case 2:
                return pointC;
            default:
                return null;
        }
    }

    /**
     * Get point a
     * @return Point a
     */
    public Vector3f getPointA() {
        return pointA;
    }

    /**
     * Set point a
     * @param a Point a
     */
    public void setPointA(Vector3f a) {
        this.pointA = a;
        this.faceNormal = null;
    }

    /**
     * Get point b
     * @return Point b
     */
    public Vector3f getPointB() {
        return pointB;
    }

    /**
     * Set point b
     * @param b Point b
     */
    public void setPointB(Vector3f b) {
        this.pointB = b;
        this.faceNormal = null;
    }

    /**
     * Get point c
     * @return Point c
     */
    public Vector3f getPointC() {
        return this.pointC;
    }

    /**
     * Set point c
     * @param c Point c
     */
    public void setPointC(Vector3f c) {
        this.pointC = c;
        this.faceNormal = null;
    }

    /**
     * Set points
     * @param a Point a
     * @param b Point b
     * @param c Point c
     */
    public void setPoints(Vector3f a, Vector3f b, Vector3f c) {
        this.pointA = a;
        this.pointB = b;
        this.pointC = c;
        this.faceNormal = null;
    }

    /**
     * Set points
     * @param points 3 points
     */
    public void setPoints(List<Vector3f> points) {
        setPoints(points.get(0), points.get(1), points.get(2));
    }

    /**
     * Returns the unit face normal shared by all three vertices.
     * For a planar triangle, vertex normals are identical to the face normal.
     * Computed once and cached; invalidated automatically on point mutation.
     */
    public Vector3f getFaceNormal() {
        if (faceNormal == null) {
            Vector3f edgeAB = new Vector3f(pointB).sub(pointA);
            Vector3f edgeAC = new Vector3f(pointC).sub(pointA);
            faceNormal = edgeAB.cross(edgeAC, new Vector3f());

            float lenSq = faceNormal.lengthSquared();
            if (lenSq > 1e-12f) {
                faceNormal.normalize();
            } else {
                // Degenerate triangle fallback
                faceNormal.set(0, 1, 0);
            }
        }
        return faceNormal;
    }

    /**
     * Get normal of point A
     * @return Normal of point A
     */
    public Vector3f getNormalA() {
        return getFaceNormal();
    }

    /**
     * Get normal of point B
     * @return Normal of point B
     */
    public Vector3f getNormalB() {
        return getFaceNormal();
    }

    /**
     * Get normal of point C
     * @return Normal of point C
     */
    public Vector3f getNormalC() {
        return getFaceNormal();
    }

    /**
     * Get normal of a point
     * @param point The point
     * @return The normal of the point
     */
    public Vector3f getNormal(Vector3f point) {
        if (point.equals(pointA)) {
            return getNormalA();
        } else if (point.equals(pointB)) {
            return getNormalB();
        } else if (point.equals(pointC)) {
            return getNormalC();
        } else {
            return null;
        }
    }

    /**
     * Get all points
     * @return All points
     */
    public List<Vector3f> getPoints() {
        return new ArrayList<Vector3f>(Arrays.asList(pointA, pointB, pointC));
    }

    /**
     * Get all normals
     * @return All normals
     */
    public List<Vector3f> getNormals() {
        return new ArrayList<Vector3f>(Arrays.asList(getNormalA(), getNormalB(), getNormalC()));
    }
}
