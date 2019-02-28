#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2018-7-18
# Purpose: MeteoInfo index module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.data.dataframe import Index as MIIndex
from org.meteoinfo.data.dataframe import DateTimeIndex as MIDateTimeIndex
from org.joda.time import DateTime

import datetime

from mipylib.numeric.miarray import MIArray
import mipylib.miutil as miutil

class Index(object):
    
    @staticmethod
    def factory(data=None, name='Index', index=None):
        '''
        Factory method
        '''
        if index is None:
            if isinstance(data[0], (DateTime, datetime.datetime)):
                return DateTimeIndex(data, name)
            else:
                return Index(data, name)
        else:
            if isinstance(index, MIDateTimeIndex):
                return DateTimeIndex(index=index)
            else:
                return Index(index=index)
    
    def __init__(self, data=None, name='Index', index=None):
        '''
        Index 
        
        :param data: (*array_like*) Index values
        :param name: (*string*) Index name                
        '''
        if index is None:
            if isinstance(data, MIArray):
                data = data.aslist()
            self.data = data
            self._index = MIIndex(data, name)            
            self.name = name
        else:
            self._index = index
            self.data = list(self._index.getData())
            self.name = self._index.getName()
        
    def __len__(self):
        return self._index.size()
        
    def __iter__(self):
        """
        provide iteration over the values of the Index
        """
        return iter(self._index)
        
    def __str__(self):
        return self.__repr__()
        
    def __repr__(self):
        return self._index.toString()
        
    def __getitem__(self, k):
        if isinstance(k, int):
            return self.data[k]
        else:
            sidx = 0 if k.start is None else k.start
            if sidx < 0:
                sidx = self.__len__() + sidx
            eidx = self.__len__() if k.stop is None else k.stop
            if eidx < 0:
                eidx = self.__len__() + eidx                    
            step = 1 if k.step is None else k.step
            r = self._index.subIndex(sidx, eidx, step)
            return Index(index=r)
            
    def index(self, v):
        '''
        Get index of a value.
        
        :param v: (*object*) value
        
        :returns: (*int*) Value index
        '''
        return self._index.indexOf(v)

    def get_loc(self, key, outkeys=False):
        '''
        Get integer location, slice or boolean mask for requested label.
        
        :param key: (*string or list*) Label.
        :param outkeys: (*boolean*) If return location keys or not.
        
        :returns: int if unique index, slice if monotonic index, else mask.
        '''
        r = self._index.getIndices(key)
        if outkeys:            
            return list(r[0]), list(r[1])
        else:
            return list(r[0])
        
    def fill_keylist(self, rdata, rfdata):
        return self._index.fillKeyList(rdata.asarray(), rfdata)
        
    def get_format(self):
        '''
        Get value to string format.
        
        :returns: (*string*) Format string.
        '''
        return self._index.getFormat()
        
    def set_format(self, format):
        '''
        Set value to string format.
        
        :param format: (*string*) Format string.
        '''
        self._index.setFormat(format)
        
############################################
class DateTimeIndex(Index):
    
    def __init__(self, data=None, start=None, end=None, periods=None, freq='D', index=None):
        if index is None:
            if not data is None:
                if isinstance(data, MIArray):
                    data = data.aslist()
                self.data = data
                if isinstance(data[0], datetime.datetime):
                    self._index = MIDateTimeIndex(miutil.jdate(data))
                else:
                    self._index = MIDateTimeIndex(data)
            else:
                if start is None:
                    self._index = MIDateTimeIndex(periods, end, freq)
                elif end is None:
                    self._index = MIDateTimeIndex(start, periods, freq)
                else:
                    self._index = MIDateTimeIndex(start, end, freq)
                self.data = miutil.pydate(list(self._index.getDateValues()))
        else:
            self._index = index
            self.data = miutil.pydate(list(self._index.getDateValues()))
            
    def index(self, v):
        '''
        Get index of a value.
        
        :param v: (*datetime or string*) Date time value
        
        :returns: (*int*) Value index
        '''
        if isinstance(v, datetime.datetime):
            v = miutil.jdatetime(v)
        else:
            v = miutil.str2jdate(v)
        return self._index.indexOf(v)
        
    def get_loc(self, key, outkeys=False):
        '''
        Get integer location, slice or boolean mask for requested label.
        
        :param key: (*string or list*) Label.
        :param outkeys: (*boolean*) If return location keys or not.
        
        :returns: int if unique index, slice if monotonic index, else mask.
        '''
        if isinstance(key, datetime.datetime):
            key = miutil.jdatetime(key)
        elif isinstance(key, (list, tuple, MIArray)) and isinstance(key[0], datetime.datetime):
            key = miutil.jdatetime(key)
        r = self._index.getIndices(key)
        if outkeys:            
            return list(r[0]), list(r[1])
        else:
            return list(r[0])
        
#############################################
def date_range(start=None, end=None, periods=None, freq='D'):
    '''
    Create DateTimeIndex by date range.
    
    :param start: (*string or datetime*) Start date time.
    :param end: (*string or datetime*) End date time.
    :param periods: (*int*) Periods number.
    :param freq: (*string*) Date time frequent value [ Y | M | D | H | m | S ]. 
    
    :returns: (*DateTimeIndex*) DateTimeIndex
    '''
    r = DateTimeIndex(start=start, end=end, periods=periods, freq=freq)
    return r
    