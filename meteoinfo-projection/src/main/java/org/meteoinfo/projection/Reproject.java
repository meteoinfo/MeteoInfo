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
package org.meteoinfo.projection;

import org.locationtech.proj4j.*;
import org.meteoinfo.common.Extent;
import org.meteoinfo.common.PointD;
import org.meteoinfo.common.ResampleMethods;
import org.meteoinfo.ndarray.*;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Yaqiang Wang
 */
public class Reproject {

    private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();

    /**
     * Reproject a point
     * @param x X
     * @param y Y
     * @param source Source projection info
     * @param dest Destination projection info
     * @return Projected point
     */
    public static PointD reprojectPoint(double x, double y, ProjectionInfo source, ProjectionInfo dest) {
        double[][] points = new double[1][];
        points[0] = new double[]{x, y};
        Reproject.reprojectPoints(points, source, dest, 0, points.length);
        PointD rPoint = new PointD(points[0][0], points[0][1]);
        
        return rPoint;
    }
    
    /**
     * Reproject a point
     * @param point The point
     * @param source Source projection info
     * @param dest Destination projection info
     * @return Projected point
     */
    public static PointD reprojectPoint(PointD point, ProjectionInfo source, ProjectionInfo dest) {
        return reprojectPoint(point.X, point.Y, source, dest);
    }
    
    /**
     * Reproject a point
     * @param points The points
     * @param source Source projection info
     * @param dest Destination projection info
     */
    public static void reprojectPoints(double[][] points, ProjectionInfo source, ProjectionInfo dest) {
        reprojectPoints(points, source, dest, 0, points.length);
    }
    
    /**
     * Reproject points
     *
     * @param points The points
     * @param source Source projection info
     * @param dest Destination projection info
     * @param startIndex Start index
     * @param numPoints Point number
     */
    public static void reprojectPoints(double[][] points, ProjectionInfo source, ProjectionInfo dest, int startIndex, int numPoints) {
        CoordinateTransform trans = ctFactory.createTransform(source.getCoordinateReferenceSystem(), dest.getCoordinateReferenceSystem());
        if (source.getProjectionName() == ProjectionNames.LongLat) {
            for (int i = startIndex; i < startIndex + numPoints; i++) {
                if (i >= points.length) {
                    break;
                }
                if (points[i][0] > 180.0) {
                    points[i][0] -= 360;
                } else if (points[i][0] < -180) {
                    points[i][0] += 360;
                }
            }
        }
        for (int i = startIndex; i < startIndex + numPoints; i++) {
            if (i >= points.length) {
                break;
            }
            ProjCoordinate p1 = new ProjCoordinate(points[i][0], points[i][1]);
            ProjCoordinate p2 = new ProjCoordinate();
            try {
                trans.transform(p1, p2);
                points[i][0] = p2.x;
                points[i][1] = p2.y;
            } catch (ProjectionException e) {
                points[i][0] = Double.NaN;
                points[i][1] = Double.NaN;
            }
        }
    }
    
    /**
     * Project grid data
     *
     * @param data Data array
     * @param xx X array
     * @param yy Y array
     * @param fromProj From projection
     * @param toProj To projection
     * @param method Resample method
     * @return Projected grid data
     * @throws InvalidRangeException
     */
    public static Object[] reproject(Array data, List<Number> xx, List<Number> yy, CoordinateReferenceSystem fromProj,
            CoordinateReferenceSystem toProj, ResampleMethods method) throws InvalidRangeException {
        return reproject(data, xx, yy, ProjectionInfo.factory(fromProj), ProjectionInfo.factory(toProj), method);
    }

