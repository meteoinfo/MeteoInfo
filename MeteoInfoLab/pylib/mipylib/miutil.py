#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2015-12-23
# Purpose: MeteoInfo util module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.global import PointD
from org.meteoinfo.global.util import DateUtil
from org.meteoinfo.math import Complex
from org.meteoinfo.shape import PointShape, ShapeUtil
from java.util import Calendar, Locale
from java.text import SimpleDateFormat
from java.awt import Color
from org.joda.time import DateTime
import datetime

def pydate(t):    
    """
    Convert java date to python date.
    
    :param t: Java date
    
    :returns: Python date
    """    
    if isinstance(t, list):
        r = []
        if isinstance(t[0], DateTime):
            for tt in t:
                dt = datetime.datetime(tt.getYear(), tt.getMonthOfYear(), tt.getDayOfMonth(), tt.getHourOfDay(), \
                    tt.getMinuteOfHour(), tt.getSecondOfMinute(), tt.getMillisOfSecond() * 1000)
                r.append(dt)            
        else:
            cal = Calendar.getInstance()
            for tt in t:
                cal.setTime(tt)
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
        if isinstance(t, DateTime):
            dt = datetime.datetime(tt.getYear(), tt.getMonthOfYear(), tt.getDayOfMonth(), \
                tt.getHourOfDay(), tt.getMinuteOfHour(), tt.getSecondOfMinute(), tt.getMillisOfSecond())
            return dt
        else:
            cal = Calendar.getInstance()
            cal.setTime(t)
            year = cal.get(Calendar.YEAR)
            month = cal.get(Calendar.MONTH) + 1
            day = cal.get(Calendar.DAY_OF_MONTH)
            hour = cal.get(Calendar.HOUR_OF_DAY)
            minute = cal.get(Calendar.MINUTE)
            second = cal.get(Calendar.SECOND)
            dt = datetime.datetime(year, month, day, hour, minute, second)
            return dt
    
def jdate(t):
    """
    Convert python date to java date.
    
    :param t: Python date
    
    :returns: Java date
    """
    cal = Calendar.getInstance()
    if isinstance(t, list):
        r = []
        for tt in t:
            cal.set(tt.year, tt.month - 1, tt.day, tt.hour, tt.minute, tt.second)
            cal.set(Calendar.MILLISECOND, 0)
            r.append(cal.getTime())
        return r
    else:
        cal.set(t.year, t.month - 1, t.day, t.hour, t.minute, t.second)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.getTime()
        
def jdatetime(t):
    """
    Convert python date to joda DateTime.
    
    :param t: Python date
    
    :returns: Joda DateTime
    """
    if isinstance(t, (list, tuple)):
        r = []
        for tt in t:
            r.append(DateTime(tt.year, tt.month, tt.day, tt.hour, tt.minute, tt.second, tt.microsecond / 1000))
        return r
    else:
        return DateTime(t.year, t.month, t.day, t.hour, t.minute, t.second, t.microsecond / 1000)
    
def date2num(t):
    """
    Convert python date to numerical value.
    
    :param t: Python date.
    
    :returns: Numerical value
    """
    tt = jdate(t)
    v = DateUtil.toOADate(tt)
    return v
    
def dates2nums(dates):
    """
    Convert python dates to numerical values.
    
    :param dates: (*list*) Python dates.
    
    :returns: (*list*) Numerical values
    """
    values = []
    for t in dates:
        tt = jdate(t)
        values.append(DateUtil.toOADate(tt))
    return values
    
def num2date(v):
    """
    Convert numerical value to python date.
    
    :param v: Numerical value of date.
    
    :returns: Python date
    """
    t = DateUtil.fromOADate(v)
    return pydate(t)
    
def nums2dates(values):
    """
    Convert numerical values to python dates.
    
    :param values: Numerical values of date.
    
    :returns: Python dates
    """
    tt = []
    for v in values:
        t = DateUtil.fromOADate(v)
        tt.append(pydate(t))
    return tt
    
