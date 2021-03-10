#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2015-12-23
# Purpose: MeteoInfoLab meteo module
# Note: Jython, some functions code revised from MetPy
#-----------------------------------------------------

from org.meteoinfo.math.meteo import MeteoMath

import mipylib.numeric as np
from mipylib.numeric.core import NDArray, DimArray

import constants as constants
from .calc.thermo import saturation_vapor_pressure, saturation_mixing_ratio

__all__ = [
    'cumsimp','dewpoint','dewpoint2rh','dewpoint_from_relative_humidity','dewpoint_rh','dry_lapse','ds2uv',
    'flowfun','h2p',
    'moist_lapse','p2h','qair2rh','rh2dewpoint',
    'sigma_to_pressure','tc2tf',
    'tf2tc','uv2ds','pressure_to_height_std',
    'height_to_pressure_std','eof','vapor_pressure','varimax'
    ]

def uv2ds(u, v):
    '''
    Calculate wind direction and wind speed from U/V.
    
    :param u: (*array_like*) U component of wind field.
    :param v: (*array_like*) V component of wind field.
    
    :returns: Wind direction and wind speed.
    '''
    if isinstance(u, NDArray):
        r = MeteoMath.uv2ds(u.asarray(), v.asarray())
        d = NDArray(r[0])
        s = NDArray(r[1])
        if isinstance(u, DimArray) and isinstance(v, DimArray):
            d = DimArray(d, u.dims, u.fill_value, u.proj)
            s = DimArray(s, u.dims, u.fill_value, u.proj)
        return d, s
    else:
        r = MeteoMath.uv2ds(u, v)
        return r[0], r[1]
        
def ds2uv(d, s):
    '''
    Calculate U/V from wind direction and wind speed.
    
    :param d: (*array_like*) Wind direction.
    :param s: (*array_like*) Wind speed.
    
    :returns: Wind U/V.
    '''
    if isinstance(d, NDArray):
        r = MeteoMath.ds2uv(d.asarray(), s.asarray())
        u = NDArray(r[0])
        v = NDArray(r[1])
        if isinstance(d, DimArray) and isinstance(s, DimArray):
            u = DimArray(u, d.dims, d.fill_value, d.proj)
            v = DimArray(v, d.dims, d.fill_value, d.proj)
        return u, v
    else:
        r = MeteoMath.ds2uv(d, s)
        return r[0], r[1]

def p2h(press):
    """
    Pressure to height
    
    :param press: (*float*) Pressure - hPa.
    
    :returns: (*float*) Height - meter.
    """
    if isinstance(press, NDArray):
        r = NDArray(MeteoMath.press2Height(press.asarray()))
        if isinstance(press, DimArray):
            r = DimArray(r, press.dims, press.fill_value, press.proj)
        return r
    else:
        return MeteoMath.press2Height(press)
        
def pressure_to_height_std(press):
    """
    Convert pressure data to heights using the U.S. standard atmosphere.
    
    :param press: (*float*) Pressure - hPa.
    
    :returns: (*float*) Height - meter.
    """
    t0 = 288.
    gamma = 6.5
    p0 = 1013.25
    h = (t0 / gamma) * (1 - (press / p0)**(constants.Rd * gamma / constants.g)) * 1000
    return h
        
def h2p(height):
    """
    Height to pressure
    
    :param height: (*float*) Height - meter.
    
    :returns: (*float*) Pressure - hPa.
    """
    if isinstance(height, NDArray):
        r = NDArray(MeteoMath.height2Press(height.asarray()))
        if isinstance(height, DimArray):
            r = DimArray(r, height.dims, height.fill_value, height.proj)
        return r
    else:
        return MeteoMath.height2Press(height)
        
def height_to_pressure_std(height):
    """
    Convert height data to pressures using the U.S. standard atmosphere.
    
    :param height: (*float*) Height - meter.
    
    :returns: (*float*) Height - meter.
    """
    t0 = 288.
    gamma = 6.5
    p0 = 1013.25
    height = height * 0.001
    p = p0 * (1 - (gamma / t0) * height) ** (constants.g / (constants.Rd * gamma))
    return p
    
