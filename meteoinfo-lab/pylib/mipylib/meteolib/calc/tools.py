"""
Calculation tools functions.

Ported from MetPy.
"""

import mipylib.numeric as np
import mipylib.numeric.ma as ma
from mipylib.geolib import Geod
from ..interpolate import interpolate_1d, log_interpolate_1d
from ..cbook import broadcast_indices

__all__ = ['resample_nn_1d', 'nearest_intersection_idx', 'first_derivative', 'find_intersections', 'get_layer',
           'gradient', 'lat_lon_grid_deltas', 'get_layer_heights', 'find_bounding_indices',
           'geospatial_gradient']


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


def find_intersections(x, a, b, direction='all', log_x=False):
    """Calculate the best estimate of intersection.

    Calculates the best estimates of the intersection of two y-value
    data sets that share a common x-value set.

    Parameters
    ----------
    x : array-like
        1-dimensional array of numeric x-values
    a : array-like
        1-dimensional array of y-values for line 1
    b : array-like
        1-dimensional array of y-values for line 2
    direction : str, optional
        specifies direction of crossing. 'all', 'increasing' (a becoming greater than b),
        or 'decreasing' (b becoming greater than a). Defaults to 'all'.
    log_x : bool, optional
        Use logarithmic interpolation along the `x` axis (i.e. for finding intersections
        in pressure coordinates). Default is False.

    Returns
    -------
        A tuple (x, y) of array-like with the x and y coordinates of the
        intersections of the lines.

    Notes
    -----
    This function implicitly converts `xarray.DataArray` to `pint.Quantity`, with the results
    given as `pint.Quantity`.

    """
    # Change x to logarithmic if log_x=True
    if log_x is True:
        x = np.log(x)

    # Find the index of the points just before the intersection(s)
    nearest_idx = nearest_intersection_idx(a, b)
    next_idx = nearest_idx + 1

    # Determine the sign of the change
    sign_change = np.sign(a[next_idx] - b[next_idx])

    # x-values around each intersection
    _, x0 = _next_non_masked_element(x, nearest_idx)
    _, x1 = _next_non_masked_element(x, next_idx)

    # y-values around each intersection for the first line
    _, a0 = _next_non_masked_element(a, nearest_idx)
    _, a1 = _next_non_masked_element(a, next_idx)

    # y-values around each intersection for the second line
    _, b0 = _next_non_masked_element(b, nearest_idx)
    _, b1 = _next_non_masked_element(b, next_idx)

    # Calculate the x-intersection. This comes from finding the equations of the two lines,
    # one through (x0, a0) and (x1, a1) and the other through (x0, b0) and (x1, b1),
    # finding their intersection, and reducing with a bunch of algebra.
    delta_y0 = a0 - b0
    delta_y1 = a1 - b1
    intersect_x = (delta_y1 * x0 - delta_y0 * x1) / (delta_y1 - delta_y0)

    # Calculate the y-intersection of the lines. Just plug the x above into the equation
    # for the line through the a points. One could solve for y like x above, but this
    # causes weirder unit behavior and seems a little less good numerically.
    intersect_y = ((intersect_x - x0) / (x1 - x0)) * (a1 - a0) + a0

    # If there's no intersections, return
    if len(intersect_x) == 0:
        return intersect_x, intersect_y

    # Return x to linear if log_x is True
    if log_x is True:
        intersect_x = np.exp(intersect_x)

    # Check for duplicates
    duplicate_mask = (np.ediff1d(intersect_x, to_end=1) != 0)

    # Make a mask based on the direction of sign change desired
    if direction == 'increasing':
        mask = sign_change > 0
    elif direction == 'decreasing':
        mask = sign_change < 0
    elif direction == 'all':
        return intersect_x[duplicate_mask], intersect_y[duplicate_mask]
    else:
        raise ValueError('Unknown option for direction: {}'.format(direction))

    return intersect_x[mask & duplicate_mask], intersect_y[mask & duplicate_mask]


