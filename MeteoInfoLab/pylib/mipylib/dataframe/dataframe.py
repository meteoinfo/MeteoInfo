# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2017-3-7
# Purpose: MeteoInfo DataFrame module
# Note: Jython
#-----------------------------------------------------

import datetime

from org.meteoinfo.data.dataframe import DataFrame as MIDataFrame
from org.meteoinfo.data.dataframe import Series as MISeries
from org.meteoinfo.ndarray import Range, Array

import mipylib.numeric as np
import mipylib.miutil as miutil
from index import Index
import series
import groupby
from indexing import LocIndexer, ILocIndexer, AtIndexer, IAtIndexer

from java.lang import Double
nan = Double.NaN

class DataFrame(object):
    '''
    Two-dimensional size-mutable, potentially heterogeneous tabular data structure with 
    labeled axes (rows and columns). Arithmetic operations align on both row and column 
    labels. Can be thought of as a dict-like container for Series objects.
    
    :param data: (*array_like*) Two-dimensional array data or list of one-dimensional arrays.
    :param index: (*list*) Data index list. Values must be unique and hashable, same length as data.
    :param columns: (*list*) Column labels to use for resulting frame. Will default to 
        arange(n) if no column labels are provided
    '''   
    def __init__(self, data=None, index=None, columns=None, dataframe=None):                             
        if dataframe is None:
            if not data is None:
                if isinstance(data, dict):
                    columns = data.keys()
                    dlist = []
                    n = 1
                    for v in data.values():
                        if isinstance(v, (list, tuple)):
                            n = len(v)
                            v = np.array(v)                    
                        elif isinstance(v, np.NDArray):
                            n = len(v)
                        dlist.append(v)
                    for i in range(len(dlist)):
                        d = dlist[i]
                        if not isinstance(d, np.NDArray):
                            d = [d] * n
                            d = np.array(d)
                            dlist[i] = d
                    data = dlist
                    
                if isinstance(data, np.NDArray):
                    n = len(data)
                    data = data._array
                else:
                    dlist = []
                    n = len(data[0])
                    for dd in data:
                        dlist.append(dd._array)
                    data = dlist
                        
                if index is None:
                    index = range(0, n)
                else:
                    if n != len(index):
                        raise ValueError('Wrong length of index!')
                        
            if isinstance(index, np.NDArray):
                index = index.tolist()
                
            if isinstance(index, Index):
                self._index = index
            else:
                self._index = Index.factory(index)
            if data is None:
                self._dataframe = MIDataFrame(self._index._index)
            else:
                self._dataframe = MIDataFrame(data, self._index._index, columns)
        else:
            self._dataframe = dataframe
            self._index = Index.factory(index=self._dataframe.getIndex())
        
    #---- index property
    def get_index(self):
        return self._index
        
    def set_index(self, value):
        if isinstance(value, series.Series):
            value = value.values
        self._index = Index.factory(value, self._index.name)
        self._dataframe.setIndex(self._index._index)
        
    index = property(get_index, set_index)
    
    #---- data property
    def get_data(self):
        r = self._dataframe.getData()
        if isinstance(r, Array):
            r = np.array(r)
        else:
            rr = []
            for d in r:
                rr.append(np.array(d))
            r = rr
        return r
        
    def set_data(self, value):
        value = np.array(value)
        self._dataframe.setData(value._array)
        
    values = property(get_data, set_data)
    
    #---- columns property
    def get_columns(self):
        return self._dataframe.getColumns()
        
    def set_columns(self, value):
        self._dataframe.setColumns(value)
        
    columns = property(get_columns, set_columns)
    
    #---- shape property
    def get_shape(self):
        s = self._dataframe.getShape()
        s1 = []
        for i in range(len(s)):
            s1.append(s[i])
        return tuple(s1)
        
    shape = property(get_shape)
    
    #---- dtypes property
    def get_dtypes(self):
        colnames = list(self.columns.getNames())
        datatypes = list(self.columns.getDataTypes())
        r = series.Series(datatypes, colnames, 'DataTypes')
        return r
        
    dtypes = property(get_dtypes)
    
    @property
    def loc(self):
        '''
        Access a group of rows and columns by label(s) or a boolean array.
        '''
        return LocIndexer(self)
        
    @property
    def iloc(self):
        '''
        Purely integer-location based indexing for selection by position.
        '''
        return ILocIndexer(self)
        
    @property
    def at(self):
        '''
        Access a single value for a row/column label pair.
        '''
        return AtIndexer(self)
       
    @property
    def iat(self):
        '''
        Access a single value for a row/column pair by integer position.
        '''
        return IAtIndexer(self)
        
    def __getitem__(self, key):
        if isinstance(key, basestring):
            data = self._dataframe.getColumnData(key)
            if data is None:
                return data
            idx = self._index[:]
            r = series.Series(np.array(data), idx, key)
            return r
            
        hascolkey = True
        if isinstance(key, tuple): 
            ridx = key[0]
            cidx = key[1]
            if isinstance(ridx, int) and isinstance(cidx, int):
                if ridx < 0:
                    ridx = self.shape[0] + ridx
                if cidx < 0:
                    cidx = self.shape[1] + cidx
                return self._dataframe.getValue(ridx, cidx)
            elif isinstance(ridx, int) and isinstance(cidx, basestring):
                if ridx < 0:
                    ridx = self.shape[0] + ridx
                return self._dataframe.getValue(ridx, cidx)
        else:
            key = (key, slice(None))
            hascolkey = False
            
        k = key[0]
        if isinstance(k, Index):
            k = k.data
        if isinstance(k, int):
            if k < 0:
                k = self.shape[0] + k
            rowkey = k
        elif isinstance(k, basestring):
            sidx = self._index.index(k)
            if sidx < 0:
                return None
            eidx = sidx
            step = 1
            rowkey = Range(sidx, eidx, step)
        elif isinstance(k, slice):
            if isinstance(k.start, basestring):
                sidx = self._index.index(k.start)
                if sidx < 0:
                    sidx = 0
            else:
                sidx = 0 if k.start is None else k.start
                if sidx < 0:
                    sidx = self.shape[0] + sidx
            if isinstance(k.stop, basestring):
                eidx = self._index.index(k.stop)
                if eidx < 0:
                    eidx = self.shape[0] + eidx
            else:
                eidx = self.shape[0] - 1 if k.stop is None else k.stop - 1
                if eidx < 0:
                    eidx = self.shape[0] + eidx                    
            step = 1 if k.step is None else k.step
            rowkey = Range(sidx, eidx, step)
        elif isinstance(k, (list,tuple,np.NDArray,series.Series)):
            if isinstance(k[0], (int, bool)):
                if isinstance(k, (list, tuple)):
                    rowkey = k
                else:
                    rowkey = k.asarray()
            else:
                tlist = []
                for tstr in k:
                    idx = self._index.index(tstr)
                    if idx >= 0:
                        tlist.append(idx)
                rowkey = tlist
        else:
            rowkey = self._index.get_loc(k)
                   
        if not hascolkey:
            colkey = Range(0, self.shape[1] - 1, 1)
        else:
            k = key[1]
            if isinstance(k, int):
                sidx = k
                if sidx < 0:
                    sidx = self.shape[1] + sidx
                eidx = sidx
                step = 1
                colkey = Range(sidx, eidx, step)
            elif isinstance(k, slice):
                sidx = 0 if k.start is None else k.start
                if sidx < 0:
                    sidx = self.shape[1] + sidx
                eidx = self.shape[1] - 1 if k.stop is None else k.stop - 1
                if eidx < 0:
                    eidx = self.shape[1] + eidx                    
                step = 1 if k.step is None else k.step
                colkey = Range(sidx, eidx, step)        
            elif isinstance(k, list):
                if isinstance(k[0], int):
                    colkey = k
                else:
                    colkey = self.columns.indexOfName(k)               
            elif isinstance(k, basestring):
                col = self.columns.indexOf(k)
                colkey = Range(col, col + 1, 1)
            else:
                return None
        
        r = self._dataframe.select(rowkey, colkey)
        if r is None:
            return None
        if isinstance(r, MISeries):
            r = series.Series(series=r)
        else:
            r = DataFrame(dataframe=r)
        return r
        
    def __setitem__(self, key, value):
        if isinstance(value, datetime.datetime):
            value = miutil.jdatetime(value)
        if isinstance(value, (list, tuple)):
            if isinstance(value[0], datetime.datetime):
                value = miutil.jdatetime(value)
            value = np.array(value)
        if isinstance(value, np.NDArray):
            value = value._array            
            
        if isinstance(key, basestring):
            if isinstance(value, series.Series):
                value = value.values._array
            self._dataframe.setColumn(key, value)
            return
            
        hascolkey = True
        if isinstance(key, tuple): 
            ridx = key[0]
            cidx = key[1]
            if isinstance(ridx, int) and isinstance(cidx, int):
                if ridx < 0:
                    ridx = self.shape[0] + ridx
                if cidx < 0:
                    cidx = self.shape[1] + cidx
                self._dataframe.setValue(ridx, cidx, value)
                return
            elif isinstance(ridx, int) and isinstance(cidx, basestring):
                if ridx < 0:
                    ridx = self.shape[0] + ridx
                self._dataframe.setValue(ridx, cidx, value)
                return
        else:
            key = (key, slice(None))
            hascolkey = False
            
        k = key[0]
        if isinstance(k, int):
            if k < 0:
                k = self.shape[0] + k
            rowkey = k
        elif isinstance(k, basestring):
            sidx = self._index.index(k)
            if sidx < 0:
                return None
            eidx = sidx
            step = 1
            rowkey = Range(sidx, eidx, step)
        elif isinstance(k, slice):
            if isinstance(k.start, basestring):
                sidx = self._index.index(k.start)
                if sidx < 0:
                    sidx = 0
            else:
                sidx = 0 if k.start is None else k.start
                if sidx < 0:
                    sidx = self.shape[0] + sidx
            if isinstance(k.stop, basestring):
                eidx = self._index.index(k.stop)
                if eidx < 0:
                    eidx = self.shape[0] + eidx
            else:
                eidx = self.shape[0] - 1 if k.stop is None else k.stop - 1
                if eidx < 0:
                    eidx = self.shape[0] + eidx                    
            step = 1 if k.step is None else k.step
            rowkey = Range(sidx, eidx, step)
        elif isinstance(k, list):
            if isinstance(k[0], int):
                rowkey = k
            else:
                tlist = []
                for tstr in k:
                    idx = self._index.index(tstr)
                    if idx >= 0:
                        tlist.append(idx)
                rowkey = tlist
        else:
            return
            
        if not hascolkey:
            colkey = Range(0, self.shape[1] - 1, 1)
        else:
            k = key[1]
            if isinstance(k, int):
                sidx = k
                if sidx < 0:
                    sidx = self.shape[1] + sidx
                eidx = sidx
                step = 1
                colkey = Range(sidx, eidx, step)
            elif isinstance(k, slice):
                sidx = 0 if k.start is None else k.start
                if sidx < 0:
                    sidx = self.shape[1] + sidx
                eidx = self.shape[1] - 1 if k.stop is None else k.stop - 1
                if eidx < 0:
                    eidx = self.shape[1] + eidx                    
                step = 1 if k.step is None else k.step
                colkey = Range(sidx, eidx, step)        
            elif isinstance(k, list):
                if isinstance(k[0], int):
                    colkey = k
                else:
                    colkey = self.columns.indexOfName(k)               
            elif isinstance(k, basestring):
                col = self.columns.indexOf(k)
                colkey = Range(col, col + 1, 1)
            else:
                return
        
        self._dataframe.setValues(rowkey, colkey, value)
        
    def _getitem_loc(self, key):   
        if not isinstance(key, tuple): 
            key = (key, None)
      
        k = key[0]
        rkeys = key[0]
        if isinstance(k, slice):
            sidx = 0 if k.start is None else self._index.index(k.start)
            if sidx < 0:
                raise KeyError(key)
            eidx = self.shape[0] - 1 if k.stop is None else self._index.index(k.stop)
            if eidx < 0:
                raise KeyError(key)                   
            step = 1 if k.step is None else k.step
            rowkey = Range(sidx, eidx, step)
        else:
            rloc = self._index.get_loc(k, outkeys=True)
            if isinstance(rloc, tuple):
                rowkey = rloc[0]
                rkeys = rloc[1]
            else:
                rowkey = rloc
                rkeys = None
            if len(rowkey) == 0:
                raise KeyError(key)
                   
        k = key[1]
        if k is None:
            colkey = Range(0, self.shape[1] - 1, 1)
        else:
            if isinstance(k, slice):
                sidx = 0 if k.start is None else self.columns.indexOfName(k.start)
                if sidx < 0:
                    raise KeyError(key)
                eidx = self.shape[1] - 1 if k.stop is None else self.columns.indexOfName(k.stop)
                if eidx < 0:
                    raise KeyError(key)                  
                step = 1 if k.step is None else k.step
                colkey = Range(sidx, eidx, step)        
            elif isinstance(k, list):
                colkey = self.columns.indexOfName(k)               
            elif isinstance(k, basestring):
                col = self.columns.indexOfName(k)
                if col < 0:
                    raise KeyError(key)
                colkey = [col]
            else:
                return None
                
        if isinstance(rowkey, (int, Range)):
            r = self._dataframe.select(rowkey, colkey)
        else:
            if isinstance(colkey, Range):
                ncol = colkey.length()
            else:
                ncol = len(colkey)
            if len(rowkey) == 1 and ncol == 1:
                if isinstance(colkey, Range):
                    return self._dataframe.getValue(rowkey[0], colkey.first())
                else:
                    return self._dataframe.getValue(rowkey[0], colkey[0])
            if rkeys is None:
                r = self._dataframe.select(rowkey, colkey)
            else:
                if not isinstance(rkeys, list):
                    rkeys = [rkeys]            
                r = self._dataframe.select(rkeys, rowkey, colkey)
        if r is None:
            return None
        if isinstance(r, MISeries):
            r = series.Series(series=r)
        else:
            r = DataFrame(dataframe=r)
        return r
        
    def _setitem_loc(self, key, value):
        if isinstance(value, datetime.datetime):
            value = [miutil.jdatetime(value)]
        if isinstance(value, (list, tuple)):
            if isinstance(value[0], datetime.datetime):
                value = miutil.jdatetime(value)
            #value = np.array(value)
        if isinstance(value, np.NDArray):
            value = value._array            
                    
        self._dataframe.setRow(key, value)
        
    def _getitem_iloc(self, key):
        if not isinstance(key, tuple): 
            key = (key, None)
    
        if isinstance(key[0], int) and isinstance(key[1], int):
            return self._dataframe.getValue(key[0], key[1])
    
        k = key[0]
        if isinstance(k, int):
            if k < 0:
                k = self.shape[0] + k
            rowkey = k
        elif isinstance(k, slice):
            sidx = 0 if k.start is None else k.start
            if sidx < 0:
                sidx = self.shape[0] + sidx
            eidx = self.shape[0] - 1 if k.stop is None else k.stop - 1
            if eidx < 0:
                eidx = self.shape[0] + eidx                    
            step = 1 if k.step is None else k.step
            rowkey = Range(sidx, eidx, step)
        elif isinstance(k, list):
            rowkey = k
        elif isinstance(k, np.NDArray):
            rowkey = k.aslist()
        else:
            return None
                   
        k = key[1]
        if k is None:
            colkey = Range(0, self.shape[1] - 1, 1)
        else:
            if isinstance(k, int):
                sidx = k
                if sidx < 0:
                    sidx = self.shape[1] + sidx
                eidx = sidx
                step = 1
                colkey = Range(sidx, eidx, step)
            elif isinstance(k, slice):
                sidx = 0 if k.start is None else k.start
                if sidx < 0:
                    sidx = self.shape[1] + sidx
                eidx = self.shape[1] - 1 if k.stop is None else k.stop - 1
                if eidx < 0:
                    eidx = self.shape[1] + eidx                    
                step = 1 if k.step is None else k.step
                colkey = Range(sidx, eidx, step)        
            elif isinstance(k, list):
                colkey = k
            elif isinstance(k, np.NDArray):
                colkey = k.aslist()
            else:
                return None
        
        r = self._dataframe.select(rowkey, colkey)
        if r is None:
            return None
        if isinstance(r, MISeries):
            r = series.Series(series=r)
        else:
            r = DataFrame(dataframe=r)
        return r     

    def _getitem_at(self, key):
        ridx = key[0]
        cidx = key[1]
        ridx = self._index.index(ridx)        
        if ridx < 0:
            raise KeyError(key)
        cidx = self.columns.indexOfName(cidx)
        if cidx < 0:
            raise KeyError(key)
        return self._dataframe.getValue(ridx, cidx)
            
    def _getitem_iat(self, key):
        ridx = key[0]
        cidx = key[1]
        if ridx < 0:
            ridx = self.shape[0] + ridx
        if cidx < 0:
            cidx = self.shape[1] + cidx
        return self._dataframe.getValue(ridx, cidx)
    
    def __getkey(self, key):
        if isinstance(key, basestring):
            rkey = self.index.get_indices(key)
            ikey = rkey[0]
            rindex = rkey[1]
            if len(ikey) == 1:
                ikey = ikey[0]
            elif len(ikey) > 1:
                ikey = list(ikey)
            else:
                raise KeyError(key)
            return ikey, rindex
        elif isinstance(key, (list, tuple, np.NDArray)) and isinstance(key[0], basestring):
            if isinstance(key, (np.NDArray)):
                key = key.asarray()            
            rkey = self.index.get_indices(key)
            ikey = rkey[0]
            rindex = rkey[1]
            rdata = rkey[2]
            rrindex = rkey[3]
            if len(ikey) == 0:
                raise KeyError()
            else:
                ikey = list(ikey)
            return ikey, rindex, rdata, rrindex
        else:
            return key, None
        
    def __iter__(self):
        """
        provide iteration over the values of the Series
        """
        #return iter(self.data)
        #return zip(iter(self.index), iter(self.data))
        return iter(self.index)
        
    def iteritems(self):
        """
        Lazily iterate over (index, value) tuples
        """
        return zip(iter(self.index), iter(self))
        
    def __len__(self):
        return self.shape[0]
        
    def __str__(self):
        return self._dataframe.toString() 
        
    def __repr__(self):
        return self._dataframe.toString()    
        
    def head(self, n=5):
        '''
        Get top rows
        
        :param n: (*int*) row number.
        
        :returns: Top rows
        '''
        print self._dataframe.head(n)
        
    def tail(self, n=5):
        '''
        Get bottom rows
        
        :param n: (*int*) row number.
        
        :returns: Bottom rows
        '''
        print self._dataframe.tail(n)
        
    def transpose(self):
        '''
        Transpose data frame.
        
        :returns: Transposed data frame.
        '''        
        r = self._dataframe.transpose()
        return DataFrame(dataframe=r)
        
    T = property(transpose)
    
    def insert(self, loc, column, value):
        '''
        Insert column into DataFrame at specified location.
        
        :param loc: (*int*) Insertation index.
        :param column: (*string*) Label of inserted column.
        :param value: (*array_like*) Column values.
        '''
        if isinstance(value, datetime.datetime):
            value = miutil.jdatetime(value)
        if isinstance(value, (list, tuple)):
            if isinstance(value[0], datetime.datetime):
                value = miutil.jdatetime(value)
            value = np.array(value)
        if isinstance(value, Index):
            if isinstance(value[0], datetime.datetime):
                value = miutil.jdatetime(value.data)
            else:
                value = value.data
            value = np.array(value)
        if isinstance(value, np.NDArray):
            value = value._array 
        self._dataframe.addColumn(loc, column, value)
        
    def drop(self, columns=None):
        '''
        Drop specified labels from rows or columns.
        
        :param columns: (*list like*) Column labels.
        '''
        if isinstance(columns, basestring):
            columns = [columns]
        r = self._dataframe.drop(columns)
        return DataFrame(dataframe=r)
    
    def append(self, other):
        '''
        Append another data frame.
        
        :param other: (*DataFrame, dict, list*) Other data frame or row data.
        
        :returns: (*DataFrame*) Appended data frame.
        '''
        if isinstance(other, DataFrame):
            r = self._dataframe.append(other._dataframe)
            return DataFrame(dataframe=r)
        else:
            self._dataframe.append(other)
            return self
        
    def describe(self):
        '''
        Generates descriptive statistics that summarize the central tendency, dispersion and shape of a 
        dataset’s distribution, excluding NaN values.
        
        :returns: Describe DataFrame.
        '''
        r = self._dataframe.describe()
        return DataFrame(dataframe=r)
        
    def sort_index(self, axis=0, ascending=True):
        '''
        Sort by the index along either axis

        :param axis: (*int*) Axis to be sorted {0 or ‘index’, 1 or ‘columns’}, default 0
        :param ascending: (*boolean*) Sort ascending vs. descending. 
            
        :returns: Sorted DataFrame
        '''
        df = self._dataframe.sortByIndex(ascending)
        return DataFrame(dataframe=df)
        
    def sort_values(self, by, axis=0, ascending=True):
        '''
        Sort by the values along either axis
        
        :param by: (*string or list of string*) Name or list of names to sort by.
        :param axis: (*int*) Axis to be sorted {0 or ‘index’, 1 or ‘columns’}, default 0
        :param ascending: (*boolean*) Sort ascending vs. descending. Specify list for multiple sort orders. 
            If this is a list of bools, must match the length of the by.
            
        :returns: Sorted DataFrame
        '''
        if isinstance(by, basestring):
            by = [by]
        if isinstance(ascending, bool):
            ascending = [ascending] * len(by)
        df = self._dataframe.sortBy(by, ascending)
        return DataFrame(dataframe=df)
    
    def groupby(self, by):
        '''
        Group DataFrame.
        
        :param by: Period string.
        
        :returns: GroupBy object.
        '''
        if isinstance(by, basestring):
            by = [by]
        gb = self._dataframe.groupBy(by)
        return groupby.GroupBy(gb)
        
    def resample(self, by):
        '''
        Group DataFrame by date time index.
        
        :param by: Used to determine the groups for the groupby.
        
        :returns: GroupBy object
        '''
        gb = self._dataframe.groupByIndex(by)
        return groupby.GroupBy(gb)
        
    def count(self):
        '''
        Return the count of the values for the requested axis
        '''
        return DataFrame(dataframe=self._dataframe.count())
        
    def sum(self):
        '''
        Return the sum of the values for the requested axis
        '''
        return DataFrame(dataframe=self._dataframe.sum())
        
    def mean(self):
        '''
        Return the mean of the values for the requested axis
        '''
        return DataFrame(dataframe=self._dataframe.mean())
        
    def min(self):
        '''
        Return the minimum of the values for the requested axis
        '''
        return DataFrame(dataframe=self._dataframe.min())
        
    def max(self):
        '''
        Return the maximum of the values for the requested axis
        '''
        return DataFrame(dataframe=self._dataframe.max())
        
    def median(self):
        '''
        Return the median of the values for the requested axis
        '''
        return DataFrame(dataframe=self._dataframe.median())
        
    def std(self):
        '''
        Return the standard deviation of the values for the requested axis
        '''
        return DataFrame(dataframe=self._dataframe.stdDev())
    
    @classmethod
    def read_table(cls, filepath, **kwargs):
        '''
        Create DataFrame by reading column oriented data from a file.
    
        :param filepath: (*string*) File path for reading.
        :param delimiter: (*string*) Variable delimiter character. Default is ``None``, means space or tab 
            delimiter.
        :param format: (*string*) Colomn format of the file. Default is ``None``, means all columns were
            read as string variable. ``%s``: string; ``%i``: integer; ``%f``: float; ``%{yyyyMMdd...}D``: 
            date time.           
        :param skiprows: (*int*) Lines to skip at beginning of the file. Default is ``0``.
        :param skipfooter: (*int*) Number of lines at bottom of file to skip.
        :param encoding: (*string*) Character encoding scheme associated with the file. Default is ``UTF8``.
        :param names: (*array_like*) List of column names to use. If file contains no header row, then you should 
            explicitly pass header=None. Default is None.
        :param header: (*int*) Row number to use as the column names. If column names are passed explicitly 
            then the behavior is identical to ``header=None``. 
        :param index_col: (*int*) Column to use as the row labels (index) of the DataFrame.
        :param index_format: (*string*) Index column format.
        :param usecols: (*list*) Return a subset of the columns. If list-like, all elements 
            must either be positional (i.e. integer indices into the document columns) or 
            strings that correspond to column names provided either by the user in names or 
            inferred from the document header row(s).
            
        :returns: (*DataFrame*) The DataFrame.
        '''
        delimiter = kwargs.pop('delimiter', None)
        format = kwargs.pop('format', None)
        skiprows = kwargs.pop('skiprows', 0)
        skipfooter = kwargs.pop('skipfooter', 0)
        encoding = kwargs.pop('encoding', 'UTF8')
        names = kwargs.pop('names', None)
        header = kwargs.pop('header', 0)
        index_col = kwargs.pop('index_col', -1)
        index_format = kwargs.pop('index_format', None)
        usecols = kwargs.pop('usecols', None)
        if usecols is None:
            midf = MIDataFrame.readTable(filepath, delimiter, skiprows, format, encoding,
                index_col, index_format, names, header, skipfooter)
        else:
            midf = MIDataFrame.readTable(filepath, delimiter, skiprows, format, encoding,
                index_col, index_format, names, header, skipfooter, usecols)
        return DataFrame(dataframe=midf)
        
    def to_csv(self, filepath, delimiter=',', format=None, date_format=None, \
        float_format=None, index=True):
        '''
        Save the data to an csv file.
        
        :param filename: (*string*) The file name.
        :param delimiter: (*string*) Field delimiter character. Default is ``,``.
        :param format: (*string*) Format string.
        :param date_format: (*string*) Date format string. i.e. 'yyyyMMddHH'.
        :param float_format: (*string*) Float format string. i.e. '%.2f'.
        :param index: (*boolean*) Write index or not.
        '''
        self._dataframe.saveCSV(filepath, delimiter, date_format, float_format, index)

#################################################################