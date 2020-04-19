# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2019-9-7
# Purpose: MeteoInfoLab glfigure module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.chart.jogl import GLChartPanel
from com.jogamp.opengl import GLProfile, GLCapabilities

from ._axes3dgl import Axes3DGL

__all__ = ['GLFigure']

class GLFigure(GLChartPanel):

    def __init__(self, ax=None):
        '''
        Constructor
        
        :param ax: (*Axes3DGL*) 3D axes with JOGL
        '''
        if ax is None:
            ax = Axes3DGL(figure=self)
        self.axes = ax
        #super(GLFigure, self).__init__(ax.axes)
        profile = GLProfile.get(GLProfile.GL2)
        cap = GLCapabilities(profile)
        cap.setDoubleBuffered(True)
        cap.setSampleBuffers(True)
        cap.setNumSamples(4)

        super(GLFigure, self).__init__(cap, ax.axes)
        
    def _add_axes(self, ax):
        '''
        Add a axes.
        
        :param ax: (*Axes*) The axes.
        '''
        self.axes.append(ax)
        self.getChart().addPlot(ax.axes)

    def set_antialias(self, b=None, symbol=None):
        """
        Set figure antialias or not.

        :param b: (*boolean*) Antialias or not.
        :param symbol: (*boolean*) Set symbol antialias or not.
        """
        if b is None:
            b = not self.axes.get_antialias()
        self.axes.set_antialias(b)