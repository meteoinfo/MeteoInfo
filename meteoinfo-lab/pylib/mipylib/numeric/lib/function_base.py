
from ..core import numeric as _nx
from ..core.fromnumeric import (ravel, nonzero)

__all__ = ['extract']

def extract(condition, arr):
    """
    Return the elements of an array that satisfy some condition.
    This is equivalent to ``np.compress(ravel(condition), ravel(arr))``.  If
    `condition` is boolean ``np.extract`` is equivalent to ``arr[condition]``.
    Note that `place` does the exact opposite of `extract`.
    Parameters
    ----------
    condition : array_like
        An array whose nonzero or True entries indicate the elements of `arr`
        to extract.
    arr : array_like
        Input array of the same size as `condition`.
    Returns
    -------
    extract : ndarray
        Rank 1 array of values from `arr` where `condition` is True.
    See Also
    --------
    take, put, copyto, compress, place
    Examples
    --------
    >>> arr = np.arange(12).reshape((3, 4))
    >>> arr
    array([[ 0,  1,  2,  3],
           [ 4,  5,  6,  7],
           [ 8,  9, 10, 11]])
    >>> condition = np.mod(arr, 3)==0
    >>> condition
    array([[ True, False, False,  True],
           [False, False,  True, False],
           [False,  True, False, False]])
    >>> np.extract(condition, arr)
    array([0, 3, 6, 9])
    If `condition` is boolean:
    >>> arr[condition]
    array([0, 3, 6, 9])
    """
    return _nx.take(ravel(arr), nonzero(ravel(condition))[0])