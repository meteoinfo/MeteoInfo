#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2014-12-27
# Purpose: MeteoInfo Dataset module
# Note: Jython
#-----------------------------------------------------
from org.meteoinfo.ndarray import Range, Array, MAMath
from org.meteoinfo.data.dimarray import Dimension, DimensionType
from org.meteoinfo.ndarray.math import ArrayMath, ArrayUtil
from org.meteoinfo.common import PointD
from org.meteoinfo.projection import KnownCoordinateSystems, Reproject
from org.meteoinfo.data.meteodata import Attribute
from ucar.nc2 import Attribute as NCAttribute
from ucar.ma2 import DataType as NCDataType
from ucar.ma2 import ArrayStructure, StructureMembers
from org.meteoinfo.data.meteodata.netcdf import NCUtil
from org.meteoinfo.ndarray import DataType

import mipylib.numeric as np
import mipylib.miutil as miutil
import datetime
import numbers
import warnings

# Dimension variable
class DimVariable(object):

    @staticmethod
    def factory(variable=None, dataset=None, ncvariable=None):
        """
        Factory method.
        """
        if variable.getDataType().isStructure():
            return StructureVariable(variable, dataset)
        else:
            return DimVariable(variable, dataset, ncvariable)
    
    # variable must be org.meteoinfo.data.meteodata.Variable
    # dataset is DimDataFile
    def __init__(self, variable=None, dataset=None, ncvariable=None):
        self._variable = variable
        self.dataset = dataset
        self.ncvariable = ncvariable
        if not variable is None:
            self._name = variable.getName()
            self.dtype = np.dtype.fromjava(variable.getDataType())
            self.dims = variable.getDimensions()
            self.ndim = variable.getDimNumber()
            self.attributes = variable.getAttributes()
        elif not ncvariable is None:
            self._name = ncvariable.getFullName()
            self.dtype = ncvariable.getDataType()
            self.dims = ncvariable.getDimensions()
            self.ndim = len(self.dims)
            self.attributes = list(ncvariable.getAttributes())
        else:
            self._name = None
            self.dtype = None
            self.dims = None
            self.ndim = 0
            self.attributes = None
        self.proj = None if dataset is None else dataset.projection
        self.projection = self.proj

    @property
    def name(self):
        return self._name

    @name.setter
    def name(self, val):
        self._name = val
        if self._variable is not None:
            self._variable.setName(val)
        elif self.ncvariable is not None:
            self.ncvariable.setFullName(val)

    @property
    def short_name(self):
        if self._variable is not None:
            return self._variable.getShortName()

        if self.ncvariable is not None:
            return self.ncvariable.getShortName()

        return None
            
    def __len__(self):
        len = 1
        if not self._variable is None:
            for dim in self._variable.getDimensions():
                len = len * dim.getLength()            
        return len
        
    def __str__(self):
        r = str(self.dtype) + ' ' + 'None' if self.name is None else self.name + '('
        if self.dims is None:
            r = r + '):'
        else:
            for dim in self.dims:
                dimname = dim.getShortName()
                if dimname is None:
                    dimname = 'null'
                r = r + dimname + ','
            r = r[:-1] + '):'
        if not self.attributes is None:
            for attr in self.attributes:
                r = r + '\n\t' + self.name + ': ' + attr.toString()
        return r
        
    def __repr__(self):
        return self.__str__()
        
    def __getitem__(self, indices):
        if self._variable.getDataType() in [DataType.STRUCTURE, DataType.SEQUENCE]:
            if isinstance(indices, str):    #metadata
                return self.member_array(indices)
            else:
                a = self.dataset.read(self.name)
                return StructureArray(a.getArrayObject())

        if indices is None:
            inds = []
            for _ in range(self.ndim):
                inds.append(slice(None))
            indices = tuple(inds)
        
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

        if self.ndim > 0:
            if len(indices) < self.ndim:
                indices = list(indices)
                for _ in range(self.ndim - len(indices)):
                    indices.append(slice(None))
                indices = tuple(indices)

            if len(indices) != self.ndim:
                print('indices must be ' + str(self.ndim) + ' dimensions!')
                return None
            
        if not self.proj is None and not self.proj.isLonLat():
            xlim = None
            ylim = None
            xidx = -1
            yidx = -1
            for i in range(0, self.ndim):
                dim = self.dims[i]
                if dim.getDimType() == DimensionType.X:                    
                    k = indices[i]
                    if isinstance(k, basestring):
                        xlims = k.split(':')
                        if len(xlims) == 1:
                            xlim = [float(xlims[0])]
                        else:
                            xlim = [float(xlims[0]), float(xlims[1])]
                        xidx = i
                elif dim.getDimType() == DimensionType.Y:
                    k = indices[i]
                    if isinstance(k, basestring):
                        ylims = k.split(':')
                        if len(ylims) == 1:
                            ylim = [float(ylims[0])]
                        else:
                            ylim = [float(ylims[0]), float(ylims[1])]
                        yidx = i
            if not xlim is None and not ylim is None:                
                fromproj=KnownCoordinateSystems.geographic.world.WGS1984
                inpt = PointD(xlim[0], ylim[0])
                outpt1 = Reproject.reprojectPoint(inpt, fromproj, self.proj)
                if len(xlim) == 1:
                    xlim = [outpt1.X]
                    ylim = [outpt1.Y]
                else:
                    inpt = PointD(xlim[1], ylim[1])
                    outpt2 = Reproject.reprojectPoint(inpt, fromproj, self.proj)
                    xlim = [outpt1.X, outpt2.X]
                    ylim = [outpt1.Y, outpt2.Y]
                indices1 = []
                for i in range(0, self.ndim):
                    if i == xidx:
                        if len(xlim) == 1:
                            indices1.append(str(xlim[0]))
                        else:
                            indices1.append(str(xlim[0]) + ':' + str(xlim[1]))
                    elif i == yidx:
                        if len(ylim) == 1:
                            indices1.append(str(ylim[0]))
                        else:
                            indices1.append(str(ylim[0]) + ':' + str(ylim[1]))
                    else:
                        indices1.append(indices[i])
                indices = indices1
        
        origin = []
        size = []
        stride = []
        ranges = []
        dims = []
        flips = []
        onlyrange = True
        for i in range(0, self.ndim):  
            isrange = True
            dimlen = self.dimlen(i)
            k = indices[i]
            sidx = 0
            eidx = 0
            step = 1
            if isinstance(k, int):
                if k < 0:
                    k = self.dims[i].getLength() + k
                sidx = k
                eidx = k
                step = 1
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
                    sidx = self.dimlen(i) + sidx
                    
                if isinstance(k.stop, basestring):
                    ev = float(k.stop)
                    eidx = self.dims[i].getValueIndex(ev)
                elif isinstance(k.stop, datetime.datetime):
                    ev = miutil.date2num(k.stop)
                    eidx = self.dims[i].getValueIndex(ev)
                else:
                    eidx = self.dimlen(i) if k.stop is None else k.stop
                    if eidx < 0:
                        eidx = self.dimlen(i) + eidx
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
            elif isinstance(k, list):
                onlyrange = False
                isrange = False
                if not isinstance(k[0], datetime.datetime):
                    ranges.append(k)
                else:
                    tlist = []
                    for tt in k:
                        sv = miutil.date2num(tt)
                        idx = self.dims[i].getValueIndex(sv)
                        tlist.append(idx)
                    ranges.append(tlist)
                    k = tlist
            elif isinstance(k, basestring):
                dim = self._variable.getDimension(i)
                kvalues = k.split(':')
                sv = float(kvalues[0])
                sidx = dim.getValueIndex(sv)
                if len(kvalues) == 1:
                    eidx = sidx
                    step = 1
                else:                    
                    ev = float(kvalues[1])
                    eidx = dim.getValueIndex(ev)
                    if len(kvalues) == 2:
                        step = 1
                    else:
                        step = int(float(kvalues[2]) / dim.getDeltaValue())
                if sidx > eidx and step > 0:
                    step = -step
            else:
                print(k)
                return None

            if isrange:
                if sidx >= dimlen or eidx >= dimlen:
                    print('Index out of range!')
                    return None
                origin.append(sidx)
                n = abs(eidx - sidx) + 1
                size.append(n)                   
                if n > 1:
                    dim = self._variable.getDimension(i)
                    #if dim.isReverse():
                    #    step = -step
                    dim = dim.extract(sidx, eidx, step)
                    #dim.setReverse(False)
                    dims.append(dim)
                stride.append(step) 
                if step < 0:
                    #step = abs(step)
                    flips.append(i)
                if sidx > eidx:
                    iidx = eidx
                    eidx = sidx
                    sidx = iidx
                rr = Range(sidx, eidx, abs(step))
                ranges.append(rr)
            else:
                if len(k) > 1:
                    dim = self._variable.getDimension(i)
                    dim = dim.extract(k)
                    #dim.setReverse(False)
                    dims.append(dim)

        if onlyrange:
            rr = self.dataset.dataset.read(self.name, ranges)
        else:
            rr = self.dataset.dataset.take(self.name, ranges)
        if rr.getSize() == 1:
            iter = rr.getIndexIterator()
            return iter.getObjectNext()
        else:
            for i in flips:
                rr = rr.flip(i)
            rr = rr.reduce()
            #ArrayMath.missingToNaN(rr, self.fill_value)
            if len(flips) > 0:
                rrr = Array.factory(rr.getDataType(), rr.getShape())
                MAMath.copy(rrr, rr)
                array = np.array(rrr)
            else:
                array = np.array(rr)
            data = np.DimArray(array, dims, self.dataset.proj)
            return data
    
    def read(self):
        """
        Read data array.
        :return: (*array*) Data array.
        """
        return np.array(self.dataset.read(self.name))

    def get_pack_paras(self):
        """
        Get pack parameters.
        :return: missing_value, scale_factor, add_offset
        """
        pack_paras = NCUtil.getPackData(self._variable)

        return pack_paras[2], pack_paras[1], pack_paras[0]

    def is_member(self):
        """
        Whether the variable is a member of a structure.

        :return: Is a member of a structure or not.
        """
        return self._variable.isMemberOfStructure()

    def get_members(self):
        """
        Get structure members. Only valid for Structure data type.

        :return: Structure members.
        """
        a = self.read()
        if a._array.getDataType() != DataType.STRUCTURE:
            print('This method is only valid for structure array!')
            return None
        a = a._array.getArrayObject()
        return a.getMembers()

    def get_member(self, member):
        """
        Get structure members. Only valid for Structure data type.

        :param member: (*str*) Member name.
        :return: Structure members.
        """
        a = self.read()
        if a._array.getDataType() != DataType.STRUCTURE:
            print('This method is only valid for structure array!')
            return None
        a = a._array.getArrayObject()
        return a.findMember(member)

    def member_array(self, member, index=None, rec=0):
        """
        Extract member array. Only valid for Structure data type.

        :param member: (*string*) Member name.
        :param index: (*slice*) Index.
        :param rec: (*int*) Record index.

        :returns: (*array*) Extracted member array.
        """
        a = self.read()
        a = a._array.getArrayObject()
        is_structure = isinstance(a, ArrayStructure)
        if isinstance(member, basestring):
            if is_structure:
                member = a.findMember(member)
            else:
                member = a.getObject(rec).findMember(member)

        if member is None:
            raise KeyError('The member %s not exists!' % member)

        if is_structure:
            a = a.extractMemberArray(member)
        else:
            a = a.getObject(rec).extractMemberArray(member)
        if a.getDataType() in [NCDataType.SEQUENCE, NCDataType.STRUCTURE]:
            return StructureArray(a)

        a = NCUtil.convertArray(a)
        r = np.array(a)
        if r.size == 1:
            return r[0]

        if not index is None:
            r = r.__getitem__(index)

        return r

    # def member_array(self, member, indices=None):
    #     """
    #     Extract member array. Only valid for Structure data type.
    #
    #     :param member: (*string*) Member name.
    #     :param indices: (*slice*) Indices.
    #
    #     :returns: (*array*) Extracted member array.
    #     """
    #     a = self.read()
    #     if a._array.getDataType() != DataType.STRUCTURE:
    #         print('This method is only valid for structure array!')
    #         return None
    #
    #     a = a._array.getArrayObject()
    #     if isinstance(member, basestring):
    #         member = a.findMember(member)
    #     if member is None:
    #         raise KeyError('The member %s not exists!' % member)
    #
    #     self.dataset.reopen()
    #     a = a.extractMemberArray(member)
    #     if a.getDataType() in [NCDataType.SEQUENCE, NCDataType.STRUCTURE]:
    #         return StructureArray(a)
    #
    #     a = NCUtil.convertArray(a)
    #     r = np.array(a)
    #     if r.size == 1:
    #         return r[0]
    #
    #     if not indices is None:
    #         r = r.__getitem__(indices)
    #
    #     return r

    def dimlen(self, idx):
        """
        Get dimension length.

        :param idx: (*int*) Dimension index.

        :return: Dimension length.
        """
        return self.dims[idx].getLength()
        
    def dimvalue(self, idx, convert=False):
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
                return np.array(dim.getDimValue())
        else:
            return np.array(dim.getDimValue())
        
    def attrvalue(self, attr):
        """
        Get a global attribute value.
        
        :param attr: (*string or Attribute*) Attribute or Attribute name
        """
        if isinstance(attr, str):
            attr = self._variable.findAttribute(attr)
        if attr is None:
            return None
        v = np.array(attr.getValues())
        return v
        
    def xdim(self):
        for dim in self.dims:
            if dim.getDimType() == DimensionType.X:
                return dim        
        return None
        
    def ydim(self):
        for dim in self.dims:
            if dim.getDimType() == DimensionType.Y:
                return dim        
        return None
        
    def zdim(self):
        for dim in self.dims:
            if dim.getDimType() == DimensionType.Z:
                return dim        
        return None
        
    def tdim(self):
        for dim in self.dims:
            if dim.getDimType() == DimensionType.T:
                return dim        
        return None
        
    def adddim(self, dimtype, dimvalue):
        if isinstance(dimvalue, np.NDArray):
            dimvalue = dimvalue.aslist()
        self._variable.addDimension(dimtype, dimvalue)
        self.ndim = self._variable.getDimNumber()
        
    def setdim(self, dimtype, dimvalue, index=None, reverse=False):
        if isinstance(dimvalue, np.NDArray):
            dimvalue = dimvalue.aslist()
        if index is None:
            self._variable.setDimension(dimtype, dimvalue, reverse)
        else:
            self._variable.setDimension(dimtype, dimvalue, reverse, index)
        self.ndim = self._variable.getDimNumber()
        
    def setdimrev(self, idx, reverse):
        self.dims[idx].setReverse(reverse)
        
    def addattr(self, attrname, attrvalue):
        if self.attributes is None:
            self.attributes = []

        if isinstance(attrvalue, Array):
            attrvalue = np.NDArray(attrvalue)

        if isinstance(attrvalue, np.NDArray):
            if attrvalue.size == 1:
                attrvalue = attrvalue[0]
                attr = Attribute(attrname, attrvalue)
            else:
                attr = Attribute(attrname, attrvalue._array)
        else:
            attr = Attribute(attrname, attrvalue)

        self.attributes.append(attr)

        if not self.ncvariable is None:
            if isinstance(attrvalue, np.NDArray):
                attrvalue = NCUtil.convertArray(attrvalue._array)
            ncattr = NCAttribute(attrname, attrvalue)
            self.ncvariable.addAttribute(ncattr)
        return attr


