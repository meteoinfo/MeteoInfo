#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2014-12-27
# Purpose: MeteoInfo geo module
# Note: Jython
#-----------------------------------------------------

import os
import numbers

from org.meteoinfo.data.mapdata.geotiff import GeoTiff
from org.meteoinfo.shape import ShapeUtil, PolygonShape
from org.meteoinfo.legend import BreakTypes
from org.meteoinfo.geoprocess import GeoComputation
from org.meteoinfo.data import ArrayMath, ArrayUtil
from org.meteoinfo.data.mapdata import MapDataManage, AttributeTable
from org.meteoinfo.projection import KnownCoordinateSystems, Reproject
from org.meteoinfo.projection.info import ProjectionInfo
from org.meteoinfo.global import PointD
from org.meteoinfo.io import IOUtil

from milayer import MILayer
from mipylib.numeric.miarray import MIArray
from mipylib.numeric.dimarray import DimArray
import mipylib.migl as migl
import mipylib.numeric.minum as minum

from java.util import ArrayList

__all__ = [
    'arrayinpolygon','circle','convert_encoding_dbf','distance','georead','geotiffread',
    'maplayer','inpolygon','maskin','maskout','polyarea','polygon','rmaskin','rmaskout','shaperead',
    'projinfo','project','projectxy'
    ]

def shaperead(fn, encoding=None):   
    '''
    Returns a layer readed from a shape file.
    
    :param fn: (*string*) The shape file name (.shp).
    :param encoding: (*string*) Encoding
    
    :returns: (*MILayer*) The created layer.
    '''
    if not fn.endswith('.shp'):
        fn = fn + '.shp'
    if not os.path.exists(fn):
        fn = os.path.join(migl.mapfolder, fn)
        
    if os.path.exists(fn):        
        try:
            if encoding is None:
                encoding = IOUtil.encodingDetectShp(fn)
                if encoding == 'ISO8859_1':
                    encoding = 'UTF-8'
            layer = MILayer(MapDataManage.readMapFile_ShapeFile(fn, encoding))
            if not layer.legend() is None:
                lb = layer.legend().getLegendBreaks()[0]
                if lb.getBreakType() == BreakTypes.PolygonBreak:
                    lb.setDrawFill(False)
            return layer
        except:
            raise
    else:
        print 'File not exists: ' + fn
        raise
    
def georead(fn):
    '''
    Returns a layer readed from a supported geo-data file.
    
    :param fn: (*string*) The supported geo-data file name (shape file, wmp, geotiff, image, bil...).
    
    :returns: (*MILayer*) The created layer.
    '''
    if not os.path.exists(fn):
        fn = os.path.join(migl.mapfolder, fn)
        
    if os.path.exists(fn):        
        try:
            layer = MILayer(MapDataManage.loadLayer(fn))
            if not layer.legend() is None:
                lb = layer.legend().getLegendBreaks()[0]
                if lb.getBreakType() == BreakTypes.PolygonBreak:
                    lb.setDrawFill(False)
            return layer
        except:
            raise
    else:
        print 'File not exists: ' + fn
        raise IOError
    
def geotiffread(filename):
    '''
    Return data array from a GeoTiff data file.
    
    :param filename: (*string*) The GeoTiff file name.
    
    :returns: (*MIArray*) Readed data array.
    '''
    geotiff = GeoTiff(filename)
    geotiff.read()
    r = geotiff.readArray()
    return MIArray(r)
    
def convert_encoding_dbf(filename, fromencoding, toencoding):
    '''
    Convert encoding of a dBase file (.dbf).
    
    :param filename: (*string*) The dBase file name.
    :param fromencoding: (*string*) From encoding.
    :param toencoding: (*string*) To encoding.
    '''
    #Read dBase file
    atable = AttributeTable()
    atable.setEncoding(fromencoding)
    atable.openDBF(filename)
    atable.fill(atable.getNumRecords())
    
    #Save dBase file
    atable.setEncoding(toencoding)
    atable.save()
    
def maplayer(shapetype='polygon'):
    '''
    Create a new map layer.
    
    :param shapetype: (*string*) Shape type of the layer. ['point' | 'line' | 'polygon'].
    
    :returns: (*MILayer*) MILayer object.
    '''
    return MILayer(shapetype=shapetype)
    
