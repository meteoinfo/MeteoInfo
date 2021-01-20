# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2014-12-27
# Purpose: MeteoInfo numerical module
# Note: Jython
#-----------------------------------------------------
import math
import cmath
import datetime
import numbers
import operator
import itertools
from org.meteoinfo.data import GridData, GridArray, StationData, DataMath, TableData, TableUtil
from org.meteoinfo.math import ArrayMath, ArrayUtil
from org.meteoinfo.data.meteodata.netcdf import NetCDFDataInfo
from org.meteoinfo.math.interpolate import InterpUtil
from org.meteoinfo.ndarray import Array, Dimension
from org.meteoinfo.geoprocess import GeometryUtil
from org.meteoinfo.util import JythonUtil
from org.python.core import PyComplex

from dimarray import PyGridData, DimArray, PyStationData
from multiarray import NDArray
from mitable import PyTableData
import mipylib.miutil as miutil
import _dtype

from java.lang import Math, Double, Float
from java.util import Calendar

# Global variables
pi = Math.PI
e = Math.E
inf = Double.POSITIVE_INFINITY
nan = Double.NaN
newaxis = None

__all__ = [
    'pi','e','inf','nan','acos','absolute','all','any','arange','arange1',
    'argmin','argmax','array','array_split','asanyarray','asarray','asgridarray','asgriddata','asin',
    'asmiarray','asstationdata','atleast_1d','atleast_2d','atan','atan2','ave_month','average','histogram',
    'broadcast_to','cdiff','ceil','concatenate','corrcoef','cos','cumsum','degrees','delete','delnan','diag',
    'diff','dim_array','datatable','dot','empty','empty_like','exp','eye','flatnonzero',
    'floor','fmax','fmin','full',
    'griddata','hcurl','hdivg','hstack','identity','interp2d',
    'interpn','isarray','isfinite','isinf','isnan','linspace','log','log10',
    'logical_not','logspace','magnitude','max','maximum','mean','median','meshgrid','min','minimum',
    'monthname','moveaxis','newaxis','nonzero','ones','ones_like','pol2cart','power','radians','ravel',
    'reshape','repeat','roll','rolling_mean','rot90','sin','shape','smooth5','smooth9','sort','squeeze','argsort',
    'split','sqrt','square','std','sum','swapaxes','tan','tile','transpose','trapz','vdot','unique',
    'unravel_index','var','vstack','where','zeros','zeros_like'
    ]

def isgriddata(gdata):
    return isinstance(gdata, PyGridData)
    
def isstationdata(sdata):
    return isinstance(sdata, PyStationData)
    
def array(object, dtype=None):
    """
    Create an array.
    
    :param object: (*array_like*) A Jython list or digital object.
    :param dtype: (*DataType*) Data type
                        
    :returns: (*NDArray*) An array object satisfying the specified requirements.
                    
    Examples
    
    ::
    
        >>> array([1,2,3])
        array([1, 2, 3])
        >>> array(25.6)
        array([25.6])
        
    More than one dimensions:
    
    ::
    
        >>> array([[1,2], [3,4]])
        array([[1.0, 2.0]
              [3.0, 4.0]])
    """
    if isinstance(object, NDArray):
        return object
    elif isinstance(object, Array):
        return NDArray(object)

    if isinstance(object, PyComplex):
        return NDArray(JythonUtil.toComplexArray(object))

    if isinstance(object, (list, tuple)):
        if len(object) > 0:
            if isinstance(object[0], datetime.datetime):
                object = miutil.dates2nums(object)
            elif isinstance(object[0], PyComplex):
                return NDArray(JythonUtil.toComplexArray(object))
            elif isinstance(object[0], (list, tuple)):
                if isinstance(object[0][0], PyComplex):
                    return NDArray(JythonUtil.toComplexArray(object))

    if isinstance(dtype, basestring):
        dtype = _dtype.DataType(dtype)
    if not dtype is None:
        dtype = dtype._dtype

    return NDArray(ArrayUtil.array(object, dtype))
    
def dim_array(a, dims=None):
    '''
    Create a dimension array (DimArray).
    
    :param a: (*array_like*) Array (NDArray) or data list.
    :param dims: (*list*) List of dimensions.
    
    :returns: (*DimArray*) Dimension array.
    '''
    if not isinstance(a, NDArray):
        a = array(a)
    if dims is None:
        dims = []
        for i in range(a.ndim):
            dim = Dimension()
            dim.setDimValues(range(a.shape[i]))
            dims.append(dim)
    return DimArray(a, dims)
    
def isarray(a):
    '''
    Check if input object is an array or not.
    
    :param a: (*object*) Input object.
    
    :returns: (*boolean*) True if the input object is an array.
    '''
    return isinstance(a, NDArray)
    
def datatable(data=None):
    '''
    Create a PyTableData object.
    
    :param data: (*TableData*) Table data.
    
    :returns: (*PyTableData*) PyTableData object.
    '''
    return PyTableData(data)   
    
def arange(*args):
    """
    Return evenly spaced values within a given interval
    
    Values are generated within the half-open interval ``[start, stop]`` (in other words,
    the interval including *start* but excluding *stop*).
    
    When using a non-integer step, such as 0.1, the results will often not be consistent.
    It is better to use ``linespace`` for these cases.
    
    :param start: (*number, optional*) Start of interval. The interval includes this value.
        The default start value is 0.
    :param stop: (*number*) End of interval. The interval does not include this value,
        except in some cases where *step* is not an integer and floating point round-off
        affects the length of *out*.
    :param step: (*number, optional*) Spacing between values. For any output *out*, this
        is the distance between two adjacent values, ``out[i+1] - out[i]``. The default
        step size is 1. If *step* is specified. *start* must also be given.
    :param dtype: (*dtype*) The type of output array. If dtype is not given, infer the data
        type from the other input arguments.
        
    :returns: (*NDArray*) Array of evenly spaced values.
    
    Examples::
    
        >>> arange(3)
        array([0, 1, 2])
        >>> arange(3,7,2)
        array([3, 5])
    """
    if len(args) == 1:
        start = 0
        stop = args[0]
        step = 1
    elif len(args) == 2:
        start = args[0]
        stop = args[1]
        step = 1
    else:
        start = args[0]
        stop = args[1]
        step = args[2]
    return NDArray(ArrayUtil.arrayRange(start, stop, step))
    
def arange1(start, num=50, step=1):
    """
    Return evenly spaced values within a given interval.
    
    :param start: (*number*) Start of interval. The interval includes this value.
    :param num: (*int*) Number of samples to generate. Default is 50. Must 
        be non-negative.
    :param step: (*number*) Spacing between values. For any output *out*, this
        is the distance between two adjacent values, ``out[i+1] - out[i]``. The default
        step size is 1.
        
    :returns: (*NDArray*) Array of evenly spaced values.
    
    Examples::
    
        >>> arange1(2, 5)
        array([2, 3, 4, 5, 6])
        >>> arange1(2, 5, 0.1)
        array([2.0, 2.1, 2.2, 2.3, 2.4])
    """
    return NDArray(ArrayUtil.arrayRange1(start, num, step))
    
def linspace(start, stop, num=50, endpoint=True, retstep=False, dtype=None):
    """
    Return evenly spaced numbers over a specified interval.

    Returns *num* evenly spaced samples, calculated over the interval [*start, stop*].

    The endpoint of the interval can optionally be excluded.
    
    :param start: (*number*) Start of interval. The interval includes this value.
    :param stop: (*number*) The end value of the sequence, unless endpoint is set to 
        False. In that case, the sequence consists of all but the last of ``num + 1`` 
        evenly spaced samples, so that stop is excluded. Note that the step size changes 
        when endpoint is False.
    :param num: (*int, optional*) Number of samples to generate. Default is 50. Must 
        be non-negative.
    :param endpoint: (*boolean, optional*) If true, stop is the last sample. Otherwise, it is not included. 
        Default is True.
    :param dtype: (*dtype*) The type of output array. If dtype is not given, infer the data
        type from the other input arguments.
        
    :returns: (*NDArray*) Array of evenly spaced values.
    
    Examples::
    
        >>> linspace(2.0, 3.0, num=5)
        array([2.0, 2.25, 2.5, 2.75, 3.0])
        >>> linspace(2.0, 3.0, num=5, endpoint=False)
        array([2.0, 2.25, 2.5, 2.75])
    """
    return NDArray(ArrayUtil.lineSpace(start, stop, num, endpoint))
    
def logspace(start, stop, num=50, endpoint=True, base=10.0, dtype=None):
    """
    Return numbers spaced evenly on a log scale.

    In linear space, the sequence starts at base ** start (*base to the power of start*) and ends with
    base ** stop.
    
    :param start: (*float*) Base ** start is the starting value of the sequence.
    :param stop: (*float*) Base ** stop is the final value of the sequence, unless *endpoint* is False.
        In that case, num + 1 values are spaced over the interval in log-space, of which all but the last
        (a sequence of length num) are returned.
    :param num: (*int, optional*) Number of samples to generate. Default is 50. Must 
        be non-negative.
    :param endpoint: (*boolean, optional*) If true, stop is the last sample. Otherwise, it is not included. 
        Default is True.
    :param base: (*float, optional*) The base of the log space. The step size between the elements in
        ln(samples) / ln(base) (or log_base(samples)) is uniform. Default is 10.0. 
    :param dtype: (*dtype*) The type of output array. If dtype is not given, infer the data
        type from the other input arguments.
        
    :returns: (*NDArray*) Array of evenly spaced values.
    
    Examples::
    
        >>> logspace(2.0, 3.0, num=4)
        array([100.0, 215.4434295785405, 464.1589682991224, 1000.0])
        >>> logspace(2.0, 3.0, num=4, base=2.0)
        array([4.0, 5.0396839219614975, 6.349604557649573, 8.0])
    """
    r = NDArray(ArrayUtil.lineSpace(start, stop, num, endpoint))
    r = pow(base, r)
    return r

def empty(shape, dtype='float'):
    """
    Create a new aray of given shape and type, without initializing entries.

    :param shape: (*int or sequence of ints*) Shape of the new array, e.g., ``(2, 3)`` or ``2``.
    :param dtype: (*data-type, optional*) The desired data-type for the array, including 'int', 
        'float' and 'double'.
        
    :returns: (*NDArray*) Array of zeros with the given shape and dtype.
                    
    Examples::
    
        >>> empty(5)
        array([0.0, 0.0, 0.0, 0.0, 0.0])
        >>> empty(5, dtype='int')
        array([0, 0, 0, 0, 0])
        >>> empty((2, 1))
        array([[0.0]
              [0.0]])
    """
    shapelist = []
    if isinstance(shape, int):
        shapelist.append(shape)
    else:
        shapelist = shape
    if isinstance(dtype, basestring):
        dtype = _dtype.DataType(dtype)
    return NDArray(ArrayUtil.zeros(shapelist, dtype._dtype))
    