    /**
     * Project grid data
     *
     * @param data Data array
     * @param xx X array
     * @param yy Y array
     * @param fromProj From projection
     * @param toProj To projection
     * @param method Resample method
     * @return Projected grid data
     * @throws InvalidRangeException
     */
    public static Object[] reproject(Array data, List<Number> xx, List<Number> yy, ProjectionInfo fromProj,
                                     ProjectionInfo toProj, ResampleMethods method) throws InvalidRangeException {
        Extent aExtent;
        int xnum = xx.size();
        int ynum = yy.size();
        aExtent = ProjectionUtil.getProjectionExtent(fromProj, toProj, xx, yy);

        double xDelt = (aExtent.maxX - aExtent.minX) / (xnum - 1);
        double yDelt = (aExtent.maxY - aExtent.minY) / (ynum - 1);
        int i;
        Array rx = Array.factory(DataType.DOUBLE, new int[]{xnum});
        Array ry = Array.factory(DataType.DOUBLE, new int[]{ynum});
        for (i = 0; i < xnum; i++) {
            rx.setDouble(i, aExtent.minX + i * xDelt);
        }

        for (i = 0; i < ynum; i++) {
            ry.setDouble(i, aExtent.minY + i * yDelt);
        }

        Array[] rr = ArrayUtil.meshgrid(rx, ry);

        Array r = reproject(data, xx, yy, rr[0], rr[1], fromProj, toProj, method);

        return new Object[]{r, rx, ry};
    }

    /**
     * Project grid data
     *
     * @param data Data array
     * @param xx X array
     * @param yy Y array
     * @param fromProj From projection
     * @param toProj To projection
     * @param method Resample method
     * @return Projected grid data
     * @throws InvalidRangeException
     */
    public static Object[] reproject(Array data, Array xx, Array yy, ProjectionInfo fromProj,
                                     ProjectionInfo toProj) throws InvalidRangeException {
        return reproject(data, xx, yy, fromProj, toProj, ResampleMethods.NearestNeighbor);
    }

    /**
     * Project grid data
     *
     * @param data Data array
     * @param xx X array
     * @param yy Y array
     * @param fromProj From projection
     * @param toProj To projection
     * @param method Resample method
     * @return Projected grid data
     * @throws InvalidRangeException
     */
    public static Object[] reproject(Array data, Array xx, Array yy, ProjectionInfo fromProj,
                                     ProjectionInfo toProj, ResampleMethods method) throws InvalidRangeException {
        xx = xx.copyIfView();
        yy = yy.copyIfView();

        Extent aExtent;
        int xnum = (int) xx.getSize();
        int ynum = (int) yy.getSize();
        aExtent = ProjectionUtil.getProjectionExtent(fromProj, toProj, xx, yy);

        double xDelt = (aExtent.maxX - aExtent.minX) / (xnum - 1);
        double yDelt = (aExtent.maxY - aExtent.minY) / (ynum - 1);
        int i;
        Array rx = Array.factory(DataType.DOUBLE, new int[]{xnum});
        Array ry = Array.factory(DataType.DOUBLE, new int[]{ynum});
        for (i = 0; i < xnum; i++) {
            rx.setDouble(i, aExtent.minX + i * xDelt);
        }

        for (i = 0; i < ynum; i++) {
            ry.setDouble(i, aExtent.minY + i * yDelt);
        }

        Array[] rr = ArrayUtil.meshgrid(rx, ry);

        Array r = reproject(data, xx, yy, rr[0], rr[1], fromProj, toProj, method);

        return new Object[]{r, rx, ry};
    }

    /**
     * Project grid data
     *
     * @param data Data array
     * @param xx X array
     * @param yy Y array
     * @param fromProj From projection
     * @param toProj To projection
     * @return Porjected grid data
     * @throws InvalidRangeException
     */
    public static Object[] reproject(Array data, List<Number> xx, List<Number> yy, ProjectionInfo fromProj,
            ProjectionInfo toProj) throws InvalidRangeException {
        return reproject(data, xx, yy, fromProj, toProj, ResampleMethods.NearestNeighbor);
    }

