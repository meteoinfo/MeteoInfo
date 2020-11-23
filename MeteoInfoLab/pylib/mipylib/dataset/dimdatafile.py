#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2014-12-27
# Purpose: MeteoInfo Dataset module
# Note: Jython
#-----------------------------------------------------
from org.meteoinfo.data.meteodata import MeteoDataType
from org.meteoinfo.data.meteodata.netcdf import NCUtil
from org.meteoinfo.ndarray import DimensionType, Dimension
from ucar.ma2 import DataType as NCDataType
from ucar.nc2 import Attribute as NCAttribute
from dimvariable import DimVariable, TDimVariable
from mipylib.geolib.milayer import MILayer, MIXYListData
from mipylib.dataframe.dataframe import DataFrame
import mipylib.miutil as miutil
import mipylib.numeric as np
from mipylib.numeric.core._dtype import DataType

import datetime

from java.lang import Float
import jarray

# Dimension dataset
class DimDataFile(object):
    
    # dataset must be org.meteoinfo.data.meteodata.MeteoDataInfo
    def __init__(self, dataset=None, access='r', ncfile=None, arldata=None, bufrdata=None):
        self.dataset = dataset
        self.access = access
        if not dataset is None:
            self.filename = dataset.getFileName()
            self.nvar = dataset.getDataInfo().getVariableNum()
            self.fill_value = dataset.getMissingValue()
            self.proj = dataset.getProjectionInfo()
        self.ncfile = ncfile
        self.arldata = arldata
        self.bufrdata = bufrdata
        
    def __getitem__(self, key):
        if isinstance(key, basestring):
            var = self.dataset.getDataInfo().getVariable(key)
            if var is None:
                print key + ' is not a variable name'
                raise ValueError()
            else:
                return DimVariable(self.dataset.getDataInfo().getVariable(key), self)
        else:
            print key + ' is not a variable name'
            raise ValueError()
        
    def __str__(self):
        if self.dataset is None:
            return 'None'
        return self.dataset.getInfoText()
        
    def __repr__(self):
        if self.dataset is None:
            return 'None'
        return self.dataset.getInfoText()
            
    def close(self):
        '''
        Close the opended dataset
        '''
        if not self.dataset is None:
            self.dataset.close()
        elif not self.ncfile is None:
            self.ncfile.close()
        elif not self.arldata is None:
            self.arldata.closeDataFile()
        elif not self.bufrdata is None:
            self.bufrdata.closeDataFile()

    def reopen(self):
        """
        Reopen the data file
        """
        self.dataset.getDataInfo().reOpen()
    
    def dimensions(self):
        '''
        Get dimensions
        '''
        return self.dataset.getDataInfo().getDimensions()
        
    def finddim(self, name):
        '''
        Find a dimension by name
        
        :name: (*string*) Dimension name
        '''
        for dim in self.dataset.getDataInfo().getDimensions():
            if name == dim.getShortName():
                return dim
        return None
        
    def attributes(self):
        '''
        Get global attributes.
        '''
        return self.dataset.getDataInfo().getGlobalAttributes()
    
    def attrvalue(self, attr):
        '''
        Get a global attribute value.
        
        :param attr: (*string or Attribute*) Attribute or Attribute name
        '''
        if isinstance(attr, str):
            attr = self.dataset.getDataInfo().findGlobalAttribute(attr)
        
        if attr is None:
            return None
        v = np.array(attr.getValues())
        return v
        
    def variables(self):
        '''
        Get all variables.
        '''
        vars = []
        for var in self.dataset.getDataInfo().getVariables():
            vars.append(DimVariable(var))
            
        return vars
        
    def varnames(self):
        '''
        Get all variable names.
        '''
        return self.dataset.getDataInfo().getVariableNames()
        
    def read(self, varname, origin=None, size=None, stride=None):
        '''
        Read data array from a variable.
        
        :varname: (*string*) Variable name
        '''
        if origin is None:
            return self.dataset.read(varname)
        else:
            return self.dataset.read(varname, origin, size, stride)
        
    def dump(self):
        '''
        Print data file information
        '''
        print self.dataset.getInfoText()
        
    def read_dataframe(self, tidx=None):
        '''
        Read data frame from dataset.
        :param tidx: (*int*) Time index. Default is ``None``.
        :returns: (*DataFrame*) The DataFrame.
        '''
        if tidx is None:
            df = self.dataset.getDataInfo().readDataFrame()
        else:
            df = self.dataset.getDataInfo().readDataFrame(tidx)
        return DataFrame(dataframe=df)
        
    def read_table(self):
        '''
        Read data table from dataset.
        '''
        dt = self.dataset.getDataInfo().readTable()
        return np.datatable(dt)
        
    def griddata(self, varname='var', timeindex=0, levelindex=0, yindex=None, xindex=None):
        if self.dataset.isGridData():
            self.dataset.setTimeIndex(timeindex)
            self.dataset.setLevelIndex(levelindex)
            gdata = PyGridData(self.dataset.getGridData(varname))
            return gdata
        else:
            return None
        
    def stationdata(self, varname='var', timeindex=0, levelindex=0):
        if self.dataset.isStationData():
            self.dataset.setTimeIndex(timeindex)
            self.dataset.setLevelIndex(levelindex)
            sdata = PyStationData(self.dataset.getStationData(varname))
            return sdata
        else:
            return None
            
    def stinfodata(self):
        '''
        Get station info data
        '''
        if self.dataset.isStationData():
            sidata = self.dataset.getStationInfoData()
            return sidata
        else:
            return None
            
    def smodeldata(self, timeindex=0, levelindex=0):
        '''
        Get station model data.
        '''
        if self.dataset.isStationData():
            self.dataset.setTimeIndex(timeindex)
            self.dataset.setLevelIndex(levelindex)
            smdata = self.dataset.getStationModelData()
            return smdata
        else:
            return None
            
    def trajlayer(self):
        '''
        Create trajectory polyline layer.
        '''
        if self.dataset.isTrajData():
            return MILayer(self.dataset.getDataInfo().createTrajLineLayer())
        else:
            return None
            
    def trajplayer(self):
        '''
        Create trajectory point layer.
        '''
        if self.dataset.isTrajData():
            return MILayer(self.dataset.getDataInfo().createTrajPointLayer())
        else:
            return None
            
    def trajsplayer(self):
        '''
        Create trajectory start point layer.
        '''
        if self.dataset.isTrajData():
            return MILayer(self.dataset.getDataInfo().createTrajStartPointLayer())
        else:
            return None
            
    def trajvardata(self, varidx, hourx=False):
        '''
        Get trajectory variable data.
        '''
        if self.dataset.isTrajData():
            if hourx:
                return MIXYListData(self.dataset.getDataInfo().getXYDataset_HourX(varidx))
            else:
                return MIXYListData(self.dataset.getDataInfo().getXYDataset(varidx))
        else:
            return None
    
    def timenum(self):
        """
        Get time dimension length
        
        :returns: (*int*) Time dimension length.
        """
        return self.dataset.getDataInfo().getTimeNum()
    
    def gettime(self, idx):
        '''
        Get time by index.
        
        :param idx: (*int*) Time index.
        
        :returns: (*datetime*) The time
        '''
        t = self.dataset.getDataInfo().getTimes().get(idx)     
        t = miutil.pydate(t)
        return t
        
    def gettimes(self):
        '''
        Get time list.
        '''
        tt = self.dataset.getDataInfo().getTimes()
        times = []
        for t in tt:
            times.append(miutil.pydate(t))
        return times
        
    def bigendian(self, big_endian):
        '''
        Set dataset as big_endian or little_endian. Only for GrADS binary data.
        
        :param big_endian: (*boolean*) Big endian or not.
        '''
        datatype = self.dataset.getDataInfo().getDataType()
        if datatype.isGrADS() or datatype == MeteoDataType.HYSPLIT_Conc:
            self.dataset.getDataInfo().setBigEndian(big_endian)            
            
    def tostation(self, varname, x, y, z, t):
        '''
        Interpolate data to a point.
        '''
        if isinstance(t, datetime.datetime):
            t = miutil.jdate(t)
        if z is None:
            return self.dataset.toStation(varname, x, y, t)
        else:
            return self.dataset.toStation(varname, x, y, z, t)

