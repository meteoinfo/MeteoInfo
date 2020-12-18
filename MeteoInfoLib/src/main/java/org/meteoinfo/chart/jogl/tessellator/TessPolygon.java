package org.meteoinfo.chart.jogl.tessellator;

import org.meteoinfo.shape.PolygonZ;

import java.util.List;

public class TessPolygon extends PolygonZ {
    private List<Primitive> primitives;

    /**
     * Constructor
     * @param primitives Primitive list
     */
    public TessPolygon(List<Primitive> primitives) {
        this.primitives = primitives;
    }

    /**
     * Constructor
     * @param polygonZ Input PolygonZ
     */
    public TessPolygon(PolygonZ polygon) {
        this.setOutLine(polygon.getOutLine());
        this.setHoleLines(polygon.getHoleLines());
        this.setExtent(polygon.getExtent());

        PrimitiveTessellator tessellator = new PrimitiveTessellator();
        try {
            this.primitives = tessellator.getPrimitives(polygon);
        } catch (PrimitiveTessellator.TesselationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get primitive list
     * @return Primitive list
     */
    public List<Primitive> getPrimitives() {
        return this.primitives;
    }
}