    /**
     * Reproject
     *
     * @param data Data array
     * @param x X array
     * @param y Y array
     * @param rx Result x array
     * @param ry Result y array
     * @param fromProj From projection
     * @param toProj To projection
     * @param fill_value Fill value
     * @param resampleMethod Resample method
     * @return Result arrays
     * @throws InvalidRangeException
     */
    public static Array reproject(Array data, List<Number> x, List<Number> y, Array rx, Array ry,
            ProjectionInfo fromProj, ProjectionInfo toProj, double fill_value, ResampleMethods resampleMethod) throws InvalidRangeException {
        int n = (int) rx.getSize();
        int[] dshape = data.getShape();
        int[] shape;
        if (rx.getRank() == 1) {
            shape = new int[1];
            shape[0] = rx.getShape()[0];
        } else {
            shape = new int[data.getRank()];
            for (int i = 0; i < shape.length; i++) {
                if (i == shape.length - 2) {
                    shape[i] = rx.getShape()[0];
                } else if (i == shape.length - 1) {
                    shape[i] = rx.getShape()[1];
                } else {
                    shape[i] = data.getShape()[i];
                }
            }
        }
        Array r = Array.factory(data.getDataType(), shape);

        double[][] points = new double[n][];
        for (int i = 0; i < n; i++) {
            points[i] = new double[]{rx.getDouble(i), ry.getDouble(i)};
        }
        if (!fromProj.equals(toProj)) {
            Reproject.reprojectPoints(points, toProj, fromProj, 0, points.length);
        }
        double xx, yy;
        if (resampleMethod == ResampleMethods.Bilinear) {
            if (shape.length <= 2) {
                for (int i = 0; i < n; i++) {
                    xx = points[i][0];
                    yy = points[i][1];
                    if (Double.isNaN(xx) || Double.isNaN(yy)) {
                        r.setObject(i, Double.NaN);
                    } else {
                        r.setObject(i, ArrayUtil.toStation(data, x, y, xx, yy, fill_value));
                    }
                }
            } else {
                Index indexr = r.getIndex();
                int[] current, cc = null;
                boolean isNew;
                Array ndata = null;
                int k;
                for (int i = 0; i < r.getSize(); i++) {
                    current = indexr.getCurrentCounter();
                    isNew = true;
                    if (i > 0) {
                        for (int j = 0; j < shape.length - 2; j++) {
                            if (cc[j] != current[j]) {
                                isNew = false;
                                break;
                            }
                        }
                    }
                    cc = Arrays.copyOf(current, current.length);
                    if (isNew) {
                        List<Range> ranges = new ArrayList<>();
                        for (int j = 0; j < shape.length - 2; j++) {
                            ranges.add(new Range(current[j], current[j], 1));
                        }
                        ranges.add(new Range(0, dshape[dshape.length - 2] - 1, 1));
                        ranges.add(new Range(0, dshape[dshape.length - 1] - 1, 1));
                        ndata = data.section(ranges).reduce();
                    }
                    k = current[shape.length - 2] * shape[shape.length - 1] + current[shape.length - 1];
                    xx = points[k][0];
                    yy = points[k][1];
                    if (Double.isNaN(xx) || Double.isNaN(yy)) {
                        r.setObject(i, Double.NaN);
                    } else {
                        r.setObject(i, ArrayUtil.toStation(ndata, x, y, xx, yy, fill_value));
                    }
                    indexr.incr();
                }
            }
        } else if (shape.length <= 2) {
            for (int i = 0; i < n; i++) {
                xx = points[i][0];
                yy = points[i][1];
                if (Double.isNaN(xx) || Double.isNaN(yy)) {
                    r.setObject(i, Double.NaN);
                } else {
                    r.setObject(i, ArrayUtil.toStation_Neighbor(data, x, y, xx, yy, fill_value));
                }
            }
        } else {
            Index indexr = r.getIndex();
            int[] current, cc = null;
            boolean isNew;
            Array ndata = null;
            int k;
            for (int i = 0; i < r.getSize(); i++) {
                current = indexr.getCurrentCounter();
                isNew = true;
                if (i > 0) {
                    for (int j = 0; j < shape.length - 2; j++) {
                        if (cc[j] != current[j]) {
                            isNew = false;
                            break;
                        }
                    }
                }
                cc = Arrays.copyOf(current, current.length);
                if (isNew) {
                    List<Range> ranges = new ArrayList<>();
                    for (int j = 0; j < shape.length - 2; j++) {
                        ranges.add(new Range(current[j], current[j], 1));
                    }
                    ranges.add(new Range(0, dshape[dshape.length - 2] - 1, 1));
                    ranges.add(new Range(0, dshape[dshape.length - 1] - 1, 1));
                    ndata = data.section(ranges).reduce();
                }
                k = current[shape.length - 2] * shape[shape.length - 1] + current[shape.length - 1];
                xx = points[k][0];
                yy = points[k][1];
                if (Double.isNaN(xx) || Double.isNaN(yy)) {
                    r.setObject(i, Double.NaN);
                } else {
                    r.setObject(i, ArrayUtil.toStation_Neighbor(ndata, x, y, xx, yy, fill_value));
                }
                indexr.incr();
            }
        }

        return r;
    }

