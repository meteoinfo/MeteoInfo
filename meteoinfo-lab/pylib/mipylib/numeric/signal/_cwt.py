from org.meteoinfo.math.transform import CWT, MorletWavelet, MexicanHatWavelet, Gaussian1Wavelet

from .. import core as np


__all__ = ["cwt"]


def cwt(data, scales, wavelet, sampling_period=1., method='conv', axis=-1, precision=12):
    """
    One dimensional Continuous Wavelet Transform.

    Parameters
    ----------
    data : array_like
        Input signal
    scales : array_like
        The wavelet scales to use. One can use
        ``f = scale2frequency(wavelet, scale)/sampling_period`` to determine
        what physical frequency, ``f``. Here, ``f`` is in hertz when the
        ``sampling_period`` is given in seconds.
    wavelet : Wavelet object or name
        Wavelet to use
    sampling_period : float
        Sampling period for the frequencies output (optional).
        The values computed for ``coefs`` are independent of the choice of
        ``sampling_period`` (i.e. ``scales`` is not scaled by the sampling
        period).
    method : {'conv', 'fft'}, optional
        The method used to compute the CWT. Can be any of:
            - ``conv`` uses ``numpy.convolve``.
            - ``fft`` uses frequency domain convolution.
            - ``auto`` uses automatic selection based on an estimate of the
              computational complexity at each scale.

        The ``conv`` method complexity is ``O(len(scale) * len(data))``.
        The ``fft`` method is ``O(N * log2(N))`` with
        ``N = len(scale) + len(data) - 1``. It is well suited for large size
        signals but slightly slower than ``conv`` on small ones.
    axis: int, optional
        Axis over which to compute the CWT. If not given, the last axis is
        used.
    precision: int, optional
        Length of wavelet (``2 ** precision``) used to compute the CWT. Greater
        will increase resolution, especially for higher scales, but will
        compute a bit slower. Too low will distort coefficients and their
        norms, with a zipper-like effect. The default is 12, it's recommended
        to use >=12.

    Returns
    -------
    coefs : array_like
        Continuous wavelet transform of the input signal for the given scales
        and wavelet. The first axis of ``coefs`` corresponds to the scales.
        The remaining axes match the shape of ``data``.
    frequencies : array_like
        If the unit of sampling period are seconds and given, then frequencies
        are in hertz. Otherwise, a sampling period of 1 is assumed.

    Notes
    -----
    Size of coefficients arrays depends on the length of the input array and
    the length of given scales.

    Examples
    --------
    >>> x = np.exp(np.linspace(0, 2, 512))
    >>> y = np.cos(2*np.pi*x)  # exponential chirp
    >>> scales = np.logspace(np.log10(1), np.log10(128), 128)
    >>> coef, freqs = np.signal.cwt(y, scales, 'gaus1')
    >>> plt.matshow(coef)
    >>> plt.show()

    >>> import pywt
    >>> import numpy as np
    >>> import matplotlib.pyplot as plt
    >>> t = np.linspace(-1, 1, 200, endpoint=False)
    >>> sig  = np.cos(2 * np.pi * 7 * t) + np.real(np.exp(-7*(t-0.4)**2)*np.exp(1j*2*np.pi*2*(t-0.4)))
    >>> widths = np.logspace(np.log10(1), np.log10(30), 30)
    >>> cwtmatr, freqs = pywt.cwt(sig, widths, 'mexh')
    >>> plt.imshow(cwtmatr, extent=[-1, 1, 1, 31], cmap='PRGn', aspect='auto',
    ...            vmax=abs(cwtmatr).max(), vmin=-abs(cwtmatr).max())
    >>> plt.show()
    """
    data = np.asanyarray(data)
    scales = np.asanyarray(scales)
    wavelet = CWT.getWavelet(wavelet)
    if method == 'conv':
        wt = CWT.cwt(data._array, scales._array, wavelet, sampling_period, precision)
    else:
        wt = CWT.cwtFFT(data._array, scales._array, wavelet, sampling_period, precision)

    wt = np.NDArray(wt)
    if not wavelet.isComplex():
        wt = wt.real

    freqs = CWT.scalesToFrequencies(scales._array, wavelet, sampling_period)

    return wt, np.NDArray(freqs)
