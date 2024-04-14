# coding=utf-8
# -----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2017-1-12
# Purpose: MeteoInfoLab linear algebra module
# Note: Jython
# -----------------------------------------------------

from org.meteoinfo.math.linalg import LinalgUtil
# from org.meteoinfo.math.linalg import LinalgUtilJava as LinalgUtil
from org.meteoinfo.math.stats import StatsUtil

from .. import core as np

__all__ = ['solve', 'cholesky', 'cond', 'det', 'lu', 'qr', 'svd', 'eig', 'eigvals', 'inv',
           'lstsq', 'slogdet', 'solve_triangular', 'norm', 'pinv', 'LinAlgError']


class LinAlgError(Exception):
    """
    Generic Python-exception-derived object raised by linalg functions.
    General purpose exception class, derived from Python's exception.Exception
    class, programmatically raised in linalg functions when a Linear
    Algebra-related condition would prevent further correct execution of the
    function.
    """
    pass


def _assert_2d(*arrays):
    for a in arrays:
        if a.ndim != 2:
            raise LinAlgError('%d-dimensional array given. Array must be '
                              'two-dimensional' % a.ndim)


def solve(a, b):
    """
    Solve a linear matrix equation, or system of linear scalar equations.
    
    Computes the "exact" solution, ``x``, of the well-determined, i.e., full
    rank, linear matrix equation ``ax = b``.
    
    ``Parameters``

    a : (M, M) array_like
        Coefficient matrix.
    b : {(M), (M, K)}, array_like
        Ordinate or "dependent variable" values.
        
    ``Returns``

    x : {(M), (M, K)} ndarray
        Solution to the system a x = b.  Returned shape is identical to ``b``.
    """
    _assert_2d(a)
    x = LinalgUtil.solve(a.asarray(), b.asarray())
    r = np.NDArray(x)
    return r


def solve_triangular(a, b, lower=False):
    """
    Solve the equation `a x = b` for `x`, assuming a is a triangular matrix.

    Parameters
    --------------
    a : (M, M) array_like
        A triangular matrix.
    b : {(M), (M, K)}, array_like
        Right-hand side matrix in `a x = b`
    lower : bool, optional
        Use only data contained in the lower triangle of `a`.
        Default is to use upper triangle.

    ``Returns``

    x : {(M), (M, K)} ndarray
        Solution to the system a x = b.  Returned shape is identical to ``b``.
    """
    x = LinalgUtil.solve(a.asarray(), b.asarray())
    return np.NDArray(x)


def cholesky(a, lower=True):
    """
    Cholesky decomposition.
    
    Return the Cholesky decomposition, `L * L.H`, of the square matrix `a`,
    where `L` is lower-triangular and .H is the conjugate transpose operator
    (which is the ordinary transpose if `a` is real-valued).  `a` must be
    Hermitian (symmetric if real-valued) and positive-definite.  Only `L` is
    actually returned.
    
    Parameters
    ----------
    a : (M, M) array_like
        Hermitian (symmetric if all elements are real), positive-definite
        input matrix.
    lower : bool
        Return lower or upper triangle matrix. Default is lower.
        
    Returns
    -------
    L : (M, M) array_like
        Upper or lower-triangular Cholesky factor of `a`.  Returns a
        matrix object if `a` is a matrix object.
    """
    r = LinalgUtil.cholesky(a.asarray(), lower)
    return np.NDArray(r)


