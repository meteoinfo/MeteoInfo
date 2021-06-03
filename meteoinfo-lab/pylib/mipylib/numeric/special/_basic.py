from ..core import numeric as np
from ._gamma import gamma

from org.meteoinfo.math.special import SpecialUtil

__all__ = ['factorial']

def factorial(n, exact=False):
    """
    The factorial of a number or array of numbers.

    The factorial of non-negative integer n is the product of all positive integers less than or equal to n:
    :param n: (*int or array_like of ints*) Input values. If `n < 0`, the return value is 0.
    :param exact: (*bool*) If True, calculate the answer exactly using long integer arithmetic. If False,
        result is approximated in floating point rapidly using the `gamma` function. Default is False.
    :return: Factorial of *n*, as integer or float depending on *exact*.
    """
    if isinstance(n, (list, tuple)):
        n = np.array(n)

    if exact:
        if isinstance(n, np.NDArray):
            n = n.asarray()

        r = SpecialUtil.factorial(n)
        if isinstance(r, long):
            return r
        else:
            return np.array(r)
    else:
        return gamma(n + 1)