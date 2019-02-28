#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2016-7-4
# Purpose: MeteoInfo io module
# Note: Jython
#-----------------------------------------------------

import datetime
import mipylib.numeric.minum as minum
import mipylib.miutil as miutil
from mipylib.numeric.miarray import MIArray
from mipylib.numeric.dimarray import DimArray
import midata as midata
from dimdatafile import DimDataFile
from org.meteoinfo.data.meteodata import Dimension, DimensionType

__all__ = [
    'convert2nc','dimension','grads2nc','ncwrite'
    ]

def convert2nc(infn, outfn, version='netcdf3', writedimvar=False, largefile=False):
    """
    Convert data file (Grib, HDF...) to netCDF data file.
    
    :param infn: (*string or DimDataFile*) Input data file (or file name).
    :param outfn: (*string*) Output netCDF data file name.
    :param writedimvar: (*boolean*) Write dimension variables or not.
    :param largefile: (*boolean*) Create netCDF as large file or not.
    """
    if isinstance(infn, DimDataFile):
        f = infn
    else:
        #Open input data file
        f = midata.addfile(infn)
        
    #New netCDF file
    ncfile = midata.addfile(outfn, 'c', version=version, largefile=largefile)
    
    #Add dimensions
    dims = []
    for dim in f.dimensions():
        dims.append(ncfile.adddim(dim.getShortName(), dim.getLength()))
        
    #Add global attributes
    for attr in f.attributes():
        ncfile.addgroupattr(attr.getName(), attr.getValues())
        
    #Add dimension variables
    tvar = None
    if writedimvar:
        dimvars = []
        for i in range(len(f.dimensions())):
            dim = f.dimensions()[i]
            dname = dim.getShortName()
            if dim.getDimType() == DimensionType.T:
                var = ncfile.addvar(dname, 'int', [dims[i]])
                var.addattr('units', 'hours since 1900-01-01 00:00:0.0')
                var.addattr('long_name', 'Time')
                var.addattr('standard_name', 'time')
                var.addattr('axis', 'T')
                tvar = var
            elif dim.getDimType() == DimensionType.Z:
                var = ncfile.addvar(dname, 'float', [dims[i]])
                var.addattr('long_name', 'Level')
                var.addattr('axis', 'Z')
            elif dim.getDimType() == DimensionType.Y:
                var = ncfile.addvar(dname, 'float', [dims[i]])
                var.addattr('long_name', dname)
                var.addattr('axis', 'Y')
            elif dim.getDimType() == DimensionType.X:
                var = ncfile.addvar(dname, 'float', [dims[i]])
                var.addattr('long_name', dname)
                var.addattr('axis', 'X')
            else:
                var = ncfile.addvar(dname, 'float', [dims[i]])
                var.addattr('long_name', dname)
                var.addattr('axis', dname)
            dimvars.append(var)
        
    #Add variables
    variables = []
    for var in f.variables():    
        #print 'Variable: ' + var.getShortName()
        if var.hasNullDimension():
            continue
        vdims = []
        missdim = False
        for vdim in var.getDimensions():
            isvalid = False
            for dim in dims:
                if dim.getShortName() == vdim.getShortName():
                    vdims.append(dim)
                    isvalid = True
                    break
            if not isvalid:
                missdim = True
                break
        if missdim:
            continue
        nvar = ncfile.addvar(var.getShortName(), var.getDataType(), vdims)
        for attr in var.getAttributes():
            nvar.addattr(attr.getName(), attr.getValues())
        variables.append(nvar)
        
    #Create netCDF file
    ncfile.create()
    
    #Write dimension variable data
    if writedimvar:
        for dimvar, dim in zip(dimvars, f.dimensions()):
            if dim.getDimType() != DimensionType.T:
                ncfile.write(dimvar, minum.array(dim.getDimValue()))
    
    #Write time dimension variable data
    if writedimvar and not tvar is None:
        sst = datetime.datetime(1900,1,1)
        tnum = f.timenum()
        hours = []
        for t in range(0, tnum):
            st = f.gettime(t)
            hs = (st - sst).total_seconds() // 3600
            hours.append(hs)
        ncfile.write(tvar, minum.array(hours))
    
    #Write variable data
    for var in variables:
        print 'Variable: ' + var.name
        data = f[str(var.name)].read()
        ncfile.write(var, data)    
        
    #Close netCDF file
    ncfile.close()
    print 'Convert finished!'
    
