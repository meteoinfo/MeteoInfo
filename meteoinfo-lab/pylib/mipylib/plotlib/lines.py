from org.meteoinfo.geometry.graphic import Graphic
from org.meteoinfo.geometry.shape import ShapeUtil

from . import plotutil
import mipylib.numeric as np

__all__ = ['Line2D']

class Line2D(Graphic):
    """
    A line - the line can have both a solid linestyle connecting all the vertices, and a marker
    at each vertex.
    """

    def __init__(self, xdata, ydata, **kwargs):
        """
        Create a `.Line2D` instance with *x* and *y* data in sequences of
        *xdata*, *ydata*.

        :param xdata: (*array_like*) X data of the line.
        :param ydata: (*array_like*) Y data of the line.
        """
        xdata = np.asarray(xdata)
        ydata = np.asarray(ydata)

        self._xdata = xdata
        self._ydata = ydata
        shape = ShapeUtil.createPolylineShape(xdata._array, ydata._array)
        legend, isunique = plotutil.getlegendbreak('line', **kwargs)
        super(Line2D, self).__init__(shape, legend)

    @property
    def xdata(self):
        return self._xdata

    @property
    def ydata(self):
        return self._ydata