from .numeric import *
import numeric as np
import numeric.random as random
import numeric.linalg as linalg
from .plotlib import *
import plotlib as plt
from .geolib.migeo import *
import geolib.topology as topo
from .dataset import *
import meteolib as meteo
import imagelib
from dataframe import *
import enum

import os
mi_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
migl.mifolder = mi_dir

from org.meteoinfo.common.util import GlobalUtil
__version__ = GlobalUtil.getVersion()
pstr = 'MeteoInfoLab {}'.format(__version__)

lookup_cma = os.path.join(mi_dir, 'tables', 'bufr', 'tablelookup_cma.csv')
if os.path.isfile(lookup_cma):
    try:
        is_ok = dataset.add_bufr_lookup(lookup_cma)
    except:
        is_ok = False
    if is_ok:
        pstr += ' (CMA Bufr lookup file added)'

print(pstr)