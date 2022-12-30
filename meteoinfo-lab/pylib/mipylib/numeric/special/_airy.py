from org.meteoinfo.math.special import SpecialUtil

from ..core import numeric as np

__all__ = ['airy']


def airy(z):
    """
    Airy function and its derivative.

    :param z: (*array*) Input array.

    :return: Ai, Aip: Airy function Ai and its derivative Aip
    """
    if isinstance(z, (list, tuple)):
        z = np.array(z).asarray()

    if isinstance(z, np.NDArray):
        z = z.asarray()

    r = SpecialUtil.airy(z)

    if isinstance(r, float):
        return r[0], r[1]
    else:
        return np.array(r[0]), np.array(r[1])
