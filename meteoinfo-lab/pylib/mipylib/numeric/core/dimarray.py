#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2014-12-27
# Purpose: MeteoInfo dimarray module
# Note: Jython
#-----------------------------------------------------
from org.meteoinfo.projection import KnownCoordinateSystems, Reproject, ProjectionInfo
from org.meteoinfo.data import GridData, GridArray
from org.meteoinfo.ndarray.math import ArrayMath, ArrayUtil
from org.meteoinfo.geometry.geoprocess import GeometryUtil
from org.meteoinfo.common import ResampleMethods
from org.meteoinfo.common import PointD
from org.meteoinfo.ndarray import Array, Range, MAMath, DataType
from org.meteoinfo.data.dimarray import Dimension, DimensionType
from ._ndarray import NDArray
import math
import datetime
import numbers
import mipylib.miutil as miutil
from java.lang import Double
from java.util import ArrayList

nan = Double.NaN

def dimension(value, name='null', type=None):
    """
    Create a new Dimension.

    :param value: (*array_like*) Dimension value.
    :param name: (*string*) Dimension name.
    :param type: (*string*) Dimension type ['X' | 'Y' | 'Z' | 'T'].
    """
    if isinstance(value, NDArray):
        value = value.aslist()
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
    dim.setDimValues(value)
    dim.setShortName(name)
    return dim

