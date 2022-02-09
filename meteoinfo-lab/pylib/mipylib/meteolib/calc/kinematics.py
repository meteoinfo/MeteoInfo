"""Contains calculation of kinematic parameters (e.g. divergence or vorticity)."""

from org.meteoinfo.math.meteo import MeteoMath
import mipylib.numeric as np
from mipylib.numeric.core import NDArray, DimArray
from .tools import first_derivative
from .basic import coriolis_parameter

__all__ = [
    'cdiff','divergence','vorticity','advection','absolute_vorticity','potential_vorticity_barotropic'
    ]

def cdiff(a, dimidx):
    '''
    Performs a centered difference operation on a array in a specific direction

    :param a: (*array*) The input array.
    :param dimidx: (*int*) Demension index of the specific direction.

    :returns: Result array.
    '''
    r = MeteoMath.cdiff(a.asarray(), dimidx)
    if isinstance(a, DimArray):
        return DimArray(NDArray(r), a.dims, a.fill_value, a.proj)
    else:
        return NDArray(r)

def vorticity(u, v, dx=None, dy=None, x_dim=-1, y_dim=-2):
    r"""Calculate the vertical vorticity of the horizontal wind.

    Parameters
    ----------
    u : (..., M, N) `array`
        x component of the wind
    v : (..., M, N) `array`
        y component of the wind
    dx : `array`, optional
        The grid spacing(s) in the x-direction. If an array, there should be one item less than
        the size of `u` along the applicable axis. Optional if `DimArray` with
        latitude/longitude coordinates used as input. Keyword-only argument.
    dy : `array`, optional
        The grid spacing(s) in the y-direction. If an array, there should be one item less than
        the size of `u` along the applicable axis. Optional if `DimArray` with
        latitude/longitude coordinates used as input. Keyword-only argument.
    x_dim : int, optional
        Axis number of x dimension. Defaults to -1 (implying [..., Y, X] order). Automatically
        parsed from input if using `DimArray`. Keyword-only argument.
    y_dim : int, optional
        Axis number of y dimension. Defaults to -2 (implying [..., Y, X] order). Automatically
        parsed from input if using `DimArray`. Keyword-only argument.

    Returns
    -------
    (..., M, N) `array`
        vertical vorticity

    See Also
    --------
    divergence

    Notes
    -----
    This implements a numerical version of the typical vertical vorticity equation in
    Cartesian coordinates:

    .. math:: \zeta = \frac{\partial v}{\partial x} - \frac{\partial u}{\partial y}

    """
    if dx is None:
        xx = u.dimvalue(x_dim)
        yy = u.dimvalue(y_dim)
        xx,yy = np.meshgrid(xx, yy)
        dudy = first_derivative(u, x=yy, axis=y_dim)
        dvdx = first_derivative(v, x=xx, axis=x_dim)
    else:
        dudy = first_derivative(u, delta=dy, axis=y_dim)
        dvdx = first_derivative(v, delta=dx, axis=x_dim)
    return dvdx - dudy

def vorticity_bak(u, v, x=None, y=None):
    """
    Calculates the vertical component of the curl (ie, vorticity). The data should be lon/lat projection.

    :param u: (*array*) U component array (2D).
    :param v: (*array*) V component array (2D).
    :param x: (*array*) X coordinate array (1D).
    :param y: (*array*) Y coordinate array (1D).

    :returns: Array of the vertical component of the curl.
    """
    ny = u.shape[-2]
    nx = u.shape[-1]
    if x is None:
        if isinstance(u, DimArray):
            x = u.dimvalue(-1)
        else:
            x = np.arange(nx)
    elif isinstance(x, (list, tuple)):
        x = np.array(x)

    if y is None:
        if isinstance(v, DimArray):
            y = u.dimvalue(-2)
        else:
            y = np.arange(ny)
    elif isinstance(y, (list, tuple)):
        y = np.array(y)

    r = MeteoMath.vorticity(u.asarray(), v.asarray(), x.asarray(), y.asarray())
    if isinstance(u, DimArray):
        return DimArray(NDArray(r), u.dims, u.fill_value, u.proj)
    else:
        return NDArray(r)

