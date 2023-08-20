
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
            return list(self.datainfo.getElevations())
        else:
            return list(self.datainfo.getElevations(product))

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
