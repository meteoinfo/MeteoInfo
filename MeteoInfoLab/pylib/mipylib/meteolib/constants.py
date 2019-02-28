#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2018-11-23
# Purpose: MeteoInfoLab constants module
# Note: Jython
#-----------------------------------------------------

P0 = 1000.          #reference pressure for potential temperature (hPa)
R = 8.3144598       #molar gas constant (J / K / mol)
Mw = 18.01528       #Molecular weight of water (g / mol)
Md = 28.9644        #Nominal molecular weight of dry air at the surface of th Earth (g / mol)
Rd = R / Md         #Gas constant for dry air at the surface of the Earth (J (K kg)^-1)
Lv = 2.501e6        #Latent heat of vaporization for liquid water at 0C (J kg^-1)
Cp_d = 1005         #Specific heat at constant pressure for dry air (J kg^-1)
epsilon = Mw / Md
kappa = 0.286
degCtoK = 273.15    # Temperature offset between K and C (deg C)
g = 9.8             # Gravitational acceleration (m / s^2)
sat_pressure_0c = 6.112  #Saturation presssure at 0 degree (hPa)
T_BASE = 300.