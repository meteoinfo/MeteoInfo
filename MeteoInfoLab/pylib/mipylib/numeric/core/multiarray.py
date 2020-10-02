#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2014-12-27
# Purpose: MeteoInfo Dataset module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.data import GridArray
from org.meteoinfo.math import ArrayMath, ArrayUtil
from org.meteoinfo.math.linalg import LinalgUtil
from org.meteoinfo.ndarray import Array, Range, MAMath, Complex, Dimension

import datetime

import _dtype
        
# The encapsulate class of Array
class NDArray(object):

    def __init__(self, array):
        if not isinstance(array, Array):
            array = ArrayUtil.array(array, None)
        self._array = array
        self.ndim = array.getRank()
        s = array.getShape()
        s1 = []
        for i in range(len(s)):
            s1.append(s[i])
        self._shape = tuple(s1)
        self.dtype = _dtype.fromjava(array.getDataType())
        self.size = int(self._array.getSize())
        self.iterator = array.getIndexIterator()
        self.base = None
        if self.ndim > 0:
            self.sizestr = str(self.shape[0])
            if self.ndim > 1:
                for i in range(1, self.ndim):
                    self.sizestr = self.sizestr + '*%s' % self.shape[i]
    
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
        self.__init__(self._array.reshape(value))
        
    shape = property(get_shape, set_shape)

    def get_base(self):
        '''
        Get base array.
        '''
        if self.base is None:
            return self
        else:
            return self.base.get_base()
        
    def __len__(self):
        return self._shape[0]         
        
    def __str__(self):
        return ArrayUtil.convertToString(self._array)
        
    def __repr__(self):
        return ArrayUtil.convertToString(self._array)
    
    def __getitem__(self, indices):
        if not isinstance(indices, tuple):
            inds = []
            inds.append(indices)            
            indices = inds
        
        if len(indices) < self.ndim:
            if isinstance(indices, tuple):
                indices = list(indices)
            for i in range(self.ndim - len(indices)):
                indices.append(slice(None))
            
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
            if self.dtype == _dtype.char:
                return self._array.getString(aindex)
            else:
                r = self._array.getObject(aindex)
                if isinstance(r, Complex):
                    return complex(r.getReal(), r.getImaginary())
                else:
                    return r
            
        if self.ndim == 0:
            return self

        newaxis = []
        if len(indices) > self.ndim:
            nindices = []
            i = 0
            for ii in indices:
                if ii is None:
                    newaxis.append(i)
                else:
                    nindices.append(ii)
                i += 1
            indices = nindices
        
        if len(indices) != self.ndim:
            print 'indices must be ' + str(self.ndim) + ' dimensions!'
            raise IndexError()

        ranges = []
        flips = []
        onlyrange = True
        alllist = True
        isempty = False
        nshape = []
        for i in range(0, self.ndim):  
            k = indices[i]
            if isinstance(k, int):
                if k < 0:
                    k = self._shape[i] + k
                sidx = k
                eidx = k
                step = 1
                alllist = False
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
                if isinstance(k, NDArray):
                    k = k.aslist()
                if isinstance(k[0], bool):
                    kk = []
                    for i in range(len(k)):
                        if k[i]:
                            kk.append(i)
                    k = kk                        
                onlyrange = False
                ranges.append(k)
                continue
            else:
                print k
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
            r = ArrayMath.section(self._array, ranges)
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
                
        if r.getSize() == 1:
            iter = r.getIndexIterator()
            if self.dtype == _dtype.char:
                return iter.getStringNext()
            else:
                r = iter.getObjectNext()
                if isinstance(r, Complex):
                    return complex(r.getReal(), r.getImaginary())
                else:
                    return r
        else:
            for i in flips:
                r = r.flip(i)
            r = NDArray(r)
            if onlyrange:
                r.base = self.get_base()
            return r
        
    def __setitem__(self, indices, value):
        #print type(indices) 
        if isinstance(indices, NDArray):
            if isinstance(value, NDArray):
                value = value.asarray()
            ArrayMath.setValue(self._array, indices._array, value)
            return None
        
        if not isinstance(indices, tuple):
            inds = []
            inds.append(indices)
            indices = inds
            
        if len(indices) < self.ndim:
            for i in range(self.ndim - len(indices)):
                indices.append(slice(None))
        
        if self.ndim == 0:
            self._array.setObject(0, value)
            return None
        
        if len(indices) != self.ndim:
            print 'indices must be ' + str(self.ndim) + ' dimensions!'
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
                if isinstance(k, NDArray):
                    k = k.aslist()
                onlyrange = False
                ranges.append(k)
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
        return other
    
    def __abs__(self):
        return NDArray(ArrayMath.abs(self._array))
    
    def __add__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.add(self._array, other)        
        if r is None:
            raise ValueError('Dimension missmatch, can not broadcast!')
        return NDArray(r)
        
    def __radd__(self, other):
        return NDArray.__add__(self, other)
        
    def __sub__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.sub(self._array, other)        
        if r is None:
            raise ValueError('Dimension missmatch, can not broadcast!')
        return NDArray(r)
        
    def __rsub__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.sub(other, self._array)        
        if r is None:
            raise ValueError('Dimension missmatch, can not broadcast!')
        return NDArray(r)
    
    def __mul__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.mul(self._array, other)
        if r is None:
            raise ValueError('Dimension missmatch, can not broadcast!')
        return NDArray(r)
        
    def __rmul__(self, other):
        return NDArray.__mul__(self, other)
        
    def __div__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.div(self._array, other)        
        if r is None:
            raise ValueError('Dimension missmatch, can not broadcast!')
        return NDArray(r)
        
    def __rdiv__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.div(other, self._array)        
        if r is None:
            raise ValueError('Dimension missmatch, can not broadcast!')
        return NDArray(r)
        
    def __pow__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.pow(self._array, other)        
        if r is None:
            raise ValueError('Dimension missmatch, can not broadcast!')
        return NDArray(r)
        
    def __rpow__(self, other):
        other = NDArray.__value_other(self, other)
        r = ArrayMath.pow(other, self._array)        
        if r is None:
            raise ValueError('Dimension missmatch, can not broadcast!')
        return NDArray(r)
        
    def __neg__(self):
        r = NDArray(ArrayMath.sub(0, self._array))
        return r
        
    def __lt__(self, other):
        other = NDArray.__value_other(self, other)
        r = NDArray(ArrayMath.lessThan(self._array, other))
        return r
        
    def __le__(self, other):
        other = NDArray.__value_other(self, other)
        r = NDArray(ArrayMath.lessThanOrEqual(self._array, other))        
        return r
        
    def __eq__(self, other):
        other = NDArray.__value_other(self, other)
        r = NDArray(ArrayMath.equal(self._array, other))
        return r
        
    def __ne__(self, other):
        other = NDArray.__value_other(self, other)
        r = NDArray(ArrayMath.notEqual(self._array, other))
        return r
        
    def __gt__(self, other):
        other = NDArray.__value_other(self, other)
        r = NDArray(ArrayMath.greaterThan(self._array, other))
        return r
        
    def __ge__(self, other):
        other = NDArray.__value_other(self, other)
        r = NDArray(ArrayMath.greaterThanOrEqual(self._array, other))
        return r
        
    def __and__(self, other):
        other = NDArray.__value_other(self, other)
        r = NDArray(ArrayMath.bitAnd(self._array, other))
        return r
        
    def __or__(self, other):
        other = NDArray.__value_other(self, other)
        r = NDArray(ArrayMath.bitOr(self._array, other))
        return r
        
    def __xor__(self, other):
        other = NDArray.__value_other(self, other)
        r = NDArray(ArrayMath.bitXor(self._array, other))
        return r
        
    def __invert__(self):
        r = NDArray(ArrayMath.bitInvert(self._array))
        return r
        
    def __lshift__(self, other):
        other = NDArray.__value_other(self, other)
        r = NDArray(ArrayMath.leftShift(self._array, other))
        return r
        
    def __rshift__(self, other):
        other = NDArray.__value_other(self, other)
        r = NDArray(ArrayMath.rightShift(self._array, other))
        return r     

    def __iter__(self):
        """
        provide iteration over the values of the array
        """
        #self.idx = -1
        self.iterator = self._array.getIndexIterator()
        return self
        
    def next(self):
        if self.iterator.hasNext():
            return self.iterator.getObjectNext()
        else:
            raise StopIteration()
        # self.idx += 1
        # if self.idx >= self.size:
            # raise StopIteration()        
        # return self._array.getObject(self.idx)
        
    def copy(self):
        '''
        Copy array values to a new array.
        '''
        return NDArray(self._array.copy())

    def view(self):
        '''
        New view of array with the same data.

        :return: New view of array with the same data.
        '''
        return NDArray(ArrayUtil.view(self._array))
        
    def tojarray(self, dtype=None):
        '''
        Convert to java array.

        :param dtype: (*string*) Data type ['double','long',None]. 
        
        :returns: (*java array*) Java array.
        '''
        r = ArrayUtil.copyToNDJavaArray(self._array, dtype)
        return r

    def to_encoding(self, encoding):
        '''
        Convert char array to encoding from UTF-8

        :param encoding: (*string*) Encoding string.

        :returns: (*array*) Converted array.
        '''
        if self.dtype == _dtype.char:
            return NDArray(ArrayUtil.convertEncoding(self._array, encoding))
        else:
            return None

    def get_string(self, encoding='UTF-8'):
        '''
        Get string from a char array.

        :param encoding: (*string*) Encoding string.

        :returns: (*string*) String.
        '''
        if self.dtype == _dtype.char:
            return ArrayUtil.getString(self._array, encoding)
        else:
            return None
    
    def in_values(self, other):
        '''
        Return the array with the value of 1 when the element value
        in the list other, otherwise set value as 0.
        
        :param other: (*list or array*) List value.
        
        :returns: (*array*) Result array.
        '''
        if not isinstance(other, (list, tuple)):
            other = other.aslist()
        r = NDArray(ArrayMath.inValues(self._array, other))
        return r
        
    def contains_nan(self):
        '''
        Check if the array contains nan value.
        
        :returns: (*boolean*) True if contains nan, otherwise return False.
        '''
        return ArrayMath.containsNaN(self._array)
    
    def getsize(self):
        if name == 'size':
            sizestr = str(self.shape[0])
            if self.ndim > 1:
                for i in range(1, self.ndim):
                    sizestr = sizestr + '*%s' % self.shape[i]
            return sizestr
    
    def astype(self, dtype):
        '''
        Convert to another data type.
        
        :param dtype: (*string*) Data type.
        
        :returns: (*array*) Converted array.
        '''
        if not isinstance(dtype, _dtype.DataType):
            dtype = _dtype.DataType(dtype)
        if dtype.kind == 'i':
            r = NDArray(ArrayUtil.toInteger(self._array))
        elif dtype.kind == 'f':
            if dtype.name == 'float':
                r = NDArray(ArrayUtil.toFloat(self._array))
            else:
                r = NDArray(ArrayUtil.toDouble(self._array))
        elif dtype.kind == 'b':
            r = NDArray(ArrayUtil.toBoolean(self._array))
        else:
            r = self
        return r
        
    def min(self, axis=None):
        '''
        Get minimum value along an axis.
        
        :param axis: (*int*) Axis along which the minimum is computed. The default is to 
            compute the minimum of the flattened array.
            
        :returns: Minimum values.
        '''
        if axis is None:
            r = ArrayMath.min(self._array)
            return r
        else:
            r = ArrayMath.min(self._array, axis)
            return NDArray(r)
            
    def argmin(self, axis=None):
        '''
        Returns the indices of the minimum values along an axis.
        
        :param axis: (*int*) By default, the index is into the flattened array, otherwise 
            along the specified axis.
            
        :returns: Array of indices into the array. It has the same shape as a.shape with the 
            dimension along axis removed.
        '''
        if axis is None:
            r = ArrayMath.argMin(self._array)
            return r
        else:
            r = ArrayMath.argMin(self._array, axis)
            return NDArray(r)
            
    def argmax(self, axis=None):
        '''
        Returns the indices of the minimum values along an axis.
        
        :param axis: (*int*) By default, the index is into the flattened array, otherwise 
            along the specified axis.
            
        :returns: Array of indices into the array. It has the same shape as a.shape with the 
            dimension along axis removed.
        '''
        if axis is None:
            r = ArrayMath.argMax(self._array)
            return r
        else:
            r = ArrayMath.argMax(self._array, axis)
            return NDArray(r)
        
    def max(self, axis=None):
        '''
        Get maximum value along an axis.
        
        :param axis: (*int*) Axis along which the maximum is computed. The default is to 
            compute the maximum of the flattened array.
            
        :returns: Maximum values.
        '''
        if axis is None:
            r = ArrayMath.max(self._array)
            return r
        else:
            r = ArrayMath.max(self._array, axis)
            return NDArray(r)
        
    def sum(self, axis=None):
        '''
        Sum of array elements over a given axis.

        :param axis: (*int*) Axis along which the standard deviation is computed. 
            The default is to compute the standard deviation of the flattened array.
        
        returns: (*array_like*) Sum result
        '''
        if axis is None:
            return ArrayMath.sum(self._array)
        else:
            r = ArrayMath.sum(self._array, axis)
            return NDArray(r)

    def cumsum(self, axis=None):
        '''
        Return the cumulative sum of the elements along a given axis.

        :param axis: (*int*) Axis along which the standard deviation is computed.
            The default is to compute the standard deviation of the flattened array.

        returns: (*array_like*) Sum result
        '''
        if axis is None:
            return ArrayMath.cumsum(self.flatten()._array, 0)
        else:
            r = ArrayMath.cumsum(self._array, axis)
            return NDArray(r)
            
    def prod(self):
        '''
        Return the product of array elements.
        
        :returns: (*float*) Produce value.
        '''
        return ArrayMath.prodDouble(self._array)
        
    def abs(self):
        '''
        Calculate the absolute value element-wise.
        
        :returns: An array containing the absolute value of each element in x. 
            For complex input, a + ib, the absolute value is \sqrt{ a^2 + b^2 }.
        '''
        return NDArray(ArrayMath.abs(self._array))

    def ceil(self):
        '''
        Return the ceiling of the input, element-wise.

        :return: The ceiling of each element.
        '''
        return NDArray(ArrayMath.ceil(self._array))

    def floor(self):
        '''
        Return the floor of the input, element-wise.

        :return: The floor of each element.
        '''
        return NDArray(ArrayMath.floor(self._array))
            
    def ave(self, fill_value=None):
        if fill_value == None:
            return ArrayMath.aveDouble(self._array)
        else:
            return ArrayMath.aveDouble(self._array, fill_value)
            
    def mean(self, axis=None):
        '''
        Compute tha arithmetic mean along the specified axis.

        :param axis: (*int*) Axis along which the standard deviation is computed. 
            The default is to compute the standard deviation of the flattened array.
        
        returns: (*array_like*) Mean result
        '''
        if self.ndim == 1 or axis is None:
            return ArrayMath.mean(self._array)
        else:
            return NDArray(ArrayMath.mean(self._array, axis))
            
    def median(self, axis=None):
        '''
        Compute tha median along the specified axis.

        :param axis: (*int*) Axis along which the standard deviation is computed. 
            The default is to compute the standard deviation of the flattened array.
        
        returns: (*array_like*) Median result
        '''
        if self.ndim == 1 or axis is None:
            return ArrayMath.median(self._array)
        else:
            return NDArray(ArrayMath.median(self._array, axis))
            
    def std(self, axis=None, ddof=0):
        '''
        Compute the standard deviation along the specified axis.

        :param axis: (*int*) Axis along which the standard deviation is computed. 
            The default is to compute the standard deviation of the flattened array.
        :param ddof: (*int*) Delta Degrees of Freedom: the divisor used in the calculation is
            N - ddof, where N represents the number of elements. By default ddof is zero.
        
        returns: (*array_like*) Standart deviation result.
        '''
        if self.ndim == 1 or axis is None:
            r = ArrayMath.std(self._array, ddof)
            return r
        else:
            r = ArrayMath.std(self._array, axis, ddof)
            return NDArray(r)

    def var(self, axis=None, ddof=0):
        '''
        Compute variance along the specified axis.

        :param axis: (*int*) Axis along which the variance is computed.
            The default is to compute the variance of the flattened array.
        :param ddof: (*int*) Delta Degrees of Freedom: the divisor used in the calculation is
            N - ddof, where N represents the number of elements. By default ddof is zero.

        returns: (*array_like*) Variance result.
        '''
        if self.ndim == 1 or axis is None:
            r = ArrayMath.var(self._array, ddof)
            return r
        else:
            r = ArrayMath.var(self._array, axis, ddof)
            return NDArray(r)

    def square(self):
        return self.__mul__(self)

    def sqrt(self):
        return NDArray(ArrayMath.sqrt(self._array))
    
    def sin(self):
        return NDArray(ArrayMath.sin(self._array))
        
    def cos(self):
        return NDArray(ArrayMath.cos(self._array))
        
    def tan(self):
        return NDArray(ArrayMath.tan(self._array))
        
    def asin(self):
        return NDArray(ArrayMath.asin(self._array))
        
    def acos(self):
        return NDArray(ArrayMath.acos(self._array))
        
    def atan(self):
        return NDArray(ArrayMath.atan(self._array))
        
    def exp(self):
        return NDArray(ArrayMath.exp(self._array))
        
    def log(self):
        return NDArray(ArrayMath.log(self._array))
        
    def log10(self):
        return NDArray(ArrayMath.log10(self._array))
        
    def sign(self):
        '''
        Returns an element-wise indication of the sign of a number.

        The sign function returns -1 if x < 0, 0 if x==0, 1 if x > 0. nan is returned for nan inputs.
        '''
        return NDArray(ArrayMath.sign(self._array))
        
    def dot(self, other):
        """
        Matrix multiplication.
        
        :param other: (*2D or 1D Array*) Matrix or vector b.
        
        :returns: Result Matrix or vector.
        """  
        if isinstance(other, list):
            other = array(other)
        r = ArrayMath.dot(self._array, other._array)
        return NDArray(r)
            
    def aslist(self):
        r = ArrayMath.asList(self._array)
        return list(r)
        
    def tolist(self):
        '''
        Convert to a list
        '''
        r = ArrayMath.asList(self._array)
        return list(r)
        
    def index(self, v):
        '''
        Get index of a value in the array.
        
        :param v: (*object*) Value object.
        
        :returns: (*int*) Value index.
        '''
        return self.tolist().index(v)

    def asarray(self):
        '''
        Get backend Java Array

        :return: Backend Java Array
        '''
        return self._array

    def backend(self):
        '''
        Get backend Java Array

        :return: Backend Java Array
        '''
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

        r = NDArray(self._array.reshape(shape))
        r.base = self.get_base()
        return r
        
    def transpose(self, axes=None):
        '''
        Permute the dimensions of an array.

        :param axes: (*list of int*) By default, reverse the dimensions, otherwise permute the axes according to the
            values given.
        
        :returns: Permuted array.
        '''
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
        '''
        Interchange two axes of an array.

        :param axis1: (*int*) First axis.
        :param axis2: (*int*) Second axis.

        :returns: Axes swapped array.
        '''
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
        '''
        Calculate inverse matrix array.
        
        :returns: Inverse matrix array.
        '''
        r = LinalgUtil.inv(self._array)
        return NDArray(r)
        
    I = property(inv)
        
    def flatten(self):
        '''
        Return a copy of the array collapsed into one dimension.
        
        :returns: (*NDArray*) A copy of the input array, flattened to one dimension.
        '''
        r = self.reshape(int(self._array.getSize()))
        return r

    def ravel(self):
        '''
        Return a copy of the array collapsed into one dimension.

        :returns: (*NDArray*) A copy of the input array, flattened to one dimension.
        '''
        r = self.reshape(int(self._array.getSize()))
        return r
        
    def repeat(self, repeats, axis=None):
        '''
        Repeat elements of an array.
        
        :param repeats: (*int or list of ints*) The number of repetitions for each 
            element. repeats is broadcasted to fit the shape of the given axis.
        :param axis: (*int*) The axis along which to repeat values. By default, use 
            the flattened input array, and return a flat output array.
        
        :returns: (*array_like*) Repeated array.
        '''
        if isinstance(repeats, int):
            repeats = [repeats]
        if axis is None:
            r = ArrayUtil.repeat(self._array, repeats)
        else:
            r = ArrayUtil.repeat(self._array, repeats, axis)
        return NDArray(r)
        
    def take(self, indices, axis=None):
        '''
        Take elements from an array along an axis.
        
        :param indices: (*array_like*) The indices of the values to extract.
        :param axis: (*int*) The axis over which to select values.
        
        :returns: (*array*) The returned array has the same type as a.
        '''
        if isinstance(indices, (list, tuple)):
            indices = NDArray(indices)
        r = ArrayMath.take(self._array, indices._array, axis)
        return NDArray(r)
    
    def asdimarray(self, x, y, fill_value=-9999.0):
        dims = []
        ydim = Dimension(DimensionType.Y)
        ydim.setDimValues(y.aslist())
        dims.append(ydim)
        xdim = Dimension(DimensionType.X)
        xdim.setDimValues(x.aslist())
        dims.append(xdim)        
        return DimArray(self, dims, fill_value)
        
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