class StructureVariable(DimVariable):

    def __init__(self, variable=None, dataset=None, parent_variable=None):
        """
        Structure variable.

        :param variable: (*Structure*) NC Structure object.
        :param dataset: (*DimDataFile*) Data file.
        :param parent_variable: (*StructureVariable*) Parent structure variable.
        """
        DimVariable.__init__(self, variable, dataset)

        self._parent_variable = parent_variable

        if dataset is not None:
            datainfo = dataset.dataset.getDataInfo()
            if not datainfo.isOpened():
                datainfo.reOpen()

            self._ncfile = datainfo.getFile()
            self._ncvar = self._ncfile.findVariable(self.name)
            self._variables = []
            for var in self._ncvar.getVariables():
                self._variables.append(MemberVariable.factory(NCUtil.convertVariable(var), dataset, self))

    def __getitem__(self, key):
        if isinstance(key, basestring):
            for var in self._variables:
                if var.name == key:
                    return var

            for var in self._variables:
                if var.short_name == key:
                    return var

            raise ValueError(key + ' is not a variable name')
        else:
            return np.array(self.dataset.read(self.name))

    @property
    def variables(self):
        """
        Get all variables.
        """
        return self._variables

    @property
    def varnames(self):
        """
        Get all variable names.
        """
        names = []
        for var in self._variables:
            names.append(var.short_name)

        return names


