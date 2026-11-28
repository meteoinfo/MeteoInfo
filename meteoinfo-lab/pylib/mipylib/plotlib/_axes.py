# coding=utf-8
# -----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2019-9-4
# Purpose: MeteoInfoLab axes3dgl module - using JOGL
# Note: Jython
# -----------------------------------------------------

from org.meteoinfo.chart.graphic import GraphicFactory
from org.meteoinfo.chart import AspectType
from org.meteoinfo.geometry.legend import BreakTypes, BarBreak, LegendManage
from org.meteoinfo.geo.layer import LayerTypes
from org.meteoinfo.geo.io import GraphicUtil
from org.meteoinfo.geometry import GeometryType, Extent
from org.meteoinfo.chart.graphic import Graphic, GraphicCollection, ModelGraphic
from org.meteoinfo.chart.jogl import Axes as JAxes, GLForm, JOGLUtil, EarthGLPlot, MapGLPlot
from org.meteoinfo.math.interpolate import InterpolationMethod
from org.meteoinfo.image import ImageUtil
from org.meteoinfo.chart.transform import GeoTransform
from javax.swing import WindowConstants
from java.awt import Font, Color
from java.awt.image import BufferedImage

import os
import numbers
import warnings

import plotutil
import colors
from ._axes3d import Axes3D
from .graphic import PolyCollection
from mipylib.numeric.core import NDArray
from mipylib.dataset import DimArray
import mipylib.numeric as np
from mipylib import migl
from mipylib.geolib import migeo

from org.meteoinfo.chart.plot import Plot3D
from org.meteoinfo.chart import ChartText3D
from org.meteoinfo.chart.axis import Axis, LonLatAxis, TimeAxis, LogAxis
from org.meteoinfo.geometry.legend import BreakTypes, PolylineBreak, LegendManage
from org.meteoinfo.geometry import GeometryType
from org.meteoinfo.chart.graphic import Graphic
from ._axes2d import Axes2D
from .graphic import LineCollection, PointCollection
import mipylib.miutil as miutil
import datetime
from java.awt import Font, Color, BasicStroke
from org.meteoinfo.chart import Location, ChartWindArrow, ChartText, LegendPosition, \
    ChartLegend, ChartColorBar, AspectType, ScaleType, AxisType
from org.meteoinfo.chart.plot import Plot2D, PolarPlot, PlotOrientation
from org.meteoinfo.common import XAlign, YAlign
from org.meteoinfo.geometry.legend import BarBreak, PolygonBreak, PolylineBreak, \
    PointBreak, LineStyles, PointStyle, LegendScheme, LegendType, LegendManage, ExtendFraction
from org.meteoinfo.chart.graphic import Graphic, GraphicCollection, ImageGraphic
from org.meteoinfo.geometry.colors import ExtendType
from org.meteoinfo.common import MIMath
from org.meteoinfo.geometry import Extent, GeometryType
from org.meteoinfo.geo.layer import MapLayer
from java.util import HashMap
import math
from mipylib.geolib.milayer import MILayer, MIXYListData
from .graphic import Line, Artist, Polygon, PointCollection, LineCollection, PolyCollection
from ..numeric.spatial import transform
from ._axes2d import Axes2D, PolarAxes2D
from ._axes3d import Axes3D
__all__ = ['Axes', 'MapAxes', 'EarthAxes']



