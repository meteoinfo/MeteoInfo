package org.meteoinfo.math.transform;


import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Complex;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;
import org.meteoinfo.ndarray.math.ArrayMath;
import org.meteoinfo.math.transform.FastFourierTransform.Norm;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.util.*;
import java.util.stream.IntStream;

public class CWT {

    /**
     * Get Wavelet from name
     * @param wtName The wavelet name
     * @return The Wavelet
     */
    public static Wavelet getWavelet(String wtName) {
        switch (wtName.toLowerCase()) {
            case "gaus1":
                return new Gaussian1Wavelet();
            case "mexh":
                return new MexicanHatWavelet();
            case "morl":
                return new MorletWavelet();
            case "cmor1.5-1.0":
                return new ComplexMorletWavelet(1.5, 1.0);
            default:
                return null;
        }
    }

    /**
     * Continuous Wavelet Transform.
     *
     * @param signal  input signal (length N)
     * @param scales  array of scales (>0)
     * @param wavelet mother wavelet instance
     * @param dt      sampling interval
     * @param precision Length of wavelet (``2 ** precision``) used to compute the CWT
     * @return matrix [scales.length][N] of complex CWT coefficients
     */
    public static Array cwt_bak(Array signal, Array scales,
                            Wavelet wavelet, double dt, int precision) {
        signal = signal.copyIfView();
        scales = scales.copyIfView();

        int N = (int) signal.getSize();
        int nScales = (int) scales.getSize();
        Array coeffs = Array.factory(DataType.COMPLEX, new int[]{nScales, N});

        for (int s = 0; s < nScales; s++) {
            double scale = scales.getDouble(s);
            double sqrtScale = Math.sqrt(scale);
            // Determine wavelet support length (M = 5 covers Gaussian tails)
            int waveLen = waveletLength(s, precision, N);
            Complex[] psi = wavelet.generate(waveLen, scale, dt);
            int halfLen = waveLen / 2;

            // Convolution: signal * reversed(conj(psi))
            for (int n = 0; n < N; n++) {
                Complex sum = new Complex(0, 0);
                for (int k = 0; k < waveLen; k++) {
                    int idx = n - (k - halfLen);
                    if (idx >= 0 && idx < N) {
                        Complex psiConj = psi[waveLen - 1 - k].conj(); // reverse + conj
                        sum = sum.add(psiConj.multiply(signal.getDouble(idx)));
                    }
                }
                //coeffs.setComplex(s * N + n, sum.multiply(dt));
                coeffs.setComplex(s * N + n, sum);
                //coeffs.setComplex(s * N + n, sum.multiply(sqrtScale));
            }
        }
        return coeffs;
    }

    /**
     * Continuous Wavelet Transform.
     *
     * @param signal  input signal (length N)
     * @param scales  array of scales (>0)
     * @param wavelet mother wavelet instance
     * @param dt      sampling interval
     * @param precision Length of wavelet (``2 ** precision``) used to compute the CWT
     * @return matrix [scales.length][N] of complex CWT coefficients
     */
    public static Array cwt(Array signal, Array scales,
                            Wavelet wavelet, double dt, int precision) {
        signal = signal.copyIfView();
        scales = scales.copyIfView();

        int N = (int) signal.getSize();
        int nScales = (int) scales.getSize();
        Array[] r = integrateWavelet(wavelet, precision);
        Array intPsi = r[0];
        Array x = r[1];
        int nPsi = (int) intPsi.getSize();
        if (intPsi.getDataType() ==  DataType.COMPLEX) {
            intPsi = ArrayMath.conj(intPsi);
        }
        double step = x.getDouble(1) - x.getDouble(0);
        double range = x.getDouble(nPsi - 1) - x.getDouble(0);
        Array coeffs = Array.factory(intPsi.getDataType(), new int[]{nScales, N});
        for (int i = 0; i < nScales; i++) {
            double scale = scales.getDouble(i);
            double sqrtScale = Math.sqrt(scale);
            List<Integer> j = new ArrayList<>();
            double jd = 0.0;
            double max = scale * range + 1;
            while (jd < max) {
                int idx = (int) (jd / (scale * step));
                if (idx >= nPsi) {
                    break;
                }
                j.add(idx);
                jd += 1;
            }

            int waveLen = j.size();
            Array intPsiScale = Array.factory(intPsi.getDataType(), new int[]{waveLen});
            for (int k = 0; k < waveLen; k++) {
                intPsiScale.setObject(waveLen - k - 1, intPsi.getObject(j.get(k)));
            }

            // Convolution: signal * reversed(conj(psi))
            if (intPsiScale.getDataType() == DataType.COMPLEX) {
                intPsiScale = ArrayMath.conj(intPsiScale);
            }
            Array conv = ArrayMath.convolveFull(signal, intPsiScale);
            conv = ArrayMath.mul(diff(conv), -sqrtScale);

            int d = ((int) conv.getSize() - N) / 2;
            for (int n = 0; n < N; n++) {
                coeffs.setObject(i * N + n, conv.getObject(n + d));
            }

        }
        return coeffs;
    }

