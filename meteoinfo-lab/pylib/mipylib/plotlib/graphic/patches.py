from org.meteoinfo.geometry.graphic import Graphic, PolygonGraphic
from org.meteoinfo.geometry.shape import ShapeUtil, CircleShape, EllipseShape, \
    RectangleShape, ArcShape

from .. import plotutil
import mipylib.numeric as np
from artist import Artist

__all__ = ['Arc','Circle','Ellipse','Rectangle','Polygon','Wedge']

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
        Create an ellipse at center *xy* = (*x*, *y*) with given *width* and *height*.

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

class Arc(Graphic):
    """
    An Arc patch.
    """

    def __init__(self, xy, width, height, angle=0, theta1=0.0, theta2=360.0, **kwargs):
        """
        Create an arc at anchor point *xy* = (*x*, *y*) with given *width* and *height*.

        :param xy: (float, float) xy coordinates of anchor point.
        :param width: (float) Ellipse width.
        :param height: (float) Ellipse height.
        :param angle: (float) Ellipse angle. Default is `0`.
        :param theta1: (float) Starting angle of the arc in degrees. Default is `0.0`.
        :param theta2: (float) Ending angle of the arc in degrees. Default is `360`.
        :param closure: (str) Closure type ['open' | 'chord' | 'pie']. Default is `pie`.
        """
        self._center = xy
        self._width = width
        self._height = height
        self._angle = angle
        self._theta1 = theta1
        self._theta2 = theta2
        shape = ArcShape(xy[0], xy[1], width, height)
        shape.setAngle(angle)
        shape.setStartAngle(theta1)
        shape.setSweepAngle(theta2 - theta1)
        self._closure = kwargs.pop('closure', None)
        if self._closure is None:
            self._closure = 'pie'
        else:
            if self._closure == 'open':
                closure = 0
            elif self._closure == 'chord':
                closure = 1
            else:
                self._closure = 'pie'
                closure = 2
            shape.setClosure(closure)

        legend, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        super(Arc, self).__init__(shape, legend)

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

    @property
    def theta1(self):
        return self._theta1

    @property
    def theta2(self):
        return self._theta2

    @property
    def closure(self):
        return self._closure

class Wedge(Graphic):
    """
    Wedge shaped patch.

    A wedge centered at x, y center with radius r that sweeps theta1 to theta2 (in degrees). If width is
    given, then a partial wedge is drawn from inner radius r - width to outer radius r.
    """

    def __init__(self, center, r, theta1, theta2, **kwargs):
        """
        Create an arc at anchor point *xy* = (*x*, *y*) with given *width* and *height*.

        :param center: (float, float) xy coordinates of wedge center point.
        :param r: (float) Radius.
        :param theta1: (float) Starting angle of the arc in degrees.
        :param theta2: (float) Ending angle of the arc in degrees.
        :param closure: (str) Closure type ['open' | 'chord' | 'pie']. Default is `pie`.
        """
        self._center = center
        self._r = r
        self._theta1 = theta1
        self._theta2 = theta2
        shape = ArcShape(center[0], center[1], r * 2, r * 2)
        shape.setStartAngle(theta1)
        shape.setSweepAngle(theta2 - theta1)
        self._closure = kwargs.pop('closure', None)
        if self._closure is None:
            self._closure = 'pie'
        else:
            if self._closure == 'open':
                closure = 0
            elif self._closure == 'chord':
                closure = 1
            else:
                self._closure = 'pie'
                closure = 2
            shape.setClosure(closure)

        legend, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        super(Wedge, self).__init__(shape, legend)

    @property
    def center(self):
        return self._center

    @property
    def r(self):
        return self._r

    @property
    def theta1(self):
        return self._theta1

    @property
    def theta2(self):
        return self._theta2

    @property
    def closure(self):
        return self._closure

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

class Polygon(PolygonGraphic, Artist):
    """
    A general polygon patch.
    """

    def __init__(self, *args, **kwargs):
        """
        Create a polygon with *xy* point array.

        :param xy: (array_like) xy point array.
        :param closed: (bool) If *closed* is *True*, the polygon will be closed so the
            starting and ending points are the same.
        """
        Artist.__init__(self)

        n = len(args)
        if n == 1:
            xy = np.asarray(args[0])
            self._x = xy[:, 0]
            self._y = xy[:, 1]
        else:
            self._x = np.asarray(args[0])
            self._y = np.asarray(args[1])

        self._closed = kwargs.pop('closed', True)
        legend, is_unique = plotutil.getlegendbreak('polygon', **kwargs)
        PolygonGraphic.__init__(self, self._x._array, self._y._array, legend)

    @property
    def visible(self):
        """
        The artist is visible or not.
        """
        return self.isVisible()

    @visible.setter
    def visible(self, val):
        self.setVisible(val)
        self.stale = True

    @property
    def xdata(self):
        """
        Return the xdata.

        :return: (*array*) xdata.
        """
        return self._x

    @xdata.setter
    def xdata(self, xdata):
        """
        Set the xdata.

        :param xdata: (*array*) The xdata.
        """
        xdata = np.asarray(xdata)
        self._x = xdata
        self.setXData(xdata._array)
        self.stale = True

    @property
    def ydata(self):
        """
        Return the ydata.

        :return: (*array*) ydata.
        """
        return self._y

    @ydata.setter
    def ydata(self, ydata):
        """
        Set the ydata.

        :param ydata: (*array*) The ydata.
        """
        ydata = np.asarray(ydata)
        self._y = ydata
        self.setYData(ydata._array)
        self.stale = True

    @property
    def data(self):
        """
        Get x, y data.

        :return: x, y data.
        """
        return (self._x, self._y)

    @data.setter
    def data(self, *args):
        """
        Set x, y data.

        :param xdata: (*array*) X data.
        :param ydata: (*array*) Y data.
        """
        if len(args) == 1:
            xdata = args[0][0]
            ydata = args[0][1]
        else:
            xdata = args[0]
            ydata = args[1]

        xdata = np.asarray(xdata)
        ydata = np.asarray(ydata)
        self._x = xdata
        self._y = ydata
        self.setData(xdata._array, ydata._array)
        self.stale = True

    @property
    def color(self):
        """
        Return the line color.

        :return: (*Color*) The line color.
        """
        return self.legend.getColor()

    @color.setter
    def color(self, color):
        """
        Set the line color.

        :param color: (*color or str*) The line color.
        """
        color = plotutil.getcolor(color)
        self.legend.setColor(color)
        self.stale = True