# Dimension array
class DimArray(NDArray):
    
    def __init__(self, array, dims=None, proj=ProjectionInfo.LONG_LAT):
        if isinstance(array, NDArray):
            array = array._array
        super(DimArray, self).__init__(array)
        self.dims = None
        if not dims is None:
            for dim in dims:
                self.adddim(dim)
        self.proj = proj
        
    def __getitem__(self, indices):
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
                return self._array.getObject(aindex)

        #for None index - np.newaxis
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

        #add slice(None) to end
        if len(indices) < self.ndim:
            if isinstance(indices, tuple):
                indices = list(indices)
            for i in range(self.ndim - len(indices)):
                indices.append(slice(None))
        
        if len(indices) != self.ndim:
            print('indices must be ' + str(self.ndim) + ' dimensions!')
            raise IndexError()
            
        if not self.proj is None and not self.proj.isLonLat():
            xlim = None
            ylim = None
            xidx = -1
            yidx = -1
            for i in range(0, self.ndim):
                dim = self.dims[i]
                if dim.getDimType() == DimensionType.X:                    
                    k = indices[i]
                    #if isinstance(k, (tuple, list)):
                    if isinstance(k, basestring):
                        xlims = k.split(':')
                        xlim = [float(xlims[0]), float(xlims[1])]
                        xidx = i
                elif dim.getDimType() == DimensionType.Y:
                    k = indices[i]
                    #if isinstance(k, (tuple, list)):
                    if isinstance(k, basestring):
                        ylims = k.split(':')
                        ylim = [float(ylims[0]), float(ylims[1])]
                        yidx = i
            if not xlim is None and not ylim is None:                
                fromproj=KnownCoordinateSystems.geographic.world.WGS1984
                inpt = PointD(xlim[0], ylim[0])
                outpt1 = Reproject.reprojectPoint(inpt, fromproj, self.proj)
                inpt = PointD(xlim[1], ylim[1])
                outpt2 = Reproject.reprojectPoint(inpt, fromproj, self.proj)
                xlim = [outpt1.X, outpt2.X]
                ylim = [outpt1.Y, outpt2.Y]
                indices1 = []
                for i in range(0, self.ndim):
                    if i == xidx:
                        #indices1.append(xlim
                        indices1.append(str(xlim[0]) + ':' + str(xlim[1]))
                    elif i == yidx:
                        #indices1.append(ylim)
                        indices1.append(str(ylim[0]) + ':' + str(ylim[1]))
                    else:
                        indices1.append(indices[i])
                indices = indices1

        ndims = []
        ranges = []
        flips = []
        iszerodim = True
        onlyrange = True
        alllist = True
        isempty = False
        nshape = []
        squeeze = False
        for i in range(0, self.ndim):  
            isrange = True
            k = indices[i]
            if isinstance(k, int):
                if k < 0:
                    k = self.dims[i].getLength() + k
                sidx = k
                eidx = k
                step = 1       
                alllist = False
                squeeze = True
            elif isinstance(k, slice):
                if isinstance(k.start, basestring):
                    sv = float(k.start)
                    sidx = self.dims[i].getValueIndex(sv)
                elif isinstance(k.start, datetime.datetime):
                    sv = miutil.date2num(k.start)
                    sidx = self.dims[i].getValueIndex(sv)
                else:
                    sidx = 0 if k.start is None else k.start
                if sidx < 0:
                    sidx = self.dims[i].getLength() + sidx
                    
                if isinstance(k.stop, basestring):
                    ev = float(k.stop)
                    eidx = self.dims[i].getValueIndex(ev)
                elif isinstance(k.stop, datetime.datetime):
                    ev = miutil.date2num(k.stop)
                    eidx = self.dims[i].getValueIndex(ev)
                else:
                    eidx = self.dims[i].getLength() if k.stop is None else k.stop
                    if eidx < 0:
                        eidx = self.dims[i].getLength() + eidx
                    eidx -= 1
                    
                if isinstance(k.step, basestring):
                    nv = float(k.step) + self.dims[i].getDimValue()[0]
                    nidx = self.dims[i].getValueIndex(nv)
                    step = nidx - sidx
                elif isinstance(k.step, datetime.timedelta):
                    nv = miutil.date2num(k.start + k.step)
                    nidx = self.dims[i].getValueIndex(nv)
                    step = nidx - sidx
                else:
                    step = 1 if k.step is None else k.step
                alllist = False
            elif isinstance(k, (list, tuple, NDArray)):
                onlyrange = False
                isrange = False
                if not isinstance(k[0], datetime.datetime):
                    if isinstance(k, (list, tuple)):
                        k = NDArray(k)
                    # if isinstance(k, NDArray):
                    #     k = k.aslist()
                    # ranges.append(k)
                else:
                    tlist = []
                    for tt in k:
                        sv = miutil.date2num(tt)
                        idx = self.dims[i].getValueIndex(sv)
                        tlist.append(idx)
                    # ranges.append(tlist)
                    k = NDArray(tlist)
                ranges.append(k.asarray())
            elif isinstance(k, basestring):
                dim = self.dims[i]
                kvalues = k.split(':')
                sidx = dim.getValueIndex(float(kvalues[0]))
                if len(kvalues) == 1:
                    eidx = sidx
                    step = 1
                    squeeze = True
                else:                    
                    eidx = dim.getValueIndex(float(kvalues[1]))
                    if len(kvalues) == 2:
                        step = 1
                    else:
                        step = int(float(kvalues[2]) / dim.getDeltaValue())
                    if sidx > eidx and step > 0:
                        step = -step
                alllist = False
            else:                
                print(k)
                raise IndexError()
                
            if isrange:
                if sidx >= self.shape[i] or eidx >= self.shape[i]:
                    raise IndexError()

                n = abs(eidx - sidx) + 1
                if n > 1:
                    dim = self.dims[i]
                    ndims.append(dim.extract(sidx, eidx, step))

                if step < 0:
                    #step = abs(step)
                    flips.append(i)
                if sidx > eidx:
                    iidx = eidx
                    eidx = sidx
                    sidx = iidx
                rr = Range(sidx, eidx, abs(step))
                ranges.append(rr)
                nshape.append(eidx - sidx + 1 if eidx - sidx >= 0 else 0)
            else:
                if len(k) > 1:
                    dim = self.dims[i]
                    if isinstance(k, NDArray):
                        k = k.asarray()
                    ndims.append(dim.extract(k))

        if isempty:
            r = ArrayUtil.zeros(nshape, 'int')
            return NDArray(r)
        
        if onlyrange:
            r = ArrayMath.section(self._array, ranges, squeeze)
        else:
            if alllist:
                r = ArrayMath.takeValues(self._array, ranges)
                return NDArray(r)
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
        data = DimArray(r, ndims, self.proj)
        if onlyrange:
            data.base = self.get_base()
        return data
        
    def __add__(self, other):
        r = super(DimArray, self).__add__(other)
        return DimArray(r, self.dims, self.proj)
        
    def __radd__(self, other):
        return DimArray.__add__(self, other)
        
    def __sub__(self, other):
        r = super(DimArray, self).__sub__(other)
        return DimArray(r, self.dims, self.proj)
        
    def __rsub__(self, other):
        r = super(DimArray, self).__rsub__(other)
        return DimArray(r, self.dims, self.proj)
        
    def __mul__(self, other):
        r = super(DimArray, self).__mul__(other)
        return DimArray(r, self.dims, self.proj)
        
    def __rmul__(self, other):
        return DimArray.__mul__(self, other)
        
    def __div__(self, other):
        r = super(DimArray, self).__div__(other)
        return DimArray(r, self.dims, self.proj)
        
    def __rdiv__(self, other):
        r = super(DimArray, self).__rdiv__(other)
        return DimArray(r, self.dims, self.proj)

    def __floordiv__(self, other):
        r = super(DimArray, self).__floordiv__(other)
        return DimArray(r, self.dims, self.proj)

    def __rfloordiv__(self, other):
        r = super(DimArray, self).__rfloordiv__(other)
        return DimArray(r, self.dims, self.proj)

    def __mod__(self, other):
        r = super(DimArray, self).__mod__(other)
        return DimArray(r, self.dims, self.proj)

    def __rmod__(self, other):
        r = super(DimArray, self).__rmod__(other)
        return DimArray(r, self.dims, self.proj)

    def __divmod__(self, other):
        r = super(DimArray, self).__divmod__(other)
        return DimArray(r, self.dims, self.proj)

    def __rdivmod__(self, other):
        r = super(DimArray, self).__rdivmod__(other)
        return DimArray(r, self.dims, self.proj)
        
    def __pow__(self, other):
        r = super(DimArray, self).__pow__(other)
        return DimArray(r, self.dims, self.proj)
        
    def __rpow__(self, other):
        r = super(DimArray, self).__rpow__(other)
        return DimArray(r, self.dims, self.proj)
        
    def __neg__(self):
        r = super(DimArray, self).__neg__()
        return DimArray(r, self.dims, self.proj)
        
    def __lt__(self, other):
        r = super(DimArray, self).__lt__(other)
        return DimArray(r, self.dims, self.proj)
        
    def __le__(self, other):
        r = super(DimArray, self).__le__(other)
        return DimArray(r, self.dims, self.proj)
        
    def __eq__(self, other):
        r = super(DimArray, self).__eq__(other)
        return DimArray(r, self.dims, self.proj)
        
    def __ne__(self, other):
        r = super(DimArray, self).__ne__(other)
        return DimArray(r, self.dims, self.proj)
        
    def __gt__(self, other):
        r = super(DimArray, self).__gt__(other)
        return DimArray(r, self.dims, self.proj)
        
    def __ge__(self, other):
        r = super(DimArray, self).__ge__(other)
        return DimArray(r, self.dims, self.proj)

    def __and__(self, other):
        r = super(DimArray, self).__and__(other)
        return DimArray(r, self.dims, self.proj)
        
    def __or__(self, other):
        r = super(DimArray, self).__or__(other)
        return DimArray(r, self.dims, self.proj)
        
    def __xor__(self, other):
        r = super(DimArray, self).__xor__(other)
        return DimArray(r, self.dims, self.proj)
        
    def __invert__(self, other):
        r = super(DimArray, self).__invert__(other)
        return DimArray(r, self.dims, self.proj)
        
    def __lshift__(self, other):
        r = super(DimArray, self).__lshift__(other)
        return DimArray(r, self.dims, self.proj)
        
    def __rshift__(self, other):
        r = super(DimArray, self).__rshift__(other)
        return DimArray(r, self.dims, self.proj)

    @property
    def array(self):
        """
        Get NDArray object
        :return: NDArray object
        """
        return NDArray(self._array)

    @array.setter
    def array(self, value):
        """
        Set NDArray object
        :param value: NDArray object
        """
        self._array = value._array
    
    def member_names(self):
        """
        Get member names. Only valid for Structure data type.
        
        :returns: (*list*) Member names
        """
        if self._array.getDataType() != DataType.STRUCTURE:
            print 'This method is only valid for structure array!'
            return None
            
        ms = self._array.getStructureMemberNames()
        return list(ms)
    
    def member_array(self, member, indices=None):
        """
        Extract member array. Only valid for Structure data type.
        
        :param member: (*string*) Member name.
        :param indices: (*slice*) Indices.
        
        :returns: (*array*) Extracted member array.
        """
        if self._array.getDataType() != DataType.STRUCTURE:
            print 'This method is only valid for structure array!'
            return None

        a = self._array.getArrayObject()
        m = a.findMember(member)
        if m is None:
            raise KeyError('The member %s not exists!' % member)
            
        a = a.extractMemberArray(m)
        r = DimArray(a, self.dims, self.proj)
        if not indices is None:
            r = r.__getitem__(indices)
            
        return r
    
    def in_values(self, other):
        """
        The returned array element set 1 when the input array element is in other, otherwise the
        element set 0.
        
        :param other: (*array_like*) The array or list value.
        
        :returns: (*array*) The array with element value of 1 or 0.
        """
        r = super(DimArray, self).in_values(other)
        return DimArray(r, self.dims, self.proj)
        
    def astype(self, dtype):
        """
        Convert to another data type.
        
        :param dtype: (*string*) Data type.
        
        :returns: (*array*) Converted array.
        """
        r = super(DimArray, self).astype(dtype)
        return DimArray(r, self.dims, self.proj)
    
    def value(self, indices):
        #print type(indices)
        if not isinstance(indices, tuple):
            inds = []
            inds.append(indices)
            indices = inds
        
        if len(indices) != self.ndim:
            print 'indices must be ' + str(self.ndim) + ' dimensions!'
            return None
            
        #origin = []
        #size = []
        #stride = []
        dims = []
        ranges = []
        flips = []
        for i in range(0, self.ndim):  
            k = indices[i]
            if isinstance(indices[i], int):
                sidx = k
                eidx = k
                step = 1                
            elif isinstance(k, slice):
                sidx = 0 if k.start is None else k.start
                eidx = self.dims[i].getLength()-1 if k.stop is None else k.stop
                step = 1 if k.step is None else k.step
            elif isinstance(k, tuple) or isinstance(k, list):
                dim = self.dims[i]
                sidx = dim.getValueIndex(k[0])
                if len(k) == 1:
                    eidx = sidx
                    step = 1
                else:                    
                    eidx = dim.getValueIndex(k[1])
                    if len(k) == 2:
                        step = 1
                    else:
                        step = int(k[2] / dim.getDeltaValue)
            else:
                print k
                return None
                
            if step < 0:
                step = abs(step)
                flips.append(i)
            rr = Range(sidx, eidx, step)
            ranges.append(rr)
            #origin.append(sidx)
            n = eidx - sidx + 1
            #size.append(n)
            #stride.append(step)
            if n > 1:
                dim = self.dims[i]
                dims.append(dim.extract(sidx, eidx, step))
                    
        #r = ArrayMath.section(self._array, origin, size, stride)
        r = ArrayMath.section(self._array, ranges)
        for i in flips:
            r = r.flip(i)
        rr = Array.factory(r.getDataType(), r.getShape());
        MAMath.copy(rr, r);
        array = NDArray(rr)
        data = DimArray(array, dims, self.proj)
        return data
        
    def copy(self):
        """
        Copy array vlaues to a new array.
        """
        return DimArray(self._array.copy(), self.dims, self.proj)

    def _ufunc_finalize(self, obj, axis=None):
        """
        Return a new array after universal function finalized.

        :param obj: The object output from the universal function.
        :param axis: (*int*) The axis for ufunc compute along. Default is `None`, means not consider.

        :return: New array object.
        """
        if isinstance(obj, (Array, NDArray)):
            if axis is None:
                return DimArray(obj, self.dims, self.proj)
            else:
                dims = []
                for i in range(0, self.ndim):
                    if i != axis:
                        dims.append(self.dims[i])
                return DimArray(obj, dims, self.proj)
        else:
            return obj

    def array_wrap(self, arr, axis=None):
        """
        Return a new array wrapped as self class object.

        :param arr: The array to be wrapped.
        :param axis: (*int*) The axis for ufunc compute along. Default is `None`, means not consider.

        :return: New array object.
        """
        if isinstance(arr, (Array, NDArray)):
            if axis is None:
                return DimArray(arr, self.dims, self.proj)
            else:
                dims = []
                for i in range(0, self.ndim):
                    if i != axis:
                        dims.append(self.dims[i])
                return DimArray(arr, dims, self.proj)
        else:
            return arr
    
    # get dimension length
    def dimlen(self, idx=0):
        """
        Get dimension length.
        
        :param idx: (*int*) Dimension index.        
        
        :returns: (*int*) Dimension length.
        """
        return self.dims[idx].getLength()
        
    def dimvalue(self, idx=0, convert=False):
        """
        Get dimension values.
        
        :param idx: (*int*) Dimension index.
        :param convert: (*boolean*) If convert to real values (i.e. datetime). Default
            is ``False``.
        
        :returns: (*array_like*) Dimension values
        """
        dim = self.dims[idx]
        if convert:
            if dim.getDimType() == DimensionType.T:
                return miutil.nums2dates(dim.getDimValue())
            else:
                return NDArray(self.dims[idx].getDimValue())
        else:
            return NDArray(self.dims[idx].getDimValue())
        
    def setdimvalue(self, idx, dimvalue):
        """
        Set dimension value.
        
        :param idx: (*int*) Dimension index.
        :param dimvalue: (*array_like*) Dimension value.
        """
        if isinstance(dimvalue, NDArray):
            self.dims[idx].setDimValue(dimvalue._array)
        else:
            self.dims[idx].setDimValues(dimvalue)
        
    def setdimtype(self, idx, dimtype):
        """
        Set dimension type.
        
        :param idx: (*int*) Dimension index.
        :param dimtype: (*string*) Dimension type. [X | Y | Z | T].
        """
        dtype = DimensionType.Other
        if dimtype.upper() == 'X':
            dtype = DimensionType.X
        elif dimtype.upper() == 'Y':
            dtype = DimensionType.Y
        elif dimtype.upper() == 'Z':
            dtype = DimensionType.Z
        elif dimtype.upper() == 'T':
            dtype = DimensionType.T
        self.dims[idx].setDimType(dtype)
        
    def adddim(self, dimvalue, dimtype=None, index=None):
        """
        Add a dimension.
        
        :param dimvalue: (*array_like*) Dimension value.
        :param dimtype: (*string*) Dimension type.
        :param index: (*int*) Index to be inserted.
        """
        if isinstance(dimvalue, Dimension):
            dim = dimvalue
        else:
            if isinstance(dimvalue, (NDArray, DimArray)):
                dimvalue = dimvalue.aslist()
            dtype = DimensionType.OTHER
            if not dimtype is None:
                if dimtype.upper() == 'X':
                    dtype = DimensionType.X
                elif dimtype.upper() == 'Y':
                    dtype = DimensionType.Y
                elif dimtype.upper() == 'Z':
                    dtype = DimensionType.Z
                elif dimtype.upper() == 'T':
                    dtype = DimensionType.T
            dim = Dimension(dtype)
            dim.setDimValues(dimvalue)
        if self.dims is None:
            self.dims = [dim]
        else:
            if index is None:
                self.dims.append(dim)
            else:
                self.dims.insert(index, dim)
        self.ndim = len(self.dims)
        
    def addtdim(self, t):
        """
        Add a time dimension as first dimension.
        
        :param t: (*array_like*) datetime array.
        """
        if self.tdim() is None:
            dim = Dimension(DimensionType.T)
            t = miutil.date2num(t)
            dim.setDimValues([t])
            if self.dims is None:
                self.dims = [dim]
            else:
                self.dims.insert(0, dim)
            self.ndim = len(self.dims)
            ss = list(self.shape)
            ss.insert(0, 1)
            ss = tuple(ss)
            self._array = self._array.reshape(ss)
            #self.shape = self._array.shape
        
    def xdim(self):
        """
        Get x dimension.
        """
        for dim in self.dims:
            if dim.getDimType() == DimensionType.X:
                return dim        
        return None
        
    def ydim(self):
        """
        Get y dimension.
        """
        for dim in self.dims:
            if dim.getDimType() == DimensionType.Y:
                return dim        
        return None
        
    def zdim(self):
        """
        Get z dimension.
        """
        for dim in self.dims:
            if dim.getDimType() == DimensionType.Z:
                return dim        
        return None
        
    def tdim(self):
        """
        Get time dimension.
        """
        if self.dims is None:
            return None

        for dim in self.dims:
            if dim.getDimType() == DimensionType.T:
                return dim        
        return None
        
    def islondim(self, idx=0):
        """
        Check a dimension is a longitude dimension or not.
        
        :param idx: (*int*) Dimension index.
        """
        dim = self.dims[idx]
        if dim.getDimType() == DimensionType.X and self.proj.isLonLat():
            return True
        else:
            return False
            
    def islatdim(self, idx=0):
        """
        Check a dimension is a latitude dimension or not.
        
        :param idx: (*int*) Dimension index.
        """
        dim = self.dims[idx]
        if dim.getDimType() == DimensionType.Y and self.proj.isLonLat():
            return True
        else:
            return False
            
    def islonlatdim(self, idx=0):
        """
        Check a dimension is a longitude or latitude dimension or not.
        
        :param idx: (*int*) Dimension index.
        """
        return self.islondim(idx) or self.islatdim(idx)
            
    def istimedim(self, idx=0):
        """
        Check a dimension is a time dimension or not.
        
        :param idx: (*int*) Dimension index.
        """
        dim = self.dims[idx]
        if dim.getDimType() == DimensionType.T:
            return True
        else:
            return False
                   
    def asgriddata(self, xdata=None, ydata=None, fill_value=-9999.0):
        if xdata is None or ydata is None:
            xdata = self.dimvalue(1)
            ydata = self.dimvalue(0)
        data = self.array
        if xdata[1] < xdata[0]:
            xdata = xdata[::-1]
            data = self.array[:,::-1]
        if ydata[1] < ydata[0]:
            ydata = ydata[::-1]
            data = self.array[::-1,:]

        gdata = GridData(data._array, xdata._array, ydata._array, fill_value, self.proj)
        return gdata
        
    def asgridarray(self, xdata=None, ydata=None, fill_value=-9999.0):
        if xdata is None or ydata is None:
            xdata = self.dimvalue(1)
            ydata = self.dimvalue(0)
        data = self.array
        if xdata[1] < xdata[0]:
            xdata = xdata[::-1]
            data = self.array[:,::-1]
        if ydata[1] < ydata[0]:
            ydata = ydata[::-1]
            data = self.array[::-1,:]

        gdata = GridArray(data._array, xdata._array, ydata._array, fill_value, self.proj)
        return gdata

    def squeeze(self):
        """
        Remove single-dimensional entries from the shape of an array.

        :returns: (*array_like*) The self array, but with all or a subset of the dimensions of length 1
            removed.
        """
        r = self._array.reduce()
        dims = []
        for dim in a.dims:
            if dim.getLength() > 1:
                dims.append(dim)
        return DimArray(r, dims, self.proj)
        
    def sum(self, axis=None):
        """
        Sum of array elements over a given axis.

        :param axis: (*int*) Axis along which the standard deviation is computed. 
            The default is to compute the standard deviation of the flattened array.
        
        returns: (*array_like*) Sum result
        """
        r = super(DimArray, self).sum(axis)
        if axis is None:
            return r
        else:
            dims = []
            for i in range(0, self.ndim):
                if i != axis:
                    dims.append(self.dims[i])
            return DimArray(r, dims, self.proj)

    def max(self, axis=None):
        """
        Compute tha arithmetic maximum along the specified axis.

        :param axis: (*int*) Axis along which the value is computed.
            The default is to compute the value of the flattened array.

        returns: (*array_like*) Maximum result
        """
        r = super(DimArray, self).max(axis)
        if isinstance(r, numbers.Number):
            return r
        else:
            dims = []
            for i in range(0, self.ndim):
                if isinstance(axis, (list, tuple)):
                    if not i in axis:
                        dims.append(self.dims[i])
                else:
                    if i != axis:
                        dims.append(self.dims[i])
            return DimArray(r, dims, self.proj)

    def min(self, axis=None):
        """
        Compute tha arithmetic minimum along the specified axis.

        :param axis: (*int*) Axis along which the value is computed.
            The default is to compute the value of the flattened array.

        returns: (*array_like*) Minimum result
        """
        r = super(DimArray, self).min(axis)
        if isinstance(r, numbers.Number):
            return r
        else:
            dims = []
            for i in range(0, self.ndim):
                if isinstance(axis, (list, tuple)):
                    if not i in axis:
                        dims.append(self.dims[i])
                else:
                    if i != axis:
                        dims.append(self.dims[i])
            return DimArray(r, dims, self.proj)
        
    def mean(self, axis=None, keepdims=False):
        """
        Compute tha arithmetic mean along the specified axis.

        :param axis: (*int*) Axis along which the value is computed.
            The default is to compute the value of the flattened array.
        :param keepdims: (*bool*) If this is set to True, the axes which are reduced are
            left in the result as dimensions with size one. Default if `False`.
        
        returns: (*array_like*) Mean result
        """
        r = super(DimArray, self).mean(axis, keepdims)
        if isinstance(r, numbers.Number):
            return r
        else:
            dims = []
            for i in range(0, self.ndim):
                if isinstance(axis, (list, tuple)):
                    if not i in axis:
                        dims.append(self.dims[i])
                else:
                    if i != axis:
                        dims.append(self.dims[i])
            return DimArray(r, dims, self.proj)
            
    def median(self, axis=None):
        """
        Compute tha median along the specified axis.

        :param axis: (*int*) Axis along which the value is computed.
            The default is to compute the value of the flattened array.
        
        returns: (*array_like*) Median result
        """
        r = super(DimArray, self).median(axis)
        if isinstance(r, numbers.Number):
            return r
        else:
            dims = []
            for i in range(0, self.ndim):
                if i != axis:
                    dims.append(self.dims[i])
            return DimArray(r, dims, self.proj)
            
    def std(self, axis=None, ddof=0):
        """
        Compute the standard deviation along the specified axis.

        :param axis: (*int*) Axis along which the standard deviation is computed. 
            The default is to compute the standard deviation of the flattened array.
        :param ddof: (*int*) Delta Degrees of Freedom: the divisor used in the calculation is
            N - ddof, where N represents the number of elements. By default ddof is zero.
        
        returns: (*array_like*) Standart deviation result.
        """
        r = super(DimArray, self).std(axis, ddof)
        if isinstance(r, numbers.Number):
            return r
        else:
            dims = []
            for i in range(0, self.ndim):
                if i != axis:
                    dims.append(self.dims[i])
            return DimArray(r, dims, self.proj)

    def var(self, axis=None, ddof=0):
        """
        Compute the variance along the specified axis.

        :param axis: (*int*) Axis along which the standard deviation is computed.
            The default is to compute the standard deviation of the flattened array.
        :param ddof: (*int*) Delta Degrees of Freedom: the divisor used in the calculation is
            N - ddof, where N represents the number of elements. By default ddof is zero.

        returns: (*array_like*) Variance result.
        """
        r = super(DimArray, self).std(axis, ddof)
        if isinstance(r, numbers.Number):
            return r
        else:
            dims = []
            for i in range(0, self.ndim):
                if i != axis:
                    dims.append(self.dims[i])
            return DimArray(r, dims, self.proj)
        
    def abs(self):
        """
        Calculate the absolute value element-wise.
        
        :returns: An array containing the absolute value of each element in x. 
            For complex input, a + ib, the absolute value is \sqrt{ a^2 + b^2 }.
        """
        r = super(DimArray, self).abs()
        return DimArray(r, self.dims, self.proj)

    def ceil(self):
        """
        Return the ceiling of the input, element-wise.

        :return: The ceiling of each element.
        """
        r = super(DimArray, self).ceil()
        return DimArray(r, self.dims, self.proj)

    def floor(self):
        """
        Return the floor of the input, element-wise.

        :return: The floor of each element.
        """
        r = super(DimArray, self).floor()
        return DimArray(r, self.dims, self.proj)
        
    def sqrt(self):
        """
        Calculate sqrt value.
        
        :returns: (*DimArray*) Sqrt value array.
        """
        r = super(DimArray, self).sqrt()
        return DimArray(r, self.dims, self.proj)
    
    def sin(self):
        """
        Calculate sin value.
        
        :returns: (*DimArray*) Sin value array.
        """
        r = super(DimArray, self).sin()
        return DimArray(r, self.dims, self.proj)

    def sinh(self):
        """
        Calculate hyperbolic sin value.

        :returns: (*DimArray*) Hyperbolic sin value array.
        """
        r = super(DimArray, self).sinh()
        return DimArray(r, self.dims, self.proj)
        
    def cos(self):
        r = super(DimArray, self).cos()
        return DimArray(r, self.dims, self.proj)

    def cosh(self):
        """
        Calculate hyperbolic cos value.

        :returns: (*DimArray*) Hyperbolic cos value array.
        """
        r = super(DimArray, self).cosh()
        return DimArray(r, self.dims, self.proj)
        
    def tan(self):
        r = super(DimArray, self).tan()
        return DimArray(r, self.dims, self.proj)

    def tanh(self):
        """
        Calculate hyperbolic tan value.

        :returns: (*DimArray*) Hyperbolic tan value array.
        """
        r = super(DimArray, self).tanh()
        return DimArray(r, self.dims, self.proj)
        
    def asin(self):
        r = super(DimArray, self).asin()
        return DimArray(r, self.dims, self.proj)
        
    def acos(self):
        """
        Calculate acos value.
        
        :returns: (*DimArray*) Acos value array.
        """
        r = super(DimArray, self).acos()
        return DimArray(r, self.dims, self.proj)
        
    def atan(self):
        r = super(DimArray, self).atan()
        return DimArray(r, self.dims, self.proj)
        
    def exp(self):
        r = super(DimArray, self).exp()
        return DimArray(r, self.dims, self.proj)
        
    def log(self):
        r = super(DimArray, self).log()
        return DimArray(r, self.dims, self.proj)
        
    def log10(self):
        r = super(DimArray, self).log10()
        return DimArray(r, self.dims, self.proj)

    def rot90(self, k=1):
        """
        Rotate an array by 90 degrees in the counter-clockwise direction. The first two dimensions
        are rotated if the array has more than 2 dimensions.

        :param k: (*int*) Number of times the array is rotated by 90 degrees

        :returns: (*array_like*) Rotated array.
        """
        r = ArrayMath.rot90(self._array, k)
        dims = []
        if math.fabs(k) == 1 or math.fabs(k) == 3:
            dims.append(self.dims[1])
            dims.append(self.dims[0])
            for i in range(2, len(self.dims)):
                dims.append(self.dims[i])
        else:
            for i in range(0, len(self.dims)):
                dims.append(self.dims[i])
        return DimArray(r, dims, self.proj)
        
    def maskout(self, mask):
        """
        Maskout data by polygons - the elements outside polygons will be set as NaN.

        :param mask: (*list*) Polygon list as mask borders.
        
        :returns: (*DimArray*) Maskouted data.
        """
        if isinstance(mask, NDArray):
            r = ArrayMath.maskout(self.asarray(), mask.asarray())
            return DimArray(NDArray(r), self.dims, self.proj)
        else:
            x = self.dims[1].getDimValue()
            y = self.dims[0].getDimValue()
            xy = ArrayUtil.meshgrid([x,y])
            x = xy[0]
            y = xy[1]
            if not isinstance(mask, (list, ArrayList)):
                mask = [mask]
            r = GeometryUtil.maskout(self.asarray(), x, y, mask)
            r = DimArray(NDArray(r), self.dims, self.proj)
            return r
            
    def maskin(self, mask):
        """
        Maskin data by polygons - the elements inside polygons will be set as NaN.

        :param mask: (*list*) Polygon list as mask borders.
        
        :returns: (*DimArray*) Maskined data.
        """
        if isinstance(mask, NDArray):
            r = ArrayMath.maskin(self.asarray(), mask.asarray())
            return DimArray(r, self.dims, self.proj)
        else:
            x = self.dimvalue(1)
            y = self.dimvalue(0)
            if not isinstance(mask, (list, ArrayList)):
                mask = [mask]
            r = GeometryUtil.maskin(self._array, x._array, y._array, mask)
            r = DimArray(r, self.dims, self.proj)
            return r
        
    def transpose(self, axes=None):
        """
        Permute the dimensions of an array.

        :param axes: (*list of int*) By default, reverse the dimensions, otherwise permute the axes according to the
            values given.

        :returns: Permuted array.
        """
        if axes is None:
            axes = [self.ndim-i-1 for i in range(self.ndim)]

        r = super(DimArray, self).transpose(axes)

        if self.ndim == 1:
            dims = self.dims
        else:
            dims = []
            for ax in axes:
                dims.append(self.dims[ax])

        return DimArray(r, dims, self.proj)
        
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
        dims = []
        for i in range(self.ndim):
            if i == axis1:
                dims.append(self.dims[axis2])
            elif i == axis2:
                dims.append(self.dims[axis1])
            else:
                dims.append(self.dims[i])
        return DimArray(r, dims, self.proj)
    
    def inv(self):
        """
        Calculate inverse matrix array.
        
        :returns: Inverse matrix array.
        """
        r = super(DimArray, self).inv()
        return DimArray(r, self.dims, self.proj)
        
    I = property(inv)                
        
    def lonflip(self):
        """
        Reorder global array from 0 - 360 longitude to -180 - 180 longitude or vice versa.
        
        :returns: Reordered array.
        """
        lon = self.dimvalue(self.ndim - 1)
        if lon.max() > 180:
            return self.lonpivot(180)
        else:
            return self.lonpivot(0)
        
    def lonpivot(self, pivot):
        """
        Pivots an array about a user-specified longitude. The rightmost dimension must be the
        longitude dimension, which must be global and without a cyclic point.
        
        :param pivot: (*float*) The longitude value around which to pivot.
        
        :returns: Result array after longitude pivot.
        """
        lon = self.dimvalue(self.ndim - 1)    
        minlon = lon.min()
        maxlon = lon.max()
        dlon = lon[1] - lon[0]
        if pivot < minlon:
            pivot += 360
        elif pivot > maxlon:
            pivot -= 360
            
        keys1 = []
        keys2 = []
        for i in range(self.ndim - 1):
            keys1.append(slice(None,None,None))
            keys2.append(slice(None,None,None))
        keys1.append('%f:%f' % (pivot, maxlon))  
        keys2.append('%f:%f' % (minlon, pivot - dlon))
        r1 = self.__getitem__(tuple(keys1))
        r2 = self.__getitem__(tuple(keys2))
            
        lon1 = r1.dimvalue(r1.ndim - 1)
        lon2 = r2.dimvalue(r2.ndim - 1)
        if maxlon > 180:
            lon1 = lon1 - 360
        else:
            lon2 = lon2 + 360
        r = r1.join(r2, self.ndim - 1)
        lon1 = lon1.aslist()
        lon1.extend(lon2.aslist())
        r.setdimvalue(self.ndim - 1, lon1)
        return r
        
    def month_to_season(self, season):
        """
        Computes a user-specified three-month seasonal mean
        (DJF, JFM, FMA, MAM, AMJ, MJJ, JJA, JAS, ASO, SON, OND, NDJ).
        The first average (DJF=JF) and the last average (NDJ=ND) are actually 
        two-month averages.
        
        The time (leftmost) dimension must be divisible by 12. The data are assumed 
        to be monthly mean data and the first record is assumed to be January.
        
        :param season: (*string*) A string representing the season to 
            calculate: e.g., "JFM", "JJA".
            
        :returns: Season averaged data array.
        """
        nmonth = self.dimlen(0)
        nyear = nmonth / 12
        seasons = ['DJF','JFM','FMA','MAM','AMJ','MJJ','JJA','JAS','ASO','SON','OND','NDJ']
        season = season.upper()
        if not season in seasons:
            print('Season string is not valid: "' + season + '"!')
            raise KeyError()
        idx = seasons.index(season) - 1
        keys = []
        keys.append(slice(0,nyear,1))
        for i in range(1, self.ndim):
            keys.append(slice(None,None,None))
        r = self.__getitem__(tuple(keys))
        si = idx
        for i in range(nyear):
            ei = si + 3
            if si < 0:
                si = 0
            if ei > nmonth:
                ei = nmonth
            keys[0] = slice(si,ei,1)
            sdata = self.__getitem__(tuple(keys))
            sdata = ArrayMath.mean(sdata.asarray(), 0)
            keys[0] = i
            r.__setitem__(tuple(keys), sdata)
            si += 12
        
        return r
        
    def interpn(self, xi):
        """
        Multidimensional interpolation on regular grids.

        :param xi: (*list*) The coordinates to sample the gridded data at.
        
        :returns: (*float*) Interpolated value at input coordinates.
        """
        points = []
        for i in range(self.ndim):
            points.append(ArrayUtil.array(self.dims[i].getDimValue()))
        if isinstance(xi, (list, tuple)):
            if isinstance(xi[0], NDArray):
                nxi = []
                for x in xi:
                    nxi.append(x._array)
            else:
                nxi = []
                for x in xi:
                    if isinstance(x, datetime.datetime):
                        x = miutil.date2num(x)
                    nxi.append(x)
                nxi = NDArray(nxi)._array
        else:
            nxi = nxi._array
        r = ArrayUtil.interpn(points, self._array, nxi)
        if isinstance(r, Array):
            return NDArray(r)
        else:
            return r
            
    def project(self, x=None, y=None, toproj=None, method='bilinear'):
        """
        Project array
        
        :param x: To x coordinates.
        :param y: To y coordinates.
        :param toproj: To projection.
        :param method: Interpolation method: ``bilinear`` or ``nearest``. Default is ``bilinear``.
        
        :returns: (*NDArray*) Projected array
        """
        yy = self.dims[self.ndim - 2].getDimValue()
        xx = self.dims[self.ndim - 1].getDimValue()
        if toproj is None:
            toproj = self.proj
        
        if x is None or y is None:
            pr = Reproject.reproject(self._array, xx, yy, self.proj, toproj)
            r = pr[0]
            x = pr[1]
            y = pr[2]
            dims = self.dims
            ydim = Dimension(DimensionType.Y)
            ydim.setDimValues(NDArray(y).aslist())
            dims[-2] = ydim
            xdim = Dimension(DimensionType.X)
            xdim.setDimValues(NDArray(x).aslist())    
            dims[-1] = xdim
            rr = DimArray(NDArray(r), dims, toproj)
            return rr
        
        if method == 'bilinear':
            method = ResampleMethods.Bilinear
        else:
            method = ResampleMethods.NearestNeighbor
        if isinstance(x, (list, tuple)):
            x = NDArray(x)
        if isinstance(y, (list, tuple)):
            y = NDArray(y)
        if x.ndim == 1:
            x, y = ArrayUtil.meshgrid(x.asarray(), y.asarray())
        else:
            x = x._array
            y = y._array
        r = Reproject.reproject(self._array, xx, yy, x, y, self.proj, toproj, method)
        return NDArray(r)
            
    def join(self, b, dimidx):
        r = ArrayMath.join(self._array, b._array, dimidx)
        dima = self.dimvalue(dimidx)
        dimb = b.dimvalue(dimidx)
        dimr = []
        if dima[0] < dimb[0]:
            for i in range(0, len(dima)):
                dimr.append(dima[i])
            for i in range(0, len(dimb)):
                dimr.append(dimb[i])
        else:
            for i in range(0, len(dimb)):
                dimr.append(dimb[i])
            for i in range(0, len(dima)):
                dimr.append(dima[i])
        rdims = []
        for i in range(0, len(self.dims)):
            if i == dimidx:
                ndim = Dimension()
                ndim.setDimValues(dimr)
                rdims.append(ndim)
            else:
                rdims.append(self.dims[i])
        return DimArray(NDArray(r), rdims, self.proj)
        
    def savegrid(self, fname, format='surfer', **kwargs):
        """
        Save the array data to an ASCII or binary file. The array must be 2 dimension.
        
        :param fname: (*string*) File name.
        :param format: (*string*) File format [surfer | bil | esri_ascii | micaps4].
        :param description: (*string*) Data description - only used for ``micaps4`` file.
        :param date: (*datetime*) Data datetime - only used for ``micaps4`` file.
        :param hours: (*int*) Data forcasting hours - only used for ``micaps4`` file.
        :param level: (*float*) Data vertical level - only used for ``micaps4`` file.
        :param smooth: (*int*) 1 or 0 - only used for ``micaps4`` file.
        :param boldvalue: (*int*) Bold contour value - only used for ``micaps4`` file.
        :param proj: (*ProjectionInfo*) Data ProjectionInfo - only used for ``micaps4`` file.
        :param float_format: (*string*) Float number format, such as '%.2f'.
        """
        if self.ndim != 2:
            print 'The array must be 2 dimensional!'
            return
            
        gdata = self.asgridarray()
        float_format = kwargs.pop('float_format', None)
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
            proj = kwargs.pop('proj', self.proj)
            if proj is None:
                gdata.saveAsMICAPS4File(fname, desc, date, hours, level, smooth, boldvalue, float_format)
            else:
                if proj.isLonLat():
                    gdata.saveAsMICAPS4File(fname, desc, date, hours, level, smooth, boldvalue, float_format)
                else:
                    gdata.saveAsMICAPS4File(fname, desc, date, hours, level, smooth, boldvalue, float_format, proj)

def dim_array(a, dims=None):
    """
    Create a dimension array (DimArray).

    :param a: (*array_like*) Array (NDArray) or data list.
    :param dims: (*list*) List of dimensions.

    :returns: (*DimArray*) Dimension array.
    """
    if not isinstance(a, NDArray):
        a = array(a)
    if dims is None:
        dims = []
        for i in range(a.ndim):
            dim = Dimension()
            dim.setDimValues(range(a.shape[i]))
            dims.append(dim)
    return DimArray(a, dims)