def zeros(shape, dtype='float'):
    """
    Create a new aray of given shape and type, filled with zeros.

    :param shape: (*int or sequence of ints*) Shape of the new array, e.g., ``(2, 3)`` or ``2``.
    :param dtype: (*data-type, optional*) The desired data-type for the array, including 'int', 
        'float' and 'double'.
        
    :returns: (*NDArray*) Array of zeros with the given shape and dtype.
                    
    Examples::
    
        >>> zeros(5)
        array([0.0, 0.0, 0.0, 0.0, 0.0])
        >>> zeros(5, dtype='int')
        array([0, 0, 0, 0, 0])
        >>> zeros((2, 1))
        array([[0.0]
              [0.0]])
    """
    shapelist = []
    if isinstance(shape, int):
        shapelist.append(shape)
    else:
        shapelist = shape
    if isinstance(dtype, basestring):
        dtype = _dtype.DataType(dtype)
    return NDArray(ArrayUtil.zeros(shapelist, dtype._dtype))
    
def zeros_like(a, dtype=None):
    '''
    Return an array of zeros with the same shape and type as a given array.
    
    :param a: (*array*) The shape and data-type of a define these same attributes of the returned array.
    :param dtype: (*string*) Overrides the data type of the result. Default is ``None``, keep the data
        type of array ``a``.
        
    :returns: Array of zeros with the same shape and type as a.
    '''
    shape = a.shape
    if dtype is None:
        dtype = _dtype.fromjava(a.dtype)
    elif isinstance(dtype, basestring):
        dtype = _dtype.DataType(dtype)
    return NDArray(ArrayUtil.zeros(shape, dtype._dtype))

def empty_like(a, dtype=None):
    """
    Return a new array with the same shape and type as a given array.

    :param a: (*array*) The shape and data-type of a define these same attributes of the returned array.
    :param dtype: (*string*) Overrides the data type of the result. Default is ``None``, keep the data
        type of array ``a``.

    :returns: Array of uninitialized (arbitrary) data with the same shape and type as prototype.
    """
    shape = a.shape
    if dtype is None:
        dtype = a.dtype
    elif isinstance(dtype, basestring):
        dtype = _dtype.DataType(dtype)
    return NDArray(ArrayUtil.empty(shape, dtype._dtype))
    
def ones_like(a, dtype=None):
    '''
    Return an array of ones with the same shape and type as a given array.
    
    :param a: (*array*) The shape and data-type of a define these same attributes of the returned array.
    :param dtype: (*string*) Overrides the data type of the result. Default is ``None``, keep the data
        type of array ``a``.
        
    :returns: Array of ones with the same shape and type as a.
    '''
    shape = a.shape
    if dtype is None:
        dtype = _dtype.fromjava(a.dtype)
    elif isinstance(dtype, basestring):
        dtype = _dtype.DataType(dtype)
    return NDArray(ArrayUtil.ones(shape, dtype._dtype))
    
def ones(shape, dtype='float'):
    """
    Create a new aray of given shape and type, filled with ones.

    :param shape: (*int or sequence of ints*) Shape of the new array, e.g., ``(2, 3)`` or ``2``.
    :param dtype: (*data-type, optional*) The desired data-type for the array, including 'int', 
        'float' and 'double'.
        
    :returns: (*NDArray*) Array of ones with the given shape and dtype.
                    
    Examples::
    
        >>> ones(5)
        array([1.0, 1.0, 1.0, 1.0, 1.0])
        >>> ones(5, dtype='int')
        array([1, 1, 1, 1, 1])
        >>> ones((2, 1))
        array([[1.0]
              [1.0]])
    """
    shapelist = []
    if isinstance(shape, int):
        shapelist.append(shape)
    else:
        shapelist = shape
    if isinstance(dtype, basestring):
        dtype = _dtype.DataType(dtype)
    return NDArray(ArrayUtil.ones(shapelist, dtype._dtype))
    
def full(shape, fill_value, dtype=None):
    '''
    Return a new array of given shape and type, filled with fill_value.
    
    :param shape: (*int or sequence of ints*) Shape of the new array, e.g., ``(2, 3)`` or ``2``.
    :param fill_value: (*scalar*) Fill value.
    :param dtype: (*data-type, optional*) The desired data-type for the array, including 'int', 
        'float' and 'double'.
        
    :returns: (*NDArray*) Array of ones with the given shape and dtype.
    '''
    shapelist = []
    if isinstance(shape, int):
        shapelist.append(shape)
    else:
        shapelist = shape
    if isinstance(dtype, basestring):
        dtype = _dtype.DataType(dtype)._dtype

    return NDArray(ArrayUtil.full(shapelist, fill_value, dtype))
    
def identity(n, dtype='float'):
    '''
    Return the identity array - a square array with ones on the main diagonal.
    
    :param n: (*int*) Number of rows (and columns) in ``n x n`` output.
    :param dtype: (*string*) The desired data-type for the array, including 'int', 
        'float' and 'double'. Default is ``float``.
        
    :returns: (*NDArray*) ``n x n`` array with its main diagonal set to one, and all other elements 0.
    '''
    if isinstance(dtype, basestring):
        dtype = _dtype.DataType(dtype)
    return NDArray(ArrayUtil.identity(n, dtype._dtype))
    
def eye(n, m=None, k=0, dtype='float'):
    '''
    Return a 2-D array with ones on the diagonal and zeros elsewhere.
    
    :param n: (*int*) Number of rows in the output.
    :param m: (*int*) Number of columns in the output. If ``None``, defaults to ``n``.
    :param k: (*int*) Index of the diagonal: 0 (the default) refers to the main diagonal, a positive value 
        refers to an upper diagonal, and a negative value to a lower diagonal.
    :param dtype: (*string*) The desired data-type for the array, including 'int', 
        'float' and 'double'. Default is ``float``.
        
    :returns: (*NDArray*) ``n x n`` array with its main diagonal set to one, and all other elements 0.
    '''
    if m is None:
        m = n
    if isinstance(dtype, basestring):
        dtype = _dtype.DataType(dtype)
    return NDArray(ArrayUtil.eye(n, m, k, dtype._dtype))
    
def diag(v, k=0):
    '''
    Extract a diagonal or construct a diagonal array.
        
    Parameters
    ----------
    v : array_like
        If `v` is a 2-D array, return a copy of its `k`-th diagonal.
        If `v` is a 1-D array, return a 2-D array with `v` on the `k`-th
        diagonal.
    k : int, optional
        Diagonal in question. The default is 0. Use `k>0` for diagonals
        above the main diagonal, and `k<0` for diagonals below the main
        diagonal.
        
    Returns
    -------
    out : ndarray
        The extracted diagonal or constructed diagonal array.
    '''
    if isinstance(v, (list, tuple)):
        v = array(v)
    return NDArray(ArrayUtil.diag(v.asarray(), k))
    
def repeat(a, repeats, axis=None):
    '''
    Repeat elements of an array.
    
    :param repeats: (*int or list of ints*) The number of repetitions for each 
        element. repeats is broadcasted to fit the shape of the given axis.
    :param axis: (*int*) The axis along which to repeat values. By default, use 
        the flattened input array, and return a flat output array.
    
    :returns: (*array_like*) Repeated array.
    '''
    if isinstance(repeats, int):
        repeats = [repeats]
    if isinstance(a, (list, tuple)):
        a = array(a)
    if isinstance(a, NDArray):
        a = a.asarray()    
    if axis is None:
        r = ArrayUtil.repeat(a, repeats)
    else:
        r = ArrayUtil.repeat(a, repeats, axis)
    return NDArray(r)
    
def tile(a, repeats):
    '''
    Construct an array by repeating ``a`` the number of times given by repeats.
    
    If repeats has length ``d``, the result will have dimension of ``max(d, a.ndim)``.
    
    :param repeats: (*int or list of ints*) The number of repetitions of ``a`` along each 
        axis.
    
    :returns: (*array_like*) Tiled array.
    '''
    if isinstance(repeats, int):
        repeats = [repeats]
    if isinstance(a, (list, tuple)):
        a = array(a)
    if isinstance(a, NDArray):
        a = a.asarray()    
    r = ArrayUtil.tile(a, repeats)
    return NDArray(r)
    
def rand(*args):
    """
    Random values in a given shape.
    
    Create an array of the given shape and propagate it with random samples from a uniform 
        distribution over [0, 1).
    
    :param d0, d1, ..., dn: (*int*) optional. The dimensions of the returned array, should all
        be positive. If no argument is given a single Python float is returned.
        
    :returns: Random values array.
    """
    if len(args) == 0:
        return ArrayUtil.rand()
    elif len(args) == 1:
        return NDArray(ArrayUtil.rand(args[0]))
    else:
        return NDArray(ArrayUtil.rand(args))
        
def absolute(x):
    '''
    Calculate the absolute value element-wise.
    
    :param x: (*array_like*) Input array.
    
    :returns: An array containing the absolute value of each element in x. 
        For complex input, a + ib, the absolute value is \sqrt{ a^2 + b^2 }.
    '''
    if isinstance(x, list):
        x = array(x)
    if isinstance(x, NDArray):
        return x.abs()
    else:
        return abs(x)

def ceil(x):
    '''
    Return the ceiling of the input, element-wise.

    :param x: (*array_like*) Input array.

    :return: The ceiling of each element.
    '''
    if isinstance(x, list):
        x = array(x)
    if isinstance(x, NDArray):
        return x.ceil()
    else:
        return math.ceil(x)

def floor(x):
    '''
    Return the floor of the input, element-wise.

    :param x: (*array_like*) Input array.

    :return: The floor of each element.
    '''
    if isinstance(x, list):
        x = array(x)
    if isinstance(x, NDArray):
        return x.floor()
    else:
        return math.floor(x)

def square(x):
    """
    Return the element-wise square of the input.

    :param x: (*array_like*) Input data.

    :returns: Element-wise x*x, of the same shape and dtype as x. This is a scalar if x is a scalar.
    """
    if isinstance(x, list):
        return array(x).square()
    elif isinstance(x, NDArray):
        return x.square()
    else:
        return x * x
    
def sqrt(x):
    """
    Return the positive square-root of an array, element-wise.
    
    :param x: (*array_like*) The values whose square-roots are required.
    
    :returns: (*array_like*) An array of the same shape as *x*, containing the positive
        square-root of each element in *x*.
        
    Examples::
    
        >>> sqrt([1,4,9])
        array([1.0, 2.0, 3.0])
    """
    if isinstance(x, list):
        return array(x).sqrt()
    elif isinstance(x, NDArray):
        return x.sqrt()
    else:
        return math.sqrt(x)
        
def power(x1, x2):
    """
    First array elements raised to powers from second array, element-wise.
    
    :param x1: (*array_like*) The bases.
    :param x2: (*array_like*) The exponents.
    
    :returns: (*array_like*) The bases in *x1* raised to the exponents in *x2*.
    """
    if isinstance(x1, list):
        x1 = array(x1)
    if isinstance(x2, list):
        x2 = array(x2)
    if isinstance(x1, NDArray):
        if isinstance(x2, NDArray):
            return NDArray(ArrayMath.pow(x1.asarray(), x2.asarray()))
        else:
            return NDArray(ArrayMath.pow(x1.asarray(), x2))
    else:
        if isinstance(x2, NDArray):
            return NDArray(ArrayMath.pow(x1, x2.asarray()))
        else:
            if isinstance(x1, complex):
                return pow(x1, x2)
            else:
                return math.pow(x1, x2)
    
