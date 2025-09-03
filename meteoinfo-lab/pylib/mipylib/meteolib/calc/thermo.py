"""
Contains a collection of thermodynamic calculations.

Ported from MetPy.
"""

import warnings
from .. import constants
from ..cbook import broadcast_indices, validate_choice
from .tools import find_bounding_indices, find_intersections, _less_or_close, _remove_nans, get_layer, \
    _greater_or_close
from .exceptions import InvalidSoundingError
from ..interpolate import interpolate_1d
import mipylib.numeric as np
import mipylib.numeric.optimize as so
import mipylib.numeric.integrate as si
from mipylib.numeric.special import lambertw


__all__ = [
    'cape_cin','ccl','density','dewpoint','dewpoint_from_relative_humidity','dry_lapse','dry_static_energy',
    'el','equivalent_potential_temperature','exner_function',
    'isentropic_interpolation','lcl','lfc','lifted_index','mixed_layer','mixed_layer_cape_cin',
    'mixed_parcel','mixing_ratio','mixing_ratio_from_relative_humidity',
    'mixing_ratio_from_specific_humidity',
    'moist_air_gas_constant','moist_air_specific_heat_pressure',
    'moist_air_poisson_exponent','moist_lapse','most_unstable_parcel','most_unstable_cape_cin',
    'parcel_profile','parcel_profile_with_lcl',
    'potential_temperature','psychrometric_vapor_pressure_wet',
    'relative_humidity_from_mixing_ratio','relative_humidity_from_specific_humidity',
    'relative_humidity_from_dewpoint','relative_humidity_wet_psychrometric',
    'saturation_equivalent_potential_temperature',
    'saturation_mixing_ratio','saturation_vapor_pressure','specific_humidity_from_dewpoint',
    'specific_humidity_from_mixing_ratio','specific_humidity_from_relative_humidity','surface_based_cape_cin',
    'temperature_from_potential_temperature','vapor_pressure','virtual_potential_temperature',
    'virtual_temperature','virtual_temperature_from_dewpoint',
    'water_latent_heat_vaporization','water_latent_heat_sublimation','water_latent_heat_melting',
    'wet_bulb_temperature','wet_bulb_potential_temperature'
    ]


def _check_pressure(pressure):
    """Check that pressure does not increase.

    Returns True if the pressure does not increase from one level to the next;
    otherwise, returns False.

    """
    pressure = np.asarray(pressure)
    return np.all(pressure[:-1] >= pressure[1:])


def _check_pressure_error(pressure):
    """Raise an `InvalidSoundingError` if _check_pressure returns False."""
    if not _check_pressure(pressure):
        raise InvalidSoundingError('Pressure increases between at least two points in '
                                   'your sounding.')


def _parcel_profile_helper(pressure, temperature, dewpoint):
    """Help calculate parcel profiles.

    Returns the temperature and pressure, above, below, and including the LCL. The
    other calculation functions decide what to do with the pieces.

    """
    # Check that pressure does not increase.
    _check_pressure_error(pressure)

    # Find the LCL
    press_lcl, temp_lcl = lcl(pressure[0], temperature, dewpoint)

    # Find the dry adiabatic profile, *including* the LCL. We need >= the LCL in case the
    # LCL is included in the levels. It's slightly redundant in that case, but simplifies
    # the logic for removing it later.
    press_lower = np.concatenate((pressure[pressure >= press_lcl], press_lcl))
    temp_lower = dry_lapse(press_lower, temperature)

    # If the pressure profile doesn't make it to the lcl, we can stop here
    if _greater_or_close(np.min(pressure), press_lcl):
        return (press_lower[:-1], press_lcl, np.array([]),
                temp_lower[:-1], temp_lcl, np.array([]))

    # Establish profile above LCL
    press_upper = np.concatenate((press_lcl, pressure[pressure < press_lcl]))

    # Remove duplicate pressure values from remaining profile. Needed for solve_ivp in
    # moist_lapse. unique will return remaining values sorted ascending.
    unique, indices, counts = np.unique(press_upper, return_inverse=True, return_counts=True)
    if np.any(counts > 1):
        _warnings.warn('Duplicate pressure(s) provided. '
                       'Output profile includes duplicate temperatures as a result.')

    # Find moist pseudo-adiabatic profile starting at the LCL, reversing above sorting
    temp_upper = moist_lapse(unique[::-1], temp_lower[-1])
    temp_upper = temp_upper[::-1][indices]

    # Return profile pieces
    return (press_lower[:-1], press_lcl, press_upper[1:],
            temp_lower[:-1], temp_lcl, temp_upper[1:])


def _insert_lcl_level(pressure, temperature, lcl_pressure):
    """Insert the LCL pressure into the profile."""
    interp_temp = interpolate_1d(lcl_pressure, pressure, temperature)

    # Pressure needs to be increasing for searchsorted, so flip it and then convert
    # the index back to the original array
    loc = pressure.size - pressure[::-1].searchsorted(lcl_pressure)
    return np.insert(temperature, loc, interp_temp)


def moist_air_gas_constant(specific_humidity):
    r"""Calculate R_m, the specific gas constant for a parcel of moist air.

    Parameters
    ----------
    specific_humidity : `g/kg`
        Specific humidity

    Returns
    -------
    `J/K/kg`
        Specific gas constant

    Examples
    --------
    >>> from mipylib.meteolib.calc import moist_air_gas_constant
    >>> moist_air_gas_constant(11)
    <288.966723, 'joule / kelvin / kilogram'>

    See Also
    --------
    moist_air_specific_heat_pressure, moist_air_poisson_exponent

    Notes
    -----
    Adapted from

    .. math:: R_m = (1 - q_v) R_a + q_v R_v

    Eq 16, [Romps2017]_ using defined constants in place of cited values.

    """
    return constants.Rd + specific_humidity * 1e-3 * (constants.Rv - constants.Rd)


def moist_air_specific_heat_pressure(specific_humidity):
    r"""Calculate C_pm, the specific heat at constant pressure for a moist air parcel.

    Parameters
    ----------
    specific_humidity : `g/kg`
        Specific humidity

    Returns
    -------
    `J/K/kg`
        Specific heat capacity of air at constant pressure

    Examples
    --------
    >>> from mipylib.meteolib.calc import moist_air_specific_heat_pressure
    >>> moist_air_specific_heat_pressure(11)
    <1014.07575, 'joule / kelvin / kilogram'>

    See Also
    --------
    moist_air_gas_constant, moist_air_poisson_exponent

    Notes
    -----
    Adapted from

    .. math:: c_{pm} = (1 - q_v) c_{pa} + q_v c_{pv}

    Eq 17, [Romps2017]_ using defined constants in place of cited values.

    """
    return (constants.Cp_d
            + specific_humidity * 1e-3 * (constants.Cp_v - constants.Cp_d))


def moist_air_poisson_exponent(specific_humidity):
    r"""Calculate kappa_m, the Poisson exponent for a moist air parcel.

    Parameters
    ----------
    specific_humidity : `g/kg`
        Specific humidity

    Returns
    -------
    `dimensionless`
        Poisson exponent of moist air parcel

    Examples
    --------
    >>> from mipylib.meteolib.calc import moist_air_poisson_exponent
    >>> moist_air_poisson_exponent(11)
    <0.284955757, 'dimensionless'>

    See Also
    --------
    moist_air_gas_constant, moist_air_specific_heat_pressure

    """
    return (moist_air_gas_constant(specific_humidity)
            / moist_air_specific_heat_pressure(specific_humidity))


def water_latent_heat_vaporization(temperature):
    r"""Calculate the latent heat of vaporization for water.

    Accounts for variations in latent heat across valid temperature range.

    Parameters
    ----------
    temperature : `array` (Kelvin)

    Returns
    -------
    `array` (J/kg)
        Latent heat of vaporization

    Examples
    --------
    >>> from mipylib.meteolib.calc import water_latent_heat_vaporization
    >>> water_latent_heat_vaporization(293.16)
    <2453677.15, 'joule / kilogram'>

    See Also
    --------
    water_latent_heat_sublimation, water_latent_heat_melting

    Notes
    -----
    Assumption of constant :math:`C_{pv}` limits validity to :math:`0` -- :math:`100^{\circ} C`
    range.

    .. math:: L = L_0 - (c_{pl} - c_{pv}) (T - T_0)

    Eq 15, [Ambaum2020]_, using MetPy-defined constants in place of cited values.

    """
    return (constants.Lv
            - (constants.Cp_l - constants.Cp_v)
            * (temperature - constants.T0))


def water_latent_heat_sublimation(temperature):
    r"""Calculate the latent heat of sublimation for water.

    Accounts for variations in latent heat across valid temperature range.

    Parameters
    ----------
    temperature : `array` (Kelvin)

    Returns
    -------
    `array` (J/kg)
        Latent heat of vaporization

    Examples
    --------
    >>> from mipybli.meteolib.calc import water_latent_heat_sublimation
    >>> water_latent_heat_sublimation(-15 + 273.16)
    <2837991.13, 'joule / kilogram'>

    See Also
    --------
    water_latent_heat_vaporization, water_latent_heat_melting

    Notes
    -----
    .. math:: L_s = L_{s0} - (c_{pl} - c_{pv}) (T - T_0)

    Eq 18, [Ambaum2020]_, using defined constants in place of cited values.

    """
    return (constants.Ls
            - (constants.Cp_i - constants.Cp_v)
            * (temperature - constants.T0))


def water_latent_heat_melting(temperature):
    r"""Calculate the latent heat of melting for water.

    Accounts for variations in latent heat across valid temperature range.

    Parameters
    ----------
    temperature : `array` (Kelvin)

    Returns
    -------
    `array` (J/kg)
        Latent heat of vaporization

    Examples
    --------
    >>> from mipylib.meteolib.calc import water_latent_heat_melting
    >>> water_latent_heat_melting(-15 + 273.16)
    <365662.294, 'joule / kilogram'>

    See Also
    --------
    water_latent_heat_vaporization, water_latent_heat_sublimation

    Notes
    -----
    .. math:: L_m = L_{m0} + (c_{pl} - c_{pi}) (T - T_0)

    Body text below Eq 20, [Ambaum2020]_, derived from Eq 15, Eq 18.
    Uses defined constants in place of cited values.

    """
    return (constants.Lf
            - (constants.Cp_l - constants.Cp_i)
            * (temperature - constants.T0))


def vapor_pressure(pressure, mixing_ratio):
    r"""Calculate water vapor (partial) pressure.

    Given total ``pressure`` and water vapor ``mixing_ratio``, calculates the
    partial pressure of water vapor.

    Parameters
    ----------
    pressure : `hPa`
        Total atmospheric pressure

    mixing_ratio : `dimensionless`
        Dimensionless mass mixing ratio

    Returns
    -------
    `hPa`
        Ambient water vapor (partial) pressure in the same units as ``pressure``

    Examples
    --------
    >>> from mipylib.meteolib.calc import vapor_pressure
    >>> vapor_pressure(988, 0.018)
    <(27.789371, 'hectopascal')>

    See Also
    --------
    saturation_vapor_pressure, dewpoint

    Notes
    -----
    This function is a straightforward implementation of the equation given in many places,
    such as [Hobbs1977]_ pg.71:

    .. math:: e = p \frac{r}{r + \epsilon}
    """
    return pressure * mixing_ratio / (constants.epsilon + mixing_ratio)


def saturation_vapor_pressure(temperature, phase='liquid'):
    r"""Calculate the saturation (equilibrium) water vapor (partial) pressure.

    Parameters
    ----------
    temperature : `kelvin`
        Air temperature

    phase : {'liquid', 'solid', 'auto'}
        Where applicable, adjust assumptions and constants to make calculation valid in
        ``'liquid'`` water (default) or ``'solid'`` ice regimes. ``'auto'`` will change regime
        based on determination of phase boundaries, eg `temperature` relative to freezing.

    Returns
    -------
    `hPa`
        Saturation water vapor (partial) pressure

    Examples
    --------
    >>> from mipylib.meteolib.calc import saturation_vapor_pressure
    >>> saturation_vapor_pressure(25 + 273.15, phase='liquid')
    <(31.623456, 'hectopascal')>

    See Also
    --------
    vapor_pressure, dewpoint

    Notes
    -----
    Instead of temperature, dewpoint may be used in order to calculate
    the actual (ambient) water vapor (partial) pressure.

    Implements separate solutions from [Ambaum2020]_ for

    ``phase='liquid'``, Eq. 13,

    .. math::
        e = e_{s0} \frac{T_0}{T}^{(c_{pl} - c_{pv}) / R_v} \exp \left(
        \frac{L_0}{R_v T_0} - \frac{L}{R_v T} \right)

    and ``phase='solid'``, Eq. 17,

    .. math::
        e_i = e_{i0} \frac{T_0}{T}^{(c_{pi} - c_{pv}) / R_v} \exp \left(
        \frac{L_{s0}}{R_v T_0} - \frac{L_s}{R_v T} \right)
    """
    if phase == 'liquid':
        return _saturation_vapor_pressure_liquid(temperature)
    elif phase == 'solid':
        return _saturation_vapor_pressure_solid(temperature)
    elif phase == 'auto':
        return np.where(temperature > constants.T0,
                        _saturation_vapor_pressure_liquid(temperature),
                        _saturation_vapor_pressure_solid(temperature))
    else:
        raise ValueError(
            '{} is not a valid option for phase. '
            'Valid options are {}.'.format(phase,"'liquid', 'solid', 'auto'"))


def _saturation_vapor_pressure_liquid(temperature):
    r"""Calculate saturation (equilibrium) water vapor (partial) pressure over liquid water.

    Parameters
    ----------
    temperature : `kelvin`
        Air temperature

    Returns
    -------
    `hPa`
        Saturation water vapor (partial) pressure over liquid water

    See Also
    --------
    saturation_vapor_pressure, vapor_pressure

    Notes
    -----
    Implemented solution from [Ambaum2020]_, Eq. 13,
    .. math:: e = e_{s0} \frac{T_0}{T}^{(c_{pl} - c_{pv}) / R_v} \exp{
    \frac{L_0}{R_v T_0} - \frac{L}{R_v T}}
    """
    latent_heat = water_latent_heat_vaporization(temperature)
    heat_power = (constants.Cp_l - constants.Cp_v) / constants.Rv
    exp_term = ((constants.Lv / constants.T0 - latent_heat / temperature)
                / constants.Rv)

    return (
            constants.sat_pressure_0c
            * (constants.T0 / temperature) ** heat_power
            * np.exp(exp_term)
    )