    /**
     * Reproject
     *
     * @param data Data array
     * @param x X array
     * @param y Y array
     * @param rx Result x array
     * @param ry Result y array
     * @param fromProj From projection
     * @param toProj To projection
     * @param resampleMethod Resample method
     * @return Result arrays
     * @throws InvalidRangeException
     */
    public static Array reproject(Array data, List<Number> x, List<Number> y, Array rx, Array ry,
            ProjectionInfo fromProj, ProjectionInfo toProj, ResampleMethods resampleMethod) throws InvalidRangeException {
        int n = (int) rx.getSize();
        int[] dshape = data.getShape();
        int[] shape;
        if (rx.getRank() == 1) {
            shape = new int[1];
            shape[0] = rx.getShape()[0];
        } else {
            shape = new int[data.getRank()];
            for (int i = 0; i < shape.length; i++) {
                if (i == shape.length - 2) {
                    shape[i] = rx.getShape()[0];
                } else if (i == shape.length - 1) {
                    shape[i] = rx.getShape()[1];
                } else {
                    shape[i] = data.getShape()[i];
                }
            }
        }
        Array r = Array.factory(data.getDataType(), shape);

        double[][] points = new double[n][];
        for (int i = 0; i < n; i++) {
            points[i] = new double[]{rx.getDouble(i), ry.getDouble(i)};
        }
        if (!fromProj.equals(toProj)) {
            Reproject.reprojectPoints(points, toProj, fromProj, 0, points.length);
        }
        double xx, yy;
        if (resampleMethod == ResampleMethods.Bilinear) {
            if (shape.length <= 2) {
                for (int i = 0; i < n; i++) {
                    xx = points[i][0];
                    yy = points[i][1];
                    r.setObject(i, ArrayUtil.toStation(data, x, y, xx, yy));
                }
            } else {
                Index indexr = r.getIndex();
                int[] current, cc = null;
                boolean isNew;
                Array ndata = null;
                int k;
                for (int i = 0; i < r.getSize(); i++) {
                    current = indexr.getCurrentCounter();
                    isNew = true;
                    if (i > 0) {
                        for (int j = 0; j < shape.length - 2; j++) {
                            if (cc[j] != current[j]) {
                                isNew = false;
                                break;
                            }
                        }
                    }
                    cc = Arrays.copyOf(current, current.length);
                    if (isNew) {
                        List<Range> ranges = new ArrayList<>();
                        for (int j = 0; j < shape.length - 2; j++) {
                            ranges.add(new Range(current[j], current[j], 1));
                        }
                        ranges.add(new Range(0, dshape[dshape.length - 2] - 1, 1));
                        ranges.add(new Range(0, dshape[dshape.length - 1] - 1, 1));
                        ndata = data.section(ranges).reduce();
                    }
                    k = current[shape.length - 2] * shape[shape.length - 1] + current[shape.length - 1];
                    xx = points[k][0];
                    yy = points[k][1];
                    r.setObject(i, ArrayUtil.toStation(ndata, x, y, xx, yy));
                    indexr.incr();
                }
            }
        } else if (shape.length == 2) {
            for (int i = 0; i < n; i++) {
                xx = points[i][0];
                yy = points[i][1];
                r.setObject(i, ArrayUtil.toStation_Neighbor(data, x, y, xx, yy));
            }
        } else {
            Index indexr = r.getIndex();
            int[] current, cc = null;
            boolean isNew;
            Array ndata = null;
            int k;
            for (int i = 0; i < r.getSize(); i++) {
                current = indexr.getCurrentCounter();
                isNew = true;
                if (i > 0) {
                    for (int j = 0; j < shape.length - 2; j++) {
                        if (cc[j] != current[j]) {
                            isNew = false;
                            break;
                        }
                    }
                }
                cc = Arrays.copyOf(current, current.length);
                if (isNew) {
                    List<Range> ranges = new ArrayList<>();
                    for (int j = 0; j < shape.length - 2; j++) {
                        ranges.add(new Range(current[j], current[j], 1));
                    }
                    ranges.add(new Range(0, dshape[dshape.length - 2] - 1, 1));
                    ranges.add(new Range(0, dshape[dshape.length - 1] - 1, 1));
                    ndata = data.section(ranges).reduce();
                }
                k = current[shape.length - 2] * shape[shape.length - 1] + current[shape.length - 1];
                xx = points[k][0];
                yy = points[k][1];
                r.setObject(i, ArrayUtil.toStation_Neighbor(ndata, x, y, xx, yy));
                indexr.incr();
            }
        }

        return r;
    }

