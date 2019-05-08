/**
 * %SVN.HEADER%
 * 
 * based on work by Simon Levy
 * http://www.cs.wlu.edu/~levy/software/kd/
 */
package org.meteoinfo.math.stats.kde.kdtree;

import java.io.Serializable;
import java.util.Vector;

// K-D Tree node class

class KDNode implements Serializable {

    // these are seen by KDTree
    protected HPoint k;

    Object v;

    protected KDNode left, right;

    protected boolean deleted;

    // Method ins translated from 352.ins.c of Gonnet & Baeza-Yates
    protected static KDNode ins(HPoint key, Object val, KDNode t, int lev, int K)  {

        if (t == null) {
            t = new KDNode(key, val);
        }

        else if (key.equals(t.k)) {

            // "re-insert"
            if (t.deleted) {
                t.deleted = false;
                t.v = val;
            }

            // else {
            // throw new KeyDuplicateException();
            // }
        }

        else if (key.coord[lev] > t.k.coord[lev]) {
            t.right = ins(key, val, t.right, (lev + 1) % K, K);
        } else {
            t.left = ins(key, val, t.left, (lev + 1) % K, K);
        }

        return t;
    }

    // Method srch translated from 352.srch.c of Gonnet & Baeza-Yates
    protected static KDNode srch(HPoint key, KDNode t, int K) {

        for (int lev = 0; t != null; lev = (lev + 1) % K) {

            if (!t.deleted && key.equals(t.k)) {
                return t;
            } else if (key.coord[lev] > t.k.coord[lev]) {
                t = t.right;
            } else {
                t = t.left;
            }
        }

        return null;
    }

    // Method rsearch translated from 352.range.c of Gonnet & Baeza-Yates
    protected static void rsearch(HPoint lowk, HPoint uppk, KDNode t, int lev, int K, Vector<KDNode> v) {

        if (t == null)
            return;
        if (lowk.coord[lev] <= t.k.coord[lev]) {
            rsearch(lowk, uppk, t.left, (lev + 1) % K, K, v);
        }
        int j;
        for (j = 0; j < K && lowk.coord[j] <= t.k.coord[j] && uppk.coord[j] >= t.k.coord[j]; j++)
            ;
        if (j == K)
            v.add(t);
        if (uppk.coord[lev] > t.k.coord[lev]) {
            rsearch(lowk, uppk, t.right, (lev + 1) % K, K, v);
        }
    }

