#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2017-7-27
# Purpose: MeteoInfo plotutil module in plotlib package
# Note: Jython
#-----------------------------------------------------

import os
import datetime

from org.meteoinfo.legend import LineStyles, HatchStyle, ColorBreak, PointBreak, PolylineBreak, \
    PolygonBreak, ArrowBreak, ArrowLineBreak, ArrowPolygonBreak, StreamlineBreak, LegendManage, \
    PointStyle, MarkerType, LegendScheme
from org.meteoinfo.global.colors import ColorUtil, ColorMap
from org.meteoinfo.shape import ShapeTypes
from org.meteoinfo.chart import ChartText

from java.awt import Color, Font

from mipylib.numeric.core import NDArray, DimArray
import mipylib.numeric as np
import mipylib.miutil as miutil
import mipylib.migl as migl

def getplotdata(data):
    if isinstance(data, NDArray):
        if data.dtype == np.dtype.date:
            return data.astype('double')._array
        return data.asarray()
    elif isinstance(data, (list, tuple)):
        if isinstance(data[0], datetime.datetime):
            dd = []
            for d in data:
                v = miutil.date2num(d)
                dd.append(v)
            return np.array(dd)._array
        else:
            return np.array(data)._array
    else:
        return np.array([data])._array
        
def getfont(fontdic, **kwargs):
    basefont = kwargs.pop('basefont', None)
    if basefont is None:
        name = 'Arial'
        size = 14
        bold = False
        italic = False
    else:
        name = basefont.getName()
        size = basefont.getSize()
        bold = basefont.isBold()
        italic = basefont.isItalic()
    name = fontdic.pop('name', name)
    size = fontdic.pop('size', size)
    bold = fontdic.pop('bold', bold)
    italic = fontdic.pop('italic', italic)
    if bold:
        if italic:
            font = Font(name, Font.BOLD | Font.ITALIC, size)
        else:
            font = Font(name, Font.BOLD, size)
    else:
        if italic:
            font = Font(name, Font.ITALIC, size)
        else:
            font = Font(name, Font.PLAIN, size)
    return font
    
def getfont_1(**kwargs):
    fontname = kwargs.pop('fontname', 'Arial')
    fontsize = kwargs.pop('fontsize', 14)
    bold = kwargs.pop('bold', False)
    if bold:
        font = Font(fontname, Font.BOLD, fontsize)
    else:
        font = Font(fontname, Font.PLAIN, fontsize)
    return font

def getcolor(style, alpha=None):
    if style is None:
        return None
        
    if isinstance(style, Color):
        c = style
        if not alpha is None:
            alpha = (int)(alpha * 255)
            c = Color(c.getRed(), c.getGreen(), c.getBlue(), alpha)
        return c
        
    c = Color.black
    if isinstance(style, str):
        if style == 'red' or style == 'r':
            c = Color.red
        elif style == 'black' or style == 'k':
            c = Color.black
        elif style == 'blue' or style == 'b':
            c = Color.blue
        elif style == 'green' or style == 'g':
            c = Color.green
        elif style == 'white' or style == 'w':
            c = Color.white
        elif style == 'yellow' or style == 'y':
            c = Color.yellow
        elif style == 'gray':
            c = Color.gray
        elif style == 'lightgray':
            c = Color.lightGray
        elif style == 'cyan' or style == 'c':
            c = Color.cyan
        elif style == 'magenta' or style == 'm':
            c = Color.magenta
        elif style == 'pink' or style == 'p':
            c = Color.pink
        else:
            try:
                c = Color.decode(style)
            except:
                c = None
                print('Not a valid color: {}'.format(style))
                return c
    elif isinstance(style, (tuple, list)):
        if len(style) == 3:
            c = Color(style[0], style[1], style[2])
        else:
            c = Color(style[0], style[1], style[2], style[3])
    
    if not alpha is None:
        alpha = (int)(alpha * 255)
        c = Color(c.getRed(), c.getGreen(), c.getBlue(), alpha)
    
    return c
    
