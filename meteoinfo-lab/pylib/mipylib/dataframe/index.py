#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2018-7-18
# Purpose: MeteoInfo index module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.dataframe import Index as MIIndex
from org.meteoinfo.dataframe import DateTimeIndex as MIDateTimeIndex
from java.time import LocalDateTime

import datetime
import numbers

import mipylib.numeric as np
import mipylib.miutil as miutil

class Index(object):
    
    @staticmethod
    def factory(data=None, name='Index', index=None):
        """
        Factory method
        """
        if index is None:
            if isinstance(data[0], (LocalDateTime, datetime.datetime)):
                return DateTimeIndex(data, name)
            else:
                return Index(data, name)
        else:
            if isinstance(index, MIDateTimeIndex):
                return DateTimeIndex(index=index)
            else:
                return Index(index=index)
    
    def __init__(self, data=None, name='Index', index=None):
        """
        Index 
        
        :param data: (*array_like*) Index values
        :param name: (*string*) Index name                
        """
        if index is None:
            if isinstance(data, np.NDArray):
                data = data.aslist()
            self.data = data
            self._index = MIIndex.factory(data)
        else:
            self._index = index
            self.data = list(self._index.getData())
        self._index.setName(name)

    @property
    def name(self):
        return self._index.getName()

    @name.setter
    def name(self, value):
        self._index.setName(value)
        
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
            return Index.factory(index=r)
            
    def __eq__(self, other):
        if isinstance(other, numbers.Number):
            return np.NDArray(self._index.equal(other))
        else:
            return False
            
    def index(self, v):
        """
        Get index of a value.
        
        :param v: (*object*) value
        
        :returns: (*int*) Value index
        """
        return self._index.indexOf(v)

    def get_loc(self, key, outkeys=False):
        """
        Get integer location, slice or boolean mask for requested label.
        
        :param key: (*string or list*) Label.
        :param outkeys: (*boolean*) If return location keys or not.
        
        :returns: int if unique index, slice if monotonic index, else mask.
        """
        if isinstance(key, np.NDArray) and key.dtype == np.dtype.bool:
            r = self._index.filterIndices(key.asarray())
            return list(r)
        else:
            if isinstance(key, np.NDArray):
                r = self._index.getIndices(key.asarray())
            else:
                r = self._index.getIndices(key)
            if outkeys:            
                return list(r[0]), list(r[1])
            else:
                return list(r[0])
        
    def fill_keylist(self, rdata, rfdata):
        return self._index.fillKeyList(rdata.asarray(), rfdata)
        
    def get_format(self):
        """
        Get value to string format.
        
        :returns: (*string*) Format string.
        """
        return self._index.getFormat()
        
    def set_format(self, format):
        """
        Set value to string format.
        
        :param format: (*string*) Format string.
        """
        self._index.setFormat(format)
        
############################################
class DateTimeIndex(Index):
    
    def __init__(self, data=None, name='Index', start=None, end=None, periods=None, freq='D', index=None):
        if index is None:
            if not data is None:
                if isinstance(data, np.NDArray):
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
                self.data = miutil.pydate(list(self._index.getData()))
        else:
            self._index = index
            self.data = miutil.pydate(list(self._index.getData()))
        self._index.setName(name)
            
    def index(self, v):
        """
        Get index of a value.
        
        :param v: (*datetime or string*) Date time value
        
        :returns: (*int*) Value index
        """
        if isinstance(v, datetime.datetime):
            v = miutil.jdatetime(v)
        else:
            v = miutil.str2jdate(v)
        return self._index.indexOf(v)
        
    def get_loc(self, key, outkeys=False):
        """
        Get integer location, slice or boolean mask for requested label.
        
        :param key: (*string or list*) Label.
        :param outkeys: (*boolean*) If return location keys or not.
        
        :returns: int if unique index, slice if monotonic index, else mask.
        """
        if isinstance(key, np.NDArray) and key.dtype.kind == 'b':
            r = self._index.filterIndices(key.asarray())
            return list(r)
        elif isinstance(key, datetime.datetime):
            key = miutil.jdatetime(key)
        elif isinstance(key, (list, tuple, np.NDArray)) and isinstance(key[0], datetime.datetime):
            key = miutil.jdatetime(key)
        r = self._index.getIndices(key)
        if outkeys:            
            return list(r[0]), list(r[1])
        else:
            return list(r[0])
            
    @property
    def year(self):
        """
        Get year index.
        """
        r = self._index.getYear()
        return Index(index=r)
    
    @property
    def month(self):
        """
        Get month index.
        """
        r = self._index.getMonth()
        return Index(index=r)
        
    @property
    def day(self):
        """
        Get day index.
        """
        r = self._index.getDay()
        return Index(index=r)
        
    @property
    def hour(self):
        """
        Get hour index.
        """
        r = self._index.getHour()
        return Index(index=r)
        
    @property
    def minute(self):
        """
        Get minute index.
        """
        r = self._index.getMinute()
        return Index(index=r)
        
    @property
    def second(self):
        """
        Get second index.
        """
        r = self._index.getSecond()
        return Index(index=r)

        
#############################################
def date_range(start=None, end=None, periods=None, freq='D'):
    """
    Create DateTimeIndex by date range.
    
    :param start: (*string or datetime*) Start date time.
    :param end: (*string or datetime*) End date time.
    :param periods: (*int*) Periods number.
    :param freq: (*string*) Date time frequent value [ Y | M | D | H | m | S ]. 
    
    :returns: (*DateTimeIndex*) DateTimeIndex
    """
    r = DateTimeIndex(start=start, end=end, periods=periods, freq=freq)
    return r
    