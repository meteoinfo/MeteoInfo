# coding=utf-8
# -----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2014-12-26
# Purpose: MeteoInfoLab plot module
# Note: Jython
# -----------------------------------------------------

from javax.swing import WindowConstants
from org.meteoinfo.chart import Location
from org.meteoinfo.chart.form import ChartForm
from org.meteoinfo.chart.jogl import JOGLUtil
from org.meteoinfo.chart.plot import Plot2D, MapPlot, Plot3D
from org.meteoinfo.geo.legend import LegendManage
from org.meteoinfo.geo.meteodata import DrawMeteoData
from org.meteoinfo.geometry.legend import LegendScheme, LegendType
from org.meteoinfo.geometry.shape import ShapeTypes
from org.meteoinfo.image import AnimatedGifEncoder

import datetime
import os
import functools
import mipylib.migl as migl
import mipylib.miutil as miutil
from mipylib.numeric.core import NDArray, DimArray
import plotutil
from ._axes import Axes, PolarAxes
from ._axes3d import Axes3D
from ._axes3dgl import Axes3DGL, MapAxes3D, EarthAxes3D
from ._figure import Figure
from ._glfigure import GLFigure
from ._mapaxes import MapAxes
import docstring

## Global ##
batchmode = False
isinteractive = False
g_figure = None
g_axes = None

__all__ = [
    'annotate', 'antialias', 'arrow', 'arrowline', 'axes', 'axes3d', 'axes3dgl', 'axesm', 'caxes', 'axis',
    'axism', 'bar', 'bar3', 'barh', 'barbs', 'barbsm', 'bgcolor', 'box',
    'boxplot', 'windrose', 'cla', 'clabel', 'clc', 'clear', 'clf', 'cll', 'cloudspec', 'colorbar', 'contour',
    'contourf',
    'contourfm', 'contourm', 'contourfslice', 'contourslice', 'delfig', 'draw', 'draw_if_interactive', 'errorbar',
    'figure', 'glfigure', 'figsize', 'patch', 'rectangle', 'fill', 'fill_between', 'fill_betweenx', 'fimplicit3',
    'webmap', 'gca', 'gcf', 'gc_collect', 'geoshow', 'get_figure', 'gifaddframe', 'gifanimation', 'giffinish',
    'grid', 'gridshow', 'gridshowm', 'hist', 'imshow', 'imshowm', 'isosurface', 'legend', 'left_title', 'lighting',
    'loglog', 'makecolors',
    'makelegend', 'makesymbolspec', 'masklayer', 'material', 'mesh', 'particles', 'pcolor', 'pcolorm', 'pie', 'plot',
    'plot3', 'plotm', 'quiver', 'quiver3',
    'quiverkey', 'quiverm', 'readlegend', 'right_title', 'refresh', 'savefig', 'savefig_jpeg', 'scatter', 'scatter3',
    'scatterm',
    'semilogx', 'semilogy', 'show', 'slice3', 'stationmodel', 'stem', 'stem3', 'step', 'streamplot', 'streamplot3',
    'streamplotm', 'streamslice', 'subplot', 'subplots', 'suptitle', 'supxlabel', 'supylabel',
    'surf', 'taylor_diagram', 'text', 'text3', 'title', 'trisurf', 'twinx', 'twiny', 'violinplot', 'volumeplot',
    'weatherspec',
    'xaxis',
    'xlabel', 'xlim', 'xreverse', 'xticks', 'yaxis', 'ylabel', 'ylim', 'yreverse', 'yticks', 'zaxis', 'zlabel', 'zlim',
    'zticks',
    'isinteractive'
]


def _copy_docstring_and_deprecators(method, func=None):
    if func is None:
        return functools.partial(_copy_docstring_and_deprecators, method)
    decorators = [docstring.copy(method)]
    while getattr(method, "__wrapped__", None) is not None:
        method = method.__wrapped__
    for decorator in decorators[::-1]:
        func = decorator(func)
    return func


def gcf():
    """
    Get current figure.

    :return: Current figure.
    """
    return g_figure


def gca():
    """
    Get current axes

    :return: Current axes
    """
    return g_axes