def degrees(x):
    '''
    Convert radians to degrees.
    
    :param x: (*array_like*) Array in radians.
    
    :returns: (*array_like*) Array in degrees.
    '''
    if isinstance(x, (list, tuple)):
        x = array(x)
    if isinstance(x, NDArray):
        return NDArray(ArrayMath.toDegrees(x.asarray()))
    else:
        return math.degrees(x)
        
def radians(x):
    '''
    Convert degrees to radians.
    
    :param x: (*array_like*) Array in degrees.
    
    :returns: (*array_like*) Array in radians.
    '''
    if isinstance(x, (list, tuple)):
        x = array(x)
    if isinstance(x, NDArray):
        return NDArray(ArrayMath.toRadians(x.asarray()))
    else:
        return math.radians(x)

def sin(x):
    """
    Trigonometric sine, element-wise.
    
    :param x: (*array_like*) Angle, in radians.
    
    :returns: (*array_like*) The sine of each element of x.
    
    Examples::
    
        >>> sin(pi/2.)
        1.0
        >>> sin(array([0., 30., 45., 60., 90.]) * pi / 180)
        array([0.0, 0.49999999999999994, 0.7071067811865475, 0.8660254037844386, 1.0])
    """
    if isinstance(x, list):
        return array(x).sin()
    elif isinstance(x, NDArray):
        return x.sin()
    else:
        if isinstance(x, complex):
            return cmath.sin(x)
        else:
            return math.sin(x)
    
def cos(x):
    """
    Trigonometric cosine, element-wise.
    
    :param x: (*array_like*) Angle, in radians.
    
    :returns: (*array_like*) The cosine of each element of x.
    
    Examples::
    
        >>> cos(array([0, pi/2, pi]))
        array([1.0, 6.123233995736766E-17, -1.0])
    """
    if isinstance(x, list):
        return array(x).cos()
    elif isinstance(x, NDArray):
        return x.cos()
    else:
        if isinstance(x, complex):
            return cmath.cos(x)
        else:
            return math.cos(x)
        
def tan(x):
    """
    Trigonometric tangent, element-wise.
    
    :param x: (*array_like*) Angle, in radians.
    
    :returns: (*array_like*) The tangent of each element of x.
    
    Examples::
    
        >>> tan(array([-pi,pi/2,pi]))
        array([1.2246467991473532E-16, 1.633123935319537E16, -1.2246467991473532E-16])
    """
    if isinstance(x, list):
        return array(x).tan()
    elif isinstance(x, NDArray):
        return x.tan()
    else:
        if isinstance(x, complex):
            return cmath.tan(x)
        else:
            return math.tan(x)
        
def asin(x):
    """
    Trigonometric inverse sine, element-wise.
    
    :param x: (*array_like*) *x*-coordinate on the unit circle.
    
    :returns: (*array_like*) The inverse sine of each element of *x*, in radians and in the
        closed interval ``[-pi/2, pi/2]``.
    
    Examples::
    
        >>> asin(array([1,-1,0]))
        array([1.5707964, -1.5707964, 0.0])
    """
    if isinstance(x, list):
        return array(x).asin()
    elif isinstance(x, NDArray):
        return x.asin()
    else:
        if isinstance(x, complex):
            return cmath.asin(x)
        else:
            return math.asin(x)
        
def acos(x):
    """
    Trigonometric inverse cosine, element-wise.
    
    :param x: (*array_like*) *x*-coordinate on the unit circle. For real arguments, the domain
        is ``[-1, 1]``.
    
    :returns: (*array_like*) The inverse cosine of each element of *x*, in radians and in the
        closed interval ``[0, pi]``.
    
    Examples::
    
        >>> acos([1, -1])
        array([0.0, 3.1415927])
    """
    if isinstance(x, list):
        return array(x).acos()
    elif isinstance(x, NDArray):
        return x.acos()
    else:
        if isinstance(x, complex):
            return cmath.acos(x)
        else:
            return math.acos(x)
        
def atan(x):
    """
    Trigonometric inverse tangent, element-wise.
    
    The inverse of tan, so that if ``y = tan(x)`` then ``x = atan(y)``.
    
    :param x: (*array_like*) Input values, ``atan`` is applied to each element of *x*.
    
    :returns: (*array_like*) Out has the same shape as *x*. Its real part is in
        ``[-pi/2, pi/2]`` .
    
    Examples::
    
        >>> atan([0, 1])
        array([0.0, 0.7853982])
    """
    if isinstance(x, list):
        return array(x).atan()
    elif isinstance(x, NDArray):
        return x.atan()
    else:
        if isinstance(x, complex):
            return cmath.atan(x)
        else:
            return math.atan(x)
        
def atan2(x1, x2):
    """
    Element-wise arc tangent of ``x1/x2`` choosing the quadrant correctly.

    :param x1: (*array_like*) *y*-coordinates.
    :param x2: (*array_like*) *x*-coordinates. *x2* must be broadcastable to match the 
        shape of *x1* or vice versa.
        
    :returns: (*array_like*) Array of angles in radians, in the range ``[-pi, pi]`` .
    
    Examples::
    
        >>> x = array([-1, +1, +1, -1])
        >>> y = array([-1, -1, +1, +1])
        >>> atan2(y, x) * 180 / pi
        array([-135.00000398439022, -45.000001328130075, 45.000001328130075, 135.00000398439022])
    """    
    if isinstance(x1, NDArray):
        r = NDArray(ArrayMath.atan2(x1._array, x2._array))
        if isinstance(x1, DimArray):
            return DimArray(r, x1.dims, x1.fill_value, x1.proj)
        else:
            return r
    else:
        return math.atan2(x1, x2)
        
def exp(x):
    """
    Calculate the exponential of all elements in the input array.
    
    :param x: (*array_like*) Input values.
    
    :returns: (*array_like*) Output array, element-wise exponential of *x* .
    
    Examples::
    
        >>> x = linspace(-2*pi, 2*pi, 10)
        >>> exp(x)
        array([0.0018674424051939472, 0.007544609964764651, 0.030480793298392952, 
            0.12314470389303135, 0.4975139510383202, 2.0099938864286777, 
            8.120527869949177, 32.80754507307142, 132.54495655444984, 535.4917491531113])
    """
    if isinstance(x, list):
        return array(x).exp()
    elif isinstance(x, NDArray):
        return x.exp()
    else:
        if isinstance(x, complex):
            return cmath.exp(x)
        else:
            return math.exp(x)
        
def log(x):
    """
    Natural logarithm, element-wise.
    
    The natural logarithm log is the inverse of the exponential function, so that 
    *log(exp(x))* = *x* . The natural logarithm is logarithm in base e.
    
    :param x: (*array_like*) Input values.
    
    :returns: (*array_like*) The natural logarithm of *x* , element-wise.
    
    Examples::
    
        >>> log([1, e, e**2, 0])
        array([0.0, 1.0, 2.0, -Infinity])
    """
    if isinstance(x, list):
        return array(x).log()
    elif isinstance(x, NDArray):
        return x.log()
    else:
        if isinstance(x, complex):
            return cmath.exp(x)
        else:
            return math.log(x)
        
def log10(x):
    """
    Return the base 10 logarithm of the input array, element-wise.
    
    :param x: (*array_like*) Input values.
    
    :returns: (*array_like*) The logarithm to the base 10 of *x* , element-wise.
    
    Examples::
    
        >>> log10([1e-15, -3.])
        array([-15.,  NaN])
    """
    if isinstance(x, list):
        return array(x).log10()
    elif isinstance(x, NDArray):
        return x.log10()
    else:
        if isinstance(x, complex):
            return cmath.log10(x)
        else:
            return math.log10(x)
            
def sign(x):
    '''
    Returns an element-wise indication of the sign of a number.

    The sign function returns -1 if x < 0, 0 if x==0, 1 if x > 0. nan is returned for nan inputs.
    
    :param x: (*array_like*) Input values.
    
    :returns: The sign of x. This is a scalar if x is a scalar.
    '''
    if isinstance(x, list):
        x = array(x)
        
    if isinstance(x, NDArray):
        return x.sign()
    else:
        return math.copysign(1, x)
        
def any(x, axis=None):
    '''
    Test whether any array element along a given axis evaluates to True.
    
    :param x: (*array_like or list*) Input values.
    :param axis: (*int*) Axis along which a logical OR reduction is performed. 
        The default (axis = None) is to perform a logical OR over all the 
        dimensions of the input array.
    
    :returns: (*array_like*) Any result
    '''
    if isinstance(x, list):
        x = array(x)
        
    if axis is None:
        return ArrayMath.any(x._array)
    else:
        if axis < 0:
            axis += x.ndim
        return NDArray(ArrayMath.any(x._array, axis))
        
def all(x, axis=None):
    '''
    Test whether all array element along a given axis evaluates to True.
    
    :param x: (*array_like or list*) Input values.
    :param axis: (*int*) Axis along which a logical OR reduction is performed. 
        The default (axis = None) is to perform a logical OR over all the 
        dimensions of the input array.
    
    :returns: (*array_like*) All result
    '''
    if isinstance(x, list):
        x = array(x)
        
    if axis is None:
        return ArrayMath.all(x._array)
    else:
        if axis < 0:
            axis += x.ndim
        return NDArray(ArrayMath.all(x._array, axis))

def sum(x, axis=None):
    """
    Sum of array elements over a given axis.
    
    :param x: (*array_like or list*) Input values.
    :param axis: (*int*) Axis along which the standard deviation is computed. 
        The default is to compute the standard deviation of the flattened array.
    
    :returns: (*array_like*) Sum result
    """
    if isinstance(x, list):
        if isinstance(x[0], NDArray):
            a = []
            for xx in x:
                a.append(xx.asarray())
            r = ArrayMath.sum(a)
            if type(x[0]) is NDArray:            
                return NDArray(r)
            else:
                return DimArray(NDArray(r), x[0].dims, x[0].fill_value, x[0].proj)
        else:
            x = array(x)
    if axis is None:
        r = ArrayMath.sum(x.asarray())
        return r
    else:
        r = ArrayMath.sum(x.asarray(), axis)
        if type(x) is NDArray:
            return NDArray(r)
        else:
            dims = []
            for i in range(0, x.ndim):
                if i != axis:
                    dims.append(x.dims[i])
            return DimArray(NDArray(r), dims, x.fill_value, x.proj)
            
def cumsum(x, axis=None):
    """
    Return the cumulative sum of the elements along a given axis.
    
    :param x: (*array_like or list*) Input values.
    :param axis: (*int*) Axis along which the standard deviation is computed. 
        The default is to compute the standard deviation of the flattened array.
    
    :returns: (*array_like*) Cumulative sum result.
    """
    x = asarray(x)
    r = x.cumsum(axis)
    if type(x) is NDArray:
        return r
    else:
        dims = []
        for i in range(0, x.ndim):
            dims.append(x.dims[i])
        return DimArray(r, dims, x.fill_value, x.proj)
            
