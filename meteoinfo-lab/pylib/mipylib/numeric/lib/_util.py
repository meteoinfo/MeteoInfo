import inspect
import numbers
from mipylib import numeric as np

def prod(iterable):
    """
    Product of a sequence of numbers.
    Faster than np.prod for short lists like array shapes, and does
    not overflow if using Python integers.
    """
    product = 1
    for x in iterable:
        product *= x
    return product

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

def _lazywhere(cond, arrays, f, fillvalue=None, f2=None):
    """
    np.where(cond, x, fillvalue) always evaluates x even where cond is False.
    This one only evaluates f(arr1[cond], arr2[cond], ...).
    Examples
    --------
    >>> a, b = np.array([1, 2, 3, 4]), np.array([5, 6, 7, 8])
    >>> def f(a, b):
    ...     return a*b
    >>> _lazywhere(a > 2, (a, b), f, np.nan)
    array([ nan,  nan,  21.,  32.])
    Notice, it assumes that all `arrays` are of the same shape, or can be
    broadcasted together.
    """
    cond = np.asarray(cond)
    if fillvalue is None:
        if f2 is None:
            raise ValueError("One of (fillvalue, f2) must be given.")
        else:
            fillvalue = np.nan
    else:
        if f2 is not None:
            raise ValueError("Only one of (fillvalue, f2) can be given.")

    args = np.broadcast_arrays(cond, *arrays)
    cond,  arrays = args[0], args[1:]
    temp = tuple(np.extract(cond, arr) for arr in arrays)
    tcode = np.mintypecode([a.dtype.char for a in arrays])
    out = np.full(np.shape(arrays[0]), fill_value=fillvalue, dtype=tcode)
    np.place(out, cond, f(*temp))
    if f2 is not None:
        temp = tuple(np.extract(~cond, arr) for arr in arrays)
        np.place(out, ~cond, f2(*temp))

    return out

def _asarray_validated(a, check_finite=True,
                       sparse_ok=False, objects_ok=False, mask_ok=False,
                       as_inexact=False):
    """
    Helper function for SciPy argument validation.
    Many SciPy linear algebra functions do support arbitrary array-like
    input arguments. Examples of commonly unsupported inputs include
    matrices containing inf/nan, sparse matrix representations, and
    matrices with complicated elements.
    Parameters
    ----------
    a : array_like
        The array-like input.
    check_finite : bool, optional
        Whether to check that the input matrices contain only finite numbers.
        Disabling may give a performance gain, but may result in problems
        (crashes, non-termination) if the inputs do contain infinities or NaNs.
        Default: True
    sparse_ok : bool, optional
        True if scipy sparse matrices are allowed.
    objects_ok : bool, optional
        True if arrays with dype('O') are allowed.
    mask_ok : bool, optional
        True if masked arrays are allowed.
    as_inexact : bool, optional
        True to convert the input array to a np.inexact dtype.
    Returns
    -------
    ret : ndarray
        The converted validated array.
    """
    if not sparse_ok:
        import scipy.sparse
        if scipy.sparse.issparse(a):
            msg = ('Sparse matrices are not supported by this function. '
                   'Perhaps one of the scipy.sparse.linalg functions '
                   'would work instead.')
            raise ValueError(msg)
    if not mask_ok:
        if np.ma.isMaskedArray(a):
            raise ValueError('masked arrays are not supported')
    toarray = np.asarray_chkfinite if check_finite else np.asarray
    a = toarray(a)
    if not objects_ok:
        if a.dtype is np.dtype('O'):
            raise ValueError('object arrays are not supported')
    if as_inexact:
        if not np.issubdtype(a.dtype, np.inexact):
            a = toarray(a, dtype=np.float_)
    return a

def check_random_state(seed):
    """Turn `seed` into a `np.random.RandomState` instance.
    Parameters
    ----------
    seed : {None, int, `np.random.Generator`,
            `np.random.RandomState`}, optional
        If `seed` is None (or `np.random`), the `numpy.random.RandomState`
        singleton is used.
        If `seed` is an int, a new ``RandomState`` instance is used,
        seeded with `seed`.
        If `seed` is already a ``Generator`` or ``RandomState`` instance then
        that instance is used.
    Returns
    -------
    seed : {`np.random.Generator`, `np.random.RandomState`}
        Random number generator.
    """
    if seed is None or seed is np.random:
        return np.random.mtrand._rand
    if isinstance(seed, numbers.Integral):
        return np.random.RandomState(seed)
    if isinstance(seed, np.random.RandomState):
        return seed

    raise ValueError('%r cannot be used to seed a numpy.random.RandomState'
                     ' instance' % seed)

class _RichResult(dict):
    """ Container for multiple outputs with pretty-printing """
    def __getattr__(self, name):
        try:
            return self[name]
        except KeyError as e:
            raise AttributeError(name)

    __setattr__ = dict.__setitem__  # type: ignore[assignment]
    __delattr__ = dict.__delitem__  # type: ignore[assignment]

    def __repr__(self):
        order_keys = ['message', 'success', 'status', 'fun', 'funl', 'x', 'xl',
                      'col_ind', 'nit', 'lower', 'upper', 'eqlin', 'ineqlin',
                      'converged', 'flag', 'function_calls', 'iterations',
                      'root']
        order_keys = getattr(self, '_order_keys', order_keys)
        # 'slack', 'con' are redundant with residuals
        # 'crossover_nit' is probably not interesting to most users
        omit_keys = {'slack', 'con', 'crossover_nit', '_order_keys'}

        def key(item):
            try:
                return order_keys.index(item[0].lower())
            except ValueError:  # item not in list
                return np.inf

        def omit_redundant(items):
            for item in items:
                if item[0] in omit_keys:
                    continue
                yield item

        def item_sorter(d):
            return sorted(omit_redundant(d.items()), key=key)

        if self.keys():
            return _dict_formatter(self, sorter=item_sorter)
        else:
            return self.__class__.__name__ + "()"

    def __dir__(self):
        return list(self.keys())