def _next_non_masked_element(a, idx):
    """Return the next non masked element of a masked array.

    If an array is masked, return the next non-masked element (if the given index is masked).
    If no other unmasked points are after the given masked point, returns none.

    Parameters
    ----------
    a : array-like
        1-dimensional array of numeric values
    idx : integer
        Index of requested element

    Returns
    -------
        Index of next non-masked element and next non-masked element

    """
    try:
        next_idx = idx + a[idx:].mask.argmin()
        if ma.is_masked(a[next_idx]):
            return None, None
        else:
            return next_idx, a[next_idx]
    except (AttributeError, TypeError, IndexError):
        return idx, a[idx]


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
        v = np.asarray(v)
        ret.append(v[~mask])
    return ret


def _get_bound_pressure_height(pressure, bound, height=None, interpolate=True, is_pressure=True):
    """Calculate the bounding pressure and height in a layer.

    Given pressure, optional heights and a bound, return either the closest pressure/height
    or interpolated pressure/height. If no heights are provided, a standard atmosphere
    ([NOAA1976]_) is assumed.

    Parameters
    ----------
    pressure : `pint.Quantity`
        Atmospheric pressures
    bound : `pint.Quantity`
        Bound to retrieve (in pressure or height)
    height : `pint.Quantity`, optional
        Atmospheric heights associated with the pressure levels. Defaults to using
        heights calculated from ``pressure`` assuming a standard atmosphere.
    interpolate : boolean, optional
        Interpolate the bound or return the nearest. Defaults to True.

    Returns
    -------
    `pint.Quantity`
        The bound pressure and height

    """
    # avoid circular import if basic.py ever imports something from tools.py
    from .basic import height_to_pressure_std, pressure_to_height_std

    # Make sure pressure is monotonically decreasing
    sort_inds = np.argsort(pressure)[::-1]
    pressure = pressure[sort_inds]
    if height is not None:
        height = height[sort_inds]

    # Bound is given in pressure
    if is_pressure:
        # If the bound is in the pressure data, we know the pressure bound exactly
        if bound in pressure:
            # By making sure this is at least a 1D array we avoid the behavior in numpy
            # (at least up to 1.19.4) that float32 scalar * Python float -> float64, which
            # can wreak havok with floating point comparisons.
            bound_pressure = np.atleast_1d(bound)
            # If we have heights, we know the exact height value, otherwise return standard
            # atmosphere height for the pressure
            if height is not None:
                bound_height = height[pressure == bound_pressure]
            else:
                bound_height = pressure_to_height_std(bound_pressure)
        # If bound is not in the data, return the nearest or interpolated values
        else:
            if interpolate:
                bound_pressure = bound  # Use the user specified bound
                if height is not None:  # Interpolate heights from the height data
                    bound_height = log_interpolate_1d(bound_pressure, pressure, height)
                else:  # If not heights given, use the standard atmosphere
                    bound_height = pressure_to_height_std(bound_pressure)
            else:  # No interpolation, find the closest values
                idx = (np.abs(pressure - bound)).argmin()
                bound_pressure = pressure[idx]
                if height is not None:
                    bound_height = height[idx]
                else:
                    bound_height = pressure_to_height_std(bound_pressure)

    # Bound is given in height
    else:
        # If there is height data, see if we have the bound or need to interpolate/find nearest
        if height is not None:
            if bound in height:  # Bound is in the height data
                bound_height = bound
                bound_pressure = pressure[height == bound]
            else:  # Bound is not in the data
                if interpolate:
                    bound_height = bound

                    # Need to cast back to the input type since interp (up to at least numpy
                    # 1.13 always returns float64. This can cause upstream users problems,
                    # resulting in something like np.append() to upcast.
                    bound_pressure = interpolate_1d(np.atleast_1d(bound),
                                               height, pressure).astype(bound.dtype)
                else:
                    idx = (np.abs(height - bound)).argmin()
                    bound_pressure = pressure[idx]
                    bound_height = height[idx]
        else:  # Don't have heights, so assume a standard atmosphere
            bound_height = bound
            bound_pressure = height_to_pressure_std(bound)
            # If interpolation is on, this is all we need, if not, we need to go back and
            # find the pressure closest to this and refigure the bounds
            if not interpolate:
                idx = (np.abs(pressure - bound_pressure)).argmin()
                bound_pressure = pressure[idx]
                bound_height = pressure_to_height_std(bound_pressure)

    # If the bound is out of the range of the data, we shouldn't extrapolate
    if not (_greater_or_close(bound_pressure, np.min(pressure))
            and _less_or_close(bound_pressure, np.max(pressure))):
        raise ValueError('Specified bound is outside pressure range.')
    if height is not None and not (_less_or_close(bound_height, np.max(height))
                                   and _greater_or_close(bound_height, np.min(height))):
        raise ValueError('Specified bound is outside height range.')

    return bound_pressure, bound_height


