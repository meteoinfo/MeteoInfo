from .numeric import *
import numeric as np
import numeric.random as random
import numeric.linalg as linalg
from .geolib.migeo import *
import geolib.topology as topo
from .dataset import *
from .plotlib import *
import plotlib as plt
import meteolib as meteo
import imagelib
from dataframe import *
import enum

import os
mi_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
migl.mifolder = mi_dir

lookup_cma = os.path.join(mi_dir, 'tables', 'bufr', 'tablelookup_cma.csv')
if os.path.isfile(lookup_cma):
    try:
        is_ok = dataset.add_bufr_lookup(lookup_cma)
    except:
        is_ok = False
    if is_ok:
        print('CMA Bufr lookup file added.')