def getcolor_style(style):
    c = Color.black
    rr = None
    if 'r' in style:
        c = Color.red
        rr = 'r'
    elif 'k' in style:
        c = Color.black
        rr = 'k'
    elif 'b' in style:
        c = Color.blue
        rr = 'b'
    elif 'g' in style:
        c = Color.green
        rr = 'g'
    elif 'w' in style:
        c = Color.white
        rr = 'w'
    elif 'c' in style:
        c = Color.cyan
        rr = 'c'
    elif 'm' in style:
        c = Color.magenta
        rr = 'm'
    elif 'y' in style:
        c = Color.yellow
        rr = 'y'
    elif 'p' in style:
        c = Color.pink
        rr = 'p'
    
    if not rr is None:
        style = style.replace(rr, '')
    return c, style
    
def getcolors(cs, alpha=None):
    colors = []
    if isinstance(cs, (tuple, list, NDArray)):
        if isinstance(cs[0], int):
            colors.append(getcolor(cs, alpha))
        else:            
            for c in cs:
                colors.append(getcolor(c, alpha))
    else:
        colors.append(getcolor(cs, alpha))
    return colors
    
def getcolormap(**kwargs):
    colors = kwargs.pop('colors', None)
    issingle = False
    if colors is None:
        colors = kwargs.pop('color', None)
        issingle = True
    if not colors is None:
        if issingle or isinstance(colors, str):
            c = getcolor(colors)
            cmap = ColorMap(c)
        else:
            cs = []
            for cc in colors:
                c = getcolor(cc)
                cs.append(c)
            cmap = ColorMap(cs)
    else:
        cmapstr = kwargs.pop('cmap', 'matlab_jet')
        if cmapstr is None:
            cmapstr = 'matlab_jet'
        fn = cmapstr + '.rgb'
        fn = os.path.join(migl.get_cmap_folder(), fn)
        if not os.path.exists(fn):
            raise IOError('cmap file not exists: %s' % fn)
        alpha = kwargs.pop('alpha', None)
        if alpha is None:
            cmap = ColorUtil.loadColorMap(fn)
        else:
            alpha = (int)(alpha * 255)
            cmap = ColorUtil.loadColorMap(fn, alpha)
    reverse = kwargs.pop('cmapreverse', False)
    if reverse:
        cmap.reverse()
    return cmap
    
def makecolors(n, cmap='matlab_jet', reverse=False, alpha=None, start=None, stop=None):
    '''
    Make colors.
    
    :param n: (*int*) Colors number.
    :param cmap: (*string*) Color map name. Default is ``matlab_jet``.
    :param reverse: (*boolean*) Reverse the colors or not. Default is ``False``.
    :param alpha: (*float*) Alpha value (0 - 1) of the colors. Default is ``None``.
    :param start: (*int*) Start color index. Default is ``None``.
    :param stop: (*int*) Stop color index. Default is ``None``.

    :returns: (*list*) Created colors.
    '''
    if isinstance(n, list):
        cols = getcolors(n, alpha)
    else:
        #ocmap = ColorUtil.getColorMap(cmap)
        ocmap = getcolormap(cmap=cmap)
        if reverse:
            ocmap.reverse()
        if start is None and stop is None:
            if alpha is None:
                cols = ocmap.getColorList(n)
            else:
                alpha = (int)(alpha * 255)
                cols = ocmap.getColorListAlpha(n, alpha)
        else:
            if start is None:
                start = 0
            if stop is None:
                stop = ocmap.getColorCount() - 1
            if alpha is None:
                cols = ocmap.getColorList(n, start, stop)
            else:
                alpha = (int)(alpha * 255)
                cols = ocmap.getColorListAlpha(n, alpha, start, stop)
    return list(cols)
    
def getpointstyle(style):
    if style is None:
        return None
        
    pointStyle = None
    if 'do' in style:
        pointStyle = PointStyle.DOUBLE_CIRCLE
    elif 'os' in style:
        pointStyle = PointStyle.CIRCLE_STAR
    elif 'o' in style:
        pointStyle = PointStyle.Circle
    elif '.' in style:
        pointStyle = PointStyle.Circle
    elif 'D' in style:
        pointStyle = PointStyle.Diamond
    elif '+' in style:
        pointStyle = PointStyle.Plus
    elif 'm' in style:
        pointStyle = PointStyle.Minus
    elif 's' in style:
        pointStyle = PointStyle.Square
    elif 'S' in style:
        pointStyle = PointStyle.Star
    elif '*' in style:
        pointStyle = PointStyle.StarLines
    elif '^' in style:
        pointStyle = PointStyle.UpTriangle
    elif 'x' in style:
        pointStyle = PointStyle.XCross
    elif 'p' in style:
        pointStyle = PointStyle.Pentagon
    
    return pointStyle
    