def _saturation_vapor_pressure_solid(temperature):
    r"""Calculate the saturation water vapor (partial) pressure over solid water (ice).

    Parameters
    ----------
    temperature : `kelvin`
        Air temperature

    Returns
    -------
    `hPa`
        Saturation water vapor (partial) pressure over solid water (ice)

    See Also
    --------
    saturation_vapor_pressure, vapor_pressure

    Notes
    -----
    Implemented solution from [Ambaum2020]_, Eq. 17,
    .. math:: e_i = e_{i0} \frac{T_0}{T}^{(c_{pi} - c_{pv}) / R_v} \exp{
    \frac{L_{s0}}{R_v T_0} - \frac{L_s}{R_v T}}
    """
    latent_heat = water_latent_heat_sublimation(temperature)
    heat_power = (constants.Cp_i - constants.Cp_v) / constants.Rv
    exp_term = ((constants.Ls / constants.T0 - latent_heat / temperature)
                / constants.Rv)

    return (
            constants.sat_pressure_0c
            * (constants.T0 / temperature) ** heat_power
            * np.exp(exp_term)
    )


def mixing_ratio_from_relative_humidity(
        pressure, temperature, relative_humidity, phase='liquid'):
    r"""Calculate the mixing ratio from relative humidity, temperature, and pressure.

    Parameters
    ----------
    pressure: `hPa`
        Total atmospheric pressure

    temperature: `kelvin`
        Air temperature

    relative_humidity: `unitless`
        The relative humidity expressed as a unitless ratio in the range [0, 1]. Can also pass
        a percentage if proper units are attached.

    phase : {'liquid', 'solid', 'auto'}
        Where applicable, adjust assumptions and constants to make calculation valid in
        ``'liquid'`` water (default) or ``'solid'`` ice regimes. ``'auto'`` will change regime
        based on determination of phase boundaries, eg `temperature` relative to freezing.

    Returns
    -------
    `g/kg`
        Mixing ratio (dimensionless)

    Examples
    --------
    >>> from mipylib.meteolib.calc import mixing_ratio_from_relative_humidity
    >>> p = 1000.
    >>> T = 28.1 + 273.15
    >>> rh = .65
    >>> mixing_ratio_from_relative_humidity(p, T, rh)
    <Quantity(15.7296568, 'gram / kilogram')>

    See Also
    --------
    relative_humidity_from_mixing_ratio, saturation_mixing_ratio

    Notes
    -----
    Employs [WMO8]_ eq. 4.A.16 as derived from WMO relative humidity definition based on
    vapor partial pressures (eq. 4.A.15).

    .. math:: RH = \frac{w}{\epsilon + w} \frac{\epsilon + w_s}{w_s}
    .. math:: \therefore w = \frac{\epsilon * w_s * RH}{\epsilon + w_s (1 - RH)}

    * :math:`w` is mixing ratio
    * :math:`w_s` is the saturation mixing ratio
    * :math:`\epsilon` is the molecular weight ratio of vapor to dry air
    * :math:`RH` is relative humidity as a unitless ratio
    """
    validate_choice({'liquid', 'solid', 'auto'}, phase=phase)
    w_s = saturation_mixing_ratio(pressure, temperature, phase=phase)
    return (constants.epsilon * w_s * relative_humidity
            / (constants.epsilon + w_s * (1 - relative_humidity)))


def relative_humidity_from_mixing_ratio(pressure, temperature, mixing_ratio, phase='liquid'):
    r"""Calculate the relative humidity from mixing ratio, temperature, and pressure.

    Parameters
    ----------
    pressure: `hPa`
        Total atmospheric pressure

    temperature: `kelvin`
        Air temperature

    mixing_ratio: `dimensionless`
        Dimensionless mass mixing ratio

    phase : {'liquid', 'solid', 'auto'}
        Where applicable, adjust assumptions and constants to make calculation valid in
        ``'liquid'`` water (default) or ``'solid'`` ice regimes. ``'auto'`` will change regime
        based on determination of phase boundaries, eg `temperature` relative to freezing.

    Returns
    -------
    `dimensionless`
        Relative humidity

    Examples
    --------
    >>> from mipylib.meteolib.calc import relative_humidity_from_mixing_ratio
    >>> from mipylib.meteolib import constants as cons
    >>> relative_humidity_from_mixing_ratio(1013.25,
    ...                                     30 + cons.degCtoK, 18./1000)
    <(0.673008484)>

    See Also
    --------
    mixing_ratio_from_relative_humidity, saturation_mixing_ratio

    Notes
    -----
    Employs [WMO8]_ eq. 4.A.16 as derived from WMO relative humidity definition based on
    vapor partial pressures (eq. 4.A.15).

    .. math:: RH = \frac{w}{\epsilon + w} \frac{\epsilon + w_s}{w_s}

    * :math:`w` is mixing ratio
    * :math:`w_s` is the saturation mixing ratio
    * :math:`\epsilon` is the molecular weight ratio of vapor to dry air
    * :math:`RH` is relative humidity as a unitless ratio

    """
    validate_choice({'liquid', 'solid', 'auto'}, phase=phase)
    w_s = saturation_mixing_ratio(pressure, temperature, phase=phase)
    return (mixing_ratio / (constants.epsilon + mixing_ratio)
            * (constants.epsilon + w_s) / w_s)


def mixing_ratio_from_specific_humidity(specific_humidity):
    r"""Calculate the mixing ratio from specific humidity.

    Parameters
    ----------
    specific_humidity: `kg/kg`
        Specific humidity of air

    Returns
    -------
    `kg/kg`
        Mixing ratio

    Examples
    --------
    >>> from mipylib.meteolib.calc import mixing_ratio_from_specific_humidity
    >>> from mipylib.meteolib import constants as cons
    >>> sh = array([4.77, 12.14, 6.16, 15.29, 12.25]) * 1e-3
    >>> mixing_ratio_from_specific_humidity(sh)
    <([ 4.79286195 12.28919078  6.19818079 15.52741416 12.40192356],
    'kg / kg')>

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
    part_press : `hPa`
        Partial pressure of the constituent gas
    tot_press : `hPa`
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


def saturation_mixing_ratio(total_press, temperature, phase='liquid'):
    """
    Calculates the saturation mixing ratio given total pressure
    and the temperature.

    The implementation uses the formula outlined in [4]

    Parameters
    ----------
    total_press : array_like
        Total atmospheric pressure (hPa)

    temperature : array_like
        The temperature (kelvin)

    phase : {'liquid', 'solid', 'auto'}
        Where applicable, adjust assumptions and constants to make calculation valid in
        ``'liquid'`` water (default) or ``'solid'`` ice regimes. ``'auto'`` will change regime
        based on determination of phase boundaries, eg `temperature` relative to freezing.

    Returns
    -------
    `array`
        The saturation mixing ratio, dimensionless (e.g. Kg/Kg or g/g)

    References
    ----------
    .. [4] Hobbs, Peter V. and Wallace, John M., 1977: Atmospheric Science, an Introductory
            Survey. 73.
    """
    validate_choice({'liquid', 'solid', 'auto'}, phase=phase)
    e_s = saturation_vapor_pressure(temperature, phase=phase)
    undefined = e_s >= total_press
    if np.any(undefined):
        warnings.warn('Saturation mixing ratio is undefined for some requested pressure/'
                       'temperature combinations. Total pressure must be greater than the '
                       'water vapor saturation pressure for liquid water to be in '
                       'equilibrium.')
    return np.where(undefined, np.nan, mixing_ratio(e_s, total_press))


def relative_humidity_from_specific_humidity(pressure, temperature, specific_humidity, phase='liquid'):
    r"""Calculate the relative humidity from specific humidity, temperature, and pressure.

    Parameters
    ----------
    pressure: `hPa`
        Total atmospheric pressure

    temperature: `kelvin`
        Air temperature

    specific_humidity: `kg/kg`
        Specific humidity of air

    phase : {'liquid', 'solid', 'auto'}
        Where applicable, adjust assumptions and constants to make calculation valid in
        ``'liquid'`` water (default) or ``'solid'`` ice regimes. ``'auto'`` will change regime
        based on determination of phase boundaries, eg `temperature` relative to freezing.

    Returns
    -------
    `unitless`
        Relative humidity

    Examples
    --------
    >>> from mipylib.meteolib import relative_humidity_from_specific_humidity
    >>> from mipylib.meteolib import constants as cons
    >>> relative_humidity_from_specific_humidity(1013.25,
    ...                                          30 + cons.degCtoK, 18./1000)
    <(0.684991531)>

    See Also
    --------
    relative_humidity_from_mixing_ratio

    Notes
    -----
    Employs [WMO8]_ eq. 4.A.16 as derived from WMO relative humidity definition based on
    vapor partial pressures (eq. 4.A.15).

    .. math:: RH = \frac{w}{\epsilon + w} \frac{\epsilon + w_s}{w_s}

    given :math: w = \frac{q}{1-q}

    * :math:`w` is mixing ratio
    * :math:`w_s` is the saturation mixing ratio
    * :math:`q` is the specific humidity
    * :math:`\epsilon` is the molecular weight ratio of vapor to dry air
    * :math:`RH` is relative humidity as a unitless ratio
    """
    return relative_humidity_from_mixing_ratio(
        pressure,
        temperature,
        mixing_ratio_from_specific_humidity(specific_humidity),
        phase=phase)


def relative_humidity_from_dewpoint(temperature, dewpoint, phase='liquid'):
    r"""Calculate the relative humidity.

    Uses temperature and dewpoint to calculate relative humidity as the ratio of vapor
    pressure to saturation vapor pressures.

    Parameters
    ----------
    temperature : `kelvin`
        Air temperature

    dewpoint : `kelvin`
        Dewpoint temperature

    phase : {'liquid', 'solid', 'auto'}
        Where applicable, adjust assumptions and constants to make calculation valid in
        ``'liquid'`` water (default) or ``'solid'`` ice regimes. ``'auto'`` will change regime
        based on determination of phase boundaries, eg `temperature` relative to freezing.

    Returns
    -------
    `dimensionless`
        Relative humidity

    Examples
    --------
    >>> from mipylib.meteolib.calc import relative_humidity_from_dewpoint
    >>> relative_humidity_from_dewpoint(25 + 273.15, 12 + 273.15)
    <44.2484765>

    See Also
    --------
    saturation_vapor_pressure

    Notes
    -----
    .. math:: RH = \frac{e(T_d)}{e_s(T)}

    """
    validate_choice({'liquid', 'solid', 'auto'}, phase=phase)
    e = saturation_vapor_pressure(dewpoint, phase=phase)
    e_s = saturation_vapor_pressure(temperature, phase=phase)
    return e / e_s


def exner_function(pressure, reference_pressure=constants.P0):
    r"""Calculate the Exner function.

    .. math:: \Pi = \left( \frac{p}{p_0} \right)^\kappa

    This can be used to calculate potential temperature from temperature (and visa-versa),
    since

    .. math:: \Pi = \frac{T}{\theta}

    Parameters
    ----------
    pressure : `array` (hPa)
        The total atmospheric pressure
    reference_pressure : `pint.Quantity`, optional
        The reference pressure against which to calculate the Exner function, defaults to P0

    Returns
    -------
    `array`
        The value of the Exner function at the given pressure

    See Also
    --------
    potential_temperature
    temperature_from_potential_temperature
    """
    if isinstance(pressure, (list, tuple)):
        pressure = np.asarray(pressure)
    return (pressure / reference_pressure)**constants.kappa


def potential_temperature(pressure, temperature):
    """
    Calculate the potential temperature.

    Uses the Poisson equation to calculation the potential temperature
    given `pressure` and `temperature`.

    Parameters
    ----------
    pressure : array_like (hPa)
        The total atmospheric pressure
    temperature : array_like (Kelvin)
        The temperature

    Returns
    -------
    array_like
        The potential temperature corresponding to the temperature and
        pressure.

    See Also
    --------
    dry_lapse

    Notes
    -----
    Formula:

    .. math:: \Theta = T (P_0 / P)^\kappa
    """
    return temperature / exner_function(pressure)


def temperature_from_potential_temperature(pressure, potential_temperature):
    r"""Calculate the temperature from a given potential temperature.

    Uses the inverse of the Poisson equation to calculate the temperature from a
    given potential temperature at a specific pressure level.

    Parameters
    ----------
    pressure : `array`
        The total atmospheric pressure (hPa)
    potential_temperature : `array`
        The potential temperature (Kelvin)

    Returns
    -------
    `array` (kelvin)
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
    return potential_temperature * exner_function(pressure)


def dry_lapse(pressure, temperature, reference_pressure=None, vertical_dim=0):
    r"""Calculate the temperature at a level assuming only dry processes.

    This function lifts a parcel starting at ``temperature``, conserving
    potential temperature. The starting pressure can be given by ``reference_pressure``.

    Parameters
    ----------
    pressure : `array` (hPa)
        Atmospheric pressure level(s) of interest

    temperature : `array` (Kelvin)
        Starting temperature

    reference_pressure : `float`, optional (hPa)
        Reference pressure; if not given, it defaults to the first element of the
        pressure array.

    Returns
    -------
    `array` (Kelvin)
       The parcel's resulting temperature at levels given by ``pressure``

    Examples
    --------
    >>> from mipylib.meteolib.calc import dry_lapse
    >>> plevs = [1000, 925, 850, 700]
    >>> dry_lapse(plevs, 15 + 273.16)
    <[ 15.           8.65249458   1.92593808 -12.91786723], 'degree_Celsius'>

    See Also
    --------
    moist_lapse : Calculate parcel temperature assuming liquid saturation processes
    parcel_profile : Calculate complete parcel profile
    potential_temperature

    Notes
    -----
    Only reliably functions on 1D profiles (not higher-dimension vertical cross sections or
    grids) unless reference_pressure is specified.
    """
    pressure = np.asarray(pressure)
    temperature = np.asarray(temperature)
    if reference_pressure is None:
        reference_pressure = pressure[0]
    else:
        reference_pressure = np.asarray(reference_pressure)

    r = temperature * (pressure / reference_pressure)**constants.kappa
    return r.item() if r.ndim == 0 else r