def get_layer_heights(height, depth, *args, **kwargs):
    """Return an atmospheric layer from upper air data with the requested bottom and depth.

    This function will subset an upper air dataset to contain only the specified layer using
    the height only.

    Parameters
    ----------
    height : array-like
        Atmospheric height
    depth : `array-like`
        Thickness of the layer
    args : array-like
        Atmospheric variable(s) measured at the given pressures
    bottom : `float`, optional
        Bottom of the layer
    interpolate : bool, optional
        Interpolate the top and bottom points if they are not in the given data. Defaults
        to True.
    with_agl : bool, optional
        Returns the height as above ground level by subtracting the minimum height in the
        provided height. Defaults to False.

    Returns
    -------
    `array, array`
        Height and data variables of the layer
    Notes
    -----
    Only functions on 1D profiles (not higher-dimension vertical cross sections or grids).
    """
    # Make sure pressure and datavars are the same length
    for datavar in args:
        if len(height) != len(datavar):
            raise ValueError('Height and data variables must have the same length.')

    # If we want things in AGL, subtract the minimum height from all height values
    with_agl = kwargs.pop('with_agl', False)
    if with_agl:
        sfc_height = np.min(height)
        height = height - sfc_height

    # If the bottom is not specified, make it the surface
    bottom = kwargs.pop('bottom', None)
    if bottom is None:
        bottom = height[0]

    # Make heights and arguments base units
    height = height.to_base_units()
    bottom = bottom.to_base_units()

    # Calculate the top of the layer
    top = bottom + depth

    ret = []  # returned data variables in layer

    # Ensure heights are sorted in ascending order
    sort_inds = np.argsort(height)
    height = height[sort_inds]

    # Mask based on top and bottom
    inds = _greater_or_close(height, bottom) & _less_or_close(height, top)
    heights_interp = height[inds]

    # Interpolate heights at bounds if necessary and sort
    interpolate = kwargs.pop('interpolate', True)
    if interpolate:
        # If we don't have the bottom or top requested, append them
        if top not in heights_interp:
            heights_interp = np.sort(np.append(heights_interp.m, top.m))
        if bottom not in heights_interp:
            heights_interp = np.sort(np.append(heights_interp.m, bottom.m))

    ret.append(heights_interp)

    for datavar in args:
        # Ensure that things are sorted in ascending order
        datavar = datavar[sort_inds]

        if interpolate:
            # Interpolate for the possibly missing bottom/top values
            datavar_interp = interpolate_1d(heights_interp, height, datavar)
            datavar = datavar_interp
        else:
            datavar = datavar[inds]

        ret.append(datavar)
    return ret


