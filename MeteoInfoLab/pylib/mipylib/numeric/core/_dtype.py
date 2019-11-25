from org.meteoinfo.ndarray import DataType as JDataType

__all__ = [
    'byte','char','int','integer','int32','int16','short','int64','long','float','float64',
    'double','str','string','complex','date','datetime'
    ]

_dtype_dict = dict(byte = JDataType.BYTE,
    char = JDataType.CHAR,
    boolean = JDataType.BOOLEAN,
    int = JDataType.INT,
    short = JDataType.SHORT,
    long = JDataType.LONG,
    float = JDataType.FLOAT,
    double = JDataType.DOUBLE,
    string = JDataType.STRING,
    complex = JDataType.COMPLEX,
    date = JDataType.DATE,
    object = JDataType.OBJECT)

class DataType(object):
    '''
    Data type

    :param name: (*string*) Data type name
    '''

    def __init__(self, name):
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

    def __str__(self):
        return self.name

    def __repr__(self):
        return self.name

    @property
    def kind(self):
        '''
        Get kind property

        :return: kind string
        '''
        if self.name == 'bool' or self.name == 'boolean':
            return 'b'
        elif self.name == 'int' or self.name == 'integer' or self.name == 'int32' or self.name == 'int16' or \
            self.name == 'short' or self.name == 'int64' or self.name == 'long':
            return 'i'
        elif self.name == 'float' or self.name == 'float64' or self.name == 'double':
            return 'f'
        elif self.name == 'str' or self.name == 'string':
            return 'S'
        elif self.name == 'complex':
            return 'c'
        elif self.name == 'date' or self.name == 'datetime':
            return 'M'


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
long = DataType('long')
float = DataType('float')
float64 = DataType('double')
double = DataType('double')
str = DataType('string')
string = DataType('string')
complex = DataType('complex')
date = DataType('date')
datetime = DataType('date')
obj = DataType('object')

def fromjava(dt):
    '''
    Convert Java data type to Python data type.

    :param dt: (*JDataType*) Java data type

    :returns: Python data type
    '''
    return DataType(dt.toString().lower())