def polygon(x, y=None):
    '''
    Create polygon from coordinate data.
    
    :param x: (*array_like*) X coordinate array. If y is ``None``, x should be 2-D array contains x and y.
    :param y: (*array_like*) Y coordinate array.
    
    :returns: (*PolygonShape*) Created polygon.
    '''
    if y is None:
        polygon = ShapeUtil.createPolygonShape(x)
    else:
        if isinstance(x, MIArray):
            x = x.aslist()
        if isinstance(y, MIArray):
            y = y.aslist()
        polygon = ShapeUtil.createPolygonShape(x, y)
    return polygon  

def circle(xy, radius=5):
    '''
    Create a circle patch
    
    :param xy: (*list of float*) X and Y coordinates of the circle center.
    :param radius: (*float*) Circle radius.
    
    :returns: (*CircleShape*) Created circle.
    '''
    cc = ShapeUtil.createCircleShape(xy[0], xy[1], radius)
    return cc    
    
def inpolygon(x, y, polygon):
    '''
    Check if x/y points are inside a polygon or not.
    
    :param x: (*array_like*) X coordinate of the points.
    :param y: (*array_like*) Y coordinate of the points.
    :param polygon: (*PolygonShape list*) The polygon list.
    
    :returns: (*boolean array*) Inside or not.
    '''
    if isinstance(x, numbers.Number):
        return GeoComputation.pointInPolygon(polygon, x, y)
    
    if isinstance(x, (list, tuple)):
        x = minum.array(x)
    if isinstance(y, (list, tuple)):
        y = minum.array(y)
    if isinstance(polygon, tuple):
        x_p = polygon[0]
        y_p = polygon[1]
        if isinstance(x_p, (list, tuple)):
            x_p = minum.array(x_p)
        if isinstance(y_p, (list, tuple)):
            y_p = minum.array(y_p)
        return MIArray(ArrayMath.inPolygon(x.array, y.array, x_p.array, y_p.array))
    else:
        if isinstance(polygon, MILayer):
            polygon = polygon.shapes()
        elif isinstance(polygon, PolygonShape):
            polygon = [polygon]
        return MIArray(ArrayMath.inPolygon(x.array, y.array, polygon))
    
def arrayinpolygon(a, polygon, x=None, y=None):
    '''
    Set array element value as 1 if inside a polygon or set value as -1.
    
    :param a: (*array_like*) The array.
    :param polygon: (*PolygonShape*) The polygon.
    :param x: (*float*) X coordinate of the point. Default is ``None``, for DimArray
    :param y: (*float*) Y coordinate of the point. Default is ``None``, for DimArray
    
    :returns: (*array_like*) Result array.
    '''
    if isinstance(a, DimArray):
        if x is None or y is None:
            x = self.dimvalue(1)
            y = self.dimvalue(0)
    if not x is None and not y is None:
        if isinstance(polygon, tuple):
            x_p = polygon[0]
            y_p = polygon[1]
            if isinstance(x_p, MIArray):
                x_p = x_p.aslist()
            if isinstance(y_p, MIArray):
                y_p = y_p.aslist()
            return MIArray(ArrayMath.inPolygon(a.asarray(), x.aslist(), y.aslist(), x_p, y_p))
        else:
            if isinstance(polygon, MILayer):
                polygon = polygon.layer
            return MIArray(ArrayMath.inPolygon(a.asarray(), x.aslist(), y.aslist(), polygon))
    else:
        return None
            
def distance(*args, **kwargs):
    """
    Get distance of a line.
    
    :param args: LineString or x, y coordinate arrays.
    :param islonlat: (*boolean*) x/y is longitude/latitude or not.
    
    :returns: Distance, meters for lon/lat.
    """
    islonlat = kwargs.pop('islonlat', False)
    if len(args) == 1:
        r = GeoComputation.getDistance(args[0].getPoints(), islonlat)
    else:
        x = args[0]
        y = args[1]
        if isinstance(x, (MIArray, DimArray)):
            x = x.aslist()
        if isinstance(y, (MIArray, DimArray)):
            y = y.aslist()
        r = GeoComputation.getDistance(x, y, islonlat)
    return r
    
def polyarea(*args, **kwargs):
    '''
    Calculate area of polygon.
    
    Parameter is a polygon object or x, y coordinate arrays.
    
    :return: The area of the polygon.
    '''
    islonlat = kwargs.pop('islonlat', False)
    if len(args) == 1:
        if islonlat:
            r = args[0].getSphericalArea()
        else:
            r = args[0].getArea()
    else:
        x = args[0]
        y = args[1]
        if isinstance(x, MIArray):
            x = x.aslist()
        if isinstance(y, MIArray):
            y = y.aslist()
        r = GeoComputation.getArea(x, y, islonlat)
    return r
    