    // --- FFT‑based CWT ---
    public static Array cwtFFT(Array signal, Array scales, Wavelet wavelet, double dt, int precision) {
        signal = signal.copyIfView();
        scales = scales.copyIfView();

        int N = (int) signal.getSize();
        int nScales = (int) scales.getSize();
        Array[] r = integrateWavelet(wavelet, precision);
        Array intPsi = r[0];
        Array x = r[1];
        int nPsi = (int) intPsi.getSize();
        if (intPsi.getDataType() ==  DataType.COMPLEX) {
            intPsi = ArrayMath.conj(intPsi);
        }
        double step = x.getDouble(1) - x.getDouble(0);
        double range = x.getDouble(nPsi - 1) - x.getDouble(0);
        Array coeffs = Array.factory(intPsi.getDataType(), new int[]{nScales, N});
        int size_scale0 = -1;
        Array fft_data;
        for (int i = 0; i < nScales; i++) {
            double scale = scales.getDouble(i);
            double sqrtScale = Math.sqrt(scale);
            List<Integer> j = new ArrayList<>();
            double jd = 0.0;
            double max = scale * range + 1;
            while (jd < max) {
                int idx = (int) (jd / (scale * step));
                if (idx >= nPsi) {
                    break;
                }
                j.add(idx);
                jd += 1;
            }

            int waveLen = j.size();
            int halfLen = waveLen / 2;
            Array intPsiScale = Array.factory(intPsi.getDataType(), new int[]{waveLen});
            for (int k = 0; k < waveLen; k++) {
                intPsiScale.setObject(waveLen - k - 1, intPsi.getObject(j.get(k)));
            }

            // Zero‑pad signal and kernel to length L = N + waveLen - 1
            int L = N + waveLen - 1;
            int fftLen = nextPowerOf2(L);   // MeteoInfo works with any length,
            // but powers of two are faster

            Array xx =  Array.factory(DataType.COMPLEX, new int[]{fftLen});
            for (int k = 0; k < N; k++) {
                xx.setComplex(k, signal.getComplex(k));
            }
            Array hh = Array.factory(DataType.COMPLEX, new int[]{fftLen});
            for (int k = 0; k < waveLen; k++) {
                hh.setComplex(k, intPsiScale.getComplex(k));
            }

            // Forward FFT (inverse = false, no scaling)
            FastFourierTransform fft = new FastFourierTransform();
            xx = fft.apply(xx);
            hh = fft.apply(hh);

            // Complex multiplication
            for (int k = 0; k < fftLen; k++) {
                xx.setComplex(k, xx.getComplex(k).multiply(hh.getComplex(k)));
            }

            // Inverse FFT (inverse = true, MeteoInfo scales by 1/n)
            FastFourierTransform fft2 = new FastFourierTransform(true);
            xx = fft2.apply(xx);

            xx = ArrayMath.mul(diff(xx), -sqrtScale);

            // Extract central N samples (convolution ‘same’ mode)
            int start = halfLen;
            if (coeffs.getDataType() == DataType.COMPLEX) {
                for (int n = 0; n < N; n++) {
                    coeffs.setComplex(i * N + n, xx.getComplex(start + n));
                }
            } else {
                for (int n = 0; n < N; n++) {
                    coeffs.setDouble(i * N + n, xx.getComplex(start + n).real());
                }
            }
        }
        return coeffs;
    }

