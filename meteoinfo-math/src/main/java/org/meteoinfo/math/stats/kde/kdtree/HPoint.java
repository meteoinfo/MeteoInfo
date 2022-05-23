/**
 * %SVN.HEADER%
 * 
 * based on work by Simon Levy
 * http://www.cs.wlu.edu/~levy/software/kd/
 */
package org.meteoinfo.math.stats.kde.kdtree;

import java.io.Serializable;

// Hyper-Point class supporting KDTree class

class HPoint implements Serializable{

    protected double[] coord;

    protected HPoint(int n) {
        coord = new double[n];
    }

    protected HPoint(double[] x) {

        coord = new double[x.length];
        for (int i = 0; i < x.length; ++i)
            coord[i] = x[i];
    }

    protected Object clone() {

        return new HPoint(coord);
    }

    protected boolean equals(HPoint p) {

        // seems faster than java.util.Arrays.equals(), which is not
        // currently supported by Matlab anyway
        for (int i = 0; i < coord.length; ++i)
            if (coord[i] != p.coord[i])
                return false;

        return true;
    }

    protected static double sqrdist(HPoint x, HPoint y) {

        double dist = 0;

        for (int i = 0; i < x.coord.length; ++i) {
            double diff = (x.coord[i] - y.coord[i]);
            dist += (diff * diff);
        }

        return dist;

    }

    protected static double eucdist(HPoint x, HPoint y) {

        return Math.sqrt(sqrdist(x, y));
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < coord.length; ++i) {
            s = s + coord[i] + " ";
        }
        return s;
    }

}
