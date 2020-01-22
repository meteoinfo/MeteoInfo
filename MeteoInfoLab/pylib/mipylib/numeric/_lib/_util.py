import inspect

def getargspec_no_self(func):
    """inspect.getargspec replacement for compatibility with python 3.x.
    inspect.getargspec is deprecated in python 3. This wraps it, and
    *removes* `self` from the argument list of `func`, if present.
    This is done for forward compatibility with python 3.
    Parameters
    ----------
    func : callable
        A callable to inspect
    Returns
    -------
    argspec : ArgSpec(args, varargs, varkw, defaults)
        This is similar to the result of inspect.getargspec(func) under
        python 2.x.
        NOTE: if the first argument of `func` is self, it is *not*, I repeat
        *not* included in argspec.args.
        This is done for consistency between inspect.getargspec() under
        python 2.x, and inspect.signature() under python 3.x.
    """
    argspec = inspect.getargspec(func)
    if argspec.args[0] == 'self':
        argspec.args.pop(0)
    return argspec