import types as _types
from ._dtype import dtype

__all__ = ['ScalarType', 'find_common_type']

try:
    ScalarType = [_types.IntType, _types.FloatType, _types.ComplexType,
                  _types.LongType, _types.BooleanType,
                  _types.StringType, _types.UnicodeType, _types.BufferType]
except AttributeError:
    # Py3K
    ScalarType = [int, float, complex, int, bool, bytes, str, memoryview]

#ScalarType.extend(_concrete_types)
ScalarType = tuple(ScalarType)

typecodes = {'Character':'c',
             'Integer':'bhilqp',
             'UnsignedInteger':'BHILQP',
             'Float':'efdg',
             'Complex':'FDG',
             'AllInteger':'bBhHiIlLqQpP',
             'AllFloat':'efdgFDG',
             'Datetime': 'Mm',
             'All':'?bhilqpBHILQPefdgFDGSUVOMm'}

# b -> boolean
# u -> unsigned integer
# i -> signed integer
# f -> floating point
# c -> complex
# M -> datetime
# m -> timedelta
# S -> string
# U -> Unicode string
# V -> record
# O -> Python object
_kind_list = ['b', 'u', 'i', 'f', 'c', 'S', 'U', 'V', 'O', 'M', 'm']

__test_types = '?'+typecodes['AllInteger'][:-2]+typecodes['AllFloat']+'O'
__len_test_types = len(__test_types)

# Keep incrementing until a common type both can be coerced to
#  is found.  Otherwise, return None
def _find_common_coerce(a, b):
    if a > b:
        return a
    try:
        thisind = __test_types.index(a.char)
    except ValueError:
        return None
    return _can_coerce_all([a, b], start=thisind)

# Find a data-type that all data-types in a list can be coerced to
def _can_coerce_all(dtypelist, start=0):
    N = len(dtypelist)
    if N == 0:
        return None
    if N == 1:
        return dtypelist[0]
    thisind = start
    while thisind < __len_test_types:
        newdtype = dtype(__test_types[thisind])
        numcoerce = len([x for x in dtypelist if newdtype >= x])
        if numcoerce == N:
            return newdtype
        thisind += 1
    return None

def find_common_type(array_types, scalar_types):
    """
    Determine common type following standard coercion rules.
    Parameters
    ----------
    array_types : sequence
        A list of dtypes or dtype convertible objects representing arrays.
    scalar_types : sequence
        A list of dtypes or dtype convertible objects representing scalars.
    Returns
    -------
    datatype : dtype
        The common data type, which is the maximum of `array_types` ignoring
        `scalar_types`, unless the maximum of `scalar_types` is of a
        different kind (`dtype.kind`). If the kind is not understood, then
        None is returned.
    See Also
    --------
    dtype, common_type, can_cast, mintypecode
    Examples
    --------
    >>> np.find_common_type([], [np.int64, np.float32, complex])
    dtype('complex128')
    >>> np.find_common_type([np.int64, np.float32], [])
    dtype('float64')
    The standard casting rules ensure that a scalar cannot up-cast an
    array unless the scalar is of a fundamentally different kind of data
    (i.e. under a different hierarchy in the data type hierarchy) then
    the array:
    >>> np.find_common_type([np.float32], [np.int64, np.float64])
    dtype('float32')
    Complex is of a different type, so it up-casts the float in the
    `array_types` argument:
    >>> np.find_common_type([np.float32], [complex])
    dtype('complex128')
    Type specifier strings are convertible to dtypes and can therefore
    be used instead of dtypes:
    >>> np.find_common_type(['f4', 'f4', 'i4'], ['c8'])
    dtype('complex128')
    """
    array_types = [dtype(x) for x in array_types]
    scalar_types = [dtype(x) for x in scalar_types]

    maxa = _can_coerce_all(array_types)
    maxsc = _can_coerce_all(scalar_types)

    if maxa is None:
        return maxsc

    if maxsc is None:
        return maxa

    try:
        index_a = _kind_list.index(maxa.kind)
        index_sc = _kind_list.index(maxsc.kind)
    except ValueError:
        return None

    if index_sc > index_a:
        return _find_common_coerce(maxsc, maxa)
    else:
        return maxa