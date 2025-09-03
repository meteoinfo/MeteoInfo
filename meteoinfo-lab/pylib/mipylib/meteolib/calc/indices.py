"""
Contains calculation of various derived indices.

Ported from MetPy.
"""

import mipylib.numeric as np
from .tools import _remove_nans, get_layer
from .thermo import mixing_ratio, saturation_vapor_pressure
from .. import constants


__all__ = ['precipitable_water']


def precipitable_water(pressure, dewpoint, bottom=None, top=None):
    r"""Calculate precipitable water through the depth of a sounding.

    Formula used is:

    .. math::  -\frac{1}{\rho_l g} \int\limits_{p_\text{bottom}}^{p_\text{top}} r dp

    from [Salby1996]_, p. 28

    Parameters
    ----------
    pressure : `hPa`
        Atmospheric pressure profile

    dewpoint : `kelvin`
        Atmospheric dewpoint profile

    bottom: `hPa`, optional
        Bottom of the layer, specified in pressure. Defaults to None (highest pressure).

    top: `hPa`, optional
        Top of the layer, specified in pressure. Defaults to None (lowest pressure).

    Returns
    -------
    `millimeters`
        Precipitable water in the layer

    Examples
    --------
    >>> from mipylib.meteolib.calc import precipitable_water
    >>> from mipylib.meteolib import constants as cons
    >>> pressure = np.array([1000, 950, 900])
    >>> dewpoint = np.array([20, 15, 10]) + cons.degCtoK
    >>> pw = precipitable_water(pressure, dewpoint)
    (11.7702606153 millimeter)

    Notes
    -----
    Only functions on 1D profiles (not higher-dimension vertical cross sections or grids).

    """
    # Sort pressure and dewpoint to be in decreasing pressure order (increasing height)
    sort_inds = np.argsort(pressure)[::-1]
    pressure = pressure[sort_inds]
    dewpoint = dewpoint[sort_inds]

    pressure, dewpoint = _remove_nans(pressure, dewpoint)

    min_pressure = np.min(pressure)
    max_pressure = np.max(pressure)

    if top is None:
        top = min_pressure
    elif not min_pressure <= top <= max_pressure:
        raise ValueError('The pressure and dewpoint profile ranges from {} to '
                         '{}, after removing missing values. {} is outside '
                         'this range.'.format(max_pressure, min_pressure, top))

    if bottom is None:
        bottom = max_pressure
    elif not min_pressure <= bottom <= max_pressure:
        raise ValueError('The pressure and dewpoint profile ranges from {} to '
                         '{}, after removing missing values. {} is outside '
                         'this range.'.format(max_pressure, min_pressure, bottom))

    pres_layer, dewpoint_layer = get_layer(pressure, dewpoint, bottom=bottom,
                                           depth=bottom - top)

    w = mixing_ratio(saturation_vapor_pressure(dewpoint_layer), pres_layer)

    # Since pressure is in decreasing order, pw will be the opposite sign of that expected.
    pw = -np.trapezoid(w, pres_layer) / (constants.g * constants.rho_l)
    return pw * 1e5