def lu(a):
    """
    Compute pivoted LU decomposition of a matrix.
    
    The decomposition is::
    
        A = P L U
        
    where P is a permutation matrix, L lower triangular with unit
    diagonal elements, and U upper triangular.
    
    Parameters
    ----------
    a : (M, M) array_like
        Array to decompose
    permute_l : bool, optional
        Perform the multiplication P*L  (Default: do not permute)
    overwrite_a : bool, optional
        Whether to overwrite data in a (may improve performance)
    check_finite : bool, optional
        Whether to check that the input matrix contains only finite numbers.
        Disabling may give a performance gain, but may result in problems
        (crashes, non-termination) if the inputs do contain infinities or NaNs.
        
    Returns
    -------
    p : (M, M) ndarray
        Permutation matrix
    l : (M, M) ndarray
        Lower triangular or trapezoidal matrix with unit diagonal.
    u : (M, M) ndarray
        Upper triangular or trapezoidal matrix
    """
    r = LinalgUtil.lu(a.asarray())
    p = np.NDArray(r[0])
    l = np.NDArray(r[1])
    u = np.NDArray(r[2])
    return p, l, u


def qr(a):
    """
    Compute QR decomposition of a matrix.
    
    Calculate the decomposition ``A = Q R`` where Q is unitary/orthogonal
    
    and R upper triangular.
    
    Parameters
    ----------
    a : (M, N) array_like
        Matrix to be decomposed
    
    Returns
    -------
    Q : float or complex ndarray
        Of shape (M, M), or (M, K) for ``mode='economic'``.  Not returned
        if ``mode='r'``.
    R : float or complex ndarray
        Of shape (M, N), or (K, N) for ``mode='economic'``.  ``K = min(M, N)``.
    """
    r = LinalgUtil.qr(a.asarray())
    q = np.NDArray(r[0])
    r = np.NDArray(r[1])
    return q, r


def svd(a, full_matrices=True, compute_uv=True):
    """
    Singular Value Decomposition.
    
    Factorizes the matrix `a` into two unitary matrices U and Vh, and
    a 1-D array s of singular values (real, non-negative) such that
    ``a == U*S*Vh``, where S is a suitably shaped matrix of zeros with
    main diagonal s.
    
    Parameters
    ----------
    a : (M, N) array_like
        Matrix to decompose.
    full_matrices: bool, optional
        If True (default), u and vh have the shapes (..., M, M) and (..., N, N), respectively.
        Otherwise, the shapes are (..., M, K) and (..., K, N), respectively, where K = min(M, N).
    compute_uv:bool, optional
        Whether to compute u and vh in addition to s. True by default.
        
    Returns
    -------
    When `compute_uv` is True, the result is a namedtuple with the following attribute names:

    U : ndarray
        Unitary matrix having left singular vectors as columns.
        Of shape ``(M,K)``.
    s : ndarray
        The singular values, sorted in non-increasing order.
        Of shape (K,), with ``K = min(M, N)``.
    Vh : ndarray
        Unitary matrix having right singular vectors as rows.
        Of shape ``(N,N)``.
    """
    r = LinalgUtil.svd(a.asarray(), full_matrices)
    # r = LinalgUtil.svd_EJML(a.asarray())
    s = np.NDArray(r[1])
    if compute_uv:
        U = np.NDArray(r[0])
        Vh = np.NDArray(r[2])
        return U, s, Vh
    else:
        return s


def eig(a):
    """
    Compute the eigenvalues and right eigenvectors of a square array.

    Parameters
    ----------
    a : (M, M) array
        Matrices for which the eigenvalues and right eigenvectors will
        be computed
        
    Returns
    -------
    w : (M) array
        The eigenvalues, each repeated according to its multiplicity.
        The eigenvalues are not necessarily ordered. The resulting
        array will be of complex type, unless the imaginary part is
        zero in which case it will be cast to a real type. When `a`
        is real the resulting eigenvalues will be real (0 imaginary
        part) or occur in conjugate pairs
    v : (M, M) array
        The normalized (unit "length") eigenvectors, such that the
        column ``v[:,i]`` is the eigenvector corresponding to the
        eigenvalue ``w[i]``.
    """
    r = LinalgUtil.eigen(a.asarray())
    # r = LinalgUtil.eigen_EJML(a.asarray())
    w = np.NDArray(r[0])
    v = np.NDArray(r[1])
    return w, v


