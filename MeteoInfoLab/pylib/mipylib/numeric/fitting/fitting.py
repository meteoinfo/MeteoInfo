# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2017-3-9
# Purpose: MeteoInfoLab fitting module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.math.fitting import FittingUtil
from org.meteoinfo.data import ArrayMath, ArrayUtil

from mipylib.numeric.miarray import MIArray

__all__ = [
    'powerfit', 'expfit','polyfit','polyval','predict'
    ]

def powerfit(x, y, func=False):
    '''
    Power law fitting.
    
    :param x: (*array_like*) x data array.
    :param y: (*array_like*) y data array.
    :param func: (*boolean*) Return fit function (for predict function) or not. Default is ``False``.
    
    :returns: Fitting parameters and function (optional).
    '''
    if isinstance(x, list):
        x = MIArray(ArrayUtil.array(x))
    if isinstance(y, list):
        y = MIArray(ArrayUtil.array(y))
    r = FittingUtil.powerFit(x.asarray(), y.asarray())
    if func:
        return r[0], r[1], r[2], r[3]
    else:
        return r[0], r[1], r[2]
        
def expfit(x, y, func=False):
    '''
    Exponent fitting.
    
    :param x: (*array_like*) x data array.
    :param y: (*array_like*) y data array.
    :param func: (*boolean*) Return fit function (for predict function) or not. Default is ``False``.
    
    :returns: Fitting parameters and function (optional).
    '''
    if isinstance(x, list):
        x = MIArray(ArrayUtil.array(x))
    if isinstance(y, list):
        y = MIArray(ArrayUtil.array(y))
    r = FittingUtil.expFit(x.asarray(), y.asarray())
    if func:
        return r[0], r[1], r[2], r[3]
    else:
        return r[0], r[1], r[2]
        
def polyfit(x, y, degree, func=False):
    '''
    Polynomail fitting.
    
    :param x: (*array_like*) x data array.
    :param y: (*array_like*) y data array.
    :param degree: (*int*) Degree of the fitting polynomial.
    :param func: (*boolean*) Return fit function (for predict function) or not. Default is ``False``.
    
    :returns: Fitting parameters and function (optional).
    '''
    if isinstance(x, list):
        x = MIArray(ArrayUtil.array(x))
    if isinstance(y, list):
        y = MIArray(ArrayUtil.array(y))
    r = FittingUtil.polyFit(x.asarray(), y.asarray(), degree)
    if func:
        return r[0], r[1], r[2]
    else:
        return r[0], r[1]
        
def polyval(p, x):
    """
    Evaluate a polynomial at specific values.
    
    If p is of length N, this function returns the value:
    
    p[0]*x**(N-1) + p[1]*x**(N-2) + ... + p[N-2]*x + p[N-1]
    
    If x is a sequence, then p(x) is returned for each element of x. If x is another polynomial then the 
    composite polynomial p(x(t)) is returned.
    
    :param p: (*array_like*) 1D array of polynomial coefficients (including coefficients equal to zero) 
        from highest degree to the constant term.
    :param x: (*array_like*) A number, an array of numbers, or an instance of poly1d, at which to evaluate 
        p.
        
    :returns: Polynomial value
    """
    if isinstance(x, list):
        x = MIArray(ArrayUtil.array(x))
    return MIArray(ArrayMath.polyVal(p, x.asarray()))
    
def predict(func, x):
    '''
    Predict y value using fitting function and x value.
    
    :param func: (*Fitting function object*) Fitting function.
    :param x: (*float*) x value.
    
    :returns: (*float*) y value.
    '''
    if isinstance(x, (int, float, long)):
        return func.predict(x)
        
    if isinstance(x, list):
        x = MIArray(ArrayUtil.array(x))
    return MIArray(FittingUtil.predict(x.asarray(), func))