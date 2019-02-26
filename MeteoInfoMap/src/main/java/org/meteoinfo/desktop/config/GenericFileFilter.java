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
public class GenericFileFilter extends FileFilter {

    private static final boolean ONE = true;
    private String fileExt;
    private String[] fileExts;
    private boolean type = false;
    private String description;
    private int length;
    private String extension;

    /**
     * This is the constructor - it takes in the following:-<br> filesExtsIn -
     * this is the array of file extensions that you wish to create a file
     * filter for. <br> description - this is the description that will be
     * displayed in the file chooser dialog box.
     * @param filesExtsIn File extensions
     * @param description Description
     */
    public GenericFileFilter(String[] filesExtsIn, String description) {
        if (filesExtsIn.length == 1) {//we only have one file
            type = ONE;
            fileExt = filesExtsIn[0];
        } else {
            fileExts = filesExtsIn;
            length = fileExts.length;
        }
        this.description = description;
    }
    
    /**
     * Get file extent
     * 
     * @return File extent
     */
    public String getFileExtent(){
        return fileExt;
    }

    /**
     * This is the method to allow a file to bee added to the displayed list or
     * not. This method is called by the model that handles the FileChooser
     * dialog
     * @param f File
     * @return Boolean
     */
    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        extension = getExtension(f);
        if (extension != null) {
            if (type) {
                return check(fileExt);
            } else {
                for (int i = 0; i < length; i++) {
                    if (check(fileExts[i])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * This checks to see if the extension (stored) is the same as the file type
     * stored at construction time. The "in" being the value passed over.
     */
    private boolean check(String in) {
        return extension.equalsIgnoreCase(in);
    }

    /**
     * This is the method defined by the model
     * @return 
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * This is the method to get the file extension from the file name
     */
    private String getExtension(File file) {
        String filename = file.getName();
        int len = filename.length();
        int i = filename.lastIndexOf('.');
        if (i > 0 && i < len - 1) {
            return filename.substring(i + 1).toLowerCase();
        }
        return null;
    }
}
