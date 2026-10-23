package org.meteoinfo.chart.jogl.tessellator;

import org.joml.Vector3f;
import org.maplibre.earcut4j.Earcut;
import org.meteoinfo.chart.graphic.PolygonGraphic;
import org.meteoinfo.chart.graphic.Triangle;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.MultiPolygon;
import org.meteoinfo.geometry.Polygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * High-performance 3D polygon tessellator using EarCut algorithm.
 * Automatically selects the optimal projection plane to handle polygons
 * parallel to any axis (including Z-axis degenerate cases).
 * Pure Java, no JNI, zero-GC index mapping.
 */
public class TriangleTessellator {

    /**
     * Tessellates a 3D Polygon into independent triangles.
     * Robustly handles concave polygons, holes, and arbitrary 3D orientations.
     *
     * @param polygon The polygon to tessellate
     * @return List of triangles, each as Coordinate[3]. Empty list if degenerate.
     */
    public static List<Triangle> tessellate(Polygon polygon) {
        List<Coordinate> outer = polygon.getOutLine();
        if (outer == null || outer.size() < 3) {
            return Collections.emptyList();
        }

        // 1. Determine optimal projection plane from outer ring
        int dropAxis = getProjectionDropAxis(outer);

        // 2. Flatten ALL rings using the SAME optimal projection plane
        List<Double> coords = new ArrayList<>();
        List<Integer> holeIndices = new ArrayList<>();

        flattenRing(outer, dropAxis, coords);

        if (polygon.hasHoles()) {
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                holeIndices.add(coords.size() / 2);
                List<Coordinate> hole = polygon.getHoleLine(i);
                if (hole != null && !hole.isEmpty()) {
                    flattenRing(hole, dropAxis, coords);
                }
            }
        }

        double[] data = coords.stream().mapToDouble(Double::doubleValue).toArray();
        int[] holes = holeIndices.isEmpty() ? null : holeIndices.stream().mapToInt(Integer::intValue).toArray();

        // 3. Run EarCut on the 2D projection
        List<Integer> indices;
        try {
            indices = Earcut.earcut(data, holes, 2);
        } catch (Exception e) {
            System.err.println("EarCut failed on dropAxis=" + dropAxis + ": " + e.getMessage());
            return Collections.emptyList();
        }

        if (indices == null || indices.size() < 3) {
            return Collections.emptyList();
        }

        // 4. Map 2D indices directly back to ORIGINAL 3D vertices
        //    EarCut preserves vertex insertion order, so no Z-restoration needed.
        List<Coordinate> allOriginalVertices = new ArrayList<>(data.length / 2);
        allOriginalVertices.addAll(outer);
        if (polygon.hasHoles()) {
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                List<Coordinate> hole = polygon.getHoleLine(i);
                if (hole != null) {
                    allOriginalVertices.addAll(hole);
                }
            }
        }

        List<Triangle> triangles = new ArrayList<>(indices.size() / 3);
        for (int i = 0; i + 2 < indices.size(); i += 3) {
            triangles.add(new Triangle(
                    allOriginalVertices.get(indices.get(i)),
                    allOriginalVertices.get(indices.get(i + 1)),
                    allOriginalVertices.get(indices.get(i + 2))
            ));
        }

        return triangles;
    }

    public static List<Triangle> tessellate(MultiPolygon polygon) {
        List<Triangle> triangles = new ArrayList<>();
        for (Polygon poly : polygon.getPolygons()) {
            triangles.addAll(tessellate(poly));
        }

        return triangles;
    }

    public static List<Triangle> tessellate(PolygonGraphic polygonGraphic) {
        return tessellate((MultiPolygon) polygonGraphic.getGeometry());
    }

    /**
     * Determines the optimal 2D projection plane via Newell's method.
     * Returns the axis index to DROP: 0=X, 1=Y, 2=Z.
     * Choosing the axis with largest normal component maximizes projected area.
     */
    private static int getProjectionDropAxis(List<Coordinate> ring) {
        double nx = 0, ny = 0, nz = 0;
        int n = ring.size();
        for (int i = 0; i < n; i++) {
            Coordinate curr = ring.get(i);
            Coordinate next = ring.get((i + 1) % n);
            nx += (curr.y - next.y) * (curr.z + next.z);
            ny += (curr.z - next.z) * (curr.x + next.x);
            nz += (curr.x - next.x) * (curr.y + next.y);
        }

        double ax = Math.abs(nx), ay = Math.abs(ny), az = Math.abs(nz);
        if (ax >= ay && ax >= az) return 0; // Project onto YZ
        if (ay >= ax && ay >= az) return 1; // Project onto XZ
        return 2;                             // Project onto XY (default)
    }

    /**
     * Projects a 3D ring onto 2D by dropping the specified axis.
     */
    private static void flattenRing(List<Coordinate> ring, int dropAxis, List<Double> out) {
        for (Coordinate p : ring) {
            switch (dropAxis) {
                case 0: out.add(p.y); out.add(p.z); break;
                case 1: out.add(p.x); out.add(p.z); break;
                default: out.add(p.x); out.add(p.y); break;
            }
        }
    }

    /**
     * Subdivide triangles internal
     * @param triangles Triangles to be subdivide
     * @param targetEdgeLen Maximum allowed edge length; subdivision stops when
     *                      all edges are shorter than this threshold
     * @return Subdivided triangles
     */
    public List<Triangle> subdivideEquilateral(List<Triangle> triangles, float targetEdgeLen) {
        List<Triangle> result = new ArrayList<>();
        for (Triangle triangle : triangles) {
            result.addAll(subdivideEquilateral(triangle, targetEdgeLen));
        }

        return result;
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
