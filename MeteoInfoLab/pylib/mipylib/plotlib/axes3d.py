# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2018-4-10
# Purpose: MeteoInfoLab axes3d module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.chart.plot import Plot3D, GraphicFactory
from org.meteoinfo.chart import ChartText3D
from org.meteoinfo.legend import LegendManage, BreakTypes, PolylineBreak
from org.meteoinfo.shape import ShapeTypes, Graphic
from org.meteoinfo.layer import LayerTypes

from axes import Axes
from mipylib.numeric.dimarray import DimArray
from mipylib.numeric.miarray import MIArray
import mipylib.numeric.minum as minum
import plotutil
import mipylib.miutil as miutil

import datetime

from java.awt import Font, Color

#########################################################
class Axes3D(Axes):
    '''
    Axes with 3 dimensional.
    '''
    
    def __init__(self, axes=None, figure=None, **kwargs):
        self.figure = figure
        if axes is None:        
            self.axes = Plot3D()
        else:
            self.axes = axes
        self.axestype = '3d'
        self.projector = self.axes.getProjector()
        #distance = kwargs.pop('distance', 10000)
        #self.projector.setDistance(distance)
        rotation_angle = kwargs.pop('rotation', 225)
        self.projector.setRotationAngle(rotation_angle)
        elevation_angle = kwargs.pop('elevation', 30)
        self.projector.setElevationAngle(elevation_angle)
        xyaxis = kwargs.pop('xyaxis', True)
        self.axes.setDisplayXY(xyaxis)
        zaxis = kwargs.pop('zaxis', True)
        self.axes.setDisplayZ(zaxis)
        grid = kwargs.pop('grid', True)
        self.axes.setDisplayGrids(grid)
        boxed = kwargs.pop('boxed', True)
        self.axes.setBoxed(boxed)
        bbox = kwargs.pop('bbox', False)
        self.axes.setDrawBoundingBox(bbox)
        
    def get_distance(self):
        '''
        Get distance to object.
        
        :returns: Distance to object.
        '''
        return self.projector.getDistance()
        
    def set_distance(self, dis):
        '''
        Set distance to object.
        
        :param dis: (*float*) Distance to object.
        '''
        self.projector.setDistance(dis)
        
    def get_rotation(self):
        '''
        Get rotation angle.
        
        :returns: Rotation angle.
        '''
        return self.projector.getRotationAngle()
        
    def set_rotation(self, rotation):
        '''
        Set rotation angle.
        
        :param rotation: (*float*) Rotation angle.
        '''
        self.projector.setRotationAngle(rotation)
        
    def get_elevation(self):
        '''
        Get elevation angle.
        
        :returns: Elevation angle.
        '''
        return self.projector.getElevationAngle()
        
    def set_elevation(self, elevation):
        '''
        Set elevation angle.
        
        :param elevation: (*float*) Elevation angle.
        '''
        self.projector.setElevationAngle(elevation)
        
    def set_draw_xy(self, dxy):
        '''
        Set draw xy axis or not.
        
        :param dxy: (*boolean*) Draw xy axis or not.
        '''
        self.axes.setDisplayXY(dxy)
        
    def set_draw_z(self, dz):
        '''
        Set draw z axis or not.
        
        :param dz: (*boolean*) Draw z axis or not.
        '''
        self.axes.setDisplayZ(dz)
        
    def set_draw_box(self, db):
        '''
        Set draw 3D box or not.
        
        :param db: (*boolean*) Draw 3D box or not.
        '''
        self.axes.setBoxed(db)
        
    def set_draw_bbox(self, bbox):
        '''
        Set draw bounding box or not.
        
        :param db: (*boolean*) Draw bounding box or not.
        '''
        self.axes.setDrawBoundingBox(bbox)
        
    def get_xlim(self):
        """
        Get the *x* limits of the current axes.
        
        :returns: (*tuple*) x limits.
        """
        return self.axes.getXMin(), self.axes.getXMax()
        
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
        self.axes.setXMinMax(xmin, xmax)
        
    def get_ylim(self):
        """
        Get the *y* limits of the current axes.
        
        :returns: (*tuple*) y limits.
        """
        return self.axes.getYMin(), self.axes.getYMax()
            
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
        self.axes.setYMinMax(ymin, ymax) 

    def get_zlim(self):
        """
        Get the *z* limits of the current axes.
        
        :returns: (*tuple*) z limits.
        """
        return self.axes.getZMin(), self.axes.getZMax()
            
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
        self.axes.setZMinMax(zmin, zmax) 
    
    def get_zticks(self):
        '''
        Get z axis tick locations.
        '''
        axis = self.axes.getZAxis()
        axis.updateTickLabels()
        return axis.getTickLocations()
        
    def set_zticks(self, locs):
        '''
        Set z axis tick locations.
        '''
        axis = self.axes.getZAxis()
        if isinstance(locs, (MIArray, DimArray)):
            locs = labels.aslist()
        axis.setTickLocations(locs)
    
    def get_zticklabels(self):
        '''
        Get z axis tick labels.
        '''
        axis = self.axes.getZAxis()
        axis.updateTickLabels()
        return axis.getTickLabelText()
        
    def set_zticklabels(self, labels, **kwargs):
        '''
        Set z axis tick labels.
        '''
        axis = self.axes.getZAxis()

        if not labels is None:
            if isinstance(labels, (MIArray, DimArray)):
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
        tickline = kwargs.pop('tickline', None)
        tickline = kwargs.pop('tickvisible', tickline)
        ticklabel = kwargs.pop('ticklabel', None)
        minortick = kwargs.pop('minortick', False)
        minorticknum = kwargs.pop('minorticknum', 5)
        tickin = kwargs.pop('tickin', True)
        axistype = kwargs.pop('axistype', None)
        tickfontname = kwargs.pop('tickfontname', 'Arial')
        tickfontsize = kwargs.pop('tickfontsize', 14)
        tickbold = kwargs.pop('tickbold', False)
        if tickbold:
            font = Font(tickfontname, Font.BOLD, tickfontsize)
        else:
            font = Font(tickfontname, Font.PLAIN, tickfontsize)
        axislist = []
        axislist.append(self.axes.getZAxis())
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
            if not ticklabel is None:
                axis.setDrawTickLabel(ticklabel)
            axis.setMinorTickVisible(minortick)
            axis.setMinorTickNum(minorticknum)
            axis.setInsideTick(tickin)
            axis.setTickLabelFont(font)
    
    def plot(self, x, y, z, *args, **kwargs):
        """
        Plot 3D lines and/or markers to the axes. *args* is a variable length argument, allowing
        for multiple *x, y* pairs with an optional format string.
        
        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param z: (*array_like*) Input z data.
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
        if mvalues is None:
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
                if isinstance(mvalues, (list, tuple)):
                    mvalues = minum.array(mvalues)
                levels = kwargs.pop('levs', None)
                if levels is None:
                    levels = kwargs.pop('levels', None)
                if levels is None:
                    cnum = kwargs.pop('cnum', None)
                    if cnum is None:
                        ls = plotutil.getlegendscheme([], mvalues.min(), mvalues.max(), **kwargs)
                    else:
                        ls = plotutil.getlegendscheme([cnum], mvalues.min(), mvalues.max(), **kwargs)
                else:
                    ls = plotutil.getlegendscheme([levels], mvalues.min(), mvalues.max(), **kwargs)
                ls = plotutil.setlegendscheme_line(ls, **kwargs)

        #Add graphics
        if mvalues is None:
            if colors is None:
                graphics = GraphicFactory.createLineString3D(xdata, ydata, zdata, line)
            else:
                graphics = GraphicFactory.createLineString3D(xdata, ydata, zdata, cbs)
        else:
            mdata = plotutil.getplotdata(mvalues)
            graphics = GraphicFactory.createLineString3D(xdata, ydata, zdata, mdata, ls)
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics
        
    def scatter(self, x, y, z, s=8, c='b', marker='o', alpha=None, linewidth=None, 
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
        
        :returns: Points legend break.
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
        if len(c) > 1:
            if isinstance(c, (MIArray, DimArray)):
                isvalue = True
            elif isinstance(c[0], (int, long, float)):
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
            graphics = GraphicFactory.createPoints3D(xdata, ydata, zdata, c.asarray(), ls)
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
            graphics = GraphicFactory.createPoints3D(xdata, ydata, zdata, pbs)
        
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
        
        :returns: Points legend break.
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
            if isinstance(c, (MIArray, DimArray)):
                isvalue = True
            elif isinstance(c[0], (int, long, float)):
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
        
    def plot_wireframe(self, *args, **kwargs):
        '''
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
        '''        
        if len(args) == 1:
            x = args[0].dimvalue(1)
            y = args[0].dimvalue(0)
            x, y = minum.meshgrid(x, y)
            z = args[0]    
            args = args[1:]
        else:
            x = args[0]
            y = args[1]
            z = args[2]
            args = args[3:]
 
        line = plotutil.getlegendbreak('line', **kwargs)[0]
        graphics = GraphicFactory.createWireframe(x.asarray(), y.asarray(), z.asarray(), line)
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics
        
    def plot_surface(self, *args, **kwargs):
        '''
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
        '''        
        if len(args) <= 2:
            x = args[0].dimvalue(1)
            y = args[0].dimvalue(0)
            x, y = minum.meshgrid(x, y)
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
                if isinstance(level_arg, MIArray):
                    level_arg = level_arg.aslist()
                ls = LegendManage.createLegendScheme(z.min(), z.max(), level_arg, cmap)
        else:    
            ls = LegendManage.createLegendScheme(z.min(), z.max(), cmap)
        ls = ls.convertTo(ShapeTypes.Polygon)
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
        :param smooth: (*boolean*) Smooth countour lines or not.
        
        :returns: (*VectoryLayer*) Contour VectoryLayer created from array data.
        """
        n = len(args)
        cmap = plotutil.getcolormap(**kwargs)
        fill_value = kwargs.pop('fill_value', -9999.0)
        offset = kwargs.pop('offset', 0)
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
        zdir = kwargs.pop('zdir', 'z')
        if zdir == 'xy':
            sepoint = kwargs.pop('sepoint', [0,0,1,1])
            igraphic = GraphicFactory.createContourLines(gdata.data, offset, zdir, ls, smooth, \
                sepoint)
        else:
            igraphic = GraphicFactory.createContourLines(gdata.data, offset, zdir, ls, smooth)
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
        :param smooth: (*boolean*) Smooth countour lines or not.
        
        :returns: (*VectoryLayer*) Contour VectoryLayer created from array data.
        """
        n = len(args)
        cmap = plotutil.getcolormap(**kwargs)
        fill_value = kwargs.pop('fill_value', -9999.0)
        offset = kwargs.pop('offset', 0)
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
        edge = kwargs.pop('edge', None)
        if edge is None:
            kwargs['edge'] = False
        else:
            kwargs['edge'] = edge
        plotutil.setlegendscheme(ls, **kwargs)
        
        smooth = kwargs.pop('smooth', True)
        zdir = kwargs.pop('zdir', 'z')
        if zdir == 'xy':
            sepoint = kwargs.pop('sepoint', [0,0,1,1])
            igraphic = GraphicFactory.createContourPolygons(gdata.data, offset, zdir, ls, smooth, \
                sepoint)
        else:
            igraphic = GraphicFactory.createContourPolygons(gdata.data, offset, zdir, ls, smooth)
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
        :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level curves 
            to draw, in increasing order.
        :param cmap: (*string*) Color map string.
        :param colors: (*list*) If None (default), the colormap specified by cmap will be used. If a 
            string, like ‘r’ or ‘red’, all levels will be plotted in this color. If a tuple of matplotlib 
            color args (string, float, rgb, etc), different levels will be plotted in different colors in 
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
                if isinstance(rgbdata[0], MIArray):
                    x = minum.arange(0, rgbdata[0].shape[1])
                    y = minum.arange(0, rgbdata[0].shape[0])
                else:
                    x = rgbdata[0].dimvalue(1)
                    y = rgbdata[0].dimvalue(0)
            elif args[0].ndim > 2:
                isrgb = True
                rgbdata = args[0]
                if isinstance(rgbdata, MIArray):
                    x = minum.arange(0, rgbdata.shape[1])
                    y = minum.arange(0, rgbdata.shape[0])
                else:
                    x = rgbdata.dimvalue(1)
                    y = rgbdata.dimvalue(0)
            else:
                gdata = minum.asgridarray(args[0])
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
            if isinstance(a, (list, tuple)):
                isrgb = True
                rgbdata = a
            elif a.ndim > 2:
                isrgb = True
                rgbdata = a
            else:
                gdata = minum.asgridarray(a, x, y, fill_value)
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
                    ls = LegendManage.createImageLegend(gdata, cn, cmap)
                else:
                    if isinstance(level_arg, MIArray):
                        level_arg = level_arg.aslist()
                    ls = LegendManage.createImageLegend(gdata, level_arg, cmap)
            else:
                ls = plotutil.getlegendscheme(args, gdata.min(), gdata.max(), **kwargs)
            ls = ls.convertTo(ShapeTypes.Image)
            plotutil.setlegendscheme(ls, **kwargs)
            if zdir == 'xy':
                sepoint = kwargs.pop('sepoint', [0,0,1,1])
            else:
                sepoint = None
            graphics = GraphicFactory.createImage(gdata, ls, offset, zdir, sepoint, interpolation)
                
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics
        
    def quiver(self, *args, **kwargs):
        """
        Plot a 2-D field of arrows.
        
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
        :param length: (*float*) The length of each quiver, default to 1.0, the unit is 
            the same with the axes.
        
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
                        if isinstance(levs, MIArray):
                            levs = levs.tolist()
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
        length = kwargs.pop('length', 1)
        igraphic = GraphicFactory.createArrows3D(x, y, z, u, v, w, length, cdata, ls)

        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(igraphic)
        return igraphic
        
    def plot_layer(self, layer, **kwargs):
        '''
        Plot a layer in 3D axes.
        
        :param layer: (*MILayer*) The layer to be plotted.
        
        :returns: Graphics.
        '''
        ls = kwargs.pop('symbolspec', None)
        offset = kwargs.pop('offset', 0)
        xshift = kwargs.pop('xshift', 0)
        layer = layer.layer
        if layer.getLayerType() == LayerTypes.VectorLayer:            
            if ls is None:
                ls = layer.getLegendScheme()
                if len(kwargs) > 0 and layer.getLegendScheme().getBreakNum() == 1:
                    lb = layer.getLegendScheme().getLegendBreaks().get(0)
                    btype = lb.getBreakType()
                    geometry = 'point'
                    if btype == BreakTypes.PolylineBreak:
                        geometry = 'line'
                    elif btype == BreakTypes.PolygonBreak:
                        geometry = 'polygon'
                    lb, isunique = plotutil.getlegendbreak(geometry, **kwargs)
                    ls.getLegendBreaks().set(0, lb)

            plotutil.setlegendscheme(ls, **kwargs)
            layer.setLegendScheme(ls)                    
            graphics = GraphicFactory.createGraphicsFromLayer(layer, offset, xshift)
        else:
            interpolation = kwargs.pop('interpolation', None)
            graphics = GraphicFactory.createImage(layer, offset, xshift, interpolation)
        
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics
        
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
        '''
        Add text to the plot. kwargs will be passed on to text, except for the zdir 
        keyword, which sets the direction to be used as the z direction.
        
        :param x: (*float*) X coordinate.
        :param y: (*float*) Y coordinate.
        :param z: (*float*) Z coordinate.
        :param s: (*string*) Text string.
        :param zdir: Z direction.
        '''
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
        text.setText(s)
        text.setFont(font)
        text.setColor(c)
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
        graphic = Graphic(text, None)
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphic)
        return graphic
        
    def data2pixel(self, x, y, z=None):
        '''
        Transform data coordinate to screen coordinate
        
        :param x: (*float*) X coordinate.
        :param y: (*float*) Y coordinate.
        :param z: (*float*) Z coordinate - only used for 3-D axes.
        '''
        r = self.axes.project(x, y, z) 
        x = r.x
        y = r.y
        rect = self.axes.getPositionArea()
        r = self.axes.projToScreen(x, y, rect)
        sx = r[0] + rect.getX()
        sy = r[1] + rect.getY()
        sy = self.figure.get_size()[1] - sy
        return sx, sy

##################################33
def test():
    print 'Test!'