
from .dimdatafile import DimDataFile


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
