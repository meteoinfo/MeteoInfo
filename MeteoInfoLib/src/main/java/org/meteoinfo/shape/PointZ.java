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

import org.meteoinfo.global.PointD;

/**
 *
 * @author yaqiang
 */
public class PointZ extends PointD implements Cloneable{
    // <editor-fold desc="Variables">

    /**
     * Z coordinate
     */
    public double Z;
    /**
     * Measure
     */
    public double M;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public PointZ() {
    }
    
    /**
     * Constructor
     *
     * @param x X
     * @param y Y
     * @param z Z
     */
    public PointZ(double x, double y, double z) {
        X = x;
        Y = y;
        Z = z;
    }

    /**
     * Constructor
     * @param coord Coordinate array
     */
    public PointZ(double[] coord) {
        X = coord[0];
        Y = coord[1];
        Z = coord[2];
    }

    /**
     * Constructor
     *
     * @param x X
     * @param y Y
     * @param z Z
     * @param m M
     */
    public PointZ(double x, double y, double z, double m) {
        X = x;
        Y = y;
        Z = z;
        M = m;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Convert to PointD
     *
     * @return PointD
     */
    public PointD toPointD() {
        return new PointD(X, Y);
    }

    /**
     * To double array
     * @return Double array
     */
    public double[] toArray() {
        return new double[]{X, Y, Z};
    }
    
    /**
     * Clone
     * 
     * @return PointZ object
     */
    @Override
    public Object clone() {
        return (PointZ)super.clone();
    }
    // </editor-fold>
}
