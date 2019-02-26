/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.shape;

/**
 *
 * @author Yaqiang Wang
 */
public class WindArrow3D extends PointZShape {
    // <editor-fold desc="Variables">
    public double u;
    public double v;
    public double w;
    public float length = 1;
    
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public WindArrow3D() {

    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    @Override
    public ShapeTypes getShapeType(){
        return ShapeTypes.WindArraw;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get arrow end point
     * @return End point
     */
    public PointZ getEndPoint() {
        PointZ sp = (PointZ)this.getPoint();
        PointZ ed = new PointZ();
        ed.X = sp.X + u * length;
        ed.Y = sp.Y + v * length;
        ed.Z = sp.Z + w * length;
        return ed;
    }

    /**
     * Clone
     *
     * @return WindArraw object
     */
    @Override
    public Object clone() {
        WindArrow3D aWA = new WindArrow3D();
        aWA.u = u;
        aWA.v = v;
        aWA.w = w;
        aWA.setPoint(this.getPoint());
        aWA.setValue(this.getValue());
        
        return aWA;
    }
    // </editor-fold>
}
