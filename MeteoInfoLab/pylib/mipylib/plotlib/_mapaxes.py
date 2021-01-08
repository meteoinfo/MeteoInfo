# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2018-4-5
# Purpose: MeteoInfoLab mapaxes module
# Note: Jython
#-----------------------------------------------------

import os
import numbers

from org.meteoinfo.chart import ChartScaleBar, ChartNorthArrow
from org.meteoinfo.chart.plot import MapPlot, GraphicFactory
from org.meteoinfo.data.meteodata import DrawMeteoData
from org.meteoinfo.map import MapView
from org.meteoinfo.legend import BreakTypes, LegendManage, LegendScheme, LegendType
from org.meteoinfo.shape import Shape, PolylineShape, PolygonShape, ShapeTypes, Graphic
from org.meteoinfo.projection.info import ProjectionInfo
from org.meteoinfo.global import Extent
from org.meteoinfo.layer import LayerTypes, WebMapLayer
from org.meteoinfo.data.mapdata.webmap import WebMapProvider
from org.meteoinfo.layout import ScaleBarType

from java.awt import Font, Color

from ._axes import Axes
import mipylib.numeric as np
from mipylib.numeric.core import NDArray, DimArray
from mipylib.geolib.milayer import MILayer
import mipylib.geolib.migeo as migeo
import plotutil
import mipylib.migl as migl
import mipylib.miutil as miutil

__all__ = ['MapAxes']