    /**
     * Reproject
     *
     * @param data Data array
     * @param x X array
     * @param y Y array
     * @param rx Result x array
     * @param ry Result y array
     * @param fromProj From projection
     * @param toProj To projection
     * @param resampleMethod Resample method
     * @return Result arrays
     * @throws InvalidRangeException
     */
    public static Array reproject(Array data, Array x, Array y, Array rx, Array ry,
                                  ProjectionInfo fromProj, ProjectionInfo toProj, ResampleMethods resampleMethod) throws InvalidRangeException {
        int n = (int) rx.getSize();
        int[] dshape = data.getShape();
        int[] shape;
        if (rx.getRank() == 1) {
            shape = new int[1];
            shape[0] = rx.getShape()[0];
        } else {
            shape = new int[data.getRank()];
            for (int i = 0; i < shape.length; i++) {
                if (i == shape.length - 2) {
                    shape[i] = rx.getShape()[0];
                } else if (i == shape.length - 1) {
                    shape[i] = rx.getShape()[1];
                } else {
                    shape[i] = data.getShape()[i];
                }
            }
        }
        Array r = Array.factory(data.getDataType(), shape);

        double[][] points = new double[n][];
        for (int i = 0; i < n; i++) {
            points[i] = new double[]{rx.getDouble(i), ry.getDouble(i)};
        }
        if (!fromProj.equals(toProj)) {
            Reproject.reprojectPoints(points, toProj, fromProj, 0, points.length);
        }
        double xx, yy;
        if (resampleMethod == ResampleMethods.Bilinear) {
            if (shape.length <= 2) {
                for (int i = 0; i < n; i++) {
                    xx = points[i][0];
                    yy = points[i][1];
                    r.setObject(i, ArrayUtil.toStation(data, x, y, xx, yy));
                }
            } else {
                Index indexr = r.getIndex();
                int[] current, cc = null;
                boolean isNew;
                Array ndata = null;
                int k;
                for (int i = 0; i < r.getSize(); i++) {
                    current = indexr.getCurrentCounter();
                    isNew = true;
                    if (i > 0) {
                        for (int j = 0; j < shape.length - 2; j++) {
                            if (cc[j] != current[j]) {
                                isNew = false;
                                break;
                            }
                        }
                    }
                    cc = Arrays.copyOf(current, current.length);
                    if (isNew) {
                        List<Range> ranges = new ArrayList<>();
                        for (int j = 0; j < shape.length - 2; j++) {
                            ranges.add(new Range(current[j], current[j], 1));
                        }
                        ranges.add(new Range(0, dshape[dshape.length - 2] - 1, 1));
                        ranges.add(new Range(0, dshape[dshape.length - 1] - 1, 1));
                        ndata = data.section(ranges).reduce();
                    }
                    k = current[shape.length - 2] * shape[shape.length - 1] + current[shape.length - 1];
                    xx = points[k][0];
                    yy = points[k][1];
                    r.setObject(i, ArrayUtil.toStation(ndata, x, y, xx, yy));
                    indexr.incr();
                }
            }
        } else if (shape.length == 2) {
            for (int i = 0; i < n; i++) {
                xx = points[i][0];
                yy = points[i][1];
                r.setObject(i, ArrayUtil.toStation_Neighbor(data, x, y, xx, yy));
            }
        } else {
            Index indexr = r.getIndex();
            int[] current, cc = null;
            boolean isNew;
            Array ndata = null;
            int k;
            for (int i = 0; i < r.getSize(); i++) {
                current = indexr.getCurrentCounter();
                isNew = true;
                if (i > 0) {
                    for (int j = 0; j < shape.length - 2; j++) {
                        if (cc[j] != current[j]) {
                            isNew = false;
                            break;
                        }
                    }
                }
                cc = Arrays.copyOf(current, current.length);
                if (isNew) {
                    List<Range> ranges = new ArrayList<>();
                    for (int j = 0; j < shape.length - 2; j++) {
                        ranges.add(new Range(current[j], current[j], 1));
                    }
                    ranges.add(new Range(0, dshape[dshape.length - 2] - 1, 1));
                    ranges.add(new Range(0, dshape[dshape.length - 1] - 1, 1));
                    ndata = data.section(ranges).reduce();
                }
                k = current[shape.length - 2] * shape[shape.length - 1] + current[shape.length - 1];
                xx = points[k][0];
                yy = points[k][1];
                r.setObject(i, ArrayUtil.toStation_Neighbor(ndata, x, y, xx, yy));
                indexr.incr();
            }
        }

        return r;
    }

