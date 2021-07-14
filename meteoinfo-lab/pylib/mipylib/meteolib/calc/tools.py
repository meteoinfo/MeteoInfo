import mipylib.numeric as np

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