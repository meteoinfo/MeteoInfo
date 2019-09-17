# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2019-9-7
# Purpose: MeteoInfoLab glfigure module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.chart.jogl import GLChartPanel

from axes3dgl import Axes3DGL

class GLFigure(GLChartPanel):

    def __init__(self, ax=None):
        '''
        Constructor
        
        :param ax: (*Axes3DGL*) 3D axes with JOGL
        '''
        if ax is None:
            ax = Axes3DGL(figure=self)
        self.axes = ax
        super(GLFigure, self).__init__(ax.axes)           
        
    def _add_axes(self, ax):
        '''
        Add a axes.
        
        :param ax: (*Axes*) The axes.
        '''
        self.axes.append(ax)
        self.getChart().addPlot(ax.axes)
        
        
############################################