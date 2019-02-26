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
package org.meteoinfo.shape;

import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.global.PointF;
import java.util.ArrayList;
import java.util.List;

/**
 * Polyline class
 * 
 * @author Yaqiang Wang
 */
public class Polyline {
    // <editor-fold desc="Variables">

    private Extent _extent;
    private List<? extends PointD> _pointList;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    public Polyline() {
        _extent = new Extent();
        _pointList = new ArrayList<>();
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get point list
     * @return point list
     */
    public List<? extends PointD> getPointList() {
        return _pointList;
    }

    /**
     * Set point list
     * @param points point list
     */
    public void setPointList(List<? extends PointD> points) {
        _pointList = points;
        _extent = MIMath.getPointsExtent(_pointList);
    }

    /**
     * Get extent
     * @return extent
     */
    public Extent getExtent() {
        return _extent;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Set points
     * 
     * @param points point array
     */
    public void setPoints(PointF[] points) {
        List<PointD> pointList = new ArrayList<>();
        for (PointF aP : points) {
            pointList.add(new PointD(aP.X, aP.Y));
        }
        _pointList = pointList;

        _extent = MIMath.getPointsExtent(_pointList);
    }

    /**
     * Determine if the polyline is closed
     * 
     * @return boolean
     */
    public boolean isClosed() {
        PointD sPoint = _pointList.get(0);
        PointD ePoint = _pointList.get(_pointList.size() - 1);
        if (MIMath.doubleEquals(sPoint.X, ePoint.X) && MIMath.doubleEquals(sPoint.Y, ePoint.Y)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get length
     * 
     * @return length
     */
    public double getLength() {
        double length = 0.0;
        double dx, dy;
        for (int i = 0; i < _pointList.size() - 1; i++) {
            dx = _pointList.get(i + 1).X - _pointList.get(i).X;
            dy = _pointList.get(i + 1).Y - _pointList.get(i).Y;
            length += Math.sqrt(dx * dx + dy * dy);
        }

        return length;
    }

    /**
     * Get lengths of each segment
     * 
     * @return length array
     */
    public double[] getLengths() {
        double[] lengths = new double[_pointList.size() - 1];
        double dx, dy;
        for (int i = 0; i < _pointList.size() - 1; i++) {
            dx = _pointList.get(i + 1).X - _pointList.get(i).X;
            dy = _pointList.get(i + 1).Y - _pointList.get(i).Y;
            lengths[i] = Math.sqrt(dx * dx + dy * dy);
        }

        return lengths;
    }

    /**
     * Get position list: x, y, angle
     * @param aLen segment length
     * @return  position list
     */
    public List<double[]> getPositions(double aLen) {
        double length = getLength();
        int n = (int) (length / aLen);
        if (n <= 0) {
            return null;
        }

        List<double[]> pos = new ArrayList<>();
        double x, y, angle;
        double[] lengths = getLengths();
        int idx = 0;
        double sLen = lengths[0];
        for (int i = 0; i < n; i++) {
            double len = aLen * (i + 1);
            for (int j = idx; j < lengths.length; j++) {
                if (sLen > len) {
                    idx = j;
                    break;
                }
                sLen += lengths[j + 1];
            }

            PointD aPoint = _pointList.get(idx);
            PointD bPoint = _pointList.get(idx + 1);
            x = aPoint.X + (bPoint.X - aPoint.X) * (lengths[idx] - (sLen - len)) / (lengths[idx]);
            y = aPoint.Y + (bPoint.Y - aPoint.Y) * (lengths[idx] - (sLen - len)) / (lengths[idx]);
            double U = bPoint.X - aPoint.X;
            double V = bPoint.Y - aPoint.Y;
            angle = Math.atan((V) / (U)) * 180 / Math.PI;
            if (U < 0) {
                angle += 180;
            }

            double[] data = new double[]{x, y, angle};
            pos.add(data);
        }

        return pos;
    }
    // </editor-fold>
}
