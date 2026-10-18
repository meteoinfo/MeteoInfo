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
    private Vector3f normalA;
    private Vector3f normalB;
    private Vector3f normalC;

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
     * Set points
     * @param a Point a
     * @param b Point b
     * @param c Point c
     */
    public void setPoints(Vector3f a, Vector3f b, Vector3f c) {
        this.pointA = a;
        this.pointB = b;
        this.pointC = c;
    }

    /**
     * Set points
     * @param points 3 points
     */
    public void setPoints(List<Vector3f> points) {
        this.pointA = points.get(0);
        this.pointB = points.get(1);
        this.pointC = points.get(2);
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

    /**
     * Recursively subdivides a triangle in 3D Cartesian space using pure linear interpolation.
     * No spherical projection or normalization is applied to the midpoints.
     *
     * @param triangle The original triangle to subdivide
     * @param depth    Subdivision depth; each level produces 4 child triangles (4^depth total)
     * @return List of subdivided triangles. Returns a singleton list containing the original if depth <= 0.
     */
    public static List<Triangle> subdivide(Triangle triangle, int depth) {
        List<Triangle> result = new ArrayList<>();
        if (depth <= 0) {
            result.add(triangle);
            return result;
        }
        subdivideRecursive(triangle, depth, result);
        return result;
    }

    /**
     * Internal recursive worker for Cartesian triangle subdivision.
     * Splits one triangle into four at each recursion level by computing
     * linear midpoints along each edge.
     *
     * @param tri   Current triangle being processed
     * @param depth Remaining recursion depth
     * @param out   Accumulator list for resulting leaf triangles
     */
    private static void subdivideRecursive(Triangle tri, int depth, List<Triangle> out) {
        // Base case: emit the triangle as-is when recursion depth is exhausted
        if (depth == 0) {
            out.add(tri);
            return;
        }

        // Compute linear midpoints for all three edges (no normalization / no projection)
        Vector3f abMid = linearMidpoint(tri.getPointA(), tri.getPointB());
        Vector3f bcMid = linearMidpoint(tri.getPointB(), tri.getPointC());
        Vector3f caMid = linearMidpoint(tri.getPointC(), tri.getPointA());

        // Split into 4 child triangles and recurse on each
        // Child 1: corner A with adjacent edge midpoints
        subdivideRecursive(new Triangle(tri.getPointA(), abMid, caMid), depth - 1, out);
        // Child 2: corner B with adjacent edge midpoints
        subdivideRecursive(new Triangle(abMid, tri.getPointB(), bcMid), depth - 1, out);
        // Child 3: corner C with adjacent edge midpoints
        subdivideRecursive(new Triangle(caMid, bcMid, tri.getPointC()), depth - 1, out);
        // Child 4: center triangle formed by the three edge midpoints
        subdivideRecursive(new Triangle(abMid, bcMid, caMid), depth - 1, out);
    }

    /**
     * Computes the 3D linear midpoint between two vectors: (a + b) / 2.
     * The result lies exactly on the straight line segment between a and b,
     * which is correct for planar geometry but NOT for spherical surfaces.
     *
     * @param a First endpoint
     * @param b Second endpoint
     * @return New Vector3f representing the linear midpoint
     */
    private static Vector3f linearMidpoint(Vector3f a, Vector3f b) {
        return new Vector3f(
                (a.x + b.x) * 0.5f,
                (a.y + b.y) * 0.5f,
                (a.z + b.z) * 0.5f
        );
    }

    /**
     * Subdivides a triangle into well-shaped (near-equilateral) children using
     * Longest Edge Bisection. Unlike midpoint subdivision, this method prevents
     * angle degradation and guarantees that child triangles converge toward
     * equilateral shapes regardless of the original triangle's aspect ratio.
     *
     * @param triangle      The original triangle to subdivide
     * @param targetEdgeLen Maximum allowed edge length; subdivision stops when
     *                      all edges are shorter than this threshold
     * @return List of well-shaped subdivided triangles
     */
    public static List<Triangle> subdivideEquilateral(Triangle triangle, float targetEdgeLen) {
        List<Triangle> result = new ArrayList<>();
        longestEdgeBisect(triangle, targetEdgeLen, result);
        return result;
    }

    /**
     * Recursively bisects the triangle along its longest edge until all edges
     * are below the target length. This strategy maximizes the minimum angle
     * in each subdivision step, avoiding sliver triangle generation.
     *
     * @param tri           Current triangle being processed
     * @param targetEdgeLen Stopping criterion for edge length
     * @param out           Accumulator list for resulting leaf triangles
     */
    private static void longestEdgeBisect(Triangle tri, float targetEdgeLen, List<Triangle> out) {
        Vector3f a = tri.getPointA();
        Vector3f b = tri.getPointB();
        Vector3f c = tri.getPointC();

        // Compute squared edge lengths to avoid expensive sqrt operations
        float abSq = a.distanceSquared(b);
        float bcSq = b.distanceSquared(c);
        float caSq = c.distanceSquared(a);
        float maxEdgeSq = Math.max(abSq, Math.max(bcSq, caSq));

        // Base case: all edges are within tolerance, emit as-is
        if (maxEdgeSq <= targetEdgeLen * targetEdgeLen) {
            out.add(tri);
            return;
        }

        // Identify the longest edge and compute its linear midpoint
        Vector3f mid;
        if (abSq >= bcSq && abSq >= caSq) {
            // AB is the longest edge
            mid = linearMidpoint(a, b);
            // Split into two children sharing the new midpoint
            longestEdgeBisect(new Triangle(a, mid, c), targetEdgeLen, out);
            longestEdgeBisect(new Triangle(mid, b, c), targetEdgeLen, out);
        } else if (bcSq >= abSq && bcSq >= caSq) {
            // BC is the longest edge
            mid = linearMidpoint(b, c);
            longestEdgeBisect(new Triangle(b, mid, a), targetEdgeLen, out);
            longestEdgeBisect(new Triangle(mid, c, a), targetEdgeLen, out);
        } else {
            // CA is the longest edge
            mid = linearMidpoint(c, a);
            longestEdgeBisect(new Triangle(c, mid, b), targetEdgeLen, out);
            longestEdgeBisect(new Triangle(mid, a, b), targetEdgeLen, out);
        }
    }
}