def maskout(data, mask, x=None, y=None):
    """
    Maskout data by polygons - NaN values of elements outside polygons.
    
    :param data: (*array_like*) Array data for maskout.
    :param mask: (*list*) Polygon list as maskout borders.    
    :param x: (*array_like*) X coordinate array.
    :param y: (*array_like*) Y coordinate array.

    :returns: (*array_like*) Maskouted data array.
    """
    if mask is None:
        return data        
    elif isinstance(mask, (MIArray, DimArray)):
        r = ArrayMath.maskout(data.asarray(), mask.asarray())
        if isinstance(data, DimArray):
            return DimArray(r, data.dims, data.fill_value, data.proj)
        else:
            return MIArray(r)
            
    if x is None or y is None:
        if isinstance(data, DimArray):
            x = data.dimvalue(data.ndim - 1)
            y = data.dimvalue(data.ndim - 2)
        else:
            return None

    if not isinstance(mask, (list, ArrayList)):
        mask = [mask]
    r = ArrayMath.maskout(data.array, x.array, y.array, mask)
    if isinstance(data, DimArray):
        return DimArray(r, data.dims, data.fill_value, data.proj)
    else:
        return MIArray(r)
        
def rmaskout(data, x, y, mask):
    """
    Maskout data by polygons - the elements outside polygons will be removed
    
    :param data: (*array_like*) Array data for maskout.
    :param x: (*array_like*) X coordinate array.
    :param y: (*array_like*) Y coordinate array.
    :param mask: (*list*) Polygon list as maskout borders.
    
    :returns: (*list*) Maskouted data, x and y array list.
    """
    if not isinstance(mask, (list, ArrayList)):
        mask = [mask]
    r = ArrayMath.maskout_Remove(data.asarray(), x.asarray(), y.asarray(), mask)
    return MIArray(r[0]), MIArray(r[1]), MIArray(r[2])  
    
def maskin(data, mask, x=None, y=None):
    """
    Maskin data by polygons - NaN values of elements inside polygons.
    
    :param data: (*array_like*) Array data for maskout.
    :param mask: (*list*) Polygon list as maskin borders.    
    :param x: (*array_like*) X coordinate array.
    :param y: (*array_like*) Y coordinate array.

    :returns: (*array_like*) Maskined data array.
    """
    if mask is None:
        return data        
    elif isinstance(mask, MIArray):
        r = ArrayMath.maskin(data.array, mask.array)
        if isinstance(data, DimArray):
            return DimArray(r, data.dims, data.fill_value, data.proj)
        else:
            return MIArray(r)
        
    if x is None or y is None:
        if isinstance(data, DimArray):
            x = data.dimvalue(data.ndim - 1)
            y = data.dimvalue(data.ndim - 2)
        else:
            return None

    if not isinstance(mask, (list, ArrayList)):
        mask = [mask]
    r = ArrayMath.maskin(data.array, x.array, y.array, mask)
    if isinstance(data, DimArray):
        return DimArray(r, data.dims, data.fill_value, data.proj)
    else:
        return MIArray(r)
        
def rmaskin(data, x, y, mask):
    """
    Maskin data by polygons - the elements inside polygons will be removed
    
    :param data: (*array_like*) Array data for maskin.
    :param x: (*array_like*) X coordinate array.
    :param y: (*array_like*) Y coordinate array.
    :param mask: (*list*) Polygon list as mask borders.
    
    :returns: (*list*) Masked data, x and y array list.
    """
    if not isinstance(mask, (list, ArrayList)):
        mask = [mask]
    r = ArrayMath.maskin_Remove(data.array, x.array, y.array, mask)
    return MIArray(r[0]), MIArray(r[1]), MIArray(r[2])  
    
