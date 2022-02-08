import mipylib.numeric as np

__all__ = ['resample_nn_1d','nearest_intersection_idx','first_derivative','gradient']

def resample_nn_1d(a, centers):
    """Return one-dimensional nearest-neighbor indexes based on user-specified centers.
    Parameters
    ----------
    a : array-like
        1-dimensional array of numeric values from which to extract indexes of
        nearest-neighbors
    centers : array-like
        1-dimensional array of numeric values representing a subset of values to approximate
    Returns
    -------
        A list of indexes (in type given by `array.argmin()`) representing values closest to
        given array values.
    """
    ix = []
    for center in centers:
        index = (np.abs(a - center)).argmin()
        if index not in ix:
            ix.append(index)
    return ix

def nearest_intersection_idx(a, b):
    """Determine the index of the point just before two lines with common x values.
    Parameters
    ----------
    a : array-like
        1-dimensional array of y-values for line 1
    b : array-like
        1-dimensional array of y-values for line 2
    Returns
    -------
        An array of indexes representing the index of the values
        just before the intersection(s) of the two lines.
    """
    # Difference in the two y-value sets
    difference = a - b

    # Determine the point just before the intersection of the lines
    # Will return multiple points for multiple intersections
    sign_change_idx, = np.nonzero(np.diff(np.sign(difference)))

    return sign_change_idx

def _remove_nans(*variables):
    """Remove NaNs from arrays that cause issues with calculations.
    Takes a variable number of arguments and returns masked arrays in the same
    order as provided.
    """
    mask = None
    for v in variables:
        if mask is None:
            mask = np.isnan(v)
        else:
            mask |= np.isnan(v)

    # Mask everyone with that joint mask
    ret = []
    for v in variables:
        ret.append(v[~mask])
    return ret

def make_take(ndims, slice_dim):
    """Generate a take function to index in a particular dimension."""
    def take(indexer):
        return tuple(indexer if slice_dim % ndims == i else slice(None)  # noqa: S001
                     for i in range(ndims))
    return take

def _broadcast_to_axis(arr, axis, ndim):
    """Handle reshaping coordinate array to have proper dimensionality.
    This puts the values along the specified axis.
    """
    if arr.ndim == 1 and arr.ndim < ndim:
        new_shape = [1] * ndim
        new_shape[axis] = arr.size
        arr = arr.reshape(*new_shape)
    return arr

def _process_gradient_args(f, axes, coordinates, deltas):
    """Handle common processing of arguments for gradient and gradient-like functions."""
    axes_given = axes is not None
    axes = axes if axes_given else range(f.ndim)

    def _check_length(positions):
        if axes_given and len(positions) < len(axes):
            raise ValueError('Length of "coordinates" or "deltas" cannot be less than that '
                             'of "axes".')
        elif not axes_given and len(positions) != len(axes):
            raise ValueError('Length of "coordinates" or "deltas" must match the number of '
                             'dimensions of "f" when "axes" is not given.')

    if deltas is not None:
        if coordinates is not None:
            raise ValueError('Cannot specify both "coordinates" and "deltas".')
        _check_length(deltas)
        return 'delta', deltas, axes
    elif coordinates is not None:
        _check_length(coordinates)
        return 'x', coordinates, axes
    elif isinstance(f, np.DimArray):
        return 'pass', axes, axes  # only the axis argument matters
    else:
        raise ValueError('Must specify either "coordinates" or "deltas" for value positions '
                         'when "f" is not a DataArray.')

def _process_deriv_args(f, axis, x, delta):
    """Handle common processing of arguments for derivative functions."""
    n = f.ndim
    axis = np.normalize_axis_index(axis if axis is not None else 0, n)

    if f.shape[axis] < 3:
        raise ValueError('f must have at least 3 point along the desired axis.')

    if delta is not None:
        if x is not None:
            raise ValueError('Cannot specify both "x" and "delta".')

        delta = np.atleast_1d(delta)
        if delta.size == 1:
            diff_size = list(f.shape)
            diff_size[axis] -= 1
            delta = np.broadcast_to(delta, diff_size)
        else:
            delta = _broadcast_to_axis(delta, axis, n)
    elif x is not None:
        x = _broadcast_to_axis(x, axis, n)
        delta = np.diff(x, axis=axis)
    else:
        raise ValueError('Must specify either "x" or "delta" for value positions.')

    return n, axis, delta