    /**
     * Reproject
     *
     * @param data Data array
     * @param x X array
     * @param y Y array
     * @param rx Result x array
     * @param ry Result y array
     * @param fromProj From projection
     * @param toProj To projection
     * @param fill_value Fill value
     * @param resampleMethod Resample method
     * @return Result arrays
     */
    public static Array reproject(Array data, List<Number> x, List<Number> y, List<Number> rx, List<Number> ry,
            ProjectionInfo fromProj, ProjectionInfo toProj, double fill_value, ResampleMethods resampleMethod) {
        int n = rx.size() * ry.size();
        int[] shape = new int[]{ry.size(), rx.size()};
        Array r = Array.factory(data.getDataType(), shape);

        double[][] points = new double[n][];
        for (int i = 0; i < ry.size(); i++) {
            for (int j = 0; j < rx.size(); j++) {
                points[i * rx.size() + j] = new double[]{rx.get(j).doubleValue(), ry.get(i).doubleValue()};
            }
        }
        if (!fromProj.equals(toProj)) {
            Reproject.reprojectPoints(points, toProj, fromProj, 0, points.length);
        }
        double xx, yy;
        if (resampleMethod == ResampleMethods.Bilinear) {
            for (int i = 0; i < n; i++) {
                xx = points[i][0];
                yy = points[i][1];
                r.setObject(i, ArrayUtil.toStation(data, x, y, xx, yy, fill_value));
            }
        } else {
            for (int i = 0; i < n; i++) {
                xx = points[i][0];
                yy = points[i][1];
                r.setObject(i, ArrayUtil.toStation_Neighbor(data, x, y, xx, yy, fill_value));
            }
        }

        return r;
    }
    
    /**
     * Reproject
     *
     * @param x X array
     * @param y Y array
     * @param fromProj From projection
     * @param toProj To projection
     * @return Result arrays
     */
    public static Array[] reproject(Array x, Array y, ProjectionInfo fromProj, ProjectionInfo toProj) {
        Array rx = Array.factory(DataType.DOUBLE, x.getShape());
        Array ry = Array.factory(DataType.DOUBLE, x.getShape());
        int n = (int) x.getSize();
        double[][] points = new double[n][];
        IndexIterator iterX = x.getIndexIterator();
        IndexIterator iterY = y.getIndexIterator();
        for (int i = 0; i < n; i++) {
            points[i] = new double[]{iterX.getDoubleNext(), iterY.getDoubleNext()};
        }
        Reproject.reprojectPoints(points, fromProj, toProj, 0, points.length);
        for (int i = 0; i < n; i++) {
            rx.setDouble(i, points[i][0]);
            ry.setDouble(i, points[i][1]);
        }

        return new Array[]{rx, ry};
    }

