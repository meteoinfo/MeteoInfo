"""
Utilities that manipulate strides to achieve desirable effects.
"""

from org.meteoinfo.ndarray.math import ArrayUtil
from . import numeric as np

__all__ = ['broadcast_shapes','broadcast_arrays']

def _broadcast_shapes(*args):
    args = [np.array(_m).shape for _m in args]
    return ArrayUtil.broadcastShapes(args)


def broadcast_shapes(*args):
    """
    Broadcast the input shapes into a single shape.
    :param args: (*list of ints*) The shapes to be broadcast against each other.
    :return: Broadcasted shape.
    """
    args = [_m for _m in args]
    return ArrayUtil.broadcastShapes(args)


def broadcast_arrays(*args):
    """
    Broadcast any number of arrays against each other.
    :param args: (*list of array*) The shapes to broadcast.
    :return: Broadcasted arrays.
    """

    args = [np.array(_m) for _m in args]

    shape = _broadcast_shapes(*args)

    if all(array.shape == shape for array in args):
        # Common case where nothing needs to be broadcasted.
        return args

    return [np.broadcast_to(array, shape) for array in args]
