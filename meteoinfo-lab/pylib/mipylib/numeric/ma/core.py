from mipylib import numeric as np
from ..core._ndarray import NDArray
from org.meteoinfo.ndarray.math import ArrayUtil

nomask = False

__all__ = ['MaskedArray','masked_array','masked_invalid','getdata','getmask','is_masked']

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

    def __setmask__(self, mask, copy=False):
        """
        Set the mask.

        """
        idtype = self.dtype
        current_mask = self._mask
        if mask is masked:
            mask = True

        if current_mask is nomask:
            # Make sure the mask is set
            # Just don't do anything if there's nothing to do.
            if mask is nomask:
                return
            current_mask = self._mask = make_mask_none(self.shape, idtype)

        if idtype.names is None:
            # No named fields.
            # Hardmask: don't unmask the data
            if self._hardmask:
                current_mask |= mask
            # Softmask: set everything to False
            # If it's obviously a compatible scalar, use a quick update
            # method.
            elif isinstance(mask, (int, float, np.bool, np.number)):
                current_mask[...] = mask
            # Otherwise fall back to the slower, general purpose way.
            else:
                current_mask.flat = mask
        else:
            # Named fields w/
            mdtype = current_mask.dtype
            mask = np.asarray(mask)
            # Mask is a singleton
            if not mask.ndim:
                # It's a boolean : make a record
                if mask.dtype.kind == 'b':
                    mask = np.array(tuple([mask.item()] * len(mdtype)),
                                    dtype=mdtype)
                # It's a record: make sure the dtype is correct
                else:
                    mask = mask.astype(mdtype)
            # Mask is a sequence
            else:
                # Make sure the new mask is a ndarray with the proper dtype
                try:
                    copy = None if not copy else True
                    mask = np.array(mask, copy=copy, dtype=mdtype)
                # Or assume it's a sequence of bool/int
                except TypeError:
                    mask = np.array([tuple([m] * len(mdtype)) for m in mask],
                                    dtype=mdtype)
            # Hardmask: don't unmask the data
            if self._hardmask:
                for n in idtype.names:
                    current_mask[n] |= mask[n]
            # Softmask: set everything to False
            # If it's obviously a compatible scalar, use a quick update
            # method.
            elif isinstance(mask, (int, float, np.dtyp.bool, np.dtype.number)):
                current_mask[...] = mask
            # Otherwise fall back to the slower, general purpose way.
            else:
                current_mask.flat = mask
        # Reshape if needed
        if current_mask.shape:
            current_mask.shape = self.shape
        return

    _set_mask = __setmask__

    @property
    def mask(self):
        """ Current mask. """

        # We could try to force a reshape, but that wouldn't work in some
        # cases.
        # Return a view so that the dtype and shape cannot be changed in place
        # This still preserves nomask by identity
        return self._mask.view()

    @mask.setter
    def mask(self, value):
        self.__setmask__(value)

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


def getdata(a, subok=True):
    """
    Return the data of a masked array as an ndarray.
    Return the data of `a` (if any) as an ndarray if `a` is a ``MaskedArray``,
    else return `a` as a ndarray or subclass (depending on `subok`) if not.
    Parameters
    ----------
    a : array_like
        Input ``MaskedArray``, alternatively a ndarray or a subclass thereof.
    subok : bool
        Whether to force the output to be a `pure` ndarray (False) or to
        return a subclass of ndarray if appropriate (True, default).
    See Also
    --------
    getmask : Return the mask of a masked array, or nomask.
    getmaskarray : Return the mask of a masked array, or full array of False.
    Examples
    --------
    >>> import mipylib.numeric.ma as ma
    >>> a = ma.masked_equal([[1,2],[3,4]], 2)
    >>> a
    masked_array(
      data=[[1, --],
            [3, 4]],
      mask=[[False,  True],
            [False, False]],
      fill_value=2)
    >>> ma.getdata(a)
    array([[1, 2],
           [3, 4]])
    Equivalently use the ``MaskedArray`` `data` attribute.
    >>> a.data
    array([[1, 2],
           [3, 4]])
    """
    try:
        data = a._data
    except AttributeError:
        data = np.array(a, copy=False, subok=subok)

    return data


