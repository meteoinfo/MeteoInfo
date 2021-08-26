from org.meteoinfo.geometry.graphic import Graphic
from org.meteoinfo.geometry.shape import ShapeUtil, CircleShape, EllipseShape, \
    RectangleShape

from . import plotutil
import mipylib.numeric as np

__all__ = ['Circle','Ellipse','Rectangle','Polygon']

class Circle(Graphic):
    """
    A circle patch.
    """

    def __init__(self, xy, radius=5, **kwargs):
        """
        Create a true circle at center *xy* = (*x*, *y*) with given *radius*.

        :param xy: (float, float) xy coordinates of circle centre.
        :param radius: (float) Circle radius.
        """
        self._center = xy
        self._radius = radius
        shape = CircleShape(xy[0], xy[1], radius)
        legend, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        super(Circle, self).__init__(shape, legend)

    @property
    def center(self):
        return self._center

    @property
    def radius(self):
        return self._radius

class Ellipse(Graphic):
    """
    A ellipse patch.
    """

    def __init__(self, xy, width, height, angle=0, **kwargs):
        """
        Create a ellipse at center *xy* = (*x*, *y*) with given *width* and *height*.

        :param xy: (float, float) xy coordinates of ellipse centre.
        :param width: (float) Ellipse width.
        :param height: (float) Ellipse height.
        :param angle: (float) Ellipse angle. Default is 0.
        """
        self._center = xy
        self._width = width
        self._height = height
        self._angle = angle
        shape = EllipseShape(xy[0], xy[1], width, height)
        shape.setAngle(angle)
        legend, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        super(Ellipse, self).__init__(shape, legend)

    @property
    def center(self):
        return self._center

    @property
    def width(self):
        return self._width

    @property
    def height(self):
        return self._height

    @property
    def angle(self):
        return self._angle

class Rectangle(Graphic):
    """
    A rectangle patch.
    """

    def __init__(self, xy, width, height, angle=0, **kwargs):
        """
        Create a rectangle at anchor point *xy* = (*x*, *y*) with given *width* and *height*.

        :param xy: (float, float) xy coordinates of anchor point.
        :param width: (float) Rectangle width.
        :param height: (float) Rectangle height.
        :param angle: (float) Rectangle angle. Default is 0.
        """
        self._center = xy
        self._width = width
        self._height = height
        self._angle = angle
        shape = RectangleShape(xy[0], xy[1], width, height)
        shape.setAngle(angle)
        legend, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        super(Rectangle, self).__init__(shape, legend)

    @property
    def center(self):
        return self._center

    @property
    def width(self):
        return self._width

    @property
    def height(self):
        return self._height

    @property
    def angle(self):
        return self._angle

class Polygon(Graphic):
    """
    A general polygon patch.
    """

    def __init__(self, xy, closed=True, **kwargs):
        """
        Create a polygon with *xy* point array.

        :param xy: (array_like) xy point array.
        :param closed: (bool) If *closed* is *True*, the polygon will be closed so the
            starting and ending points are the same.
        """
        if isinstance(xy, (list, tuple)):
            xy = np.array(xy)

        self._xy = xy
        self._closed = closed
        shape = ShapeUtil.createPolygonShape(xy._array)
        legend, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        super(Polygon, self).__init__(shape, legend)

    @property
    def xy(self):
        return self._xy