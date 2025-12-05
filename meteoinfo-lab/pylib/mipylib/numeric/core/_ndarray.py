# coding=utf-8
"""
NDArray class - multiple dimensional array
"""

from org.meteoinfo.data import GridArray
from org.meteoinfo.ndarray.math import ArrayMath, ArrayUtil
from org.meteoinfo.math.linalg import LinalgUtil
from org.meteoinfo.ndarray import Array, Range, MAMath, Complex, DataType
from java.time import LocalDateTime

import datetime

import _dtype
from ._base import flatiter
from ... import miutil

# The encapsulate class of Array
class NDArray(object):

    def __init__(self, array):
        if not isinstance(array, Array):
            array = ArrayUtil.array(array, None)

        self._array = array
        self.ndim = array.getRank()
        s = array.getShape()
        self._shape = tuple(s)
        self.dtype = _dtype.dtype.fromjava(array.getDataType())
        self.size = int(self._array.getSize())
        self.base = None
        if self.ndim > 0:
            self.sizestr = str(self.shape[0])
            if self.ndim > 1:
                for i in range(1, self.ndim):
                    self.sizestr = self.sizestr + '*%s' % self.shape[i]

    @property
    def jarray(self):
        return self._array

    #---- shape property
    def get_shape(self):
        return self._shape

    def set_shape(self, value):
        if isinstance(value, int):
            value = tuple([value])
        if -1 in value:
            nvalue = list(value)
            l = 1
            for i in nvalue:
                if i >= 0:
                    l *= i
            idx = nvalue.index(-1)
            nvalue[idx] = int(self._array.getSize() / l)
            value = tuple(nvalue)
        self._shape = value
        self.__init__(self._array.reshapeNoCopy(value))

    shape = property(get_shape, set_shape)

    def get_base(self):
        """
        Get base array.
        """
        if self.base is None:
            return self
        else:
            return self.base.get_base()

    @property
    def itemsize(self):
        """
        Length of one array element in bytes.
        :return: (*int*) item size.
        """
        return self.dtype.itemsize

    def __array_finalize__(self, obj):
        pass

    def __len__(self):
        if self.ndim == 0:
            return 0
        else:
            return self._shape[0]

    def __str__(self):
        return ArrayUtil.convertToString(self._array)

    def __repr__(self):
        return ArrayUtil.convertToString(self._array)

    def __getitem__(self, indices):
        if isinstance(indices, NDArray) and indices.dtype == _dtype.bool:
            r = ArrayMath.take(self._array, indices._array)
            return NDArray(r)

        if not isinstance(indices, tuple):
            indices = [indices]

        #deal with Ellipsis
        if Ellipsis in indices:
            n = self.ndim - len(indices) + 1

            indices1 = []
            for ii in indices:
                if ii is Ellipsis:
                    for _ in range(n):
                        indices1.append(slice(None))
                else:
                    indices1.append(ii)
            indices = indices1

        #for all int indices
        if len(indices) == self.ndim:
            allint = True
            aindex = self._array.getIndex()
            i = 0
            for ii in indices:
                if isinstance(ii, int):
                    if ii < 0:
                        ii = self.shape[i] + ii
                    aindex.setDim(i, ii)
                else:
                    allint = False
                    break
                i += 1
            if allint:
                if self.dtype == _dtype.dtype.char:
                    return self._array.getString(aindex)
                else:
                    r = self._array.getObject(aindex)
                    if isinstance(r, Complex):
                        return complex(r.getReal(), r.getImaginary())
                    elif isinstance(r, LocalDateTime):
                        return miutil.pydate(r)
                    else:
                        return r

        #for None index - np.newaxis
        newaxis = []
        if None in indices:
            nindices = []
            i = 0
            for ii in indices:
                if ii is None:
                    newaxis.append(i)
                else:
                    nindices.append(ii)
                i += 1
            indices = nindices

        #add slice(None) to end
        if len(indices) < self.ndim:
            if isinstance(indices, tuple):
                indices = list(indices)
            for _ in range(self.ndim - len(indices)):
                indices.append(slice(None))

        if len(indices) != self.ndim:
            print('indices must be ' + str(self.ndim) + ' dimensions!')
            raise IndexError()

        ranges = []
        flips = []
        onlyrange = True
        alllist = True
        isempty = False
        nshape = []
        squeeze = False
        for i in range(0, self.ndim):
            k = indices[i]
            if isinstance(k, int):
                if k < 0:
                    k = self._shape[i] + k
                sidx = k
                eidx = k
                step = 1
                alllist = False
                squeeze = True
            elif isinstance(k, slice):
                sidx = 0 if k.start is None else k.start
                if sidx < 0:
                    sidx = self._shape[i] + sidx
                eidx = self._shape[i] if k.stop is None else k.stop
                if eidx < 0:
                    eidx = self._shape[i] + eidx
                eidx -= 1
                step = 1 if k.step is None else k.step
                alllist = False
            elif isinstance(k, (list, tuple, NDArray)):
                if isinstance(k, (list, tuple)):
                    k = NDArray(k)
                onlyrange = False
                ranges.append(k.asarray())
                continue
            else:
                print(k)
                return None

            if step < 0:
                step = abs(step)
                flips.append(i)
                if eidx < sidx:
                    tempidx = sidx
                    sidx = eidx + 2
                    eidx = tempidx
            if sidx >= self.shape[i]:
                isempty = True
            if eidx < sidx:
                isempty = True
            else:
                rr = Range(sidx, eidx, step)
                ranges.append(rr)
            nshape.append(eidx - sidx + 1 if eidx - sidx >= 0 else 0)

        if isempty:
            r = ArrayUtil.empty([0], self.dtype._dtype)
            return NDArray(r)

        if onlyrange:
            r = ArrayMath.section(self._array, ranges, False)
        else:
            if alllist:
                r = ArrayMath.takeValues(self._array, ranges)
            else:
                r = ArrayMath.take(self._array, ranges)

        if newaxis:
            for i in flips:
                r = r.flip(i)
            rr = Array.factory(r.getDataType(), r.getShape())
            MAMath.copy(rr, r)
            rr = NDArray(rr)
            newshape = list(rr.shape)
            for i in newaxis:
                newshape.insert(i, 1)
            rr = rr.reshape(newshape)
            if onlyrange:
                rr.base = self.get_base()
            return rr

        for i in flips:
            r = r.flip(i)
        r = r.reduce()
        r = NDArray(r)
        if onlyrange:
            r.base = self.get_base()
        return r

    def __setitem__(self, indices, value):
        #print type(indices)
        if isinstance(indices, NDArray):
            if isinstance(value, NDArray):
                if value.size == indices.size == self.size:
                    ArrayMath.setValueArray(self._array, indices._array, value._array)
                    return

                value = value.asarray()

            ArrayMath.setValue(self._array, indices._array, value)
            return

        if not isinstance(indices, tuple):
            indices = [indices]

        #deal with Ellipsis
        if Ellipsis in indices:
            indices1 = []
            n = self.ndim - len(indices) + 1
            for ii in indices:
                if ii is Ellipsis:
                    for _ in range(n):
                        indices1.append(slice(None))
                else:
                    indices1.append(ii)
            indices = indices1

        if len(indices) < self.ndim:
            for _ in range(self.ndim - len(indices)):
                indices.append(slice(None))

        if self.ndim == 0:
            self._array.setObject(0, value)
            return None

        if len(indices) != self.ndim:
            print('indices must be ' + str(self.ndim) + ' dimensions!')
            raise IndexError()

        ranges = []
        flips = []
        onlyrange = True
        alllist = True
        for i in range(0, self.ndim):
            k = indices[i]
            if isinstance(k, int):
                sidx = k
                if sidx < 0:
                    sidx = self._shape[i] + sidx
                eidx = sidx
                step = 1
                alllist = False
            elif isinstance(k, (list, tuple, NDArray)):
                if isinstance(k, (list, tuple)):
                    k = NDArray(k)
                onlyrange = False
                ranges.append(k._array)
                continue
            else:
                sidx = 0 if k.start is None else k.start
                if sidx < 0:
                    sidx = self._shape[i] + sidx
                eidx = self._shape[i] if k.stop is None else k.stop
                if eidx < 0:
                    eidx = self._shape[i] + eidx
                eidx -= 1
                step = 1 if k.step is None else k.step
                alllist = False
            if step < 0:
                step = abs(step)
                flips.append(i)
            if eidx < sidx:
                return
            rr = Range(sidx, eidx, step)
            ranges.append(rr)

        if isinstance(value, (list,tuple)):
            value = ArrayUtil.array(value)
        if isinstance(value, NDArray):
            value = value.asarray()
        if onlyrange:
            r = ArrayMath.setSection(self._array, ranges, value)
        else:
            if alllist:
                r = ArrayMath.setSection_List(self._array, ranges, value)
            else:
                r = ArrayMath.setSection_Mix(self._array, ranges, value)
        self._array = r

    def __value_other(self, other):
        if isinstance(other, NDArray):
            other = other.asarray()
        elif isinstance(other, complex):
            other = Complex(other.real, other.imag)
        return other

    def __abs__(self):
        return self.array_wrap(ArrayMath.abs(self._array))

    def __add__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.add(self._array, other)
        if r is None:
            raise ValueError('Dimension mismatch, can not broadcast!')
        return self.array_wrap(r)

    def __radd__(self, other):
        return NDArray.__add__(self, other)

    def __sub__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.sub(self._array, other)
        if r is None:
            raise ValueError('Dimension mismatch, can not broadcast!')
        return self.array_wrap(r)

    def __rsub__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.sub(other, self._array)
        if r is None:
            raise ValueError('Dimension mismatch, can not broadcast!')
        return self.array_wrap(r)

    def __mul__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.mul(self._array, other)
        if r is None:
            raise ValueError('Dimension mismatch, can not broadcast!')
        return self.array_wrap(r)

    def __rmul__(self, other):
        return NDArray.__mul__(self, other)

    def __div__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.div(self._array, other)
        if r is None:
            raise ValueError('Dimension mismatch, can not broadcast!')
        return self.array_wrap(r)

    def __rdiv__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.div(other, self._array)
        if r is None:
            raise ValueError('Dimension mismatch, can not broadcast!')
        return self.array_wrap(r)

    def __floordiv__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.floorDiv(self._array, other)
        if r is None:
            raise ValueError('Dimension mismatch, can not broadcast!')
        return self.array_wrap(r)

    def __rfloordiv__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.floorDiv(other, self._array)
        if r is None:
            raise ValueError('Dimension mismatch, can not broadcast!')
        return self.array_wrap(r)

    def __mod__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.mod(self._array, other)
        if r is None:
            raise ValueError('Dimension mismatch, can not broadcast!')
        return self.array_wrap(r)

    def __rmod__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.mod(other, self._array)
        if r is None:
            raise ValueError('Dimension mismatch, can not broadcast!')
        return self.array_wrap(r)

    def __divmod__(self, other):
        return self.__floordiv__(other), self.__mod__(other)

    def __rdivmod__(self, other):
        return self.__rfloordiv__(other), self.__rmod__(other)

    def __pow__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.pow(self._array, other)
        if r is None:
            raise ValueError('Dimension mismatch, can not broadcast!')
        return self.array_wrap(r)

    def __rpow__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.pow(other, self._array)
        if r is None:
            raise ValueError('Dimension mismatch, can not broadcast!')
        return self.array_wrap(r)

    def __neg__(self):
        return self.array_wrap(ArrayMath.sub(0, self._array))

    def __lt__(self, other):
        other = NDArray.__value_other(self, other)
        return self.array_wrap(ArrayMath.lessThan(self._array, other))

    def __le__(self, other):
        other = NDArray.__value_other(self, other)
        return self.array_wrap(ArrayMath.lessThanOrEqual(self._array, other))

    def __eq__(self, other):
        if other is Ellipsis:
            return False
        other = NDArray.__value_other(self, other)
        return self.array_wrap(ArrayMath.equal(self._array, other))

    def __ne__(self, other):
        other = NDArray.__value_other(self, other)
        return self.array_wrap(ArrayMath.notEqual(self._array, other))

    def __gt__(self, other):
        other = NDArray.__value_other(self, other)
        return self.array_wrap(ArrayMath.greaterThan(self._array, other))

    def __ge__(self, other):
        other = NDArray.__value_other(self, other)
        return self.array_wrap(ArrayMath.greaterThanOrEqual(self._array, other))

    def __and__(self, other):
        other = NDArray.__value_other(self, other)
        return self.array_wrap(ArrayMath.bitAnd(self._array, other))

    def __or__(self, other):
        other = NDArray.__value_other(self, other)
        return self.array_wrap(ArrayMath.bitOr(self._array, other))

    def __xor__(self, other):
        other = NDArray.__value_other(self, other)
        return self.array_wrap(ArrayMath.bitXor(self._array, other))

    def __invert__(self):
        return self.array_wrap(ArrayMath.bitInvert(self._array))

    def __lshift__(self, other):
        other = NDArray.__value_other(self, other)
        return self.array_wrap(ArrayMath.leftShift(self._array, other))

    def __rshift__(self, other):
        other = NDArray.__value_other(self, other)
        return self.array_wrap(ArrayMath.rightShift(self._array, other))

    def __contains__(self, other):
        other = NDArray.__value_other(self, other)
        return self.array_wrap(ArrayMath.contains(self._array, other))

    def __iter__(self):
        """
        provide iteration over the values of the array
        """
        self._idx = 0
        return self

    def next(self):
        if self._idx >= self.shape[0]:
            raise StopIteration()
        else:
            value = self.__getitem__(self._idx)
            self._idx += 1
            return value

    def item(self, *args):
        """
        Copy an element of an array to a standard Python scalar and return it.

        :param args: none: in this case, the method only works for arrays with one element
            (a.size == 1), which element is copied into a standard Python scalar object and returned.
            int_type: this argument is interpreted as a flat index into the array, specifying which
            element to copy and return.
            tuple of int_types: functions as does a single int_type argument, except that the argument
            is interpreted as a nd-index into the array.

        :return: A copy of the specified element of the array as a suitable Python scalar
        """
        if self.ndim == 0:
            return  self._array.get()
        else:
            index = self._array.getIndex()
            if len(args) == 1:
                if isinstance(args[0], int):
                    index.setCurrentIndex(args[0])
                else:
                    index.set(args[0])
            elif len(args) > 1:
                index.set(args)
            return  self._array.getObject(index)

    def copy(self):
        """
        Copy array values to a new array.
        """
        return self.array_wrap(self._array.copy())

    def view(self):
        """
        New view of array with the same data.

        :return: New view of array with the same data.
        """
        return self.array_wrap(ArrayUtil.view(self._array))

    def _ufunc_finalize(self, obj, axis=None):
        """
        Return a new array after universal function finalized.

        :param obj: The object output from the universal function.
        :param axis: (*int*) The axis for ufunc compute along. Default is `None`, means not consider.

        :return: New array object.
        """
        if isinstance(obj, Array):
            return NDArray(obj)
        else:
            return obj

    def array_wrap(self, arr, axis=None):
        """
        Return a new array wrapped as self class object.

        :param arr: The array to be wrapped.
        :param axis: (*int*) The axis for ufunc compute along. Default is `None`, means not consider.

        :return: New array object.
        """
        if isinstance(arr, Array):
            return NDArray(arr)
        else:
            return arr

    def tojarray(self, dtype=None):
        """
        Convert to java array.

        :param dtype: (*string*) Data type ['double','long',None].

        :returns: (*java array*) Java array.
        """
        r = ArrayUtil.copyToNDJavaArray(self._array, dtype)
        return r

    def to_encoding(self, encoding):
        """
        Convert char array to encoding from UTF-8

        :param encoding: (*string*) Encoding string.

        :returns: (*array*) Converted array.
        """
        if self.dtype == _dtype.dtype.char:
            return NDArray(ArrayUtil.convertEncoding(self._array, encoding))
        else:
            return None

    def get_string(self, encoding='UTF-8'):
        """
        Get string from a char array.

        :param encoding: (*string*) Encoding string.

        :returns: (*string*) String.
        """
        if self.dtype == _dtype.dtype.char:
            return ArrayUtil.getString(self._array, encoding)
        else:
            return None

    def index(self, other):
        """
        Return index of the other in this array.

        :param other: (*object*) The value to be indexed.
        :return: (*int*) The index
        """
        return ArrayMath.isIn(self._array, other)


    def isin(self, other):
        """
        Return the array with the value of 1 when the element value
        in the list other, otherwise set value as 0.

        :param other: (*list or array*) List value.

        :returns: (*array*) Result array.
        """
        if not isinstance(other, (list, tuple)):
            other = other.aslist()
        return self.array_wrap(ArrayMath.isIn(self._array, other))

    def squeeze(self):
        """
        Remove single-dimensional entries from the shape of an array.

        :returns: (*array_like*) The self array, but with all or a subset of the dimensions of length 1
            removed.
        """
        return self.array_wrap(self._array.reduce())

    def all(self, axis=None):
        """
        Test whether all array element along a given axis evaluates to True.

        :param x: (*array_like or list*) Input values.
        :param axis: (*int*) Axis along which a logical OR reduction is performed.
            The default (axis = None) is to perform a logical OR over all the
            dimensions of the input array.

        :returns: (*array_like*) All result
        """
        if axis is None:
            return ArrayMath.all(self._array)
        else:
            if axis < 0:
                axis += self.ndim
            return self.array_wrap(ArrayMath.all(self._array, axis), axis=axis)

    def any(self, axis=None):
        """
        Test whether any array element along a given axis evaluates to True.

        :param axis: (*int*) Axis along which a logical OR reduction is performed.
            The default (axis = None) is to perform a logical OR over all the
            dimensions of the input array.

        :returns: (*array_like*) Any result
        """
        if axis is None:
            return ArrayMath.any(self._array)
        else:
            if axis < 0:
                axis += self.ndim
            return self.array_wrap(ArrayMath.any(self._array, axis), axis=axis)

    def contains_nan(self):
        """
        Check if the array contains nan value.

        :returns: (*boolean*) True if contains nan, otherwise return False.
        """
        return ArrayMath.containsNaN(self._array)

    def getsize(self, name='size'):
        if name == 'size':
            sizestr = str(self.shape[0])
            if self.ndim > 1:
                for i in range(1, self.ndim):
                    sizestr = sizestr + '*%s' % self.shape[i]
            return sizestr

    def astype(self, dtype):
        """
        Convert to another data type.

        :param dtype: (*str or DataType*) Data type.

        :returns: (*array*) Converted array.
        """
        if not isinstance(dtype, _dtype.DataType):
            dtype = _dtype.dtype(dtype)

        if self.dtype == dtype:
            return self.copy()

        return self.array_wrap(ArrayUtil.convertToDataType(self._array, dtype._dtype))

    @property
    def real(self):
        """
        Return the real part of the complex argument.

        :return: (*array*) The real component of the complex argument.
        """
        if self.dtype == _dtype.complex:
            return self.array_wrap(ArrayMath.getReal(self._array))
        else:
            return self

    @real.setter
    def real(self, val):
        """
        Return the real part of the complex argument.

        :param val: (*number or array*) Real value.
        """
        if self.dtype == _dtype.complex:
            if isinstance(val, (list, tuple)):
                val = NDArray(val)

            if isinstance(val, NDArray):
                ArrayMath.setReal(self._array, val._array)
            else:
                ArrayMath.setReal(self._array, val)

    @property
    def imag(self):
        """
        Return the image part of the complex argument.

        :return: (*array*) The image component of the complex argument.
        """
        if self.dtype == _dtype.complex:
            return self.array_wrap(ArrayMath.getImage(self._array))
        else:
            return self

    @imag.setter
    def imag(self, val):
        """
        Return the image part of the complex argument.

        :param val: (*number or array*) Image value.
        """
        if self.dtype == _dtype.complex:
            if isinstance(val, (list, tuple)):
                val = NDArray(val)

            if isinstance(val, NDArray):
                ArrayMath.setImage(self._array, val._array)
            else:
                ArrayMath.setImage(self._array, val)

    def conjugate(self):
        """
        Return the complex conjugate, element-wise.

        The complex conjugate of a complex number is obtained by changing the sign of its imaginary part.

        :return: (*array*) Complex conjugate array.
        """
        return self.array_wrap(ArrayMath.conj(self._array))

    conj = conjugate

    def min(self, axis=None):
        """
        Get minimum value along an axis.

        :param axis: (*int*) Axis along which the minimum is computed. The default is to
            compute the minimum of the flattened array.

        :returns: Minimum values.
        """
        if self.ndim == 1:
            axis = None
        elif isinstance(axis, (list, tuple)):
            if len(axis) == 1:
                axis = axis[0]
            elif len(axis) == self.ndim:
                axis = None

        if axis is None:
            r = ArrayMath.min(self._array)
            return r
        else:
            r = ArrayMath.min(self._array, axis)
            return self.array_wrap(r, axis=axis)

    def argmin(self, axis=None):
        """
        Returns the indices of the minimum values along an axis.

        :param axis: (*int*) By default, the index is into the flattened array, otherwise
            along the specified axis.

        :returns: Array of indices into the array. It has the same shape as a.shape with the
            dimension along axis removed.
        """
        if axis is None:
            r = ArrayMath.argMin(self._array)
            return r
        else:
            r = ArrayMath.argMin(self._array, axis)
            return self.array_wrap(r, axis=axis)

    def argmax(self, axis=None):
        """
        Returns the indices of the minimum values along an axis.

        :param axis: (*int*) By default, the index is into the flattened array, otherwise
            along the specified axis.

        :returns: Array of indices into the array. It has the same shape as a.shape with the
            dimension along axis removed.
        """
        if axis is None:
            r = ArrayMath.argMax(self._array)
            return r
        else:
            r = ArrayMath.argMax(self._array, axis)
            return self.array_wrap(r, axis=axis)

    def sort(self, axis=-1):
        """
        Sort an array in-place.

        :param: (*int*) Axis along which to sort. Default is -1, which means sort along the last axis.
        """
        self._array = ArrayUtil.sort(self._array, axis)

    def searchsorted(self, v, side='left', sorter=None):
        """
        Find indices where elements should be inserted to maintain order.

        Find the indices into a sorted array a such that, if the corresponding elements in v were inserted
        before the indices, the order of a would be preserved.

        Parameters
        ----------
        v : array_like
            Input array. If sorter is None, then it must be sorted in ascending order, otherwise sorter must
            be an array of indices that sort it.

        side : {‘left’, ‘right’}, optional
            If ‘left’, the index of the first suitable location found is given. If ‘right’, return the last
            such index. If there is no suitable index, return either 0 or N (where N is the length of a).

        sorter : 1-D array_like, optional
            Optional array of integer indices that sort array a into ascending order. They are typically
            the result of argsort.

        Returns
        -------
        indices : int or array of ints
            Array of insertion points with the same shape as v, or an integer if v is a scalar.
        """
        v = NDArray(v)
        left = True if side == 'left' else False
        r = ArrayUtil.searchSorted(self._array, v._array, left)
        if isinstance(r, int):
            return r
        else:
            return self.array_wrap(r)

    def max(self, axis=None):
        """
        Get maximum value along an axis.

        :param axis: (*int*) Axis along which the maximum is computed. The default is to
            compute the maximum of the flattened array.

        :returns: Maximum values.
        """
        if self.ndim == 1:
            axis = None
        elif isinstance(axis, (list, tuple)):
            if len(axis) == 1:
                axis = axis[0]
            elif len(axis) == self.ndim:
                axis = None

        if axis is None:
            r = ArrayMath.max(self._array)
            return r
        else:
            r = ArrayMath.max(self._array, axis)
            return self.array_wrap(r, axis=axis)

    def sum(self, axis=None):
        """
        Sum of array elements over a given axis.

        :param axis: (*int*) Axis along which the sum is computed.
            The default is to compute the sum of the flattened array.

        returns: (*array_like*) Sum result
        """
        if self.ndim == 1:
            axis = None
        elif isinstance(axis, (list, tuple)):
            if len(axis) == 1:
                axis = axis[0]
            elif len(axis) == self.ndim:
                axis = None

        if axis is None:
            return ArrayMath.sum(self._array)
        else:
            if isinstance(axis, (list, tuple)):
                aa = []
                for a in axis:
                    if a < 0:
                        a = self.ndim + a
                    aa.append(a)
                axis = aa
            else:
                if axis < 0:
                    axis = self.ndim + axis
            r = ArrayMath.sum(self._array, axis)
            return self.array_wrap(r, axis=axis)

    def cumsum(self, axis=None):
        """
        Return the cumulative sum of the elements along a given axis.

        :param axis: (*int*) Axis along which the standard deviation is computed.
            The default is to compute the standard deviation of the flattened array.

        returns: (*array_like*) Sum result
        """
        if axis is None:
            r = ArrayMath.cumsum(self._array)
        else:
            r = ArrayMath.cumsum(self._array, axis)

        return NDArray(r)

    def prod(self):
        """
        Return the product of array elements.

        :returns: (*float*) Produce value.
        """
        return ArrayMath.prodDouble(self._array)

    def abs(self):
        """
        Calculate the absolute value element-wise.

        :returns: An array containing the absolute value of each element in x.
            For complex input, a + ib, the absolute value is \sqrt{ a^2 + b^2 }.
        """
        return self.array_wrap(ArrayMath.abs(self._array))

    def ceil(self):
        """
        Return the ceiling of the input, element-wise.

        :return: The ceiling of each element.
        """
        return self.array_wrap(ArrayMath.ceil(self._array))

    def floor(self):
        """
        Return the floor of the input, element-wise.

        :return: The floor of each element.
        """
        return self.array_wrap(ArrayMath.floor(self._array))

    def round(self, decimals=0):
        """
        Round of the decimal numbers to the nearest value.

        :param: (*int*) Decimal numbers. Default is 0.

        :return: (*array*) Rounded array.
        """
        if decimals == 0:
            r = ArrayMath.round(self._array)
        else:
            r = ArrayMath.round(self._array, decimals)
        return self.array_wrap(r)

    def clip(self, min=None, max=None):
        """
        Clip (limit) the values in an array.

        Given an interval, values outside the interval are clipped to the interval edges. For example,
        if an interval of [0, 1] is specified, values smaller than 0 become 0, and values larger than 1
        become 1.

        Parameters
        ----------
        min, max : array_like or None
            Minimum and maximum value. If ``None``, clipping is not performed on
            the corresponding edge. Only one of `min` and `max` may be
            ``None``. Both are broadcast against this array.

        Returns
        -------
        clipped_array : NDArray
            An array with the elements of this array, but where values
            < `min` are replaced with `min`, and those > `max`
            with `max`.
        """
        if min is None:
            if max is None:
                raise ValueError("Only one of min and max my be None!")

            r = ArrayMath.clipMax(self._array, max)
        elif max is None:
            r = ArrayMath.clipMin(self._array, min)
        else:
            r = ArrayMath.clip(self._array, min, max)

        return self.array_wrap(r)

    def rot90(self, k=1):
        """
        Rotate an array by 90 degrees in the counter-clockwise direction. The first two dimensions
        are rotated if the array has more than 2 dimensions.

        :param k: (*int*) Number of times the array is rotated by 90 degrees

        :returns: (*array_like*) Rotated array.
        """
        r = ArrayMath.rot90(self._array, k)
        return NDArray(r)

    def ave(self, fill_value=None):
        if fill_value == None:
            return ArrayMath.aveDouble(self._array)
        else:
            return ArrayMath.aveDouble(self._array, fill_value)

    def mean(self, axis=None, keepdims=False):
        """
        Compute tha arithmetic mean along the specified axis.

        :param axis: (*int*) Axis along which the standard deviation is computed.
            The default is to compute the standard deviation of the flattened array.
        :param keepdims: (*bool*) If this is set to True, the axes which are reduced are
            left in the result as dimensions with size one. Default if `False`.

        returns: (*array_like*) Mean result
        """
        if self.ndim == 1 or axis is None:
            return ArrayMath.mean(self._array)
        else:
            r = NDArray(ArrayMath.mean(self._array, axis))
            if keepdims:
                s = list(r.shape)
                s.insert(axis, 1)
                r = r.reshape(s)
            return r

    def median(self, axis=None):
        """
        Compute tha median along the specified axis.

        :param axis: (*int*) Axis along which the standard deviation is computed.
            The default is to compute the standard deviation of the flattened array.

        returns: (*array_like*) Median result
        """
        if self.ndim == 1 or axis is None:
            return ArrayMath.median(self._array)
        else:
            return self.array_wrap(ArrayMath.median(self._array, axis), axis=axis)

    def std(self, axis=None, ddof=0):
        """
        Compute the standard deviation along the specified axis.

        :param axis: (*int*) Axis along which the standard deviation is computed.
            The default is to compute the standard deviation of the flattened array.
        :param ddof: (*int*) Delta Degrees of Freedom: the divisor used in the calculation is
            N - ddof, where N represents the number of elements. By default, ddof is zero.

        returns: (*array_like*) Standart deviation result.
        """
        if self.ndim == 1 or axis is None:
            r = ArrayMath.std(self._array, ddof)
            return r
        else:
            r = ArrayMath.std(self._array, axis, ddof)
            return self.array_wrap(r, axis=axis)

    def var(self, axis=None, ddof=0):
        """
        Compute variance along the specified axis.

        :param axis: (*int*) Axis along which the variance is computed.
            The default is to compute the variance of the flattened array.
        :param ddof: (*int*) Delta Degrees of Freedom: the divisor used in the calculation is
            N - ddof, where N represents the number of elements. By default, ddof is zero.

        returns: (*array_like*) Variance result.
        """
        if self.ndim == 1 or axis is None:
            r = ArrayMath.var(self._array, ddof)
            return r
        else:
            r = ArrayMath.var(self._array, axis, ddof)
            return self.array_wrap(r, axis=axis)

    def square(self):
        return self.__mul__(self)

    def sqrt(self):
        return self.array_wrap(ArrayMath.sqrt(self._array))

    def sin(self):
        return self.array_wrap(ArrayMath.sin(self._array))

    def sinh(self):
        return self.array_wrap(ArrayMath.sinh(self._array))

    def cos(self):
        return self.array_wrap(ArrayMath.cos(self._array))

    def cosh(self):
        return self.array_wrap(ArrayMath.cosh(self._array))

    def tan(self):
        return self.array_wrap(ArrayMath.tan(self._array))

    def tanh(self):
        return self.array_wrap(ArrayMath.tanh(self._array))

    def asin(self):
        return self.array_wrap(ArrayMath.asin(self._array))

    def acos(self):
        return self.array_wrap(ArrayMath.acos(self._array))

    def atan(self):
        return self.array_wrap(ArrayMath.atan(self._array))

    def exp(self):
        return self.array_wrap(ArrayMath.exp(self._array))

    def log(self):
        return self.array_wrap(ArrayMath.log(self._array))

    def log10(self):
        return self.array_wrap(ArrayMath.log10(self._array))

    def sign(self):
        """
        Returns an element-wise indication of the sign of a number.

        The sign function returns -1 if x < 0, 0 if x==0, 1 if x > 0. nan is returned for nan inputs.
        """
        return self.array_wrap(ArrayMath.sign(self._array))

    def dot(self, other):
        """
        Matrix multiplication.

        :param other: (*2D or 1D Array*) Matrix or vector b.

        :returns: Result Matrix or vector.
        """
        if isinstance(other, list):
            other = NDArray(ArrayUtil.array(other))

        if self.ndim == 1 and other.ndim == 1:
            return self.__mul__(other).sum()

        r = LinalgUtil.dot(self._array, other._array)
        return NDArray(r)

    def aslist(self):
        r = ArrayMath.asList(self._array)
        return list(r)

    def tolist(self):
        """
        Convert to a list
        """
        r = ArrayMath.asList(self._array)
        return list(r)

    def index(self, v):
        """
        Get index of a value in the array.

        :param v: (*object*) Value object.

        :returns: (*int*) Value index.
        """
        return self.tolist().index(v)

    def asarray(self):
        """
        Get backend Java Array

        :return: Backend Java Array
        """
        return self._array

    def backend(self):
        """
        Get backend Java Array

        :return: Backend Java Array
        """
        return self._array

    def reshape(self, *args):
        if len(args) == 1:
            shape = args[0]
            if isinstance(shape, int):
                shape = [shape]
            elif isinstance(shape, tuple):
                shape = list(shape)
        else:
            shape = []
            for arg in args:
                shape.append(arg)
        n = 1
        idx = None
        i = 0
        for s in shape:
            if s == -1:
                idx = i
            else:
                n = n * s
            i += 1
        if not idx is None:
            shape[idx] = self.size / n

        r = NDArray(self._array.reshapeNoCopy(shape))
        r.base = self.get_base()
        return r

    def transpose(self, axes=None):
        """
        Permute the dimensions of an array.

        :param axes: (*list of int*) By default, reverse the dimensions, otherwise permute the axes according to the
            values given.

        :returns: Permuted array.
        """
        if self.ndim == 1:
            return self[:]

        if axes is None:
            axes = [self.ndim-i-1 for i in range(self.ndim)]

        r = self._array.permute(axes)
        r = NDArray(r)
        r.base = self.get_base()
        return r

    T = property(transpose)

    def swapaxes(self, axis1, axis2):
        """
        Interchange two axes of an array.

        :param axis1: (*int*) First axis.
        :param axis2: (*int*) Second axis.

        :returns: Axes swapped array.
        """
        if self.ndim == 1:
            return self

        if axis1 < 0:
            axis1 = self.ndim + axis1
        if axis2 < 0:
            axis2 = self.ndim + axis2

        if axis1 == axis2:
            return self

        r = self._array.transpose(axis1, axis2)
        r = NDArray(r)
        r.base = self.get_base()
        return r

    def inv(self):
        """
        Calculate inverse matrix array.

        :returns: Inverse matrix array.
        """
        r = LinalgUtil.inv(self._array)
        return NDArray(r)

    I = property(inv)

    @property
    def flat(self):
        """
        A 1-D iterator over the array.
        :return: 1-D iterator over the array.
        """
        return flatiter(self)

    @flat.setter
    def flat(self, value):
        """
        flat setter.
        :param value: The setting value.
        """
        self.flat[:] = value

    def flatten(self, order='C'):
        """
        Return a copy of the array collapsed into one dimension.

        :param order: (*str*) Optional. ['C' | 'F'], ‘C’ means to flatten in row-major (C-style) order.
            ‘F’ means to flatten in column-major (Fortran- style) order. The default is ‘C’.

        :returns: (*NDArray*) A copy of the input array, flattened to one dimension.
        """
        shape = [self.size]
        if order.upper() == 'C':
            r = NDArray(self._array.reshape(shape))
        else:
            r = self.swapaxes(-1, -2)
            r = NDArray(r._array.reshape(shape))
        return r

    def ravel(self, order='C'):
        """
        Return a contiguous flattened array.

        :param order: (*str*) Optional. ['C' | 'F'], ‘C’ means to flatten in row-major (C-style) order.
            ‘F’ means to flatten in column-major (Fortran- style) order. The default is ‘C’.

        :returns: (*NDArray*) A contiguous flattened array.
        """
        shape = [self.size]
        if order.upper() == 'C':
            r = NDArray(self._array.reshapeNoCopy(shape))
        else:
            r = self.swapaxes(-1, -2)
            r = NDArray(r._array.reshape(shape))
        return r

    def repeat(self, repeats, axis=None):
        """
        Repeat elements of an array.

        :param repeats: (*int or list of ints*) The number of repetitions for each
            element. repeats is broadcasted to fit the shape of the given axis.
        :param axis: (*int*) The axis along which to repeat values. By default, use
            the flattened input array, and return a flat output array.

        :returns: (*array_like*) Repeated array.
        """
        if isinstance(repeats, int):
            repeats = [repeats]
        if axis is None:
            r = ArrayUtil.repeat(self._array, repeats)
        else:
            r = ArrayUtil.repeat(self._array, repeats, axis)
        return NDArray(r)

    def take(self, indices, axis=None):
        """
        Take elements from an array along an axis.

        :param indices: (*array_like*) The indices of the values to extract.
        :param axis: (*int*) The axis over which to select values.

        :returns: (*array*) The returned array has the same type as this array.
        """
        if isinstance(indices, (list, tuple)):
            indices = NDArray(indices)
        r = ArrayMath.take(self._array, indices._array, axis)
        return NDArray(r)

    def join(self, b, dimidx):
        r = ArrayMath.join(self._array, b._array, dimidx)
        return NDArray(r)

    def savegrid(self, x, y, fname, format='surfer', **kwargs):
        gdata = GridArray(self._array, x._array, y._array, -9999.0)
        if format == 'surfer':
            gdata.saveAsSurferASCIIFile(fname)
        elif format == 'bil':
            gdata.saveAsBILFile(fname)
        elif format == 'esri_ascii':
            gdata.saveAsESRIASCIIFile(fname)
        elif format == 'micaps4':
            desc = kwargs.pop('description', 'var')
            date = kwargs.pop('date', datetime.datetime.now())
            date = miutil.jdate(date)
            hours = kwargs.pop('hours', 0)
            level = kwargs.pop('level', 0)
            smooth = kwargs.pop('smooth', 1)
            boldvalue =kwargs.pop('boldvalue', 0)
            proj = kwargs.pop('proj', None)
            if proj is None:
                gdata.saveAsMICAPS4File(fname, desc, date, hours, level, smooth, boldvalue)
            else:
                if proj.isLonLat():
                    gdata.saveAsMICAPS4File(fname, desc, date, hours, level, smooth, boldvalue)
                else:
                    gdata.saveAsMICAPS4File(fname, desc, date, hours, level, smooth, boldvalue, proj)

    def tofile(self, fn, sep="", format="%s"):
        """
        Write array to a file as text or binary (default).
        :param fn: (*str*) File name.
        :param sep: (*str*) Separator between array items for text output. If “” (empty), a binary file is
            written.
        :param format: (*str*) Format string for text file output.
        """
        if sep:
            ArrayUtil.saveASCIIFile(fn, self._array, 80, format, sep)
        else:
            ArrayUtil.saveBinFile(fn, self._array, 'little_endian', False, False)