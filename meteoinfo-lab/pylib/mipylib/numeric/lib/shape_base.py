from ..core.numeric import asanyarray, normalize_axis_tuple

__all__ = [
    'expand_dims'
]

def expand_dims(a, axis):
    """
    Expand the shape of an array.
    Insert a new axis that will appear at the `axis` position in the expanded
    array shape.
    Parameters
    ----------
    a : array_like
        Input array.
    axis : int or tuple of ints
        Position in the expanded axes where the new axis (or axes) is placed.
        .. deprecated:: 1.13.0
            Passing an axis where ``axis > a.ndim`` will be treated as
            ``axis == a.ndim``, and passing ``axis < -a.ndim - 1`` will
            be treated as ``axis == 0``. This behavior is deprecated.
        .. versionchanged:: 1.18.0
            A tuple of axes is now supported.  Out of range axes as
            described above are now forbidden and raise an `AxisError`.
    Returns
    -------
    result : ndarray
        View of `a` with the number of dimensions increased.
    See Also
    --------
    squeeze : The inverse operation, removing singleton dimensions
    reshape : Insert, remove, and combine dimensions, and resize existing ones
    doc.indexing, atleast_1d, atleast_2d, atleast_3d
    Examples
    --------
    >>> x = np.array([1, 2])
    >>> x.shape
    (2,)
    The following is equivalent to ``x[np.newaxis, :]`` or ``x[np.newaxis]``:
    >>> y = np.expand_dims(x, axis=0)
    >>> y
    array([[1, 2]])
    >>> y.shape
    (1, 2)
    The following is equivalent to ``x[:, np.newaxis]``:
    >>> y = np.expand_dims(x, axis=1)
    >>> y
    array([[1],
           [2]])
    >>> y.shape
    (2, 1)
    ``axis`` may also be a tuple:
    >>> y = np.expand_dims(x, axis=(0, 1))
    >>> y
    array([[[1, 2]]])
    >>> y = np.expand_dims(x, axis=(2, 0))
    >>> y
    array([[[1],
            [2]]])
    Note that some examples may use ``None`` instead of ``np.newaxis``.  These
    are the same objects:
    >>> np.newaxis is None
    True
    """
    a = asanyarray(a)

    if type(axis) not in (tuple, list):
        axis = (axis,)

    out_ndim = len(axis) + a.ndim
    axis = normalize_axis_tuple(axis, out_ndim)

    shape_it = iter(a.shape)
    shape = [1 if ax in axis else next(shape_it) for ax in range(out_ndim)]

    return a.reshape(shape)