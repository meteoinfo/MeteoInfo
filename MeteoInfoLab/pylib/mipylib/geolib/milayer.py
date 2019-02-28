#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2015-9-20
# Purpose: MeteoInfoLab layer module
# Note: Jython
#-----------------------------------------------------
from org.meteoinfo.data import TableUtil, XYListDataset
from org.meteoinfo.layer import LayerTypes, VectorLayer, ChartSet
from org.meteoinfo.projection import ProjectionUtil, KnownCoordinateSystems
from org.meteoinfo.shape import PolygonShape, ShapeTypes
from org.meteoinfo.legend import LegendType
from java.util import Date, Calendar
from java.awt import Font
from datetime import datetime
import mipylib.miutil as miutil
import mipylib.numeric.minum as minum
import geoutil

class MILayer(object):
    '''
    Map layer
    
    :param layer: (*MapLayer*) MapLayer object.
    :param shapetype: (*ShapeTypes*) Shape type ['point' | 'line' | 'polygon']
    '''
    def __init__(self, layer=None, shapetype=None):
        if layer is None:
            if shapetype is None:
                print 'shapetype must be specified!'
            else:                
                type = ShapeTypes.Point
                if shapetype == 'line':
                    type = ShapeTypes.Polyline
                elif shapetype == 'polygon':
                    type = ShapeTypes.Polygon
                self.layer = VectorLayer(type)
                self.shapetype = type
                self.proj = KnownCoordinateSystems.geographic.world.WGS1984
        else:
            self.layer = layer
            self.shapetype = layer.getShapeType()
            self.proj = layer.getProjInfo()
    
    def __repr__(self):
        return self.layer.getLayerInfo()            
    
    def isvectorlayer(self):
        '''
        Check this layer is VectorLayer or not.
        
        :returns: (*boolean*) Is VectorLayer or not.
        '''
        return self.layer.getLayerType() == LayerTypes.VectorLayer
        
    def get_encoding(self):
        '''
        Get encoding.
        
        :returns: (*string*) Encoding
        '''
        return self.layer.getAttributeTable().getEncoding()
        
    def gettable(self):
        '''
        Get attribute table.
        
        :returns: (*PyTableData') Attribute table.
        '''
        r = self.layer.getAttributeTable().getTable()
        return minum.datatable(r)
    
    def cellvalue(self, fieldname, shapeindex):
        '''
        Get attribute table cell value.
        
        :param fieldname: (*string*) Field name.
        :param shapeindex: (*int*) Shape index.
        
        :returns: The value in attribute table identified by field name and shape index.
        '''
        v = self.layer.getCellValue(fieldname, shapeindex)
        if isinstance(v, Date):
            cal = Calendar.getInstance()
            cal.setTime(v)
            year = cal.get(Calendar.YEAR)
            month = cal.get(Calendar.MONTH) + 1
            day = cal.get(Calendar.DAY_OF_MONTH)
            hour = cal.get(Calendar.HOUR_OF_DAY)
            minute = cal.get(Calendar.MINUTE)
            second = cal.get(Calendar.SECOND)
            dt = datetime(year, month, day, hour, minute, second)
            return dt
        else:
            return v
            
    def setcellvalue(self, fieldname, shapeindex, value):
        '''
        Set cell value in attribute table.
        
        :param fieldname: (*string*) Field name.
        :param shapeindex: (*int*) Shape index.
        :param value: (*object*) Cell value to be asigned.
        '''
        self.layer.editCellValue(fieldname, shapeindex, value)
            
    def shapes(self):
        '''
        Get shapes.
        '''
        return self.layer.getShapes()
        
    def shapenum(self):
        '''
        Get shape number
        '''
        return self.layer.getShapeNum()
        
    def legend(self):
        '''
        Get legend scheme.
        '''
        return self.layer.getLegendScheme()
        
    def setlegend(self, legend):
        '''
        Set legend scheme.
        
        :param legend: (*LegendScheme*) Legend scheme.
        '''
        self.layer.setLegendScheme(legend)
        
    def update_legend(self, ltype, fieldname):
        '''
        Update legend scheme.
        
        :param ltype: (*string*) Legend type [single | unique | graduate].
        :param fieldname: (*string*) Field name.
        '''
        if ltype == 'single':
            ltype = LegendType.SingleSymbol
        elif ltype == 'unique':
            ltype = LegendType.UniqueValue
        elif ltyp == 'graduate':
            ltype = LegendType.GraduatedColor
        else:
            raise ValueError(ltype)
        self.layer.updateLegendScheme(ltype, fieldname)
        return self.layer.getLegendScheme()
    
    def addfield(self, fieldname, dtype, values=None):
        '''
        Add a field into the attribute table.
        
        :param fieldname: (*string*) Field name.
        :param dtype: (*string*) Field data type [string | int | float | double].
        :param values: (*array_like*) Field values.
        '''
        dt = TableUtil.toDataTypes(dtype)
        self.layer.editAddField(fieldname, dt)
        if not values is None:
            n = self.shapenum()
            for i in range(n):
                if i < len(values):
                    self.layer.editCellValue(fieldname, i, values[i])
                    
    def delfield(self, fieldname):
        '''
        Delete a field from the attribute table.
        
        :param fieldname: (*string*) Filed name.
        '''
        self.layer.editRemoveField(fieldname)
        
    def renamefield(self, fieldname, newfieldname):
        '''
        Rename the field.
        
        :param fieldname: (*string*) The old field name.
        :param newfieldname: (*string*) The new field name.
        '''
        self.layer.editRenameField(fieldname, newfieldname)
        
    def addshape(self, x, y, fields=None, z=None, m=None):
        '''
        Add a shape.
        
        :param x: (*array_like*) X coordinates of the shape points.
        :param y: (*array_like*) Y coordinates of the shape points.
        :param fields: (*array_like*) Field values of the shape.
        :param z: (*array_like*) Optional, Z coordinates of the shape points.
        :param m: (*array_like*) Optional, M coordinates of the shape points.
        '''
        type = 'point'
        if self.shapetype == ShapeTypes.Polyline:
            type = 'line'
        elif self.shapetype == ShapeTypes.Polygon:
            type = 'polygon'
        shapes = geoutil.makeshapes(x, y, type, z, m)
        if len(shapes) == 1:
            self.layer.editAddShape(shapes[0], fields)
        else:
            for shape, field in zip(shapes, fields):
                self.layer.editAddShape(shape, field)
                    
    def addlabels(self, fieldname, **kwargs):
        '''
        Add labels
        
        :param fieldname: (*string*) Field name
        :param fontname: (*string*) Font name. Default is ``Arial``.
        :param fontsize: (*string*) Font size. Default is ``14``.
        :param bold: (*boolean*) Font bold or not. Default is ``False``.
        :param color: (*color*) Label color. Default is ``None`` with black color.
        :param xoffset: (*int*) X coordinate offset. Default is ``0``.
        :param yoffset: (*int*) Y coordinate offset. Default is ``0``.
        :param avoidcoll: (*boolean*) Avoid labels collision or not. Default is ``True``.
        :param decimals: (*int*) Number of decimals of labels.
        '''
        labelset = self.layer.getLabelSet()
        labelset.setFieldName(fieldname)
        fontname = kwargs.pop('fontname', 'Arial')
        fontsize = kwargs.pop('fontsize', 14)
        bold = kwargs.pop('bold', False)
        if bold:
            font = Font(fontname, Font.BOLD, fontsize)
        else:
            font = Font(fontname, Font.PLAIN, fontsize)
        labelset.setLabelFont(font)
        color = kwargs.pop('color', None)
        if not color is None:
            color = miutil.getcolor(color)
            labelset.setLabelColor(color)
        xoffset = kwargs.pop('xoffset', 0)
        labelset.setXOffset(xoffset)
        yoffset = kwargs.pop('yoffset', 0)
        labelset.setYOffset(yoffset)
        avoidcoll = kwargs.pop('avoidcoll', True)
        labelset.setAvoidCollision(avoidcoll)
        decimals = kwargs.pop('decimals', None)
        if not decimals is None:
            labelset.setAutoDecimal(False)
            labelset.setDecimalDigits(decimals)
        self.layer.addLabels()
        
    def getlabel(self, text):
        '''
        Get a label.
        
        :param text: (*string*) The label text.
        '''
        return self.layer.getLabel(text)
        
    def movelabel(self, label, x=0, y=0):
        '''
        Move a label.
        
        :param label: (*string*) The label text.
        :param x: (*float*) X shift for moving in pixel unit.
        :param y: (*float*) Y shift for moving in pixel unit.
        '''
        self.layer.moveLabel(label, x, y)
        
    def add_charts(self, fieldnames, legend=None, **kwargs):
        '''
        Add charts
        
        :param fieldnames: (*list of string*) Field name list.
        :param legend: (*LegendScheme*) Chart legend.
        :param charttype: (*string*) Chart type [bar | pie]. Default is ``bar``.
        :param minsize: (*int*) Minimum chart size. Default is ``0``.
        :param maxsize: (*int*) Maximum chart size. Default is ``50``.
        :param barwidth: (*int*) Bar width. Only valid for bar chart. Default is ``8``.
        :param xoffset: (*int*) X coordinate offset. Default is ``0``.
        :param yoffset: (*int*) Y coordinate offset. Default is ``0``.
        :param avoidcoll: (*boolean*) Avoid labels collision or not. Default is ``True``.
        :param align: (*string*) Chart align type [center | left | right | none], Default is ``center``.
        :param view3d: (*boolean*) Draw chart as 3D or not. Default is ``False``.
        :param thickness: (*int*) 3D chart thickness. Default is ``5``.
        :param drawlabel: (*boolean*) Draw label or not. Default is ``False``.
        :param fontname: (*string*) Label font name.
        :param fontsize: (*int*) Label font size.
        :param bold: (*boolean*) Font bold or not. Default is ``False``.
        :param labelcolor: (*color*) Label color.
        :param decimals: (*int*) Number of decimals of labels.
        '''
        charttype = kwargs.pop('charttype', None)
        minsize = kwargs.pop('minsize', None)
        maxsize = kwargs.pop('maxsize', None)
        barwidth = kwargs.pop('barwidth', None)
        xoffset = kwargs.pop('xoffset', None)
        yoffset = kwargs.pop('yoffset', None)
        avoidcoll = kwargs.pop('avoidcoll', None)
        align = kwargs.pop('align', None)
        view3d = kwargs.pop('view3d', None)
        thickness = kwargs.pop('thickness', None)
        drawlabel = kwargs.pop('drawlabel', None)
        fontname = kwargs.pop('fontname', 'Arial')
        fontsize = kwargs.pop('fontsize', 12)
        bold = kwargs.pop('bold', False)
        if bold:
            font = Font(fontname, Font.BOLD, fontsize)
        else:
            font = Font(fontname, Font.PLAIN, fontsize)
        labelcolor = kwargs.pop('labelcolor', None)
        decimals = kwargs.pop('decimals', None)
        
        chartset = self.layer.getChartSet()
        chartset.setFieldNames(fieldnames)
        chartset.setLegendScheme(legend)
        if not charttype is None:
            chartset.setChartType(charttype)
        if not minsize is None:
            chartset.setMinSize(minsize)
        if not maxsize is None:
            chartset.setMaxSize(maxsize)
        if not barwidth is None:
            chartset.setBarWidth(barwidth)
        if not xoffset is None:
            chartset.setXShift(xoffset)
        if not yoffset is None:
            chartset.setYShift(yoffset)
        if not avoidcoll is None:
            chartset.setAvoidCollision(avoidcoll)
        if not align is None:
            chartset.setAlignType(align)
        if not view3d is None:
            chartset.setView3D(view3d)
        if not thickness is None:
            chartset.setThickness(thickness)
        if not drawlabel is None:
            chartset.setDrawLabel(drawlabel)
        chartset.setLabelFont(font)
        if not labelcolor is None:
            chartset.setLabelColor(miutil.getcolor(labelcolor))
        if not decimals is None:
            chartset.setDecimalDigits(decimals)
        self.layer.updateChartSet()
        self.layer.addCharts()
        return chartset
        
    def get_chartlegend(self):
        '''
        Get legend of the chart graphics.
        '''
        return self.layer.getChartSet().getLegendScheme()
        
    def get_chart(self, index):
        '''
        Get a chart graphic.
        
        :param index: (*int*) Chart index.
        
        :returns: Chart graphic
        '''
        return self.layer.getChartPoints()[index]
        
    def move_chart(self, index, x=0, y=0):
        '''
        Move a chart graphic.
        
        :param index: (*int*) Chart index.
        :param x: (*float*) X shift for moving.
        :param y: (*float*) Y shift for moving.
        '''
        s = self.layer.getChartPoints()[index].getShape()
        p = s.getPoint()
        p.X = p.X + x
        p.Y = p.Y + y
        s.setPoint(p)
        
    def set_avoidcoll(self, avoidcoll):
        '''
        Set if avoid collision or not. Only valid for VectorLayer with Point shapes.
        
        :param avoidcoll: (*boolean*) Avoid collision or not.
        '''
        self.layer.setAvoidCollision(avoidcoll)
        
    def project(self, toproj):
        '''
        Project to another projection.
        
        :param toproj: (*ProjectionInfo*) The projection to be projected.
        '''
        ProjectionUtil.projectLayer(self.layer, toproj)
        
    def buffer(self, dist=0, merge=False):
        '''
        Get the buffer layer.
        
        :param dist: (*float*) Buffer value.
        :param merge: (*boolean*) Merge the buffered shapes or not.
        
        :returns: (*MILayer*) Buffered layer.
        '''
        r = self.layer.buffer(dist, False, merge)
        return MILayer(r)
        
    def clip(self, clipobj):
        '''
        Clip this layer by polygon or another polygon layer.
        
        :param clipobj: (*PolygonShape or MILayer*) Clip object.
        
        :returns: (*MILayer*) Clipped layer.
        '''
        if isinstance(clipobj, PolygonShape):
            clipobj = [clipobj]
        elif isinstance(clipobj, MILayer):
            clipobj = clipobj.layer
        r = self.layer.clip(clipobj)
        return MILayer(r)
        
    def select(self, expression, seltype='new'):
        '''
        Select shapes by SQL expression.
        
        :param expression: (*string*) SQL expression.
        :param seltype: (*string*) Selection type ['new' | 'add_to_current' |
            'remove_from_current' | 'select_from_current']
            
        :returns: (*list of Shape*) Selected shape list.
        '''
        self.layer.sqlSelect(expression, seltype)
        return self.layer.getSelectedShapes()
        
    def clear_selection(self):
        '''
        Clear shape selection.
        '''
        self.layer.clearSelectedShapes()
        
    def clone(self):
        '''
        Clone self.
        '''
        return MILayer(self.layer.clone())
    
    def save(self, fn=None, encoding=None):
        """
        Save layer as shape file.
        
        :param fn: (*string*) Shape file name (.shp).
        :param encoding: (*string*) Encoding.
        """
        if fn is None:
            fn = self.layer.getFileName()
            
        if fn.strip() == '':
            print 'File name is needed to save the layer!'
            raise IOError
        else:
            if encoding is None:
                self.layer.saveFile(fn)
            else:
                self.layer.saveFile(fn, encoding)
    
    def savekml(self, fn):
        """
        Save layer as KML file.
        
        :param fn: (*string*) KML file name.
        """
        self.layer.saveAsKMLFile(fn)


class MIXYListData():
    def __init__(self, data=None):
        if data is None:
            self.data = XYListDataset()
        else:
            self.data = data
        
    def __getitem__(self, indices):
        if not isinstance(indices, tuple):
            inds = []
            inds.append(indices)
            indices = inds
            
        if isinstance(indices[0], int):
            if isinstance(indices[1], int):
                x = self.data.getX(indices[0], indices[1])
                y = self.data.getY(indices[0], indices[1])
                return x, y
            else:
                return self.data.getXValues(indices[0]), self.data.getXValues(indices[0])           
        
    def size(self, series=None):
        if series is None:
            return self.data.getSeriesCount()
        else:
            return self.data.getItemCount(series)
            
    def addseries(self, xdata, ydata, key=None):
        if key is None:
            key = 'Series_' + str(self.size())
        if isinstance(xdata, list):
            self.data.addSeries(key, xdata, ydata)
        else:
            self.data.addSeries(key, xdata.asarray(), ydata.asarray())   
            