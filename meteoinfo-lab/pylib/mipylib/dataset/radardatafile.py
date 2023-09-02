
from .dimdatafile import DimDataFile
import mipylib.numeric as np


class RadarDataFile(DimDataFile):

    def __init__(self, dataset=None, access='r'):
        super(RadarDataFile, self).__init__(dataset, access)
        self.datainfo = dataset.getDataInfo()

    def get_products(self):
        """
        Get product names.

        :return: Product names.
        """
        return list(self.datainfo.getProducts())

    def get_elevations(self, product=None):
        """
        Get scan elevation angles.

        :param product: (*Str*) Product name. Default is None.

        :return: Scan elevation angles.
        """
        if product is None:
            return np.array(self.datainfo.getElevations())
        else:
            return np.array(self.datainfo.getElevations(product))

    def get_grid_ppi(self, product, scan, x, y, h=None):
        """
        Get grid ppi data.

        :param product: (*str*) Product name.
        :param scan: (*int*) Scan index.
        :param x: (*array*) X coordinates with meters units.
        :param y: (*array*) Y coordinates with meters units.
        :param h: (*float*) Radar height with meters units.

        :return: (*array*) Grid ppi data.
        """
        if (x.ndim == 1):
            x, y = np.meshgrid(x, y)

        r = self.datainfo.readGridData(product, scan, x._array, y._array, h)
        return np.array(r)

    def get_cr_data(self, product, x, y, h=None):
        """
        Get grid cr data.

        :param product: (*str*) Product name.
        :param x: (*array*) X coordinates with meters units.
        :param y: (*array*) Y coordinates with meters units.
        :param h: (*float*) Radar height with meters units.

        :return: (*array*) Grid cr data.
        """
        if (x.ndim == 1):
            x, y = np.meshgrid(x, y)

        r = self.datainfo.getCRData(product, x._array, y._array, h)
        return np.array(r)

    def get_cappi_data(self, product, x, y, z, h=None):
        """
        Get cappi grid data.

        :param product: (*str*) Product name.
        :param x: (*array*) X coordinates with meters units.
        :param y: (*array*) Y coordinates with meters units.
        :param z: (*float*) Z coordinates value with meters units.
        :param h: (*float*) Radar height with meters units.

        :return: (*array*) cappi data.
        """
        if (x.ndim == 1):
            x, y = np.meshgrid(x, y)

        r = self.datainfo.getCAPPIData(product, x._array, y._array, z, h)
        return np.array(r)

    def get_grid_3d_data(self, product, x, y, z, h=None):
        """
        Get grid 3d data.

        :param product: (*str*) Product name.
        :param x: (*array*) X coordinates with meters units - 1D.
        :param y: (*array*) Y coordinates with meters units - 1D.
        :param z: (*array*) Z coordinates with meters units - 1D.
        :param h: (*float*) Radar height with meters units.

        :return: (*array*) Grid 3d data.
        """
        if x.ndim == 1:
            x, y = np.meshgrid(x, y)

        r = self.datainfo.getGrid3DData(product, x._array, y._array, z._array, h)
        return np.array(r)

    def get_vcs_data(self, product, start_point, end_point):
        """
        Get VCS data.

        :param product: (*str*) Product name.
        :param start_point: (*tuple of float*) Start point x/y coordinates with km units.
        :param end_point: (*tuple of float*) End point x/y coordinates with km units.

        :return: VCS data arrays: X/Y mesh, Z mesh, product data.
        """
        r = self.datainfo.getVCSData(product, start_point[0], start_point[1], end_point[0],
                                     end_point[1])

        return np.array(r[0]), np.array(r[1]), np.array(r[2])