def first_derivative(f, axis=None, x=None, delta=None):
    """Calculate the first derivative of a grid of values.
    Works for both regularly-spaced data and grids with varying spacing.
    Either `x` or `delta` must be specified, or `f` must be given as an `DimArray` with
    attached coordinate and projection information.
    This uses 3 points to calculate the derivative, using forward or backward at the edges of
    the grid as appropriate, and centered elsewhere. The irregular spacing is handled
    explicitly, using the formulation as specified by [Bowen2005]_.
    Parameters
    ----------
    f : array-like
        Array of values of which to calculate the derivative
    axis : int or str, optional
        The array axis along which to take the derivative. If `f` is ndarray-like, must be an
        integer. If `f` is a `DataArray`, can be a string (referring to either the coordinate
        dimension name or the axis type) or integer (referring to axis number), unless using
        implicit conversion to `pint.Quantity`, in which case it must be an integer. Defaults
        to 0. For reference, the current standard axis types are 'time', 'vertical', 'y', and
        'x'.
    x : array-like, optional
        The coordinate values corresponding to the grid points in `f`
    delta : array-like, optional
        Spacing between the grid points in `f`. Should be one item less than the size
        of `f` along `axis`.
    Returns
    -------
    array-like
        The first derivative calculated along the selected axis`
    See Also
    --------
    second_derivative
    """
    n, axis, delta = _process_deriv_args(f, axis, x, delta)
    take = make_take(n, axis)

    # First handle centered case
    slice0 = take(slice(None, -2))
    slice1 = take(slice(1, -1))
    slice2 = take(slice(2, None))
    delta_slice0 = take(slice(None, -1))
    delta_slice1 = take(slice(1, None))

    combined_delta = delta[delta_slice0] + delta[delta_slice1]
    delta_diff = delta[delta_slice1] - delta[delta_slice0]
    center = (- delta[delta_slice1] / (combined_delta * delta[delta_slice0]) * f[slice0]
              + delta_diff / (delta[delta_slice0] * delta[delta_slice1]) * f[slice1]
              + delta[delta_slice0] / (combined_delta * delta[delta_slice1]) * f[slice2])

    # Fill in "left" edge with forward difference
    slice0 = take(slice(None, 1))
    slice1 = take(slice(1, 2))
    slice2 = take(slice(2, 3))
    delta_slice0 = take(slice(None, 1))
    delta_slice1 = take(slice(1, 2))

    combined_delta = delta[delta_slice0] + delta[delta_slice1]
    big_delta = combined_delta + delta[delta_slice0]
    left = (- big_delta / (combined_delta * delta[delta_slice0]) * f[slice0]
            + combined_delta / (delta[delta_slice0] * delta[delta_slice1]) * f[slice1]
            - delta[delta_slice0] / (combined_delta * delta[delta_slice1]) * f[slice2])

    # Now the "right" edge with backward difference
    slice0 = take(slice(-3, -2))
    slice1 = take(slice(-2, -1))
    slice2 = take(slice(-1, None))
    delta_slice0 = take(slice(-2, -1))
    delta_slice1 = take(slice(-1, None))

    combined_delta = delta[delta_slice0] + delta[delta_slice1]
    big_delta = combined_delta + delta[delta_slice1]
    right = (delta[delta_slice1] / (combined_delta * delta[delta_slice0]) * f[slice0]
             - combined_delta / (delta[delta_slice0] * delta[delta_slice1]) * f[slice1]
             + big_delta / (combined_delta * delta[delta_slice1]) * f[slice2])

    data = []
    for a in (left, center, right):
        data.append(np.atleast_1d(a))
    data = np.concatenate(data, axis=axis)

    return data

def gradient(f, axes=None, coordinates=None, deltas=None):
    """Calculate the gradient of a grid of values.
    Works for both regularly-spaced data, and grids with varying spacing.
    Either `coordinates` or `deltas` must be specified, or `f` must be given as an
    `DimArray` with  attached coordinate and projection information.
    Parameters
    ----------
    f : array-like
        Array of values of which to calculate the derivative
    axes : sequence, optional
        Sequence of strings (if `f` is a `xarray.DataArray` and implicit conversion to
        `pint.Quantity` is not used) or integers that specify the array axes along which to
        take the derivatives. Defaults to all axes of `f`. If given, and used with
        `coordinates` or `deltas`, its length must be less than or equal to that of the
        `coordinates` or `deltas` given. In general, each axis can be an axis number
        (integer), dimension coordinate name (string) or a standard axis type (string). The
        current standard axis types are 'time', 'vertical', 'y', and 'x'.
    coordinates : array-like, optional
        Sequence of arrays containing the coordinate values corresponding to the
        grid points in `f` in axis order.
    deltas : array-like, optional
        Sequence of arrays or scalars that specify the spacing between the grid points in `f`
        in axis order. There should be one item less than the size of `f` along the applicable
        axis.
    Returns
    -------
    tuple of array-like
        The first derivative calculated along each specified axis of the original array
    See Also
    --------
    laplacian, first_derivative
    Notes
    -----
    If this function is used without the `axes` parameter, the length of `coordinates` or
    `deltas` (as applicable) should match the number of dimensions of `f`.
    """
    pos_kwarg, positions, axes = _process_gradient_args(f, axes, coordinates, deltas)
    return tuple(first_derivative(f, axis=axis, **{pos_kwarg: positions[ind]})
                 for ind, axis in enumerate(axes))