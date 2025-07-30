
from java.lang import Double
from org.meteoinfo.math.integrate import ODESolver

from warnings import warn
from mipylib.numeric import core as np


ESP = Double.MIN_VALUE


def validate_first_step(first_step, t0, t_bound):
    """Assert that first_step is valid and return it."""
    if first_step <= 0:
        raise ValueError("`first_step` must be positive.")
    if first_step > np.abs(t_bound - t0):
        raise ValueError("`first_step` exceeds bounds.")
    return first_step


def validate_max_step(max_step):
    """Assert that max_Step is valid and return it."""
    if max_step <= 0:
        raise ValueError("`max_step` must be positive.")
    return max_step


def warn_extraneous(extraneous):
    """Display a warning for extraneous keyword arguments.

    The initializer of each solver class is expected to collect keyword
    arguments that it doesn't understand and warn about them. This function
    prints a warning for each key in the supplied dictionary.

    Parameters
    ----------
    extraneous : dict
        Extraneous keyword arguments
    """
    if extraneous:
        warn("The following arguments have no effect for a chosen solver: " \
             "{', '.join(f'`{x}`' for x in extraneous)}.",
             stacklevel=3)


def validate_tol(rtol, atol, n):
    """Validate tolerance values."""

    if np.any(rtol < 100 * EPS):
        warn("At least one element of `rtol` is too small. " \
             "Setting `rtol = np.maximum(rtol, {})`.".format(100 * EPS),
             stacklevel=3)
        rtol = np.maximum(rtol, 100 * EPS)

    atol = np.asarray(atol)
    if atol.ndim > 0 and atol.shape != (n,):
        raise ValueError("`atol` has wrong shape.")

    if np.any(atol < 0):
        raise ValueError("`atol` must be positive.")

    return rtol, atol


def norm(x):
    """Compute RMS norm."""
    return np.linalg.norm(x) / x.size ** 0.5


def select_initial_step(fun, t0, y0, t_bound,
                        max_step, f0, direction, order, rtol, atol):
    """Empirically select a good initial step.

    The algorithm is described in [1]_.

    Parameters
    ----------
    fun : callable
        Right-hand side of the system.
    t0 : float
        Initial value of the independent variable.
    y0 : ndarray, shape (n,)
        Initial value of the dependent variable.
    t_bound : float
        End-point of integration interval; used to ensure that t0+step<=tbound
        and that fun is only evaluated in the interval [t0,tbound]
    max_step : float
        Maximum allowable step size.
    f0 : ndarray, shape (n,)
        Initial value of the derivative, i.e., ``fun(t0, y0)``.
    direction : float
        Integration direction.
    order : float
        Error estimator order. It means that the error controlled by the
        algorithm is proportional to ``step_size ** (order + 1)`.
    rtol : float
        Desired relative tolerance.
    atol : float
        Desired absolute tolerance.

    Returns
    -------
    h_abs : float
        Absolute value of the suggested initial step.

    References
    ----------
    .. [1] E. Hairer, S. P. Norsett G. Wanner, "Solving Ordinary Differential
           Equations I: Nonstiff Problems", Sec. II.4.
    """
    if y0.size == 0:
        return np.inf

    interval_length = abs(t_bound - t0)
    if interval_length == 0.0:
        return 0.0

    scale = atol + np.abs(y0) * rtol
    d0 = norm(y0 / scale)
    d1 = norm(f0 / scale)
    if d0 < 1e-5 or d1 < 1e-5:
        h0 = 1e-6
    else:
        h0 = 0.01 * d0 / d1
    # Check t0+h0*direction doesn't take us beyond t_bound
    h0 = min(h0, interval_length)
    y1 = y0 + h0 * direction * f0
    f1 = fun(t0 + h0 * direction, y1)
    d2 = norm((f1 - f0) / scale) / h0

    if d1 <= 1e-15 and d2 <= 1e-15:
        h1 = max(1e-6, h0 * 1e-3)
    else:
        h1 = (0.01 / max(d1, d2)) ** (1 / (order + 1))

    return min(100 * h0, h1, interval_length, max_step)


class OdeSolution():
    """Continuous ODE solution.

    It is organized as a collection of `DenseOutput` objects which represent
    local interpolants. It provides an algorithm to select a right interpolant
    for each given point.
    """
    def __init__(self, solver):
        self.solver = solver

    def __call__(self, t):
        """Evaluate the solution.

        Parameters
        ----------
        t : float or array_like with shape (n_points,)
            Points to evaluate at.

        Returns
        -------
        y : ndarray, shape (n_states,) or (n_states, n_points)
            Computed values. Shape depends on whether `t` is a scalar or a
            1-D array.
        """
        t = np.asarray(t)
        ys = self.solver.solve(t._array)

        return np.array(ys)
