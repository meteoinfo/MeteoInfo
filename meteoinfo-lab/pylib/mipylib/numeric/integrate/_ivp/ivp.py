
from org.meteoinfo.math.integrate import ODEEquations, ODESolver

from mipylib.numeric import core as np
from ...optimize import OptimizeResult
from .common import OdeSolution


_all_ = ['solve_ivp']


METHODS = ['RK23', 'RK45', 'DOP853', 'Radau', 'BDF', 'LSODA']


class OdeResult(OptimizeResult):
    pass

class ODE(ODEEquations):

    def __init__(self, f):
        """
        Initialize

        :param f: Jython function
        """
        self.f = f
        self._args = list(f.__code__.co_varnames)[2:]
        self._args = tuple(self._args)
        self.order = len(self._args)

    def doComputeDerivatives(self, y, t):
        y = np.array(y)
        args = tuple(self.getParameters())
        r = self.f(t, y, *args)
        if isinstance(r, np.NDArray):
            r = tuple(r.aslist())
        return r

def solve_ivp(fun, t_span, y0, method='RK45', t_eval=None, dense_output=False, args=None, **options):
    """Solve an initial value problem for a system of ODEs.

    This function numerically integrates a system of ordinary differential
    equations given an initial value::

        dy / dt = f(t, y)
        y(t0) = y0

    Here t is a 1-D independent variable (time), y(t) is an
    N-D vector-valued function (state), and an N-D
    vector-valued function f(t, y) determines the differential equations.
    The goal is to find y(t) approximately satisfying the differential
    equations, given an initial value y(t0)=y0.

    Some of the solvers support integration in the complex domain, but note
    that for stiff ODE solvers, the right-hand side must be
    complex-differentiable (satisfy Cauchy-Riemann equations [11]_).
    To solve a problem in the complex domain, pass y0 with a complex data type.
    Another option always available is to rewrite your problem for real and
    imaginary parts separately.

    Parameters
    ----------
    fun : callable
        Right-hand side of the system: the time derivative of the state ``y``
        at time ``t``. The calling signature is ``fun(t, y)``, where ``t`` is a
        scalar and ``y`` is a ndarray with ``len(y) = len(y0)``. Additional
        arguments need to be passed if ``args`` is used (see documentation of
        ``args`` argument). ``fun`` must return an array of the same shape as
        ``y``. See `vectorized` for more information.
    t_span : 2-member sequence
        Interval of integration (t0, tf). The solver starts with t=t0 and
        integrates until it reaches t=tf. Both t0 and tf must be floats
        or values interpretable by the float conversion function.
    y0 : array_like, shape (n,)
        Initial state. For problems in the complex domain, pass `y0` with a
        complex data type (even if the initial value is purely real).
    method : string or `OdeSolver`, optional
        Integration method to use:

            * 'RK45' (default): Explicit Runge-Kutta method of order 5(4) [1]_.
              The error is controlled assuming accuracy of the fourth-order
              method, but steps are taken using the fifth-order accurate
              formula (local extrapolation is done). A quartic interpolation
              polynomial is used for the dense output [2]_. Can be applied in
              the complex domain.
            * 'RK23': Explicit Runge-Kutta method of order 3(2) [3]_. The error
              is controlled assuming accuracy of the second-order method, but
              steps are taken using the third-order accurate formula (local
              extrapolation is done). A cubic Hermite polynomial is used for the
              dense output. Can be applied in the complex domain.
            * 'DOP853': Explicit Runge-Kutta method of order 8 [13]_.
              Python implementation of the "DOP853" algorithm originally
              written in Fortran [14]_. A 7-th order interpolation polynomial
              accurate to 7-th order is used for the dense output.
              Can be applied in the complex domain.
            * 'Radau': Implicit Runge-Kutta method of the Radau IIA family of
              order 5 [4]_. The error is controlled with a third-order accurate
              embedded formula. A cubic polynomial which satisfies the
              collocation conditions is used for the dense output.
            * 'BDF': Implicit multi-step variable-order (1 to 5) method based
              on a backward differentiation formula for the derivative
              approximation [5]_. The implementation follows the one described
              in [6]_. A quasi-constant step scheme is used and accuracy is
              enhanced using the NDF modification. Can be applied in the
              complex domain.
            * 'LSODA': Adams/BDF method with automatic stiffness detection and
              switching [7]_, [8]_. This is a wrapper of the Fortran solver
              from ODEPACK.

        Explicit Runge-Kutta methods ('RK23', 'RK45', 'DOP853') should be used
        for non-stiff problems and implicit methods ('Radau', 'BDF') for
        stiff problems [9]_. Among Runge-Kutta methods, 'DOP853' is recommended
        for solving with high precision (low values of `rtol` and `atol`).

        If not sure, first try to run 'RK45'. If it makes unusually many
        iterations, diverges, or fails, your problem is likely to be stiff and
        you should use 'Radau' or 'BDF'. 'LSODA' can also be a good universal
        choice, but it might be somewhat less convenient to work with as it
        wraps old Fortran code.

        You can also pass an arbitrary class derived from `OdeSolver` which
        implements the solver.
    t_eval : array_like or None, optional
        Times at which to store the computed solution, must be sorted and lie
        within `t_span`. If None (default), use points selected by the solver.
    dense_output : bool, optional
        Whether to compute a continuous solution. Default is False.
    args : tuple, optional
        Additional arguments to pass to the user-defined functions.  If given,
        the additional arguments are passed to all user-defined functions.
        So if, for example, `fun` has the signature ``fun(t, y, a, b, c)``,
        then `jac` (if given) and any event functions must have the same
        signature, and `args` must be a tuple of length 3.
    first_step : float or None, optional
        Initial step size. Default is `None` which means that the algorithm
        should choose.
    max_step : float, optional
        Maximum allowed step size. Default is np.inf, i.e., the step size is not
        bounded and determined solely by the solver.
    rtol, atol : float or array_like, optional
        Relative and absolute tolerances. The solver keeps the local error
        estimates less than ``atol + rtol * abs(y)``. Here `rtol` controls a
        relative accuracy (number of correct digits), while `atol` controls
        absolute accuracy (number of correct decimal places). To achieve the
        desired `rtol`, set `atol` to be smaller than the smallest value that
        can be expected from ``rtol * abs(y)`` so that `rtol` dominates the
        allowable error. If `atol` is larger than ``rtol * abs(y)`` the
        number of correct digits is not guaranteed. Conversely, to achieve the
        desired `atol` set `rtol` such that ``rtol * abs(y)`` is always smaller
        than `atol`. If components of y have different scales, it might be
        beneficial to set different `atol` values for different components by
        passing array_like with shape (n,) for `atol`. Default values are
        1e-3 for `rtol` and 1e-6 for `atol`.
    jac : array_like, sparse_matrix, callable or None, optional
        Jacobian matrix of the right-hand side of the system with respect
        to y, required by the 'Radau', 'BDF' and 'LSODA' method. The
        Jacobian matrix has shape (n, n) and its element (i, j) is equal to
        ``d f_i / d y_j``.  There are three ways to define the Jacobian:

            * If array_like or sparse_matrix, the Jacobian is assumed to
              be constant. Not supported by 'LSODA'.
            * If callable, the Jacobian is assumed to depend on both
              t and y; it will be called as ``jac(t, y)``, as necessary.
              Additional arguments have to be passed if ``args`` is
              used (see documentation of ``args`` argument).
              For 'Radau' and 'BDF' methods, the return value might be a
              sparse matrix.
            * If None (default), the Jacobian will be approximated by
              finite differences.

        It is generally recommended to provide the Jacobian rather than
        relying on a finite-difference approximation.
    jac_sparsity : array_like, sparse matrix or None, optional
        Defines a sparsity structure of the Jacobian matrix for a finite-
        difference approximation. Its shape must be (n, n). This argument
        is ignored if `jac` is not `None`. If the Jacobian has only few
        non-zero elements in *each* row, providing the sparsity structure
        will greatly speed up the computations [10]_. A zero entry means that
        a corresponding element in the Jacobian is always zero. If None
        (default), the Jacobian is assumed to be dense.
        Not supported by 'LSODA', see `lband` and `uband` instead.
    lband, uband : int or None, optional
        Parameters defining the bandwidth of the Jacobian for the 'LSODA'
        method, i.e., ``jac[i, j] != 0 only for i - lband <= j <= i + uband``.
        Default is None. Setting these requires your jac routine to return the
        Jacobian in the packed format: the returned array must have ``n``
        columns and ``uband + lband + 1`` rows in which Jacobian diagonals are
        written. Specifically ``jac_packed[uband + i - j , j] = jac[i, j]``.
        The same format is used in `scipy.linalg.solve_banded` (check for an
        illustration).  These parameters can be also used with ``jac=None`` to
        reduce the number of Jacobian elements estimated by finite differences.
    min_step : float, optional
        The minimum allowed step size for 'LSODA' method.
        By default `min_step` is zero.

    Returns
    -------
    Bunch object with the following fields defined:
    t : ndarray, shape (n_points,)
        Time points.
    y : ndarray, shape (n, n_points)
        Values of the solution at `t`.
    sol : `OdeSolution` or None
        Found solution as `OdeSolution` instance; None if `dense_output` was
        set to False.
    t_events : list of ndarray or None
        Contains for each event type a list of arrays at which an event of
        that type event was detected. None if `events` was None.
    y_events : list of ndarray or None
        For each value of `t_events`, the corresponding value of the solution.
        None if `events` was None.
    nfev : int
        Number of evaluations of the right-hand side.
    njev : int
        Number of evaluations of the Jacobian.
    nlu : int
        Number of LU decompositions.
    status : int
        Reason for algorithm termination:

            * -1: Integration step failed.
            *  0: The solver successfully reached the end of `tspan`.
            *  1: A termination event occurred.

    message : string
        Human-readable description of the termination reason.
    success : bool
        True if the solver reached the interval end or a termination event
        occurred (``status >= 0``).

    References
    ----------
    .. [1] J. R. Dormand, P. J. Prince, "A family of embedded Runge-Kutta
           formulae", Journal of Computational and Applied Mathematics, Vol. 6,
           No. 1, pp. 19-26, 1980.
    .. [2] L. W. Shampine, "Some Practical Runge-Kutta Formulas", Mathematics
           of Computation,, Vol. 46, No. 173, pp. 135-150, 1986.
    .. [3] P. Bogacki, L.F. Shampine, "A 3(2) Pair of Runge-Kutta Formulas",
           Appl. Math. Lett. Vol. 2, No. 4. pp. 321-325, 1989.
    .. [4] E. Hairer, G. Wanner, "Solving Ordinary Differential Equations II:
           Stiff and Differential-Algebraic Problems", Sec. IV.8.
    .. [5] `Backward Differentiation Formula
            <https://en.wikipedia.org/wiki/Backward_differentiation_formula>`_
            on Wikipedia.
    .. [6] L. F. Shampine, M. W. Reichelt, "THE MATLAB ODE SUITE", SIAM J. SCI.
           COMPUTE., Vol. 18, No. 1, pp. 1-22, January 1997.
    .. [7] A. C. Hindmarsh, "ODEPACK, A Systematized Collection of ODE
           Solvers," IMACS Transactions on Scientific Computation, Vol 1.,
           pp. 55-64, 1983.
    .. [8] L. Petzold, "Automatic selection of methods for solving stiff and
           nonstiff systems of ordinary differential equations", SIAM Journal
           on Scientific and Statistical Computing, Vol. 4, No. 1, pp. 136-148,
           1983.
    .. [9] `Stiff equation <https://en.wikipedia.org/wiki/Stiff_equation>`_ on
           Wikipedia.
    .. [10] A. Curtis, M. J. D. Powell, and J. Reid, "On the estimation of
            sparse Jacobian matrices", Journal of the Institute of Mathematics
            and its Applications, 13, pp. 117-120, 1974.
    .. [11] `Cauchy-Riemann equations
             <https://en.wikipedia.org/wiki/Cauchy-Riemann_equations>`_ on
             Wikipedia.
    .. [12] `Lotka-Volterra equations
            <https://en.wikipedia.org/wiki/Lotka%E2%80%93Volterra_equations>`_
            on Wikipedia.
    .. [13] E. Hairer, S. P. Norsett G. Wanner, "Solving Ordinary Differential
            Equations I: Nonstiff Problems", Sec. II.
    .. [14] `Page with original Fortran code of DOP853
            <http://www.unige.ch/~hairer/software.html>`_.

    Examples
    --------
    Basic exponential decay showing automatically chosen time points.

    >>> from mipylib.numeric.integrate import solve_ivp
    >>> def exponential_decay(t, y): return -0.5 * y
    >>> sol = solve_ivp(exponential_decay, [0, 10], [2, 4, 8])
    >>> print(sol.t)
    [ 0.          0.11487653  1.26364188  3.06061781  4.81611105  6.57445806
      8.33328988 10.        ]
    >>> print(sol.y)
    [[2.         1.88836035 1.06327177 0.43319312 0.18017253 0.07483045
      0.03107158 0.01350781]
     [4.         3.7767207  2.12654355 0.86638624 0.36034507 0.14966091
      0.06214316 0.02701561]
     [8.         7.5534414  4.25308709 1.73277247 0.72069014 0.29932181
      0.12428631 0.05403123]]

    Specifying points where the solution is desired.

    >>> sol = solve_ivp(exponential_decay, [0, 10], [2, 4, 8],
    ...                 t_eval=[0, 1, 2, 4, 10])
    >>> print(sol.t)
    [ 0  1  2  4 10]
    >>> print(sol.y)
    [[2.         1.21305369 0.73534021 0.27066736 0.01350938]
     [4.         2.42610739 1.47068043 0.54133472 0.02701876]
     [8.         4.85221478 2.94136085 1.08266944 0.05403753]]

    Cannon fired upward with terminal event upon impact. The ``terminal`` and
    ``direction`` fields of an event are applied by monkey patching a function.
    Here ``y[0]`` is position and ``y[1]`` is velocity. The projectile starts
    at position 0 with velocity +10. Note that the integration never reaches
    t=100 because the event is terminal.

    >>> def upward_cannon(t, y): return [y[1], -0.5]
    >>> def hit_ground(t, y): return y[0]
    >>> hit_ground.terminal = True
    >>> hit_ground.direction = -1
    >>> sol = solve_ivp(upward_cannon, [0, 100], [0, 10], events=hit_ground)
    >>> print(sol.t_events)
    [array([40.])]
    >>> print(sol.t)
    [0.00000000e+00 9.99900010e-05 1.09989001e-03 1.10988901e-02
     1.11088891e-01 1.11098890e+00 1.11099890e+01 4.00000000e+01]

    Use `dense_output` and `events` to find position, which is 100, at the apex
    of the cannonball's trajectory. Apex is not defined as terminal, so both
    apex and hit_ground are found. There is no information at t=20, so the sol
    attribute is used to evaluate the solution. The sol attribute is returned
    by setting ``dense_output=True``. Alternatively, the `y_events` attribute
    can be used to access the solution at the time of the event.

    >>> def apex(t, y): return y[1]
    >>> sol = solve_ivp(upward_cannon, [0, 100], [0, 10],
    ...                 events=(hit_ground, apex), dense_output=True)
    >>> print(sol.t_events)
    [array([40.]), array([20.])]
    >>> print(sol.t)
    [0.00000000e+00 9.99900010e-05 1.09989001e-03 1.10988901e-02
     1.11088891e-01 1.11098890e+00 1.11099890e+01 4.00000000e+01]
    >>> print(sol.sol(sol.t_events[1][0]))
    [100.   0.]
    >>> print(sol.y_events)
    [array([[-5.68434189e-14, -1.00000000e+01]]),
     array([[1.00000000e+02, 1.77635684e-15]])]

    As an example of a system with additional parameters, we'll implement
    the Lotka-Volterra equations [12]_.

    >>> def lotkavolterra(t, z, a, b, c, d):
    ...     x, y = z
    ...     return [a*x - b*x*y, -c*y + d*x*y]
    ...

    We pass in the parameter values a=1.5, b=1, c=3 and d=1 with the `args`
    argument.

    >>> sol = solve_ivp(lotkavolterra, [0, 15], [10, 5], args=(1.5, 1, 3, 1),
    ...                 dense_output=True)

    Compute a dense solution and plot it.

    >>> t = np.linspace(0, 15, 300)
    >>> z = sol.sol(t)
    >>> import matplotlib.pyplot as plt
    >>> plt.plot(t, z.T)
    >>> plt.xlabel('t')
    >>> plt.legend(['x', 'y'], shadow=True)
    >>> plt.title('Lotka-Volterra System')
    >>> plt.show()

    A couple examples of using solve_ivp to solve the differential
    equation ``y' = Ay`` with complex matrix ``A``.

    >>> A = np.array([[-0.25 + 0.14j, 0, 0.33 + 0.44j],
    ...               [0.25 + 0.58j, -0.2 + 0.14j, 0],
    ...               [0, 0.2 + 0.4j, -0.1 + 0.97j]])

    Solving an IVP with ``A`` from above and ``y`` as 3x1 vector:

    >>> def deriv_vec(t, y):
    ...     return A @ y
    >>> result = solve_ivp(deriv_vec, [0, 25],
    ...                    np.array([10 + 0j, 20 + 0j, 30 + 0j]),
    ...                    t_eval=np.linspace(0, 25, 101))
    >>> print(result.y[:, 0])
    [10.+0.j 20.+0.j 30.+0.j]
    >>> print(result.y[:, -1])
    [18.46291039+45.25653651j 10.01569306+36.23293216j
     -4.98662741+80.07360388j]

    Solving an IVP with ``A`` from above with ``y`` as 3x3 matrix :

    >>> def deriv_mat(t, y):
    ...     return (A @ y.reshape(3, 3)).flatten()
    >>> y0 = np.array([[2 + 0j, 3 + 0j, 4 + 0j],
    ...                [5 + 0j, 6 + 0j, 7 + 0j],
    ...                [9 + 0j, 34 + 0j, 78 + 0j]])

    >>> result = solve_ivp(deriv_mat, [0, 25], y0.flatten(),
    ...                    t_eval=np.linspace(0, 25, 101))
    >>> print(result.y[:, 0].reshape(3, 3))
    [[ 2.+0.j  3.+0.j  4.+0.j]
     [ 5.+0.j  6.+0.j  7.+0.j]
     [ 9.+0.j 34.+0.j 78.+0.j]]
    >>> print(result.y[:, -1].reshape(3, 3))
    [[  5.67451179 +12.07938445j  17.2888073  +31.03278837j
        37.83405768 +63.25138759j]
     [  3.39949503 +11.82123994j  21.32530996 +44.88668871j
        53.17531184+103.80400411j]
     [ -2.26105874 +22.19277664j -15.1255713  +70.19616341j
       -38.34616845+153.29039931j]]


    """
    if method not in METHODS:
        raise ValueError("`method` must be one of {METHODS}.")

    t0, tf = map(float, t_span)
    first_step = options.pop('first_step', None)
    max_step = options.pop('max_step', None)
    rtol = options.pop('rtol', None)
    atol = options.pop('atol', None)

    fun = ODE(fun)
    if args is not None:
        fun.setParameters(args)

    if isinstance(y0, (tuple, list)):
        y0 = np.array(y0)

    y0 = y0.astype('double')
    ndim = len(y0)
    fun.setDimension(ndim)

    if t_eval is not None: t_eval = np.array(t_eval)._array

    solver = ODESolver(method, fun, t0, tf, y0._array)
    solver.setDenseOutput(dense_output)
    if t_eval is not None: solver.setTEval(t_eval)
    if first_step is not None: solver.setMinStep(first_step)
    if max_step is not None: solver.setMaxStep(max_step)
    if rtol is not None: solver.setRTol(rtol)
    if atol is not None: solver.setATol(atol)
    solver.solve()

    ts = solver.getTResult()
    ys = solver.getYResult()
    if ts is not None: ts = np.array(ts)
    if ys is not None: ys = np.array(ys)

    sol = OdeSolution(solver) if dense_output else None

    return OdeResult(t=ts, y=ys, sol=sol)
