from mipylib import numeric as np
from org.meteoinfo.math.distribution import DistributionUtil

from ..lib._util import check_random_state

__all__ = ['multivariate_normal']

class multi_rv_generic:
    """
    Class which encapsulates common functionality between all multivariate
    distributions.
    """
    def __init__(self, seed=None):
        self._random_state = check_random_state(seed)

    @property
    def random_state(self):
        """ Get or set the Generator object for generating random variates.
        If `seed` is None (or `np.random`), the `numpy.random.RandomState`
        singleton is used.
        If `seed` is an int, a new ``RandomState`` instance is used,
        seeded with `seed`.
        If `seed` is already a ``Generator`` or ``RandomState`` instance then
        that instance is used.
        """
        return self._random_state

    @random_state.setter
    def random_state(self, seed):
        self._random_state = check_random_state(seed)

    def _get_random_state(self, random_state):
        if random_state is not None:
            return check_random_state(random_state)
        else:
            return self._random_state

class multi_rv_frozen:
    """
    Class which encapsulates common functionality between all frozen
    multivariate distributions.
    """
    @property
    def random_state(self):
        return self._dist._random_state

    @random_state.setter
    def random_state(self, seed):
        self._dist._random_state = check_random_state(seed)

class multivariate_normal_gen(multi_rv_generic):
    r"""A multivariate normal random variable.
    The `mean` keyword specifies the mean. The `cov` keyword specifies the
    covariance matrix.
    """

    def __call__(self, mean=None, cov=1, allow_singular=False, seed=None):
        """Create a frozen multivariate normal distribution.
        See `multivariate_normal_frozen` for more information.
        """
        return multivariate_normal_frozen(mean, cov,
                                          allow_singular=allow_singular,
                                          seed=seed)

    def _process_parameters(self, dim, mean, cov):
        """
        Infer dimensionality from mean or covariance matrix, ensure that
        mean and covariance are full vector resp. matrix.
        """
        # Try to infer dimensionality
        if dim is None:
            if mean is None:
                if cov is None:
                    dim = 1
                else:
                    cov = np.asarray(cov, dtype='float')
                    if cov.ndim < 2:
                        dim = 1
                    else:
                        dim = cov.shape[0]
            else:
                mean = np.asarray(mean, dtype='float')
                dim = mean.size
        else:
            if not np.isscalar(dim):
                raise ValueError("Dimension of random variable must be "
                                 "a scalar.")

        # Check input sizes and return full arrays for mean and cov if
        # necessary
        if mean is None:
            mean = np.zeros(dim)
        mean = np.asarray(mean, dtype='float')

        if cov is None:
            cov = 1.0
        cov = np.asarray(cov, dtype='float')

        if dim == 1:
            mean = mean.reshape(1)
            cov = cov.reshape(1, 1)

        if mean.ndim != 1 or mean.shape[0] != dim:
            raise ValueError("Array 'mean' must be a vector of length %d." %
                             dim)
        if cov.ndim == 0:
            cov = cov * np.eye(dim)
        elif cov.ndim == 1:
            cov = np.diag(cov)
        elif cov.ndim == 2 and cov.shape != (dim, dim):
            rows, cols = cov.shape
            if rows != cols:
                msg = ("Array 'cov' must be square if it is two dimensional,"
                       " but cov.shape = %s." % str(cov.shape))
            else:
                msg = ("Dimension mismatch: array 'cov' is of shape %s,"
                       " but 'mean' is a vector of length %d.")
                msg = msg % (str(cov.shape), len(mean))
            raise ValueError(msg)
        elif cov.ndim > 2:
            raise ValueError("Array 'cov' must be at most two-dimensional,"
                             " but cov.ndim = %d" % cov.ndim)

        return dim, mean, cov

multivariate_normal = multivariate_normal_gen()

class multivariate_normal_frozen(multi_rv_frozen):

    def __init__(self, mean=None, cov=1, allow_singular=False, seed=None,
                 maxpts=None, abseps=1e-5, releps=1e-5):
        """Create a frozen multivariate normal distribution.
        Parameters
        ----------
        mean : array_like, optional
            Mean of the distribution (default zero)
        cov : array_like, optional
            Covariance matrix of the distribution (default one)
        allow_singular : bool, optional
            If this flag is True then tolerate a singular
            covariance matrix (default False).
        seed : {None, int, `numpy.random.Generator`,
                `numpy.random.RandomState`}, optional
            If `seed` is None (or `np.random`), the `numpy.random.RandomState`
            singleton is used.
            If `seed` is an int, a new ``RandomState`` instance is used,
            seeded with `seed`.
            If `seed` is already a ``Generator`` or ``RandomState`` instance
            then that instance is used.
        maxpts : integer, optional
            The maximum number of points to use for integration of the
            cumulative distribution function (default `1000000*dim`)
        abseps : float, optional
            Absolute error tolerance for the cumulative distribution function
            (default 1e-5)s
        releps : float, optional
            Relative error tolerance for the cumulative distribution function
            (default 1e-5)
        """
        self._dist_gen = multivariate_normal_gen(seed)
        self.dim, self.mean, self.cov = self._dist_gen._process_parameters(
            None, mean, cov)
        self._dist = DistributionUtil.mvNormDist(self.mean._array, self.cov._array)

    def rvs(self, size):
        """
        Random variates of given type.

        :param size: (*int*) Size.

        :return: (*array*) Probability density function.
        """
        r = DistributionUtil.rvs(self._dist, size)
        return np.NDArray(r)