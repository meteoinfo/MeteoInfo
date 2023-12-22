from org.meteoinfo.ndarray.io.npy import Npy, Npz
from ..core._ndarray import NDArray

__all__ = ['load', 'save', 'savez']


class NpzFile:
    """
    NpzFile(fid)

    A dictionary-like object with lazy-loading of files in the zipped
    archive provided on construction.

    `NpzFile` is used to load files in the NumPy ``.npz`` data archive
    format. It assumes that files in the archive have a ``.npy`` extension,
    other files are ignored.

    Attributes
    ----------
    files : list of str
        List of all files in the archive with a ``.npy`` extension.
    zip_file : ZipFile instance
        The ZipFile object initialized with the zipped archive.
    """
    zip_file = None
    _MAX_REPR_ARRAY_COUNT = 5

    def __init__(self, file):
        self.filename = file
        self.zip_file = Npz.open(file)
        self._files = list(Npz.entries(self.zip_file))
        self.files = []
        for x in self._files:
            if x.endswith('.npy'):
                self.files.append(x[:-4])
            else:
                self.files.append(x)

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_value, traceback):
        self.close()

    def close(self):
        """
        Close the zip file.
        """
        if self.zip_file is not None:
            self.zip_file.close()
            self.zip_file = None

    def __del__(self):
        self.close()

    # Implement the Mapping ABC
    def __iter__(self):
        return iter(self.files)

    def __len__(self):
        return len(self.files)

    def __contains__(self, key):
        return (key in self._files or key in self.files)

    def __repr__(self):
        # Get the name of arrays
        array_names = ', '.join(self.files[:self._MAX_REPR_ARRAY_COUNT])
        if len(self.files) > self._MAX_REPR_ARRAY_COUNT:
            array_names += "..."
        return "NpzFile {} with keys: {}".format(self.filename, array_names)

    def __getitem__(self, key):
        member = False
        if key in self._files:
            member = True
        elif key in self.files:
            member = True
            key += '.npy'
        if member:
            a = Npz.load(self.zip_file, key)
            return NDArray(a)
        else:
            raise KeyError("{} is not a file in the archive".format(key))


def load(file):
    """
    Load arrays from `npy` or `npz` data file.

    :param file: (*str*) Data file path.

    :return: Array or diction of arrays.
    """
    # Code to distinguish from NumPy binary files and pickles.
    MAGIC_PREFIX = b'\x93NUMPY'
    _ZIP_PREFIX = b'PK\x03\x04'
    _ZIP_SUFFIX = b'PK\x05\x06' # empty zip files start with this

    fid = open(file, 'rb')
    N = len(MAGIC_PREFIX)
    magic = fid.read(N)
    fid.close()
    if not magic:
        raise EOFError("No data left in file")

    if magic.startswith(_ZIP_PREFIX) or magic.startswith(_ZIP_SUFFIX):
        return NpzFile(file)
    else:
        a = Npy.load(file)
        return NDArray(a)

def save(file, arr):
    """
    Save an array to a binary file in NumPy .npy format.

    :param file: (*str*) Npy file path.
    :param arr: (*array*) Array data to be saved.
    """
    if not file.endswith('.npy'):
        file = file + '.npy'

    Npy.save(file, arr._array)

def savez(file, *args, **kwds):
    """
    Save several arrays into a single file in uncompressed ``.npz`` format.

    Provide arrays as keyword arguments to store them under the
    corresponding name in the output file: ``savez(fn, x=x, y=y)``.

    If arrays are specified as positional arguments, i.e., ``savez(fn,
    x, y)``, their names will be `arr_0`, `arr_1`, etc.

    Parameters
    ----------
    file : str
        The filename (string) where the data will be saved. If file is a string or a Path, the
        ``.npz`` extension will be appended to the filename if it is not
        already there.
    args : Arguments, optional
        Arrays to save to the file. Please use keyword arguments (see
        `kwds` below) to assign names to arrays.  Arrays specified as
        args will be named "arr_0", "arr_1", and so on.
    kwds : Keyword arguments, optional
        Arrays to save to the file. Each array will be saved to the
        output file with its corresponding keyword name.
    """
    if not file.endswith('.npz'):
        file = file + '.npz'

    namedict = kwds
    for i, val in enumerate(args):
        key = 'arr_{}'.format(i)
        if key in namedict.keys():
            raise ValueError(
                "Cannot use un-named variables and keyword {}".format(key))
        namedict[key] = val

    outstream = Npz.create(file)
    for key, val in namedict.items():
        fname = key + '.npy'
        Npz.write(outstream, key, val._array)

    outstream.close()
