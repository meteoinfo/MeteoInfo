# -*- coding: utf-8 -*-
"""
SymJy: A SymPy-like Library for Jython Based on Symja
Main initialization file
"""

from .base import Basic
from .expr import Expr
from .numbers import *
from .symbol import *
from .functions import *
from .relations import Eq
from .matrices import Matrix
from .solvers import *
from .sympify import sympify


__all__ = ['Basic', 'Expr', 'Eq', 'Matrix', 'sympify']
__all__ += numbers.__all__
__all__ += symbol.__all__
__all__ += functions.__all__
__all__ += solvers.__all__


# --- Example Usage ---
if __name__ == "__main__":
    print("=== SymJy: A SymPy-like Library for Jython Based on Symja (Complete Version) ===\n")

    # 1. Create symbols
    x, y, z = symbols('x y z')
    print("Symbol x:", x)
    print("Symbol y:", y)
    print("Symbol z:", z)
    print()

    # 2. Basic number types
    i = Integer(5)
    f = Float(3.14)
    r = Rational(1, 3)
    print("Integer:", i)
    print("Float:", f)
    print("Rational:", r)
    print()

    # 3. Build expressions
    expr = x**2 + 2*x*y + y**2
    print("Expression:", expr)
    print()

    # 4. Simplification and expansion
    simplified = expr.simplify()
    print("Simplified:", simplified)

    expanded = (x + y)**2
    print("Before expansion: (x + y)^2")
    print("After expansion:", expanded.expand())
    print()

    # 5. Differentiation
    derivative = expr.diff(x)
    print("Derivative w.r.t. x:", derivative)
    print()

    # 6. Integration
    integral = expr.integrate(x)
    print("Integral w.r.t. x:", integral)
    print()

    # 7. Numerical evaluation
    num_result = expr.subs({x: 2, y: 3}).evalf()
    print("Substitute x=2, y=3 and evaluate numerically:", num_result)
    print()

    # 8. Trigonometric functions
    trig_expr = sin(x)**2 + cos(x)**2
    print("Trigonometric identity:", trig_expr)
    print("Simplified:", trig_expr.simplify())
    print()

    # 9. Solve equations
    equation = x**2 - 4
    print("Solve equation", equation, "= 0")
    solution = solve(equation, x)
    print("Solution:", solution)
    print()

    # 10. Special constants
    print("Pi:", pi())
    print("E:", E())
    print("I:", I())
    print()

    # 11. Matrix
    matrix_data = [[1, 2], [3, 4]]
    mat = Matrix(matrix_data)
    print("Matrix:", mat)
    print()

    # 12. Limits
    lim_expr = sin(x)/x
    limit_result = limit(lim_expr, x, 0)
    print("lim(sin(x)/x, x->0):", limit_result)
    print()

    # 13. Taylor expansion
    taylor_result = series(sin(x), x, 0, 5)
    print("Taylor expansion of sin(x) (x=0, 5 terms):", taylor_result)
    print()

    # 14. Free symbols
    print("Free symbols in expression", expr, ":", expr.free_symbols())
    print()

    # 15. Using Add, Mul, Pow classes
    a = Add(x, y)
    m = Mul(x, y)
    p = Pow(x, 2)
    print("Add(x, y):", a)
    print("Mul(x, y):", m)
    print("Pow(x, 2):", p)
    print()

    print("=== Demo complete ===")
