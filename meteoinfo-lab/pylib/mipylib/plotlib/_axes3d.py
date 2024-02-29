# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2018-4-10
# Purpose: MeteoInfoLab axes3d module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.chart.plot import Plot3D
from org.meteoinfo.chart.graphic import GraphicFactory
from org.meteoinfo.chart import ChartText3D
from org.meteoinfo.chart.axis import Axis, LonLatAxis, TimeAxis, LogAxis
#from org.meteoinfo.geo.legend import LegendManage
from org.meteoinfo.geo.io import GraphicUtil
from org.meteoinfo.geometry.legend import BreakTypes, PolylineBreak, LegendManage
from org.meteoinfo.geometry.shape import ShapeTypes
from org.meteoinfo.geometry.graphic import Graphic
from org.meteoinfo.geo.layer import LayerTypes
from org.meteoinfo.common import Extent3D

from ._axes import Axes
from mipylib.numeric.core import NDArray, DimArray
import mipylib.numeric as np
import plotutil
import mipylib.miutil as miutil
from mipylib import migl
from mipylib.geolib import migeo

import os
import datetime
import numbers
import warnings

from java.awt import Font, Color, BasicStroke

__all__ = ['Axes3D']

#########################################################
class Axes3D(Axes):
    """
    Axes with 3 dimensional.
    """
    
    def __init__(self, *args, **kwargs):
        axes = kwargs.pop('axes', None)
        self._set_plot(axes)
        
        figure = kwargs.pop('figure', None)
        self.figure = figure
        
        if len(args) > 0:
            position = args[0]
        else:
            position = kwargs.pop('position', None)    
        outerposition = kwargs.pop('outerposition', None)
        if position is None:
            position = [0.13, 0.11, 0.71, 0.815]
            self.active_outerposition(True)
        else:        
            self.active_outerposition(False)        
        self.set_position(position)   
        if not outerposition is None:
            self.set_outerposition(outerposition)
            self.active_outerposition(True)
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
        
        self.projector = self._axes.getProjector()
        #distance = kwargs.pop('distance', 10000)
        #self.projector.setDistance(distance)
        rotation_angle = kwargs.pop('rotation', 225)
        self.projector.setRotationAngle(rotation_angle)
        elevation_angle = kwargs.pop('elevation', 30)
        self.projector.setElevationAngle(elevation_angle)
        xyaxis = kwargs.pop('xyaxis', True)
        self._axes.setDisplayXY(xyaxis)
        zaxis = kwargs.pop('zaxis', True)
        self._axes.setDisplayZ(zaxis)
        grid = kwargs.pop('grid', True)
        grid_line = self._axes.getGridLine()
        grid_line.setDrawXLine(grid)
        grid_line.setDrawYLine(grid)
        grid_line.setDrawZLine(grid)
        boxed = kwargs.pop('boxed', True)
        self._axes.setBoxed(boxed)
        bbox = kwargs.pop('bbox', False)
        self._axes.setDrawBoundingBox(bbox)
        
    def _set_plot(self, plot):
        """
        Set plot.
        
        :param plot: (*Plot3D*) Plot.
        """
        if plot is None:
            self._axes = Plot3D()
        else:
            self._axes = plot
    
    @property
    def axestype(self):
        return '3d'

    @property
    def ndim(self):
        """
        Dimension number property
        """
        return 3
    
    def get_distance(self):
        """
        Get distance to object.
        
        :returns: Distance to object.
        """
        return self.projector.getDistance()
        
    def set_distance(self, dis):
        """
        Set distance to object.
        
        :param dis: (*float*) Distance to object.
        """
        self.projector.setDistance(dis)
        
    def get_rotation(self):
        """
        Get rotation angle.
        
        :returns: Rotation angle.
        """
        return self.projector.getRotationAngle()
        
    def set_rotation(self, rotation):
        """
        Set rotation angle.
        
        :param rotation: (*float*) Rotation angle.
        """
        self.projector.setRotationAngle(rotation)
        
    def get_elevation(self):
        """
        Get elevation angle.
        
        :returns: Elevation angle.
        """
        return self.projector.getElevationAngle()
        
    def set_elevation(self, elevation):
        """
        Set elevation angle.
        
        :param elevation: (*float*) Elevation angle.
        """
        self.projector.setElevationAngle(elevation)
        
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
                try:
                    xmin, xmax, ymin, ymax, zmin, zmax = arg
                except (TypeError, ValueError) as err:
                    raise TypeError('the first argument to axis() must be an '
                                    'iterable of the form '
                                    '[xmin, xmax, ymin, ymax, zmin, zmax]')
                self.set_xlim(xmin, xmax)
                self.set_ylim(ymin, ymax)
                self.set_zlim(zmin, zmax)

        return self.get_xlim() + self.get_ylim() + self.get_zlim()

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

    def plot(self, x, y, z, *args, **kwargs):
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
        xdata = plotutil.getplotdata(x)
        ydata = plotutil.getplotdata(y)
        zdata = plotutil.getplotdata(z)  
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
        if cdata is None:
            if colors is None:
                graphics = GraphicFactory.createLineString3D(xdata, ydata, zdata, line)
            else:
                graphics = GraphicFactory.createLineString3D(xdata, ydata, zdata, cbs)
        else:
            cdata = plotutil.getplotdata(cdata)
            graphics = GraphicFactory.createLineString3D(xdata, ydata, zdata, cdata, ls)

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
        
    def scatter(self, x, y, z, s=8, c='b', marker='o', **kwargs):
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
        xdata = plotutil.getplotdata(x)
        ydata = plotutil.getplotdata(y)
        zdata = plotutil.getplotdata(z)
        
        #Set plot data styles
        pb, isunique = plotutil.getlegendbreak('point', **kwargs)
        pb.setCaption(label)
        pstyle = plotutil.getpointstyle(marker)    
        pb.setStyle(pstyle)
        isvalue = False
        if isinstance(c, (NDArray, DimArray)):
            isvalue = True
        elif isinstance(c, (list, tuple)):
            if isinstance(c[0], numbers.Number):
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
            #Create graphics
            graphics = GraphicFactory.createPoints3D(xdata, ydata, zdata, c.asarray(), ls)
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
            graphics = GraphicFactory.createPoints3D(xdata, ydata, zdata, pbs)

        sphere = kwargs.pop('sphere', None)
        if not sphere is None:
            graphics.setSphere(sphere)

        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics
        
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

    def mesh(self, *args, **kwargs):
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

        :returns: Mesh graphics
        """
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
            ls = ls.convertTo(ShapeTypes.POLYLINE)
            plotutil.setlegendscheme(ls, **kwargs)
            graphics = GraphicFactory.createWireframe(x.asarray(), y.asarray(), z.asarray(), ls)

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
            ls = ls.convertTo(ShapeTypes.POLYLINE)
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
        :param xyaxis: (*boolean*) Draw x and y axis or not.
        :param zaxis: (*boolean*) Draw z axis or not.
        :param grid: (*boolean*) Draw grid or not.
        :param boxed: (*boolean*) Draw boxed or not.
        :param mesh: (*boolean*) Draw mesh line or not.

        :returns: Legend
        """
        if len(args) <= 2:
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
        ls = ls.convertTo(ShapeTypes.POLYGON)
        edge = kwargs.pop('edge', True)
        kwargs['edge'] = edge
        plotutil.setlegendscheme(ls, **kwargs)
        graphics = GraphicFactory.createMeshPolygons(x.asarray(), y.asarray(), z.asarray(), ls)
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
        :param xyaxis: (*boolean*) Draw x and y axis or not.
        :param zaxis: (*boolean*) Draw z axis or not.
        :param grid: (*boolean*) Draw grid or not.
        :param boxed: (*boolean*) Draw boxed or not.
        :param mesh: (*boolean*) Draw mesh line or not.
        
        :returns: Legend
        """
        warnings.warn("plot_surface is deprecated", DeprecationWarning)
        if len(args) <= 2:
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
        ls = ls.convertTo(ShapeTypes.POLYGON)
        edge = kwargs.pop('edge', True)
        kwargs['edge'] = edge
        plotutil.setlegendscheme(ls, **kwargs)
        graphics = GraphicFactory.createMeshPolygons(x.asarray(), y.asarray(), z.asarray(), ls)
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics
        
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
        ls = ls.convertTo(ShapeTypes.POLYLINE)
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
        ls = ls.convertTo(ShapeTypes.POLYLINE)
        plotutil.setlegendscheme(ls, **kwargs)

        smooth = kwargs.pop('smooth', True)
        igraphic = GraphicFactory.createContourLines3D(x.asarray(), y.asarray(), z.asarray(), ls, smooth)

        lighting = kwargs.pop('lighting', None)
        if not lighting is None:
            igraphic.setUsingLight(lighting)

        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(igraphic)
        return igraphic
        
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
        ls = ls.convertTo(ShapeTypes.POLYGON)
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
            ls = ls.convertTo(ShapeTypes.IMAGE)
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
        
    def quiver(self, *args, **kwargs):
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
                ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POINT, c, 10)
            ls = plotutil.setlegendscheme_point(ls, **kwargs)
        
        if not cdata is None:
            cdata = plotutil.getplotdata(cdata)
        scale = kwargs.pop('scale', 1)
        headwidth = kwargs.pop('headwidth', 1)
        headlength = kwargs.pop('headlength', 2.5)
        igraphic = GraphicFactory.createArrows3D(x, y, z, u, v, w, scale, headwidth,
                                                 headlength, cdata, ls)

        lighting = kwargs.pop('lighting', None)
        if not lighting is None:
            igraphic.setUsingLight(lighting)

        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(igraphic)
        return igraphic

    def geoshow(self, layer, **kwargs):
        """
        Plot a layer map in 3D axes.

        :param layer: (*str or MILayer*) The layer to be plotted.
        :param offset: (*float*) Location on z axis.
        :param xshift: (*float*) X coordinate shift.
        :param facecolor: (*color*) Face color.
        :param edgecolor: (*color*) Edge color.
        :param linewidth: (*float*) Line width.

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
                if len(kwargs) > 0 and layer.getLegendScheme().getBreakNum() == 1:
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

            plotutil.setlegendscheme(ls, **kwargs)
            layer.setLegendScheme(ls)
            graphics = GraphicUtil.layerToGraphics(layer, offset, xshift)
        else:
            interpolation = kwargs.pop('interpolation', None)
            graphics = GraphicUtil.layerToGraphics(layer, offset, xshift, interpolation)

        lighting = kwargs.pop('lighting', None)
        if not lighting is None:
            graphics.setUsingLight(lighting)

        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics
        
    def plot_layer(self, layer, **kwargs):
        """
        Plot a layer in 3D axes.
        
        :param layer: (*MILayer*) The layer to be plotted.
        
        :returns: Graphics.
        """
        warnings.warn("plot_layer is deprecated", DeprecationWarning)
        return self.geoshow(layer, **kwargs)
        
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
        sy = self.figure.get_size()[1] - sy
        return sx, sy
