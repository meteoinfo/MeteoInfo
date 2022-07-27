
# Helper utilities for metadata
class either(object):
    """A callable class that determines which variable is present in the
    file.

    This is used in situations where the same variable type has different names
    depending on the type of file used.  For example, in a WRF output file,
    'P' is used for pressure, whereas in a met_em file, pressure is named
    'PRES'.

    Methods:
        __call__(wrfin): Return the variable that is present in the file.
            Args:
                wrfin (:class:`netCDF4.Dataset`, :class:`Nio.NioFile`, or an \
                iterable): WRF-ARW NetCDF
                    data as a :class:`netCDF4.Dataset`, :class:`Nio.NioFile`
                    or an iterable sequence of the aforementioned types.
            Returns:
                :obj:`str`: The variable name that is present in the file.

    Attributes:
        varnames (sequence): A sequence of possible variable names.
    """
    def __init__(self, *varnames):
        """Initialize an :class:`either` object.

        Args:
            *varnames (sequence): A sequence of possible variable names.
        """
        self.varnames = varnames

    def __call__(self, wrfin):
        for varname in self.varnames:
            if varname in wrfin.varnames:
                return varname

        raise ValueError("{} are not valid variable names".format(
            self.varnames))