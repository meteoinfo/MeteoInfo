
import mipylib.numeric as np

__all__ = ['broadcast_indices', 'validate_choice']

def broadcast_indices(x, minv, ndim, axis):
    """Calculate index values to properly broadcast index array within data array.

    See usage in interp.
    """
    ret = []
    for dim in range(ndim):
        if dim == axis:
            ret.append(minv)
        else:
            broadcast_slice = [np.newaxis] * ndim
            broadcast_slice[dim] = slice(None)
            dim_inds = np.arange(x.shape[dim])
            ret.append(dim_inds[tuple(broadcast_slice)])
    return tuple(ret)

def validate_choice(options, **kwargs):
    r"""Confirm that a choice is contained within a set of options.

    Parameters
    ----------
    options : iterable

    \*\*kwargs
        Function kwarg names (keys) and their passed-in values (values) for validation

    Raises
    ------
    ValueError
        If the choice is not contained within any of these options, present valid options

    Examples
    --------
    >>> from mipylib.meteolib.cbook import validate_choice
    >>> def try_wrong_choice(color=None):
    ...    validate_choice({'blue', 'green', 'yellow'}, color=color)
    >>> try_wrong_choice(color='red') # doctest: +IGNORE_EXCEPTION_DETAIL
    Traceback (most recent call last):
    ValueError: 'red' is not a valid option for color.
    Valid options are 'yellow', 'green', 'blue'.
    """
    for kw, choice in kwargs.items():
        if choice not in options:
            raise ValueError(
                '{} is not a valid option for {}. '
                'Valid options are {}.'.format(choice, kw,", ".join(map(repr, options)))
            )