def mean(x, axis=None):
    """
    Compute tha arithmetic mean along the specified axis.
    
    :param x: (*array_like*) Input values.
    :param axis: (*int*) Axis along which the means is computed.
        The default is to compute the means of the flattened array.
    
    returns: (*array_like*) Mean result
    """
    if isinstance(x, list):
        if isinstance(x[0], NDArray):
            a = []
            for xx in x:
                a.append(xx.asarray())
            r = ArrayMath.mean(a)
            if type(x[0]) is NDArray:            
                return NDArray(r)
            else:
                return DimArray(NDArray(r), x[0].dims, x[0].fill_value, x[0].proj)
        elif isinstance(x[0], PyStationData):
            a = []
            for xx in x:
                a.append(xx.data)
            r = DataMath.mean(a)
            return PyStationData(r)
        else:
            x = array(x)
    r = x.mean(axis)
    return r

def average(a, axis=None, weights=None):
    """
    Compute tha arithmetic mean along the specified axis.

    :param a: (*array_like*) Input values.
    :param axis: (*int*) Axis along which the standard deviation is computed.
        The default is to compute the standard deviation of the flattened array.
    :param weights: (*array_like*) An array of weights associated with the values in `a`. Each value in
        `a` contributes to the average according to its associated weight.

    returns: (*array_like*) Average result
    """
    a = asanyarray(a)

    if weights is None:
        return a.mean(axis)
    else:
        wgt = asanyarray(weights)
        # Sanity checks
        if a.shape != wgt.shape:
            if axis is None:
                raise TypeError(
                    "Axis must be specified when shapes of a and weights "
                    "differ.")
            if wgt.ndim != 1:
                raise TypeError(
                    "1D weights expected when shapes of a and weights differ.")
            if wgt.shape[0] != a.shape[axis]:
                raise ValueError(
                    "Length of weights not compatible with specified axis.")

            # setup wgt to broadcast along axis
            wgt = broadcast_to(wgt, (a.ndim-1)*(1,) + wgt.shape)
            wgt = wgt.swapaxes(-1, axis)

        scl = wgt.sum(axis=axis)
        avg = (a * wgt).sum(axis) / scl

        if type(a) is NDArray:
            return avg
        else:
            dims = []
            for i in range(0, a.ndim):
                if i != axis:
                    dims.append(a.dims[i])
            return DimArray(avg, dims, x.fill_value, x.proj)
            
def std(x, axis=None, ddof=0):
    '''
    Compute the standard deviation along the specified axis.
    
    :param x: (*array_like or list*) Input values.
    :param axis: (*int*) Axis along which the standard deviation is computed. 
        The default is to compute the standard deviation of the flattened array.
    :param ddof: (*int*) Delta Degrees of Freedom: the divisor used in the calculation is
        N - ddof, where N represents the number of elements. By default ddof is zero.
    
    returns: (*array_like*) Standart deviation result.
    '''
    if isinstance(x, (list, tuple)):
        x = array(x)
    r = x.std(axis, ddof)
    return r
            
def var(x, axis=None, ddof=0):
    '''
    Compute variance along the specified axis.
    
    :param x: (*array_like or list*) Input values.
    :param axis: (*int*) Axis along which the variance is computed. 
        The default is to compute the variance of the flattened array.
    :param ddof: (*int*) Delta Degrees of Freedom: the divisor used in the calculation is
        N - ddof, where N represents the number of elements. By default ddof is zero.
    
    returns: (*array_like*) Variance result.
    '''
    if isinstance(x, (list, tuple)):
        x = array(x)
    r = x.var(axis, ddof)
    return r
                
def median(x, axis=None):
    """
    Compute tha median along the specified axis.
    
    :param x: (*array_like or list*) Input values.
    :param axis: (*int*) Axis along which the standard deviation is computed. 
        The default is to compute the standard deviation of the flattened array.
    
    returns: (*array_like*) Median result
    """
    if isinstance(x, list):
        if isinstance(x[0], NDArray):
            a = []
            for xx in x:
                a.append(xx.asarray())
            r = ArrayMath.median(a)
            if type(x[0]) is NDArray:            
                return NDArray(r)
            else:
                return DimArray(NDArray(r), x[0].dims, x[0].fill_value, x[0].proj)
        elif isinstance(x[0], PyStationData):
            a = []
            for xx in x:
                a.append(xx.data)
            r = DataMath.median(a)
            return PyStationData(r)
        else:
            x = array(x)
    r = x.median(axis)
    return r
                
def maximum(x1, x2):
    """
    Element-wise maximum of array elements.
    
    Compare two arrays and returns a new array containing the element-wise maxima. If one of the elements 
    being compared is a NaN, then that element is returned. If both elements are NaNs then the first is 
    returned. The latter distinction is important for complex NaNs, which are defined as at least one of 
    the real or imaginary parts being a NaN. The net effect is that NaNs are propagated.
    
    :param x1,x2: (*array_like*) The arrays holding the elements to be compared. They must have the same 
        shape.
    
    :returns: The maximum of x1 and x2, element-wise. Returns scalar if both x1 and x2 are scalars.
    """
    if isinstance(x1, list):
        x1 = array(x1)
    if isinstance(x2, list):
        x2 = array(x2)
    if type(x1) is NDArray:
        return NDArray(ArrayMath.maximum(x1.asarray(), x2.asarray()))
    elif isinstance(x1, DimArray):
        r = NDArray(ArrayMath.maximum(x1.asarray(), x2.asarray()))
        return DimArray(r, x1.dims, x1.fill_value, x1.proj)
    else:
        return max(x1, x2)
        
def fmax(x1, x2):
    """
    Element-wise maximum of array elements.
    
    Compare two arrays and returns a new array containing the element-wise maxima. If one of the 
    elements being compared is a NaN, then the non-nan element is returned. If both elements are 
    NaNs then the first is returned. The latter distinction is important for complex NaNs, which 
    are defined as at least one of the real or imaginary parts being a NaN. The net effect is that 
    NaNs are ignored when possible.
    
    :param x1,x2: (*array_like*) The arrays holding the elements to be compared. They must have the same 
        shape.
    
    :returns: The maximum of x1 and x2, element-wise. Returns scalar if both x1 and x2 are scalars.
    """
    if isinstance(x1, list):
        x1 = array(x1)
    if isinstance(x2, list):
        x2 = array(x2)
    if type(x1) is NDArray:
        return NDArray(ArrayMath.fmax(x1.asarray(), x2.asarray()))
    elif isinstance(x1, DimArray):
        r = NDArray(ArrayMath.fmax(x1.asarray(), x2.asarray()))
        return DimArray(r, x1.dims, x1.fill_value, x1.proj)
    else:
        return max(x1, x2)
        
def minimum(x1, x2):
    """
    Element-wise minimum of array elements.
    
    Compare two arrays and returns a new array containing the element-wise minima. If one of the elements 
    being compared is a NaN, then that element is returned. If both elements are NaNs then the first is 
    returned. The latter distinction is important for complex NaNs, which are defined as at least one of 
    the real or imaginary parts being a NaN. The net effect is that NaNs are propagated.
    
    :param x1,x2: (*array_like*) The arrays holding the elements to be compared. They must have the same 
        shape.
    
    :returns: The minimum of x1 and x2, element-wise. Returns scalar if both x1 and x2 are scalars.
    """
    if isinstance(x1, list):
        x1 = array(x1)
    if isinstance(x2, list):
        x2 = array(x2)
    if type(x1) is NDArray:
        return NDArray(ArrayMath.minimum(x1.asarray(), x2.asarray()))
    elif isinstance(x1, DimArray):
        r = NDArray(ArrayMath.minimum(x1.asarray(), x2.asarray()))
        return DimArray(r, x1.dims, x1.fill_value, x1.proj)
    else:
        return min(x1, x2)
        
def fmin(x1, x2):
    """
    Element-wise minimum of array elements.
    
    Compare two arrays and returns a new array containing the element-wise minima. If one of the 
    elements being compared is a NaN, then the non-nan element is returned. If both elements are 
    NaNs then the first is returned. The latter distinction is important for complex NaNs, which 
    are defined as at least one of the real or imaginary parts being a NaN. The net effect is that 
    NaNs are ignored when possible.
    
    :param x1,x2: (*array_like*) The arrays holding the elements to be compared. They must have the same 
        shape.
    
    :returns: The minimum of x1 and x2, element-wise. Returns scalar if both x1 and x2 are scalars.
    """
    if isinstance(x1, list):
        x1 = array(x1)
    if isinstance(x2, list):
        x2 = array(x2)
    if type(x1) is NDArray:
        return NDArray(ArrayMath.fmin(x1.asarray(), x2.asarray()))
    elif isinstance(x1, DimArray):
        r = NDArray(ArrayMath.fmin(x1.asarray(), x2.asarray()))
        return DimArray(r, x1.dims, x1.fill_value, x1.proj)
    else:
        return min(x1, x2)
        
def min(a, axis=None):
    '''
    Returns the minimum values along an axis.
    
    :param a: (*array_like*) Input array.
    :param axis: (*int*) By default, the minimum is into the flattened array, otherwise 
        along the specified axis.
        
    :returns: Array of minimum values. It has the same shape as a.shape with the 
        dimension along axis removed.
    '''
    if isinstance(a, (list, tuple)):
        a = array(a)
    return a.min(axis)
    
def max(a, axis=None):
    '''
    Returns the maximum values along an axis.
    
    :param a: (*array_like*) Input array.
    :param axis: (*int*) By default, the maximum is into the flattened array, otherwise 
        along the specified axis.
        
    :returns: Array of maximum values. It has the same shape as a.shape with the 
        dimension along axis removed.
    '''
    if isinstance(a, (list, tuple)):
        a = array(a)
    return a.max(axis)
        
def argmin(a, axis=None):
    '''
    Returns the indices of the minimum values along an axis.
    
    :param a: (*array_like*) Input array.
    :param axis: (*int*) By default, the index is into the flattened array, otherwise 
        along the specified axis.
        
    :returns: Array of indices into the array. It has the same shape as a.shape with the 
        dimension along axis removed.
    '''
    if axis is None:
        r = ArrayMath.argMin(a.asarray())
        return r
    else:
        r = ArrayMath.argMin(a.asarray(), axis)
        return NDArray(r)
        
def argmax(a, axis=None):
    '''
    Returns the indices of the minimum values along an axis.
    
    :param a: (*array_like*) Input array.
    :param axis: (*int*) By default, the index is into the flattened array, otherwise 
        along the specified axis.
        
    :returns: Array of indices into the array. It has the same shape as a.shape with the 
        dimension along axis removed.
    '''
    if axis is None:
        r = ArrayMath.argMax(a.asarray())
        return r
    else:
        r = ArrayMath.argMax(a.asarray(), axis)
        return NDArray(r)
        
