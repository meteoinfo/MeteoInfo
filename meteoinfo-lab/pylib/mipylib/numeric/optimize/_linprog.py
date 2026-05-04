from org.meteoinfo.math.optimize import LinearProgram

from ..core import numeric as np
from ._optimize import OptimizeResult
from numbers import Number


__all__ = ['linprog']


def linprog(c, A_ub=None, b_ub=None, A_eq=None, b_eq=None,
            bounds=(0, None), method='simplex'):
    r"""
    Linear programming: minimize a linear objective function subject to linear
    equality and inequality constraints.

    Linear programming solves problems of the following form:

    .. math::

        \min_x \ & c^T x \\
        \mbox{such that} \ & A_{ub} x \leq b_{ub},\\
        & A_{eq} x = b_{eq},\\
        & l \leq x \leq u ,

    where :math:`x` is a vector of decision variables; :math:`c`,
    :math:`b_{ub}`, :math:`b_{eq}`, :math:`l`, and :math:`u` are vectors; and
    :math:`A_{ub}` and :math:`A_{eq}` are matrices.

    Alternatively, that's:

    - minimize ::

        c @ x

    - such that ::

        A_ub @ x <= b_ub
        A_eq @ x == b_eq
        lb <= x <= ub

    Note that by default ``lb = 0`` and ``ub = None``. Other bounds can be
    specified with ``bounds``.

    Parameters
    ----------
    c : 1-D array
        The coefficients of the linear objective function to be minimized.
    A_ub : 2-D array, optional
        The inequality constraint matrix. Each row of ``A_ub`` specifies the
        coefficients of a linear inequality constraint on ``x``.
    b_ub : 1-D array, optional
        The inequality constraint vector. Each element represents an
        upper bound on the corresponding value of ``A_ub @ x``.
    A_eq : 2-D array, optional
        The equality constraint matrix. Each row of ``A_eq`` specifies the
        coefficients of a linear equality constraint on ``x``.
    b_eq : 1-D array, optional
        The equality constraint vector. Each element of ``A_eq @ x`` must equal
        the corresponding element of ``b_eq``.
    bounds : sequence, optional
        A sequence of ``(min, max)`` pairs for each element in ``x``, defining
        the minimum and maximum values of that decision variable.
        If a single tuple ``(min, max)`` is provided, then ``min`` and ``max``
        will serve as bounds for all decision variables.
        Use ``None`` to indicate that there is no bound. For instance, the
        default bound ``(0, None)`` means that all decision variables are
        non-negative, and the pair ``(None, None)`` means no bounds at all,
        i.e. all variables are allowed to be any real.
    method : str, optional
        The algorithm used to solve the standard form problem.
        The following are supported.

        - :ref:`'highs' <optimize.linprog-highs>` (default)
        - :ref:`'highs-ds' <optimize.linprog-highs-ds>`
        - :ref:`'highs-ipm' <optimize.linprog-highs-ipm>`
        - :ref:`'interior-point' <optimize.linprog-interior-point>` (legacy)
        - :ref:`'revised simplex' <optimize.linprog-revised_simplex>` (legacy)
        - :ref:`'simplex' <optimize.linprog-simplex>` (legacy)

        The legacy methods are deprecated and will be removed in SciPy 1.11.0.

    Returns
    -------
    res : OptimizeResult
        A :class:`scipy.optimize.OptimizeResult` consisting of the fields
        below. Note that the return types of the fields may depend on whether
        the optimization was successful, therefore it is recommended to check
        `OptimizeResult.status` before relying on the other fields:

        x : 1-D array
            The values of the decision variables that minimizes the
            objective function while satisfying the constraints.
        fun : float
            The optimal value of the objective function ``c @ x``.
        slack : 1-D array
            The (nominally positive) values of the slack variables,
            ``b_ub - A_ub @ x``.
        con : 1-D array
            The (nominally zero) residuals of the equality constraints,
            ``b_eq - A_eq @ x``.
        success : bool
            ``True`` when the algorithm succeeds in finding an optimal
            solution.
        status : int
            An integer representing the exit status of the algorithm.

            ``0`` : Optimization terminated successfully.

            ``1`` : Iteration limit reached.

            ``2`` : Problem appears to be infeasible.

            ``3`` : Problem appears to be unbounded.

            ``4`` : Numerical difficulties encountered.

        nit : int
            The total number of iterations performed in all phases.
        message : str
            A string descriptor of the exit status of the algorithm.

    References
    ----------
    .. [1] Dantzig, George B., Linear programming and extensions. Rand
           Corporation Research Study Princeton Univ. Press, Princeton, NJ,
           1963
    .. [2] Hillier, S.H. and Lieberman, G.J. (1995), "Introduction to
           Mathematical Programming", McGraw-Hill, Chapter 4.
    .. [3] Bland, Robert G. New finite pivoting rules for the simplex method.
           Mathematics of Operations Research (2), 1977: pp. 103-107.
    .. [4] Andersen, Erling D., and Knud D. Andersen. "The MOSEK interior point
           optimizer for linear programming: an implementation of the
           homogeneous algorithm." High performance optimization. Springer US,
           2000. 197-232.
    .. [5] Andersen, Erling D. "Finding all linearly dependent rows in
           large-scale linear programming." Optimization Methods and Software
           6.3 (1995): 219-227.
    .. [6] Freund, Robert M. "Primal-Dual Interior-Point Methods for Linear
           Programming based on Newton's Method." Unpublished Course Notes,
           March 2004. Available 2/25/2017 at
           https://ocw.mit.edu/courses/sloan-school-of-management/15-084j-nonlinear-programming-spring-2004/lecture-notes/lec14_int_pt_mthd.pdf
    .. [7] Fourer, Robert. "Solving Linear Programs by Interior-Point Methods."
           Unpublished Course Notes, August 26, 2005. Available 2/25/2017 at
           http://www.4er.org/CourseNotes/Book%20B/B-III.pdf
    .. [8] Andersen, Erling D., and Knud D. Andersen. "Presolving in linear
           programming." Mathematical Programming 71.2 (1995): 221-245.
    .. [9] Bertsimas, Dimitris, and J. Tsitsiklis. "Introduction to linear
           programming." Athena Scientific 1 (1997): 997.
    .. [10] Andersen, Erling D., et al. Implementation of interior point
            methods for large scale linear programming. HEC/Universite de
            Geneve, 1996.
    .. [11] Bartels, Richard H. "A stabilization of the simplex method."
            Journal in  Numerische Mathematik 16.5 (1971): 414-434.
    .. [12] Tomlin, J. A. "On scaling linear programming problems."
            Mathematical Programming Study 4 (1975): 146-166.
    .. [13] Huangfu, Q., Galabova, I., Feldmeier, M., and Hall, J. A. J.
            "HiGHS - high performance software for linear optimization."
            https://highs.dev/
    .. [14] Huangfu, Q. and Hall, J. A. J. "Parallelizing the dual revised
            simplex method." Mathematical Programming Computation, 10 (1),
            119-142, 2018. DOI: 10.1007/s12532-017-0130-5

    Examples
    --------
    Consider the following problem:

    .. math::

        \min_{x_0, x_1} \ -x_0 + 4x_1 & \\
        \mbox{such that} \ -3x_0 + x_1 & \leq 6,\\
        -x_0 - 2x_1 & \geq -4,\\
        x_1 & \geq -3.

    The problem is not presented in the form accepted by `linprog`. This is
    easily remedied by converting the "greater than" inequality
    constraint to a "less than" inequality constraint by
    multiplying both sides by a factor of :math:`-1`. Note also that the last
    constraint is really the simple bound :math:`-3 \leq x_1 \leq \infty`.
    Finally, since there are no bounds on :math:`x_0`, we must explicitly
    specify the bounds :math:`-\infty \leq x_0 \leq \infty`, as the
    default is for variables to be non-negative. After collecting coeffecients
    into arrays and tuples, the input for this problem is:

    >>> from mipylib.numeric.optimize import linprog
    >>> c = [-1, 4]
    >>> A = [[-3, 1], [1, 2]]
    >>> b = [6, 4]
    >>> x0_bounds = (None, None)
    >>> x1_bounds = (-3, None)
    >>> res = linprog(c, A_ub=A, b_ub=b, bounds=[x0_bounds, x1_bounds])
    >>> res.fun
    -22.0
    >>> res.x
    array([10., -3.])
    >>> res.message
    'Optimization terminated successfully. (HiGHS Status 7: Optimal)'

    The marginals (AKA dual values / shadow prices / Lagrange multipliers)
    and residuals (slacks) are also available.

    >>> res.ineqlin
      residual: [ 3.900e+01  0.000e+00]
     marginals: [-0.000e+00 -1.000e+00]

    For example, because the marginal associated with the second inequality
    constraint is -1, we expect the optimal value of the objective function
    to decrease by ``eps`` if we add a small amount ``eps`` to the right hand
    side of the second inequality constraint:

    >>> eps = 0.05
    >>> b[1] += eps
    >>> linprog(c, A_ub=A, b_ub=b, bounds=[x0_bounds, x1_bounds]).fun
    -22.05

    Also, because the residual on the first inequality constraint is 39, we
    can decrease the right hand side of the first constraint by 39 without
    affecting the optimal solution.

    >>> b = [6, 4]  # reset to original values
    >>> b[0] -= 39
    >>> linprog(c, A_ub=A, b_ub=b, bounds=[x0_bounds, x1_bounds]).fun
    -22.0

    """
    c = np.asarray(c)
    A_ub = None if A_ub is None else np.asarray(A_ub)._array
    b_ub = None if b_ub is None else np.asarray(b_ub)._array
    A_eq = None if A_eq is None else np.asarray(A_eq)._array
    b_eq = None if b_eq is None else np.asarray(b_eq)._array
    if isinstance(bounds[0], Number):
        bounds = [bounds]

    _bounds = []
    for b in bounds:
        _b = []
        if b[0] is None:
            _b.append(-1.797e+308)
        else:
            _b.append(float(b[0]))
        if b[1] is None:
            _b.append(float('inf'))
        else:
            _b.append(float(b[1]))
        _bounds.append(_b)

    lp = LinearProgram(c._array, A_ub, b_ub, A_eq, b_eq, _bounds, method)
    r = lp.solve()

    return OptimizeResult(x=r.x, fun=r.fun, message=r.message, success=r.success)
