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
from org.meteoinfo.geometry.shape import ShapeTypes
from org.meteoinfo.geometry.graphic import Graphic, GraphicCollection
from org.meteoinfo.chart.jogl import GLPlot, GLForm, JOGLUtil, EarthGLPlot, MapGLPlot
from org.meteoinfo.math.interpolate import InterpolationMethod
from org.meteoinfo.image import ImageUtil
from org.meteoinfo.common import Extent3D
from org.meteoinfo.projection import GeoTransform
from javax.swing import WindowConstants
from java.awt import Font, Color
from java.awt.image import BufferedImage

import os
import numbers
import warnings

import plotutil
import colors
from ._axes3d import Axes3D
from mipylib.numeric.core import NDArray
from mipylib.dataset import DimArray
import mipylib.numeric as np
from mipylib import migl
from mipylib.geolib import migeo

__all__ = ['Axes3DGL', 'MapAxes3D', 'EarthAxes3D']


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
            # position = [0.13, 0.11, 0.71, 0.815]
            position = [0, 0, 1, 1]
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
            # self._axes = Plot3DGL()
            self._axes = GLPlot()
        else:
            self._axes = plot

    @property
    def axestype(self):
        return '3d'


    @property
    def rotation(self):
        """Get or set rotation angle"""
        return self._axes.getAngleY()


    @rotation.setter
    def rotation(self, val):
        self._axes.setAngleY(val)
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


    @property
    def elevation(self):
        """Get or set elevation angle"""
        return self._axes.getAngleX()


    @elevation.setter
    def elevation(self, val):
        self._axes.setAngleX(val)


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
        self._axes.getPitchAngle()

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


    @property
    def material(self):
        """Get or set material"""
        lighting = self._axes.getLighting()
        m = dict(ambient=lighting.getMaterialAmbient(),
                 diffuse=lighting.getMaterialDiffuse(),
                 specular=lighting.getMaterialSpecular(),
                 shininess=lighting.getMaterialShininess(),
                 emission=lighting.getMaterialEmission())
        return m


    @material.setter
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

    def fill(self, x, y, z, color=None, **kwargs):
        """
        Create filled 3D patches.

        :param x: (*array_like*) X coordinates for each vertex.
        :param y: (*array_like*) Y coordinates for each vertex.
        :param z: (*array_like*) Z coordinates for each vertex.
        :param color: (*Color*) Fill color.

        :return: Filled 3D patches.
        """
        x = plotutil.getplotdata(x)
        y = plotutil.getplotdata(y)
        z = plotutil.getplotdata(z)

        lb, isunique = plotutil.getlegendbreak('polygon', **kwargs)
        if color is None:
            graphics = GraphicFactory.createPolygons3D(x, y, z, lb)
        else:
            alpha = kwargs.pop('alpha', None)
            colors = plotutil.getcolors(color, alpha)
            if len(colors) == 1:
                lb.setColor(colors[0])
                graphics = GraphicFactory.createPolygons3D(x, y, z, lb)
            else:
                lbs = []
                for c in colors:
                    nlb = lb.clone()
                    nlb.setColor(c)
                    lbs.append(nlb)
                graphics = GraphicFactory.createPolygons3D(x, y, z, lbs)

        self.add_graphic(graphics)
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

    def bar(self, *args, **kwargs):
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
        ls = ls.convertTo(ShapeTypes.POLYGON)
        facecolor = kwargs.pop('facecolor', None)
        face_interp = None
        if not facecolor is None:
            face_interp = (facecolor == 'interp')
            if not face_interp:
                if not facecolor in ['flat', 'texturemap', 'none']:
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
        # ls = ls.convertTo(ShapeTypes.POLYGON, True)
        ls = ls.convertTo(ShapeTypes.POLYGON)
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

        ls = ls.convertTo(ShapeTypes.POLYGON)

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
                        image = cdata.getGraphicN(0).getShape().getImage()
                    else:
                        image = cdata.getShape().getImage()
                elif not facecolor in ['flat', 'none']:
                    facecolor = plotutil.getcolor(facecolor)
                    ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POLYGON, facecolor, 1)
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
        visible = kwargs.pop('visible', True)
        if visible:
            self.add_graphic(graphics)
        return graphics

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
            ls = ls.convertTo(ShapeTypes.POLYGON)
        else:
            facecolor = plotutil.getcolor(facecolor)
            ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POLYGON, facecolor, 1)

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

    def trisurf(self, T, x, y, z, normal=None, **kwargs):
        """
        Triangular surface plot.

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
        ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POLYGON, facecolor, 1)

        if not kwargs.has_key('edgecolor'):
            kwargs['edgecolor'] = None
        plotutil.setlegendscheme(ls, **kwargs)

        if normal is None:
            graphics = GraphicFactory.triSurface(T._array, x._array, y._array, z._array, ls)
        else:
            graphics = GraphicFactory.triSurface(T._array, x._array, y._array, z._array,
                                                 normal._array, ls)

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
        ls = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.POLYGON, facecolor, 1)

        if 'edgecolor' not in kwargs.keys():
            kwargs['edgecolor'] = None
        plotutil.setlegendscheme(ls, **kwargs)

        if normal is None:
            graphics = GraphicFactory.model(T._array, x._array, y._array, z._array, ls)
        else:
            graphics = GraphicFactory.model(T._array, x._array, y._array, z._array,
                                                 normal._array, ls)

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


class MapAxes3D(Axes3DGL):
    """
    Map 3D axes.
    """

    def __init__(self, *args, **kwargs):
        super(MapAxes3D, self).__init__(*args, **kwargs)

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


class EarthAxes3D(Axes3DGL):
    """
    Earth spherical 3D axes.
    """

    def __init__(self, *args, **kwargs):
        kwargs['aspect'] = 'equal'
        if 'facecolor' not in kwargs.keys():
            kwargs['facecolor'] = 'k'
        kwargs['clip_plane'] = False
        kwargs['axis'] = False
        if 'distance' not in kwargs.keys():
            kwargs['distance'] = 500
        super(EarthAxes3D, self).__init__(*args, **kwargs)

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
        lons = np.zeros([nlon, npoints])
        lats = np.zeros([nlon, npoints])
        alts = np.zeros([nlon, npoints]) + offset
        lat = np.linspace(-90., 90., npoints)
        idx = 0
        for i in np.arange(0., 360., lon_delta):
            lons[idx] = np.full(npoints, i)
            lats[idx] = lat
            idx += 1
        self.plot(lons, lats, alts, **kwargs)

        nlat = int(180. / lat_delta)
        lons = np.zeros([nlat, npoints])
        lats = np.zeros([nlat, npoints])
        alts = np.zeros([nlat, npoints]) + offset
        lon = np.linspace(-180., 180., npoints)
        idx = 0
        for i in np.arange(-90., 90., lat_delta):
            lats[idx] = np.full(npoints, i)
            lons[idx] = lon
            idx += 1
        self.plot(lons, lats, alts, **kwargs)

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
            extent = Extent3D(xmin, xmax, ymin, ymax, zmin, zmax)
            self._axes.setDrawExtent(extent)
            return True
        else:
            print('The limits parameter must be a list with 6 elements: xmin, xmax, ymin, ymax, zmin, zmax!')
            return None

####################################################