def eigvals(a):
    """
    Compute the eigenvalues of a general matrix.

    Main difference between `eigvals` and `eig`: the eigenvectors aren't
    returned.

    Parameters
    ----------
    a : (M, M) array
        Matrices for which the eigenvalues and right eigenvectors will
        be computed

    Returns
    -------
    w : (M) array
        The eigenvalues, each repeated according to its multiplicity.
        The eigenvalues are not necessarily ordered. The resulting
        array will be of complex type, unless the imaginary part is
        zero in which case it will be cast to a real type. When `a`
        is real the resulting eigenvalues will be real (0 imaginary
        part) or occur in conjugate pairs
    v : (M, M) array
        The normalized (unit "length") eigenvectors, such that the
        column ``v[:,i]`` is the eigenvector corresponding to the
        eigenvalue ``w[i]``.
    """
    r = LinalgUtil.eigen(a.asarray())
    # r = LinalgUtil.eigen_EJML(a.asarray())
    w = np.NDArray(r[0])
    return w


def inv(a):
    """
    Compute the (multiplicative) inverse of a matrix.
    
    :param a: (*array_like*) Input array.
    
    :returns: Inverse matrix.
    """
    a = np.asarray(a)
    r = LinalgUtil.inv(a.asarray())
    return np.NDArray(r)


def pinv(a):
    """
    Compute the pseudo inverse of a matrix.

    :param a: (*array_like*) Input array.

    :returns: Pseudo inverse matrix.
    """
    a = np.asarray(a)
    r = LinalgUtil.pinv(a.asarray())
    return np.NDArray(r)


def lstsq(a, b):
    """
    Compute least-squares solution to equation Ax = b.

    Compute a vector x such that the 2-norm |b - A x| is minimized.
    
    Parameters
    ----------
    a : (M, N) array
        Left hand side matrix (2-D array).
    b : (M,) array
        Right hand side vector.
        
    Returns
    -------
    x : (N,) array
        Least-squares solution. Return shape matches shape of b.
    residues : (0,) or () or (K,) ndarray
        Sums of residues, squared 2-norm for each column in b - a x.
    """
    r = StatsUtil.multipleLineRegress_OLS(b.asarray(), a.asarray(), True)
    return np.NDArray(r[0]), np.NDArray(r[1])


def det(a):
    """
    Compute the determinant of an array.
    
    parameters
    ----------
    a : (..., M, M) array_like
        Input array to compute determinants for.

    Returns
    -------
    det : (...) array_like
        Determinant of `a`.
    """
    # r = LinalgUtil.determinantOfMatrix(a.asarray())
    r = LinalgUtil.det(a.asarray())
    return r


def slogdet(a):
    """
    Compute the sign and (natural) logarithm of the determinant of an array.

    :param a: (*array_like*) Input array, has to be a square 2-D array.

    :return: Sign and logarithm of the determinant.
    """
    r = LinalgUtil.sLogDet(a.asarray())
    return r[0], r[1]


def _multi_svd_norm(x, row_axis, col_axis, op):
    """Compute a function of the singular values of the 2-D matrices in `x`.

    This is a private utility function used by `numpy.linalg.norm()`.

    Parameters
    ----------
    x : ndarray
    row_axis, col_axis : int
        The axes of `x` that hold the 2-D matrices.
    op : callable
        This should be either amin or `amax` or `sum`.

    Returns
    -------
    result : float or ndarray
        If `x` is 2-D, the return values is a float.
        Otherwise, it is an array with ``x.ndim - 2`` dimensions.
        The return values are either the minimum or maximum or sum of the
        singular values of the matrices, depending on whether `op`
        is `amin` or `amax` or `sum`.

    """
    y = np.moveaxis(x, (row_axis, col_axis), (-2, -1))
    result = op(svd(y, compute_uv=False), axis=-1)
    return result


