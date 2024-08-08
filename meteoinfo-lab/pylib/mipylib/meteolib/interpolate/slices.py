"""
Tools for interpolating to a vertical slice/cross section through data.
"""

from org.meteoinfo.math.interpolate import InterpUtil, InterpolationMethod

from mipylib.numeric.core import NDArray


__all__ = ['cross_section']


def cross_section(data, start, end, steps=100, interp_type='linear'):
    r"""Obtain an interpolated cross-sectional slice through gridded data.

    This function takes a vertical cross-sectional slice along a geodesic through the given data
    on a regular grid, which is given as an `DimArray` so that we can utilize its coordinate and
    projection metadata.

    Parameters
    ----------
    data: `DimArray`
        Three- (or higher) dimensional field(s) to interpolate. The DimArray must include both an x and
        y coordinate dimension.
    start: (2, ) array-like
        A latitude-longitude pair designating the start point of the cross section (units are
        degrees north and degrees east).
    end: (2, ) array-like
        A latitude-longitude pair designating the end point of the cross section (units are
        degrees north and degrees east).
    steps: int, optional
        The number of points along the geodesic between the start and the end point
        (including the end points) to use in the cross section. Defaults to 100.
    interp_type: str, optional
        The interpolation method, either 'linear' or 'nearest'. Defaults to 'linear'.

    Returns
    -------
    List of `NDArray`
        The interpolated cross section, new x coordinates and new y coordinates
    """
    x = data.dimvalue(-1)
    y = data.dimvalue(-2)
    interp_type = InterpolationMethod.valueOf(interp_type.upper())
    xyslice = start + end
    if data.ndim == 2:
        rs = InterpUtil.sliceXY(x._array, y._array, data._array, xyslice, steps, interp_type)
    else:
        z = data.dimvalue(-3)
        rs = InterpUtil.sliceXY(x._array, y._array, z._array, data._array, xyslice, steps, interp_type)

    return NDArray(rs[0]), NDArray(rs[1]), NDArray(rs[2])
