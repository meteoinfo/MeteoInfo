
from .dimdatafile import DimDataFile
import mipylib.miutil as miutil
import mipylib.numeric as np


class ARLDataFile(DimDataFile):

    def __init__(self, dataset=None, access='r', arldata=None):
        super(ARLDataFile, self).__init__(dataset, access)
        self.arldata = arldata

    # Write ARL data
    def setx(self, x):
        """
        Set x (longitude) dimension value.

        :param x: (*array_like*) X dimension value.
        """
        self.arldata.setX(x.aslist())

    def sety(self, y):
        """
        Set y (latitude) dimension value.

        :param y: (*array_like*) Y dimension value.
        """
        self.arldata.setY(y.aslist())

    def setlevels(self, levels, add_ground=True):
        """
        Set vertical levels.

        :param levels: (*list*) Vertical levels.
        :param add_ground: (*bool*) Whether add ground level. Default is `True`.
        """
        if isinstance(levels, np.NDArray):
            levels = levels.aslist()
        if add_ground:
            levels.insert(0, 1)
        self.arldata.levels = levels

    def set2dvar(self, vnames):
        """
        Set surface variables (2 dimensions ignore time dimension).

        :param vnames: (*list*) Variable names.
        """
        self.arldata.LevelVarList.add(vnames)

    def set3dvar(self, vnames):
        """
        Set level variables (3 dimensions ignore time dimension).

        :param vnames: (*list*) Variable names.
        """
        self.arldata.LevelVarList.add(vnames)

    def getdatahead(self, proj, model, vertical, icx=0, mn=0):
        """
        Get data head.

        :param proj: (*ProjectionInfo*) Projection information.
        :param model: (*string*) Model name with 4 characters.
        :param vertical: (*int*) Vertical coordinate system flag. 1-sigma (fraction);
            2-pressure (mb); 3-terrain (fraction); 4-hybrid (mb: offset.fraction)
        :param icx: (*int*) Forecast hour (>99 the header forecast hr = 99)
        :param mn: (*int*) Minutes associated with data time.
        """
        return self.arldata.getDataHead(proj, model, vertical, icx, mn)

    def diff_origin_pack(self, data):
        """
        Get difference between the original data and the packed data.
        :param data: (*array*) The original data.
        :return: (*array*) Difference.
        """
        r = self.arldata.diffOriginPack(data._array)
        return np.NDArray(r)

    def writeindexrec(self, t, datahead, ksums=None):
        """
        Write index record.

        :param t: (*datatime*) The time of the data.
        :param datahead: (*DataHeader') Data header of the record.
        :param ksums: (*list*) Check sum list.
        """
        t = miutil.jdate(t)
        self.arldata.writeIndexRecord(t, datahead, ksums)

    def writedatarec(self, t, lidx, vname, fhour, grid, data):
        """
        Write data record.

        :param t: (*datetime*) The time of the data.
        :param lidx: (*int*) Level index.
        :param vname: (*string*) Variable name.
        :param fhour: (*int*) Forecasting hour.
        :param grid: (*int*) Grid id to check if the data grid is bigger than 999. Header
            record does not support grids of more than 999, therefore in those situations
            the grid number is converted to character to represent the 1000s digit,
            e.g. @(64)=<1000, A(65)=1000, B(66)=2000, etc.
        :param data: (*array_like*) Data array.

        :returns: (*int*) Check sum of the record data.
        """
        t = miutil.jdate(t)
        ksum = self.arldata.writeGridData(t, lidx, vname, fhour, grid, data.asarray())
        return ksum