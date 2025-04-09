# -----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2014-12-27
# Purpose: MeteoInfo geo module
# Note: Jython
# -----------------------------------------------------

import os
import numbers

from org.meteoinfo.data.mapdata.geotiff import GeoTiff
from org.meteoinfo.geometry.shape import ShapeUtil, PolygonShape
from org.meteoinfo.geometry.graphic import Graphic
from org.meteoinfo.geometry.legend import BreakTypes
from org.meteoinfo.geometry.geoprocess import GeoComputation, GeometryUtil
from org.meteoinfo.ndarray.math import ArrayMath, ArrayUtil
from org.meteoinfo.geo.mapdata import MapDataManage, ShapeFileManage
from org.meteoinfo.geo.util import GeoIOUtil
from org.meteoinfo.geo.io import GeoJSONWriter
from org.meteoinfo.table import AttributeTable
from org.meteoinfo.projection import KnownCoordinateSystems, Reproject
from org.meteoinfo.projection import ProjectionInfo
from org.meteoinfo.common import PointD
from org.meteoinfo.common.io import IOUtil
from org.meteoinfo.common import ResampleMethods

from .milayer import MILayer
from ._graphic import GeoGraphicCollection
from mipylib.numeric.core import NDArray, DimArray
import mipylib.migl as migl
import mipylib.numeric as np

from java.util import ArrayList

__all__ = [
    'arrayinpolygon', 'bilwrite', 'circle', 'convert_encoding_dbf', 'distance', 'geojson_write', 'georead', 'geotiffread', 'gridarea',
    'maplayer', 'inpolygon', 'maskin', 'maskout', 'polyarea', 'polygon', 'rmaskin', 'rmaskout', 'shaperead',
    'shapewrite', 'polygonindex', 'projinfo', 'project', 'projectxy', 'reproject', 'reproject_image'
]


def shaperead(fn, encoding=None):
    """
    Returns a layer read from a shape file.
    
    :param fn: (*string*) The shape file name (.shp).
    :param encoding: (*string*) Encoding
    
    :returns: (*MILayer*) The created layer.
    """
    if not fn.endswith('.shp'):
        fn = fn + '.shp'
    if not os.path.exists(fn):
        fn = os.path.join(migl.get_map_folder(), fn)

    if os.path.exists(fn):
        try:
            if encoding is None:
                encoding = IOUtil.encodingDetectShp(fn)
                if encoding == 'ISO8859_1':
                    encoding = 'UTF-8'
            layer = MILayer(MapDataManage.readMapFile_ShapeFile(fn, encoding))
            if not layer.legend is None:
                lb = layer.legend.getLegendBreaks()[0]
                if lb.getBreakType() == BreakTypes.POLYGON_BREAK:
                    lb.setDrawFill(False)
            return layer
        except:
            raise
    else:
        print('File not exists: {}'.format(fn))
        raise


def shapewrite(fn, graphics, projection=None):
    """
    Save graphics as a shape file.

    :param fn: (*str*) The shape file name
    :param graphics: (*GraphicCollection*) The graphics
    :param projection: (*ProjectionInfo*) The projection
    """
    geographic = GeoGraphicCollection.factory(graphics, projection=projection)
    ShapeFileManage.saveShapeFile(fn, geographic._geographic)


def georead(fn, encoding=None):
    """
    Returns a layer read from a supported geo-data file.
    
    :param fn: (*string*) The supported geo-data file name (shape file, wmp, geotiff, image, bil...).
    :param encoding: (*string*) Table file encoding - only used for shape file. Default is `None`.
    
    :returns: (*MILayer*) The created layer.
    """
    if not os.path.exists(fn):
        fn = os.path.join(migl.get_map_folder(), fn)

    if not os.path.exists(fn):
        fn = fn + '.shp'

    if os.path.exists(fn):
        if fn.endswith('.shp'):
            return shaperead(fn, encoding)
        else:
            try:
                layer = MILayer(MapDataManage.loadLayer(fn))
                if not layer.legend is None:
                    lb = layer.legend.getLegendBreaks()[0]
                    if lb.getBreakType() == BreakTypes.POLYGON_BREAK:
                        lb.setDrawFill(False)
                return layer
            except:
                raise
    else:
        print('File not exists: ' + fn)
        raise IOError


