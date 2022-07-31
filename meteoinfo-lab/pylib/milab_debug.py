import sys
import os

toolbox_path = 'D:/MyProgram/Java/MeteoInfoDev/toolbox'
sys.path.append(toolbox_path)
sys.path.append(os.path.join(toolbox_path, 'miml_dev'))
sys.path.append(os.path.join(toolbox_path, 'EMIPS'))
sys.path.append(os.path.join(toolbox_path, 'IMEP'))
sys.path.append(os.path.join(toolbox_path, 'meteoview3d'))

mipylib.migl.mifolder = 'D:/MyProgram/Distribution/Java/MeteoInfo/MeteoInfo'