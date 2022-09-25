# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2017-1-12
# Purpose: MeteoInfoLab linear algebra module
# Note: Jython
#-----------------------------------------------------

from org.meteoinfo.math.linalg import LinalgUtil
from org.meteoinfo.math.stats import StatsUtil

from ..core import NDArray

__all__ = [
    'solve','cholesky','det','lu','qr', 'svd','eig','inv','lstsq','slogdet','solve_triangular'
    ]

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
    r = NDArray(x)
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
    return NDArray(x)
    
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
    return NDArray(r)
    
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
    p = NDArray(r[0])
    l = NDArray(r[1])
    u = NDArray(r[2])
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
    q = NDArray(r[0])
    r = NDArray(r[1])
    return q, r

def svd(a, full_matrices=True):
    """
    Singular Value Decomposition.
    
    Factorizes the matrix a into two unitary matrices U and Vh, and
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
        
    Returns
    -------
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
    r = LinalgUtil.svd(a.asarray())
    #r = LinalgUtil.svd_EJML(a.asarray())
    U = NDArray(r[0])
    s = NDArray(r[1])
    Vh = NDArray(r[2])
    if not full_matrices:
        m, n = a.shape
        if m != n:
            k = min(m, n)
            if k == m:
                Vh = Vh[:k,:].copy()
            else:
                U = U[:,:k].copy()
    return U, s, Vh
    
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
    #r = LinalgUtil.eigen_EJML(a.asarray())
    w = NDArray(r[0])
    v = NDArray(r[1])
    return w, v
    
def inv(a):
    """
    Compute the (multiplicative) inverse of a matrix.
    
    :param a: (*array_like*) Input array.
    
    :returns: Inverse matrix.
    """
    r = LinalgUtil.inv(a.asarray())
    return NDArray(r)
    
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
    return NDArray(r[0]), NDArray(r[1])
    
def det(a):
    """
    Compute the determinant of an array.
    
    arameters
    ----------
    a : (..., M, M) array_like
        Input array to compute determinants for.
    Returns
    -------
    det : (...) array_like
        Determinant of `a`.
    """
    #r = LinalgUtil.determinantOfMatrix(a.asarray())
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