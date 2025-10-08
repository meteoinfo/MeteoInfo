from org.meteoinfo.math.transform import FastFourierTransform, FastFourierTransform2D, \
    FastFourierTransformND

from .. import core as _nx


__all__ = ['fft', 'ifft', 'fft2', 'ifft2', 'fftn', 'ifftn']


def fft(a, axis=-1, norm=None):
    """
    Compute the one-dimensional discrete Fourier Transform.

    This function computes the one-dimensional *n*-point discrete Fourier
    Transform (DFT) with the efficient Fast Fourier Transform (FFT)
    algorithm [CT].

    Parameters
    ----------
    a : array_like
        Input array, can be complex.
    axis : int, optional
        Axis over which to compute the FFT.  If not given, the last axis is
        used.
    norm : {"backward", "ortho", "forward"}, optional
        Normalization mode. Default is "backward".
        Indicates which direction of the forward/backward pair of transforms
        is scaled and with what normalization factor.

    Returns
    -------
    out : complex ndarray
        The truncated or zero-padded input, transformed along the axis
        indicated by `axis`, or the last one if `axis` is not specified.

    References
    ----------
    .. [CT] Cooley, James W., and John W. Tukey, 1965, "An algorithm for the
            machine calculation of complex Fourier series," *Math. Comput.*
            19: 297-301.
    """
    a = _nx.asarray(a)
    jfft = FastFourierTransform()
    if norm is not None:
        jfft.setNormalization(norm)

    r = jfft.apply(a._array, axis)

    return _nx.NDArray(r)


def ifft(a, axis=-1, norm=None):
    """
    Compute the one-dimensional inverse discrete Fourier Transform.

    This function computes the inverse of the one-dimensional *n*-point
    discrete Fourier transform computed by `fft`.  In other words,
    ``ifft(fft(a)) == a`` to within numerical accuracy.
    For a general description of the algorithm and definitions.

    The input should be ordered in the same way as is returned by `fft`,
    i.e.,

    Parameters
    ----------
    a : array_like
        Input array, can be complex.
    axis : int, optional
        Axis over which to compute the inverse DFT.  If not given, the last
        axis is used.
    norm : {"backward", "ortho", "forward"}, optional
        Normalization mode. Default is "backward".
        Indicates which direction of the forward/backward pair of transforms
        is scaled and with what normalization factor.

    Returns
    -------
    out : complex ndarray
        The truncated or zero-padded input, transformed along the axis
        indicated by `axis`, or the last one if `axis` is not specified.
    """
    a = _nx.asarray(a)
    jfft = FastFourierTransform(True)
    if norm is not None:
        jfft.setNormalization(norm)

    r = jfft.apply(a._array, axis)

    return _nx.NDArray(r)


def fft2(a, axes=(-2, -1), norm=None):
    """
    Compute the 2-dimensional discrete Fourier Transform.

    This function computes the *n*-dimensional discrete Fourier Transform
    over any axes in an *M*-dimensional array by means of the
    Fast Fourier Transform (FFT).  By default, the transform is computed over
    the last two axes of the input array, i.e., a 2-dimensional FFT.

    Parameters
    ----------
    a : array_like
        Input array, can be complex
    axes : sequence of ints, optional
        Axes over which to compute the FFT.  If not given, the last two
        axes are used.  A repeated index in `axes` means the transform over
        that axis is performed multiple times.  A one-element sequence means
        that a one-dimensional FFT is performed. Default: ``(-2, -1)``.
    norm : {"backward", "ortho", "forward"}, optional
        Normalization mode. Default is "backward".
        Indicates which direction of the forward/backward pair of transforms
        is scaled and with what normalization factor.

    Returns
    -------
    out : complex ndarray
        The truncated or zero-padded input, transformed along the axes
        indicated by `axes`, or the last two axes if `axes` is not given.
    """
    a = _nx.asarray(a)
    jfft = FastFourierTransformND()
    if norm is not None:
        jfft.setNormalization(norm)

    r = jfft.apply(a._array, axes)

    return _nx.NDArray(r)