def getmask(a):
    """
    Return the mask of a masked array, or nomask.

    Return the mask of `a` as an ndarray if `a` is a `MaskedArray` and the
    mask is not `nomask`, else return `nomask`. To guarantee a full array
    of booleans of the same shape as a, use `getmaskarray`.

    Parameters
    ----------
    a : array_like
        Input `MaskedArray` for which the mask is required.

    See Also
    --------
    getdata : Return the data of a masked array as an ndarray.
    getmaskarray : Return the mask of a masked array, or full array of False.

    Examples
    --------
    >>> a = np.ma.masked_equal([[1,2],[3,4]], 2)
    >>> a
    masked_array(
      data=[[1, --],
            [3, 4]],
      mask=[[False,  True],
            [False, False]],
      fill_value=2)
    >>> ma.getmask(a)
    array([[False,  True],
           [False, False]])

    Equivalently use the `MaskedArray` `mask` attribute.

    >>> a.mask
    array([[False,  True],
           [False, False]])

    Result when mask == `nomask`

    >>> b = np.ma.masked_array([[1,2],[3,4]])
    >>> b
    masked_array(
      data=[[1, 2],
            [3, 4]],
      mask=False,
      fill_value=999999)
    >>> np.ma.nomask
    False
    >>> np.ma.getmask(b) == np.ma.nomask
    True
    >>> b.mask == np.ma.nomask
    True

    """
    return getattr(a, '_mask', nomask)


get_mask = getmask


def masked_invalid(a, copy=True):
    """
    Mask an array where invalid values occur (NaNs or infs).
    This function is a shortcut to ``masked_where``, with
    `condition` = ~(np.isfinite(a)). Any pre-existing mask is conserved.
    Only applies to arrays with a dtype where NaNs or infs make sense
    (i.e. floating point types), but accepts any array_like object.
    See Also
    --------
    masked_where : Mask where a condition is met.
    Examples
    --------
    >>> import mipylib.numeric.ma as ma
    >>> a = np.arange(5, dtype='float')
    >>> a[2] = np.nan
    >>> a[3] = np.inf
    >>> a
    array([ 0.,  1., nan, inf,  4.])
    >>> ma.masked_invalid(a)
    masked_array(data=[0.0, 1.0, --, --, 4.0],
                 mask=[False, False,  True,  True, False],
           fill_value=1e+20)
    """
    a = np.array(a, copy=copy, subok=True)
    mask = getattr(a, '_mask', None)
    if mask is not None:
        condition = ~(np.isfinite(getdata(a)))
        if mask is not nomask:
            condition |= mask
    else:
        condition = ~(np.isfinite(a))
    if isinstance(a, MaskedArray):
        result = a
    else:
        result = MaskedArray(a)
    result._mask = condition
    return result


def is_masked(x):
    """
    Determine whether input has masked values.

    Accepts any object as input, but always returns False unless the
    input is a MaskedArray containing masked values.

    Parameters
    ----------
    x : array_like
        Array to check for masked values.

    Returns
    -------
    result : bool
        True if `x` is a MaskedArray with masked values, False otherwise.

    Examples
    --------
    >>> import numpy as np
    >>> import numpy.ma as ma
    >>> x = ma.masked_equal([0, 1, 0, 2, 3], 0)
    >>> x
    masked_array(data=[--, 1, --, 2, 3],
                 mask=[ True, False,  True, False, False],
           fill_value=0)
    >>> ma.is_masked(x)
    True
    >>> x = ma.masked_equal([0, 1, 0, 2, 3], 42)
    >>> x
    masked_array(data=[0, 1, 0, 2, 3],
                 mask=False,
           fill_value=42)
    >>> ma.is_masked(x)
    False

    Always returns False if `x` isn't a MaskedArray.

    >>> x = [False, True, False]
    >>> ma.is_masked(x)
    False
    >>> x = 'a string'
    >>> ma.is_masked(x)
    False

    """
    m = getmask(x)
    if m is nomask:
        return False
    elif m.any():
        return True
    return False
