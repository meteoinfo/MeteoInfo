package org.meteoinfo.math.transform;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Complex;
import org.meteoinfo.ndarray.DataType;

public abstract class Wavelet {
    protected final String name;
    protected boolean complex = true;
    protected double lowerBound = -8.0;
    protected double upperBound = 8.0;

    protected Wavelet(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isComplex() {
        return complex;
    }

    /** Evaluate wavelet psi(x) */
    public abstract Complex value(double x);

    /** Effective lower bound of the wavelet (e.g., -8) */
    public double getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(double value) {
        this.lowerBound = value;
    }

    /** Effective upper bound of the wavelet (e.g., 8) */
    public double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(double value) {
        this.upperBound = value;
    }

    /**
     * Generate sampled, scaled wavelet psi(t/a)/sqrt(a).
     * @param n      number of samples (odd, symmetric about 0)
     * @param scale  scale factor a (>0)
     * @param dt     sampling interval
     * @return complex array of length n, psi[i] corresponds to t[i] = (i - n/2)*dt
     */
    public abstract Complex[] generate(int n, double scale, double dt);

    /**
     * Get wavelet psi values
     * @param precision The precision
     * @return psi and x array
     */
    public abstract Array[] waveFun(int precision);

    /** Central frequency of the mother wavelet (scale=1). */
    public abstract double centralFrequency();

    public double sigma() {
        return 1.0;
    }

    // inside Wavelet class
    protected static double[] linspace(int n, double dt) {
        double[] t = new double[n];
        double tMin = -(n - 1) / 2.0 * dt;
        for (int i = 0; i < n; i++) {
            t[i] = tMin + i * dt;
        }
        return t;
    }

    /**
     * Fill the FFT of the convolution filter H[k] for this wavelet.
     * The default implementation creates time-domain samples and then FFTs them.
     * Override this for wavelets with known analytic Fourier transforms!
     *
     * @param waveLen time-domain length of the wavelet (odd)
     * @param scale  scale a
     * @param dt     sampling interval
     * @param fftLen FFT length (power of two)
     */
    public Array fillFilterFft(int waveLen,
                              double scale, double dt, int fftLen) {
        // Default: generate time-domain filter and FFT it
        Complex[] hTime = buildFilter(scale, dt, waveLen);
        Array hh = Array.factory(DataType.COMPLEX, new int[]{fftLen});
        for (int i = 0; i < waveLen; i++) {
            hh.setComplex(i, hTime[i]);
        }

        FastFourierTransform fft = new FastFourierTransform();
        return fft.apply(hh);
    }

    // Protected helper to build the time-domain filter
    protected Complex[] buildFilter(double scale, double dt, int waveLen) {
        Complex[] psi = generate(waveLen, scale, dt);
        Complex[] h = new Complex[waveLen];
        for (int k = 0; k < waveLen; k++) {
            h[k] = psi[waveLen - 1 - k].conj().multiply(dt);
        }
        return h;
    }
}
