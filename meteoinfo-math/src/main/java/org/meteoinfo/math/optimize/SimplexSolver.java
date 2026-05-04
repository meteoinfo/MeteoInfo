package org.meteoinfo.math.optimize;

import org.bytedeco.javacpp.DoublePointer;
import static org.bytedeco.openblas.global.openblas.*;

/**
 * Two-Phase Simplex Method solver for linear programming problems
 * in the standard form:
 *      maximize    c^T x
 *      subject to  Ax = b, x >= 0
 *
 * Uses OpenBLAS (via JavaCPP) for core linear algebra operations.
 */
public class SimplexSolver extends LinProgSolver {

    private final int cols;           // total columns: original n + artificial m + RHS 1
    private DoublePointer tableau;    // simplex tableau stored in row-major order
    private int[] basis;              // indices of basic variables (length m)
    private final double epsilon = 1e-8;
    private final double tolerance = 1e-12;

    public SimplexSolver(double[] c, double[][] A, double[] b) {
        super(c, A, b);
        this.cols = n + m + 1;
        this.tableau = new DoublePointer((m + 1) * cols);
        this.basis = new int[m];

        initializePhaseI(A, b);
    }

    // --------------- Phase I ---------------
    private void initializePhaseI(double[][] A, double[] b) {
        // Fill constraint rows (1..m)
        for (int i = 0; i < m; i++) {
            int row = i + 1;
            for (int j = 0; j < n; j++) {
                tableau.put(row * cols + j, A[i][j]);
            }
            tableau.put(row * cols + n + i, 1.0);    // artificial variable column
            tableau.put(row * cols + cols - 1, b[i]);
            basis[i] = n + i;                        // artificials are initial basis
        }

        // Phase I objective: maximize -sum(artificials)
        // Start with row: w + sum a_i = 0  →  coefficients (0…0, 1…1) RHS 0
        for (int j = 0; j < cols; j++) {
            tableau.put(0 * cols + j, 0.0);
        }
        for (int i = 0; i < m; i++) {
            tableau.put(0 * cols + n + i, 1.0);      // coefficient of a_i is +1
        }

        // Eliminate basic variables (artificials) by subtracting each constraint row
        for (int i = 0; i < m; i++) {
            int row = i + 1;
            DoublePointer objRow = new DoublePointer(tableau);
            objRow.position(0 * cols).limit(cols);
            DoublePointer conRow = new DoublePointer(tableau);
            conRow.position(row * cols).limit(cols);
            cblas_daxpy(cols, -1.0, conRow, 1, objRow, 1);
            objRow.limit(0);
            conRow.limit(0);
        }
    }

    @Override
    public double[] solve() {
        if (!phaseI()) {
            throw new RuntimeException("The problem is infeasible.");
        }

        setupPhaseII();      // replace objective row with original objective
        phaseII();
        return extractSolution();
    }

    private boolean phaseI() {
        while (true) {
            int entering = findEnteringVariable(0, true);
            if (entering == -1) break;           // optimal for Phase I

            int leaving = findLeavingVariable(entering);
            if (leaving == -1) return false;     // unbounded auxiliary → infeasible

            pivot(entering, leaving);
        }

        // The optimal value of the auxiliary objective must be 0
        double z = tableau.get(0 * cols + cols - 1);
        return Math.abs(z) <= epsilon;
    }

    // --------------- Phase II ---------------
    private void setupPhaseII() {
        // 1. Zero out the entire objective row
        for (int j = 0; j < cols; j++) {
            tableau.put(0 * cols + j, 0.0);
        }

        // 2. Write originalC into the first n columns
        for (int j = 0; j < n; j++) {
            tableau.put(0 * cols + j, originalC[j]);
        }

        // 3. Eliminate basic variables from the objective row
        for (int i = 0; i < m; i++) {
            int basicVar = basis[i];
            double objCoeff = tableau.get(0 * cols + basicVar);
            if (Math.abs(objCoeff) > tolerance) {
                int row = i + 1;
                DoublePointer targetRow = new DoublePointer(tableau);
                targetRow.position(0 * cols).limit(cols);
                DoublePointer sourceRow = new DoublePointer(tableau);
                sourceRow.position(row * cols).limit(row * cols + cols);
                // objRow = objRow - objCoeff * constraintRow
                cblas_daxpy(cols, -objCoeff, sourceRow, 1, targetRow, 1);
                targetRow.limit(0);
                sourceRow.limit(0);
            }
        }
    }

