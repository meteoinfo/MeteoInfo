from ..core import numeric as np

from org.meteoinfo.math.special import SpecialUtil

__all__ = ['gamma','gammaln']

def gamma(z):
    """
    gamma function.
    :param z: (*array_like*) Value.
    :return: (*array_like*) Value of gamma function.
    """
    if isinstance(z, (list, tuple)):
        z = np.array(z).asarray()

    if isinstance(z, np.NDArray):
        z = z.asarray()

    r = SpecialUtil.gamma(z)
    if isinstance(r, float):
        return r
    else:
        return np.array(r)

def gammaln(z):
    """
    Logarithm of the absolute value of the gamma function.
    :param z: (*array_like*) Value.
    :return: (*array_like*) Value of logarithm of the gamma function.
    """
    if isinstance(z, (list, tuple)):
        z = np.array(z).asarray()

    if isinstance(z, np.NDArray):
        z = z.asarray()

    r = SpecialUtil.logGamma(z)
    if isinstance(r, float):
        return r
    else:
        return np.array(r)