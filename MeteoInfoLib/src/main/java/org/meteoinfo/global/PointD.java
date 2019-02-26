/*
 * Copyright 2012 Yaqiang Wang,
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
package org.meteoinfo.global;

/**
 *
 * @author User
 */
public class PointD implements Cloneable{
    // <editor-fold desc="Variables">

    public double X;
    public double Y;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public PointD() {
    }

    /**
     * Contructor
     * @param x
     * @param y 
     */
    public PointD(double x, double y) {
        X = x;
        Y = y;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Equals of two pointDs
     * @param p PointD
     * @return Boolean
     */
    public boolean equals(PointD p){
        if (this.X != p.X)
            return false;
        
        return this.Y == p.Y;
    }
    
    /**
     * Clone
     * 
     * @return PointD object
     */
    @Override
    public Object clone() {
        PointD o = null;
        try {
            o = (PointD)super.clone();
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        
        return o;
    }
    // </editor-fold>
}
