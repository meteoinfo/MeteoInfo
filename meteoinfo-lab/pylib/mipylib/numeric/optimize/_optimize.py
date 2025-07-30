
from ..lib._util import _RichResult


__all__ = ['OptimizeResult']


class OptimizeResult(_RichResult):
    """
    Represents the optimization result.

    Attributes
    ----------
    x : ndarray
        The solution of the optimization.
    success : bool
        Whether or not the optimizer exited successfully.
    status : int
        Termination status of the optimizer. Its value depends on the
        underlying solver. Refer to `message` for details.
    message : str
        Description of the cause of the termination.
    fun : float
        Value of objective function at `x`.
    jac, hess : ndarray
        Values of objective function's Jacobian and its Hessian at `x` (if
        available). The Hessian may be an approximation, see the documentation
        of the function in question.
    hess_inv : object
        Inverse of the objective function's Hessian; may be an approximation.
        Not available for all solvers. The type of this attribute may be
        either np.ndarray or scipy.sparse.linalg.LinearOperator.
    nfev, njev, nhev : int
        Number of evaluations of the objective functions and of its
        Jacobian and Hessian.
    nit : int
        Number of iterations performed by the optimizer.
    maxcv : float
        The maximum constraint violation.

    Notes
    -----
    Depending on the specific solver being used, `OptimizeResult` may
    not have all attributes listed here, and they may have additional
    attributes not listed here. Since this class is essentially a
    subclass of dict with attribute accessors, one can see which
    attributes are available using the `OptimizeResult.keys` method.

    """
    pass