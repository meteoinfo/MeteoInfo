package org.meteoinfo.chart.jogl.tessellator;

import org.maplibre.earcut4j.Earcut;
import org.meteoinfo.chart.graphic.Triangle;
import org.meteoinfo.geometry.Coordinate;
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
}
