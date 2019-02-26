 /* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.shape;

import org.meteoinfo.global.MIMath;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.global.Extent;
import org.meteoinfo.jts.geom.Coordinate;
import org.meteoinfo.jts.geom.Geometry;

/**
 * PolylineZ shape class
 *
 * @author yaqiang
 */
public class PolylineZShape extends PolylineShape {
    // <editor-fold desc="Variables">

    //private List<PointZ> _points = new ArrayList<PointZ>();
    //private List<PolylineZ> _polylines = new ArrayList<PolylineZ>();
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public PolylineZShape() {
        super();
    }
    
    /**
     * Constructor
     * @param geometry Geometry
     */
    public PolylineZShape(Geometry geometry) {
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
        return ShapeTypes.PolylineZ;
    }

    /**
     * Get Z Array
     *
     * @return Z array
     */
    public double[] getZArray() {
        double[] zArray = new double[this.getPoints().size()];
        for (int i = 0; i < this.getPoints().size(); i++) {
            zArray[i] = ((PointZ)this.getPoints().get(i)).Z;
        }

        return zArray;
    }

    /**
     * Get Z Array
     *
     * @return Z value array
     */
    public double[] getMArray() {
        double[] mArray = new double[this.getPoints().size()];
        for (int i = 0; i < this.getPoints().size(); i++) {
            mArray[i] = ((PointZ)this.getPoints().get(i)).M;
        }

        return mArray;
    }

    /**
     * Get Z range - min, max
     *
     * @return Z min, max
     */
    public double[] getZRange() {
        return MIMath.arrayMinMax(getZArray());
    }

    /**
     * Get M range - min, max
     *
     * @return M min, max
     */
    public double[] getMRange() {
        return MIMath.arrayMinMax(getMArray());
    }
    
    //@Override
    public Object clone_back(){
        PolylineZShape o = (PolylineZShape)super.clone();
//        List<PointZ> points = new ArrayList<>();
//        for (PointZ point : (List<PointZ>)this.getPoints()){
//            points.add((PointZ)point.clone());
//        }
//        o.setPoints(points);
        
        return o;
    }

    /**
     * Clone
     *
     * @return PolylineZShape object
     */
    @Override
    public Object clone() {
        PolylineZShape aPLS = new PolylineZShape();
        aPLS.setValue(this.getValue());
        aPLS.setExtent((Extent)this.getExtent().clone());
        aPLS.setPartNum(this.getPartNum());
        aPLS.parts = (int[]) parts.clone();
        List<PointZ> points = new ArrayList<>();
        for (PointZ point : (List<PointZ>)this.getPoints()){
            points.add((PointZ)point.clone());
        }
        aPLS.setPoints(points);
        aPLS.setVisible(this.isVisible());
        aPLS.setSelected(this.isSelected());
        aPLS.setLegendIndex(this.getLegendIndex());
        //aPLS.ZRange = (double[])ZRange.Clone();
        //aPLS.MRange = (double[])MRange.Clone();
        //aPLS.ZArray = (double[])ZArray.Clone();
        //aPLS.MArray = (double[])MArray.Clone();

        return aPLS;
    }

    /**
     * Value clone
     *
     * @return PolylineZShape object
     */
    @Override
    public Object valueClone() {
        PolylineZShape aPLS = new PolylineZShape();
        aPLS.setValue(this.getValue());
        aPLS.setVisible(this.isVisible());
        aPLS.setSelected(this.isSelected());
        aPLS.setLegendIndex(this.getLegendIndex());

        return aPLS;
    }
    // </editor-fold>
}
