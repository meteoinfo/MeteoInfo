package org.meteoinfo.math.optimize;

import org.bytedeco.javacpp.DoublePointer;
import java.util.Arrays;
import static org.bytedeco.openblas.global.openblas.*;

public class InteriorPointSolver extends LinProgSolver {

    private double tol = 1e-8;          // relaxed tolerance
    private int maxIter = 100;
    private boolean debug = false;      // set to true to watch progress

    private final double[][] A;
    private final double[] b;
    private double[] xOpt;
    private double objVal;

    public InteriorPointSolver(double[] c, double[][] A, double[] b) {
        super(c, A, b);        // stores m, n, originalC (minimization coefficients)
        this.A = A;
        this.b = b;
    }

    @Override
    public double[] solve() {
        int m = this.m;
        int n = this.n;

        double[] x = new double[n];
        double[] y = new double[m];
        double[] s = new double[n];
        Arrays.fill(x, 1.0);
        Arrays.fill(s, 1.0);

        // Warm start: reduce initial infeasibility
        double[] Ax0 = new double[m];
        matVecMult(A, x, Ax0);
        for (int i = 0; i < m; i++) {
            if (b[i] > 1e-12 && Math.abs(Ax0[i]) > 1e-12) {
                double scale = b[i] / Ax0[i];
                if (scale > 0 && scale < 1.0) {
                    for (int j = 0; j < n; j++) x[j] *= scale;
                    Arrays.fill(s, 1.0);
                    break;
                }
            }
        }

        double[] rc = new double[n];
        double[] rb = new double[m];
        double[] ATy = new double[n];

        for (int iter = 0; iter < maxIter; iter++) {
            // Residuals
            matVecMult(A, x, Ax0);
            matVecTransMult(A, y, ATy);
            for (int j = 0; j < n; j++) rc[j] = ATy[j] + s[j] - originalC[j];
            for (int i = 0; i < m; i++) rb[i] = Ax0[i] - b[i];

            double xDotS = dot(x, s);
            double mu = xDotS / n;

            double priNorm = nrm2(rb) / (1.0 + nrm2(b));
            double dualNorm = nrm2(rc) / (1.0 + nrm2(originalC));
            double gap = xDotS / (1.0 + Math.abs(dot(originalC, x)));
            if (debug) System.out.printf("Iter %2d: pri=%e dual=%e gap=%e mu=%e%n",
                    iter, priNorm, dualNorm, gap, mu);
            if (priNorm < tol && dualNorm < tol && gap < tol) break;

            // Predictor
            double[] rxsAff = new double[n];
            for (int j = 0; j < n; j++) rxsAff[j] = x[j] * s[j];
            double[] deltaAff = solveKKT(x, s, rc, rb, rxsAff);
            if (deltaAff == null) throw new RuntimeException("KKT singular in predictor");
            double[] dxAff = Arrays.copyOfRange(deltaAff, 0, n);
            double[] dyAff = Arrays.copyOfRange(deltaAff, n, n + m);
            double[] dsAff = Arrays.copyOfRange(deltaAff, n + m, 2 * n + m);

            double alphaPaff = stepLength(x, dxAff, 1.0);
            double alphaDaff = stepLength(s, dsAff, 1.0);

            double muAff = 0.0;
            for (int j = 0; j < n; j++)
                muAff += (x[j] + alphaPaff * dxAff[j]) * (s[j] + alphaDaff * dsAff[j]);
            muAff /= n;
            double sigma = Math.min(1.0, Math.pow(muAff / mu, 3.0));

            // Corrector
            double[] rxsCorr = new double[n];
            for (int j = 0; j < n; j++)
                rxsCorr[j] = x[j] * s[j] + dxAff[j] * dsAff[j] - sigma * mu;

            double[] delta = solveKKT(x, s, rc, rb, rxsCorr);
            if (delta == null) throw new RuntimeException("KKT singular in corrector");
            double[] dx = Arrays.copyOfRange(delta, 0, n);
            double[] dy = Arrays.copyOfRange(delta, n, n + m);
            double[] ds = Arrays.copyOfRange(delta, n + m, 2 * n + m);

            double alphaP = Math.min(1.0, 0.99 * stepLength(x, dx, 1.0));
            double alphaD = Math.min(1.0, 0.99 * stepLength(s, ds, 1.0));

            for (int j = 0; j < n; j++) x[j] = Math.max(1e-14, x[j] + alphaP * dx[j]);
            for (int i = 0; i < m; i++) y[i] += alphaD * dy[i];
            for (int j = 0; j < n; j++) s[j] = Math.max(1e-14, s[j] + alphaD * ds[j]);
        }

        xOpt = x.clone();
        objVal = dot(originalC, xOpt);
        return xOpt;
    }

    @Override
    public double getObjectiveValue() { return objVal; }

