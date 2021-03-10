"""Contains a collection of thermodynamic calculations."""

from .. import constants
import mipylib.numeric as np

__all__ = [
    'equivalent_potential_temperature','exner_function',
    'mixing_ratio','mixing_ratio_from_specific_humidity','potential_temperature',
    'relative_humidity_from_specific_humidity',
    'saturation_mixing_ratio','saturation_vapor_pressure','temperature_from_potential_temperature',
    'virtual_temperature'
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

def exner_function(pressure, reference_pressure=constants.P0):
    r"""Calculate the Exner function.
    .. math:: \Pi = \left( \frac{p}{p_0} \right)^\kappa
    This can be used to calculate potential temperature from temperature (and visa-versa),
    since
    .. math:: \Pi = \frac{T}{\theta}
    Parameters
    ----------
    pressure : `pint.Quantity`
        The total atmospheric pressure
    reference_pressure : `pint.Quantity`, optional
        The reference pressure against which to calculate the Exner function, defaults to P0
    Returns
    -------
    `pint.Quantity`
        The value of the Exner function at the given pressure
    See Also
    --------
    potential_temperature
    temperature_from_potential_temperature
    """
    return (pressure / reference_pressure)**constants.kappa

def potential_temperature(pressure, temperature):
    """
    Calculate the potential temperature.
    Uses the Poisson equation to calculation the potential temperature
    given `pressure` and `temperature`.
    Parameters
    ----------
    pressure : array_like
        The total atmospheric pressure
    temperature : array_like
        The temperature
    Returns
    -------
    array_like
        The potential temperature corresponding to the the temperature and
        pressure.
    See Also
    --------
    dry_lapse
    Notes
    -----
    Formula:
    .. math:: \Theta = T (P_0 / P)^\kappa
    """
    return temperature * (constants.P0 / pressure)**constants.kappa

def temperature_from_potential_temperature(pressure, theta):
    r"""Calculate the temperature from a given potential temperature.
    Uses the inverse of the Poisson equation to calculate the temperature from a
    given potential temperature at a specific pressure level.
    Parameters
    ----------
    pressure : `pint.Quantity`
        The total atmospheric pressure
    theta : `pint.Quantity`
        The potential temperature
    Returns
    -------
    `pint.Quantity`
        The temperature corresponding to the potential temperature and pressure.
    See Also
    --------
    dry_lapse
    potential_temperature
    Notes
    -----
    Formula:
    .. math:: T = \Theta (P / P_0)^\kappa
    """
    return theta * exner_function(pressure)

def equivalent_potential_temperature(pressure, temperature, dewpoint):
    r"""Calculate equivalent potential temperature.
    This calculation must be given an air parcel's pressure, temperature, and dewpoint.
    The implementation uses the formula outlined in [Bolton1980]_:
    First, the LCL temperature is calculated:
    .. math:: T_{L}=\frac{1}{\frac{1}{T_{D}-56}+\frac{ln(T_{K}/T_{D})}{800}}+56
    Which is then used to calculate the potential temperature at the LCL:
    .. math:: \theta_{DL}=T_{K}\left(\frac{1000}{p-e}\right)^k
              \left(\frac{T_{K}}{T_{L}}\right)^{.28r}
    Both of these are used to calculate the final equivalent potential temperature:
    .. math:: \theta_{E}=\theta_{DL}\exp\left[\left(\frac{3036.}{T_{L}}
                                              -1.78\right)*r(1+.448r)\right]
    Parameters
    ----------
    pressure: `float`
        Total atmospheric pressure (hPa)
    temperature: `float`
        Temperature of parcel (celsius)
    dewpoint: `float`
        Dewpoint of parcel (celsius)
    Returns
    -------
    `float`
        The equivalent potential temperature of the parcel (celsius)
    Notes
    -----
    [Bolton1980]_ formula for Theta-e is used, since according to
    [DaviesJones2009]_ it is the most accurate non-iterative formulation
    available.
    """
    t = temperature + 273.15
    td = dewpoint + 273.15
    p = pressure
    e = saturation_vapor_pressure(dewpoint)
    r = saturation_mixing_ratio(pressure, dewpoint)

    t_l = 56 + 1. / (1. / (td - 56) + np.log(t / td) / 800.)
    th_l = t * (1000 / (p - e)) ** constants.kappa * (t / t_l) ** (0.28 * r)
    th_e = th_l * np.exp((3036. / t_l - 1.78) * r * (1 + 0.448 * r))

    return th_e - 273.15

def virtual_temperature(temperature, mixing, molecular_weight_ratio=constants.epsilon):
    r"""Calculate virtual temperature.

    This calculation must be given an air parcel's temperature and mixing ratio.
    The implementation uses the formula outlined in [Hobbs2006]_ pg.80.

    Parameters
    ----------
    temperature: `array`
        air temperature
    mixing : `array`
        dimensionless mass mixing ratio
    molecular_weight_ratio : float, optional
        The ratio of the molecular weight of the constituent gas to that assumed
        for air. Defaults to the ratio for water vapor to dry air.
        (:math:`\epsilon\approx0.622`).

    Returns
    -------
    `array`
        The corresponding virtual temperature of the parcel

    Notes
    -----
    .. math:: T_v = T \frac{\text{w} + \epsilon}{\epsilon\,(1 + \text{w})}

    """
    return temperature * ((mixing + molecular_weight_ratio)
                          / (molecular_weight_ratio * (1 + mixing)))