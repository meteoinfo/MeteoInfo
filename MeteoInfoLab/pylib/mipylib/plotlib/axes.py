# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2017-3-25
# Purpose: MeteoInfoLab axes module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.chart import Location, ChartWindArrow, ChartText, LegendPosition, \
    ChartLegend, ChartColorBar
from org.meteoinfo.chart.plot import Plot2D, PolarPlot, GraphicFactory, \
    PlotOrientation, XAlign, YAlign
from org.meteoinfo.chart.axis import Axis, LonLatAxis, TimeAxis, LogAxis
from org.meteoinfo.legend import LegendManage, BarBreak, PolygonBreak, PolylineBreak, \
    PointBreak, LineStyles, PointStyle, LegendScheme, LegendType
from org.meteoinfo.shape import ShapeTypes, Graphic, GraphicCollection
from org.meteoinfo.global import MIMath, Extent
from org.meteoinfo.layer import MapLayer

from java.awt import Font, Color
from java.awt.image import BufferedImage

import numbers
import datetime

from mipylib.numeric.dimarray import DimArray
from mipylib.numeric.miarray import MIArray
from mipylib.geolib.milayer import MILayer, MIXYListData
import plotutil
import mipylib.numeric.minum as minum
import mipylib.miutil as miutil

class Axes(object):
    '''
    Axes with Cartesian coordinate.
    '''

    def __init__(self, axes=None, figure=None):
        if axes is None:
            self.axes = Plot2D()
        else:
            self.axes = axes
        self.axestype = 'cartesian'
        self.figure = figure
            
    def get_type(self):
        '''
        Get axes type
        
        :returns: Axes type
        '''
        return self.axes.getPlotType()
            
    def get_position(self):
        '''
        Get axes position             

        :returns: Axes position [left, bottom, width, height] in normalized (0, 1) units
        '''
        pos = self.axes.getPosition()
        return [pos.x, pos.y, pos.width, pos.height]
        
    def set_position(self, pos):
        '''
        Set axes position
        
        :param pos: (*list*) Axes position specified by *position=* [left, bottom, width,
            height] in normalized (0, 1) units
        '''
        self.axes.setPosition(pos)
        
    def get_outerposition(self):
        '''
        Get axes outer position
        
        :returns: Axes outer position [left, bottom, width, height] in normalized (0, 1) units
        '''
        pos = self.axes.getPosition()
        return [pos.x, pos.y, pos.width, pos.height]
        
    def set_outerposition(self, pos):
        '''
        Set axes outer position
        
        :param pos: (*list*) Axes outer position specified by *position=* [left, bottom, width,
            height] in normalized (0, 1) units
        '''
        self.axes.setPosition(pos)
        
    def active_outerposition(self, active):
        '''
        Set axes outer position active or not.
        
        :param active: (*boolean*) Active or not
        '''
        self.axes.setOuterPosActive(active)     
    
    def get_axis(self, loc):
        '''
        Get axis by location.
        
        :param loc: (*Location*) Location enum.
        
        :returns: Axis
        '''
        return self.axes.getAxis(loc)
        
    def get_title(self, loc='center'):
        '''
        Get title            

        :param loc: (*string*) Which title to get ['center' | 'left' | 'right'],
            default to 'center'.
        
        :returns: The title.
        '''
        if loc == 'left':
            return self.aexs.getLeftTitle()
        elif loc == 'right':
            return self.axes.getRightTitle()
        else:
            return self.axes.getTitle()        
        
    def set_title(self, label, loc='center', fontname=None, fontsize=14, bold=True, color='black', **kwargs):
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
        exfont = False
        if fontname is None:
            fontname = 'Arial'
        else:
            exfont = True
            
        if bold:
            font = Font(fontname, Font.BOLD, fontsize)
        else:
            font = Font(fontname, Font.PLAIN, fontsize)
        c = plotutil.getcolor(color)
        title = ChartText(label, font)
        title.setXAlign(loc)
        title.setUseExternalFont(exfont)
        title.setColor(c)
        linespace = kwargs.pop('linespace', None)
        if not linespace is None:
            title.setLineSpace(linespace)
        
        if loc == 'left':
            self.axes.setLeftTitle(title)
        elif loc == 'right':
            self.axes.setRightTitle(title)
        else:
            self.axes.setTitle(title)
            
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
        axis = self.axes.getXAxis()
        axis.setLabel(ctext)
        axis.setDrawLabel(True)
        if self.axestype != '3d':
            axis_t = self.axes.getAxis(Location.TOP)
            text = ctext.clone()
            text.setXAlign('center')
            text.setYAlign('bottom')
            axis_t.setLabel(text)
    
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
        axis = self.axes.getYAxis()
        axis.setLabel(ctext)
        axis.setDrawLabel(True)
        if self.axestype != '3d':
            axis_r = self.axes.getAxis(Location.RIGHT)
            text = ctext.clone()            
            text.setXAlign('left')
            text.setYAlign('center')
            axis_r.setLabel(text)
    
    def get_xticks(self):
        '''
        Get x axis tick locations.
        '''
        axis = self.axes.getXAxis()
        axis.updateTickLabels()
        return axis.getTickLocations()
        
    def set_xticks(self, locs):
        '''
        Set x axis tick locations.
        '''
        axis = self.axes.getXAxis()
        if isinstance(locs, (MIArray, DimArray)):
            locs = labels.aslist()
        axis.setTickLocations(locs)
        
        if self.axestype == '3d':
            axis_t = None
        else:
            axis_t = self.axes.getAxis(Location.TOP)
        if not axis_t is None:
            axis_t.setTickLocations(locs)
        
    def get_yticks(self):
        '''
        Get y axis tick locations.
        '''
        axis = self.axes.getYAxis()
        axis.updateTickLabels()
        return axis.getTickLocations()
        
    def set_yticks(self, locs):
        '''
        Set y axis tick locations.
        '''
        axis = self.axes.getYAxis()
        if isinstance(locs, MIArray):
            locs = locs.aslist()
        axis.setTickLocations(locs)
        
        if self.axestype == '3d':
            axis_r = None
        else:
            axis_r = self.axes.getAxis(Location.RIGHT)
        if not axis_r is None:
            axis_r.setTickLocations(locs)
        
    def get_xticklabels(self):
        '''
        Get x axis tick labels.
        '''
        axis = self.axes.getXAxis()
        axis.updateTickLabels()
        return axis.getTickLabelText()
        
    def set_xticklabels(self, labels, **kwargs):
        '''
        Set x axis tick labels.
        '''
        axis = self.axes.getXAxis()
        if self.axestype == '3d':
            axis_t = None
        else:
            axis_t = self.axes.getAxis(Location.TOP)
            
        if not labels is None:
            if isinstance(labels, (MIArray, DimArray)):
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
        if not axis_t is None:
            axis_t.setTickLabelFont(font)
            axis_t.setTickLabelColor(c)
            axis_t.setTickLabelAngle(angle)
            
    def get_yticklabels(self):
        '''
        Get y axis tick labels.
        '''
        axis = self.axes.getYAxis()
        axis.updateTickLabels()
        return axis.getTickLabelText()
        
    def set_yticklabels(self, labels, **kwargs):
        '''
        Set y axis tick labels.
        '''
        axis = self.axes.getYAxis()
        if self.axestype == '3d':
            axis_r = None
        else:
            axis_r = self.axes.getAxis(Location.RIGHT)
            
        if not labels is None:
            if isinstance(labels, (MIArray, DimArray)):
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
        if not axis_r is None:
            axis_r.setTickLabelFont(font)
            axis_r.setTickLabelColor(c)
            axis_r.setTickLabelAngle(angle)
            
    def set_xaxis_type(self, axistype, timetickformat=None):
        '''
        Set x axis type.
        
        :param axistype: (*string*) Axis type ['lon' | 'lat' | 'time' | 'log'].
        :param timetickformat: (*string*) Time tick label format.
        '''
        ax = self.axes
        if axistype == 'lon':
            b_axis = LonLatAxis(ax.getAxis(Location.BOTTOM))
            #b_axis.setLabel('Longitude')
            b_axis.setLongitude(True)
            ax.setAxis(b_axis, Location.BOTTOM)
            t_axis = LonLatAxis(ax.getAxis(Location.TOP))
            #t_axis.setLabel('Longitude')
            t_axis.setLongitude(True)
            ax.setAxis(t_axis, Location.TOP)
        elif axistype == 'lat':
            b_axis = LonLatAxis(ax.getAxis(Location.BOTTOM))
            #b_axis.setLabel('Latitude')
            b_axis.setLongitude(False)
            ax.setAxis(b_axis, Location.BOTTOM)
            t_axis = LonLatAxis(ax.getAxis(Location.TOP))
            #t_axis.setLabel('Latitude')
            t_axis.setLongitude(False)
            ax.setAxis(t_axis, Location.TOP)
        elif axistype == 'time':
            b_axis = TimeAxis(ax.getAxis(Location.BOTTOM))
            ax.setAxis(b_axis, Location.BOTTOM)
            t_axis = TimeAxis(ax.getAxis(Location.TOP))
            ax.setAxis(t_axis, Location.TOP)
            if not timetickformat is None:
                ax.getAxis(Location.BOTTOM).setTimeFormat(timetickformat)
                ax.getAxis(Location.TOP).setTimeFormat(timetickformat)
        elif axistype == 'log':
            b_axis = LogAxis(ax.getAxis(Location.BOTTOM))
            #b_axis.setLabel('Log')
            b_axis.setMinorTickNum(10)
            ax.setAxis(b_axis, Location.BOTTOM)
            t_axis = LogAxis(ax.getAxis(Location.TOP))
            #t_axis.setLabel('Log')
            t_axis.setMinorTickNum(10)
            ax.setAxis(t_axis, Location.TOP)   
        else:
            b_axis = Axis(ax.getAxis(Location.BOTTOM))
            ax.setAxis(b_axis, Location.BOTTOM)
            t_axis = Axis(ax.getAxis(Location.TOP))
            ax.setAxis(t_axis, Location.TOP)
                    
    def set_yaxis_type(self, axistype, timetickformat=None):
        '''
        Set y axis type.
        
        :param axistype: (*string*) Axis type ['lon' | 'lat' | 'time' | 'log'].
        :param timetickformat: (*string*) Time tick label format.
        '''
        ax = self.axes
        if axistype == 'lon':
            b_axis = LonLatAxis(ax.getAxis(Location.LEFT))
            #b_axis.setLabel('Longitude')
            b_axis.setLongitude(True)
            ax.setAxis(b_axis, Location.LEFT)
            t_axis = LonLatAxis(ax.getAxis(Location.RIGHT))
            #t_axis.setLabel('Longitude')
            t_axis.setLongitude(True)
            ax.setAxis(t_axis, Location.RIGHT)
        elif axistype == 'lat':
            b_axis = LonLatAxis(ax.getAxis(Location.LEFT))
            #b_axis.setLabel('Latitude')
            b_axis.setLongitude(False)
            ax.setAxis(b_axis, Location.LEFT)
            t_axis = LonLatAxis(ax.getAxis(Location.RIGHT))
            #t_axis.setLabel('Latitude')
            t_axis.setLongitude(False)
            ax.setAxis(t_axis, Location.RIGHT)
        elif axistype == 'time':
            b_axis = TimeAxis(ax.getAxis(Location.LEFT))
            ax.setAxis(b_axis, Location.LEFT)
            t_axis = TimeAxis(ax.getAxis(Location.RIGHT))
            ax.setAxis(t_axis, Location.RIGHT)
            if not timetickformat is None:
                ax.getAxis(Location.LEFT).setTimeFormat(timetickformat)
                ax.getAxis(Location.RIGHT).setTimeFormat(timetickformat)
        elif axistype == 'log':
            l_axis = LogAxis(ax.getAxis(Location.LEFT))
            #l_axis.setLabel('Log')
            l_axis.setMinorTickNum(10)
            ax.setAxis(l_axis, Location.LEFT)
            r_axis = LogAxis(ax.getAxis(Location.RIGHT))
            #r_axis.setLabel('Log')
            r_axis.setMinorTickNum(10)
            ax.setAxis(r_axis, Location.RIGHT)
        else:
            l_axis = Axis(ax.getAxis(Location.LEFT))
            ax.setAxis(l_axis, Location.LEFT)
            r_axis = Axis(ax.getAxis(Location.RIGHT))
            ax.setAxis(r_axis, Location.RIGHT)
        
    def axis(self, limits):
        """
        Sets the min and max of the x and y axes, with ``[xmin, xmax, ymin, ymax]`` .
        
        :param limits: (*list*) Min and max of the x and y axes.
        """
        if len(limits) == 4:
            xmin = limits[0]
            xmax = limits[1]
            ymin = limits[2]
            ymax = limits[3]
            extent = Extent(xmin, xmax, ymin, ymax)
            self.axes.setDrawExtent(extent)
            self.axes.setExtent(extent.clone())
            return True
        else:
            print 'The limits parameter must be a list with 4 elements: xmin, xmax, ymin, ymax!'    
            return None
            
    def get_xlim(self):
        """
        Get the *x* limits of the current axes.
        
        :returns: (*tuple*) x limits.
        """
        extent = self.axes.getDrawExtent()
        return extent.minX, extent.maxX
            
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
            
        extent = self.axes.getDrawExtent()
        extent.minX = xmin
        extent.maxX = xmax
        self.axes.setDrawExtent(extent)
        self.axes.setExtent(extent.clone())    

    def get_ylim(self):
        """
        Get the *y* limits of the current axes.
        
        :returns: (*tuple*) y limits.
        """
        extent = self.axes.getDrawExtent()
        return extent.minY, extent.maxY
            
    def set_ylim(self, ymin, ymax):
        """
        Set the *yt* limits of the current axes.
        
        :param ymin: (*float*) Minimum limit of the y axis.
        :param ymax: (*float*) Maximum limit of the y axis.
        """
        if isinstance(ymin, datetime.datetime):
            ymin = miutil.date2num(ymin)
        if isinstance(ymax, datetime.datetime):
            ymax = miutil.date2num(ymax)    
            
        extent = self.axes.getDrawExtent()
        extent.minY = ymin
        extent.maxY = ymax
        self.axes.setDrawExtent(extent)
        self.axes.setExtent(extent.clone())  
        
    def twinx(self):
        """
        Make a second axes that shares the x-axis. The new axes will overlay *ax*. The ticks 
        for *ax2* will be placed on the right, and the *ax2* instance is returned.
        
        :returns: The second axes
        """
        self.axes.getAxis(Location.RIGHT).setVisible(False)
        self.axes.setSameShrink(True) 
        ax2 = Axes()
        ax2.axes.setSameShrink(True)
        ax2.axes.setPosition(self.get_position())
        ax2.axes.setOuterPosActive(self.axes.isOuterPosActive())
        ax2.axes.getAxis(Location.BOTTOM).setVisible(False)
        ax2.axes.getAxis(Location.LEFT).setVisible(False)
        ax2.axes.getAxis(Location.TOP).setVisible(False)
        axis = ax2.axes.getAxis(Location.RIGHT)
        axis.setDrawTickLabel(True)
        axis.setDrawLabel(True)
        return ax2
        
    def twiny(self):
        """
        Make a second axes that shares the y-axis. The new axes will overlay *ax*. The ticks 
        for *ax2* will be placed on the top, and the *ax2* instance is returned.
        
        :returns: The second axes
        """
        self.axes.getAxis(Location.TOP).setVisible(False)
        self.axes.setSameShrink(True) 
        ax2 = Axes()
        ax2.axes.setSameShrink(True)
        ax2.axes.setPosition(self.get_position())
        ax2.axes.setOuterPosActive(self.axes.isOuterPosActive())
        ax2.axes.getAxis(Location.BOTTOM).setVisible(False)
        ax2.axes.getAxis(Location.LEFT).setVisible(False)
        ax2.axes.getAxis(Location.RIGHT).setVisible(False)
        axis = ax2.axes.getAxis(Location.TOP)
        axis.setDrawTickLabel(True)
        axis.setDrawLabel(True)
        return ax2

    def xaxis(self, **kwargs):
        """
        Set x axis of the axes.
        
        :param color: (*Color*) Color of the x axis. Default is 'black'.
        :param shift: (*int) X axis shif along x direction. Units is pixel. Default is 0.
        """
        visible = kwargs.pop('visible', None)
        shift = kwargs.pop('shift', None)
        color = kwargs.pop('color', None)
        if not color is None:
            color = plotutil.getcolor(color)
        linewidth = kwargs.pop('linewidth', None)
        linestyle = kwargs.pop('linestyle', None)
        tickline = kwargs.pop('tickline', None)
        tickline = kwargs.pop('tickvisible', tickline)
        ticklength = kwargs.pop('ticklength', None)
        ticklabel = kwargs.pop('ticklabel', None)
        minortick = kwargs.pop('minortick', False)
        minorticknum = kwargs.pop('minorticknum', 5)
        tickin = kwargs.pop('tickin', True)
        axistype = kwargs.pop('axistype', None)
        timetickformat = kwargs.pop('timetickformat', None)
        if not axistype is None:
            self.set_xaxis_type(axistype, timetickformat)
            self.axes.setAutoExtent()
        tickfontname = kwargs.pop('tickfontname', 'Arial')
        tickfontsize = kwargs.pop('tickfontsize', 14)
        tickbold = kwargs.pop('tickbold', False)
        if tickbold:
            font = Font(tickfontname, Font.BOLD, tickfontsize)
        else:
            font = Font(tickfontname, Font.PLAIN, tickfontsize)
        location = kwargs.pop('location', 'both')
        if location == 'top':
            locs = [Location.TOP]
        elif location == 'bottom':
            locs = [Location.BOTTOM]
        else:
            locs = [Location.BOTTOM, Location.TOP]
        axislist = []
        if self.axestype == '3d':
            axislist.append(self.axes.getXAxis())
        else:
            for loc in locs:    
                axislist.append(self.axes.getAxis(loc))
        for axis in axislist:
            if not visible is None:
                axis.setVisible(visible)
            if not shift is None:
                axis.setShift(shift)
            if not color is None:
                axis.setColor_All(color)
            if not linewidth is None:
                axis.setLineWidth(linewidth)
            if not linestyle is None:
                axis.setLineStyle(linestyle)
            if not tickline is None:
                axis.setDrawTickLine(tickline)
            if not ticklength is None:
                axis.setTickLength(ticklength)
            if not ticklabel is None:
                axis.setDrawTickLabel(ticklabel)
            axis.setMinorTickVisible(minortick)
            axis.setMinorTickNum(minorticknum)
            axis.setInsideTick(tickin)
            axis.setTickLabelFont(font)
        
    def yaxis(self, **kwargs):
        """
        Set y axis of the axes.

        :param color: (*Color*) Color of the y axis. Default is 'black'.
        :param shift: (*int) Y axis shif along x direction. Units is pixel. Default is 0.
        """
        visible = kwargs.pop('visible', None)
        shift = kwargs.pop('shift', None)
        color = kwargs.pop('color', None)
        if not color is None:
            color = plotutil.getcolor(color)
        linewidth = kwargs.pop('linewidth', None)
        linestyle = kwargs.pop('linestyle', None)
        tickline = kwargs.pop('tickline', None)
        tickline = kwargs.pop('tickvisible', tickline)
        ticklength = kwargs.pop('ticklength', None)
        ticklabel = kwargs.pop('ticklabel', None)
        minortick = kwargs.pop('minortick', False)
        minorticknum = kwargs.pop('minorticknum', 5)
        tickin = kwargs.pop('tickin', True)
        axistype = kwargs.pop('axistype', None)
        timetickformat = kwargs.pop('timetickformat', None)
        if not axistype is None:
            self.set_yaxis_type(axistype, timetickformat)
            self.axes.updateDrawExtent()
        tickfontname = kwargs.pop('tickfontname', 'Arial')
        tickfontsize = kwargs.pop('tickfontsize', 14)
        tickbold = kwargs.pop('tickbold', False)
        if tickbold:
            font = Font(tickfontname, Font.BOLD, tickfontsize)
        else:
            font = Font(tickfontname, Font.PLAIN, tickfontsize)
        location = kwargs.pop('location', 'both')
        if location == 'left':
            locs = [Location.LEFT]
        elif location == 'right':
            locs = [Location.RIGHT]
        else:
            locs = [Location.LEFT, Location.RIGHT]
        axislist = []
        if self.axestype == '3d':
            axislist.append(self.axes.getYAxis())
        else:
            for loc in locs:    
                axislist.append(self.axes.getAxis(loc))
        for axis in axislist:
            if not visible is None:
                axis.setVisible(visible)
            if not shift is None:
                axis.setShift(shift)
            if not color is None:
                axis.setColor_All(color)
            if not linewidth is None:
                axis.setLineWidth(linewidth)
            if not linestyle is None:
                axis.setLineStyle(linestyle)
            if not tickline is None:
                axis.setDrawTickLine(tickline)
            if not ticklength is None:
                axis.setTickLength(ticklength)
            if not ticklabel is None:
                axis.setDrawTickLabel(ticklabel)
            axis.setMinorTickVisible(minortick)
            axis.setMinorTickNum(minorticknum)
            axis.setInsideTick(tickin)
            axis.setTickLabelFont(font)
    
    def xreverse(self):
        '''
        Reverse x axis.
        '''
        self.axes.getXAxis().setInverse(True)
        
    def yreverse(self):
        '''
        Reverse y axis.
        '''
        self.axes.getYAxis().setInverse(True)
    
    def add_graphic(self, graphic):
        '''
        Add a graphic
        
        :param graphic: (*Graphic*) The graphic to be added.
        '''
        self.axes.addGraphic(graphic)
        
    def remove(self):
        '''
        Remove all graphics.
        '''
        self.axes.getGraphics().clear()
        
    def data2pixel(self, x, y, z=None):
        '''
        Transform data coordinate to screen coordinate
        
        :param x: (*float*) X coordinate.
        :param y: (*float*) Y coordinate.
        :param z: (*float*) Z coordinate - only used for 3-D axes.
        '''
        rect = self.axes.getPositionArea()
        r = self.axes.projToScreen(x, y, rect)
        sx = r[0] + rect.getX()
        sy = r[1] + rect.getY()
        sy = self.figure.get_size()[1] - sy
        return sx, sy
        
    def grid(self, b=None, which='major', axis='both', **kwargs):
        """
        Turn the aexs grids on or off.
        
        :param b: If b is *None* and *len(kwargs)==0* , toggle the grid state. If *kwargs*
            are supplied, it is assumed that you want a grid and *b* is thus set to *True* .
        :param which: *which* can be 'major' (default), 'minor', or 'both' to control
            whether major tick grids, minor tick grids, or both are affected.
        :param axis: *axis* can be 'both' (default), 'x', or 'y' to control which set of
            gridlines are drawn.
        :param kwargs: *kwargs* are used to set the grid line properties.
        """
        gridline = self.axes.getGridLine()
        isDraw = gridline.isDrawXLine()
        if b is None:
            isDraw = not gridline.isDrawXLine()
        elif b == True or b == 'on':
            isDraw = True
        elif b == False or b == 'on':
            isDraw = False
        if axis == 'both':
            gridline.setDrawXLine(isDraw)
            gridline.setDrawYLine(isDraw)
        elif axis == 'x':
            gridline.setDrawXLine(isDraw)
        elif axis == 'y':
            gridline.setDrawYLine(isDraw)
        color = kwargs.pop('color', None)
        if not color is None:
            c = plotutil.getcolor(color)
            gridline.setColor(c)
        linewidth = kwargs.pop('linewidth', None)
        if not linewidth is None:
            gridline.setSize(linewidth)
        linestyle = kwargs.pop('linestyle', None)
        if not linestyle is None:
            linestyle = plotutil.getlinestyle(linestyle)
            gridline.setStyle(linestyle)
        top = kwargs.pop('top', None)
        if not top is None:
            gridline.setTop(top)
            
    def plot(self, *args, **kwargs):
        """
        Plot lines and/or markers to the axes. *args* is a variable length argument, allowing
        for multiple *x, y* pairs with an optional format string.
        
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
        xdatalist = []
        ydatalist = []    
        styles = []
        xaxistype = None
        isxylistdata = False
        if len(args) == 1:
            if isinstance(args[0], MIXYListData):
                dataset = args[0].data
                snum = args[0].size()
                isxylistdata = True
            else:
                ydata = minum.array(args[0])
                if isinstance(ydata, DimArray):
                    xdata = ydata.dimvalue(0)
                    if ydata.ndim == 2:
                        xdata = ydata.dimvalue(1)
                        xx = minum.zeros(ydata.shape)
                        xx[:,:] = xdata
                        xdata = xx
                    if ydata.islondim(0):
                        xaxistype = 'lon'
                    elif ydata.islatdim(0):
                        xaxistype = 'lat'
                    elif ydata.istimedim(0):
                        xaxistype = 'time'
                else:
                    xdata = minum.arange(ydata.shape[-1])
                    if ydata.ndim == 2:
                        xx = minum.zeros(ydata.shape)
                        xx[:,:] = xdata
                        xdata = xx
                xdatalist.append(xdata)
                ydatalist.append(ydata)
        elif len(args) == 2:
            if isinstance(args[1], basestring):
                ydata = minum.array(args[0])
                if isinstance(ydata, DimArray):
                    xdata = ydata.dimvalue(0)
                    if ydata.ndim == 2:
                        xdata = ydata.dimvalue(1)
                        xx = minum.zeros(ydata.shape)
                        xx[:,:] = xdata
                        xdata = xx
                    if ydata.islondim(0):
                        xaxistype = 'lon'
                    elif ydata.islatdim(0):
                        xaxistype = 'lat'
                    elif ydata.istimedim(0):
                        xaxistype = 'time'
                else:
                    xdata = minum.arange(ydata.shape[-1])
                    if ydata.ndim == 2:
                        xx = minum.zeros(ydata.shape)
                        xx[:,:] = xdata
                        xdata = xx
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
                        styles.append('-')
                        xdatalist.append(arg)
                        c = 'y'
        if len(styles) == 0:
            styles = None
        else:
            while len(styles) < len(xdatalist):
                styles.append('-')
        
        #Set plot data styles
        zvalues = kwargs.pop('zvalues', None)
        if zvalues is None:
            lines = []
            legend = kwargs.pop('legend', None)
            if not legend is None:
                if isinstance(legend, list):
                    lines = legend
                else:
                    lines = legend.getLegendBreaks()
            else:
                if styles != None:
                    for i in range(0, len(styles)):
                        label = kwargs.pop('label', 'S_' + str(i + 1))
                        line = plotutil.getplotstyle(styles[i], label, **kwargs)
                        lines.append(line)
                else:
                    snum = len(xdatalist)
                    for i in range(0, snum):
                        label = kwargs.pop('label', 'S_' + str(i + 1))
                        line = plotutil.getlegendbreak('line', **kwargs)[0]
                        line.setCaption(label)
                        lines.append(line) 
        else:
            ls = kwargs.pop('symbolspec', None)
            if ls is None:        
                if isinstance(zvalues, (list, tuple)):
                    zvalues = minum.array(zvalues)
                levels = kwargs.pop('levs', None)
                if levels is None:
                    levels = kwargs.pop('levels', None)
                if levels is None:
                    cnum = kwargs.pop('cnum', None)
                    if cnum is None:
                        ls = plotutil.getlegendscheme([], zvalues.min(), zvalues.max(), **kwargs)
                    else:
                        ls = plotutil.getlegendscheme([cnum], zvalues.min(), zvalues.max(), **kwargs)
                else:
                    ls = plotutil.getlegendscheme([levels], zvalues.min(), zvalues.max(), **kwargs)
                ls = plotutil.setlegendscheme_line(ls, **kwargs)
        
        if not xaxistype is None:
            self.set_xaxis_type(xaxistype)    
        timetickformat = kwargs.pop('timetickformat', None)
        if not timetickformat is None:
            if not xaxistype == 'time':
                self.axes.setXAxis(TimeAxis('Time', True))
            self.axes.getAxis(Location.BOTTOM).setTimeFormat(timetickformat)
            self.axes.getAxis(Location.TOP).setTimeFormat(timetickformat)    

        #Add graphics
        iscurve = kwargs.pop('iscurve', False)
        graphics = []
        if isxylistdata:
            graphic = GraphicFactory.createLineString(dataset, lines)
            self.add_graphic(graphic)
            graphics.append(graphic)
        else:
            if zvalues is None:
                #Add data series
                snum = len(xdatalist)
                if snum == 1:
                    xdata = plotutil.getplotdata(xdatalist[0])
                    ydata = plotutil.getplotdata(ydatalist[0])
                    if len(lines) == 1:
                        colors = kwargs.pop('colors', None)
                        if not colors is None:
                            colors = plotutil.getcolors(colors)
                            cb = lines[0]
                            lines = []
                            for cc in colors:
                                ncb = cb.clone()
                                ncb.setColor(cc)
                                lines.append(ncb)
                            graphic = GraphicFactory.createLineString(xdata, ydata, lines, iscurve)
                        else:
                            graphic = GraphicFactory.createLineString(xdata, ydata, lines[0], iscurve)
                    else:    #>1                        
                        graphic = GraphicFactory.createLineString(xdata, ydata, lines, iscurve)
                    self.add_graphic(graphic)
                    graphics.append(graphic)
                else:
                    for i in range(0, snum):
                        label = kwargs.pop('label', 'S_' + str(i + 1))
                        xdata = plotutil.getplotdata(xdatalist[i])
                        ydata = plotutil.getplotdata(ydatalist[i])
                        graphic = GraphicFactory.createLineString(xdata, ydata, lines[i], iscurve)
                        self.add_graphic(graphic)
                        graphics.append(graphic)
            else:
                xdata = plotutil.getplotdata(xdatalist[0])
                ydata = plotutil.getplotdata(ydatalist[0])
                zdata = plotutil.getplotdata(zvalues)
                graphic = GraphicFactory.createLineString(xdata, ydata, zdata, ls, iscurve)
                self.add_graphic(graphic)
                graphics.append(graphic)
        self.axes.setAutoExtent()

        if len(graphics) > 1:
            return graphics
        else:
            return graphics[0]
            
    def step(self, x, y, *args, **kwargs):
        '''
        Make a step plot.
        
        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param style: (*string*) Line style for plot.
        :param label: (*string*) Step line label.
        :param where: (*string*) ['pre' | 'post' | 'mid']. If 'pre' (the default), the interval 
            from x[i] to x[i+1] has level y[i+1]. If 'post', that interval has level y[i].
            If ‘mid’, the jumps in y occur half-way between the x-values.
        
        :returns: Step lines
        '''    
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
        
        #Create graphics
        graphics = GraphicFactory.createStepLineString(xdata, ydata, fmt, where)
        self.add_graphic(graphics)
        self.axes.setAutoExtent()
        return graphics 
            
    def scatter(self, x, y, s=8, c='b', marker='o', norm=None, vmin=None, vmax=None,
                alpha=None, linewidth=None, verts=None, hold=None, **kwargs):
        """
        Make a scatter plot of x vs y, where x and y are sequence like objects of the same lengths.
        
        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param s: (*int*) Size of points.
        :param c: (*Color or array*) Color of the points. Or z vlaues.
        :param alpha: (*int*) The alpha blending value, between 0 (transparent) and 1 (opaque).
        :param marker: (*string*) Marker of the points.
        :param label: (*string*) Label of the points series.
        :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level 
            points to draw, in increasing order.
        
        :returns: Points legend break.
        """
        #Add data series
        label = kwargs.pop('label', 'S_0')
        xdata = plotutil.getplotdata(x)
        ydata = plotutil.getplotdata(y)
        
        #Set plot data styles
        pb, isunique = plotutil.getlegendbreak('point', **kwargs)
        pb.setCaption(label)
        pstyle = plotutil.getpointstyle(marker)    
        pb.setStyle(pstyle)
        isvalue = False
        if len(c) > 1:
            if isinstance(c, (MIArray, DimArray)):
                isvalue = True
            elif len(x) == len(c) and isinstance(c[0], (int, long, float)):
                isvalue = True            
        if isvalue:
            ls = kwargs.pop('symbolspec', None)
            if ls is None:        
                if isinstance(c, (list, tuple)):
                    c = minum.array(c)
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
            graphics = GraphicFactory.createPoints(xdata, ydata, c.asarray(), ls)
        else:
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
            graphics = GraphicFactory.createPoints(xdata, ydata, pbs)

        self.add_graphic(graphics)
        self.axes.setAutoExtent()

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
        self.axes.setAutoExtent()
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
        self.axes.setAutoExtent()
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
        self.axes.setAutoExtent()
        return lines
        
    def errorbar(self, x, y, yerr=None, xerr=None, fmt='', ecolor=None, elinewidth=None, capsize=None,
            **kwargs):
        '''
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
        '''
        #Add data series
        label = kwargs.pop('label', 'S_0')
        xdata = plotutil.getplotdata(x)
        ydata = plotutil.getplotdata(y)
        if not yerr is None:
            if isinstance(yerr, (int, float)):
                ye = []
                for i in range(xdata.getSize()):
                    ye.append(yerr)
                yerrB = minum.array(ye).array
                yerrU = yerrB
            else:
                if isinstance(yerr, (list, tuple)):
                    yerrB = plotutil.getplotdata(yerr[0])
                    yerrU = plotutil.getplotdata(yerr[1])
                elif yerr.ndim == 2:
                    yerrB = yerr[0,:].asarray()
                    yerrU = yerr[1,:].asarray()
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
                xerrL = minum.array(ye).array
                xerrR = xerrL         
            else:
                if isinstance(xerr, (list, tuple)):
                    xerrL = plotutil.getplotdata(xerr[0])
                    xerrR = plotutil.getplotdata(xerr[1])
                elif xerr.ndim == 2:
                    xerrL = xerr[0,:].asarray()
                    xerrR = xerr[1,:].asarray()
                else:
                    xerrL = plotutil.getplotdata(xerr)
                    xerrR = xerrL
        else:
            xerrL = None
            xerrR = None
        
        #Get plot data style
        if fmt == '':
            line = plotutil.getlegendbreak('line', **kwargs)[0]
            line.setCaption(label)
        else:
            line = plotutil.getplotstyle(fmt, label, **kwargs)
        eline = line.clone()
        eline.setDrawSymbol(False)
        eline.setStyle(LineStyles.SOLID)
        if not ecolor is None:
            ecolor = plotutil.getcolor(ecolor)
            eline.setColor(ecolor)
        if not elinewidth is None:
            eline.setSize(elinewidth)
        
        #Create graphics
        if capsize is None:
            capsize = 10
        graphics = GraphicFactory.createErrorLineString(xdata, ydata, xerrL, xerrR, yerrB, \
            yerrU, line, eline, capsize)
        self.add_graphic(graphics)
        self.axes.setAutoExtent()

        return graphics 
            
    def bar(self, *args, **kwargs):
        """
        Make a bar plot.
        
        Make a bar plot with rectangles bounded by:
            left, left + width, bottom, bottom + height
        
        :param left: (*array_like*) The x coordinates of the left sides of the bars.
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
            (only one argument widthout ``width`` augument). Defaul is 0.8.
        :param morepoints: (*boolean*) More points in bar rectangle. Defaul is False.
        
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
        #Add data series
        label = kwargs.pop('label', 'S_0')
        xdata = None
        autowidth = True
        width = 0.8
        if len(args) == 1:
            ydata = args[0]
        elif len(args) == 2:
            if isinstance(args[1], (int, float)):
                ydata = args[0]
                width = args[1]
                autowidth = False
            else:
                xdata = args[0]
                ydata = args[1]
        else:
            xdata = args[0]
            ydata = args[1]
            width = args[2]
            autowidth = False        
        
        if xdata is None:
            xdata = []
            for i in range(1, len(args[0]) + 1):
                xdata.append(i)
        xdata = plotutil.getplotdata(xdata)
        ydata = plotutil.getplotdata(ydata)
        width = plotutil.getplotdata(width)
        yerr = kwargs.pop('yerr', None)
        if not yerr is None:
            if not isinstance(yerr, (int, float)):
                yerr = plotutil.getplotdata(yerr)
        bottom = kwargs.pop('bottom', None)   
        if not bottom is None:
            bottom = plotutil.getplotdata(bottom)
        
        #Set plot data styles
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
        morepoints = kwargs.pop('morepoints', False)
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
            
        #Create bar graphics
        if morepoints:
            graphics = GraphicFactory.createBars1(xdata, ydata, autowidth, width, not yerr is None, yerr, \
                not bottom is None, bottom, barbreaks)
        else:
            graphics = GraphicFactory.createBars(xdata, ydata, autowidth, width, not yerr is None, yerr, \
                not bottom is None, bottom, barbreaks)        

        self.add_graphic(graphics)
        self.axes.setAutoExtent()
        if autowidth:
            barswidth = kwargs.pop('barswidth', 0.8)
            self.axes.setBarsWidth(barswidth)

        return barbreaks
 
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
            (only one argument widthout ``width`` augument). Defaul is 0.8.
        :param morepoints: (*boolean*) More points in bar rectangle. Defaul is False.
        
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
        #Add data series
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
        
        #Set plot data styles
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
            
        #Create bar graphics
        graphics = GraphicFactory.createHBars(ydata, xdata, autoheight, height, not xerr is None, xerr, \
            not left is None, left, barbreaks)  
                      
        self.add_graphic(graphics)
        self.axes.setAutoExtent()
        if autoheight:
            barsheight = kwargs.pop('barsheight', 0.8)
            self.axes.setBarsWidth(barsheight)        

        return barbreaks
        
    def hist(self, x, bins=10, range=None, normed=False, cumulative=False,
            bottom=None, histtype='bar', align='mid',
            orientation='vertical', rwidth=None, log=False, **kwargs):
        """
        Plot a histogram.
        
        :param x: (*array_like*) Input values, this takes either a single array or a sequency of arrays 
            which are not required to be of the same length.
        :param bins: (*int*) If an integer is given, bins + 1 bin edges are returned.
        """
        #Add data series
        label = kwargs.pop('label', 'S_0')
        
        #Set plot data styles
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
            
        #Create bar graphics
        x = plotutil.getplotdata(x)
        if not isinstance(bins, numbers.Number):
            bins = plotutil.getplotdata(bins)
        graphics = GraphicFactory.createHistBars(x, bins, barbreaks)        
        self.add_graphic(graphics)
        self.axes.setAutoExtent()

        return lb
        
    def stem(self, *args, **kwargs):
        """
        Make a stem plot.
        
        A stem plot plots vertical lines at each x location from the baseline to y, and 
        places a marker there.
        
        :param x: (*array_like*) The x-positions of the stems.
        :param y: (*array_like*) The y-values of the stem heads.
        :param bottom: (*array_like*) Optional, The y-position of the baseline.
        :param linefmt: (*dict*) Optional, stem line format.
        :param markerfmt: (*dict*) Optional, stem marker format.
        :param color: (*Color*) Optional, the color of the stem.
        
        :returns: Stem line legend break.                  
        """
        #Add data series
        label = kwargs.pop('label', 'S_0')
        xdata = None
        if len(args) == 1:
            ydata = args[0]
        else:
            xdata = args[0]
            ydata = args[1]       
        
        if xdata is None:
            xdata = []
            for i in range(1, len(args[0]) + 1):
                xdata.append(i)
        xdata = plotutil.getplotdata(xdata)
        ydata = plotutil.getplotdata(ydata)        
        bottom = kwargs.pop('bottom', 0)   
        
        #Set plot data styles
        color = kwargs.pop('color', None)
        if not color is None:
            color = plotutil.getcolor(color)
        linefmt = kwargs.pop('linefmt', None)
        if linefmt is None:
            linefmt = PolylineBreak()
            linefmt.setColor(color is None and Color.red or color)
        else:
            linefmt = plotutil.getlegendbreak('line', **linefmt)[0]
        linefmt.setCaption(label)
        markerfmt = kwargs.pop('markerfmt', None)
        if markerfmt is None:
            markerfmt = PointBreak()
            markerfmt.setOutlineColor(color is None and Color.red or color)
            markerfmt.setDrawFill(False)
        else:
            markerfmt = plotutil.getlegendbreak('point', **markerfmt)[0]
        basefmt = kwargs.pop('basefmt', None)
        if basefmt is None:
            basefmt = PolylineBreak()
            basefmt.setColor(color is None and Color.red or color)
        else:
            basefmt = plotutil.getlegendbreak('line', **basefmt)[0]
        
        #Create stem graphics
        graphics = GraphicFactory.createStems(xdata, ydata, linefmt, markerfmt, \
            basefmt, bottom)       

        self.add_graphic(graphics)
        self.axes.setAutoExtent()

        return linefmt
        
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
        :param smooth: (*boolean*) Smooth countour lines or not.
        
        :returns: (*VectoryLayer*) Contour VectoryLayer created from array data.
        """
        n = len(args)
        ls = kwargs.pop('symbolspec', None)
        cmap = plotutil.getcolormap(**kwargs)
        fill_value = kwargs.pop('fill_value', -9999.0)
        xaxistype = None
        if n <= 2:
            gdata = minum.asgriddata(args[0])
            if isinstance(args[0], DimArray):
                if args[0].islondim(1):
                    xaxistype = 'lon'
                elif args[0].islatdim(1):
                    xaxistype = 'lat'
                elif args[0].istimedim(1):
                    xaxistype = 'time'
            args = args[1:]
        elif n <=4:
            x = args[0]
            y = args[1]
            a = args[2]
            gdata = minum.asgriddata(a, x, y, fill_value)
            args = args[3:]
        if ls is None:
            if len(args) > 0:
                level_arg = args[0]
                if isinstance(level_arg, int):
                    cn = level_arg
                    ls = LegendManage.createLegendScheme(gdata.min(), gdata.max(), cn, cmap)
                else:
                    if isinstance(level_arg, MIArray):
                        level_arg = level_arg.aslist()
                    ls = LegendManage.createLegendScheme(gdata.min(), gdata.max(), level_arg, cmap)
            else:    
                ls = LegendManage.createLegendScheme(gdata.min(), gdata.max(), cmap)
            ls = ls.convertTo(ShapeTypes.Polyline)
            plotutil.setlegendscheme(ls, **kwargs)
        
        smooth = kwargs.pop('smooth', True)
        igraphic = GraphicFactory.createContourLines(gdata.data, ls, smooth)

        if not xaxistype is None:
            self.set_xaxis_type(xaxistype)
            self.axes.updateDrawExtent()
        self.add_graphic(igraphic)
        #self.axes.setAutoExtent()
        self.axes.setExtent(igraphic.getExtent())
        self.axes.setDrawExtent(igraphic.getExtent())

        return igraphic
        
    def clabel(self, layer, **kwargs):
        '''
        Add contour layer labels.
        
        :param layer: (*MILayer*) The contour layer.
        :param fontname, fontsize: The font auguments.
        :param color: (*color*) The label color. Default is ``None``, the label color will be set as
            same as color of the line.
        :param dynamic: (*boolean*) Draw labels dynamic or not. Default is ``True``.
        :param drawshadow: (*boolean*) Draw shadow under labels or not.
        :param fieldname: (*string*) The field name used for label.
        :param xoffset: (*int*) X offset of the labels.
        :param yoffset: (int*) Y offset of the labels.
        :param avoidcoll: (*boolean*) Avoid labels collision or not.
        '''    
        color = kwargs.pop('color', None)    
        gc = layer
        if isinstance(layer, MILayer):
            gc = layer.layer   
        dynamic = kwargs.pop('dynamic', True)
        if gc.getShapeType() != ShapeTypes.Polyline:
            dynamic = False
        drawshadow = kwargs.pop('drawshadow', dynamic)    
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
        labelset.setDrawShadow(drawshadow)
        xoffset = kwargs.pop('xoffset', 0)
        labelset.setXOffset(xoffset)
        yoffset = kwargs.pop('yoffset', 0)
        labelset.setYOffset(yoffset)
        avoidcoll = kwargs.pop('avoidcoll', True)
        labelset.setAvoidCollision(avoidcoll)    
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
        :param smooth: (*boolean*) Smooth countour lines or not.
        
        :returns: (*VectoryLayer*) Contour filled VectoryLayer created from array data.
        """
        n = len(args)    
        ls = kwargs.pop('symbolspec', None)
        cmap = plotutil.getcolormap(**kwargs)
        fill_value = kwargs.pop('fill_value', -9999.0)
        xaxistype = None
        if n <= 2:
            gdata = minum.asgriddata(args[0])
            if isinstance(args[0], DimArray):
                if args[0].islondim(1):
                    xaxistype = 'lon'
                elif args[0].islatdim(1):
                    xaxistype = 'lat'
                elif args[0].istimedim(1):
                    xaxistype = 'time'
            args = args[1:]
        elif n <=4:
            x = args[0]
            y = args[1]
            a = args[2]
            gdata = minum.asgriddata(a, x, y, fill_value)
            args = args[3:]
        if ls is None:
            if len(args) > 0:
                level_arg = args[0]
                if isinstance(level_arg, int):
                    cn = level_arg
                    ls = LegendManage.createLegendScheme(gdata.min(), gdata.max(), cn, cmap)
                else:
                    if isinstance(level_arg, MIArray):
                        level_arg = level_arg.aslist()
                    ls = LegendManage.createLegendScheme(gdata.min(), gdata.max(), level_arg, cmap)
            else:    
                ls = LegendManage.createLegendScheme(gdata.min(), gdata.max(), cmap)
        ls = ls.convertTo(ShapeTypes.Polygon)
        plotutil.setlegendscheme(ls, **kwargs)
        smooth = kwargs.pop('smooth', True)
        igraphic = GraphicFactory.createContourPolygons(gdata.data, ls, smooth)
        
        visible = kwargs.pop('visible', True)
        if visible:
            if not xaxistype is None:
                self.set_xaxis_type(xaxistype)
                self.axes.updateDrawExtent()
            self.add_graphic(igraphic)
            #self.setAutoExtent()
            self.axes.setExtent(igraphic.getExtent())
            self.axes.setDrawExtent(igraphic.getExtent())
   
        return igraphic

    def imshow(self, *args, **kwargs):
        """
        Display an image on the axes.
        
        :param X: (*array_like*) 2-D or 3-D (RGB or RGBA) image value array or BufferedImage.
        :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level curves 
            to draw, in increasing order.
        :param cmap: (*string*) Color map string.
        :param colors: (*list*) If None (default), the colormap specified by cmap will be used. If a 
            string, like ‘r’ or ‘red’, all levels will be plotted in this color. If a tuple of matplotlib 
            color args (string, float, rgb, etc), different levels will be plotted in different colors in 
            the order specified.
        :param interpolation: (*string*) Interpolation option [None | bilinear | bicubic].
        
        :returns: (*Image graphic*) Image graphic created from array data.
        """
        n = len(args)
        cmap = plotutil.getcolormap(**kwargs)
        fill_value = kwargs.pop('fill_value', -9999.0)
        xaxistype = None
        isrgb = False
        isimage = False
        extent = None
        if n >= 3:
            xdata = args[0]
            ydata = args[1]
            extent = [xdata[0],xdata[-1],ydata[0],ydata[-1]]
            args = args[2:]
        X = args[0]
        if isinstance(X, (list, tuple)):
            isrgb = True
        elif isinstance(X, BufferedImage):
            isimage = True
        elif X.ndim > 2:
            isrgb = True
        else:
            gdata = minum.asgridarray(X)
            if isinstance(X, DimArray):
                if X.islondim(1):
                    xaxistype = 'lon'
                elif X.islatdim(1):
                    xaxistype = 'lat'
                elif X.istimedim(1):
                    xaxistype = 'time'
        args = args[1:]   
        
        extent = kwargs.pop('extent', extent)
        if isrgb:
            if isinstance(X, (list, tuple)):
                rgbd = []
                for d in rgbdata:
                    rgbd.append(d.asarray())
                rgbdata = rgbd
            else:
                rgbdata = X.asarray()
            igraphic = GraphicFactory.createImage(rgbdata, extent)
            ls = None
        elif isimage:
            igraphic = GraphicFactory.createImage(X)
            ls = None
        else:
            ls = kwargs.pop('symbolspec', None)
            if ls is None:
                if len(args) > 0:
                    level_arg = args[0]
                    if isinstance(level_arg, int):
                        cn = level_arg
                        ls = LegendManage.createImageLegend(gdata, cn, cmap)
                    else:
                        if isinstance(level_arg, MIArray):
                            level_arg = level_arg.aslist()
                        ls = LegendManage.createImageLegend(gdata, level_arg, cmap)
                else:
                    ls = plotutil.getlegendscheme(args, gdata.min(), gdata.max(), **kwargs)
                ls = ls.convertTo(ShapeTypes.Image)
                plotutil.setlegendscheme(ls, **kwargs)
                
            igraphic = GraphicFactory.createImage(gdata, ls, extent)
        interpolation = kwargs.pop('interpolation', None)
        if not interpolation is None:
            igraphic.getShape().setInterpolation(interpolation)

        if not xaxistype is None:
            self.set_xaxis_type(xaxistype)
            self.axes.updateDrawExtent()
        self.add_graphic(igraphic)
        self.axes.setAutoExtent()
        gridline = self.axes.getGridLine()
        gridline.setTop(True)

        if ls is None:
            return igraphic
        else:
            return ls
        
    def pcolor(self, *args, **kwargs):
        '''
        Draw a pseudocolor plot.
        
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
        '''
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
        if a.ndim == 2 and x.ndim == 1:            
            x, y = minum.meshgrid(x, y)            
        ls = plotutil.getlegendscheme(args, a.min(), a.max(), **kwargs)   
        ls = ls.convertTo(ShapeTypes.Polygon)
        plotutil.setlegendscheme(ls, **kwargs)
        graphics = GraphicFactory.createPColorPolygons(x.asarray(), y.asarray(), a.asarray(), ls)            
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
            self.axes.setExtent(graphics.getExtent())
            self.axes.setDrawExtent(graphics.getExtent())
        return graphics
        
    def gridshow(self, *args, **kwargs):
        '''
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
        '''
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
        ls = ls.convertTo(ShapeTypes.Polygon)
        plotutil.setlegendscheme(ls, **kwargs)
        graphics = GraphicFactory.createGridPolygons(x.asarray(), y.asarray(), a.asarray(), ls)            
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
            self.axes.setExtent(graphics.getExtent())
            self.axes.setDrawExtent(graphics.getExtent())
        return graphics
        
    def text(x, y, s, **kwargs):
        """
        Add text to the axes. Add text in string *s* to axis at location *x* , *y* , data
        coordinates.
        
        :param x: (*float*) Data x coordinate.
        :param y: (*float*) Data y coordinate.
        :param s: (*string*) Text.
        :param fontname: (*string*) Font name. Default is ``Arial`` .
        :param fontsize: (*int*) Font size. Default is ``14`` .
        :param bold: (*boolean*) Is bold font or not. Default is ``False`` .
        :param color: (*color*) Tick label string color. Default is ``black`` .
        :param coordinates=['axes'|'figure'|'data'|'inches']: (*string*) Coordinate system and units for 
            *X, Y*. 'axes' and 'figure' are normalized coordinate system with 0,0 in the lower left and 
            1,1 in the upper right, 'data' are the axes data coordinates (Default value); 'inches' is 
            position in the figure in inches, with 0,0 at the lower left corner.
        """
        ctext = plotutil.text(x, y, s, **kwargs)
        self.axes.addText(ctext)
        return ctext
    
    def arrow(self, x, y, dx, dy, **kwargs):
        '''
        Add an arrow to the axes.
        
        :param x: (*float*) X coordinate.
        :param y: (*float*) Y coordinate.
        :param dx: (*float*) The length of arrow along x direction. 
        :param dy: (*float*) The length of arrow along y direction.
        
        :returns: Arrow graphic.
        '''
        if not kwargs.has_key('facecolor'):
            kwargs['facecolor'] = (51,204,255)
        apb, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        apb = plotutil.polygon2arrow(apb, **kwargs)
        graphic = GraphicFactory.createArrow(x, y, dx, dy, apb)
            
        self.add_graphic(graphic)
        self.axes.setAutoExtent()
        
        return graphic
    
    def arrowline(self, x, y, dx=0, dy=0, **kwargs):
        '''
        Add an arrow line to the axes.
        
        :param x: (*float or array_like*) X coordinates.
        :param y: (*float or array_like*) Y coordinates.
        :param dx: (*float*) The length of arrow along x direction. Only valid when x is float.
        :param dy: (*float*) The length of arrow along y direction. Only valid when y is float.
        
        :returns: Arrow line graphic.
        '''
        if isinstance(x, (list, tuple)):
            x = minum.array(x)
        if isinstance(y, (list, tuple)):
            y = minum.array(y)
            
        alb, isunique = plotutil.getlegendbreak('line', **kwargs)
        alb = plotutil.line2arrow(alb, **kwargs)
        if isinstance(x, MIArray):
            iscurve = kwargs.pop('iscurve', False)
            graphic = GraphicFactory.createArrowLine(x.array, y.array, alb, iscurve)
        else:
            graphic = GraphicFactory.createArrowLine(x, y, dx, dy, alb)
            
        self.add_graphic(graphic)
        self.axes.setAutoExtent()
        
        return graphic
        
    def annotate(self, s, xy, *args, **kwargs):
        '''
        Annotate the point xy with text s.
        
        :param s: (*string*) The text of the annotation.
        :param xy: (*float, float*) The point (x,y) to annotate.
        :param xytext: (*float, float*) The position (x,y) to place the text at. If None, 
            defaults to xy.
        :param arrowprops: (*dict*) Arrow properties.
            
        :returns: Annotation.
        '''
        if len(args) > 0:
            xytext = args[0]
        else:
            xytext = xy
        
        ctext = plotutil.text(xytext[0], xytext[1], s, **kwargs)
        self.axes.addText(ctext)
                
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
        self.axes.setAutoExtent()
        
        return ctext, graphic
    
    def patch(self, x, y=None, **kwargs):
        '''
        Create one or more filled polygons.
        
        :param x: (*array_like*) X coordinates for each vertex. X should be PolygonShape if y
            is None.
        :param y: (*array_like*) Y coordinates for each vertex.
        '''
        lbreak, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        if y is None:
            graphics = Graphic(x, lbreak)
        else:
            x = plotutil.getplotdata(x)
            y = plotutil.getplotdata(y)
            graphics = GraphicFactory.createPolygons(x, y, lbreak)

        self.add_graphic(graphics)
        self.axes.setAutoExtent()
        return graphics
        
    def rectangle(self, position, curvature=None, **kwargs):
        '''
        Create one or more filled polygons.
        
        :param position: (*list*) Position of the rectangle [x, y, width, height].
        :param curvature: (*list*) Curvature of the rectangle [x, y]. Default is None.
        '''
        lbreak, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        if isinstance(curvature, (int, float)):
            curvature = [curvature, curvature]
        graphic = GraphicFactory.createRectangle(position, curvature, lbreak)

        self.add_graphic(graphic)
        self.axes.setAutoExtent()
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
        #Add data series
        label = kwargs.pop('label', 'S_0')
        dn = len(x)
        xdata = plotutil.getplotdata(x)
        if isinstance(y1, (int, long, float)):
            yy = []
            for i in range(dn):
                yy.append(y1)
            y1 = minum.array(yy).array
        else:
            y1 = plotutil.getplotdata(y1)
        if isinstance(y2, (int, long, float)):
            yy = []
            for i in range(dn):
                yy.append(y2)
            y2 = minum.array(yy).array
        else:
            y2 = plotutil.getplotdata(y2)
        if not where is None:
            if isinstance(where, (tuple, list)):
                where = minum.array(where)
            where = where.asarray()
        
        #Set plot data styles
        if not 'fill' in kwargs:
            kwargs['fill'] = True
        if not 'edge' in kwargs:
            kwargs['edge'] = False
        pb, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        pb.setCaption(label)
        
        #Create graphics
        graphics = GraphicFactory.createFillBetweenPolygons(xdata, y1, y2, where, pb)    
        self.add_graphic(graphics)
        self.axes.setAutoExtent()

        return pb 
        
    def fill_betweenx(self, y, x1, x2=0, where=None, **kwargs):
        """
        Make filled polygons between two curves (x1 and x2) where ``where==True``.
        
        :param y: (*array_like*) An N-length array of the y data.
        :param x1: (*array_like*) An N-length array (or scalar) of the x data.
        :param x2: (*array_like*) An N-length array (or scalar) of the x data.
        :param where: (*array_like*) If None, default to fill between everywhere. If not None, it is an 
            N-length boolean array and the fill will only happen over the regions where ``where==True``.
        """
        #Add data series
        label = kwargs.pop('label', 'S_0')
        dn = len(y)
        ydata = plotutil.getplotdata(y)
        if isinstance(x1, (int, long, float)):
            xx = []
            for i in range(dn):
                xx.append(x1)
            x1 = minum.array(xx).array
        else:
            x1 = plotutil.getplotdata(x1)
        if isinstance(x2, (int, long, float)):
            xx = []
            for i in range(dn):
                xx.append(x2)
            x2 = minum.array(xx).array
        else:
            x2 = plotutil.getplotdata(x2)
        if not where is None:
            if isinstance(where, (tuple, list)):
                where = minum.array(where)
            where = where.asarray()
        
        #Set plot data styles
        if not 'fill' in kwargs:
            kwargs['fill'] = True
        if not 'edge' in kwargs:
            kwargs['edge'] = False
        pb, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        pb.setCaption(label)
        
        #Create graphics
        graphics = GraphicFactory.createFillBetweenPolygonsX(ydata, x1, x2, where, pb)    
        self.add_graphic(graphics)
        self.axes.setAutoExtent()

        return pb

    def pie(self, x, explode=None, labels=None, colors=None, autopct=None, pctdistance=0.6, shadow=False, 
        labeldistance=1.1, startangle=0, radius=None, hold=None, **kwargs):
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
        :radius: (*float*) The radius of the pie, if *radius* is *None* it will be set to 1.
        :param fontname: (*string*) Font name. Default is ``Arial`` .
        :param fontsize: (*int*) Font size. Default is ``14`` .
        
        :returns: (*tuple*) Patches and texts.
        """        
        n = len(x)
        x = plotutil.getplotdata(x)
        if colors is None:
            colors = plotutil.makecolors(n)
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
        
        #Create graphics
        graphics = GraphicFactory.createPieArcs(x, colors, labels, startangle, explode, font, fontcolor, \
            labeldistance, autopct, pctdistance)

        for graphic in graphics:
            self.add_graphic(graphic)
        self.axes.setAutoExtent()
        self.axes.setAutoAspect(False)
        self.axes.getAxis(Location.BOTTOM).setVisible(False)
        self.axes.getAxis(Location.LEFT).setVisible(False)
        self.axes.getAxis(Location.TOP).setVisible(False)
        self.axes.getAxis(Location.RIGHT).setVisible(False)

        if len(graphics) == 2:
            return graphics[0], graphics[1]
        else:
            return graphics[0], graphics[1], graphics[2]
        
    def boxplot(self, x, sym=None, positions=None, widths=None, color=None, showcaps=True, showfliers=True, showmeans=False, \
            meanline=False, boxprops=None, medianprops=None, meanprops=None, whiskerprops=None, capprops=None, flierprops=None):
        """
        Make a box and whisker plot.
        
        Make a box and whisker plot for each column of x or each vector in sequence x. The box extends from lower
        to upper quartile values of the data, with a line at the median. The whiskers extend from the box to show
        the range of the data. Flier points are those past the end of the whiskers.
        
        :param x: (*Array or a sequence of vectors*) The input data.
        :param sym: (*string*) The default symbol for flier points. Enter an empty string ('') if you don’t 
            want to show fliers. If None, then the fliers default to ‘b+’ If you want more control use the 
            flierprops kwarg.
        :param positions: (*array_like*) Sets the positions of the boxes. The ticks and limits are automatically 
            set to match the positions. Defaults to range(1, N+1) where N is the number of boxes to be drawn.
        :param widths: (*scalar or array_like*) Sets the width of each box either with a scalar or a sequence. 
            The default is 0.5, or 0.15*(distance between extreme positions), if that is smaller.
        :param color: (*Color*) Color for all parts of the box plot. Defaul is None.
        :param showcaps: (*boolean*) Show the caps on the ends of whiskers. Default is ``True``.
        :param showfliers: (*boolean*) Show the outliers beyond the caps. Defaul is ``True``.
        :param showmeans: (*boolean*) Default is ``False``. Show the mean or not.
        :param meanline: (*boolean*) Default is ``False``. If ``True`` (and showmeans is ``True``), will try to render
            the mean as a line spanning. Otherwise, means will be shown as points.
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
            if isinstance(positions, (MIArray, DimArray)):
                positions = positions.tolist()
        
        if not widths is None:
            if isinstance(widths, (int, float)):
                nwidths = []
                for i in range(len(x)):
                    nwidths.append(widths)
                widths = nwidths
            elif isinstance(widths, (MIArray, DimArray)):
                widths = widths.tolist()
            
        #Get box plot properties
        if not color is None:
            color = plotutil.getcolor(color)
        if not sym is None:
            sym = plotutil.getplotstyle(sym, '')
            sym.setDrawFill(False)
            if not color is None:
                sym.setColor(color)
                sym.setOutlineColor(color)
        if boxprops is None:
            boxprops = PolygonBreak()
            boxprops.setDrawFill(False)
            boxprops.setOutlineColor(color is None and Color.blue or color)
        else:
            boxprops = plotutil.getlegendbreak('polygon', **boxprops)[0]
        if medianprops is None:
            medianprops = PolylineBreak()
            medianprops.setColor(color is None and Color.red or color)
        else:
            medianprops = plotutil.getlegendbreak('line', **medianprops)[0]
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
                meanprops.setStyle(PointStyle.Square)
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
            flierprops.setStyle(PointStyle.Plus)
        
        #Create graphics
        graphics = GraphicFactory.createBox(x, positions, widths, showcaps, showfliers, showmeans, boxprops, \
            medianprops, whiskerprops, capprops, meanprops, flierprops)

        self.add_graphic(graphics)
        self.axes.setAutoExtent()

        return graphics
        
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
        :param size: (*float*) Base size of the arrows.
        :param order: (*int*) Z-order of created layer for display.
        
        :returns: (*VectoryLayer*) Created barbs VectoryLayer.
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
            x, y = minum.meshgrid(x, y)
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
                        if isinstance(levs, MIArray):
                            levs = levs.aslist()
                        ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), levs, cmap)
            else:    
                if cmap.getColorCount() == 1:
                    c = cmap.getColor(0)
                else:
                    c = Color.black
                ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Point, c, 10)
            ls = plotutil.setlegendscheme_point(ls, **kwargs)
        
        if not cdata is None:
            cdata = plotutil.getplotdata(cdata)
        igraphic = GraphicFactory.createBarbs(x, y, u, v, cdata, ls, isuv)

        if not xaxistype is None:
            self.set_xaxis_type(xaxistype)
            self.axes.updateDrawExtent()
        self.add_graphic(igraphic)
        self.axes.setAutoExtent()

        return igraphic
        
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
        
        :returns: (*VectoryLayer*) Created quiver VectoryLayer.
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
            x = args[0].dimvalue(1)
            y = args[0].dimvalue(0)
            x, y = minum.meshgrid(x, y)
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
                        if isinstance(levs, MIArray):
                            levs = levs.tolist()
                        ls = LegendManage.createLegendScheme(cdata.min(), cdata.max(), levs, cmap)
            else:    
                if cmap.getColorCount() == 1:
                    c = cmap.getColor(0)
                else:
                    c = Color.black
                ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Point, c, 10)
            ls = plotutil.setlegendscheme_arrow(ls, **kwargs)
        
        if not cdata is None:
            cdata = plotutil.getplotdata(cdata)
        igraphic = GraphicFactory.createArrows(x, y, u, v, cdata, ls, isuv)

        if not xaxistype is None:
            self.set_xaxis_type(xaxistype)
            self.axes.updateDrawExtent()
        self.add_graphic(igraphic)
        self.axes.setAutoExtent()

        return igraphic
        
    def quiverkey(self, *args, **kwargs):
        """
        Add a key to a quiver plot.
        
        :param Q: (*MILayer or GraphicCollection*) The quiver layer instance returned by a call to quiver/quiverm.
        :param X: (*float*) The location x of the key.
        :param Y: (*float*) The location y of the key.
        :param U: (*float*) The length of the key.
        :param label: (*string*) A string with the length and units of the key.
        :param coordinates=['axes'|'figure'|'data']: (*string*) Coordinate system and units for 
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
            wa.setLayer(Q.layer)
        else:
            wa.setLayer(Q)
        X = args[1]
        Y = args[2]
        wa.setX(X)
        wa.setY(Y)
        U = args[3]
        wa.setLength(U)
        if len(args) == 5:
            label = args[4]
            wa.setLabel(label)
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
        self.axes.setWindArrow(wa)
        
    def get_legend(self):
        '''
        Get legend of the axes.
        
        :return: Legend
        '''
        return self.axes.getLegendScheme()
        
    def legend(self, *args, **kwargs):
        """
        Places a legend on the axes.
        
        :param breaks: (*ColorBreak*) Legend breaks (optional).
        :param labels: (*list of string*) Legend labels (optional).
        :param orientation: (*string*) Colorbar orientation: ``vertical`` or ``horizontal``.
        :param loc: (*string*) The location of the legend, including: 'upper right', 'upper left',
            'lower left', 'lower right', 'right', 'ceter left', 'center right', lower center',
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
        :param markerscale: (*float*) Marker symbol scale.
        :param markerwidth: (*float*) Marker symbol width.
        :param markerheight: (*float*) Marker symbol height.
        :param ncol: (*float*) Column number of the legend.
        :param xshift: (*float*) X shift.
        :param yshift: (*float*) Y shift.
        
        :returns: (*ChartLegend*) The chart legend.
        """ 
        newlegend = kwargs.pop('newlegend', True)
        ols = self.axes.getLegendScheme()
        if newlegend:
            clegend = ChartLegend(ols)
        else:
            clegend = self.axes.getLegend()   
        ls = kwargs.pop('legend', None)
        if len(args) > 0:
            if isinstance(args[0], MILayer):
                ls = args[0].legend()
                args = args[1:]
            elif isinstance(args[0], LegendScheme):
                ls = args[0]
                args = args[1:]
            elif isinstance(args[0], GraphicCollection):
                if not args[0].isSingleLegend():
                    ls = args[0].getLegendScheme()
                    args = args[1:]
        if ls is None:
            if len(args) > 0:
                lbs = []
                for lb in args[0]:
                    if isinstance(lb, Graphic):
                        lbs.append(lb.getLegend().clone())
                    else:
                        lbs.append(lb)
                if len(args) == 2:
                    labels = args[1]
                    for i in range(0, len(lbs)):
                        lbs[i].setCaption(labels[i])
                if isinstance(lbs[0], basestring):
                    clegend.setTickCaptions(lbs)
                else:
                    ls = LegendScheme()
                    for lb in lbs:
                        ls.addLegendBreak(lb)
                    if lbs[0].getStartValue() == lbs[1].getEndValue():
                        ls.setLegendType(LegendType.UniqueValue)
                    else:
                        ls.setLegendType(LegendType.GraduatedColor)
                    if clegend is None:
                        clegend = ChartLegend(ls)
                        self.axes.setLegend(clegend)
                    else:
                        clegend.setLegendScheme(ls)
        else:
            if len(args) > 0:
                labels = args[0]
                for i in range(len(labels)):
                    if i < ls.getBreakNum():
                        ls.getLegendBreak(i).setCaption(labels[i])
            if clegend is None:
                clegend = ChartLegend(ls)
                self.axes.setLegend(clegend)
            else:
                clegend.setLegendScheme(ls)
            
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
            self.axes.addLegend(clegend)

        return clegend
        
    def colorbar(self, mappable, **kwargs):
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
            Defaul is ``out``.
        :param newlegend: (*boolean*) Add a new legend or replace existing one.
        :param extendrect: (*boolean*) If ``True`` the minimum and maximum colorbar extensions will be
            rectangular (the default). If ``False`` the extensions will be triangular.
        :param extendfrac: [None | 'auto' | length] If set to *None*, both the minimum and maximum triangular
            colorbar extensions with have a length of 5% of the interior colorbar length (the default). If
            set to 'auto', makes the triangular colorbar extensions the same lengths as the interior boxes
            . If a scalar, indicates the length of both the minimum and maximum triangle colorbar extensions
            as a fraction of the interior colorbar length.
        :param ticks: [None | list of ticks] If None, ticks are determined automatically from the input.
        :param ticklabels: [None | list of ticklabels] Tick labels.
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
        if isinstance(mappable, MILayer):
            ls = mappable.legend()
        elif isinstance(mappable, LegendScheme):
            ls = mappable
        elif isinstance(mappable, GraphicCollection):
            ls = mappable.getLegendScheme()
        else:
            ls = plotutil.makelegend(mappable)
        
        newlegend = kwargs.pop('newlegend', True)
        if newlegend:
            legend = ChartColorBar(ls)
            self.axes.addLegend(legend)
        else:
            legend = self.axes.getLegend()   
            if legend is None:
                legend = ChartColorBar(ls)
                self.axes.setLegend(legend)
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
        if orientation == 'horizontal':
            legend.setPlotOrientation(PlotOrientation.HORIZONTAL)
            legend.setPosition(LegendPosition.LOWER_CENTER_OUTSIDE)
        else:
            legend.setPlotOrientation(PlotOrientation.VERTICAL)
            legend.setPosition(LegendPosition.RIGHT_OUTSIDE)
        legend.setDrawNeatLine(False)
        extendrect = kwargs.pop('extendrect', True)
        legend.setExtendRect(extendrect)
        extendfrac = kwargs.pop('extendfrac', None)
        if extendfrac == 'auto':
            legend.setAutoExtendFrac(True)
        tickin = kwargs.pop('tickin', None)
        if not tickin is None:
            legend.setInsideTick(tickin)
        ticklen = kwargs.pop('ticklen', None)
        if not ticklen is None:
            legend.setTickLength(ticklen)
        ticks = kwargs.pop('ticks', None)
        if not ticks is None:
            if isinstance(ticks, MIArray):
                ticks = ticks.aslist()
            legend.setTickLocations(ticks)
        ticklabels = kwargs.pop('ticklabels', None)
        if not ticklabels is None:
            if isinstance(ticklabels, (MIArray, DimArray)):
                ticklabels = ticklabels.aslist()
            if ls.getLegendType() == LegendType.UniqueValue:
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


###############################################
class PolarAxes(Axes):
    '''
    Axes with polar coordinate.
    '''
    
    def __init__(self, axes=None, figure=None):
        if axes is None:
            self.axes = PolarPlot()
        else:
            self.axes = axes
        self.axestype = 'polar'
        self.figure = figure
    
    def set_rmax(self, rmax):
        '''
        Set radial max circle.
        
        :param rmax: (*float*) Radial max value.
        '''
        self.axes.setRadius(rmax)
        
    def set_rlabel_position(self, pos):
        '''
        Updates the theta position of the radial labels.
        
        :param pos: (*float*) The angular position of the radial labels in degrees.
        '''
        if isinstance(pos, (DimArray, MIArray)):
            pos = pos.tolist()
        self.axes.setYTickLabelPos(pos)
        
    def set_rticks(self, ticks):
        '''
        Set radial ticks.
        
        :param ticks: (*string list*) Tick labels.
        '''
        self.axes.setYTickLabels(ticks)
        
    def set_rtick_format(self, fmt=''):
        '''
        Set radial tick format.
        
        :param ftm: (*string*) Tick format ['' | '%'].
        '''
        self.axes.setYTickFormat(fmt)
        
    def set_rtick_locations(self, loc):
        '''
        Set radial tick locations.
        
        :param loc: (*float list*) Tick locations.
        '''
        if isinstance(loc, (DimArray, MIArray)):
            loc = loc.tolist()
        self.axes.setYTickLocations(loc)
        
    def set_xtick_locations(self, loc):
        '''
        Set angular tick locations.
        
        :param loc: (*float list*) Tick locations.
        '''
        if isinstance(loc, (DimArray, MIArray)):
            loc = loc.tolist()
        self.axes.setXTickLocations(loc)
        
    def set_xticks(self, ticks):
        '''
        Set angular ticks.
        
        :param ticks: (*string list*) Tick labels.
        '''
        self.axes.setXTickLabels(ticks)
        
    def set_rtick_font(self, name=None, size=None, style=None):
        '''
        Set radial tick font.
        
        :param name: (*string*) Font name.
        :param size: (*int*) Font size.
        :param style: (*string*) Font style.
        '''
        font = self.axes.getYTickFont()
        if name is None:
            name = font.getName()
        if size is None:
            size = font.getSize()
        if style is None:
            style = font.getStyle()
        else:
            if style.lower() == 'bold':
                style = Font.BOLD
            elif style.lower() == 'italic':
                style = Font.ITALIC
            else:
                style = Font.PLAIN
        font = Font(name, style, size)
        self.axes.setYTickFont(font)
        
    def set_xtick_font(self, name=None, size=None, style=None):
        '''
        Set angular tick font.
        
        :param name: (*string*) Font name.
        :param size: (*int*) Font size.
        :param style: (*string*) Font style.
        '''
        font = self.axes.getXTickFont()
        if name is None:
            name = font.getName()
        if size is None:
            size = font.getSize()
        if style is None:
            style = font.getStyle()
        else:
            if style.lower() == 'bold':
                style = Font.BOLD
            elif style.lower() == 'italic':
                style = Font.ITALIC
            else:
                style = Font.PLAIN
        font = Font(name, style, size)
        self.axes.setXTickFont(font)
        
    def data2pixel(self, x, y, z=None):
        '''
        Transform data coordinate to screen coordinate
        
        :param x: (*float*) X coordinate.
        :param y: (*float*) Y coordinate.
        :param z: (*float*) Z coordinate - only used for 3-D axes.
        '''
        r = MIMath.polarToCartesian(x, y) 
        x = r[0]
        y = r[1]
        rect = self.axes.getPositionArea()
        r = self.axes.projToScreen(x, y, rect)
        sx = r[0] + rect.getX()
        sy = r[1] + rect.getY()
        sy = self.figure.get_size()[1] - sy
        return sx, sy
                
        
########################################################3
class Test():
    def test():
        print 'Test...'