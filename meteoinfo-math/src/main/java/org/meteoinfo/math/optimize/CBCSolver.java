package org.meteoinfo.math.optimize;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import org.meteoinfo.math.optimize.LinearProgram.Result;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Linear programming solver that uses Google OR‑Tools with the
 * CBC backend.
 *
 * <pre>{@code
 *   Result res = CBCSolver.solve(
 *       c,          // objective coefficients (minimization)
 *       A_ub, b_ub, // inequality constraints A_ub * x <= b_ub
 *       A_eq, b_eq, // equality constraints   A_eq * x == b_eq
 *       bounds      // variable bounds [lower, upper]
 *   );
 * }</pre>
 */
public class CBCSolver {

    // Load OR‑Tools native libraries once
    static {
        Loader.loadNativeLibraries();
    }

    /**
     * Solve a linear program.
     *
     * @param c      Coefficients of the (minimization) objective.
     * @param A_ub   2‑D array for inequality constraints (A_ub @ x <= b_ub). May be null.
     * @param b_ub   1‑D array for inequality RHS. May be null.
     * @param A_eq   2‑D array for equality constraints (A_eq @ x == b_eq). May be null.
     * @param b_eq   1‑D array for equality RHS. May be null.
     * @param bounds Variable bounds, each entry {lower, upper}. Use infinity for no bound.
     *               Default is [0, ∞) for each variable.
     * @return Result with x, fun, success, message.
     */
    public static Result solve (Array ca, Array A_uba, Array b_uba, Array A_eqa, Array b_eqa,
                                List<List<Double>> bounds) {
        return solve((double[]) ca.get1DJavaArray(double.class),
                A_uba == null ? null : (double[][]) ArrayUtil.copyToNDJavaArray_Double(A_uba),
                b_uba == null ? null : (double[]) b_uba.get1DJavaArray(double.class),
                A_eqa == null ? null : (double[][]) ArrayUtil.copyToNDJavaArray_Double(A_eqa),
                b_eqa == null ? null : (double[]) b_eqa.get1DJavaArray(double.class),
                (double[][]) bounds.stream().map(innerList -> innerList.stream()
                                .mapToDouble(Double::doubleValue)
                                .toArray())
                        .toArray(double[][]::new));
    }

    /**
     * Solve a linear program.
     *
     * @param c      Coefficients of the (minimization) objective.
     * @param A_ub   2‑D array for inequality constraints (A_ub @ x <= b_ub). May be null.
     * @param b_ub   1‑D array for inequality RHS. May be null.
     * @param A_eq   2‑D array for equality constraints (A_eq @ x == b_eq). May be null.
     * @param b_eq   1‑D array for equality RHS. May be null.
     * @param bounds Variable bounds, each entry {lower, upper}. Use infinity for no bound.
     *               Default is [0, ∞) for each variable.
     * @return Result with x, fun, success, message.
     */
    public static Result solve(double[] c,
                               double[][] A_ub, double[] b_ub,
                               double[][] A_eq, double[] b_eq,
                               double[][] bounds) {
        try {
            int nVars = c.length;

            // Create the CBC solver via OR‑Tools
            MPSolver solver = MPSolver.createSolver("CBC");
            if (solver == null) {
                Result res = new Result();
                res.success = false;
                res.message = "Could not create CBC solver (check OR‑Tools native libraries).";
                return res;
            }
            solver.suppressOutput();

            // ---------- 1. Create variables ----------
            MPVariable[] vars = new MPVariable[nVars];
            for (int j = 0; j < nVars; j++) {
                double lb = 0.0;
                double ub = Double.POSITIVE_INFINITY;
                if (bounds != null && j < bounds.length) {
                    lb = bounds[j][0];
                    ub = bounds[j][1];
                    // OR‑Tools accepts -inf / +inf
                    if (Double.isInfinite(lb)) lb = -Double.POSITIVE_INFINITY;
                    if (Double.isInfinite(ub)) ub = Double.POSITIVE_INFINITY;
                }
                vars[j] = solver.makeNumVar(lb, ub, "x" + j);
            }

            // ---------- 2. Inequality constraints: A_ub * x <= b_ub ----------
            if (A_ub != null) {
                for (int i = 0; i < A_ub.length; i++) {
                    MPConstraint con = solver.makeConstraint(
                            -Double.POSITIVE_INFINITY, b_ub[i], "ineq" + i);
                    for (int j = 0; j < nVars; j++) {
                        if (Math.abs(A_ub[i][j]) > 1e-15) {
                            con.setCoefficient(vars[j], A_ub[i][j]);
                        }
                    }
                }
            }

            // ---------- 3. Equality constraints: A_eq * x == b_eq ----------
            if (A_eq != null) {
                for (int i = 0; i < A_eq.length; i++) {
                    MPConstraint con = solver.makeConstraint(b_eq[i], b_eq[i], "eq" + i);
                    for (int j = 0; j < nVars; j++) {
                        if (Math.abs(A_eq[i][j]) > 1e-15) {
                            con.setCoefficient(vars[j], A_eq[i][j]);
                        }
                    }
                }
            }

            // ---------- 4. Objective function (minimization) ----------
            MPObjective objective = solver.objective();
            for (int j = 0; j < nVars; j++) {
                objective.setCoefficient(vars[j], c[j]);
            }
            objective.setMinimization();

            // ---------- 5. Solve ----------
            MPSolver.ResultStatus status = solver.solve();

            Result result = new Result();
            if (status == MPSolver.ResultStatus.OPTIMAL) {
                double[] x = new double[nVars];
                for (int j = 0; j < nVars; j++) {
                    x[j] = vars[j].solutionValue();
                }
                result.success = true;
                result.message = "Optimization terminated successfully (CBC via OR-Tools).";
                result.x = x;
                result.fun = objective.value();
            } else {
                result.success = false;
                result.message = "Solver ended with status: " + status;
                result.x = null;
                result.fun = Double.NaN;
            }

            return result;

        } catch (Exception e) {
            Result res = new Result();
            res.success = false;
            res.message = "Exception in OR-Tools LP solver: " + e.getMessage();
            res.x = null;
            res.fun = Double.NaN;
            return res;
        }
    }

    // ---------------- Example usage ----------------
    public static void main(String[] args) {
        // minimize  -x1 + 4*x2
        // subject to  -3*x1 + x2 <= 6,  x1 + 2*x2 <= 4,  no explicit bounds
        double[] c = {-1, 4};
        double[][] A_ub = {
                {-3, 1},
                {1, 2}
        };
        double[] b_ub = {6, 4};
        double[][] bounds = {
                {0, Double.POSITIVE_INFINITY},
                {0, Double.POSITIVE_INFINITY}
        };

        Result res = CBCSolver.solve(c, A_ub, b_ub, null, null, bounds);
        System.out.println("Success: " + res.success);
        System.out.println("Message: " + res.message);
        System.out.println("x = " + Arrays.toString(res.x));
        System.out.println("fun = " + res.fun);
    }
}
