# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2016-7-10
# Purpose: MeteoInfoLab random module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.math.random import RandomUtil
from org.meteoinfo.math.distribution import DistributionUtil
from org.apache.commons.math3.distribution import NormalDistribution, BetaDistribution, \
    BinomialDistribution, ChiSquaredDistribution, ExponentialDistribution, FDistribution, \
    GammaDistribution, GumbelDistribution, LaplaceDistribution, LogisticDistribution, \
    LogNormalDistribution, ParetoDistribution, TDistribution, TriangularDistribution, \
    UniformRealDistribution, WeibullDistribution

from ..core import NDArray

__all__ = [
    'beta','binomial','chisquare','exponential','f','gamma','gumbel','laplace','logistic',
    'lognormal','normal','rand','randn','randint','random','pareto','poisson','seed','standard_t',
    'triangular','uniform','weibull'
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

def random(size=None):
    """
    Random values in a given shape.

    Create an array of the given shape and propagate it with random samples from a uniform
        distribution over [0, 1).

    :param size: (*int or tuple*) Output shape. If the given shape is, e.g., (m, n, k), then m * n * k samples
        are drawn. Default is None, in which case a single value is returned.

    :returns: Random values array.
    """
    if size is None:
        return RandomUtil.rand()
    else:
        return NDArray(RandomUtil.rand(size))

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
        return NDArray(RandomUtil.rand(args[0]))
    else:
        return NDArray(RandomUtil.rand(args))
        
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
        return NDArray(RandomUtil.randn(args[0]))
    else:
        return NDArray(RandomUtil.randn(args))
        
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
        r = NDArray(RandomUtil.randint(high, size))
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
        r = NDArray(RandomUtil.poisson(lam, size))
    return r
    
def normal(loc=0.0, scale=1.0, size=None):
    """
    Draw random samples from a normal (Gaussian) distribution.
    
    :param loc: (*float*) Mean (“centre”) of the distribution.
    :param scale: (*float*) Standard deviation (spread or “width”) of the distribution.
    :param size: (*int*) Output shape. If size is None (default), a single value is returned.
    
    """
    dist = NormalDistribution(loc, scale)
    if size is None:
        size = 1
    r = DistributionUtil.rvs(dist, size)
    return NDArray(r)
    
def chisquare(df, size=None):
    """
    Draw samples from a chi-square distribution.
    
    :param df: (*float*) Number of degrees of freedom, should be > 0.
    :param size: (*int*) Output shape. If size is None (default), a single value is returned.
    
    :returns: (*ndarray or scalar*) Drawn samples from the parameterized chisquare distribution.    
    """
    dist = ChiSquaredDistribution(df)
    if size is None:
        size = 1
    r = DistributionUtil.rvs(dist, size)
    return NDArray(r)
    
def exponential(scale=1.0, size=None):
    """
    Draw samples from a exponential distribution.
    
    :param scale: (*float*) The scale parameter.
    :param size: (*int*) Output shape. If size is None (default), a single value is returned.
    
    :returns: (*ndarray or scalar*) Drawn samples from the parameterized exponential distribution.    
    """
    dist = ExponentialDistribution(scale)
    if size is None:
        size = 1
    r = DistributionUtil.rvs(dist, size)
    return NDArray(r)
    
def f(dfnum, dfden, size=None):
    """
    Draw random samples from a F distribution.
    
    :param dfnum: (*float*) Degrees of freedom in numerator, should be > 0.
    :param dfden: (*float*) Degrees of freedom in denominator, should be > 0.
    :param size: (*int*) Output shape. If size is None (default), a single value is returned.
    
    :returns: (*ndarray or scalar*) Drawn samples from the parameterized Fisher distribution.    
    """
    dist = FDistribution(dfnum, dfden)
    if size is None:
        size = 1
    r = DistributionUtil.rvs(dist, size)
    return NDArray(r)
    
def gamma(shape, scale=1.0, size=None):
    """
    Draw random samples from a Gamma distribution.
    
    :param shape: (*float*) The shape of the gamma distribution. Should be greater than zero.
    :param scale: (*float*) Standard deviation (spread or “width”) of the distribution.
    :param size: (*int*) Output shape. If size is None (default), a single value is returned.
    
    :returns: (*ndarray or scalar*) Drawn samples from the parameterized Gamma distribution.    
    """
    dist = GammaDistribution(shape, scale)
    if size is None:
        size = 1
    r = DistributionUtil.rvs(dist, size)
    return NDArray(r)
    
def gumbel(loc=0.0, scale=1.0, size=None):
    """
    Draw random samples from a Gumbel distribution.
    
    :param loc: (*float*) Mean (“centre”) of the distribution.
    :param scale: (*float*) Standard deviation (spread or “width”) of the distribution.
    :param size: (*int*) Output shape. If size is None (default), a single value is returned.
    
    :returns: (*ndarray or scalar*) Drawn samples from the parameterized Gumbel distribution.    
    """
    dist = GumbelDistribution(loc, scale)
    if size is None:
        size = 1
    r = DistributionUtil.rvs(dist, size)
    return NDArray(r)
    
def laplace(loc=0.0, scale=1.0, size=None):
    """
    Draw samples from the Laplace or double exponential distribution with specified location (or mean) and scale (decay).
    
    :param loc: (*float*) Mean (“centre”) of the distribution.
    :param scale: (*float*) Standard deviation (spread or “width”) of the distribution.
    :param size: (*int*) Output shape. If size is None (default), a single value is returned.
    
    :returns: (*ndarray or scalar*) Drawn samples from the parameterized Laplace distribution.    
    """
    dist = LaplaceDistribution(loc, scale)
    if size is None:
        size = 1
    r = DistributionUtil.rvs(dist, size)
    return NDArray(r)
    
def logistic(loc=0.0, scale=1.0, size=None):
    """
    Draw samples from the Logistic distribution.
    
    :param loc: (*float*) Mean (“centre”) of the distribution.
    :param scale: (*float*) Standard deviation (spread or “width”) of the distribution.
    :param size: (*int*) Output shape. If size is None (default), a single value is returned.
    
    :returns: (*ndarray or scalar*) Drawn samples from the parameterized Logistic distribution.    
    """
    dist = LogisticDistribution(loc, scale)
    if size is None:
        size = 1
    r = DistributionUtil.rvs(dist, size)
    return NDArray(r)
    
def lognormal(mean=0.0, sigma=1.0, size=None):
    """
    Draw samples from the log-normal distribution.
    
    :param mean: (*float*) Mean value of the underlying normal distribution. Default is 0.
    :param sigma: (*float*) Standard deviation of the underlying normal distribution. Should be greater than zero. Default is 1.
    :param size: (*int*) Output shape. If size is None (default), a single value is returned.
    
    :returns: (*ndarray or scalar*) Drawn samples from the parameterized log-normal distribution.    
    """
    dist = LogNormalDistribution(loc, scale)
    if size is None:
        size = 1
    r = DistributionUtil.rvs(dist, size)
    return NDArray(r)
    
def pareto(a, size=None):
    """
    Draw samples from a Pareto II or Lomax distribution with specified shape.
    
    :param a: (*float*) Shape of the distribution. Should be greater than zero.
    :param size: (*int*) Output shape. If size is None (default), a single value is returned.
    
    :returns: (*ndarray or scalar*) Drawn samples from the parameterized Pareto distribution.    
    """
    dist = ParetoDistribution(1, a)
    if size is None:
        size = 1
    r = DistributionUtil.rvs(dist, size)
    return NDArray(r)
    
def standard_t(df, size=None):
    """
    Draw samples from a standard Student’s t distribution with df degrees of freedom.
    
    :param df: (*float*) Degrees of freedom, should be > 0.
    :param size: (*int*) Output shape. If size is None (default), a single value is returned.
    
    :returns: (*ndarray or scalar*) Drawn samples from the parameterized Student’s t distribution.    
    """
    dist = TDistribution(df)
    if size is None:
        size = 1
    r = DistributionUtil.rvs(dist, size)
    return NDArray(r)
    
def triangular(left, mode, right, size=None):
    """
    Draw samples from the triangular distribution over the interval [left, right].
    
    :param left: (*float*) Lower limit.
    :param mode: (*float*) The value where the peak of the distribution occurs. The value 
        should fulfill the condition left <= mode <= right.
    :param right: (*float*) Upper limit, should be larger than left.
    :param size: (*int*) Output shape. If size is None (default), a single value is returned.
    
    :returns: (*ndarray or scalar*) Drawn samples from the parameterized triangular distribution.    
    """
    dist = TriangularDistribution(left, mode, right)
    if size is None:
        size = 1
    r = DistributionUtil.rvs(dist, size)
    return NDArray(r)
    
def uniform(low=0.0, high=1.0, size=None):
    """
    Draw samples from the uniform distribution.
    
    :param low: (*float*) Lower boundary of the output interval. All values generated will 
        be greater than or equal to low. The default value is 0.
    :param high: (*float*) Upper boundary of the output interval. All values generated will 
        be less than high. The default value is 1.0.
    :param size: (*int*) Output shape. If size is None (default), a single value is returned.
    
    :returns: (*ndarray or scalar*) Drawn samples from the parameterized uniform distribution.    
    """
    dist = UniformRealDistribution(low, high)
    if size is None:
        size = 1
    r = DistributionUtil.rvs(dist, size)
    return NDArray(r)
    
def weibull(a, size=None):
    """
    Draw samples from a Weibull distribution.
    
    :param a: (*float*) Shape parameter of the distribution. Must be nonnegative.
    :param size: (*int*) Output shape. If size is None (default), a single value is returned.
    
    :returns: (*ndarray or scalar*) Drawn samples from the parameterized Weibull distribution.    
    """
    dist = WeibullDistribution(a, 1)
    if size is None:
        size = 1
    r = DistributionUtil.rvs(dist, size)
    return NDArray(r)