class MemberVariable(DimVariable):

    @staticmethod
    def factory(variable=None, dataset=None, parent_variable=None):
        """
        Factor method.
        """
        if variable.getDataType().isStructure():
            return StructureVariable(variable, dataset, parent_variable)
        else:
            return MemberVariable(variable, dataset, parent_variable)

    def __init__(self, variable=None, dataset=None, parent_variable=None):
        """
        Structure variable.

        :param variable: (*Structure*) NC Structure object.
        :param dataset: (*DimDataFile*) Data file.
        :param array: (*NCArray*) NC Array.
        """
        DimVariable.__init__(self, variable, dataset)

        self._parent_variable = parent_variable

        if dataset is not None:
            datainfo = dataset.dataset.getDataInfo()
            if not datainfo.isOpened():
                datainfo.reOpen()

            self._ncfile = datainfo.getFile()
            self._ncvar = self._ncfile.findVariable(self.name)

    def __getitem__(self, key):
        """
        Read array data from the variable.

        :param key: (*int, complex or slice*) For `int`, key means record index. For `complex`, the image
            of the complex is the sequence index. For `slice`, `:` to read all data, otherwise the start
            of the slice is the sequence index.

        :return: (*NDArray*) Data array.
        """
        if self._parent_variable is not None:
            if self._parent_variable._variable.getDataType() == DataType.STRUCTURE:
                return DimVariable.__getitem__(self, key)

        if isinstance(key, int):
            return self.read_array(record=key)
        elif isinstance(key, complex):
            return self.read_array(seq=int(key.imag))
        elif isinstance(key, slice):
            if key == slice(None):
                return self.read()
            else:
                return self.read_array(seq=key.start)

    def read_array(self, record=0, seq=None):
        """
        Read data array.

        :param record: (*int*) Record index. Default is 0.
        :param seq: (*int*) Sequence index. Default is `None`, means all sequences.
        :return: (*array*) Data array.
        """
        a = self._parent_variable.read()
        a = a._array.getArrayObject()
        missing_value, scale_factor, add_offset = self.get_pack_paras()
        is_structure = isinstance(a, ArrayStructure)
        if is_structure:
            r = NCUtil.readSequence(a, self.short_name)
        else:
            if seq is None:
                r = NCUtil.readSequenceRecord(a, self.short_name, record, missing_value)
            else:
                r = NCUtil.readSequence(a, self.short_name, seq)

        if r is None:
            return None

        r = ArrayUtil.unPack(r, missing_value, scale_factor, add_offset)
        return np.array(r)

    @property
    def variables(self):
        """
        Get all variables.
        """
        return self._variables

    @property
    def varnames(self):
        """
        Get all variable names.
        """
        names = []
        for var in self._variables:
            names.append(var.short_name)

        return names


