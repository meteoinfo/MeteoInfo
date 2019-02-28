# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2018-4-4
# Purpose: MeteoInfoLab figure module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.chart import ChartPanel, Chart, Location, MouseMode, ChartText

import plotutil
from axes import Axes, PolarAxes
from mapaxes import MapAxes
from axes3d import Axes3D

from java.awt import Font

class Figure(ChartPanel):
    '''
    top level container for all plot elements
    '''
    
    def __init__(self, figsize=None, dpi=None, bgcolor='w'):
        '''
        Constructor
        
        :param figsize: (*list*) Optional, width and height of the figure such as ``[600, 400]``.
        :param bgcolor: (*Color*) Optional, background color of the figure. Default is ``w`` (white).
        :param dpi: (*int*) Dots per inch.
        '''
        chart = Chart()
        chart.setBackground(plotutil.getcolor(bgcolor))
        if figsize is None:
            super(Figure, self).__init__(chart)
        else:
            super(Figure, self).__init__(chart, figsize[0], figsize[1])
        self.axes = []
        self.current_axes = -1
            
    def get_size(self):
        '''
        Get figure size.
        
        :returns: Figure width and height
        '''
        return self.getFigureWidth(), self.getFigureHeight()
     
    def __create_axes(self, *args, **kwargs):
        """
        Create an axes.
        
        :param position: (*list*) Optional, axes position specified by *position=* [left, bottom, width
            height] in normalized (0, 1) units. Default is [0.13, 0.11, 0.775, 0.815].
        :param outerposition: (*list*) Optional, axes size and location, including labels and margin.
        
        :returns: The axes.
        """        
        if len(args) > 0:
            position = args[0]
        else:
            position = kwargs.pop('position', None)    
        outerposition = kwargs.pop('outerposition', None)
        axestype = kwargs.pop('axestype', 'cartesian')
        polar = kwargs.pop('polar', False)
        if polar:
            axestype = 'polar'
        if axestype == 'polar':
            ax = PolarAxes()
        elif axestype == 'map':
            ax = MapAxes()
        elif axestype == '3d':
            ax = Axes3D()
        else:
            ax = Axes()
        if position is None:
            position = [0.13, 0.11, 0.775, 0.815]
            ax.active_outerposition(True)
        else:        
            ax.active_outerposition(False)        
        ax.set_position(position)   
        if not outerposition is None:
            ax.set_outerposition(outerposition)
            ax.active_outerposition(True)
        
        return ax
        
    def __set_axes_common(self, ax, *args, **kwargs):
        if len(args) > 0:
            position = args[0]
        else:
            position = kwargs.pop('position', None)    
        outerposition = kwargs.pop('outerposition', None)
        if position is None:
            if ax.axestype == '3d':
                position = [0.13, 0.11, 0.71, 0.815]
            else:
                position = [0.13, 0.11, 0.775, 0.815]
            ax.active_outerposition(True)
        else:        
            ax.active_outerposition(False)        
        ax.set_position(position)   
        if not outerposition is None:
            ax.set_outerposition(outerposition)
            ax.active_outerposition(True)
        units = kwargs.pop('units', None)
        if not units is None:
            ax.axes.setUnits(units)
        
    def __set_axes(self, ax, **kwargs):
        """
        Set an axes.

        :param aspect: (*string*) ['equal' | 'auto'] or a number. If a number the ratio of x-unit/y-unit in screen-space.
            Default is 'auto'.
        :param bgcolor: (*Color*) Optional, axes background color.
        :param axis: (*boolean*) Optional, set all axis visible or not. Default is ``True`` .
        :param bottomaxis: (*boolean*) Optional, set bottom axis visible or not. Default is ``True`` .
        :param leftaxis: (*boolean*) Optional, set left axis visible or not. Default is ``True`` .
        :param topaxis: (*boolean*) Optional, set top axis visible or not. Default is ``True`` .
        :param rightaxis: (*boolean*) Optional, set right axis visible or not. Default is ``True`` .
        :param xaxistype: (*string*) Optional, set x axis type as 'normal', 'lon', 'lat' or 'time'.
        :param xreverse: (*boolean*) Optional, set x axis reverse or not. Default is ``False`` .
        :param yreverse: (*boolean*) Optional, set yaxis reverse or not. Default is ``False`` .
        
        :returns: The axes.
        """        
        aspect = kwargs.pop('aspect', 'auto')
        axis = kwargs.pop('axis', True)
        b_axis = ax.get_axis(Location.BOTTOM)
        l_axis = ax.get_axis(Location.LEFT)
        t_axis = ax.get_axis(Location.TOP)
        r_axis = ax.get_axis(Location.RIGHT)
        if axis:
            bottomaxis = kwargs.pop('bottomaxis', True)
            leftaxis = kwargs.pop('leftaxis', True)
            topaxis = kwargs.pop('topaxis', True)
            rightaxis = kwargs.pop('rightaxis', True)
        else:
            bottomaxis = False
            leftaxis = False
            topaxis = False
            rightaxis = False
        xreverse = kwargs.pop('xreverse', False)
        yreverse = kwargs.pop('yreverse', False)
        xaxistype = kwargs.pop('xaxistype', None)
        bgcobj = kwargs.pop('bgcolor', None)        
        
        if aspect == 'equal':
            ax.axes.setAutoAspect(False)
        else:
            if isinstance(aspect, (int, float)):
                ax.axes.setAspect(aspect)
                ax.axes.setAutoAspect(False)
        if bottomaxis == False:
            b_axis.setVisible(False)
        if leftaxis == False:
            l_axis.setVisible(False)
        if topaxis == False:
            t_axis.setVisible(False)
        if rightaxis == False:
            r_axis.setVisible(False)
        if xreverse:
            b_axis.setInverse(True)
            t_axis.setInverse(True)
        if yreverse:
            l_axis.setInverse(True)
            r_axis.setInverse(True)        
        if not xaxistype is None:
            ax.set_xaxis_type(xaxistype)
        bgcolor = plotutil.getcolor(bgcobj)
        ax.axes.setBackground(bgcolor)
        tickline = kwargs.pop('tickline', True)
        b_axis.setDrawTickLine(tickline)
        t_axis.setDrawTickLine(tickline)
        l_axis.setDrawTickLine(tickline)
        r_axis.setDrawTickLine(tickline)
        tickfontname = kwargs.pop('tickfontname', 'Arial')
        tickfontsize = kwargs.pop('tickfontsize', 14)
        tickbold = kwargs.pop('tickbold', False)
        if tickbold:
            font = Font(tickfontname, Font.BOLD, tickfontsize)
        else:
            font = Font(tickfontname, Font.PLAIN, tickfontsize)
        ax.axes.setAxisLabelFont(font)
        
    def __create_axesm(self, *args, **kwargs):  
        """
        Create an map axes.
        
        :param projinfo: (*ProjectionInfo*) Optional, map projection, default is longlat projection.
        :param position: (*list*) Optional, axes position specified by *position=* [left, bottom, width
            height] in normalized (0, 1) units. Default is [0.13, 0.11, 0.775, 0.815].
        
        :returns: The map axes.
        """       
        ax = MapAxes(**kwargs)
        if len(args) > 0:
            position = args[0]
        else:
            position = kwargs.pop('position', None)        
        if position is None:
           position = [0.13, 0.11, 0.775, 0.815]
        ax.set_position(position)    
        return ax
        
    def __set_axesm(self, ax, **kwargs):  
        """
        Create an map axes.
        
        :param bgcolor: (*Color*) Optional, axes background color.
        :param axis: (*boolean*) Optional, set all axis visible or not. Default is ``True`` .
        :param bottomaxis: (*boolean*) Optional, set bottom axis visible or not. Default is ``True`` .
        :param leftaxis: (*boolean*) Optional, set left axis visible or not. Default is ``True`` .
        :param topaxis: (*boolean*) Optional, set top axis visible or not. Default is ``True`` .
        :param rightaxis: (*boolean*) Optional, set right axis visible or not. Default is ``True`` .
        :param xyscale: (*int*) Optional, set scale of x and y axis, default is 1. It is only
            valid in longlat projection.
        :param gridlabel: (*boolean*) Optional, set axis tick labels visible or not. Default is ``True`` .
        :param gridlabelloc: (*string*) Optional, Set grid label locations 
			[left_bottom | left_up | right_bottom | right_up | all]. Default is ``left_bottom'.
		:param gridline: (*boolean*) Optional, set grid line visible or not. Default is ``False`` .
        :param griddx: (*float*) Optional, set x grid line interval. Default is 10 degree.
        :param griddy: (*float*) Optional, set y grid line interval. Default is 10 degree.
        :param frameon: (*boolean*) Optional, set frame visible or not. Default is ``False`` for lon/lat
            projection, ortherwise is ``True``.
        :param tickfontname: (*string*) Optional, set axis tick labels font name. Default is ``Arial`` .
        :param tickfontsize: (*int*) Optional, set axis tick labels font size. Default is 14.
        :param tickbold: (*boolean*) Optional, set axis tick labels font bold or not. Default is ``False`` .
        :param boundaryprop: (*dict*) boundary property.
        
        :returns: The map axes.
        """       
        aspect = kwargs.pop('aspect', 'equal')
        if aspect == 'equal':
            ax.axes.setAutoAspect(False)
        elif aspect == 'auto':
            ax.axes.setAutoAspect(True)
        else:
            if isinstance(aspect, (int, float)):
                ax.axes.setAspect(aspect)
                ax.axes.setAutoAspect(False)
        axis = kwargs.pop('axis', True)
        if axis:
            bottomaxis = kwargs.pop('bottomaxis', True)
            leftaxis = kwargs.pop('leftaxis', True)
            topaxis = kwargs.pop('topaxis', True)
            rightaxis = kwargs.pop('rightaxis', True)
        else:
            bottomaxis = False
            leftaxis = False
            topaxis = False
            rightaxis = False            
        gridlabel = kwargs.pop('gridlabel', True)
        gridlabelloc = kwargs.pop('gridlabelloc', 'left_bottom')
        gridline = kwargs.pop('gridline', False)
        griddx = kwargs.pop('griddx', 10)
        griddy = kwargs.pop('griddy', 10)
        if ax.axes.getProjInfo().isLonLat():
            frameon = kwargs.pop('frameon', False)
        else:
            frameon = kwargs.pop('frameon', True)
        axison = kwargs.pop('axison', None)
        bgcobj = kwargs.pop('bgcolor', None)
        xyscale = kwargs.pop('xyscale', 1)     
        tickfontname = kwargs.pop('tickfontname', 'Arial')
        tickfontsize = kwargs.pop('tickfontsize', 14)
        tickbold = kwargs.pop('tickbold', False)
        if tickbold:
            font = Font(tickfontname, Font.BOLD, tickfontsize)
        else:
            font = Font(tickfontname, Font.PLAIN, tickfontsize)
            
        mapview = ax.axes.getMapView()
        mapview.setXYScaleFactor(xyscale)
        ax.axes.setAspect(xyscale)
        ax.axes.setAxisLabelFont(font)
        if not axison is None:
            ax.axes.setAxisOn(axison)
        else:
            if bottomaxis == False:
                ax.axes.getAxis(Location.BOTTOM).setVisible(False)
            if leftaxis == False:
                ax.axes.getAxis(Location.LEFT).setVisible(False)
            if topaxis == False:
                ax.axes.getAxis(Location.TOP).setVisible(False)
            if rightaxis == False:
                ax.axes.getAxis(Location.RIGHT).setVisible(False)
        mapframe = ax.axes.getMapFrame()
        mapframe.setGridFont(font)
        mapframe.setDrawGridLabel(gridlabel)
        mapframe.setDrawGridTickLine(gridlabel)
        mapframe.setGridLabelPosition(gridlabelloc)
        mapframe.setDrawGridLine(gridline)
        mapframe.setGridXDelt(griddx)
        mapframe.setGridYDelt(griddy)
        ax.axes.setDrawNeatLine(frameon)
        bgcolor = plotutil.getcolor(bgcobj)
        ax.axes.setBackground(bgcolor)
        boundaryprop = kwargs.pop('boundaryprop', None)
        if not boundaryprop is None:
            boundaryprop = plotutil.getlegendbreak('polygon', **boundaryprop)[0]
            ax.axes.setBoundaryProp(boundaryprop)
     
        return ax

    def __create_axes3d(self, *args, **kwargs):
        """
        Create an axes.
        
        :param position: (*list*) Optional, axes position specified by *position=* [left, bottom, width
            height] in normalized (0, 1) units. Default is [0.13, 0.11, 0.775, 0.815].
        :param outerposition: (*list*) Optional, axes size and location, including labels and margin.
        
        :returns: The axes.
        """        
        if len(args) > 0:
            position = args[0]
        else:
            position = kwargs.pop('position', None)    
        outerposition = kwargs.pop('outerposition', None)
        ax = Axes3D(**kwargs)
        if position is None:
            position = [0.13, 0.11, 0.71, 0.815]
            ax.active_outerposition(True)
        else:        
            ax.active_outerposition(False)        
        ax.set_position(position)   
        if not outerposition is None:
            ax.set_outerposition(outerposition)
            ax.active_outerposition(True)
        
        return ax
        
    def __set_axes3d(self, ax, **kwargs):
        """
        Set an axes.

        :param aspect: (*string*) ['equal' | 'auto'] or a number. If a number the ratio of x-unit/y-unit in screen-space.
            Default is 'auto'.
        :param bgcolor: (*Color*) Optional, axes background color.
        :param axis: (*boolean*) Optional, set all axis visible or not. Default is ``True`` .
        :param bottomaxis: (*boolean*) Optional, set bottom axis visible or not. Default is ``True`` .
        :param leftaxis: (*boolean*) Optional, set left axis visible or not. Default is ``True`` .
        :param topaxis: (*boolean*) Optional, set top axis visible or not. Default is ``True`` .
        :param rightaxis: (*boolean*) Optional, set right axis visible or not. Default is ``True`` .
        :param xaxistype: (*string*) Optional, set x axis type as 'normal', 'lon', 'lat' or 'time'.
        :param xreverse: (*boolean*) Optional, set x axis reverse or not. Default is ``False`` .
        :param yreverse: (*boolean*) Optional, set yaxis reverse or not. Default is ``False`` .
        
        :returns: The axes.
        """     
        tickfontname = kwargs.pop('tickfontname', 'Arial')
        tickfontsize = kwargs.pop('tickfontsize', 14)
        tickbold = kwargs.pop('tickbold', False)
        if tickbold:
            font = Font(tickfontname, Font.BOLD, tickfontsize)
        else:
            font = Font(tickfontname, Font.PLAIN, tickfontsize)
        ax.axes.setAxisTickFont(font)
        return ax
        
    def new_axes(self, *args, **kwargs):
        '''
        Add an axes to the figure.
    
        :param position: (*list*) Optional, axes position specified by *position=* [left, bottom, width
            height] in normalized (0, 1) units. Default is [0.13, 0.11, 0.775, 0.815].
        :param outerposition: (*list*) Optional, axes size and location, including labels and margin.
        :param aspect: (*string*) ['equal' | 'auto'] or a number. If a number the ratio of x-unit/y-unit in screen-space.
            Default is 'auto'.
        :param bgcolor: (*Color*) Optional, axes background color.
        :param axis: (*boolean*) Optional, set all axis visible or not. Default is ``True`` .
        :param bottomaxis: (*boolean*) Optional, set bottom axis visible or not. Default is ``True`` .
        :param leftaxis: (*boolean*) Optional, set left axis visible or not. Default is ``True`` .
        :param topaxis: (*boolean*) Optional, set top axis visible or not. Default is ``True`` .
        :param rightaxis: (*boolean*) Optional, set right axis visible or not. Default is ``True`` .
        :param xaxistype: (*string*) Optional, set x axis type as 'normal', 'lon', 'lat' or 'time'.
        :param xreverse: (*boolean*) Optional, set x axis reverse or not. Default is ``False`` .
        :param yreverse: (*boolean*) Optional, set yaxis reverse or not. Default is ``False`` .
        
        :returns: The axes.
        '''
        axestype = kwargs.pop('axestype', 'cartesian')
        polar = kwargs.pop('polar', False)
        if polar:
            axestype = 'polar'
        if axestype == 'polar':
            ax = PolarAxes(figure=self)
            self.__set_axes(ax, **kwargs)
        elif axestype == 'map':
            ax = MapAxes(figure=self, **kwargs)
            self.__set_axesm(ax, **kwargs)
        elif axestype == '3d':
            ax = Axes3D(figure = self, **kwargs)
            self.__set_axes3d(ax, **kwargs)
        else:
            ax = Axes(figure=self)
            self.__set_axes(ax, **kwargs)
        self.__set_axes_common(ax, *args, **kwargs)   

        return ax
     
    def add_axes(self, *args, **kwargs):
        '''
        Add an axes to the figure.
    
        :param position: (*list*) Optional, axes position specified by *position=* [left, bottom, width
            height] in normalized (0, 1) units. Default is [0.13, 0.11, 0.775, 0.815].
        :param outerposition: (*list*) Optional, axes size and location, including labels and margin.
        :param aspect: (*string*) ['equal' | 'auto'] or a number. If a number the ratio of x-unit/y-unit in screen-space.
            Default is 'auto'.
        :param bgcolor: (*Color*) Optional, axes background color.
        :param axis: (*boolean*) Optional, set all axis visible or not. Default is ``True`` .
        :param bottomaxis: (*boolean*) Optional, set bottom axis visible or not. Default is ``True`` .
        :param leftaxis: (*boolean*) Optional, set left axis visible or not. Default is ``True`` .
        :param topaxis: (*boolean*) Optional, set top axis visible or not. Default is ``True`` .
        :param rightaxis: (*boolean*) Optional, set right axis visible or not. Default is ``True`` .
        :param xaxistype: (*string*) Optional, set x axis type as 'normal', 'lon', 'lat' or 'time'.
        :param xreverse: (*boolean*) Optional, set x axis reverse or not. Default is ``False`` .
        :param yreverse: (*boolean*) Optional, set yaxis reverse or not. Default is ``False`` .
        
        :returns: The axes.
        '''
        ax = self.new_axes(*args, **kwargs)
        newaxes = kwargs.pop('newaxes', True)
        chart = self.getChart()
        if newaxes:
            self._add_axes(ax)
        else:
            plot = chart.getCurrentPlot()
            if plot.isSubPlot:
                ax.axes.isSubPlot = True
                position = kwargs.pop('position', None)
                if position is None:
                    ax.set_position(plot.getPosition())  
            chart.setCurrentPlot(ax.axes)

        return ax
        
    def _add_axes(self, ax):
        '''
        Add a axes.
        
        :param ax: (*Axes*) The axes.
        '''
        self.axes.append(ax)
        self.getChart().addPlot(ax.axes)
        
    def remove_axes(self, ax=None):
        '''
        Remove the axes.
        
        :param ax: (*Axes*) The axes.
        '''
        if ax is None:
            self.axes = []
            self.getChart().getPlots().clear()
        elif isinstance(ax, int):
            self.axes.pop(ax)
            self.getChart().getPlots().remove(ax)
        else:
            self.axes.remove(ax)
            self.getChart().removePlot(ax.axes)
        
    def draw(self):
        '''
        Re-paint the figure.
        '''
        self.paintGraphics()
        
    def set_mousemode(self, mm):
        '''
        Set MouseMode.
        
        :param mm: (*string*) MouseMode string [zoom_in | zoom_out | pan | identifer
            | rotate | select].
        '''
        mm = MouseMode.valueOf(mm.upper())
        self.setMouseMode(mm)

    def subplot(self, nrows, ncols, plot_number, **kwargs):
        """
        Returen a subplot axes positioned by the given grid definition.

        :param nrows, nrows: (*int*) Whree *nrows* and *ncols* are used to notionally spli the 
            figure into ``nrows * ncols`` sub-axes.
        :param plot_number: (*int) Is used to identify the particular subplot that this function
            is to create within the notional gird. It starts at 1, increments across rows first
            and has a maximum of ``nrows * ncols`` .

        :returns: Current axes specified by ``plot_number`` .
        """
        chart = self.getChart()
        chart.setRowNum(nrows)
        chart.setColumnNum(ncols)
        polar = kwargs.pop('polar', False)
        isnew = True
        if isnew:
            polar = kwargs.pop('polar', False)
            if polar:
                ax = PolarAxes()
            else:
                ax = Axes()
            ax.axes.isSubPlot = True        
        else:
            chart.setCurrentPlot(plot_number - 1)  
        position = kwargs.pop('position', None)
        if position is None:
            if isnew:
                if isinstance(plot_number, (list, tuple)):
                    i = 0
                    for pnum in plot_number:
                        pnum -= 1
                        rowidx = pnum / ncols
                        colidx = pnum % ncols
                        width = 1. / ncols
                        height = 1. / nrows                    
                        x = width * colidx
                        y = 1. - height * (rowidx + 1)
                        if i == 0:
                            minx = x
                            miny = y
                            maxx = x + width
                            maxy = y + height
                        else:
                            minx = min(x, minx)
                            miny = min(y, miny)
                            maxx = max(x + width, maxx)
                            maxy = max(y + height, maxy)
                        i += 1
                    x = minx
                    y = miny
                    width = maxx - minx
                    height = maxy - miny
                else:
                    plot_number -= 1
                    rowidx = plot_number / ncols
                    colidx = plot_number % ncols
                    width = 1. / ncols
                    height = 1. / nrows
                    x = width * colidx
                    y = 1. - height * (rowidx + 1)
                ax.set_position([x, y, width, height])
                ax.set_outerposition([x, y, width, height])
                ax.active_outerposition(True)
        else:
            ax.set_position(position)
            ax.active_outerposition(False)
        outerposition = kwargs.pop('outerposition', None)
        if not outerposition is None:
            ax.set_outerposition(outerposition)
            ax.active_outerposition(True)

        if isinstance(ax, MapAxes):
            self.__set_axesm(ax, **kwargs)
        else:
            self.__set_axes(ax, **kwargs)

        if isnew:
            chart.addPlot(ax.axes)
            chart.setCurrentPlot(chart.getPlots().size() - 1)

        return ax

    def subplots(self, nrows=1, ncols=1, position=None, sharex=False, sharey=False, \
        wspace=None, hspace=None, axestype='Axes', **kwargs):
        '''
        Create a figure and a set of subplots.

        :param nrows: (*int*) Number of rows.
        :param ncols: (*int*) Number of cols.
        :param position: (*list*) All axes' position specified by *position=* [left, bottom, width
            height] in normalized (0, 1) units. Default is [0,0,1,1].
        :param sharex: (*boolean*) If share x axis.
        :param sharey: (*boolean*) If share y axis.
        :param subplot_kw: (*dict*) Subplot key words.
        :param wspace: (*float*) The amount of width reserved for blank space between subplots,
            expressed as a fraction of the average axis width.
        :param hspace: (*float*) The amount of height reserved for blank space between subplots,
            expressed as a fraction of the average axis height.
        :param axestype: (*string*) Axes type [Axes | Axes3D | MapAxes | PolarAxes].

        :returns: The figure and the axes tuple.
        '''
        if position is None:
            if wspace is None and hspace is None:
                position = [0, 0, 1, 1]
            else:
                position = [0.13, 0.11, 0.775, 0.815]
        left = float(position[0])
        bottom = float(position[1])
        width = float(position[2])
        height = float(position[3])

        chart = self.getChart()
        chart.setRowNum(nrows)
        chart.setColumnNum(ncols)
        axs = []
        ax2d = nrows > 1 and ncols > 1
        w = width / ncols
        h = height / nrows
        iswspace = False
        ishspace = False
        if not wspace is None and ncols > 1:
            w = (width - wspace * (ncols - 1)) / ncols
            iswspace = True
        if not hspace is None and nrows > 1:
            h = (height - hspace * (nrows - 1)) / nrows
            ishspace = True
        axestype = axestype.lower()
        y = bottom + height - h
        for i in range(nrows):
            if ax2d:
                axs2d = []
            x = left
            if ishspace:
                if i > 0:
                    y -= hspace
            for j in range(ncols):   
                if axestype == 'axes3d':
                    ax = Axes3D()
                    self.__set_axes3d(ax, **kwarg)
                elif axestype == 'mapaxes':
                    ax = MapAxes()
                    self.__set_axesm(ax, **kwargs)
                elif axestype == 'polaraxes':
                    ax = PolarAxes()
                else:
                    ax = Axes()
                    self.__set_axes(ax, **kwargs)
                ax.axes.isSubPlot = True             
                if not iswspace and not ishspace:
                    x = left + w * j
                    y = (bottom + height) - h * (i + 1)
                    ax.set_position([x, y, w, h])
                    ax.set_outerposition([x, y, w, h])
                    ax.active_outerposition(True)
                else:
                    if iswspace:
                        if j > 0:
                            x += wspace                
                    ax.set_position([x, y, w, h])
                    ax.active_outerposition(False)
                    x += w
                if sharex:
                    if i < nrows - 1:
                        ax.axes.getAxis(Location.BOTTOM).setDrawTickLabel(False)
                if sharey:
                    if j > 0:
                        ax.axes.getAxis(Location.LEFT).setDrawTickLabel(False)
                chart.addPlot(ax.axes)
                if ax2d:
                    axs2d.append(ax)
                else:
                    axs.append(ax)
            if ax2d:
                axs.append(tuple(axs2d))
            y -= h
        
        chart.setCurrentPlot(0)
        return tuple(axs)
        
    def get_title(self):
        '''
        Get title               
        '''
        return self.getChart().getTitle()  
        
    def set_title(self, label, fontname=None, fontsize=14, bold=True, color='black'):
        """
        Add a centered title to the figure.
        
        :param label: (*string*) Title label string.
        :param fontname: (*string*) Font name. Default is ``Arial`` .
        :param fontsize: (*int*) Font size. Default is ``14`` .
        :param bold: (*boolean*) Is bold font or not. Default is ``True`` .
        :param color: (*color*) Title string color. Default is ``black`` .
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
        ctitle = ChartText(label, font)
        ctitle.setUseExternalFont(exfont)
        ctitle.setColor(c)
        self.getChart().setTitle(ctitle)
        return ctitle
        
########################################################3
class Test():
    def test():
        print 'Test...'