def getlinestyle(style):
    if style is None:
        return None
        
    lineStyle = None
    if style[0].isalpha():
        style = style.upper()
        lineStyle = LineStyles.valueOf(style)
    else:
        if '--' in style:
            lineStyle = LineStyles.DASH
        elif ':' in style:
            lineStyle = LineStyles.DOT
        elif '-.' in style:
            lineStyle = LineStyles.DASHDOT
        elif '-' in style:
            lineStyle = LineStyles.SOLID
    
    return lineStyle
    
def getlinestyle_1(style):
    if style is None:
        return None
        
    lineStyle = None
    rr = None
    if '--' in style:
        lineStyle = LineStyles.DASH
        rr = '--'
    elif ':' in style:
        lineStyle = LineStyles.DOT
        rr = ':'
    elif '-.' in style:
        lineStyle = LineStyles.DASHDOT
        rr = '-.'
    elif '-' in style:
        lineStyle = LineStyles.SOLID
        rr = '-'
    
    if not rr is None:
        style = style.replace(rr, '')
    return lineStyle, style
    
def gethatch(h):
    if h is None:
        return h
    else:
        return HatchStyle.getStyle(h)
    
def getplotstyle(style, caption, **kwargs):    
    linewidth = kwargs.pop('linewidth', 1.0)
    if style is None:
        color = kwargs.pop('color', 'red')
        c = getcolor(color)
    else:
        c, style = getcolor_style(style)
    lineStyle, style = getlinestyle_1(style)
    pointStyle = getpointstyle(style)    
    if not pointStyle is None:
        fill = kwargs.pop('fill', True)        
        if lineStyle is None:           
            pb = PointBreak()
            pb.setCaption(caption)
            if '.' in style:
                pb.setSize(4)
                pb.setDrawOutline(False)
            else:
                pb.setSize(8)
            pb.setStyle(pointStyle)
            pb.setDrawFill(fill)
            if not c is None:
                pb.setColor(c)      
            edgecolor = kwargs.pop('edgecolor', pb.getColor())
            edgecolor = getcolor(edgecolor)
            pb.setOutlineColor(edgecolor)
            return pb
        else:
            plb = PolylineBreak()
            plb.setCaption(caption)
            plb.setWidth(linewidth)
            plb.setStyle(lineStyle)
            plb.setDrawSymbol(True)
            plb.setSymbolStyle(pointStyle)
            plb.setFillSymbol(fill)
            interval = kwargs.pop('markerinterval', 1)
            plb.setSymbolInterval(interval)
            if not c is None:
                plb.setColor(c)
            markersize = kwargs.pop('markersize', None)
            if not markersize is None:
                plb.setSymbolSize(markersize)
            markercolor = kwargs.pop('markeredgecolor', plb.getColor())
            markercolor = getcolor(markercolor)
            plb.setSymbolColor(markercolor)
            markerfillcolor = kwargs.pop('markerfacecolor', markercolor)
            markerfillcolor = getcolor(markerfillcolor)
            plb.setSymbolFillColor(markerfillcolor)
            return plb
    else:
        plb = PolylineBreak()
        plb.setCaption(caption)
        plb.setWidth(linewidth)
        if not c is None:
            plb.setColor(c)
        if not lineStyle is None:
            plb.setStyle(lineStyle)
        return plb
        
