# coding=utf-8

from .numeric import asarray, array, isscalar
from ._ndarray import NDArray
from .stride_tricks import broadcast_arrays
from org.meteoinfo.ndarray.math import ArrayUtil, ArrayMath


__all__ = ['clip','cumprod', 'cumsum', 'ndim', 'nonzero', 'prod', 'ravel', 'searchsorted', 'sum',
           'where']


def ndim(a):
    """
    Return the number of dimensions of an array.

    Parameters
    ----------
    a : array_like
        Input array.  If it is not already a ndarray, a conversion is
        attempted.

    Returns
    -------
    number_of_dimensions : int
        The number of dimensions in `a`.  Scalars are zero-dimensional.

    See Also
    --------
    ndarray.ndim : equivalent method
    shape : dimensions of array
    ndarray.shape : dimensions of array

    Examples
    --------
    >>> np.ndim([[1,2,3],[4,5,6]])
    2
    >>> np.ndim(np.array([[1,2,3],[4,5,6]]))
    2
    >>> np.ndim(1)
    0
    """
    if isscalar(a):
        return 0

    try:
        return a.ndim
    except AttributeError:
        return asarray(a).ndim


def ravel(a):
    """
    Return a contiguous flattened array.

    :param a: (*array*) Input array.
    :return: A contiguous flattened array.
    """
    if isinstance(a, (list, tuple)):
        a = array(a)

    return a.ravel()


def nonzero(a):
    """
    Return the indices of the elements that are non-zero.

    Returns a tuple of arrays, one for each dimension of a, containing the indices of the
    non-zero elements in that dimension.

    :param a: (*array_like*) Input array.

    :returns: (*tuple*) Indices of elements that are non-zero.
    """
    if isinstance(a, list):
        a = array(a)
    ra = ArrayUtil.nonzero(a.asarray())
    if ra is None:
        return tuple([NDArray([]).astype('int')])

    r = []
    for aa in ra:
        r.append(NDArray(aa))
    return tuple(r)


def where(condition, *args):
    """
    Return elements, either from x or y, depending on condition.

    If only condition is given, return condition.nonzero().

    Parameters
    ----------
    condition : `array_like, bool`
        Where True, yield x, otherwise yield y.

    x, y : `array_like`
        Values from which to choose. x, y and condition need to be broadcastable to some shape.

    Returns
    -------
    `array`
        An array with elements from x where condition is True, and elements from y elsewhere.
    """
    if len(args) == 0:
        return nonzero(condition)

    x = args[0]
    y = args[1]
    if isinstance(condition, bool):
        return x if condition else y
    else:
        condition = asarray(condition)
        x = asarray(x)
        y = asarray(y)
        x, y = broadcast_arrays(x, y)
        r = ArrayUtil.where(condition._array, x._array, y._array)
        return condition.array_wrap(r)


def searchsorted(a, v, side='left', sorter=None):
    """
    Find indices where elements should be inserted to maintain order.
    :param a: (*array_like*) Input 1-D array. If sorter is None, then it must be sorted in ascending order,
        otherwise sorter must be an array of indices that sort it.
    :param v: (*array_like*) Values to insert into a.
    :param side: (*str*) [left | right], default is `left`. If `left`, the index of the first suitable location found is given.
        If `right`, return the last such index. If there is no suitable index, return either 0 or N (where N
        is the length of a).
    :param sorter: (*array_like*) Optional array of integer indices that sort array a into ascending order.
        They are typically the result of argsort.
    :return: (*array_like*) Array of insertion points with the same shape as v.
    """
    if isinstance(a, (list, tuple)):
        a = array(a).asarray()
    elif isinstance(a, NDArray):
        a = a.asarray()

    if isinstance(v, (list, tuple)):
        v = array(v).asarray()
    elif isinstance(v, NDArray):
        v = v.asarray()

    left = True if side == 'left' else False
    r = ArrayUtil.searchSorted(a, v, left)
    if isinstance(r, int):
        return r
    else:
        return NDArray(r)


def sum(x, axis=None):
    """
    Sum of array elements over a given axis.

    :param x: (*array_like or list*) Input values.
    :param axis: (*int*) Axis along which the standard deviation is computed.
        The default is to compute the standard deviation of the flattened array.

    :returns: (*array_like*) Sum result
    """
    if isinstance(x, (list, tuple)):
        if isinstance(x[0], NDArray):
            a = []
            for xx in x:
                a.append(xx.asarray())
            r = ArrayMath.sum(a)
            return x[0].array_wrap(r)
        else:
            x = array(x)

    if axis is None:
        r = ArrayMath.sum(x.asarray())
        return r
    else:
        r = x.sum(axis)
        return x.array_wrap(r, axis)