    // ---------- KKT system solver (analytic for m <= 2) ----------
    private double[] solveKKT(double[] x, double[] s, double[] rc, double[] rb, double[] rxs) {
        int m = this.m;
        int n = this.n;

        double[] theta = new double[n];
        for (int j = 0; j < n; j++) {
            theta[j] = Math.max(x[j] / Math.max(s[j], 1e-14), 1e-14);
        }

        // Build RHS: v = (X*rc - rxs) ./ s
        double[] v = new double[n];
        for (int j = 0; j < n; j++) {
            v[j] = (x[j] * rc[j] - rxs[j]) / Math.max(s[j], 1e-14);
        }
        double[] Av = new double[m];
        matVecMult(A, v, Av);
        double[] rhs = new double[m];
        for (int i = 0; i < m; i++) rhs[i] = -rb[i] - Av[i];

        // Build M = A * diag(theta) * A^T + reg*I
        double[][] M = new double[m][m];
        for (int i = 0; i < m; i++) {
            for (int k = 0; k < m; k++) {
                double sum = 0.0;
                for (int j = 0; j < n; j++) sum += A[i][j] * theta[j] * A[k][j];
                M[i][k] = sum;
            }
        }

        double[] dy = null;
        if (m == 1) {
            double reg = 1e-8;
            double a = M[0][0] + reg;
            if (Math.abs(a) < 1e-14) return null;
            dy = new double[] { rhs[0] / a };
        } else if (m == 2) {
            // analytic inverse with regularization
            double a = M[0][0], b = M[0][1], c = M[1][0], d = M[1][1];
            double reg = 1e-8;
            a += reg; d += reg;
            double det = a * d - b * c;
            if (Math.abs(det) < 1e-14) return null;
            dy = new double[2];
            dy[0] = ( d * rhs[0] - b * rhs[1]) / det;
            dy[1] = (-c * rhs[0] + a * rhs[1]) / det;
        } else {
            // general LU
            dy = solveLU(M, rhs);
            if (dy == null) return null;
        }

        // Recover ds and dx
        double[] ATdy = new double[n];
        matVecTransMult(A, dy, ATdy);
        double[] ds = new double[n];
        for (int j = 0; j < n; j++) ds[j] = -ATdy[j] - rc[j];

        double[] dx = new double[n];
        for (int j = 0; j < n; j++) {
            dx[j] = (-rxs[j] - x[j] * ds[j]) / Math.max(s[j], 1e-14);
        }

        double[] result = new double[2 * n + m];
        System.arraycopy(dx, 0, result, 0, n);
        System.arraycopy(dy, 0, result, n, m);
        System.arraycopy(ds, 0, result, n + m, n);
        return result;
    }

    // ---------- LU fallback ----------
    private double[] solveLU(double[][] M, double[] rhs) {
        int n = M.length;
        double[][] A = new double[n][n];
        double[] b = Arrays.copyOf(rhs, n);
        for (int i = 0; i < n; i++) System.arraycopy(M[i], 0, A[i], 0, n);

        for (int k = 0; k < n; k++) {
            int maxRow = k;
            double maxVal = Math.abs(A[k][k]);
            for (int i = k + 1; i < n; i++) {
                if (Math.abs(A[i][k]) > maxVal) { maxVal = Math.abs(A[i][k]); maxRow = i; }
            }
            if (maxVal < 1e-14) return null;
            if (maxRow != k) {
                double[] tmp = A[k]; A[k] = A[maxRow]; A[maxRow] = tmp;
                double tb = b[k]; b[k] = b[maxRow]; b[maxRow] = tb;
            }
            for (int i = k + 1; i < n; i++) {
                double factor = A[i][k] / A[k][k];
                A[i][k] = factor;
                for (int j = k + 1; j < n; j++) A[i][j] -= factor * A[k][j];
            }
        }
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = b[i];
            for (int j = 0; j < i; j++) sum -= A[i][j] * y[j];
            y[i] = sum;
        }
        double[] z = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = y[i];
            for (int j = i + 1; j < n; j++) sum -= A[i][j] * z[j];
            z[i] = sum / A[i][i];
        }
        return z;
    }

    // ---------- Utilities ----------
    private void matVecMult(double[][] A, double[] x, double[] y) {
        int m = A.length, n = A[0].length;
        for (int i = 0; i < m; i++) {
            double sum = 0.0;
            for (int j = 0; j < n; j++) sum += A[i][j] * x[j];
            y[i] = sum;
        }
    }
    private void matVecTransMult(double[][] A, double[] y, double[] x) {
        int m = A.length, n = A[0].length;
        for (int j = 0; j < n; j++) {
            double sum = 0.0;
            for (int i = 0; i < m; i++) sum += A[i][j] * y[i];
            x[j] = sum;
        }
    }
    private double dot(double[] a, double[] b) {
        return cblas_ddot(a.length, new DoublePointer(a), 1, new DoublePointer(b), 1);
    }
    private double nrm2(double[] v) {
        return cblas_dnrm2(v.length, new DoublePointer(v), 1);
    }
    private double stepLength(double[] v, double[] dv, double alphaMax) {
        double alpha = alphaMax;
        for (int i = 0; i < v.length; i++) {
            if (dv[i] < 0) {
                double ratio = -v[i] / dv[i];
                if (ratio < alpha) alpha = ratio;
            }
        }
        return alpha;
    }

    // ---------- Test ----------
    public static void main(String[] args) {
        double[] c = {-1, -1, 0, 0};
        double[][] A = {
                {1, 2, 1, 0},
                {2, 1, 0, 1}
        };
        double[] b = {4, 4};
        InteriorPointSolver solver = new InteriorPointSolver(c, A, b);
        solver.debug = true;
        double[] x = solver.solve();
        System.out.println("Optimal x: " + Arrays.toString(x));
        System.out.println("Optimal value: " + solver.getObjectiveValue());
    }
}
