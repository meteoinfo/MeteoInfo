package org.meteoinfo.chart.jogl.tessellator;

import org.meteoinfo.geometry.shape.PolygonZ;

import java.util.ArrayList;
import java.util.List;

public class TessPolygon extends PolygonZ {
    protected List<Primitive> primitives;

    /**
     * Constructor
     */
    public TessPolygon() {
        this.primitives = new ArrayList<>();
    }

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
