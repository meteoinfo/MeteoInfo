/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math.distance;

/**
 *
 * @author Yaqiang Wang
 */
public class EuclideanDistanceJ {

    /**
     * The weights used in weighted distance.
     */
    private double[] weight = null;

    /**
     * Constructor. Standard (unweighted) Euclidean distance.
     */
    public EuclideanDistanceJ() {
    }

    /**
     * Constructor with a given weight vector.
     *
     * @param weight the weight vector.
     */
    public EuclideanDistanceJ(double[] weight) {
        for (int i = 0; i < weight.length; i++) {
            if (weight[i] < 0) {
                throw new IllegalArgumentException(String.format("Weight has to be nonnegative: %f", weight[i]));
            }
        }

        this.weight = weight;
    }

    @Override
    public String toString() {
        if (weight != null) {
            return "weighted Euclidean distance";
        } else {
            return "Euclidean distance";
        }
    }

    /**
     * Euclidean distance between the two arrays of type integer. No missing
     * value handling in this method.
     */
    public double d(int[] x, int[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException(String.format("Arrays have different length: x[%d], y[%d]", x.length, y.length));
        }

        double dist = 0.0;

        if (weight == null) {
            for (int i = 0; i < x.length; i++) {
                double d = x[i] - y[i];
                dist += d * d;
            }
        } else {
            if (x.length != weight.length) {
                throw new IllegalArgumentException(String.format("Input vectors and weight vector have different length: %d, %d", x.length, weight.length));
            }

            for (int i = 0; i < x.length; i++) {
                double d = x[i] - y[i];
                dist += weight[i] * d * d;
            }
        }

        return Math.sqrt(dist);
    }

    /**
     * Euclidean distance between the two arrays of type float. NaN will be
     * treated as missing values and will be excluded from the calculation. Let
     * m be the number nonmissing values, and n be the number of all values. The
     * returned distance is sqrt(n * d / m), where d is the square of distance
     * between nonmissing values.
     */
    public double d(float[] x, float[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException(String.format("Arrays have different length: x[%d], y[%d]", x.length, y.length));
        }

        int n = x.length;
        int m = 0;
        double dist = 0.0;

        if (weight == null) {
            for (int i = 0; i < n; i++) {
                if (!Float.isNaN(x[i]) && !Float.isNaN(y[i])) {
                    m++;
                    double d = x[i] - y[i];
                    dist += d * d;
                }
            }
        } else {
            if (x.length != weight.length) {
                throw new IllegalArgumentException(String.format("Input vectors and weight vector have different length: %d, %d", x.length, weight.length));
            }

            for (int i = 0; i < n; i++) {
                if (!Float.isNaN(x[i]) && !Float.isNaN(y[i])) {
                    m++;
                    double d = x[i] - y[i];
                    dist += weight[i] * d * d;
                }
            }
        }

        if (m == 0) {
            dist = Double.NaN;
        } else {
            dist = n * dist / m;
        }

        return Math.sqrt(dist);
    }

    /**
     * Euclidean distance between the two arrays of type double. NaN will be
     * treated as missing values and will be excluded from the calculation. Let
     * m be the number nonmissing values, and n be the number of all values. The
     * returned distance is sqrt(n * d / m), where d is the square of distance
     * between nonmissing values.
     */
    public double d(double[] x, double[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException(String.format("Arrays have different length: x[%d], y[%d]", x.length, y.length));
        }

        int n = x.length;
        int m = 0;
        double dist = 0.0;

        if (weight == null) {
            for (int i = 0; i < n; i++) {
                if (!Double.isNaN(x[i]) && !Double.isNaN(y[i])) {
                    m++;
                    double d = x[i] - y[i];
                    dist += d * d;
                }
            }
        } else {
            if (x.length != weight.length) {
                throw new IllegalArgumentException(String.format("Input vectors and weight vector have different length: %d, %d", x.length, weight.length));
            }

            for (int i = 0; i < n; i++) {
                if (!Double.isNaN(x[i]) && !Double.isNaN(y[i])) {
                    m++;
                    double d = x[i] - y[i];
                    dist += weight[i] * d * d;
                }
            }
        }

        if (m == 0) {
            dist = Double.NaN;
        } else {
            dist = n * dist / m;
        }

        return Math.sqrt(dist);
    }

    /**
     * eturns the proximity matrix of a dataset for given distance function.
     *
     * @param data Input data
     * @param half If true, only the lower half of matrix is allocated to save space.
     * @return Proximity maxtrix
     */
    public double[][] proximity(double[][] data, boolean half) {
        int n = data.length;
        double[][] proximity;
        if (half) {
            proximity = new double[n][];
            for (int i = 0; i < n; i++) {
                proximity[i] = new double[i + 1];
                for (int j = 0; j < i; j++) {
                    proximity[i][j] = this.d(data[i], data[j]);
                }
            }
        } else {
            proximity = new double[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < i; j++) {
                    proximity[i][j] = this.d(data[i], data[j]);
                    proximity[j][i] = proximity[i][j];
                }
            }
        }

        return proximity;
    }
}
