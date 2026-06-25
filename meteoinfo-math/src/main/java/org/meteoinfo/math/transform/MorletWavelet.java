package org.meteoinfo.math.transform;


import org.meteoinfo.ndarray.Complex;

/**
 * Analytic Morlet wavelet: ψ(t) = π^{-1/4} e^{iω0 t} e^{-t^2/2}, with default ω0 = 6.
 * Frequency domain:
 *   ψ̂(ω) = π^{-1/4} √2 e^{-(ω - ω0)^2 / 2}  for ω > 0,
 *   ψ̂(ω) = 0                               for ω ≤ 0.
 */
public class MorletWavelet implements Wavelet {
    private static final double PI = Math.PI;
    private final double omega0;
    private final double normFactor;   // π^{-1/4} * √2

    /** Default constructor with ω0 = 6. */
    public MorletWavelet() {
        this(6.0);
    }

    /** @param omega0 center angular frequency (usually ≥ 5) */
    public MorletWavelet(double omega0) {
        this.omega0 = omega0;
        this.normFactor = Math.pow(PI, -0.25) * Math.sqrt(2.0);
    }

    @Override
    public Complex psiHat(double omega) {
        if (omega <= 0) {
            return Complex.ZERO;   // analytic: only positive frequencies
        }
        double val = normFactor * Math.exp(-0.5 * (omega - omega0) * (omega - omega0));
        return new Complex(val, 0);
    }

    @Override
    public double getCenterFrequency() {
        return omega0;
    }
}