class Axes(object):
    """
    Axes with Cartesian coordinate.
    """

    def __init__(self, *args, **kwargs):
        """
        Axes 3d with openGL support.
        :param position: (*list of float*) Axes position specified by *position=* [left, bottom, width
            height] in normalized (0, 1) units. Default is [0.13, 0.11, 0.775, 0.815].
        :param rotation: (*float*) Axes rotation angle around Z axis.
        :param elevation: (*float*) Axes elevation angle with Z axis.
        :param antialias: (*bool*) Antialias or not. Default is `None`.
        """
        self._stale = True

        axes = kwargs.pop('axes', None)
        self._set_plot(axes)

        figure = kwargs.pop('figure', None)
        self._figure = figure

        if len(args) > 0:
            position = args[0]
        else:
            position = kwargs.pop('position', None)
        outerposition = kwargs.pop('outerposition', None)
        if position is None:
            position = [0.13, 0.11, 0.71, 0.815]
            #position = [0, 0, 1, 1]
            self.active_outerposition(True)
        else:
            self.active_outerposition(False)
        self.position = position
        if not outerposition is None:
            self.outerposition = outerposition
            self.active_outerposition(True)
        facecolor = kwargs.pop('facecolor', None)
        if not facecolor is None:
            facecolor = plotutil.getcolor(facecolor)
            self._axes.setBackground(facecolor)
        edgecolor = kwargs.pop('edgecolor', None)
        if not edgecolor is None:
            edgecolor = plotutil.getcolor(edgecolor)
            self._axes.setForeground(edgecolor)
        units = kwargs.pop('units', None)
        if not units is None:
            self._axes.setUnits(units)
        tickfontname = kwargs.pop('tickfontname', 'Arial')
        tickfontsize = kwargs.pop('tickfontsize', 14)
        tickbold = kwargs.pop('tickbold', False)
        if tickbold:
            font = Font(tickfontname, Font.BOLD, tickfontsize)
        else:
            font = Font(tickfontname, Font.PLAIN, tickfontsize)
        self._axes.setAxisTickFont(font)
        orthographic = kwargs.pop('orthographic', None)
        if not orthographic is None:
            self._axes.setOrthographic(orthographic)
        rotation = kwargs.pop('rotation', None)
        if not rotation is None:
            self._axes.setAngleY(rotation)
        elevation = kwargs.pop('elevation', None)
        if not elevation is None:
            self._axes.setAngleX(elevation)
        antialias = kwargs.pop('antialias', None)
        if not antialias is None:
            self._axes.setAntialias(antialias)
        clip_plane = kwargs.pop('clip_plane', None)
        if not clip_plane is None:
            self._axes.setClipPlane(clip_plane)
        axes_zoom = kwargs.pop('axes_zoom', None)
        if not axes_zoom is None:
            self._axes.setAxesZoom(axes_zoom)
        aspect = kwargs.pop('aspect', None)
        if not aspect is None:
            self._axes.setAspectType(AspectType.valueOf(aspect.upper()))
        distance = kwargs.pop('distance', None)
        if not distance is None:
            self._axes.setDistance(distance)
        axis = kwargs.pop('axis', True)
        if not axis:
            self._axes.setDrawBase(False)
            self._axes.setBoxed(False)
            self._axes.setDisplayXY(False)
            self._axes.setDisplayZ(False)

    def _set_plot(self, plot):
        """
        Set plot.
        
        :param plot: (*Axes3D*) Plot.
        """
        if plot is None:
            self._axes = JAxes()
        else:
            self._axes = plot

    @property
    def stale(self):
        return self._stale

    @stale.setter
    def stale(self, val):
        self._stale = val
        #self._axes.setAutoExtent()

        if self._figure is not None:
            self._figure.stale = val

    @property
    def figure(self):
        return self._figure

    @figure.setter
    def figure(self, val):
        self._figure = val

    @property
    def transData(self):
        return self._axes.getTransData()

    @transData.setter
    def transData(self, val):
        self._axes.setTransData(val)

    @property
    def transScale(self):
        return self._axes.getTransScale()

    @transScale.setter
    def transScale(self, val):
        self._axes.setTransScale(val)

    @property
    def transLimits(self):
        return self._axes.getTransLimits()

    @transLimits.setter
    def transLimits(self, val):
        self._axes.setTransLimits(val)

    @property
    def transAxes(self):
        return self._axes.getTransAxes()

    @transAxes.setter
    def transAxes(self, val):
        self._axes.setTransAxes(val)

    @property
    def axestype(self):
        return '3d'

    @property
    def axestype(self):
        return '3d'

    @property
    def ndim(self):
        """
        Dimension number property
        """
        return 31

    def get_type(self):
        """
        Get axes type
        
        :returns: Axes type
        """
        return self._axes.getPlotType()

    @property
    def position(self):
        """
        Get axes position             

        :returns: Axes position [left, bottom, width, height] in normalized (0, 1) units
        """
        pos = self._axes.getPosition()
        return [pos.x, pos.y, pos.width, pos.height]

    @position.setter
    def position(self, pos):
        """
        Set axes position
        
        :param pos: (*list*) Axes position specified by *position=* [left, bottom, width,
            height] in normalized (0, 1) units
        """
        self._axes.setPosition(pos)
        self.stale = True

    @property
    def outerposition(self):
        """
        Get axes outer position
        
        :returns: Axes outer position [left, bottom, width, height] in normalized (0, 1) units
        """
        pos = self._axes.getOuterPosition()
        return [pos.x, pos.y, pos.width, pos.height]

    @outerposition.setter
    def outerposition(self, pos):
        """
        Set axes outer position
        
        :param pos: (*list*) Axes outer position specified by *outerposition=* [left, bottom, width,
            height] in normalized (0, 1) units
        """
        self._axes.setOuterPosition(pos)
        self.stale = True

    def active_outerposition(self, active):
        """
        Set axes outer position active or not.
        
        :param active: (*boolean*) Active or not
        """
        self._axes.setOuterPosActive(active)

    def get_axis(self, loc):
        """
        Get axis by location.
        
        :param loc: (*Location*) Location enum.
        
        :returns: Axis
        """
        return self._axes.getAxis(loc)

    @property
    def facecolor(self):
        """Get facecolor"""
        return self._axes.getBackground()

    @facecolor.setter
    def facecolor(self, value):
        """
        Set facecolor property value.

        :param value: Fill color of the axes.
        """
        self._axes.setBackground(plotutil.getcolor(value))
        self.stale = True

    @property
    def aspect(self):
        """Get or set aspect ['auto' | 'equal']"""
        return 'equal' if self._axes.getAspectType() == AspectType.EQUAL else 'auto'

    @aspect.setter
    def aspect(self, aspect):
        """
        Set axes aspect

        :param aspect: (*string or number*) Axes aspect ['equal' | 'auto'].
        """
        if aspect == 'equal':
            self._axes.setAspectType(AspectType.EQUAL)
        else:
            if isinstance(aspect, (int, float)):
                self._axes.setAspect(aspect)
                self._axes.setAspectType(AspectType.RATIO)
            else:
                self._axes.setAspectType(AspectType.AUTO)
        self.stale = True

    def set_clip(self, clip):
        """
        Set axes clip or not

        :param clip: (*bool*) Clip or not
        """
        self._axes.setClip(clip)
        self.stale = True

    def get_title(self, loc='center'):
        """
        Get title            

        :param loc: (*string*) Which title to get ['center' | 'left' | 'right'],
            default to 'center'.
        
        :returns: The title.
        """
        if loc == 'left':
            return self.aexs.getLeftTitle()
        elif loc == 'right':
            return self._axes.getRightTitle()
        else:
            return self._axes.getTitle()

    def set_title(self, label, loc='center', **kwargs):
        """
        Set a title of the current axes.
        
        :param label: (*string*) Title string.
        :param loc: (*string') Which title to set ['center' | 'left' | 'right'],
            default to 'center'.
        :param fontname: (*string*) Font name. Default is ``None``, using ``Arial`` .
        :param fontsize: (*int*) Font size. Default is ``14`` .
        :param bold: (*boolean*) Is bold font or not. Default is ``True`` .
        :param color: (*color*) Title string color. Default is ``black`` .  
        :param linespace: (*int*) Line space of multiple line title.
        """
        if not kwargs.has_key('bold'):
            kwargs['bold'] = True
        if not kwargs.has_key('xalign'):
            kwargs['xalign'] = 'center'
        title = plotutil.text(0, 0, label, **kwargs)

        if loc == 'left':
            self._axes.setLeftTitle(title)
        elif loc == 'right':
            self._axes.setRightTitle(title)
        else:
            self._axes.setTitle(title)
        self.stale = True
        return title

    def set_xlabel(self, label, **kwargs):
        """
        Set the x axis label of the current axes.
        
        :param label: (*string*) Label string.
        :param fontname: (*string*) Font name. Default is ``Arial`` .
        :param fontsize: (*int*) Font size. Default is ``14`` .
        :param bold: (*boolean*) Is bold font or not. Default is ``True`` .
        :param color: (*color*) Label string color. Default is ``black`` .
        """
        if not kwargs.has_key('xalign'):
            kwargs['xalign'] = 'center'
        if not kwargs.has_key('yalign'):
            kwargs['yalign'] = 'top'
        ctext = plotutil.text(0, 0, label, **kwargs)
        axis = self._axes.getXAxis()
        axis.setLabel(ctext)
        axis.setDrawLabel(True)
        if self.axestype != '3d':
            axis_t = self._axes.getAxis(Location.TOP)
            text = ctext.clone()
            text.setXAlign('center')
            text.setYAlign('bottom')
            axis_t.setLabel(text)
        self.stale = True

    def set_ylabel(self, label, **kwargs):
        """
        Set the y axis label of the current axes.
        
        :param label: (*string*) Label string.
        :param fontname: (*string*) Font name. Default is ``Arial`` .
        :param fontsize: (*int*) Font size. Default is ``14`` .
        :param bold: (*boolean*) Is bold font or not. Default is ``True`` .
        :param color: (*color*) Label string color. Default is ``black`` .
        """
        if not kwargs.has_key('xalign'):
            kwargs['xalign'] = 'center'
        if not kwargs.has_key('yalign'):
            kwargs['yalign'] = 'bottom'
        if not kwargs.has_key('rotation'):
            kwargs['rotation'] = 90
        ctext = plotutil.text(0, 0, label, **kwargs)
        axis = self._axes.getYAxis()
        axis.setLabel(ctext)
        axis.setDrawLabel(True)
        if self.axestype != '3d':
            axis_r = self._axes.getAxis(Location.RIGHT)
            text = ctext.clone()
            text.setXAlign('left')
            text.setYAlign('center')
            axis_r.setLabel(text)
        self.stale = True

    def get_xticks(self):
        """
        Get x axis tick locations.
        """
        axis = self._axes.getXAxis()
        axis.updateTickLabels()
        return axis.getTickLocations()

    def set_xticks(self, locs):
        """
        Set x axis tick locations.
        """
        axis = self._axes.getXAxis()
        if isinstance(locs, (NDArray, DimArray)):
            locs = locs.aslist()
        axis.setTickLocations(locs)

        if self.axestype == '3d':
            axis_t = None
        else:
            axis_t = self._axes.getAxis(Location.TOP)
        if not axis_t is None:
            axis_t.setTickLocations(locs)
        self.stale = True

    def get_yticks(self):
        """
        Get y axis tick locations.
        """
        axis = self._axes.getYAxis()
        axis.updateTickLabels()
        return axis.getTickLocations()

    def set_yticks(self, locs):
        """
        Set y axis tick locations.
        """
        axis = self._axes.getYAxis()
        if isinstance(locs, NDArray):
            locs = locs.aslist()
        axis.setTickLocations(locs)

        if self.axestype == '3d':
            axis_r = None
        else:
            axis_r = self._axes.getAxis(Location.RIGHT)
        if not axis_r is None:
            axis_r.setTickLocations(locs)
        self.stale = True

    def get_xticklabels(self):
        """
        Get x axis tick labels.
        """
        axis = self._axes.getXAxis()
        axis.updateTickLabels()
        return axis.getTickLabelText()

    def set_xticklabels(self, labels, **kwargs):
        """
        Set x axis tick labels.
        """
        axis = self._axes.getXAxis()
        if self.axestype == '3d':
            axis_t = None
        else:
            axis_t = self._axes.getAxis(Location.TOP)

        if not labels is None:
            if isinstance(labels, (NDArray, DimArray)):
                labels = labels.aslist()
            if isinstance(labels[0], (int, long, float)):
                axis.setTickLabels_Number(labels)
            else:
                axis.setTickLabelText(labels)

            if not axis_t is None:
                if isinstance(labels[0], (int, long, float)):
                    axis_t.setTickLabels_Number(labels)
                else:
                    axis_t.setTickLabelText(labels)

        fontname = kwargs.pop('fontname', axis.getTickLabelFont().getName())
        fontsize = kwargs.pop('fontsize', axis.getTickLabelFont().getSize())
        bold = kwargs.pop('bold', axis.getTickLabelFont().isBold())
        if bold:
            font = Font(fontname, Font.BOLD, fontsize)
        else:
            font = Font(fontname, Font.PLAIN, fontsize)
        color = kwargs.pop('color', axis.getTickLabelColor())
        c = plotutil.getcolor(color)
        angle = kwargs.pop('rotation', 0)
        if angle == 'vertical':
            angle = 90
        axis.setTickLabelFont(font)
        axis.setTickLabelColor(c)
        axis.setTickLabelAngle(angle)
        if not axis_t is None:
            axis_t.setTickLabelFont(font)
            axis_t.setTickLabelColor(c)
            axis_t.setTickLabelAngle(angle)
        self.stale = True

    def get_yticklabels(self):
        """
        Get y axis tick labels.
        """
        axis = self._axes.getYAxis()
        axis.updateTickLabels()
        return axis.getTickLabelText()

    def set_yticklabels(self, labels, **kwargs):
        """
        Set y axis tick labels.
        """
        axis = self._axes.getYAxis()
        if self.axestype == '3d':
            axis_r = None
        else:
            axis_r = self._axes.getAxis(Location.RIGHT)

        if not labels is None:
            if isinstance(labels, (NDArray, DimArray)):
                labels = labels.aslist()
            if isinstance(labels[0], (int, long, float)):
                axis.setTickLabels_Number(labels)
            else:
                axis.setTickLabelText(labels)

            if not axis_r is None:
                if isinstance(labels[0], (int, long, float)):
                    axis_r.setTickLabels_Number(labels)
                else:
                    axis_r.setTickLabelText(labels)

        fontname = kwargs.pop('fontname', axis.getTickLabelFont().getName())
        fontsize = kwargs.pop('fontsize', axis.getTickLabelFont().getSize())
        bold = kwargs.pop('bold', axis.getTickLabelFont().isBold())
        if bold:
            font = Font(fontname, Font.BOLD, fontsize)
        else:
            font = Font(fontname, Font.PLAIN, fontsize)
        color = kwargs.pop('color', axis.getTickLabelColor())
        c = plotutil.getcolor(color)
        angle = kwargs.pop('rotation', 0)
        if angle == 'vertical':
            angle = 90
        axis.setTickLabelFont(font)
        axis.setTickLabelColor(c)
        axis.setTickLabelAngle(angle)
        if not axis_r is None:
            axis_r.setTickLabelFont(font)
            axis_r.setTickLabelColor(c)
            axis_r.setTickLabelAngle(angle)
        self.stale = True

    def set_xaxis_type(self, axistype, timetickformat=None):
        """
        Set x axis type.

        :param axistype: (*string*) Axis type ['lon' | 'lat' | 'time' | 'log'].
        :param timetickformat: (*string*) Time tick label format.
        """
        ax = self._axes
        if axistype == 'lon':
            axis = LonLatAxis(ax.getXAxis())
            axis.setLongitude(False)
            ax.setXAxis(axis)
        elif axistype == 'lat':
            axis = LonLatAxis(ax.getXAxis())
            axis.setLongitude(False)
            ax.setXAxis(axis)
        elif axistype == 'time':
            axis = TimeAxis(ax.getXAxis())
            ax.setXAxis(axis)
            if not timetickformat is None:
                ax.getXAxis().setTimeFormat(timetickformat)
        elif axistype == 'log':
            axis = LogAxis(ax.getXAxis())
            axis.setMinorTickNum(10)
            ax.setXAxis(axis)
        else:
            axis = Axis(ax.getXAxis())
            ax.setXAxis(axis)

    def set_yaxis_type(self, axistype, timetickformat=None):
        """
        Set y axis type.

        :param axistype: (*string*) Axis type ['lon' | 'lat' | 'time' | 'log'].
        :param timetickformat: (*string*) Time tick label format.
        """
        ax = self._axes
        if axistype == 'lon':
            axis = LonLatAxis(ax.getYAxis())
            axis.setLongitude(True)
            ax.setYAxis(axis)
        elif axistype == 'lat':
            axis = LonLatAxis(ax.getYAxis())
            axis.setLongitude(False)
            ax.setYAxis(axis)
        elif axistype == 'time':
            axis = TimeAxis(ax.getYAxis())
            ax.setYAxis(axis)
            if not timetickformat is None:
                ax.getYAxis().setTimeFormat(timetickformat)
        elif axistype == 'log':
            axis = LogAxis(ax.getYAxis())
            axis.setMinorTickNum(10)
            ax.setYAxis(axis)
        else:
            axis = Axis(ax.getYAxis())
            ax.setYAxis(axis)

    def set_axis_on(self):
        """
        Set all axis visible.
        """
        self._axes.setDrawBase(True)
        self._axes.setBoxed(True)
        self._axes.setDisplayXY(True)
        self._axes.setDisplayZ(True)

    def set_axis_off(self):
        """
        Set all axis not visible.
        """
        self._axes.setDrawBase(False)
        self._axes.setBoxed(False)
        self._axes.setDisplayXY(False)
        self._axes.setDisplayZ(False)

    def set_aspect(self, aspect):
        if aspect == 'equal':
            self._axes.setAspectType(AspectType.EQUAL)
        else:
            if isinstance(aspect, (int, float)):
                self._axes.setAspect(aspect)
                self._axes.setAspectType(AspectType.RATIO)

    def axis(self, arg=None, **kwargs):
        """
        Convenience method to get or set some axis properties.

        Call signatures::
          xmin, xmax, ymin, ymax = axis()
          xmin, xmax, ymin, ymax = axis([xmin, xmax, ymin, ymax])
          xmin, xmax, ymin, ymax = axis(option)
          xmin, xmax, ymin, ymax = axis(**kwargs)

        Parameters
        ----------
        xmin, xmax, ymin, ymax, zmin, zmax : float, optional
            The axis limits to be set.  This can also be achieved using ::
                ax.set(xlim=(xmin, xmax), ylim=(ymin, ymax), zlim=(zmin, zmax))

        option : bool or str
            If a bool, turns axis lines and labels on or off. If a string,
            possible values are:
            ======== ==========================================================
            Value    Description
            ======== ==========================================================
            'on'     Turn on axis lines and labels. Same as ``True``.
            'off'    Turn off axis lines and labels. Same as ``False``.
            'equal'  Set equal scaling (i.e., make circles circular) by
                     changing axis limits. This is the same as
                     ``ax.set_aspect('equal', adjustable='datalim')``.
                     Explicit data limits may not be respected in this case.
            'scaled' Set equal scaling (i.e., make circles circular) by
                     changing dimensions of the plot box. This is the same as
                     ``ax.set_aspect('equal', adjustable='box', anchor='C')``.
                     Additionally, further autoscaling will be disabled.
            'tight'  Set limits just large enough to show all data, then
                     disable further autoscaling.
            'auto'   Automatic scaling (fill plot box with data).
            'image'  'scaled' with axis limits equal to data limits.
            'square' Square plot; similar to 'scaled', but initially forcing
                     ``xmax-xmin == ymax-ymin``.
            ======== ==========================================================

        Returns
        -------
        xmin, xmax, ymin, ymax, zmin, zmax : float
            The axis limits.
        """
        if isinstance(arg, (str, bool)):
            if arg is True:
                arg = 'on'
            if arg is False:
                arg = 'off'
            arg = arg.lower()
            if arg == 'on':
                self.set_axis_on()
            elif arg == 'off':
                self.set_axis_off()
            elif arg in ['auto', 'equal']:
                self.set_aspect(arg)
            else:
                raise ValueError("Unrecognized string {} to axis; "
                                 "try 'on' or 'off'".format(arg))
        else:
            if arg is not None:
                if len(arg) == 4:
                    xmin, xmax, ymin, ymax = arg
                else:
                    xmin, xmax, ymin, ymax, zmin, zmax = arg
                    self.set_zlim(zmin, zmax)

                self.set_xlim(xmin, xmax)
                self.set_ylim(ymin, ymax)

        return self.get_xlim() + self.get_ylim() + self.get_zlim()

    @property
    def xlim(self):
        """Get or set x axis limits"""
        return self._axes.getXMin(), self._axes.getXMax()

    @xlim.setter
    def xlim(self, val):
        if not isinstance(val, (list, tuple)):
            raise ValueError("value {} is not supported, list or tuple with min and max limit values"
                             "are expected".format(val))

        xmin = val[0]
        xmax = val[1]
        if isinstance(xmin, datetime.datetime):
            xmin = miutil.date2num(xmin)
        if isinstance(xmax, datetime.datetime):
            xmax = miutil.date2num(xmax)
        self._axes.setXMinMax(xmin, xmax)
        self.stale = True

    def get_xlim(self):
        """
        Get the *x* limits of the current axes.
        
        :returns: (*tuple*) x limits.
        """
        return self._axes.getXMin(), self._axes.getXMax()

    def set_xlim(self, xmin, xmax):
        """
        Set the *x* limits of the current axes.
        
        :param xmin: (*float*) Minimum limit of the x axis.
        :param xmax: (*float*) Maximum limit of the x axis.
        """
        if isinstance(xmin, datetime.datetime):
            xmin = miutil.date2num(xmin)
        if isinstance(xmax, datetime.datetime):
            xmax = miutil.date2num(xmax)                
        self._axes.setXMinMax(xmin, xmax)

    @property
    def ylim(self):
        """Get or set y axis limits"""
        return self._axes.getYMin(), self._axes.getYMax()

    @ylim.setter
    def ylim(self, val):
        if not isinstance(val, (list, tuple)):
            raise ValueError("value {} is not supported, list or tuple with min and max limit values"
                             "are expected".format(val))

        ymin = val[0]
        ymax = val[1]
        if isinstance(ymin, datetime.datetime):
            ymin = miutil.date2num(ymin)
        if isinstance(ymax, datetime.datetime):
            ymax = miutil.date2num(ymax)
        self._axes.setYMinMax(ymin, ymax)
        self.stale = True

    def get_ylim(self):
        """
        Get the *y* limits of the current axes.
        
        :returns: (*tuple*) y limits.
        """
        return self._axes.getYMin(), self._axes.getYMax()

    def set_ylim(self, ymin, ymax):
        """
        Set the *y* limits of the current axes.
        
        :param ymin: (*float*) Minimum limit of the y axis.
        :param ymax: (*float*) Maximum limit of the y axis.
        """
        if isinstance(ymin, datetime.datetime):
            ymin = miutil.date2num(ymin)
        if isinstance(ymax, datetime.datetime):
            ymax = miutil.date2num(ymax)    
        self._axes.setYMinMax(ymin, ymax)

    def set_draw_extent(self, extent):
        """
        Set axes draw extent.

        :param extent: The extent
        """
        if not self._axes.isFixDrawExtent():
            self._axes.setDrawExtent(extent.clone())
            self._axes.setExtent(extent.clone())
        self.stale = True

    def twinx(self):
        """
        Make a second axes that shares the x-axis. The new axes will overlay *ax*. The ticks 
        for *ax2* will be placed on the right, and the *ax2* instance is returned.
        
        :returns: The second axes
        """
        self._axes.getAxis(Location.RIGHT).setVisible(False)
        self._axes.setSameShrink(True)
        ax2 = Axes2D()
        ax2._axes.setSameShrink(True)
        ax2._axes.setPosition(self.position)
        ax2._axes.setOuterPosActive(self._axes.isOuterPosActive())
        ax2._axes.getAxis(Location.BOTTOM).setVisible(False)
        ax2._axes.getAxis(Location.LEFT).setVisible(False)
        ax2._axes.getAxis(Location.TOP).setVisible(False)
        axis = ax2._axes.getAxis(Location.RIGHT)
        axis.setDrawTickLine(True)
        axis.setDrawTickLabel(True)
        axis.setDrawLabel(True)
        return ax2

    def twiny(self):
        """
        Make a second axes that shares the y-axis. The new axes will overlay *ax*. The ticks 
        for *ax2* will be placed on the top, and the *ax2* instance is returned.
        
        :returns: The second axes
        """
        self._axes.getAxis(Location.TOP).setVisible(False)
        self._axes.setSameShrink(True)
        ax2 = Axes2D()
        ax2._axes.setSameShrink(True)
        ax2._axes.setPosition(self.position)
        ax2._axes.setOuterPosActive(self._axes.isOuterPosActive())
        ax2._axes.getAxis(Location.BOTTOM).setVisible(False)
        ax2._axes.getAxis(Location.LEFT).setVisible(False)
        ax2._axes.getAxis(Location.RIGHT).setVisible(False)
        axis = ax2._axes.getAxis(Location.TOP)
        axis.setDrawTickLine(True)
        axis.setDrawTickLabel(True)
        axis.setDrawLabel(True)
        return ax2

    def xaxis(self, **kwargs):
        """
        Set x-axis of the axes.
        
        :param color: (*Color*) Color of the axis. Default is `black`.
        :param position: (*tuple*) Axis position specified by a 2 tuple of (position type, amount). 
            The position types are ['outerward' | 'axes' | 'data'].
        :param shift: (*int) Axis shif along x direction. Units are pixel. Default is 0.
        :param visible: (*bool*) Set axis visible or not, Default is `None`.
        :param linewidth: (*float*) Line width of the axis.
        :param linestyle: (*string*) Line style of the axis.
        :param tickline: (*boolean*) Draw tick line or not.
        :param tickwidth: (*float*) Tick line width.
        :param ticklength: (*float*) Tick line length.
        :param ticklabel: (*bool*) Draw tick label or not.
        :param minortick: (*bool*) Draw minor tick line or not.
        :param minorticknum: (*int*) Minor tick line number between two adjacent major tick lines.
        :param tickin: (*bool*) Tick lines are plot inside or outside the axes.
        :param axistype: (*string*) Axis type ['normal' | 'lon' | 'lat' | 'time' | 'log'].
        :param timetickformat: (*string*) Time tick label format, only valid with time axis.
        :param tickfontname: (*string*) Tick label font name.
        :param tickfontsize: (*int*) Tick label font size.
        :param tickbold: (*bool*) Tick label font is bold or not.
        :param tickavoidcoll: (*bool*) Whether avoid collision of tick labels. Default is `True`.
        :param location: (*string*) Locations of the axis ['both' | 'top' | 'bottom'].
        """
        visible = kwargs.pop('visible', None)
        position = kwargs.pop('position', None)
        if position == 'center':
            position = ['axes', 0.5]
        elif position == 'zero':
            position = ['data', 0]
        shift = kwargs.pop('shift', None)
        color = kwargs.pop('color', None)
        if not color is None:
            color = plotutil.getcolor(color)
        tickcolor = kwargs.pop('tickcolor', None)
        if not tickcolor is None:
            tickcolor = plotutil.getcolor(tickcolor)
        linewidth = kwargs.pop('linewidth', None)
        linestyle = kwargs.pop('linestyle', None)
        tickline = kwargs.pop('tickline', None)
        tickline = kwargs.pop('tickvisible', tickline)
        tickwidth = kwargs.pop('tickwidth', None)
        ticklength = kwargs.pop('ticklength', None)
        ticklabel = kwargs.pop('ticklabel', None)
        minortick = kwargs.pop('minortick', None)
        minorticknum = kwargs.pop('minorticknum', None)
        tickin = kwargs.pop('tickin', None)
        axistype = kwargs.pop('axistype', None)
        timetickformat = kwargs.pop('timetickformat', None)
        if not axistype is None:
            self.set_xaxis_type(axistype, timetickformat)
            #self._axes.setAutoExtent()

        location = kwargs.pop('location', 'both')
        if location == 'top':
            locs = [Location.TOP]
        elif location == 'bottom':
            locs = [Location.BOTTOM]
        else:
            locs = [Location.BOTTOM, Location.TOP]
        axislist = []
        if self.axestype == '3d':
            axislist.append(self._axes.getXAxis())
        else:
            for loc in locs:
                axislist.append(self._axes.getAxis(loc))

        ticklabelcolor = kwargs.pop('ticklabelcolor', None)
        if not ticklabelcolor is None:
            ticklabelcolor = plotutil.getcolor(ticklabelcolor)
        tick_font = axislist[0].getTickLabelFont()
        tickfontname = kwargs.pop('tickfontname', tick_font.getFontName())
        tickfontsize = kwargs.pop('tickfontsize', tick_font.getSize())
        tickbold = kwargs.pop('tickbold', tick_font.isBold())
        if tickbold:
            font = Font(tickfontname, Font.BOLD, tickfontsize)
        else:
            font = Font(tickfontname, Font.PLAIN, tickfontsize)
        tickavoidcoll = kwargs.pop('tickavoidcoll', None)

        for axis in axislist:
            if not visible is None:
                axis.setVisible(visible)
            if not position is None:
                axis.setPositionType(position[0])
                axis.setPosition(position[1])
            else:
                if not shift is None:
                    axis.setPositionType('OUTERWARD')
                    axis.setPosition(shift)
            if not color is None:
                axis.setColor_All(color)
            if not tickcolor is None:
                axis.setTickColor(tickcolor)
            if not linewidth is None:
                axis.setLineWidth(linewidth)
            if not linestyle is None:
                axis.setLineStyle(linestyle)
            if not tickline is None:
                axis.setDrawTickLine(tickline)
            if not tickwidth is None:
                axis.setTickWidth(tickwidth)
            if not ticklength is None:
                axis.setTickLength(ticklength)
            if not ticklabel is None:
                axis.setDrawTickLabel(ticklabel)
            if not minortick is None:
                axis.setMinorTickVisible(minortick)
            if not minorticknum is None:
                axis.setMinorTickNum(minorticknum)
            if not tickin is None:
                axis.setInsideTick(tickin)
            if not ticklabelcolor is None:
                axis.setTickLabelColor(ticklabelcolor)
            if not tickavoidcoll is None:
                axis.setTickLabelAvoidCollision(tickavoidcoll)
            axis.setTickLabelFont(font)
        self.stale = True

    def yaxis(self, **kwargs):
        """
        Set y-axis of the axes.

        :param color: (*Color*) Color of the y axis. Default is 'black'.
        :param position: (*tuple*) Axis position specified by a 2 tuple of (position type, amount). 
            The position types are ['outerward' | 'axes' | 'data'].
        :param shift: (*int) Y axis shift along x direction. Units are pixel. Default is 0.
        :param visible: (*bool*) Set axis visible or not, Default is `None`.
        :param linewidth: (*float*) Line width of the axis.
        :param linestyle: (*string*) Line style of the axis.
        :param tickline: (*bool*) Draw tick line or not.
        :param tickwidth: (*float*) Tick line width.
        :param ticklength: (*float*) Tick line length.
        :param ticklabel: (*bool*) Draw tick label or not.
        :param minortick: (*bool*) Draw minor tick line or not.
        :param minorticknum: (*int*) Minor tick line number between two adjacent major tick lines.
        :param tickin: (*bool*) Tick lines are ploted inside or outside the axes.
        :param axistype: (*string*) Axis type ['normal' | 'lon' | 'lat' | 'time' | 'log'].
        :param timetickformat: (*string*) Time tick label format, only valid with time axis.
        :param tickfontname: (*string*) Tick label font name.
        :param tickfontsize: (*int*) Tick label font size.
        :param tickbold: (*bool*) Tick label font is bold or not.
        :param tickavoidcoll: (*bool*) Whether avoid collision of tick labels. Default is `True`.
        :param location: (*string*) Locations of the axis ['both' | 'left' | 'right'].
        """
        visible = kwargs.pop('visible', None)
        position = kwargs.pop('position', None)
        if position == 'center':
            position = ['axes', 0.5]
        elif position == 'zero':
            position = ['data', 0]
        shift = kwargs.pop('shift', None)
        color = kwargs.pop('color', None)
        if not color is None:
            color = plotutil.getcolor(color)
        tickcolor = kwargs.pop('tickcolor', None)
        if not tickcolor is None:
            tickcolor = plotutil.getcolor(tickcolor)
        linewidth = kwargs.pop('linewidth', None)
        linestyle = kwargs.pop('linestyle', None)
        tickline = kwargs.pop('tickline', None)
        tickline = kwargs.pop('tickvisible', tickline)
        tickwidth = kwargs.pop('tickwidth', None)
        ticklength = kwargs.pop('ticklength', None)
        ticklabel = kwargs.pop('ticklabel', None)
        minortick = kwargs.pop('minortick', None)
        minorticknum = kwargs.pop('minorticknum', None)
        tickin = kwargs.pop('tickin', None)
        axistype = kwargs.pop('axistype', None)
        timetickformat = kwargs.pop('timetickformat', None)
        if not axistype is None:
            self.set_yaxis_type(axistype, timetickformat)
            #self._axes.updateDrawExtent()

        location = kwargs.pop('location', 'both')
        if location == 'left':
            locs = [Location.LEFT]
        elif location == 'right':
            locs = [Location.RIGHT]
        else:
            locs = [Location.LEFT, Location.RIGHT]
        axislist = []
        if self.axestype == '3d':
            axislist.append(self._axes.getYAxis())
        else:
            for loc in locs:
                axislist.append(self._axes.getAxis(loc))

        ticklabelcolor = kwargs.pop('ticklabelcolor', None)
        if not ticklabelcolor is None:
            ticklabelcolor = plotutil.getcolor(ticklabelcolor)
        tick_font = axislist[0].getTickLabelFont()
        tickfontname = kwargs.pop('tickfontname', tick_font.getFontName())
        tickfontsize = kwargs.pop('tickfontsize', tick_font.getSize())
        tickbold = kwargs.pop('tickbold', tick_font.isBold())
        if tickbold:
            font = Font(tickfontname, Font.BOLD, tickfontsize)
        else:
            font = Font(tickfontname, Font.PLAIN, tickfontsize)
        tickavoidcoll = kwargs.pop('tickavoidcoll', None)

        for axis in axislist:
            if not visible is None:
                axis.setVisible(visible)
            if not position is None:
                axis.setPositionType(position[0])
                axis.setPosition(position[1])
            else:
                if not shift is None:
                    axis.setPositionType('OUTERWARD')
                    axis.setPosition(shift)
            if not color is None:
                axis.setColor_All(color)
            if not tickcolor is None:
                axis.setTickColor(tickcolor)
            if not linewidth is None:
                axis.setLineWidth(linewidth)
            if not linestyle is None:
                axis.setLineStyle(linestyle)
            if not tickline is None:
                axis.setDrawTickLine(tickline)
            if not tickwidth is None:
                axis.setTickWidth(tickwidth)
            if not ticklength is None:
                axis.setTickLength(ticklength)
            if not ticklabel is None:
                axis.setDrawTickLabel(ticklabel)
            if not minortick is None:
                axis.setMinorTickVisible(minortick)
            if not minorticknum is None:
                axis.setMinorTickNum(minorticknum)
            if not tickin is None:
                axis.setInsideTick(tickin)
            if not ticklabelcolor is None:
                axis.setTickLabelColor(ticklabelcolor)
            if not tickavoidcoll is None:
                axis.setTickLabelAvoidCollision(tickavoidcoll)
            axis.setTickLabelFont(font)
        self.stale = True

    def get_xscale(self):
        """
        Return the xaxis' scale (as a str).
        """
        axis = self._axes.getAxis(Location.BOTTOM)
        if isinstance(axis, LogAxis):
            return 'log'
        else:
            return 'linear'

    def set_xscale(self, scale):
        """
        Set the yaxis' scale (as a str).

        Parameters
        ----------
        valuestr or ScaleBase
            The axis scale type to apply. Valid string values are the names of scale
            classes ("linear", "log",...).
        """
        self._axes.setXScaleType(scale)
        self.stale = True

    def get_yscale(self):
        """
        Return the yaxis' scale (as a str).
        """
        axis = self._axes.getAxis(Location.LEFT)
        if isinstance(axis, LogAxis):
            return 'log'
        else:
            return 'linear'

    def set_yscale(self, scale):
        """
        Set the yaxis' scale (as a str).

        Parameters
        ----------
        valuestr or ScaleBase
            The axis scale type to apply. Valid string values are the names of scale
            classes ("linear", "log",...).
        """
        self._axes.setYScaleType(scale)
        self.stale = True

    def get_xinverted(self):
        """
        Return whether the xaxis is oriented in the "inverse" direction.
        """
        return self._axes.isXInverted()

    def set_xinverted(self, value):
        """
        Set whether the xaxis is oriented in the "inverse" direction.
        """
        self._axes.setXInverted(value)
        self.stale = True

    def get_yinverted(self):
        """
        Return whether the yaxis is oriented in the "inverse" direction.
        """
        return self._axes.isYInverted()

    def set_yinverted(self, value):
        """
        Set whether the yaxis is oriented in the "inverse" direction.
        """
        self._axes.setYInverted(value)
        self.stale = True

    def xreverse(self):
        """
        Reverse x axis.
        """
        self._axes.getXAxis().setInverse(True)
        self.stale = True

    def yreverse(self):
        """
        Reverse y axis.
        """
        self._axes.getYAxis().setInverse(True)
        self.stale = True

    def invert_xaxis(self):
        """
        Invert x axis.
        """
        b_axis = self.get_axis(Location.BOTTOM)
        t_axis = self.get_axis(Location.TOP)
        b_axis.setInverse(not b_axis.isInverse())
        t_axis.setInverse(not t_axis.isInverse())
        self.stale = True

    def invert_yaxis(self):
        """
        Invert y axis.
        """
        l_axis = self.get_axis(Location.LEFT)
        r_axis = self.get_axis(Location.RIGHT)
        l_axis.setInverse(not l_axis.isInverse())
        r_axis.setInverse(not r_axis.isInverse())
        self.stale = True

    def minorticks_on(self):
        """
        Display minor ticks on the Axes.

        Displaying minor ticks may reduce performance; you may turn them off
        using `minorticks_off()` if drawing speed is a problem.
        """
        self._axes.getAxis(Location.BOTTOM).setMinorTickVisible(True)
        self._axes.getAxis(Location.TOP).setMinorTickVisible(True)
        self._axes.getAxis(Location.LEFT).setMinorTickVisible(True)
        self._axes.getAxis(Location.RIGHT).setMinorTickVisible(True)

    def minorticks_off(self):
        """
        Remove minor ticks from the Axes.
        """
        self._axes.getAxis(Location.BOTTOM).setMinorTickVisible(False)
        self._axes.getAxis(Location.TOP).setMinorTickVisible(False)
        self._axes.getAxis(Location.LEFT).setMinorTickVisible(False)
        self._axes.getAxis(Location.RIGHT).setMinorTickVisible(False)

    def add_patch(self, patch):
        """
        Add a patch.

        :param patch: (*Graphic or list of graphics*) The patch(s) to be added.
        """
        if isinstance(patch, (list, tuple)):
            self._axes.addGraphics(patch)
        else:
            self._axes.addGraphic(patch)
        self._axes.setAutoExtent()
        self.stale = True

    def add_graphic(self, graphic, zorder=None):
        """
        Add a graphic
        
        :param graphic: (*Graphic*) The graphic to be added.
        :param transform: (*Transform*) The transform. Default is `None`.
        :param zorder: (*int*) Z order of the graphic. Default is `None` that the graphic added
            to the end.
        """
        if isinstance(graphic, Artist):
            graphic.axes = self

        if not zorder is None:
            if zorder > self.num_graphics():
                zorder = self.num_graphics()

        if zorder is None:
            rGraphic = self._axes.addGraphic(graphic)
        else:
            rGraphic = self._axes.addGraphic(zorder, graphic)

        if self.num_graphics() <= 1:
            self._axes.setAutoExtent()

        self.stale = True
        return rGraphic

    def get_graphics(self):
        """
        Get graphics
        :return: (*list*) The graphics
        """
        return self._axes.getGraphics()

    def num_graphics(self):
        """
        Get the number of graphics
        :return: (*int*) The number of graphics
        """
        return self._axes.getGraphicNumber()

    def remove_graphic(self, graphic):
        """
        Remove a graphic.

        :param graphic: (*int or Graphic*) The graphic.
        """
        if isinstance(graphic, int):
            if graphic < 0:
                graphic = self._axes.getGraphicNumber() + graphic

        self._axes.getGraphics().remove(graphic)
        self.stale = True

    def remove(self):
        """
        Remove all graphics.
        """
        self._axes.getGraphics().clear()
        self.stale = True

    def cll(self):
        """
        Remove last graphic.
        """
        self._axes.removeLastGraphic()
        self.stale = True

    def clear(self):
        """
        Remove all graphics.
        """
        self._axes.getGraphics().clear()

    def data2pixel(self, x, y, z=None):
        """
        Transform data coordinate to screen coordinate
        
        :param x: (*float*) X coordinate.
        :param y: (*float*) Y coordinate.
        :param z: (*float*) Z coordinate - only used for 3-D axes.
        """
        r = self._axes.project(x, y, z)
        x = r.x
        y = r.y
        rect = self._axes.getPositionArea()
        r = self._axes.projToScreen(x, y, rect)
        sx = r[0] + rect.getX()
        sy = r[1] + rect.getY()
        sy = self._figure.get_size()[1] - sy
        return sx, sy

    def grid(self, b=None, **kwargs):
        """
        Turn the axes grids on or off.

        :param b: If b is *None* and *len(kwargs)==0* , toggle the grid state. If *kwargs*
            are supplied, it is assumed that you want a grid and *b* is thus set to *True* .
        :param kwargs: *kwargs* are used to set the grid line properties.
        """
        gridline = self._axes.getGridLine()
        if b is None and len(kwargs) == 0:
            b = not gridline.isDrawZLine()

        axis = kwargs.pop('axis', 'all')
        if b is not None:
            if axis == 'all':
                gridline.setDrawXLine(b)
                gridline.setDrawYLine(b)
                gridline.setDrawZLine(b)
            elif axis == 'x':
                gridline.setDrawXLine(b)
            elif axis == 'y':
                gridline.setDrawYLine(b)
            elif axis == 'z':
                gridline.setDrawZLine(b)

        color = kwargs.pop('color', None)
        if not color is None:
            c = plotutil.getcolor(color)
            gridline.setColor(c)
        alpha = kwargs.pop('alpha', None)
        if alpha is not None:
            gridline.setAlpha(float(alpha))
        linewidth = kwargs.pop('linewidth', None)
        if not linewidth is None:
            gridline.setSize(linewidth)
        linestyle = kwargs.pop('linestyle', None)
        if not linestyle is None:
            linestyle = plotutil.getlinestyle(linestyle)
            gridline.setStyle(linestyle)

        self.stale = True

    def plot(self, *args, **kwargs):
        """
        Plot lines and/or markers to the axes. *args* is a variable length argument, allowing
        for multiple *x, y* pairs with an optional format string.
        
        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param style: (*string*) Line style for plot.
        :param linewidth: (*float*) Line width.
        :param color: (*Color*) Line color.
        
        :returns: lines.
        
        The following format string characters are accepted to control the line style or marker:
        
          =========  ===========
          Character  Description
          =========  ===========
          '-'         solid line style
          '--'        dashed line style
          '-.'        dash-dot line style
          ':'         dotted line style
          '.'         point marker
          ','         pixel marker
          'o'         circle marker
          'v'         triangle_down marker
          '^'         triangle_up marker
          '<'         triangle_left marker
          '>'         triangle_right marker
          's'         square marker
          'p'         pentagon marker
          '*'         star marker
          'x'         x marker
          'D'         diamond marker
          =========  ===========
          
        The following color abbreviations are supported:
          
          =========  =====
          Character  Color  
          =========  =====
          'b'        blue
          'g'        green
          'r'        red
          'c'        cyan
          'm'        magenta
          'y'        yellow
          'k'        black
          =========  =====
        """
        xdatalist = []
        ydatalist = []
        styles = []
        xaxistype = None
        isxylistdata = False
        if len(args) == 1:
            ydata = np.array(args[0])
            if isinstance(ydata, DimArray):
                xdata = ydata.dimvalue(0)
                if ydata.ndim == 2:
                    xdata = xdata[:,np.newaxis].repeat(ydata.shape[1], axis=1)
            else:
                xdata = np.arange(ydata.shape[0])
                if ydata.ndim == 2:
                    xdata = xdata[:,np.newaxis].repeat(ydata.shape[1], axis=1)
            xdatalist.append(xdata)
            ydatalist.append(ydata)
        elif len(args) == 2:
            if isinstance(args[1], basestring):
                ydata = np.array(args[0])
                if isinstance(ydata, DimArray):
                    xdata = ydata.dimvalue(0)
                    if ydata.ndim == 2:
                        xdata = xdata[:,np.newaxis].repeat(ydata.shape[1], axis=1)
                else:
                    xdata = np.arange(ydata.shape[0])
                    if ydata.ndim == 2:
                        xdata = xdata[:,np.newaxis].repeat(ydata.shape[1], axis=1)
                styles.append(args[1])
            else:
                xdata = args[0]
                ydata = args[1]
            xdatalist.append(xdata)
            ydatalist.append(ydata)
        else:
            c = 'x'
            for arg in args:
                if c == 'x':
                    xdatalist.append(arg)
                    c = 'y'
                elif c == 'y':
                    ydatalist.append(arg)
                    c = 's'
                elif c == 's':
                    if isinstance(arg, basestring):
                        styles.append(arg)
                        c = 'x'
                    else:
                        styles.append(None)
                        xdatalist.append(arg)
                        c = 'y'

        snum = len(xdatalist)
        if len(styles) == 0:
            styles = None
        else:
            while len(styles) < snum:
                styles.append(None)

        # Set plot data styles
        zvalues = kwargs.pop('zvalues', None)
        cdata = kwargs.pop('cdata', zvalues)
        alpha = kwargs['alpha'] if kwargs.has_key('alpha') else None
        if cdata is None:
            lines = []
            legend = kwargs.pop('legend', None)
            if not legend is None:
                if isinstance(legend, list):
                    lines = legend
                else:
                    lines = legend.getLegendBreaks()
            else:
                if not styles is None:
                    colors = plotutil.makecolors(len(styles), alpha=alpha)
                    for i in range(0, len(styles)):
                        label = kwargs.pop('label', 'S_' + str(i + 1))
                        if styles[i] is None:
                            line = plotutil.getlegendbreak('line', **kwargs)[0]
                            line.setCaption(label)
                            line.setColor(colors[i])
                        else:
                            line = plotutil.getplotstyle(styles[i], label, **kwargs)
                        lines.append(line)
                else:
                    if kwargs.has_key('colors'):
                        colors = kwargs['colors']
                        snum = len(colors) if len(colors) > snum else snum
                    else:
                        if kwargs.has_key('color'):
                            color = kwargs['color']
                            color = plotutil.getcolor(color, alpha=alpha)
                            colors = [color] * snum
                        else:
                            colors = plotutil.makecolors(snum, alpha=alpha)

                    for i in range(0, snum):
                        label = kwargs.pop('label', 'S_' + str(i + 1))
                        line = plotutil.getlegendbreak('line', **kwargs)[0]
                        line.setCaption(label)
                        if i < len(colors):
                            line.setColor(plotutil.getcolor(colors[i], alpha=alpha))
                        lines.append(line)
        else:
            ls = kwargs.pop('symbolspec', None)
            if ls is None:
                if isinstance(cdata, (list, tuple)):
                    cdata = np.array(cdata)
                levels = kwargs.pop('levs', None)
                levels = kwargs.pop('levels', levels)
                if levels is None:
                    cnum = kwargs.pop('cnum', None)
                    if cnum is None:
                        ls = plotutil.getlegendscheme([], cdata.min(), cdata.max(), **kwargs)
                    else:
                        ls = plotutil.getlegendscheme([cnum], cdata.min(), cdata.max(), **kwargs)
                else:
                    ls = plotutil.getlegendscheme([levels], cdata.min(), cdata.max(), **kwargs)
                ls = plotutil.setlegendscheme_line(ls, **kwargs)

        # Add graphics
        iscurve = kwargs.pop('curve', False)
        if cdata is None:
            # Add data series
            snum = len(xdatalist)
            if snum == 1:
                xdata = np.asarray(xdatalist[0])
                ydata = np.asarray(ydatalist[0])
                if ydata.ndim == 1:
                    if xdata.contains_nan():
                        xdata = np.split_array_by_nan(xdata)
                        ydata = np.split_array_by_nan(ydata)
                        if kwargs.has_key('color'):
                            kwargs['colors'] = kwargs['color']
                        if not kwargs.has_key('cmap'):
                            kwargs['cmap'] = 'matlab_jet'
                        graphics = LineCollection(None, xyz_data=[xdata, ydata], **kwargs)
                    else:
                        graphics = Line(xdata, ydata, legend=lines[0], curve=iscurve)
                else:
                    if kwargs.has_key('color'):
                        kwargs['colors'] = kwargs['color']
                    if not kwargs.has_key('cmap'):
                        kwargs['cmap'] = 'matlab_jet'
                    graphics = LineCollection(None, xyz_data=[xdata, ydata], **kwargs)
            else:
                graphics = []
                for i in range(0, snum):
                    label = kwargs.pop('label', 'S_' + str(i + 1))
                    xdata = np.asarray(xdatalist[i])
                    ydata = np.asarray(ydatalist[i])
                    graphic = Line(xdata, ydata, legend=lines[i], curve=iscurve)
                    graphics.append(graphic)
        else:
            xdata = np.asarray(xdatalist[0])
            ydata = np.asarray(ydatalist[0])
            cdata = np.asarray(cdata)
            if ydata.ndim == 1:
                if xdata.contains_nan():
                    xdata = np.split_array_by_nan(xdata)
                    ydata = np.split_array_by_nan(ydata)
                    cdata = np.split_array_by_nan(cdata)
                    graphics = LineCollection(None, xyz_data=[xdata, ydata], cdata=cdata, legend=ls, **kwargs)
                else:
                    graphics = Line(xdata, ydata, legend=ls, cdata=cdata, curve=iscurve)
            else:
                graphics = LineCollection(None, xyz_data=[xdata, ydata], cdata=cdata, legend=ls, **kwargs)

        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            if isinstance(graphics, list):
                for graphic in graphics:
                    graphic.setAntiAlias(antialias)
            else:
                graphics.setAntiAlias(antialias)

        zorder = kwargs.pop('zorder', None)
        if isinstance(graphics, list):
            for graphic in graphics:
                self.add_graphic(graphic, zorder=zorder)
        else:
            self.add_graphic(graphics, zorder=zorder)
        self._axes.setAutoExtent()

        return graphics

    def step(self, x, y, *args, **kwargs):
        """
        Make a step plot.
        
        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param style: (*string*) Line style for plot.
        :param label: (*string*) Step line label.
        :param where: (*string*) ['pre' | 'post' | 'mid']. If 'pre' (the default), the interval 
            from x[i] to x[i+1] has level y[i+1]. If 'post', that interval has level y[i].
            If ‘mid’, the jumps in y occur half-way between the x-values.
        
        :returns: Step lines
        """
        label = kwargs.pop('label', 'S_0')
        xdata = plotutil.getplotdata(x)
        ydata = plotutil.getplotdata(y)
        if len(args) > 0:
            fmt = args[0]
            fmt = plotutil.getplotstyle(fmt, label, **kwargs)
        else:
            fmt = plotutil.getlegendbreak('line', **kwargs)[0]
            fmt.setCaption(label)
        where = kwargs.pop('where', 'pre')

        # Create graphics
        graphics = GraphicFactory.createStepLineString(xdata, ydata, fmt, where)
        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            graphics.setAntiAlias(antialias)

        zorder = kwargs.pop('zorder', None)
        self.add_graphic(graphics, zorder=zorder)
        self._axes.setAutoExtent()

        return graphics

    def scatter(self, *args, **kwargs):
        """
        Make a scatter plot of x vs y, where x and y are sequence like objects of the same lengths.
        
        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param s: (*int*) Size of points.
        :param c: (*Color or array*) Color of the points. Or z values.
        :param alpha: (*int*) The alpha blending value, between 0 (transparent) and 1 (opaque).
        :param marker: (*string*) Marker of the points.
        :param label: (*string*) Label of the point series.
        :param levels: (*array_like*) Optional. A list of floating point numbers indicating the level
            points to draw, in increasing order.
        
        :returns: Points legend break.
        """
        n = len(args)
        if n == 1:
            a = args[0]
            y = a.dimvalue(0)
            x = a.dimvalue(1)
        else:
            x = np.asarray(args[0])
            y = np.asarray(args[1])

        s = kwargs.pop('s', 8)
        c = kwargs.pop('c', None)

        # Add data series
        label = kwargs.pop('label', 'S_0')

        # Set plot data styles
        pb, isunique = plotutil.getlegendbreak('point', **kwargs)
        if c is None:
            c = pb.getColor()
            if c is None:
                c = 'b'
        pb.setCaption(label)
        is_value = False

        if isinstance(c, NDArray):
            if x.size == c.size:
                is_value = True
        elif isinstance(c, (list, tuple)):
            if len(x) == len(c) and isinstance(c[0], (int, long, float)):
                is_value = True

        if is_value:
            ls = kwargs.pop('symbolspec', None)
            if ls is None:
                if isinstance(c, (list, tuple)):
                    c = np.array(c)
                levels = kwargs.pop('levs', None)
                if levels is None:
                    levels = kwargs.pop('levels', None)
                if levels is None:
                    cnum = kwargs.pop('cnum', None)
                    if cnum is None:
                        ls = plotutil.getlegendscheme([], c.min(), c.max(), **kwargs)
                    else:
                        ls = plotutil.getlegendscheme([cnum], c.min(), c.max(), **kwargs)
                else:
                    ls = plotutil.getlegendscheme([levels], c.min(), c.max(), **kwargs)
                ls = plotutil.setlegendscheme_point(ls, **kwargs)
                if isinstance(s, int):
                    for lb in ls.getLegendBreaks():
                        lb.setSize(s)
                else:
                    n = len(s)
                    for i in range(0, n):
                        ls.getLegendBreaks()[i].setSize(s[i])
            # Create graphics
            graphics = PointCollection(x, y, cdata=c, legend=ls)
        else:
            alpha = kwargs.pop('alpha', None)
            colors = plotutil.getcolors(c, alpha)
            edgecolors = kwargs.pop('edgecolors', pb.getOutlineColor())
            if edgecolors is None:
                edgecolors = [Color.black]
            pbs = []
            if isinstance(s, int):
                s = [s]
            n = max(len(s), len(colors))
            if isinstance(edgecolors, Color):
                edgecolors = [edgecolors]
            if n == 1:
                n = len(edgecolors)
            if n == 1:
                pb.setSize(s[0])
                pb.setColor(colors[0])
                pb.setOutlineColor(edgecolors[0])
                pbs.append(pb)
            else:
                if len(s) == 1:
                    s = s * n
                if len(colors) == 1:
                    colors = colors * n
                if len(edgecolors) == 1:
                    edgecolors = edgecolors * n
                for i in range(n):
                    kwargs['size'] = s[i]
                    kwargs['color'] = colors[i]
                    kwargs['edgecolor'] = edgecolors[i]
                    npb = pb.clone()
                    plotutil.setpointlegendbreak(npb, **kwargs)
                    pbs.append(npb)
            # Create graphics
            graphics = PointCollection(x, y, legend=pbs)

        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            graphics.setAntiAlias(antialias)

        zorder = kwargs.pop('zorder', None)
        self.add_graphic(graphics, zorder=zorder)
        self._axes.setAutoExtent()

        return graphics

    def semilogy(self, *args, **kwargs):
        """
        Make a plot with log scaling on the y axis.
        
        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param style: (*string*) Line style for plot.
        
        :returns: Legend breaks of the lines.
        
        The following format string characters are accepted to control the line style or marker:
        
          =========  ===========
          Character  Description
          =========  ===========
          '-'         solid line style
          '--'        dashed line style
          '-.'        dash-dot line style
          ':'         dotted line style
          '.'         point marker
          ','         pixel marker
          'o'         circle marker
          'v'         triangle_down marker
          '^'         triangle_up marker
          '<'         triangle_left marker
          '>'         triangle_right marker
          's'         square marker
          'p'         pentagon marker
          '*'         star marker
          'x'         x marker
          'D'         diamond marker
          =========  ===========
          
        The following color abbreviations are supported:
          
          =========  =====
          Character  Color  
          =========  =====
          'b'        blue
          'g'        green
          'r'        red
          'c'        cyan
          'm'        magenta
          'y'        yellow
          'k'        black
          =========  =====
        """
        lines = self.plot(*args, **kwargs)
        self.set_yaxis_type('log')
        self._axes.setAutoExtent()
        return lines

    def semilogx(self, *args, **kwargs):
        """
        Make a plot with log scaling on the x axis.
        
        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param style: (*string*) Line style for plot.
        
        :returns: Legend breaks of the lines.
        
        The following format string characters are accepted to control the line style or marker:
        
          =========  ===========
          Character  Description
          =========  ===========
          '-'         solid line style
          '--'        dashed line style
          '-.'        dash-dot line style
          ':'         dotted line style
          '.'         point marker
          ','         pixel marker
          'o'         circle marker
          'v'         triangle_down marker
          '^'         triangle_up marker
          '<'         triangle_left marker
          '>'         triangle_right marker
          's'         square marker
          'p'         pentagon marker
          '*'         star marker
          'x'         x marker
          'D'         diamond marker
          =========  ===========
          
        The following color abbreviations are supported:
          
          =========  =====
          Character  Color  
          =========  =====
          'b'        blue
          'g'        green
          'r'        red
          'c'        cyan
          'm'        magenta
          'y'        yellow
          'k'        black
          =========  =====
        """
        lines = self.plot(*args, **kwargs)
        self.set_xaxis_type('log')
        self._axes.setAutoExtent()
        return lines

    def loglog(self, *args, **kwargs):
        """
        Make a plot with log scaling on both x and y axis.
        
        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param style: (*string*) Line style for plot.
        
        :returns: Legend breaks of the lines.
        
        The following format string characters are accepted to control the line style or marker:
        
          =========  ===========
          Character  Description
          =========  ===========
          '-'         solid line style
          '--'        dashed line style
          '-.'        dash-dot line style
          ':'         dotted line style
          '.'         point marker
          ','         pixel marker
          'o'         circle marker
          'v'         triangle_down marker
          '^'         triangle_up marker
          '<'         triangle_left marker
          '>'         triangle_right marker
          's'         square marker
          'p'         pentagon marker
          '*'         star marker
          'x'         x marker
          'D'         diamond marker
          =========  ===========
          
        The following color abbreviations are supported:
          
          =========  =====
          Character  Color  
          =========  =====
          'b'        blue
          'g'        green
          'r'        red
          'c'        cyan
          'm'        magenta
          'y'        yellow
          'k'        black
          =========  =====
        """
        lines = self.plot(*args, **kwargs)
        self.set_xaxis_type('log')
        self.set_yaxis_type('log')
        self._axes.setAutoExtent()
        return lines

    def errorbar(self, x, y, yerr=None, xerr=None, fmt='', ecolor=None, elinewidth=None, capsize=None,
                 **kwargs):
        """
        Plot an errorbar graph.
        
        :param x: (*array_like*) X data.
        :param y: (*array_like*) Y data.
        :param yerr: (*scalar or array_like*) Y error values.
        :param xerr: (*scalar or array_like*) X error values.
        :param fmt: (*string*) Plot format string.
        :param ecolor: (*color*) Error bar color.
        :param elinewidth: (*float*) Error bar line width.
        :param capsize: (*float*) The length of the error bar caps.

        :returns: Error bar lines.
        """
        # Add data series
        label = kwargs.pop('label', 'S_0')
        xdata = plotutil.getplotdata(x)
        ydata = plotutil.getplotdata(y)
        if not yerr is None:
            if isinstance(yerr, (int, float)):
                ye = []
                for i in range(xdata.getSize()):
                    ye.append(yerr)
                yerrB = np.array(ye)._array
                yerrU = yerrB
            else:
                if isinstance(yerr, (list, tuple)):
                    if isinstance(yerr[0], numbers.Number):
                        yerrB = plotutil.getplotdata(yerr)
                        yerrU = yerrB
                    else:
                        yerrB = plotutil.getplotdata(yerr[0])
                        yerrU = plotutil.getplotdata(yerr[1])
                elif yerr.ndim == 2:
                    yerrB = yerr[0, :].asarray()
                    yerrU = yerr[1, :].asarray()
                else:
                    yerrB = plotutil.getplotdata(yerr)
                    yerrU = yerrB
        else:
            yerrB = None
            yerrU = None

        if not xerr is None:
            if isinstance(xerr, (int, float)):
                ye = []
                for i in range(xdata.getSize()):
                    ye.append(xerr)
                xerrL = np.array(ye)._array
                xerrR = xerrL
            else:
                if isinstance(xerr, (list, tuple)):
                    xerrL = plotutil.getplotdata(xerr[0])
                    xerrR = plotutil.getplotdata(xerr[1])
                elif xerr.ndim == 2:
                    xerrL = xerr[0, :].asarray()
                    xerrR = xerr[1, :].asarray()
                else:
                    xerrL = plotutil.getplotdata(xerr)
                    xerrR = xerrL
        else:
            xerrL = None
            xerrR = None

        # Get plot data style
        if fmt == '':
            line = plotutil.getlegendbreak('line', **kwargs)[0]
            line.setCaption(label)
        else:
            line = plotutil.getplotstyle(fmt, label, **kwargs)
        if isinstance(line, PolylineBreak):
            eline = line.clone()
            eline.setDrawPolyline(True)
        else:
            eline = PolylineBreak()
        eline.setDrawSymbol(False)
        eline.setStyle(LineStyles.SOLID)
        if not ecolor is None:
            ecolor = plotutil.getcolor(ecolor)
            eline.setColor(ecolor)
        if not elinewidth is None:
            eline.setSize(elinewidth)

        # Create graphics
        if capsize is None:
            capsize = 10
        graphics = GraphicFactory.createErrorLineString(xdata, ydata, xerrL, xerrR, yerrB, \
                                                        yerrU, line, eline, capsize)
        zorder = kwargs.pop('zorder', None)
        self.add_graphic(graphics, zorder=zorder)
        self._axes.setAutoExtent()

        return graphics

    def bar(self, x, height, width=0.8, bottom=None, align='center', data=None, **kwargs):
        """
        Make a bar plot.
        
        The bars are positioned at x with the given alignment. Their dimensions are given by 
        width and height. The vertical baseline is bottom (default 0).
        
        :param x: (*array_like*) The x coordinates of the bars. See also align for the alignment 
            of the bars to the coordinates.
        :param height: (*array_like*) The height of the bars.
        :param width: (*array_like*) Optional, the widths of the bars default: 0.8.
        :param bottom: (*array_like*) Optional, the y coordinates of the bars default: None
        :param color: (*Color*) Optional, the color of the bar faces.
        :param edgecolor: (*Color*) Optional, the color of the bar edge. Default is black color.
            Edge line will not be plotted if ``edgecolor`` is ``None``.
        :param linewidth: (*int*) Optional, width of bar edge.
        :param label: (*string*) Label of the bar series.
        :param hatch: (*string*) Hatch string.
        :param hatchsize: (*int*) Hatch size. Default is None (8).
        :param bgcolor: (*Color*) Background color, only valid with hatch.
        :param barswidth: (*float*) Bars width (0 - 1), only used for automatic bar with plot
            (only one argument without ``width`` argument). Default is 0.8.
        :param ecolor: (*Color*) The line color of the errorbars. Default is black.
        :param elinewidth: (*float*) The line width of the errorbars.
        :param capsize: (*float*) The length of the error bar caps in pixels. Default is 0.
        :param morepoints: (*boolean*) More points in bar rectangle. Default is False.
        
        :returns: Bar legend break.
        
        
        The following format string characters are accepted to control the hatch style:
          =========  ===========
          Character  Description
          =========  ===========
          '-'         horizontal hatch style
          '|'         vertical hatch style
          '\\'        forward_diagonal hatch style
          '/'         backward_diagonal hatch style
          '+'         cross hatch style
          'x'         diagonal_cross hatch style
          '.'         dot hatch style
          =========  ===========
          
        """
        # Add data series
        label = kwargs.pop('label', 'S_0')
        autowidth = False
        isdate = False
        if isinstance(x, (list, tuple)):
            if isinstance(x[0], datetime.datetime):
                isdate = True
        x = np.asarray(x)
        x = x.astype('double')
        height = np.asarray(height)
        if isdate and width <= 1:
            width = (x[1] - x[0]) * width
        width = np.asarray(width)
        if align == 'center':
            x = x - width / 2

        yerr = kwargs.pop('yerr', None)
        if not yerr is None:
            if not isinstance(yerr, (int, float)):
                yerr = plotutil.getplotdata(yerr)
        if not bottom is None:
            bottom = plotutil.getplotdata(bottom)

        # Set plot data styles
        fcobj = kwargs.pop('color', None)
        if fcobj is None:
            fcobj = kwargs.pop('facecolor', None)
        if isinstance(fcobj, (tuple, list)):
            colors = plotutil.getcolors(fcobj)
        else:
            color = plotutil.getcolor(fcobj)
            colors = [color]
        ecobj = kwargs.pop('edgecolor', 'k')
        edgecolor = plotutil.getcolor(ecobj)
        linewidth = kwargs.pop('linewidth', 1.0)
        hatch = kwargs.pop('hatch', None)
        hatch = plotutil.gethatch(hatch)
        hatchsize = kwargs.pop('hatchsize', None)
        bgcolor = kwargs.pop('bgcolor', None)
        bgcolor = plotutil.getcolor(bgcolor)
        ecolor = kwargs.pop('ecolor', 'k')
        ecolor = plotutil.getcolor(ecolor)
        elinewidth = kwargs.pop('elinewidth', None)
        capsize = kwargs.pop('capsize', None)
        morepoints = kwargs.pop('morepoints', False)
        barbreaks = []
        for color in colors:
            lb = BarBreak()
            lb.setCaption(label)
            if color is not None:
                lb.setColor(color)
                lb.setDrawFill(True)
            if edgecolor is None:
                lb.setDrawOutline(False)
            else:
                lb.setOutlineColor(edgecolor)
            lb.setOutlineSize(linewidth)
            if not hatch is None:
                lb.setStyle(hatch)
                if not bgcolor is None:
                    lb.setBackColor(bgcolor)
                if not hatchsize is None:
                    lb.setStyleSize(hatchsize)
            lb.setErrorColor(ecolor)
            if not elinewidth is None:
                lb.setErrorSize(elinewidth)
            if not capsize is None:
                lb.setCapSize(capsize)
            barbreaks.append(lb)

        # Create bar graphics
        if isinstance(width, NDArray):
            width = width.asarray()
        if morepoints:
            graphics = GraphicFactory.createBars1(x.asarray(), height.asarray(), autowidth, width, not yerr is None,
                                                  yerr, \
                                                  not bottom is None, bottom, barbreaks)
        else:
            graphics = GraphicFactory.createBars(x.asarray(), height.asarray(), autowidth, width, not yerr is None,
                                                 yerr, \
                                                 not bottom is None, bottom, barbreaks)

        zorder = kwargs.pop('zorder', None)
        self.add_graphic(graphics, zorder=zorder)
        if autowidth:
            barswidth = kwargs.pop('barswidth', 0.8)
            self._axes.setBarsWidth(barswidth)
        self._axes.setAutoExtent()

        return graphics

    def barh(self, *args, **kwargs):
        """
        Make a horizontal bar plot.
        
        Make a bar plot with rectangles bounded by:
            left, left + width, y, y + height
        
        :param y: (*array_like*) The y coordinates of the bars.
        :param width: (*array_like*) The widths of the bars.
        :param height: (*array_like*) Optional, the height of the bars default: 0.8.
        :param left: (*array_like*) Optional, the x coordinates of the bars default: None
        :param color: (*Color*) Optional, the color of the bar faces.
        :param edgecolor: (*Color*) Optional, the color of the bar edge. Default is black color.
            Edge line will not be plotted if ``edgecolor`` is ``None``.
        :param linewidth: (*int*) Optional, width of bar edge.
        :param label: (*string*) Label of the bar series.
        :param hatch: (*string*) Hatch string.
        :param hatchsize: (*int*) Hatch size. Default is None (8).
        :param bgcolor: (*Color*) Background color, only valid with hatch.
        :param barswidth: (*float*) Bars width (0 - 1), only used for automatic bar with plot
            (only one argument without ``width`` augment). Default is 0.8.
        :param morepoints: (*boolean*) More points in bar rectangle. Default is False.
        
        :returns: Bar legend break.
        
        
        The following format string characters are accepted to control the hatch style:
          =========  ===========
          Character  Description
          =========  ===========
          '-'         horizontal hatch style
          '|'         vertical hatch style
          '\\'        forward_diagonal hatch style
          '/'         backward_diagonal hatch style
          '+'         cross hatch style
          'x'         diagonal_cross hatch style
          '.'         dot hatch style
          =========  ===========
          
        """
        # Add data series
        label = kwargs.pop('label', 'S_0')
        xdata = None
        autoheight = True
        height = 0.8
        if len(args) == 1:
            xdata = args[0]
        elif len(args) == 2:
            if isinstance(args[1], (int, float)):
                xdata = args[0]
                height = args[1]
                autoheight = False
            else:
                ydata = args[0]
                xdata = args[1]
        else:
            ydata = args[0]
            xdata = args[1]
            height = args[2]
            autoheight = False

        if ydata is None:
            ydata = []
            for i in range(1, len(args[0]) + 1):
                ydata.append(i)
        ydata = plotutil.getplotdata(ydata)
        xdata = plotutil.getplotdata(xdata)
        height = plotutil.getplotdata(height)
        xerr = kwargs.pop('xerr', None)
        if not xerr is None:
            if not isinstance(xerr, (int, float)):
                xerr = plotutil.getplotdata(xerr)
        left = kwargs.pop('left', None)
        if not left is None:
            left = plotutil.getplotdata(left)

        # Set plot data styles
        fcobj = kwargs.pop('color', None)
        if fcobj is None:
            fcobj = kwargs.pop('facecolor', None)
        if isinstance(fcobj, (tuple, list)):
            colors = plotutil.getcolors(fcobj)
        else:
            color = plotutil.getcolor(fcobj)
            colors = [color]
        ecobj = kwargs.pop('edgecolor', 'k')
        edgecolor = plotutil.getcolor(ecobj)
        linewidth = kwargs.pop('linewidth', 1.0)
        hatch = kwargs.pop('hatch', None)
        hatch = plotutil.gethatch(hatch)
        hatchsize = kwargs.pop('hatchsize', None)
        bgcolor = kwargs.pop('bgcolor', None)
        bgcolor = plotutil.getcolor(bgcolor)
        ecolor = kwargs.pop('ecolor', 'k')
        ecolor = plotutil.getcolor(ecolor)
        barbreaks = []
        for color in colors:
            lb = BarBreak()
            lb.setCaption(label)
            if color is not None:
                lb.setColor(color)
                lb.setDrawFill(True)
            if edgecolor is None:
                lb.setDrawOutline(False)
            else:
                lb.setOutlineColor(edgecolor)
            lb.setOutlineSize(linewidth)
            if not hatch is None:
                lb.setStyle(hatch)
                if not bgcolor is None:
                    lb.setBackColor(bgcolor)
                if not hatchsize is None:
                    lb.setStyleSize(hatchsize)
            lb.setErrorColor(ecolor)
            barbreaks.append(lb)

        # Create bar graphics
        graphics = GraphicFactory.createHBars(ydata, xdata, autoheight, height, not xerr is None, xerr, \
                                              not left is None, left, barbreaks)

        zorder = kwargs.pop('zorder', None)
        self.add_graphic(graphics, zorder=zorder)

        if autoheight:
            barsheight = kwargs.pop('barsheight', 0.8)
            self._axes.setBarsWidth(barsheight)
        self._axes.setAutoExtent()

        return graphics

    def hist(self, x, bins=10, density=False, cumulative=False,
             rwidth=None, orientation='vertical', **kwargs):
        """
        Plot a histogram.
        
        :param x: (*array_like*) Input values, this takes either a single array or a sequence of arrays
            which are not required to be of the same length.
        :param bins: (*int*) If an integer is given, bins + 1 bin edges are returned.
        :param density: (*bool*) Default is `False`. If `True`, draw and return a probability
            density: each bin will display the bin's raw count divided by the total number of
            counts and the bin width
        :param cumulative: (*bool*) Default is `False`. If `True`, then a histogram is computed
            where each bin gives the counts in that bin plus all bins for smaller values. The
            last bin gives the total number of datapoints.
        :param rwidth: (*float or None*) Default is `None`. The relative width of the bars as a
            fraction of the bin width. If None, automatically compute the width.
        :param orientation: (*str*) {'vertical', 'horizontal'}, default: 'vertical'. If
            'horizontal', barh will be used for bar-type histograms and the bottom kwarg will be
            the left edges.

        Returns
        -------
        n : array or list of arrays
            The values of the histogram bins. See *density* and *weights* for a
            description of the possible semantics.  If input *x* is an array,
            then this is an array of length *nbins*. If input is a sequence of
            arrays ``[data1, data2, ...]``, then this is a list of arrays with
            the values of the histograms for each of the arrays in the same
            order.  The dtype of the array *n* (or of its element arrays) will
            always be float even if no weighting or normalization is used.
        bins : array
            The edges of the bins. Length nbins + 1 (nbins left edges and right
            edge of last bin).  Always a single array even when multiple data
            sets are passed in.
        patches : `.BarContainer` or list of a single `.Polygon` or list of such objects
            Container of individual artists used to create the histogram
            or list of such containers if there are multiple input datasets.
        """
        if isinstance(x, NDArray) and x.ndim == 1:
            m, bins = np.histogram(x, bins=bins, density=density)
            width = np.diff(bins)
            if cumulative:
                m = np.cumsum(m * width)

            if rwidth is not None:
                width = width * rwidth

            if orientation == 'vertical':
                barbreaks = self.bar(bins[:-1], m, width, align='center', **kwargs)
            else:
                barbreaks = self.barh(bins[:-1], m, width, align='center', **kwargs)

            return m, bins, barbreaks
        else:
            mlist = []
            barbreaklist = []
            if isinstance(x, (list, tuple)):
                n = len(x)
            else:
                n = x.shape[1]

            colors = kwargs.pop('color', None)
            if colors is None:
                colors = plotutil.makecolors(n)
            labels = kwargs.pop('label', None)

            for i in range(n):
                if isinstance(x, NDArray):
                    xx = x[:,i]
                else:
                    xx = x[i]

                m, bins = np.histogram(xx, bins=bins, density=density)
                width = np.diff(bins) / n
                if cumulative:
                    m = np.cumsum(m * width)
                mlist.append(m)
                if rwidth is not None:
                    width = width * rwidth
                kwargs['color'] = colors[i]
                if labels is not None:
                    kwargs['label'] = labels[i]
                if orientation == 'vertical':
                    barbreaks = self.bar(bins[:-1] + width * i, m, width, align='center', **kwargs)
                else:
                    barbreaks = self.barh(bins[:-1] + width * i, m, width, align='center', **kwargs)
                barbreaklist.append(barbreaks)

            return mlist, bins, barbreaklist

    def stem(self, x, y, z, s=8, c='b', marker='o', alpha=None, linewidth=None, 
                verts=None, **kwargs):
        """
        Make a 3D scatter plot of x, y and z, where x, y and z are sequence like objects of the same lengths.
        
        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param z: (*array_like*) Input z data.
        :param s: (*int*) Size of points.
        :param c: (*Color*) Color of the points. Or z vlaues.
        :param alpha: (*int*) The alpha blending value, between 0 (transparent) and 1 (opaque).
        :param marker: (*string*) Marker of the points.
        :param label: (*string*) Label of the points series.
        :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level 
            points to draw, in increasing order.
        
        :returns: Stem graphics.
        """        
        #Add data series
        label = kwargs.pop('label', 'S_0')
        xdata = plotutil.getplotdata(x)
        ydata = plotutil.getplotdata(y)
        zdata = plotutil.getplotdata(z)
        
        #Set plot data styles
        pb, isunique = plotutil.getlegendbreak('point', **kwargs)
        pb.setCaption(label)
        pstyle = plotutil.getpointstyle(marker)    
        pb.setStyle(pstyle)
        bottom = kwargs.pop('bottom', 0)   
        samestemcolor = kwargs.pop('samestemcolor', False)
        isvalue = False
        if len(c) > 1:
            if isinstance(c, (NDArray, DimArray)):
                isvalue = True
            elif isinstance(c[0], (int, long, float)):
                isvalue = True            
        if isvalue:
            ls = kwargs.pop('symbolspec', None)
            if ls is None:        
                if isinstance(c, (list, tuple)):
                    c = np.array(c)
                levels = kwargs.pop('levs', None)
                if levels is None:
                    levels = kwargs.pop('levels', None)
                if levels is None:
                    cnum = kwargs.pop('cnum', None)
                    if cnum is None:
                        ls = plotutil.getlegendscheme([], c.min(), c.max(), **kwargs)
                    else:
                        ls = plotutil.getlegendscheme([cnum], c.min(), c.max(), **kwargs)
                else:
                    ls = plotutil.getlegendscheme([levels], c.min(), c.max(), **kwargs)
                ls = plotutil.setlegendscheme_point(ls, **kwargs)
                if isinstance(s, int):
                    for lb in ls.getLegendBreaks():
                        lb.setSize(s)
                else:
                    n = len(s)
                    for i in range(0, n):
                        ls.getLegendBreaks()[i].setSize(s[i])
            linefmt = kwargs.pop('linefmt', None)
            if linefmt is None:
                linefmt = PolylineBreak()
                linefmt.setColor(Color.black)
            else:
                linefmt = plotutil.getlegendbreak('line', **linefmt)[0]
            #Create graphics
            graphics = GraphicFactory.createStems3D(xdata, ydata, zdata, c.asarray(), \
                ls, linefmt, bottom, samestemcolor)
        else:
            colors = plotutil.getcolors(c, alpha)   
            pbs = []
            if isinstance(s, int):   
                pb.setSize(s)
                if len(colors) == 1:
                    pb.setColor(colors[0])
                    pb.setOutlineColor(colors[0])
                    pbs.append(pb)
                else:
                    n = len(colors)
                    for i in range(0, n):
                        npb = pb.clone()
                        npb.setColor(colors[i])
                        npb.setOutlineColor(colors[i])
                        pbs.append(npb)
            else:
                n = len(s)
                if len(colors) == 1:
                    pb.setColor(colors[0])
                    pb.setOutlineColor(colors[0])
                    for i in range(0, n):
                        npb = pb.clone()
                        npb.setSize(s[i])
                        pbs.append(npb)
                else:
                    for i in range(0, n):
                        npb = pb.clone()
                        npb.setSize(s[i])
                        npb.setColor(colors[i])
                        npb.setOutlineColor(colors[i])
                        pbs.append(npb)
            linefmt = kwargs.pop('linefmt', None)
            if linefmt is None:
                linefmt = PolylineBreak()
                linefmt.setColor(colors[0])
            else:
                linefmt = plotutil.getlegendbreak('line', **linefmt)[0]
            #Create graphics
            graphics = GraphicFactory.createStems3D(xdata, ydata, zdata, pbs, linefmt, \
                bottom, samestemcolor)
        
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics[0])
            self.add_graphic(graphics[1])
        return graphics[0], graphics[1]

    def contour(self, *args, **kwargs):
        """
        Plot contours.
        
        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) 2-D z value array.
        :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level curves 
            to draw, in increasing order.
        :param cmap: (*string*) Color map string.
        :param colors: (*list*) If None (default), the colormap specified by cmap will be used. If a 
            string, like ‘r’ or ‘red’, all levels will be plotted in this color. If a tuple of matplotlib 
            color args (string, float, rgb, etc), different levels will be plotted in different colors in 
            the order specified.
        :param smooth: (*boolean*) Smooth contour lines or not.
        
        :returns: (*graphics*) Contour graphics created from array data.
        """
        n = len(args)
        cmap = plotutil.getcolormap(**kwargs)
        fill_value = kwargs.pop('fill_value', -9999.0)
        offset = kwargs.pop('offset', 0)
        if n <= 2:
            a = args[0]
            if isinstance(a, DimArray):
                y = a.dimvalue(0)
                x = a.dimvalue(1)
            else:
                x = np.arange(a.shape[1])
                y = np.arange(a.shape[0])
            args = args[1:]
        else:
            x = args[0]
            y = args[1]
            a = args[2]
            args = args[3:]

        if x.ndim == 2:
            x = x[0]
        if y.ndim == 2:
            y = y[:,0]

        if len(args) > 0:
            level_arg = args[0]
            if isinstance(level_arg, int):
                cn = level_arg
                ls = LegendManage.createLegendScheme(a.min(), a.max(), cn, cmap)
            else:
                if isinstance(level_arg, NDArray):
                    level_arg = level_arg.aslist()
                ls = LegendManage.createLegendScheme(a.min(), a.max(), level_arg, cmap)
        else:    
            ls = LegendManage.createLegendScheme(a.min(), a.max(), cmap)
        ls = ls.convertTo(GeometryType.LINESTRING)
        plotutil.setlegendscheme(ls, **kwargs)
        
        smooth = kwargs.pop('smooth', True)
        zdir = kwargs.pop('zdir', 'z')
        if zdir == 'xy':
            sepoint = kwargs.pop('sepoint', [0,0,1,1])
            igraphic = GraphicFactory.createContourLines(x.asarray(), y.asarray(), a.asarray(), offset, zdir, ls, smooth, \
                sepoint)
        else:
            igraphic = GraphicFactory.createContourLines(x.asarray(), y.asarray(), a.asarray(), offset, zdir, ls, smooth)

        lighting = kwargs.pop('lighting', None)
        if not lighting is None:
            igraphic.setUsingLight(lighting)

        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(igraphic)
        return igraphic

    def clabel(self, layer, **kwargs):
        """
        Add contour layer labels.
        
        :param layer: (*MILayer*) The contour layer.
        :param fontname, fontsize: The font augments.
        :param color: (*color*) The label color. Default is ``None``, the label color will be set as
            same as color of the line.
        :param dynamic: (*boolean*) Draw labels dynamic or not. Default is ``True``.
        :param drawshadow: (*boolean*) Draw shadow under labels or not.
        :param shadowcolor: (*color*) Shadow color.
        :param fieldname: (*string*) The field name used for label.
        :param xoffset: (*int*) X offset of the labels.
        :param yoffset: (int*) Y offset of the labels.
        :param avoidcoll: (*boolean*) Avoid labels collision or not.
        """
        color = kwargs.pop('color', None)
        gc = layer
        if isinstance(layer, MILayer):
            gc = layer._layer

        labelset = gc.getLabelSet()
        if isinstance(gc, MapLayer):
            fieldname = kwargs.pop('fieldname', labelset.getFieldName())
            if fieldname is None:
                fieldname = gc.getFieldName(0)
            labelset.setFieldName(fieldname)
        fontdic = kwargs.pop('font', None)
        if not fontdic is None:
            font = plotutil.getfont(fontdic)
            labelset.setLabelFont(font)
        else:
            font = plotutil.getfont_1(**kwargs)
            labelset.setLabelFont(font)
        if color is None:
            labelset.setColorByLegend(True)
        else:
            labelset.setColorByLegend(False)
            color = plotutil.getcolor(color)
            labelset.setLabelColor(color)

        dynamic = kwargs.pop('dynamic', gc.getShapeType() == GeometryType.LINESTRING)
        drawshadow = kwargs.pop('drawshadow', dynamic)
        shadowcolor= kwargs.pop('shadowcolor', Color.white if drawshadow else None)
        if shadowcolor is not None:
            labelset.setShadowColor(plotutil.getcolor(shadowcolor))
            drawshadow = True
        labelset.setDrawShadow(drawshadow)
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
        if dynamic:
            gc.addLabelsContourDynamic(gc.getExtent())
        else:
            gc.addLabels()

    def contourf(self, *args, **kwargs):
        """
        Plot filled contours.
        
        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) 2-D z value array.
        :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level curves 
            to draw, in increasing order.
        :param cmap: (*string*) Color map string.
        :param colors: (*list*) If None (default), the colormap specified by cmap will be used. If a 
            string, like ‘r’ or ‘red’, all levels will be plotted in this color. If a tuple of matplotlib 
            color args (string, float, rgb, etc), different levels will be plotted in different colors in 
            the order specified.
        :param smooth: (*boolean*) Smooth contour lines or not.
        
        :returns: (*VectoryLayer*) Contour VectoryLayer created from array data.
        """
        n = len(args)
        cmap = plotutil.getcolormap(**kwargs)
        fill_value = kwargs.pop('fill_value', -9999.0)
        offset = kwargs.pop('offset', 0)
        if n <= 2:
            a = args[0]
            if isinstance(a, DimArray):
                y = a.dimvalue(0)
                x = a.dimvalue(1)
            else:
                x = np.arange(a.shape[1])
                y = np.arange(a.shape[0])
            args = args[1:]
        else:
            x = args[0]
            y = args[1]
            a = args[2]
            args = args[3:]

        if x.ndim == 2:
            x = x[0]
        if y.ndim == 2:
            y = y[:,0]

        if len(args) > 0:
            level_arg = args[0]
            if isinstance(level_arg, int):
                cn = level_arg
                ls = LegendManage.createLegendScheme(a.min(), a.max(), cn, cmap)
            else:
                if isinstance(level_arg, NDArray):
                    level_arg = level_arg.aslist()
                ls = LegendManage.createLegendScheme(a.min(), a.max(), level_arg, cmap)
        else:    
            ls = LegendManage.createLegendScheme(a.min(), a.max(), cmap)
        ls = ls.convertTo(GeometryType.POLYGON)
        if not kwargs.has_key('edgecolor'):
            kwargs['edgecolor'] = None
        plotutil.setlegendscheme(ls, **kwargs)
        
        smooth = kwargs.pop('smooth', True)
        zdir = kwargs.pop('zdir', 'z')
        if zdir == 'xy':
            sepoint = kwargs.pop('sepoint', [0,0,1,1])
            igraphic = GraphicFactory.createContourPolygons(x.asarray(), y.asarray(), a.asarray(), offset, zdir, ls, smooth, \
                sepoint)
        else:
            igraphic = GraphicFactory.createContourPolygons(x.asarray(), y.asarray(), a.asarray(), offset, zdir, ls, smooth)

        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(igraphic)
        return igraphic

    def imshow(self, *args, **kwargs):
        """
        Display an image on the 3D axes.
        
        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) 2-D or 3-D (RGB) z value array.
        :param levels: (*array_like*) Optional. A list of floating point numbers indicating the level curves
            to draw, in increasing order.
        :param cmap: (*string*) Color map string.
        :param colors: (*list*) If None (default), the colormap specified by cmap will be used. If a 
            string, like ‘r’ or ‘red’, all levels will be plotted in this color. If a tuple of matplotlib 
            color args (string, float, rgb, etc.), different levels will be plotted in different colors in
            the order specified.
        
        :returns: (*RasterLayer*) RasterLayer created from array data.
        """
        n = len(args)
        cmap = plotutil.getcolormap(**kwargs)
        fill_value = kwargs.pop('fill_value', -9999.0)
        xaxistype = None
        isrgb = False
        if n <= 2:
            if isinstance(args[0], (list, tuple)):
                isrgb = True
                rgbdata = args[0]
                if isinstance(rgbdata[0], NDArray):
                    x = np.arange(0, rgbdata[0].shape[1])
                    y = np.arange(0, rgbdata[0].shape[0])
                else:
                    x = rgbdata[0].dimvalue(1)
                    y = rgbdata[0].dimvalue(0)
            elif args[0].ndim > 2:
                isrgb = True
                rgbdata = args[0]
                if isinstance(rgbdata, NDArray):
                    x = np.arange(0, rgbdata.shape[1])
                    y = np.arange(0, rgbdata.shape[0])
                else:
                    x = rgbdata.dimvalue(1)
                    y = rgbdata.dimvalue(0)
            else:
                arr = args[0]
                if isinstance(arr, DimArray):
                    x = arr.dimvalue(1)
                    y = arr.dimvalue(0)
                else:
                    x = np.arange(0, arr.shape[1])
                    y = np.arange(0, arr.shape[0])
                args = args[1:]
        elif n <=4:
            x = args[0]
            y = args[1]
            arr = args[2]
            if isinstance(arr, (list, tuple)):
                isrgb = True
                rgbdata = arr
            elif arr.ndim > 2:
                isrgb = True
                rgbdata = arr
            else:
                args = args[3:]   
        
        offset = kwargs.pop('offset', 0)
        zdir = kwargs.pop('zdir', 'z')
        interpolation = kwargs.pop('interpolation', None)
        if isrgb:
            if isinstance(rgbdata, (list, tuple)):
                rgbd = []
                for d in rgbdata:
                    rgbd.append(d.asarray())
                rgbdata = rgbd
            else:
                rgbdata = rgbdata.asarray()
            x = plotutil.getplotdata(x)
            y = plotutil.getplotdata(y)
            graphics = GraphicFactory.createImage(x, y, rgbdata, offset, zdir, interpolation)
            ls = None
        else:
            if len(args) > 0:
                level_arg = args[0]
                if isinstance(level_arg, int):
                    cn = level_arg
                    ls = LegendManage.createImageLegend(arr._array, cn, cmap)
                else:
                    if isinstance(level_arg, NDArray):
                        level_arg = level_arg.aslist()
                    ls = LegendManage.createImageLegend(arr._array, level_arg, cmap)
            else:
                ls = plotutil.getlegendscheme(args, arr.min(), arr.max(), **kwargs)
            ls = ls.convertTo(GeometryType.IMAGE)
            plotutil.setlegendscheme(ls, **kwargs)
            if zdir == 'xy':
                sepoint = kwargs.pop('sepoint', [0,0,1,1])
            else:
                sepoint = None
            graphics = GraphicFactory.createImage(arr._array, x._array, y._array, fill_value,
                                                  ls, offset, zdir, sepoint, interpolation)
                
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics

    def pcolor(self, *args, **kwargs):
        """
        Draw a pseudo color plot.
        
        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) 2-D z value array.
        :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level curves 
            to draw, in increasing order.
        :param cmap: (*string*) Color map string.
        :param colors: (*list*) If None (default), the colormap specified by cmap will be used. If a 
            string, like ‘r’ or ‘red’, all levels will be plotted in this color. If a tuple of matplotlib 
            color args (string, float, rgb, etc), different levels will be plotted in different colors in 
            the order specified.
        
        :returns: (*GraphicCollection*) Polygon graphic collection.
        """
        n = len(args)
        if n <= 2:
            a = args[0]
            if isinstance(a, DimArray):
                y = a.dimvalue(0)
                x = a.dimvalue(1)
            else:
                ny, nx = a.shape
                y = np.arange(ny)
                x = np.arange(nx)
            args = args[1:]
        else:
            x = args[0]
            y = args[1]
            a = args[2]
            args = args[3:]
        if a.ndim == 2 and x.ndim == 1:
            x, y = np.meshgrid(x, y)
        ls = plotutil.getlegendscheme(args, a.min(), a.max(), **kwargs)
        ls = ls.convertTo(GeometryType.POLYGON)
        if not kwargs.has_key('edgecolor'):
            kwargs['edgecolor'] = None
        plotutil.setlegendscheme(ls, **kwargs)

        graphics = GraphicFactory.createPColorPolygons(x.asarray(), y.asarray(), a.asarray(), ls)
        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            graphics.setAntiAlias(antialias)

        visible = kwargs.pop('visible', True)
        if visible:
            zorder = kwargs.pop('zorder', None)
            self.add_graphic(graphics, zorder=zorder)
            self.set_draw_extent(graphics.getExtent())

        return graphics

    def gridshow(self, *args, **kwargs):
        """
        Draw a grid plot.
        
        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) 2-D z value array.
        :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level curves 
            to draw, in increasing order.
        :param cmap: (*string*) Color map string.
        :param colors: (*list*) If None (default), the colormap specified by cmap will be used. If a 
            string, like ‘r’ or ‘red’, all levels will be plotted in this color. If a tuple of matplotlib 
            color args (string, float, rgb, etc), different levels will be plotted in different colors in 
            the order specified.
        :param fill_value: (*float*) Fill_value. Default is ``-9999.0``.
        
        :returns: (*GraphicCollection*) Polygon graphic collection.
        """
        fill_value = kwargs.pop('fill_value', -9999.0)
        n = len(args)
        if n <= 2:
            a = args[0]
            y = a.dimvalue(0)
            x = a.dimvalue(1)
            args = args[1:]
        else:
            x = args[0]
            y = args[1]
            a = args[2]
            args = args[3:]

        ls = plotutil.getlegendscheme(args, a.min(), a.max(), **kwargs)
        ls = ls.convertTo(GeometryType.POLYGON)
        plotutil.setlegendscheme(ls, **kwargs)
        graphics = GraphicFactory.createGridPolygons(x.asarray(), y.asarray(), a.asarray(), ls)
        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            graphics.setAntiAlias(antialias)

        visible = kwargs.pop('visible', True)
        if visible:
            zorder = kwargs.pop('zorder', None)
            self.add_graphic(graphics, zorder=zorder)
            self.set_draw_extent(graphics.getExtent())

        return graphics

    def text(self, x, y, z, s, zdir=None, **kwargs):
        """
        Add text to the plot. kwargs will be passed on to text, except for the zdir 
        keyword, which sets the direction to be used as the z direction.
        
        :param x: (*float*) X coordinate.
        :param y: (*float*) Y coordinate.
        :param z: (*float*) Z coordinate.
        :param s: (*string*) Text string.
        :param zdir: Z direction.

        :return: 3D text graphics
        """
        if isinstance(x, (list, tuple)):
            x = np.array(x)
        if isinstance(y, (list, tuple)):
            y = np.array(y)
        if isinstance(z, (list, tuple)):
            z = np.array(z)
        if isinstance(s, (list, tuple)):
            s = np.array(s)
            ss = s[0]
        else:
            ss = s

        fontname = kwargs.pop('fontname', 'Arial')
        fontsize = kwargs.pop('fontsize', 14)
        bold = kwargs.pop('bold', False)
        color = kwargs.pop('color', 'black')
        if bold:
            font = Font(fontname, Font.BOLD, fontsize)
        else:
            font = Font(fontname, Font.PLAIN, fontsize)
        c = plotutil.getcolor(color)
        text = ChartText3D()
        text.setText(ss)
        text.setFont(font)
        text.setColor(c)
        if not isinstance(x, np.NDArray):
            text.setPoint(x, y, z)
        ha = kwargs.pop('horizontalalignment', None)
        if ha is None:
            ha = kwargs.pop('ha', None)
        if not ha is None:
            text.setXAlign(ha)
        va = kwargs.pop('verticalalignment', None)
        if va is None:
            va = kwargs.pop('va', None)
        if not va is None:
            text.setYAlign(va)
        bbox = kwargs.pop('bbox', None)
        if not bbox is None:
            fill = bbox.pop('fill', None)
            if not fill is None:
                text.setFill(fill)
            facecolor = bbox.pop('facecolor', None)
            if not facecolor is None:
                facecolor = plotutil.getcolor(facecolor)
                text.setFill(True)
                text.setBackground(facecolor)
            edge = bbox.pop('edge', None)
            if not edge is None:
                text.setDrawNeatline(edge)
            edgecolor = bbox.pop('edgecolor', None)
            if not edgecolor is None:
                edgecolor = plotutil.getcolor(edgecolor)
                text.setNeatlineColor(edgecolor)
                text.setDrawNeatline(True)
            linewidth = bbox.pop('linewidth', None)
            if not linewidth is None:
                text.setNeatlineSize(linewidth)
                text.setDrawNeatline(True)
            gap = bbox.pop('gap', None)
            if not gap is None:
                text.setGap(gap)
        if not zdir is None:
            if isinstance(zdir, (list, tuple)):
                text.setZDir(zdir[0], zdir[1], zdir[2])
            else:
                text.setZDir(zdir)
        draw3D = kwargs.pop('draw3d', None)
        if not draw3D is None:
            text.setDraw3D(draw3D)

        if isinstance(x, np.NDArray):
            graphic = GraphicFactory.createTexts3D(x._array, y._array, z._array, s._array, text)
        else:
            graphic = Graphic(text, None)
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphic)
        return graphic

    def arrow(self, x, y, dx, dy, **kwargs):
        """
        Add an arrow to the axes.
        
        :param x: (*float*) X coordinate.
        :param y: (*float*) Y coordinate.
        :param dx: (*float*) The length of arrow along x direction. 
        :param dy: (*float*) The length of arrow along y direction.
        
        :returns: Arrow graphic.
        """
        if not kwargs.has_key('facecolor'):
            kwargs['facecolor'] = (51, 204, 255)
        apb, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        apb = plotutil.polygon2arrow(apb, **kwargs)
        graphic = GraphicFactory.createArrow(x, y, dx, dy, apb)

        zorder = kwargs.pop('zorder', None)
        self.add_graphic(graphic, zorder=zorder)

        return graphic

    def arrowline(self, x, y, dx=0, dy=0, **kwargs):
        """
        Add an arrow line to the axes.
        
        :param x: (*float or array_like*) X coordinates.
        :param y: (*float or array_like*) Y coordinates.
        :param dx: (*float*) The length of arrow along x direction. Only valid when x is float.
        :param dy: (*float*) The length of arrow along y direction. Only valid when y is float.
        
        :returns: Arrow line graphic.
        """
        if isinstance(x, (list, tuple)):
            x = np.array(x)
        if isinstance(y, (list, tuple)):
            y = np.array(y)

        alb, isunique = plotutil.getlegendbreak('line', **kwargs)
        alb = plotutil.line2arrow(alb, **kwargs)
        if isinstance(x, NDArray):
            iscurve = kwargs.pop('curve', False)
            graphic = GraphicFactory.createArrowLine(x._array, y._array, alb, iscurve)
        else:
            graphic = GraphicFactory.createArrowLine(x, y, dx, dy, alb)

        zorder = kwargs.pop('zorder', None)
        self.add_graphic(graphic, zorder=zorder)

        return graphic

    def annotate(self, s, xy, *args, **kwargs):
        """
        Annotate the point xy with text s.
        
        :param s: (*string*) The text of the annotation.
        :param xy: (*float, float*) The point (x,y) to annotate.
        :param xytext: (*float, float*) The position (x,y) to place the text at. If None, 
            defaults to xy.
        :param arrowprops: (*dict*) Arrow properties.
            
        :returns: Annotation.
        """
        if len(args) > 0:
            xytext = args[0]
        else:
            xytext = xy

        ctext = plotutil.text(xytext[0], xytext[1], s, **kwargs)
        self._axes.addText(ctext)

        arrowprops = kwargs.pop('arrowprops', dict())
        if not arrowprops.has_key('headwidth'):
            arrowprops['headwidth'] = 10
        if not arrowprops.has_key('shrink'):
            arrowprops['shrink'] = 0.05
        alb, isunique = plotutil.getlegendbreak('line', **arrowprops)
        alb = plotutil.line2arrow(alb, **arrowprops)
        x0 = xytext[0]
        y0 = xytext[1]
        dx = xy[0] - xytext[0]
        dy = xy[1] - xytext[1]
        shrink = arrowprops['shrink']
        if shrink > 0:
            sx = dx * shrink
            sy = dy * shrink
            x0 = xytext[0] + sx
            y0 = xytext[1] + sy
            dx = dx - sx * 2
            dy = dy - sy * 2
        graphic = GraphicFactory.createArrowLine(x0, y0, dx, dy, alb)
        self.add_graphic(graphic)

        return ctext, graphic

    def axhline(self, y=0, xmin=0, xmax=1, **kwargs):
        """
        Add a horizontal line across the axis.
        Parameters
        ----------
        y : float, default: 0
            y position in data coordinates of the horizontal line.
        xmin : float, default: 0
            Should be between 0 and 1, 0 being the far left of the plot, 1 the
            far right of the plot.
        xmax : float, default: 1
            Should be between 0 and 1, 0 being the far left of the plot, 1 the
            far right of the plot.
        Returns
        -------
        `~matplotlib.lines.Line2D`
        """
        pass

    def fill(self, x, y, z, color=None, **kwargs):
        """
        Plot filled polygons.

        - To plot one region, specify x and y as vectors.
        - To plot multiple regions, specify x and y as matrices where each column corresponds to a polygon.

        :param x: (*array_like*) X coordinates for each vertex.
        :param y: (*array_like*) Y coordinates for each vertex.
        :param z: (*array_like*) Z coordinates for each vertex.
        :param color: (*Color*) Fill color.

        :return: Filled polygons
        """
        if color is not None:
            if isinstance(color, (list, tuple)) and (not isinstance(color[0], (int, float))):
                kwargs['colors'] = color
            else:
                kwargs['facecolor'] = color

        x = np.asarray(x)
        y = np.asarray(y)
        z = np.asarray(z)
        if x.ndim == 1 and x.contains_nan():
            x = np.split_array_by_nan(x)
        if y.ndim == 1 and y.contains_nan():
            y = np.split_array_by_nan(y)
        if z.ndim == 1 and z.contains_nan():
            z = np.split_array_by_nan(z)

        graphics = PolyCollection(None, xyz_data=[x, y, z], **kwargs)

        zorder = kwargs.pop('zorder', None)
        self.add_graphic(graphics, zorder=zorder)

        return graphics

    def patch(self, *args, **kwargs):
        """
        Create filled 3D patches.

        :param x: (*array_like*) X coordinates for each vertex.
        :param y: (*array_like*) Y coordinates for each vertex.
        :param z: (*array_like*) Z coordinates for each vertex.
        :param color: (*Color*) Fill color.

        :return: Filled 3D patches.
        """
        color = None
        is_xyz = True
        if len(args) == 4:
            x = np.asarray(args[0])
            y = np.asarray(args[1])
            z = np.asarray(args[2])
            color = args[3]
        elif kwargs.has_key('xdata'):
            x = np.asarray(kwargs.pop('xdata'))
            y = np.asarray(kwargs.pop('ydata'))
            z = np.asarray(kwargs.pop('zdata'))
        elif kwargs.has_key('faces'):
            faces = np.asarray(kwargs.pop('faces'))
            vertices = np.asarray(kwargs.pop('vertices'))
            is_xyz = False

        lb, is_unique = plotutil.getlegendbreak('polygon', **kwargs)
        if color is not None:
            alpha = kwargs.pop('alpha', None)
            colors = plotutil.getcolors(color, alpha)
            if len(colors) == 1:
                lb.setColor(colors[0])
            else:
                lbs = []
                for c in colors:
                    nlb = lb.clone()
                    nlb.setColor(c)
                    lbs.append(nlb)
                lb = lbs
        if is_xyz:
            graphics = GraphicFactory.createPolygons3D(x._array, y._array, z._array, lb)
        else:
            graphics = GraphicFactory.createPolygons3D(faces._array, vertices._array, lb)

        self.add_graphic(graphics)
        return graphics

    def rectangle(self, position, curvature=None, **kwargs):
        """
        Create one or more filled polygons.
        
        :param position: (*list*) Position of the rectangle [x, y, width, height].
        :param curvature: (*list*) Curvature of the rectangle [x, y]. Default is None.
        """
        lbreak, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        if isinstance(curvature, (int, float)):
            curvature = [curvature, curvature]
        graphic = GraphicFactory.createRectangle(position, curvature, lbreak)

        zorder = kwargs.pop('zorder', None)
        self.add_graphic(graphic, zorder=zorder)
        self._axes.setAutoExtent()

        return graphic

    def fill_between(self, x, y1, y2=0, where=None, **kwargs):
        """
        Make filled polygons between two curves (y1 and y2) where ``where==True``.
        
        :param x: (*array_like*) An N-length array of the x data.
        :param y1: (*array_like*) An N-length array (or scalar) of the y data.
        :param y2: (*array_like*) An N-length array (or scalar) of the y data.
        :param where: (*array_like*) If None, default to fill between everywhere. If not None, it is an 
            N-length boolean array and the fill will only happen over the regions where ``where==True``.
        """
        #Get dataset
        global gca   
        
        #Add data series
        label = kwargs.pop('label', 'S_0')
        dn = len(x)
        xdata = plotutil.getplotdata(x)
        if isinstance(y1, (int, long, float)):
            yy = []
            for i in range(dn):
                yy.append(y1)
            y1 = np.array(yy)._array
        else:
            y1 = plotutil.getplotdata(y1)
        if isinstance(y2, (int, long, float)):
            yy = []
            for i in range(dn):
                yy.append(y2)
            y2 = np.array(yy)._array
        else:
            y2 = plotutil.getplotdata(y2)
        if not where is None:
            if isinstance(where, (tuple, list)):
                where = np.array(where)
            where = where.asarray()
        
        #Set plot data styles
        if not 'fill' in kwargs:
            kwargs['fill'] = True
        if not 'edge' in kwargs:
            kwargs['edge'] = False
        pb, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        pb.setCaption(label)
        
        #Create graphics
        offset = kwargs.pop('offset', 0)
        zdir = kwargs.pop('zdir', 'z')
        if zdir == 'xy':
            y = kwargs.pop('y', x)
            ydata = plotutil.getplotdata(y)
            graphics = GraphicFactory.createFillBetweenPolygons(xdata, ydata, y1, y2, where, pb, \
                offset, zdir) 
        else:
            graphics = GraphicFactory.createFillBetweenPolygons(xdata, y1, y2, where, pb, \
                offset, zdir) 
            
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics

    def fill_betweenx(self, y, x1, x2=0, where=None, **kwargs):
        """
        Make filled polygons between two curves (x1 and x2) where ``where==True``.
        
        :param y: (*array_like*) An N-length array of the y data.
        :param x1: (*array_like*) An N-length array (or scalar) of the x data.
        :param x2: (*array_like*) An N-length array (or scalar) of the x data.
        :param where: (*array_like*) If None, default to fill between everywhere. If not None, it is an 
            N-length boolean array and the fill will only happen over the regions where ``where==True``.
        """
        # Add data series
        label = kwargs.pop('label', 'S_0')
        dn = len(y)
        ydata = plotutil.getplotdata(y)
        if isinstance(x1, (int, long, float)):
            xx = []
            for i in range(dn):
                xx.append(x1)
            x1 = np.array(xx)._array
        else:
            x1 = plotutil.getplotdata(x1)
        if isinstance(x2, (int, long, float)):
            xx = []
            for i in range(dn):
                xx.append(x2)
            x2 = np.array(xx)._array
        else:
            x2 = plotutil.getplotdata(x2)
        if not where is None:
            if isinstance(where, (tuple, list)):
                where = np.array(where)
            where = where.asarray()

        pb, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        pb.setCaption(label)

        # Create graphics
        graphics = GraphicFactory.createFillBetweenPolygonsX(ydata, x1, x2, where, pb)
        if graphics.size() > 0:
            zorder = kwargs.pop('zorder', None)
            self.add_graphic(graphics, zorder=zorder)
            self._axes.setAutoExtent()

        return graphics

    def pie(self, x, explode=None, labels=None, colors=None, autopct=None, pctdistance=0.6, shadow=False,
            labeldistance=1.1, startangle=0, radius=None, wedgeprops=None, **kwargs):
        """
        Plot a pie chart.
        
        Make a pie chart of array *x*. The fraction area of each wedge is given by x/sum(x). If
        sum(x) <= 1, then the values of x give the fractional area directly and the array will not
        be normalized. The wedges are plotted counterclockwise, by default starting from the x-axis.
        
        :param explode: (*None | len(x)sequence) If not *None*, is a ``len(x)`` array which specifies
            the fraction of the radius with which to offset each wedge.
        :param labels: (*None | len(x) sequence of colors*] A sequence of strings providing the labels
            for each wedge.
        :param colors: (*None | color sequence*) A sequence of color args through which the pie chart
            will cycle.
        :param autopct: (*None | format string | format function) If not *None*, is a string or function
            used to label the wedges with their numeric value. The label will be placed inside the wedge.
            If it is a format string, the label will be ``fmt%pct``. If it is a function, it will be called.
        :param pctdistance: (*float*) The ratio between the center of each pie slice and the start of the
            text generated by *autopct*. Ignored if autopct is *None*; default is 0.6.
        :param labeldistance: (*float*) The ratial distance at which the pie labels are drawn.
        :param shadow: (*boolean*) Draw a shadow beneath the pie.
        :param startangle: (*float*) If not *0*, rotates the start of the pie chart by *angle* degrees
            counterclockwise from the x-axis.
        :param radius: (*float*) The radius of the pie, if *radius* is *None* it will be set to 1.
        :param wedgeprops: (*dict*) Dict of arguments passed to the wedge objects making the pie. 
        :param fontname: (*string*) Font name. Default is ``Arial`` .
        :param fontsize: (*int*) Font size. Default is ``14`` .
        
        :returns: (*tuple*) Patches and texts.
        """
        n = len(x)
        x = plotutil.getplotdata(x)
        if colors is None:
            cmap = kwargs.pop('cmap', 'matlab_jet')
            colors = plotutil.makecolors(n, cmap=cmap)
        else:
            colors = plotutil.getcolors(colors)

        fontname = kwargs.pop('fontname', 'Arial')
        fontsize = kwargs.pop('fontsize', 14)
        bold = kwargs.pop('bold', False)
        fontcolor = kwargs.pop('fontcolor', 'black')
        if bold:
            font = Font(fontname, Font.BOLD, fontsize)
        else:
            font = Font(fontname, Font.PLAIN, fontsize)
        fontcolor = plotutil.getcolor(fontcolor)
        if radius is None:
            radius = 1
        if wedgeprops is None:
            wedgeprops = HashMap()
        else:
            jmap = HashMap()
            for key in wedgeprops.keys():
                value = wedgeprops[key]
                if key == 'edgecolor':
                    if value is None:
                        jmap['drawedge'] = False
                    else:
                        value = plotutil.getcolor(value)
                        jmap[key] = value
                else:
                    jmap[key] = value
            wedgeprops = jmap

        # Create graphics
        graphics = GraphicFactory.createPieArcs(x, colors, labels, startangle, explode, font, fontcolor, \
                                                labeldistance, autopct, pctdistance, radius, wedgeprops)

        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            graphics.setAntiAlias(antialias)

        for graphic in graphics:
            self.add_graphic(graphic)

        self._axes.setAspectType(AspectType.EQUAL)
        self._axes.getAxis(Location.BOTTOM).setVisible(False)
        self._axes.getAxis(Location.LEFT).setVisible(False)
        self._axes.getAxis(Location.TOP).setVisible(False)
        self._axes.getAxis(Location.RIGHT).setVisible(False)
        self._axes.setClip(False)

        return tuple(graphics)

    def boxplot(self, x, sym=None, vert=True, positions=None, widths=None, color=None, showcaps=True, showfliers=True,
                showmeans=False, \
                showmedians=True, meanline=False, medianline=True, boxprops=None, medianprops=None, meanprops=None,
                whiskerprops=None, capprops=None, flierprops=None, **kwargs):
        """
        Make a box and whisker plot.
        
        Make a box and whisker plot for each column of x or each vector in sequence x. The box extends from lower
        to upper quartile values of the data, with a line at the median. The whiskers extend from the box to show
        the range of the data. Flier points are those past the end of the whiskers.
        
        :param x: (*Array or a sequence of vectors*) The input data.
        :param sym: (*string*) The default symbol for flier points. Enter an empty string ('') if you don’t 
            want to show fliers. If None, then the fliers default to ‘b+’ If you want more control use the 
            flierprops kwarg.
        :param vert: (*boolean*) If True, draws vertical boxes (default). If False, draw horizontal boxes.
        :param positions: (*array_like*) Sets the positions of the boxes. The ticks and limits are automatically 
            set to match the positions. Defaults to range(1, N+1) where N is the number of boxes to be drawn.
        :param widths: (*scalar or array_like*) Sets the width of each box either with a scalar or a sequence. 
            The default is 0.5, or 0.15*(distance between extreme positions), if that is smaller.
        :param color: (*Color*) Color for all parts of the box plot. Defaul is None.
        :param showcaps: (*boolean*) Show the caps on the ends of whiskers. Default is ``True``.
        :param showfliers: (*boolean*) Show the outliers beyond the caps. Defaul is ``True``.
        :param showmeans: (*boolean*) Default is ``False``. Show the mean or not.
        :param showmedians: (*boolean*) Default is ``True``. Show the median or not.
        :param meanline: (*boolean*) Default is ``False``. If ``True`` (and showmeans is ``True``), will try to render
            the mean as a line spanning. Otherwise, means will be shown as points.
        :param medianline: (*boolean*) Default is ``True``. If ``True`` (and showmedians is ``True``), will try to render
            the median as a line spanning. Otherwise, medians will be shown as points.
        :param boxprops: (*dict*) Specifies the style of the box.
        :param medianprops: (*dict*) Specifies the style of the median.
        :param meanprops: (*dict*) Specifies the style of the mean.
        :param whiskerprops: (*dict*) Specifies the style of the whiskers.
        :param capprops: (*dict*) Specifies the style of the caps.
        :param flierprops: (*dict*) Specifies the style of the fliers.
        """
        if isinstance(x, list):
            x1 = []
            for a in x:
                x1.append(plotutil.getplotdata(a))
            x = x1
        else:
            x = plotutil.getplotdata(x)
            x = [x]

        if not positions is None:
            if isinstance(positions, (NDArray, DimArray)):
                positions = positions.tolist()

        if not widths is None:
            if isinstance(widths, (int, float)):
                nwidths = []
                for i in range(len(x)):
                    nwidths.append(widths)
                widths = nwidths
            elif isinstance(widths, (NDArray, DimArray)):
                widths = widths.tolist()

        # Get box plot properties
        if not color is None:
            color = plotutil.getcolor(color)
        if not sym is None:
            sym = plotutil.getmarkerplotstyle(sym, '')
            sym.setDrawFill(False)
            if not color is None:
                sym.setColor(color)
                sym.setOutlineColor(color)
        if boxprops is None:
            boxprops = PolygonBreak()
            boxprops.setDrawFill(False)
            boxprops.setOutlineColor(color is None and Color.blue or color)
        else:
            if not boxprops.has_key('facecolor'):
                boxprops['facecolor'] = None
            boxprops = plotutil.getlegendbreak('polygon', **boxprops)[0]
        if medianline:
            if medianprops is None:
                medianprops = PolylineBreak()
                medianprops.setColor(color is None and Color.red or color)
            else:
                medianprops = plotutil.getlegendbreak('line', **medianprops)[0]
        else:
            if medianprops is None:
                medianprops = PointBreak()
                medianprops.setColor(color is None and Color.blue or color)
            else:
                medianprops = plotutil.getlegendbreak('point', **medianprops)[0]
        if whiskerprops is None:
            whiskerprops = PolylineBreak()
            whiskerprops.setColor(color is None and Color.black or color)
            whiskerprops.setStyle(LineStyles.DASH)
        else:
            whiskerprops = plotutil.getlegendbreak('line', **whiskerprops)[0]
        if capprops is None:
            capprops = PolylineBreak()
            capprops.setColor(color is None and Color.black or color)
        else:
            capprops = plotutil.getlegendbreak('line', **capprops)[0]
        if meanline:
            if not meanprops is None:
                meanprops = plotutil.getlegendbreak('line', **meanprops)[0]
            else:
                meanprops = PolylineBreak()
                meanprops.setColor(color is None and Color.black or color)
        else:
            if meanprops is None:
                meanprops = PointBreak()
                meanprops.setStyle(PointStyle.SQUARE)
                meanprops.setColor(color is None and Color.red or color)
                meanprops.setOutlineColor(color is None and Color.black or color)
            else:
                meanprops = plotutil.getlegendbreak('point', **meanprops)[0]
        if not flierprops is None:
            flierprops = plotutil.getlegendbreak('point', **flierprops)[0]
        else:
            flierprops = sym
        if flierprops is None:
            flierprops = PointBreak()
            flierprops.setColor(color is None and Color.red or color)
            flierprops.setStyle(PointStyle.PLUS)

        # Create graphics
        if vert:
            graphics = GraphicFactory.createBox(x, positions, widths, showcaps, showfliers, showmeans, \
                                                showmedians, boxprops, medianprops, whiskerprops, capprops, meanprops,
                                                flierprops)
        else:
            graphics = GraphicFactory.createHBox(x, positions, widths, showcaps, showfliers, showmeans, \
                                                 showmedians, boxprops, medianprops, whiskerprops, capprops, meanprops,
                                                 flierprops)

        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            graphics.setAntiAlias(antialias)

        self.add_graphic(graphics)
        self._axes.setAutoExtent()

        return graphics

    def violinplot(self, dataset, positions=None, widths=0.5, boxwidth=0.01, boxprops=None,
                   whiskerprops=None, **kwargs):
        """
        Make a violin plot.
        
        :param dataset: (*Array or a sequence of vectors*) The input data.
        :param positions: (*array_like*) Sets the positions of the violins. The ticks and limits are automatically 
            set to match the positions. Defaults to range(1, N+1) where N is the number of violins to be drawn.
        :param widths: (*scalar or array_like*) Sets the width of each box either with a scalar or a sequence. 
            The default is 0.5, or 0.15*(distance between extreme positions), if that is smaller.   
        :param boxwidth: (*float*) box width.
        :param boxprops: (*dict*) Specifies the style of the box.
        :param whiskerprops: (*dict*) Specifies the style of the whiskers.
        
        :returns: Violin graphics.
        """
        if isinstance(dataset, NDArray):
            dataset = [dataset]
        n = len(dataset)

        if positions is None:
            positions = np.arange(1, n + 1)

        graphics = []
        pdfs = []
        xx = []
        max = 0
        for data, position in zip(dataset, positions):
            if data.contains_nan():
                data = np.delnan(data)
            kde = np.stats.GaussianKDE(data)
            x = np.linspace(data.min(), data.max(), 100)
            pdf = kde.evaluate(x)
            pdfs.append(pdf)
            xx.append(x)
            if pdf.max() > max:
                max = pdf.max()

        if boxprops is None:
            boxprops = dict(color='k', edgecolor=None)
        if whiskerprops is None:
            whiskerprops = dict(color='k')
        if not kwargs.has_key('color'):
            kwargs['color'] = 'c'
        if not kwargs.has_key('edgecolor'):
            kwargs['edgecolor'] = 'b'
        ratio = widths / max
        for data, position, pdf, x in zip(dataset, positions, pdfs, xx):
            pdf = pdf * ratio
            self.fill_betweenx(x, position - pdf, position + pdf, **kwargs)
            ggs = self.boxplot(data, positions=[position], widths=boxwidth, showfliers=False, \
                               showcaps=False, medianline=False, boxprops=boxprops, \
                               whiskerprops=whiskerprops, medianprops=dict(color='w', edgecolor=None))
            graphics.extend(ggs)

        return graphics

    def taylor_diagram(self, stddev, correlation, std_max=1.65, labels=None, ref_std=1., colors=None,
                       **kwargs):
        """
        Create Taylor diagram.

        :param stddev: (*array*) Standard deviation.
        :param correlation: (*array*) Pattern correlations.
        :param ref_std: (*float*) Reference standard deviation. Default value is `1.`.
        :param std_max: (*float*) Maximum standard deviation. Default value is `1.651`.
        :param labels: (*list of string*) Data labels.
        :param colors: (*list of color*) Data points colors.
        :param markers: (*list of marker string*) Data points markers.
        :param sizes: (*list of int*) Data points sizes.
        :param std_max_axis_props: (*dict*) Property of the maximum standard deviation axis line.
        :param corr_tick_props: (*dict*) Property of the correlation tick lines.
        :param corr_tick_label_props: (*dict*) Property of the correlation tick labels.
        :param corr_label_props: (*dict*) Property of the correlation label.

        :returns: Axes and Taylor graphic.
        """
        # Set axes
        self.set_aspect('equal')
        self.set_clip(False)
        self.xaxis(location='top', visible=False)
        self.yaxis(location='right', visible=False)

        # plot RMS circle
        th = np.linspace(0, np.pi, 200)
        xunit = np.cos(th)
        yunit = np.sin(th)
        tickrms = np.arange(0.25, ref_std + 0.2, 0.25)
        radius = np.sqrt(ref_std ** 2 + std_max ** 2 -
                         2 * ref_std * std_max * xunit)
        for iradius in tickrms:
            phi = th[np.where(radius >= iradius)]
            if len(phi) > 0:
                phi = phi[0]
                ig = np.where(iradius * np.cos(th) + ref_std <=
                              std_max * np.cos(phi))
                self.plot(xunit[ig] * iradius + ref_std, yunit[ig] * iradius, color='gray')

        # plot stddev circle
        self.set_xlim(0, std_max)
        self.set_ylim(0, std_max)
        std_ticks = np.arange(0, std_max, 0.25)
        self.set_xticks(std_ticks)
        xtick_labels = []
        for std_tick in std_ticks:
            if std_tick == ref_std:
                xtick_labels.append('REF')
            else:
                xtick_labels.append(str(std_tick))
        self.set_xticklabels(xtick_labels)
        self.set_yticks(std_ticks)
        th = np.linspace(0, np.pi * 0.5, 200)
        xunit = np.cos(th)
        yunit = np.sin(th)
        xticks = self.get_xticks()
        for i in xticks:
            self.plot(xunit * i, yunit * i, color='gray', linestyle='--')
        std_max_axis_props = kwargs.pop('std_max_axis_props', dict(color='k'))
        self.plot(xunit * std_max, yunit * std_max, **std_max_axis_props)

        # plot correlation lines
        values = np.arange(0., 1., 0.1)
        values = values.join(np.array([0.95, 0.99, 1.0]), 0)
        corr_tick_props = kwargs.pop('corr_tick_props', dict(color='k'))
        corr_tick_label_props = kwargs.pop('corr_tick_label_props', dict(yalign='center'))
        if not corr_tick_label_props.has_key('yalign'):
            corr_tick_label_props['yalign'] = 'center'
        corr_label_props = kwargs.pop('corr_label_props', dict(xalign='center', yalign='bottom'))
        if not corr_label_props.has_key('xalign'):
            corr_label_props['xalign'] = 'center'
        if not corr_label_props.has_key('yalign'):
            corr_label_props['yalign'] = 'bottom'
        for t in values:
            theta = np.acos(t)
            x = np.cos(theta) * std_max
            y = np.sin(theta) * std_max
            if 0 < t < 1:
                if t == 0.6 or t == 0.9:
                    self.plot([0, x], [0, y], color='gray', linestyle=':')
                self.plot([x * 0.98, x], [y * 0.98, y], **corr_tick_props)
            x = x * 1.02
            y = y * 1.02
            self.text(x, y, str(t), rotation=np.degrees(theta), **corr_tick_label_props)
            if t == 0.7:
                self.text(x * 1.1, y * 1.1, 'Correlation', rotation=np.degrees(theta) - 90,
                          **corr_label_props)

        values = np.arange(0.05, 0.9, 0.1)
        values = values.join(np.array([0.91, 0.92, 0.93, 0.94, 0.96, 0.97, 0.98]), 0)
        for t in values:
            theta = np.acos(t)
            x = np.cos(theta) * std_max
            y = np.sin(theta) * std_max
            self.plot([x * 0.99, x], [y * 0.99, y], **corr_tick_props)

        # plot data
        stddev = np.atleast_2d(stddev)
        correlation = np.atleast_2d(correlation)
        if not labels is None:
            labels = np.atleast_2d(labels)
        ncase = stddev.shape[0]
        if colors is None:
            cmap = kwargs.pop('cmap', 'matlab_jet')
            colors = plotutil.makecolors(ncase, cmap)
        gg = []
        markers = kwargs.pop('markers', [None] * ncase)
        sizes = kwargs.pop('sizes', [6] * ncase)
        for i in range(ncase):
            rho = stddev[i]
            theta = np.acos(correlation[i])
            x = np.cos(theta) * rho
            y = np.sin(theta) * rho
            gg.append(self.scatter(x, y, edge=False, c=colors[i], s=sizes[i], marker=markers[i]))
            if labels is None:
                lbs = []
                for j in range(len(rho)):
                    lbs.append(str(j + 1))
            else:
                lbs = labels[i]
            for xx, yy, label in zip(x, y, lbs):
                self.text(xx, yy, label, color=colors[i], xalign='center', yalign='bottom', yshift=-sizes[i])

        self.set_xlim(0, std_max)
        self.set_ylim(0, std_max)

        xl = kwargs.pop('xlabel', None)
        if not xl is None:
            self.set_xlabel(xl)
        yl = kwargs.pop('ylabel', 'Standard Deviation (Normalized)')
        self.set_ylabel(yl)
        tt = kwargs.pop('title', None)
        if not tt is None:
            self.set_title(tt)
            self.set_title(' ', loc='left')

        return self, gg

    def barbs(self, *args, **kwargs):
        """
        Plot a 2-D field of barbs.
        
        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param u: (*array_like*) U component of the arrow vectors (wind field) or wind direction.
        :param v: (*array_like*) V component of the arrow vectors (wind field) or wind speed.
        :param z: (*array_like*) Optional, 2-D z value array.
        :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level 
            barbs to draw, in increasing order.
        :param cmap: (*string*) Color map string.
        :param fill_value: (*float*) Fill_value. Default is ``-9999.0``.
        :param isuv: (*boolean*) Is U/V or direction/speed data array pairs. Default is True.
        :param size: (*float*) Base size of the arrows. Default is 10.
        :param order: (*int*) Z-order of created layer for display.
        
        :returns: Barbs graphics.
        """
        ls = kwargs.pop('symbolspec', None)
        cmap = plotutil.getcolormap(**kwargs)
        fill_value = kwargs.pop('fill_value', -9999.0)
        order = kwargs.pop('order', None)
        isuv = kwargs.pop('isuv', True)
        n = len(args)
        iscolor = False
        cdata = None
        xaxistype = None
        if n <= 3 or (n == 4 and isinstance(args[3], int)):
            x = args[0].dimvalue(1)
            y = args[0].dimvalue(0)
            x, y = np.meshgrid(x, y)
            u = args[0]
            v = args[1]
            if args[0].islondim(1):
                xaxistype = 'lon'
            elif args[0].islatdim(1):
                xaxistype = 'lat'
            elif args[0].istimedim(1):
                xaxistype = 'time'
            args = args[2:]
            if len(args) > 0:
                cdata = args[0]
                iscolor = True
                args = args[1:]
        elif n <= 6:
            x = args[0]
            y = args[1]
            u = args[2]
            v = args[3]
            args = args[4:]
            if len(args) > 0:
                cdata = args[0]
                iscolor = True
                args = args[1:]
        x = plotutil.getplotdata(x)
        y = plotutil.getplotdata(y)
        u = plotutil.getplotdata(u)
        v = plotutil.getplotdata(v)

        if ls is None:
            if iscolor:
                if len(args) > 0:
                    cn = args[0]
                    ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), cn, cmap)
                else:
                    levs = kwargs.pop('levs', None)
                    if levs is None:
                        ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), cmap)
                    else:
                        if isinstance(levs, NDArray):
                            levs = levs.aslist()
                        ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), levs, cmap)
            else:
                if cmap.getColorCount() == 1:
                    c = cmap.getColor(0)
                else:
                    c = Color.black
                ls = LegendManage.createSingleSymbolLegendScheme(GeometryType.POINT, c, 10)
            ls = plotutil.setlegendscheme_point(ls, **kwargs)

        if not cdata is None:
            cdata = plotutil.getplotdata(cdata)
        graphics = GraphicFactory.createBarbs(x, y, u, v, cdata, ls, isuv)

        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            graphics.setAntiAlias(antialias)

        if not xaxistype is None:
            self.set_xaxis_type(xaxistype)
            self._axes.updateDrawExtent()

        clip_on = kwargs.pop('clip_on', None)
        if clip_on is not None:
            graphics.setClipOn(clip_on)

        transform = kwargs.pop('transform', None)
        if transform is not None:
            graphics.setTransform(transform)

        zorder = kwargs.pop('zorder', None)
        self.add_graphic(graphics, zorder=zorder)

        return graphics

    def quiver(self, *args, **kwargs):
        """
        Plot a 2-D field of arrows.
        
        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param u: (*array_like*) U component of the arrow vectors (wind field) or wind direction.
        :param v: (*array_like*) V component of the arrow vectors (wind field) or wind speed.
        :param z: (*array_like*) Optional, 2-D z value array.
        :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level 
            vectors to draw, in increasing order.
        :param cmap: (*string*) Color map string.
        :param fill_value: (*float*) Fill_value. Default is ``-9999.0``.
        :param isuv: (*boolean*) Is U/V or direction/speed data array pairs. Default is True.
        :param size: (*float*) Base size of the arrows.
        :param overhang: (*float*) fraction that the arrow is swept back (0 overhang means 
            triangular shape). Can be negative or greater than one.
        :param zorder: (*int*) Z-order of created layer for display.
        
        :returns: Quiver graphics.
        """
        ls = kwargs.pop('symbolspec', None)
        cmap = plotutil.getcolormap(**kwargs)
        fill_value = kwargs.pop('fill_value', -9999.0)
        zorder = kwargs.pop('zorder', None)
        isuv = kwargs.pop('isuv', True)
        n = len(args)
        iscolor = False
        cdata = None
        xaxistype = None
        if n < 4 or (n == 4 and isinstance(args[3], int)):
            u = args[0]
            v = args[1]
            if isinstance(u, DimArray):
                x = u.dimvalue(1)
                y = u.dimvalue(0)
                # if args[0].islondim(1):
                #     xaxistype = 'lon'
                # elif args[0].islatdim(1):
                #     xaxistype = 'lat'
                # elif args[0].istimedim(1):
                #     xaxistype = 'time'
            else:
                x = np.arange(u.shape[1])
                y = np.arange(u.shape[0])
            x, y = np.meshgrid(x, y)
            args = args[2:]
            if len(args) > 0:
                cdata = args[0]
                iscolor = True
                args = args[1:]
        elif n <= 6:
            x = args[0]
            y = args[1]
            u = args[2]
            v = args[3]
            args = args[4:]
            if len(args) > 0:
                cdata = args[0]
                iscolor = True
                args = args[1:]
        x = plotutil.getplotdata(x)
        y = plotutil.getplotdata(y)
        u = plotutil.getplotdata(u)
        v = plotutil.getplotdata(v)

        if ls is None:
            if iscolor:
                if len(args) > 0:
                    cn = args[0]
                    ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), cn, cmap)
                else:
                    levs = kwargs.pop('levs', None)
                    if levs is None:
                        ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), cmap)
                    else:
                        if isinstance(levs, NDArray):
                            levs = levs.tolist()
                        ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), levs, cmap)
            else:
                if cmap.getColorCount() == 1:
                    c = cmap.getColor(0)
                else:
                    c = Color.black
                ls = LegendManage.createSingleSymbolLegendScheme(GeometryType.POINT, c, 10)
            ls = plotutil.setlegendscheme_arrow(ls, **kwargs)

        if not cdata is None:
            cdata = plotutil.getplotdata(cdata)

        igraphic = GraphicFactory.createArrows(x, y, u, v, cdata, ls, isuv)

        if not xaxistype is None:
            self.set_xaxis_type(xaxistype)
            self._axes.updateDrawExtent()

        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            igraphic.setAntiAlias(antialias)

        zorder = kwargs.pop('zorder', None)
        self.add_graphic(igraphic, zorder=zorder)

        return igraphic

    def quiverkey(self, *args, **kwargs):
        """
        Add a key to a quiver plot.
        
        :param Q: (*MILayer or GraphicCollection*) The quiver layer instance returned by a call to quiver/quiverm.
        :param X: (*float*) The location x of the key.
        :param Y: (*float*) The location y of the key.
        :param U: (*float*) The length of the key.
        :param label: (*string*) A string with the length and units of the key.
        :param coordinates=['axes'|'figure'|'data']: (*string*) Default: 'axes'. Coordinate system and units for
            *X, Y*. 'axes' and 'figure' are normalized coordinate system with 0,0 in the lower left and 
            1,1 in the upper right, 'data' are the axes data coordinates (used for the locations of the 
            vectors in the quiver plot itself).
        :param color: (*Color*) Overrides face and edge colors from Q.
        :param labelpos=['N'|'S'|'E'|'W']: (*string*) Position the label above, below, to the right, to
            the left of the arrow, respectively.
        :param labelsep: (*float*) Distance in pixel between the arrow and the label. Default is 5.
        :param labelcolor: (*Color*) Label color. Default to default is black.
        :param fontname: (*string*) Label font name. Default is ``Arial`` .
        :param fontsize: (*int*) Label font size. Default is ``12`` .
        :param bold: (*boolean*) Is bold font or not. Default is ``False`` .
        :param fontproperties: (*dict*) A dictionary with keyword arguments accepted by the FontProperties
            initializer: *family, style, variant, size, weight*.
        """
        wa = ChartWindArrow()
        Q = args[0]
        if isinstance(Q, MILayer):
            wa.setLayer(Q._layer)
        else:
            wa.setLayer(Q)
        x = args[1]
        y = args[2]
        wa.setX(x)
        wa.setY(y)
        u = args[3]
        wa.setLength(u)
        if len(args) == 5:
            label = args[4]
            wa.setLabel(label)

        coordinates = kwargs.pop('coordinates', 'axes')
        wa.setCoordinates(coordinates)
        arrowbreak, isunique = plotutil.getlegendbreak('point', **kwargs)
        arrowbreak = plotutil.point2arrow(arrowbreak, **kwargs)
        wa.setArrowBreak(arrowbreak)
        lcobj = kwargs.pop('labelcolor', 'k')
        lcolor = plotutil.getcolor(lcobj)
        wa.setLabelColor(lcolor)
        fontname = kwargs.pop('fontname', 'Arial')
        fontsize = kwargs.pop('fontsize', 12)
        bold = kwargs.pop('bold', False)
        if bold:
            font = Font(fontname, Font.BOLD, fontsize)
        else:
            font = Font(fontname, Font.PLAIN, fontsize)
        wa.setFont(font)
        labelsep = kwargs.pop('labelsep', None)
        if not labelsep is None:
            wa.setLabelSep(labelsep)
        bbox = kwargs.pop('bbox', None)
        if not bbox is None:
            fill = bbox.pop('fill', None)
            if not fill is None:
                wa.setFill(fill)
            facecolor = bbox.pop('facecolor', None)
            if not facecolor is None:
                facecolor = plotutil.getcolor(facecolor)
                wa.setFill(True)
                wa.setBackground(facecolor)
            edge = bbox.pop('edge', None)
            if not edge is None:
                wa.setDrawNeatline(edge)
            edgecolor = bbox.pop('edgecolor', None)
            if not edgecolor is None:
                edgecolor = plotutil.getcolor(edgecolor)
                wa.setNeatlineColor(edgecolor)
                wa.setDrawNeatline(True)
            linewidth = bbox.pop('linewidth', None)
            if not linewidth is None:
                wa.setNeatlineSize(linewidth)
                wa.setDrawNeatline(True)
        self._axes.setWindArrow(wa)

    def streamplot(self, *args, **kwargs):
        """
        Plot 2D streamline.

        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param u: (*array_like*) U component of the arrow vectors (wind field) or wind direction.
        :param v: (*array_like*) V component of the arrow vectors (wind field) or wind speed.
        :param z: (*array_like*) Optional, 2-D z value array.
        :param color: (*Color*) Streamline color.
        :param fill_value: (*float*) Fill_value. Default is ``-9999.0``.
        :param isuv: (*boolean*) Is U/V or direction/speed data array pairs. Default is True.
        :param density: (*int*) Streamline density. Default is 4.
        :param offset: (*float*) Z direction offset. Default is 0.
        :param zdir: (*int*) Z direction ['x'|'y'|'z']. Default is `z`.

        :returns: (*graphics*) 2D streamline graphics.
        """
        ls = kwargs.pop('symbolspec', None)
        cmap = plotutil.getcolormap(**kwargs)
        density = kwargs.pop('density', 4)
        iscolor = False
        cdata = None
        if len(args) < 4:
            u = args[0]
            v = args[1]
            if isinstance(u, DimArray):
                y = u.dimvalue(0)
                x = u.dimvalue(1)
            else:
                ny, nx = u.shape
                x = np.arange(nx)
                y = np.arange(ny)
            args = args[2:]
        else:
            x = args[0]
            y = args[1]
            u = args[2]
            v = args[3]
            args = args[4:]
        if len(args) > 0:
            cdata = args[0]
            iscolor = True
            args = args[1:]

        if x.ndim == 2:
            x = x[0]
        if y.ndim == 2:
            y = y[:,0]

        x = plotutil.getplotdata(x)
        y = plotutil.getplotdata(y)
        u = plotutil.getplotdata(u)
        v = plotutil.getplotdata(v)

        if ls is None:
            if iscolor:
                if len(args) > 0:
                    cn = args[0]
                    if isinstance(cn, NDArray):
                        cn = cn.aslist()
                    ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), cn, cmap)
                else:
                    levs = kwargs.pop('levels', None)
                    if levs is None:
                        ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), cmap)
                    else:
                        if isinstance(levs, NDArray):
                            levs = levs.tolist()
                        ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), levs, cmap)
            else:
                if cmap.getColorCount() == 1:
                    c = cmap.getColor(0)
                else:
                    c = Color.black
                ls = LegendManage.createSingleSymbolLegendScheme(GeometryType.LINESTRING, c, 1)
            ls = plotutil.setlegendscheme_line(ls, **kwargs)

        if not kwargs.has_key('headwidth'):
            kwargs['headwidth'] = 1
        if not kwargs.has_key('headlength'):
            kwargs['headlength'] = 2.5 * kwargs['headwidth']
        for i in range(ls.getBreakNum()):
            lb = plotutil.line2stream(ls.getLegendBreak(i), **kwargs)
            ls.setLegendBreak(i, lb)

        if not cdata is None:
            cdata = plotutil.getplotdata(cdata)

        isuv = kwargs.pop('isuv', True)
        offset = kwargs.pop('offset', 0)
        zdir = kwargs.pop('zdir', 'z')
        graphics = GraphicFactory.createStreamlines(x, y, u, v, cdata, density, ls, isuv,
                                                    offset, zdir)

        lighting = kwargs.pop('lighting', None)
        if not lighting is None:
            graphics.setUsingLight(lighting)

        # Pipe
        pipe = kwargs.pop('pipe', False)
        if pipe:
            radius = kwargs.pop('radius', 0.02)
            steps = kwargs.pop('steps', 48)
            graphics = GraphicFactory.lineString3DToPipe(graphics, radius, steps)

        self.add_graphic(graphics)

        return graphics

    def get_legend(self):
        """
        Get legend of the axes.
        
        :return: Legend
        """
        return self._axes.getLegendScheme()

    def legend(self, *args, **kwargs):
        """
        Places a legend on the axes.
        
        :param breaks: (*ColorBreak*) Legend breaks (optional).
        :param labels: (*list of string*) Legend labels (optional).
        :param orientation: (*string*) Legend orientation: ``vertical`` or ``horizontal``.
        :param loc: (*string*) The location of the legend, including: 'upper right', 'upper left',
            'lower left', 'lower right', 'right', 'center left', 'center right', lower center',
            'upper center', 'center' and 'custom'. Default is 'upper right'.
        :param x: (*float*) Location x in normalized (0, 1) units when ``loc=custom`` .
        :param y: (*float*) Location y in normalized (0, 1) units when ``loc=custom`` .
        :param frameon: (*boolean*) Control whether a frame should be drawn around the legend. Default
            is True.
        :param facecolor: (*None or color*) Control the legend’s background color. Default is None which 
            set not draw background.
        :param fontname: (*string*) Label font name. Default is ``Arial`` .
        :param fontsize: (*int*) Label font size. Default is ``14`` .
        :param labelcolor: (*color*) Label string color. Defaul is ``black``.
        :param bold: (*boolean*) Is bold font or not. Default is ``False`` .
        :param title: (*string*) Title string.
        :param titlefontname: (*string*) Title font name.
        :param titlefontsize: (*int*) Title font size.
        :param titlecolor: (*color*) Title color. Default is ``black`` .
        :param breakspace: (*float*) Break space. Default is `3`.
        :param titlespace: (*float*) Title space. Default is `5`.
        :param markerscale: (*float*) Marker symbol scale.
        :param markerwidth: (*float*) Marker symbol width.
        :param markerheight: (*float*) Marker symbol height.
        :param ncol: (*float*) Column number of the legend.
        :param xshift: (*float*) X shift.
        :param yshift: (*float*) Y shift.
        
        :returns: (*ChartLegend*) The chart legend.
        """
        newlegend = kwargs.pop('newlegend', True)
        ols = self._axes.getLegendBreaks()
        if newlegend:
            clegend = ChartLegend(ols)
        else:
            clegend = self._axes.getLegend()

        ls = kwargs.pop('legend', None)

        if ls is None:
            if len(args) > 0:
                gg = args[0]
                if not isinstance(gg, (list, tuple)):
                    gg = [gg]

                lbs = []
                for lb in gg:
                    if isinstance(lb, Graphic):
                        lbs.extend(lb.getLegendBreaks())
                    elif isinstance(lb, MILayer):
                        lbs.extend(lb.legend().getLegendBreaks())
                    elif isinstance(lb, LegendScheme):
                        lbs.extend(lb.getLegendBreaks())
                    else:
                        lbs.append(lb)

                if len(args) == 2:
                    labels = args[1]
                    for i in range(0, len(lbs)):
                        if i < len(labels):
                            lbs[i].setCaption(labels[i])

                if isinstance(lbs[0], basestring):
                    clegend.setTickCaptions(lbs)
                else:
                    # ls = LegendScheme()
                    # for lb in lbs:
                    #     ls.addLegendBreak(lb)
                    # if len(lbs) == 1:
                    #     ls.setLegendType(LegendType.SINGLE_SYMBOL)
                    # elif lbs[0].getStartValue() == lbs[1].getEndValue():
                    #     ls.setLegendType(LegendType.UNIQUE_VALUE)
                    # else:
                    #     ls.setLegendType(LegendType.GRADUATED_COLOR)
                    if clegend is None:
                        clegend = ChartLegend(lbs)
                        self._axes.setLegend(clegend)
                    else:
                        clegend.setLegendBreaks(lbs)
        else:
            if len(args) > 0:
                labels = args[0]
                for i in range(len(labels)):
                    if i < ls.getBreakNum():
                        ls.getLegendBreak(i).setCaption(labels[i])
            if clegend is None:
                clegend = ChartLegend(ls)
                self._axes.setLegend(clegend)
            else:
                clegend.setLegendBreaks(ls.getLegendBreaks())

        loc = kwargs.pop('loc', 'upper right')
        lp = LegendPosition.fromString(loc)
        clegend.setPosition(lp)
        if lp == LegendPosition.CUSTOM:
            x = kwargs.pop('x', 0)
            y = kwargs.pop('y', 0)
            clegend.setX(x)
            clegend.setY(y)
        orien = 'vertical'
        if lp == LegendPosition.UPPER_CENTER_OUTSIDE or lp == LegendPosition.LOWER_CENTER_OUTSIDE:
            orien = 'horizontal'
        orientation = kwargs.pop('orientation', orien)
        if orientation == 'horizontal':
            clegend.setPlotOrientation(PlotOrientation.HORIZONTAL)
        else:
            clegend.setPlotOrientation(PlotOrientation.VERTICAL)
        frameon = kwargs.pop('frameon', True)
        clegend.setDrawNeatLine(frameon)
        bcobj = kwargs.pop('background', None)
        bcobj = kwargs.pop('facecolor', bcobj)
        if bcobj is None:
            clegend.setDrawBackground(False)
        else:
            clegend.setDrawBackground(True)
            background = plotutil.getcolor(bcobj)
            clegend.setBackground(background)
        labelfontdic = kwargs.pop('labelfont', None)
        if labelfontdic is None:
            labelfont = plotutil.getfont_1(**kwargs)
        else:
            labelfont = plotutil.getfont(labelfontdic)
        clegend.setTickLabelFont(labelfont)
        labelcolor = kwargs.pop('labelcolor', None)
        if not labelcolor is None:
            labelcolor = plotutil.getcolor(labelcolor)
            clegend.setTickLabelColor(labelcolor)
        title = kwargs.pop('title', None)
        if not title is None:
            titlefontdic = kwargs.pop('titlefont', None)
            exfont = False
            if titlefontdic is None:
                fontname = kwargs.pop('titlefontname', None)
                exfont = False
                if fontname is None:
                    fontname = 'Arial'
                else:
                    exfont = True
                fontsize = kwargs.pop('titlefontsize', 14)
                bold = kwargs.pop('titlebold', False)
                if bold:
                    titlefont = Font(fontname, Font.BOLD, fontsize)
                else:
                    titlefont = Font(fontname, Font.PLAIN, fontsize)
            else:
                titlefont = plotutil.getfont(titlefontdic)
            title = ChartText(title, titlefont)
            title.setUseExternalFont(exfont)
            titlecolor = kwargs.pop('titlecolor', None)
            if not titlecolor is None:
                titlecolor = plotutil.getcolor(titlecolor)
                title.setColor(titlecolor)
            title.setXAlign(XAlign.CENTER)
            title.setYAlign(YAlign.TOP)
            clegend.setLabel(title)
        breakspace = kwargs.pop('breakspace', None)
        if not breakspace is None:
            clegend.setBreakSpace(breakspace)
        titlespace = kwargs.pop('titlespace', None)
        if titlespace is not None:
            clegend.setTitleSpace(titlespace)
        markerscale = kwargs.pop('markerscale', None)
        if not markerscale is None:
            clegend.setSymbolScale(markerscale)
        markerwidth = kwargs.pop('markerwidth', None)
        markerheight = kwargs.pop('markerheight', None)
        if not markerwidth is None:
            clegend.setSymbolWidth(markerwidth)
        if not markerheight is None:
            clegend.setSymbolHeight(markerheight)
        ncol = kwargs.pop('ncol', None)
        if not ncol is None:
            clegend.setColumnNumber(ncol)
            clegend.setAutoRowColNum(False)
        xshift = kwargs.pop('xshift', None)
        if not xshift is None:
            clegend.setXShift(xshift)
        yshift = kwargs.pop('yshift', None)
        if not yshift is None:
            clegend.setYShift(yshift)
        if newlegend:
            self._axes.addLegend(clegend)

        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            clegend.setAntiAlias(antialias)

        return clegend

    def colorbar(self, mappable=None, **kwargs):
        """
        Add a colorbar to a plot.
        
        :param mappable: (*MapLayer | LegendScheme | List of ColorBreak*) The mappable in plot.
        :param cax: (*Plot*) None | axes object into which the colorbar will be drawn.
        :param cmap: (*string*) Color map name. Default is None.
        :param shrink: (*float*) Fraction by which to shrink the colorbar. Default is 1.0.
        :param orientation: (*string*) Colorbar orientation: ``vertical`` or ``horizontal``.
        :param aspect: (*int*) Ratio of long to short dimensions.
        :param fontname: (*string*) Font name. Default is ``Arial`` .
        :param fontsize: (*int*) Font size. Default is ``14`` .
        :param bold: (*boolean*) Is bold font or not. Default is ``False`` .
        :param label: (*string*) Label. Default is ``None`` .
        :param labelloc: (*string*) Label location ['in' | 'out' | 'top' | 'bottom' | 'left' | 'right'].
            Default is ``out``.
        :param labelshift: (*float*) Label location shift value. Default is None (value is 5).
        :param newlegend: (*boolean*) Add a new legend or replace existing one.
        :param extend: (*string*) {'neither', 'both', 'min', 'max'} If not 'neither', make pointed end(s)
            for out-of- range values. These are set for a given colormap using the colormap set_under and
            set_over methods.
        :param extendrect: (*boolean*) If ``True`` the minimum and maximum colorbar extensions will be
            rectangular. If ``False`` the extensions will be triangular (the default).
        :param extendfrac: [None | 'auto' | length] If set to *None*, both the minimum and maximum triangular
            colorbar extensions with have a length of 5% of the interior colorbar length (the default). If
            set to 'auto', makes the triangular colorbar extensions the same lengths as the interior boxes
            . If a scalar, indicates the length of both the minimum and maximum triangle colorbar extensions
            as a fraction of the interior colorbar length.
        :param ticks: [None | list of ticks] If None, ticks are determined automatically from the input.
        :param ticklabels: [None | list of ticklabels] Tick labels.
        :param tickin: (*boolean*) Draw tick line inside or outside the colorbar.
        :param tickrotation: (*float*) Set tick label rotation angle.
        :param xshift: (*float*) X shift of the colorbar with pixel coordinate.
        :param yshift: (*float*) Y shift of the colorbar with pixel coordinate.
        :param vmintick: (*boolean*) Draw minimum value tick or not.
        :param vmaxtick: (*boolean*) Draw maximum value tick or not.
        :param minortick: (*boolean*) Draw minor tick line or not.
        :param minorticknum: (*int*) Minor tick line number between two adjacent major tick lines.
        """
        cmap = kwargs.pop('cmap', None)
        shrink = kwargs.pop('shrink', 1)
        orientation = kwargs.pop('orientation', 'vertical')
        aspect = kwargs.pop('aspect', 20)
        tickfontdic = kwargs.pop('tickfont', None)
        if tickfontdic is None:
            tickfont = plotutil.getfont_1(**kwargs)
        else:
            tickfont = plotutil.getfont(tickfontdic)
        exfont = False
        labelfontdic = kwargs.pop('labelfont', None)
        if labelfontdic is None:
            labfontname = kwargs.pop('labelfontname', tickfont.getName())
            if labfontname is None:
                labfontname = tickfont.getName()
            else:
                exfont = True
            labfontsize = kwargs.pop('labelfontsize', tickfont.getSize())
            labbold = kwargs.pop('labelbold', tickfont.isBold())
            if labbold:
                labelfont = Font(labfontname, Font.BOLD, labfontsize)
            else:
                labelfont = Font(labfontname, Font.PLAIN, labfontsize)
        else:
            labelfont = plotutil.getfont(labelfontdic)

        if mappable is None:
            ls = self.get_legend()
        else:
            if isinstance(mappable, MILayer):
                ls = mappable.legend
            elif isinstance(mappable, LegendScheme):
                ls = mappable
            elif isinstance(mappable, ImageGraphic):
                ls = mappable.getLegendScheme()
            elif isinstance(mappable, GraphicCollection):
                ls = mappable.getLegendScheme()
            elif isinstance(mappable, Graphic):
                ls = mappable.getLegendScheme()
            else:
                ls = plotutil.makelegend(mappable, **kwargs)

        newlegend = kwargs.pop('newlegend', True)
        if newlegend:
            legend = ChartColorBar(ls)
            self._axes.addLegend(legend)
        else:
            legend = self._axes.getLegend()
            if legend is None:
                legend = ChartColorBar(ls)
                self._axes.setLegend(legend)
            else:
                legend.setLegendScheme(ls)
        legend.setColorbar(True)
        legend.setShrink(shrink)
        legend.setAspect(aspect)
        legend.setTickLabelFont(tickfont)
        label = kwargs.pop('label', None)
        if not label is None:
            label = ChartText(label, labelfont)
            label.setUseExternalFont(exfont)
            legend.setLabel(label)
        labelloc = kwargs.pop('labelloc', None)
        if not labelloc is None:
            legend.setLabelLocation(labelloc)
        labelshift = kwargs.pop('labelshift', None)
        if not labelshift is None:
            legend.setLabelShift(labelshift)
        if orientation == 'horizontal':
            legend.setPlotOrientation(PlotOrientation.HORIZONTAL)
            legend.setPosition(LegendPosition.LOWER_CENTER_OUTSIDE)
        else:
            legend.setPlotOrientation(PlotOrientation.VERTICAL)
            legend.setPosition(LegendPosition.RIGHT_OUTSIDE)
        legend.setDrawNeatLine(False)
        extend = kwargs.pop('extend', None)
        if extend is not None:
            legend.setExtendType(extend)
        extendrect = kwargs.pop('extendrect', True)
        legend.setExtendRect(extendrect)
        extendfrac = kwargs.pop('extendfrac', None)
        if extendfrac is not None:
            if extendfrac == 'auto':
                efrac = ExtendFraction.AUTO
            else:
                efrac = ExtendFraction.LENGTH
                efrac.fraction = extendfrac
            legend.setExtendFraction(efrac)
        tickvisible = kwargs.pop('tickvisible', None)
        if not tickvisible is None:
            legend.setTickVisible(tickvisible)
        tickin = kwargs.pop('tickin', None)
        if not tickin is None:
            legend.setInsideTick(tickin)
        ticklen = kwargs.pop('ticklen', None)
        if not ticklen is None:
            legend.setTickLength(ticklen)
        if kwargs.has_key('tickwidth'):
            tickwidth = kwargs.pop('tickwidth')
            legend.setTickWidth(tickwidth)
        if kwargs.has_key('tickcolor'):
            tickcolor = kwargs.pop('tickcolor')
            tickcolor = plotutil.getcolor(tickcolor)
            legend.setTickColor(tickcolor)
        ticks = kwargs.pop('ticks', None)
        if not ticks is None:
            if isinstance(ticks, NDArray):
                ticks = ticks.aslist()
            legend.setTickLocations(ticks)
        ticklabels = kwargs.pop('ticklabels', None)
        if not ticklabels is None:
            if isinstance(ticklabels, (NDArray, DimArray)):
                ticklabels = ticklabels.aslist()
            if ls.getLegendType() == LegendType.UNIQUE_VALUE:
                legend.setTickCaptions(ticklabels)
            else:
                if isinstance(ticklabels[0], (int, long, float)):
                    legend.setTickLabels_Number(ticklabels)
                else:
                    legend.setTickLabelText(ticklabels)
        tickrotation = kwargs.pop('tickrotation', None)
        if not tickrotation is None:
            legend.setTickLabelAngle(tickrotation)
        xshift = kwargs.pop('xshift', None)
        if not xshift is None:
            legend.setXShift(xshift)
        yshift = kwargs.pop('yshift', None)
        if not yshift is None:
            legend.setYShift(yshift)
        vmintick = kwargs.pop('vmintick', False)
        vmaxtick = kwargs.pop('vmaxtick', False)
        legend.setDrawMinLabel(vmintick)
        legend.setDrawMaxLabel(vmaxtick)
        if kwargs.has_key('edgecolor'):
            edgecolor = kwargs.pop('edgecolor')
            edgecolor = plotutil.getcolor(edgecolor)
            legend.setNeatLineColor(edgecolor)
        if kwargs.has_key('edgesize'):
            edgesize = kwargs.pop('edgesize')
            legend.setNeatLineSize(edgesize)
        minortick = kwargs.pop('minortick', None)
        if not minortick is None:
            legend.setDrawMinorTick(minortick)

        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            legend.setAntiAlias(antialias)

        return legend

    def get_distance(self):
        """
        Get camera distance.
        :return: (*float*) Camera distance.
        """
        return self._axes.getDistance()

    def set_distance(self, dis):
        """
        Set camera distance.
        :param dis: (*float*) Camera distance.
        """
        self._axes.setDistance(dis)
        self.stale = True

    def get_rotation(self):
        """
        Get rotation angle.
        
        :returns: Rotation angle.
        """
        return self._axes.getAngleY()

    def set_rotation(self, rotation):
        """
        Set rotation angle.
        
        :param rotation: (*float*) Rotation angle.
        """
        self._axes.setAngleY(rotation)
        self.stale = True

    def get_elevation(self):
        """
        Get elevation angle.
        
        :returns: Elevation angle.
        """
        return self._axes.getAngleX()

    def set_elevation(self, elevation):
        """
        Set elevation angle.
        
        :param elevation: (*float*) Elevation angle.
        """
        self._axes.setAngleX(elevation)
        self.stale = True

    def set_draw_xy(self, dxy):
        """
        Set draw xy axis or not.
        
        :param dxy: (*boolean*) Draw xy axis or not.
        """
        self._axes.setDisplayXY(dxy)

    def set_draw_z(self, dz):
        """
        Set draw z axis or not.
        
        :param dz: (*boolean*) Draw z axis or not.
        """
        self._axes.setDisplayZ(dz)

    def set_draw_base(self, is_draw):
        """
        Set draw base area or not.

        :param is_draw: (*bool*) Draw base area or not.
        """
        self._axes.setDrawBase(is_draw)

    def set_draw_box(self, db):
        """
        Set draw 3D box or not.
        
        :param db: (*boolean*) Draw 3D box or not.
        """
        self._axes.setBoxed(db)

    def set_draw_bbox(self, bbox):
        """
        Set draw bounding box or not.
        
        :param bbox: (*boolean*) Draw bounding box or not.
        """
        self._axes.setDrawBoundingBox(bbox)

    def set_draw_axis(self, axis):
        """
        Set whether draw axis.

        :param axis: (*bool*) Whether draw axis.
        """
        self.set_draw_base(axis)
        self.set_draw_bbox(axis)
        self.set_draw_box(axis)
        self.set_draw_xy(axis)
        self.set_draw_z(axis)

    def set_box_color(self, color):
        """
        Set box fill color.

        :param color: (*color*) Box fill color.
        """
        color = plotutil.getcolor(color)
        self._axes.setBoxColor(color)

    @property
    def zlim(self):
        """Get or set z axis limits"""
        return self._axes.getZMin(), self._axes.getZMax()

    @zlim.setter
    def zlim(self, val):
        if not isinstance(val, (list, tuple)):
            raise ValueError("value {} is not supported, list or tuple with min and max limit values"
                             "are expected".format(val))

        zmin = val[0]
        zmax = val[1]
        if isinstance(zmin, datetime.datetime):
            zmin = miutil.date2num(zmin)
        if isinstance(zmax, datetime.datetime):
            zmax = miutil.date2num(zmax)
        self._axes.setZMinMax(zmin, zmax)
        self.stale = True

    def get_zlim(self):
        """
        Get the *z* limits of the current axes.
        
        :returns: (*tuple*) z limits.
        """
        return self._axes.getZMin(), self._axes.getZMax()

    def set_zlim(self, zmin, zmax):
        """
        Set the *z* limits of the current axes.
        
        :param zmin: (*float*) Minimum limit of the z axis.
        :param zmax: (*float*) Maximum limit of the z axis.
        """
        if isinstance(zmin, datetime.datetime):
            zmin = miutil.date2num(zmin)
        if isinstance(zmax, datetime.datetime):
            zmax = miutil.date2num(zmax)    
        self._axes.setZMinMax(zmin, zmax)

    def set_zlabel(self, label, **kwargs):
        """
        Set the z axis label of the current axes.

        :param label: (*string*) Label string.
        :param fontname: (*string*) Font name. Default is ``Arial`` .
        :param fontsize: (*int*) Font size. Default is ``14`` .
        :param bold: (*boolean*) Is bold font or not. Default is ``True`` .
        :param color: (*color*) Label string color. Default is ``black`` .
        """
        if not kwargs.has_key('xalign'):
            kwargs['xalign'] = 'center'
        if not kwargs.has_key('yalign'):
            kwargs['yalign'] = 'bottom'
        if not kwargs.has_key('rotation'):
            kwargs['rotation'] = 90
        ctext = plotutil.text(0, 0, label, **kwargs)
        axis = self._axes.getZAxis()
        axis.setLabel(ctext)
        axis.setDrawLabel(True)

    def get_zticks(self):
        """
        Get z axis tick locations.
        """
        axis = self._axes.getZAxis()
        axis.updateTickLabels()
        return axis.getTickLocations()

    def set_zticks(self, locs):
        """
        Set z axis tick locations.
        """
        axis = self._axes.getZAxis()
        if isinstance(locs, (NDArray, DimArray)):
            locs = labels.aslist()
        axis.setTickLocations(locs)

    def get_zticklabels(self):
        """
        Get z axis tick labels.
        """
        axis = self._axes.getZAxis()
        axis.updateTickLabels()
        return axis.getTickLabelText()

    def set_zticklabels(self, labels, **kwargs):
        """
        Set z axis tick labels.
        """
        axis = self._axes.getZAxis()

        if not labels is None:
            if isinstance(labels, (NDArray, DimArray)):
                labels = labels.aslist()
            if isinstance(labels[0], (int, long, float)):
                axis.setTickLabels_Number(labels)
            else:
                axis.setTickLabelText(labels)
                
        fontname = kwargs.pop('fontname', axis.getTickLabelFont().getName())
        fontsize = kwargs.pop('fontsize', axis.getTickLabelFont().getSize())
        bold =kwargs.pop('bold', axis.getTickLabelFont().isBold())
        if bold:
            font = Font(fontname, Font.BOLD, fontsize)
        else:
            font = Font(fontname, Font.PLAIN, fontsize)
        color = kwargs.pop('color', axis.getTickLabelColor())
        c = plotutil.getcolor(color)
        angle = kwargs.pop('rotation', 0)
        if angle == 'vertical':
            angle = 90
        axis.setTickLabelFont(font)
        axis.setTickLabelColor(c)
        axis.setTickLabelAngle(angle)

    def set_zaxis_type(self, axistype, timetickformat=None):
        """
        Set z axis type.

        :param axistype: (*string*) Axis type ['lon' | 'lat' | 'time' | 'log'].
        :param timetickformat: (*string*) Time tick label format.
        """
        ax = self._axes
        if axistype == 'lon':
            axis = LonLatAxis(ax.getZAxis())
            axis.setLongitude(True)
            ax.setZAxis(axis)
        elif axistype == 'lat':
            axis = LonLatAxis(ax.getZAxis())
            axis.setLongitude(False)
            ax.setZAxis(axis)
        elif axistype == 'time':
            axis = TimeAxis(ax.getZAxis())
            ax.setZAxis(axis)
            if not timetickformat is None:
                ax.getZAxis().setTimeFormat(timetickformat)
        elif axistype == 'log':
            axis = LogAxis(ax.getZAxis())
            axis.setMinorTickNum(10)
            ax.setZAxis(axis)
        else:
            axis = Axis(ax.getZAxis())
            ax.setZAxis(axis)

    def zaxis(self, **kwargs):
        """
        Set z axis of the axes.

        :param color: (*Color*) Color of the z axis. Default is 'black'.
        :param shift: (*int) z axis shif along horizontal direction. Units is pixel. Default is 0.
        """
        visible = kwargs.pop('visible', None)
        shift = kwargs.pop('shift', None)
        color = kwargs.pop('color', None)
        if not color is None:
            color = plotutil.getcolor(color)
        linewidth = kwargs.pop('linewidth', None)
        linestyle = kwargs.pop('linestyle', None)
        tickcolor = kwargs.pop('tickcolor', None)
        if not tickcolor is None:
            tickcolor = plotutil.getcolor(tickcolor)
        tickline = kwargs.pop('tickline', None)
        tickline = kwargs.pop('tickvisible', tickline)
        tickwidth = kwargs.pop('tickwidth', None)
        ticklabel = kwargs.pop('ticklabel', None)
        minortick = kwargs.pop('minortick', False)
        minorticknum = kwargs.pop('minorticknum', 5)
        tickin = kwargs.pop('tickin', True)
        axistype = kwargs.pop('axistype', None)
        ticklabelcolor= kwargs.pop('ticklabelcolor', None)
        if not ticklabelcolor is None:
            ticklabelcolor = plotutil.getcolor(ticklabelcolor)
        tickfontname = kwargs.pop('tickfontname', 'Arial')
        tickfontsize = kwargs.pop('tickfontsize', 14)
        tickbold = kwargs.pop('tickbold', False)
        if tickbold:
            font = Font(tickfontname, Font.BOLD, tickfontsize)
        else:
            font = Font(tickfontname, Font.PLAIN, tickfontsize)
        axislist = []
        axislist.append(self._axes.getZAxis())
        for axis in axislist:
            if not visible is None:
                axis.setVisible(visible)
            if not shift is None:
                axis.setShift(shift)
            if not color is None:
                axis.setColor_All(color)
            if not tickcolor is None:
                axis.setTickColor(tickcolor)
            if not linewidth is None:
                axis.setLineWidth(linewidth)
            if not linestyle is None:
                axis.setLineStyle(linestyle)
            if not tickline is None:
                axis.setDrawTickLine(tickline)
            if not tickwidth is None:
                axis.setTickWidth(tickwidth)
            if not ticklabel is None:
                axis.setDrawTickLabel(ticklabel)
            axis.setMinorTickVisible(minortick)
            axis.setMinorTickNum(minorticknum)
            axis.setInsideTick(tickin)
            if not ticklabelcolor is None:
                axis.setTickLabelColor(ticklabelcolor)
            axis.setTickLabelFont(font)

    def bar3(self, *args, **kwargs):
        """
        Make a 3D bar plot of x, y and z, where x, y and z are sequence like objects of the same lengths.

        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param z: (*array_like*) Input z data.
        :param width: (*float*) Bar width.
        :param cylinder: (*bool*) Is cylinder bar or rectangle bar.
        :param bottom: (*bool*) Color of the points. Or z values.
        :param facecolor: (*Color*) Optional, the color of the bar faces.
        :param edgecolor: (*Color*) Optional, the color of the bar edge. Default is black color.
            Edge line will not be plotted if ``edgecolor`` is ``None``.
        :param linewidth: (*int*) Optional, width of bar edge.
        :param label: (*string*) Label of the bar series.
        :param hatch: (*string*) Hatch string.
        :param hatchsize: (*int*) Hatch size. Default is None (8).
        :param bgcolor: (*Color*) Background color, only valid with hatch.
        :param barswidth: (*float*) Bars width (0 - 1), only used for automatic bar with plot
            (only one argument without ``width`` argument). Default is 0.8.

        :returns: Bar 3D graphics.
        """
        # Add data series
        label = kwargs.pop('label', 'S_0')
        if len(args) == 1:
            z = np.asarray(args[0])
            if z.ndim == 1:
                nx, = z.shape
                y = np.array([0] * nx)
                x = np.arange(nx)
            else:
                ny, nx = z.shape
                x = np.arange(nx)
                y = np.arange(ny)
                x, y = np.meshgrid(x, y)
        elif len(args) == 2:
            x = np.asarray(args[0])
            z = np.asarray(args[1])
            nx, = x.shape
            y = np.array([0] * nx)
        else:
            x = np.asarray(args[0])
            y = np.asarray(args[1])
            z = np.asarray(args[2])

        xdata = x._array
        ydata = y._array
        zdata = z._array

        autowidth = False
        width = kwargs.pop('width', 0.8)
        width = np.asarray(width)

        bottom = kwargs.pop('bottom', None)
        if not bottom is None:
            bottom = plotutil.getplotdata(bottom)

        cylinder = kwargs.pop('cylinder', False)

        # Set plot data styles
        cdata = kwargs.pop('cdata', None)
        if cdata is None:
            fcobj = kwargs.pop('color', None)
            if fcobj is None:
                fcobj = kwargs.pop('facecolor', 'b')
            if isinstance(fcobj, (tuple, list)):
                colors = plotutil.getcolors(fcobj)
            else:
                color = plotutil.getcolor(fcobj)
                colors = [color]
            ecobj = kwargs.pop('edgecolor', 'k')
            edgecolor = plotutil.getcolor(ecobj)
            linewidth = kwargs.pop('linewidth', 1.0)
            hatch = kwargs.pop('hatch', None)
            hatch = plotutil.gethatch(hatch)
            hatchsize = kwargs.pop('hatchsize', None)
            bgcolor = kwargs.pop('bgcolor', None)
            bgcolor = plotutil.getcolor(bgcolor)
            ecolor = kwargs.pop('ecolor', 'k')
            ecolor = plotutil.getcolor(ecolor)
            barbreaks = []
            for color in colors:
                lb = BarBreak()
                lb.setCaption(label)
                lb.setColor(color)
                if edgecolor is None:
                    lb.setDrawOutline(False)
                else:
                    lb.setOutlineColor(edgecolor)
                lb.setOutlineSize(linewidth)
                if not hatch is None:
                    lb.setStyle(hatch)
                    if not bgcolor is None:
                        lb.setBackColor(bgcolor)
                    if not hatchsize is None:
                        lb.setStyleSize(hatchsize)
                lb.setErrorColor(ecolor)
                barbreaks.append(lb)

            # Create bar graphics
            if isinstance(width, NDArray):
                width = width.asarray()

            if cylinder:
                graphics = GraphicFactory.createCylinderBars3D(xdata, ydata, zdata, autowidth, width, bottom, barbreaks)
            else:
                graphics = GraphicFactory.createBars3D(xdata, ydata, zdata, autowidth, width, bottom, barbreaks)
        else:
            if isinstance(cdata, (list, tuple)):
                cdata = np.array(cdata)

            levels = kwargs.pop('levels', None)
            if levels is None:
                cnum = kwargs.pop('cnum', None)
                if cnum is None:
                    ls = plotutil.getlegendscheme([], cdata.min(), cdata.max(), **kwargs)
                else:
                    ls = plotutil.getlegendscheme([cnum], cdata.min(), cdata.max(), **kwargs)
            else:
                ls = plotutil.getlegendscheme([levels], cdata.min(), cdata.max(), **kwargs)

            ls = plotutil.setlegendscheme_polygon(ls, **kwargs)

            # Create bar graphics
            if isinstance(width, NDArray):
                width = width.asarray()

            if cylinder:
                graphics = GraphicFactory.createCylinderBars3D(xdata, ydata, zdata, cdata._array, autowidth, width, bottom, ls)
            else:
                graphics = GraphicFactory.createBars3D(xdata, ydata, zdata, cdata._array, autowidth, width, bottom, ls)

        self.add_graphic(graphics)

        return graphics

    def plot3(self, x, y, z, *args, **kwargs):
        """
        Plot 3D lines and/or markers to the axes. *args* is a variable length argument, allowing
        for multiple *x, y* pairs with an optional format string.
        
        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param z: (*array_like*) Input z data.
        :param cdata: (*array_like*) Optional, data for colors.
        :param style: (*string*) Line style for plot.
        
        :returns: Legend breaks of the lines.
        
        The following format string characters are accepted to control the line style or marker:
        
          =========  ===========
          Character  Description
          =========  ===========
          '-'         solid line style
          '--'        dashed line style
          '-.'        dash-dot line style
          ':'         dotted line style
          '.'         point marker
          ','         pixel marker
          'o'         circle marker
          'v'         triangle_down marker
          '^'         triangle_up marker
          '<'         triangle_left marker
          '>'         triangle_right marker
          's'         square marker
          'p'         pentagon marker
          '*'         star marker
          'x'         x marker
          'D'         diamond marker
          =========  ===========
          
        The following color abbreviations are supported:
          
          =========  =====
          Character  Color  
          =========  =====
          'b'        blue
          'g'        green
          'r'        red
          'c'        cyan
          'm'        magenta
          'y'        yellow
          'k'        black
          =========  =====
        """      
        xdata = np.asarray(x)
        ydata = np.asarray(y)
        zdata = np.asarray(z)
        style = None
        if len(args) > 0:
            style = args[0]
        
        #Set plot data styles
        label = kwargs.pop('label', 'S_1')
        mvalues = kwargs.pop('mvalues', None)
        cdata = kwargs.pop('cdata', mvalues)
        if cdata is None:
            if style is None:
                line = plotutil.getlegendbreak('line', **kwargs)[0]
                line.setCaption(label)
            else:
                line = plotutil.getplotstyle(style, label, **kwargs)
            colors = kwargs.pop('colors', None)
            if not colors is None:
                colors = plotutil.getcolors(colors)
                cbs = []
                for color in colors:
                    cb = line.clone()
                    cb.setColor(color)
                    cbs.append(cb)
        else:
            ls = kwargs.pop('symbolspec', None)
            if ls is None:        
                if isinstance(cdata, (list, tuple)):
                    cdata = np.array(cdata)
                levels = kwargs.pop('levs', None)
                if levels is None:
                    levels = kwargs.pop('levels', None)
                if levels is None:
                    cnum = kwargs.pop('cnum', None)
                    if cnum is None:
                        ls = plotutil.getlegendscheme([], cdata.min(), cdata.max(), **kwargs)
                    else:
                        ls = plotutil.getlegendscheme([cnum], cdata.min(), cdata.max(), **kwargs)
                else:
                    ls = plotutil.getlegendscheme([levels], cdata.min(), cdata.max(), **kwargs)
                ls = plotutil.setlegendscheme_line(ls, **kwargs)

        #Add graphics
        if ydata.ndim == 1 and ydata.contains_nan():
            xdata, ydata, zdata = np.split_by_nan_multi(xdata, ydata, zdata)
            if cdata is not None:
                cdata = np.split_array_by_nan(cdata)

        if cdata is None:
            if colors is None:
                graphics = LineCollection(None, xyz_data=(xdata, ydata, zdata), legend=line)
            else:
                graphics = LineCollection(None, xyz_data=(xdata, ydata, zdata), legend=cbs)
        else:
            graphics = LineCollection(None, xyz_data=(xdata, ydata, zdata), cdata=cdata, legend=ls)

        #Pipe
        pipe = kwargs.pop('pipe', False)
        if pipe:
            radius = kwargs.pop('radius', 0.02)
            steps = kwargs.pop('steps', 48)
            graphics = GraphicFactory.lineString3DToPipe(graphics, radius, steps)

        lighting = kwargs.pop('lighting', None)
        if not lighting is None:
            graphics.setUsingLight(lighting)

        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics

    def scatter3(self, x, y, z, s=8, c='b', marker='o', **kwargs):
        """
        Make a 3D scatter plot of x, y and z, where x, y and z are sequence like objects of the same lengths.
        
        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param z: (*array_like*) Input z data.
        :param s: (*int*) Size of points.
        :param c: (*color or array*) Color of the points. Or data values for colors.
        :param alpha: (*int*) The alpha blending value, between 0 (transparent) and 1 (opaque).
        :param marker: (*string*) Marker of the points.
        :param label: (*string*) Label of the point series.
        :param levels: (*array_like*) Optional. A list of floating point numbers indicating the level
            points to draw, in increasing order.
        :param sphere: (*bool*) Draw point as sphere or not. Default is `None` that means `False`.
        
        :returns: Point 3D graphics.
        """        
        #Add data series
        label = kwargs.pop('label', 'S_0')
        xdata = np.asarray(x)
        ydata = np.asarray(y)
        if z is None:
            zdata = None
        elif isinstance(z, numbers.Number):
            zdata = np.full(xdata.shape, z)
        else:
            zdata = np.asarray(z)
        
        #Set plot data styles
        pb, isunique = plotutil.getlegendbreak('point', **kwargs)
        pb.setCaption(label)
        pstyle = plotutil.getpointstyle(marker)    
        pb.setStyle(pstyle)
        is_value = False
        if isinstance(c, (list, tuple, NDArray)):
            c = np.asarray(c)
            if c.shape == xdata.shape and c.dtype.is_numeric():
                is_value = True

        if is_value:
            ls = kwargs.pop('symbolspec', None)
            if ls is None:        
                if isinstance(c, (list, tuple)):
                    c = np.array(c)
                levels = kwargs.pop('levs', None)
                if levels is None:
                    levels = kwargs.pop('levels', None)
                if levels is None:
                    cnum = kwargs.pop('cnum', None)
                    if cnum is None:
                        ls = plotutil.getlegendscheme([], c.min(), c.max(), **kwargs)
                    else:
                        ls = plotutil.getlegendscheme([cnum], c.min(), c.max(), **kwargs)
                else:
                    ls = plotutil.getlegendscheme([levels], c.min(), c.max(), **kwargs)
                ls = plotutil.setlegendscheme_point(ls, **kwargs)
                if isinstance(s, int):
                    for lb in ls.getLegendBreaks():
                        lb.setSize(s)
                else:
                    n = len(s)
                    for i in range(0, n):
                        ls.getLegendBreaks()[i].setSize(s[i])
            #Create graphics
            #graphics = GraphicFactory.createPoints3D(xdata._array, ydata._array, zdata._array, c._array, ls)
            graphics = PointCollection(xdata, ydata, zdata, c, legend=ls)
        else:
            alpha = kwargs.pop('alpha', None)
            colors = plotutil.getcolors(c, alpha)   
            pbs = []
            if isinstance(s, int):   
                pb.setSize(s)
                if len(colors) == 1:
                    pb.setColor(colors[0])
                    pbs.append(pb)
                else:
                    n = len(colors)
                    for i in range(0, n):
                        npb = pb.clone()
                        npb.setColor(colors[i])
                        pbs.append(npb)
            else:
                n = len(s)
                if len(colors) == 1:
                    pb.setColor(colors[0])
                    for i in range(0, n):
                        npb = pb.clone()
                        npb.setSize(s[i])
                        pbs.append(npb)
                else:
                    for i in range(0, n):
                        npb = pb.clone()
                        npb.setSize(s[i])
                        npb.setColor(colors[i])
                        pbs.append(npb)
            #Create graphics
            #graphics = GraphicFactory.createPoints3D(xdata._array, ydata._array, zdata._array, pbs)
            graphics = PointCollection(xdata, ydata, zdata, legend=pbs)

        sphere = kwargs.pop('sphere', None)
        if not sphere is None:
            graphics.setSphere(sphere)

        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics

    def mesh(self, *args, **kwargs):
        """
        creates a three-dimensional surface mesh plot

        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) 2-D z value array.
        :param cmap: (*string*) Color map string.

        :returns: Legend
        """
        if len(args) <= 2:
            z = args[0]
            if isinstance(z, DimArray):
                x = args[0].dimvalue(1)
                y = args[0].dimvalue(0)
            else:
                ny, nx = z.shape
                x = np.arange(nx)
                y = np.arange(ny)
            x, y = np.meshgrid(x, y)
            args = args[1:]
        else:
            x = args[0]
            y = args[1]
            z = args[2]
            args = args[3:]

        if 'colors' in kwargs.keys():
            cn = len(kwargs['colors'])
        else:
            cn = None
        cmap = plotutil.getcolormap(**kwargs)
        if len(args) > 0:
            level_arg = args[0]
            if isinstance(level_arg, int):
                cn = level_arg
                ls = LegendManage.createLegendScheme(z.min(), z.max(), cn, cmap)
            else:
                if isinstance(level_arg, NDArray):
                    level_arg = level_arg.aslist()
                ls = LegendManage.createLegendScheme(z.min(), z.max(), level_arg, cmap)
        else:
            if cn is None:
                ls = LegendManage.createLegendScheme(z.min(), z.max(), cmap)
            else:
                ls = LegendManage.createLegendScheme(z.min(), z.max(), cn, cmap)
        # ls = ls.convertTo(GeometryType.POLYGON, True)
        ls = ls.convertTo(GeometryType.POLYGON)
        face_interp = None
        if 'facecolor' in kwargs.keys():
            facecolor = kwargs.pop('facecolor', None)
            face_interp = (facecolor == 'interp')
            if not face_interp:
                if facecolor not in ['flat', 'texturemap', 'none']:
                    kwargs['facecolor'] = facecolor
        else:
            kwargs['facecolor'] = None
        edgecolor = kwargs.pop('edgecolor', None)
        edge_interp = None
        if edgecolor is not None:
            edge_interp = (edgecolor == 'interp')
            if not edge_interp:
                if edgecolor not in ['flat', 'texturemap', 'none']:
                    kwargs['edgecolor'] = edgecolor
        plotutil.setlegendscheme(ls, **kwargs)
        graphics = GraphicFactory.surface(x.asarray(), y.asarray(), z.asarray(), ls)
        graphics.setMesh(True)
        if face_interp:
            graphics.setFaceInterp(face_interp)
        if edge_interp:
            graphics.setEdgeInterp(edge_interp)
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics

    def plot_wireframe(self, *args, **kwargs):
        """
        creates a three-dimensional wireframe plot
        
        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) 2-D z value array.
        :param cmap: (*string*) Color map string.
        :param xyaxis: (*boolean*) Draw x and y axis or not.
        :param zaxis: (*boolean*) Draw z axis or not.
        :param grid: (*boolean*) Draw grid or not.
        :param boxed: (*boolean*) Draw boxed or not.
        :param mesh: (*boolean*) Draw mesh line or not.
        
        :returns: Legend
        """
        warnings.warn("plot_wireframe is deprecated", DeprecationWarning)
        if len(args) == 1:
            x = args[0].dimvalue(1)
            y = args[0].dimvalue(0)
            x, y = np.meshgrid(x, y)
            z = args[0]    
            args = args[1:]
        else:
            x = args[0]
            y = args[1]
            z = args[2]
            args = args[3:]
 
        zcolors = kwargs.pop('zcolors', False)
        if not zcolors:
            line = plotutil.getlegendbreak('line', **kwargs)[0]
            graphics = GraphicFactory.createWireframe(x.asarray(), y.asarray(), z.asarray(), line)
        else:
            cmap = plotutil.getcolormap(**kwargs)
            if len(args) > 0:
                level_arg = args[0]
                if isinstance(level_arg, int):
                    cn = level_arg
                    ls = LegendManage.createLegendScheme(z.min(), z.max(), cn, cmap)
                else:
                    if isinstance(level_arg, NDArray):
                        level_arg = level_arg.aslist()
                    ls = LegendManage.createLegendScheme(z.min(), z.max(), level_arg, cmap)
            else:    
                ls = LegendManage.createLegendScheme(z.min(), z.max(), cmap)
            ls = ls.convertTo(GeometryType.LINESTRING)
            plotutil.setlegendscheme(ls, **kwargs)
            graphics = GraphicFactory.createWireframe(x.asarray(), y.asarray(), z.asarray(), ls)
        
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics

    def surf(self, *args, **kwargs):
        """
        creates a three-dimensional surface plot

        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) 2-D z value array.
        :param cmap: (*string*) Color map string.
        :param lighting: (*bool*) Using light or not.

        :returns: 3D surface graphic
        """
        if len(args) <= 2:
            z = args[0]
            if isinstance(z, DimArray):
                x = args[0].dimvalue(1)
                y = args[0].dimvalue(0)
            else:
                ny, nx = z.shape
                x = np.arange(nx)
                y = np.arange(ny)
            x, y = np.meshgrid(x, y)
            args = args[1:]
        else:
            x = args[0]
            y = args[1]
            z = args[2]
            args = args[3:]

        if kwargs.has_key('colors'):
            cn = len(kwargs['colors'])
        else:
            cn = None
        alpha = None
        if kwargs.has_key('alpha'):
            alpha = kwargs['alpha']
        cmap = plotutil.getcolormap(**kwargs)
        level_arg = None
        C = None
        min = z.min()
        max = z.max()
        if len(args) > 0:
            if isinstance(args[0], NDArray):
                if args[0].shape == z.shape:
                    C = args[0]
                    min = C.min()
                    max = C.max()
                    if len(args) > 1:
                        level_arg = args[1]
                elif args[0].ndim == 3:
                    C = args[0]
                else:
                    level_arg = args[0]
            else:
                level_arg = args[0]

        facecolor = kwargs.pop('facecolor', None)
        if facecolor == 'texturemap':
            cdata = kwargs.pop('cdata')
            if isinstance(cdata, NDArray) and cdata.ndim == 2:
                min = cdata.min()
                max = cdata.max()

        if not level_arg is None:
            if isinstance(level_arg, int):
                cn = level_arg
                ls = LegendManage.createLegendScheme(min, max, cn, cmap)
            else:
                if isinstance(level_arg, NDArray):
                    level_arg = level_arg.aslist()
                ls = LegendManage.createLegendScheme(min, max, level_arg, cmap)
        else:
            if cn is None:
                ls = LegendManage.createLegendScheme(min, max, cmap)
            else:
                ls = LegendManage.createLegendScheme(min, max, cn, cmap)

        ls = ls.convertTo(GeometryType.POLYGON)

        face_interp = False
        image = None
        if not facecolor is None:
            face_interp = (facecolor == 'interp')
            if not face_interp:
                if facecolor == 'texturemap':
                    if isinstance(cdata, NDArray):
                        if cdata.ndim == 3:
                            if alpha is None:
                                image = ImageUtil.createImage(cdata._array)
                            else:
                                image = ImageUtil.createImage(cdata._array, alpha)
                        else:
                            image = GraphicFactory.createImage(cdata._array, ls)
                    elif isinstance(cdata, BufferedImage):
                        image = cdata
                    elif isinstance(cdata, GraphicCollection):
                        image = cdata.getGraphicN(0).getGeometry().getImage()
                    else:
                        image = cdata.getGeometry().getImage()
                elif not facecolor in ['flat', 'none']:
                    facecolor = plotutil.getcolor(facecolor)
                    ls = LegendManage.createSingleSymbolLegendScheme(GeometryType.POLYGON, facecolor, 1)
                    face_interp = True
        plotutil.setlegendscheme(ls, **kwargs)
        if C is None:
            graphics = GraphicFactory.surface(x.asarray(), y.asarray(), z.asarray(), ls)
        else:
            graphics = GraphicFactory.surface(x.asarray(), y.asarray(), z.asarray(), C.asarray(), ls)

        if not image is None:
            graphics.setImage(image)

        if face_interp:
            graphics.setFaceInterp(face_interp)

        lighting = kwargs.pop('lighting', None)
        if not lighting is None:
            graphics.setUsingLight(lighting)

        model = kwargs.pop('model', False)
        if model:
            graphics = ModelGraphic(graphics)

        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)

        return graphics

    def plot_surface(self, *args, **kwargs):
        """
        creates a three-dimensional surface plot
        
        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) 2-D z value array.
        :param cmap: (*string*) Color map string.
        
        :returns: Legend
        """
        return self.surf(*args, **kwargs)

    def contour3(self, *args, **kwargs):
        """
        3-D contour plot.

        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) 2-D z value array.
        :param levels: (*array_like*) Optional. A list of floating point numbers indicating the level curves
            to draw, in increasing order.
        :param cmap: (*string*) Color map string.
        :param colors: (*list*) If None (default), the colormap specified by cmap will be used. If a
            string, like ‘r’ or ‘red’, all levels will be plotted in this color. If a tuple of matplotlib
            color args (string, float, rgb, etc), different levels will be plotted in different colors in
            the order specified.
        :param smooth: (*boolean*) Smooth contour lines or not.

        :returns: (*graphics*) Contour graphics created from array data.
        """
        n = len(args)
        cmap = plotutil.getcolormap(**kwargs)
        fill_value = kwargs.pop('fill_value', -9999.0)
        offset = kwargs.pop('offset', 0)
        if n <= 2:
            z = args[0]
            if isinstance(z, DimArray):
                y = z.dimvalue(0)
                x = z.dimvalue(1)
            else:
                x = np.arange(z.shape[1])
                y = np.arange(z.shape[0])
            args = args[1:]
        else:
            x = args[0]
            y = args[1]
            z = args[2]
            args = args[3:]

        if x.ndim == 2:
            x = x[0]
        if y.ndim == 2:
            y = y[:,0]

        if len(args) > 0:
            level_arg = args[0]
            if isinstance(level_arg, int):
                cn = level_arg
                ls = LegendManage.createLegendScheme(z.min(), z.max(), cn, cmap)
            else:
                if isinstance(level_arg, NDArray):
                    level_arg = level_arg.aslist()
                ls = LegendManage.createLegendScheme(z.min(), z.max(), level_arg, cmap)
        else:
            ls = LegendManage.createLegendScheme(z.min(), z.max(), cmap)
        ls = ls.convertTo(GeometryType.LINESTRING)
        plotutil.setlegendscheme(ls, **kwargs)

        smooth = kwargs.pop('smooth', False)
        igraphic = GraphicFactory.createContourLines3D(x.asarray(), y.asarray(), z.asarray(), ls, smooth)

        lighting = kwargs.pop('lighting', None)
        if not lighting is None:
            igraphic.setUsingLight(lighting)

        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(igraphic)
        return igraphic

    def quiver3(self, *args, **kwargs):
        """
        Plot a 3-D field of arrows.
        
        :param x: (*array_like*) X coordinate array.
        :param y: (*array_like*) Y coordinate array.
        :param z: (*array_like*) Z coordinate array.
        :param u: (*array_like*) U component of the arrow vectors (wind field).
        :param v: (*array_like*) V component of the arrow vectors (wind field).
        :param w: (*array_like*) W component of the arrow vectors (wind field).
        :param z: (*array_like*) Optional, 2-D z value array.
        :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level 
            vectors to draw, in increasing order.
        :param cmap: (*string*) Color map string.
        :param fill_value: (*float*) Fill_value. Default is ``-9999.0``.
        :param scale: (*float*) The length scale of each quiver, default to 1.0, the unit is
            the same with the axes.
        :param headwidth: (*float*) Arrow head width, default is 1.
        :param headlength: (*float*) Arrow head length, default is 2.5.
        :param arrowstyle: (*string*) Arrow head style, ``'line'`` (short line
            segments, default) or ``'cone'``.
        
        :returns: (*Graphic list*) Created quiver graphics.
        """
        ls = kwargs.pop('symbolspec', None)
        cmap = plotutil.getcolormap(**kwargs)
        fill_value = kwargs.pop('fill_value', -9999.0)
        n = len(args) 
        iscolor = False
        cdata = None
        xaxistype = None
        x = args[0]
        y = args[1]
        z = args[2]
        u = args[3]
        v = args[4]
        w = args[5]
        args = args[6:]
        if len(args) > 0:
            cdata = args[0]
            iscolor = True
            args = args[1:]
        x = plotutil.getplotdata(x)
        y = plotutil.getplotdata(y)
        z = plotutil.getplotdata(z)
        u = plotutil.getplotdata(u)
        v = plotutil.getplotdata(v)   
        w = plotutil.getplotdata(w)
        
        if ls is None:
            if iscolor:
                if len(args) > 0:
                    cn = args[0]
                    ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), cn, cmap)
                else:
                    levs = kwargs.pop('levs', None)
                    if levs is None:
                        ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), cmap)
                    else:
                        if isinstance(levs, NDArray):
                            levs = levs.tolist()
                        ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), levs, cmap)
            else:    
                if cmap.getColorCount() == 1:
                    c = cmap.getColor(0)
                else:
                    c = Color.black
                ls = LegendManage.createSingleSymbolLegendScheme(GeometryType.POINT, c, 10)
            ls = plotutil.setlegendscheme_point(ls, **kwargs)
        
        if not cdata is None:
            cdata = plotutil.getplotdata(cdata)
        scale = kwargs.pop('scale', 1)
        headwidth = kwargs.pop('headwidth', 3)
        headlength = kwargs.pop('headlength', 4)
        arrowstyle = kwargs.pop('arrowstyle', 'line')
        if not isinstance(arrowstyle, basestring):
            raise ValueError("arrowstyle must be 'line' or 'cone'")
        arrowstyle = arrowstyle.lower()
        if arrowstyle not in ('line', 'cone'):
            raise ValueError("arrowstyle must be 'line' or 'cone'")
        line_head = arrowstyle == 'line'
        igraphic = GraphicFactory.createArrows3D(x, y, z, u, v, w, scale, headwidth,
                                                 headlength, cdata, ls, line_head)

        lighting = kwargs.pop('lighting', None)
        if not lighting is None:
            igraphic.setUsingLight(lighting)

        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(igraphic)
        return igraphic

    def geoshow(self, layer, **kwargs):
        """
        Plot a layer in 3D axes.

        :param layer: (*MILayer*) The layer to be plotted.

        :returns: Graphics.
        """
        ls = kwargs.pop('symbolspec', None)
        offset = kwargs.pop('offset', 0)
        xshift = kwargs.pop('xshift', 0)

        if isinstance(layer, basestring):
            fn = layer
            encoding = kwargs.pop('encoding', None)
            layer = migeo.georead(fn, encoding)

        layer = layer._layer
        if layer.getLayerType() == LayerTypes.VECTOR_LAYER:
            if ls is None:
                ls = layer.getLegendScheme()
                if layer.getLegendScheme().getBreakNum() == 1:
                    lb = layer.getLegendScheme().getLegendBreaks().get(0)
                    btype = lb.getBreakType()
                    geometry = 'point'
                    if btype == BreakTypes.POLYLINE_BREAK:
                        geometry = 'line'
                    elif btype == BreakTypes.POLYGON_BREAK:
                        geometry = 'polygon'
                        if not kwargs.has_key('facecolor'):
                            kwargs['facecolor'] = None
                        if not kwargs.has_key('edgecolor'):
                            kwargs['edgecolor'] = 'k'
                    lb, isunique = plotutil.getlegendbreak(geometry, **kwargs)
                    ls.getLegendBreaks().set(0, lb)
            else:
                plotutil.setlegendscheme(ls, **kwargs)
            layer.setLegendScheme(ls)
            graphics = GraphicUtil.layerToGraphics(layer, offset, xshift)
        else:
            nlat = kwargs.pop('nlat', 180)
            nlon = kwargs.pop('nlon', 360)
            if self._axes.getProjInfo() is None:
                graphics = GraphicFactory.geoSurface(layer.getImage(), layer.getExtent(), offset, xshift, nlon, nlat)
            else:
                limits = kwargs.pop('limits', None)
                if limits is None:
                    graphics = GraphicFactory.geoSurface(layer.getImage(), layer.getExtent(), offset, xshift, nlon, nlat, self._axes.getProjInfo())
                else:
                    graphics = GraphicFactory.geoSurface(layer.getImage(), layer.getExtent(), offset, xshift, nlon, nlat, self._axes.getProjInfo(),
                                                         limits)

        lighting = kwargs.pop('lighting', None)
        if not lighting is None:
            graphics.setUsingLight(lighting)

        visible = kwargs.pop('visible', True)
        if visible:
            if hasattr(self, 'projection'):
                data_proj = kwargs.pop('transform', layer.getProjInfo())
                transform = GeoTransform(data_proj, self.projection)
                graphics.transform = transform
            self.add_graphic(graphics)

        return graphics

    def plot_layer(self, layer, **kwargs):
        """
        Plot a layer in 3D axes.
        
        :param layer: (*MILayer*) The layer to be plotted.
        
        :returns: Graphics.
        """
        return self.geoshow(layer, **kwargs)

    @property
    def rotation(self):
        """Get or set rotation angle"""
        return self._axes.getAngleY()

    @rotation.setter
    def rotation(self, val):
        self._axes.setAngleY(val)
        self.stale = True

    @property
    def elevation(self):
        """Get or set elevation angle"""
        return self._axes.getAngleX()

    @elevation.setter
    def elevation(self, val):
        self._axes.setAngleX(val)
        self.stale = True

    def view(self, *args):
        """
        Set camera line of sight.

        view(az, el)

        view(dim)

        :param az: (*float*) Azimuth angle.
        :param el: (*float*) Elevation angle.
        :param dim: (*int*) Uses the default line of sight for 2-D or 3-D plots. Specify dim as 2
            for the default 2-D view or 3 for the default 3-D view.

        :return: Current azimuth and elevation angle.
        """
        if len(args) == 1:
            dim = args[0]
            if dim == 2:
                self.set_rotation(0)
                self.set_elevation(0)
            elif dim == 3:
                self.set_rotation(45)
                self.set_elevation(-45)
        elif len(args) == 2:
            az = args[0]
            el = args[1]
            self.set_rotation(-az)
            self.set_elevation(el - 90)

        az = -self.get_rotation()
        el = self.get_elevation() + 90

        return az, el

    @property
    def head_angle(self):
        """Get or set head angle"""
        return self._axes.getHeadAngle()

    @head_angle.setter
    def head_angle(self, head):
        self._axes.setHeadAngle(head)
        self.stale = True

    def get_head(self):
        """
        Get head angle.

        :return: (*float*) Head angle
        """
        return self._axes.getHeadAngle()

    def set_head(self, head):
        """
        Set head angle.

        :param head: (*float*) Head angle
        """
        self._axes.setHeadAngle(head)

    @property
    def pitch_angle(self):
        """Get or set pitch angle"""
        return self._axes.getPitchAngle()

    @pitch_angle.setter
    def pitch_angle(self, pitch):
        self._axes.setPitchAngle(pitch)
        self.stale = True

    def get_pitch(self):
        """
        Get pitch angle.

        :return: (*float*) Pitch angle
        """
        return self._axes.getPitchAngle()

    def set_pitch(self, pitch):
        """
        Set pitch angle.

        :param pitch: (*float*) Pitch angle.
        """
        self._axes.setPitchAngle(pitch)

    @property
    def zscale(self):
        """Get or set z axis scale"""
        return self._axes.getZScale()

    @zscale.setter
    def zscale(self, scale):
        self._axes.setZScale(scale)
        self.stale = True

    def get_zscale(self):
        """
        Get z axis scale.

        :return: (*float*) Z axis scale.
        """
        return self._axes.getZScale()

    def set_zscale(self, zscale):
        """
        Set z axis scale.

        :param zscale: (*float*) Z axis scale.
        """
        self._axes.setZScale(zscale)

    def set_background(self, color):
        """
        Set background color.

        :param color: (*color*) Background color.
        """
        color = plotutil.getcolor(color)
        self._axes.setBackground(color)

    @property
    def antialias(self):
        """Get or set antialias"""
        return self._axes.isAntialias()

    @antialias.setter
    def antialias(self, val):
        self._axes.setAntialias(val)
        self.stale = True

    def get_antialias(self):
        """
        Get antialias
        :return: (*bool*) Antialias or not.
        """
        return self._axes.isAntialias()

    def set_antialias(self, antialias):
        """
        Set antialias.
        :param antialias: (*bool*) Antialias or not.
        """
        self._axes.setAntialias(antialias)

    @property
    def orthographic(self):
        """Get or set orthographic"""
        return self._axes.isOrthographic()

    @orthographic.setter
    def orthographic(self, val):
        self._axes.setOrthographic(val)
        self.stale = True

    def get_orthographic(self):
        """
        Get orthographic.
        :return: (*bool*) Orthographic or not.
        """
        return self._axes.isOrthographic()

    def set_orthographic(self, orthographic):
        """
        Set orthographic.
        :param orthographic: (*bool*) Orthographic or not.
        """
        self._axes.setOrthographic(orthographic)

    @property
    def distance(self):
        """Get or set camera distance"""
        return self._axes.getDistance()

    @distance.setter
    def distance(self, val):
        self._axes.setDistance(val)
        self.stale = True

    @property
    def fov(self):
        """
        Get field of view angles in degrees for perspective projection.

        :return: (*float*) Field of view angles in degrees.
        """
        return self._axes.getFieldOfView()

    @fov.setter
    def fov(self, fov):
        """
        Set field of view angles in degrees for perspective projection.

        :param fov: (*float*) Field of view angles in degrees with the range of (0, 180).
        """
        if fov <= 0 or fov >= 180:
            warnings.warn('Field of view angles should be in the range of (0, 180)')
        else:
            self._axes.setFieldOfView(fov)
            self.stale = True

    def set_lighting(self, enable=True, **kwargs):
        """
        Set lighting.
        
        :param enable: (*boolean*) Set lighting enable or not.
        :param position: (*list of float*) Lighting position. Default is [0,0,1,0].
        :param ambient: (*list of float*) Ambient light. Default is [0.2,0.2,0.2,1].
        :param diffuse: (*list of float*) Diffuse light.  Default is [1,1,1,1].
        :param specular: (*list of float*) Specular light. Default is [1,1,1,1].
        :param mat_ambient: (*list of float*) Material ambient light. Default is [0.2,0.2,0.2,1].
        :param mat_diffuse: (*list of float*) Material diffuse light. Default is [0.8,0.8,0.8,1].
        :param mat_specular: (*list of float*) Material specular light. Default is [0,0,0,1].
        :param mat_emission: (*list of float*) Material emission light. Default is [0,0,0,1].
        :param mat_shininess: (*float*) Material shininess (0 - 128). Default is 50.
        """
        lighting = self._axes.getLighting()
        lighting.setEnable(enable)
        position = kwargs.pop('position', None)
        if not position is None:
            lighting.setPosition(position)
        ambient = kwargs.pop('ambient', None)
        if not ambient is None:
            lighting.setAmbient(ambient)
        diffuse = kwargs.pop('diffuse', None)
        if not diffuse is None:
            lighting.setDiffuse(diffuse)
        specular = kwargs.pop('specular', None)
        if not specular is None:
            lighting.setSpecular(specular)
        mat_ambient = kwargs.pop('mat_ambient', None)
        if not mat_ambient is None:
            lighting.setMaterialAmbient(mat_ambient)
        mat_diffuse = kwargs.pop('mat_diffuse', None)
        if not mat_diffuse is None:
            lighting.setMaterialDiffuse(mat_diffuse)
        mat_specular = kwargs.pop('mat_specular', None)
        if not mat_specular is None:
            lighting.setMaterialSpecular(mat_specular)
        mat_emission = kwargs.pop('mat_emission', None)
        if not mat_emission is None:
            lighting.setMaterialEmission(mat_emission)
        mat_shininess = kwargs.pop('mat_shininess', None)
        if not mat_shininess is None:
            lighting.setMaterialShininess(mat_shininess)

        self.stale = True

    def material(self, mvalues):
        """
        Set reflectance properties of surfaces and patches.

        :param mvalues: (*list*) Material value list. Sets the ambient/diffuse/specular strength,
            specular exponent, and specular color reflectance of the objects.
        """
        lighting = self._axes.getLighting()
        lighting.setMaterialAmbient(mvalues[0])
        if len(mvalues) > 1:
            lighting.setMaterialDiffuse(mvalues[1])
        if len(mvalues) > 2:
            lighting.setMaterialSpecular(mvalues[2])
        if len(mvalues) > 3:
            lighting.setMaterialShininess(mvalues[3])
        if len(mvalues) > 4:
            lighting.setMaterialEmission(mvalues[4])
        self.stale = True

    def add_zaxis(self, x, y, left=True):
        """
        Add a z axis.

        :param x: (*float*) X coordinate of the z axis.
        :param y: (*float*) Y coordinate of the z axis.
        :param left: (*boolean*) Whether left tick. Default is True.
        """
        self._axes.addZAxis(x, y, left)
        self.stale = True

    def streamplot3(self, *args, **kwargs):
        """
        Plot streamlines in 3D axes.

        :param x: (*array_like*) X coordinate array.
        :param y: (*array_like*) Y coordinate array.
        :param z: (*array_like*) Z coordinate array.
        :param u: (*array_like*) U component of the arrow vectors (wind field).
        :param v: (*array_like*) V component of the arrow vectors (wind field).
        :param w: (*array_like*) W component of the arrow vectors (wind field).
        :param density: (*int*) Streamline density. Default is 4.

        :return: Streamlines
        """
        ls = kwargs.pop('symbolspec', None)
        cmap = plotutil.getcolormap(**kwargs)
        density = kwargs.pop('density', 4)
        iscolor = False
        cdata = None
        if len(args) < 6:
            u = args[0]
            v = args[1]
            w = args[2]
            u = np.asarray(u)
            nz, ny, nx = u.shape
            x = np.arange(nx)
            y = np.arange(ny)
            z = np.arange(nz)
            args = args[3:]
        else:
            x = args[0]
            y = args[1]
            z = args[2]
            u = args[3]
            v = args[4]
            w = args[5]
            args = args[6:]
        if len(args) > 0:
            cdata = args[0]
            iscolor = True
            args = args[1:]
        x = plotutil.getplotdata(x)
        y = plotutil.getplotdata(y)
        z = plotutil.getplotdata(z)
        u = plotutil.getplotdata(u)
        v = plotutil.getplotdata(v)
        w = plotutil.getplotdata(w)

        if ls is None:
            if iscolor:
                if len(args) > 0:
                    cn = args[0]
                    ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), cn, cmap)
                else:
                    levs = kwargs.pop('levs', None)
                    if levs is None:
                        ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), cmap)
                    else:
                        if isinstance(levs, NDArray):
                            levs = levs.tolist()
                        ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), levs, cmap)
            else:
                if cmap.getColorCount() == 1:
                    c = cmap.getColor(0)
                else:
                    c = Color.black
                ls = LegendManage.createSingleSymbolLegendScheme(GeometryType.LINESTRING, c, 1)
            ls = plotutil.setlegendscheme_line(ls, **kwargs)

        if not kwargs.has_key('headwidth'):
            kwargs['headwidth'] = 1
        if not kwargs.has_key('headlength'):
            kwargs['headlength'] = 2.5 * kwargs['headwidth']
        for i in range(ls.getBreakNum()):
            lb = plotutil.line2stream(ls.getLegendBreak(i), **kwargs)
            ls.setLegendBreak(i, lb)

        if not cdata is None:
            cdata = plotutil.getplotdata(cdata)

        min_points = kwargs.pop('min_points', 3)
        nloop = kwargs.pop('nloop', 1000)
        start_x = kwargs.pop('start_x', None)
        start_y = kwargs.pop('start_y', None)
        start_z = kwargs.pop('start_z', None)
        if start_x is None or start_y is None or start_z is None:
            graphics = GraphicFactory.createStreamlines3D(x, y, z, u, v, w, cdata, density, ls, min_points, nloop)
        else:
            start_x = np.asarray(start_x).flatten()
            start_y = np.asarray(start_y).flatten()
            start_z = np.asarray(start_z).flatten()
            graphics = GraphicFactory.createStreamlines3D(x, y, z, u, v, w, cdata, density, ls, min_points, nloop,
                                                          start_x._array, start_y._array, start_z._array)
        lighting = kwargs.pop('lighting', None)
        if not lighting is None:
            graphics.setUsingLight(lighting)

        # Pipe
        pipe = kwargs.pop('pipe', False)
        if pipe:
            radius = kwargs.pop('radius', 0.02)
            steps = kwargs.pop('steps', 48)
            graphics = GraphicFactory.lineString3DToPipe(graphics, radius, steps)

        self.add_graphic(graphics)

        return graphics

    def streamslice(self, *args, **kwargs):
        """
        Plot streamlines slice in 3D axes.

        :param x: (*array_like*) X coordinate array.
        :param y: (*array_like*) Y coordinate array.
        :param z: (*array_like*) Z coordinate array.
        :param u: (*array_like*) U component of the arrow vectors (wind field).
        :param v: (*array_like*) V component of the arrow vectors (wind field).
        :param w: (*array_like*) W component of the arrow vectors (wind field).
        :param xslice: (*list*) X slice locations.
        :param yslice: (*list*) Y slice locations.
        :param zslice: (*list*) Z slice locations.
        :param density: (*int*) Streamline density. Default is 4.

        :return: Streamline slices
        """
        ls = kwargs.pop('symbolspec', None)
        cmap = plotutil.getcolormap(**kwargs)
        density = kwargs.pop('density', 4)
        iscolor = False
        cdata = None
        if len(args) < 6:
            u = args[0]
            v = args[1]
            w = args[2]
            u = np.asarray(u)
            nz, ny, nx = u.shape
            x = np.arange(nx)
            y = np.arange(ny)
            z = np.arange(nz)
            args = args[3:]
        else:
            x = args[0]
            y = args[1]
            z = args[2]
            u = args[3]
            v = args[4]
            w = args[5]
            args = args[6:]
        if len(args) > 0:
            cdata = args[0]
            iscolor = True
            args = args[1:]
        x = plotutil.getplotdata(x)
        y = plotutil.getplotdata(y)
        z = plotutil.getplotdata(z)
        u = plotutil.getplotdata(u)
        v = plotutil.getplotdata(v)
        w = plotutil.getplotdata(w)

        if ls is None:
            if iscolor:
                if len(args) > 0:
                    cn = args[0]
                    if isinstance(cn, NDArray):
                        cn = cn.aslist()
                    ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), cn, cmap)
                else:
                    levs = kwargs.pop('levels', None)
                    if levs is None:
                        ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), cmap)
                    else:
                        if isinstance(levs, NDArray):
                            levs = levs.tolist()
                        ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), levs, cmap)
            else:
                if cmap.getColorCount() == 1:
                    c = cmap.getColor(0)
                else:
                    c = Color.black
                ls = LegendManage.createSingleSymbolLegendScheme(GeometryType.LINESTRING, c, 1)
            ls = plotutil.setlegendscheme_line(ls, **kwargs)

        if not kwargs.has_key('headwidth'):
            kwargs['headwidth'] = 1
        if not kwargs.has_key('headlength'):
            kwargs['headlength'] = 2.5
        for i in range(ls.getBreakNum()):
            lb = plotutil.line2stream(ls.getLegendBreak(i), **kwargs)
            ls.setLegendBreak(i, lb)

        if not cdata is None:
            cdata = plotutil.getplotdata(cdata)

        min_points = kwargs.pop('min_points', 3)
        zslice_index = kwargs.pop('zslice_index', None)
        if zslice_index is None:
            xslice = kwargs.pop('xslice', [])
            if isinstance(xslice, numbers.Number):
                xslice = [xslice]
            yslice = kwargs.pop('yslice', [])
            if isinstance(yslice, numbers.Number):
                yslice = [yslice]
            zslice = kwargs.pop('zslice', [])
            if isinstance(zslice, numbers.Number):
                zslice = [zslice]
            graphics = GraphicFactory.streamSlice(x, y, z, u, v, w, cdata, xslice, yslice, zslice, density, ls)
        else:
            if isinstance(zslice_index, int):
                zslice_index = [zslice_index]
            graphics = GraphicFactory.streamSlice(x, y, z, u, v, w, cdata, zslice_index, density, ls)

        xyslice = kwargs.pop('xyslice', None)
        if not xyslice is None:
            method = kwargs.pop('method', 'nearest')
            method = InterpolationMethod.valueOf(method.upper())
            gg = GraphicFactory.streamSlice(x, y, z, u, v, w, cdata, xyslice, method, density, ls)
            graphics.append(gg)

        lighting = kwargs.pop('lighting', None)
        if not lighting is None:
            for gg in graphics:
                gg.setUsingLight(lighting)
        visible = kwargs.pop('visible', True)
        if visible:
            for gg in graphics:
                self.add_graphic(gg)

        return graphics

    def slice(self, *args, **kwargs):
        """
        Volume slice planes

        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) Optional. Z coordinate array.
        :param data: (*array_like*) 3D data array.
        :param xslice: (*list*) X slice locations.
        :param yslice: (*list*) Y slice locations.
        :param zslice: (*list*) Z slice locations.
        :param cmap: (*string*) Color map string.

        :return: Slice plane graphics.
        """
        if len(args) <= 3:
            x = args[0].dimvalue(2)
            y = args[0].dimvalue(1)
            z = args[0].dimvalue(0)
            data = args[0]
            args = args[1:]
        else:
            x = args[0]
            y = args[1]
            z = args[2]
            data = args[3]
            args = args[4:]

        x = np.asarray(x)
        y = np.asarray(y)
        z = np.asarray(z)
        if x.ndim == 3:
            x = x[0, 0]
        if y.ndim == 3:
            y = y[0, :, 0]
        if z.ndim == 3:
            z = z[:, 0, 0]

        cmap = plotutil.getcolormap(**kwargs)
        if len(args) > 0:
            level_arg = args[0]
            if isinstance(level_arg, int):
                cn = level_arg
                ls = LegendManage.createLegendScheme(data.min(), data.max(), cn, cmap)
            else:
                if isinstance(level_arg, NDArray):
                    level_arg = level_arg.aslist()
                ls = LegendManage.createLegendScheme(data.min(), data.max(), level_arg, cmap)
        else:
            ls = LegendManage.createLegendScheme(data.min(), data.max(), cmap)
        ls = ls.convertTo(GeometryType.POLYGON)
        facecolor = kwargs.pop('facecolor', None)
        face_interp = None
        if not facecolor is None:
            face_interp = (facecolor == 'interp')
            if not face_interp:
                if not facecolor in ['flat', 'texturemap', 'none']:
                    facecolor = plotutil.getcolor(facecolor)
                    ls = LegendManage.createSingleSymbolLegendScheme(GeometryType.POLYGON, facecolor, 1)
        plotutil.setlegendscheme(ls, **kwargs)

        xslice = kwargs.pop('xslice', [])
        if isinstance(xslice, numbers.Number):
            xslice = [xslice]
        yslice = kwargs.pop('yslice', [])
        if isinstance(yslice, numbers.Number):
            yslice = [yslice]
        zslice = kwargs.pop('zslice', [])
        if isinstance(zslice, numbers.Number):
            zslice = [zslice]
        if isinstance(xslice, NDArray):
            graphics = GraphicFactory.slice(data.asarray(), x.asarray(), y.asarray(), z.asarray(),
                                            xslice._array, yslice._array, zslice._array, ls)
        else:
            graphics = GraphicFactory.slice(data.asarray(), x.asarray(), y.asarray(), z.asarray(), xslice,
                                            yslice, zslice, ls)

        xyslice = kwargs.pop('xyslice', None)
        if not xyslice is None:
            method = kwargs.pop('method', 'nearest')
            method = InterpolationMethod.valueOf(method.upper())
            gg = GraphicFactory.slice(data.asarray(), x.asarray(), y.asarray(), z.asarray(),
                                      xyslice, ls, method)
            graphics.append(gg)

        if face_interp:
            for gg in graphics:
                gg.setFaceInterp(face_interp)
        lighting = kwargs.pop('lighting', None)
        if not lighting is None:
            for gg in graphics:
                gg.setUsingLight(lighting)
        visible = kwargs.pop('visible', True)
        if visible:
            for gg in graphics:
                self.add_graphic(gg)
        return graphics

    def plot_slice(self, *args, **kwargs):
        """
        Volume slice planes
        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) Optional. Z coordinate array.
        :param data: (*array_like*) 3D data array.
        :param xslice: (*list*) X slice locations.
        :param yslice: (*list*) Y slice locations.
        :param zslice: (*list*) Z slice locations.
        :param cmap: (*string*) Color map string.
        :return:
        """
        return self.slice(*args, **kwargs)

    def contourslice(self, *args, **kwargs):
        """
        Volume slice contours

        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) Optional. Z coordinate array.
        :param data: (*array_like*) 3D data array.
        :param xslice: (*list*) X slice locations.
        :param yslice: (*list*) Y slice locations.
        :param zslice: (*list*) Z slice locations.
        :param cmap: (*string*) Color map string.
        :param smooth: (*bool*) Smooth contour lines or not.
        :return: Contour slice graphics
        """
        if len(args) <= 3:
            x = args[0].dimvalue(2)
            y = args[0].dimvalue(1)
            z = args[0].dimvalue(0)
            data = args[0]
            args = args[1:]
        else:
            x = args[0]
            y = args[1]
            z = args[2]
            data = args[3]
            args = args[4:]
        if x.ndim == 3:
            x = x[0, 0]
        if y.ndim == 3:
            y = y[0, :, 0]
        if z.ndim == 3:
            z = z[:, 0, 0]

        cmap = plotutil.getcolormap(**kwargs)
        if len(args) > 0:
            level_arg = args[0]
            if isinstance(level_arg, int):
                cn = level_arg
                ls = LegendManage.createLegendScheme(data.min(), data.max(), cn, cmap)
            else:
                if isinstance(level_arg, NDArray):
                    level_arg = level_arg.aslist()
                ls = LegendManage.createLegendScheme(data.min(), data.max(), level_arg, cmap)
        else:
            ls = LegendManage.createLegendScheme(data.min(), data.max(), cmap)
        ls = ls.convertTo(GeometryType.LINESTRING)
        plotutil.setlegendscheme(ls, **kwargs)

        xslice = kwargs.pop('xslice', [])
        if isinstance(xslice, numbers.Number):
            xslice = [xslice]
        yslice = kwargs.pop('yslice', [])
        if isinstance(yslice, numbers.Number):
            yslice = [yslice]
        zslice = kwargs.pop('zslice', [])
        if isinstance(zslice, numbers.Number):
            zslice = [zslice]
        graphics = []
        if isinstance(xslice, NDArray):
            smooth = kwargs.pop('smooth', False)
            gg = GraphicFactory.contourSlice(data.asarray(), x.asarray(), y.asarray(), z.asarray(),
                                                   xslice._array, yslice._array, zslice._array, ls, smooth)
            graphics.append(gg)
        else:
            smooth = kwargs.pop('smooth', True)
            graphics = GraphicFactory.contourSlice(data.asarray(), x.asarray(), y.asarray(), z.asarray(),
                                                   xslice, yslice, zslice, ls, smooth)

        xyslice = kwargs.pop('xyslice', None)
        if not xyslice is None:
            method = kwargs.pop('method', 'nearest')
            method = InterpolationMethod.valueOf(method.upper())
            gg = GraphicFactory.contourSlice(data.asarray(), x.asarray(), y.asarray(), z.asarray(),
                                             xyslice, method, ls, smooth)
            if not gg is None:
                graphics.append(gg)

        lighting = kwargs.pop('lighting', None)
        if not lighting is None:
            for gg in graphics:
                gg.setUsingLight(lighting)
        visible = kwargs.pop('visible', True)
        if visible:
            for gg in graphics:
                self.add_graphic(gg)
        return graphics

    def contourfslice(self, *args, **kwargs):
        """
        Volume slice contour polygons
        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) Optional. Z coordinate array.
        :param data: (*array_like*) 3D data array.
        :param xslice: (*list*) X slice locations.
        :param yslice: (*list*) Y slice locations.
        :param zslice: (*list*) Z slice locations.
        :param cmap: (*string*) Color map string.
        :param smooth: (*bool*) Smooth contour lines or not.
        :return: Contour polygon slice graphics
        """
        if len(args) <= 3:
            x = args[0].dimvalue(2)
            y = args[0].dimvalue(1)
            z = args[0].dimvalue(0)
            data = args[0]
            args = args[1:]
        else:
            x = args[0]
            y = args[1]
            z = args[2]
            data = args[3]
            args = args[4:]
        if x.ndim == 3:
            x = x[0, 0]
        if y.ndim == 3:
            y = y[0, :, 0]
        if z.ndim == 3:
            z = z[:, 0, 0]

        cmap = plotutil.getcolormap(**kwargs)
        if len(args) > 0:
            level_arg = args[0]
            if isinstance(level_arg, int):
                cn = level_arg
                ls = LegendManage.createLegendScheme(data.min(), data.max(), cn, cmap)
            else:
                if isinstance(level_arg, NDArray):
                    level_arg = level_arg.aslist()
                ls = LegendManage.createLegendScheme(data.min(), data.max(), level_arg, cmap)
        else:
            ls = LegendManage.createLegendScheme(data.min(), data.max(), cmap)
        ls = ls.convertTo(GeometryType.POLYGON)
        plotutil.setlegendscheme(ls, **kwargs)

        xslice = kwargs.pop('xslice', [])
        if isinstance(xslice, numbers.Number):
            xslice = [xslice]
        yslice = kwargs.pop('yslice', [])
        if isinstance(yslice, numbers.Number):
            yslice = [yslice]
        zslice = kwargs.pop('zslice', [])
        if isinstance(zslice, numbers.Number):
            zslice = [zslice]
        smooth = kwargs.pop('smooth', True)
        graphics = GraphicFactory.contourfSlice(data.asarray(), x.asarray(), y.asarray(), z.asarray(), xslice, \
                                                yslice, zslice, ls, smooth)

        xyslice = kwargs.pop('xyslice', None)
        if not xyslice is None:
            method = kwargs.pop('method', 'nearest')
            method = InterpolationMethod.valueOf(method.upper())
            gg = GraphicFactory.contourfSlice(data.asarray(), x.asarray(), y.asarray(), z.asarray(),
                                              xyslice, method, ls, smooth)
            if not gg is None:
                graphics.append(gg)

        lighting = kwargs.pop('lighting', None)
        if not lighting is None:
            for gg in graphics:
                gg.setUsingLight(lighting)
        visible = kwargs.pop('visible', True)
        if visible:
            for gg in graphics:
                self.add_graphic(gg)
        return graphics

    def meshc(self, *args, **kwargs):
        """
        Contour plot under mesh surface plot.

        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) 2-D z value array.
        :param cmap: (*string*) Color map string.

        :returns: 3D mesh and contour graphics.
        """
        if len(args) <= 2:
            z = args[0]
            if isinstance(z, DimArray):
                x = args[0].dimvalue(1)
                y = args[0].dimvalue(0)
            else:
                ny, nx = z.shape
                x = np.arange(nx)
                y = np.arange(ny)
            x, y = np.meshgrid(x, y)
            args1 = [x, y, z] + args[1:]
        else:
            args1 = args

        gmesh = self.mesh(*args1, **kwargs)
        zmin = args1[2].min()

        kwargs['offset'] = zmin
        args1 = args1[:3]
        gcontour = self.contour(*args1, **kwargs)

        return gmesh, gcontour

    def surfc(self, *args, **kwargs):
        """
        Contour plot under surface plot.

        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) 2-D z value array.
        :param cmap: (*string*) Color map string.
        :param lighting: (*bool*) Using light or not.

        :returns: 3D surface and contour graphics.
        """
        if len(args) <= 2:
            z = args[0]
            if isinstance(z, DimArray):
                x = args[0].dimvalue(1)
                y = args[0].dimvalue(0)
            else:
                ny, nx = z.shape
                x = np.arange(nx)
                y = np.arange(ny)
            x, y = np.meshgrid(x, y)
            args1 = [x, y, z] + args[1:]
        else:
            args1 = args

        gsurf = self.surf(*args1, **kwargs)
        zmin = args1[2].min()

        kwargs['offset'] = zmin
        args1 = args1[:3]
        gcontour = self.contour(*args1, **kwargs)

        return gsurf, gcontour

    def isosurface(self, *args, **kwargs):
        """
        creates a three-dimensional isosurface plot.

        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) Optional. Z coordinate array.
        :param data: (*array_like*) Volume data array.
        :param isovalue: (*float*) Specified isosurface value.
        :param cdata: (*array_like*) Optional. Volume color data array.
        :param facecolor: (*color*) Optional. Color map string.
        :param cmap: (*string*) Optional. Color map string.
        :param nthread: (*int*) Optional. Thread number. Default is 4.

        :returns: 3D Mesh graphic.
        """
        if len(args) <= 3:
            x = args[0].dimvalue(2)
            y = args[0].dimvalue(1)
            z = args[0].dimvalue(0)
            data = args[0]
            isovalue = args[1]
            args = args[2:]
        else:
            x = args[0]
            y = args[1]
            z = args[2]
            if x.ndim == 3:
                x = x[0, 0]
            if y.ndim == 3:
                y = y[0, :, 0]
            if z.ndim == 3:
                z = z[:, 0, 0]
            data = args[3]
            isovalue = args[4]
            args = args[5:]

        cdata = None
        if len(args) > 0:
            if isinstance(args[0], NDArray) and args[0].shape == data.shape:
                cdata = args[0]
                args = args[1:]

        facecolor = kwargs.pop('facecolor', 'c')

        if cdata is not None:
            cmap = plotutil.getcolormap(**kwargs)
            if len(args) > 0:
                level_arg = args[0]
                if isinstance(level_arg, int):
                    cn = level_arg
                    ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), cn, cmap)
                else:
                    if isinstance(level_arg, NDArray):
                        level_arg = level_arg.aslist()
                    ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), level_arg, cmap)
            else:
                ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), cmap)
            ls = ls.convertTo(GeometryType.POLYGON)
        else:
            facecolor = plotutil.getcolor(facecolor)
            ls = LegendManage.createSingleSymbolLegendScheme(GeometryType.POLYGON, facecolor, 1)

        if not kwargs.has_key('edgecolor'):
            kwargs['edgecolor'] = None
        plotutil.setlegendscheme(ls, **kwargs)

        nthread = kwargs.pop('nthread', 4)
        if nthread is None:
            graphics = GraphicFactory.isosurface(data.asarray(), x.asarray(), y.asarray(), z.asarray(), isovalue, ls)
        else:
            data = data.asarray().copyIfView()
            x = x.asarray().copyIfView()
            y = y.asarray().copyIfView()
            z = z.asarray().copyIfView()
            if cdata is None:
                graphics = GraphicFactory.isosurface(data, x, y, z, isovalue, ls, nthread)
            else:
                cdata = cdata.asarray().copyIfView()
                graphics = GraphicFactory.isosurface(data, x, y, z, isovalue, cdata, ls, nthread)

        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics

    def plot_isosurface(self, *args, **kwargs):
        """
        creates a three-dimensional isosurface plot

        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) Optional. Z coordinate array.
        :param data: (*array_like*) 3D data array.
        :param cmap: (*string*) Color map string.
        :param nthread: (*int*) Thread number. Default is 4.

        :returns: 3D Mesh graphic
        """
        return self.isosurface(*args, **kwargs)

    def trisurf(self, T, x, y, z, normal=None, model=False, **kwargs):
        """
        Triangular surface plot.

        :param T: (*array*) Triangle connectivity, specified as a 3-column matrix where each
            row contains the point vertices defining a triangle face.
        :param x: (*array*) X coordinates array.
        :param y: (*array*) Y coordinates array.
        :param z: (*array*) Z coordinates array.
        :param normal: (*array*) Normal array. Default is `None`.
        :param model: (*bool*) Create model array or not. Default is `False`.

        :return: Triangle mesh graphic.
        """
        facecolor = kwargs.pop('facecolor', 'c')
        facecolor = plotutil.getcolor(facecolor)
        ls = LegendManage.createSingleSymbolLegendScheme(GeometryType.POLYGON, facecolor, 1)

        if not kwargs.has_key('edgecolor'):
            kwargs['edgecolor'] = None
        plotutil.setlegendscheme(ls, **kwargs)

        if normal is None:
            graphics = GraphicFactory.triSurface(T._array, x._array, y._array, z._array, ls)
        else:
            graphics = GraphicFactory.triSurface(T._array, x._array, y._array, z._array,
                                                 normal._array, ls)

        if model:
            graphics = ModelGraphic(graphics)

        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)

        return graphics

    def model(self, T, x, y, z, normal=None, **kwargs):
        """
        Model plot.

        :param T: (*array*) Triangle connectivity, specified as a 3-column matrix where each
            row contains the point vertices defining a triangle face.
        :param x: (*array*) X coordinates array.
        :param y: (*array*) Y coordinates array.
        :param z: (*array*) Z coordinates array.
        :param normal: (*array*) Normal array. Default is `None`.

        :return: Triangle mesh graphic.
        """
        facecolor = kwargs.pop('facecolor', 'c')
        facecolor = plotutil.getcolor(facecolor)
        ls = LegendManage.createSingleSymbolLegendScheme(GeometryType.POLYGON, facecolor, 1)

        if 'edgecolor' not in kwargs.keys():
            kwargs['edgecolor'] = None
        plotutil.setlegendscheme(ls, **kwargs)

        if normal is None:
            graphics = GraphicFactory.triSurface(T._array, x._array, y._array, z._array, ls)
        else:
            graphics = GraphicFactory.triSurface(T._array, x._array, y._array, z._array,
                                                 normal._array, ls)

        graphics = ModelGraphic(graphics)

        location = kwargs.pop('location', None)
        if location is not None:
            graphics.setLocation(location)
        angle = kwargs.pop('angle', None)
        if angle is not None:
            graphics.setAngle(angle)
        scale = kwargs.pop('scale', None)
        if scale is not None:
            graphics.setScale(scale)

        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)

        return graphics

    def fimplicit3(self, f, interval=[-5., 5.], mesh_density=35, *args, **kwargs):
        """
        Plot the 3-D implicit function defined by f(x,y,z) = 0 over the default interval [-5, 5] for x, y, and z.

        :param f: (*function*) The 3-D implicit function defined by f(x,y,z).
        :param interval: (*list*) Optional. the plotting interval for x, y, and z. Default is [-5.,5.]. Length
            2 or 6, 2 means x, y, and z use same interval.
        :param mesh_density: (*int*) Optional. Number of evaluation points per direction. Default is 35.
        :param cmap: (*string*) Color map string.
        :param nthread: (*int*) Thread number. Default is 4.

        :returns: 3D Mesh graphic
        """
        if len(interval) == 2:
            interval = interval * 3

        a = np.linspace(interval[0], interval[1], mesh_density)
        b = np.linspace(interval[2], interval[3], mesh_density)
        c = np.linspace(interval[4], interval[5], mesh_density)
        x, y, z = np.meshgrid(a, b, c)
        v = f(x, y, z)

        if not kwargs.has_key('edgecolor'):
            kwargs['edgecolor'] = 'k'

        if kwargs.has_key('facecolor'):
            return self.isosurface(a, b, c, v, 0, *args, **kwargs)
        else:
            return self.isosurface(a, b, c, v, 0, z, *args, **kwargs)

    def particles(self, *args, **kwargs):
        """
        creates a three-dimensional particles plot

        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) Optional. Z coordinate array.
        :param data: (*array_like*) 3D data array.
        :param s: (*float*) Point size.
        :param cmap: (*string*) Color map string.
        :param vmin: (*float*) Minimum value for particle plotting.
        :param vmax: (*float*) Maximum value for particle plotting.
        :param alpha_min: (*float*) Minimum alpha value. Default is 0.1.
        :param alpha_max: (*float*) Maximum alpha value. Default is 0.6.
        :param density: (*int*) Particle density value. Default is 2.

        :returns: Legend
        """
        if len(args) <= 3:
            x = args[0].dimvalue(2)
            y = args[0].dimvalue(1)
            z = args[0].dimvalue(0)
            data = args[0]
            args = args[1:]
        else:
            x = args[0]
            y = args[1]
            z = args[2]
            data = args[3]
            args = args[4:]
        cmap = plotutil.getcolormap(**kwargs)
        vmin = kwargs.pop('vmin', data.min())
        vmax = kwargs.pop('vmax', data.max())
        if vmin >= vmax:
            raise ValueError("Minimum value larger than maximum value")

        if len(args) > 0:
            level_arg = args[0]
            if isinstance(level_arg, int):
                cn = level_arg
                ls = LegendManage.createLegendScheme(vmin, vmax, cn, cmap)
            else:
                if isinstance(level_arg, NDArray):
                    level_arg = level_arg.aslist()
                ls = LegendManage.createLegendScheme(vmin, vmax, level_arg, cmap)
        else:
            ls = LegendManage.createLegendScheme(vmin, vmax, cmap)
        plotutil.setlegendscheme(ls, **kwargs)
        alpha_min = kwargs.pop('alpha_min', 0.1)
        alpha_max = kwargs.pop('alpha_max', 0.6)
        density = kwargs.pop('density', 2)
        graphics = GraphicFactory.particles(data.asarray(), x.asarray(), y.asarray(), z.asarray(), ls, \
                                            alpha_min, alpha_max, density)
        s = kwargs.pop('s', None)
        if s is None:
            s = kwargs.pop('size', None)
        if not s is None:
            graphics.setPointSize(s)
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics

    def plot_particles(self, *args, **kwargs):
        """
        creates a three-dimensional particles plot

        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) Optional. Z coordinate array.
        :param data: (*array_like*) 3D data array.
        :param s: (*float*) Point size.
        :param cmap: (*string*) Color map string.
        :param vmin: (*float*) Minimum value for particle plotting.
        :param vmax: (*float*) Maximum value for particle plotting.
        :param alpha_min: (*float*) Minimum alpha value.
        :param alpha_max: (*float*) Maximum alpha value.
        :param density: (*int*) Particle density value.

        :returns: Legend
        """
        return self.particles(*args, **kwargs)

    def volumeplot(self, *args, **kwargs):
        """
        creates a three-dimensional volume plot

        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) Optional. Z coordinate array.
        :param data: (*array_like*) 3D data array.
        :param cmap: (*string*) Color map string.
        :param vmin: (*float*) Minimum value for particle plotting.
        :param vmax: (*float*) Maximum value for particle plotting.
        :param ray_casting: (*str*) Ray casting algorithm ['basic' | 'max_value' | 'specular'].
            Default is 'max_value'.
        :param brightness: (*float*) Volume brightness. Default is 1.
        :param alpha_min: (*float*) Minimum alpha value. Default is 0.
        :param alpha_max: (*float*) Maximum alpha value. Default is 1.
        :param opacity_nodes: (*list of float*) Opacity nodes. Default is None.
        :param opacity_levels: (*list of float*) Opacity levels. Default is [0., 1.].

        :returns: Volumeplot graphic
        """
        if len(args) <= 3:
            data = args[0]
            if isinstance(data, DimArray):
                x = data.dimvalue(2)
                y = data.dimvalue(1)
                z = data.dimvalue(0)
            else:
                nz = data.shape[0]
                ny = data.shape[1]
                nx = data.shape[2]
                x = np.arange(nx)
                y = np.arange(ny)
                z = np.arange(nz)
            args = args[1:]
        else:
            x = args[0]
            y = args[1]
            z = args[2]
            data = args[3]
            args = args[4:]
        vmin = kwargs.pop('vmin', data.min())
        vmax = kwargs.pop('vmax', data.max())
        if vmin >= vmax:
            raise ValueError("Minimum value larger than maximum value")

        alpha_min = kwargs.pop('alpha_min', 0.0)
        alpha_max = kwargs.pop('alpha_max', 1.0)
        cmap = plotutil.getcolormap(**kwargs)
        norm = kwargs.pop('norm', colors.Normalize(vmin, vmax, clip=True))
        if len(args) > 0:
            level_arg = args[0]
            if isinstance(level_arg, int):
                cn = level_arg
                ls = LegendManage.createLegendScheme(vmin, vmax, cn, cmap)
            else:
                if isinstance(level_arg, NDArray):
                    level_arg = level_arg.aslist()
                ls = LegendManage.createLegendScheme(vmin, vmax, level_arg, cmap)
            plotutil.setlegendscheme(ls, **kwargs)
            ls.setNormalize(norm._norm)
            ls.setColorMap(cmap)
            graphics = GraphicFactory.volume(data.asarray(), x.asarray(), y.asarray(), z.asarray(), ls, \
                                             alpha_min, alpha_max)
        else:
            opacity_nodes = kwargs.pop('opacity_nodes', None)
            opacity_levels = kwargs.pop('opacity_levels', [0., 1.])
            graphics = GraphicFactory.volume(data.asarray(), x.asarray(), y.asarray(), z.asarray(), cmap, \
                                             norm._norm, opacity_nodes, opacity_levels)

        ray_casting = kwargs.pop('ray_casting', None)
        if not ray_casting is None:
            graphics.setRayCastingType(ray_casting)
        brightness = kwargs.pop('brightness', None)
        if not brightness is None:
            graphics.setBrightness(brightness)
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics

    def view_form(self):
        """
        Open GLForm
        """
        form = GLForm(self._axes)
        form.setSize(600, 500)
        form.setLocationRelativeTo(None)
        form.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
        form.setVisible(True)