def norm(x, ord=None, axis=None, keepdims=False):
    """
    Matrix or vector norm.

    This function is able to return one of eight different matrix norms, or one of an infinite number
    of vector norms (described below), depending on the value of the ord parameter.

    Parameters
    ----------
    x : array_like
        Input array.  If `axis` is None, `x` must be 1-D or 2-D, unless `ord`
        is None. If both `axis` and `ord` are None, the 2-norm of
        ``x.ravel`` will be returned.
    ord : {non-zero int, inf, -inf, 'fro', 'nuc'}, optional
        Order of the norm (see table under ``Notes``). inf means numpy's
        `inf` object. The default is None.
    axis : {None, int, 2-tuple of ints}, optional.
        If `axis` is an integer, it specifies the axis of `x` along which to
        compute the vector norms.  If `axis` is a 2-tuple, it specifies the
        axes that hold 2-D matrices, and the matrix norms of these matrices
        are computed.  If `axis` is None then either a vector norm (when `x`
        is 1-D) or a matrix norm (when `x` is 2-D) is returned. The default
        is None.
    keepdims: bool, optional
        If this is set to True, the axes which are normed over are left in the
        result as dimensions with size one. With this option the result will
        broadcast correctly against the original x.

    Returns
    -------
    n : float or ndarray
        Norm of the matrix or vector(s).
    """
    x = np.asarray(x)

    # Immediately handle some default, simple, fast, and common cases.
    if axis is None:
        ndim = x.ndim
        if ((ord is None) or
                (ord in ('f', 'fro') and ndim == 2) or
                (ord == 2 and ndim == 1)):

            x = x.ravel()
            if x.dtype == np.dtype.complex:
                x_real = x.real
                x_imag = x.imag
                sqnorm = x_real.dot(x_real) + x_imag.dot(x_imag)
            else:
                sqnorm = x.dot(x)
            ret = np.sqrt(sqnorm)
            if keepdims:
                ret = ret.reshape(ndim * [1])
            return ret

    # Normalize the `axis` argument to a tuple.
    nd = x.ndim
    if axis is None:
        axis = tuple(range(nd))
    elif not isinstance(axis, tuple):
        try:
            axis = int(axis)
        except Exception as e:
            raise TypeError("'axis' must be None, an integer or a tuple of integers")
        axis = (axis,)

    if len(axis) == 1:
        if ord == np.inf:
            return np.abs(x).max(axis=axis)
        elif ord == -np.inf:
            return np.abs(x).min(axis=axis)
        elif ord == 0:
            # Zero norm
            return (x != 0).astype(x.real.dtype).sum(axis=axis)
        elif ord == 1:
            return x.abs().sum(axis=axis)
        elif ord is None or ord == 2:
            s = (x.conj() * x).real
            return np.sqrt(s.sum(axis=axis))
        # None of the str-type keywords for ord ('fro', 'nuc')
        # are valid for vectors
        elif isinstance(ord, str):
            raise ValueError("Invalid norm order {} for vectors".format(ord))
        else:
            absx = np.abs(x)
            absx **= ord
            ret = absx.sum(axis=axis)
            ret **= (1. / ord)
            return ret
    elif len(axis) == 2:
        row_axis, col_axis = axis
        row_axis = np.normalize_axis_index(row_axis, nd)
        col_axis = np.normalize_axis_index(col_axis, nd)
        if row_axis == col_axis:
            raise ValueError('Duplicate axes given.')
        if ord == 2:
            ret =  _multi_svd_norm(x, row_axis, col_axis, np.amax)
        elif ord == -2:
            ret = _multi_svd_norm(x, row_axis, col_axis, np.amin)
        elif ord == 1:
            if col_axis > row_axis:
                col_axis -= 1
            ret = np.sum(np.abs(x), axis=row_axis).max(axis=col_axis)
        elif ord == np.inf:
            if row_axis > col_axis:
                row_axis -= 1
            ret = np.sum(np.abs(x), axis=col_axis).max(axis=row_axis)
        elif ord == -1:
            if col_axis > row_axis:
                col_axis -= 1
            ret = np.sum(np.abs(x), axis=row_axis).min(axis=col_axis)
        elif ord == -np.inf:
            if row_axis > col_axis:
                row_axis -= 1
            ret = np.sum(np.abs(x), axis=col_axis).min(axis=row_axis)
        elif ord in [None, 'fro', 'f']:
            ret = np.sqrt(np.sum((x.conj() * x).real, axis=axis))
        elif ord == 'nuc':
            ret = _multi_svd_norm(x, row_axis, col_axis, np.sum)
        else:
            raise ValueError("Invalid norm order for matrices.")
        if keepdims:
            ret_shape = list(x.shape)
            ret_shape[axis[0]] = 1
            ret_shape[axis[1]] = 1
            ret = ret.reshape(ret_shape)
        return ret
    else:
        raise ValueError("Improper number of dimensions to norm.")


