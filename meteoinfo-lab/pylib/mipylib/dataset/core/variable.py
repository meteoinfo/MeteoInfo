import mipylib.numeric as np


class Variable(np.NDArray):

    def __init__(self, dims, data, attrs=None):
        super(Variable, self).__init__(data)

        self._dims = dims
        self._data = data
        self._attrs = attrs

    def array_wrap(self, arr, axis=None):
        """
        Return a new array wrapped as self class object.

        :param arr: The array to be wrapped.
        :param axis: (*int*) The axis for ufunc compute along. Default is `None`, means not consider.

        :return: New array object.
        """
        if isinstance(arr, (Array, NDArray)):
            if axis is None:
                return Variable(arr, self._dims)
            else:
                dims = []
                for i in range(0, self.ndim):
                    if isinstance(axis, (list, tuple)):
                        if not i in axis:
                            dims.append(self._dims[i])
                    else:
                        if i != axis:
                            dims.append(self._dims[i])
                return Variable(arr, dims)
        else:
            return arr

    @property
    def data(self):
        return self._data

    @data.setter
    def data(self, data):
        self._data = np.asarray(data)

    @property
    def dims(self):
        return self._dims

    @property
    def attrs(self):
        return self._attrs

    def load(self):
        pass