def get_layer(pressure, *args, **kwargs):
    r"""Return an atmospheric layer from upper air data with the requested bottom and depth.

    This function will subset an upper air dataset to contain only the specified layer. The
    bottom of the layer can be specified with a pressure or height above the surface
    pressure. The bottom defaults to the surface pressure. The depth of the layer can be
    specified in terms of pressure or height above the bottom of the layer. If the top and
    bottom of the layer are not in the data, they are interpolated by default.

    Parameters
    ----------
    pressure : array-like
        Atmospheric pressure profile
    args : array-like
        Atmospheric variable(s) measured at the given pressures
    height: array-like, optional
        Atmospheric heights corresponding to the given pressures. Defaults to using
        heights calculated from ``pressure`` assuming a standard atmosphere [NOAA1976]_.
    bottom : `pint.Quantity`, optional
        Bottom of the layer as a pressure or height above the surface pressure. Defaults
        to the highest pressure or lowest height given.
    depth : `pint.Quantity`, optional
        Thickness of the layer as a pressure or height above the bottom of the layer.
        Defaults to 100 hPa.
    interpolate : bool, optional
        Interpolate the top and bottom points if they are not in the given data. Defaults
        to True.

    Returns
    -------
    `pint.Quantity, pint.Quantity`
        The pressure and data variables of the layer

    Notes
    -----
    Only functions on 1D profiles (not higher-dimension vertical cross sections or grids).
    Also, this will return Pint Quantities even when given xarray DataArray profiles.
    """
    height = kwargs.pop('height', None)
    bottom = kwargs.pop('bottom', None)
    depth = kwargs.pop('depth', 100)    #'hPa'
    interpolate = kwargs.pop('interpolate', True)
    is_pressure = height is None

    # Make sure pressure and datavars are the same length
    for datavar in args:
        if len(pressure) != len(datavar):
            raise ValueError('Pressure and data variables must have the same length.')

    # If the bottom is not specified, make it the surface pressure
    if bottom is None:
        bottom = np.max(pressure)

    bottom_pressure, bottom_height = _get_bound_pressure_height(pressure, bottom,
                                                                height=height,
                                                                interpolate=interpolate)

    # Calculate the top in whatever units depth is in
    if is_pressure:
        top = bottom_pressure - depth
    else:
        top = bottom_height + depth

    top_pressure, _ = _get_bound_pressure_height(pressure, top, height=height,
                                                 interpolate=interpolate, is_pressure=is_pressure)

    ret = []  # returned data variables in layer

    # Ensure pressures are sorted in ascending order
    sort_inds = np.argsort(pressure)
    pressure = pressure[sort_inds]

    # Mask based on top and bottom pressure
    inds = (_less_or_close(pressure, bottom_pressure)
            & _greater_or_close(pressure, top_pressure))
    p_interp = pressure[inds]

    # Interpolate pressures at bounds if necessary and sort
    if interpolate:
        # If we don't have the bottom or top requested, append them
        if not np.any(np.isclose(top_pressure, p_interp)):
            p_interp = np.sort(np.append(p_interp, top_pressure))
        if not np.any(np.isclose(bottom_pressure, p_interp)):
            p_interp = np.sort(np.append(p_interp, bottom_pressure))

    ret.append(p_interp[::-1])

    for datavar in args:
        # Ensure that things are sorted in ascending order
        datavar = datavar[sort_inds]

        if interpolate:
            # Interpolate for the possibly missing bottom/top values
            datavar_interp = log_interpolate_1d(p_interp, pressure, datavar)
            datavar = datavar_interp
        else:
            datavar = datavar[inds]

        ret.append(datavar[::-1])
    return ret