def moist_lapse(pressure, temperature, reference_pressure=None):
    r"""Calculate the temperature at a level assuming liquid saturation processes.

    This function lifts a parcel starting at `temperature`. The starting pressure can
    be given by `reference_pressure`. Essentially, this function is calculating moist
    pseudo-adiabats.

    Parameters
    ----------
    pressure : `hPa`
        Atmospheric pressure level(s) of interest

    temperature : `kelvin`
        Starting temperature

    reference_pressure : `hPa`, optional
        Reference pressure; if not given, it defaults to the first element of the
        pressure array.

    Returns
    -------
    `kelvin`
       The resulting parcel temperature at levels given by `pressure`

    Examples
    --------
    >>> from mipylib.meteolib.calc import moist_lapse
    >>> plevs = [925, 850, 700, 500, 300, 200]
    >>> moist_lapse(plevs, 5 + 273.15)
    <[  5.           0.99635104  -8.88958079 -28.38862857 -60.12003999
    -83.34321585], 'degree_Celsius'>

    See Also
    --------
    dry_lapse : Calculate parcel temperature assuming dry adiabatic processes
    parcel_profile : Calculate complete parcel profile

    Notes
    -----
    This function is implemented by integrating the following differential
    equation:

    .. math:: \frac{dT}{dP} = \frac{1}{P} \frac{R_d T + L_v r_s}
                                {C_{pd} + \frac{L_v^2 r_s \epsilon}{R_d T^2}}

    This equation comes from [Bakhshaii2013]_.

    Only reliably functions on 1D profiles (not higher-dimension vertical cross sections or
    grids).

    .. versionchanged:: 1.0
       Renamed ``ref_pressure`` parameter to ``reference_pressure``

    """
    def dt(p, t):
        rs = saturation_mixing_ratio(p, t)
        frac = (
                (constants.Rd * t + constants.Lv * rs)
                / (constants.Cp_d + (
                constants.Lv * constants.Lv * rs * constants.epsilon
                / (constants.Rd * t**2)
        ))
        )
        return frac / p

    temperature = np.atleast_1d(temperature)
    pressure = np.atleast_1d(pressure)
    if reference_pressure is None:
        reference_pressure = pressure[0]

    if np.isnan(reference_pressure) or np.all(np.isnan(temperature)):
        return np.full((temperature.size, pressure.size), np.nan)

    pres_decreasing = (pressure[0] > pressure[-1])
    if pres_decreasing:
        # Everything is easier if pressures are in increasing order
        pressure = pressure[::-1]

    # It would be preferable to use a regular solver like RK45, but as of scipy 1.8.0
    # anything other than LSODA goes into an infinite loop when given NaNs for y0.
    solver_args = {'fun': dt, 'y0': temperature,
                   'method': 'RK45', 'atol': 1e-7, 'rtol': 1.5e-8}

    # Need to handle close points to avoid an error in the solver
    close = np.isclose(pressure, reference_pressure)
    if np.any(close):
        ret = np.broadcast_to(temperature[:, np.newaxis], (temperature.size, np.sum(close)))
    else:
        ret = np.empty((temperature.size, 0), dtype=temperature.dtype)

    # Do we have any points above the reference pressure
    points_above = (pressure < reference_pressure) & ~close
    if np.any(points_above):
        # Integrate upward--need to flip so values are properly ordered from ref to min
        press_side = pressure[points_above][::-1]

        # Flip on exit so t values correspond to increasing pressure
        result = si.solve_ivp(dt, (reference_pressure, press_side[-1]), temperature,
                              t_eval=press_side, atol=1e-7, rtol=1.5e-8)
        ret = np.concatenate((result.y[..., ::-1], ret), axis=-1)

    # Do we have any points below the reference pressure
    points_below = ~points_above & ~close
    if np.any(points_below):
        # Integrate downward
        press_side = pressure[points_below]
        result = si.solve_ivp(dt, (reference_pressure, press_side[-1]), temperature,
                              t_eval=press_side, atol=1e-7, rtol=1.5e-8)
        ret = np.concatenate((ret, result.y), axis=-1)

    if pres_decreasing:
        ret = ret[..., ::-1]

    return ret.squeeze()


def lcl(pressure, temperature, dewpoint, max_iters=None, eps=None):
    r"""Calculate the lifted condensation level (LCL) from the starting point.

    The starting state for the parcel is defined by `temperature`, `dewpoint`,
    and `pressure`. If these are arrays, this function will return a LCL
    for every index. This function does work with surface grids as a result.

    Parameters
    ----------
    pressure : `hPa`
        Starting atmospheric pressure

    temperature : `kelvin`
        Starting temperature

    dewpoint : `kelvin`
        Starting dewpoint

    Returns
    -------
    `hPa`
        LCL pressure

    `kelvin`
        LCL temperature

    Examples
    --------
    >>> from mipylib.meteolib.calc import lcl
    >>> lcl(943, 33 + 273.15, 28 + 273.15)
    (<(877.033549)>, <(299.918469)>)

    See Also
    --------
    parcel_profile

    Notes
    -----
    From [Romps2017]_, this directly solves for the temperature at the LCL, Eq 22a,

    .. math:: T_{LCL} = c [W_{-1}(RH_l^{1/a} c \exp{c})]^{-1} T

    and the pressure at the LCL, Eq 22b,

    .. math:: p_{LCL} = p \left( \frac{T_{LCL}}{T} \right)^{c_{pm} / R_m}

    where :math:`a` (Eq 22d), :math:`b` (Eq 22e), and :math:`c` (Eq 22f) are derived constants,
    and :math:`W_{-1}` is the :math:`k=-1` branch of the Lambert :math:`W` function.
    """
    if max_iters or eps:
        warnings.warn(
            'max_iters, eps arguments unused and will be deprecated in a future version.')

    q = specific_humidity_from_dewpoint(pressure, dewpoint, phase='liquid')
    moist_heat_ratio = (moist_air_specific_heat_pressure(q)
                        / moist_air_gas_constant(q))
    spec_heat_diff = constants.Cp_l - constants.Cp_v

    a = moist_heat_ratio + spec_heat_diff / constants.Rv
    b = (-(constants.Lv + spec_heat_diff * constants.T0)
         / (constants.Rv * temperature))
    c = b / a

    w_minus1 = lambertw(
        (relative_humidity_from_dewpoint(temperature, dewpoint, phase='liquid')
         ** (1 / a) * c * np.exp(c)), k=-1).real

    t_lcl = c / w_minus1 * temperature
    p_lcl = pressure * (t_lcl / temperature) ** moist_heat_ratio

    return p_lcl, t_lcl


def ccl(pressure, temperature, dewpoint, height=None, mixed_layer_depth=None, which='top'):
    r"""Calculate the convective condensation level (CCL) and convective temperature.

    This function is implemented directly based on the definition of the CCL,
    as in [USAF1990]_, and finding where the ambient temperature profile intersects
    the line of constant mixing ratio starting at the surface, using the surface dewpoint
    or the average dewpoint of a shallow layer near the surface.

    Parameters
    ----------
    pressure : `hPa`
        Atmospheric pressure profile. This array must be from high to low pressure.

    temperature : `kelvin`
        Temperature at the levels given by `pressure`

    dewpoint : `kelvin`
        Dewpoint at the levels given by `pressure`

    height : `meters`, optional
        Atmospheric heights at the levels given by `pressure`.
        Only needed when specifying a mixed layer depth as a height.

    mixed_layer_depth : `meters`, optional
        The thickness of the mixed layer as a pressure or height above the bottom
        of the layer (default None).

    which: str, optional
        Pick which CCL value to return; must be one of 'top', 'bottom', or 'all'.
        'top' returns the lowest-pressure CCL (default),
        'bottom' returns the highest-pressure CCL,
        'all' returns every CCL in a `Pint.Quantity` array.

    Returns
    -------
    `hPa`
        CCL Pressure

    `kelvin`
        CCL Temperature

    `lelvin`
        Convective Temperature

    See Also
    --------
    lcl, lfc, el

    Notes
    -----
    Only functions on 1D profiles (not higher-dimension vertical cross sections or grids).
    Since this function returns scalar values when given a profile, this will return Pint
    Quantities even when given xarray DataArray profiles.

    Examples
    --------
    >>> import mipylib.meteolib.calc as mpcalc
    >>> pressure = [993, 957, 925, 886, 850, 813, 798, 732, 716, 700]
    >>> temperature = array([34.6, 31.1, 27.8, 24.3, 21.4, 19.6, 18.7, 13, 13.5, 13]) + 273.15
    >>> dewpoint = array([19.6, 18.7, 17.8, 16.3, 12.4, -0.4, -3.8, -6, -13.2, -11]) + 273.15
    >>> ccl_p, ccl_t, t_c = mpcalc.ccl(pressure, temperature, dewpoint)
    >>> ccl_p, t_c
    (<(758.137299)>, <(38.4385502)>)
    """
    pressure, temperature, dewpoint = _remove_nans(pressure, temperature, dewpoint)
    _check_pressure_error(pressure)

    # If the mixed layer is not defined, take the starting dewpoint to be the
    # first element of the dewpoint array and calculate the corresponding mixing ratio.
    if mixed_layer_depth is None:
        p_start, dewpoint_start = pressure[0], dewpoint[0]
        vapor_pressure_start = saturation_vapor_pressure(dewpoint_start)
        r_start = mixing_ratio(vapor_pressure_start, p_start)

    # Else, calculate the mixing ratio of the mixed layer.
    else:
        vapor_pressure_profile = saturation_vapor_pressure(dewpoint)
        r_profile = mixing_ratio(vapor_pressure_profile, pressure)
        r_start = mixed_layer(pressure, r_profile, height=height,
                              depth=mixed_layer_depth)[0]

    # rt_profile is the temperature-pressure profile with a fixed mixing ratio
    rt_profile = globals()['dewpoint'](vapor_pressure(pressure, r_start))

    x, y = find_intersections(pressure, rt_profile, temperature,
                              direction='increasing', log_x=True)

    # In the case of multiple CCLs, select which to return
    if which == 'top':
        x, y = x[-1], y[-1]
    elif which == 'bottom':
        x, y = x[0], y[0]
    elif which not in ['top', 'bottom', 'all']:
        raise ValueError('Invalid option for "which": {}. Valid options are '
                         '"top", "bottom", and "all".'.format(which))

    print(pressure[0])
    return x, y, dry_lapse(pressure[0], y, x)


def lfc(pressure, temperature, dewpoint, parcel_temperature_profile=None, dewpoint_start=None,
        which='top'):
    r"""Calculate the level of free convection (LFC).

    This works by finding the first intersection of the ideal parcel path and
    the measured parcel temperature. If this intersection occurs below the LCL,
    the LFC is determined to be the same as the LCL, based upon the conditions
    set forth in [USAF1990]_, pg 4-14, where a parcel must be lifted dry adiabatically
    to saturation before it can freely rise.

    Parameters
    ----------
    pressure : `hPa`
        Atmospheric pressure profile. This array must be from high to low pressure.

    temperature : `kelvin`
        Temperature at the levels given by `pressure`

    dewpoint : `kelvin`
        Dewpoint at the levels given by `pressure`

    parcel_temperature_profile: `kelvin`, optional
        The parcel's temperature profile from which to calculate the LFC. Defaults to the
        surface parcel profile.

    dewpoint_start: `kelvin`, optional
        Dewpoint of the parcel for which to calculate the LFC. Defaults to the surface
        dewpoint.

    which: str, optional
        Pick which LFC to return. Options are 'top', 'bottom', 'wide', 'most_cape', and 'all';
        'top' returns the lowest-pressure LFC (default),
        'bottom' returns the highest-pressure LFC,
        'wide' returns the LFC whose corresponding EL is farthest away,
        'most_cape' returns the LFC that results in the most CAPE in the profile.

    Returns
    -------
    `hPa`
        LFC pressure, or array of same if which='all'

    `kelvin`
        LFC temperature, or array of same if which='all'

    Examples
    --------
    >>> from mipylib.meteolib.calc import dewpoint_from_relative_humidity, lfc
    >>> from mipylib.meteolib import constants as cons
    >>> # pressure
    >>> p = [1008., 1000., 950., 900., 850., 800., 750., 700., 650., 600.,
    ...      550., 500., 450., 400., 350., 300., 250., 200.,
    ...      175., 150., 125., 100., 80., 70., 60., 50.,
    ...      40., 30., 25., 20.]
    >>> # temperature
    >>> T = array([29.3, 28.1, 23.5, 20.9, 18.4, 15.9, 13.1, 10.1, 6.7, 3.1,
    ...      -0.5, -4.5, -9.0, -14.8, -21.5, -29.7, -40.0, -52.4,
    ...      -59.2, -66.5, -74.1, -78.5, -76.0, -71.6, -66.7, -61.3,
    ...      -56.3, -51.7, -50.7, -47.5]) + cons.degCtoK
    >>> # relative humidity
    >>> rh = [.85, .65, .36, .39, .82, .72, .75, .86, .65, .22, .52,
    ...       .66, .64, .20, .05, .75, .76, .45, .25, .48, .76, .88,
    ...       .56, .88, .39, .67, .15, .04, .94, .35]
    >>> # calculate dewpoint
    >>> Td = dewpoint_from_relative_humidity(T, rh)
    >>> # calculate LFC
    >>> lfc(p, T, Td)
    (<(967.309996, 'hectopascal')>, <(25.778387, 'degree_Celsius')>)

    See Also
    --------
    parcel_profile

    Notes
    -----
    Only functions on 1D profiles (not higher-dimension vertical cross sections or grids).
    Since this function returns scalar values when given a profile, this will return Pint
    Quantities even when given xarray DataArray profiles.

    """
    # Default to surface parcel if no profile or starting pressure level is given
    if parcel_temperature_profile is None:
        pressure, temperature, dewpoint = _remove_nans(pressure, temperature, dewpoint)
        new_profile = parcel_profile_with_lcl(pressure, temperature, dewpoint)
        pressure, temperature, dewpoint, parcel_temperature_profile = new_profile
        parcel_temperature_profile = parcel_temperature_profile
    else:
        new_profile = _remove_nans(pressure, temperature, dewpoint, parcel_temperature_profile)
        pressure, temperature, dewpoint, parcel_temperature_profile = new_profile

    if dewpoint_start is None:
        dewpoint_start = dewpoint[0]

    # The parcel profile and data may have the same first data point.
    # If that is the case, ignore that point to get the real first
    # intersection for the LFC calculation. Use logarithmic interpolation.
    if np.isclose(parcel_temperature_profile[0], temperature[0]):
        x, y = find_intersections(pressure[1:], parcel_temperature_profile[1:],
                                  temperature[1:], direction='increasing', log_x=True)
    else:
        x, y = find_intersections(pressure, parcel_temperature_profile,
                                  temperature, direction='increasing', log_x=True)

    # Compute LCL for this parcel for future comparisons
    this_lcl = lcl(pressure[0], parcel_temperature_profile[0], dewpoint_start)

    # The LFC could:
    # 1) Not exist
    # 2) Exist but be equal to the LCL
    # 3) Exist and be above the LCL

    # LFC does not exist or is LCL
    if len(x) == 0:
        # Is there any positive area above the LCL?
        mask = pressure < this_lcl[0]
        if np.all(_less_or_close(parcel_temperature_profile[mask], temperature[mask])):
            # LFC doesn't exist
            x = np.nan
            y = np.nan
        else:  # LFC = LCL
            x, y = this_lcl
        return x, y

    # LFC exists. Make sure it is no lower than the LCL
    else:
        idx = x < this_lcl[0]
        # LFC height < LCL height, so set LFC = LCL
        if not any(idx):
            el_pressure, _ = find_intersections(pressure[1:], parcel_temperature_profile[1:],
                                                temperature[1:], direction='decreasing',
                                                log_x=True)
            if el_pressure.size and np.min(el_pressure) > this_lcl[0]:
                # EL exists and it is below the LCL
                x = np.nan
                y = np.nan, temperature.units
            else:
                # EL exists and it is above the LCL or the EL does not exist
                x, y = this_lcl
            return x, y
        # Otherwise, find all LFCs that exist above the LCL
        # What is returned depends on which flag as described in the docstring
        else:
            return _multiple_el_lfc_options(x, y, idx, which, pressure,
                                            parcel_temperature_profile, temperature,
                                            dewpoint, intersect_type='LFC')


