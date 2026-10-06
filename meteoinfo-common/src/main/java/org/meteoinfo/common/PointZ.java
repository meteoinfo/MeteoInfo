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
package org.meteoinfo.common;

/**
 *
 * @author yaqiang
 */
public class PointZ implements Cloneable{
    // <editor-fold desc="Variables">
    public double X;
    public double Y;
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
     */
    public PointZ(double x, double y) {
        X = x;
        Y = y;
        Z = Double.NaN;
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
     * To double array
     * @return Double array
     */
    public double[] toArray() {
        return new double[]{X, Y, Z};
    }

    /**
     * To float array
     * @return Float array
     */
    public float[] toFloatArray() {
        return new float[]{(float) X, (float) Y, (float) Z};
    }

    /**
     * To PointF
     * @return PointF object
     */
    public PointF toPointF() {
        return new PointF((float) X, (float) Y);
    }
    
    /**
     * Clone
     * 
     * @return PointZ object
     */
    public Object clone() {
        PointZ o = null;
        try {
            o = (PointZ) super.clone();
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }

        return o;
    }
    // </editor-fold>
}
