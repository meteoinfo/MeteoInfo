package org.meteoinfo.math.special;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Complex;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;

/**
 * Implementation of an algorithm for the Lambert W
 */
public class LambertW {

    public static int MAXIT = 15;
    public static double EPS = 1e-15;

    public static void setEPS(double eps) {
        EPS = eps;
    }

    /** main branch W₀(z) */
    public static Complex W0(Complex z) { return eval(z, 0); }

    /** main branch W₀(z) */
    public static Array W0(Array z) {
        Array r = Array.factory(DataType.COMPLEX, z.getShape());
        IndexIterator iterR = r.getIndexIterator();
        IndexIterator iterZ = z.getIndexIterator();
        while (iterR.hasNext()) {
            iterR.setComplexNext(eval(iterZ.getComplexNext(), 0));
        }

        return r;
    }

    /** other branch Wₖ(z) */
    public static Complex Wk(Complex z, int k) { return eval(z, k); }

    /** other branch Wₖ(z) */
    public static Array Wk(Array z, int k) {
        Array r = Array.factory(DataType.COMPLEX, z.getShape());
        IndexIterator iterR = r.getIndexIterator();
        IndexIterator iterZ = z.getIndexIterator();
        while (iterR.hasNext()) {
            iterR.setComplexNext(eval(iterZ.getComplexNext(), k));
        }

        return r;
    }

    /* ---------- core iteration ---------- */
    private static Complex eval(Complex z, int k) {
        if (z.real() == Double.NEGATIVE_INFINITY || z.imag() == Double.NEGATIVE_INFINITY
                || Double.isNaN(z.real()) || Double.isNaN(z.imag()))
            return new Complex(Double.NaN, Double.NaN);

        /* deal special points */
        if (k == 0 && z.real() == -1.0 / Math.E && z.imag() == 0.0)
            return new Complex(-1, 0);
        if (k == 0 && z.real() == 0 && z.imag() == 0)
            return new Complex(0, 0);

        Complex w = initialGuess(z, k);

        for (int iter = 0; iter < MAXIT; iter++) {
            Complex e = w.exp();
            Complex f = w.multiply(e).subtract(z);
            Complex df = e.multiply(w.add(new Complex(1, 0)));
            Complex ddf = e.multiply(w.add(new Complex(2, 0)));
            Complex delta = f.multiply(df).divide(df.multiply(df).subtract(f.multiply(ddf).multiply(0.5)));
            w = w.subtract(delta);
            if (delta.abs() < EPS * w.abs())
                break;
        }
        return w;
    }

    /* ---------- initial guess ---------- */
    private static Complex initialGuess(Complex z, int k) {
        if (k == 0) return initialGuess0(z);
        else        return initialGuessK(z, k);
    }

    /* main branch W₀(z) initial value */
    private static Complex initialGuess0(Complex z) {
        double x = z.real(), y = z.imag();
        if (Math.abs(y) < 1e-15 && Math.abs(x) <= 0.5) {
            /* from SciPy cephes/lambertw.c  Pade approximation */
            if (x == 0) return new Complex(0, 0);
            double L1 = Math.log(1 + x);
            // first order correction: w ≈ L1 / (1 + L1)
            return new Complex(L1 / (1 + L1), 0);
        }
        // approximate: w ≈ log(z)  (k=0)
        return z.log();
    }

    /* other branch Wₖ(z), k≠0 initial value */
    private static Complex initialGuessK(Complex z, int k) {
        Complex logZ = z.log();
        Complex twoPiK = new Complex(0, 2 * Math.PI * k);
        return logZ.add(twoPiK);
    }
}