def find_bounding_indices(arr, values, axis, from_below=True):
    """Find the indices surrounding the values within arr along axis.

    Returns a set of above, below, good. Above and below are lists of arrays of indices.
    These lists are formulated such that they can be used directly to index into a numpy
    array and get the expected results (no extra slices or ellipsis necessary). `good` is
    a boolean array indicating the "columns" that actually had values to bound the desired
    value(s).

    Parameters
    ----------
    arr : array-like
        Array to search for values
    values: array-like
        One or more values to search for in `arr`
    axis : int
        Dimension of `arr` along which to search
    from_below : bool, optional
        Whether to search from "below" (i.e. low indices to high indices). If `False`,
        the search will instead proceed from high indices to low indices. Defaults to `True`.

    Returns
    -------
    above : list of arrays
        List of broadcasted indices to the location above the desired value
    below : list of arrays
        List of broadcasted indices to the location below the desired value
    good : array
        Boolean array indicating where the search found proper bounds for the desired value
    """
    # The shape of generated indices is the same as the input, but with the axis of interest
    # replaced by the number of values to search for.
    indices_shape = list(arr.shape)
    indices_shape[axis] = len(values)

    # Storage for the found indices and the mask for good locations
    indices = np.empty(indices_shape, dtype=np.dtype.int)
    good = np.empty(indices_shape, dtype=np.dtype.bool)

    # Used to put the output in the proper location
    take = make_take(arr.ndim, axis)

    # Loop over all of the values and for each, see where the value would be found from a
    # linear search
    for level_index, value in enumerate(values):
        # Look for changes in the value of the test for <= value in consecutive points
        # Taking abs() because we only care if there is a flip, not which direction.
        switches = np.abs(np.diff((arr <= value).astype(np.dtype.int), axis=axis))

        # Good points are those where it's not just 0's along the whole axis
        good_search = np.any(switches, axis=axis)

        if from_below:
            # Look for the first switch; need to add 1 to the index since argmax is giving the
            # index within the difference array, which is one smaller.
            index = switches.argmax(axis=axis) + 1
        else:
            # Generate a list of slices to reverse the axis of interest so that searching from
            # 0 to N is starting at the "top" of the axis.
            arr_slice = [slice(None)] * arr.ndim
            arr_slice[axis] = slice(None, None, -1)

            # Same as above, but we use the slice to come from the end; then adjust those
            # indices to measure from the front.
            index = arr.shape[axis] - 1 - switches[tuple(arr_slice)].argmax(axis=axis)

        # Set all indices where the results are not good to 0
        index[~good_search] = 0

        # Put the results in the proper slice
        store_slice = take(level_index)
        indices[store_slice] = index
        good[store_slice] = good_search

    # Create index values for broadcasting arrays
    above = broadcast_indices(arr, indices, arr.ndim, axis)
    below = broadcast_indices(arr, indices - 1, arr.ndim, axis)

    return above, below, good


def _greater_or_close(a, value, **kwargs):
    r"""Compare values for greater or close to boolean masks.

    Returns a boolean mask for values greater than or equal to a target within a specified
    absolute or relative tolerance.

    Parameters
    ----------
    a : array-like
        Array of values to be compared
    value : float
        Comparison value

    Returns
    -------
    array-like
        Boolean array where values are greater than or nearly equal to value.
    """
    return (a > value) | np.isclose(a, value, **kwargs)


def _less_or_close(a, value, **kwargs):
    r"""Compare values for less or close to boolean masks.

    Returns a boolean mask for values less than or equal to a target within a specified
    absolute or relative tolerance.

    Parameters
    ----------
    a : array-like
        Array of values to be compared
    value : float
        Comparison value

    Returns
    -------
    array-like
        Boolean array where values are less than or nearly equal to value
    """
    return (a < value) | np.isclose(a, value, **kwargs)


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


def lat_lon_grid_deltas(longitude, latitude, x_dim=-1, y_dim=-2, geod=None):
    r"""
    Calculate the actual delta between grid points that are in latitude/longitude format.

    Parameters
    ----------
    longitude : array_like
        Array of longitudes defining the grid. Assumed to be in
        degrees.
    latitude : array_like
        Array of latitudes defining the grid. Assumed to be in
        degrees.
    x_dim: int
        axis number for the x dimension, defaults to -1.
    y_dim : int
        axis number for the y dimension, defaults to -2.
    geod : `geolib.Geod` or ``None``
        geolib Geod to use for forward azimuth and distance calculations. If ``None``, use a
        default spherical ellipsoid.

    Returns
    -------
    dx, dy:
        At least two dimensional arrays of signed deltas between grid points in the x and y
        direction

    Notes
    -----
    Accepts 1D, 2D, or higher arrays for latitude and longitude
    Assumes [..., Y, X] dimension order for input and output, unless keyword arguments `y_dim`
    and `x_dim` are otherwise specified.
    """
    # Inputs must be the same number of dimensions
    if latitude.ndim != longitude.ndim:
        raise ValueError('Latitude and longitude must have the same number of dimensions.')

    # If we were given 1D arrays, make a mesh grid
    if latitude.ndim < 2:
        longitude, latitude = np.meshgrid(longitude, latitude)

    longitude = np.asarray(longitude)
    latitude = np.asarray(latitude)

    # Determine dimension order for offset slicing
    take_y = make_take(latitude.ndim, y_dim)
    take_x = make_take(latitude.ndim, x_dim)

    g = Geod() if geod is None else geod
    forward_az, _, dy = g.inv(longitude[take_y(slice(None, -1))],
                              latitude[take_y(slice(None, -1))],
                              longitude[take_y(slice(1, None))],
                              latitude[take_y(slice(1, None))])
    dy[(forward_az < -90.) | (forward_az > 90.)] *= -1

    forward_az, _, dx = g.inv(longitude[take_x(slice(None, -1))],
                              latitude[take_x(slice(None, -1))],
                              longitude[take_x(slice(1, None))],
                              latitude[take_x(slice(1, None))])
    dx[(forward_az < 0.) | (forward_az > 180.)] *= -1

    return dx, dy


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