def str2date(dstr):
    '''
    Convert string to python date.
    
    :param dstr: (*string*) date string.
    
    :returns: Python date
    '''
    n = len(dstr)
    if n == 8:
        t = datetime.datetime.strptime(dstr, '%Y%m%d')
    elif n == 10:
        if '-' in dstr:
            t = datetime.datetime.strptime(dstr, '%Y-%m-%d')
        else:
            t = datetime.datetime.strptime(dstr, '%Y%m%d%H')
    elif n == 12:
        t = datetime.datetime.strptime(dstr, '%Y%m%d%H%M')
    elif n == 14:
        t = datetime.datetime.strptime(dstr, '%Y%m%d%H%M%S')
    elif n == 18:
        t = datetime.datetime.strptime(dstr, '%Y-%m-%d %H:%M:%S')
    else:
        t = None
        
    return t
    
def str2jdate(dstr):
    '''
    Convert string to java date.
    
    :param dstr: (*string*) date string.
    
    :returns: Java date
    '''
    pt = str2date(dstr)
    jt = jdate(pt)
    return jt
    
def str2jdatetime(dstr):
    '''
    Convert string to joda DateTime.
    
    :param dstr: (*string*) date string.
    
    :returns: Joda DateTime
    '''
    pt = str2date(dstr)
    jt = jdatetime(pt)
    return jt
    
def dateformat(t, format, language=None):
    """
    Format python date to string using Java SimpleDateFormat.
    
    :param t: Python date.
    
    :returns: Format string of the date
    """
    jt = jdate(t)
    if language is None:
        df = SimpleDateFormat(format)
    else:
        locale = Locale(language)
        df = SimpleDateFormat(format, locale)
    return df.format(jt)
    
def jcomplex(v):
    '''
    Convert Python complex number to Java Complex object.
    
    :param v: (*complex*) Python complex number.
    
    :returns: (*Complex*) Java Complex object.
    '''
    return Complex(v.real, v.img)

def makeshapes(x, y, type=None, z=None, m=None):
    """
    Make shapes by x and y coordinates.
    
    :param x: (*array_like*) X coordinates.
    :param y: (*array_like*) Y coordinates.    
    :param type: (*string*) Shape type [point | line | polygon].
    :param z: (*array_like*) Z coordinates.
    :param m: (*array_like*) M coordinates.
    
    :returns: Shapes
    """
    shapes = []   
    if isinstance(x, (int, float)):
        shape = PointShape()
        shape.setPoint(PointD(x, y))
        shapes.append(shape)    
    else:
        if not isinstance(x, list):
            x = x.asarray()
        if not isinstance(y, list):
            y = y.asarray()
        if type == 'point':
            shapes = ShapeUtil.createPointShapes(x, y)
        elif type == 'line':
            shapes = ShapeUtil.createPolylineShapes(x, y)
        elif type == 'polygon':
            shapes = ShapeUtil.createPolygonShapes(x, y)
    return shapes     

def getcolor(style, alpha=None):
    '''
    Get color.
    
    :param style: (*color object*) Color object.
    :param alpha: (*float*) Color alpha.
    '''
    if style is None:
        return None
        
    if isinstance(style, Color):
        c = style
        if not alpha is None:
            alpha = (int)(alpha * 255)
            c = Color(c.getRed(), c.getGreen(), c.getBlue(), alpha)
        return c
        
    c = Color.black
    if isinstance(style, str):
        if style == 'red':
            c = Color.red
        elif style == 'black':
            c = Color.black
        elif style == 'blue':
            c = Color.blue
        elif style == 'green':
            c = Color.green
        elif style == 'white':
            c = Color.white
        elif style == 'yellow':
            c = Color.yellow
        elif style == 'gray':
            c = Color.gray
        elif style == 'lightgray':
            c = Color.lightGray
        else:
            if 'r' in style:
                c = Color.red
            elif 'k' in style:
                c = Color.black
            elif 'b' in style:
                c = Color.blue
            elif 'g' in style:
                c = Color.green
            elif 'w' in style:
                c = Color.white
            elif 'c' in style:
                c = Color.cyan
            elif 'm' in style:
                c = Color.magenta
            elif 'y' in style:
                c = Color.yellow 
    elif isinstance(style, (tuple, list)):
        if len(style) == 3:
            c = Color(style[0], style[1], style[2])
        else:
            c = Color(style[0], style[1], style[2], style[3])
    
    if not alpha is None:
        alpha = (int)(alpha * 255)
        c = Color(c.getRed(), c.getGreen(), c.getBlue(), alpha)
    
    return c    