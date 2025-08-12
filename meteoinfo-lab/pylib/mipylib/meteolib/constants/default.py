
# Earth
Re = earth_avg_radius = 6371008.7714    # Earth average radius with meters
earth_gravity = g = 9.80665    #Gravitational acceleration (m / s^2)
G = gravitational_constant = 6.67430e-11    #(m^3 / kg / s^2)
GM = geocentric_gravitational_constant = 3986005e8    #(m^3 / s^2)
omega = 7292115e-11  # Avg. angular velocity of Earth (rad / s)
d = earth_sfc_avg_dist_sun = 149597870700.    #(m)
S = earth_solar_irradiance = 1360.8    #(W / m^2)
delta = earth_max_declination = 23.45    #(degrees)
earth_orbit_eccentricity = 0.0167    #(dimensionless)
earth_mass = me = geocentric_gravitational_constant / gravitational_constant

# molar gas constant
R = 8.314462618       #molar gas constant (J / K / mol)

# Water
Mw = 18.015268       #Molecular weight of water (g / mol)
Rv = water_gas_constant = R / Mw * 1000   #(J / kg / K)
rho_l = density_water = 999.97495    #(kg / m^3)
wv_specific_heat_ratio = 1.330    #(dimensionless)
Cp_v = wv_specific_heat_press = wv_specific_heat_ratio * Rv / (wv_specific_heat_ratio - 1)
Cv_v = wv_specific_heat_vol = Cp_v / wv_specific_heat_ratio
Cp_l = water_specific_heat = 4219.4    #(J / kg / K)
Lv = water_heat_vaporization = 2.50084e6     #Latent heat of vaporization for liquid water at 0C (J kg^-1)
Lf = water_heat_fusion = 3.337e5    #(J / kg)
Ls = water_heat_sublimation = Lv + Lf
Cp_i = ice_specific_heat = 2090    #(J / kg / K)
rho_i = density_ice = 917    #(kg / m^3)
sat_pressure_0c = 6.112  #Saturation pressure at 0 degree (hPa)
T0 = water_triple_point_temperature = 273.16    #(K)

# Dry air
Md = dry_air_molecular_weight = 28.96546e-3    #(kg / mol)
Rd = dry_air_gas_constant = R / Md     #Gas constant for dry air at the surface of the Earth (J (K kg)^-1)
dry_air_spec_heat_ratio = 1.4    #(dimensionless)
Cp_d = dry_air_spec_heat_press = dry_air_spec_heat_ratio * Rd / (dry_air_spec_heat_ratio - 1)
Cv_d = dry_air_spec_heat_vol = Cp_d / dry_air_spec_heat_ratio
degCtoK = 273.15    # Temperature offset between K and C (deg C)
rho_d = dry_air_density_stp = 1000. / (Rd * degCtoK)

# General meteorology constants
P0 = 1000.          #reference pressure for potential temperature (hPa)
kappa = poisson_exponent = Rd / Cp_d
gamma_d = dry_adiabatic_lapse_rate = g / Cp_d
epsilon = Mw / Md / 1000.
T_BASE = 300.
