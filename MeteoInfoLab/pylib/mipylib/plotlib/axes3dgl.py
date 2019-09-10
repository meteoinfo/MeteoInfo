# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2019-9-4
# Purpose: MeteoInfoLab axes3dgl module - using JOGL
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.chart.plot import GraphicFactory
from org.meteoinfo.chart.jogl import Plot3DGL, GLForm
from javax.swing import WindowConstants

import plotutil
from axes3d import Axes3D

class Axes3DGL(Axes3D):
    
    def __init__(self):
        self.axes = Plot3DGL()
        self.axestype = '3d'
        
    def add_graphic(self, graphic):
        '''
        Add a graphic
        
        :param graphic: (*Graphic*) The graphic to be added.
        '''
        self.axes.addGraphic(graphic)

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