def getlegendbreak(geometry, **kwargs):
    fill = True
    color = None
    if kwargs.has_key('color'):
        color = kwargs.pop('color')
        if color is None:
            fill = False
        else:
            color = getcolor(color)
    else:
        if kwargs.has_key('facecolor'):
            color = kwargs.pop('facecolor')
            if color is None:
                fill = False
            else:
                color = getcolor(color)

    if geometry == 'point':
        lb = PointBreak()        
        marker = kwargs.pop('marker', 'o')
        if marker == 'image':
            imagepath = kwargs.pop('imagepath', None)
            if not imagepath is None:
                lb.setMarkerType(MarkerType.Image)
                lb.setImagePath(imagepath)
        elif marker == 'font':
            fontname = kwargs.pop('fontname', 'Weather')
            lb.setMarkerType(MarkerType.Character)
            lb.setFontName(fontname)
            charindex = kwargs.pop('charindex', 0)
            lb.setCharIndex(charindex)
        else:
            pstyle = getpointstyle(marker)
            lb.setStyle(pstyle)
        if not color is None:
            lb.setColor(color)
        size = kwargs.pop('size', 6)
        lb.setSize(size)
        edge = True
        edgecolor = None
        if kwargs.has_key('edgecolor'):
            edgecolor = kwargs.pop('edgecolor')
            if edgecolor is None:
                edge = False
            else:
                edgecolor = getcolor(edgecolor)
        if not edgecolor is None:
            lb.setOutlineColor(edgecolor)
        linewith = kwargs.pop('linewidth', None)
        edgesize = kwargs.pop('edgesize', linewith)
        if not edgesize is None:
            lb.setOutlineSize(edgesize)
        fill = kwargs.pop('fill', fill)
        lb.setDrawFill(fill)
        edge = kwargs.pop('edge', edge)
        lb.setDrawOutline(edge)
    elif geometry == 'line':
        lb = PolylineBreak()
        if not color is None:
            lb.setColor(color)
        size = kwargs.pop('size', 1.0)
        size = kwargs.pop('linewidth', size)
        lb.setWidth(size)
        lsobj = kwargs.pop('linestyle', '-')
        linestyle = getlinestyle(lsobj)
        lb.setStyle(linestyle)
        marker = kwargs.pop('marker', None)
        if not marker is None:
            pstyle = getpointstyle(marker)
            lb.setDrawSymbol(True)
            lb.setSymbolStyle(pstyle)
            markersize = kwargs.pop('markersize', None)
            if not markersize is None:
                lb.setSymbolSize(markersize)
            markercolor = kwargs.pop('markercolor', None)
            if not markercolor is None:
                markercolor = getcolor(markercolor)
                lb.setSymbolColor(markercolor)
            fillcolor = kwargs.pop('makerfillcolor', None)
            if not fillcolor is None:
                lb.setFillSymbol(True)
                lb.setSymbolFillColor(getcolor(fillcolor))
            else:
                lb.setSymbolFillColor(markercolor)
            interval = kwargs.pop('markerinterval', 1)
            lb.setSymbolInterval(interval)
    elif geometry == 'polygon':
        lb = PolygonBreak()
        edge = True
        edgecolor = None
        if kwargs.has_key('edgecolor'):
            edgecolor = kwargs.pop('edgecolor')
            if edgecolor is None:
                edge = False
            else:
                edgecolor = getcolor(edgecolor)
        if not edgecolor is None:
            lb.setOutlineColor(edgecolor)
        fill = kwargs.pop('fill', fill)
        lb.setDrawFill(fill)
        edge = kwargs.pop('edge', edge)
        lb.setDrawOutline(edge)
        size = kwargs.pop('size', None)
        size = kwargs.pop('linewidth', size)
        size = kwargs.pop('edgesize', size)
        if not size is None:
            lb.setOutlineSize(size)
        hatch = kwargs.pop('hatch', None)
        hatch = gethatch(hatch) 
        hatchsize = kwargs.pop('hatchsize', None)
        bgcolor = kwargs.pop('bgcolor', None)
        bgcolor = getcolor(bgcolor)
        if not hatch is None:
            lb.setStyle(hatch)
            if not bgcolor is None:
                lb.setBackColor(bgcolor)
            if not hatchsize is None:
                lb.setStyleSize(hatchsize)
    else:
        lb = ColorBreak()
    caption = kwargs.pop('caption', None)
    if not caption is None:
        lb.setCaption(caption) 
    if not color is None:
        lb.setColor(color)
    alpha = kwargs.pop('alpha', None)
    if not alpha is None:
        lb.setColor(getcolor(lb.getColor(), alpha))
    value = kwargs.pop('value', None)
    isunique = True
    if not value is None:
        if isinstance(value, (tuple, list)):
            lb.setStartValue(value[0])
            lb.setEndValue(value[1])
            isunique = False
        else:
            lb.setStartValue(value)
            lb.setEndValue(value)
    return lb, isunique
    
