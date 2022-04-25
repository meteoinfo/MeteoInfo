# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2018-3-20
# Purpose: MeteoInfoLab stats.distributions module
# Note: Jython
#-----------------------------------------------------

from org.apache.commons.math3.distribution import NormalDistribution, BetaDistribution, CauchyDistribution, \
    ChiSquaredDistribution, ExponentialDistribution, FDistribution, GammaDistribution, GumbelDistribution, \
    LaplaceDistribution, LevyDistribution, LogisticDistribution, LogNormalDistribution, NakagamiDistribution, \
    ParetoDistribution, TDistribution, TriangularDistribution, UniformRealDistribution, WeibullDistribution

from _distn_infrastructure import rv_continuous

__all__ = [
    'norm','beta','cauchy','chi2','expon','f','gamma','gumbel','laplace','levy','logistic','lognorm',
    'nakagami','pareto','t','triang','uniform','weibull'
    ]

class norm_gen(rv_continuous):
    """
    A normal continuous random variable.
    """

    def __init__(self, mean=0, std=1):
        """
        Initialize.
        :param mean: (*float*) Mean of the distribution.
        :param std: (*float*) Standard deviation of the distribution.
        """
        self.name = "norm"
        self._dist = NormalDistribution(mean, std)
    
    def _create_distribution(self, *args):
        """
        Create a normal distribution object.
        """
        loc, scale = self._parse_args(*args)        
        dist = NormalDistribution(loc, scale)
        return dist

norm = norm_gen()

class beta_gen(rv_continuous):
    """
    A beta continuous random variable.
    """

    def __init__(self, alpha=0, beta=1):
        """
        Initialize.
        :param alpha: (*float*) First shape parameter (must be positive).
        :param beta: (*float*) Second shape parameter (must be positive).
        """
        self.name = "beta"
        self._dist = BetaDistribution(alpha, beta)
    
    def _create_distribution(self, *args):
        """
        Create a normal distribution object.
        """
        loc, scale = self._parse_args(*args)        
        dist = BetaDistribution(loc, scale)
        return dist

beta = beta_gen()

class cauchy_gen(rv_continuous):
    """
    A cauchy continuous random variable.
    """

    def __init__(self, median=0, scale=1):
        """
        Initialize.
        :param median: (*float*) Median of the distribution.
        :param scale: (*float*) Scale parameter of the distribution.
        """
        self.name = "cauchy"
        self._dist = CauchyDistribution(median, scale)
    
    def _create_distribution(self, *args):
        """
        Create a cauchy distribution object.
        """
        loc, scale = self._parse_args(*args)        
        dist = CauchyDistribution(loc, scale)
        return dist

cauchy = cauchy_gen()

class chi2_gen(rv_continuous):
    """
    A chi squared continuous random variable.
    """

    def __init__(self, dof=1):
        """
        Initialize.
        :param dof: (*float*) Degree of freedom.
        """
        self.name = "chi2"
        self._dist = ChiSquaredDistribution(dof)
    
    def _create_distribution(self, *args):
        """
        Create a chi squared distribution object.
        """
        dof = self._parse_args(*args)[0]     
        dist = ChiSquaredDistribution(dof)
        return dist

chi2 = chi2_gen()

class expon_gen(rv_continuous):
    """
    A exponential continuous random variable.
    """

    def __init__(self, mean=1):
        """
        Initialize.
        :param mean: (*float*) Mean of the distribution.
        """
        self.name = "expon"
        self._dist = ExponentialDistribution(mean)
    
    def _create_distribution(self, *args):
        """
        Create a exponential distribution object.
        """
        mean = self._parse_args(*args)[0]      
        dist = ExponentialDistribution(mean)
        return dist

expon = expon_gen()

class f_gen(rv_continuous):
    """
    A F continuous random variable.
    """

    def __init__(self, ndof=1, ddof=1):
        """
        Initialize.
        :param ndof: (*float*) Numerator degrees of freedom.
        :param ddof: (*float*) Denominator degrees of freedom.
        """
        self.name = "f"
        self._dist = FDistribution(ndof, ddof)
    
    def _create_distribution(self, *args):
        """
        Create a F distribution object.
        """
        loc, scale = self._parse_args(*args)        
        dist = FDistribution(loc, scale)
        return dist

f = f_gen()

class gamma_gen(rv_continuous):
    """
    A gamma continuous random variable.
    """

    def __init__(self, shape=1, scale=1):
        """
        Initialize.
        :param shape: (*float*) The shape parameter.
        :param scale: (*float*) The scale parameter.
        """
        self.name = "gamma"
        self._dist = GammaDistribution(shape, scale)
    
    def _create_distribution(self, *args):
        """
        Create a gamma distribution object.
        """
        loc, scale = self._parse_args(*args)        
        dist = GammaDistribution(loc, scale)
        return dist

gamma = gamma_gen()

class gumbel_gen(rv_continuous):
    """
    A gumbel continuous random variable.
    """

    def __init__(self, loc=0, scale=1):
        """
        Initialize.
        :param loc: (*float*) The location parameter.
        :param scale: (*float*) The scale parameter.
        """
        self.name = "gumbel"
        self._dist = GumbelDistribution(loc, scale)

    def _create_distribution(self, *args):
        """
        Create a gumbel distribution object.
        """
        loc, scale = self._parse_args(*args)        
        dist = GumbelDistribution(loc, scale)
        return dist

gumbel = gumbel_gen()

class laplace_gen(rv_continuous):
    """
    A Laplace continuous random variable.
    """

    def __init__(self, loc=0, scale=1):
        """
        Initialize.
        :param loc: (*float*) The location parameter.
        :param scale: (*float*) The scale parameter.
        """
        self.name = "laplace"
        self._dist = LaplaceDistribution(loc, scale)
    
    def _create_distribution(self, *args):
        """
        Create a Laplace distribution object.
        """
        loc, scale = self._parse_args(*args)        
        dist = LaplaceDistribution(loc, scale)
        return dist

