# -*- coding: utf-8 -*-
"""
Base class definitions for SymJy
"""

from org.matheclipse.core.expression import F
from org.matheclipse.core.eval import EvalEngine
from org.matheclipse.core.interfaces import IExpr
from java.lang.reflect import Array


__all__ = ['Basic']


# Global engine instance
_engine = EvalEngine(False)


class Basic(object):
    """
    Symjy basic class, corresponding to SymPy's Basic
    """
    def __init__(self, symja_expr):
        if not isinstance(symja_expr, IExpr):
            raise TypeError("Argument must be an IExpr instance")
        self._symja_obj = symja_expr

    def __repr__(self):
        return self._symja_obj.toString()

    def __str__(self):
        return self._symja_obj.toString()

    def free_symbols(self):
        """Get free symbols"""
        # Use Symja function to find free symbols
        free_syms_str = _engine.evaluate("Variables(" + self._symja_obj.toString() + ")")
        # Simplified processing: return string representation
        return free_syms_str.toString()

    def has(self, pattern):
        """Check if contains a pattern"""
        pattern_expr = _ensure_expr(pattern)
        # Use Symja's FreeQ or MatchQ
        result = _engine.evaluate("FreeQ(" + self._symja_obj.toString() + ", " + pattern_expr._symja_obj.toString() + ")")
        return result.toString() == "False"

def _ensure_expr(obj):
    """Ensure object is an Expr instance"""
    if isinstance(obj, Expr):
        return obj
    elif isinstance(obj, (int, float)):
        if isinstance(obj, int):
            return Integer(obj)
        else:
            return Float(obj)
    elif isinstance(obj, str):
        # Try parsing string
        try:
            parsed = F.eval(obj)
            return Expr(parsed)
        except:
            # If parsing fails, treat as symbol
            return Symbol(obj)
    else:
        raise TypeError("Cannot convert " + str(type(obj)) + " to Expr")


def _to_java_array(plist):
    """Convert jython list to java array"""
    size = len(plist)
    jarray = Array.newInstance(type(plist[0]), size)

    for i, val in enumerate(plist):
        Array.set(jarray, i, val)

    return jarray


# Import here to avoid circular dependency
from .expr import Expr
from .numbers import Integer, Float
from .symbol import Symbol