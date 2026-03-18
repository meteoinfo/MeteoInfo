
from org.meteoinfo.data.dimarray import Dimension as JDimension, DimensionType
from org.meteoinfo.jython import JythonUtil
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
    dim = JDimension(dtype)
    dim.setDimValue(value._array)
    dim.setShortName(name)
    return dim


class Dimension(object):

    def __init__(self, name, values, type=None):
        self._name = name
        if isinstance(values, int):
            self._values = np.arange(values)
        else:
            self._values = np.asarray(values)

        if type is None:
            type = DimensionType.OTHER
        elif isinstance(type, basestring):
            if type.upper() == 'X':
                type = DimensionType.X
            elif type.upper() == 'Y':
                type = DimensionType.Y
            elif type.upper() == 'Z':
                type = DimensionType.Z
            elif type.upper() == 'T':
                type = DimensionType.T
            else:
                type = DimensionType.OTHER

        self._type = type
        dim = JDimension(self._type)
        dim.setDimValue(self._values._array)
        dim.setShortName(name)
        self._dim = dim

    @classmethod
    def new_dimension(cls, jdim):
        """
        Create a new Dimension from Java Dimension object.

        :param jdim: Java Dimension object.
        :return: Dimension object.
        """
        return Dimension(jdim.getName(), jdim.getDimValue(), jdim.getDimType())

    @property
    def name(self):
        return self._name

    @name.setter
    def name(self, value):
        self._name = value

    @property
    def values(self):
        return self._values

    @values.setter
    def values(self, value):
        self._values = value

    @property
    def length(self):
        return len(self._values)

    @property
    def type(self):
        return self._type

    @type.setter
    def type(self, value):
        self._type = value

    def __repr__(self):
        r = '{}: {}\n'.format(self._name, self.length)
        r += self._values.__repr__()
        return r

    def value_index(self, value):
        idx = self._dim.getValueIndex(value)
        if isinstance(value, (list, tuple)):
            idx = list(idx)
        return idx

    def delta_value(self):
        return self._dim.getDeltaValue()

    def extract(self, *args):
        if len(args) == 1:
            dim = self._dim.extract(args[0])
        else:
            dim = self._dim.extract(args[0], args[1], args[2])
        return Dimension.new_dimension(dim)


class Dimensions(tuple):

    def __new__(cls, *args):
        cls._fields = []
        if len(args) == 1 and isinstance(args[0], (tuple, list)):
            args = args[0]

        for arg in args:
            if not isinstance(arg, Dimension):
                raise TypeError("values must by Dimension object")
            cls._fields.append(arg.name)

        instance = super(Dimensions, cls).__new__(cls, args)
        instance._fields = cls._fields
        for naem, dim in zip(instance._fields, instance):
            instance.__setattr__(naem, dim)

        return instance


    @classmethod
    def new_dimensions(cls, jdims):
        dims = []
        for jdim in jdims:
            dims.append(Dimension.new_dimension(jdim))

        return Dimensions(dims)


    def __repr__(self):
        r = ''
        for dim in self:
            r += '{}: {}\n'.format(dim.name, dim.length)

        return r


    def name_index(self, value):
        try:
            idx =self._fields.index(value)
            return idx, self[idx]
        except ValueError:
            return -1, None
