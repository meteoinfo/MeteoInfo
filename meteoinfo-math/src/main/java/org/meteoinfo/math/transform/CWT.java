package org.meteoinfo.math.transform;


import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Complex;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.math.ArrayMath;

/**
 * Continuous Wavelet Transform (CWT) using frequency-domain convolution.
 * Implementation mimics the core algorithm of MATLAB's cwt() function.
 */
public class CWT {

    /**
     * Compute the CWT of a real signal.
     *
     * @param signal  real-valued input signal
     * @param dt      sampling period (seconds)
     * @param scales  array of scales (from smallest to largest)
     * @param wavelet analytic wavelet used for the transform
     * @return complex matrix [nScales][nTimes], each row corresponds to one scale
     */
    public static Array cwt(double[] signal, double dt, double[] scales, Wavelet wavelet) {
        int n = signal.length;
        int nScales = scales.length;

        // 1. Forward FFT of the signal (standard normalization, no scaling on forward transform)
        FastFourierTransform fft = new FastFourierTransform();
        Array fftSignal = fft.apply(signal);

        // 2. Build angular frequency array (positive and negative frequencies)
        double[] omega = new double[n];
        for (int i = 0; i <= n / 2; i++) {
            omega[i] = 2.0 * Math.PI * i / (n * dt);
        }
        for (int i = n / 2 + 1; i < n; i++) {
            omega[i] = -omega[n - i];
        }

        // 3. For each scale: multiply FFT(signal) by conj(ψ̂(sω)) and inverse FFT
        Array coeffs = Array.factory(DataType.COMPLEX, new int[]{nScales, n});
        for (int sIdx = 0; sIdx < nScales; sIdx++) {
            double s = scales[sIdx];
            Array prod = Array.factory(DataType.COMPLEX, new int[]{n});
            for (int i = 0; i < n; i++) {
                double w = omega[i];
                Complex psiHatScaled = wavelet.psiHat(s * w);
                // Convolution in time domain → multiplication in frequency domain,
                // taking the conjugate of the wavelet.
                prod.setComplex(i, fftSignal.getComplex(i).multiply(psiHatScaled.conj()));
            }
            // Inverse FFT to obtain CWT coefficients at this scale
            FastFourierTransform ifft = new FastFourierTransform();
            Array timeCoeffs = ifft.apply(prod);
            for (int i = 0; i < n; i++) {
                coeffs.setComplex(i + sIdx * n, timeCoeffs.getComplex(i));
            }
        }
        return coeffs;
    }

    /**
     * Generate logarithmically spaced scales similar to MATLAB default.
     *
     * @param dt       sampling period
     * @param n        signal length
     * @param wavelet  wavelet (used to determine frequency range via center frequency)
     * @param nVoices  number of scales per octave (default 10 or 12)
     * @return array of scales from smallest to largest
     */
    public static double[] logScales(double dt, int n, Wavelet wavelet, int nVoices) {
        double fNyq = 0.5 / dt;                          // Nyquist frequency
        double fMin = fNyq / n;                          // lowest resolvable frequency
        double sMax = wavelet.getCenterFrequency() / (2.0 * Math.PI * fMin);
        double sMin = 2.0;                               // smallest scale corresponds to ≥ 2 samples

        int nOctaves = (int) Math.floor(Math.log(sMax / sMin) / Math.log(2.0));
        int nScales = nOctaves * nVoices;
        double[] scales = new double[nScales];
        double base = Math.pow(2.0, 1.0 / nVoices);
        for (int i = 0; i < nScales; i++) {
            scales[i] = sMin * Math.pow(base, i);
        }
        return scales;
    }
}
