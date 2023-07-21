from ..core import numeric as _nx
from ..core import dtype
from ..core._ndarray import NDArray
from ..core.fromnumeric import (ravel, nonzero)
from org.meteoinfo.ndarray.math import ArrayMath

__all__ = ['angle','extract', 'place', 'grid_edge']


def extract(condition, arr):
    """
    Return the elements of an array that satisfy some condition.
    This is equivalent to ``np.compress(ravel(condition), ravel(arr))``.  If
    `condition` is boolean ``np.extract`` is equivalent to ``arr[condition]``.
    Note that `place` does the exact opposite of `extract`.
    Parameters
    ----------
    condition : array_like
        An array whose nonzero or True entries indicate the elements of `arr`
        to extract.
    arr : array_like
        Input array of the same size as `condition`.
    Returns
    -------
    extract : ndarray
        Rank 1 array of values from `arr` where `condition` is True.
    See Also
    --------
    take, put, copyto, compress, place
    Examples
    --------
    >>> arr = np.arange(12).reshape((3, 4))
    >>> arr
    array([[ 0,  1,  2,  3],
           [ 4,  5,  6,  7],
           [ 8,  9, 10, 11]])
    >>> condition = np.mod(arr, 3)==0
    >>> condition
    array([[ True, False, False,  True],
           [False, False,  True, False],
           [False,  True, False, False]])
    >>> np.extract(condition, arr)
    array([0, 3, 6, 9])
    If `condition` is boolean:
    >>> arr[condition]
    array([0, 3, 6, 9])
    """
    return _nx.take(ravel(arr), nonzero(ravel(condition))[0])


def place(arr, mask, vals):
    """
    Change elements of an array based on conditional and input values.
    Similar to ``np.copyto(arr, vals, where=mask)``, the difference is that
    `place` uses the first N elements of `vals`, where N is the number of
    True values in `mask`, while `copyto` uses the elements where `mask`
    is True.
    Note that `extract` does the exact opposite of `place`.

    Parameters
    ----------
    arr : ndarray
        Array to put data into.
    mask : array_like
        Boolean mask array. Must have the same size as `a`.
    vals : 1-D sequence
        Values to put into `a`. Only the first N elements are used, where
        N is the number of True values in `mask`. If `vals` is smaller
        than N, it will be repeated, and if elements of `a` are to be masked,
        this sequence must be non-empty.

    See Also
    --------
    copyto, put, take, extract

    Examples
    --------
    >>> arr = np.arange(6).reshape(2, 3)
    >>> np.place(arr, arr>2, [44, 55])
    >>> arr
    array([[ 0,  1,  2],
           [44, 55, 44]])
    """
    if not isinstance(arr, NDArray):
        raise TypeError("argument 1 must be numpy.ndarray, "
                        "not {name}".format(name=type(arr).__name__))

    if isinstance(vals, (list, tuple)):
        vals = NDArray(vals)

    ArrayMath.place(arr.asarray(), mask.asarray(), vals.asarray())


def grid_edge(x, y):
    """
    Return grid edge coordinate array.
    :param x: (*array*) X coordinate array with one dimension.
    :param y: (*array*) Y coordinate array width one dimension.
    :return: Grid edge coordinate array of x and y with one dimension.
    """
    yn = y.size
    xn = x.size
    n = (xn + yn) * 2
    xx = _nx.zeros(n)
    yy = _nx.zeros(n)
    xx[:xn] = x
    yy[:xn] = y[0]
    xx[xn:xn + yn] = x[-1]
    yy[xn:xn + yn] = y
    xx[xn + yn:xn + yn + xn] = x[::-1]
    yy[xn + yn:xn + yn + xn] = y[-1]
    xx[xn + yn + xn:] = x[0]
    yy[xn + yn + xn:] = y[::-1]

    return xx, yy

def angle(z, deg=False):
    """
    Return the angle of the complex argument.

    Parameters
    ----------
    z : array_like
        A complex number or sequence of complex numbers.
    deg : bool, optional
        Return angle in degrees if True, radians if False (default).

    Returns
    -------
    angle : ndarray or scalar
        The counterclockwise angle from the positive real axis on the complex
        plane in the range ``(-pi, pi]``, with dtype as double.

    See Also
    --------
    arctan2
    absolute

    Notes
    -----
    Although the angle of the complex number 0 is undefined, ``angle(0)``
    returns the value 0.

    Examples
    --------
    >>> np.angle([1.0, 1.0j, 1+1j])               # in radians
    array([ 0.        ,  1.57079633,  0.78539816]) # may vary
    >>> np.angle(1+1j, deg=True)                  # in degrees
    45.0

    """
    z = _nx.asanyarray(z)
    if z.dtype == dtype.complex:
        zimag = z.imag
        zreal = z.real
    else:
        zimag = 0
        zreal = z

    a = _nx.arctan2(zimag, zreal)
    if deg:
        a *= 180 / _nx.pi
    return a