def cond(x, p=None):
    """
    Compute the condition number of a matrix.

    This function is capable of returning the condition number using
    one of seven different norms, depending on the value of `p` (see
    Parameters below).

    Parameters
    ----------
    x : (..., M, N) array_like
        The matrix whose condition number is sought.
    p : {None, 1, -1, 2, -2, inf, -inf, 'fro'}, optional
        Order of the norm used in the condition number computation:

        =====  ============================
        p      norm for matrices
        =====  ============================
        None   2-norm, computed directly using the ``SVD``
        'fro'  Frobenius norm
        inf    max(sum(abs(x), axis=1))
        -inf   min(sum(abs(x), axis=1))
        1      max(sum(abs(x), axis=0))
        -1     min(sum(abs(x), axis=0))
        2      2-norm (largest sing. value)
        -2     smallest singular value
        =====  ============================

        inf means the `numpy.inf` object, and the Frobenius norm is
        the root-of-sum-of-squares norm.

    Returns
    -------
    c : {float, inf}
        The condition number of the matrix. May be infinite.

    See Also
    --------
    numpy.linalg.norm

    Notes
    -----
    The condition number of `x` is defined as the norm of `x` times the
    norm of the inverse of `x` [1]_; the norm can be the usual L2-norm
    (root-of-sum-of-squares) or one of a number of other matrix norms.

    References
    ----------
    .. [1] G. Strang, *Linear Algebra and Its Applications*, Orlando, FL,
           Academic Press, Inc., 1980, pg. 285.

    Examples
    --------
    >>> from mipylib.numeric import linalg as LA
    >>> a = np.array([[1, 0, -1], [0, 1, 0], [1, 0, 1]])
    >>> a
    array([[ 1,  0, -1],
           [ 0,  1,  0],
           [ 1,  0,  1]])
    >>> LA.cond(a)
    1.4142135623730951
    >>> LA.cond(a, 'fro')
    3.1622776601683795
    >>> LA.cond(a, np.inf)
    2.0
    >>> LA.cond(a, -np.inf)
    1.0
    >>> LA.cond(a, 1)
    2.0
    >>> LA.cond(a, -1)
    1.0
    >>> LA.cond(a, 2)
    1.4142135623730951
    >>> LA.cond(a, -2)
    0.70710678118654746 # may vary
    >>> min(LA.svd(a, compute_uv=False))*min(LA.svd(LA.inv(a), compute_uv=False))
    0.70710678118654746 # may vary
    """
    x = np.asarray(x)  # in case we have a matrix
    if p is None or p == 2 or p == -2:
        s = svd(x, compute_uv=False)
        if p == -2:
            r = s[..., -1] / s[..., 0]
        else:
            r = s[..., 0] / s[..., -1]
    else:
        # Call inv(x) ignoring errors. The result array will
        # contain nans in the entries where inversion failed.
        invx = inv(x)
        r = norm(x, p, axis=(-2, -1)) * norm(invx, p, axis=(-2, -1))

    return r