import mipylib.numeric as np

class DateTimeAccessor(object):
    """Access datetime fields for DimArrays with datetime-like dtypes.

    Fields can be accessed through the `.dt` attribute
    for applicable DimArrays.
    """

    def __init__(self, data):
        """
        Init.
        :param data: NDArray with datetime data type.
        """
        self._data = np.asarray(data)

    @property
    def year(self):
        """The year of the datetime"""
        r = self._data._array.getYears()
        return np.NDArray(r)

    @property
    def month(self):
        """The month of the datetime"""
        r = self._data._array.getMonths()
        return np.NDArray(r)

    @property
    def day(self):
        """The day of the datetime"""
        r = self._data._array.getDays()
        return np.NDArray(r)

    @property
    def hour(self):
        """The hour of the datetime"""
        r = self._data._array.getHours()
        return np.NDArray(r)

    @property
    def minute(self):
        """The minute of the datetime"""
        r = self._data._array.getMinutes()
        return np.NDArray(r)

    @property
    def second(self):
        """The second of the datetime"""
        r = self._data._array.getSeconds()
        return np.NDArray(r)