    public static Array cwtFFT_bak (Array signal, Array scales, Wavelet wavelet, double dt, int precision) {
        signal = signal.copyIfView();
        scales = scales.copyIfView();
        int N = (int) signal.getSize();
        int nScales = (int) scales.getSize();
        Array coeffs = Array.factory(DataType.COMPLEX, new int[]{nScales, N});

        for (int s = 0; s < nScales; s++) {
            double a = scales.getDouble(s);

            // Wavelet support length (odd, symmetric)
            int waveLen = waveletLength(a, precision, N);
            int halfLen = waveLen / 2;

            // Generate scaled wavelet
            Complex[] psi = wavelet.generate(waveLen, a, dt);

            // Build convolution kernel h[m] = dt * conj(psi[-m])
            // (reversed + conjugated)
            Array h = Array.factory(DataType.COMPLEX, new int[]{waveLen});
            for (int m = 0; m < waveLen; m++) {
                //h.setComplex(m, psi[waveLen - 1 - m].conj().multiply(dt));
                h.setComplex(m, psi[waveLen - 1 - m].conj());
            }

            // Zero‑pad signal and kernel to length L = N + waveLen - 1
            int L = N + waveLen - 1;
            int fftLen = nextPowerOf2(L);   // MeteoInfo works with any length,
            // but powers of two are faster

            Array xx =  Array.factory(DataType.COMPLEX, new int[]{fftLen});
            for (int i = 0; i < N; i++) {
                xx.setComplex(i, signal.getComplex(i));
            }
            Array hh = Array.factory(DataType.COMPLEX, new int[]{fftLen});
            for (int i = 0; i < waveLen; i++) {
                hh.setComplex(i, h.getComplex(i));
            }

            // Forward FFT (inverse = false, no scaling)
            FastFourierTransform fft = new FastFourierTransform();
            xx = fft.apply(xx);
            hh = fft.apply(hh);

            // Complex multiplication
            for (int i = 0; i < fftLen; i++) {
                xx.setComplex(i, xx.getComplex(i).multiply(hh.getComplex(i)));
            }

            // Inverse FFT (inverse = true, MeteoInfo scales by 1/n)
            FastFourierTransform fft2 = new FastFourierTransform(true);
            xx = fft2.apply(xx);

            // Extract central N samples (convolution ‘same’ mode)
            int start = halfLen;
            for (int n = 0; n < N; n++) {
                coeffs.setComplex(s * N + n, xx.getComplex(start + n));
            }
        }
        return coeffs;
    }

    /** parallel = true → uses parallel streams */
    public static Array cwtFFT(Array signal, Array scales, Wavelet wavelet, double dt,
                               int precision, boolean parallel) {
        signal = signal.copyIfView();
        scales = scales.copyIfView();
        int N = (int) signal.getSize();
        int nScales = (int) scales.getSize();
        Array coeffs = Array.factory(DataType.COMPLEX, new int[]{nScales, N});

        // 1. Pre-compute signal FFT (done only once!)
        int fftLen = signalFftLength(N, scales, precision);
        //int fftLen = nextPowerOf2(N * 2);
        Array sig =  Array.factory(DataType.COMPLEX, new int[]{fftLen});
        for (int i = 0; i < N; i++) {
            sig.setComplex(i, signal.getComplex(i));
        }
        FastFourierTransform fft = new FastFourierTransform();
        sig = fft.apply(sig);

        // 2. Process scales (parallel or sequential)
        IntStream scaleStream = IntStream.range(0, nScales);
        if (parallel) scaleStream = scaleStream.parallel();

        Array finalScales = scales;
        Array finalSig = sig;
        scaleStream.forEach(s -> {
            double a = finalScales.getDouble(s);
            int waveLen = waveletLength(a, precision, N);
            int halfLen = waveLen / 2;

            // Build filter FFT directly (very fast for analytic wavelets)
            Array hh = wavelet.fillFilterFft(waveLen, a, dt, fftLen);

            // Multiply signal FFT and filter FFT
            Array prod = Array.factory(DataType.COMPLEX, new int[]{fftLen});
            for (int i = 0; i < fftLen; i++) {
                prod.setComplex(i, finalSig.getComplex(i).multiply(hh.getComplex(i)));
            }

            // Inverse FFT
            FastFourierTransform fft2 = new FastFourierTransform(true);
            prod = fft2.apply(prod);

            // Extract central N samples (convolution ‘same’ mode)
            int start = halfLen;
            for (int n = 0; n < N; n++) {
                coeffs.setComplex(s * N + n, prod.getComplex(start + n));
            }
        });

        return coeffs;
    }

