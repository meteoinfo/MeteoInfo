from org.meteothink.imep.verification import VerifyStat, DataRange, DichotomousMethod, MultiCategoryMethod, \
    ContinuousMethod, ScoreMethod
from mipylib.numeric.core import NDArray
from java.time import LocalDateTime

__all__ = ['categorical','aggregate','datarange','verifymethod','addscore','verifytable',
           'saveverifyfile']

def categorical(obs, fcst, min=None, max=None, values=None):
    """
    Categoraical calculation
    
    :param obs: (*array_like*) Observation data.
    :param fcst: (*array_like*) Forecast data.
    :param min: (*Number*) Minimum value of data range.
    :param max: (*Number*) Maximum value of data range.
    :param values: (*list*) Values of data range - not using min/max.
    
    :returns: (*array_like*) Categoraical result data.
    """
    if values is None:
        drange = DataRange(min, max)
    else:
        drange = DataRange(values)
    return NDArray(VerifyStat.categorical(obs.asarray(), fcst.asarray(), drange))
    
def aggregate(data):
    """
    Aggregate calculation
    
    :param data: (*array_like*) Categoraical data for aggregate calculation.
    
    :returns: (*ContingencyTable*) Contingency table.
    """
    return VerifyStat.aggregate(data.asarray());
    
def datarange(min=None, max=None, minequal=True, maxequal=True, values=None):
    """
    Return data range object.

    :param min: (*Number*) Minimum value of data range.
    :param max: (*Number*) Maximum value of data range.
    :param minequal: (*boolean*) Including minimum value or not in the data range.
    :param maxequal: (*boolean*) Including maximum value of not in the data range.
    :param values: (*list*) Values of data range (not using min/max).
        
    :returns: (*DataRange*) Data range object.
    """
    if values is None:
        drange = DataRange(min, max, minequal, maxequal)
    else:
        drange = DataRange(values)
    return drange
    
def verifymethod(method='dichotomous', drange=None, min=None, max=None, minequal=True, maxequal=True, values=None):
    """
    Return verification method object.
    
    :param method: (*string*) Verification method: ``'dichotomous'``, ``'multicategory'``,
        ``'continuous'``, ``score``. Default is ``'dichotomous'``.
    :param drange: (*DataRange*) Data range. Default is None.
    :param min: (*Number*) Minimum value of data range.
    :param max: (*Number*) Maximum value of data range.
    :param minequal: (*boolean*) Including minimum value or not in the data range.
    :param maxequal: (*boolean*) Including maximum value of not in the data range.
    :param values: (*list*) Values of data range (not using min/max) for dichotomous method, or
        values for multicategory method.
        
    :returns: (*VerifyMethod*) Verification method object.
    """
    if method == 'dichotomous':
        if drange is None:
            if values is None:
                drange = DataRange(min, max, minequal, maxequal)
            else:
                drange = DataRange(values)
        return DichotomousMethod(drange)
    elif method == 'multicategory':
        if values is None:
            return MultiCategoryMethod()
        else:
            return MultiCategoryMethod(values)
    elif method == 'continuous':
        return ContinuousMethod()
    elif method == 'score':
        return ScoreMethod()
    else:
        print 'Not supported method: ' + method
        return None
        
def addscore(method, obs, fcsts, scores):
    """
    Add a score record in a score method.
    
    :param method: (*ScoreMethod*) The score method.
    :param obs: (*DataRange*) Observation data range.
    :param fcsts: (*list of DataRange*) forecast data range list.
    :param scores: (*list of number*) Score value list.
    """
    method.addScore(obs, fcsts, scores)
    
def verifytable(obs, fcst, method):
    """
    Calculate verify table.
    
    :param obs: (*array_like*) Observation data.
    :param fcst: (*array_like*) Forecast data.
    :param method: (*VerifyMethod*) Verification method.
    
    :returns: (*VerifyTable*) Verification table.
    """
    return VerifyStat.getVerifyTable(obs.asarray(), fcst.asarray(), method)
    
def saveverifyfile(filename, vtables, times=None):
    """
    Save verification result cvs file.
    
    :param filename: (*string*) Output file name.
    :param vtables: (*list or VerifyTable*) Verification table list.
    :param times: (*list or datetime*) Times corresponding to virification tables.
    """
    if not isinstance(vtables, list):
        vtables = [vtables]
        if not times is None:
            times = [times]
    if times is None:
        VerifyStat.writeVerifyFile(vtables, filename)
    else:
        dates = []
        for t in times:
            d = LocalDateTime.of(t.year, t.month, t.day, t.hour, t.minute, t.second)
            dates.append(d)
        VerifyStat.writeVerifyFile(vtables, dates, filename)
    
def test():
    print 'Test passed!'