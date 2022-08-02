# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2018-3-20
# Purpose: MeteoInfoLab stats.distributions module
# Note: Jython
#-----------------------------------------------------

from org.apache.commons.statistics.distribution import NormalDistribution, BetaDistribution, CauchyDistribution, \
    ChiSquaredDistribution, ExponentialDistribution, FDistribution, GammaDistribution, GumbelDistribution, \
    LaplaceDistribution, LevyDistribution, LogisticDistribution, LogNormalDistribution, NakagamiDistribution, \
    ParetoDistribution, TDistribution, TriangularDistribution, UniformContinuousDistribution, WeibullDistribution

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
        self._dist = NormalDistribution.of(mean, std)
    
    def _create_distribution(self, *args):
        """
        Create a normal distribution object.
        """
        loc, scale = self._parse_args(*args)        
        dist = NormalDistribution.of(loc, scale)
        return dist

norm = norm_gen()

class beta_gen(rv_continuous):
    """
    A beta continuous random variable.
    """

    def __init__(self, alpha=0.1, beta=1):
        """
        Initialize.
        :param alpha: (*float*) First shape parameter (must be positive).
        :param beta: (*float*) Second shape parameter (must be positive).
        """
        self.name = "beta"
        self._dist = BetaDistribution.of(alpha, beta)
    
    def _create_distribution(self, *args):
        """
        Create a normal distribution object.
        """
        loc, scale = self._parse_args(*args)        
        dist = BetaDistribution.of(loc, scale)
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
        self._dist = CauchyDistribution.of(median, scale)
    
    def _create_distribution(self, *args):
        """
        Create a cauchy distribution object.
        """
        loc, scale = self._parse_args(*args)        
        dist = CauchyDistribution.of(loc, scale)
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
        self._dist = ChiSquaredDistribution.of(dof)
    
    def _create_distribution(self, *args):
        """
        Create a chi squared distribution object.
        """
        dof = self._parse_args(*args)[0]     
        dist = ChiSquaredDistribution.of(dof)
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
        self._dist = ExponentialDistribution.of(mean)
    
    def _create_distribution(self, *args):
        """
        Create a exponential distribution object.
        """
        mean = self._parse_args(*args)[0]      
        dist = ExponentialDistribution.of(mean)
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
        self._dist = FDistribution.of(ndof, ddof)
    
    def _create_distribution(self, *args):
        """
        Create a F distribution object.
        """
        loc, scale = self._parse_args(*args)        
        dist = FDistribution.of(loc, scale)
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
        self._dist = GammaDistribution.of(shape, scale)
    
    def _create_distribution(self, *args):
        """
        Create a gamma distribution object.
        """
        loc, scale = self._parse_args(*args)        
        dist = GammaDistribution.of(loc, scale)
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
        self._dist = GumbelDistribution.of(loc, scale)

    def _create_distribution(self, *args):
        """
        Create a gumbel distribution object.
        """
        loc, scale = self._parse_args(*args)        
        dist = GumbelDistribution.of(loc, scale)
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
        self._dist = LaplaceDistribution.of(loc, scale)
    
    def _create_distribution(self, *args):
        """
        Create a Laplace distribution object.
        """
        loc, scale = self._parse_args(*args)        
        dist = LaplaceDistribution.of(loc, scale)
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
        self._dist = LevyDistribution.of(loc, scale)
    
    def _create_distribution(self, *args):
        """
        Create a Levy distribution object.
        """
        loc, scale = self._parse_args(*args)        
        dist = LevyDistribution.of(loc, scale)
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
        self._dist = LogisticDistribution.of(loc, scale)
    
    def _create_distribution(self, *args):
        """
        Create a logistic distribution object.
        """
        loc, scale = self._parse_args(*args)        
        dist = LogisticDistribution.of(loc, scale)
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
        self._dist = LevyDistribution.of(scale, shape)
    
    def _create_distribution(self, *args):
        """
        Create a Log-normal distribution object.
        """
        scale, shape = self._parse_args(*args)        
        dist = LogNormalDistribution.of(scale, shape)
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
        self._dist = NakagamiDistribution.of(loc, scale)
    
    def _create_distribution(self, *args):
        """
        Create a Nakagami distribution object.
        """
        shape, scale = self._parse_args(*args)
        dist = NakagamiDistribution.of(shape, scale)
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
        self._dist = ParetoDistribution.of(scale, shape)
    
    def _create_distribution(self, *args):
        """
        Create a Pareto distribution object.
        """
        scale, shape = self._parse_args(*args)        
        dist = ParetoDistribution.of(scale, shape)
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
        self._dist = TDistribution.of(dof)
    
    def _create_distribution(self, *args):
        """
        Create a Student's t-distribution object.
        """
        dof = self._parse_args(*args)[0]     
        dist = TDistribution.of(dof)
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
        self._dist = TriangularDistribution.of(a, c, b)
    
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
        dist = TriangularDistribution.of(a, c, b)
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
        self._dist = UniformContinuousDistribution.of(a, b)
    
    def _create_distribution(self, *args):
        """
        Create a Uniform distribution object.
        """
        scale, shape = self._parse_args(*args)        
        dist = UniformContinuousDistribution.of(scale, shape)
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
        self._dist = WeibullDistribution.of(shape, scale)
    
    def _create_distribution(self, *args):
        """
        Create a Weibull distribution object.
        """
        shape, scale = self._parse_args(*args)
        dist = WeibullDistribution.of(shape, scale)
        return dist

weibull = weibull_gen()