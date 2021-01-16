# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2014-12-26
# Purpose: MeteoInfoLab plot module
# Note: Jython
#-----------------------------------------------------
import datetime
import math
import mipylib.migl as migl
import mipylib.miutil as miutil
import mipylib.numeric as np
import os
import plotutil
from javax.swing import WindowConstants
from mipylib.numeric.core import NDArray, DimArray

from org.meteoinfo.chart import Location
from org.meteoinfo.chart.plot import Plot2D, MapPlot, Plot3D
from org.meteoinfo.data.meteodata import DrawMeteoData
from org.meteoinfo.image import AnimatedGifEncoder
from org.meteoinfo.legend import LegendManage, LegendScheme, LegendType
from org.meteoinfo.script import ChartForm
from org.meteoinfo.shape import ShapeTypes
from ._axes import Axes, PolarAxes
from ._axes3d import Axes3D
from ._axes3dgl import Axes3DGL
from ._figure import Figure
from ._glfigure import GLFigure
from ._mapaxes import MapAxes

## Global ##
batchmode = False
isinteractive = False
g_figure = None
g_axes = None

__all__ = [
    'gca','annotate','antialias','arrow','arrowline','axes','axes3d','axes3dgl','axesm','caxes','axis',
    'axism','bar','bar3','barh','barbs','barbsm','bgcolor','box',
    'boxplot','windrose','cla','clabel','clc','clear','clf','cll','cloudspec','colorbar','contour','contourf',
    'contourfm','contourm','delfig','draw','draw_if_interactive','errorbar',
    'figure','glfigure','figsize','patch','rectangle','fill_between','fill_betweenx','webmap','gc_collect','geoshow',
    'get_figure','gifaddframe','gifanimation','giffinish',
    'grid','gridshow','gridshowm','hist','imshow','imshowm','isosurface','legend','left_title','lighting','loglog','makecolors',
    'makelegend','makesymbolspec','masklayer','mesh','particles','pcolor','pcolorm','pie','plot','plot3','plotm','quiver','quiver3',
    'quiverkey','quiverm','readlegend','right_title','savefig','savefig_jpeg','scatter','scatter3','scatterm',
    'semilogx','semilogy','set','show','slice3','stationmodel','stem','stem3','step','streamplot','streamplotm','subplot','subplots','suptitle',
    'surf','taylor_diagram','text','text3','title','twinx','twiny','violinplot','weatherspec','xaxis',
    'xlabel','xlim','xreverse','xticks','yaxis','ylabel','ylim','yreverse','yticks','zaxis','zlabel','zlim','zticks',
    'isinteractive'
    ]

def gca():
    '''
    Get current axes
    :return: Current axes
    '''
    return g_axes
        
def figsize():
    '''
    Get current figure size.
    
    :returns: Figure width and height.    
    '''
    if g_figure is None:
        return None
    else:    
        width = g_figure.getFigureWidth()
        height = g_figure.getFigureHeight()
        return width, height

def draw_if_interactive():
    '''
    Draw current figure if is interactive model.
    '''
    if isinteractive:
		g_figure.paintGraphics()
        
def draw():
    '''
    Draw the current figure.
    '''
    g_figure.paintGraphics()
    
