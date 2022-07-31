#from org.apache.commons.math3.analysis import UnivariateFunction
from org.meteoinfo.math.optimize import OptimizeUtil, ParamUnivariateFunction
from org.apache.commons.math3.fitting.leastsquares import LeastSquaresBuilder, LevenbergMarquardtOptimizer

import warnings
from ..core import numeric as np
from ._lsq.least_squares import prepare_bounds
from ..linalg import cholesky, solve_triangular, svd
from ..lib._util import _lazywhere

#__all__ = ['fsolve', 'leastsq', 'fixed_point', 'curve_fit']
__all__ = ['curve_fit','fixed_point']

def _check_func(checker, argname, thefunc, x0, args, numinputs,
                output_shape=None):
    res = np.atleast_1d(thefunc(*((x0[:numinputs],) + args)))
    if (output_shape is not None) and (np.shape(res) != output_shape):
        if (output_shape[0] != 1):
            if len(output_shape) > 1:
                if output_shape[1] == 1:
                    return shape(res)
            msg = "%s: there is a mismatch between the input and output " \
                  "shape of the '%s' argument" % (checker, argname)
            func_name = getattr(thefunc, '__name__', None)
            if func_name:
                msg += " '%s'." % func_name
            else:
                msg += "."
            msg += 'Shape should be %s but it is %s.' % (output_shape, np.shape(res))
            raise TypeError(msg)
    return np.shape(res), res.dtype

def _wrap_func(func, xdata, ydata, transform):
    if transform is None:
        def func_wrapped(params):
            return func(xdata, *params) - ydata
    elif transform.ndim == 1:
        def func_wrapped(params):
            return transform * (func(xdata, *params) - ydata)
    else:
        # Chisq = (y - yd)^T C^{-1} (y-yd)
        # transform = L such that C = L L^T
        # C^{-1} = L^{-T} L^{-1}
        # Chisq = (y - yd)^T L^{-T} L^{-1} (y-yd)
        # Define (y-yd)' = L^{-1} (y-yd)
        # by solving
        # L (y-yd)' = (y-yd)
        # and minimize (y-yd)'^T (y-yd)'
        def func_wrapped(params):
            return solve_triangular(transform, func(xdata, *params) - ydata, lower=True)
    return func_wrapped

def _wrap_jac(jac, xdata, transform):
    if transform is None:
        def jac_wrapped(params):
            return jac(xdata, *params)
    elif transform.ndim == 1:
        def jac_wrapped(params):
            return transform[:, np.newaxis] * np.asarray(jac(xdata, *params))
    else:
        def jac_wrapped(params):
            return solve_triangular(transform, np.asarray(jac(xdata, *params)), lower=True)
    return jac_wrapped

def _initialize_feasible(lb, ub):
    p0 = np.ones_like(lb)
    lb_finite = np.isfinite(lb)
    ub_finite = np.isfinite(ub)

    mask = lb_finite & ub_finite
    p0[mask] = 0.5 * (lb[mask] + ub[mask])

    mask = lb_finite & ~ub_finite
    p0[mask] = lb[mask] + 1

    mask = ~lb_finite & ub_finite
    p0[mask] = ub[mask] - 1

    return p0

class UniFunc(ParamUnivariateFunction):
    def __init__(self, f):
        """
        Initialize

        :param f: Jython function
        """
        self.f = f
        self._args = list(f.__code__.co_varnames)[1:]
        self._args = tuple(self._args)
        self.order = len(self._args)

    def value(self, x):
        args = tuple(self.getParameters())
        return self.f(x, *args)

def curve_fit(f, xdata, ydata, p0=None, sigma=None, absolute_sigma=False,
              check_finite=True, bounds=(-np.inf, np.inf), method=None,
              jac=None, **kwargs):
    """
    Use non-linear least squares to fit a function, f, to data.

    Assumes ``ydata = f(xdata, *params) + eps``

    :param f: callable
        The model function, f(x, ...).  It must take the independent
        variable as the first argument and the parameters to fit as
        separate remaining arguments.
    :param xdata: array_like or object
        The independent variable where the data is measured.
        Should usually be an M-length sequence or an (k,M)-shaped array for
        functions with k predictors, but can actually be any object.
    :param ydata: array_like
        The dependent data, a length M array - nominally ``f(xdata, ...)``.
    :param p0: array_like, optional
        Initial guess for the parameters (length N).  If None, then the
        initial values will all be 1 (if the number of parameters for the
        function can be determined using introspection, otherwise a
        ValueError is raised).
    :return:
    """
    if p0 is None:
        # determine number of parameters by inspecting the function
        from ..lib._util import getargspec_no_self as _getargspec
        args, varargs, varkw, defaults = _getargspec(f)
        if len(args) < 2:
            raise ValueError("Unable to determine number of fit parameters.")
        n = len(args) - 1
        p0 = np.ones(n)
    else:
        p0 = np.atleast_1d(p0)
        n = p0.size

    func = UniFunc(f)
    best = OptimizeUtil.curveFit(func, xdata.asarray(), ydata.asarray(), 5, 0.1, p0.tojarray('double'))
    r = tuple(best)

    return r