    /**
     * Integrate for a wavelet
     * @param wavelet The wavelet
     * @param precision The precision
     * @return psi and x array
     */
    public static Array[] integrateWavelet(Wavelet wavelet, int precision) {
        Array[] r = wavelet.waveFun(precision);
        Array psi = r[0];
        Array x = r[1];
        Array intPsi = integrate(psi, x);

        return new Array[]{intPsi, x};
    }

    /**
     * Equivalent to pywt._integrate(y, x).
     * Computes the cumulative integral of y with respect to x using the trapezoidal rule.
     *
     * @param psi Function values at sample points (arbitrary discrete signal).
     * @param x Corresponding independent variable sample points.
     *          Does NOT need to be uniformly spaced (unlike integrate_wavelet).
     * @return Cumulative integral array where result[i] = ∫[x[0]]^{x[i]} y(τ) dτ.
     *         result[0] is always 0.0.
     * @throws IllegalArgumentException if arrays have mismatched lengths or fewer than 2 elements.
     */
    public static Array integrate(Array psi, Array x) {
        int n = (int) psi.getSize();
        // Compute uniform sampling interval (assumes equally-spaced x, consistent with pywt.wavefun output)
        double dt = x.getDouble(1) - x.getDouble(0);

        Array intPsi = Array.factory(psi.getDataType(), psi.getShape());
        if (psi.getDataType() == DataType.COMPLEX) {
            Complex v = Complex.ZERO;
            for (int i = 0; i < n; i++) {
                v = v.add(psi.getComplex(i));
                intPsi.setComplex(i, v.multiply(dt));
            }
        } else {
            double v = 0.0;
            for (int i = 0; i < n; i++) {
                v += psi.getDouble(i);
                intPsi.setDouble(i, v * dt);
            }
        }

        return intPsi;
    }

    /**
     * Generate linearly spaced array.
     */
    private static double[] linspace(double start, double end, int num) {
        double[] result = new double[num];
        double step = (end - start) / (num - 1);
        for (int i = 0; i < num; i++) {
            result[i] = start + i * step;
        }
        return result;
    }

    /**
     * First-order difference for double array: result[i] = arr[i+1] - arr[i]
     */
    private static Array diff(Array arr) {
        arr = arr.copyIfView();
        DataType dataType = (arr.getDataType() == DataType.COMPLEX) ? DataType.COMPLEX : DataType.DOUBLE;
        int n = (int) arr.getSize();
        Array result = Array.factory(dataType, new int[]{n - 1});
        if (dataType == DataType.COMPLEX) {
            for (int i = 0; i < n - 1; i++) {
                result.setComplex(i, arr.getComplex(i + 1).subtract(arr.getComplex(i)));
            }
        } else {
            for (int i = 0; i < n - 1; i++) {
                result.setDouble(i, arr.getDouble(i + 1) - arr.getDouble(i));
            }
        }
        return result;
    }

    private static int signalFftLength(int N, Array scales, int precision) {
        int maxWaveLen = 0;
        IndexIterator iter = scales.getIndexIterator();
        while (iter.hasNext()) {
            double a = iter.getDoubleNext();
            int wl = waveletLength(a, precision, N);
            if (wl > maxWaveLen) maxWaveLen = wl;
        }
        int L = N + maxWaveLen - 1;
        return nextPowerOf2(L);
    }

    private static int waveletLength(double scale, int precision, int N) {
        //int len = (int) Math.min(Math.ceil(scale * precision * 2) + 1, N);
        int len = (int) Math.ceil(scale * precision * 2) + 1;
        if (len % 2 == 0) len++;
        return len;
    }

    public static int nextPowerOf2(int n) {
        int p = 1;
        while (p < n) p <<= 1;
        return p;
    }

    /** Scales → pseudo‑frequencies (Hz) */
    public static Array scalesToFrequencies(Array scales, Wavelet wavelet, double dt) {
        double fc = wavelet.centralFrequency();
        Array freqs = Array.factory(DataType.DOUBLE, new int[]{(int) scales.getSize()});
        for (int i = 0; i < scales.getSize(); i++) {
            freqs.setDouble(i, fc / (scales.getDouble(i) * dt));
        }
        return freqs;
    }

}
