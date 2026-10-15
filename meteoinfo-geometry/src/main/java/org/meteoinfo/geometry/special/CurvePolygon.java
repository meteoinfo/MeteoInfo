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
package org.meteoinfo.geometry.special;

 import org.meteoinfo.geometry.GeometryType;
 import org.meteoinfo.geometry.MultiPolygon;

 /**
 * Curve polygon shape class
 * 
 * @author Yaqiang Wang
 */
public class CurvePolygon extends MultiPolygon {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public CurvePolygon() {

    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    @Override
    public GeometryType getGeometryType(){
        return GeometryType.CURVE_POLYGON;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Clone
     * 
     * @return CurvePolygonShape
     */
    @Override
    public Object clone() {
        CurvePolygon aPGS = new CurvePolygon();
        aPGS.setExtent(this.getExtent());
        aPGS.highValue = highValue;
        aPGS.lowValue = lowValue;
        aPGS.setPartNum(this.getNumParts());
        aPGS.parts = (int[]) parts.clone();
        aPGS.setCoordinates(this.getCoordinates());
        aPGS.setVisible(this.isVisible());
        aPGS.setSelected(this.isSelected());
        
        return aPGS;
    }

    /**
     * Value clone
     * 
     * @return CurvePolygonShape
     */
    @Override
    public CurvePolygon valueClone() {
        CurvePolygon aPGS = new CurvePolygon();
        aPGS.highValue = highValue;
        aPGS.lowValue = lowValue;
        aPGS.setVisible(this.isVisible());
        aPGS.setSelected(this.isSelected());
        
        return aPGS;
    }
    // </editor-fold>
}
