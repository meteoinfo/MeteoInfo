package org.meteoinfo.math.optimize;

import org.bytedeco.javacpp.DoublePointer;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.math.ArrayUtil;

import static org.bytedeco.openblas.global.openblas.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Linear programming solver with an interface aligned to SciPy's linprog.
 * Internally uses a two‑phase simplex method accelerated by OpenBLAS.
 */
public class LinearProgram {

    private String method = "simplex";
    private final double[] c;          // minimization coefficients
    private final double[][] A_ub;     // inequality constraints (<=)
    private final double[] b_ub;
    private final double[][] A_eq;     // equality constraints (==)
    private final double[] b_eq;
    private final double[][] bounds;   // variable bounds: [lower, upper]

    /** Result object similar to SciPy's OptimizeResult. */
    public static class Result {
        public boolean success;
        public String message;
        public double[] x;
        public double fun;      // optimal value of the minimization objective
    }

    public LinearProgram(double[] c,
                         double[][] A_ub, double[] b_ub,
                         double[][] A_eq, double[] b_eq,
                         double[][] bounds) {
        this.c = c.clone();
        this.A_ub = (A_ub != null) ? A_ub : new double[0][];
        this.b_ub = (b_ub != null) ? b_ub : new double[0];
        this.A_eq = (A_eq != null) ? A_eq : new double[0][];
        this.b_eq = (b_eq != null) ? b_eq : new double[0];
        this.bounds = bounds;
    }

    public LinearProgram(double[] c,
                         double[][] A_ub, double[] b_ub,
                         double[][] A_eq, double[] b_eq,
                         double[][] bounds, String method) {
        this.c = c.clone();
        this.A_ub = (A_ub != null) ? A_ub : new double[0][];
        this.b_ub = (b_ub != null) ? b_ub : new double[0];
        this.A_eq = (A_eq != null) ? A_eq : new double[0][];
        this.b_eq = (b_eq != null) ? b_eq : new double[0];
        this.bounds = bounds;
        this.method = method;
    }

    public LinearProgram(Array ca, Array A_uba, Array b_uba, Array A_eqa, Array b_eqa, List<List<Double>> bounds,
                         String method) {
        this((double[]) ca.get1DJavaArray(double.class),
            A_uba == null ? null : (double[][]) ArrayUtil.copyToNDJavaArray_Double(A_uba),
            b_uba == null ? null : (double[]) b_uba.get1DJavaArray(double.class),
            A_eqa == null ? null : (double[][]) ArrayUtil.copyToNDJavaArray_Double(A_eqa),
            b_eqa == null ? null : (double[]) b_eqa.get1DJavaArray(double.class),
            (double[][]) bounds.stream().map(innerList -> innerList.stream()
                    .mapToDouble(Double::doubleValue)
                    .toArray())
                    .toArray(double[][]::new),
            method);
    }

    public Result solve() {
        try {
            // Convert to standard
            StandardForm sf = convertToStandard();

            // Solve with a solver
            LinProgSolver solver = LinProgSolver.factory(sf.c, sf.A, sf.b, method);
            double[] y = solver.solve();
            double obj = solver.getObjectiveValue();

            // Fix sign for simplex (returns maximization value)
            if ("simplex".equalsIgnoreCase(method)) obj = -obj;

            // Map back
            double[] x = new double[c.length];
            for (int j = 0; j < sf.nOrig; j++) {
                int origIdx = sf.origIdx[j];
                if (origIdx >= 0) {
                    x[origIdx] = y[j] + (sf.shift != null ? sf.shift[origIdx] : 0);
                }
            }

            // Add back the constant term from variable shifting
            double constant = 0.0;
            for (int j = 0; j < sf.nOrig; j++) {
                constant += c[j] * sf.shift[j];
            }
            obj += constant;

            Result res = new Result();
            res.success = true;
            res.message = "Optimization terminated successfully.";
            res.x = x;
            res.fun = obj;
            return res;
        } catch (Exception e) {
            Result res = new Result();
            res.success = false;
            res.message = e.getMessage();
            res.x = null;
            res.fun = Double.NaN;
            return res;
        }
    }

