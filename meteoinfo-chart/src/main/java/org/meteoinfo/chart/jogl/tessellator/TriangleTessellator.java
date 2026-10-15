package org.meteoinfo.chart.jogl.tessellator;

import org.maplibre.earcut4j.Earcut;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.Polygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * High-performance polygon tessellator using EarCut algorithm.
 * Pure Java, no JNI, optimized for GIS polygons with holes.
 */
public class TriangleTessellator {

    /**
     * Tessellates a PolygonZ into independent triangles (3 PointZ per triangle).
     * Output is directly consumable by VBO builders without further processing.
     *
     * @param polygon The polygon to tessellate (supports concave & holed)
     * @return List of triangles, each as PointZ[3]. Empty list if degenerate.
     */
    public static List<Coordinate[]> tessellate(Polygon polygon) {
        // 1. Flatten vertices into double[] for EarCut
        //    Format: [x0,y0, x1,y1, ..., xn,yn]
        List<Double> coords = new ArrayList<>();
        List<Integer> holeIndices = new ArrayList<>();

        // Outer ring
        List<Coordinate> outer = polygon.getOutLine();
        for (Coordinate p : outer) {
            coords.add(p.x);
            coords.add(p.y);
        }

        // Inner rings (holes)
        if (polygon.hasHoles()) {
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                // Record the starting index of this hole in the flat array
                holeIndices.add(coords.size() / 2);

                List<Coordinate> hole = polygon.getHoleLine(i);
                for (Coordinate p : hole) {
                    coords.add(p.x);
                    coords.add(p.y);
                }
            }
        }

        // Convert to primitive array (Earcut requires double[])
        double[] data = coords.stream().mapToDouble(Double::doubleValue).toArray();
        int[] holes = holeIndices.stream().mapToInt(Integer::intValue).toArray();

        // 2. Run EarCut — returns triangle indices [i0,i1,i2, i3,i4,i5, ...]
        List<Integer> indices;
        try {
            indices = Earcut.earcut(data, holes.length > 0 ? holes : null, 2);
        } catch (Exception e) {
            System.err.println("EarCut failed: " + e.getMessage());
            return Collections.emptyList();
        }

        if (indices == null || indices.size() < 3) {
            return Collections.emptyList();
        }

        // 3. Map indices back to PointZ triangles
        //    Reconstruct full vertex list for index lookup
        List<Coordinate> allVertices = new ArrayList<>(data.length / 2);
        for (int i = 0; i < data.length; i += 2) {
            allVertices.add(new Coordinate(data[i], data[i + 1], 0.0));
        }

        // Preserve original Z values from source polygon
        restoreZValues(allVertices, polygon);

        List<Coordinate[]> triangles = new ArrayList<>(indices.size() / 3);
        for (int i = 0; i + 2 < indices.size(); i += 3) {
            Coordinate[] tri = new Coordinate[]{
                    allVertices.get(indices.get(i)),
                    allVertices.get(indices.get(i + 1)),
                    allVertices.get(indices.get(i + 2))
            };
            triangles.add(tri);
        }

        return triangles;
    }

    /**
     * Restores Z coordinates that EarCut discards (it operates in 2D).
     * Matches flattened vertices back to original PointZ by XY proximity.
     */
    private static void restoreZValues(List<Coordinate> flatVerts, Polygon polygon) {
        // Build a combined ordered list of original vertices matching EarCut's traversal order
        List<Coordinate> originals = new ArrayList<>();
        originals.addAll(polygon.getOutLine());
        if (polygon.hasHoles()) {
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                originals.addAll(polygon.getHoleLine(i));
            }
        }

        // Direct positional mapping (EarCut preserves vertex order)
        for (int i = 0; i < Math.min(flatVerts.size(), originals.size()); i++) {
            flatVerts.get(i).z = originals.get(i).z;
        }
    }
}
