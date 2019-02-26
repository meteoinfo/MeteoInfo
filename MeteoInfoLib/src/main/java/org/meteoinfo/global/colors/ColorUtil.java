 /* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.global.colors;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.util.GlobalUtil;

/**
 * ColorUtiles class
 *
 * @author Yaqiang
 */
public class ColorUtil {
    // <editor-fold desc="Variables">   

    private final static Map colorNames;

    static {
        // color names.
        colorNames = new HashMap();
        colorNames.put("aliceblue", new Color(0xF0F8FF));
        colorNames.put("antiquewhite", new Color(0xFAEBD7));
        colorNames.put("black", new Color(0x000000));
        colorNames.put("green", new Color(0x008000));
        colorNames.put("silver", new Color(0xC0C0C0));
        colorNames.put("lime", new Color(0x00FF00));
        colorNames.put("gray", new Color(0x808080));
        colorNames.put("darkgray", new Color(0xA9A9A9));
        colorNames.put("olive", new Color(0x808000));
        colorNames.put("white", new Color(0xFFFFFF));
        colorNames.put("yellow", new Color(0xFFFF00));
        colorNames.put("maroon", new Color(0x800000));
        colorNames.put("navy", new Color(0x000080));
        colorNames.put("red", new Color(0xFF0000));
        colorNames.put("blue", new Color(0x0000FF));
        colorNames.put("purple", new Color(0x800080));
        colorNames.put("teal", new Color(0x008080));
        colorNames.put("fuchsia", new Color(0xFF00FF));
        colorNames.put("aqua", new Color(0x00FFFF));
        colorNames.put("transparent", new Color(0, 0, 0, 0));
    }

    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get common color
     *
     * @param idx Index
     * @return Common color
     */
    public static Color getCommonColor(int idx) {
//        if (idx == 0) {
//            idx = 1;
//        }
        if (idx > 11) {
            idx = idx % 11;
        }

        switch (idx) {
            case 0:
                return Color.red;
            case 1:
                return Color.blue;
            case 2:
                return Color.green;
            case 3:
                return Color.black;
            case 4:
                return Color.yellow;
            case 5:
                return Color.pink;
            case 6:
                return Color.gray;
            case 7:
                return Color.cyan;
            case 8:
                return Color.magenta;
            case 9:
                return Color.orange;
            case 10:
                return Color.darkGray;
            case 11:
                return Color.lightGray;
        }

        return Color.red;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Convert a color to hex string
     *
     * @param color a color
     * @return Hex string
     */
    public static String toHexEncoding(Color color) {
        String A, R, G, B;
        StringBuilder sb = new StringBuilder();

        A = Integer.toHexString(color.getAlpha());
        R = Integer.toHexString(color.getRed());
        G = Integer.toHexString(color.getGreen());
        B = Integer.toHexString(color.getBlue());

        A = A.length() == 1 ? "0" + A : A;
        R = R.length() == 1 ? "0" + R : R;
        G = G.length() == 1 ? "0" + G : G;
        B = B.length() == 1 ? "0" + B : B;

        sb.append("0x");
        sb.append(A);
        sb.append(R);
        sb.append(G);
        sb.append(B);

        return sb.toString();
    }

    /**
     * Parse hex string to color
     *
     * @param c hex string
     * @return Color
     */
    public static Color parseToColor(final String c) {
        //Color convertedColor = (Color) colorNames.get(c.trim().toLowerCase());
        Color convertedColor = Color.white;
        try {
            WebColor webColor = WebColor.valueOf(c.trim());
            convertedColor = WebColor.valueOf(c.trim()).getColor();
        } catch (IllegalArgumentException e) {
            try {
                if (c.length() == 10) {
                    String aStr = c.substring(2, 4);
                    String cStr = c.substring(0, 2) + c.substring(4);
                    int alpha = Integer.parseInt(aStr, 16);
                    //int rgb = Integer.parseInt(cStr);
                    convertedColor = Color.decode(cStr);
                    //convertedColor = new Color(rgb);
                    convertedColor = new Color(convertedColor.getRed(), convertedColor.getGreen(), convertedColor.getBlue(), alpha);
                } else {
                    //convertedColor = new Color(Integer.parseInt(c, 16));
                    convertedColor = Color.decode(c);
                }
            } catch (NumberFormatException ne) {
                // codes to deal with this exception
                //convertedColor = Color.white;
            }
        }

        return convertedColor;
    }

    /**
     * Modifies an existing brightness level of a color
     *
     * @param c The color
     * @param brightness The brightness
     * @return Adjusted color
     */
    public static Color modifyBrightness(Color c, float brightness) {
        float hsbVals[] = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        return Color.getHSBColor(hsbVals[0], hsbVals[1], brightness * hsbVals[2]);
    }

    /**
     * Convert color to KML color string - AABBGGRR
     *
     * @param color The color
     * @return KML color string
     */
    public static String toKMLColor(Color color) {
        String A, R, G, B;
        StringBuilder sb = new StringBuilder();

        A = Integer.toHexString(color.getAlpha()).toUpperCase();
        R = Integer.toHexString(color.getRed()).toUpperCase();
        G = Integer.toHexString(color.getGreen()).toUpperCase();
        B = Integer.toHexString(color.getBlue()).toUpperCase();

        A = A.length() == 1 ? "0" + A : A;
        R = R.length() == 1 ? "0" + R : R;
        G = G.length() == 1 ? "0" + G : G;
        B = B.length() == 1 ? "0" + B : B;

        //sb.append("0x");
        sb.append(A);
        sb.append(B);
        sb.append(G);
        sb.append(R);

        return sb.toString();
    }

    /**
     * Get color tables
     *
     * @return Color tables
     * @throws IOException
     */
    public static ColorMap[] getColorTables() throws IOException {
        String fn = GlobalUtil.getAppPath(ColorUtil.class);
        boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");
        if (isDebug) {
            fn = "D:/MyProgram/java/MeteoInfoDev/MeteoInfoMap/";
        }     
        fn = fn.substring(0, fn.lastIndexOf("/"));
        String path = fn + File.separator + "colormaps";
        File pathDir = new File(path);
        if (!pathDir.isDirectory()) {
            return null;
        }

        File[] files = pathDir.listFiles();
        List<ColorMap> cts = new ArrayList<>();
        for (File file : files) {
            //InputStream is = ColorUtil.class.getResourceAsStream(pdir + "/" + fileName);
            //System.out.println(file.getAbsolutePath());
            ColorMap ct = new ColorMap();
            ct.readFromFile(file);            
            if (ct.getColorCount() > 0) {
                String name = file.getName();
                name = name.substring(0, name.lastIndexOf("."));
                ct.setName(name);
                cts.add(ct);
            }
        }

        ColorMap[] ncts = new ColorMap[cts.size()];
        for (int i = 0; i < cts.size(); i++) {
            ncts[i] = cts.get(i);
        }

        return ncts;
    }
    
    /**
     * Get color map
     * @param ctName Color map name
     * @return Color table
     * @throws java.io.IOException
     */
    public static ColorMap getColorMap(String ctName) throws IOException{
        String fn = GlobalUtil.getAppPath(ColorUtil.class);
        boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");
        if (isDebug) {
            fn = "D:/MyProgram/java/MeteoInfoDev/MeteoInfoMap/";
        }     
        fn = fn.substring(0, fn.lastIndexOf("/"));
        String path = fn + File.separator + "colormaps";
        File pathDir = new File(path);
        if (!pathDir.isDirectory()) {
            return null;
        }
        
        fn = path + File.separator + ctName + ".rgb";
        File ctFile = new File(fn);
        if (!ctFile.isFile())
            return null;                
        
        ColorMap ct = new ColorMap();
        ct.readFromFile(fn);
        
        return ct;
    }
    
    /**
     * Get color map
     * @param ctName Color map name
     * @param alpha Alpha
     * @return Color table
     * @throws java.io.IOException
     */
    public static ColorMap getColorMap(String ctName, int alpha) throws IOException{
        String fn = GlobalUtil.getAppPath(ColorUtil.class);
        boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");
        if (isDebug) {
            fn = "D:/MyProgram/java/MeteoInfoDev/MeteoInfoMap/";
        }     
        fn = fn.substring(0, fn.lastIndexOf("/"));
        String path = fn + File.separator + "colormaps";
        File pathDir = new File(path);
        if (!pathDir.isDirectory()) {
            return null;
        }
        
        fn = path + File.separator + ctName + ".rgb";
        File ctFile = new File(fn);
        if (!ctFile.isFile())
            return null;                
        
        ColorMap ct = new ColorMap();
        ct.readFromFile(fn, alpha);
        
        return ct;
    }
    
    /**
     * Find color table
     * @param cts Color tables
     * @param name Color table name
     * @return Finded color table
     */
    public static ColorMap findColorTable(ColorMap[] cts, String name){
        for (ColorMap ct : cts){
            if (ct.getName().equalsIgnoreCase(name))
                return ct;
        }
        
        return null;
    }

    /**
     * Get color tables
     *
     * @return Color tables
     * @throws IOException
     */
    public static ColorMap[] getColorTables_old() throws IOException {
        String pdir = "/images/colortables";
        List<String> fns = new ArrayList<>();
        fns.add("grads_rainbow.rgb");
        fns.add("GHRSST_anomaly.rgb");
        fns.add("amwg256.rgb");
        fns.add("cmp_b2r.rgb");
        fns.add("cmp_flux.rgb");
        fns.add("cmp_haxby.rgb");
        fns.add("matlab_hot.rgb");
        fns.add("matlab_hsv.rgb");
        fns.add("matlab_jet.rgb");
        fns.add("matlab_lines.rgb");
        fns.add("ncl_default.rgb");
        fns.add("ncview_default.ncmap");
        fns.add("rainbow+white+gray.gp");
        fns.add("rainbow.gp");
        fns.add("seaice_1.rgb");
        fns.add("seaice_2.rgb");

        List<ColorMap> cts = new ArrayList<>();
        for (String fileName : fns) {
            InputStream is = ColorUtil.class.getResourceAsStream(pdir + "/" + fileName);
            ColorMap ct = new ColorMap();
            ct.readFromFile(is);
            cts.add(ct);
        }

        ColorMap[] ncts = new ColorMap[cts.size()];
        for (int i = 0; i < cts.size(); i++) {
            ncts[i] = cts.get(i);
        }

        return ncts;
    }
    
    /**
     * Create colors from start and end color
     *
     * @param sColor Start color
     * @param eColor End color
     * @param cNum Color number
     * @return Color array
     */
    public static Color[] createColors(Color sColor, Color eColor, int cNum) {
        Color[] colors = new Color[cNum];
        int sR, sG, sB, eR, eG, eB;
        int rStep, gStep, bStep;
        int i;
        
        sR = sColor.getRed();
        sG = sColor.getGreen();
        sB = sColor.getBlue();
        eR = eColor.getRed();
        eG = eColor.getGreen();
        eB = eColor.getBlue();
        rStep = (int) ((eR - sR) / cNum);
        gStep = (int) ((eG - sG) / cNum);
        bStep = (int) ((eB - sB) / cNum);
        for (i = 0; i < colors.length; i++) {
            colors[i] = new Color(sR + i * rStep, sG + i * gStep, sB + i * bStep);
        }
        
        return colors;
    }
    
    /**
     * Create colors
     * @param cm Color map
     * @param min Min value
     * @param max Max value
     * @return Colors
     */
    public static Color[] createColors(ColorMap cm, double min, double max){
        double[] values = MIMath.getIntervalValues(min, max);
        return cm.getColors(values.length + 1);
    }
    
    /**
     * Create colors
     * @param cm Color map
     * @param min Min value
     * @param max Max value
     * @param n Color number
     * @return Colors
     */
    public static Color[] createColors(ColorMap cm, double min, double max, int n){
        double[] values = MIMath.getIntervalValues(min, max, n);
        return cm.getColors(values.length + 1);
    }
    
    /**
     * Create color from start and end color
     *
     * @param sColor Start color
     * @param eColor End color
     * @param p Proportion
     * @return Color
     */
    public static Color createColor(Color sColor, Color eColor, float p) {
        int sR, sG, sB, eR, eG, eB, r, g, b;
        
        sR = sColor.getRed();
        sG = sColor.getGreen();
        sB = sColor.getBlue();
        eR = eColor.getRed();
        eG = eColor.getGreen();
        eB = eColor.getBlue();
        r = (int) (sR + (eR - sR) * p);
        g = (int) (sG + (eG - sG) * p);
        b = (int) (sB + (eB - sB) * p);        
        
        return new Color(r, g, b);
    }
    
    /**
     * Create rainbow colors
     *
     * @param cNum Color number
     * @return Rainbow color array
     */
    public static Color[] createRainBowColors(int cNum) {
        if (cNum > 13) {
            //return getRainBowColors_HSL(cNum);
            return getRainBowColors_HSV(cNum);
        }
        
        List<Color> colorList = new ArrayList<>();
        
        colorList.add(new Color(160, 0, 200));
        colorList.add(new Color(110, 0, 220));
        colorList.add(new Color(30, 60, 255));
        colorList.add(new Color(0, 160, 255));
        colorList.add(new Color(0, 200, 200));
        colorList.add(new Color(0, 210, 140));
        colorList.add(new Color(0, 220, 0));
        colorList.add(new Color(160, 230, 50));
        colorList.add(new Color(230, 220, 50));
        colorList.add(new Color(230, 175, 45));
        colorList.add(new Color(240, 130, 40));
        colorList.add(new Color(250, 60, 60));
        colorList.add(new Color(240, 0, 130));
        
        switch (cNum) {
            case 12:
                colorList.remove(new Color(0, 210, 140));
                break;
            case 11:
                colorList.remove(new Color(0, 210, 140));
                colorList.remove(new Color(30, 60, 255));
                break;
            case 10:
                colorList.remove(new Color(0, 210, 140));
                colorList.remove(new Color(30, 60, 255));
                colorList.remove(new Color(230, 175, 45));
                break;
            case 9:
                colorList.remove(new Color(0, 210, 140));
                colorList.remove(new Color(30, 60, 255));
                colorList.remove(new Color(230, 175, 45));
                colorList.remove(new Color(160, 230, 50));
                break;
            case 8:
                colorList.remove(new Color(0, 210, 140));
                colorList.remove(new Color(30, 60, 255));
                colorList.remove(new Color(230, 175, 45));
                colorList.remove(new Color(160, 230, 50));
                colorList.remove(new Color(110, 0, 220));
                break;
            case 7:
                colorList.remove(new Color(0, 210, 140));
                colorList.remove(new Color(30, 60, 255));
                colorList.remove(new Color(230, 175, 45));
                colorList.remove(new Color(160, 230, 50));
                colorList.remove(new Color(110, 0, 220));
                colorList.remove(new Color(0, 200, 200));
                break;
            case 6:
                colorList.remove(new Color(0, 210, 140));
                colorList.remove(new Color(30, 60, 255));
                colorList.remove(new Color(230, 175, 45));
                colorList.remove(new Color(160, 230, 50));
                colorList.remove(new Color(110, 0, 220));
                colorList.remove(new Color(0, 200, 200));
                colorList.remove(new Color(240, 130, 40));
                break;
            case 5:
                colorList.remove(new Color(0, 210, 140));
                colorList.remove(new Color(30, 60, 255));
                colorList.remove(new Color(230, 175, 45));
                colorList.remove(new Color(160, 230, 50));
                colorList.remove(new Color(110, 0, 220));
                colorList.remove(new Color(0, 200, 200));
                colorList.remove(new Color(240, 130, 40));
                colorList.remove(new Color(160, 0, 200));
                break;
        }
        
        Color[] colors = new Color[cNum];
        for (int i = 0; i < cNum; i++) {
            colors[i] = colorList.get(i);
        }
        
        return colors;
    }
    
    /**
     * Get rainbow color by HSV/HSB
     *
     * @param cNum Color number
     * @return Rainbow colors
     */
    public static Color[] getRainBowColors_HSV(int cNum) {
        double p = 360.0 / cNum;
        Color[] colors = new Color[cNum];
        for (int i = 0; i < cNum; i++) {
            colors[cNum - i - 1] = Color.getHSBColor((float) (i * p), 1.0f, 1.0f);
        }
        
        return colors;
    }
    
    /**
     * Create a random color
     * @return A random color
     */
    public static Color createRandomColor(){
        Random randomColor = new Random();
        return new Color(randomColor.nextInt(256), randomColor.nextInt(256), randomColor.nextInt(256));
    }
    
    /**
     * Create random colors
     *
     * @param cNum Color number
     * @return The random colors
     */
    public static Color[] createRandomColors(int cNum) {
        Color[] colors = new Color[cNum];
        int i;
        Random randomColor = new Random();
        
        for (i = 0; i < cNum; i++) {
            colors[i] = new Color(randomColor.nextInt(256),
                    randomColor.nextInt(256), randomColor.nextInt(256));
        }
        
        return colors;
    }
    // </editor-fold>
}