def divergence(u, v, dx=None, dy=None, x_dim=-1, y_dim=-2):
    r"""Calculate the horizontal divergence of the horizontal wind.

    Parameters
    ----------
    u : (..., M, N) `array`
        x component of the wind
    v : (..., M, N) `array`
        y component of the wind
    dx : `array`, optional
        The grid spacing(s) in the x-direction. If an array, there should be one item less than
        the size of `u` along the applicable axis. Optional if `DimArray` with
        latitude/longitude coordinates used as input. Keyword-only argument.
    dy : `array`, optional
        The grid spacing(s) in the y-direction. If an array, there should be one item less than
        the size of `u` along the applicable axis. Optional if `DimArray` with
        latitude/longitude coordinates used as input. Keyword-only argument.
    x_dim : int, optional
        Axis number of x dimension. Defaults to -1 (implying [..., Y, X] order). Automatically
        parsed from input if using `DimArray`. Keyword-only argument.
    y_dim : int, optional
        Axis number of y dimension. Defaults to -2 (implying [..., Y, X] order). Automatically
        parsed from input if using `DimArray`. Keyword-only argument.

    Returns
    -------
    (..., M, N) `array`
        horizontal divergence

    See Also
    --------
    vorticity

    Notes
    -----
    This implements a numerical version of the typical vertical vorticity equation in
    Cartesian coordinates:

    .. math:: \nabla \cdot \vec{U} =
        \frac{\partial u}{\partial x} + \frac{\partial v}{\partial y}

    """
    if dx is None:
        xx = u.dimvalue(x_dim)
        yy = u.dimvalue(y_dim)
        xx,yy = np.meshgrid(xx, yy)
        dudx = first_derivative(u, x=xx, axis=x_dim)
        dvdy = first_derivative(v, x=yy, axis=y_dim)
    else:
        dudx = first_derivative(u, delta=dx, axis=x_dim)
        dvdy = first_derivative(v, delta=dy, axis=y_dim)
    return dudx + dvdy

def divergence_bak(u, v, x=None, y=None):
    '''
    Calculates the horizontal divergence using finite differencing. The data should be lon/lat projection.

    :param u: (*array*) U component array.
    :param v: (*array*) V component array.
    :param x: (*array*) X coordinate.
    :param y: (*array*) Y coordinate.

    :returns: Array of the horizontal divergence.
    '''
    ny = u.shape[-2]
    nx = u.shape[-1]
    if x is None:
        if isinstance(u, DimArray):
            x = u.dimvalue(-1)
        else:
            x = np.arange(nx)
    elif isinstance(x, (list, tuple)):
        x = np.array(x)

    if y is None:
        if isinstance(v, DimArray):
            y = u.dimvalue(-2)
        else:
            y = np.arange(ny)
    elif isinstance(y, (list, tuple)):
        y = np.array(y)

    r = MeteoMath.divergence(u.asarray(), v.asarray(), x.asarray(), y.asarray())
    if isinstance(u, DimArray):
        return DimArray(NDArray(r), u.dims, u.fill_value, u.proj)
    else:
        return NDArray(r)

def advection(scalar, u=None, v=None, w=None, dx=None, dy=None, dz=None, x_dim=-1,
        y_dim=-2, vertical_dim=-3):
    r"""
    Calculate the advection of a scalar field by the wind.

    Parameters
    ----------
    scalar : `array`
        Array (with N-dimensions) with the quantity to be advected. Use `DimArray` to
        have dimension ordering automatically determined, otherwise, use default
        [..., Z, Y, X] ordering or specify \*_dim keyword arguments.
    u, v, w : `array` or None
        N-dimensional arrays with units of velocity representing the flow, with a component of
        the wind in each dimension. For 1D advection, use 1 positional argument (with `dx` for
        grid spacing and `x_dim` to specify axis if not the default of -1) or use 1 applicable
        keyword argument (u, v, or w) for proper physical dimension (with corresponding `d\*`
        for grid spacing and `\*_dim` to specify axis). For 2D/horizontal advection, use 2
        positional arguments in order for u and v winds respectively (with `dx` and `dy` for
        grid spacings and `x_dim` and `y_dim` keyword arguments to specify axes), or specify u
        and v as keyword arguments (grid spacings and axes likewise). For 3D advection,
        likewise use 3 positional arguments in order for u, v, and w winds respectively or
        specify u, v, and w as keyword arguments (either way, with `dx`, `dy`, `dz` for grid
        spacings and `x_dim`, `y_dim`, and `vertical_dim` for axes).
    dx, dy, dz: `array` or None, optional
        Grid spacing in applicable dimension(s). If using arrays, each array should have one
        item less than the size of `scalar` along the applicable axis. If `scalar` is an
        `DimArray`, these are automatically determined from its coordinates, and are
        therefore optional. Required if `scalar` is a `array`. These are keyword-only
        arguments.
    x_dim, y_dim, vertical_dim: int or None, optional
        Axis number in applicable dimension(s). Defaults to -1, -2, and -3 respectively for
        (..., Z, Y, X) dimension ordering. If `scalar` is an `DimArray`, these are
        automatically determined from its coordinates. These are keyword-only arguments.

    Returns
    -------
    `array`
        An N-dimensional array containing the advection at all grid points.
    """
    return -sum(
        wind * first_derivative(scalar, axis=axis, delta=delta)
        for wind, delta, axis in (
            (u, dx, x_dim),
            (v, dy, y_dim),
            (w, dz, vertical_dim)
        )
        if wind is not None
    )