def getlegendscheme(args, min, max, **kwargs):
    ls = kwargs.pop('symbolspec', None)
    if ls is None:
        cmap = getcolormap(**kwargs)        
        if len(args) > 0:
            level_arg = args[0]
            if isinstance(level_arg, int):
                cn = level_arg
                ls = LegendManage.createLegendScheme(min, max, cn, cmap)
            else:
                if isinstance(level_arg, NDArray):
                    level_arg = level_arg.aslist()
                ls = LegendManage.createLegendScheme(min, max, level_arg, cmap)
        else:    
            ls = LegendManage.createLegendScheme(min, max, cmap)
        # ecobj = kwargs.pop('edgecolor', None)
        # if not ecobj is None:
        #     edgecolor = getcolor(ecobj)
        #     ls = ls.convertTo(ShapeTypes.Polygon)
        #     for lb in ls.getLegendBreaks():
        #         lb.setDrawOutline(True)
        #         lb.setOutlineColor(edgecolor)
    return ls
    
def setlegendscheme(ls, **kwargs):
    st = ls.getShapeType()
    if st == ShapeTypes.Point:
        setlegendscheme_point(ls, **kwargs)
    elif st == ShapeTypes.Polyline:
        setlegendscheme_line(ls, **kwargs)
    elif st == ShapeTypes.Polygon:
        setlegendscheme_polygon(ls, **kwargs)
    else:
        setlegendscheme_image(ls, **kwargs)

def setlegendscheme_image(ls, **kwargs):
    cobj = kwargs.pop('color', None)
    alpha = kwargs.pop('alpha', None)
    for lb in ls.getLegendBreaks():
        if not cobj is None:
            color = getcolor(cobj)
            lb.setColor(color)   
        if not alpha is None:
            c = lb.getColor()
            c = getcolor(c, alpha)
            lb.setColor(c)
        
    return ls
        
def setlegendscheme_point(ls, **kwargs):
    ls = ls.convertTo(ShapeTypes.Point)  
    sizes = kwargs.get('size', None)
    colors = kwargs.get('colors', None)
    edgecolors = kwargs.get('edgecolors', None)
    marker = kwargs.get('marker', None)
    i = 0
    for lb in ls.getLegendBreaks():
        if isinstance(sizes, (list, tuple, NDArray)): 
            kwargs['size'] = sizes[i]
        if isinstance(colors, (list, tuple, NDArray)):
            kwargs['color'] = colors[i]
        if isinstance(edgecolors, (list, tuple, NDArray)):
            kwargs['edgecolor'] = edgecolors[i]
        if isinstance(marker, (list, tuple, NDArray)):
            kwargs['marker'] = marker[i]
        setpointlegendbreak(lb, **kwargs)
        i += 1

    return ls
    
def setlegendscheme_arrow(ls, **kwargs):
    '''
    Set legend scheme as arrow breaks.
    
    :param ls: (*LegendScheme') Input legend scheme.
    
    :returns: (*LegendScheme*) Result legend scheme.
    '''
    ls = ls.convertTo(ShapeTypes.Point)  
    sizes = kwargs.get('size', None)
    colors = kwargs.get('colors', None)
    marker = kwargs.get('marker', None)    
    for i in range(ls.getBreakNum()):
        lb = ls.getLegendBreak(i)
        if isinstance(sizes, (list, tuple, NDArray)): 
            kwargs['size'] = sizes[i]
        if isinstance(colors, (list, tuple, NDArray)):
            kwargs['color'] = colors[i]
        if isinstance(marker, (list, tuple, NDArray)):
            kwargs['marker'] = marker[i]
        if not kwargs.has_key('edgecolor'):
            kwargs['edgecolor'] = None
        setpointlegendbreak(lb, **kwargs)
        lb = point2arrow(lb, **kwargs)
        ls.setLegendBreak(i, lb)

    return ls
    
def point2arrow(pb, **kwargs):
    '''
    Convert point break to arrow break.
    
    :param pb: (*PointBreak*) Point break.
    :param width: (*float*) Arrow line width.
    :param headwidth: (*float*) Arrow head width. Default is ``width*5``.
    :param headlength: (*float*) Arrow head length. 
    :param overhang: (*float*) fraction that the arrow is swept back (0 overhang means 
        triangular shape). Can be negative or greater than one.
    
    :returns: (*ArrowBreak*) Arrow break.
    '''
    arrowbreak = ArrowBreak(pb)
    width = kwargs.pop('width', 1.)
    arrowbreak.setWidth(width)
    headwidth = kwargs.pop('headwidth', width * 5.)
    arrowbreak.setHeadWidth(headwidth)
    headlength = kwargs.pop('headlength', headwidth * 1.5)
    arrowbreak.setHeadLength(headlength)
    overhang = kwargs.pop('overhang', None)
    if not overhang is None:
        arrowbreak.setOverhang(overhang)
    
    return arrowbreak
    
