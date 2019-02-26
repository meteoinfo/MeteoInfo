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
 * PointF class
 * 
 * @author Yaqiang Wang
 */
public class PointF implements Cloneable{
     // <editor-fold desc="Variables">

    public float X;
    public float Y;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public PointF() {
    }

    /**
     * Contructor
     * @param x
     * @param y 
     */
    public PointF(float x, float y) {
        X = x;
        Y = y;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Clone
     * 
     * @return PointF object
     * @throws java.lang.CloneNotSupportedException
     */
    @Override
    public Object clone() {
        PointF o = null;
        try {
            o = (PointF)super.clone();
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        
        return o;
    }
    // </editor-fold>
}
