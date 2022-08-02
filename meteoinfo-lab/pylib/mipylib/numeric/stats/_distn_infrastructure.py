# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2018-3-20
# Purpose: MeteoInfoLab stats.distribution module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.math.distribution import DistributionUtil

from ..core import NDArray
from ..core import numeric as np


class rv_continuous(object):
    """
    A generic continuous random variable class meant for subclassing.
    """
    
    def __init__(self):
        self.name = None
        self._dist = None
    
    def _parse_args(self, *args):
        loc = 0
        scale = 1
        if len(args) >= 1:
            loc = args[0]
            args = args[1:]
        if len(args) >= 1:
            scale = args[0]
            args = args[1:]
        if len(args) == 0:
            return loc, scale
        else:
            r = [loc, scale]
            for arg in args:
                r.append(arg)
            return tuple(r)
        
    def _create_distribution(self, *args):
        """
        Create a distribution object.
        """
        pass

    def __call__(self, *args):
        """
        Create frozen distribution.
        """
        self._dist = self._create_distribution(*args)
        return self
        
    def rvs(self, *args, **kwargs):
        """
        Random variates of given type.

        :param loc: (*float*) location parameter (default=0).
        :param scale: (*float*) scale parameter (default=1).
        :param size: (*int*) Size.
        
        :returns: Probability density function.
        """
        if len(args) == 0:
            dist = self._dist
        else:
            dist = self._create_distribution(*args)
        size = kwargs.pop('size', 10)
        r = DistributionUtil.rvs(dist, size)
        return NDArray(r)
    
    def pdf(self, x, *args):
        """
        Probability density function at x of the given RV.
        
        :param x: (*array_like*) quantiles.
        :param loc: (*float*) location parameter (default=0).
        :param scale: (*float*) scale parameter (default=1).
        
        :returns: Probability density function.
        """
        if len(args) == 0:
            dist = self._dist
        else:
            dist = self._create_distribution(*args)
        if isinstance(x, (list, tuple)):
            x = np.array(x)
        if isinstance(x, NDArray):
            x = x._array
        r = DistributionUtil.pdf(dist, x)
        return NDArray(r)
        
    def logpdf(self, x, *args):
        """
        Log of the probability density function at x of the given RV.
        
        :param x: (*array_like*) quantiles.
        :param loc: (*float*) location parameter (default=0).
        :param scale: (*float*) scale parameter (default=1).
        
        :returns: Log of the probability density function.
        """
        if len(args) == 0:
            dist = self._dist
        else:
            dist = self._create_distribution(*args)
        if isinstance(x, (list, tuple)):
            x = np.array(x)
        if isinstance(x, NDArray):
            x = x._array
        r = DistributionUtil.logpdf(dist, x)
        return NDArray(r)
        
    def cdf(self, x, *args):
        """
        Cumulative distribution function of the given RV.
        
        :param x: (*array_like*) quantiles.
        :param loc: (*float*) location parameter (default=0).
        :param scale: (*float*) scale parameter (default=1).
        
        :returns: Cumulative distribution function.
        """
        if len(args) == 0:
            dist = self._dist
        else:
            dist = self._create_distribution(*args)
        if isinstance(x, (list, tuple)):
            x = np.array(x)
        if isinstance(x, NDArray):
            x = x._array
        r = DistributionUtil.cdf(dist, x)
        return NDArray(r)
        
    def pmf(self, x, *args):
        """
        Probability mass function (PMF) of the given RV.
        
        :param x: (*array_like*) quantiles.
        :param loc: (*float*) location parameter (default=0).
        :param scale: (*float*) scale parameter (default=1).
        
        :returns: Probability mas function.
        """
        if len(args) == 0:
            dist = self._dist
        else:
            dist = self._create_distribution(*args)
        if isinstance(x, (list, tuple)):
            x = np.array(x)
        if isinstance(x, NDArray):
            x = x._array
        r = DistributionUtil.pmf(dist, x)
        return NDArray(r)
        
    def ppf(self, x, *args):
        """
        Percent point function (inverse of cdf) at q of the given RV.
        
        :param q: (*array_like*) lower tail probability.
        :param loc: (*float*) location parameter (default=0).
        :param scale: (*float*) scale parameter (default=1).
        
        :returns: Quantile corresponding to the lower tail probability q.
        """
        if len(args) == 0:
            dist = self._dist
        else:
            dist = self._create_distribution(*args)
        if isinstance(x, (list, tuple)):
            x = np.array(x)
        if isinstance(x, NDArray):
            x = x._array
        r = DistributionUtil.ppf(dist, x)
        return NDArray(r)
        
    def mean(self, *args):
        """
        Mean of the distribution.
        
        :param loc: (*float*) location parameter (default=0).
        :param scale: (*float*) scale parameter (default=1).
        
        :returns: Mean of the distribution.
        """
        if len(args) == 0:
            dist = self._dist
        else:
            dist = self._create_distribution(*args)
        return dist.getMean()
        
    def std(self, *args):
        """
        Standard deviation of the distribution.
        
        :param loc: (*float*) location parameter (default=0).
        :param scale: (*float*) scale parameter (default=1).
        
        :returns: Standard deviation of the distribution.
        """
        if len(args) == 0:
            dist = self._dist
        else:
            dist = self._create_distribution(*args)
        return dist.getStandardDeviation()
        
    def var(self, *args):
        """
        Variance of the distribution.
        
        :param loc: (*float*) location parameter (default=0).
        :param scale: (*float*) scale parameter (default=1).
        
        :returns: Variance of the distribution.
        """
        if len(args) == 0:
            dist = self._dist
        else:
            dist = self._create_distribution(*args)
        return dist.getNumericalVariance()

    def interval(self, alpha, *args, **kwargs):
        """
        Confidence interval with equal areas around the median.

        :param alpha: (*float*) Probability that an rv will be drawn from the returned range.
            Each value should be in the range [0, 1].
        :param loc: (*float*) location parameter (default=0).
        :param scale: (*float*) scale parameter (default=1).

        :return: end-points of range that contain ``100 * alpha %`` of the rv's
            possible values.
        """
        loc = kwargs.pop('loc', 0)
        scale = kwargs.pop('scale', 1)
        if len(args) == 0:
            dist = self._dist
        else:
            dist = self._create_distribution(*args)
        significance = 1 - alpha
        n = kwargs.pop('n', 1)
        a = dist.inverseCumulativeProbability(1.0 - significance / 2)
        r = a * scale / np.sqrt(n)
        lower = loc - r
        upper = loc + r
        return lower, upper