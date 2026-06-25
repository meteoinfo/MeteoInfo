package org.meteoinfo.math.transform;

import org.meteoinfo.ndarray.Complex;

/**
 * Analytic wavelet defined in the frequency domain.
 * Only positive frequencies are non-zero to obtain an analytic signal.
 */
public interface Wavelet {
    /** Frequency domain function ψ̂(ω), where ω is angular frequency */
    Complex psiHat(double omega);

    /** Center angular frequency, used to convert scale to pseudo-frequency */
    double getCenterFrequency();
}
