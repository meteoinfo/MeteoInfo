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
from org.meteoinfo.chart.plot import GridLabelPosition
from org.meteoinfo.chart.geo import MapPlot
from org.meteoinfo.chart.graphic import GraphicFactory, WebMapImage
from org.meteoinfo.geo.meteodata import DrawMeteoData
from org.meteoinfo.geo.mapview import MapView
from org.meteoinfo.geo.io import GraphicUtil
from org.meteoinfo.geometry.legend import BreakTypes, LegendScheme, LegendType, LegendManage
from org.meteoinfo.geometry.shape import Shape, PolylineShape, PolygonShape, ShapeTypes
from org.meteoinfo.geometry.graphic import Graphic
from org.meteoinfo.projection import ProjectionInfo
from org.meteoinfo.common import Extent
from org.meteoinfo.geo.layer import LayerTypes, WebMapLayer
from org.meteoinfo.data.mapdata.webmap import WebMapProvider, DefaultTileFactory, TileFactoryInfo

from java.awt import Font, Color

from ._axes import Axes
import mipylib.numeric as np
from mipylib.numeric.core import NDArray, DimArray
from mipylib.geolib.milayer import MILayer
from mipylib.geolib._graphic import GeoGraphicCollection
import mipylib.geolib.migeo as migeo
import plotutil
import colors
import mipylib.migl as migl
import mipylib.miutil as miutil

__all__ = ['MapAxes','WebMapProvider']

