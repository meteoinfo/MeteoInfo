from org.meteoinfo.chart.graphic import LineGraphic
from org.meteoinfo.geometry.legend import PolylineBreak

from .. import plotutil
from artist import Artist
import mipylib.numeric as np


__all__ = ['Line']

class Line(LineGraphic, Artist):
    """
    A line - the line can have both a solid linestyle connecting all
    the vertices, and a marker at each vertex.  Additionally, the
    drawing of the solid line is influenced by the drawstyle, e.g., one
    can create "stepped" lines in various styles.
    """

    #__slots__ = ('set_data')

    def __init__(self, xdata, ydata, zdata=None, legend=None,
                 cdata=None, curve=False, **kwargs):
        """
        Create a `.Line` instance with *x* and *y* data in sequences of
        *xdata*, *ydata*, *zdata*
        """
        Artist.__init__(self)

        if legend is None:
            legend = plotutil.getlegendbreak('line', **kwargs)[0]

        self._x = np.asarray(xdata)
        self._y = np.asarray(ydata)
        self._z = None if zdata is None else np.asarray(zdata)
        self._cdata = np.asarray(cdata)

        if zdata is None:
            if cdata is None:
                LineGraphic.__init__(self, self._x._array, self._y._array, legend)
            else:
                LineGraphic.__init__(self, self._x._array, self._y._array, self._cdata._array, legend)
        else:
            if cdata is None:
                LineGraphic.__init__(self, self._x._array, self._y._array, self._z._array, legend)
            else:
                LineGraphic.__init__(self, self._x._array, self._y._array, self._z._array, self._cdata._array, legend)

        if curve:
            self.setCurve(curve)

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
    def zdata(self):
        """
        Return the zdata.

        :return: (*array*) zdata.
        """
        return self._z

    @zdata.setter
    def zdata(self, zdata):
        """
        Set the zdata.

        :param zdata: (*array*) The zdata.
        """
        self._z = np.asarray(zdata)
        self.setYData(self._z._array)
        self.stale = True

    @property
    def cdata(self):
        """
        Return the cdata.

        :return: (*array*) The cdata.
        """
        return self._cdata

    @property
    def data(self):
        """
        Get x, y, z data.

        :return: x, y, z data.
        """
        return (self._x, self._y, self._z)

    @data.setter
    def data(self, *args):
        """
        Set x, y, z data.

        :param xdata: (*array*) X data.
        :param ydata: (*array*) Y data.
        :param zdata: (*array*) Z data.
        """
        zdata = None
        if len(args) == 1:
            arr = args[0]
            xdata = arr[0]
            ydata = arr[1]
            if len(arr) > 2:
                zdata = arr[2]
        else:
            xdata = args[0]
            ydata = args[1]
            if len(args) > 2:
                zdata = args[2]

        self._x = np.asarray(xdata)
        self._y = np.asarray(ydata)
        if zdata is None:
            self.setData(self._x._array, self._y._array, zdata)
        else:
            self._z = np.asarray(zdata)
            self.setData(self._x._array, self._y._array, self._z._array)

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

    @property
    def curve(self):
        """
        Return curve line or not.

        :return: (*bool*) Curve line or not.
        """
        return self.isCurve()

    @curve.setter
    def curve(self, curve):
        """
        Set curve line or not.

        :param curve: (*bool*) Curve line or not.
        """
        self.setCurve(curve)
        self.stale = True
