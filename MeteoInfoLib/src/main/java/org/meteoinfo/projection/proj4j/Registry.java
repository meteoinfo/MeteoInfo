package org.meteoinfo.projection.proj4j;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meteoinfo.projection.proj4j.datum.Datum;
import org.meteoinfo.projection.proj4j.datum.Ellipsoid;
import org.meteoinfo.projection.proj4j.proj.*;

/**
 * Supplies predefined values for various library classes such as
 * {@link Ellipsoid}, {@link Datum}, and {@link Projection}.
 *
 * @author Martin Davis
 *
 */
public class Registry {

    public Registry() {
        super();
        initialize();
    }
    public final static Datum[] datums = {
        Datum.WGS84,
        Datum.GGRS87,
        Datum.NAD27,
        Datum.NAD83,
        Datum.POTSDAM,
        Datum.CARTHAGE,
        Datum.HERMANNSKOGEL,
        Datum.IRE65,
        Datum.NZGD49,
        Datum.OSEB36
    };

    public Datum getDatum(String code) {
        for (int i = 0; i < datums.length; i++) {
            if (datums[i].getCode().equals(code)) {
                return datums[i];
            }
        }
        return null;
    }
    public final static Ellipsoid[] ellipsoids = {
        Ellipsoid.SPHERE,
        new Ellipsoid("MERIT", 6378137.0, 0.0, 298.257, "MERIT 1983"),
        new Ellipsoid("SGS85", 6378136.0, 0.0, 298.257, "Soviet Geodetic System 85"),
        Ellipsoid.GRS80,
        new Ellipsoid("IAU76", 6378140.0, 0.0, 298.257, "IAU 1976"),
        Ellipsoid.AIRY,
        Ellipsoid.MOD_AIRY,
        new Ellipsoid("APL4.9", 6378137.0, 0.0, 298.25, "Appl. Physics. 1965"),
        new Ellipsoid("NWL9D", 6378145.0, 298.25, 0.0, "Naval Weapons Lab., 1965"),
        new Ellipsoid("andrae", 6377104.43, 300.0, 0.0, "Andrae 1876 (Den., Iclnd.)"),
        new Ellipsoid("aust_SA", 6378160.0, 0.0, 298.25, "Australian Natl & S. Amer. 1969"),
        new Ellipsoid("GRS67", 6378160.0, 0.0, 298.2471674270, "GRS 67 (IUGG 1967)"),
        Ellipsoid.BESSEL,
        new Ellipsoid("bess_nam", 6377483.865, 0.0, 299.1528128, "Bessel 1841 (Namibia)"),
        Ellipsoid.CLARKE_1866,
        Ellipsoid.CLARKE_1880,
        new Ellipsoid("CPM", 6375738.7, 0.0, 334.29, "Comm. des Poids et Mesures 1799"),
        new Ellipsoid("delmbr", 6376428.0, 0.0, 311.5, "Delambre 1810 (Belgium)"),
        new Ellipsoid("engelis", 6378136.05, 0.0, 298.2566, "Engelis 1985"),
        Ellipsoid.EVEREST,
        new Ellipsoid("evrst48", 6377304.063, 0.0, 300.8017, "Everest 1948"),
        new Ellipsoid("evrst56", 6377301.243, 0.0, 300.8017, "Everest 1956"),
        new Ellipsoid("evrst69", 6377295.664, 0.0, 300.8017, "Everest 1969"),
        new Ellipsoid("evrstSS", 6377298.556, 0.0, 300.8017, "Everest (Sabah & Sarawak)"),
        new Ellipsoid("fschr60", 6378166.0, 0.0, 298.3, "Fischer (Mercury Datum) 1960"),
        new Ellipsoid("fschr60m", 6378155.0, 0.0, 298.3, "Modified Fischer 1960"),
        new Ellipsoid("fschr68", 6378150.0, 0.0, 298.3, "Fischer 1968"),
        new Ellipsoid("helmert", 6378200.0, 0.0, 298.3, "Helmert 1906"),
        new Ellipsoid("hough", 6378270.0, 0.0, 297.0, "Hough"),
        Ellipsoid.INTERNATIONAL,
        Ellipsoid.INTERNATIONAL_1967,
        Ellipsoid.KRASSOVSKY,
        new Ellipsoid("kaula", 6378163.0, 0.0, 298.24, "Kaula 1961"),
        new Ellipsoid("lerch", 6378139.0, 0.0, 298.257, "Lerch 1979"),
        new Ellipsoid("mprts", 6397300.0, 0.0, 191.0, "Maupertius 1738"),
        new Ellipsoid("plessis", 6376523.0, 6355863.0, 0.0, "Plessis 1817 France)"),
        new Ellipsoid("SEasia", 6378155.0, 6356773.3205, 0.0, "Southeast Asia"),
        new Ellipsoid("walbeck", 6376896.0, 6355834.8467, 0.0, "Walbeck"),
        Ellipsoid.WGS60,
        Ellipsoid.WGS66,
        Ellipsoid.WGS72,
        Ellipsoid.WGS84,
        new Ellipsoid("NAD27", 6378249.145, 0.0, 293.4663, "NAD27: Clarke 1880 mod."),
        new Ellipsoid("NAD83", 6378137.0, 0.0, 298.257222101, "NAD83: GRS 1980 (IUGG, 1980)"),};

