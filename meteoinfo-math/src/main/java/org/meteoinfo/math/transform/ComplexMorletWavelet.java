package org.meteoinfo.math.transform;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Complex;
import org.meteoinfo.ndarray.DataType;

public class ComplexMorletWavelet extends Wavelet {
    private final double bandwidth;   // B
    private final double centerFreq;  // C (Hz)
    private final double NORM;

    /**
     * @param bandwidth  B (controls time-frequency resolution)
     * @param centerFreq C (central frequency in Hz)
     */
    public ComplexMorletWavelet(double bandwidth, double centerFreq) {
        super("cmor" + bandwidth + "-" + centerFreq);
        this.bandwidth = bandwidth;
        this.centerFreq = centerFreq;
        this.NORM = 1.0 / Math.sqrt(Math.PI * bandwidth);
    }

    @Override
    public Complex value(double x) {
        double norm = Math.pow(Math.PI * bandwidth, -0.25);
        double envelope = norm * Math.exp(-x * x / bandwidth);
        double phase = 2 * Math.PI * centerFreq * x;
        return new Complex(envelope * Math.cos(phase), envelope * Math.sin(phase));
    }

    @Override
    public Complex[] generate(int n, double scale, double dt) {
        double[] t = linspace(n, dt);          // symmetric around 0
        Complex[] psi = new Complex[n];
        //double norm = Math.pow(Math.PI * bandwidth, -0.25) / Math.sqrt(scale);
        double invSqrtA = 1.0 / Math.sqrt(scale);

        for (int i = 0; i < n; i++) {
            double x = t[i] / scale;           // t/a
            double gauss = Math.exp(-x * x / bandwidth);
            double envelope = NORM * gauss * invSqrtA;
            double phase = 2 * Math.PI * centerFreq * x;
            psi[i] = new Complex(envelope * Math.cos(phase),
                    envelope * Math.sin(phase));
        }
        return psi;
    }

    @Override
    public Array[] waveFun(int precision) {
        // 1. Number of sampling points
        int numPoints = (int) Math.pow(2, precision);

        // 2. Define time grid range
        double step = (upperBound - lowerBound) / (numPoints - 1);

        Array psi = Array.factory(DataType.COMPLEX, new int[]{numPoints});
        Array x = Array.factory(DataType.DOUBLE, new int[]{numPoints});

        // 3. Compute wavelet values at each point
        for (int i = 0; i < numPoints; i++) {
            double t = lowerBound + i * step;
            x.setDouble(i, t);
            double gauss = Math.exp(-t * t / bandwidth);
            double envelope = NORM * gauss;
            double phase = 2 * Math.PI * centerFreq * t;
            psi.setComplex(i, new Complex(envelope * Math.cos(phase),
                    envelope * Math.sin(phase)));
        }

        // 4. Return wavelet values and grid
        return new Array[]{psi, x};
    }

    @Override
    public double centralFrequency() {
        return centerFreq;
    }

    // Getters (optional)
    public double getBandwidth() { return bandwidth; }
    public double getCenterFreq() { return centerFreq; }

    @Override
    public double sigma() {
        return Math.sqrt(bandwidth / 2.0);
    }

    // ======== Override for speed: build H(ω) analytically ========
    @Override
    public Array fillFilterFft(int waveLen, double scale, double dt, int fftLen) {
        double a = scale;
        double norm = Math.pow(Math.PI * bandwidth, -0.25) * Math.sqrt(2 * Math.PI * bandwidth * a);
        double factor = dt * norm;
        double df = 1.0 / (fftLen * dt);
        double twoPiC = 2.0 * Math.PI * centerFreq;
        Array hh = Array.factory(DataType.COMPLEX, new int[]{fftLen});

        for (int k = 0; k < fftLen; k++) {
            double f = k * df;
            if (k > fftLen / 2) f -= fftLen * df;   // negative freqs in standard layout
            double omega = 2.0 * Math.PI * f;
            double arg = a * omega - twoPiC;
            double gauss = Math.exp(-bandwidth * arg * arg / 4.0);
            Complex h = new Complex(factor * gauss, 0.0); // analytic wavelet → FT real & zero for ω<0
            hh.setComplex(k, h);
        }

        return hh;
    }
}
