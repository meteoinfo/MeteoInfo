import mipylib.numeric as np


class Variable(np.NDArray):

    def __init__(self, dims, data, attrs=None):
        super(Variable, self).__init__(data)

        self._dims = dims
        self._data = data
        self._attrs = attrs

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
