/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author wyq
 */
public class MyClassLoader extends URLClassLoader {
    
    public MyClassLoader() {
        super(new URL[]{});
    }

    public MyClassLoader(URL[] urls) {
        super(urls);
    }
    
    public MyClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public void addFile (String path) throws MalformedURLException
    {
        String urlPath = "jar:file://" + path + "!/";
        addURL (new URL (urlPath));
    }
}
