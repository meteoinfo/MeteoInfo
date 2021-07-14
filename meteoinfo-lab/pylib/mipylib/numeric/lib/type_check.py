from ..core.numeric import asarray

__all__ = ['mintypecode']

_typecodes_by_elsize = 'GDFgdfQqLlIiHhBb?'

def mintypecode(typechars, typeset='GDFgdf', default='d'):
    """
    Return the character for the minimum-size type to which given types can
    be safely cast.
    The returned type character must represent the smallest size dtype such
    that an array of the returned type can handle the data from an array of
    all types in `typechars` (or if `typechars` is an array, then its
    dtype.char).
    Parameters
    ----------
    typechars : list of str or array_like
        If a list of strings, each string should represent a dtype.
        If array_like, the character representation of the array dtype is used.
    typeset : str or list of str, optional
        The set of characters that the returned character is chosen from.
        The default set is 'GDFgdf'.
    default : str, optional
        The default character, this is returned if none of the characters in
        `typechars` matches a character in `typeset`.
    Returns
    -------
    typechar : str
        The character representing the minimum-size type that was found.
    See Also
    --------
    dtype, sctype2char, maximum_sctype
    Examples
    --------
    >>> np.mintypecode(['d', 'f', 'S'])
    'd'
    >>> x = np.array([1.1, 2-3.j])
    >>> np.mintypecode(x)
    'D'
    >>> np.mintypecode('abceh', default='G')
    'G'
    """
    typecodes = ((isinstance(t, str) and t) or asarray(t).dtype.char
                 for t in typechars)
    intersection = set(t for t in typecodes if t in typeset)
    if not intersection:
        return default
    if 'F' in intersection and 'd' in intersection:
        return 'D'
    return min(intersection, key=_typecodes_by_elsize.index)