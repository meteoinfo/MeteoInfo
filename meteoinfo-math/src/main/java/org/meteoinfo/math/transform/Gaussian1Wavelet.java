package org.meteoinfo.math.transform;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Complex;
import org.meteoinfo.ndarray.DataType;

public class Gaussian1Wavelet extends Wavelet {

    private static final double NORM = (Math.pow(2, 1.25) / Math.pow(Math.PI, 0.25));

    public Gaussian1Wavelet() {
        super("gaus1");
        this.complex = false;
        this.lowerBound = -5.0;
        this.upperBound = 5.0;
    }

    @Override
    public Complex value(double x) {
        return new Complex(-NORM * x * Math.exp(-0.5 * x * x), 0);
    }

    @Override
    public Complex[] generate(int n, double scale, double dt) {
        double[] t = linspace(n, dt);
        Complex[] psi = new Complex[n];
        double invSqrtA = 1.0 / Math.sqrt(scale);
        for (int i = 0; i < n; i++) {
            double x = t[i] / scale;
            double val = -x * Math.exp(-x * x) * NORM * invSqrtA;
            psi[i] = new Complex(val, 0.0);
        }
        return psi;
    }

    @Override
    public Array[] waveFun(int precision) {
        // 1. Number of sampling points
        int numPoints = (int) Math.pow(2, precision);

        // 2. Define time grid range
        //    Gaussian wavelet decays to near zero outside [-5, 5], which is sufficient.
        double step = (upperBound - lowerBound) / (numPoints - 1);

        Array psi = Array.factory(DataType.DOUBLE, new int[]{numPoints});
        Array x = Array.factory(DataType.DOUBLE, new int[]{numPoints});

        // 3. Compute wavelet values at each point
        //    psi(t) = -t * exp(-t^2)
        for (int i = 0; i < numPoints; i++) {
            double t = lowerBound + i * step;
            x.setDouble(i, t);
            psi.setDouble(i, -t * Math.exp(-t * t) * NORM);
        }

        // 4. Return wavelet values and grid
        return new Array[]{psi, x};
    }

    @Override
    public double centralFrequency() {
        return 0.2;   // approximate
    }
}
