
from .expr import Expr
from org.matheclipse.core.expression import F


__all__ = ['sympify']


def sympify(a):
    """
    Converts an arbitrary expression to a type that can be used inside SymJy.

    Explanation
    ===========

    It will convert Python ints into instances of :class:`~.Integer`, floats
    into instances of :class:`~.Float`, etc. It is also able to coerce
    symbolic expressions which inherit from :class:`~.Basic`. This can be
    useful in cooperation with SAGE.

    .. warning::
        Note that this function uses ``eval``, and thus shouldn't be used on
        unsanitized input.

    If the argument is already a type that SymPy understands, it will do
    nothing but return that value. This can be used at the beginning of a
    function to ensure you are working with the correct type.
    """
    r = F.eval(a)
    return Expr(r)