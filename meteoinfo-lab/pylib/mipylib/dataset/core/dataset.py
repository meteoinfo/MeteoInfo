
class Dataset(object):
    """A multi-dimensional, in memory, array database.

    A dataset resembles an in-memory representation of a NetCDF file,
    and consists of variables, coordinates and attributes which
    together form a self describing dataset.

    Dataset implements the mapping interface with keys given by variable
    names and values given by DataArray objects for each variable name.

    By default, pandas indexes are created for one dimensional variables with
    name equal to their dimension (i.e., :term:`Dimension coordinate`) so those
    variables can be readily used as coordinates for label based indexing. When a
    :py:class:`~xarray.Coordinates` object is passed to ``coords``, any existing
    index(es) built from those coordinates will be added to the Dataset.

    To load data from a file or file-like object, use the `open_dataset`
    function.

    Parameters
    ----------
    data_vars : dict-like, optional
        A mapping from variable names to :py:class:`~xarray.DataArray`
        objects, :py:class:`~xarray.Variable` objects or to tuples of
        the form ``(dims, data[, attrs])`` which can be used as
        arguments to create a new ``Variable``. Each dimension must
        have the same length in all variables in which it appears.

        The following notations are accepted:

        - mapping {var name: DataArray}
        - mapping {var name: Variable}
        - mapping {var name: (dimension name, array-like)}
        - mapping {var name: (tuple of dimension names, array-like)}
        - mapping {dimension name: array-like}
          (if array-like is not a scalar it will be automatically moved to coords,
          see below)

        Each dimension must have the same length in all variables in
        which it appears.
    coords : :py:class:`~xarray.Coordinates` or dict-like, optional
        A :py:class:`~xarray.Coordinates` object or another mapping in
        similar form as the `data_vars` argument, except that each item
        is saved on the dataset as a "coordinate".
        These variables have an associated meaning: they describe
        constant/fixed/independent quantities, unlike the
        varying/measured/dependent quantities that belong in
        `variables`.

        The following notations are accepted for arbitrary mappings:

        - mapping {coord name: DataArray}
        - mapping {coord name: Variable}
        - mapping {coord name: (dimension name, array-like)}
        - mapping {coord name: (tuple of dimension names, array-like)}
        - mapping {dimension name: array-like}
          (the dimension name is implicitly set to be the same as the
          coord name)

        The last notation implies either that the coordinate value is a scalar
        or that it is a 1-dimensional array and the coord name is the same as
        the dimension name (i.e., a :term:`Dimension coordinate`). In the latter
        case, the 1-dimensional array will be assumed to give index values
        along the dimension with the same name.

        Alternatively, a :py:class:`~xarray.Coordinates` object may be used in
        order to explicitly pass indexes (e.g., a multi-index or any custom
        Xarray index) or to bypass the creation of a default index for any
        :term:`Dimension coordinate` included in that object.

    attrs : dict-like, optional
        Global attributes to save on this dataset.
        (see FAQ, :ref:`approach-to-metadata`)

    Examples
    --------
    In this example dataset, we will represent measurements of the temperature
    and pressure that were made under various conditions:

    * the measurements were made on four different days;
    * they were made at two separate locations, which we will represent using
      their latitude and longitude; and
    * they were made using three instrument developed by three different
      manufacturers, which we will refer to using the strings `'manufac1'`,
      `'manufac2'`, and `'manufac3'`.

    >>> np.random.seed(0)
    >>> temperature = 15 + 8 * np.random.randn(2, 3, 4)
    >>> precipitation = 10 * np.random.rand(2, 3, 4)
    >>> lon = [-99.83, -99.32]
    >>> lat = [42.25, 42.21]
    >>> instruments = ["manufac1", "manufac2", "manufac3"]
    >>> time = pd.date_range("2014-09-06", periods=4)
    >>> reference_time = pd.Timestamp("2014-09-05")

    Here, we initialize the dataset with multiple dimensions. We use the string
    `"loc"` to represent the location dimension of the data, the string
    `"instrument"` to represent the instrument manufacturer dimension, and the
    string `"time"` for the time dimension.

    >>> ds = xr.Dataset(
    ...     data_vars=dict(
    ...         temperature=(["loc", "instrument", "time"], temperature),
    ...         precipitation=(["loc", "instrument", "time"], precipitation),
    ...     ),
    ...     coords=dict(
    ...         lon=("loc", lon),
    ...         lat=("loc", lat),
    ...         instrument=instruments,
    ...         time=time,
    ...         reference_time=reference_time,
    ...     ),
    ...     attrs=dict(description="Weather related data."),
    ... )
    >>> ds
    <xarray.Dataset> Size: 552B
    Dimensions:         (loc: 2, instrument: 3, time: 4)
    Coordinates:
        lon             (loc) float64 16B -99.83 -99.32
        lat             (loc) float64 16B 42.25 42.21
      * instrument      (instrument) <U8 96B 'manufac1' 'manufac2' 'manufac3'
      * time            (time) datetime64[us] 32B 2014-09-06 ... 2014-09-09
        reference_time  datetime64[us] 8B 2014-09-05
    Dimensions without coordinates: loc
    Data variables:
        temperature     (loc, instrument, time) float64 192B 29.11 18.2 ... 9.063
        precipitation   (loc, instrument, time) float64 192B 4.562 5.684 ... 1.613
    Attributes:
        description:  Weather related data.

    Find out where the coldest temperature was and what values the
    other variables had:

    >>> ds.isel(ds.temperature.argmin(...))
    <xarray.Dataset> Size: 80B
    Dimensions:         ()
    Coordinates:
        lon             float64 8B -99.32
        lat             float64 8B 42.21
        instrument      <U8 32B 'manufac3'
        time            datetime64[us] 8B 2014-09-06
        reference_time  datetime64[us] 8B 2014-09-05
    Data variables:
        temperature     float64 8B -5.424
        precipitation   float64 8B 9.884
    Attributes:
        description:  Weather related data.
    """

    def __init__(self, data_vars=None, coords=None, attrs=None):
        if data_vars is None:
            data_vars = {}

        if coords is None:
            coords = {}

        self._attrs = dict(attrs) if attrs else None

