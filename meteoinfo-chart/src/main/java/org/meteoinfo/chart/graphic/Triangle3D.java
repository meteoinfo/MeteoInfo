package org.meteoinfo.chart.graphic;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Triangle3D {
    private Vector3f pointA;
    private Vector3f pointB;
    private Vector3f pointC;
    private Vector3f normalA;
    private Vector3f normalB;
    private Vector3f normalC;

    /**
     * Constructor
     * @param a Point a
     * @param b Point b
     * @param c Point c
     */
    public Triangle3D(Vector3f a, Vector3f b, Vector3f c) {
        this.pointA = a;
        this.pointB = b;
        this.pointC = c;
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
    }

    /**
     * Get normal of point A
     * @return Normal of point A
     */
    public Vector3f getNormalA() {
        if (normalA == null) {
            normalA = pointC.sub(pointA, new Vector3f()).cross(pointB.sub(pointA, new Vector3f()));
        }
        return normalA;
    }

    /**
     * Get normal of point B
     * @return Normal of point B
     */
    public Vector3f getNormalB() {
        if (normalB == null) {
            normalB = pointA.sub(pointB, new Vector3f()).cross(pointC.sub(pointB, new Vector3f()));
        }
        return normalB;
    }

    /**
     * Get normal of point C
     * @return Normal of point C
     */
    public Vector3f getNormalC() {
        if (normalC == null) {
            normalC = pointB.sub(pointC, new Vector3f()).cross(pointA.sub(pointC, new Vector3f()));
        }
        return normalC;
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
