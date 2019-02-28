#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2014-12-27
# Purpose: MeteoInfo Dataset module
# Note: Jython
#-----------------------------------------------------
from org.meteoinfo.data.meteodata import Dimension, DimensionType
from org.meteoinfo.data import ArrayMath, ArrayUtil
from org.meteoinfo.global import PointD
from org.meteoinfo.projection import KnownCoordinateSystems, Reproject
from ucar.nc2 import Attribute
from ucar.ma2 import Range, Array, MAMath
from mipylib.numeric.dimarray import DimArray
from mipylib.numeric.miarray import MIArray
import mipylib.numeric.minum as minum
import mipylib.miutil as miutil
import datetime

# Dimension variable
class DimVariable(object):
    
    # variable must be org.meteoinfo.data.meteodata.Variable
    # dataset is DimDataFile
    def __init__(self, variable=None, dataset=None, ncvariable=None):
        self.variable = variable
        self.dataset = dataset
        self.ncvariable = ncvariable
        if not variable is None:
            self.name = variable.getName()
            self.datatype = variable.getDataType()
            self.dims = variable.getDimensions()
            self.ndim = variable.getDimNumber()
            self.fill_value = variable.getFillValue()
            self.scale_factor = variable.getScaleFactor()
            self.add_offset = variable.getAddOffset()
        elif not ncvariable is None:
            self.name = ncvariable.getShortName()
            self.dims = ncvariable.getDimensions()
            self.ndim = len(self.dims)
        if not dataset is None:
            self.proj = dataset.proj
            
    def __len__(self):
        len = 1;
        if not self.variable is None:
            for dim in self.variable.getDimensions():
                len = len * dim.getLength()            
        return len
        
    def __str__(self):
        if self.variable is None:
            return 'None'
            
        r = self.datatype.toString() + ' ' + self.name + '('
        for dim in self.dims:
            dimname = dim.getShortName()
            if dimname is None:
                dimname = 'null'
            r = r + dimname + ','
        r = r[:-1] + '):'
        attrs = self.variable.getAttributes()
        for attr in attrs:
            r = r + '\n\t' + self.name + ': ' + attr.toString()
        return r
        
    def __repr__(self):
        return self.__str__()
        
    def __getitem__(self, indices):
        if indices is None:
            inds = []
            for i in range(self.ndim):
                inds.append(slice(None))
            indices = tuple(inds)
                
        if isinstance(indices, str):    #metadata
            rr = self.dataset.read(self.name)
            m = rr.findMember(indices)
            data = rr.getArray(0, m)
            return MIArray(data)
        
        if not isinstance(indices, tuple):
            inds = []
            inds.append(indices)
            indices = inds
        
        if len(indices) != self.ndim:
            print 'indices must be ' + str(self.ndim) + ' dimensions!'
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
                if sidx > eidx:
                    iidx = eidx
                    eidx = sidx
                    sidx = iidx
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
                dim = self.variable.getDimension(i)
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
                    if sidx > eidx:
                        iidx = eidx
                        eidx = sidx
                        sidx = iidx
            else:
                print k
                return None
            if isrange:
                if eidx >= dimlen:
                    print 'Index out of range!'
                    return None
                origin.append(sidx)
                n = eidx - sidx + 1
                size.append(n)                   
                if n > 1:
                    dim = self.variable.getDimension(i)
                    if dim.isReverse():
                        step = -step      
                    dim = dim.extract(sidx, eidx, step)
                    dim.setReverse(False)
                    dims.append(dim)
                stride.append(step) 
                if step < 0:
                    step = abs(step)
                    flips.append(i)
                rr = Range(sidx, eidx, step)
                ranges.append(rr)
            else:
                if len(k) > 1:
                    dim = self.variable.getDimension(i)
                    dim = dim.extract(k)
                    dim.setReverse(False)
                    dims.append(dim)
        #rr = self.dataset.read(self.name, origin, size, stride).reduce()
        if onlyrange:
            rr = self.dataset.dataset.read(self.name, ranges)
        else:
            rr = self.dataset.dataset.take(self.name, ranges)
        if rr.getSize() == 1:
            return rr.getObject(0)
        else:
            for i in flips:
                rr = rr.flip(i)
            rr = rr.reduce()
            ArrayMath.missingToNaN(rr, self.fill_value)
            if len(flips) > 0:
                rrr = Array.factory(rr.getDataType(), rr.getShape())
                MAMath.copy(rrr, rr)
                array = MIArray(rrr)
            else:
                array = MIArray(rr)
            data = DimArray(array, dims, self.fill_value, self.dataset.proj)
            return data
    
    def read(self):
        return MIArray(self.dataset.read(self.name))
    
    # get dimension length
    def dimlen(self, idx):
        return self.dims[idx].getLength()
        
    def dimvalue(self, idx, convert=False):
        '''
        Get dimension values.
        
        :param idx: (*int*) Dimension index.
        :param convert: (*boolean*) If convert to real values (i.e. datetime). Default
            is ``False``.
        
        :returns: (*array_like*) Dimension values
        '''
        dim = self.dims[idx]
        if convert:
            if dim.getDimType() == DimensionType.T:
                return miutil.nums2dates(dim.getDimValue())
            else:
                return MIArray(ArrayUtil.array(self.dims[idx].getDimValue()))
        else:
            return MIArray(ArrayUtil.array(self.dims[idx].getDimValue()))
        
    def attrvalue(self, key):
        attr = self.variable.findAttribute(key)
        if attr is None:
            return None
        v = MIArray(attr.getValues())
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
        if isinstance(dimvalue, MIArray):
            dimvalue = dimvalue.aslist()
        self.variable.addDimension(dimtype, dimvalue)
        self.ndim = self.variable.getDimNumber()
        
    def setdim(self, dimtype, dimvalue, index=None, reverse=False):
        if isinstance(dimvalue, MIArray):
            dimvalue = dimvalue.aslist()
        if index is None:
            self.variable.setDimension(dimtype, dimvalue, reverse)
        else:
            self.variable.setDimension(dimtype, dimvalue, reverse, index)
        self.ndim = self.variable.getDimNumber()
        
    def setdimrev(self, idx, reverse):
        self.dims[idx].setReverse(reverse)
        
    def addattr(self, attrname, attrvalue):
        self.ncvariable.addAttribute(Attribute(attrname, attrvalue))