    /**
     * Reproject image data array
     *
     * @param data Data array of image - 3D, [ny,nx,3]
     * @param x X array
     * @param y Y array
     * @param rx Result x array
     * @param ry Result y array
     * @param fromProj From projection
     * @param toProj To projection
     * @return Result arrays
     * @throws InvalidRangeException
     */
    public static Array reprojectImage(Array data, Array x, Array y, Array rx, Array ry,
                                  ProjectionInfo fromProj, ProjectionInfo toProj) throws InvalidRangeException {
        data = data.copyIfView();
        x = x.copyIfView();
        y = y.copyIfView();
        rx = rx.copyIfView();
        ry = ry.copyIfView();

        int[] shape = rx.getShape();
        int ny = shape[0];
        int nx = shape[1];
        int[] newShape;
        if (data.getRank() == 2) {
            newShape = new int[]{ny, nx};
        } else {
            newShape = new int[]{ny, nx, data.getShape()[2]};
        }
        Array r = Array.factory(data.getDataType(), newShape);

        int n = ny * nx;
        double[][] points = new double[n][];
        for (int i = 0; i < n; i++) {
            points[i] = new double[]{rx.getDouble(i), ry.getDouble(i)};
        }
        if (!fromProj.equals(toProj)) {
            Reproject.reprojectPoints(points, toProj, fromProj, 0, points.length);
        }

        double minX = x.getDouble(0);
        double maxX = x.getDouble((int) x.getSize() - 1);
        double minY = y.getDouble(0);
        double maxY = y.getDouble((int) y.getSize() - 1);
        double dx = (maxX - minX) / ((int) x.getSize() - 1);
        double dy = (maxY - minY) / ((int) y.getSize() - 1);
        double xx, yy;
        int xi, yi, ii;
        int idx = 0;
        if (data.getRank() == 2) {
            Index2D index = (Index2D) data.getIndex();
            for (int i = 0; i < ny; i++) {
                for (int j = 0; j < nx; j++) {
                    ii = i * nx + j;
                    xx = points[ii][0];
                    yy = points[ii][1];
                    if (xx < minX || xx > maxX)
                        xi = -1;
                    else
                        xi = (int) ((xx - minX) / dx);
                    if (yy < minY || yy > maxY)
                        yi = -1;
                    else
                        yi = (int) ((yy - minY) / dy);

                    if (xi >= 0 && yi >= 0) {
                        index.set(yi, xi);
                        r.setObject(ii, data.getObject(index));
                    } else {
                        r.setObject(ii, Double.NaN);
                    }
                }
            }
        } else {
            Index3D index = (Index3D) data.getIndex();
            for (int i = 0; i < ny; i++) {
                for (int j = 0; j < nx; j++) {
                    ii = i * nx + j;
                    xx = points[ii][0];
                    yy = points[ii][1];
                    if (xx < minX || xx > maxX)
                        xi = -1;
                    else
                        xi = (int) ((xx - minX) / dx);
                    if (yy < minY || yy > maxY)
                        yi = -1;
                    else
                        yi = (int) ((yy - minY) / dy);

                    if (xi >= 0 && yi >= 0) {
                        index.set(yi, xi, 0);
                        r.setObject(idx, data.getObject(index));
                        idx += 1;
                        index.set2(1);
                        r.setObject(idx, data.getObject(index));
                        idx += 1;
                        index.set2(2);
                        r.setObject(idx, data.getObject(index));
                        idx += 1;
                    } else {
                        r.setObject(idx, 255);
                        idx += 1;
                        r.setObject(idx, 255);
                        idx += 1;
                        r.setObject(idx, 255);
                        idx += 1;
                    }
                }
            }
        }

        return r;
    }

    /**
     * Reproject image data array
     *
     * @param data Data array of image - 3D, [ny,nx,3]
     * @param x X array
     * @param y Y array
     * @param rx Result x array
     * @param ry Result y array
     * @param fromProj From projection
     * @param toProj To projection
     * @return Result arrays
     * @throws InvalidRangeException
     */
    public static Object[] reprojectImage(Array data, Array x, Array y, ProjectionInfo fromProj,
                                       ProjectionInfo toProj) throws InvalidRangeException {
        Extent extent = ProjectionUtil.getProjectionExtent(fromProj, toProj, x, y);

        int nx = (int) x.getSize();
        int ny = (int) y.getSize();
        double dx = (extent.maxX - extent.minX) / (nx - 1);
        double dy = (extent.maxY - extent.minY) / (ny - 1);
        int i;
        Array rx = Array.factory(DataType.DOUBLE, new int[]{nx});
        Array ry = Array.factory(DataType.DOUBLE, new int[]{ny});
        for (i = 0; i < nx; i++) {
            rx.setDouble(i, extent.minX + i * dx);
        }

        for (i = 0; i < ny; i++) {
            ry.setDouble(i, extent.minY + i * dy);
        }

        Array[] rr = ArrayUtil.meshgrid(rx, ry);

        Array r = reprojectImage(data, x, y, rr[0], rr[1], fromProj, toProj);

        return new Object[]{r, rx, ry};
    }
}
