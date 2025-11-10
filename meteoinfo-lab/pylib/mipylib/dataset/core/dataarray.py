import mipylib.numeric as np
from .variable import Variable


class DataArray(np.NDArray):
    """N-dimensional array with labeled coordinates and dimensions.

    DataArray provides a wrapper around numpy ndarrays that uses
    labeled dimensions and coordinates to support metadata aware
    operations. The API is similar to that for the pandas Series or
    DataFrame, but DataArray objects can have any number of dimensions,
    and their contents have fixed data types.

    Additional features over raw numpy arrays:

    - Apply operations over dimensions by name: ``x.sum('time')``.
    - Select or assign values by integer location (like numpy):
      ``x[:10]`` or by label (like pandas): ``x.loc['2014-01-01']`` or
      ``x.sel(time='2014-01-01')``.
    - Mathematical operations (e.g., ``x - y``) vectorize across
      multiple dimensions (known in numpy as "broadcasting") based on
      dimension names, regardless of their original order.
    - Keep track of arbitrary metadata in the form of a Python
      dictionary: ``x.attrs``
    - Convert to a pandas Series: ``x.to_series()``.

    Getting items from or doing mathematical operations with a
    DataArray always returns another DataArray.

    Parameters
    ----------
    data : array_like
        Values for this array. Must be an ``ndarray``, ndarray
        like, or castable to an ``ndarray``. If a self-described xarray
        or pandas object, attempts are made to use this array's
        metadata to fill in other unspecified arguments. A view of the
        array's data is used instead of a copy if possible.
    coords : sequence or dict of array_like or :py:class:`~Coordinates`, optional
        Coordinates (tick labels) to use for indexing along each
        dimension. The following notations are accepted:

        - mapping {dimension name: array-like}
        - sequence of tuples that are valid arguments for
          ``xarray.Variable()``
          - (dims, data)
          - (dims, data, attrs)
          - (dims, data, attrs, encoding)

        Additionally, it is possible to define a coord whose name
        does not match the dimension name, or a coord based on multiple
        dimensions, with one of the following notations:

        - mapping {coord name: DataArray}
        - mapping {coord name: Variable}
        - mapping {coord name: (dimension name, array-like)}
        - mapping {coord name: (tuple of dimension names, array-like)}

        Alternatively, a :py:class:`~Coordinates` object may be used in
        order to explicitly pass indexes (e.g., a multi-index or any custom
        Xarray index) or to bypass the creation of a default index for any
        :term:`Dimension coordinate` included in that object.
    dims : Hashable or sequence of Hashable, optional
        Name(s) of the data dimension(s). Must be either a Hashable
        (only for 1D data) or a sequence of Hashables with length equal
        to the number of dimensions. If this argument is omitted,
        dimension names are taken from ``coords`` (if possible) and
        otherwise default to ``['dim_0', ... 'dim_n']``.
    name : str or None, optional
        Name of this array.
    attrs : dict_like or None, optional
        Attributes to assign to the new instance. By default, an empty
        attribute dictionary is initialized.
    """

    def __init__(self, data, coords=None, dims=None, name=None, attrs=None):
        self._variable = Variable(dims, data, attrs)
        self._coords = coords
        self._dims = dims
        self._name = name
        self._attrs = attrs

    @property
    def name(self):
        """The name of this array."""
        return self._name

    @name.setter
    def name(self, value):
        self._name = value

    @property
    def variable(self):
        """Low level interface to the Variable object for this DataArray."""
        return self._variable

    @property
    def dtype(self):
        """
        Data-type of the array’s elements.

        See Also
        --------
        ndarray.dtype
        numpy.dtype
        """
        return self.variable.dtype

    @property
    def shape(self):
        """
        Tuple of array dimensions.

        See Also
        --------
        ndarray.shape
        """
        return self.variable.shape
