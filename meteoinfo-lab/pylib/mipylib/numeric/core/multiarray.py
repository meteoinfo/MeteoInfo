"""
multiarray module
"""

from .fromnumeric import nonzero
from ._exceptions import AxisError

__all__ = ['normalize_axis_index','where']

def normalize_axis_index(axis, ndim, msg_prefix=None):
    """
    Normalizes an axis index, `axis`, such that is a valid positive index into
    the shape of array with `ndim` dimensions. Raises an AxisError with an
    appropriate message if this is not possible.

    :param axis: (*int*) The un-normalized index of the axis. Can be negative.
    :param ndim: (*int*) The number of dimensions of the array that `axis` should be normalized
        against.
    :param msg_prefix: (*str*) A prefix to put before the message, typically the name of the argument.
    :return: (*int*) The normalized axis index, such that `0 <= normalized_axis < ndim`.
    """
    if 0 <= axis < ndim:
        return axis
    elif axis < 0:
        axis = ndim + axis
        if 0 <= axis < ndim:
            return axis
        else:
            raise AxisError(axis, ndim, msg_prefix)
    else:
        raise AxisError(axis, ndim, msg_prefix)

def where(condition):
    '''
    Return elements, either from x or y, depending on condition.

    If only condition is given, return condition.nonzero().

    :param condition: (*array_like*) Input array.

    :returns: (*tuple*) Indices of elements that are non-zero.
    '''
    return nonzero(condition)