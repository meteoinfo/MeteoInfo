from org.meteoinfo.geometry.graphic import GraphicCollection, Point2DGraphicCollection, \
    Line2DGraphicCollection
from java.awt import Font

from .. import plotutil
from ... import miutil
from artist import Artist
import mipylib.numeric as np

__all__ = ['Point2DCollection','LineCollection']


class Collection(Artist):

    def __init__(self):
        """
        Class init.

        :param graphics: (*GraphicCollection*) Graphics
        """
        Artist.__init__(self)

    def addlabels(self, **kwargs):
        """
        Add labels

        :param fontname: (*string*) Font name. Default is ``Arial``.
        :param fontsize: (*string*) Font size. Default is ``14``.
        :param bold: (*boolean*) Font bold or not. Default is ``False``.
        :param color: (*color*) Label color. Default is ``None`` with black color.
        :param xoffset: (*int*) X coordinate offset. Default is ``0``.
        :param yoffset: (*int*) Y coordinate offset. Default is ``0``.
        :param avoidcoll: (*boolean*) Avoid labels collision or not. Default is ``True``.
        :param decimals: (*int*) Number of decimals of labels.
        """
        labelset = self.getLabelSet()
        fontname = kwargs.pop('fontname', 'Arial')
        fontsize = kwargs.pop('fontsize', 14)
        bold = kwargs.pop('bold', False)
        if bold:
            font = Font(fontname, Font.BOLD, fontsize)
        else:
            font = Font(fontname, Font.PLAIN, fontsize)
        labelset.setLabelFont(font)
        color = kwargs.pop('color', None)
        if not color is None:
            color = miutil.getcolor(color)
            labelset.setLabelColor(color)
        xoffset = kwargs.pop('xoffset', 0)
        labelset.setXOffset(xoffset)
        yoffset = kwargs.pop('yoffset', 0)
        labelset.setYOffset(yoffset)
        avoidcoll = kwargs.pop('avoidcoll', True)
        labelset.setAvoidCollision(avoidcoll)
        decimals = kwargs.pop('decimals', None)
        if not decimals is None:
            labelset.setAutoDecimal(False)
            labelset.setDecimalDigits(decimals)
        self.addLabels()


class Point2DCollection(Collection, Point2DGraphicCollection):

    def __init__(self, xdata, ydata, cdata=None, legend=None, **kwargs):
        """
        Class init

        :param xdata: (*Array*) X data array.
        :param ydata: (*Array*) Y data array.
        :param cdata: (*Array*) Color data array.
        :param legend: (*Legend*) Point legend.
        """
        Collection.__init__(self)

        if legend is None:
            legend = plotutil.getlegendbreak('point', **kwargs)[0]

        self._x = np.asarray(xdata)
        self._y = np.asarray(ydata)
        self._cdata = np.asarray(cdata)

        if cdata is None:
            Point2DGraphicCollection.__init__(self, self._x._array, self._y._array, legend)
        else:
            Point2DGraphicCollection.__init__(self, self._x._array, self._y._array, self._cdata._array, legend)

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

        self._x = xdata
        self._y = ydata
        self.setData(xdata._array, ydata._array)
        self.stale = True


class LineCollection(Collection, Line2DGraphicCollection):

    def __init__(self, segments, legend=None, **kwargs):
        """
        Class init

        Parameters
        ----------
        segments : list of Array
                A sequence (*line0*, *line1*, *line2*) of lines, where each line is a list
            of points::

                lineN = [(x0, y0), (x1, y1), ... (xm, ym)]

            or the equivalent Mx2 numpy array with two columns. Each line
            can have a different number of segments.
        legend : Legend
            Point legend.
        xydata : List array
             X and Y 2D coordinates data array for lines.
        array : Array
            Array data for line colors.
        cdata : Array
            Color data array to plot multiple color lines.
        """
        Collection.__init__(self)

        if segments is None:
            xydata = kwargs.pop('xydata', None)
            if xydata is None:
                raise ValueError('Segments data is None!')
            else:
                if len(xydata) == 1:
                    ydata = xydata[0]
                    xdata = None
                else:
                    xdata = xydata[0]
                    ydata = xydata[1]

                nseg, nline = ydata.shape
                if xdata is None:
                    xdata = np.arange(nseg)

                if xdata.ndim == 1:
                    segments = [np.column_stack([xdata, ydata[:,i]]) for i in range(nline)]
                else:
                    segments = [np.column_stack([xdata[:,i], ydata[:,i]]) for i in range(nline)]

        if isinstance(segments, np.NDArray):
            self._segments = []
            ns = segments.shape[0]
            for i in range(ns):
                self._segments.append(segments[i])
        else:
            self._segments = segments

        data = []
        for s in self._segments:
            data.append(s._array)

        self._array = kwargs.pop('array', None)
        self._cdata = kwargs.pop('cdata', None)

        if self._array is None and self._cdata is None:
            if legend is None:
                legend = plotutil.getlegendbreak('line', **kwargs)[0]
                kwargs['ncolors'] = len(self._segments)
                legend = plotutil.getlegendbreaks(legend, **kwargs)
            Line2DGraphicCollection.__init__(self, data, legend)
        else:
            if self._array is not None:
                if legend is None:
                    legend = plotutil.getlegendscheme([len(self._segments)], self._array.min(), self._array.max(), **kwargs)
                    legend = plotutil.setlegendscheme_line(legend, **kwargs)
                Line2DGraphicCollection.__init__(self, data, self._array._array, legend)
            else:
                if legend is None:
                    legend = plotutil.getlegendscheme([], self._cdata.min(), self._cdata.max(), **kwargs)
                    legend = plotutil.setlegendscheme_line(legend, **kwargs)
                Line2DGraphicCollection.__init__(self, data, self._cdata._array, legend)

        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            self.setAntiAlias(antialias)

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
    def segments(self):
        return self._segments

    @segments.setter
    def segments(self, val):
        self._segments = val
        data = []
        for s in self._segments:
            data.append(s._array)
        self.setData(data)
        self.stale = True

    def set_segments(self, val, cdata=None):
        if cdata is None:
            self.segments = val
        else:
            self._segments = val
            data = []
            for s in self._segments:
                data.append(s._array)
            self.setData(data, cdata._array)
            self.stale = True

    def set_xydata(self, xydata, cdata=None):
        if len(xydata) == 1:
            ydata = xydata[0]
            xdata = None
        else:
            xdata = xydata[0]
            ydata = xydata[1]

        nseg, nline = ydata.shape
        if xdata is None:
            xdata = np.arange(nseg)

        if xdata.ndim == 1:
            segments = [np.column_stack([xdata, ydata[:,i]]) for i in range(nline)]
        else:
            segments = [np.column_stack([xdata[:,i], ydata[:,i]]) for i in range(nline)]

        self.set_segments(segments, cdata=cdata)
