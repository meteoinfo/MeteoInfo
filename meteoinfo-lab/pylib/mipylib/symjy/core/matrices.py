# -*- coding: utf-8 -*-
"""
Matrix class for SymjaPy
"""

from .expr import Expr
from .base import _ensure_expr
from org.matheclipse.core.expression import F


__all__ = ['Matrix']


def convert_to_symja_list(py_list):
    """Converts a python list to a SymjaPy list"""
    if not isinstance(py_list, list):
        return _ensure_expr(py_list)._symja_obj
    items = [convert_to_symja_list(item) for item in py_list]
    return F.list(*items)


class Matrix(Expr):
    """Matrix class"""

    def __init__(self, data):
        matrix = convert_to_symja_list(data)
        Expr.__init__(self, matrix)