def diff(a, axis=-1):
    '''
    Calculate the n-th discrete difference along the given axis.
    
    The first difference is given by out[n] = a[n+1] - a[n] along the given axis.
    
    :param a: (*array_like*) Input array.
    :param axis: (*int*) The axis along which the difference is taken, default is the last axis.
        
    :returns: The n-th differences. The shape of the output is the same as a except along axis 
        where the dimension is smaller by n.
    '''
    a = asarray(a)
    nd = a.ndim
    if axis < 0:
        axis = nd + axis
    
    slice1 = [slice(None)] * nd
    slice2 = [slice(None)] * nd
    slice1[axis] = slice(1, None)
    slice2[axis] = slice(None, -1)
    slice1 = tuple(slice1)
    slice2 = tuple(slice2)
    r = a[slice1] - a[slice2]
    return r
        
def unravel_index(indices, dims):
    '''
    Converts a flat index or array of flat indices into a tuple of coordinate arrays.
    
    :param indices: (*array_like*) An integer array whose elements are indices into the 
        flattened version of an array of dimensions ``dims``.
    :param dims: (*tuple of ints*) The shape of the array to use for unraveling indices.
    
    :returns: tuple of ndarray. Each array in the tuple has the same shape as the indices 
        array.
    '''
    if isinstance(indices, int):
        idx = indices
        coords = []
        for i in range(len(dims)):
            if i < len(dims) - 1:
                n = 1
                for j in range(i + 1, len(dims)):
                    n = n * dims[j]
                coord = idx / n
                coords.append(coord)
                idx = idx - coord * n
            else:
                coords.append(idx)
        return tuple(coords)

def ave_month(data, colnames, t):
    """
    Average data month by month.
    
    :param data: (*list of Array*) Data array list.
    :param colnames: (*list of string*) Column name list.
    :param t: (*list of datetime*) Datetime list.
    
    :returns: (*PyTableData*) Averaged table data.
    """
    jt = miutil.jdate(t)
    if isinstance(data, NDArray):
        a = [data.asarray()]
    else:
        a = []
        for d in data:
            a.append(d.asarray())
    r = TableUtil.ave_Month(a, colnames, jt)
    return PyTableData(TableData(r))
    
def histogram(a, bins=10, density=False):
    '''
    Compute the histogram of a set of data.
    
    :param a: (*array_like*) Input data. The histogram is computed over the flattened array.
    :param bins: (*int or list*) If bins is an int, it defines the number of equal-width bins in the given 
        range (10, by default). If bins is a sequence, it defines the bin edges, including the rightmost edge, allowing for non-uniform bin widths.
    :param density: (*boolean*) If False, the result will contain the number of samples in 
        each bin. If True, the result is the value of the probability density function at 
        the bin, normalized such that the integral over the range is 1.
    
    :returns: The values of the histogram (hist) and the bin edges (length(hist)+1).
    '''
    if isinstance(a, list):
        a = array(a)
    elif isinstance(a, numbers.Number):
        a = array([a])
    if isinstance(bins, list):
        bins = array(bins)
    if isinstance(bins, int):
        r = ArrayUtil.histogram(a.asarray(), bins)
    else:
        r = ArrayUtil.histogram(a.asarray(), bins.asarray())
    h = NDArray(r[0])
    b = NDArray(r[1])
    
    if density:
        db = diff(b).astype('float')
        return h / db / h.sum(), b
    else:
        return h, b
                
def sort(a, axis=-1):
    """
    Returns the indices that would sort an array.
    
    :param a: (*array_like*) Array to be sorted.
    :param axis: (*int or None*) Optional. Axis along which to sort. If None, the array is
        flattened after sorting. The default is ``-1`` , which sorts along the last axis.
        
    :returns: (*NDArray*) Sorted array.
    """
    if isinstance(a, list):
        a = array(a)
    r = ArrayUtil.sort(a.asarray(), axis)
    return NDArray(r)
    
def argsort(a, axis=-1):
    """
    Return a sorted copy of an array.
    
    :param a: (*array_like*) Array to be sorted.
    :param axis: (*int or None*) Optional. Axis along which to sort. If None, the array is
        flattened after sorting. The default is ``-1`` , which sorts along the last axis.
        
    :returns: (*NDArray*) Array of indices that sort a along the specified axis. If a is 
        one-dimensional, a[index_array] yields a sorted a.
    """
    if isinstance(a, list):
        a = array(a)
    r = ArrayUtil.argSort(a.asarray(), axis)
    return NDArray(r)

def unique(a, axis=None):
    """
    Find the unique elements of an array.

    Returns the sorted unique elements of an array.

    :param a: (*array_like*) Array to be sorted.
    :param axis: (*int or None*) Optional. Axis along which to operate on. If None, the array is
        flattened.

    :returns: (*NDArray*) Sorted unique elements of input array.
    """
    if isinstance(a, list):
        a = array(a)
    r = ArrayUtil.unique(a.asarray(), axis)
    return NDArray(r)
    
def isnan(a):
    '''
    Test element-wise for NaN and return result as a boolean array.
    
    :param a: (*array_like*) Input array.
    
    :returns: (*array*) For scalar input, the result is a new boolean with value True if the input is NaN; 
        otherwise the value is False. For array input, the result is a boolean array of the same dimensions 
        as the input and the values are True if the corresponding element of the input is NaN; otherwise the 
        values are False.
    '''
    if isinstance(a, (list, tuple)):
        a = array(a)
    if isarray(a):
        return a == nan
    else:
        return Double.isNaN(a)

def isinf(x):
    '''
    Test element-wise for positive or negative infinity.

    :param a: (*array_like*) Input array.

    :returns: (*array*) True where x is positive or negative infinity, false otherwise. This is a scalar if x
        is a scalar.
    '''
    if isinstance(x, (list, tuple)):
        x = array(x)
    if isarray(x):
        return ArrayMath.isInfinite(x._array)
    else:
        return Double.isInfinite(a)

def isfinite(x):
    '''
    Test element-wise for finiteness (not infinity or not Not a Number).

    :param a: (*array_like*) Input array.

    :returns: (*array*) True where x is not positive infinity, negative infinity, or NaN; false otherwise. This is a
        scalar if x is a scalar.
    '''
    if isinstance(x, (list, tuple)):
        x = array(x)
    if isarray(x):
        return ArrayMath.isFinite(x._array)
    else:
        return Double.isFinite(a)

def delnan(a):
    '''
    Delete NaN values.
    
    :param a: (*arrays*) Input arrays with one dimension.
    
    :returns: The array or arrays without NaN values.
    '''
    if isinstance(a, (list, tuple))and (not isinstance(a[0], NDArray)):
        a = array(a)
    if isinstance(a, NDArray):
        r = ArrayMath.removeNaN(a._array)[0]
        return NDArray(r)
    else:
        aa = []
        for a0 in a:
            if not isinstance(a0, NDArray):
                a0 = array(a0)
            aa.append(a0._array)
        r = ArrayMath.removeNaN(aa)
        rr = []
        for r1 in r:
            rr.append(NDArray(r1))
        return rr
    
def nonzero(a):
    '''
    Return the indices of the elements that are non-zero.
    
    Returns a tuple of arrays, one for each dimension of a, containing the indices of the 
    non-zero elements in that dimension.
        
    :param a: (*array_like*) Input array.
    
    :returns: (*tuple*) Indices of elements that are non-zero.
    '''
    if isinstance(a, list):
        a = array(a)
    ra = ArrayMath.nonzero(a.asarray())
    if ra is None:
        return None
        
    r = []
    for aa in ra:
        r.append(NDArray(aa))
    return tuple(r)

def flatnonzero(a):
    '''
    Return indices that are non-zero in the flattened version of a.

    :param a: (*array_like*) Input array.

    :returns: (*array*) Indices of elements that are non-zero.
    '''
    if isinstance(a, list):
        a = array(a)
    r = ArrayMath.flatNonZero(a.asarray())

    return NDArray(r)
    
def where(condition):
    '''
    Return elements, either from x or y, depending on condition.

    If only condition is given, return condition.nonzero().
    
    :param condition: (*array_like*) Input array.
    
    :returns: (*tuple*) Indices of elements that are non-zero.
    '''
    return nonzero(condition)

def logical_not(arr):
    '''
    Compute the truth value of NOT x element-wise.

    :param arr: (*array_like*) Input array.
    :return: (*array_like*) Boolean result with the same shape as x of the NOT operation on elements of x.
        This is a scalar if x is a scalar.
    '''
    if isinstance(arr, int):
        return arr == 0
    elif isinstance(arr, bool):
        return not arr

    if isinstance(arr, (list, tuple)):
        arr = array(arr)
    r = ArrayMath.logicalNot(arr._array)
    return NDArray(r)
    
def delete(arr, obj, axis=None):
    '''
    Return a new array with sub-arrays along an axis deleted.
    
    :param arr: (*array_like*) Input array.
    :param obj: (*slice, int or array of ints*) Indicate which sub-arrays to remove.
    :param axis: (*int*) The axis along which to delete the subarray defined by obj. 
        If axis is None, obj is applied to the flattened array.
        
    :returns: A copy of arr with the elements specified by obj removed. If axis is None, 
        out is a flattened array.
    '''
    if isinstance(arr, (list, tuple)):
        arr = array(arr)
    
    if isinstance(obj, NDArray):
        obj = obj.aslist()
        
    if axis is None:
        arr = arr.reshape(arr.size)
        axis = 0
    
    r = ArrayUtil.delete(arr._array, obj, axis)
    return NDArray(r)
    
def concatenate(arrays, axis=0):
    '''
    Join a sequence of arrays along an existing axis.
    
    :param arrays: (list of arrays) The arrays must have the same shape, except in the dimension 
        corresponding to axis (the first, by default).
    :param axis: (*int*) The axis along which the arrays will be joined. Default is 0.
    
    :returns: (*array_like*) The concatenated array.
    '''
    ars = []
    for a in arrays:
        ars.append(a.asarray())
    r = ArrayUtil.concatenate(ars, axis)
    return NDArray(r)

def array_split(ary, indices_or_sections, axis=0):
    '''
    Split an array into multiple sub-arrays.

    :param ary: (*array*) Array to be divided into sub-arrays.
    :param indices_or_sections: (*int or 1-D array*) If indices_or_sections is an integer, N, the array
        will be divided into N equal arrays along axis. If indices_or_sections is a 1-D array of sorted
        integers, the entries indicate where along axis the array is split.
    :param axis: (*int*) Array to be divided into sub-arrays.
    :return:
    '''
    if axis < 0:
        axis = ary.ndim + axis
    n = ary.shape[axis]
    key = [slice(None)] * ary.ndim
    si = 0
    r = []
    if isinstance(indices_or_sections, int):
        sn = int(n / indices_or_sections)
        sns = [sn] * indices_or_sections
        m = n % indices_or_sections
        if m > 0:
            for i in range(m):
                sns[i] = sn + 1
        for sn in sns:
            key[axis] = slice(si, si + sn, 1)
            r.append(ary[tuple(key)])
            si += sn
    else:
        for idx in indices_or_sections:
            if idx > n:
                idx = n
            key[axis] = slice(si, idx, 1)
            r.append(ary[tuple(key)])
            si = idx
        key[axis] = slice(si, n, 1)
        r.append(ary[tuple(key)])
    return r

