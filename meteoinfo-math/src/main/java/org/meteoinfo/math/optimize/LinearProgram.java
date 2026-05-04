package org.meteoinfo.math.optimize;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Linear programming solver with an interface aligned to SciPy's linprog.
 * Now supports arbitrary variable bounds (including free variables).
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
        this(c, A_ub, b_ub, A_eq, b_eq, bounds);
        this.method = method;
    }

    public LinearProgram(Array ca, Array A_uba, Array b_uba, Array A_eqa, Array b_eqa,
                         List<List<Double>> bounds, String method) {
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
            StandardForm sf = convertToStandard();

            LinProgSolver solver = LinProgSolver.factory(sf.c, sf.A, sf.b, method);
            double[] y = solver.solve();
            double obj = solver.getObjectiveValue();

            // Simplex returns maximization value → flip sign
            if ("simplex".equalsIgnoreCase(method)) obj = -obj;

            // Recover original variables
            double[] x = new double[c.length];
            for (int k = 0; k < sf.nTotal; k++) {
                int origIdx = sf.origIdx[k];
                if (origIdx >= 0) {
                    x[origIdx] += sf.coeff[k] * y[k] + sf.constantPerCol[k];
                }
            }

            // Add constant offset from variable transformations
            obj += sf.objectiveConstant;

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
     * Converts the user‑friendly problem into standard form.
     * Handles all bound types: finite, one‑sided, and free.
     */
    private StandardForm convertToStandard() {
        int nOrig = c.length;
        List<ColInfo> colList = new ArrayList<>();
        List<Double> objCoeffs = new ArrayList<>();
        double objConst = 0.0;

        // Upper bounds that become explicit constraints
        List<double[]> upperRows = new ArrayList<>();
        List<Double> upperRHS = new ArrayList<>();

        // ---------- 1. Process each original variable ----------
        for (int j = 0; j < nOrig; j++) {
            double lb = (bounds != null && j < bounds.length) ? bounds[j][0] : 0.0;
            double ub = (bounds != null && j < bounds.length) ? bounds[j][1] : Double.POSITIVE_INFINITY;

            if (lb > Double.NEGATIVE_INFINITY && ub < Double.POSITIVE_INFINITY) {
                // Both finite: shift to zero, add upper constraint if needed
                double shift = lb;
                double newUb = ub - lb;
                colList.add(new ColInfo(j, 1.0, shift));
                objCoeffs.add(c[j]);           // minimization coefficient stays the same (x = y + shift)
                objConst += c[j] * shift;

                if (newUb < Double.POSITIVE_INFINITY) {
                    double[] row = new double[colList.size()]; // will be extended later
                    row[colList.size() - 1] = 1.0;
                    upperRows.add(row);
                    upperRHS.add(newUb);
                }

            } else if (lb > Double.NEGATIVE_INFINITY && ub == Double.POSITIVE_INFINITY) {
                // Only lower bound: shift
                double shift = lb;
                colList.add(new ColInfo(j, 1.0, shift));
                objCoeffs.add(c[j]);
                objConst += c[j] * shift;

            } else if (lb == Double.NEGATIVE_INFINITY && ub < Double.POSITIVE_INFINITY) {
                // Only upper bound: substitute x = ub - y
                colList.add(new ColInfo(j, -1.0, ub));
                objCoeffs.add(-c[j]);          // since c[j]*x = c[j]*(ub - y) = c[j]*ub - c[j]*y
                objConst += c[j] * ub;

            } else if (lb == Double.NEGATIVE_INFINITY && ub == Double.POSITIVE_INFINITY) {
                // Free variable: split x = x⁺ - x⁻
                colList.add(new ColInfo(j, 1.0, 0.0));
                colList.add(new ColInfo(j, -1.0, 0.0));
                objCoeffs.add(c[j]);           // coefficient for x⁺
                objCoeffs.add(-c[j]);          // coefficient for x⁻
                // no constant term

            } else {
                throw new UnsupportedOperationException("Unsupported bound combination.");
            }
        }

        int nNew = colList.size();          // columns before slacks

        // ---------- 2. Process original constraints ----------
        List<double[]> consRows = new ArrayList<>();
        List<Double> consRHS = new ArrayList<>();
        List<Integer> rowType = new ArrayList<>(); // 1=slack, -1=surplus, 0=equality

        // Upper bound rows (each gets a slack variable)
        for (int r = 0; r < upperRows.size(); r++) {
            double[] oldRow = upperRows.get(r);
            double[] newRow = new double[nNew];
            System.arraycopy(oldRow, 0, newRow, 0, oldRow.length);
            consRows.add(newRow);
            consRHS.add(upperRHS.get(r));
            rowType.add(1);   // slack
        }

        // Inequality constraints: A_ub * x <= b_ub
        if (A_ub != null) {
            for (int i = 0; i < A_ub.length; i++) {
                double[] row = new double[nNew];
                double rhs = b_ub[i];
                for (int k = 0; k < nNew; k++) {
                    ColInfo info = colList.get(k);
                    row[k] += A_ub[i][info.origIdx] * info.coeff;
                }
                // Subtract constant contributions
                for (int j = 0; j < nOrig; j++) {
                    double constJ = 0.0;
                    for (ColInfo ci : colList) {
                        if (ci.origIdx == j) {
                            constJ = ci.constant;
                            break;
                        }
                    }
                    rhs -= A_ub[i][j] * constJ;
                }

                if (rhs >= -1e-12) {
                    consRows.add(row);
                    consRHS.add(rhs);
                    rowType.add(1);      // slack
                } else {
                    for (int k = 0; k < nNew; k++) row[k] = -row[k];
                    rhs = -rhs;
                    consRows.add(row);
                    consRHS.add(rhs);
                    rowType.add(-1);     // surplus
                }
            }
        }

        // Equality constraints: A_eq * x == b_eq
        if (A_eq != null) {
            for (int i = 0; i < A_eq.length; i++) {
                double[] row = new double[nNew];
                double rhs = b_eq[i];
                for (int k = 0; k < nNew; k++) {
                    ColInfo info = colList.get(k);
                    row[k] += A_eq[i][info.origIdx] * info.coeff;
                }
                for (int j = 0; j < nOrig; j++) {
                    double constJ = 0.0;
                    for (ColInfo ci : colList) {
                        if (ci.origIdx == j) {
                            constJ = ci.constant;
                            break;
                        }
                    }
                    rhs -= A_eq[i][j] * constJ;
                }
                if (rhs < -1e-12) {
                    for (int k = 0; k < nNew; k++) row[k] = -row[k];
                    rhs = -rhs;
                }
                consRows.add(row);
                consRHS.add(rhs);
                rowType.add(0);   // equality (no extra variable)
            }
        }

        // ---------- 3. Count slack/surplus and build final matrices ----------
        int nSlack = 0, nSurplus = 0;
        for (int t : rowType) {
            if (t == 1) nSlack++;
            else if (t == -1) nSurplus++;
        }

        int nTotal = nNew + nSlack + nSurplus;
        double[][] A_std = new double[consRows.size()][nTotal];
        double[] b_std = new double[consRows.size()];
        double[] c_std = new double[nTotal];
        int[] origIdx = new int[nTotal];
        double[] coeff = new double[nTotal];
        double[] constantPerCol = new double[nTotal];

        // Fill columns from variable transformations
        for (int k = 0; k < nNew; k++) {
            ColInfo info = colList.get(k);
            c_std[k] = objCoeffs.get(k);
            origIdx[k] = info.origIdx;
            coeff[k] = info.coeff;
            constantPerCol[k] = info.constant;
        }

        // Slack / surplus columns
        for (int k = nNew; k < nTotal; k++) {
            c_std[k] = 0.0;
            origIdx[k] = -1;
            coeff[k] = 0.0;
            constantPerCol[k] = 0.0;
        }

        int slkIdx = 0, surIdx = 0;
        for (int i = 0; i < consRows.size(); i++) {
            System.arraycopy(consRows.get(i), 0, A_std[i], 0, nNew);
            int type = rowType.get(i);
            if (type == 1) {
                A_std[i][nNew + slkIdx++] = 1.0;
            } else if (type == -1) {
                A_std[i][nNew + nSlack + surIdx++] = -1.0;
            }
            b_std[i] = consRHS.get(i);
        }

        StandardForm sf = new StandardForm();
        sf.c = c_std;
        sf.A = A_std;
        sf.b = b_std;
        sf.origIdx = origIdx;
        sf.coeff = coeff;
        sf.constantPerCol = constantPerCol;
        sf.nTotal = nTotal;
        sf.nOrig = nOrig;
        sf.objectiveConstant = objConst;
        return sf;
    }

    // ---------- Helper classes ----------

    /** Stores information about one column in the standard form. */
    private static class ColInfo {
        int origIdx;       // original variable index
        double coeff;      // multiplier for recovery (1 or -1)
        double constant;   // constant added to the original variable from this column

        ColInfo(int origIdx, double coeff, double constant) {
            this.origIdx = origIdx;
            this.coeff = coeff;
            this.constant = constant;
        }
    }

    static class StandardForm {
        double[] c;
        double[][] A;
        double[] b;
        int[] origIdx;
        double[] coeff;
        double[] constantPerCol;
        int nTotal;
        int nOrig;
        double objectiveConstant;
    }

    // ---------------- Example usage ----------------
    public static void main(String[] args) {
        double[] c = {-1, 4};
        double[][] A_ub = {
                {-3, 1},
                {1, 2}
        };
        double[] b_ub = {6, 4};
        double[][] bounds = {
                {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY},  // free variable x1
                {-3, Double.POSITIVE_INFINITY}                          // x2 >= -3
        };

        LinearProgram solver = new LinearProgram(c, A_ub, b_ub, null, null, bounds);
        Result res = solver.solve();
        System.out.println("Success: " + res.success);
        System.out.println("Message: " + res.message);
        System.out.println("x = " + Arrays.toString(res.x));
        System.out.println("fun = " + res.fun);
    }
}