def grads2nc(infn, outfn, big_endian=None, largefile=False):
    """
    Convert GrADS data file to netCDF data file.
    
    :param infn: (*string*) Input GrADS data file name.
    :param outfn: (*string*) Output netCDF data file name.
    :param big_endian: (*boolean*) Is GrADS data big_endian or not.
    :param largefile: (*boolean*) Create netCDF as large file or not.
    """
    #Open GrADS file
    f = midata.addfile_grads(infn)
    if not big_endian is None:
        f.bigendian(big_endian)

    #New netCDF file
    ncfile = midata.addfile(outfn, 'c')

    #Add dimensions
    dims = []
    for dim in f.dimensions():
        dims.append(ncfile.adddim(dim.getShortName(), dim.getLength()))
    xdim = f.finddim('X')
    ydim = f.finddim('Y')
    tdim = f.finddim('T')
    xnum = xdim.getLength()
    ynum = ydim.getLength()
    tnum = tdim.getLength()

    #Add global attributes
    ncfile.addgroupattr('Conventions', 'CF-1.6')
    for attr in f.attributes():
        ncfile.addgroupattr(attr.getName(), attr.getValues())

    #Add dimension variables
    dimvars = []
    for dim in dims:
        dname = dim.getShortName()
        if dname == 'T':
            var = ncfile.addvar('time', 'int', [dim])
            var.addattr('units', 'hours since 1900-01-01 00:00:0.0')
            var.addattr('long_name', 'Time')
            var.addattr('standard_name', 'time')
            var.addattr('axis', dname)
            tvar = var
        elif dname == 'Z':
            var = ncfile.addvar('level', 'float', [dim])
            var.addattr('axis', dname)
        else:
            var = ncfile.addvar(dim.getShortName(), 'float', [dim])
            if 'Z' in dname:
                var.addattr('axis', 'Z')
            else:
                var.addattr('axis', dname)
        dimvars.append(var)

    #Add variables
    variables = []
    for var in f.variables():    
        print 'Variable: ' + var.getShortName()
        vdims = []
        for vdim in var.getDimensions():
            for dim in dims:
                if vdim.getShortName() == dim.getShortName():
                    vdims.append(dim)
        #print vdims
        nvar = ncfile.addvar(var.getShortName(), var.getDataType(), vdims)
        nvar.addattr('fill_value', -9999.0)
        for attr in var.getAttributes():
            nvar.addattr(attr.getName(), attr.getValues())
        variables.append(nvar)

    #Create netCDF file
    ncfile.create()

    #Write variable data
    for dimvar, dim in zip(dimvars, f.dimensions()):
        if dim.getShortName() != 'T':
            ncfile.write(dimvar, minum.array(dim.getDimValue()))

    sst = datetime.datetime(1900,1,1)
    for t in range(0, tnum):
        st = f.gettime(t)
        print st.strftime('%Y-%m-%d %H:00')
        hours = (st - sst).total_seconds() // 3600
        origin = [t]
        ncfile.write(tvar, minum.array([hours]), origin=origin)
        for var in variables:
            print 'Variable: ' + var.name
            if var.ndim == 3:
                data = f[str(var.name)][t,:,:]    
                data[data==minum.nan] = -9999.0        
                origin = [t, 0, 0]
                shape = [1, ynum, xnum]
                data = data.reshape(shape)
                ncfile.write(var, data, origin=origin)
            else:
                znum = var.dims[1].getLength()
                for z in range(0, znum):
                    data = f[str(var.name)][t,z,:,:]
                    data[data==minum.nan] = -9999.0
                    origin = [t, z, 0, 0]
                    shape = [1, 1, ynum, xnum]
                    data = data.reshape(shape)
                    ncfile.write(var, data, origin=origin)

    #Close netCDF file
    ncfile.close()
    print 'Convert finished!'
    
