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
package org.meteoinfo.desktop.config;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author yaqiang
 */
public class MapFileFilter extends FileFilter {

    @Override
    public boolean accept(File f) {

        if (f.isDirectory()) {
            return true;
        }

        //get the extension of the file
        String extension = getExtension(f);
        //check to see if the extension is equal to "html" or "htm"
        if ((extension.equals("shp")) || (extension.equals("wmp"))) {
            return true;
        }
        //default -- fall through. False is return on all
        //occasions except:
        //a) the file is a directory
        //b) the file's extension is what we are looking for.
        return false;
    }

    @Override
    public String getDescription() {
        return "Shape File (*.shp)";
    }

    private String getExtension(File f) {
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            return s.substring(i + 1).toLowerCase();
        }
        return "";
    }
}
