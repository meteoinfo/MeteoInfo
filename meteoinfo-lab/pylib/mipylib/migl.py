# coding=utf-8
#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2017-8-4
# Purpose: MeteoInfo global module
# Note: Put global variables
#-----------------------------------------------------

import os

#MeteoInfoLab application object
milapp = None

#Current folder of MeteoInfoLab
currentfolder = None

#MeteoInfo folder
mifolder = None

interactive = False

#Map folder
def get_map_folder():
    return None if (mifolder is None) else os.path.join(mifolder, 'map')

#Sample folder
def get_sample_folder():
    return None if (mifolder is None) else os.path.join(mifolder, 'sample')
    
#cmap folder
def get_cmap_folder():
    return None if (mifolder is None) else os.path.join(mifolder, 'colormaps')