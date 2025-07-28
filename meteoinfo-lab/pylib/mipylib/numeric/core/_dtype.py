from org.meteoinfo.ndarray import DataType as JDataType

__all__ = [
    'byte','char','int','integer','int32','int16','short','int64','uint','long','float','float32','float64',
    'double','str','string','complex','date','datetime','timedelta'
    ]

_dtype_dict = dict(byte = JDataType.BYTE,
    char = JDataType.CHAR,
    bool = JDataType.BOOLEAN,
    boolean = JDataType.BOOLEAN,
    int = JDataType.INT,
    int32 = JDataType.INT,
    integer = JDataType.INT,
    uint = JDataType.UINT,
    short = JDataType.SHORT,
    int16 = JDataType.SHORT,
    long = JDataType.LONG,
    int64 = JDataType.LONG,
    float = JDataType.FLOAT,
    float32 = JDataType.FLOAT,
    double = JDataType.DOUBLE,
    float64 = JDataType.DOUBLE,
    str = JDataType.STRING,
    string = JDataType.STRING,
    complex = JDataType.COMPLEX,
    date = JDataType.DATE,
    datetime = JDataType.DATE,
    timedelta = JDataType.DURATION,
    object = JDataType.OBJECT)

class DataType(object):
    """
    Data type

    :param name: (*string*) Data type name
    :param byteorder: (*str*) Byte order. Default to '<'.
    """

    def __init__(self, name, byteorder='<'):
        if name is int:
            name = 'int'
        elif name is float:
            name = 'float'
        elif name is bool:
            name = 'bool'
        self.name = name
        if _dtype_dict.has_key(name):
            self._dtype = _dtype_dict[name]
        else:
            self._dtype = JDataType.OBJECT
        self._byteorder=byteorder

    @property
    def itemsize(self):
        """
        The element size of this data-type object.
        :return: (*int*) size
        """
        return self._dtype.getSize()

    @property
    def byteorder(self):
        """
        A character indicating the byte-order of this data-type object.
        :return: (*str*) Byte order.
        """
        return self._byteorder

    @byteorder.setter
    def byteorder(self, val):
        """
        Set byte order.
        :param val: (*str*) Byte order ['<' | '>'].
        """
        self._byteorder = val

    @property
    def num(self):
        """
        A unique number for each of the different built-in types.
        These are roughly ordered from least-to-most precision.

        :return: (*int*) number
        """
        return self._dtype.getNumber()

    def __str__(self):
        return "dtype('{}')".format(self.name)

    def __repr__(self):
        return "dtype('{}')".format(self.name)

    def __eq__(self, other):
        return self.num == other.num

    def __ne__(self, other):
        return self.num != other.num

    def __ge__(self, other):
        return self.num >= other.num

    def __le__(self, other):
        return self.num <= other.num

    def __gt__(self, other):
        return self.num > other.num

    def __lt__(self, other):
        return self.num < other.num

    @property
    def char(self):
        """
        Get A unique character code for each of the 21 different built-in types.

        :return: (*str*) Character code
        """
        if self.name == 'bool' or self.name == 'boolean':
            return 'b'
        elif self.name == 'int16' or self.name == 'short':
            return 'h'
        elif self.name == 'int' or self.name == 'integer' or self.name == 'int32' or self.name == 'uint':
            return 'l'
        elif self.name == 'int64' or self.name == 'long':
            return 'q'
        elif self.name == 'float' or self.name == 'float32':
            return 'f'
        elif self.name == 'float64' or self.name == 'double':
            return 'd'
        elif self.name == 'str' or self.name == 'string':
            return 'U'
        elif self.name == 'complex':
            return 'D'
        elif self.name == 'date' or self.name == 'datetime':
            return 'M'

    @property
    def kind(self):
        """
        Get kind property

        :return: kind string
        """
        if self._dtype == JDataType.BOOLEAN:
            return 'b'
        elif self._dtype in (JDataType.SHORT, JDataType.INT, JDataType.LONG):
            return 'i'
        elif self._dtype == JDataType.UINT:
            return 'u'
        elif self._dtype in (JDataType.FLOAT, JDataType.DOUBLE):
            return 'f'
        elif self._dtype == JDataType.STRING:
            return 'S'
        elif self._dtype == JDataType.COMPLEX:
            return 'c'
        elif self._dtype == JDataType.DATE:
            return 'M'
        elif self._dtype == JDataType.DURATION:
            return 'm'
        elif self._dtype == JDataType.OBJECT:
            return 'O'

    @staticmethod
    def from_char(c):
        """
        Create DataType from char.
        :param c: (*str*) Char string.
        :return: The DataType
        """
        if c == 'b' or c == 'bool':
            return dtype.bool
        elif c == 'h':
            return dtype.short
        elif c == 'l' or c == 'int':
            return DataType('int')
        elif c == 'q':
            return DataType('long')
        elif c == 'f' or c == 'float':
            return DataType('float')
        elif c == 'd':
            return DataType('double')
        elif c == 'U':
            return DataType('string')
        elif c == 'D':
            return DataType('complex')
        elif c == 'M':
            return DataType('date')
        elif c == 'm':
            return DataType('timedelta')
        else:
            return DataType('object')

    @staticmethod
    def from_kind(k):
        """
        Create DataType from kind.
        :param k: (*str*) Kind string.
        :return: The DataType
        """
        if k == 'b':
            return dtype.bool
        elif k == 'i':
            return dtype.int
        elif k == 'u':
            return dtype.uint
        elif k == 'f':
            return dtype.float
        elif k == 'S':
            return dtype.string
        elif k == 'c':
            return dtype.complex
        elif k == 'M':
            return dtype.date
        elif k == 'm':
            return dtype.timedelta
        else:
            return dtype.obj

    def is_numeric(self):
        """
        Is numeric data type or not.

        :return: (*bool*) Is numeric data type or not.
        """
        return self._dtype.isNumeric()