def projinfo(proj4string=None, proj='longlat', **kwargs):
    """
    Create a projection object with Proj.4 parameters (http://proj4.org/)
    
    :param proj4string: (*string*) Proj.4 projection string.
    :param proj: (*string*) Projection name.
    :param lat_0: (*float*) Latitude of origin.
    :param lon_0: (*float*) Central meridian.
    :param lat_1: (*float*) Latitude of first standard paralle.
    :param lat_2: (*float*) Latitude of second standard paralle.
    :param lat_ts: (*float*) Latitude of true scale.
    :param k: (*float*) Scaling factor.
    :param x_0: (*float*) False easting.
    :param y_0: (*float*) False northing.
    :param h: (*float*) Height from earth surface.
    
    :returns: (*ProjectionInfo*) ProjectionInfo object.
    """
    if not proj4string is None:
        return ProjectionInfo.factory(proj4string)
    
    if proj == 'longlat' and len(kwargs) == 0:
        return KnownCoordinateSystems.geographic.world.WGS1984
        
    origin = kwargs.pop('origin', (0, 0, 0))    
    lat_0 = origin[0]
    lon_0 = origin[1]
    lat_0 = kwargs.pop('lat_0', lat_0)
    lon_0 = kwargs.pop('lon_0', lon_0)
    lat_ts = kwargs.pop('truescalelat', 0)
    lat_ts = kwargs.pop('lat_ts', lat_ts)
    k = kwargs.pop('scalefactor', 1)
    k = kwargs.pop('k', k)
    paralles = kwargs.pop('paralles', (30, 60))
    lat_1 = paralles[0]
    if len(paralles) == 2:
        lat_2 = paralles[1]
    else:
        lat_2 = lat_1
    lat_1 = kwargs.pop('lat_1', lat_1)
    lat_2 = kwargs.pop('lat_2', lat_2)
    x_0 = kwargs.pop('falseeasting', 0)
    y_0 = kwargs.pop('falsenorthing', 0)
    x_0 = kwargs.pop('x_0', x_0)
    y_0 = kwargs.pop('y_0', y_0)
    h = kwargs.pop('h', None)
    projstr = '+proj=' + proj \
        + ' +lat_0=' + str(lat_0) \
        + ' +lon_0=' + str(lon_0) \
        + ' +lat_1=' + str(lat_1) \
        + ' +lat_2=' + str(lat_2) \
        + ' +lat_ts=' + str(lat_ts) \
        + ' +k=' + str(k) \
        + ' +x_0=' + str(x_0) \
        + ' +y_0=' + str(y_0)
    if not h is None:
        projstr = projstr + ' +h=' + str(h)
        
    return ProjectionInfo.factory(projstr) 

def project(x, y, fromproj=KnownCoordinateSystems.geographic.world.WGS1984, toproj=KnownCoordinateSystems.geographic.world.WGS1984):
    """
    Project geographic coordinates from one projection to another.
    
    :param x: (*array_like*) X coordinate values for projection.
    :param y: (*array_like*) Y coordinate values for projection.
    :param fromproj: (*ProjectionInfo*) From projection. Default is longlat projection.
    :param toproj: (*ProjectionInfo*) To projection. Default is longlat projection.
    
    :returns: (*array_like*, *array_like*) Projected geographic coordinates.
    """
    if isinstance(fromproj, str):
        fromproj = ProjectionInfo.factory(fromproj)
    if isinstance(toproj, str):
        toproj = ProjectionInfo.factory(toproj)
    if isinstance(x, (tuple, list)):
        x = array(x)
    if isinstance(y, (tuple, list)):
        y = array(y)
    if isinstance(x, MIArray):
        outxy = ArrayUtil.reproject(x.asarray(), y.asarray(), fromproj, toproj)
        return MIArray(outxy[0]), MIArray(outxy[1])
    else:
        inpt = PointD(x, y)
        outpt = Reproject.reprojectPoint(inpt, fromproj, toproj)
        return outpt.X, outpt.Y
    
def projectxy(lon, lat, xnum, ynum, dx, dy, toproj, fromproj=None, pos='lowerleft'):
    """
    Get projected x, y coordinates by projection and a given lon, lat coordinate.
    
    :param lon: (*float*) Longitude value.
    :param lat: (*float*) Latitude value.
    :param xnum: (*int*) X number.
    :param ynum: (*int*) Y number.
    :param dx: (*float*) X delta.
    :param dy: (*float*) Y delta.
    :param toproj: (*ProjectionInfo*) To projection.
    :param fromproj: (*ProjectionInfo*) From projection. Default is longlat projection.
    :param pos: (*string*) ['lowerleft' | 'center'] Lon, lat coordinate position.

    :returns: (*array_like*, *array_like*) Projected x, y coordinates.
    """
    if fromproj is None:
        fromproj = KnownCoordinateSystems.geographic.world.WGS1984
    x, y = project(lon, lat, toproj, fromproj)
    if pos == 'lowerleft':
        xx = minum.arange1(x, xnum, dx)
        yy = minum.arange1(y, ynum, dy)
    else:
        llx = x - ((xnum - 1) * 0.5 * dx)
        lly = y - ((ynum - 1) * 0.5 * dy)
        xx = minum.arange1(llx, xnum, dx)
        yy = minum.arange1(lly, ynum, dy)
    return xx, yy
        