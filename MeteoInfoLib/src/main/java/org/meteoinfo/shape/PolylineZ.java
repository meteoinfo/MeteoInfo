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

import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yaqiang
 */
public class PolylineZ extends Polyline{
    // <editor-fold desc="Variables">

    //private Extent _extent;
    //private List<PointZ> _pointList;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public PolylineZ() {
        super();
        //_extent = new Extent();
        //_pointList = new ArrayList<>();
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

//    /**
//     * Get points
//     *
//     * @return Point list
//     */
//    public List<PointZ> getPoints() {
//        return _pointList;
//    }
//
//    /**
//     * Set points
//     *
//     * @param points Point list
//     */
//    public void setPoints(List<PointZ> points) {
//        _pointList = points;
//        _extent = MIMath.getPointsExtent((getPointDList()));
//    }
//
//    /**
//     * Get extent
//     *
//     * @return Extent
//     */
//    public Extent getExtent() {
//        return _extent;
//    }
//
//    // </editor-fold>
//    // <editor-fold desc="Methods">
//    private List<PointD> getPointDList() {
//        List<PointD> pList = new ArrayList<>();
//        for (PointZ aP : _pointList) {
//            pList.add(aP.toPointD());
//        }
//
//        return pList;
//    }

//    /**
//     * Determine if the polyline is closed
//     *
//     * @return Boolean
//     */
//    public boolean isClosed() {
//        PointZ sPoint = _pointList.get(0);
//        PointZ ePoint = _pointList.get(_pointList.size() - 1);
//        if (MIMath.doubleEquals(sPoint.X, ePoint.X) && MIMath.doubleEquals(sPoint.Y, ePoint.Y)) {
//            return true;
//        } else {
//            return false;
//        }
//    }
    // </editor-fold>
}
