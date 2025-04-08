from org.meteoinfo.ndarray.math import ArrayUtil

from .. import core as np


__all__ = ['toeplitz']


# -----------------------------------------------------------------------------
#  matrix construction functions
# -----------------------------------------------------------------------------


def toeplitz(c, r=None):
    r"""
    Construct a Toeplitz matrix.

    The Toeplitz matrix has constant diagonals, with c as its first column
    and r as its first row. If r is not given, ``r == conjugate(c)`` is
    assumed.

    Parameters
    ----------
    c : array_like
        First column of the matrix.
    r : array_like, optional
        First row of the matrix. If None, ``r = conjugate(c)`` is assumed;
        in this case, if c[0] is real, the result is a Hermitian matrix.
        r[0] is ignored; the first row of the returned matrix is
        ``[c[0], r[1:]]``.

        .. warning::

            Beginning in SciPy 1.17, multidimensional input will be treated as a batch,
            not ``ravel``\ ed. To preserve the existing behavior, ``ravel`` arguments
            before passing them to `toeplitz`.

    Returns
    -------
    A : (len(c), len(r)) ndarray
        The Toeplitz matrix. Dtype is the same as ``(c[0] + r[0]).dtype``.

    See Also
    --------
    circulant : circulant matrix
    hankel : Hankel matrix
    solve_toeplitz : Solve a Toeplitz system.

    Notes
    -----
    The behavior when `c` or `r` is a scalar, or when `c` is complex and
    `r` is None, was changed in version 0.8.0. The behavior in previous
    versions was undocumented and is no longer supported.

    Examples
    --------
    >>> from scipy.linalg import toeplitz
    >>> toeplitz([1,2,3], [1,4,5,6])
    array([[1, 4, 5, 6],
           [2, 1, 4, 5],
           [3, 2, 1, 4]])
    >>> toeplitz([1.0, 2+3j, 4-1j])
    array([[ 1.+0.j,  2.-3.j,  4.+1.j],
           [ 2.+3.j,  1.+0.j,  2.-3.j],
           [ 4.-1.j,  2.+3.j,  1.+0.j]])

    """
    c = np.asarray(c)
    if r is None:
        r = c.conjugate()
    else:
        r = np.asarray(r)

    ta = ArrayUtil.toeplitz(c._array, r._array)

    return np.NDArray(ta)