class dtype(DataType):
    byte = DataType('byte')
    char = DataType('char')
    bool = DataType('boolean')
    boolean = DataType('boolean')
    int = DataType('int')
    uint = DataType('uint')
    integer = DataType('int')
    int32 = DataType('int')
    int16 = DataType('short')
    short = DataType('short')
    int64 = DataType('long')
    long = DataType('long')
    float = DataType('float')
    float32 = DataType('float')
    float64 = DataType('double')
    double = DataType('double')
    str = DataType('string')
    string = DataType('string')
    complex = DataType('complex')
    date = DataType('date')
    datetime = DataType('date')
    timedelta = DataType('timedelta')
    obj = DataType('object')
    
    def __init__(self, name):
        if isinstance(name, DataType):
            DataType.__init__(self, name.name)
        else:
            DataType.__init__(self, name)

    @staticmethod
    def fromjava(dt):
        """
        Convert Java data type to Python data type.

        :param dt: (*JDataType*) Java data type

        :returns: Python data type
        """
        return DataType(dt.toString().lower())


byte = DataType('byte')
char = DataType('char')
bool = DataType('boolean')
boolean = DataType('boolean')
int = DataType('int')
integer = DataType('int')
int32 = DataType('int')
int16 = DataType('short')
short = DataType('short')
int64 = DataType('long')
uint = DataType('uint')
long = DataType('long')
float = DataType('float')
float32 = DataType('float')
float64 = DataType('double')
double = DataType('double')
str = DataType('string')
string = DataType('string')
complex = DataType('complex')
date = DataType('date')
datetime = DataType('date')
timedelta = DataType('timedelta')
obj = DataType('object')

def fromjava(dt):
    """
    Convert Java data type to Python data type.

    :param dt: (*JDataType*) Java data type

    :returns: Python data type
    """
    return DataType(dt.toString().lower())