# Variable in multiple data files (DimDataFiles) - only time dimension is different.
class TDimVariable():
    
    # variable must be org.meteoinfo.data.meteodata.Variable
    # dataset is DimDataFiles
    def __init__(self, variable, dataset):
        self.variable = variable
        self.dataset = dataset
        self.name = variable.getName()
        self.datatype = variable.getDataType()        
        self.ndim = variable.getDimNumber()
        self.fill_value = variable.getFillValue()
        self.scale_factor = variable.getScaleFactor()
        self.add_offset = variable.getAddOffset()
        dims = variable.getDimensions()
        tdim = Dimension(DimensionType.T)
        times = []
        for t in self.dataset.times:
            times.append(miutil.date2num(t))
        tdim.setDimValues(times)
        dims[0] = tdim
        self.dims = dims
        self.tnum = len(times)
        
    def __getitem__(self, indices):
        if len(indices) != self.ndim:
            print 'indices must be ' + str(self.ndim) + ' dimensions!'
            return None
        
        k = indices[0]
        if isinstance(k, int):
            sidx = k
            eidx = k
            step = 1
        elif isinstance(k, slice):
            sidx = 0 if k.start is None else k.start
            if sidx < 0:
                sidx = self.tnum + sidx
            eidx = self.tnum if k.stop is None else k.stop
            if eidx < 0:
                eidx = self.tnum + eidx
            eidx -= 1
            step = 1 if k.step is None else k.step
        elif isinstance(k, list):
            sidx = self.dataset.timeindex(k[0])
            if len(k) == 1:
                eidx = sidx
                step = 1
            else:
                eidx = self.dataset.timeindex(k[1])
                if len(k) == 3:
                    tt = self.dataset.timeindex(k[0] + k[3])
                    step = tt - sidx
                else:
                    step = 1
        
        sfidx = self.dataset.datafileindex(sidx)
        si = sidx
        isfirst = True
        times = []
        fidx = sfidx
        aa = None
        var = None
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
                    aa.addtdim(self.dataset.gettime(si))
                if isfirst:
                    data = aa
                    isfirst = False
                else:
                    data = minum.concatenate([data, aa])
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
                aa.addtdim(self.dataset.gettime(si))
            if isfirst:
                data = aa
                isfirst = False
            else:
                data = minum.concatenate([data, aa])
        
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
                
        if isinstance(data, DimArray):
            return data
        else:
            dims = aa.dims
            dims[0].setDimValues(times)
            r = DimArray(data, dims, aa.fill_value, aa.proj)
            return r