def absolute_vorticity(u, v, dx=None, dy=None, latitude=None, x_dim=-1, y_dim=-2):
    """Calculate the absolute vorticity of the horizontal wind.

    Parameters
    ----------
    u : (..., M, N) `array`
        x component of the wind
    v : (..., M, N) `array`
        y component of the wind
    dx : `pint.Quantity`, optional
        The grid spacing(s) in the x-direction. If an array, there should be one item less than
        the size of `u` along the applicable axis. Optional if `DimArray` with
        latitude/longitude coordinates used as input.
    dy : `pint.Quantity`, optional
        The grid spacing(s) in the y-direction. If an array, there should be one item less than
        the size of `u` along the applicable axis. Optional if `DimArray` with
        latitude/longitude coordinates used as input.
    latitude : `pint.Quantity`, optional
        Latitude of the wind data. Optional if `DimArray` with latitude/longitude
        coordinates used as input. Note that an argument without units is treated as
        dimensionless, which translates to radians.
    x_dim : int, optional
        Axis number of x dimension. Defaults to -1 (implying [..., Y, X] order). Automatically
        parsed from input if using `DimArray`.
    y_dim : int, optional
        Axis number of y dimension. Defaults to -2 (implying [..., Y, X] order). Automatically
        parsed from input if using `DimArray`.

    Returns
    -------
    (..., M, N) `array`
        absolute vorticity
    """
    f = coriolis_parameter(latitude)
    relative_vorticity = vorticity(u, v, dx=dx, dy=dy, x_dim=x_dim, y_dim=y_dim)
    return relative_vorticity + f

def potential_vorticity_barotropic(height, u, v, dx=None, dy=None, latitude=None,
        x_dim=-1, y_dim=-2):
    r"""Calculate the barotropic (Rossby) potential vorticity.

    .. math:: PV = \frac{f + \zeta}{H}
    This formula is based on equation 7.27 [Hobbs2006]_.

    Parameters
    ----------
    height : (..., M, N) `array`
        atmospheric height
    u : (..., M, N) `array`
        x component of the wind
    v : (..., M, N) `array`
        y component of the wind
    dx : `array`, optional
        The grid spacing(s) in the x-direction. If an array, there should be one item less than
        the size of `u` along the applicable axis. Optional if `DimArray` with
        latitude/longitude coordinates used as input.
    dy : `array`, optional
        The grid spacing(s) in the y-direction. If an array, there should be one item less than
        the size of `u` along the applicable axis. Optional if `DimArray` with
        latitude/longitude coordinates used as input.
    latitude : `array`, optional
        Latitude of the wind data. Optional if `DimArray` with latitude/longitude
        coordinates used as input. Note that an argument without units is treated as
        dimensionless, which translates to radians.
    x_dim : int, optional
        Axis number of x dimension. Defaults to -1 (implying [..., Y, X] order). Automatically
        parsed from input if using `DimArray`.
    y_dim : int, optional
        Axis number of y dimension. Defaults to -2 (implying [..., Y, X] order). Automatically
        parsed from input if using `DimArray`.

    Returns
    -------
    (..., M, N) `array`
        barotropic potential vorticity
    """
    avor = absolute_vorticity(u, v, dx, dy, latitude, x_dim=x_dim, y_dim=y_dim)
    return avor / height