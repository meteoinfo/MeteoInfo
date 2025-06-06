# coding=utf-8

from org.meteoinfo.image.ndimage import Correlate1D, ExtendMode, GaussianFilter, \
    UniformFilter, MaximumFilter, MinimumFilter

from ..core import numeric as np


__all__ = ['correlate1d', 'gaussian_filter', 'uniform_filter', 'maximum_filter',
           'minimum_filter']


def correlate1d(input, weights, axis=-1, mode='reflect', cval=0.0):
    """
    Calculate a 1-D correlation along the given axis.

    The lines of the array along the given axis are correlated with the
    given weights.

    Parameters
    ----------
    input : array
        The input array.
    weights : array
        1-D sequence of numbers.
    axis : int
        The axis of input along which to calculate. Default is -1.
    mode : {‘reflect’, ‘constant’, ‘nearest’, ‘mirror’, ‘wrap’}, optional
        The mode parameter determines how the input array is extended beyond its boundaries.
        Default is ‘reflect’. Behavior for each valid value is as follows:
        ‘reflect’ (d c b a | a b c d | d c b a)
            The input is extended by reflecting about the edge of the last pixel. This mode is
            also sometimes referred to as half-sample symmetric.
        ‘constant’ (k k k k | a b c d | k k k k)
            The input is extended by filling all values beyond the edge with the same constant
            value, defined by the cval parameter.
        ‘nearest’ (a a a a | a b c d | d d d d)
            The input is extended by replicating the last pixel.
        ‘mirror’ (d c b | a b c d | c b a)
            The input is extended by reflecting about the center of the last pixel. This mode
            is also sometimes referred to as whole-sample symmetric.
        ‘wrap’ (a b c d | a b c d | a b c d)
            The input is extended by wrapping around to the opposite edge.
    cval : scalar, optional
        Value to fill past edges of input if mode is ‘constant’. Default is 0.0.

    Returns
    -------
    result : array
        Correlation result. Has the same shape as `input`.

    Examples
    --------
    >>> from np.ndimage import correlate1d
    >>> correlate1d([2, 8, 0, 4, 1, 9, 9, 0], weights=[1, 3])
    array([ 8, 26,  8, 12,  7, 28, 36,  9])
    """
    input = np.asarray(input)
    weights = np.asarray(weights)
    mode = ExtendMode.valueOf(mode.upper())
    jc1d = Correlate1D(weights._array, mode)
    jc1d.setAxis(axis)
    jc1d.setCValue(cval)
    r = jc1d.correlate(input._array)

    return np.array(r)


def gaussian_filter(input, sigma, order=0, mode='reflect', cval=0.0, radius=1):
    """
    Multidimensional Gaussian filter.

    Parameters
    ----------
    input : array
        The input array
    sigma : scalar or sequence of scalars
        Standard deviation for Gaussian kernel. The standard
        deviations of the Gaussian filter are given for each axis as a
        sequence, or as a single number, in which case it is equal for
        all axes.
    order : int or sequence of ints, optional
        The order of the filter along each axis is given as a sequence
        of integers, or as a single number. An order of 0 corresponds
        to convolution with a Gaussian kernel. A positive order
        corresponds to convolution with that derivative of a Gaussian.
    mode : {‘reflect’, ‘constant’, ‘nearest’, ‘mirror’, ‘wrap’}, optional
        The mode parameter determines how the input array is extended beyond its boundaries.
        Default is ‘reflect’. Behavior for each valid value is as follows:
        ‘reflect’ (d c b a | a b c d | d c b a)
            The input is extended by reflecting about the edge of the last pixel. This mode is
            also sometimes referred to as half-sample symmetric.
        ‘constant’ (k k k k | a b c d | k k k k)
            The input is extended by filling all values beyond the edge with the same constant
            value, defined by the cval parameter.
        ‘nearest’ (a a a a | a b c d | d d d d)
            The input is extended by replicating the last pixel.
        ‘mirror’ (d c b | a b c d | c b a)
            The input is extended by reflecting about the center of the last pixel. This mode
            is also sometimes referred to as whole-sample symmetric.
        ‘wrap’ (a b c d | a b c d | a b c d)
            The input is extended by wrapping around to the opposite edge.
    cval : scalar, optional
        Value to fill past edges of input if mode is ‘constant’. Default is 0.0.
    radius : None or int or sequence of ints, optional
        Radius of the Gaussian kernel. The radius is given for each axis
        as a sequence, or as a single number, in which case it is equal
        for all axes. If specified, the size of the kernel along each axis
        will be ``2*radius + 1``, and `truncate` is ignored.
        Default is 1.

    Returns
    -------
    gaussian_filter : array
        Returned array of same shape as `input`.

    Notes
    -----
    The multidimensional filter is implemented as a sequence of
    1-D convolution filters. The intermediate arrays are
    stored in the same data type as the output. Therefore, for output
    types with a limited precision, the results may be imprecise
    because intermediate results may be stored with insufficient
    precision.

    The Gaussian kernel will have size ``2*radius + 1`` along each axis. If
    `radius` is None, the default ``radius = round(truncate * sigma)`` will be
    used.
    """
    input = np.asarray(input)
    mode = ExtendMode.valueOf(mode.upper())
    size = 2 * radius + 1
    g_filter = GaussianFilter(sigma, size, mode)
    g_filter.setCValue(cval)
    r = g_filter.filter(input._array)

    return np.array(r)


