package org.meteoinfo.math.interpolate;

import org.meteoinfo.math.spatial.KDTree;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;

import java.util.ArrayList;
import java.util.List;

public class NearestNDInterpolator {
    protected KDTree.Euclidean<Double> kdTree;
    protected DataType dataType;
    protected boolean excludeNaN = true;

    /**
     * Constructor
     * @param points Points coordinate arrays
     * @param values Points values array
     */
    public NearestNDInterpolator(List<Array> points, Array values) {
        this(points, values, true);
    }

    /**
     * Constructor
     * @param points Points coordinate arrays
     * @param values Points values array
     * @param excludeNaN If exclude NaN values
     */
    public NearestNDInterpolator(List<Array> points, Array values, boolean excludeNaN) {
        this.excludeNaN = excludeNaN;

        int n = points.size();
        this.kdTree = new KDTree.Euclidean<>(n);
        for (Array a : points) {
            a = a.copyIfView();
        }
        values = values.copyIfView();
        this.dataType = values.getDataType();
        int pNum = (int) points.get(0).getSize();
        if (excludeNaN) {
            double v;
            for (int i = 0; i < pNum; i++) {
                v = values.getDouble(i);
                if (!Double.isNaN(v)) {
                    kdTree.addPoint(getCoordinate(points, n, i), v);
                }
            }
        } else {
            for (int i = 0; i < pNum; i++) {
                kdTree.addPoint(getCoordinate(points, n, i), values.getDouble(i));
            }
        }
    }

    /**
     * Constructor
     * @param points Points coordinate arrays
     * @param values Points values array
     */
    public NearestNDInterpolator(Array points, Array values) {
        this(points, values, true);
    }

    /**
     * Constructor
     * @param points Points coordinate array - 2D
     * @param values Points values array
     * @param excludeNaN If exclude NaN values
     */
    public NearestNDInterpolator(Array points, Array values, boolean excludeNaN) {
        this.excludeNaN = excludeNaN;

        points = points.copyIfView();
        int[] shape = points.getShape();
        int n = shape[0];
        int pNum = shape[1];
        this.kdTree = new KDTree.Euclidean<>(n);
        values = values.copyIfView();
        this.dataType = values.getDataType();
        if (excludeNaN) {
            double v;
            for (int i = 0; i < pNum; i++) {
                v = values.getDouble(i);
                if (!Double.isNaN(v)) {
                    kdTree.addPoint(getCoordinate(points, n, pNum, i), v);
                }
            }
        } else {
            for (int i = 0; i < pNum; i++) {
                kdTree.addPoint(getCoordinate(points, n, pNum, i), values.getDouble(i));
            }
        }
    }

    protected double[] getCoordinate(List<Array> points, int n, int idx) {
        double[] coord = new double[n];
        for (int i = 0; i < n; i++) {
            coord[i] = points.get(i).getDouble(idx);
        }

        return coord;
    }

    protected double[] getCoordinate(Array points, int nRow, int nCol, int idx) {
        double[] coord = new double[nRow];
        for (int i = 0; i < nRow; i++) {
            coord[i] = points.getDouble(i * nCol + idx);
        }

        return coord;
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
     * Get nearest value
     * @param location The search location
     * @param K Nearest points number
     * @return Nearest points list
     */
    public List<KDTree.SearchResult<Double>> nearest(double[] location, int K) {
        List<KDTree.SearchResult<Double>> r = this.kdTree.nearestNeighbours(location, K);
        return r;
    }

    /**
     * Get nearest values
     * @param location The search locations
     * @return Nearest values
     */
    public Array nearest(List<Array> location) {
        for (Array a : location) {
            a = a.copyIfView();
        }
        int n = location.size();
        int pNum = (int)location.get(0).getSize();
        Array r = Array.factory(this.dataType, location.get(0).getShape());

        for (int i = 0; i < pNum; i++) {
            r.setObject(i, nearest(getCoordinate(location, n, i)));
        }

        return r;
    }

    /**
     * Get nearest values
     * @param location The search locations
     * @param nThreads Number of threads
     * @return Nearest values
     */
    public Array nearest(List<Array> location, int nThreads) {
        for (Array a : location) {
            a = a.copyIfView();
        }
        int n = location.size();
        int pNum = (int)location.get(0).getSize();
        Array r = Array.factory(this.dataType, location.get(0).getShape());

        int segment = pNum / nThreads;
        int remainder = pNum % nThreads;
        int offset = 0;
        int segEnd;
        ArrayList<Thread> threads = new ArrayList<>();
        for (int ti = 0; ti < nThreads; ti++) {
            // Distribute remainder among first (remainder) threads
            int segmentSize = (remainder-- > 0) ? segment + 1 : segment;
            segEnd = offset + segmentSize;

            //Start the thread
            int finalSegEnd = segEnd;
            int finalOffset = offset;
            Thread t = new Thread() {
                public void run() {
                    for (int i = finalOffset; i < finalSegEnd; i++) {
                        r.setObject(i, nearest(getCoordinate(location, n, i)));
                    }
                }
            };

            threads.add(t);
            t.start();

            offset += segmentSize;
        }

        // Join the threads
        for (int i = 0; i < threads.size(); i++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return r;
    }
}