def sigma_to_pressure(sigma, psfc, ptop):
    r"""Calculate pressure from sigma values.
    Parameters
    ----------
    sigma : ndarray
        The sigma levels to be converted to pressure levels.
    psfc : ndarray
        The surface pressure value.
    ptop : ndarray
        The pressure value at the top of the model domain.
    Returns
    -------
    ndarray
        The pressure values at the given sigma levels.
    Notes
    -----
    Sigma definition adapted from [Philips1957]_.
    .. math:: p = \sigma * (p_{sfc} - p_{top}) + p_{top}
    * :math:`p` is pressure at a given `\sigma` level
    * :math:`\sigma` is non-dimensional, scaled pressure
    * :math:`p_{sfc}` is pressure at the surface or model floor
    * :math:`p_{top}` is pressure at the top of the model domain
    """
    if np.any(sigma < 0) or np.any(sigma > 1):
        raise ValueError('Sigma values should be bounded by 0 and 1')

    if psfc.min() < 0 or ptop.min() < 0:
        raise ValueError('Pressure input should be non-negative')

    return sigma * (psfc - ptop) + ptop
        
def tf2tc(tf):
    """
    Fahrenheit temperature to Celsius temperature
        
    tf: DimArray or NDArray or number 
        Fahrenheit temperature - degree f   
        
    return: DimArray or NDArray or number
        Celsius temperature - degree c
    """    
    if isinstance(tf, NDArray):
        r = NDArray(MeteoMath.tf2tc(tf.asarray()))
        if isinstance(tf, DimArray):
            r = DimArray(r, tf.dims, tf.fill_value, tf.proj)
        return r
    else:
        return MeteoMath.tf2tc(tf)
        
def tc2tf(tc):
    """
    Celsius temperature to Fahrenheit temperature
        
    tc: DimArray or NDArray or number 
        Celsius temperature - degree c    
        
    return: DimArray or NDArray or number
        Fahrenheit temperature - degree f
    """    
    if isinstance(tc, NDArray):
        r = NDArray(MeteoMath.tc2tf(tc.asarray()))
        if isinstance(tc, DimArray):
            r = DimArray(r, tc.dims, tc.fill_value, tc.proj)
        return r
    else:
        return MeteoMath.tc2tf(tc)

def qair2rh(qair, temp, press=1013.25):
    """
    Specific humidity to relative humidity
        
    qair: DimArray or NDArray or number 
        Specific humidity - dimensionless (e.g. kg/kg) ratio of water mass / total air mass
    temp: DimArray or NDArray or number
        Temperature - degree c
    press: DimArray or NDArray or number
        Pressure - hPa (mb)
    
    return: DimArray or NDArray or number
        Relative humidity - %
    """    
    if isinstance(press, NDArray) or isinstance(press, DimArray):
        p = press.asarray()
    else:
        p = press
    if isinstance(qair, NDArray):
        r = NDArray(MeteoMath.qair2rh(qair.asarray(), temp.asarray(), p))
        if isinstance(qair, DimArray):
            r = DimArray(r, qair.dims, qair.fill_value, qair.proj)
        return r
    else:
        return MeteoMath.qair2rh(qair, temp, press)
        
def dewpoint2rh(dewpoint, temp):    
    """
    Dew point to relative humidity
        
    dewpoint: DimArray or NDArray or number 
        Dew point - degree c
    temp: DimArray or NDArray or number
        Temperature - degree c
        
    return: DimArray or NDArray or number
        Relative humidity - %
    """    
    if isinstance(dewpoint, NDArray):
        r = NDArray(MeteoMath.dewpoint2rh(dewpoint.asarray(), temp.asarray()))
        if isinstance(dewpoint, DimArray):
            r = DimArray(r, dewpoint.dims, dewpoint.fill_value, dewpoint.proj)
        return r
    else:
        return MeteoMath.dewpoint2rh(temp, dewpoint)  

def rh2dewpoint(rh, temp):    
    """
    Calculate dewpoint from relative humidity and temperature
        
    rh: DimArray or NDArray or number 
        Relative humidity - %
    temp: DimArray or NDArray or number
        Temperature - degree c
        
    return: DimArray or NDArray or number
        Relative humidity - %
    """    
    if isinstance(rh, NDArray):
        r = NDArray(MeteoMath.rh2dewpoint(rh.asarray(), temp.asarray()))
        if isinstance(rh, DimArray):
            r = DimArray(r, rh.dims, rh.fill_value, rh.proj)
        return r
    else:
        return MeteoMath.rh2dewpoint(rh, temp)     
        
