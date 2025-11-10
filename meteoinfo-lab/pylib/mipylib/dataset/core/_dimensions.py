
from collections import OrderedDict


class Dimensions(OrderedDict):

    def __init__(self, **kw):
        super(OrderedDict, self).__init__(**kw)

    def __getattr__(self, key):
        try:
            return self[key]
        except KeyError:
            raise AttributeError("Has no dimension '{}'".format(key))

    def __setattr__(self, key, value):
        self[key] = value