def _multiple_el_lfc_options(intersect_pressures, intersect_temperatures, valid_x,
                             which, pressure, parcel_temperature_profile, temperature,
                             dewpoint, intersect_type):
    """Choose which ELs and LFCs to return from a sounding."""
    p_list, t_list = intersect_pressures[valid_x], intersect_temperatures[valid_x]
    if which == 'all':
        x, y = p_list, t_list
    elif which == 'bottom':
        x, y = p_list[0], t_list[0]
    elif which == 'top':
        x, y = p_list[-1], t_list[-1]
    elif which == 'wide':
        x, y = _wide_option(intersect_type, p_list, t_list, pressure,
                            parcel_temperature_profile, temperature)
    elif which == 'most_cape':
        x, y = _most_cape_option(intersect_type, p_list, t_list, pressure, temperature,
                                 dewpoint, parcel_temperature_profile)
    else:
        raise ValueError('Invalid option for "which". Valid options are "top", "bottom", '
                         '"wide", "most_cape", and "all".')
    return x, y


def _wide_option(intersect_type, p_list, t_list, pressure, parcel_temperature_profile,
                 temperature):
    """Calculate the LFC or EL that produces the greatest distance between these points."""
    # zip the LFC and EL lists together and find greatest difference
    if intersect_type == 'LFC':
        # Find EL intersection pressure values
        lfc_p_list = p_list
        el_p_list, _ = find_intersections(pressure[1:], parcel_temperature_profile[1:],
                                          temperature[1:], direction='decreasing',
                                          log_x=True)
    else:  # intersect_type == 'EL'
        el_p_list = p_list
        # Find LFC intersection pressure values
        lfc_p_list, _ = find_intersections(pressure, parcel_temperature_profile,
                                           temperature, direction='increasing',
                                           log_x=True)
    diff = [lfc_p - el_p for lfc_p, el_p in zip(lfc_p_list, el_p_list, strict=False)]
    return (p_list[np.where(diff == np.max(diff))][0],
            t_list[np.where(diff == np.max(diff))][0])


def _most_cape_option(intersect_type, p_list, t_list, pressure, temperature, dewpoint,
                      parcel_temperature_profile):
    """Calculate the LFC or EL that produces the most CAPE in the profile."""
    # Need to loop through all possible combinations of cape, find greatest cape profile
    cape_list, pair_list = [], []
    for which_lfc in ['top', 'bottom']:
        for which_el in ['top', 'bottom']:
            cape, _ = cape_cin(pressure, temperature, dewpoint, parcel_temperature_profile,
                               which_lfc=which_lfc, which_el=which_el)
            cape_list.append(cape.m)
            pair_list.append([which_lfc, which_el])
    (lfc_chosen, el_chosen) = pair_list[np.where(cape_list == np.max(cape_list))[0][0]]
    if intersect_type == 'LFC':
        if lfc_chosen == 'top':
            x, y = p_list[-1], t_list[-1]
        else:  # 'bottom' is returned
            x, y = p_list[0], t_list[0]
    else:  # EL is returned
        if el_chosen == 'top':
            x, y = p_list[-1], t_list[-1]
        else:
            x, y = p_list[0], t_list[0]
    return x, y


def el(pressure, temperature, dewpoint, parcel_temperature_profile=None, which='top'):
    r"""Calculate the equilibrium level.

    This works by finding the last intersection of the ideal parcel path and
    the measured environmental temperature. If there is one or fewer intersections, there is
    no equilibrium level.

    Parameters
    ----------
    pressure : `hPa`
        Atmospheric pressure profile. This array must be from high to low pressure.

    temperature : `kelvin`
        Temperature at the levels given by `pressure`

    dewpoint : `kelvin`
        Dewpoint at the levels given by `pressure`

    parcel_temperature_profile: `kelvin`, optional
        The parcel's temperature profile from which to calculate the EL. Defaults to the
        surface parcel profile.

    which: str, optional
        Pick which EL to return. Options are 'top', 'bottom', 'wide', 'most_cape', and 'all'.
        'top' returns the lowest-pressure EL, default.
        'bottom' returns the highest-pressure EL.
        'wide' returns the EL whose corresponding LFC is farthest away.
        'most_cape' returns the EL that results in the most CAPE in the profile.

    Returns
    -------
    `hPa`
        EL pressure, or array of same if which='all'

    `kelvin`
        EL temperature, or array of same if which='all'

    Examples
    --------
    >>> from mipylib.meteolib.calc import el, dewpoint_from_relative_humidity, parcel_profile
    >>> from mipylib.meteolib import constants as cons
    >>> # pressure
    >>> p = [1008., 1000., 950., 900., 850., 800., 750., 700., 650., 600.,
    ...      550., 500., 450., 400., 350., 300., 250., 200.,
    ...      175., 150., 125., 100., 80., 70., 60., 50.,
    ...      40., 30., 25., 20.]
    >>> # temperature
    >>> T = array([29.3, 28.1, 23.5, 20.9, 18.4, 15.9, 13.1, 10.1, 6.7, 3.1,
    ...      -0.5, -4.5, -9.0, -14.8, -21.5, -29.7, -40.0, -52.4,
    ...      -59.2, -66.5, -74.1, -78.5, -76.0, -71.6, -66.7, -61.3,
    ...      -56.3, -51.7, -50.7, -47.5]) + cons.degCtoK
    >>> # relative humidity
    >>> rh = [.85, .65, .36, .39, .82, .72, .75, .86, .65, .22, .52,
    ...       .66, .64, .20, .05, .75, .76, .45, .25, .48, .76, .88,
    ...       .56, .88, .39, .67, .15, .04, .94, .35]
    >>> # calculate dewpoint
    >>> Td = dewpoint_from_relative_humidity(T, rh)
    >>> # compute parcel profile temperature
    >>> prof = parcel_profile(p, T[0], Td[0]).to('degC')
    >>> # calculate EL
    >>> el(p, T, Td, prof)
    (<(112.252054, 'hectopascal')>, <(-76.2210312, 'degree_Celsius')>)

    See Also
    --------
    parcel_profile

    Notes
    -----
    Only functions on 1D profiles (not higher-dimension vertical cross sections or grids).
    Since this function returns scalar values when given a profile, this will return Pint
    Quantities even when given xarray DataArray profiles.

    """
    # Default to surface parcel if no profile or starting pressure level is given
    if parcel_temperature_profile is None:
        pressure, temperature, dewpoint = _remove_nans(pressure, temperature, dewpoint)
        new_profile = parcel_profile_with_lcl(pressure, temperature, dewpoint)
        pressure, temperature, dewpoint, parcel_temperature_profile = new_profile
    else:
        new_profile = _remove_nans(pressure, temperature, dewpoint, parcel_temperature_profile)
        pressure, temperature, dewpoint, parcel_temperature_profile = new_profile

    # If the top of the sounding parcel is warmer than the environment, there is no EL
    if parcel_temperature_profile[-1] > temperature[-1]:
        return (np.nan, np.nan)

    # Interpolate in log space to find the appropriate pressure - units have to be stripped
    # and reassigned to allow np.log() to function properly.
    x, y = find_intersections(pressure[1:], parcel_temperature_profile[1:], temperature[1:],
                              direction='decreasing', log_x=True)
    lcl_p, _ = lcl(pressure[0], temperature[0], dewpoint[0])
    if len(x) > 0 and x[-1] < lcl_p:
        idx = x < lcl_p
        return _multiple_el_lfc_options(x, y, idx, which, pressure,
                                        parcel_temperature_profile, temperature, dewpoint,
                                        intersect_type='EL')
    else:
        return (np.nan, np.nan)


def parcel_profile(pressure, temperature, dewpoint):
    r"""Calculate the profile a parcel takes through the atmosphere.

    The parcel starts at `temperature`, and `dewpoint`, lifted up
    dry adiabatically to the LCL, and then moist adiabatically from there.
    `pressure` specifies the pressure levels for the profile.

    Parameters
    ----------
    pressure : `hPa`
        Atmospheric pressure level(s) of interest. This array must be from
        high to low pressure.

    temperature : `kelvin`
        Starting temperature

    dewpoint : `kelvin`
        Starting dewpoint

    Returns
    -------
    `kelvin`
        The parcel's temperatures at the specified pressure levels

    Examples
    --------
    >>> from mipylib.meteolib.calc import dewpoint_from_relative_humidity, parcel_profile
    >>> from mipylib.meteolib import constants as cons
    >>> # pressure
    >>> p = [1008., 1000., 950., 900., 850., 800., 750., 700., 650., 600.,
    ...      550., 500., 450., 400., 350., 300., 250., 200.,
    ...      175., 150., 125., 100., 80., 70., 60., 50.,
    ...      40., 30., 25., 20.]
    >>> # temperature
    >>> T = array([29.3, 28.1, 23.5, 20.9, 18.4, 15.9, 13.1, 10.1, 6.7, 3.1,
    ...      -0.5, -4.5, -9.0, -14.8, -21.5, -29.7, -40.0, -52.4,
    ...      -59.2, -66.5, -74.1, -78.5, -76.0, -71.6, -66.7, -61.3,
    ...      -56.3, -51.7, -50.7, -47.5]) + cons.degCtoK
    >>> # relative humidity
    >>> rh = [.85, .65, .36, .39, .82, .72, .75, .86, .65, .22, .52,
    ...       .66, .64, .20, .05, .75, .76, .45, .25, .48, .76, .88,
    ...       .56, .88, .39, .67, .15, .04, .94, .35]
    >>> # calculate dewpoint
    >>> Td = dewpoint_from_relative_humidity(T, rh)
    >>> # computer parcel temperature
    >>> parcel_profile(p, T[0], Td[0]) - cons.degCtoK
    <([  29.3          28.61221952   25.17408111   23.41044641   21.53049669
    19.51679547   17.34763012   14.99552875   12.4250297     9.58933992
        6.4250951     2.84385238   -1.28217807   -6.14487817  -12.0437512
    -19.45887455  -29.14459155  -42.13147376  -50.19971377  -59.49169312
    -70.19973455  -82.69068969  -94.44583924 -101.13413664 -108.54542355
    -116.90037584 -126.55118719 -138.11894611 -144.97290122 -152.88981956], 'degree_Celsius')>

    See Also
    --------
    lcl, moist_lapse, dry_lapse, parcel_profile_with_lcl, parcel_profile_with_lcl_as_dataset

    Notes
    -----
    Only functions on 1D profiles (not higher-dimension vertical cross sections or grids).
    Duplicate pressure levels return duplicate parcel temperatures. Consider preprocessing
    low-precision, high frequency profiles with tools like `scipy.medfilt`,
    `pandas.drop_duplicates`, or `numpy.unique`.

    Will only return Pint Quantities, even when given xarray DataArray profiles. To
    obtain a xarray Dataset instead, use `parcel_profile_with_lcl_as_dataset` instead.

    """
    pressure = np.asarray(pressure)
    _, _, _, t_l, _, t_u = _parcel_profile_helper(pressure, temperature, dewpoint)
    return np.concatenate((t_l, t_u))