def geotiffread(filename):
    """
    Return data array from a GeoTiff data file.
    
    :param filename: (*string*) The GeoTiff file name.
    
    :returns: (*NDArray*) Read data array.
    """
    geotiff = GeoTiff(filename)
    geotiff.read()
    r = geotiff.readArray()
    return NDArray(r)


def convert_encoding_dbf(filename, fromencoding, toencoding):
    """
    Convert encoding of a dBase file (.dbf).
    
    :param filename: (*string*) The dBase file name.
    :param fromencoding: (*string*) From encoding.
    :param toencoding: (*string*) To encoding.
    """
    # Read dBase file
    atable = AttributeTable()
    atable.setEncoding(fromencoding)
    atable.openDBF(filename)
    atable.fill(atable.getNumRecords())

    # Save dBase file
    atable.setEncoding(toencoding)
    atable.save()


def maplayer(shapetype='polygon'):
    """
    Create a new map layer.
    
    :param shapetype: (*ShapeTypes*) Shape type ['point' | 'point_z' | 'line' | 'line_z' | 'polygon'
        | 'polygon_z']
    
    :returns: (*MILayer*) MILayer object.
    """
    return MILayer(shapetype=shapetype)


def polygon(x, y=None):
    """
    Create polygon from coordinate data.
    
    :param x: (*array_like*) X coordinate array. If y is ``None``, x should be 2-D array contains x and y.
    :param y: (*array_like*) Y coordinate array.
    
    :returns: (*PolygonShape*) Created polygon.
    """
    if y is None:
        polygon = ShapeUtil.createPolygonShape(x)
    else:
        if isinstance(x, NDArray):
            x = x.aslist()
        if isinstance(y, NDArray):
            y = y.aslist()
        polygon = ShapeUtil.createPolygonShape(x, y)
    return polygon


def circle(xy, radius=5):
    """
    Create a circle patch
    
    :param xy: (*list of float*) X and Y coordinates of the circle center.
    :param radius: (*float*) Circle radius.
    
    :returns: (*CircleShape*) Created circle.
    """
    cc = ShapeUtil.createCircleShape(xy[0], xy[1], radius)
    return cc


def inpolygon(x, y, polygon):
    """
    Check if x/y points are inside a polygon or not.
    
    :param x: (*array_like*) X coordinate of the points.
    :param y: (*array_like*) Y coordinate of the points.
    :param polygon: (*PolygonShape list*) The polygon list.
    
    :returns: (*boolean array*) Inside or not.
    """
    if isinstance(x, numbers.Number):
        x = np.array([x])
    if isinstance(y, numbers.Number):
        y = np.array([y])

    if isinstance(x, (list, tuple)):
        x = np.array(x)
    if isinstance(y, (list, tuple)):
        y = np.array(y)
    if isinstance(polygon, tuple):
        x_p = polygon[0]
        y_p = polygon[1]
        if isinstance(x_p, (list, tuple)):
            x_p = np.array(x_p)
        if isinstance(y_p, (list, tuple)):
            y_p = np.array(y_p)
        r = NDArray(GeometryUtil.inPolygon(x._array, y._array, x_p._array, y_p._array))
    else:
        if isinstance(polygon, MILayer):
            polygon = polygon.shapes
        elif isinstance(polygon, PolygonShape):
            polygon = [polygon]
        r = NDArray(GeometryUtil.inPolygon(x._array, y._array, polygon))
        if len(r) == 1:
            return r[0]
        else:
            return r