def dewpoint(e):
    r"""Calculate the ambient dewpoint given the vapor pressure.
    Parameters
    ----------
    e : `pint.Quantity`
        Water vapor partial pressure
    Returns
    -------
    `pint.Quantity`
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
    val = np.log(e / constants.sat_pressure_0c)
    return 243.5 * val / (17.67 - val)

def dewpoint_from_relative_humidity(temperature, rh):
    r"""Calculate the ambient dewpoint given air temperature and relative humidity.
    Parameters
    ----------
    temperature : `float`
        Air temperature (celsius)
    rh : `float`
        Relative humidity expressed as a ratio in the range 0 < rh <= 1
    Returns
    -------
    `float`
        The dew point temperature (celsius)
    See Also
    --------
    dewpoint, saturation_vapor_pressure
    """
    #if np.any(rh > 1.2):
    #    warnings.warn('Relative humidity >120%, ensure proper units.')
    return dewpoint(rh * saturation_vapor_pressure(temperature))

def dewpoint_rh(temperature, rh):
    r"""Calculate the ambient dewpoint given air temperature and relative humidity.

    deprecated - replaced by dewpoint_from_relative_humidity.

    Parameters
    ----------
    temperature : `float`
        Air temperature (celsius)
    rh : `float`
        Relative humidity expressed as a ratio in the range 0 < rh <= 1
    Returns
    -------
    `float`
        The dew point temperature (celsius)
    See Also
    --------
    dewpoint, saturation_vapor_pressure
    """
    #if np.any(rh > 1.2):
    #    warnings.warn('Relative humidity >120%, ensure proper units.')
    return dewpoint(rh * saturation_vapor_pressure(temperature))

def dry_lapse(pressure, temperature):
    """
    Calculate the temperature at a level assuming only dry processes
    operating from the starting point.
    This function lifts a parcel starting at `temperature`, conserving
    potential temperature. The starting pressure should be the first item in
    the `pressure` array.
    Parameters
    ----------
    pressure : array_like
        The atmospheric pressure level(s) of interest
    temperature : array_like
        The starting temperature
    Returns
    -------
    array_like
       The resulting parcel temperature at levels given by `pressure`
    See Also
    --------
    moist_lapse : Calculate parcel temperature assuming liquid saturation
                  processes
    parcel_profile : Calculate complete parcel profile
    potential_temperature
    """

    return temperature * (pressure / pressure[0])**constants.kappa
    
def moist_lapse(pressure, temperature):
    """
    Calculate the temperature at a level assuming liquid saturation processes
    operating from the starting point.
    This function lifts a parcel starting at `temperature`. The starting
    pressure should be the first item in the `pressure` array. Essentially,
    this function is calculating moist pseudo-adiabats.
    Parameters
    ----------
    pressure : array_like
        The atmospheric pressure level(s) of interest
    temperature : array_like
        The starting temperature
    Returns
    -------
    array_like
       The temperature corresponding to the the starting temperature and
       pressure levels.
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
    This equation comes from [1]_.
    References
    ----------
    .. [1] Bakhshaii, A. and R. Stull, 2013: Saturated Pseudoadiabats--A
           Noniterative Approximation. J. Appl. Meteor. Clim., 52, 5-15.
    """

    def dt(t, p):
        rs = saturation_mixing_ratio(p, t)
        frac = ((constants.Rd * t + constants.Lv * rs) /
                (constants.Cp_d + (constants.Lv * constants.Lv * rs * constants.epsilon / (constants.Rd * t * t)))).to('kelvin')
        return frac / p
    return dt

def vapor_pressure(pressure, mixing):
    r"""Calculate water vapor (partial) pressure.
    Given total `pressure` and water vapor `mixing` ratio, calculates the
    partial pressure of water vapor.
    Parameters
    ----------
    pressure : `float`
        total atmospheric pressure (hPa)
    mixing : `float`
        dimensionless mass mixing ratio
    Returns
    -------
    `float`
        The ambient water vapor (partial) pressure in the same units as
        `pressure`.
    Notes
    -----
    This function is a straightforward implementation of the equation given in many places,
    such as [Hobbs1977]_ pg.71:
    .. math:: e = p \frac{r}{r + \epsilon}
    See Also
    --------
    saturation_vapor_pressure, dewpoint
    """
    return pressure * mixing / (constants.epsilon + mixing)

def cumsimp(y):
    """
    Simpson-rule column-wise cumulative summation.
    Numerical approximation of a function F(x) such that
    Y(X) = dF/dX.  Each column of the input matrix Y represents
    the value of the integrand  Y(X)  at equally spaced points
    X = 0,1,...size(Y,1).
    The output is a matrix  F of the same size as Y.
    The first row of F is equal to zero and each following row
    is the approximation of the integral of each column of matrix
    Y up to the givem row.
    CUMSIMP assumes continuity of each column of the function Y(X)
    and uses Simpson rule summation.
    Similar to the command F = CUMSUM(Y), exept for zero first
    row and more accurate summation (under the assumption of
    continuous integrand Y(X)).

    Transferred from MATLAT code by Kirill K. Pankratov, March 7, 1994.

    :param y: (*array*) Input 2-D array.

    :returns: (*array*) Summation result.
    """
    # 3-points interpolation coefficients to midpoints.
    # Second-order polynomial (parabolic) interpolation coefficients
    # from  Xbasis = [0 1 2]  to  Xint = [.5 1.5]
    c1 = 3./8; c2 = 6./8; c3 = -1./8

    # Determine the size of the input and make column if vector
    ist = 0         # If to be transposed
    lv = y.shape[0]
    if lv == 1:
        ist = 1
        y = y.T
        lv = len(y)
    f = np.zeros(y.shape)

    # If only 2 elements in columns - simple sum divided by 2
    if lv == 2:
        f[1,:] = (y[0,:] + y[1]) / 2
        if ist:
            f = f.T   # Transpose output if necessary
        return f

    # If more than two elements in columns - Simpson summation
    num = np.arange(0, lv-2)
    # Interpolate values of Y to all midpoints
    f[num+1,:] = c1*y[num,:]+c2*y[num+1,:]+c3*y[num+2,:]
    f[num+2,:] = f[num+2,:]+c3*y[num,:]+c2*y[num+1,:]+c1*y[num+2,:]
    f[1,:] = f[1,:]*2; f[lv-1,:] = f[lv-1,:]*2
    # Now Simpson (1,4,1) rule
    f[1:lv,:] = 2*f[1:lv,:]+y[0:lv-1,:]+y[1:lv,:]
    f = np.cumsum(f, axis=0) / 6  # Cumulative sum, 6 - denom. from the Simpson rule

    if ist:
        f = f.T     # Transpose output if necessary

    return f

def flowfun(u, v):
    """
    Computes the potential PHI and the streamfunction PSI
     of a 2-dimensional flow defined by the matrices of velocity
     components U and V, so that

           d(PHI)    d(PSI)          d(PHI)    d(PSI)
      u =  -----  -  ----- ,    v =  -----  +  -----
            dx        dy              dx        dy

     For a potential (irrotational) flow  PSI = 0, and the laplacian
     of PSI is equal to the divergence of the velocity field.
     A non-divergent flow can be described by the streamfunction
     alone, and the laplacian of the streamfunction is equal to
     vorticity (curl) of the velocity field.
     The stepsizes dx and dy are assumed to equal unity.
    [PHI,PSI] = FLOWFUN(U,V), or in a complex form
    [PHI,PSI] = FLOWFUN(U+iV)
     returns matrices PHI and PSI of the same sizes as U and V,
     containing potential and streamfunction given by velocity
     components U, V.
     Because these potentials are defined up to the integration
     constant their absolute values are such that
     PHI(1,1) = PSI(1,1) = 0.

    Uses command CUMSIMP (Simpson rule summation).

    transferred from MATLAB code by Kirill K. Pankratov, March 7, 1994.

    :param u: (*array*) U component of the wind. 2-D array.
    :param v: (*array*) V component of the wind, 2-D array.

    :returns: (*array*) Stream function and potential velocity.
    """
    ly, lx = u.shape  # Size of the velocity matrices

    # Now the main computations .........................................
    # Integrate velocity fields to get potential and streamfunction
    # Use Simpson rule summation (function CUMSIMP)

    # Compute potential PHI (potential, non-rotating part)
    cx = cumsimp(u[0,:][np.newaxis,:])  # Compute x-integration constant
    cy = cumsimp(v[:,0][:,np.newaxis])  # Compute y-integration constant
    phi = cumsimp(v) + np.tile(cx, [ly,1])
    phi = (phi+cumsimp(u.T).T + np.tile(cy, [1,lx]))/2

    # Compute streamfunction PSI (solenoidal part)
    cx = cumsimp(v[0,:][np.newaxis,:])  # Compute x-integration constant
    cy = cumsimp(u[:,0][:,np.newaxis])  # Compute y-integration constant
    psi = -cumsimp(u) + np.tile(cx, [ly,1])
    psi = (psi+cumsimp(v.T).T - np.tile(cy, [1,lx]))/2

    return psi, phi
    
def eof(x, svd=False, transform=False):
    '''
    Empirical Orthogonal Function (EOF) analysis to finds both time series and spatial patterns.
    
    :param x: (*array_like*) Input 2-D array with space-time field.
    :param svd: (*boolean*) Using SVD or eigen method.
    :param transform: (*boolean*) Do space-time transform or not. This transform will speed up
        the computation if the space location number is much more than time stamps. Only valid
        when ``svd=False``.
        
    :returns: (EOF, E, PC) EOF: eigen vector 2-D array; E: eigen values 1-D array;
        PC: Principle component 2-D array.
    '''
    has_nan = False
    if x.contains_nan():       #Has NaN value
        valid_idx = np.where(x[:,0]!=np.nan)[0]
        xx = x[valid_idx,:]
        has_nan = True
    else:
        xx = x
        
    m, n = xx.shape    
    if svd:
        U, S, V = np.linalg.svd(xx)
        EOF = U
        C = np.zeros((m, n))
        for i in range(len(S)):
            C[i,i] = S[i]
        PC = np.dot(C, V)
        E = S**2 / n
    else:
        if transform:        
            C = np.dot(xx.T, xx)
            E1, EOF1 = np.linalg.eig(C)
            EOF1 = EOF1[:,::-1]
            E = E1[::-1]
            EOFa = np.dot(xx, EOF1)
            EOF = np.zeros((m,n))
            for i in range(n):
                EOF[:,i] = EOFa[:,i]/np.sqrt(abs(E[i]))
            PC = np.dot(EOF.T, xx)
        else:
            C = np.dot(xx, xx.T) / n
            E, EOF = np.linalg.eig(C)
            PC = np.dot(EOF.T, xx)
            EOF = EOF[:,::-1]
            PC = PC[::-1,:]
            E = E[::-1]
    
    if has_nan:
        _EOF = np.ones(x.shape) * np.nan
        _PC = np.ones(x.shape) * np.nan
        _EOF[valid_idx,:] = -EOF
        _PC[valid_idx,:] = -PC
        return _EOF, E, _PC
    else:
        return EOF, E, PC
    
def varimax(x, normalize=False, tol=1e-10, it_max=1000):
    '''
    Rotate EOFs according to varimax algorithm
    
    :param x: (*array_like*) Input 2-D array.
    :param normalize: (*boolean*) Determines whether or not to normalize the rows or columns
        of the loadings before performing the rotation.
    :param tol: (*float*) Tolerance.
    :param it_max: (*int*) Specifies the maximum number of iterations to do.
    
    :returns: Rotated EOFs and rotate matrix.
    '''
    p, nc = x.shape
    TT = np.eye(nc)
    d = 0
    for i in range(it_max):
        z = np.dot(x, TT)
        B = np.dot(x.T, (z**3 - np.dot(z, np.diag(np.squeeze(np.dot(np.ones((1,p)), (z**2))))) / p))
        U, S, Vh = np.linalg.svd(B)
        TT = np.dot(U, Vh)        
        d2 = d;
        d = np.sum(S)
        # End if exceeded tolerance.
        if d < d2 * (1 + tol):
            break
            
    # Final matrix.
    r = np.dot(x, TT)
    return r, TT