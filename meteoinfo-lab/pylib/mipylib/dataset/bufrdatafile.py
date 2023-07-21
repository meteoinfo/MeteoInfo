
from .dimdatafile import DimDataFile


class BUFRDataFile(DimDataFile):

    def __init__(self, dataset=None, access='r', bufrdata=None):
        super(BUFRDataFile, self).__init__(dataset, access)
        self.bufrdata = bufrdata

    def write_indicator(self, bufrlen, edition=3):
        """
        Write indicator section with arbitrary length.

        :param bufrlen: (*int*) The total length of the message.
        :param edition: (*int*) Bruf edition.

        :returns: (*int*) Indicator section length.
        """
        return self.bufrdata.writeIndicatorSection(bufrlen, edition)

    def rewrite_indicator(self, bufrlen, edition=3):
        """
        Write indicator section with correct length.

        :param bufrlen: (*int*) The total length of the message.
        :param edition: (*int*) Bruf edition.
        """
        self.bufrdata.reWriteIndicatorSection(bufrlen, edition)

    def write_identification(self, **kwargs):
        """
        Write identification section.

        :param length: (*int*) Section length
        :param master_table: (*int*) Master table
        :param subcenter_id: (*int*) Subcenter id
        :param center_id: (*int*) Center id
        :param update: (*int*) Update sequency
        :param optional: (*int*) Optional
        :param category: (*int*) Category
        :param sub_category: (*int*) Sub category
        :param master_table_version: (*int*) Master table version
        :param local_table_version: (*int*) Local table version
        :param year: (*int*) Year
        :param month: (*int*) Month
        :param day: (*int*) Day
        :param hour: (*int*) Hour
        :param minute: (*int*) Minute

        :returns: (*int*) Section length
        """
        length = kwargs.pop('length', 18)
        master_table = kwargs.pop('master_table', 0)
        subcenter_id = kwargs.pop('subcenter_id', 0)
        center_id = kwargs.pop('center_id', 74)
        update = kwargs.pop('update', 0)
        optional = kwargs.pop('optional', 0)
        category = kwargs.pop('category', 7)
        sub_category = kwargs.pop('sub_category', 0)
        master_table_version = kwargs.pop('master_table_version', 11)
        local_table_version = kwargs.pop('local_table_version', 1)
        year = kwargs.pop('year', 2016)
        month = kwargs.pop('month', 1)
        day = kwargs.pop('day', 1)
        hour = kwargs.pop('hour', 0)
        minute = kwargs.pop('minute', 0)
        return self.bufrdata.writeIdentificationSection(length, master_table, subcenter_id, center_id, \
                                                        update, optional, category, sub_category, master_table_version, \
                                                        local_table_version, year, month, day, hour, minute)

    def write_datadescription(self, n, datatype, descriptors):
        """
        Write data description section

        :param n: (*int*) Numer of dataset.
        :param datatype: (*int*) Data type.
        :param descriptors: (*list*) Data descriptors.
        """
        return self.bufrdata.writeDataDescriptionSection(n, datatype, descriptors)

    def write_datahead(self, len):
        """
        Write data header with arbitrary data length.

        :param len: (*int*) Data section length.

        :returns: (*int*) Data section head length - always 4.
        """
        return self.bufrdata.writeDataSectionHead(len)

    def rewrite_datahead(self, len):
        """
        Write data header with correct data length.

        :param len: (*int*) Data section length.
        """
        self.bufrdata.reWriteDataSectionHead(len)

    def write_data(self, value, nbits=None):
        """
        Write data.

        :param value: (*int*) Value.
        :param nbits: (*int*) Bit number.

        :returns: (*int*) Data value length.
        """
        return self.bufrdata.write(value, nbits)

    def write_end(self):
        """
        Write end section ('7777').

        :returns: (*int*) End section length - always 4.
        """
        return self.bufrdata.writeEndSection()