    /**
     * Converts user‑provided problem to standard form:
     *    minimize    c_std^T y
     *    subject to  A_std y = b_std,  y >= 0.
     *
     * Lower bounds are shifted to zero via y_j = x_j - lb_j.
     * Upper bounds become extra equality rows with a slack variable.
     * Inequalities become equalities by adding slack (+1) or surplus (-1) variables.
     * Equalities are kept as they are (after possible RHS sign flip to ensure b_std >= 0).
     */
    private StandardForm convertToStandard() {
        int nOrig = c.length;
        double[] shift = new double[nOrig];
        List<double[]> rows = new ArrayList<>();
        List<Double> rhsList = new ArrayList<>();
        List<Integer> rowTypes = new ArrayList<>(); // 1 = slack, -1 = surplus, 0 = equality

        // Process bounds and upper bounds
        for (int j = 0; j < nOrig; j++) {
            double lb = (bounds != null && j < bounds.length) ? bounds[j][0] : 0.0;
            double ub = (bounds != null && j < bounds.length) ? bounds[j][1] : Double.POSITIVE_INFINITY;
            if (Double.isInfinite(lb)) throw new UnsupportedOperationException("Free variables not supported.");
            if (Math.abs(lb) > 1e-12) {
                shift[j] = lb;
                ub -= lb;
            }
            // Upper bound constraint: y_j <= ub => y_j + s = ub
            if (ub < Double.POSITIVE_INFINITY) {
                double[] row = new double[nOrig];
                row[j] = 1.0;
                rows.add(row);
                rhsList.add(ub);
                rowTypes.add(1); // slack
            }
        }

        // Inequalities
        if (A_ub != null) {
            for (int i = 0; i < A_ub.length; i++) {
                double[] row = new double[nOrig];
                double rhs = b_ub[i];
                for (int j = 0; j < nOrig; j++) {
                    row[j] = A_ub[i][j];
                    rhs -= A_ub[i][j] * shift[j];
                }
                if (rhs >= -1e-12) {
                    rows.add(row); rhsList.add(rhs); rowTypes.add(1); // slack
                } else {
                    for (int j = 0; j < nOrig; j++) row[j] = -row[j];
                    rhs = -rhs;
                    rows.add(row); rhsList.add(rhs); rowTypes.add(-1); // surplus
                }
            }
        }

        // Equalities
        if (A_eq != null) {
            for (int i = 0; i < A_eq.length; i++) {
                double[] row = new double[nOrig];
                double rhs = b_eq[i];
                for (int j = 0; j < nOrig; j++) {
                    row[j] = A_eq[i][j];
                    rhs -= A_eq[i][j] * shift[j];
                }
                if (rhs < -1e-12) {
                    for (int j = 0; j < nOrig; j++) row[j] = -row[j];
                    rhs = -rhs;
                }
                rows.add(row); rhsList.add(rhs); rowTypes.add(0);
            }
        }

        // Count slack/surplus variables
        int nSlack = 0, nSurplus = 0;
        for (int t : rowTypes) {
            if (t == 1) nSlack++;
            if (t == -1) nSurplus++;
        }

        int m = rows.size();
        int nTotal = nOrig + nSlack + nSurplus;
        double[][] A_std = new double[m][nTotal];
        double[] b_std = new double[m];
        double[] c_std = new double[nTotal];
        int[] origIdx = new int[nTotal]; // maps standard column to original variable index

        // c_std: original c (minimization) for original vars, zeros for added
        for (int j = 0; j < nOrig; j++) {
            c_std[j] = c[j];
            origIdx[j] = j;
        }
        for (int j = nOrig; j < nTotal; j++) {
            c_std[j] = 0.0;
            origIdx[j] = -1;
        }

        int slkCnt = 0, surCnt = 0;
        for (int i = 0; i < m; i++) {
            System.arraycopy(rows.get(i), 0, A_std[i], 0, nOrig);
            int type = rowTypes.get(i);
            if (type == 1) {
                A_std[i][nOrig + slkCnt++] = 1.0;
            } else if (type == -1) {
                A_std[i][nOrig + nSlack + surCnt++] = -1.0;
            }
            b_std[i] = rhsList.get(i);
        }

        StandardForm sf = new StandardForm();
        sf.c = c_std;
        sf.A = A_std;
        sf.b = b_std;
        sf.shift = shift;
        sf.origIdx = origIdx;
        sf.nOrig = nOrig;
        return sf;
    }

    static class StandardForm {
        double[] c;
        double[][] A;
        double[] b;
        double[] shift;
        int[] origIdx;
        int nOrig;
    }


    // ---------------- Example usage ----------------
    public static void main(String[] args) {
        // Original problem: maximize 5x1 + 7x2
        // s.t. 2x1 + 3x2 <= 18
        //      x1 + 2x2 >= 8
        //      x1 + x2  = 6
        //      x1, x2 >= 0
        // Equivalent minimization: min -5x1 -7x2
        /*double[] c = {-5, -7};
        double[][] A_ub = {
                {2, 3},                // 2x1 + 3x2 <= 18
                {-1, -2}               // -x1 -2x2 <= -8  ->  x1 + 2x2 >= 8
        };
        double[] b_ub = {18, -8};
        double[][] A_eq = {{1, 1}};
        double[] b_eq = {6};
        double[][] bounds = {
                {0, Double.POSITIVE_INFINITY},
                {0, Double.POSITIVE_INFINITY}
        };*/

        double[] c = {-1, 4};
        double[][] A_ub = {
                {-3, 1},
                {1, 2}
        };
        double[] b_ub = {6, 4};
        double[][] A_eq = null;   // no equality constraints
        double[] b_eq = null;
        double[][] bounds = {
                {-10000, Double.POSITIVE_INFINITY},
                {-3, Double.POSITIVE_INFINITY}
        };

        //LinearProgram solver = new LinearProgram(c, A_ub, b_ub, A_eq, b_eq, bounds,"interior-point");
        LinearProgram solver = new LinearProgram(c, A_ub, b_ub, A_eq, b_eq, bounds);
        Result res = solver.solve();
        System.out.println("Success: " + res.success);
        System.out.println("Message: " + res.message);
        System.out.println("x = " + Arrays.toString(res.x));
        System.out.println("fun = " + res.fun);
    }
}
