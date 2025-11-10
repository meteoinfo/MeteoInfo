
from org.meteoinfo.data.dimarray import Dimension, DimensionType
from collections import OrderedDict
import mipylib.numeric as np


def dimension(value, name='null', type=None):
    """
    Create a new Dimension.

    :param value: (*array_like*) Dimension value.
    :param name: (*string*) Dimension name.
    :param type: (*string*) Dimension type ['X' | 'Y' | 'Z' | 'T'].
    """
    value = np.asarray(value)

    dtype = DimensionType.OTHER
    if not type is None:
        if type.upper() == 'X':
            dtype = DimensionType.X
        elif type.upper() == 'Y':
            dtype = DimensionType.Y
        elif type.upper() == 'Z':
            dtype = DimensionType.Z
        elif type.upper() == 'T':
            dtype = DimensionType.T
    dim = Dimension(dtype)
    dim.setDimValue(value._array)
    dim.setShortName(name)
    return dim


class Dimensions(OrderedDict):

    def __init__(self, **kw):
        super(OrderedDict, self).__init__(**kw)

    def __getattr__(self, key):
        try:
            return self[key]
        except KeyError:
            raise AttributeError("Has no dimension '{}'".format(key))

    def __setattr__(self, key, value):
        self[key] = value
