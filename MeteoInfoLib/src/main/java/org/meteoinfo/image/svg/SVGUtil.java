package org.meteoinfo.image.svg;

import javax.swing.*;
import java.net.URL;

public class SVGUtil {

    /**
     * Set SVG icon
     * @param button The button
     * @param iconPath SVG icon path
     */
    public static void setSVGIcon(AbstractButton button, String iconPath) {
        try {
            FlatSVGIcon icon = new FlatSVGIcon(iconPath);
            URL url = FlatSVGIcon.class.getClassLoader().getResource(iconPath);
            if (url != null) {
                button.setIcon(icon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set SVG icon
     * @param button The button
     * @param iconPath SVG icon path
     * @param classLoader ClassLoader
     */
    public static void setSVGIcon(AbstractButton button, String iconPath, ClassLoader classLoader) {
        try {
            FlatSVGIcon icon = new FlatSVGIcon(iconPath, classLoader);
            URL url = icon.getIconURL();
            if (url != null) {
                button.setIcon(icon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
