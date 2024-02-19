from org.meteoinfo.geometry.graphic import Line2DGraphic
from org.meteoinfo.geometry.legend import PolylineBreak

from .. import plotutil


__all__ = ['Line2D']

class Line2D(Line2DGraphic):
    """
    A line - the line can have both a solid linestyle connecting all
    the vertices, and a marker at each vertex.  Additionally, the
    drawing of the solid line is influenced by the drawstyle, e.g., one
    can create "stepped" lines in various styles.
    """

    #__slots__ = ('set_data')

    def __init__(self, xdata, ydata, legend=None, cdata=None, curve=False, **kwargs):
        """
        Create a `.Line2D` instance with *x* and *y* data in sequences of
        *xdata*, *ydata*.
        """
        if legend is None:
            legend = plotutil.getlegendbreak('line', **kwargs)[0]

        self._x = xdata
        self._y = ydata
        self._cdata = cdata

        if cdata is None:
            super(Line2D, self).__init__(xdata._array, ydata._array, legend)
        else:
            super(Line2D, self).__init__(xdata._array, ydata._array, cdata._array, legend)

        if curve:
            self.setCurve(curve)

    def get_xdata(self):
        """
        Return the xdata.

        :return: (*array*) xdata.
        """
        return self._x

    def set_xdata(self, xdata):
        """
        Set the xdata.

        :param xdata: (*array*) The xdata.
        """
        self._x = xdata
        self.setXData(xdata._array)

    def get_ydata(self):
        """
        Return the ydata.

        :return: (*array*) ydata.
        """
        return self._y

    def set_ydata(self, ydata):
        """
        Set the ydata.

        :param ydata: (*array*) The ydata.
        """
        self._y = ydata
        self.setYData(ydata._array)

    def get_data(self):
        """
        Get x, y data.

        :return: x, y data.
        """
        return (self._x, self._y)

    def set_data(self, xdata, ydata):
        """
        Set x, y data.

        :param xdata: (*array*) X data.
        :param ydata: (*array*) Y data.
        """
        self._x = xdata
        self._y = ydata
        self.setData(xdata._array, ydata._array)

    def get_color(self):
        """
        Return the line color.

        :return: (*Color*) The line color.
        """
        return self.legend.getColor()

    def set_color(self, color):
        """
        Set the line color.

        :param color: (*color or str*) The line color.
        """
        color = plotutil.getcolor(color)
        self.legend.setColor(color)

    def get_curve(self):
        """
        Return curve line or not.

        :return: (*bool*) Curve line or not.
        """
        return self.isCurve()

    def set_curve(self, curve):
        """
        Set curve line or not.

        :param curve: (*bool*) Curve line or not.
        """
        self.setCurve(curve)
