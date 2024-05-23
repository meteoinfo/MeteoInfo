package org.meteoinfo.data.meteodata.radar.cinrad;

public class Cinrad2Record {

    /** Reflectivity moment identifier */
    public static final int REFLECTIVITY = 1;

    /** Radial Velocity moment identifier */
    public static final int VELOCITY_HI = 2;

    /** Radial Velocity moment identifier */
    public static final int VELOCITY_LOW = 4;

    /** Spectrum Width moment identifier */
    public static final int SPECTRUM_WIDTH = 3;

    /** Low doppler resolution code */
    public static final int DOPPLER_RESOLUTION_LOW_CODE = 4;

    /** High doppler resolution code */
    public static final int DOPPLER_RESOLUTION_HIGH_CODE = 2;

    /** Horizontal beam width */
    public static final float HORIZONTAL_BEAM_WIDTH = (float) 1.5; // LOOK

    public static byte MISSING_DATA = (byte) 1;
    public static final byte BELOW_THRESHOLD = (byte) 0;

    /** Size of the file header, aka title */
    static int FILE_HEADER_SIZE = 0;

    /** Size of the CTM record header */
    private static int CTM_HEADER_SIZE = 14;

    /** Size of the message header, to start of the data message */
    private static final int MESSAGE_HEADER_SIZE = 28;

    /** Size of the entire message, if its a radar data message */
    private static int RADAR_DATA_SIZE = 2432;

    public static String getDatatypeName(int datatype) {
        switch (datatype) {
            case REFLECTIVITY:
                return "Reflectivity";
            case VELOCITY_HI:
            case VELOCITY_LOW:
                return "RadialVelocity";
            case SPECTRUM_WIDTH:
                return "SpectrumWidth";
            default:
                throw new IllegalArgumentException();
        }
    }

    public static String getDatatypeUnits(int datatype) {
        switch (datatype) {
            case REFLECTIVITY:
                return "dBz";

            case VELOCITY_HI:
            case VELOCITY_LOW:
            case SPECTRUM_WIDTH:
                return "m/s";
        }
        throw new IllegalArgumentException();
    }

    public static float getDatatypeScaleFactor(int datatype) {
        switch (datatype) {
            case REFLECTIVITY:
                if (CinradDataInfo.isCC)
                    return 0.1f;
                if (CinradDataInfo.isCC20)
                    return 0.5f;
                else
                    return 0.5f;
            case VELOCITY_LOW:
                if (CinradDataInfo.isSC)
                    return 0.3673f;
                else if (CinradDataInfo.isCC)
                    return 0.1f;
                else
                    return 1.0f;
            case VELOCITY_HI:
            case SPECTRUM_WIDTH:
                if (CinradDataInfo.isSC)
                    return 0.1822f;
                else if (CinradDataInfo.isCC)
                    return 0.1f;
                else if (CinradDataInfo.isCC20)
                    return 1.0f;
                else
                    return 0.5f;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static float getDatatypeAddOffset(int datatype) {
        switch (datatype) {
            case REFLECTIVITY:
                if (CinradDataInfo.isSC)
                    return -32.0f;
                else if (CinradDataInfo.isCC)
                    return 0.0f;
                else if (CinradDataInfo.isCC20)
                    return -32.0f;
                else
                    return -33.0f;
            case VELOCITY_LOW:
                if (CinradDataInfo.isSC)
                    return 0.0f;
                else if (CinradDataInfo.isCC)
                    return 0.0f;
                else if (CinradDataInfo.isCC20)
                    return 0.0f;
                else
                    return -129.0f;
            case VELOCITY_HI:
            case SPECTRUM_WIDTH:
                if (CinradDataInfo.isSC)
                    return 0.0f;
                else if (CinradDataInfo.isCC)
                    return 0.0f;
                else if (CinradDataInfo.isCC20)
                    return 0.0f;
                else
                    return -64.5f;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static String getMessageTypeName(int code) {
        switch (code) {
            case 1:
                return "digital radar data";
            case 2:
                return "RDA status data";
            case 3:
                return "performance/maintainence data";
            case 4:
                return "console message - RDA to RPG";
            case 5:
                return "maintainence log data";
            case 6:
                return "RDA control ocmmands";
            case 7:
                return "volume coverage pattern";
            case 8:
                return "clutter censor zones";
            case 9:
                return "request for data";
            case 10:
                return "console message - RPG to RDA";
            case 11:
                return "loop back test - RDA to RPG";
            case 12:
                return "loop back test - RPG to RDA";
            case 13:
                return "clutter filter bypass map - RDA to RPG";
            case 14:
                return "edited clutter filter bypass map - RDA to RPG";
            case 15:
                return "Notchwidth Map";
            case 18:
                return "RDA Adaptation data";
            default:
                return "unknown " + code;
        }
    }

    public static String getRadialStatusName(int code) {
        switch (code) {
            case 0:
                return "start of new elevation";
            case 1:
                return "intermediate radial";
            case 2:
                return "end of elevation";
            case 3:
                return "begin volume scan";
            case 4:
                return "end volume scan";
            default:
                return "unknown " + code;
        }
    }

    public static String getVolumeCoveragePatternName(int code) {
        switch (code) {
            case 11:
                return "16 elevation scans every 5 mins";
            case 12:
                return "14 elevation scan every 4.1 mins";
            case 21:
                return "11 elevation scans every 6 mins";
            case 31:
                return "8 elevation scans every 10 mins";
            case 32:
                return "7 elevation scans every 10 mins";
            case 121:
                return "9 elevations, 20 scans every 5 minutes";
            default:
                return "unknown " + code;
        }
    }
}