def arrayinpolygon(a, polygon, x=None, y=None):
    """
    Set array element value as 1 if inside a polygon or set value as -1.
    
    :param a: (*array_like*) The array.
    :param polygon: (*PolygonShape*) The polygon.
    :param x: (*float*) X coordinate of the point. Default is ``None``, for DimArray
    :param y: (*float*) Y coordinate of the point. Default is ``None``, for DimArray
    
    :returns: (*array_like*) Result array.
    """
    if isinstance(a, DimArray):
        if x is None or y is None:
            x = self.dimvalue(1)
            y = self.dimvalue(0)

    if not x is None and not y is None:
        if isinstance(polygon, tuple):
            x_p = polygon[0]
            y_p = polygon[1]
            if isinstance(x_p, NDArray):
                x_p = x_p.aslist()
            if isinstance(y_p, NDArray):
                y_p = y_p.aslist()
            return NDArray(GeometryUtil.inPolygon(a.asarray(), x.aslist(), y.aslist(), x_p, y_p))
        else:
            if isinstance(polygon, MILayer):
                polygon = polygon._layer
            return NDArray(GeometryUtil.inPolygon(a.asarray(), x.aslist(), y.aslist(), polygon))
    else:
        return None


def polygonindex(x, y, polygons):
    """
    Find polygon index by x, y coordinates.

    :param x: (*float*) X coordinate of the point.
    :param y: (*float*) Y coordinate of the point.
    :param polygons: (*list of PolygonShape*) The polygons

    :returns: (*array_like*) Result array.
    """
    if isinstance(x, (tuple, list)):
        x = np.array(x)

    if isinstance(x, (tuple, list)):
        y = np.array(y)

    r = GeometryUtil.polygonIndex(x.jarray, y.jarray, polygons)
    return NDArray(r)


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
        if isinstance(x, (NDArray, DimArray)):
            x = x.aslist()
        if isinstance(y, (NDArray, DimArray)):
            y = y.aslist()
        r = GeoComputation.getDistance(x, y, islonlat)
    return r


def polyarea(*args, **kwargs):
    """
    Calculate area of polygon.
    
    Parameter is a polygon object or x, y coordinate arrays.
    
    :return: The area of the polygon.
    """
    islonlat = kwargs.pop('islonlat', False)
    if len(args) == 1:
        if islonlat:
            r = args[0].getSphericalArea()
        else:
            r = args[0].getArea()
    else:
        x = args[0]
        y = args[1]
        if isinstance(x, NDArray):
            x = x.aslist()
        if isinstance(y, NDArray):
            y = y.aslist()
        r = GeoComputation.getArea(x, y, islonlat)
    return r


def gridarea(x_orig, x_cell, x_num, y_orig, y_cell, y_num, islonlat=False,
             allcell=True, earth_radius=None):
    """
    Calculate area of grid cells.

    :param x_orig: (*float*) X origin.
    :param x_cell: (*float*) X cell spacing.
    :param x_num: (*int*) Cell number in x direction.
    :param y_orig: (*float*) Y origin.
    :param y_cell: (*float*) Y cell spacing.
    :param y_num: (*int*) Cell number in y direction.
    :param islonlat: (*bool*) Lonlat projection or not.
    :param allcell: (*bool*) Calculate all grid cells or not.
    :param earth_radius: (*float*) Earth radius in meters.
    :return: (*array*) Grid cell areas.
    """
    if earth_radius is None:
        a = GeoComputation.getGridArea(x_orig, x_cell, x_num, y_orig, y_cell, y_num,
                                       islonlat, allcell)
    else:
        a = GeoComputation.getGridArea(x_orig, x_cell, x_num, y_orig, y_cell, y_num,
                                       islonlat, allcell, earth_radius)
    return NDArray(a)


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
    elif isinstance(mask, NDArray):
        r = ArrayMath.maskout(data.asarray(), mask.asarray())
        return data.array_wrap(r)

    if x is None or y is None:
        if isinstance(data, DimArray):
            x = data.dimvalue(data.ndim - 1)
            y = data.dimvalue(data.ndim - 2)
        else:
            return None

    if not isinstance(mask, (list, tuple, ArrayList)):
        if isinstance(mask, Graphic):
            mask = mask.getShape()
        mask = [mask]
    else:
        for i in range(len(mask)):
            if isinstance(mask[i], Graphic):
                mask[i] = mask[i].getShape()

    if data.ndim == 2 and x.ndim == 1 and y.ndim == 1:
        x, y = np.meshgrid(x, y)

    r = GeometryUtil.maskout(data._array, x._array, y._array, mask)
    return data.array_wrap(r)


