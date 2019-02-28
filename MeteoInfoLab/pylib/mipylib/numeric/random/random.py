# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2016-7-10
# Purpose: MeteoInfoLab random module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.math import RandomUtil

from mipylib.numeric.miarray import MIArray

__all__ = [
    'rand','randn','randint','poisson','seed'
    ]

def seed(seed=None):
    '''
    Seed the generator.
    
    :param seed: (*int*) Seed for random data generator.
    '''
    if seed is None:
        RandomUtil.useSeed = False
    else:
        RandomUtil.useSeed = True
        RandomUtil.seed = seed
    
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
        return RandomUtil.rand()
    elif len(args) == 1:
        return MIArray(RandomUtil.rand(args[0]))
    else:
        return MIArray(RandomUtil.rand(args))
        
def randn(*args):
    """
    Return a sample (or samples) from the “standard normal” distribution.
    
    Create an array of the given shape and propagate it with random samples from a "normal" 
        (Gaussian) distribution of mean 0 and variance 1.
    
    :param d0, d1, ..., dn: (*int*) optional. The dimensions of the returned array, should all
        be positive. If no argument is given a single Python float is returned.
        
    :returns: Random values array.
    """
    if len(args) == 0:
        return RandomUtil.randn()
    elif len(args) == 1:
        return MIArray(RandomUtil.randn(args[0]))
    else:
        return MIArray(RandomUtil.randn(args))
        
def randint(low, high=None, size=None):
    """
    Return random integers from low (inclusive) to high (exclusive).
    
    Return random integers from the “discrete uniform” distribution of the specified dtype in the “half-open” 
    interval [low, high). If high is None (the default), then results are from [0, low).
    
    :param low: (*int*) Lowest (signed) integer to be drawn from the distribution (unless high=None, in which 
        case this parameter is one above the highest such integer).
    :param high: (*int*) If provided, one above the largest (signed) integer to be drawn from the distribution 
        (see above for behavior if high=None).
    :param size: (*int or tuple*) Output shape. If the given shape is, e.g., (m, n, k), then m * n * k samples 
        are drawn. Default is None, in which case a single value is returned.
        
    :returns: (*int or array*) Random integer array.
    """
    if high is None:
        high = low
        low = 0
    else:
        high = high - low
    if size is None:
        r = RandomUtil.randint(high)
        r += low
    else:
        r = MIArray(RandomUtil.randint(high, size))
        if low != 0:
            r += low
    return r
    
def poisson(lam=1.0, size=None):
    """
    Draw samples from a Poisson distribution.
    
    :param lam: (*float*) Expectation of interval, should be >= 0.
    :param size: (*int or tuple*) Output shape. If the given shape is, e.g., (m, n, k), then m * n * k samples 
        are drawn. Default is None, in which case a single value is returned.
        
    :returns: (*float or array*) Drawn samples from the parameterized Poisson distribution.
    """
    if size is None:
        r = RandomUtil.poisson(lam)
    else:
        r = MIArray(RandomUtil.poisson(lam, size))
    return r