def prod(x, axis=None):
    """
    Product of array elements over a given axis.

    :param x: (*array_like or list*) Input values.
    :param axis: (*int*) Axis along which the standard deviation is computed.
        The default is to compute the standard deviation of the flattened array.

    :returns: (*array_like*) Product result
    """
    if isinstance(x, (list, tuple)):
        x = array(x)

    if axis is None:
        r = ArrayMath.prod(x.asarray())
        return r
    else:
        r = ArrayMath.prod(x.asarray(), axis)
        x.array_wrap(r, axis)


def cumsum(a, axis=None):
    """
    Return the cumulative summary of elements along a given axis.

    Parameters
    ----------
    a : array_like
        Input array.
    axis : int, optional
        Axis along which the cumulative summary is computed. By default,
        the input is flattened.

    Returns
    -------
    cumsum : ndarray
        A new array holding the result is returned.
    """
    if axis is None:
        r = ArrayMath.cumsum(a._array)
    else:
        r = ArrayMath.cumsum(a._array, axis)

    return a.array_wrap(r, axis)


def cumprod(a, axis=None):
    """
    Return the cumulative product of elements along a given axis.

    Parameters
    ----------
    a : array_like
        Input array.
    axis : int, optional
        Axis along which the cumulative product is computed. By default,
        the input is flattened.

    Returns
    -------
    cumprod : ndarray
        A new array holding the result is returned.
    """
    if axis is None:
        r = ArrayMath.cumprod(a._array)
    else:
        r = ArrayMath.cumprod(a._array, axis)

    return a.array_wrap(r, axis)


def clip(a, a_min=None, a_max=None, **kwargs):
    """
    Clip (limit) the values in an array.

    Given an interval, values outside the interval are clipped to
    the interval edges.  For example, if an interval of ``[0, 1]``
    is specified, values smaller than 0 become 0, and values larger
    than 1 become 1.

    Equivalent to but faster than ``np.minimum(a_max, np.maximum(a, a_min))``.

    No check is performed to ensure ``a_min < a_max``.

    Parameters
    ----------
    a : array_like
        Array containing elements to clip.
    a_min, a_max : array_like or None
        Minimum and maximum value. If ``None``, clipping is not performed on
        the corresponding edge. If both ``a_min`` and ``a_max`` are ``None``,
        the elements of the returned array stay the same. Both are broadcasted
        against ``a``.
    **kwargs
        For other keyword-only arguments, see the
        :ref:`ufunc docs <ufuncs.kwargs>`.

    Returns
    -------
    clipped_array : ndarray
        An array with the elements of `a`, but where values
        < `a_min` are replaced with `a_min`, and those > `a_max`
        with `a_max`.

    See Also
    --------
    :ref:`ufuncs-output-type`

    Notes
    -----
    When `a_min` is greater than `a_max`, `clip` returns an
    array in which all values are equal to `a_max`,
    as shown in the second example.

    Examples
    --------
    >>> a = np.arange(10)
    >>> a
    array([0, 1, 2, 3, 4, 5, 6, 7, 8, 9])
    >>> np.clip(a, 1, 8)
    array([1, 1, 2, 3, 4, 5, 6, 7, 8, 8])
    >>> np.clip(a, 8, 1)
    array([1, 1, 1, 1, 1, 1, 1, 1, 1, 1])
    >>> np.clip(a, 3, 6, out=a)
    array([3, 3, 3, 3, 4, 5, 6, 6, 6, 6])
    >>> a
    array([3, 3, 3, 3, 4, 5, 6, 6, 6, 6])
    >>> a = np.arange(10)
    >>> a
    array([0, 1, 2, 3, 4, 5, 6, 7, 8, 9])
    >>> np.clip(a, [3, 4, 1, 1, 1, 4, 4, 4, 4, 4], 8)
    array([3, 4, 2, 3, 4, 5, 6, 7, 8, 8])

    """
    if isinstance(a_min, (list, tuple)):
        a_min = array(a_min)

    if isinstance(a_max, (list, tuple)):
        a_max = array(a_max)

    if isinstance(a_min, NDArray):
        a_min = a_min._array

    if isinstance(a_max, NDArray):
        a_max = a_max._array

    if a_min is None and a_max is None:
        return a
    elif a_min is None:
        r = ArrayUtil.clipMax(a._array, a_max)
    elif a_max is None:
        r = ArrayUtil.clipMin(a._array, a_min)
    else:
        r = ArrayUtil.clip(a._array, a_min, a_max)

    return a.array_wrap(r)
