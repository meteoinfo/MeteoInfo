# -*- coding: utf-8 -*-
"""
Mathematical functions for symjy
"""

from .expr import Expr
from .numbers import Integer, Rational
from .base import _ensure_expr, _engine
from org.matheclipse.core.expression import F


__all__ = ['sin','cos','tan','exp','log','sqrt','diff','E','expand','factor','gamma',
           'I','limit','pi','series','integrate','simplify','apart','collect']


class Function(Expr):
    """Function base class"""
    def __new__(cls, *args):
        # This requires special handling since Symja functions are in F
        # e.g., sin, cos, etc.
        raise NotImplementedError("Use specific function classes like sin, cos, etc.")


def sin(x):
    x_expr = _ensure_expr(x)
    return Expr(F.Sin(x_expr._symja_obj))


def cos(x):
    x_expr = _ensure_expr(x)
    return Expr(F.Cos(x_expr._symja_obj))


def tan(x):
    x_expr = _ensure_expr(x)
    return Expr(F.Tan(x_expr._symja_obj))


def exp(x):
    x_expr = _ensure_expr(x)
    return Expr(F.Exp(x_expr._symja_obj))


def log(x):
    x_expr = _ensure_expr(x)
    return Expr(F.Log(x_expr._symja_obj))


def sqrt(x):
    x_expr = _ensure_expr(x)
    return x ** Rational(1, 2)


def pi():
    return Expr(F.symbol("Pi"))


def E():
    return Expr(F.symbol("E"))


def I():
    return Expr(F.symbol("I"))


def gamma(x):
    x_expr = _ensure_expr(x)
    result = _engine.evaluate(F.Gamma(x_expr._symja_obj))
    return Expr(result)


def factor(x):
    x_expr = _ensure_expr(x)
    result = _engine.evaluate(F.Factor(x_expr._symja_obj))
    return Expr(result)


def expand(expr):
    _expr = _ensure_expr(expr)
    result = _engine.evaluate(F.Expand(_expr._symja_obj))
    return Expr(result)


def limit(expr, var, point):
    """Calculate limits"""
    expr_expr = _ensure_expr(expr)
    var_expr = _ensure_expr(var)
    point_expr = _ensure_expr(point)
    rule = F.Rule(var_expr._symja_obj, point_expr._symja_obj)
    result = _engine.evaluate(F.Limit(expr_expr._symja_obj, rule))
    return Expr(result)


def series(expr, var, point, order):
    """Taylor series expansion"""
    expr_expr = _ensure_expr(expr)
    var_expr = _ensure_expr(var)
    point_expr = _ensure_expr(point)
    order_expr = _ensure_expr(order)
    plist = F.List(var_expr._symja_obj, point_expr._symja_obj, order_expr._symja_obj)
    result = _engine.evaluate(F.Series(expr_expr._symja_obj, plist))
    return Expr(result)


def diff(expr, var, order=1):
    """Differentiation"""
    expr_expr = _ensure_expr(expr)
    var_expr = _ensure_expr(var)
    result = _engine.evaluate(F.D(expr_expr._symja_obj, var_expr._symja_obj))
    while order > 1:
        result = _engine.evaluate(F.D(result, var_expr._symja_obj))
        order -= 1

    return Expr(result)


def integrate(expr, var):
    """Integrate"""
    expr_expr = _ensure_expr(expr)
    if isinstance(var, (list, tuple)):
        v, l, u = var
        v_expr = _ensure_expr(v)
        l_expr = _ensure_expr(l)
        u_expr = _ensure_expr(u)
        limits = F.List(v_expr._symja_obj, l_expr._symja_obj, u_expr._symja_obj)
        result = _engine.evaluate(F.Integrate(expr_expr._symja_obj, limits))
    else:
        var_expr = _ensure_expr(var)
        result = _engine.evaluate(F.Integrate(expr_expr._symja_obj, var_expr._symja_obj))

    return Expr(result)


def simplify(expr):
    """Simplifies the given expression"""
    expr_expr = _ensure_expr(expr)
    result = _engine.evaluate(F.Simplify(expr_expr._symja_obj))
    return Expr(result)


def apart(f, x=None):
    """
    Compute partial fraction decomposition of a rational function

    Given a rational function ``f``, computes the partial fraction
    decomposition of ``f``. Two algorithms are available: One is based on the
    undetermined coefficients method, the other is Bronstein's full partial
    fraction decomposition algorithm.

    The undetermined coefficients method (selected by ``full=False``) uses
    polynomial factorization (and therefore accepts the same options as
    factor) for the denominator. Per default it works over the rational
    numbers, therefore decomposition of denominators with non-rational roots
    (e.g. irrational, complex roots) is not supported by default (see options
    of factor).

    Bronstein's algorithm can be selected by using ``full=True`` and allows a
    decomposition of denominators with non-rational roots. A human-readable
    result can be obtained via ``doit()`` (see examples below).
    """
    f_expr = _ensure_expr(f)
    if x is None:
        result = _engine.evaluate(F.Apart(f_expr._symja_obj))
    else:
        x_expr = _ensure_expr(x)
        result = _engine.evaluate(F.Apart(f_expr._symja_obj, x_expr._symja_obj))

    return Expr(result)


def collect(expr, syms):
    """
    Collect additive terms of an expression.

    Explanation
    ===========

    This function collects additive terms of an expression with respect
    to a list of expression up to powers with rational exponents. By the
    term symbol here are meant arbitrary expressions, which can contain
    powers, products, sums etc. In other words symbol is a pattern which
    will be searched for in the expression's terms.

    The input expression is not expanded by :func:`collect`, so user is
    expected to provide an expression in an appropriate form. This makes
    :func:`collect` more predictable as there is no magic happening behind the
    scenes. However, it is important to note, that powers of products are
    converted to products of powers using the :func:`~.expand_power_base`
    function.

    There are two possible types of output. First, if ``evaluate`` flag is
    set, this function will return an expression with collected terms or
    else it will return a dictionary with expressions up to rational powers
    as keys and collected coefficients as values.
    """
    expr_expr = _ensure_expr(expr)
    syms_expr = _ensure_expr(syms)
    result = _engine.evaluate(F.Collect(expr_expr._symja_obj, syms_expr._symja_obj))
    return Expr(result)
