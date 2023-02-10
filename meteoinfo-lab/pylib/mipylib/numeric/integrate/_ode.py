# coding=utf-8

from org.meteoinfo.math.integrate import ODEEquations, IntegrateUtil
from ..core import numeric as np

__all__ = ['odeint']


class ODE(ODEEquations):

    def __init__(self, f):
        """
        Initialize

        :param f: Jython function
        """
        self.f = f
        self._args = list(f.__code__.co_varnames)[2:]
        self._args = tuple(self._args)
        self.order = len(self._args)

    def doComputeDerivatives(self, y, t):
        args = tuple(self.getParameters())
        return self.f(y, t, *args)


def odeint(func, y0, t, args=()):
    """
    Integrate a system of ordinary differential equations.

    :param func: (callable(y, t, …) ) Computes the derivative of y at t. 
    :param y0: (*array*) Initial condition on y (can be a vector).
    :param t: (*array*) A sequence of time points for which to solve for y. The initial value point should
        be the first element of this sequence.
    :param args: (*tuple*) Extra arguments to pass to function.
    :return: Array containing the value of y for each desired time in t.
    """
    func = ODE(func)
    if len(args) > 0:
        func.setParameters(args)

    if isinstance(y0, (tuple, list)):
        y0 = np.array(y0)
    ndim = len(y0)
    func.setDimension(ndim)

    if isinstance(t, (tuple, list)):
        t = np.array(t)

    r = IntegrateUtil.odeIntegrate(func, y0.asarray(), t.asarray())

    return np.NDArray(r)