def uniform_filter(input, size=3, mode='reflect', cval=0.0):
    """
    Multidimensional uniform filter.

    Parameters
    ----------
    input : array
        The input array
    size : int or sequence of ints, optional
        The sizes of the uniform filter are given for each axis as a sequence, or as a single
        number, in which case the size is equal for all axes.
    mode : {‘reflect’, ‘constant’, ‘nearest’, ‘mirror’, ‘wrap’}, optional
        The mode parameter determines how the input array is extended beyond its boundaries.
        Default is ‘reflect’. Behavior for each valid value is as follows:
        ‘reflect’ (d c b a | a b c d | d c b a)
            The input is extended by reflecting about the edge of the last pixel. This mode is
            also sometimes referred to as half-sample symmetric.
        ‘constant’ (k k k k | a b c d | k k k k)
            The input is extended by filling all values beyond the edge with the same constant
            value, defined by the cval parameter.
        ‘nearest’ (a a a a | a b c d | d d d d)
            The input is extended by replicating the last pixel.
        ‘mirror’ (d c b | a b c d | c b a)
            The input is extended by reflecting about the center of the last pixel. This mode
            is also sometimes referred to as whole-sample symmetric.
        ‘wrap’ (a b c d | a b c d | a b c d)
            The input is extended by wrapping around to the opposite edge.
    cval : scalar, optional
        Value to fill past edges of input if mode is ‘constant’. Default is 0.0.

    Returns
    -------
    uniform_filter : array
        Returned array of same shape as `input`.

    Notes
    -----
    The multidimensional filter is implemented as a sequence of
    1-D uniform filters. The intermediate arrays are stored
    in the same data type as the output. Therefore, for output types
    with a limited precision, the results may be imprecise because
    intermediate results may be stored with insufficient precision.
    """
    input = np.asarray(input)
    mode = ExtendMode.valueOf(mode.upper())
    u_filter = UniformFilter(size, mode)
    u_filter.setCValue(cval)
    r = u_filter.filter(input._array)

    return np.array(r)


def minimum_filter(input, size=3, mode='reflect', cval=0.0):
    """
    Multidimensional minimum filter.

    Parameters
    ----------
    input : array
        The input array
    size : int or sequence of ints, optional
        The sizes of the uniform filter are given for each axis as a sequence, or as a single
        number, in which case the size is equal for all axes.
    mode : {‘reflect’, ‘constant’, ‘nearest’, ‘mirror’, ‘wrap’}, optional
        The mode parameter determines how the input array is extended beyond its boundaries.
        Default is ‘reflect’. Behavior for each valid value is as follows:
        ‘reflect’ (d c b a | a b c d | d c b a)
            The input is extended by reflecting about the edge of the last pixel. This mode is
            also sometimes referred to as half-sample symmetric.
        ‘constant’ (k k k k | a b c d | k k k k)
            The input is extended by filling all values beyond the edge with the same constant
            value, defined by the cval parameter.
        ‘nearest’ (a a a a | a b c d | d d d d)
            The input is extended by replicating the last pixel.
        ‘mirror’ (d c b | a b c d | c b a)
            The input is extended by reflecting about the center of the last pixel. This mode
            is also sometimes referred to as whole-sample symmetric.
        ‘wrap’ (a b c d | a b c d | a b c d)
            The input is extended by wrapping around to the opposite edge.
    cval : scalar, optional
        Value to fill past edges of input if mode is ‘constant’. Default is 0.0.

    Returns
    -------
    minimum_filter : array
        Returned array of same shape as `input`.
    """
    input = np.asarray(input)
    mode = ExtendMode.valueOf(mode.upper())
    u_filter = MinimumFilter(size, mode)
    u_filter.setCValue(cval)
    r = u_filter.filter(input._array)

    return np.array(r)


def maximum_filter(input, size=3, mode='reflect', cval=0.0):
    """
    Multidimensional maximum filter.

    Parameters
    ----------
    input : array
        The input array
    size : int or sequence of ints, optional
        The sizes of the uniform filter are given for each axis as a sequence, or as a single
        number, in which case the size is equal for all axes.
    mode : {‘reflect’, ‘constant’, ‘nearest’, ‘mirror’, ‘wrap’}, optional
        The mode parameter determines how the input array is extended beyond its boundaries.
        Default is ‘reflect’. Behavior for each valid value is as follows:
        ‘reflect’ (d c b a | a b c d | d c b a)
            The input is extended by reflecting about the edge of the last pixel. This mode is
            also sometimes referred to as half-sample symmetric.
        ‘constant’ (k k k k | a b c d | k k k k)
            The input is extended by filling all values beyond the edge with the same constant
            value, defined by the cval parameter.
        ‘nearest’ (a a a a | a b c d | d d d d)
            The input is extended by replicating the last pixel.
        ‘mirror’ (d c b | a b c d | c b a)
            The input is extended by reflecting about the center of the last pixel. This mode
            is also sometimes referred to as whole-sample symmetric.
        ‘wrap’ (a b c d | a b c d | a b c d)
            The input is extended by wrapping around to the opposite edge.
    cval : scalar, optional
        Value to fill past edges of input if mode is ‘constant’. Default is 0.0.

    Returns
    -------
    maximum_filter : array
        Returned array of same shape as `input`.
    """
    input = np.asarray(input)
    mode = ExtendMode.valueOf(mode.upper())
    u_filter = MaximumFilter(size, mode)
    u_filter.setCValue(cval)
    r = u_filter.filter(input._array)

    return np.array(r)
