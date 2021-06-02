# coding=utf-8

from .numeric import asarray, array
from ._ndarray import NDArray
from org.meteoinfo.ndarray.math import ArrayUtil

__all__ = ['ndim', 'take', 'searchsorted']

def ndim(a):
    """
    Return the number of dimensions of an array.
    Parameters
    ----------
    a : array_like
        Input array.  If it is not already an ndarray, a conversion is
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
    try:
        return a.ndim
    except AttributeError:
        return asarray(a).ndim

def take(a, indices, axis=None, out=None, mode='raise'):
    """
    Take elements from an array along an axis.
    When axis is not None, this function does the same thing as "fancy"
    indexing (indexing arrays using arrays); however, it can be easier to use
    if you need elements along a given axis. A call such as
    ``np.take(arr, indices, axis=3)`` is equivalent to
    ``arr[:,:,:,indices,...]``.
    Explained without fancy indexing, this is equivalent to the following use
    of `ndindex`, which sets each of ``ii``, ``jj``, and ``kk`` to a tuple of
    indices::
        Ni, Nk = a.shape[:axis], a.shape[axis+1:]
        Nj = indices.shape
        for ii in ndindex(Ni):
            for jj in ndindex(Nj):
                for kk in ndindex(Nk):
                    out[ii + jj + kk] = a[ii + (indices[jj],) + kk]
    Parameters
    ----------
    a : array_like (Ni..., M, Nk...)
        The source array.
    indices : array_like (Nj...)
        The indices of the values to extract.
        .. versionadded:: 1.8.0
        Also allow scalars for indices.
    axis : int, optional
        The axis over which to select values. By default, the flattened
        input array is used.
    out : ndarray, optional (Ni..., Nj..., Nk...)
        If provided, the result will be placed in this array. It should
        be of the appropriate shape and dtype. Note that `out` is always
        buffered if `mode='raise'`; use other modes for better performance.
    mode : {'raise', 'wrap', 'clip'}, optional
        Specifies how out-of-bounds indices will behave.
        * 'raise' -- raise an error (default)
        * 'wrap' -- wrap around
        * 'clip' -- clip to the range
        'clip' mode means that all indices that are too large are replaced
        by the index that addresses the last element along that axis. Note
        that this disables indexing with negative numbers.
    Returns
    -------
    out : ndarray (Ni..., Nj..., Nk...)
        The returned array has the same type as `a`.
    See Also
    --------
    compress : Take elements using a boolean mask
    ndarray.take : equivalent method
    take_along_axis : Take elements by matching the array and the index arrays
    Notes
    -----
    By eliminating the inner loop in the description above, and using `s_` to
    build simple slice objects, `take` can be expressed  in terms of applying
    fancy indexing to each 1-d slice::
        Ni, Nk = a.shape[:axis], a.shape[axis+1:]
        for ii in ndindex(Ni):
            for kk in ndindex(Nj):
                out[ii + s_[...,] + kk] = a[ii + s_[:,] + kk][indices]
    For this reason, it is equivalent to (but faster than) the following use
    of `apply_along_axis`::
        out = np.apply_along_axis(lambda a_1d: a_1d[indices], axis, a)
    Examples
    --------
    >>> a = [4, 3, 5, 7, 6, 8]
    >>> indices = [0, 1, 4]
    >>> np.take(a, indices)
    array([4, 3, 6])
    In this example if `a` is an ndarray, "fancy" indexing can be used.
    >>> a = np.array(a)
    >>> a[indices]
    array([4, 3, 6])
    If `indices` is not one dimensional, the output also has these dimensions.
    >>> np.take(a, [[0, 1], [2, 3]])
    array([[4, 3],
           [5, 7]])
    """
    if isinstance(a, (list, tuple)):
        a = array(a)

    return a.take(indices, axis=axis)

def searchsorted(a, v, side='left', sorter=None):
    """
    Find indices where elements should be inserted to maintain order.
    :param a: (*array_like*) Input 1-D array. If sorter is None, then it must be sorted in ascending order,
        otherwise sorter must be an array of indices that sort it.
    :param v: (*array_like*) Values to insert into a.
    :param side: (*str*) [left | right], default is 'left'. If ‘left’, the index of the first suitable location found is given.
        If ‘right’, return the last such index. If there is no suitable index, return either 0 or N (where N
        is the length of a).
    :param sorter: (*array_like*) Optional array of integer indices that sort array a into ascending order.
        They are typically the result of argsort.
    :return: (*array_like*) Array of insertion points with the same shape as v.
    """
    if isinstance(a, (list, tuple)):
        a = array(a).asarray()

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