class StructureArray(object):

    def __init__(self, array):
        """
        Structure array.

        :param array: (*ArrayStructure*) NC ArrayStructure object.
        """
        self._array = array

    @property
    def shape(self):
        """
        Get array shape.

        :return: (*tuple of int*) Array shape.
        """
        return tuple(self._array.getShape())

    @property
    def dtype(self):
        """
        Get array data type.

        :return: (*DataType*) Array data type.
        """
        return NCUtil.convertDataType(self._array.getDataType())

    def get_members(self, rec=0):
        """
        Get structure members.

        :param rec: (*int*) Record index.
        :return: (*Member*) Structure members.
        """
        if isinstance(self._array, ArrayStructure):
            return self._array.getMembers()
        else:
            return self._array.getObject(rec).getMembers()

    def get_member(self, member, rec=0):
        """
        Get structure members.

        :param member: (*str*) Member name.
        :param rec: (*int*) Record index.
        :return: (*Member*) Structure members.
        """
        if isinstance(self._array, ArrayStructure):
            return self._array.findMember(member)
        else:
            return self._array.getObject(rec).findMember(member)

    def member_array(self, member, index=None, rec=0):
        """
        Extract member array. Only valid for Structure data type.

        :param member: (*string*) Member name.
        :param index: (*slice*) Index.
        :param rec: (*int*) Record index.

        :returns: (*array*) Extracted member array.
        """
        is_structure = isinstance(self._array, ArrayStructure)
        if isinstance(member, basestring):
            if is_structure:
                member = self._array.findMember(member)
            else:
                member = self._array.getObject(rec).findMember(member)

        if member is None:
            raise KeyError('The member %s not exists!' % member)

        if is_structure:
            a = self._array.extractMemberArray(member)
        else:
            a = self._array.getObject(rec).extractMemberArray(member)
        if a.getDataType() in [NCDataType.SEQUENCE, NCDataType.STRUCTURE]:
            return StructureArray(a)

        a = NCUtil.convertArray(a)
        r = np.array(a)
        if r.size == 1:
            return r[0]

        if not index is None:
            r = r.__getitem__(index)

        return r

