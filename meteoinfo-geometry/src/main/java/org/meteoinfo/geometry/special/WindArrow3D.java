/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.geometry.special;

import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.Geometry;
import org.meteoinfo.geometry.GeometryType;
import org.meteoinfo.geometry.Point;

/**
 *
 * @author Yaqiang Wang
 */
public class WindArrow3D extends Point {
    // <editor-fold desc="Variables">
    public double u;
    public double v;
    public double w;
    public float scale = 1;
    private float headWidth = 1;
    private float headLength = 2.5f;
    
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
    public GeometryType getGeometryType(){
        return GeometryType.WIND_ARROW;
    }

    /**
     * Get head width
     * @return Head width
     */
    public float getHeadWidth() {
        return this.headWidth;
    }

    /**
     * Set head width
     * @param value Head width
     */
    public void setHeadWidth(float value) {
        this.headWidth = value;
    }

    /**
     * Get head length
     * @return Head length
     */
    public float getHeadLength() {
        return this.headLength;
    }

    /**
     * Set head length
     * @param value Head length
     */
    public void setHeadLength(float value) {
        this.headLength = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get arrow end point
     * @return End point
     */
    public Coordinate getEndPoint() {
        Coordinate sp = (Coordinate)this.getCoordinate();
        Coordinate ed = new Coordinate();
        ed.x = sp.x + u * scale;
        ed.y = sp.y + v * scale;
        ed.z = sp.z + w * scale;
        return ed;
    }

    /**
     * Get intersection shape
     * @param b Other shape
     * @return Intersection shape
     */
    @Override
    public Geometry intersection(Geometry b){
        org.locationtech.jts.geom.Geometry g1 = this.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g2 = b.toJTSGeometry();
        org.locationtech.jts.geom.Geometry g3 = g1.intersection(g2);

        if (g3.getNumPoints() < 1)
            return null;
        else {
            return (WindArrow3D) this.clone();
        }
    }

    /**
     * Clone
     *
     * @return WindArrow object
     */
    @Override
    public Object clone() {
        WindArrow3D aWA = new WindArrow3D();
        aWA.u = u;
        aWA.v = v;
        aWA.w = w;
        aWA.scale = scale;
        aWA.setCoordinate(this.getCoordinate());
        aWA.setValue(this.getValue());
        aWA.setLegendIndex(this.getLegendIndex());
        
        return aWA;
    }
    // </editor-fold>
}
