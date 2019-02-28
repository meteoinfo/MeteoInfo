#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2014-12-27
# Purpose: MeteoInfo table module
# Note: Jython
#-----------------------------------------------------

import datetime

from org.meteoinfo.data import TableData, TimeTableData, ArrayUtil, TableUtil, DataTypes
from ucar.ma2 import Range

from miarray import MIArray
import mipylib.miutil as miutil

from java.util import Calendar, Date

###############################################################        
#  The encapsulate class of TableData
class PyTableData(object):
    # Must be a TableData object
    def __init__(self, data=None):
        self.data = data
        if data is None:
            self.data = TableData()
        self.timedata = isinstance(data, TimeTableData)
        self.columns = self.data.getColumnNames()
        self.shape = (self.data.getRowCount(), self.data.getColumnCount())
        
    def __getitem__(self, key):
        if isinstance(key, basestring):     
            coldata = self.data.getColumnData(key)
            if coldata.getDataType().isNumeric():
                return MIArray(ArrayUtil.array(coldata.getDataValues()))
            elif coldata.getDataType() == DataTypes.Date:
                vv = coldata.getData()
                r = []
                cal = Calendar.getInstance()
                for v in vv:
                    cal.setTime(v)
                    year = cal.get(Calendar.YEAR)
                    month = cal.get(Calendar.MONTH) + 1
                    day = cal.get(Calendar.DAY_OF_MONTH)
                    hour = cal.get(Calendar.HOUR_OF_DAY)
                    minute = cal.get(Calendar.MINUTE)
                    second = cal.get(Calendar.SECOND)
                    dt = datetime.datetime(year, month, day, hour, minute, second)
                    r.append(dt)
                return r
            else:
                return MIArray(ArrayUtil.array(coldata.getData()))
                        
        hascolkey = True
        if isinstance(key, tuple): 
            ridx = key[0]
            cidx = key[1]
            if isinstance(ridx, int) and isinstance(cidx, int):
                if ridx < 0:
                    ridx = self.shape[0] + ridx
                if cidx < 0:
                    cidx = self.shape[1] + cidx
                return self.data.getValue(ridx, cidx)
            elif isinstance(ridx, int) and isinstance(cidx, basestring):
                if ridx < 0:
                    ridx = self.shape[0] + ridx
                return self.data.getValue(ridx, cidx)
        else:
            key = (key, slice(None))
            hascolkey = False
            
        k = key[0]
        if isinstance(k, int):
            sidx = k
            if sidx < 0:
                sidx = self.shape[0] + sidx
            eidx = sidx + 1
            step = 1
            rowkey = Range(sidx, eidx, step)
        elif isinstance(k, slice):
            if isinstance(k.start, basestring):
                t = miutil.str2date(k.start)
                t = miutil.jdate(t)
                sidx = self.data.getTimeIndex(t)
                if sidx < 0:
                    sidx = 0
            else:
                sidx = 0 if k.start is None else k.start
                if sidx < 0:
                    sidx = self.shape[0] + sidx
            if isinstance(k.stop, basestring):
                t = miutil.str2date(k.stop)
                t = miutil.jdate(t)
                eidx = self.data.getTimeIndex(t) + 1
                if eidx < 0:
                    eidx = self.shape[0]
            else:
                eidx = self.shape[0] if k.stop is None else k.stop
                if eidx < 0:
                    eidx = self.shape[0] + eidx                    
            step = 1 if k.step is None else k.step
            rowkey = Range(sidx, eidx, step)
        elif isinstance(k, list):
            if isinstance(k[0], basestring):
                tlist = []
                for tstr in k:
                    t = miutil.jdate(miutil.str2date(tstr))
                    idx = self.data.getTimeIndex_Ex(t)
                    if idx >= 0:
                        tlist.append(idx)
                rowkey = tlist
            else:
                rowkey = k
        else:
            return None
                   
        tcolname = self.data.getTimeColName()
        if not hascolkey:
            r = self.data.select(rowkey)
            if r.findColumn(tcolname) is None:
                r = TableData(r)
            else:
                r = TimeTableData(r, tcolname)
            return PyTableData(r)
            
        k = key[1]
        if isinstance(k, int):
            sidx = k
            if sidx < 0:
                sidx = self.shape[1] + sidx
            eidx = sidx + 1
            step = 1
            colkey = Range(sidx, eidx, step)
        elif isinstance(k, slice):
            sidx = 0 if k.start is None else k.start
            if sidx < 0:
                sidx = self.shape[1] + sidx
            eidx = self.shape[1] if k.stop is None else k.stop
            if eidx < 0:
                eidx = self.shape[1] + eidx                    
            step = 1 if k.step is None else k.step
            colkey = Range(sidx, eidx, step)        
        elif isinstance(k, list):
            if isinstance(k[0], basestring):
                cols = self.data.findColumns(k)
            else:
                cols = self.data.findColumns_Index(k)
            colkey = cols
        elif isinstance(k, basestring):
            rows = self.data.getRows(rowkey)
            coldata = self.data.getColumnData(rows, k)
            if coldata.getDataType().isNumeric():
                return MIArray(ArrayUtil.array(coldata.getDataValues()))
            else:
                return MIArray(ArrayUtil.array(coldata.getData()))
        else:
            return None
        
        r = self.data.select(rowkey, colkey)
        if r.findColumn(tcolname) is None:
            r = TableData(r)
        else:
            r = TimeTableData(r, tcolname)
        return PyTableData(r)
        
    def __setitem__(self, key, value):
        if isinstance(value, MIArray):
            self.data.setColumnData(key, value.aslist())
        else:
            self.data.setColumnData(key, value)
            
    def __repr__(self):
        return self.data.toString()
        
    @property
    def loc(self):
        return 'loc'
    
    def head(self, n=5):
        '''
        Get top rows
        
        :param n: (*int*) row number.
        
        :returns: Top rows
        '''
        print self.data.head(n)
        
    def tail(self, n=5):
        '''
        Get bottom rows
        
        :param n: (*int*) row number.
        
        :returns: Bottom rows
        '''
        print self.data.tail(n)
    
    def rownum(self):
        '''
        Returns the row number.
        '''
        return self.data.getRowCount()
        
    def colnum(self):
        '''
        Returns the column number.
        '''
        return self.data.getColumnCount()
    
    def colnames(self):
        '''
        Returns the column names.
        '''
        return self.data.getColumnNames()
        
    def setcolname(self, col, colname):
        '''
        Set column name to a specified column.
        
        :param col: (*int or string*) Column index or column name.
        :param colname: (*string*) New column name.
        '''
        self.data.renameColumn(col, colname)
        self.columns = self.data.getColumnNames()
        
    def setcolnames(self, colnames):
        '''
        Set column names to all or first part of columns.
        
        :param colnames: (*list*) List of the column names.
        '''
        for i in range(len(colnames)):
            self.data.renameColumn(i, colnames[i])
        self.columns = self.data.getColumnNames()
    
    def coldata(self, key):
        '''
        Return column data as one dimension array.
        
        :param key: (*string*) Column name.
        
        :returns: (*MIArray*) Colomn data.
        '''
        if isinstance(key, str):
            print key     
            values = self.data.getColumnData(key).getDataValues()
            return MIArray(ArrayUtil.array(values))
        return None
        
    def getvalue(self, row, col):
        '''
        Return a value in the table.
        
        :param row: (*int*) Row index.
        :param col: (*int*) Column index.
        
        :returns: The value at the row and column.
        '''
        r = self.data.getValue(row, col)
        if isinstance(r, Date):
            r = miutil.pydate(r)
        return r

    def setvalue(self, row, col, value):
        '''
        Set a value to the table.
        
        :param row: (*int*) Row index.
        :param col: (*int*) Column index.
        :param value: (*object*) The value.
        '''
        self.data.setValue(row, col, value)
    
    def addcoldata(self, colname, dtype, coldata, index=None):
        '''
        Add a column and its data.
        
        :param colname: (*string*) The new column name.
        :param dtype: (*string*) The data type. [string | int | float].
        :param value: (*array_like*) The data value.
        :param index: (*int*) The order index of the column to be added. Default is ``None``, the
            column will be added as last column.
        '''
        if isinstance(coldata, MIArray):
            coldata = coldata.aslist()
        if index is None:
            self.data.addColumnData(colname, dtype, coldata)
        else:
            self.data.addColumnData(index, colname, dtype, coldata)

    def addcol(self, colname, dtype, index=None):
        '''
        Add an emtpy column.
        
        :param colname: (*string*) The new column name.
        :param dtype: (*string*) The data type. [string | int | float].
        :param index: (*int*) The order index of the column to be added. Default is ``None``, the
            column will be added as last column.
        '''
        dtype = TableUtil.toDataTypes(dtype)
        if index is None:
            self.data.addColumn(colname, dtype)
        else:
            self.data.addColumn(index, colname, dtype)
    
    def delcol(self, colname):
        '''
        Delete a column.
        
        :param colname: (*string*) The column name.
        '''
        self.data.removeColumn(colname)
        
    def addrow(self, row=None):
        '''
        Add a row.
        
        :param row: (*DataRow*) The row. Default is ``None`, an emtpy row will be added.
        '''
        if row is None:
            self.data.addRow()
        else:
            self.data.addRow(row)
        self.shape = (self.data.getRowCount(), self.data.getColumnCount())
            
    def addrows(self, rows):
        '''
        Add rows.
        
        :param rows: (*list*) The list of the rows.
        '''
        self.data.addRows(rows)
        self.shape = (self.data.getRowCount(), self.data.getColumnCount())
        
    def delrow(self, row):
        '''
        Delete a row.
        
        :param row: (*int or DataRow*) Data row.
        '''
        self.data.removeRow(row)
        self.shape = (self.data.getRowCount(), self.data.getColumnCount())
        
    def delrows(self, rows):
        '''
        Delete rows.
        
        :param rows: (*list*) Data rows.
        '''
        self.data.removRows(rows)
        self.shape = (self.data.getRowCount(), self.data.getColumnCount())
        
    def clearrows(self):
        '''
        Clear all rows.               
        '''
        self.data.getRows().clear()
        self.shape = (self.data.getRowCount(), self.data.getColumnCount())
        
    def getrow(self, index):
        '''
        Return a row.
        
        :param index: (*int*) Row index.
        
        :returns: The row
        '''
        return self.data.getRow(index)
        
    def getrows(self):
        '''
        Return all rows.               
        '''
        return self.data.getRows()
        
    #Set time column
    def timecol(self, colname):
        '''
        Set time column.
        
        :param colname: (*string*) The Name of the column which will be set as time column. For time
            statistic calculation such as daily average.
        '''
        tdata = TimeTableData(self.data.dataTable, colname)
        self.data = tdata;
        self.timedata = True
        
    def join(self, other, colname, colname1=None):
        '''
        Join with another table. Joining data is typically used to append the fields of one table to 
        those of another through an attribute or field common to both tables.
        
        :param other: (*PyTableData*) The other table.
        :param colname: (*string*) The common field name.
        :param colname1: (*string*) The common field name in the other table. Default is ``None`` if
            the common field names are same in both tables.
        '''
        if colname1 == None:
            self.data.join(other.data, colname)
        else:
            self.data.join(other.data, colname, colname1)
        
    def savefile(self, filename, delimiter=',', format=None, date_format=None, float_format=None):
        '''
        Save the table data to an ASCII file.
        
        :param filename: (*string*) The file name.
        :param delimiter: (*string*) Field delimiter character. Default is ``,``.
        :param format: (*string*) Format string.
        :param date_format: (*string*) Date format string. i.e. 'yyyyMMddHH'.
        :param float_format: (*string*) Float format string. i.e. '%.2f'.
        '''
        self.data.saveAsASCIIFile(filename, delimiter, date_format, float_format)
            
    def ave(self, colnames):
        '''
        Average some columns data.
        
        :param colnames: (*list*) Column names.
        
        :returns: (*PyTableData*) Result table contains one row of average data of the columns.
        '''
        cols = self.data.findColumns(colnames)
        dtable = self.data.average(cols)
        return PyTableData(TableData(dtable))
        
    def sum(self, colnames):
        '''
        Summary some columns data.
        
        :param colnames: (*list*) Column names.
        
        :returns: (*PyTableData*) Result table contains one row of summary data of the columns.
        '''
        cols = self.data.findColumns(colnames)
        dtable = self.data.sum(cols)
        return PyTableData(TableData(dtable))
        
    def ave_year(self, colnames, year=None):
        '''
        Yearly average function. Time column is needed.
        
        :param colnames: (*list*) Column names.
        :param year: (*int*) Specific year. Default is ``None``.
        
        :returns: (*PyTableData*) Result table contains some rows of yearly average data of the columns.
        '''
        if not self.timedata:
            print 'There is no time column!'
            return None
        else:
            cols = self.data.findColumns(colnames)
            if year is None:
                dtable = self.data.ave_Year(cols)
            else:
                dtable = self.data.ave_Year(cols, year)
            return PyTableData(TableData(dtable))
            
    def sum_year(self, colnames, year=None):
        '''
        Yearly summary function. Time column is needed.
        
        :param colnames: (*list*) Column names.
        :param year: (*int*) Specific year. Default is ``None``.
        
        :returns: (*PyTableData*) Result table contains some rows of yearly summary data of the columns.
        '''
        if not self.timedata:
            print 'There is no time column!'
            return None
        else:
            cols = self.data.findColumns(colnames)
            if year is None:
                dtable = self.data.sum_Year(cols)
            else:
                dtable = self.data.sum_Year(cols, year)
            return PyTableData(TableData(dtable))
            
    def ave_yearmonth(self, colnames, month):
        '''
        Average the table data by year and month. Time column is needed.
        
        :param colnames: (*list*) Column names.
        :param month: (*int*) Specific month.
        
        :returns: (*PyTableData*) Result table contains some rows of year-month average data of the columns.
        '''
        if not self.timedata:
            print 'There is no time column!'
            return None
        else:
            cols = self.data.findColumns(colnames)
            dtable = self.data.ave_YearMonth(cols, month)
            return PyTableData(TableData(dtable))
            
    def sum_yearmonth(self, colnames, month):
        '''
        summary the table data by year and month. Time column is needed.
        
        :param colnames: (*list*) Column names.
        :param month: (*int*) Specific month.
        
        :returns: (*PyTableData*) Result table contains some rows of year-month summary data of the columns.
        '''
        if not self.timedata:
            print 'There is no time column!'
            return None
        else:
            cols = self.data.findColumns(colnames)
            dtable = self.data.sum_YearMonth(cols, month)
            return PyTableData(TableData(dtable))
                  
    def ave_monthofyear(self, colnames):
        '''
        Month of year average function. Time column is needed.
        
        :param colnames: (*list*) Column names.
        
        :returns: (*PyTableData*) Result table contains some rows of month of year average data of the columns.
        '''
        if not self.timedata:
            print 'There is no time column!'
            return None
        else:
            cols = self.data.findColumns(colnames)
            dtable = self.data.ave_MonthOfYear(cols)
            return PyTableData(TableData(dtable))
            
    def sum_monthofyear(self, colnames):
        '''
        Month of year summary function. Time column is needed.
        
        :param colnames: (*list*) Column names.
        
        :returns: (*PyTableData*) Result table contains some rows of month of year summary data of the columns.
        '''
        if not self.timedata:
            print 'There is no time column!'
            return None
        else:
            cols = self.data.findColumns(colnames)
            dtable = self.data.sum_MonthOfYear(cols)
            return PyTableData(TableData(dtable))
            
    def ave_seasonofyear(self, colnames):
        '''
        Season of year average function. Time column is needed.
        
        :param colnames: (*list*) Column names.
        
        :returns: (*PyTableData*) Result table contains some rows of season of year average data of the columns.
        '''
        if not self.timedata:
            print 'There is no time column!'
            return None
        else:
            cols = self.data.findColumns(colnames)
            dtable = self.data.ave_SeasonOfYear(cols)
            return PyTableData(TableData(dtable))
            
    def sum_seasonofyear(self, colnames):
        '''
        Season of year summary function. Time column is needed.
        
        :param colnames: (*list*) Column names.
        
        :returns: (*PyTableData*) Result table contains some rows of season of year summary data of the columns.
        '''
        if not self.timedata:
            print 'There is no time column!'
            return None
        else:
            cols = self.data.findColumns(colnames)
            dtable = self.data.sum_SeasonOfYear(cols)
            return PyTableData(TableData(dtable))
            
    def ave_hourofday(self, colnames):
        '''
        Hour of day average function. Time column is needed.
        
        :param colnames: (*list*) Column names.
        
        :returns: (*PyTableData*) Result table contains some rows of hour of day average data of the columns.
        '''
        if not self.timedata:
            print 'There is no time column!'
            return None
        else:
            cols = self.data.findColumns(colnames)
            dtable = self.data.ave_HourOfDay(cols)
            return PyTableData(TableData(dtable))
            
    def sum_hourofday(self, colnames):
        '''
        Hour of day summary function. Time column is needed.
        
        :param colnames: (*list*) Column names.
        
        :returns: (*PyTableData*) Result table contains some rows of hour of day summary data of the columns.
        '''
        if not self.timedata:
            print 'There is no time column!'
            return None
        else:
            cols = self.data.findColumns(colnames)
            dtable = self.data.sum_HourOfDay(cols)
            return PyTableData(TableData(dtable))
    
    def ave_month(self, colnames):
        '''
        Monthly average function. Time column is needed.
        
        :param colnames: (*list*) Column names.
        
        :returns: (*PyTableData*) Result table contains some rows of monthly average data of the columns.
        '''
        if not self.timedata:
            print 'There is no time column!'
            return None
        else:
            cols = self.data.findColumns(colnames)
            dtable = self.data.ave_Month(cols)
            return PyTableData(TableData(dtable))
            
    def sum_month(self, colnames):
        '''
        Monthly summary function. Time column is needed.
        
        :param colnames: (*list*) Column names.
        
        :returns: (*PyTableData*) Result table contains some rows of monthly summary data of the columns.
        '''
        if not self.timedata:
            print 'There is no time column!'
            return None
        else:
            cols = self.data.findColumns(colnames)
            dtable = self.data.sum_Month(cols)
            return PyTableData(TableData(dtable))
            
    def ave_day(self, colnames, day=None):
        '''
        Daily average function. Time column is needed.
        
        :param colnames: (*list*) Column names.
        
        :returns: (*PyTableData*) Result table contains some rows of daily average data of the columns.
        '''
        if not self.timedata:
            print 'There is no time column!'
            return None
        else:
            cols = self.data.findColumns(colnames)
            dtable = self.data.ave_Day(cols)
            ttd = TimeTableData(dtable, 'Date')
            return PyTableData(ttd)
            
    def sum_day(self, colnames, day=None):
        '''
        Daily summary function. Time column is needed.
        
        :param colnames: (*list*) Column names.
        
        :returns: (*PyTableData*) Result table contains some rows of daily summary data of the columns.
        '''
        if not self.timedata:
            print 'There is no time column!'
            return None
        else:
            cols = self.data.findColumns(colnames)
            dtable = self.data.sum_Day(cols)
            ttd = TimeTableData(dtable, 'Date')
            return PyTableData(ttd)
            
    def ave_dayofweek(self, colnames, day=None):
        '''
        Day of week average function. Time column is needed.
        
        :param colnames: (*list*) Column names.
        
        :returns: (*PyTableData*) Result table contains some rows of dya of week average data of the columns.
        '''
        if not self.timedata:
            print 'There is no time column!'
            return None
        else:
            cols = self.data.findColumns(colnames)
            dtable = self.data.ave_DayOfWeek(cols)
            ttd = TimeTableData(dtable, 'Date')
            return PyTableData(ttd)
            
    def sum_dayofweek(self, colnames, day=None):
        '''
        Day of week summary function. Time column is needed.
        
        :param colnames: (*list*) Column names.
        
        :returns: (*PyTableData*) Result table contains some rows of day of week summary data of the columns.
        '''
        if not self.timedata:
            print 'There is no time column!'
            return None
        else:
            cols = self.data.findColumns(colnames)
            dtable = self.data.sum_DayOfWeek(cols)
            ttd = TimeTableData(dtable, 'Date')
            return PyTableData(ttd)
            
    def ave_hour(self, colnames):
        '''
        Hourly average function. Time column is needed.
        
        :param colnames: (*list*) Column names.
        
        :returns: (*PyTableData*) Result table contains some rows of hourly average data of the columns.
        '''
        if not self.timedata:
            print 'There is no time column!'
            return None
        else:
            cols = self.data.findColumns(colnames)
            dtable = self.data.ave_Hour(cols)
            ttd = TimeTableData(dtable, 'Date')
            return PyTableData(ttd)
            
    def sum_hour(self, colnames):
        '''
        Hourly summary function. Time column is needed.
        
        :param colnames: (*list*) Column names.
        
        :returns: (*PyTableData*) Result table contains some rows of hourly summary data of the columns.
        '''
        if not self.timedata:
            print 'There is no time column!'
            return None
        else:
            cols = self.data.findColumns(colnames)
            dtable = self.data.sum_Hour(cols)
            ttd = TimeTableData(dtable, 'Date')
            return PyTableData(ttd)
            
    def assinglerow(self):
        '''
        Returns single row table if this table is single column table.
        '''
        return PyTableData(TableData(self.data.toSingleRowTable()))
        
    def sql(self, expression):
        '''
        Returns SQL selection result.
        
        :param expression: (*string*) SQL expression.
        
        :returns: (*PyTableData*) SQL result table.
        '''
        return PyTableData(self.data.sqlSelect(expression))
    
    def clone(self):
        '''
        Return coloned table.
        '''
        return PyTableData(self.data.clone())

#################################################################  