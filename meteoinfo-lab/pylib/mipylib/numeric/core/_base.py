from org.meteoinfo.ndarray import FlatIndex
from org.meteoinfo.ndarray.math import ArrayUtil

class flatiter(object):

    def __init__(self, array):
        self.array = array
        self.flat_index = FlatIndex(array._array)
        self.size = self.array.size

    def __iter__(self):
        return self

    def next(self):
        return self.array.next()

    def __getitem__(self, key):
        if isinstance(key, int):
            return self.flat_index.getObject(key)
        elif isinstance(key, slice):
            sidx = 0 if key.start is None else key.start
            if sidx < 0:
                sidx = self.size + sidx
            eidx = self.size if key.stop is None else key.stop
            if eidx < 0:
                eidx = self.size + eidx
            eidx -= 1
            step = 1 if key.step is None else key.step
            r = self.flat_index.section(sidx, eidx, step)
            return asarray(r)
        else:
            r = self.flat_index.section(key)
            return asarray(r)

    def __setitem__(self, key, value):
        if isinstance(value, (list, tuple)):
            value = ArrayUtil.array(value, None)

        if isinstance(key, int):
            return self.flat_index.setSection(key, value)
        elif isinstance(key, slice):
            sidx = 0 if key.start is None else key.start
            if sidx < 0:
                sidx = self.size + sidx
            eidx = self.size if key.stop is None else key.stop
            if eidx < 0:
                eidx = self.size + eidx
            eidx -= 1
            step = 1 if key.step is None else key.step
            self.flat_index.setSection(sidx, eidx, step, value)
        else:
            self.flat_index.setSection(key, value)
