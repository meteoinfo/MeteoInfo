# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2018-8-29
# Purpose: MeteoInfo groupby module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.data.dataframe import DataFrame as MIDataFrame
from org.meteoinfo.data.dataframe import Series as MISeries

import dataframe
import series

class GroupBy(object):
    
    def __init__(self, groupby):
        '''
        GroupBy
        
        :param groupby: MIGroupBy object
        '''
        self._groupby = groupby
        
    def count(self):
        '''
        Compute count of group.
        '''
        r = self._groupby.count()
        if isinstance(r, MIDataFrame):
            return dataframe.DataFrame(dataframe=r)
        else:
            return series.Series(series=r)
            
    def sum(self):
        '''
        Compute sum of group.
        '''
        r = self._groupby.sum()
        if isinstance(r, MIDataFrame):
            return dataframe.DataFrame(dataframe=r)
        else:
            return series.Series(series=r)
            
    def mean(self):
        '''
        Compute mean of group.
        '''
        r = self._groupby.mean()
        if isinstance(r, MIDataFrame):
            return dataframe.DataFrame(dataframe=r)
        else:
            return series.Series(series=r)
            
    def max(self):
        '''
        Compute maximum of group.
        '''
        r = self._groupby.max()
        if isinstance(r, MIDataFrame):
            return dataframe.DataFrame(dataframe=r)
        else:
            return series.Series(series=r)
            
    def min(self):
        '''
        Compute minimum of group.
        '''
        r = self._groupby.min()
        if isinstance(r, MIDataFrame):
            return dataframe.DataFrame(dataframe=r)
        else:
            return series.Series(series=r)
            
    def median(self):
        '''
        Compute median of group.
        '''
        r = self._groupby.median()
        if isinstance(r, MIDataFrame):
            return dataframe.DataFrame(dataframe=r)
        else:
            return series.Series(series=r)
            
########################################################