def line2arrow(lb, **kwargs):
    '''
    Convert linestring break to arrow line break.
    
    :param lb: (*PolylineBreak*) Linestring break.
    :param headwidth: (*float*) Arrow head width. Default is ``width*5``.
    :param headlength: (*float*) Arrow head length. 
    :param overhang: (*float*) fraction that the arrow is swept back (0 overhang means 
        triangular shape). Can be negative or greater than one.
    :param fillcolor: (*Color*) Arrow fill color.
    :param edgecolor: (*Color*) Arrow edge color.
    
    :returns: (*ArrowLineBreak*) Arrow line break.
    '''
    albreak = ArrowLineBreak(lb)
    headwidth = kwargs.pop('headwidth', lb.getWidth() * 5.)
    albreak.setArrowHeadWidth(headwidth)
    headlength = kwargs.pop('headlength', headwidth * 1.5)
    albreak.setArrowHeadLength(headlength)
    overhang = kwargs.pop('overhang', None)
    if not overhang is None:
        albreak.setArrowOverhang(overhang)
    if kwargs.has_key('fillcolor'):
        fillcolor = kwargs.pop('fillcolor')
        albreak.setArrowFillColor(getcolor(fillcolor))
    if kwargs.has_key('edgecolor'):
        edgecolor = kwargs.pop('edgecolor')
        albreak.setArrowOutlineColor(getcolor(edgecolor))
    
    return albreak
    
def line2stream(lb, **kwargs):
    '''
    Convert linestring break to stream line break.
    
    :param lb: (*PolylineBreak*) Linestring break.
    :param headwidth: (*float*) Arrow head width. Default is ``width*5``.
    :param headlength: (*float*) Arrow head length. 
    :param overhang: (*float*) fraction that the arrow is swept back (0 overhang means 
        triangular shape). Can be negative or greater than one.
    :param fillcolor: (*Color*) Arrow fill color.
    :param edgecolor: (*Color*) Arrow edge color.
    :param interval: (*int*) Arrow interval.
    
    :returns: (*StreamineBreak*) Stream line break.
    '''
    albreak = StreamlineBreak(lb)
    headwidth = kwargs.pop('headwidth', lb.getWidth() * 5.)
    albreak.setArrowHeadWidth(headwidth)
    headlength = kwargs.pop('headlength', headwidth * 1.5)
    albreak.setArrowHeadLength(headlength)
    overhang = kwargs.pop('overhang', None)
    if not overhang is None:
        albreak.setArrowOverhang(overhang)
    if kwargs.has_key('fillcolor'):
        fillcolor = kwargs.pop('fillcolor')
        albreak.setArrowFillColor(getcolor(fillcolor))
    if kwargs.has_key('edgecolor'):
        edgecolor = kwargs.pop('edgecolor')
        albreak.setArrowOutlineColor(getcolor(edgecolor))
    if kwargs.has_key('interval'):
        interval = kwargs.pop('interval')
        albreak.setInterval(interval)
    
    return albreak
    
def polygon2arrow(pb, **kwargs):
    '''
    Convert polygon break to arrow polygon break.
    
    :param pb: (*PolygonBreak*) Polygon break.
    :param width: (*float*) Arrow line width.
    :param headwidth: (*float*) Arrow head width. Default is ``width*3``.
    :param headlength: (*float*) Arrow head length. 
    :param overhang: (*float*) fraction that the arrow is swept back (0 overhang means 
        triangular shape). Can be negative or greater than one.
    
    :returns: (*ArrowPolygonBreak*) Arrow polygon break.
    '''
    arrowbreak = ArrowPolygonBreak(pb)
    width = kwargs.pop('width', 0.001)
    arrowbreak.setWidth(width)
    headwidth = kwargs.pop('headwidth', width * 3.)
    arrowbreak.setHeadWidth(headwidth)
    headlength = kwargs.pop('headlength', headwidth * 1.5)
    arrowbreak.setHeadLength(headlength)
    length_includes_head = kwargs.pop('length_includes_head', False)
    arrowbreak.setLengthIncludesHead(length_includes_head)
    overhang = kwargs.pop('overhang', None)
    if not overhang is None:
        arrowbreak.setOverhang(overhang)
    
    return arrowbreak
    