laplace = laplace_gen()

class levy_gen(rv_continuous):
    """
    A Levy continuous random variable.
    """

    def __init__(self, loc=0, scale=1):
        """
        Initialize.
        :param loc: (*float*) The location parameter.
        :param scale: (*float*) The scale parameter.
        """
        self.name = "levy"
        self._dist = LevyDistribution(loc, scale)
    
    def _create_distribution(self, *args):
        """
        Create a Levy distribution object.
        """
        loc, scale = self._parse_args(*args)        
        dist = LevyDistribution(loc, scale)
        return dist

levy = levy_gen()

class logistic_gen(rv_continuous):
    """
    A logistic continuous random variable.
    """

    def __init__(self, loc=0, scale=1):
        """
        Initialize.
        :param loc: (*float*) The location parameter.
        :param scale: (*float*) The scale parameter.
        """
        self.name = "logistic"
        self._dist = LogisticDistribution(loc, scale)
    
    def _create_distribution(self, *args):
        """
        Create a logistic distribution object.
        """
        loc, scale = self._parse_args(*args)        
        dist = LogisticDistribution(loc, scale)
        return dist

logistic = logistic_gen()

class lognorm_gen(rv_continuous):
    """
    A Log-normal continuous random variable.
    """

    def __init__(self, scale=0, shape=1):
        """
        Initialize.
        :param scale: (*float*) The scale parameter.
        :param shape: (*float*) The shape parameter.
        """
        self.name = "lognorm"
        self._dist = LevyDistribution(scale, shape)
    
    def _create_distribution(self, *args):
        """
        Create a Log-normal distribution object.
        """
        scale, shape = self._parse_args(*args)        
        dist = LogNormalDistribution(scale, shape)
        return dist

lognorm = lognorm_gen()

class nakagami_gen(rv_continuous):
    """
    A Nakagami continuous random variable.
    """

    def __init__(self, loc=1, scale=1):
        """
        Initialize.
        :param loc: (*float*) The location parameter.
        :param scale: (*float*) The scale parameter.
        """
        self.name = "nakagami"
        self._dist = NakagamiDistribution(loc, scale)
    
    def _create_distribution(self, *args):
        """
        Create a Nakagami distribution object.
        """
        shape, scale = self._parse_args(*args)
        dist = NakagamiDistribution(shape, scale)
        return dist

nakagami = nakagami_gen()

class pareto_gen(rv_continuous):
    """
    A Pareto continuous random variable.
    """

    def __init__(self, scale=1, shape=1):
        """
        Initialize.
        :param scale: (*float*) The scale parameter.
        :param shape: (*float*) The scale parameter.
        """
        self.name = "pareto"
        self._dist = ParetoDistribution(scale, shape)
    
    def _create_distribution(self, *args):
        """
        Create a Pareto distribution object.
        """
        scale, shape = self._parse_args(*args)        
        dist = ParetoDistribution(scale, shape)
        return dist

pareto = pareto_gen()

class t_gen(rv_continuous):
    """
    A Student's t continuous random variable.
    """

    def __init__(self, dof=1):
        """
        Initialize.
        :param dof: (*float*) Degree of freedom.
        """
        self.name = "t"
        self._dist = TDistribution(dof)
    
    def _create_distribution(self, *args):
        """
        Create a Student's t-distribution object.
        """
        dof = self._parse_args(*args)[0]     
        dist = TDistribution(dof)
        return dist

t = t_gen()

class triang_gen(rv_continuous):
    """
    A Triangular continuous random variable.
    """

    def __init__(self, a=0, b=1, c=1):
        """
        Initialize.
        :param a: (*float*) Lower limit of this distribution (inclusive).
        :param b: (*float*) Upper limit of this distribution (inclusive).
        :param c: (*float*) Mode of this distribution.
        """
        self.name = "triang"
        self._dist = TriangularDistribution(a, c, b)
    
    def _create_distribution(self, *args):
        """
        Create a Triangular distribution object.
        """
        r = self._parse_args(*args)
        if len(r) == 1:
            c = r[0]
            a = 0
            b = c
        else:
            a = r[0]
            c = r[2]
            b = a + r[1] * c
        dist = TriangularDistribution(a, c, b)
        return dist

triang = triang_gen()

class uniform_gen(rv_continuous):
    """
    A Uniform continuous random variable.
    """

    def __init__(self, a=0, b=1):
        """
        Initialize.
        :param a: (*float*) Lower limit of this distribution (inclusive).
        :param b: (*float*) Upper limit of this distribution (exclusive).
        """
        self.name = "uniform"
        self._dist = UniformRealDistribution(a, b)
    
    def _create_distribution(self, *args):
        """
        Create a Uniform distribution object.
        """
        scale, shape = self._parse_args(*args)        
        dist = UniformRealDistribution(scale, shape)
        return dist

uniform = uniform_gen()

class weibull_gen(rv_continuous):
    """
    A Weibull continuous random variable.
    """

    def __init__(self, shape=1, scale=1):
        """
        Initialize.
        :param shape: (*float*) The shape parameter.
        :param scale: (*float*) Upper limit of this distribution (inclusive).
        """
        self.name = "weibull"
        self._dist = WeibullDistribution(shape, scale)
    
    def _create_distribution(self, *args):
        """
        Create a Weibull distribution object.
        """
        shape, scale = self._parse_args(*args)
        dist = WeibullDistribution(shape, scale)
        return dist

weibull = weibull_gen()