import mipylib.numeric as np

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