def setlegendscheme_line(ls, **kwargs):
    ls = ls.convertTo(ShapeTypes.Polyline)
    size = kwargs.pop('size', None)
    size = kwargs.pop('linewidth', size)
    lsobj = kwargs.pop('linestyle', None)
    linestyle = getlinestyle(lsobj)
    cobj = kwargs.pop('color', None)
    if cobj is None:
        color = None
    else:
        color = getcolor(cobj)    
    for lb in ls.getLegendBreaks():
        if not color is None:
            lb.setColor(color)
        if not linestyle is None:
            lb.setStyle(linestyle)
        if not size is None:
            lb.setSize(size)
    return ls
    
def setlegendscheme_polygon(ls, **kwargs):
    ls = ls.convertTo(ShapeTypes.Polygon)
    fill = True
    if kwargs.has_key('facecolor'):
        facecolor = kwargs['facecolor']
        if facecolor is None:
            fill = False
        else:
            facecolor = getcolor(facecolor)
    else:
        facecolor = None
    edge = True
    if kwargs.has_key('edgecolor'):
        edgecolor = kwargs['edgecolor']
        if edgecolor is None:
            edge = False
        else:
            edgecolor = getcolor(edgecolor)
    else:
        edgecolor = None
    edge = kwargs.pop('edge', edge)
    edgesize = kwargs.pop('edgesize', None)
    fill = kwargs.pop('fill', fill)
    alpha = kwargs.pop('alpha', None)
    hatch = kwargs.pop('hatch', None)
    hatch = gethatch(hatch) 
    hatchsize = kwargs.pop('hatchsize', None)
    bgcolor = kwargs.pop('bgcolor', None)
    bgcolor = getcolor(bgcolor)
    for lb in ls.getLegendBreaks():
        lb.setDrawFill(fill)
        if not facecolor is None:
            lb.setColor(facecolor)
        if not alpha is None:
            c = lb.getColor()
            c = getcolor(c, alpha)
            lb.setColor(c)
        if not edgesize is None:
            lb.setOutlineSize(edgesize)
        lb.setDrawOutline(edge)
        if not edgecolor is None:
            lb.setOutlineColor(edgecolor)
        if not hatch is None:
            lb.setStyle(hatch)
            if not bgcolor is None:
                lb.setBackColor(bgcolor)
            if not hatchsize is None:
                lb.setStyleSize(hatchsize)
    return ls
    
