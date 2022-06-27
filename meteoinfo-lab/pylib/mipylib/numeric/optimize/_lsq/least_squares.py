from org.apache.commons.math3.analysis import UnivariateFunction
from org.meteoinfo.math.optimize import OptimizeUtil
from org.apache.commons.math3.fitting.leastsquares import LeastSquaresBuilder, LevenbergMarquardtOptimizer
#import mipylib.numeric as np
from ...core import numeric as np
from .common import in_bounds

def prepare_bounds(bounds, n):
    lb, ub = [np.asarray(b, dtype='float') for b in bounds]
    if lb.ndim == 0:
        lb = np.resize(lb, n)

    if ub.ndim == 0:
        ub = np.resize(ub, n)

    return lb, ub

# Loss functions.
def huber(z, rho, cost_only):
    mask = z <= 1
    rho[0, mask] = z[mask]
    rho[0, ~mask] = 2 * z[~mask]**0.5 - 1
    if cost_only:
        return
    rho[1, mask] = 1
    rho[1, ~mask] = z[~mask]**-0.5
    rho[2, mask] = 0
    rho[2, ~mask] = -0.5 * z[~mask]**-1.5


def soft_l1(z, rho, cost_only):
    t = 1 + z
    rho[0] = 2 * (t**0.5 - 1)
    if cost_only:
        return
    rho[1] = t**-0.5
    rho[2] = -0.5 * t**-1.5


def cauchy(z, rho, cost_only):
    rho[0] = np.log1p(z)
    if cost_only:
        return
    t = 1 + z
    rho[1] = 1 / t
    rho[2] = -1 / t**2


def arctan(z, rho, cost_only):
    rho[0] = np.arctan(z)
    if cost_only:
        return
    t = 1 + z**2
    rho[1] = 1 / t
    rho[2] = -2 * z / t**2


IMPLEMENTED_LOSSES = dict(linear=None, huber=huber, soft_l1=soft_l1,
                          cauchy=cauchy, arctan=arctan)

def construct_loss_function(m, loss, f_scale):
    if loss == 'linear':
        return None

    if not callable(loss):
        loss = IMPLEMENTED_LOSSES[loss]
        rho = np.empty((3, m))

        def loss_function(f, cost_only=False):
            z = (f / f_scale) ** 2
            loss(z, rho, cost_only=cost_only)
            if cost_only:
                return 0.5 * f_scale ** 2 * np.sum(rho[0])
            rho[0] *= f_scale ** 2
            rho[2] /= f_scale ** 2
            return rho
    else:
        def loss_function(f, cost_only=False):
            z = (f / f_scale) ** 2
            rho = loss(z)
            if cost_only:
                return 0.5 * f_scale ** 2 * np.sum(rho[0])
            rho[0] *= f_scale ** 2
            rho[2] /= f_scale ** 2
            return rho

    return loss_function

class UniFunc(UnivariateFunction):
    def __init__(self, f):
        """
        Initialize

        :param f: Jython function
        """
        self.f = f

    def value(self, *args):
        return self.f(args)

def least_squares(
        fun, x0, jac='2-point', bounds=(-np.inf, np.inf), method='trf',
        ftol=1e-8, xtol=1e-8, gtol=1e-8, x_scale=1.0, loss='linear',
        f_scale=1.0, diff_step=None, tr_solver=None, tr_options={},
        jac_sparsity=None, max_nfev=None, verbose=0, args=(), kwargs={}):
    """Solve a nonlinear least-squares problem with bounds on the variables.
    Given the residuals f(x) (an m-dimensional real function of n real
    variables) and the loss function rho(s) (a scalar function), `least_squares`
    finds a local minimum of the cost function F(x)::

        minimize F(x) = 0.5 * sum(rho(f_i(x)**2), i = 0, ..., m - 1)
        subject to lb <= x <= ub

    The purpose of the loss function rho(s) is to reduce the influence of
    outliers on the solution.

    :param fun: (*callable*) Function which computes the vector of residuals, with the signature
        ``fun(x, *args, **kwargs)``
    """
    if method not in ['trf', 'dogbox', 'lm']:
        raise ValueError("`method` must be 'trf', 'dogbox' or 'lm'.")

    if jac not in ['2-point', '3-point', 'cs'] and not callable(jac):
        raise ValueError("`jac` must be '2-point', '3-point', 'cs' or "
                         "callable.")
    if len(bounds) != 2:
        raise ValueError("`bounds` must contain 2 elements.")

    x0 = np.atleast_1d(x0).astype(float)

    if x0.ndim > 1:
        raise ValueError("`x0` must have at most 1 dimension.")
    #
    # lb, ub = prepare_bounds(bounds, x0.shape[0])
    #
    # if lb.shape != x0.shape or ub.shape != x0.shape:
    #     raise ValueError("Inconsistent shapes between bounds and `x0`.")
    #
    # if lb.shape != x0.shape or ub.shape != x0.shape:
    #     raise ValueError("Inconsistent shapes between bounds and `x0`.")
    #
    # if np.any(lb >= ub):
    #     raise ValueError("Each lower bound must be strictly less than each "
    #                      "upper bound.")
    #
    # if not in_bounds(x0, lb, ub):
    #     raise ValueError("`x0` is infeasible.")

    def fun_wrapped(x):
        return np.atleast_1d(fun(x, *args, **kwargs))

    f0 = fun_wrapped(x0)

    if f0.ndim != 1:
        raise ValueError("`fun` must return at most 1-d array_like. "
                         "f0.shape: {0}".format(f0.shape))

    if not np.all(np.isfinite(f0)):
        raise ValueError("Residuals are not finite in the initial point.")

    n = x0.size
    m = f0.size

    loss_function = construct_loss_function(m, loss, f_scale)
    if callable(loss):
        rho = loss_function(f0)
        if rho.shape != (3, m):
            raise ValueError("The return value of `loss` callable has wrong "
                             "shape.")
        initial_cost = 0.5 * np.sum(rho[0])
    elif loss_function is not None:
        initial_cost = loss_function(f0, cost_only=True)
    else:
        initial_cost = 0.5 * np.dot(f0, f0)

    # Estimate Jacobian by finite differences.
    func = UniFunc(fun)
    x = args[0]
    y = args[1]
    jac_func = OptimizeUtil.getJacobianFunction(func, x.asarray(), 5, 0.1)
    problem = LeastSquaresBuilder().start(x0.tojarray('double')). \
        model(jac_func). \
        target(y.tojarray('double')). \
        lazyEvaluation(False). \
        maxEvaluations(1000). \
        maxIterations(1000). \
        build()
    optimum = LevenbergMarquardtOptimizer().optimize(problem)
    r = np.array(optimum.getPoint().toArray())

    return r