def ifft2(a, axes=(-2, -1), norm=None):
    """
    Compute the 2-dimensional inverse discrete Fourier Transform.

    This function computes the inverse of the 2-dimensional discrete Fourier
    Transform over any number of axes in an M-dimensional array by means of
    the Fast Fourier Transform (FFT).  In other words, ``ifft2(fft2(a)) == a``
    to within numerical accuracy.  By default, the inverse transform is
    computed over the last two axes of the input array.

    The input, analogously to `ifft`, should be ordered in the same way as is
    returned by `fft2`, i.e. it should have the term for zero frequency
    in the low-order corner of the two axes, the positive frequency terms in
    the first half of these axes, the term for the Nyquist frequency in the
    middle of the axes and the negative frequency terms in the second half of
    both axes, in order of decreasingly negative frequency.

    Parameters
    ----------
    a : array_like
        Input array, can be complex.
    axes : sequence of ints, optional
        Axes over which to compute the FFT.  If not given, the last two
        axes are used.  A repeated index in `axes` means the transform over
        that axis is performed multiple times.  A one-element sequence means
        that a one-dimensional FFT is performed. Default: ``(-2, -1)``.
    norm : {"backward", "ortho", "forward"}, optional
        Normalization mode. Default is "backward".
        Indicates which direction of the forward/backward pair of transforms
        is scaled and with what normalization factor.

    Returns
    -------
    out : complex ndarray
        The truncated or zero-padded input, transformed along the axes
        indicated by `axes`, or the last two axes if `axes` is not given.
    """
    a = _nx.asarray(a)
    jfft = FastFourierTransformND(True)
    if norm is not None:
        jfft.setNormalization(norm)

    r = jfft.apply(a._array, axes)

    return _nx.NDArray(r)


def fftn(a, axes=None, norm=None):
    """
    Compute the N-dimensional discrete Fourier Transform.

    This function computes the *N*-dimensional discrete Fourier Transform over
    any number of axes in an *M*-dimensional array by means of the Fast Fourier
    Transform (FFT).

    Parameters
    ----------
    a : array_like
        Input array, can be complex
    axes : sequence of ints, optional
        Axes over which to compute the FFT.  If not given, the last ``len(s)``
        axes are used, or all axes if `s` is also not specified.
        Repeated indices in `axes` means that the transform over that axis is
        performed multiple times.
    norm : {"backward", "ortho", "forward"}, optional
        Normalization mode (see `numpy.fft`). Default is "backward".
        Indicates which direction of the forward/backward pair of transforms
        is scaled and with what normalization factor.

    Returns
    -------
    out : complex ndarray
        The truncated or zero-padded input, transformed along the axes
        indicated by `axes`, or the all axes if `axes` is not given.
    """
    a = _nx.asarray(a)
    jfft = FastFourierTransformND()
    if norm is not None:
        jfft.setNormalization(norm)

    if axes is None:
        r = jfft.apply(a._array)
    else:
        r = jfft.apply(a._array, axes)

    return _nx.NDArray(r)


def ifftn(a, axes=None, norm=None):
    """
    Compute the n-dimensional inverse discrete Fourier Transform.

    This function computes the inverse of the n-dimensional discrete Fourier
    Transform over any number of axes in an M-dimensional array by means of
    the Fast Fourier Transform (FFT).  In other words, ``ifftn(fftn(a)) == a``
    to within numerical accuracy.  By default, the inverse transform is
    computed over the last two axes of the input array.

    The input, analogously to `ifft`, should be ordered in the same way as is
    returned by `fftn`, i.e. it should have the term for zero frequency
    in the low-order corner of the two axes, the positive frequency terms in
    the first half of these axes, the term for the Nyquist frequency in the
    middle of the axes and the negative frequency terms in the second half of
    both axes, in order of decreasingly negative frequency.

    Parameters
    ----------
    a : array_like
        Input array, can be complex.
    axes : sequence of ints, optional
        Axes over which to compute the IFFT.  If not given, the last ``len(s)``
        axes are used, or all axes if `s` is also not specified.
        Repeated indices in `axes` means that the inverse transform over that
        axis is performed multiple times.
    norm : {"backward", "ortho", "forward"}, optional
        Normalization mode (see `numpy.fft`). Default is "backward".
        Indicates which direction of the forward/backward pair of transforms
        is scaled and with what normalization factor.

    Returns
    -------
    out : complex ndarray
        The truncated or zero-padded input, transformed along the axes
        indicated by `axes`, or the last two axes if `axes` is not given.
    """
    a = _nx.asarray(a)
    jfft = FastFourierTransformND(True)
    if norm is not None:
        jfft.setNormalization(norm)

    if axes is None:
        r = jfft.apply(a._array)
    else:
        r = jfft.apply(a._array, axes)

    return _nx.NDArray(r)
