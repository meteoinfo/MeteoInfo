# coding=utf-8
"""
Array input output
"""

from org.meteoinfo.ndarray.math import ArrayUtil

import _dtype
from ._ndarray import NDArray

__all__ = [
    'fromfile','frombuffer'
    ]

def fromfile(file, dtype=_dtype.float, count=-1, sep='', offset=0):
    """
    Construct an array from data in a text or binary file.
    :param file: (*str*) Open file object or filename.
    :param dtype: (*dtype*) Data type of the returned array.
    :param count: (*int*) Number of items to read. -1 means all items (i.e., the complete file).
    :param sep: (*str*) Separator between items if file is a text file. Empty (“”) separator means
        the file should be treated as binary.
    :param offset: (*int*) The offset (in bytes) from the file’s current position. Defaults to 0. Only
        permitted for binary files.
    :return: (*array*) The array read from the file.
    """
    if not isinstance(dtype, _dtype.DataType):
        dtype = _dtype.DataType(dtype)

    if sep:
        r = ArrayUtil.readASCIIFile(file, dtype._dtype, count, sep)
    else:
        r = ArrayUtil.readBinFile(file, dtype._dtype, count, offset)
    return NDArray(r)


def frombuffer(buffer, dtype=_dtype.float, count=-1, offset=0):
    """
    Interpret a buffer as a 1-dimensional array.

    :param buffer: (*buffer-like*) An object that exposes the buffer interface.
    :param dtype: (*dtype*) Data type. Defaults to float.
    :param count: (*int*) Number of items to read. -1 means all items.
    :param offset: (*int*) The offset (in bytes) from the file’s current position. Defaults to 0.
    :return: (*array*) The array read from the buffer.
    """
    if not isinstance(dtype, _dtype.DataType):
        dtype = _dtype.DataType(dtype)

    byteorder = 'little_endian' if dtype.byteorder == '<' else 'big_endian'
    r = ArrayUtil.fromBuffer(buffer, dtype._dtype, count, offset, byteorder)
    return NDArray(r)