# Variable in multiple data files (DimDataFiles) - only time dimension is different.
class TDimVariable(object):
    
    # variable must be org.meteoinfo.data.meteodata.Variable
    # dataset is DimDataFiles
    def __init__(self, variable, dataset):
        self._variable = variable
        self.dataset = dataset
        self.name = variable.getName()
        self.dtype = np.dtype.fromjava(variable.getDataType())
        self.ndim = variable.getDimNumber()
        dims = variable.getDimensions()
        tdim = Dimension(DimensionType.T)
        times = []
        for t in self.dataset.times:
            times.append(miutil.date2num(t))
        tdim.setDimValues(times)
        dims[0] = tdim
        self.dims = dims
        self.tnum = len(times)

    def __str__(self):
        if self._variable is None:
            return 'None'

        r = str(self.dtype) + ' ' + self.name + '('
        for dim in self.dims:
            dimname = dim.getShortName()
            if dimname is None:
                dimname = 'null'
            r = r + dimname + ','
        r = r[:-1] + '):'
        attrs = self._variable.getAttributes()
        for attr in attrs:
            r = r + '\n\t' + self.name + ': ' + attr.toString()
        return r

    def __repr__(self):
        return self.__str__()
        
    def __getitem__(self, indices):
        if not isinstance(indices, tuple):
            indices = [indices]

        if len(indices) < self.ndim:
            indices = list(indices)
            for _ in range(self.ndim - len(indices)):
                indices.append(slice(None))
            indices = tuple(indices)

        if len(indices) != self.ndim:
            print('indices must be ' + str(self.ndim) + ' dimensions!')
            return None
        
        k = indices[0]
        t_list = None
        if isinstance(k, int):
            if k < 0:
                k = self.tnum + k
            sidx = k
            eidx = k
            step = 1
        elif isinstance(k, datetime.datetime):
            sidx = self.dataset.timeindex(k)
            eidx = sidx
            step = 1
        elif isinstance(k, slice):
            sidx = 0 if k.start is None else k.start
            if isinstance(sidx, datetime.datetime):
                sidx = self.dataset.timeindex(sidx)
            if sidx < 0:
                sidx = self.tnum + sidx
            eidx = self.tnum if k.stop is None else k.stop
            if isinstance(eidx, datetime.datetime):
                eidx = self.dataset.timeindex(eidx) + 1
            if eidx < 0:
                eidx = self.tnum + eidx
            eidx -= 1
            step = 1 if k.step is None else k.step
        elif isinstance(k, list):
            t_list = []
            for t in k:
                if isinstance(t, datetime.datetime):
                    t_list.append(self.dataset.timeindex(t))
                else:
                    t_list.append(t)

        times = []
        data = None
        if t_list is None:
            sfidx = self.dataset.datafileindex(sidx)
            si = sidx
            aa = None
            for i in range(sidx, eidx + 1, step):
                times.append(miutil.date2num(self.dataset.gettime(i)))
                fidx = self.dataset.datafileindex(i)
                if fidx > sfidx:
                    ei = i - step
                    ddf = self.dataset[sfidx]
                    var = ddf[self.name]
                    ii, ssi = self.dataset.dftindex(si)
                    ii, eei = self.dataset.dftindex(ei)
                    eei += 1
                    nindices = list(indices)
                    nindices[0] = slice(ssi, eei, step)
                    nindices = tuple(nindices)
                    aa = var.__getitem__(nindices)
                    if si == ei:
                        if isinstance(aa, np.DimArray):
                            aa.addtdim(self.dataset.gettime(si))
                        else:
                            aa = np.array([aa])
                            aa = np.DimArray(aa)
                            aa.addtdim(self.dataset.gettime(si))
                    if data is None:
                        data = aa
                    else:
                        data = np.concatenate([data, aa])
                    si = i
                    sfidx = fidx

            if si < eidx + 1:
                ei = eidx + 1 - step
                ddf = self.dataset[sfidx]
                var = ddf[self.name]
                ii, ssi = self.dataset.dftindex(si)
                ii, eei = self.dataset.dftindex(ei)
                eei += 1
                nindices = list(indices)
                nindices[0] = slice(ssi, eei, step)
                nindices = tuple(nindices)
                aa = var.__getitem__(nindices)
                if si == ei and eidx != sidx:
                    if isinstance(aa, np.DimArray):
                        aa.addtdim(self.dataset.gettime(si))
                    else:
                        aa = np.array([aa])
                        aa = np.DimArray(aa)
                        aa.addtdim(self.dataset.gettime(si))
                if data is None:
                    data = aa
                else:
                    data = np.concatenate([data, aa])

            if aa is None:
                sfidx = self.dataset.datafileindex(sidx)
                ddf = self.dataset[sfidx]
                var = ddf[self.name]
                ii, ssi = self.dataset.dftindex(sidx)
                nindices = list(indices)
                nindices[0] = slice(ssi, ssi, step)
                nindices = tuple(nindices)
                aa = var.__getitem__(nindices)
                return aa
        else:
            for i in t_list:
                times.append(miutil.date2num(self.dataset.gettime(i)))
                fidx, ssi = self.dataset.dftindex(i)
                ddf = self.dataset[fidx]
                var = ddf[self.name]
                nindices = list(indices)
                nindices[0] = ssi
                nindices = tuple(nindices)
                aa = var.__getitem__(nindices)
                aa.addtdim(self.dataset.gettime(i))
                if data is None:
                    data = aa
                else:
                    data = np.concatenate([data, aa])
                
        if isinstance(data, np.DimArray):
            return data
        else:
            if isinstance(aa, numbers.Number):
                return aa

            dims = aa.dims
            dims[0].setDimValues(times)
            r = np.DimArray(data, dims, aa.proj)
            return r