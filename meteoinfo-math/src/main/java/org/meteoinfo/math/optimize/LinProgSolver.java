package org.meteoinfo.math.optimize;

public abstract class LinProgSolver {

    protected final int m;              // number of constraints
    protected final int n;              // number of original decision variables
    protected final double[] originalC; // original objective coefficients (length n)

    /**
     * Constructor
     * @param c standard form objective (minimization)
     * @param A constraint matrix (mStd × nStd)
     * @param b right‑hand side (non‑negative)
     */
    public LinProgSolver(double[] c, double[][] A, double[] b) {
        this.m = b.length;
        this.n = c.length;
        this.originalC = c.clone();
    }

    /**
     * Factory
     * @param c standard form objective (minimization)
     * @param A constraint matrix (mStd × nStd)
     * @param b right‑hand side (non‑negative)
     * @param method the method string ["simplex" | "interior-point"]
     * @return
     */
    public static LinProgSolver factory(double[] c, double[][] A, double[] b, String method) {
        switch (method.toLowerCase()) {
            case "simplex":
                return new SimplexSolver(c, A, b);
            case "interior-point":
                return new InteriorPointSolver(c, A, b);
            default:
                throw new UnsupportedOperationException(String.format("%s not supported.", method));
        }
    }

    public abstract double[] solve();

    public abstract double getObjectiveValue();

}