def geospatial_gradient(f, dx=None, dy=None, x_dim=-1, y_dim=-2,
                        parallel_scale=None, meridional_scale=None, return_only=None):
    r"""Calculate the projection-correct gradient of a 2D scalar field.

    Parameters
    ----------
    f : (..., M, N) `array`
        scalar field for which the horizontal gradient should be calculated
    return_only : str or Sequence[str], optional
        Sequence of which components of the gradient to compute and return. If none,
        returns the gradient tuple ('df/dx', 'df/dy'). Otherwise, matches the return
        pattern of the given strings. Only valid strings are 'df/dx', 'df/dy'.

    Returns
    -------
    `array`, tuple of `array`, or tuple of pairs of `array`
        Component(s) of vector derivative

    Other Parameters
    ----------------
    dx : `array`, optional
        The grid spacing(s) in the x-direction. If an array, there should be one item less than
        the size of `u` along the applicable axis. Optional if `DimArray` with
        latitude/longitude coordinates used as input. Also optional if one-dimensional
        longitude and latitude arguments are given for your data on a non-projected grid.
        Keyword-only argument.
    dy : `array`, optional
        The grid spacing(s) in the y-direction. If an array, there should be one item less than
        the size of `u` along the applicable axis. Optional if `DimArray` with
        latitude/longitude coordinates used as input. Also optional if one-dimensional
        longitude and latitude arguments are given for your data on a non-projected grid.
        Keyword-only argument.
    x_dim : int, optional
        Axis number of x dimension. Defaults to -1 (implying [..., Y, X] order). Automatically
        parsed from input if using `DimArray`. Keyword-only argument.
    y_dim : int, optional
        Axis number of y dimension. Defaults to -2 (implying [..., Y, X] order). Automatically
        parsed from input if using `DimArray`. Keyword-only argument.
    parallel_scale : `array`, optional
        Parallel scale of map projection at data coordinate. Optional if `DimArray`
        with latitude/longitude coordinates and MetPy CRS used as input. Also optional if
        longitude, latitude, and crs are given. If otherwise omitted, calculation will be
        carried out on a Cartesian, rather than geospatial, grid. Keyword-only argument.
    meridional_scale : `array`, optional
        Meridional scale of map projection at data coordinate. Optional if `DimArray`
        with latitude/longitude coordinates and MetPy CRS used as input. Also optional if
        longitude, latitude, and crs are given. If otherwise omitted, calculation will be
        carried out on a Cartesian, rather than geospatial, grid. Keyword-only argument.

    See Also
    --------
    vector_derivative, gradient, geospatial_laplacian
    """
    derivatives = {component: None
                   for component in ('df/dx', 'df/dy')
                   if (return_only is None or component in return_only)}

    scales = {'df/dx': parallel_scale, 'df/dy': meridional_scale}

    map_factor_correction = parallel_scale is not None and meridional_scale is not None

    for component in derivatives:
        delta, dim = (dx, x_dim) if component[-2:] == 'dx' else (dy, y_dim)
        derivatives[component] = first_derivative(f, delta=delta, axis=dim)

        if map_factor_correction:
            derivatives[component] *= scales[component]

    # Build return collection
    if return_only is None:
        return derivatives['df/dx'], derivatives['df/dy']
    elif isinstance(return_only, str):
        return derivatives[return_only]
    else:
        return tuple(derivatives[component] for component in return_only)