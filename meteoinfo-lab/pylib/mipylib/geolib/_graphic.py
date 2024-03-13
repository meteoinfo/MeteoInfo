from org.meteoinfo.chart.graphic import GeoGraphicCollection as JGeoGraphicCollection
from java.awt import Font

import mipylib.miutil as miutil

class GeoGraphicCollection(object):

    def __init__(self, geographic=None):
        """
        Class init.

        :param geographic: (*JGeoGraphicCollection*) Java GeoGraphicCollection object.
        """
        self._geographic = geographic

    def get_graphics(self):
        """
        Get graphics.
        :return: Graphics.
        """
        return self._geographic

    @property
    def shapes(self):
        return self._geographic.getShapes()

    def addlabels(self, fieldname, **kwargs):
        """
        Add labels

        :param fieldname: (*string*) Field name.
        :param fontname: (*string*) Font name. Default is ``Arial``.
        :param fontsize: (*string*) Font size. Default is ``14``.
        :param bold: (*boolean*) Font bold or not. Default is ``False``.
        :param color: (*color*) Label color. Default is ``None`` with black color.
        :param xoffset: (*int*) X coordinate offset. Default is ``0``.
        :param yoffset: (*int*) Y coordinate offset. Default is ``0``.
        :param avoidcoll: (*boolean*) Avoid labels collision or not. Default is ``True``.
        :param decimals: (*int*) Number of decimals of labels.
        """
        labelset = self._geographic.getLabelSet()
        labelset.setFieldName(fieldname)
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
        self._geographic.addLabels()

    def getlabel(self, text):
        """
        Get a label.

        :param text: (*string*) The label text.
        """
        return self._geographic.getLabel(text)

    def movelabel(self, label, x=0, y=0):
        """
        Move a label.

        :param label: (*string*) The label text.
        :param x: (*float*) X shift for moving in pixel unit.
        :param y: (*float*) Y shift for moving in pixel unit.
        """
        self._geographic.moveLabel(label, x, y)

    def update_legend(self, ltype, fieldname):
        """
        Update legend scheme.

        :param ltype: (*string*) Legend type [single | unique | graduate].
        :param fieldname: (*string*) Field name.
        """
        if ltype == 'single':
            ltype = LegendType.SINGLE_SYMBOL
        elif ltype == 'unique':
            ltype = LegendType.UNIQUE_VALUE
        elif ltyp == 'graduate':
            ltype = LegendType.GRADUATED_COLOR
        else:
            raise ValueError(ltype)
        self._geographic.updateLegendScheme(ltype, fieldname)
        return self._geographic.getLegendScheme()
