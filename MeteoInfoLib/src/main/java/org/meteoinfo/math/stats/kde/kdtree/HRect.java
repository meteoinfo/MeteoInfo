/**
 * %SVN.HEADER%
 * 
 * based on work by Simon Levy
 * http://www.cs.wlu.edu/~levy/software/kd/
 */
package org.meteoinfo.math.stats.kde.kdtree;

// Hyper-Rectangle class supporting KDTree class


class HRect {

    protected HPoint min;
    protected HPoint max;

    protected HRect(int ndims) {
	min = new HPoint(ndims);
	max = new HPoint(ndims);
    }

    protected HRect(HPoint vmin, HPoint vmax) {

	min = (HPoint)vmin.clone();
	max = (HPoint)vmax.clone();
    }

    protected Object clone() {
	
	return new HRect(min, max);
    }

    // from Moore's eqn. 6.6
    protected HPoint closest(HPoint t) {

	HPoint p = new HPoint(t.coord.length);

	for (int i=0; i<t.coord.length; ++i) {
           if (t.coord[i]<=min.coord[i]) {
               p.coord[i] = min.coord[i];
           }
           else if (t.coord[i]>=max.coord[i]) {
               p.coord[i] = max.coord[i];
           }
           else {
               p.coord[i] = t.coord[i];
           }
	}
	
	return p;
    }

    // used in initial conditions of KDTree.nearest()
    protected static HRect infiniteHRect(int d) {
	
	HPoint vmin = new HPoint(d);
	HPoint vmax = new HPoint(d);
	
	for (int i=0; i<d; ++i) {
	    vmin.coord[i] = Double.NEGATIVE_INFINITY;
	    vmax.coord[i] = Double.POSITIVE_INFINITY;
	}

	return new HRect(vmin, vmax);
    }

    // currently unused
    protected HRect intersection(HRect r) {

	HPoint newmin = new HPoint(min.coord.length);
	HPoint newmax = new HPoint(min.coord.length);

	for (int i=0; i<min.coord.length; ++i) {
	    newmin.coord[i] = Math.max(min.coord[i], r.min.coord[i]);
	    newmax.coord[i] = Math.min(max.coord[i], r.max.coord[i]);
	    if (newmin.coord[i] >= newmax.coord[i]) return null;
	}

	return new HRect(newmin, newmax);
    }

    // currently unused
    protected double area () {

	double a = 1;

	for (int i=0; i<min.coord.length; ++i) {
	    a *= (max.coord[i] - min.coord[i]);
	}

	return a;
    }

    public String toString() {
	return min + "\n" + max + "\n";
    }
}
