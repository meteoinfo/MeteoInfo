from org.meteoinfo.ndarray.io.matlab import Mat
from ..core._ndarray import NDArray

__all__ = ['loadmat']


def loadmat(file):
    """
    Load MATLAB file.

    :param file: (*str*) Data file path.

    :return: Diction of arrays.
    """
    data_map = Mat.load(file)
    data = {k: NDArray(data_map[k]) for k in data_map}
    return data