    private void phaseII() {
        while (true) {
            int entering = findEnteringVariable(0, false);  // only original variables
            if (entering == -1) break;                     // optimal reached

            int leaving = findLeavingVariable(entering);
            if (leaving == -1) {
                throw new RuntimeException("The objective is unbounded.");
            }
            pivot(entering, leaving);
        }
    }

    // --------------- Core simplex operations ---------------
    /**
     * Finds the most negative coefficient in the given objective row.
     * If allowArtificial is false, only columns 0..n-1 are scanned.
     */
    private int findEnteringVariable(int objRow, boolean allowArtificial) {
        int entering = -1;
        double mostNegative = -epsilon;
        int limit = allowArtificial ? cols - 1 : n;
        for (int j = 0; j < limit; j++) {
            double val = tableau.get(objRow * cols + j);
            if (val < mostNegative) {
                mostNegative = val;
                entering = j;
            }
        }
        return entering;
    }

    private int findLeavingVariable(int enteringCol) {
        int leaving = -1;
        double minRatio = Double.MAX_VALUE;
        for (int i = 0; i < m; i++) {
            int row = i + 1;
            double a_ie = tableau.get(row * cols + enteringCol);
            if (a_ie > epsilon) {
                double ratio = tableau.get(row * cols + cols - 1) / a_ie;
                if (ratio < minRatio) {
                    minRatio = ratio;
                    leaving = i;
                }
            }
        }
        return leaving;
    }

    private void pivot(int enteringCol, int leavingRowIdx) {
        int leavingRow = leavingRowIdx + 1;
        double pivotVal = tableau.get(leavingRow * cols + enteringCol);

        // 1. Normalize the pivot row
        DoublePointer pivotRow = new DoublePointer(tableau);
        pivotRow.position(leavingRow * cols).limit(leavingRow * cols + cols);
        cblas_dscal(cols, 1.0 / pivotVal, pivotRow, 1);
        pivotRow.limit(0);

        // 2. Eliminate the entering column from all other rows
        for (int r = 0; r <= m; r++) {
            if (r == leavingRow) continue;
            double factor = tableau.get(r * cols + enteringCol);
            if (Math.abs(factor) > epsilon) {
                DoublePointer targetRow = new DoublePointer(tableau);
                targetRow.position(r * cols).limit(r * cols + cols);
                DoublePointer sourceRow = new DoublePointer(tableau);
                sourceRow.position(leavingRow * cols).limit(leavingRow * cols + cols);
                cblas_daxpy(cols, -factor, sourceRow, 1, targetRow, 1);
                targetRow.limit(0);
            }
        }
        basis[leavingRowIdx] = enteringCol;
    }

    // --------------- Solution extraction ---------------
    private double[] extractSolution() {
        double[] solution = new double[n];
        for (int j = 0; j < n; j++) {
            int basicRow = -1;
            for (int i = 0; i < m; i++) {
                if (basis[i] == j) {
                    basicRow = i;
                    break;
                }
            }
            solution[j] = (basicRow != -1)
                    ? tableau.get((basicRow + 1) * cols + cols - 1)
                    : 0.0;
        }
        return solution;
    }

    @Override
    public double getObjectiveValue() {
        return tableau.get(0 * cols + cols - 1);
    }

    // --------------- Test ---------------
    public static void main(String[] args) {
        // Maximize 3x1 + 2x2
        // s.t.  x1 + x2 + x3    = 4
        //      2x1 + x2    + x4 = 5
        //      x1, x2, x3, x4 >= 0
        /*double[] c = {3.0, 2.0, 0.0, 0.0};
        double[][] A = {
                {1.0, 1.0, 1.0, 0.0},
                {2.0, 1.0, 0.0, 1.0}
        };
        double[] b = {4.0, 5.0};*/

        double[] c = {5, 7, 0, 0};
        double[][] A = {
                {2, 3, 1,  0},
                {1, 2, 0, -1},
                {1, 1, 0,  0}
        };
        double[] b = {18, 8, 6};

        /*double[] c = {-1, 4};
        double[][] A = {
                {-3, 1},
                {1, 2}
        };
        double[] b = {6, 4};*/

        SimplexSolver solver = new SimplexSolver(c, A, b);
        double[] x = solver.solve();
        System.out.println("Optimal solution:");
        for (int i = 0; i < x.length; i++) {
            System.out.printf("x%d = %.4f%n", i+1, x[i]);
        }
        System.out.printf("Optimal value = %.4f%n", solver.getObjectiveValue());
    }
}