def rmaskout(data, x, y, mask):
    """
    Maskout data by polygons - the elements outside polygons will be removed
    
    :param data: (*array_like*) Array data for maskout.
    :param x: (*array_like*) X coordinate array.
    :param y: (*array_like*) Y coordinate array.
    :param mask: (*list*) Polygon list as maskout borders.
    
    :returns: (*list*) Maskouted data, x and y array list.
    """
    if not isinstance(mask, (list, tuple, ArrayList)):
        if isinstance(mask, Graphic):
            mask = mask.getShape()
        mask = [mask]
    else:
        for i in range(len(mask)):
            if isinstance(mask[i], Graphic):
                mask[i] = mask[i].getShape()

    r = GeometryUtil.maskout_Remove(data.asarray(), x.asarray(), y.asarray(), mask)
    return NDArray(r[0]), NDArray(r[1]), NDArray(r[2])


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
    elif isinstance(mask, NDArray):
        r = ArrayMath.maskin(data._array, mask._array)
        return data.array_wrap(r)

    if x is None or y is None:
        if isinstance(data, DimArray):
            x = data.dimvalue(data.ndim - 1)
            y = data.dimvalue(data.ndim - 2)
        else:
            return None

    if data.ndim == 2 and x.ndim == 1 and y.ndim == 1:
        x, y = np.meshgrid(x, y)

    if not isinstance(mask, (list, ArrayList)):
        mask = [mask]
    r = GeometryUtil.maskin(data._array, x._array, y._array, mask)
    return data.array_wrap(r)


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
    r = GeometryUtil.maskin_Remove(data._array, x._array, y._array, mask)
    return NDArray(r[0]), NDArray(r[1]), NDArray(r[2])


