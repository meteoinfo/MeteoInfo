# -*- coding: utf-8 -*-
"""
Solver functions for SymJy
"""

from .expr import Expr
from .base import _ensure_expr, _engine, _to_java_array
from .relations import Eq
from .matrices import Matrix
from org.matheclipse.core.expression import F


__all__ = ['solve','linsolve']


def solve(eq, var):
    """Solve equations"""
    if isinstance(eq, (list, tuple)):
        eqs = []
        for eq_ in eq:
            eqs.append(_ensure_expr(eq_)._symja_obj)
        eqs = F.List(_to_java_array(eqs))
        vars = []
        for var_ in var:
            vars.append(_ensure_expr(var_)._symja_obj)
        vars = F.List(_to_java_array(vars))
        result = _engine.evaluate(F.Solve(eqs, vars))
    else:
        if not isinstance(eq, Eq):
            eq_expr = Eq(eq, 0)
        else:
            eq_expr = _ensure_expr(eq)
        var_expr = _ensure_expr(var)
        result = _engine.evaluate(F.Solve(eq_expr._symja_obj, var_expr._symja_obj))

    # Return result
    return Expr(result)


def linsolve(system, *symbols):
    """
    Solve system of $N$ linear equations with $M$ variables; both
    underdetermined and overdetermined systems are supported.
    The possible number of solutions is zero, one or infinite.
    Zero solutions throws a ValueError, whereas infinite
    solutions are represented parametrically in terms of the given
    symbols. For unique solution a :class:`~.FiniteSet` of ordered tuples
    is returned.

    All standard input formats are supported:
    For the given set of equations, the respective input types
    are given below:

    .. math:: 3x + 2y -   z = 1
    .. math:: 2x - 2y + 4z = -2
    .. math:: 2x -   y + 2z = 0

    * Augmented matrix form, ``system`` given below:

    $$ \text{system} = \left[{array}{cccc}
    3 &  2 & -1 &  1\ \
            2 & -2 &  4 & -2\ \
            2 & -1 &  2 &  0
    \end{array}\right] $$

    ::

    system = Matrix([[3, 2, -1, 1], [2, -2, 4, -2], [2, -1, 2, 0]])

    * List of equations form

    ::

    system  =  [3x + 2y - z - 1, 2x - 2y + 4z + 2, 2x - y + 2z]

    * Input $A$ and $b$ in matrix form (from $Ax = b$) are given as:

    $$ A = \left[\begin{array}{ccc}
    3 &  2 & -1 \ \
        2 & -2 &  4 \ \
        2 & -1 &  2
    \end{array}\right] \ \  b = \left[\begin{array}{c}
    1 \\ -2 \\ 0
    \end{array}\right] $$

    ::

    A = Matrix([[3, 2, -1], [2, -2, 4], [2, -1, 2]])
    b = Matrix([[1], [-2], [0]])
    system = (A, b)

    Symbols can always be passed but are actually only needed
    when 1) a system of equations is being passed and 2) the
    system is passed as an underdetermined matrix and one wants
    to control the name of the free variables in the result.
    An error is raised if no symbols are used for case 1, but if
    no symbols are provided for case 2, internally generated symbols
    will be provided. When providing symbols for case 2, there should
    be at least as many symbols are there are columns in matrix A.

    The algorithm used here is Gauss-Jordan elimination, which
    results, after elimination, in a row echelon form matrix.

    Returns
    =======

    A FiniteSet containing an ordered tuple of values for the
    unknowns for which the `system` has a solution. (Wrapping
    the tuple in FiniteSet is used to maintain a consistent
    output format throughout solveset.)

    Returns EmptySet, if the linear system is inconsistent.
    """
    # If second argument is an iterable
    if symbols and hasattr(symbols[0], '__iter__'):
        symbols = symbols[0]

    if hasattr(system, '__iter__'):

        # 1). (A, b)
        if len(system) == 2 and isinstance(system[0], Matrix):
            A, b = system

        result = _engine.evaluate(F.LinearSolve(A._symja_obj, b._symja_obj))

        return Expr(result)