##############################################        
class MapAxes(Axes):
    '''
    Axes with geological map coordinate.
    '''
    
    def __init__(self, *args, **kwargs):
        super(MapAxes, self).__init__(*args, **kwargs)               
     
        # Set projection
        projinfo = kwargs.pop('projinfo', None)
        if projinfo is None:
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
                #+ ' +h=' + str(h)
            projinfo = ProjectionInfo.factory(projstr)
        cutoff = kwargs.pop('cutoff', None)
        if not cutoff is None:
            projinfo.setCutoff(cutoff)        
        self.axes.setProjInfo(projinfo)
        self.proj = self.axes.getProjInfo()
        
        # set other properties
        frameon = kwargs.pop('frameon', None)
        if not frameon is None:
            self.axes.setDrawNeatLine(frameon)
        gridlabel = kwargs.pop('gridlabel', True)
        gridlabelloc = kwargs.pop('gridlabelloc', 'left_bottom')
        gridline = kwargs.pop('gridline', False)
        griddx = kwargs.pop('griddx', 10)
        griddy = kwargs.pop('griddy', 10)
        start_lon = kwargs.pop('start_lon', -180)
        start_lat = kwargs.pop('start_lat', -90)
        xyscale = kwargs.pop('xyscale', 1)
        mapframe = self.axes.getMapFrame()
        mapframe.setDrawGridLabel(gridlabel)
        mapframe.setDrawGridTickLine(gridlabel)
        mapframe.setGridLabelPosition(gridlabelloc)
        mapframe.setDrawGridLine(gridline)
        mapframe.setGridXDelt(griddx)
        mapframe.setGridYDelt(griddy)
        mapframe.setGridXOrigin(start_lon)
        mapframe.setGridYOrigin(start_lat)
        mapview = self.axes.getMapView()
        mapview.setXYScaleFactor(xyscale)
        self.axes.setAspect(xyscale)
        boundaryprop = kwargs.pop('boundaryprop', None)
        if not boundaryprop is None:
            boundaryprop = plotutil.getlegendbreak('polygon', **boundaryprop)[0]
            self.axes.setBoundaryProp(boundaryprop)               
    
    def _set_plot(self, plot):
        '''
        Set plot.
        
        :param plot: (*Axes3D*) Plot.
        '''
        if plot is None:
            mapview = MapView()
            self.axes = MapPlot(mapview)
        else:
            self.axes = plot
    
    @property
    def axestype(self):
        return 'map'
    
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
        if not kwargs.has_key('facecolor'):
            kwargs['facecolor'] = None
        lbreak, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        circle = self.axes.addCircle(xy[0], xy[1], radius, lbreak)
        return circle
        
    def scale_bar(self, x, y, **kwargs):
        '''
        Set map scale bar.
        
        :param x: (*float*) The location x of the scale bar.
        :param y: (*float*) The location y of the scale bar.
        :param bartype: (*string*) Scale bar type ['scaleline_1' | 'scaleline_2' | 'alternating_bar'].
        :param width: (*float*) The width of the scale bar with pixel unit.
        :param height: (*float*) The height of the scale bar with pixel unit.
        :param color: (*Color*) The color of the scale bar.
        :param linewidth: (*float*) Line width.
        :param labelcolor: (*Color*) Label color. Default to default is black.
        :param fontname: (*string*) Label font name. Default is ``Arial`` .
        :param fontsize: (*int*) Label font size. Default is ``12`` .
        :param bold: (*boolean*) Is bold font or not. Default is ``False`` .
        :param fontproperties: (*dict*) A dictionary with keyword arguments accepted by the FontProperties
            initializer: *family, style, variant, size, weight*.
        '''
        sb = ChartScaleBar(self.axes)
        sb.setX(x)
        sb.setY(y)
        bartype = kwargs.pop('bartype', None)
        if not bartype is None:
            bartype = ScaleBarType.valueOf(bartype.upper())
            sb.setScaleBarType(bartype)
        linewidth = kwargs.pop('linewidth', None)
        if not linewidth is None:
            sb.setLineWidth(linewidth)
        width = kwargs.pop('width', None)
        if not width is None:
            sb.setWidth(width)
        height = kwargs.pop('height', None)
        if not height is None:
            sb.setHeight(height)
        color = kwargs.pop('color', None)
        if not color is None:
            color = plotutil.getcolor(color)
            sb.setForeground(color)
        fontname = kwargs.pop('fontname', 'Arial')
        fontsize = kwargs.pop('fontsize', 12)
        bold = kwargs.pop('bold', False)
        if bold:
            font = Font(fontname, Font.BOLD, fontsize)
        else:
            font = Font(fontname, Font.PLAIN, fontsize)
        sb.setFont(font)
        bbox = kwargs.pop('bbox', None)
        if not bbox is None:
            fill = bbox.pop('fill', None)
            if not fill is None:
                sb.setFill(fill)
            facecolor = bbox.pop('facecolor', None)
            if not facecolor is None:
                facecolor = plotutil.getcolor(facecolor)
                sb.setFill(True)
                sb.setBackground(facecolor)
            edge = bbox.pop('edge', None)
            if not edge is None:
                sb.setDrawNeatline(edge)
            edgecolor = bbox.pop('edgecolor', None)
            if not edgecolor is None:
                edgecolor = plotutil.getcolor(edgecolor)
                sb.setNeatlineColor(edgecolor)
                sb.setDrawNeatline(True)
            linewidth = bbox.pop('linewidth', None)
            if not linewidth is None:
                sb.setNeatlineSize(linewidth)
                sb.setDrawNeatline(True)
        self.axes.setScaleBar(sb)
        
    def north_arrow(self, x, y, **kwargs):
        '''
        Set map scale bar.
        
        :param x: (*float*) The location x of the scale bar.
        :param y: (*float*) The location y of the scale bar.
        :param bartype: (*string*) Scale bar type ['scaleline_1' | 'scaleline_2' | 'alternating_bar'].
        :param width: (*float*) The width of the scale bar with pixel unit.
        :param height: (*float*) The height of the scale bar with pixel unit.
        :param color: (*Color*) The color of the scale bar.
        :param linewidth: (*float*) Line width.
        :param labelcolor: (*Color*) Label color. Default to default is black.
        :param fontproperties: (*dict*) A dictionary with keyword arguments accepted by the FontProperties
            initializer: *family, style, variant, size, weight*.
        '''
        cna = ChartNorthArrow(self.axes)
        cna.setX(x)
        cna.setY(y)
        linewidth = kwargs.pop('linewidth', None)
        if not linewidth is None:
            cna.setLineWidth(linewidth)
        width = kwargs.pop('width', None)
        if not width is None:
            cna.setWidth(width)
        height = kwargs.pop('height', None)
        if not height is None:
            cna.setHeight(height)
        color = kwargs.pop('color', None)
        if not color is None:
            color = plotutil.getcolor(color)
            cna.setForeground(color)
        bbox = kwargs.pop('bbox', None)
        if not bbox is None:
            fill = bbox.pop('fill', None)
            if not fill is None:
                cna.setFill(fill)
            facecolor = bbox.pop('facecolor', None)
            if not facecolor is None:
                facecolor = plotutil.getcolor(facecolor)
                cna.setFill(True)
                cna.setBackground(facecolor)
            edge = bbox.pop('edge', None)
            if not edge is None:
                cna.setDrawNeatline(edge)
            edgecolor = bbox.pop('edgecolor', None)
            if not edgecolor is None:
                edgecolor = plotutil.getcolor(edgecolor)
                cna.setNeatlineColor(edgecolor)
                cna.setDrawNeatline(True)
            linewidth = bbox.pop('linewidth', None)
            if not linewidth is None:
                cna.setNeatlineSize(linewidth)
                cna.setDrawNeatline(True)
        self.axes.setNorthArrow(cna)
        
    def grid(self, b=None, **kwargs):
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
            super(MapAxes, self).grid(b, **kwargs)
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
                fn = os.path.join(migl.get_map_folder(), fn)
            if os.path.exists(fn):
                encoding = kwargs.pop('encoding', None)
                layer = migeo.shaperead(fn, encoding)
                islayer = True
            else:
                raise IOError('File not exists: ' + fn)
        elif isinstance(args[0], MILayer):
            layer = args[0]
            islayer = True

        visible = kwargs.pop('visible', True)
        if islayer:    
            layer = layer.layer
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
                    if layer.getShapeType().isPolygon():
                        if not kwargs.has_key('edgecolor'):
                            kwargs['edgecolor'] = 'k'
                    if len(kwargs) > 0 and layer.getLegendScheme().getBreakNum() == 1:
                        lb = layer.getLegendScheme().getLegendBreaks().get(0)
                        btype = lb.getBreakType()
                        geometry = 'point'
                        if btype == BreakTypes.PolylineBreak:
                            geometry = 'line'
                        elif btype == BreakTypes.PolygonBreak:
                            geometry = 'polygon'
                            if not kwargs.has_key('facecolor'):
                                kwargs['facecolor'] = None
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
                if isinstance(lat, numbers.Number):
                    displaytype = 'point'
                else:
                    if len(lat) == 1:
                        displaytype = 'point'
                    if isinstance(lon, (list, tuple)):
                        lon = np.array(lon)
                    if isinstance(lat, (list, tuple)):
                        lat = np.array(lat)

                lbreak, isunique = plotutil.getlegendbreak(displaytype, **kwargs)
                iscurve = kwargs.pop('iscurve', False)
                if displaytype == 'point':
                    #graphic = self.axes.addPoint(lat, lon, lbreak)
                    if isinstance(lon, NDArray):
                        graphic = GraphicFactory.createPoints(lon._array, lat._array, lbreak)
                    else:
                        graphic = GraphicFactory.createPoint(lon, lat, lbreak)
                elif displaytype == 'polyline' or displaytype == 'line':
                    #graphic = self.axes.addPolyline(lat, lon, lbreak, iscurve)
                    graphic = GraphicFactory.createLineString(lon._array, lat._array, lbreak, iscurve)
                elif displaytype == 'polygon':
                    #graphic = self.axes.addPolygon(lat, lon, lbreak)
                    graphic = GraphicFactory.createPolygons(lon._array, lat._array, lbreak)

                if graphic.getNumGraphics() == 1:
                    graphic = graphic.getGraphicN(0)

                if visible:
                    if graphic.isCollection():
                        if self.islonlat():
                            self.axes.addGraphics(graphic)
                        else:
                            graphic = self.axes.addGraphics(graphic, migeo.projinfo())
                    else:
                        if self.islonlat():
                            self.axes.addGraphic(graphic)
                        else:
                            graphic = self.axes.addGraphic(graphic, migeo.projinfo())

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
        if proj is None:
            is_lonlat = True
        else:
            is_lonlat = proj.isLonLat()
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
                    zvalues = np.array(zvalues)
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
                if is_lonlat:
                    layer = DrawMeteoData.createPolylineLayer(xdatalist, ydatalist, ls, \
                        'Plot_lines', 'ID', -180, 180)
                else:
                    layer = DrawMeteoData.createPolylineLayer(xdatalist, ydatalist, ls, \
                                                              'Plot_lines', 'ID')
            else:
                xdata = plotutil.getplotdata(xdatalist[0])
                ydata = plotutil.getplotdata(ydatalist[0])
                zdata = plotutil.getplotdata(zvalues)
                if is_lonlat:
                    layer = DrawMeteoData.createPolylineLayer(xdata, ydata, zdata, ls, \
                        'Plot_lines', 'ID', -180, 180)
                else:
                    layer = DrawMeteoData.createPolylineLayer(xdata, ydata, zdata, ls, \
                                                              'Plot_lines', 'ID')
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
            xdatalist.append(np.asarray(xdata)._array)
            ydatalist.append(np.asarray(ydata)._array)
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
            xdatalist.append(np.asarray(xdata)._array)
            ydatalist.append(np.asarray(ydata)._array)
        else:
            c = 'x'
            for arg in args: 
                if c == 'x':    
                    xdatalist.append(np.asarray(arg)._array)
                    c = 'y'
                elif c == 'y':
                    ydatalist.append(np.asarray(arg)._array)
                    c = 's'
                elif c == 's':
                    if isinstance(arg, basestring):
                        styles.append(arg)
                        c = 'x'
                    else:
                        styles.append('-')
                        xdatalist.append(np.asarray(arg)._array)
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
            if not isinstance(x, NDArray):
                x = np.array(x)
            if not isinstance(y, NDArray):
                y = np.array(y)
            if n == 2:
                a = x
                args = args[2:]
            else:
                a = args[2]
                if not isinstance(a, NDArray):
                    a = np.array(a)
                args = args[3:]
		
		if (a.ndim == 2) and (x.ndim == 1):
			x, y = np.meshgrid(x, y)
			
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
                if isinstance(size, (list, tuple, NDArray)) and len(size) == x.size:
                    isunique = True
            marker = kwargs.get('marker', None)
            if not marker is None:
                if isinstance(marker, (list, tuple, NDArray)) and len(marker) == x.size:
                    isunique = True
            if isunique:
                ls = LegendManage.createUniqValueLegendScheme(x.size, ShapeTypes.Point)
            else:
                ls = plotutil.getlegendscheme(args, a.min(), a.max(), **kwargs)
            ls = plotutil.setlegendscheme_point(ls, **kwargs)
        
        if a.size == ls.getBreakNum() and ls.getLegendType() == LegendType.UniqueValue:
            layer = DrawMeteoData.createSTPointLayer_Unique(a._array, x._array, y._array, ls, 'layer', 'data')
        else:
            layer = DrawMeteoData.createSTPointLayer(a._array, x._array, y._array, ls, 'layer', 'data')
        
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

    def text(self, x, y, s, **kwargs):
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
        islonlat = kwargs.pop('islonlat', True)
        self.axes.addText(ctext, islonlat)
        return ctext
        
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
        layer = DrawMeteoData.createContourLayer(a._array, x._array, y._array, ls, 'layer', 'data', smooth)
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
        if not kwargs.has_key('edgecolor'):
            kwargs['edgecolor'] = None
        plotutil.setlegendscheme(ls, **kwargs)
        isadd = kwargs.pop('isadd', True)
        smooth = kwargs.pop('smooth', True)
        layer = DrawMeteoData.createShadedLayer(a._array, x._array, y._array, ls, 'layer', 'data', smooth)
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
                    x = np.arange(0, rgbdata[0].shape[1])
                    y = np.arange(0, rgbdata[0].shape[0])
            elif args[0].ndim > 2:
                isrgb = True
                rgbdata = args[0]
                x = rgbdata.dimvalue(1)
                y = rgbdata.dimvalue(0)
            else:
                gdata = np.asgridarray(args[0])
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
                gdata = np.asgridarray(a, x, y, fill_value)
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
                        if isinstance(level_arg, NDArray):
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
            x, y = np.meshgrid(x, y)  
            
        ls = plotutil.getlegendscheme(args, a.min(), a.max(), **kwargs)   
        ls = ls.convertTo(ShapeTypes.Polygon)
        if not kwargs.has_key('edgecolor'):
            kwargs['edgecolor'] = None
        plotutil.setlegendscheme(ls, **kwargs)
            
        if proj is None or proj.isLonLat():
            lonlim = 90
        else:
            lonlim = 0
            x, y = np.project(x, y, toproj=proj)
        layer = DrawMeteoData.meshLayer(x.asarray(), y.asarray(), a.asarray(), ls, lonlim)
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

        layer = DrawMeteoData.createGridFillLayer(x._array, y._array, a._array, ls, 'layer', 'data')
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
        if n >= 4 and isinstance(args[3], (DimArray, NDArray)):
            onlyuv = False
        if onlyuv:
            u = np.asarray(args[0])
            v = np.asarray(args[1])
            xx = args[0].dimvalue(1)
            yy = args[0].dimvalue(0)
            x, y = np.meshgrid(xx, yy)
            args = args[2:]
            if len(args) > 0:
                cdata = np.asarray(args[0])
                iscolor = True
                args = args[1:]
        else:
            x = np.asarray(args[0])
            y = np.asarray(args[1])
            u = np.asarray(args[2])
            v = np.asarray(args[3])
            args = args[4:]
            if len(args) > 0:
                cdata = np.asarray(args[0])
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
                    if isinstance(levs, NDArray):
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
            cdata = cdata._array
        if u.ndim == 2 and x.ndim == 1:
            x, y = np.meshgrid(x, y)
        layer = DrawMeteoData.createVectorLayer(x._array, y._array, u._array, v._array, cdata, ls, 'layer', isuv)
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
        if n >= 4 and isinstance(args[3], (DimArray, NDArray)):
            onlyuv = False
        if onlyuv:
            u = np.asarray(args[0])
            v = np.asarray(args[1])
            xx = args[0].dimvalue(1)
            yy = args[0].dimvalue(0)
            x, y = np.meshgrid(xx, yy)
            args = args[2:]
            if len(args) > 0:
                cdata = np.asarray(args[0])
                iscolor = True
                args = args[1:]
        else:
            x = np.asarray(args[0])
            y = np.asarray(args[1])
            u = np.asarray(args[2])
            v = np.asarray(args[3])
            args = args[4:]
            if len(args) > 0:
                cdata = np.asarray(args[0])
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
                    if isinstance(levs, NDArray):
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
            cdata = cdata._array
        layer = DrawMeteoData.createBarbLayer(x._array, y._array, u._array, v._array, cdata, ls, 'layer', isuv)
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
        proj = kwargs.pop('proj', None)
        isuv = kwargs.pop('isuv', True)
        density = kwargs.pop('density', 4)
        n = len(args)
        if n < 4:
            u = args[0]
            v = args[1]
            y = u.dimvalue(0)
            x = u.dimvalue(1)
        else:
            x = args[0]
            y = args[1]
            u = args[2]
            v = args[3]
        ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Polyline)
        #plotutil.setlegendscheme(ls, **kwargs)
        lb, isunique = plotutil.getlegendbreak('line', **kwargs)
        if not kwargs.has_key('headwidth'):
            kwargs['headwidth'] = 8
        if not kwargs.has_key('overhang'):
            kwargs['overhang'] = 0.5
        lb = plotutil.line2stream(lb, **kwargs)
        ls.setLegendBreak(0, lb)

        #layer = __plot_uvgriddata_m(plot, udata, vdata, None, ls, 'streamplot', isuv, proj=proj, density=density)
        layer = DrawMeteoData.createStreamlineLayer(u._array, v._array, x._array, y._array, density, ls, 'layer', isuv)
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
        color = kwargs.pop('color', 'b')
        color = plotutil.getcolor(color)
        ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Point, color, size)
        layer = DrawMeteoData.createStationModelLayer(smdata, ls, 'stationmodel', surface)
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
