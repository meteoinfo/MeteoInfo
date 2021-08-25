from org.meteoinfo.math.special import SpecialUtil

from ..core import numeric as np

__all__ = ['erf','erfc']

def erf(x):
    """
    Returns the error function of complex argument.

    It is defined as 2/sqrt(pi)*integral(exp(-t**2), t=0..z).

    :param x: (*array_like*) Input array.

    :return: The values of the error function at the given points x.
    """
    if isinstance(x, (list, tuple)):
        x = np.array(x).asarray()

    if isinstance(x, np.NDArray):
        x = x.asarray()

    r = SpecialUtil.erf(x)
    if isinstance(r, float):
        return r
    else:
        return np.array(r)

def erfc(x):
    """
    Complementary error function, 1 - erf(x).

    :param x: (*array_like*) Input array.

    :return: The values of the error function at the given points x.
    """
    if isinstance(x, (list, tuple)):
        x = np.array(x).asarray()

    if isinstance(x, np.NDArray):
        x = x.asarray()

    r = SpecialUtil.erfc(x)
    if isinstance(r, float):
        return r
    else:
        return np.array(r)