    public Ellipsoid getEllipsoid(String name) {
        for (int i = 0; i < ellipsoids.length; i++) {
            if (ellipsoids[i].shortName.equals(name)) {
                return ellipsoids[i];
            }
        }
        return null;
    }
    private Map<String, Class> projRegistry;
    private Map projRegistryEsri;

    private void register(String name, Class cls, String description) {
        projRegistry.put(name, cls);
    }

    private void registerEsri(String name, Class cls, String description) {
        projRegistryEsri.put(name, cls);
    }

    public Projection getProjection(String name) {
//    if ( projRegistry == null )
//      initialize();
        Class cls = (Class) projRegistry.get(name);
        if (cls != null) {
            try {
                Projection projection = (Projection) cls.getDeclaredConstructor().newInstance();
//                if (projection != null) {
//                    projection.setName(name);
//                }
                return projection;
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(Registry.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public Projection getProjectionEsri(String name) {
//    if ( projRegistry == null )
//      initialize();
        Class cls = (Class) projRegistryEsri.get(name);
        if (cls != null) {
            try {
                Projection projection = (Projection) cls.getDeclaredConstructor().newInstance();
                if (projection != null) {
                    projection.setName(name);
                }
                return projection;
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(Registry.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    private synchronized void initialize() {
        initializeProj4();
        initializeEsri();
    }

    private synchronized void initializeProj4() {
        // guard against race condition
        if (projRegistry != null) {
            return;
        }
        projRegistry = new HashMap();
        register("aea", AlbersProjection.class, "Albers Equal Area");
        register("aeqd", EquidistantAzimuthalProjection.class, "Azimuthal Equidistant");
        register("airy", AiryProjection.class, "Airy");
        register("aitoff", AitoffProjection.class, "Aitoff");
        //register( "alsk", Projection.class, "Mod. Stereographics of Alaska" );
        //register( "apian", Projection.class, "Apian Globular I" );
        register("august", AugustProjection.class, "August Epicycloidal");
        //register( "bacon", Projection.class, "Bacon Globular" );
        register("bipc", BipolarProjection.class, "Bipolar conic of western hemisphere");
        register("boggs", BoggsProjection.class, "Boggs Eumorphic");
        register("bonne", BonneProjection.class, "Bonne (Werner lat_1=90)");
        register("cass", CassiniProjection.class, "Cassini");
        register("cc", CentralCylindricalProjection.class, "Central Cylindrical");
        register("cea", CylindricalEqualAreaProjection.class, "Equal Area Cylindrical");
//    register( "chamb", Projection.class, "Chamberlin Trimetric" );
        register("collg", CollignonProjection.class, "Collignon");
        register("crast", CrasterProjection.class, "Craster Parabolic (Putnins P4)");
        register("denoy", DenoyerProjection.class, "Denoyer Semi-Elliptical");
        register("eck1", Eckert1Projection.class, "Eckert I");
        register("eck2", Eckert2Projection.class, "Eckert II");
//    register( "eck3", Eckert3Projection.class, "Eckert III" );
        register("eck4", Eckert4Projection.class, "Eckert IV");
        register("eck5", Eckert5Projection.class, "Eckert V");
        register("eck6", Eckert6Projection.class, "Eckert VI");
        register("eqc", PlateCarreeProjection.class, "Equidistant Cylindrical (Plate Caree)");
        register("eqdc", EquidistantConicProjection.class, "Equidistant Conic");
        register("euler", EulerProjection.class, "Euler");
        register("fahey", FaheyProjection.class, "Fahey");
        register("fouc", FoucautProjection.class, "Foucaut");
        register("fouc_s", FoucautSinusoidalProjection.class, "Foucaut Sinusoidal");
        register("gall", GallProjection.class, "Gall (Gall Stereographic)");
        register("geos", GeostationarySatelliteProjection.class, "Geostationary Satellite");
//    register( "gins8", Projection.class, "Ginsburg VIII (TsNIIGAiK)" );
//    register( "gn_sinu", Projection.class, "General Sinusoidal Series" );
        register("gnom", GnomonicAzimuthalProjection.class, "Gnomonic");
        register("goode", GoodeProjection.class, "Goode Homolosine");
//    register( "gs48", Projection.class, "Mod. Stererographics of 48 U.S." );
//    register( "gs50", Projection.class, "Mod. Stererographics of 50 U.S." );
        register("hammer", HammerProjection.class, "Hammer & Eckert-Greifendorff");
        register("hatano", HatanoProjection.class, "Hatano Asymmetrical Equal Area");
//    register( "imw_p", Projection.class, "Internation Map of the World Polyconic" );
        register("kav5", KavraiskyVProjection.class, "Kavraisky V");
//    register( "kav7", Projection.class, "Kavraisky VII" );
//    register( "labrd", Projection.class, "Laborde" );
        register("laea", LambertAzimuthalEqualAreaProjection.class, "Lambert Azimuthal Equal Area");
        register("lagrng", LagrangeProjection.class, "Lagrange");
        register("larr", LarriveeProjection.class, "Larrivee");
        register("lask", LaskowskiProjection.class, "Laskowski");
        register("latlong", LongLatProjection.class, "Lat/Long");
        register("longlat", LongLatProjection.class, "Lat/Long");
        register("lcc", LambertConformalConicProjection.class, "Lambert Conformal Conic");
        register("leac", LambertEqualAreaConicProjection.class, "Lambert Equal Area Conic");
//    register( "lee_os", Projection.class, "Lee Oblated Stereographic" );
        register("loxim", LoximuthalProjection.class, "Loximuthal");
        register("lsat", LandsatProjection.class, "Space oblique for LANDSAT");
        register("mbt_s", McBrydeThomasFlatPolarSine1Projection.class, "McBryde-Thomas Flat-Polar Sine");
        register("mbt_fps", McBrydeThomasFlatPolarSine2Projection.class, "McBryde-Thomas Flat-Pole Sine (No. 2)");
        register("mbtfpp", McBrydeThomasFlatPolarParabolicProjection.class, "McBride-Thomas Flat-Polar Parabolic");
        register("mbtfpq", McBrydeThomasFlatPolarQuarticProjection.class, "McBryde-Thomas Flat-Polar Quartic");
//    register( "mbtfps", Projection.class, "McBryde-Thomas Flat-Polar Sinusoidal" );
        register("merc", MercatorProjection.class, "Mercator");
//    register( "mil_os", Projection.class, "Miller Oblated Stereographic" );
        register("mill", MillerProjection.class, "Miller Cylindrical");
//    register( "mpoly", Projection.class, "Modified Polyconic" );
        register("moll", MolleweideProjection.class, "Mollweide");
        register("murd1", Murdoch1Projection.class, "Murdoch I");
        register("murd2", Murdoch2Projection.class, "Murdoch II");
        register("murd3", Murdoch3Projection.class, "Murdoch III");
        register("nell", NellProjection.class, "Nell");
//    register( "nell_h", Projection.class, "Nell-Hammer" );
        register("nicol", NicolosiProjection.class, "Nicolosi Globular");
        register("nsper", PerspectiveProjection.class, "Near-sided perspective");
//    register( "nzmg", Projection.class, "New Zealand Map Grid" );
//    register( "ob_tran", Projection.class, "General Oblique Transformation" );
//    register( "ocea", Projection.class, "Oblique Cylindrical Equal Area" );
//    register( "oea", Projection.class, "Oblated Equal Area" );
        register("omerc", ObliqueMercatorProjection.class, "Oblique Mercator");
//    register( "ortel", Projection.class, "Ortelius Oval" );
        register("ortho", OrthographicAzimuthalProjection.class, "Orthographic");
        register("pconic", PerspectiveConicProjection.class, "Perspective Conic");
        register("poly", PolyconicProjection.class, "Polyconic (American)");
//    register( "putp1", Projection.class, "Putnins P1" );
        register("putp2", PutninsP2Projection.class, "Putnins P2");
//    register( "putp3", Projection.class, "Putnins P3" );
//    register( "putp3p", Projection.class, "Putnins P3'" );
        register("putp4p", PutninsP4Projection.class, "Putnins P4'");
        register("putp5", PutninsP5Projection.class, "Putnins P5");
        register("putp5p", PutninsP5PProjection.class, "Putnins P5'");
//    register( "putp6", Projection.class, "Putnins P6" );
//    register( "putp6p", Projection.class, "Putnins P6'" );
        register("qua_aut", QuarticAuthalicProjection.class, "Quartic Authalic");
        register("robin", RobinsonProjection.class, "Robinson");
        register("rpoly", RectangularPolyconicProjection.class, "Rectangular Polyconic");
        register("sinu", SinusoidalProjection.class, "Sinusoidal (Sanson-Flamsteed)");
        register("somerc", SwissObliqueMercatorProjection.class, "Swiss Oblique Mercator");
        register("stere", StereographicAzimuthalProjection.class, "Stereographic");
        register("sterea", ObliqueStereographicAlternativeProjection.class, "Oblique Stereographic Alternative");
        register("tcc", TranverseCentralCylindricalProjection.class, "Transverse Central Cylindrical");
        register("tcea", TransverseCylindricalEqualArea.class, "Transverse Cylindrical Equal Area");
//    register( "tissot", TissotProjection.class, "Tissot Conic" );
        register("tmerc", TransverseMercatorProjection.class, "Transverse Mercator");
//    register( "tpeqd", Projection.class, "Two Point Equidistant" );
//    register( "tpers", Projection.class, "Tilted perspective" );
//    register( "ups", Projection.class, "Universal Polar Stereographic" );
//    register( "urm5", Projection.class, "Urmaev V" );
        register("urmfps", UrmaevFlatPolarSinusoidalProjection.class, "Urmaev Flat-Polar Sinusoidal");
        register("utm", TransverseMercatorProjection.class, "Universal Transverse Mercator (UTM)");
        register("vandg", VanDerGrintenProjection.class, "van der Grinten (I)");
//    register( "vandg2", Projection.class, "van der Grinten II" );
//    register( "vandg3", Projection.class, "van der Grinten III" );
//    register( "vandg4", Projection.class, "van der Grinten IV" );
        register("vitk1", VitkovskyProjection.class, "Vitkovsky I");
        register("wag1", Wagner1Projection.class, "Wagner I (Kavraisky VI)");
        register("wag2", Wagner2Projection.class, "Wagner II");
        register("wag3", Wagner3Projection.class, "Wagner III");
        register("wag4", Wagner4Projection.class, "Wagner IV");
        register("wag5", Wagner5Projection.class, "Wagner V");
//    register( "wag6", Projection.class, "Wagner VI" );
        register("wag7", Wagner7Projection.class, "Wagner VII");
        register("weren", WerenskioldProjection.class, "Werenskiold I");
//    register( "wink1", Projection.class, "Winkel I" );
//    register( "wink2", Projection.class, "Winkel II" );
        register("wintri", WinkelTripelProjection.class, "Winkel Tripel");
    }

    private synchronized void initializeEsri() {
        // guard against race condition
        if (projRegistryEsri != null) {
            return;
        }
        projRegistryEsri = new HashMap();
        registerEsri("Albers", AlbersProjection.class, "Albers Equal Area");
        registerEsri("Azimuthal_Equidistant", EquidistantAzimuthalProjection.class, "Azimuthal Equidistant");
        registerEsri("Airy", AiryProjection.class, "Airy");
        registerEsri("Aitoff", AitoffProjection.class, "Aitoff");
        //registerEsri( "Alaska_Albers_Equal_Area_Conic", Projection.class, "Mod. Stereographics of Alaska" );
        //registerEsri( "apian", Projection.class, "Apian Globular I" );
        registerEsri("August", AugustProjection.class, "August Epicycloidal");
        //registerEsri( "bacon", Projection.class, "Bacon Globular" );
        registerEsri("Bipolar_Oblique_Conformal_Conic", BipolarProjection.class, "Bipolar conic of western hemisphere");
        registerEsri("Boggs", BoggsProjection.class, "Boggs Eumorphic");
        registerEsri("Bonne", BonneProjection.class, "Bonne (Werner lat_1=90)");
        registerEsri("Cassini", CassiniProjection.class, "Cassini");
        registerEsri("Central_Cylindrical", CentralCylindricalProjection.class, "Central Cylindrical");
        registerEsri("Cylindrical_Equal_Area", CylindricalEqualAreaProjection.class, "Equal Area Cylindrical");
//    registerEsri( "chamb", Projection.class, "Chamberlin Trimetric" );
        registerEsri("Collignon", CollignonProjection.class, "Collignon");
        registerEsri("Craster_Parabolic", CrasterProjection.class, "Craster Parabolic (Putnins P4)");
        registerEsri("Denoyer_Semi", DenoyerProjection.class, "Denoyer Semi-Elliptical");
        registerEsri("Eckert_I", Eckert1Projection.class, "Eckert I");
        registerEsri("Eckert_II", Eckert2Projection.class, "Eckert II");
//    registerEsri( "eck3", Eckert3Projection.class, "Eckert III" );
        registerEsri("Eckert_II", Eckert4Projection.class, "Eckert IV");
        registerEsri("Eckert_V", Eckert5Projection.class, "Eckert V");
        registerEsri("Eckert_VI", Eckert6Projection.class, "Eckert VI");
        registerEsri("Equidistant_Cylindrical", PlateCarreeProjection.class, "Equidistant Cylindrical (Plate Caree)");
        registerEsri("Equidistant_Conic", EquidistantConicProjection.class, "Equidistant Conic");
        registerEsri("Euler", EulerProjection.class, "Euler");
        registerEsri("Fahey", FaheyProjection.class, "Fahey");
        registerEsri("Foucaut", FoucautProjection.class, "Foucaut");
        registerEsri("Foucaut_Sinusoidal", FoucautSinusoidalProjection.class, "Foucaut Sinusoidal");
        registerEsri("Gall_Stereographic", GallProjection.class, "Gall (Gall Stereographic)");
        registerEsri("geos", GeostationarySatelliteProjection.class, "Geostationary Satellite");
//    registerEsri( "gins8", Projection.class, "Ginsburg VIII (TsNIIGAiK)" );
//    registerEsri( "gn_sinu", Projection.class, "General Sinusoidal Series" );
        registerEsri("Gnomonic", GnomonicAzimuthalProjection.class, "Gnomonic");
        registerEsri("Goode_Homolosine", GoodeProjection.class, "Goode Homolosine");
//    registerEsri( "gs48", Projection.class, "Mod. Stererographics of 48 U.S." );
//    registerEsri( "gs50", Projection.class, "Mod. Stererographics of 50 U.S." );
        registerEsri("Hammer_Aitoff", HammerProjection.class, "Hammer & Eckert-Greifendorff");
        registerEsri("Hatano_Asymmetrical_Equal_Area", HatanoProjection.class, "Hatano Asymmetrical Equal Area");
//    registerEsri( "imw_p", Projection.class, "Internation Map of the World Polyconic" );
        registerEsri("Kavraisky_V", KavraiskyVProjection.class, "Kavraisky V");
//    registerEsri( "kav7", Projection.class, "Kavraisky VII" );
//    registerEsri( "labrd", Projection.class, "Laborde" );
        registerEsri("Lambert_Azimuthal_Equal_Area", LambertAzimuthalEqualAreaProjection.class, "Lambert Azimuthal Equal Area");
        registerEsri("Lagrange", LagrangeProjection.class, "Lagrange");
        registerEsri("Larrivee", LarriveeProjection.class, "Larrivee");
        registerEsri("Laskowski", LaskowskiProjection.class, "Laskowski");
        registerEsri("WGS84", LongLatProjection.class, "Lat/Long");
        //registerEsri( "longlat", LongLatProjection.class, "Lat/Long" );
        registerEsri("Lambert_Conformal_Conic", LambertConformalConicProjection.class, "Lambert Conformal Conic");
        registerEsri("Lambert_Equal_Area_Conic", LambertEqualAreaConicProjection.class, "Lambert Equal Area Conic");
//    registerEsri( "lee_os", Projection.class, "Lee Oblated Stereographic" );
        registerEsri("Loximuthal", LoximuthalProjection.class, "Loximuthal");
        registerEsri("Space_Oblique_Mercator", LandsatProjection.class, "Space oblique for LANDSAT");
        registerEsri("McBryde_Thomas_Flat_Polar_Sine", McBrydeThomasFlatPolarSine1Projection.class, "McBryde-Thomas Flat-Polar Sine");
        registerEsri("McBryde_Thomas_Flat_Polar_Sine2", McBrydeThomasFlatPolarSine2Projection.class, "McBryde-Thomas Flat-Pole Sine (No. 2)");
        registerEsri("McBryde_Thomas_Flat_Polar_Parabolic", McBrydeThomasFlatPolarParabolicProjection.class, "McBride-Thomas Flat-Polar Parabolic");
        registerEsri("McBryde_Thomas_Flat_Polar_Quartic", McBrydeThomasFlatPolarQuarticProjection.class, "McBryde-Thomas Flat-Polar Quartic");
//    registerEsri( "mbtfps", Projection.class, "McBryde-Thomas Flat-Polar Sinusoidal" );
        registerEsri("Mercator", MercatorProjection.class, "Mercator");
//    registerEsri( "mil_os", Projection.class, "Miller Oblated Stereographic" );
        registerEsri("MillerCylindrical", MillerProjection.class, "Miller Cylindrical");
//    registerEsri( "mpoly", Projection.class, "Modified Polyconic" );
        registerEsri("Mollweide", MolleweideProjection.class, "Mollweide");
        registerEsri("Murdoch_I", Murdoch1Projection.class, "Murdoch I");
        registerEsri("Murdoch_II", Murdoch2Projection.class, "Murdoch II");
        registerEsri("Murdoch_III", Murdoch3Projection.class, "Murdoch III");
        registerEsri("Nell", NellProjection.class, "Nell");
//    registerEsri( "nell_h", Projection.class, "Nell-Hammer" );
        registerEsri("Nicolosi_Globular", NicolosiProjection.class, "Nicolosi Globular");
        registerEsri("Perspective", PerspectiveProjection.class, "Near-sided perspective");
//    registerEsri( "nzmg", Projection.class, "New Zealand Map Grid" );
//    registerEsri( "ob_tran", Projection.class, "General Oblique Transformation" );
//    registerEsri( "ocea", Projection.class, "Oblique Cylindrical Equal Area" );
//    registerEsri( "oea", Projection.class, "Oblated Equal Area" );
        registerEsri("Hotine_Oblique_Mercator", ObliqueMercatorProjection.class, "Oblique Mercator");
//    registerEsri( "ortel", Projection.class, "Ortelius Oval" );
        registerEsri("Orthographic", OrthographicAzimuthalProjection.class, "Orthographic");
        registerEsri("Perspective_Conic", PerspectiveConicProjection.class, "Perspective Conic");
        registerEsri("Polyconic", PolyconicProjection.class, "Polyconic (American)");
//    registerEsri( "putp1", Projection.class, "Putnins P1" );
        registerEsri("Putnins_P2", PutninsP2Projection.class, "Putnins P2");
//    registerEsri( "putp3", Projection.class, "Putnins P3" );
//    registerEsri( "putp3p", Projection.class, "Putnins P3'" );
        registerEsri("Putnins_P4", PutninsP4Projection.class, "Putnins P4'");
        registerEsri("Putnins_P5", PutninsP5Projection.class, "Putnins P5");
        registerEsri("Putnins_P5P", PutninsP5PProjection.class, "Putnins P5'");
//    registerEsri( "putp6", Projection.class, "Putnins P6" );
//    registerEsri( "putp6p", Projection.class, "Putnins P6'" );
        registerEsri("Quartic_Authalic", QuarticAuthalicProjection.class, "Quartic Authalic");
        registerEsri("Robinson", RobinsonProjection.class, "Robinson");
        registerEsri("Rectangular_Polyconic", RectangularPolyconicProjection.class, "Rectangular Polyconic");
        registerEsri("Sinusoidal", SinusoidalProjection.class, "Sinusoidal (Sanson-Flamsteed)");
        registerEsri("Swiss_Oblique_Mercator", SwissObliqueMercatorProjection.class, "Swiss Oblique Mercator");
        registerEsri("stere", StereographicAzimuthalProjection.class, "Stereographic");
        registerEsri("Stereographic", StereographicAzimuthalProjection.class, "Stereographic");
        registerEsri("sterea", ObliqueStereographicAlternativeProjection.class, "Oblique Stereographic Alternative");
        registerEsri("Tranverse_Central_Cylindrical", TranverseCentralCylindricalProjection.class, "Transverse Central Cylindrical");
        registerEsri("Transverse_Cylindrical_Equal_Area", TransverseCylindricalEqualArea.class, "Transverse Cylindrical Equal Area");
//    registerEsri( "tissot", TissotProjection.class, "Tissot Conic" );
        registerEsri("Transverse_Mercator", TransverseMercatorProjection.class, "Transverse Mercator");
//    registerEsri( "tpeqd", Projection.class, "Two Point Equidistant" );
//    registerEsri( "tpers", Projection.class, "Tilted perspective" );
//    registerEsri( "ups", Projection.class, "Universal Polar Stereographic" );
//    registerEsri( "urm5", Projection.class, "Urmaev V" );
        registerEsri("Urmaev_Flat_Polar_Sinusoidal", UrmaevFlatPolarSinusoidalProjection.class, "Urmaev Flat-Polar Sinusoidal");
        registerEsri("Universal_Transverse_Mercator", UniversalTransverseMercatorProjection.class, "Universal Transverse Mercator (UTM)");
        registerEsri("Van_der_Grinten_I", VanDerGrintenProjection.class, "van der Grinten (I)");
//    registerEsri( "vandg2", Projection.class, "van der Grinten II" );
//    registerEsri( "vandg3", Projection.class, "van der Grinten III" );
//    registerEsri( "vandg4", Projection.class, "van der Grinten IV" );
        registerEsri("Vitkovsky_I", VitkovskyProjection.class, "Vitkovsky I");
        registerEsri("Wagner_I", Wagner1Projection.class, "Wagner I (Kavraisky VI)");
        registerEsri("Wagner_II", Wagner2Projection.class, "Wagner II");
        registerEsri("Wagner_III", Wagner3Projection.class, "Wagner III");
        registerEsri("Wagner_IV", Wagner4Projection.class, "Wagner IV");
        registerEsri("Wagner_V", Wagner5Projection.class, "Wagner V");
//    registerEsri( "wag6", Projection.class, "Wagner VI" );
        registerEsri("Wagner_VII", Wagner7Projection.class, "Wagner VII");
        registerEsri("Werenskiold_I", WerenskioldProjection.class, "Werenskiold I");
//    registerEsri( "wink1", Projection.class, "Winkel I" );
//    registerEsri( "wink2", Projection.class, "Winkel II" );
        registerEsri("Winkel_Tripel", WinkelTripelProjection.class, "Winkel Tripel");
    }
}
