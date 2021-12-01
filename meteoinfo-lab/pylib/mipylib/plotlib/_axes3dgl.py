# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2019-9-4
# Purpose: MeteoInfoLab axes3dgl module - using JOGL
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.chart.graphic import GraphicFactory
from org.meteoinfo.chart import AspectType
from org.meteoinfo.geometry.legend import BreakTypes, BarBreak
from org.meteoinfo.geo.legend import LegendManage
from org.meteoinfo.geo.layer import LayerTypes
from org.meteoinfo.geometry.shape import ShapeTypes
from org.meteoinfo.geometry.graphic import Graphic, GraphicCollection
from org.meteoinfo.chart.jogl import Plot3DGL, GLForm, JOGLUtil
from org.meteoinfo.math.interpolate import InterpolationMethod
from org.meteoinfo.image import ImageUtil
from javax.swing import WindowConstants
from java.awt import Font, Color
from java.awt.image import BufferedImage

import os
import numbers
import warnings

import plotutil
from ._axes3d import Axes3D
from mipylib.numeric.core import NDArray, DimArray
import mipylib.numeric as np
from mipylib import migl
from mipylib.geolib import migeo

__all__ = ['Axes3DGL']

class Axes3DGL(Axes3D):
    
    def __init__(self, *args, **kwargs):
        """
        Axes 3d with openGL support.
        :param position: (*list of float*) Axes position specified by *position=* [left, bottom, width
            height] in normalized (0, 1) units. Default is [0.13, 0.11, 0.775, 0.815].
        :param rotation: (*float*) Axes rotation angle around Z axis.
        :param elevation: (*float*) Axes elevation angle with Z axis.
        :param antialias: (*bool*) Antialias or not. Default is `None`.
        """
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
        bgcolor = kwargs.pop('bgcolor', None)
        if not bgcolor is None:
            bgcolor = plotutil.getcolor(bgcolor)
            self.axes.setBackground(bgcolor)
        units = kwargs.pop('units', None)
        if not units is None:
            self.axes.setUnits(units)
        tickfontname = kwargs.pop('tickfontname', 'Arial')
        tickfontsize = kwargs.pop('tickfontsize', 14)
        tickbold = kwargs.pop('tickbold', False)
        if tickbold:
            font = Font(tickfontname, Font.BOLD, tickfontsize)
        else:
            font = Font(tickfontname, Font.PLAIN, tickfontsize)
        self.axes.setAxisTickFont(font)
        rotation = kwargs.pop('rotation', None)
        if not rotation is None:
            self.axes.setAngleY(rotation)
        elevation = kwargs.pop('elevation', None)
        if not elevation is None:
            self.axes.setAngleX(elevation)
        antialias = kwargs.pop('antialias', None)
        if not antialias is None:
            self.axes.setAntialias(antialias)
        clip_plane = kwargs.pop('clip_plane', None)
        if not clip_plane is None:
            self.axes.setClipPlane(clip_plane)
        aspect = kwargs.pop('aspect', None)
        if not aspect is None:
            self.axes.setAspectType(AspectType.valueOf(aspect.upper()))
        axis = kwargs.pop('axis', True)
        if not axis:
            self.axes.setDrawBase(False)
            self.axes.setBoxed(False)
            self.axes.setDisplayXY(False)
            self.axes.setDisplayZ(False)
        
    def _set_plot(self, plot):
        '''
        Set plot.
        
        :param plot: (*Axes3D*) Plot.
        '''
        if plot is None:
            self.axes = Plot3DGL()
        else:
            self.axes = plot
    
    @property
    def axestype(self):
        return '3d'
        
    def get_rotation(self):
        '''
        Get rotation angle.
        
        :returns: Rotation angle.
        '''
        return self.axes.getAngleY()
        
    def set_rotation(self, rotation):
        '''
        Set rotation angle.
        
        :param rotation: (*float*) Rotation angle.
        '''
        self.axes.setAngleY(rotation)
        
    def get_elevation(self):
        '''
        Get elevation angle.
        
        :returns: Elevation angle.
        '''
        return self.axes.getAngleX()
        
    def set_elevation(self, elevation):
        '''
        Set elevation angle.
        
        :param elevation: (*float*) Elevation angle.
        '''
        self.axes.setAngleX(elevation)

    def set_background(self, color):
        '''
        Set background color.

        :param color: (*color*) Background color.
        '''
        color = plotutil.getcolor(color)
        self.axes.setBackground(color)

    def get_antialias(self):
        '''
        Get antialias
        :return: (*bool*) Antialias or not.
        '''
        return self.axes.isAntialias()

    def set_antialias(self, antialias):
        '''
        Set antialias
        :param antialias: (*bool*) Antialias or not.
        '''
        self.axes.setAntialias(antialias)
        
    def set_lighting(self, enable=True, **kwargs):
        '''
        Set lighting.
        
        :param enable: (*boolean*) Set lighting enable or not.
        :param position: (*list of float*) Lighting position.
        :param ambient: (*list of float*) Ambient light.
        :param diffuse: (*list of float*) Diffuse light.
        :param specular: (*list of float*) Specular light.
        :param mat_ambient: (*list of float*) Material ambient light.
        :param mat_diffuse: (*list of float*) Material diffuse light.
        :param mat_specular: (*list of float*) Material specular light.
        :param mat_emission: (*list of float*) Material emission light.
        :param mat_shininess: (*float*) Material shininess (0 - 128).
        '''
        lighting = self.axes.getLighting()
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
            lighting.setMat_Ambient(mat_ambient)
        mat_diffuse = kwargs.pop('mat_diffuse', None)
        if not mat_diffuse is None:
            lighting.setMat_Diffuse(mat_diffuse)
        mat_specular = kwargs.pop('mat_specular', None)
        if not mat_specular is None:
            lighting.setMat_Specular(mat_specular)
        mat_emission = kwargs.pop('mat_emission', None)
        if not mat_emission is None:
            lighting.setMat_Emission(mat_emission)
        mat_shininess = kwargs.pop('mat_shininess', None)
        if not mat_shininess is None:
            lighting.setMat_Shininess(mat_shininess)

    def bar(self, x, y, z, width=0.8, bottom=None, cylinder=False, **kwargs):
        """
        Make a 3D bar plot of x, y and z, where x, y and z are sequence like objects of the same lengths.

        :param x: (*array_like*) Input x data.
        :param y: (*array_like*) Input y data.
        :param z: (*array_like*) Input z data.
        :param width: (*float*) Bar width.
        :param cylinder: (*bool*) Is cylinder bar or rectangle bar.
        :param bottom: (*bool*) Color of the points. Or z values.
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

        :returns: Bar 3D graphics.
        """
        #Add data series
        label = kwargs.pop('label', 'S_0')
        xdata = plotutil.getplotdata(x)
        ydata = plotutil.getplotdata(y)
        zdata = plotutil.getplotdata(z)

        autowidth = False
        width = np.asarray(width)

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
        if isinstance(width, NDArray):
            width = width.asarray()
        if cylinder:
            graphics = GraphicFactory.createCylinderBars3D(xdata, ydata, zdata, autowidth, width, bottom, barbreaks)
        else:
            graphics = GraphicFactory.createBars3D(xdata, ydata, zdata, autowidth, width, bottom, barbreaks)

        self.add_graphic(graphics)

        return barbreaks

    def streamplot(self, *args, **kwargs):
        """
        Plot stream lines in 3D axes.

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
                ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POLYLINE, c, 1)
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

        #Pipe
        pipe = kwargs.pop('pipe', False)
        if pipe:
            radius = kwargs.pop('radius', 0.02)
            steps = kwargs.pop('steps', 48)
            graphics = GraphicFactory.lineString3DToPipe(graphics, radius, steps)

        self.add_graphic(graphics)

        return graphics

    def streamslice(self, *args, **kwargs):
        """
        Plot stream lines slice in 3D axes.

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
                ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POLYLINE, c, 1)
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

    def geoshow(self, layer, **kwargs):
        '''
        Plot a layer in 3D axes.

        :param layer: (*MILayer*) The layer to be plotted.

        :returns: Graphics.
        '''
        ls = kwargs.pop('symbolspec', None)
        offset = kwargs.pop('offset', 0)
        xshift = kwargs.pop('xshift', 0)

        if isinstance(layer, basestring):
            fn = layer
            if not fn.endswith('.shp'):
                fn = fn + '.shp'
            if not os.path.exists(fn):
                fn = os.path.join(migl.get_map_folder(), fn)
            if os.path.exists(fn):
                encoding = kwargs.pop('encoding', None)
                layer = migeo.shaperead(fn, encoding)
            else:
                raise IOError('File not exists: ' + fn)

        layer = layer.layer
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
            graphics = GraphicFactory.createGraphicsFromLayer(layer, offset, xshift)
        else:
            interpolation = kwargs.pop('interpolation', None)
            graphics = JOGLUtil.createTexture(layer, offset, xshift, interpolation)
            #graphics = GraphicFactory.createImage(layer, offset, xshift, interpolation)

        lighting = kwargs.pop('lighting', None)
        if not lighting is None:
            graphics.setUsingLight(lighting)

        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics
        
    def plot_layer(self, layer, **kwargs):
        '''
        Plot a layer in 3D axes.
        
        :param layer: (*MILayer*) The layer to be plotted.
        
        :returns: Graphics.
        '''
        return self.geoshow(layer, **kwargs)

    def slice(self, *args, **kwargs):
        '''
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
        '''
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
            x = x[0,0]
        if y.ndim == 3:
            y = y[0,:,0]
        if z.ndim == 3:
            z = z[:,0,0]

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
        ls = ls.convertTo(ShapeTypes.POLYGON)
        facecolor = kwargs.pop('facecolor', None)
        face_interp = None
        if not facecolor is None:
            face_interp = (facecolor == 'interp')
            if not face_interp:
                if not facecolor in ['flat','texturemap','none']:
                    facecolor = plotutil.getcolor(facecolor)
                    ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POLYGON, facecolor, 1)
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
            graphics = JOGLUtil.slice(data.asarray(), x.asarray(), y.asarray(), z.asarray(),
                                      xslice._array, yslice._array, zslice._array, ls)
        else:
            graphics = JOGLUtil.slice(data.asarray(), x.asarray(), y.asarray(), z.asarray(), xslice, \
                                      yslice, zslice, ls)

        xyslice = kwargs.pop('xyslice', None)
        if not xyslice is None:
            method = kwargs.pop('method', 'nearest')
            method = InterpolationMethod.valueOf(method.upper())
            gg = JOGLUtil.slice(data.asarray(), x.asarray(), y.asarray(), z.asarray(),
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
        '''
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
        '''
        warnings.warn("plot_slice is deprecated", DeprecationWarning)
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
            x = x[0,0]
        if y.ndim == 3:
            y = y[0,:,0]
        if z.ndim == 3:
            z = z[:,0,0]

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
        ls = ls.convertTo(ShapeTypes.POLYGON)
        edge = kwargs.pop('edge', True)
        kwargs['edge'] = edge
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
        graphics = JOGLUtil.slice(data.asarray(), x.asarray(), y.asarray(), z.asarray(), xslice, \
                                  yslice, zslice, ls)
        visible = kwargs.pop('visible', True)
        if visible:
            for gg in graphics:
                self.add_graphic(gg)
        return graphics

    def contourslice(self, *args, **kwargs):
        '''
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
        '''
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
            x = x[0,0]
        if y.ndim == 3:
            y = y[0,:,0]
        if z.ndim == 3:
            z = z[:,0,0]

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
        ls = ls.convertTo(ShapeTypes.POLYLINE)
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
        graphics = GraphicFactory.contourSlice(data.asarray(), x.asarray(), y.asarray(), z.asarray(), xslice, \
                                  yslice, zslice, ls, smooth)

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
        '''
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
        '''
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
            x = x[0,0]
        if y.ndim == 3:
            y = y[0,:,0]
        if z.ndim == 3:
            z = z[:,0,0]

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
        ls = ls.convertTo(ShapeTypes.POLYGON)
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

    def mesh(self, *args, **kwargs):
        '''
        creates a three-dimensional surface mesh plot

        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) 2-D z value array.
        :param cmap: (*string*) Color map string.

        :returns: Legend
        '''
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

        if kwargs.has_key('colors'):
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
        ls = ls.convertTo(ShapeTypes.POLYGON, True)
        face_interp = None
        if kwargs.has_key('facecolor'):
            facecolor = kwargs.pop('facecolor', None)
            face_interp = (facecolor == 'interp')
            if not face_interp:
                if not facecolor in ['flat','texturemap','none']:
                    kwargs['facecolor'] = facecolor
        else:
            kwargs['facecolor'] = 'w'
        edgecolor = kwargs.pop('edgecolor', None)
        edge_interp = None
        if not edgecolor is None:
            edge_interp = (edgecolor == 'interp')
            if not edge_interp:
                if not edgecolor in ['flat','texturemap','none']:
                    kwargs['edgecolor'] = edgecolor
        plotutil.setlegendscheme(ls, **kwargs)
        graphics = JOGLUtil.surface(x.asarray(), y.asarray(), z.asarray(), ls)
        graphics.setMesh(True)
        if face_interp:
            graphics.setFaceInterp(face_interp)
        if edge_interp:
            graphics.setEdgeInterp(edge_interp)
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics

    def surf(self, *args, **kwargs):
        '''
        creates a three-dimensional surface plot

        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) 2-D z value array.
        :param cmap: (*string*) Color map string.
        :param lighting: (*bool*) Using light or not.

        :returns: 3D surface graphic
        '''
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
        cmap = plotutil.getcolormap(**kwargs)
        level_arg = None
        C = None
        min = z.min()
        max = z.max()
        if len(args) > 0:
            if isinstance(args[0], NDArray) and args[0].shape == z.shape:
                C = args[0]
                min = C.min()
                max = C.max()
                if len(args) > 1:
                    level_arg = args[1]
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

        ls = ls.convertTo(ShapeTypes.POLYGON)

        face_interp = None
        image = None
        if not facecolor is None:
            face_interp = (facecolor == 'interp')
            if not face_interp:
                if facecolor == 'texturemap':
                    if isinstance(cdata, NDArray):
                        if cdata.ndim == 3:
                            image = ImageUtil.createImage(cdata._array)
                        else:
                            image = GraphicFactory.createImage(cdata._array, ls)
                    elif isinstance(cdata, BufferedImage):
                        image = cdata
                    elif isinstance(cdata, GraphicCollection):
                        image = cdata.getGraphicN(0).getShape().getImage()
                    else:
                        image = cdata.getShape().getImage()
                elif not facecolor in ['flat','none']:
                    facecolor = plotutil.getcolor(facecolor)
                    ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POLYGON, facecolor, 1)
        plotutil.setlegendscheme(ls, **kwargs)
        if C is None:
            graphics = JOGLUtil.surface(x.asarray(), y.asarray(), z.asarray(), ls)
        else:
            graphics = JOGLUtil.surface(x.asarray(), y.asarray(), z.asarray(), C.asarray(), ls)

        if not image is None:
            graphics.setImage(image)
        if face_interp:
            graphics.setFaceInterp(face_interp)
        lighting = kwargs.pop('lighting', None)
        if not lighting is None:
            graphics.setUsingLight(lighting)
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
        
        :returns: Legend
        '''
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

        if kwargs.has_key('colors'):
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
        ls = ls.convertTo(ShapeTypes.POLYGON)
        facecolor = kwargs.pop('facecolor', None)
        face_interp = None
        if not facecolor is None:
            face_interp = (facecolor == 'interp')
            if not face_interp:
                if not facecolor in ['flat','texturemap','none']:
                    kwargs['facecolor'] = facecolor
        plotutil.setlegendscheme(ls, **kwargs)
        graphics = JOGLUtil.surface(x.asarray(), y.asarray(), z.asarray(), ls)
        if face_interp:
            graphics.setFaceInterp(face_interp)
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics

    def isosurface(self, *args, **kwargs):
        '''
        creates a three-dimensional isosurface plot

        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) Optional. Z coordinate array.
        :param data: (*array_like*) 3D data array.
        :param cmap: (*string*) Color map string.
        :param nthread: (*int*) Thread number.

        :returns: Legend
        '''
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
            data = args[3]
            isovalue = args[4]
            args = args[5:]
        cmap = plotutil.getcolormap(**kwargs)
        cvalue = kwargs.pop('cvalue', None)
        if not cvalue is None:
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
            ls = ls.convertTo(ShapeTypes.POLYGON)
            edge = kwargs.pop('edge', True)
            kwargs['edge'] = edge
            plotutil.setlegendscheme(ls, **kwargs)
        else:
            ls = plotutil.getlegendbreak('polygon', **kwargs)[0]
        nthread = kwargs.pop('nthread', None)
        if nthread is None:
            graphics = JOGLUtil.isosurface(data.asarray(), x.asarray(), y.asarray(), z.asarray(), isovalue, ls)
        else:
            data = data.asarray().copyIfView()
            x = x.asarray().copyIfView()
            y = y.asarray().copyIfView()
            z = z.asarray().copyIfView()
            graphics = JOGLUtil.isosurface(data, x, y, z, isovalue, ls, nthread)
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics

    def plot_isosurface(self, *args, **kwargs):
        '''
        creates a three-dimensional isosurface plot

        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) Optional. Z coordinate array.
        :param data: (*array_like*) 3D data array.
        :param cmap: (*string*) Color map string.
        :param nthread: (*int*) Thread number.

        :returns: Legend
        '''
        warnings.warn("plot_isosurface is deprecated", DeprecationWarning)
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
            data = args[3]
            isovalue = args[4]
            args = args[5:]
        cmap = plotutil.getcolormap(**kwargs)
        cvalue = kwargs.pop('cvalue', None)
        if not cvalue is None:
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
            ls = ls.convertTo(ShapeTypes.POLYGON)
            edge = kwargs.pop('edge', True)
            kwargs['edge'] = edge
            plotutil.setlegendscheme(ls, **kwargs)
        else:
            ls = plotutil.getlegendbreak('polygon', **kwargs)[0]
        nthread = kwargs.pop('nthread', None)
        if nthread is None:
            graphics = JOGLUtil.isosurface(data.asarray(), x.asarray(), y.asarray(), z.asarray(), isovalue, ls)
        else:
            data = data.asarray().copyIfView()
            x = x.asarray().copyIfView()
            y = y.asarray().copyIfView()
            z = z.asarray().copyIfView()
            graphics = JOGLUtil.isosurface(data, x, y, z, isovalue, ls, nthread)
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics

    def particles(self, *args, **kwargs):
        '''
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
        '''
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
        graphics = JOGLUtil.particles(data.asarray(), x.asarray(), y.asarray(), z.asarray(), ls, \
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
        '''
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
        '''
        warnings.warn("plot_particles is deprecated", DeprecationWarning)
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
        graphics = JOGLUtil.particles(data.asarray(), x.asarray(), y.asarray(), z.asarray(), ls, \
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

    def volumeplot(self, *args, **kwargs):
        '''
        creates a three-dimensional volume plot

        :param x: (*array_like*) Optional. X coordinate array.
        :param y: (*array_like*) Optional. Y coordinate array.
        :param z: (*array_like*) Optional. Z coordinate array.
        :param data: (*array_like*) 3D data array.
        :param cmap: (*string*) Color map string.
        :param vmin: (*float*) Minimum value for particle plotting.
        :param vmax: (*float*) Maximum value for particle plotting.
        :param alpha_min: (*float*) Minimum alpha value.
        :param alpha_max: (*float*) Maximum alpha value.

        :returns: Volumeplot graphic
        '''
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
            graphics = JOGLUtil.volume(data.asarray(), x.asarray(), y.asarray(), z.asarray(), ls, \
                                       alpha_min, alpha_max)
        else:
            graphics = JOGLUtil.volume(data.asarray(), x.asarray(), y.asarray(), z.asarray(), cmap, \
                                       vmin, vmax, alpha_min, alpha_max)

        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics

    def view(self):
        '''
        Open GLForm
        '''
        form = GLForm(self.axes)
        form.setSize(600, 500)
        form.setLocationRelativeTo(None)
        form.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
        form.setVisible(True)
        
####################################################