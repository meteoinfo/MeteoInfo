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
package org.meteoinfo.ui.util;

 import org.meteoinfo.common.util.GlobalUtil;

 import java.awt.*;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
//import org.meteoinfo.drawing.Draw;
//import org.meteoinfo.legend.MapFrame;

 /**
  *
  * @author yaqiang
  */
 public class FontUtil {

     /**
      * Get all available fonts (system fonts, weather font and custom fonts)
      *
      * @return Font list
      */
     public static List<Font> getAllFonts() {
         List<Font> fontList = new ArrayList<>();

         //Weather font
         Font weatherFont = getWeatherFont();
         if (weatherFont != null) {
             fontList.add(weatherFont);
         }

         //System fonts
         GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
         Font[] fonts = gEnv.getAllFonts();
         for (Font font : fonts) {
             fontList.add(font);
         }

         //Custom fonts
         String fn = GlobalUtil.getAppPath(FontUtil.class);
         fn = fn.substring(0, fn.lastIndexOf("/"));
         String path = fn + File.separator + "font";
         File pathDir = new File(path);
         if (pathDir.isDirectory()) {

         }

         return fontList;
     }

     /**
      * Register a font
      * @param font The font
      */
     public static void registerFont(Font font){
         GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
     }

     /**
      * Register a font
      * @param fileName Font file name
      */
     public static void registerFont(String fileName){
         Font font = getFont(fileName);
         if (font != null){
             registerFont(font);
         }
     }

     /**
      * Register weather font
      */
     public static void registerWeatherFont(){
         Font weatherFont = getWeatherFont();
         if (weatherFont != null) {
             GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(weatherFont);
         }
         Font yaHeiFont = getYaheiHybridFont();
         if (yaHeiFont != null) {
             GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(yaHeiFont);
         }
     }

     /**
      * Get font from font file - .ttf
      * @param fileName Font file name
      * @return The font
      */
     public static Font getFont(String fileName){
         Font font = null;
         try {
             font = Font.createFont(Font.TRUETYPE_FONT, new File(fileName));
         } catch (FontFormatException | IOException ex) {
             Logger.getLogger(FontUtil.class.getName()).log(Level.SEVERE, null, ex);
         }
         return font;
     }

     /**
      * Get weather symbol font
      *
      * @return Weather symbol font
      */
     public static Font getWeatherFont() {
         Font font = null;
         InputStream is = FontUtil.class.getResourceAsStream("/fonts/WeatherSymbol.ttf");
         try {
             font = Font.createFont(Font.TRUETYPE_FONT, is);
         } catch (FontFormatException | IOException ex) {
             Logger.getLogger(FontUtil.class.getName()).log(Level.SEVERE, null, ex);
         }

         return font;
     }

     /**
      * Get yahei consolas hybrid font
      *
      * @return Yahei consolas hybrid font
      */
     public static Font getYaheiHybridFont() {
         Font font = null;
         InputStream is = FontUtil.class.getResourceAsStream("/fonts/YaheiHybrid.ttf");
         try {
             font = Font.createFont(Font.TRUETYPE_FONT, is);
         } catch (FontFormatException | IOException ex) {
             Logger.getLogger(FontUtil.class.getName()).log(Level.SEVERE, null, ex);
         }

         return font;
     }
 }
