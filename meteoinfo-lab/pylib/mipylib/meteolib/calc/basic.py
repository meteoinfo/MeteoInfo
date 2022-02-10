import warnings
from itertools import product

import mipylib.numeric as np
from .. import constants

__all__ = ['coriolis_parameter','smooth_window','smooth_n_point']

def coriolis_parameter(latitude):
    r"""Calculate the Coriolis parameter at each point.

    The implementation uses the formula outlined in [Hobbs1977]_ pg.370-371.

    Parameters
    ----------
    latitude : array_like
        Latitude at each point

    Returns
    -------
    `array`
        Corresponding Coriolis force at each point
    """
    latitude = _check_radians(latitude, max_radians=np.pi / 2)
    return 2. * constants.omega * np.sin(latitude)

def _check_radians(value, max_radians=2 * np.pi):
    """Input validation of values that could be in degrees instead of radians.

    Parameters
    ----------
    value : `pint.Quantity`
        Input value to check
    max_radians : float
        Maximum absolute value of radians before warning

    Returns
    -------
    `pint.Quantity`
        Input value
    """
    value = np.radians(value)
    if np.any(np.abs(value) > max_radians):
        warnings.warn('Input over {} radians. '
                      'Ensure proper units are given.'.format(np.max(max_radians)))
    return value

def smooth_window(scalar_grid, window, passes=1, normalize_weights=True):
    """Filter with an arbitrary window smoother.

    Parameters
    ----------
    scalar_grid : array-like
        N-dimensional scalar grid to be smoothed
    window : ndarray
        Window to use in smoothing. Can have dimension less than or equal to N. If
        dimension less than N, the scalar grid will be smoothed along its trailing dimensions.
        Shape along each dimension must be odd.
    passes : int
        The number of times to apply the filter to the grid. Defaults to 1.
    normalize_weights : bool
        If true, divide the values in window by the sum of all values in the window to obtain
        the normalized smoothing weights. If false, use supplied values directly as the
        weights.

    Returns
    -------
    array-like
        The filtered scalar grid

    See Also
    --------
    smooth_rectangular, smooth_circular, smooth_n_point, smooth_gaussian
    Notes
    -----
    This function can be applied multiple times to create a more smoothed field and will only
    smooth the interior points, leaving the end points with their original values (this
    function will leave an unsmoothed edge of size `(n - 1) / 2` for each `n` in the shape of
    `window` around the data). If a masked value or NaN values exists in the array, it will
    propagate to any point that uses that particular grid point in the smoothing calculation.
    Applying the smoothing function multiple times will propagate NaNs further throughout the
    domain.
    """
    def _pad(n):
        # Return number of entries to pad given length along dimension.
        return (n - 1) // 2

    def _zero_to_none(x):
        # Convert zero values to None, otherwise return what is given.
        return x if x != 0 else None

    def _offset(pad, k):
        # Return padded slice offset by k entries
        return slice(_zero_to_none(pad + k), _zero_to_none(-pad + k))

    def _trailing_dims(indexer):
        # Add ... to the front of an indexer, since we are working with trailing dimensions.
        return (Ellipsis,) + tuple(indexer)

    # Verify that shape in all dimensions is odd (need to have a neighborhood around a
    # central point)
    if any((size % 2 == 0) for size in window.shape):
        raise ValueError('The shape of the smoothing window must be odd in all dimensions.')

    # Optionally normalize the supplied weighting window
    if normalize_weights:
        weights = window / np.sum(window)
    else:
        weights = window

    # Set indexes
    # Inner index for the centered array elements that are affected by the smoothing
    inner_full_index = _trailing_dims(_offset(_pad(n), 0) for n in weights.shape)
    # Indexes to iterate over each weight
    weight_indexes = tuple(product(*(range(n) for n in weights.shape)))

    # Index for full array elements, offset by the weight index
    def offset_full_index(weight_index):
        return _trailing_dims(_offset(_pad(n), weight_index[i] - _pad(n))
                              for i, n in enumerate(weights.shape))

    # TODO: this is not lazy-loading/dask compatible, as it "densifies" the data
    data = np.array(scalar_grid)
    for _ in range(passes):
        # Set values corresponding to smoothing weights by summing over each weight and
        # applying offsets in needed dimensions
        data[inner_full_index] = sum(weights[index] * data[offset_full_index(index)]
                                     for index in weight_indexes)

    return data

def smooth_n_point(scalar_grid, n=5, passes=1):
    """Filter with an n-point smoother.

    Parameters
    ----------
    scalar_grid : array-like or `pint.Quantity`
        N-dimensional scalar grid to be smoothed. If more than two axes, smoothing is only
        done along the last two.
    n: int
        The number of points to use in smoothing, only valid inputs
        are 5 and 9. Defaults to 5.
    passes : int
        The number of times to apply the filter to the grid. Defaults to 1.

    Returns
    -------
    array-like or `pint.Quantity`
        The filtered scalar grid

    See Also
    --------
    smooth_window, smooth_rectangular, smooth_circular, smooth_gaussian

    Notes
    -----
    This function is a close replication of the GEMPAK function SM5S and SM9S depending on the
    choice of the number of points to use for smoothing. This function can be applied multiple
    times to create a more smoothed field and will only smooth the interior points, leaving
    the end points with their original values (this function will leave an unsmoothed edge of
    size 1 around the data). If a masked value or NaN values exists in the array, it will
    propagate to any point that uses that particular grid point in the smoothing calculation.
    Applying the smoothing function multiple times will propagate NaNs further throughout the
    domain.
    """
    if n == 9:
        weights = np.array([[0.0625, 0.125, 0.0625],
                            [0.125, 0.25, 0.125],
                            [0.0625, 0.125, 0.0625]])
    elif n == 5:
        weights = np.array([[0., 0.125, 0.],
                            [0.125, 0.5, 0.125],
                            [0., 0.125, 0.]])
    else:
        raise ValueError('The number of points to use in the smoothing '
                         'calculation must be either 5 or 9.')

    return smooth_window(scalar_grid, window=weights, passes=passes, normalize_weights=False)