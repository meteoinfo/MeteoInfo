# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2017-3-9
# Purpose: MeteoInfoLab interpolate module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.math.interpolate import InterpUtil
from org.meteoinfo.ndarray.math import ArrayUtil
from org.meteoinfo.geometry.geoprocess import GeometryUtil

from ..core import NDArray
from ..core import numeric as np

__all__ = [
    'interp1d','interp2d','linint2','RectBivariateSpline','griddata'
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

def griddata(points, values, xi=None, **kwargs):
    '''
    Interpolate scattered data to grid data.

    :param points: (*list*) The list contains x and y coordinate arrays of the scattered data.
    :param values: (*array_like*) The scattered data array.
    :param xi: (*list*) The list contains x and y coordinate arrays of the grid data. Default is ``None``,
        the grid x and y coordinate size were both 500.
    :param method: (*string*) The interpolation method. [idw | cressman | nearest | inside_mean | inside_min
        | inside_max | inside_sum | inside_count | surface | barnes]
    :param fill_value: (*float*) Fill value, Default is ``nan``.
    :param pointnum: (*int*) Only used for 'idw' method. The number of the points to be used for each grid
        value interpolation.
    :param radius: (*float*) Used for 'idw', 'cressman' and 'neareast' methods. The searching raduis. Default
        is ``None`` in 'idw' method, means no raduis was used. Default is ``[10, 7, 4, 2, 1]`` in cressman
        method.
    :param centerpoint: (*boolean*) The grid points located at center or border of grid. Default
        is True (pont at center of grid).
    :param convexhull: (*boolean*) If the convexhull will be used to mask result grid data. Default is ``False``.

    :returns: (*array*) Interpolated grid data (2-D array)
    '''
    method = kwargs.pop('method', 'idw')
    x_s = points[0]
    y_s = points[1]
    is_3d = False
    if len(points) == 3:
        z_s = points[2]
        is_3d = True

    if xi is None:
        xn = 500
        yn = 500
        if is_3d:
            xn = 50
            yn = 50
            zn = 50
            z_g = np.linspace(z_s.min(), z_s.max(), zn)
        x_g = np.linspace(x_s.min(), x_s.max(), xn)
        y_g = np.linspace(y_s.min(), y_s.max(), yn)
    else:
        x_g = xi[0]
        y_g = xi[1]
        if is_3d:
            z_g = xi[2]

    if isinstance(values, NDArray):
        values = values.asarray()

    if method == 'idw':
        radius = kwargs.pop('radius', None)
        if radius is None:
            pnum = kwargs.pop('pointnum', None)
            if is_3d:
                r = InterpUtil.interpolation_IDW_Neighbor(x_s.asarray(), y_s.asarray(), z_s.asarray(), values,
                                                          x_g.asarray(), y_g.asarray(), z_g.asarray(), pnum)
                return NDArray(r), x_g, y_g, z_g
            else:
                r = InterpUtil.interpolation_IDW_Neighbor(x_s.asarray(), y_s.asarray(), values,
                                                          x_g.asarray(), y_g.asarray(), pnum)
        else:
            pnum = kwargs.pop('pointnum', 2)
            if is_3d:
                r = InterpUtil.interpolation_IDW_Radius(x_s.asarray(), y_s.asarray(), z_s.asarray(), values,
                                                        x_g.asarray(), y_g.asarray(), z_g.asarray(), pnum, radius)
                return NDArray(r), x_g, y_g, z_g
            else:
                r = InterpUtil.interpolation_IDW_Radius(x_s.asarray(), y_s.asarray(), values,
                                                        x_g.asarray(), y_g.asarray(), pnum, radius)
    elif method == 'cressman':
        radius = kwargs.pop('radius', [10, 7, 4, 2, 1])
        if isinstance(radius, NDArray):
            radius = radius.aslist()
        r = InterpUtil.cressman(x_s.aslist(), y_s.aslist(), values, x_g.aslist(), y_g.aslist(), radius)
    elif method == 'barnes':
        kappa = kwargs.pop('kappa', 1)
        gamma = kwargs.pop('gamma', 1)
        radius = kwargs.pop('radius', [10, 7, 4, 2, 1])
        if radius is None:
            r = InterpUtil.barnes(x_s.aslist(), y_s.aslist(), values, x_g.aslist(), y_g.aslist(), kappa, gamma)
        else:
            if isinstance(radius, NDArray):
                radius = radius.aslist()
            r = InterpUtil.barnes(x_s.aslist(), y_s.aslist(), values, x_g.aslist(), y_g.aslist(), radius, kappa, gamma)
    elif method == 'nearest':
        radius = kwargs.pop('radius', np.inf)
        if is_3d:
            r = InterpUtil.interpolation_Nearest(x_s.asarray(), y_s.asarray(), z_s.asarray(), values,
                                                 x_g.asarray(), y_g.asarray(), z_g.asarray(), radius)
            return NDArray(r), x_g, y_g, z_g
        else:
            r = InterpUtil.interpolation_Nearest(x_s.asarray(), y_s.asarray(), values, x_g.asarray(),
                                                 y_g.asarray(), radius)
    elif method == 'inside' or method == 'inside_mean':
        centerpoint = kwargs.pop('centerpoint', True)
        r = InterpUtil.interpolation_Inside_Mean(x_s.asarray(), y_s.asarray(), values, x_g.asarray(), y_g.asarray(), centerpoint)
    elif method == 'inside_max':
        centerpoint = kwargs.pop('centerpoint', True)
        r = InterpUtil.interpolation_Inside_Max(x_s.aslist(), y_s.aslist(), values, x_g.aslist(), y_g.aslist(), centerpoint)
    elif method == 'inside_min':
        centerpoint = kwargs.pop('centerpoint', True)
        r = InterpUtil.interpolation_Inside_Min(x_s.aslist(), y_s.aslist(), values, x_g.aslist(), y_g.aslist(), centerpoint)
    elif method == 'inside_sum':
        centerpoint = kwargs.pop('centerpoint', True)
        r = InterpUtil.interpolation_Inside_Sum(x_s.aslist(), y_s.aslist(), values, x_g.aslist(), y_g.aslist(), centerpoint)
    elif method == 'inside_count':
        centerpoint = kwargs.pop('centerpoint', True)
        r = InterpUtil.interpolation_Inside_Count(x_s.aslist(), y_s.aslist(), values, x_g.aslist(), y_g.aslist(), True, centerpoint)
        return NDArray(r[0]), x_g, y_g, NDArray(r[1])
    elif method == 'surface':
        r = InterpUtil.interpolation_Surface(x_s.asarray(), y_s.asarray(), values, x_g.asarray(), y_g.asarray())
    else:
        return None

    convexhull = kwargs.pop('convexhull', False)
    if convexhull:
        polyshape = GeometryUtil.convexHull(x_s.asarray(), y_s.asarray())
        x_gg, y_gg = np.meshgrid(x_g, y_g)
        r = GeometryUtil.maskout(r, x_gg._array, y_gg._array, [polyshape])
        return NDArray(r), x_g, y_g
    else:
        return NDArray(r), x_g, y_g