def plot(*args, **kwargs):
    """
    Plot lines and/or markers to the axes. *args* is a variable length argument, allowing
    for multiple *x, y* pairs with an optional format string.
    
    :param x: (*array_like*) Input x data.
    :param y: (*array_like*) Input y data.
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
      'S'         star marker
      'p'         pentagon marker
      '*'         star line marker
      'x'         x cross marker
      'D'         diamond marker
      'm'         minus marker
      '+'         plus marker
      'os'        circle star marker
      'do'        double circle
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
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.ndim == 3:
            g_axes = axes()
            
    r = g_axes.plot(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r    

def step(x, y, *args, **kwargs):
    '''
    Make a step plot.
    
    :param x: (*array_like*) Input x data.
    :param y: (*array_like*) Input y data.
    :param style: (*string*) Line style for plot.
    :param label: (*string*) Step line label.
    :param where: (*string*) ['pre' | 'post' | 'mid']. If 'pre' (the default), the interval 
        from x[i] to x[i+1] has level y[i+1]. If 'post', that interval has level y[i].
        If ‘mid’, the jumps in y occur half-way between the x-values.
    
    :returns: Step lines
    '''    
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian' and g_axes.axestype != 'polar':
            g_axes = axes()
            
    r = g_axes.step(x, y, *args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r 
        
def plot3(x, y, z, *args, **kwargs):
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
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3D):
            g_axes = axes3d()
    
    r = g_axes.plot(x, y, z, *args, **kwargs)
    draw_if_interactive()
    return r
        
def semilogy(*args, **kwargs):
    """
    Make a plot with log scaling on the y axis.
    
    :param x: (*array_like*) Input x data.
    :param y: (*array_like*) Input y data.
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
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()
            
    r = g_axes.semilogy(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r 
    
def semilogx(*args, **kwargs):
    """
    Make a plot with log scaling on the x axis.
    
    :param x: (*array_like*) Input x data.
    :param y: (*array_like*) Input y data.
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
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()
            
    r = g_axes.semilogx(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r 
    
def loglog(*args, **kwargs):
    """
    Make a plot with log scaling on both x and y axis.
    
    :param x: (*array_like*) Input x data.
    :param y: (*array_like*) Input y data.
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
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()
            
    r = g_axes.loglog(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r    
        
def errorbar(x, y, yerr=None, xerr=None, fmt='', ecolor=None, elinewidth=None, capsize=None,
            **kwargs):
    '''
    Plot an errorbar graph.
    
    :param x: (*array_like*) X data.
    :param y: (*array_like*) Y data.
    :param yerr: (*scalar or array_like*) Y error values.
    :param xerr: (*scalar or array_like*) X error values.
    :param fmt: (*string*) Plot format string.
    :param ecolor: (*color*) Error bar color.
    :param elinewidth: (*float*) Error bar line width.
    :param capsize: (*float*) The length of the error bar caps.

    :returns: Error bar lines.
    '''
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian' and g_axes.axestype != 'polar':
            g_axes = axes()
            
    r = g_axes.errorbar(x, y, yerr, xerr, fmt, ecolor, elinewidth, capsize, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r    

def bar(x, height, width=0.8, bottom=None, align='center', data=None, **kwargs):
    """
    Make a bar plot.
    
    Make a bar plot with rectangles bounded by:
        left, left + width, bottom, bottom + height
    
    :param left: (*array_like*) The x coordinates of the left sides of the bars.
    :param height: (*array_like*) The height of the bars.
    :param width: (*array_like*) Optional, the widths of the bars default: 0.8.
    :param bottom: (*array_like*) Optional, the y coordinates of the bars default: None
    :param color: (*Color*) Optional, the color of the bar faces.
    :param edgecolor: (*Color*) Optional, the color of the bar edge. Default is black color.
        Edge line will not be plotted if ``edgecolor`` is ``None``.
    :param linewidth: (*int*) Optional, width of bar edge.
    :param label: (*string*) Label of the bar series.
    :param hatch: (*string*) Hatch string.
    :param hatchsize: (*int*) Hatch size. Default is None (8).
    :param bgcolor: (*Color*) Background color, only valid with hatch.
    :param barswidth: (*float*) Bars width (0 - 1), only used for automatic bar with plot
        (only one argument widthout ``width`` augument). Defaul is 0.8.
    :param morepoints: (*boolean*) More points in bar rectangle. Defaul is False.
    
    :returns: Bar legend break.
    
    
    The following format string characters are accepted to control the hatch style:
      =========  ===========
      Character  Description
      =========  ===========
      '-'         horizontal hatch style
      '|'         vertical hatch style
      '\\'        forward_diagonal hatch style
      '/'         backward_diagonal hatch style
      '+'         cross hatch style
      'x'         diagonal_cross hatch style
      '.'         dot hatch style
      =========  ===========
      
    """
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian' and g_axes.axestype != 'polar':
            g_axes = axes()
            
    r = g_axes.bar(x, height, width, bottom, align, data, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r

def bar3(x, y, z, width=0.8, bottom=None, cylinder=False, **kwargs):
    """
    Make a 3D bar plot of x, y and z, where x, y and z are sequence like objects of the same lengths.

    :param x: (*array_like*) Input x data.
    :param y: (*array_like*) Input y data.
    :param z: (*array_like*) Input z data.
    :param width: (*float*) Bar width.
    :param cylinder: (*bool*) Is sylinder bar or rectangle bar.
    :param bottom: (*bool*) Color of the points. Or z vlaues.
    :param color: (*Color*) Optional, the color of the bar faces.
    :param edgecolor: (*Color*) Optional, the color of the bar edge. Default is black color.
        Edge line will not be plotted if ``edgecolor`` is ``None``.
    :param linewidth: (*int*) Optional, width of bar edge.
    :param label: (*string*) Label of the bar series.
    :param hatch: (*string*) Hatch string.
    :param hatchsize: (*int*) Hatch size. Default is None (8).
    :param bgcolor: (*Color*) Background color, only valid with hatch.
    :param barswidth: (*float*) Bars width (0 - 1), only used for automatic bar with plot
        (only one argument widthout ``width`` augument). Defaul is 0.8.

    :returns: Points legend break.
    """
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3D):
            g_axes = axes3d()

    r = g_axes.bar(x, y, z, width, bottom, cylinder, **kwargs)
    draw_if_interactive()
    return r

def barh(*args, **kwargs):
    """
    Make a horizontal bar plot.
    
    Make a bar plot with rectangles bounded by:
        left, left + width, y, y + height
    
    :param y: (*array_like*) The y coordinates of the bars.
    :param width: (*array_like*) The widths of the bars.
    :param height: (*array_like*) Optional, the height of the bars default: 0.8.
    :param left: (*array_like*) Optional, the x coordinates of the bars default: None
    :param color: (*Color*) Optional, the color of the bar faces.
    :param edgecolor: (*Color*) Optional, the color of the bar edge. Default is black color.
        Edge line will not be plotted if ``edgecolor`` is ``None``.
    :param linewidth: (*int*) Optional, width of bar edge.
    :param label: (*string*) Label of the bar series.
    :param hatch: (*string*) Hatch string.
    :param hatchsize: (*int*) Hatch size. Default is None (8).
    :param bgcolor: (*Color*) Background color, only valid with hatch.
    :param barswidth: (*float*) Bars width (0 - 1), only used for automatic bar with plot
        (only one argument widthout ``width`` augument). Defaul is 0.8.
    :param morepoints: (*boolean*) More points in bar rectangle. Defaul is False.
    
    :returns: Bar legend break.
    
    
    The following format string characters are accepted to control the hatch style:
      =========  ===========
      Character  Description
      =========  ===========
      '-'         horizontal hatch style
      '|'         vertical hatch style
      '\\'        forward_diagonal hatch style
      '/'         backward_diagonal hatch style
      '+'         cross hatch style
      'x'         diagonal_cross hatch style
      '.'         dot hatch style
      =========  ===========
      
    """
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian' and g_axes.axestype != 'polar':
            g_axes = axes()
            
    r = g_axes.barh(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r
          
def hist(x, bins=10, range=None, density=False, cumulative=False,
        bottom=None, histtype='bar', align='mid',
        orientation='vertical', rwidth=None, log=False, **kwargs):
    """
    Plot a histogram.
    
    :param x: (*array_like*) Input values, this takes either a single array or a sequency of arrays 
        which are not required to be of the same length.
    :param bins: (*int*) If an integer is given, bins + 1 bin edges are returned.
    """
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()
            
    r = g_axes.hist(x, bins, range, density, cumulative,
        bottom, histtype, align, orientation, rwidth, log, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r
    
def stem(*args, **kwargs):
    """
    Make a stem plot.
    
    A stem plot plots vertical lines at each x location from the baseline to y, and 
    places a marker there.
    
    :param x: (*array_like*) The x-positions of the stems.
    :param y: (*array_like*) The y-values of the stem heads.
    :param bottom: (*array_like*) Optional, the y coordinates of the bars default: None
    :param linefmt: (*dict*) Optional, stem line format.
    :param markerfmt: (*dict*) Optional, stem marker format.
    :param color: (*Color*) Optional, the color of the stem.
    
    :returns: Stem line legend break.                  
    """
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()
            
    r = g_axes.stem(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r

def stem3(x, y, z, s=8, c='b', marker='o', alpha=None, linewidth=None,
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
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3D):
            g_axes = axes3d()

    r = g_axes.stem(x, y, z, s, c, marker, alpha, linewidth, verts, **kwargs)
    draw_if_interactive()
    return r

def scatter(*args, **kwargs):
    """
    Make a scatter plot of x vs y, where x and y are sequence like objects of the same lengths.
    
    :param x: (*array_like*) Input x data.
    :param y: (*array_like*) Input y data.
    :param s: (*int*) Size of points.
    :param c: (*Color or array*) Color of the points. Or z vlaues.
    :param alpha: (*int*) The alpha blending value, between 0 (transparent) and 1 (opaque).
    :param marker: (*string*) Marker of the points.
    :param label: (*string*) Label of the points series.
    :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level 
        points to draw, in increasing order.
    
    :returns: Points legend break.
    """
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    # else:
    #     if g_axes.axestype != 'cartesian':
    #         g_axes = axes()
            
    r = g_axes.scatter(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r
    
def scatter3(x, y, z, s=8, c='b', marker='o', **kwargs):
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
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3D):
            g_axes = axes3d()
    
    return g_axes.scatter(x, y, z, s, c, marker, **kwargs)

def arrow(x, y, dx, dy, **kwargs):
    '''
    Add an arrow to the axes.
    
    :param x: (*float*) X coordinate.
    :param y: (*float*) Y coordinate.
    :param dx: (*float*) The length of arrow along x direction.
    :param dy: (*float*) The length of arrow along y direction.
    
    :returns: Arrow graphic.
    '''
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()
            
    r = g_axes.arrow(x, y, dx, dy, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r

def arrowline(x, y, dx=0, dy=0, **kwargs):
    '''
    Add an arrow line to the axes.
    
    :param x: (*float or array_like*) X coordinates.
    :param y: (*float or array_like*) Y coordinates.
    :param dx: (*float*) The length of arrow along x direction. Only valid when x is float.
    :param dy: (*float*) The length of arrow along y direction. Only valid when y is float.
    
    :returns: Arrow line graphic.
    '''
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()
            
    r = g_axes.arrowline(x, y, dx, dy, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r
    
def annotate(s, xy, *args, **kwargs):
    '''
    Annotate the point xy with text s.
    
    :param s: (*string*) The text of the annotation.
    :param xy: (*float, float*) The point (x,y) to annotate.
    :param xytext: (*float, float*) The position (x,y) to place the text at. If None, 
        defaults to xy.
        
    :returns: Annotation.
    '''
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()
            
    r = g_axes.annotate(s, xy, *args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r

def patch(x, y=None, **kwargs):
    '''
    Create one or more filled polygons.
    
    :param x: (*array_like*) X coordinates for each vertex. X should be PolygonShape if y
        is None.
    :param y: (*array_like*) Y coordinates for each vertex.
    '''
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    # else:
        # if g_axes.axestype != 'cartesian':
            # g_axes = axes()
            
    r = g_axes.patch(x, y, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r
    
def rectangle(position, curvature=None, **kwargs):
    '''
    Create one or more filled polygons.
    
    :param position: (*list*) Position of the rectangle [x, y, width, height].
    :param curvature: (*list*) Curvature of the rectangle [x, y]. Default is None.
    '''
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()
            
    r = g_axes.rectangle(position, curvature, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r
    
def fill_between(x, y1, y2=0, where=None, **kwargs):
    """
    Make filled polygons between two curves (y1 and y2) where ``where==True``.
    
    :param x: (*array_like*) An N-length array of the x data.
    :param y1: (*array_like*) An N-length array (or scalar) of the y data.
    :param y2: (*array_like*) An N-length array (or scalar) of the y data.
    :param where: (*array_like*) If None, default to fill between everywhere. If not None, it is an 
        N-length boolean array and the fill will only happen over the regions where ``where==True``.
    """
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()
            
    r = g_axes.fill_between(x, y1, y2, where, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r
    
def fill_betweenx(y, x1, x2=0, where=None, **kwargs):
    """
    Make filled polygons between two curves (x1 and x2) where ``where==True``.
    
    :param y: (*array_like*) An N-length array of the y data.
    :param x1: (*array_like*) An N-length array (or scalar) of the x data.
    :param x2: (*array_like*) An N-length array (or scalar) of the x data.
    :param where: (*array_like*) If None, default to fill between everywhere. If not None, it is an 
        N-length boolean array and the fill will only happen over the regions where ``where==True``.
    """
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()
            
    r = g_axes.fill_betweenx(y, x1, x2, where, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r
        
def pie(x, explode=None, labels=None, colors=None, autopct=None, pctdistance=0.6, shadow=False, 
    labeldistance=1.1, startangle=0, radius=None, wedgeprops=None, **kwargs):
    """
    Plot a pie chart.
    
    Make a pie chart of array *x*. The fraction area of each wedge is given by x/sum(x). If
    sum(x) <= 1, then the values of x give the fractional area directly and the array will not
    be normalized. The wedges are plotted counterclockwise, by default starting from the x-axis.
    
    :param explode: (*None | len(x)sequence) If not *None*, is a ``len(x)`` array which specifies
        the fraction of the radius with which to offset each wedge.
    :param labels: (*None | len(x) sequence of colors*] A sequence of strings providing the labels
        for each wedge.
    :param colors: (*None | color sequence*) A sequence of color args through which the pie chart
        will cycle.
    :param autopct: (*None | format string | format function) If not *None*, is a string or function
        used to label the wedges with their numeric value. The label will be placed inside the wedge.
        If it is a format string, the label will be ``fmt%pct``. If it is a function, it will be called.
    :param pctdistance: (*float*) The ratio between the center of each pie slice and the start of the
        text generated by *autopct*. Ignored if autopct is *None*; default is 0.6.
    :param labeldistance: (*float*) The ratial distance at which the pie labels are drawn.
    :param shadow: (*boolean*) Draw a shadow beneath the pie.
    :param startangle: (*float*) If not *0*, rotates the start of the pie chart by *angle* degrees
        counterclockwise from the x-axis.
    :param radius: (*float*) The radius of the pie, if *radius* is *None* it will be set to 1.
    :param wedgeprops: (*dict*) Dict of arguments passed to the wedge objects making the pie. 
    :param fontname: (*string*) Font name. Default is ``Arial`` .
    :param fontsize: (*int*) Font size. Default is ``14`` .
    
    :returns: (*tuple*) Patches and texts.
    """        
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()
            
    r = g_axes.pie(x, explode, labels, colors, autopct, pctdistance, shadow,
        labeldistance, startangle, radius, wedgeprops, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r

def boxplot(x, sym=None, positions=None, widths=None, color=None, showcaps=True, showfliers=True, showmeans=False, \
        showmedians=True, meanline=False, medianline=True, boxprops=None, medianprops=None, meanprops=None, whiskerprops=None, capprops=None, flierprops=None):
    """
    Make a box and whisker plot.
    
    Make a box and whisker plot for each column of x or each vector in sequence x. The box extends from lower
    to upper quartile values of the data, with a line at the median. The whiskers extend from the box to show
    the range of the data. Flier points are those past the end of the whiskers.
    
    :param x: (*Array or a sequence of vectors*) The input data.
    :param sym: (*string*) The default symbol for flier points. Enter an empty string ('') if you don’t 
        want to show fliers. If None, then the fliers default to ‘b+’ If you want more control use the 
        flierprops kwarg.
    :param positions: (*array_like*) Sets the positions of the boxes. The ticks and limits are automatically 
        set to match the positions. Defaults to range(1, N+1) where N is the number of boxes to be drawn.
    :param widths: (*scalar or array_like*) Sets the width of each box either with a scalar or a sequence. 
        The default is 0.5, or 0.15*(distance between extreme positions), if that is smaller.
    :param color: (*Color*) Color for all parts of the box plot. Defaul is None.
    :param showcaps: (*boolean*) Show the caps on the ends of whiskers. Default is ``True``.
    :param showfliers: (*boolean*) Show the outliers beyond the caps. Defaul is ``True``.
    :param showmeans: (*boolean*) Default is ``False``. Show the mean or not.
    :param showmedians: (*boolean*) Default is ``True``. Show the median or not.
    :param meanline: (*boolean*) Default is ``False``. If ``True`` (and showmeans is ``True``), will try to render
        the mean as a line spanning. Otherwise, means will be shown as points.
    :param medianline: (*boolean*) Default is ``True``. If ``True`` (and showmedians is ``True``), will try to render
        the median as a line spanning. Otherwise, medians will be shown as points.
    :param boxprops: (*dict*) Specifies the style of the box.
    :param medianprops: (*dict*) Specifies the style of the median.
    :param meanprops: (*dict*) Specifies the style of the mean.
    :param whiskerprops: (*dict*) Specifies the style of the whiskers.
    :param capprops: (*dict*) Specifies the style of the caps.
    :param flierprops: (*dict*) Specifies the style of the fliers.
    """
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()
            
    r = g_axes.boxplot(x, sym, positions, widths, color, showcaps, showfliers, showmeans, \
        showmedians, meanline, medianline, boxprops, medianprops, meanprops, whiskerprops, capprops, flierprops)
    if not r is None:
        draw_if_interactive()
    return r
    
def violinplot(dataset, positions=None, widths=0.5, boxwidth=0.01, boxprops=None, \
    whiskerprops=None, **kwargs):
    """
    Make a violin plot.
    
    :param dateset: (*Array or a sequence of vectors*) The input data.
    :param positions: (*array_like*) Sets the positions of the violins. The ticks and limits are automatically 
        set to match the positions. Defaults to range(1, N+1) where N is the number of violins to be drawn.
    :param widths: (*scalar or array_like*) Sets the width of each box either with a scalar or a sequence. 
        The default is 0.5, or 0.15*(distance between extreme positions), if that is smaller.   
    :param boxwidth: (*float*) box width.
    :param boxprops: (*dict*) Specifies the style of the box.
    :param whiskerprops: (*dict*) Specifies the style of the whiskers.
    
    :returns: Violin graphics.
    """
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()
            
    r = g_axes.violinplot(dataset, positions, widths, boxwidth, boxprops, whiskerprops, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r
  
def windrose(wd, ws, nwdbins=16, wsbins=None, degree=True, colors=None, cmap='matlab_jet', \
    alpha=0.7, rmax=None, rtickloc=None, rticks=None, rlabelpos=60, xticks=None, **kwargs):
    '''
    Plot windrose chart.
    
    :param wd: (*array_like*) Wind direction.
    :param ws: (*array_like*) Wind speed.
    :param nwdbins: (*int*) Number of wind direction bins [4 | 8 | 16].
    :param wsbins: (*array_like*) Wind speed bins.
    :param degree: (*boolean*) The unit of wind direction is degree or radians.
    :param colors: (*color list*) The colors.
    :param cmap: (*string*) Color map.
    :param alpha: (*float*) Color alpha (0 - 1).
    :param rmax: (*float*) Radial maximum value.
    :param rtickloc: (*list of float*) Radial tick locations.
    :param rticks: (*list of string*) Radial ticks.
    :param rlabelpos: (*float*) Radial label position in degree.
    :param xticks: (*list of string*) X ticks.
    
    :returns: Polar axes and bars
    '''    
    if not nwdbins in [4, 8, 16]:
        print 'nwdbins must be 4, 8 or 16!'
        raise ValueError(nwdbins)
        
    if isinstance(wd, list):
        wd = np.array(wd)
    if isinstance(ws, list):
        ws = np.array(ws)
    
    wdbins = np.linspace(0.0, 2 * np.pi, nwdbins + 1)    
    if wsbins is None:
        wsbins = np.arange(0., ws.max(), 2.).tolist()
        wsbins.append(100)
        wsbins = np.array(wsbins)            
    
    dwdbins = np.degrees(wdbins)
    dwdbins = dwdbins - 90
    for i in range(len(dwdbins)):
        if dwdbins[i] < 0:
            dwdbins[i] += 360
    for i in range(len(dwdbins)):
        d = dwdbins[i]
        d = 360 - d
        dwdbins[i] = d
    rwdbins = np.radians(dwdbins)
        
    N = len(wd)
    wdN = nwdbins
    wsN = len(wsbins) - 1
    if colors is None:
        colors = makecolors(wsN, cmap=cmap, alpha=alpha)
    
    wd = wd + 360./wdN/2
    wd[wd>360] = wd - 360
    rwd = np.radians(wd)    

    bottom = kwargs.pop('bottom', None)
    global g_axes
    if g_axes is None:
        g_axes = axes(polar=True, bottom=bottom)
    else:
        if not isinstance(g_axes, PolarAxes):
            g_axes = axes(polar=True, bottom=bottom)
    
    width = kwargs.pop('width', 0.5)
    if width > 1:
        width = 1
    if width <= 0:
        width = 0.2
    theta = rwdbins[:-1]
    width = 2. * width * np.pi / wdN
        
    bars = []
    hhist = 0
    rrmax = 0       
    for i in range(wsN):
        idx = np.where((ws>=wsbins[i]) * (ws<wsbins[i+1]))
        if idx is None:
            continue
        print wsbins[i], wsbins[i+1]
        s_wd = rwd[idx]
        wdhist = np.histogram(s_wd, wdbins)[0].astype('float')
        wdhist = wdhist / N
        rrmax = max(rrmax, wdhist.max())
        lab = '%s - %s' % (wsbins[i], wsbins[i+1])
        bb = bar(theta, wdhist, width, bottom=hhist, color=colors[i], \
            edgecolor='gray', label=lab, morepoints=True)[0]
        bb.setStartValue(wsbins[i])
        bb.setEndValue(wsbins[i+1])
        bars.append(bb)
        hhist = hhist + wdhist
    
    if rmax is None:
        rmax = math.ceil(rrmax)
    g_axes.set_rmax(rmax)
    if not rtickloc is None:
        g_axes.set_rtick_locations(rtickloc)
    if not rticks is None:
        g_axes.set_rticks(rticks)
    g_axes.set_rtick_format('%')
    g_axes.set_rlabel_position(rlabelpos)
    g_axes.set_xtick_locations(np.arange(0., 360., 360./wdN))
    step = 16 / nwdbins
    if xticks is None:
        xticks = ['E','ENE','NE','NNE','N','NNW','NW','WNW','W','WSW',\
            'SW','SSW','S','SSE','SE','ESE']
        xticks = xticks[::step]
    g_axes.set_xticks(xticks)
    draw_if_interactive()
    return g_axes, bars
 
def figure(bgcolor='w', figsize=None, newfig=True):
    """
    Creates a figure.
    
    :param bgcolor: (*Color*) Optional, background color of the figure. Default is ``w`` (white) .
    :param figsize: (*list*) Optional, width and height of the figure such as ``[600, 400]`` .
        Default is ``None`` with changable size same as *Figures* window.
    :param newfig: (*boolean*) Optional, if creates a new figure. Default is ``True`` .
    """
    global g_figure
    g_figure = Figure(figsize, bgcolor=bgcolor)
    if not batchmode:
        show(newfig)
        
    return g_figure
    
def glfigure(bgcolor='w', newfig=True, **kwargs):
    """
    Creates a figure.
    
    :param bgcolor: (*Color*) Optional, background color of the figure. Default is ``w`` (white) .
    :param newfig: (*boolean*) Optional, if creates a new figure. Default is ``True`` .
    """
    global g_figure
    g_figure = GLFigure(**kwargs)
    if not batchmode:
        show(newfig)
        
    return g_figure
    
def get_figure():
    """
    Get figure object.
    
    :returns: (*Figure*) Figure object.
    """
    return g_figure
        
def show(newfig=True):
    if migl.milapp is None:
        if not batchmode:            
            form = ChartForm(g_figure)
            g_figure.paintGraphics()
            form.setSize(600, 500)
            form.setLocationRelativeTo(None)
            form.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
            form.setVisible(True)     
    else:
        figureDock = migl.milapp.getFigureDock()
        if newfig:
            figureDock.addFigure(g_figure)
        else:
            if figureDock.getCurrentFigure() is None:
                figureDock.addFigure(g_figure)
            else:
                figureDock.setCurrentFigure(g_figure)
    
# Set figure background color
def bgcolor(color):
    '''
    Set figure background color
    
    :param color: (*Color*) Background color    
    '''
    chart = g_figure.getChart()
    chart.setBackground(plotutil.getcolor(color))
    draw_if_interactive()    
    
def caxes(ax=None):
    '''
    Set or get current axes.
    
    :param ax: (*Axes or int*) The axes to be set as current axes. Is None, get current
        axes.
    '''
    global g_axes
    chart = g_figure.getChart()    
    if isinstance(ax, int):
        if g_figure is None:
            figure()
                        
        g_axes = __get_axes(chart, ax)
        chart.setCurrentPlot(ax - 1)
    elif not ax is None:
        g_axes = ax
        chart.setCurrentPlot(chart.getPlotIndex(ax.axes))
    return g_axes

def subplot(nrows, ncols, plot_number, **kwargs):
    """
    Returen a subplot axes positioned by the given grid definition.
    
    :param nrows, nrows: (*int*) Whree *nrows* and *ncols* are used to notionally spli the 
        figure into ``nrows * ncols`` sub-axes.
    :param plot_number: (*int) Is used to identify the particular subplot that this function
        is to create within the notional gird. It starts at 1, increments across rows first
        and has a maximum of ``nrows * ncols`` .
    
    :returns: Current axes specified by ``plot_number`` .
    """
    if g_figure is None:
        figure()
        
    global g_axes
    g_axes = g_figure.subplot(nrows, ncols, plot_number, **kwargs)
    
    return g_axes
    
def subplots(nrows=1, ncols=1, position=None, sharex=False, sharey=False, \
    wspace=None, hspace=None, axestype='Axes', **kwargs):
    '''
    Create a figure and a set of subplots.
    
    :param nrows: (*int*) Number of rows.
    :param ncols: (*int*) Number of cols.
    :param position: (*list*) All axes' position specified by *position=* [left, bottom, width
        height] in normalized (0, 1) units. Default is [0,0,1,1].
    :param sharex: (*boolean*) If share x axis.
    :param sharey: (*boolean*) If share y axis.
    :param subplot_kw: (*dict*) Subplot key words.
    :param wspace: (*float*) The amount of width reserved for blank space between subplots,
        expressed as a fraction of the average axis width.
    :param hspace: (*float*) The amount of height reserved for blank space between subplots,
        expressed as a fraction of the average axis height.
    :param axestype: (*string*) Axes type [Axes | Axes3D | MapAxes | PolarAxes].
    
    :returns: The figure and the axes tuple.
    '''
    global g_figure
    if g_figure is None:
        figure()
        
    axs = g_figure.subplots(nrows, ncols, position, sharex, sharey, \
        wspace, hspace, axestype, **kwargs)
        
    global g_axes
    if isinstance(axs[0], tuple):
        g_axes = axs[0][0]
    else:
        g_axes = axs[0]
    return g_figure, axs

def currentplot(plot_number):
    if g_figure is None:
        figure()
        
    global g_axes
    chart = g_figure.getChart()
    g_axes = __get_axes(chart, plot_number)
    chart.setCurrentPlot(plot_number - 1)
    
    return plot

def __get_axes(chart, idx):
    ax = chart.getPlot(idx)
    if isinstance(ax, Plot2D):
        ax = Axes(ax)
    elif isinstance(ax, MapPlot):
        ax = MapAxes(ax)
    elif isinstance(ax, PolarAxes):
        ax = PolarAxes(ax)
    elif isinstance(ax, Plot3D):
        ax = Plot3D(ax)
    return ax
    
def axes(*args, **kwargs):
    """
    Add an axes to the figure.
    
    :param position: (*list*) Optional, axes position specified by *position=* [left, bottom, width
        height] in normalized (0, 1) units. Default is [0.13, 0.11, 0.775, 0.815].
    :param outerposition: (*list*) Optional, axes size and location, including labels and margin.
    :param aspect: (*string*) ['equal' | 'auto'] or a number. If a number the ratio of x-unit/y-unit in screen-space.
        Default is 'auto'.
    :param bgcolor: (*Color*) Optional, axes background color.
    :param axis: (*boolean*) Optional, set all axis visible or not. Default is ``True`` .
    :param bottomaxis: (*boolean*) Optional, set bottom axis visible or not. Default is ``True`` .
    :param leftaxis: (*boolean*) Optional, set left axis visible or not. Default is ``True`` .
    :param topaxis: (*boolean*) Optional, set top axis visible or not. Default is ``True`` .
    :param rightaxis: (*boolean*) Optional, set right axis visible or not. Default is ``True`` .
    :param xaxistype: (*string*) Optional, set x axis type as 'normal', 'lon', 'lat' or 'time'.
    :param xreverse: (*boolean*) Optional, set x axis reverse or not. Default is ``False`` .
    :param yreverse: (*boolean*) Optional, set yaxis reverse or not. Default is ``False`` .
    
    :returns: The axes.
    """
    global g_axes
               
    if g_figure is None or isinstance(g_figure, GLFigure):
        figure()
        
    ax = g_figure.add_axes(*args, **kwargs) 

    g_axes = ax
    draw_if_interactive()
    return ax

def axesm(*args, **kwargs):  
    """
    Add an map axes to the figure.
    
    :param projinfo: (*ProjectionInfo*) Optional, map projection, default is longlat projection.
    :param position: (*list*) Optional, axes position specified by *position=* [left, bottom, width
        height] in normalized (0, 1) units. Default is [0.13, 0.11, 0.775, 0.815].
    :param bgcolor: (*Color*) Optional, axes background color.
    :param axis: (*boolean*) Optional, set all axis visible or not. Default is ``True`` .
    :param bottomaxis: (*boolean*) Optional, set bottom axis visible or not. Default is ``True`` .
    :param leftaxis: (*boolean*) Optional, set left axis visible or not. Default is ``True`` .
    :param topaxis: (*boolean*) Optional, set top axis visible or not. Default is ``True`` .
    :param rightaxis: (*boolean*) Optional, set right axis visible or not. Default is ``True`` .
    :param xyscale: (*int*) Optional, set scale of x and y axis, default is 1. It is only
        valid in longlat projection.
    :param gridlabel: (*boolean*) Optional, set axis tick labels visible or not. Default is ``True`` .
    :param gridline: (*boolean*) Optional, set grid line visible or not. Default is ``False`` .
    :param griddx: (*float*) Optional, set x grid line interval. Default is 10 degree.
    :param griddy: (*float*) Optional, set y grid line interval. Default is 10 degree.
    :param frameon: (*boolean*) Optional, set frame visible or not. Default is ``False`` for lon/lat
        projection, ortherwise is ``True``.
    :param tickfontname: (*string*) Optional, set axis tick labels font name. Default is ``Arial`` .
    :param tickfontsize: (*int*) Optional, set axis tick labels font size. Default is 14.
    :param tickbold: (*boolean*) Optional, set axis tick labels font bold or not. Default is ``False`` .
    
    :returns: The map axes.
    """
    kwargs['axestype'] = 'map'
    return axes(*args, **kwargs)
    
def axes3d(*args, **kwargs):
    """
    Add an axes to the figure.
    
    :param position: (*list*) Optional, axes position specified by *position=* [left, bottom, width
        height] in normalized (0, 1) units. Default is [0.13, 0.11, 0.775, 0.815].
    :param outerposition: (*list*) Optional, axes size and location, including labels and margin.    
    
    :returns: The axes.
    """
    opengl = kwargs.pop('opengl', True)
    if opengl:
        return axes3dgl(*args, **kwargs)
    else:
        kwargs['axestype'] = '3d'
        return axes(*args, **kwargs)
    
def axes3dgl(*args, **kwargs):
    """
    Add an 3d axes with JOGL to the figure.
    
    :returns: The axes.
    """
    global g_axes
               
    if g_figure is None or isinstance(g_figure, Figure):
        glfigure(**kwargs)
        
    ax = Axes3DGL(*args, **kwargs)
    g_figure.set_axes(ax)
    g_axes = ax
    draw_if_interactive()
    return ax
    
def twinx(ax):
    """
    Make a second axes that shares the x-axis. The new axes will overlay *ax*. The ticks 
    for *ax2* will be placed on the right, and the *ax2* instance is returned.
    
    :param ax: Existing axes.
    
    :returns: The second axes
    """
    ax2 = ax.twinx()
    g_figure._add_axes(ax2)
    global g_axes
    g_axes = ax2
    return ax2
    
def twiny(ax):
    """
    Make a second axes that shares the y-axis. The new axes will overlay *ax*. The ticks 
    for *ax2* will be placed on the top, and the *ax2* instance is returned.
    
    :param ax: Existing axes.
    
    :returns: The second axes
    """
    ax2 = ax.twiny()
    g_figure._add_axes(ax2)
    global g_axes
    g_axes = ax2
    return ax2

def xaxis(ax=None, **kwargs):
    """
    Set x axis of the axes.
    
    :param ax: The axes.
    :param color: (*Color*) Color of the x axis. Default is 'black'.
    :param shift: (*int) X axis shif along x direction. Units is pixel. Default is 0.
    :param visible: (*boolean*) Set axis visible or not, Default is `None`.
    :param linewidth: (*float*) Line width of the axis.
    :param linestyle: (*string*) Line style of the axis.
    :param tickline: (*boolean*) Draw tick line or not.
    :param tickwidth: (*float*) Tick line width.
    :param ticklength: (*float*) Tick line length.
    :param ticklabel: (*boolean*) Draw tick label or not.
    :param minortick: (*boolean*) Draw minor tick line or not.
    :param minorticknum: (*int*) Minor tick line number between two adjacent major tick lines.
    :param tickin: (*boolean*) Tick lines are ploted inside or outside of the axes.
    :param axistype: (*string*) Axis type ['normal' | 'lon' | 'lat' | 'time' | 'log'].
    :param timetickformat: (*string*) Time tick label format, only valid with time axis.
    :param tickfontname: (*string*) Tick label font name.
    :param tickfontsize: (*int*) Tick label font size.
    :param tickbold: (*boolean*) Tick label font is bold or not.
    :param location: (*string*) Locations of the axis ['both' | 'top' | 'bottom'].
    """
    if ax is None:
        ax = g_axes
    ax.xaxis(**kwargs)
    draw_if_interactive()
    
def yaxis(ax=None, **kwargs):
    """
    Set y axis of the axes.
    
    :param ax: The axes.
    :param color: (*Color*) Color of the y axis. Default is 'black'.
    :param shift: (*int) Y axis shif along x direction. Units is pixel. Default is 0.
    :param visible: (*boolean*) Set axis visible or not, Default is `None`.
    :param linewidth: (*float*) Line width of the axis.
    :param linestyle: (*string*) Line style of the axis.
    :param tickline: (*boolean*) Draw tick line or not.
    :param tickwidth: (*float*) Tick line width.
    :param ticklength: (*float*) Tick line length.
    :param ticklabel: (*boolean*) Draw tick label or not.
    :param minortick: (*boolean*) Draw minor tick line or not.
    :param minorticknum: (*int*) Minor tick line number between two adjacent major tick lines.
    :param tickin: (*boolean*) Tick lines are ploted inside or outside of the axes.
    :param axistype: (*string*) Axis type ['normal' | 'lon' | 'lat' | 'time' | 'log'].
    :param timetickformat: (*string*) Time tick label format, only valid with time axis.
    :param tickfontname: (*string*) Tick label font name.
    :param tickfontsize: (*int*) Tick label font size.
    :param tickbold: (*boolean*) Tick label font is bold or not.
    :param location: (*string*) Locations of the axis ['both' | 'left' | 'right'].
    """
    if ax is None:
        ax = g_axes
    ax.yaxis(**kwargs)
    draw_if_interactive()
    
def zaxis(ax=None, **kwargs):
    """
    Set z axis of the axes.
    
    :param ax: The axes.
    :param color: (*Color*) Color of the z axis. Default is 'black'.
    :param shift: (*int) z axis shif along horizontal direction. Units is pixel. Default is 0.
    """
    if ax is None:
        ax = g_axes
    ax.zaxis(**kwargs)
    draw_if_interactive()
    
def box(ax=None, on=None):
    """
    Display axes outline or not.
    
    :param ax: The axes. Current axes is used if ax is None.
    :param on: (*boolean*) Box on or off. If on is None, toggle state.
    """
    if ax is None:
        ax = g_axes
    locs_all = [Location.LEFT, Location.BOTTOM, Location.TOP, Location.RIGHT]
    locs = []
    for loc in locs_all:
        if not ax.axes.getAxis(loc).isDrawTickLabel():
            locs.append(loc)
    for loc in locs:
        axis = ax.axes.getAxis(loc)
        if on is None:
            axis.setVisible(not axis.isVisible())
        else:
            axis.setVisible(on)
    draw_if_interactive()
    
def antialias(b=None, symbol=None):
    """
    Set figure antialias or not.
    
    :param b: (*boolean*) Set figure antialias or not. Default is ``None``, means the opposite with 
        current status.
    :param symbol: (*boolean*) Set symbol antialias or not.
    """
    if g_figure is None:
        figure()
    
    g_figure.set_antialias(b, symbol)
    draw_if_interactive()
    
def savefig(fname, width=None, height=None, dpi=None, sleep=None):
    """
    Save the current figure.
    
    :param fname: (*string*) A string containing a path to a filename. The output format
        is deduced from the extention of the filename. Supported format: 'png', 'bmp',
        'jpg', 'eps' and 'pdf'.
    :param width: (*int*) Optional, width of the output figure with pixel units. Default
        is None, the output figure size is same as *figures* window.
    :param height: (*int*) Optional, height of the output figure with pixel units. Default
        is None, the output figure size is same as *figures* window.
    :param dpi: (*int*) Optional, figure resolution.
    :param sleep: (*int*) Optional, sleep seconds. For web map tiles loading.
    """
    if fname.endswith('.eps') or fname.endswith('.pdf'):
        dpi = None
        
    if dpi != None:
        if (not width is None) and (not height is None):
            g_figure.saveImage(fname, dpi, width, height, sleep)
        else:
            g_figure.saveImage(fname, dpi, sleep)
    else:
        if (not width is None) and (not height is None):
            g_figure.saveImage(fname, width, height, sleep)
        else:
            if sleep is None:
                g_figure.saveImage(fname)
            else:
                g_figure.saveImage(fname, sleep)
        
def savefig_jpeg(fname, width=None, height=None, dpi=None):
    """
    Save the current figure as a jpeg file.
    
    :param fname: (*string*) A string containing a path to a filename. The output format
        is deduced from the extention of the filename. Supported format: 'jpg'.
    :param width: (*int*) Optional, width of the output figure with pixel units. Default
        is None, the output figure size is same as *figures* window.
    :param height: (*int*) Optional, height of the output figure with pixel units. Default
        is None, the output figure size is same as *figures* window.
    """
    #if (not width is None) and (not height is None):
    #    g_figure.setSize(width, height)
    #g_figure.paintGraphics()
    if not dpi is None:
        if (not width is None) and (not height is None):
            g_figure.saveImage_Jpeg(fname, width, height, dpi)
        else:
            g_figure.saveImage_Jpeg(fname, dpi)
    else:
        if (not width is None) and (not height is None):
            g_figure.saveImage(fname, width, height)
        else:
            g_figure.saveImage(fname)  

# Clear current axes
def cla():
    '''
    Clear current axes.
    '''
    global g_axes
    if not g_axes is None:
        if not g_figure is None:
            chart = g_figure.getChart()
            if not chart is None:
                g_figure.getChart().removePlot(g_axes.axes)
        g_axes = None
        draw_if_interactive()

# Delete current figure
def delfig():
    '''
    Clear current figure.
    '''
    if g_figure is None:
        return
    
    figureDock = migl.milapp.getFigureDock()
    figureDock.removeFigure(g_figure)
    
    global g_axes
    g_axes = None
    #draw_if_interactive()
    
# Clear current figure    
def clf():
    '''
    Clear current figure.
    '''
    if g_figure is None:
        return

    if isinstance(g_figure, GLFigure):
        delfig()
        return

    if g_figure.getChart() is None:
        return
    
    g_figure.getChart().setTitle(None)
    g_figure.getChart().clearPlots()
    g_figure.getChart().clearTexts()
    global g_axes
    g_axes = None
    draw_if_interactive()

# Clear last layer    
def cll():
    '''
    Clear last added layer or plot object.
    '''
    if not g_axes is None:
        if isinstance(g_axes, MapAxes):
            g_axes.axes.removeLastLayer()
        else:
            g_axes.axes.removeLastGraphic()
            g_axes.axes.setAutoExtent()
        draw_if_interactive()
        
def clc():
    '''
    Clear command window.
    '''
    if not migl.milapp is None:
        console = migl.milapp.getConsoleDockable().getConsole()
        console.getTextPane().setText('')   

def title(label, loc='center', fontname=None, fontsize=14, bold=True, color='black', **kwargs):
    """
    Set a title of the current axes.
    
    :param label: (*string*) Title label string.
    :param loc: (*string') Which title to set ['center' | 'left' | 'right'],
        default to 'center'.
    :param fontname: (*string*) Font name. Default is ``None``, using ``Arial`` .
    :param fontsize: (*int*) Font size. Default is ``14`` .
    :param bold: (*boolean*) Is bold font or not. Default is ``True`` .
    :param color: (*color*) Title string color. Default is ``black`` .  
    :param linespace: (*int*) Line space of multiple line title.
    """
    r = g_axes.set_title(label, loc, fontname, fontsize, bold, color, **kwargs)
    draw_if_interactive()
    return r
    
def suptitle(label, fontname=None, fontsize=14, bold=True, color='black'):
    """
    Add a centered title to the figure.
    
    :param label: (*string*) Title label string.
    :param fontname: (*string*) Font name. Default is ``Arial`` .
    :param fontsize: (*int*) Font size. Default is ``14`` .
    :param bold: (*boolean*) Is bold font or not. Default is ``True`` .
    :param color: (*color*) Title string color. Default is ``black`` .
    """    
    r = g_figure.set_title(label, fontname, fontsize, bold, color)
    draw_if_interactive()
    return r
    
def left_title(label, fontname=None, fontsize=14, bold=False, color='black', **kwargs):
    """
    Set a left sub title of the current axes.
    
    :param label: (*string*) Title string.
    :param fontname: (*string*) Font name. Default is ``None``, using ``Arial`` .
    :param fontsize: (*int*) Font size. Default is ``14`` .
    :param bold: (*boolean*) Is bold font or not. Default is ``False`` .
    :param color: (*color*) Title string color. Default is ``black`` .    
    """
    r = g_axes.set_title(label, 'left', fontname, fontsize, bold, color, **kwargs)
    draw_if_interactive()
    return r
    
def right_title(label, fontname=None, fontsize=14, bold=False, color='black', **kwargs):
    """
    Set a right sub title of the current axes.
    
    :param label: (*string*) Title string.
    :param fontname: (*string*) Font name. Default is ``None``, using ``Arial`` .
    :param fontsize: (*int*) Font size. Default is ``14`` .
    :param bold: (*boolean*) Is bold font or not. Default is ``False`` .
    :param color: (*color*) Title string color. Default is ``black`` .    
    """
    r = g_axes.set_title(label, 'right', fontname, fontsize, bold, color, **kwargs)
    draw_if_interactive()
    return r

def xlabel(label, **kwargs):
    """
    Set the x axis label of the current axes.
    
    :param label: (*string*) Label string.
    :param fontname: (*string*) Font name. Default is ``Arial`` .
    :param fontsize: (*int*) Font size. Default is ``14`` .
    :param bold: (*boolean*) Is bold font or not. Default is ``True`` .
    :param color: (*color*) Label string color. Default is ``black`` .
    """
    g_axes.set_xlabel(label, **kwargs)
    draw_if_interactive()
    
def ylabel(label, **kwargs):
    """
    Set the y axis label of the current axes.
    
    :param label: (*string*) Label string.
    :param fontname: (*string*) Font name. Default is ``Arial`` .
    :param fontsize: (*int*) Font size. Default is ``14`` .
    :param bold: (*boolean*) Is bold font or not. Default is ``True`` .
    :param color: (*color*) Label string color. Default is ``black`` .
    """
    g_axes.set_ylabel(label, **kwargs)
    draw_if_interactive()

def zlabel(label, **kwargs):
    """
    Set the z axis label of the current axes.

    :param label: (*string*) Label string.
    :param fontname: (*string*) Font name. Default is ``Arial`` .
    :param fontsize: (*int*) Font size. Default is ``14`` .
    :param bold: (*boolean*) Is bold font or not. Default is ``True`` .
    :param color: (*color*) Label string color. Default is ``black`` .
    """
    global g_axes
    if not isinstance(g_axes, Axes3D):
        return

    g_axes.set_zlabel(label, **kwargs)
    draw_if_interactive()

def xticks(*args, **kwargs):
    """
    Set the x-limits of the current tick locations and labels.
    
    :param locs: (*array_like*) Tick locations.
    :param labels: (*string list*) Tick labels.
    :param fontname: (*string*) Font name. Default is ``Arial`` .
    :param fontsize: (*int*) Font size. Default is ``14`` .
    :param bold: (*boolean*) Is bold font or not. Default is ``True`` .
    :param color: (*color*) Tick label string color. Default is ``black`` .
    :param rotation: (*float*) Tick label rotation angle. Default is 0.
    """
    if len(args) > 0:
        locs = args[0]
        if len(locs) > 0:
            if isinstance(locs, NDArray):
                locs = locs.aslist()
            if isinstance(locs[0], datetime.datetime):
                for i in range(len(locs)):
                    locs[i] = miutil.date2num(locs[i])
        g_axes.set_xticks(locs)
        args = args[1:]
    if len(args) > 0:
        labels = args[0]
        if isinstance(labels, (NDArray, DimArray)):
            labels = labels.aslist()
    else:
        labels = None
    g_axes.set_xticklabels(labels, **kwargs)
    
    draw_if_interactive()
    
def yticks(*args, **kwargs):
    """
    Set the y-limits of the current tick locations and labels.
    
    :param locs: (*array_like*) Tick locations.
    :param labels: (*string list*) Tick labels.
    :param fontname: (*string*) Font name. Default is ``Arial`` .
    :param fontsize: (*int*) Font size. Default is ``14`` .
    :param bold: (*boolean*) Is bold font or not. Default is ``True`` .
    :param color: (*color*) Tick label string color. Default is ``black`` .
    :param rotation: (*float*) Tick label rotation angle. Default is 0.
    """
    if len(args) > 0:
        locs = args[0]
        if len(locs) > 0:
            if isinstance(locs, (NDArray, DimArray)):
                locs = locs.aslist()
            if isinstance(locs[0], datetime.datetime):
                for i in range(len(locs)):
                    locs[i] = miutil.date2num(locs[i])
        g_axes.set_yticks(locs)
        args = args[1:]
    if len(args) > 0:
        labels = args[0]
        if isinstance(labels, (NDArray, DimArray)):
            labels = labels.aslist()
    else:
        labels = None
    g_axes.set_yticklabels(labels, **kwargs)
    
    draw_if_interactive()
    
def zticks(*args, **kwargs):
    """
    Set the z-limits of the current tick locations and labels.
    
    :param locs: (*array_like*) Tick locations.
    :param labels: (*string list*) Tick labels.
    :param fontname: (*string*) Font name. Default is ``Arial`` .
    :param fontsize: (*int*) Font size. Default is ``14`` .
    :param bold: (*boolean*) Is bold font or not. Default is ``True`` .
    :param color: (*color*) Tick label string color. Default is ``black`` .
    :param rotation: (*float*) Tick label rotation angle. Default is 0.
    """
    if not isinstance(g_axes, Axes3D):
        return
        
    if len(args) > 0:
        locs = args[0]
        if isinstance(locs, (NDArray, DimArray)):
            locs = locs.aslist()
        if isinstance(locs[0], datetime.datetime):
            for i in range(len(locs)):
                locs[i] = miutil.date2num(locs[i])
        g_axes.set_zticks(locs)
        args = args[1:]
    if len(args) > 0:
        labels = args[0]
        if isinstance(labels, (NDArray, DimArray)):
            labels = labels.aslist()
    else:
        labels = None
    g_axes.set_zticklabels(labels, **kwargs)
    
    draw_if_interactive()
    
def text(x, y, s, **kwargs):
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
    coordinates = kwargs.pop('coordinates', 'data')
    if coordinates == 'figure':
        g_figure.getChart().addText(ctext)
    else:
        if isinstance(g_axes, MapAxes):
            islonlat = kwargs.pop('islonlat', True)
            g_axes.axes.addText(ctext, islonlat)
        else:
            g_axes.axes.addText(ctext)
    draw_if_interactive()
    return ctext

def text3(x, y, z, s, zdir=None, **kwargs):
    '''
    Add text to the plot. kwargs will be passed on to text, except for the zdir
    keyword, which sets the direction to be used as the z direction.

    :param x: (*float*) X coordinate.
    :param y: (*float*) Y coordinate.
    :param z: (*float*) Z coordinate.
    :param s: (*string*) Text string.
    :param zdir: Z direction.
    '''
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3D):
            g_axes = axes3d()

    r = g_axes.text(x, y, z, s, zdir, **kwargs)
    draw_if_interactive()
    return r
    
def axis(limits):
    """
    Sets the min and max of the x and y axes, with ``[xmin, xmax, ymin, ymax]`` .
    
    :param limits: (*list*) Min and max of the x and y axes.
    """
    r = g_axes.axis(limits)
    if not r is None:
        draw_if_interactive()
            
def axism(limits=None, lonlat=True):
    """
    Sets the min and max of the x and y map axes, with ``[xmin, xmax, ymin, ymax]`` .
    
    :param limits: (*list*) Min and max of the x and y map axes.
    :param lonlat: (*boolean*) Is longitude/latitude or not.
    """
    r = g_axes.axis(limits, lonlat)
    if not r is None:
        draw_if_interactive()

def grid(b=None, **kwargs):
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
    g_axes.grid(b, **kwargs)
    draw_if_interactive()
    
def xlim(xmin, xmax):
    """
    Set the *x* limits of the current axes.
    
    :param xmin: (*float*) Minimum limit of the x axis.
    :param xmax: (*float*) Maximum limit of the x axis.
    """
    g_axes.set_xlim(xmin, xmax)
    draw_if_interactive()
            
def ylim(ymin, ymax):
    """
    Set the *y* limits of the current axes.
    
    :param ymin: (*float*) Minimum limit of the y axis.
    :param ymax: (*float*) Maximum limit of the y axis.
    """
    g_axes.set_ylim(ymin, ymax)
    draw_if_interactive()   
    
def zlim(zmin, zmax):
    """
    Set the *z* limits of the current axes.
    
    :param zmin: (*float*) Minimum limit of the z axis.
    :param zmax: (*float*) Maximum limit of the z axis.
    """
    g_axes.set_zlim(zmin, zmax)
    draw_if_interactive()   

def xreverse():
    '''
    Reverse x axis.
    '''
    g_axes.xreverse()
    draw_if_interactive()
    
def yreverse():
    '''
    Reverse y axis.
    '''
    g_axes.yreverse()
    draw_if_interactive()
            
def legend(*args, **kwargs):
    """
    Places a legend on the axes.
    
    :param breaks: (*ColorBreak*) Legend breaks (optional).
    :param labels: (*list of string*) Legend labels (optional).
    :param orientation: (*string*) Colorbar orientation: ``vertical`` or ``horizontal``.
    :param loc: (*string*) The location of the legend, including: 'upper right', 'upper left',
        'lower left', 'lower right', 'right', 'ceter left', 'center right', lower center',
        'upper center', 'center' and 'custom'. Default is 'upper right'.
    :param x: (*float*) Location x in normalized (0, 1) units when ``loc=custom`` .
    :param y: (*float*) Location y in normalized (0, 1) units when ``loc=custom`` .
    :param frameon: (*boolean*) Control whether a frame should be drawn around the legend. Default
        is True.
    :param facecolor: (*None or color*) Control the legend’s background color. Default is None which 
        set not draw background.
    :param fontname: (*string*) Tick font name. Default is ``Arial`` .
    :param fontsize: (*int*) Tick font size. Default is ``14`` .
    :param bold: (*boolean*) Is bold font or not. Default is ``False`` .
    :param title: (*string*) Label string.
    :param labelfontname: (*string*) Title font name.
    :param labelfontsize: (*int*) Label font size.
    :param labcolor: (*color*) Label color. Default is ``black`` .
    :param markerscale: (*float*) Marker symbol scale.
    :param markerwidth: (*float*) Marker symbol width.
    :param markerheight: (*float*) Marker symbol height.
    :param ncol: (*float*) Column number of the legend.
    :param xshift: (*float*) X shift.
    :param yshift: (*float*) Y shift.
    
    :returns: (*ChartLegend*) The chart legend.
    """
    r = g_axes.legend(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r
    
def readlegend(fn):
    """
    Read legend from a legend file (.lgs).
    
    :param fn: (*string*) Legend file name.
    
    :returns: (*LegendScheme*) Legend.
    """
    if os.path.exists(fn):
        ls = LegendScheme()
        ls.importFromXMLFile(fn, False)
        return ls
    else:
        print 'File not exists: ' + fn
        return None
        
def colorbar(mappable=None, **kwargs):
    """
    Add a colorbar to a plot.
    
    :param mappable: (*MapLayer | LegendScheme | List of ColorBreak*) The mappable in plot.
    :param cax: (*Plot*) None | axes object into which the colorbar will be drawn.
    :param cmap: (*string*) Color map name. Default is None.
    :param shrink: (*float*) Fraction by which to shrink the colorbar. Default is 1.0.
    :param orientation: (*string*) Colorbar orientation: ``vertical`` or ``horizontal``.
    :param aspect: (*int*) Ratio of long to short dimensions.
    :param fontname: (*string*) Font name. Default is ``Arial`` .
    :param fontsize: (*int*) Font size. Default is ``14`` .
    :param bold: (*boolean*) Is bold font or not. Default is ``False`` .
    :param label: (*string*) Label. Default is ``None`` .
    :param labelloc: (*string*) Label location ['in' | 'out' | 'top' | 'bottom' | 'left' | 'right'].
        Defaul is ``out``.
    :param extendrect: (*boolean*) If ``True`` the minimum and maximum colorbar extensions will be
        rectangular (the default). If ``False`` the extensions will be triangular.
    :param extendfrac: [None | 'auto' | length] If set to *None*, both the minimum and maximum triangular
        colorbar extensions with have a length of 5% of the interior colorbar length (the default). If
        set to 'auto', makes the triangular colorbar extensions the same lengths as the interior boxes
        . If a scalar, indicates the length of both the minimum and maximum triangle colorbar extensions
        as a fraction of the interior colorbar length.
    :param ticks: [None | list of ticks] If None, ticks are determined automatically from the input.
    :param ticklabels: [None | list of ticklabels] Tick labels.
    :param tickin: (*boolean*) Draw tick line inside or outside of the colorbar.
    :param tickrotation: (*float*) Set tick label rotation angle.
    :param xshift: (*float*) X shift of the colorbar with pixel coordinate.
    :param yshift: (*float*) Y shift of the colorbar with pixel coordinate.
    :param vmintick: (*boolean*) Draw minimum value tick or not.
    :param vmaxtick: (*boolean*) Draw maximum value tick or not.
    """
    cax = kwargs.pop('cax', None)
    if cax is None:
        cax = g_axes
    cax.colorbar(mappable, **kwargs)
    draw_if_interactive()

def set(obj, **kwargs):
    '''
    Set properties to an object. Used to change the plot parameters.
    '''
    if isinstance(obj, Axes):
        xminortick = kwargs.pop('xminortick', None)
        if not xminortick is None:
            locs = [Location.BOTTOM, Location.TOP]
            for loc in locs:
                axis = obj.axes.getAxis(loc)
                axis.setMinorTickVisible(xminortick)
        yminortick = kwargs.pop('yminortick', None)
        if not yminortick is None:
            locs = [Location.LEFT, Location.RIGHT]
            for loc in locs:
                axis = obj.axes.getAxis(loc)
                axis.setMinorTickVisible(yminortick)
        tickin = kwargs.pop('tickin', None)
        if not tickin is None:
            obj.axes.setInsideTick(tickin)
    draw_if_interactive()

def imshow(*args, **kwargs):
    """
    Display an image on the axes.
    
    :param X: (*array_like*) 2-D or 3-D (RGB or RGBA) image value array or BufferedImage.
    :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level curves 
        to draw, in increasing order.
    :param cmap: (*string*) Color map string.
    :param colors: (*list*) If None (default), the colormap specified by cmap will be used. If a 
        string, like ‘r’ or ‘red’, all levels will be plotted in this color. If a tuple of matplotlib 
        color args (string, float, rgb, etc), different levels will be plotted in different colors in 
        the order specified.
    :param interpolation: (*string*) Interpolation option [None | bilinear | bicubic].
    
    :returns: (*Image graphic*) Image graphic created from array data.
    """
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    # else:
    #     if g_axes.axestype != 'cartesian':
    #         g_axes = axes()
            
    r = g_axes.imshow(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r

def pcolor(*args, **kwargs):
    '''
    Create a pseudocolor plot of a 2-D array.
    
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
    
    :returns: (*GraphicCollection*) Polygon graphic collection.
    '''    
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    # else:
    #     if g_axes.axestype != 'cartesian':
    #         g_axes = axes()
            
    r = g_axes.pcolor(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r
    
def gridshow(*args, **kwargs):
    '''
    Draw a grid plot.
    
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
    
    :returns: (*GraphicCollection*) Polygon graphic collection.
    '''
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    # else:
    #     if g_axes.axestype != 'cartesian':
    #         g_axes = axes()
            
    r = g_axes.gridshow(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r
      
def contour(*args, **kwargs):
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
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    # else:
    #     if g_axes.axestype != 'cartesian' and g_axes.axestype != 'polar':
    #         g_axes = axes()
            
    r = g_axes.contour(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r

def contourf(*args, **kwargs):
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
    
    :returns: (*VectoryLayer*) Contour filled VectoryLayer created from array data.
    """
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    # else:
    #     if g_axes.axestype != 'cartesian' and g_axes.axestype != 'polar':
    #         g_axes = axes()
            
    r = g_axes.contourf(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r

def quiver(*args, **kwargs):
    """
    Plot a 2-D field of arrows.
    
    :param x: (*array_like*) Optional. X coordinate array.
    :param y: (*array_like*) Optional. Y coordinate array.
    :param u: (*array_like*) U component of the arrow vectors (wind field) or wind direction.
    :param v: (*array_like*) V component of the arrow vectors (wind field) or wind speed.
    :param z: (*array_like*) Optional, 2-D z value array.
    :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level 
        vectors to draw, in increasing order.
    :param cmap: (*string*) Color map string.
    :param fill_value: (*float*) Fill_value. Default is ``-9999.0``.
    :param isuv: (*boolean*) Is U/V or direction/speed data array pairs. Default is True.
    :param size: (*float*) Base size of the arrows.
    :param order: (*int*) Z-order of created layer for display.
    
    :returns: (*VectoryLayer*) Created quiver VectoryLayer.
    """
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    # else:
    #     if g_axes.axestype != 'cartesian' and g_axes.axestype != 'polar':
    #         g_axes = axes()
            
    r = g_axes.quiver(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r    

def quiver3(*args, **kwargs):
    """
    Plot a 3-D field of arrows.

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
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3D):
            g_axes = axes3d()

    return g_axes.quiver(*args, **kwargs)

def barbs(*args, **kwargs):
    """
    Plot a 2-D field of barbs.
    
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
    :param order: (*int*) Z-order of created layer for display.
    
    :returns: (*VectoryLayer*) Created barbs VectoryLayer.
    """
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    # else:
    #     if g_axes.axestype != 'cartesian' and g_axes.axestype != 'polar':
    #         g_axes = axes()
            
    r = g_axes.barbs(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r  

def streamplot(*args, **kwargs):
    """
    Plot streamline.
    
    :param x: (*array_like*) Optional. X coordinate array.
    :param y: (*array_like*) Optional. Y coordinate array.
    :param u: (*array_like*) U component of the arrow vectors (wind field) or wind direction.
    :param v: (*array_like*) V component of the arrow vectors (wind field) or wind speed.
    :param z: (*array_like*) Optional, 2-D z value array.
    :param color: (*Color*) Streamline color.
    :param fill_value: (*float*) Fill_value. Default is ``-9999.0``.
    :param isuv: (*boolean*) Is U/V or direction/speed data array pairs. Default is True.
    :param density: (*int*) Streamline density. Default is 4.
    :param zorder: (*int*) Z-order of streamline graphic for display.
    
    :returns: (*VectoryLayer*) Created streamline VectoryLayer.
    """
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    # else:
    #     if g_axes.axestype != 'cartesian' and g_axes.axestype != 'polar':
    #         g_axes = axes()
            
    r = g_axes.streamplot(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r      
 
def scatterm(*args, **kwargs):
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
    :param fill_value: (*float*) Fill_value. Default is ``-9999.0``.
    :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
    :param order: (*int*) Z-order of created layer for display.
    
    :returns: (*VectoryLayer*) Point VectoryLayer.
    """
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()
            
    r = g_axes.scatter(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r  
    
def plotm(*args, **kwargs):
    """
    Plot lines and/or markers to the map.
    
    :param x: (*array_like*) Input x data.
    :param y: (*array_like*) Input y data.
    :param style: (*string*) Line style for plot.
    :param linewidth: (*float*) Line width.
    :param color: (*Color*) Line color.
    
    :returns: (*VectoryLayer*) Line VectoryLayer.
    """
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()
            
    r = g_axes.plot(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r  
    
def stationmodel(smdata, **kwargs):
    """
    Plot station model data on the map.
    
    :param smdata: (*StationModelData*) Station model data.
    :param surface: (*boolean*) Is surface data or not. Default is True.
    :param size: (*float*) Size of the station model symbols. Default is 12.
    :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
    :param order: (*int*) Z-order of created layer for display.
    
    :returns: (*VectoryLayer*) Station model VectoryLayer.
    """
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()
            
    r = g_axes.stationmodel(smdata, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r  
        
def imshowm(*args, **kwargs):
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
    :param order: (*int*) Z-order of created layer for display.
    :param interpolation: (*string*) Interpolation option [None | bilinear | bicubic].
    
    :returns: (*RasterLayer*) RasterLayer created from array data.
    """
    global g_axes
    if g_figure is None:
        figure()
        
    if g_axes is None:
        g_axes = axesm()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()
            
    r = g_axes.imshow(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    
    return r
    
def contourm(*args, **kwargs):  
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
    :param fill_value: (*float*) Fill_value. Default is ``-9999.0``.
    :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
    :param isplot: (*boolean*) Plot layer or not. Default is ``True``.
    :param order: (*int*) Z-order of created layer for display.
    :param smooth: (*boolean*) Smooth countour lines or not.
    :param select: (*boolean*) Set the return layer as selected layer or not.
    
    :returns: (*VectoryLayer*) Contour VectoryLayer created from array data.
    """
    global g_axes
    if g_figure is None:
        figure()
        
    if g_axes is None:
        g_axes = axesm()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()
            
    r = g_axes.contour(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    
    return r
        
def contourfm(*args, **kwargs):
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
    :param fill_value: (*float*) Fill_value. Default is ``-9999.0``.
    :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
    :param isadd: (*boolean*) Add layer in the axes or not. Default is ``True``.
    :param zorder: (*int*) Z-order of created layer for display.
    :param smooth: (*boolean*) Smooth countour lines or not.
    :param select: (*boolean*) Set the return layer as selected layer or not.
    
    :returns: (*VectoryLayer*) Contour filled VectoryLayer created from array data.
    """    
    global g_axes
    if g_figure is None:
        figure()
        
    if g_axes is None:
        g_axes = axesm()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()
            
    r = g_axes.contourf(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    
    return r
    
def pcolorm(*args, **kwargs):
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
    :param isplot: (*boolean*) Plot layer or not. Default is ``True``.
    :param order: (*int*) Z-order of created layer for display.
    :param select: (*boolean*) Set the return layer as selected layer or not.
    
    :returns: (*VectoryLayer*) Polygon VectoryLayer created from array data.
    """    
    global g_axes
    if g_figure is None:
        figure()
        
    if g_axes is None:
        g_axes = axesm()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()
            
    r = g_axes.pcolor(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    
    return r
    
def gridshowm(*args, **kwargs):
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
    :param isplot: (*boolean*) Plot layer or not. Default is ``True``.
    :param order: (*int*) Z-order of created layer for display.
    :param select: (*boolean*) Set the return layer as selected layer or not.
    
    :returns: (*VectoryLayer*) Polygon VectoryLayer created from array data.
    """    
    global g_axes
    if g_figure is None:
        figure()
        
    if g_axes is None:
        g_axes = axesm()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()
            
    r = g_axes.gridshow(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    
    return r
    
def quiverm(*args, **kwargs):
    """
    Plot a 2-D field of arrows in a map.
    
    :param x: (*array_like*) Optional. X coordinate array.
    :param y: (*array_like*) Optional. Y coordinate array.
    :param u: (*array_like*) U component of the arrow vectors (wind field) or wind direction.
    :param v: (*array_like*) V component of the arrow vectors (wind field) or wind speed.
    :param z: (*array_like*) Optional, 2-D z value array.
    :param levs: (*array_like*) Optional. A list of floating point numbers indicating the level 
        vectors to draw, in increasing order.
    :param cmap: (*string*) Color map string.
    :param fill_value: (*float*) Fill_value. Default is ``-9999.0``.
    :param isuv: (*boolean*) Is U/V or direction/speed data array pairs. Default is True.
    :param size: (*float*) Base size of the arrows.
    :param proj: (*ProjectionInfo*) Map projection of the data. Default is None.
    :param zorder: (*int*) Z-order of created layer for display.
    :param select: (*boolean*) Set the return layer as selected layer or not.
    
    :returns: (*VectoryLayer*) Created quiver VectoryLayer.
    """
    global g_axes
    if g_figure is None:
        figure()
        
    if g_axes is None:
        g_axes = axesm()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()
            
    r = g_axes.quiver(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    
    return r
    
def quiverkey(*args, **kwargs):
    """
    Add a key to a quiver plot.
    
    :param Q: (*MILayer or GraphicCollection*) The quiver layer instance returned by a call to quiver/quiverm.
    :param X: (*float*) The location x of the key.
    :param Y: (*float*) The location y of the key.
    :param U: (*float*) The length of the key.
    :param label: (*string*) A string with the length and units of the key.
    :param coordinates=['axes'|'figure'|'data'|'inches']: (*string*) Coordinate system and units for 
        *X, Y*. 'axes' and 'figure' are normalized coordinate system with 0,0 in the lower left and 
        1,1 in the upper right, 'data' are the axes data coordinates (used for the locations of the 
        vectors in the quiver plot itself); 'inches' is position in the figure in inches, with 0,0 
        at the lower left corner.
    :param color: (*Color*) Overrides face and edge colors from Q.
    :param labelpos=['N'|'S'|'E'|'W']: (*string*) Position the label above, below, to the right, to
        the left of the arrow, respectively.
    :param labelsep: (*float*) Distance in inches between the arrow and the label. Default is 0.1.
    :param labelcolor: (*Color*) Label color. Default to default is black.
    :param fontproperties: (*dict*) A dictionary with keyword arguments accepted by the FontProperties
        initializer: *family, style, variant, size, weight*.
    """
    g_axes.quiverkey(*args, **kwargs)
    draw_if_interactive()
 
def barbsm(*args, **kwargs):
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
    :param order: (*int*) Z-order of created layer for display.
    :param select: (*boolean*) Set the return layer as selected layer or not.
    
    :returns: (*VectoryLayer*) Created barbs VectoryLayer.
    """
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axesm()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()
            
    r = g_axes.barbs(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r   
  
def streamplotm(*args, **kwargs):
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
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axesm()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()
            
    r = g_axes.streamplot(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r   

def clabel(layer, **kwargs):
    '''
    Add contour layer labels.
    
    :param layer: (*MILayer*) The contour layer.
    :param fontname, fontsize: The font auguments.
    :param color: (*color*) The label color. Default is ``None``, the label color will be set as
        same as color of the line.
    :param dynamic: (*boolean*) Draw labels dynamic or not. Default is ``True``.
    :param drawshadow: (*boolean*) Draw shadow under labels or not.
    :param fieldname: (*string*) The field name used for label.
    :param xoffset: (*int*) X offset of the labels.
    :param yoffset: (int*) Y offset of the labels.
    :param avoidcoll: (*boolean*) Avoid labels collision or not.
    '''    
    g_axes.clabel(layer, **kwargs)
    draw_if_interactive()

def webmap(provider='OpenStreetMap', zorder=0):
    '''
    Add a new web map layer.
    
    :param provider: (*string*) Web map provider.
    :param zorder: (*int*) Layer order.
    
    :returns: Web map layer
    '''
    layer = g_axes.webmap(provider, zorder)
    draw_if_interactive()
    return layer
        
def geoshow(*args, **kwargs):
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
    global g_axes
    if g_figure is None:
        figure()
        
    if g_axes is None:
        g_axes = axesm()
    else:
        if not isinstance(g_axes, (MapAxes, Axes3D)):
            g_axes = axesm()
            
    r = g_axes.geoshow(*args, **kwargs)
    if not r is None:
        draw_if_interactive()
    
    return r

def taylor_diagram(stddev, correlation, std_max=1.65, labels=None, ref_std=1., colors=None,
                   **kwargs):
    '''
    Create Taylor diagram.

    :param stddev: Standard deviation.
    :param correlation: Pattern correlations.
    :param ref_std: Reference standard deviation.
    :param std_max: Maximum standard deviation.
    :param labels: Data labels.
    :param colors: Data points colors.

    :returns:
    '''
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes(position=[0.13,0.11,0.775,0.75])

    r = g_axes.taylor_diagram(stddev, correlation, std_max, labels, ref_std, colors, **kwargs)
    if not r is None:
        draw_if_interactive()
    return r

def lighting(enable=True, **kwargs):
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
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3DGL):
            g_axes = axes3dgl()

    g_axes.set_lighting(enable, **kwargs)
    draw_if_interactive()

def mesh(*args, **kwargs):
    '''
    creates a three-dimensional surface mesh plot

    :param x: (*array_like*) Optional. X coordinate array.
    :param y: (*array_like*) Optional. Y coordinate array.
    :param z: (*array_like*) 2-D z value array.
    :param cmap: (*string*) Color map string.

    :returns: Legend
    '''
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3D):
            g_axes = axes3d()

    r = g_axes.mesh(*args, **kwargs)
    draw_if_interactive()
    return r

def surf(*args, **kwargs):
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
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3D):
            g_axes = axes3d()

    r = g_axes.surf(*args, **kwargs)
    draw_if_interactive()
    return r

def slice3(*args, **kwargs):
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
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3DGL):
            g_axes = axes3dgl()

    r = g_axes.slice(*args, **kwargs)
    draw_if_interactive()
    return r

def isosurface(*args, **kwargs):
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
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3DGL):
            g_axes = axes3dgl()

    r = g_axes.isosurface(*args, **kwargs)
    draw_if_interactive()
    return r

def particles(*args, **kwargs):
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
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3DGL):
            g_axes = axes3dgl()

    r = g_axes.particles(*args, **kwargs)
    draw_if_interactive()
    return r
    
def makecolors(n, cmap='matlab_jet', reverse=False, alpha=None, start=None, stop=None):
    '''
    Make colors.
    
    :param n: (*int*) Colors number.
    :param cmap: (*string*) Color map name. Default is ``matlab_jet``.
    :param reverse: (*boolean*) Reverse the colors or not. Default is ``False``.
    :param alpha: (*float*) Alpha value (0 - 1) of the colors. Defaul is ``None``.
    :param start: (*int*) Start color index. Default is ``None``.
    :param stop: (*int*) Stop color index. Default is ``None``.

    :returns: (*list*) Created colors.
    '''
    return plotutil.makecolors(n, cmap, reverse, alpha, start, stop)

def makelegend(source, **kwargs):
    '''
    Make a legend.
    
    :param souce: Legend file name or list of the legen breaks.
    
    :returns: Created legend.
    '''
    return plotutil.makelegend(source, **kwargs)
    
def makesymbolspec(geometry, *args, **kwargs):
    '''
    Make a legend.
    
    :param geometry: (*string*) Geometry type. [point | line | polygon].
    :param levels: (*array_like*) Value levels. Default is ``None``, not used.
    :param colors: (*list*) Colors. Defaul is ``None``, not used.
    :param legend break parameter maps: (*map*) Legend breaks.
    :param field: (*string*) The field to be used in the legend.
    
    :returns: Created legend.
    '''
    shapetype = ShapeTypes.Image
    if geometry == 'point':
        shapetype = ShapeTypes.Point
    elif geometry == 'line':
        shapetype = ShapeTypes.Polyline
    elif geometry == 'polygon':
        shapetype = ShapeTypes.Polygon  
    else:
        shapetype = ShapeTypes.Image
        
    levels = kwargs.pop('levels', None)
    cols = kwargs.pop('colors', None)
    field = kwargs.pop('field', '')
    if not levels is None and not cols is None:
        if isinstance(levels, NDArray):
            levels = levels.aslist()
        colors = []
        for cobj in cols:
            colors.append(plotutil.getcolor(cobj))
        ls = LegendManage.createLegendScheme(shapetype, levels, colors)
        plotutil.setlegendscheme(ls, **kwargs)         
        ls.setFieldName(field)
        values = kwargs.pop('values', None)
        if values is None:
            return ls
        else:
            nls = LegendScheme(ls.getShapeType())
            for v in values:
                nls.addLegendBreak(ls.findLegendBreak(v))
            return nls
           
    n = len(args)
    isunique = True
    if n == 0:
        ls = LegendManage.createSingleSymbolLegendScheme(shapetype)
        plotutil.setlegendscheme(ls, **kwargs)
    elif n == 1 and isinstance(args[0], int):
        ls = LegendManage.createUniqValueLegendScheme(args[0], shapetype)
        plotutil.setlegendscheme(ls, **kwargs)
    else:
        ls = LegendScheme(shapetype)
        for arg in args:
            if isinstance(arg, (list, tuple)):
                for argi in arg:
                    lb, isu = plotutil.getlegendbreak(geometry, **argi)
                    if isunique and not isu:
                        isunique = False
                    ls.addLegendBreak(lb)
            else:
                lb, isu = plotutil.getlegendbreak(geometry, **arg)
                if isunique and not isu:
                    isunique = False
                ls.addLegendBreak(lb)
       
    ls.setFieldName(field)
    if ls.getBreakNum() > 1:
        if isunique:
            ls.setLegendType(LegendType.UniqueValue)
        else:
            ls.setLegendType(LegendType.GraduatedColor)
            
    return ls
    
def weatherspec(weather='all', size=20, color='b'):
    '''
    Make a weather symbol legend.
    
    :param weather: (*string or list*) The weather index list. Defaul is ``all``, used all weathers.
    :param size: (*string*) The weather symbol size.
    :param color: (*color*) The weather symbol color.
    
    :returns: Weather symbol legend.
    '''
    if isinstance(weather, str):
        wlist = DrawMeteoData.getWeatherTypes(weather)
    else:
        wlist = weather
    c = plotutil.getcolor(color)
    return DrawMeteoData.createWeatherLegendScheme(wlist, size, c)
    
def cloudspec(size=12, color='b'):
    '''
    Make a cloud amount symbol legend.

    :param size: (*string*) The symbol size.
    :param color: (*color*) The symbol color.
    
    :returns: Cloud amount symbol legend.
    '''
    c = plotutil.getcolor(color)
    return DrawMeteoData.createCloudLegendScheme(size, c)
    
def masklayer(mobj, layers):
    '''
    Mask layers.
    
    :param mobj: (*layer or polgyons*) Mask object.
    :param layers: (*list*) The layers will be masked.       
    '''
    g_axes.masklayer(mobj, layers)
    draw_if_interactive()
        
def gifanimation(filename, repeat=0, delay=1000):
    """
    Create a gif animation file
    
    :param: repeat: (*int, Default 0*) Animation repeat time number. 0 means repeat forever.
    :param: delay: (*int, Default 1000*) Animation frame delay time with units of millsecond.
    
    :returns: Gif animation object.
    """
    encoder = AnimatedGifEncoder()
    encoder.setRepeat(repeat)
    encoder.setDelay(delay)
    encoder.start(filename)
    return encoder

def gifaddframe(animation, width=None, height=None, dpi=None):
    """
    Add a frame to an gif animation object
    
    :param animation: Gif animation object
    :param width: (*int*) Image width
    :param height: (*int*) Image height
    :param dpi: (*int*) Image resolution
    """
    #chartpanel.paintGraphics()
    if dpi is None:
        if width is None or height is None:
            animation.addFrame(g_figure.paintViewImage())
        else:
            animation.addFrame(g_figure.paintViewImage(width, height))
    else:
        if width is None or height is None:
            animation.addFrame(g_figure.paintViewImage(dpi))
        else:
            animation.addFrame(g_figure.paintViewImage(width, height, dpi))
    
def giffinish(animation):
    """
    Finish a gif animation object and write gif animation image file
    
    :param animation: Gif animation object
    """
    animation.finish()
        
def clear():
    """
    Clear all variables.
    """
    migl.milapp.delVariables()
    
def gc_collect():
    '''
    Clear variables and release memory
    '''
    clear()
    import gc
    gc.collect()