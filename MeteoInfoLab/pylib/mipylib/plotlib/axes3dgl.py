# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2019-9-4
# Purpose: MeteoInfoLab axes3dgl module - using JOGL
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.chart.plot import GraphicFactory
from org.meteoinfo.legend import BreakTypes
from org.meteoinfo.layer import LayerTypes
from org.meteoinfo.chart.jogl import Plot3DGL, GLForm, JOGLUtil
from javax.swing import WindowConstants
from java.awt import Font, Color, BasicStroke

import plotutil
from axes3d import Axes3D

class Axes3DGL(Axes3D):
    
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
            self.axes.setUnits(units)
        tickfontname = kwargs.pop('tickfontname', 'Arial')
        tickfontsize = kwargs.pop('tickfontsize', 14)
        tickbold = kwargs.pop('tickbold', False)
        if tickbold:
            font = Font(tickfontname, Font.BOLD, tickfontsize)
        else:
            font = Font(tickfontname, Font.PLAIN, tickfontsize)
        self.axes.setAxisTickFont(font)
        
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
            graphics = JOGLUtil.createTexture(self.figure.getGL2(), layer, offset, xshift, interpolation)
        
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