# def curve_fit(f, xdata, ydata, p0=None, sigma=None, absolute_sigma=False,
#               check_finite=True, bounds=(-np.inf, np.inf), method=None,
#               jac=None, **kwargs):
#     """
#     Use non-linear least squares to fit a function, f, to data.
#
#     Assumes ``ydata = f(xdata, *params) + eps``
#
#     :param f: callable
#         The model function, f(x, ...).  It must take the independent
#         variable as the first argument and the parameters to fit as
#         separate remaining arguments.
#     :param xdata: array_like or object
#         The independent variable where the data is measured.
#         Should usually be an M-length sequence or an (k,M)-shaped array for
#         functions with k predictors, but can actually be any object.
#     :param ydata: array_like
#         The dependent data, a length M array - nominally ``f(xdata, ...)``.
#     :param p0: array_like, optional
#         Initial guess for the parameters (length N).  If None, then the
#         initial values will all be 1 (if the number of parameters for the
#         function can be determined using introspection, otherwise a
#         ValueError is raised).
#     :return:
#     """
#     if p0 is None:
#         # determine number of parameters by inspecting the function
#         from ..lib._util import getargspec_no_self as _getargspec
#         args, varargs, varkw, defaults = _getargspec(f)
#         if len(args) < 2:
#             raise ValueError("Unable to determine number of fit parameters.")
#         n = len(args) - 1
#         p0 = np.ones(n)
#     else:
#         p0 = np.atleast_1d(p0)
#         n = p0.size
#
#     func = UniFunc(f)
#     jac_func = OptimizeUtil.getJacobianFunction(func, xdata.asarray(), func.order, 5, 0.1)
#     problem = LeastSquaresBuilder().start(p0.tojarray('double')). \
#         model(jac_func). \
#         target(ydata.tojarray('double')). \
#         lazyEvaluation(False). \
#         maxEvaluations(1000). \
#         maxIterations(1000). \
#         build()
#     optimum = LevenbergMarquardtOptimizer().optimize(problem)
#     r = tuple(optimum.getPoint().toArray())
#
#     return r

def _del2(p0, p1, d):
    return p0 - np.square(p1 - p0) / d


def _relerr(actual, desired):
    return (actual - desired) / desired

def _fixed_point_helper(func, x0, args, xtol, maxiter, use_accel):
    p0 = x0
    for _ in range(maxiter):
        p1 = func(p0, *args)
        if use_accel:
            p2 = func(p1, *args)
            d = p2 - 2.0 * p1 + p0
            p = _lazywhere(d != 0, (p0, p1, d), f=_del2, fillvalue=p2)
        else:
            p = p1
        relerr = _lazywhere(p0 != 0, (p, p0), f=_relerr, fillvalue=p)
        if np.all(np.abs(relerr) < xtol):
            return p
        p0 = p
    msg = "Failed to converge after %d iterations, value is %s" % (maxiter, p)
    raise RuntimeError(msg)


def fixed_point(func, x0, args=(), xtol=1e-8, maxiter=500, method='del2'):
    """
    Find a fixed point of the function.
    Given a function of one or more variables and a starting point, find a
    fixed point of the function: i.e., where ``func(x0) == x0``.
    Parameters
    ----------
    func : function
        Function to evaluate.
    x0 : array_like
        Fixed point of function.
    args : tuple, optional
        Extra arguments to `func`.
    xtol : float, optional
        Convergence tolerance, defaults to 1e-08.
    maxiter : int, optional
        Maximum number of iterations, defaults to 500.
    method : {"del2", "iteration"}, optional
        Method of finding the fixed-point, defaults to "del2",
        which uses Steffensen's Method with Aitken's ``Del^2``
        convergence acceleration [1]_. The "iteration" method simply iterates
        the function until convergence is detected, without attempting to
        accelerate the convergence.
    References
    ----------
    .. [1] Burden, Faires, "Numerical Analysis", 5th edition, pg. 80
    Examples
    --------
    >>> from mipylib.numeric import optimize
    >>> def func(x, c1, c2):
    ...    return np.sqrt(c1/(x+c2))
    >>> c1 = np.array([10,12.])
    >>> c2 = np.array([3, 5.])
    >>> optimize.fixed_point(func, [1.2, 1.3], args=(c1,c2))
    array([ 1.4920333 ,  1.37228132])
    """
    use_accel = {'del2': True, 'iteration': False}[method]
    #x0 = _asarray_validated(x0, as_inexact=True)
    x0 = np.asarray(x0)
    return _fixed_point_helper(func, x0, args, xtol, maxiter, use_accel)