####################################################################            
    #Write netCDF data    
    def adddim(self, dimname, dimsize, group=None):
        '''
        Add a dimension.
        
        :param dimname: (*string*) Dimension name.
        :param dimsize: (*int*) Dimension size.
        :param group: None means global dimension.
        '''
        return self.ncfile.addDimension(group, dimname, dimsize)
        
    def addgroupattr(self, attrname, attrvalue, group=None, float=False):
        '''
        Add a global attribute.
        
        :param attrname: (*string*) Attribute name.
        :param attrvalue: (*object*) Attribute value.
        :param group: None means global attribute.
        :param float: (*boolean*) Transfer data as float or not.
        '''
        if float:
            if isinstance(attrvalue, (list, tuple)):
                for i in range(len(attrvalue)):
                    attrvalue[i] = Float(attrvalue[i])
            else:
                attrvalue = Float(attrvalue)
        if isinstance(attrvalue, np.NDArray):
            attrvalue = NCUtil.convertArray(attrvalue._array)
        return self.ncfile.addGroupAttribute(group, NCAttribute(attrname, attrvalue))
 
    def __getdatatype(self, datatype):
        if isinstance(datatype, str):
            if datatype == 'string':
                dt = NCDataType.STRING
            elif datatype == 'int':
                dt = NCDataType.INT
            elif datatype == 'long':
                dt = NCDataType.LONG
            elif datatype == 'float':
                dt = NCDataType.FLOAT
            elif datatype == 'double':
                dt = NCDataType.DOUBLE
            elif datatype == 'char':
                dt = NCDataType.CHAR
            else:
                dt = NCDataType.STRING
            return dt
        else:
            if isinstance(datatype, DataType):
                datatype = NCUtil.convertDataType(datatype._dtype)
            return datatype
 
    def addvar(self, varname, datatype, dims, attrs=None, group=None):
        '''
        Add a variable.
        
        :param varname: (*string*) Variable name.
        :param datatype: (*string*) Data type [string | int | long | float | double |
            char].
        :param dims: (*list*) Dimensions.
        :param attrs: (*dict*) Attributes.
        '''
        dt = self.__getdatatype(datatype)
        if isinstance(dims[0], Dimension):
            ncdims = []
            for dim in dims:
                ncdims.append(self.ncfile.findDimension(dim.getName()))
            var = DimVariable(ncvariable=self.ncfile.addVariable(group, varname, dt, ncdims))
        else:
            var = DimVariable(ncvariable=self.ncfile.addVariable(group, varname, dt, dims))
        if not attrs is None:
            for key in attrs:
                var.addattr(key, attrs[key])
        return var
        
    def create(self):
        '''
        Create a netCDF data file according the settings of dimensions, global attributes
        and variables
        '''
        self.ncfile.create()

    def nc_define(self, dims, gattrs, vars):
        '''
        Define dimensions, global attributes, variables of the netcdf file
        :param dims: (*list of Dimension*) The dimensions
        :param gattrs: (*list of Attribute*) The global attributes
        :param vars: (*list of DimVariable*) The variables
        '''
        #Add dimensions
        ncdims = []
        for dim in dims:
            ncdims.append(self.adddim(dim.getName(), dim.getLength()))

        #Add global attributes
        if not gattrs is None:
            for key in gattrs:
                self.addgroupattr(key, gattrs[key])

        #Add dimension variables
        dimvars = []
        wdims = []
        for dim,midim in zip(ncdims,dims):
            dimtype = midim.getDimType()
            dimname = dim.getShortName()
            if dimtype == DimensionType.T:
                var = self.addvar(dimname, 'int', [dim])
                var.addattr('units', 'hours since 1900-01-01 00:00:0.0')
                var.addattr('long_name', 'Time')
                var.addattr('standard_name', 'time')
                var.addattr('axis', 'T')
                tvar = var
            elif dimtype == DimensionType.Z:
                var = self.addvar(dimname, 'float', [dim])
                var.addattr('axis', 'Z')
            elif dimtype == DimensionType.Y:
                var = self.addvar(dimname, 'float', [dim])
                var.addattr('axis', 'Y')
            elif dimtype == DimensionType.X:
                var = self.addvar(dimname, 'float', [dim])
                var.addattr('axis', 'X')
            else:
                var = None
            if not var is None:
                dimvars.append(var)
                wdims.append(midim)

        #Add variables
        for v in vars:
            var = self.addvar(v.name, v.dtype, v.dims)
            v.ncvariable = var
            if v.attributes is None:
                var.addattr('name', v.name)
            else:
                for attr in v.attributes:
                    var.addattr(attr.getName(), attr.getStringValue())

        #Create netCDF file
        self.ncfile.create()

        #Write dimension variable data
        for dimvar, dim in zip(dimvars, wdims):
            if dim.getDimType() == DimensionType.T:
                sst = datetime.datetime(1900,1,1)
                tt = miutil.nums2dates(dim.getDimValue())
                hours = []
                for t in tt:
                    hours.append((t - sst).total_seconds() // 3600)
                self.write(dimvar, np.array(hours))
            else:
                self.write(dimvar, np.array(dim.getDimValue()))
        
    def write(self, variable, value, origin=None):
        '''
        Write variable value.
        
        :param variable: (*Variable*) Variable object.
        :param value: (*array_like*) Data array to be write.
        :param origin: (*list*) Dimensions origin indices. None means all from 0.
        '''
        if isinstance(value, (list, tuple)):
            value = np.array(value)
        if isinstance(value, np.NDArray):
            value = NCUtil.convertArray(value._array)
        if isinstance(variable, DimVariable):
            if self.access == 'c':
                ncvariable = variable.ncvariable
                if ncvariable is None:
                    ncvariable = self.ncfile.findVariable(variable.name)
                if ncvariable is None:
                    ncvariable = variable.name
            else:
                ncvariable = self.dataset.getDataInfo().findNCVariable(variable.name)
        else:
            ncvariable = variable
        if origin is None:
            self.ncfile.write(ncvariable, value)
        else:
            origin = jarray.array(origin, 'i')
            self.ncfile.write(ncvariable, origin, value)

    def flush(self):
        '''
        Flush the data.
        '''
        self.ncfile.flush()        
        
    def largefile(self, islarge=True):
        '''
        Set the netCDF file is large file (more than 2G) nor not.
        
        :param islarge: (*boolean*) Is large file or not.
        '''
        self.ncfile.setLargeFile(islarge)
        
##################################################################
    # Write ARL data
    def setx(self, x):
        '''
        Set x (longitude) dimension value.
        
        :param x: (*array_like*) X dimension value.
        '''
        self.arldata.setX(x.aslist())
        
    def sety(self, y):
        '''
        Set y (latitude) dimension value.
        
        :param y: (*array_like*) Y dimension value.
        '''
        self.arldata.setY(y.aslist())
        
    def setlevels(self, levels):
        '''
        Set vertical levels.
        
        :param leveles: (*list*) Vertical levels.
        '''
        if isinstance(levels, np.NDArray):
            levels = levels.aslist()
        if levels[0] != 1:
            levels.insert(0, 1)
        self.arldata.levels = levels
        
    def set2dvar(self, vnames):
        '''
        Set surface variables (2 dimensions ignore time dimension).
        
        :param vnames: (*list*) Variable names.
        '''
        self.arldata.LevelVarList.add(vnames)
        
    def set3dvar(self, vnames):
        '''
        Set level variables (3 dimensions ignore time dimension).
        
        :param vnames: (*list*) Variable names.
        '''
        self.arldata.LevelVarList.add(vnames)
    
    def getdatahead(self, proj, model, vertical, icx=0, mn=0):
        '''
        Get data head.
        
        :param proj: (*ProjectionInfo*) Projection information.
        :param model: (*string*) Model name with 4 characters.
        :param vertical: (*int*) Vertical coordinate system flag. 1-sigma (fraction);
            2-pressure (mb); 3-terrain (fraction); 4-hybrid (mb: offset.fraction)
        :param icx: (*int*) Forecast hour (>99 the header forecast hr = 99)
        :param mn: (*int*) Minutes associated with data time.
        '''
        return self.arldata.getDataHead(proj, model, vertical, icx, mn)

    def diff_origin_pack(self, data):
        '''
        Get difference between the original data and the packed data.
        :param data: (*array*) The original data.
        :return: (*array*) Difference.
        '''
        r = self.arldata.diffOriginPack(data._array)
        return np.NDArray(r)
        
    def writeindexrec(self, t, datahead, ksums=None):
        '''
        Write index record.
        
        :param t: (*datatime*) The time of the data.
        :param datahead: (*DataHeader') Data header of the record.
        :param ksums: (*list*) Check sum list.
        '''
        t = miutil.jdate(t)
        self.arldata.writeIndexRecord(t, datahead, ksums)
        
    def writedatarec(self, t, lidx, vname, fhour, grid, data):
        '''
        Write data record.
        
        :param t: (*datetime*) The time of the data.
        :param lidx: (*int*) Level index.
        :param vname: (*string*) Variable name.
        :param fhour: (*int*) Forecasting hour.
        :param grid: (*int*) Grid id to check if the data grid is bigger than 999. Header 
            record does not support grids of more than 999, therefore in those situations 
            the grid number is converted to character to represent the 1000s digit, 
            e.g. @(64)=<1000, A(65)=1000, B(66)=2000, etc.
        :param data: (*array_like*) Data array.
        
        :returns: (*int*) Check sum of the record data.
        '''
        t = miutil.jdate(t)
        ksum = self.arldata.writeGridData(t, lidx, vname, fhour, grid, data.asarray())
        return ksum

########################################################################        
    # Write Bufr data
    def write_indicator(self, bufrlen, edition=3):
        '''
        Write indicator section with arbitrary length.
        
        :param bufrlen: (*int*) The total length of the message.
        :param edition: (*int*) Bruf edition.
        
        :returns: (*int*) Indicator section length.
        '''
        return self.bufrdata.writeIndicatorSection(bufrlen, edition)
        
    def rewrite_indicator(self, bufrlen, edition=3):
        '''
        Write indicator section with correct length.
        
        :param bufrlen: (*int*) The total length of the message.
        :param edition: (*int*) Bruf edition.
        '''
        self.bufrdata.reWriteIndicatorSection(bufrlen, edition)
        
    def write_identification(self, **kwargs):
        '''
        Write identification section.
        
        :param length: (*int*) Section length
        :param master_table: (*int*) Master table
        :param subcenter_id: (*int*) Subcenter id
        :param center_id: (*int*) Center id
        :param update: (*int*) Update sequency
        :param optional: (*int*) Optional
        :param category: (*int*) Category
        :param sub_category: (*int*) Sub category
        :param master_table_version: (*int*) Master table version
        :param local_table_version: (*int*) Local table version
        :param year: (*int*) Year
        :param month: (*int*) Month
        :param day: (*int*) Day
        :param hour: (*int*) Hour
        :param minute: (*int*) Minute
        
        :returns: (*int*) Section length
        '''
        length = kwargs.pop('length', 18)
        master_table = kwargs.pop('master_table', 0)
        subcenter_id = kwargs.pop('subcenter_id', 0)
        center_id = kwargs.pop('center_id', 74)
        update = kwargs.pop('update', 0)
        optional = kwargs.pop('optional', 0)
        category = kwargs.pop('category', 7)
        sub_category = kwargs.pop('sub_category', 0)
        master_table_version = kwargs.pop('master_table_version', 11)
        local_table_version = kwargs.pop('local_table_version', 1)
        year = kwargs.pop('year', 2016)
        month = kwargs.pop('month', 1)
        day = kwargs.pop('day', 1)
        hour = kwargs.pop('hour', 0)
        minute = kwargs.pop('minute', 0)
        return self.bufrdata.writeIdentificationSection(length, master_table, subcenter_id, center_id,\
            update, optional, category, sub_category, master_table_version,\
            local_table_version, year, month, day, hour, minute)
            
    def write_datadescription(self, n, datatype, descriptors):
        '''
        Write data description section
        
        :param n: (*int*) Numer of dataset.
        :param datatype: (*int*) Data type.
        :param descriptors: (*list*) Data descriptors.
        '''
        return self.bufrdata.writeDataDescriptionSection(n, datatype, descriptors)
        
    def write_datahead(self, len):
        '''
        Write data header with arbitrary data length.
        
        :param len: (*int*) Data section length.
        
        :returns: (*int*) Data section head length - always 4.
        '''
        return self.bufrdata.writeDataSectionHead(len)
        
    def rewrite_datahead(self, len):
        '''
        Write data header with correct data length.
        
        :param len: (*int*) Data section length.
        '''
        self.bufrdata.reWriteDataSectionHead(len)
        
    def write_data(self, value, nbits=None):
        '''
        Write data.
        
        :param value: (*int*) Value.
        :param nbits: (*int*) Bit number.
        
        :returns: (*int*) Data value length.
        '''
        return self.bufrdata.write(value, nbits)
        
    def write_end(self):
        '''
        Write end section ('7777').
        
        :returns: (*int*) End section length - always 4.
        '''
        return self.bufrdata.writeEndSection()

        
#*********************************************
# Created by addfiles function in midata module - multiple data files with difference only 
# on time dimension.      
class DimDataFiles(list):
    
    # dataset must be list of DimDataFile
    def __init__(self, dataset=[]):
        list.__init__([])
        ndataset = []
        ftimes = []
        for ds in dataset:
            if len(ndataset) == 0:
                ndataset.append(ds)
                ftimes.append(ds.gettime(0))
            else:
                idx = len(ndataset)
                ftime = ds.gettime(0)
                for i in range(len(ndataset)):                    
                    if ftime < ftimes[i]:
                        idx = i
                        break
                ndataset.insert(idx, ds)
                ftimes.insert(idx, ftime)
                
        self.extend(ndataset)
        self.times = []
        self.tnums = []
        self.tnum = 0
        for ds in ndataset:
            tts = ds.gettimes()
            self.times.extend(tts)
            self.tnums.append(len(tts))
            self.tnum += len(tts)
        
    def append(self, ddf):
        self.append(ddf)
        tts = ddf.gettimes()
        self.times.extend(tts)
        self.tnums.append(len(tts))
        self.tnum += len(tts)
        
    def __getitem__(self, key):
        if isinstance(key, str):
            #print key
            return TDimVariable(self[0].dataset.getDataInfo().getVariable(key), self)
        else:
            return list.__getitem__(self, key)
    
    def filenames(self):
        '''
        Get file names.
        
        :returns: File name list
        '''
        fns = []
        for df in self:
            fns.append(df.filename)
        return fns
    
    def datafileindex(self, t):
        """
        Get data file by time
        
        :param t: (*datetime or idx*) Time value of index.
        
        :returns: (*int*) Data file index
        """
        if isinstance(t, datetime.datetime):
            t = self.timeindex(t)
        nn = 0
        idx = 0
        for n in self.tnums:
            nn += n
            if t < nn:
                break
            idx += 1
        return idx
        
    def datafile(self, t):
        """
        Get data file by time
        
        :param t: (*datetime or idx*) Time value of index.
        
        :returns: (*DimDataFile*) Data file
        """
        idx = self.datafileindex(t)
        return self[idx]
        
    def dftindex(self, t):
        '''
        Get data file index and time index of it.
        
        :param t: (*datetime or idx*) Time value of index.
        
        :returns: (*list of int*) Data file index and time index of it.
        '''
        if isinstance(t, datetime.datetime):
            t = self.timeindex(t)
        nn = 0
        dfidx = 0
        tidx = 0
        sn = 0
        for n in self.tnums:
            nn += n
            if t < nn:
                tidx = t - sn
                break
            dfidx += 1
            sn = nn
        return dfidx, tidx
        
    def timeindex(self, t):
        '''
        Get time index.
        
        :param t: (*datetime*) Given time
        
        :returns: (*int*) Time index
        '''
        idx = 0
        for tt in self.times:
            if t >= tt:
                break
            idx += 1
        return idx
    
    def gettime(self, idx):
        '''
        Get time by index.
        
        :param idx: (*int*) Time index.
        
        :returns: (*datetime*) The time
        '''        
        return self.times[idx]
        
    def varnames(self):
        '''
        Get variable names
        '''
        return self[0].varnames()
        
#############################################