# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2018-4-5
# Purpose: MeteoInfoLab mapaxes module
# Note: Jython
#-----------------------------------------------------

import os

from org.meteoinfo.chart.plot import MapPlot, GraphicFactory
from org.meteoinfo.data import ArrayUtil
from org.meteoinfo.data.meteodata import DrawMeteoData
from org.meteoinfo.map import MapView
from org.meteoinfo.legend import BreakTypes, LegendManage, LegendScheme, LegendType
from org.meteoinfo.shape import Shape, PolylineShape, PolygonShape, ShapeTypes, Graphic
from org.meteoinfo.projection.info import ProjectionInfo
from org.meteoinfo.global import Extent
from org.meteoinfo.layer import LayerTypes, WebMapLayer
from org.meteoinfo.data.mapdata.webmap import WebMapProvider

from java.awt import Font, Color

from axes import Axes
from mipylib.numeric.dimarray import DimArray
from mipylib.numeric.miarray import MIArray
from mipylib.geolib.milayer import MILayer
import mipylib.geolib.migeo as migeo
import plotutil
import mipylib.numeric.minum as minum
import mipylib.migl as migl
import mipylib.miutil as miutil

##############################################        
class MapAxes(Axes):
    '''
    Axes with geological map coordinate.
    '''
    
    def __init__(self, axes=None, figure=None, **kwargs):
        self.figure = figure
        if axes is None:      
            projinfo = kwargs.pop('projinfo', None)
            if projinfo == None:
                proj = kwargs.pop('proj', 'longlat')
                origin = kwargs.pop('origin', (0, 0, 0))    
                lat_0 = origin[0]
                lon_0 = origin[1]
                lat_0 = kwargs.pop('lat_0', lat_0)
                lon_0 = kwargs.pop('lon_0', lon_0)
                lat_ts = kwargs.pop('truescalelat', 0)
                lat_ts = kwargs.pop('lat_ts', lat_ts)
                k = kwargs.pop('scalefactor', 1)
                k = kwargs.pop('k', k)
                paralles = kwargs.pop('paralles', (30, 60))
                lat_1 = paralles[0]
                if len(paralles) == 2:
                    lat_2 = paralles[1]
                else:
                    lat_2 = lat_1
                lat_1 = kwargs.pop('lat_1', lat_1)
                lat_2 = kwargs.pop('lat_2', lat_2)
                x_0 = kwargs.pop('falseeasting', 0)
                y_0 = kwargs.pop('falsenorthing', 0)
                x_0 = kwargs.pop('x_0', x_0)
                y_0 = kwargs.pop('y_0', y_0)
                h = kwargs.pop('h', 0)
                projstr = '+proj=' + proj \
                    + ' +lat_0=' + str(lat_0) \
                    + ' +lon_0=' + str(lon_0) \
                    + ' +lat_1=' + str(lat_1) \
                    + ' +lat_2=' + str(lat_2) \
                    + ' +lat_ts=' + str(lat_ts) \
                    + ' +k=' + str(k) \
                    + ' +x_0=' + str(x_0) \
                    + ' +y_0=' + str(y_0) \
                    + ' +h=' + str(h)
                projinfo = ProjectionInfo.factory(projstr)
            cutoff = kwargs.pop('cutoff', None)
            if not cutoff is None:
                projinfo.setCutoff(cutoff)
                
            mapview = MapView(projinfo)     
            self.axes = MapPlot(mapview)
        else:
            self.axes = axes
        self.axestype = 'map'
        self.proj = self.axes.getProjInfo()
        
    def islonlat(self):
        '''
        Get if the map axes is lonlat projection or not.
        
        :returns: (*boolean*) Is lonlat projection or not.
        '''
        return self.proj.isLonLat()
            
    def add_layer(self, layer, zorder=None, select=None):
        '''
        Add a map layer
        
        :param layer: (*MapLayer*) The map layer.
        :param zorder: (*int*) Layer z order.
        :param select: (*boolean*) Select layer or not.
        '''
        if isinstance(layer, MILayer):
            layer = layer.layer
        if zorder is None:
            self.axes.addLayer(layer)
        else:
            self.axes.addLayer(zorder, layer)
        if not select is None:
            if select:
                self.axes.setSelectedLayer(layer)
                
    def get_layers(self):
        '''
        Get all layers.
        
        :returns: All layers
        '''
        r = self.axes.getLayers()
        layers = []
        for layer in r:
            layers.append(MILayer(layer))
        return layers
    
    def get_layer(self, by):
        '''
        Get a layer by name or index.
        
        :param by: (*string or int*) Layer name or index.
        
        :returns: The layer.
        '''
        r = self.axes.getLayer(by)
        if not r is None:
            r = MILayer(r)
        return r
            
    def set_active_layer(self, layer):
        '''
        Set active layer
        
        :param layer: (*MILayer*) The map layer.
        '''
        self.axes.setSelectedLayer(layer.layer)
        
    def add_graphic(self, graphic, proj=None):
        '''
        Add a graphic
        
        :param graphic: (*Graphic*) The graphic to be added.
        :param proj: (*ProjectionInfo*) Graphic projection.
        
        :returns: Added graphic
        '''
        if proj is None:
            self.axes.addGraphic(graphic)
        else:
            graphic = self.axes.addGraphic(graphic, proj)
        return graphic
        
    def add_circle(self, xy, radius=5, **kwargs):
        '''
        Add a circle patch
        '''
        lbreak, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        circle = self.axes.addCircle(xy[0], xy[1], radius, lbreak)
        return circle
        
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
        if self.islonlat():
            super(MapAxes, self).grid(b, which, axis, **kwargs)
        else:
            mapframe = self.axes.getMapFrame()
            gridline = mapframe.isDrawGridLine()
            if b is None:
                gridline = not gridline
            else:
                gridline = b
            griddx = kwargs.pop('griddx', None)
            griddy = kwargs.pop('griddy', None)            
            if not gridline is None:
                mapframe.setDrawGridLine(gridline)
            if not griddx is None:
                mapframe.setGridXDelt(griddx)
            if not griddy is None:
                mapframe.setGridYDelt(griddy)
            color = kwargs.pop('color', None)
            if not color is None:
                c = plotutil.getcolor(color)
                mapframe.setGridLineColor(c)
            linewidth = kwargs.pop('linewidth', None)
            if not linewidth is None:
                mapframe.setGridLineSize(linewidth)
            linestyle = kwargs.pop('linestyle', None)
            if not linestyle is None:
                linestyle = plotutil.getlinestyle(linestyle)
                mapframe.setGridLineStyle(linestyle)
                
    def axis(self, limits=None, lonlat=True):
        """
        Sets the min and max of the x and y map axes, with ``[xmin, xmax, ymin, ymax]`` .
        
        :param limits: (*list*) Min and max of the x and y map axes.
        :param lonlat: (*boolean*) Is longitude/latitude or not.
        """
        if limits is None:
            self.axes.setDrawExtent(self.axes.getFullExtent())
            self.axes.setExtent(self.axes.getDrawExtent().clone())
            return True
        else:
            if len(limits) == 4:
                xmin = limits[0]
                xmax = limits[1]
                ymin = limits[2]
                ymax = limits[3]
                extent = Extent(xmin, xmax, ymin, ymax)
                if lonlat:
                    self.axes.setLonLatExtent(extent)
                    self.axes.setExtent(self.axes.getDrawExtent().clone())
                else:
                    self.axes.setDrawExtent(extent)
                    self.axes.setExtent(extent)
                return True
            else:
                print 'The limits parameter must be a list with 4 elements: xmin, xmax, ymin, ymax!'
                return None
        
    def data2pixel(self, x, y, z=None):
        '''
        Transform data coordinate to screen coordinate
        
        :param x: (*float*) X coordinate.
        :param y: (*float*) Y coordinate.
        :param z: (*float*) Z coordinate - only used for 3-D axes.
        '''
        if not self.axes.isLonLatMap():
            x, y = migeo.project(x, y, toproj=self.proj)  
            
        rect = self.axes.getPositionArea()
        r = self.axes.projToScreen(x, y, rect)
        sx = r[0] + rect.getX()
        sy = r[1] + rect.getY()
        sy = self.figure.get_size()[1] - sy
        return sx, sy
        
    def loadmip(self, mipfn, mfidx=0):
        '''
        Load one map frame from a MeteoInfo project file.
        
        :param mipfn: (*string*) MeteoInfo project file name.
        :param mfidx: (*int*) Map frame index.
        '''
        self.axes.loadMIProjectFile(mipfn, mfidx)
        
    def geoshow(self, *args, **kwargs):
        '''
        Display map layer or longitude latitude data.
        
        Syntax:
        --------    
            geoshow(shapefilename) - Displays the map data from a shape file.
            geoshow(layer) - Displays the map data from a map layer which may created by ``shaperead`` function.
            geoshow(S) - Displays the vector geographic features stored in S as points, multipoints, lines, or 
              polygons.
            geoshow(lat, lon) - Displays the latitude and longitude vectors.
        '''
        islayer = False
        if isinstance(args[0], basestring):
            fn = args[0]
            if not fn.endswith('.shp'):
                fn = fn + '.shp'
            if not os.path.exists(fn):
                fn = os.path.join(migl.mapfolder, fn)
            if os.path.exists(fn):
                encoding = kwargs.pop('encoding', None)
                layer = migeo.shaperead(fn, encoding)
                islayer = True
            else:
                raise IOError('File not exists: ' + fn)
        elif isinstance(args[0], MILayer):
            layer = args[0]
            islayer = True
        
        if islayer:    
            layer = layer.layer   
            visible = kwargs.pop('visible', True)
            layer.setVisible(visible)
            order = kwargs.pop('order', None)
            if layer.getLayerType() == LayerTypes.ImageLayer:
                if order is None:
                    self.add_layer(layer)
                else:
                    self.add_layer(layer, order)
            else:
                #LegendScheme
                ls = kwargs.pop('symbolspec', None)
                if ls is None:
                    if len(kwargs) > 0 and layer.getLegendScheme().getBreakNum() == 1:
                        lb = layer.getLegendScheme().getLegendBreaks().get(0)
                        btype = lb.getBreakType()
                        geometry = 'point'
                        if btype == BreakTypes.PolylineBreak:
                            geometry = 'line'
                        elif btype == BreakTypes.PolygonBreak:
                            geometry = 'polygon'
                        lb, isunique = plotutil.getlegendbreak(geometry, **kwargs)
                        layer.getLegendScheme().getLegendBreaks().set(0, lb)
                else:
                    layer.setLegendScheme(ls)
                if order is None:
                    self.add_layer(layer)
                else:
                    self.add_layer(layer, order)
                #Labels        
                labelfield = kwargs.pop('labelfield', None)
                if not labelfield is None:
                    labelset = layer.getLabelSet()
                    labelset.setFieldName(labelfield)
                    fontname = kwargs.pop('fontname', 'Arial')
                    fontsize = kwargs.pop('fontsize', 14)
                    bold = kwargs.pop('bold', False)
                    if bold:
                        font = Font(fontname, Font.BOLD, fontsize)
                    else:
                        font = Font(fontname, Font.PLAIN, fontsize)
                    labelset.setLabelFont(font)
                    lcolor = kwargs.pop('labelcolor', None)
                    if not lcolor is None:
                        lcolor = miutil.getcolor(lcolor)
                        labelset.setLabelColor(lcolor)
                    xoffset = kwargs.pop('xoffset', 0)
                    labelset.setXOffset(xoffset)
                    yoffset = kwargs.pop('yoffset', 0)
                    labelset.setYOffset(yoffset)
                    avoidcoll = kwargs.pop('avoidcoll', True)
                    decimals = kwargs.pop('decimals', None)
                    if not decimals is None:
                        labelset.setAutoDecimal(False)
                        labelset.setDecimalDigits(decimals)
                    labelset.setAvoidCollision(avoidcoll)
                    layer.addLabels()  
            self.axes.setDrawExtent(layer.getExtent().clone())
            self.axes.setExtent(layer.getExtent().clone())
            return MILayer(layer)
        else:
            if isinstance(args[0], Graphic):
                graphic = args[0]
                displaytype = 'point'
                stype = graphic.getShape().getShapeType()
                if stype == ShapeTypes.Polyline:
                    displaytype = 'line'
                elif stype == ShapeTypes.Polygon:
                    displaytype = 'polygon'
                lbreak, isunique = plotutil.getlegendbreak(displaytype, **kwargs)
                graphic.setLegend(lbreak)
                self.add_graphic(graphic)            
            elif isinstance(args[0], Shape):
                shape = args[0]
                displaytype = 'point'
                if isinstance(shape, PolylineShape):
                    displaytype = 'line'
                elif isinstance(shape, PolygonShape):
                    displaytype = 'polygon'
                lbreak, isunique = plotutil.getlegendbreak(displaytype, **kwargs)
                graphic = Graphic(shape, lbreak)
                self.add_graphic(graphic)            
            elif len(args) == 2:
                lat = args[0]
                lon = args[1]
                displaytype = kwargs.pop('displaytype', 'line')
                if isinstance(lat, (int, float)):
                    displaytype = 'point'
                else:
                    if len(lat) == 1:
                        displaytype = 'point'
                    else:
                        if isinstance(lon, (MIArray, DimArray)):
                            lon = lon.aslist()
                        if isinstance(lat, (MIArray, DimArray)):
                            lat = lat.aslist()

                lbreak, isunique = plotutil.getlegendbreak(displaytype, **kwargs)
                iscurve = kwargs.pop('iscurve', False)
                if displaytype == 'point':
                    graphic = self.axes.addPoint(lat, lon, lbreak)
                elif displaytype == 'polyline' or displaytype == 'line':
                    graphic = self.axes.addPolyline(lat, lon, lbreak, iscurve)
                elif displaytype == 'polygon':
                    graphic = self.axes.addPolygon(lat, lon, lbreak)
            return graphic
            
    def plot(self, *args, **kwargs):
        """
        Plot lines and/or markers to the map.
        
        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param style: (*string*) Line style for plot.
        :param linewidth: (*float*) Line width.
        :param color: (*Color*) Line color.
        
        :returns: (*VectoryLayer*) Line VectoryLayer.
        """
        fill_value = kwargs.pop('fill_value', -9999.0)
        proj = kwargs.pop('proj', None)    
        n = len(args) 
        xdatalist = []
        ydatalist = []    
        styles = []
        if n == 1:
            ydata = plotutil.getplotdata(args[0])
            if isinstance(args[0], DimArray):
                xdata = args[0].dimvalue(0)
            else:
                xdata = []
                for i in range(0, len(args[0])):
                    xdata.append(i)
            xdatalist.append(xdata)
            ydatalist.append(ydata)
        elif n == 2:
            if isinstance(args[1], basestring):
                ydata = plotutil.getplotdata(args[0])
                if isinstance(args[0], DimArray):
                    xdata = args[0].dimvalue(0)
                else:
                    xdata = []
                    for i in range(0, len(args[0])):
                        xdata.append(i)
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
        
        snum = len(xdatalist)
            
        if len(styles) == 0:
            styles = None
        else:
            while len(styles) < snum:
                styles.append('-')
        
        #Get plot data styles - Legend
        zvalues = kwargs.pop('zvalues', None)
        if zvalues is None:
            lines = []
            ls = kwargs.pop('legend', None) 
            if ls is None:
                if styles is None:                
                    for i in range(0, snum):
                        label = kwargs.pop('label', 'S_' + str(i + 1))
                        line = plotutil.getlegendbreak('line', **kwargs)[0]
                        line.setCaption(label)
                        line.setStartValue(i)
                        line.setEndValue(i)
                        lines.append(line)
                else:
                    for i in range(0, len(styles)):
                        line = plotutil.getplotstyle(styles[i], str(i), **kwargs)
                        line.setStartValue(i)
                        line.setEndValue(i)
                        lines.append(line)
                ls = LegendScheme(lines)
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
            ls.setFieldName('Geometry_Z')
        
        aslayer = kwargs.pop('aslayer', True)
        if aslayer:            
            if zvalues is None:
                for i in range(snum):
                    xdatalist[i] = plotutil.getplotdata(xdatalist[i])
                    ydatalist[i] = plotutil.getplotdata(ydatalist[i])
                if snum == 1:
                    if len(lines) == 1:
                        colors = kwargs.pop('colors', None)
                        if not colors is None:
                            colors = plotutil.getcolors(colors)
                            cb = lines[0]
                            lines = []
                            idx = 0
                            for cc in colors:
                                ncb = cb.clone()
                                ncb.setColor(cc)
                                ncb.setStartValue(idx)
                                ncb.setEndValue(idx)
                                lines.append(ncb)
                                idx += 1
                            ls = LegendScheme(lines)
                layer = DrawMeteoData.createPolylineLayer(xdatalist, ydatalist, ls, \
                    'Plot_lines', 'ID', -180, 180)
            else:
                xdata = plotutil.getplotdata(xdatalist[0])
                ydata = plotutil.getplotdata(ydatalist[0])
                zdata = plotutil.getplotdata(zvalues)
                layer = DrawMeteoData.createPolylineLayer(xdata, ydata, zdata, ls, \
                    'Plot_lines', 'ID', -180, 180)
            if (proj != None):
                layer.setProjInfo(proj)
         
            # Add layer
            isadd = kwargs.pop('isadd', True)
            if isadd:
                zorder = kwargs.pop('zorder', None)
                select = kwargs.pop('select', True)
                self.add_layer(layer, zorder, select)
                self.axes.setDrawExtent(layer.getExtent().clone())
                self.axes.setExtent(layer.getExtent().clone())
                
            return MILayer(layer)
        else:
            iscurve = False
            graphics = []
            if zvalues is None:
                #Add data series
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
                        graphic = self.add_graphic(graphic, proj)
                        graphics.append(graphic)
            else:
                xdata = plotutil.getplotdata(xdatalist[0])
                ydata = plotutil.getplotdata(ydatalist[0])
                zdata = plotutil.getplotdata(zvalues)
                graphic = GraphicFactory.createLineString(xdata, ydata, zdata, ls, iscurve)
                self.add_graphic(graphic, proj)
                graphics.append(graphic)
            
            self.axes.setAutoExtent()

            if len(graphics) > 1:
                return graphics
            else:
                return graphics[0]
        
    def plot_bak(self, *args, **kwargs):
        """
        Plot lines and/or markers to the map.
        
        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param style: (*string*) Line style for plot.
        :param linewidth: (*float*) Line width.
        :param color: (*Color*) Line color.
        
        :returns: (*VectoryLayer*) Line VectoryLayer.
        """
        fill_value = kwargs.pop('fill_value', -9999.0)
        proj = kwargs.pop('proj', None)    
        order = kwargs.pop('order', None)
        n = len(args) 
        xdatalist = []
        ydatalist = []    
        styles = []
        if n == 1:
            ydata = plotutil.getplotdata(args[0])
            if isinstance(args[0], DimArray):
                xdata = args[0].dimvalue(0)
            else:
                xdata = []
                for i in range(0, len(args[0])):
                    xdata.append(i)
            xdatalist.append(minum.asarray(xdata).array)
            ydatalist.append(minum.asarray(ydata).array)
        elif n == 2:
            if isinstance(args[1], basestring):
                ydata = plotutil.getplotdata(args[0])
                if isinstance(args[0], DimArray):
                    xdata = args[0].dimvalue(0)
                else:
                    xdata = []
                    for i in range(0, len(args[0])):
                        xdata.append(i)
                styles.append(args[1])
            else:
                xdata = plotutil.getplotdata(args[0])
                ydata = plotutil.getplotdata(args[1])
            xdatalist.append(minum.asarray(xdata).array)
            ydatalist.append(minum.asarray(ydata).array)
        else:
            c = 'x'
            for arg in args: 
                if c == 'x':    
                    xdatalist.append(minum.asarray(arg).array)
                    c = 'y'
                elif c == 'y':
                    ydatalist.append(minum.asarray(arg).array)
                    c = 's'
                elif c == 's':
                    if isinstance(arg, basestring):
                        styles.append(arg)
                        c = 'x'
                    else:
                        styles.append('-')
                        xdatalist.append(minum.asarray(arg).array)
                        c = 'y'
        
        snum = len(xdatalist)
            
        if len(styles) == 0:
            styles = None
        else:
            while len(styles) < snum:
                styles.append('-')
        
        #Get plot data styles - Legend
        lines = []
        ls = kwargs.pop('legend', None) 
        if ls is None:
            if styles != None:
                for i in range(0, len(styles)):
                    line = plotutil.getplotstyle(styles[i], str(i), **kwargs)
                    lines.append(line)
            else:
                for i in range(0, snum):
                    label = kwargs.pop('label', 'S_' + str(i + 1))
                    line = plotutil.getlegendbreak('line', **kwargs)[0]
                    line.setCaption(label)
                    lines.append(line)
            ls = LegendScheme(lines)
        
        layer = DrawMeteoData.createPolylineLayer(xdatalist, ydatalist, ls, \
                'Plot_lines', 'ID', -180, 180)
        if (proj != None):
            layer.setProjInfo(proj)
     
        # Add layer
        isadd = kwargs.pop('isadd', True)
        if isadd:
            zorder = kwargs.pop('zorder', None)
            select = kwargs.pop('select', True)
            self.add_layer(layer, zorder, select)
            self.axes.setDrawExtent(layer.getExtent().clone())
            self.axes.setExtent(layer.getExtent().clone())
            
        return MILayer(layer)
        
    def scatter(self, *args, **kwargs):
        """
        Make a scatter plot on a map.
        
        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param z: (*array_like*) Input z data.
        :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level curves 
            to draw, in increasing order.
        :param cmap: (*string*) Color map string.
        :param colors: (*list*) If None (default), the colormap specified by cmap will be used. If a 
            string, like ‘r’ or ‘red’, all levels will be plotted in this color. If a tuple, different 
            levels will be plotted in different colors in the order specified.
        :param size: (*int of list*) Marker size.
        :param marker: (*string*) Marker of the points.
        :param fill: (*boolean*) Fill markers or not. Default is True.
        :param edge: (*boolean*) Draw edge of markers or not. Default is True.
        :param facecolor: (*Color*) Fill color of markers. Default is black.
        :param edgecolor: (*Color*) Edge color of markers. Default is black.
        :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
        :param zorder: (*int*) Z-order of created layer for display.
        
        :returns: (*VectoryLayer*) Point VectoryLayer.
        """
        n = len(args) 
        if n == 1:
            a = args[0]
            y = a.dimvalue(0)
            x = a.dimvalue(1)
            args = args[1:]
        else:
            x = args[0]
            y = args[1]
            if not isinstance(x, MIArray):
                x = minum.array(x)
            if not isinstance(y, MIArray):
                y = minum.array(y)
            if n == 2:
                a = x
                args = args[2:]
            else:
                a = args[2]
                if not isinstance(a, MIArray):
                    a = minum.array(a)
                args = args[3:]
		
		if (a.ndim == 2) and (x.ndim == 1):
			x, y = minum.meshgrid(x, y)
			
		if (a.size != x.size) or (a.size != y.size):
			raise ValueError('Sizes of x/y and data are not same!')			
        
        ls = kwargs.pop('symbolspec', None)
        if ls is None:
            isunique = False
            colors = kwargs.get('colors', None) 
            if not colors is None:
                if isinstance(colors, (list, tuple)) and len(colors) == x.size:
                    isunique = True
            size = kwargs.get('size', None)
            if not size is None:
                if isinstance(size, (list, tuple, MIArray)) and len(size) == x.size:
                    isunique = True
            marker = kwargs.get('marker', None)
            if not marker is None:
                if isinstance(marker, (list, tuple, MIArray)) and len(marker) == x.size:
                    isunique = True
            if isunique:
                ls = LegendManage.createUniqValueLegendScheme(x.size, ShapeTypes.Point)
            else:
                ls = plotutil.getlegendscheme(args, a.min(), a.max(), **kwargs)
            ls = plotutil.setlegendscheme_point(ls, **kwargs)
        
        if a.size == ls.getBreakNum() and ls.getLegendType() == LegendType.UniqueValue:
            layer = DrawMeteoData.createSTPointLayer_Unique(a.array, x.array, y.array, ls, 'layer', 'data')
        else:
            layer = DrawMeteoData.createSTPointLayer(a.array, x.array, y.array, ls, 'layer', 'data')
        
        proj = kwargs.pop('proj', None)
        if not proj is None:
            layer.setProjInfo(proj)
        avoidcoll = kwargs.pop('avoidcoll', None)
        if not avoidcoll is None:
            layer.setAvoidCollision(avoidcoll)
        
        # Add layer
        isadd = kwargs.pop('isadd', True)
        if isadd:
            zorder = kwargs.pop('zorder', None)
            select = kwargs.pop('select', True)
            self.add_layer(layer, zorder, select)
            self.axes.setDrawExtent(layer.getExtent().clone())
            self.axes.setExtent(layer.getExtent().clone())
        
        return MILayer(layer)
        
    def contour(self, *args, **kwargs):  
        """
        Plot contours on the map.
        
        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) 2-D z value array.
        :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level curves 
            to draw, in increasing order.
        :param cmap: (*string*) Color map string.
        :param colors: (*list*) If None (default), the colormap specified by cmap will be used. If a 
            string, like ``r`` or ``red``, all levels will be plotted in this color. If a tuple of matplotlib 
            color args (string, float, rgb, etc), different levels will be plotted in different colors in 
            the order specified.
        :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
        :param isadd: (*boolean*) Add layer or not. Default is ``True``.
        :param zorder: (*int*) Z-order of created layer for display.
        :param smooth: (*boolean*) Smooth countour lines or not.
        :param select: (*boolean*) Set the return layer as selected layer or not.
        
        :returns: (*VectoryLayer*) Contour VectoryLayer created from array data.
        """
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
        ls = ls.convertTo(ShapeTypes.Polyline)
        plotutil.setlegendscheme(ls, **kwargs)
        isadd = kwargs.pop('isadd', True)
        smooth = kwargs.pop('smooth', True)
        layer = DrawMeteoData.createContourLayer(a.array, x.array, y.array, ls, 'layer', 'data', smooth)
        if layer is None:
            return None
            
        proj = kwargs.pop('proj', None)
        if not proj is None:
            layer.setProjInfo(proj)
        
        # Add layer
        isadd = kwargs.pop('isadd', True)
        if isadd:
            zorder = kwargs.pop('zorder', None)
            select = kwargs.pop('select', True)
            self.add_layer(layer, zorder, select)
            self.axes.setDrawExtent(layer.getExtent().clone())
            self.axes.setExtent(layer.getExtent().clone())
                
        return MILayer(layer)
        
    def contourf(self, *args, **kwargs):  
        """
        Plot filled contours on the map.
        
        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) 2-D z value array.
        :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level curves 
            to draw, in increasing order.
        :param cmap: (*string*) Color map string.
        :param colors: (*list*) If None (default), the colormap specified by cmap will be used. If a 
            string, like ``r`` or ``red``, all levels will be plotted in this color. If a tuple of matplotlib 
            color args (string, float, rgb, etc), different levels will be plotted in different colors in 
            the order specified.
        :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
        :param isadd: (*boolean*) Add layer or not. Default is ``True``.
        :param zorder: (*int*) Z-order of created layer for display.
        :param smooth: (*boolean*) Smooth countour lines or not.
        :param select: (*boolean*) Set the return layer as selected layer or not.
        
        :returns: (*VectoryLayer*) Contour VectoryLayer created from array data.
        """
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
        isadd = kwargs.pop('isadd', True)
        smooth = kwargs.pop('smooth', True)
        layer = DrawMeteoData.createShadedLayer(a.array, x.array, y.array, ls, 'layer', 'data', smooth)
        proj = kwargs.pop('proj', None)
        if not proj is None:
            layer.setProjInfo(proj)
        
        # Add layer
        isadd = kwargs.pop('isadd', True)
        if isadd:
            zorder = kwargs.pop('zorder', None)
            select = kwargs.pop('select', True)
            if zorder is None:
                zorder = 0
            self.add_layer(layer, zorder, select)
            self.axes.setDrawExtent(layer.getExtent().clone())
            self.axes.setExtent(layer.getExtent().clone())
                
        return MILayer(layer)
        
    def imshow(self, *args, **kwargs):
        """
        Display an image on the map.
        
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
        :param fill_color: (*color*) Fill_color. Default is None (white color).
        :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
        :param zorder: (*int*) Z-order of created layer for display.
        :param interpolation: (*string*) Interpolation option [None | bilinear | bicubic].
        
        :returns: (*RasterLayer*) RasterLayer created from array data.
        """
        cmap = plotutil.getcolormap(**kwargs)
        fill_value = kwargs.pop('fill_value', -9999.0)        
        ls = kwargs.pop('symbolspec', None)
        n = len(args) 
        isrgb = False
        if n <= 2:
            if isinstance(args[0], (list, tuple)):
                isrgb = True
                rgbdata = args[0]
                if isinstance(rgbdata[0], DimArray):
                    x = rgbdata[0].dimvalue(1)
                    y = rgbdata[0].dimvalue(0)                
                else:
                    x = minum.arange(0, rgbdata[0].shape[1])
                    y = minum.arange(0, rgbdata[0].shape[0])
            elif args[0].ndim > 2:
                isrgb = True
                rgbdata = args[0]
                x = rgbdata.dimvalue(1)
                y = rgbdata.dimvalue(0)
            else:
                gdata = minum.asgridarray(args[0])
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
        
        isadd = kwargs.pop('isadd', True)
        interpolation = kwargs.pop('interpolation', None)
        if isrgb:
            if isinstance(rgbdata, (list, tuple)):
                rgbd = []
                for d in rgbdata:
                    rgbd.append(d.asarray())
                rgbdata = rgbd
            else:
                rgbdata = rgbdata.asarray()        
            extent = [x[0],x[-1],y[0],y[-1]]
            igraphic = GraphicFactory.createImage(rgbdata, extent)
            x = plotutil.getplotdata(x)
            y = plotutil.getplotdata(y)
            layer = DrawMeteoData.createImageLayer(x, y, igraphic, 'layer_image')
        else:
            if len(args) > 0:
                if ls is None:
                    level_arg = args[0]
                    if isinstance(level_arg, int):
                        cn = level_arg
                        ls = LegendManage.createImageLegend(gdata, cn, cmap)
                    else:
                        if isinstance(level_arg, MIArray):
                            level_arg = level_arg.aslist()
                        ls = LegendManage.createImageLegend(gdata, level_arg, cmap)
            else:    
                if ls is None:
                    ls = LegendManage.createImageLegend(gdata, cmap)
            plotutil.setlegendscheme(ls, **kwargs)
            fill_color = kwargs.pop('fill_color', None)
            if not fill_color is None:
                cb = ls.getLegendBreaks().get(ls.getBreakNum() - 1)
                if cb.isNoData():
                    cb.setColor(plotutil.getcolor(fill_color))

            layer = DrawMeteoData.createRasterLayer(gdata, 'layer', ls) 
                            
        proj = kwargs.pop('proj', None)
        if not proj is None:
            layer.setProjInfo(proj)
        if not interpolation is None:
            layer.setInterpolation(interpolation)
        if isadd:
            zorder = kwargs.pop('zorder', None)
            select = kwargs.pop('select', True)
            if zorder is None:
                zorder = 0
            self.add_layer(layer, zorder, select)
            self.axes.setDrawExtent(layer.getExtent().clone())
            self.axes.setExtent(layer.getExtent().clone())
        return MILayer(layer)
        
    def pcolor(self, *args, **kwargs):
        """
        Create a pseudocolor plot of a 2-D array in a MapAxes.
        
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
        :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
        :param isadd: (*boolean*) Add layer or not. Default is ``True``.
        :param zorder: (*int*) Z-order of created layer for display.
        :param select: (*boolean*) Set the return layer as selected layer or not.
        
        :returns: (*VectoryLayer*) Polygon VectoryLayer created from array data.
        """    
        proj = kwargs.pop('proj', None)            
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
            
        if proj is None or proj.isLonLat():
            lonlim = 90
        else:
            lonlim = 0
            x, y = minum.project(x, y, toproj=proj)
        layer = ArrayUtil.meshLayer(x.asarray(), y.asarray(), a.asarray(), ls, lonlim)
        #layer = ArrayUtil.meshLayer(x.asarray(), y.asarray(), a.asarray(), ls)
        if not proj is None:
            layer.setProjInfo(proj)
            
        # Add layer
        isadd = kwargs.pop('isadd', True)
        if isadd:
            zorder = kwargs.pop('zorder', None)
            select = kwargs.pop('select', True)
            if zorder is None:
                zorder = 0
            self.add_layer(layer, zorder, select)
            self.axes.setDrawExtent(layer.getExtent().clone())
            self.axes.setExtent(layer.getExtent().clone())

        return MILayer(layer)
        
    def gridshow(self, *args, **kwargs):
        """
        Create a grid plot of a 2-D array in a MapAxes.
        
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
        :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
        :param isadd: (*boolean*) Add layer or not. Default is ``True``.
        :param zorder: (*int*) Z-order of created layer for display.
        :param select: (*boolean*) Set the return layer as selected layer or not.
        
        :returns: (*VectoryLayer*) Polygon VectoryLayer created from array data.
        """    
        proj = kwargs.pop('proj', None)            
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

        layer = DrawMeteoData.createGridFillLayer(x.array, y.array, a.array, ls, 'layer', 'data')
        if not proj is None:
            layer.setProjInfo(proj)
            
        # Add layer
        isadd = kwargs.pop('isadd', True)
        if isadd:
            zorder = kwargs.pop('zorder', None)
            select = kwargs.pop('select', True)
            if zorder is None:
                zorder = 0
            self.add_layer(layer, zorder, select)
            self.axes.setDrawExtent(layer.getExtent().clone())
            self.axes.setExtent(layer.getExtent().clone())

        return MILayer(layer)
    
    def quiver(self, *args, **kwargs):
        """
        Plot a 2-D field of quiver in a map.
        
        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param u: (*array_like*) U component of the arrow vectors (wind field) or wind direction.
        :param v: (*array_like*) V component of the arrow vectors (wind field) or wind speed.
        :param z: (*array_like*) Optional, 2-D z value array.
        :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level 
            quiver to draw, in increasing order.
        :param cmap: (*string*) Color map string.
        :param fill_value: (*float*) Fill_value. Default is ``-9999.0``.
        :param isuv: (*boolean*) Is U/V or direction/speed data array pairs. Default is True.
        :param size: (*float*) Base size of the arrows.
        :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
        :param zorder: (*int*) Z-order of created layer for display.
        :param select: (*boolean*) Set the return layer as selected layer or not.
        
        :returns: (*VectoryLayer*) Created barbs VectoryLayer.
        """
        cmap = plotutil.getcolormap(**kwargs)
        fill_value = kwargs.pop('fill_value', -9999.0)
        proj = kwargs.pop('proj', None)
        order = kwargs.pop('order', None)
        isuv = kwargs.pop('isuv', True)
        n = len(args) 
        iscolor = False
        cdata = None
        onlyuv = True
        if n >= 4 and isinstance(args[3], (DimArray, MIArray)):
            onlyuv = False
        if onlyuv:
            u = minum.asarray(args[0])
            v = minum.asarray(args[1])
            xx = args[0].dimvalue(1)
            yy = args[0].dimvalue(0)
            x, y = minum.meshgrid(xx, yy)
            args = args[2:]
            if len(args) > 0:
                cdata = minum.asarray(args[0])
                iscolor = True
                args = args[1:]
        else:
            x = minum.asarray(args[0])
            y = minum.asarray(args[1])
            u = minum.asarray(args[2])
            v = minum.asarray(args[3])
            args = args[4:]
            if len(args) > 0:
                cdata = minum.asarray(args[0])
                iscolor = True
                args = args[1:]
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
            cdata = cdata.array
        if u.ndim == 2 and x.ndim == 1:
            x, y = minum.meshgrid(x, y)
        layer = DrawMeteoData.createVectorLayer(x.array, y.array, u.array, v.array, cdata, ls, 'layer', isuv)
        if not proj is None:
            layer.setProjInfo(proj)
            
        # Add layer
        isadd = kwargs.pop('isadd', True)
        if isadd:
            zorder = kwargs.pop('zorder', None)
            select = kwargs.pop('select', True)
            self.add_layer(layer, zorder, select)
            self.axes.setDrawExtent(layer.getExtent().clone())
            self.axes.setExtent(layer.getExtent().clone())

        return MILayer(layer)
    
    def barbs(self, *args, **kwargs):
        """
        Plot a 2-D field of barbs in a map.
        
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
        :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
        :param zorder: (*int*) Z-order of created layer for display.
        :param select: (*boolean*) Set the return layer as selected layer or not.
        
        :returns: (*VectoryLayer*) Created barbs VectoryLayer.
        """
        cmap = plotutil.getcolormap(**kwargs)
        fill_value = kwargs.pop('fill_value', -9999.0)
        proj = kwargs.pop('proj', None)
        order = kwargs.pop('order', None)
        isuv = kwargs.pop('isuv', True)
        n = len(args) 
        iscolor = False
        cdata = None
        onlyuv = True
        if n >= 4 and isinstance(args[3], (DimArray, MIArray)):
            onlyuv = False
        if onlyuv:
            u = minum.asarray(args[0])
            v = minum.asarray(args[1])
            xx = args[0].dimvalue(1)
            yy = args[0].dimvalue(0)
            x, y = minum.meshgrid(xx, yy)
            args = args[2:]
            if len(args) > 0:
                cdata = minum.asarray(args[0])
                iscolor = True
                args = args[1:]
        else:
            x = minum.asarray(args[0])
            y = minum.asarray(args[1])
            u = minum.asarray(args[2])
            v = minum.asarray(args[3])
            args = args[4:]
            if len(args) > 0:
                cdata = minum.asarray(args[0])
                iscolor = True
                args = args[1:]
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
            cdata = cdata.array
        layer = DrawMeteoData.createBarbLayer(x.array, y.array, u.array, v.array, cdata, ls, 'layer', isuv)
        if not proj is None:
            layer.setProjInfo(proj)
            
        # Add layer
        isadd = kwargs.pop('isadd', True)
        if isadd:
            zorder = kwargs.pop('zorder', None)
            select = kwargs.pop('select', True)
            self.add_layer(layer, zorder, select)
            self.axes.setDrawExtent(layer.getExtent().clone())
            self.axes.setExtent(layer.getExtent().clone())

        return MILayer(layer)
        
    def streamplot(self, *args, **kwargs):
        """
        Plot streamline in a map.
        
        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param u: (*array_like*) U component of the arrow vectors (wind field) or wind direction.
        :param v: (*array_like*) V component of the arrow vectors (wind field) or wind speed.
        :param z: (*array_like*) Optional, 2-D z value array.
        :param color: (*Color*) Streamline color. Default is blue.
        :param fill_value: (*float*) Fill_value. Default is ``-9999.0``.
        :param isuv: (*boolean*) Is U/V or direction/speed data array pairs. Default is True.
        :param density: (*int*) Streamline density. Default is 4.
        :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
        :param zorder: (*int*) Z-order of created layer for display.
        :param select: (*boolean*) Set the return layer as selected layer or not.
        
        :returns: (*VectoryLayer*) Created streamline VectoryLayer.
        """
        cmap = plotutil.getcolormap(**kwargs)
        fill_value = kwargs.pop('fill_value', -9999.0)
        proj = kwargs.pop('proj', None)
        cobj = kwargs.pop('color', 'b')
        color = plotutil.getcolor(cobj)
        isuv = kwargs.pop('isuv', True)
        density = kwargs.pop('density', 4)
        n = len(args)
        if n < 4:
            u = args[0]
            v = args[1]
            y = u.dimvalue(0)
            x = u.dimvalue(1)
            args = args[2:]
        else:
            x = args[0]
            y = args[1]
            u = args[2]
            v = args[3]
            args = args[4:]  
        ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Polyline, color, 1)
        plotutil.setlegendscheme(ls, **kwargs)
        #layer = __plot_uvgriddata_m(plot, udata, vdata, None, ls, 'streamplot', isuv, proj=proj, density=density)
        layer = DrawMeteoData.createStreamlineLayer(u.array, v.array, x.array, y.array, density, ls, 'layer', isuv)
        if not proj is None:
            layer.setProjInfo(proj)
            
        # Add layer
        isadd = kwargs.pop('isadd', True)
        if isadd:
            zorder = kwargs.pop('zorder', None)
            select = kwargs.pop('select', True)
            self.add_layer(layer, zorder, select)
            self.axes.setDrawExtent(layer.getExtent().clone())
            self.axes.setExtent(layer.getExtent().clone())
            
        return MILayer(layer)
        
    def stationmodel(self, smdata, **kwargs):
        """
        Plot station model data on the map.
        
        :param smdata: (*StationModelData*) Station model data.
        :param surface: (*boolean*) Is surface data or not. Default is True.
        :param size: (*float*) Size of the station model symbols. Default is 12.
        :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
        :param order: (*int*) Z-order of created layer for display.
        
        :returns: (*VectoryLayer*) Station model VectoryLayer.
        """
        proj = kwargs.pop('proj', None)
        size = kwargs.pop('size', 12)
        surface = kwargs.pop('surface', True)
        ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Point, Color.blue, size)
        layer = DrawMeteoData.createStationModelLayer(smdata, ls, 'stationmodel', surface)
        if (proj != None):
            layer.setProjInfo(proj)
     
        # Add layer
        isadd = kwargs.pop('isadd', True)
        if isadd:
            zorder = kwargs.pop('zorder', None)
            select = kwargs.pop('select', True)
            self.add_layer(layer, zorder, select)
            self.axes.setDrawExtent(layer.getExtent().clone())
            self.axes.setExtent(layer.getExtent().clone())
            
        return MILayer(layer)
        
    def webmap(self, provider='OpenStreetMap', order=0):
        '''
        Add a new web map layer.
        
        :param provider: (*string*) Web map provider.
        :param order: (*int*) Layer order.
        
        :returns: Web map layer
        '''
        layer = WebMapLayer()
        provider = WebMapProvider.valueOf(provider)
        layer.setWebMapProvider(provider)
        self.add_layer(layer, order)
        return MILayer(layer)
        
    def masklayer(self, mobj, layers):
        '''
        Mask layers.
        
        :param mobj: (*layer or polgyons*) Mask object.
        :param layers: (*list*) The layers will be masked.       
        '''
        mapview = self.axes.getMapView()
        mapview.getMaskOut().setMask(True)
        mapview.getMaskOut().setMaskLayer(mobj.layer.getLayerName())
        for layer in layers:
            layer.layer.setMaskout(True)
            
    def move_graphic(self, graphic, x=0, y=0, coordinates='screen'):
        '''
        Move a graphic by screen coordinate.
        
        :param graphic: (*Graphic*) A graphic.
        :param x: (*float*) X shift for moving.
        :param y: (*float*) Y shift for moving.
        :param coordinates: (*string*) Coordinates of x/y ['screen' | 'data'].
        '''
        mapview = self.axes.getMapView()
        mapview.moveGraphic(graphic, x, y, coordinates == 'screen')

########################################################3
class Test():
    def test():
        print 'Test...'        