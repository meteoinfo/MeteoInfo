"""
Exceptions
"""

class AxisError(IndexError):
    """ Axis supplied was invalid. """
    def __init__(self, axis, ndim=None, msg_prefix=None):
        # single-argument form just delegates to base class
        if ndim is None and msg_prefix is None:
            msg = axis

        # do the string formatting here, to save work in the C code
        else:
            msg = ("axis {} is out of bounds for array of dimension {}"
                   .format(axis, ndim))
            if msg_prefix is not None:
                msg = "{}: {}".format(msg_prefix, msg)

        super(IndexError, self).__init__(msg)