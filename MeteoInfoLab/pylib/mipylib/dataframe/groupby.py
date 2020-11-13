# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2018-8-29
# Purpose: MeteoInfo groupby module
# Note: Jython
#-----------------------------------------------------

import dataframe
import series

from org.meteoinfo.data.dataframe import DataFrame as MIDataFrame


class GroupBy(object):
    
    def __init__(self, groupby):
        '''
        GroupBy
        
        :param groupby: MIGroupBy object
        '''
        self._groupby = groupby
        self.iterator = groupby.iterator()

    def __len__(self):
        return self._groupby.groupNumber()

    def __iter__(self):
        self.iterator = self._groupby.iterator()
        return self

    def next(self):
        if self.iterator.hasNext():
            v = self.iterator.next()
            return v.getKey(), dataframe.DataFrame(dataframe=v.getValue())
        else:
            raise StopIteration()

    @property
    def groups(self):
        '''
        Groups description
        :return: (*dict*) Groups description
        '''
        gs = {}
        for name, df in self:
            gs[name] = df.index
        return gs

    def get_group(self, name):
        '''
        Get a group
        :param name: The name of the group
        :return: The group
        '''
        r = self._groupby.getGroup(name)
        if isinstance(r, MIDataFrame):
            return dataframe.DataFrame(dataframe=r)
        else:
            return series.Series(series=r)
        
    def count(self):
        '''
        Compute count of groups.
        '''
        r = self._groupby.count()
        if isinstance(r, MIDataFrame):
            return dataframe.DataFrame(dataframe=r)
        else:
            return series.Series(series=r)
            
    def sum(self):
        '''
        Compute sum of groups.
        '''
        r = self._groupby.sum()
        if isinstance(r, MIDataFrame):
            return dataframe.DataFrame(dataframe=r)
        else:
            return series.Series(series=r)
            
    def mean(self):
        '''
        Compute mean of groups.
        '''
        r = self._groupby.mean()
        if isinstance(r, MIDataFrame):
            return dataframe.DataFrame(dataframe=r)
        else:
            return series.Series(series=r)
            
    def max(self):
        '''
        Compute maximum of groups.
        '''
        r = self._groupby.max()
        if isinstance(r, MIDataFrame):
            return dataframe.DataFrame(dataframe=r)
        else:
            return series.Series(series=r)
            
    def min(self):
        '''
        Compute minimum of groups.
        '''
        r = self._groupby.min()
        if isinstance(r, MIDataFrame):
            return dataframe.DataFrame(dataframe=r)
        else:
            return series.Series(series=r)
            
    def median(self):
        '''
        Compute median of groups.
        '''
        r = self._groupby.median()
        if isinstance(r, MIDataFrame):
            return dataframe.DataFrame(dataframe=r)
        else:
            return series.Series(series=r)
            
    def std(self):
        '''
        Compute standard deviation of groups.
        '''
        r = self._groupby.median()
        if isinstance(r, MIDataFrame):
            return dataframe.DataFrame(dataframe=r)
        else:
            return series.Series(series=r)
            
    def quantile(self, q):
        '''
        Return values at the given quantile.
        
        :param q: (*float*) Value between 0 <= q <= 1, the quantile(s) to compute.
        
        :returns: Series or DataFrame
        '''
        r = self._groupby.percentile(q)
        if isinstance(r, MIDataFrame):
            return dataframe.DataFrame(dataframe=r)
        else:
            return series.Series(series=r)
            
########################################################