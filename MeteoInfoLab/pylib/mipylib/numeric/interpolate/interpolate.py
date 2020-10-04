# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2017-3-9
# Purpose: MeteoInfoLab interpolate module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.math.interpolate import InterpUtil
from org.meteoinfo.math import ArrayUtil

from ..core import NDArray
from ..core import numeric as np

__all__ = [
    'interp1d','interp2d','linint2','RectBivariateSpline'
    ]

class interp1d(object):
    '''
    Interpolate a 1-D function.
    
    :param x: (*array_like*) A 1-D array of real values.
    :param y: (*array_like*) A 1-D array of real values. The length of y must be equal to the length of x.
    :param kind: (*boolean*) Specifies the kind of interpolation as a string (‘linear’, 
        ‘cubic’,‘akima’,‘divided’,‘loess’,‘neville’). Default is ‘linear’.
    '''
    def __init__(self, x, y, kind='linear'):        
        if isinstance(x, list):
            x = np.array(x)
        if isinstance(y, list):
            y = np.array(y)
        self._func = InterpUtil.getInterpFunc(x.asarray(), y.asarray(), kind)

    def __call__(self, x):
        '''
        Evaluate the interpolate vlaues.
        
        :param x: (*array_like*) Points to evaluate the interpolant at.
        '''
        if isinstance(x, list):
            x = np.array(x)
        if isinstance(x, NDArray):
            x = x.asarray()
        r = InterpUtil.evaluate(self._func, x)
        if isinstance(r, float):
            return r
        else:
            return NDArray(r)
            
class interp2d(object):
    '''
    Interpolate over a 2-D grid.

    x, y and z are arrays of values used to approximate some function f: z = f(x, y). 
    This class returns a function whose call method uses spline interpolation to find 
    the value of new points.

    If x and y represent a regular grid, consider using RectBivariateSpline.
    
    :param x: (*array_like*) 1-D arrays of x coordinate in strictly ascending order.
    :param y: (*array_like*) 1-D arrays of y coordinate in strictly ascending order.
    :param z: (*array_like*) 2-D array of data with shape (x.size,y.size).
    :param kind: (*boolean*) Specifies the kind of interpolation as a string (‘linear’, 
        ‘nearest’). Default is ‘linear’.
    '''
    def __init__(self, x, y, z, kind='linear'):
        if isinstance(x, list):
            x = np.array(x)
        if isinstance(y, list):
            y = np.array(y)
        if isinstance(z, list):
            z = np.array(z)
        self._func = InterpUtil.getBiInterpFunc(x.asarray(), y.asarray(), z.T.asarray(), kind)
        
    def __call__(self, x, y):
        '''
        Evaluate the interpolate vlaues.
        
        :param x: (*array_like*) X to evaluate the interpolant at.
        :param y: (*array_like*) Y to evaluate the interpolant at.
        '''
        if isinstance(x, list):
            x = np.array(x)
        if isinstance(x, NDArray):
            x = x.asarray()
        if isinstance(y, list):
            y = np.array(y)
        if isinstance(y, NDArray):
            y = y.asarray()
        r = InterpUtil.evaluate(self._func, x, y)
        if isinstance(r, float):
            return r
        else:
            return NDArray(r)
            
class RectBivariateSpline(object):
    '''
    Bivariate spline approximation over a rectangular mesh.
    
    Can be used for both smoothing and interpolating data.
    
    :param x: (*array_like*) 1-D arrays of x coordinate in strictly ascending order.
    :param y: (*array_like*) 1-D arrays of y coordinate in strictly ascending order.
    :param z: (*array_like*) 2-D array of data with shape (x.size,y.size).
    '''
    def __init__(self, x, y, z):        
        if isinstance(x, list):
            x = np.array(x)
        if isinstance(y, list):
            y = np.array(y)
        if isinstance(z, list):
            z = np.array(z)
        self._func = InterpUtil.getBiInterpFunc(x.asarray(), y.asarray(), z.asarray(), 'linear')

    def __call__(self, x, y):
        '''
        Evaluate the interpolate vlaues.
        
        :param x: (*array_like*) X to evaluate the interpolant at.
        :param y: (*array_like*) Y to evaluate the interpolant at.
        '''
        if isinstance(x, list):
            x = np.array(x)
        if isinstance(x, NDArray):
            x = x.asarray()
        if isinstance(y, list):
            y = np.array(y)
        if isinstance(y, NDArray):
            y = y.asarray()
        r = InterpUtil.evaluate(self._func, x, y)
        if isinstance(r, float):
            return r
        else:
            return NDArray(r)

def linint2(*args, **kwargs):
    """
    Interpolates from a rectilinear grid to another rectilinear grid using bilinear interpolation.

    :param x: (*array_like*) X coordinate array of the sample data (one dimension).
    :param y: (*array_like*) Y coordinate array of the sample data (one dimension).
    :param z: (*array_like*) Value array of the sample data (muti-dimension, last two dimensions are y and x).
    :param xq: (*array_like*) X coordinate array of the query data (one dimension).
    :param yq: (*array_like*) Y coordinate array of the query data (one dimension).

    :returns: (*array_like*) Interpolated array.
    """
    if len(args) == 3:
        z = args[0]
        x = z.dimvalue(z.ndim - 1)
        y = z.dimvalue(z.ndim - 2)
        xq = args[1]
        yq = args[2]
    else:
        x = args[0]
        y = args[1]
        z = args[2]
        xq = args[3]
        yq = args[4]
    x = np.array(x)._array
    y = np.array(y)._array
    z = np.array(z)._array
    xq = np.array(xq)._array
    yq = np.array(yq)._array
    r = ArrayUtil.linint2(z, x, y, xq, yq)
    return NDArray(r)