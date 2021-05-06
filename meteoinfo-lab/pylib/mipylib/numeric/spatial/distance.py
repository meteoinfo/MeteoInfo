# coding=utf-8

from org.meteoinfo.math.spatial.distance import DistanceUtil

from ..core import numeric as np

__all__ = [
    'pdist'
    ]

def pdist(X, metric='euclidean', **kwargs):
    """
    Pairwise distances between observations in n-dimensional space.
    :param X: (*array*) An m by n array of m original observations in an n-dimensional space.
    :param metric: (*str*) The distance metric to use. The distance function can be ‘chebyshev’,
        ‘cityblock’, ‘correlation’, ‘euclidean’, ‘hamming’, ‘jaccard’, ‘jensenshannon’, ‘mahalanobis’,
        ‘matching’, ‘minkowski’.
    :param kwargs: Some possible arguments:

        p : scalar The p-norm to apply for Minkowski, weighted and unweighted. Default: 2.
    :return: （*array*) Pairwise distances, returned as a numeric row vector of length m(m–1)/2,
        corresponding to pairs of observations, where m is the number of observations in X.
    """
    if metric.lower() == 'minkowski':
        p = kwargs.pop('p', 2)
        _func = DistanceUtil.getDistanceFunc(metric, p)
    else:
        _func = DistanceUtil.getDistanceFunc(metric)

    if _func is None:
        return None
    else:
        if isinstance(X, (list, tuple)):
            X = np.array(X)
        r = DistanceUtil.calculateDistance(_func, X._array);
        return np.NDArray(r)