# -*- coding: utf-8 -*-
"""
Number classes for SymjaPy
"""

from .expr import Expr
from org.matheclipse.core.expression import F


__all__ = ['Integer', 'Float', 'Rational', 'Add', 'Mul', 'Pow']


class Number(Expr):
    """Base number class"""
    pass

class Integer(Number):
    """Integer class"""
    def __init__(self, val):
        Number.__init__(self, F.integer(int(val)))


class Float(Number):
    """Float class"""
    def __init__(self, val):
        Number.__init__(self, F.num(float(val)))


class Rational(Number):
    """Rational class"""
    def __init__(self, num, den):
        Number.__init__(self, F.fraction(int(num), int(den)))


class Add(Expr):
    """Addition class"""
    def __new__(cls, *args):
        if len(args) == 0:
            return Integer(0)
        elif len(args) == 1:
            return _ensure_expr(args[0])
        else:
            result = args[0]
            for arg in args[1:]:
                result = result + _ensure_expr(arg)
            return result

class Mul(Expr):
    """Multiplication class"""
    def __new__(cls, *args):
        if len(args) == 0:
            return Integer(1)
        elif len(args) == 1:
            return _ensure_expr(args[0])
        else:
            result = args[0]
            for arg in args[1:]:
                result = result * _ensure_expr(arg)
            return result

class Pow(Expr):
    """Power class"""
    def __new__(cls, base, exp):
        base_expr = _ensure_expr(base)
        exp_expr = _ensure_expr(exp)
        return base_expr ** exp_expr

def _ensure_expr(obj):
    """Import from base module to avoid circular dependency"""
    from .base import _ensure_expr as base_ensure_expr
    return base_ensure_expr(obj)