def split(ary, indices_or_sections, axis=0):
    '''
    Split an array into multiple sub-arrays.

    :param ary: (*array*) Array to be divided into sub-arrays.
    :param indices_or_sections: (*int or 1-D array*) If indices_or_sections is an integer, N, the array
        will be divided into N equal arrays along axis. If indices_or_sections is a 1-D array of sorted
        integers, the entries indicate where along axis the array is split.
    :param axis: (*int*) Array to be divided into sub-arrays.
    :return:
    '''
    if axis < 0:
        axis = ary.ndim + axis
    n = ary.shape[axis]
    if isinstance(indices_or_sections, int):
        if n % indices_or_sections:
            raise ValueError('array split does not result in an equal division')
    return array_split(ary, indices_or_sections, axis)

def atleast_1d(*args):
    '''
    View inputs as arrays with at least one dimensions.
    
    Parameters
    ----------
    args1, args2, ... : array_like
        One or more array-like sequences. 
        
    Returns
    -------
    res, res2, ... : array_like
        An array, or list of arrays, each with ``a.ndim >= 1``.
    '''
    res = []
    for arg in args:
        arg = array(arg)
        if arg.ndim == 0:
            result = arg.reshape(1)
        else:
            result = arg
        res.append(result)
    if len(res) == 1:
        return res[0]
    else:
        return res

    
def atleast_2d(*args):
    '''
    View inputs as arrays with at least two dimensions.
    
    Parameters
    ----------
    args1, args2, ... : array_like
        One or more array-like sequences.  Non-array inputs are converted
        to arrays.  Arrays that already have two or more dimensions are
        preserved.
    Returns
    -------
    res, res2, ... : array_like
        An array, or list of arrays, each with ``a.ndim >= 2``.
        Copies are avoided where possible, and views with two or more
        dimensions are returned.
    '''
    res = []
    for arg in args:
        arg = array(arg)
        if arg.ndim == 0:
            result = arg.reshape(1, 1)
        elif arg.ndim == 1:
            result = arg.reshape(1,len(arg))
        else:
            result = arg
        res.append(result)
    if len(res) == 1:
        return res[0]
    else:
        return res
        
def vstack(tup):
    '''
    Stack arrays in sequence vertically (row wise).
    
    This is equivalent to concatenation along the first axis after 1-D arrays
    of shape `(N,)` have been reshaped to `(1,N)`.
    
    :param tup: (*tuple*) Sequence of array. The arrays must have the same shape 
        along all but the first axis. 1-D arrays must have the same length.
        
    :returns: (*array*) The array formed by stacking the given arrays, will be 
        at least 2-D.
    '''
    return concatenate([atleast_2d(_m) for _m in tup], 0)
    
def hstack(tup):
    '''
    Stack arrays in sequence horizontally (column wise).

    This is equivalent to concatenation along the second axis, except for 1-D
    arrays where it concatenates along the first axis.
    
    :param tup: (*tuple*) Sequence of array. The arrays must have the same shape 
        along all but the first axis. 1-D arrays must have the same length.
        
    :returns: (*array*) The array formed by stacking the given arrays.
    '''
    arrs = [atleast_1d(_m) for _m in tup]
    # As a special case, dimension 0 of 1-dimensional arrays is "horizontal"
    if arrs and arrs[0].ndim == 1:
        return concatenate(arrs, 0)
    else:
        return concatenate(arrs, 1)

                
def dot(a, b):
    """
    Matrix multiplication.
    
    :param a: (*2D Array*) Matrix a.
    :param b: (*2D or 1D Array*) Matrix or vector b.
    
    :returns: Result Matrix or vector.
    """
    if isinstance(a, (int, long, float, complex)) and isinstance(b, (int, long, float, complex)):
        return a * b
        
    if isinstance(a, list):
        a = array(a)
    if isinstance(b, list):
        b = array(b)
    r = ArrayMath.dot(a.asarray(), b.asarray())
    return NDArray(r)
    
def vdot(a, b):
    '''
    Return the dot product of two vectors.
    
    Note that ``vdot`` handles multidimensional arrays differently than dot: it does not 
    perform a matrix product, but flattens input arguments to 1-D vectors first. 
    Consequently, it should only be used for vectors.
    
    :param a: (*array_like*) First argument to the dot product.
    :param b: (*array_like*) Second argument to the dot product.
    
    :returns: (*float*) Dot product of ``a`` and ``b``.    
    '''
    if isinstance(a, list):
        a = array(a)
    if isinstance(b, list):
        b = array(b)
    if a.ndim > 1:
        a = a.flatten()
    if b.ndim > 1:
        b = b.flatten()
    return ArrayMath.vdot(a.asarray(), b.asarray())

def shape(a):
    '''
    Return the shape of an array.

    :param a: (*array_like*) Input array.

    :return: (*tuple*) The elements of the shape tuple give the lengths of the corresponding array dimensions.
    '''
    try:
        result = a.shape
    except AttributeError:
        result = asarray(a).shape
    return result
        
def reshape(a, *args):
    """
    Gives a new shape to an array without changing its data.
    
    :param a: (*array_like*) Array to be reshaped.
    :param shape: (*int or tuple of ints*) The new shape should be compatible with the original 
        shape. If an integer, then the result will be a 1-D array of that length. One shape 
        dimension can be -1. In this case, the value is inferred from the length of the array and 
        remaining dimensions.
        
    :returns: Reshaped array.
    """
    return a.reshape(*args)
    
def squeeze(a):
    '''
    Remove single-dimensional entries from the shape of an array.
    
    :param a: (*array_like*) Input data array.
    
    :returns: (*array_like*) The input array, but with all or a subset of the dimensions of length 1 
        removed.
    '''
    da = a.asarray()
    da = da.reduce()
    if type(a) is NDArray:
        return NDArray(da)
    else:
        dims = []
        for dim in a.dims:
            if dim.getLength() > 1:
                dims.append(dim)
        return DimArray(NDArray(da), dims, a.fill_value, a.proj)

def ravel(a):
    '''
    Return a contiguous flattened array.

    :param a: (*array*) Input array.
    :return: A contiguous flattened array.
    '''
    return a.ravel()
        
def meshgrid(*args):
    '''
    Return coordinate matrices from coordinate vectors.

    Make N-D coordinate arrays for vectorized evaluations of N-D scalar/vector fields 
    over N-D grids, given one-dimensional coordinate arrays x1, x2,…, xn.

    :param x1,x2...xn: (*array_like*) 1-D arrays representing the coordinates of a grid.. 
    
    :returns X1,X2...XN: For vectors x1, x2,…, ‘xn’ with lengths Ni=len(xi) , 
        return (N1, N2, N3,...Nn) shaped arrays
    '''
    xs = []
    for x in args:
        if isinstance(x, list):
            x = array(x)
        if x.ndim != 1:
            print 'The paramters must be vector arrays!'
            return None
        xs.append(x._array)

    ra = ArrayUtil.meshgrid(xs)
    rs = []
    for r in ra:
        rs.append(NDArray(r))
    return tuple(rs)
    
def meshgrid_bak(*args):
    '''
    Return coordinate matrices from coordinate vectors.

    Make N-D coordinate arrays for vectorized evaluations of N-D scalar/vector fields 
    over N-D grids, given one-dimensional coordinate arrays x1, x2,…, xn.

    :param x1,x2...xn: (*array_like*) 1-D arrays representing the coordinates of a grid.. 
    
    :returns X1,X2...XN: For vectors x1, x2,…, ‘xn’ with lengths Ni=len(xi) , 
        return (N1, N2, N3,...Nn) shaped arrays
    '''
    if isinstance(x, list):
        x = array(x)
    if isinstance(y, list):
        y = array(y)
        
    if x.ndim != 1 or y.ndim != 1:
        print 'The paramters must be vector arrays!'
        return None
        
    xa = x.asarray()
    ya = y.asarray()
    ra = ArrayUtil.meshgrid(xa, ya)
    return NDArray(ra[0]), NDArray(ra[1])
    
def broadcast_to(a, shape):
    """
    Broadcast an array to a new shape.
    
    :param a: (*array_like*) The array to broadcast.
    :param shape: (*tuple*) The shape of the desired array.
    
    :returns: (*NDArray*) A readonly view on the original array with the given shape.
    """
    if isinstance(a, numbers.Number):
        return full(shape, a)

    if isinstance(a, list):
        a = array(a)
    r = ArrayUtil.broadcast(a.asarray(), shape)
    if r is None:
        raise ValueError('Can not broadcast to the shape!')
    return NDArray(r)
    
def corrcoef(x, y):
    """
    Return Pearson product-moment correlation coefficients.
    
    :param x: (*array_like*) A 1-D or 2-D array containing multiple variables and observations. 
        Each row of x represents a variable, and each column a single observation of all those 
        variables.
    :param y: (*array_like*) An additional set of variables and observations. y has the same 
        shape as x.
        
    :returns: The correlation coefficient matrix of the variables.
    """
    if isinstance(x, list):
        x = array(x)
    if isinstance(y, list):
        y = array(y)
    a = ArrayMath.getR(x.asarray(), y.asarray())
    b = ArrayMath.getR(y.asarray(), x.asarray())
    r = array([[1, a], [b, 1]])
    return r

def transpose(a, axes=None):
    '''
    Transpose 2-D array.
    
    :param a: (*array*) 2-D array to be transposed.
    :param axes: (*list of int*) By default, reverse the dimensions, otherwise permute the axes according to the
            values given.
    
    :returns: Transposed array.
    '''
    if isinstance(a, (list, tuple)):
        a = array(a)
    return a.transpose(axes)

def swapaxes(a, axis1, axis2):
    '''
    Interchange two axes of an array.

    :param axis1: (*int*) First axis.
    :param axis2: (*int*) Second axis.

    :returns: Axes swapped array.
    '''
    return a.swapaxes(axis1, axis2)

def moveaxis(a, source, destination):
    """
    Move axes of an array to new positions.
    Other axes remain in their original order.
    .. versionadded:: 1.11.0
    Parameters
    ----------
    a : np.ndarray
        The array whose axes should be reordered.
    source : int or sequence of int
        Original positions of the axes to move. These must be unique.
    destination : int or sequence of int
        Destination positions for each of the original axes. These must also be
        unique.
    Returns
    -------
    result : np.ndarray
        Array with moved axes. This array is a view of the input array.
    See Also
    --------
    transpose: Permute the dimensions of an array.
    swapaxes: Interchange two axes of an array.
    Examples
    --------
    >>> x = np.zeros((3, 4, 5))
    >>> np.moveaxis(x, 0, -1).shape
    (4, 5, 3)
    >>> np.moveaxis(x, -1, 0).shape
    (5, 3, 4)
    These all achieve the same result:
    >>> np.transpose(x).shape
    (5, 4, 3)
    >>> np.swapaxes(x, 0, -1).shape
    (5, 4, 3)
    >>> np.moveaxis(x, [0, 1], [-1, -2]).shape
    (5, 4, 3)
    >>> np.moveaxis(x, [0, 1, 2], [-1, -2, -3]).shape
    (5, 4, 3)
    """
    try:
        # allow duck-array types if they define transpose
        transpose = a.transpose
    except AttributeError:
        a = asarray(a)
        transpose = a.transpose

    source = normalize_axis_tuple(source, a.ndim, 'source')
    destination = normalize_axis_tuple(destination, a.ndim, 'destination')
    if len(source) != len(destination):
        raise ValueError('`source` and `destination` arguments must have '
                         'the same number of elements')

    order = [n for n in range(a.ndim) if n not in source]

    for dest, src in sorted(zip(destination, source)):
        order.insert(dest, src)

    result = transpose(order)
    return result