class MapAxes(Axes):
    """
    Map 3D axes.
    """

    def __init__(self, *args, **kwargs):
        super(MapAxes, self).__init__(*args, **kwargs)

        projection = kwargs.pop('projection', None)
        if not projection is None:
            self._axes.setProjInfo(projection)

    def _set_plot(self, plot):
        """
        Set plot.

        :param plot: (*EarthPlot3D*) Plot.
        """
        if plot is None:
            self._axes = MapGLPlot()
        else:
            self._axes = plot

    @property
    def axestype(self):
        return '3d'

    @property
    def projection(self):
        return self._axes.getProjInfo()


class EarthAxes(Axes):
    """
    Earth spherical 3D axes.

    Graphics are given in longitude / latitude / altitude coordinates and are drawn on
    the earth surface. `volumeplot` draws a volume above the earth surface: the volume
    is ray cast against the spherical shell between the minimum and maximum altitude of
    the z coordinates, so it follows the curvature of the earth.
    """

    def __init__(self, *args, **kwargs):
        kwargs['aspect'] = 'equal'
        if 'facecolor' not in kwargs.keys():
            kwargs['facecolor'] = 'k'
        kwargs['clip_plane'] = False
        kwargs['axis'] = False
        if 'distance' not in kwargs.keys():
            kwargs['distance'] = 500
        super(EarthAxes, self).__init__(*args, **kwargs)

        image = kwargs.pop('image', 'world_topo.jpg')
        if image is not None:
            if not os.path.exists(image):
                image = os.path.join(migl.get_map_folder(), image)
            if os.path.exists(image):
                self._axes.earthImage(image)

    def _set_plot(self, plot):
        """
        Set plot.

        :param plot: (*EarthPlot3D*) Plot.
        """
        if plot is None:
            self._axes = EarthGLPlot()
        else:
            self._axes = plot

    @property
    def axestype(self):
        return '3d'

    def earth_image(self, image):
        """
        Set earth surface image.
        :param image: (*str*) Earth image file path.
        """
        if not os.path.exists(image):
            image = os.path.join(migl.get_map_folder(), image)
        if os.path.exists(image):
            self._axes.earthImage(image)

    def volumeplot(self, *args, **kwargs):
        """
        Creates a three-dimensional volume plot above the earth surface.

        The z coordinate is the altitude above the earth surface in km (the same length
        unit as the earth radius), and the volume is ray cast against the spherical
        shell between z.min() and z.max(), so the drawn volume follows the curvature of
        the earth.

        :param x: (*array_like*) Optional. Longitude coordinate array.
        :param y: (*array_like*) Optional. Latitude coordinate array.
        :param z: (*array_like*) Optional. Altitude coordinate array in km.
        :param data: (*array_like*) 3D data array.
        :param cmap: (*string*) Color map string.
        :param vmin: (*float*) Minimum value for volume plotting.
        :param vmax: (*float*) Maximum value for volume plotting.
        :param ray_casting: (*str*) Ray casting algorithm ['basic' | 'max_value' | 'specular'].
            Default is 'max_value'.
        :param brightness: (*float*) Volume brightness. Default is 1.
        :param opacity_nodes: (*list of float*) Opacity nodes. Default is None.
        :param opacity_levels: (*list of float*) Opacity levels. Default is [0., 1.].

        :returns: Volumeplot graphic
        """
        return super(EarthAxes, self).volumeplot(*args, **kwargs)


    def lonlat(self, lon_delta=30, lat_delta=30, npoints=50, offset=10, **kwargs):
        """
        Draw longitude and latitude lines.

        :param lon_delta: (*float*) Longitude delta. Default is 30.
        :param lat_delta: (*float*) Latitude delta. Default is 30.
        :param npoints: (*int*) Number of points in one longitude/latitude line. Default is 50.
        :param offset: (*float*) Altitude offset from earth surface. Default is 10.

        :return: Longitude and latitude lines.
        """
        nlon = int(360. / lon_delta)
        lons = np.zeros([npoints, nlon])
        lats = np.zeros([npoints, nlon])
        alts = np.zeros([npoints, nlon]) + offset
        lat = np.linspace(-90., 90., npoints)
        idx = 0
        for i in np.arange(0., 360., lon_delta):
            lons[:,idx] = np.full(npoints, i)
            lats[:,idx] = lat
            idx += 1
        self.plot3(lons, lats, alts, **kwargs)

        nlat = int(180. / lat_delta)
        lons = np.zeros([npoints, nlat])
        lats = np.zeros([npoints, nlat])
        alts = np.zeros([npoints, nlat]) + offset
        lon = np.linspace(-180., 180., npoints)
        idx = 0
        for i in np.arange(-90., 90., lat_delta):
            lats[:,idx] = np.full(npoints, i)
            lons[:,idx] = lon
            idx += 1
        self.plot3(lons, lats, alts, **kwargs)

    def axis(self, limits):
        """
        Sets the min and max of the x,y, axes, with ``[xmin, xmax, ymin, ymax, zmin, zmax]`` .

        :param limits: (*list*) Min and max of the x,y,z axes.
        """
        if len(limits) == 6:
            xmin = limits[0]
            xmax = limits[1]
            ymin = limits[2]
            ymax = limits[3]
            zmin = limits[4]
            zmax = limits[5]
            extent = Extent(xmin, xmax, ymin, ymax, zmin, zmax)
            self._axes.setDrawExtent(extent)
            return True
        else:
            print('The limits parameter must be a list with 6 elements: xmin, xmax, ymin, ymax, zmin, zmax!')
            return None

####################################################