def setpointlegendbreak(lb, **kwargs):
    if kwargs.has_key('marker'):
        marker = kwargs['marker']
        if marker == 'image':
            if kwargs.has_key('imagepath'):
                lb.setMarkerType(MarkerType.Image)
                lb.setImagePath(kwargs['imagepath'])
        elif marker == 'font':
            fontname = 'Weather'
            if kwargs.has_key('fontname'):
                fontname = kwargs['fontname']
            lb.setMarkerType(MarkerType.Character)
            lb.setFontName(fontname)
            charindex = 0
            if kwargs.has_key('charindex'):
                charindex = kwargs['charindex']
            lb.setCharIndex(charindex)
        else:
            pstyle = getpointstyle(marker)
            lb.setStyle(pstyle)
    color = lb.getColor()
    if kwargs.has_key('color'):
        color = kwargs['color']
    elif kwargs.has_key('facecolor'):
        color = kwargs['facecolor']
    alpha = None
    if kwargs.has_key('alpha'):
        alpha = kwargs['alpha']
    if color is None:
        if alpha is None:
            lb.setDrawFill(False)
        else:
            color = getcolor(lb.getColor(), alpha)
            lb.setColor(color)
    else:
        color = getcolor(color, alpha)
        lb.setColor(color)
    if kwargs.has_key('size'):
        size = kwargs['size']
        lb.setSize(size)
    if kwargs.has_key('edgecolor'):
        ecobj = kwargs['edgecolor']
        if ecobj is None:
            lb.setDrawOutline(False)
        else:
            edgecolor = getcolor(ecobj)
            lb.setOutlineColor(edgecolor)
    if kwargs.has_key('fill'):
        lb.setDrawFill(kwargs['fill'])
    if kwargs.has_key('edge'):
        lb.setDrawOutline(kwargs['edge'])
    if kwargs.has_key('edgesize'):
        lb.setOutlineSize(kwargs['edgesize'])
    elif kwargs.has_key('linewidth'):
        lb.setOutlineSize(kwargs['linewidth'])
        
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
    
    :returns: (*ChartText*) text.
    """
    fontname = kwargs.pop('fontname', None)
    exfont = False
    if fontname is None:
        fontname = 'Arial'
    else:
        exfont = True
    fontsize = kwargs.pop('fontsize', 14)
    bold = kwargs.pop('bold', False)
    color = kwargs.pop('color', 'black')
    if bold:
        font = Font(fontname, Font.BOLD, fontsize)
    else:
        font = Font(fontname, Font.PLAIN, fontsize)
    c = getcolor(color)
    text = ChartText(s, font)
    text.setUseExternalFont(exfont)
    text.setColor(c)
    text.setX(x)
    text.setY(y)
    xshift = kwargs.pop('xshift', None)
    if not xshift is None:
        text.setXShift(xshift)
    yshift = kwargs.pop('yshift', None)
    if not yshift is None:
        text.setYShift(yshift)
    xalign = kwargs.pop('xalign', None)
    if not xalign is None:
        text.setXAlign(xalign)
    yalign = kwargs.pop('yalign', None)
    if not yalign is None:
        text.setYAlign(yalign)
    bbox = kwargs.pop('bbox', None)
    if not bbox is None:
        fill = bbox.pop('fill', None)
        if not fill is None:
            text.setFill(fill)
        facecolor = bbox.pop('facecolor', None)
        if not facecolor is None:
            facecolor = getcolor(facecolor)
            text.setFill(True)
            text.setBackground(facecolor)
        edge = bbox.pop('edge', None)
        if not edge is None:
            text.setDrawNeatline(edge)
        edgecolor = bbox.pop('edgecolor', None)
        if not edgecolor is None:
            edgecolor = getcolor(edgecolor)
            text.setNeatlineColor(edgecolor)
            text.setDrawNeatline(True)
        linewidth = bbox.pop('linewidth', None)
        if not linewidth is None:
            text.setNeatlineSize(linewidth)
            text.setDrawNeatline(True)
        gap = bbox.pop('gap', None)
        if not gap is None:
            text.setGap(gap)
    rotation = kwargs.pop('rotation', None)
    if not rotation is None:
        text.setAngle(rotation)
    coordinates = kwargs.pop('coordinates', 'data')
    text.setCoordinates(coordinates)
    return text
    
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
            colors.append(getcolor(cobj))
        ls = LegendManage.createLegendScheme(shapetype, levels, colors)
        setlegendscheme(ls, **kwargs)         
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
        setlegendscheme(ls, **kwargs)
    elif n == 1 and isinstance(args[0], int):
        ls = LegendManage.createUniqValueLegendScheme(args[0], shapetype)
        setlegendscheme(ls, **kwargs)
    else:
        ls = LegendScheme(shapetype)
        for arg in args:
            if isinstance(arg, (list, tuple)):
                for argi in arg:
                    lb, isu = getlegendbreak(geometry, **argi)
                    if isunique and not isu:
                        isunique = False
                    ls.addLegendBreak(lb)
            else:
                lb, isu = getlegendbreak(geometry, **arg)
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
    
def makelegend(source, **kwargs):
    '''
    Make a legend.
    
    :param souce: Legend file name or list of the legen breaks.
    
    :returns: Created legend.
    '''
    if isinstance(source, basestring):
        if os.path.exists(source):
            ls = LegendScheme()
            ls.importFromXMLFile(source, False)
            return ls
        else:
            source = getcolormap(source)
    else:
        if isinstance(source, list):
            if isinstance(source[0], ColorBreak):
                ls = LegendScheme(source)
            else:
                colors = getcolors(source)
                values = kwargs.pop('values', None)
                if values is None:
                    cbs = []
                    for c in colors:
                        cb = ColorBreak()
                        cb.setColor(c)
                        cbs.append(cb)
                    ls = LegendScheme(cbs)
                else:
                    ls = LegendManage.createLegendScheme(values, colors)
        else:
            values = kwargs.pop('values', None)
            if values is None:
                ls = None
            else:
                if isinstance(values, NDArray):
                    values = values.aslist()
                cbs = source.findBreaks(values)
                ls = LegendScheme(cbs)
    return ls