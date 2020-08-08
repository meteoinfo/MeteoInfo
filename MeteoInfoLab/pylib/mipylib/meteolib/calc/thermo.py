"""Contains a collection of thermodynamic calculations."""

from .. import constants
import mipylib.numeric as np

__all__ = [
    'mixing_ratio','mixing_ratio_from_specific_humidity','relative_humidity_from_specific_humidity',
    'saturation_mixing_ratio','saturation_vapor_pressure'
    ]

def saturation_vapor_pressure(temperature):
    r"""Calculate the saturation water vapor (partial) pressure.
    Parameters
    ----------
    temperature : `float`
        The temperature (celsius)
    Returns
    -------
    `float`
        The saturation water vapor (partial) pressure
    See Also
    --------
    vapor_pressure, dewpoint
    Notes
    -----
    Instead of temperature, dewpoint may be used in order to calculate
    the actual (ambient) water vapor (partial) pressure.
    The formula used is that from [Bolton1980]_ for T in degrees Celsius:
    .. math:: 6.112 e^\frac{17.67T}{T + 243.5}
    """
    # Converted from original in terms of C to use kelvin. Using raw absolute values of C in
    # a formula plays havoc with units support.
    return constants.sat_pressure_0c * np.exp(17.67 * temperature
                                              / (temperature + 243.5))

def mixing_ratio_from_specific_humidity(specific_humidity):
    r"""Calculate the mixing ratio from specific humidity.
    Parameters
    ----------
    specific_humidity: `pint.Quantity`
        Specific humidity of air
    Returns
    -------
    `pint.Quantity`
        Mixing ratio
    Notes
    -----
    Formula from [Salby1996]_ pg. 118.
    .. math:: w = \frac{q}{1-q}
    * :math:`w` is mixing ratio
    * :math:`q` is the specific humidity
    See Also
    --------
    mixing_ratio, specific_humidity_from_mixing_ratio
    """
    return specific_humidity / (1 - specific_humidity)

def mixing_ratio(part_press, tot_press):
    """
    Calculates the mixing ratio of gas given its partial pressure
    and the total pressure of the air.
    There are no required units for the input arrays, other than that
    they have the same units.
    Parameters
    ----------
    part_press : array_like
        Partial pressure of the constituent gas
    tot_press : array_like
        Total air pressure
    Returns
    -------
    array_like
        The (mass) mixing ratio, dimensionless (e.g. Kg/Kg or g/g)
    See Also
    --------
    vapor_pressure
    """

    return constants.epsilon * part_press / (tot_press - part_press)

def saturation_mixing_ratio(tot_press, temperature):
    """
    Calculates the saturation mixing ratio given total pressure
    and the temperature.
    The implementation uses the formula outlined in [4]
    Parameters
    ----------
    tot_press: array_like
        Total atmospheric pressure (hPa)
    temperature: array_like
        The temperature (celsius)
    Returns
    -------
    array_like
        The saturation mixing ratio, dimensionless
    References
    ----------
    .. [4] Hobbs, Peter V. and Wallace, John M., 1977: Atmospheric Science, an Introductory
            Survey. 73.
    """

    return mixing_ratio(saturation_vapor_pressure(temperature), tot_press)

def relative_humidity_from_specific_humidity(specific_humidity, temperature, pressure):
    r"""Calculate the relative humidity from specific humidity, temperature, and pressure.
    Parameters
    ----------
    specific_humidity: `pint.Quantity`
        Specific humidity of air
    temperature: `pint.Quantity`
        Air temperature
    pressure: `pint.Quantity`
        Total atmospheric pressure
    Returns
    -------
    `pint.Quantity`
        Relative humidity
    Notes
    -----
    Formula based on that from [Hobbs1977]_ pg. 74. and [Salby1996]_ pg. 118.
    .. math:: RH = \frac{q}{(1-q)w_s}
    * :math:`RH` is relative humidity as a unitless ratio
    * :math:`q` is specific humidity
    * :math:`w_s` is the saturation mixing ratio
    See Also
    --------
    relative_humidity_from_mixing_ratio
    """
    return (mixing_ratio_from_specific_humidity(specific_humidity)
            / saturation_mixing_ratio(pressure, temperature))