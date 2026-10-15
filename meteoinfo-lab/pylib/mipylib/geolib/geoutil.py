#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2017-11-28
# Purpose: MeteoInfo geoutil module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.geometry import GeometryUtil
from org.meteoinfo.geometry import Coordinate, Point, GeometryType
import mipylib.numeric as np

__all__ = [
    'makeshapes'
    ]

def makeshapes(x, y, shape_type=None, z=None, m=None):
    """
    Make shapes by x and y coordinates.
    
    :param x: (*array_like*) X coordinates.
    :param y: (*array_like*) Y coordinates.    
    :param shape_type: (*string*) Shape type [point | line | polygon].
    :param z: (*array_like*) Z coordinates.
    :param m: (*array_like*) M coordinates.
    
    :returns: Shapes
    """
    shapes = []   
    if isinstance(x, (int, float)):
        shape = PointShape()
        shape.setPoint(Coordinate(x, y))
        shapes.append(shape)    
    else:
        x = np.asarray(x)._array
        y = np.asarray(y)._array
        if not z is None:            
            if m is None:
                m = np.zeros(len(z))
            else:
                m = np.asarray(m)
            z = np.asarray(z)
        if isinstance(shape_type, basestring):
            try:
                shape_type = GeometryType.valueOf(shape_type)
            except:
                shape_type = GeometryType.POINT

        if shape_type.isZ():
            if z is None:
                z = np.zeros(len(x))
            if m is None:
                m = z.copy()
            z = z._array
            m = m._array

        if shape_type == GeometryType.POINT:
            shapes = GeometryUtil.createPointShapes(x, y)
        elif shape_type == GeometryType.POINT_Z:
            shapes = GeometryUtil.createPointShapes(x, y, z, m)
        elif shape_type == GeometryType.LINESTRING:
            shapes = GeometryUtil.createPolylineShapes(x, y)
        elif shape_type == GeometryType.POLYLINE_Z:
            shapes = GeometryUtil.createPolylineShapes(x, y, z, m)
        elif shape_type == GeometryType.POLYGON:
            shapes = GeometryUtil.createPolygonShapes(x, y)
        elif shape_type == GeometryType.POLYGON_Z:
            shapes = GeometryUtil.createPolygonShape(x, y, z, m)
    return shapes   
    