def rot90(a, k=1):
    """
    Rotate an array by 90 degrees in the counter-clockwise direction. The first two dimensions
    are rotated if the array has more than 2 dimensions.
    
    :param a: (*array_like*) Array for rotate.
    :param k: (*int*) Number of times the array is rotated by 90 degrees
    
    :returns: (*array_like*) Rotated array.
    """
    r = ArrayMath.rot90(a.asarray(), k)
    if type(a) is NDArray:
        return NDArray(r)
    else:
        dims = []
        if Math.abs(k) == 1 or Math.abs(k) == 3:
            dims.append(a.dims[1])
            dims.append(a.dims[0])
            for i in range(2, len(a.dims)):            
                dims.append(a.dims[i])
        else:
            for i in range(0, len(a.dims)):
                dims.append(a.dims[i])
        return DimArray(NDArray(r), dims, a.fill_value, a.proj) 
        
def trapz(y, x=None, dx=1.0, axis=-1):
    """
    Integrate along the given axis using the composite trapezoidal rule.
    
    :param y: (*array_like*) Input array to integrate.
    :param x: (*array_like*) Optional, If x is None, then spacing between all y elements is dx.
    :param dx: (*scalar*) Optional, If x is None, spacing given by dx is assumed. Default is 1.
    :param axis: (*int*) Optional, Specify the axis.
    
    :returns: Definite integral as approximated by trapezoidal rule.
    """
    if isinstance(y, list):
        y = array(y)
    
    if y.ndim == 1:
        if x is None:
            r = ArrayMath.trapz(y.asarray(), dx)
        else:
            if isinstance(x, list):
                x = array(x)
            r = ArrayMath.trapz(y.asarray(), x.asarray())
        return r
    else:
        if axis == -1:
            shape = y.shape
            for i in range(y.ndim):
                if shape[i] > 1:
                    axis = i
                    break
        if x is None:
            r = ArrayMath.trapz(y.asarray(), dx, axis)
        else:
            if isinstance(x, list):
                x = array(x)
            r = ArrayMath.trapz(y.asarray(), x.asarray(), axis)
        if type(y) is NDArray:
            return NDArray(r)
        else:
            dims = []
            for i in range(0, y.ndim):
                if i != axis:
                    dims.append(y.dims[i])
            return DimArray(NDArray(r), dims, y.fill_value, y.proj)
            
def rolling_mean(x, window, center=False):
    '''
    Moving average function
    
    :param x: (*array_like*) Input data array. Must be vector (one dimension).
    :param window: (*int*) Size of the moving window.
    :param center: (*boolean*) Set the labels at the center of the window. Default is ``False``.
    
    :returns: (*array_like*) Moving averaged array.
    '''
    if isinstance(x, list):
        x = array(x)
    r = ArrayMath.rolling_mean(x.asarray(), window, center)
    return NDArray(r)

def roll(a, shift, axis=None):
    """
    Roll array elements along a given axis.
    Elements that roll beyond the last position are re-introduced at
    the first.
    Parameters
    ----------
    a : array_like
        Input array.
    shift : int or tuple of ints
        The number of places by which elements are shifted.  If a tuple,
        then `axis` must be a tuple of the same size, and each of the
        given axes is shifted by the corresponding number.  If an int
        while `axis` is a tuple of ints, then the same value is used for
        all given axes.
    axis : int or tuple of ints, optional
        Axis or axes along which elements are shifted.  By default, the
        array is flattened before shifting, after which the original
        shape is restored.
    Returns
    -------
    res : ndarray
        Output array, with the same shape as `a`.
    See Also
    --------
    rollaxis : Roll the specified axis backwards, until it lies in a
               given position.
    Notes
    -----
    .. versionadded:: 1.12.0
    Supports rolling over multiple dimensions simultaneously.
    Examples
    --------
    >>> x = np.arange(10)
    >>> np.roll(x, 2)
    array([8, 9, 0, 1, 2, 3, 4, 5, 6, 7])
    >>> np.roll(x, -2)
    array([2, 3, 4, 5, 6, 7, 8, 9, 0, 1])
    >>> x2 = np.reshape(x, (2,5))
    >>> x2
    array([[0, 1, 2, 3, 4],
           [5, 6, 7, 8, 9]])
    >>> np.roll(x2, 1)
    array([[9, 0, 1, 2, 3],
           [4, 5, 6, 7, 8]])
    >>> np.roll(x2, -1)
    array([[1, 2, 3, 4, 5],
           [6, 7, 8, 9, 0]])
    >>> np.roll(x2, 1, axis=0)
    array([[5, 6, 7, 8, 9],
           [0, 1, 2, 3, 4]])
    >>> np.roll(x2, -1, axis=0)
    array([[5, 6, 7, 8, 9],
           [0, 1, 2, 3, 4]])
    >>> np.roll(x2, 1, axis=1)
    array([[4, 0, 1, 2, 3],
           [9, 5, 6, 7, 8]])
    >>> np.roll(x2, -1, axis=1)
    array([[1, 2, 3, 4, 0],
           [6, 7, 8, 9, 5]])
    """
    a = asanyarray(a)
    if axis is None:
        return roll(a.ravel(), shift, 0).reshape(a.shape)

    else:
        axis = normalize_axis_tuple(axis, a.ndim, allow_duplicate=True)
        broadcasted = broadcast_to(shift, asanyarray(axis).shape)
        shifts = {ax: 0 for ax in range(a.ndim)}
        for ax, sh in zip(axis, broadcasted):
            shifts[ax] += sh

        rolls = [((slice(None), slice(None)),)] * a.ndim
        for ax, offset in shifts.items():
            offset %= a.shape[ax] or 1  # If `a` is empty, nothing matters.
            if offset:
                # (original, result), (original, result)
                rolls[ax] = ((slice(None, -offset), slice(offset, None)),
                             (slice(-offset, None), slice(None, offset)))

        result = empty_like(a)
        for indices in itertools.product(*rolls):
            arr_index, res_index = zip(*indices)
            result[res_index] = a[arr_index]

        return result

def normalize_axis_index(axis, ndim, argname=None):
    """
    Normalizes an axis index.

    axis : int, iterable of int
        The un-normalized index or indices of the axis.
    ndim : int
        The number of dimensions of the array that `axis` should be normalized
        against.
    argname : str, optional
        A prefix to put before the error message, typically the name of the
        argument.
    Returns
    -------
    normalized_axes : tuple of int
        The normalized axis index, such that `0 <= normalized_axis < ndim`
    """
    if axis >= ndim or axis + ndim < 0:
        if argname:
            raise ValueError('AxisError: {}: axis {} is out of bounds for array of dimension {}'.format(argname, axis, ndim))
        else:
            raise ValueError('AxisError: axis {} is out of bounds for array of dimension {}'.format(axis, ndim))

    return axis if axis >= 0 else ndim + axis

def normalize_axis_tuple(axis, ndim, argname=None, allow_duplicate=False):
    """
    Normalizes an axis argument into a tuple of non-negative integer axes.
    This handles shorthands such as ``1`` and converts them to ``(1,)``,
    as well as performing the handling of negative indices covered by
    `normalize_axis_index`.
    By default, this forbids axes from being specified multiple times.
    Used internally by multi-axis-checking logic.
    .. versionadded:: 1.13.0
    Parameters
    ----------
    axis : int, iterable of int
        The un-normalized index or indices of the axis.
    ndim : int
        The number of dimensions of the array that `axis` should be normalized
        against.
    argname : str, optional
        A prefix to put before the error message, typically the name of the
        argument.
    allow_duplicate : bool, optional
        If False, the default, disallow an axis from being specified twice.
    Returns
    -------
    normalized_axes : tuple of int
        The normalized axis index, such that `0 <= normalized_axis < ndim`
    Raises
    ------
    AxisError
        If any axis provided is out of range
    ValueError
        If an axis is repeated
    See also
    --------
    normalize_axis_index : normalizing a single scalar axis
    """
    # Optimization to speed-up the most common cases.
    if type(axis) not in (tuple, list):
        try:
            axis = [operator.index(axis)]
        except TypeError:
            pass
    # Going via an iterator directly is slower than via list comprehension.
    axis = tuple([normalize_axis_index(ax, ndim, argname) for ax in axis])
    if not allow_duplicate and len(set(axis)) != len(axis):
        if argname:
            raise ValueError('repeated axis in `{}` argument'.format(argname))
        else:
            raise ValueError('repeated axis')
    return axis
    
def smooth5(x):
    '''
    Performs a 5 point smoothing to the 2D array x. 
    
    The result at each grid point is a weighted average of the grid point plus the 4 
    surrounding points. The center point receives a wieght of 1.0, the points at each side 
    and above and below receive a weight of 0.5.
    
    All 5 points are multiplied by their weights and summed, then divided by the total 
    weight to obtain the smoothed value. Any missing data points are not included in the 
    sum; points beyond the grid boundary are considered to be missing. Thus the final result 
    may be the result of an averaging with less than 5 points.
    
    :param x: (*array_like*) Input 2D array.
    
    :returned: (*array*) Smoothed 2D array.
    '''
    if isinstance(x, list):
        x = array(x)
    if x.ndim != 2:
        print 'The array must be 2 dimension!'
        raise ValueError()
    r = ArrayUtil.smooth5(x._array)
    if isinstance(x, DimArray):
        return DimArray(r, x.dims, x.fill_value, x.proj)
    else:
        return NDArray(r)
        
def smooth9(x):
    '''
    Performs a 9 point smoothing to the 2D array x. 
    
    The result at each grid point is a weighted average of the grid point plus the 4 
    surrounding points. The center point receives a wieght of 1.0, the points at each side 
    and above and below receive a weight of 0.5, and corner points receive a weight of 0.3.
    
    All 9 points are multiplied by their weights and summed, then divided by the total 
    weight to obtain the smoothed value. Any missing data points are not included in the 
    sum; points beyond the grid boundary are considered to be missing. Thus the final result 
    may be the result of an averaging with less than 9 points.
    
    :param x: (*array_like*) Input 2D array.
    
    :returned: (*array*) Smoothed 2D array.
    '''
    if isinstance(x, list):
        x = array(x)
    if x.ndim != 2:
        print 'The array must be 2 dimension!'
        raise ValueError()
    r = ArrayUtil.smooth9(x._array)
    if isinstance(x, DimArray):
        return DimArray(r, x.dims, x.fill_value, x.proj)
    else:
        return NDArray(r)
 
