#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2017-11-28
# Purpose: MeteoInfo geoutil module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.shape import ShapeUtil, PointShape
from org.meteoinfo.global import PointD
import mipylib.numeric.minum as minum

__all__ = [
    'makeshapes'
    ]

def makeshapes(x, y, type=None, z=None, m=None):
    """
    Make shapes by x and y coordinates.
    
    :param x: (*array_like*) X coordinates.
    :param y: (*array_like*) Y coordinates.    
    :param type: (*string*) Shape type [point | line | polygon].
    :param z: (*array_like*) Z coordinates.
    :param m: (*array_like*) M coordinates.
    
    :returns: Shapes
    """
    shapes = []   
    if isinstance(x, (int, float)):
        shape = PointShape()
        shape.setPoint(PointD(x, y))
        shapes.append(shape)    
    else:
        x = minum.asarray(x).array
        y = minum.asarray(y).array
        if not z is None:            
            if m is None:
                m = minum.zeros(len(z)).array
            else:
                m = minum.asarray(m).array
            z = minum.asarray(z).array
        if type == 'point':
            if z is None:
                shapes = ShapeUtil.createPointShapes(x, y)
            else:
                shapes = ShapeUtil.createPointShapes(x, y, z, m)
        elif type == 'line':
            if z is None:
                shapes = ShapeUtil.createPolylineShapes(x, y)
            else:
                shapes = ShapeUtil.createPolylineShapes(x, y, z, m)
        elif type == 'polygon':
            if z is None:
                shapes = ShapeUtil.createPolygonShapes(x, y)
            else:
                shapes = ShapeUtil.createPolygonShape(x, y, z, m)
    return shapes   
    