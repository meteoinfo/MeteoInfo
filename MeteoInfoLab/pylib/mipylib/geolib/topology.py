# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2014-12-26
# Purpose: MeteoInfoLab topology module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.data import ArrayUtil
from org.meteoinfo.shape import Graphic

__all__ = [
    'buffer','contains','convexhull','coveredby','covers','crosses','difference',
    'disjoint','equals','intersection','overlaps','reform','union','symdifference',
    'split','touches','within','asshape'
    ]
            
def asshape(a):
    '''
    Get shape from a graphic.
    
    :param a: (*Graphic*) The graphic.
    '''
    if isinstance(a, Graphic):
        return a.getShape()
    else:
        return a

def buffer(a, dis):
    '''
    Computes the buffer of a geometry by given distance.
    
    :param a: (*Shape*) The geometry shape.
    :param dis: (*float*) The buffer distance.
    
    :returns: Buffer polygon
    '''
    ap = asshape(a)
    r = ap.buffer(dis)
    return r
    
def contains(a, b):
    '''
    Tests whether the first geometry contains the second geometry.
    
    :param a: (*Shape*) The first geometry.
    :param b: (*Shape*) The second geometry.
    
    :returns: (*boolean*) Contains test result.
    '''
    ap = asshape(a)
    bp = asshape(b)
    return ap.contains(bp)
    
def convexhull(*args):
    '''
    Computes the smallest convex Polygon that contains all the points in the Geometry.
    '''
    if len(args) == 1:
        a = args[0]
        ap = asshape(a)
        r = ap.convexHull()
        return r
    else:
        x = args[0]
        y = args[1]
        r = ArrayUtil.convexHull(x.asarray(), y.asarray())
        return r    

def coveredby(a, b):
    '''
    Tests whether the first geometry covered by the second geometry.
    
    :param a: (*Shape*) The first geometry.
    :param b: (*Shape*) The second geometry.
    
    :returns: (*boolean*) Covered by test result.
    '''
    ap = asshape(a)
    bp = asshape(b)
    return ap.coveredBy(bp)
    
def covers(a, b):
    '''
    Tests whether the first geometry covers the second geometry.
    
    :param a: (*Shape*) The first geometry.
    :param b: (*Shape*) The second geometry.
    
    :returns: (*boolean*) Covers test result.
    '''
    ap = asshape(a)
    bp = asshape(b)
    return ap.covers(bp)
    
def crosses(a, b):
    '''
    Tests whether the first geometry crosses the second geometry.
    
    :param a: (*Shape*) The first geometry.
    :param b: (*Shape*) The second geometry.
    
    :returns: (*boolean*) Crosses test result.
    '''
    ap = asshape(a)
    bp = asshape(b)
    return ap.crosses(bp)

def difference(a, b):
    '''
    Computes a Geometry representing the closure of the point-set of the points contained in 
    the first Geometry that are not contained in the second Geometry.
    
    :param a: (*Shape*) The first geometry.
    :param b: (*Shape*) The second geometry.
    
    :returns: (*Shape*) Result geometry.
    '''
    ap = asshape(a)
    bp = asshape(b)
    r = ap.difference(bp)
    return r  

def disjoint(a, b):
    '''
    Tests whether the first geometry is disjoint from the second geometry.
    
    :param a: (*Shape*) The first geometry.
    :param b: (*Shape*) The second geometry.
    
    :returns: (*boolean*) Disjoint test result.
    '''
    ap = asshape(a)
    bp = asshape(b)
    return ap.disjoint(bp)
    
def equals(a, b):
    '''
    Tests whether the first geometry equals with the second geometry.
    
    :param a: (*Shape*) The first geometry.
    :param b: (*Shape*) The second geometry.
    
    :returns: (*boolean*) Equals test result.
    '''
    ap = asshape(a)
    bp = asshape(b)
    return ap.equals(bp)
    
def intersection(a, b):
    '''
    Computes a Geometry representing the point-set which is common to both the first Geometry and 
    the second Geometry.
    
    :param a: (*Shape*) The first geometry.
    :param b: (*Shape*) The second geometry.
    
    :returns: (*Shape*) Intersection result geometry.
    '''
    ap = asshape(a)
    bp = asshape(b)
    r = ap.intersection(bp)
    return r
    
def intersects(a, b):
    '''
    Tests whether the first geometry intersects the second geometry.
    
    :param a: (*Shape*) The first geometry.
    :param b: (*Shape*) The second geometry.
    
    :returns: (*boolean*) Intersects test result.
    '''
    ap = asshape(a)
    bp = asshape(b)
    return ap.intersects(bp)
    
def overlaps(a, b):
    '''
    Tests whether the first geometry overlaps the second geometry.
    
    :param a: (*Shape*) The first geometry.
    :param b: (*Shape*) The second geometry.
    
    :returns: (*boolean*) Overlaps test result.
    '''
    ap = asshape(a)
    bp = asshape(b)
    return ap.overlaps(bp)
    
def reform(a, b):
    '''
    Computes a new geometry from the first geometry reformed by the second geometry.
    
    :param a: (*Shape*) The first geometry.
    :param b: (*Shape*) The second geometry. Must be LineString.
    
    :returns: (*Shape*) Reform result geometry.
    '''
    ap = asshape(a)
    bp = asshape(b)
    r = ap.reform(bp)
    return r
    
def union(a, b):
    '''
    Computes the union of all the elements of the two geometries.
    
    :param a: (*Shape*) The first geometry.
    :param b: (*Shape*) The second geometry.
    
    :returns: (*Shape*) Union result geometry.
    '''
    ap = asshape(a)
    bp = asshape(b)
    r = ap.union(bp)
    return r
    
def symdifference(a, b):
    '''
    Computes a Geometry representing the closure of the point-set which is the union of the 
    points in the first Geometry which are not contained in the other Geometry, with the points in 
    the other Geometry not contained in the second Geometry.
    
    :param a: (*Shape*) The first geometry.
    :param b: (*Shape*) The second geometry.
    
    :returns: (*Shape*) Symdifference result geometry.
    '''
    ap = asshape(a)
    bp = asshape(b)
    r = ap.symDifference(bp)
    return r
    
def split(a, b):
    '''
    Split the first geometry by the second geometry.
    
    :param a: (*Shape*) The first geometry.
    :param b: (*Shape*) The second geometry.
    
    :returns: (*Shape*) Split result geometry.
    '''
    ap = asshape(a)
    bp = asshape(b)
    r = ap.split(bp)
    return r
    
def touches(a, b):
    '''
    Tests whether the first geometry touches the second geometry.
    
    :param a: (*Shape*) The first geometry.
    :param b: (*Shape*) The second geometry.
    
    :returns: (*boolean*) Touches test result.
    '''
    ap = asshape(a)
    bp = asshape(b)
    return ap.touches(bp)
    
def within(a, b):
    '''
    Tests whether the first geometry is within the second geometry.
    
    :param a: (*Shape*) The first geometry.
    :param b: (*Shape*) The second geometry.
    
    :returns: (*boolean*) Within test result.
    '''
    ap = asshape(a)
    bp = asshape(b)
    return ap.within(bp)