def dimension(dimvalue, dimname='null', dimtype=None):
    """
    Create a new Dimension.
    
    :param dimvalue: (*array_like*) Dimension value.
    :param dimname: (*string*) Dimension name.
    :param dimtype: (*DimensionType*) Dimension type.
    """
    if isinstance(dimvalue, (MIArray, DimArray)):
        dimvalue = dimvalue.aslist()
    dtype = DimensionType.Other
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
    dim.setShortName(dimname)
    return dim
    
def ncwrite(fn, data, varname, dims=None, attrs=None, largefile=False):
    """
    Write a netCDF data file.
    
    :param: fn: (*string*) netCDF data file path.
    :param data: (*array_like*) A numeric array variable of any dimensionality.
    :param varname: (*string*) Variable name.
    :param dims: (*list of dimensions*) Dimension list.
    :param attrs: (*list of attributes*) Attribute list.
    :param largefile: (*boolean*) Create netCDF as large file or not.
    """
    if dims is None:
        if isinstance(data, MIArray):
            dims = []
            for s in data.shape:
                dimvalue = minum.arange(s)
                dimname = 'dim' + str(len(dims))
                dims.append(dimension(dimvalue, dimname))
        else:
            dims = data.dims
    #New netCDF file
    ncfile = midata.addfile(fn, 'c')
    #Add dimensions
    ncdims = []
    for dim in dims:    
        ncdims.append(ncfile.adddim(dim.getShortName(), dim.getLength()))
    #Add global attributes
    ncfile.addgroupattr('Conventions', 'CF-1.6')
    ncfile.addgroupattr('Tools', 'Created using MeteoInfo')
    #Add dimension variables
    dimvars = []
    for dim,midim in zip(ncdims,dims):
        dimtype = midim.getDimType()
        dimname = dim.getShortName()
        if dimtype == DimensionType.T:
            var = ncfile.addvar(dimname, 'int', [dim])
            var.addattr('units', 'hours since 1900-01-01 00:00:0.0')
            var.addattr('long_name', 'Time')
            var.addattr('standard_name', 'time')
            var.addattr('axis', 'T')
            tvar = var
        elif dimtype == DimensionType.Z:
            var = ncfile.addvar(dimname, 'float', [dim])
            var.addattr('axis', 'Z')
        elif dimtype == DimensionType.Y:
            var = ncfile.addvar(dimname, 'float', [dim])
            var.addattr('axis', 'Y')
        elif dimtype == DimensionType.X:
            var = ncfile.addvar(dimname, 'float', [dim])
            var.addattr('axis', 'X')
        else:
            var = ncfile.addvar(dim.getShortName(), 'float', [dim])
            var.addattr('axis', 'null')
        dimvars.append(var)
    #Add variable
    var = ncfile.addvar(varname, data.dtype, ncdims)
    if attrs is None:    
        var.addattr('name', varname)
    else:
        for key in attrs:
            var.addattr(key, attr[key])
    #Create netCDF file
    ncfile.create()
    #Write variable data
    for dimvar, dim in zip(dimvars, dims):
        if dim.getDimType() == DimensionType.T:
            sst = datetime.datetime(1900,1,1)
            tt = miutil.nums2dates(dim.getDimValue())
            hours = []
            for t in tt:
                hours.append((t - sst).total_seconds() // 3600)
            ncfile.write(dimvar, minum.array(hours))
        else:
            ncfile.write(dimvar, minum.array(dim.getDimValue()))
    ncfile.write(var, data)
    #Close netCDF file
    ncfile.close()