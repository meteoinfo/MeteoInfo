
from collections import OrderedDict


class Coordinates(OrderedDict):

    def __init__(self, **kw):
        super(OrderedDict, self).__init__(**kw)

    def __getattr__(self, key):
        try:
            return self[key]
        except KeyError:
            raise AttributeError("Has no coordinates '{}'".format(key))

    def __setattr__(self, key, value):
        self[key] = value
