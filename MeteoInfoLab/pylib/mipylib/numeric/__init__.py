import miarray
import dimarray
import minum
from .minum import *
from . import linalg
from . import random
from . import fitting
from . import stats
from . import interpolate
from stats import percentile

__all__ = []
__all__ += minum.__all__
__all__ += ['percentile']