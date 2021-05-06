package org.meteoinfo.math.spatial.distance;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.InvalidRangeException;
import smile.math.distance.*;

public class DistanceUtil {

    /**
     * Get distance function
     * @param metric The metric
     * @return Distance function
     */
    public static Distance getDistanceFunc(String metric) {
        switch (metric.toLowerCase()) {
            case "chebyshev":
                return new ChebyshevDistance();
            case "cityblock":
                return new ManhattanDistance();
            case "correlation":
                return new CorrelationDistance();
            case "euclidean":
                return new EuclideanDistance();
            case "hamming":
                return new HammingDistance();
            case "jaccard":
                return new JaccardDistance();
            case "jensenshannon":
                return new JensenShannonDistance();
        }

        return null;
    }

    /**
     * Get distance function
     * @param metric The metric
     * @param p P
     * @return Distance function
     */
    public static Distance getDistanceFunc(String metric, int p) {
        switch (metric.toLowerCase()) {
            case "minkowski":
                return new MinkowskiDistance(p);
        }

        return null;
    }

    /**
     * Calculate distance
     * @param distance The distance function
     * @param data Data array
     * @return Distance array
     */
    public static Array calculateDistance(Distance distance, Array data) throws InvalidRangeException {
        int[] shape = data.getShape();
        int m = shape[0];
        int n = shape[1];

        Array xa, ya;
        double[] x, y;
        double dist;
        Array r = Array.factory(DataType.DOUBLE, new int[]{m * (m - 1) / 2});
        int idx = 0;
        for (int i = 0; i < m - 1; i++) {
            xa = data.section(new int[]{i, 0}, new int[]{1, n});
            x = (double[]) xa.get1DJavaArray(double.class);
            for (int j = i + 1; j < m; j++) {
                ya = data.section(new int[]{j, 0}, new int[]{1, n});
                y = (double[]) ya.get1DJavaArray(double.class);
                dist = distance.apply(x, y);
                r.setDouble(idx, dist);
                idx += 1;
            }
        }

        return r;
    }
}
