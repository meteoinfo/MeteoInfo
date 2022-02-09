import warnings
import mipylib.numeric as np
from .. import constants

def coriolis_parameter(latitude):
    r"""Calculate the Coriolis parameter at each point.

    The implementation uses the formula outlined in [Hobbs1977]_ pg.370-371.

    Parameters
    ----------
    latitude : array_like
        Latitude at each point

    Returns
    -------
    `array`
        Corresponding Coriolis force at each point
    """
    latitude = _check_radians(latitude, max_radians=np.pi / 2)
    return 2. * constants.omega * np.sin(latitude)

def _check_radians(value, max_radians=2 * np.pi):
    """Input validation of values that could be in degrees instead of radians.

    Parameters
    ----------
    value : `pint.Quantity`
        Input value to check
    max_radians : float
        Maximum absolute value of radians before warning

    Returns
    -------
    `pint.Quantity`
        Input value
    """
    value = np.radians(value)
    if np.any(np.abs(value) > max_radians):
        warnings.warn('Input over {} radians. '
                      'Ensure proper units are given.'.format(np.max(max_radians)))
    return value