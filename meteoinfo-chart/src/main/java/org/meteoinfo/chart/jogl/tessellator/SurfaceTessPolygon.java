package org.meteoinfo.chart.jogl.tessellator;

import org.meteoinfo.common.Extent;
import org.meteoinfo.geometry.geoprocess.ClipLine;
import org.meteoinfo.geometry.geoprocess.GeoComputation;
import org.meteoinfo.geometry.shape.PolygonZ;

import java.util.ArrayList;
import java.util.List;

public class SurfaceTessPolygon extends TessPolygon {

    /**
     * Constructor
     * @param primitives Primitive list
     */
    public SurfaceTessPolygon(List<Primitive> primitives) {
        super(primitives);
    }

    /**
     * Constructor
     * @param polygonZ Input PolygonZ
     */
    public SurfaceTessPolygon(PolygonZ polygon) {
        this.setOutLine(polygon.getOutLine());
        this.setHoleLines(polygon.getHoleLines());
        this.setExtent(polygon.getExtent());

        PrimitiveTessellator tessellator = new PrimitiveTessellator();

        List<PolygonZ> polygonZS = new ArrayList<>();
        Extent extent = polygon.getExtent();
        if (extent.getWidth() > 1) {
            for (double min = extent.minX + 1; min < extent.maxX; min+=1) {
                ClipLine clipLine = new ClipLine();
                clipLine.setLongitude(true);
                clipLine.setValue(min);
                clipLine.setLeftOrTop(true);
                //polygonZS.addAll((List<PolygonZ>)GeoComputation.clipPolygon(polygon, clipLine));
            }
        }

        try {
            this.primitives = tessellator.getPrimitives(polygon);
        } catch (PrimitiveTessellator.TesselationException e) {
            e.printStackTrace();
        }
    }
}