##############################################        
class MapAxes(Axes):
    """
    Axes with geological map coordinate.
    """
    
    def __init__(self, *args, **kwargs):
        super(MapAxes, self).__init__(*args, **kwargs)               
     
        # Set projection
        projinfo = kwargs.pop('projection', None)
        if projinfo is None:
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
        self._axes.setProjInfo(projinfo)
        self.proj = self._axes.getProjInfo()

        xyscale = kwargs.pop('xyscale', 1)
        self._axes.setAspect(xyscale)
        boundaryprop = kwargs.pop('boundaryprop', None)
        if not boundaryprop is None:
            boundaryprop = plotutil.getlegendbreak('polygon', **boundaryprop)[0]
            self._axes.setBoundaryProp(boundaryprop)
    
    def _set_plot(self, plot):
        """
        Set plot.
        
        :param plot: (*Axes3D*) Plot.
        """
        if plot is None:
            self._axes = MapPlot()
        else:
            self._axes = plot
    
    @property
    def axestype(self):
        return 'map'
    
    def islonlat(self):
        """
        Get if the map axes is lonlat projection or not.
        
        :returns: (*boolean*) Is lonlat projection or not.
        """
        return self.proj.isLonLat()
            
    def add_layer(self, layer, zorder=None, select=None):
        """
        Add a map layer
        
        :param layer: (*MapLayer*) The map layer.
        :param zorder: (*int*) Layer z order.
        :param select: (*boolean*) Select layer or not.
        """
        if isinstance(layer, MILayer):
            layer = layer._layer

        if zorder is None:
            self._axes.addLayer(layer)
        else:
            if zorder > self.num_layers():
                zorder = self.num_layers()

            self._axes.addLayer(zorder, layer)

        if not select is None:
            if select:
                self._axes.setSelectedLayer(layer)

        self.stale = True

    def num_layers(self):
        """
        Get number of layers.

        :return: (*int*) Number of layers
        """
        return self._axes.getLayerNum()
                
    def get_layers(self):
        """
        Get all layers.
        
        :returns: All layers
        """
        r = self._axes.getLayers()
        layers = []
        for layer in r:
            layers.append(MILayer(layer))
        return layers
    
    def get_layer(self, by):
        """
        Get a layer by name or index.
        
        :param by: (*string or int*) Layer name or index.
        
        :returns: The layer.
        """
        r = self._axes.getLayer(by)
        if not r is None:
            r = MILayer(r)
        return r
            
    def set_active_layer(self, layer):
        """
        Set active layer
        
        :param layer: (*MILayer*) The map layer.
        """
        self._axes.setSelectedLayer(layer._layer)
        
    # def add_graphic(self, graphic, proj=None, **kwargs):
    #     """
    #     Add a graphic
    #
    #     :param graphic: (*Graphic*) The graphic to be added.
    #     :param proj: (*ProjectionInfo*) Graphic projection.
    #
    #     :returns: Added graphic
    #     """
    #     if proj is None:
    #         self._axes.addGraphic(graphic)
    #     else:
    #         graphic = self._axes.addGraphic(graphic, proj)
    #
    #     self.stale = True
    #     return graphic
        
    def add_circle(self, xy, radius=5, **kwargs):
        """
        Add a circle patch
        """
        if not kwargs.has_key('facecolor'):
            kwargs['facecolor'] = None
        lbreak, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        circle = self._axes.addCircle(xy[0], xy[1], radius, lbreak)
        self.stale = True
        return circle
        
    def scale_bar(self, x, y, **kwargs):
        """
        Set map scale bar.
        
        :param x: (*float*) The location x of the scale bar.
        :param y: (*float*) The location y of the scale bar.
        :param bartype: (*string*) Scale bar type ['scale_line_1' | 'scale_line_2' | 'alternating_bar'].
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
        """
        sb = ChartScaleBar(self._axes)
        sb.setX(x)
        sb.setY(y)
        bartype = kwargs.pop('bartype', None)
        if not bartype is None:
            bartype = ChartScaleBar.ScaleBarType.valueOf(bartype.upper())
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
        self._axes.setScaleBar(sb)
        
    def north_arrow(self, x, y, **kwargs):
        """
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
        """
        cna = ChartNorthArrow(self._axes)
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
        self._axes.setNorthArrow(cna)
                
    def axis(self, limits=None, lonlat=True):
        """
        Sets the min and max of the x and y map axes, with ``[xmin, xmax, ymin, ymax]`` .
        
        :param limits: (*list*) Min and max of the x and y map axes.
        :param lonlat: (*boolean*) Is longitude/latitude or not.
        """
        if limits is None:
            self._axes.setDrawExtent(self._axes.getFullExtent())
            self._axes.setExtent(self._axes.getDrawExtent().clone())
            return True
        else:
            if len(limits) == 4:
                xmin = limits[0]
                xmax = limits[1]
                ymin = limits[2]
                ymax = limits[3]
                extent = Extent(xmin, xmax, ymin, ymax)
                if lonlat:
                    self._axes.setLonLatExtent(extent)
                    self._axes.setExtent(self._axes.getDrawExtent().clone())
                else:
                    self._axes.setDrawExtent(extent)
                    self._axes.setExtent(extent)
                return True
            else:
                print('The limits parameter must be a list with 4 elements: xmin, xmax, ymin, ymax!')
                return None
        
    def data2pixel(self, x, y, z=None):
        """
        Transform data coordinate to screen coordinate
        
        :param x: (*float*) X coordinate.
        :param y: (*float*) Y coordinate.
        :param z: (*float*) Z coordinate - only used for 3-D axes.
        """
        if not self._axes.isLonLatMap():
            x, y = migeo.project(x, y, toproj=self.proj)  
            
        rect = self._axes.getPositionArea()
        r = self._axes.projToScreen(x, y, rect)
        sx = r[0] + rect.getX()
        sy = r[1] + rect.getY()
        sy = self.figure.get_size()[1] - sy
        return sx, sy

    def grid(self, b=None, **kwargs):
        """
        Turn the axes grids on or off.

        :param b: If b is *None* and *len(kwargs)==0* , toggle the grid state. If *kwargs*
            are supplied, it is assumed that you want a grid and *b* is thus set to *True* .
        :param which: *which* can be 'major' (default), 'minor', or 'both' to control
            whether major tick grids, minor tick grids, or both are affected.
        :param axis: *axis* can be 'both' (default), 'x', or 'y' to control which set of
            gridlines are drawn.
        :param color: (*color*) Line color.
        :param alpha: (*float*) Color alpha.
        :param linewidth: (*float*) Line width.
        :param linestyle: (*str*) Line dash style.
        :param top: (*bool*) Plot grid line on top of the graphics or not.
        :param tickvisible: (*bool*) Tick label visible or not.
        :param tickposition: (*str*) Tick label position ['all' | 'left_bottom' | 'left_top' |
            'right_bottom' | 'right_up'].
        :param tickcolor: (*color*) Tick label color.
        :param tickfont: (*dict*) Tick label font.
        :param fixlocation: (*bool*) Fix lon/lat locations or not.
        """
        gridline = Axes.grid(self, b, **kwargs)

        tickvisible = kwargs.pop('tickvisible', None)
        if not tickvisible is None:
            gridline.setLabelVisible(tickvisible)
        tickposition =kwargs.pop( 'tickposition', None)
        if not tickposition is None:
            tickposition = GridLabelPosition.valueOf(tickposition.upper())
            gridline.setLabelPosition(tickposition)
        tickcolor = kwargs.pop('tickcolor', None)
        if not tickcolor is None:
            c = plotutil.getcolor(tickcolor)
            gridline.setLabelColor(c)
        tickfontdic = kwargs.pop('tickfont', None)
        if tickfontdic is None:
            tickfont = plotutil.getfont_1(**kwargs)
        else:
            tickfont = plotutil.getfont(tickfontdic)
        gridline.setLabelFont(tickfont)
        fixlocation = kwargs.pop('fixlocation', None)
        if fixlocation is not None:
            gridline.setFixLocations(fixlocation)

    def set_xticks(self, locs):
        """
        Set x axis tick locations.
        """
        if self.islonlat():
            Axes.set_xticks(self, locs)
        else:
            gridline = self._axes.getGridLine()
            if isinstance(locs, (NDArray, DimArray)):
                locs = locs.aslist()
            gridline.setLongitudeLocations(locs)

    def set_yticks(self, locs):
        """
        Set y axis tick locations.
        """
        if self.islonlat():
            Axes.set_yticks(self, locs)
        else:
            gridline = self._axes.getGridLine()
            if isinstance(locs, (NDArray, DimArray)):
                locs = locs.aslist()
            gridline.setLatitudeLocations(locs)
        
    def loadmip(self, mipfn, mfidx=0):
        """
        Load one map frame from a MeteoInfo project file.
        
        :param mipfn: (*string*) MeteoInfo project file name.
        :param mfidx: (*int*) Map frame index.
        """
        self._axes.loadMIProjectFile(mipfn, mfidx)
        
    def geoshow(self, *args, **kwargs):
        """
        Display map layer or longitude latitude data.
        
        Syntax:
        --------    
            geoshow(shapefilename) - Displays the map data from a shape file.
            geoshow(layer) - Displays the map data from a map layer which may created by ``shaperead`` function.
            geoshow(S) - Displays the vector geographic features stored in S as points, multipoints, lines, or 
              polygons.
            geoshow(lat, lon) - Displays the latitude and longitude vectors.
        """
        islayer = False
        if isinstance(args[0], basestring):
            fn = args[0]
            encoding = kwargs.pop('encoding', None)
            layer = migeo.georead(fn, encoding)
            islayer = True
        elif isinstance(args[0], MILayer):
            layer = args[0]
            islayer = True

        visible = kwargs.pop('visible', True)
        if islayer:
            layer.visible = visible
            xshift = kwargs.pop('xshift', 0)
            zorder = kwargs.pop('zorder', None)
            if layer.layer_type == LayerTypes.IMAGE_LAYER:
                interpolation = kwargs.pop('interpolation', None)
                graphics = layer.get_graphics(xshift, interpolation)
            else:
                #LegendScheme
                ls = kwargs.pop('symbolspec', None)
                if ls is None:
                    if layer.shapetype.isPolygon():
                        if not kwargs.has_key('edgecolor'):
                            kwargs['edgecolor'] = 'k'
                    if len(kwargs) > 0 and layer.legend.getBreakNum() == 1:
                        lb = layer.legend.getLegendBreaks().get(0)
                        btype = lb.getBreakType()
                        geometry = 'point'
                        if btype == BreakTypes.POLYLINE_BREAK:
                            geometry = 'line'
                        elif btype == BreakTypes.POLYGON_BREAK:
                            geometry = 'polygon'
                            if not kwargs.has_key('facecolor'):
                                kwargs['facecolor'] = None
                        lb, isunique = plotutil.getlegendbreak(geometry, **kwargs)
                        layer.legend.getLegendBreaks().set(0, lb)
                else:
                    layer.legend = ls
                graphics = layer.get_graphics(xshift)

            antialias = kwargs.pop('antialias', None)
            if antialias is not None:
                graphics.setAntiAlias(antialias)

            graphics = self.add_graphic(graphics, projection=layer.proj, zorder=zorder)
            self._axes.setDrawExtent(graphics.getExtent().clone())
            self._axes.setExtent(graphics.getExtent().clone())
            return GeoGraphicCollection(graphics)
        else:
            if isinstance(args[0], Graphic):
                graphic = args[0]
                displaytype = 'point'
                stype = graphic.getShape().getShapeType()
                if stype == ShapeTypes.POLYLINE:
                    displaytype = 'line'
                elif stype == ShapeTypes.POLYGON:
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
                    #graphic = self._axes.addPoint(lat, lon, lbreak)
                    if isinstance(lon, NDArray):
                        graphic = GraphicFactory.createPoints(lon._array, lat._array, lbreak)
                    else:
                        graphic = GraphicFactory.createPoint(lon, lat, lbreak)
                elif displaytype == 'polyline' or displaytype == 'line':
                    #graphic = self._axes.addPolyline(lat, lon, lbreak, iscurve)
                    graphic = GraphicFactory.createLineString(lon._array, lat._array, lbreak, iscurve)
                elif displaytype == 'polygon':
                    #graphic = self._axes.addPolygon(lat, lon, lbreak)
                    graphic = GraphicFactory.createPolygons(lon._array, lat._array, lbreak)

                if graphic.getNumGraphics() == 1:
                    graphic = graphic.getGraphicN(0)

                if visible:
                    if graphic.isCollection():
                        if self.islonlat():
                            self._axes.addGraphics(graphic)
                        else:
                            graphic = self._axes.addGraphics(graphic, migeo.projinfo())
                    else:
                        if self.islonlat():
                            self._axes.addGraphic(graphic)
                        else:
                            graphic = self._axes.addGraphic(graphic, migeo.projinfo())

            return graphic
            
    def plot(self, *args, **kwargs):
        """
        Plot lines and/or markers to the map.
        
        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param style: (*string*) Line style for plot.
        :param linewidth: (*float*) Line width.
        :param color: (*Color*) Line color.
        
        :returns: (*VectorLayer*) Line VectorLayer.
        """
        fill_value = kwargs.pop('fill_value', -9999.0)
        proj = kwargs.pop('proj', migeo.projinfo())
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
        cdata = kwargs.pop('cdata', zvalues)
        if cdata is None:
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
            ls.setFieldName('Geometry_Z')

        iscurve = kwargs.pop('curve', False)
        graphics = []
        if cdata is None:
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
                graphic = self.add_graphic(graphic, proj)
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
            zdata = plotutil.getplotdata(cdata)
            graphic = GraphicFactory.createLineString(xdata, ydata, zdata, ls, iscurve)
            graphic = self.add_graphic(graphic, proj)
            graphics.append(graphic)

        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            for graphic in graphics:
                graphic.setAntiAlias(antialias)

        if len(graphics) > 1:
            return graphics
        else:
            return graphics[0]


    def scatter(self, *args, **kwargs):
        """
        Make a scatter plot on a map.
        
        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param z: (*array_like*) Input z data.
        :param levels: (*array_like*) Optional. A list of floating point numbers indicating the level curves
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
            if a.ndim == 2:
                x, y = np.meshgrid(x, y)
            args = args[1:]
        else:
            x = np.asarray(args[0])
            y = np.asarray(args[1])
            if n == 2:
                a = x
                args = args[2:]
            else:
                a = np.asarray(args[2])
                if a.ndim == 2 and x.ndim == 1 and y.ndim == 1:
                    x, y = np.meshgrid(x, y)
                args = args[3:]
		
		if (a.ndim == 2) and (x.ndim == 1):
			x, y = np.meshgrid(x, y)
			
		if (a.size != x.size) or (a.size != y.size):
			raise ValueError('Sizes of x/y and data are not same!')			
        
        ls = kwargs.pop('symbolspec', None)
        if ls is None:
            isunique = (a.ndim == 0)
            if not isunique:
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
                ls = LegendManage.createUniqValueLegendScheme(x.size, ShapeTypes.POINT)
            else:
                ls = plotutil.getlegendscheme(args, a.min(), a.max(), **kwargs)
            ls = plotutil.setlegendscheme_point(ls, **kwargs)

        proj = kwargs.pop('proj', migeo.projinfo())
        # Create graphics
        if a.ndim == 0:
            graphics = GraphicFactory.createPoints(x._array, y._array, ls.getLegendBreak(0))
        else:
            graphics = GraphicFactory.createPoints(x._array, y._array, a._array, ls)

        visible = kwargs.pop('visible', True)
        if visible:
            zorder = kwargs.pop('zorder', None)
            graphics = self.add_graphic(graphics, projection=proj, zorder=zorder)
            self._axes.setExtent(graphics.getExtent())
            self._axes.setDrawExtent(graphics.getExtent())

        return graphics

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
        self._axes.addText(ctext, islonlat)
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
            color args (string, float, rgb, etc.), different levels will be plotted in different colors in
            the order specified.
        :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
        :param isadd: (*boolean*) Add layer or not. Default is ``True``.
        :param zorder: (*int*) Z-order of created layer for display.
        :param smooth: (*boolean*) Smooth contour lines or not.
        :param select: (*boolean*) Set the return layer as selected layer or not.
        
        :returns: (*GraphicCollection*) Contour graphics created from array data.
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
        ls = ls.convertTo(ShapeTypes.POLYLINE)
        plotutil.setlegendscheme(ls, **kwargs)
        smooth = kwargs.pop('smooth', True)
        if x.ndim == 2 and y.ndim == 2:
            griddata_props = kwargs.pop('griddata_props', dict(method='idw', pointnum=5, convexhull=True))
            a, x, y = np.griddata((x,y), a, **griddata_props)

        graphics = GraphicFactory.createContourLines(x.asarray(), y.asarray(), a.asarray(), ls, smooth)

        proj = kwargs.pop('proj', migeo.projinfo())
        
        # Add graphics
        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            graphics.setAntiAlias(antialias)

        visible = kwargs.pop('visible', True)
        if visible:
            zorder = kwargs.pop('zorder', None)
            contours = self.add_graphic(graphics, projection=proj, zorder=zorder)
            self._axes.setDrawExtent(graphics.getExtent())
            self._axes.setExtent(graphics.getExtent())
                
        return graphics
        
    def contourf(self, *args, **kwargs):  
        """
        Plot filled contours on the map.
        
        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) 2-D z value array.
        :param levels: (*array_like*) Optional. A list of floating point numbers indicating the level curves
            to draw, in increasing order.
        :param cmap: (*string*) Color map string.
        :param colors: (*list*) If None (default), the colormap specified by cmap will be used. If a 
            string, like ``r`` or ``red``, all levels will be plotted in this color. If a tuple of matplotlib 
            color args (string, float, rgb, etc.), different levels will be plotted in different colors in
            the order specified.
        :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
        :param isadd: (*boolean*) Add layer or not. Default is ``True``.
        :param zorder: (*int*) Z-order of created layer for display.
        :param smooth: (*boolean*) Smooth contour lines or not.
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

        if not kwargs.has_key('extend'):
            kwargs['extend'] = 'neither'
        ls = plotutil.getlegendscheme(args, a.min(), a.max(), **kwargs)
        ls = ls.convertTo(ShapeTypes.POLYGON)
        if not kwargs.has_key('edgecolor'):
            kwargs['edgecolor'] = None
        plotutil.setlegendscheme(ls, **kwargs)
        smooth = kwargs.pop('smooth', True)
        if x.ndim == 2 and y.ndim == 2:
            griddata_props = kwargs.pop('griddata_props', dict(method='idw', pointnum=5, convexhull=True))
            a, x, y = np.griddata((x,y), a, **griddata_props)

        graphics = GraphicFactory.createContourPolygons(x.asarray(), y.asarray(), a.asarray(), ls, smooth)
        proj = kwargs.pop('proj', migeo.projinfo())
        
        # Add graphics
        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            graphics.setAntiAlias(antialias)

        visible = kwargs.pop('visible', True)
        if visible:
            zorder = kwargs.pop('zorder', None)
            if zorder is None:
                zorder = 0
            graphics = self.add_graphic(graphics, projection=proj, zorder=zorder)
            self._axes.setDrawExtent(graphics.getExtent())
            self._axes.setExtent(graphics.getExtent())
                
        return graphics
        
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
            color args (string, float, rgb, etc.), different levels will be plotted in different colors in
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
        
        visible = kwargs.pop('visible', True)
        interpolation = kwargs.pop('interpolation', None)
        proj = kwargs.pop('proj', migeo.projinfo())
        if isrgb:
            if isinstance(rgbdata, (list, tuple)):
                rgbd = []
                for d in rgbdata:
                    rgbd.append(d.asarray())
                rgbdata = rgbd
            else:
                rgbdata = rgbdata.asarray()
            y_reverse = False
            if x[1] < x[0]:
                x = x[::-1]
            if y[1] < y[0]:
                y = y[::-1]
                y_reverse = True
            extent = [x[0],x[-1],y[0],y[-1]]
            igraphic = GraphicFactory.createImage(rgbdata, extent, y_reverse)
        else:
            if x[1] < x[0]:
                x = x[::-1]
                arr = arr[:,::-1]
            if y[1] < y[0]:
                y = y[::-1]
                arr = arr[::-1,:]
            extent = [x[0],x[-1],y[0],y[-1]]
            if ls is None:
                vmin = kwargs.pop('vmin', arr.min())
                vmax = kwargs.pop('vmax', arr.max())
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
                    ls = plotutil.getlegendscheme(args, vmin, vmax, **kwargs)
                    norm = kwargs.pop('norm', colors.Normalize(vmin, vmax))
                    ls.setNormalize(norm._norm)
                    ls.setColorMap(cmap)
            plotutil.setlegendscheme(ls, **kwargs)
            fill_color = kwargs.pop('fill_color', None)
            if not fill_color is None:
                cb = ls.getLegendBreaks().get(ls.getBreakNum() - 1)
                if cb.isNoData():
                    cb.setColor(plotutil.getcolor(fill_color))

            if not proj.equals(self.proj):
                arr, x, y = migeo.reproject(arr, x, y, fromproj=proj, toproj=self.proj)
                extent = [x[0],x[-1],y[0],y[-1]]
            igraphic = GraphicFactory.createImage(arr._array, x._array, y._array, ls, extent)

        if not interpolation is None:
            igraphic.getShape().setInterpolation(interpolation)

        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            igraphic.setAntiAlias(antialias)

        if visible:
            zorder = kwargs.pop('zorder', None)
            if zorder is None:
                zorder = 0
            igraphic = self.add_graphic(igraphic, zorder=zorder)
            self._axes.setDrawExtent(igraphic.getExtent().clone())
            self._axes.setExtent(igraphic.getExtent().clone())
            gridline = self._axes.getGridLine()
            gridline.setTop(True)

        return igraphic
        
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
        proj = kwargs.pop('proj', migeo.projinfo())
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
        ls = ls.convertTo(ShapeTypes.POLYGON)
        if not kwargs.has_key('edgecolor'):
            kwargs['edgecolor'] = None
        plotutil.setlegendscheme(ls, **kwargs)
            
        if proj is None or proj.isLonLat():
            lonlim = 90
        else:
            lonlim = 0
            #x, y = np.project(x, y, toproj=proj)

        graphics = GraphicFactory.createPColorPolygons(x.asarray(), y.asarray(), a.asarray(), ls)
        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            graphics.setAntiAlias(antialias)

        visible = kwargs.pop('visible', True)
        if visible:
            zorder = kwargs.pop('zorder', None)
            graphics = self.add_graphic(graphics, projection=proj, zorder=zorder)
            self._axes.setExtent(graphics.getExtent())
            self._axes.setDrawExtent(graphics.getExtent())

        return graphics
        
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
        ls = ls.convertTo(ShapeTypes.POLYGON)
        plotutil.setlegendscheme(ls, **kwargs)

        graphics = GraphicFactory.createGridPolygons(x.asarray(), y.asarray(), a.asarray(), ls)
            
        # Add graphics
        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            graphics.setAntiAlias(antialias)

        proj = kwargs.pop('proj', migeo.projinfo())
        visible = kwargs.pop('visible', True)
        if visible:
            zorder = kwargs.pop('zorder', None)
            if zorder is None:
                zorder = 0
            graphics = self.add_graphic(graphics, projection=proj, zorder=zorder)
            self._axes.setDrawExtent(graphics.getExtent().clone())
            self._axes.setExtent(graphics.getExtent().clone())

        return graphics
    
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
        proj = kwargs.pop('proj', migeo.projinfo())
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
                levs = kwargs.pop('levels', levs)
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

        ls = plotutil.setlegendscheme_arrow(ls, **kwargs)
        if not cdata is None:
            cdata = cdata._array
        if u.ndim == 2 and x.ndim == 1:
            x, y = np.meshgrid(x, y)

        graphics = GraphicFactory.createArrows(x._array, y._array, u._array, v._array, cdata, ls, isuv)
            
        # Add graphics
        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            graphics.setAntiAlias(antialias)

        visible = kwargs.pop('visible', True)
        if visible:
            zorder = kwargs.pop('zorder', None)
            rGraphics = self.add_graphic(graphics, projection=proj, zorder=zorder)
            self._axes.setDrawExtent(rGraphics.getExtent().clone())
            self._axes.setExtent(rGraphics.getExtent().clone())

        return graphics
    
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
        :param size: (*float*) Base size of the arrows. Default is 10.
        :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
        :param zorder: (*int*) Z-order of created layer for display.
        :param select: (*boolean*) Set the return layer as selected layer or not.
        
        :returns: (*VectoryLayer*) Created barbs VectoryLayer.
        """
        cmap = plotutil.getcolormap(**kwargs)
        fill_value = kwargs.pop('fill_value', -9999.0)
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
            if u.ndim == 2 and x.ndim == 1:
                x, y = np.meshgrid(x, y)
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
                levs = kwargs.pop('levels', levs)
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
            cdata = cdata._array

        graphics = GraphicFactory.createBarbs(x._array, y._array, u._array, v._array, cdata, ls, isuv)
            
        # Add graphics
        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            graphics.setAntiAlias(antialias)

        proj = kwargs.pop('proj', migeo.projinfo())
        visible = kwargs.pop('visible', True)
        if visible:
            zorder = kwargs.pop('zorder', None)
            graphics = self.add_graphic(graphics, projection=proj, zorder=zorder)
            self._axes.setDrawExtent(graphics.getExtent().clone())
            self._axes.setExtent(graphics.getExtent().clone())

        return graphics
        
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
        proj = kwargs.pop('proj', migeo.projinfo())
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

        if not kwargs.has_key('headwidth'):
            kwargs['headwidth'] = 8
        if not kwargs.has_key('overhang'):
            kwargs['overhang'] = 0.5

        cdata = kwargs.pop('cdata', None)
        if cdata is None:
            lb, isunique = plotutil.getlegendbreak('line', **kwargs)
            lb = plotutil.line2stream(lb, **kwargs)
            graphics = GraphicFactory.createStreamlines(x._array, y._array, u._array, v._array,
                                                        density, lb, isuv)
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
            ls = plotutil.setlegendscheme_line(ls, **kwargs)
            for i in range(ls.getBreakNum()):
                lb = plotutil.line2stream(ls.getLegendBreak(i), **kwargs)
                ls.setLegendBreak(i, lb)

            graphics = GraphicFactory.createStreamlines(x._array, y._array, u._array, v._array,
                                                        cdata._array, density, ls, isuv)
            
        # Add graphics
        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            graphics.setAntiAlias(antialias)

        visible = kwargs.pop('visible', True)
        if visible:
            zorder = kwargs.pop('zorder', None)
            graphics = self.add_graphic(graphics, projection=proj, zorder=zorder)
            self._axes.setDrawExtent(graphics.getExtent().clone())
            self._axes.setExtent(graphics.getExtent().clone())
            
        return graphics
        
    def stationmodel(self, smdata, **kwargs):
        """
        Plot station model data on the map.
        
        :param smdata: (*StationModelData*) Station model data.
        :param surface: (*boolean*) Is surface data or not. Default is True.
        :param size: (*float*) Size of the station model symbols. Default is 12.
        :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
        :param order: (*int*) Z-order of created layer for display.
        
        :returns: (*graphics*) Station model graphics.
        """
        proj = kwargs.pop('proj', migeo.projinfo())
        size = kwargs.pop('size', 12)
        surface = kwargs.pop('surface', True)
        color = kwargs.pop('color', 'b')
        color = plotutil.getcolor(color)
        ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POINT, color, size)
        graphics = GraphicFactory.createStationModel(smdata, ls, surface)
     
        # Add graphics
        antialias = kwargs.pop('antialias', None)
        if antialias is not None:
            graphics.setAntiAlias(antialias)

        visible = kwargs.pop('visible', True)
        if visible:
            zorder = kwargs.pop('zorder', None)
            graphics = self.add_graphic(graphics, projection=proj, zorder=zorder)
            self._axes.setDrawExtent(graphics.getExtent().clone())
            self._axes.setExtent(graphics.getExtent().clone())
            
        return graphics
        
    def webmap(self, provider='OpenStreetMap', zorder=0):
        """
        Add a new web map layer.
        
        :param provider: (*string*) Web map provider.
        :param zorder: (*int*) Layer order.
        
        :returns: Web map layer
        """
        graphic = WebMapImage()
        if isinstance(provider, TileFactoryInfo):
            tf = DefaultTileFactory(provider)
            graphic.setTileFactory(tf)
        elif isinstance(provider, basestring):
            provider = WebMapProvider.valueOf(provider)
            graphic.setWebMapProvider(provider)
        else:
            graphic.setWebMapProvider(provider)

        self.add_graphic(graphic, zorder=zorder)
        return graphic
        
    def masklayer(self, mask, graphics):
        """
        Mask layers.
        
        :param mask: (*layer or polygon graphic*) Mask object.
        :param graphics: (*list*) The graphics will be masked.
        """
        if isinstance(mask, (MILayer, GeoGraphicCollection)):
            mask = mask.get_graphics()

        for graphic in graphics:
            graphic.setClipGraphic(mask)
            
    def move_graphic(self, graphic, x=0, y=0, coordinates='screen'):
        """
        Move a graphic by screen coordinate.
        
        :param graphic: (*Graphic*) A graphic.
        :param x: (*float*) X shift for moving.
        :param y: (*float*) Y shift for moving.
        :param coordinates: (*string*) Coordinates of x/y ['screen' | 'data'].
        """
        mapview = self._axes.getMapView()
        mapview.moveGraphic(graphic, x, y, coordinates == 'screen')