def parcel_profile_with_lcl(pressure, temperature, dewpoint):
    r"""Calculate the profile a parcel takes through the atmosphere.

    The parcel starts at `temperature`, and `dewpoint`, lifted up
    dry adiabatically to the LCL, and then moist adiabatically from there.
    `pressure` specifies the pressure levels for the profile. This function returns
    a profile that includes the LCL.

    Parameters
    ----------
    pressure : `hPa`
        Atmospheric pressure level(s) of interest. This array must be from
        high to low pressure.

    temperature : `kelvin`
        Atmospheric temperature at the levels in `pressure`. The first entry should be at
        the same level as the first `pressure` data point.

    dewpoint : `kelvin`
        Atmospheric dewpoint at the levels in `pressure`. The first entry should be at
        the same level as the first `pressure` data point.

    Returns
    -------
    pressure : `hPa`
        The parcel profile pressures, which includes the specified levels and the LCL

    ambient_temperature : `kelvin`
        Atmospheric temperature values, including the value interpolated to the LCL level

    ambient_dew_point : `kelvin`
        Atmospheric dewpoint values, including the value interpolated to the LCL level

    profile_temperature : `kelvin`
        The parcel profile temperatures at all of the levels in the returned pressures array,
        including the LCL

    Examples
    --------
    >>> from mipylib.meteolib.calc import dewpoint_from_relative_humidity, parcel_profile_with_lcl
    >>> from mipylib.meteolib import constants as cons
    >>> # pressure
    >>> p = [1008., 1000., 950., 900., 850., 800., 750., 700., 650., 600.,
    ...      550., 500., 450., 400., 350., 300., 250., 200.,
    ...      175., 150., 125., 100., 80., 70., 60., 50.,
    ...      40., 30., 25., 20.]
    >>> # temperature
    >>> T = array([29.3, 28.1, 23.5, 20.9, 18.4, 15.9, 13.1, 10.1, 6.7, 3.1,
    ...      -0.5, -4.5, -9.0, -14.8, -21.5, -29.7, -40.0, -52.4,
    ...      -59.2, -66.5, -74.1, -78.5, -76.0, -71.6, -66.7, -61.3,
    ...      -56.3, -51.7, -50.7, -47.5]) + cons.degCtoK
    >>> # relative humidity
    >>> rh = [.85, .65, .36, .39, .82, .72, .75, .86, .65, .22, .52,
    ...       .66, .64, .20, .05, .75, .76, .45, .25, .48, .76, .88,
    ...       .56, .88, .39, .67, .15, .04, .94, .35]
    >>> # calculate dewpoint
    >>> Td = dewpoint_from_relative_humidity(T, rh)
    >>> # compute parcel temperature
    >>> Td = dewpoint_from_relative_humidity(T, rh)
    >>> p_wLCL, T_wLCL, Td_wLCL, prof_wLCL = parcel_profile_with_lcl(p, T, Td)

    See Also
    --------
    lcl, moist_lapse, dry_lapse, parcel_profile, parcel_profile_with_lcl_as_dataset

    Notes
    -----
    Only functions on 1D profiles (not higher-dimension vertical cross sections or grids).
    Duplicate pressure levels return duplicate parcel temperatures. Consider preprocessing
    low-precision, high frequency profiles with tools like `scipy.medfilt`,
    `pandas.drop_duplicates`, or `numpy.unique`.

    Will only return Pint Quantities, even when given xarray DataArray profiles. To
    obtain a xarray Dataset instead, use `parcel_profile_with_lcl_as_dataset` instead.

    """
    p_l, p_lcl, p_u, t_l, t_lcl, t_u = _parcel_profile_helper(pressure, temperature[0],
                                                              dewpoint[0])
    new_press = np.concatenate((p_l, p_lcl, p_u))
    prof_temp = np.concatenate((t_l, t_lcl, t_u))
    new_temp = _insert_lcl_level(pressure, temperature, p_lcl)
    new_dewp = _insert_lcl_level(pressure, dewpoint, p_lcl)
    return new_press, new_temp, new_dewp, prof_temp


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
    pressure : `hPa`
        Total atmospheric pressure

    temperature : `kelvin`
        Temperature of parcel

    dewpoint : `kelvin`
        Dewpoint of parcel

    Returns
    -------
    `kelvin`
        The equivalent potential temperature of the parcel

    Notes
    -----
    [Bolton1980]_ formula for Theta-e is used, since according to
    [DaviesJones2009]_ it is the most accurate non-iterative formulation
    available.
    """
    t = temperature
    td = dewpoint
    e = saturation_vapor_pressure(dewpoint)
    r = saturation_mixing_ratio(pressure, dewpoint)

    t_l = 56 + 1. / (1. / (td - 56) + np.log(t / td) / 800.)
    th_l = potential_temperature(pressure - e, temperature) * (t / t_l) ** (0.28 * r)
    return th_l * np.exp(r * (1 + 0.448 * r) * (3036. / t_l - 1.78))


def saturation_equivalent_potential_temperature(pressure, temperature):
    r"""Calculate saturation equivalent potential temperature.

    This calculation must be given an air parcel's pressure and temperature.
    The implementation uses the formula outlined in [Bolton1980]_ for the
    equivalent potential temperature, and assumes a saturated process.

    First, because we assume a saturated process, the temperature at the LCL is
    equivalent to the current temperature. Therefore the following equation.

    .. math:: T_{L}=\frac{1}{\frac{1}{T_{D}-56}+\frac{ln(T_{K}/T_{D})}{800}}+56

    reduces to:

    .. math:: T_{L} = T_{K}

    Then the potential temperature at the temperature/LCL is calculated:

    .. math:: \theta_{DL}=T_{K}\left(\frac{1000}{p-e}\right)^k
              \left(\frac{T_{K}}{T_{L}}\right)^{.28r}

    However, because:

    .. math:: T_{L} = T_{K}

    it follows that:

    .. math:: \theta_{DL}=T_{K}\left(\frac{1000}{p-e}\right)^k

    Both of these are used to calculate the final equivalent potential temperature:

    .. math:: \theta_{E}=\theta_{DL}\exp\left[\left(\frac{3036.}{T_{K}}
                                              -1.78\right)*r(1+.448r)\right]

    Parameters
    ----------
    pressure: `hPa`
        Total atmospheric pressure

    temperature: `kelvin`
        Temperature of parcel

    Returns
    -------
    `kelvin`
        Saturation equivalent potential temperature of the parcel

    Examples
    --------
    >>> from mipylib.meteolib.calc import saturation_equivalent_potential_temperature
    >>> from mipylib.meteolib import constants as cons
    >>> saturation_equivalent_potential_temperature(500, -20 + cons.degCtoK)
    <(313.793845, 'kelvin')>

    Notes
    -----
    [Bolton1980]_ formula for Theta-e is used (for saturated case), since according to
    [DaviesJones2009]_ it is the most accurate non-iterative formulation
    available.

    """
    t = temperature
    p = pressure
    e = saturation_vapor_pressure(temperature)
    r = saturation_mixing_ratio(pressure, temperature)

    th_l = t * (1000 / (p - e)) ** constants.kappa
    th_es = th_l * np.exp((3036. / t - 1.78) * r * (1 + 0.448 * r))

    return th_es


def wet_bulb_potential_temperature(pressure, temperature, dewpoint):
    r"""Calculate wet-bulb potential temperature.

    This calculation must be given an air parcel's pressure, temperature, and dewpoint.
    The implementation uses the formula outlined in [DaviesJones2008]_.
    First, theta-e is calculated
    then use the formula from [DaviesJones2008]_

    .. math:: \theta_w = \theta_e -
        exp(\frac{a_0 + a_1 x + a_2 x^2 + a_3 x^3 + a_4 x^4}
            {1 + b_1 x + b_2 x^2 + b_3 x^3 + b_4 x^4})

    where :math:`x = \theta_e / 273.15 K`.

    When :math:`\theta_e <= -173.15 K` then :math:`\theta_w = \theta_e`.

    Parameters
    ----------
    pressure: `hPa`
        Total atmospheric pressure

    temperature: `kelvin`
        Temperature of parcel

    dewpoint: `kelvin`
        Dewpoint of parcel

    Returns
    -------
    `kelvin`
        wet-bulb potential temperature of the parcel

    """
    theta_e = equivalent_potential_temperature(pressure, temperature, dewpoint)
    x = theta_e / 273.15
    x2 = x * x
    x3 = x2 * x
    x4 = x2 * x2
    a = 7.101574 - 20.68208 * x + 16.11182 * x2 + 2.574631 * x3 - 5.205688 * x4
    b = 1 - 3.552497 * x + 3.781782 * x2 - 0.6899655 * x3 - 0.5929340 * x4

    theta_w = theta_e - np.exp(a / b)
    return np.where(theta_e <= 173.15, theta_e, theta_w)


def virtual_temperature(temperature, mixing, molecular_weight_ratio=constants.epsilon):
    r"""Calculate virtual temperature.

    This calculation must be given an air parcel's temperature and mixing ratio.
    The implementation uses the formula outlined in [Hobbs2006]_ pg.80.

    Parameters
    ----------
    temperature: `kelvin`
        air temperature

    mixing : `dimensionless`
        dimensionless mass mixing ratio

    molecular_weight_ratio : 'dimensionless', optional
        The ratio of the molecular weight of the constituent gas to that assumed
        for air. Defaults to the ratio for water vapor to dry air.
        (:math:`\epsilon\approx0.622`).

    Returns
    -------
    `kelvin`
        The corresponding virtual temperature of the parcel

    Examples
    --------
    >>> from mipylib.meteolib.calc import virtual_temperature
    >>> virtual_temperature(283, 12e-3)
    <(285.039709, 'kelvin')>

    Notes
    -----
    .. math:: T_v = T \frac{\text{w} + \epsilon}{\epsilon\,(1 + \text{w})}

    """
    return temperature * ((mixing + molecular_weight_ratio)
                          / (molecular_weight_ratio * (1 + mixing)))


def virtual_temperature_from_dewpoint(pressure, temperature, dewpoint,
        molecular_weight_ratio=constants.epsilon, phase='liquid'):
    r"""Calculate virtual temperature.

    This calculation must be given an air parcel's temperature and mixing ratio.
    The implementation uses the formula outlined in [Hobbs2006]_ pg.80.

    Parameters
    ----------
    pressure: `hPa`
        Total atmospheric pressure

    temperature: `kelvin`
        Air temperature

    dewpoint : `kelvin`
        Dewpoint temperature

    molecular_weight_ratio : `dimensionless` or float, optional
        The ratio of the molecular weight of the constituent gas to that assumed
        for air. Defaults to the ratio for water vapor to dry air.
        (:math:`\epsilon\approx0.622`)

    phase : {'liquid', 'solid', 'auto'}
        Where applicable, adjust assumptions and constants to make calculation valid in
        ``'liquid'`` water (default) or ``'solid'`` ice regimes. ``'auto'`` will change regime
        based on determination of phase boundaries, eg `temperature` relative to freezing.

    Returns
    -------
    `kelvin`
        Corresponding virtual temperature of the parcel

    Examples
    --------
    >>> from mipylib.meteolib.calc import virtual_temperature_from_dewpoint
    >>> from mipylib.meteolib import constants
    >>> virtual_temperature_from_dewpoint(1000, 30 + constants.degCtoK, 25 + constants.degCtoK) - constants.degCtoK
    <(33.6680183, 'degree_Celsius')>

    Notes
    -----
    .. math:: T_v = T \frac{\text{w} + \epsilon}{\epsilon\,(1 + \text{w})}

    .. versionchanged:: 1.0
       Renamed ``mixing`` parameter to ``mixing_ratio``

    """
    validate_choice({'liquid', 'solid', 'auto'}, phase=phase)

    # Convert dewpoint to mixing ratio
    mixing_ratio = saturation_mixing_ratio(pressure, dewpoint, phase=phase)

    # Calculate virtual temperature with given parameters
    return virtual_temperature(temperature, mixing_ratio, molecular_weight_ratio)


def virtual_potential_temperature(pressure, temperature, mixing_ratio,
                                  molecular_weight_ratio=constants.epsilon):
    r"""Calculate virtual potential temperature.

    This calculation must be given an air parcel's pressure, temperature, and mixing ratio.
    The implementation uses the formula outlined in [Markowski2010]_ pg.13.

    Parameters
    ----------
    pressure: `hPa`
        Total atmospheric pressure

    temperature: `kelvin`
        Air temperature

    mixing_ratio : `dimensionless`
        Dimensionless mass mixing ratio

    molecular_weight_ratio : `dimensionless` or float, optional
        The ratio of the molecular weight of the constituent gas to that assumed
        for air. Defaults to the ratio for water vapor to dry air.
        (:math:`\epsilon\approx0.622`)

    Returns
    -------
    `kelvin`
        Corresponding virtual potential temperature of the parcel

    Examples
    --------
    >>> from mipylib.meteolib.calc import virtual_potential_temperature
    >>> from mipylib.meteolib import constants as cons
    >>> virtual_potential_temperature(500, -15 + cons.degCtoK, 1e-3)
    <(314.87946, 'kelvin')>

    Notes
    -----
    .. math:: \Theta_v = \Theta \frac{\text{w} + \epsilon}{\epsilon\,(1 + \text{w})}

    .. versionchanged:: 1.0
       Renamed ``mixing`` parameter to ``mixing_ratio``

    """
    pottemp = potential_temperature(pressure, temperature)
    return virtual_temperature(pottemp, mixing_ratio, molecular_weight_ratio)


def density(pressure, temperature, mixing_ratio, molecular_weight_ratio=constants.epsilon):
    r"""Calculate density.

    This calculation must be given an air parcel's pressure, temperature, and mixing ratio.
    The implementation uses the formula outlined in [Hobbs2006]_ pg.67.

    Parameters
    ----------
    pressure: `hPa`
        Total atmospheric pressure

    temperature: `kelvin`
        Air temperature (or the virtual temperature if the mixing ratio is set to 0)

    mixing_ratio : `dimensionless`
        Mass mixing ratio (dimensionless)

    molecular_weight_ratio : `dimensionless` or float, optional
        The ratio of the molecular weight of the constituent gas to that assumed
        for air. Defaults to the ratio for water vapor to dry air.
        (:math:`\epsilon\approx0.622`)

    Returns
    -------
    `kg/m**3`
        Corresponding density of the parcel

    Examples
    --------
    >>> from mipylib.meteolib.calc import density
    >>> from mipylib.meteolib import constants as cons
    >>> density(1000, 10 + cons.degCtoK, 24e3)
    <(1.21307146, 'kilogram / meter ** 3')>

    Notes
    -----
    .. math:: \rho = \frac{\epsilon p\,(1+w)}{R_dT\,(w+\epsilon)}

    """
    virttemp = virtual_temperature(temperature, mixing_ratio, molecular_weight_ratio)
    return pressure * 100 / (constants.Rd * virttemp)


def relative_humidity_wet_psychrometric(pressure, dry_bulb_temperature, wet_bulb_temperature,
                                        **kwargs):
    r"""Calculate the relative humidity with wet bulb and dry bulb temperatures.

    This uses a psychrometric relationship as outlined in [WMO8]_, with
    coefficients from [Fan1987]_.

    Parameters
    ----------
    pressure: `hPa`
        Total atmospheric pressure

    dry_bulb_temperature: `kelvin`
        Dry bulb temperature

    wet_bulb_temperature: `kelvin`
        Wet bulb temperature

    Returns
    -------
    `dimensionless`
        Relative humidity

    Examples
    --------
    >>> from mipylib.meteolib.calc import relative_humidity_wet_psychrometric
    >>> from mipylib.meteolib import constants as cons
    >>> relative_humidity_wet_psychrometric(1000, 19 + cons.degCtoK,
    ...                                     10 + cons.degCtoK)
    <(30.4333697, 'percent')>

    See Also
    --------
    psychrometric_vapor_pressure_wet, saturation_vapor_pressure

    Notes
    -----
    .. math:: RH = \frac{e}{e_s}

    * :math:`RH` is relative humidity as a unitless ratio
    * :math:`e` is vapor pressure from the wet psychrometric calculation
    * :math:`e_s` is the saturation vapor pressure

    .. versionchanged:: 1.0
       Changed signature from
       ``(dry_bulb_temperature, web_bulb_temperature, pressure, **kwargs)``

    """
    return (psychrometric_vapor_pressure_wet(pressure, dry_bulb_temperature,
                                             wet_bulb_temperature, **kwargs)
            / saturation_vapor_pressure(dry_bulb_temperature))


def psychrometric_vapor_pressure_wet(pressure, dry_bulb_temperature, wet_bulb_temperature,
                                     psychrometer_coefficient=None):
    r"""Calculate the vapor pressure with wet bulb and dry bulb temperatures.

    This uses a psychrometric relationship as outlined in [WMO8]_, with
    coefficients from [Fan1987]_.

    Parameters
    ----------
    pressure: `hPa`
        Total atmospheric pressure

    dry_bulb_temperature: `kelvin`
        Dry bulb temperature

    wet_bulb_temperature: `kelvin`
        Wet bulb temperature

    psychrometer_coefficient: `K^-1`, optional
        Psychrometer coefficient. Defaults to 6.21e-4 K^-1.

    Returns
    -------
    `hPa`
        Vapor pressure

    Examples
    --------
    >>> from mipylib.meteolib.calc import psychrometric_vapor_pressure_wet, saturation_vapor_pressure
    >>> from mipylib.meteolib import constants as cons
    >>> vp = psychrometric_vapor_pressure_wet(958, 25 + cons.degCtoK,
    ...                                       12 + cons.degCtoK)
    >>> print('Vapor Pressure: {:.2f}'.format(vp))
    Vapor Pressure: 627.52 pascal
    >>> rh = vp / saturation_vapor_pressure(25 + cons.degCtoK)
    >>> print('RH: {:.2f}'.format(rh))
    RH: 19.84 percent

    See Also
    --------
    saturation_vapor_pressure

    Notes
    -----
    .. math:: e' = e'_w(T_w) - A p (T - T_w)

    * :math:`e'` is vapor pressure
    * :math:`e'_w(T_w)` is the saturation vapor pressure with respect to water at temperature
      :math:`T_w`
    * :math:`p` is the pressure of the wet bulb
    * :math:`T` is the temperature of the dry bulb
    * :math:`T_w` is the temperature of the wet bulb
    * :math:`A` is the psychrometer coefficient

    Psychrometer coefficient depends on the specific instrument being used and the ventilation
    of the instrument.

    .. versionchanged:: 1.0
       Changed signature from
       ``(dry_bulb_temperature, wet_bulb_temperature, pressure, psychrometer_coefficient)``

    """
    if psychrometer_coefficient is None:
        psychrometer_coefficient = 6.21e-4    #'1/K'
    return (saturation_vapor_pressure(wet_bulb_temperature) - psychrometer_coefficient
            * pressure * (dry_bulb_temperature - wet_bulb_temperature))


def dry_static_energy(height, temperature):
    r"""Calculate the dry static energy of parcels.

    This function will calculate the dry static energy following the first two terms of
    equation 3.72 in [Hobbs2006]_.

    Parameters
    ----------
    height : `array`
        Atmospheric height
    temperature : `array`
        Air temperature

    Returns
    -------
    `array`
        Dry static energy

    See Also
    --------
    montgomery_streamfunction

    Notes
    -----
    .. math:: \text{dry static energy} = c_{pd} T + gz

    * :math:`T` is temperature
    * :math:`z` is height
    """
    return constants.g * height + constants.Cp_d * temperature


def cape_cin(pressure, temperature, dewpoint, parcel_profile, which_lfc='bottom',
             which_el='top'):
    r"""Calculate CAPE and CIN.

    Calculate the convective available potential energy (CAPE) and convective inhibition (CIN)
    of a given upper air profile and parcel path. CIN is integrated between the surface and
    LFC, CAPE is integrated between the LFC and EL (or top of sounding). Intersection points
    of the measured temperature profile and parcel profile are logarithmically interpolated.

    Parameters
    ----------
    pressure : `hPa`
        Atmospheric pressure level(s) of interest, in order from highest to
        lowest pressure

    temperature : `kelvin`
        Atmospheric temperature corresponding to pressure

    dewpoint : `kelvin`
        Atmospheric dewpoint corresponding to pressure

    parcel_profile : `kelvin`
        Temperature profile of the parcel

    which_lfc : str
        Choose which LFC to integrate from. Valid options are 'top', 'bottom', 'wide',
        and 'most_cape'. Default is 'bottom'.

    which_el : str
        Choose which EL to integrate to. Valid options are 'top', 'bottom', 'wide',
        and 'most_cape'. Default is 'top'.

    Returns
    -------
    `J/Kg`
        Convective Available Potential Energy (CAPE)

    `J/Kg`
        Convective Inhibition (CIN)

    Examples
    --------
    >>> from mipylib.meteolib.calc import cape_cin, dewpoint_from_relative_humidity, parcel_profile
    >>> from mipylib.meteolib import constants as cons
    >>> # pressure
    >>> p = [1008., 1000., 950., 900., 850., 800., 750., 700., 650., 600.,
    ...      550., 500., 450., 400., 350., 300., 250., 200.,
    ...      175., 150., 125., 100., 80., 70., 60., 50.,
    ...      40., 30., 25., 20.]
    >>> # temperature
    >>> T = array([29.3, 28.1, 23.5, 20.9, 18.4, 15.9, 13.1, 10.1, 6.7, 3.1,
    ...      -0.5, -4.5, -9.0, -14.8, -21.5, -29.7, -40.0, -52.4,
    ...      -59.2, -66.5, -74.1, -78.5, -76.0, -71.6, -66.7, -61.3,
    ...      -56.3, -51.7, -50.7, -47.5]) + cons.degCtoK
    >>> # relative humidity
    >>> rh = [.85, .65, .36, .39, .82, .72, .75, .86, .65, .22, .52,
    ...       .66, .64, .20, .05, .75, .76, .45, .25, .48, .76, .88,
    ...       .56, .88, .39, .67, .15, .04, .94, .35]
    >>> # calculate dewpoint
    >>> Td = dewpoint_from_relative_humidity(T, rh)
    >>> # compture parcel temperature
    >>> prof = parcel_profile(p, T[0], Td[0])
    >>> # calculate surface based CAPE/CIN
    >>> cape_cin(p, T, Td, prof)
    (<(4830.74608, 'joule / kilogram')>, <(0, 'joule / kilogram')>)

    See Also
    --------
    lfc, el

    Notes
    -----
    Formula adopted from [Hobbs1977]_.

    .. math:: \text{CAPE} = -R_d \int_{LFC}^{EL}
            (T_{{v}_{parcel}} - T_{{v}_{env}}) d\text{ln}(p)

    .. math:: \text{CIN} = -R_d \int_{SFC}^{LFC}
            (T_{{v}_{parcel}} - T_{{v}_{env}}) d\text{ln}(p)


    * :math:`CAPE` is convective available potential energy
    * :math:`CIN` is convective inhibition
    * :math:`LFC` is pressure of the level of free convection
    * :math:`EL` is pressure of the equilibrium level
    * :math:`SFC` is the level of the surface or beginning of parcel path
    * :math:`R_d` is the gas constant
    * :math:`g` is gravitational acceleration
    * :math:`T_{{v}_{parcel}}` is the parcel virtual temperature
    * :math:`T_{{v}_{env}}` is environment virtual temperature
    * :math:`p` is atmospheric pressure

    Only functions on 1D profiles (not higher-dimension vertical cross sections or grids).
    Since this function returns scalar values when given a profile, this will return Pint
    Quantities even when given xarray DataArray profiles.

    """
    pressure, temperature, dewpoint, parcel_profile = _remove_nans(pressure, temperature,
                                                                   dewpoint, parcel_profile)

    pressure_lcl, _ = lcl(pressure[0], temperature[0], dewpoint[0])
    below_lcl = pressure > pressure_lcl

    # The mixing ratio of the parcel comes from the dewpoint below the LCL, is saturated
    # based on the temperature above the LCL
    parcel_mixing_ratio = np.where(
        below_lcl,
        saturation_mixing_ratio(pressure[0], dewpoint[0]),
        saturation_mixing_ratio(pressure, parcel_profile)
    )

    # Convert the temperature/parcel profile to virtual temperature
    temperature = virtual_temperature_from_dewpoint(pressure, temperature, dewpoint)
    parcel_profile = virtual_temperature(parcel_profile, parcel_mixing_ratio)

    # Calculate LFC limit of integration
    lfc_pressure, _ = lfc(pressure, temperature, dewpoint,
                          parcel_temperature_profile=parcel_profile, which=which_lfc)

    # If there is no LFC, no need to proceed.
    if np.isnan(lfc_pressure):
        return 0, 0

    # Calculate the EL limit of integration
    el_pressure, _ = el(pressure, temperature, dewpoint,
                        parcel_temperature_profile=parcel_profile, which=which_el)

    # No EL and we use the top reading of the sounding.
    el_pressure = pressure[-1] if np.isnan(el_pressure) else el_pressure

    # Difference between the parcel path and measured temperature profiles
    y = parcel_profile - temperature

    # Estimate zero crossings
    x, y = _find_append_zero_crossings(pressure.copy(), y)

    # CAPE
    # Only use data between the LFC and EL for calculation
    p_mask = _less_or_close(x, lfc_pressure) & _greater_or_close(x, el_pressure)
    x_clipped = x[p_mask]
    y_clipped = y[p_mask]
    cape = (constants.Rd * np.trapezoid(y_clipped, np.log(x_clipped)))

    # CIN
    # Only use data between the surface and LFC for calculation
    p_mask = _greater_or_close(x, lfc_pressure)
    x_clipped = x[p_mask]
    y_clipped = y[p_mask]
    cin = (constants.Rd * np.trapezoid(y_clipped, np.log(x_clipped)))

    # Set CIN to 0 if it's returned as a positive value (#1190)
    if cin > 0:
        cin = 0
    return cape, cin


def _find_append_zero_crossings(x, y):
    r"""
    Find and interpolate zero crossings.

    Estimate the zero crossings of an x,y series and add estimated crossings to series,
    returning a sorted array with no duplicate values.

    Parameters
    ----------
    x : `array-like`
        X values of data

    y : `array-like`
        Y values of data

    Returns
    -------
    x : `array-like`
        X values of data
    y : `array-like`
        Y values of data

    """
    crossings = find_intersections(x[1:], y[1:],
                                   np.zeros_like(y[1:]), log_x=True)
    x = np.concatenate((x, crossings[0]))
    y = np.concatenate((y, crossings[1]))

    # Resort so that data are in order
    sort_idx = np.argsort(x)
    x = x[sort_idx]
    y = y[sort_idx]

    # Remove duplicate data points if there are any
    keep_idx = np.ediff1d(x, to_end=[1]) > 1e-6
    x = x[keep_idx]
    y = y[keep_idx]
    return x, y


def most_unstable_parcel(pressure, temperature, dewpoint, height=None, bottom=None,
                         depth=None):
    """
    Determine the most unstable parcel in a layer.

    Determines the most unstable parcel of air by calculating the equivalent
    potential temperature and finding its maximum in the specified layer.

    Parameters
    ----------
    pressure: `hPa`
        Atmospheric pressure profile

    temperature: `kelvin`
        Atmospheric temperature profile

    dewpoint: `kelvin`
        Atmospheric dewpoint profile

    height: `meter`, optional
        Atmospheric height profile. Standard atmosphere assumed when None (the default).

    bottom: `hPa or meter`, optional
        Bottom of the layer to consider for the calculation in pressure or height.
        Defaults to using the bottom pressure or height.

    depth: `hPa or meter`, optional
        Depth of the layer to consider for the calculation in pressure or height. Defaults
        to 300 hPa.

    Returns
    -------
    `hPa`
        Pressure of the most unstable parcel in the profile

    `kelvin`
        Temperature of the most unstable parcel in the profile

    `kelvin`
        Dewpoint of the most unstable parcel in the profile

    int
        The index of the most unstable parcel within the original data

    Examples
    --------
    >>> from mipylib.meteolib.calc import dewpoint_from_relative_humidity, most_unstable_parcel
    >>> from mipylib.meteolib import constants as cons
    >>> # pressure
    >>> p = [1008., 1000., 950., 900., 850., 800., 750., 700., 650., 600.,
    ...      550., 500., 450., 400., 350., 300., 250., 200.,
    ...      175., 150., 125., 100., 80., 70., 60., 50.,
    ...      40., 30., 25., 20.]
    >>> # temperature
    >>> T = array([29.3, 28.1, 23.5, 20.9, 18.4, 15.9, 13.1, 10.1, 6.7, 3.1,
    ...      -0.5, -4.5, -9.0, -14.8, -21.5, -29.7, -40.0, -52.4,
    ...      -59.2, -66.5, -74.1, -78.5, -76.0, -71.6, -66.7, -61.3,
    ...      -56.3, -51.7, -50.7, -47.5]) + cons.degCtoK
    >>> # relative humidity
    >>> rh = [.85, .65, .36, .39, .82, .72, .75, .86, .65, .22, .52,
    ...       .66, .64, .20, .05, .75, .76, .45, .25, .48, .76, .88,
    ...       .56, .88, .39, .67, .15, .04, .94, .35]
    >>> # calculate dewpoint
    >>> Td = dewpoint_from_relative_humidity(T, rh)
    >>> # find most unstable parcel of depth 50 hPa
    >>> most_unstable_parcel(p, T, Td, depth=50)
    (<(1008.0, 'hectopascal')>, <(29.3, 'degree_Celsius')>,
    <(26.4766738, 'degree_Celsius')>, 0)

    See Also
    --------
    get_layer

    Notes
    -----
    Only functions on 1D profiles (not higher-dimension vertical cross sections or grids).
    Since this function returns scalar values when given a profile, this will return Pint
    Quantities even when given xarray DataArray profiles.

    """
    if depth is None:
        depth = 300    #'hPa'
    p_layer, t_layer, td_layer = get_layer(pressure, temperature, dewpoint, bottom=bottom,
                                           depth=depth, height=height, interpolate=False)
    theta_e = equivalent_potential_temperature(p_layer, t_layer, td_layer)
    max_idx = np.argmax(theta_e)
    return p_layer[max_idx], t_layer[max_idx], td_layer[max_idx], max_idx


def isentropic_interpolation(levels, pressure, temperature, *args, **kwargs):
    r"""Interpolate data in isobaric coordinates to isentropic coordinates.

    Parameters
    ----------
    levels : array
        One-dimensional array of desired potential temperature surfaces
    pressure : array
        One-dimensional array of pressure levels
    temperature : array
        Array of temperature
    vertical_dim : int, optional
        The axis corresponding to the vertical in the temperature array, defaults to 0.
    temperature_out : bool, optional
        If true, will calculate temperature and output as the last item in the output list.
        Defaults to False.
    max_iters : int, optional
        Maximum number of iterations to use in calculation, defaults to 50.
    eps : float, optional
        The desired absolute error in the calculated value, defaults to 1e-6.
    bottom_up_search : bool, optional
        Controls whether to search for levels bottom-up, or top-down. Defaults to
        True, which is bottom-up search.
    args : array, optional
        Any additional variables will be interpolated to each isentropic level

    Returns
    -------
    list
        List with pressure at each isentropic level, followed by each additional
        argument interpolated to isentropic coordinates.

    See Also
    --------
    potential_temperature, isentropic_interpolation_as_dataset

    Notes
    -----
    Input variable arrays must have the same number of vertical levels as the pressure levels
    array. Pressure is calculated on isentropic surfaces by assuming that temperature varies
    linearly with the natural log of pressure. Linear interpolation is then used in the
    vertical to find the pressure at each isentropic level. Interpolation method from
    [Ziv1994]_. Any additional arguments are assumed to vary linearly with temperature and will
    be linearly interpolated to the new isentropic levels.
    Will only return Pint Quantities, even when given xarray DataArray profiles. To
    obtain a xarray Dataset instead, use `isentropic_interpolation_as_dataset` instead.
    """
    # iteration function to be used later
    # Calculates theta from linearly interpolated temperature and solves for pressure
    def _isen_iter(iter_log_p, isentlevs_nd, ka, a, b, pok):
        exner = pok * np.exp(-ka * iter_log_p)
        t = a * iter_log_p + b
        # Newton-Raphson iteration
        f = isentlevs_nd - t * exner
        fp = exner * (ka * t - a)
        return iter_log_p - (f / fp)

    # Get dimensions in temperature
    ndim = temperature.ndim

    # Convert units
    #pressure = pressure.to('hPa')
    #temperature = temperature.to('kelvin')
    vertical_dim = kwargs.pop('vertical_dim', 0)
    slices = [np.newaxis] * ndim
    slices[vertical_dim] = slice(None)
    slices = tuple(slices)
    pressure = np.broadcast_to(pressure[slices], temperature.shape)

    # Sort input data
    sort_pressure = np.argsort(pressure, axis=vertical_dim)
    sort_pressure = np.swapaxes(np.swapaxes(sort_pressure, 0, vertical_dim)[::-1], 0,
                                vertical_dim)
    sorter = broadcast_indices(pressure, sort_pressure, ndim, vertical_dim)
    levs = pressure[sorter]
    tmpk = temperature[sorter]

    levels = np.asarray(levels).reshape(-1)
    isentlevels = levels[np.argsort(levels)]

    # Make the desired isentropic levels the same shape as temperature
    shape = list(temperature.shape)
    shape[vertical_dim] = isentlevels.size
    isentlevs_nd = np.broadcast_to(isentlevels[slices], shape)

    # exponent to Poisson's Equation, which is imported above
    ka = constants.kappa

    # calculate theta for each point
    pres_theta = potential_temperature(levs, tmpk)

    # Raise error if input theta level is larger than pres_theta max
    if np.max(pres_theta) < np.max(levels):
        raise ValueError('Input theta level out of data bounds')

    # Find log of pressure to implement assumption of linear temperature dependence on
    # ln(p)
    log_p = np.log(levs)

    # Calculations for interpolation routine
    pok = constants.P0 ** ka

    # index values for each point for the pressure level nearest to the desired theta level
    bottom_up_search = kwargs.pop('bottom_up_search', True)
    above, below, good = find_bounding_indices(pres_theta, levels, vertical_dim,
                                               from_below=bottom_up_search)

    # calculate constants for the interpolation
    a = (tmpk[above] - tmpk[below]) / (log_p[above] - log_p[below])
    b = tmpk[above] - a * log_p[above]

    # calculate first guess for interpolation
    isentprs = 0.5 * (log_p[above] + log_p[below])

    # Make sure we ignore any nans in the data for solving; checking a is enough since it
    # combines log_p and tmpk.
    good &= ~np.isnan(a)

    # iterative interpolation using scipy.optimize.fixed_point and _isen_iter defined above
    max_iters = kwargs.pop('max_iters', 50)
    eps = kwargs.pop('eps', 1e-6)
    log_p_solved = so.fixed_point(_isen_iter, isentprs[good],
                                  args=(isentlevs_nd[good], ka, a[good], b[good], pok),
                                  xtol=eps, maxiter=max_iters)

    # get back pressure from log p
    isentprs[good] = np.exp(log_p_solved)

    # Mask out points we know are bad as well as points that are beyond the max pressure
    isentprs[~(good & _less_or_close(isentprs, np.max(pressure)))] = np.nan

    # create list for storing output data
    ret = [isentprs]

    # if temperature_out = true, calculate temperature and output as last item in list
    temperature_out = kwargs.pop('temperature_out', False)
    if temperature_out:
        ret.append(isentlevs_nd / ((constants.P0 / isentprs) ** ka))

    # do an interpolation for each additional argument
    if args:
        others = interpolate_1d(isentlevels, pres_theta, *(arr[sorter] for arr in args),
                                axis=vertical_dim, return_list_always=True)
        ret.extend(others)

    return ret


def surface_based_cape_cin(pressure, temperature, dewpoint):
    r"""Calculate surface-based CAPE and CIN.

    Calculate the convective available potential energy (CAPE) and convective inhibition (CIN)
    of a given upper air profile for a surface-based parcel. CIN is integrated
    between the surface and LFC, CAPE is integrated between the LFC and EL (or top of
    sounding). Intersection points of the measured temperature profile and parcel profile are
    logarithmically interpolated.

    Parameters
    ----------
    pressure : `hPa`
        Atmospheric pressure profile. The first entry should be the starting
        (surface) observation, with the array going from high to low pressure.

    temperature : `kelvin`
        Temperature profile corresponding to the `pressure` profile

    dewpoint : `kelvin`
        Dewpoint profile corresponding to the `pressure` profile

    Returns
    -------
    `J/Kg`
        Surface based Convective Available Potential Energy (CAPE)

    `J/kg`
        Surface based Convective Inhibition (CIN)

    See Also
    --------
    cape_cin, parcel_profile

    Notes
    -----
    Only functions on 1D profiles (not higher-dimension vertical cross sections or grids).
    Since this function returns scalar values when given a profile, this will return Pint
    Quantities even when given xarray DataArray profiles.

    """
    pressure, temperature, dewpoint = _remove_nans(pressure, temperature, dewpoint)
    p, t, td, profile = parcel_profile_with_lcl(pressure, temperature, dewpoint)
    return cape_cin(p, t, td, profile)


def most_unstable_cape_cin(pressure, temperature, dewpoint, **kwargs):
    r"""Calculate most unstable CAPE/CIN.

    Calculate the convective available potential energy (CAPE) and convective inhibition (CIN)
    of a given upper air profile and most unstable parcel path. CIN is integrated between the
    surface and LFC, CAPE is integrated between the LFC and EL (or top of sounding).
    Intersection points of the measured temperature profile and parcel profile are
    logarithmically interpolated.

    Parameters
    ----------
    pressure : `hPa`
        Pressure profile

    temperature : `Kelvin`
        Temperature profile

    dewpoint : `Kelvin`
        Dew point profile

    kwargs
        Additional keyword arguments to pass to `most_unstable_parcel`

    Returns
    -------
    `J/Kg`
        Most unstable Convective Available Potential Energy (CAPE)

    `J/Kg`
        Most unstable Convective Inhibition (CIN)

    Examples
    --------
    >>> from mipylib.meteolib.calc import dewpoint_from_relative_humidity, most_unstable_cape_cin
    >>> from mipylib.meteolib import constants as cons
    >>> # pressure
    >>> p = [1008., 1000., 950., 900., 850., 800., 750., 700., 650., 600.,
    ...      550., 500., 450., 400., 350., 300., 250., 200.,
    ...      175., 150., 125., 100., 80., 70., 60., 50.,
    ...      40., 30., 25., 20.]
    >>> # temperature
    >>> T = array([29.3, 28.1, 23.5, 20.9, 18.4, 15.9, 13.1, 10.1, 6.7, 3.1,
    ...      -0.5, -4.5, -9.0, -14.8, -21.5, -29.7, -40.0, -52.4,
    ...      -59.2, -66.5, -74.1, -78.5, -76.0, -71.6, -66.7, -61.3,
    ...      -56.3, -51.7, -50.7, -47.5]) + cons.degCtoK
    >>> # relative humidity
    >>> rh = [.85, .65, .36, .39, .82, .72, .75, .86, .65, .22, .52,
    ...       .66, .64, .20, .05, .75, .76, .45, .25, .48, .76, .88,
    ...       .56, .88, .39, .67, .15, .04, .94, .35]
    >>> # calculate dewpoint
    >>> Td = dewpoint_from_relative_humidity(T, rh)
    >>> # calculate most unstbale CAPE/CIN
    >>> most_unstable_cape_cin(p, T, Td)
    (<(4830.74608, 'joule / kilogram')>, <(0, 'joule / kilogram')>)

    See Also
    --------
    cape_cin, most_unstable_parcel, parcel_profile

    Notes
    -----
    Only functions on 1D profiles (not higher-dimension vertical cross sections or grids).
    Since this function returns scalar values when given a profile, this will return Pint
    Quantities even when given xarray DataArray profiles.

    """
    pressure, temperature, dewpoint = _remove_nans(pressure, temperature, dewpoint)
    _, _, _, parcel_idx = most_unstable_parcel(pressure, temperature, dewpoint, **kwargs)
    p, t, td, mu_profile = parcel_profile_with_lcl(pressure[parcel_idx:],
                                                   temperature[parcel_idx:],
                                                   dewpoint[parcel_idx:])
    return cape_cin(p, t, td, mu_profile)


def mixed_layer_cape_cin(pressure, temperature, dewpoint, **kwargs):
    r"""Calculate mixed-layer CAPE and CIN.

    Calculate the convective available potential energy (CAPE) and convective inhibition (CIN)
    of a given upper air profile and mixed-layer parcel path. CIN is integrated between the
    surface and LFC, CAPE is integrated between the LFC and EL (or top of sounding).
    Intersection points of the measured temperature profile and parcel profile are
    logarithmically interpolated. Kwargs for `mixed_parcel` can be provided, such as `depth`.
    Default mixed-layer depth is 100 hPa.

    Parameters
    ----------
    pressure : `hPa`
        Pressure profile

    temperature : `kelvin`
        Temperature profile

    dewpoint : `kelvin`
        Dewpoint profile

    kwargs
        Additional keyword arguments to pass to `mixed_parcel`

    Returns
    -------
    `J/Kg`
        Mixed-layer Convective Available Potential Energy (CAPE)
    `J/Kg`
        Mixed-layer Convective INhibition (CIN)

    Examples
    --------
    >>> from mipylib.meteolib.calc import dewpoint_from_relative_humidity, mixed_layer_cape_cin
    >>> from mipylib.meteolib import contants as cons
    >>> # pressure
    >>> p = [1008., 1000., 950., 900., 850., 800., 750., 700., 650., 600.,
    ...      550., 500., 450., 400., 350., 300., 250., 200.,
    ...      175., 150., 125., 100., 80., 70., 60., 50.,
    ...      40., 30., 25., 20.]
    >>> # temperature
    >>> T = array([29.3, 28.1, 25.5, 20.9, 18.4, 15.9, 13.1, 10.1, 6.7, 3.1,
    ...      -0.5, -4.5, -9.0, -14.8, -21.5, -29.7, -40.0, -52.4,
    ...      -59.2, -66.5, -74.1, -78.5, -76.0, -71.6, -66.7, -61.3,
    ...      -56.3, -51.7, -50.7, -47.5]) + cons.degCtoK
    >>> # relative humidity
    >>> rh = [.85, .75, .56, .39, .82, .72, .75, .86, .65, .22, .52,
    ...       .66, .64, .20, .05, .75, .76, .45, .25, .48, .76, .88,
    ...       .56, .88, .39, .67, .15, .04, .94, .35]
    >>> # calculate dewpoint
    >>> Td = dewpoint_from_relative_humidity(T, rh)
    >>> mixed_layer_cape_cin(p, T, Td, depth=50)
    (<(678.967843, 'joule / kilogram')>, <(0, 'joule / kilogram')>)

    See Also
    --------
    cape_cin, mixed_parcel, parcel_profile

    Notes
    -----
    Only functions on 1D profiles (not higher-dimension vertical cross sections or grids).
    Since this function returns scalar values when given a profile, this will return Pint
    Quantities even when given xarray DataArray profiles.

    """
    depth = kwargs.get('depth', 100)
    start_p = kwargs.get('parcel_start_pressure', pressure[0])
    parcel_pressure, parcel_temp, parcel_dewpoint = mixed_parcel(pressure, temperature,
                                                                 dewpoint, **kwargs)

    # Remove values below top of mixed layer and add in the mixed layer values
    pressure_prof = pressure[pressure < (start_p - depth)]
    temp_prof = temperature[pressure < (start_p - depth)]
    dew_prof = dewpoint[pressure < (start_p - depth)]
    pressure_prof = np.concatenate([parcel_pressure, pressure_prof])
    temp_prof = np.concatenate([parcel_temp, temp_prof])
    dew_prof = np.concatenate([parcel_dewpoint, dew_prof])

    p, t, td, ml_profile = parcel_profile_with_lcl(pressure_prof, temp_prof, dew_prof)
    return cape_cin(p, t, td, ml_profile)


def wet_bulb_temperature(pressure, temperature, dewpoint):
    """Calculate the wet-bulb temperature using Normand's rule.

    This function calculates the wet-bulb temperature using the Normand method. The LCL is
    computed, and that parcel brought down to the starting pressure along a moist adiabat.
    The Normand method (and others) are described and compared by [Knox2017]_.

    Parameters
    ----------
    pressure : `hPa`
        Initial atmospheric pressure

    temperature : `kelvin`
        Initial atmospheric temperature

    dewpoint : `kelvin`
        Initial atmospheric dewpoint

    Returns
    -------
    `kelvin`
        Wet-bulb temperature

    Examples
    --------
    >>> from mipylib.meteolib.calc import wet_bulb_temperature
    >>> from mipylib.meteolib import constants as cons
    >>> wet_bulb_temperature(993, 32 + cons.degCtoK, 15 + cons.degCtoK)
    <(20.3937601, 'degree_Celsius')>

    See Also
    --------
    lcl, moist_lapse

    Notes
    -----
    Since this function iteratively applies a parcel calculation, it should be used with
    caution on large arrays.

    """
    if not getattr(pressure, 'shape', False):
        pressure = np.atleast_1d(pressure)
        temperature = np.atleast_1d(temperature)
        dewpoint = np.atleast_1d(dewpoint)

    lcl_press, lcl_temp = lcl(pressure, temperature, dewpoint)

    ret = np.zeros_like(pressure)
    ret_flat = ret.flat
    i = 0
    for press, lpress, ltemp in zip(pressure.flat, lcl_press.flat, lcl_temp.flat):
        ml = moist_lapse(press, ltemp, lpress)
        ret_flat[i] = ml.item()
        i += 1

    # If we started with a scalar, return a scalar
    if ret.size == 1:
        ret = ret[0]
    return ret


def dewpoint_from_relative_humidity(temperature, relative_humidity):
    r"""Calculate the ambient dewpoint given air temperature and relative humidity.
    Parameters
    ----------
    temperature : `kelvin`
        Air temperature

    relative_humidity : `dimensionless`
        Relative humidity expressed as a ratio in the range 0 < relative_humidity <= 1

    Returns
    -------
    `kelvin`
        The dew point temperature

    Examples
    --------
    >>> from mipylib.meteolib.calc import dewpoint_from_relative_humidity
    >>> from mipylib.meteolib import constants as cons
    >>> dewpoint_from_relative_humidity(10 + cons.degCtoK, 0.5) - cons.degCtoK
    <(0.047900916, 'degree_Celsius')>

    See Also
    --------
    dewpoint, saturation_vapor_pressure
    """
    if isinstance(relative_humidity, (tuple, list)):
        relative_humidity = np.asarray(relative_humidity)

    if np.any(relative_humidity > 1.2):
        warnings.warn('Relative humidity >120%, ensure proper units.')

    return dewpoint(relative_humidity * saturation_vapor_pressure(temperature))


def dewpoint(vapor_pressure):
    r"""Calculate the ambient dewpoint given the vapor pressure.

    Parameters
    ----------
    vapor_pressure : `array`
        Water vapor partial pressure

    Returns
    -------
    `array`
        Dew point temperature

    See Also
    --------
    dewpoint_rh, saturation_vapor_pressure, vapor_pressure

    Notes
    -----
    This function inverts the [Bolton1980]_ formula for saturation vapor
    pressure to instead calculate the temperature. This yield the following
    formula for dewpoint in degrees Celsius:

    .. math:: T = \frac{243.5 log(e / 6.112)}{17.67 - log(e / 6.112)}
    """
    val = np.log(vapor_pressure / constants.sat_pressure_0c)
    return constants.nounit.zero_degc + 243.5 * val / (17.67 - val)


def specific_humidity_from_mixing_ratio(mixing_ratio):
    r"""Calculate the specific humidity from the mixing ratio.

    Parameters
    ----------
    mixing_ratio: `kg/kg`
        Mixing ratio

    Returns
    -------
    `kg/kg`
        Specific humidity

    Examples
    --------
    >>> from mipylib.meteolib.calc import specific_humidity_from_mixing_ratio
    >>> specific_humidity_from_mixing_ratio(19e-3)
    <(0.186457311, 'kg / kg')>

    See Also
    --------
    mixing_ratio, mixing_ratio_from_specific_humidity

    Notes
    -----
    Formula from [Salby1996]_ pg. 118.

    .. math:: q = \frac{w}{1+w}

    * :math:`w` is mixing ratio
    * :math:`q` is the specific humidity
    """
    return mixing_ratio / (1 + mixing_ratio)


def specific_humidity_from_dewpoint(pressure, dewpoint, phase='liquid'):
    r"""Calculate the specific humidity from the dewpoint temperature and pressure.

    Parameters
    ----------
    dewpoint: `kelvin`
        Dewpoint temperature
    pressure: `hPa`
        Pressure

    phase : {'liquid', 'solid', 'auto'}
        Where applicable, adjust assumptions and constants to make calculation valid in
        ``'liquid'`` water (default) or ``'solid'`` ice regimes. ``'auto'`` will change regime
        based on determination of phase boundaries, eg `temperature` relative to freezing.

    Returns
    -------
    `dimensionless`
        Specific humidity

    See Also
    --------
    mixing_ratio, saturation_mixing_ratio
    """
    validate_choice({'liquid', 'solid', 'auto'}, phase=phase)
    mixing_ratio = saturation_mixing_ratio(pressure, dewpoint, phase=phase)
    return specific_humidity_from_mixing_ratio(mixing_ratio)


def specific_humidity_from_relative_humidity(pressure, temperature, rh):
    """Calculate specific humidity from relative humidity, pressure and temperature.

    Parameters
    ----------
    pressure: `hPa`
        Pressure
    temperature: `kelvin`
        temperature
    rh: `dimensionless`
        relative humidity

    Returns
    -------
    `dimensionless`
        Specific humidity
    """
    dp = dewpoint_from_relative_humidity(temperature, rh)
    return specific_humidity_from_dewpoint(pressure, dp)


def lifted_index(pressure, temperature, parcel_profile, vertical_dim=0):
    """Calculate Lifted Index from the pressure temperature and parcel profile.

    Lifted index formula derived from [Galway1956]_ and referenced by [DoswellSchultz2006]_:

    .. math:: LI = T500 - Tp500

    where:

    * :math:`T500` is the measured temperature at 500 hPa
    * :math:`Tp500` is the temperature of the lifted parcel at 500 hPa

    Calculation of the lifted index is defined as the temperature difference between the
    observed 500 hPa temperature and the temperature of a parcel lifted from the
    surface to 500 hPa. As noted in [Galway1956]_, a low-level mixed parcel is often used
    as the surface parcel.

    Parameters
    ----------
    pressure : `hPa`
        Atmospheric pressure level(s) of interest, in order from highest to
        lowest pressure

    temperature : `kelvin`
        Atmospheric temperature corresponding to pressure

    parcel_profile : `kelvin`
        Temperature profile of the parcel

    vertical_dim : int, optional
        The axis corresponding to vertical, defaults to 0. Automatically determined from
        xarray DataArray arguments.

    Returns
    -------
    `delta temperature`
        Lifted Index

    Examples
    --------
    >>> from mipylib.meteolib.calc import lifted_index, mixed_parcel, parcel_profile
    >>> from mipylib.meteolib import constants as cons
    >>>
    >>> # Define pressure, temperature, dewpoint temperature, and height
    >>> p = [1002., 1000., 993., 925., 850., 846., 723., 632., 479., 284.,
    ...      239., 200., 131., 91., 72.7, 54.6, 41., 30., 22.8]
    >>> T = array([28.2, 27., 25.4, 19.4, 12.8, 12.3, 4.2, 0.8, -12.7, -41.7, -52.3,
    ...      -57.5, -54.9, -57.8, -58.5, -52.3, -53.4, -50.3, -49.9]) + cons.degCtoK
    >>> Td = array([14.2, 14., 12.4, 11.4, 10.2, 10.1, -7.8, -16.2, -37.7, -55.7,
    ...       -58.3, -69.5, -85.5, -88., -89.5, -88.3, -88.4, -87.3, -87.9]) + cons.degCtoK
    >>> h = [139, 159, 221, 839, 1559, 1599, 2895, 3982, 6150, 9933, 11072,
    ...      12200, 14906, 17231, 18650, 20474, 22323, 24350, 26149]
    >>>
    >>> # Calculate 500m mixed parcel
    >>> parcel_p, parcel_t, parcel_td = mixed_parcel(p, T, Td, depth=500, height=h)
    >>>
    >>> # Replace sounding temp/pressure in lowest 500m with mixed values
    >>> above = h > 500
    >>> press = np.concatenate([[parcel_p], p[above]])
    >>> temp = np.concatenate([[parcel_t], T[above]])
    >>>
    >>> # Calculate parcel profile from our new mixed parcel
    >>> mixed_prof = parcel_profile(press, parcel_t, parcel_td)
    >>>
    >>> # Calculate lifted index using our mixed profile
    >>> lifted_index(press, temp, mixed_prof)
    <([2.54198585], 'delta_degree_Celsius')>

    See Also
    --------
    mixed_parcel, parcel_profile

    """
    # find the measured temperature and parcel profile temperature at 500 hPa.
    t500, tp500 = interpolate_1d(500,
                                 pressure, temperature, parcel_profile, axis=vertical_dim)

    # calculate the lifted index.
    return t500 - tp500


def mixed_parcel(pressure, temperature, dewpoint, parcel_start_pressure=None,
                 height=None, bottom=None, depth=None, interpolate=True):
    r"""Calculate the properties of a parcel mixed from a layer.

    Determines the properties of an air parcel that is the result of complete mixing of a
    given atmospheric layer.

    Parameters
    ----------
    pressure : `hPa`
        Atmospheric pressure profile

    temperature : `kelvin`
        Atmospheric temperature profile

    dewpoint : `kelvin`
        Atmospheric dewpoint profile

    parcel_start_pressure : `hPa`, optional
        Pressure at which the mixed parcel should begin (default None)

    height: `meter`, optional
        Atmospheric heights corresponding to the given pressures (default None)

    bottom : `hPa`, optional
        The bottom of the layer as a pressure or height above the surface pressure
        (default None)

    depth : `hPa`, optional
        The thickness of the layer as a pressure or height above the bottom of the layer
        (default 100 hPa)

    interpolate : bool, optional
        Interpolate the top and bottom points if they are not in the given data

    Returns
    -------
    `hPa`
        Pressure of the mixed parcel
    `kelvin`
        Temperature of the mixed parcel
    `kelvin`
        Dewpoint of the mixed parcel

    Examples
    --------
    >>> from mipylib.meteolib.calc import dewpoint_from_relative_humidity, mixed_parcel
    >>> from mipylib.meteolib import contants as cons
    >>> # pressure
    >>> p = [1008., 1000., 950., 900., 850., 800., 750., 700., 650., 600.,
    ...      550., 500., 450., 400., 350., 300., 250., 200.,
    ...      175., 150., 125., 100., 80., 70., 60., 50.,
    ...      40., 30., 25., 20.]
    >>> # temperature
    >>> T = array([29.3, 28.1, 23.5, 20.9, 18.4, 15.9, 13.1, 10.1, 6.7, 3.1,
    ...      -0.5, -4.5, -9.0, -14.8, -21.5, -29.7, -40.0, -52.4,
    ...      -59.2, -66.5, -74.1, -78.5, -76.0, -71.6, -66.7, -61.3,
    ...      -56.3, -51.7, -50.7, -47.5]) + cons.degCtoK
    >>> # relative humidity
    >>> rh = [.85, .65, .36, .39, .82, .72, .75, .86, .65, .22, .52,
    ...       .66, .64, .20, .05, .75, .76, .45, .25, .48, .76, .88,
    ...       .56, .88, .39, .67, .15, .04, .94, .35]
    >>> # calculate dewpoint
    >>> Td = dewpoint_from_relative_humidity(T, rh)
    >>> # find the mixed parcel of depth 50 hPa
    >>> mixed_parcel(p, T, Td, depth=50)
    (<(1008.0, 'hectopascal')>, <(28.750033, 'degree_Celsius')>,
    <(18.1504239, 'degree_Celsius')>)

    Notes
    -----
    Only functions on 1D profiles (not higher-dimension vertical cross sections or grids).
    Since this function returns scalar values when given a profile, this will return Pint
    Quantities even when given xarray DataArray profiles.

    """
    pressure = np.asarray(pressure)
    temperature = np.asarray(temperature)
    dewpoint = np.asarray(dewpoint)

    # If a parcel starting pressure is not provided, use the surface
    if not parcel_start_pressure:
        parcel_start_pressure = pressure[0]

    if depth is None:
        depth = 100    #'hPa'

    # Calculate the potential temperature and mixing ratio over the layer
    theta = potential_temperature(pressure, temperature)
    mixing_ratio = saturation_mixing_ratio(pressure, dewpoint)

    # Mix the variables over the layer
    mean_theta, mean_mixing_ratio = mixed_layer(pressure, theta, mixing_ratio, bottom=bottom,
                                                height=height, depth=depth,
                                                interpolate=interpolate)

    # Convert back to temperature
    mean_temperature = mean_theta * exner_function(parcel_start_pressure)

    # Convert back to dewpoint
    mean_vapor_pressure = vapor_pressure(parcel_start_pressure, mean_mixing_ratio)

    # Using globals() here allows us to keep the dewpoint parameter but still call the
    # function of the same name.
    mean_dewpoint = globals()['dewpoint'](mean_vapor_pressure)

    return (parcel_start_pressure, mean_temperature,
            mean_dewpoint)


def mixed_layer(pressure, *args, **kwargs):
    r"""Mix variable(s) over a layer, yielding a mass-weighted average.

    This function will integrate a data variable with respect to pressure and determine the
    average value using the mean value theorem.

    Parameters
    ----------
    pressure : `hPa`
        Atmospheric pressure profile

    datavar : array-like
        Atmospheric variable measured at the given pressures

    height: `meter`, optional
        Atmospheric heights corresponding to the given pressures (default None)

    bottom : `hPa`, optional
        The bottom of the layer as a pressure or height above the surface pressure
        (default None)

    depth : `hPa`, optional
        The thickness of the layer as a pressure or height above the bottom of the layer
        (default 100 hPa)

    interpolate : bool, optional
        Interpolate the top and bottom points if they are not in the given data (default True)

    Returns
    -------
    `array-like`
        The mixed value of the data variable

    Examples
    --------
    >>> from mipylib.meteolib.calc import dewpoint_from_relative_humidity, mixed_layer
    >>> from mipylib.meteolib import constants as cons
    >>> # pressure
    >>> p = [1008., 1000., 950., 900., 850., 800., 750., 700., 650., 600.,
    ...      550., 500., 450., 400., 350., 300., 250., 200.,
    ...      175., 150., 125., 100., 80., 70., 60., 50.,
    ...      40., 30., 25., 20.]
    >>> # temperature
    >>> T = array([29.3, 28.1, 23.5, 20.9, 18.4, 15.9, 13.1, 10.1, 6.7, 3.1,
    ...      -0.5, -4.5, -9.0, -14.8, -21.5, -29.7, -40.0, -52.4,
    ...      -59.2, -66.5, -74.1, -78.5, -76.0, -71.6, -66.7, -61.3,
    ...      -56.3, -51.7, -50.7, -47.5]) + cons.degCtoK
    >>> # relative humidity
    >>> rh = [.85, .65, .36, .39, .82, .72, .75, .86, .65, .22, .52,
    ...       .66, .64, .20, .05, .75, .76, .45, .25, .48, .76, .88,
    ...       .56, .88, .39, .67, .15, .04, .94, .35]
    >>> # calculate dewpoint
    >>> Td = dewpoint_from_relative_humidity(T, rh)
    >>> # find mixed layer T and Td of depth 50 hPa
    >>> mixed_layer(p, T, Td, depth=50)
    [<(26.5798571, 'degree_Celsius')>, <(16.6455209, 'degree_Celsius')>]

    Notes
    -----
    Only functions on 1D profiles (not higher-dimension vertical cross sections or grids).
    Since this function returns scalar values when given a profile, this will return Pint
    Quantities even when given xarray DataArray profiles.

    """
    height = kwargs.pop('height', None)
    bottom = kwargs.pop('bottom', None)
    depth = kwargs.pop('depth', None)
    interpolate = kwargs.pop('interpolate', True)

    if depth is None:
        depth = 100    #'hPa'

    layer = get_layer(pressure, *args, height=height, bottom=bottom,
                      depth=depth, interpolate=interpolate)
    p_layer = layer[0]
    datavars_layer = layer[1:]

    ret = []
    for datavar_layer in datavars_layer:
        actual_depth = abs(p_layer[0] - p_layer[-1])
        ret.append(np.trapezoid(datavar_layer, p_layer) / -actual_depth)
    return ret
