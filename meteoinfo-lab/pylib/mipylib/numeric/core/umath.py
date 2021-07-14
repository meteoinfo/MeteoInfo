"""
Universal math functions
"""

import __builtin__
from ._ndarray import NDArray
from .numeric import sign

__all__ = [
    'absolute','add','divmod','floor_divide','fmod','mod','remainder'
    ]

def absolute(x):
    '''
    Calculate the absolute value element-wise.

    :param x: (*array_like*) Input array.

    :returns: An array containing the absolute value of each element in x.
        For complex input, a + ib, the absolute value is \sqrt{ a^2 + b^2 }.
    '''
    if isinstance(x, list):
        x = array(x)
    if isinstance(x, NDArray):
        return x.abs()
    else:
        return __builtin__.abs(x)

def add(x1, x2):
    """
    Add arguments element-wise.

    :param x1: (*array_like*) The array to be added.
    :param x2: (*array_like*) The array to be added.
    :return: (*array_like*) Added array
    """
    if isinstance(x1, (list, tuple)):
        x1 = NDArray(x1)

    if isinstance(x2, (list, tuple)):
        x2 = NDArray(x2)

    if isinstance(x1, NDArray):
        return x1.__add__(x2)
    elif isinstance(x2, NDArray):
        return x2.__radd__(x1)
    else:
        return x1 + x2

def floor_divide(x1, x2):
    """
    Return the largest integer smaller or equal to the division of the inputs. It is equivalent to the
    Python // operator and pairs with the Python % (remainder), function so that a = a % b + b * (a // b)
    up to roundoff.

    :param x1: (*array_like*) Numerator.
    :param x2: (*array_like*) Denominator.
    :return: (*array*) Result array.
    """
    if isinstance(x1, (list, tuple)):
        x1 = NDArray(x1)

    if isinstance(x2, (list, tuple)):
        x2 = NDArray(x2)

    if isinstance(x1, NDArray):
        return x1.__floordiv__(x2)
    elif isinstance(x2, NDArray):
        return x2.__rfloordiv__(x1)
    else:
        return x1 // x2

def mod(x1, x2):
    """
    Return element-wise remainder of division.

    :param x1: (*array_like*) Dividend array.
    :param x2: (*array_like*) Divisor array.
    :return: (*array*) remainder array
    """
    if isinstance(x1, (list, tuple)):
        x1 = NDArray(x1)

    if isinstance(x2, (list, tuple)):
        x2 = NDArray(x2)

    if isinstance(x1, NDArray):
        return x1.__mod__(x2)
    elif isinstance(x2, NDArray):
        return x2.__rmod__(x1)
    else:
        return x1 % x2

def remainder(x1, x2):
    """
    Return element-wise remainder of division.

    :param x1: (*array_like*) Dividend array.
    :param x2: (*array_like*) Divisor array.
    :return: (*array*) remainder array
    """
    return mod(x1, x2)

def divmod(x1, x2):
    """
    Return element-wise quotient and remainder simultaneously.

    :param x1: (*array_like*) Dividend array.
    :param x2: (*array_like*) Divisor array.
    :return: Element-wise quotient and remainder array.
    """
    if isinstance(x1, (list, tuple)):
        x1 = NDArray(x1)

    if isinstance(x2, (list, tuple)):
        x2 = NDArray(x2)

    if isinstance(x1, NDArray):
        return x1.__divmod__(x2)
    elif isinstance(x2, NDArray):
        return x2.__rdivmod__(x1)
    else:
        return __builtin__.divmod(x1, x2)

def fmod(x1, x2):
    """
    Return the element-wise remainder of division. For `fmod`, the sign of result is the sign of the
    dividend, while for `remainder` the sign of the result is the sign of the divisor.

    :param x1: (*array_like*) Dividend array.
    :param x2: (*array_like*) Divisor array.
    :return: Element-wise remainder array.
    """
    s = sign(x1)
    return mod(x1, x2) * s