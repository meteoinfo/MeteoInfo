from org.meteoinfo.geometry.graphic import GraphicCollection, Point2DGraphicCollection
from java.awt import Font

from .. import plotutil
from ... import miutil
from artist import Artist
import mipylib.numeric as np

__all__ = ['Point2DCollection']


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
