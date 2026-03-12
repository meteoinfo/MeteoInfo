# -*- coding: utf-8 -*-
"""
Core expression class for SymjaPy
"""

from .base import Basic, _ensure_expr, _engine
from org.matheclipse.core.expression import F


__all__ = ['Expr']


class Expr(Basic):
    """
    SymjaPy expression class, wrapping Symja's IExpr
    """
    def __init__(self, symja_expr):
        #super(Expr, self).__init__(symja_expr)
        Basic.__init__(self, symja_expr)

    def __add__(self, other):
        other_expr = _ensure_expr(other)
        return Expr(F.Plus(self._symja_obj, other_expr._symja_obj))

    def __radd__(self, other):
        other_expr = _ensure_expr(other)
        return Expr(F.Plus(other_expr._symja_obj, self._symja_obj))

    def __sub__(self, other):
        other_expr = _ensure_expr(other)
        return Expr(F.Subtract(self._symja_obj, other_expr._symja_obj))

    def __rsub__(self, other):
        other_expr = _ensure_expr(other)
        return Expr(F.Subtract(other_expr._symja_obj, self._symja_obj))

    def __mul__(self, other):
        other_expr = _ensure_expr(other)
        return Expr(F.Times(self._symja_obj, other_expr._symja_obj))

    def __rmul__(self, other):
        other_expr = _ensure_expr(other)
        return Expr(F.Times(other_expr._symja_obj, self._symja_obj))

    def __pow__(self, other):
        other_expr = _ensure_expr(other)
        return Expr(F.Power(self._symja_obj, other_expr._symja_obj))

    def __rpow__(self, other):
        other_expr = _ensure_expr(other)
        return Expr(F.Power(other_expr._symja_obj, self._symja_obj))

    def __div__(self, other):
        other_expr = _ensure_expr(other)
        return Expr(F.Divide(self._symja_obj, other_expr._symja_obj))

    def __rdiv__(self, other):
        other_expr = _ensure_expr(other)
        return Expr(F.Divide(other_expr._symja_obj, self._symja_obj))

    def __truediv__(self, other):
        return self.__div__(other)

    def __rtruediv__(self, other):
        return self.__rdiv__(other)

    def __neg__(self):
        return Expr(F.Times(F.integer(-1), self._symja_obj))

    def __eq__(self, other):
        # For symbolic equality, Symja uses SameQ (===) or Equal (==)
        # It's more appropriate to return a Symja equality expression
        other_expr = _ensure_expr(other)
        from .relations import Eq
        return Eq(self, other)

    def subs(self, old, new=None):
        """
        Substitute symbols in expression
        - subs({old: new, ...})
        - subs(old, new)
        """
        if isinstance(old, dict):
            rules_list = []
            for k, v in old.items():
                k_expr = _ensure_expr(k)
                v_expr = _ensure_expr(v)
                rules_list.append(F.rule(k_expr._symja_obj, v_expr._symja_obj))
            rules = F.list(*rules_list)
        else:
            old_expr = _ensure_expr(old)
            new_expr = _ensure_expr(new)
            rules = F.Rule(old_expr._symja_obj, new_expr._symja_obj)

        result = _engine.evaluate(F.ReplaceAll(self._symja_obj, rules))
        return Expr(result)

    def simplify(self):
        """Simplify expression"""
        result = _engine.evaluate(F.Simplify(self._symja_obj))
        return Expr(result)

    def expand(self):
        """Expand expression"""
        result = _engine.evaluate(F.Expand(self._symja_obj))
        return Expr(result)

    def factor(self):
        """Factor expression"""
        result = _engine.evaluate(F.Factor(self._symja_obj))
        return Expr(result)

    def evalf(self, precision=15):
        """Numerical evaluation"""
        result = _engine.evaluate(F.N(self._symja_obj, precision))
        return Expr(result)

    def diff(self, var):
        """Differentiation"""
        var_expr = _ensure_expr(var)
        result = _engine.evaluate(F.D(self._symja_obj, var_expr._symja_obj))
        return Expr(result)

    def integrate(self, var):
        """Integration"""
        var_expr = _ensure_expr(var)
        result = _engine.evaluate(F.Integrate(self._symja_obj, var_expr._symja_obj))
        return Expr(result)
