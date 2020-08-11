package org.meteoinfo.math.interpolate;

import org.meteoinfo.math.spatial.KDTree;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.InvalidRangeException;

import java.util.List;

public class NearestNDInterpolator {
    private KDTree.Euclidean<Double> kdTree;
    private DataType dataType;

    /**
     * Constructor
     * @param points Points coordinate arrays
     * @param values Points values array
     */
    public NearestNDInterpolator(List<Array> points, Array values) {
        int n = points.size();
        this.kdTree = new KDTree.Euclidean<>(n);
        Array x = points.get(0);
        Array y = points.get(1);
        x = x.copyIfView();
        y = y.copyIfView();
        values = values.copyIfView();
        this.dataType = values.getDataType();
        int pNum = (int) x.getSize();
        if (n == 2) {
            for (int i = 0; i < pNum; i++) {
                kdTree.addPoint(new double[]{x.getDouble(i), y.getDouble(i)}, values.getDouble(i));
            }
        } else {
            Array z = points.get(2);
            z = z.copyIfView();
            for (int i = 0; i < pNum; i++) {
                kdTree.addPoint(new double[]{x.getDouble(i), y.getDouble(i), z.getDouble(i)}, values.getDouble(i));
            }
        }
    }

    /**
     * Constructor
     * @param points Points coordinate array - 2D
     * @param values Points values array
     */
    public NearestNDInterpolator(Array points, Array values) throws InvalidRangeException {
        points = points.copyIfView();
        int[] shape = points.getShape();
        int n = shape[0];
        int pNum = shape[1];
        this.kdTree = new KDTree.Euclidean<>(n);
        Array x = points.section(new int[]{0, 0}, new int[]{1, pNum});
        Array y = points.section(new int[]{1, 0}, new int[]{1, pNum});
        x = x.copyIfView();
        y = y.copyIfView();
        values = values.copyIfView();
        this.dataType = values.getDataType();
        if (n == 2) {
            for (int i = 0; i < pNum; i++) {
                kdTree.addPoint(new double[]{x.getDouble(i), y.getDouble(i)}, values.getDouble(i));
            }
        } else {
            Array z = points.section(new int[]{2, 0}, new int[]{1, pNum});
            z = z.copyIfView();
            for (int i = 0; i < pNum; i++) {
                kdTree.addPoint(new double[]{x.getDouble(i), y.getDouble(i), z.getDouble(i)}, values.getDouble(i));
            }
        }
    }

    /**
     * Get nearest value
     * @param location The search location
     * @return Nearest value
     */
    public Object nearest(double[] location) {
        KDTree.SearchResult r = this.kdTree.nearestNeighbours(location, 1).get(0);
        return r.payload;
    }

    /**
     * Get nearest values
     * @param location The search locations
     * @return Nearest values
     */
    public Array nearest(List<Array> location) {
        Array x = location.get(0);
        Array y = location.get(1);
        x = x.copyIfView();
        y = y.copyIfView();
        Array r = Array.factory(this.dataType, x.getShape());
        if (this.kdTree.dimensions() == 2) {
            for (int i = 0; i < x.getSize(); i++) {
                r.setObject(i, nearest(new double[]{x.getDouble(i), y.getDouble(i)}));
            }
        } else {
            Array z = location.get(2);
            z = z.copyIfView();
            for (int i = 0; i < x.getSize(); i++) {
                r.setObject(i, nearest(new double[]{x.getDouble(i), y.getDouble(i), z.getDouble(i)}));
            }
        }

        return r;
    }
}