def cdiff(a, dimidx):
    '''
    Performs a centered difference operation on a array in a specific direction
    
    :param a: (*array*) The input array.
    :param dimidx: (*int*) Demension index of the specific direction.
    
    :returns: Result array.
    '''
    if isinstance(a, DimArray):
        r = ArrayMath.cdiff(a.asarray(), dimidx)
        return DimArray(NDArray(r), a.dims, a.fill_value, a.proj)
    else:
        return NDArray(ArrayMath.cdiff(a.asarray(), dimidx))

# Calculates the vertical component of the curl (ie, vorticity)    
def hcurl(u, v):
    '''
    Calculates the vertical component of the curl (ie, vorticity). The data should be lon/lat projection.
    
    :param u: (*array*) U component array.
    :param v: (*array*) V component array.
    
    :returns: Array of the vertical component of the curl.
    '''
    if isinstance(u, DimArray) and isinstance(v, DimArray):
        ydim = u.ydim()
        xdim = u.xdim()
        r = ArrayMath.hcurl(u.asarray(), v.asarray(), xdim.getDimValue(), ydim.getDimValue())
        return DimArray(NDArray(r), u.dims, u.fill_value, u.proj)

#  Calculates the horizontal divergence using finite differencing        
def hdivg(u, v):
    '''
    Calculates the horizontal divergence using finite differencing. The data should be lon/lat projection.
    
    :param u: (*array*) U component array.
    :param v: (*array*) V component array.
    
    :returns: Array of the horizontal divergence.
    '''
    if isinstance(u, DimArray) and isinstance(v, DimArray):
        ydim = u.ydim()
        xdim = u.xdim()
        r = ArrayMath.hdivg(u.asarray(), v.asarray(), xdim.getDimValue(), ydim.getDimValue())
        return DimArray(NDArray(r), u.dims, u.fill_value, u.proj)
              
def magnitude(u, v):
    '''
    Performs the calculation: sqrt(u*u+v*v).
    
    :param u: (*array*) U component array.
    :param v: (*array*) V component array.
    
    :returns: Result array.
    '''
    if isinstance(u, DimArray) and isinstance(v, DimArray):
        r = ArrayMath.magnitude(u.asarray(), v.asarray())
        return DimArray(NDArray(r), u.dims, u.fill_value, u.proj)
    elif isinstance(u, NDArray) and isinstance(v, NDArray):
        r = ArrayMath.magnitude(u.asarray(), v.asarray())
        return NDArray(r)
    else:
        r = sqrt(u * u + v * v)
        return r

def asarray(data, dtype=None):
    '''
    Convert the array_like data to NDArray data.
    
    :param data: (*array_like*) The input data.
    :param dtype: (*datatype*) Data type.
    
    :returns: NDArray data.
    '''
    if isinstance(data, Array):
        data = NDArray(data)
    if isinstance(data, NDArray):
        if dtype is None:
            return data
        else:
            return a.astype(dtype)
    else:
        return array(data, dtype)

def asanyarray(data, dtype=None):
    '''
    Convert the array_like data to NDArray data.

    :param data: (*array_like*) The input data.
    :param dtype: (*datatype*) Data type.

    :returns: NDArray data.
    '''
    return asarray(data, dtype)

def asmiarray(data):
    '''
    Convert the array_like data to NDArray data.
    
    :param data: (*array_like*) The input data.
    
    :returns: NDArray data.
    '''
    if isinstance(data, Array):
        return NDArray(data)
    elif isinstance(data, NDArray):
        return data
    else:
        return array(data)       
        
def asgriddata(data, x=None, y=None, fill_value=-9999.0):
    if x is None:    
        if isinstance(data, PyGridData):
            return data
        elif isinstance(data, DimArray):
            return data.asgriddata()
        elif isinstance(data, NDArray):
            if x is None:
                x = arange(0, data.shape[1])
            if y is None:
                y = arange(0, data.shape[0])
            gdata = GridData(data._array, x._array, y._array, fill_value)
            return PyGridData(gdata)
        else:
            return None
    else:
        gdata = GridData(data.asarray(), x.asarray(), y.asarray(), fill_value)
        return PyGridData(gdata)
        
def asgridarray(data, x=None, y=None, fill_value=-9999.0):
    if x is None:    
        if isinstance(data, PyGridData):
            return data.data.toGridArray()
        elif isinstance(data, DimArray):
            return data.asgridarray()
        elif isinstance(data, NDArray):
            if x is None:
                x = arange(0, data.shape[1])
            if y is None:
                y = arange(0, data.shape[0])
            gdata = GridArray(data._array, x._array, y._array, fill_value)
            return gdata
        else:
            return None
    else:
        gdata = GridArray(data.asarray(), x.asarray(), y.asarray(), fill_value)
        return gdata
        
def asstationdata(data, x, y, fill_value=-9999.0):
    stdata = StationData(data.asarray(), x.asarray(), y.asarray(), fill_value)
    return PyStationData(stdata)
    
def interp2d(*args, **kwargs):
    """
    Interpolate over a 2-D grid.
    
    :param x: (*array_like*) X coordinate array of the sample points.
    :param y: (*array_like*) Y coordinate array of the sample points.
    :param z: (*array_like*) 2-D value array of the sample points.
    :param xq: (*array_like*) X coordinate array of the query points.
    :param yq: (*array_like*) Y coordinate array of the query points.
    :param kind: (*string*) The kind of the interpolation method. ['linear' | 'nearest'].
    
    :returns: (*array_like*) Interpolated array.
    """
    if len(args) == 3:
        z = args[0]
        x = z.dimvalue(1)
        y = z.dimvalue(0)
        xq = args[1]
        yq = args[2]
    else:
        x = args[0]
        y = args[1]
        z = args[2]
        xq = args[3]
        yq = args[4]
    x = array(x)._array
    y = array(y)._array
    z = array(z)._array
    xq = array(xq)._array
    yq = array(yq)._array
    kind = kwargs.pop('kind', 'linear')
    if kind == 'neareast':
        r = ArrayUtil.resample_Neighbor(z, x, y, xq, yq)
    else:
        r = ArrayUtil.resample_Bilinear(z, x, y, xq, yq)
    if r.getSize() == 1:
        return r.getDouble(0)
    else:
        return NDArray(r)

def interpn(points, values, xi):
    """
    Multidimensional interpolation on regular grids.
    
    :param points: (*list*) The points defining the regular grid in n dimensions.
    :param values: (*array_like*) The data on the regular grid in n dimensions.
    :param xi: (*array_like*) The coordinates to sample the gridded data at.
    
    :returns: (*float*) Interpolated value at input coordinates.
    """
    npoints = []
    for p in points:
        if isinstance(p, (list,tuple)):
            p = array(p)
        npoints.append(p._array)
        
    if isinstance(xi, (list, tuple)):
        if isinstance(xi[0], NDArray):
            nxi = []
            for x in xi:
                nxi.append(x._array)
        else:
            nxi = []
            for x in xi:
                if isinstance(x, datetime.datetime):
                    x = miutil.date2num(x)
                nxi.append(x)
            nxi = array(nxi)._array        
    else:
        nxi = nxi._array
    r = ArrayUtil.interpn(npoints, values._array, nxi)
    if isinstance(r, Array):
        return NDArray(r)
    else:
        return r
    
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
            z_g = linspace(z_s.min(), z_s.max(), zn)
        x_g = linspace(x_s.min(), x_s.max(), xn)
        y_g = linspace(y_s.min(), y_s.max(), yn)
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
        radius = kwargs.pop('radius', inf)
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
        r = InterpUtil.interpolation_Inside_Count(x_s.aslist(), y_s.aslist(), x_g.aslist(), y_g.aslist(), True, centerpoint)
        return NDArray(r[0]), x_g, y_g, NDArray(r[1])
    elif method == 'surface':        
        r = InterpUtil.interpolation_Surface(x_s.asarray(), y_s.asarray(), values, x_g.asarray(), y_g.asarray())
    else:
        return None
    
    convexhull = kwargs.pop('convexhull', False)
    if convexhull:
        polyshape = GeometryUtil.convexHull(x_s.asarray(), y_s.asarray())
        x_gg, y_gg = meshgrid(x_g, y_g)
        r = GeometryUtil.maskout(r, x_gg._array, y_gg._array, [polyshape])
        return NDArray(r), x_g, y_g
    else:
        return NDArray(r), x_g, y_g

def pol2cart(theta, rho):
    '''
    Transform polar coordinates to Cartesian
    
    :param theta: (*array_like*) Theta value in polar coordinates
    :param rho: (*array_like*) Rho value in polar coordinates
    
    :returns: x and y value in Cartesian coordinates
    '''
    if isinstance(theta, (int, float)):
        r = ArrayMath.polarToCartesian(theta, rho)
        return r[0], r[1]
    else:
        theta = array(theta)
        rho = array(rho)
        r = ArrayMath.polarToCartesian(theta._array, rho._array)
        return NDArray(r[0]), NDArray(r[1])
        
def cart2pol(x, y):
    '''
    Transform Cartesian coordinates to polar
    
    :param x: (*array_like*) X value in Cartesian coordinates
    :param y: (*array_like*) Y value in Cartesian coordinates
    
    :returns: Theta and rho value in polar coordinates
    '''
    if isinstance(x, (int, float)):
        r = ArrayMath.cartesianToPolar(x, y)
        return r[0], r[1]
    else:
        x = array(x)
        y = array(y)
        r = ArrayMath.cartesianToPolar(x._array, y._array)
        return NDArray(r[0]), NDArray(r[1])
    
def addtimedim(infn, outfn, t, tunit='hours'):
    '''
    Add a time dimension to a netCDF data file.
    
    :param infn: (*string*) Input netCDF file name.
    :param outfn: (*string*) Output netCDF file name.
    :param t: (*DateTime*) A time value.
    :param tunit: (*string*) Time unite, Default is ``hours``.
    
    :returns: The new netCDF with time dimension.
    '''
    cal = Calendar.getInstance()
    cal.set(t.year, t.month - 1, t.day, t.hour, t.minute, t.second)
    nt = cal.getTime()
    NetCDFDataInfo.addTimeDimension(infn, outfn, nt, tunit)
        
def joinncfile(infns, outfn, tdimname):
    '''
    Join several netCDF files to one netCDF file.
    
    :param infns: (*list*) Input netCDF file name list.
    :param outfn: (*string*) Output netCDF file name.
    :param tdimname: (*string*) Time dimension name.
    
    :returns: Joined netCDF file.
    '''
    NetCDFDataInfo.joinDataFiles(infns, outfn, tdimname)
    
# Get month abstract English name
def monthname(m):  
    '''
    Get month abstract English name.
    
    :param m: (*int*) Month number (1 to 12).
    '''
    mmm = 'jan'
    if m == 1:
        mmm = 'jan'
    elif m == 2:
        mmm = 'feb'
    elif m == 3:
        mmm = 'mar'
    elif m == 4:
        mmm = 'apr'
    elif m == 5:
        mmm = 'may'
    elif m == 6:
        mmm = 'jun'
    elif m == 7:
        mmm = 'jul'
    elif m == 8:
        mmm = 'aug'
    elif m == 9:
        mmm = 'sep'
    elif m == 10:
        mmm = 'oct'
    elif m == 11:
        mmm = 'nov'
    elif m == 12:
        mmm = 'dec'

    return mmm