/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.shape;

import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.jts.geom.Coordinate;
import org.meteoinfo.jts.geom.Geometry;

/**
 *
 * @author yaqiang
 */
public class PolygonMShape extends PolygonShape{
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public PolygonMShape(){
        super();
    }
    
    /**
     * Constructor
     * @param geometry Geometry
     */
    public PolygonMShape(Geometry geometry) {
        Coordinate[] cs = geometry.getCoordinates();
        List<PointZ> points = new ArrayList();
        for (Coordinate c : cs)
            points.add(new PointZ(c.x, c.y, c.z, c.m));
        this.setPoints(points);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    @Override
    public ShapeTypes getShapeType(){
        return ShapeTypes.PolygonM;
    }
    
    /**
     * Get M Array
     *
     * @return M value array
     */
    public double[] getMArray() {
        double[] mArray = new double[this.getPoints().size()];
        for (int i = 0; i < this.getPoints().size(); i++) {
            mArray[i] = ((PointM)this.getPoints().get(i)).M;
        }

        return mArray;
    }
    
    /**
     * Get M range - min, max
     *
     * @return M min, max
     */
    public double[] getMRange() {
        return MIMath.arrayMinMax(getMArray());
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