    // Method Nearest Neighbor from Andrew Moore's thesis. Numbered
    // comments are direct quotes from there. Step "SDL" is added to
    // make the algorithm work correctly. NearestNeighborList solution
    // courtesy of Bjoern Heckel.
    protected static void nnbr(KDNode kd, HPoint target, HRect hr, double max_dist_sqd, int lev, int K,
            NearestNeighborList nnl) {

        // 1. if kd is empty then set dist-sqd to infinity and exit.
        if (kd == null) {
            return;
        }

        // 2. s := split field of kd
        int s = lev % K;

        // 3. pivot := dom-elt field of kd
        HPoint pivot = kd.k;
        double pivot_to_target = HPoint.sqrdist(pivot, target);

        // 4. Cut hr into to sub-hyperrectangles left-hr and right-hr.
        // The cut plane is through pivot and perpendicular to the s
        // dimension.
        HRect left_hr = hr; // optimize by not cloning
        HRect right_hr = (HRect) hr.clone();
        left_hr.max.coord[s] = pivot.coord[s];
        right_hr.min.coord[s] = pivot.coord[s];

        // 5. target-in-left := target_s <= pivot_s
        boolean target_in_left = target.coord[s] < pivot.coord[s];

        KDNode nearer_kd;
        HRect nearer_hr;
        KDNode further_kd;
        HRect further_hr;

        // 6. if target-in-left then
        // 6.1. nearer-kd := left field of kd and nearer-hr := left-hr
        // 6.2. further-kd := right field of kd and further-hr := right-hr
        if (target_in_left) {
            nearer_kd = kd.left;
            nearer_hr = left_hr;
            further_kd = kd.right;
            further_hr = right_hr;
        }
        //
        // 7. if not target-in-left then
        // 7.1. nearer-kd := right field of kd and nearer-hr := right-hr
        // 7.2. further-kd := left field of kd and further-hr := left-hr
        else {
            nearer_kd = kd.right;
            nearer_hr = right_hr;
            further_kd = kd.left;
            further_hr = left_hr;
        }

        // 8. Recursively call Nearest Neighbor with paramters
        // (nearer-kd, target, nearer-hr, max-dist-sqd), storing the
        // results in nearest and dist-sqd
        nnbr(nearer_kd, target, nearer_hr, max_dist_sqd, lev + 1, K, nnl);

        KDNode nearest = (KDNode) nnl.getHighest();
        double dist_sqd;

        if (!nnl.isCapacityReached()) {
            dist_sqd = Double.MAX_VALUE;
        } else {
            dist_sqd = nnl.getMaxPriority();
        }

        // 9. max-dist-sqd := minimum of max-dist-sqd and dist-sqd
        max_dist_sqd = Math.min(max_dist_sqd, dist_sqd);

        // 10. A nearer point could only lie in further-kd if there were some
        // part of further-hr within distance sqrt(max-dist-sqd) of
        // target. If this is the case then
        HPoint closest = further_hr.closest(target);
        if (HPoint.eucdist(closest, target) < Math.sqrt(max_dist_sqd)) {

            // 10.1 if (pivot-target)^2 < dist-sqd then
            if (pivot_to_target < dist_sqd) {

                // 10.1.1 nearest := (pivot, range-elt field of kd)
                nearest = kd;

                // 10.1.2 dist-sqd = (pivot-target)^2
                dist_sqd = pivot_to_target;

                // add to nnl
                if (!kd.deleted) {
                    nnl.insert(kd, dist_sqd);
                }

                // 10.1.3 max-dist-sqd = dist-sqd
                // max_dist_sqd = dist_sqd;
                if (nnl.isCapacityReached()) {
                    max_dist_sqd = nnl.getMaxPriority();
                } else {
                    max_dist_sqd = Double.MAX_VALUE;
                }
            }

            // 10.2 Recursively call Nearest Neighbor with parameters
            // (further-kd, target, further-hr, max-dist_sqd),
            // storing results in temp-nearest and temp-dist-sqd
            nnbr(further_kd, target, further_hr, max_dist_sqd, lev + 1, K, nnl);
            KDNode temp_nearest = (KDNode) nnl.getHighest();
            double temp_dist_sqd = nnl.getMaxPriority();

            // 10.3 If tmp-dist-sqd < dist-sqd then
            if (temp_dist_sqd < dist_sqd) {

                // 10.3.1 nearest := temp_nearest and dist_sqd := temp_dist_sqd
                nearest = temp_nearest;
                dist_sqd = temp_dist_sqd;
            }
        }

        // SDL: otherwise, current point is nearest
        else if (pivot_to_target < max_dist_sqd) {
            nearest = kd;
            dist_sqd = pivot_to_target;
        }
    }

    // constructor is used only by class; other methods are static
    private KDNode(HPoint key, Object val) {

        k = key;
        v = val;
        left = null;
        right = null;
        deleted = false;
    }

    protected String toString(int depth) {
        String s = k + "  " + v + (deleted ? "*" : "");
        if (left != null) {
            s = s + "\n" + pad(depth) + "L " + left.toString(depth + 1);
        }
        if (right != null) {
            s = s + "\n" + pad(depth) + "R " + right.toString(depth + 1);
        }
        return s;
    }

    private static String pad(int n) {
        String s = "";
        for (int i = 0; i < n; ++i) {
            s += " ";
        }
        return s;
    }

    private static void hrcopy(HRect hr_src, HRect hr_dst) {
        hpcopy(hr_src.min, hr_dst.min);
        hpcopy(hr_src.max, hr_dst.max);
    }

    private static void hpcopy(HPoint hp_src, HPoint hp_dst) {
        for (int i = 0; i < hp_dst.coord.length; ++i) {
            hp_dst.coord[i] = hp_src.coord[i];
        }
    }
}