def projinfo(proj4string=None, proj='longlat', **kwargs):
    """
    Create a projection object with Proj.4 parameters (http://proj4.org/)
    
    :param proj4string: (*string*) Proj.4 projection string.
    :param proj: (*string*) Projection name.
    :param lat_0: (*float*) Latitude of origin.
    :param lon_0: (*float*) Central meridian.
    :param lat_1: (*float*) Latitude of first standard parallel.
    :param lat_2: (*float*) Latitude of second standard parallel.
    :param lat_ts: (*float*) Latitude of true scale.
    :param k: (*float*) Scaling factor.
    :param x_0: (*float*) False easting.
    :param y_0: (*float*) False northing.
    :param a: (*float*) Semi-major radius of the ellipsoid axis. Unit: meter.
    :param b: (*float*) Semi-minor radius of the ellipsoid axis. Unit: meter.
    :param h: (*float*) Height from earth surface.
    :param zone: (*int*) Projection zone - used in UTM or other projection with zone setting.
    
    :returns: (*ProjectionInfo*) ProjectionInfo object.
    """
    if not proj4string is None:
        return ProjectionInfo.factory(proj4string)

    if proj == 'longlat' and len(kwargs) == 0:
        return KnownCoordinateSystems.geographic.world.WGS1984

    projstr = '+proj=' + proj

    lat_0 = None
    lon_0 = None
    origin = kwargs.pop('origin', None)
    if not origin is None:
        lat_0 = origin[0]
        lon_0 = origin[1]

    lon_0 = kwargs.pop('lon_0', lon_0)
    if not lon_0 is None:
        projstr = projstr + ' +lon_0=' + str(lon_0)

    lat_0 = kwargs.pop('lat_0', lat_0)
    if not lat_0 is None:
        projstr = projstr + ' +lat_0=' + str(lat_0)

    lat_ts = kwargs.pop('truescalelat', None)
    lat_ts = kwargs.pop('lat_ts', lat_ts)
    if not lat_ts is None:
        projstr = projstr + ' +lat_ts=' + str(lat_ts)

    k = kwargs.pop('scalefactor', None)
    k = kwargs.pop('k', k)
    if not k is None:
        projstr = projstr + ' +k=' + str(k)

    lat_1 = None
    lat_2 = None
    paralles = kwargs.pop('paralles', None)
    if not paralles is None:
        lat_1 = paralles[0]
        if len(paralles) == 2:
            lat_2 = paralles[1]
        else:
            lat_2 = lat_1

    lat_1 = kwargs.pop('lat_1', lat_1)
    lat_2 = kwargs.pop('lat_2', lat_2)
    if not lat_1 is None:
        projstr = projstr + ' +lat_1=' + str(lat_1)
    if not lat_2 is None:
        projstr = projstr + ' +lat_2=' + str(lat_2)

    x_0 = kwargs.pop('falseeasting', None)
    y_0 = kwargs.pop('falsenorthing', None)
    x_0 = kwargs.pop('x_0', x_0)
    y_0 = kwargs.pop('y_0', y_0)
    if not x_0 is None:
        projstr = projstr + ' +x_0=' + str(x_0)
    if not y_0 is None:
        projstr = projstr + ' +y_0=' + str(y_0)

    a = kwargs.pop('a', None)
    if not a is None:
        projstr = projstr + ' +a=' + str(a)

    b = kwargs.pop('b', None)
    if not b is None:
        projstr = projstr + ' +b=' + str(b)

    h = kwargs.pop('h', None)
    if not h is None:
        projstr = projstr + ' +h=' + str(h)

    zone = kwargs.pop('zone', None)
    if not zone is None:
        projstr = projstr + ' +zone=' + str(zone)

    return ProjectionInfo.factory(projstr)


def project(x, y, fromproj=KnownCoordinateSystems.geographic.world.WGS1984,
            toproj=KnownCoordinateSystems.geographic.world.WGS1984):
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
        x = np.array(x)
    if isinstance(y, (tuple, list)):
        y = np.array(y)
    if isinstance(x, NDArray):
        outxy = Reproject.reproject(x.asarray(), y.asarray(), fromproj, toproj)
        return NDArray(outxy[0]), NDArray(outxy[1])
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
        xx = np.arange1(x, xnum, dx)
        yy = np.arange1(y, ynum, dy)
    else:
        llx = x - ((xnum - 1) * 0.5 * dx)
        lly = y - ((ynum - 1) * 0.5 * dy)
        xx = np.arange1(llx, xnum, dx)
        yy = np.arange1(lly, ynum, dy)
    return xx, yy


def reproject(a, x=None, y=None, fromproj=None, xp=None, yp=None, toproj=None, method='bilinear'):
    """
    Project array
    
    :param a: (*array_like*) Input array.
    :param x: (*array_like*) Input x coordinates.
    :param y: (*array_like*) Input y coordinates.
    :param fromproj: (*ProjectionInfo*) Input projection.
    :param xp: (*array_like*) Projected x coordinates.
    :param yp: (*array_like*) Projected y coordinates.
    :param toproj: (*ProjectionInfo*) Output projection.
    :param method: Interpolation method: ``bilinear`` or ``nearest``. Default is ``bilinear``.
    
    :returns: (*NDArray*) Projected array
    """
    if x is None or y is None:
        if isinstance(a, DimArray):
            y = a.dimvalue(a.ndim - 2)
            x = a.dimvalue(a.ndim - 1)
        else:
            raise ValueError('Input x/y coordinates are None')

    if fromproj is None:
        if isinstance(a, DimArray):
            fromproj = a.proj
        else:
            fromproj = KnownCoordinateSystems.geographic.world.WGS1984

    if toproj is None:
        toproj = KnownCoordinateSystems.geographic.world.WGS1984

    if method == 'bilinear':
        method = ResampleMethods.Bilinear
    else:
        method = ResampleMethods.NearestNeighbor

    if xp is None or yp is None:
        pr = Reproject.reproject(a._array, x._array, y._array, fromproj, toproj, method)
        return NDArray(pr[0]), NDArray(pr[1]), NDArray(pr[2])

    if isinstance(xp, (list, tuple)):
        xp = NDArray(xp)
    if isinstance(yp, (list, tuple)):
        yp = NDArray(yp)
    xp, yp = ArrayUtil.meshgrid(xp.asarray(), yp.asarray())
    r = Reproject.reproject(a.asarray(), x.aslist(), y.aslist(), xp, yp, fromproj, toproj, method)
    return NDArray(r)


