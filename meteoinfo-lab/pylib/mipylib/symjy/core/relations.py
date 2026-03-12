# -*- coding: utf-8 -*-
"""
Relation classes for SymjaPy
"""

from .expr import Expr
from .base import _ensure_expr
from org.matheclipse.core.expression import F


__all__ = ['Eq']


class Eq(Expr):
    """Equality class"""

    def __init__(self, lhs, rhs):
        lhs_expr = _ensure_expr(lhs)
        rhs_expr = _ensure_expr(rhs)
        Expr.__init__(self, F.Equal(lhs_expr._symja_obj, rhs_expr._symja_obj))
