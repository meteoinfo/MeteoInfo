package org.meteoinfo.math.interpolate;

import org.meteoinfo.math.spatial.KDTree;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.InvalidRangeException;

import java.util.List;

public class IDWNDInterpolator extends NearestNDInterpolator{

    private double radius = Double.NaN;
    private int pointNum = Integer.MAX_VALUE;
    private int weightPower = 1;

    /**
     * Constructor
     * @param points Points coordinate arrays
     * @param values Points values array
     */
    public IDWNDInterpolator(List<Array> points, Array values) {
        super(points, values);
        this.pointNum = this.kdTree.size();
    }

    /**
     * Constructor
     * @param points Points coordinate array - 2D
     * @param values Points values array
     */
    public IDWNDInterpolator(Array points, Array values) {
        super(points, values);
        this.pointNum = this.kdTree.size();
    }

    /**
     * Set point search radius
     * @param value Radius
     */
    public void setRadius(double value) {
        this.radius = value;
    }

    /**
     * Set point number for interpolation
     * @param value Point number
     */
    public void setPointNum(int value) {
        this.pointNum = value;
    }

    /**
     * Set distance weight power
     * @param value Weight power
     */
    public void setWeightPower(int value) {
        this.weightPower = value;
    }

    /**
     * Interpolate to location
     * @param location The location
     * @return Interpolated value
     */
    public Array Interpolate(List<Array> location) {
        for (Array a : location) {
            a = a.copyIfView();
        }
        int n = location.size();
        int pNum = (int)location.get(0).getSize();
        Array r = Array.factory(this.dataType, location.get(0).getShape());
        boolean match;
        double v, w;
        if (Double.isNaN(this.radius)) {
            for (int i = 0; i < pNum; i++) {
                List<KDTree.SearchResult<Double>> srs = kdTree.nearestNeighbours(getCoordinate(location, n, i), pointNum);
                double v_sum = 0.0;
                double weight_sum = 0.0;
                match = false;
                for (KDTree.SearchResult sr : srs) {
                    v = (double) sr.payload;
                    if (sr.distance == 0) {
                        r.setDouble(i, v);
                        match = true;
                        break;
                    } else {
                        w = 1. / Math.pow(sr.distance, this.weightPower);
                        weight_sum += w;
                        v_sum += v * w;
                    }
                }
                if (!match) {
                    r.setDouble(i, v_sum / weight_sum);
                }
            }

            return r;
        } else {
            for (int i = 0; i < pNum; i++) {
                List<KDTree.SearchResult<Double>> srs = kdTree.ballSearch_distance(getCoordinate(location, n, i), radius * radius);
                if (srs == null || srs.size() < this.pointNum) {
                    r.setDouble(i, Double.NaN);
                } else {
                    double v_sum = 0.0;
                    double weight_sum = 0.0;
                    match = false;
                    for (KDTree.SearchResult sr : srs) {
                        v = (double) sr.payload;
                        if (sr.distance == 0) {
                            r.setDouble(i, v);
                            match = true;
                            break;
                        } else {
                            w = 1. / Math.pow(sr.distance, this.weightPower);
                            weight_sum += w;
                            v_sum += v * w;
                        }
                    }
                    if (!match) {
                        r.setDouble(i, v_sum / weight_sum);
                    }
                }
            }
        }
        return null;
    }

}