def reproject_image(a, x=None, y=None, fromproj=None, xp=None, yp=None, toproj=None):
    """
    Project image data array

    :param a: (*array_like*) Input image data array - 3D [ny,nx,n-channel].
    :param x: (*array_like*) Input x coordinates.
    :param y: (*array_like*) Input y coordinates.
    :param fromproj: (*ProjectionInfo*) Input projection.
    :param xp: (*array_like*) Projected x coordinates.
    :param yp: (*array_like*) Projected y coordinates.
    :param toproj: (*ProjectionInfo*) Output projection.

    :returns: (*NDArray*) Projected array
    """
    if x is None or y is None:
        if isinstance(a, DimArray):
            y = a.dimvalue(a.ndim - 2)
            x = a.dimvalue(a.ndim - 1)
        else:
            raise ValueError('Input x/y coordinates are None')

    if fromproj is None:
        fromproj = KnownCoordinateSystems.geographic.world.WGS1984

    if toproj is None:
        toproj = KnownCoordinateSystems.geographic.world.WGS1984

    if xp is None or yp is None:
        pr = Reproject.reprojectImage(a.asarray(), x.asarray(), y.asarray(), fromproj, toproj)
        return NDArray(pr[0]), NDArray(pr[1]), NDArray(pr[2])

    if isinstance(xp, (list, tuple)):
        xp = NDArray(xp)
    if isinstance(yp, (list, tuple)):
        yp = NDArray(yp)
    xp, yp = ArrayUtil.meshgrid(xp.asarray(), yp.asarray())
    r = Reproject.reprojectImage(a.asarray(), x.asarray(), y.asarray(), xp, yp, fromproj, toproj)
    return NDArray(r)


def bilwrite(fn, data, x, y, proj=projinfo()):
    """
    Write a bil file from a 2D array.

    :param fn: (*str*) The output file name.
    :param data: (*array*) The 2D data array.
    :param x: (*array*) The x coordinate array - 1D.
    :param y: (*array*) The y coordinate array - 1D.
    :param proj: (*ProjectionInfo*) The projection. Default is long_lat projection.
    """
    GeoIOUtil.saveAsBILFile(fn, data.asarray(), x.asarray(), y.asarray(), proj)

def geojson_write(fn, graphics):
    """
    Save as GeoJSON file.

    :param fn: (*str*) GeoJSON file name.
    :param graphics: (*Graphics*) The graphics to be saved as GeoJSON file.
    """
    if isinstance(graphics, MILayer):
        graphics = MILayer._layer
    elif isinstance(graphics, GeoGraphicCollection):
        graphics = graphics.get_graphics()

    features = GeoJSONWriter.write(graphics)
    with open(fn, 'w') as f:
        f.write("{\n")
        f.write(""""type": "FeatureCollection",\n""")
        f.write(""""features": [\n""")
        for i in range(features.getNumFeatures()):
            feature = features.getFeature(i)
            f.write(feature.toString())
            if i < features.getNumFeatures() - 1:
                f.write(",\n")
            else:
                f.write("\n")
        f.write("]\n")
        f.write("}")
