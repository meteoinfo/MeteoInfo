from org.meteoinfo.chart.io import WavefrontObjectLoader
import mipylib.numeric as np

__all__ = ['load_obj_model']


def load_obj_model(filename):
    """
    Load wave front object model.

    :param filename: (*str*) Wave front object file name.

    :return: (*array list*) Face indices array and x,y,z coordinates array.
    """
    obj = WavefrontObjectLoader(filename)
    face = obj.getVertexIndicesArray()
    vertex = obj.getVertexArrays()
    x = vertex[0]
    y = vertex[1]
    z = vertex[2]
    normal = obj.getVertexNormalArray()
    if normal is not None:
        normal = np.array(normal)

    return np.array(face), np.array(x), np.array(y), np.array(z), normal
