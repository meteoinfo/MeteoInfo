from mipylib import numeric as np
from ..core._ndarray import NDArray
from org.meteoinfo.ndarray.math import ArrayUtil

nomask = False

class MaskedArray(NDArray):
    """
    An array class with possibly masked values.
    Masked values of True exclude the corresponding element from any
    computation.
    Construction::
      x = MaskedArray(data, mask=nomask, dtype=None, copy=False, subok=True,
                      ndmin=0, fill_value=None, keep_mask=True, hard_mask=None,
                      shrink=True, order=None)
    Parameters
    ----------
    data : array_like
        Input data.
    mask : sequence, optional
        Mask. Must be convertible to an array of booleans with the same
        shape as `data`. True indicates a masked (i.e. invalid) data.
    dtype : dtype, optional
        Data type of the output.
        If `dtype` is None, the type of the data argument (``data.dtype``)
        is used. If `dtype` is not None and different from ``data.dtype``,
        a copy is performed.
    copy : bool, optional
        Whether to copy the input data (True), or to use a reference instead.
        Default is False.
    subok : bool, optional
        Whether to return a subclass of `MaskedArray` if possible (True) or a
        plain `MaskedArray`. Default is True.
    ndmin : int, optional
        Minimum number of dimensions. Default is 0.
    fill_value : scalar, optional
        Value used to fill in the masked values when necessary.
        If None, a default based on the data-type is used.
    """

    def __init__(self, data, mask=nomask, dtype=None, copy=False,
                 subok=True, ndmin=0, fill_value=None):
        if isinstance(data, NDArray):
            data = data._array
        super(MaskedArray, self).__init__(data)
        self._data = NDArray(self._array)
        self._baseclass = getattr(data, '_baseclass', type(self._data))
        if mask is nomask:
            self._mask = mask
        else:
            self._mask = np.array(mask)
            if self._mask.shape != self._data.shape:
                self._mask = self._mask.reshape(self._data.shape)
            if self._mask.dtype != np.dtype.bool:
                self._mask = self._mask.astype(np.dtype.bool)
        self._fill_value = fill_value

    def __str__(self):
        r = 'masked_array(data=' + ArrayUtil.convertToString(self._data._array) + ',\n\tmask='
        if self._mask is nomask:
            r = r + 'False,'
        else:
            r = r + ArrayUtil.convertToString(self._mask._array) + ','
        r = r + '\n\tfill_value=' + str(self._fill_value) + ')'
        return r

    def __repr__(self):
        return self.__str__()

    def filled(self, fill_value=None):
        """
        Return a copy of self, with masked values filled with a given value.
        **However**, if there are no masked values to fill, self will be
        returned instead as an ndarray.
        Parameters
        ----------
        fill_value : array_like, optional
            The value to use for invalid entries. Can be scalar or non-scalar.
            If non-scalar, the resulting ndarray must be broadcastable over
            input array. Default is None, in which case, the `fill_value`
            attribute of the array is used instead.
        Returns
        -------
        filled_array : ndarray
            A copy of ``self`` with invalid entries replaced by *fill_value*
            (be it the function argument or the attribute of ``self``), or
            ``self`` itself as an ndarray if there are no invalid entries to
            be replaced.
        Notes
        -----
        The result is **not** a MaskedArray!
        Examples
        --------
        >>> x = np.ma.array([1,2,3,4,5], mask=[0,0,1,0,1], fill_value=-999)
        >>> x.filled()
        array([   1,    2, -999,    4, -999])
        >>> x.filled(fill_value=1000)
        array([   1,    2, 1000,    4, 1000])
        >>> type(x.filled())
        <class 'numpy.ndarray'>
        Subclassing is preserved. This means that if, e.g., the data part of
        the masked array is a recarray, `filled` returns a recarray:
        >>> x = np.array([(-1, 2), (-3, 4)], dtype='i8,i8').view(np.recarray)
        >>> m = np.ma.array(x, mask=[(True, False), (False, True)])
        >>> m.filled()
        rec.array([(999999,      2), (    -3, 999999)],
                  dtype=[('f0', '<i8'), ('f1', '<i8')])
        """
        m = self._mask
        if m is nomask:
            return self._data

        if fill_value is None:
            fill_value = self._fill_value

        result = self._data.copy()
        result[m] = fill_value
        return result

    def sum(self, axis=None):
        """
        Sum of array elements over a given axis.

        :param axis: (*int*) Axis along which the standard deviation is computed.
            The default is to compute the standard deviation of the flattened array.

        returns: (*array_like*) Sum result.
        """
        if self._mask is nomask:
            return self._data.sum(axis=axis)
        else:
            r = self.filled(0)
            return r.sum(axis=axis)

    def count(self, axis=None):
        """
        Count of valid array elements over a given axis.

        :param axis: (*int*) Axis along which the standard deviation is computed.
            The default is to compute the standard deviation of the flattened array.
        :return: (*array_like*) Count result.
        """
        if self._mask is nomask:
            mask = np.ones(self._data.shape)
            return mask.sum(axis=axis)
        else:
            return (~self._mask).sum(axis=axis)

    def mean(self, axis=None):
        """
        Compute tha arithmetic mean along the specified axis.

        :param axis: (*int*) Axis along which the value is computed.
            The default is to compute the value of the flattened array.

        returns: (*array_like*) Mean result
        """
        if self._mask is nomask:
            return self._data.mean(axis=axis)
        else:
            dsum = self.sum(axis=axis)
            cnt = self.count(axis=axis)
            return dsum * 1. / cnt

masked_array = MaskedArray