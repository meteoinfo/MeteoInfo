package org.meteoinfo.math;

public class Constants {
    public static double P0 = 1000.;          //reference pressure for potential temperature (hPa)
    public static double R = 8.3144598;       //molar gas constant (J / K / mol)
    public static double Mw = 18.01528;       //Molecular weight of water (g / mol)
    public static double Md = 28.9644;        //Nominal molecular weight of dry air at the surface of th Earth (g / mol)
    public static double Rd = R / Md;         //Gas constant for dry air at the surface of the Earth (J (K kg)^-1)
    public static double Lv = 2.501e6;        //Latent heat of vaporization for liquid water at 0C (J kg^-1)
    public static double Cp_d = 1005;         //Specific heat at constant pressure for dry air (J kg^-1)
    public static double epsilon = Mw / Md;
    public static double kappa = 0.286;
    public static double degCtoK = 273.15;    //Temperature offset between K and C (deg C)
    public static double g = 9.8;             //Gravitational acceleration (m / s^2)
    public static double sat_pressure_0c = 6.112;  //Saturation pressure at 0 degree (hPa)
    public static double T_BASE = 300.;
    public static double omega = 7292115e-11;  //Avg. angular velocity of Earth (rad / s)
}