def figsize():
    """
    Get current figure size.
    
    :returns: Figure width and height.    
    """
    if g_figure is None:
        return None
    else:
        width = g_figure.getFigureWidth()
        height = g_figure.getFigureHeight()
        return width, height


def draw_if_interactive():
    """
    Draw current figure if is interactive model.
    """
    if isinteractive:
        g_figure.paintGraphics()


def draw():
    """
    Draw the current figure.
    """
    g_figure.paintGraphics()


@_copy_docstring_and_deprecators(Axes.plot)
def plot(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.ndim == 3:
            g_axes = axes()

    r = g_axes.plot(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.step)
def step(x, y, *args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian' and g_axes.axestype != 'polar':
            g_axes = axes()

    r = g_axes.step(x, y, *args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes3D.plot)
def plot3(x, y, z, *args, **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3D):
            g_axes = axes3d()

    r = g_axes.plot(x, y, z, *args, **kwargs)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.semilogy)
def semilogy(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()

    r = g_axes.semilogy(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.semilogx)
def semilogx(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()

    r = g_axes.semilogx(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.loglog)
def loglog(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()

    r = g_axes.loglog(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.errorbar)
def errorbar(x, y, yerr=None, xerr=None, fmt='', ecolor=None, elinewidth=None, capsize=None,
             **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian' and g_axes.axestype != 'polar':
            g_axes = axes()

    r = g_axes.errorbar(x, y, yerr, xerr, fmt, ecolor, elinewidth, capsize, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.bar)
def bar(x, height, width=0.8, bottom=None, align='center', data=None, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian' and g_axes.axestype != 'polar':
            g_axes = axes()

    r = g_axes.bar(x, height, width, bottom, align, data, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes3D.bar)
def bar3(*args, **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3D):
            g_axes = axes3d()

    r = g_axes.bar(*args, **kwargs)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.barh)
def barh(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian' and g_axes.axestype != 'polar':
            g_axes = axes()

    r = g_axes.barh(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.hist)
def hist(x, bins=10, range=None, density=False, cumulative=False,
         bottom=None, histtype='bar', align='mid',
         orientation='vertical', rwidth=None, log=False, **kwargs):
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
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.stem)
def stem(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()

    r = g_axes.stem(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes3D.stem)
def stem3(x, y, z, s=8, c='b', marker='o', alpha=None, linewidth=None,
          verts=None, **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3D):
            g_axes = axes3d()

    r = g_axes.stem(x, y, z, s, c, marker, alpha, linewidth, verts, **kwargs)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.scatter)
def scatter(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    # else:
    #     if g_axes.axestype != 'cartesian':
    #         g_axes = axes()

    r = g_axes.scatter(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes3D.scatter)
def scatter3(x, y, z, s=8, c='b', marker='o', **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3D):
            g_axes = axes3d()

    return g_axes.scatter(x, y, z, s, c, marker, **kwargs)


@_copy_docstring_and_deprecators(Axes.arrow)
def arrow(x, y, dx, dy, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()

    r = g_axes.arrow(x, y, dx, dy, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.arrowline)
def arrowline(x, y, dx=0, dy=0, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()

    r = g_axes.arrowline(x, y, dx, dy, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.annotate)
def annotate(s, xy, *args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()

    r = g_axes.annotate(s, xy, *args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.patch)
def fill(x, y=None, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()

    r = g_axes.patch(x, y, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.patch)
def patch(x, y=None, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    # else:
    # if g_axes.axestype != 'cartesian':
    # g_axes = axes()

    r = g_axes.patch(x, y, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.rectangle)
def rectangle(position, curvature=None, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()

    r = g_axes.rectangle(position, curvature, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.fill_between)
def fill_between(x, y1, y2=0, where=None, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()

    r = g_axes.fill_between(x, y1, y2, where, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.fill_betweenx)
def fill_betweenx(y, x1, x2=0, where=None, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()

    r = g_axes.fill_betweenx(y, x1, x2, where, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.pie)
def pie(x, explode=None, labels=None, colors=None, autopct=None, pctdistance=0.6, shadow=False,
        labeldistance=1.1, startangle=0, radius=None, wedgeprops=None, **kwargs):
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
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.boxplot)
def boxplot(x, sym=None, vert=True, positions=None, widths=None, color=None, showcaps=True, showfliers=True,
            showmeans=False, showmedians=True, meanline=False, medianline=True, boxprops=None,
            medianprops=None, meanprops=None, whiskerprops=None, capprops=None, flierprops=None):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()

    r = g_axes.boxplot(x, sym, vert, positions, widths, color, showcaps, showfliers, showmeans,
                       showmedians, meanline, medianline, boxprops, medianprops, meanprops, whiskerprops, capprops,
                       flierprops)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.violinplot)
def violinplot(dataset, positions=None, widths=0.5, boxwidth=0.01, boxprops=None, \
               whiskerprops=None, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes()

    r = g_axes.violinplot(dataset, positions, widths, boxwidth, boxprops, whiskerprops, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(PolarAxes.windrose)
def windrose(wd, ws, nwdbins=16, wsbins=None, degree=True, colors=None, cmap='matlab_jet', \
             alpha=0.7, rmax=None, rtickloc=None, rticks=None, rlabelpos=60, xticks=None, **kwargs):
    bottom = kwargs.pop('bottom', None)
    global g_axes
    if g_axes is None:
        g_axes = axes(polar=True, bottom=bottom)
    else:
        if not isinstance(g_axes, PolarAxes):
            g_axes = axes(polar=True, bottom=bottom)

    bars = g_axes.windrose(wd, ws, nwdbins, wsbins, degree, colors, cmap, alpha,
                           rmax, rtickloc, rticks, rlabelpos, xticks, **kwargs)
    if bars is not None:
        draw_if_interactive()

    return g_axes, bars


def figure(opengl=True, facecolor='w', figsize=None, newfig=True, **kwargs):
    """
    Creates a figure.

    :param opengl: (*bool*) Optional, use opengl or not. Default is `True`.
    :param facecolor: (*Color*) Optional, fill color of the figure. Default is ``w`` (white) .
    :param figsize: (*list*) Optional, width and height of the figure such as ``[600, 400]`` .
        Default is ``None`` with changeable size same as *Figures* window.
    :param newfig: (*boolean*) Optional, if creates a new figure. Default is ``True`` .
    """
    global g_figure
    if opengl:
        g_figure = GLFigure(figsize, facecolor=facecolor, **kwargs)
    else:
        g_figure = Figure(figsize, facecolor=facecolor, **kwargs)

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
    if batchmode:
        pass
    else:
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
    """
    Set figure background color
    
    :param color: (*Color*) Background color    
    """
    chart = g_figure.getChart()
    chart.setBackground(plotutil.getcolor(color))
    draw_if_interactive()


def caxes(ax=None):
    """
    Set or get current axes.
    
    :param ax: (*Axes or int*) The axes to be set as current axes. Is None, get current
        axes.
    """
    global g_axes
    chart = g_figure.getChart()
    if isinstance(ax, int):
        if g_figure is None:
            figure()

        g_axes = __get_axes(chart, ax)
        chart.setCurrentPlot(ax - 1)
    elif ax is not None:
        g_axes = ax
        chart.setCurrentPlot(chart.getPlotIndex(ax._axes))
    return g_axes


def subplot(nrows, ncols, plot_number, **kwargs):
    """
    Returen a subplot axes positioned by the given grid definition.
    
    :param nrows, nrows: (*int*) Whree *nrows* and *ncols* are used to notionally spli the 
        figure into ``nrows * ncols`` sub-axes.
    :param plot_number: (*int) Is used to identify the particular subplot that this function
        is to create within the notional gird. It starts at 1, increments across rows first
        and has a maximum of ``nrows * ncols`` .
    :param axestype: (*string*) Axes type [axes | 3d | map | polar].
    
    :returns: Current axes specified by ``plot_number`` .
    """
    if g_figure is None:
        figure()

    global g_axes
    g_axes = g_figure.subplot(nrows, ncols, plot_number, **kwargs)

    return g_axes


def subplots(nrows=1, ncols=1, position=None, sharex=False, sharey=False,
             wspace=None, hspace=None, axestype='Axes', **kwargs):
    """
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
    :param axestype: (*string*) Axes type [axes | 3d | map | polar].
    
    :returns: The figure and the axes tuple.
    """
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

    if g_figure is None:
        figure()

    ax = g_figure.add_axes(*args, **kwargs)

    g_axes = ax
    draw_if_interactive()
    return ax


def axesm(*args, **kwargs):
    """
    Add a map axes to the figure.
    
    :param projection: (*ProjectionInfo*) Optional, map projection, default is longlat projection.
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
    :param projection: (*str or ProjectionInfo*) If ``earth``, 3D earth axes will be created. If a
        ``ProjectionInfo``, 3D map axes will be created. Default is ``None`` with normal 3D axes.
    :param opengl: (*bool*) Using opengl backend or not. Default is ``True``.
    :param orthographic: (*bool*) Using orthographic orthographic or perspective view. Default is
        ``True``.
    :param aspect: (*str*) ['equal' | 'xy_equal' | None]. Default is ``None``.
    :param bgcolor: (*color*) Background color. Default is white.
    :param fgcolor: (*color*) Foreground color. Default is black.
    :param clip_plane: (*bool*) Clip plane in axes or not. Default is ``True``;
    :param axis: (*bool*) Draw axis or not. Default is ``True``.
    :param image: (*str*) Image file in ``map`` folder of the MeteoInfo. Only valid with 3D earth axes.
    
    :returns: The axes.
    """
    opengl = kwargs.pop('opengl', True)
    if opengl:
        projection = kwargs.get('projection', None)
        if projection is None:
            earth = kwargs.pop('earth', False)
            if earth:
                projection = 'earth'

        if projection is None:
            return axes3dgl(*args, **kwargs)
        elif projection == 'earth':
            return axes3d_earth(*args, **kwargs)
        else:
            return axes3d_map(*args, **kwargs)
    else:
        kwargs['axestype'] = '3d'
        return axes(*args, **kwargs)


def axes3dgl(*args, **kwargs):
    """
    Add a 3d axes with JOGL to the figure.
    
    :returns: The axes.
    """
    global g_axes

    ax = Axes3DGL(*args, **kwargs)
    g_axes = ax

    if not batchmode:
        if g_figure is None:
            figure(**kwargs)
        g_figure.add_axes(ax)

    draw_if_interactive()
    return ax


def axes3d_map(*args, **kwargs):
    """
    Add a map 3d axes with JOGL to the figure.

    :returns: The axes.
    """
    global g_axes

    ax = MapAxes3D(*args, **kwargs)
    g_axes = ax

    if not batchmode:
        if g_figure is None:
            figure(**kwargs)
        g_figure.add_axes(ax)

    draw_if_interactive()
    return ax


def axes3d_earth(*args, **kwargs):
    """
    Add an earth 3d axes with JOGL to the figure.

    :returns: The axes.
    """
    global g_axes

    ax = EarthAxes3D(*args, **kwargs)
    g_axes = ax

    if not batchmode:
        if g_figure is None:
            if 'facecolor' not in kwargs.keys():
                kwargs['facecolor'] = 'k'
            figure(**kwargs)
        g_figure.add_axes(ax)

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


@_copy_docstring_and_deprecators(Axes.xaxis)
def xaxis(ax=None, **kwargs):
    if ax is None:
        ax = g_axes
    ax.xaxis(**kwargs)
    draw_if_interactive()


@_copy_docstring_and_deprecators(Axes.yaxis)
def yaxis(ax=None, **kwargs):
    if ax is None:
        ax = g_axes
    ax.yaxis(**kwargs)
    draw_if_interactive()


@_copy_docstring_and_deprecators(Axes3D.zaxis)
def zaxis(ax=None, **kwargs):
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
        if not ax._axes.getAxis(loc).isDrawTickLabel():
            locs.append(loc)
    for loc in locs:
        axis = ax._axes.getAxis(loc)
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
        is deduced from the extension of the filename. Supported format: 'png', 'bmp',
        'jpg', 'gif', 'tif', 'eps' and 'pdf'.
    :param width: (*int*) Optional, width of the output figure with pixel units. Default
        is None, the output figure size is same as *figures* window.
    :param height: (*int*) Optional, height of the output figure with pixel units. Default
        is None, the output figure size is same as *figures* window.
    :param dpi: (*int*) Optional, figure resolution.
    :param sleep: (*int*) Optional, sleep seconds. For web map tiles loading.
    """
    global g_axes
    global g_figure

    if batchmode and isinstance(g_axes, Axes3DGL):
        if width is None:
            width = g_figure.getWidth() if not g_figure is None else 600
        if height is None:
            height = g_figure.getHeight() if not g_figure is None else 400
        if dpi is None:
            JOGLUtil.saveImage(g_axes._axes, fname, width, height)
        else:
            JOGLUtil.saveImage(g_axes._axes, fname, width, height, dpi)
    else:
        if fname.endswith('.eps') or fname.endswith('.pdf'):
            dpi = None

        if dpi is None:
            if (not width is None) and (not height is None):
                g_figure.saveImage(fname, width, height, sleep)
            else:
                if sleep is None:
                    g_figure.saveImage(fname)
                else:
                    g_figure.saveImageSleep(fname, sleep)
        else:
            if (not width is None) and (not height is None):
                g_figure.saveImage(fname, dpi, width, height, sleep)
            else:
                g_figure.saveImage(fname, dpi, sleep)


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
    # if (not width is None) and (not height is None):
    #    g_figure.setSize(width, height)
    # g_figure.paintGraphics()
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
    """
    Clear current axes.
    """
    global g_axes
    if not g_axes is None:
        if not g_figure is None:
            chart = g_figure.getChart()
            if not chart is None:
                g_figure.getChart().removePlot(g_axes._axes)
        g_axes = None
        draw_if_interactive()


# Delete current figure
def delfig():
    """
    Clear current figure.
    """
    if g_figure is None:
        return

    figureDock = migl.milapp.getFigureDock()
    figureDock.removeFigure(g_figure)

    global g_axes
    g_axes = None
    # draw_if_interactive()


# Clear current figure    
def clf():
    """
    Clear current figure.
    """
    if g_figure is None:
        return

    if isinstance(g_figure, GLFigure):
        delfig()
        return

    if g_figure.getChart() is None:
        return

    g_figure.getChart().clearAll()
    global g_axes
    g_axes = None
    draw_if_interactive()


def cll():
    """
    Clear last added plot object.
    """
    if not g_axes is None:
        if isinstance(g_axes, MapAxes):
            g_axes._axes.removeLastLayer()
        else:
            g_axes._axes.removeLastGraphic()
            g_axes._axes.setAutoExtent()
        draw_if_interactive()


def clc():
    """
    Clear command window.
    """
    if not migl.milapp is None:
        console = migl.milapp.getConsoleDockable().getConsole()
        console.getTextPane().setText('')


@_copy_docstring_and_deprecators(Axes.set_title)
def title(label, loc='center', fontname=None, fontsize=14, bold=True, color='black', **kwargs):
    r = g_axes.set_title(label, loc, fontname, fontsize, bold, color, **kwargs)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Figure.set_title)
def suptitle(label, fontname=None, fontsize=14, bold=True, color='black'):
    r = g_figure.set_title(label, fontname, fontsize, bold, color)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Figure.set_xlabel)
def supxlabel(label, **kwargs):
    r = g_figure.set_xlabel(label, **kwargs)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Figure.set_ylabel)
def supylabel(label, **kwargs):
    r = g_figure.set_ylabel(label, **kwargs)
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


@_copy_docstring_and_deprecators(Axes.set_xlabel)
def xlabel(label, **kwargs):
    g_axes.set_xlabel(label, **kwargs)
    draw_if_interactive()


@_copy_docstring_and_deprecators(Axes.set_ylabel)
def ylabel(label, **kwargs):
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
        if len(locs) > 0:
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
            g_axes._axes.addText(ctext, islonlat)
        else:
            g_axes._axes.addText(ctext)
    draw_if_interactive()
    return ctext


@_copy_docstring_and_deprecators(Axes3D.text)
def text3(x, y, z, s, zdir=None, **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3D):
            g_axes = axes3d()

    r = g_axes.text(x, y, z, s, zdir, **kwargs)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.axis)
def axis(limits):
    r = g_axes.axis(limits)
    if r is not None:
        draw_if_interactive()


@_copy_docstring_and_deprecators(MapAxes.axis)
def axism(limits=None, lonlat=True):
    r = g_axes.axis(limits, lonlat)
    if r is not None:
        draw_if_interactive()


@_copy_docstring_and_deprecators(Axes.grid)
def grid(b=None, **kwargs):
    g_axes.grid(b, **kwargs)
    draw_if_interactive()


@_copy_docstring_and_deprecators(Axes.set_xlim)
def xlim(xmin, xmax):
    g_axes.set_xlim(xmin, xmax)
    draw_if_interactive()


@_copy_docstring_and_deprecators(Axes.set_ylim)
def ylim(ymin, ymax):
    g_axes.set_ylim(ymin, ymax)
    draw_if_interactive()


@_copy_docstring_and_deprecators(Axes3D.set_zlim)
def zlim(zmin, zmax):
    g_axes.set_zlim(zmin, zmax)
    draw_if_interactive()


@_copy_docstring_and_deprecators(Axes.xreverse)
def xreverse():
    g_axes.xreverse()
    draw_if_interactive()


@_copy_docstring_and_deprecators(Axes.yreverse)
def yreverse():
    g_axes.yreverse()
    draw_if_interactive()


@_copy_docstring_and_deprecators(Axes.legend)
def legend(*args, **kwargs):
    r = g_axes.legend(*args, **kwargs)
    if r is not None:
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
        print('File not exists: ' + fn)
        return None


@_copy_docstring_and_deprecators(Axes.colorbar)
def colorbar(mappable=None, **kwargs):
    cax = kwargs.pop('cax', None)
    if cax is None:
        cax = g_axes
    cb = cax.colorbar(mappable, **kwargs)
    draw_if_interactive()
    return cb


@_copy_docstring_and_deprecators(Axes.imshow)
def imshow(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()

    r = g_axes.imshow(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.pcolor)
def pcolor(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()

    r = g_axes.pcolor(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.gridshow)
def gridshow(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    # else:
    #     if g_axes.axestype != 'cartesian':
    #         g_axes = axes()

    r = g_axes.gridshow(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.contour)
def contour(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()

    r = g_axes.contour(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.contourf)
def contourf(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()

    r = g_axes.contourf(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.quiver)
def quiver(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()

    r = g_axes.quiver(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes3D.quiver)
def quiver3(*args, **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3D):
            g_axes = axes3d()

    return g_axes.quiver(*args, **kwargs)


@_copy_docstring_and_deprecators(Axes.barbs)
def barbs(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()

    r = g_axes.barbs(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.streamplot)
def streamplot(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()

    r = g_axes.streamplot(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(MapAxes.scatter)
def scatterm(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()

    r = g_axes.scatter(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes3D.streamplot)
def streamplot3(*args, **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3DGL):
            g_axes = axes3dgl()

    r = g_axes.streamplot(*args, **kwargs)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes3DGL.streamslice)
def streamslice(*args, **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3DGL):
            g_axes = axes3dgl()

    r = g_axes.streamslice(*args, **kwargs)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(MapAxes.plot)
def plotm(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()

    r = g_axes.plot(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(MapAxes.stationmodel)
def stationmodel(smdata, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()

    r = g_axes.stationmodel(smdata, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(MapAxes.imshow)
def imshowm(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axesm()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()

    r = g_axes.imshow(*args, **kwargs)
    if r is not None:
        draw_if_interactive()

    return r


@_copy_docstring_and_deprecators(MapAxes.contour)
def contourm(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axesm()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()

    r = g_axes.contour(*args, **kwargs)
    if r is not None:
        draw_if_interactive()

    return r


@_copy_docstring_and_deprecators(MapAxes.contourf)
def contourfm(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axesm()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()

    r = g_axes.contourf(*args, **kwargs)
    if r is not None:
        draw_if_interactive()

    return r


@_copy_docstring_and_deprecators(MapAxes.pcolor)
def pcolorm(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axesm()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()

    r = g_axes.pcolor(*args, **kwargs)
    if r is not None:
        draw_if_interactive()

    return r


@_copy_docstring_and_deprecators(MapAxes.gridshow)
def gridshowm(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axesm()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()

    r = g_axes.gridshow(*args, **kwargs)
    if r is not None:
        draw_if_interactive()

    return r


@_copy_docstring_and_deprecators(MapAxes.quiver)
def quiverm(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axesm()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()

    r = g_axes.quiver(*args, **kwargs)
    if r is not None:
        draw_if_interactive()

    return r


@_copy_docstring_and_deprecators(Axes.quiverkey)
def quiverkey(*args, **kwargs):
    g_axes.quiverkey(*args, **kwargs)
    draw_if_interactive()


@_copy_docstring_and_deprecators(MapAxes.barbs)
def barbsm(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axesm()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()

    r = g_axes.barbs(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(MapAxes.streamplot)
def streamplotm(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axesm()
    else:
        if g_axes.axestype != 'map':
            g_axes = axesm()

    r = g_axes.streamplot(*args, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes.clabel)
def clabel(layer, **kwargs):
    """
    Add contour layer labels.
    
    :param layer: (*MILayer*) The contour layer.
    :param fontname, fontsize: The font arguments.
    :param color: (*color*) The label color. Default is ``None``, the label color will be set as
        same as color of the line.
    :param dynamic: (*boolean*) Draw labels dynamic or not. Default is ``True``.
    :param drawshadow: (*boolean*) Draw shadow under labels or not.
    :param fieldname: (*string*) The field name used for label.
    :param xoffset: (*int*) X offset of the labels.
    :param yoffset: (int*) Y offset of the labels.
    :param avoidcoll: (*boolean*) Avoid labels collision or not.
    """
    g_axes.clabel(layer, **kwargs)
    draw_if_interactive()


@_copy_docstring_and_deprecators(MapAxes.webmap)
def webmap(provider='OpenStreetMap', zorder=0):
    layer = g_axes.webmap(provider, zorder)
    draw_if_interactive()
    return layer


@_copy_docstring_and_deprecators(MapAxes.geoshow)
def geoshow(*args, **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axesm()
    else:
        if not isinstance(g_axes, (MapAxes, Axes3D)):
            g_axes = axesm()

    r = g_axes.geoshow(*args, **kwargs)
    if r is not None:
        draw_if_interactive()

    return r


@_copy_docstring_and_deprecators(Axes.taylor_diagram)
def taylor_diagram(stddev, correlation, std_max=1.65, labels=None, ref_std=1., colors=None,
                   **kwargs):
    global g_axes
    if g_figure is None:
        figure()

    if g_axes is None:
        g_axes = axes()
    else:
        if g_axes.axestype != 'cartesian':
            g_axes = axes(position=[0.13, 0.11, 0.775, 0.75])

    r = g_axes.taylor_diagram(stddev, correlation, std_max, labels, ref_std, colors, **kwargs)
    if r is not None:
        draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes3DGL.set_lighting)
def lighting(enable=True, **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3DGL):
            g_axes = axes3dgl()

    g_axes.set_lighting(enable, **kwargs)
    draw_if_interactive()


@_copy_docstring_and_deprecators(Axes3DGL.set_material)
def material(mvalues):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3DGL):
            g_axes = axes3dgl()

    g_axes.set_material(mvalues)
    draw_if_interactive()


@_copy_docstring_and_deprecators(Axes3D.mesh)
def mesh(*args, **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3D):
            g_axes = axes3d()

    r = g_axes.mesh(*args, **kwargs)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes3D.surf)
def surf(*args, **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3D):
            g_axes = axes3d()

    r = g_axes.surf(*args, **kwargs)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes3DGL.trisurf)
def trisurf(T, x, y, z, normal=None, **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3DGL):
            g_axes = axes3dgl()

    r = g_axes.trisurf(T, x, y, z, normal, **kwargs)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes3DGL.slice)
def slice3(*args, **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3DGL):
            g_axes = axes3dgl()

    r = g_axes.slice(*args, **kwargs)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes3DGL.contourslice)
def contourslice(*args, **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3DGL):
            g_axes = axes3dgl()

    r = g_axes.contourslice(*args, **kwargs)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes3DGL.contourfslice)
def contourfslice(*args, **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3DGL):
            g_axes = axes3dgl()

    r = g_axes.contourfslice(*args, **kwargs)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes3DGL.isosurface)
def isosurface(*args, **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3DGL):
            g_axes = axes3dgl()

    r = g_axes.isosurface(*args, **kwargs)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes3DGL.fimplicit3)
def fimplicit3(f, interval=[-5., 5.], mesh_density=35, *args, **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3DGL):
            g_axes = axes3dgl()

    r = g_axes.fimplicit3(f, interval, mesh_density, *args, **kwargs)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes3DGL.particles)
def particles(*args, **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3DGL):
            g_axes = axes3dgl()

    r = g_axes.particles(*args, **kwargs)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(Axes3DGL.volumeplot)
def volumeplot(*args, **kwargs):
    global g_axes
    if g_axes is None:
        g_axes = axes3d()
    else:
        if not isinstance(g_axes, Axes3DGL):
            g_axes = axes3dgl()

    r = g_axes.volumeplot(*args, **kwargs)
    draw_if_interactive()
    return r


@_copy_docstring_and_deprecators(plotutil.makecolors)
def makecolors(n, cmap='matlab_jet', reverse=False, alpha=None, start=None, stop=None):
    return plotutil.makecolors(n, cmap, reverse, alpha, start, stop)


@_copy_docstring_and_deprecators(plotutil.makelegend)
def makelegend(source, **kwargs):
    return plotutil.makelegend(source, **kwargs)


def makesymbolspec(geometry, *args, **kwargs):
    """
    Make a legend.
    
    :param geometry: (*string*) Geometry type. [point | line | polygon].
    :param levels: (*array_like*) Value levels. Default is ``None``, not used.
    :param colors: (*list*) Colors. Default is ``None``, not used.
    :param legend break parameter maps: (*map*) Legend breaks.
    :param field: (*string*) The field to be used in the legend.
    
    :returns: Created legend.
    """
    shapetype = ShapeTypes.IMAGE
    if geometry == 'point':
        shapetype = ShapeTypes.POINT
    elif geometry == 'line':
        shapetype = ShapeTypes.POLYLINE
    elif geometry == 'polygon':
        shapetype = ShapeTypes.POLYGON

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
            ls.setLegendType(LegendType.UNIQUE_VALUE)
        else:
            ls.setLegendType(LegendType.GRADUATED_COLOR)

    return ls


def weatherspec(weather='all', size=20, color='b'):
    """
    Make a weather symbol legend.
    
    :param weather: (*string or list*) The weather index list. Defaul is ``all``, used all weathers.
    :param size: (*string*) The weather symbol size.
    :param color: (*color*) The weather symbol color.
    
    :returns: Weather symbol legend.
    """
    if isinstance(weather, str):
        wlist = DrawMeteoData.getWeatherTypes(weather)
    else:
        wlist = weather
    c = plotutil.getcolor(color)
    return DrawMeteoData.createWeatherLegendScheme(wlist, size, c)


def cloudspec(size=12, color='b'):
    """
    Make a cloud amount symbol legend.

    :param size: (*string*) The symbol size.
    :param color: (*color*) The symbol color.
    
    :returns: Cloud amount symbol legend.
    """
    c = plotutil.getcolor(color)
    return DrawMeteoData.createCloudLegendScheme(size, c)


@_copy_docstring_and_deprecators(MapAxes.masklayer)
def masklayer(mobj, layers):
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
    Add a frame to a gif animation object
    
    :param animation: Gif animation object
    :param width: (*int*) Image width
    :param height: (*int*) Image height
    :param dpi: (*int*) Image resolution
    """
    # chartpanel.paintGraphics()
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
    """
    Clear variables and release memory
    """
    clear()
    import gc
    gc.collect()


def refresh():
    """
    Refresh the figure
    """
    migl.milapp.getFigureDock().setVisible(False)
    migl.milapp.getFigureDock().setVisible(True)
